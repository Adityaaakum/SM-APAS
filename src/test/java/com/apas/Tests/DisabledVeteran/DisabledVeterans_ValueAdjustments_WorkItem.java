package com.apas.Tests.DisabledVeteran;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.apas.PageObjects.ValueAdjustmentsPage;
import com.apas.PageObjects.WorkItemMngmntHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;

public class DisabledVeterans_ValueAdjustments_WorkItem extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objExemptionsPage;
	ValueAdjustmentsPage ObjValueAdjustmentPage;
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
		ObjValueAdjustmentPage = new ValueAdjustmentsPage(driver);
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
	
	@Test(description = "SMAB-T2103 APAS system should generate an Annual Exemption amount verification WI on editing/entering few fields in VA", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyWorkItemGeneratedOnEditingVAFields(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	ReportLogger.INFO("Step 1: Login to the Salesforce ");
	objApasGenericFunctions.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
	objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
	
	//Step3: Click on New button to create Exemption
	ReportLogger.INFO("Step 3: Create New Exemption");
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	//Step4: Enter the Exemption details 
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	String applicationDate=objExemptionsPage.dateApplicationReceivedExemptionDetails.getText().trim();
	//Ste5: Click on the Value Adjustments link
	ReportLogger.INFO("Step 4: Click on the TAB Value Adjustments");
	objPage.Click(ObjValueAdjustmentPage.valueAdjustmentTab);
	ReportLogger.INFO("Step 5: Click on the link View All");
	objPage.waitForElementToBeVisible(ObjValueAdjustmentPage.viewAllLink, 10);
	objPage.Click(ObjValueAdjustmentPage.viewAllLink);
	String date = DateUtil.getCurrentDate("MM/dd/yyyy");
	String strRollYear = ExemptionsPage.determineRollYear(date);
	strRollYear = String.valueOf(Integer.parseInt(strRollYear)-1);
	ReportLogger.INFO("Step 6: Click on the VA Name link that is one less then the current Roll Year");
	ObjValueAdjustmentPage.clickVA(strRollYear);
	String vANameValue = objPage.getElementText(ObjValueAdjustmentPage.vAnameValue);
	ReportLogger.INFO("Step 7: Click on the Details link");
	objPage.Click(objExemptionsPage.exemptionDetailsTab);
	ReportLogger.INFO("Step 8: Click on the Edit Icon on the Determination Field");
	objPage.Click(ObjValueAdjustmentPage.editButton);
	objPage.waitForElementToBeClickable(ObjValueAdjustmentPage.vaEditDeterminationDropDown, 10);
	ReportLogger.INFO("Step 9: Select the Low-Income Disabled Veterans Exemption option from drop down");
	objApasGenericFunctions.selectFromDropDown(ObjValueAdjustmentPage.vaEditDeterminationDropDown,"Low-Income Disabled Veterans Exemption");
	String dateAfterAppdate=DateUtil.getFutureORPastDate(applicationDate, 1, "MM/dd/yyyy");
	ReportLogger.INFO("Step 10: Select the Annual Form Receive Date :"+dateAfterAppdate);
	objPage.enter(ObjValueAdjustmentPage.vaAnnualFormReceiveddate,dateAfterAppdate);
	ReportLogger.INFO("Step 11: Enter the Annual Household income : 10000");
	objPage.enter(ObjValueAdjustmentPage.vaTotalAnuualHouseholdIncome,"10000");
	ReportLogger.INFO("Step 12: Select the Penalty Adjustments Reason:Supervisory Judgement");
	objPage.enter(ObjValueAdjustmentPage.penaltyAdjustmentReason, "Supervisory Judgement");
	ReportLogger.INFO("Step 13: Enter the Penalty Amount User Adjusted : 2000 ");
	objPage.Select(ObjValueAdjustmentPage.penaltyAmountUserAdjusted, "2000");
	ReportLogger.INFO("Step 14: Click on the SAVE button");
	objPage.Click(ExemptionsPage.saveButton);
	
	String sqlgetWIDeatilsForVA = objWIHomePage.getWorkItemDetailsForVA(vANameValue, "In Pool", "Disabled Veterans", "Review and Update", "Annual exemption amount verification");
	HashMap<String, ArrayList<String>> response  = salesforceAPI.select(sqlgetWIDeatilsForVA);
    String WIName = response.get("Work_Item__c.Name").get(0);
    String WIRequestType = response.get("Work_Item__c.Request_Type__c").get(0);
    
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
  	ReportLogger.INFO("Step 19: Click on the SUB TAB - In POOL");
  	objPage.Click(objWIHomePage.lnkTABInPool);
  	WebElement actualWIName = objWIHomePage.searchWIinGrid(WIName);
  	ReportLogger.INFO("Step 20: Search and select the Work Item from the Grid");
  	objPage.Click(actualWIName);
  	ReportLogger.INFO("Step 21: Click on the link - Linked Items");
  	objPage.Click(objWIHomePage.lnkLinkedItems);
  	ReportLogger.INFO("Step 22: Verify the VA linked with the WI :"+vANameValue);
  	WebElement actualVAName = objWIHomePage.searchLinkedExemption(vANameValue);
  	ReportLogger.INFO("Step 23: Click on the link : Details");
  	objPage.Click(objWIHomePage.lnkDetails);
  	WebElement actualRequestTypeName = objWIHomePage.searchRequestTypeNameonWIDetails(WIRequestType);
  	ReportLogger.INFO("Step 24: Verify the Request Type is as per the Naming Convention :"+actualRequestTypeName);
  	String RequestTypeName  = "Disabled Veterans - Review and Update - Annual exemption amount verification";
    ReportLogger.INFO("Step 25: Verifying Work Item is generated , Work Item Request Name , VA Link on creation of Work Item");
  	
  	boolean condition_1 = false;
  	boolean condition_2 = false;
  	boolean condition_3 = false;
  	
  	condition_1 = actualWIName.toString().equals(WIName);
  	condition_2 = actualVAName.toString().equals(vANameValue);
  	condition_3 = actualRequestTypeName.toString().equals(RequestTypeName); 	
  	
  	if(condition_1 && condition_2 && condition_3) {
  		softAssert.assertTrue(true, "SMAB-T2103: Verifying Work Item is generated , Work Item Request Name , VA Link on creation of Work Item");
  		String updateWIStatus = "SELECT Status__c FROM Work_Item__c where Name = '"+WIName+"'";
  	    salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");
  	}else {
  		softAssert.assertTrue(false, "SMAB-T2103: Verifying Work Item is generated , Work Item Request Name , VA Link on creation of Work Item");
  	}
  	ReportLogger.INFO("Step 11: Logging out from SF");
  	objApasGenericFunctions.logout();
 }

	@Test(description = "SMAB-T2104 APAS system should not generate an Annual Exemption amount verification WI on editing/entering few fields in V for already opened WI", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyWorkItemNotGeneratedOnEditingVAFieldsWithOpenWI(String loginUser) throws Exception {
		
		Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ReportLogger.INFO("Step 1: Login to the Salesforce ");
		objApasGenericFunctions.login(loginUser);
		objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
		
		//Step2: Opening the Exemption Module
		ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
		objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
		
		//Step3: Click on New button to create Exemption
		ReportLogger.INFO("Step 3: Create New Exemption");
		objPage.Click(objExemptionsPage.newExemptionButton);
		
		//Step4: Enter the Exemption details 
		newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
		String applicationDate=objExemptionsPage.dateApplicationReceivedExemptionDetails.getText().trim();
		//Ste5: Click on the Value Adjustments link
		ReportLogger.INFO("Step 4: Click on Value Adjustment TAB");
		objPage.Click(ObjValueAdjustmentPage.valueAdjustmentTab);
		ReportLogger.INFO("Step 5: Click on View All link");
		objPage.waitForElementToBeVisible(ObjValueAdjustmentPage.viewAllLink, 10);
		objPage.Click(ObjValueAdjustmentPage.viewAllLink);
		
		String date = DateUtil.getCurrentDate("MM/dd/yyyy");
		String strRollYear = ExemptionsPage.determineRollYear(date);
		strRollYear = String.valueOf(Integer.parseInt(strRollYear)-1);
		ReportLogger.INFO("Step 5: Click on the VA Name link having Roll Year :"+strRollYear);
		ObjValueAdjustmentPage.clickVA(strRollYear);
		ReportLogger.INFO("Step 6: Click on the VA Name link");
		String vANameValue = objPage.getElementText(ObjValueAdjustmentPage.vAnameValue);
		ReportLogger.INFO("Step 7: Click on the Details  link");
		objPage.Click(objExemptionsPage.exemptionDetailsTab);
		ReportLogger.INFO("Step 8: Click on the Edit Icon for Determination field");
		objPage.Click(ObjValueAdjustmentPage.editButton);
		ReportLogger.INFO("Step 9: Select the Lo-Income... option from the drop down");
		objPage.waitForElementToBeClickable(ObjValueAdjustmentPage.vaEditDeterminationDropDown, 10);
		objApasGenericFunctions.selectFromDropDown(ObjValueAdjustmentPage.vaEditDeterminationDropDown,"Low-Income Disabled Veterans Exemption");
		String dateAfterAppdate=DateUtil.getFutureORPastDate(applicationDate, 1, "MM/dd/yyyy");
		ReportLogger.INFO("Step 10: Enter the Annual Form Receive Date :"+dateAfterAppdate);
		objPage.enter(ObjValueAdjustmentPage.vaAnnualFormReceiveddate,dateAfterAppdate);
		ReportLogger.INFO("Step 11: Enter the Annual household income : 10000");
		objPage.enter(ObjValueAdjustmentPage.vaTotalAnuualHouseholdIncome,"10000");
		ReportLogger.INFO("Step 12: Enter the Penalty Adjustment Reason : Supervisory Judgement");
		objPage.enter(ObjValueAdjustmentPage.penaltyAdjustmentReason, "Supervisory Judgement");
		ReportLogger.INFO("Step 13: Enter the Penalty Amount User Adjusted : 2000");
		objPage.Select(ObjValueAdjustmentPage.penaltyAmountUserAdjusted, "2000");
		ReportLogger.INFO("Step 14: Click on the SAVE button");
		objPage.Click(ExemptionsPage.saveButton);
		
		//====================Edit VA fields again=========================
		ReportLogger.INFO("Step 15: Click on the Edit Icon for Determination field");
		objPage.Click(ObjValueAdjustmentPage.editButton);
		objPage.waitForElementToBeClickable(ObjValueAdjustmentPage.vaEditDeterminationDropDown, 10);
		ReportLogger.INFO("Step 16: Select the Determination option : Basic Disabled Veterans Exemption");
		objApasGenericFunctions.selectFromDropDown(ObjValueAdjustmentPage.vaEditDeterminationDropDown,"Basic Disabled Veterans Exemption");
		ReportLogger.INFO("Step 17: Enter the Annual Form Received Date : ");
		objPage.enter(ObjValueAdjustmentPage.vaAnnualFormReceiveddate,"");
		ReportLogger.INFO("Step 18: Enter the Annual House Hold Income : ");
		objPage.enter(ObjValueAdjustmentPage.vaTotalAnuualHouseholdIncome,"");
		ReportLogger.INFO("Step 19: Enter the Penalty Adjustment Reason : --None--");
		objPage.enter(ObjValueAdjustmentPage.penaltyAdjustmentReason, "--None--");
		ReportLogger.INFO("Step 20: Enter the Penalty Amount Adjusted : ");
		objPage.Select(ObjValueAdjustmentPage.penaltyAmountUserAdjusted, "");
		ReportLogger.INFO("Step 21: Click on the SAVE button");
		objPage.Click(ExemptionsPage.saveButton);
		
		String sqlgetWIDeatilsForVA = objWIHomePage.getWorkItemDetailsForVA(vANameValue, "In Pool", "Disabled Veterans", "Review and Update", "Annual exemption amount verification");
		HashMap<String, ArrayList<String>> response  = salesforceAPI.select(sqlgetWIDeatilsForVA);
		
		String numWI = String.valueOf(response.get("Work_Item__c.Name").size());
		softAssert.assertEquals(numWI, "1", "SMAB-T2104: verify Work Item is not generated on entering or editing the VA proposed fields having opened WI");
		
	}

	@Test(description = "SMAB-T1979 Approver should be able to Approve the WI - Annual Exemption Amount Verification" , dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyAnnualExemptionAmountVerificationWIIsApproved(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	ReportLogger.INFO("Step 1: Login to the Salesforce ");
	objApasGenericFunctions.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
	objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
	
	//Step3: Click on New button to create Exemption
	ReportLogger.INFO("Step 3: Create New Exemption");
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	//Step4: Enter the Exemption details 
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	String applicationDate=objExemptionsPage.dateApplicationReceivedExemptionDetails.getText().trim();
	//Ste5: Click on the Value Adjustments link
	ReportLogger.INFO("Step 4: Click on the Value Adjustments TAB");
	objPage.Click(ObjValueAdjustmentPage.valueAdjustmentTab);
	ReportLogger.INFO("Step 5: Click on View All link");
	objPage.waitForElementToBeVisible(ObjValueAdjustmentPage.viewAllLink, 10);
	objPage.Click(ObjValueAdjustmentPage.viewAllLink);
	
	String date = DateUtil.getCurrentDate("MM/dd/yyyy");
	String strRollYear = ExemptionsPage.determineRollYear(date);
	strRollYear = String.valueOf(Integer.parseInt(strRollYear)-1);
    
	ReportLogger.INFO("Step 6: Click on VA Name link for the Roll year : "+strRollYear);
	ObjValueAdjustmentPage.clickVA(strRollYear);
	String vANameValue = objPage.getElementText(ObjValueAdjustmentPage.vAnameValue);
	ReportLogger.INFO("Step 7: Click on the Exemption Details TAB");
	objPage.Click(objExemptionsPage.exemptionDetailsTab);
	ReportLogger.INFO("Step 8: Click on the Edit Icon for the Determination field");
	objPage.Click(ObjValueAdjustmentPage.editButton);
	objPage.waitForElementToBeClickable(ObjValueAdjustmentPage.vaEditDeterminationDropDown, 10);
	ReportLogger.INFO("Step 9: Select from the Determination option : Low-Income ..");
	objApasGenericFunctions.selectFromDropDown(ObjValueAdjustmentPage.vaEditDeterminationDropDown,"Low-Income Disabled Veterans Exemption");
	String dateAfterAppdate=DateUtil.getFutureORPastDate(applicationDate, 1, "MM/dd/yyyy");
	ReportLogger.INFO("Step 10: Enter the Annual Form Received Date :"+dateAfterAppdate );
	objPage.enter(ObjValueAdjustmentPage.vaAnnualFormReceiveddate,dateAfterAppdate);
	ReportLogger.INFO("Step 11: Enter the Annual House hold Income : 10000" );
	objPage.enter(ObjValueAdjustmentPage.vaTotalAnuualHouseholdIncome,"10000");
	ReportLogger.INFO("Step 12: Enter the Penalty adjustment reason :Supervisory Judgement");
	objPage.enter(ObjValueAdjustmentPage.penaltyAdjustmentReason, "Supervisory Judgement");
	ReportLogger.INFO("Step 13: Enter the Penalty adjustment Amount : 2000");
	objPage.Select(ObjValueAdjustmentPage.penaltyAmountUserAdjusted, "2000");
	ReportLogger.INFO("Step 14: Click on the SAVE button");
	objPage.Click(ExemptionsPage.saveButton);
	
	String sqlgetWIDeatilsForVA = objWIHomePage.getWorkItemDetailsForVA(vANameValue, "In Pool", "Disabled Veterans", "Review and Update", "Annual exemption amount verification");
	HashMap<String, ArrayList<String>> response  = salesforceAPI.select(sqlgetWIDeatilsForVA);
    String WIName = response.get("Work_Item__c.Name").get(0);
    ReportLogger.INFO("Step 15: Logging OUT from SF");
    objApasGenericFunctions.logout();
    ReportLogger.INFO("Step 16: Logging IN as a DATA ADMIN");
  	objApasGenericFunctions.login(users.DATA_ADMIN);
  	
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
  	ReportLogger.INFO("Step 21: Click on the TAB Needs My Approval");
  	objPage.Click(objWIHomePage.lnkTABNeedsMyApproval);
  	ReportLogger.INFO("Step 22: Search and select the work item :"+WIName);
  	objWIHomePage.clickCheckBoxForSelectingWI(WIName);
  	ReportLogger.INFO("Step 23: Click on the Approve button");
  	objPage.Click(objWIHomePage.btnApprove);
  	
	/*
	 * String actualSuccessAlertText =
	 * objPage.getElementText(objApasGenericPage.successAlert); String
	 * expectedSuccessAlertText = "Success\n Work item(s) processed successfully!";
	 * softAssert.assertEquals(actualSuccessAlertText, expectedSuccessAlertText,
	 * "SMAB-T1978: validating the Success alert on clicking the WI Approve button"
	 * );
	 */
  	ReportLogger.INFO("Step 24: Logging OUT from SF");
  	objApasGenericFunctions.logout();
  	ReportLogger.INFO("Step 25: Logging IN SF");
  	objApasGenericFunctions.login(loginUser);
  	
    //Step4: Opening the Work Item Module
    ReportLogger.INFO("Step 26: Search open App. module - Work Item Management from App Launcher");
  	objApasGenericFunctions.searchModule(modules.WORKITEM_MNGMNT);
  	//Step5: Click on the Main TAB - Home
  	ReportLogger.INFO("Step 27: Click on the Main TAB - Home");
  	objPage.Click(objWIHomePage.lnkTABHome);
  	ReportLogger.INFO("Step 28: Click on the Sub  TAB - Work Items");
  	objPage.Click(objWIHomePage.lnkTABWorkItems);
  	ReportLogger.INFO("Step 29: Click on the check box - Show RP");
  	objPage.Click(objWIHomePage.chkShowRP);
  	ReportLogger.INFO("Step 30: Click on the Completed TAB");
  	objPage.Click(objWIHomePage.lnkTABCompleted);
  	ReportLogger.INFO("Step 31: Search work item under the Completed TAB :"+WIName);
  	WebElement actualWIName = objWIHomePage.searchWIinGrid(WIName);
  	ReportLogger.INFO("Step 32: Verifying the Approver has successfully Approved the Work Item");
  	softAssert.assertEquals(actualWIName, WIName, "SMAB-T1979: Approver has successfully Approved the Work Item");
  	objApasGenericFunctions.logout();
	
  }

	@Test(description = "SMAB-T1986 Approver should be able to Return the WI - Annual Exemption Amount Verification" , dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyAnnualExemptionAmountVerificationWIIsReturned(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	ReportLogger.INFO("Step 1: Login to the Salesforce ");
	objApasGenericFunctions.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
	objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
	
	//Step3: Click on New button to create Exemption
	ReportLogger.INFO("Step 3: Create New Exemption");
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	//Step4: Enter the Exemption details 
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	String applicationDate=objExemptionsPage.dateApplicationReceivedExemptionDetails.getText().trim();
	//Ste5: Click on the Value Adjustments link
	ReportLogger.INFO("Step 4: Click on the Value Adjustment TAB");
	objPage.Click(ObjValueAdjustmentPage.valueAdjustmentTab);
	ReportLogger.INFO("Step 5: Click on the View ALL link");
	objPage.waitForElementToBeVisible(ObjValueAdjustmentPage.viewAllLink, 10);
	objPage.Click(ObjValueAdjustmentPage.viewAllLink);
	String date = DateUtil.getCurrentDate("MM/dd/yyyy");
	String strRollYear = ExemptionsPage.determineRollYear(date);
	strRollYear = String.valueOf(Integer.parseInt(strRollYear)-1);
	ReportLogger.INFO("Step 6: Click on the VA Name link :"+strRollYear);
	ObjValueAdjustmentPage.clickVA(strRollYear);
	String vANameValue = objPage.getElementText(ObjValueAdjustmentPage.vAnameValue);
	ReportLogger.INFO("Step 7: Click on the Exemption Details TAB");
	objPage.Click(objExemptionsPage.exemptionDetailsTab);
	ReportLogger.INFO("Step 8: Click on the Edit Icon for Determination Field");
	objPage.Click(ObjValueAdjustmentPage.editButton);
	objPage.waitForElementToBeClickable(ObjValueAdjustmentPage.vaEditDeterminationDropDown, 10);
	ReportLogger.INFO("Step 9: Select the Determination option : Low-Income ...");
	objApasGenericFunctions.selectFromDropDown(ObjValueAdjustmentPage.vaEditDeterminationDropDown,"Low-Income Disabled Veterans Exemption");
	String dateAfterAppdate=DateUtil.getFutureORPastDate(applicationDate, 1, "MM/dd/yyyy");
	ReportLogger.INFO("Step 10: Enter the Annual Form Received Date : "+dateAfterAppdate);
	objPage.enter(ObjValueAdjustmentPage.vaAnnualFormReceiveddate,dateAfterAppdate);
	ReportLogger.INFO("Step 11: Enter the Annual House Hold Incone : 10000");
	objPage.enter(ObjValueAdjustmentPage.vaTotalAnuualHouseholdIncome,"10000");
	ReportLogger.INFO("Step 12: Enter the Penalty Adjustment Reason :Supervisory Judgement" );
	objPage.enter(ObjValueAdjustmentPage.penaltyAdjustmentReason, "Supervisory Judgement");
	ReportLogger.INFO("Step 13: Enter the Penalty Amount User Adjusted : 2000" );
	objPage.Select(ObjValueAdjustmentPage.penaltyAmountUserAdjusted, "2000");
	ReportLogger.INFO("Step 14: Click on the SAVE button" );
	objPage.Click(ExemptionsPage.saveButton);
	
	String sqlgetWIDeatilsForVA = objWIHomePage.getWorkItemDetailsForVA(vANameValue, "In Pool", "Disabled Veterans", "Review and Update", "Annual exemption amount verification");
	HashMap<String, ArrayList<String>> response  = salesforceAPI.select(sqlgetWIDeatilsForVA);
    String WIName = response.get("Work_Item__c.Name").get(0);
    ReportLogger.INFO("Step 15: Logging OUT from SF" );
    objApasGenericFunctions.logout();
    ReportLogger.INFO("Step 16: Logging IN as DATA ADMIN");
  	objApasGenericFunctions.login(users.DATA_ADMIN);
  	
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
  	ReportLogger.INFO("Step 21: Click on the TAB Needs My Approval" );
  	objPage.Click(objWIHomePage.lnkTABNeedsMyApproval);
  	ReportLogger.INFO("Step 22: Search and Select the work item from the grid :"+WIName);
  	objWIHomePage.clickCheckBoxForSelectingWI(WIName);
  	ReportLogger.INFO("Step 23: Click on the Return button" );
  	objPage.Click(objWIHomePage.btnReturn);
  	ReportLogger.INFO("Step 24: Enter the Return Reason : Return to Assignee" );
  	objPage.enter(objWIHomePage.txtReturnedReason, "Return to Assignee");
  	ReportLogger.INFO("Step 25: Click on the SAVE button" );
  	objPage.Click(objWIHomePage.btnSaveOnReturnDlg);
	
  	/*
	 * String actualSuccessAlertText =
	 * objPage.getElementText(objApasGenericPage.successAlert); String
	 * expectedSuccessAlertText = "Success\n Work item(s) processed successfully!";
	 * softAssert.assertEquals(actualSuccessAlertText, expectedSuccessAlertText,
	 * "SMAB-T1981: validating the Success alert on clicking the WI Return button");
	 */
  	ReportLogger.INFO("Step 26: Logging OUT from SF" );
  	objApasGenericFunctions.logout();
  	ReportLogger.INFO("Step 27: Logging IN SF" );
  	objApasGenericFunctions.login(loginUser);
  	
    //Step4: Opening the Work Item Module
    ReportLogger.INFO("Step 28: Search open App. module - Work Item Management from App Launcher");
  	objApasGenericFunctions.searchModule(modules.WORKITEM_MNGMNT);
  	//Step5: Click on the Main TAB - Home
  	ReportLogger.INFO("Step 29: Click on the Main TAB - Home");
  	objPage.Click(objWIHomePage.lnkTABHome);
  	ReportLogger.INFO("Step 30: Click on the Sub  TAB - Work Items");
  	objPage.Click(objWIHomePage.lnkTABWorkItems);
  	ReportLogger.INFO("Step 31: Click on the check box - Show RP");
  	objPage.Click(objWIHomePage.chkShowRP);
  	ReportLogger.INFO("Step 32: Click on the TAB - In Progress");
  	objPage.Click(objWIHomePage.lnkTABInProgress);
  	ReportLogger.INFO("Step 33: Search the work item in the Grid :"+WIName);
  	WebElement actualWIName = objWIHomePage.searchWIinGrid(WIName);
  	ReportLogger.INFO("Step 34: Verify the  Approver has successfully returned the Work Item to the Asignee");
  	softAssert.assertEquals(actualWIName, WIName, "SMAB-T1986: Approver has successfully returned the Work Item to the Asignee");
  	ReportLogger.INFO("Step 35: Logging OUT from SF");
  	objApasGenericFunctions.logout();
  }	

    
}
