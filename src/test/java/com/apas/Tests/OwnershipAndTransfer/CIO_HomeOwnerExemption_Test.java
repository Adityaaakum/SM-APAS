package com.apas.Tests.OwnershipAndTransfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
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
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	
	
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
	}
	
	/**
	 Below test case is used to validate,
	 -Exemption value, Exemption Type and Qualification fields are not retained if a Transfer Code with 'Retain Exemption' as 'No' is selected
	 **/
	
	@Test(description = "SMAB-T3479, SMAB-T3287: Validate Exemption value, Exemption Type and Qualification fields are not retained if a Transfer Code with 'Retain Exemption' as 'No' is selected", groups = {"Regression","ChangeInOwnershipManagement", "HomeOwnerExemption"}, dataProvider = "loginCIOStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void HomeOwnerExemption_NoExemptionRetain_UnrecordedTransfer(String loginUser) throws Exception {
		
		String queryForActiveAPN = "SELECT Name,Id FROM Parcel__c where Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') Limit 1";
		String apn = salesforceAPI.select(queryForActiveAPN).get("Name").get(0);
		String apnId = salesforceAPI.select(queryForActiveAPN).get("Id").get(0);
		
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT id, Name FROM PUC_Code__c where Name Not in ('99-RETIRED PARCEL') and Disabled_Veteran_Exemption__c = true limit 1");
		salesforceAPI.update("Parcel__c", apnId, "PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		
		Map<String, String> dataToCreateHomeOwnerExemptionMap = objUtil.generateMapFromJsonFile(homeOwnerExemptionData, "NewHOECreation");
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		
		//Step1: Login to the APAS application
		objApasGenericPage.login(users.SYSTEM_ADMIN);
				
		//Step2: Open the Parcel module and add values for Exemption fields
		objApasGenericPage.searchModule(modules.PARCELS);
		objApasGenericPage.globalSearchRecords(apn);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.editParcelButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.saveButton);
		objApasGenericPage.scrollToElement(objApasGenericPage.getWebElementWithLabel(objParcelsPage.exemptionLabel));
		objExemptionsPage.enter(objParcelsPage.exemptionLabel, dataToCreateHomeOwnerExemptionMap.get("Exemption"));
		objExemptionsPage.selectMultipleValues(dataToCreateHomeOwnerExemptionMap.get("ExemptionType"), objParcelsPage.exemptionTypeLabel);
		objExemptionsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.saveParcelButton));
		
		/*Step3: Open Exemption and create HOE*/
		objParcelsPage.openParcelRelatedTab(objParcelsPage.exemptionRelatedTab);
		objExemptionsPage.createHomeOwnerExemption(dataToCreateHomeOwnerExemptionMap);
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertTrue(exemptionName.contains("EXMPTN"),"SMAB-T3479: Validate user is able to create Exemption with mandtory fields'");
		
		objApasGenericPage.logout();
		Thread.sleep(5000);
		
		//Step4: Login from CIO Staff and update the Transfer code with Retain Exemption as No on CIO Transfer activity
		objApasGenericPage.login(users.CIO_STAFF);
		objApasGenericPage.searchModule(modules.PARCELS);
		objApasGenericPage.globalSearchRecords(apn);
		String transferActivityId = objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementExists(objCIOTransferPage.warningMessageArea),
				"SMAB-T3287: Validate that no warning message is displayed on CIO Transfer screen for Active Parcel");
		
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
		objApasGenericPage.searchModule(modules.PARCELS);
		objApasGenericPage.globalSearchRecords(apn);
		
		softAssert.assertTrue(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionTypeLabel, "Summary Values").equals(""),
				"SMAB-T3479: Validate that 'Home Owner' value is removed from Exemption Type field on Parcel record");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionLabel, "Summary Values"),"$0",
				"SMAB-T3479: Validate that 'Exemption' amount is removed from Exemption field on Parcel record");
		
		objApasGenericPage.searchModule(modules.EXEMPTION);
		objApasGenericPage.displayRecords("All");
		objApasGenericPage.globalSearchRecords(exemptionName);
		
		objCIOTransferPage.waitForElementToBeVisible(6, objExemptionsPage.qualification);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objExemptionsPage.qualification, "General Information"),"Not Qualified",
				"SMAB-T3479: Validate that 'Qualification?' field is updated to on HOE Exemption record");
		
		objApasGenericPage.logout();
		
	}
	
	
	/**
	 * Verify that HOE Qualification and Exemption field values on Parcel are retained if a Transfer code with 'Retain Exemption' is selected as ' for Recorded Event
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3478 : Verify that HOE Qualification and Exemption field values on Parcel are retained if a Transfer code with 'Retain Exemption' is selected as ' for Recorded Event", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "HomeOwnerExemption" },enabled=true)
	public void HomeOwnerExemption_ExemptionRetain_RecordedEvent(String loginUser) throws Exception {
		
		// Step 1: Executing the recorder feed batch job to generate CIO WI
		objCIOTransferPage.generateRecorderJobWorkItems("DE", 1);
		String cioWorkItem = objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);

		// Step2: Login to the APAS application 
		objCIOTransferPage.login(loginUser);
		Thread.sleep(2000);
		objCIOTransferPage.closeDefaultOpenTabs();

		// Step3: Opening the work items and accepting the WI created by recorder batch
		objCIOTransferPage.searchModule(modules.HOME);
		objWorkItemHomePage.globalSearchRecords(cioWorkItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objApasGenericPage.scrollToElement(objApasGenericPage.getWebElementWithLabel(objParcelsPage.editApnField));
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
		
		String executionEnv = System.getProperty("region");
		driver.navigate().to("https://smcacre--"+executionEnv+
				 ".lightning.force.com/lightning/r/Parcel__c/"+apnId+"/view");
		
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.editParcelButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.saveButton);
		objApasGenericPage.scrollToElement(objApasGenericPage.getWebElementWithLabel(objParcelsPage.exemptionLabel));
		objExemptionsPage.enter(objParcelsPage.exemptionLabel, dataToCreateHomeOwnerExemptionMap.get("Exemption"));
		objExemptionsPage.selectMultipleValues(dataToCreateHomeOwnerExemptionMap.get("ExemptionType"), objParcelsPage.exemptionTypeLabel);
		objExemptionsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.saveParcelButton));
		
		/*Step6: Open Exemption and create HOE*/
		objParcelsPage.openParcelRelatedTab(objParcelsPage.exemptionRelatedTab);
		objExemptionsPage.createHomeOwnerExemption(dataToCreateHomeOwnerExemptionMap);
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		
		objApasGenericPage.logout();
		Thread.sleep(5000);
		
		// Step7: CIO staff user navigating to transfer screen by clicking on related action link
		objCIOTransferPage.login(loginUser);
		Thread.sleep(5000);
		objCIOTransferPage.closeDefaultOpenTabs();

		// Step8: Opening the work items and accepting the WI created by recorder batch
		objCIOTransferPage.searchModule(modules.HOME);
		objWorkItemHomePage.globalSearchRecords(cioWorkItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		
		// Step9: Update the Transfer Code with Retain Exemption as 'Yes'
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
		objApasGenericPage.searchModule(modules.PARCELS);
		objApasGenericPage.globalSearchRecords(apn);
		
		softAssert.assertTrue(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionTypeLabel, "Summary Values").equals("Home Owners"),
				"SMAB-T3478: Validate that 'Home Owner' value is removed from Exemption Type field on Parcel record");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionLabel, "Summary Values"),"$7,000",
				"SMAB-T3478: Validate that 'Exemption' amount is removed from Exemption field on Parcel record");
		
		objApasGenericPage.searchModule(modules.EXEMPTION);
		objApasGenericPage.globalSearchRecords(exemptionName);
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objExemptionsPage.qualification, "General Information"),"Qualified",
				"SMAB-T3478: Validate that 'Qualification?' field is updated to on HOE Exemption record");
		
		driver.switchTo().window(parentWindow);
		objCIOTransferPage.logout();	
	
	}
	
	/**
	 * Below test case will verify error message on saving Exemption when the Claimant SSN value already exist in San Mateo county with another ownership with an existing / qualified HOE record
	 **/
	@Test(description = "SMAB-T4293, SMAB-T4294: Verify user is able toview an error message on saving HO Exemptions when the SSN value entered in the HOE record already exist against another HOE record against another APN.",
			dataProvider = "RPAppraiser", dataProviderClass = DataProviders.class , groups = {"regression","HomeOwnerExemption" })
	public void HOE_verifyExemptionwithSSNisAlreadyInUse(String loginUser) throws Exception {
		
		// Test data
		String invalidClaimantSSN = "123454321";
		String validClaimantSSN = "123454322";
		String expectedErrorMessage = "SSN Exists with a qualified HOE in this APN";
		
		// Getting an active HOE record
		String exemptionQuery = "SELECT Id FROM Exemption__c WHERE Qualification__c = 'Qualified' AND Status__c = 'Active' AND RecordTypeId = '01235000000EXHEAA4' AND Parcel__c IN (SELECT Id FROM Parcel__c WHERE Status__c = 'Active') limit 1";
		String exemptionID = salesforceAPI.select(exemptionQuery).get("Id").get(0);
		
		//Step1: Login to the APAS application using the credentials passed through
		objExemptionsPage.login(loginUser);
		
		//Step2: User opens a HOExemption record 
		String execEnv = System.getProperty("region");
		driver.navigate().to(("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Exemption__c/" + exemptionID + "/view"));
				
		// Copying previous SSN value to undo any changes later
		objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.claimantSSNOnDetailPage);
		String currentSSN = objExemptionsPage.claimantSSNOnDetailPage.getText();
		
		// Step3: User enters SSN 
		objExemptionsPage.editExemptionRecord();		
		objExemptionsPage.enter(objExemptionsPage.claimantSSNOnDetailEditPage, invalidClaimantSSN);
		
		// Step4: User clicks on save button
		objExemptionsPage.saveRecordAndGetError();
		
		// Verify error message
		objExemptionsPage.waitForElementToBeVisible(5,objExemptionsPage.errorMessage);
		ReportLogger.INFO("User cannot enter a SSN that already exists in another HOE");
		softAssert.assertContains(expectedErrorMessage, objExemptionsPage.errorMessage.getText(), "SMAB-T4293: Verify user is able to view an error message on saving HO Exemption when the SSN value entered already exist against another HOE record against another APN.");
		
		
		// ------------------------------ SMAB-T4294 ------------------------------
		
		// Step3: User enters valid SSN
		objExemptionsPage.enter(objExemptionsPage.claimantSSNOnDetailEditPage, validClaimantSSN);
		
		//Step4: User clicks on save button
		objExemptionsPage.saveRecord();
		
		// Verify SSN was saved
		ReportLogger.INFO("User is able to save the SSN");
		softAssert.assertEquals(objExemptionsPage.claimantSSNOnDetailPage.getText(), validClaimantSSN, "SMAB-T4294: Verify the SSN data entry is allowed and saved when SNN doesn't exist in APAS previously.");
		
		
		// ------------------------------ Undoing changes  ------------------------------ 
		
		objExemptionsPage.editExemptionRecord();
		objExemptionsPage.enter(objExemptionsPage.claimantSSNOnDetailEditPage, currentSSN);
		objExemptionsPage.saveRecord();
				
	}
}















