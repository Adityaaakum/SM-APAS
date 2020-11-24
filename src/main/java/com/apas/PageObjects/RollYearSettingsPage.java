package com.apas.PageObjects;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.PasswordUtils;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.generic.ApasGenericFunctions;
import com.gargoylesoftware.htmlunit.javascript.host.dom.Document;
import com.relevantcodes.extentreports.LogStatus;

public class RollYearSettingsPage extends ApasGenericPage {
	
	Util objUtil;
	ApasGenericPage objApasGenericPage;
	SalesforceAPI objSalesforceAPI;
	ApasGenericFunctions apasGenericObj;
	Page objPage;

	public RollYearSettingsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
		objApasGenericPage = new ApasGenericPage(driver);
		objSalesforceAPI = new SalesforceAPI();
		apasGenericObj= new ApasGenericFunctions(driver);
		objPage=new Page(driver);
	}
	
	
	//Locators added for elements on Roll Year Settings screen

	@FindBy(xpath = "//a[@title='New']/div[@title='New'][1]")
	public WebElement newRollYearButton;
	
	@FindBy(xpath = "//span[text()='Recently Viewed']")
	public WebElement recentlyViewedListView;
	
	/*@FindBy(xpath = "//span[text() = 'Roll Year Settings']//parent::label//following-sibling::input[@class=' input']")
	public WebElement rollYearSettings;

	@FindBy(xpath = "//span[text() = 'Roll Year']//parent::span//following-sibling::div[@class='uiMenu']")
	public WebElement rollYear;

	@FindBy(xpath = "//span[text() = 'Open Roll Start Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement openRollStartDate;

	@FindBy(xpath = "//span[text() = 'Open Roll End Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement openRollEndDate;

	@FindBy(xpath = "//span[text() = 'Calendar Start Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement calendarStartDate;

	@FindBy(xpath = "//span[text() = 'Calendar End Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement calendarEndDate;

	@FindBy(xpath = "//span[text() = 'Lien Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement lienDate;

	@FindBy(xpath = "//span[text() = 'Tax Start Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement taxStartDate;

	@FindBy(xpath = "//span[text() = 'Tax End Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement taxEndDate;

	@FindBy(xpath = "//span[text() = 'Status']//parent::span//following-sibling::div[@class='uiMenu']")
	public WebElement status;*/
	
	//@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//label[text()='Roll Year Settings']/..//input")
		public String rollYearSettings="Roll Year Settings";

		//@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//label[text()='Roll Year']//following-sibling::div//input")
		public String rollYear="Roll Year";

		//@FindBy(xpath = "//span[text() = 'Open Roll Start Date']//parent::label//following-sibling::div//input[@class=' input']")
		public String openRollStartDate="Open Roll Start Date";

		//@FindBy(xpath = "//span[text() = 'Open Roll End Date']//parent::label//following-sibling::div//input[@class=' input']")
		public String openRollEndDate="Open Roll End Date";

		//@FindBy(xpath = "//span[text() = 'Calendar Start Date']//parent::label//following-sibling::div//input[@class=' input']")
		public String calendarStartDate="Calendar Start Date";

		//@FindBy(xpath = "//span[text() = 'Calendar End Date']//parent::label//following-sibling::div//input[@class=' input']")
		public String calendarEndDate="Calendar End Date";
		public String annualBatchExecutionDate="Annual Batch Execution Date";

		//@FindBy(xpath = "//span[text() = 'Lien Date']//parent::label//following-sibling::div//input[@class=' input']")
		public String lienDate="Lien Date";

		//@FindBy(xpath = "//span[text() = 'Tax Start Date']//parent::label//following-sibling::div//input[@class=' input']")
		public String taxStartDate="Tax Start Date";

		//@FindBy(xpath = "//span[text() = 'Tax End Date']//parent::label//following-sibling::div//input[@class=' input']")
		public String taxEndDate="Tax End Date";

		//@FindBy(xpath = "//span[text() = 'Status']//parent::span//following-sibling::div[@class='uiMenu']")
		public String status="Status";
	
		@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@title='Save' and text()='Save']")
		public WebElement saveButton;
		
		@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@title='Cancel' and text()='Cancel']")
		public WebElement cancelButton;
		
		@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[text() = 'Edit']")
		public WebElement editButton;
		
		@FindBy(xpath = "//div[@role='menu']//li[@class='uiMenuItem']/a[@title = 'Edit']")
		public WebElement editMenuItemButton;
	
	@FindBy(xpath = "//div[@data-aura-class='forceDedupeManager']/div")
	public WebElement duplicateRecord;
	
	@FindBy(xpath = "//div//a[text() = 'View Duplicates']")
	public WebElement viewDuplicateRecord;
	
	/*@FindBy(xpath = "//li[text()='Lien Date year should be same as Roll Year']")
	public WebElement errorOnLienDate;
	
	@FindBy(xpath = "//li[text()='Tax Start Date year should be same as Roll Year']")
	public WebElement errorOnTaxStartDate;
	
	@FindBy(xpath = "//li[text()= " + "\"" + "Tax End Date" + "'s" + " year should be one year greater of selected Roll Year" + "\"]")
	public WebElement errorOnTaxEndDate;
	
	@FindBy(xpath = "//li[text()= " + "\"" + "Start Date" + "'s" + " year should be one year less of selected Roll Year" + "\"]")
	public WebElement errorOnOpenRollStartDate;
	
	@FindBy(xpath = "//li[text()='End Date year should be same as Roll Year']")
	public WebElement errorOnOpenRollEndDate1;
	
	@FindBy(xpath = "//li[text()='End Date must be greater than Start Date']")
	public WebElement errorOnOpenRollEndDate2;
	
	@FindBy(xpath = "//li[text()='Calendar Start Date year should be same as Roll Year']")
	public WebElement errorOnCalendarStartDate;
	
	@FindBy(xpath = "//li[text()='Calendar End Date must be greater than Calendar Start Date']")
	public WebElement errorOnCalendarEndDate1;
	
	@FindBy(xpath = "//li[text()='Calendar End Date year should be same as Roll Year']")
	public WebElement errorOnCalendarEndDate2;*/
	
	//@FindBy(xpath = "//li[text()='Lien Date year should be same as Roll Year']")
		public String errorOnLienDate="Lien Date";
		
		//@FindBy(xpath = "//li[text()='Tax Start Date year should be same as Roll Year']")
		public String errorOnTaxStartDate="Tax Start Date";
		
		//@FindBy(xpath = "//li[text()= " + "\"" + "Tax End Date" + "'s" + " year should be one year greater of selected Roll Year" + "\"]")
		public String errorOnTaxEndDate="Tax End Date";
		
		//@FindBy(xpath = "//li[text()= " + "\"" + "Start Date" + "'s" + " year should be one year less of selected Roll Year" + "\"]")
		public String errorOnOpenRollStartDate="Open Roll Start Date";
		
		//@FindBy(xpath = "//li[text()='End Date year should be same as Roll Year']")
		public String errorOnOpenRollEndDate1="Open Roll End Date";
		
		//@FindBy(xpath = "//li[text()='End Date must be greater than Start Date']")
		//public String errorOnOpenRollEndDate2=;
		
		//@FindBy(xpath = "//li[text()='Calendar Start Date year should be same as Roll Year']")
		public String errorOnCalendarStartDate="Calendar Start Date";
		
		@FindBy(xpath = "//li[text()='Calendar End Date must be greater than Calendar Start Date']")
		public String errorOnCalendarEndDate1="Calendar End Date";
		
		//@FindBy(xpath = "//li[text()='Calendar End Date year should be same as Roll Year']")
		//public String errorOnCalendarEndDate2;
	
	
	//Locators added for elements on Roll Years Settings screen - Detail Page
	
	@FindBy(xpath = "//span[text() = 'Roll Year Settings']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement rollYearSettingsOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Roll Year']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement rollYearOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Calendar Start Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement calendarStartDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Calendar End Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement calendarEndDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Lien Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement lienDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Tax Start Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement taxStartDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Tax End Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement taxEndDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Status']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement statusOnDetailPage;
	
	@FindBy(xpath = "//button[contains(text(),'Cancel')]")
	public WebElement cancelButtonOnDetailPage;
	
	@FindBy(xpath = "//button[contains(text(),'Save')]")
	public WebElement saveButtonOnDetailPage;
	
	@FindBy(xpath = "//a[text()='Real Property Settings Library']")
	public WebElement rpslOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Roll Year']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]")
	public WebElement editPencilIconForRollYearOnDetailPage;
	
	
	//Locators added for elements on Roll Year Settings screen - Edit Detail Page
	
	@FindBy(xpath = "//label[text()='Roll Year Settings']//following-sibling::div//input")
	public WebElement rollYearSettingsOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Roll Year']//following-sibling::div//input")
	public WebElement rollYearOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Status']//following-sibling::div//input")
	public WebElement statusOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Calendar Start Date']//following-sibling::div//input")
	public WebElement calendarStartDateOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Calendar End Date']//following-sibling::div//input")
	public WebElement calendarEndDateOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Tax Start Date']//following-sibling::div//input")
	public WebElement taxStartDateOnDetailEditPage;
	
	
	/*	Next 4 locators are for validating error messages for duplicate Roll Year or missing details
	 *	These would be moved to common package/class
	 * */
	
	@FindBy(xpath = "//li[contains(text(), 'These required fields must be completed:')]")
	public WebElement errorMsgOnTop;
	
	@FindBy(xpath = "//li[text() = 'Complete this field'] | //span[text() = 'Complete this field']")
	private List<WebElement> errorMsgUnderLabels; 
	
	@FindBy(xpath = "//li[contains(text(),'There seems to be an existing record with a blank')]")
	public WebElement duplicateErrorMsgWithBlankEndDate;
	
	@FindBy(xpath = "//li[contains(text(),'There seems to be an existing record with overlapp')]")
	public WebElement duplicateErrorMsgWithOverlappingDetails;
	
	
	/**
	 * Description: This method will save a Roll Year record with no values entered
	 */
	public void saveRollYearRecordWithNoValues() throws Exception {
		ReportLogger.INFO("Click 'New' button to open a Roll Year record");
		Thread.sleep(2000);
		Click(waitForElementToBeClickable(newRollYearButton));
		waitForElementToBeClickable(rollYearSettings);
		waitForElementToBeClickable(rollYear);
		ReportLogger.INFO("Without entering any data on the Roll Year record, click 'Save' button");
		Click(saveButton);
		Thread.sleep(1000);
	}
	
	/**
	 * Description: This method includes other methods and creates/updates a Roll Year record
	 * @param dataMap: Map that is storing values from JSON file
	 */
	
	public void createOrUpdateRollYearRecord(Map<String, String> dataMap, String action) throws Exception {
		ReportLogger.INFO("Click '" + action + "' button to open a Roll Year record");
		Thread.sleep(1000);
		if (action == "New") Click(waitForElementToBeClickable(newRollYearButton));
		if (action == "Edit") Click(waitForElementToBeClickable(editButton));
		Thread.sleep(1000);
		waitForElementToBeClickable(rollYearSettings, 10);
		waitForElementToBeClickable(rollYear, 10);
		enterRollYearData(dataMap);
		ReportLogger.INFO("Click 'Save' button to save the details entered in Roll Year record");
		Click(saveButton);
		Thread.sleep(3000);
		
	}
	
	/**
	 * Description: This method will enter mandatory field values in Roll Year screen
	 * @param dataMap: Map that is storing values from JSON file
	 */
	
	/*public void enterRollYearData(Map<String, String> dataMap) throws Exception {
		ReportLogger.INFO("Enter the following values : " + dataMap);
		enter(rollYearSettings, dataMap.get("Roll Year Settings"));
		apasGenericObj.selectFromDropDown(rollYear, dataMap.get("Roll Year"));
		enterDate(lienDate, dataMap.get("Lien Date"));
		enterDate(taxStartDate, dataMap.get("Tax Start Date"));
		enterDate(taxEndDate, dataMap.get("Tax End Date"));
		enterDate(openRollStartDate, dataMap.get("Open Roll Start Date"));
		enterDate(openRollEndDate, dataMap.get("Open Roll End Date"));
		enterDate(calendarStartDate, dataMap.get("Calendar Start Date"));
		enterDate(calendarEndDate, dataMap.get("Calendar End Date"));	
	}*/
	
	public void enterRollYearData(Map<String, String> dataMap) throws Exception {
		ReportLogger.INFO("Enter the following values : " + dataMap);
		enter("Roll Year Settings", dataMap.get("Roll Year Settings"));
		apasGenericObj.selectFromDropDown("Roll Year", dataMap.get("Roll Year"));
		apasGenericObj.selectFromDropDown("Status", dataMap.get("Status"));
		enter("Lien Date", dataMap.get("Lien Date"));
		enter("Tax Start Date", dataMap.get("Tax Start Date"));
		enter("Tax End Date", dataMap.get("Tax End Date"));
		enter("Open Roll Start Date", dataMap.get("Open Roll Start Date"));
		enter("Open Roll End Date", dataMap.get("Open Roll End Date"));
		enter("Calendar Start Date", dataMap.get("Calendar Start Date"));
		enter("Calendar End Date", dataMap.get("Calendar End Date"));
		enter("Annual Batch Execution Date", dataMap.get("Annual Batch Execution Date"));
	}
	
	/**
	 * Description: This method will open the Roll Year record passed in the argument
	 * @param exempName: Takes Roll Year record as an argument
	 */
	public void openRollYearRecord(String rollYearName) throws IOException, InterruptedException {
		ReportLogger.INFO("Open the Roll Year record : " + rollYearName);
		Click(driver.findElement(By.xpath("//a[@title='" + rollYearName + "']")));
		Thread.sleep(3000);
	}
	
	public void openRollYearRecord(String recordId, String rollYearName) throws Exception {
		ReportLogger.INFO("Open the Roll Year record : " + rollYearName);
		String xpathStr = "//a[@data-recordid='" + recordId + "']";
	    WebElement rollYearLocator = objPage.locateElement(xpathStr, 30);
	    Click(rollYearLocator);
		Thread.sleep(3000);
	}
	
	/**
	 * @description: This method will return the error message appeared against the filed name passed in the parameter
	 * @param fieldName: field name for which error message needs to be fetched
	 */
	public String getIndividualFieldErrorMessage(String fieldName) throws Exception {
		return apasGenericObj.getIndividualFieldErrorMessage(fieldName);
	}
}
