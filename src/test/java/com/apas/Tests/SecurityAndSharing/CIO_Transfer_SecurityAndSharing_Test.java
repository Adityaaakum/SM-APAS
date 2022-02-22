package com.apas.Tests.SecurityAndSharing;

import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class CIO_Transfer_SecurityAndSharing_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	CIOTransferPage objCIOTransferPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	JSONObject jsonObject = new JSONObject();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objCIOTransferPage = new CIOTransferPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objCIOTransferPage.updateAllOpenRollYearStatus();
	}

	/**
	 * Verify that When CIO users navigates to quick action dropdown button ,after
	 * arriving on CIO transfer screen, different CIO users are able to view
	 * different dropdown buttons
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3140,SMAB-T3193,SMAB-T3390,SMAB-T3467 : Verify that When CIO users navigates to quick action dropdown button different CIO users are able to view different dropdown buttons", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "SecurityAndSharing" },enabled=true)
	public void QuickActionButtonsValidation_CIOTransferScreen_SubmitForApproval(String loginUser) throws Exception {

		String OwnershipAndTransferGranteeCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferGranteeCreationData,
				"dataToCreateGranteeWithCompleteOwnership");

		// step 1: executing the recorder feed batch job to generate CIO WI
		objCIOTransferPage.generateRecorderJobWorkItems("DE", 1);
		Thread.sleep(7000);
		String cioWorkItem = objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);

		// Step2: Login to the APAS application using the credentials passed through
		// dataprovider (CIO staff user)
		objCIOTransferPage.login(loginUser);
		Thread.sleep(5000);
		objCIOTransferPage.closeDefaultOpenTabs();

		// Step3: Opening the work items and accepting the WI created by recorder batch
		objCIOTransferPage.searchModule(HOME);
		objWorkItemHomePage.globalSearchRecords(cioWorkItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
	  	
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// step 4: fetching the recorded apn transfer object associated with the CIO WI
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + cioWorkItem + "'";
		String recordeAPNTransferID = salesforceAPI.select(queryRecordedAPNTransfer).get("Navigation_Url__c").get(0)
				.split("/")[3];

		// deleting the current ownership records for the APN linked with WI
		String queryAPN = "SELECT Parcel__c FROM Recorded_APN_Transfer__c where id='" + recordeAPNTransferID + "'";
		objCIOTransferPage.deleteOwnershipFromParcel(salesforceAPI.select(queryAPN).get("Parcel__c").get(0));

		//deleting the CIO Transfer grantees for the current transfer screen
		objCIOTransferPage.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);

		// Step5: CIO staff user navigating to transfer screen by clicking on related action link
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.quickActionButtonDropdownIcon,20);


		objCIOTransferPage.scrollToBottomOfPage();
		
		// Step6: verifying the presence of quick action buttons visible to CIO staff
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.calculateOwnershipButtonLabel),
				"SMAB-T3390: Validation that calculateOwnershipButtonLabel button is visible CIO staff");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is visible CIO staff");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.copyToMailToButtonLabel),
				"SMAB-T3390: Validation that copyToMailToButtonLabel button is visible CIO staff");
		softAssert.assertTrue(
				objCIOTransferPage.verifyElementVisible(objCIOTransferPage.checkOriginalTransferListButtonLabel),
				"SMAB-T3390: Validation that checkOriginalTransferListButtonLabel button is visible CIO staff");

		//adding below assertions to verify various table names and other labels on transfer screen  
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.cioTransferActivityLabel),"CIO Transfer Activity",
				"SMAB-T3140,SMAB-T3330: Validation that CIO Transfer Activity label is visible on top left of transfer screen and user is landed to transfer scren after accepting WI from home page ");

		List<WebElement> cioTransferScreenSectionlabels=objCIOTransferPage.locateElements(objCIOTransferPage.cioTransferScreenSectionlabels, 10);

		softAssert.assertEquals(objCIOTransferPage.getElementText(cioTransferScreenSectionlabels.get(0)),"Ownership for Parent Parcel",
				"SMAB-T3140: Validation that Ownership for Parent Parcel section is visible on screen");

		softAssert.assertEquals(objCIOTransferPage.getElementText(cioTransferScreenSectionlabels.get(1)),"CIO Transfer Grantors",
				"SMAB-T3140: Validation that CIO Transfer Grantors section  is visible on screen ");

		softAssert.assertEquals(objCIOTransferPage.getElementText(cioTransferScreenSectionlabels.get(2)),"CIO Transfer Grantee & New Ownership",
				"SMAB-T3140: Validation that CIO Transfer Grantee & New Ownership section  is visible on screen ");

		softAssert.assertEquals(objCIOTransferPage.getElementText(cioTransferScreenSectionlabels.get(3)),"CIO Transfer Mail To",
				"SMAB-T3140: Validation that CIO Transfer Mail To section is visible on screen ");

		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionBack),
				"SMAB-T3390: Validation that back option in dropdown  is visible to CIO staff");
		softAssert.assertTrue(
				objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionSubmitForReview),
				"SMAB-T3390: Validation that SubmitForReview option in dropdown  is visible CIO staff");
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionApprove),
				"SMAB-T3390: Validation that Approve  option in dropdown  is not visible to CIO staff");
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionReturn),
				"SMAB-T3390: Validation that Return  option in dropdown  is not visible to CIO staff");
		softAssert.assertTrue(
				!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionReviewComplete),
				"SMAB-T3390: Validation that ReviewComplete  option in dropdown  is not visible to CIO staff");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionBackToWIs),
				"SMAB-T3390: Validation that back to WIs  option in dropdown  is  visible to CIO staff");
		softAssert.assertTrue(
				objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionSubmitForApproval),
				"SMAB-T3390: Validation that submitforApprovalButton button is visible CIO staff");

		// Step7: submitting the WI for approval
		ReportLogger.INFO("Updating the transfer code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		ReportLogger.INFO("transfer code updated successfully");

		ReportLogger.INFO("Creating new grantee record");
		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);	
		ReportLogger.INFO("Grantee record created successfully");

		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.quickActionButtonDropdownIcon,20);


		ReportLogger.INFO("Submitting the WI for approval");
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
		
		
		objCIOTransferPage.Click(objCIOTransferPage.yesRadioButtonRetainMailToWindow);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.nextButton));
		
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.confirmationMessageOnTranferScreen);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButtonLabel));
		ReportLogger.INFO("WI Submitted  for approval successfully");

		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.xpathSpinner, 6);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible CIO staff after submit for approval");

		//adding assertion for SMAB-T3193
		ReportLogger.INFO("CIO Staff :- Entering text in Remarks field on transfer screen after WI submitted for approval ");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.remarksLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.remarksLabel);
		objCIOTransferPage.enter(objCIOTransferPage.remarksLabel,"test data");

		softAssert.assertContains(objCIOTransferPage.saveRecordAndGetError(),
				"Oops...you don't have the necessary privileges to edit this record. See your administrator for help.",
				"SMAB-T3193: Verify that after submit for approval  transfer screen is now in read only mode for CIO staff user ");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.CancelButton));

		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);	
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText("Save"));
		objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.pageError,30);
		String actualErrormessage="";
		
		for(int i=0; i<=2;i++){
			  try{
				  actualErrormessage= objCIOTransferPage.pageError.getText();
			     break;
			  }
			  catch(Exception e){
			     
			  }
			}
		
		softAssert.assertContains(actualErrormessage,
				"insufficient access rights on cross-reference id",
				"SMAB-T3193: Verify that after submit for approval  CIO staff user can't create a new grantee record ");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.CancelButton));

		objCIOTransferPage.logout();
		Thread.sleep(5000);
		
		// Step8: CIO supervisor now logs in and navigates to the above transfer screen
		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		driver.navigate().refresh();
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.quickActionButtonDropdownIcon,20);

		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible CIO supervisor after submit for approval");

		//adding assertion for SMAB-T3193
		ReportLogger.INFO("CIO Supervisor :-Entering text in Remarks field on transfer screen after submit for approval");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.remarksLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.remarksLabel);
		objCIOTransferPage.enter(objCIOTransferPage.remarksLabel,"test data");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));

		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.remarksLabel, ""),"test data",
				"SMAB-T3193: Verify that after submit for approval  transfer screen  is now in read and write only mode for CIO supervisor");
		ReportLogger.INFO("CIO Supervisor:- Text entered in Remarks field successfully after submit for approval");

		// Step9: approving the WI for approval
		ReportLogger.INFO("CIO Supervisor:- Approving the transfer screen");
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionApprove);
		objCIOTransferPage.waitForElementToBeVisible(20,objCIOTransferPage.confirmationMessageOnTranferScreen);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButtonLabel));
		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.xpathSpinner, 6);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible after WI is approved");

		//adding assertion for SMAB-T3193
		ReportLogger.INFO("CIO Supervisor:- entering text in remarks field after approval");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.remarksLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.remarksLabel);
		objCIOTransferPage.enter(objCIOTransferPage.remarksLabel,"test data");

		softAssert.assertContains(objCIOTransferPage.saveRecordAndGetError(),
				"Oops...you don't have the necessary privileges to edit this record. See your administrator for help.",
				"SMAB-T3193: Verify that after approval , transfer screen  is now in read only mode ");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.CancelButton));

		objCIOTransferPage.logout();
	}

	/**
	 * Verify that When CIO users navigates to quick action dropdown button ,after
	 * arriving on CIO transfer screen, different CIO users are able to view
	 * different dropdown buttons
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3390,SMAB-T3468 : Verify that When CIO users navigates to quick action dropdown button different CIO users are able to view different dropdown buttons",dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "SecurityAndSharing" },enabled=false)
	public void QuickActionButtonsValidation_CIOTransferScreen_SubmitForReview(String loginUser) throws Exception {

		String OwnershipAndTransferGranteeCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferGranteeCreationData,
				"dataToCreateGranteeWithCompleteOwnership");

		// step 1: executing the recorder feed batch job to generate CIO WI
		objCIOTransferPage.generateRecorderJobWorkItems("DE", 1);
		Thread.sleep(7000);
		String cioWorkItem = objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);

		// Step2: Login to the APAS application using the credentials passed through
		// dataprovider (CIO staff user)
		objCIOTransferPage.login(loginUser);
		Thread.sleep(5000);
		objCIOTransferPage.closeDefaultOpenTabs();

		// Step3: Opening the work items and accepting the WI created by recorder batch
		objCIOTransferPage.searchModule(HOME);
		objWorkItemHomePage.globalSearchRecords(cioWorkItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);

		// fetching the recorded apn transfer object associated with the CIO WI
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + cioWorkItem + "'";
		String recordeAPNTransferID = salesforceAPI.select(queryRecordedAPNTransfer).get("Navigation_Url__c").get(0)
				.split("/")[3];

		// deleting the current ownership records for the APN linked with WI
		String queryAPN = "SELECT Parcel__c FROM Recorded_APN_Transfer__c where id='" + recordeAPNTransferID + "'";
		objCIOTransferPage.deleteOwnershipFromParcel(salesforceAPI.select(queryAPN).get("Parcel__c").get(0));

		//deleting the CIO Transfer grantees for the current transfer screen
		objCIOTransferPage.deleteRecordedAPNTransferGranteesRecords(recordeAPNTransferID);

		// Step4: CIO staff user navigating to transfer screen by clicking on related
		// action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCIOTransferPage.waitForElementToBeVisible(10,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));

		// Step5: submitting the WI for review
		ReportLogger.INFO("Updating the transfer code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		ReportLogger.INFO("transfer code updated successfully");

		ReportLogger.INFO("Creating new grantee record");
		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);	
		ReportLogger.INFO("Grantee record created successfully");

		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		objCIOTransferPage.waitForElementToBeVisible(15,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));

		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForReview);
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen,30);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButtonLabel));
		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.xpathSpinner, 6);

		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3468: Validation that componentActionsButtonLabel  button is not visible CIO staff after submit for review");
		objCIOTransferPage.logout();
		Thread.sleep(5000);

		// Step6: CIO supervisor now logs in and navigates to the above transfer screen
		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		driver.navigate().refresh();
		objCIOTransferPage.waitForElementToBeVisible(20,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));

		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3468: Validation that componentActionsButtonLabel  button is not visible CIO supervisor after submit for review");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.calculateOwnershipButtonLabel),
				"SMAB-T3390: Validation that calculateOwnershipButtonLabel button is visible CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.copyToMailToButtonLabel),
				"SMAB-T3390: Validation that copyToMailToButtonLabel button is visible CIO supervisor");
		softAssert.assertTrue(
				objCIOTransferPage.verifyElementVisible(objCIOTransferPage.checkOriginalTransferListButtonLabel),
				"SMAB-T3390: Validation that checkOriginalTransferListButtonLabel button is visible CIO supervisor");

		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionBack),
				"SMAB-T3390: Validation that back option in dropdown  is visible to CIO supervisor");
		softAssert.assertTrue(
				!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionSubmitForReview),
				"SMAB-T3390: Validation that SubmitForReview option in dropdown  is not visible CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionApprove),
				"SMAB-T3390: Validation that Approve  option in dropdown  is  visible to CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionReturn),
				"SMAB-T3390: Validation that Return  option in dropdown  is  visible to CIO supervisor");
		softAssert.assertTrue(
				objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionReviewComplete),
				"SMAB-T3390: Validation that ReviewComplete  option in dropdown  is  visible to CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionBackToWIs),
				"SMAB-T3390: Validation that back to WIs  option in dropdown  is  visible to CIO supervisor");
		softAssert.assertTrue(
				!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionSubmitForApproval),
				"SMAB-T3390: Validation that submitforApprovalButton button is not visible CIO supervisor");

		// Step7: marking the WI as review complete
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionReviewComplete);
		objCIOTransferPage.waitForElementToBeVisible(30,objCIOTransferPage.confirmationMessageOnTranferScreen);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButtonLabel));
		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.xpathSpinner, 6);
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3468: Validation that componentActionsButtonLabel  button is  visible to supervisor after WI is reviewed");

		driver.switchTo().window(parentWindow);
		objCIOTransferPage.logout();
	}
}
