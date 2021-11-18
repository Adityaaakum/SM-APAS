package com.apas.Tests.ParcelManagement;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.core.IsNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	CIOTransferPage objtransfer;
	AuditTrailPage trail;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objtransfer=new CIOTransferPage(driver);
		trail= new AuditTrailPage(driver);

	}
	/**
	 * This method is to Verify that User is able to perform validations for "Split" mapping action for a Parcel (Active) from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2294, SMAB-T2295, SMAB-T2428, SMAB-T2296, SMAB-T2314, SMAB-T2613:Verify the UI validations for \"Split\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifySplitMappingActionUIValidations(String loginUser) throws Exception {
		String apn = objMappingPage.fetchActiveAPN();

		jsonObject.put("Neighborhood_Reference__c","");
		String query = "Select Id from Parcel__c where Name = '"+apn+"'";
		salesforceAPI.update("Parcel__c",query,jsonObject);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithoutAllFields");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform split mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'Split' & Taxes Paid fields value as 'N/A'
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"N/A");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objMappingPage.reasonCodeTextBoxLabel)),"SMAB-T2613: Validation that all fields are nor visible when 'Taxes Paid Dropdown' is selected as 'N/A'");

		//Step 6: Selecting Action as 'Split' & Taxes Paid fields value as 'NO'
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"No");
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Taxes must be fully paid in order to perform any action",
				"SMAB-T2613: Validation that error message is displayed when 'Taxes Paid Dropdown' is selected as 'No'");

		//Step 7: Selecting Action as 'Split' & Taxes Paid fields value as 'Yes'
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 8: Validating that default values of Number of Child Non-Condo Parcels and Number of Child Condo Parcels are 0
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.numberOfChildNonCondoTextBoxLabel),
				"SMAB-T3050: Validate that number of non condo field is visible");
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfChildNonCondoTextBoxLabel),"value"),"0",
				"SMAB-T2294: Validation that default value of Number of Child Non-Condo Parcels  is 0");
		
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.numberOfChildCondoTextBoxLabel),
				"SMAB-T3050: Validate that number of  condo field is visible");
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfChildCondoTextBoxLabel),"value"),"0",
				"SMAB-T2294: Validation that default value of Number of Child Condo Parcels  is 0");

		//Step 9: Validating that reason code field is auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2613: Validation that reason code field is auto populated from parent parcel work item");

		//Step 10: Validating help icons and field of first non condo
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.firstNonCondoTextBoxLabel),
				"SMAB-T3050: Validate that First non condo field is visible");
		objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
				"SMAB-T2481,SMAB-T3050: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");

		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.firstCondoTextBoxLabel),
				"SMAB-T3050: Validate that First condo field is visible");
		
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.legalDescriptionTextBoxLabel),
				"SMAB-T3050: Validate that Legal description field is visible");
		objMappingPage.Click(objMappingPage.helpIconLegalDescription);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank.",
				"SMAB-T2481,SMAB-T3050: Validation that help text is generated on clicking the help icon for legal description");

		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.situsTextBoxLabel),
				"SMAB-T3050: Validation that  Situs Information label");
		Actions action = new Actions(driver);
		action.moveToElement(objMappingPage.helpIconSitus).perform();
		//objMappingPage.Click(objMappingPage.helpIconSitus);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent situs, leave as blank.",
				"SMAB-T2481,SMAB-T3050: Validation that help text is generated on clicking the help icon for Situs text box");
		//objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.closeButton));

		//Step 11: Validating Error Message when both Number of Child Non-Condo & Condo Parcels fields contain 0
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Number of Child Non-Condo Parcels and/or Number of Child Condo parcels values total must be equivalent to two or greater.",
				"SMAB-T2294: Validation that error message is displayed when both Number of Child Non-Condo & Condo Parcels fields contain 0");


		//Step 12: entering incorrect map book in 'First Non Condo Parcel Number' & 'First Condo Parcel Number' fields
		Map<String, String> hashMapSplitActionInvalidData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithIncorrectData");
		objMappingPage.fillMappingActionForm(hashMapSplitActionInvalidData);

		//Step 13: Validating Error Message having incorrect map book data
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Non Condo Parcel Number cannot start with 100, Please enter valid Parcel Number",
				"SMAB-T2428: Validation that error message is displayed when map book of First Child Non-Condo Parcel is 100");
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Condo Parcel Number should start with 100 only, Please enter valid Parcel Number",
				"SMAB-T2295: Validation that error message is displayed when map book of First Child Condo Parcel is any number except 100");

		//Step 14: entering data having special characters in form for split mapping action
		Map<String, String> hashMapSplitActionSpecialCharsData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithSpecialChars");
		objMappingPage.fillMappingActionForm(hashMapSplitActionSpecialCharsData);

		//Step 15: Validating Error Message having special characters in fields: First Child Non-Condo Parcel & First Child Condo Parcel
		softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2613: Validation that error message is displayed when First Child Non-Condo Parcel & First Child Condo Parcel contains Special Characters");

		//Step 16: entering alphanumeric data in form for split mapping action
		Map<String, String> hashMapSplitActionAlphaNumData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithAlphaNumeric");
		objMappingPage.fillMappingActionForm(hashMapSplitActionAlphaNumData);

		//Step 17: Validating Error Message having alphabets in fields: First Child Non-Condo Parcel & First Child Condo Parcel
		softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2296: Validation that error message is displayed when First Child Non-Condo Parcel & First Child Condo Parcel contains alphabets");

		//Step 18: entering alphanumeric data in form for split mapping action
		Map<String, String> hashMapSplitActionDotInData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithDotInData");
		objMappingPage.fillMappingActionForm(hashMapSplitActionDotInData);

		//Step 19: Validating Error Message having dot in fields: First Child Non-Condo Parcel & First Child Condo Parcel
		softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2613: Validation that error message is displayed when First Child Non-Condo Parcel & First Child Condo Parcel contains dot");

		//Step 20: entering valid data in form for split mapping action
		Map<String, String> hashMapSplitActionValidMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithValidData");
		objMappingPage.fillMappingActionForm(hashMapSplitActionValidMappingData);

		//Step 21: Verify that APNs generated must be 9-digits and should end in '0'

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

		//Step 22: Verify total number of parcels getting generated
		int actualTotalParcels = gridDataHashMap.get("APN").size();
		int expectedTotalParcels = Integer.parseInt(hashMapSplitActionValidMappingData.get("Number of Child Non-Condo Parcels")) +
				Integer.parseInt(hashMapSplitActionValidMappingData.get("Number of Child Condo Parcels"));

		softAssert.assertEquals(actualTotalParcels,expectedTotalParcels,"SMAB-T2613: Verify total no of parcels getting generated");

		//Step 23: Validating warning messages
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is",
				"SMAB-T2613: Validation that warning message is displayed when Parcel number generated is different from the user selection");

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		softAssert.assertContains(objMappingPage.getErrorMessage(),"The district and neighborhood is required in order to proceed",
				"SMAB-T3737: Verify that for all mapping actions the \"District/Neighborhood\" must be mandatory "
						+ "and error msg should be displayed on generating parcel if District/Neighborhood "
						+ "is empty");
		Map<String, String> hashMapSplitActionMappingDataSitus = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithSitusData");
		for(int i=1;i<=gridDataHashMap.get("APN").size();i++) {
			
		objMappingPage.Click(objMappingPage.locateElement("//tr["+i+"]"+objMappingPage.secondScreenEditButton, 2));
		objMappingPage.editActionInMappingSecondScreen(hashMapSplitActionMappingDataSitus);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.generateParcelButton);
		}
		
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		
		driver.switchTo().window(parentWindow);
		objMappingPage.searchModule(PARCELS);
		String childAPN = gridDataHashMap.get("APN").get(0);
		objMappingPage.globalSearchRecords(childAPN);
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),
				objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelDistrictNeighborhood, "Summary Values"),
				"SMAB-T3818: Parcel Management- Verify that for all relevant mapping actions the"
						+ " \"District/Neighborhood\" must be mandatory and should be inherited in child parcel");

		objWorkItemHomePage.logout();

	}
	/**
	 * This method is to Verify that User is able to perform Parent APN validations for "Split" mapping action for a Parcel (Active) from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2313, SMAB-T2292, SMAB-T2613:Verify the Parent APN validations for \"Split\" mapping action for a Parcel (Active) from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
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

		// Step2: Opening the PARCELS page  and searching the  parcel to perform split mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(3000);
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
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2313: Validation that proper error message is displayed if parent parcel is retired");

		//Step 7: Edit Parent APN, enter In-Progress APN and Verify Error Message
		// fetching  parcel that is In progress
		String inProgressAPNValue= objMappingPage.fetchInProgressAPN();

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2313: Validation that proper error message is displayed if parent parcel is in progress status");

		//Step 7: Enter more than one Parent APNs and Verify Error message
		ArrayList<String> APNs=objMappingPage.fetchActiveAPN(2);
		String activeParcelToPerformMapping=APNs.get(0);
		String activeParcelToPerformMapping2=APNs.get(1);

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,activeParcelToPerformMapping + ","+ activeParcelToPerformMapping2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- Split process should have exactly one parent Apn",
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
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifySplitMappingActionManualOverwriteValidations(String loginUser) throws Exception {
		
		JSONObject jsonForManualOverwrite =objMappingPage.getJsonObject();
		
		String queryAPN = "Select name,ID  From Parcel__c where  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO')  limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		
		objMappingPage.deleteRelationshipInstanceFromParcel(apn);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);

		// Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI
				.select("SELECT Name,Id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') and Legacy__c = 'No' limit 1");
		

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue = "Legal PM 85/25-260";
		String districtValue = "District01";
		String parcelSize = "200";

		jsonForManualOverwrite.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonForManualOverwrite.put("Status__c", "Active");
		jsonForManualOverwrite.put("Short_Legal_Description__c", legalDescriptionValue);
		jsonForManualOverwrite.put("District__c", districtValue);
		jsonForManualOverwrite.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		jsonForManualOverwrite.put("TRA__c", responseTRADetails.get("Id").get(0));
		jsonForManualOverwrite.put("Lot_Size_SQFT__c", parcelSize);
		

		// updating PUC details
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonForManualOverwrite);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;

		// Step1: Login to the APAS application using the credentials passed through
		// dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to perform split
		// mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		objMappingPage.waitForElementToBeClickable(10, objParcelsPage.componentActionsButtonText);
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Selecting Action & Taxes Paid fields values
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

		// Step 6: entering valid data in form for split mapping action
		Map<String, String> hashMapSplitActionValidMappingData = objUtil
				.generateMapFromJsonFile(mappingActionCreationData, "DataToPerformSplitMappingActionWithValidData");
		objMappingPage.fillMappingActionForm(hashMapSplitActionValidMappingData);

		// Step 7: Verify total number of parcels getting generated
		HashMap<String, ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();
		int actualTotalParcels = gridDataHashMap.get("APN").size();
		int expectedTotalParcels = Integer
				.parseInt(hashMapSplitActionValidMappingData.get("Number of Child Non-Condo Parcels"))
				+ Integer.parseInt(hashMapSplitActionValidMappingData.get("Number of Child Condo Parcels"));

		softAssert.assertEquals(actualTotalParcels, expectedTotalParcels,
				"SMAB-T2613: Verify total no of parcels getting generated");

		// Step 8: Validating error message when APN entered is not next available
		// number
		// Considering Map Book & Map Page from the APN generated by system
		// and adding 100 to parcel number will become new APN which is not available
		// next as per system
		String childAPNNumber = gridDataHashMap.get("APN").get(0);
		String childAPNComponents[] = childAPNNumber.split("-");
		String apnNotNextAvailable = childAPNComponents[0] + childAPNComponents[1]
				+ String.valueOf(Integer.parseInt(childAPNComponents[2]) + 100);

		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen, apnNotNextAvailable);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		//objMappingPage.waitForElementToDisappear(objMappingPage.xpathSpinner, 60);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.apnColumnSecondScreen);
		Thread.sleep(2000);

		gridDataHashMap = objMappingPage.getGridDataInHashMap();
		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),
				"The parcel entered is invalid since the following parcel is available",
				"SMAB-T2613: Validation that error message is displayed when parcel entered is not next abvaialble");

		// Step 9: Validating error message when APN entered contains alphabets
		// Considering Map Book & Map Page from the APN generated by system
		// and adding alphabets to parcel number
		String apnContainingAlphabets = childAPNComponents[0] + childAPNComponents[1] + "abc";

		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen, apnContainingAlphabets);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		gridDataHashMap = objMappingPage.getGridDataInHashMap();
		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),
				"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2296: Validation that error message is displayed when parcel entered contains alphabets");

		// Step 10: Validating error message when APN entered contains special
		// characters
		// Considering Map Book & Map Page from the APN generated by system
		// and adding special characters to parcel number
		String apnContainingspecialChars = childAPNComponents[0] + childAPNComponents[1] + "45&";

		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen, apnContainingspecialChars);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		gridDataHashMap = objMappingPage.getGridDataInHashMap();
		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),
				"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2613: Validation that error message is displayed when parcel entered contains special characters");

		// Step 10: Validating error message when APN entered already exists in the
		// system
		String alreadyExistingApn = apn;

		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen, alreadyExistingApn);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		gridDataHashMap = objMappingPage.getGridDataInHashMap();
		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),
				"The APN provided already exists in the system",
				"SMAB-T2613: Validation that error message is displayed when parcel entered already exists in the system");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();


	}
	/**
	 * This method is to Verify that User is able to perform output validations for "Split" mapping action for a Parcel (Active) from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2613, SMAB-T2614:Verify that parcel can be split into any number of parcels", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifySplitMappingActionNoOfParcelsValidations(String loginUser) throws Exception {
		String apn = objMappingPage.fetchActiveAPN();

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithoutAllFields");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform split mapping
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

		//Step 5: Selecting Action as 'Split' & Taxes Paid fields value as 'N/A'
		String customScreenWindow = driver.getWindowHandle();
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

		//Step 6: clicking on tax collector link
		objMappingPage.clickTaxCollectorLink(apn);
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertContains(driver.getCurrentUrl(),"taxes","SMAB-T2613: Validate that user is able to open tax collector link");
		driver.switchTo().window(customScreenWindow);

		//Step 7: entering correct data in mandatory fields
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		Map<String, String> hashMapSplitActionValidData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithAnyNoOfParcels");
		objMappingPage.fillMappingActionForm(hashMapSplitActionValidData);

		//Step 8: Verify total number of parcels getting generated
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		int actualTotalParcels = gridDataHashMap.get("APN").size();
		int expectedTotalParcels = Integer.parseInt(hashMapSplitActionValidData.get("Number of Child Non-Condo Parcels")) +
				Integer.parseInt(hashMapSplitActionValidData.get("Number of Child Condo Parcels"));

		softAssert.assertEquals(actualTotalParcels,expectedTotalParcels,"SMAB-T2614: Verify total no of parcels getting generated");

	}
	/**
	 * This method is to Verify that User is able to perform output validations for "Split" mapping action for a Parcel (Active) from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3453,SMAB-T2541, SMAB-T2550, SMAB-T2551,SMAB-T3245:Verify the Output Validations for Split Mapping Action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifySplitMappingActionOutputValidations(String loginUser) throws Exception {

		JSONObject jsonForOutputValidations = objMappingPage.getJsonObject();
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%'and"
				+ " Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String apnId=responseAPNDetails.get("Id").get(0);
		
		objMappingPage.deleteRelationshipInstanceFromParcel(apn);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);
		objMappingPage.deleteParcelSitusFromParcel(apn);
		objMappingPage.deleteOwnershipFromParcel(apnId);
		
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), "Lot_Size_SQFT__c", "100");

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,Id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') and Legacy__c = 'No' limit 1");

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonForOutputValidations.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonForOutputValidations.put("Status__c","Active");
		jsonForOutputValidations.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonForOutputValidations.put("District__c",districtValue);
		jsonForOutputValidations.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonForOutputValidations.put("TRA__c",responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonForOutputValidations);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithoutAllFields");
		
		String ownershipCreationData = testdata.MANUAL_PARCEL_CREATION_DATA;
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData,
                "DataToCreateOwnershipRecord");
		
		String mailToRecordCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapMailToRecordData = objUtil.generateMapFromJsonFile(mailToRecordCreationData,
				"dataToCreateMailToRecordsWithIncompleteData");
		
		String characteristicsRecordCreationData = testdata.CHARACTERISTICS;
		Map<String, String> hashMapImprovementCharacteristicsData = objUtil.generateMapFromJsonFile(characteristicsRecordCreationData,
				"DataToCreateImprovementCharacteristics");
		Map<String, String> hashMapLandCharacteristicsData = objUtil.generateMapFromJsonFile(characteristicsRecordCreationData,
				"DataToCreateLandCharacteristics");
		
		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.closeDefaultOpenTabs();

		objMappingPage.waitForElementToBeVisible(10, objMappingPage.appLauncher);
		
		objMappingPage.globalSearchRecords(apn);
		String primarySitusValue = objParcelsPage.createParcelSitus(apn);
		
		HashMap<String, ArrayList<String>> responseAssesseeDetails = objMappingPage.getOwnerForMappingAction(2);
	    String assesseeName1 = responseAssesseeDetails.get("Name").get(0);

		try {
			objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
			objParcelsPage.createOwnershipRecord(assesseeName1, hashMapCreateOwnershipRecordData);
		}
		catch(Exception e) {
			ReportLogger.INFO("Fail to create ownership record : "+e);
		}

		objMappingPage.globalSearchRecords(apn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.createCharacteristicsOnParcel(hashMapImprovementCharacteristicsData,apn);

		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.createCharacteristicsOnParcel(hashMapLandCharacteristicsData,apn);
		objMappingPage.logout();
		Thread.sleep(5000);

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform split mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.closeDefaultOpenTabs();
		
		objMappingPage.globalSearchRecords(apn);
		
		//Fetching the PUC of parent before Split Action
		objMappingPage.waitForElementToBeClickable(10, objParcelsPage.componentActionsButtonText);
	    String parentAPNPucBeforeAction = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");

		// Step 3: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'Split' & Taxes Paid fields value as 'N/A'
		String customScreenWindow = driver.getWindowHandle();
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

		//Step 6: entering correct data in mandatory fields
		Map<String, String> hashMapSplitActionValidData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionForUIValidations");
		objMappingPage.fillMappingActionForm(hashMapSplitActionValidData);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.generateParcelButton);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
       
		   //updating child parcels size in second screen on mapping action 
	       for(int i=1;i<=gridDataHashMap.get("Parcel Size(SQFT)*").size();i++) {
	            objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen,"100",i);
	       }

		//Step 7: Click Split Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 8: Verify Field values are inheritted from Parent to Child Parcels
		String childAPNNumber1 =gridDataHashMap.get("APN").get(0);
		String childAPNNumber2 =gridDataHashMap.get("APN").get(1);

		//Step 9: Verify Primary Situs value is inheritted from Parent to Child Parcels		
		HashMap<String, ArrayList<String>> parentAPNPrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",apn);
		HashMap<String, ArrayList<String>> childAPN1PrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",childAPNNumber1);
		HashMap<String, ArrayList<String>> childAPN2PrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",childAPNNumber2);

		softAssert.assertEquals(parentAPNPrimarySitus.get("Name").get(0),childAPN1PrimarySitus.get("Name").get(0),"SMAB-T2541: Verify Primary Situs of Child Parcel is inheritted from Parent Parcel");
		softAssert.assertEquals(parentAPNPrimarySitus.get("Name").get(0),childAPN2PrimarySitus.get("Name").get(0),"SMAB-T2541: Verify Primary Situs of Child Parcel is inheritted from Parent Parcel");

		//Step 10: Verify Neighborhood Code value is inheritted from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNNeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",apn);
		HashMap<String, ArrayList<String>> childAPN1NeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",childAPNNumber1);
		HashMap<String, ArrayList<String>> childAPN2NeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",childAPNNumber2);
		softAssert.assertEquals(parentAPNNeighborhoodCode.get("Name").get(0),childAPN1NeighborhoodCode.get("Name").get(0),"SMAB-T2541: Verify District/Neighborhood Code of Child Parcel is inheritted from Parent Parcel");
		softAssert.assertEquals(parentAPNNeighborhoodCode.get("Name").get(0),childAPN2NeighborhoodCode.get("Name").get(0),"SMAB-T2541: Verify District/Neighborhood Code of Child Parcel is inheritted from Parent Parcel");

		//Step 11: Verify TRA value is inheritted from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNTRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",apn);
		HashMap<String, ArrayList<String>> childAPN1TRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",childAPNNumber1);
		HashMap<String, ArrayList<String>> childAPN2TRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",childAPNNumber2);
		softAssert.assertEquals(parentAPNTRA.get("Name").get(0),childAPN1TRA.get("Name").get(0),"SMAB-T2541: Verify TRA of Child Parcel is inheritted from Parent Parcel");
		softAssert.assertEquals(parentAPNTRA.get("Name").get(0),childAPN2TRA.get("Name").get(0),"SMAB-T2541: Verify TRA of Child Parcel is inheritted from Parent Parcel");

		//Step 12: Verify Status of Parent & Child Parcels after parcel is split and before WI completion
		HashMap<String, ArrayList<String>> parentAPNStatus = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn);
		HashMap<String, ArrayList<String>> childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",childAPNNumber1);
		HashMap<String, ArrayList<String>> childAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",childAPNNumber2);
		softAssert.assertEquals(parentAPNStatus.get("Status__c").get(0),"In Progress - To Be Expired","SMAB-T2541,SMAB-T3245: Verify Status of Parent Parcel: "+apn);
		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2541,SMAB-T3245: Verify Status of Child Parcel: "+childAPNNumber1);
		softAssert.assertEquals(childAPN2Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2541,SMAB-T3245: Verify Status of Child Parcel: "+childAPNNumber2);
		
		//Fetching required PUC'c of parent and child after Split action
		String childAPN1PucFromGrid = gridDataHashMap.get("Use Code*").get(0);
		String childAPN2PucFromGrid = gridDataHashMap.get("Use Code*").get(1);
		driver.switchTo().window(parentWindow);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		String parentAPNPuc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
						
		softAssert.assertEquals(parentAPNPuc,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Parent Parcel:"+apn);
		softAssert.assertEquals(childAPN1PucFromGrid,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Child Parcel:"+childAPNNumber1);
	    softAssert.assertEquals(childAPN2PucFromGrid,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Parent Parcel:"+childAPNNumber2);

		//Step 13: Verify no Parent WI is inherrited by Child Parcels after parcel is split
		String query = "SELECT Id FROM Parcel__c Where Name = '"+childAPNNumber1+ "'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String childAPNId1 = response.get("Id").get(0);
		query = "SELECT Work_Item__r.Name FROM Work_Item_Linkage__c Where Parcel__c = '"+childAPNId1+"'";
		response = salesforceAPI.select(query);
		softAssert.assertEquals(response.size(),0,"SMAB-T2550: Validate that no Parent Parent Work tem is inheritted to Child Parcel "+childAPNNumber1+" after Split Mapping Action");

		query = "SELECT Id FROM Parcel__c Where Name = '"+childAPNNumber2+ "'";
		response = salesforceAPI.select(query);
		String childAPNId2 = response.get("Id").get(0);
		query = "SELECT Work_Item__r.Name FROM Work_Item_Linkage__c Where Parcel__c = '"+childAPNId2+"'";
		response = salesforceAPI.select(query);
		softAssert.assertEquals(response.size(),0,"SMAB-T2550: Validate that no Parent Parent Work tem is inheritted to Child Parcel "+childAPNNumber2+"  after Split Mapping Action");

		//Step 14: Mark the WI complete
		String   queryWI = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
		salesforceAPI.update("Work_Item__c",queryWI, "Status__c", "Submitted for Approval");
		objWorkItemHomePage.logout();
		objMappingPage.login(users.MAPPING_SUPERVISOR);
		Thread.sleep(5000);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh(); //refresh as the focus is getting lost
		Thread.sleep(5000);
		objWorkItemHomePage.completeWorkItem();

		//Step 15: Verify Status of Parent & Child Parcels after parcel is split and WI is completed
		parentAPNStatus = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn);
		childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",childAPNNumber1);
		childAPN2Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",childAPNNumber2);
		softAssert.assertEquals(parentAPNStatus.get("Status__c").get(0),"Retired","SMAB-T2551,SMAB-T3245: Verify Status of Parent Parcel: "+apn);
		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"Active","SMAB-T2551,SMAB-T3245: Verify Status of Child Parcel: "+childAPNNumber1);
		softAssert.assertEquals(childAPN2Status.get("Status__c").get(0),"Active","SMAB-T2551,SMAB-T3245 Verify Status of Child Parcel: "+childAPNNumber2);
		
		//Fetching required PUC's of parent and child after closing WI
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(childAPNNumber1);
		String childParcelPuc1 = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
    	objMappingPage.globalSearchRecords(childAPNNumber2);
		String childParcelPuc2 = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
		objMappingPage.globalSearchRecords(apn);
		parentAPNPuc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
						
	    softAssert.assertEquals(parentAPNPuc,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Parent Parcel:"+apn);
	    softAssert.assertEquals(childParcelPuc1,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Child Parcel:"+childAPNNumber1);
	    softAssert.assertEquals(childParcelPuc2,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Parent Parcel:"+childAPNNumber2);

		//Step 16: Verify 2 new WIs are generated and linked to Child Parcels after parcel is split and WI is completed
		String queryToGetRequestType = "SELECT Work_Item__r.Request_Type__c FROM Work_Item_Linkage__c Where Parcel__c = '"+childAPNId1+"' OR Parcel__c = '"+childAPNId2+"'";
		response = salesforceAPI.select(queryToGetRequestType);
		int expectedWorkItemsGenerated = response.get("Work_Item__r").size();
		softAssert.assertEquals(expectedWorkItemsGenerated,2,"SMAB-T2551: Verify 2 new Work Items are generated and linked to each child parcel after parcel is split and WI is completed");
		for(int i =0;i<expectedWorkItemsGenerated;i++) {
			softAssert.assertContains(response.get("Work_Item__r").get(i),"New APN - Update Characteristics & Verify PUC","SMAB-T2551: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after parcel is split and WI is completed");
			softAssert.assertTrue(!response.get("Work_Item__r").get(i).contains("New APN - Allocate Value"),
					"SMAB-T3453:When a mapping action is completed the system should not automatically create the work item \"Allocate Value\"");

		}

	}
	/**
	 * This method is to Parcel Management- Verify that User is able to update Situs of child parcels from the Parcel mapping screen for "Split" mapping action
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2661:Parcel Management- Verify that User is able to update Situs of child parcels from the Parcel mapping screen for \"Split\" mapping action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {"Regression","ParcelManagement" })
	public void ParcelManagement_UpdateChildParcelSitus_SplitMappingAction(String loginUser) throws Exception {

		JSONObject jsonForChildParcelSitus = objMappingPage.getJsonObject();
		
		String queryAPN = "Select name,ID  From Parcel__c where  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO')  limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		objMappingPage.deleteRelationshipInstanceFromParcel(apn);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);
		

		// Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI
				.select("SELECT Name,Id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') and Legacy__c = 'No' limit 1");
		

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue = "Legal PM 85/25-260";
		String districtValue = "District01";
		String parcelSize = "200";

		jsonForChildParcelSitus.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonForChildParcelSitus.put("Status__c", "Active");
		jsonForChildParcelSitus.put("Short_Legal_Description__c", legalDescriptionValue);
		jsonForChildParcelSitus.put("District__c", districtValue);
		jsonForChildParcelSitus.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		jsonForChildParcelSitus.put("TRA__c", responseTRADetails.get("Id").get(0));
		jsonForChildParcelSitus.put("Lot_Size_SQFT__c", parcelSize);
		jsonForChildParcelSitus.put("Status__c", "Active");

		// updating PUC details
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonForChildParcelSitus);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithSitusData");

		String situsCityName = hashMapSplitActionMappingData.get("Situs City Name");
		String direction = hashMapSplitActionMappingData.get("Direction");
		String situsNumber = hashMapSplitActionMappingData.get("Situs Number");
		String situsStreetName = hashMapSplitActionMappingData.get("Situs Street Name");
		String situsType = hashMapSplitActionMappingData.get("Situs Type");
		String situsUnitNumber = hashMapSplitActionMappingData.get("Situs Unit Number");
		String cityName = hashMapSplitActionMappingData.get("City Name");
		String childprimarySitus = situsNumber + " " + direction + " " + situsStreetName + " " + situsType + " "
				+ situsUnitNumber + ", " + cityName+"";

		// Step1: Login to the APAS application using the credentials passed through
		// dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to perform split
		// mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		objMappingPage.waitForElementToBeClickable(objParcelsPage.componentActionsButtonText);
        objParcelsPage.createParcelSitus(apn);

		// Step 3: Creating Manual work item for the Parcel
		objMappingPage.waitForElementToBeClickable(10, objParcelsPage.componentActionsButtonText);
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Selecting Action as 'Split' & Taxes Paid fields value as 'Yes'
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,
				hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, "Yes");

		// Step 6: editing situs for child parcel and filling all fields
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel));

		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.editSitusLabelSitusModal),
				"SMAB-T2661: Validation that Edit Situs label is displayed as heading of situs modal window in first screen");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.situsInformationLabelSitusModal),
				"SMAB-T2661: Validation that  Situs Information label is displayed in  situs modal window in first screen");
		objMappingPage.editSitusModalWindowFirstScreen(hashMapSplitActionMappingData);
		softAssert.assertEquals(
				objMappingPage.getAttributeValue(
						objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel), "value"),
				childprimarySitus,
				"SMAB-T2661: Validation that User is able to update a Situs for child parcel from the Parcel mapping screen");

		objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);

		// Step 7: Validation that primary situs on second screen is getting populated
		// from situs entered in first screen
		HashMap<String, ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();

		for (int i = 0; i < gridDataHashMap.get("Situs").size(); i++)
			softAssert.assertEquals(gridDataHashMap.get("Situs").get(i), childprimarySitus,
					"SMAB-T2661: Validation that System populates primary situs on second screen for child parcel number "
							+ i + 1 + " with the situs value that was added in first screen");

		// Step 8: Click Split Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap = objMappingPage.getGridDataInHashMap();

		// Step 9: Validation that primary situs on last screen screen is getting
		// populated from situs entered in first screen
		for (int i = 0; i < gridDataHashMap.get("Situs").size(); i++)
			softAssert.assertEquals(gridDataHashMap.get("Situs").get(i), childprimarySitus,
					"SMAB-T2661: Validation that System populates primary situs on last screen for child parcel number "
							+ i + " with the situs value that was added in first screen");

		// Step 10: Validation that primary situs of child parcel is the situs value
		// that was added in first screen from situs modal window
		for (int i = 0; i < gridDataHashMap.get("Situs").size(); i++) {
			String primarySitusValueChildParcel = salesforceAPI.select(
					"SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"
							+ gridDataHashMap.get("APN").get(i) + "')")
					.get("Name").get(0);
			softAssert.assertEquals(primarySitusValueChildParcel, childprimarySitus,
					"SMAB-T2661: Validation that primary situs of  child parcel number " + i
							+ " has value that was entered in first screen through situs modal window");
		}

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "split" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2682,SMAB-T3121:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"Split\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_SplitMappingAction_NoPrimarySitusTRA(String loginUser) throws Exception {
		String childAPNPUC;

		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c =NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO')and TRA__c=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithSitusData");
		
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), "Lot_Size_SQFT__c","100");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String workItem=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel combine' 
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size(SQFT)*").get(0);

		//Step 7: Click generate Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+apn+"') limit 1");
		if(responsePUCDetailsChildAPN.size()==0)
			childAPNPUC ="";
		else
			childAPNPUC=responsePUCDetailsChildAPN.get("Name").get(0);

		//Step 8: Navigating back to the WI that was created and clicking on related action link 
		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2682: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2682: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2682: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2682: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2682: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2682: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2682: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size(SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2682: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2682: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2682: Validation that APN column should not be editable on retirning to custom screen");
		// Legal Description and Reason code are editable as part of SMAB-12026
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Legal Description*"),"SMAB-T2682: Validation that Legal Description column on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*"),"SMAB-T2682: Validation that TRA column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2682: Validation that Situs column should not be editable on retirning to custom screen");
		// Legal Description and Reason code are editable as part of SMAB-12026
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Reason Code*"),"SMAB-T2682: Validation that Reason Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*"),"SMAB-T2682: Validation that District/Neighborhood column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*"),"SMAB-T2682: Validation that Use Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size(SQFT)*"),"SMAB-T2682,SMAB-T3121: Validation that Parcel Size(SQFT) column should  be editable on retirning to custom screen");
		
		objMappingPage.waitForElementToBeVisible(3, objMappingPage.parcelSizeColumnSecondScreen);
		objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "40");
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName));
		ReportLogger.INFO(" Parcel size is updated. ");

		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String APN = gridDataHashMap.get("APN").get(0);

		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(APN);

		// Verify that the parcel size(SQFT)* of second screen with the parcel size on
		// parcel screen and also checks if the Parcel Size (SqFt)field is present on
		// the parcel screen or not
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size(SQFT)*").get(0),
				objMappingPage.getFieldValueFromAPAS("Parcel Size (SqFt)", "Parcel Information"),
				"SMAB-T3121:Parcel size(SQFT) matched and field is avilable on parcel screen");
	
		objWorkItemHomePage.logout();
	}
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "Combine" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3495,SMAB-T3494,SMAB-T3496,SMAB-T2682:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"Split\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_SplitMappingAction__WithPrimarySitusTRA(String loginUser) throws Exception {

		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeMappingWithActionCustomerRequestSplit");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithSitusData");
		
		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c",apn,jsonObject);
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String workItem=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		
		//validating related action
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Related Action", "Information"),
				hashMapmanualWorkItemData.get("Actions"),"SMAB-T3494-Verify that the Related Action"
						+ " label should match the Actions labels while creating WI and it should"
						+ " open mapping screen on clicking-Perform Customer Request Split");
		
		//validating Event Id in Work item screen of Action type
		String eventIDValue = objWorkItemHomePage.getFieldValueFromAPAS("Event ID", "Information");
		softAssert.assertEquals(eventIDValue.contains("Alpha"),
				true,"SMAB-T3496-Verify that the Event ID based on the mapping should be"
						+ " created and populated on the Work item record.");
				

		softAssert.assertTrue(!objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.editEventIdButton),
				"SMAB-T3496-This field should not be editable.");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel combine' 
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size(SQFT)*").get(0);
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(apn), "SMAB-T3364 : Verify that for \" Split \" mapping action, in custom action second screen and third screen Parent APN (s) "+apn+" is displayed");

		//Step 7: Click Combine Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(apn), "SMAB-T3364 : Verify that for \" Split \" mapping action, in custom action second screen and third screen Parent APN (s) "+apn+" is displayed");

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+apn+"') limit 1");
		if(responsePUCDetailsChildAPN.size()==0)
			childAPNPUC ="";
		else
			childAPNPUC=responsePUCDetailsChildAPN.get("Name").get(0);

		//Step 8: Navigating back to the WI that was created and clicking on related action link 
		//validate that The "Return " functionality for parcel mgmt activities should work for all these work items.
		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);
		softAssert.assertEquals(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName).getText(),"Update Parcel(s)",
				"SMAB-T3495-validate that The Return functionality for parcel mgmt activities should work for all these work items.");
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(apn), "SMAB-T3364 : Verify that for \" Split \" mapping action, in custom action second screen and third screen Parent APN (s) "+apn+" is displayed");

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2682: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2682: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2682: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2682: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2682: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2682: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2682: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size(SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2682: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2682: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	@Test(description = "SMAB-T2830,SMAB-T2682:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"Split\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_SplitMappingAction__IndependentMappingActionWI(String loginUser) throws Exception {

		String childAPNPUC;
		String apn = objMappingPage.fetchActiveAPN();

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithSitusData");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);
		Thread.sleep(7000);
		objMappingPage.closeDefaultOpenTabs();

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule("APAS");
		objMappingPage.searchModule("Mapping Action");
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);

		//Step 3: Selecting Action as 'perform parcel combine' 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 4: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size(SQFT)*").get(0);

		//Step 5: Click Combine Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);

		softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2830: Validate that User is able to perform one to one  action from mapping actions tab");			    
	    
		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+apn+"') limit 1");
		if(responsePUCDetailsChildAPN.size()==0)
			childAPNPUC ="";
		else
			childAPNPUC=responsePUCDetailsChildAPN.get("Name").get(0);

		//Step 8: Navigating  to the independent mapping action WI that would have been created after performing combine action and clicking on related action link 
		String workItemId= objWorkItemHomePage.getWorkItemIDFromParcelOnWorkbench(apn);
		String query = "SELECT Name FROM Work_Item__c where id = '"+ workItemId + "'";
		HashMap<String, ArrayList<String>> responseDetails = salesforceAPI.select(query);
		String workItem=responseDetails.get("Name").get(0);

		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type","Information"), "Mapping",
				"SMAB-T2830: Validation that  A new WI of type Mapping is created after performing one to one from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action","Information"), "Independent Mapping Action",
				"SMAB-T2830: Validation that  A new WI of action Independent Mapping Action is created after performing one to one from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(DateUtil.getCurrentDate("MM/dd/yyyy")), "SMAB-T2830: Validation that 'Date' fields is equal to date when this WI was created");

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2682: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2682: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2682: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2682: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2682: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2682: Validation that  System populates Use Code that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size(SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2682: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2682: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	
	/**
	 * This method is to  Verify  the custom edit on mapping page
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3451,SMAB-T3459,SMAB-T3452,SMAB-T2838, SMAB-T2843:I need to have the ability to select specific fields from the mapping custom screen, so that the correct values can be assigned to the parcels.", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void  ParcelManagement_VerifySplitParcelEditAction(String loginUser) throws Exception {

		JSONObject jsonForEditAction = objMappingPage.getJsonObject();
		
		String queryAPNValue = "SELECT Name,Id FROM Parcel__c where  Id NOT IN (SELECT APN__c FROM Work_Item__c "
				+  "where type__c='CIO') Limit 1";

	    HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn=responseAPNDetails.get("Name").get(0);
		objMappingPage.deleteRelationshipInstanceFromParcel(apn);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);
		
		
		//Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,Id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') and Legacy__c = 'No' limit 1");
		

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";
		String parcelSize	= "200";	

		jsonForEditAction.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonForEditAction.put("Status__c","Active");
		jsonForEditAction.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonForEditAction.put("District__c",districtValue);
		jsonForEditAction.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonForEditAction.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonForEditAction.put("Lot_Size_SQFT__c",parcelSize);

		//updating PUC details
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonForEditAction);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithSitusData");
		String situsCityName = hashMapSplitActionMappingData.get("City Name");
		String direction = hashMapSplitActionMappingData.get("Direction");
		String situsNumber = hashMapSplitActionMappingData.get("Situs Number");
		String situsStreetName = hashMapSplitActionMappingData.get("Situs Street Name");
		String situsType = hashMapSplitActionMappingData.get("Situs Type");
		String situsUnitNumber = hashMapSplitActionMappingData.get("Situs Unit Number");
		String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;

		String PUC = salesforceAPI.select("SELECT Name FROM PUC_Code__c  limit 1").get("Name").get(0);
	    String TRA=salesforceAPI.select("SELECT Name FROM TRA__c limit 1").get("Name").get(0); 


		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		objMappingPage.waitForElementToBeClickable(10, objParcelsPage.componentActionsButtonText);
		  String situsBeforeEdit= objParcelsPage.createParcelSitus(apn) ;

		// Step 3: Creating Manual work item for the Parcel
		driver.navigate().refresh();
		objMappingPage.waitForElementToBeClickable(10, objParcelsPage.componentActionsButtonText);
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(3000);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action & Taxes Paid fields values
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		objMappingPage.waitForElementToBeVisible(objMappingPage.reasonCodeField);
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2838: Validation that reason code field is auto populated from parent parcel work item");
		objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//updating child parcel size in second screen on mapping action 
		for(int i=1;i<=gridDataHashMap.get("APN").size();i++) {
			objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen,"99",i);
		}
		
		//	validating second screen warning message
		String parcelsizewarningmessage=objMappingPage.secondScreenParcelSizeWarning.getText();
		softAssert.assertEquals(parcelsizewarningmessage,
				"Parent Parcel Size = "+parcelSize+", Net Land Loss = 10, Net Land Gain = 0, "
						+ "Total Child Parcel(s) Size = 198.",
				"SMAB-T3451,SMAB-T3459-Verify that parent parcel size and entered net gain/loss and value is getting displayed");

		
		objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
		objMappingPage.editActionInMappingSecondScreen(hashMapSplitActionMappingData);
		objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
		ReportLogger.INFO("Validate the Grid values");
		HashMap<String, ArrayList<String>> gridDataHashMapAfterEditAction =objMappingPage.getGridDataInHashMap();
		String childAPNNumber= gridDataHashMapAfterEditAction.get("APN").get(0);
		//Verifying new situs,TRA ,use code is populated in grid table		    
	    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Situs").get(0),childprimarySitus,
				"SMAB-T2838,SMAB-T2843: Validation that System populates Situs from the parent parcel");
	    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("TRA*").get(0),TRA,
				"SMAB-T2838,SMAB-T2843: Validation that System populates TRA from the parent parcel");
	    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Use Code*").get(0),PUC,
				"SMAB-T2838,SMAB-T2843: Validation that System populates Use Code from the parent parcel");
	    objMappingPage.Click(objMappingPage.secondmappingSecondScreenEditActionGridButton);
		Thread.sleep(3000);
		objMappingPage.editActionInMappingSecondScreen(hashMapSplitActionMappingData);
		objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
		ReportLogger.INFO("Validate the Grid values");
		HashMap<String, ArrayList<String>> gridDataHashMapAfterSecondEditAction =objMappingPage.getGridDataInHashMap();
		String seconChildAPNNumber= gridDataHashMapAfterEditAction.get("APN").get(0);
		//Verifying new situs,TRA ,use code is populated in grid table		    
	    softAssert.assertEquals(gridDataHashMapAfterSecondEditAction.get("Situs").get(1),childprimarySitus,
				"SMAB-T2838,SMAB-T2843: Validation that System populates Situs from the parent parcel");
	    softAssert.assertEquals(gridDataHashMapAfterSecondEditAction.get("TRA*").get(1),TRA,
				"SMAB-T2838,SMAB-T2843: Validation that System populates TRA from the parent parcel");
	    softAssert.assertEquals(gridDataHashMapAfterSecondEditAction.get("Use Code*").get(1),PUC,
				"SMAB-T2838,SMAB-T2843: Validation that System populates Use Code from the parent parcel");
	    ReportLogger.INFO("Click on Combine Parcel button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2838,SMAB-T2843: Validate that User is able to perform Combine action for multiple active parcels");			    
	    
	    driver.switchTo().window(parentWindow);
	    objMappingPage.searchModule(PARCELS);
		
		objMappingPage.globalSearchRecords(childAPNNumber);
		objMappingPage.waitForElementToBeVisible(objParcelsPage.puc);
		//Validate the Situs of child parcel generated
	    softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus, "Parcel Information"),childprimarySitus,
				"SMAB-T2843,SMAB-T2838: Validate the Situs of child parcel generated");
	  //Validate the Situs of second child parcel generated
	    objMappingPage.globalSearchRecords(seconChildAPNNumber);
	    softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus, "Parcel Information"),childprimarySitus,
				"SMAB-T2843,SMAB-T2838: Validate the Situs of child parcel generated");
	    
	    //submit WI for approval
	    String   queryWI = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
	    salesforceAPI.update("Work_Item__c",queryWI, "Status__c", "Submitted for Approval");
	    objWorkItemHomePage.logout();
	    
	    //login as supervisor 
	    objMappingPage.login(users.MAPPING_SUPERVISOR);
	    objMappingPage.searchModule(WORK_ITEM);
	    objMappingPage.globalSearchRecords(workItemNumber);
	    objWorkItemHomePage.javascriptClick(objWorkItemHomePage.dataTabCompleted);
	    objWorkItemHomePage.javascriptClick(objWorkItemHomePage.markAsCurrentStatusButton);
	    objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentParcelSizeErrorMsg, 20);
	    
	    String parcelSizeMismatchMsg = objWorkItemHomePage.parentParcelSizeErrorMsg.getText();
	    softAssert.assertEquals(parcelSizeMismatchMsg.contains("Total Child Parcel(s) size must match the Parent's Parcel Size"),
	    	 true,"SMAB-T3452: parent parcel validation at Work Item level");
	    
		objWorkItemHomePage.logout();

	    
	}

	/**
	 * This method is to  Verify WI rejection on split mapping action
	 *@param loginUser
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T3464:Verify the Output validations for \"Split\" mapping action for a Parcel (retired) after rejected the work item ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyWIRejectionAfterPerformSplitMappingAction(String loginUser) throws Exception {

		JSONObject jsonForWIRejection = objMappingPage.getJsonObject();
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%'and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		
		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonForWIRejection.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonForWIRejection.put("Status__c","Active");
		jsonForWIRejection.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonForWIRejection.put("District__c",districtValue);
		jsonForWIRejection.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonForWIRejection.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonForWIRejection.put("Lot_Size_SQFT__c", "100");

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonForWIRejection);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithoutAllFields");

		// Step1: Login to the APAS application using the credentials passed through dataprovider 
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform split mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'Split' & Taxes Paid fields value as 'N/A'
		String customScreenWindow = driver.getWindowHandle();
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

		//Step 6: entering correct data in mandatory fields
		Map<String, String> hashMapSplitActionValidData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionForWorkItemRollBack");
		objMappingPage.fillMappingActionForm(hashMapSplitActionValidData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
        Thread.sleep(5000);
        //updating child parcels size in second screen on mapping action 

       for(int i=1;i<=gridDataHashMap.get("Parcel Size(SQFT)*").size();i++) {
            objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen,"50",i);
       }
		
       //Step 7: Click Split Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		String childAPNNumber1 =gridDataHashMap.get("APN").get(0);
		String childAPNNumber2 =gridDataHashMap.get("APN").get(1);
		
		//Step 8: Mark the WI complete
		String   queryWI = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
		salesforceAPI.update("Work_Item__c",queryWI, "Status__c", "Submitted for Approval");
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		
		// step 9: login with mapping supervisor
		objMappingPage.login(users.MAPPING_SUPERVISOR);
		
		//step 10: rejecting the work item
		objWorkItemHomePage.rejectWorkItem(workItemNumber,"Other","Reject Mapping action after submit for approval");

		// Step 11: Verify Status of Parent Parcel after WI rejected
		objMappingPage.globalSearchRecords(apn);
		String parentAPNStatus = objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus,"Parcel Information");			    
		softAssert.assertEquals(parentAPNStatus,"Active","SMAB-T3464: Verify Status of Parent Parcel: "+apn);
		
		//Step 12: Verify Child Parcels should be delete after WI rejected
		String query = "SELECT Id FROM Parcel__c Where Name = '"+childAPNNumber1+ "'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		softAssert.assertEquals(response.size(),0,"SMAB-T3464: Validate that child apn should be deleted "+childAPNNumber1+" after Split Mapping Action after performing rejection of work item");
		query = "SELECT Id FROM Parcel__c Where Name = '"+childAPNNumber2+ "'";
		response = salesforceAPI.select(query);
		softAssert.assertEquals(response.size(),0,"SMAB-T3464: Validate that child apn should be deleted "+childAPNNumber2+" after Split Mapping Action after performing rejection of work item");
		String targetedApnquery="SELECT  Id, Target_Parcel__c FROM Parcel_Relationship__c where source_parcel__r.name='"+apn+ "' and   Parcel_Actions__c='Perform Parcel Split'";
	    response = salesforceAPI.select(targetedApnquery);
		softAssert.assertEquals(response.size(),0,"SMAB-T3464: Validate that there is no parcel relationship on Parent Parcel when Rejected the Work tem after Split Mapping Action");
		
	    // Step 13 : Switch to parent window and logout
		objMappingPage.logout();	
	}	
	
	@Test(description = "SMAB-T3385,SMAB-T3601,SMAB-T3465,SMAB-T3469,SMAB-T3511,SMAB-T3512,SMAB-T3513:Verify that the Related Action label should"
			+ " match the Actions labels while creating WI and it should open mapping screen on clicking",
			dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, 
			groups = {"Regression","ParcelManagement","RecorderIntegration" },enabled=true)
	public void ParcelManagement_VerifyNewWICondominiumPlansGeneratedfromRecorderIntegrationAndSplitMappingAction(String loginUser) throws Exception {

		JSONObject jsonObject = objMappingPage.getJsonObject();
				
		//Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id"
				+ "  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c "
				+ "where Status__c='Active') limit 1");

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";
		String parcelSize	= "200";	

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObject.put("Lot_Size_SQFT__c",parcelSize);
		
		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithoutAllFields");
		String ownershipCreationData = testdata.MANUAL_PARCEL_CREATION_DATA;
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData,
                "DataToCreateOwnershipRecord");
		
		String characteristicsRecordCreationData = testdata.CHARACTERISTICS;
		Map<String, String> hashMapImprovementCharacteristicsData = objUtil.generateMapFromJsonFile(characteristicsRecordCreationData,
				"DataToCreateImprovementCharacteristics");
		Map<String, String> hashMapLandCharacteristicsData = objUtil.generateMapFromJsonFile(characteristicsRecordCreationData,
				"DataToCreateLandCharacteristics");
		
		HashMap<String, ArrayList<String>> responseAssesseeDetails = objMappingPage.getOwnerForMappingAction(2);
	    String assesseeName1 = responseAssesseeDetails.get("Name").get(0);

		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.closeDefaultOpenTabs();
		
		salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c"
				+ " where Sub_type__c='Condominium plans' and status__c ='In pool'", 
				"status__c","In Progress");

		//generating WI
		objtransfer.generateRecorderJobWorkItems(objMappingPage.DOC_Condominium_plans, 1);

		String WorkItemQuery="SELECT Id,Name FROM Work_Item__c where Type__c='MAPPING'"
				+ "order by createdDate desc limit 1"; 

		HashMap<String, ArrayList<String>> responseWIDetails = salesforceAPI.select(WorkItemQuery);
		String WorkItemNo=responseWIDetails.get("Name").get(0);
			
		//Searching for the WI genrated
		objMappingPage.globalSearchRecords(WorkItemNo); 
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%'and"
				+ " Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);
		String apnId = responseAPNDetails.get("Id").get(0);
		
		String ApnfromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		
		//updating APN details
		String query = "Select Id from Parcel__c where Name = '"+ApnfromWIPage+"'";
		salesforceAPI.update("Parcel__c",query,jsonObject);	
		
		query = "Select Id from Parcel__c where Name = '"+apn+"'";
		salesforceAPI.update("Parcel__c",query,jsonObject);	
		
		ReportLogger.INFO("Updating details on parcel : " + apn);
		objMappingPage.deleteOwnershipFromParcel(apnId);
		objMappingPage.deleteRelationshipInstanceFromParcel(apn);
		objMappingPage.deleteParcelSitusFromParcel(apn);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);
		objMappingPage.deleteExistingWIFromParcel(apn);
		
		
		String parentAuditTrailNumber = objWorkItemHomePage
				.getElementText(objWorkItemHomePage.firstRelatedBuisnessEvent);
		
		objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);
		String eventId=objMappingPage.getFieldValueFromAPAS(trail.eventIdLabel);
		String requestOrigin=objMappingPage.getFieldValueFromAPAS(trail.requestOriginLabel);
		
		driver.navigate().back();
		objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
	
		objMappingPage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);
		objWorkItemHomePage.waitForElementToBeVisible(5, objWorkItemHomePage.secondRelatedBuisnessEvent);
		objWorkItemHomePage.scrollToBottom();
		
		softAssert.assertEquals(trail.getFieldValueFromAPAS(trail.relatedCorrespondenceLabel), parentAuditTrailNumber,
				"SMAB-T3385: Verifying that business event created by Recorder feed for Mapping WI is child of parent Recorded correspondence event");

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.relatedBusinessEventLabel), "",
				"SMAB-T3385:Verifying that related business event field in business event created by Recorder feed for Mapping WI  is blank");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.eventIdLabel), eventId,
				"SMAB-T3385:Verifying that Event ID field in the business event created by Recorder feed for Mapping WI should be inherited from parent correspondence event");
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(trail.requestOriginLabel), requestOrigin,
				"SMAB-T3385:Verifying that business event created by Recorder feed for Mapping WI inherits the Request Origin from parent event");
		
		driver.navigate().back();
		objMappingPage.globalSearchRecords(apn);
		String primarySitusValue = objParcelsPage.createParcelSitus(apn);

		try {
			objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
			objParcelsPage.createOwnershipRecord(assesseeName1, hashMapCreateOwnershipRecordData);
		}
		catch(Exception e) {
			ReportLogger.INFO("Fail to create ownership record : "+e);
		}

		objMappingPage.globalSearchRecords(apn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.createCharacteristicsOnParcel(hashMapImprovementCharacteristicsData,apn);

		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.createCharacteristicsOnParcel(hashMapLandCharacteristicsData,apn);
		
		objMappingPage.logout();
		Thread.sleep(5000);

		//Mapping user logs in and perform mapping action on the WI genrated
		objMappingPage.login(loginUser);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.appLauncher);
		objMappingPage.closeDefaultOpenTabs();
		objMappingPage.globalSearchRecords(WorkItemNo);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		softAssert.assertTrue(!(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiEventId).equals(" ")),
				"SMAB-T3513: Verfiying the Event ID of WI genrated for given Recorded Document");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS
				(objWorkItemHomePage.wiRelatedActionDetailsPage),"Condominium plans" ,
				"SMAB-T3511: Verfiying the Related Action of WI genrated for given Recorded Document");

		softAssert.assertTrue(!objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.editEventIdButton),
				"SMAB-T3513-This field should not be editable.");

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Fill data  in mapping screen
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitMappingData.get("Action"));		
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, hashMapSplitMappingData.get("Are taxes fully paid?"));
		objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel, hashMapSplitMappingData.get("Reason code"));
		objMappingPage.enter(objMappingPage.numberOfChildNonCondoTextBoxLabel,"2");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

	
		//second screen of mapping action
		objMappingPage.waitForElementToBeClickable(10, objMappingPage.generateParcelButton);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		//updating child parcel size in second screen on mapping action 
		for(int i=1;i<=gridDataHashMap.get("APN").size();i++) {
			objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen,"100",i);
		}		
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		HashMap<String, ArrayList<String>> gridChildApnHashMap = objMappingPage.getGridDataInHashMap(1);

		//switching to main screen
		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(WorkItemNo);

		//validate that The "Return " functionality for parcel mgmt activities should work for all these work items.
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertEquals(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName).getText(),"Update Parcel(s)",
				"SMAB-T3512-validate that The Return functionality for parcel mgmt activities should work for all these work items.");
		driver.switchTo().window(parentWindow);
		
		objMappingPage.searchModule(PARCELS);
		objMappingPage.closeDefaultOpenTabs();
		objMappingPage.globalSearchRecords(gridChildApnHashMap.get("APN").get(0));
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		//		Verify Neighborhood Code value is inherited from Parent to Child Parcels
		softAssert.assertEquals(
				objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelDistrictNeighborhood, "Summary Values"),
				responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T3601: Verify District/Neighborhood Code of Child Parcel is inheritted from first Parent Parcel");

		// Step 10: Verify TRA value is inherited from Parent to Child Parcels
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelTRA, "Parcel Information"),
				responseTRADetails.get("Name").get(0),
				"SMAB-T3601: Verify TRA of Child Parcel is inheritted from first Parent Parcel");

		// Step 11: Validation that child parcel primary situs is inherited from parent parcel
		String childPrimarySitusValue = objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus,
				"Parcel Information");
		softAssert.assertEquals(childPrimarySitusValue,primarySitusValue,
				"SMAB-T3601: Validation that primary situs of child parcel is same as primary sitrus of parent parcel");


		//Verify PUC is inherited from Parent to Child Parcels
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),
				responsePUCDetails.get("Name").get(0),
				"SMAB-T3601: Verify PUC of Child Parcel is inheritted from first Parent Parcel");

		String childApnstatus = objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information");

		objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
		HashMap<String, ArrayList<String>> gridownershipDataHashMap = objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridownershipDataHashMap.get("Owner").get(0),assesseeName1,
				"SMAB-T3465: Verify Owner of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertContains(gridownershipDataHashMap.get("Ownership Percentage").get(0),hashMapCreateOwnershipRecordData.get("Ownership Percentage"),
				"SMAB-T3465: Verify Ownership Percentage of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(gridownershipDataHashMap.get("Status").get(0),hashMapCreateOwnershipRecordData.get("Status"),
				"SMAB-T3465: Verify Status of Child Parcel is inheritted from first Parent Parcel");

		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		HashMap<String, ArrayList<String>> gridCharacteristicsDataHashMap = objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridCharacteristicsDataHashMap.get("Characteristics Screen").get(0),
				hashMapImprovementCharacteristicsData.get("Characteristics Screen"),
				"SMAB-T3465: Verify Characteristics Screen of Child Parcel is inheritted from first Parent Parcel");
		softAssert.assertEquals(gridCharacteristicsDataHashMap.get("Characteristics Screen").get(1),
				hashMapLandCharacteristicsData.get("Characteristics Screen"),
				"SMAB-T3465: Verify Characteristics Screen of Child Parcel is inheritted from first Parent Parcel");

		// Step 13: Verify Status of Parent & Child Parcels before WI completion
		objMappingPage.globalSearchRecords(apn);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),
				"In Progress - To Be Expired","SMAB-T3465: Verify Status of Parent Parcel: ");
		softAssert.assertEquals(childApnstatus, "In Progress - New Parcel","SMAB-T2574, SMAB-T3573: Verify Status of Child Parcel: ");

		objMappingPage.globalSearchRecords(WorkItemNo);
		String newApnfromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		softAssert.assertEquals(newApnfromWIPage,apn,"SMAB-T3601:Verify parent parcel updated on WI");

		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		
		objMappingPage.login(users.MAPPING_SUPERVISOR);
		objMappingPage.waitForElementToBeVisible(objWorkItemHomePage.appLauncher, 10);
		objMappingPage.closeDefaultOpenTabs();
		objMappingPage.globalSearchRecords(WorkItemNo);
        objWorkItemHomePage.completeWorkItem(); 
        driver.navigate().refresh();
       
		objMappingPage.waitForElementToBeVisible(objWorkItemHomePage.linkedItemsWI, 10);
		objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsWI);
        
        //navigating to business event audit trail
        objWorkItemHomePage.scrollToElement(objWorkItemHomePage.secondRelatedBuisnessEvent);
        objWorkItemHomePage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);
		String auditTrailNo = objWorkItemHomePage.getFieldValueFromAPAS("Name", "");
		ReportLogger.INFO("Audit Trail no:" +auditTrailNo);
		softAssert.assertContains(objMappingPage.getFieldValueFromAPAS("Event Library"),
				"Split","SMAB-T3469: Verify event library updated from draft to Split");
		objWorkItemHomePage.logout();
	}
	
	/**
	 * This method is to verify the generation of Interim Parcels for Split Mapping Action and various validation around it
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2882, SMAB-T2895, SMAB-T3826: Verify generation of Interim Parcels for Split Mapping Action and validation around it", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyGenerationOfInterimParcelForSplitMappingAction(String loginUser) throws Exception {
		
		//Fetching Interim parcels
		String queryInterimAPNValue = "Select name,ID  From Parcel__c where name like '8%' and name like '%0'"
				  		+ "and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		
		String apn1 = salesforceAPI.select(queryInterimAPNValue).get("Name").get(0);
		String apn1Id = salesforceAPI.select(queryInterimAPNValue).get("Id").get(0);
		
		//Getting Active Non-Condo Parcel
		String queryAPNValue = "Select name,ID  From Parcel__c where Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and (Not Name like '1%') and (Not Name like '8%') and (Not Name like '%990') limit 1";
		String apn2 = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String apn2Id = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		//Deleting the current ownership records for all the Parcel records
		objMappingPage.deleteOwnershipFromParcel(apn1Id);
		objMappingPage.deleteOwnershipFromParcel(apn2Id);
		
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn1);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn2);
		
		//Update District/Neighborhood on the Non-Condo Parcel
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
		
		//Updating the status of all parcels
		salesforceAPI.update("Parcel__c", apn1Id, "Status__c", "Active");
		salesforceAPI.update("Parcel__c", apn2Id, "Status__c", "Active");
		salesforceAPI.update("Parcel__c", apn1Id, "Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", apn2Id, "Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithoutAllFields");
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to perform Combine Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);
		
		// Step 3: Creating Manual work item for the Interim Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Select the Split value in Action field
		ReportLogger.INFO("Select the 'Split' Action and Yes in Tax field");
		Thread.sleep(2000);  //Allows screen to load completely
		
		//Step 6: Fill data in mapping screen
		ReportLogger.INFO("Fill Mapping data for Split action");
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitMappingData.get("Action"));		
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, hashMapSplitMappingData.get("Are taxes fully paid?"));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel, hashMapSplitMappingData.get("Reason code"));
		objMappingPage.enter(objMappingPage.numberOfChildNonCondoTextBoxLabel,"2");
		
		//Step 7: Update Interim Parcel count to 0
		ReportLogger.INFO("Update Interim Parcel count to 0 and click NEXT button to navigate to next screen");
		objMappingPage.enter(objMappingPage.numberOfIntermiParcelLabel,"0");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		
		//Step 8: Validate that ALL fields THAT ARE displayed on second screen
		ReportLogger.INFO("Validate the Grid values");
		objMappingPage.waitForElementToBeClickable(10, objMappingPage.generateParcelButton);
		HashMap<String, ArrayList<String>> gridDataInterimParcelHashMap =objMappingPage.getGridDataInHashMap();
	
		softAssert.assertTrue(gridDataInterimParcelHashMap.get("APN").get(0).startsWith("8"),
				"SMAB-T3826: Validate system generates the first Parcel as Interim : " + gridDataInterimParcelHashMap.get("APN").get(0));
		softAssert.assertTrue(gridDataInterimParcelHashMap.get("APN").get(1).startsWith("8"),
				"SMAB-T3826: Validate system generates the second Parcel as Interim : " + gridDataInterimParcelHashMap.get("APN").get(1));		
		
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		
		// Step 9: Update Interim Parcel count to -1
		ReportLogger.INFO("Update Interim Parcel count to -1 and click NEXT button");
		objMappingPage.enter(objMappingPage.numberOfIntermiParcelLabel,"-1");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- Number of Interim Parcel can not be less than 0",
				"SMAB-T2895: Validate that user is not able to move to the next screen as number of Interim Parcel can not be less than 0");
		
		// Step 10: Update Interim Parcel count to 1
		ReportLogger.INFO("Update Interim Parcel count to 1 and click NEXT button");
		objMappingPage.enter(objMappingPage.numberOfIntermiParcelLabel,"1");
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		
		//Step 11: Validate that ALL fields THAT ARE displayed on second screen
		ReportLogger.INFO("Validate the Grid values");
		objMappingPage.waitForElementToBeClickable(10, objMappingPage.generateParcelButton);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		
		String childAPNNumber1 =gridDataHashMap.get("APN").get(0);
		String childAPNNumber2 =gridDataHashMap.get("APN").get(1);
		String childAPNNumber3 =gridDataHashMap.get("APN").get(2);
		
		softAssert.assertTrue(!childAPNNumber1.startsWith("8"),
				"SMAB-T2882: Validate system generates the first Parcel as Non-Condo : " + childAPNNumber1);
		softAssert.assertTrue(!childAPNNumber2.startsWith("8"),
				"SMAB-T2882: Validate system generates the second Parcel as Non-Condo : " + childAPNNumber2);
		softAssert.assertTrue(childAPNNumber3.startsWith("8"),
				"SMAB-T2882: Validate system generates the third Parcel as Interim : " + childAPNNumber3);
		
		//Step 12: Validate the columns that are enabled/disabled for Interim parcel generated
		ReportLogger.INFO("Validate the columns that are enabled or disabled in the grid for Interim parcel");
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Legal Description*", 2),"SMAB-T2882: Validation that Legal Description column is editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*", 2),"SMAB-T2882: Validation that TRA column is not editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs", 2),"SMAB-T2882: Validation that Situs column is not editable");
        softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Reason Code*", 2),"SMAB-T2882: Validation that Reason Code column is editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*", 2),"SMAB-T2882: Validation that District/Neighborhood column is not editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*", 2),"SMAB-T2882: Validation that Use Code column is not editable");
        softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size(SQFT)*", 2),"SMAB-T2882: Validation that Parcel Size column is editable");
        softAssert.assertTrue(objMappingPage.getAttributeValue(objMappingPage.locateElement("//tr[@data-row-key-value='row-2']//th[@data-label='APN']", 30), "class").equals("grey-out-column slds-cell-edit"), "SMAB-T2882: Validation that APN column is not editable");
     
        //Step 13: Validate the generation interim parcel, non-condo parcel and status of parent interim parcel
        ReportLogger.INFO("Generate the Parcels");
        objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
        
        Thread.sleep(1000);//To avoid regression failure
        objMappingPage.Click(objMappingPage.getButtonWithText(childAPNNumber1));
        objMappingPage.waitForElementToBeVisible(6, objParcelsPage.LongLegalDescriptionLabel);
        softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("APN", "Parcel Information"),childAPNNumber1,
				"SMAB-T3826: Validate that first parcel generated in the system is Non-Condo parcel");
        
        objMappingPage.globalSearchRecords(childAPNNumber2);
        objMappingPage.waitForElementToBeVisible(6, objParcelsPage.LongLegalDescriptionLabel);
        softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("APN", "Parcel Information"),childAPNNumber2,
				"SMAB-T3826: Validate that second parcel generated in the system is Non-Condo parcel");
        
        objMappingPage.globalSearchRecords(childAPNNumber3);
        objMappingPage.waitForElementToBeVisible(6, objParcelsPage.LongLegalDescriptionLabel);
        softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("APN", "Parcel Information"),childAPNNumber3,
				"SMAB-T3826: Validate that third parcel generated in the system is Interim parcel");
        
        objMappingPage.globalSearchRecords(apn1);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"In Progress - To Be Expired",
				"SMAB-T3826: Validate the Status of parent interim parcel : " + apn1);
        
        driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	
	/**
	 * This method is to  Verify  that Divided Interest Parcel generated
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T4390,SMAB-T4034,SMAB-T3310,SMAB-T3311,SMAB-T3312,SMAB-T3313,"
			+ "SMAB-T3314,SMAB-T3282:Verify that user is able to perform Split mapping action "
			+ "having Divided Interest parcel as Parent APN ", dataProvider = "loginMappingUser", 
			dataProviderClass = DataProviders.class, groups = {"Regression","ParcelManagement" })
	public void  ParcelManagement_VerifySplitDividedInterestParcelGeneration(String loginUser) throws Exception {

		JSONObject jsonDividedInterestObject= new JSONObject();
		//Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id"
				+ "  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c "
				+ "where Status__c='Active') limit 1");
		String PUC = responsePUCDetails.get("Name").get(0);
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c where Name != NULL limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue="Legal PM 85/25-260";
		String parcelSize	= "200";	

		jsonDividedInterestObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonDividedInterestObject.put("Status__c","Active");
		jsonDividedInterestObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonDividedInterestObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonDividedInterestObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonDividedInterestObject.put("Lot_Size_SQFT__c",parcelSize);

        objMappingPage.login(users.SYSTEM_ADMIN);
        String createNewParcel = testdata.MANUAL_PARCEL_CREATION_DATA;
		Map<String, String> hashMapCreateNewParcel = objUtil.generateMapFromJsonFile(createNewParcel,
				"DataToDividedInterestCreateNewParcel");
		String parentDividedInterestAPN1 = hashMapCreateNewParcel.get("APN1");
		String newParcelNumber1 = hashMapCreateNewParcel.get("Parcel Number1");
		
        objMappingPage.searchModule(PARCELS);
		objParcelsPage.createNewParcel(parentDividedInterestAPN1,newParcelNumber1,PUC);
		String nonDivideInterestAPN = objMappingPage.fetchActiveAPN();

		objWorkItemHomePage.logout();

	    String apnLike = parentDividedInterestAPN1.substring(0,10);
		String queryExistingAPNValue = "Select name,status__c From Parcel__c where"
				+ " Name like '"+apnLike+"%' and (not name like '%0') order by name desc";
	    HashMap<String, ArrayList<String>> responseExistingAPNDetails = salesforceAPI.select(queryExistingAPNValue);
		String apnPresentInSystem =responseExistingAPNDetails.get("Name").get(0);
		int totalNoOfApns = responseExistingAPNDetails.get("Name").size();
		int noOfApnCanBecreated = 9-totalNoOfApns;
		ReportLogger.INFO("No of parcels can be created: "+noOfApnCanBecreated);
		
		// get last divided interest parcel in system of map book and map page = "297-097"
	    String apnLike1 = parentDividedInterestAPN1.substring(0,7);
		String queryExistingAPNValue1 = "Select name,status__c From Parcel__c where"
				+ " Name like '"+apnLike1+"%' and  name like '%0' order by name desc";
	    HashMap<String, ArrayList<String>> responseExistingAPNDetails1 = salesforceAPI.select(queryExistingAPNValue1);
		String apnPresentInSystemEndsWith0 =responseExistingAPNDetails1.get("Name").get(0);
		
		//updating Parcel details
		String queryApnId = "SELECT Id FROM Parcel__c where Name in('"+
				parentDividedInterestAPN1+"')";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryApnId);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonDividedInterestObject);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithSitusData");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(parentDividedInterestAPN1);

		// Step 3: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(3000);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Enter DividedInterest parcel number as Parent parcel
		ReportLogger.INFO("child parcel have same map book, map page and ends with 0 if parent parcel is divideinterest parcel : " + parentDividedInterestAPN1);
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,parentDividedInterestAPN1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		objMappingPage.waitForElementToBeVisible(objMappingPage.reasonCodeField);
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"");
		objMappingPage.enter(objMappingPage.numberOfChildNonCondoTextBoxLabel, "2");
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
		
		//Second screen of mapping action
		objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		// Validate child parcel have have same map book and map page no as parent parcel and
		//doesnot ends with 0 if Parent parcel is dividedinterest parcel
		String parentAPNComponent[] = parentDividedInterestAPN1.split("-");
		String	childAPNNumber=gridDataHashMap.get("APN").get(0);	
		String childAPNComponents[] = childAPNNumber.split("-");
		softAssert.assertEquals(childAPNComponents[0],parentAPNComponent[0],
				"SMAB-T4390: Validation that MAP BOOK of parent and child parcels are same" );
		softAssert.assertEquals(childAPNComponents[1],parentAPNComponent[1],
				"SMAB-T4390: Validation that MAP page of parent and child parcels are same");
		softAssert.assertTrue(!childAPNNumber.endsWith("0"),
				"SMAB-T4390: Validation that child APN number should not ends with 0");
		
		// if child apn to be generated is Regular Parcel
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		String nextGeneratedParcel = objMappingPage.generateNextAvailableAPN(apnPresentInSystemEndsWith0);
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,nextGeneratedParcel);
		objMappingPage.enter(objMappingPage.numberOfChildNonCondoTextBoxLabel, "2");
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
		
		//Second screen of mapping action
		objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"Only divided"
				+ " interest child parcels are allowed to be created when a parent parcel is a divided interest.",
				"SMAB-T4034: Validate regaular child parcel cant be generated from divided interest parcel");
		
		// if child apn no is greated than available limit 
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"");
		int  noOfparceltoBeCreated = noOfApnCanBecreated+1;
		objMappingPage.enter(objMappingPage.numberOfChildNonCondoTextBoxLabel, String.valueOf(noOfparceltoBeCreated));
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
	
		softAssert.assertContains(objMappingPage.getErrorMessage(),"The amount of child divided"
				+ " parcels entered exceeds the total amount available",
				"SMAB-T4391: Validate The amount of child divided parcels entered exceeds the total amount available");
		
		//Validate dividedinterest child 1st 8 char is same as parent parcel 
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"");
		objMappingPage.enter(objMappingPage.numberOfChildNonCondoTextBoxLabel,"2");
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
		ReportLogger.INFO("dividedinterest child 1st 8 char is same as parent parcel");
		String firstChildAPN = gridDataHashMap.get("APN").get(0);	
		String firstchildAPNNumber[] = firstChildAPN.split("-");
		String updatechildlastdigit = firstchildAPNNumber[0]+firstchildAPNNumber[1]+
				String.valueOf(Integer.parseInt(firstchildAPNNumber[2]) +101);
		objMappingPage.updateMultipleGridCellValue(objMappingPage.apnColumnSecondScreen,
				updatechildlastdigit,1);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"To override an APN "
				+ "with divided interest parcel, the first 8 characters must be same as the Parent APN",
				"SMAB-T3311: Validation that any mapping action if child APN is overwritten with "
				+ "divided interest APN such that first 8 characters of APN are not the same as "
				+ "that of first parent parcel, error message is displayed");

		//Validate child parcel cannot be parcel which is present in system 
		ReportLogger.INFO("dividedinterest child parcel cannot be parcel which is present in system");
		objMappingPage.updateMultipleGridCellValue(objMappingPage.apnColumnSecondScreen,
				apnPresentInSystem,1);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0)," already exist",
				"SMAB-T3312: Validate that if child APN is "
				+ "overwritten with a divided interest APN such that no divided interest APN is"
				+ " available error message is displayed");
		
		// Validate child parcel cannot skip next available apn
		ReportLogger.INFO("dividedinterest child parcel cannot skip next available apn");
		String apnInSystem[] = apnPresentInSystem.split("-");
		String updateNextApn = apnInSystem[0]+apnInSystem[1]+apnInSystem[2].substring(0,2)+
					String.valueOf(Integer.parseInt(apnInSystem[2].substring(2)) +2);
			objMappingPage.updateMultipleGridCellValue(objMappingPage.apnColumnSecondScreen,
					updateNextApn,1);
			objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
			gridDataHashMap =objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"APN cannot be skipped",
					"SMAB-T3313: Validate that if child APN is overwritten with divided interest APN which is not the next available"
					+ " APN, error message is displayed");
		    
	    driver.switchTo().window(parentWindow);		
		objWorkItemHomePage.logout();

	    
	}
	
	/**
	 *This method is to  Verify  the Audit trail 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3975,SMAB-T3974,SMAB-T3728,SMAB-T3816,SMAB-T3727,SMAB-T3647: Verify many to many audit trail", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {"Regression","ParcelManagement" })

	public void ParcelManagement_VerifyAuditTrailForSplitMappingAction(String loginUser) throws Exception {

		JSONObject jsonObjectNew = objMappingPage.getJsonObject();

		//Fetching parcels that are Active 
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE "
				+ " (Not Name like '134%') and Id NOT IN (SELECT APN__c FROM Work_Item__c"
				+ " where type__c='CIO') and Status__c = 'Active' Limit 1";

		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		
		//updating fields of parcels
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,Id FROM PUC_Code__c "
				+ "where Legacy__c = 'NO' limit 1");
	
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		
		String legalDescriptionValue="Legal PM 85/25-260";
		String parcelSize	= "200";		
		jsonObjectNew.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObjectNew.put("Status__c","Active");
		jsonObjectNew.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObjectNew.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObjectNew.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObjectNew.put("Lot_Size_SQFT__c",parcelSize);

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObjectNew);
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithoutAllFields");
					
		//login with mapping user
		objMappingPage.login(loginUser);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.Click(objMappingPage.getButtonWithText(objParcelsPage.createNewParcelButton));
		
		objMappingPage.waitForElementToBeVisible(objMappingPage.createNewParcelErrorMessage,10);
		
		softAssert.assertEquals(objMappingPage.createNewParcelErrorMessage.getText(),
				"In order to create a new parcel, leverage the \"Mapping Action\" feature",
				"SMAB-T3727:Verify that mapping staff is not able to create new apn");
	
		objMappingPage.globalSearchRecords(apn1);

		//creating WI on parcel
		String WorkItemNo=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		ReportLogger.INFO("Reference : " + reasonCode);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//populating fields on mapping action screen
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		ReportLogger.INFO("Fill Mapping data for Split action");
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));		
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, hashMapSplitActionMappingData.get("Are taxes fully paid?"));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel, hashMapSplitActionMappingData.get("Reason code"));
		objMappingPage.enter(objMappingPage.numberOfChildNonCondoTextBoxLabel,"1");
		
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel2,"101-090-800");
		objMappingPage.enter(objMappingPage.numberOfChildCondoTextBoxLabel,"1");
		objMappingPage.enter(objMappingPage.commentsTextBoxLabel,"Perform Parcel Split");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Non Condo Parcel Number cannot start with 100-199, Please enter valid Parcel Number",
				"SMAB-T3974: Verify the APNs allowed in '\"First Non Condo Parcel Number\"' field while performing mapping actions is: - (parcels below 100),133, 134,"
				+ " and range between 200 - 999, excluding the 800 map book");
		
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel2,"133-090-800");
		objMappingPage.enter(objMappingPage.firstCondoTextBoxLabel,"002-090-800");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Condo Parcel Number should start with 100-199 only, Please enter valid Parcel Number",
				"SMAB-T3975: Verify the APNs allowed in '\"First Non Condo Parcel Number\"' field while performing mapping actions is: - (parcels below 100),133, 134,"
				+ " and range between 200 - 999, excluding the 800 map book");
		
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel2,"");
		objMappingPage.enter(objMappingPage.firstCondoTextBoxLabel,"");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

//		objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);
		objMappingPage.waitForElementToBeVisible(10,objMappingPage.generateParcelButton);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
	       
		   //updating child parcels size in second screen on mapping action 
	       for(int i=1;i<=gridDataHashMap.get("APN").size();i++) {
	            objMappingPage.updateMultipleGridCellValue
	            (objMappingPage.parcelSizeColumnSecondScreen,"200",i);
	       }
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(10,objMappingPage.performAdditionalMappingButton);
			
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(1);
		String childmap = childAPNNumber.substring(0,3);
		Boolean childMapBook = childmap.matches("(10[0-9]|1[1-9][0-9])");
		softAssert.assertTrue(childMapBook,"SMAB-T3647:Child Map page is between 100-199");
		int totalChildSize =Integer.parseInt(gridDataHashMap.get("Parcel Size(SQFT)*").get(0))+Integer.parseInt(gridDataHashMap.get("Parcel Size(SQFT)*").get(1));
		softAssert.assertTrue(totalChildSize!=Integer.parseInt(parcelSize), "SMAB-T3647: Total parent size is not equal to total child parcel size");

		driver.switchTo().window(parentWindow);
        objWorkItemHomePage.logout();
        Thread.sleep(5000);
        
        //login with supervisor
        objMappingPage.login(users.MAPPING_SUPERVISOR);
        objMappingPage.searchModule(WORK_ITEM);
        objMappingPage.globalSearchRecords(WorkItemNo);
        
        //Completing the workItem
        objWorkItemHomePage.completeWorkItem(); 
        String workItemStatus = objMappingPage.getFieldValueFromAPAS("Status", "Information");
		softAssert.assertEquals(workItemStatus, "Completed", "SMAB-T3647: Validation WI completed successfully");
        driver.navigate().refresh(); 
        objMappingPage.waitForElementToBeVisible(objWorkItemHomePage.linkedItemsWI, 10);
		objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsWI);
        
        //navigating to business event audit trail
        objWorkItemHomePage.scrollToElement(objWorkItemHomePage.secondRelatedBuisnessEvent);
        objWorkItemHomePage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);
		String auditTrailNo = objWorkItemHomePage.getFieldValueFromAPAS("Name", "");
		ReportLogger.INFO("Audit Trail no:" +auditTrailNo);
		
        String description= objWorkItemHomePage.getFieldValueFromAPAS(trail.descriptionLabel);
		String comments= hashMapSplitActionMappingData.get("Comments");
		reasonCode= hashMapSplitActionMappingData.get("Reason code");
			
		String queryWIDescription = "SELECT Id,Description__c FROM Work_Item__c where Name='" + WorkItemNo+"'";
		HashMap<String, ArrayList<String>> responseWIdesc = salesforceAPI.select(queryWIDescription);
		String wiDescription=responseWIdesc.get("Description__c").get(0);
		softAssert.assertContains(description,comments+" "+wiDescription+" "+reasonCode,
				"SMAB-T3816,SMAB-T3728: Verify that comment,description,reason code provided during mapping actionis present in description field of the audit trail");		
		objWorkItemHomePage.logout();
		}
	

	@Test(description = "SMAB-T2955,SMAB-T2880,SMAB-T2878,SMAB-T2953,SMAB-T2952,SMAB-T2877,SMAB-T2954,SMAB-T2879,SMAB-T2951,SMAB-T2876,SMAB-T2950,SMAB-T2814SMAB-T2956,SMAB-T2881: Verify Parcel size validations for Split mapping action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {"Regression","ParcelManagement" })

	public void ParcelManagement_VerifyParcelSizeValidationsForSplitMappingAction(String loginUser) throws Exception {

		// Fetching parcels that are Active with same Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '134%') and  Primary_Situs__c !=NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1 = responseAPNDetails.get("Name").get(0);


		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");


		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI.select(
				"SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
		String legalDescriptionValue = "Legal PM 85/25-260";
		String districtValue="District01";
		
		JSONObject jsonParcelObject = objMappingPage.getJsonObject();
		jsonParcelObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonParcelObject.put("Short_Legal_Description__c", legalDescriptionValue);
		jsonParcelObject.put("TRA__c", responseTRADetails.get("Id").get(0));
		jsonParcelObject.put("Lot_Size_SQFT__c", "200");
		jsonObject.put("District__c",districtValue);
		jsonParcelObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonParcelObject);
		
		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapCombineActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithoutAllFields");
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to perform Combine
		// Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// populating fields on mapping action screen
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.actionDropDownLabel);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel, "Perform Parcel Split");
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, "Yes");
		objMappingPage.enter(objMappingPage.numberOfChildCondoTextBoxLabel, "2");
		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "-10");
		objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
				"- Please provide valid field value: Net Land Loss (SQ FT)",
				"SMAB-T2955,SMAB-T2880:Verify error message successfully when there is negative value for Net Loss field");

		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");
		objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "-10");
		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
				"- Please provide valid field value: Net Land Gain (SQ FT)",
				"SMAB-T2955,SMAB-T2880:Verify error message successfully when there is negative value for Net Gain field");

		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "b1");
		objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageOnFirstCustomScreen),
				"Enter a valid value.",
				"SMAB-T2955:Verify error message successfully when there is invalid value for Net Loss field");

		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");
		objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "b1");
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "");

		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageOnFirstCustomScreen),
				"Enter a valid value.",
				"SMAB-T2955:Verify error message successfully when there is invalid value for Net Gain field");

		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "100");
		objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "100");
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
				"- Please populate either \"Net Land Loss (SQ FT)\" or \"Net Land Gain (SQ FT)\"",
				"SMAB-T2953,SMAB-T2955,SMAB-T2878:Verify error message successfully when both Net Loss and Net Gain field are populated");

		objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "020");
		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.legalDescriptionFieldSecondScreen);

		objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "100", 1);
		objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "120", 2);

		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageonSecondCustomScreen),
				"Parent Parcel Size = 200, Net Land Loss = 0, Net Land Gain = 020, Total Child Parcel(s) Size = 220.",
				"SMAB-T2952,SMAB-T2877:Verify message on Second custom screen when user enter Net Gain value");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
				"Total Child Parcel (s) size currently match the Parent's Parcel Size!",
				"SMAB-T2954,SMAB-T2952,SMAB-T2879:Verify message when parent parcel size matches the child's parcel size when there is Net Gain");

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));

		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "20");
		objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "");

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

		objMappingPage.waitForElementToBeVisible(objMappingPage.legalDescriptionFieldSecondScreen);

		objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "100", 1);
		objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "80", 2);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageonSecondCustomScreen),
				"Parent Parcel Size = 200, Net Land Loss = 20, Net Land Gain = 0, Total Child Parcel(s) Size = 180.",
				"SMAB-T2951,SMAB-T2876;Verify message on Second custom screen when user enter Net Loss value ");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
				"Total Child Parcel (s) size currently match the Parent's Parcel Size!",
				"SMAB-T2951,SMAB-T2879;Verify message when parent parcel size matches the child's parcel size when there is Net Loss");

		objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "-100");
		objMappingPage.clickAction(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
				"- Please provide valid field value: Parcel Size (SQFT)",
				"SMAB-T2955: Verify error message on negative parcel size value");

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));

		objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");
		objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

		objMappingPage.waitForElementToBeVisible(objMappingPage.legalDescriptionFieldSecondScreen);

		objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "100", 1);
		objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "100", 2);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);

		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageonSecondCustomScreen),
				"Parent Parcel Size = 200, Net Land Loss = 0, Net Land Gain = 0, Total Child Parcel(s) Size = 200.",
				"SMAB-T2950,SMAB-T2814:verify message on second custom screen when there is no Net Loss and Net Gain");
		
		objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen, "", 1);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		ReportLogger.INFO("Parcels are generated");
		
		HashMap<String, ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();
		objParcelsPage.addParcelDetails(responsePUCDetails.get("Id").get(0), "Legal", districtValue,
				responseNeighborhoodDetails.get("Id").get(0), responseTRADetails.get("Id").get(0), "", gridDataHashMap,
				"APN");
		driver.switchTo().window(parentWindow);
		driver.navigate().refresh();
		objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentParcelSizeErrorMsg);
		String errorMsg = objWorkItemHomePage.parentParcelSizeErrorMsg.getText();
		objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
		softAssert.assertEquals(errorMsg,
				"Status: In order to submit or close the work item, the following field needs to be populated : Parcel Size (SqFt). Please navigate to the mapping custom screen to provide the necessary information.",
				"SMAB-T2956,SMAB-T2881:Expected error message is displayed when parcel size is missing");
		
		
		objWorkItemHomePage.logout();

	}
}