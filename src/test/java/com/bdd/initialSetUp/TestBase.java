package com.bdd.initialSetUp;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.core.pattern.EqualsIgnoreCaseReplacementConverter;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.bdd.runners.TestRunner;
import com.bdd.utils.EmailDriver;
import com.bdd.utils.ExcelDriver;
import com.bdd.utils.Util;
import com.bdd.utils.sendResultEmail;
import com.bdd.utils.Util.FileType;
import com.cucumber.listener.ExtentProperties;
import com.cucumber.listener.Reporter;
import com.galenframework.api.Galen;
import com.galenframework.reports.GalenTestInfo;
import com.galenframework.reports.HtmlReportBuilder;
import com.galenframework.reports.model.LayoutReport;

import cucumber.api.Scenario;
import net.masterthought.cucumber.json.Feature;


public class TestBase extends BrowserDriver{

	public static Properties p;
	public String urlvalue;
	
	
	public static String EnvURL;
	public static String SuiteName ;
	public static Properties CONFIG;
	public static Connection objConn;
	public static String envURL;
	public static FileInputStream fsConfig;
	public static FileInputStream fsAPI;
	public static FileInputStream fsData;
	public static FileInputStream fsDB;
	
    public static String GalenTestReportPath ;
    
    public final String browserName = System.getProperty("browserName");
    public final String executionType = System.getProperty("executionType");
    public final String gridHubURL  = System.getProperty("gridHubURL");
    public final String platform = System.getProperty("platform");
    public final String region = System.getProperty("region");
    public final String deviceName = System.getProperty("deviceName");
    public final String os_version = System.getProperty("os_version");
    
	/**
	 * Function Setuptest will execute before every test class.
	 *
	 * @param browser
	 *            the browser
	 * @return the web driver
	 * @throws Exception
	 *             the exception
	 */
	
	public void setUpEnv() {
		
		try{
			
			CONFIG = new Properties();
			TestBase.loadPropertyFiles();
			
			WebDriver ldriver = setupTest(browserName);
			
            if(region.equalsIgnoreCase("dev")){
				
				envURL = CONFIG.getProperty("URL_dev");
				
			}
			if(region.equalsIgnoreCase("qa")){
				
				envURL = CONFIG.getProperty("URL_qa");
				
			}
			else if(region.equalsIgnoreCase("sit")){
				
				envURL = CONFIG.getProperty("URL_sit");
				
			}
			else if(region.equalsIgnoreCase("uat")){
				
			 envURL = CONFIG.getProperty("URL_uat");			
				
			}
			
			ldriver.get(envURL);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public WebDriver setupTest(String browser) throws Exception {
		
		WebDriver ldriver = getDriverVal(browser);

		ldriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		ldriver.manage().timeouts().pageLoadTimeout(300, TimeUnit.SECONDS);
		ldriver.manage().timeouts().setScriptTimeout(10, TimeUnit.SECONDS);
		//ldriver.manage().window().maximize();			
		Thread.sleep(5000);
		return ldriver;		
	}
		

	/**
	 * Function Tear down will execute after each test class.
	 */
	public void TearDown() {
		WebDriver ldriver=BrowserDriver.getBrowserInstance();
		try {
			
				ldriver.close();
				Thread.sleep(4000);
				ldriver.quit();
				
		 } catch (Exception e) {
			 
		   }
				
	}	
	
	public static void loadPropertyFiles() throws Exception{
		        fsConfig = new FileInputStream(
				        System.getProperty("user.dir") + "//src//test//resources//envConfig.properties");
				/*fsDB = new FileInputStream(
						System.getProperty("user.dir") + "//src//test//resources//DataBaseQueries.properties");*/
				fsData= new FileInputStream(
						System.getProperty("user.dir") + "//src//test//resources//TestData.properties");
				/*fsAPI= new FileInputStream(
						System.getProperty("user.dir") + "//src//test//resources//API.properties");*/
				CONFIG.load(fsConfig);
				CONFIG.load(fsData);
				/*CONFIG.load(fsDB);
				CONFIG.load(fsData);
				CONFIG.load(fsAPI);*/				
				
	}

	public void onComplete() {
		
		try {
			  loadPropertyFiles();
			
			  //sendResultEmail sndEmail = new sendResultEmail();
			  			
			  String resultFile = null;
			  String Recipients; 
			  
			  //Recipients = Util.getValFromResource("resultEmailRecipients");
			  
			  //sndEmail.sendResultEmail(resultFile, Recipients);
			  
		} catch (Exception e) {
			e.printStackTrace();
		 }
	  }
	
     public LayoutReport creativeCheckLayout(WebDriver driver,String PageSpec, String ViewPort,String ScenarioName) throws IOException{
        
    	TestRunner TR = new TestRunner();
    	
        GalenTestInfo test = GalenTestInfo.fromString(ScenarioName);
        LayoutReport report = Galen.checkLayout(driver,PageSpec,Arrays.asList(ViewPort));
        test.getReport().layout(report,ScenarioName);
        TR.tests.add(test);
        GalenTestReportPath = "test-output/GallenReports/"+TR.upDate+"_"+ScenarioName;   
        TR.repObj.build(TR.tests,GalenTestReportPath);        
        return report;
        
     }
     
}  
