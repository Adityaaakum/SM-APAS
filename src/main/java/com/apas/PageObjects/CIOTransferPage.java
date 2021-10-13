package com.apas.PageObjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import com.apas.Assertions.SoftAssertion;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class CIOTransferPage extends ApasGenericPage  implements modules,users{
	Util objUtil;
	SalesforceAPI salesforceApi;
	MappingPage objMappingPage;
	WorkItemHomePage objWorkItemHomePage;
    ParcelsPage	objParcelsPage;
    SoftAssertion softAssert;
   

	public CIOTransferPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
		salesforceApi = new SalesforceAPI();
		objMappingPage = new MappingPage(driver);
        objParcelsPage = new ParcelsPage(driver);
        softAssert = new SoftAssertion();
        objWorkItemHomePage= new WorkItemHomePage(driver);
        
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
	public String formattedName1LabelForParcelMailTo="Formatted Name 1";
	public String startDate="Start Date";
	public String endDate="End Date";
	public String emailId="Email";
	public String mailingZip="Mailing Zip";
	public String CancelButton="Cancel";
	public String LastNameLabel="Last Name";
	public String careOfLabel="Care Of";
	public String OwnershipStartDate="Ownership Start Date";
	public String OwnershipEndDate="Ownership End Date";
	public String RecordedApnTransfer="Recorded APN Transfer";
	public String Edit="Edit";
	public String Status="Status";
	public String ownerPercentage="Owner Percentage";
	public final String DOC_DEED="DE";
	public String firstNameLabel="First Name";
	
	public String transferCodeLabel = "Transfer Code";
	public String transferDescriptionLabel = "Transfer Description";
	public String transferStatusLabel = "CIO Transfer Status";
	public String exemptionRetainLabel = "Exemption Retain";
	public String saveButton ="Save";
	public String finishButton ="Finish";
	public String nextButton="Next";
	public String cioTransferScreenSectionlabels= "//*[@class='slds-card slds-card_boundary']//span[@class='slds-truncate slds-m-right--xx-small']";
	public String remarksLabel = "Remarks";
	public String fieldsInCalculateOwnershipModal="//*[@id='wrapper-body']//flowruntime-screen-field//p";
	public String ownershipPercentage ="Ownership Percentage";
	public String auditTrailLabel ="Audit Trail";


	public String approveButton ="Approve";

	
	public static final String CIO_EVENT_CODE_COPAL="CIO-COPAL";
	public static final String CIO_EVENT_CODE_PART="CIO-PART";
	public static final String CIO_EVENT_CODE_ElessThan5Percent="E<5%";
	public static final String CIO_EVENT_CODE_CIOGOVT="CIO-GOVT";
	public static final String CIO_EVENT_CODE_BASE_YEAR_TRANSFER="CIO-P19BL";
	public static final String CIO_EVENT_CODE_BASE_YEAR_AUTOCONFIRM_CODE="CIO-P19B6";
	public static final String CIO_RESPONSE_NoChangeRequired="No Edits required";
	public static final String CIO_RESPONS_EventCodeChangeRequired="Event Code needs to be changed";
	public static final String APPRAISAL_NORMAL_ENROLLMENT="Normal Enrollment";
	
	
	public String eventIDLabel = "EventID";
	public String situsLabel = "Situs";
	public String shortLegalDescriptionLabel = "Short Legal Description";
	public String pucCodeLabel = "PUC Code";
	public String doeLabel = "DOE";
	public String dorLabel = "DOR";
	public String originalTransferor = "Original Transferor";
	public String vestingType = "Vesting Type";
	public String dovLabel = "DOV";

	public String xpathShowMoreLinkForEditOption = "//table//tbody/tr//td//span[text() = 'propertyName']//parent::span//parent::td//following-sibling::td//a[@role = 'button']";

	public String documentTypeLabel = "Document Type";
	public String apnCountLabel = "APN Count";
	public String transferTaxLabel = "Transfer Tax";
	public String valueFromDocTaxLabel = "Value from Doc Tax";
	public String cityOfSmTaxLabel = "City of SM Tax";
	public String valueFromDocTaxCityLabel = "Value from Doc Tax(City)";
	public String pcorLable = "PCOR?";
	public String createdByLabel = "Created By";
	public String lastModifiedByLabel = "Last Modified By";
	public String transferSucessMessage="//div[@class='flowruntimeRichTextWrapper flowruntimeDisplayText']//b | //div[@class='slds-card__body slds-p-horizontal_small flowruntimeBody']//b";
	
	@FindBy(xpath = "//a[@id='relatedListsTab__item']")
	public WebElement relatedListTab;

	@FindBy(xpath = "//button[@name='New'][1]")
	public WebElement NewRecordedAPNsButton;

	@FindBy(xpath = "//*[@class='flexipage-tabset']//a[1]")
	public WebElement RelatedTab;

	@FindBy(xpath = "//div[contains(@class,'uiOutputRichText')] | //*[@class='slds-rich-text-editor__output']//b")
	public WebElement confirmationMessageOnTranferScreen;

	@FindBy(xpath = commonXpath+"//div[@class='highlights slds-clearfix slds-page-header slds-page-header_record-home']//ul[@class='slds-button-group-list']//lightning-primitive-icon")
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
	
	@FindBy(xpath = "//span[text()='Start Date']//parent::div//following-sibling::div//span")
	public WebElement startDateInParcelMaito;
	
	@FindBy(xpath = "//span[text()='End Date']//parent::div//following-sibling::div//span")
	public WebElement endDateInParcelMaito;

	@FindBy(xpath = commonXpath + "//*[@class='slds-truncate' and text()='Back'] | //button[text()='Back']")
	public WebElement quickActionOptionBack;
	
	@FindBy(xpath = "//div[@class='flowruntimeRichTextWrapper flowruntimeDisplayText']")
	public WebElement validateErrorText;	
	
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
	
	@FindBy(xpath = commonXpath+"//*[text()='Return Reason']//ancestor:: div[contains(@class,'slds-modal__content slds-p-around--medium')]//textarea")
	public WebElement returnReasonTextBox;
	
	@FindBy(xpath ="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//force-record-layout-section//force-record-layout-item//*[text()='CIO Transfer Status']/../..//slot[@slot='outputField']//lightning-formatted-text")
	public WebElement CIOstatus;
	
	@FindBy(xpath ="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//force-record-layout-section//force-record-layout-item//*[text()='Audit Trail']/../..//slot[@slot='outputField']//a//span")
	public WebElement CIOAuditTrail;

	@FindBy(xpath=commonXpath+"//button[text()='Finish']")
	public WebElement finishButtonPopUp;
	
	@FindBy(xpath="//a[@title='Edit']")
	public WebElement editLinkUnderShowMore;
	
	@FindBy(xpath="	//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//button[text()='Save']")
	public WebElement saveButtonModalWindow;
	
	@FindBy(xpath =commonXpath+"//force-record-layout-section//div//span[text()='APN']//parent::div//following-sibling::div//a//slot//slot//span")
	public WebElement apnOnTransferActivityLabel;
	
	@FindBy(xpath =commonXpath+"//force-record-layout-section//force-record-layout-item//*[text()='Situs']/../..//slot[@slot='outputField']//lightning-formatted-text")
	public WebElement situsOnTransferActivityLabel;
	
	@FindBy(xpath =commonXpath+"//force-record-layout-section//force-record-layout-item//*[text()='Short Legal Description']/../..//slot[@slot='outputField']//lightning-formatted-text")
	public WebElement shortLegalDescriptionOnTransferActivityLabel;
	
	@FindBy(xpath =commonXpath+"//force-record-layout-section//force-record-layout-item//*[text()='PUC Code']/../..//slot[@slot='outputField']//lightning-formatted-text")
	public WebElement pucCodeTransferActivityLabel;
	
	@FindBy(xpath =commonXpath+"//*[@class='slds-form-element__help']")
	public WebElement errorMessageOnTransferScreen;
	
	@FindBy(xpath =commonXpath+ "//select[@name='States']")
	public WebElement mailingState;
	
	@FindBy(xpath =commonXpath+"//force-record-layout-section//force-record-layout-item//*[text()='EventID']//following::a[@target='_blank']")
	public WebElement eventIDOnTransferActivityLabel;
		
	
	/*
	    * This method adds the recorded APN in Recorded-Document
	    * 
	    */
	    
	    public void addRecordedApn(String DocId,int count) throws Exception
	    {

	    	String getApnToAdd="Select Id,Name from Parcel__c where Id NOT IN(Select Parcel__c from Recorded_APN__c ) and status__c='Active' Limit "+count;
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
	    
	    public void generateRecorderJobWorkItems(String DocType,int ApnCount) throws IOException, InterruptedException
	    {    
	    	Thread.sleep(4000);
	    	   	
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
	    		Thread.sleep(10000);
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
		Thread.sleep(12000);
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
					Thread.sleep(4000);
					Click(getButtonWithText(copyToMailToButtonLabel));
					waitForElementToDisappear(formattedName1, 5);
					Click(formattedName1);
					Select select = new Select(formattedName1);
					select.selectByVisibleText(granteeForMailTo);
					Click(formattedName1);
					Click(mailingState);
					Select selectMailingState = new Select(mailingState);
					selectMailingState.selectByVisibleText(dataToCreateMailTo.get("Mailing State"));
					Click(mailingState);
					enter(mailZipCopyToMailTo, dataToCreateMailTo.get("Mailing Zip"));
					Click(getButtonWithText(nextButton));
					ReportLogger.INFO("Generated mail to record from Copy to mail  quick action button");
				} catch (Exception e) {
					ReportLogger.INFO("SORRY!! MAIL TO RECORD CANNOT BE ADDED THROUGH COPY TO MAIL TO ACTION BUTTON");
				}
			}

		 
		 
		 /*
		  * Deleting existing Grantee from particular recorded-document .
		  * 
		  */
		 
			public void deleteOldGranteesRecords(String recordedocId) throws IOException, Exception {
				HashMap<String, ArrayList<String>> HashMapOldGrantee = salesforceApi
						.select("SELECT Id FROM Transfer__c where recorded_document__c='" + recordedocId + "'");
				if (!HashMapOldGrantee.isEmpty()) {
					HashMapOldGrantee.get("Id").stream().forEach(Id -> {
						objSalesforceAPI.delete("Transfer__c", Id);
						ReportLogger.INFO("!!Deleted grantee with id= " + Id);
					});}
				}
			/*
			 * This method deletes Old mail to records from CIO Transfer mail to object
			 * @param : Recorded APN Transfer Id of the CIO WI
			 */
				
			public void deleteOldMailToRecords(String recordedApnTransferId) throws IOException, Exception {
				HashMap<String, ArrayList<String>> HashMapOldGrantee = salesforceApi
						.select("SELECT Id FROM CIO_Transfer_Mail_To__c where Recorded_APN_Transfer__c='"
								+ recordedApnTransferId + "'");
				if (!HashMapOldGrantee.isEmpty()) {
					HashMapOldGrantee.get("Id").stream().forEach(Id -> {
						objSalesforceAPI.delete("CIO_Transfer_Mail_To__c", Id);
						ReportLogger.INFO("!!Deleted mail to record with id= " + Id);
					});
				}
		 }
		 
		 /*
		  * This method will create one new grantee per method call in Recorded APN transfer screen.
		  * 
		  */		 

		 public void createNewGranteeRecords(String recordeAPNTransferID,Map<String, String>dataToCreateGrantee ) throws Exception
		 {
				try {
					Thread.sleep(4000);
					String execEnv = System.getProperty("region");
					driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/"
							+ recordeAPNTransferID + "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
					waitForElementToBeVisible(5, newButton);
					Click(getButtonWithText(newButton));
					enter(LastNameLabel, dataToCreateGrantee.get("Last Name"));
					if (dataToCreateGrantee.get("Owner Percentage") != null)
						enter(ownerPercentage, dataToCreateGrantee.get("Owner Percentage"));
					if (dataToCreateGrantee.get("First Name") != null)
						enter(firstNameLabel, dataToCreateGrantee.get("First Name"));					
						//enter("Legacy_System_Id", "12345");
					
					Click(getButtonWithText(saveButton));
					Thread.sleep(3000);
					ReportLogger.INFO("GRANTEE RECORD ADDED!!");
				} catch (Exception e) {
					ReportLogger.INFO("SORRY!! GRANTEE RECORD CANNOT BE ADDED");
				}

			}
		 
		 public void deleteRecordedApnFromRecordedDocument(String recordedDocumentId)
		 {
			       HashMap<String, ArrayList<String>>HashMapRecordedDocuments = salesforceApi.select("SELECT ID FROM RECORDED_APN__C WHERE Recorded_Document__c='"+recordedDocumentId+"'");
			     
			       if(!HashMapRecordedDocuments.isEmpty())
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
		 	 scrollToElement(fieldLocator1);
		 	 Thread.sleep(2000);
		 	 Click(fieldLocator1);
		 	 Thread.sleep(5000);
		 }
		 
		 public void deleteRecordedAPNTransferGranteesRecords(String recordedAPNTransferId) throws IOException, Exception
		 {      	       
			   HashMap<String, ArrayList<String>>HashMapOldGrantee =salesforceApi.select("SELECT Id ,Last_Name__c  FROM CIO_Transfer_Grantee_New_Ownership__c where Recorded_APN_Transfer__c ='"+recordedAPNTransferId+"'");			      
		        if(!HashMapOldGrantee.isEmpty()) {		    	  
		    	  HashMapOldGrantee.get("Id").stream().forEach(Id ->{
	    		  objSalesforceAPI.delete("CIO_Transfer_Grantee_New_Ownership__c", Id);
	    		  ReportLogger.INFO("!!Deleted RAT transfer grantee with id= "+Id + " and grantee name "+HashMapOldGrantee.get("Last_Name__c"));
		          } );}	 
		 } 
			

		 public void createNewGrantorRecords(String recordeAPNTransferID,Map<String, String>dataToCreateGrantee ) throws Exception
		 {
				try {
					String execEnv = System.getProperty("region");
					driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/"
							+ recordeAPNTransferID + "/related/CIO_Transfer_Grantors__r/view");
					waitForElementToBeVisible(5, newButton);
					Click(getButtonWithText(newButton));
					enter(LastNameLabel, dataToCreateGrantee.get("Last Name"));
					if (dataToCreateGrantee.get("First Name") != null)
						enter(firstNameLabel, dataToCreateGrantee.get("First Name"));
					Click(getButtonWithText(saveButton));
					Thread.sleep(3000);
					ReportLogger.INFO("GRANTOR RECORD ADDED!!");
				} catch (Exception e) {
					ReportLogger.INFO("SORRY!! GRANTOR RECORD CANNOT BE ADDED");
				}
		 }
		 
		 /*
			 * This method will click on show more actions on transfer activity screen and
			 * takes an argument of the button name .
			 * 
			 * @Param : Button text inside show more actions
			 */

			public void clickQuickActionButtonOnTransferActivity(String enterButtonText ,WebElement...others) throws Exception {
				try {
					if(others.length==0) {
					waitForElementToBeVisible(getButtonWithText(enterButtonText),10);
					Click(getButtonWithText(enterButtonText));
					ReportLogger.INFO("Successfully clicked on "+ enterButtonText +" button");
					Thread.sleep(1000);
					return;}
					waitForElementToBeVisible(others[0],10);
					Click(others[0]);
					Thread.sleep(1000);
				} catch (Exception e) {
					waitForElementToBeVisible(quickActionButtonDropdownIcon,10);
					Click(quickActionButtonDropdownIcon);
					if(others.length==0) {
					waitForElementToBeVisible(getButtonWithText(enterButtonText),10);
					Click(getButtonWithText(enterButtonText));
					ReportLogger.INFO("Successfully clicked on "+ enterButtonText +" button");
					Thread.sleep(1000);
					return ;}
					waitForElementToBeVisible(others[0],10);
					Click(others[0]);
					Thread.sleep(1000);
					
				}
			}
			
			/**
		     * Description: This method will edit recorded apn on work item page
		     * @param labelName: Takes Label Name as an argument
		     */
			 public void editRecordedApnOnWorkitem(String recordedAPNName, String recordedAPN) throws Exception {
			        ReportLogger.INFO("Edit the field : " + recordedAPNName);
			        waitForElementToBeClickable(objWorkItemHomePage.recordedAPNtab);
					objMappingPage.Click(objWorkItemHomePage.recordedAPNtab);
			        Thread.sleep(2000);
			        String xpathStr = "//span[text() = '"+recordedAPNName+"']/ancestor::th/following-sibling::td//a[@title='Show 2 more actions']";		        
			        WebElement showMoreButton =driver.findElement(By.xpath(xpathStr));
			        scrollToElement(showMoreButton);
			        Click(showMoreButton);
			        Click(editLinkUnderShowMore);
			        
			        objWorkItemHomePage.enter(objWorkItemHomePage.apnLabel, recordedAPN);
					objWorkItemHomePage.selectOptionFromDropDown(objWorkItemHomePage.apnLabel, recordedAPN);
					objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.SaveButton));					
			        Thread.sleep(2000);
			    }
	
			 /*
			  * @Description :This method will perform end to end step to create and approve CIO WI based on Enrollement type 
			  * 
			  * @Return : Returns a String array for all the related WI generated after approval for the given enrollment type
			  * 			  
			  * @param enrollementType : Enrollement Type -Direct or Normal enrollment type
			  * @param Transfer code : Event Code in RecordedAPNTransfer screen
			  * @param hashMapOwnershipAndTransferMailToCreationData : Data to create mail to record
			  * @param hashMapOwnershipAndTransferGranteeCreationData : Data to create Grantee records
			  * @param hashMapCreateOwnershipRecordData : Data to create ownership record
			  * @param hashMapCreateAssessedValueRecord : Data to create acessed value record
			  */
	
			 public String[] createAppraisalActivityWorkItemForRecordedCIOTransfer(String enrollmentType,String transferCode,Map<String, String> hashMapOwnershipAndTransferMailToCreationData,Map<String, String> hashMapOwnershipAndTransferGranteeCreationData,Map<String, String> hashMapCreateOwnershipRecordData,Map<String, String> hashMapCreateAssessedValueRecord) throws Exception {

					String excEnv = System.getProperty("region");

					JSONObject jsonForAppraiserActivity = getJsonObject();

					login(SYSTEM_ADMIN);

					Thread.sleep(4000);
					String recordedDocumentID = salesforceApi.select(
							"SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c=0")
							.get("Id").get(0);

					deleteRecordedApnFromRecordedDocument(recordedDocumentID);
					Thread.sleep(3000);
					addRecordedApn(recordedDocumentID, 1);

					generateRecorderJobWorkItems(recordedDocumentID);

					// STEP 2-Query to fetch WI

					String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 1";
					String workItemNo = salesforceApi.select(workItemQuery).get("Name").get(0);

					objMappingPage.globalSearchRecords(workItemNo);

					waitForElementToBeInVisible(ApnLabel, 5);
					String apnFromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);

					// Updating neighborhood code of parcel so Normal enrollement WI is generated
					if (enrollmentType.equalsIgnoreCase(APPRAISAL_NORMAL_ENROLLMENT)) {
						salesforceApi.update("Parcel__C",
								salesforceApi.select("Select Id from parcel__c where name ='" + apnFromWIPage + "'")
										.get("Id").get(0),
								"Neighborhood_Reference__c",
								salesforceApi.select("Select Id from Neighborhood__c where name like '03%'").get("Id")
										.get(0));
					} else {
						salesforceApi.update("Parcel__C",
								salesforceApi.select("Select Id from parcel__c where name ='" + apnFromWIPage + "'")
										.get("Id").get(0),
								"Neighborhood_Reference__c",
								salesforceApi.select("Select Id from Neighborhood__c where name = '01/011E'").get("Id")
										.get(0));
						salesforceApi.update("Parcel__C",
								salesforceApi.select("Select Id from parcel__c where name ='" + apnFromWIPage + "'")
										.get("Id").get(0),
								"PUC_Code__c",
								salesforceApi
										.select("Select Id from PUC_Code__c where name = '105- Apartment (Migrated)'")
										.get("Id").get(0));
					}

					// Deleting existing ownership from parcel

					deleteOwnershipFromParcel(salesforceApi
							.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0));

					// STEP 3- adding owner after deleting for the recorded APN

					String acesseName = objMappingPage.getOwnerForMappingAction();
					driver.navigate()
							.to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Parcel__c/"
									+ salesforceApi
											.select("Select Id from parcel__C where name='" + apnFromWIPage + "'")
											.get("Id").get(0)
									+ "/related/Property_Ownerships__r/view");
					objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
					String ownershipId = driver.getCurrentUrl().split("/")[6];
					objParcelsPage.deleteOldAndCreateNewAssessedValuesRecords(hashMapCreateAssessedValueRecord,
							apnFromWIPage);

					// STEP 4- updating the ownership date for current owners

					String dateOfEvent = salesforceApi
							.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '"
									+ ownershipId + "'")
							.get("Ownership_Start_Date__c").get(0);
					jsonForAppraiserActivity.put("DOR__c", dateOfEvent);
					jsonForAppraiserActivity.put("DOV_Date__c", dateOfEvent);

					salesforceApi.update("Property_Ownership__c", ownershipId, jsonForAppraiserActivity);

					objMappingPage.logout();

					objMappingPage.login(CIO_STAFF);
					objMappingPage.waitForElementToBeClickable(objMappingPage.appLauncher, 10);
					
					// Selecting E-FILE intake as CIO works best with E-FILE AND APAS and there are some issues with navigation on APAS
					
					searchModule(modules.EFILE_INTAKE);
					objMappingPage.globalSearchRecords(workItemNo);
					Thread.sleep(5000);
					String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='"
							+ workItemNo + "'";
					HashMap<String, ArrayList<String>> navigationUrL = salesforceApi.select(queryRecordedAPNTransfer);

					// STEP 6-Finding the recorded apn transfer id

					String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
					deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);
					waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
					objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
					objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
					objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);

					// STEP 7-Clicking on related action link

					objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
					String parentWindow = driver.getWindowHandle();
					objWorkItemHomePage.switchToNewWindow(parentWindow);
					softAssert.assertContains(driver.getCurrentUrl(), navigationUrL.get("Navigation_Url__c").get(0),
							"SMAB-T3306:Validating that user navigates to CIo transfer screenafter clicking on related action hyperlink");

					waitForElementToBeClickable(quickActionButtonDropdownIcon, 10);
					ReportLogger.INFO("Add the Transfer Code");

					editRecordedApnField(transferCodeLabel);
					waitForElementToBeVisible(10, transferCodeLabel);
					searchAndSelectOptionFromDropDown(transferCodeLabel, transferCode);
					Click(getButtonWithText(saveButton));

					// STEP 8-Creating the new grantee

					createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
					driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/"
							+ recordeAPNTransferID + "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
					HashMap<String, ArrayList<String>> granteeHashMap = getGridDataForRowString("1");
					String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);

					// STEP 11- Performing calculate ownership to perform partial transfer

					driver.navigate()
							.to("https://smcacre--" + excEnv
									+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"
									+ recordeAPNTransferID + "/view");
					waitForElementToBeClickable(10, calculateOwnershipButtonLabel);

					Click(getButtonWithText(calculateOwnershipButtonLabel));
					waitForElementToBeVisible(5, nextButton);
					enter(calculateOwnershipRetainedFeld, "50");
					Click(getButtonWithText(nextButton));

					// STEP 12-Creating copy to mail to record

					createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferMailToCreationData);
					waitForElementToBeClickable(7, copyToMailToButtonLabel);

					// STEP 13-Navigating back to RAT screen

					driver.navigate()
							.to("https://smcacre--" + excEnv
									+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"
									+ recordeAPNTransferID + "/view");
					
					//STEP 14 - Click on submit for approval button
					 clickQuickActionButtonOnTransferActivity(null,quickActionOptionSubmitForApproval);
					
					ReportLogger.INFO("CIO!! Transfer submitted for approval");
					waitForElementToBeClickable(10, finishButton);
					Click(getButtonWithText(finishButton));

					logout();

					login(CIO_SUPERVISOR);
					driver.navigate()
							.to("https://smcacre--" + excEnv
									+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"
									+ recordeAPNTransferID + "/view");
					//STEP 14 - Click on submit for approval button
					 clickQuickActionButtonOnTransferActivity(null,quickActionOptionApprove);
					

					waitForElementToBeClickable(10, finishButton);
					Click(getButtonWithText(finishButton));

					// Fetching appraiser WI genrated on approval of CIO WI
					if (enrollmentType.equalsIgnoreCase(APPRAISAL_NORMAL_ENROLLMENT)) {
						
						//Filtering that if type is normal enrollement and Event code is CIO-GOVT
						
						if (transferCode.equals(CIO_EVENT_CODE_CIOGOVT)) {
							String workItemNoForGovtCIOAppraisal = salesforceApi.select(
									"Select Id ,Name from Work_Item__c where type__c='Govt CIO Appraisal' and sub_type__c='Appraisal Activity' order by createdDate desc")
									.get("Name").get(0);
							String[] arrayForWorkItemAfterCIOSupervisorApproval = { workItemNoForGovtCIOAppraisal };
							logout();
							return arrayForWorkItemAfterCIOSupervisorApproval;
						}					
						
						String workItemNoForAppraiser = salesforceApi.select(
								"Select Id ,Name from Work_Item__c where type__c='Appraiser' and sub_type__c='Appraisal Activity' order by createdDate desc")
								.get("Name").get(0);						
						String workItemNoForQuestionnaireCorrespondence = salesforceApi.select(
								"Select Id ,Name from Work_Item__c where type__c='Appraiser' and sub_type__c='Questionnaire Correspondence' order by createdDate desc")
								.get("Name").get(0);						
						String[] arrayForWorkItemAfterCIOSupervisorApproval = { workItemNoForAppraiser,
								workItemNoForQuestionnaireCorrespondence };
						logout();
						return arrayForWorkItemAfterCIOSupervisorApproval;
					}					
					

					else {
						String workItemNoForDirectEnrollement = salesforceApi.select(
								"Select Id ,Name from Work_Item__c where type__c='Direct Enrollment' and sub_type__c='Verify DE' order by createdDate desc")
								.get("Name").get(0);
						String[] arrayForWorkItemAfterCIOSupervisorApproval = { workItemNoForDirectEnrollement };
						logout();
						return arrayForWorkItemAfterCIOSupervisorApproval;

					}
					
			 }
}
		 	
