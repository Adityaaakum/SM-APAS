package com.apas.Tests.WorkItemsTest.WorkItemWorkFlow;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.ExcelUtils;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.BPFileSource;
import com.apas.config.fileTypes;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.relevantcodes.extentreports.LogStatus;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestClass extends TestBase{

    RemoteWebDriver driver;
    Page objPage;
    BuildingPermitPage objBuildingPermitPage;
    WorkItemHomePage objWorkItemHomePage;
    EFileImportPage objEfileImportPage;
    ReportsPage objReportsPage;

    SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {
        
    	driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();
        
        objBuildingPermitPage = new BuildingPermitPage(driver);
        objEfileImportPage = new EFileImportPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objReportsPage = new ReportsPage(driver);
    }
    
    
    
    @Test(description = "SMAB-T383,SMAB-T520,SMAB-T402,SMAB-T421,SMAB-T416: Creating manual entry for building permit", dataProvider = "RPAppraiser", dataProviderClass = com.apas.DataProviders.DataProviders.class, groups={"Smoke","Regression","BuildingPermit"}, alwaysRun = true)
	public void BuildingPermit_ManualCreateNewBuildingPermitWithDataValidations(String loginUser) throws Exception {

    	//waitForElementToBeVisible

		//Fetching the Active Parcel
		String query ="SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Active' and Primary_Situs__C !='' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String parcelToSearch = response.get("Name").get(0);

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the Parcels module
		objBuildingPermitPage.searchModule(modules.PARCELS);

		//Step3: Search and Open the Parcel
		objBuildingPermitPage.globalSearchRecords(parcelToSearch);
		
		//Step4: Opening the building permit module
		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

		//Step5: Prepare a test data to create a new building permit
		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestDataWithComplitionDate();
		String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");
		manualBuildingPermitMap.put("APN",parcelToSearch);

		//Step6: Adding a new Building Permit with the APN passed in the above steps
		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);

		//Step7: Opening the Home module & Clicking on In Pool Tab
		objBuildingPermitPage.searchModule(modules.HOME);
		//objPage.waitForElementToBeVisible(10, objWorkItemHomePage.inPoolTab);
		System.out.println("We are on home page now...................");
//		Thread.sleep(10000);
		objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
		Thread.sleep(10000);

        //Step8: "Import Review" Work Item generation validation after file is imported
        String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction Completed - Manual Entry" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
        String importReviewWorkItem = salesforceAPI.select(queryWorkItem).get("Name").get(0);
        ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
        System.out.println("We are on home page now..................." + importReviewWorkItem);
        
//        Step9: Accepting the work item & Going to Tab In Progress

        Thread.sleep(10000);
        objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
        Thread.sleep(10000);
        objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
        objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
        objBuildingPermitPage.scrollToBottom();
        Thread.sleep(10000);

    }
    
    
    /*
    @Test(description = "SMAB-T383,SMAB-T520,SMAB-T402,SMAB-T421,SMAB-T416: Creating manual entry for building permit without complition date", dataProvider = "RPAppraiser", dataProviderClass = com.apas.DataProviders.DataProviders.class, groups={"Smoke","Regression","BuildingPermit"}, alwaysRun = true)
   	public void BuildingPermit_ManualCreateNewBuildingPermitWithoutCompletionDate(String loginUser) throws Exception {

       	//waitForElementToBeVisible

   		//Fetching the Active Parcel
   		String query ="SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Active' and Primary_Situs__C !='' limit 1";
   		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
   		String parcelToSearch = response.get("Name").get(0);

   		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
   		objBuildingPermitPage.login(loginUser);

   		//Step2: Opening the Parcels module
   		objBuildingPermitPage.searchModule(modules.PARCELS);

   		//Step3: Search and Open the Parcel
   		objBuildingPermitPage.globalSearchRecords(parcelToSearch);
   		
   		//Step4: Opening the building permit module
   		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);

   		//Step5: Prepare a test data to create a new building permit
   		Map<String, String> manualBuildingPermitMap = objBuildingPermitPage.getBuildingPermitManualCreationTestDataWithoutComplitionDate();
   		String buildingPermitNumber = manualBuildingPermitMap.get("Building Permit Number");
   		manualBuildingPermitMap.put("APN",parcelToSearch);

   		//Step6: Adding a new Building Permit with the APN passed in the above steps
   		objBuildingPermitPage.addAndSaveManualBuildingPermit(manualBuildingPermitMap);
   		System.out.println("Building Pemrit Number Is :- " + buildingPermitNumber);

   		//Step7: Opening the Home module
   		objBuildingPermitPage.searchModule(modules.HOME);
   		System.out.println("We are on home page now...................");

   		Thread.sleep(10000);
   		objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
   		Thread.sleep(10000);

   		//	Step8: "Import Review" Work Item generation validation after file is imported
           String queryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction In Progress - Manual Entry" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";

           String importReviewWorkItem = salesforceAPI.select(queryWorkItem).get("Name").get(0);
           ReportLogger.INFO("Created Work Item : " + importReviewWorkItem);
           System.out.println("We are on home page now..................." + importReviewWorkItem);
           
//           Step9: Accepting the work item

           Thread.sleep(10000);
           objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
           Thread.sleep(10000);
           objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
           Thread.sleep(10000);
           
         //Step10: Opening the building permit module
      		objBuildingPermitPage.searchModule(modules.BUILDING_PERMITS);
      		
      	//Step10: Editing the existing Building Permit entering completion date

    		objBuildingPermitPage.displayRecords("All Manual Building Permits");
    		objBuildingPermitPage.globalSearchRecords(buildingPermitNumber);
    		objBuildingPermitPage.scrollToElement(objBuildingPermitPage.editPermitButton);
    		objWorkItemHomePage.Click(objBuildingPermitPage.editPermitButton);
    		Thread.sleep(10000);

    		//Step11: Prepare a test data to create a new building permit
       		Map<String, String> manualBuildingPermitMapOnlyCompletionDate = objBuildingPermitPage.getBuildingPermitManualCreationTestDataOnlyCompletionDate();
       		String buildingPermitNumberCompletionDate = manualBuildingPermitMapOnlyCompletionDate.get("Building Permit Number");
       		manualBuildingPermitMapOnlyCompletionDate.put("APN",parcelToSearch);
    		System.out.println(buildingPermitNumberCompletionDate);
       
    		Thread.sleep(10000);

    		//Step12: Adding a new Building Permit with the APN passed in the above steps

    		objBuildingPermitPage.enterManualEntryCompletionDateOnly(manualBuildingPermitMapOnlyCompletionDate);

       	//Step12: Opening the Home module
       		objBuildingPermitPage.searchModule(modules.HOME);
       		System.out.println("We are on home page now...................");
    
       	//Step 13: Clicking on In the pool tab
       		Thread.sleep(10000);
       		objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
       		Thread.sleep(10000);

        //Step14: "Import Review" Work Item generation validation after file is imported
               String editQueryWorkItem = "SELECT Id, Name FROM Work_Item__c where Request_Type__c like '%" + "Building Permit - Construction Completed - Manual Entry" + "%' AND AGE__C=0 ORDER By Name Desc LIMIT 1";
               String editImportReviewWorkItem = salesforceAPI.select(editQueryWorkItem).get("Name").get(0);
               ReportLogger.INFO("Created Work Item : " + editImportReviewWorkItem);
               System.out.println("We are on home page now..................." + editImportReviewWorkItem);
               
        //Step15: Accepting the work item
               objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);
               Thread.sleep(10000);
               objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
               Thread.sleep(10000);
               objWorkItemHomePage.acceptWorkItem(editImportReviewWorkItem);
               objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
               objBuildingPermitPage.scrollToBottom();
               Thread.sleep(10000);

       		
       }*/
}
