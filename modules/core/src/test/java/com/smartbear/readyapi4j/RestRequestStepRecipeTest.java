package com.smartbear.readyapi4j;

import com.smartbear.readyapi4j.client.model.Authentication;
import com.smartbear.readyapi4j.client.model.RequestAttachment;
import com.smartbear.readyapi4j.client.model.RestParameter;
import com.smartbear.readyapi4j.client.model.RestTestRequestStep;
import com.smartbear.readyapi4j.extractor.ExtractorData;
import com.smartbear.readyapi4j.teststeps.TestStepTypes;
import com.smartbear.readyapi4j.teststeps.TestSteps;
import com.sun.jersey.core.util.Base64;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.smartbear.readyapi4j.TestRecipeBuilder.newTestRecipe;
import static com.smartbear.readyapi4j.attachments.Attachments.*;
import static com.smartbear.readyapi4j.auth.Authentications.*;
import static com.smartbear.readyapi4j.client.model.RestParameter.TypeEnum.*;
import static com.smartbear.readyapi4j.extractor.Extractors.fromProperty;
import static com.smartbear.readyapi4j.extractor.Extractors.fromResponse;
import static com.smartbear.readyapi4j.teststeps.TestSteps.*;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RestRequestStepRecipeTest {
    private static final String URI = "http://maps.googleapis.com/maps/api/geocode/xml";
    private static final String REQUEST_BODY = "{ \"values\": [ \"Value 1\", \"Value2 2\"] }";
    public static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";

    @Test
    public void buildsRestRequestTestStepRecipe() throws Exception {

        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .named("Rest Request")
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getName(), is("Rest Request"));
        assertThat(testStep.getType(), is(TestStepTypes.REST_REQUEST.getName()));
        assertThat(testStep.getURI(), is(URI));
        assertThat(testStep.getMethod(), is(TestSteps.HttpMethod.GET.name()));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithRequestBody() throws Exception {
        TestRecipe recipe = newTestRecipe(
                POST(URI)
                        .named("Rest Request")
                        .withRequestBody(REQUEST_BODY)
                        .withMediaType(MEDIA_TYPE_APPLICATION_JSON)
                        .withEncoding("UTF-8")
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getMethod(), is(TestSteps.HttpMethod.POST.name()));
        assertThat(testStep.getRequestBody(), is(REQUEST_BODY));
        assertThat(testStep.getMediaType(), is(MEDIA_TYPE_APPLICATION_JSON));
        assertThat(testStep.getEncoding(), is(StandardCharsets.UTF_8.displayName()));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithRequestHeader() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .addHeader("header", "value")
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        Map<String, Object> headers = testStep.getHeaders();
        assertThat(headers.size(), is(1));
        assertThat((List<String>) headers.get("header"), is(singletonList("value")));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithRequestHeaderWithMultipleValues() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .addHeader("header1", "value1")
                        .addHeader("header1", "value2")
                        .addHeader("header1", "value3")
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        Map<String, Object> headers = testStep.getHeaders();
        assertThat(headers.size(), is(1));
        assertThat((List<String>) headers.get("header1"), is(Arrays.asList("value1", "value2", "value3")));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithRequestHeaderWithMultipleValuesInOneGo() throws Exception {
        final List<String> headerValues = Arrays.asList("value1", "value2", "value3");
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .addHeader("header1", headerValues)
        )
                .buildTestRecipe();
        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        Map<String, Object> headers = testStep.getHeaders();
        assertThat(headers.size(), is(1));
        assertThat((List<String>) headers.get("header1"), is(headerValues));
    }

    @Test
    public void buildsRestRequestTestStepWithMethodPut() throws Exception {
        TestRecipe recipe = newTestRecipe(
                PUT(URI))
                .buildTestRecipe();

        RestTestRequestStep testStep = ((RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0));
        assertThat(testStep.getMethod(), is(TestSteps.HttpMethod.PUT.name()));
        assertThat(testStep.getURI(), is(URI));
    }

    @Test
    public void buildsRestRequestTestStepWithMethodPost() throws Exception {
        TestRecipe recipe = newTestRecipe(
                POST(URI))
                .buildTestRecipe();

        RestTestRequestStep testStep = ((RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0));
        assertThat(testStep.getMethod(), is(TestSteps.HttpMethod.POST.name()));
        assertThat(testStep.getURI(), is(URI));
    }

    @Test
    public void buildsRestRequestTestStepWithMethodDelete() throws Exception {
        TestRecipe recipe = newTestRecipe(
                DELETE(URI))
                .buildTestRecipe();

        RestTestRequestStep testStep = ((RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0));
        assertThat(testStep.getMethod(), is(TestSteps.HttpMethod.DELETE.name()));
        assertThat(testStep.getURI(), is(URI));
    }

    @Test
    public void buildsRestRequestTestStepWithQueryParameter() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .addQueryParameter("param1", "value1")
        )
                .buildTestRecipe();

        RestParameter parameter = ((RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0)).getParameters().get(0);
        assertThat(parameter.getName(), is("param1"));
        assertThat(parameter.getValue(), is("value1"));
        assertThat(parameter.getType(), is(QUERY));
    }

    @Test
    public void buildsRestRequestTestStepWithHeaderParameter() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .addHeaderParameter("param1", "value1")
        )
                .buildTestRecipe();

        RestParameter parameter = ((RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0)).getParameters().get(0);
        assertThat(parameter.getName(), is("param1"));
        assertThat(parameter.getValue(), is("value1"));
        assertThat(parameter.getType(), is(HEADER));
    }

    @Test
    public void buildsRestRequestTestStepWithPathParameter() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .addPathParameter("param1", "value1")
        )
                .buildTestRecipe();

        RestParameter parameter = ((RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0)).getParameters().get(0);
        assertThat(parameter.getName(), is("param1"));
        assertThat(parameter.getValue(), is("value1"));
        assertThat(parameter.getType(), is(PATH));
    }

    @Test
    public void buildsRestRequestTestStepWithMatrixParameter() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .addMatrixParameter("param1", "value1")
        )
                .buildTestRecipe();

        RestParameter parameter = ((RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0)).getParameters().get(0);
        assertThat(parameter.getName(), is("param1"));
        assertThat(parameter.getValue(), is("value1"));
        assertThat(parameter.getType(), is(MATRIX));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithRequestTimeout() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .setTimeout(2000)
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getTimeout(), is(String.valueOf(2000)));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithRequestTimeoutWithPropertyExpansion() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .setTimeout("${#Project#Timeout")
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getTimeout(), is("${#Project#Timeout"));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithRequestOptions() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .postQueryString()
                        .followRedirects()
                        .entitizeParameters()
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.isFollowRedirects(), is(true));
        assertThat(testStep.isEntitizeParameters(), is(true));
        assertThat(testStep.isPostQueryString(), is(true));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithParameters() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .addQueryParameter("address", "1600+Amphitheatre+Parkway,+Mountain+View,+CA")
                        .addQueryParameter("sensor", "false")
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        List<RestParameter> parameters = testStep.getParameters();
        assertThat(parameters.size(), is(2));

        assertQueryParameter(parameters.get(0), "address", "1600+Amphitheatre+Parkway,+Mountain+View,+CA");
        assertQueryParameter(parameters.get(1), "sensor", "false");
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithBasicAuthentication() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .withAuthentication(basic("username", "password")
                        )
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        Authentication authentication = testStep.getAuthentication();

        assertThat(authentication.getUsername(), is("username"));
        assertThat(authentication.getPassword(), is("password"));
        assertThat(authentication.getType(), is("Basic"));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithNTLMAuthentication() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .withAuthentication(ntlm("username", "password")
                                .setDomain("domain")
                        )
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        Authentication authentication = testStep.getAuthentication();

        assertThat(authentication.getUsername(), is("username"));
        assertThat(authentication.getPassword(), is("password"));
        assertThat(authentication.getDomain(), is("domain"));
        assertThat(authentication.getType(), is("NTLM"));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithKerberosAuthentication() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .withAuthentication(kerberos("username", "password")
                                .setDomain("domain1")
                        )
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        Authentication authentication = testStep.getAuthentication();

        assertThat(authentication.getUsername(), is("username"));
        assertThat(authentication.getPassword(), is("password"));
        assertThat(authentication.getDomain(), is("domain1"));
        assertThat(authentication.getType(), is("SPNEGO/Kerberos"));
    }

    @Test
    public void buildsRestRequestTestStepRecipeWithClientCertificate() throws Exception {
        TestRecipe recipe = newTestRecipe(
                GET(URI)
                        .withClientCertificate("clientCertificate.jks")
                        .withClientCertificatePassword("password")
        )
                .buildTestRecipe();

        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getClientCertificateFileName(), is("clientCertificate.jks"));
        assertThat(testStep.getClientCertificatePassword(), is("password"));
    }

    private void assertQueryParameter(RestParameter parameter, String paramName, String value) {
        assertThat(parameter.getName(), is(paramName));
        assertThat(parameter.getValue(), is(value));
        assertThat(parameter.getType(), is(QUERY));
    }

    @Test
    public void buildRestRequestTestStepRecipeWithStreamAttachment() {
        InputStream inputStream = new ByteArrayInputStream("Content".getBytes());
        TestRecipe recipe = newTestRecipe(
                POST(URI)
                        .withAttachments(
                                stream(inputStream, "ContentType")))
                .buildTestRecipe();
        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        List<RequestAttachment> attachments = testStep.getAttachments();
        assertThat(attachments.size(), is(1));
        assertRequestAttachment(attachments.get(0), null, "ContentType", null, "Content".getBytes());
    }

    @Test
    public void buildRestRequestTestStepRecipeWithByteAttachment() {
        TestRecipe recipe = newTestRecipe(
                POST(URI)
                        .withAttachments(
                                byteArray("Content".getBytes(), "ContentType")))
                .buildTestRecipe();
        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        List<RequestAttachment> attachments = testStep.getAttachments();
        assertThat(attachments.size(), is(1));
        assertRequestAttachment(attachments.get(0), null, "ContentType", null, "Content".getBytes());
    }

    @Test
    public void buildRestRequestTestStepRecipeWithStringAttachment() {
        TestRecipe recipe = newTestRecipe(
                POST(URI)
                        .withAttachments(
                                string("Content", "ContentType")))
                .buildTestRecipe();
        RestTestRequestStep testStep = (RestTestRequestStep) recipe.getTestCase().getTestSteps().get(0);
        List<RequestAttachment> attachments = testStep.getAttachments();
        assertThat(attachments.size(), is(1));
        assertRequestAttachment(attachments.get(0), null, "ContentType", null, "Content".getBytes());
    }

    private void assertRequestAttachment(RequestAttachment attachment, String contentId, String contentType, String name, byte[] content) {
        assertThat(attachment.getContentId(), is(contentId));
        assertThat(attachment.getContentType(), is(contentType));
        assertThat(attachment.getName(), is(name));
        assertThat(Base64.decode(attachment.getContent()), is(content));
    }

    @Test
    public void buildRestRequestTestRecipeWithPropertyExtractor() {
        final String[] extractedProperty = {""};
        TestRecipe recipe = newTestRecipe(
                POST(URI)
                        .named("RestRequest")
                        .withExtractors(
                                fromProperty("Endpoint", property -> extractedProperty[0] = property)))
                .buildTestRecipe();

        // This should not be set only after building the testrecipe, it should be set after run
        assertThat(extractedProperty[0], is(""));
        assertThat(recipe.getTestCase().getProperties().size(), is(2));
        recipe.getTestCase().getProperties().forEach((key, value) -> {
            if (key.contains("Endpoint")) {
                assertThat(value, is(""));
            } else {
                assertThat(key, is(ExtractorData.EXTRACTOR_DATA_KEY));
            }
        });
    }

    @Test
    public void buildRestRequestTestRecipeWithJsonPathExtractor() {
        final String[] extractedProperty = {""};
        TestRecipe recipe = newTestRecipe(
                POST(URI)
                        .named("RestRequest")
                        .withExtractors(
                                fromResponse("$[0].Endpoint", property -> extractedProperty[0] = property)))
                .buildTestRecipe();

        // This should not be set only after building the testrecipe, it should be set after run
        assertThat(extractedProperty[0], is(""));
        assertThat(recipe.getTestCase().getProperties().size(), is(2));
        recipe.getTestCase().getProperties().forEach((key, value) -> {
            if (key.contains("$[0].Endpoint")) {
                assertThat(value, is(""));
            } else {
                assertThat(key, is(ExtractorData.EXTRACTOR_DATA_KEY));
            }
        });
    }
}
