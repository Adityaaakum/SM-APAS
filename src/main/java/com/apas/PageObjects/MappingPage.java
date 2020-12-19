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
	public String legalDescriptionEditTextBoxSecondScreenLabel = "Legal Description";

	public String districtEditTextBoxSecondScreenLabel = "District";
	public String useCodeEditTextBoxSecondScreenLabel = "Use Code";

	public String nextButton = "Next";
	public String generateParcelButton = "Generate Parcel";
	public String editButton = "Edit";
	public String saveButton = "Save";

	@FindBy(xpath = "//label[text()='First non-Condo Parcel Number']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconFirstNonCondoParcelNumber;

	@FindBy(xpath = "//label[text()='Legal Description Auto-populate field for Child Parcels']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconLegalDescription;

	@FindBy(xpath = "//label[text()='Situs Auto-populate field for Child Parcels']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconSitus;

	@FindBy(xpath = "//div[contains(@id,'salesforce-lightning-tooltip-bubble')]")
	public WebElement helpIconToolTipBubble;

	@FindBy(xpath = "//td[@data-label='Legal Description']//lightning-base-formatted-text")
	public WebElement legalDescriptionFieldInTable;

	@FindBy(xpath = "//td[@data-label='Legal Description']//button")
	public WebElement legalDescriptionFieldEditIcon;

	@FindBy(xpath = "//td[@data-label='District']//button")
	public WebElement ditrictFieldEditIcon;

	@FindBy(xpath = "//td[@data-label='Use Code']//button")
	public WebElement useCodeFieldEditIcon;

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

	@FindBy(xpath = "//td[@data-label='Neighborhood Code']//lightning-base-formatted-text")
	public WebElement neighborhoodFieldInTable;

	@FindBy(xpath = "//th[@data-label='APN']//lightning-base-formatted-text")
	public WebElement apnFieldInTable;

	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//li")
	public WebElement errorMessageForParentParcels;

	/**
	 * @Description: This method will fill  the fields in Mapping Action Page for one to one mapping action
	 * @param dataMap: A data map which contains data to perform one to one mapping
	 * @throws Exception
	 */
	public void fillOneToOneMappingActionForm(Map<String, String> dataMap) throws Exception {
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
		if (reasonCode != null)enter(reasonCodeTextBoxLabel, reasonCode);
		if (parcelSizeValidation != null)selectOptionFromDropDown(parcelSizeDropDownLabel, parcelSizeValidation);
		if (netLandLoss != null)enter(netLandLossTextBoxLabel, netLandLoss);
		if (netLandGain != null)enter(netLandGainTextBoxLabel, netLandGain);
		if (firstnonCondoParcelNumber != null)
			enter(firstNonCondoTextBoxLabel, firstnonCondoParcelNumber);
		if (legalDescription != null)
			enter(legalDescriptionTextBoxLabel, legalDescription);
		if (situs != null)
			enter(situsTextBoxLabel, situs);
		if (comments != null)
			enter(commentsTextBoxLabel, comments);
		Click(getButtonWithText(nextButton));
	}

	/**
	 * @Description: This method will generate child parcels for one to one mapping action 
	 *@param dataMap: A data map which contains data to perform one to one mapping
	 * @throws Exception
	 */
	public void generateChildParcelsOneToOneMapping(Map<String, String> dataMap) throws Exception {
		fillOneToOneMappingActionForm(dataMap);
		Click(getButtonWithText(generateParcelButton));
	}
}
