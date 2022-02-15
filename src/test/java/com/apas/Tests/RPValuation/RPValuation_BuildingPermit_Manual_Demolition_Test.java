package com.apas.Tests.RPValuation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.DemolitionPage;
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

public class RPValuation_BuildingPermit_Manual_Demolition_Test extends TestBase implements users, testdata, modules {

	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	DemolitionPage ObjDemolitionPage;
	BuildingPermitPage ObjBuildingPermit;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		ObjDemolitionPage = new DemolitionPage(driver);
		ObjBuildingPermit = new BuildingPermitPage(driver);

	}

	/**
	 * This method is to verify that some fields are pre-populated from BP to the
	 * Demolition Screen
	 * 
	 * @param loginUser
	 */
	@Test(description = "SMAB-T7530,SMAB-T7572: Verify that Building permit should be automatically pre-populated through BP WI ", groups = {
			"Regression","RPValuation", "Demolition", "ManualDemolition", "BuildingPermit", "WorkItemWorkflow" }, dataProvider = "loginRPAppraiser", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void RPValuation_BuildingPermit_Manual_Demolition_WorkItem(String loginUser) throws Exception {
	
		// Fetching the active APN
		String executionEnv = System.getProperty("region");
		String query = "SELECT Name FROM Parcel__c where Status__C='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);

		String activeAPNW = response.get("Name").get(0);
		ReportLogger.INFO("Active APN has been fetched");

		// Step1: Login to the APAS application using the user passed through the data
		// provider
		ObjDemolitionPage.login(loginUser);

		// Step2: Opening the building permit module
		ObjDemolitionPage.searchModule(modules.BUILDING_PERMITS);

		// Step3: Manually creating building permit Record
		String mappingActionCreationData = testdata.BUILDING_PERMIT_DATA_DEMOLITION;
		Map<String, String> manualBuildingPermitMap = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"BuildingPermitManualCreationDataForDemolition");
		manualBuildingPermitMap.put("APN", activeAPNW);
		String buildingPermitNumber = manualBuildingPermitMap.get("Permit City Code") + "-"
				+ DateUtil.getCurrentDate("yyyMMdd-HHmmss");
		manualBuildingPermitMap.put("Building Permit Number", buildingPermitNumber);
		ObjBuildingPermit.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
		ObjDemolitionPage.waitForElementToBeClickable(ObjDemolitionPage.editButton, 15);

		// Capturing the fields values from building permit for the validation purpose
		String actualEstimatedProjectValue = ObjDemolitionPage.getFieldValueFromAPAS("Estimated Project Value",
				"Building Permit Information");
		String actualIssueDate = ObjDemolitionPage.getFieldValueFromAPAS("Issue Date", "Building Permit Information");
		String actualWorkDescription = ObjDemolitionPage.getFieldValueFromAPAS("Work Description",
				"Building Permit Information");
		String actualCompletionDate = ObjDemolitionPage.getFieldValueFromAPAS("Completion Date",
				"Building Permit Information");
		
		softAssert.assertEquals(ObjDemolitionPage.retiredAPNWarning.getAttribute("innerText"), " APN is retired.",
				"SMAB-T7572: Warning message is displayed successfully.");

		// Fetching the DEMO WI and marking it in 'In Progress' status
		String demolitionWorkItem = salesforceAPI.select("SELECT Name,id FROM Work_Item__c order by name desc limit 1")
				.get("Name").get(0);
		String queryWI = "Select Id from Work_Item__c where Name = '" + demolitionWorkItem + "'";
		salesforceAPI.update("Work_Item__c", queryWI, "Status__c", "In Progress");

		// Moving to the DEMO WI
		String WorkitemID = salesforceAPI.select("SELECT Id FROM Work_Item__c where Name='" + demolitionWorkItem + "'")
				.get("Id").get(0);
		driver.navigate().to(
				"https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Work_Item__c/" + WorkitemID + "/view");
		
		objParcelsPage.waitForElementToBeClickable(20,objWorkItemHomePage.TAB_IN_PROGRESS);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		Thread.sleep(3000);

		// Capturing the fields values from Demolition screen for validation
		String expectedEstimatedProjectValue = ObjDemolitionPage.getFieldValueFromAPAS("Estimated Project Value",
				"Building Permit Details");
		String expectekdIssueDate = ObjDemolitionPage.getFieldValueFromAPAS("Issue Date", "Building Permit Details");
		String expectedWorkDescription = ObjDemolitionPage.getFieldValueFromAPAS("Work Description",
				"Building Permit Details");
		String expectedCompletionDate = ObjDemolitionPage.getFieldValueFromAPAS("Completion Date",
				"Building Permit Details");

		// Performing all the validations
		softAssert.assertEquals(actualEstimatedProjectValue, expectedEstimatedProjectValue,
				"SMAB-T7530: Project Value is matched successfully");
		softAssert.assertEquals(actualIssueDate, expectekdIssueDate, "SMAB-T7530: Issue Date is matched successfully");
		softAssert.assertEquals(actualWorkDescription, expectedWorkDescription,
				"SMAB-T7530: Work Description is matched successfully");
		softAssert.assertEquals(actualCompletionDate, expectedCompletionDate,
				"SMAB-T7530: Completion Date is matched successfully");

		// Logout at the end of the test
		ObjDemolitionPage.logout();
	}
}
