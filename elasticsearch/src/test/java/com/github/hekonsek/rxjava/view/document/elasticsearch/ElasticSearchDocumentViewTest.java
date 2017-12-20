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

import com.github.hekonsek.rxjava.view.document.DocumentView;
import com.github.hekonsek.rxjava.view.document.DocumentWithKey;
import com.google.common.collect.ImmutableList;
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
import java.net.UnknownHostException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.rules.Timeout.seconds;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.CLUSTER_NAME;
import static pl.allegro.tech.embeddedelasticsearch.PopularProperties.TRANSPORT_TCP_PORT;

@RunWith(VertxUnitRunner.class)
public class ElasticSearchDocumentViewTest {

    @Rule
    public Timeout timeout = seconds(10);

    DocumentView view = new ElasticSearchDocumentView().start();

    String collection = UUID.randomUUID().toString();

    String key = UUID.randomUUID().toString();

    Map<String, Object> document = ImmutableMap.of("foo", "bar", "timestamp", new Date());

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
    public void shouldValidateInvalidHost() {
        Throwable t = catchThrowable(() -> new ElasticSearchDocumentView().host("someRandomHost").start());
        assertThat(t.getCause() instanceof UnknownHostException);
    }

    @Test
    public void shouldHandleInvalidPortOnSave(TestContext context) {
        Async async = context.async();
        new ElasticSearchDocumentView().port(9999).start().
                save(collection, key, emptyMap()).doOnError(e -> async.complete()).subscribe();
    }

    @Test
    public void shouldSave(TestContext context) {
        Async async = context.async();
        view.save(collection, key, document).subscribe(() ->
                view.findById(collection, key).subscribe(document -> {
                    assertThat(document.get("foo")).isEqualTo("bar");
                    assertThat(document.get("timestamp")).isNotNull();
                    async.complete();
                })
        );
    }

    @Test
    public void shouldFindEmptyById(TestContext context) {
        Async async = context.async();
        view.save(collection, key, document).subscribe(() ->
                view.findById(collection, "someRandomKey").doOnComplete(async::complete).subscribe()
        );
    }

    @Test
    public void shouldFindEmptyFromUnknownIndex(TestContext context) {
        Async async = context.async();
        view.findById(collection, key).doOnComplete(async::complete).subscribe();
    }

    @Test
    public void shouldCallSubscriberOnSave(TestContext context) {
        Async async = context.async();
        view.save(collection, key, document).subscribe(async::complete);
    }

    @Test
    public void shouldCount(TestContext context) {
        Async async = context.async();
        view.save(collection, key, document).subscribe(() ->
                view.findById(collection, key).subscribe(document ->
                        view.count(collection).subscribe(count -> {
                            assertThat(count).isEqualTo(1);
                            async.complete();
                        })
                )
        );
    }

    @Test
    public void shouldFindAll(TestContext context) {
        Async async = context.async();
        view.save(collection, key, document).subscribe(() -> {
            new LinkedList<>();
            view.findById(collection, key).subscribe(document -> {
                        List<DocumentWithKey> documents = ImmutableList.copyOf(view.findAll(collection).blockingIterable());
                        assertThat(documents).hasSize(1);
                        assertThat(documents.get(0).key()).isEqualTo(key);
                        assertThat(documents.get(0).document().get("foo")).isEqualTo("bar");
                        async.complete();
                    }
            );
        });
    }

    @Test
    public void shouldRemove(TestContext context) {
        Async async = context.async();
        view.save(collection, key, document).subscribe(() ->
                view.remove(collection, key).subscribe(() ->
                        view.count(collection).subscribe(count -> {
                            assertThat(count).isEqualTo(0);
                            async.complete();
                        })
                )
        );
    }

}
