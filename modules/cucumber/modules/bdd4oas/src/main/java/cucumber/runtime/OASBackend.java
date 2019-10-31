package cucumber.runtime;

import com.google.common.collect.Lists;
import com.smartbear.readyapi4j.cucumber.RestStepDefs;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.pickles.PickleStep;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.TypeRegistry;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom Backend that reads OAS BDD extensions and translates them to the readyapi4j OAS StepDefs
 */

public class OASBackend implements Backend {

    private JavaBackend javaBackend;
    private List<StepDefinitionWrapper> wrapperList = Lists.newArrayList();
    private OASWrapper oasWrapper;

    public OASBackend(ResourceLoader resourceLoader, TypeRegistry typeRegistry) {
        javaBackend = new JavaBackend(resourceLoader, typeRegistry);
    }

    @Override
    public void loadGlue(Glue glue, List<URI> list) {
        ArrayList<URI> glueList = Lists.newArrayList(list);

        try {
            glueList.add( new URI("classpath:com/smartbear/readyapi4j/cucumber" ));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        GlueWrapper glueWrapper = new GlueWrapper(glue);
        javaBackend.loadGlue(glueWrapper, glueList);
    }

    @Override
    public void buildWorld() {
        javaBackend.buildWorld();
    }

    @Override
    public void disposeWorld() {
        javaBackend.disposeWorld();
    }

    @Override
    public List<String> getSnippet(PickleStep pickleStep, String s, FunctionNameGenerator functionNameGenerator) {
        return javaBackend.getSnippet( pickleStep, s, functionNameGenerator);
    }

    /**
     * Glue wrapper so we can wrap step definitions for runtime interception
     */

    private class GlueWrapper implements Glue {
        private Glue glue;

        public GlueWrapper(Glue glue) {
            this.glue = glue;
        }

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) throws DuplicateStepDefinitionException {
            glue.addStepDefinition(new StepDefinitionWrapper(stepDefinition));
        }

        @Override
        public void addBeforeHook(HookDefinition hookDefinition) {
            glue.addBeforeHook(hookDefinition);
        }

        @Override
        public void addAfterHook(HookDefinition hookDefinition) {
            glue.addAfterHook(hookDefinition);
        }

        @Override
        public void addBeforeStepHook(HookDefinition hookDefinition) {
            glue.addBeforeStepHook(hookDefinition);
        }

        @Override
        public void addAfterStepHook(HookDefinition hookDefinition) {
            glue.addAfterStepHook(hookDefinition);
        }

        @Override
        public void removeScenarioScopedGlue() {
            glue.removeScenarioScopedGlue();
        }
    }

    /**
     * StepDefinition Wrapper that allows us to intercept custom bdd statements defined in OAS extensions and translate
     * them to defined StepDefs
     */

    private class StepDefinitionWrapper implements StepDefinition {

        private final StepDefinition stepDefinition;

        StepDefinitionWrapper( StepDefinition stepDefinition ){
            this.stepDefinition = stepDefinition;
        }

        @Override
        public List<Argument> matchedArguments(PickleStep pickleStep) {
            if( pickleStep.getText().startsWith( "the OAS definition at ")){
                String oas = pickleStep.getText().substring("the OAS definition at ".length());
                if( oasWrapper == null ) {
                    oasWrapper = new OASWrapper(oas);
                }
                else {
                    oasWrapper.loadDefinition( oas );
                }
            }
            else if( stepDefinition.getPattern().equalsIgnoreCase("^a request to ([^ ]*) with parameters$")){
                if( oasWrapper != null ){
                    OASWrapper.WhenOperationWrapper operationWrapper = oasWrapper.getWhen( pickleStep.getText());
                    if( operationWrapper != null ){
                        return extractOperationArguments(operationWrapper);
                    }
                }
            }
            else if( stepDefinition.getPattern().equalsIgnoreCase("^the response is (.*)$")){
                if( oasWrapper != null ){
                    OASWrapper.ThenResponseWrapper responseWrapper = oasWrapper.getThen( pickleStep.getText());
                    if( responseWrapper != null ){
                        return Lists.newArrayList( new ApiResponseArgument(responseWrapper));
                    }
                }
            }

            return stepDefinition.matchedArguments( pickleStep );
        }

        private List<Argument> extractOperationArguments(OASWrapper.WhenOperationWrapper operation) {
            Map<String, String> parameters = operation.getParameters();
            if( parameters != null && !parameters.isEmpty()){
                StringBuilder builder = new StringBuilder();
                for( String name : parameters.keySet()){
                    builder.append(name).append('=').append(parameters.get(name)).append('\n');
                }
                return Lists.newArrayList(new StringArgument(operation.getOperation().getOperationId()),
                        new StringArgument(builder.toString()));
            }
            else {
                return Lists.newArrayList(new StringArgument(operation.getOperation().getOperationId()),
                        new StringArgument(""));
            }
        }

        @Override
        public String getLocation(boolean b) {
            return stepDefinition.getLocation(b);
        }

        @Override
        public Integer getParameterCount() {
            return stepDefinition.getParameterCount();
        }

        @Override
        public void execute(Object[] objects) throws Throwable {
            if( objects.length == 1 && objects[0] instanceof OASWrapper.ThenResponseWrapper) {
                OASWrapper.ThenResponseWrapper argument = (OASWrapper.ThenResponseWrapper) objects[0];
                stepDefinition.execute( new Object[]{ argument.getApiResponse().getDescription() });

                if( argument.getAssertions() != null ) {
                    ObjectFactory objectFactory = (ObjectFactory) FieldUtils.readField(stepDefinition, "objectFactory", true);
                    RestStepDefs stepDefs = objectFactory.getInstance(RestStepDefs.class);
                }
            }
            else {
                stepDefinition.execute(objects);
            }
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return stepDefinition.isDefinedAt(stackTraceElement);
        }

        @Override
        public String getPattern() {
            return stepDefinition.getPattern();
        }

        @Override
        @Deprecated
        public boolean isScenarioScoped() {
            return stepDefinition.isScenarioScoped();
        }
    }

    private class StringArgument implements Argument {

        private final String value;

        StringArgument(String string ){
            this.value = string;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    private class ApiResponseArgument implements Argument {

        private final OASWrapper.ThenResponseWrapper value;

        ApiResponseArgument(OASWrapper.ThenResponseWrapper apiResponse ){
            this.value = apiResponse;
        }

        @Override
        public OASWrapper.ThenResponseWrapper getValue() {
            return value;
        }
    }
}
