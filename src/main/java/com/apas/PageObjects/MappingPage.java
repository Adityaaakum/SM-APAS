package com.apas.PageObjects;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.Util;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.io.IOException;
import java.util.Map;

public class MappingPage extends ApasGenericPage {
	Util objUtil;

	public MappingPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	public String actionDropDownLabel = "Action";
	public String taxesPaidDropDownLabel = "Are taxes fully paid?";
	public String reasonCodeTextBoxLabel = "Reason Code";
	public String parcelSizeDropDownLabel  = "Parcel Size Validation for Parent & Children Needed?";
	public String netLandLossTextBoxLabel = "Net Land Loss";
	public String netLandGainTextBoxLabel = "Net Land Gain";
	public String firstNonCondoTextBoxLabel = "First non-Condo Parcel Number";
	public String legalDescriptionTextBoxLabel = "Legal Description Auto-populate field for Child Parcels";
	public String situsTextBoxLabel = "Situs Auto-populate field for Child Parcels";
	public String commentsTextBoxLabel = "Comments";
	public String parentAPNTextBoxLabel = "Parent APN(s)";

	public String nextButton = "Next";
	public String updateButton = "Update";
	public String editButton = "Edit";
	public String saveButton = "Save";

	@FindBy(xpath = "//label[text()='First non-Condo Parcel Number']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconFirstNonCondoParcelNumber;

	@FindBy(xpath = "//div[contains(@id,'salesforce-lightning-tooltip-bubble')]")
	public WebElement helpIconToolTipBubble;

	@FindBy(xpath = "//td[@data-label='Legal Description']//lightning-base-formatted-text")
	public WebElement legalDescriptionFieldInTable;
	
	@FindBy(xpath = "//td[@data-label='TRA']//lightning-base-formatted-text")
	public WebElement traFieldInTable;
	
	@FindBy(xpath = "//td[@data-label='Situs']//lightning-base-formatted-text")
	public WebElement situsFieldInTable;
	
	@FindBy(xpath = "//td[@data-label='District']//lightning-base-formatted-text")
	public WebElement districtFieldInTable;
	
	@FindBy(xpath = "//td[@data-label='Reason Code']//lightning-base-formatted-text")
	public WebElement reasonCodeFieldInTable;
	
	@FindBy(xpath = "//td[@data-label='Use Code']//lightning-base-formatted-text")
	public WebElement useCodeFieldInTable;
	
	@FindBy(xpath = "//td[@data-label='Neighborhood']//lightning-base-formatted-text")
	public WebElement neighborhoodFieldInTable;


	/**
	 * @Description: This method will fill all the fields in Mapping Action Page for one to one mapping and generate child parcels
	 * @param dataMap: A data map which contains data to perform one to one mapping
	 * @throws Exception
	 */
	public String performOneToOneMappingAction(Map<String, String> dataMap) throws Exception {
		String action = dataMap.get("Action");
		String taxesPaid = dataMap.get("Are taxes fully paid?");
		String reasonCode = dataMap.get("Reason code");
		String parcelSizeValidation = dataMap.get("Parcel Size Validation");
		String netLandLoss = dataMap.get("Net Land Loss");
		String netLandGain = dataMap.get("Net Land Gain");
		String firstnonCondoParcelNumber = dataMap.get("First non-Condo Parcel Number");
		String legalDescription = dataMap.get("Legal Description");
		String situs= dataMap.get("Situs");
		String comments= dataMap.get("Comments");

		selectOptionFromDropDown(actionDropDownLabel, action);
		selectOptionFromDropDown(taxesPaidDropDownLabel, taxesPaid);
		enter(reasonCodeTextBoxLabel, reasonCode);
		selectOptionFromDropDown(parcelSizeDropDownLabel, parcelSizeValidation);
		if (netLandLoss != null)enter(netLandLossTextBoxLabel, netLandLoss);
		if (netLandGain != null)
		enter(netLandGainTextBoxLabel, netLandGain);
		if (firstnonCondoParcelNumber != null)
		enter(firstNonCondoTextBoxLabel, firstnonCondoParcelNumber);
		if (legalDescription != null)
		enter(legalDescriptionTextBoxLabel, legalDescription);
		if (situs != null)
		enter(situsTextBoxLabel, situs);
		if (comments != null)
		enter(commentsTextBoxLabel, comments);
		Click(getButtonWithText(nextButton));

		String apnNumber = "";
		ReportLogger.INFO("APN Created after one to one mapping is  " + apnNumber  );
		Click(getButtonWithText(nextButton));
		//validations for columns or overwrite scenarios 
		
		Click(getButtonWithText(updateButton));

		//validate values of columns 
		return apnNumber;
	}
}
