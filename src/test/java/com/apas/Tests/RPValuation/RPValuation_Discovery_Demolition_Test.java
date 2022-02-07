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
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class RPValuation_Discovery_Demolition_Test extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	DemolitionPage ObjDemolitionPage;
	AuditTrailPage businessAuditTrail;


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		ObjDemolitionPage = new DemolitionPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		businessAuditTrail = new AuditTrailPage(driver);


	}

	/**
	 * This method is to verify few validations based on full demolition , partial
	 * demolition event codes also verify if few buttons are present on the
	 * Demolition Page
	 * 
	 * @param loginUser
	 */
	@Test(description = "SMAB-T4455,SMAB-T4366,SMAB-T4377,SMAB-T4378,SMAB-T4379,SMAB-T4403:This method is to verify few validations based on full demolition , partial demolition event codes", dataProvider = "loginRPAppraiser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "RPValuation", "Demolition", "DiscoveryDemolition"})
	public void RPValuation_Manual_Demolition_WorkItem(String loginUser) throws Exception {

		// Fetching Active parcel
		String executionEnv = System.getProperty("region");
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Status__c = 'Active' Limit 1";
		String apn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String apnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeRPDemolition");

		// Fetching some Event codes from database
		String anyEventCode = salesforceAPI.select("SELECT Name FROM Event_Library__c where Name like 'DEMO%' limit 1")
				.get("Name").get(0);
		String fullDemolitionEventCode = salesforceAPI
				.select("SELECT Name FROM Event_Library__c where Name like 'DEMO-%SUP' limit 1").get("Name").get(0);
		String pucOtherThanVacantLand = salesforceAPI
				.select("SELECT Name,id  FROM PUC_Code__c where not name like '%Vacant%' limit 1").get("Name").get(0);
		String partialDemolitionEventCode = salesforceAPI
				.select("SELECT Name FROM Event_Library__c where Name like 'DEMO-partial%' limit 1").get("Name").get(0);
		String noStartDemolitionEventCode = salesforceAPI
				.select("SELECT Name FROM Event_Library__c where Name like 'DEMO-NS' limit 1").get("Name").get(0);

		// Step1: Login to the APAS application
		ObjDemolitionPage.login(loginUser);

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
		String demolitionScreenURL = driver.getCurrentUrl();
		ObjDemolitionPage.searchModule("APAS");
		driver.navigate().to(demolitionScreenURL);

		ObjDemolitionPage.waitForElementToBeVisible(
				ObjDemolitionPage.getButtonWithText(ObjDemolitionPage.createNewConstructionWIBtn));
		softAssert.assertTrue(
				!(ObjDemolitionPage.isNotDisplayed(
						ObjDemolitionPage.getButtonWithText(ObjDemolitionPage.createNewConstructionWIBtn))),
				"SMAB-T4366:New Construction WI Button is present");

		// Validating if the fields are in read Only mode
		boolean editButtonStatus = objParcelsPage.verifyEditButtonIsPresent("APN");
		softAssert.assertEquals(editButtonStatus, false, "SMAB-T4366:APN field is read Only");

		editButtonStatus = objParcelsPage.verifyEditButtonIsPresent("Appraiser Activity Status");
		softAssert.assertEquals(editButtonStatus, false, "SMAB-T4366:Appraiser Activity Status field is read Only");

		editButtonStatus = objParcelsPage.verifyEditButtonIsPresent("Building Permit");
		softAssert.assertEquals(editButtonStatus, false, "SMAB-T4366:Building Permit field is read Only");

		ObjDemolitionPage.waitForElementToBeVisible(20,
				ObjDemolitionPage.getButtonWithText(ObjDemolitionPage.createNewConstructionWIBtn));
		objWorkItemHomePage.scrollToBottom();

		// Checking if proper error message is displayed if user don't enter event code
		// and DOV
		WebElement editButton = objParcelsPage.editFieldButton("Event Code");
		objParcelsPage.Click(editButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		List<WebElement> errorMessage = driver.findElements(By.xpath(ObjDemolitionPage.errorMessageWithLinks));

		softAssert.assertEquals(errorMessage.get(0).getText(), "DOV", "SMAB-T4366: DOV is present in error message");
		softAssert.assertEquals(errorMessage.get(1).getText(), "Event Code",
				"SMAB-T4366: Event Code is present in error message");

		// Validating user is able to click on createNewConstructionWI Button
		objParcelsPage.searchAndSelectOptionFromDropDown("Event Code", anyEventCode);
		objParcelsPage.enter("DOV", "1/13/2022");
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		ObjDemolitionPage.Click(ObjDemolitionPage.getButtonWithText(ObjDemolitionPage.createNewConstructionWIBtn));
		ObjDemolitionPage.waitForElementToBeVisible(20, ObjDemolitionPage.errorMsgUp);

		softAssert.assertEquals(ObjDemolitionPage.errorMsgUp.getText(),
				"A New Construction WI will be created and assigned to you.",
				"SMAB-T4366:New Construction message is dispalyed successfully");

		// Validating if user is able to 'Submit For Approval (DEMO)' Button
		ObjDemolitionPage.Click(ObjDemolitionPage.getButtonWithText("Finish"));
		ObjDemolitionPage.waitForElementToBeVisible(20, ObjDemolitionPage.submitForApprovalDEMOWIBtn);

		softAssert.assertEquals(
				ObjDemolitionPage.getButtonWithText(ObjDemolitionPage.submitForApprovalDEMOWIBtn).getText(),
				"Submit For Approval (DEMO)", "SMAB-T4366:Submit for approval DEMO button is present on page");

		// Validating error message in case of full demolition event code with vacant
		// land as PUC
		objWorkItemHomePage.scrollToBottom();
		objParcelsPage.Click(objParcelsPage.editFieldButton("Event Code"));
		objParcelsPage.clearSelectionFromLookup("Event Code");
		objParcelsPage.searchAndSelectOptionFromDropDown("Event Code", fullDemolitionEventCode);
		objWorkItemHomePage.scrollToTop();
		objParcelsPage.clearSelectionFromLookup("PUC Code");
		objParcelsPage.searchAndSelectOptionFromDropDown("PUC Code", pucOtherThanVacantLand);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		String errorMessageDown = driver.findElement(By.xpath(ObjDemolitionPage.errorMsgDown)).getText();

		softAssert.assertEquals(errorMessageDown,
				"A Full Demolition Event Code requires that the PUC Code can only be Vacant Land.",
				"SMAB-T4377:Error message is displayed successfully");

		// Validating error message in case of partial demolition event code with vacant
		// land as PUC
		ObjDemolitionPage.Click(ObjDemolitionPage.crossButton);
		objParcelsPage.clearSelectionFromLookup("Event Code");
		objParcelsPage.searchAndSelectOptionFromDropDown("Event Code", partialDemolitionEventCode);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		errorMessageDown = driver.findElement(By.xpath(ObjDemolitionPage.errorMsgDown)).getText();
		softAssert.assertEquals(errorMessageDown,
				"A Partial Demolition Event Code requires that the Improvement Value should not be blank.",
				"SMAB-T4378: Error messgae is displayed sucessfully");

		// Validating error message in case of no start demolition event code
		ObjDemolitionPage.Click(ObjDemolitionPage.crossButton);
		objParcelsPage.clearSelectionFromLookup("Event Code");
		objParcelsPage.searchAndSelectOptionFromDropDown("Event Code", noStartDemolitionEventCode);
		objParcelsPage.enter("Land Cash Value", "1");
		objParcelsPage.enter("Improvement Cash Value", "1");
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		Thread.sleep(2000);
		errorMessageDown = driver.findElement(By.xpath(ObjDemolitionPage.errorMsgDown)).getText();

		softAssert.assertEquals(errorMessageDown,
				"For No Start Event Code the Improvement Cash Value and Land Cash Value should be blank.",
				"SMAB-T4379: Error message is displayed successfully");
		ObjDemolitionPage.Click(ObjDemolitionPage.crossButton);
		ObjDemolitionPage.Click(objParcelsPage.getButtonWithText(ObjDemolitionPage.cancel));
		objWorkItemHomePage.scrollToTop();

		// Staff is submitting request for approval to supervisor
		objParcelsPage.Click(objParcelsPage.getButtonWithText(ObjDemolitionPage.submitForApprovalDEMOWIBtn));
		objParcelsPage.Click(objParcelsPage.getButtonWithText(ObjDemolitionPage.nextButton));

		// Staff Logout
		objWorkItemHomePage.logout();
		Thread.sleep(5000);

		// Supervisor logins
		ObjDemolitionPage.login(users.RP_PRINCIPAL);

		// Validating 'Return' and 'Approve(Demo)' Button is visible to Supervisor
		driver.navigate().to(demolitionScreenURL);
		softAssert.assertEquals(ObjDemolitionPage.getButtonWithText(ObjDemolitionPage.returnButton).getText(), "Return",
				"SMAB-T4403:Return button is present");

		softAssert.assertEquals(ObjDemolitionPage.getButtonWithText(ObjDemolitionPage.approveDemoButton).getText(),
				"Approve(Demo)", "SMAB-T4403:Approve(DEMO) button is present");
		
		objParcelsPage.Click(objParcelsPage.getButtonWithText(ObjDemolitionPage.approveDemoButton));
		
		ObjDemolitionPage.searchModule(PARCELS);
		ObjDemolitionPage.globalSearchRecords(apn);
		
		objParcelsPage.waitForElementToBeVisible(20, ObjDemolitionPage.demoAuditTrail);
		objParcelsPage.Click(ObjDemolitionPage.demoAuditTrail);
		
		businessAuditTrail.Click(businessAuditTrail.relatedBusinessRecords);
		HashMap<String, ArrayList<String>> gridRelatedBusinessRecords = ObjDemolitionPage.getGridDataInHashMap();
		String auditTrailSubject = gridRelatedBusinessRecords.get("Subject").get(0);
		softAssert.assertEquals(auditTrailSubject, "Demo - Manual Entry",
				"SMAB-T4455: verify Subject of audit trail is Demo - Manual Entry");

		// Logout at the end of the test
		ObjDemolitionPage.logout();

	}

}
