package com.github.hekonsek.rxjava.view.document;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

import java.util.Map;

/**
 * Represents operations common for materialized views based on documents.
 */
public interface DocumentView {

    Completable save(String collection, String key, Map<String, Object> document);

    Maybe<Map<String, Object>> findById(String collection, String key);

    Single<Long> count(String collection);

    Observable<DocumentWithKey> findAll(String collection);

    Completable remove(String collection, String key);

}