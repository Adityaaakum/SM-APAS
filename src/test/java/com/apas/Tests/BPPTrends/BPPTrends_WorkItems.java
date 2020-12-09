package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import com.apas.PageObjects.*;
import com.apas.Utils.SalesforceAPI;
import com.apas.config.users;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;

public class BPPTrends_WorkItems extends TestBase {

    RemoteWebDriver driver;
    Page objPage;
    BppTrendSetupPage objBppTrendSetupPage;
    BppTrendPage objBppTrendPage;
    WorkItemHomePage objWorkItemHomePage;
    EFileImportPage objEfileImportPage;
    ApasGenericPage objApasGenericPage;
    SalesforceAPI objSalesforceAPI = new SalesforceAPI();
    SoftAssertion softAssert = new SoftAssertion();

    String rollYear;

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();
        objApasGenericPage = new ApasGenericPage(driver);
        objPage = new Page(driver);
        objEfileImportPage = new EFileImportPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objBppTrendPage = new BppTrendPage(driver);
        objBppTrendSetupPage = new BppTrendSetupPage(driver);
        rollYear = "2021";
    }


    /**
     * This test case is to validate reminder work item creation and the work item flow for approved file
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1731,SMAB-T1732,SMAB-T2175,SMAB-T1944,SMAB-T1948: Verify auto generated Reminder WI, Approval of Imported BOE Index & Goods Factors, auto generated Review Import WI", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = false)
    public void BPPTrends_BOEIndexAndGoods_WorkItemImportAndApprove(String loginUser, boolean flagDeleteAndGenerateReminderWI) throws Exception {
        String sourceFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_VALID + "BOE Equipment Index Factors and Percent Good Factors 2021.xlsx";

        //Step1: Delete the existing data & WIs and Generate Reminder Work Items
        if(!flagDeleteAndGenerateReminderWI){
            //Delete the existing data from system before importing files
            objEfileImportPage.deleteImportedRecords("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYear);

            //Delete the existing WI from system before importing files
            String query = "select id from Work_Item__c where Reference__c = 'BOE - Index and Percent Good Factors'";
            objSalesforceAPI.delete("Work_Item__c", query);

            //Generate Reminder Work Items
            objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_EFILE);
        }

        //Step2: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Step3: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step4: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Import - BOE Index and Percent Good Factors")).count();
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - BOE Index and Percent Good Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Import - BOE Index and Percent Good Factors", "SMAB-T1731,SMAB-T1944: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1731: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1731: Imported work item count validation");

        //Step5: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step6: Upload BOE Index and Goods Factor File
        objEfileImportPage.uploadFile(sourceFile);

        //Step7: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step8: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1731: Validation that import work item moved to Completed status after file is imported");

        //Step9: Verifying "Review Import" Work Item generation after BOE Index and Goods Factor File is 'Imported'
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int reviewImportWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Review Import - BOE Index and Percent Good Factors")).count();
        int reviewImportRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Review Import - BOE Index and Percent Good Factors");
        String reviewImportWorkItem = InPoolWorkItems.get("Work Item Number").get(reviewImportRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(reviewImportRowNumber), "BPP Trends - Review Import - BOE Index and Percent Good Factors", "SMAB-T2175,SMAB-T1948: Review Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(reviewImportRowNumber), "BPP Admin", "SMAB-T2175: Work Pool Name Validation for Review Import Work Item");
        softAssert.assertEquals(reviewImportWorkItemCount, 1, "SMAB-T2175: Imported work item count validation");

        //Step10: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(reviewImportWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(reviewImportWorkItem);
        parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step11: Approving the Imported File
        objEfileImportPage.approveImportedFile();

        //Stpe12: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step13: "Import" Reminder Work Item generation validation
        completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1732: Validation that Review Import work item moved to Completed status after imported file is approved");

        //Step14: Log out from the application
        objBppTrendSetupPage.logout();
    }

    /**
     * This test case is to validate reminder work item creation and the work item flow for reverted file
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1731,SMAB-T1733,SMAB-T2175: Verify auto generated Reminder WI, Revert Imported BOE Index & Goods Factors, auto generated Import WI again upon revert", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = true)
    public void BPPTrends_BOEIndexAndGoods_WorkItemImportAndRevert(String loginUser) throws Exception {
        String sourceFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_INDEX_FACTORS_VALID + "BOE Equipment Index Factors and Percent Good Factors 2021.xlsx";

        //Step1: Delete the existing data from system before importing files
        objEfileImportPage.deleteImportedRecords("BPP Trend Factors", "BOE - Index and Percent Good Factors", rollYear);

        //Step2: Delete the existing WI from system before importing files
        String query = "select id from Work_Item__c where Reference__c = 'BOE - Index and Percent Good Factors'";
        objSalesforceAPI.delete("Work_Item__c", query);

        //Step3: Generate Reminder Work Items
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_EFILE);

        //Step4: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Stpe5: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step6: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Import - BOE Index and Percent Good Factors")).count();
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - BOE Index and Percent Good Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Import - BOE Index and Percent Good Factors", "SMAB-T1731: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1731: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1731: Imported work item count validation");

        //Step7: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step8: Upload BOE Index and Goods Factor File
        objEfileImportPage.uploadFile(sourceFile);

        //Stpe9: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step10: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1731: Validation that import work item moved to Completed status after file is imported");

        //Step11: Verifying "Review Import" Work Item generation after BOE Index and Goods Factor File is 'Imported'
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int reviewImportWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Review Import - BOE Index and Percent Good Factors")).count();
        int reviewImportRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Review Import - BOE Index and Percent Good Factors");
        String reviewImportWorkItem = InPoolWorkItems.get("Work Item Number").get(reviewImportRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(reviewImportRowNumber), "BPP Trends - Review Import - BOE Index and Percent Good Factors", "SMAB-T2175: Review Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(reviewImportRowNumber), "BPP Admin", "SMAB-T2175: Work Pool Name Validation for Review Import Work Item");
        softAssert.assertEquals(reviewImportWorkItemCount, 1, "SMAB-T2175: Imported work item count validation");

        //Step12: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(reviewImportWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(reviewImportWorkItem);
        parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step12: Approving the Imported File
        objEfileImportPage.revertImportedFile();

        //Stpe14: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step15: "Review Import" Work Item validation
        completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1733: Validation that Review Import work item moved to Completed status after imported file is reverted");

        //Step16: "Import" Reminder Work Item generation validation
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Import - BOE Index and Percent Good Factors")).count();
        importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - BOE Index and Percent Good Factors");
        importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Import - BOE Index and Percent Good Factors", "SMAB-T1733: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1733: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1733: Imported work item count validation");
    }

    /**
     * This test case is to validate reminder work item creation and the 'BOE Valuation Factors' work item flow for approved file
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1738,SMAB-T1739,SMAB-T1769,SMAB-T1945,SMAB-T1949: Verify auto generated Reminder WI, Approval of Imported BOE Valuation Factors, auto generated Review Import WI", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = false)
    public void BPPTrends_BOEValuation_WorkItemImportAndApprove(String loginUser, boolean flagDeleteAndGenerateReminderWI) throws Exception {
        String sourceFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VAL_FACTORS_VALID + "BOE Valuation Factors 2021.xlsx";

        //Step1: Delete the existing data & WIs and Generate Reminder Work Items
        if(!flagDeleteAndGenerateReminderWI) {
            //Delete the existing data from system before importing files
            objEfileImportPage.deleteImportedRecords("BPP Trend Factors", "BOE - Valuation Factors", rollYear);

            //Delete the existing WI from system before importing files
            String query = "select id from Work_Item__c where Reference__c = 'BOE Valuation Factors'";
            objSalesforceAPI.delete("Work_Item__c", query);

            //Generate Reminder WIs
            objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_EFILE);
        }
        //Step2: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Step3: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step4: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Import - BOE Valuation Factors")).count();
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - BOE Valuation Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Import - BOE Valuation Factors", "SMAB-T1738,SMAB-T1945: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1738: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1738: Imported work item count validation");

        //Step5: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step6: Upload BOE Valuation Factors File
        objEfileImportPage.uploadFile(sourceFile);

        //Step7: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step8: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1769: Validation that import work item moved to Completed status after file is imported");

        //Step9: Verifying "Review Import" Work Item generation after BOE Valuation Factors File is 'Imported'
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int reviewImportWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Review Import - BOE Valuation Factors")).count();
        int reviewImportRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Review Import - BOE Valuation Factors");
        String reviewImportWorkItem = InPoolWorkItems.get("Work Item Number").get(reviewImportRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(reviewImportRowNumber), "BPP Trends - Review Import - BOE Valuation Factors", "SMAB-T1769,SMAB-T1949: Review Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(reviewImportRowNumber), "BPP Admin", "SMAB-T1769: Work Pool Name Validation for Review Import Work Item");
        softAssert.assertEquals(reviewImportWorkItemCount, 1, "SMAB-T1769: Imported work item count validation");

        //Step10: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(reviewImportWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(reviewImportWorkItem);
        parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step11: Approving the Imported File
        objEfileImportPage.approveImportedFile();

        //Stpe12: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step13: "Import" Reminder Work Item generation validation
        completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1739: Validation that Review Import work item moved to Completed status after imported file is approved");

        //Step14: Log out from the application
        objBppTrendSetupPage.logout();
    }

    /**
     * This test case is to validate reminder work item creation and the work item flow for reverted file
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1738,SMAB-T1740,SMAB-T1769: Verify auto generated Reminder WI, Revert Imported BOE Valuation Factors, auto generated Import WI again upon revert", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = true)
    public void BPPTrends_BOEValuation_WorkItemImportAndRevert(String loginUser) throws Exception {
        String sourceFile = System.getProperty("user.dir") + testdata.BPP_TREND_BOE_VAL_FACTORS_VALID + "BOE Valuation Factors 2021.xlsx";

        //Step1: Delete the existing data from system before importing files
        objEfileImportPage.deleteImportedRecords("BPP Trend Factors", "BOE - Valuation Factors", rollYear);

        //Step2: Delete the existing WI from system before importing files
        String query = "select id from Work_Item__c where Reference__c = 'BOE Valuation Factors'";
        objSalesforceAPI.delete("Work_Item__c", query);

        //Step3: Generate Reminder Work Items
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_EFILE);

        //Step4: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Stpe5: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step6: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Import - BOE Valuation Factors")).count();
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - BOE Valuation Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Import - BOE Valuation Factors", "SMAB-T1738: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1738: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1738: Imported work item count validation");

        //Step7: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step8: Upload BOE Valuation Factor File
        objEfileImportPage.uploadFile(sourceFile);

        //Stpe9: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step10: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1769: Validation that import work item moved to Completed status after file is imported");

        //Step11: Verifying "Review Import" Work Item generation after BOE Valuation Factors File is 'Imported'
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int reviewImportWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Review Import - BOE Valuation Factors")).count();
        int reviewImportRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Review Import - BOE Valuation Factors");
        String reviewImportWorkItem = InPoolWorkItems.get("Work Item Number").get(reviewImportRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(reviewImportRowNumber), "BPP Trends - Review Import - BOE Valuation Factors", "SMAB-T1769: Review Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(reviewImportRowNumber), "BPP Admin", "SMAB-T1769: Work Pool Name Validation for Review Import Work Item");
        softAssert.assertEquals(reviewImportWorkItemCount, 1, "SMAB-T1769: Imported work item count validation");

        //Step12: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(reviewImportWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(reviewImportWorkItem);
        parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step13: Approving the Imported File
        objEfileImportPage.revertImportedFile();

        //Stpe14: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step15: "Import" Reminder Work Item generation validation
        completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1740: Validation that Review Import work item moved to Completed status after imported file is approved");

        //Step16: "Import" Reminder Work Item generation validation
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Import - BOE Valuation Factors")).count();
        importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - BOE Valuation Factors");
        importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Import - BOE Valuation Factors", "SMAB-T1740: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1740: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1740: Imported work item count validation");
    }

    /**
     * This test case is to validate reminder work item creation and the 'CAA Valuation Factors' work item flow for approved file
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1741,SMAB-T1742,SMAB-T1776,SMAB-T1946,SMAB-T1950: Verify auto generated Reminder WI, Approval of Imported CAA Valuation Factors, auto generated Review Import WI", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = false)
    public void BPPTrends_CAAValuation_WorkItemImportAndApprove(String loginUser, boolean flagDeleteAndGenerateReminderWI) throws Exception {
        String sourceFile = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VAL_FACTORS_VALID + "CAA Valuation Factors 2021.xlsx";

        //Step1: Delete the existing data & WIs and Generate Reminder Work Items
        if(!flagDeleteAndGenerateReminderWI) {
            //Step1: Delete the existing data from system before importing files
            objEfileImportPage.deleteImportedRecords("BPP Trend Factors", "CAA - Valuation Factors", rollYear);

            //Step2: Delete the existing WI from system before importing files
            String query = "select id from Work_Item__c where Reference__c = 'CAA Valuation Factors'";
            objSalesforceAPI.delete("Work_Item__c", query);

            //Generate Reminder Work Items
            objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_EFILE);
        }
        //Step2: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Step3: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step4: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Import - CAA Valuation Factors")).count();
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - CAA Valuation Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Import - CAA Valuation Factors", "SMAB-T1741,SMAB-T1946: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1741: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1741: Imported work item count validation");

        //Step5: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step6: Upload CAA Valuation Factors File
        objEfileImportPage.uploadFile(sourceFile);

        //Step7: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step8: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1776: Validation that import work item moved to Completed status after file is imported");

        //Step9: Verifying "Review Import" Work Item generation after CAA Valuation Factors File is 'Imported'
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int reviewImportWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Review Import - CAA Valuation Factors")).count();
        int reviewImportRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Review Import - CAA Valuation Factors");
        String reviewImportWorkItem = InPoolWorkItems.get("Work Item Number").get(reviewImportRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(reviewImportRowNumber), "BPP Trends - Review Import - CAA Valuation Factors", "SMAB-T1776,SMAB-T1950: Review Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(reviewImportRowNumber), "BPP Admin", "SMAB-T1776: Work Pool Name Validation for Review Import Work Item");
        softAssert.assertEquals(reviewImportWorkItemCount, 1, "SMAB-T1776: Imported work item count validation");

        //Step10: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(reviewImportWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(reviewImportWorkItem);
        parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step11: Approving the Imported File
        objEfileImportPage.approveImportedFile();

        //Stpe12: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step13: "Import" Reminder Work Item generation validation
        completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1742: Validation that Review Import work item moved to Completed status after imported file is approved");

        //Step14: Log out from the application
        objBppTrendSetupPage.logout();
    }

    /**
     * This test case is to validate reminder work item creation and the work item flow for reverted file
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1741,SMAB-T1743,SMAB-T1776: Verify auto generated Reminder WI, Revert Imported CAA Valuation Factors, auto generated Import WI again upon revert", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = true)
    public void BPPTrends_CAAValuation_WorkItemImportAndRevert(String loginUser) throws Exception {
        String sourceFile = System.getProperty("user.dir") + testdata.BPP_TREND_CAA_VAL_FACTORS_VALID + "CAA Valuation Factors 2021.xlsx";

        //Step1: Delete the existing data from system before importing files
        objEfileImportPage.deleteImportedRecords("BPP Trend Factors", "CAA - Valuation Factors", rollYear);

        //Step2: Delete the existing WI from system before importing files
        String query = "select id from Work_Item__c where Reference__c = 'CAA Valuation Factors'";
        objSalesforceAPI.delete("Work_Item__c", query);

        //Step3: Generate Reminder Work Items
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_EFILE);

        //Step4: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Stpe5: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step6: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Import - CAA Valuation Factors")).count();
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - CAA Valuation Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Import - CAA Valuation Factors", "SMAB-T1741: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1741: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1741: Imported work item count validation");

        //Step7: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step8: Upload CAA Valuation Factor File
        objEfileImportPage.uploadFile(sourceFile);

        //Step9: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step10: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1776: Validation that import work item moved to Completed status after file is imported");

        //Step11: Verifying "Review Import" Work Item generation after CAA Valuation Factors File is 'Imported'
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int reviewImportWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Review Import - CAA Valuation Factors")).count();
        int reviewImportRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Review Import - CAA Valuation Factors");
        String reviewImportWorkItem = InPoolWorkItems.get("Work Item Number").get(reviewImportRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(reviewImportRowNumber), "BPP Trends - Review Import - CAA Valuation Factors", "SMAB-T1776: Review Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(reviewImportRowNumber), "BPP Admin", "SMAB-T1776: Work Pool Name Validation for Review Import Work Item");
        softAssert.assertEquals(reviewImportWorkItemCount, 1, "SMAB-T1776: Imported work item count validation");

        //Step12: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(reviewImportWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(reviewImportWorkItem);
        parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step13: Approving the Imported File
        objEfileImportPage.revertImportedFile();

        //Stpe14: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);
        driver.navigate().refresh();
        Thread.sleep(5000);

        //Step15: "Import" Reminder Work Item generation validation
        completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importWorkItem), "SMAB-T1742: Validation that Review Import work item moved to Completed status after imported file is approved");

        //Step16: "Import" Reminder Work Item generation validation
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Import - CAA Valuation Factors")).count();
        importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - CAA Valuation Factors");
        importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Import - CAA Valuation Factors", "SMAB-T1742: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1742: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1742: Imported work item count validation");
    }

    /**
     * This test case is to validate Perform Calculations work item creation after BOE Index & Goods Factor is approved
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1736,SMAB-T1947: Verify auto generated Reminder WI, Approval of Imported BOE Index & Goods Factors, auto generated Review Import WI", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = true)
    public void BPPTrends_PerformCalculations_WorkItemGeneration(String loginUser) throws Exception {
        //Step1: Delete the existing WI from system before importing files
        String query = "select id from Work_Item__c where Reference__c = 'BPP Composite Factors'";
        objSalesforceAPI.delete("Work_Item__c", query);

        //Step2: Validate reminder work item creation and the work item flow for approved file
        BPPTrends_BOEIndexAndGoods_WorkItemImportAndApprove(loginUser,false);

        //Step3: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Step4: "Perform Calculations" Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Perform Calculations - BPP Composite Factors")).count();
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Perform Calculations - BPP Composite Factors");

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importRowNumber), "BPP Trends - Perform Calculations - BPP Composite Factors", "SMAB-T1736,SMAB-T1947: Import Work Item Name validation");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importRowNumber), "BPP Admin", "SMAB-T1736: Work Pool Name Validation for Import Work Item");
        softAssert.assertEquals(importWorkItemCount, 1, "SMAB-T1736: Imported work item count validation");

    }

    /**
     * This test case is to validate user is not able to submit calculations if any of the 'Import' WI is not 'Completed'
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1761: Verify auto generated Reminder WI, Approval of Imported BOE Index & Goods Factors, auto generated Review Import WI", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = true)
    public void BPPTrends_verifySubmitAllFactorForApprovalBtnNotVisible_whenImportWINotCompleted(String loginUser) throws Exception {
        //Step1: Delete the existing 'Annual Factor Settings' WIs before generating
        String query = "select id from Work_Item__c where Reference__c = 'Annual Factor Settings' OR Reference__c = 'BPP Composite Factors'";
        objSalesforceAPI.delete("Work_Item__c", query);

        //Step2: Delete the existing 'Import' WIs before generating
        query = "select id from Work_Item__c where Sub_Type__c = 'Import'";
        objSalesforceAPI.delete("Work_Item__c", query);

        //Step3: Generate Annual Factor Settings Reminder Work Items
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_ANNUAL_FACTORS);

        //Step4: Validate reminder work item creation and the work item flow for approved file
        BPPTrends_BOEIndexAndGoods_WorkItemImportAndApprove(loginUser,false);

        //Step5: Update 'Annual Factor Status' & WI status to Completed
        query = "select id from Work_Item__c where Reference__c = 'Annual Factor Settings' OR Reference__c = 'BOE Valuation Factors'";
        objSalesforceAPI.update("Work_Item__c", query, "Status__c", "In Progress");
        objSalesforceAPI.update("Work_Item__c", query, "Status__c", "Completed");

        query = "SELECT id FROM BPP_Trend_Roll_Year__c WHERE Roll_Year__c = '" + rollYear + "'";
        objSalesforceAPI.update("BPP_Trend_Roll_Year__c", query, "Annual_Factor_Status__c", "Reviewed by Admin");

        //Step6: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Step7: "Perform Calculations" Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Perform Calculations - BPP Composite Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        //Step8: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step9: Trigger Calculations by clicking 'Calculate All' Button
        objPage.Click(objBppTrendPage.calculateAllBtn);
        objPage.waitForElementToDisappear(objBppTrendPage.xpathSpinner, 50);
        objPage.waitUntilElementIsPresent(objBppTrendPage.xpathTableMessage, 10);
        softAssert.assertEquals(objPage.getElementText(objBppTrendPage.tableMessage), "Yet to be submitted for approval", "SMAB-T1761: Message displayed above the table after Calculation is completed");

        //Step10: Validating unavailability of Submit All Factors For Approval button
        softAssert.assertTrue(Objects.isNull(objPage.locateElement(objBppTrendPage.xpathSubmitAllFactorsForApprovalBtn, 20)), "SMAB-T1761: Submit All Factors For Approval button is not visible");
    }

    /**
     * This test case is to validate user is not able to submit calculations if any of the 'Import' WI is not 'Completed'
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T2196: Verify auto generated Reminder WI, Approval of Imported BOE Index & Goods Factors, auto generated Review Import WI", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = true)
    public void BPPTrends_verifyErrorMsg_whenAnnualSettingsWINotCompleted(String loginUser) throws Exception {

        //Step1: Delete the existing WIs before generating
        String query = "select id from Work_Item__c where Reference__c = 'Annual Factor Settings' OR Reference__c = 'BPP Composite Factors'";
        objSalesforceAPI.delete("Work_Item__c", query);

        //Step2: Delete the existing WIs before generating
        query = "select id from Work_Item__c where Sub_Type__c = 'Import'";
        objSalesforceAPI.delete("Work_Item__c", query);

        //Step3: Generate Reminder Work Items
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_ANNUAL_FACTORS);

        //Step4: Validate reminder work item creation and the work item flow for approved file
        BPPTrends_BOEIndexAndGoods_WorkItemImportAndApprove(loginUser,false);

        //Step5: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step6: Update WI status to Completed
        query = "select id from Work_Item__c where Reference__c = 'BOE Valuation Factors' OR Reference__c = 'CAA Valuation Factors'";
        objSalesforceAPI.update("Work_Item__c", query, "Status__c", "In Progress");
        objSalesforceAPI.update("Work_Item__c", query, "Status__c", "Completed");

        //Step7: "Perform Calculations" Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Perform Calculations - BPP Composite Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        //Step8: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step9: Trigger Calculations by clicking 'Calculate All' Button
        objPage.Click(objBppTrendPage.calculateAllBtn);

        //Step10: Validating Error Message when 'Annual Settings' WI is not Completed and calculations are triggered
        String actualErrorMessage = objBppTrendPage.waitForErrorPopUpMsgOnCalculateClick(60);
        softAssert.assertContains(actualErrorMessage, "BPP Annual Factors is not yet completed/reviewed by admin for selected Roll Year", "SMAB-T2196: Verify Error Message when 'Annual Settings' WI is not Completed and calculations are triggered");

    }
    
    /** This test case is to validate user is not able to submit calculations if any of the 'Import' WI is not 'Completed'
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SSMAB-T1750,SMAB-T1737: Verify auto generated Reminder WI, Approval of Imported BOE Index & Goods Factors, auto generated Review Import WI", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "Work_Item_BPP"}, alwaysRun = true, enabled = true)
    public void BPPTrends_verifyPerformCalculationWI_SubmittedForApprovalAndApproval_Status(String loginUser) throws Exception {

        //Step1: Delete the existing WIs before generating
        String query = "select id from Work_Item__c where Reference__c = 'Annual Factor Settings' OR Reference__c = 'BPP Composite Factors'";
        objSalesforceAPI.delete("Work_Item__c",query);

        //Step2: Delete the existing WIs before generating
        query = "select id from Work_Item__c where Sub_Type__c = 'Import'";
        objSalesforceAPI.delete("Work_Item__c",query);

        //Step3: Generate 'Annual Factor Settings' Reminder Work Items
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_ANNUAL_FACTORS);

        //Step10: Update 'Annual Factor Status' & WI status to Completed
        query = "select id from Work_Item__c where Reference__c = 'Annual Factor Settings'";
        objSalesforceAPI.update("Work_Item__c", query, "Status__c", "In Progress");
        objSalesforceAPI.update("Work_Item__c", query, "Status__c", "Completed");

        query = "SELECT id FROM BPP_Trend_Roll_Year__c WHERE Roll_Year__c = '" +rollYear+ "'";
        objSalesforceAPI.update("BPP_Trend_Roll_Year__c", query, "Annual_Factor_Status__c", "Reviewed by Admin");

        //Step4: Validate reminder work item creation and the work item flow for approved file
        BPPTrends_BOEIndexAndGoods_WorkItemImportAndApprove(loginUser,false);
        BPPTrends_BOEValuation_WorkItemImportAndApprove(loginUser,true);
        BPPTrends_CAAValuation_WorkItemImportAndApprove(loginUser,true);
        Thread.sleep(15000);

        //Step4: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Step6: "Perform Calculations" Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Perform Calculations - BPP Composite Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        //Step7: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step8: Trigger Calculations by clicking 'Calculate All' Button
        objPage.Click(objBppTrendPage.calculateAllBtn);
        objPage.waitForElementToDisappear(objBppTrendPage.xpathSpinner, 50);

        //Step12: Submit Calculations for Approval
        objPage.Click(objBppTrendPage.submitAllFactorForApprovalButton);
        objPage.waitForElementToDisappear(objBppTrendPage.xpathSpinner, 50);

        //Step13: Verify Status of WI 'Perform Calculations' is 'Submitted for Approval'
        query = "select Status__c from Work_Item__c where Name = '"+ importWorkItem +"'";
        HashMap<String, ArrayList<String>> workItemData = new SalesforceAPI().select(query);
        String actualWIStatus = workItemData.get("Status__c").get(0);
        softAssert.assertEquals(actualWIStatus, "Submitted for Approval", "SMAB-T1737: Verify status of WI : 'Perform Calculations' is 'Submitted for Approval'");
        
        //Step14: Log out from the application and log in as BPP Principal
        objBppTrendSetupPage.logout();
        Thread.sleep(15000);

        objBppTrendSetupPage.login(users.PRINCIPAL_USER);
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step15: Navigate to 'Needs My Approval' tab and
        objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);

        //Step16: Opening the link under 'Action' Column
        objWorkItemHomePage.openActionLink(importWorkItem);
        parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step17: Approve all factors
        objPage.Click(objBppTrendPage.approveAllButton);
        objPage.waitForElementToDisappear(objBppTrendPage.xpathSpinner, 50);

        //Step18: Verify Status of WI 'Perform Calculations' is 'Completed'
        query = "select Status__c from Work_Item__c where Name = '"+ importWorkItem +"'";
        workItemData = new SalesforceAPI().select(query);
        actualWIStatus = workItemData.get("Status__c").get(0);
        softAssert.assertEquals(actualWIStatus, "Completed", "SMAB-T1750: Verify status of WI : 'Perform Calculations' is 'Completed'");
    }
    
    /** This test case is to validate user is not able to submit calculations if any of the 'Import' WI is not 'Completed'
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and BPP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T2195: Verify user is able to submit 'Perform Calculations' WI for Approval", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BPP"}, alwaysRun = true, enabled = true)
    public void BPPTrends_verifyPerformCalculationWI_ReturnedStatus(String loginUser) throws Exception {

        //Step1: Delete the existing WIs before generating
        String query = "select id from Work_Item__c where Reference__c = 'Annual Factor Settings' OR Reference__c = 'BPP Composite Factors'";
        objSalesforceAPI.delete("Work_Item__c",query);

        //Step2: Delete the existing WIs before generating
        query = "select id from Work_Item__c where Sub_Type__c = 'Import'";
        objSalesforceAPI.delete("Work_Item__c",query);

        //Step3: Generate 'Annual Factor Settings' Reminder Work Items
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_ANNUAL_FACTORS);

        //Step10: Update 'Annual Factor Status' & WI status to Completed
        query = "select id from Work_Item__c where Reference__c = 'Annual Factor Settings'";
        objSalesforceAPI.update("Work_Item__c", query, "Status__c", "In Progress");
        objSalesforceAPI.update("Work_Item__c", query, "Status__c", "Completed");

        query = "SELECT id FROM BPP_Trend_Roll_Year__c WHERE Roll_Year__c = '" +rollYear+ "'";
        objSalesforceAPI.update("BPP_Trend_Roll_Year__c", query, "Annual_Factor_Status__c", "Reviewed by Admin");

        //Step4: Validate reminder work item creation and the work item flow for approved file
        BPPTrends_BOEIndexAndGoods_WorkItemImportAndApprove(loginUser,false);
        BPPTrends_BOEValuation_WorkItemImportAndApprove(loginUser,true);
        BPPTrends_CAAValuation_WorkItemImportAndApprove(loginUser,true);

        //Step4: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objBppTrendSetupPage.login(loginUser);

        //Step5: "Perform Calculations" Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Perform Calculations - BPP Composite Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        //Step6: Accepting the work item and opening the link under 'Action' Column
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink(importWorkItem);
        String parentwindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentwindow);

        //Step7: Trigger Calculations by clicking 'Calculate All' Button
        objPage.Click(objBppTrendPage.calculateAllBtn);
        objPage.waitForElementToDisappear(objBppTrendPage.xpathSpinner, 50);

        //Step8: Submit Calculations for Approval
        objPage.Click(objBppTrendPage.submitAllFactorForApprovalButton);
        objPage.waitForElementToDisappear(objBppTrendPage.xpathSpinner, 50);

        //Step9: Verify Status of WI 'Perform Calculations' is 'Submitted for Approval'
        query = "select Status__c from Work_Item__c where Name = '"+ importWorkItem +"'";
        HashMap<String, ArrayList<String>> workItemData = new SalesforceAPI().select(query);
        String actualWIStatus = workItemData.get("Status__c").get(0);
        softAssert.assertEquals(actualWIStatus, "Submitted for Approval", "SMAB-T2195: Verify status of WI : 'Perform Calculations' is 'Submitted for Approval'");

        //Step10: Log out from the application and log in as BPP Principal
        objBppTrendSetupPage.logout();
        Thread.sleep(15000);

        objBppTrendSetupPage.login(users.PRINCIPAL_USER);
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step11: Navigate to 'Needs My Approval' tab and
        objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);

        //Step12: Return the Work Item
        objWorkItemHomePage.returntWorkItem(importWorkItem,"Test WI Rejection");

        //Step13: Verify Status of WI 'Perform Calculations' is 'Completed'
        query = "select Status__c from Work_Item__c where Name = '"+ importWorkItem +"'";
        workItemData = new SalesforceAPI().select(query);
        actualWIStatus = workItemData.get("Status__c").get(0);
        softAssert.assertEquals(actualWIStatus, "Returned", "SMAB-T2195: Verify status of WI : 'Perform Calculations' is 'Returned'");

        //Step14: Log out from the application and log in again as BPP Admin
        objBppTrendSetupPage.logout();
        Thread.sleep(15000);

        objBppTrendSetupPage.login(loginUser);
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Ste15: Verify WI is present in 'In Progress' tab
        HashMap<String, ArrayList<String>> InProgressWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_PROGRESS);
        int workItemRowNumber = InProgressWorkItems.get("Request Type").indexOf("BPP Trends - Perform Calculations - BPP Composite Factors");
        String workItemName = InProgressWorkItems.get("Work Item Number").get(workItemRowNumber);
        softAssert.assertEquals(workItemName, "importWorkItem", "SMAB-T2195: Verify WI returned to user is found in 'In Progress' tab");

    }


    /**
     * DESCRIPTION: Validation of BPP Trend Annual Factors Reminder Work Item workflow
     */
    @Test(description = "SMAB-T1729,SMAB-T1730,SMAB-T1734,SMAB-T1735,SMAB-T1943,SMAB-T2168,SMAB-T2169,SMAB-T2179: Validation of BPP Trend Annual Factors Reminder Work Item workflow", groups={"smoke","regression","Work_Item_BPP"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, alwaysRun = true)
    public void BppTrend_AnnualFactors_ReminderWorkItemWorkFlow(String loginUser) throws Exception {
        String rollYear = "2021";

        //Code to delete current roll year BPP Trend Setup, BPP Settings and Composite Factor Records
        objBppTrendPage.removeExistingBppSettingEntry(rollYear);
        objBppTrendPage.removeExistingBppFactorSettingEntry(rollYear);
        String queryToDeleteBPPTrendSetUp = "SELECT Id FROM BPP_Trend_Roll_Year__c where Name = '" + rollYear + " - BPP Trend Setup'";
        objSalesforceAPI.delete("BPP_Trend_Roll_Year__c",queryToDeleteBPPTrendSetUp);

        //Delete the existing WI from system before generating the work item
        String queryToDeleteAnnualFactorSettingsWI = "select id from Work_Item__c where Reference__c = 'Annual Factor Settings'";
        objSalesforceAPI.delete("Work_Item__c", queryToDeleteAnnualFactorSettingsWI);

        //Generating the Reminder Work Items
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_ANNUAL_FACTORS);

        //Validation for BPP Trend Setup, BPP Settings and Composite Factor Records after generating the reminder work item
        HashMap<String, ArrayList<String>> BPPSettings = objSalesforceAPI.select("SELECT Id FROM BPP_Setting__c where BPP_Trend_Roll_Year_Parent__c = '"+ rollYear + "'");
        softAssert.assertEquals(BPPSettings.get("Id").size(),1,"SMAB-T2169: Validation for cloning of prior year BPP Settings on reminder work item generation");

        HashMap<String, ArrayList<String>> BPPFactorSettings = objSalesforceAPI.select("SELECT Name,Property_Type__c FROM BPP_Composite_Factors_Setting__c Where BPP_Trend_Roll_Year_Parent__c = '"+ rollYear + "'");
        softAssert.assertEquals(BPPFactorSettings.get("Name").size(),4,"SMAB-T2169: Composite Factor Count Validation on reminder work item generation");
        ReportLogger.INFO("List of Composite Factor : " + BPPFactorSettings.get("Property_Type__c"));
        softAssert.assertTrue(BPPFactorSettings.get("Property_Type__c").contains("Commercial"),"SMAB-T2169: Validation for cloning of prior year Commercial Composite Factor on reminder work item generation");
        softAssert.assertTrue(BPPFactorSettings.get("Property_Type__c").contains("Industrial"),"SMAB-T2169: Validation for cloning of prior year Industrial Composite Factor on reminder work item generation");
        softAssert.assertTrue(BPPFactorSettings.get("Property_Type__c").contains("Construction"),"SMAB-T2169: Validation for cloning of prior year Construction Composite Factor on reminder work item generation");
        softAssert.assertTrue(BPPFactorSettings.get("Property_Type__c").contains("Agricultural"),"SMAB-T2169: Validation for cloning of prior year Agriculture Composite Factor on reminder work item generation");

        //Step1: Login to the APAS application using the given user
        objBppTrendSetupPage.login(loginUser);

        //Step2: Validation for the cloned annual factor from the prior year
        objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
        objBppTrendSetupPage.displayRecords("All");
        softAssert.assertTrue(objBppTrendSetupPage.getGridDataInHashMap().get("Name").contains("2021 - BPP Trend Setup"),"SMAB-T2168: Validation for cloning of prior year BPP Trend Setup record on reminder work item generation");

        //Stpe3: Open the Work Item Home Page
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step4: "BPP Trend Annual Factor" Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int annualFactorSettingsWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("BPP Trends - Update and Validate - Annual Factor Settings")).count();
        int annualFactorSettingsWorkItemRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Update and Validate - Annual Factor Settings");
        String annualFactorSettingsWorkItemWorkItem = InPoolWorkItems.get("Work Item Number").get(annualFactorSettingsWorkItemRowNumber);
        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(annualFactorSettingsWorkItemRowNumber), "BPP Trends - Update and Validate - Annual Factor Settings" , "SMAB-T1729,SMAB-T1943: BPP Trend Annual Factor Settings work item name validation in the imported review work item");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(annualFactorSettingsWorkItemRowNumber), "BPP Admin", "SMAB-T1729: BPP Trend Annual Factor pool name validation");
        softAssert.assertEquals(annualFactorSettingsWorkItemCount, 1, "SMAB-T1729: BPP Trend Annual Factor Settings work item count validation");

        //Step5: Accepting the work item
        objWorkItemHomePage.acceptWorkItem(annualFactorSettingsWorkItemWorkItem);

        //Step6: Status validation after accepting the work item
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objPage.scrollToBottom();
        objWorkItemHomePage.openWorkItem(annualFactorSettingsWorkItemWorkItem);
        objPage.Click(objWorkItemHomePage.detailsTab);
        Thread.sleep(2000);
        softAssert.assertEquals(objBppTrendSetupPage.getFieldValueFromAPAS("Status"), "In Progress", "SMAB-T1729: Validation of annual factor setting work item status after accepting the work item");

        //Step7: Steps to edit and save BPP Trend Setup Record
        objBppTrendSetupPage.searchModule(modules.HOME);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objPage.scrollToBottom();
        String parentWindow = driver.getWindowHandle();
        objWorkItemHomePage.openActionLink(annualFactorSettingsWorkItemWorkItem);

        objPage.switchToNewWindow(parentWindow);
        Thread.sleep(1000);

        //Step8: Edit the settings
        objPage.Click(objBppTrendSetupPage.dropDownIconBppSetting);
        objPage.Click(objBppTrendSetupPage.editLinkUnderShowMore);
        Thread.sleep(2000);
        objPage.enter("Maximum Equipment index Factor","124.4");
        String successMessage = objBppTrendSetupPage.saveRecord();
        softAssert.assertEquals(successMessage,"success\nBPP Setting was saved.\nClose","SMAB-T1730: Validation that user is able to edit BPP Settings and Factors through Annual Factor Settings work item");

        //Change the status to "Reviewed By Admin"
        softAssert.assertEquals(objBppTrendSetupPage.getFieldValueFromAPAS("Annual Factor Status"),"To be Reviewed by Admin","SMAB-T1734 : Validation that new field Annual Factor Status is visible on UI");
        objBppTrendSetupPage.editAndSelectFieldData("Annual Factor Status","Reviewed by Admin");

        driver.switchTo().window(parentWindow);

        // //Step19: Open Home Page
        driver.navigate().refresh();
        objBppTrendSetupPage.searchModule(modules.HOME);

        //Step20: Completed "Annual Factor Settings" work item validation
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(annualFactorSettingsWorkItemWorkItem), "SMAB-T1730,SMAB-T1735: Validation that Annual Factor Settings work item moved to Completed status after Annual Factor Status is saved to Reviewed By Admin");

        //Update the settings even after closing the work item
        objBppTrendSetupPage.searchModule(modules.BPP_TRENDS_SETUP);
        objBppTrendSetupPage.displayRecords("All");
        objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);

        objPage.Click(objBppTrendSetupPage.dropDownIconBppSetting);
        objPage.Click(objBppTrendSetupPage.editLinkUnderShowMore);
        Thread.sleep(2000);
        objPage.enter("Maximum Equipment index Factor","124.2");
        successMessage = objBppTrendSetupPage.saveRecord();
        softAssert.assertEquals(successMessage,"success\nBPP Setting was saved.\nClose","SMAB-T1730: Validation that user is able to edit BPP Settings and Factors through Annual Factor Settings work item");

        //Validation for status even after editing the work BPP Trend set up for completed work item
        driver.navigate().refresh();
        objBppTrendSetupPage.searchModule(modules.HOME);
        objWorkItemHomePage.Click(objWorkItemHomePage.completedTab);
        objWorkItemHomePage.openWorkItem(annualFactorSettingsWorkItemWorkItem);
        objPage.Click(objWorkItemHomePage.detailsTab);
        softAssert.assertEquals(objBppTrendSetupPage.getFieldValueFromAPAS("Status"), "Completed", "SMAB-T2179: Work item status should be completed even after editing the BPP Settings for the completed work item");

        objBppTrendSetupPage.logout();
    }
    
    /**
     * This test case is to validate Work Item details after submitting it for approval
    **/
    @Test(description = "SMAB-T1838: Verify user is able to view Work Item details after submitting it for approval", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "Work_Item_BPP"}, alwaysRun = true, enabled = true)
    public void BPPTrends_VerifyWorkItemDetailsAfterSubmittedForApproval(String loginUser) throws Exception {
        
    	//Step1: Delete the existing data from system before importing files
        objEfileImportPage.deleteImportedRecords("BPP Trend Factors", "BOE - Valuation Factors", rollYear);

        //Step2: Delete the existing WI from system before importing files
        String query = "select id from Work_Item__c where Reference__c = 'BOE Valuation Factors'";
        objSalesforceAPI.delete("Work_Item__c", query);

        //Step3: Generate Reminder Work Items
        objSalesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_BPP_EFILE);

        //Step4: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objApasGenericPage.login(loginUser);

        //Stpe5: Open the Work Item Home Page
        objApasGenericPage.searchModule(modules.HOME);

        //Step6: "Import" Reminder Work Item generation validation
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importRowNumber = InPoolWorkItems.get("Request Type").indexOf("BPP Trends - Import - BOE Valuation Factors");
        String importWorkItem = InPoolWorkItems.get("Work Item Number").get(importRowNumber);

        //Step7: Accepting the work item and open it
        objWorkItemHomePage.acceptWorkItem(importWorkItem);
        objApasGenericPage.searchModule(modules.WORK_ITEM);
        objApasGenericPage.globalSearchRecords(importWorkItem);
        
        //Step 8: User submits the Work Item for Approval 
     	ReportLogger.INFO("User submits the Work Item for Approval :: " + importWorkItem);
		driver.navigate().refresh();
		Thread.sleep(2000);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline, 10);
		objPage.javascriptClick(objWorkItemHomePage.submittedforApprovalTimeline);
		Thread.sleep(2000);
		objPage.javascriptClick(objWorkItemHomePage.markStatusCompleteBtn);
		Thread.sleep(2000);
		softAssert.assertEquals(objPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1838:Verify user is able to submit the Work Item for approval");
		
		//Step 9: Validate the Work Item details after the Work Item is submitted for approval
		ReportLogger.INFO("User validates the Work Item details after it is Submitted for Approval");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);
		
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Status", "Information"),"Completed","SMAB-T1838: Validate user is able to validate the value of 'Status' field");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Type", "Information"),"BPP Trends","SMAB-T1838: Validate user is able to validate the value of 'Type' field");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Action", "Information"),"Import","SMAB-T1838: Validate user is able to validate the value of 'Action' field");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Work Pool", "Information"),"BPP Admin","SMAB-T1838: Validate user is able to validate the value of 'Work Pool' field");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Priority", "Information"),"None","SMAB-T1838: Validate user is able to validate the value of 'Priority' field");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Reference", "Information"),"BOE Valuation Factors","SMAB-T1838: Validate user is able to validate the value of 'Reference' field");
		
        objApasGenericPage.logout();
    }
    	 
}
