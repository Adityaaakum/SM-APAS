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
	public String generateParcelButton = "Generate Parcel";
	public String editButton = "Edit";
	public String saveButton = "Save";

	@FindBy(xpath = "//th[@data-label='APN']//lightning-base-formatted-text")
	public WebElement apnFieldInTable;

	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//li")
	public WebElement errorMessageForParentParcels;
	
	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//li |//div[contains(@class,'error') and not(contains(@class,'message-font'))]")
	public WebElement errorMessageFirstScreen;

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
	
	/**
	 * @Description: This method will enter value in mapping action page fields and return the error message that would be displayed on page
	 **@param element: ThE element on which validations are needed to be verified
	 * @throws Exception
	 */
	public String verifyMappingActionsFieldsValidation(Object element,String value) throws Exception {
		enter(element, value);
		
		if(verifyElementVisible(saveButton))
			Click(getButtonWithText(saveButton));

		else
		Click(getButtonWithText(nextButton));
		Thread.sleep(6000);
		if(verifyElementVisible(errorMessageFirstScreen))
				return  getElementText(errorMessageFirstScreen);
		else
			return "No error message is displayed on page";
		
	}
}