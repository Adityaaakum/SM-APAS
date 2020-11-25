package com.apas.PageObjects;

import android.text.style.ClickableSpan;
import com.apas.Reports.ReportLogger;
import com.apas.generic.ApasGenericFunctions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WorkItemHomePage extends Page {

	public final String TAB_IN_PROGRESS = "In Progress";
	public final String TAB_IN_POOL = "In Pool";
	public final String TAB_MY_SUBMITTED_FOR_APPROVAL = "Submitted for Approval";
	public final String TAB_NEED_MY_APPROVAL = "Needs My Approval";
	public final String TAB_COMPLETED = "Completed";
	public final String TAB_On_Hold = "On Hold";
	public final String TAB_StaffInProgress = "Staff - In Progress";
	public final String TAB_StaffOnHold = "Staff - On Hold";
	public final String TAB_StaffInPool = "Staff - In Pool";

	ApasGenericPage objApasGenericPage;
	ApasGenericFunctions objApasGenericFunctions;
	Page objPageObj;

	public WorkItemHomePage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericPage = new ApasGenericPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objPageObj=new Page(driver);
	}

	@FindBy(xpath = "//div[@data-key='success'][@role='alert']")
	public WebElement successAlert;

	@FindBy(xpath = "//label[contains(.,'Show RP')]//span[@class='slds-checkbox_faux_container']")
	public WebElement chkShowRP;

	@FindBy(xpath = "//button[text()='Accept Work Item']")
	public WebElement acceptWorkItemButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@data-tab-name='Completed']")
	public WebElement dataTabCompleted;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[contains(.,'Mark Status as Complete')]")
	public WebElement markStatusAsCompleteButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[contains(.,'Mark as Current Status')]")
	public WebElement markAsCurrentStatusButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@role='tab'][@data-label='Details']")
	public WebElement detailsTab;

	@FindBy(xpath = "//a[@role='tab'][@data-label='In Progress']")
	public WebElement inProgressTab;

	@FindBy(xpath = "//a[@role='tab'][@data-label='Completed']")
	public WebElement completedTab;

	@FindBy(xpath = "//a[@role='tab'][@data-label='In Pool']")
	public WebElement inPoolTab;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//force-record-layout-item[contains(.,'Related Action')]//a[@target='_blank']")
	public WebElement reviewLink;

	@FindBy(xpath = "//a[@role='tab'][@data-label='Needs My Approval']")
	public WebElement needsMyApprovalTab;

	@FindBy(xpath = "//button[text()='Return']")
	public WebElement returnWorkItemButton;

	@FindBy(xpath = "//button[text() = 'Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//label[text()='Returned Reason']//following-sibling::div//input")
	public WebElement returnedReasonTxtBox;
	
	@FindBy(xpath = "//table[@role='grid']//span[text()='Action']")
	public WebElement actionColumn;
	
	@FindBy(xpath = "//*[@aria-labelledby='In Progress__item']//span[text()='Action']")
	public WebElement actionColumnInProgressTab;

	public String linkedItemEFileIntakeLogs = "//flexipage-tab2[contains(@class,'slds-show')]//c-org_work-item-related-list[contains(.,'E File Intake Logs')]";

	public String relatedBuildingPermits = "//flexipage-tab2[contains(@class,'slds-show')]//c-org_work-item-related-list[contains(.,'Related Building Permits')]";
	
	@FindBy(xpath="//a[@title='Home']")
	public WebElement lnkTABHome;

	@FindBy(xpath="//a/span[text()='Work Items']")
	public WebElement lnkTABWorkItems;  

	@FindBy(xpath="//a[@data-label='In Progress']")
	public WebElement lnkTABInProgress;

	@FindBy(xpath="//a[@data-label='In Pool']")
	public WebElement lnkTABInPool;

	@FindBy(xpath="//a[@data-label='Submitted for Approval']")
	public WebElement lnkTABMySubmittedforApproval;

	@FindBy(xpath="//a[@data-label='Completed']")
	public WebElement lnkTABCompleted;

	@FindBy(xpath="//input[@placeholder='Search Work Pool']")
	public WebElement txtSearchWorkPool;

	@FindBy(xpath="//input[@name='status']")
	public WebElement selStatus;

	@FindBy(xpath="//input[@name='type']")
	public WebElement selType;

	@FindBy(xpath="//input[@name='action']")
	public WebElement selAction;

	@FindBy(xpath="//label[contains(.,'Show RP')]//span[@class='slds-checkbox_faux']")
	public WebElement toggleBUtton;

	@FindBy(xpath="//td[@data-label='Request Type' and contains(.,'Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits')]//parent::tr/th//span")
	public WebElement reminderWINameLink;

	@FindBy(xpath="//lightning-tab[@aria-labelledby='In Pool__item']//td[@data-label='Request Type' and contains(.,'Disabled Veterans - Review and Update - Annual exemption amount verification')]//parent::tr/th//span")
	public List<WebElement> lowIncomeInPoolWILinks;

	@FindBy(xpath="//lightning-tab[@aria-labelledby='In Pool__item']//td[@data-label='Request Type' and contains(.,'Disabled Veterans - Review and Update - Annual exemption amount verification')]//parent::tr/td[1]//input[@type='checkbox']/parent::span")
	public List<WebElement> lowIncomeInPoolWIbox;

	@FindBy(xpath="//lightning-tab[@aria-labelledby='In Progress__item']//td[@data-label='Request Type' and contains(.,'Disabled Veterans - Review and Update - Annual exemption amount verification')]//parent::tr/th//span")
	public List<WebElement> lowIncomeInProgressWILinks;

	@FindBy(xpath="//lightning-tab[@aria-labelledby='In Progress__item']//td[@data-label='Request Type' and contains(.,'Disabled Veterans - Review and Update - Annual exemption amount verification')]//parent::tr/td[2]//input[@type='checkbox']/parent::span")
	public List<WebElement> lowIncomeInProgressWIbox;

	@FindBy(xpath="//lightning-tab[@aria-labelledby='In Progress__item']//td[@data-label='Request Type' and contains(.,'Disabled Veterans - Review and Update - Annual exemption amount verification')]//parent::tr/td[2]//input[@type='checkbox']/parent::span")
	public List<WebElement> lowincomeSubmittedWI;

	@FindBy(xpath="//div[@class='windowViewMode-maximized active lafPageHost']//div[@class='slds-truncate']//a")
	public WebElement linkedItemsRecord;

	@FindBy(xpath="//button[@title='Accept Work Item']")
	public WebElement acceptWorkItemBtn;

	@FindBy(xpath="//div[@class='pageLevelErrors']//ul[1]")
	public WebElement errorMsg;

	@FindBy(xpath="//li[@title='Details']//a[@data-label='Details']")
	public WebElement detailsWI;

	@FindBy(xpath="//div[@class='windowViewMode-maximized active lafPageHost']//*[@class='test-id__field-label' and text()='Related Action']/parent::div/following-sibling::div//a")
	public WebElement relatedActionLink;

	@FindBy(xpath = "//div[@class='windowViewMode-maximized active lafPageHost']//*[@class='test-id__field-label' and text()='Status']/parent::div/following-sibling::div//lightning-formatted-text")
	public WebElement wiStatusDetailsPage;

	@FindBy(xpath = "//li//a[@aria-selected='true' and @role='option']")
	public WebElement currenWIStatusonTimeline;

	@FindBy(xpath = "//div[@class='pageLevelErrors']//li")
	public WebElement pageLevelErrorMsg;

	@FindBy(xpath = "//button[contains(.,'Cancel')]")
	public WebElement cancelBtn;

	@FindBy(xpath = "//div[@class='windowViewMode-maximized active lafPageHost']//button[@name= 'Edit']")
	public WebElement editBtn;

	@FindBy(xpath ="//div[@class='windowViewMode-maximized active lafPageHost']//span[text()='Roll Year Settings']//parent::div/following-sibling::lightning-helptext/following-sibling::div//slot//a")
	public WebElement vaRollYear;

	@FindBy(xpath="//a[@title='Submitted for Approval']//span[text()='Submitted for Approval']")
	public WebElement submittedforApprovalTimeline;

	@FindBy(xpath="//div[@class='windowViewMode-maximized active lafPageHost']//button//span[text()='Mark as Current Status']")
	public WebElement markStatusCompleteBtn;

	@FindBy(xpath="//a[@title='Exemption Limits - 2021']")
	public WebElement rpslRecord;

	@FindBy(xpath="//span[text()='Reference Data Details']")
	public WebElement referenceDetailsLabel;
	
	/**
	 * This method will return grid data from the work item home page tab passed in the parameter
	 *
	 * @param tabName : Tab name for which data needs to be fetched
	 **/
	public HashMap<String, ArrayList<String>> getWorkItemData(String tabName) throws Exception {
		String xpath = "//a[@role='tab'][@data-label='" + tabName + "']";
		waitUntilElementIsPresent(xpath, 10);
		WebElement webElement = driver.findElement(By.xpath(xpath));
		Click(webElement);
		Thread.sleep(2000);
		return objApasGenericFunctions.getGridDataInHashMap();
	}

	/**
	 * This method will open the work item passed in the parameter
	 *
	 * @param workItem: Work item number to be opened
	 * @throws InterruptedException 
	 **/
	public void openWorkItem(String workItem) throws IOException, InterruptedException {
		WebElement webElement = driver.findElement(By.xpath("//lightning-formatted-url//a[@title='" + workItem + "']"));
		javascriptClick(webElement);
		Thread.sleep(3000);

	}

	/**
	 * This method will open related action record linked with the work item
	 *
	 * @param workItem: Work item number linked with the item
	 **/
	public void openRelatedActionRecord(String workItem) throws Exception {
		ReportLogger.INFO("Opening the Related action window linked with work item : " + workItem);
		String xpath = "//a[@title='" + workItem + "']";
		waitUntilElementIsPresent(xpath, 15);
		//waitForElementToBeClickable(driver.findElement(By.xpath(xpath)), 10);
		javascriptClick(driver.findElement(By.xpath(xpath)));
		Thread.sleep(3000);
		//Click(detailsTab);
		javascriptClick(detailsTab);
		scrollToBottom();
		javascriptClick(reviewLink);
		Thread.sleep(4000);
		objPageObj.waitUntilPageisReady(driver);

	}

	/**
	 * This method will accept the work item passed in the parameter
	 *
	 * @param workItem: Work item number to be accepted
	 **/
	public void acceptWorkItem(String workItem) throws Exception {
		ReportLogger.INFO("Accepting the work item: " + workItem);
		WebElement webElementCheckBox = driver.findElement(By.xpath("//table//tr[contains(.,'" + workItem + "')]//span[@class='slds-checkbox_faux']"));
		scrollToElement(webElementCheckBox);
		Click(webElementCheckBox);
		scrollToElement(acceptWorkItemButton);
		Click(acceptWorkItemButton);
		objPageObj.waitForElementToDisappear(objApasGenericPage.xpathSpinner,20);
		waitForElementToBeVisible(successAlert, 20);
		waitForElementToDisappear(successAlert, 10);
	}

	/**
	 * This method will completed the work item currently displayed on UI
	 **/
	public String completeWorkItem() throws Exception {
		ReportLogger.INFO("Completing the work item");
		javascriptClick(dataTabCompleted);
		javascriptClick(markAsCurrentStatusButton);
		waitForElementToBeVisible(successAlert, 20);
		String messageText = successAlert.getText();
		waitForElementToDisappear(successAlert, 10);
		return messageText;
	}

	/**
	 * This method will open record under Action link for the specified work item
	 *
	 * @param workItem: Work item number linked with the item
	 **/
	public void openActionLink(String workItem) throws Exception {
		ReportLogger.INFO("Clicking on Action Link of the work item : " + workItem);
		String xpath = "//a[@title='" + workItem + "']//ancestor::th//following-sibling::td//a";
		waitUntilElementIsPresent(xpath, 15);
		waitForElementToBeClickable(driver.findElement(By.xpath(xpath)), 10);
		javascriptClick(driver.findElement(By.xpath(xpath)));
		Thread.sleep(4000);
	}

	/**
	 * This method will reject the work item passed in the parameter
	 *
	 * @param workItem: Work item number to be returned
	 **/
	public void returntWorkItem(String workItem, String returnReason) throws Exception {
		ReportLogger.INFO("Rejecting the work item: " + workItem);
		WebElement webElementCheckBox = driver.findElement(By.xpath("//table//tr[contains(.,'" + workItem + "')]//span[@class='slds-checkbox_faux']"));
		scrollToElement(webElementCheckBox);
		Click(webElementCheckBox);
		scrollToElement(returnWorkItemButton);
		Click(returnWorkItemButton);
		enter(returnedReasonTxtBox,returnReason);
		Click(saveButton);
		waitForElementToBeVisible(successAlert, 20);
		waitForElementToDisappear(successAlert, 10);
	}

	
}
