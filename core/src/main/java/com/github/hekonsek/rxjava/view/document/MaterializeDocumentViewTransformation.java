package com.github.hekonsek.rxjava.view.document;

import com.github.hekonsek.rxjava.event.Event;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.github.hekonsek.rxjava.event.Headers.requiredAddress;
import static com.github.hekonsek.rxjava.event.Headers.requiredKey;
import static com.github.hekonsek.rxjava.failable.FailableFlatMap.failable;

public class MaterializeDocumentViewTransformation implements ObservableTransformer<Event<Map<String, Object>>, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(MaterializeDocumentViewTransformation.class);

    private final DocumentView view;

    private final BiConsumer<Throwable, Event<Map<String, Object>>> deadLetterQueue;

    public MaterializeDocumentViewTransformation(DocumentView view,
            BiConsumer<Throwable, Event<Map<String, Object>>> deadLetterQueue) {
        this.view = view;
        this.deadLetterQueue = deadLetterQueue;
    }

    public MaterializeDocumentViewTransformation(DocumentView view) {
        this(view, (ex, value) -> LOG.info("Failed to save document: " + value, ex));
    }

    public static MaterializeDocumentViewTransformation materialize(DocumentView view,
            BiConsumer<Throwable, Event<Map<String, Object>>> deadLetterQueue) {
        return new MaterializeDocumentViewTransformation(view, deadLetterQueue);
    }

    public static MaterializeDocumentViewTransformation materialize(DocumentView view) {
        return new MaterializeDocumentViewTransformation(view);
    }

    @Override public ObservableSource<Object> apply(Observable<Event<Map<String, Object>>> events) {
        return events.compose(failable(
                event -> {
                    if (event.payload() != null) {
                        return view.save(requiredAddress(event), requiredKey(event), event.payload()).toObservable();
                    } else {
                        return view.remove(requiredAddress(event), requiredKey(event)).toObservable();
                    }
                },
                deadLetterQueue
        ));
    }

}
