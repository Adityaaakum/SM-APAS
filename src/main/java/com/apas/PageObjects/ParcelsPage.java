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
	
	@FindBy (xpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//flexipage-component2[@data-component-id='tem_workItemTimeline']//li[1]//*[text()='Submit for Approval']")
    public WebElement SubmittedForApprovalButton;
	
	@FindBy (xpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//flexipage-component2[@data-component-id='tem_workItemTimeline']//li[1]//*[text()='Withdraw']")
    public WebElement WithdrawButton;
	
	@FindBy (xpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//flexipage-component2[@data-component-id='tem_workItemTimeline']//li[1]//*[text()='Approve']")
    public WebElement ApprovalButton;
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
		String reference = dataMap.get("Reference") + "_" + timeStamp;
		String description = dataMap.get("Description");
		String priority = dataMap.get("Priority");
		String workItemRouting = dataMap.get("Work Item Routing");
		String dueDate = dataMap.get("Due Date");
		String dov = dataMap.get("DOV");
		String workItemOwner= dataMap.get("Work Item Owner");
		String workItemNumber;

		Click(getButtonWithText(componentActionsButtonText));
		waitForElementToBeClickable(selectOptionDropDownComponentsActionsModal);
		selectOptionFromDropDown(selectOptionDropDownComponentsActionsModal, "Create Work Item");
		Click(getButtonWithText(nextButtonComponentsActionsModal));
		waitForElementToBeClickable(workItemTypeDropDownComponentsActionsModal);

		selectOptionFromDropDown(workItemTypeDropDownComponentsActionsModal, workItemType);
		selectOptionFromDropDown(actionsDropDownLabel, actions);
		enter(referenceInputTextBoxComponentActionModal, reference);
		enter(descriptionInputTextBoxComponentActionModal, description);
		selectOptionFromDropDown(priorityDropDownComponentsActionsModal, priority);
		selectOptionFromDropDown(workItemRoutingDropDownComponentsActionsModal, workItemRouting);
		if (dueDate != null) enter(dueDateInputTextBox, dueDate);
		if (dov != null) enter(dovInputTextBox, dov);
		if (workItemOwner != null) searchAndSelectOptionFromDropDown(workItemOwnerSearchBox,workItemOwner);
		Click(getButtonWithText(nextButtonComponentsActionsModal));

		Thread.sleep(2000);

		String workItemQuery = "SELECT Name FROM Work_Item__c where Reference__C = '" + reference + "' order by Name desc limit 1";
		workItemNumber = objSalesforceAPI.select(workItemQuery).get("Name").get(0);
		ReportLogger.INFO("Work item created is " + workItemNumber  );
		
		return workItemNumber;
	}
	public String getFieldvalueFromWITimeLine(String fieldName) {
		String fieldValue="";
		String fieldXpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//flexipage-component2[@data-component-id='tem_workItemTimeline']//li[1]//*[text()='"+fieldName+"']/../following-sibling::span";
		try{
			fieldValue =driver.findElement(By.xpath(fieldXpath)).getText();
		}catch (Exception ex){
			fieldValue= "";
		}
		return fieldValue;
	}
}
