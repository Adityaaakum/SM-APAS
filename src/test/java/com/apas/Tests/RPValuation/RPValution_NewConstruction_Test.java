package com.apas.Tests.RPValuation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.DemolitionPage;
import com.apas.PageObjects.NewConstructionPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class RPValution_NewConstruction_Test extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	NewConstructionPage objNewConstructionPage;
	AuditTrailPage businessAudittrail;


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objNewConstructionPage = new NewConstructionPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		businessAudittrail = new AuditTrailPage(driver);


	}

	/**
	 * This method is to verify few validations based on full New construction , partial
	 * New construction event codes also verify if few buttons are present on the
	 * New construction Page
	 * 
	 * @param loginUser
	 */
	@Test(description = "SMAB-T4365,SMAB-T4367,SMAB-T4404,SMAB-T4368,SMAB-T4454:This method is to verify few validations based on full NewConstruction ,"
			+ " partial NewConstruction event codes", dataProvider = "loginRPAppraiser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "RPValuation", "NewConstruction"})
	public void Manual_NewConstruction_WorkItem(String loginUser) throws Exception {

		// Fetching Active parcel
		String executionEnv = System.getProperty("region");
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Status__c = 'Active' Limit 1";
		String apn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String apnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select(
				"SELECT Name,Id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c"
				+ " where Status__c='Active') and Legacy__c = 'No' limit 1");
		String pucOnParcel = responsePUCDetails.get("Id").get(0);

		salesforceAPI.update("Parcel__c", apnId,"PUC_Code_Lookup__c",pucOnParcel);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeRPNewConstruction");

		// Fetching some Event codes from database
		

		String puc = salesforceAPI
				.select("SELECT Name,id FROM PUC_Code__c where legacy__c = 'NO' limit 1").get("Name").get(0);
		String partialNewConstructionEventCode = salesforceAPI
				.select("SELECT Name FROM Event_Library__c where Name like 'NC-Partial' limit 1").get("Name").get(0);
		String noStartNewConstructionEventCode = salesforceAPI
				.select("SELECT Name FROM Event_Library__c where Name like 'NC-NS' limit 1").get("Name").get(0);

		// Step1: Login to the APAS application
		objNewConstructionPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to perform Combine
		// Action
		driver.navigate().to(
				"https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/" + apnId + "/view");
		objParcelsPage.waitForElementToBeVisible(30, objParcelsPage.componentActionsButtonText);

		// Step3 : Creating and navigating to the Demolition work item
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		Thread.sleep(3000);

		String newConstructionScreenURL = driver.getCurrentUrl();
		objNewConstructionPage.searchModule("APAS");
		driver.navigate().to(newConstructionScreenURL);
		Thread.sleep(3000);

		softAssert.assertTrue(
				!(objNewConstructionPage.isNotDisplayed(
						objNewConstructionPage.getButtonWithText(objNewConstructionPage.createDemolitionWIBtn))),
				"SMAB-T4404:Demolition WI Button is present");
		objNewConstructionPage.Click(objNewConstructionPage.getButtonWithText
				(objNewConstructionPage.createDemolitionWIBtn));
		objNewConstructionPage.waitForElementToBeVisible(20,
				objNewConstructionPage.messageForDemolition);
		softAssert.assertContains(objNewConstructionPage.messageForDemolition.getText(),
				"A Demolition WI will be created and assigned to you",
				" SMAB-T4404: Verify that Create Demolition WI btn should be visible on screen and on clicking it a msg should be shown");
		objNewConstructionPage.Click(objNewConstructionPage.crossButton);

		softAssert.assertEquals(objNewConstructionPage.getFieldValueFromAPAS(objNewConstructionPage.puc,
				"Building Permit Details"),pucOnParcel,
				"SMAB-T4365: Verify PUC is autopopulated from parcel");
		// Validating if the fields are in read Only mode
		boolean editButtonStatus = objParcelsPage.verifyEditButtonIsPresent("APN");
		softAssert.assertEquals(editButtonStatus, false, "SMAB-T4365:APN field is read Only");

		editButtonStatus = objParcelsPage.verifyEditButtonIsPresent("Appraiser Activity Status");
		softAssert.assertEquals(editButtonStatus, false, "SMAB-T4365:Appraiser Activity Status field is read Only");

		objNewConstructionPage.waitForElementToBeVisible(20,
				objNewConstructionPage.getButtonWithText(objNewConstructionPage.createDemolitionWIBtn));
		objWorkItemHomePage.scrollToBottom();

		// Checking if proper error message is displayed if user don't enter event code
		// and DOV
		WebElement editButton = objParcelsPage.editFieldButton("Event Code");
		objParcelsPage.Click(editButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		List<WebElement> errorMessage = driver.findElements(By.xpath(objNewConstructionPage.errorMessageWithLinks));

		softAssert.assertEquals(errorMessage.get(0).getText(), "DOV", "SMAB-T4365: DOV is present in error message");
		softAssert.assertEquals(errorMessage.get(1).getText(), "Event Code",
				"SMAB-T4365: Event Code is present in error message");
		objNewConstructionPage.Click(objNewConstructionPage.cancelButton);
		
		// Validating user is able to click on createNewConstructionWI Button
//		objWorkItemHomePage.scrollToElement(objNewConstructionPage.getWebElementWithLabel("DOV"));

		objParcelsPage.enter("DOV", "1/13/2022");
		objParcelsPage.searchAndSelectOptionFromDropDown("Event Code", noStartNewConstructionEventCode);
		
		objParcelsPage.enter(objNewConstructionPage.landCashValueLabel,"100");
		objParcelsPage.enter(objNewConstructionPage.improvementCashValueLabel,"200");
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		
		objNewConstructionPage.waitForElementToBeVisible(20,objNewConstructionPage.errorMsg);
		softAssert.assertContains(objNewConstructionPage.errorMsg.getText(),
				"A No Start Event Code requires that the Improvement Cash Value and Land Cash Value are blank",
				" SMAB-T4367: Verify that error msg should be thrown if land and improvement is populated");
		objNewConstructionPage.Click(objNewConstructionPage.cancelButton);


//		objParcelsPage.Click(objParcelsPage.getButtonWithText("Cancel"));
		objParcelsPage.searchAndSelectOptionFromDropDown("Event Code", partialNewConstructionEventCode);
		objParcelsPage.enter(objNewConstructionPage.puc,puc);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		
		// Staff is submitting request for approval to supervisor
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objNewConstructionPage.submitForApprovalNCWIBtn));
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objNewConstructionPage.nextButton));

		// Staff Logout
		objWorkItemHomePage.logout();
		Thread.sleep(5000);

		// Supervisor logins
		objNewConstructionPage.login(users.RP_PRINCIPAL);

		driver.navigate().to(newConstructionScreenURL);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objNewConstructionPage.approveNCButton));
		
		objNewConstructionPage.searchModule(PARCELS);
		objNewConstructionPage.globalSearchRecords(apn);
		
		softAssert.assertEquals(objNewConstructionPage.getFieldValueFromAPAS("PUC",
				"Parcel Information"),puc,
				"SMAB-T4368: Verify PUC is updated on parcel after approval of New Construction WI");

		objParcelsPage.waitForElementToBeVisible(20, objNewConstructionPage.ncAuditTrail);
		objParcelsPage.Click(objNewConstructionPage.ncAuditTrail);
		
		businessAudittrail.Click(businessAudittrail.relatedBusinessRecords);
		HashMap<String, ArrayList<String>> gridRelatedBusinessRecords = objNewConstructionPage.getGridDataInHashMap();
		String auditTrailSubject = gridRelatedBusinessRecords.get("Subject").get(0);
		softAssert.assertEquals(auditTrailSubject, "NC-Manual Entry",
				"SMAB-T4454: verify Subject of audit trail is NC-Manual Entry ");

		// Logout at the end of the test
		objNewConstructionPage.logout();

	}
}