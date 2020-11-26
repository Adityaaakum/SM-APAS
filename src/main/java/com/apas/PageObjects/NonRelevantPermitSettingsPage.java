package com.apas.PageObjects;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class NonRelevantPermitSettingsPage extends ApasGenericPage {

	public NonRelevantPermitSettingsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	public String cityCodeDrpDown = "City Code";
	public String statusDrpDown = "Status";

}
