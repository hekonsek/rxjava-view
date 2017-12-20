package com.github.hekonsek.rxjava.view.document;

import com.github.hekonsek.rxjava.event.Event;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.github.hekonsek.rxjava.event.Headers.requiredAddress;
import static com.github.hekonsek.rxjava.event.Headers.requiredKey;
import static com.github.hekonsek.rxjava.failable.FailableFlatMap.failable;

public class MaterializeDocumentViewTransformation implements ObservableTransformer<Event<Map<String, Object>>, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(MaterializeDocumentViewTransformation.class);

    private final DocumentView view;

    public MaterializeDocumentViewTransformation(DocumentView view) {
        this.view = view;
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
                failure -> LOG.info("Failed to save document: " + failure.value(), failure.cause())
        ));
    }

}
