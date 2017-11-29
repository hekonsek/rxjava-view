package com.github.hekonsek.rxjava.view.document.memory;

import com.github.hekonsek.rxjava.view.document.DocumentView;
import io.reactivex.Observable;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Maps.newHashMap;

@RunWith(VertxUnitRunner.class)
public class MaterializedViewTest {

    DocumentView materializedView = new InMemoryDocumentView();

    @Test
    public void shouldGenerateMaterializedView(TestContext context) {
        Async async = context.async();
        // Create data set
        Observable.just(1, 2, 3).
                // Generate materialized view
                flatMap(it -> done -> materializedView.save("numbers", it + "", newHashMap("number", it)).subscribe(done::onComplete)).
                subscribe();
        // Verify that view has been generated
        materializedView.count("numbers").subscribe(count -> {
            assertThat(count).isEqualTo(3);
            async.complete();
        });
    }

}
