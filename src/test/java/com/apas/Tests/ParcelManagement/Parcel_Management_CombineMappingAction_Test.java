package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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

public class Parcel_Management_CombineMappingAction_Test extends TestBase implements testdata, modules, users {
	
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonParcelObject= new JSONObject();
	

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
	 * This method is to Verify that User is able to view various error messages (excluding Ownership ones) while performing "Combine" mapping action from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2356: Verify user is able to view error messages on the first screen during combine process", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyErrorMessageOnFirstScreenForParcelCombineMappingAction(String loginUser) throws Exception {
		
		//Getting Owner or Account records
	    String assesseeName = objMappingPage.getOwnerForMappingAction();
		
		//Getting parcel that is Retired 
	    HashMap<String, ArrayList<String>> retiredAPN = objMappingPage.getRetiredApnHavingOwner(assesseeName);
	    String retiredAPNValue = retiredAPN.get("Name").get(0);
		
		//Getting parcel that is In Progress - To Be Expired	
	    HashMap<String, ArrayList<String>> inProgressAPN = objMappingPage.getInProgressApnHavingOwner(assesseeName);
	    String inProgressAPNValue = inProgressAPN.get("Name").get(0);
	   
		//Getting parcels that are Active 
	    HashMap<String, ArrayList<String>> activeAPN = objMappingPage.getActiveApnHavingOwner(assesseeName,2);
	    String apn1 = activeAPN.get("Name").get(0);
		String apn2 = activeAPN.get("Name").get(1);
		
		//Data Manipulation to test various validations
		String activeParcelWithoutHyphen=apn2.replace("-","");
		String accessorMapParcel = apn1.replace("-", "").substring(0, 5);
		String concatenateActiveAPN = apn1+","+apn2;
		String concatenateRetireWithActiveAPN = apn1+","+retiredAPNValue;
		String concatenateInProgressWithActiveAPN = inProgressAPNValue+","+apn1;
		String legalDescriptionValue="Legal PM 85/25-260";
		
		//Fetch TRA value from database to enter in APN to test validations
		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		
		//Enter values in the Parcels
		jsonParcelObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonParcelObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c",activeAPN.get("Id").get(0),jsonParcelObject);
		
		salesforceAPI.update("Parcel__c", activeAPN.get("Id").get(1), "TRA__c", responseTRADetails.get("Id").get(1));
		salesforceAPI.update("Parcel__c", retiredAPN.get("Id").get(0), "TRA__c", responseTRADetails.get("Id").get(1));
		salesforceAPI.update("Parcel__c", inProgressAPN.get("Id").get(0), "TRA__c", responseTRADetails.get("Id").get(0));
		
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
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Please provide more than one APN to combine",
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
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-Warning: TRAs of the combined parcels are different\n-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2356: Validate that user is able to view Warning message");
				
		//Step 10: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-Warning: TRAs of the combined parcels are different\n-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2356: Validate that user is not able to move to the next screen and still able to view Warning message");
			
		// Step 11: Update the Parent APN field and add an In Progress parcel
		ReportLogger.INFO("Add an In Progress parcel in Parent APN field :: " + concatenateInProgressWithActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateInProgressWithActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2356: Validate that user is able to view error message for Inactive parcel");
								
		//Step 12: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2356: Validate that user is not able to move to the next screen and still able to view error message for Inactive parcel");
				
		// Step 13: Add parcels in Parent APN field with different TRA records
		ReportLogger.INFO("Add parcels in Parent APN field with different TRA records :: " + apn1 + ", " + apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateActiveAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-Warning: TRAs of the combined parcels are different",
				"SMAB-T2356: Validate that user is able to view warning message");
				
		//Step 14: Validate that user is able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
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
		
		//Step 16: Validate that Legal description appears from the first parent parcel
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescriptionValue,
				"SMAB-T2356: Validate that System populates Legal Description from the parent parcel");
		
