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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

public class BuildingPermitPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);
	List<WebElement> listOfElementsToClear = new ArrayList<WebElement>();
	
	public BuildingPermitPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}
	
	// Locators to create a manual entry.
	
	@FindBy(xpath = "//one-app-launcher-header/button[@class = 'slds-button']")
	private WebElement appLauncher;

	@FindBy(xpath = "//input[contains(@placeholder, 'Search apps and items')]")
	private WebElement appLauncherSearchBox;

	@FindBy(xpath = "//a//span[text() = 'Building Permits']")
	private WebElement buildingPermitsTab;
	
	@FindBy(xpath = "//div[text() = 'New']")
	private WebElement newButton;
	
	@FindBy(xpath = "//h2[text() = 'New Building Permit: Manual Entry Building Permit']")
	private WebElement buildingPermitPopUp;
	
	@FindBy(xpath = "//button[@title='Close this window']")
	private WebElement closeBuildingPermitPopUp;
	
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
	
	@FindBy(xpath = "//button[@title = 'Cancel']//span[text() = 'Cancel']")
	private WebElement cancelButton;

	@FindBy(xpath = "//button[@title = 'Save & New']//span[text() = 'Save & New']")
	private WebElement saveAndNewButton;
	
	@FindBy(xpath = "//button[@title = 'Save']//span[text() = 'Save']")
	private WebElement saveButton;
	
	// Locators to validate error message for mandatory fields while creating manual entry.
	
	@FindBy(xpath = "//li[contains(text(), 'These required fields must be completed:')]")
	private WebElement errorMsgOnTop;
	
	@FindBy(xpath = "//li[text() = 'Complete this field']")
	private List<WebElement> errorMsgUnderLabels;
	
	// Locators to perform actions on existing building permits from grid.
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Edit']")
	private WebElement editLinkUnderShowMore;
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Delete']")
	private WebElement deleteLinkUnderShowMore;
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Change Owner']")
	private WebElement changeOwnerLinkUnderShowMore;
	
	@FindBy(xpath = "//ul[contains(@class, 'oneActionsRibbon')]//div[text() = 'Change Owner']")
	private WebElement changeOwnerBtnAboveTbl;
	
	@FindBy(xpath = "//span[text() = 'City APN']/parent::label/following-sibling::input")
	private WebElement cityApnTxtBox;
	
	@FindBy(xpath = "//span[text() = 'Parcel']/parent::label/following-sibling::div//input[@title = 'Search Parcels']")
	private WebElement parcelsSearchBoxInEditPopUp;
	
	@FindBy(xpath = "//span[text() = 'Owner Name']/parent::label/following-sibling::input")
	private WebElement ownerNameTxtBox;
	
	@FindBy(xpath = "//a//span[text() = 'Press Delete to Remove']")
	private WebElement clearDrpDownBtn;
	
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
	
	//@FindBy(xpath = "//span[text() = 'Edit " + FieldToEdit + "']")
	//private WebElement buildingPermitDetailEditIcon;
	
	// Locators to edit building permit from Details screen.
	
	//@FindBy(xpath = "//tbody/tr//th//a[text() = '" + permitNumber + "']")
	//private WebElement buildingPermitName;
		
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
		Click(newButton);
	}
	
	public void enterManualEntryData(Map<String, String> dataMap) throws Exception {
		String buildingPermitNumber = dataMap.get("PermitCityCode") + "-" + getCurrentDate("yyyMMdd-HHmmss");
		System.setProperty("permitName", buildingPermitNumber);
		
		enterDataInTextBox(buildingPermitNumberTxtBox, buildingPermitNumber);
		searchAndSelectFromDropDown(parcelsSearchBox, dataMap.get("Parcel"));
		selectFromDropDown(processingStatusDrpDown, dataMap.get("ProcessingStatus"));
		selectFromDropDown(calculatedProcessingStatusDrpDown, dataMap.get("CalculatedProcessingStatus"));
		searchAndSelectFromDropDown(countyStratCodeSearchBox, dataMap.get("CountyStratCodeDescription"));
		enterDataInTextBox(estimatedProjectValueTxtBox, dataMap.get("EstimatedProjectValue"));
		enterDate(issueDateCalender, dataMap.get("IssueDate"));
		enterDate(completionDateCalender, dataMap.get("CompletionDate"));
		selectFromDropDown(permitCityCodeDrpDown, dataMap.get("PermitCityCode"));
		enterDataInTextBox(workDescriptionTxtBox, dataMap.get("WorkDescription"));
	}
	
	public void saveManualEntry() throws Exception {
		Click(saveButton);
	}
	
	public boolean saveManualEntryAndExit() throws Exception {
		Click(saveAndNewButton);
		boolean isNewManualEntryPopUpDisplayed = waitForElementToBeVisible(10, buildingPermitPopUp);
		if (isNewManualEntryPopUpDisplayed) {
			Click(closeBuildingPermitPopUp);
		}
		return isNewManualEntryPopUpDisplayed;
	}
	
	public void abortManualEntry() throws Exception {
		Click(cancelButton);
	}
	
	public boolean checkBuildingPermitOnDetailsPage () throws Exception {
		String xpathStr = "//lightning-formatted-text[text() = '" + System.getProperty("permitName") + "']";
		WebElement element = locateElement(xpathStr, 300);
		return element.isDisplayed();
	}
	
	public boolean checkBuildingPermitOnGrid() {
		String xpathStr = "//tbody/tr//th//a[text() = '" + System.getProperty("permitName") + "']";		
		return waitForElementToBeVisible(10, xpathStr);
	}

	public List<String> retrieveMandatoryFieldsValidationErrorMsgs() throws Exception {
		List<String> errorsList = new ArrayList<String>();
		String errorMsgOnTopOfPopUpWindow = getElementText(errorMsgOnTop);
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
		listOfElementsToClear.add(element);
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
