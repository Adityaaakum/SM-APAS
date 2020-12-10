package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkItemHomePage extends ApasGenericPage {

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
	Page objPageObj;
	SalesforceAPI salesforceAPI ;

	public WorkItemHomePage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericPage = new ApasGenericPage(driver);
		objPageObj=new Page(driver);
	}

	public String changeWorkPool= "Change Work Pool";
	public String changeAssignee= "Change Assignee";
	public String reasonForTransferring= "Reason for Transferring";
	
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

	@FindBy(xpath="//li[@title='Linked Items']//a[@data-label='Linked Items']")
	public WebElement linkedItemsWI;

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

	@FindBy(xpath="//a[text()='Submitted for Approval']")
	public WebElement submittedforApprovalTimeline;

	@FindBy(xpath="//div[@class='windowViewMode-maximized active lafPageHost']//button//span[text()='Mark as Current Status']")
	public WebElement markStatusCompleteBtn;
	
	@FindBy(xpath="//button[@title='Mark Complete']") 
	public WebElement btnMarkComplete;

	@FindBy(xpath="//a[@title='Exemption Limits - 2021']")
	public WebElement rpslRecord;

	@FindBy(xpath="//span[text()='Reference Data Details']")
	public WebElement referenceDetailsLabel;
	
	@FindBy(xpath="//button[@title='Approve']") 
	public WebElement btnApprove;
	
    public String ConsolidateButton="Consolidate";
    
    @FindBy (xpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@role='tab'][@data-label='Child Work Items']")
    public WebElement ChildWorkItemsTab;
   
    @FindBy (xpath="//*[@data-key='error']//..//button[@title='Close'] | //button[@title='Close error dialog']")
    public WebElement CloseErrorMsg;
    
    @FindBy(xpath="//div[not(contains(@class,'hasActiveSubtab')) and contains(@class,'oneWorkspace active')]//following::lightning-formatted-text[contains(text(),'WI')]")
	public WebElement workItemNumberDetailView;

	@FindBy(xpath = "//div[contains(@class,'approver-modal slds-modal__container')]//label[text()='Assigned To']/..//input")
	public WebElement AssignedTo;

	@FindBy(xpath = "//div[contains(@class,'approver-modal slds-modal__container')]//label[text()='Work Pool']/..//input")
	public WebElement WorkPool;

    public String SaveButton="Save";
    public String valueTextBox = "Value";

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
		return getGridDataInHashMap();
	}

	/**
	 * This method will open the work item passed in the parameter
	 *
	 * @param workItem: Work item number to be opened
	 * @throws InterruptedException 
	 **/
	public void openWorkItem(String workItem) throws IOException, InterruptedException {
		WebElement webElement = driver.findElement(By.xpath("//lightning-formatted-url//a[@title='" + workItem + "' or text()='" + workItem + "'] | //div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text()='"+workItem + "']"));
		scrollToElement(webElement);
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
		String xpath = "//a[@title='" + workItem + "' or text()='" + workItem + "']//ancestor::th//following-sibling::td//a";
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


    public WebElement searchWIinGrid(String WIName) {
    
		WebElement btnNext = null;
		List<WebElement> actualWINames = null;

		try {
			actualWINames = driver.findElementsByXPath("//table/tbody//tr/th//a[@title='" + WIName + "' or text()='" + WIName + "']");
			if(actualWINames.isEmpty()) {				
				String pageMsg = driver.findElementByXPath("//p[@class='slds-m-vertical_medium content']").getText();
				pageMsg=pageMsg.replaceAll("\\s","").trim();
				//Displaying 1 to 500 of 1128 records. Page 1 of 3.
				String[] arrSplit = pageMsg.split("\\.");
				System.out.println(arrSplit[1]);
				Pattern p = Pattern.compile("\\d");
		        Matcher m = p.matcher(arrSplit[1]);
		        String lastPageNum = null;
				while(m.find()){ 
					System.out.println(m.group());
					lastPageNum = m.group();
					}					 
		        for(int i = 0 ; i < Integer.valueOf(lastPageNum); i++) {
		        	btnNext = driver.findElementByXPath("//lightning-button/button[text()='Next']");
		        	javascriptClick(btnNext);
		        	Thread.sleep(20000);
		        	actualWINames = driver.findElementsByXPath("//table//tr[contains(.,'" + WIName + "')]");
		        	if(!actualWINames.isEmpty()) {		        				    			
		    			break;		  
		        	}
			    }
			}
		 }	
			catch (Exception e) {
				ReportLogger.INFO(e.getMessage());
						}
    	return actualWINames.get(0);
    }

	/*
	 * This method will search for the WI in the TAB GRID and click Open
	 * 
	 * @param WIName : Name of the work item
	 */
    public String searchandClickWIinGrid(String WIName) throws IOException {
    	
    	String actualWINamefrmGrid = null;
    	WebElement lnlWorkItem = null;
    	
    	lnlWorkItem = searchWIinGrid(WIName);
    	actualWINamefrmGrid = lnlWorkItem.getText();
    	javascriptClick(lnlWorkItem);
		
		return actualWINamefrmGrid;
	}


    
    public HashMap<String, ArrayList<String>>  getWorkItemDetails(String newExemptionName, String WIStatus, String WIType, String WISubType, String WIReference) throws InterruptedException {
		
		/*
		 * String query = "Select Work_Item__r.Name,Work_Item__r.Request_Type__c " +
		 * "from Work_Item_linkage__c " +
		 * "where Exemption__r.name      = '"+newExemptionName+"' " +
		 * "and Work_Item__r.Status__c   = '"+WIStatus+"' " +
		 * "and Work_Item__r.Type__c     = '"+WIType+"' " +
		 * "and Work_Item__r.Sub_Type__c = '"+WISubType+"' " +
		 * "and Work_Item__r.Reference__c ='"+WIReference+"'" ;
		 */
		salesforceAPI = new SalesforceAPI();
        
		String sqlExemption_Id = "Select Id from Exemption__c where Name = '"+newExemptionName+"'";
        HashMap<String, ArrayList<String>> response_1  = salesforceAPI.select(sqlExemption_Id);
        String Exemption_Id = response_1.get("Id").get(0);
        
        String slqWork_Item_Id = "Select Work_Item__c from Work_Item_Linkage__c where Exemption__c = '"+Exemption_Id+"'and Work_Item__r.Status__c ='"+WIStatus+"'";
        Thread.sleep(5000);
        HashMap<String, ArrayList<String>> response_2  = salesforceAPI.select(slqWork_Item_Id);
        String WorkItem_Id = response_2.get("Work_Item__c").get(0);
        
        String slqWork_Item_Details = "Select Name, Request_Type__c from Work_Item__c "+
        		                      "where Id = '"+WorkItem_Id+"' "+        		          
       		                          "and Type__c     = '"+WIType+"' " +
    		                          "and Sub_Type__c = '"+WISubType+"' " +
    		                          "and Reference__c ='"+WIReference+"'";
        HashMap<String, ArrayList<String>> response_3  = salesforceAPI.select(slqWork_Item_Details);
		return response_3 ;
         
     }

public HashMap<String, ArrayList<String>> getWorkItemDetailsForVA(String VAName, String WIStatus, String WIType,
		String WISubType, String WIReference) throws InterruptedException {

	/*
	 * String query = "Select Work_Item__r.Name,Work_Item__r.Request_Type__c " +
	 * "from Work_Item_linkage__c " +
	 * "where Value_Adjustments__r.Name  = '"+VAName+"' " +
	 * "and Work_Item__r.Status__c   = '"+WIStatus+"' " +
	 * "and Work_Item__r.Type__c     = '"+WIType+"' " +
	 * "and Work_Item__r.Sub_Type__c = '"+WISubType+"' " +
	 * "and Work_Item__r.Reference__c ='"+WIReference+"'" ;
	 */
	salesforceAPI = new SalesforceAPI();

	String sqlValueAdjustment_Id = "Select Id from Value_Adjustments__c where Name = '" + VAName + "'";
	HashMap<String, ArrayList<String>> response_1 = salesforceAPI.select(sqlValueAdjustment_Id);
	String ValueAdjustment_Id = response_1.get("Id").get(0);

	String slqWork_Item_Id = "Select Work_Item__c from Work_Item_Linkage__c where Value_Adjustments__c = '"
			+ ValueAdjustment_Id + "'";
	Thread.sleep(2000);
	HashMap<String, ArrayList<String>> response_2 = salesforceAPI.select(slqWork_Item_Id);
	String WorkItem_Id = response_2.get("Work_Item__c").get(0);

	String slqWork_Item_Details = "Select Name, Request_Type__c from Work_Item__c " + "where Id = '" + WorkItem_Id
			+ "' " + "and Status__c   = '" + WIStatus + "' " + "and Type__c     = '" + WIType + "' "
			+ "and Sub_Type__c = '" + WISubType + "' " + "and Reference__c ='" + WIReference + "'";
	HashMap<String, ArrayList<String>> response_3 = salesforceAPI.select(slqWork_Item_Details);
	return response_3;

  }

	public void clickExemptionNameLink(String ExemptionName) throws IOException {
	
		WebElement lnkExemptionName = driver.findElementByXPath("//a[text()='" + ExemptionName + "']");
		objPageObj.Click(lnkExemptionName);
	}
	
	public void clickCheckBoxForSelectingWI(String WIName) throws IOException {
	    
		  searchWIinGrid(WIName);
		
		  WebElement chkBoxWI = driver.findElementByXPath("//table/tbody//tr/th//a[@title='"+ WIName + "' or text()='"+ WIName + "']"  + "/ancestor::tr/td//input[@type='checkbox']");
	    
		//WebElement chkBoxWI = lnkWorkItem.findElement(By.xpath("/ancestor::tr/td//input[@type='checkbox']"));
		javascriptClick(chkBoxWI);
	}
	
	public String searchLinkedExemptionOrVA(String ExemptionOrVAName) {
	
		WebElement actualVAName = null;
		String actualExemptionNameFrmGrid = null;
	
		try {
			actualVAName = driver.findElementByXPath("//table/tbody//tr//a[@title='" + ExemptionOrVAName + "']");
			actualExemptionNameFrmGrid = actualVAName.getAttribute("title");
	
		} catch (Exception e) {
	
			ReportLogger.INFO(e.getMessage());
		}
	
		return actualExemptionNameFrmGrid;
	}
	
	public String searchRequestTypeNameonWIDetails(String RequestTypeName) {
	
		WebElement actualRequestTypeName = null;
		String actualRequestTypeNameFrmGrid = null;
	
		try {
			actualRequestTypeName = driver.findElement(By.xpath("//*[text()='" + RequestTypeName + "']"));
			actualRequestTypeNameFrmGrid = actualRequestTypeName.getText();
		} catch (Exception e) {
	
			ReportLogger.INFO(e.getMessage());
		}
		return actualRequestTypeNameFrmGrid;
	}
	 
	/**
	 * This method will select work item from in progress tab
	 *
	 * @param workItem :created workItem
	 **/
	 public void selectWorkItemOnHomePage(String workItem) throws IOException{
			WebElement webElementCheckBox = driver.findElement(By.xpath("//table//tr[contains(.,'" + workItem + "')]//span[@class='slds-checkbox_faux']"));
			scrollToElement(webElementCheckBox);
			Click(webElementCheckBox);			
	    }
	 public boolean searchWIInGrid(String workItem) throws IOException{
	        WebElement webElement = driver.findElement(By.xpath("//table//tr[contains(.,'" + workItem + "')]"));
	        scrollToElement(webElement);
	        return waitForElementToBeVisible(90,webElement);         
	    }
}
