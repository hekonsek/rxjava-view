package com.github.hekonsek.rxjava.view.document.elasticsearch;

import com.github.hekonsek.rxjava.view.document.DocumentView;
import com.github.hekonsek.rxjava.view.document.DocumentWithKey;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import static io.reactivex.Completable.complete;
import static io.reactivex.Observable.fromIterable;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class ElasticSearchDocumentView implements DocumentView {

    private final Client client;

    private String clusterName = "default";

    public ElasticSearchDocumentView() {
        try {
            Settings settings = Settings.builder().put("cluster.name", clusterName).build();
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(InetAddress.getByName("localhost"), 9300));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override public Completable save(String collection, String key, Map<String, Object> document) {
        client.prepareIndex(collection, "default", key).setSource(document).get();
        return complete();
    }

    @Override public Single<Map<String, Object>> findById(String collection, String key) {
        GetResponse response = client.prepareGet(collection, "default", key).get();
        return Single.just(response.getSource());
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

}