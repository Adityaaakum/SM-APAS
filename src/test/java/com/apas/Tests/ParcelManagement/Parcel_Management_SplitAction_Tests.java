package com.apas.Tests.ParcelManagement;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
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

public class Parcel_Management_SplitAction_Tests extends TestBase implements testdata, modules, users {
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
     * This method is to Verify that User is able to perform validations for "Split" mapping action for a Parcel (Active) from a work item
     * @param loginUser
     * @throws Exception
     */
    @Test(description = "SMAB-T2294, SMAB-T2295, SMAB-T2428, SMAB-T2296, SMAB-T2314, SMAB-T2613:Verify the UI validations for \"Split\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "regression","parcel_management" })
    public void ParcelManagement_VerifySplitMappingActionUIValidations(String loginUser) throws Exception {
        String apn = objMappingPage.fetchActiveAPN();

        String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
                "DataToCreateWorkItemOfTypeParcelManagement");

        String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
        Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithoutAllFields");

        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objMappingPage.login(loginUser);

        // Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn);

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
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
        objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

        //Step 6: Validating that default values of Number of Child Non-Condo Parcels and Number of Child Condo Parcels are 0
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfChildNonCondoTextBoxLabel),"value"),"0",
                "SMAB-T2294: Validation that default value of Number of Child Non-Condo Parcels  is 0");
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfChildCondoTextBoxLabel),"value"),"0",
                "SMAB-T2294: Validation that default value of Number of Child Condo Parcels  is 0");

        //Step 7: Validating that reason code field is auto populated from parent parcel work item
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
                "SMAB-T2613: Validation that reason code field is auto populated from parent parcel work item");

        //Step 8: Validating help icons
       objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
                "SMAB-T2481: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");

        objMappingPage.Click(objMappingPage.helpIconLegalDescription);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank",
                "SMAB-T2481: Validation that help text is generated on clicking the help icon for legal description");

        objMappingPage.Click(objMappingPage.helpIconSitus);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent situs, leave as blank",
                "SMAB-T2481: Validation that help text is generated on clicking the help icon for Situs text box");
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.closeButton));

        //Step 9: Validating Error Message when both Number of Child Non-Condo & Condo Parcels fields contain 0
        objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"Number of Child Non-Condo Parcels and/or Number of Child Condo parcels values total must be equivalent to two or greater.",
                "SMAB-T2294: Validation that error message is displayed when both Number of Child Non-Condo & Condo Parcels fields contain 0");


        //Step 10: entering incorrect map book in 'First Non Condo Parcel Number' & 'First Condo Parcel Number' fields
        Map<String, String> hashMapSplitActionInvalidData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithIncorrectData");
        objMappingPage.fillMappingActionForm(hashMapSplitActionInvalidData);

        //Step 11: Validating Error Message having incorrect map book data
        softAssert.assertContains(objMappingPage.getErrorMessage(),"Non Condo Parcel Number cannot start with 100 or 134, Please enter valid Parcel Number",
                "SMAB-T2428: Validation that error message is displayed when map book of First Child Non-Condo Parcel is 100");
        softAssert.assertContains(objMappingPage.getErrorMessage(),"Condo Parcel Number should start with 100 only, Please enter valid Parcel Number",
                "SMAB-T2295: Validation that error message is displayed when map book of First Child Condo Parcel is any number except 100");

        //Step 12: entering data having special characters in form for split mapping action
        Map<String, String> hashMapSplitActionSpecialCharsData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithSpecialChars");
        objMappingPage.fillMappingActionForm(hashMapSplitActionSpecialCharsData);

        //Step 13: Validating Error Message having special characters in fields: First Child Non-Condo Parcel & First Child Condo Parcel
        softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2613: Validation that error message is displayed when First Child Non-Condo Parcel & First Child Condo Parcel contains Special Characters");

        //Step 14: entering alphanumeric data in form for split mapping action
        Map<String, String> hashMapSplitActionAlphaNumData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithAlphaNumeric");
        objMappingPage.fillMappingActionForm(hashMapSplitActionAlphaNumData);

        //Step 15: Validating Error Message having alphabets in fields: First Child Non-Condo Parcel & First Child Condo Parcel
        softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2296: Validation that error message is displayed when First Child Non-Condo Parcel & First Child Condo Parcel contains alphabets");

        //Step 16: entering alphanumeric data in form for split mapping action
        Map<String, String> hashMapSplitActionDotInData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithDotInData");
        objMappingPage.fillMappingActionForm(hashMapSplitActionDotInData);

        //Step 17: Validating Error Message having dot in fields: First Child Non-Condo Parcel & First Child Condo Parcel
        softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2613: Validation that error message is displayed when First Child Non-Condo Parcel & First Child Condo Parcel contains dot");

        //Step 18: entering valid data in form for split mapping action
        Map<String, String> hashMapSplitActionValidMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithValidData");
        
        String situsCityName = hashMapSplitActionValidMappingData.get("Situs City Name");
		String direction = hashMapSplitActionValidMappingData.get("Direction");
		String situsNumber = hashMapSplitActionValidMappingData.get("Situs Number");
		String situsStreetName = hashMapSplitActionValidMappingData.get("Situs Street Name");
		String situsType = hashMapSplitActionValidMappingData.get("Situs Type");
		String situsUnitNumber = hashMapSplitActionValidMappingData.get("Situs Unit Number");
		String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;
		
		objMappingPage.editSitusModalWindowFirstScreen(hashMapSplitActionValidMappingData);
        objMappingPage.fillMappingActionForm(hashMapSplitActionValidMappingData);
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel),"value"),childprimarySitus,
				"SMAB-T2661: Validation that User is able to update a Situs for child parcels from the Parcel mapping screen for split mapping action");

        //Step 19: Verify that APNs generated must be 9-digits and should end in '0'
        HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
        String childAPNNumber =gridDataHashMap.get("APN").get(0);
        String childAPNComponents[] = childAPNNumber.split("-");
        softAssert.assertEquals(childAPNComponents.length,3,
                "SMAB-T2314: Validation that child APN number contains 3 parts: map book,map page,parcel number");
        softAssert.assertEquals(childAPNComponents[0].length(),3,
                "SMAB-T2314: Validation that MAP BOOK of child parcels contains 3 digits");
        softAssert.assertEquals(childAPNComponents[1].length(),3,
                "SMAB-T2314: Validation that MAP page of child parcels contains 3 digits");
        softAssert.assertEquals(childAPNComponents[2].length(),3,
                "SMAB-T2314: Validation that parcel number of child parcels contains 3 digits");
        softAssert.assertTrue(childAPNNumber.endsWith("0"),
                "SMAB-T2314: Validation that child APN number ends with 0");

        //Step 20: Verify total number of parcels getting generated
        int actualTotalParcels = gridDataHashMap.get("APN").size();
        int expectedTotalParcels = Integer.parseInt(hashMapSplitActionValidMappingData.get("Number of Child Non-Condo Parcels")) +
                Integer.parseInt(hashMapSplitActionValidMappingData.get("Number of Child Condo Parcels"));

        softAssert.assertEquals(actualTotalParcels,expectedTotalParcels,"SMAB-T2613: Verify total no of parcels getting generated");
        softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),childprimarySitus,
				"SMAB-T2661: Validation that System populates primary situs for child parcel from the first screen for split mapping action");

        //Step 21: Validating warning messages
        softAssert.assertContains(objMappingPage.getErrorMessage(),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is",
                "SMAB-T2613: Validation that warning message is displayed when Parcel number generated is different from the user selection");

        softAssert.assertContains(objMappingPage.getErrorMessage(),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is",
                "SMAB-T2613: Validation that warning message is displayed when Parcel number generated is different from the user selection");

        driver.switchTo().window(parentWindow);
        objWorkItemHomePage.logout();

    }
    /**
     * This method is to Verify that User is able to perform Parent APN validations for "Split" mapping action for a Parcel (Active) from a work item
     * @param loginUser
     * @throws Exception
     */
    @Test(description = "SMAB-T2313, SMAB-T2292:Verify the Parent APN validations for \"Split\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "regression","parcel_management" })
    public void ParcelManagement_VerifyParentAPNValidationsForSplitMappingAction(String loginUser) throws Exception {
        String apn = objMappingPage.fetchActiveAPN();

        String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
                "DataToCreateWorkItemOfTypeParcelManagement");

        String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
        Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithoutAllFields");

        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objMappingPage.login(loginUser);

        // Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn);

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
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
        objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

        //Step 6: Edit Parent APN, enter Retired APN  and Verify Error Message
        // fetching  parcel that is retired
        String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
        HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
        String retiredAPNValue= response.get("Name").get(0);

        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
                "SMAB-T2313: Validation that proper error message is displayed if parent parcel is retired");

        //Step 7: Edit Parent APN, enter In-Progress APN and Verify Error Message
        // fetching  parcel that is In progress
        String inProgressAPNValue= objMappingPage.fetchInProgressAPN();

        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
                "SMAB-T2313: Validation that proper error message is displayed if parent parcel is in progress status");

        //Step 7: Enter more than one Parent APNs and Verify Error message
        ArrayList<String> APNs=objMappingPage.fetchActiveAPN(2);
        String activeParcelToPerformMapping=APNs.get(0);
        String activeParcelToPerformMapping2=APNs.get(1);

        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
        objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,activeParcelToPerformMapping + ","+ activeParcelToPerformMapping2);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"-Split process should have exactly one parent Apn",
                "SMAB-T2292: Validation that proper error message is displayed if parent parcel is in progress status");

        driver.switchTo().window(parentWindow);
        objWorkItemHomePage.logout();

    }
    /**
     * This method is to Verify that User is able to perform manual overwrite validations for "Split" mapping action for a Parcel (Active) from a work item
     * @param loginUser
     * @throws Exception
     */
    @Test(description = "SMAB-T2296, SMAB-T2613:Verify the UI validations for \"Split\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "regression","parcel_management" })
    public void ParcelManagement_VerifySplitMappingActionManualOverwriteValidations(String loginUser) throws Exception {
       String apn =  objMappingPage.fetchActiveAPN();

        String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
                "DataToCreateWorkItemOfTypeParcelManagement");

        String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;

        // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
        objMappingPage.login(loginUser);

        // Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn);

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

        //Step 6: entering valid data in form for split mapping action
        Map<String, String> hashMapSplitActionValidMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithValidData");
        objMappingPage.fillMappingActionForm(hashMapSplitActionValidMappingData);

        //Step 7: Verify total number of parcels getting generated
        HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
        int actualTotalParcels = gridDataHashMap.get("APN").size();
        int expectedTotalParcels = Integer.parseInt(hashMapSplitActionValidMappingData.get("Number of Child Non-Condo Parcels")) +
                Integer.parseInt(hashMapSplitActionValidMappingData.get("Number of Child Condo Parcels"));

        softAssert.assertEquals(actualTotalParcels,expectedTotalParcels,"Verify total no of parcels getting generated");

        //Step 8: Validating error message when APN entered is not next available number
        //Considering Map Book & Map Page from the APN generated by system
        // and adding 100 to parcel number will become new APN which is not available next as per system
        String childAPNNumber =gridDataHashMap.get("APN").get(0);
        String childAPNComponents[] = childAPNNumber.split("-");
        String apnNotNextAvailable = childAPNComponents[0] + childAPNComponents[1] +
                String.valueOf(Integer.parseInt(childAPNComponents[2]) + 100);

        objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,apnNotNextAvailable);
        objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.splitParcelButton));

        gridDataHashMap =objMappingPage.getGridDataInHashMap();
        softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"The parcel entered is invalid since the following parcel is available",
                "SMAB-T2613: Validation that error message is displayed when parcel entered is not next abvaialble");

        //Step 9: Validating error message when APN entered contains alphabets
        //Considering Map Book & Map Page from the APN generated by system
        // and adding alphabets to parcel number
        String apnContainingAlphabets = childAPNComponents[0] + childAPNComponents[1] + "abc";

        objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,apnContainingAlphabets);
        objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.splitParcelButton));

        gridDataHashMap =objMappingPage.getGridDataInHashMap();
        softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2296: Validation that error message is displayed when parcel entered contains alphabets");

        //Step 10: Validating error message when APN entered contains special characters
        //Considering Map Book & Map Page from the APN generated by system
        // and adding special characters to parcel number
        String apnContainingspecialChars = childAPNComponents[0] + childAPNComponents[1] + "45&";

        objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,apnContainingspecialChars);
        objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.splitParcelButton));

        gridDataHashMap =objMappingPage.getGridDataInHashMap();
        softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2613: Validation that error message is displayed when parcel entered contains special characters");

        //Step 10: Validating error message when APN entered already exists in the system
        String alreadyExistingApn = apn;

        objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,alreadyExistingApn);
        objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.splitParcelButton));

        gridDataHashMap =objMappingPage.getGridDataInHashMap();
        softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"The APN provided already exists in the system",
                "SMAB-T2613: Validation that error message is displayed when parcel entered already exists in the system");

        driver.switchTo().window(parentWindow);
        objWorkItemHomePage.logout();

    }
}