package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
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
	@Test(description = "SMAB-T2455,SMAB-T2457,SMAB-T2672:Verify that User is able to view the various error message during Retire Action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","RetireAction" })

	public void ParcelManagement_VerifyErrorMessagesInRetireMappingAction(String loginUser) throws Exception {
		
		//Fetching parcel that is Retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);
		
		//Fetching parcel that is In Progress - To Be Expired	
		String inProgressAPNValue = objMappingPage.fetchInProgressAPN();
		
		//Fetching parcels that are Active 
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') AND Status__c='Active' limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);
		
		//Deleting charecteristic record from parcels
		
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn1);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn2);
		
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
		String parentParcelPUC = objWorkItemHomePage.getFieldValueFromAPAS("PUC", "Parcel Information");

		// Step 3: Creating Manual work item for the Active Parcel 
		String workItem = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		Thread.sleep(2000); //Allows Mapping screen to load completely

		// Step 5: Validate the APN value
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel)),apn1,
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
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2457: Validate that proper error message is displayed if parent parcel is retired");
		
		//Step 18: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Retire Action cannot be completed with Retired parcel");
		objMappingPage.enter(objMappingPage.commentsTextBoxLabel, hashMapRetireeMappingData.get("Comments"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2457: Validate that user is not able to perform Retire action with error message on the screen");

		//Step 19: Validate that proper error message is displayed if parent parcel is in progress
		ReportLogger.INFO("Enter the In Progress parcel in Parent APN field");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2457: Validate that proper error message is displayed if parent parcel is in progress status");
		
		//Step 20: Validate that user is not able to move to the next screen
		ReportLogger.INFO("Retire Action cannot be completed with In Progress parcel");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2457: Validate that user is not able to perform Retire action with error message on the screen");
		
		//Step 21: Validate that User should be allowed to enter the 9 digit APN without the - sign
		ReportLogger.INFO("Enter the parcel number without '-' in parent parcel field");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,activeParcelWithoutHyphen);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel)),apn2,
				"SMAB-T2455: Validate that User is able to enter 9 digit parent APN without the \"-\"");

		//Step 22: Validate that User is able to perform Retire action
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel "+apn1+" is pending verification from the supervisor in order to be retired.",
				"SMAB-T2455: Validate that User is able to perform Retire action for one active parcel");
		
		//Step 23: Validate that the status and PUC of the parcel is updated to Retired
		driver.switchTo().window(parentWindow);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("PUC"),parentParcelPUC,
				"SMAB-T2455 : Verify PUC of the parcel is not updated after Retire action is completed");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status"),"In Progress - To Be Expired",
				"SMAB-T2455 : Verify Status of the parcel is updated after Retire action is completed");
		 
		//Step 24: Validate that the work item is automatically closed after the mapping action is completed
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.displayRecords("All");
		objMappingPage.globalSearchRecords(workItem);
		
		/**Post implementation of SMAB-9063, now, WI will not get closed automatically after the Retire action is completed.
		   Hence, commenting the below code for now**/
		
		/**softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Completed","SMAB-T2672:Verify the status of Work Item is automatically updated to 'Completed'");**/
		
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeVisible(10,objWorkItemHomePage.linkedItemsRecord);
		softAssert.assertEquals(objMappingPage.getGridDataInHashMap(1).get("APN").get(0), apn1,
				"SMAB-T2672: Validate that parent parcel is displayed in the Linked Items of WI");
		
		objMappingPage.logout();

	}
	
	/**
	 * This method is to Verify that User is able to perform a "Retire" mapping action for a Parcels (Active) of type Non Condo, Condo and Others from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2456,SMAB-T2671:Verify that User is able to perform Retire Action for more than one active parcels", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Smoke","Regression","ParcelManagement","RetireAction" })
	public void ParcelManagement_VerifyRetireMappingActionForMoreThanOneActiveParcels(String loginUser) throws Exception {
		
		//Fetching Active General parcel 
		String queryAPN1 = "Select Name, ID From Parcel__c where name like '0%' AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails1 = salesforceAPI.select(queryAPN1);
		String apn1=responseAPNDetails1.get("Name").get(0);
		
		//Fetching Active Condo parcel 
		String queryAPN2 = "Select Name, ID From Parcel__c where name like '100%' AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails2 = salesforceAPI.select(queryAPN2);
		String apn2=responseAPNDetails2.get("Name").get(0);
		
		//Fetching Active Mobile home parcel
		String apn3 = "";
		String createNewParcel = testdata.MANUAL_PARCEL_CREATION_DATA;
		Map<String, String> hashMapCreateNewParcel = objUtil.generateMapFromJsonFile(createNewParcel,
				"DataToCreateParcelStartingWith134");
		String apnStartingWith134 = hashMapCreateNewParcel.get("APN");
		String parcelNumberStartingWith134 = hashMapCreateNewParcel.get("Parcel Number");
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id"
				+ "  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c "
				+ "where Status__c='Active') limit 1");
		String PUC = responsePUCDetails.get("Name").get(0);
				
		String queryAPN3 = "Select Name, ID From Parcel__c where name like '134%' AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails3 = salesforceAPI.select(queryAPN3);
		
		if (responseAPNDetails3 != null) {
			apn3=responseAPNDetails3.get("Name").get(0);
			salesforceAPI.update("Parcel__c",responseAPNDetails3.get("Id").get(0),"Status__c","Active");
		}
		else {
			objMappingPage.login(users.SYSTEM_ADMIN);
			apn3 = objParcelsPage.createNewParcel(apnStartingWith134,parcelNumberStartingWith134,PUC);
	        objWorkItemHomePage.logout();
	        Thread.sleep(5000);	
		}
		
		//Add the parcels in a Hash Map for validations later
		Map<String,String> apnValue = new HashMap<String,String>(); 
		apnValue.put("APN1", apn1); 
		apnValue.put("APN2", apn2); 
		apnValue.put("APN3", apn3); 
		
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
		String workItem = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4: Clicking the details tab for the work item newly created and clicking on Related Action Link
		ReportLogger.INFO("Click on the Related Action link");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		Thread.sleep(2000); //Allows Mapping screen to load completely
		
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
		softAssert.assertContains(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),"is pending verification from the supervisor in order to be retired.",
				"SMAB-T2456: Validate that User is able to perform Retire action for more than one active parcels");
		
		//Step 7: Validate that the status and PUC of the parcels is updated to Retired
		driver.switchTo().window(parentWindow);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);
		softAssert.assertTrue(!objMappingPage.getFieldValueFromAPAS("PUC").equals("99-RETIRED PARCEL"),
				"SMAB-T2456 : Verify PUC of the "+apn1+" is not updated after Retire action is completed");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status"),"In Progress - To Be Expired",
				"SMAB-T2456 : Verify Status of the "+apn1+" is updated after Retire action is completed");
		  
		objMappingPage.globalSearchRecords(apn2);
		softAssert.assertTrue(!objMappingPage.getFieldValueFromAPAS("PUC").equals("99-RETIRED PARCEL"),
				"SMAB-T2456 : Verify PUC of the "+apn2+" is not updated after Retire action is completed");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status"),"In Progress - To Be Expired",
				"SMAB-T2456 : Verify Status of the "+apn2+" is updated after Retire action is completed");
		
		objMappingPage.globalSearchRecords(apn3);
		softAssert.assertTrue(!objMappingPage.getFieldValueFromAPAS("PUC").equals("99-RETIRED PARCEL"),
				"SMAB-T2456 : Verify PUC of the "+apn3+" is not updated after Retire action is completed");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Status"),"In Progress - To Be Expired",
				"SMAB-T2456 : Verify Status of the "+apn3+" is updated after Retire action is completed");
		
		//Step 8: Validate that the work item is automatically closed after the mapping action is completed
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.displayRecords("All");
		objMappingPage.globalSearchRecords(workItem);
		driver.navigate().refresh();
		Thread.sleep(5000);
		
		/**Post implementation of SMAB-9063, now, WI will not get closed automatically after the Retire action is completed.
		   Hence, commenting the below code for now**/
		
		/**softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Completed","SMAB-T2671:Verify the status of Work Item is automatically updated to 'Completed'");**/
		
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
				"SMAB-T2671: Validate that first Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("1")),
				"SMAB-T2671: Validate that second Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("2")),
				"SMAB-T2671: Validate that third Parent APN is displayed in the linked item");
				
		objMappingPage.logout();

	}

	@Test(description = "SMAB-T2833,SMAB-T3622,SMAB-T3634:Parcel Management- Verify that User is able to perform retire action from mapping actions tab for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","RetireAction" })
	public void ParcelManagement_Retire_MappingAction_IndependentMappingAction(String loginUser) throws Exception {

		//Fetching Active General parcel 
		String queryAPN1 = "Select Name, ID From Parcel__c where name like '0%' AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails1 = salesforceAPI.select(queryAPN1);
		String apn=responseAPNDetails1.get("Name").get(0);
		
		String mappingActionCreationData = testdata.RETIRE_ACTION;
		Map<String, String> hashMapRetireeMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRetireAction");
				
		// Step1: Login to the APAS application using the credentials passed through dataprovider (MAPPING_STAFF User)
		objMappingPage.login(loginUser);
		Thread.sleep(7000);
		objMappingPage.closeDefaultOpenTabs();

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule("APAS");
		objMappingPage.searchModule("Mapping Action");
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);

		//Step 3: Selecting Action as 'perform parcel retire' 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireeMappingData.get("Action"));

		//Step 4: filling all fields in mapping action screen
		objMappingPage.enter(objMappingPage.commentsTextBoxLabel, hashMapRetireeMappingData.get("Comments"));
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), "For Testing");
	
		//Step 5: Click retire Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);
		softAssert.assertContains(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),"is pending verification from the supervisor in order to be retired.",
				"SMAB-T2833,SMAB-T3622: Validate that User is able to perform Retire action from mapping actions tab");
		
		//Step 6: Navigating  to the independent mapping action WI that would have been created after performing retire action 
		String workItemId= objWorkItemHomePage.getWorkItemIDFromParcelOnWorkbench(apn);
		String query = "SELECT Name FROM Work_Item__c where id = '"+ workItemId + "'";
		HashMap<String, ArrayList<String>> responseDetails = salesforceAPI.select(query);
		String workItem=responseDetails.get("Name").get(0);
		
		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type","Information"), "Mapping",
				"SMAB-T2833: Validation that  A new WI of type Mapping is created after performing retire from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action","Information"), "Independent Mapping Action",
				"SMAB-T2833: Validation that  A new WI of action Independent Mapping Action is created after performing retire from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(DateUtil.getCurrentDate("MM/dd/yyyy")), "SMAB-T2831: Validation that 'Date' fields is equal to date when this WI was created");
		
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

		// Checking the status of parcel after WI closed
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		String Status = objMappingPage.getFieldValueFromAPAS("Status", "Parcel Information");
		softAssert.assertEquals(Status, "Retired", "SMAB-T3622: Verify Status of Parcel:" + apn);

		objWorkItemHomePage.logout();
	}
	
	/**
	 * This method is to Parcel Management- Verify that User is able to view other parcels getting added in the associated work item after Retire Parcel Action is completed
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2670,SMAB-T2764,SMAB-T2910,SMAB-T3473,SMAB-T2909,SMAB-T3474,SMAB-T3475:Parcel Management- Verify that User is able to view other parcels getting added in the associated work item after Retire Parcel Action is completed", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement","RetireAction" })
	public void ParcelManagement_verifyLinkedAPN_OnDiffMapBookAndMapPageParentParcelAdditions_RetireParcelMappingAction(String loginUser)
			throws Exception {

		// Fetching parcel that is Active
		String queryAPNValue = "select Name from Parcel__c where Status__c='Active' AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
		String apn1 = responseAPNDetails.get("Name").get(0);

		// Fetching parcels that are Active with different map book and map page
		String mapBookForAPN1 = apn1.split("-")[0];
		String mapPageForAPN1 = apn1.split("-")[1];
		queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE (Not Name like '"
				+ mapBookForAPN1 + "%') and (Not Name like '" + mapBookForAPN1 + "-" + mapPageForAPN1
				+ "%') and Status__c = 'Active' AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO')limit 2";
		HashMap<String, ArrayList<String>> responseAPN2Details = salesforceAPI.select(queryAPNValue);
		String apn2 = responseAPN2Details.get("Name").get(0);
		String apn3 = responseAPN2Details.get("Name").get(1);

		String concatenateAPNWithDifferentMapBookMapPage = apn2 + "," + apn3;

		// Add the parcels in a Hash Map for validations later
		Map<String, String> apnValue = new HashMap<String, String>();
		apnValue.put("APN1", apn1);
		apnValue.put("APN2", apn2);
		apnValue.put("APN3", apn3);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.RETIRE_ACTION;
		Map<String, String> hashMapRetireParcelActionMappingData = objUtil
				.generateMapFromJsonFile(mappingActionCreationData, "DataToPerformRetireAction");

		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn1);

		// Step 3: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Selecting Action as 'perform Retire Parcel'
		String mappingActionWindow = driver.getWindowHandle();
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		ReportLogger.INFO("Add a parcel with different Map Book and Map Page in Parent APN field :: "
				+ concatenateAPNWithDifferentMapBookMapPage);
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, concatenateAPNWithDifferentMapBookMapPage);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		// Step 6: filling all fields in mapping action screen
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRetireParcelActionMappingData.get("Action"));
		objMappingPage.enter(objMappingPage.commentsTextBoxLabel, hashMapRetireParcelActionMappingData.get("Comments"));
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), "For Testing");

		// Step 7: Verify Linked Items on WI before Retire Parcel Mapping Action is
		// performed
		ReportLogger.INFO(
				"validate that new APNs added are not lnked to WI before Retire Parcel Mapping Action is performed");
		driver.switchTo().window(parentWindow);
		objMappingPage.waitUntilPageisReady(driver);

		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh();
		objMappingPage.waitForElementToBeVisible(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);

		softAssert.assertEquals(1, objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI, 10).size(),
				"SMAB-T2670: Validate that only 1 APN is linked to Work Item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
				"SMAB-T2670: Validate that first Parent APN is displayed in the linked item");

		// Step 8: Click Retire Parcel Button
		driver.switchTo().window(mappingActionWindow);
		objMappingPage.waitUntilPageisReady(driver);
		ReportLogger.INFO("Perform the Retire action");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.retireButton));
		softAssert.assertContains(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),
				"pending verification from the supervisor in order to be retired.",
				"SMAB-T2670: Validate that User is able to perform Retire action for more than one active parcels");

		// Step 9: Submit the WI for approval and validate the linked parcels to the WI
		driver.switchTo().window(parentWindow);
		objMappingPage.waitUntilPageisReady(driver);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh();
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),
				"Submitted for Approval", "SMAB-T2670:Verify user is able to submit the Work Item for approval");

		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);

		softAssert.assertEquals(2, objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI, 10).size(),
				"SMAB-T2670,SMAB-T2764,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that 2 APNs are linked to Work Item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
				"SMAB-T2670,SMAB-T2764,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that first Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("1")),
				"SMAB-T2670,SMAB-T2764,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that second Parent APN is displayed in the linked item");
		objMappingPage.Click(objWorkItemHomePage.detailsTab);
		String referenceURl= objMappingPage.getFieldValueFromAPAS("Navigation Url", "Reference Data Details");
		softAssert.assertTrue(referenceURl.contains(concatenateAPNWithDifferentMapBookMapPage),
				"SMAB-T2910,SMAB-T3473: Validate that Parent APNs are present in the reference Link");
		
		objWorkItemHomePage.logout();
		Thread.sleep(5000);

		// Step 10: Login from Mapping Supervisor to approve the WI
		ReportLogger.INFO(
				"Now logging in as Mapping Supervisor to approve the work item and validate that new WIs are accessible");
		objWorkItemHomePage.login(MAPPING_SUPERVISOR);

		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		objWorkItemHomePage.completeWorkItem();
		softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),
				"Completed", "SMAB-T2670:Verify user is able to complete the Work Item");

		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);

		softAssert.assertEquals(2, objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI, 10).size(),
				"SMAB-T2670,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that 2 APNs are linked to Work Item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
				"SMAB-T2670,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that first Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("1")),
				"SMAB-T2670,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that second Parent APN is displayed in the linked item");
		objMappingPage.Click(objWorkItemHomePage.detailsTab);
		referenceURl= objMappingPage.getFieldValueFromAPAS("Navigation Url", "Reference Data Details");
		softAssert.assertTrue(referenceURl.contains(concatenateAPNWithDifferentMapBookMapPage),
				"SMAB-T2910,SMAB-T3473: Validate that Parent APNs are present in the reference Link");

		objWorkItemHomePage.logout();
	}

}