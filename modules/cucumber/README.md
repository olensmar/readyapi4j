## Cucumber/BDD testing for REST APIs

This module provides a generic Cucumber vocabulary for testing APIs with readyapi4j
with dedicated support for OAS/Swagger 2.0 to remove some of the technicalities required to define scenarios. 

A quick example for the Petstore API at http://petstore.swagger.io, testing of the 
/pet/findByTags resource could be defined withe following Scenario:

```gherkin
  Scenario: Find pet by tags
    Given the API running at http://petstore.swagger.io/v2
    When a GET request to /pet/findByTags is made
    And the tags parameter is test
    And the request expects json
    Then a 200 response is returned within 50ms
    And the response type is json
```

Using the integrated OAS/Swagger support this can be shortened to

```gherkin
  Scenario: Find pet by tags
    Given the OAS definition at http://petstore.swagger.io/v2/swagger.json
    # deducts path and method from Swagger definition by operationId
    When a request to findPetsByTags is made
    # deducts type of "tags" parameter (query/path/parameter/body) from Swagger definition
    And tags is test
    And the request expects json
    Then a 200 response is returned within 500ms
    And the response type is json
```

Not a huge difference - but as you can see by the comments the OAS/Swagger support removes some of the 
technicalities; read more about OAS/Swagger specific steps below!

Check out the [samples](modules/samples) submodule for more examples.

### Usage with maven/junit

If you want to run scenarios as part of a maven build you need to add the following 
dependency to your pom:

```xml
<dependency>
    <groupId>com.smartbear.readyapi</groupId>
    <artifactId>readyapi4j-cucumber-stepdefs</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

(This library has transient dependencies on cucumber-jvm 2.0.0, so no other dependencies are required)

Then create a JUnit runner class that uses Cucumber, add the readyapi4j glue/stepdefs, and point 
it to your feature files, for example:
 
```java
@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = {"pretty", "html:target/cucumber"},
    features = {"src/test/resources/cucumber"},
    glue = {"com.smartbear.readyapi4j.cucumber" })
public class CucumberTest {
}
```

### Running from the command-line

If you don't want to run your tests as part of a java/maven/etc-build or simply want to run them from the command-line you 
can use readyapi4j-cucumber-runner.jar (from the [runner module](modules/runner)) which includes all required libraries including the Cucumber
runtime. Run tests with:

```
java -jar readyapi4j-cucumber-runner-1.0.0-SNAPSHOT.jar <path to feature-files> -p pretty
```

Internally this will call the regular cucumber.api.cli.Main class with an added -g argument to the
included glue-code, all other options are passed as usual, see https://cucumber.io/docs/cucumber/api/#from-the-command-line.

(you will need java8 installed on your path)

### Running with Docker

If the above two options are too much of a Java hassle for you, you can use the prepackaged docker image 
available at https://hub.docker.com/r/smartbear/readyapi4j-cucumber instead - it packages the above runner and makes it
super-easy to run feature files for your APIs without having to install anything (except Docker of course).

For example:

```
docker run -v /Users/Ole/cucumber:/features smartbear/readyapi4j-cucumber -p pretty /features
```

Here we mounted a local folder containing feature files into a volume named "/features" in the container - and then 
specify that volume as the source for feature files for the Cucumber Runner, together with the -p pretty argument.

### Recipe logging

If you add the `-Dreadyapi4j.cucumber.logfolder=...` system property to your command line invocation the runner will write 
generated JSON recipes to the specified folder before executing them, for example allowing you 
to import them into ReadyAPI for load-testing, monitoring, etc.

### Configuring execution with ReadyAPI TestEngine
 
The included Cucumber StepDefs (see below) by default execute test recipes using the local open-source execution engine of readyapi4j. 
If you would like to execute your tests via [ReadyAPI TestEngine](https://smartbear.com/product/ready-api/testengine/overview/) instead 
you will need to download and install TestEngine and specify the following system properties when running your tests:

- testengine.endpoint=...url to your testengine installation...
- testengine.user=...the configured user to use...
- testengine.password=...the configured password for that user...

### Building 

Clone this project and and run
 
```
mvn clean install 
```

To build and install the artifacts in your local maven repository - the packaged jar is created in the root
target folder.

## API Testing Vocabulary Reference
 
The included [StepDefs](modules/stepdefs) for API testing adds the following vocabulary:

##### Given statements

- "the OAS definition at &lt;swagger endpoint&gt;"
    - The specified endpoint must reference a valid OAS/Swagger 2.0 definition
    - Example: "the OAS definition at http://petstore.swagger.io/v2/swagger.json"

- "the API running at &lt;API endpoint&gt;"
    - Example: "the API running at http://petstore.swagger.io/v2"

- "the oAuth2 token &lt;token&gt;"
    - Example: "the oAuth2 token 18273827aefef123"
    - Adds an OAuth 2.0 Bearer token to requests


##### When/And statements

- "a &lt;HTTP Method&gt; request to &lt;path&gt; is made"
    - Example: "a GET request to /test/search is made"
    
- "a request to &lt;Swagger OperationID&gt; is made"
    - will fail if no Swagger definition has been Given
    - Example: "a request to findPetById is made"

- "the request body is" &lt;text block&gt;
    - Example: "the request body is
    ```
    """
    { "id" : "123" }
    """
    ```"
    
- "the &lt;parameter name&gt; parameter is &lt;parameter value&gt;"
    - adds the specified parameter as a query parameter
    - Example: "the query parameter is miles davis"
    
- "the &lt;http header&gt; is &lt;header value&gt;
    - Example: "the Encoding header is UTF-8"
    
- "the type is &lt;content-type&gt;
    - single word types will be expanded to "application/&lt;content-type&gt;"
    - Example: "the type is json"

- "&lt;parameter name&gt; is &lt;parameter value&gt;"
    - if a valid OperationId has been given the type of parameter will be deduced from its list of parameters
    - if no OperationId has been given this will be added to a map of values that will be sent as the request body
    - Example: "name is John"
    - will work for both inline or multi-line values
    
- "&lt;the request expects &lt;content-type&gt;"
    - adds an Accept header
    - Example "the request expects yaml"

##### Then/And statements:

- "a &lt;HTTP Status code&gt; response is returned"
    - Example: "a 200 response is returned"
    
- "a &lt;HTTP Status code&gt; response is returned within &lt;number&gt;ms"
    - Example: "a 404 response is returned within 10ms"

- "the response is &lt;a valid Swagger Response description for the specified operationId&gt;"
    - Requires that a valid OperationId has been Given
    - Example: "the response is a list of people"

- "the response body contains" &lt;text block&gt;
    - Example: "the response body contains
    ```
    """
    "id" : "123"
    """
    ```"

- "the response body matches" &lt;regex text block&gt;
    - Example: "the response body matches
    ```
    """
    .*testing.*
    """
    ```"

- "the response type is &lt;content-type&gt;"
    - Example: "the response type is application/yaml"

- "the response contains a &lt;http-header name&gt; header"
    - Example: "the response contains a Cache-Control header"

- "the response &lt;http header name&gt; is &lt;http header value&gt;"
    - Example: "the response Cache-Control header is None"

- "the response body contains &lt;text token&gt;"
    - Example: "the response body contains Testing text"

### Complete Example:

Below is the [swaggerhub.feature](modules/samples/src/test/resources/cucumber/swaggerhub.feature) in the 
[samples](modules/samples) submodule.

```gherkin
Feature: SwaggerHub REST API

  Background:
    Given the OAS definition at https://api.swaggerhub.com/apis/swagger-hub/registry-api/1.0.10

  Scenario: Default API Listing
    When a request to searchApis is made
    Then the response is a list of APIs in APIs.json format

  Scenario: Owner API Listing
    When a request to getOwnerApis is made
    And owner is swagger-hub
    Then the response is a list of APIs in APIs.json format

  Scenario: API Version Listing
    When a request to getApiVersions is made
    And owner is swagger-hub
    And api is registry-api
    Then the response is a list of API versions in APIs.json format
    And the response body contains
      """
      "url":"/apis/swagger-hub/registry-api"
      """

  Scenario Outline: API Retrieval
    When a request to getDefinition is made
    And owner is <owner>
    And api is <api>
    And version is <version>
    Then a 200 response is returned within 500ms
    And the response type is json
    And the response body contains
    """
    "description":"<description>"
    """
    Examples:
    | owner       | api          | version  | description                       |
    | swagger-hub | registry-api | 1.0.10   | The registry API for SwaggerHub   |
    | fehguy      | sonos-api    | 1.0.0    | A REST API for the Sonos platform |
