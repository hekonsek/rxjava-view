/**
 * Licensed to the RxJava View under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hekonsek.rxjava.view.document.elasticsearch;

import com.github.hekonsek.rxjava.event.Event;
import com.google.common.collect.ImmutableMap;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.github.hekonsek.rxjava.event.Events.event;
import static com.github.hekonsek.rxjava.event.Headers.ADDRESS;
import static com.github.hekonsek.rxjava.event.Headers.KEY;
import static com.github.hekonsek.rxjava.view.document.elasticsearch.ElasticSearchDocumentViewSaveTransformation.save;
import static io.reactivex.Observable.just;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.rules.Timeout.seconds;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.CLUSTER_NAME;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.TRANSPORT_TCP_PORT;

@RunWith(VertxUnitRunner.class)
public class ElasticSearchDocumentViewTransformationsTest {

    @Rule
    public Timeout timeout = seconds(5);

    ElasticSearchDocumentView view = new ElasticSearchDocumentView().start();

    String collection = UUID.randomUUID().toString();

    String key = UUID.randomUUID().toString();

    Map<String, Object> document = ImmutableMap.of("foo", "bar", "timestamp", new Date());

    Event<Map<String, Object>> event = event(ImmutableMap.of(ADDRESS, collection, KEY, key), document);

    static EmbeddedElastic embeddedElastic;

    @BeforeClass
    public static void beforeClass() throws IOException, InterruptedException {
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("6.0.0")
                .withSetting(TRANSPORT_TCP_PORT, 9300)
                .withSetting(CLUSTER_NAME, "default")
                .withEsJavaOpts("-Xms128m -Xmx512m")
                .withStartTimeout(1, MINUTES)
                .build().start();
    }

    @AfterClass
    public static void afterClass() {
        embeddedElastic.stop();
    }

    @Test
    public void shouldSave(TestContext context) {
        Async async = context.async();
        just(event).compose(save(view)).doOnComplete(() ->
                view.findById(collection, key).subscribe(document -> {
                    assertThat(document.get("foo")).isEqualTo("bar");
                    assertThat(document.get("timestamp")).isNotNull();
                    async.complete();
                })
        ).subscribe();
    }

    @Test
    public void shouldRemove(TestContext context) {
        Async async = context.async();
        just(event).compose(save(view)).doOnComplete(() ->
                just(event.withPayload(null)).compose(save(view)).doOnComplete(() ->
                        view.findById(collection, key).doOnComplete(async::complete).subscribe()
                ).subscribe()
        ).subscribe();
    }

}
