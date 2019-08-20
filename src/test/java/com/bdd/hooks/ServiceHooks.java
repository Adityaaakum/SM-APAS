
package com.bdd.hooks;
 
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeClass;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.gherkin.model.Feature;
import com.bdd.initialSetUp.BrowserDriver;
import com.bdd.initialSetUp.ExtentReport;
import com.cucumber.listener.ExtentProperties;
import com.cucumber.listener.Reporter;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;

 
public class ServiceHooks {

	
    @Before
    public void initializeTest(){
    	//ExtentTest feature = extent.createTest(Feature.class, "Refund item");
    }
 
    @After
    public void embedScreenshot(Scenario scenario) {
        if (scenario.isFailed()) {
            try {
            	WebDriver ldriver = BrowserDriver.getBrowserInstance();
    			TakesScreenshot ts = (TakesScreenshot) ldriver;
    			File source = ts.getScreenshotAs(OutputType.FILE);
    			Date date= new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
                String upDate=sdf.format(date);
                
                String dest = System.getProperty("user.dir")+"/test-output/html-Results/ErrorScreenshots/"+ upDate+"_"+scenario.getName() +".png";
                
                File destinationPath = new File(dest);
    			FileUtils.copyFile(source, destinationPath);
    			Reporter.addScreenCaptureFromPath(destinationPath.toString());
    			
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    
}