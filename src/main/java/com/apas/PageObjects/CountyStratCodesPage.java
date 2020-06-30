package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.Map;

public class CountyStratCodesPage extends ApasGenericPage {

	public CountyStratCodesPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@title = 'New']")
	public WebElement newButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@name = 'Edit']")
	public WebElement editButton;

	@FindBy(xpath = "//button[@title='Cancel']")
	public WebElement cancelButton;

	@FindBy(xpath = "//span[text() = 'Processing Status']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement processingStatusDrpDown;

	@FindBy(xpath = "(//tbody//tr//th//a)[1]")
	public WebElement firstEntryInGrid;

	@FindBy(xpath = "//div[@data-aura-class='forcePageError']//li")
	public WebElement errorMsgOnTop;

	@FindBy(xpath = "//span[text() = 'Strat Code Reference Number']/parent::label/following-sibling::input")
	public WebElement stratCodeRefNumInputFiled;

	@FindBy(xpath = "//div[@role='alert'][@data-key='success']")
	public WebElement successAlert;

	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;

	@FindBy(xpath = "//button[@title='Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//span[text() = 'Strat Code Description']/parent::label/following-sibling::input")
	public WebElement stratCodeDescInputField;

	@FindBy(xpath = "//span[text() = 'Permit Value Operator']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement permitValueOperatorDropDown;

	@FindBy(xpath = "//span[text() = 'Status']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement statusDropDown;

	@FindBy(xpath = "//span[text() = 'Permit Value Limit']/parent::label/following-sibling::input")
	public WebElement permitValueLimit;

	@FindBy(xpath = "//span[text() = 'Strat Code Description']//ancestor::div[contains(@class, 'has-error')]//following-sibling::ul//li[text() = 'Complete this field']")
	public WebElement errorMsgUnderStratCodeDesc;

	@FindBy(xpath = "//span[text() = 'Strat Code Reference Number']//ancestor::div[contains(@class, 'has-error')]//following-sibling::ul//li[text() = 'Complete this field']")
	public WebElement errorMsgUnderStratCodeRefNum;

	@FindBy(xpath = "//span[text() = 'Processing Status']//ancestor::div[contains(@class, 'has-error')]//following-sibling::ul//li[text() = 'Complete this field']")
	public WebElement errorMsgUnderProcessingStatus;

	@FindBy(xpath = "//div[contains(@class, 'text--warning forceDedupeManager')]//div")
	public WebElement errorMsgForDuplicateEntry;

	@FindBy(xpath = "//span[text() = 'City Strat Codes']/ancestor::header//following-sibling::div//a[contains(@class, 'slds-button') and @role = 'button']")
	public WebElement cityStratCodesDropDownIcon;

	@FindBy(xpath = "//div[contains(@class, 'uiMenuList') and contains(@class,'visible positioned')]//div[@title = 'New'][@role='button']")
	public WebElement cityStratCodesNewOptionToCreateEntry;

	@FindBy(xpath = "//span[text() = 'City Strat Codes']//parent::span[text() = 'View All']")
	public WebElement viewAllCityStratCodes;

	@FindBy(xpath = "(//h1[@title = 'City Strat Codes']//ancestor::div[contains(@class, 'slds-page-header--object-home')]//following-sibling::div[contains(@class, 'slds-grid listDisplays')]//table//tbody/tr//th//a)[1]")
	public WebElement nameOfFirstEntryOnViewAllPage;

	@FindBy(xpath = "(//h1[@title = 'City Strat Codes']//ancestor::div[contains(@class, 'slds-page-header--object-home')]//following-sibling::div[contains(@class, 'slds-grid listDisplays')]//table//tr//td//span//a[@role = 'button'])[1]")
	public WebElement showMoreBtnOfFirstEntryOnViewAllPage;

	/**
	 * Description: This method will open the new entry pop up
	 */
	public void openNewEntry() throws Exception {
		ReportLogger.INFO("Opening the City Strat code Entry Form");
		Click(newButton);
		waitForElementToBeClickable(stratCodeRefNumInputFiled, 10);
	}

	/**
	 * Description: This method enter values in given fields in new entry pop up
	 * @param dataMap: Data map containing keys as field names and values as their values
	 */
	public void enterCountyStratCodeDetails(Map<String, String> dataMap) throws Exception {

		enter(stratCodeRefNumInputFiled, dataMap.get("Strat Code Reference Number"));
		enter(stratCodeDescInputField, dataMap.get("Strat Code Description"));
		selectOptionFromDropDown(statusDropDown, dataMap.get("Status"));
		selectOptionFromDropDown(processingStatusDrpDown, dataMap.get("Processing Status"));
		//objApasGenericPage.selectOptionFromDropDown(permitValueOperatorDropDown, dataMap.get("Permit Value Operator"));
		//enter(permitValueLimit, dataMap.get("Permit Value Limit"));
	}

	/**
	 * Description: This method will add a new city strat code
	 * @param dataMap: Data map containing keys as field names and values as their values
	 * @return String: returns the text message of success alert
	 */
	public String addAndSaveCountyStratCode(Map<String, String> dataMap) throws Exception {
		openNewEntry();
		Thread.sleep(1000);
		enterCountyStratCodeDetails(dataMap);
		Click(saveButton);
		waitForElementToBeVisible(successAlert,20);
		return getElementText(successAlertText);
	}

}
