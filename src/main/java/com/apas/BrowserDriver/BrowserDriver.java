package com.apas.BrowserDriver;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.openqa.selenium.Platform;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import com.apas.Utils.Util;

public class BrowserDriver {
	public static ThreadLocal<RemoteWebDriver> dr = new ThreadLocal<RemoteWebDriver>();

	/**
	 * Initializes the RemoteWebDriver and internally calls setWebDriver() method
	 * 
	 * @param browser: "Chrome" / "Firefox"
	 * @return: It returns the the instance of RemoteWebDriver
	 * @throws Exception
	 */
	public RemoteWebDriver getDriver(String browser) throws Exception {
		URL url = new URL(Util.getValFromResource("grid.hub"));
		RemoteWebDriver Wdriver = null;

		System.setProperty("jsse.enableSNIExtension", "false");

		if (Wdriver == null) {
			if (browser.equalsIgnoreCase("firefox")) {
				System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE, "true");
				System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null");

				DesiredCapabilities capabilities = DesiredCapabilities.firefox();
				capabilities.setCapability("marionette", true);
				capabilities.setCapability("acceptInsecureCerts", true);

				FirefoxProfile profile = new FirefoxProfile();
				profile.setPreference("dom.file.createInChild", true);
				profile.setPreference("browser.download.folderList", 2);
				profile.setPreference("browser.helperApps.alwaysAsk.force", false);
				profile.setPreference("browser.download.manager.showWhenStarting", false);
				profile.setPreference("dom.webnotifications.enabled", false);
				File file = new File("C:\\Downloads");
				String strPath = file.getAbsolutePath();
				profile.setPreference("browser.download.dir", strPath);
				profile.setPreference("browser.download.downloadDir", strPath);
				profile.setPreference("browser.download.defaultFolder", strPath);
				profile.setPreference("browser.helperApps.neverAsk.saveToDisk", "text/anytext,text/plain,text,jpeg");

				capabilities.setCapability(FirefoxDriver.PROFILE, profile);

				Wdriver = new RemoteWebDriver(url, capabilities);
				Wdriver.manage().window().maximize();
				setWebDriver(Wdriver);

			} else if (browser.equalsIgnoreCase("chrome")) {
				String downloadFilepath = "C:\\Downloads";
				HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
				chromePrefs.put("profile.default_content_settings.popups", 0);
				chromePrefs.put("download.default_directory", downloadFilepath);
				chromePrefs.put("profile.default_content_setting_values.notifications", 2);

				ChromeOptions options = new ChromeOptions();
				options.setExperimentalOption("prefs", chromePrefs);
				options.addArguments("disable-infobars");

				DesiredCapabilities dwc = DesiredCapabilities.chrome();
				dwc.setPlatform(Platform.WINDOWS);
				dwc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				dwc.setCapability("unexpectedAlertBehaviour", "accept");
				dwc.setCapability(ChromeOptions.CAPABILITY, options);
				System.out.println("Opened Chrome");
				Wdriver = new RemoteWebDriver(url, dwc);
				Wdriver.manage().window().maximize();
				setWebDriver(Wdriver);

			} else if (browser.equalsIgnoreCase("IE")) {
				DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
				System.setProperty("webdriver.ie.driver", "C:\\Users\\akaila\\Documents\\SeleniumGrid\\IEDriverServer.exe");
				capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
				capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
				capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
				capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, false);
				capabilities.setCapability("ie.ensureCleanSession", true);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

				Wdriver = new RemoteWebDriver(url, capabilities);
				Wdriver.manage().window().maximize();

			} else if (browser.equalsIgnoreCase("Edge")) {
				DesiredCapabilities capabilities = DesiredCapabilities.edge();
				System.setProperty("webdriver.edge.driver", "C:\\Users\\akaila\\Documents\\SeleniumGrid\\MicrosoftWebDriver.exe");
				Wdriver = new RemoteWebDriver(url, capabilities);
				Wdriver.manage().window().maximize();

			} else if (browser.equalsIgnoreCase("safari")) {
				DesiredCapabilities capabilities = DesiredCapabilities.safari();
				SafariOptions options = new SafariOptions();
				capabilities.setPlatform(org.openqa.selenium.Platform.MAC);
				capabilities.setBrowserName("safari");
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

				Wdriver = new RemoteWebDriver(url, capabilities);
				Wdriver.manage().window().maximize();
			}
		}
		return Wdriver;
	}

	/**
	 * Internally calls getDriver(browser) method
	 * 
	 * @param browser: "Chrome" / "Firefox"
	 * @return: It returns the the instance of RemoteWebDriver
	 * @throws Exception
	 */
	public RemoteWebDriver getDriverVal(String browser) throws Exception {
		getDriver(browser);
		return dr.get();
	}

	/**
	 * Sets the value of class variable "dr" of ThreadLocal type
	 * 
	 * @param: Accepts argument of RemoteWebDriver type
	 */
	public void setWebDriver(RemoteWebDriver driver) {
		dr.set(driver);
	}

	/**
	 * @return: It returns the class variable "dr" of ThreadLocal type 
	 */
	public static RemoteWebDriver getBrowserInstance() {
		return dr.get();
	}
}