package io.swagger.assert4j.swagger;

import io.swagger.assert4j.teststeps.TestSteps;
import io.swagger.assert4j.teststeps.restrequest.RestRequestStepBuilder;
import io.swagger.assert4j.teststeps.restrequest.RestRequestStepWithBodyBuilder;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

/**
 * Utility class for building RestRequestStepBuilders for operations in a Swagger/OAS 2.0 definition
 */

public class OASTestStepBuilder {

    private static final Collection<TestSteps.HttpMethod> HTTP_METHODS_WITH_BODY =
            EnumSet.of(TestSteps.HttpMethod.POST, TestSteps.HttpMethod.PUT, TestSteps.HttpMethod.PATCH);
    private final OpenAPI openAPI;
    private String targetEndpoint;

    /**
     * Creates a SwaggerTestStepBuilder for the specified Swagger definition and target targetEndpoint
     *
     * @param swaggerUrl endpoint to the Swagger definition to use
     * @param targetEndpoint where the target API under test running
     * @throws IllegalArgumentException if the specified Swagger definition can not be parsed
     */
    public OASTestStepBuilder(String swaggerUrl, String targetEndpoint) throws IllegalArgumentException {
        this.openAPI = parseSwagger(swaggerUrl);
        setTargetEndpoint(targetEndpoint);
    }

    /**
     * Creates a SwaggerTestStepBuilder for the specified Swagger definition, uses the host and first scheme in the
     * definition for the target endpoint.
     *
     * @param swaggerUrl endpoint to the Swagger definition to use
     * @throws IllegalArgumentException if the specified Swagger definition can not be parsed
     */
    public OASTestStepBuilder(String swaggerUrl) throws IllegalArgumentException {
        this(swaggerUrl, null);
    }

    /**
     * Creates a SwaggerTestStepBuilder for the specified Swagger definition and target targetEndpoint
     *
     * @param swaggerInputStream stream from which the Swagger definition can be read
     * @param targetEndpoint where the target API under test running
     * @throws IllegalArgumentException if the specified Swagger definition can not be parsed
     * @throws IOException if the Swagger definition can not be read
     */
    public OASTestStepBuilder(InputStream swaggerInputStream, String targetEndpoint) throws IllegalArgumentException, IOException {
        this.openAPI = parseSwagger(swaggerInputStream);
        setTargetEndpoint(targetEndpoint);
    }

    /**
     * Creates a SwaggerTestStepBuilder for the specified Swagger definition, uses the host and first scheme in the
     * definition for the target endpoint.
     *
     * @param swaggerInputStream stream from which the swagger definition can be read
     * @throws IllegalArgumentException if the specified Swagger definition can not be parsed
     * @throws IOException if the Swagger definition can not be read
     */
    public OASTestStepBuilder(InputStream swaggerInputStream) throws IllegalArgumentException, IOException {
        this(swaggerInputStream, null);
    }

    private OpenAPI parseSwagger(String swaggerUrl) throws IllegalArgumentException {
        SwaggerParseResult result = new OpenAPIParser().readLocation(swaggerUrl, null, new ParseOptions());
        OpenAPI swagger = result.getOpenAPI();
        if( swagger == null ){
            throw new IllegalArgumentException( "Failed to parse Swagger definition at [" + swaggerUrl + "]; " +
                Arrays.toString(result.getMessages().toArray()));
        }
        return swagger;
    }

    private OpenAPI parseSwagger(InputStream swaggerInputStream) throws IllegalArgumentException, IOException {
        String swaggerAsString = IOUtils.toString(swaggerInputStream, StandardCharsets.UTF_8);
        SwaggerParseResult result = new OpenAPIParser().readContents(swaggerAsString, null, new ParseOptions());
        OpenAPI swagger = result.getOpenAPI();
        if (swagger == null) {
            throw new IllegalArgumentException("Failed to parse Swagger definition; " +
                    Arrays.toString(result.getMessages().toArray()));
        }
        return swagger;
    }

