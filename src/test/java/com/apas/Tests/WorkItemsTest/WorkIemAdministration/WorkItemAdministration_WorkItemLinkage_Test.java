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
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.Page;
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
	Page objPage;
    WorkItemHomePage objWorkItemHomePage;
    MappingPage objMappingPage;
    ApasGenericPage objApasGenericPage;
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
        objApasGenericPage = new ApasGenericPage(driver);
        objPage = new Page(driver);
    }

    @Test(description = "SMAB-T2658: Verify APN field when there is only parcel record are added to WI via Linkages", 
    		dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, 
    		groups = {"Regression", "WorkItemAdministration" }, alwaysRun = true)
    public void WorkItemAdministration_VerifyAPNColumn(String loginUser) throws Exception {
    	//Fetch Active APN
    	String apn = objMappingPage.fetchActiveAPN();
    	
    	//Setup data to create Manual WI
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = 
				objUtil.generateMapFromJsonFile(workItemCreationData,
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
    
    @Test(description = "SMAB-T2654,SMAB-T2656: Verify APN field when there are more than one "
    		+ "parcel records are added to WI via Linkages", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "Regression", "WorkItemAdministration" }, alwaysRun = false)
    public void WorkItemAdministration_VerifyAPNColumnWithMoreThan1APN(String loginUser) throws Exception {
    	//Fetch Active APN
    	ArrayList<String> apns = objMappingPage.fetchActiveAPN(2);
    	String apn = apns.get(0);
    	String execEnv = System.getProperty("region");
    	String queryAPN = "select Id from Parcel__c where Name = '"+apn+"'";
    	HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apnId= responseAPNDetails.get("Id").get(0);
    	//Setup data to create Manual WI
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to create manual WI
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		
		driver.navigate().to("https://smcacre--"
				+ execEnv + ".lightning.force.com/lightning/r/Parcel__c/"+apnId+"/view");
		// Step 3: Creating Manual work item for the Parcel
		objMappingPage.waitForElementToBeClickable(objParcelsPage.componentActionsButtonText);
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
    	objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
    	Thread.sleep(2000);
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
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
    	Thread.sleep(2000);
    	objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.getButtonWithText("Accept Work Item"));
		boolean WIPresent = objWorkItemHomePage.searchWIInGrid(WINumber);
		if(WIPresent) {
			gridData = objMappingPage.getGridDataInHashMap();
			
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(WINumber)), "Multiple", "SMAB-T2654: APN Number Validation for Work Item");
	
		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2654: Not able to find Work Item on Home Page: "+WINumber);
		
		// Step 8: Navigate to In Pool tab, move the WI to In Progress and then to On Hold and verify APN Column under On Hold tab
		objWorkItemHomePage.acceptWorkItem(WINumber);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
    	Thread.sleep(2000);
		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.PutOnHoldButton));
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_On_Hold);
		WI = objWorkItemHomePage.searchWIinGrid(WINumber);
		
		if(objWorkItemHomePage.verifyElementVisible(WI)) {
			gridData = objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(WINumber)), "Multiple", "SMAB-T2654: APN Number Validation for Work Item");
	
		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2654: Not able to find Work Item on Home Page: "+WINumber);
		
		// Step 9: Navigate to On Hold tab, move the WI to In Progress and then to Submit For Approval and verify APN Column under Submitted For Approval tab
		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.markInProgress));
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
    	Thread.sleep(2000);
		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);
		objWorkItemHomePage.Click(objWorkItemHomePage.btnMarkComplete);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_MY_SUBMITTED_FOR_APPROVAL);
		WI = objWorkItemHomePage.searchWIinGrid(WINumber);
		
		if(objWorkItemHomePage.verifyElementVisible(WI)) {
			gridData = objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(WINumber)), "Multiple", "SMAB-T2654: APN Number Validation for Work Item");
	
		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2654: Not able to find Work Item on Home Page: "+WINumber);
		
		//Step 10: Logout & Login to the APAS application using Supervisor of Work Item
		objWorkItemHomePage.logout();
		Thread.sleep(15000);
		objWorkItemHomePage.login(users.MAPPING_SUPERVISOR);		
		
		//Step 11:Navigate to home and need my approval tab
		objWorkItemHomePage.searchModule(modules.HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
    	Thread.sleep(2000);
		//Step 12:Select Work Item

		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);

		//Step 13:Approve the WI and Navigate to Completed tab to verify APN column
		objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10);
		objWorkItemHomePage.logout();
		Thread.sleep(15000);
		objWorkItemHomePage.login(users.RP_APPRAISER);		
		//Step 11:Navigate to home and need my approval tab
		objWorkItemHomePage.searchModule(modules.HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
    	Thread.sleep(2000);
		//Step 12:Select Work Item
		objWorkItemHomePage.selectWorkItemOnHomePage(WINumber);

		//Step 13:Approve the WI and Navigate to Completed tab to verify APN column
		objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.successAlert, 20);
		objWorkItemHomePage.waitForElementToDisappear(objWorkItemHomePage.successAlert, 10);
		
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_COMPLETED);
    	Thread.sleep(2000);
		WI = objWorkItemHomePage.searchWIinGrid(WINumber);
		for(int i=0;i<10;i++) {
		if(objWorkItemHomePage.verifyElementVisible(WI)) {
			gridData = objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridData.get("APN").get(gridData.get("Work item #").indexOf(WINumber)), "Multiple", "SMAB-T2654: APN Number Validation for Work Item");
	break;
		}
		else if(objWorkItemHomePage.getButtonWithText("Next").isDisplayed()) {
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText("Next"));
			Thread.sleep(2000);
		}
		else	{	
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2654: Not able to find Work Item on Home Page: "+WINumber);
		
		}
		}
		objWorkItemHomePage.logout();
    }
    @Test(description = "SMAB-T2657,SMAB-T2620: Verify APN field when there are more than one parcel records "
    		+ "linked to WI via Linkage and all the records are deleted except one", 
    		dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "Regression", "WorkItemAdministration", "WILinkage"},
    		alwaysRun = true)
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
		
		// Step 5: Verify APN Field value linked
		String apnOnWIDetailsTab = objWorkItemHomePage.getFieldValueFromAPAS("APN");
		softAssert.assertEquals(apnOnWIDetailsTab, apnForLinkage, "SMAB-T2657: Verify APN column "
				+ "displays parcel number :"+apnOnWIDetailsTab);
    
		// Step 6: Deleting APN linked recently
		objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsTab);
		objWorkItemHomePage.deleteWorkItemLinkage(apnForLinkage);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		// Step 7: Verify APN Field value after WI linkage is deleted
		apnOnWIDetailsTab = objWorkItemHomePage.getFieldValueFromAPAS("APN");
		softAssert.assertEquals(apnOnWIDetailsTab, apn, "SMAB-T2657: Verify APN column "
				+ "displays parcel number :"+apnOnWIDetailsTab);
		
		// Step 8: Deleting Last APN linked 
		objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsTab);		
		objWorkItemHomePage.deleteWorkItemLinkage(apnOnWIDetailsTab);
        
		String expectedWarningMsg = "You cannot delete the last related Parcel from the Work Item. "
				+ "Either edit the existing Parcel on the Work Item Linkage, "
				+ "or create a new Parcel linkage record and delete the previous one.";
		
		String actualWarningMsg = objApasGenericPage.getAlertMessage();
		// Step 9: Verify last APN linked is not able to delete and gets a warning msg
		softAssert.assertContains(actualWarningMsg, expectedWarningMsg,"SMAB-T2620: "
				+ "Verify the Warning Msg is displayed on deleting last linked APN :" +actualWarningMsg);
    }
    
    @Test(description = "SMAB-T2619,SMAB-T2675,SMAB-T2676: Verify that the  APAS User who is assigned the RP Work Item Linkages - CRED "
    		+ "view permissions, On click on edit option user can change the related Parcel on the Work Item linkage record", 
    		dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, 
    		groups = {"Regression", "WorkItemAdministration","WILinkage" }, 
    		alwaysRun = true)
    public void WorkItemAdministration_VerifyWILinkedAPNIsUpdatedOnEdit(String loginUser) throws Exception {
    	//Fetch Active APN
    	ArrayList<String> apns = objMappingPage.fetchActiveAPN(2);
    	String apn = apns.get(0);
    	    	
    	//Setup data to create Manual WI
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		//Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		//Step2: Opening the PARCELS page and searching the parcel to create manual WI
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);		
		
		//Step 3: Creating Manual work item for the Parcel
		String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
        
		objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsTab);		
		
		// verify Duplicate validation on APN field
		objWorkItemHomePage.editWorkItemLinkage(apn, apn);
		WebElement errorMsgB0x = objPage.locateElement("//div[@class='error']", 15);
		String actualErrorMsgforDuplicateAPN = errorMsgB0x.getText();
		String expectedErrorMsgforDuplicateAPN = "A linkage record for selected the parcel already exists. "
				+ "Please choose other parcels.";
		
		softAssert.assertContains(actualErrorMsgforDuplicateAPN, expectedErrorMsgforDuplicateAPN,
				"SMAB-T2676: Verify the Warning Msg is displayed for duplicate Linked APN :" +actualErrorMsgforDuplicateAPN);		
		
		//verify for APN is a required field when linking New APN
		String xpathClearSection = "//div/button[@title='Clear Selection']";			
		WebElement clearText = objPage.waitForElementToBeClickable(10,xpathClearSection);
		objPage.Click(clearText);
		  objPage.enter(objPage.getWebElementWithLabel("APN"),"");
		  objPage.Click(objPage.getButtonWithText("Save"));
		  errorMsgB0x = objPage.locateElement("//div[@class='error']", 15);
		  String actualErrorMsgforBlankAPN = errorMsgB0x.getText();
		  objPage.Click(objPage.getButtonWithText("Cancel"));
		  
		  String expectedErrorMsgforBlankAPN = "the field can not be blank.";
		  
		  softAssert.assertContains(actualErrorMsgforBlankAPN,  expectedErrorMsgforBlankAPN,
		  "SMAB-T2676: Verify the Warning Msg is displayed for BLANK APN field :"
		  +actualErrorMsgforBlankAPN);		 
			
		//verify the APN field is Editable
		String expectedUpdatedAPN = apns.get(1);
		objWorkItemHomePage.editWorkItemLinkage(apn, expectedUpdatedAPN);
		String query_1 = "SELECT Id,Event_ID__c from work_item__c where name = '"+WINumber+"'";        
        HashMap<String, ArrayList<String>> response_1 = salesforceAPI.select(query_1);        
        String Id = response_1.get("Id").get(0);
        String eventID = response_1.get("Event_ID__c").get(0);
        
        String expectedNavigationURL = "/flow/processParcelFlow?workItemId="+Id+""
        		+ "&reasonCode="+eventID+""
        		+ "&apn="+apn+","+expectedUpdatedAPN+"";
        
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		String actualApnOnWIDetailsTab = objWorkItemHomePage.getFieldValueFromAPAS("APN");
		String actualNavigationURL = objWorkItemHomePage.getFieldValueFromAPAS("Navigation Url");
		
		softAssert.assertEquals(actualApnOnWIDetailsTab, expectedUpdatedAPN, "SMAB-T2619,SMAB-T2675: "
				+ "Verify APN field has the updated APN :"+actualApnOnWIDetailsTab);
		softAssert.assertEquals(actualNavigationURL, expectedNavigationURL, "SMAB-T2619,SMAB-T2675: "
				+ "Verify Navigation URL field has the updated navigation url :"+actualNavigationURL);
		
		
    }
		
}

