package com.github.hekonsek.rxjava.view.document.memory;

import com.github.hekonsek.rxjava.view.document.DocumentView;
import com.github.hekonsek.rxjava.view.document.DocumentWithKey;
import com.google.common.collect.ImmutableMap;
import io.reactivex.internal.operators.maybe.MaybeObserveOn;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(VertxUnitRunner.class)
public class InMemoryDocumentViewTest {

    DocumentView view = new InMemoryDocumentView();

    String collection = UUID.randomUUID().toString();

    String key = UUID.randomUUID().toString();

    Map<String, Object> document = ImmutableMap.of("foo", "bar");

    // Tests

    @Test
    public void shouldSave(TestContext context) {
        Async async = context.async();
        view.save(collection, key, document).subscribe(() ->
                view.findById(collection, key).subscribe(document -> {
                    assertThat(document).isEqualTo(this.document);
                    async.complete();
                })
        );
    }

    @Test
    public void shouldCallSubscriberOnSave(TestContext context) {
        Async async = context.async();
        view.save(collection, key, document).subscribe(async::complete);
    }

    @Test(timeout = 5000)
    public void shouldFindEmptyById(TestContext context) {
        Async async = context.async();
        view.findById(collection, key).doOnComplete(async::complete).subscribe();
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
    public void shouldFindAll() {
        view.save(collection, key, document).subscribe(() -> {
            List<DocumentWithKey> documents = new LinkedList<>();
            view.findById(collection, key).subscribe(document ->
                    view.findAll(collection).subscribe(documents::add)
            );
            assertThat(documents).hasSize(1);
            assertThat(documents.get(0).key()).isEqualTo(key);
            assertThat(documents.get(0).document()).isEqualTo(document);
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
