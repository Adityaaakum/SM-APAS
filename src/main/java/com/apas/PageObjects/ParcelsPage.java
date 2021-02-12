package com.apas.PageObjects;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.io.IOException;
import java.util.Map;

public class ParcelsPage extends ApasGenericPage {
	Util objUtil;
	SalesforceAPI objSalesforceAPI = new SalesforceAPI();

	public ParcelsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	public String componentActionsButtonText = "Component Actions";
	public String nextButtonComponentsActionsModal = "Next";
	public String parcelMapInGISPortal = "Parcel Map in GIS Portal";

	public String workItemTypeDropDownComponentsActionsModal = "Work Item Type";
	public String referenceInputTextBoxComponentActionModal = "Reference";
	public String descriptionInputTextBoxComponentActionModal = "Description";
	public String dueDateInputTextBox = "Due_Date";
	public String dovInputTextBox = "DOV";
	public String actionsDropDownLabel = "Actions";
	public String selectOptionDropDownComponentsActionsModal = "Select Option";
	public String priorityDropDownComponentsActionsModal = "Priority";
	public String workItemRoutingDropDownComponentsActionsModal = "Work Item Routing";
	public String workItemOwnerSearchBox = "Work Item Owner (if someone other than you)";

	@FindBy(xpath = "//p[text()='Primary Situs']/../..//force-hoverable-link")
	public WebElement linkPrimarySitus;

	@FindBy(xpath = "//li[not(contains(@style,'visibility: hidden'))]//*[@title='More Tabs']")
	public WebElement moretab;

	@FindBy(xpath = "//*[@role='menuitem' and contains(.,'Exemptions')]")
	public WebElement exemptionRelatedList;

	@FindBy(xpath = "//div[contains(@class,'flowruntime-input-error')]//span")
	public WebElement descriptionError;

	@FindBy(xpath = "//div[contains(@class,'ErrorText')]")
	public WebElement workItemTypeAndSubTypeError;

	@FindBy (xpath= "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container')]//li[1]//button[@title='Toggle details for work item']")
	public WebElement ExpendWIOnParcels;
	
	@FindBy(xpath = "//button[contains(text(),'Advanced')]")
	public WebElement advancedButton;
	
	@FindBy(xpath = "//*[contains(text(),'Proceed to')]")
	public WebElement proceedButton;
	
	@FindBy(xpath = "//button[contains(text(),'Open Assessor')]")
	public WebElement openAsessorsMapButton;
	
    public String SubmittedForApprovalButton="Submit for Approval";
	
    public String WithdrawButton="Withdraw";
	
    public String ApprovalButton="Approve";
	/**
	 * Description: This method will open the parcel with the APN passed in the
	 * parameter
	 * 
	 * @param APN: Value in the APN column
	 */
	public void openParcel(String APN) throws IOException, InterruptedException {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening the parcel with APN : " + APN);
		Click(driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@title='" + APN + "']")));
		Thread.sleep(2000);
	}
	/**
	 * @Description: This method will fill all the fields in manual work item modal and create a work item
	 * @param dataMap: A data map which contains data to create work item
	 * @throws Exception
	 */
	public String createWorkItem(Map<String, String> dataMap) throws Exception {
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String workItemType = dataMap.get("Work Item Type");
		String actions = dataMap.get("Actions");
		String reference = dataMap.get("Reference");
		String description = dataMap.get("Description") + "_" + timeStamp;
		String priority = dataMap.get("Priority");
		String workItemRouting = dataMap.get("Work Item Routing");
		String dueDate = dataMap.get("Due Date");
		String dov = dataMap.get("DOV");
		String workItemOwner= dataMap.get("Work Item Owner");
		String workItemNumber;
		
		waitForElementToBeClickable(getButtonWithText(componentActionsButtonText));
		Click(getButtonWithText(componentActionsButtonText));
		waitForElementToBeClickable(selectOptionDropDownComponentsActionsModal);
		selectOptionFromDropDown(selectOptionDropDownComponentsActionsModal, "Create Work Item");
		Click(getButtonWithText(nextButtonComponentsActionsModal));
		waitForElementToBeClickable(workItemTypeDropDownComponentsActionsModal);
		
		selectOptionFromDropDown(workItemTypeDropDownComponentsActionsModal, workItemType);
		selectOptionFromDropDown(actionsDropDownLabel, actions);

		if (reference != null)enter(referenceInputTextBoxComponentActionModal, reference);
		enter(descriptionInputTextBoxComponentActionModal, description);
		selectOptionFromDropDown(priorityDropDownComponentsActionsModal, priority);
		selectOptionFromDropDown(workItemRoutingDropDownComponentsActionsModal, workItemRouting);
		if (dueDate != null) enter(dueDateInputTextBox, dueDate);
		if (dov != null) enter(dovInputTextBox, dov);
		if (workItemOwner != null) searchAndSelectOptionFromDropDown(workItemOwnerSearchBox,workItemOwner);
		Click(getButtonWithText(nextButtonComponentsActionsModal));
		Thread.sleep(2000);

		String workItemQuery = "SELECT Name FROM Work_Item__c where Description__c = '" + description + "' order by Name desc limit 1";
		workItemNumber = objSalesforceAPI.select(workItemQuery).get("Name").get(0);
		ReportLogger.INFO("Work item created is " + workItemNumber  );
		
		return workItemNumber;
	}
	
}