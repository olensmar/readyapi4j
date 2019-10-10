package com.smartbear.readyapi4j.testengine.teststeps;

import com.smartbear.readyapi4j.testengine.teststeps.datasource.DataSourceTestStepBuilder;
import com.smartbear.readyapi4j.testengine.teststeps.datasource.ExcelDataSourceTestStepBuilder;
import com.smartbear.readyapi4j.testengine.teststeps.datasource.FileDataSourceTestStepBuilder;
import com.smartbear.readyapi4j.testengine.teststeps.datasource.GridDataSourceTestStepBuilder;
import com.smartbear.readyapi4j.testengine.teststeps.datasource.datagen.DataGenDataSourceTestStepBuilder;
import com.smartbear.readyapi4j.teststeps.TestSteps;

/**
 * Factory methods for test steps that can only be executed by ReadyAPI TestEngine, not by SoapUI.
 */
public class ServerTestSteps extends TestSteps {
    public static DataSourceTestStepBuilder dataSource() {
        return new DataSourceTestStepBuilder();
    }

    public static GridDataSourceTestStepBuilder gridDataSource() {
        return new GridDataSourceTestStepBuilder();
    }

    public static ExcelDataSourceTestStepBuilder excelDataSource() {
        return new ExcelDataSourceTestStepBuilder();
    }

    public static FileDataSourceTestStepBuilder fileDataSource() {
        return new FileDataSourceTestStepBuilder();
    }

    public static DataGenDataSourceTestStepBuilder dataGenDataSource() {
        return new DataGenDataSourceTestStepBuilder();
    }

    public static GridDataSourceTestStepBuilder gridDataSource(String name) {
        return (GridDataSourceTestStepBuilder) new GridDataSourceTestStepBuilder().named(name);
    }

    public static ExcelDataSourceTestStepBuilder excelDataSource(String name) {
        return (ExcelDataSourceTestStepBuilder) new ExcelDataSourceTestStepBuilder().named(name);
    }

    public static FileDataSourceTestStepBuilder fileDataSource(String name) {
        return (FileDataSourceTestStepBuilder) new FileDataSourceTestStepBuilder().named(name);
    }
}
