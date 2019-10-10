package com.smartbear.readyapi4j.assertions;

import com.smartbear.readyapi4j.client.model.Assertion;

public interface AssertionBuilder<T extends Assertion> {

    T build();
}
