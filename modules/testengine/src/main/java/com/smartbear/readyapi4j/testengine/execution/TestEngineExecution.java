package com.smartbear.readyapi4j.testengine.execution;

import com.google.common.collect.Lists;
import com.smartbear.readyapi4j.HttpBasicAuth;
import com.smartbear.readyapi4j.client.model.TestCaseResultReport;
import com.smartbear.readyapi4j.client.model.TestJobReport;
import com.smartbear.readyapi4j.client.model.TestStepResultReport;
import com.smartbear.readyapi4j.client.model.TestSuiteResultReport;
import com.smartbear.readyapi4j.execution.Execution;
import com.smartbear.readyapi4j.result.RecipeExecutionResult;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Class corresponding to an execution on a TestEngine instance. The execution can be either ongoing or completed.
 */

public class TestEngineExecution implements Execution {
    private final Deque<TestJobReport> executionStatusReports = new ConcurrentLinkedDeque<>();
    private final String id;
    private final TestEngineApi testEngineApi;
    private final HttpBasicAuth auth;

    /**
     * Package-scoped constructor since this class should only be created by executors or tests
     */

    TestEngineExecution(TestEngineApi testEngineApi, HttpBasicAuth auth, TestJobReport projectResultReport) {
        this.testEngineApi = testEngineApi;
        this.auth = auth;
        executionStatusReports.add(projectResultReport);
        this.id = projectResultReport.getTestjobId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TestJobReport.StatusEnum getCurrentStatus() {
        return executionStatusReports.getLast().getStatus();
    }

    @Override
    public TestJobReport getCurrentReport() {
        return executionStatusReports.getLast();
    }

    TestEngineApi getTestEngineApi() {
        return testEngineApi;
    }

    HttpBasicAuth getAuth() {
        return auth;
    }

    void addResultReport(TestJobReport newReport) {
        executionStatusReports.add(newReport);
    }

    @Override
    public RecipeExecutionResult getExecutionResult() {
        TestJobReport lastReport = executionStatusReports.getLast();
        return lastReport == null ? null : new TestEngineRecipeExecutionResult(this, getCurrentReport());
    }

    @Override
    public List<String> getErrorMessages() {
        List<String> result = Lists.newArrayList();

        if (executionStatusReports.getLast() != null) {
            for (TestSuiteResultReport testSuiteReport : executionStatusReports.getLast().getTestSuiteResultReports()) {
                for (TestCaseResultReport testCaseResultReport : testSuiteReport.getTestCaseResultReports()) {
                    for (TestStepResultReport testStepResultReport : testCaseResultReport.getTestStepResultReports()) {
                        if (testStepResultReport.getAssertionStatus() == TestStepResultReport.AssertionStatusEnum.FAIL) {
                            result.add(String.format("TestStepName: %s, messages: %s",
                                    testStepResultReport.getTestStepName(),
                                    String.join(", ", testStepResultReport.getMessages())));
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public void cancelExecution() {
        testEngineApi.cancelExecution(id, auth);
    }
}
