package com.apas.Tests.DisabledVeteran;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.*;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;

public class DisabledVeteran_Exemption_WorkItem_Tests extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objExemptionsPage;
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath;
	Map<String, String> rpslData;
	String rpslFileDataPath;
	String newExemptionName;
	WorkItemHomePage objWIHomePage;
	SalesforceAPI salesforceAPI;
	
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objWIHomePage = new WorkItemHomePage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
		objApasGenericPage.updateRollYearStatus("Closed", "2020");
		rpslFileDataPath = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;
		rpslData= objUtil.generateMapFromJsonFile(rpslFileDataPath, "DataToCreateRPSLEntryForValidation");
		salesforceAPI = new SalesforceAPI();
		
	}
	
	@Test(description = "SMAB-T2080,SMAB-T1922: APAS system should generate a WI on new Exemption Creation", 
			dataProvider = "loginExemptionSupportStaff", 
			dataProviderClass = DataProviders.class , 
			groups = {"regression","DV_WorkItem_Exemption"})
	public void DisabledVeteran_verifyWorkItemGeneratedOnNewExemptionCreation(String loginUser) throws Exception {
		  
		   Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
		   String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy");
			String currentRollYear=ExemptionsPage.determineRollYear(currentDate);	
			//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		   ReportLogger.INFO("Step 1: Login to the Salesforce ");
		   objApasGenericPage.login(loginUser);
		   objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
		  
		   //Step2: Opening the Exemption Module
		   ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
		   objApasGenericPage.searchModule(modules.EXEMPTIONS);
		  
		   //Step3: create a New Exemption record
		   ReportLogger.INFO("Step 3: Create New Exemption");
		   objPage.Click(objExemptionsPage.newExemptionButton);
		  
		   //Get the WI Name from DB through the newly created Exemption
		   newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
		   HashMap<String, ArrayList<String>> getWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
		   
		    String WIName = getWIDetails.get("Name").get(0);
		    String WIRequestType = getWIDetails.get("Request_Type__c").get(0);
		   
		    //Step4: Opening the Work Item Module
		    ReportLogger.INFO("Step 4: Search open App. module - Work Item Management from App Launcher");
		   objApasGenericPage.searchModule(modules.HOME);

		   //Step5: Click on the Main TAB - Home
		   ReportLogger.INFO("Step 5: Click on the Main TAB - Home");
		   objPage.Click(objWIHomePage.lnkTABHome);
		   ReportLogger.INFO("Step 6: Click on the Sub  TAB - Work Items");
		   objPage.Click(objWIHomePage.lnkTABWorkItems);
		   ReportLogger.INFO("Step 7: Click on the check box - Show RP");
		   ReportLogger.INFO("Step 8: Click on the SUB TAB - My Submitted for Approval");
		   objPage.Click(objWIHomePage.lnkTABMySubmittedforApproval);
			
		   //Search the Work Item Name in the Grid 1st Column
		   String actualWIName = objWIHomePage.searchandClickWIinGrid(WIName);
			
		   objPage.waitForElementToBeClickable(objWIHomePage.detailsWI);
		   objPage.Click(objWIHomePage.detailsWI);
			 //Validating that 'Use Code' field and 'Date' field gets automatically populated in the work item record
		   objWIHomePage.waitForElementToBeVisible(10, objWIHomePage.referenceDetailsLabel);
			softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Use Code", "Reference Data Details"),"SEC",
							"SMAB-T2080: Validation that 'Use Code' fields getting automatically populated in the work item record");
			softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,
							"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
			
			String actualRequestTypeName = objWIHomePage.searchRequestTypeNameonWIDetails(WIRequestType);
			String RequestTypeName  = "Disabled Veterans - Direct Review and Update - Initial filing/changes";
			  
			objPage.Click(objWIHomePage.linkedItemsWI);
		   //objPage.Click(actualWIName);
		   objPage.Click(objWIHomePage.linkedItemsRecord);
		   String actualExemptionName = objWIHomePage.searchLinkedExemptionOrVA(newExemptionName);
		   Thread.sleep(2000);
		 		   
		   ReportLogger.INFO("Step 9: Verifying Work Item is generated , Work Item Request Name , Exemption Link on creation of new Work Item");
		  
		   ReportLogger.INFO("Step 10: Work Item is generated on creation of new Exemption");
		   softAssert.assertEquals(actualWIName.toString(),WIName,"SMAB-T1922:Verify name of WI generated");
		   softAssert.assertEquals(actualExemptionName.toString(),newExemptionName,"SMAB-T1922:Verify Exemption Name of WI generated");
		   softAssert.assertEquals(actualRequestTypeName.toString(),RequestTypeName,"SMAB-T1922:Verify RequestType Name of WI generated");

		   String updateWIStatus = "SELECT Id FROM Work_Item__c where Name = '"+WIName+"'";
		   salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");

		   ReportLogger.INFO("Step 11: Logging out from SF");

		   objApasGenericPage.logout();

	}
	
	@Test(description = "SMAB-T1923: APAS system should generate a WI on updating the End Date of Rating for Existing Exemption", 
			dataProvider = "loginExemptionSupportStaff", 
			dataProviderClass = DataProviders.class , 
			groups = {"regression","DV_WorkItem_Exemption"})
	public void DisabledVeteran_verifyWorkItemGeneratedOnEnterEndDateRatingExistingExemption(String loginUser) throws Exception {
	
		Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
		Map<String, String> dataToEdit = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ReportLogger.INFO("Step 1: Login to the Salesforce ");
		objApasGenericPage.login(loginUser);
		objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
		
		//Step2: Opening the Exemption Module
		ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
		objApasGenericPage.searchModule(modules.EXEMPTIONS);
		
		//Step3: create a New Exemption record
		ReportLogger.INFO("Step 3: Create New Exemption");
		objPage.Click(objExemptionsPage.newExemptionButton);
		
		//Get the WI Name from DB through the newly created Exemption
		newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
		
		HashMap<String, ArrayList<String>> getWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
	    String WIName = getWIDetails.get("Name").get(0);
	    String updateWIStatus = "SELECT Id FROM Work_Item__c where Name = '"+WIName+"'";
  	    salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");

		//step3: adding end date of rating
		ReportLogger.INFO("Step 4: Click on Edit Icon for End Date of Rating Field");
		objPage.Click(objExemptionsPage.editExemption);
		ReportLogger.INFO("Step 5: Enter the End Date of Rating :"+dataToEdit.get("EnddateOfRating"));
		objPage.enter(objExemptionsPage.endDateOfRating, dataToEdit.get("EnddateOfRating"));
		ReportLogger.INFO("Step 6: Enter the End Date of Rating Reason :"+dataToEdit.get("EndRatingReason"));
		objApasGenericPage.selectOptionFromDropDown(objExemptionsPage.endRatingReason, dataToEdit.get("EndRatingReason"));
		ReportLogger.INFO("Step 7: Click on the SAVE button");
		objPage.Click(ExemptionsPage.saveButton);
		HashMap<String, ArrayList<String>> getWIDetailsAfterEndDate = null;
		getWIDetailsAfterEndDate = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
	    WIName = getWIDetailsAfterEndDate.get("Name").get(0);
	    			    
	    //Step4: Opening the Work Item Module
	    ReportLogger.INFO("Step 8: Search open App. module - Work Item Management from App Launcher");
	  	objApasGenericPage.searchModule(modules.HOME);
	  	//Step5: Click on the Main TAB - Home
	  	ReportLogger.INFO("Step 9: Click on the Main TAB - Home");
	  	objPage.Click(objWIHomePage.lnkTABHome);
	  	ReportLogger.INFO("Step 10: Click on the Sub  TAB - Work Items");
	  	objPage.Click(objWIHomePage.lnkTABWorkItems);
	    ReportLogger.INFO("Step 12: Click on the SUB TAB - My Submitted for Approval");
	  	objPage.Click(objWIHomePage.lnkTABMySubmittedforApproval);
	  	//Search the Work Item Name in the Grid 1st Column
	  	String actualWIName = objWIHomePage.searchandClickWIinGrid(WIName);
	  	ReportLogger.INFO("Step 13: Verifying Work Item is generated on entering/editing the End Date of Rating for an Exemption");
	  	softAssert.assertEquals(actualWIName, WIName, "SMAB-T1923: Work Item is generated on Entering the End Date of Rating for an Exemption");
	  	updateWIStatus = "SELECT Id FROM Work_Item__c where Name = '"+WIName+"'";
  	    salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");
  	    ReportLogger.INFO("Step 11: Logging out from SF");
  	    objApasGenericPage.logout();
	   	
	 }
	
	@Test(description = "SMAB-T1926: APAS system should not generate a WI on new Exemption having WI opened", 
			dataProvider = "loginExemptionSupportStaff", 
			dataProviderClass = DataProviders.class , 
			groups = {"regression","DV_WorkItem_Exemption"})
	public void DisabledVeteran_verifyWorkItemNotGeneratedOnExemptionWithOpenWI(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	ReportLogger.INFO("Step 1: Login to the Salesforce ");
	objApasGenericPage.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
	objApasGenericPage.searchModule(modules.EXEMPTIONS);
	
	//Step3: create a New Exemption record
	ReportLogger.INFO("Step 3: Create New Exemption");
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	//Get the WI Name from DB through the newly created Exemption
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	Thread.sleep(5000);
	HashMap<String, ArrayList<String>> getWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
    String WIName = getWIDetails.get("Name").get(0);
    
    //step4: adding end date of rating
    Map<String, String> dataToEdit = objUtil.generateMapFromJsonFile(exemptionFilePath, "newExemptionMandatoryData");
  	ReportLogger.INFO("Step 4: Adding End date of Rating in the exemption");
  	objPage.Click(objExemptionsPage.editExemption);
  	objPage.enter(objExemptionsPage.endDateOfRating, dataToEdit.get("EnddateOfRating"));
  	ReportLogger.INFO("Step 5: Adding End date of Rating Reason in the exemption");
  	objApasGenericPage.selectOptionFromDropDown(objExemptionsPage.endRatingReason, dataToEdit.get("EndRatingReason"));
  	ReportLogger.INFO("Step 6: Click the SAVE button");
  	objPage.Click(ExemptionsPage.saveButton);
  	Thread.sleep(5000);
  	ReportLogger.INFO("Step 7: Verifying the Work Item is not generated on Entering the End Date of Rating for an Exemption having opened WI");
  	
  	salesforceAPI = new SalesforceAPI();
    
  	String slqWork_Item_Id = "Select Work_Item__c from Work_Item_Linkage__c where Exemption__r.Name = '"+newExemptionName+"'";
    Thread.sleep(2000);
    HashMap<String, ArrayList<String>> response_2  = salesforceAPI.select(slqWork_Item_Id);
    
  	String numWI = String.valueOf(response_2.get("Work_Item__c").size());
  	
  	softAssert.assertEquals(numWI, "1", "SMAB-T1926: Verify Work Item is not generated on Entering the End Date of Rating for an Exemption having opened WI");
  	String updateWIStatus = "SELECT Status__c FROM Work_Item__c where Name = '"+WIName+"'";
	salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");
	ReportLogger.INFO("Step 4: Logging Out from SF");
  	objApasGenericPage.logout();
 }

	@Test(description = "SMAB-T2094,SMAB-T1978: APAS Verify the Supervisor is able to Approve the WI initial filing/changes on new Exemption Creation", 
			dataProvider = "loginExemptionSupportStaff", 
			dataProviderClass = DataProviders.class , 
			groups = {"regression","DV_WorkItem_Exemption"})
	public void DisabledVeteran_verifyWorkItemExemptionFilingChangesIsApproved(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	ReportLogger.INFO("Step 1: Login to the Salesforce ");
	objApasGenericPage.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
	objApasGenericPage.searchModule(modules.EXEMPTIONS);
	
	//Step3: create a New Exemption record
	ReportLogger.INFO("Step 3: Create New Exemption");
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	//Get the WI Name from DB through the newly created Exemption
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	HashMap<String, ArrayList<String>> getWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
    String WIName = getWIDetails.get("Name").get(0);
    
    ReportLogger.INFO("Step 4: Logging Out from SF");
    objApasGenericPage.logout();
    Thread.sleep(10000);
    ReportLogger.INFO("Step 5: Logging In as Approver - Data Admin");
  	objApasGenericPage.login(users.RP_BUSINESS_ADMIN);
  	
    //Step4: Opening the Work Item Module
    ReportLogger.INFO("Step 6: Search open App. module - Work Item Management from App Launcher");
  	objApasGenericPage.searchModule(modules.HOME);
  	//Step5: Click on the Main TAB - Home
  	ReportLogger.INFO("Step 7: Click on the Main TAB - Home");
  	objPage.Click(objWIHomePage.lnkTABHome);
  	ReportLogger.INFO("Step 8: Click on the Sub  TAB - Work Items");
  	objPage.Click(objWIHomePage.lnkTABWorkItems);
  	//ReportLogger.INFO("Step 9: Click on the check box - Show RP");
  	//objPage.Click(objWIHomePage.chkShowRP);
  	ReportLogger.INFO("Step 10: Click on Needs My Approval TAB");
  	objPage.Click(objWIHomePage.needsMyApprovalTab);
  	ReportLogger.INFO("Step 11: Search for the Work Item and select the checkbox");
  	objWIHomePage.clickCheckBoxForSelectingWI(WIName);
  	
  	String parentwindow = driver.getWindowHandle();
	//SMAB-T2094 opening the action link to validate that link redirects to Exemptions page 
  	objWIHomePage.openActionLink(WIName);
	objPage.switchToNewWindow(parentwindow);
	softAssert.assertTrue(objPage.verifyElementVisible(objExemptionsPage.newExemptionNameAftercreation),
			"SMAB-T2094: Validation that Exemption label is visible");
	driver.switchTo().window(parentwindow);
	
	ReportLogger.INFO("Step 12: Click on the Approve button");
  	objPage.javascriptClick(objWIHomePage.btnApprove);
  	Thread.sleep(5000);
  	ReportLogger.INFO("Step 13: Logging Out from SF");
  	objApasGenericPage.logout();
  	Thread.sleep(10000);
  	ReportLogger.INFO("Step 14: Logging IN to SF");
  	objApasGenericPage.login(loginUser);
  	Thread.sleep(5000);
  	
    //Step4: Opening the Work Item Module
    ReportLogger.INFO("Step 15: Search open App. module - Work Item Management from App Launcher");
  	objApasGenericPage.searchModule(modules.HOME);
  	//Step5: Click on the Main TAB - Home
  	ReportLogger.INFO("Step 16: Click on the Main TAB - Home");
  	objPage.Click(objWIHomePage.lnkTABHome);
  	ReportLogger.INFO("Step 17: Click on the Sub  TAB - Work Items");
  	objPage.Click(objWIHomePage.lnkTABWorkItems);
  	ReportLogger.INFO("Step 18: Click on the check box - Show RP");
  	ReportLogger.INFO("Step 19: Click on the TAB - Completed");
  	objPage.Click(objWIHomePage.lnkTABCompleted);
  	String actualWIName = objWIHomePage.searchandClickWIinGrid(WIName);
  	ReportLogger.INFO("Step 20: Verifying the Approver has successfully Approved the Work Item");
  	softAssert.assertEquals(actualWIName, WIName, "SMAB-T1978: Approver has successfully Approved the Work Item");
  	Thread.sleep(5000);
  	objApasGenericPage.logout();
  	
  }

	@Test(description = "SMAB-T1981: APAS Verify the Supervisor is able to Return the WI initial filing/changes on new Exemption Creation", 
			dataProvider = "loginExemptionSupportStaff", 
			dataProviderClass = DataProviders.class , 
			groups = {"regression","DV_WorkItem_Exemption"})
	public void DisabledVeteran_verifyWorkItemExemptionFilingChangesIsReturned(String loginUser) throws Exception {
	
		Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ReportLogger.INFO("Step 1: Login to the Salesforce ");
		objApasGenericPage.login(loginUser);
		objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
		
		//Step2: Opening the Exemption Module
		ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
		objApasGenericPage.searchModule(modules.EXEMPTIONS);
		
		//Step3: create a New Exemption record
		ReportLogger.INFO("Step 3: Create New Exemption");
		objPage.Click(objExemptionsPage.newExemptionButton);
		
		//Get the WI Name from DB through the newly created Exemption
		newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
		HashMap<String, ArrayList<String>> getWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
	    String WIName = getWIDetails.get("Name").get(0);    
	    ReportLogger.INFO("Step 4: Logging OUT of SF");
	  	objApasGenericPage.logout();
	  	Thread.sleep(10000);
	  	ReportLogger.INFO("Step 5: Logging IN as Approver - Data Admin");
	  	objApasGenericPage.login(users.RP_BUSINESS_ADMIN);
	  	
	    //Step4: Opening the Work Item Module
	    ReportLogger.INFO("Step 6: Search open App. module - Work Item Management from App Launcher");
	  	objApasGenericPage.searchModule(modules.HOME);
	  	//Step5: Click on the Main TAB - Home
	  	ReportLogger.INFO("Step 7: Click on the Main TAB - Home");
	  	objPage.Click(objWIHomePage.lnkTABHome);
	  	ReportLogger.INFO("Step 8: Click on the Sub  TAB - Work Items");
	  	objPage.Click(objWIHomePage.lnkTABWorkItems);
	  	//ReportLogger.INFO("Step 9: Click on the check box - Show RP");
	  	//objPage.Click(objWIHomePage.chkShowRP);
	  	ReportLogger.INFO("Step 10: Click on TAB - Needs My Approval");
	  	objPage.Click(objWIHomePage.needsMyApprovalTab);
	  	ReportLogger.INFO("Step 11: Search and select the Work Item checkbox");
	  	objWIHomePage.clickCheckBoxForSelectingWI(WIName);
	  	ReportLogger.INFO("Step 12: Click on the Return TAB");
	  	objPage.javascriptClick(objWIHomePage.returnWorkItemButton);
	  	ReportLogger.INFO("Step 13: Enter the Return Reason in the text box");
	  	objPage.enter(objWIHomePage.returnedReasonTxtBox, "Return to Assignee");
	  	ReportLogger.INFO("Step 14: Click on the SAVE button on the dialogbox");
	  	objPage.Click(objWIHomePage.saveButton);
		
	  	ReportLogger.INFO("Step 15: Logging OUT from SF");
	  	objApasGenericPage.logout();
	  	Thread.sleep(10000);
	  	ReportLogger.INFO("Step 16: Logging IN SF");
	  	objApasGenericPage.login(loginUser);
	  	Thread.sleep(20000);
	    //Step4: Opening the Work Item Module
	    ReportLogger.INFO("Step 17: Search open App. module - Work Item Management from App Launcher");
	  	objApasGenericPage.searchModule(modules.HOME);
	  	//Step5: Click on the Main TAB - Home
	  	ReportLogger.INFO("Step 18: Click on the Main TAB - Home");
	  	objPage.Click(objWIHomePage.lnkTABHome);
	  	ReportLogger.INFO("Step 19: Click on the Sub  TAB - Work Items");
	  	objPage.Click(objWIHomePage.lnkTABWorkItems);
	  	//ReportLogger.INFO("Step 20: Click on the check box - Show RP");
	  	//objPage.Click(objWIHomePage.chkShowRP);
	  	ReportLogger.INFO("Step 21: Click the TAB - In Progress");
	  	objPage.Click(objWIHomePage.lnkTABInProgress);
	  	String actualWIName = objWIHomePage.searchandClickWIinGrid(WIName);
	  	ReportLogger.INFO("Step 22: Verify the  Approver has successfully returned the Work Item to the Asignee");
	  	softAssert.assertEquals(actualWIName, WIName, "SMAB-T1981: Approver has successfully returned the Work Item to the Asignee");
	  	ReportLogger.INFO("Step 23: Logging OUT");
	  	Thread.sleep(2000);
	  	objApasGenericPage.logout();
  }	
}	
