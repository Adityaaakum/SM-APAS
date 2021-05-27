package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class WorkItemAdministration_WorkItemLinkage_Test extends TestBase implements testdata, modules, users {
	RemoteWebDriver driver;
	
	ParcelsPage objParcelsPage;
    WorkItemHomePage objWorkItemHomePage;
    MappingPage objMappingPage;
    Util objUtil = new Util();
    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI salesforceAPI = new SalesforceAPI();

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();
        objMappingPage= new MappingPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objParcelsPage = new ParcelsPage(driver);
    }

    @Test(description = "SMAB-T2658: Verify APN field when there is only parcel record are added to WI via Linkages", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "Regression", "WorkItemAdministration" }, alwaysRun = true)
    public void WorkItemAdministration_VerifyAPNColumn(String loginUser) throws Exception {
    	//Fetch Active APN
    	String apn = objMappingPage.fetchActiveAPN();
    	
    	//Setup data to create Manual WI
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to create manual WI
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		// Step 4: Verify APN Column on WI Details Page
		String apnOnWIDetailsTab = objWorkItemHomePage.getFieldValueFromAPAS("APN");
		softAssert.assertEquals(apnOnWIDetailsTab, apn, "SMAB-T2658: Verify APN column displays parcel number");
    
		// Step 5: Navigate to Home Page and verify APN Column
    	objMappingPage.searchModule(HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInProgress);
		Thread.sleep(2000);
		WebElement WI = objWorkItemHomePage.searchWIinGrid(WINumber);
		String updatedWINumber = WINumber + "\n" + "Launch Data Entry Screen";
		if(objWorkItemHomePage.verifyElementVisible(WI)) {
			HashMap<String, ArrayList<String>> gridData = objMappingPage.getGridDataInHashMap();	
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(updatedWINumber)), apn, "SMAB-T2658: APN Number Validation for Work Item");
		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2658: Not able to find Work Item on Home Page: "+WINumber);


    }
    
    @Test(description = "SMAB-T2654,SMAB-T2656: Verify APN field when there are more than one parcel records are added to WI via Linkages", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "Regression", "WorkItemAdministration" }, alwaysRun = false)
    public void WorkItemAdministration_VerifyAPNColumnWithMoreThan1APN(String loginUser) throws Exception {
    	//Fetch Active APN
    	ArrayList<String> apns = objMappingPage.fetchActiveAPN(2);
    	String apn = apns.get(0);
    	
    	//Setup data to create Manual WI
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to create manual WI
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		// Step 4: Creating WI Linkage
		String apnForLinkage = apns.get(1);
		objWorkItemHomePage.createWorkItemLinkage(apnForLinkage);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.linkedItemsWI);		
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		// Step 5: Verify APN Field value
		String apnOnWIDetailsTab = objWorkItemHomePage.getFieldValueFromAPAS("APN");
		softAssert.assertEquals(apnOnWIDetailsTab, apnForLinkage, "SMAB-T2656: Verify APN column displays parcel number");
		String updatedWINumber = WINumber + "\n" + "Launch Data Entry Screen";
		
		// Step 6: Navigate to Home Page and verify APN Column under In Progress tab
    	objMappingPage.searchModule(HOME);
    	Thread.sleep(2000);
    	objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
		WebElement WI = objWorkItemHomePage.searchWIinGrid(WINumber);
		HashMap<String, ArrayList<String>> gridData;
		
		if(objWorkItemHomePage.verifyElementVisible(WI)) {
			gridData = objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(updatedWINumber)), "Multiple", "SMAB-T2654: APN Number Validation for Work Item");

		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2654: Not able to find Work Item on Home Page: "+WINumber);
    
		// Step 7: Navigate to In Progress tab, move the WI to In Pool and verify APN Column under In Pool tab
		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.returnToPool));
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10); 
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
		boolean WIPresent = objWorkItemHomePage.searchWIInGrid(WINumber);
		
		if(WIPresent) {
			gridData = objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(updatedWINumber)), "Multiple", "SMAB-T2654: APN Number Validation for Work Item");
	
		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2654: Not able to find Work Item on Home Page: "+WINumber);
		
		// Step 8: Navigate to In Pool tab, move the WI to In Progress and then to On Hold and verify APN Column under On Hold tab
		objWorkItemHomePage.acceptWorkItem(WINumber);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.PutOnHoldButton));
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_On_Hold);
		WI = objWorkItemHomePage.searchWIinGrid(WINumber);
		
		if(objWorkItemHomePage.verifyElementVisible(WI)) {
			gridData = objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(updatedWINumber)), "Multiple", "SMAB-T2654: APN Number Validation for Work Item");
	
		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2654: Not able to find Work Item on Home Page: "+WINumber);
		
		// Step 9: Navigate to On Hold tab, move the WI to In Progress and then to Submit For Approval and verify APN Column under Submitted For Approval tab
		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.markInProgress));
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);
		objWorkItemHomePage.Click(objWorkItemHomePage.btnMarkComplete);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_MY_SUBMITTED_FOR_APPROVAL);
		WI = objWorkItemHomePage.searchWIinGrid(WINumber);
		
		if(objWorkItemHomePage.verifyElementVisible(WI)) {
			gridData = objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(updatedWINumber)), "Multiple", "SMAB-T2654: APN Number Validation for Work Item");
	
		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2654: Not able to find Work Item on Home Page: "+WINumber);
		
		//Step 10: Logout & Login to the APAS application using Supervisor of Work Item
		objWorkItemHomePage.logout();
		Thread.sleep(15000);
		objWorkItemHomePage.login(users.RP_BUSINESS_ADMIN);		
		
		//Step 11:Navigate to home and need my approval tab
		objWorkItemHomePage.searchModule(modules.HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);

		//Step 12:Select Work Item
		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);

		//Step 13:Approve the WI and Navigate to Completed tab to verify APN column
		objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_COMPLETED);
		WI = objWorkItemHomePage.searchWIinGrid(WINumber);
		
		if(objWorkItemHomePage.verifyElementVisible(WI)) {
			gridData = objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(updatedWINumber)), "Multiple", "SMAB-T2654: APN Number Validation for Work Item");
	
		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2654: Not able to find Work Item on Home Page: "+WINumber);
		
    
    }
    @Test(description = "SMAB-T2657: Verify APN field when there are more than one parcel records linked to WI via Linkage and all the records are deleted except one", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "Regression", "WorkItemAdministration" }, alwaysRun = true)
    public void WorkItemAdministration_VerifyAPNColumnAfterDeletingAPNLinked(String loginUser) throws Exception {
    	//Fetch Active APN
    	ArrayList<String> apns = objMappingPage.fetchActiveAPN(2);
    	String apn = apns.get(0);
    	
    	//Setup data to create Manual WI
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to create manual WI
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		// Step 4: Creating WI Linkage
		String apnForLinkage = apns.get(1);
		objWorkItemHomePage.createWorkItemLinkage(apnForLinkage);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.linkedItemsWI);		
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		// Step 5: Verify APN Field value
		String apnOnWIDetailsTab = objWorkItemHomePage.getFieldValueFromAPAS("APN");
		softAssert.assertEquals(apnOnWIDetailsTab, apnForLinkage, "SMAB-T2657: Verify APN column displays parcel number");
    
		// Step 6: Deleting APN linked recently
		objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsTab);
		objWorkItemHomePage.deleteWorkItemLinkage(apnForLinkage);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		// Step 7: Verify APN Field value after WI linkage is deleted
		apnOnWIDetailsTab = objWorkItemHomePage.getFieldValueFromAPAS("APN");
		softAssert.assertEquals(apnOnWIDetailsTab, apn, "SMAB-T2657: Verify APN column displays parcel number");
    
    }
}
