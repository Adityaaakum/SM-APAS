package com.bdd.initialSetUp;


import java.io.File;
import java.net.URL;
import java.util.HashMap;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.testng.ITestResult;

import com.bdd.utils.Util;


public class BrowserDriver {
	
	public static ThreadLocal<RemoteWebDriver> dr = new ThreadLocal<RemoteWebDriver>();
	public static RemoteWebDriver Wdriver = null;
	/**
	 * Function will return the driver instance depends upon the browser value
	 * passed.
	 *
	 * @param browser
	 *            the browser
	 * @return the driver
	 * @throws Exception
	 *             the exception
	 */
	
	//private static String browserName;
	
	public WebDriver getDriver(String browser) throws Exception {

		URL url = new URL(Util.getValFromResource("grid.hub"));
		//EventFiringWebDriver driver = null;
		System.setProperty("jsse.enableSNIExtension", "false");
		//RemoteWebDriver Wdriver = null;
		if (Wdriver == null) {
			if (browser.equalsIgnoreCase("firefox")) {
				
				System.setProperty(FirefoxDriver.SystemProperty.DRIVER_USE_MARIONETTE,"true");
				System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
				
				DesiredCapabilities capabilities=DesiredCapabilities.firefox();
				capabilities.setCapability("marionette", true);
				capabilities.setCapability("acceptInsecureCerts",true);
				FirefoxProfile profile = new FirefoxProfile();
				profile.setPreference("dom.file.createInChild", true);
				profile.setPreference("browser.download.folderList", 2);
				profile.setPreference("browser.helperApps.alwaysAsk.force", false);
				profile.setPreference("browser.download.manager.showWhenStarting",false);
				File file = new File("C:\\Downloads");	    
				String strPath =file.getAbsolutePath();
				profile.setPreference("browser.download.dir", strPath); 
				profile.setPreference("browser.download.downloadDir",strPath); 
				profile.setPreference("browser.download.defaultFolder",strPath); 
				profile.setPreference("browser.helperApps.neverAsk.saveToDisk","text/anytext,text/plain,text,jpeg");
										
				capabilities.setCapability(FirefoxDriver.PROFILE, profile);
				
				/*FirefoxProfile profile = new FirefoxProfile();
				DesiredCapabilities dwf = DesiredCapabilities.firefox();
				dwf.setPlatform(Platform.WINDOWS);
				dwf.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				dwf.setCapability("unexpectedAlertBehaviour", "accept");
				dwf.setCapability(FirefoxDriver.PROFILE, profile);*/
				Wdriver = new RemoteWebDriver(url, capabilities);
				//Wdriver.manage().window().maximize();
				setWebDriver(Wdriver);
				//driver = eventRegister(Wdriver);

			} else if (browser.equalsIgnoreCase("chrome")) {
				
				String downloadFilepath = "C:\\Downloads";
				HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
				chromePrefs.put("profile.default_content_settings.popups", 0);
				chromePrefs.put("download.default_directory", downloadFilepath);
				ChromeOptions options = new ChromeOptions();
				options.setExperimentalOption("prefs", chromePrefs);
				DesiredCapabilities dwc = DesiredCapabilities.chrome();
				dwc.setPlatform(Platform.WINDOWS);
				dwc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				dwc.setCapability("unexpectedAlertBehaviour", "accept");
				dwc.setCapability(ChromeOptions.CAPABILITY, options);
				Wdriver = new RemoteWebDriver(url, dwc);
				Wdriver.manage().window().maximize();
				setWebDriver(Wdriver);
				//driver = eventRegister(Wdriver);				

			} else if (browser.equalsIgnoreCase("IE")) {
				//InternetExplorerDriverManager.getInstance().setup();
				DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
				System.setProperty("webdriver.ie.driver", "C:\\Users\\akaila\\Documents\\SeleniumGrid\\IEDriverServer.exe");
				capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
						true);
				capabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, false);
				capabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
				capabilities.setCapability(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS, false);
				capabilities.setCapability("ie.ensureCleanSession", true);				
				//capabilities.setCapability("nativeEvents", false);
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);

