package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.NeighborhoodsPage;
import com.apas.PageObjects.RoutingAssignmentPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.PageObjects.WorkItemsTerritoriesPage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class WorkItemAdministration_DistrictNeighborhood_Test extends TestBase {
	
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

    @Test(description = "SMAB-T1811,SMAB-T1812,SMAB-T1814,SMAB-T1815,SMAB-T2600,SMAB-2607,SMAB-T2609: Verify user is able to create,"
    		+ "edit Neighborhood reference record with mandatory fields & not able to create duplicate record", 
    		dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, 
    		groups = {"Regression", "WorkItemAdministration"}, alwaysRun = true)
    public void WorkItemAdministration_NeighborhoodReferenceRecordCreation(String loginUser) throws Exception {
        String workItemCreationData = testdata.WORK_ITEMS_ROUTING_SETUP;
        Map<String, String> hashMapNeighborhoodData = 
        		objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateNeighborhood");
        hashMapNeighborhoodData.put("Primary Appraiser",salesforceAPI.getUserName(users.RP_BUSINESS_ADMIN));
        
        Map<String, String> hashMapDuplicateNeighborhoodData = 
        		objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateDuplicateNeighborhood");
        hashMapDuplicateNeighborhoodData.put("Primary Appraiser",salesforceAPI.getUserName(users.RP_BUSINESS_ADMIN));

        //Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objWorkItemHomePage.login(loginUser);

        //Step2: Open the Neighborhoods Page & select all list view
        objWorkItemHomePage.searchModule(modules.NEIGHBORHOODS);
        //objWorkItemHomePage.searchModuleByNameorObject(modules.NEIGHBORHOODS, modules.NEIGHBORHOODS_OBJECT);
        //objApasGenericFunctions.displayRecords("All");

        //Step3: Click on New Button and save the record without entering mandatory fields
        // Delete existing record before creating new record
        String query = "SELECT Id FROM Neighborhood__c WHERE Name  = '"+hashMapNeighborhoodData.get("District")+"/"+ hashMapNeighborhoodData.get("Neighborhood Code")+"'";
        salesforceAPI.delete("Neighborhood__c",query);
        
        //Delete updated NB record 
        String query_2 = "SELECT Id FROM Neighborhood__c "
        		+ "WHERE Name  = '"+hashMapDuplicateNeighborhoodData.get("District")+"/"+ hashMapDuplicateNeighborhoodData.get("Neighborhood Code")+"'";
        salesforceAPI.delete("Neighborhood__c",query_2);

                
        objWorkItemHomePage.createRecord();
        objWorkItemHomePage.saveRecordAndGetError();

        //Step4 : Verify Error message for mandatory fields
        String expectedErrorMessage = "Complete this field.";
        String actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsNeighborhoodsPage.neighborhoodCodeEditBox);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, 
        		"SMAB-T1811,SMAB-T2609: Verify Neighborhood Code is a mandatory field");

        actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsNeighborhoodsPage.neighborhoodDescriptionEditBox);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, 
        		"SMAB-T1811: Verify Neighborhood Description is a mandatory field");

        actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsNeighborhoodsPage.primaryAppraiserDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, 
        		"SMAB-T1811: Verify Primary Appraiser is a mandatory field");

        actualErrorMessage = objWorkItemHomePage.getIndividualFieldErrorMessage(objWorkItemsNeighborhoodsPage.districtDropDown);
        softAssert.assertContains(actualErrorMessage, expectedErrorMessage, 
        		"SMAB-T1811, SMAB-T2609: Verify District is a mandatory field");

        //Step5: Create new Neighborhood reference record
        objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
        objWorkItemsNeighborhoodsPage.enterNeighborhoodReferenceRecordDetails(hashMapNeighborhoodData);
        String actualSuccessMessage = objWorkItemHomePage.saveRecord();
        String expectedSuccessMessage="Neighborhood \""+hashMapNeighborhoodData.get("District / Neighborhood Code") +"\" was created.";
        softAssert.assertContains(actualSuccessMessage, expectedSuccessMessage, 
        		"SMAB-T1812: Verify user is able to create new Neighborhood Reference Record successfully");

        //Step6: Edit existing Neighborhood reference record
        objWorkItemHomePage.editRecord();
        objWorkItemHomePage.enter(objWorkItemsNeighborhoodsPage.neighborhoodCodeEditBox,
        		hashMapDuplicateNeighborhoodData.get("Neighborhood Code"));
        actualSuccessMessage = objWorkItemHomePage.saveRecord();
        expectedSuccessMessage="Neighborhood \""+hashMapDuplicateNeighborhoodData.get("District / Neighborhood Code") +"\" was saved.";
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
        query = "SELECT Id FROM Neighborhood__c WHERE Name  = '"+hashMapNeighborhoodData.get("District")+"/"+ hashMapNeighborhoodData.get("Neighborhood Code")+"'";
        salesforceAPI.delete("Neighborhood__c",query);
        
        //Delete updated NB record 
        query_2 = "SELECT Id FROM Neighborhood__c "
        		+ "WHERE Name  = '"+hashMapDuplicateNeighborhoodData.get("District")+"/"+ hashMapDuplicateNeighborhoodData.get("Neighborhood Code")+"'";
        salesforceAPI.delete("Neighborhood__c",query_2);


    }


}
