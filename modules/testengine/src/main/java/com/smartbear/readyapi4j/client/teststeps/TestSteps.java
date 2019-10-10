package com.smartbear.readyapi4j.client.teststeps;

import com.smartbear.readyapi4j.testengine.teststeps.ServerTestSteps;
import com.smartbear.readyapi4j.testengine.teststeps.datasource.DataSourceTestStepBuilder;
import com.smartbear.readyapi4j.testengine.teststeps.datasource.ExcelDataSourceTestStepBuilder;
import com.smartbear.readyapi4j.testengine.teststeps.datasource.FileDataSourceTestStepBuilder;
import com.smartbear.readyapi4j.testengine.teststeps.datasource.GridDataSourceTestStepBuilder;
import com.smartbear.readyapi4j.testengine.teststeps.datasource.datagen.DataGenDataSourceTestStepBuilder;
import com.smartbear.readyapi4j.teststeps.groovyscript.GroovyScriptTestStepBuilder;
import com.smartbear.readyapi4j.teststeps.jdbcrequest.JdbcConnection;
import com.smartbear.readyapi4j.teststeps.plugin.PluginTestStepBuilder;
import com.smartbear.readyapi4j.teststeps.propertytransfer.PropertyTransferTestStepBuilder;
import com.smartbear.readyapi4j.teststeps.restrequest.RestRequestStepBuilder;
import com.smartbear.readyapi4j.teststeps.restrequest.RestRequestStepWithBodyBuilder;
import com.smartbear.readyapi4j.teststeps.soaprequest.SoapRequestStepBuilder;

import java.net.URL;

/**
 * This class exists solely for backward compatibility. The <code>ServerTestSteps</code> class should be used instead.
 *
 * @see ServerTestSteps
 */
@Deprecated
public class TestSteps {

    public static SoapRequestStepBuilder soapRequest(URL wsdlUrl) {
        return ServerTestSteps.soapRequest(wsdlUrl);
    }

    public static RestRequestStepBuilder<RestRequestStepBuilder> restRequest() {
        return ServerTestSteps.restRequest();
    }

    public static RestRequestStepBuilder<RestRequestStepBuilder> getRequest(String uri) {
        return ServerTestSteps.GET(uri);
    }

    public static RestRequestStepWithBodyBuilder postRequest(String uri) {
        return ServerTestSteps.POST(uri);
    }

    public static RestRequestStepWithBodyBuilder putRequest(String uri) {
        return ServerTestSteps.PUT(uri);
    }

    public static RestRequestStepBuilder<RestRequestStepBuilder> deleteRequest(String uri) {
        return ServerTestSteps.DELETE(uri);
    }

    public static PropertyTransferTestStepBuilder propertyTransfer() {
        return ServerTestSteps.propertyTransfer();
    }


    public static GroovyScriptTestStepBuilder groovyScriptStep(String scriptText) {
        return ServerTestSteps.groovyScriptStep(scriptText);
    }

    public static JdbcConnection jdbcConnection(String driver, String connectionString) {
        return ServerTestSteps.jdbcConnection(driver, connectionString);
    }

    /**
     * @param pluginTestStepType test step type defined by plugin. For example one of 'MQTTPublishTestStep',
     *                           'MQTTDropConnectionTestStep' or 'MQTTReceiveTestStep' defined by MQTT ReadyAPI plugin.
     * @return PluginTestStepBuilder
     */
    public static PluginTestStepBuilder pluginTestStep(String pluginTestStepType) {
        return com.smartbear.readyapi4j.teststeps.TestSteps.pluginTestStep(pluginTestStepType);
    }

    public static DataSourceTestStepBuilder dataSource() {
        return ServerTestSteps.dataSource();
    }

    public static GridDataSourceTestStepBuilder gridDataSource() {
        return ServerTestSteps.gridDataSource();
    }

    public static ExcelDataSourceTestStepBuilder excelDataSource() {
        return ServerTestSteps.excelDataSource();
    }

    public static FileDataSourceTestStepBuilder fileDataSource() {
        return ServerTestSteps.fileDataSource();
    }

    public static DataGenDataSourceTestStepBuilder dataGenDataSource() {
        return ServerTestSteps.dataGenDataSource();
    }

    public static GridDataSourceTestStepBuilder gridDataSource(String name) {
        return ServerTestSteps.gridDataSource(name);
    }

    public static ExcelDataSourceTestStepBuilder excelDataSource(String name) {
        return (ExcelDataSourceTestStepBuilder) new ExcelDataSourceTestStepBuilder().named(name);
    }

    public static FileDataSourceTestStepBuilder fileDataSource(String name) {
        return (FileDataSourceTestStepBuilder) new FileDataSourceTestStepBuilder().named(name);
    }

}
