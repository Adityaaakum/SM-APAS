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
		salesforceApi=new SalesforceAPI();
		objMappingPage= new MappingPage(driver);
		
		
	}
	
	public String ApnLabel ="APN";
	int counterForFailedattempts=0;
	public String componentActionsButtonLabel = "Component Actions";
	public String submitforApprovalButtonLabel = "Submit for Approval";
	public String copyToMailToButtonLabel = "Copy to Mail To";
	public String calculateOwnershipButtonLabel  = "Calculate Ownership";
	public String checkOriginalTransferListButtonLabel = "Check Original Transfer List";
	public String backToWIsButtonLabel = "Back to WIs";
	

	@FindBy(xpath = "//a[@id='relatedListsTab__item']")
	public WebElement relatedListTab;
	
    @FindBy(xpath = "//button[@name='New'][1]")
	public WebElement NewRecordedAPNsButton;
    
    @FindBy(xpath = "//button[@name='SaveEdit']")
    public WebElement SaveButton;
    
    @FindBy(xpath = "//*[@class='flexipage-tabset']//a[1]")
    public WebElement RelatedTab;
	
	@FindBy(xpath = "//div[contains(@class,'uiOutputRichText')]")
	public WebElement confirmationMessageOnTranferScreen;
	
	@FindBy(xpath = "//div[@class='highlights slds-clearfix slds-page-header slds-page-header_record-home']//ul[@class='slds-button-group-list']//lightning-primitive-icon")
	public WebElement quickActionButtonDropdownIcon;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Back']")
	public WebElement quickActionOptionBack;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Approve']")
	public WebElement quickActionOptionApprove;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Return']")
	public WebElement quickActionOptionReturn;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Submit for Review']")
	public WebElement quickActionOptionSubmitForReview;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Review Complete']")
	public WebElement quickActionOptionReviewComplete;
	
	
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
							Click(SaveButton);
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
	     * This method triggers the job to get the desried WI for given document type and APN count
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
	    		ReportLogger.INFO("Genrated Recorded WorkeItems."); 
	    		counterForFailedattempts=0;
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
		   
		   String fetchDocId ="SELECT id from recorded_document__c where recorder_doc_type__c='"+type+"'"+" and xAPN_count__c="+count;
		   
		   if(salesforceApi.select(fetchDocId).get("Id")==null)
		   {
			   return null;
		   }
		   
		  return salesforceApi.select(fetchDocId).get("Id").get(0);
	   }	    
	    
	    
		public HashMap<String, ArrayList<String>>getAPNNameFromRecordedDocument(String RecordedDocId)
		{
			
			return salesforceApi.select("select Name from Parcel__c where Id in (SELECT Parcel__c FROM Recorded_APN__c where Recorded_document__c ='"+RecordedDocId+"'");
		}

}
