package com.apas.Tests.ParcelManagement;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Parcel_Management_ManyToManyAction_Tests extends TestBase implements testdata, modules, users {
    private RemoteWebDriver driver;

    ParcelsPage objParcelsPage;
    WorkItemHomePage objWorkItemHomePage;
    Util objUtil = new Util();
    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI salesforceAPI = new SalesforceAPI();
    MappingPage objMappingPage;
    JSONObject jsonObject= new JSONObject();
    String apnPrefix=new String();

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {
        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();
        objParcelsPage = new ParcelsPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objMappingPage= new MappingPage(driver);

    }
    /**
     * This method is to Verify that User is able to perform validations for "ManyToMany" mapping action for a Parcel (Active) from a work item
     * @param loginUser
     * @throws Exception
     */
    @Test(description = "SMAB-T2583, SMAB-T2585, SMAB-T2581, SMAB-T2586:Verify the UI validations for \"Split\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "regression","parcel_management" })
    public void ParcelManagement_VerifyManyToManyMappingActionUIValidations(String loginUser) throws Exception {
        //Fetching Assessee records
        String queryAssesseeRecord = "SELECT Id, Name FROM Account Limit 1";
        HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
        String assesseeName = responseAssesseeDetails.get("Name").get(0);

        //Fetching parcels that are Active with same Ownership record
        String queryAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName + "') and (Not Name like '%990') and Status__c = 'Active' Limit 2";
        HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
        String apn1=responseAPNDetails.get("Name").get(0);
        String apn2=responseAPNDetails.get("Name").get(1);

        String concatenateAPNWithSameOwnership = apn1+","+apn2;

        String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
                "DataToCreateWorkItemOfTypeParcelManagement");

        String mappingActionCreationData = testdata.MANY_TO_MANY_MAPPING_ACTION;
        Map<String, String> hashMapManyToManyActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformManyToManyMappingActionWithoutAllFields");

        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objMappingPage.login(loginUser);

        // Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn1);

        // Step 3: Creating Manual work item for the Parcel
        objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

        //Step 4: Clicking the  details tab for the work item newly created and clicking on Related Action Link
        objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
        objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
        String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
        objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
        String parentWindow = driver.getWindowHandle();
        objWorkItemHomePage.switchToNewWindow(parentWindow);
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

        // Step 5: Update the Parent APN field and add another parcel with same ownership record
        ReportLogger.INFO("Add a parcel in Parent APN field with same ownership record :: " + apn1 + ", " + apn2);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

        //Step 6: Selecting Action as 'Many To Many' & Taxes Paid fields value as 'N/A'
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapManyToManyActionMappingData.get("Action"));
        objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"N/A");
        softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objMappingPage.reasonCodeTextBoxLabel)),"SMAB-T2583: Validation that all fields are nor visible when 'Taxes Paid Dropdown' is selected as 'N/A'");

        //Step 7: Selecting Action as 'Many To Many' & Taxes Paid fields value as 'NO'
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapManyToManyActionMappingData.get("Action"));
        objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"No");
        softAssert.assertContains(objMappingPage.getErrorMessage(),"Taxes must be fully paid in order to perform any action",
                "SMAB-T2583: Validation that error message is displayed when 'Taxes Paid Dropdown' is selected as 'No'");

        //Step 8: Selecting Action as 'Many To Many' & Taxes Paid fields value as 'Yes'
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapManyToManyActionMappingData.get("Action"));
        objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

        //Step 9: Validating that default value of Number of Child Non-Condo Parcels, Net Land Loss & Net Land Gain are 0
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfChildNonCondoTextBoxLabel),"value"),"0",
                "SMAB-T2581: Validation that default value of Number of Child Non-Condo Parcels  is 0");
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandLossTextBoxLabel),"value"),"0",
                "SMAB-T2581: Validation that default value of Net Land Loss is 0");
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandGainTextBoxLabel),"value"),"0",
                "SMAB-T2581: Validation that default value of Net Land Gain is 0");

        //Step 10: Validating that reason code field is auto populated from parent parcel work item
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
                "SMAB-T2581: Validation that reason code field is auto populated from parent parcel work item");

        //Step 11: Validating help icons
        objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
                "SMAB-T2581: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");

        objMappingPage.Click(objMappingPage.helpIconLegalDescription);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank",
                "SMAB-T2581: Validation that help text is generated on clicking the help icon for legal description");

        objMappingPage.Click(objMappingPage.helpIconSitus);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent situs, leave as blank",
                "SMAB-T2581: Validation that help text is generated on clicking the help icon for Situs text box");
        objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.closeButton));

        //Step 12: Validating Error Message when Number of Child Non-Condo Parcels field contains 0
        objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"Number of Child Non Condo Parcels\" must be greater than 1",
                "SMAB-T2583: Validation that error message is displayed when both Number of Child Non-Condo & Condo Parcels fields contain 0");

        //Step 13: entering incorrect map book in 'First Non Condo Parcel Number' field
        Map<String, String> hashMapManyToManyActionInvalidData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformManyToManyMappingActionWithIncorrectData");
        objMappingPage.fillMappingActionForm(hashMapManyToManyActionInvalidData);

        //Step 14: Validating Error Message having incorrect map book data
        softAssert.assertContains(objMappingPage.getErrorMessage(),"Non Condo Parcel Number cannot start with 100, Please enter valid Parcel Number",
                "SMAB-T2585: Validation that error message is displayed when map book of First Child Non-Condo Parcel is 100");

        //Step 15: entering data having special characters in form for Many To Many mapping action
        Map<String, String> hashMapManyToManyActionSpecialCharsData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformManyToManyMappingActionWithSpecialChars");
        objMappingPage.fillMappingActionForm(hashMapManyToManyActionSpecialCharsData);

        //Step 16: Validating Error Message having special characters in fields: First Child Non-Condo Parcel
        softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2583: Validation that error message is displayed when First Child Non-Condo Parcel contains Special Characters");

        //Step 17: entering alphanumeric data in form for Many To Many mapping action
        Map<String, String> hashMapManyToManyActionAlphaNumData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformManyToManyMappingActionWithAlphaNumeric");
        objMappingPage.fillMappingActionForm(hashMapManyToManyActionAlphaNumData);

        //Step 18: Validating Error Message having alphabets in fields: First Child Non-Condo Parcel
        softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2583: Validation that error message is displayed when First Child Non-Condo Parcel contains alphabets");

        //Step 19: entering alphanumeric data in form for Many To Many mapping action
        Map<String, String> hashMapManyToManyActionDotInData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformManyToManyMappingActionWithDotInData");
        objMappingPage.fillMappingActionForm(hashMapManyToManyActionDotInData);

        //Step 20: Validating Error Message having dot in fields: First Child Non-Condo Parcel
        softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2583: Validation that error message is displayed when First Child Non-Condo Parcel contains dot");

        //Step 21: entering valid data in form for ManyToMany mapping action
        Map<String, String> hashMapManyToManyActionValidMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformManyToManyMappingActionWithValidData");
        objMappingPage.fillMappingActionForm(hashMapManyToManyActionValidMappingData);

        //Step 22: Verify that APNs generated must be 9-digits and should end in '0'
        HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
        String childAPNNumber1 =gridDataHashMap.get("APN").get(0);
        String childAPN1Components[] = childAPNNumber1.split("-");
        softAssert.assertEquals(childAPN1Components.length,3,
                "SMAB-T2586: Validation that child APN number contains 3 parts: map book,map page,parcel number");
        softAssert.assertEquals(childAPN1Components[0].length(),3,
                "SMAB-T2586: Validation that MAP BOOK of child parcels contains 3 digits");
        softAssert.assertEquals(childAPN1Components[1].length(),3,
                "SMAB-T2586: Validation that MAP page of child parcels contains 3 digits");
        softAssert.assertEquals(childAPN1Components[2].length(),3,
                "SMAB-T2586: Validation that parcel number of child parcels contains 3 digits");
        softAssert.assertTrue(childAPNNumber1.endsWith("0"),
                "SMAB-T2586: Validation that child APN number ends with 0");

        String childAPNNumber2 =gridDataHashMap.get("APN").get(1);
        String childAPN2Components[] = childAPNNumber2.split("-");
        softAssert.assertEquals(childAPN2Components.length,3,
                "SMAB-T2586: Validation that child APN number contains 3 parts: map book,map page,parcel number");
        softAssert.assertEquals(childAPN2Components[0].length(),3,
                "SMAB-T2586: Validation that MAP BOOK of child parcels contains 3 digits");
        softAssert.assertEquals(childAPN2Components[1].length(),3,
                "SMAB-T2586: Validation that MAP page of child parcels contains 3 digits");
        softAssert.assertEquals(childAPN2Components[2].length(),3,
                "SMAB-T2586: Validation that parcel number of child parcels contains 3 digits");
        softAssert.assertTrue(childAPNNumber2.endsWith("0"),
                "SMAB-T2586: Validation that child APN number ends with 0");

        //Step 23: Verify total number of parcels getting generated
        int actualTotalParcels = gridDataHashMap.get("APN").size();
        int expectedTotalParcels = Integer.parseInt(hashMapManyToManyActionValidMappingData.get("Number of Child Non-Condo Parcels"));

        softAssert.assertEquals(actualTotalParcels,expectedTotalParcels,"SMAB-T2586: Verify total no of parcels getting generated");

        //Step 24: Validating warning message
        softAssert.assertContains(objMappingPage.getErrorMessage(),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is",
                "SMAB-T2586: Validation that warning message is displayed when Parcel number generated is different from the user selection");

        driver.switchTo().window(parentWindow);
        objWorkItemHomePage.logout();
    }

    /**
     * This method is to Verify that User is able to perform Parent APN validations for "ManyToMany" mapping action for a Parcel (Active) from a work item
     * @param loginUser
     * @throws Exception
     */
    @Test(description = "SMAB-T2587, SMAB-T2594, SMAB-T2595, SMAB-T2596, SMAB-T2626, SMAB-T2582:Verify the Parent APN validations for \"Many To Many\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "regression","parcel_management" })
    public void ParcelManagement_VerifyParentAPNValidationsForManyToManyMappingAction(String loginUser) throws Exception {
        //Fetching Assessee records
        String queryAssesseeRecord = "SELECT Id, Name FROM Account Limit 1";
        HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
        String assesseeName = responseAssesseeDetails.get("Name").get(0);

        //Fetching parcels that are Active with different Ownership record
        String queryAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') and (Not Name like '%990') and (Not Name like '134%') and Status__c = 'Active' Limit 1";
        HashMap<String, ArrayList<String>> responseAPNDetails1 = salesforceAPI.select(queryAPNValue);
        String apn1=responseAPNDetails1.get("Name").get(0);

        queryAPNValue = "SELECT Name, Id from parcel__c where Id Not in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') and (Not Name like '%990') and (Not Name like '134%') and Status__c = 'Active' Limit 1";
        HashMap<String, ArrayList<String>> responseAPNDetails2 = salesforceAPI.select(queryAPNValue);
        String apn2=responseAPNDetails2.get("Name").get(0);

        String concatenateAPNWithDifferentOwnership = apn1+","+apn2;

        String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
                "DataToCreateWorkItemOfTypeParcelManagement");

        String mappingActionCreationData = testdata.MANY_TO_MANY_MAPPING_ACTION;
        Map<String, String> hashMapManyToManyActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformManyToManyMappingActionWithoutAllFields");

        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objMappingPage.login(loginUser);

        // Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn1);

        // Step 3: Creating Manual work item for the Parcel
        objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

        //Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
        objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
        objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
        String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
        objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
        String parentWindow = driver.getWindowHandle();
        objWorkItemHomePage.switchToNewWindow(parentWindow);

        //Step 5: Selecting Action & Taxes Paid fields values
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapManyToManyActionMappingData.get("Action"));
        objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

        //Step 6: Edit Parent APN, enter APN with different ownerships and Verify Error Message
        // fetching  parcel that is retired
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithDifferentOwnership);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"-In order to proceed with a parcel \"Many To Many\" action, the parent APN must have the same ownership and ownership allocation.",
                "SMAB-T2596: Validation that proper error message is displayed if parcels are of different ownership");
        softAssert.assertContains(objMappingPage.getErrorMessage(),"-Warning: TRAs of the \"Many to Many\" parent parcels are different.",
                "SMAB-T2594: Validation that proper warning message is displayed if parcels are of different TRAs");

        //Step 7: Edit Parent APN, enter Retired APN  and Verify Error Message
        // fetching  parcel that is retired
        queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
        HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
        String retiredAPNValue= response.get("Name").get(0);

        String concatenateRetiredAPN = apn1+","+retiredAPNValue;

        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateRetiredAPN);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
                "SMAB-T2587: Validation that proper error message is displayed if parent parcel is retired");

        //Step 7: Edit Parent APN, enter In-Progress APN and Verify Error Message
        // fetching  parcel that is In progress
        String inProgressAPNValue= objMappingPage.fetchInProgressAPN();
        String concatenateinProgressAPN = apn1+","+inProgressAPNValue;

        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateinProgressAPN);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
                "SMAB-T2587: Validation that proper error message is displayed if parent parcel is in progress status");

        //Step 8: Enter only one Parent APNs and Verify Error message
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn1);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"In order to continue with the \"Many to Many\" action please provide more than one \"Parent Parcel\".",
                "SMAB-T2626: Validation that proper error message is displayed if only one parent APN is given for many to many");

        //Step 9: Edit Parent APN, enter APN starting with 134 and Verify Error Message
        queryAPNValue = "SELECT Name from parcel__c where Name like '134%' limit 1";
        response = salesforceAPI.select(queryAPNValue);
        String aPNValue= response.get("Name").get(0);

        String concatenateAPNStartingWith134 = apn1+","+aPNValue;

        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNStartingWith134);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"In order to proceed with this action, the parent parcel(s) cannot start with 134.",
                "SMAB-T2595: Validation that proper error message is displayed if any of the parent parcel starts with 134");

        //Step 10: Edit Parent APN without hyphen and Verify hyphen is added to APN after saving
        //Fetching parcels that are Active with same Ownership record
        queryAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName + "') and (Not Name like '%990') and (Not Name like '134%') and Status__c = 'Active' Limit 2";
        HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
        String activeAPN1=responseAPNDetails.get("Name").get(0);
        String activeAPN2=responseAPNDetails.get("Name").get(1);

        String concatenateAPNWithoutHyphen = activeAPN1.replace("-","") +","+activeAPN2.replace("-","");

        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithoutHyphen);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
        objMappingPage.waitForElementToBeClickable(20,objMappingPage.parentAPNFieldValue);
        softAssert.assertTrue(objMappingPage.getElementText(objMappingPage.parentAPNFieldValue).split(",")[0].contains("-"),"SMAB-T2582: Verify that when 9 digit APN is entered without hyphen, after saving hyphen is added automatically");


        driver.switchTo().window(parentWindow);
        objWorkItemHomePage.logout();

    }


}
