package com.apas.Tests.SecurityAndSharing;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
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
	@Test(description = "SMAB-T3390,SMAB-T3467 : Verify that When CIO users navigates to quick action dropdown button different CIO users are able to view different dropdown buttons", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "SecurityAndSharing" })
	public void QuickActionButtonsValidation_CIOTransferScreen_SubmitForApproval(String loginUser) throws Exception {

		// step 1: executing the recorder feed batch job to generate CIO WI
		objCIOTransferPage.generateRecorderJobWorkItems("AD", 1);
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

		// step 4: fetching the recorded apn transfer object associated with the CIO WI
		String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='" + cioWorkItem + "'";
		String recordeAPNTransferID = salesforceAPI.select(queryRecordedAPNTransfer).get("Navigation_Url__c").get(0)
				.split("/")[3];
		
		// deleting the current ownership records for the APN linked with WI
		String queryAPN = "SELECT Parcel__c FROM Recorded_APN_Transfer__c where id='" + recordeAPNTransferID + "'";
		objCIOTransferPage.deleteOwnershipFromParcel(salesforceAPI.select(queryAPN).get("Parcel__c").get(0));

		// update the grantee last name for recorded apn transfer object associated with
		// the CIO WI to ensure no blank last names
		String queryTransferId = "SELECT id FROM CIO_Transfer_Grantee_New_Ownership__c where Recorded_APN_Transfer__c='"
				+ recordeAPNTransferID + "'";
		HashMap<String, ArrayList<String>> responseTransferDetails = salesforceAPI.select(queryTransferId);

		for (int i = 0; i < responseTransferDetails.size(); i++) {
			jsonObject.put("Last_Name__c", "owner " + i);
			salesforceAPI.update("CIO_Transfer_Grantee_New_Ownership__c", responseTransferDetails.get("Id").get(i),
					jsonObject);
		}

		// Step5: CIO staff user navigating to transfer screen by clicking on related action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCIOTransferPage.waitForElementToBeVisible(10,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));

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
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForApproval);
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButtonLabel));
		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.xpathSpinner, 6);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible CIO staff after submit for approval");

		objCIOTransferPage.logout();

		// Step8: CIO supervisor now logs in and navigates to the above transfer screen
		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		driver.navigate().refresh();
		objCIOTransferPage.waitForElementToBeVisible(10,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible CIO supervisor after submit for approval");

		// Step9: approving the WI for approval
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionApprove);
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButtonLabel));
		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.xpathSpinner, 6);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible after WI is approved");

		driver.switchTo().window(parentWindow);
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
	@Test(description = "SMAB-T3390,SMAB-T3468 : Verify that When CIO users navigates to quick action dropdown button different CIO users are able to view different dropdown buttons", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement", "SecurityAndSharing" })
	public void QuickActionButtonsValidation_CIOTransferScreen_SubmitForReview(String loginUser) throws Exception {

		// step 1: executing the recorder feed batch job to generate CIO WI
		objCIOTransferPage.generateRecorderJobWorkItems("AD", 1);
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

		// update the grantee last name for recorded apn transfer object associated with
		// the CIO WI to ensure no blank last names
		String queryTransferId = "SELECT id FROM CIO_Transfer_Grantee_New_Ownership__c where Recorded_APN_Transfer__c='"
				+ recordeAPNTransferID + "'";
		HashMap<String, ArrayList<String>> responseTransferDetails = salesforceAPI.select(queryTransferId);

		for (int i = 0; i < responseTransferDetails.size(); i++) {
			jsonObject.put("Last_Name__c", "owner " + i);
			salesforceAPI.update("CIO_Transfer_Grantee_New_Ownership__c", responseTransferDetails.get("Id").get(i),
					jsonObject);
		}

		// Step4: CIO staff user navigating to transfer screen by clicking on related
		// action link
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objCIOTransferPage.waitForElementToBeVisible(10,
				objCIOTransferPage.getButtonWithText(objCIOTransferPage.calculateOwnershipButtonLabel));

		// Step5: submitting the WI for review
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForReview);
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButtonLabel));
		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.xpathSpinner, 6);

		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3468: Validation that componentActionsButtonLabel  button is not visible CIO staff after submit for review");
		objCIOTransferPage.logout();

		// Step6: CIO supervisor now logs in and navigates to the above transfer screen
		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + System.getProperty("region").toLowerCase()
				+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
		driver.navigate().refresh();
		objCIOTransferPage.waitForElementToBeVisible(10,
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
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButtonLabel));
		objCIOTransferPage.waitForElementToBeInVisible(objCIOTransferPage.xpathSpinner, 6);
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3468: Validation that componentActionsButtonLabel  button is  visible to supervisor after WI is reviewed");

		driver.switchTo().window(parentWindow);
		objCIOTransferPage.logout();
	}

}
