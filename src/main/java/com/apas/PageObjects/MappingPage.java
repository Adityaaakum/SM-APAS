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
	public String numberOfChildNonCondoTextBoxLabel = "Number of Child Non-Condo Parcels";
	public String numberOfChildCondoTextBoxLabel = "Number of Child Condo Parcels";
	public String nextButton = "Next";
	public String generateParcelButton = "Generate Parcel";
	public String parentAPNEditButton = "Edit";
	public String previousButton = "Previous";
	public String retireButton = "Retire Parcel (s)";
	public String assessorMapLabel = "Assessor's Map";
	public String taxField = "//label[text()='Are taxes fully paid?']";
	public String saveButton = "Save";
	public String firstCondoTextBoxLabel = "First Condo Parcel Number";
	public String splitParcelButton = "Split Parcel";
	public String situsCityDescriptionLabel = "Situs City Description";
	public String situsCityCodeLabel = "Situs City Code";
	public String situsCityNameLabel = "Situs City Name";
	public String directionLabel = "Direction";
	public String situsNumberLabel = "Situs Number";
	public String situsStreetNameLabel = "Situs Street Name";
	public String situsTypeLabel = "Situs Type";
	public String situsUnitNumberLabel = "Situs Unit Number";
	public String closeButton = "Close";

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

	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//*[@data-label='Legal Description']")
	public WebElement legalDescriptionFieldSecondScreen;

	@FindBy(xpath = "//div[@class='body']//div/following-sibling::c-tem_parcel-process-parent-view//div[contains(@class,'message-font slds-align_absolute-center slds-text-color_success')]")
	public WebElement confirmationMessageOnSecondScreen;
	
	@FindBy(xpath = "//header[@class='slds-modal__header']//h2[text()='Edit Situs']")
	public WebElement editSitusLabelSitusModal;
	
	@FindBy(xpath = "//div[@class='slds-card__header slds-grid']//span[text()='Situs Information']")
	public WebElement situsInformationLabelSitusModal;

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
		String comments= dataMap.get("Comments");
		String numberOfChildNonCondoParcels= dataMap.get("Number of Child Non-Condo Parcels");
		String numberOfChildCondoParcels= dataMap.get("Number of Child Condo Parcels");
		String firstCondoParcelNumber= dataMap.get("First Condo Parcel Number");

		selectOptionFromDropDown(actionDropDownLabel, action);
		selectOptionFromDropDown(taxesPaidDropDownLabel, taxesPaid);
		if (reasonCode != null)enter(reasonCodeTextBoxLabel, reasonCode);
		if (parcelSizeValidation != null)selectOptionFromDropDown(parcelSizeDropDownLabel, parcelSizeValidation);
		if (netLandLoss != null)enter(netLandLossTextBoxLabel, netLandLoss);
		if (netLandGain != null)enter(netLandGainTextBoxLabel, netLandGain);
		if (numberOfChildNonCondoParcels != null)
			enter(numberOfChildNonCondoTextBoxLabel, numberOfChildNonCondoParcels);
		if (firstnonCondoParcelNumber != null)
			enter(firstNonCondoTextBoxLabel, firstnonCondoParcelNumber);
		if (numberOfChildCondoParcels != null)
			enter(numberOfChildCondoTextBoxLabel, numberOfChildCondoParcels);
		if (firstCondoParcelNumber != null)
			enter(firstCondoTextBoxLabel, firstCondoParcelNumber);
		if (legalDescription != null)
			enter(legalDescriptionTextBoxLabel, legalDescription);
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
	 * Description: this method is to get the confirmation message after mapping action is completed
	 * 	 
	 * @throws: Exception
	 */
	public String confirmationMsgOnSecondScreen() throws Exception {
		return getElementText(waitForElementToBeClickable(20, confirmationMessageOnSecondScreen));
	}

	/**
	 * @Description: This method will create situs for child parcel from situs modal window from first screen	
	 * @param dataMap: A data map which contains data to create situs
	 * @throws Exception
	 */
	public void editSitusModalWindowFirstScreen(Map<String, String> dataMap) throws Exception {
		
		String situsCityDescription = dataMap.get("Situs City Description");
		String situsCityCode = dataMap.get("Situs City Code");
		String situsCityName = dataMap.get("Situs City Name");
		String direction = dataMap.get("Direction");
		String situsNumber = dataMap.get("Situs Number");
		String situsStreetName = dataMap.get("Situs Street Name");
		String situsType = dataMap.get("Situs Type");
		String situsUnitNumber = dataMap.get("Situs Unit Number");
		
		if (situsCityDescription != null) selectOptionFromDropDown(situsCityDescriptionLabel, situsCityDescription);
		if (situsCityCode != null) selectOptionFromDropDown(situsCityCodeLabel, situsCityCode);
		if (situsCityName != null) enter(situsCityNameLabel, situsCityName);
		if (direction != null)enter(directionLabel, direction);
		if (situsNumber != null) enter(situsNumberLabel, situsNumber);
		if (situsStreetName != null) enter(situsStreetNameLabel, situsStreetName);
		if (situsType != null) selectOptionFromDropDown(situsTypeLabel, situsType);
		if (situsUnitNumber != null) enter(situsUnitNumberLabel, situsUnitNumber);
		Click(getButtonWithText(saveButton));
	}

}
