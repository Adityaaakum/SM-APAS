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
	String manualWIFilePath;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		ObjDemolitionPage = new DemolitionPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		manualWIFilePath = testdata.MANUAL_WORK_ITEMS;

	}

	/**
	 * This method is to verify few validations based on full demolition , partial
	 * demolition event codes also verify if few buttons are present on the
	 * Demolition Page
	 * 
	 * @param loginUser
	 */
	@Test(description = "SMAB-T4366,SMAB-T4377,SMAB-T4378,SMAB-T4379,SMAB-T4403:This method is to verify few validations based on full demolition , partial demolition event codes", dataProvider = "loginRPAppraiser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "RPValuation", "Demolition", "DiscoveryDemolition", "WorkItemWorkflow", "BuildingPermit"})
	public void BuildingPermit_Manual_Discovery_Demolition_WorkItem(String loginUser) throws Exception {

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
		if (ObjDemolitionPage.isNotDisplayed(ObjDemolitionPage.apas)) {
			ObjDemolitionPage.searchModule("APAS");
		}
		driver.navigate().to(demolitionScreenURL);
        Thread.sleep(5000);
		ObjDemolitionPage.waitForElementToBeVisible(30,
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

		// Logout at the end of the test
		ObjDemolitionPage.logout();

	}
	
	/*
	 * This method is to create a WI for the Demolition Discovery by the Appraiser
	 * @param loginUser
	 * @throws Exception
	 */

	@Test(description = "SMAB-T4272,SMAB-T4273,SMAB-T4274:RP Demolition Discovery- Verify that Appraisers "
			+ "will have the ability to create a demolition discovery work item from the Component Actions"
			+ " list and its Type label should be NC, the Actions label should be Demo - Other", 
			dataProvider = "loginRPAppraiser", 
			dataProviderClass = DataProviders.class , 
			groups = {"Regression", "RPValuation", "Demolition", "DiscoveryDemolition", "WorkItemWorkflow", "BuildingPermit"})
	public void BuildingPermit_Manual_Discovery_Demolition_ValidateWorkItemDetails(String loginUser) throws Exception {
		
		//Fetching parcel that are Active
		String queryApnDetails ="SELECT Id,Name FROM Parcel__c where primary_situs__c != NULL and "
				+ "Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where "
				+ "type__c='CIO') and (Not Name like '100%') and (Not Name like '800%') "
				+ "and (Not Name like '%990') and (Not Name like '134%') Limit 2";
		
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryApnDetails);
		String apn1=responseAPNDetails.get("Name").get(0);		
		String apnId1=responseAPNDetails.get("Id").get(0);
				
		Map<String, String> newConstructionWIData = objUtil.generateMapFromJsonFile(manualWIFilePath, 
				                               "DataToCreateWorkItemOfTypeNCWithActionDemolitionOther");
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ReportLogger.INFO("Step 1: Login to the Salesforce ");
		ObjDemolitionPage.login(loginUser);
		ObjDemolitionPage.searchModule(modules.APAS);
		
		// Step2: Navigating to the Parcel View page								
		String execEnv = System.getProperty("region");
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com"
				+ "/lightning/r/Parcel__c/"
				+ apnId1 + "/view");
				
		// Step 3: Creating Manual work item for the Parcel 
		String WINumber = objParcelsPage.createWorkItem(newConstructionWIData);
		
		if(!WINumber.isEmpty()) {			
			softAssert.assertTrue(true, "SMAB-T4272: Demolition Discovery WI is created successfully");
		}
		else {	
			softAssert.assertTrue(false, "SMAB-T4272: Demolition Discovery WI is created successfully");
		}
						
		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
	
		String sqlWIDetails = "select type__c ,sub_type__c ,"
				+ "use_code_f__c , event_id__c ,request_type__c "
				+ "from work_item__c where name = '"+WINumber+"'";
				
		String expectedUseCode = salesforceAPI.select(sqlWIDetails).get("Use_Code_f__c").get(0);
		
		//verify the WI details		
		String actualType = objWorkItemHomePage.getFieldValueFromAPAS("Type"); 
		String actualAction = objWorkItemHomePage.getFieldValueFromAPAS("Action");;
		String actualReference = objWorkItemHomePage.getFieldValueFromAPAS("Reference");;
		String actualRequestType = objWorkItemHomePage.getFieldValueFromAPAS("Request Type");;
		String actualRelatedAction = objWorkItemHomePage.getFieldValueFromAPAS("Related Action");;
		String actualAPN = objWorkItemHomePage.getFieldValueFromAPAS("APN");
		String actualEventID = objWorkItemHomePage.getFieldValueFromAPAS("Event ID");
		String actualUseCode = objWorkItemHomePage.getFieldValueFromAPAS("Use Code");
		
		//Assertions
		softAssert.assertEquals(actualType, "NC", "SMAB-T4273: The WI Type is verified");
		softAssert.assertEquals(actualAction, "Demo - Other", "SMAB-T4273: The WI Action is verified");
		softAssert.assertContains(actualReference, "DC", "SMAB-T4273: The WI Reference is verified");
		softAssert.assertContains(actualRequestType, "NC - Demo - Other", "SMAB-T4273: The WI RequestType is verified");
		softAssert.assertEquals(actualRelatedAction, "Demo - Other", "SMAB-T4273: The WI Related Action is verified");
		softAssert.assertEquals(actualAPN, apn1, "SMAB-T4273: The WI APN is verified");
		softAssert.assertContains(actualEventID, "DC", "SMAB-T4273: The WI Event ID is verified");
		softAssert.assertEquals(actualUseCode, expectedUseCode, "SMAB-T4273: The WI Use Code is verified");
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		ObjDemolitionPage.switchToNewWindow(parentWindow);
		
		String newConstructionPage = "As part of the new construction process, confirm and update the values as needed:";		
		String  constructPage = driver.findElement(By.xpath("//div[@class='slds-rich-text-editor__output uiOutputRichText forceOutputRichText']/p/span")).getText();
		
		boolean flag= false;
	    if(constructPage.equalsIgnoreCase(newConstructionPage)) {
	    	flag = true;
	    	softAssert.assertTrue(flag, "SMAB-T4274:" + newConstructionPage);
	    }	
		
	    // Logout at the end of the test
	    ObjDemolitionPage.logout();
	}

}
