package com.apas.Tests.WorkItemsTest.WorkItemWorkFlow;

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

public class WorkItemWorkflow_DisabledVeteranExemption_Tests extends TestBase {

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
	public void WorkItemWorkflow_DisabledVeteran_WorkItemGeneratedOnNewExemptionCreation(String loginUser) throws Exception {

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
		ReportLogger.INFO("Step 8: Click on the SUB TAB - My Submitted for Approval");
		objPage.Click(objWIHomePage.lnkTABMySubmittedforApproval);

		//Search the Work Item Name in the Grid 1st Column
		WebElement actualWIName = objWIHomePage.searchWIinGrid(WIName);

		ReportLogger.INFO("Step: Fetching Data for Work Item : " + WIName );
		HashMap<String, ArrayList<String>> rowData = objWIHomePage.getGridDataForRowString(WIName);

		//String actualRequestTypeName = objWIHomePage.searchRequestTypeNameonWIDetails(WIRequestType);
		String RequestTypeName  = "Disabled Veterans - Direct Review and Update - Initial filing/changes";
		String actualRequestTypeName = rowData.get("Request Type").get(0) ;

		ReportLogger.INFO("Step 9: Verifying on new Exemption creation Work Item '"+WIName+"' is generated of Request Type : '"+RequestTypeName+"'" );

		softAssert.assertEquals(actualWIName.getText(),WIName,"SMAB-T1922:Verify name of WI generated");
		softAssert.assertEquals(actualRequestTypeName,RequestTypeName,"SMAB-T1922:Verify RequestType Name of WI generated");

		objPage.Click(actualWIName);
		objPage.waitForElementToBeClickable(objWIHomePage.detailsTab);
		objPage.Click(objWIHomePage.detailsTab);
		//Validating that 'Use Code' and 'Street' field  gets automatically populated in the work item record
		objWIHomePage.waitForElementToBeVisible(10, objWIHomePage.referenceDetailsLabel);
		String linkedAPN=objApasGenericPage.getFieldValueFromAPAS("APN", "Information");
		String streetName=salesforceAPI.select("SELECT Situs_Street_Name__c  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ linkedAPN +"')").get("Situs_Street_Name__c").get(0);
		String useCodeValue=salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where name='"+ linkedAPN +"')").get("Name").get(0);

		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Use Code", "Reference Data Details"),useCodeValue,
				"SMAB-T2080: Validation that 'Use Code' fields getting automatically populated in the work item record");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Street", "Reference Data Details"),streetName,
				"SMAB-T2080: Validation that 'Street' fields getting automatically populated in the work item record");
		
		String updateWIStatus = "SELECT Id FROM Work_Item__c where Name = '"+WIName+"'";
		salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");

		ReportLogger.INFO("Step 11: Logging out from SF");

		objApasGenericPage.logout();

	}

	@Test(description = "SMAB-T1923: APAS system should generate a WI on updating the End Date of Rating for Existing Exemption", 
			dataProvider = "loginExemptionSupportStaff", 
			dataProviderClass = DataProviders.class , 
			groups = {"regression","DV_WorkItem_Exemption"})
	public void WorkItemWorkflow_DisabledVeteran_WorkItemGeneratedOnEnterEndDateRatingExistingExemption(String loginUser) throws Exception {

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
	public void WorkItemWorkflow_DisabledVeteran_WorkItemNotGeneratedOnExemptionWithOpenWI(String loginUser) throws Exception {

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
	public void WorkItemWorkflow_DisabledVeteran_WorkItemExemptionFilingChangesIsApproved(String loginUser) throws Exception {

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
		ReportLogger.INFO("Step 10: Click on Needs My Approval TAB");
		objPage.Click(objWIHomePage.needsMyApprovalTab);
		ReportLogger.INFO("Step 11: Search for the Work Item and select the checkbox");
		objWIHomePage.clickCheckBoxForSelectingWI(WIName);
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
	public void WorkItemWorkflow_DisabledVeteran_WorkItemExemptionFilingChangesIsReturned(String loginUser) throws Exception {

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
		ReportLogger.INFO("Step 21: Click the TAB - In Progress");
		objPage.Click(objWIHomePage.lnkTABInProgress);
		String actualWIName = objWIHomePage.searchandClickWIinGrid(WIName);
		ReportLogger.INFO("Step 22: Verify the  Approver has successfully returned the Work Item to the Asignee");
		softAssert.assertEquals(actualWIName, WIName, "SMAB-T1981: Approver has successfully returned the Work Item to the Asignee");
		ReportLogger.INFO("Step 23: Logging OUT");
		Thread.sleep(2000);
		objApasGenericPage.logout();
	}	

	/**
	 * This method is to verify that 1st level Approver is able to assign WI to a different 2nd level Approver than what is there in the relevant Work Pool
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2554,SMAB-T2556,SMAB-T2558: Verify that 1st level Approver is able to assign WIs to a different 2nd level Approver than what is there in the relevant Work Pool, Verify that 2nd level Approver is able to view 'Warning' message if user tries to assign WI to another user using 'Assign Level2 Approver' button", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DV_WorkItem_Exemption"})
	public void WorkItemWorkflow_DisabledVeteran_Level2ApproverIsAbleToAssignWorkItem(String loginUser) throws Exception {

		ReportLogger.INFO("Get the user names through SOQL query");
		String rpBusinessAdminName = salesforceAPI.getUserName(users.RP_BUSINESS_ADMIN);
		String dataAdminName = salesforceAPI.getUserName(users.DATA_ADMIN);
		String mappingStaffName = salesforceAPI.getUserName(users.MAPPING_STAFF);

		String expectedIndividualFieldMessage = "Complete this field.";
		String warningMsgOnAssignLevel2Approver = "warning\nYou are already the 2nd Level approver on one or more of the selected work items. If you want to delegate 2nd Level approval, then select a different user.";
		Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");


		//Step1: Login to the APAS application through Exemption Support Staff
		objApasGenericPage.login(loginUser);
		objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);

		//Step2: Opening the Exemption Module
		objApasGenericPage.searchModule(modules.EXEMPTIONS);

		//Step3: create a New Exemption record
		objPage.Click(objExemptionsPage.newExemptionButton);

		//Step4: Get the WI Name from DB generated through the newly created Exemption
		newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
		HashMap<String, ArrayList<String>> getWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
		String WIName = getWIDetails.get("Name").get(0);

		objApasGenericPage.logout();
		Thread.sleep(5000);

		//Step5: Login to the APAS application through RP Business Admin
		objApasGenericPage.login(users.RP_BUSINESS_ADMIN);

		//Step6: Opening the Work Item Module
		objApasGenericPage.searchModule(modules.HOME);

		//Step7: Click on the Main TAB - Home followed by Needs my Approval tab
		ReportLogger.INFO("Click on the Main TAB - Home");
		objPage.Click(objWIHomePage.lnkTABHome);
		ReportLogger.INFO("Click on the Sub  TAB - Work Items");
		objPage.Click(objWIHomePage.lnkTABWorkItems);
		ReportLogger.INFO("Click on Needs My Approval TAB");
		objPage.Click(objWIHomePage.needsMyApprovalTab);

		//Step8: Click on the Assign Level2 Supervisor button without selecting a WI
		objPage.javascriptClick(objPage.getButtonWithText(objWIHomePage.assignLevel2Approver));
		String alertMessage = objWIHomePage.getAlertMessage();
		String infoMessage = "Please select atleast one record.";
		softAssert.assertEquals(alertMessage,infoMessage,"SMAB-T2556: Unable to access 'Select Level2 Approver' pop-up screen");
		objWIHomePage.waitForElementToBeClickable(objWIHomePage.closeButton, 3);
		objWIHomePage.Click(objWIHomePage.closeButton);
		
		//Step9: Search for the Work Item and select the checkbox
		ReportLogger.INFO("Search for the Work Item and select the checkbox");
		objWIHomePage.clickCheckBoxForSelectingWI(WIName);

		//Step10: Click on the Assign Level2 Supervisor button and validate the details
		ReportLogger.INFO("Click the Assign Level2 Supervisor button");
		objPage.javascriptClick(objPage.getButtonWithText(objWIHomePage.assignLevel2Approver));
		softAssert.assertEquals(objPage.getElementText(objWIHomePage.headerLevel2Approver),"Select Level2 Approver",
				"SMAB-T2556: Validate the header of the pop-up screen");
		softAssert.assertTrue(!objWIHomePage.verifyElementExists(objWIHomePage.warningOnAssignLevel2ApproverScreen),
				"SMAB-T2558: Validate the warning message isn't displayed on the pop-up screen");

		objWIHomePage.enter(objWIHomePage.getWebElementWithLabel(objWIHomePage.wiLevel2ApproverDetailsPage), "");
		objWIHomePage.Click(objWIHomePage.getButtonWithText(objWIHomePage.SaveButton));
		softAssert.assertEquals(objWIHomePage.getIndividualFieldErrorMessage(objWIHomePage.wiLevel2ApproverDetailsPage),expectedIndividualFieldMessage,
				"SMAB-T2556: Validate that 'Level2 Approver' is a mandatory field");
		objWIHomePage.searchAndSelectOptionFromDropDown(objWIHomePage.wiLevel2ApproverDetailsPage, mappingStaffName);

		String successMessage = objWIHomePage.saveRecord();
		softAssert.assertEquals(successMessage,"success\nSuccess\nWork item(s) processed successfully!\nClose","SMAB-T2556 : Validate user is able to assign the WI" );
		objWIHomePage.waitForElementToBeClickable(6, objPage.getButtonWithText(objWIHomePage.assignLevel2Approver));

		//Step11: Verify the Status and Supervisor details in WI
		ReportLogger.INFO("Verify the Status and Supervisor details in WI");
		objWIHomePage.searchModule(modules.WORK_ITEM);
		objWIHomePage.globalSearchRecords(WIName); 
		objWIHomePage.Click(objWIHomePage.detailsTab);
		objWIHomePage.waitForElementToBeVisible(6, objWIHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2554: Validate the status of Work Item");

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2554: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),mappingStaffName,
				"SMAB-T2554: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2554: Validate the Current Approver on the Work Item");

		//Step12: Select & Approve the WI
		objApasGenericPage.searchModule(modules.HOME);
		ReportLogger.INFO("Click on the Main TAB - Home");
		objPage.Click(objWIHomePage.lnkTABHome);
		ReportLogger.INFO("Click on the Sub  TAB - Work Items");
		objPage.Click(objWIHomePage.lnkTABWorkItems);
		ReportLogger.INFO("Click on Needs My Approval TAB");
		objPage.Click(objWIHomePage.needsMyApprovalTab);

		objWIHomePage.clickCheckBoxForSelectingWI(WIName);
		ReportLogger.INFO("Click on the Approve button");
		objPage.javascriptClick(objWIHomePage.btnApprove);
		softAssert.assertEquals(successMessage,"success\nSuccess\nWork item(s) processed successfully!\nClose","SMAB-T2556 : Validate user is able to approve the WI" );
		Thread.sleep(5000);
		softAssert.assertTrue(!objWIHomePage.verifyElementExists(WIName),
				"SMAB-T2556: Validate the WI is not visible anymore");

		//Step13: Verify the Status and Supervisor details in WI
		ReportLogger.INFO("Verify the Status and Supervisor details in WI"); 
		objWIHomePage.searchModule(modules.WORK_ITEM);
		objWIHomePage.globalSearchRecords(WIName); 
		objWIHomePage.Click(objWIHomePage.detailsTab);
		objWIHomePage.waitForElementToBeVisible(6, objWIHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2556: Validate the status of Work Item");

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2556: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),mappingStaffName,
				"SMAB-T2556: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),mappingStaffName,
				"SMAB-T2556: Validate the Current Approver on the Work Item");

		//Step14: Logout from the application
		objApasGenericPage.logout();
		Thread.sleep(5000);

		//Step15: Login in the application
		objApasGenericPage.login(users.MAPPING_STAFF);

		//Step16: Opening the Work Item Module
		objApasGenericPage.searchModule(modules.HOME);

		//Step17: Click on the Main TAB - Home followed by Needs my Approval tab
		ReportLogger.INFO("Click on the Main TAB - Home");
		objPage.Click(objWIHomePage.lnkTABHome);
		ReportLogger.INFO("Click on the Sub  TAB - Work Items");
		objPage.Click(objWIHomePage.lnkTABWorkItems);
		ReportLogger.INFO("Click on Needs My Approval TAB");
		objPage.Click(objWIHomePage.needsMyApprovalTab);
		ReportLogger.INFO("Search for the Work Item and select the checkbox");

		//Step18: Search for the Work Item and select the checkbox
		objWIHomePage.clickCheckBoxForSelectingWI(WIName);

		//Step19: Click on the Assign Level2 Supervisor button and validate the details
		ReportLogger.INFO("Click the Assign Level2 Supervisor button");
		objPage.javascriptClick(objPage.getButtonWithText(objWIHomePage.assignLevel2Approver));

		softAssert.assertEquals(objPage.getElementText(objWIHomePage.headerLevel2Approver),"Select Level2 Approver",
				"SMAB-T2556: Validate the header of the pop-up screen");
		softAssert.assertEquals(objPage.getElementText(objWIHomePage.warningOnAssignLevel2Approver),warningMsgOnAssignLevel2Approver,
				"SMAB-T2558: Validate the warning message is displayed on the pop-up screen");
		objWIHomePage.enter(objWIHomePage.getWebElementWithLabel(objWIHomePage.wiLevel2ApproverDetailsPage), "");
		objWIHomePage.Click(objWIHomePage.getButtonWithText(objWIHomePage.SaveButton));
		softAssert.assertEquals(objWIHomePage.getIndividualFieldErrorMessage(objWIHomePage.wiLevel2ApproverDetailsPage),expectedIndividualFieldMessage,
				"SMAB-T2556: Validate that 'Level2 Approver' is a mandatory field");

		objWIHomePage.searchAndSelectOptionFromDropDown(objWIHomePage.wiLevel2ApproverDetailsPage, dataAdminName);

		String successMessage2 = objWIHomePage.saveRecord();
		softAssert.assertEquals(successMessage2,"success\nSuccess\nWork item(s) processed successfully!\nClose",
				"SMAB-T2556 : Validate user is able to assign the WI" );
		objWIHomePage.waitForElementToBeClickable(6, objPage.getButtonWithText(objWIHomePage.assignLevel2Approver));

		//Step20: Search for the Work Item and select the checkbox and then approve it
		objWIHomePage.clickCheckBoxForSelectingWI(WIName);
		ReportLogger.INFO("Click on the Approve button");
		objPage.javascriptClick(objWIHomePage.btnApprove);
		softAssert.assertEquals(successMessage,"success\nSuccess\nWork item(s) processed successfully!\nClose",
				"SMAB-T2556 : Validate user is able to approve the WI" );
		Thread.sleep(5000);
		softAssert.assertTrue(!objWIHomePage.verifyElementExists(WIName),
				"SMAB-T2556: Validate the WI is not visible anymore");

		//Step21: Logout from the application
		objApasGenericPage.logout();
		Thread.sleep(5000);

		//Step22: Login in the application through Data Admin
		objApasGenericPage.login(users.DATA_ADMIN);

		//Step23: Opening the Work Item Module
		objApasGenericPage.searchModule(modules.HOME);

		//Step24: Click on the Main TAB - Home followed by Needs my Approval tab
		ReportLogger.INFO("Click on the Main TAB - Home");
		objPage.Click(objWIHomePage.lnkTABHome);
		ReportLogger.INFO("Click on the Sub  TAB - Work Items");
		objPage.Click(objWIHomePage.lnkTABWorkItems);
		ReportLogger.INFO("Click on the check box - Show RP");
		objPage.Click(objWIHomePage.chkShowRP);
		ReportLogger.INFO("Click on Needs My Approval TAB");
		objPage.Click(objWIHomePage.needsMyApprovalTab);

		ReportLogger.INFO("Search for the Work Item and select the checkbox");
		objWIHomePage.clickCheckBoxForSelectingWI(WIName);
		ReportLogger.INFO("Click on the Approve button");
		objPage.javascriptClick(objWIHomePage.btnApprove);

		softAssert.assertEquals(successMessage,"success\nSuccess\nWork item(s) processed successfully!\nClose",
				"SMAB-T2556 : Validate user is able to approve the WI" );
		Thread.sleep(5000);
		softAssert.assertTrue(!objWIHomePage.verifyElementExists(WIName),
				"SMAB-T2556: Validate the WI is not visible anymore");

		//Step25: Verify the Status and Supervisor details in WI
		ReportLogger.INFO("Verify the Status and Supervisor details in WI");
		objWIHomePage.searchModule(modules.WORK_ITEM);
		objWIHomePage.globalSearchRecords(WIName); 
		objWIHomePage.Click(objWIHomePage.detailsTab);
		objWIHomePage.waitForElementToBeVisible(6, objWIHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiStatus, "Information"),"Completed",
				"SMAB-T2556: Validate the status of Work Item");

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2556: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),dataAdminName,
				"SMAB-T2556: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWIHomePage.getFieldValueFromAPAS(objWIHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),dataAdminName,
				"SMAB-T2556: Validate the Current Approver on the Work Item");


		//Step26: Logout from the application
		objApasGenericPage.logout();
		Thread.sleep(5000);

		//Step27: Login in the application through Exemption Support Staff
		objApasGenericPage.login(loginUser);

		//Step28: Opening the Work Item Module
		objApasGenericPage.searchModule(modules.HOME);

		//Step29: Click on the Main TAB - Home
		ReportLogger.INFO("Click on the Main TAB - Home");
		objPage.Click(objWIHomePage.lnkTABHome);
		ReportLogger.INFO("Click on the Sub  TAB - Work Items");
		objPage.Click(objWIHomePage.lnkTABWorkItems);

		//Step30: Verify that the WI is present in the Completed tab
		softAssert.assertTrue(!objWIHomePage.verifyElementExists(objWIHomePage.assignLevel2ApproverBtn),
				"SMAB-T2556: Validate that 'Assign Level2 Approver' button is not visible");

		//Step31: Click on the Completed tab
		ReportLogger.INFO("Click on the TAB - Completed");
		objPage.Click(objWIHomePage.lnkTABCompleted);

		//Step32: Verify that the WI is present in the Completed tab
		softAssert.assertTrue(!objWIHomePage.verifyElementExists(objWIHomePage.assignLevel2ApproverBtn),
				"SMAB-T2556: Validate that 'Assign Level2 Approver' button is not visible");

		//Step33: Verify that the WI is present in the Completed tab
		ReportLogger.INFO("Verify that the WI is present in the Completed tab");
		String actualWIName = objWIHomePage.searchandClickWIinGrid(WIName);
		softAssert.assertEquals(actualWIName, WIName, "SMAB-T2556: Validate that the WI is present in the Completed tab");

		objApasGenericPage.logout();

	}

}	
