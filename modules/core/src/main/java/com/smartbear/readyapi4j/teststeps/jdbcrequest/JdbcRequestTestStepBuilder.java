package com.smartbear.readyapi4j.teststeps.jdbcrequest;

import com.smartbear.readyapi4j.assertions.AssertionBuilder;
import com.smartbear.readyapi4j.client.model.Assertion;
import com.smartbear.readyapi4j.client.model.JdbcRequestTestStep;
import com.smartbear.readyapi4j.teststeps.TestStepBuilder;
import com.smartbear.readyapi4j.teststeps.TestStepTypes;

import java.util.*;

import static com.smartbear.readyapi4j.assertions.Assertions.jdbcRequestStatusOk;
import static com.smartbear.readyapi4j.assertions.Assertions.jdbcRequestTimeout;

/**
 * Builder for JdbcRequestTestStep objects.
 */
public class JdbcRequestTestStepBuilder implements TestStepBuilder<JdbcRequestTestStep> {

    private final String driver;
    private final String connectionString;
    private final boolean storedProcedure;
    private String sqlQuery;
    private List<AssertionBuilder> assertionBuilders = new ArrayList<>();
    private Map<String, Object> properties = new HashMap<>();
    private String name;

    public JdbcRequestTestStepBuilder(String driver, String connectionString, boolean storedProcedure) {
        this.driver = driver;
        this.connectionString = connectionString;
        this.storedProcedure = storedProcedure;
    }

    public JdbcRequestTestStepBuilder withSql(String sql) {
        this.sqlQuery = sql;
        return this;
    }

    public JdbcRequestTestStepBuilder named(String name) {
        this.name = name;
        return this;
    }

    public JdbcRequestTestStepBuilder withProperties(Map<String, Object> newProperties) {
        Objects.requireNonNull(newProperties, "Properties can't be null");
        this.properties = newProperties;
        return this;
    }

    public JdbcRequestTestStepBuilder addProperty(String propertyName, Object value) {
        properties.put(propertyName, value);
        return this;
    }

    public JdbcRequestTestStepBuilder addAssertion(AssertionBuilder assertion) {
        assertionBuilders.add(assertion);
        return this;
    }

    @Override
    public JdbcRequestTestStep build() {
        JdbcRequestTestStep testStep = new JdbcRequestTestStep();
        testStep.setType(TestStepTypes.JDBC_REQUEST.getName());
        testStep.setDriver(driver);
        testStep.setConnectionString(connectionString);
        testStep.setStoredProcedure(storedProcedure);
        testStep.setSqlQuery(sqlQuery);
        testStep.setProperties(properties);
        testStep.setName(name);
        setAssertions(testStep);
        return testStep;
    }

    private void setAssertions(JdbcRequestTestStep testStep) {
        List<Assertion> assertions = new ArrayList<>();
        for (AssertionBuilder assertionBuilder : assertionBuilders) {
            assertions.add(assertionBuilder.build());
        }
        testStep.setAssertions(assertions);
    }

    /**
     * Assertion shortcuts
     */

    public JdbcRequestTestStepBuilder assertTimeout(long timeout) {
        return addAssertion(jdbcRequestTimeout(timeout));
    }

    public JdbcRequestTestStepBuilder assertStatus() {
        return addAssertion(jdbcRequestStatusOk());
    }
}