				url = new URL(Util.getValFromResource("grid.hub"));
				Wdriver = new RemoteWebDriver(url, capabilities);
				//Wdriver = new InternetExplorerDriver(capabilities);
				Wdriver.manage().window().maximize();
				setWebDriver(Wdriver);
				//driver = eventRegister(Wdriver); 
			}
			else if (browser.equalsIgnoreCase("Edge")) {
				DesiredCapabilities capabilities = DesiredCapabilities.edge();
				System.setProperty("webdriver.edge.driver", "C:\\Users\\akaila\\Documents\\SeleniumGrid\\MicrosoftWebDriver.exe");
				url = new URL(Util.getValFromResource("grid.hub"));				
				Wdriver = new RemoteWebDriver(url, capabilities);
				Wdriver.manage().window().maximize();
				setWebDriver(Wdriver);
				//driver = eventRegister(Wdriver);
			}
			else if (browser.equalsIgnoreCase("safari")) {
				DesiredCapabilities capabilities = DesiredCapabilities.safari();
				SafariOptions options = new SafariOptions();
				options.setUseCleanSession(true);
				capabilities.setPlatform(org.openqa.selenium.Platform.MAC);
				capabilities.setBrowserName("safari");
				capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				Wdriver = new RemoteWebDriver(url, capabilities);
				Wdriver.manage().window().maximize();
				setWebDriver(Wdriver);
				//driver = eventRegister(Wdriver);
				}
			
			else if (browser.equalsIgnoreCase("CreativeChrome")) {

				DesiredCapabilities dwc = DesiredCapabilities.chrome();
				dwc.setPlatform(Platform.WINDOWS);
				dwc.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
				dwc.setCapability("unexpectedAlertBehaviour", "accept");
				Wdriver = new RemoteWebDriver(url, dwc);
				setWebDriver(Wdriver);
				Wdriver.manage().window().setSize(new Dimension(320, 768));
				//driver = eventRegister(Wdriver);				

			}
		   
			else if(browser.equalsIgnoreCase("Android")) {
				
				/*DesiredCapabilities capabilities = new DesiredCapabilities().android();
		        capabilities.setCapability("browserName","chrome");
		        
		        capabilities.setCapability("chromedriverExecutable","D:\SeleniumGrid\\chromedriver.exe");
		        
		        capabilities.setCapability("platformName","Android");
		        capabilities.setCapability("platformVersion","5.1.1");*/
		        
				
		       // Created object of DesiredCapabilities class.
		        DesiredCapabilities capabilities = new DesiredCapabilities();
		        
		        capabilities.setCapability("chromedriverExecutable","D:\\SeleniumGrid\\chromedriver.exe");

		        // Set android deviceName desired capability. Set it Android Emulator.
		        //capabilities.setCapability("deviceName", "Galaxy S7 API 25");

		        // Set browserName desired capability. It's Android in our case here.
		        capabilities.setCapability("browserName", "chrome");
		        
		        capabilities.setCapability("deviceName", "LC4C5Y683267");
		        // Set android platformVersion desired capability. Set your emulator's android version.
		        //capabilities.setCapability("platformVersion", "7.1");

		        // Set android platformName desired capability. It's Android in our case here.
		        capabilities.setCapability("platformName", "Android");
		        Wdriver = new RemoteWebDriver(new URL("http://127.0.0.1:4723/wd/hub"),capabilities);		        
		        setWebDriver(Wdriver);
				//driver = eventRegister(Wdriver);
			}

		}
		//browserName = browser ;
		return Wdriver;
	}
	public WebDriver getDriverVal(String browser) throws Exception {		
		getDriver(browser);
        return dr.get();
    }
	
	public static WebDriver getBrowserInstance(){
		//return  dr.get();
		return Wdriver;
	}
 
    public void setWebDriver(RemoteWebDriver driver) {
        dr.set(driver);
    }
    
    /*public static String getBrowserName() {
    	
    	return browserName ;    	
    }*/

	/**
	 * Function will register the Event handler.
	 *
	 * @param Wdriver
	 *            the webdriver
	 * @return the event firing web driver
	 */
	/*public static EventFiringWebDriver eventRegister(RemoteWebDriver Wdriver) {
		EventFiringWebDriver driver = new EventFiringWebDriver(Wdriver);
		dubaiProperty.Listeners.TestEventHandler handler = new dubaiProperty.Listeners.TestEventHandler();
		driver.register(handler);
		return driver;
	}*/
	
}