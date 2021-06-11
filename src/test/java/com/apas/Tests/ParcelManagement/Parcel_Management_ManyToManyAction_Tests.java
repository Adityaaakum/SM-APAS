package com.apas.Tests.ParcelManagement;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.relevantcodes.extentreports.LogStatus;

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
	@Test(description = "SMAB-T2583, SMAB-T2585, SMAB-T2581, SMAB-T2586, SMAB-T2590:Verify the UI validations for \"Split\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyManyToManyMappingActionUIValidations(String loginUser) throws Exception {
		//Fetching Assessee records
		String queryAssesseeRecord = "SELECT Id, Name FROM Account Limit 1";
		HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		String assesseeName = responseAssesseeDetails.get("Name").get(0);

		//Fetching parcels that are Active with same Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '100%') and (Not Name like '134%') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);

		//Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");
		String primarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ apn1 +"')").get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		String concatenateAPNWithSameOwnership = apn1+","+apn2;        

		//Enter values in the Parcels
		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), "TRA__c", responseTRADetails.get("Id").get(1));	

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.MANY_TO_MANY_MAPPING_ACTION;
		Map<String, String> hashMapManyToManyActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionWithoutAllFields");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToCreateOwnershipRecord");

		// Step1: Login to the APAS application
		objMappingPage.login(users.SYSTEM_ADMIN);

		// Step2: Opening the PARCELS page and searching the parcel to create ownership record   
		responseAPNDetails.get("Name").stream().forEach(parcel -> {
			try {
				objMappingPage.searchModule(PARCELS);
				objMappingPage.globalSearchRecords(parcel);
				objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
				objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
			}
			catch(Exception e) {
				ExtentTestManager.getTest().log(LogStatus.ERROR, "Fail to create ownership record"+e);
			}
		});
		objWorkItemHomePage.logout();
		Thread.sleep(20000);

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4: Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(40,objWorkItemHomePage.referenceDetailsLabel);
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
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank.",
				"SMAB-T2581: Validation that help text is generated on clicking the help icon for legal description");

		objMappingPage.Click(objMappingPage.helpIconSitus);
		// softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent situs, leave as blank",
		//    "SMAB-T2581: Validation that help text is generated on clicking the help icon for Situs text box");
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.closeButton));

		//Step 12: Validating Error Message when Number of Child Non-Condo Parcels field contains 0
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Number of Child Non Condo Parcels\" must be greater than 1",
				"SMAB-T2583: Validation that error message is displayed when Number of Child Non-Condo & Condo Parcels fields contain 0");

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
				"SMAB-T2584: Validation that error message is displayed when First Child Non-Condo Parcel contains Special Characters");

		//Step 17: entering alphanumeric data in form for Many To Many mapping action
		Map<String, String> hashMapManyToManyActionAlphaNumData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionWithAlphaNumeric");
		objMappingPage.fillMappingActionForm(hashMapManyToManyActionAlphaNumData);

		//Step 18: Validating Error Message having alphabets in fields: First Child Non-Condo Parcel
		softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2584: Validation that error message is displayed when First Child Non-Condo Parcel contains alphabets");

		//Step 19: entering alphanumeric data in form for Many To Many mapping action
		Map<String, String> hashMapManyToManyActionDotInData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionWithDotInData");
		objMappingPage.fillMappingActionForm(hashMapManyToManyActionDotInData);

		//Step 20: Validating Error Message having dot in fields: First Child Non-Condo Parcel
		softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2584: Validation that error message is displayed when First Child Non-Condo Parcel contains dot");

		//Step 21: Validating Error Message when reason code is blank
		Map<String, String> hashMapManyToManyActionWithBlankReasonCodeMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionWithMissingReasonCode");
		objMappingPage.fillMappingActionForm(hashMapManyToManyActionWithBlankReasonCodeMappingData);
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- Please enter the required field(s) : Reason Code",
				"SMAB-T2590: Validation that error message is displayed when reason code is blank");

		//Step 22: entering valid data in form for ManyToMany mapping action
		Map<String, String> hashMapManyToManyActionValidMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionWithValidData");
		objMappingPage.fillMappingActionForm(hashMapManyToManyActionValidMappingData);

		//Step 23: Verify that APNs generated must be 9-digits and should end in '0'
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

		//Step 24: Verify total number of parcels getting generated
		int actualTotalParcels = gridDataHashMap.get("APN").size();
		int expectedTotalParcels = Integer.parseInt(hashMapManyToManyActionValidMappingData.get("Number of Child Non-Condo Parcels"));

		softAssert.assertEquals(actualTotalParcels,expectedTotalParcels,"SMAB-T2586: Verify total no of parcels getting generated");

		//Step 25: Validating warning message
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is",
				"SMAB-T2591: Validation that warning message is displayed when Parcel number generated is different from the user selection");

		//Step 26: Validation of ALL fields THAT ARE displayed on second screen
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),primarySitusValue.replaceFirst("\\s", ""),
				"SMAB-T2873: Validation that System populates Situs from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescriptionValue,
				"SMAB-T2873: Validation that System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2873: Validation that System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),responsePUCDetails.get("Name").get(0),
				"SMAB-T2873: Validation that System populates Use Code  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2873: Validation that System populates District/Neighborhood from the parent parcel");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}

	/**
	 * This method is to Verify that User is able to perform Parent APN validations for "ManyToMany" mapping action for a Parcel (Active) from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2587, SMAB-T2594, SMAB-T2595, SMAB-T2596, SMAB-T2626, SMAB-T2582:Verify the Parent APN validations for \"Many To Many\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyParentAPNValidationsForManyToManyMappingAction(String loginUser) throws Exception {

		//Fetching parcels that are Active with different Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '100%') and (Not Name like '134%') and Status__c = 'Active' Limit 3";

		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);
		String apn3=responseAPNDetails.get("Name").get(2);

		String concatenateAPNWithDifferentOwnership = apn1+","+apn2;

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.MANY_TO_MANY_MAPPING_ACTION;
		Map<String, String> hashMapManyToManyActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionWithoutAllFields");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToCreateOwnershipRecord");

		objMappingPage.login(users.SYSTEM_ADMIN);


		// Step2: Opening the PARCELS page and searching the parcel to create different ownership record 
		//Fetching Assessee records
		String queryAssesseeRecord = "SELECT Id, Name FROM Account Limit 1";
		HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		String assesseeName = responseAssesseeDetails.get("Name").get(0);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(responseAPNDetails.get("Name").get(0));
		objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
		objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);


		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(responseAPNDetails.get("Name").get(2));
		objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
		objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);

		queryAssesseeRecord = "SELECT Id, Name FROM Account Where Id != '"+responseAssesseeDetails.get("Id").get(0)+"' Limit 1";
		responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		assesseeName = responseAssesseeDetails.get("Name").get(0);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(responseAPNDetails.get("Name").get(1));
		objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
		objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);

		objWorkItemHomePage.logout();
		Thread.sleep(20000);

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(40,objWorkItemHomePage.referenceDetailsLabel);
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
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with a parcel \"Many To Many\" action, the parent APN must have the same ownership and ownership allocation.",
				"SMAB-T2596: Validation that proper error message is displayed if parcels are of different ownership");

		//Step 7: Edit Parent APN, enter Retired APN  and Verify Error Message
		// fetching  parcel that is retired
		queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);

		String concatenateRetiredAPN = apn1+","+retiredAPNValue;

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateRetiredAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2587: Validation that proper error message is displayed if parent parcel is retired");
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- Warning: TRAs of the \"Many to Many\" parent parcels are different.",
				"SMAB-T2594: Validation that proper warning message is displayed if parcels are of different TRAs");

		//Step 7: Edit Parent APN, enter In-Progress APN and Verify Error Message
		// fetching  parcel that is In progress
		String inProgressAPNValue= objMappingPage.fetchInProgressAPN();
		String concatenateinProgressAPN = apn1+","+inProgressAPNValue;

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateinProgressAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2587: Validation that proper error message is displayed if parent parcel is in progress status");

		//Step 8: Enter only one Parent APNs and Verify Error message
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to continue with the \"Many to Many\" action please provide more than one \"Parent Parcel\".",
				"SMAB-T2626: Validation that proper error message is displayed if only one parent APN is given for many to many");

		//Step 9: Edit Parent APN, enter APN starting with 134 and Verify Error Message
		queryAPNValue = "SELECT Name from parcel__c where Name like '134%' limit 1";
		response = salesforceAPI.select(queryAPNValue);
		String aPNValue= response.get("Name").get(0);

		String concatenateAPNStartingWith134 = apn1+","+aPNValue;

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNStartingWith134);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel(s) cannot start with 134.",
				"SMAB-T2595: Validation that proper error message is displayed if any of the parent parcel starts with 134");

		//Step 10: Edit Parent APN without hyphen and Verify hyphen is added to APN after saving
		//Fetching parcels that are Active with same Ownership record
		//queryAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName + "') and (Not Name like '%990') and (Not Name like '134%') and Status__c = 'Active' Limit 2";
		//responseAPNDetails = salesforceAPI.select(queryAPNValue);
		//  String activeAPN1=responseAPNDetails.get("Name").get(0);
		//String activeAPN2=responseAPNDetails.get("Name").get(1);

		String concatenateAPNWithoutHyphen = apn1.replace("-","") +","+apn3.replace("-","");

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithoutHyphen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		Thread.sleep(2000);
		softAssert.assertTrue(objMappingPage.getElementText(objMappingPage.parentAPNFieldValue).split(",")[0].contains("-"),"SMAB-T2582: Verify that when 9 digit APN is entered without hyphen, after saving hyphen is added automatically");


		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}

	/**
	 * This method is to Verify that User is able to perform output validations for "ManyToMany" mapping action for a Parcel (Active) from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2722:Verify the Output validations for \"Many to Many\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyNonCondoManyToManyMappingActionOutputValidations(String loginUser) throws Exception {

		//Fetching parcels that are Active with no Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '100%') and (Not Name like '134%') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);
		String parentAPN=apn1;
        if (Integer.parseInt(apn1.replace("-",""))>Integer.parseInt(apn2.replace("-","")))
        	parentAPN=apn2;
 
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(1),jsonObject);

		String concatenateAPNWithSameOwnership = apn1+","+apn2;

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.MANY_TO_MANY_MAPPING_ACTION;
		Map<String, String> hashMapManyToManyActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionWithoutAllFields");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToCreateOwnershipRecord");

		// Step1: Login to the APAS application
		objMappingPage.login(users.SYSTEM_ADMIN);

		// Step2: Opening the PARCELS page and searching the parcel to create ownership record 
		//Fetching Assessee records
		String queryAssesseeRecord = "SELECT Id, Name FROM Account Limit 1";
		HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		String assesseeName = responseAssesseeDetails.get("Name").get(0);
		responseAPNDetails.get("Name").stream().forEach(parcel -> {
			try {
				objMappingPage.searchModule(PARCELS);
				objMappingPage.globalSearchRecords(parcel);
				objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
				objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
			}
			catch(Exception e) {
				ExtentTestManager.getTest().log(LogStatus.ERROR, "Fail to create ownership record"+e);
			}
		});
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		driver.navigate().refresh();
		Thread.sleep(6000);

		// Step 3: Login to the APAS application using the credentials passed through data provider
		objMappingPage.login(loginUser);

		// Step 4: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 5: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 6: Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(40,objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

		// Step 7: Update the Parent APN field and add another parcel with same ownership record
		ReportLogger.INFO("Add a parcel in Parent APN field with same ownership record :: " + apn1 + ", " + apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		//Step 8: Selecting Action as 'Many To Many' & Taxes Paid fields value as 'N/A'
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapManyToManyActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 9: entering valid data in form and generating parcels for ManyToMany mapping action
		Map<String, String> hashMapManyToManyActionValidMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionForOutputValidations");
		objMappingPage.fillMappingActionForm(hashMapManyToManyActionValidMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		//Step 10: Verify the success message after parcels are generated
		softAssert.assertContains(objMappingPage.getSuccessMessage(),"Parcel(s) have been created successfully. Please review spatial information",
				"SMAB-T2722: Validation that success message is displayed when Parcels are generated");

		//Step 11: Verify the grid cells are not editable after parcels are generated
		Thread.sleep(3000);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		boolean actionColumn = gridDataHashMap.containsKey("Action");
		softAssert.assertTrue(!actionColumn,"SMAB-T2722: Validation that columns should not be editable as Action column has disappeared after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2722: Validation that APN column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Legal Description"),"SMAB-T2722: Validation that Legal Description column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA"),"SMAB-T2722: Validation that TRA column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2722: Validation that Situs column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Reason Code"),"SMAB-T2722: Validation that Reason Code column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("District/Neighborhood"),"SMAB-T2722: Validation that District/Neighborhood column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code"),"SMAB-T2722: Validation that Use Code column should not be editable after generating parcels");

		//Step 12: Verify total number of parcels getting generated
		int actualTotalParcels = gridDataHashMap.get("APN").size();
		int expectedTotalParcels = Integer.parseInt(hashMapManyToManyActionValidMappingData.get("Number of Child Non-Condo Parcels"));
		softAssert.assertEquals(actualTotalParcels,expectedTotalParcels,"SMAB-T2722: Verify total no of parcels getting generated");

		//Step 13: Click on APN generated and verify Source Relationship details
		gridDataHashMap.get("APN").stream().forEach(parcel -> {
			try {
				objMappingPage.Click(objMappingPage.getButtonWithText(parcel));
				objMappingPage.waitUntilPageisReady(driver);
				objMappingPage.waitForElementToBeVisible(60, objParcelsPage.moretab);

				objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelRelationshipsTabLabel);
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(apn1)), "SMAB-T2722: Verify Parent Parcel: "+apn1+" is visible under Source Parcel Relationships section");
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(apn2)), "SMAB-T2722: Verify Parent Parcel: "+apn2+" is visible under Source Parcel Relationships section");

				driver.navigate().back();
			}
			catch(Exception e) {
				ExtentTestManager.getTest().log(LogStatus.FAIL, "Fail to validate Parent Parcel under Source Parcel Relationships section"+e);
			}
		});

		//Step 14: Verify Status of Parent & Child Parcels before WI completion
		HashMap<String, ArrayList<String>> parentAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn1);
		HashMap<String, ArrayList<String>> parentAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn2);		
		HashMap<String, ArrayList<String>> childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPN1Status.get("Status__c").get(0),"In Progress - To Be Expired","SMAB-T2722: Verify Status of Parent Parcel: "+apn1);
		softAssert.assertEquals(parentAPN2Status.get("Status__c").get(0),"In Progress - To Be Expired","SMAB-T2722: Verify Status of Parent Parcel: "+apn2);
		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2722: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(childAPN2Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2722: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(1));     

		//Step 15: Verify Neighborhood Code value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNNeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",parentAPN);
		HashMap<String, ArrayList<String>> childAPN1NeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2NeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPNNeighborhoodCode.get("Name").get(0),childAPN1NeighborhoodCode.get("Name").get(0),"SMAB-T2722: Verify District/Neighborhood Code of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(parentAPNNeighborhoodCode.get("Name").get(0),childAPN2NeighborhoodCode.get("Name").get(0),"SMAB-T2722: Verify District/Neighborhood Code of Child Parcel is inheritted from first Parent Parcel");

		//Step 16: Verify TRA value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNTRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",parentAPN);
		HashMap<String, ArrayList<String>> childAPN1TRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2TRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPNTRA.get("Name").get(0),childAPN1TRA.get("Name").get(0),"SMAB-T2722: Verify TRA of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(parentAPNTRA.get("Name").get(0),childAPN2TRA.get("Name").get(0),"SMAB-T2722: Verify TRA of Child Parcel is inheritted from first Parent Parcel");

		//Step 17: Verify District value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNDistrict = objParcelsPage.fetchFieldValueOfParcel("District__c",parentAPN);
		HashMap<String, ArrayList<String>> childAPN1District = objParcelsPage.fetchFieldValueOfParcel("District__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2District = objParcelsPage.fetchFieldValueOfParcel("District__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPNDistrict.get("District__c").get(0),childAPN1District.get("District__c").get(0),"SMAB-T2722: Verify District of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(parentAPNDistrict.get("District__c").get(0),childAPN2District.get("District__c").get(0),"SMAB-T2722: Verify District of Child Parcel is inheritted from first Parent Parcel");

		//Step 18: Verify Primary Situs value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNPrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",parentAPN);
		HashMap<String, ArrayList<String>> childAPN1PrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2PrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPNPrimarySitus.get("Name").get(0),childAPN1PrimarySitus.get("Name").get(0),"SMAB-T2722: Verify Primary Situs of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(parentAPNPrimarySitus.get("Name").get(0),childAPN2PrimarySitus.get("Name").get(0),"SMAB-T2722: Verify Primary Situs of Child Parcel is inheritted from first Parent Parcel");


		//Step 19: Mark the WI complete
		String query = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
		salesforceAPI.update("Work_Item__c", query, "Status__c", "Submitted for Approval");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		driver.navigate().refresh();
		Thread.sleep(6000);

		objMappingPage.login(users.MAPPING_SUPERVISOR);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh(); //refresh as the focus is getting lost
		Thread.sleep(5000);
		objWorkItemHomePage.completeWorkItem();

		//Step 20: Verify Status of Parent & Child Parcels after WI completion
		parentAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn1);
		parentAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn2);		
		childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(0));
		childAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPN1Status.get("Status__c").get(0),"Retired","SMAB-T2722: Verify Status of Parent Parcel: "+apn1);
		softAssert.assertEquals(parentAPN2Status.get("Status__c").get(0),"Retired","SMAB-T2722: Verify Status of Parent Parcel: "+apn2);
		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"Active","SMAB-T2722: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(childAPN2Status.get("Status__c").get(0),"Active","SMAB-T2722: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(1));

		//Step 21: Verify 2 new WIs are generated and linked to Child Parcels after parcel is split and WI is completed
		String queryToGetRequestType = "SELECT Work_Item__r.Request_Type__c FROM Work_Item_Linkage__c Where Parcel__r.Name = '"+gridDataHashMap.get("APN").get(0)+"' OR Parcel__r.Name = '"+gridDataHashMap.get("APN").get(1)+"'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryToGetRequestType);
		int expectedWorkItemsGenerated = response.get("Work_Item__r").size();
		softAssert.assertEquals(expectedWorkItemsGenerated,2,"SMAB-T2722: Verify 2 new Work Items are generated and linked to each child parcel after many to many mapping action is performed and WI is completed");

		softAssert.assertContains(response.get("Work_Item__r").get(0),"New APN - Update Characteristics & Verify PUC","SMAB-T2722: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
		//softAssert.assertContains(response.get("Work_Item__r").get(1),"New APN - Allocate Value","SMAB-T2722: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
		softAssert.assertContains(response.get("Work_Item__r").get(1),"New APN - Update Characteristics & Verify PUC","SMAB-T2722: Verify Request Type of 2 new Work Items generated that are linked to each many to many mapping action is performed and WI is completed");
		//softAssert.assertContains(response.get("Work_Item__r").get(3),"New APN - Allocate Value","SMAB-T2722: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");

		driver.switchTo().window(parentWindow);

		//Step 22: Open Parent APN and verify Target Relationship details
		responseAPNDetails.get("Name").stream().forEach(parcel -> {
			try {
				objMappingPage.searchModule(PARCELS);
				objMappingPage.globalSearchRecords(parcel);
				objMappingPage.waitForElementToBeVisible(60, objParcelsPage.moretab);

				objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelRelationshipsTabLabel);
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(gridDataHashMap.get("APN").get(0))), "SMAB-T2722: Verify Child Parcel: "+gridDataHashMap.get("APN").get(0)+" is visible under Target Parcel Relationships section");
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(gridDataHashMap.get("APN").get(1))), "SMAB-T2722: Verify Child Parcel: "+gridDataHashMap.get("APN").get(1)+" is visible under Target Parcel Relationships section");

				driver.navigate().back();
			}
			catch(Exception e) {
				ExtentTestManager.getTest().log(LogStatus.FAIL, "Fail to validate Parent Parcel under Source Parcel Relationships section"+e);
			}
		});

		objWorkItemHomePage.logout();
	}

	/**
	 * This method is to Verify that User is able to perform output validations for "ManyToMany" mapping action for a Parcel (Active) from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2730:Verify the Output validations for \"Many to Many\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyCondoManyToManyMappingActionOutputValidations(String loginUser) throws Exception {

		//Fetching parcels that are Active with no Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Name like '100%') and (Not Name like '134%') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);

		String parentAPN=apn1;
		if (Integer.parseInt(apn1.replace("-",""))>Integer.parseInt(apn2.replace("-","")))
        	parentAPN=apn2;
        
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(1),jsonObject);

		String concatenateAPNWithSameOwnership = apn1+","+apn2;

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.MANY_TO_MANY_MAPPING_ACTION;
		Map<String, String> hashMapManyToManyActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionWithoutAllFields");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToCreateOwnershipRecord");

		// Step1: Login to the APAS application
		objMappingPage.login(users.SYSTEM_ADMIN);

		// Step2: Opening the PARCELS page and searching the parcel to create ownership record 
		String queryAssesseeRecord = "SELECT Id, Name FROM Account Limit 1";
		HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		String assesseeName = responseAssesseeDetails.get("Name").get(0);
		responseAPNDetails.get("Name").stream().forEach(parcel -> {
			try {
				objMappingPage.searchModule(PARCELS);
				objMappingPage.globalSearchRecords(parcel);
				objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
				objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
			}
			catch(Exception e) {
				ExtentTestManager.getTest().log(LogStatus.ERROR, "Fail to create ownership record"+e);
			}
		});
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		driver.navigate().refresh();
		Thread.sleep(6000);

		// Step 3: Login to the APAS application using the credentials passed through data provider
		objMappingPage.login(loginUser);

		// Step 4: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 5: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 6: Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(40,objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

		// Step 7: Update the Parent APN field and add another parcel with same ownership record
		ReportLogger.INFO("Add a parcel in Parent APN field with same ownership record :: " + apn1 + ", " + apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		//Step 8: Selecting Action as 'Many To Many' & Taxes Paid fields value as 'N/A'
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapManyToManyActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 9: entering valid data in form and generating parcels for ManyToMany mapping action
		Map<String, String> hashMapManyToManyActionValidMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionForOutputValidations");
		objMappingPage.fillMappingActionForm(hashMapManyToManyActionValidMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		//Step 10: Verify the success message after parcels are generated
		softAssert.assertContains(objMappingPage.getSuccessMessage(),"Parcel(s) have been created successfully. Please review spatial information",
				"SMAB-T2730: Validation that success message is displayed when Parcels are generated");

		//Step 11: Verify the grid cells are not editable after parcels are generated
		Thread.sleep(3000);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		boolean actionColumn = gridDataHashMap.containsKey("Action");
		softAssert.assertTrue(!actionColumn,"SMAB-T2722: Validation that columns should not be editable as Action column has disappeared after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2730: Validation that APN column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Legal Description"),"SMAB-T2730: Validation that Legal Description column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA"),"SMAB-T2730: Validation that TRA column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2730: Validation that Situs column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Reason Code"),"SMAB-T2730: Validation that Reason Code column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("District/Neighborhood"),"SMAB-T2730: Validation that District/Neighborhood column should not be editable after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code"),"SMAB-T2730: Validation that Use Code column should not be editable after generating parcels");

		//Step 12: Verify total number of parcels getting generated
		int actualTotalParcels = gridDataHashMap.get("APN").size();
		int expectedTotalParcels = Integer.parseInt(hashMapManyToManyActionValidMappingData.get("Number of Child Non-Condo Parcels"));
		softAssert.assertEquals(actualTotalParcels,expectedTotalParcels,"SMAB-T2730: Verify total no of parcels getting generated");

		//Step 13: Click on APN generated and verify Source Relationship details
		gridDataHashMap.get("APN").stream().forEach(parcel -> {
			try {
				objMappingPage.Click(objMappingPage.getButtonWithText(parcel));
				objMappingPage.waitUntilPageisReady(driver);
				objMappingPage.waitForElementToBeVisible(60, objParcelsPage.moretab);

				objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelRelationshipsTabLabel);
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(apn1)), "SMAB-T2730: Verify Parent Parcel: "+apn1+" is visible under Source Parcel Relationships section");
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(apn2)), "SMAB-T2730: Verify Parent Parcel: "+apn2+" is visible under Source Parcel Relationships section");

				driver.navigate().back();
			}
			catch(Exception e) {
				ExtentTestManager.getTest().log(LogStatus.FAIL, "Fail to validate Parent Parcel under Source Parcel Relationships section"+e);
			}
		});

		//Step 14: Verify Status of Parent & Child Parcels before WI completion
		HashMap<String, ArrayList<String>> parentAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn1);
		HashMap<String, ArrayList<String>> parentAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn2);		
		HashMap<String, ArrayList<String>> childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPN1Status.get("Status__c").get(0),"In Progress - To Be Expired","SMAB-T2730: Verify Status of Parent Parcel: "+apn1);
		softAssert.assertEquals(parentAPN2Status.get("Status__c").get(0),"In Progress - To Be Expired","SMAB-T2730: Verify Status of Parent Parcel: "+apn2);
		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2730: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(childAPN2Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2730: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(1));


		//Step 15: Verify Neighborhood Code value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNNeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",parentAPN);
		HashMap<String, ArrayList<String>> childAPN1NeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2NeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPNNeighborhoodCode.get("Name").get(0),childAPN1NeighborhoodCode.get("Name").get(0),"SMAB-T2730: Verify District/Neighborhood Code of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(parentAPNNeighborhoodCode.get("Name").get(0),childAPN2NeighborhoodCode.get("Name").get(0),"SMAB-T2730: Verify District/Neighborhood Code of Child Parcel is inheritted from first Parent Parcel");

		//Step 16: Verify TRA value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNTRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",parentAPN);
		HashMap<String, ArrayList<String>> childAPN1TRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2TRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPNTRA.get("Name").get(0),childAPN1TRA.get("Name").get(0),"SMAB-T2730: Verify TRA of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(parentAPNTRA.get("Name").get(0),childAPN2TRA.get("Name").get(0),"SMAB-T2730: Verify TRA of Child Parcel is inheritted from first Parent Parcel");

		//Step 17: Verify District value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNDistrict = objParcelsPage.fetchFieldValueOfParcel("District__c",parentAPN);
		HashMap<String, ArrayList<String>> childAPN1District = objParcelsPage.fetchFieldValueOfParcel("District__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2District = objParcelsPage.fetchFieldValueOfParcel("District__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPNDistrict.get("District__c").get(0),childAPN1District.get("District__c").get(0),"SMAB-T2730: Verify District of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(parentAPNDistrict.get("District__c").get(0),childAPN2District.get("District__c").get(0),"SMAB-T2730: Verify District of Child Parcel is inheritted from first Parent Parcel");

		//Step 18: Verify Primary Situs value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNPrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",parentAPN);
		HashMap<String, ArrayList<String>> childAPN1PrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",gridDataHashMap.get("APN").get(0));
		HashMap<String, ArrayList<String>> childAPN2PrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPNPrimarySitus.get("Name").get(0),childAPN1PrimarySitus.get("Name").get(0),"SMAB-T2730: Verify Primary Situs of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(parentAPNPrimarySitus.get("Name").get(0),childAPN2PrimarySitus.get("Name").get(0),"SMAB-T2730: Verify Primary Situs of Child Parcel is inheritted from first Parent Parcel");


		//Step 19: Mark the WI complete
		String query = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
		salesforceAPI.update("Work_Item__c", query, "Status__c", "Submitted for Approval");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		driver.navigate().refresh();
		Thread.sleep(6000);

		objMappingPage.login(users.MAPPING_SUPERVISOR);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh(); //refresh as the focus is getting lost
		Thread.sleep(5000);
		objWorkItemHomePage.completeWorkItem();

		//Step 20: Verify Status of Parent & Child Parcels after WI completion
		parentAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn1);
		parentAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn2);		
		childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(0));
		childAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(1));
		softAssert.assertEquals(parentAPN1Status.get("Status__c").get(0),"Retired","SMAB-T2730: Verify Status of Parent Parcel: "+apn1);
		softAssert.assertEquals(parentAPN2Status.get("Status__c").get(0),"Retired","SMAB-T2730: Verify Status of Parent Parcel: "+apn2);
		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"Active","SMAB-T2730: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(childAPN2Status.get("Status__c").get(0),"Active","SMAB-T2730: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(1));

		//Step 21: Verify 2 new WIs are generated and linked to Child Parcels after parcel is split and WI is completed
		String queryToGetRequestType = "SELECT Work_Item__r.Request_Type__c FROM Work_Item_Linkage__c Where Parcel__r.Name = '"+gridDataHashMap.get("APN").get(0)+"' OR Parcel__r.Name = '"+gridDataHashMap.get("APN").get(1)+"'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryToGetRequestType);
		int expectedWorkItemsGenerated = response.get("Work_Item__r").size();
		softAssert.assertEquals(expectedWorkItemsGenerated,2,"SMAB-T2730: Verify 2 new Work Items are generated and linked to each child parcel after many to many mapping action is performed and WI is completed");

		softAssert.assertContains(response.get("Work_Item__r").get(0),"New APN - Update Characteristics & Verify PUC","SMAB-T2730: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
		//softAssert.assertContains(response.get("Work_Item__r").get(1),"New APN - Allocate Value","SMAB-T2730: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
		softAssert.assertContains(response.get("Work_Item__r").get(1),"New APN - Update Characteristics & Verify PUC","SMAB-T2730: Verify Request Type of 2 new Work Items generated that are linked to each many to many mapping action is performed and WI is completed");
		//softAssert.assertContains(response.get("Work_Item__r").get(3),"New APN - Allocate Value","SMAB-T2730: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");

		driver.switchTo().window(parentWindow);

		//Step 22: Open Parent APN and verify Target Relationship details
		responseAPNDetails.get("Name").stream().forEach(parcel -> {
			try {
				objMappingPage.searchModule(PARCELS);
				objMappingPage.globalSearchRecords(parcel);
				objMappingPage.waitForElementToBeVisible(60, objParcelsPage.moretab);

				objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelRelationshipsTabLabel);
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(gridDataHashMap.get("APN").get(0))), "SMAB-T2730: Verify Child Parcel: "+gridDataHashMap.get("APN").get(0)+" is visible under Target Parcel Relationships section");
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(gridDataHashMap.get("APN").get(1))), "SMAB-T2730: Verify Child Parcel: "+gridDataHashMap.get("APN").get(1)+" is visible under Target Parcel Relationships section");

				driver.navigate().back();
			}
			catch(Exception e) {
				ExtentTestManager.getTest().log(LogStatus.FAIL, "Fail to validate Parent Parcel under Source Parcel Relationships section"+e);
			}
		});

		objWorkItemHomePage.logout();
	}
	/**
	 * This method is to Verify that  User is able to update a Situs of child parcel from the Parcel mapping screen for  "Many to Many" mapping action
	 ** @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2660:Parcel Management- Verify that User is able to update a Situs of child parcel from the Parcel mapping screen for  \"Many to Many\" mapping action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_UpdateChildParcelSitus_ManyToManyMappingAction(String loginUser) throws Exception {
		//Fetching parcels that are Active with no Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Name like '100%') and (Not Name like '134%') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");
		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(1),jsonObject);

		String concatenateAPNWithSameOwnership = apn1+","+apn2;

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.MANY_TO_MANY_MAPPING_ACTION;
		Map<String, String> hashMapManyToManyActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformManyToManyMappingActionWithSitusData");

		String situsCityName = hashMapManyToManyActionMappingData.get("Situs City Name");
		String direction = hashMapManyToManyActionMappingData.get("Direction");
		String situsNumber = hashMapManyToManyActionMappingData.get("Situs Number");
		String situsStreetName = hashMapManyToManyActionMappingData.get("Situs Street Name");
		String situsType = hashMapManyToManyActionMappingData.get("Situs Type");
		String situsUnitNumber = hashMapManyToManyActionMappingData.get("Situs Unit Number");
		String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;

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
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

		// Step 5: Update the Parent APN field and add another parcel with same ownership record
		ReportLogger.INFO("Add a parcel in Parent APN field with same ownership record :: " + apn1 + ", " + apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		//Step 6: Selecting Action as 'Many To Many' & Taxes Paid fields value as 'Yes'
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapManyToManyActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 7: editing situs for child parcel and filling all fields
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel));

		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.editSitusLabelSitusModal),
				"SMAB-T2660: Validation that Edit Situs label is displayed as heading of situs modal window in first screen");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.situsInformationLabelSitusModal),
				"SMAB-T2660: Validation that  Situs Information label is displayed in  situs modal window in first screen");
		objMappingPage.editSitusModalWindowFirstScreen(hashMapManyToManyActionMappingData);
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel),"value"),childprimarySitus,
				"SMAB-T2660: Validation that User is able to update a Situs for child parcel from the Parcel mapping screen");

		//Step 8: entering valid data in form for ManyToMany mapping action
		objMappingPage.fillMappingActionForm(hashMapManyToManyActionMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		for(int i=0;i<gridDataHashMap.get("Situs").size();i++)
			softAssert.assertEquals(gridDataHashMap.get("Situs").get(i),childprimarySitus,
					"SMAB-T2660: Validation that System populates primary situs on second screen for child parcel number "+i+1+" with the situs value that was added in first screen");

		//Step 9: Click generate Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 10: Validation that primary situs on last screen screen is getting populated from situs entered in first screen
		for(int i=0;i<gridDataHashMap.get("Situs").size();i++)
			softAssert.assertEquals(gridDataHashMap.get("Situs").get(i),childprimarySitus,
					"SMAB-T2660: Validation that System populates primary situs on last screen for child parcel number "+i+" with the situs value that was added in first screen");

		//Step 11: Validation that primary situs of child parcel is the situs value that was added in first screen from situs modal window
		for(int i=0;i<gridDataHashMap.get("Situs").size();i++)
		{
			String primarySitusValueChildParcel=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ gridDataHashMap.get("APN").get(i) +"')").get("Name").get(0);
			softAssert.assertEquals(primarySitusValueChildParcel,childprimarySitus,
					"SMAB-T2660: Validation that primary situs of  child parcel number "+i+" has value that was entered in first screen through situs modal window");
		}
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
}