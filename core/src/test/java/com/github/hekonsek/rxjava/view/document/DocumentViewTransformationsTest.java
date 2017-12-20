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
package com.github.hekonsek.rxjava.view.document;

import com.github.hekonsek.rxjava.event.Event;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static com.github.hekonsek.rxjava.event.Events.event;
import static com.github.hekonsek.rxjava.event.Headers.ADDRESS;
import static com.github.hekonsek.rxjava.event.Headers.KEY;
import static com.github.hekonsek.rxjava.view.document.MaterializeDocumentViewTransformation.materialize;
import static io.reactivex.Observable.just;
import static org.mockito.Mockito.verify;

public class DocumentViewTransformationsTest {

    DocumentView view = Mockito.mock(DocumentView.class);

    String collection = UUID.randomUUID().toString();

    String key = UUID.randomUUID().toString();

    Map<String, Object> document = ImmutableMap.of("foo", "bar", "timestamp", new Date());

    Event<Map<String, Object>> event = event(ImmutableMap.of(ADDRESS, collection, KEY, key), document);

    @Test
    public void shouldSave() {
        just(event).compose(materialize(view)).subscribe();
        verify(view).save(collection, key, event.payload());
    }

    @Test
    public void shouldRemove() {
        just(event.withPayload(null)).compose(materialize(view)).subscribe();
        verify(view).remove(collection, key);
    }

}