```

## Extending the vocabulary

You can extend the included REST API testing vocabulary by providing custom StepDefs that tie into the underlying readyapi4j
recipe generation. Do this as follows (a complete example is shown below):
 
1. Create a Custom StepDefs class which you annotate with @ScenarioScoped
2. Create a Constructor into which you inject an instance of CucumberRecipeBuilder
3. Implement your Given/When/Then/And methods to build TestSteps and add them to the builder provided to the constructor
 
Internally the actual recipe gets created and sent to the execution engine first in a Cucumber @After handler 

If you want to delegate some of your custom vocabulary to the existing RestStepDefs you can inject them 
into your custom StepDefs constructor also and then use it as needed.

The below class shows all the above concepts (this class is in the [samples module](modules/samples/src/main/java/com/smartbear/readyapi4j/cucumber/samples/extension/CustomStepDefs.java)):

```java
package com.smartbear.cucumber.samples.extension;

import CucumberRecipeBuilder;
import RestStepDefs;
import cucumber.api.java.en.Given;
import cucumber.runtime.java.guice.ScenarioScoped;

import javax.inject.Inject;

@ScenarioScoped
public class CustomStepDefs {

    private final CucumberRecipeBuilder recipeBuilder;
    private final RestStepDefs restStepDefs;

    @Inject
    public CustomStepDefs(CucumberRecipeBuilder recipeBuilder, RestStepDefs restStepDefs ){
        this.recipeBuilder = recipeBuilder;
        this.restStepDefs = restStepDefs;
    }

    /**
     * Provide an alternative vocabulary for specifying an API endpoint
     */

    @Given("^an endpoint of (.*)$")
    public void anEndpointOf( String endpoint ) throws Throwable {
        restStepDefs.setEndpoint( endpoint );
    }
}
```

To get this used during execution you will need to

1. Compile the above into a jar file
2. Include the JAR file in the classpath for the Cucumber runner
3. Add the containing package(s) of your StepDefs with the -g argument

For example (line-breaks and comments added for readability):

```
java -cp modules/samples/target/readyapi4j-cucumber-samples-1.0.0-SNAPSHOT.jar: // the extension jar
   modules/runner/target/readyapi4j-cucumber-runner-1.0.0-SNAPSHOT.jar          // the runner jar  
   com.smartbear.readyapi4j.cucumber.CucumberRunner                                        // the runner class 
   -g com.smartbear.cucumber.samples.extension                                        // the extension package argument
   modules/samples/src/test/resources/cucumber                                        // the features folder
```

## What's next?

If you've found a bug or are missing some kind of vocabulary/functionality/etc please contribute or 
open an issue!
