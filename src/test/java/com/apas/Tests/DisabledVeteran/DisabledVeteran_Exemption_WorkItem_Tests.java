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
import com.apas.PageObjects.WorkItemMngmntHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.generic.ApasGenericFunctions;
import com.apas.config.*;
import com.apas.Utils.SalesforceAPI;

public class DisabledVeteran_Exemption_WorkItem_Tests extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objExemptionsPage;
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath;
	Map<String, String> rpslData;
	String rpslFileDataPath;
	String newExemptionName;
	WorkItemMngmntHomePage objWIHomePage;
	SalesforceAPI salesforceAPI;
	
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objWIHomePage = new WorkItemMngmntHomePage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
		objApasGenericFunctions.updateRollYearStatus("Closed", "2020");
		rpslFileDataPath = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;
		rpslData= objUtil.generateMapFromJsonFile(rpslFileDataPath, "DataToCreateRPSLEntryForValidation");
		salesforceAPI = new SalesforceAPI();
		
	}
	
	@Test(description = "SMAB-T1922 APAS system should generate a WI on new Exemption Creation", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyWorkItemGeneratedOnNewExemptionCreation(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	ReportLogger.INFO("Step 1: Login to the Salesforce ");
	objApasGenericFunctions.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
	objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
	
	//Step3: create a New Exemption record
	ReportLogger.INFO("Step 3: Create New Exemption");
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	//Get the WI Name from DB through the newly created Exemption
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	String sqlgetWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
	HashMap<String, ArrayList<String>> response  = salesforceAPI.select(sqlgetWIDetails);
    String WIName = response.get("Work_Item__c.Name").get(0);
    String WIRequestType = response.get("Work_Item__c.Request_Type__c").get(0);
    
    //Step4: Opening the Work Item Module
    ReportLogger.INFO("Step 4: Search open App. module - Work Item Management from App Launcher");
  	objApasGenericFunctions.searchModule(modules.WORKITEM_MNGMNT);
  	//Step5: Click on the Main TAB - Home
  	ReportLogger.INFO("Step 5: Click on the Main TAB - Home");
  	objPage.Click(objWIHomePage.lnkTABHome);
  	ReportLogger.INFO("Step 6: Click on the Sub  TAB - Work Items");
  	objPage.Click(objWIHomePage.lnkTABWorkItems);
  	ReportLogger.INFO("Step 7: Click on the check box - Show RP");
  	objPage.Click(objWIHomePage.chkShowRP);
  	ReportLogger.INFO("Step 8: Click on the SUB TAB - My Submitted for Approval");
  	objPage.Click(objWIHomePage.lnkTABMySubmittedforApproval);
  	//Search the Work Item Name in the Grid 1st Column
  	WebElement actualWIName = objWIHomePage.searchWIinGrid(WIName);
  	objPage.Click(actualWIName);
  	objPage.Click(objWIHomePage.lnkLinkedItems);
  	WebElement actualExemptionName = objWIHomePage.searchLinkedExemption(newExemptionName);
  	objPage.Click(objWIHomePage.lnkDetails);
  	WebElement actualRequestTypeName = objWIHomePage.searchRequestTypeNameonWIDetails(WIRequestType);
  	String RequestTypeName  = "Disabled Veterans - Direct Review and Update - Initial filing/changes";
  	
  	ReportLogger.INFO("Step 9: Verifying Work Item is generated , Work Item Request Name , Exemption Link on creation of new Work Item");
  	
  	boolean condition_1 = false;
  	boolean condition_2 = false;
  	boolean condition_3 = false;
  	
  	condition_1 = actualWIName.toString().equals(WIName);
  	condition_2 = actualExemptionName.toString().equals(newExemptionName);
  	condition_3 = actualRequestTypeName.toString().equals(RequestTypeName); 	
  	ReportLogger.INFO("Step 10: Work Item is generated on creation of new Exemption");
  	
  	if(condition_1 && condition_2 && condition_3) {
  		softAssert.assertTrue(true, "SMAB-T1922: verify Work Item is generated on creation of new Exemption");
  		String updateWIStatus = "SELECT Status__c FROM Work_Item__c where Name = '"+WIName+"'";
  	    salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");
  	}else {
  		softAssert.assertTrue(false, "SMAB-T1922: verify Work Item is generated on creation of new Exemption");
  	}
  	ReportLogger.INFO("Step 11: Logging out from SF");
  	objApasGenericFunctions.logout();

  }

	@Test(description = "SMAB-T1923 APAS system should generate a WI on updating the End Date of Rating for Existing Exemption", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyWorkItemGeneratedOnEnterEndDateRatingExistingExemption(String loginUser) throws Exception {
	
		Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
		Map<String, String> dataToEdit = objUtil.generateMapFromJsonFile(exemptionFilePath, "editExemptionData");
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ReportLogger.INFO("Step 1: Login to the Salesforce ");
		objApasGenericFunctions.login(loginUser);
		objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
		
		//Step2: Opening the Exemption Module
		ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
		objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
		
		//Step3: create a New Exemption record
		ReportLogger.INFO("Step 3: Create New Exemption");
		objPage.Click(objExemptionsPage.newExemptionButton);
		
		//Get the WI Name from DB through the newly created Exemption
		newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
		
		String sqlgetWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
		HashMap<String, ArrayList<String>> response_1  = salesforceAPI.select(sqlgetWIDetails);
	    String WIName = response_1.get("Work_Item__c.Name").get(0);
	    String updateWIStatus = "SELECT Status__c FROM Work_Item__c where Name = '"+WIName+"'";
  	    salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");

		//step3: adding end date of rating
		ReportLogger.INFO("Step 4: Click on Edit Icon for End Date of Rating Field");
		objPage.Click(objExemptionsPage.editExemption);
		ReportLogger.INFO("Step 5: Enter the End Date of Rating :"+dataToEdit.get("EnddateOfRating"));
		objPage.enter(objExemptionsPage.endDateOfRating, dataToEdit.get("EnddateOfRating"));
		ReportLogger.INFO("Step 6: Enter the End Date of Rating Reason :"+dataToEdit.get("EndRatingReason"));
		objApasGenericFunctions.selectFromDropDown(objExemptionsPage.endRatingReason, dataToEdit.get("EndRatingReason"));
		ReportLogger.INFO("Step 7: Click on the SAVE button");
		objPage.Click(ExemptionsPage.saveButton);
		
		sqlgetWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
		HashMap<String, ArrayList<String>> response_2  = salesforceAPI.select(sqlgetWIDetails);
	    WIName = response_2.get("Work_Item__c.Name").get(0);
	    			    
	    //Step4: Opening the Work Item Module
	    ReportLogger.INFO("Step 8: Search open App. module - Work Item Management from App Launcher");
	  	objApasGenericFunctions.searchModule(modules.WORKITEM_MNGMNT);
	  	//Step5: Click on the Main TAB - Home
	  	ReportLogger.INFO("Step 9: Click on the Main TAB - Home");
	  	objPage.Click(objWIHomePage.lnkTABHome);
	  	ReportLogger.INFO("Step 10: Click on the Sub  TAB - Work Items");
	  	objPage.Click(objWIHomePage.lnkTABWorkItems);
	  	ReportLogger.INFO("Step 11: Click on the check box - Show RP");
	  	objPage.Click(objWIHomePage.chkShowRP);
	  	ReportLogger.INFO("Step 12: Click on the SUB TAB - My Submitted for Approval");
	  	objPage.Click(objWIHomePage.lnkTABMySubmittedforApproval);
	  	//Search the Work Item Name in the Grid 1st Column
	  	WebElement actualWIName = objWIHomePage.searchWIinGrid(WIName);
	  	ReportLogger.INFO("Step 13: Verifying Work Item is generated on entering/editing the End Date of Rating for an Exemption");
	  	softAssert.assertEquals(actualWIName, WIName, "SMAB-T1923: Work Item is generated on Entering the End Date of Rating for an Exemption");
	  	updateWIStatus = "SELECT Status__c FROM Work_Item__c where Name = '"+WIName+"'";
  	    salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");
  	    ReportLogger.INFO("Step 11: Logging out from SF");
  	    objApasGenericFunctions.logout();
	   	
	 }
	
	@Test(description = "SMAB-T1926 APAS system should not generate a WI on new Exemption having WI opened", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyWorkItemNotGeneratedOnExemptionWithOpenWI(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	ReportLogger.INFO("Step 1: Login to the Salesforce ");
	objApasGenericFunctions.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
	objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
	
	//Step3: create a New Exemption record
	ReportLogger.INFO("Step 3: Create New Exemption");
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	//Get the WI Name from DB through the newly created Exemption
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	String sqlgetWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
	HashMap<String, ArrayList<String>> response_1  = salesforceAPI.select(sqlgetWIDetails);
    String WIName = response_1.get("Work_Item__c.Name").get(0);
    
    //step4: adding end date of rating
    Map<String, String> dataToEdit = objUtil.generateMapFromJsonFile(exemptionFilePath, "newExemptionMandatoryData");
  	ReportLogger.INFO("Step 4: Adding End date of Rating in the exemption");
  	objPage.Click(objExemptionsPage.editExemption);
  	objPage.enter(objExemptionsPage.endDateOfRating, dataToEdit.get("EnddateOfRating"));
  	ReportLogger.INFO("Step 5: Adding End date of Rating Reason in the exemption");
  	objApasGenericFunctions.selectFromDropDown(objExemptionsPage.endRatingReason, dataToEdit.get("EndRatingReason"));
  	ReportLogger.INFO("Step 6: Click the SAVE button");
  	objPage.Click(ExemptionsPage.saveButton);
  	ReportLogger.INFO("Step 7: Verifying the Work Item is not generated on Entering the End Date of Rating for an Exemption having opened WI");
  	HashMap<String, ArrayList<String>> response_2  = salesforceAPI.select(sqlgetWIDetails);
  	String numWI = String.valueOf(response_2.get("Work_Item__c.Name").size());
  	softAssert.assertEquals(numWI, "1", "SMAB-T1926: Verify Work Item is not generated on Entering the End Date of Rating for an Exemption having opened WI");
  	String updateWIStatus = "SELECT Status__c FROM Work_Item__c where Name = '"+WIName+"'";
	salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");
	ReportLogger.INFO("Step 4: Logging Out from SF");
  	objApasGenericFunctions.logout();
 }

	@Test(description = "SMAB-T1978 APAS Verify the Supervisor is able to Approve the WI initial filing/changes on new Exemption Creation", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyWorkItemExemptionFilingChangesIsApproved(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	ReportLogger.INFO("Step 1: Login to the Salesforce ");
	objApasGenericFunctions.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
	objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
	
	//Step3: create a New Exemption record
	ReportLogger.INFO("Step 3: Create New Exemption");
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	//Get the WI Name from DB through the newly created Exemption
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	String sqlgetWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
	HashMap<String, ArrayList<String>> response  = salesforceAPI.select(sqlgetWIDetails);
    String WIName = response.get("Work_Item__c.Name").get(0);
    ReportLogger.INFO("Step 4: Logging Out from SF");
    objApasGenericFunctions.logout();
    ReportLogger.INFO("Step 5: Logging In as Approver - Data Admin");
  	objApasGenericFunctions.login(users.DATA_ADMIN);
  	
    //Step4: Opening the Work Item Module
    ReportLogger.INFO("Step 6: Search open App. module - Work Item Management from App Launcher");
  	objApasGenericFunctions.searchModule(modules.WORKITEM_MNGMNT);
  	//Step5: Click on the Main TAB - Home
  	ReportLogger.INFO("Step 7: Click on the Main TAB - Home");
  	objPage.Click(objWIHomePage.lnkTABHome);
  	ReportLogger.INFO("Step 8: Click on the Sub  TAB - Work Items");
  	objPage.Click(objWIHomePage.lnkTABWorkItems);
  	ReportLogger.INFO("Step 9: Click on the check box - Show RP");
  	objPage.Click(objWIHomePage.chkShowRP);
  	ReportLogger.INFO("Step 10: Click on Needs My Approval TAB");
  	objPage.Click(objWIHomePage.lnkTABNeedsMyApproval);
  	ReportLogger.INFO("Step 11: Search for the Work Item and select the checkbox");
  	objWIHomePage.clickCheckBoxForSelectingWI(WIName);
  	ReportLogger.INFO("Step 12: Click on the Approve button");
  	objPage.Click(objWIHomePage.btnApprove);
  	
	/*
	 * String actualSuccessAlertText =
	 * objPage.getElementText(objApasGenericPage.successAlert); String
	 * expectedSuccessAlertText = "Success\n Work item(s) processed successfully!";
	 * softAssert.assertEquals(actualSuccessAlertText, expectedSuccessAlertText,
	 * "SMAB-T1978: validating the Success alert on clicking the WI Approve button"
	 * );
	 */
  	ReportLogger.INFO("Step 13: Logging Out from SF");
  	objApasGenericFunctions.logout();
  	ReportLogger.INFO("Step 14: Logging IN to SF");
  	objApasGenericFunctions.login(loginUser);
  	
    //Step4: Opening the Work Item Module
    ReportLogger.INFO("Step 15: Search open App. module - Work Item Management from App Launcher");
  	objApasGenericFunctions.searchModule(modules.WORKITEM_MNGMNT);
  	//Step5: Click on the Main TAB - Home
  	ReportLogger.INFO("Step 16: Click on the Main TAB - Home");
  	objPage.Click(objWIHomePage.lnkTABHome);
  	ReportLogger.INFO("Step 17: Click on the Sub  TAB - Work Items");
  	objPage.Click(objWIHomePage.lnkTABWorkItems);
  	ReportLogger.INFO("Step 18: Click on the check box - Show RP");
  	objPage.Click(objWIHomePage.chkShowRP);
  	ReportLogger.INFO("Step 19: Click on the TAB - Completed");
  	objPage.Click(objWIHomePage.lnkTABCompleted);
  	WebElement actualWIName = objWIHomePage.searchWIinGrid(WIName);
  	ReportLogger.INFO("Step 20: Verifying the Approver has successfully Approved the Work Item");
  	softAssert.assertEquals(actualWIName, WIName, "SMAB-T1978: Approver has successfully Approved the Work Item");
  	objApasGenericFunctions.logout();
  	
  }

	@Test(description = "SMAB-T1981 APAS Verify the Supervisor is able to Return the WI initial filing/changes on new Exemption Creation", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyWorkItemExemptionFilingChangesIsReturned(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	ReportLogger.INFO("Step 1: Login to the Salesforce ");
	objApasGenericFunctions.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
	objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
	
	//Step3: create a New Exemption record
	ReportLogger.INFO("Step 3: Create New Exemption");
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	//Get the WI Name from DB through the newly created Exemption
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	String sqlgetWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
	HashMap<String, ArrayList<String>> response  = salesforceAPI.select(sqlgetWIDetails);
    String WIName = response.get("Work_Item__c.Name").get(0);    
    ReportLogger.INFO("Step 4: Logging OUT of SF");
  	objApasGenericFunctions.logout();
  	ReportLogger.INFO("Step 5: Logging IN as Approver - Data Admin");
  	objApasGenericFunctions.login(users.DATA_ADMIN);
  	
    //Step4: Opening the Work Item Module
    ReportLogger.INFO("Step 6: Search open App. module - Work Item Management from App Launcher");
  	objApasGenericFunctions.searchModule(modules.WORKITEM_MNGMNT);
  	//Step5: Click on the Main TAB - Home
  	ReportLogger.INFO("Step 7: Click on the Main TAB - Home");
  	objPage.Click(objWIHomePage.lnkTABHome);
  	ReportLogger.INFO("Step 8: Click on the Sub  TAB - Work Items");
  	objPage.Click(objWIHomePage.lnkTABWorkItems);
  	ReportLogger.INFO("Step 9: Click on the check box - Show RP");
  	objPage.Click(objWIHomePage.chkShowRP);
  	ReportLogger.INFO("Step 10: Click on TAB - Needs My Approval");
  	objPage.Click(objWIHomePage.lnkTABNeedsMyApproval);
  	ReportLogger.INFO("Step 11: Search and select the Work Item checkbox");
  	objWIHomePage.clickCheckBoxForSelectingWI(WIName);
  	ReportLogger.INFO("Step 12: Click on the Return TAB");
  	objPage.Click(objWIHomePage.btnReturn);
  	ReportLogger.INFO("Step 13: Enter the Return Reason in the text box");
  	objPage.enter(objWIHomePage.txtReturnedReason, "Return to Assignee");
  	ReportLogger.INFO("Step 14: Click on the SAVE button on the dialogbox");
  	objPage.Click(objWIHomePage.btnSaveOnReturnDlg);
	
  	/*
	 * String actualSuccessAlertText =
	 * objPage.getElementText(objApasGenericPage.successAlert); String
	 * expectedSuccessAlertText = "Success\n Work item(s) processed successfully!";
	 * softAssert.assertEquals(actualSuccessAlertText, expectedSuccessAlertText,
	 * "SMAB-T1981: validating the Success alert on clicking the WI Return button");
	 */
  	ReportLogger.INFO("Step 15: Logging OUT from SF");
  	objApasGenericFunctions.logout();
  	ReportLogger.INFO("Step 16: Logging IN SF");
  	objApasGenericFunctions.login(loginUser);
  	
    //Step4: Opening the Work Item Module
    ReportLogger.INFO("Step 17: Search open App. module - Work Item Management from App Launcher");
  	objApasGenericFunctions.searchModule(modules.WORKITEM_MNGMNT);
  	//Step5: Click on the Main TAB - Home
  	ReportLogger.INFO("Step 18: Click on the Main TAB - Home");
  	objPage.Click(objWIHomePage.lnkTABHome);
  	ReportLogger.INFO("Step 19: Click on the Sub  TAB - Work Items");
  	objPage.Click(objWIHomePage.lnkTABWorkItems);
  	ReportLogger.INFO("Step 20: Click on the check box - Show RP");
  	objPage.Click(objWIHomePage.chkShowRP);
  	ReportLogger.INFO("Step 21: Click the TAB - In Progress");
  	objPage.Click(objWIHomePage.lnkTABInProgress);
  	WebElement actualWIName = objWIHomePage.searchWIinGrid(WIName);
  	ReportLogger.INFO("Step 22: Verify the  Approver has successfully returned the Work Item to the Asignee");
  	softAssert.assertEquals(actualWIName, WIName, "SMAB-T1981: Approver has successfully returned the Work Item to the Asignee");
  	ReportLogger.INFO("Step 23: Logging OUT");
  	objApasGenericFunctions.logout();
  }	
}	
