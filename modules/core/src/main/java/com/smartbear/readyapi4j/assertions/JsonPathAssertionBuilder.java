package com.smartbear.readyapi4j.assertions;

import com.smartbear.readyapi4j.client.model.Assertion;

public interface JsonPathAssertionBuilder<T extends Assertion> extends AssertionBuilder<T> {
    JsonPathAssertionBuilder allowWildcards();

    JsonPathAssertionBuilder named(String assertionName);
}