		//Step 17: Click PREVIOUS button and validate that Reason Code is retained and Comments are not
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2356: Validate that value of 'Reason Code' field is retained");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.commentsTextBoxLabel)),"",
				"SMAB-T2356: Validate that value entered in Comments section doesn't get retained");
		
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
		softAssert.assertEquals(gridDataMap.get("Legal Description").get(0),hashMapCombineMappingData.get("Legal Description"),
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
	@Test(description = "SMAB-T2356,SMAB-T2358: Verify user is able to overwrite APN generated by system during Combine process", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void AParcelManagement_VerifyParcelOverwriteForCombineMappingAction(String loginUser) throws Exception {
		
		//Getting Owner or Account records
		HashMap<String, ArrayList<String>> responseAssesseeDetails = objMappingPage.getOwnerForMappingAction(3);
	    String assesseeName1 = responseAssesseeDetails.get("Name").get(0);
		String assesseeName2 = responseAssesseeDetails.get("Name").get(2);
		
		/*String queryAssesseeRecord = "SELECT Id, Name FROM Account";
		HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		String assesseeName1 = responseAssesseeDetails.get("Name").get(0);
		String assesseeName2 = responseAssesseeDetails.get("Name").get(2);*/
		
		//Fetching parcel that is Retired 
		String retiredAPNValue = objMappingPage.fetchRetiredAPN();
		
		//Fetching Interim parcel 
		String interimAPN = objMappingPage.fetchInterimAPN();
		
		/*String interimAPNDetail = "Select name,ID  From Parcel__c where name like '800%' AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseInterimAPNDetails = salesforceAPI.select(interimAPNDetail);
		String interimAPN=responseInterimAPNDetails.get("Name").get(0);*/
		
		//Fetching parcel that are Active  with Ownership record 1
		HashMap<String, ArrayList<String>> responseAPNDetails1 = objMappingPage.getActiveApnHavingOwner(assesseeName1, 2);
		String apn1=responseAPNDetails1.get("Name").get(0);
		String apn2=responseAPNDetails1.get("Name").get(1);
		
		/*String queryAPNValue1 = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName1 + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName1 + "') and (Not Name like '%990') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails1 = salesforceAPI.select(queryAPNValue1);
		String apn1=responseAPNDetails1.get("Name").get(0);
		String apn2=responseAPNDetails1.get("Name").get(1);*/
		
		//Fetching parcel that are Active  with Ownership record 2
		HashMap<String, ArrayList<String>> responseAPNDetails2 = objMappingPage.getActiveApnHavingOwner(assesseeName2);
		String apn3=responseAPNDetails2.get("Name").get(0);
		
		/*String queryAPNValue2 = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName2 + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName2 + "') and (Not Name like '%990') and Status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails2 = salesforceAPI.select(queryAPNValue2);
		String apn3=responseAPNDetails2.get("Name").get(0);*/
		
		//Fetching parcel that are Active  with No Ownership record
		HashMap<String, ArrayList<String>> responseAPNDetails3 = objMappingPage.getActiveApnWithNoOwner();
		String apn4=responseAPNDetails3.get("Name").get(0);
		
		/*String queryAPNValue3 = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Status__c = 'Active' LIMIT 1";
		HashMap<String, ArrayList<String>> responseAPNDetails3 = salesforceAPI.select(queryAPNValue3);
		String apn4=responseAPNDetails3.get("Name").get(0);*/
		
		//Data Manipulation to test the overwrite scenarios
		String concatenateAPNWithSameOwnership = apn1+","+apn2;
		String concatenateAPNWithDifferentOwnership = apn1+","+apn3;
		String concatenateAPNWithOneWithNoOwnership = apn1+","+apn4;
		String lessThan9DigitAPN = apn1.substring(0, 9);
		String moreThan9DigitAPN = apn2.concat("0");
		String alphanumericAPN1 = apn1.substring(0, 10).concat("a");
		String alphanumericAPN2 = "abc" + apn1.substring(3, 11);
		String specialSymbolAPN = apn1.substring(0, 9).concat(".%");
		
		//Fetch TRA value from database and Enter it in Parcels
		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
				
		salesforceAPI.update("Parcel__c", responseAPNDetails1.get("Id").get(0), "TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", responseAPNDetails1.get("Id").get(1), "TRA__c", responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", responseAPNDetails2.get("Id").get(0), "TRA__c", responseTRADetails.get("Id").get(1));
		salesforceAPI.update("Parcel__c", responseAPNDetails3.get("Id").get(0), "TRA__c", responseTRADetails.get("Id").get(0));
				
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
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-Warning: TRAs of the combined parcels are different\n-In order to proceed with a parcel combine action, the parent APN must have the same ownership and ownership allocation",
				"SMAB-T2356: Validate that user is able to view Warning message");
						
		//Step 6: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-Warning: TRAs of the combined parcels are different\n-In order to proceed with a parcel combine action, the parent APN must have the same ownership and ownership allocation",
				"SMAB-T2356: Validate that user is able to view Warning message");
							
		// Step 7: Update the Parent APN field and add another parcel with no ownership record
		ReportLogger.INFO("Add a parcel in Parent APN field with no ownership record :: " + apn4);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithOneWithNoOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with a parcel combine action, the parent APN must have the same ownership and ownership allocation",
				"SMAB-T2356: Validate that user is able to view error message related to ownership record");						
		
		//Step 8: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with a parcel combine action, the parent APN must have the same ownership and ownership allocation",
				"SMAB-T2356: Validate that user is able to view error message related to ownership record");										
		
		// Step 9: Update the Parent APN field and add another parcel with same ownership record
		ReportLogger.INFO("Add a parcel in Parent APN field with same ownership record :: " + apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		Thread.sleep(1000);   //Allows screen to load completely so that non-availability of the error message can be validated
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.errorMessageOnScreenOne),
				"SMAB-T2356: Validate that user is not able to view error message related to ownership record");
				
		//Step 10: Validate that user is able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		//Step 11 :Overwrite parcel value with existing parcel# and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,apn3);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + apn3);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"The APN provided already exists in the system",
				"SMAB-T2358: Validate that User is able to view error message if APN (Active) already exist");
		
		//Step 12 :Overwrite parcel value with retired parcel# and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,retiredAPNValue);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + retiredAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"The APN provided already exists in the system",
				"SMAB-T2358: Validate that User is able to view error message if APN (Retired) already exist");
		
		//Step 13 :Overwrite parcel value with interim parcel# and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,interimAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + interimAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"This map book is reserved for interim parcels",
				"SMAB-T2358: Validate that User is able to view error message if Interim APN is overwritten");
		
		//Step 14 :Overwrite parcel value with less than 9 digits and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,lessThan9DigitAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + lessThan9DigitAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2358: Validate that User is able to view error message if APN is less than 9 digits");
		
		//Step 15 :Overwrite parcel value with more than 9 digits and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,moreThan9DigitAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + moreThan9DigitAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.r",
				"SMAB-T2358: Validate that User is able to view error message if APN is more than 9 digits");
		
		//Step 16 : Click previous button followed by next button
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String nextGeneratedAPN = gridDataHashMap.get("APN").get(0);
		
		//Step 17 : Use the generated APN and create the next available in that order
		String notNextGeneratedAPN = objMappingPage.generateNextAvailableAPN(nextGeneratedAPN);
		
		//Step 18 :Overwrite parcel value with above generated APN and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,notNextGeneratedAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + notNextGeneratedAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"The parcel entered is invalid since the following parcel is available " + nextGeneratedAPN,
				"SMAB-T2358: Validate that User is able to view error message if APN overwritten is not the next available one in the system");
	
		//Step 19 :Overwrite parcel value with alphanumeric value and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,alphanumericAPN1);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + alphanumericAPN1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2358: Validate that User is able to view error message if APN is overwritten with alphanumeric value at the end");
		
		//Step 20 :Overwrite parcel value with alphanumeric value and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,alphanumericAPN2);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + alphanumericAPN2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2358: Validate that User is able to view error message if APN is overwritten with alphanumeric value in the beginning");
		
		//Step 21 :Overwrite parcel value with special symbol and Click Combine parcel button
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,specialSymbolAPN);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);
		ReportLogger.INFO("Click on Combine Parcel button after updating the APN value :: " + specialSymbolAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2358: Validate that User is able to view error message if APN is overwritten with special symbol");
		
		
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
		
	}
	
	
	/**
	 * This method is to validate APN generated by system during Combine process 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2359: Verify user is able to validate APN generation by system during Combine process", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyParcelGenerationForCombineMappingAction(String loginUser) throws Exception {
		
		//Fetching Assessee records
		String queryAssesseeRecord = "SELECT Id, Name FROM Account Limit 1";
		HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		String assesseeName = responseAssesseeDetails.get("Name").get(0);
		
		//Fetching parcel that are Active with Ownership record 
		String queryAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName + "') and (Not Name like '%990') and Status__c = 'Active' Limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);
		
		String concatenateAPNWithSameOwnership = apn1+","+apn2;
		String concatenateAPNInReverseOrderWithSameOwnership = apn2+","+apn1;
		String combineMapBookAndPageForFirstParcel = apn1.substring(0, 8);
		String combineMapBookAndPageForSecondParcel = apn2.substring(0, 8);
		
		//Query last APN in Map Book and Map Page for first parcel
		String queryLastAPNValueForFirstParcel = "SELECT Name FROM Parcel__c where Name like '" + combineMapBookAndPageForFirstParcel + "%' ORDER BY Name DESC LIMIT 1";
		HashMap<String, ArrayList<String>> responseLastAPNValueForFirstParcel = salesforceAPI.select(queryLastAPNValueForFirstParcel);
		String parcel1=responseLastAPNValueForFirstParcel.get("Name").get(0);
		
		//Create the next available APN
		String updatedAPN1 = objMappingPage.generateNextAvailableAPN(parcel1);
		
		//Query last APN in Map Book and Map Page for second parcel
		String queryLastAPNValueForSecondParcel = "SELECT Name FROM Parcel__c where Name like '" + combineMapBookAndPageForSecondParcel + "%' ORDER BY Name DESC LIMIT 1";
		HashMap<String, ArrayList<String>> responseLastAPNValueForSecondParcel = salesforceAPI.select(queryLastAPNValueForSecondParcel);
		String parcel2=responseLastAPNValueForSecondParcel.get("Name").get(0);
		
		//Create the next available APN
		String updatedAPN2 = objMappingPage.generateNextAvailableAPN(parcel2);
		
		//Fetching a Condo Active parcel
		String queryCondoAPN = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName + "') and name like '100%' and (Not Name like '%990') and Status__c = 'Active' Limit 1";
		HashMap<String, ArrayList<String>> responseCondoAPNDetails = salesforceAPI.select(queryCondoAPN);
		String condoAPN=responseCondoAPNDetails.get("Name").get(0);
		String combineMapBookAndPageForCondoParcel = condoAPN.substring(0, 8);
		String concatenateCondoWithNonCondoWithSameOwnership = condoAPN+","+apn1;
		
		//Query last APN in Map Book and Map Page for second parcel
		String queryLastAPNValueForCondoParcel = "SELECT Name FROM Parcel__c where Name like '" + combineMapBookAndPageForCondoParcel + "%' ORDER BY Name DESC LIMIT 1";
		HashMap<String, ArrayList<String>> responseLastAPNValueForCondoParcel = salesforceAPI.select(queryLastAPNValueForCondoParcel);
		String parcel3=responseLastAPNValueForCondoParcel.get("Name").get(0);
		
		//Create the next available APN
		String updatedAPN3 = objMappingPage.generateNextAvailableAPN(parcel3);
		
		
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
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Update the Parent APN field and add another parcel with same ownership record and move to next screen
		ReportLogger.INFO("Add a parcel in Parent APN field with same ownership record :: " + apn1 + ", " + apn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		// Step 6: Fetch the APN generated
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String nextGeneratedAPN1 = gridDataHashMap.get("APN").get(0);
		
		softAssert.assertEquals(nextGeneratedAPN1,updatedAPN1,
				"SMAB-T2359: Validate that APN generated is from the same Map Book & Map Page as of first parcel in Parent APN field");
		softAssert.assertTrue(nextGeneratedAPN1.endsWith("0"),
				"SMAB-T2359: Validation that child APN number ends with 0");
		
		// Step 7: Click previous button and update parent parcel field
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Reverse the parcels in Parent APN field with same ownership record :: " + apn2 + ", " + apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNInReverseOrderWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		// Step 8: Fetch the APN generated
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String nextGeneratedAPN2 = gridDataHashMap.get("APN").get(0);
		
		softAssert.assertEquals(nextGeneratedAPN2,updatedAPN2,
				"SMAB-T2359: Validate that APN generated is from the same Map Book & Map Page as of first parcel in Parent APN field");
		softAssert.assertTrue(nextGeneratedAPN2.endsWith("0"),
				"SMAB-T2359: Validation that child APN number ends with 0");
		
		// Step 9: Click previous button and update parent parcel field
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Add Condo parcel as first parent parcel in Parent APN field along with Non-Condo parcel with same ownership record :: " + condoAPN + ", " + apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateCondoWithNonCondoWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		// Step 10: Fetch the APN generated
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String nextGeneratedAPNWithCondoAsFirstParcel = gridDataHashMap.get("APN").get(0);
		
		softAssert.assertEquals(nextGeneratedAPNWithCondoAsFirstParcel,updatedAPN3,
				"SMAB-T2359: Validate that APN generated is from the same Map Book & Map Page as of first parcel in Parent APN field");
		softAssert.assertTrue(nextGeneratedAPN2.endsWith("0"),
				"SMAB-T2359: Validation that child APN number ends with 0");
		
		//Fetching parcel that are Active and ends with 990
		String queryLastAPNInMapBookAndPage = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName + "') and name like '%990' and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseLastAPNInMapBookAndPageDetails = salesforceAPI.select(queryLastAPNInMapBookAndPage);
		String lastAPNInMapBookAndPage=responseLastAPNInMapBookAndPageDetails.get("Name").get(0);
		
		String mapBookInAPNFetched = lastAPNInMapBookAndPage.substring(0, 3);
		String mapPageInAPNFetched = lastAPNInMapBookAndPage.substring(4, 7);
		String concatenateTwoAPNWithSameOwnership = lastAPNInMapBookAndPage+","+apn1;
		
		// Step 11: Click previous button and update parent parcel field
		ReportLogger.INFO("Click PREVIOUS button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Add two parcels in Parent APN field with same ownership record :: " + lastAPNInMapBookAndPage + ", " + apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateTwoAPNWithSameOwnership);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.nextButton);
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		// Step 12: Fetch the APN generated
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String nextGeneratedAPNWithChangedMapPage = gridDataHashMap.get("APN").get(0);
		String mapBookInGeneratedAPN = nextGeneratedAPNWithChangedMapPage.substring(0, 3);
		String mapPageInGeneratedAPN = nextGeneratedAPNWithChangedMapPage.substring(4, 7);
		
		// Step 13: Validate the APN generated
		String createdAPN = objMappingPage.generateNextAvailableAPN(lastAPNInMapBookAndPage);
		if(!createdAPN.substring(0, 3).equals("War")){
			softAssert.assertEquals(createdAPN,nextGeneratedAPNWithChangedMapPage,
				"SMAB-T2359: Validate that next available APN generated");
		}
		else{
			ReportLogger.INFO(createdAPN);	
			softAssert.assertEquals(mapBookInGeneratedAPN,mapBookInAPNFetched,
					"SMAB-T2359: Validate that APN generated is from the same Map Book as of first parcel in Parent APN field");
			softAssert.assertTrue(!mapPageInGeneratedAPN.equals(mapPageInAPNFetched),
					"SMAB-T2359: Validate that APN generated (" + nextGeneratedAPNWithChangedMapPage + ") is from the different Map Page as of first parcel (" + lastAPNInMapBookAndPage + ") in Parent APN field");
			softAssert.assertTrue(nextGeneratedAPNWithChangedMapPage.endsWith("0"),
					"SMAB-T2359: Validation that child APN number ends with 0");
		}
		
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	
	/**
	 * This method is to Verify that User is able to view various error messages (excluding Ownership one) while perform a "Combine" mapping action from a work item
	 * This method is to Verify user is able to combine as many number of parcels into one
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2356,SMAB-T2357: Verify user is able to combine as many number of parcels into one", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyParcelCombineMappingAction(String loginUser) throws Exception {
		
		//Fetching Assessee records
		String queryAssesseeRecord = "SELECT Id, Name FROM Account";
		HashMap<String, ArrayList<String>> responseAssesseeDetails = salesforceAPI.select(queryAssesseeRecord);
		String assesseeName = responseAssesseeDetails.get("Name").get(0);
		
		//Fetching parcel that is Retired 
		String queryRetiredAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') and Status__c = 'Retired'";
		HashMap<String, ArrayList<String>> responseRetiredAPNDetails = salesforceAPI.select(queryRetiredAPNValue);
		String retiredAPNValue= salesforceAPI.select(queryRetiredAPNValue).get("Name").get(0);
		
		//Fetching parcel that is In Progress - To Be Expired	
		String queryInProgressAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') and Status__c = 'In Progress - To Be Expired'";
		HashMap<String, ArrayList<String>> responseInProgressAPNDetails = salesforceAPI.select(queryInProgressAPNValue);
		String inProgressAPNValue= salesforceAPI.select(queryInProgressAPNValue).get("Name").get(0);
				
		//Fetching parcels that are Active 
		String queryAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') and Status__c = 'Active'";
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
		
		String activeParcelWithoutHyphen=apn2.replace("-","");
		String accessorMapParcel = apn1.replace("-", "").substring(0, 5);
		String legalDecsription = "Test Legal Description";
		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";
		
		String concatenateActiveAPN = apn1+","+apn2;
		String concatenateRetireWithActiveAPN = apn1+","+retiredAPNValue;
		String concatenateInProgressWithActiveAPN = inProgressAPNValue+","+apn1;
		
		//Enter values in the Parcels
		jsonParcelObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonParcelObject.put("Status__c","Active");
		jsonParcelObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonParcelObject.put("District__c",districtValue);
		jsonParcelObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonParcelObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonParcelObject);
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), "TRA__c", responseTRADetails.get("Id").get(1));
		salesforceAPI.update("Parcel__c", responseRetiredAPNDetails.get("Id").get(0), "TRA__c", responseTRADetails.get("Id").get(1));
		salesforceAPI.update("Parcel__c", responseInProgressAPNDetails.get("Id").get(0), "TRA__c", responseTRADetails.get("Id").get(0));
		
		
		
		//salesforceAPI.update("Parcel__c", responseInProgressAPNDetails.get("Id").get(0), "TRA__c", responseTRADetails.get("Id").get(0));
		
		
		
		
		//Fetching some more Active parcels
		String queryCondoAPN = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') and name like '100%' and Status__c = 'Active'";
		HashMap<String, ArrayList<String>> responseCondoAPNDetails = salesforceAPI.select(queryCondoAPN);
		String apn3=responseCondoAPNDetails.get("Name").get(0);
		
		String queryMobileHomeAPN = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') and name like '133%' and Status__c = 'Active'";
		HashMap<String, ArrayList<String>> responseMobileHomeAPNDetails = salesforceAPI.select(queryMobileHomeAPN);
		String apn4=responseMobileHomeAPNDetails.get("Name").get(0);
						
		String concatenateMixAPNs = apn1+","+apn2+","+apn3+","+apn4;
				
		
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

		// Step 6: Select the Combine value in Action field, various values in 'Are Taxes fully paid?' field and validate other fields in layout are/are not visible
		ReportLogger.INFO("Select the 'Combine' Action and Yes in Tax field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		
		// Step 15: Update the Parent APN field and add more Active parcel records
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
		ReportLogger.INFO("Add multiple Active APNs :: " + concatenateMixAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateMixAPNs);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.reasonCodeField);
				
		//Step 16: Validate that user is able to move to the next screen
		ReportLogger.INFO("Click NEXT button");
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(6, objMappingPage.useCodeFieldSecondScreen);
		
		//Step 17: Verify that APNs generated must be 9-digits and should end in '0'
		ReportLogger.INFO("Validate the Grid values");
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		

		//Step 18: Validation of ALL fields THAT ARE displayed on second screen
		//softAssert.assertEquals(gridDataHashMap.get("District").get(0),districtValue,
		//		"SMAB-T2356: Validation that System populates District from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),primarySitusValue.replaceFirst("\\s", ""),
				"SMAB-T2356: Validation that System populates Situs from the parent parcel");
		//softAssert.assertEquals(gridDataHashMap.get("Neighborhood Code").get(0),responseNeighborhoodDetails.get("Name").get(0),
		//		"SMAB-T2356: Validation that System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
				"SMAB-T2356: Validation that System populates reason code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescriptionValue,
				"SMAB-T2356: Validation that System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2356: Validation that System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),responsePUCDetails.get("Name").get(0),
				"SMAB-T2356: Validation that System populates Use Code  from the parent parcel");

		//Step 19 :Verify that User is able to update Legal Description on the Grid
		ReportLogger.INFO("Update the Legal Description field");
		objMappingPage.editGridCellValue(objMappingPage.legalDescriptionColumnSecondScreen,legalDecsription);
		objMappingPage.Click(objMappingPage.useCodeFieldSecondScreen);

		//Step 20 :Clicking generate parcel button
		ReportLogger.INFO("Click on Combine Parcel button");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.combineParcelButton));
		softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"APNs have been Combined Successfully!",
				"SMAB-T2357: Validate that User is able to perform Combine action for multiple active parcels");
		
		//Step 21: Validation of ALL fields THAT ARE displayed AFTER PARCEL ARE GENERATED
		ReportLogger.INFO("Validate the Grid values");
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),primarySitusValue.replaceFirst("\\s", ""),
				"SMAB-T2357: Validation that System populates Situs  from the parent parcel");
		//softAssert.assertEquals(gridDataHashMap.get("Neighborhood Code").get(0),responseNeighborhoodDetails.get("Name").get(0),
		//		"SMAB-T2357: Validation that System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
				"SMAB-T2357: Validation that System populates reason code from parent parcel work item");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDecsription,
				"SMAB-T2357: Validation that System populates Legal Description which was updated in Grid");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2357: Validation that System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),responsePUCDetails.get("Name").get(0),
				"SMAB-T2357: Verify that User is able to to create a Use Code for the child parcel from the custom screen ");
		
		
		
		
		
		//Open the child parcel and validate the status
		driver.switchTo().window(parentWindow);
		objMappingPage.searchModule(PARCELS);
		
		objMappingPage.globalSearchRecords(childAPNNumber);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"In Progress - New Parcel",
				"SMAB-T2373: Validate the status of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"In Progress - New Parcel",
				"SMAB-T2373: Validate the PUC of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelTRA, "Parcel Information"),"",
				"SMAB-T2373: Validate the PUC of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus, "Parcel Information"),"",
				"SMAB-T2373: Validate the PUC of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelDistrictNeighborhood, "Parcel Information"),"",
				"SMAB-T2373: Validate the PUC of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelShortLegalDescription, "Parcel Information"),"",
				"SMAB-T2373: Validate the PUC of child parcel generated");
		
		
		
		
		objParcelsPage.openRelatedTabInParcelRecord("Parcel Relationships");
		HashMap<String, ArrayList<String>> relationshipTableDataHashMap = objParcelsPage.getParcelTableDataInHashMap("Source Parcel Relationships");
		
		Map<String,String> apnValue = new HashMap<String,String>(); 
	     
		apnValue.put("APN1", apn1); 
		apnValue.put("APN2", apn2); 
		apnValue.put("APN3", apn3); 
		apnValue.put("APN4", apn4); 
          
       /* // using for-each loop for iteration over Map.entrySet() 
        //for (Map.Entry<String,String> entry : apnValue.entrySet())
        	boolean apnExist = apnValue.containsValue(tableDataHashMap.get("Source Parcel").get(0));
            //System.out.println("Key = " + entry.getKey() + 
            //                 ", Value = " + entry.getValue()); 
    */
		

		softAssert.assertTrue(apnValue.containsValue(relationshipTableDataHashMap.get("Source Parcel").get(0)),
				"SMAB-T2373: Validation that System populates Situs  from the parent parcel");
		softAssert.assertTrue(apnValue.containsValue(relationshipTableDataHashMap.get("Source Parcel").get(1)),
				"SMAB-T2373: Validation that System populates Situs  from the parent parcel");
		softAssert.assertTrue(apnValue.containsValue(relationshipTableDataHashMap.get("Source Parcel").get(2)),
				"SMAB-T2373: Validation that System populates Situs  from the parent parcel");
		softAssert.assertTrue(apnValue.containsValue(relationshipTableDataHashMap.get("Source Parcel").get(3)),
				"SMAB-T2373: Validation that System populates Situs  from the parent parcel");
		
		
		objParcelsPage.openRelatedTabInParcelRecord("Ownership");
		HashMap<String, ArrayList<String>> ownershipTableDataHashMap = objParcelsPage.getParcelTableDataInHashMap("Property Ownerships");
		
		softAssert.assertEquals(ownershipTableDataHashMap.get("Owner").get(0),assesseeName,
				"SMAB-T2373: Validate that the Oownership record appears in Ownership tab");
		
		
		
		
		
		
		//Parent parcel status
		objMappingPage.globalSearchRecords(apn1);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"In Progress - To Be Expired",
				"SMAB-T2373: Validate the status of first parent parcel");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"In Progress - To Be Expired",
				"SMAB-T2373: Validate the PUC of first parent parcel");
		
		objParcelsPage.openRelatedTabInParcelRecord("Parcel Relationships");
		HashMap<String, ArrayList<String>> tableDataHashMap1 = objParcelsPage.getParcelTableDataInHashMap("Target Parcel Relationships");
		softAssert.assertEquals(tableDataHashMap1.get("Target Parcel").get(0),childAPNNumber,
				"SMAB-T2373: Validate that the Child parcel appears in Parcel Relationship tab");
		
		objMappingPage.globalSearchRecords(apn2);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"In Progress - To Be Expired",
				"SMAB-T2373: Validate the status of second parent parcel");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"In Progress - To Be Expired",
				"SMAB-T2373: Validate the PUC of second parent parcel");
		
		objParcelsPage.openRelatedTabInParcelRecord("Parcel Relationships");
		HashMap<String, ArrayList<String>> tableDataHashMap2 = objParcelsPage.getParcelTableDataInHashMap("Target Parcel Relationships");
		softAssert.assertEquals(tableDataHashMap2.get("Target Parcel").get(0),childAPNNumber,
				"SMAB-T2373: Validate that the Child parcel appears in Parcel Relationship tab");
		
		objMappingPage.globalSearchRecords(apn3);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"In Progress - To Be Expired",
				"SMAB-T2373: Validate the status of third parent parcel");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"In Progress - To Be Expired",
				"SMAB-T2373: Validate the PUC of third parent parcel");
		
		objParcelsPage.openRelatedTabInParcelRecord("Parcel Relationships");
		HashMap<String, ArrayList<String>> tableDataHashMap3 = objParcelsPage.getParcelTableDataInHashMap("Target Parcel Relationships");
		softAssert.assertEquals(tableDataHashMap3.get("Target Parcel").get(0),childAPNNumber,
				"SMAB-T2373: Validate that the Child parcel appears in Parcel Relationship tab");
		
		objMappingPage.globalSearchRecords(apn4);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"In Progress - To Be Expired",
				"SMAB-T2373: Validate the status of fourth parent parcel");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"In Progress - To Be Expired",
				"SMAB-T2373: Validate the PUC of fourth parent parcel");
		
		objParcelsPage.openRelatedTabInParcelRecord("Parcel Relationships");
		HashMap<String, ArrayList<String>> tableDataHashMap4 = objParcelsPage.getParcelTableDataInHashMap("Target Parcel Relationships");
		softAssert.assertEquals(tableDataHashMap4.get("Target Parcel").get(0),childAPNNumber,
				"SMAB-T2373: Validate that the Child parcel appears in Parcel Relationship tab");
		
		
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		
		
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
		softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Completed","SMAB-T1838:Verify user is able to submit the Work Item for approval");

		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		softAssert.assertEquals(objMappingPage.getLinkedParcelInWorkItem("0"), apn1,"SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");
		softAssert.assertEquals(objMappingPage.getLinkedParcelInWorkItem("1"), apn2,"SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");
		softAssert.assertEquals(objMappingPage.getLinkedParcelInWorkItem("2"), apn3,"SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");
		softAssert.assertEquals(objMappingPage.getLinkedParcelInWorkItem("3"), apn4,"SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");

		
		
		//Fetching work items generated 
		String queryWIs = "SELECT Name,Id FROM Work_Item__c Order By Name Desc Limit 2";
		HashMap<String, ArrayList<String>> responseWorkItem = salesforceAPI.select(queryWIs);
		String workItem1=responseWorkItem.get("Name").get(0);
		String workItem2=responseWorkItem.get("Name").get(1);
		
		objMappingPage.searchModule(WORK_ITEM);
		//objMappingPage.displayRecords("All");
		
		objMappingPage.globalSearchRecords(workItem1);
		softAssert.assertEquals(objMappingPage.getLinkedParcelInWorkItem("0"), childAPNNumber,"SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");
		
		objMappingPage.Click(objWorkItemHomePage.detailsTab);
		objMappingPage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Validating that  'Date' field gets automatically populated in the work item record
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type", "Information"),"New APN",
						"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action", "Information"),"Allocate Value",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status", "Information"),"In Pool",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Reference", "Information"),"",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		
		objMappingPage.globalSearchRecords(workItem2);
		softAssert.assertEquals(objMappingPage.getLinkedParcelInWorkItem("0"), childAPNNumber,"SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");
		
		objMappingPage.Click(objWorkItemHomePage.detailsTab);
		objMappingPage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Validating that  'Date' field gets automatically populated in the work item record
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type", "Information"),"New APN",
						"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action", "Information"),"Update Characteristics & Verify PUC",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status", "Information"),"In Pool",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Reference", "Information"),"",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		
		
		objMappingPage.searchModule(PARCELS);
		
		//Child parcel status
		objMappingPage.globalSearchRecords(childAPNNumber);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"Active",
				"SMAB-T2373: Validate the status of child parcel generated");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"00-VACANT LAND",
				"SMAB-T2373: Validate the PUC of child parcel generated");
		
		//Parent parcel status
		objMappingPage.globalSearchRecords(apn1);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"Retired",
				"SMAB-T2373: Validate the status of first parent parcel");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"99-RETIRED PARCEL",
				"SMAB-T2373: Validate the PUC of first parent parcel");
				
			
		objMappingPage.globalSearchRecords(apn2);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"Retired",
				"SMAB-T2373: Validate the status of second parent parcel");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"99-RETIRED PARCEL",
				"SMAB-T2373: Validate the PUC of second parent parcel");
		
		objMappingPage.globalSearchRecords(apn3);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"Retired",
				"SMAB-T2373: Validate the status of third parent parcel");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"99-RETIRED PARCEL",
				"SMAB-T2373: Validate the PUC of third parent parcel");
		
		objMappingPage.globalSearchRecords(apn4);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),"Retired",
				"SMAB-T2373: Validate the status of fourth parent parcel");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPUC, "Parcel Information"),"99-RETIRED PARCEL",
				"SMAB-T2373: Validate the PUC of fourth parent parcel");
		
		objWorkItemHomePage.logout();
		Thread.sleep(000);

		ReportLogger.INFO("Now logging in as RP Appraiser to validate that new WIs are accessible");
		objWorkItemHomePage.login(RP_APPRAISER);
		
		objMappingPage.searchModule(WORK_ITEM);
		//objMappingPage.displayRecords("All");
		
		objMappingPage.globalSearchRecords(workItem1);
		softAssert.assertEquals(objMappingPage.getLinkedParcelInWorkItem("0"), childAPNNumber,"SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");
		
		objMappingPage.Click(objWorkItemHomePage.detailsTab);
		objMappingPage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Validating that  'Date' field gets automatically populated in the work item record
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type", "Information"),"New APN",
						"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action", "Information"),"Allocate Value",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status", "Information"),"In Pool",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Reference", "Information"),"",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		
		objMappingPage.globalSearchRecords(workItem2);
		softAssert.assertEquals(objMappingPage.getLinkedParcelInWorkItem("0"), childAPNNumber,"SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");
		
		objMappingPage.Click(objWorkItemHomePage.detailsTab);
		objMappingPage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Validating that  'Date' field gets automatically populated in the work item record
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type", "Information"),"New APN",
						"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action", "Information"),"Update Characteristics & Verify PUC",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status", "Information"),"In Pool",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Reference", "Information"),"",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/");
		
	}
	
}
