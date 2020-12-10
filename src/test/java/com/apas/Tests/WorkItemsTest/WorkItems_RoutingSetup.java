package com.apas.Tests.WorkItemsTest;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorkItems_RoutingSetup extends TestBase {
    RemoteWebDriver driver;
    Page objPage;
    ApasGenericPage apasGenericObj;
    WorkItemHomePage objWorkItemHomePage;
    Util objUtil;
    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI salesforceAPI = new SalesforceAPI();

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();

        objPage = new Page(driver);
        apasGenericObj = new ApasGenericPage(driver);
        objUtil = new Util();
        objWorkItemHomePage = new WorkItemHomePage(driver);
    }

    @Test(description = "SMAB-T1811,SMAB-T1812,SMAB-T1814,SMAB-T1815: Verify user is able to create,edit Neighborhood reference record with mandatory fields & not able to create duplicate record", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
            "smoke", "regression", "Work_Items_Manual" }, alwaysRun = true)
    public void WorkItem_verify_NeighborhoodReferenceRecordCreation(String loginUser) throws Exception {
        String workItemCreationData = System.getProperty("user.dir") + testdata.WORK_ITEMS_ROUTING_SETUP;
        Map<String, String> hashMapNeighborhoodData = objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateNeighborhood");

        //Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        apasGenericObj.login(loginUser);

        //Step2: Open the Neighborhoods Page & select all list view
        apasGenericObj.searchModule(modules.NEIGHBORHOODS);
        //objApasGenericFunctions.displayRecords("All");

        //Step3: Click on New Button and save the record without entering mandatory fields
        // Delete existing record before creating new record
        String query = "SELECT Id FROM Neighborhood__c WHERE Name  = '"+hashMapNeighborhoodData.get("Neighborhood Code")+"'";
        salesforceAPI.delete("Neighborhood__c",query);

        objPage.Click(objPage.getButtonWithText(apasGenericObj.NewButton));
        Thread.sleep(1000);
        objPage.Click(objPage.getButtonWithText(apasGenericObj.SaveButton));

        //Step4 : Verify Error message for mandatory fields
        String expectedErrorMessage = "Complete this field.";
        String actualErrorMessage = apasGenericObj.getIndividualFieldErrorMessage(objWorkItemHomePage.neighborhoodCodeEditBox);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-1811: Verify Neighborhood Code is a mandatory field");

        actualErrorMessage = apasGenericObj.getIndividualFieldErrorMessage(objWorkItemHomePage.neighborhoodDescriptionEditBox);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-1811: Verify Neighborhood Description is a mandatory field");

        actualErrorMessage = apasGenericObj.getIndividualFieldErrorMessage(objWorkItemHomePage.primaryAppraiserDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-1811: Verify Primary Appraiser is a mandatory field");

        actualErrorMessage = apasGenericObj.getIndividualFieldErrorMessage(objWorkItemHomePage.districtDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-1811: Verify District is a mandatory field");

        //Step5: Create new Neighborhood reference record
        objPage.Click(objWorkItemHomePage.CloseErrorMsg);
        objPage.enter(objWorkItemHomePage.neighborhoodCodeEditBox,hashMapNeighborhoodData.get("Neighborhood Code"));
        objPage.enter(objWorkItemHomePage.neighborhoodDescriptionEditBox,hashMapNeighborhoodData.get("Neighborhood Description"));
        apasGenericObj.searchAndSelectOptionFromDropDown(objWorkItemHomePage.primaryAppraiserDropDown,hashMapNeighborhoodData.get("Primary Appraiser"));
        apasGenericObj.selectOptionFromDropDown(objWorkItemHomePage.districtDropDown,hashMapNeighborhoodData.get("District"));
        String actualSuccessMessage = apasGenericObj.saveRecord();
        String expectedSuccessMessage="Neighborhood \""+hashMapNeighborhoodData.get("Neighborhood Code") +"\" was created.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-1812: Verify user is able to create new Neighborhood Reference Record successfully");

        //Step6: Edit existing Neighborhood reference record
        objPage.Click(objPage.getButtonWithText(apasGenericObj.EditButton));
        objPage.enter(objWorkItemHomePage.neighborhoodCodeEditBox,hashMapNeighborhoodData.get("Updated Neighborhood Code"));
        actualSuccessMessage = apasGenericObj.saveRecord();
        expectedSuccessMessage="Neighborhood \""+hashMapNeighborhoodData.get("Updated Neighborhood Code") +"\" was saved.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-1814: Verify user is able to edit Neighborhood Reference Record successfully");

        //Step7: Open the Neighborhoods Page & select all list view
        apasGenericObj.searchModule(modules.NEIGHBORHOODS);
        //objApasGenericFunctions.displayRecords("All");

        //Step8: Create duplicate Neighborhood reference record
        objPage.Click(objPage.getButtonWithText(apasGenericObj.NewButton));
        objPage.enter(objWorkItemHomePage.neighborhoodCodeEditBox,hashMapNeighborhoodData.get("Updated Neighborhood Code"));
        objPage.enter(objWorkItemHomePage.neighborhoodDescriptionEditBox,hashMapNeighborhoodData.get("Neighborhood Description"));
        apasGenericObj.searchAndSelectOptionFromDropDown(objWorkItemHomePage.primaryAppraiserDropDown,hashMapNeighborhoodData.get("Primary Appraiser"));
        apasGenericObj.selectOptionFromDropDown(objWorkItemHomePage.districtDropDown,hashMapNeighborhoodData.get("District"));
        actualErrorMessage = objPage.getElementText(apasGenericObj.pageError);
        expectedErrorMessage = "You can't save this record because a duplicate record already exists. To save, use different information.";
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-1815: Verify user is not able to create duplicate Neighborhood Reference Record");

        //Step9: Delete record create above
        query = "SELECT Id FROM Neighborhood__c WHERE Name  = '"+hashMapNeighborhoodData.get("Updated Neighborhood Code")+"'";
        salesforceAPI.delete("Neighborhood__c",query);

    }

    @Test(description = "SMAB-T1816,SMAB-T1817,SMAB-T1821,SMAB-T1822: Verify user is able to create,edit Territory record with mandatory fields & not able to create duplicate record", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
            "smoke", "regression", "Work_Items_Manual" }, alwaysRun = true)
    public void WorkItem_verify_TerritoryRecordCreation(String loginUser) throws Exception {
        String workItemCreationData = System.getProperty("user.dir") + testdata.WORK_ITEMS_ROUTING_SETUP;
        Map<String, String> hashMapTerritoryData = objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateTerritory");

        //Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        apasGenericObj.login(loginUser);

        //Step2: Open the Territories Page & select all list view
        apasGenericObj.searchModule(modules.TERRITORIES);
        //objApasGenericFunctions.displayRecords("All");

        //Step3: Click on New Button and save the record wthout entering mandatory fields
        // Delete existing record before creating new record
        String query = "SELECT Id FROM Territory__c WHERE Name  = '"+hashMapTerritoryData.get("Territory Name")+"'";
        salesforceAPI.delete("Territory__c",query);

        objPage.Click(objPage.getButtonWithText(apasGenericObj.NewButton));
        Thread.sleep(1000);
        objPage.Click(objPage.getButtonWithText(apasGenericObj.SaveButton));

        //Step4 : Verify Error message for mandatory fields
        String expectedErrorMessage = "Complete this field.";
        String actualErrorMessage = apasGenericObj.getIndividualFieldErrorMessage(objWorkItemHomePage.territoryNameEditBox);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-1816: Verify Territory Name is a mandatory field");

        actualErrorMessage = apasGenericObj.getIndividualFieldErrorMessage(objWorkItemHomePage.primaryAuditorDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-1816: Verify Primary Auditor is a mandatory field");

        //Step5: Create new Territory record
        objPage.Click(objWorkItemHomePage.CloseErrorMsg);
        objPage.enter(objWorkItemHomePage.territoryNameEditBox,hashMapTerritoryData.get("Territory Name"));
        apasGenericObj.searchAndSelectOptionFromDropDown(objWorkItemHomePage.primaryAuditorDropDown,hashMapTerritoryData.get("Primary Auditor"));
        String actualSuccessMessage = apasGenericObj.saveRecord();

        String expectedSuccessMessage="Territory \""+hashMapTerritoryData.get("Territory Name") +"\" was created.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-1817: Verify user is able to create new Territory Record successfully");

        //Step6: Edit existing Territory record
        objPage.Click(objPage.getButtonWithText(apasGenericObj.EditButton));
        objPage.enter(objWorkItemHomePage.territoryNameEditBox,hashMapTerritoryData.get("Updated Territory Name"));
        actualSuccessMessage = apasGenericObj.saveRecord();
        expectedSuccessMessage="Territory \""+hashMapTerritoryData.get("Updated Territory Name") +"\" was saved.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-1821: Verify user is able to edit Territory Record successfully");

        //Step7: Open the Territories Page & select all list view
        apasGenericObj.searchModule(modules.TERRITORIES);
        //objApasGenericFunctions.displayRecords("All");

        //Step8: Create duplicate Territory record
        objPage.Click(objPage.getButtonWithText(apasGenericObj.NewButton));
        apasGenericObj.searchAndSelectOptionFromDropDown(objWorkItemHomePage.primaryAuditorDropDown,"bpp adminAUT");
        objPage.enter(objWorkItemHomePage.territoryNameEditBox,hashMapTerritoryData.get("Updated Territory Name"));
        objPage.Click(objPage.getButtonWithText(apasGenericObj.SaveButton));
        actualErrorMessage = objPage.getElementText(apasGenericObj.pageError);
        expectedErrorMessage = "You can't save this record because a duplicate record already exists. To save, use different information.";
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-1822: Verify user is not able to create duplicate Territory Record");

        //Step9: Delete record create above
        query = "SELECT Id FROM Territory__c WHERE Name  = '"+hashMapTerritoryData.get("Updated Territory Name")+"'";
        salesforceAPI.delete("Territory__c",query);
    }

    @Test(description = "SMAB-T1826,SMAB-T1827,SMAB-T1828,SMAB-T1829: Verify user is able to create,edit Routing Assignments record with mandatory fields & not able to create duplicate record", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
            "smoke", "regression", "Work_Items_Manual" }, alwaysRun = true)
    public void WorkItem_verify_RoutingAssignmentRecordCreation(String loginUser) throws Exception {
        String workItemCreationData = System.getProperty("user.dir") + testdata.WORK_ITEMS_ROUTING_SETUP;
        Map<String, String> hashMapRoutingAssignmentData = objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateRoutingAssignment");

        //Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        apasGenericObj.login(loginUser);

        //Step2: Open the Routing Assignments Page & select all list view
        apasGenericObj.searchModule(modules.ROUTING_ASSIGNMENTS);
        apasGenericObj.displayRecords("All");

        //Step3: Click on New Button and save the record without entering mandatory fields
        //Delete existing record
        String query = "SELECT Id FROM Work_Item_Configuration__c WHERE Work_Item_Sub_Type__c = '"+hashMapRoutingAssignmentData.get("Work Item Sub Type")+"'";
        String workItemCofigurationId = salesforceAPI.select(query).get("Id").get(0);
        String routingAssignmentQuery = "SELECT Id FROM Routing_Assignment__c Where Configuration__c  = '"+workItemCofigurationId+"'";
        salesforceAPI.delete("Routing_Assignment__c",routingAssignmentQuery);

        objPage.Click(objPage.getButtonWithText(apasGenericObj.NewButton));
        Thread.sleep(1000);
        objPage.Click(objPage.getButtonWithText(apasGenericObj.SaveButton));

        //Step4 : Verify Error message for mandatory fields
        String expectedErrorMessage = "Complete this field.";
        String actualErrorMessage = apasGenericObj.getIndividualFieldErrorMessage(objWorkItemHomePage.workItemConfigurationDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1826: Verify Work Item Configuration is a mandatory field");

        //Step5: Create new Routing Assignments record
        query = "SELECT Name FROM Work_Item_Configuration__c WHERE Work_Item_Sub_Type__c = '"+hashMapRoutingAssignmentData.get("Work Item Sub Type")+"'";
        String workItemCofiguration = salesforceAPI.select(query).get("Name").get(0);
        objPage.Click(objWorkItemHomePage.CloseErrorMsg);
        apasGenericObj.searchAndSelectOptionFromDropDown(objWorkItemHomePage.workItemConfigurationDropDown,workItemCofiguration);
        String actualSuccessMessage = apasGenericObj.saveRecord();
        String expectedSuccessMessage="was created.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-T1827: Verify user is able to create new Routing Assignments Record successfully");

        //Step6: Edit existing Routing Assignments record
        objPage.Click(objPage.getButtonWithText(apasGenericObj.EditButton));
        apasGenericObj.searchAndSelectOptionFromDropDown(objWorkItemHomePage.workPoolDropDown,hashMapRoutingAssignmentData.get("Work Pool"));
        actualSuccessMessage = apasGenericObj.saveRecord();
        expectedSuccessMessage="was saved.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-T1828: Verify user is able to edit Routing Assignments Record successfully");

        //Step7: Open the Routing Assignments Page & select all list view
        apasGenericObj.searchModule(modules.ROUTING_ASSIGNMENTS);
        apasGenericObj.displayRecords("All");

        //Step8: Create duplicate Routing Assignments record
        objPage.Click(objPage.getButtonWithText(apasGenericObj.NewButton));
        apasGenericObj.searchAndSelectOptionFromDropDown(objWorkItemHomePage.workItemConfigurationDropDown,workItemCofiguration);
        apasGenericObj.searchAndSelectOptionFromDropDown(objWorkItemHomePage.workPoolDropDown,hashMapRoutingAssignmentData.get("Work Pool"));
        objPage.Click(objPage.getButtonWithText("Save"));;

        actualErrorMessage = objPage.getElementText(apasGenericObj.pageError);
        expectedErrorMessage = "You can't save this record because a duplicate record already exists. To save, use different information.";
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1829:Verify user is not able to create duplicate Routing Assignments Record");

        //Step9: Delete record create above
        salesforceAPI.delete("Routing_Assignment__c",routingAssignmentQuery);

    }
}
