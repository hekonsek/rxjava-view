package com.github.hekonsek.rxjava.view.document;

import org.junit.Test;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

public class DocumentWithKeyTest {

    @Test
    public void shouldCreateDocument() {
        DocumentWithKey document = new DocumentWithKey("key", emptyMap());
        assertThat(document.key()).isEqualTo("key");
        assertThat(document.document()).isEmpty();
    }

}
