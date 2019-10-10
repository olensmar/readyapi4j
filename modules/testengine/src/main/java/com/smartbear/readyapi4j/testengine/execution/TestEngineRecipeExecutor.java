package com.smartbear.readyapi4j.testengine.execution;

import com.smartbear.readyapi4j.TestRecipe;
import com.smartbear.readyapi4j.execution.ExecutionMode;
import com.smartbear.readyapi4j.execution.RecipeExecutor;
import com.smartbear.readyapi4j.execution.RecipeFilter;
import com.smartbear.readyapi4j.extractor.ExtractorData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Executor for executing Recipes on a TestEngine instances - both synchronously and asynchronously
 */
public class TestEngineRecipeExecutor extends AbstractTestEngineExecutor implements RecipeExecutor {
    private static Logger logger = LoggerFactory.getLogger(TestEngineRecipeExecutor.class);

    private final List<RecipeFilter> recipeFilters = new CopyOnWriteArrayList<>();

    TestEngineRecipeExecutor(TestEngineClient testEngineClient) {
        super(testEngineClient);
    }

    @Override
    public void addRecipeFilter(RecipeFilter recipeFilter) {
        recipeFilters.add(recipeFilter);
    }

    @Override
    public void removeRecipeFilter(RecipeFilter recipeFilter) {
        recipeFilters.remove(recipeFilter);
    }

    @Override
    public ExecutionMode getExecutionMode() {
        return ExecutionMode.REMOTE;
    }

    @Override
    public TestEngineExecution submitRecipe(TestRecipe recipe) {
        for (RecipeFilter recipeFilter : recipeFilters) {
            recipeFilter.filterRecipe(recipe);
        }

        TestEngineExecution execution = doExecuteTestCase(recipe, recipe.getExtractorData(), true);
        notifyExecutionStarted(execution);
        return execution;
    }

    @Override
    public TestEngineExecution executeRecipe(TestRecipe recipe) {
        for (RecipeFilter recipeFilter : recipeFilters) {
            recipeFilter.filterRecipe(recipe);
        }

        TestEngineExecution execution = doExecuteTestCase(recipe, recipe.getExtractorData(), false);
        notifyExecutionFinished(execution);
        return execution;
    }

    private TestEngineExecution doExecuteTestCase(TestRecipe testRecipe, ExtractorData optionalExtractorData, boolean async) {
        try {
            Optional<ExtractorData> extractorDataOptional = Optional.ofNullable(optionalExtractorData);
            extractorDataOptional.ifPresent(extractorData -> extractorDataList.add(extractorData));
            TestEngineExecution execution = testEngineClient.postTestRecipe(testRecipe, async);
            return execution;
        } catch (ApiException e) {
            notifyErrorOccurred(e);
            logger.debug("An error occurred when sending test recipe to server. Details: " + e.toString());
            throw e;
        } catch (Exception e) {
            notifyErrorOccurred(e);
            logger.debug("An error occurred when sending test recipe to server", e);
            throw new ApiException(e);
        }
    }
}
