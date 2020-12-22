package com.apas.PageObjects;

import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Utils.Util;

public class MappingPage extends ApasGenericPage {
	Util objUtil;

	public MappingPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	public String actionDropDownLabel = "Action";
	public String reasonCodeTextBoxLabel = "Reason Code";
	public String firstNonCondoTextBoxLabel = "First non-Condo Parcel Number";
	public String commentsTextBoxLabel = "Comments";
	public String parentAPNTextBoxLabel = "Parent APN(s)";

	public String nextButton = "Next";
	public String editButton = "Edit";
	public String saveButton = "Save";

	@FindBy(xpath = "//th[@data-label='APN']//lightning-base-formatted-text")
	public WebElement apnFieldInTable;

	public void remapActionForm(Map<String, String> dataMap) throws Exception {
		String action = dataMap.get("Action");
		String reasonCode = dataMap.get("Reason code");
		String firstnonCondoParcelNumber = dataMap.get("First non-Condo Parcel Number");
		String comments= dataMap.get("Comments");

		selectOptionFromDropDown(actionDropDownLabel, action);
		if (reasonCode != null)enter(reasonCodeTextBoxLabel, reasonCode);
		if (firstnonCondoParcelNumber != null)
			enter(firstNonCondoTextBoxLabel, firstnonCondoParcelNumber);
		if (comments != null)
			enter(commentsTextBoxLabel, comments);
		Click(getButtonWithText(nextButton));
	}
}