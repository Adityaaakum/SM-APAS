package com.apas.PageObjects;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.Map;

public class CityStratCodesPage extends Page {

	ApasGenericPage objApasGenericPage;
	BuildingPermitPage objBuildingPermitPage;

	public CityStratCodesPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericPage = new ApasGenericPage(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
	}

	@FindBy(xpath = "//a[@title = 'New']")
	public WebElement newButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@name = 'Edit']")
	public WebElement editButton;

	@FindBy(xpath = "//div[@data-aura-class='forcePageError']//li")
	public WebElement errorMsgOnTop;

	@FindBy(xpath = "//button[@title='Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//input[@title='Search County Strat Codes']")
	public WebElement countyStratCodeEditBox;

	@FindBy(xpath = "//span[text() = 'City Code']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement cityCodeDropDown;

	@FindBy(xpath = "//label/span[text()='City Strat Code']/../../input")
	public WebElement cityStratCodeEditBox;

	@FindBy(xpath = "//span[text() = 'Status']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement statusDropDown;

	@FindBy(xpath = "//div[@role='alert'][@data-key='success']")
	public WebElement successAlert;

	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;

	@FindBy(xpath = "//h2[contains(text(),'New')]")
	public WebElement newEntryPopUp;

	@FindBy(xpath = "//button[@title='Cancel']")
	public WebElement cancelButton;

	@FindBy(xpath = "//span[text() = 'County Strat Code']//ancestor::div[contains(@class, 'has-error')]//following-sibling::ul//li[text() = 'Complete this field']")
	public WebElement errorMsgUnderCountyStratCodeField;

	@FindBy(xpath = "//span[text() = 'City Code']//ancestor::div[contains(@class, 'has-error')]//following-sibling::ul//li[text() = 'Complete this field']")
	public WebElement errorMsgUnderCityCodeField;

	@FindBy(xpath = "//span[text() = 'City Strat Code']//ancestor::div[contains(@class, 'has-error')]//following-sibling::ul//li[text() = 'Complete this field']")
	public WebElement errorMsgUnderCityStratCodeField;

	@FindBy(xpath = "(//span[contains(text(), 'Strat Codes')]//parent::a | (//span[contains(text(), 'Recently Viewed')]//parent::a))[1]")
	public WebElement recentlyViewedTab;

	@FindBy(xpath = "//button[@title='Delete']")
	public WebElement deleteButtonInPopUp;

	@FindBy(xpath = "//input[contains(@name, '_Strat_Code__c-search-input')]")
	public WebElement searchBox;

	@FindBy(xpath = "//span[@class = 'deleteIcon']")
	public WebElement crossButtonToClearCountyStratField;


	/**
	 * Description: This method will only enter the values of city strat code fiels on the application
	 * @param countyStratCode: County Strat Code
	 * @param cityCode: City Code
	 * @param cityStratCode: City Strat Code
	 * @param status: Status
	 */
	public void enterCityStratCodeDetails(String countyStratCode, String cityCode, String cityStratCode, String status) throws Exception {
		enter(countyStratCodeEditBox,countyStratCode);
		Click(driver.findElement(By.xpath("//*[@role='option']//mark[text()='" + countyStratCode + "']")));
		Select(cityCodeDropDown,cityCode);
		enter(cityStratCodeEditBox,cityStratCode);
		Select(statusDropDown,status);
	}

	/**
	 * Description: This method will add a new city strat code
	 * @param countyStratCode: County Strat Code
	 * @param cityCode: City Code
	 * @param cityStratCode: City Strat Code
	 * @param status: Status
	 * @return : returns the text message of success alert
	 */
	public String addAndSaveCityStratcode(String countyStratCode, String cityCode, String cityStratCode, String status) throws Exception {
		Click(newButton);
		waitForElementToBeClickable(countyStratCodeEditBox,10);
		enterCityStratCodeDetails(countyStratCode,cityCode,cityStratCode,status);
		Click(saveButton);
		waitForElementToBeVisible(successAlert,20);
		return getElementText(successAlertText);
	}

	/**
	 * Description: This method will open the new entry pop up
	 */
	public void openNewEntry() throws Exception {
		ReportLogger.INFO("Opening the City Strat code Entry Form");
		Click(newButton);
		waitForElementToBeVisible(newEntryPopUp, 10);
	}

//	/**
//	 * Description: This method enter values in given fields in new entry pop up
//	 * @param dataMap: Data map containing keys as field names and values as their values
//	 */
//	public void enterCityStratCodeDetails(Map<String, String> dataMap) throws Exception {
//		openNewEntry();
//		Thread.sleep(1000);
//		objApasGenericPage.searchAndSelectOptionFromDropDown(countyStratCodeEditBox, dataMap.get("County Strat Code"));
//		objApasGenericPage.selectOptionFromDropDown(cityCodeDropDown, dataMap.get("City Code"));
//		enter(cityStratCodeEditBox, dataMap.get("City Strat Code"));
//		objApasGenericPage.selectOptionFromDropDown(statusDropDown, dataMap.get("Status"));
//	}

//	/**
//	 * Description: This method will add a new city strat code
//	 * @param dataMap: Map containing field names as keys and values as their values
//	 * @return String: returns the text message of success alert
//	 */
//	public String addAndSaveCityStratCode(Map<String, String> dataMap) throws Exception {
//		enterCityStratCodeDetails(dataMap);
//		Click(saveButton);
//		waitForElementToBeVisible(successAlert,20);
//		return getElementText(successAlertText);
//	}
//
//	/**
//	 * Description: This method will edit the values in existing entry
//	 * @param updatedCountyStratCodes: Updated value of County Strat Cides
//	 * @param updatedCityStratCodes
//	 */
//	public void editExistingCityStratEntry(String updatedCountyStratCodes, String updatedCityStratCodes) throws Exception {
//		Click(crossButtonToClearCountyStratField);
//		objApasGenericPage.searchAndSelectOptionFromDropDown(countyStratCodeEditBox, updatedCountyStratCodes);
//		waitForElementToBeVisible(cityStratCodeEditBox, 10);
//		enter(cityStratCodeEditBox, updatedCityStratCodes);
//	}

}
