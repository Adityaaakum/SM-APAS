package com.apas.TestBase;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import com.apas.Utils.FileUtils;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.apas.BrowserDriver.BrowserDriver;

public class TestBase extends BrowserDriver {

	public static Properties CONFIG;
	public static FileInputStream fsEnv;
	public static FileInputStream fsConfig;
	public static FileInputStream fsData;
	
	public final static String browserName = System.getProperty("browserName");
	public final String executionType = System.getProperty("executionType");
	public final String gridHubURL = System.getProperty("gridHubURL");
	public final String platform = System.getProperty("platform");
	public final String region = System.getProperty("region");
	public final String deviceName = System.getProperty("deviceName");
	public final String os_version = System.getProperty("os_version");
	public final boolean flagToUpdateJira = Boolean.parseBoolean(System.getProperty("flagToUpdateJira"));
	public static String testCycle = System.getProperty("testCycle");


	public static String envURL;
	public static ArrayList<String> failedMethods = new ArrayList<String>();

	/**
	 * Function SetupTest will be executed before every test class.
	 */
	public RemoteWebDriver setupTest() throws Exception {
		RemoteWebDriver ldriver = getDriver(browserName);
		try {
			CONFIG = new Properties();
			TestBase.loadPropertyFiles();
			if (region.equalsIgnoreCase("dev")) {
				envURL = CONFIG.getProperty("URL_dev");
			}else if (region.equalsIgnoreCase("qa")) {
				envURL = CONFIG.getProperty("URL_qa");
			} else if (region.equalsIgnoreCase("sit")) {
				envURL = CONFIG.getProperty("URL_sit");
			} else if (region.equalsIgnoreCase("uat")) {
				envURL = CONFIG.getProperty("URL_uat");
			} else if (region.equalsIgnoreCase("preuat")) {
				envURL = CONFIG.getProperty("URL_preuat");
			}
			ldriver.get(envURL);

			//Temp folder is to create temporary file during tests so that those can be deleted at the end if required
			FileUtils.createFolder(System.getProperty("user.dir") + CONFIG.getProperty("temporaryFolderPath"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ldriver;
	}

	/**
	 * Function Tear down will execute after each test class.
	 */
	public void TearDown() {
		RemoteWebDriver ldriver = BrowserDriver.getBrowserInstance();
		try {
			ldriver.close();
			Thread.sleep(4000);
			ldriver.quit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This function will load the properties from the multiple property files in CONFIG
	 * 
	 */
	public static void loadPropertyFiles() throws Exception {
		fsEnv  = new FileInputStream(System.getProperty("user.dir") + "//src//test//resources//envConfig" + System.getProperty("region").toUpperCase() + ".properties");
		fsConfig = new FileInputStream(System.getProperty("user.dir") + "//src//test//resources//envConfig.properties");
		fsData = new FileInputStream(System.getProperty("user.dir") + "//src//test//resources//TestData.properties");
		CONFIG.load(fsEnv);
		CONFIG.load(fsConfig);
		CONFIG.load(fsData);
	}

}
