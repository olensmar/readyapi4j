package com.smartbear.readyapi4j;

import com.smartbear.readyapi4j.client.model.*;
import com.smartbear.readyapi4j.teststeps.TestStepTypes;
import com.smartbear.readyapi4j.teststeps.propertytransfer.PathLanguage;
import org.junit.Test;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.smartbear.readyapi4j.TestRecipeBuilder.newTestRecipe;
import static com.smartbear.readyapi4j.properties.Properties.property;
import static com.smartbear.readyapi4j.teststeps.TestSteps.*;
import static com.smartbear.readyapi4j.teststeps.propertytransfer.PropertyTransferBuilder.*;
import static com.smartbear.readyapi4j.teststeps.propertytransfer.PropertyTransferSourceBuilder.aSource;
import static com.smartbear.readyapi4j.teststeps.propertytransfer.PropertyTransferTargetBuilder.aTarget;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class TestRecipeBuilderTest {

    private static final String URI = "http://maps.googleapis.com/maps/api/geocode/xml";

    @Test
    public void dumpsRecipe() throws Exception {
        String recipe = newTestRecipe(GET(URI)
                .addQueryParameter("address", "1600+Amphitheatre+Parkway,+Mountain+View,+CA")
                .addQueryParameter("sensor", "false")
                .assertJsonContent("$.results[0].address_components[1].long_name", "Amphitheatre Parkway")
        ).buildTestRecipe().toString();

        assertThat(recipe.length(), not(0));
    }

    @Test
    public void buildRecipeWithTestCaseProperty() {
        TestRecipe recipe = newTestRecipe().withProperty("test", "test").buildTestRecipe();
        assertThat(recipe.getTestCase().getProperties().size(), is(1));
        assertThat(recipe.getTestCase().getProperties().get("test"), is("test"));
    }

    @Test
    public void buildsRecipeWithName() throws Exception {
        String recipeName = "AuthenticationRecipe";
        TestRecipe testRecipe = newTestRecipe().named(recipeName).buildTestRecipe();
        assertThat(testRecipe.getName(), is(recipeName));
    }

    @Test
    public void buildRecipeWithTestCaseProperties() {
        TestRecipe recipe = newTestRecipe().withProperties(property("test", "test")).buildTestRecipe();
        assertThat(recipe.getTestCase().getProperties().size(), is(1));
        assertThat(recipe.getTestCase().getProperties().get("test"), is("test"));
    }

    @Test
    public void buildsRecipeWithPropertyTransferTestStep() throws Exception {
        TestRecipe recipe = newTestRecipe(propertyTransfer(from(aSource()
                        .withSourceStep("sourceName")
                        .withProperty("username")
                        .withPath("sourcePath")
                        .withPathLanguage(PathLanguage.XPath)
                )
                        .to(aTarget()
                                .withTargetStep("targetName")
                                .withProperty("username")
                                .withPath("targetPath")
                                .withPathLanguage(PathLanguage.XPath)
                        )
                )
        )
                .buildTestRecipe();

        PropertyTransferTestStep testStep = (PropertyTransferTestStep) recipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getType(), is(TestStepTypes.PROPERTY_TRANSFER.getName()));

        assertThat(testStep.getTransfers().size(), is(1));

        PropertyTransfer propertyTransfer = testStep.getTransfers().get(0);
        assertSource(propertyTransfer.getSource());
        assertTarget(propertyTransfer.getTarget());
    }

    @Test
    public void buildsJsonPathTransferWithImplicitSourceStep() throws Exception {
        final String jsonPath = "$.customer.address";
        TestRecipe recipe = newTestRecipe(
                GET("/get/something").named("theGet"),
                propertyTransfer(fromPreviousResponse(jsonPath)
                        .to(aTarget()
                                .withTargetStep("targetName")
                                .withProperty("username")
                                .withPath("targetPath")
                                .withPathLanguage(PathLanguage.XPath)
                        )
                )
        )
                .buildTestRecipe();

        verifyImplicitTransfer(recipe, jsonPath, "JSONPath");
    }

    private void verifyImplicitTransfer(TestRecipe recipe, String expectedPath, String pathLanguage) {
        List<TestStep> testSteps = recipe.getTestCase().getTestSteps();
        PropertyTransferTestStep testStep = (PropertyTransferTestStep) testSteps.get(1);
        assertThat(testStep.getType(), is(TestStepTypes.PROPERTY_TRANSFER.getName()));

        String firstStepName = testSteps.get(0).getName();
        assertThat(testStep.getTransfers().size(), is(1));

        PropertyTransfer propertyTransfer = testStep.getTransfers().get(0);
        PropertyTransferSource source = propertyTransfer.getSource();
        assertThat(source.getSourceName(), is(firstStepName));
        assertThat(source.getPath(), is(expectedPath));
        assertThat(source.getPathLanguage(), is(pathLanguage));
        assertThat(source.getProperty(), is("Response"));
    }

    @Test
    public void buildsXPathTransferWithImplicitSourceStep() throws Exception {
        final String xPath = "/customer/address";
        TestRecipe recipe = newTestRecipe(
                GET("/get/something").named("theGet"),
                propertyTransfer(fromPreviousResponse(xPath)
                        .to(aTarget()
                                .withTargetStep("targetName")
                                .withProperty("username")
                                .withPath("targetPath")
                                .withPathLanguage(PathLanguage.XPath)
                        )
                )
        )
                .buildTestRecipe();

        verifyImplicitTransfer(recipe, xPath, "XPath");
    }

    @Test
    public void buildsTransferWithSpecifiedSourceStep() throws Exception {
        final String testStepName = "theGet";
        final String xPath = "/customer/address";
        TestRecipe recipe = newTestRecipe(
                GET("/get/something").named(testStepName),
                propertyTransfer(fromResponse(testStepName, xPath)
                        .to(aTarget()
                                .withTargetStep("targetName")
                                .withProperty("username")
                                .withPath("targetPath")
                                .withPathLanguage(PathLanguage.XPath)
                        )
                )
        )
                .buildTestRecipe();

        verifyImplicitTransfer(recipe, xPath, "XPath");
    }

    @Test
    public void buildsXPathTransferWithImplicitTargetStep() throws Exception {
        final String xPath = "/customer/address";
        TestRecipe recipe = newTestRecipe(
                GET("/get/something"),
                propertyTransfer(fromPreviousResponse("/some/path").toNextRequest(xPath)),
                POST("/some/destination").named("thePost"))
                .buildTestRecipe();

        List<TestStep> testSteps = recipe.getTestCase().getTestSteps();
        verifyImplicitTargetStep(testSteps, xPath, "XPath");
    }

    private void verifyImplicitTargetStep(List<TestStep> testSteps, String xPath, String expectedPathLanguage) {
        PropertyTransferTestStep testStep = (PropertyTransferTestStep) testSteps.get(1);
        assertThat(testStep.getType(), is(TestStepTypes.PROPERTY_TRANSFER.getName()));

        String targetStepName = testSteps.get(2).getName();
        assertThat(testStep.getTransfers().size(), is(1));

        PropertyTransfer propertyTransfer = testStep.getTransfers().get(0);
        PropertyTransferTarget target = propertyTransfer.getTarget();
        assertThat(target.getTargetName(), is(targetStepName));
        assertThat(target.getPath(), is(xPath));
        assertThat(target.getPathLanguage(), is(expectedPathLanguage));
        assertThat(target.getProperty(), is("Request"));
    }

    @Test
    public void buildsJsonPathTransferWithImplicitTargetStep() throws Exception {
        final String jsonPath = "$.customer.address";
        TestRecipe recipe = newTestRecipe(
                GET("/get/something"),
                propertyTransfer(fromPreviousResponse("/some/path").toNextRequest(jsonPath)),
                POST("/some/destination").named("thePost")
        ).buildTestRecipe();

        List<TestStep> testSteps = recipe.getTestCase().getTestSteps();
        verifyImplicitTargetStep(testSteps, jsonPath, "JSONPath");
    }

    @Test
    public void buildsJsonPathTransferWithSpecifiedTargetStep() throws Exception {
        final String jsonPath = "$.customer.address";
        TestRecipe recipe = newTestRecipe(
                GET("/get/something"),
                propertyTransfer(fromPreviousResponse("/some/path").toRequestStep("thePost", jsonPath)),
                POST("/some/destination").named("thePost")
        ).buildTestRecipe();

        List<TestStep> testSteps = recipe.getTestCase().getTestSteps();
        verifyImplicitTargetStep(testSteps, jsonPath, "JSONPath");
    }

    @Test
    public void buildsRecipeWithGroovyScriptTestStep() throws Exception {
        String script = "def a = 'a'";
        String name = "The name";
        TestRecipe recipe = newTestRecipe(
                groovyScriptStep(script).named(name)
        ).buildTestRecipe();

        GroovyScriptTestStep testStep = (GroovyScriptTestStep) recipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getType(), is(TestStepTypes.GROOVY_SCRIPT.getName()));
        assertThat(testStep.getScript(), is(script));
        assertThat(testStep.getName(), is(name));
    }

    @Test
    public void buildsRecipeWithDelayTestStep() throws Exception {
        String testStepName = "DelayStep";
        TestRecipe recipe = newTestRecipe()
                .addStep(delayStep(3000).named(testStepName))
                .buildTestRecipe();

        DelayTestStep testStep = (DelayTestStep) recipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getType(), is(TestStepTypes.DELAY.getName()));
        assertThat(testStep.getDelay(), is(3000));
        assertThat(testStep.getName(), is(testStepName));
    }

    @Test
    public void buildsRecipeWithPropertiesTestStep() throws Exception {
        TestRecipe testRecipe = newTestRecipe(
                properties()
                        .named("PropertiesStep")
                        .addProperty("property1", "value1")
        ).buildTestRecipe();
        PropertiesTestStep testStep = (PropertiesTestStep) testRecipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getType(), is(TestStepTypes.PROPERTIES.getName()));
        assertThat(testStep.getProperties().get("property1"), is("value1"));
    }

    @Test
    public void buildsRecipeWithFluentPropertiesTestStep() throws Exception {
        TestRecipe testRecipe = newTestRecipe(
                properties(
                        property("property1", "value1")
                )
                        .named("PropertiesStep")
        ).buildTestRecipe();
        PropertiesTestStep testStep = (PropertiesTestStep) testRecipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getType(), is(TestStepTypes.PROPERTIES.getName()));
        assertThat(testStep.getProperties().get("property1"), is("value1"));
    }

    @Test
    public void buildsRecipeWithPropertiesTestStepWithProvidedProperties() throws Exception {
        Map<String, String> properties1 = new HashMap<>();
        properties1.put("property1", "value1");

        Map<String, String> properties2 = new HashMap<>();
        properties2.put("property2", "value2");

        TestRecipe testRecipe = newTestRecipe(properties(properties1)
                .named("PropertiesStep")
                .addProperties(properties2)
                .addProperty("property3", "value3")
        ).buildTestRecipe();
        PropertiesTestStep testStep = (PropertiesTestStep) testRecipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getType(), is(TestStepTypes.PROPERTIES.getName()));
        assertThat(testStep.getProperties().get("property1"), is("value1"));
        assertThat(testStep.getProperties().get("property2"), is("value2"));
        assertThat(testStep.getProperties().get("property3"), is("value3"));
    }

    @Test
    public void buildsRecipeWithSoapMockResponseTestStep() throws Exception {
        TestRecipe testRecipe = newTestRecipe(soapMockResponse(new URL("http://www.webservicex.com/globalweather.asmx?WSDL"))
                .named("SoapMockResponse")
                .forBinding("GlobalWeatherSoap12")
                .forOperation("GetWeather")
                .withPath("/weather")
                .withPort(6091)
        ).buildTestRecipe();
        SoapMockResponseTestStep testStep = (SoapMockResponseTestStep) testRecipe.getTestCase().getTestSteps().get(0);
        assertThat(testStep.getWsdl(), is("http://www.webservicex.com/globalweather.asmx?WSDL"));
        assertThat(testStep.getBinding(), is("GlobalWeatherSoap12"));
        assertThat(testStep.getOperation(), is("GetWeather"));
        assertThat(testStep.getPath(), is("/weather"));
        assertThat(testStep.getPort(), is(6091));
    }

    private void assertSource(PropertyTransferSource source) {
        assertThat(source.getSourceName(), is("sourceName"));
        assertThat(source.getProperty(), is("username"));
        assertThat(source.getPath(), is("sourcePath"));
        assertThat(source.getPathLanguage(), is(PathLanguage.XPath.name()));
    }

    private void assertTarget(PropertyTransferTarget target) {
        assertThat(target.getTargetName(), is("targetName"));
        assertThat(target.getProperty(), is("username"));
        assertThat(target.getPath(), is("targetPath"));
        assertThat(target.getPathLanguage(), is(PathLanguage.XPath.name()));
    }
}
