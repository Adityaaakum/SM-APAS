package com.apas.PageObjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;

public class CIOTransferPage extends ApasGenericPage {
	Util objUtil;
	SalesforceAPI salesforceApi;
	MappingPage objMappingPage;
	WorkItemHomePage objWorkItemHomePage;

	public CIOTransferPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
		salesforceApi = new SalesforceAPI();
		objMappingPage = new MappingPage(driver);

	}

	public String ApnLabel = "APN";
	int counterForFailedattempts = 0;
	public String componentActionsButtonLabel = "Component Actions";
	public String copyToMailToButtonLabel = "Copy to Mail To";
	public String calculateOwnershipButtonLabel = "Calculate Ownership";
	public String checkOriginalTransferListButtonLabel = "Check Original Transfer List";
	public String finishButtonLabel = "Finish";
	public final String commonXpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]";
	public final String warningMessageArea = commonXpath + "//div[@class='slds-card slds-has-cushion flexipageRichText']//b";
	public String backToWIsButtonLabel = "Back to WIs";
	public String saveLabel ="Save";
	public String newButton="New";
	public String formattedName1Label="Formatted Name1";
	public String startDate="Start Date";
	public String endDate="End Date";
	public String mailingZip="Mailing Zip";
	public String CancelButton="Cancel";
	public String LastNameLabel="Last Name";
	public String OwnershipStartDate="Ownership Start Date";
	public String OwnershipEndDate="Ownership End Date";
	public String RecordedApnTransfer="Recorded APN Transfer";
	public String Edit="Edit";
	public String Status="Status";
	public String ownerPercentage="Owner Percentage";
	public final String DOC_DEED="DE";
	


	public String transferCodeLabel = "Transfer Code";
	public String transferDescriptionLabel = "Transfer Description";
	public String transferStatusLabel = "CIO Transfer Status";
	public String exemptionRetainLabel = "Exemption Retain";
	public String saveButton ="Save";
	public String finishButton ="Finish";
	public String nextButton="Next";
	public String cioTransferScreenSectionlabels= "//*[@class='slds-card slds-card_boundary']//span[@class='slds-truncate slds-m-right--xx-small']";
	public String remarksLabel = "Remarks";

	public static final String CIO_EVENT_CODE_COPAL="CIO-COPAL";
	public static final String CIO_EVENT_CODE_PART="CIO-PART";
	public static final String CIO_EVENT_CODE_ElessThan5Percent="E<5%";
	public static final String CIO_EVENT_CODE_CIOGOVT="CIO-GOVT";
	public static final String CIO_EVENT_CODE_BASE_YEAR_TRANSFER="CIO-P19BL";
	public static final String CIO_EVENT_CODE_BASE_YEAR_AUTOCONFIRM_CODE="CIO-P19B6";
	public static final String CIO_RESPONSE_NoChangeRequired="No Edits required";
	public static final String CIO_RESPONS_EventCodeChangeRequired="Event Code needs to be changed";
	
	
	
	
	
	

	public String eventIDLabel = "EventID";
	public String situsLabel = "Situs";
	public String shortLegalDescriptionLabel = "Short Legal Description";
	public String pucCodeLabel = "PUC Code";
	public String doeLabel = "DOE";
	public String dorLabel = "DOR";
	public String dovLabel = "DOV";
	

	
	@FindBy(xpath = "//a[@id='relatedListsTab__item']")
	public WebElement relatedListTab;

	@FindBy(xpath = "//button[@name='New'][1]")
	public WebElement NewRecordedAPNsButton;

	@FindBy(xpath = "//*[@class='flexipage-tabset']//a[1]")
	public WebElement RelatedTab;

	@FindBy(xpath = "//div[contains(@class,'uiOutputRichText')]")
	public WebElement confirmationMessageOnTranferScreen;

	@FindBy(xpath = "//div[@class='highlights slds-clearfix slds-page-header slds-page-header_record-home']//ul[@class='slds-button-group-list']//lightning-primitive-icon")
	public WebElement quickActionButtonDropdownIcon;

	@FindBy(xpath = commonXpath + "//*[@class='slds-truncate' and text()='Approve']")
	public WebElement quickActionOptionApprove;

	@FindBy(xpath = commonXpath + "//*[@class='slds-truncate' and text()='Return']")
	public WebElement quickActionOptionReturn;

	@FindBy(xpath = commonXpath + "//*[@class='slds-truncate' and text()='Submit for Review']")
	public WebElement quickActionOptionSubmitForReview;

	@FindBy(xpath = commonXpath + "//*[@class='slds-truncate' and text()='Review Complete']")
	public WebElement quickActionOptionReviewComplete;

	@FindBy(xpath = commonXpath
			+ "//*[@class='slds-truncate' and text()='Back to WIs'] | //button[text()='Back to WIs']")
	public WebElement quickActionOptionBackToWIs;

	@FindBy(xpath = commonXpath
			+ "//*[@class='slds-truncate' and text()='Submit for Approval'] | //button[text()='Submit for Approval']")
	public WebElement quickActionOptionSubmitForApproval;

	@FindBy(xpath = commonXpath + "//*[@class='slds-truncate' and text()='Back'] | //button[text()='Back']")
	public WebElement quickActionOptionBack;
	
	@FindBy(xpath =commonXpath+ "//select[@name='Formatted_Name_1']")
	public WebElement formattedName1;
	
	@FindBy(xpath = commonXpath+"//input[@name='Mailing_ZIP']")
	public WebElement mailZipCopyToMailTo;
	
	@FindBy(xpath = commonXpath+"//input[@name='Please_Enter_the_Retained_Ownership_Percentage_for_this_Owner']")
	public WebElement calculateOwnershipRetainedFeld;

	@FindBy(xpath = commonXpath + "//div[@class='slds-card slds-has-cushion flexipageRichText']//b")
	public WebElement transferPageMessageArea;
	
	@FindBy(xpath = commonXpath + "//div[text()='Recorded APN Transfer']//following::lightning-formatted-text")
	public WebElement cioTransferActivityLabel;
	

	@FindBy(xpath = "//div[@class='flowruntimeRichTextWrapper flowruntimeDisplayText']//b")
	public WebElement cioTransferSuccessMsg;
	
	@FindBy(xpath = "//*[contains(@data-value,'Reviewed Assessee Response')]")
	public WebElement reviewAssecesseLink;
	
	@FindBy(xpath = "//label[text()='Transfer Code']/..//button[@title='Clear Selection']")
	public WebElement clearSelectionEventCode;

	@FindBy(xpath = commonXpath + "//span[text() = 'CIO Transfer Grantors']/following-sibling::span")
	public WebElement numberOfGrantorLabel;
	
	@FindBy(xpath = commonXpath + "//span[text() = 'CIO Transfer Grantee & New Ownership']/following-sibling::span")
	public WebElement numberOfGranteeLabel;
	
	@FindBy(xpath = commonXpath + "//span[text() = 'CIO Transfer Mail To']/following-sibling::span")
	public WebElement numberOfMailToLabel;
	
	@FindBy(xpath = commonXpath + "//h1[text()='Ownership']")
	public WebElement ownershipLabelOnGridForGrantee;
	
	@FindBy(xpath = commonXpath+"//div[text()='Return Reason']/ancestor:: div[@class='bBody']//textarea")
	public WebElement returnReasonTextBox;
	
	@FindBy(xpath ="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//force-record-layout-section//force-record-layout-item//*[text()='CIO Transfer Status']/../..//slot[@slot='outputField']//lightning-formatted-text")
	public WebElement CIOstatus;
	
	@FindBy(xpath ="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//force-record-layout-section//force-record-layout-item//*[text()='Audit Trail']/../..//slot[@slot='outputField']//a//span")
	public WebElement CIOAuditTrail;

	@FindBy(xpath=commonXpath+"//button[text()='Finish']")
	public WebElement finishButtonPopUp;
	
	public String xpathSpinner = "//lightning-spinner";
	
	
	/*
	    * This method adds the recorded APN in Recorded-Document
	    * 
	    */
	    
	    public void addRecordedApn(String DocId,int count) throws Exception
	    {

	    	String getApnToAdd="Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) Limit "+count;
	    	HashMap<String, ArrayList<String>> hashMapRecordedApn= salesforceApi.select(getApnToAdd);
	    	
	    	if(count!=0) {
	    		
	    		navigateToRecorderDocument(DocId);
	    		Thread.sleep(3000);
	            objMappingPage.Click(relatedListTab);   
	      
	        if(!hashMapRecordedApn.isEmpty())
	        {
	        	
	          hashMapRecordedApn.get("Name").stream().forEach(Name->{
	        		
						try {
							Click(NewRecordedAPNsButton);
							enter(ApnLabel, Name);
							selectOptionFromDropDown(ApnLabel, Name);
							Click(getButtonWithText(SaveButton));
							driver.navigate().back();
							driver.navigate().back();
							ReportLogger.INFO("Recorded APN Name added "+Name);
							
						} catch (Exception e) {
						ReportLogger.INFO("UNABLE TO ADD RECORDED APN!!");	
						}						   		
	        	});       	
	        }
	    	}	 	   
	    }
	    
	    /*
	     * 
	     * This method triggers the job to get the desired WI for given document type and APN count
	     */
	    
	    public void generateRecorderJobWorkItems(String DocType,int ApnCount) throws IOException
	    {
	    	   	
	    	String fetchDocId ="SELECT id from recorded_document__c where recorder_doc_type__c='"+DocType+"'"+" and xAPN_count__c="+ApnCount;
	    	try
	    	{   
	    		if(getRecordedDocumentId(DocType, ApnCount)!=null && ApnCount>=0)
	    		{
	    		String recorderDocId=getRecordedDocumentId(DocType,ApnCount); 
	    		
	    		markPendingRecordedDocsAsProcessed();    		
	    		addRecordedApn(recorderDocId, counterForFailedattempts);
	    		salesforceApi.update("recorded_document__c" , recorderDocId, "Status__c","Pending");
	    		ReportLogger.INFO("Marking "+recorderDocId+"in Pending state");
	    		salesforceApi.generateReminderWorkItems(SalesforceAPI.RECORDER_WORKITEM);
	    		ReportLogger.INFO("-------------Generated Recorded WorkItems.------------------"); 
	    		counterForFailedattempts=0;
	    		Thread.sleep(5000);
	    		return;
	    		}
	    		if(ApnCount<0)
	    		{
	    			throw new Exception();
	    		}
	    		++counterForFailedattempts;
	    		generateRecorderJobWorkItems(DocType, ApnCount-1);
	    		
	    	}
	    	catch (Exception e) {
	    		/*
	    		 * Ability to handle situations when there are no requested documents with the given number of  recorded APN's is out of scope for now and can be developed later.
	    		 * 
	    		 */
	    		counterForFailedattempts=0;	    		
	    		ReportLogger.INFO("SORRY!! NO RECORDER DOC FOUND WITH THE GIVEN TYPE AND APN COUNT");    		 		
	    		
			}
	    	
	    }
	    /*
	     * 
	     * This is an overloaded version that generates WI based on  only RecordedDocumentID,this method is more emphasised when particular APN data needs to be used for a given recorded document
	     */
	    
	    public void generateRecorderJobWorkItems(String RecordedDocumentId) throws IOException
	    {
	    	try {
	    	markPendingRecordedDocsAsProcessed();
	    	salesforceApi.update("recorded_document__c" , RecordedDocumentId, "Status__c","Pending");
		ReportLogger.INFO("Marking "+RecordedDocumentId+"in Pending state");
		salesforceApi.generateReminderWorkItems(SalesforceAPI.RECORDER_WORKITEM);
		Thread.sleep(5000);
		ReportLogger.INFO("-------------Generated Recorded WorkItems.------------"); 
	    	
	    	}
	    	catch (Exception e) {
	    		ReportLogger.INFO("SORRY!! WorkItem cannot be genrated");
			}	    	
	    }
	    
	    
	    
	    /*
	     * This methods marks all the pending recorder doc's to processed
	     * 
	     */
	    public void markPendingRecordedDocsAsProcessed() {
	    	
	    	salesforceApi.update("recorded_document__c" , "Select Id from recorded_document__c where status__c='Pending'", "Status__c","Processed");
			ReportLogger.INFO("Marking all the recorded document to processed state");
			
		}
	    
	    /*
	     * This method navigates to the recorded document UI
	     */
	    public void navigateToRecorderDocument(String id) throws InterruptedException
	    {
	    	
	    	String executionEnv = "";
			
			if (System.getProperty("region").toUpperCase().equals("QA"))
				executionEnv = "qa";
			if (System.getProperty("region").toUpperCase().equals("E2E"))
				executionEnv = "e2e";
			if (System.getProperty("region").toUpperCase().equals("PREUAT"))
				executionEnv = "preuat";
			if (System.getProperty("region").toUpperCase().equals("STAGING"))
				executionEnv = "staging";		
			
			driver.navigate().to("https://smcacre--"+executionEnv+
					 ".lightning.force.com/lightning/r/Recorded_Document__c/"+id+"/view");
			ReportLogger.INFO("https://smcacre--"+executionEnv+
					 ".lightning.force.com/lightning/r/Recorded_Document__c/"+id+"/view");
			Thread.sleep(5000);
	    	
	    }
	    
	    
	   /*
	    * This method returns the DocId with required no of apns
	    *  
	    */
	   public String getRecordedDocumentId(String type,int count)
	   {
		   String documentId=null;
		   String fetchDocId ="SELECT id from recorded_document__c where recorder_doc_type__c='"+type+"'"+" and xAPN_count__c="+count;
		   if(count!=0) {
		   
		        if(salesforceApi.select(fetchDocId)!= null)
		        {
		        	documentId=salesforceApi.select(fetchDocId).get("Id").get(0);
		        	 if(salesforceApi.select("SELECT ID FROM Recorded_APN__c WHERE RECORDED_DOCUMENT__C='"+documentId+"'"+" AND PARCEL__C != NULL ").size()==0)
		  		   {
		  			   return null;
		  		   }
		        	
		        }
		   }
		   
		   	   
		  return salesforceApi.select(fetchDocId).get("Id").get(0);
	   }	    
	   
	   /*
	    * This method will fetch recorded APN from recorded document
	    * @param RecordedDocId: recorded document number
	    * 
	    */
	    
		public HashMap<String, ArrayList<String>>getAPNNameFromRecordedDocument(String RecordedDocId)
		{
			
			return salesforceApi.select("select Name from Parcel__c where Id in (SELECT Parcel__c FROM Recorded_APN__c where Recorded_document__c ='"+RecordedDocId+"'");
		}
		
		/**
	     * Description: This method will click the pencil icon edit button against the label passed as argument
	     * @param labelName: Takes Label Name as an argument
	     */
		 public void editRecordedApnField(String labelName) throws Exception {
		        ReportLogger.INFO("Edit the field : " + labelName);
		        Thread.sleep(1000);
		        String xpathStr = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//span[text() = '" + labelName + "']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]";		        
		        WebElement fieldLocator = locateElement(xpathStr, 30);
		        Click(fieldLocator);
		        Thread.sleep(1000);
		    }
		 
		 
		 /*
		  * This method creates mail to record by clicking on copy to mail to action button on CIO transfer screen
		  * 
		  */
		 
		 public void createCopyToMailTo(String granteeForMailTo,Map<String, String> dataToCreateMailTo) throws IOException, Exception {		 		 
			 try {
			   waitForElementToBeClickable(7, copyToMailToButtonLabel);			   
			   Click(getButtonWithText(copyToMailToButtonLabel));
			   waitForElementToDisappear(formattedName1, 5);
			   Click(formattedName1);
			   Select select= new Select(formattedName1);
			   select.selectByVisibleText(granteeForMailTo);
			   Click(formattedName1);		   
			   enter(mailZipCopyToMailTo, dataToCreateMailTo.get("Mailing Zip"));
			   Click(getButtonWithText(nextButton));
			   ReportLogger.INFO("Generated mail to record from Copy to mail  quick action button");}
			 catch (Exception e) {
				ReportLogger.INFO("SORRY!! MAIL TO RECORD CANNOT BE ADDED THROUGH COPY TO MAIL TO ACTION BUTTON");
			}
		 }
		 
		 
		 /*
		  * Deleting existing Grantee from particular recorded-document .
		  * 
		  */
		 
		 public void deleteOldGranteesRecords(String recordedocId) throws IOException, Exception
		 {      	       
			   HashMap<String, ArrayList<String>>HashMapOldGrantee =salesforceApi.select("SELECT Id FROM Transfer__c where recorded_document__c='"+recordedocId+"'");			      
		        if(!HashMapOldGrantee.isEmpty()) {		    	  
		    	  HashMapOldGrantee.get("Id").stream().forEach(Id ->{
	    		  objSalesforceAPI.delete("Transfer__c", Id);
	    		  ReportLogger.INFO("!!Deleted grantee with id= "+Id);
		          } );}	 
		 }
		 
		 /*
		  * This method will create one new grantee per method call in Recorded APN transfer screen.
		  * 
		  */
		 
		 public void createNewGranteeRecords(String recordeAPNTransferID,Map<String, String>dataToCreateGrantee ) throws Exception
		 {
			 try {
			   String execEnv= System.getProperty("region");			 
			   driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+"/related/CIO_Transfer_Grantee_New_Ownership__r/view");			   
			   waitForElementToBeVisible(5,newButton);
			   Click(getButtonWithText(newButton));
			   enter(LastNameLabel, dataToCreateGrantee.get("Last Name"));	
			   if(dataToCreateGrantee.get("Owner Percentage")!=null)
			   enter(ownerPercentage,dataToCreateGrantee.get("Owner Percentage"));	   	
			   Click(getButtonWithText(saveButton));
			   Thread.sleep(3000);
			   ReportLogger.INFO("GRANTEE RECORD ADDED!!");	}
			   catch (Exception e) {
			    ReportLogger.INFO("SORRY!! GRANTEE RECORD CANNOT BE ADDED");
			}
		 }
		 
		 public void deleteRecordedApnFromRecordedDocument(String recordedDocumentId)
		 {
			       HashMap<String, ArrayList<String>>HashMapRecordedDocuments = salesforceApi.select("SELECT ID FROM RECORDED_APN__C WHERE Recorded_Document__c='"+recordedDocumentId+"'");
			     
			       if(!HashMapRecordedDocuments.isEmpty());
			       {
			    	       HashMapRecordedDocuments.get("Id").stream().forEach(Id->{			    		   
			    		   salesforceApi.delete("Recorded_APN__c", Id); 
			    		   ReportLogger.INFO("Recorded APN Deleted "+Id);
			    	 });
			       }
			 
			 
			 
		 }
		 
		 /*
		  * Description : This method will click 'View All' button on RAT screen under the Grid
		  * Param : Grid Name 
		  *
		  */
		 
		 public void clickViewAll(String gridName) throws Exception{
			 ReportLogger.INFO("Click View ALL button under "+ gridName);
			 String updateGridName="";
			 if (gridName.contains("CIO Transfer Grantors"))updateGridName = "CIO_Transfer_Grantor"; 
			 if (gridName.contains("CIO Transfer Grantee & New Ownership"))updateGridName = "CIO_Transfer_Grantee_New_Ownership";
			 if (gridName.contains("CIO Transfer Mail To"))updateGridName = "CIO_Transfer_Mail_To";
			 if (gridName.contains("Ownership for Parent Parcel"))updateGridName = "Property_Ownerships";
			 
			 String xpathStr = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//a[contains(@href,'" + updateGridName + "')]//span[text() = 'View All']";		        
		 	 WebElement fieldLocator1 = locateElement(xpathStr, 30);
		 	 Click(fieldLocator1);
		 	 Thread.sleep(5000);
		 }
		 	
}
