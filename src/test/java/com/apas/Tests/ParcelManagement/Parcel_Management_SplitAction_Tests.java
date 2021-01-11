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
     * This method is to Verify that User is able to perform a "One to One" mapping action for a Parcel (Active) of type Non Condo from a work item
     * @param loginUser
     * @throws Exception
     */
    @Test(description = "SMAB-T2482:Verify that User is able to perform the \"Split\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "regression","parcel_management" })
    public void ParcelManagement_VerifySplitMappingActionParcel(String loginUser) throws Exception {
        String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 1";
        HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
        String apn = responseAPNDetails.get("Name").get(0);

        String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
        HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

        String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
        HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

        HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

        String primarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ apn +"'").get("Name").get(0);
        String legalDescriptionValue=salesforceAPI.select("SELECT Short_Legal_Description__c FROM Parcel__c where Short_Legal_Description__c !=NULL limit 1").get("Short_Legal_Description__c").get(0);
        String districtValue=salesforceAPI.select("SELECT District__c FROM Parcel__c where District__c !=Null limit 1").get("District__c").get(0);

        jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
        jsonObject.put("Status__c","Active");
        jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
        jsonObject.put("District__c",districtValue);
        jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
        jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));

        salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);

        String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
                "DataToCreateWorkItemOfTypeParcelManagement");

        String mappingActionCreationData = System.getProperty("user.dir") + testdata.SPLIT_MAPPING_ACTION;
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

        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
        objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

        //Step 5: Validating that default values of Number of Child Non-Condo Parcels and Number of Child Condo Parcels are 0
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfChildNonCondoTextBoxLabel),"value"),"0",
                "SMAB-T2481: Validation that default value of Number of Child Non-Condo Parcels  is 0");
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfChildCondoTextBoxLabel),"value"),"0",
                "SMAB-T2481: Validation that default value of Number of Child Condo Parcels  is 0");

        //Step 6: Validating that reason code field is auto populated from parent parcel work item
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
                "SMAB-T2481: Validation that reason code field is auto populated from parent parcel work item");

        //Step 7: Validating help icons
        objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
                "SMAB-T2481: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");
        objMappingPage.Click(objMappingPage.helpIconLegalDescription);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank.",
                "SMAB-T2481: Validation that help text is generated on clicking the help icon for legal description");
        objMappingPage.Click(objMappingPage.helpIconSitus);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent situs, leave as blank.",
                "SMAB-T2481: Validation that help text is generated on clicking the help icon for Situs text box");

        //Step 7: Validating Error Message when both Number of Child Non-Condo & Condo Parcels fields contain 0
        objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
        softAssert.assertEquals(objMappingPage.getErrorMessage(),"Please populate either or both 'Number of Child Non-Condos' or 'Number of Child Condos' field (s)",
                "SMAB-T2490: Validation that error message is displayed when both Number of Child Non-Condo & Condo Parcels fields contain 0");


        //Step 8: entering data in form for Split mapping action
        objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);
        HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

        //Step 9: Validating Error Message having incorrect map book data
        softAssert.assertEquals(objMappingPage.getErrorMessage(),"Non Condo Parcel Number cannot start with 100 or 134, Please enter valid Parcel Number",
                "SMAB-T2490: Validation that error message is displayed when map book of First Child Non-Condo Parcel is 100");
        softAssert.assertEquals(objMappingPage.getErrorMessage(),"Condo Parcel Number should start with 100 only, Please enter valid Parcel Number",
                "SMAB-T2490: Validation that error message is displayed when map book of First Child Condo Parcel is any number except 100");

        //Step 10: entering data in form for split mapping action
        hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithSpecialChars");
        objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);

        //Step 11: Validating Error Message having special characters in fields: First Child Non-Condo Parcel & First Child Condo Parcel
        softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2490: Validation that error message is displayed when First Child Non-Condo Parcel & First Child Condo Parcel contains Special Characters");

        //Step 12: entering data in form for split mapping action
        hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithAlphaNumeric");
        objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);

        //Step 13: Validating Error Message having alphabets in fields: First Child Non-Condo Parcel & First Child Condo Parcel
        softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2490: Validation that error message is displayed when First Child Non-Condo Parcel & First Child Condo Parcel contains alphabets");

        //Step 14: entering data in form for split mapping action
        hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithAlphaNumeric");
        objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);

        //Step 15: Validating Error Message having dot in fields: First Child Non-Condo Parcel & First Child Condo Parcel
        softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
                "SMAB-T2490: Validation that error message is displayed when First Child Non-Condo Parcel & First Child Condo Parcel contains alphabets");

        //Step 16: entering valid data in form for split mapping action
        hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToPerformSplitMappingActionWithValidData");
        objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);

        //Step 17: Verify that APNs generated must be 9-digits and should end in '0'
        String childAPNNumber =gridDataHashMap.get("APN").get(0);
        String childAPNComponents[] = childAPNNumber.split("-");
        softAssert.assertEquals(childAPNComponents.length,3,
                "SMAB-T2488: Validation that child APN number contains 3 parts: map book,map page,parcel number");
        softAssert.assertEquals(childAPNComponents[0].length(),3,
                "SMAB-T2488: Validation that MAP BOOK of child parcels contains 3 digits");
        softAssert.assertEquals(childAPNComponents[1].length(),3,
                "SMAB-T2488: Validation that MAP page of child parcels contains 3 digits");
        softAssert.assertEquals(childAPNComponents[2].length(),3,
                "SMAB-T2488: Validation that parcel number of child parcels contains 3 digits");
        softAssert.assertTrue(childAPNNumber.endsWith("0"),
                "SMAB-T2488: Validation that child APN number ends with 0");

        //Step 18: Validation of ALL fields THAT ARE displayed on second screen
        softAssert.assertEquals(gridDataHashMap.get("District").get(0),districtValue,
                "SMAB-T2481: Validation that  System populates District  from the parent parcel");
        softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),primarySitusValue.replaceFirst("\\s", ""),"SMAB-T2481: Validation that  System populates Situs  from the parent parcel");
        softAssert.assertEquals(gridDataHashMap.get("Neighborhood Code").get(0),responseNeighborhoodDetails.get("Name").get(0),
                "SMAB-T2481: Validation that  System populates neighborhood Code from the parent parcel");
        softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
                "SMAB-T2481: Validation that  System populates reason code from before screen");
        softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescriptionValue,
                "SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");
        softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
                "SMAB-T2481: Validation that  System populates TRA from the parent parcel");
        softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),responsePUCDetails.get("Name").get(0),
                "SMAB-T2481: Validation that  System populates Use Code  from the parent parcel");

        //Step 11 :Verify that User is able to to create a district, Use Code for the child parcel from the custom screen after performing one to one mapping action
        objMappingPage.editGridCellValue(objMappingPage.districtColumnSecondScreen,"Distrct 001");
        objMappingPage.editGridCellValue(objMappingPage.useCodeColumnSecondScreen,"001vacant");

        //Step 13 :Clicking generate parcel button
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
        gridDataHashMap =objMappingPage.getGridDataInHashMap();

        //Step 14: Validation of ALL fields THAT ARE displayed AFTER PARCEL ARE GENERATED
        softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),primarySitusValue.replaceFirst("\\s", ""),
                "SMAB-T2481: Validation that  System populates Situs  from the parent parcel");
        softAssert.assertEquals(gridDataHashMap.get("Neighborhood Code").get(0),responseNeighborhoodDetails.get("Name").get(0),
                "SMAB-T2481: Validation that  System populates neighborhood Code from the parent parcel");
        softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
                "SMAB-T2481: Validation that  System populates reason code from parent parcel work item");
        softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescriptionValue,
                "SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");
        softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
                "SMAB-T2481: Validation that  System populates TRA from the parent parcel");
        softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),"001vacant",
                "SMAB-T2486: Verify that User is able to to create a Use Code for the child parcel from the custom screen ");
        softAssert.assertEquals(gridDataHashMap.get("District").get(0),"Distrct 001",
                "SMAB-T2486: Verify that User is able to to create a  district for the child parcel from the custom screen ");

        driver.switchTo().window(parentWindow);
        objWorkItemHomePage.logout();

    }
}
