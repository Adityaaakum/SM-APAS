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
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
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
	JSONObject  jsonObject= new JSONObject();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objCIOTransferPage = new CIOTransferPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);

	}

	/**
	 * Verify that When CIO users navigates to quick action dropdown button ,after arriving on CIO transfer screen, different CIO users are able to view different dropdown buttons
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3390,SMAB-T3467 : Verify that When CIO users navigates to quick action dropdown button different CIO users are able to view different dropdown buttons", dataProvider = "loginCIOStaffUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement" })
	public void QuickActionButtonsValidation_CIOTransferScreen_SubmitForApproval(String loginUser) throws Exception {

		//fetching recorder doc with only one grantor and grantee
		String queryDOCId = "select Recorded_Document__c FROM Transfer__c group by  Recorded_Document__c having count(id) =1 limit 1";			
		HashMap<String, ArrayList<String>> responseDOCDetails = salesforceAPI.select(queryDOCId);
		String recorerDOCId=responseDOCDetails.get("Recorded_Document__c").get(0);

		//fetching the recorded apn associated with the recorded doc
		String queryAPNId = "SELECT parcel__c FROM Recorded_APN__c where Recorded_Document__c ='"+ recorerDOCId +"'";			
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNId);
		String apnId=responseAPNDetails.get("Parcel__c").get(0);

		//deleting the current ownership records for the above parcel
		//objCIOTransferPage.deleteOwnershipFromParcel(apnId);

		//updating the status of above recorder document to pending
		jsonObject.put("Status__c","Pending");
		salesforceAPI.update("Recorded_Document__c",recorerDOCId,jsonObject);

		//executing the recorder feed batch job to generate CIO WI

		String cioWorkItem= objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);

		objCIOTransferPage.login(users.SYSTEM_ADMIN);
		Thread.sleep(7000);
		objCIOTransferPage.closeDefaultOpenTabs();


		// Step1: Login to the APAS application using the credentials passed through dataprovider (CIO staff user)
		objCIOTransferPage.login(users.SYSTEM_ADMIN);

		// Step2: Opening the work items home page  and accepting the  WI created by recorder batch job
		objCIOTransferPage.searchModule(HOME);
		objCIOTransferPage.Click(objWorkItemHomePage.inPoolTab);
		Thread.sleep(7000);

		WebElement actualWIName=objWorkItemHomePage.searchWIinGrid(cioWorkItem);
		objWorkItemHomePage.acceptWorkItem(cioWorkItem);
		objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		Thread.sleep(5000);
		actualWIName=objWorkItemHomePage.searchWIinGrid(cioWorkItem);

		// Step2: navigating to transfer screen by clicking on related action link
		objWorkItemHomePage.Click(actualWIName);
		objCIOTransferPage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);
		objCIOTransferPage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		 
		
		driver.get("https://smcacre--qa.lightning.force.com/lightning/r/Recorded_APN_Transfer__c/a1R35000000TjR7EAK/view");
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.getButtonWithText(objCIOTransferPage.backToWIsButtonLabel));

		// Step2: verifying the presence of quick action buttons visible to CIO staff user
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.submitforApprovalButtonLabel),
				"SMAB-T3390: Validation that submitforApprovalButton button is visible CIO staff");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.calculateOwnershipButtonLabel),
				"SMAB-T3390: Validation that calculateOwnershipButtonLabel button is visible CIO staff");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is visible CIO staff");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.copyToMailToButtonLabel),
				"SMAB-T3390: Validation that copyToMailToButtonLabel button is visible CIO staff");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.checkOriginalTransferListButtonLabel),
				"SMAB-T3390: Validation that checkOriginalTransferListButtonLabel button is visible CIO staff");

		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionBack),
				"SMAB-T3390: Validation that back option in dropdown  is visible to CIO staff");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionSubmitForReview),
				"SMAB-T3390: Validation that SubmitForReview option in dropdown  is visible CIO staff");
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionApprove),
				"SMAB-T3390: Validation that Approve  option in dropdown  is not visible to CIO staff");
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionReturn),
				"SMAB-T3390: Validation that Return  option in dropdown  is not visible to CIO staff");
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionReviewComplete),
				"SMAB-T3390: Validation that ReviewComplete  option in dropdown  is not visible to CIO staff");

		// Step2: submitting the WI for approval
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.submitforApprovalButtonLabel));
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible CIO staff after submit for approval");

		objCIOTransferPage.logout();

		// Step2: CIO supervisor now logs in and navigates to the above transfer screen
		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.getButtonWithText(objCIOTransferPage.backToWIsButtonLabel));

		
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible CIO supervisor after submit for approval");

		// Step2: approving  the WI for approval
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionApprove);
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible after WI is approved");


		//driver.switchTo().window(parentWindow);
		objCIOTransferPage.logout();
	}
	/**
	 * Verify that When CIO users navigates to quick action dropdown button ,after arriving on CIO transfer screen, different CIO users are able to view different dropdown buttons
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3390,SMAB-T3467 : Verify that When CIO users navigates to quick action dropdown button different CIO users are able to view different dropdown buttons", dataProvider = "loginCIOStaffUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement" })
	public void QuickActionButtonsValidation_CIOTransferScreen_SubmitForReview(String loginUser) throws Exception {

		//fetching recorder doc with only one grantor and grantee
		String queryDOCId = "select Recorded_Document__c FROM Transfer__c group by  Recorded_Document__c having count(id) =1 limit 1";			
		HashMap<String, ArrayList<String>> responseDOCDetails = salesforceAPI.select(queryDOCId);
		String recorerDOCId=responseDOCDetails.get("Recorded_Document__c").get(0);

		//fetching the recorded apn associated with the recorded doc
		String queryAPNId = "SELECT parcel__c FROM Recorded_APN__c where Recorded_Document__c ='"+ recorerDOCId +"'";			
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNId);
		String apnId=responseAPNDetails.get("Parcel__c").get(0);

		//deleting the current ownership records for the above parcel
		//objCIOTransferPage.deleteOwnershipFromParcel(apnId);

		//updating the status of above recorder document to pending
		jsonObject.put("Status__c","Pending");
		salesforceAPI.update("Recorded_Document__c",recorerDOCId,jsonObject);

		//executing the recorder feed batch job to generate CIO WI

		
		String cioWorkItem= objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);

		objCIOTransferPage.login(users.SYSTEM_ADMIN);
		Thread.sleep(7000);
		objCIOTransferPage.closeDefaultOpenTabs();

		// Step1: Login to the APAS application using the credentials passed through dataprovider (CIO staff user)
		objCIOTransferPage.login(users.SYSTEM_ADMIN);

		// Step2: Opening the work items home page  and accepting the  WI created by recorder batch job
		objCIOTransferPage.searchModule(HOME);
		objCIOTransferPage.Click(objWorkItemHomePage.inPoolTab);
		Thread.sleep(7000);

		WebElement actualWIName=objWorkItemHomePage.searchWIinGrid(cioWorkItem);
		objWorkItemHomePage.acceptWorkItem(cioWorkItem);
		objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		Thread.sleep(5000);
		actualWIName=objWorkItemHomePage.searchWIinGrid(cioWorkItem);

		// Step2: navigating to transfer screen by clicking on related action link
		objWorkItemHomePage.Click(actualWIName);
		objCIOTransferPage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);
		objCIOTransferPage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		 
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.getButtonWithText(objCIOTransferPage.backToWIsButtonLabel));

		// Step2: submitting the WI for review
		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionSubmitForReview);
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible CIO staff after submit for review");

		objCIOTransferPage.logout();

		// Step2: CIO supervisor now logs in and navigates to the above transfer screen
		objCIOTransferPage.login(users.CIO_SUPERVISOR);
		
		
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.getButtonWithText(objCIOTransferPage.backToWIsButtonLabel));

		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible CIO supervisor after submit for review");
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.submitforApprovalButtonLabel),
				"SMAB-T3390: Validation that submitforApprovalButton button is not visible CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.calculateOwnershipButtonLabel),
				"SMAB-T3390: Validation that calculateOwnershipButtonLabel button is visible CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.copyToMailToButtonLabel),
				"SMAB-T3390: Validation that copyToMailToButtonLabel button is visible CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.checkOriginalTransferListButtonLabel),
				"SMAB-T3390: Validation that checkOriginalTransferListButtonLabel button is visible CIO supervisor");

		objCIOTransferPage.Click(objCIOTransferPage.quickActionButtonDropdownIcon);

		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionBack),
				"SMAB-T3390: Validation that back option in dropdown  is visible to CIO supervisor");
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionSubmitForReview),
				"SMAB-T3390: Validation that SubmitForReview option in dropdown  is not visible CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionApprove),
				"SMAB-T3390: Validation that Approve  option in dropdown  is not visible to CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionReturn),
				"SMAB-T3390: Validation that Return  option in dropdown  is not visible to CIO supervisor");
		softAssert.assertTrue(objCIOTransferPage.verifyElementVisible(objCIOTransferPage.quickActionOptionReviewComplete),
				"SMAB-T3390: Validation that ReviewComplete  option in dropdown  is not visible to CIO supervisor");

		
		// Step2: marking the WI as review complete
		objCIOTransferPage.Click(objCIOTransferPage.quickActionOptionReviewComplete);
		objCIOTransferPage.waitForElementToBeVisible(objCIOTransferPage.confirmationMessageOnTranferScreen);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementVisible(objCIOTransferPage.componentActionsButtonLabel),
				"SMAB-T3467: Validation that componentActionsButtonLabel  button is not visible after WI is reviewed");


		//driver.switchTo().window(parentWindow);
		objCIOTransferPage.logout();
	}

}
