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
import org.hamcrest.core.IsNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.util.Strings;

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
			"Regression","ParcelManagement" })
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
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfChildNonCondoTextBoxLabel),"value"),"0",
				"SMAB-T2294: Validation that default value of Number of Child Non-Condo Parcels  is 0");
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfChildCondoTextBoxLabel),"value"),"0",
				"SMAB-T2294: Validation that default value of Number of Child Condo Parcels  is 0");

		//Step 9: Validating that reason code field is auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2613: Validation that reason code field is auto populated from parent parcel work item");

		//Step 10: Validating help icons
		objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
				"SMAB-T2481: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");

		objMappingPage.Click(objMappingPage.helpIconLegalDescription);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank.",
				"SMAB-T2481: Validation that help text is generated on clicking the help icon for legal description");

		objMappingPage.Click(objMappingPage.helpIconSitus);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent situs, leave as blank.",
				"SMAB-T2481: Validation that help text is generated on clicking the help icon for Situs text box");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.closeButton));

		//Step 11: Validating Error Message when both Number of Child Non-Condo & Condo Parcels fields contain 0
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Number of Child Non-Condo Parcels and/or Number of Child Condo parcels values total must be equivalent to two or greater.",
				"SMAB-T2294: Validation that error message is displayed when both Number of Child Non-Condo & Condo Parcels fields contain 0");


		//Step 12: entering incorrect map book in 'First Non Condo Parcel Number' & 'First Condo Parcel Number' fields
		Map<String, String> hashMapSplitActionInvalidData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithIncorrectData");
		objMappingPage.fillMappingActionForm(hashMapSplitActionInvalidData);

		//Step 13: Validating Error Message having incorrect map book data
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- Non Condo Parcel Number cannot start with 100, Please enter valid Parcel Number - Condo Parcel Number should start with 100 only, Please enter valid Parcel Number",
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
		String apn =  objMappingPage.fetchActiveAPN();

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;

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

		softAssert.assertEquals(actualTotalParcels,expectedTotalParcels,"SMAB-T2613: Verify total no of parcels getting generated");

		//Step 8: Validating error message when APN entered is not next available number
		//Considering Map Book & Map Page from the APN generated by system
		// and adding 100 to parcel number will become new APN which is not available next as per system
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		String childAPNComponents[] = childAPNNumber.split("-");
		String apnNotNextAvailable = childAPNComponents[0] + childAPNComponents[1] +
				String.valueOf(Integer.parseInt(childAPNComponents[2]) + 100);

		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,apnNotNextAvailable);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToDisappear(objMappingPage.xpathSpinner, 60);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.apnColumnSecondScreen);
		Thread.sleep(2000);

		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"The parcel entered is invalid since the following parcel is available",
				"SMAB-T2613: Validation that error message is displayed when parcel entered is not next abvaialble");

		//Step 9: Validating error message when APN entered contains alphabets
		//Considering Map Book & Map Page from the APN generated by system
		// and adding alphabets to parcel number
		String apnContainingAlphabets = childAPNComponents[0] + childAPNComponents[1] + "abc";

		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,apnContainingAlphabets);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2296: Validation that error message is displayed when parcel entered contains alphabets");

		//Step 10: Validating error message when APN entered contains special characters
		//Considering Map Book & Map Page from the APN generated by system
		// and adding special characters to parcel number
		String apnContainingspecialChars = childAPNComponents[0] + childAPNComponents[1] + "45&";

		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,apnContainingspecialChars);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2613: Validation that error message is displayed when parcel entered contains special characters");

		//Step 10: Validating error message when APN entered already exists in the system
		String alreadyExistingApn = apn;

		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,alreadyExistingApn);
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertContains(gridDataHashMap.get("Error Message").get(0),"The APN provided already exists in the system",
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
	@Test(description = "SMAB-T2541, SMAB-T2550, SMAB-T2551:Verify the Output Validations for Split Mapping Action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifySplitMappingActionOutputValidations(String loginUser) throws Exception {

		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String primarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ apn +"')").get("Name").get(0);
		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);

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

		//Step 7: Click Split Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

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
		softAssert.assertEquals(parentAPNStatus.get("Status__c").get(0),"In Progress - To Be Expired","SMAB-T2541: Verify Status of Parent Parcel: "+apn);
		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2541: Verify Status of Child Parcel: "+childAPNNumber1);
		softAssert.assertEquals(childAPN2Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2541: Verify Status of Child Parcel: "+childAPNNumber2);

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
		driver.switchTo().window(parentWindow);
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
		softAssert.assertEquals(parentAPNStatus.get("Status__c").get(0),"Retired","SMAB-T2551: Verify Status of Parent Parcel: "+apn);
		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"Active","SMAB-T2551: Verify Status of Child Parcel: "+childAPNNumber1);
		softAssert.assertEquals(childAPN2Status.get("Status__c").get(0),"Active","SMAB-T2551: Verify Status of Child Parcel: "+childAPNNumber2);

		//Step 16: Verify 2 new WIs are generated and linked to Child Parcels after parcel is split and WI is completed
		String queryToGetRequestType = "SELECT Work_Item__r.Request_Type__c FROM Work_Item_Linkage__c Where Parcel__c = '"+childAPNId1+"' OR Parcel__c = '"+childAPNId2+"'";
		response = salesforceAPI.select(queryToGetRequestType);
		int expectedWorkItemsGenerated = response.get("Work_Item__r").size();
		softAssert.assertEquals(expectedWorkItemsGenerated,2,"SMAB-T2551: Verify 2 new Work Items are generated and linked to each child parcel after parcel is split and WI is completed");

		softAssert.assertContains(response.get("Work_Item__r").get(0),"New APN - Update Characteristics & Verify PUC","SMAB-T2551: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after parcel is split and WI is completed");

	}
	/**
	 * This method is to Parcel Management- Verify that User is able to update Situs of child parcels from the Parcel mapping screen for "Split" mapping action
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2661:Parcel Management- Verify that User is able to update Situs of child parcels from the Parcel mapping screen for \"Split\" mapping action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {"Regression","ParcelManagement" })
	public void ParcelManagement_UpdateChildParcelSitus_SplitMappingAction(String loginUser) throws Exception {

		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL  and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

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
		String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;

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
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'Split' & Taxes Paid fields value as 'Yes'
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapSplitActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 6: editing situs for child parcel and filling all fields
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel));

		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.editSitusLabelSitusModal),
				"SMAB-T2661: Validation that Edit Situs label is displayed as heading of situs modal window in first screen");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.situsInformationLabelSitusModal),
				"SMAB-T2661: Validation that  Situs Information label is displayed in  situs modal window in first screen");
		objMappingPage.editSitusModalWindowFirstScreen(hashMapSplitActionMappingData);
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel),"value"),childprimarySitus,
				"SMAB-T2661: Validation that User is able to update a Situs for child parcel from the Parcel mapping screen");

		objMappingPage.fillMappingActionForm(hashMapSplitActionMappingData);

		//Step 7: Validation that primary situs on second screen is getting populated from situs entered in first screen
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		for(int i=0;i<gridDataHashMap.get("Situs").size();i++)
			softAssert.assertEquals(gridDataHashMap.get("Situs").get(i),childprimarySitus,
					"SMAB-T2661: Validation that System populates primary situs on second screen for child parcel number "+i+1+" with the situs value that was added in first screen");

		//Step 8: Click Split Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 9: Validation that primary situs on last screen screen is getting populated from situs entered in first screen
		for(int i=0;i<gridDataHashMap.get("Situs").size();i++)
			softAssert.assertEquals(gridDataHashMap.get("Situs").get(i),childprimarySitus,
					"SMAB-T2661: Validation that System populates primary situs on last screen for child parcel number "+i+" with the situs value that was added in first screen");

		//Step 10: Validation that primary situs of child parcel is the situs value that was added in first screen from situs modal window
		for(int i=0;i<gridDataHashMap.get("Situs").size();i++)
		{
			String primarySitusValueChildParcel=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ gridDataHashMap.get("APN").get(i) +"')").get("Name").get(0);
			softAssert.assertEquals(primarySitusValueChildParcel,childprimarySitus,
					"SMAB-T2661: Validation that primary situs of  child parcel number "+i+" has value that was entered in first screen through situs modal window");
		}

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "split" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2682:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"Split\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_SplitMappingAction_NoPrimarySitusTRA(String loginUser) throws Exception {
		String childAPNPUC;

		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c =NULL and TRA__c=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.SPLIT_MAPPING_ACTION;
		Map<String, String> hashMapSplitActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformSplitMappingActionWithSitusData");

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
		String legalDescription=gridDataHashMap.get("Legal Description").get(0);
		String tra=gridDataHashMap.get("TRA").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size(SQFT)").get(0);

		//Step 7: Click generate Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.getSuccessMessage();

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
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),districtNeighborhood,
				"SMAB-T2682: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2682: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
				"SMAB-T2682: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescription,
				"SMAB-T2682: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),tra,
				"SMAB-T2682: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),childAPNPUC,
				"SMAB-T2682: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size(SQFT)").get(0),parcelSizeSQFT,
				"SMAB-T2682: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2682: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2682: Validation that APN column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Legal Description"),"SMAB-T2682: Validation that Legal Description column on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA"),"SMAB-T2682: Validation that TRA column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2682: Validation that Situs column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Reason Code"),"SMAB-T2682: Validation that Reason Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd"),"SMAB-T2682: Validation that District/Neighborhood column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code"),"SMAB-T2682: Validation that Use Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size(SQFT)"),"SMAB-T2682: Validation that Parcel Size(SQFT) column should  be editable on retirning to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "Combine" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2682:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"Split\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_SplitMappingAction__WithPrimarySitusTRA(String loginUser) throws Exception {

		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

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
		String legalDescription=gridDataHashMap.get("Legal Description").get(0);
		String tra=gridDataHashMap.get("TRA").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size(SQFT)").get(0);

		//Step 7: Click Combine Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.getSuccessMessage();

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
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),districtNeighborhood,
				"SMAB-T2682: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2682: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
				"SMAB-T2682: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescription,
				"SMAB-T2682: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),tra,
				"SMAB-T2682: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),childAPNPUC,
				"SMAB-T2682: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size(SQFT)").get(0),parcelSizeSQFT,
				"SMAB-T2682: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2682: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	@Test(description = "SMAB-T2682:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"Split\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
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
		String legalDescription=gridDataHashMap.get("Legal Description").get(0);
		String tra=gridDataHashMap.get("TRA").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size(SQFT)").get(0);

		//Step 5: Click Combine Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.getSuccessMessage();

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
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2682: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),districtNeighborhood,
				"SMAB-T2682: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2682: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescription,
				"SMAB-T2682: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),tra,
				"SMAB-T2682: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),childAPNPUC,
				"SMAB-T2682: Validation that  System populates Use Code that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size(SQFT)").get(0),parcelSizeSQFT,
				"SMAB-T2682: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2682: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}


}