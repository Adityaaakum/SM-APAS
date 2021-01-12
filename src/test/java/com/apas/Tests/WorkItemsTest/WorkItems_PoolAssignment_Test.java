package com.apas.Tests.WorkItemsTest;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.PoolAssignmentPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.PageObjects.WorkPoolPage;
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

public class WorkItems_PoolAssignment_Test extends TestBase {
    RemoteWebDriver driver;

    PoolAssignmentPage objPoolAssignmentPage;
    ParcelsPage objParcelsPage;
    WorkItemHomePage objWorkItemHomePage;
    WorkPoolPage objWorkPoolPage;
    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI salesforceAPI = new SalesforceAPI();
    Util objUtil = new Util();

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();

        objParcelsPage = new ParcelsPage(driver);
        objPoolAssignmentPage = new PoolAssignmentPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objWorkPoolPage = new WorkPoolPage(driver);
    }

    @Test(description = "SMAB-T2307: Verify that the Warning message is not appearing for the Closed or No opened WI on removing the User from PA", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression", "Work_Items_PoolAssignment"}, alwaysRun = true)
    public void WorkItem_PoolAssignment_NoWarningOnRemovingStaff(String loginUser) throws Exception {

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
    public void WorkItem_PoolAssignment_WarningOnRemovingStaff(String loginUser) throws Exception {

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

}
