package com.apas.Tests.OwnershipAndTransfer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.AppraisalActivityPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.ValueAdjustmentsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class CIO_HomeOwnerExemption_Test extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ParcelsPage objParcelsPage;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objExemptionsPage;
	CIOTransferPage objCIOTransferPage;
	Util objUtil;
	SoftAssertion softAssert;
	WorkItemHomePage objWorkItemHomePage;
	String homeOwnerExemptionData;
	String unrecordedEventData;
	ValueAdjustmentsPage ObjValueAdjustmentPage;
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	AppraisalActivityPage objAppraisalActivity;
	
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objParcelsPage = new ParcelsPage(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objCIOTransferPage = new CIOTransferPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		homeOwnerExemptionData = testdata.HOME_OWNER_EXEMPTION_DATA;
		unrecordedEventData = testdata.UNRECORDED_EVENT_DATA;
		objApasGenericPage.updateRollYearStatus("Closed", "2021");
		ObjValueAdjustmentPage = new ValueAdjustmentsPage(driver);
		objMappingPage = new MappingPage(driver);
		objAppraisalActivity= new AppraisalActivityPage(driver);

	}
	
	/**
	 Below test case is used to validate,
	 -Exemption value, Exemption Type and Qualification fields are not retained if a Transfer Code with 'Retain Exemption' as 'No' is selected
	 **/
	
	/** Following TEST has been disabled for automation execution as this functionality 
	 *  is no more applicable post implementation on SMAB-10049.
	 *	Defect that was raised for automation failure :  SMAB-14601
	 */
	
	@Test(description = "SMAB-T3479 : Validate Exemption value, Exemption Type and Qualification fields are not retained if a Transfer Code with 'Retain Exemption' as 'No' is selected", groups = {"Regression","ChangeInOwnershipManagement", "HomeOwnerExemption"}, dataProvider = "loginCIOStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class, enabled=false)
	public void HomeOwnerExemption_NoExemptionRetain_UnrecordedTransfer(String loginUser) throws Exception {
		
		//Data Setup
		String execEnv = System.getProperty("region");
		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "DataToCreateGranteeToVerifyHOE");
				
		String queryForActiveAPN = "SELECT Name,Id FROM Parcel__c where Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') Limit 1";
		String apn = salesforceAPI.select(queryForActiveAPN).get("Name").get(0);
		String apnId = salesforceAPI.select(queryForActiveAPN).get("Id").get(0);
		
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT id, Name FROM PUC_Code__c where Name Not in ('99-RETIRED PARCEL') and Disabled_Veteran_Exemption__c = true limit 1");
		salesforceAPI.update("Parcel__c", apnId, "PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		
		Map<String, String> dataToCreateHomeOwnerExemptionMap = objUtil.generateMapFromJsonFile(
				homeOwnerExemptionData, "NewHOECreation");
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(
				unrecordedEventData, "UnrecordedEventCreation");
		
		//Step1: Login to the APAS application
		objApasGenericPage.login(users.SYSTEM_ADMIN);
				
		//Step2: Open the Parcel module and add values for Exemption fields
		driver.navigate().to("https://smcacre--"+execEnv+
				 ".lightning.force.com/lightning/r/Parcel__c/"+apnId+"/view");
		objCIOTransferPage.waitForElementToBeVisible(6, objParcelsPage.componentActionsButtonText);
		objParcelsPage.Click(objParcelsPage.editPencilIconForExemptionOnDetailPage);
		objCIOTransferPage.waitForElementToBeVisible(6, objParcelsPage.saveParcelButton);
		objApasGenericPage.scrollToElement(objApasGenericPage.getWebElementWithLabel(objParcelsPage.exemptionLabel));
		objExemptionsPage.enter(objParcelsPage.exemptionLabel, dataToCreateHomeOwnerExemptionMap.get("Exemption"));
		objExemptionsPage.selectMultipleValues(dataToCreateHomeOwnerExemptionMap.get("ExemptionType"), objParcelsPage.exemptionTypeLabel);
		objExemptionsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.saveParcelButton));
		
		/*Step3: Open Exemption and create HOE*/
		ReportLogger.INFO("Create HOE");
		objParcelsPage.openParcelRelatedTab(objParcelsPage.exemptionRelatedTab);
		objExemptionsPage.createHomeOwnerExemption(dataToCreateHomeOwnerExemptionMap);
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		ReportLogger.INFO("Exemption Name : " + exemptionName);
		
		softAssert.assertTrue(exemptionName.contains("EXMPTN"),
				"SMAB-T3479: Validate user is able to create Exemption with mandtory fields'");
		
		objApasGenericPage.logout();
		Thread.sleep(5000);
		
		//Step4: Login from CIO Staff and update the Transfer code with Retain Exemption as No on CIO Transfer activity
		objApasGenericPage.login(users.CIO_STAFF);
		driver.navigate().to("https://smcacre--"+execEnv+
				 ".lightning.force.com/lightning/r/Parcel__c/"+apnId+"/view");
		objCIOTransferPage.waitForElementToBeVisible(6, objParcelsPage.componentActionsButtonText);
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		String recordeAPNTransferID = driver.getCurrentUrl().split("/")[6];
		
		objCIOTransferPage.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);
		
		softAssert.assertTrue(!objCIOTransferPage.verifyElementExists(objCIOTransferPage.warningMessageArea),
				"SMAB-T3287: Validate that no warning message is displayed on CIO Transfer screen for Active Parcel");
		
		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		ReportLogger.INFO("Update the Transfer Code with Retain Exemption as 'Yes'");
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.quickActionButtonDropdownIcon,10);
		
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		Thread.sleep(5000); //Added to avoid regression failure
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
				"SMAB-T3479: Validate the Transfer Code on CIO Transfer screen");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferDescriptionLabel, ""),"Chg of Own, Ass, Sale",
				"SMAB-T3479: Validate the Transfer Description on CIO Transfer screen");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.exemptionRetainLabel, ""),"No",
				"SMAB-T3479: Validate the Exemption Retain field on CIO Transfer screen");
		
		//Step5: Submit for Approval
		objCIOTransferPage.clickQuickActionButtonOnTransferActivity("Submit for Approval");
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButton);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
		Thread.sleep(2000);
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferStatusLabel, "System Information"),"Submitted for Approval",
				"SMAB-T3479: Validate the Transfer Activity status on CIO Transfer screen");
		
		objApasGenericPage.logout();
		Thread.sleep(5000);
		
		//Step6: Login from Exemption Staff and verify the field values
		objApasGenericPage.login(users.EXEMPTION_SUPPORT_STAFF);
		driver.navigate().to("https://smcacre--"+execEnv+
				 ".lightning.force.com/lightning/r/Parcel__c/"+apnId+"/view");
		objCIOTransferPage.waitForElementToBeVisible(6, objParcelsPage.componentActionsButtonText);
		objApasGenericPage.scrollToElement(objApasGenericPage.getWebElementWithLabel(objParcelsPage.exemptionLabel));
		
		softAssert.assertTrue(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionTypeLabel, "Summary Values").equals(""),
				"SMAB-T3479: Validate that 'Home Owner' value is removed from Exemption Type field on Parcel record");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionLabel, "Summary Values"),"$0",
				"SMAB-T3479: Validate that 'Exemption' amount is removed from Exemption field on Parcel record");
		
		ReportLogger.INFO("Validate the Qualification status on HOE");
		String queryForExemptionId = "SELECT Id FROM Exemption__c where Name = '" + exemptionName + "'";
		String exemptionId = salesforceAPI.select(queryForExemptionId).get("Id").get(0);
		
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Exemption__c/" + exemptionId
				+ "/view");
		objCIOTransferPage.waitForElementToBeVisible(objExemptionsPage.exemptionName, 10);
		objApasGenericPage.scrollToBottomOfPage();
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objExemptionsPage.qualification, "General Information"),"Not Qualified",
				"SMAB-T3479: Validate that 'Qualification?' field is updated to on HOE Exemption record");
		
		objApasGenericPage.logout();
		
	}
	
	
	/**
	 * Verify that HOE Qualification and Exemption field values on Parcel are retained if a Transfer code with 'Retain Exemption' is selected as ' for Recorded Event
	 * @param loginUser
	 * @throws Exception
	 */
	
	/** Following TEST has been disabled for automation execution as this functionality 
	 *  is no more applicable post implementation on SMAB-10049.
	 *	Defect that was raised for automation failure :  SMAB-14601
	 */
	
	@Test(description = "SMAB-T3478 : Verify that HOE Qualification and Exemption field values on Parcel are retained if a Transfer code with 'Retain Exemption' is selected as ' for Recorded Event", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "HomeOwnerExemption" },enabled=false)
	public void HomeOwnerExemption_ExemptionRetain_RecordedEvent(String loginUser) throws Exception {
		
		//Data Setup
		String execEnv = System.getProperty("region");
		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "DataToCreateGranteeToVerifyHOE");

		// Step 1: Executing the recorder feed batch job to generate CIO WI
		objCIOTransferPage.generateRecorderJobWorkItems("DE", 1);
		String cioWorkItem = objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);

		// Step2: Login to the APAS application 
		objCIOTransferPage.login(loginUser);
		objCIOTransferPage.closeDefaultOpenTabs();

		// Step3: Opening the work items created by recorder batch
		objCIOTransferPage.searchModule(modules.HOME);
		objWorkItemHomePage.globalSearchRecords(cioWorkItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		ReportLogger.INFO("Get the Recorder Transfer Id and change the status of WI to 'In Progress'");
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + cioWorkItem + "'";
		HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);
		String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
		
		objCIOTransferPage.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);
		String apn = objCIOTransferPage.getFieldValueFromAPAS(objWorkItemHomePage.wiAPNDetailsPage, "Information");
		String apnQuery = "SELECT Id FROM Parcel__c where Name ='" + apn + "'";
		String apnId = salesforceAPI.select(apnQuery).get("Id").get(0);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		
		objApasGenericPage.logout();
		Thread.sleep(5000);

		//Step4: Login to the APAS application
		objApasGenericPage.login(users.SYSTEM_ADMIN);
		
		//Step5: Open the Parcel module and add values for Exemption fields
		Map<String, String> dataToCreateHomeOwnerExemptionMap = objUtil.generateMapFromJsonFile(homeOwnerExemptionData, "NewHOECreation");
		objApasGenericPage.searchModule(modules.PARCELS);
		
		driver.navigate().to("https://smcacre--"+execEnv+
				 ".lightning.force.com/lightning/r/Parcel__c/"+apnId+"/view");
		objCIOTransferPage.waitForElementToBeVisible(6, objParcelsPage.componentActionsButtonText);
		objParcelsPage.Click(objParcelsPage.editPencilIconForExemptionOnDetailPage);
		objCIOTransferPage.waitForElementToBeVisible(6, objParcelsPage.saveParcelButton);
		objApasGenericPage.scrollToElement(objApasGenericPage.getWebElementWithLabel(objParcelsPage.exemptionLabel));
		objExemptionsPage.enter(objParcelsPage.exemptionLabel, dataToCreateHomeOwnerExemptionMap.get("Exemption"));
		objExemptionsPage.selectMultipleValues(dataToCreateHomeOwnerExemptionMap.get("ExemptionType"), objParcelsPage.exemptionTypeLabel);
		objExemptionsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.saveParcelButton));
		
		/*Step6: Open Exemption and create HOE*/
		ReportLogger.INFO("Create HOE");
		objParcelsPage.openParcelRelatedTab(objParcelsPage.exemptionRelatedTab);
		objExemptionsPage.createHomeOwnerExemption(dataToCreateHomeOwnerExemptionMap);
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		ReportLogger.INFO("Exemption Name : " + exemptionName);
		objApasGenericPage.logout();
		Thread.sleep(5000);
		
		// Step7: CIO staff user navigating to transfer screen by clicking on related action link
		objCIOTransferPage.login(loginUser);
		objCIOTransferPage.closeDefaultOpenTabs();

		// Step8: Opening the work items and navigate to Transfer activity
		ReportLogger.INFO("Opening the WI and navigate to Transfer activity");
		objCIOTransferPage.searchModule(modules.HOME);
		objWorkItemHomePage.globalSearchRecords(cioWorkItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
		
		// Step9: Update the Transfer Code with Retain Exemption as 'Yes'
		ReportLogger.INFO("Update the Transfer Code with Retain Exemption as 'Yes'");
		driver.navigate().to("https://smcacre--" + execEnv
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.quickActionButtonDropdownIcon,10);
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "E-DP");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"E-DP",
				"SMAB-T3478: Validate the Transfer Code on CIO Transfer screen");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferDescriptionLabel, ""),"Exempt Domestic Partner Transfer",
				"SMAB-T3478: Validate the Transfer Description on CIO Transfer screen");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.exemptionRetainLabel, ""),"Yes",
				"SMAB-T3478: Validate the Exemption Retain field on CIO Transfer screen");
		
		//Step10: Submit for Approval and verify the status
		ReportLogger.INFO("Submit the Transfer Code for Approval");
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.finishButton);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferStatusLabel);
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferStatusLabel, "System Information"),"Submitted for Approval",
				"SMAB-T3478: Validate the Transfer Activity status on CIO Transfer screen");
		
		objApasGenericPage.logout();
		Thread.sleep(5000);
		
		//Step11: Login from Exemption Staff and verify the Exemption field values (it should be retained)
		objApasGenericPage.login(users.EXEMPTION_SUPPORT_STAFF);
		driver.navigate().to("https://smcacre--"+execEnv+
				 ".lightning.force.com/lightning/r/Parcel__c/"+apnId+"/view");
		objCIOTransferPage.waitForElementToBeVisible(6, objParcelsPage.componentActionsButtonText);
		
		softAssert.assertTrue(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionTypeLabel, "Summary Values").equals("Home Owners"),
				"SMAB-T3478: Validate that 'Home Owner' value is removed from Exemption Type field on Parcel record");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionLabel, "Summary Values"),"$7,000",
				"SMAB-T3478: Validate that 'Exemption' amount is removed from Exemption field on Parcel record");
		
		ReportLogger.INFO("Validate the Qualification status on HOE");
		String queryForExemptionId = "SELECT Id FROM Exemption__c where Name = '" + exemptionName + "'";
		String exemptionId = salesforceAPI.select(queryForExemptionId).get("Id").get(0);
		
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Exemption__c/" + exemptionId
				+ "/view");
		objCIOTransferPage.waitForElementToBeVisible(objExemptionsPage.exemptionName, 10);
		objApasGenericPage.scrollToBottomOfPage();
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objExemptionsPage.qualification, "General Information"),"Qualified",
				"SMAB-T3478: Validate that 'Qualification?' field is updated to on HOE Exemption record");
		
		driver.switchTo().window(parentWindow);
		objCIOTransferPage.logout();	
	
	}
	
	/**
	 * Below test case will verify error message on saving Exemption when the Claimant SSN value already exist in San Mateo county with another ownership with an existing / qualified HOE record
	 **/
	@Test(description = "SMAB-T4293, SMAB-T4294: Verify user is able to view an error message on saving HO Exemptions when the SSN value entered in the HOE record already exist against another HOE record against another APN.",
			dataProvider = "loginRPAppraiser", dataProviderClass = DataProviders.class , groups = {"Regression", "ChangeInOwnershipManagement", "HomeOwnerExemption" })
	public void HOE_verifyExemptionwithSSNisAlreadyInUse(String loginUser) throws Exception {
		
		// ----- Test data -----
		String validClaimantSSN = "999-33-9999";
		String invalidClaimantSSN = "999-22-9999";
		String expectedErrorMessage = "SSN Exists with a qualified HOE in this APN";	
		
		// Getting two active parcels
		String queryForActiveAPN = "SELECT Name,Id FROM Parcel__c where Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') Limit 2";
		String apnId = salesforceAPI.select(queryForActiveAPN).get("Id").get(0);
		String apnId2 = salesforceAPI.select(queryForActiveAPN).get("Id").get(1);
		
		//Data for a HOE records
		Map<String, String> dataToCreateHomeOwnerExemptionMap = objUtil.generateMapFromJsonFile(homeOwnerExemptionData, "NewHOECreation");
				
		// ----- Creating the first HOE -----
		
		// Login to the APAS application as SysAdmin
		objExemptionsPage.login(users.SYSTEM_ADMIN);
		
		// Navigating to the first parcel
		String executionEnv = System.getProperty("region");
		driver.navigate().to("https://smcacre--"+executionEnv+
				 ".lightning.force.com/lightning/r/Parcel__c/"+apnId+"/view");
		
		objParcelsPage.waitForElementToBeClickable(5, objParcelsPage.editParcelButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.editParcelButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.saveButton);
		objApasGenericPage.scrollToElement(objApasGenericPage.getWebElementWithLabel(objParcelsPage.exemptionLabel));
		objExemptionsPage.enter(objParcelsPage.exemptionLabel, dataToCreateHomeOwnerExemptionMap.get("Exemption"));
		objExemptionsPage.selectMultipleValues(dataToCreateHomeOwnerExemptionMap.get("ExemptionType"), objParcelsPage.exemptionTypeLabel);
		objExemptionsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.saveParcelButton));
		
		// Open Exemption and create HOE
		objParcelsPage.openParcelRelatedTab(objParcelsPage.exemptionRelatedTab);
		objExemptionsPage.createHomeOwnerExemption(dataToCreateHomeOwnerExemptionMap);
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
				
		// Getting the HOE id
		String exemptionQuery = "SELECT Name,Id FROM Exemption__c WHERE Name='"+exemptionName+"' limit 1";
		String exemptionId = salesforceAPI.select(exemptionQuery).get("Id").get(0);
		
		// Entering the SSN 
		objExemptionsPage.editExemptionRecord();		
		objExemptionsPage.enter(objExemptionsPage.claimantSSNOnDetailEditPage, invalidClaimantSSN);
		objExemptionsPage.saveRecord();		
		
		// ----- Creating the second HOE -----
		
		// Navigating to the second parcel
		driver.navigate().to("https://smcacre--"+executionEnv+
				 ".lightning.force.com/lightning/r/Parcel__c/"+apnId2+"/view");
		
		objParcelsPage.waitForElementToBeClickable(5, objParcelsPage.editParcelButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.editParcelButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.saveButton);
		objApasGenericPage.scrollToElement(objApasGenericPage.getWebElementWithLabel(objParcelsPage.exemptionLabel));
		objExemptionsPage.enter(objParcelsPage.exemptionLabel, dataToCreateHomeOwnerExemptionMap.get("Exemption"));
		objExemptionsPage.selectMultipleValues(dataToCreateHomeOwnerExemptionMap.get("ExemptionType"), objParcelsPage.exemptionTypeLabel);
		objExemptionsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.saveParcelButton));
		
		// Open Exemption and create HOE
		objParcelsPage.openParcelRelatedTab(objParcelsPage.exemptionRelatedTab);
		objExemptionsPage.createHomeOwnerExemption(dataToCreateHomeOwnerExemptionMap);
		String exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
				
		// Getting the HOE id
		String exemptionQuery2 = "SELECT Name,Id FROM Exemption__c WHERE Name='"+exemptionName2+"' limit 1";
		String exemptionId2 = salesforceAPI.select(exemptionQuery2).get("Id").get(0);
				
		// Logging out
		objExemptionsPage.logout();
		Thread.sleep(5000);
		
		// ----- Steps -----
				
		// Step1: Login to the APAS application using the credentials passed through
		objExemptionsPage.login(loginUser);
						
		// Step2: User opens a HOExemption record
		driver.navigate().to(("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Exemption__c/" + exemptionId2 + "/view"));
						
		
		// Step3: User enters SSN 
		objExemptionsPage.waitForElementToBeVisible(5,objExemptionsPage.editButton);
		objExemptionsPage.editExemptionRecord();		
		objExemptionsPage.enter(objExemptionsPage.claimantSSNOnDetailEditPage, invalidClaimantSSN);
						
		// Step4: User clicks on save button
		String errorMessage = objExemptionsPage.saveRecordAndGetError();
						
		// Verify error message
		ReportLogger.INFO("User cannot enter a SSN that already exists in another HOE");
		softAssert.assertContains(expectedErrorMessage, errorMessage, "SMAB-T4293: Verify user is able to view an error message on saving HO Exemption when the SSN value entered already exist against another HOE record against another APN.");
		
		// Step5: User enters valid SSN 
		objExemptionsPage.enter(objExemptionsPage.claimantSSNOnDetailEditPage, validClaimantSSN);
				
		// Step6: User clicks on save button
		objExemptionsPage.saveRecord();
				
		// Verify SSN was saved
		String finalSSNvalue = objExemptionsPage.claimantSSNOnDetailPage.getText();
		ReportLogger.INFO("User is able to save the SSN");
		softAssert.assertEquals(finalSSNvalue, validClaimantSSN, "SMAB-T4294: Verify the SSN data entry is allowed and saved when SNN doesn't exist in APAS previously.");
				
		// Logging out
		objExemptionsPage.logout();
		Thread.sleep(5000);		
		
		// ------------------------------ Deleting HOE records ------------------------------
		
		// Login to the APAS application as SysAdmin
		objExemptionsPage.login(users.SYSTEM_ADMIN);
		
		// ---- First HOE ----
		driver.navigate().to(("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Exemption__c/" + exemptionId + "/view"));
		objExemptionsPage.Click(objExemptionsPage.deleteExemption);
		objExemptionsPage.Click(objExemptionsPage.deleteConfirmationPostDeleteAction);
		
		objExemptionsPage.waitForElementToBeClickable(objExemptionsPage.successAlert,25);
		String messageOnAlert = objApasGenericPage.getElementText(objApasGenericPage.successAlert);
		objExemptionsPage.waitForElementToDisappear(objApasGenericPage.successAlert,10);
		softAssert.assertContains("was deleted",messageOnAlert,"First HOE was deleted correctly");
		
		// ---- Second HOE ----
		driver.navigate().to(("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Exemption__c/" + exemptionId2 + "/view"));
		objExemptionsPage.Click(objExemptionsPage.deleteExemption);
		objExemptionsPage.Click(objExemptionsPage.deleteConfirmationPostDeleteAction);
		
		objExemptionsPage.waitForElementToBeClickable(objExemptionsPage.successAlert,25);
		messageOnAlert = objApasGenericPage.getElementText(objApasGenericPage.successAlert);
		objExemptionsPage.waitForElementToDisappear(objApasGenericPage.successAlert,10);
		softAssert.assertContains("was deleted",messageOnAlert,"Second HOE was deleted correctly");
		
		// Logging out
		objExemptionsPage.logout();
	}
	
	/* Below test case is used to validate fields on Home owner Exemption and VA's
	 * 
	 */
		@Test(description = "SMAB-T4258,SMAB-T4291: Verify Fields on Home owner Exemption and related Value adjustment tab",  dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {"Regression","HomeOwnerExemption","Exemption"})
		public void HOE__VerifyFieldsOnExemptionAndVAs(String loginUser) throws Exception {
			Map<String, String> exemptionndata = objUtil.generateMapFromJsonFile(homeOwnerExemptionData, "NewHOECreation");		
			
			//Step1: Login to the APAS application using the credentials passed through data provider
			objExemptionsPage.login(users.SYSTEM_ADMIN);
			// Getting active parcels
			String queryForActiveAPN = "SELECT Name,Id FROM Parcel__c where Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') Limit 1";
			String apnId = salesforceAPI.select(queryForActiveAPN).get("Id").get(0);
			
			// Navigating to the first parcel
			String executionEnv = System.getProperty("region");
			driver.navigate().to("https://smcacre--"+executionEnv+
					 ".lightning.force.com/lightning/r/Parcel__c/"+apnId+"/view");			
			
			// Open Exemption and create HOE
			objParcelsPage.openParcelRelatedTab(objParcelsPage.exemptionRelatedTab);
			//Step2: creating new Home Owner exemption record
			ReportLogger.INFO(" creating new Home Owner Exemption record");
			
			objExemptionsPage.createHomeOwnerExemption(exemptionndata);
			String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
					
			//Step3: Verify fields on HOE record
		    softAssert.assertTrue(objExemptionsPage.verifyElementVisible(objExemptionsPage.exemptionCode), "SMAB-T4258: Verify that exemption code is present in Home Owner Exemption");
		    softAssert.assertTrue(objExemptionsPage.verifyElementVisible(objExemptionsPage.Penalty), "SMAB-T4258: Verify that penalty field is present in Home Owner Exemption");
		    softAssert.assertTrue(objExemptionsPage.verifyElementVisible(objExemptionsPage.filingStatus), "SMAB-T4258: Verify that Filing Status is present in Home Owner Exemption");
	        
		    //Step4: Creating new Value Adjustments
			ReportLogger.INFO("Step 4: Creating new Value Adjustments");
			objPage.javascriptClick(ObjValueAdjustmentPage.valueAdjustmentTab);
			objExemptionsPage.createNewVAsOnHOE();
			
			//Step5: Validating fields and data on Created VAs
		    softAssert.assertTrue(objExemptionsPage.verifyElementVisible(objExemptionsPage.propertySqFtProrated), "SMAB-T4291: Verify that Property Sq Ft Prorated % is present in Home Owner Exemption");
			softAssert.assertEquals(objExemptionsPage.getFieldValueFromAPAS(objExemptionsPage.Remark), "User adjusted exemption amount is 2000.", "");	
			softAssert.assertEquals(objExemptionsPage.getFieldValueFromAPAS(objExemptionsPage.penaltyPercentage), "20.00%", "SMAB-T4291: Verify that The penalties % is manually entered in the Exemptions Details Page which then flows to the Value Adjustment Page .");	
			softAssert.assertEquals(objExemptionsPage.getFieldValueFromAPAS(objExemptionsPage.ExemptionAmountUserAdjusted), "$2,000.00", "SMAB-T4291: Verify user adjueted amount is populated in user adjueted exemption amount field.");	
			softAssert.assertEquals(objExemptionsPage.getFieldValueFromAPAS(objExemptionsPage.netExemptionAmount), "$2,000.00", "SMAB-T4291: Verify user adjueted amount is populated in net exemption amount field.");	

			// Step6: Logging out of the application		
			objExemptionsPage.logout();

		}
		
		/* Below test case is used to validate fields on Home owner Exemption and VA's
		 * 
		 */
			@Test(description = "SMAB-T4168: Verify Net exemption amount of Active HOE record should be flow in respective roll entry records and master screen",  dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {"Regression","HomeOwnerExemption","Exemption"})
			public void HOE__VerifyHOEAmountOnRollEntriesAndMasterscreen(String loginUser) throws Exception {
				
				String excEnv = System.getProperty("region");
				String enrollmentType="Normal Enrollment";
				String transferCode="CIO-SALE";

				String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
				Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
						OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

				String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
				Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
						OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithCompleteOwnership");

				Map<String, String> hashMapCreateOwnershipRecordData = objUtil
						.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

				String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
				Map<String, String> hashMapCreateAssessedValueRecord = objUtil
						.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecord");

				JSONObject jsonForAppraiserActivity = objCIOTransferPage.getJsonObject();

				objCIOTransferPage.login(objCIOTransferPage.SYSTEM_ADMIN);
				objCIOTransferPage.searchModule(objCIOTransferPage.EFILE_INTAKE_VIEW);
				String recordedDocumentID = salesforceAPI.select(
						"SELECT id from recorded_document__c where recorder_doc_type__c='DE' and xAPN_count__c in (0,1,2,3,4)")
						.get("Id").get(0);

				objCIOTransferPage.deleteRecordedApnFromRecordedDocument(recordedDocumentID);
				Thread.sleep(3000);
				objCIOTransferPage.addRecordedApn(recordedDocumentID, 1);

				objCIOTransferPage.generateRecorderJobWorkItems(recordedDocumentID);

				// STEP 2-Query to fetch WI

				String workItemQuery = "SELECT Id,name FROM Work_Item__c where Type__c='CIO'   And status__c='In pool' order by createdDate desc limit 1";
				HashMap<String, ArrayList<String>> WIHashMap = salesforceAPI.select(workItemQuery);
				String workItemNo =WIHashMap.get("Name").get(0);
				String workItemID =WIHashMap.get("Id").get(0);
				driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Work_Item__c/"+ workItemID +"/view");

				objCIOTransferPage.waitForElementToBeInVisible(salesforceAPI, 5);
				String apnFromWIPage = objCIOTransferPage.getGridDataInHashMap(1).get("APN").get(0);
				String parcelId=salesforceAPI.select("Select Id from parcel__c where name ='" + apnFromWIPage + "'").get("Id").get(0);
				salesforceAPI.update("Parcel__C",parcelId,"Primary_Situs__c", "");
				salesforceAPI.update("Parcel__C", parcelId,"TRA__c",
						salesforceAPI.select("Select Id from TRA__c where city__c='SAN MATEO'").get("Id").get(0));
				salesforceAPI.update("Parcel__C", parcelId,"Primary_Situs__c",salesforceAPI.select("Select Id from Situs__c where Situs_City__c='SAN MATEO'").get("Id").get(0));
				salesforceAPI.update("Parcel__C",parcelId,"PUC_Code__c",
						salesforceAPI.select("Select Id from PUC_Code__c where name = '52-SFR, 5 TO 40 ACRES'").get("Id").get(0));
				
				// Deleting existing ownership from parcel
				objCIOTransferPage.deleteOwnershipFromParcel(salesforceAPI
						.select("Select Id from parcel__c where name='" + apnFromWIPage + "'").get("Id").get(0));

				// STEP 3- adding owner after deleting for the recorded APN
				String acesseName = objMappingPage.getOwnerForMappingAction();
				driver.navigate()
						.to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Parcel__c/"
								+ salesforceAPI.select("Select Id from parcel__C where name='" + apnFromWIPage + "'").get("Id").get(0)
								+ "/related/Property_Ownerships__r/view");
				objParcelsPage.createOwnershipRecord(acesseName, hashMapCreateOwnershipRecordData);
				String ownershipId = driver.getCurrentUrl().split("/")[6];
				objParcelsPage.deleteOldAndCreateNewAssessedValuesRecords(hashMapCreateAssessedValueRecord,
						apnFromWIPage);

				// STEP 4- updating the ownership date for current owners
				String dateOfEvent = salesforceAPI.select("Select Ownership_Start_Date__c from Property_Ownership__c where id = '"+ ownershipId + "'").get("Ownership_Start_Date__c").get(0);
				jsonForAppraiserActivity.put("DOR__c", dateOfEvent);
				jsonForAppraiserActivity.put("DOV_Date__c", dateOfEvent);
				salesforceAPI.update("Property_Ownership__c", ownershipId, jsonForAppraiserActivity);

				objCIOTransferPage.logout();
				Thread.sleep(3000);
				objCIOTransferPage.login(users.CIO_STAFF);
				objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.appLauncher, 10);

				driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Work_Item__c/"+ workItemID +"/view");
				
				String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='"+ workItemNo + "'";
				HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

				// STEP 6-Finding the recorded apn transfer id

				String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0).split("/")[3];
				objCIOTransferPage.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);
				objCIOTransferPage.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
				objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
				objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
				objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
				
				// STEP 7-Clicking on related action link

				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
				String parentWindow = driver.getWindowHandle();
				objWorkItemHomePage.switchToNewWindow(parentWindow);
				objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon, 10);
				ReportLogger.INFO("Add the Transfer Code");
				objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
				objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
				objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, transferCode);
				objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));

				// STEP 8-Creating the new grantee

				objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);
				driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/"
						+ recordeAPNTransferID + "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
				HashMap<String, ArrayList<String>> granteeHashMap = objCIOTransferPage.getGridDataForRowString("1");
				String granteeForMailTo = granteeHashMap.get("Grantee/Retain Owner Name").get(0);
				
				// STEP 9- Performing calculate ownership to perform partial transfer

				driver.navigate().to("https://smcacre--" + excEnv+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+ recordeAPNTransferID + "/view");
				objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.calculateOwnershipButtonLabel);

				// STEP 10-Creating copy to mail to record

				objCIOTransferPage.createCopyToMailTo(granteeForMailTo, hashMapOwnershipAndTransferCreationData);
				objCIOTransferPage.waitForElementToBeClickable(7, objCIOTransferPage.copyToMailToButtonLabel);

				// STEP 11-Navigating back to RAT screen

				driver.navigate().to("https://smcacre--" + excEnv+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+ recordeAPNTransferID + "/view");

				// STEP 12 - Click on submit for approval button
				objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null, objCIOTransferPage.quickActionOptionSubmitForApproval);

				if (objCIOTransferPage.waitForElementToBeVisible(7,objCIOTransferPage.yesRadioButtonRetainMailToWindow))
				{
					objCIOTransferPage.Click(objCIOTransferPage.yesRadioButtonRetainMailToWindow);
					objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.nextButton));
				}

				ReportLogger.INFO("CIO!! Transfer submitted for approval");
				objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.finishButton);
				objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));

				objCIOTransferPage.logout();
				Thread.sleep(3000);
				
				objCIOTransferPage.login(users.CIO_SUPERVISOR);
				
				driver.navigate().to("https://smcacre--" + excEnv+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+ recordeAPNTransferID + "/view");
				
				// STEP 14 - Click on submit for approval button
				objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null, objCIOTransferPage.quickActionOptionApprove);

				objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.finishButton);
				objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));

				// Fetching appraiser WI genrated on approval of CIO WI
				
				String workItemIDForAppraiser = salesforceAPI.select("Select Id ,Name from Work_Item__c where type__c='Appraiser' and sub_type__c='Appraisal Activity' order by name desc").get("Id").get(0);
					objCIOTransferPage.logout();
					
					// LOGIN with RP appraiser staff user
					objAppraisalActivity.login(users.RP_APPRAISER);

					driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Work_Item__c/"+ workItemIDForAppraiser +"/view");

				
					objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
					objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
					objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
					objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

					// Navigating to appraisal activity screen

					objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);

					objWorkItemHomePage.waitForElementToBeInVisible(objCIOTransferPage.ApnLabel, 5);
					
					String query = "SELECT  id FROM Roll_Entry__c where APN__c='" + parcelId + "' and type__c!='Annual' ";
					HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);

					if (!response.isEmpty()) {
						response.get("Id").stream().forEach(Id -> {
							salesforceAPI.delete("Roll_Entry__c", Id);

						});
					}
					objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
				    parentWindow = driver.getWindowHandle();
					objWorkItemHomePage.switchToNewWindow(parentWindow);

					objAppraisalActivity.waitForElementToBeVisible(10, objAppraisalActivity.appraisalActivityStatus);

					String appraisalScreenId = driver.getCurrentUrl().split("/")[6];
					String dovForAppraisal = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.dovLabel);

					Date dovDAte = new SimpleDateFormat("MM/dd/yyyy").parse(dovForAppraisal);

					Calendar calendar = new GregorianCalendar();
					calendar.setTime(dovDAte);
					Integer dovYear = calendar.get(Calendar.YEAR);

					String queryForRollYearId = "SELECT Id FROM Roll_Year_Settings__c Where Name = '" + dovYear.toString() + "'";
					HashMap<String, ArrayList<String>> rollYearId = salesforceAPI.select(queryForRollYearId);
					String dovYearCPIFactor = salesforceAPI.select(
							"SELECT CPI_Factor__c FROM CPI_Factor__c  Where Roll_Year__c ='" + rollYearId.get("Id").get(0) + "' ")
							.get("CPI_Factor__c").get(0);
					String expectedDOVYearCPIFactor = dovYearCPIFactor + "000";

					// Updating Land and Improvement Values and saving it for appraisal
					objAppraisalActivity
							.Click(objAppraisalActivity.appraisalActivityEditValueButton(objAppraisalActivity.landCashValueLabel));
					objAppraisalActivity.enter(objAppraisalActivity.landCashValueLabel, "300000");
					objAppraisalActivity.enter(objAppraisalActivity.improvementCashValueLabel, "500000");
					objAppraisalActivity.Click(objAppraisalActivity.getButtonWithText(objAppraisalActivity.SaveButton));
					Thread.sleep(3000);
					driver.switchTo().window(parentWindow);
					

					String supplementalQuery = "SELECT  count(id) FROM Roll_Entry__c where APN__c='" + parcelId
							+ "' and type__c='Supplemental' ";
					String countSupplementalRecords = salesforceAPI.select(supplementalQuery).get("expr0").get(0);

					softAssert.assertEquals(countSupplementalRecords, 1,
							"SMAB-T4125:verify that One supplemental roll entry record is created from AAS when land and improvement types are entered for AV type \"Assessed value\" for DOV = 31May");
					objCIOTransferPage.logout();
					
					// LOGIN with exemption support staff user
					objAppraisalActivity.login(users.EXEMPTION_SUPPORT_STAFF);
					
					String activeExemptionID =salesforceAPI.select("SELECT  Id, Name FROM Exemption__c where Parcel__c='" + parcelId + "' and Status__c='Active ORDER BY Name DESC'").get("Id").get(0);
					
					driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Exemption__c/"+ activeExemptionID +"/view");

					objCIOTransferPage.waitForElementToBeVisible(objExemptionsPage.exemptionName, 10);
					objApasGenericPage.scrollToElement(objExemptionsPage.dateOfApplicationOnDetailPage);
					objApasGenericPage.Click(objExemptionsPage.editPencilIconForDateOfApplicationOnDetailPage);
					objApasGenericPage.enterDate(objExemptionsPage.dateOfApplicationOnDetailPage, dateOfEvent);
					objApasGenericPage.enterDate(objExemptionsPage.dateOfOccupideProperty, dateOfEvent);
					objExemptionsPage.Click(objExemptionsPage.saveButtonOnDetailPage);					
										String workItemCreationData =testdata.MANUAL_WORK_ITEMS; 
					Map<String, String> hashMapmanualWorkItemData =objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateWorkItemOfHOEFormReceivedAndProcessing");
					  
					// Step 3: Creating Manual work item for the Parcel 
				    String HOEProcessingWorkitem =objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
				    workItemQuery = "SELECT Id,name FROM Work_Item__c where name='"+HOEProcessingWorkitem+"'";
				    String HOEProcessingWorkitemID = salesforceAPI.select(workItemQuery).get("Name").get(0);
					driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Work_Item__c/"+ HOEProcessingWorkitemID +"/view");

				    objCIOTransferPage.waitForElementToBeClickable(10, objWorkItemHomePage.submittedForApprovalOptionInTimeline);
					objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
					
					objCIOTransferPage.logout();
					
					// LOGIN with exemption support staff user
					objAppraisalActivity.login(users.RP_BUSINESS_ADMIN);
					
					driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Work_Item__c/"+ HOEProcessingWorkitemID +"/view");

					objCIOTransferPage.waitForElementToBeClickable(10, objWorkItemHomePage.completedOptionInTimeline);
				    objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
				    
				     query = "SELECT  id FROM Roll_Entry__c where APN__c='" + parcelId + "' and type__c!='Annual' ";
					String  RollEntryID = salesforceAPI.select(query).get("Id").get(0);
					
					driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Roll_Entry__c/"+ RollEntryID +"/view");
					
					String HomeOwnerExemptionAmount = objAppraisalActivity.getFieldValueFromAPAS(objExemptionsPage.ExemptionAmountOnRollEntry);
					softAssert.assertEquals(HomeOwnerExemptionAmount,"7000",
					        "SMAB-T4168: Validate that Net Exemption amount should be updated to on Exemption 1(HOE) on annual roll entry record");
											
					objCIOTransferPage.logout();

	}
		
}