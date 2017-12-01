package com.github.hekonsek.rxjava.view.document.elasticsearch;

import com.github.hekonsek.rxjava.view.document.DocumentView;
import com.github.hekonsek.rxjava.view.document.DocumentWithKey;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.elasticsearch.action.ActionListener;
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

import static io.reactivex.Completable.complete;
import static io.reactivex.Maybe.empty;
import static io.reactivex.Observable.fromIterable;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class ElasticSearchDocumentView implements DocumentView {

    private final Client client;

    private String clusterName = "default";

    private String host = "localhost";

    private int port = 9300;

    public ElasticSearchDocumentView() {
        try {
            Settings settings = Settings.builder().put("cluster.name", clusterName).build();
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public Completable save(String collection, String key, Map<String, Object> document) {
        return new Completable() {
            @Override protected void subscribeActual(CompletableObserver observer) {
                client.prepareIndex(collection, "default", key).setSource(document).execute(new ActionListener<IndexResponse>() {

                    @Override public void onResponse(IndexResponse indexResponse) {
                        observer.onComplete();
                    }

                    @Override public void onFailure(Exception e) {
                        observer.onError(e);
                    }
                });
            }
        };
    }

    @Override public Maybe<Map<String, Object>> findById(String collection, String key) {
        try {
            GetResponse response = client.prepareGet(collection, "default", key).get();
            Map<String, Object> result = response.getSource();
            return result != null ? Maybe.just(result) : empty();
        } catch (IndexNotFoundException e) {
            return empty();
        }
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
        client.prepareDelete(collection, "default", key).get();
        return complete();
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

}