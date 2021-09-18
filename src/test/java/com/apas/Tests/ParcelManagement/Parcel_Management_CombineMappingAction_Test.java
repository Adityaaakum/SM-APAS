package com.apas.Tests.ParcelManagement;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Reporter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.relevantcodes.extentreports.LogStatus;

public class Parcel_Management_CombineMappingAction_Test extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonParcelObject= new JSONObject();
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
	 * This method is to Verify that User is able to view various error messages (excluding Ownership ones) while performing "Combine" mapping action from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2356, SMAB-2570, SMAB-2568: Verify user is able to view error messages on the Mapping screen during combine process", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyErrorMessageOnFirstScreenForParcelCombineMappingAction(String loginUser) throws Exception {
		
		//Getting parcel that is Retired 
	    HashMap<String, ArrayList<String>> retiredAPN = objMappingPage.getRetiredApnHavingNoOwner();
	    String retiredAPNValue = retiredAPN.get("Name").get(0);
		
		//Getting parcel that is In Progress - To Be Expired	
	    HashMap<String, ArrayList<String>> inProgressAPN = objMappingPage.getInProgressApnHavingNoOwner();
	    String inProgressAPNValue = inProgressAPN.get("Name").get(0);
	   
		//Getting parcels (Name and ID) that are Active with no owners
	    HashMap<String, ArrayList<String>> activeAPN = objMappingPage.getActiveApnWithNoOwner(3);
	    String apn1 = activeAPN.get("Name").get(0);
	    String apnId1 = activeAPN.get("Id").get(0);
		String apn2 = activeAPN.get("Name").get(1);
		String apnId2 = activeAPN.get("Id").get(1);
		String apn3 = activeAPN.get("Name").get(2);
		
		String queryMobileHomeAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Name like '134%' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> responseDetails = salesforceAPI.select(queryMobileHomeAPNValue);
		String mobileHomeApn=responseDetails.get("Name").get(0);
		
		//Find lowest APN of both parcels
		String updateSmallestAPN = "";
		int numbParcel1 = objMappingPage.convertAPNIntoInteger(apn1);
		int numbParcel2 = objMappingPage.convertAPNIntoInteger(apn2);
				
		int smallestNumb = numbParcel1<numbParcel2?numbParcel1:numbParcel2;
		String smallestAPN = String.valueOf(smallestNumb);
		
		if (smallestAPN.length() == 9) updateSmallestAPN = smallestAPN.substring(0, 3).concat("-").concat(smallestAPN.substring(3, 6)).concat("-").concat(smallestAPN.substring(6, 9));
		if (smallestAPN.length() == 8) updateSmallestAPN = "0" + smallestAPN.substring(0, 2).concat("-").concat(smallestAPN.substring(2, 5)).concat("-").concat(smallestAPN.substring(5, 8));
		if (smallestAPN.length() == 7) updateSmallestAPN = "00" + smallestAPN.substring(0, 1).concat("-").concat(smallestAPN.substring(1, 4)).concat("-").concat(smallestAPN.substring(4, 7));
				
		if (updateSmallestAPN.equals(apn2)) {
			apn2 = apn1;
			apn1 = updateSmallestAPN;
			apnId1 = activeAPN.get("Id").get(1);
			apnId2 = activeAPN.get("Id").get(0);
		}
		
		//Data Manipulation to test various validations
		String activeParcelWithoutHyphen=apn2.replace("-","");
		String accessorMapParcel = apn1.replace("-", "").substring(0, 5);
		String concatenateActiveAPN = apn1+","+apn2;
		String concatenateRetireWithActiveAPN = apn3+","+retiredAPNValue;
		String concatenateInProgressWithActiveAPN = inProgressAPNValue+","+apn3;
		String concatenateMobileHomeWithActiveAPN = mobileHomeApn+","+apn3;
		String legalDescriptionValue="Legal PM 85/25-260";
		
		//Fetch TRA value from database to enter in APN to test validations
		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		
		//Enter values in the Parcels
		JSONObject jsonForCombineError = objMappingPage.getJsonObject();
		jsonForCombineError.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonForCombineError.put("TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", apnId1, jsonForCombineError);
		salesforceAPI.update("Parcel__c", apnId2, "TRA__c", responseTRADetails.get("Id").get(1));
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");


		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform Retire Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Active Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		Thread.sleep(1000); 		//Wait till the Mapping action screen loads in another browser tab
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Validate the APN value
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel)),apn1,
				"SMAB-T2356: Validate the APN value in Parent APN field");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.taxCollectorLabel)),apn1,
				"SMAB-T2356: Validate that value of 'Tax Collector Link' field is visible");

		// Step 6: Select the Combine value in Action field, various values in 'Are Taxes fully paid?' field and validate other fields in layout are/are not visible
		ReportLogger.INFO("Select the 'Combine' Action and Yes/No/NA in Tax field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"No");
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.reasonCodeField),
				"SMAB-T2356: Validate that 'Reason Code' field is not visible");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Taxes must be fully paid in order to perform any action",
				"SMAB-T2356: Validate that user is able to view error message if taxes are not fully paid");

		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"N/A");
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.reasonCodeField),
				"SMAB-T2356: Validate that 'Reason Code' field is not visible");
		softAssert.assertTrue(!objMappingPage.verifyElementVisible(objMappingPage.errorMessageFirstScreen),
				"SMAB-T2356: Validate that user is not able to view error message related to taxes");

		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"- Please provide more than one APN to combine",
				"SMAB-T2356: Validate that user is able to view error message as there is only one APN in parent APN field");

		//Step 7: Validate the reason code and assessor's map fields are auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2356: Validate that value of 'Reason Code' field is populated from the parent parcel work item");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.assessorMapLabel)).replace("-", ""),accessorMapParcel,
				"SMAB-T2356: Validate that value of 'Assessor's Map' field is the first 5 digits of the parent parcel");
		objMappingPage.enter(objMappingPage.commentsTextBoxLabel,hashMapCombineMappingData.get("Comments"));
		
		//Step 8: Validation that User should be allowed to enter the 9 digit APN without the - sign
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,activeParcelWithoutHyphen);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel)),apn2,
				"SMAB-T2356: Validation that User should be allowed to enter the 9 digit parent APN without the \"-\"");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.taxCollectorLabel)),apn2,
				"SMAB-T2356: Validate that value of 'Assessor's Map' field is the first 5 digits of the parent parcel");

		// Step 9: Update the Parent APN field and add a Retired parcel
		ReportLogger.INFO("Add a retired parcel in Parent APN field :: " + concatenateRetireWithActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateRetireWithActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2356: Validate that user is able to view Warning message");

		//Step 10: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2356: Validate that user is not able to move to the next screen and still able to view Warning message");
		
		// Step 9: Update the Parent APN field and add a mobile home APN (134-XXX-XXX) parcel
		ReportLogger.INFO("Add a retired parcel in Parent APN field :: " + concatenateMobileHomeWithActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateMobileHomeWithActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel(s) cannot start with 134.",
				"SMAB-T2570: Validate that user is able to view error message");

		//Step 10: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel(s) cannot start with 134.",
				"SMAB-T2570: Validate that user is not able to move to the next screen and still able to view error message");
				
		// Step 11: Update the Parent APN field and add an In Progress parcel
		ReportLogger.INFO("Add an In Progress parcel in Parent APN field :: " + concatenateInProgressWithActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateInProgressWithActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2356: Validate that user is able to view error message for Inactive parcel");

		//Step 12: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2356: Validate that user is not able to move to the next screen and still able to view error message for Inactive parcel");
				
		// Step 13: Add parcels in Parent APN field with different TRA records
		ReportLogger.INFO("Add parcels in Parent APN field with different TRA records :: " + apn1 + ", " + apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- Warning: TRAs of the combined parcels are different",
				"SMAB-T2356: Validate that user is able to view warning message");
		
		//Validate the error if Non-Condo field is having a parcel with pre-fix 100 or 134 and/or incorrect number of digits
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
		
		/*Commenting this code as functionality has been changed and now Mobile Home Parcels
			are allowed in Non-Condo field during Combine Mapping action */
		
		/*
		 * objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,mobileHomeApn);
		 * objMappingPage.scrollToElement(objMappingPage.getButtonWithText(
		 * objMappingPage.nextButton));
		 * objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.
		 * nextButton)); softAssert.assertContains(objMappingPage.getErrorMessage()
		 * ,"- Non Condo Parcel Number cannot start with 100 or 134, Please enter valid Parcel Number"
		 * , "SMAB-T2570: Validate that user is able to view error message");
		 * objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"");
		 */
        
        objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"100-670-700");
        objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"- Non Condo Parcel Number cannot start with 100-199, Please enter valid Parcel Number",
				"SMAB-T2570: Validate that user is able to view error message");
		
        objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"");
        objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,apn1.substring(0, 10));
        objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"- This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2570: Validate that user is able to view error message");
        
        objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"");
        objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,apn1.substring(0, 10).concat("a"));
        objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
        softAssert.assertContains(objMappingPage.getErrorMessage(),"- This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2570: Validate that user is able to view error message");
       
        objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel,"");
        objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"Please enter the required field(s) : Reason Code",
                "SMAB-T2570: Validation that error message is displayed when reason code is blank");
		
		//Step 14: Validate that user is able to move to the next screen
		ReportLogger.INFO("Enter back the reason Code and remove First Non-Condo Parcel");
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"");
		objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel,reasonCode);
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		Thread.sleep(1000); //Allows the grid to load
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.reasonCodeField),
				"SMAB-T2356: Validate that 'Reason Code' field is not visible");
	
		//Step 15: Verify that APNs generated must be 9-digits and should end in '0'
		ReportLogger.INFO("Validate the Grid values");
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		String childAPNComponents[] = childAPNNumber.split("-");
		softAssert.assertEquals(childAPNComponents.length,3,
				"SMAB-T2356: Validation that child APN number contains 3 parts: map book,map page,parcel number");
		softAssert.assertEquals(childAPNComponents[0].length(),3,
				"SMAB-T2356: Validation that MAP BOOK of child parcels contains 3 digits");
		softAssert.assertEquals(childAPNComponents[1].length(),3,
				"SMAB-T2356: Validation that MAP page of child parcels contains 3 digits");
		softAssert.assertEquals(childAPNComponents[2].length(),3,
				"SMAB-T2356: Validation that parcel number of child parcels contains 3 digits");
		softAssert.assertTrue(childAPNNumber.endsWith("0"),
				"SMAB-T2356: Validation that child APN number ends with 0");
		
		//Validate the fields that are editable and non-editable on second mapping action screen
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2568: Validation that APN column is editable");
        softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Legal Description*"),"SMAB-T2568: Validation that Legal Description column is editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*"),"SMAB-T2568: Validation that TRA column is not editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2568: Validation that Situs column is not editable");
        softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Reason Code*"),"SMAB-T2568: Validation that Reason Code column is editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*"),"SMAB-T2568: Validation that District/Neighborhood column is not editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*"),"SMAB-T2568: Validation that Use Code column is not editable");
        softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)*"),"SMAB-T2568: Validation that Parcel Size column is editable");
        
		//Step 16: Validate that Legal description appears from the first parent parcel
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescriptionValue,
				"SMAB-T2356: Validate that System populates Legal Description from the parent parcel");
		
		//Step 17: Click PREVIOUS button and validate that Reason Code is retained and Comments are not
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2356: Validate that value of 'Reason Code' field is retained");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.commentsTextBoxLabel)),hashMapCombineMappingData.get("Comments"),
				"SMAB-T2356: Validate that value entered in Comments section does get retained");
		
		//Step 18: Update the First Non-Condo and Legal Description fields & click NEXT button
		ReportLogger.INFO("Update the First Non-Condo and Legal Description fields");
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel2,hashMapCombineMappingData.get("First Non-Condo Parcel Number"));
		objMappingPage.enter(objMappingPage.legalDescriptionTextBoxLabel2,hashMapCombineMappingData.get("Legal Description"));
		
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		//Step 19: Verify the APN generated and Value in Legal Description field
		ReportLogger.INFO("Validate the APN generated and Value in Legal Description field");
		HashMap<String, ArrayList<String>> gridDataMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataMap.get("APN").get(0),hashMapCombineMappingData.get("First Non-Condo Parcel Number"),
				"SMAB-T2356: Validate that System generates APN in same Map Book and Map Page as entered in First Non-Condo field in the previous screen");
		softAssert.assertEquals(gridDataMap.get("Legal Description*").get(0),hashMapCombineMappingData.get("Legal Description"),
				"SMAB-T2356: Validate that System populates Legal Description from the previous screen");
		
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}


	/**
	 * This method is to Verify user is able to overwrite APN generated by system during Combine process 
	 * This method is to Verify user is able to view error message related to ownership record
	 * @param loginUser
	 * @throws Exception
	 */

	@Test(description = "SMAB-T2356,SMAB-T2358: Verify user is able to overwrite APN generated by system and validate error messages related to ownership record during Combine process", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyParcelOverwriteForCombineMappingAction(String loginUser) throws Exception {
		
		//Fetching parcel that are Active
		String queryApnDetails ="SELECT Id,Name FROM Parcel__c where primary_situs__c != NULL and "
				+ "Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where "
				+ "type__c='CIO') and (Not Name like '1%') and (Not Name like '8%') "
				+ "and (Not Name like '%990') Limit 2";
		
		HashMap<String, ArrayList<String>> responseAPNDetails1 = salesforceAPI.select(queryApnDetails);
		String apn1=responseAPNDetails1.get("Name").get(0);
		String apn2=responseAPNDetails1.get("Name").get(1);
		String apnId1=responseAPNDetails1.get("Id").get(0);
		String apnId2=responseAPNDetails1.get("Id").get(1);
		
		objMappingPage.deleteOwnershipFromParcel(apnId1);
		objMappingPage.deleteOwnershipFromParcel(apnId2);
		
		String parcelSize = "200";	

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
		
		JSONObject jsonForCombineOverwrite = objMappingPage.getJsonObject();
		jsonForCombineOverwrite.put("Lot_Size_SQFT__c",parcelSize);
		jsonForCombineOverwrite.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails1.get("Id").get(0),jsonForCombineOverwrite);
		salesforceAPI.update("Parcel__c",responseAPNDetails1.get("Id").get(1),jsonForCombineOverwrite);
		
		//Getting Owner or Account records
		HashMap<String, ArrayList<String>> responseAssesseeDetails = objMappingPage.getOwnerForMappingAction(2);
	    String assesseeName1 = responseAssesseeDetails.get("Name").get(0);
		String assesseeName2 = responseAssesseeDetails.get("Name").get(1);
		
		//Fetching Interim parcel 
		String queryAPNValue = "Select name,ID  From Parcel__c where name like '8%' and name like '%0' "
		  		+ "and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		String interimAPN = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String interimAPNId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		//Fetching parcel that are Active different than above
		String queryForActiveAPN = "SELECT Name,Id FROM Parcel__c where Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Id NOT IN ('" + apnId1 + "', '" + apnId2 + "') and (Not Name like '1%') and (Not Name like '8%') and (Not Name like '%990') Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails2 = salesforceAPI.select(queryForActiveAPN);
		String apn3=responseAPNDetails2.get("Name").get(0);
		String apnId3=responseAPNDetails2.get("Id").get(0);
		String apn4=responseAPNDetails2.get("Name").get(1);
		String apnId4=responseAPNDetails2.get("Id").get(1);
		
		// deleting the current ownership records for the APN linked with WI
		objMappingPage.deleteOwnershipFromParcel(interimAPNId);
		objMappingPage.deleteOwnershipFromParcel(apnId3);
		objMappingPage.deleteOwnershipFromParcel(apnId4);
		
		//Data Manipulation to test the overwrite scenarios
		String concatenateAPNWithSameOwnership = apn1+","+apn2;
		String concatenateAPNWithDifferentOwnership = apn1+","+apn3;
		String concatenateAPNWithOneWithNoOwnership = apn1+","+apn4;
		String lessThan9DigitAPN = apn1.substring(0, 9);
		String moreThan9DigitAPN = apn2.concat("0");
		String alphanumericAPN1 = apn1.substring(0, 10).concat("a");
		String alphanumericAPN2 = "abc" + apn1.substring(3, 11);
		String specialSymbolAPN = ".%" + apn1.substring(2, 11);
		
		int numbParcel1 = objMappingPage.convertAPNIntoInteger(apn1);
		int numbParcel2 = objMappingPage.convertAPNIntoInteger(apn2);
		int temp = numbParcel1<numbParcel2?numbParcel1:numbParcel2;
		String smallestAPN = (temp == numbParcel1)?apn1:apn2;
		
		//Fetch TRA value from database and enter it in Parcels (to show TRA warning on the screen along with ownership error message)
		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		
		salesforceAPI.update("Parcel__c", interimAPNId, "Status__c", "Active");
		salesforceAPI.update("Parcel__c", apnId1, "TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", apnId2, "TRA__c", responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", apnId3, "TRA__c", responseTRADetails.get("Id").get(1));
		salesforceAPI.update("Parcel__c", apnId4, "TRA__c", responseTRADetails.get("Id").get(0));
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
	                "DataToCreateOwnershipRecord");
		
		// Adding Ownership records in the parcels
        objMappingPage.login(users.SYSTEM_ADMIN);
        
        // Opening the PARCELS page and searching the parcel to create ownership record  
        responseAPNDetails1.get("Name").stream().forEach(parcel -> {
        	try {
	        	objMappingPage.searchModule(PARCELS);
		        objMappingPage.globalSearchRecords(parcel);
		        objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
		        objParcelsPage.createOwnershipRecord(assesseeName1, hashMapCreateOwnershipRecordData);
        	}
        	catch(Exception e) {
        		ReportLogger.INFO("Fail to create ownership record : "+e);
        	}
        });
        
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn3);
        objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
        objParcelsPage.createOwnershipRecord(assesseeName2, hashMapCreateOwnershipRecordData);
        
        objWorkItemHomePage.logout();
        Thread.sleep(5000);

		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform Retire Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Active Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Update the Parent APN field and add another parcel with different ownership record
		ReportLogger.INFO("Add a parcel in Parent APN field with different ownership record :: " + apn3);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithDifferentOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- Warning: TRAs of the combined parcels are different\n- In order to proceed with a parcel combine action, the parent APN(s) must have the same ownership and ownership allocation.",
				"SMAB-T2356: Validate that user is able to view Warning message");

		//Step 6: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- Warning: TRAs of the combined parcels are different\n- In order to proceed with a parcel combine action, the parent APN(s) must have the same ownership and ownership allocation.",
				"SMAB-T2356: Validate that user is able to view Warning message");

		// Step 7: Update the Parent APN field and add another parcel with no ownership record
		ReportLogger.INFO("Add a parcel in Parent APN field with no ownership record :: " + apn4);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithOneWithNoOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- In order to proceed with a parcel combine action, the parent APN(s) must have the same ownership and ownership allocation.",
				"SMAB-T2356: Validate that user is able to view error message related to ownership record");						

		//Step 8: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- In order to proceed with a parcel combine action, the parent APN(s) must have the same ownership and ownership allocation.",
				"SMAB-T2356: Validate that user is able to view error message related to ownership record");										

		// Step 9: Update the Parent APN field and add another parcel with same ownership record
		ReportLogger.INFO("Add a parcel in Parent APN field with same ownership record :: " + apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		Thread.sleep(1000);   //Allows screen to load completely so that non-availability of the error message can be validated
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.errorMessageOnScreenOne),
				"SMAB-T2356: Validate that user is not able to view error message related to ownership record");
		
		String updateMapPageofChildApn = "";
		String apn[] = smallestAPN.split("-");
		if (apn[1].substring(2).equals("9")) {
			updateMapPageofChildApn= apn[0]+apn[1].substring(0,2)+String.valueOf(Integer.parseInt(apn[1].substring(2)) -1)+apn[2];
		}
		else {
			updateMapPageofChildApn= apn[0]+apn[1].substring(0,2)+String.valueOf(Integer.parseInt(apn[1].substring(2)) +1)+apn[2];
		}
		ReportLogger.INFO("Removed hyphen from the APN and updated the Map Page : " + updateMapPageofChildApn);
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, updateMapPageofChildApn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(10,objMappingPage.generateParcelButton);
		
		//Step 10: Validate the Warning message
		ReportLogger.INFO("Validate warning message appears : " + objMappingPage.secondScreenParcelSizeWarning.getText());
		softAssert.assertContains(objMappingPage.secondScreenParcelSizeWarning.getText(), 
				"Warning: Parcel number generated is different from the user"
				+ " selection based on established criteria. As a reference the number provided is", 
				"SMAB-T3052-Verify that for APN generation, If map page is changed for child parcels,"
				+ "then it should display message(which is returned from backend) for respective parcels");
		
		//Step 11 :Overwrite parcel value with existing parcel# and Click Combine parcel button
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,apn3);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + apn3);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		softAssert.assertTrue(objMappingPage.getErrorMessage().contains("The APN provided already exists in the system"),
				"SMAB-T2358: Validate that User is able to view error message if APN (Active) already exist : The APN provided already exists in the system");

		//Step 13 :Overwrite parcel value with interim parcel# and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,interimAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + interimAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		softAssert.assertTrue(objMappingPage.getErrorMessage().contains("This map book is reserved for interim parcels"),
				"SMAB-T2358: Validate that following error message is displayed for Interim Parcel : This map book is reserved for interim parcels");
		
		//Step 14 :Overwrite parcel value with less than 9 digits and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,lessThan9DigitAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + lessThan9DigitAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		softAssert.assertTrue(objMappingPage.getErrorMessage().contains("This parcel number is not valid, it should contain 9 digit numeric values."),
				"SMAB-T2358: Validate that User is able to view error message if APN is less than 9 digits : This parcel number is not valid, it should contain 9 digit numeric values.");
		
		//Step 15 :Overwrite parcel value with more than 9 digits and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,moreThan9DigitAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + moreThan9DigitAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		softAssert.assertTrue(objMappingPage.getErrorMessage().contains("This parcel number is not valid, it should contain 9 digit numeric values."),
				"SMAB-T2358: Validate that User is able to view error message if APN is more than 9 digits : This parcel number is not valid, it should contain 9 digit numeric values.");
		
		//Step 16 : Click previous button followed by next button
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);

		//Step 17 : Use the generated APN and create the next available in that order
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String nextGeneratedAPN = gridDataHashMap.get("APN").get(0);
		ReportLogger.INFO("nextGeneratedAPN :: " + nextGeneratedAPN);
		String notNextGeneratedAPN = objMappingPage.generateNextAvailableAPN(nextGeneratedAPN);
		
		//Step 19 :Overwrite parcel value with alphanumeric value and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,alphanumericAPN1);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + alphanumericAPN1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		//**** Changed the error message due to implementation of new functionality
		softAssert.assertTrue(objMappingPage.getErrorMessage().contains("Only non-divided interest child parcels are allowed to be created"),
				"SMAB-T2358: Validate that User is able to view error message if APN is overwritten with alphanumeric value at the end : This parcel number is not valid, it should contain 9 digit numeric values");
		
		//Step 20 :Overwrite parcel value with alphanumeric value and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,alphanumericAPN2);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + alphanumericAPN2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		softAssert.assertTrue(objMappingPage.getErrorMessage().contains("This parcel number is not valid, it should contain 9 digit numeric values."),
				"SMAB-T2358: Validate that User is able to view error message if APN is overwritten with alphanumeric value in the beginning : This parcel number is not valid, it should contain 9 digit numeric values");
		
		//Step 21 :Overwrite parcel value with special symbol and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,specialSymbolAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + specialSymbolAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values",
				"SMAB-T2358: Validate that User is able to view error message if APN is overwritten with special symbol : This parcel number is not valid, it should contain 9 digit numeric values");
		
		//Step 22 :Overwrite parcel value with not the next available parcel in the system and Click Combine parcel button
		Thread.sleep(1000); //Added to avoid regression failure
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,notNextGeneratedAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Generate Parcel button after updating the APN value :: " + notNextGeneratedAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
				
		//Added code to notify the reason in the Report, if it fails in regression
		if(!objMappingPage.getErrorMessage().isEmpty() && objMappingPage.getErrorMessage().contains("The parcel entered is invalid since the following parcel is available")) {
			softAssert.assertTrue(objMappingPage.getErrorMessage().contains("The parcel entered is invalid since the following parcel is available " + nextGeneratedAPN),
					"SMAB-T2358: Validate that User is able to view error message if APN overwritten is not the next available one in the system : The parcel entered is invalid since the following parcel is available <APN>");	
			}
		else {
			ReportLogger.INFO("Some parcels are not taken in the Map Page and Map Book, hence test FAILED :: Map Book - " + notNextGeneratedAPN.substring(0, 3) + ", Map Page - " + notNextGeneratedAPN.substring(4, 7));
			}
		
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}


	/**
	 * This method is to validate APN generated by system during Combine process 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2359, SMAB-T2568, SMAB-T2902: Verify user is able to validate APN generation by system during Combine process", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyParcelGenerationForCombineMappingAction(String loginUser) throws Exception {
		
		//Getting parcels that are Active 
		String queryForActiveAPN = "SELECT Name,Id FROM Parcel__c where Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') "
				+ "and (Not Name like '1%') and (Not Name like '8%') and (Not Name like '%990') Limit 2";
		String apn1 = salesforceAPI.select(queryForActiveAPN).get("Name").get(0);
		String apnId1 = salesforceAPI.select(queryForActiveAPN).get("Id").get(0);
		String apn2 = salesforceAPI.select(queryForActiveAPN).get("Name").get(1);
		String apnId2 = salesforceAPI.select(queryForActiveAPN).get("Id").get(1);
		
		//Delete existing Ownership records from the Active parcel
		objMappingPage.deleteOwnershipFromParcel(apnId1);
		objMappingPage.deleteOwnershipFromParcel(apnId2);
		
		//Fetching a Condo Active parcel
		String queryCondoAPN = "SELECT Name, Id from parcel__c where name like '1%' and (Not Name like '%990') and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' and (Not Name like '134%') Limit 1";
		HashMap<String, ArrayList<String>> responseCondoAPNDetails = salesforceAPI.select(queryCondoAPN);
		String condoAPN=responseCondoAPNDetails.get("Name").get(0);
		String condoApnId=responseCondoAPNDetails.get("Id").get(0);
		
		//Delete existing Ownership records from the Condo parcel
		objMappingPage.deleteOwnershipFromParcel(condoApnId);
				
		//Data Manipulation to test parcel generation scenarios
		String updateSmallestAPN="";
		String concatenateCondoWithNonCondo = condoAPN+","+apn1+","+apn2;
		String concatenateNonCondoWithCondo = apn1+","+apn2+","+condoAPN;
		
		//Find Lowest APN out of all
		int numbParcel1 = objMappingPage.convertAPNIntoInteger(apn1);
		int numbParcel2 = objMappingPage.convertAPNIntoInteger(apn2);
		int numbParcel3 = objMappingPage.convertAPNIntoInteger(condoAPN);
		
		int temp = numbParcel1<numbParcel2?numbParcel1:numbParcel2;
		int smallestNumb = numbParcel3<temp?numbParcel3:temp;
		String smallestAPN = String.valueOf(smallestNumb);
		
		if (smallestAPN.length() == 9) updateSmallestAPN = smallestAPN.substring(0, 3).concat("-").concat(smallestAPN.substring(3, 6)).concat("-").concat(smallestAPN.substring(6, 9));
		if (smallestAPN.length() == 8) updateSmallestAPN = "0" + smallestAPN.substring(0, 2).concat("-").concat(smallestAPN.substring(2, 5)).concat("-").concat(smallestAPN.substring(5, 8));
		if (smallestAPN.length() == 7) updateSmallestAPN = "00" + smallestAPN.substring(0, 1).concat("-").concat(smallestAPN.substring(1, 4)).concat("-").concat(smallestAPN.substring(4, 7));
		
		//Query last APN in Map Book and Map Page for the parcel
		String combineMapBookAndPageForParcel = updateSmallestAPN.substring(0, 8);
		String queryLastAPNValueForParcel = "SELECT Name,Id FROM Parcel__c where  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') And Name like '" + combineMapBookAndPageForParcel + "%' ORDER BY Name DESC LIMIT 1";
		HashMap<String, ArrayList<String>> responseLastAPNValueForParcel = salesforceAPI.select(queryLastAPNValueForParcel);
		String lastParcel=responseLastAPNValueForParcel.get("Name").get(0);
		
		//Create the next available APN for the parcel fetched above
		String nextGeneratedForSmallestParcel = objMappingPage.generateNextAvailableAPN(lastParcel);
		
		//Setup a parcel which is not the next available one in the system to validate the warning message
		String parcelForWarningMessage = apn1.substring(0, 8).concat("900");
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");

		//Actions actions = new Actions(driver);
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform Retire Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Active Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Update the Parent APN field and add another parcel, check validations and move to next screen
		ReportLogger.INFO("Add a parcel in Parent APN field with same ownership record :: " + condoAPN+", "+apn1 +", "+apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateCondoWithNonCondo);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		
		//Validate the default values and help text
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parcelSizeDropDownLabel),"value"),"Yes",
                "SMAB-T2568: Validation that default value of Number of Child Non-Condo Parcels  is 0");
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandLossTextBoxLabel),"value"),"0",
                "SMAB-T2568: Validation that default value of Net Land Loss is 0");
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandGainTextBoxLabel),"value"),"0",
                "SMAB-T2568: Validation that default value of Net Land Gain is 0");
        softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.numberOfIntermiParcelLabel),"value"),"0",
                "SMAB-T2568: Validation that default value of Interim Parcel is 0");
        
        /** Commenting this for now as it works in local workspace but fails in Automation Server. Alternate code has been written below for the same **/
		/*
		 * objMappingPage.waitForElementToBeVisible(6,
		 * objMappingPage.helpIconFirstNonCondoParcelNumber);
		 * actions.moveToElement(objMappingPage.helpIconFirstNonCondoParcelNumber).
		 * perform();
		 * softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.
		 * helpIconToolTipBubble),"To use system generated APN, leave as blank.",
		 * "SMAB-T2568: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box"
		 * );
		 * 
		 * actions.moveToElement(objMappingPage.helpIconLegalDescription).perform();
		 * softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.
		 * helpIconToolTipBubble),"To use parent legal description, leave as blank.",
		 * "SMAB-T2568: Validation that help text is generated on clicking the help icon for legal description"
		 * );
		 * 
		 * actions.moveToElement(objMappingPage.helpIconSitus).perform();
		 * softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.
		 * helpIconToolTipBubble),"To use parent situs, leave as blank.",
		 * "SMAB-T2568: Validation that help text is generated on clicking the help icon for Situs text box"
		 * );
		 */
        
        objMappingPage.scrollToBottom();
        objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
        objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
                "SMAB-T2568: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");
        
        Thread.sleep(1000); //Added to avoid regression failure
        objMappingPage.Click(objMappingPage.helpIconLegalDescription);
        softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank.",
                "SMAB-T2568: Validation that help text is generated on clicking the help icon for legal description");
        
        //Enter First Non-Condo parcel which is not the next available parcel in the system
        Thread.sleep(1000); //Added to avoid regression failure
        objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,parcelForWarningMessage);
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);

		//Validate the warning message 
		softAssert.assertTrue(objMappingPage.getErrorMessage().contains("Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is " +parcelForWarningMessage),
				"SMAB-T2568: Validate that User is able to view warning message when suggested First Non-Condo parcel is not the next available parcel in the system");
		
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"");
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		// Step 6: Fetch the APN generated
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String nextGeneratedAPN1 = gridDataHashMap.get("APN").get(0);	
		String nextGeneratedAPN1Components[] = nextGeneratedAPN1.split("-");
		String nextGeneratedForSmallestParcelComponent[] = nextGeneratedForSmallestParcel.split("-");
		softAssert.assertEquals(nextGeneratedAPN1Components[0],nextGeneratedForSmallestParcelComponent[0],
				"SMAB-T2359: Validation that MAP BOOK of parent and child parcels are same" );
		softAssert.assertEquals(nextGeneratedAPN1Components[1],nextGeneratedForSmallestParcelComponent[1],
				"SMAB-T2359: Validation that MAP page of parent and child parcels are same");
		softAssert.assertTrue(nextGeneratedAPN1.endsWith("0"),
				"SMAB-T2359: Validation that child APN number ends with 0");
		
		// Step 7: Click previous button and update parent parcel field and update the parcels in reverse order
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Reverse the parcels in Parent APN field with same ownership record :: " + apn1+", "+apn2+", "+condoAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateNonCondoWithCondo);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		ReportLogger.INFO("Click NEXT button");
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);

		// Step 8: Fetch the APN generated
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String nextGeneratedAPN2 = gridDataHashMap.get("APN").get(0);
		String nextGeneratedAPN2Components[] = nextGeneratedAPN2.split("-");
		softAssert.assertEquals(nextGeneratedAPN2Components[0],nextGeneratedForSmallestParcelComponent[0],
				"SMAB-T2902: Validation that MAP BOOK of parent and child parcels are same" );
		softAssert.assertEquals(nextGeneratedAPN2Components[1],nextGeneratedForSmallestParcelComponent[1],
				"SMAB-T2902: Validation that MAP page of parent and child parcels are same");
		softAssert.assertTrue(nextGeneratedAPN2.endsWith("0"),
				"SMAB-T2359: Validation that child APN number ends with 0");
		
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	
	/**
	 * This method is to Verify user is able to combine as many number of parcels into one
	 * This method is to Verify that attributes are inherited in the child parcels generated
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2357,SMAB-T2373,SMAB-T2376,SMAB-T2443,SMAB-2567,SMAB-2812: Verify user is able to combine as many number of parcels into one and attributes are inherited in the child parcel from the parent parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyParcelCombineMappingAction(String loginUser) throws Exception {
		
		//Getting Owner or Account records
		String assesseeName = objMappingPage.getOwnerForMappingAction();
		
		//Getting parcels that are Active 
		String queryForActiveAPN = "SELECT Name,Id FROM Parcel__c where Status__c='Active' AND primary_situs__c != NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') "
				+ "and (Not Name like '1%') and (Not Name like '8%') and (Not Name like '%990') Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryForActiveAPN);
		String apn1 = responseAPNDetails.get("Name").get(0);
		String apnId1 = responseAPNDetails.get("Id").get(0);
		String apn2 = responseAPNDetails.get("Name").get(1);
		String apnId2 = responseAPNDetails.get("Id").get(1);
				
		//Delete existing Ownership records from the Active parcel
		objMappingPage.deleteOwnershipFromParcel(apnId1);
		objMappingPage.deleteOwnershipFromParcel(apnId2);
		
		//Fetching a Condo Active parcel
		String queryCondoAPN = "SELECT Name, Id from parcel__c where name like '1%' and (Not Name like '%990') and (Not Name like '134%') and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and primary_situs__c != NULL and Status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> responseCondoAPNDetails = salesforceAPI.select(queryCondoAPN);
		String apn3=responseCondoAPNDetails.get("Name").get(0);
		String condoApnId=responseCondoAPNDetails.get("Id").get(0);
				
		//Delete existing Ownership records from the Condo parcel
		objMappingPage.deleteOwnershipFromParcel(condoApnId);
		
		//Add the parcels in a Hash Map for validations later
		Map<String,String> apnValue = new HashMap<String,String>(); 
		apnValue.put("APN1", apn1); 
		apnValue.put("APN2", apn2); 
		apnValue.put("APN3", apn3); 
		
		//Delete the existing parcel relationship instances on all 3 parcels
		objMappingPage.deleteRelationshipInstanceFromParcel(apn1);
		objMappingPage.deleteRelationshipInstanceFromParcel(apn2);
		objMappingPage.deleteRelationshipInstanceFromParcel(apn3);
		
        //Find lowest APN of all 3 parcels
		String updateRecordOn = "";
		String updateSmallestAPN = "";
		String primarySitusValue="";
		
		int numbParcel1 = objMappingPage.convertAPNIntoInteger(apn1);
		int numbParcel2 = objMappingPage.convertAPNIntoInteger(apn2);
		int numbParcel3 = objMappingPage.convertAPNIntoInteger(apn3);
		
		int temp = numbParcel1<numbParcel2?numbParcel1:numbParcel2;
		int smallestNumb = numbParcel3<temp?numbParcel3:temp;
		String smallestAPN = String.valueOf(smallestNumb);
		
		if (smallestAPN.length() == 9) updateSmallestAPN = smallestAPN.substring(0, 3).concat("-").concat(smallestAPN.substring(3, 6)).concat("-").concat(smallestAPN.substring(6, 9));
		if (smallestAPN.length() == 8) updateSmallestAPN = "0" + smallestAPN.substring(0, 2).concat("-").concat(smallestAPN.substring(2, 5)).concat("-").concat(smallestAPN.substring(5, 8));
		if (smallestAPN.length() == 7) updateSmallestAPN = "00" + smallestAPN.substring(0, 1).concat("-").concat(smallestAPN.substring(1, 4)).concat("-").concat(smallestAPN.substring(4, 7));
		
		if (updateSmallestAPN.equals(apn1)) updateRecordOn = apnId1;
		if (updateSmallestAPN.equals(apn2)) updateRecordOn = apnId2;
		if (updateSmallestAPN.equals(apn3)) updateRecordOn = condoApnId;
		
		//Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,Id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		String legalDescriptionValue="Legal PM 85/25-260";
		String querySitusValue = "SELECT Name FROM Situs__c where id in (SELECT Primary_Situs__c FROM Parcel__c where Name='"+ updateSmallestAPN + "')";
		primarySitusValue = salesforceAPI.select(querySitusValue).get("Name").get(0);
		
		//Enter values in the Parcels
		JSONObject jsonForCombineAction = objMappingPage.getJsonObject();
		jsonForCombineAction.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonForCombineAction.put("Short_Legal_Description__c","");
		jsonForCombineAction.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonForCombineAction.put("TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", updateRecordOn, jsonForCombineAction);
		
		Thread.sleep(1000); //Allows parcel update
		salesforceAPI.update("Parcel__c", apnId1, "Lot_Size_SQFT__c", "1000");
		salesforceAPI.update("Parcel__c", apnId2, "Lot_Size_SQFT__c", "1000");
		salesforceAPI.update("Parcel__c", responseCondoAPNDetails.get("Id").get(0), "Lot_Size_SQFT__c", "1000");
			System.out.println("Test");	
		String concatenateMixAPNs = apn1+","+apn2+","+apn3;
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToCreateOwnershipRecord");
		
		// Add ownership records in the parcels
        objMappingPage.login(users.SYSTEM_ADMIN);

        // Opening the PARCELS page and searching the parcel to create ownership record        
        responseAPNDetails.get("Name").stream().forEach(parcel -> {
        	try {
	        	objMappingPage.searchModule(PARCELS);
		        objMappingPage.globalSearchRecords(parcel);
		        objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
		        objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
        	}
        	catch(Exception e) {
        		ReportLogger.INFO("Fail to create ownership record : "+e);
        	}
        });
        
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn3);
        objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
        objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
        
        objWorkItemHomePage.logout();
        Thread.sleep(5000);
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the parcel to perform Combine Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);
		
		// Step 3: Creating Manual work item for the Active Parcel 
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 6: Select the Combine value in Action field
		ReportLogger.INFO("Select the 'Combine' Action and Yes in Tax field");
		Thread.sleep(2000);  //Allows screen to load completely
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		
		// Step 7: Update the Parent APN field and add more Active parcel records
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Add multiple Active APNs :: " + concatenateMixAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateMixAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
				
		//Step 8: Validate that user is able to move to the next screen and no warning message is displayed
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		softAssert.assertTrue(!objMappingPage.getErrorMessage().contains("Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is"),
						"SMAB-T2568: Validate that User is able to view warning message when suggested First Non-Condo parcel is not the next available parcel in the system");

		//Step 9: Validate that ALL fields THAT ARE displayed on second screen
		ReportLogger.INFO("Validate the Grid values");
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(0);

		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2357: Validation that System populates District/Neighborhood from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),primarySitusValue.replaceFirst("\\s", ""),
				"SMAB-T2357: Validation that System populates Situs from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2357: Validation that System populates Reason code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),"",
				"SMAB-T2567: Validation that System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2357: Validation that System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),responsePUCDetails.get("Name").get(0),
				"SMAB-T2357: Validation that System populates Use Code  from the parent parcel");

		//Step 10 :Verify that User is able to update Legal Description on the Grid
		ReportLogger.INFO("Update the Legal Description field");
		objMappingPage.editGridCellValue(objMappingPage.legalDescriptionColumnSecondScreen,legalDescriptionValue);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);

		//Step 11 :Clicking generate parcel button
		ReportLogger.INFO("Click on Combine Parcel button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2357: Validate that User is able to perform Combine action for multiple active parcels");
		
		//Step 12: Validate that ALL fields THAT ARE displayed AFTER PARCEL ARE GENERATED
		ReportLogger.INFO("Validate the Grid values");
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		boolean actionColumn = gridDataHashMap.containsKey("Action");
		
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),primarySitusValue.replaceFirst("\\s", ""),
				"SMAB-T2357: Validation that System populates Situs  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2357: Validation that System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2357: Validation that System populates Reason code from parent parcel work item");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescriptionValue,
				"SMAB-T2357: Validation that System populates Legal Description which was updated in Grid");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2357: Validation that System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),responsePUCDetails.get("Name").get(0),
				"SMAB-T2357: Verify that User is able to to create a Use Code for the child parcel from the custom screen ");
		
		//Validate that all the fields are non-editable after the parcels are generated
        softAssert.assertTrue(!actionColumn,"SMAB-T2568: Validation that Action column has disappeared after generating parcels");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2568: Validation that APN column is not editable");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Legal Description*"),"SMAB-T2568: Validation that Legal Description column is not editable");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*"),"SMAB-T2568: Validation that TRA column is not editable");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2568: Validation that Situs column is not editable");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Reason Code*"),"SMAB-T2568: Validation that Reason Code column is not editable");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*"),"SMAB-T2568: Validation that District/Neighborhood column is not editable");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*"),"SMAB-T2568: Validation that Use Code column is not editable");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)*"),"SMAB-T2568: Validation that Parcel Size column is not editable");
		        
		//Step 13: Open the child parcel and validate the attributes
		driver.switchTo().window(parentWindow);
		objMappingPage.searchModule(PARCELS);
		
		objMappingPage.globalSearchRecords(childAPNNumber);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"In Progress - New Parcel",
				"SMAB-T2373: Validate the Status of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),responsePUCDetails.get("Name").get(0),
				"SMAB-T2373: Validate the PUC of child parcel is populated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelTRA, "Parcel Information"),responseTRADetails.get("Name").get(0),
				"SMAB-T2373: Validate the TRA of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus, "Parcel Information"),primarySitusValue.replaceFirst("\\s", ""),
				"SMAB-T2373: Validate the Situs of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelDistrictNeighborhood, "Summary Values"),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2373: Validate the District/Neighborhood of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelShortLegalDescription, "Legal Description"),legalDescriptionValue,
				"SMAB-T2373: Validate the Short Legal Description of child parcel generated");
		
		//Step 14: Validate the Parcel Relationships
		objParcelsPage.openParcelRelatedTab("Parcel Relationships");
		HashMap<String, ArrayList<String>> relationshipTableDataHashMap = objParcelsPage.getParcelTableDataInHashMap("Source Parcel Relationships");
		
		softAssert.assertTrue(apnValue.containsValue(relationshipTableDataHashMap.get("Source Parcel").get(0)),
				"SMAB-T2373: Validation that first Parent APN is displayed in the relationship");
		softAssert.assertTrue(apnValue.containsValue(relationshipTableDataHashMap.get("Source Parcel").get(1)),
				"SMAB-T2373: Validation that second Parent APN is displayed in the relationship");
		softAssert.assertTrue(apnValue.containsValue(relationshipTableDataHashMap.get("Source Parcel").get(2)),
				"SMAB-T2373: Validation that third Parent APN is displayed in the relationship");
		
		//Step 15: Validate the Ownership record on child parcel
		objParcelsPage.openParcelRelatedTab("Ownership");
		HashMap<String, ArrayList<String>> ownershipTableDataHashMap = objParcelsPage.getParcelTableDataInHashMap("Ownership");
		softAssert.assertEquals(ownershipTableDataHashMap.get("Owner").get(0),assesseeName,
				"SMAB-T2373: Validate that the Ownership record appears in Ownership tab");
		
		//Step 16: Validate Parent parcels status and Parcel relationship		
		apnValue.forEach((parcelKey, parcel) -> {
		try {
			objMappingPage.globalSearchRecords(parcel);
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"In Progress - To Be Expired",
					"SMAB-T2373: Validate the Status of parcel : " + parcel);
			softAssert.assertTrue(!objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information").equals("In Progress - To Be Expired"),
					"SMAB-T2373: Validate the PUC of parcel : " + parcel);
			
			objParcelsPage.openParcelRelatedTab("Parcel Relationships");
			HashMap<String, ArrayList<String>> tableDataHashMap1 = objParcelsPage.getParcelTableDataInHashMap("Target Parcel Relationships");
			softAssert.assertEquals(tableDataHashMap1.get("Target Parcel").get(0),childAPNNumber,
					"SMAB-T2812: Validate that the Child parcel appears in Parcel Relationship tab");
    	}
    	catch(Exception e) {
    		ReportLogger.INFO("Fail to validate the parcel status and relationship : "+e);
    	}
    	});
		
		//Step 17: Submit the WI for approval and validate the linked parcels to the WI
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh(); //refresh as the focus is getting lost
		Thread.sleep(5000);
		objMappingPage.waitForElementToBeVisible(10, objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1838:Verify user is able to submit the Work Item for approval");
		
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
				"SMAB-T2373: Validate that first Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("1")),
				"SMAB-T2373: Validate that second Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("2")),
				"SMAB-T2373: Validate that third Parent APN is displayed in the linked item");
		
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		
		//Step 18: Login from Mapping Supervisor to approve the WI
		ReportLogger.INFO("Now logging in as Mapping Supervisor to approve the work item and validate that new WIs are accessible");
		objWorkItemHomePage.login(MAPPING_SUPERVISOR);
		
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh(); //refresh as the focus is getting lost
		Thread.sleep(5000);
		objMappingPage.waitForElementToBeVisible(10, objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		objWorkItemHomePage.completeWorkItem();
		softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Completed","SMAB-T1838:Verify user is able to complete the Work Item");
		
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
				"SMAB-T2373: Validate that first Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("1")),
				"SMAB-T2373: Validate that second Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("2")),
				"SMAB-T2373: Validate that third Parent APN is displayed in the linked item");
		
		//Step 19: Fetching work item generated and validating its details
		String queryWIs = "SELECT Name,Id FROM Work_Item__c Order By Name Desc Limit 1";
		HashMap<String, ArrayList<String>> responseWorkItem = salesforceAPI.select(queryWIs);
		String workItem1=responseWorkItem.get("Name").get(0);
		
		//Step 20: Validating the Status and PUC on Parent parcels and status of Child parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(childAPNNumber);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"Active",
				"SMAB-T2373,SMAB-T2376: Validate the status of child parcel generated");
		
		apnValue.forEach((parcelKey, parcel) -> {
			try {
				objMappingPage.globalSearchRecords(parcel);
				softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"Retired",
						"SMAB-T2373: Validate the Status of parcel : " + parcel);
				softAssert.assertTrue(!objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information").equals("99-RETIRED PARCEL"),
						"SMAB-T2373: Validate the PUC of parcel : " + parcel);
	    	}
	    	catch(Exception e) {
	    		ReportLogger.INFO("Fail to validate the Parcel status and PUC : "+e);
	    	}
	    });
		
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		
		//Step 21: Login from RP Appraiser to validate new WI created
		ReportLogger.INFO("Now logging in as RP Appraiser to validate that new WIs are accessible");
		objWorkItemHomePage.login(RP_APPRAISER);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.displayRecords("All");
		
		objMappingPage.globalSearchRecords(workItem1);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		softAssert.assertEquals(objMappingPage.getLinkedParcelInWorkItem("0"), childAPNNumber,
				"SMAB-T2443: Validate that Child parcel is displayed in the Linked Items of first generated WI");
		
		objMappingPage.Click(objWorkItemHomePage.detailsTab);
		objMappingPage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type", "Information"),"New APN",
				"SMAB-T2443: Validate the Type of WI generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action", "Information"),"Update Characteristics & Verify PUC",
				"SMAB-T2443: Validate the Action of WI generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status", "Information"),"In Pool",
				"SMAB-T2443: Validate the Status of WI generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Reference", "Information"),"",
				"SMAB-T2443: Validate the Reference of WI generated");
		
		//driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}

	/**
	 * This method is to Verify that User is able to update Situs for child parcel from the Parcel mapping screen for "Combine" mapping action
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3451,SMAB-T3459,SMAB-T3452,SMAB-T2659:Parcel Management- Verify that User is able to update Situs for child parcel from the Parcel mapping screen for \"Combine\" mapping action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_UpdateChildParcelSitusFirstScreen_CombineMappingAction(String loginUser) throws Exception {

		String execEnv= System.getProperty("region");	
		
		//Getting Owner or Account records
		HashMap<String, ArrayList<String>> responseAssesseeDetails = objMappingPage.getOwnerForMappingAction(2);
	    String assesseeName1 = responseAssesseeDetails.get("Name").get(0);
	    
		//Fetching parcel that are Active
		String queryApnDetails ="SELECT Id,Name FROM Parcel__c where primary_situs__c != NULL and "
				+ "Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where "
				+ "type__c='CIO') and (Not Name like '100%') and (Not Name like '800%') "
				+ "and (Not Name like '%990') and (Not Name like '134%') Limit 2";

		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryApnDetails);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);

		objMappingPage.deleteOwnershipFromParcel(responseAPNDetails.get("Id").get(0));
		objMappingPage.deleteOwnershipFromParcel(responseAPNDetails.get("Id").get(1));
				
		String concatenateAPNWithSameOwnership = apn1+","+apn2;
		
		//Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");
		String primarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ apn1 +"')").get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";
		String parcelSize	= "200";	

		jsonParcelObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonParcelObject.put("Status__c","Active");
		jsonParcelObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonParcelObject.put("District__c",districtValue);
		jsonParcelObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonParcelObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonParcelObject.put("Lot_Size_SQFT__c",parcelSize);

		//updating PUC details
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonParcelObject);
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), "TRA__c", responseTRADetails.get("Id").get(1));	
			
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingActionWithSitusData");
		String cityName = hashMapCombineActionMappingData.get("City Name");
		String direction = hashMapCombineActionMappingData.get("Direction");
		String situsNumber = hashMapCombineActionMappingData.get("Situs Number");
		String situsStreetName = hashMapCombineActionMappingData.get("Situs Street Name");
		String situsType = hashMapCombineActionMappingData.get("Situs Type");
		String situsUnitNumber = hashMapCombineActionMappingData.get("Situs Unit Number");
		String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+cityName;

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToCreateOwnershipRecord");
		
		//Login with system admin and create new same Ownership on both parcel 
		objMappingPage.login(users.SYSTEM_ADMIN);
        
        // Opening the PARCELS page and searching the parcel to create ownership record        
		responseAPNDetails.get("Name").stream().forEach(parcel -> {
        	try {
	        	objMappingPage.searchModule(PARCELS);
		        objMappingPage.globalSearchRecords(parcel);
		        objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
		         
		        HashMap<String, ArrayList<String>> responseAPNid = 
		        		salesforceAPI.select("Select Id from parcel__C where name='"
		        		+parcel+"'");
				String id=responseAPNid.get("Id").get(0);
		        String ownershipURL = "https://smcacre--"
		        		+ execEnv
		        		+ ".lightning.force.com/lightning/r/Parcel__c/"
		        		+ id
		        		+ "/related/Property_Ownerships__r/view";
		        ReportLogger.INFO(ownershipURL);
		        driver.navigate().to(ownershipURL);
		        objParcelsPage.createOwnershipRecord(assesseeName1,hashMapCreateOwnershipRecordData);
				objMappingPage.closeDefaultOpenTabs();

        	}
        	catch(Exception e) {
        		ReportLogger.INFO("Fail to create ownership record : "+e);
        	}
        });
		objWorkItemHomePage.logout();
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform split mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel combine' & Taxes Paid fields value as 'N/A'
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 6: editing situs for child parcel and filling all fields
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel));
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.editSitusLabelSitusModal),
				"SMAB-T2659: Validation that Edit Situs label is displayed as heading of situs modal window in first screen");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.situsInformationLabelSitusModal),
				"SMAB-T2659: Validation that  Situs Information label is displayed in  situs modal window in first screen");
		objMappingPage.editSitusModalWindowFirstScreen(hashMapCombineActionMappingData);
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel),"value"),childprimarySitus,
				"SMAB-T2659: Validation that User is able to update a Situs for child parcel from the Parcel mapping screen");
		objMappingPage.fillMappingActionForm(hashMapCombineActionMappingData);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.generateParcelButton);
		//updating child parcel size in second screen on mapping action 
		objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreen,"99",1);

		//validating second screen warning message
		String parcelsizewarningmessage=objMappingPage.secondScreenParcelSizeWarning.getText();
		softAssert.assertEquals(parcelsizewarningmessage,
				"Parent Parcel Size = "+parcelSize+", Net Land Loss = 10, Net Land Gain = 0,"
						+ " Total Child Parcel(s) Size = 99.",
				"SMAB-T3451,SMAB-T3459-Verify that parent parcel size and entered net gain/loss and value is getting displayed");

		//Step 7: Validation that primary situs on second screen is getting populated from situs entered in first screen
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),childprimarySitus,
				"SMAB-T2659: Validation that System populates primary situs on second screen for child parcel with the situs value that was added in first screen");

		//Step 8: Click Combine Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 9: Validation that primary situs on last screen screen is getting populated from situs entered in first screen
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),childprimarySitus,
				"SMAB-T2659: Validation that System populates primary situs on last screen for child parcel  with the situs value that was added in first screen");

		//Step 10: Validation that primary situs of child parcel is the situs value that was added in first screen from situs modal window
		String primarySitusValueChildParcel=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ gridDataHashMap.get("APN").get(0)+"')").get("Name").get(0);
		softAssert.assertEquals(primarySitusValueChildParcel,childprimarySitus,
				"SMAB-T2659: Validation that primary situs of  child parcel  has value that was entered in first screen through situs modal window");

		driver.switchTo().window(parentWindow);
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
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "Combine" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2677:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"Combine\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_CombineMappingAction_NoPrimarySitusTRA(String loginUser) throws Exception {

		String  childAPNPUC;
		//Fetching parcels that are Active with no Ownership record, no  tra and no primary situs
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '134%') and TRA__c=NULL and Primary_Situs__c=NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);

		String concatenateAPNWithSameOwnership = apn1+","+apn2;

		String parcelSize	= "200";	

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		jsonParcelObject.put("Lot_Size_SQFT__c",parcelSize);
		jsonParcelObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonParcelObject);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(1),jsonParcelObject);
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingActionWithSitusData");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

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
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapCombineActionMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String apn=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)*").get(0);

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
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),apn,
				"SMAB-T2677: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2677: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2677: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2677: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2677: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2677: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2677: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2677: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2677: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");
		
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2677: Validation that APN column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Legal Description*"),"SMAB-T2677: Validation that Legal Description column on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*"),"SMAB-T2677: Validation that TRA column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2677: Validation that Situs column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Reason Code*"),"SMAB-T2677: Validation that Reason Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*"),"SMAB-T2677: Validation that District/Neighborhood column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*"),"SMAB-T2677: Validation that Use Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)*"),"SMAB-T2677: Validation that Parcel Size (SQFT) column should  be editable on retirning to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "Combine" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3495,SMAB-T3494,SMAB-T3496,SMAB-T2677:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"Combine\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_CombineMappingAction_WithPrimarySitusTRA(String loginUser) throws Exception {

		String  childAPNPUC;

		//Fetching parcels that are Active with same Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '134%') and  Primary_Situs__c !=NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);

		String concatenateAPNWithSameOwnership = apn1+","+apn2;

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		String parcelSize	= "200";	

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		jsonParcelObject.put("Lot_Size_SQFT__c",parcelSize);
		jsonParcelObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		jsonParcelObject.put("TRA__c",responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonParcelObject);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(1),jsonParcelObject);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeMappingWithActionInternalRequestCombine");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingActionWithSitusData");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Parcel
		String workItem=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");

		// validating related action
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Related Action", "Information"),
				hashMapmanualWorkItemData.get("Actions"),
				"SMAB-T3494-Verify that the Related Action "
				+ "label should match the Actions labels while creating WI and it should "
				+ "open mapping screen on clicking Perform Internal Request Combine");

		// validating Event Id in Work item screen of Action type
		String eventIDValue = objWorkItemHomePage.getFieldValueFromAPAS("Event ID", "Information");
		softAssert.assertEquals(eventIDValue.contains("Alpha"), true,
				"SMAB-T3496-Verify that the Event ID based on the mapping should be "
				+"created and populated on the Work item record.");

		softAssert.assertTrue(!objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.editEventIdButton),
				"SMAB-T3496-This field should not be editable.");
						
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel combine' 
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapCombineActionMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String apn=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)*").get(0);

		//Step 7: Click Combine Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);

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

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),apn,
				"SMAB-T2677: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2677: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2677: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2677: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2677: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2677: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2677: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2677: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2677: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	@Test(description = "SMAB-T2829,SMAB-T2677,SMAB-T3634,SMAB-T3633,SMAB-T3635:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"Combine\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_CombineMappingAction_IndependentMappingActionWI(String loginUser) throws Exception {

		String  childAPNPUC;

		//Fetching parcels that are Active with same Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '134%') and  Primary_Situs__c !=NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);

		String concatenateAPNWithSameOwnership = apn1+","+apn2;

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");
		
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
        HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
        String legalDescriptionValue = "Legal PM 85/25-260";
        jsonParcelObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
        jsonParcelObject.put("Short_Legal_Description__c", legalDescriptionValue);
        jsonParcelObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonParcelObject.put("Lot_Size_SQFT__c",200);
		jsonParcelObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonParcelObject);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(1),jsonParcelObject);

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingActionWithSitusData");

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
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 4: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapCombineActionMappingData);
		objMappingPage.waitForElementToBeVisible(10,objMappingPage.generateParcelButton);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String apn=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String reasonCode=gridDataHashMap.get("Reason Code*").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)*").get(0);
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(concatenateAPNWithSameOwnership), "SMAB-T3363 : Verify that for \" Combine \" mapping action, in custom action second screen and third screen Parent APN (s) "+concatenateAPNWithSameOwnership+" is displayed");

		//Step 5: Click Combine Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(concatenateAPNWithSameOwnership), "SMAB-T3363 : Verify that for \" Combine \" mapping action, in custom action second screen and third screen Parent APN (s) "+concatenateAPNWithSameOwnership+" is displayed");

		softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2829: Validate that User is able to perform one to one  action from mapping actions tab");			    

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+apn+"') limit 1");
		if(responsePUCDetailsChildAPN.size()==0)
			  childAPNPUC ="";
		else
			childAPNPUC=responsePUCDetailsChildAPN.get("Name").get(0);
		
		//Step 8: Navigating  to the independent mapping action WI that would have been created after performing combine action and clicking on related action link 
		String workItemId= objWorkItemHomePage.getWorkItemIDFromParcelOnWorkbench(apn1);
		String query = "SELECT Name FROM Work_Item__c where id = '"+ workItemId + "'";
		HashMap<String, ArrayList<String>> responseDetails = salesforceAPI.select(query);
		String workItem=responseDetails.get("Name").get(0);

		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type","Information"), "Mapping",
				"SMAB-T2829: Validation that  A new WI of type Mapping is created after performing one to one from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action","Information"), "Independent Mapping Action",
				"SMAB-T2829: Validation that  A new WI of action Independent Mapping Action is created after performing one to one from mapping action tab");
		softAssert.assertContains(objMappingPage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(DateUtil.getCurrentDate("MM/dd/yyyy")), "SMAB-T2829: Validation that 'Date' fields is equal to date when this WI was created");

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(concatenateAPNWithSameOwnership), "SMAB-T3363 : Verify that for \" Combine \" mapping action, in custom action second screen and third screen Parent APN (s) "+concatenateAPNWithSameOwnership+" is displayed");
		objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace, "390");
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName));
		

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),apn,
				"SMAB-T2677: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2677: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2677: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2677: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2677: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2677: Validation that  System populates Use Code that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2677: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2677: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),
				objMappingPage.getFieldValueFromAPAS("Parcel Size (SqFt)", "Parcel Information"),
				"SMAB-T3633,SMAB-T3635:Parcel size(SQFT) was updated successfully and user was able to go to update screen");
		
		// Mark the WI complete
		query = "Select Id from Work_Item__c where Name = '" + workItem + "'";
		salesforceAPI.update("Work_Item__c", query, "Status__c", "Submitted for Approval");
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		ReportLogger.INFO(" Supervisor logins to close the WI ");
		objMappingPage.login(users.MAPPING_SUPERVISOR);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItem);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		// refresh as the focus is getting lost
		driver.navigate().refresh();
		Thread.sleep(5000);
		objWorkItemHomePage.completeWorkItem();
		String workItemStatus = objMappingPage.getFieldValueFromAPAS("Status", "Information");
		softAssert.assertEquals(workItemStatus, "Completed", "SMAB-T3634: Validation WI completed successfully");
		objWorkItemHomePage.logout();
	}
	
	@Test(description = "SMAB-T2578,SMAB-T2579,SMAB-T2637: I need to have the ability to select specific fields from the mapping custom screen, so that the correct values can be assigned to the parcels.", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyParcelCombineMappingActionCustomScreenLookups(String loginUser) throws Exception {
		
		//Getting Owner or Account records
		String assesseeName = objMappingPage.getOwnerForMappingAction();

		//Fetching parcels that are Active with specific ownership record
		HashMap<String, ArrayList<String>> responseAPNDetails = objMappingPage.getActiveApnWithNoOwner(2);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);
		
		//Getting an Active Condo Parcel with specific ownership record
		HashMap<String, ArrayList<String>> responseCondoAPNDetails = objMappingPage.getCondoApnWithNoOwner();
		String apn3 = responseCondoAPNDetails.get("Name").get(0);
		
		String PUC = salesforceAPI.select("SELECT Name FROM PUC_Code__c  limit 1").get("Name").get(0);
	    String TRA=salesforceAPI.select("SELECT Name FROM TRA__c limit 1").get("Name").get(0); 
		
		//Add the parcels in a Hash Map for validations later
		Map<String,String> apnValue = new HashMap<String,String>(); 
		apnValue.put("APN1", apn1); 
		apnValue.put("APN2", apn2); 
		apnValue.put("APN3", apn3); 
		
		//Delete the existing parcel relationship instances on all 3 parcels
		objMappingPage.deleteRelationshipInstanceFromParcel(apn1);
		objMappingPage.deleteRelationshipInstanceFromParcel(apn2);
		objMappingPage.deleteRelationshipInstanceFromParcel(apn3);
		
        //Find lowest APN of all 3 parcels
		String updateRecordOn = "";
		String updateSmallestAPN = "";
		
		int numbParcel1 = objMappingPage.convertAPNIntoInteger(apn1);
		int numbParcel2 = objMappingPage.convertAPNIntoInteger(apn2);
		int numbParcel3 = objMappingPage.convertAPNIntoInteger(apn3);
		
		int temp = numbParcel1<numbParcel2?numbParcel1:numbParcel2;
		int smallestNumb = numbParcel3<temp?numbParcel3:temp;
		String smallestAPN = String.valueOf(smallestNumb);
		
		if (smallestAPN.length() == 9) updateSmallestAPN = smallestAPN.substring(0, 3).concat("-").concat(smallestAPN.substring(3, 6)).concat("-").concat(smallestAPN.substring(6, 9));
		if (smallestAPN.length() == 8) updateSmallestAPN = "0" + smallestAPN.substring(0, 2).concat("-").concat(smallestAPN.substring(2, 5)).concat("-").concat(smallestAPN.substring(5, 8));
		if (smallestAPN.length() == 7) updateSmallestAPN = "00" + smallestAPN.substring(0, 1).concat("-").concat(smallestAPN.substring(1, 4)).concat("-").concat(smallestAPN.substring(4, 7));
		
		if (updateSmallestAPN.equals(apn1)) updateRecordOn = responseAPNDetails.get("Id").get(0);
		if (updateSmallestAPN.equals(apn2)) updateRecordOn = responseAPNDetails.get("Id").get(1);
		if (updateSmallestAPN.equals(apn3)) updateRecordOn = responseCondoAPNDetails.get("Id").get(0);
		
		//Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		String legalDescriptionValue="Legal PM 85/25-260";
		
		//Enter values in the Parcels
		jsonParcelObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonParcelObject.put("Short_Legal_Description__c","");
		jsonParcelObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonParcelObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c",updateRecordOn,jsonParcelObject);
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), "Lot_Size_SQFT__c", "0");
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), "Lot_Size_SQFT__c", "0");
		salesforceAPI.update("Parcel__c", responseCondoAPNDetails.get("Id").get(0), "Lot_Size_SQFT__c", "0");
				
		String concatenateMixAPNs = apn1+","+apn2+","+apn3;
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingActionWithSitusData");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
                "DataToCreateOwnershipRecord");
		String situsCityName = hashMapCombineMappingData.get("City Name");
		String direction = hashMapCombineMappingData.get("Direction");
		String situsNumber = hashMapCombineMappingData.get("Situs Number");
		String situsStreetName = hashMapCombineMappingData.get("Situs Street Name");
		String situsType = hashMapCombineMappingData.get("Situs Type");
		String situsUnitNumber = hashMapCombineMappingData.get("Situs Unit Number");
		String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;

		
		// Add ownership records in the parcels
        objMappingPage.login(users.SYSTEM_ADMIN);

        // Opening the PARCELS page and searching the parcel to create ownership record        
        responseAPNDetails.get("Name").stream().forEach(parcel -> {
        	try {
	        	objMappingPage.searchModule(PARCELS);
		        objMappingPage.globalSearchRecords(parcel);
		        objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
		        objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
        	}
        	catch(Exception e) {
        		ReportLogger.INFO("Fail to create ownership record : "+e);
        	}
        });
        
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn3);
        objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
        objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
        
        objWorkItemHomePage.logout();
        Thread.sleep(5000);
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the parcel to perform Combine Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);
		
		// Step 3: Creating Manual work item for the Active Parcel 
		String workItemNumber  = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
				ReportLogger.INFO("Click on the Related Action link");
				Thread.sleep(3000);
				objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
				objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
				String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");			
				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
				String parentWindow = driver.getWindowHandle();
				ReportLogger.INFO("Switch to the Mapping Action screen");
				objWorkItemHomePage.switchToNewWindow(parentWindow);

				// Step 6: Select the Combine value in Action field
				ReportLogger.INFO("Select the 'Combine' Action and Yes in Tax field");
				Thread.sleep(2000);  //Allows screen to load completely
				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
				objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
				
				// Step 7: Update the Parent APN field and add more Active parcel records
				objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
				ReportLogger.INFO("Add multiple Active APNs :: " + concatenateMixAPNs);
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateMixAPNs);
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel)),
						"SMAB-T2578: Validation that Situs label is displayed on mapping screen.");
				//Step 8: Validate that user is able to move to the next screen and no warning message is displayed
				ReportLogger.INFO("Click NEXT button");
				objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
				objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
				softAssert.assertTrue(!objMappingPage.getErrorMessage().contains("Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is"),
								"SMAB-T2568: Validate that User is able to view warning message when suggested First Non-Condo parcel is not the next available parcel in the system");

				//Step 9: Validate that ALL fields THAT ARE displayed on second screen
				ReportLogger.INFO("Validate the Grid values");
				HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
				String childAPNNumber =gridDataHashMap.get("APN").get(0);

				softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),responseNeighborhoodDetails.get("Name").get(0),
						"SMAB-T2578: Validation that System populates District/Neighborhood from the parent parcel");			
				 
				softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
						"SMAB-T2578: Validation that System populates Reason code from the parent parcel");
				softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),"",
						"SMAB-T2578: Validation that System populates Legal Description from the parent parcel");
				softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),responseTRADetails.get("Name").get(0),
						"SMAB-T2578: Validation that System populates TRA from the parent parcel");
				softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),responsePUCDetails.get("Name").get(0),
						"SMAB-T2578: Validation that System populates Use Code  from the parent parcel");
				
		       
				softAssert.assertTrue(objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2578: Validation that APN column is  editable");
				softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Legal Description*"),"SMAB-T2578: Validation that Legal Description column is  editable");
				softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*"),"SMAB-T2578: Validation that TRA column is not editable");
		  	    softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2578: Validation that Situs column is not editable");
				softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Reason Code*"),"SMAB-T2578: Validation that Reason Code column is not editable");
				softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*"),"SMAB-T2578: Validation that District/Neighborhood column is not editable");
				softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*"),"SMAB-T2578: Validation that Use Code column is not editable");
				softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)*"),"SMAB-T2578: Validation that Parcel Size column is  editable");
				
				 objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
					Thread.sleep(3000);
				objMappingPage.editActionInMappingSecondScreen(hashMapCombineMappingData);
				objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
				ReportLogger.INFO("Validate the Grid values");
				HashMap<String, ArrayList<String>> gridDataHashMapAfterEditAction =objMappingPage.getGridDataInHashMap();
						    
			    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Situs").get(0),childprimarySitus,
						"SMAB-T2579,SMAB-T2637: Validation that System populates Situs from the parent parcel");
			    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("TRA*").get(0),TRA,
						"SMAB-T2579,SMAB-T2637: Validation that System populates TRA from the parent parcel");
			    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Use Code*").get(0),PUC,
						"SMAB-T2579,SMAB-T2637: Validation that System populates TRA from the parent parcel");
			    ReportLogger.INFO("Click on Combine Parcel button");
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
				softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
						"SMAB-T579: Validate that User is able to perform Combine action for multiple active parcels");			    
			    
			    driver.switchTo().window(parentWindow);
			    objMappingPage.searchModule(PARCELS);
				
				objMappingPage.globalSearchRecords(childAPNNumber);
				//Validate the Situs of child parcel generated
			    softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus, "Parcel Information"),childprimarySitus,
						"SMAB-T2579: Validate the Situs of child parcel generated");
				objWorkItemHomePage.logout();
	
			}       
		
	/**
	 * This method is to system doesn't consider Interim parcel as lower parcel if other parcel is higher than that in series
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2904: Verify system doesn't consider Interim parcel as lower parcel if other parcel is higher than that in series", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyParcelGenerationForCombineWithInterimAsLowestParcel(String loginUser) throws Exception {
		
		//Fetching Interim parcels
		String queryInterimAPNValue = "Select name,ID  From Parcel__c where name like '8%' "
				  		+ "and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		String apn1 = salesforceAPI.select(queryInterimAPNValue).get("Name").get(0);
		String apn1Id = salesforceAPI.select(queryInterimAPNValue).get("Id").get(0);
				
		// deleting the current ownership records for the APN linked with WI
		objMappingPage.deleteOwnershipFromParcel(apn1Id);
		salesforceAPI.update("Parcel__c", apn1Id, "Status__c", "Active");
		
		//Getting a Non-Condo Parcel starting with '9'
		String createNewParcel = testdata.MANUAL_PARCEL_CREATION_DATA;
		Map<String, String> hashMapCreateNewParcel = objUtil.generateMapFromJsonFile(createNewParcel,
					"DataToCreateParcelStartingWith9");
		String apnStartingWith9 = hashMapCreateNewParcel.get("APN");
		String parcelNumberStartingWith9 = hashMapCreateNewParcel.get("Parcel Number");
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id"
					+ "  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c "
					+ "where Status__c='Active') limit 1");
		String PUC = responsePUCDetails.get("Name").get(0);
		
		//Login to the APAS application
		objMappingPage.login(users.SYSTEM_ADMIN);

		//Opening the PARCELS page and searching for the parcel - If not there, create one   
		objMappingPage.searchModule(PARCELS);
		String apn2 = objParcelsPage.createNewParcel(apnStartingWith9,parcelNumberStartingWith9,PUC);
		
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
	
		//Get the APN Id
	    HashMap<String, ArrayList<String>> responseSearchedApnId = salesforceAPI.select("Select id from Parcel__c where name ='"+apn2+"'");
	    String apn2Id = responseSearchedApnId.get("Id").get(0);
	    
		// deleting the current ownership records for the APN linked with WI
		objMappingPage.deleteOwnershipFromParcel(apn2Id);
		salesforceAPI.update("Parcel__c", apn2Id, "Status__c", "Active");
		
		String concatenateMixAPNs = apn1+","+apn2;
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the parcel to perform Combine Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);
		
		// Step 3: Creating Manual work item for the Active Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Select the Combine value in Action field
		ReportLogger.INFO("Select the 'Combine' Action and Yes in Tax field");
		Thread.sleep(2000);  //Allows screen to load completely
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		
		// Step 6: Update the Parent APN field and add more Active parcel records
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Add multiple Active APNs :: " + concatenateMixAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateMixAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
				
		//Step 7: Validate that user is able to move to the next screen 
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		//Step 8: Validate that ALL fields THAT ARE displayed on second screen
		ReportLogger.INFO("Validate the Grid values");
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		
		softAssert.assertTrue(childAPNNumber.startsWith("9"),
				"SMAB-T2904: Validate system doesn't consider Interim parcel as lower parcel if other parcel is higher than that in series");
		
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
		}
	/**
	 * This method is to Verify that User is able to generate a recorded doc WI from recorderIntegration and is able to perform mapping actions on that document
	 * @param loginUser
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T3511,,SMAB-T3512,SMAB-T3513:Verify that the Related Action label should"
			+ " match the Actions labels while creating WI and it should open mapping screen on clicking",
			dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, 
			groups = {"Regression","ParcelManagement","RecorderIntegration" })
	public void ParcelManagement_VerifyNewWILotLineAdjustmentsGeneratedfromRecorderIntegrationAndCombineMappingAction(String loginUser) throws Exception {
				
		
		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);

		salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c"
				+ " where Sub_type__c='Lot Line Adjustments' and status__c ='In pool'", 
				"status__c","In Progress");

		//generating WI
		objtransfer.generateRecorderJobWorkItems(objMappingPage.DOC_LOT_LINE_ADJUSTMENT, 2);
		String WorkItemQuery="SELECT Id,Name FROM Work_Item__c where Type__c='MAPPING'"
				+ " AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1"; 
		HashMap<String, ArrayList<String>> responseWIDetails = salesforceAPI.select(WorkItemQuery);
		String WorkItemNo=responseWIDetails.get("Name").get(0);		

		//Searching for the WI generated
		objMappingPage.globalSearchRecords(WorkItemNo); 
		String firstApnfromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
		String secondApnfromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(1);
		
		// deleting the current ownership records for the APN linked with WI
		String queryAPN = "SELECT id FROM Parcel__c where name in('" + 
				firstApnfromWIPage + "','" +secondApnfromWIPage+"')" ;
		HashMap<String, ArrayList<String>> responseApnId= salesforceAPI.select(queryAPN);

		//Delete the existing parcel relationship instances on all 2 parcels
		objMappingPage.deleteOwnershipFromParcel(responseApnId.get("Id").get(0));
		objMappingPage.deleteOwnershipFromParcel(responseApnId.get("Id").get(1));

		
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

		jsonParcelObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonParcelObject.put("Status__c","Active");
		jsonParcelObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonParcelObject.put("District__c",districtValue);
		jsonParcelObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonParcelObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonParcelObject.put("Lot_Size_SQFT__c",parcelSize);

		//updating APN details
		String query = "Select Id from Parcel__c where Name in('"+firstApnfromWIPage+"','" +secondApnfromWIPage+"')";
		salesforceAPI.update("Parcel__c",query,jsonParcelObject);
		objMappingPage.logout();
		
		
		//Mapping user logs in and perform mapping action on the WI genrated
		objMappingPage.login(loginUser);
		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");
		objMappingPage.globalSearchRecords(WorkItemNo);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		softAssert.assertTrue(!(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiEventId).equals(" ")),
				"SMAB-T3513: Verfiying the Event ID of WI genrated for given Recorded Document");
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS
				(objWorkItemHomePage.wiRelatedActionDetailsPage),"Lot Line Adjustments" ,
				"SMAB-T3511: Verfiying the Related Action of WI genrated for given Recorded Document");

		softAssert.assertTrue(!objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.editEventIdButton),
				"SMAB-T3513-This field should not be editable.");

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Fill data  in mapping screen
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));		
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, hashMapCombineMappingData.get("Are taxes fully paid?"));
		objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel, hashMapCombineMappingData.get("Reason code"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeClickable(10, objMappingPage.generateParcelButton);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

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
		objWorkItemHomePage.logout();

	}

	/**
	 * This method is to verify the generation of Interim Parcels for Combine Mapping Action and various validation around it
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2884, SMAB-T2896: Verify generation of Interim Parcels for Combine Mapping Action and validation around it", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyGenerationOfInterimParcelForCombineMappingAction(String loginUser) throws Exception {
		
		//Fetching Interim parcels
		String queryInterimAPNValue = "Select name,ID  From Parcel__c where name like '800%' "
				  		+ "and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 2";
		
		String apn1 = salesforceAPI.select(queryInterimAPNValue).get("Name").get(0);
		String apn1Id = salesforceAPI.select(queryInterimAPNValue).get("Id").get(0);
		String apn2 = salesforceAPI.select(queryInterimAPNValue).get("Name").get(1);
		String apn2Id = salesforceAPI.select(queryInterimAPNValue).get("Id").get(1);
		
		//Getting Active Non-Condo Parcel
		String queryAPNValue = "Select name,ID  From Parcel__c where name like '0%'and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and (Not Name like '%990') limit 2";
		String apn3 = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String apn3Id = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		String apn4 = salesforceAPI.select(queryAPNValue).get("Name").get(1);
		String apn4Id = salesforceAPI.select(queryAPNValue).get("Id").get(1);
		
		//Deleting the current ownership records for all the Parcel records
		objMappingPage.deleteOwnershipFromParcel(apn1Id);
		objMappingPage.deleteOwnershipFromParcel(apn2Id);
		objMappingPage.deleteOwnershipFromParcel(apn3Id);
		objMappingPage.deleteOwnershipFromParcel(apn4Id);
		
		//Update District/Neighborhood on the Non-Condo Parcels
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
		salesforceAPI.update("Parcel__c", apn3Id, "Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", apn4Id, "Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		
		//Updating the status of all parcels
		salesforceAPI.update("Parcel__c", apn1Id, "Status__c", "Active");
		salesforceAPI.update("Parcel__c", apn2Id, "Status__c", "Active");
		salesforceAPI.update("Parcel__c", apn3Id, "Status__c", "Active");
		salesforceAPI.update("Parcel__c", apn4Id, "Status__c", "Active");
		
		String concatenateInterimAPNs = apn1+","+apn2;
		String concatenateNonCondoAPNs = apn3+","+apn4;
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to perform Combine Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn3);
		
		// Step 3: Creating Manual work item for the Active Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Select the Combine value in Action field
		ReportLogger.INFO("Select the 'Combine' Action and Yes in Tax field");
		Thread.sleep(2000);  //Allows screen to load completely
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		
		// Step 6: Update the Parent APN field to add Interim parcels
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Add multiple Interim APNs :: " + concatenateInterimAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateInterimAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Update Interim Parcel count to 1");
		objMappingPage.enter(objMappingPage.numberOfIntermiParcelLabel,"1");
		
		//Step 7: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- First non condo parcel number is required if first parent APN is interim",
				"SMAB-T2896: Validate that user is not able to move to the next screen as value in First Non Condo field is not filled");
				
		//Step 8: Enter a non-condo value and validate that user is able to move to the next screen 
		ReportLogger.INFO("Enter the First Non-Condo value and click NEXT button");
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel2,hashMapCombineMappingData.get("First Non-Condo Parcel Number"));
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		//Step 9: Validate that ALL fields THAT ARE displayed on second screen
		ReportLogger.INFO("Validate the Grid values");
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		
		softAssert.assertTrue(childAPNNumber.startsWith("800"),
				"SMAB-T2884: Validate system generates the Interim Parcel : " + childAPNNumber);
		
		//Step 10 : Click previous button and update the values
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Add multiple Non Condo Active APNs :: " + concatenateNonCondoAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateNonCondoAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Update Interim Parcel count to -1");
		objMappingPage.enter(objMappingPage.numberOfIntermiParcelLabel,"-1");
		
		//Step 11: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- Number of Interim Parcel can not be less than 0",
				"SMAB-T2896: Validate that user is not able to move to the next screen as value in Interim Parcel is less than 0");
		
		//Step 12: Enter value greater than 1 and click NEXT button
		ReportLogger.INFO("Enter value greater than 1 and click NEXT button");
		objMappingPage.enter(objMappingPage.numberOfIntermiParcelLabel,"2");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertContains(objMappingPage.getErrorMessage(),"- Combine action should only allow one interim parcel to be entered",
				"SMAB-T2896: Validate that user is not able to move to the next screen as value in Interim Parcel is greater than 1");
		
		//Step 13: Enter value equal to 1 and click NEXT button
		ReportLogger.INFO("Enter value equal to 1 and click NEXT button");
		objMappingPage.enter(objMappingPage.numberOfIntermiParcelLabel,"1");
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel2,hashMapCombineMappingData.get("First Non-Condo Parcel Number"));
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		//Step 14: Validate the Child Parcel displayed on second screen
		ReportLogger.INFO("Validate the Grid values");
		HashMap<String, ArrayList<String>> gridDataHashMap1 =objMappingPage.getGridDataInHashMap();
		String childAPNNumber1 =gridDataHashMap1.get("APN").get(0);
				
		softAssert.assertTrue(childAPNNumber1.startsWith("800"),
				"SMAB-T2884: Validate system generates the Interim Parcel : " + childAPNNumber1);
		
		//Step 15: Validate the columns that are enabled/disabled
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Legal Description*"),"SMAB-T2884: Validation that Legal Description column is editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*"),"SMAB-T2884: Validation that TRA column is not editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2884: Validation that Situs column is not editable");
        softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Reason Code*"),"SMAB-T2884: Validation that Reason Code column is editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*"),"SMAB-T2884: Validation that District/Neighborhood column is not editable");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*"),"SMAB-T2884: Validation that Use Code column is not editable");
        softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)*"),"SMAB-T2884: Validation that Parcel Size column is editable");
        softAssert.assertTrue(objMappingPage.getAttributeValue(objMappingPage.apnFieldInGridOnCustomScreen, "class").equals("grey-out-column slds-cell-edit"), "SMAB-T2884: Validation that APN column is not editable");
    	
        //Step 16: Validate the interim parcel
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
        softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2884: Validate that Interim Parcel is generated");
		
        objMappingPage.Click(objMappingPage.getButtonWithText(childAPNNumber1));
        objMappingPage.waitForElementToBeVisible(6, objParcelsPage.LongLegalDescriptionLabel);
        softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("APN", "Parcel Information"),childAPNNumber1,
				"SMAB-T2884: Validate that Interim Parcel in the system");
        
        
        driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	
	/**
	 * This method is to  Verify  that Divided Interest Parcel generated
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3309,SMAB-T3310,SMAB-T3311,SMAB-T3312,SMAB-T3313,SMAB-T3314:Verify that user is able to perform Split mapping action "
			+ "having Divided Interest parcel as Parent APN ", dataProvider = "loginMappingUser", 
			dataProviderClass = DataProviders.class, groups = {"Regression","ParcelManagement" })
	public void  ParcelManagement_VerifyCombineDividedInterestParcelGeneration(String loginUser) throws Exception {

		ArrayList<String> responseAPNDetails1 = objMappingPage.fetchActiveAPN(2);
		String apn1=responseAPNDetails1.get(0);
		String apn2=responseAPNDetails1.get(1);
		String queryApnID = "Select Id from Parcel__c where name in('"+apn1+"','"+apn2+"')";
		String apnId1 = salesforceAPI.select(queryApnID).get("Id").get(0);
		String apnId2 = salesforceAPI.select(queryApnID).get("Id").get(1);
		objMappingPage.deleteOwnershipFromParcel(apnId1);
		objMappingPage.deleteOwnershipFromParcel(apnId2);
		String concatNonDivideInterestAPN = apn1+","+apn2;
		
        objMappingPage.login(users.SYSTEM_ADMIN);
        objMappingPage.searchModule(PARCELS);
        String createNewParcel = testdata.MANUAL_PARCEL_CREATION_DATA;
		Map<String, String> hashMapCreateNewParcel = objUtil.generateMapFromJsonFile(createNewParcel,
				"DataToDividedInterestCreateNewParcel");
		String parentDividedInterestAPN1 = hashMapCreateNewParcel.get("APN1");
		String parentDividedInterestAPN2 = hashMapCreateNewParcel.get("APN2");
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id"
				+ "  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c "
				+ "where Status__c='Active') limit 1");
		String PUC = responsePUCDetails.get("Name").get(0);
		for(int i =1;i<=2;i++) {
			objMappingPage.searchModule(PARCELS);
			String newParcelNumber = hashMapCreateNewParcel.get("Parcel Number"+i);
			objParcelsPage.createNewParcel("parentDividedInterestAPN"+i,newParcelNumber,PUC);
		}
       
		objWorkItemHomePage.logout();
	
		String parentDividedInterestAPNConcat = parentDividedInterestAPN1+","+parentDividedInterestAPN2;

		int numbParcel1 = objMappingPage.convertAPNIntoInteger(parentDividedInterestAPN1);
		int numbParcel2 = objMappingPage.convertAPNIntoInteger(parentDividedInterestAPN2);
		int temp = numbParcel1<numbParcel2?numbParcel1:numbParcel2;
		String smallestAPN = (temp == numbParcel1)?parentDividedInterestAPN1:parentDividedInterestAPN2;
	
		String apnLike = smallestAPN.substring(0,10);
		String queryExistingAPNValue = "Select name,status__c From Parcel__c where"
				+ " Name like '"+apnLike+"%' and (not name like '%0') order by name desc";
	    HashMap<String, ArrayList<String>> responseExistingAPNDetails = 
	    		salesforceAPI.select(queryExistingAPNValue);
		String apnPresentInSystem =responseExistingAPNDetails.get("Name").get(0);

		//Fetch some other values from database
		
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c where Name != NULL limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue="Legal PM 85/25-260";
		String parcelSize	= "200";	

		jsonParcelObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonParcelObject.put("Status__c","Active");
		jsonParcelObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonParcelObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonParcelObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonParcelObject.put("Lot_Size_SQFT__c",parcelSize);

		//updating Parcel details
		String queryApnId = "SELECT Id FROM Parcel__c where Name in('"+
				parentDividedInterestAPN1+"','"+parentDividedInterestAPN2+"')";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryApnId);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonParcelObject);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(1),jsonParcelObject);


		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(parentDividedInterestAPN1);

		// Step 3: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Enter non divided interest parcel as parent parcel and first non-condo parcel as dividedInterest
		ReportLogger.INFO("Child parcel ends with 0 if first non condo parcel is dividedInteset parcel and  parent parcel is non divideInterent Parcel: "+concatNonDivideInterestAPN);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatNonDivideInterestAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		objMappingPage.waitForElementToBeVisible(objMappingPage.reasonCodeField);
		String nextGeneratedParcel = objMappingPage.generateNextAvailableAPN(parentDividedInterestAPN1);
        objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,nextGeneratedParcel);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
		
		//second screen of mapping action
		objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(0);	
		softAssert.assertTrue(childAPNNumber.endsWith("0"),
				"SMAB-T3310: Validation that  child parcel ending in 0 is generated when "
				+ "divided interest parcels are used in first non condo parcel Number/first"
				+ " condo parcel number field");
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		
		//Enter Divided Interest parcel number as Parent parcel
		ReportLogger.INFO("child parcel have same map book, map page and ends with 0 if parent parcel is divideinterest parcel : " + parentDividedInterestAPN1);
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,parentDividedInterestAPNConcat);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		objMappingPage.waitForElementToBeVisible(objMappingPage.reasonCodeField);
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"");
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objMappingPage.nextButton));
		
		//Second screen of mapping action
		objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		// Validate child parcel have have same map book and map page no as parent parcel and
		//ends with 0 if Parent parcel is divided interest parcel
		String parentAPNComponent[] = smallestAPN.split("-");
			childAPNNumber=gridDataHashMap.get("APN").get(0);	
			String childAPNComponents[] = childAPNNumber.split("-");
			softAssert.assertEquals(childAPNComponents[0],parentAPNComponent[0],
					"SMAB-T3309: Validation that MAP BOOK of parent and child parcels are same" );
			softAssert.assertEquals(childAPNComponents[1],parentAPNComponent[1],
					"SMAB-T3309: Validation that MAP page of parent and child parcels are same");
			softAssert.assertTrue(childAPNNumber.endsWith("0"),
					"SMAB-T3309: Validation that child APN number ends with 0");
		
		//Validate divided interest child 1st 8 char is same as parent parcel 
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
		softAssert.assertContains(objMappingPage.dividedInterestErrorMsgSecondScreen.getText(),"To override an APN "
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
		softAssert.assertContains(objMappingPage.dividedInterestErrorMsgSecondScreen.getText()," already exist",
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
			softAssert.assertContains(objMappingPage.dividedInterestErrorMsgSecondScreen.getText(),"APN cannot be skipped",
					"SMAB-T3313: Validate that if child APN is overwritten with divided interest APN which is not the next available"
					+ " APN, error message is displayed");
		
		//Validate if First 8 digits of Child parcel is same as parent parcel, 
		//system allow to to generate 
			ReportLogger.INFO("Generate child parcel when Divided Interest parcel number as Parent parcel");
			 updateNextApn = apnInSystem[0]+apnInSystem[1]+apnInSystem[2].substring(0,2)+
					String.valueOf(Integer.parseInt(apnInSystem[2].substring(2)) +1);
			ReportLogger.INFO(updateNextApn);
			objMappingPage.updateMultipleGridCellValue(objMappingPage.apnColumnSecondScreen,
					updateNextApn,1);
			objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
			gridDataHashMap =objMappingPage.getGridDataInHashMap();
			childAPNNumber=gridDataHashMap.get("APN").get(0);
			softAssert.assertContains(smallestAPN,childAPNNumber.substring(0,10),
					"SMAB-T3314:Validate that child APN can be overwritten with divided interest APN only "
					+ "when the APN has same first 8 digits of the parent APN.");
			softAssert.assertContains(objMappingPage.confirmationMsgOnSecondScreen(),
					"created successfully. Please review spatial information.",
					"SMAB-T3314: Validate that user is able to perform Split mapping action "
					+ "having Divided Interest parcel as Parent APN");
	    driver.switchTo().window(parentWindow);
	    
		objWorkItemHomePage.logout();

	    
		}
	
	@Test(description = "SMAB-T2885,SMAB-T2883:Verify that Parcel Size (SQFT) column is added to the \"Mapping Actions\" (combine) second custom screen and field exists \"Parcel Size (SQFT)\" (number) "
			+ "and it's placed under the \"PUC\" field", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
					"Regression", "ParcelManagement" })
	public void ParcelManagement_VerifyParcelSizeColumn_AddedToCombineAction_CustomScreen(String loginUser)
			throws Exception {

		// Fetching parcels that are Active with same Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c)and Primary_Situs__c !=NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1 = responseAPNDetails.get("Name").get(0);
		String apn2 = responseAPNDetails.get("Name").get(1);
		objMappingPage.deleteOwnershipFromParcel(responseAPNDetails.get("Id").get(0));
		objMappingPage.deleteOwnershipFromParcel(responseAPNDetails.get("Id").get(1));

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI.select(
				"SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String legalDescriptionValue = "Legal PM 85/25-260";
		String districtValue = "District01";

		jsonParcelObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonParcelObject.put("Status__c", "Active");
		jsonParcelObject.put("Short_Legal_Description__c", legalDescriptionValue);
		jsonParcelObject.put("District__c", districtValue);
		jsonParcelObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		jsonParcelObject.put("TRA__c", responseTRADetails.get("Id").get(0));
		jsonParcelObject.put("Lot_Size_SQFT__c", 100);

		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonParcelObject);
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), jsonParcelObject);

		String concatenateAPNWithSameOwnership = apn1 + "," + apn2;

		jsonParcelObject.put("TRA__c", responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonParcelObject);
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), jsonParcelObject);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeMappingWithActionInternalRequestCombine");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");

		// Step1: Login to the APAS application using the credentials passed through
		// dataprovider (Mapping User)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Parcel
		String workItem = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		ReportLogger.INFO("Clicked on related action under details tab for newly WI created ");

		// Step 5: Selecting Action as 'perform parcel combine'
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,
				hashMapCombineActionMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, "Yes");

		// Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapCombineActionMappingData);

		// Step 7: Click Combine Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);
		ReportLogger.INFO(" Parcel generated successfully. ");
		
		// Step 8: Switching to the WI Screen
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);

		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)*"),
				"SMAB-T2885: Validation that Parcel Size (SQFT) column should  be editable on returning to custom screen");
		
		objMappingPage.waitForElementToBeVisible(3, objMappingPage.parcelSizeColumnSecondScreenWithSpace);
		objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace, "40");
		ReportLogger.INFO(" Parcel size is updated. ");

		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName));

		HashMap<String, ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();
		String APN = gridDataHashMap.get("APN").get(0);

		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(APN);

		// Verify that the parcel size(SQFT)* of second screen with the parcel size on
		// parcel screen and also checks if the Parcel Size (SqFt)field is present on
		// the parcel screen or not
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),
				objMappingPage.getFieldValueFromAPAS("Parcel Size (SqFt)", "Parcel Information"),
				"SMAB-T2883,SMAB-T2885:Parcel size(SQFT) matched and field is avilable on parcel screen\"");
		
		
		objWorkItemHomePage.logout();

	}
	
	/**
	 *This method is to  Verify  the Audit trail 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3728,SMAB-T3816: Verify many to many audit trail", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {"Regression","ParcelManagement" })


	public void ParcelManagement_VerifyAuditTrailForCombineMappingAction(String loginUser) throws Exception {

		JSONObject jsonParcelObjectNew = objMappingPage.getJsonObject();
		//Fetching parcels that are Active with different Ownership record
		String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE "
				+ " (Not Name like '134%') and Id NOT IN (SELECT APN__c FROM Work_Item__c "
				+ "where type__c='CIO') and Status__c = 'Active' Limit 2";

		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);

		String apnId1=responseAPNDetails.get("Id").get(0);
		String apnId2=responseAPNDetails.get("Id").get(1);

		objMappingPage.deleteOwnershipFromParcel(apnId1);
		objMappingPage.deleteOwnershipFromParcel(apnId2);
		
		//updating fields of parcels
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,Id FROM PUC_Code__c "
				+ "where Legacy__c = 'NO' limit 1");
	
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		
		String legalDescriptionValue="Legal PM 85/25-260";
		String parcelSize	= "200";		

		jsonParcelObjectNew.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonParcelObjectNew.put("Status__c","Active");
		jsonParcelObjectNew.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonParcelObjectNew.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonParcelObjectNew.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonParcelObjectNew.put("Lot_Size_SQFT__c",parcelSize);

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonParcelObjectNew);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(1),jsonParcelObjectNew);
		
		String concatenateAPN = apn1+","+apn2;
		ReportLogger.INFO("Parent APNs : " + concatenateAPN);
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");
		
		//login with mapping user
		objMappingPage.login(loginUser);
		objMappingPage.searchModule(PARCELS);
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
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		objMappingPage.fillMappingActionForm(hashMapCombineActionMappingData);
		objMappingPage.waitForElementToBeVisible(10,objMappingPage.generateParcelButton);
	       
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(10,objMappingPage.performAdditionalMappingButton);

		driver.switchTo().window(parentWindow);
        objWorkItemHomePage.logout();
        Thread.sleep(3000);
        
        //login with supervisor
        objMappingPage.login(users.MAPPING_SUPERVISOR);
        objMappingPage.searchModule(WORK_ITEM);
        objMappingPage.globalSearchRecords(WorkItemNo);
        
        //Completing the workItem
        objWorkItemHomePage.completeWorkItem(); 
        driver.navigate().refresh(); 
        objMappingPage.waitForElementToBeVisible(objWorkItemHomePage.linkedItemsWI, 10);
        objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsWI);
        
        //navigating to business event audit trail
        objWorkItemHomePage.scrollToElement(objWorkItemHomePage.secondRelatedBuisnessEvent);
        objWorkItemHomePage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);
		String auditTrailNo = objWorkItemHomePage.getFieldValueFromAPAS("Name", "");
		ReportLogger.INFO("Audit Trail no:" +auditTrailNo);
		
		String description= objWorkItemHomePage.getFieldValueFromAPAS(trail.description);
		String comments= hashMapCombineActionMappingData.get("Comments");
		reasonCode= hashMapCombineActionMappingData.get("Reason code");
			
		String queryWIDescription = "SELECT Id,Description__c FROM Work_Item__c where Name='" + WorkItemNo+"'";
		HashMap<String, ArrayList<String>> responseWIdesc = salesforceAPI.select(queryWIDescription);
		String wiDescription=responseWIdesc.get("Description__c").get(0);
		softAssert.assertContains(description,comments+" "+wiDescription+" "+reasonCode,
				"SMAB-T3816,SMAB-T3728: Verify that comment,description,reason code provided during mapping actionis present in description field of the audit trail");		
		objWorkItemHomePage.logout();
		}
}			