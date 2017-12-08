/**
 * Licensed to the RxJava View under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hekonsek.rxjava.view.document.elasticsearch;

import com.github.hekonsek.rxjava.view.document.DocumentView;
import com.github.hekonsek.rxjava.view.document.DocumentWithKey;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import static io.reactivex.Observable.fromIterable;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Document-based materialized view based on ElasticSearch documents.
 */
public class ElasticSearchDocumentView implements DocumentView {

    // Collaborators

    private PreBuiltTransportClient client;

    // Configuration members

    private String clusterName = "default";

    private String host = "localhost";

    private int port = 9300;

    // Life-cycle

    public ElasticSearchDocumentView start() {
        try {
            Settings settings = Settings.builder().put("cluster.name", clusterName).build();
            client = new PreBuiltTransportClient(settings);
            client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
            return this;
        } catch (UnknownHostException e) {
            if (client != null) {
                client.close();
            }
            throw new RuntimeException(e);
        }
    }

    // Overridden operations

    @Override public Completable save(String collection, String key, Map<String, Object> document) {
        return new Completable() {

            @Override protected void subscribeActual(CompletableObserver observer) {
                try {
                    client.prepareIndex(collection, "default", key).setSource(document).execute(new ActionListener<IndexResponse>() {

                        @Override public void onResponse(IndexResponse indexResponse) {
                            observer.onComplete();
                        }

                        @Override public void onFailure(Exception e) {
                            observer.onError(e);
                        }
                    });
                } catch (Throwable e) {
                    observer.onError(e);
                }
            }
        };
    }

    @Override public Maybe<Map<String, Object>> findById(String collection, String key) {
        return Maybe.create(subscription -> client.prepareGet(collection, "default", key).execute(new ActionListener<GetResponse>() {

            @Override public void onResponse(GetResponse documentFields) {
                Map<String, Object> result = documentFields.getSource();
                if (result != null) {
                    subscription.onSuccess(result);
                } else {
                    subscription.onComplete();
                }
            }

            @Override public void onFailure(Exception e) {
                if (e.getCause() != null && IndexNotFoundException.class.isAssignableFrom(e.getCause().getClass())) {
                    subscription.onComplete();
                    return;
                }
                subscription.onError(e);
            }
        }));
    }

    @Override public Single<Long> count(String collection) {
        SearchResponse response = client.prepareSearch(collection).setTypes("default").get();
        return Single.just(response.getHits().totalHits);
    }

    @Override public Observable<DocumentWithKey> findAll(String collection) {
        SearchResponse response = client.prepareSearch(collection).setTypes("default").get();
        return fromIterable(stream(response.getHits().getHits()).
                map(hit -> new DocumentWithKey(hit.getId(), hit.getSourceAsMap())).collect(toList()));
    }

    @Override public Completable remove(String collection, String key) {
        return new Completable() {

            @Override protected void subscribeActual(CompletableObserver observer) {
                client.prepareDelete(collection, "default", key).execute(new ActionListener<DeleteResponse>() {

                    @Override public void onResponse(DeleteResponse deleteResponse) {
                        observer.onComplete();
                    }

                    @Override public void onFailure(Exception e) {
                        observer.onError(e);
                    }
                });
            }
        };
    }

    // Setters

    public ElasticSearchDocumentView clusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public ElasticSearchDocumentView host(String host) {
        this.host = host;
        return this;
    }

    public ElasticSearchDocumentView port(int port) {
        this.port = port;
        return this;
    }

    // Getters

    public Client client() {
        return client;
    }

}