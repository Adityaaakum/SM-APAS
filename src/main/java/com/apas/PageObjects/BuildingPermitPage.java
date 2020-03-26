package com.apas.PageObjects;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.apas.Utils.Util;

public class BuildingPermitPage extends ApasGenericPage {

	Util objUtil;

	public BuildingPermitPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	//Below objects are for Building Permit Module Screen

	@FindBy(xpath = "//a[@title = 'New']")
	public WebElement newButton;

	@FindBy(xpath = "//button[text() = 'Edit']")
	public WebElement editButton;

	@FindBy(xpath = "//div[@role='menu']//li[@class='uiMenuItem']/a[@title = 'Edit']")
	public WebElement editMenuItemButton;

	@FindBy(xpath = "//lightning-icon//span[text()='Show More']")
	public WebElement showMoreButton;

	@FindBy(xpath = "//span[contains(text(), 'Manual Entry Building Permit')]//parent::div//preceding-sibling::div//input[@type = 'radio']")
	public WebElement manualEntryRadioBtn;

	@FindBy(xpath = "//div[@class = 'inlineFooter']//span[text() = 'Next']//parent::button")
	public WebElement recordTypePopUpNextButton;

	//Below objects are for New Building Permit Pop Up

	@FindBy(xpath = "//h2[text() = 'New Building Permit: Manual Entry Building Permit']")
	public WebElement buildingPermitPopUp;

	@FindBy(xpath = "//div[contains(.,'New Building Permit: Manual Entry Building Permit')]//button[@title='Close this window']")
	public WebElement closeNewBuildingPermitPopUpButton;

	@FindBy(xpath = "//span[text() = 'Building Permit Number']/parent::label/following-sibling::input")
	public WebElement buildingPermitNumberTxtBox;

	@FindBy(xpath = "//input[@title = 'Search Parcels']")
	public WebElement parcelsSearchBox;

	@FindBy(xpath = "//span[text() = 'Processing Status']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement processingStatusDrpDown;

	@FindBy(xpath = "//input[@title = 'Search County Strat Codes']")
	public WebElement countyStratCodeSearchBox;

	@FindBy(xpath = "//span[text() = 'Estimated Project Value']/parent::label/following-sibling::input")
	public WebElement estimatedProjectValueTxtBox;

	@FindBy(xpath = "//span[text() = 'Issue Date']/parent::label/following-sibling::div/input")
	public WebElement issueDateCalender;

	@FindBy(xpath = "//span[text() = 'Completion Date']/parent::label/following-sibling::div/input")
	public WebElement completionDateCalender;

	@FindBy(xpath = "//span[text() = 'Permit City Code']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement permitCityCodeDrpDown;

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

	@FindBy(xpath = "//div[contains(.,'View Duplicates')]/button[@title='Close this window']")
	public WebElement closeViewDuplicatePopUpButton;

	/**
	 * @Description: This method is used to click on New button to open the new building permit.
	 * It also internally handles the additional pop up window to select manual entry
	 * creation radio button when Data Admin is logged into application
	 * @throws IOException
	 */
	public void openManualEntryForm() throws IOException, InterruptedException {
		javascriptClick(waitForElementToBeClickable(newButton));
		if(System.getProperty("isDataAdminLoggedIn") != null && System.getProperty("isDataAdminLoggedIn").equals("true")) {
			Click(waitForElementToBeClickable(manualEntryRadioBtn));
			Click(waitForElementToBeClickable(recordTypePopUpNextButton));
		}
		waitForElementToBeVisible(buildingPermitPopUp,30);
		Thread.sleep(2000);
	}

	/**
	 * @Description: It fills all the required fields in manual entry pop up
	 * @param dataMap: A data map which contains manual entry pop up field names (as keys)
	 * and their values (as values)
	 * @throws Exception
	 */
	public void enterManualEntryData(Map<String, String> dataMap) throws Exception {

		waitForElementToBeClickable(countyStratCodeSearchBox);
		waitForElementToBeClickable(permitCityCodeDrpDown);
		waitForElementToBeClickable(issueDateCalender);

		enter(buildingPermitNumberTxtBox, dataMap.get("Building Permit Number"));
		searchAndSelectFromDropDown(parcelsSearchBox, dataMap.get("Parcel"));
		selectFromDropDown(processingStatusDrpDown, dataMap.get("Processing Status"));
		searchAndSelectFromDropDown(countyStratCodeSearchBox, dataMap.get("County Strat Code Description"));
		enter(estimatedProjectValueTxtBox, dataMap.get("Estimated Project Value"));
		enterDate(issueDateCalender, dataMap.get("Issue Date"));
		enterDate(completionDateCalender, dataMap.get("Completion Date"));
		selectFromDropDown(permitCityCodeDrpDown, dataMap.get("Permit City Code"));
		enter(workDescriptionTxtBox, dataMap.get("Work Description"));

	}

	/** @Description: This method will create a manual building permit entry in APAS
	 */
	public void addAndSaveManualBuildingPermit(Map<String, String> dataMap) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding and saving a new Building Permit manual record");
		openManualEntryForm();
		enterManualEntryData(dataMap);
		Click(saveButton);
		Thread.sleep(2000);
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
		WebElement drpDwnOption = locateElement(xpathStr, 200);
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
		selectDateFromDatePicker(date);
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

	/**
	 * @description: This method will return the error message appeared against the filed name passed in the parameter
	 * @param fieldName: field name for which error message needs to be fetched
	 */
	public String getIndividualFieldErrorMessage(String fieldName) {
		return getElementText(driver.findElement(By.xpath("//div[@role='listitem']//span[text()='" + fieldName + "']/../../../ul[contains(@data-aura-class,'uiInputDefaultError')]")));
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
		String buildingPermitNumber = "LM-" + objUtil.getCurrentDate("yyyMMdd-HHmmss");
		Map<String, String> manualBuildingPermitMap = new HashMap<>();
		manualBuildingPermitMap.put("Permit City Code","LM");
		manualBuildingPermitMap.put("Parcel","000002");
		manualBuildingPermitMap.put("Building Permit Number",buildingPermitNumber);
		manualBuildingPermitMap.put("Processing Status","Process");
		manualBuildingPermitMap.put("Issue Date","11/10/2019");
		manualBuildingPermitMap.put("Completion Date","11/10/2019");
		manualBuildingPermitMap.put("County Strat Code Description","REPAIR ROOF");
		manualBuildingPermitMap.put("Work Description","New Construction");
		manualBuildingPermitMap.put("Estimated Project Value","500");

		return  manualBuildingPermitMap;
	}


}