    /**
     * Creates a RestRequestStepBuilder for the specified operationId
     *
     * @param operationId the operationId of an operation in the Swagger definition
     * @return a RestRequestStepBuilder with the correct path and method
     * @throws IllegalArgumentException if the operationId is not found in the Swagger definition
     */
    public RestRequestStepBuilder<RestRequestStepBuilder> operation(String operationId) {
        for( String path : openAPI.getPaths().keySet()){
            PathItem swaggerPath = openAPI.getPaths().get( path );

            Map<PathItem.HttpMethod, Operation> httpMethodOperationMap = swaggerPath.readOperationsMap();
            for(PathItem.HttpMethod httpMethod : httpMethodOperationMap.keySet()) {

                Operation swaggerOperation = httpMethodOperationMap.get( httpMethod );
                if (operationId.equalsIgnoreCase(swaggerOperation.getOperationId())) {
                    return new RestRequestStepBuilder<>(getBasePath() + path, toHttpMethod(httpMethod));
                }
            }
        }

        throw new IllegalArgumentException("operationId [" + operationId + "] not found in Swagger definition" );
    }

    /**
     * Creates a RestRequestStepBuilder for the specified operation with body
     *
     * @param operationId the operationId of an operation in the Swagger definition
     * @return a RestRequestStepWithBodyBuilder with the correct path and method
     * @throws IllegalArgumentException if the operationId is not found in the Swagger definition
     */
    public RestRequestStepWithBodyBuilder operationWithBody(String operationId) {
        for (Map.Entry<String, PathItem> path : openAPI.getPaths().entrySet()) {

            for(Map.Entry<PathItem.HttpMethod, Operation> method: path.getValue().readOperationsMap().entrySet()) {
                Operation swaggerOperation = method.getValue();
                if (!operationId.equalsIgnoreCase(swaggerOperation.getOperationId())) {
                    continue;
                }
                final TestSteps.HttpMethod verb = toHttpMethod(method.getKey());
                ensureHttpMethodWithBody(verb);
                ensureBodyParameter(swaggerOperation);

                return new RestRequestStepWithBodyBuilder(getBasePath() + path.getKey(), verb);
            }
        }

        throw new IllegalArgumentException("operationId [" + operationId + "] not found in Swagger definition" );
    }

    private String getBasePath() {
        if( targetEndpoint != null ){
            return targetEndpoint;
        }

        if( openAPI.getServers() != null && !openAPI.getServers().isEmpty()){
            return openAPI.getServers().get(0).getUrl();
        }

        return null;
    }

    /**
     * Creates a RestRequestStepBuilder for an arbitrary path and method - the targetEndpoint and basePath will be prefixed
     * to the specified path.
     *
     * @param path the path to append to the targetEndpoint and basePath
     * @param method the HTTP method to use
     */
    public RestRequestStepBuilder<RestRequestStepBuilder> request(String path, TestSteps.HttpMethod method){
        return new RestRequestStepBuilder<>(getBasePath() + path, method);
    }

    /**
     * Creates a RestRequestStepWithBodyBuilder for an arbitrary path and method - the
     * targetEndpoint and basePath will be prefixed to the specified path.
     *
     * @param path   the path to append to the targetEndpoint and basePath
     * @param method the HTTP method to use
     */
    public RestRequestStepWithBodyBuilder requestWithBody(String path, TestSteps.HttpMethod method){
        ensureHttpMethodWithBody(method);
        return new RestRequestStepWithBodyBuilder(getBasePath() + path, method);
    }

    private void ensureBodyParameter(Operation operation) {
        if( operation.getRequestBody() == null ) {
            throw new IllegalArgumentException(
                    "Body parameter is not defined for the [" + operation.getOperationId() + "] operation");
        }
    }

    private void ensureHttpMethodWithBody(TestSteps.HttpMethod method) {
        if (!HTTP_METHODS_WITH_BODY.contains(method)) {
            throw new IllegalArgumentException(
                    "Body parameter is not allowed for [" + method + "] methods");
        }
    }

    private TestSteps.HttpMethod toHttpMethod(PathItem.HttpMethod swaggerMethod) {
        return TestSteps.HttpMethod.valueOf(swaggerMethod.name().toUpperCase());
    }

    /**
     * @return the parsed Swagger definition object
     */
    public OpenAPI getOpenAPI() {
        return openAPI;
    }

    /**
     * @return the target endpoint of the API under test
     */
    public String getTargetEndpoint() {
        return targetEndpoint;
    }

    /**
     * @param targetEndpoint the target endpoint of the API under test
     */
    public void setTargetEndpoint(String targetEndpoint) {
        this.targetEndpoint = targetEndpoint;
    }
}
