package com.github.hekonsek.rxjava.view.document.memory;

import com.github.hekonsek.rxjava.view.document.DocumentView;
import com.github.hekonsek.rxjava.view.document.DocumentWithKey;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.reactivex.Completable.complete;
import static io.reactivex.Maybe.empty;
import static io.reactivex.Observable.fromIterable;
import static java.util.stream.Collectors.toList;

public class InMemoryDocumentView implements DocumentView {

    private final Map<String, Map<String, Map<String, Object>>> documents = new LinkedHashMap<>();

    @Override synchronized public Completable save(String collection, String key, Map<String, Object> document) {
        Map<String, Map<String, Object>> collectionData = documents.computeIfAbsent(collection, k -> new LinkedHashMap<>());
        collectionData.put(key, document);
        return complete();
    }

    @Override synchronized public Maybe<Map<String, Object>> findById(String collection, String key) {
        Map<String, Map<String, Object>> collectionData = documents.computeIfAbsent(collection, k -> new LinkedHashMap<>());
        Map<String, Object> result = collectionData.get(key);
        return result != null ? Maybe.just(result) : empty();
    }

    @Override synchronized public Single<Long> count(String collection) {
        Map<String, Map<String, Object>> collectionData = documents.computeIfAbsent(collection, key -> new LinkedHashMap<>());
        return Single.just((long) collectionData.size());
    }

    @Override synchronized public Observable<DocumentWithKey> findAll(String collection) {
        List<DocumentWithKey> documentsWithIds = documents.computeIfAbsent(collection, key -> new LinkedHashMap<>()).entrySet().stream().
                map(entry -> new DocumentWithKey(entry.getKey(), entry.getValue())).collect(toList());
        return fromIterable(documentsWithIds);
    }

    @Override synchronized public Completable remove(String collection, String key) {
        Map<String, Map<String, Object>> collectionData = documents.computeIfAbsent(collection, k -> new LinkedHashMap<>());
        collectionData.remove(key);
        return complete();
    }

}