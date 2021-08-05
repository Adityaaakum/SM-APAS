package com.apas.PageObjects;

import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Utils.SalesforceAPI;

public class TRAPage extends ApasGenericPage {
	ApasGenericPage objApasGenericPage;
	Page objPageObj;
	SalesforceAPI salesforceAPI ;

	public TRAPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericPage = new ApasGenericPage(driver);
		objPageObj=new Page(driver);
		salesforceAPI=new SalesforceAPI();
	}

	public String traNumberTextBox = "TRA Number";
	public String cityDropDown="City";
	public String effectiveStartDateTextBox="Effective Start Date";
	public String effectiveEndDateTextBox="Effective End Date";

	@FindBy(xpath = "//a[text()='View Duplicates']")
	public WebElement viewDuplicatesLinkPageError;
	
	@FindBy(xpath = "//div[contains(@class,'error strength')]//p")
	public WebElement viewDuplicatesLinkPopUpMessage;


	/**
	 * This method will Create TRA  Record
	 * @param TRAReferenceData : TRA Reference record Details
	 * @throws Exception
	 **/
	public void createTRARecord(Map<String, String> TRAReferenceData) throws Exception{

		Click(getButtonWithText(objApasGenericPage.NewButton));
		enter(traNumberTextBox,TRAReferenceData.get("TRA Number"));
		selectOptionFromDropDown(cityDropDown,TRAReferenceData.get("City"));
		enter(effectiveStartDateTextBox,TRAReferenceData.get("Effective Start Date"));
        if(TRAReferenceData.get("Effective End Date")!=null)
		enter(effectiveEndDateTextBox,TRAReferenceData.get("Effective End Date"));
		Click(getButtonWithText("Save"));
	}
}
