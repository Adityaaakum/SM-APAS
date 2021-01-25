package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

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

import java.util.Map;

public class WorkItemAdministration_RoutingSetupTest extends TestBase {
    RemoteWebDriver driver;
    WorkItemHomePage objWorkItemHomePage;
    RoutingAssignmentPage objWorkItemsRoutingSetupPage;
    NeighborhoodsPage objWorkItemsNeighborhoodsPage;
    WorkItemsTerritoriesPage objWorkItemsTerritoriesPage;
    Util objUtil;
    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI salesforceAPI = new SalesforceAPI();

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();

        objUtil = new Util();
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objWorkItemsRoutingSetupPage = new RoutingAssignmentPage(driver);
        objWorkItemsNeighborhoodsPage = new NeighborhoodsPage(driver);
        objWorkItemsTerritoriesPage = new WorkItemsTerritoriesPage(driver);
    }

    @Test(description = "SMAB-T1811,SMAB-T1812,SMAB-T1814,SMAB-T1815: Verify user is able to create,edit Neighborhood reference record with mandatory fields & not able to create duplicate record", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
            "smoke", "regression", "Work_Items_Manual" }, alwaysRun = true)
    public void WorkItemAdministration_NeighborhoodReferenceRecordCreation(String loginUser) throws Exception {
        String workItemCreationData = testdata.WORK_ITEMS_ROUTING_SETUP;
        Map<String, String> hashMapNeighborhoodData = objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateNeighborhood");
        hashMapNeighborhoodData.put("Primary Appraiser",salesforceAPI.getUserName(users.RP_BUSINESS_ADMIN));

        //Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objWorkItemHomePage.login(loginUser);

        //Step2: Open the Neighborhoods Page & select all list view
        objWorkItemHomePage.searchModule(modules.NEIGHBORHOODS);
        //objApasGenericFunctions.displayRecords("All");

        //Step3: Click on New Button and save the record without entering mandatory fields
        // Delete existing record before creating new record
        String query = "SELECT Id FROM Neighborhood__c WHERE Name  = '"+hashMapNeighborhoodData.get("Neighborhood Code")+"'";
        salesforceAPI.delete("Neighborhood__c",query);

        objWorkItemHomePage.createRecord();
        objWorkItemHomePage.saveRecordAndGetError();

        //Step4 : Verify Error message for mandatory fields
        String expectedErrorMessage = "Complete this field.";
        String actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsNeighborhoodsPage.neighborhoodCodeEditBox);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1811: Verify Neighborhood Code is a mandatory field");

        actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsNeighborhoodsPage.neighborhoodDescriptionEditBox);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1811: Verify Neighborhood Description is a mandatory field");

        actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsNeighborhoodsPage.primaryAppraiserDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1811: Verify Primary Appraiser is a mandatory field");

        actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsNeighborhoodsPage.districtDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1811: Verify District is a mandatory field");

        //Step5: Create new Neighborhood reference record
        objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
        objWorkItemsNeighborhoodsPage.enterNeighborhoodReferenceRecordDetails(hashMapNeighborhoodData);
        String actualSuccessMessage = objWorkItemHomePage.saveRecord();
        String expectedSuccessMessage="Neighborhood \""+hashMapNeighborhoodData.get("Neighborhood Code") +"\" was created.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-T1812: Verify user is able to create new Neighborhood Reference Record successfully");

        //Step6: Edit existing Neighborhood reference record
        Map<String, String> hashMapDuplicateNeighborhoodData = objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateDuplicateNeighborhood");
        hashMapDuplicateNeighborhoodData.put("Primary Appraiser",salesforceAPI.getUserName(users.RP_BUSINESS_ADMIN));

        objWorkItemHomePage.editRecord();
        objWorkItemHomePage.enter(objWorkItemsNeighborhoodsPage.neighborhoodCodeEditBox,hashMapDuplicateNeighborhoodData.get("Neighborhood Code"));
        actualSuccessMessage = objWorkItemHomePage.saveRecord();
        expectedSuccessMessage="Neighborhood \""+hashMapDuplicateNeighborhoodData.get("Neighborhood Code") +"\" was saved.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-T1814: Verify user is able to edit Neighborhood Reference Record successfully");

        //Step7: Open the Neighborhoods Page & select all list view
        objWorkItemHomePage.searchModule(modules.NEIGHBORHOODS);
        //objApasGenericFunctions.displayRecords("All");

        //Step8: Create duplicate Neighborhood reference record
        objWorkItemHomePage.createRecord();
        objWorkItemsNeighborhoodsPage.enterNeighborhoodReferenceRecordDetails(hashMapDuplicateNeighborhoodData);
        actualErrorMessage = objWorkItemHomePage.saveRecordAndGetError();
        expectedErrorMessage = "You can't save this record because a duplicate record already exists. To save, use different information.";
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1815: Verify user is not able to create duplicate Neighborhood Reference Record");

        //Step9: Delete record create above
        query = "SELECT Id FROM Neighborhood__c WHERE Name  = '"+hashMapDuplicateNeighborhoodData.get("Neighborhood Code")+"'";
        salesforceAPI.delete("Neighborhood__c",query);

    }

    @Test(description = "SMAB-T1816,SMAB-T1817,SMAB-T1821,SMAB-T1822: Verify user is able to create,edit Territory record with mandatory fields & not able to create duplicate record", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
            "smoke", "regression", "Work_Items_Manual" }, alwaysRun = true)
    public void WorkItemAdministration_TerritoryRecordCreation(String loginUser) throws Exception {
        String workItemCreationData = testdata.WORK_ITEMS_ROUTING_SETUP;
        Map<String, String> hashMapTerritoryData = objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateTerritory");

        //Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objWorkItemHomePage.login(loginUser);

        //Step2: Open the Territories Page & select all list view
        objWorkItemHomePage.searchModule(modules.TERRITORIES);
        //objApasGenericFunctions.displayRecords("All");

        //Step3: Click on New Button and save the record without entering mandatory fields
        // Delete existing record before creating new record
        String query = "SELECT Id FROM Territory__c WHERE Name  = '"+hashMapTerritoryData.get("Territory Name")+"'";
        salesforceAPI.delete("Territory__c",query);

        Map<String, String> hashMapDuplicateTerritoryData = objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateDuplicateTerritory");
        String queryToDeleteDuplicate = "SELECT Id FROM Territory__c WHERE Name  = '"+hashMapDuplicateTerritoryData.get("Territory Name")+"'";
        salesforceAPI.delete("Territory__c",queryToDeleteDuplicate);

        objWorkItemHomePage.createRecord();
        objWorkItemHomePage.saveRecordAndGetError();

        //Step4 : Verify Error message for mandatory fields
        String expectedErrorMessage = "Complete this field.";
        String actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsTerritoriesPage.territoryNameEditBox);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1816: Verify Territory Name is a mandatory field");

        actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsTerritoriesPage.primaryAuditorDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1816: Verify Primary Auditor is a mandatory field");

        //Step5: Create new Territory record
        objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
        objWorkItemsTerritoriesPage.enterTerritoryRecordDetails(hashMapTerritoryData);
        String actualSuccessMessage = objWorkItemHomePage.saveRecord();

        String expectedSuccessMessage="Territory \""+hashMapTerritoryData.get("Territory Name") +"\" was created.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-T1817: Verify user is able to create new Territory Record successfully");

        //Step6: Edit existing Territory record

        objWorkItemHomePage.editRecord();
        objWorkItemHomePage.enter(objWorkItemsTerritoriesPage.territoryNameEditBox,hashMapDuplicateTerritoryData.get("Territory Name"));
        actualSuccessMessage = objWorkItemHomePage.saveRecord();
        expectedSuccessMessage="Territory \""+hashMapDuplicateTerritoryData.get("Territory Name") +"\" was saved.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-T1821: Verify user is able to edit Territory Record successfully");

        //Step7: Open the Territories Page & select all list view
        objWorkItemHomePage.searchModule(modules.TERRITORIES);
        //objApasGenericFunctions.displayRecords("All");

        //Step8: Create duplicate Territory record
        objWorkItemHomePage.createRecord();
        objWorkItemsTerritoriesPage.enterTerritoryRecordDetails(hashMapDuplicateTerritoryData);
        actualErrorMessage = objWorkItemHomePage.saveRecordAndGetError();

        expectedErrorMessage = "You can't save this record because a duplicate record already exists. To save, use different information.";
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1822: Verify user is not able to create duplicate Territory Record");

        //Step9: Delete record create above
        query = "SELECT Id FROM Territory__c WHERE Name  = '"+hashMapDuplicateTerritoryData.get("Territory Name")+"'";
        salesforceAPI.delete("Territory__c",query);
    }

    @Test(description = "SMAB-T1826,SMAB-T1827,SMAB-T1828,SMAB-T1829: Verify user is able to create,edit Routing Assignments record with mandatory fields & not able to create duplicate record", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
            "smoke", "regression", "Work_Items_Manual" }, alwaysRun = true)
    public void WorkItemAdministration_RoutingAssignmentRecordCreation(String loginUser) throws Exception {
        String workItemCreationData = testdata.WORK_ITEMS_ROUTING_SETUP;
        Map<String, String> hashMapRoutingAssignmentData = objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateRoutingAssignment");

        //Step1: Login to the APAS application using the credentials passed through data provider (RP Business Admin)
        objWorkItemHomePage.login(loginUser);

        //Step2: Open the Routing Assignments Page & select all list view
        objWorkItemHomePage.searchModule(modules.ROUTING_ASSIGNMENTS);
        objWorkItemHomePage.displayRecords("All");

        //Step3: Click on New Button and save the record without entering mandatory fields
        //Delete existing record
        String query = "SELECT Id FROM Work_Item_Configuration__c WHERE Work_Item_Sub_Type__c = '"+hashMapRoutingAssignmentData.get("Work Item Sub Type")+"'";
        String workItemCofigurationId = salesforceAPI.select(query).get("Id").get(0);
        String routingAssignmentQuery = "SELECT Id FROM Routing_Assignment__c Where Configuration__c  = '"+workItemCofigurationId+"'";
        salesforceAPI.delete("Routing_Assignment__c",routingAssignmentQuery);

        objWorkItemHomePage.createRecord();
        objWorkItemHomePage.saveRecordAndGetError();

        //Step4 : Verify Error message for mandatory fields
        String expectedErrorMessage = "Complete this field.";
        String actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsRoutingSetupPage.workItemConfigurationDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1826: Verify Work Item Configuration is a mandatory field");

        //Step5: Create new Routing Assignments record
        query = "SELECT Name FROM Work_Item_Configuration__c WHERE Work_Item_Sub_Type__c = '"+hashMapRoutingAssignmentData.get("Work Item Sub Type")+"'";
        String workItemCofiguration = salesforceAPI.select(query).get("Name").get(0);
        objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
        objWorkItemHomePage.searchAndSelectOptionFromDropDown(objWorkItemsRoutingSetupPage.workItemConfigurationDropDown,workItemCofiguration);
        String actualSuccessMessage = objWorkItemHomePage.saveRecord();
        String expectedSuccessMessage="was created.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-T1827: Verify user is able to create new Routing Assignments Record successfully");

        //Step6: Edit existing Routing Assignments record
        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.EditButton));
        objWorkItemHomePage.searchAndSelectOptionFromDropDown(objWorkItemsRoutingSetupPage.workPoolDropDown,hashMapRoutingAssignmentData.get("Work Pool"));
        actualSuccessMessage = objWorkItemHomePage.saveRecord();
        expectedSuccessMessage="was saved.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, "SMAB-T1828: Verify user is able to edit Routing Assignments Record successfully");

        //Step7: Open the Routing Assignments Page & select all list view
        objWorkItemHomePage.searchModule(modules.ROUTING_ASSIGNMENTS);
        objWorkItemHomePage.displayRecords("All");

        //Step8: Create duplicate Routing Assignments record
        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.NewButton));
        objWorkItemHomePage.searchAndSelectOptionFromDropDown(objWorkItemsRoutingSetupPage.workItemConfigurationDropDown,workItemCofiguration);
        objWorkItemHomePage.searchAndSelectOptionFromDropDown(objWorkItemsRoutingSetupPage.workPoolDropDown,hashMapRoutingAssignmentData.get("Work Pool"));
        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText("Save"));

        actualErrorMessage = objWorkItemHomePage.getElementText(objWorkItemHomePage.pageError);
        expectedErrorMessage = "You can't save this record because a duplicate record already exists. To save, use different information.";
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, "SMAB-T1829:Verify user is not able to create duplicate Routing Assignments Record");

        //Step9: Delete record create above
        salesforceAPI.delete("Routing_Assignment__c",routingAssignmentQuery);

    }
}
