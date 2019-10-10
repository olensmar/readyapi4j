package com.smartbear.readyapi4j.assertions;

import com.smartbear.readyapi4j.client.model.SimpleContainsAssertion;

public interface ContainsAssertionBuilder extends AssertionBuilder<SimpleContainsAssertion> {
    ContainsAssertionBuilder useRegEx();

    ContainsAssertionBuilder ignoreCase();

    ContainsAssertionBuilder named(String assertionName);
}
