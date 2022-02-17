package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.config.modules;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class WorkItemAdministration_Configuration_Test extends TestBase {
    RemoteWebDriver driver;

    WorkItemHomePage objWorkItemHomePage;
    ParcelsPage objParcelsPage;
    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI salesforceAPI = new SalesforceAPI();

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();

        objParcelsPage = new ParcelsPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
    }

    @Test(description = "SMAB-T1782: Validation the Work Item Type and Action based on work item configuration for Parcels", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Regression","WorkItemAdministration"}, alwaysRun = true)
    public void WorkItemAdministration_Configuration_ManualWorkItemCreation_Parcels(String loginUser) throws Exception {

        String apnValue = objParcelsPage.fetchActiveAPN();

        String queryWorkItemType = "SELECT work_item_type__C FROM Work_Item_Configuration__c where Ad_Hoc_Creation_Allowed__c = 'Yes' and roll_code__c = 'SEC'";
        HashMap<String, ArrayList<String>> workItemTypesHashMap = salesforceAPI.select(queryWorkItemType);

        String workItemType = workItemTypesHashMap.get("Work_Item_Type__c").get(0);
        String queryActions = "SELECT work_item_sub_type__C FROM Work_Item_Configuration__c where Ad_Hoc_Creation_Allowed__c = 'Yes' and roll_code__c = 'SEC' and work_item_type__C='" + workItemType + "'";
        HashMap<String, ArrayList<String>> actionsHashMap = salesforceAPI.select(queryActions);

        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objWorkItemHomePage.login(loginUser);

        // Step2: Opening the PARCELS page and searching a parcel
        objWorkItemHomePage.searchModule(modules.PARCELS);
        objWorkItemHomePage.globalSearchRecords(apnValue);

        //Step3: Opening the manual work item creation window
        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.componentActionsButtonText));
        objWorkItemHomePage.waitForElementToBeClickable(objParcelsPage.selectOptionDropDownComponentsActionsModal);
        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.nextButtonComponentsActionsModal));
        objWorkItemHomePage.waitForElementToBeClickable(objParcelsPage.workItemTypeDropDownComponentsActionsModal);

        //Validating the mandatory field error
        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.nextButtonComponentsActionsModal));
        String expectedDescriptionError = "Please enter some valid input. Input is not optional.";
        String expectedWorkItemTypeAndSubTypeError = "Please enter some valid inputs for Work Item Type and Sub Type. Inputs are not optional.";
        softAssert.assertEquals(objWorkItemHomePage.getElementText(objParcelsPage.descriptionError),expectedDescriptionError,"SMAB-T1804 : Validation for mandatory field error on description field");
        softAssert.assertEquals(objWorkItemHomePage.getElementText(objParcelsPage.workItemTypeAndSubTypeError),expectedWorkItemTypeAndSubTypeError,"SMAB-T1804 : Validation for mandatory field error on Work Item Type and Actions field");

        //Selecting the work item type
        objWorkItemHomePage.selectOptionFromDropDown(objParcelsPage.workItemTypeDropDownComponentsActionsModal, workItemType);

        //Step4: Work Item Type values validation for manual work item for Parcels. Stream is used because Salesforce is not able to return distinct values from the query
        ArrayList<String> types = new ArrayList<>();
		workItemTypesHashMap.get("Work_Item_Type__c").stream().distinct().sorted().forEach(type -> types.add(type));
		Collections.swap(types, 10, 11);
		String expectedWorkItemTypeValues = types.toString().replace(", ","\n").replace("[","").replace("]","");
		String actualWorkItemTypeValues = objWorkItemHomePage.getDropDownValue(objParcelsPage.workItemTypeDropDownComponentsActionsModal);
		softAssert.assertEquals(actualWorkItemTypeValues,expectedWorkItemTypeValues,"SMAB-T1782: Work Item Type values validation based on the 'roll code= SEC' and 'Manual Work item Creation flag=Yes'");

		//Step4: Work Item Type values validation for manual work item for Parcels
        ArrayList<String> subTypes = new ArrayList<>();
        actionsHashMap.get("Work_Item_Sub_Type__c").stream().distinct().sorted().forEach(subType -> subTypes.add(subType));
		String expectedActionsValues = subTypes.toString().replace(", ","\n").replace("[","").replace("]","");
		String actualActionsValues = objWorkItemHomePage.getDropDownValue(objParcelsPage.actionsDropDownLabel);
		softAssert.assertEquals(actualActionsValues,expectedActionsValues,"SMAB-T1782: Actions values validation based on the 'roll code= SEC' and 'Manual Work item Creation flag=Yes'");

        //Logout of APAS Application
        objWorkItemHomePage.logout();
    }

    @Test(description = "SMAB-T1783: Validation the Work Item Type and Action based on work item configuration for BPP Accounts", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Regression", "WorkItemAdministration"}, alwaysRun = true,enabled = false)
    public void WorkItemAdministration_Configuration_ManualWorkItemCreation_BPPAccounts(String loginUser) throws Exception {

        String queryBPPAccountValue = "SELECT Name FROM BPP_Account__c where Status__C = 'Active' limit 1";
        String BPPAccountValue= salesforceAPI.select(queryBPPAccountValue).get("Name").get(0);

        String queryWorkItemType = "SELECT work_item_type__C FROM Work_Item_Configuration__c where Ad_Hoc_Creation_Allowed__c = 'Yes' and roll_code__c = 'UNS'";
        HashMap<String, ArrayList<String>> workItemTypesHashMap = salesforceAPI.select(queryWorkItemType);

        String workItemType = workItemTypesHashMap.get("Work_Item_Type__c").get(0);
        String queryActions = "SELECT work_item_sub_type__C FROM Work_Item_Configuration__c where Ad_Hoc_Creation_Allowed__c = 'Yes' and roll_code__c = 'UNS' and work_item_type__C='" + workItemType + "'";
        HashMap<String, ArrayList<String>> actionsHashMap = salesforceAPI.select(queryActions);

        // Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objWorkItemHomePage.login(loginUser);

        // Step2: Opening the BPP Account page and searching a BPP Account
        objWorkItemHomePage.searchModule(modules.BPP_ACCOUNTS);
        objWorkItemHomePage.globalSearchRecords(BPPAccountValue);

        //Step3: Opening the manual work item creation window
        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.componentActionsButtonText));
        objWorkItemHomePage.waitForElementToBeClickable(objParcelsPage.selectOptionDropDownComponentsActionsModal);
        objWorkItemHomePage.selectOptionFromDropDown(objParcelsPage.selectOptionDropDownComponentsActionsModal, "Create Work Item");
        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.nextButtonComponentsActionsModal));
        objWorkItemHomePage.waitForElementToBeClickable(objParcelsPage.workItemTypeDropDownComponentsActionsModal);

        //Validating the mandatory field error
        objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.nextButtonComponentsActionsModal));
        String expectedDescriptionError = "Please enter some valid input. Input is not optional.";
        String expectedWorkItemTypeAndSubTypeError = "Please enter some valid inputs for Work Item Type and Sub Type. Inputs are not optional.";
        softAssert.assertEquals(objWorkItemHomePage.getElementText(objParcelsPage.descriptionError),expectedDescriptionError,"SMAB-T1804 : Validation for mandatory field error on description field");
        softAssert.assertEquals(objWorkItemHomePage.getElementText(objParcelsPage.workItemTypeAndSubTypeError),expectedWorkItemTypeAndSubTypeError,"SMAB-T1804 : Validation for mandatory field error on Work Item Type and Actions field");

        //Selecting the work item type
        objWorkItemHomePage.selectOptionFromDropDown(objParcelsPage.workItemTypeDropDownComponentsActionsModal, workItemType);

        //Step4: Work Item Type values validation for manual work item for BPP Accounts. Stream is used because Salesforce is not able to return distinct values from the query
        ArrayList<String> types = new ArrayList<>();
        workItemTypesHashMap.get("Work_Item_Type__c").stream().distinct().sorted().forEach(type -> types.add(type));
        String expectedWorkItemTypeValues = types.toString().replace(", ","\n").replace("[","").replace("]","");
        String actualWorkItemTypeValues = objWorkItemHomePage.getDropDownValue(objParcelsPage.workItemTypeDropDownComponentsActionsModal);
        softAssert.assertEquals(actualWorkItemTypeValues,expectedWorkItemTypeValues,"SMAB-T1783: Work Item Type values validation based on the 'roll code= SEC' and 'Manual Work item Creation flag=Yes'");

        //Step4: Work Item Type values validation for manual work item for Parcels
        ArrayList<String> subTypes = new ArrayList<>();
        actionsHashMap.get("Work_Item_Sub_Type__c").stream().distinct().sorted().forEach(subType -> subTypes.add(subType));
        String expectedActionsValues = subTypes.toString().replace(", ","\n").replace("[","").replace("]","");
        String actualActionsValues = objWorkItemHomePage.getDropDownValue(objParcelsPage.actionsDropDownLabel);
        softAssert.assertEquals(actualActionsValues,expectedActionsValues,"SMAB-T1783: Actions values validation based on the 'roll code= SEC' and 'Manual Work item Creation flag=Yes'");

        //Logout of APAS Application
        objWorkItemHomePage.logout();
    }
}
