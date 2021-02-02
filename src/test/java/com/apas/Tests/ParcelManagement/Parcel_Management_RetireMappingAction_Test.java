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

public class Parcel_Management_RetireMappingAction_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;

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
	 * This method is to Verify that User is able to view various error messages while perform a "Retire" mapping action from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2455,SMAB-T2457:Verify that User is able to view the various error message during Retire Action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_VerifyErrorMessagesInRetireMappingAction(String loginUser) throws Exception {
		
		//Fetching parcel that is Retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);
		
		//Fetching parcel that is In Progress - To Be Expired	
		String inProgressAPNValue = objMappingPage.fetchInProgressAPN();
		
		//Fetching parcels that are Active 
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Status__c='Active' limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);
		
		String activeParcelWithoutHyphen=apn2.replace("-","");
		String accessorMapParcel = apn1.replace("-", "").substring(0, 5);
		String expectedIndividualFieldMessage = "Complete this field.";
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.RETIRE_ACTION;
		Map<String, String> hashMapRetireeMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRetireAction");

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
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Validate the APN value
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn1,
				"SMAB-T2455: Validate the APN value in Parent APN field");

		// Step 6: Select the Retire value in Action field and validate that 'Are Taxes fully paid?' field isn't visible
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("Action"));
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.taxField),
				"SMAB-T2455: Validate that 'Are Taxes fully paid?' field is not visible");
		
		// Step 7: Change the value in Action field and validate that 'Are Taxes fully paid?' field is visible
		ReportLogger.INFO("Select 'Split' in the Action dropdown field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("Split"));
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getWebElementWithLabel(objMappingPage.taxesPaidDropDownLabel)),
						"SMAB-T2455: Validate that 'Are Taxes fully paid?' field is visible");
		
		// Step 8: Select the Retire value in Action field and validate that 'Are Taxes fully paid?' field isn't visible
		ReportLogger.INFO("Select 'Retire' in the Action dropdown field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("Action"));
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.taxField),
				"SMAB-T2455: Validate that 'Are Taxes fully paid?' field is not visible");
		
		// Step 9: Change the value in Action field and validate that 'Are Taxes fully paid?' field is visible
		ReportLogger.INFO("Select 'Combine' in the Action dropdown field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("Combine"));
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getWebElementWithLabel(objMappingPage.taxesPaidDropDownLabel)),
						"SMAB-T2455: Validate that 'Are Taxes fully paid?' field is visible");
		
		// Step 10: Select the Retire value in Action field and validate that 'Are Taxes fully paid?' field isn't visible
		ReportLogger.INFO("Select 'Retire' in the Action dropdown field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("Action"));
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.taxField),
				"SMAB-T2455: Validate that 'Are Taxes fully paid?' field is not visible");
		
		// Step 11: Change the value in Action field and validate that 'Are Taxes fully paid?' field is visible
		ReportLogger.INFO("Select 'Many To Many' in the Action dropdown field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("ManyToMany"));
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getWebElementWithLabel(objMappingPage.taxesPaidDropDownLabel)),
						"SMAB-T2455: Validate that 'Are Taxes fully paid?' field is visible");
		
		// Step 12: Select the Retire value in Action field and validate that 'Are Taxes fully paid?' field isn't visible
		ReportLogger.INFO("Select 'Retire' in the Action dropdown field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("Action"));
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.taxField),
				"SMAB-T2455: Validate that 'Are Taxes fully paid?' field is not visible");
		
		// Step 13: Change the value in Action field and validate that 'Are Taxes fully paid?' field is visible
		ReportLogger.INFO("Select 'One To One' in the Action dropdown field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("OneToOne"));
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getWebElementWithLabel(objMappingPage.taxesPaidDropDownLabel)),
						"SMAB-T2455: Validate that 'Are Taxes fully paid?' field is visible");
		
		// Step 14: Select the Retire value in Action field and validate that 'Are Taxes fully paid?' field isn't visible
		ReportLogger.INFO("Select 'Retire' in the Action dropdown field");
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("Action"));
		softAssert.assertTrue(!objMappingPage.verifyElementExists(objMappingPage.taxField),
				"SMAB-T2455: Validate that 'Are Taxes fully paid?' field is not visible");
		
		//Step 15: Validate the reason code and assessor's map fields are auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2455: Validate that value of 'Reason Code' field is populated from the parent parcel work item");
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.assessorMapLabel)).replace("-", ""),accessorMapParcel,
				"SMAB-T2455: Validate that value of 'Assessor's Map' field is the first 5 digits of the parent parcel");

		//Step 16: Validate that Reason CODE is a mandatory field
		ReportLogger.INFO("Remove the value from Reason Code field and click Retire button");
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), "");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		softAssert.assertEquals(objMappingPage.getIndividualFieldErrorMessage("Reason Code"),expectedIndividualFieldMessage,
				"SMAB-T2455: Validate that 'Reason Code' is a mandatory field");
		
		//Step 17: Validate that proper error message is displayed if parent parcel is retired
		ReportLogger.INFO("Enter the Retired parcel in Parent APN field");
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), hashMapRetireeMappingData.get("Reason code"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2457: Validate that proper error message is displayed if parent parcel is retired");
		
		//Step 18: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Retire Action cannot be completed with Retired parcel");
		objMappingPage.enter(objMappingPage.commentsTextBoxLabel, hashMapRetireeMappingData.get("Comments"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2457: Validate that user is not able to perform Retire action with error message on the screen");

		//Step 19: Validate that proper error message is displayed if parent parcel is in progress
		ReportLogger.INFO("Enter the In Progress parcel in Parent APN field");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2457: Validate that proper error message is displayed if parent parcel is in progress status");
		
		//Step 20: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Retire Action cannot be completed with In Progress parcel");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2457: Validate that user is not able to perform Retire action with error message on the screen");
		
		//Step 21: Validate that User should be allowed to enter the 9 digit APN without the - sign
		ReportLogger.INFO("Enter the parcel number without '-' in parent parcel field");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,activeParcelWithoutHyphen);
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn2,
				"SMAB-T2455: Validate that User is able to enter 9 digit parent APN without the \"-\"");

		//Step 22: Validate that User is able to perform Retire action
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel (s) have been successfully retired!",
				"SMAB-T2455: Validate that User is able to perform Retire action for one active parcel");
		
		//Step 23: Validate that the status and PUC of the parcel is updated to Retired
		driver.switchTo().window(parentWindow);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("PUC"),"99-RETIRED PARCEL",
				"SMAB-T2455 : Verify PUC of the parcel is updated after Retire action is completed");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status"),"Retired",
				"SMAB-T2455 : Verify Status of the parcel is updated after Retire action is completed");
		  
		
		objMappingPage.logout();

	}
	
	/**
	 * This method is to Verify that User is able to perform a "Retire" mapping action for a Parcels (Active) of type Non Condo, Condo and Others from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2456:Verify that User is able to perform Retire Action for more than one active parcels", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"smoke","regression","parcel_management" })
	public void ParcelManagement_VerifyRetireMappingActionForMoreThanOneActiveParcels(String loginUser) throws Exception {
		
		//Fetching Active General parcel 
		String queryAPN1 = "Select name,ID  From Parcel__c where name like '0%' AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails1 = salesforceAPI.select(queryAPN1);
		String apn1=responseAPNDetails1.get("Name").get(0);
		
		//Fetching Active Condo parcel 
		String queryAPN2 = "Select name,ID  From Parcel__c where name like '100%' AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails2 = salesforceAPI.select(queryAPN2);
		String apn2=responseAPNDetails2.get("Name").get(0);
		
		//Fetching Active Mobile home parcel
		String queryAPN3 = "Select name,ID  From Parcel__c where name like '134%' AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails3 = salesforceAPI.select(queryAPN3);
		String apn3=responseAPNDetails3.get("Name").get(0);
		
		String concatenateAPN = apn1+","+apn2+","+apn3;
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.RETIRE_ACTION;
		Map<String, String> hashMapRetireeMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRetireAction");

		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform Retire Action
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn2);

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
		
		// Step 5: Update the Parent APN field and add more parcel#
		ReportLogger.INFO("Add two more parcels in Parent APN field :: " + apn1 + ", " + apn3);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("Action"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		
		// Step 6: Enter the other values and perform the Retire action
		ReportLogger.INFO("Add/Update the Comments and Reason Code fields");
		objMappingPage.enter(objMappingPage.commentsTextBoxLabel, hashMapRetireeMappingData.get("Comments"));
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), "For Testing");
		
		ReportLogger.INFO("Perform the Retire action");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),"Parcel (s) have been successfully retired!",
				"SMAB-T2456: Validate that User is able to perform Retire action for more than one active parcels");
		
		//Step 7: Validate that the status and PUC of the parcels is updated to Retired
		driver.switchTo().window(parentWindow);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("PUC"),"99-RETIRED PARCEL",
				"SMAB-T2456 : Verify PUC of the "+apn1+" is updated after Retire action is completed");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status"),"Retired",
				"SMAB-T2456 : Verify Status of the "+apn1+" is updated after Retire action is completed");
		  
		objMappingPage.globalSearchRecords(apn2);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("PUC"),"99-RETIRED PARCEL",
				"SMAB-T2456 : Verify PUC of the "+apn2+" is updated after Retire action is completed");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status"),"Retired",
				"SMAB-T2456 : Verify Status of the "+apn2+" is updated after Retire action is completed");
		
		objMappingPage.globalSearchRecords(apn3);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("PUC"),"99-RETIRED PARCEL",
				"SMAB-T2456 : Verify PUC of the "+apn3+" is updated after Retire action is completed");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status"),"Retired",
				"SMAB-T2456 : Verify Status of the "+apn3+" is updated after Retire action is completed");
		
		
		objMappingPage.logout();

	}

}
