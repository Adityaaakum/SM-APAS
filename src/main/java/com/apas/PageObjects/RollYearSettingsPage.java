package com.apas.PageObjects;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;

public class RollYearSettingsPage extends ApasGenericPage {
	
	Util objUtil;
	ApasGenericPage objApasGenericPage;
	SalesforceAPI objSalesforceAPI;
	Page objPage;

	public RollYearSettingsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
		objApasGenericPage = new ApasGenericPage(driver);
		objSalesforceAPI = new SalesforceAPI();
		objPage=new Page(driver);
	}
	
	
	//Locators added for elements on Roll Year Settings screen

	@FindBy(xpath = "//a[@title='New']/div[@title='New'][1]")
	public WebElement newRollYearButton;
	
	@FindBy(xpath = "//span[text()='Recently Viewed']")
	public WebElement recentlyViewedListView;
	
	public String rollYearSettings="Roll Year Settings";

	public String rollYear="Roll Year";

	public String openRollStartDate="Open Roll Start Date";

	public String openRollEndDate="Open Roll End Date";

	public String calendarStartDate="Calendar Start Date";

	public String calendarEndDate="Calendar End Date";
	
	public String annualBatchExecutionDate="Annual Batch Execution Date";

	public String lienDate="Lien Date";

	public String taxStartDate="Tax Start Date";

	public String taxEndDate="Tax End Date";

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
	
	public String errorOnLienDate="Lien Date";
		
	public String errorOnTaxStartDate="Tax Start Date";
		
	public String errorOnTaxEndDate="Tax End Date";
		
	public String errorOnOpenRollStartDate="Open Roll Start Date";
		
	public String errorOnOpenRollEndDate1="Open Roll End Date";
		
	public String errorOnCalendarStartDate="Calendar Start Date";
		
	@FindBy(xpath = "//li[text()='Calendar End Date must be greater than Calendar Start Date']")
	public String errorOnCalendarEndDate1="Calendar End Date";
		
	
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
		Thread.sleep(2000);
		ReportLogger.INFO("Click '" + action + "' button to open a Roll Year record");
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
	
	public void enterRollYearData(Map<String, String> dataMap) throws Exception {
		ReportLogger.INFO("Enter the following values : " + dataMap);
		enter(rollYearSettings, dataMap.get("Roll Year Settings"));
		selectFromDropDown(rollYear, dataMap.get("Roll Year"));
		selectFromDropDown(status, dataMap.get("Status"));
		enter(lienDate, dataMap.get("Lien Date"));
		enter(taxStartDate, dataMap.get("Tax Start Date"));
		enter(taxEndDate, dataMap.get("Tax End Date"));
		enter(openRollStartDate, dataMap.get("Open Roll Start Date"));
		enter(openRollEndDate, dataMap.get("Open Roll End Date"));
		enter(calendarStartDate, dataMap.get("Calendar Start Date"));
		enter(calendarEndDate, dataMap.get("Calendar End Date"));
		enter(annualBatchExecutionDate, dataMap.get("Annual Batch Execution Date"));
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
		return getIndividualFieldErrorMessage(fieldName);
	}
}
