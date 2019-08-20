package com.bdd.initialSetUp;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.testng.annotations.BeforeClass;

import com.cucumber.listener.ExtentProperties;
import com.cucumber.listener.Reporter;

import cucumber.api.testng.TestNGCucumberRunner;

public class ExtentReport {
	
	public static ExtentProperties extentProperties;
	
		
	public static String getReportConfigPath(){		
        extentProperties.setProjectName("StarBucks Project");
        System.out.println(System.getProperty("user.dir"));
    	String reportConfigPath = System.getProperty("user.dir")+"/src/test/resources/extent-config.xml";
    	if(reportConfigPath!= null) return reportConfigPath;
    	else throw new RuntimeException("Report Config Path not specified in the Configuration.properties file for the Key:reportConfigPath");	
	
    }
	
	public static void writeExtentReport() {
		Reporter.loadXMLConfig(new File(getReportConfigPath()));
		Reporter.setSystemInfo("user", System.getProperty("user.name"));
		Reporter.setSystemInfo("Time Zone", System.getProperty("user.timezone"));
	    Reporter.setSystemInfo("Machine", 	System.getProperty("os.name"));
		Reporter.setTestRunnerOutput("Cucumber reporting using Extent Config");
		
	}

}
