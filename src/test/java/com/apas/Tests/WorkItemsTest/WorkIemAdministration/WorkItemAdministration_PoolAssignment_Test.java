package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.Reports.ReportLogger;
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

public class WorkItemAdministration_PoolAssignment_Test extends TestBase {
    RemoteWebDriver driver;

    PoolAssignmentPage objPoolAssignmentPage;
    ParcelsPage objParcelsPage;
    WorkItemHomePage objWorkItemHomePage;
    WorkPoolPage objWorkPoolPage;
    NeighborhoodsPage objNeighborhoodsPage;
    RoutingAssignmentPage objRoutingAssignmentPage;
    MappingPage objMappingPage;
    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI salesforceAPI = new SalesforceAPI();
    Util objUtil = new Util();

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();
        objNeighborhoodsPage = new NeighborhoodsPage(driver);
        objParcelsPage = new ParcelsPage(driver);
        objPoolAssignmentPage = new PoolAssignmentPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objMappingPage = new MappingPage(driver);
        objWorkPoolPage = new WorkPoolPage(driver);
        objRoutingAssignmentPage = new RoutingAssignmentPage(driver);
    }

    @Test(description = "SMAB-T2307: Verify that the Warning message is not appearing for the Closed or No opened WI on removing the User from PA", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "Work_Items_PoolAssignment"}, alwaysRun = true)
    public void WorkItemAdministration_PoolAssignment_NoWarningOnRemovingStaff(String loginUser) throws Exception {

        String workPool = "RP Admin";

        //Getting the user name
        String user = salesforceAPI.getUserName(users.BPP_BUSINESS_ADMIN);

        //Deleting the work items assigned to a particular user and pool
        String workItemDeleteQuery = "SELECT Id FROM Work_Item__c where Work_Pool__r.name = '" + workPool + "' and Assigned_To__r.name = '" + user + "'";
        salesforceAPI.delete(workItemDeleteQuery);

        //Deleting the pool assignment if it already exists
        String poolAssignmentQuery = "SELECT Id FROM Pool_Assignments__c where Work_Pool__r.name ='" + workPool + "' and user__r.name = '" + user + "'";
        salesforceAPI.delete("Pool_Assignments__c",poolAssignmentQuery);

        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objPoolAssignmentPage.login(loginUser);

        //Step2: Search and open the Pool Assignment
        objPoolAssignmentPage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);

        //Step3: Create the pool assignment
        objPoolAssignmentPage.createPoolAssignment(user,"Staff","");

        //Step4: Remove the staff
        driver.navigate().refresh();
        objPoolAssignmentPage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);
        String routingAssignment = objPoolAssignmentPage.getGridDataForRowString(user).get("Pool Assignments Name").get(0).split("\n")[0];
        objWorkItemHomePage.clickCheckBoxForSelectingWI(routingAssignment);
        objPoolAssignmentPage.Click(objPoolAssignmentPage.getButtonWithText(objWorkPoolPage.buttonRemoveStaff));

        //Step5: Validation for warning message not appearing
        objPoolAssignmentPage.waitForElementToBeClickable(objWorkItemHomePage.removeButton);
        softAssert.assertTrue(!objPoolAssignmentPage.verifyElementExists(objPoolAssignmentPage.errorWarningMessage),"SMAB-T2307: Validation for warning message not appearing while removing staff as no work item is linked");

        //Step6: Removing the staff
        driver.switchTo().frame(0);
        objPoolAssignmentPage.Click(objWorkItemHomePage.removeButton);
        driver.switchTo().parentFrame();
        objPoolAssignmentPage.waitForElementToBeClickable(objPoolAssignmentPage.successAlert,5);
        objPoolAssignmentPage.waitForElementToDisappear(objPoolAssignmentPage.successAlert,10);

        //Logout of APAS Application
        Thread.sleep(2000);
        objWorkItemHomePage.logout();
    }


    @Test(description = "SMAB-T2305: Verify that the Warning message for opened WI on removing the User from PA", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "Work_Items_PoolAssignment"}, alwaysRun = true)
    public void WorkItemAdministration_PoolAssignment_WarningOnRemovingStaff(String loginUser) throws Exception {

        String workPool = "RP Admin";

        //Getting the user name
        String user = salesforceAPI.getUserName(users.BPP_BUSINESS_ADMIN);

        //Deleting the work items assigned to a particular user and pool
        String workItemDeleteQuery = "SELECT Id FROM Work_Item__c where Work_Pool__r.name = '" + workPool + "' and Assigned_To__r.name = '" + user + "'";
        salesforceAPI.delete(workItemDeleteQuery);

        //Deleting the pool assignment if it already exists
        String poolAssignmentQuery = "SELECT Id FROM Pool_Assignments__c where Work_Pool__r.name ='" + workPool + "' and user__r.name = '" + user + "'";
        salesforceAPI.delete("Pool_Assignments__c",poolAssignmentQuery);

        //Step2: fetching a parcel where PUC is not blank but Primary Situs is not blank
        String apnValue = objWorkItemHomePage.fetchActiveAPN();

        // Step3: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objPoolAssignmentPage.login(loginUser);

        //Step4: Search and open the Pool Assignment
        objPoolAssignmentPage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);

        //Step5: Create the pool assignment
        objPoolAssignmentPage.createPoolAssignment(user,"Staff","");

        // Step6: Create the work item
        objWorkItemHomePage.searchModule(modules.PARCELS);
        objWorkItemHomePage.globalSearchRecords(apnValue);
        String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapGiveWorkItemToSomeoneElse = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToSomeoneElse");
        hashMapGiveWorkItemToSomeoneElse.put("Work Item Owner",user);
        objParcelsPage.createWorkItem(hashMapGiveWorkItemToSomeoneElse);

        //Step7: Remove the staff
        driver.navigate().refresh();
        objWorkItemHomePage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);
        String routingAssignment = objPoolAssignmentPage.getGridDataForRowString(user).get("Pool Assignments Name").get(0).split("\n")[0];
        objWorkItemHomePage.clickCheckBoxForSelectingWI(routingAssignment);
        objPoolAssignmentPage.Click(objPoolAssignmentPage.getButtonWithText(objWorkPoolPage.buttonRemoveStaff));

        //Step8: Warning message validation
        objPoolAssignmentPage.waitForElementToBeClickable(objWorkItemHomePage.removeButton);

        //Step9: No warning message should appear
        driver.switchTo().frame(0);
        String expectedWarningMessage = "User(s) currently has 1 work items claimed. Please re-assign the user's work items or proceed with the removal.";
        softAssert.assertEquals(objPoolAssignmentPage.getElementText(driver.findElementByXPath(objPoolAssignmentPage.errorWarningMessage)),expectedWarningMessage,"SMAB-T2305: Validation for warning message appearing while removing staff as work item is linked to this staff");
        objPoolAssignmentPage.Click(objWorkItemHomePage.removeButton);
        driver.switchTo().parentFrame();
        objPoolAssignmentPage.waitForElementToBeClickable(objPoolAssignmentPage.successAlert,5);
        objPoolAssignmentPage.waitForElementToDisappear(objPoolAssignmentPage.successAlert,10);

        //Logout of APAS Application
        Thread.sleep(2000);
        objWorkItemHomePage.logout();
    }

    /* This test case validates below validation when Neighborhood is updated in the routing assignment
    1. New Pool Assignment record is created if the pool assignment doesn't exist with the primary appraiser of the neighborhood
    2. Existing User role is updated to "Staff" where user is not equal to the primary appraiser of the neighborhood
    3. Role is updated to "Primary Appraiser" where user equals to the primary appraiser of the updated neighborhood
     */
    @Test(description = "SMAB-T2602, SMAB-T2601, SMAB-T2599: Verify Primary Appraiser in pool assignment when neighborhood is changed in Routing assignment", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "Work_Items_PoolAssignment"}, alwaysRun = true)
    public void WorkItemAdministration_UpdateNeighborhood_ValidatePoolAssignment(String loginUser) throws Exception {

        String primaryAppraiser1 = salesforceAPI.getUserName(users.APPRAISAL_SUPPORT);
        String primaryAppraiser2 = salesforceAPI.getUserName(users.EXEMPTION_SUPPORT_STAFF);
        String workPool = "SMAB6150";
        String neighborhood1 = "1SMAB6150";
        String neighborhood2 = "2SMAB6150";

        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objPoolAssignmentPage.login(loginUser);

        //Step2: Create two neighborhood records if the records don't exist already
        String neighborhoodQuery1 = "SELECT Id FROM Neighborhood__c where Neighborhood_Code__c = '" + neighborhood1 + "'";
        HashMap<String, ArrayList<String>> neighborhoodRecord1 = salesforceAPI.select(neighborhoodQuery1);
        if (neighborhoodRecord1.size() == 0){
            objWorkItemHomePage.searchModule(modules.NEIGHBORHOODS);
            Map<String, String> hashMapNeighborhoodRecord = objUtil.generateMapFromJsonFile(testdata.NEIGHBORHOOD, "DataToCreateNeighborhood");
            hashMapNeighborhoodRecord.put("Primary Appraiser",primaryAppraiser1);
            hashMapNeighborhoodRecord.put("Neighborhood Code",neighborhood1);
            objNeighborhoodsPage.createNeighborhoodReferenceRecord(hashMapNeighborhoodRecord);
        } else{
            String userQuery = "select Id from User where UserName__c = '"+ CONFIG.getProperty(users.APPRAISAL_SUPPORT + "UserName") + "'";
            String userId = salesforceAPI.select(userQuery).get("Id").get(0);
            salesforceAPI.update("Neighborhood__c",neighborhoodQuery1,"Primary_Appraiser__c",userId);
        }


        String neighborhoodQuery2 = "SELECT Id FROM Neighborhood__c where Neighborhood_Code__c = '" + neighborhood2 + "'";
        HashMap<String, ArrayList<String>> neighborhoodRecord2 = salesforceAPI.select(neighborhoodQuery2);
        if (neighborhoodRecord2.size() == 0){
            objWorkItemHomePage.searchModule(modules.NEIGHBORHOODS);
            Map<String, String> hashMapNeighborhoodRecord = objUtil.generateMapFromJsonFile(testdata.NEIGHBORHOOD, "DataToCreateNeighborhood");
            hashMapNeighborhoodRecord.put("Primary Appraiser",primaryAppraiser2);
            hashMapNeighborhoodRecord.put("Neighborhood Code",neighborhood2);
            objNeighborhoodsPage.createNeighborhoodReferenceRecord(hashMapNeighborhoodRecord);
        } else{
            String userQuery = "select Id from User where UserName__c = '"+ CONFIG.getProperty(users.EXEMPTION_SUPPORT_STAFF + "UserName") + "'";
            String userId = salesforceAPI.select(userQuery).get("Id").get(0);
            salesforceAPI.update("Neighborhood__c",neighborhoodQuery2,"Primary_Appraiser__c",userId);
        }

        //Step3: Create the work pool if the work pool doesn't exist
        String workPoolQuery = "SELECT Name FROM Work_Pool__c where Name = '" + workPool + "'";
        if (salesforceAPI.select(workPoolQuery).size() == 0){
            objWorkItemHomePage.searchModule(modules.WORK_POOL);
            objWorkPoolPage.createWorkPool(workPool,salesforceAPI.getUserName(users.RP_BUSINESS_ADMIN));
        }

        //Step4: Deleting the pool assignment if it already exists
        String poolAssignmentQuery = "SELECT Id FROM Pool_Assignments__c where Work_Pool__r.name ='" + workPool + "'";
        salesforceAPI.delete("Pool_Assignments__c",poolAssignmentQuery);

        //Step5: Delete the existing routing assignment if routing assignment exists
        String queryWorkItemConfiguration = "SELECT Id,Name FROM Work_Item_Configuration__c WHERE Work_Item_Sub_Type__c = 'Unincorporated File Upload'";
        String workItemConfiguration = salesforceAPI.select(queryWorkItemConfiguration).get("Name").get(0);
        String routingAssignmentQuery = "SELECT Id,Name FROM Routing_Assignment__c where Configuration__r.name = '" + workItemConfiguration + "' and work_pool__r.name = '" + workPool + "'";
        salesforceAPI.delete("Routing_Assignment__c",routingAssignmentQuery);

        //Step6: Create the routing assignment without neighborhood
        ReportLogger.INFO("Creating the Routing Assignment without neighborhood");
        objWorkItemHomePage.searchModule(modules.ROUTING_ASSIGNMENTS);
        Map<String, String> hashMapRoutingAssignment = new HashMap<>();
        hashMapRoutingAssignment.put("Work Pool",workPool);
        hashMapRoutingAssignment.put("Work Item Configuration",workItemConfiguration);
        objRoutingAssignmentPage.createRoutingAssignmentRecord(hashMapRoutingAssignment);

        softAssert.assertEquals("0",salesforceAPI.select(poolAssignmentQuery).size(),"SMAB-T2603: No Pool Assignment should be created as neighborhood is not selected while createing the routing assignment");

        //Step7: Update the neighborhood in the routing assignment and pool assignment should automatically be created
        ReportLogger.INFO("Updating the Neighborhood in Routing Assignment");
        String routingAssignment = salesforceAPI.select(routingAssignmentQuery).get("Name").get(0);
        objWorkItemHomePage.searchModule(modules.ROUTING_ASSIGNMENTS);
        objPoolAssignmentPage.globalSearchRecords(routingAssignment);
        objPoolAssignmentPage.editRecord();
        objPoolAssignmentPage.searchAndSelectOptionFromDropDown(objRoutingAssignmentPage.neighborhoodDropDown,neighborhood1);
        objPoolAssignmentPage.saveRecord();

        objPoolAssignmentPage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);
        HashMap<String, ArrayList<String>> poolAssignmentData = objPoolAssignmentPage.getGridDataInHashMap();

        //SCENARIO1: New Pool Assignment record is created if the pool assignment doesn't exist with the primary appraiser of the neighborhood
        ReportLogger.INFO("SCENARIO1: New Pool Assignment record is created if the pool assignment doesn't exist with the primary appraiser of the neighborhood");
        softAssert.assertEquals(poolAssignmentData.get("Pool Assignments Name").size(),"1","SMAB-T2602: Pool Assignment should be created as neighborhood is selected while in the routing assignment");
        softAssert.assertEquals(poolAssignmentData.get("User").get(0),primaryAppraiser1,"SMAB-T2602: Primary Appraiser in the created pool assignment should be same as the primary appraiser of the updated neighborhood");
        softAssert.assertEquals(poolAssignmentData.get("Role").get(0),"Primary Appraiser","SMAB-T2602: Role should be Primary Appraiser in the created pool assignment");

        //Step8: Update the neighborhood, role should automatically updated as per the primary appraiser of the updated neighborhood
        objWorkItemHomePage.searchModule(modules.ROUTING_ASSIGNMENTS);
        objPoolAssignmentPage.globalSearchRecords(routingAssignment);
        objPoolAssignmentPage.editRecord();
        objPoolAssignmentPage.clearSelectionFromLookup(objRoutingAssignmentPage.neighborhoodDropDown);
        objPoolAssignmentPage.searchAndSelectOptionFromDropDown(objRoutingAssignmentPage.neighborhoodDropDown,neighborhood2);
        objPoolAssignmentPage.saveRecord();

        driver.navigate().refresh();
        Thread.sleep(2000);
        objPoolAssignmentPage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);

        //SCENARIO2: Existing User role is updated to "Staff" where user is not equal to the primary appraiser of the neighborhood
        ReportLogger.INFO("SCENARIO2: Existing User role is updated to \"Staff\" where user is not equal to the primary appraiser of the neighborhood");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataInHashMap().get("Pool Assignments Name").size(),"2","SMAB-T2602: New Pool Assignment should be created as neighborhood is updated in the routing assignment");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataForRowString(primaryAppraiser1).get("Role").get(0),"Staff","SMAB-T2599: Role should be changed to Staff as neighborhood record is updated");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataForRowString(primaryAppraiser2).get("Role").get(0),"Primary Appraiser","SMAB-T2602: Role should be Primary Appraiser in the created pool assignment");

        //Step9: Change the neighborhood again, current primary appraiser should be converted to staff and no new record should be created
        objWorkItemHomePage.searchModule(modules.ROUTING_ASSIGNMENTS);
        objPoolAssignmentPage.globalSearchRecords(routingAssignment);
        objPoolAssignmentPage.editRecord();
        objPoolAssignmentPage.clearSelectionFromLookup(objRoutingAssignmentPage.neighborhoodDropDown);
        objPoolAssignmentPage.searchAndSelectOptionFromDropDown(objRoutingAssignmentPage.neighborhoodDropDown,neighborhood1);
        objPoolAssignmentPage.saveRecord();

        driver.navigate().refresh();
        Thread.sleep(2000);
        objPoolAssignmentPage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);

        //SCENARIO3: Role is updated to "Primary Appraiser" where user equals to the primary appraiser of the updated neighborhood
        ReportLogger.INFO("SCENARIO3: Role is updated to \"Primary Appraiser\" where user equals to the primary appraiser of the updated neighborhood");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataInHashMap().get("Pool Assignments Name").size(),"2","SMAB-T2601: New Pool Assignment should note be created as neighborhood is updated in the routing assignment and the primary appraiser was already existing");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataForRowString(primaryAppraiser1).get("Role").get(0),"Primary Appraiser","SMAB-T2601: Role should be changed to Primary Appraiser as per the updated neighborhood");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataForRowString(primaryAppraiser2).get("Role").get(0),"Staff","SMAB-T2599: Role should be changed to Staff as neighborhood record is updated");

        //Logout of APAS Application
        objWorkItemHomePage.logout();
    }


    /* This test case validates below validation when Primary Appraiser is updated in the Neighborhood
    1. New Pool Assignment record is created if the pool assignment doesn't exist with the primary appraiser of the neighborhood
    2. Existing User role is updated to "Staff" where user is not equal to the primary appraiser of the neighborhood
    3. Role is updated to "Primary Appraiser" where user equals to the primary appraiser of the updated neighborhood
     */
    @Test(description = "SMAB-T2603, SMAB-T2604, SMAB-T2605: Verify primary appraisers in pool assignment when Primary Appraiser is changed in Neighborood", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "Work_Items_PoolAssignment"}, alwaysRun = true)
    public void WorkItemAdministration_UpdatePrimaryAppraiser_ValidatePoolAssignment(String loginUser) throws Exception {

        String primaryAppraiser1 = salesforceAPI.getUserName(users.APPRAISAL_SUPPORT);
        String primaryAppraiser2 = salesforceAPI.getUserName(users.EXEMPTION_SUPPORT_STAFF);
        String workPool = "SMAB6150";
        String neighborhood = "3SMAB6150";
        String neighborhoodWithDistrict = "01/3SMAB6150";

        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objPoolAssignmentPage.login(loginUser);

        //Step2: Create the neighborhood record if the record doesn't exist already
        String neighborhoodQuery = "SELECT Id FROM Neighborhood__c where Neighborhood_Code__c = '" + neighborhood + "'";
        HashMap<String, ArrayList<String>> neighborhoodRecord = salesforceAPI.select(neighborhoodQuery);
        if (neighborhoodRecord.size() == 0){
            objWorkItemHomePage.searchModule(modules.NEIGHBORHOODS);
            Map<String, String> hashMapNeighborhoodRecord = objUtil.generateMapFromJsonFile(testdata.NEIGHBORHOOD, "DataToCreateNeighborhood");
            hashMapNeighborhoodRecord.put("Primary Appraiser",primaryAppraiser1);
            hashMapNeighborhoodRecord.put("Neighborhood Code",neighborhood);
            objNeighborhoodsPage.createNeighborhoodReferenceRecord(hashMapNeighborhoodRecord);
        } else{
            String userQuery = "select Id from User where UserName__c = '"+ CONFIG.getProperty(users.EXEMPTION_SUPPORT_STAFF + "UserName") + "'";
            String userId = salesforceAPI.select(userQuery).get("Id").get(0);
            salesforceAPI.update("Neighborhood__c",neighborhoodQuery,"Primary_Appraiser__c",userId);
        }

        //Step3: Create the work pool if the work pool doesn't exist
        String workPoolQuery = "SELECT Name FROM Work_Pool__c where Name = '" + workPool + "'";
        if (salesforceAPI.select(workPoolQuery).size() == 0){
            objWorkItemHomePage.searchModule(modules.WORK_POOL);
            objWorkPoolPage.createWorkPool(workPool,salesforceAPI.getUserName(users.RP_BUSINESS_ADMIN));
        }

        //Step4: Creating the routing assignment if routing assignment doesn't exist
        String queryWorkItemConfiguration = "SELECT Id,Name FROM Work_Item_Configuration__c WHERE Work_Item_Sub_Type__c = 'Unincorporated File Upload'";
        String workItemConfiguration = salesforceAPI.select(queryWorkItemConfiguration).get("Name").get(0);
        String routingAssignmentQuery = "SELECT Id,Name FROM Routing_Assignment__c where Configuration__r.name = '" + workItemConfiguration + "' and work_pool__r.name = '" + workPool + "'";
        if (salesforceAPI.select(routingAssignmentQuery).size()==0){
            ReportLogger.INFO("Creating the Routing Assignment without neighborhood");
            objWorkItemHomePage.searchModule(modules.ROUTING_ASSIGNMENTS);
            Map<String, String> hashMapRoutingAssignment = new HashMap<>();
            hashMapRoutingAssignment.put("Work Pool",workPool);
            hashMapRoutingAssignment.put("Work Item Configuration",workItemConfiguration);
            hashMapRoutingAssignment.put("Neighborhood",neighborhood);
            objRoutingAssignmentPage.createRoutingAssignmentRecord(hashMapRoutingAssignment);
        }else{
            salesforceAPI.update("Routing_Assignment__c",routingAssignmentQuery,"Neighborhood__c",salesforceAPI.select(neighborhoodQuery).get("Id").get(0));
        }

        //Step5: Deleting the pool assignment if it already exists
        String poolAssignmentQuery = "SELECT Id FROM Pool_Assignments__c where Work_Pool__r.name ='" + workPool + "'";
        salesforceAPI.delete("Pool_Assignments__c",poolAssignmentQuery);


        //Step6: Update the primary appraiser in the neighborhood and pool assignment should automatically be created
        ReportLogger.INFO("Updating the primary appraiser in the Neighborhood");
        objWorkItemHomePage.searchModule(modules.NEIGHBORHOODS);
        objPoolAssignmentPage.globalSearchRecords(neighborhoodWithDistrict);
        objPoolAssignmentPage.editRecord();
        objPoolAssignmentPage.clearSelectionFromLookup(objNeighborhoodsPage.primaryAppraiserDropDown);
        objPoolAssignmentPage.searchAndSelectOptionFromDropDown(objNeighborhoodsPage.primaryAppraiserDropDown,primaryAppraiser1);
        objPoolAssignmentPage.saveRecord();

        driver.navigate().refresh();
        Thread.sleep(2000);
        objPoolAssignmentPage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);
        HashMap<String, ArrayList<String>> poolAssignmentData = objPoolAssignmentPage.getGridDataInHashMap();

        //SCENARIO1: New Pool Assignment record is created if the pool assignment doesn't exist with the primary appraiser of the neighborhood
        ReportLogger.INFO("SCENARIO1: New Pool Assignment record is created if the pool assignment doesn't exist with the primary appraiser of the neighborhood");
        softAssert.assertEquals(poolAssignmentData.get("Pool Assignments Name").size(),"1","SMAB-T2602: Pool Assignment should be created as per the updated Primary Appraiser in the neighborhood");
        softAssert.assertEquals(poolAssignmentData.get("User").get(0),primaryAppraiser1,"SMAB-T2602: Primary Appraiser in the created pool assignment should be same as the primary appraiser of the updated neighborhood");
        softAssert.assertEquals(poolAssignmentData.get("Role").get(0),"Primary Appraiser","SMAB-T2602: Role should be Primary Appraiser in the created pool assignment");

        //Step8: Update the neighborhood, role should automatically updated as per the primary appraiser of the updated neighborhood
        objWorkItemHomePage.searchModule(modules.NEIGHBORHOODS);
        objPoolAssignmentPage.globalSearchRecords(neighborhoodWithDistrict);
        objPoolAssignmentPage.editRecord();
        objPoolAssignmentPage.clearSelectionFromLookup(objNeighborhoodsPage.primaryAppraiserDropDown);
        objPoolAssignmentPage.searchAndSelectOptionFromDropDown(objNeighborhoodsPage.primaryAppraiserDropDown,primaryAppraiser2);
        objPoolAssignmentPage.saveRecord();

        driver.navigate().refresh();
        Thread.sleep(2000);
        objPoolAssignmentPage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);

        //SCENARIO2: Existing User role is updated to "Staff" where user is not equal to the primary appraiser of the neighborhood
        ReportLogger.INFO("SCENARIO2: Existing User role is updated to \"Staff\" where user is not equal to the primary appraiser of the neighborhood");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataInHashMap().get("Pool Assignments Name").size(),"2","SMAB-T2602: New Pool Assignment should be created as Primary Appraiser is updated in the neighborhood");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataForRowString(primaryAppraiser1).get("Role").get(0),"Staff","SMAB-T2599: Role should be changed to Staff as neighborhood record is updated");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataForRowString(primaryAppraiser2).get("Role").get(0),"Primary Appraiser","SMAB-T2602: Role should be Primary Appraiser in the created pool assignment");

        //Step9: Change the neighborhood again, current primary appraiser should be converted to staff and no new record should be created
        objWorkItemHomePage.searchModule(modules.NEIGHBORHOODS);
        objPoolAssignmentPage.globalSearchRecords(neighborhoodWithDistrict);
        objPoolAssignmentPage.editRecord();
        objPoolAssignmentPage.clearSelectionFromLookup(objNeighborhoodsPage.primaryAppraiserDropDown);
        objPoolAssignmentPage.searchAndSelectOptionFromDropDown(objNeighborhoodsPage.primaryAppraiserDropDown,primaryAppraiser1);
        objPoolAssignmentPage.saveRecord();

        driver.navigate().refresh();
        Thread.sleep(2000);
        objPoolAssignmentPage.searchModule(modules.WORK_POOL);
        objPoolAssignmentPage.globalSearchRecords(workPool);
        objPoolAssignmentPage.openTab(objWorkItemHomePage.tabPoolAssignment);

        //SCENARIO3: Role is updated to "Primary Appraiser" where user equals to the primary appraiser of the updated neighborhood
        ReportLogger.INFO("SCENARIO3: Role is updated to \"Primary Appraiser\" where user equals to the primary appraiser of the updated neighborhood");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataInHashMap().get("Pool Assignments Name").size(),"2","SMAB-T2601: New Pool Assignment should note be created as neighborhood is updated in the routing assignment and the primary appraiser was already existing");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataForRowString(primaryAppraiser1).get("Role").get(0),"Primary Appraiser","SMAB-T2601: Role should be changed to Primary Appraiser as per the updated neighborhood");
        softAssert.assertEquals(objPoolAssignmentPage.getGridDataForRowString(primaryAppraiser2).get("Role").get(0),"Staff","SMAB-T2599: Role should be changed to Staff as neighborhood record is updated");

        //Logout of APAS Application
        objWorkItemHomePage.logout();
    }
}
