package com.github.hekonsek.rxjava.view.document;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class DocumentWithKey {

    private final String key;

    private final Map<String, Object> document;

    public DocumentWithKey(String key, Map<String, Object> document) {
        this.key = key;
        this.document = ImmutableMap.copyOf(document);
    }

    public String key() {
        return key;
    }

    public Map<String, Object> document() {
        return document;
    }

}