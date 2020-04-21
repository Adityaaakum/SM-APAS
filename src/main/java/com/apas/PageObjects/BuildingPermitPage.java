package com.apas.PageObjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
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

public class BuildingPermitPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);
	Util objUtil;
	ApasGenericPage objApasGenericPage;

	public BuildingPermitPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
		objApasGenericPage = new ApasGenericPage(driver);
	}

	// Locators to create a manual entry.

	@FindBy(xpath = "//nav[@role = 'navigation']//span[contains (text(), 'Building Permits') and not(contains(text(), 'Menu'))]/parent::a")
	public WebElement bldngPrmtTabDetailsPage;

	@FindBy(xpath = "//div[contains(@class, 'headerRegion forceListViewManagerHeader')]//a[@title = 'New']")
	public WebElement newButton;

	@FindBy(xpath = "//legend[text() = 'Select a record type']")
	private WebElement selectRecordTypePopUp;

	@FindBy(xpath = "//span[contains(text(), 'Manual Entry Building Permit')]//parent::div//preceding-sibling::div//input[@type = 'radio']")
	private WebElement manualEntryRadioBtn;

	@FindBy(xpath = "//div[@class = 'inlineFooter']//span[text() = 'Next']//parent::button")
	private WebElement recordTypePopUpNextButton;

	@FindBy(xpath = "//div[@class = 'inlineFooter']//span[text() = 'Cancel']//parent::button")
	private WebElement recordTypePopUpCancelButton;

	@FindBy(xpath = "//a[@title='Select List View']")
	private WebElement listViewsIcon;

	@FindBy(xpath = "//div[@class = 'scroller']//span[contains(@class,'virtualAutocompleteOptionText') and text() = 'All']")
	private WebElement listViewAllOption;

	@FindBy(xpath = "//div[@class = 'scroller']//span[contains(@class,'virtualAutocompleteOptionText') and text() = 'Recently Viewed']")
	private WebElement listViewRecentlyViewedOption;

	@FindBy(xpath = "//div[@class = 'scroller']//span[contains(@class,'virtualAutocompleteOptionText') and text() = 'All E-File Building Permits']")
	private WebElement listViewEFileOptionRecentlyViewedOption;

	@FindBy(xpath = "//table/thead//th//div/a/span[@class = 'slds-truncate']")
	private List<WebElement> recentlyViewedGridColumns;

	@FindBy(xpath = "//tbody/tr//th//a")
	private List<WebElement> buildingPermitNamesFromGrid;

	@FindBy(xpath = "//h2[text() = 'New Building Permit: Manual Entry Building Permit']")
	private WebElement buildingPermitPopUp;

	@FindBy(xpath = "//button[@title='Close this window']")
	public WebElement closeEntryPopUp;

	@FindBy(xpath = "//span[text() = 'Building Permit Number']/parent::label/following-sibling::input")
	private WebElement buildingPermitNumberTxtBox;

	@FindBy(xpath = "//input[@title = 'Search Parcels']")
	private WebElement parcelsSearchBox;

	@FindBy(xpath = "//span[text() = 'Processing Status']/parent::span/following-sibling::div//a[@class = 'select']")
	private WebElement processingStatusDrpDown;

	@FindBy(xpath = "//span[text() = 'Calculated Processing Status']/parent::span/following-sibling::div//a[@class = 'select']")
	private WebElement calculatedProcessingStatusDrpDown;

	@FindBy(xpath = "//input[@title = 'Search County Strat Codes']")
	private WebElement countyStratCodeSearchBox;

	@FindBy(xpath = "//span[text() = 'Estimated Project Value']/parent::label/following-sibling::input")
	private WebElement estimatedProjectValueTxtBox;

	@FindBy(xpath = "//span[text() = 'Issue Date']/parent::label/following-sibling::div/input")
	private WebElement issueDateCalender;

	@FindBy(xpath = "//span[text() = 'Completion Date']/parent::label/following-sibling::div/input")
	private WebElement completionDateCalender;

	@FindBy(xpath = "//span[text() = 'Permit City Code']/parent::span/following-sibling::div//a[@class = 'select']")
	private WebElement permitCityCodeDrpDown;

	@FindBy(xpath = "//span[text() = 'Work Description']/parent::label/following-sibling::input")
	private WebElement workDescriptionTxtBox;

	@FindBy(xpath = "//span[text() = 'Owner Name']/parent::label/following-sibling::input")
	private WebElement ownerNameTxtBox;

	@FindBy(xpath = "//span[text() = 'City APN']/parent::label/following-sibling::input")
	private WebElement cityApnTxtBox;

	@FindBy(xpath = "//span[text() = 'Building Permit Fee']/parent::label/following-sibling::input")
	private WebElement buildingPermitFeeTxtBox;

	@FindBy(xpath = "//span[text() = 'Permit Situs Number']/parent::label/following-sibling::input")
	private WebElement permitSitusNumberTxtBox;

	@FindAll({
			@FindBy(xpath = "//div[@class = 'actionsContainer']//button[@title = 'Save & New']//span[text() = 'Save & New']"),
			@FindBy(xpath = "//div[contains(@class, 'forceModalActionContainer')]//button[@title = 'Save & New']//span[text() = 'Save & New']")
	})
	public WebElement saveAndNewButton;

	@FindBy(xpath = "//div[@class = 'actionsContainer']//button[@title = 'Cancel']//span[text() = 'Cancel']")
	public WebElement cancelButton;

	@FindBy(xpath = "//div[@class = 'actionsContainer']//button[@title = 'Save']//span[text() = 'Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//div[contains(@class, 'forceModalActionContainer')]//button[@title = 'Cancel']//span[text() = 'Cancel']")
	public WebElement cancelBtnEditPopUp;

	@FindBy(xpath = "//div[contains(@class, 'forceModalActionContainer')]//button[@title = 'Save']//span[text() = 'Save']")
	public WebElement saveBtnEditPopUp;

	@FindBy(xpath = "//div[contains(@class, 'form-footer')]//button[@title = 'Cancel']")
	public WebElement cancelBtnDetailsPage;

	@FindBy(xpath = "//div[contains(@class, 'form-footer')]//button[@title = 'Save']")
	public WebElement saveBtnDetailsPage;


	// Locators to validate error message for mandatory fields while creating / editing manual entry.

	@FindBy(xpath = "//li[contains(text(), 'These required fields must be completed:')]")
	private WebElement errorMsgOnTop;

	@FindBy(xpath = "//li[text() = 'Complete this field']")
	private List<WebElement> errorMsgUnderLabels;

	@FindBy(xpath = "//div[@class = 'container']//ul[contains(@class, 'errorsList')]/li")
	private List<WebElement> errorMsgRestrictedWorkDescManualEntryPopUp;

	@FindBy(xpath = "//li[contains(text(), 'Description should not have the following')]")
	private WebElement errorMsgRestrictedWorkDescDetailsPage;

	@FindBy(xpath = "//button[@title = 'Close error dialog']")
	private WebElement closeErrorPopUp;

	// Locators to perform actions on existing building permits from grid.

	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Edit']")
	public WebElement editLinkUnderShowMore;

	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Delete']")
	private WebElement deleteLinkUnderShowMore;

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

	@FindBy(xpath = "//span[text() = 'Delete']")
	private WebElement deleteBtnBuildingPermitInDeletePopUp;

	@FindBy(xpath = "//span[text() = 'Cancel']")
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

	/**
	 * @Description: This method is used to click on New button to open the new building permit.
	 * It also internally handles the additional pop up window to select manual entry
	 * creation radio button when Data Admin is logged into application
	 * @throws IOException
	 */
	public void openNewForm() throws Exception {
		Thread.sleep(1000);
		javascriptClick(waitForElementToBeClickable(newButton));
		if(System.getProperty("isDataAdminLoggedIn") != null && System.getProperty("isDataAdminLoggedIn").equals("true")) {
			Click(waitForElementToBeClickable(manualEntryRadioBtn));
			Click(waitForElementToBeClickable(recordTypePopUpNextButton));
		}
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
		//String buildingPermitNumber = dataMap.get("Permit City Code") + "-" + objUtil.getCurrentDate("yyyMMdd-HHmmss");
		String buildingPermitNumber  = dataMap.get("Building Permit Number");
		System.setProperty("permitNumber", dataMap.get("Building Permit Number"));

		enter(buildingPermitNumberTxtBox, dataMap.get("Building Permit Number"));
		objApasGenericPage.searchAndSelectOptionFromDropDown(parcelsSearchBox, dataMap.get("APN"));
		objApasGenericPage.selectOptionFromDropDown(processingStatusDrpDown, dataMap.get("Processing Status"));
		objApasGenericPage.searchAndSelectOptionFromDropDown(countyStratCodeSearchBox, dataMap.get("County Strat Code Description"));
		enter(estimatedProjectValueTxtBox, dataMap.get("Estimated Project Value"));
		enterDate(issueDateCalender, dataMap.get("Issue Date"));
		enterDate(completionDateCalender, dataMap.get("Completion Date"));
		objApasGenericPage.selectOptionFromDropDown(permitCityCodeDrpDown, dataMap.get("Permit City Code"));
		enter(workDescriptionTxtBox, dataMap.get("Work Description"));

		return buildingPermitNumber;
	}

	/** @Description: This method is used to click 'Save & New' button
	 *	and to close the new entry pop up which appears on button's click.
	 * 	It also internally handles the additional pop up window to select manual entry
	 * 	creation radio button when Data Admin is logged into application
	 *	@throws Exception
	 */
	public boolean saveEntryAndOpenNewAndExit() throws Exception {
		Click(saveAndNewButton);
		if(System.getProperty("isDataAdminLoggedIn") != null && System.getProperty("isDataAdminLoggedIn").equals("true")) {
			Click(waitForElementToBeClickable(manualEntryRadioBtn));
			Click(waitForElementToBeClickable(recordTypePopUpNextButton));
		}

		Thread.sleep(2000);
		boolean isNewManualEntryPopUpDisplayed = waitForElementToBeVisible(20, buildingPermitPopUp);
		if (isNewManualEntryPopUpDisplayed) {
			Click(waitForElementToBeClickable(closeEntryPopUp));
		}
		return isNewManualEntryPopUpDisplayed;
	}

	/** @Description: This method will create a manual building permit entry in APAS
	 */
	public void addAndSaveManualBuildingPermit(Map<String, String> dataMap) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding and saving a new Building Permit manual record");
		openNewForm();
		enterManualEntryData(dataMap);
		saveEntryAndOpenNewAndExit();
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
	 * @param buildingPermitNum: Takes building permit number as argument
	 * @return: Return true / false based on the status of element
	 * @throws Exception
	 */
	public boolean checkManualPermitEntryOnDetailsPage (String entryName) throws Exception {
		String xpathStr = "//h1//slot//lightning-formatted-text[text() = '" + entryName + "']";
		boolean elemStatus = locateElement(xpathStr, 10).isDisplayed();
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
		WebElement modificationsIcon = locateElement(xpathStr, 30);
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
			WebElement closeBtn = locateElement(xpathCloseBtn, 5);
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
		Click(locateElement(xpathStr, 5));

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
		String descValueBeforeEdit = locateElement(xpathDescTxt, 10).getText();

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
		calcProcStatusTxt = locateElement(xpathCalcProcStatus, 10).getText();

		String xpathSitusCityCode = "//span[text() = 'Situs City Code']//parent::div//following-sibling::div//span//lightning-formatted-text";
		situsCityCodeTxt = locateElement(xpathSitusCityCode, 10).getText();

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
	public void openBuildingPermit(String buildingPermitNum) throws IOException, InterruptedException {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening the Building Permit with number : " + buildingPermitNum);
		Click(driver.findElement(By.xpath("//a[@title='" + buildingPermitNum + "']")));
		Thread.sleep(3000);
	}

}
