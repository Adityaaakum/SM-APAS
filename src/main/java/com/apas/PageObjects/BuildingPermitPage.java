package com.apas.PageObjects;

import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.config.testdata;
import org.apache.log4j.Logger;

import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Reports.ExtentTestManager;
import com.apas.Utils.Util;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermitPage extends ApasGenericPage {

	Util objUtil;
	ApasGenericPage objApasGenericPage;
	SalesforceAPI salesforceAPI = new SalesforceAPI();

	public BuildingPermitPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
		objApasGenericPage = new ApasGenericPage(driver);
	}

	//Below objects are for Building Permit Module Screen

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div[contains(@class, 'headerRegion forceListViewManagerHeader')]//a[@title = 'New']")
	public WebElement newButton;

	@FindBy(xpath = "//legend[text() = 'Select a record type']")
	private WebElement selectRecordTypePopUp;

	@FindBy(xpath = "//span[contains(text(), 'Manual Entry Building Permit')]//parent::div//preceding-sibling::div//input[@type = 'radio']/..")
	public WebElement manualEntryRadioBtn;

	@FindBy(xpath = "//span[contains(text(), 'E-File Building Permit')]//parent::div//preceding-sibling::div//input[@type = 'radio']/..")
	public WebElement efileRadioButton;

	@FindBy(xpath = "//a[@role='menuitem'][text()='No actions available']")
	public WebElement noActionAvailableOption;

	@FindBy(xpath = "//a[@role='menuitem'][@title='Edit']")
	public WebElement editButtonMenuOption;

	@FindBy(xpath = "//a[@role='menuitem'][@title='Delete']")
	public WebElement deleteButtonMenuOption;

	@FindBy(xpath = "//button[text() = 'Edit']")
	public WebElement editButton;

	@FindBy(xpath = "//button[text() = 'Delete']")
	public WebElement deleteButton;

	@FindBy(xpath = "//div[@role='menu']//li[@class='uiMenuItem']/a[@title = 'Edit']")
	public WebElement editMenuItemButton;

	@FindBy(xpath = "//lightning-icon//span[text()='Show More']")
	public WebElement showMoreButton;

	@FindBy(xpath = "//div[@class = 'inlineFooter']//span[text() = 'Next']//parent::button")
	public WebElement recordTypePopUpNextButton;

	//Below objects are for New Building Permit Pop Up

	@FindBy(xpath = "//h2[contains(text(),'New Building Permit')]")
	public WebElement buildingPermitPopUp;

	@FindBy(xpath = "//button[@title='Close this window']")
	public WebElement closeEntryPopUp;

	@FindBy(xpath = "//span[text() = 'Building Permit Number']/parent::label/following-sibling::input")
	public WebElement buildingPermitNumberTxtBox;

	@FindBy(xpath = "//span[text() = 'Owner Name']/parent::label/following-sibling::input")
	public WebElement OwnerNameTextBox;

	@FindBy(xpath = "//input[@title = 'Search Parcels']")
	public WebElement parcelsSearchBox;

	@FindBy(xpath = "//span[text() = 'Processing Status']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement processingStatusDrpDown;

	@FindBy(xpath = "//input[@title = 'Search County Strat Codes']")
	public WebElement countyStratCodeSearchBox;

	@FindBy(xpath = "//span[text() = 'City Strat Code']/parent::label/following-sibling::input")
	public WebElement cityStratCodeTextBox;

	@FindBy(xpath = "//*[@class='pillContainerListItem'][contains(.,'REPAIR ROOF')]//*[@class='deleteAction']")
	public WebElement deleteRepairRoof;

	@FindBy(xpath = "//span[text() = 'Estimated Project Value']/parent::label/following-sibling::input")
	public WebElement estimatedProjectValueTxtBox;

	@FindBy(xpath = "//span[text() = 'Issue Date']/parent::label/following-sibling::div/input")
	public WebElement issueDateCalender;

	@FindBy(xpath = "//span[text() = 'Completion Date']/parent::label/following-sibling::div/input")
	public WebElement completionDateCalender;

	@FindBy(xpath = "//span[text() = 'Permit City Code']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement permitCityCodeDrpDown;

	@FindBy(xpath = "//div[@class='select-options']/ul/li/..")
	public WebElement permitCityCodeDrpDownOptions;

	@FindBy(xpath = "//span[text() = 'Work Description']/parent::label/following-sibling::input")
	public WebElement workDescriptionTxtBox;

	@FindAll({
			@FindBy(xpath = "//div[@class = 'actionsContainer']//button[@title = 'Save & New']//span[text() = 'Save & New']"),
			@FindBy(xpath = "//div[contains(@class, 'forceModalActionContainer')]//button[@title = 'Save & New']//span[text() = 'Save & New']")
	})
	public WebElement saveAndNewButton;

	@FindBy(xpath = "//div[@role='dialog']//button//*[text() = 'Cancel']")
	public WebElement cancelButton;

	@FindBy(xpath = "//div[@role='dialog']//button//*[text() = 'Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//lightning-primitive-icon/*[@data-key='warning']/../../../div")
	public WebElement warningMessage;

	@FindBy(xpath = "//force-record-layout-row[contains(.,'Warning Message')]//span/img[@alt='Priority Flag']/..")
	public WebElement warningMessageWithPriorityFlag;

	@FindBy(xpath = "//a[text()='View Duplicates']")
	public WebElement viewDuplicateLink;

	@FindBy(xpath = "//div[@data-aura-class='forcePageError']//li")
	public WebElement errorMsgOnTop;

	// Below objects are for View Duplicate Pop Up
	@FindBy(xpath = "//a[contains(text(),'Open This')]")
	public WebElement openBuildingPermitLink;

	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Delete']")
	public WebElement deleteLinkUnderShowMore;

	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Change Owner']")
	private WebElement changeOwnerLinkUnderShowMore;

	@FindBy(xpath = "//ul[contains(@class, 'oneActionsRibbon')]//div[text() = 'Change Owner']")
	private WebElement changeOwnerBtnAboveTbl;

	@FindBy(xpath = "//span[text() = 'Parcel']/parent::label/following-sibling::div//input[@title = 'Search Parcels']")
	private WebElement parcelsSearchBoxInEditPopUp;

	@FindBy(xpath = "//a//span[text() = 'Press Delete to Remove']")
	private WebElement clearDrpDownBtn;

	@FindBy(xpath = "//a[@class='deleteAction']")
	private WebElement closeBtnToRemoveDataFromDrpDown;

	@FindBy(xpath = "//div[@class = 'indicatorContainer forceInlineSpinner']//div[@class = 'forceDotsSpinner']")
	private WebElement spinningPageLoader;

	// Common locators to complete or abort delete action of manual entry.

	@FindBy(xpath = "//div[contains(@class, 'slds-modal__footer')]//span[text() = 'Delete']")
	public WebElement deleteBtnInDeletePopUp;

	@FindBy(xpath = "//div[contains(@class, 'slds-modal__footer')]//span[text() = 'Cancel']")
	private WebElement cancelBtnBuildingPermitInDeletePopUp;

	// Locators to edit or delete manual entry from details page.

	@FindBy(xpath = "//span[text() = 'Building Permit Number']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]")
	public WebElement editIconDetailsPage;

	@FindBy(xpath = "//button[text() = 'Edit']")
	public WebElement editBtnDetailsPage;

	@FindBy(xpath = "//button[text() = 'Delete']")
	private WebElement deleteBtnBuildingPermitFromDetailPage;

	@FindBy(xpath = "//a[@class='deleteAction']")
	private List <WebElement> dropDownCrossIcons;

	@FindBy(xpath = "//div[contains(.,'View Duplicates')]/button[@title='Close this window']")
	public WebElement closeViewDuplicatePopUpButton;
	
	@FindBy(xpath = "//li[contains(text(),'Complete this field')]")
	public List<WebElement> errorMsgUnderLabels;
	
	@FindBy(xpath = "//div[contains(@class, 'forceModalActionContainer')]//button[@title = 'Save']//span[text() = 'Save']")
	public WebElement saveBtnEditPopUp;

	@FindBy(xpath = "//li[contains(text(), 'Description should not have the following')]")
	private WebElement errorMsgRestrictedWorkDescDetailsPage;
	
	@FindBy(xpath = "//div[contains(@class, 'form-footer')]//button[@title = 'Save']")
	public WebElement saveBtnDetailsPage;

	@FindBy(xpath = "//div[contains(@class, 'form-footer')]//button[@title = 'Cancel']")
	public WebElement cancelBtnDetailsPage;
	
	@FindBy(xpath = "//button[@title = 'Close error dialog']")
	private WebElement closeErrorPopUp;
	
	@FindBy(xpath = "//nav[@role = 'navigation']//span[contains (text(), 'Building Permits') and not(contains(text(), 'Menu'))]/parent::a")
	public WebElement bldngPrmtTabDetailsPage;
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Edit']")
	public WebElement editLinkUnderShowMore;

	/**
	 * @Description: This method is used to open the building permit entry form
	 * @param buildingPermitType : value can be "E-File Building Permit" or "Manual Entry Building Permit"
	 */
	public void openNewForm(String buildingPermitType) throws Exception {
		//Select one of the following values for Building Permit Type "E-File Building Permit" or "Manual Entry Building Permit"
		javascriptClick(waitForElementToBeClickable(newButton));
		waitForElementToBeVisible(buildingPermitPopUp,30);
		waitForElementToBeClickable(buildingPermitPopUp,20);

		//Validating if E-file or manual building permit selection radio buttons are visible
		if (verifyElementVisible(manualEntryRadioBtn)){
			if (buildingPermitType.equals("Manual Entry Building Permit"))
				Click(manualEntryRadioBtn);
			else
				Click(efileRadioButton);
			Click(recordTypePopUpNextButton);
		}
		waitForElementToBeVisible(buildingPermitNumberTxtBox,30);
		waitForElementToBeClickable(buildingPermitNumberTxtBox,20);
	}

	/**
	 * @Description: This method is used to open the building permit manual entry form
	 */
	public void openNewForm() throws Exception {
		openNewForm("Manual Entry Building Permit");
////		Thread.sleep(15000);
//		javascriptClick(waitForElementToBeClickable(newButton));
//		waitForElementToBeVisible(buildingPermitPopUp,30);
//		waitForElementToBeClickable(buildingPermitPopUp,20);
//
//		if (verifyElementVisible(manualEntryRadioBtn)){
//			Click(waitForElementToBeClickable(manualEntryRadioBtn));
//			Click(waitForElementToBeClickable(recordTypePopUpNextButton));
//		}
////		if(System.getProperty("isDataAdminLoggedIn") != null && System.getProperty("isDataAdminLoggedIn").equals("true")) {
////			Click(waitForElementToBeClickable(manualEntryRadioBtn));
////			Click(waitForElementToBeClickable(recordTypePopUpNextButton));
////		}
//
//		waitForElementToBeClickable(buildingPermitNumberTxtBox,20);
//
////		Thread.sleep(2000);
	}

	/**
	 * @Description: This method waits and then locates few of elements in building permit entry pop up
	 * to ensure the pop up is ready to be used
	 */
	public void waitForManualEntryPopUpToLoad() {
		waitForElementToBeClickable(countyStratCodeSearchBox);
		waitForElementToBeClickable(permitCityCodeDrpDown);
		waitForElementToBeClickable(issueDateCalender);
	}
	
	/**
	 * @Description: It fills all the required fields in manual entry pop up
	 * @param dataMap: A data map which contains manual entry pop up field names (as keys)
	 * and their values (as values)
	 * @throws Exception
	 */
	public String enterManualEntryData(Map<String, String> dataMap) throws Exception {

		String buildingPermitNumber  = dataMap.get("Building Permit Number");
		enter(buildingPermitNumberTxtBox, dataMap.get("Building Permit Number"));
		objApasGenericPage.searchAndSelectOptionFromDropDown(parcelsSearchBox, dataMap.get("APN"));
		objApasGenericPage.selectOptionFromDropDown(processingStatusDrpDown, dataMap.get("Processing Status"));
		objApasGenericPage.searchAndSelectOptionFromDropDown(countyStratCodeSearchBox, dataMap.get("County Strat Code Description"));
		enter(estimatedProjectValueTxtBox, dataMap.get("Estimated Project Value"));
		enterDate(issueDateCalender, dataMap.get("Issue Date"));
		enterDate(completionDateCalender, dataMap.get("Completion Date"));
		objApasGenericPage.selectOptionFromDropDown(permitCityCodeDrpDown, dataMap.get("Permit City Code"));
		enter(workDescriptionTxtBox, dataMap.get("Work Description"));

		//This text box comes only while adding E-File Building Permit manually
		if (verifyElementVisible(OwnerNameTextBox)) enter(OwnerNameTextBox,dataMap.get("Owner Name"));
		if (verifyElementVisible(cityStratCodeTextBox)) enter(cityStratCodeTextBox,dataMap.get("City Strat Code"));

		return buildingPermitNumber;
	}

	/** @Description: This method is used to click 'Save & New' button
	 *	and to close the new entry pop up which appears on button's click.
	 * 	It also internally handles the additional pop up window to select manual entry
	 * 	creation radio button when Data Admin is logged into application
	 *	@throws Exception
	 */
	public void saveEntryAndOpenNewAndExit() throws Exception {
		Click(saveAndNewButton);
		if(System.getProperty("isDataAdminLoggedIn") != null && System.getProperty("isDataAdminLoggedIn").equals("true")) {
			Click(waitForElementToBeClickable(manualEntryRadioBtn));
			Click(waitForElementToBeClickable(recordTypePopUpNextButton));
		}

		/*Thread.sleep(2000);
		boolean isNewManualEntryPopUpDisplayed = waitForElementToBeVisible(20, buildingPermitPopUp);
		if (isNewManualEntryPopUpDisplayed) {
			Click(waitForElementToBeClickable(closeEntryPopUp));
		}
		return isNewManualEntryPopUpDisplayed;*/

	}
	
	/** @throws InterruptedException 
	 * @Description: This method will Click Save button on the New Building Permit PopUp Window
	 */
	public void clickSaveNewBuildingPermitEntryPopUp() throws IOException, InterruptedException {
		
		boolean isNewManualEntryPopUpDisplayed = waitForElementToBeVisible(20, buildingPermitPopUp);
		if (isNewManualEntryPopUpDisplayed) {
			Click(waitForElementToBeClickable(saveButton));
			Thread.sleep(2000);
		}
		
	}
	
	
	/** @Description: This method will Click close the New Building Permit PopUp Window
	 */
	public void closeNewBuildingPermitEntryPopUp() throws IOException {
		
		boolean isNewManualEntryPopUpDisplayed = waitForElementToBeVisible(20, buildingPermitPopUp);
		if (isNewManualEntryPopUpDisplayed) {
			Click(waitForElementToBeClickable(closeEntryPopUp));
		}
		
	}

	/** @Description: This method will create a manual building permit entry in APAS
	 */
	public void addAndSaveManualBuildingPermit(Map<String, String> dataMap) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding and saving a new Building Permit manual record");
		openNewForm();
		enterManualEntryData(dataMap);
		//saveEntryAndOpenNewAndExit();
		clickSaveNewBuildingPermitEntryPopUp();
	}

		// This method is used to validate & handle the presence of loader on manual entry creation.
	public boolean checkAndHandlePageLoaderOnEntryCreation() {
		boolean isLoaderViaible = waitForElementToBeVisible(10, spinningPageLoader);
		if(isLoaderViaible) {
			driver.navigate().refresh();
		}
		return isLoaderViaible;
	}

	/**
	 * @Description: This method is used in verification of mandatory fields validation messages
	 * @return: It returns a list of 4 elements on below indexes:
	 * 		0th Index: Validation message text displayed on top of manual entry pop
	 * 		for mandatory fields which were left blank
	 * 		1st Index: Validation message text displayed against individual fields which are blank
	 * 		2nd Index: Count of mandatory fields displayed in validation message displayed in header
	 * 		3rd Index: Count of validation message displayed against mandatory fields individually
	 * @throws Exception
	 */
	public List<String> retrieveMandatoryFieldsValidationErrorMsgs() throws Exception {
		List<String> errorsList = new ArrayList<String>();
		String errorMsgOnTopOfPopUpWindow = getElementText(waitForElementToBeVisible(errorMsgOnTop));
		errorsList.add(errorMsgOnTopOfPopUpWindow);

		String individualErrorMsg = getElementText(errorMsgUnderLabels.get(0));
		errorsList.add(individualErrorMsg);

		String fieldsStr = errorMsgOnTopOfPopUpWindow.split(":")[1];
		String[] totalMandatoryFields = fieldsStr.split(",");
		int countOfMandatoryFields = totalMandatoryFields.length;
		errorsList.add(Integer.toString(countOfMandatoryFields));

		int countOfIndividualErrorMsgs = errorMsgUnderLabels.size();
		errorsList.add(Integer.toString(countOfIndividualErrorMsgs));
		return errorsList;
	}

	/**
	 * @description: This method checks whether newly created building permit number
	 * is displayed on details page.
	 * @param entryName: Takes building permit number as argument
	 * @return: Return true / false based on the status of element
	 * @throws Exception
	 */
	public boolean checkManualPermitEntryOnDetailsPage (String entryName) throws Exception {
		String xpathStr = "//h1//slot//lightning-formatted-text[text() = '" + entryName + "']";
		boolean elemStatus = locateElement(xpathStr, 60).isDisplayed();
		return elemStatus;
	}

	/**
	 * @description: This method checks whether newly created building permit number
	 * is displayed on recently viewed grid.
	 * @param buildingPermitNum: Takes building permit number as argument
	 * @return: Return true / false based on the status of element
	 * @throws Exception
	 */
	public boolean checkManualPermitEntryOnGrid(String buildingPermitNum) {
		String xpathStr = "//tbody/tr//th//a[text() = '" + buildingPermitNum + "']";
		boolean elemStatus;
		try {
			elemStatus = waitForElementToBeVisible(xpathStr).isDisplayed();
			elemStatus = true;
		} catch(Exception Ex) {
			elemStatus = false;
		}
		return elemStatus;
	}

	/**
	 * @description: This method clicks on given building permit number
	 * and navigates to the details page
	 * @param buildingPermitNum: Building permit number to open on details page
	 * @throws Exception
	 */
	public void navToDetailsPageOfGivenBuildingPermit(String buildingPermitNum) throws Exception {
		String permitNameXpath = "//tbody/tr//th//a[text() = '" + buildingPermitNum + "']";
		WebElement permitNumber = waitForElementToBeClickable(permitNameXpath);
		Click(permitNumber);
	}

	/**
	 * @description: Clicks on the show more link displayed against the given entry
	 * @param entryDetails: Name of the entry displayed on grid which is to be accessed
	 * @throws Exception
	 */
	public void clickShowMoreLinkOnRecentlyViewedGrid(String entryDetails) throws Exception {		
		Thread.sleep(3000);
		String xpathStr = "//table//tbody/tr//th//a[text() = '"+ entryDetails +"']//parent::span//parent::th//following-sibling::td//a[@role = 'button']";
		WebElement modificationsIcon = locateElement(xpathStr, 60);
		clickAction(modificationsIcon);
	}

	// This method clears out all the pre populated fields of manual entry when opened in edit mode
	public void clearPrePopulatedMandatoryFields() throws Exception {
		waitForElementToBeVisible(buildingPermitNumberTxtBox).sendKeys(Keys.chord(Keys.CONTROL, "a"));
		buildingPermitNumberTxtBox.sendKeys(Keys.BACK_SPACE);

		waitForElementToBeVisible(estimatedProjectValueTxtBox).sendKeys(Keys.chord(Keys.CONTROL, "a"));
		estimatedProjectValueTxtBox.sendKeys(Keys.BACK_SPACE);

		waitForElementToBeVisible(issueDateCalender).sendKeys(Keys.chord(Keys.CONTROL, "a"));
		issueDateCalender.sendKeys(Keys.BACK_SPACE);

		waitForElementToBeVisible(completionDateCalender).sendKeys(Keys.chord(Keys.CONTROL, "a"));
		completionDateCalender.sendKeys(Keys.BACK_SPACE);

		waitForElementToBeVisible(workDescriptionTxtBox).sendKeys(Keys.chord(Keys.CONTROL, "a"));
		workDescriptionTxtBox.sendKeys(Keys.BACK_SPACE);

		objApasGenericPage.selectOptionFromDropDown(waitForElementToBeVisible(processingStatusDrpDown), "--None--");
		objApasGenericPage.selectOptionFromDropDown(waitForElementToBeVisible(permitCityCodeDrpDown), "--None--");

		waitForElementToBeClickable(closeBtnToRemoveDataFromDrpDown).click();
		waitForElementToBeClickable(closeBtnToRemoveDataFromDrpDown).click();
	}

	/**
	 * @description: This method fills some of the mandatory field in manual entry pop up
	 * 		Like: Building Permit Number, Parcel, Processing Status, County Strat Code Description
	 * @param dataMap: A data map which contains manual entry pop up field names (as keys)
	 * and their values (as values)
	 * @throws Exception
	 */
	public void fillSomeMandatoryFields(Map<String, String> dataMap) throws Exception {
		//String buildingPermitNumber = dataMap.get("Permit City Code") + "-" + objUtil.getCurrentDate("yyyMMdd-HHmmss");
		System.setProperty("permitNumber", dataMap.get("Building Permit Number"));

		enter(buildingPermitNumberTxtBox, dataMap.get("Building Permit Number"));
		//enter(buildingPermitNumberTxtBox, buildingPermitNumber);
		objApasGenericPage.searchAndSelectOptionFromDropDown(parcelsSearchBox, dataMap.get("Parcel"));
		objApasGenericPage.selectOptionFromDropDown(processingStatusDrpDown, dataMap.get("Processing Status"));
		objApasGenericPage.searchAndSelectOptionFromDropDown(countyStratCodeSearchBox, dataMap.get("County Strat Code Description"));
	}

	/**
	 * @description: This method fills some of the mandatory field in manual entry pop up
	 * 		Like: Estimated Project Value, Issue Date, Completion Date, Permit City Code, Work Description
	 * @param dataMap: A data map which contains manual entry pop up field names (as keys)
	 * and their values (as values)
	 * @param invalidDescEntries: List of all the restricted work description values
	 * 		Like: temporary signs/banners,public works permits,Tree Removal
	 * @return:
	 * @throws Exception
	 */
	public List<String> fillRemainingMandatoryFields(Map<String, String> dataMap, List<String> invalidDescEntries) throws Exception {
		enter(estimatedProjectValueTxtBox, dataMap.get("Estimated Project Value"));
		enterDate(issueDateCalender, dataMap.get("Issue Date"));
		enterDate(completionDateCalender, dataMap.get("Completion Date"));
		objApasGenericPage.selectOptionFromDropDown(permitCityCodeDrpDown, dataMap.get("Permit City Code"));

		List<String> validationMsgs = new ArrayList<String>();

		for(String currInvalidDesc : invalidDescEntries) {
			waitForElementToBeClickable(workDescriptionTxtBox).clear();
			for(int index = 0; index < currInvalidDesc.length(); index++) {
				waitForElementToBeClickable(workDescriptionTxtBox).sendKeys(Character.toString(currInvalidDesc.charAt(index)));
			}

			Click(waitForElementToBeClickable(saveBtnEditPopUp));
			String errorMsgOnTopOfPopUpWindow = getElementText(errorMsgRestrictedWorkDescDetailsPage);
			validationMsgs.add(errorMsgOnTopOfPopUpWindow);
		}

		waitForElementToBeClickable(workDescriptionTxtBox).clear();
		String validWorkDesc = dataMap.get("Work Description");
		for(int index = 0; index < validWorkDesc.length(); index++) {
			waitForElementToBeClickable(workDescriptionTxtBox).sendKeys(Character.toString(validWorkDesc.charAt(index)));
		}
		return validationMsgs;
	}

	/**
	 * @description: This method fills all the required fields while updating existing building permit
	 * from edit icon on details page.
	 *
	 * @param dataMap: A data map which contains manual entry pop up field names (as keys)
	 * and their values (as values)
	 * @param txtFields: List of textbox fields
	 * @param drpDownFields: List of drop down fields
	 * @param searchAndSelectFields: List of search and select fields
	 *
	 * @return: Returns a list of errors messages displayed on entering restricted work desc value
	 * @throws Exception
	 */
	public void enterUpdatedDataForRequiredFields(Map<String, String> dataMap, List<String> txtFields, List<String> drpDownFields, List<String> searchAndSelectFields) throws Exception {
		//String buildingPermitNumber = dataMap.get("Permit City Code") + "-" + objUtil.getCurrentDate("yyyMMdd-HHmmss");
		System.setProperty("permitNumber", dataMap.get("Building Permit Number"));
		dataMap.put("Building Permit Number", dataMap.get("Building Permit Number"));

		WebElement element;
		for(String fieldName : searchAndSelectFields) {
			String xpathCloseBtn = "//label[text() = '"+ fieldName +"']//following-sibling::div//button[@title = 'Clear Selection']";
			WebElement closeBtn = locateElement(xpathCloseBtn, 30);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", closeBtn);

			String xpathStrInputFields = "//label[text() = '"+ fieldName + "']//parent::label//following-sibling::div//input[@type = 'text']";
			element = waitForElementToBeClickable(xpathStrInputFields);
			enter(element, dataMap.get(fieldName));
			element.sendKeys(Keys.ARROW_DOWN);
			element.sendKeys(Keys.ENTER);
		}

		for(String fieldName : drpDownFields) {
			String xpathStrInputFields = "//label[text() = '"+ fieldName +"']//following-sibling::div//input[@type = 'text']";
			element = waitForElementToBeClickable(xpathStrInputFields);
			scrollToElement(element);
			javascriptClick(element);
			String dropDownOptionXpath = "//div[@role='listbox' and contains(@id, 'dropdown-element')]//lightning-base-combobox-item[@data-value = '"+ dataMap.get(fieldName) +"']";
			WebElement elem = waitForElementToBeClickable(dropDownOptionXpath);
			Click(elem);
		}

		for(String fieldName : txtFields) {
			String xpathStrInputFields = "//label[text() = '"+ fieldName +"']//parent::label//following-sibling::div//input[@type = 'text']";
			element = waitForElementToBeClickable(xpathStrInputFields);
			enter(element, dataMap.get(fieldName));
		}
	}

	/**
	 * @description: This method is used to validate the work description field individually
	 * by passing on the restricted work description values
	 * @param invalidDescValues: List of all the restricted work description values
	 * 		Like: temporary signs/banners,public works permits,Tree Removal
	 * @return: Returns a list of errors messages displayed on entering restricted work desc value
	 * @throws Exception
	 */
	public List<String> editWorkDescIndividuallyOnDetailsPage(List<String> invalidDescValues) throws Exception {
		List<String> validationMessage = new ArrayList<String>();
		String xpathStr = "//span[text() = 'Work Description']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]";
		Click(locateElement(xpathStr, 30));

		for (String invalidDescValue : invalidDescValues) {
			xpathStr = "//label[text() = 'Work Description']//following-sibling::div//input[contains(@name, 'Work')]";
			WebElement descTxtBox = waitForElementToBeClickable(xpathStr);
			enter(descTxtBox, invalidDescValue);
			Click(waitForElementToBeClickable(saveBtnDetailsPage));

			String errorMsgOnTopOfPopUpWindow = getElementText(errorMsgRestrictedWorkDescDetailsPage);
			validationMessage.add(errorMsgOnTopOfPopUpWindow);
			WebElement errorPopCloseBtn = waitForElementToBeClickable(closeErrorPopUp);
			Click(waitForElementToBeClickable(errorPopCloseBtn));
		}
		Click(cancelBtnDetailsPage);
		return validationMessage;
	}

	/**
	 * @description: This method is used to validate the issue date field individually
	 * by entering new value and not saving it
	 * @return: Returns a list of values before and after edit
	 * @throws Exception
	 */
	public List<String> editAndCancelIssueDateDetailsPage() throws Exception {
		String xpathDescTxt = "//span[text() = 'Issue Date']/parent::div/following-sibling::div//lightning-formatted-text[@data-output-element-id = 'output-field']";
		String descValueBeforeEdit = locateElement(xpathDescTxt, 60).getText();

		List<String> validationMessage = new ArrayList<String>();
		String xpathStr = "//span[text() = 'Issue Date']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]";
		Click(locateElement(xpathStr, 5));

		String xpathStrInputFields = "//label[text() = 'Issue Date']//parent::label//following-sibling::div//input[@type = 'text']";
		WebElement element = waitForElementToBeClickable(xpathStrInputFields);
		enter(element, "01/01/2025");

		Click(cancelBtnDetailsPage);

		String descValueAfterEdit = locateElement(xpathDescTxt, 10).getText();
		validationMessage.add(descValueBeforeEdit);
		validationMessage.add(descValueAfterEdit);
		return validationMessage;
	}

	/**
	 * @description: This method is used to fetch the data / values of calculated processing status
	   and situs city code value that are expected to be auto displayed on saving the manual enty
	 * @return: Returns a list of values that are auto-displayed
	 * @throws Exception
	 */
	public Map<String, String> getSitusCodeAndCalProcStatusFromDetailsPage() throws Exception {
		Map<String, String> autoPopulatedValues = new HashMap<String, String>();
		String calcProcStatusTxt = "";
		String situsCityCodeTxt = "";
		String xpathCalcProcStatus = "//span[text() = 'Calculated Processing Status']/parent::div/following-sibling::div//lightning-formatted-text[@data-output-element-id = 'output-field']";
		calcProcStatusTxt = locateElement(xpathCalcProcStatus, 30).getText();

		String xpathSitusCityCode = "//span[text() = 'Situs City Code']//parent::div//following-sibling::div//span//lightning-formatted-text";
		situsCityCodeTxt = locateElement(xpathSitusCityCode, 30).getText();

		autoPopulatedValues.put("Situs City Code", situsCityCodeTxt);
		autoPopulatedValues.put("Calculated Processing Status", calcProcStatusTxt);
		return autoPopulatedValues;
	}

	/**
	 * @description: This method navigates to details page and retrieves the existing values of given fields
	 * @param listOfTxtAndDrpDowns
	 * @param listOfSearchDrpDowns
	 * @return: Returns a map containing fields names (as Keys) and their values (as values of keys)
	 * @throws Exception
	 */
	public Map<String, String> getExistingManualEntryData(List<String> listOfTxtAndDrpDowns, List<String> listOfSearchDrpDowns) throws Exception {
		Map<String, String> dataMap = new HashMap<String, String>();
		String xpathStr = null;
		for(String key : listOfTxtAndDrpDowns) {
			xpathStr = "//span[text() = '"+ key +"']/parent::div/following-sibling::div//lightning-formatted-text[@data-output-element-id = 'output-field']";
			String value = locateElement(xpathStr, 30).getText();
			dataMap.put(key, value);
		}

		for(String key : listOfSearchDrpDowns) {
			xpathStr = "//span[text() = '"+ key +"']/parent::div/following-sibling::div//a[@class = 'slds-grow flex-wrap-ie11']";
			String value = locateElement(xpathStr, 30).getText();
			dataMap.put(key, value);
		}
		return dataMap;
	}

	/**
	 * @Description: This method is to handle fields like Permit City Code or Processing Status
	 * by clicking the web element and then selecting the given value from drop down
	 * @param element: WebElement for required field
	 * @param value: Like 'Process' or 'No Process' for Processing Status field etc.
	 * @throws Exception
	 */
	public void selectFromDropDown(WebElement element, String value) throws Exception {
		Click(element);
		String xpathStr = "//div[contains(@class, 'left uiMenuList--short visible positioned')]//a[text() = '" + value + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 30);
		drpDwnOption.click();
	}

	/**
	 * @Description: This methods particularly handles the issue date and completion date fields
	 * @param element: WebElement for required field
	 * @param date
	 * @throws Exception
	 */
	public void enterDate(WebElement element, String date) throws Exception {
		Click(element);
		objApasGenericPage.selectDateFromDatePicker(date);
	}

	/**
	 * @description: This method will open the Building Permit passed in the argument
	 * @param buildingPermitNum: Takes building permit number as argument
	 */
	public void openBuildingPermit(String buildingPermitNum) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening the Building Permit with number : " + buildingPermitNum);
		String xpath = "//a[@title='" + buildingPermitNum + "']";
		waitUntilElementIsPresent(xpath,15);
		waitForElementToBeClickable(driver.findElement(By.xpath(xpath)),10);
		Click(driver.findElement(By.xpath(xpath)));
		Thread.sleep(3000);
	}

	/**
	 * @description: This method will return the error message appeared against the filed name passed in the parameter
	 * @param fieldName: field name for which error message needs to be fetched
	 */
	public String getIndividualFieldErrorMessage(String fieldName) throws Exception {
		String xpath = "//div[@role='listitem']//span[text()='" + fieldName + "']/../../../ul[contains(@data-aura-class,'uiInputDefaultError')]";
		waitUntilElementIsPresent(xpath,20);
		return getElementText(driver.findElement(By.xpath(xpath)));
	}

	/**
	 * @description: This method will return the filed value from the view duplicate screen
	 * @param fieldName: field name for which error message needs to be fetched
	 */
	public String getFieldValueFromViewDuplicateScreen(String fieldName) {
		return getElementText(driver.findElement(By.xpath("//*[@class='tableRowGroup'][contains(.,'" + fieldName + "')]//span")));
	}

	/**
	 * @description: This method will return the default data to create manual building permit
	 * @return hashMapBuildingPermitData : Test data to create manual building permit
	 */
	public Map<String, String> getBuildingPermitManualCreationTestData() {

		//Fetch the APN to be used to create building permit
		String query ="SELECT Name FROM Parcel__c where status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String activeAPN = response.get("Name").get(0);
		ReportLogger.INFO("Active APN fetched through Salesforce API : " + activeAPN);

		String manualEntryData = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_MANUAL + "\\BuildingPermitManualCreationData.json";
		Map<String, String> manualBuildingPermitMap = objUtil.generateMapFromJsonFile(manualEntryData, "BuildingPermitManualCreationData");
		String buildingPermitNumber = manualBuildingPermitMap.get("Permit City Code") + "-" + objUtil.getCurrentDate("yyyMMdd-HHmmss");
		manualBuildingPermitMap.put("Building Permit Number",buildingPermitNumber);
		manualBuildingPermitMap.put("APN",activeAPN);

		return  manualBuildingPermitMap;
	}

	public void enterEstimatedProjectValue(String value) throws IOException, InterruptedException {
		waitForElementToBeClickable(estimatedProjectValueTxtBox,15);
		estimatedProjectValueTxtBox.clear();
		estimatedProjectValueTxtBox.sendKeys(Keys.chord(Keys.CONTROL,"a", Keys.DELETE));
		Thread.sleep(3000);
		estimatedProjectValueTxtBox.sendKeys(value);
	}

}
