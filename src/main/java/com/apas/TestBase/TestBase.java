package com.apas.TestBase;

import java.io.FileInputStream;
import java.util.Properties;

import org.openqa.selenium.remote.RemoteWebDriver;

import com.apas.BrowserDriver.BrowserDriver;
import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;

public class TestBase extends BrowserDriver {

	public static Properties CONFIG;
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

	/**
	 * Function Setuptest will execute before every test class.
	 *
	 * @param browser
	 *            the browser
	 * @return the web driver
	 * @throws Exception
	 *             the exception
	 */

	// public final static String browserName = "chrome";
	// public final String region = "qa";
	public static String envURL;

	public RemoteWebDriver setupTest() throws Exception {
		RemoteWebDriver ldriver = getDriver(browserName);
		try {
			CONFIG = new Properties();
			TestBase.loadPropertyFiles();

			if (region.equalsIgnoreCase("dev")) {
				envURL = CONFIG.getProperty("URL_dev");
			}
			if (region.equalsIgnoreCase("qa")) {
				envURL = CONFIG.getProperty("URL_qa");
			} else if (region.equalsIgnoreCase("sit")) {
				envURL = CONFIG.getProperty("URL_sit");
			} else if (region.equalsIgnoreCase("uat")) {
				envURL = CONFIG.getProperty("URL_uat");
			}
			ldriver.get(envURL);
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
			loadPropertyFiles();
			ldriver.close();
			Thread.sleep(4000);
			ldriver.quit();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static void loadPropertyFiles() throws Exception {
		fsConfig = new FileInputStream(System.getProperty("user.dir") + "//src//test//resources//envConfig.properties");
		fsData = new FileInputStream(System.getProperty("user.dir") + "//src//test//resources//TestData.properties");
		CONFIG.load(fsConfig);
		CONFIG.load(fsData);
	}

	public static void reportLogger(boolean flag, String Message) {
		if (flag) {
			ExtentTestManager.getTest().log(LogStatus.PASS, "Test Step Passed :" + Message);
		} else {
			ExtentTestManager.getTest().log(LogStatus.FAIL, "Test Step Failed: " + Message);
		}
	}
}
