package io.swagger.assert4j.cucumber;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.swagger.assert4j.client.model.RestParameter;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.guice.ScenarioScoped;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Additional StepDefs for simplifying testing of Swagger-defined REST APIs
 */

@ScenarioScoped
public class OASStepDefs {

    private final static Logger LOG = LoggerFactory.getLogger(OASStepDefs.class);

    private static final ArrayList<String> PARAM_TYPES = Lists.newArrayList("query", "path", "header");

    private final OASCache oasCache;
    private final RestStepDefs restStepDefs;
    private OpenAPI oas;
    private Operation oasOperation;

    @Inject
    public OASStepDefs(OASCache oasCache, RestStepDefs restStepDefs) {
        this.oasCache = oasCache;
        this.restStepDefs = restStepDefs;
    }

    @Given("^the OAS definition at (.*)$")
    public void theOASDefinitionAt(String oasUrl) {
        theSwaggerDefinitionAt( oasUrl );
    }

    @Given("^the Swagger definition at (.*)$")
    public void theSwaggerDefinitionAt(String oasUrl) {
        oas = oasCache.getOAS(oasUrl);
    }

    @When("^a request to ([^ ]*) is made$")
    public void aRequestToOperationIsMade(String operationId) {
        if (oas == null) {
            throw new CucumberExecutionException("Missing Swagger definition");
        }

        if (!findOASOperation(operationId)) {
            throw new CucumberExecutionException("Could not find operation [" + operationId + "] in Swagger definition");
        }
    }

    private boolean findOASOperation(String operationId) {
        oasOperation = null;

        for (String resourcePath : oas.getPaths().keySet()) {
            PathItem path = oas.getPaths().get(resourcePath);
            for (PathItem.HttpMethod httpMethod : path.readOperationsMap().keySet()) {
                Operation operation = path.readOperationsMap().get(httpMethod);
                if (operationId.equalsIgnoreCase(operation.getOperationId())) {
                    restStepDefs.setMethod(httpMethod.name().toUpperCase());
                    restStepDefs.setPath(resourcePath);
                    oasOperation = operation;
                }
            }
        }

        return oasOperation != null;
    }

    @Then("^the response is (.*)$")
    public void theResponseIs(String responseDescription) {
        if (oasOperation == null) {
            throw new CucumberExecutionException("missing OAS operation for request");
        }

        for (String responseCode : oasOperation.getResponses().keySet()) {
            ApiResponse response = oasOperation.getResponses().get(responseCode);
            if (responseDescription.equalsIgnoreCase(response.getDescription())) {
                restStepDefs.aResponseIsReturned(Integer.parseInt(responseCode));
            }
        }
    }

    @Given("^([^ ]*) is (.*)$")
    public void parameterIs(String name, String value) {

        if (oasOperation != null) {
            for (Parameter parameter : oasOperation.getParameters()) {
                if (parameter.getName().equalsIgnoreCase(name)) {
                    String type = parameter.getIn();
                    if (PARAM_TYPES.contains(type)) {
                        restStepDefs.addParameter(
                            new RestParameter().type(RestParameter.TypeEnum.valueOf(type.toUpperCase())).name(name).value( value));
                    } else if (type.equals("body")) {
                        restStepDefs.setRequestBody(value);
                    }

                    return;
                }
            }
        }

        restStepDefs.addBodyValue(name, value);
    }

    @Given("^([^ ]*) is$")
    public void parameterIsBlob(String name, String value) throws Throwable {
        parameterIs(name, value);
    }
}
