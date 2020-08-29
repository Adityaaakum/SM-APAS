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

	@FindBy(xpath = "//div[contains(@class,'visible positioned')]//a[@role='menuitem'][text()='No actions available']")
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

	/* Description: This method will Click Save button and return the text of the confirmation message appeared
	 */
	public String saveBuildingPermit() throws Exception {
		Click(saveButton);
		waitForElementToBeClickable(successAlert,20);
		String messageOnAlert = getElementText(successAlert);
		waitForElementToDisappear(successAlert,10);
		return messageOnAlert;
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
