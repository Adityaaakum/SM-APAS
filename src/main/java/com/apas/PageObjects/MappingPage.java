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

	public WebElement situsFieldInTable;
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
	public String legalDescriptionColumnSecondScreen = "Legal Description";
	public String districtColumnSecondScreen = "District";
	public String apnColumnSecondScreen = "APN";
	public String reasonCodeColumnSecondScreen = "Reason Code";

	public String useCodeColumnSecondScreen = "Use Code";

	public String districtEditTextBoxSecondScreenLabel = "District";
	public String useCodeEditTextBoxSecondScreenLabel = "Use Code";

	public String nextButton = "Next";
	public String generateParcelButton = "Generate Parcel";
	public String parentAPNEditButton = "Edit";
	public String previousButton = "Previous";
	public String retireButton = "Retire Parcel (s)";

	public String saveButton = "Save";

	@FindBy(xpath = "//label[text()='First non-Condo Parcel Number']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconFirstNonCondoParcelNumber;

	@FindBy(xpath = "//label[text()='Legal Description Auto-populate field for Child Parcels']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconLegalDescription;

	@FindBy(xpath = "//label[text()='Situs Auto-populate field for Child Parcels']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconSitus;

	@FindBy(xpath = "//div[contains(@id,'salesforce-lightning-tooltip-bubble')]")
	public WebElement helpIconToolTipBubble;

	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//li |//div[contains(@class,'error') and not(contains(@class,'message-font'))]")
	public WebElement errorMessageFirstScreen;
	
	@FindBy(xpath = "//div[@class='body']//div/following-sibling::c-tem_parcel-process-parent-view//div[contains(@class,'message-font slds-align_absolute-center slds-text-color_success')]")
	public WebElement confirmationMessageOnSecondScreen;
	
	@FindBy(xpath = "//label[text()=\"Assessor's Map\"]//parent::div//div//a")
	public WebElement assessorMapLabel;
	
	
	/**
	 * @Description: This method will fill  the fields in Mapping Action Page mapping action
	 * @param dataMap: A data map which contains data to perform  mapping action
	 * @throws Exception
	 */
	public void fillMappingActionForm(Map<String, String> dataMap) throws Exception {
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
	 * @Description: This method will generate child parcels for  mapping action 
	 *@param dataMap: A data map which contains data to perform one to one mapping
	 * @throws Exception
	 */
	public void generateChildParcelsMappingActions(Map<String, String> dataMap) throws Exception {
		fillMappingActionForm(dataMap);
		Click(getButtonWithText(generateParcelButton));
	}
	/**
	 * @Description: This method will enter value in mapping action page fields and return the error message that would be displayed on page
	 **@param element: ThE element on which validations are needed to be verified
	 * @throws Exception
	 */
	public String getMappingActionsFieldsErrorMessage(Object element,String value) throws Exception {
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
