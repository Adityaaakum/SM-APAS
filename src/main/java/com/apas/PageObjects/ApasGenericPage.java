package com.apas.PageObjects;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ApasGenericPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);

	public ApasGenericPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//one-app-launcher-header/button[@class = 'slds-button']")
	public WebElement appLauncher;
	
	@FindBy(xpath = "//input[contains(@placeholder, 'Search apps and items')]")
	public WebElement appLauncherSearchBox;
	

	/**
	 * Description: This will click on the module name from the drop down
	 */
	public void clickNavOptionFromDropDown(String navOption) throws Exception {
		String xpathStr = "//a[contains(@data-label, '" + navOption + "')]//b[text() = '" + navOption + "']";
		WebElement drpDwnOption = waitForElementToBeClickable(xpathStr);
		drpDwnOption.click();
	}
}
