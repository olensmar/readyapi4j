package com.smartbear.readyapi4j.dsl.pro

import com.smartbear.readyapi4j.TestRecipeBuilder
import com.smartbear.readyapi4j.dsl.DslDelegate
import com.smartbear.readyapi4j.testengine.teststeps.datasource.GridDataSourceTestStepBuilder

/**
 * The delegate responding to commands inside the "recipe" closure of ServerTestDsl.
 */
class ProDslDelegate extends DslDelegate {

    ProDslDelegate(TestRecipeBuilder testRecipeBuilder) {
        super(testRecipeBuilder)
    }

    void usingExcelFile(String excelFilePath, String testStepName = 'ExcelDataSource',
                        @DelegatesTo(ExcelDataSourceTestStepDelegate) Closure dataSourceConfig) {
        addDataSourceTestStep(new ExcelDataSourceTestStepDelegate(excelFilePath, testStepName), dataSourceConfig)
    }

    void usingCsvFile(String csvFilePath, String testStepName = 'CsvFileDataSource',
                      @DelegatesTo(FileDataSourceTestStepDelegate) Closure dataSourceConfig) {
        addDataSourceTestStep(new FileDataSourceTestStepDelegate(csvFilePath, testStepName), dataSourceConfig)
    }

    void usingData(Map<String, List<String>> data, String testStepName = 'GridDataSource') {
        GridDataSourceTestStepBuilder gridDataSourceTestStepBuilder = new GridDataSourceTestStepBuilder()
        gridDataSourceTestStepBuilder.named(testStepName)
        data.each { gridDataSourceTestStepBuilder.addProperty(it.key, it.value) }
        testRecipeBuilder.addStep(gridDataSourceTestStepBuilder)
    }

    void withGeneratedData(String testStepName = 'DataGenDataSource',
                           @DelegatesTo(DataGenDataSourceTestStepDelegate) Closure dataSourceConfig) {
        addDataSourceTestStep(new DataGenDataSourceTestStepDelegate(testStepName), dataSourceConfig)
    }

    private void addDataSourceTestStep(DataSourceTestStepDelegate delegate, Closure dataSourceConfig) {
        dataSourceConfig.delegate = delegate
        dataSourceConfig.resolveStrategy = Closure.DELEGATE_FIRST
        dataSourceConfig.call()

        testRecipeBuilder.addStep(delegate.dataSourceTestStepBuilder)
    }
}
