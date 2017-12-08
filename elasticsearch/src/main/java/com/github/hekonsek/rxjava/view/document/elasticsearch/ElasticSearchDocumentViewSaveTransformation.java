package com.github.hekonsek.rxjava.view.document.elasticsearch;

import com.github.hekonsek.rxjava.event.Event;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.github.hekonsek.rxjava.event.Headers.address;
import static com.github.hekonsek.rxjava.event.Headers.key;
import static com.github.hekonsek.rxjava.failable.FailableFlatMap.failable;

public class ElasticSearchDocumentViewSaveTransformation implements ObservableTransformer<Event<Map<String, Object>>, Object> {

    private final static Logger LOG = LoggerFactory.getLogger(ElasticSearchDocumentViewSaveTransformation.class);

    private final ElasticSearchDocumentView view;

    public ElasticSearchDocumentViewSaveTransformation(ElasticSearchDocumentView view) {
        this.view = view;
    }

    public static ElasticSearchDocumentViewSaveTransformation save(ElasticSearchDocumentView view) {
        return new ElasticSearchDocumentViewSaveTransformation(view);
    }

    @Override public ObservableSource<Object> apply(Observable<Event<Map<String, Object>>> events) {
        return events.compose(failable(
                event -> {
                    if (event.payload() != null) {
                        return view.save(address(event), key(event), event.payload()).toObservable();
                    } else {
                        return view.remove(address(event), key(event)).toObservable();
                    }
                },
                failure -> LOG.info("Failed to save document: " + failure.value(), failure.cause())
        ));
    }

}
