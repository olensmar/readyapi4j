package com.smartbear.readyapi4j.assertions;

import com.smartbear.readyapi4j.client.model.JdbcTimeoutAssertion;

public interface JdbcTimeoutAssertionBuilder extends AssertionBuilder<JdbcTimeoutAssertion> {
    JdbcTimeoutAssertionBuilder named(String assertionName);
}
