package com.bdd.runners;

import cucumber.api.CucumberOptions;
import cucumber.api.Scenario;
import cucumber.api.testng.TestNGCucumberRunner;
import net.masterthought.cucumber.json.Feature;
import cucumber.api.testng.CucumberFeatureWrapper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.bdd.initialSetUp.BrowserDriver;
import com.bdd.initialSetUp.ExtentReport;
import com.bdd.initialSetUp.TestBase;
import com.cucumber.listener.ExtentProperties;
import com.cucumber.listener.Reporter;
import com.galenframework.reports.GalenTestInfo;
import com.galenframework.reports.HtmlReportBuilder;


 
@CucumberOptions(
        features = "src\\test\\resources\\features\\",
        glue = {"com.bdd.stepDefinitions","com.bdd.hooks"},
        tags = {"~@Ignore"},
        plugin={"com.cucumber.listener.ExtentCucumberFormatter:","json:target/cucumber-reports/Cucumber.json"},
        monochrome = true)
        
       

public class TestRunner extends TestBase {
	
	private TestNGCucumberRunner testNGCucumberRunner;
    public static String upDate;
    
    public static List<GalenTestInfo>tests;
    public static HtmlReportBuilder repObj;  

    
    @BeforeSuite
    public void initBuild() {
    	    		
      setUpEnv();
    	
    }
 
	@BeforeClass(alwaysRun = true)
    public void setUpClass() throws Exception {
        testNGCucumberRunner = new TestNGCucumberRunner(this.getClass());
        System.out.println("Setup for report");
        Date date= new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm");
        upDate=sdf.format(date);
        tests = new LinkedList<GalenTestInfo>();
        repObj = new HtmlReportBuilder();
		ExtentReport.extentProperties = ExtentProperties.INSTANCE;
		ExtentReport.extentProperties.setReportPath(System.getProperty("user.dir")+"/test-output/html-Results/"+ upDate+"_" +browserName+"_result.html");
     }
 
    @Test(groups = "cucumber", description = "Runs Cucumber Feature", dataProvider = "features")
    public void feature(CucumberFeatureWrapper cucumberFeature) {
        testNGCucumberRunner.runCucumber(cucumberFeature.getCucumberFeature());
    }
 
    @DataProvider
    public Object[][] features() {
        return testNGCucumberRunner.provideFeatures();
    }
 
    @AfterClass(alwaysRun = true)
    public void tearDownClass() throws Exception {
    	ExtentReport.writeExtentReport();
        testNGCucumberRunner.finish();
        TearDown();        
    }
    
    @AfterSuite
    public void onFinish() {    	
    	onComplete();
    }    
    
    
}