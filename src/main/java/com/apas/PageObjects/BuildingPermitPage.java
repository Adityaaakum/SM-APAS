package com.apas.PageObjects;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import com.apas.Utils.Util;

public class BuildingPermitPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);
	Util objUtil;
	
	public BuildingPermitPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}
	
	// Locators to create a manual entry.
	
	@FindBy(xpath = "//span[contains (text(), 'Recently Viewed') and not(contains(text(), 'Menu'))]/parent::a")
	private WebElement buildingPermitsTab;
	
	@FindBy(xpath = "//a[@title = 'New']")
	private WebElement newButton;
	
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
	private WebElement closeBuildingPermitManualEntryPopUp;
	
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
	private WebElement saveAndNewButton;
	
	@FindBy(xpath = "//div[@class = 'actionsContainer']//button[@title = 'Cancel']//span[text() = 'Cancel']")
	private WebElement cancelButton;
	
	@FindBy(xpath = "//div[@class = 'actionsContainer']//button[@title = 'Save']//span[text() = 'Save']")
	private WebElement saveButton;

	@FindBy(xpath = "//div[contains(@class, 'forceModalActionContainer')]//button[@title = 'Cancel']//span[text() = 'Cancel']")
	private WebElement cancelBtnEditPopUp;
	
	@FindBy(xpath = "//div[contains(@class, 'forceModalActionContainer')]//button[@title = 'Save']//span[text() = 'Save']")
	private WebElement saveBtnEditPopUp;
	
	@FindBy(xpath = "//div[contains(@class, 'form-footer')]//button[@title = 'Cancel']")
	private WebElement cancelBtnDetailsPage;
	
	@FindBy(xpath = "//div[contains(@class, 'form-footer')]//button[@title = 'Save']")
	private WebElement saveBtnDetailsPage;
	
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
	private WebElement editLinkUnderShowMore;
	
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
	
	@FindBy(xpath = "//button[@title = 'Clear Selection']")
	private WebElement clearSelectionButton;
	
	@FindBy(xpath = "//div[@class = 'indicatorContainer forceInlineSpinner']//div[@class = 'forceDotsSpinner']")
	private WebElement spinningPageLoader;
	
	// Common locators to complete or abort delete action of manual entry.
	
	@FindBy(xpath = "//span[text() = 'Delete']")
	private WebElement deleteBtnBuildingPermitInDeletePopUp;
	
	@FindBy(xpath = "//span[text() = 'Cancel']")
	private WebElement cancelBtnBuildingPermitInDeletePopUp;
	
	// Locators to edit or delete manual entry from details page.
	
	@FindBy(xpath = "//button[text() = 'Edit']")
	private WebElement editBtnbuildingPermitDetailPage;

	@FindBy(xpath = "//button[text() = 'Delete']")
	private WebElement deleteBtnBuildingPermitFromDetailPage;
	
	@FindBy(xpath = "//a[@class='deleteAction']")
	private List <WebElement> dropDownCrossIcons;
		
	/*	Sikander Bhambhu:
	 *	Next 7 locators are for handling date picker
	 *	These would be moved to common package/class
	 * */
	
	@FindBy(xpath = "//div[contains(@class, 'visible DESKTOP uiDatePicker')]")
	private WebElement datePicker;

	@FindBy(xpath = "//select[contains(@class, 'select picklist')]")
	private WebElement yearDropDown;
	
	@FindBy(xpath = "//a[@class='navLink prevMonth']")
	private WebElement prevMnth;
	
	@FindBy(xpath = "//a[@class='navLink nextMonth']")
	private WebElement nextMnth;
	
	@FindBy(xpath = "//span[(contains(@class, 'uiDayInMonthCell')) and (not (contains(@class, 'nextMonth '))) and (not (contains(@class, 'prevMonth ')))]")
	private List <WebElement> dates;

	@FindBy(xpath = "//h2[@class = 'monthYear']")
	private WebElement visibleMonth;

	@FindBy(xpath = "//button[text() = 'Today']")
	private WebElement currentDate;

	public void openManualEntryForm() throws IOException {
		Click(waitForElementToBeClickable(newButton));
	}
	
	public void waitForManualEntryFormToLoad() {
		waitForElementToBeClickable(countyStratCodeSearchBox);
		waitForElementToBeClickable(permitCityCodeDrpDown);
		waitForElementToBeClickable(issueDateCalender);
	}
	
	public void enterManualEntryData(Map<String, String> dataMap) throws Exception {
		String buildingPermitNumber = dataMap.get("Permit City Code") + "-" + objUtil.getCurrentDate("yyyMMdd-HHmmss");
		System.setProperty("permitName", buildingPermitNumber);
		
		enterDataInTextBox(buildingPermitNumberTxtBox, buildingPermitNumber);
		searchAndSelectFromDropDown(parcelsSearchBox, dataMap.get("Parcel"));
		selectFromDropDown(processingStatusDrpDown, dataMap.get("Processing Status"));
		//selectFromDropDown(calculatedProcessingStatusDrpDown, dataMap.get("Calculated Processing Status"));
		searchAndSelectFromDropDown(countyStratCodeSearchBox, dataMap.get("County Strat Code Description"));
		enterDataInTextBox(estimatedProjectValueTxtBox, dataMap.get("Estimated Project Value"));
		enterDate(issueDateCalender, dataMap.get("Issue Date"));
		enterDate(completionDateCalender, dataMap.get("Completion Date"));
		selectFromDropDown(permitCityCodeDrpDown, dataMap.get("Permit City Code"));		
		enterDataInTextBox(workDescriptionTxtBox, dataMap.get("Work Description"));
	}
	
	public void saveManualEntry() throws Exception {
		Click(saveButton);
	}
	
	public boolean checkAndHandlePageLoaderOnEntryCreation() {
		boolean isLoaderViaible = waitForElementToBeVisible(10, spinningPageLoader);
		if(isLoaderViaible) {
			driver.navigate().refresh();
		}
		return isLoaderViaible;
	}
	
	public boolean saveManualEntryAndExit() throws Exception {
		Click(saveAndNewButton);
		Thread.sleep(2000);
		boolean isNewManualEntryPopUpDisplayed = waitForElementToBeVisible(20, buildingPermitPopUp);
		if (isNewManualEntryPopUpDisplayed) {
			Click(waitForElementToBeClickable(closeBuildingPermitManualEntryPopUp));
		}
		return isNewManualEntryPopUpDisplayed;
	}
	
	public void abortManualEntry() throws Exception {
		Click(cancelButton);
	}
	
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
	
	public boolean checkBuildingPermitOnDetailsPage () throws Exception {
		String xpathStr = "//lightning-formatted-text[text() = '" + System.getProperty("permitName") + "']";
		boolean elemStatus = waitForElementToBeVisible(xpathStr).isDisplayed();
		return elemStatus;
	}
	
	public boolean checkBuildingPermitOnGrid() {
		String xpathStr = "//tbody/tr//th//a[text() = '" + System.getProperty("permitName") + "']";
		boolean elemStatus;
		try {
			elemStatus = waitForElementToBeVisible(xpathStr).isDisplayed();
			elemStatus = true;
		} catch(Exception Ex) {
			elemStatus = false;
		}
		return elemStatus;
	}
		
	public void editPermitEntryOnRecentlyViewedGrid(boolean flagToUseRecentlyCreadtedEntry) throws Exception {
		String currentURL = driver.getCurrentUrl();
		if(currentURL.contains("Building_Permit__c") && currentURL.contains("/view")) {
			Click(waitForElementToBeClickable(buildingPermitsTab));
		}
			
		String buildingPermitToEdit = null;
		if(flagToUseRecentlyCreadtedEntry) {
			buildingPermitToEdit = System.getProperty("permitName");
		} else {
			List<WebElement> permitNames = waitForAllElementsToBeVisible(buildingPermitNamesFromGrid); 
			buildingPermitToEdit = permitNames.get(0).getText();
		}
		
		String xpathStr = "//tbody/tr//th//a[text() = '"+ buildingPermitToEdit +"']/parent::span/parent::th/following-sibling::td//a[@role = 'button']";
		WebElement modificationsIcon = waitForElementToBeClickable(xpathStr);
		Click(modificationsIcon);
		clickAction(waitForElementToBeVisible(editLinkUnderShowMore));
	
		System.setProperty("buildingPermitToEdit", buildingPermitToEdit);
	}
	
	public void editPermitEntryOnDetailsPage(boolean flagToUseRecentlyCreadtedEntry) throws Exception {
		String currentURL = driver.getCurrentUrl();
		String buildingPermitToEdit = null;
		if(flagToUseRecentlyCreadtedEntry) {
			if(currentURL.contains("Building_Permit__c") && currentURL.contains("/view")) {
				buildingPermitToEdit = System.getProperty("permitName");
				Click(editBtnbuildingPermitDetailPage);			
			} else {
				buildingPermitToEdit = System.getProperty("permitName");
				navToDetailsPageAndClickEditBtn(buildingPermitToEdit);
				
			}
		} else {
			if(currentURL.contains("Building_Permit__c") && currentURL.contains("/view")) {
				Click(waitForElementToBeVisible(buildingPermitsTab));	
			}
						
			List<WebElement> permitNames = waitForAllElementsToBeVisible(buildingPermitNamesFromGrid); 
			buildingPermitToEdit = permitNames.get(0).getText();
			navToDetailsPageAndClickEditBtn(buildingPermitToEdit);
		}
		System.setProperty("buildingPermitToEdit", buildingPermitToEdit);
	}

	public void editManualEntryFieldsUsingEditIcon(Map<String, String> dataMap, List<String> fieldsToEditUsingEditIcon) throws Exception {
		dataMap.put("Building Permit Number", System.getProperty("permitName"));
		System.out.println("Data Map: " + dataMap);
		
		String xpathStrEditIcon = "//span[text() = 'Building Permit Number']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]";
		Click(waitForElementToBeClickable(xpathStrEditIcon));
		for(String key : fieldsToEditUsingEditIcon) {
			String xpathStrInputFields = "//label[text() = '"+ key + "']//following-sibling::div//input[@type = 'text']";
			WebElement element = waitForElementToBeClickable(xpathStrInputFields);
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
			
			System.out.println("Current Key Is: " + key);
			if(key.equalsIgnoreCase("Parcel") || key.equalsIgnoreCase("County Strat Code Description")) {
				waitForElementToBeClickable(clearSelectionButton).click();
				Click(element);
				enterDataInTextBox(element, dataMap.get(key));
				String dropDownOptionXpath = "//div[@role='listbox' and contains(@id, 'dropdown-element')]//lightning-base-combobox-item//span[text() = '"+ dataMap.get(key) +"']";
				WebElement elem = waitForElementToBeClickable(dropDownOptionXpath);
				Click(elem);	
			} else if (key.equalsIgnoreCase("Permit City Code") || key.equalsIgnoreCase("Processing Status") || key.equalsIgnoreCase("Calculated Processing Status")) {
				Click(element);
				String dropDownOptionXpath = "//div[@role='listbox' and contains(@id, 'dropdown-element')]//lightning-base-combobox-item[@data-value = '"+ dataMap.get(key) +"']";
				WebElement elem = waitForElementToBeClickable(dropDownOptionXpath);
				Click(elem);	
			} else {
				enterDataInTextBox(element, dataMap.get(key));	
			}
		}
		Click(waitForElementToBeClickable(saveBtnDetailsPage));
	}
	
	public List<String> editWorkDescIndividuallyOnDetailsPage(List<String> invalidDescValues) throws Exception {
		List<String> validationMessage = new ArrayList<String>();
		String xpathStr = "//span[text() = 'Work Description']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]";
		Click(waitForElementToBeClickable(xpathStr));
		Thread.sleep(2000);
		for (String invalidDescValue : invalidDescValues) {
			xpathStr = "//label[text() = 'Work Description']//following-sibling::div//input[contains(@name, 'Work')]";
			WebElement descTxtBox = waitForElementToBeClickable(xpathStr);
			enterDataInTextBox(descTxtBox, invalidDescValue);
			Click(waitForElementToBeClickable(saveBtnDetailsPage));
			
			String errorMsgOnTopOfPopUpWindow = getElementText(errorMsgRestrictedWorkDescDetailsPage);
			validationMessage.add(errorMsgOnTopOfPopUpWindow);
			WebElement errorPopCloseBtn = waitForElementToBeClickable(closeErrorPopUp);
			Click(waitForElementToBeClickable(errorPopCloseBtn));
		}
		Click(waitForElementToBeClickable(cancelBtnDetailsPage));
		return validationMessage;
	}
	
	public List<String> editPermitEntryOnDetailsPageUsingEditIcon(List<String> invalidDescValues) throws Exception {
		List<String> validationMessage = new ArrayList<String>();
		String xpathStr = "//span[text() = 'Work Description']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]";
		Click(waitForElementToBeClickable(xpathStr));
		Thread.sleep(2000);
		for (String invalidDescValue : invalidDescValues) {
			xpathStr = "//label[text() = 'Work Description']//following-sibling::div//input[contains(@name, 'Work')]";
			WebElement descTxtBox = waitForElementToBeClickable(xpathStr);
			enterDataInTextBox(descTxtBox, invalidDescValue);
			Click(waitForElementToBeClickable(saveBtnDetailsPage));
			
			String errorMsgOnTopOfPopUpWindow = getElementText(errorMsgRestrictedWorkDescDetailsPage);
			validationMessage.add(errorMsgOnTopOfPopUpWindow);
			WebElement errorPopCloseBtn = waitForElementToBeClickable(closeErrorPopUp);
			Click(waitForElementToBeClickable(errorPopCloseBtn));
		}
		Click(waitForElementToBeClickable(cancelBtnDetailsPage));
		return validationMessage;
	}
	
	
	public void navToDetailsPageAndClickEditBtn(String buildingPermitToEdit) throws Exception {
		String permitNameXpath = "//tbody/tr//th//a[text() = '" + buildingPermitToEdit + "']";
		Click(waitForElementToBeClickable(permitNameXpath));
		Click(editBtnbuildingPermitDetailPage);	
	}
	
	
	public void clearPrePopulatedMandatoryFieldsAndSave() throws Exception {
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
		
		selectFromDropDown(waitForElementToBeVisible(processingStatusDrpDown), "--None--");
		//selectFromDropDown(waitForElementToBeVisible(calculatedProcessingStatusDrpDown), "--None--");
		selectFromDropDown(waitForElementToBeVisible(permitCityCodeDrpDown), "--None--");
				
		waitForElementToBeClickable(closeBtnToRemoveDataFromDrpDown).click();
		waitForElementToBeClickable(closeBtnToRemoveDataFromDrpDown).click();
		Click(saveBtnEditPopUp);
	}
	
	
	public void fillSomeMandatoryFieldsAndSave(Map<String, String> dataMap) throws Exception {
		String buildingPermitNumber = dataMap.get("Permit City Code") + "-" + objUtil.getCurrentDate("yyyMMdd-HHmmss");
		System.setProperty("permitNumberPostEdit", buildingPermitNumber);
		
		enterDataInTextBox(buildingPermitNumberTxtBox, buildingPermitNumber);
		searchAndSelectFromDropDown(parcelsSearchBox, dataMap.get("Parcel"));
		selectFromDropDown(processingStatusDrpDown, dataMap.get("Processing Status"));
		//selectFromDropDown(calculatedProcessingStatusDrpDown, dataMap.get("CalculatedProcessingStatus"));
		searchAndSelectFromDropDown(countyStratCodeSearchBox, dataMap.get("County Strat Code Description"));
		Click(saveBtnEditPopUp);
	}
	
	
	public List<String> fillRemainingMandatoryFieldsAndSave(Map<String, String> dataMap, List<String> invalidDescEntries) throws Exception {
		enterDataInTextBox(estimatedProjectValueTxtBox, dataMap.get("Estimated Project Value"));
		enterDate(issueDateCalender, dataMap.get("Issue Date"));
		enterDate(completionDateCalender, dataMap.get("Completion Date"));
		selectFromDropDown(permitCityCodeDrpDown, dataMap.get("Permit City Code"));
		
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
		Click(waitForElementToBeClickable(saveBtnEditPopUp));
		return validationMsgs;
	}

	
	public Map<String, String> fetchDataFromManualEntry(List<String> listOfTxtAndDrpDowns, List<String> listOfSearchDrpDowns) throws Exception {
		Map<String, String> dataMap = new HashMap<String, String>();
		String xpathStr = null;
		for(String key : listOfTxtAndDrpDowns) {
			xpathStr = "//span[text() = '"+ key +"']/parent::div/following-sibling::div//lightning-formatted-text[@data-output-element-id = 'output-field']";
			String value = waitForElementToBeVisible(xpathStr).getText();
			dataMap.put(key, value);
		}

		for(String key : listOfSearchDrpDowns) {
			xpathStr = "//span[text() = '"+ key +"']/parent::div/following-sibling::div//a[@class = 'slds-grow flex-wrap-ie11']";
			String value = waitForElementToBeVisible(xpathStr).getText();
			dataMap.put(key, value);
		}		
		return dataMap;
	}
	
	
	public int getCountOfManualEntriesFromGrid() {
		String xpathStr = "//tbody/tr//th//a";
		List<WebElement> elements = waitForAllElementsToBeVisible(xpathStr);
		return elements.size();
	}
	
	
	public void navigateToBuildingPermitsRecentlyViewedPage() throws Exception {
		Click(buildingPermitsTab);
	}
		
	
	public void enterDataInTextBox(WebElement element, String value) throws Exception {
		enter(element, value);
	}
	
	
	public void searchAndSelectFromDropDown(WebElement element, String value) throws Exception {
		enter(element, value);
		String xpathStr = "//mark[text() = '" + value.toUpperCase() + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 200);
		drpDwnOption.click();
	}
	
	
	public void selectFromDropDown(WebElement element, String value) throws Exception {
		Click(element);
		String xpathStr = "//div[contains(@class, 'left uiMenuList--short visible positioned')]//a[text() = '" + value + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 200);
		drpDwnOption.click();
	}
	
	
	public void enterDate(WebElement element, String date) throws Exception {
		Click(element);
		selectDateFromDatePicker(date);
	}
	
	/*	Sikander Bhambhu:
	 *	This method is solely to handle date picker and not restricted to this class
	 *	It would be moved to common package/class
	 * */
	public void selectDateFromDatePicker(String expctdDate) throws Exception {
		final Map<String, String> monthMapping = new HashMap<String, String>();
		monthMapping.put("01", "January");
		monthMapping.put("02", "February");
		monthMapping.put("03", "March");
		monthMapping.put("04", "April");
		monthMapping.put("05", "May");
		monthMapping.put("06", "June");
		monthMapping.put("07", "July");
		monthMapping.put("08", "August");
		monthMapping.put("09", "September");
		monthMapping.put("10", "October");
		monthMapping.put("11", "November");
		monthMapping.put("12", "December");
				
		final String[] monthsArr = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		final List<String> monthsList = new ArrayList<>(Arrays.asList(monthsArr));
		
		Date presentDate = new Date();
		String formattedPresentDate = new SimpleDateFormat("MM/dd/yyyy").format(presentDate);		
		Date dt = new SimpleDateFormat("MM/dd/yyyy").parse(expctdDate);
		String formattedExpctdDate = new SimpleDateFormat("MM/dd/yyyy").format(dt);
		
		if(formattedPresentDate.equals(formattedExpctdDate)) {
			Click(currentDate);
		} else {		
			String[] dateArray = formattedExpctdDate.toString().split("/");
			String yearToSelect = dateArray[2];
			String monthToSelect = monthMapping.get(dateArray[0]);
			String dateToSelect;
			if(dateArray[1].startsWith("0")) {
				dateToSelect = dateArray[1].substring(1);
			} else {
				dateToSelect = dateArray[1];
			}

			Select select = new Select(waitForElementToBeVisible(yearDropDown));
			select.selectByValue(yearToSelect);
			
			WebElement visibleMnth = waitForElementToBeVisible(visibleMonth);
			String visibleMonthTxt = visibleMnth.getText().toLowerCase();
			visibleMonthTxt = visibleMonthTxt.substring(0, 1).toUpperCase() + visibleMonthTxt.substring(1).toLowerCase();
			
			int counter = 0;
			int indexOfDefaultMonth = monthsList.indexOf(visibleMonthTxt);		
			int indexOfMonthToSelect = monthsList.indexOf(monthToSelect);
			int counterIterations = (Math.abs(indexOfDefaultMonth - indexOfMonthToSelect));
			
			while(!visibleMonthTxt.equalsIgnoreCase(monthToSelect) || counter > counterIterations) {
				if(indexOfMonthToSelect < indexOfDefaultMonth) {
					waitForElementToBeVisible(prevMnth).click();
				} else {
					waitForElementToBeVisible(nextMnth).click();
				}
				visibleMonthTxt = waitForElementToBeVisible(visibleMonth).getText();
				counter++;
			}
			
			for(WebElement date : dates) {
				String currentDate = date.getText();
				if(currentDate.equals(dateToSelect)) {
					date.click();
					break;
				}
			}
		}
	}
}
