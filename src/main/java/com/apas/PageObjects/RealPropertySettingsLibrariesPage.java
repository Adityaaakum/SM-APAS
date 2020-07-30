package com.apas.PageObjects;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Assertions.SoftAssertion;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;

public class RealPropertySettingsLibrariesPage extends ApasGenericPage{
	SoftAssertion softAssert;
	ApasGenericFunctions objApasGenericFunctions;
	Util objUtils;
	
	public RealPropertySettingsLibrariesPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericFunctions= new ApasGenericFunctions(driver);
		objUtils = new Util();
		softAssert = new SoftAssertion();
	}
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@title = 'New']")
	public WebElement newButton;
	
	@FindBy(xpath = "//button[@title = 'Cancel']")
	public WebElement cancelButton;
	
	@FindBy(xpath = "//button[@title = 'Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//label/span[text() = 'RP Setting Name']/../../input")
	public WebElement rpSettingNameEditBox;

	@FindBy(xpath = "//span[text() = 'Status']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement statusDropDown;
	
	@FindBy(xpath = "//input[@title='Search Roll Year Settings']")
	public WebElement searchRollYearSettingsLookup;
	
	@FindBy(xpath = "//label/span[text()='DV Low Income Exemption Amount']/../../input")
	public WebElement dvLowIncomeExemptionAmountEditBox;

	@FindBy(xpath = "//label/span[text()='DV Basic Exemption Amount']/../../input")
	public WebElement dvBasicIncomeExemptionAmountEditBox;
	
	@FindBy(xpath = "//label/span[text()='DV Low Income Household Limit']/../../input")
	public WebElement dvLowIncomeHouseholdLimitEditBox;
	
	@FindBy(xpath = "//label/span[text()='DV Annual Due Date']/../following-sibling::div/input")
	public WebElement dvLowIncomeHouseholdLimitDatePicker;
	
	@FindBy(xpath = "//label/span[text()='DV Annual Due Date 2']/../following-sibling::div/input")
	public WebElement dvAnnualLowIncomeDueDate2DatePicker;
	
	@FindBy(xpath = "//div[@role='alert'][@data-key='success']")
	public WebElement successAlert;

	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;
	
	@FindBy(xpath = "//ul[@class='errorsList']//li")
	public WebElement errorMsgOnTop;
	
	@FindBy(xpath = "//div[contains(@class,'warning')]//div")
	public WebElement warningMsgOnTop;
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Edit']")
	public WebElement editLinkUnderShowMore;
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Delete']")
	public WebElement deleteLinkUnderShowMore;
	
	@FindBy(xpath = "//h2[contains(text(),'Delete Real')]//..//following-sibling::div//span[text()='Delete']")
	public WebElement deleteButtonOnPopUp;
	
	@FindBy(xpath = "//div[@data-aura-class='forceSearchDesktopHeader']/div[@data-aura-class='forceSearchInputDesktop']//input")
	public WebElement globalSearchListEditBox;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//button[text() = 'Edit']")
    public WebElement editButton;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text() = 'Status']//..//following-sibling::div//lightning-formatted-text")
	public WebElement statusLabelValueDropDown;
	
	@FindBy(xpath = "//span[text() = 'Next']")
	public WebElement nextButton;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//table//tbody//tr")
    public List<WebElement> numberOfRPSL;
	
	@FindBy(xpath = "//div[@class='uiBlock']//p[@class='detail']//span")
	public WebElement errorMsgforEdit;
	
	@FindBy(xpath = "//li[@title = 'Details']")
	public WebElement detailsTabLabel;
	
	@FindBy(xpath = "//span[text()='Close this window']//ancestor::button")
	public WebElement closeErrorPopUp;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//tr//span[text()='Approved']//..//..//preceding-sibling::th//a")
	public WebElement approvedRPSLLink;
	
	@FindBy(xpath = "//li[contains(text(), 'Record is locked. Please check with your system administrator')]")
	public WebElement errorMsgOnTopForEditRPSL;
	
	/**
	 * @Description: This method is to handle fields like Status
	 * by clicking the web element and then selecting the given value from drop down
	 * @param element: WebElement for required field
	 * @param value: Like 'Yet to be submit for Approval or Approved' for Status field etc.
	 * @throws Exception
	 */
	public void selectFromDropDown(WebElement element, String value) throws Exception {
		Click(element);
		String xpathStr = "//div[contains(@class, 'uiPopupTarget')]//a[text() = '" + value + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 100);
		drpDwnOption.click();
	}
	
	/**
	 * @description: This method will return the error message appeared against the field name passed in the parameter
	 * @param fieldName: field name for which error message needs to be fetched
	 * @throws Exception 
	 */
	public String getIndividualFieldErrorMessage(String fieldName) throws Exception {
		waitUntilElementIsPresent("//*/span[text() = '\" + fieldName + \"']//..//..//..//ul[contains(@class,'has-error')]//li", 2);
		return getElementText(driver.findElement(By.xpath("//*/span[text() = '" + fieldName + "']//..//..//..//ul[contains(@class,'has-error')]//li")));
	}

	
	/**
	 * Description: This method will only enter the values of Real Property settings Libraries fields on the application
	 * @param rollYear: roll year value
	 */
	public void enterRealPropertySettingsDetails(Map<String, String> dataMap, String rollYear) throws Exception {
		ReportLogger.INFO("Clicking on New Button");
		Click(newButton);
		//selectRecordType("Exemption Limits");
		ReportLogger.INFO("Entering details for Real Property Settings record");
		enter(rpSettingNameEditBox,rollYear);
		selectFromDropDown(statusDropDown,dataMap.get("Status"));
		searchAndSelectFromDropDown(searchRollYearSettingsLookup,rollYear);
		enter(dvLowIncomeExemptionAmountEditBox,dataMap.get("DV Low Income Exemption Amount"));
		enter(dvBasicIncomeExemptionAmountEditBox,dataMap.get("DV Basic Exemption Amount"));
		enter(dvLowIncomeHouseholdLimitEditBox,dataMap.get("DV Low Income Household Limit Amount"));
		enter(dvLowIncomeHouseholdLimitDatePicker, dataMap.get("DV Annual Low Income Due Date")+"/"+rollYear);
		enter(dvAnnualLowIncomeDueDate2DatePicker, dataMap.get("DV Annual Low Income Due Date2")+"/"+rollYear);	
	}
	
	/**
	 * Description: This method will click on save and add a new Real Property settings
	 * @return : returns the text message of success alert
	 */
	public String saveRealPropertySettings() throws Exception {
		ReportLogger.INFO("Clicking on Save Button");
		Click(saveButton);
		//waitForElementToBeVisible(successAlert,30);
		locateElement("//div[@role='alert'][@data-key='success']",2);
		locateElement("//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']",2);
		return getElementText(successAlertText);
	}
	
	/**
	 * @description: Clicks on the show more link displayed against the given entry
	 * @param entryDetails: Name of the entry displayed on grid which is to be accessed
	 * @throws Exception
	 */
	public void clickShowMoreLink(String entryDetails) throws Exception {		
		Thread.sleep(2000);
		String xpathStr = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table//tbody/tr//th//a[text() = '"+ entryDetails +"']//parent::span//parent::th//following-sibling::td//a[@role = 'button']";
		WebElement modificationsIcon = locateElement(xpathStr, 30);
		clickAction(modificationsIcon);
	}

	 public void removeRealPropertySettingEntry(String rollYear) throws Exception {
	        SalesforceAPI objSalesforceAPI = new SalesforceAPI();
	        String queryForID = "SELECT Id FROM Real_Property_Settings_Library__c where Name = 'Exemption Limits - " + rollYear +"'";
	        objSalesforceAPI.delete("Real_Property_Settings_Library__c", queryForID);
	        
	    }
	
	 /**
		 * @description: This method will create the RPSL
		 * @param dataMap: Data with which RPSL is created
		 * @throws Exception
		 */
	 public String createRPSL(Map<String, String> dataMap, String rollYear) throws Exception {			 
		//Step1: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step2: Selecting 'All' List View
		objApasGenericFunctions.displayRecords("All");
		
		//Step3: Delete current Roll Year's RPSL if it already exists	
		removeRealPropertySettingEntry(rollYear);
		 		
		//Step4: Creating the RPSL record for current year
		ReportLogger.INFO("Adding current year's 'Real Property Settings' record with following details: "+ dataMap);
		enterRealPropertySettingsDetails(dataMap,rollYear);
		
		//Step7: Clicking on Save button & Verifying the RPSL record for current year after creation		
		String strSuccessAlertMessage = saveRealPropertySettings();	
		return strSuccessAlertMessage;
	 }
	 
	 	 
}