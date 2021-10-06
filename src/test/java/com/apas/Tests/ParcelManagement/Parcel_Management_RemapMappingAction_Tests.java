package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.CIOTransferPage;
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

public class Parcel_Management_RemapMappingAction_Tests extends TestBase implements testdata, modules, users {
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

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objtransfer=new CIOTransferPage(driver);

	}

	@Test(description = "SMAB-T2490,SMAB-T2536:Verify that User is able to perform a Remap mapping action for a Parcel from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyRemapMappingActionForMultipleParcels(String loginUser) throws Exception {
		ArrayList<String> APNs=objMappingPage.fetchActiveAPN(2);
		String activeParcelToPerformMapping=APNs.get(0);
		String activeParcelToPerformMapping2=APNs.get(1);

		// fetching  parcel that is retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);


		String mappingActionCreationData = testdata.REMAP_MAPPING_ACTION;
		Map<String, String> remapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform remap mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeParcelToPerformMapping);

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Validation that proper error message is displayed if one of the parent parcel is retired
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,remapMappingData.get("Action"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue + ","+ activeParcelToPerformMapping2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2490: Validation that proper error message is displayed if parent parcel is retired");


		//Step 6: entering data in form for remap 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,activeParcelToPerformMapping + ","+ activeParcelToPerformMapping2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.remapActionForm(remapMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		gridDataHashMap =objMappingPage.getGridDataInHashMap();	

		//Step 7: Verifying whether the Remap action is performed for multiple parcels and total number of parcel generated are equal to number of parent parcels
		softAssert.assertEquals(gridDataHashMap.get("APN").size(),2,
				"SMAB-T2536: Verify that after remap total number of parcel generated are equal to number of parent parcels");
		driver.switchTo().window(parentWindow);
	
		//Step 8: Logout
		objWorkItemHomePage.logout();

	}

	/**
	 * This method is to Verify that User is able to perform a "Remap" mapping action for a Parcel from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3051,SMAB-T2490,SMAB-T2493,SMAB-T2532,SMAB-T2535,SMAB-T2531,SMAB-T2533:Verify that User is able to perform a Remap mapping action for a Parcel from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyRemapMappingAction(String loginUser) throws Exception {
		String activeParcelToPerformMapping=objMappingPage.fetchActiveAPN();
		String activeParcelWithoutHyphen=activeParcelToPerformMapping.replace("-","");

		// fetching  parcel that is retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);

		// fetching  parcel that is In progress	
		String inProgressAPNValue;
		queryAPNValue = "select Name from Parcel__c where Status__c='In Progress - To Be Expired' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		response = salesforceAPI.select(queryAPNValue);
		if(!response.isEmpty())
			inProgressAPNValue= response.get("Name").get(0);
		else
		{
			inProgressAPNValue= objMappingPage.fetchActiveAPN();
			HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI.select(
					"SELECT Name,id  FROM PUC_Code__c where id in "
					+ "(Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

			jsonObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
			jsonObject.put("Status__c","In Progress - To Be Expired");
			salesforceAPI.update("Parcel__c",objMappingPage.fetchActiveAPN(),jsonObject);
		}


		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
		Map<String, String> remapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");

		String workItemCreationData =testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform remap mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeParcelToPerformMapping);

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Validation that proper error message is displayed if parent parcel is retired
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,remapMappingData.get("Action"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2532: Validation that proper error message is displayed if parent parcel is retired");

		//Step 6: Validation that proper error message is displayed if parent parcel is in progress
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2532: Validation that proper error message is displayed if parent parcel is in progress status");

		//Step 7: Verifying that User should be allowed to enter the 9 digit APN without the \"-\" in Parent APN field
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, activeParcelWithoutHyphen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		//Step 8: Validating that reason code field is auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2490: Validation that reason code field is auto populated from parent parcel work item");

		//Step 9: Verifying that proper error message is displayed if alphanumeric value  is entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "123-45*-78&");

		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.firstCondoTextBoxLabel),
				"SMAB-T3373: Validation that First Condo Text Box Feild should be visible to the user.");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2493: Validation that proper error message is displayed if alphanumeric value  is entered in First non condo parcel field");

		//Step 10:Verifying that proper error message is displayed if less than 9 digits are entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "123-456-78");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));	
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2493: Validation that proper error message is displayed if less than 9 disgits are entered in First non condo parcel field");

		//Step 11:Verifying that proper error message is displayed if parcel starting with 100 is entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "100-456-789");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));	
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- Non Condo Parcel Number cannot start with 100, Please enter valid Parcel Number",
				"SMAB-T2531: Validation that proper error message is displayed if parcel starting with 100 is entered in First non condo parcel field");

		/* APN Starting with 134 are allowed in non condo field
		 * //Step 12:Verifying that proper error message is displayed if parcel starting
		 * with 134 is entered in First non condo parcel field
		 * objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,
		 * "134-456-789");
		 * objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.
		 * reasonCodeTextBoxLabel));
		 * softAssert.assertEquals(objMappingPage.getErrorMessage()
		 * ,"Non Condo Parcel Number cannot start with 100 or 134, Please enter valid Parcel Number"
		 * ,
		 * "SMAB-T2531: Validation that proper error message is displayed if parcel starting with 134 is entered in First non condo parcel field"
		 * );
		 */
		//Step 13: Verifying that User should be allowed to enter the 9 digit APN without the \"-\" in First Non Condo Parcel Field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, activeParcelWithoutHyphen);
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));	
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.firstNonCondoTextBoxLabel),"value"),activeParcelToPerformMapping,
				"SMAB-T2535, SMAB-T3051: Validation that User should be allowed to enter the 9 digit APN without the \"-\" in First Non Condo Parcel Field");

		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, " ");
		//Step 14: entering data in form for remap 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,activeParcelToPerformMapping);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.remapActionForm(remapMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 15: Verify that APNs generated must be 9-digits and should end in '0'
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		String childAPNComponents[] = childAPNNumber.split("-");
		softAssert.assertEquals(childAPNComponents.length,3,
				"SMAB-T2533: Validation that child APN number contains 3 parts: map book,map page,parcel number");
		softAssert.assertEquals(childAPNComponents[0].length(),3,
				"SMAB-T2533: Validation that MAP BOOK of child parcels contains 3 digits");
		softAssert.assertEquals(childAPNComponents[1].length(),3,
				"SMAB-T2533: Validation that MAP page of child parcels contains 3 digits");
		softAssert.assertEquals(childAPNComponents[2].length(),3,
				"SMAB-T2533: Validation that parcel number of child parcels contains 3 digits");
		softAssert.assertTrue(childAPNNumber.endsWith("0"),
				"SMAB-T2533: Validation that child APN number ends with 0");

		gridDataHashMap =objMappingPage.getGridDataInHashMap();	
		driver.switchTo().window(parentWindow);

		//Step 16: Logout
		objWorkItemHomePage.logout();

	}


	/*
	 * This method is to verify that APN exists in the system
	 * @param -Login user
	 * @throws-Exception
	 * 
	 */


	@Test(description = "SMAB-T2483,SMAB-T2691,SMAB-T2692: Verify APN entered must exist in APAS,And no dupicates Apn allowed ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
			,groups = {"Regression","ParcelManagement"},enabled =true)
	public void ParcelManagment_Verify_APN_EnteredMust_Exist_In_Apas_RemapMappingAction(String loginUser) throws Exception
	{
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String apn1=responseAPNDetails.get("Name").get(1);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
		Map<String, String> RemapParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");


		//  user login to APAS application
		objMappingPage.login(loginUser);			
		//  Opening the PARCELS page  and searching the  parcel to perform remap mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);		
		//  Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);			

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);														
		String parentWindow=driver.getWindowHandle();
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);

		//  User enters into mapping page									
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,RemapParcelMappingData.get("Action"));		          
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));				
		// Step 6: User enters new APN that alerady exists in the system		
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));								
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn1,
				"SMAB-T2483: Validate the APN value in Parent APN field");								 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,RemapParcelMappingData.get("Action"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, "");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		// validating error msg if no parent Apn is provided
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- The Parent APN cannot be blank.", "SMAB-T2692: validating error message of no parent  APN");

		// Step 7 : User enters an invalid APN to check
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, "000000000");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- The following Parent APNs do not exist : 000-000-000", "SMAB-T2483: validating error message of invalid APN");
		//Entering multiple same Apns and validating error messages
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, apn+","+apn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- The Parent APN can not have duplicate APNs.","SMAB-T2691: Verify that when multiple same APN's are added in parent APN field, it should throw error or show the warning");

		driver.switchTo().window(parentWindow);
		objMappingPage.logout();
	}


	/**
	 * 
	 *  Verify that when multiple parent parcels are entered, if a space is entered or not after a comma, the system should format the parcel as expected,and apn should be 9 digits only.
	 * @param loginUser
	 * @throws Exception
	 */

	@Test(description = "SMAB-T2625,SMAB-T2628:  Verify that when multiple parent parcels are entered, if a space is entered or not after a comma, the system should format the parcel as expected,And Apn should be 9 digits ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
			,groups = {"Regression","ParcelManagement"},enabled=true)
	public void ParcelManagment_Verify_Multiple_Parent_Parcels_Indentation_RemapMapping(String loginUser) throws Exception
	{
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String apn1=responseAPNDetails.get("Name").get(1);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
		Map<String, String> RemapParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");	
		String combineApn=apn+","+apn1;
		String apnlessThan9 = apn.substring(0, 10);
		String validApn = apn.replace("-", "");
		String invalidApn = apnlessThan9.replace("-", "");

		// user login to APAS application
		objMappingPage.login(loginUser);		
		//  Opening the PARCELS page  and searching the  parcel to perform remap mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		//  Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		//Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);		                           							
		String parentWindow=driver.getWindowHandle();
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		//  User enters into mapping page							
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,RemapParcelMappingData.get("Action"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));								
		//  Entering combined APN'S
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,combineApn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));				
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn+" , "+apn1,
				"SMAB-T2625:  Verify that when multiple parent parcels are entered, if a space is entered or not after a comma, the system should format the parcel as expected.");	
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));    	
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apnlessThan9);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		//Validating APN of less than 9 digits
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+apnlessThan9, "T-SMAB2628 validating error message of invalid APN less than 9 digits");			  
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));		    	
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,invalidApn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));	
		//Validating invalid apn without -
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+apnlessThan9, "T-SMAB2628 validating error message of invalid APN less than 9 digits");		  
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));		
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,validApn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));									
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		//validating valid apn of 9 digits without -
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn,
				"SMAB-T2628: Validate the APN value is valid"); 
		driver.switchTo().window(parentWindow);
		objMappingPage.logout();

	}


	/**Verify APN entered must not have special character and validate apn without spaces
	 * 
	 * @param loginUser
	 * @throws Exception
	 */


	@Test(description = "SMAB-T2629,SMAB-T2630 : Verify APN entered must not have special character ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
			,groups = {"Regression","ParcelManagement"},enabled = true)
	public void ParcelManagment_VerifyAPN_Entered_MustNotHave_SpcChar_RemappingAction(String loginUser) throws Exception
	{
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");  		
		String invalidApn =  apn.substring(0, 10)+"$";
		String invalidApn2 =  apn.substring(0, 10)+".";
		String spacedApn = apn.replace("-", " ");  	
		// user login to APAS application
		objMappingPage.login(loginUser);  	
		//  Opening the PARCELS page  and searching the  parcel to perform remap mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);   		
		//  Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);   				
		//Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel); 			  
		String parentWindow=driver.getWindowHandle();
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);  				
		//  User enters into mapping page	  				
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));   
		//	objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));    
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, invalidApn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+invalidApn, "T-SMAB2629 validating error message of invalid APN");			  
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));				
		//  User enters new APN that alerady exists in the system		
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,spacedApn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));					
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));						
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn,
				"SMAB-T2630: Validate the APN value in Parent APN field is without spaces");					 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));						
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		// Entering . at the end of 8 digit apn
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, invalidApn2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"- The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+invalidApn2, "T-SMAB2629 validating error message of invalid APN");			  	


		driver.switchTo().window(parentWindow);
		objMappingPage.logout();


	}

	/**
	 * on mapping screen(second screen) when manually update the apn and enter the same existing as generated for another child parcel and try to finalize the action of multiple parcels generated error should be thrownS
	 * 
	 * @param loginUser
	 * @throws Exception
	 */



	@Test(description = "SMAB-T2634: on mapping screen(second screen) when manually update the apn and enter the same existing as generated for another child parcel and try to finalize the action of multiple parcels generated error should be thrownS ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
			,groups = {"Regression","ParcelManagement"},enabled =true)
	public void ParcelManagment_Verify_Remap_With_Duplicate_Apns_RemappingAction(String loginUser) throws Exception
	{
		String queryAPN = "select name from parcel__c where status__c ='Active' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Name not like '134%' limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String apn1=responseAPNDetails.get("Name").get(1);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.REMAP_MAPPING_ACTION;
		Map<String, String> RemapParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");		  

		// user login to APAS application
		objMappingPage.login(loginUser);
		//  Opening the PARCELS page  and searching the  parcel to perform remap mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);  	

		//  Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);  				

		//Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);  				
		String parentWindow=driver.getWindowHandle();
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);  				
		//  User enters into mapping page	 				
		objWorkItemHomePage.switchToNewWindow(parentWindow);  				
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));	
		//User duplicates apn in remap mapping 2 screen 
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn+","+apn1);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));	 	          
		objMappingPage.remapActionForm(RemapParcelMappingData);  
		Thread.sleep(4000);
		HashMap<String, ArrayList<String>> gridParcelData=      objMappingPage.getGridDataInHashMap();            
		objMappingPage.editGridCellValue("APN", gridParcelData.get("APN").get(1));
		Thread.sleep(4000);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.remapErrorMessageonSecondScreen, 10);
		//Validating error message ,that duplicate apn cannot be remapped to parcels
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.remapErrorMessageonSecondScreen), "The APN provided is a duplicate APN.Please check.", "SMAB-T2634: validate duplicates Apn Cannot be entered in parcel remap");  			

		driver.switchTo().window(parentWindow);

		objMappingPage.logout();

	} 


	/**
	 * Verify that when many parcels are entered that exceed the allocated space, the system should automatically auto-wrap the parent APN so they are displayed properly
	 * 
	 * 
	 * 
	 * @param loginUser
	 * @throws Exception
	 */

	@Test(description = "SMAB-T2627: Verify that when many parcels are entered that exceed the allocated space, the system should automatically auto-wrap the parent APN so they are displayed properly ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
			,groups = {"Regression","ParcelManagement"},enabled =true)
	public void ParcelManagment_Many_NewParcel_Apn_Formatted_RemappingAction(String loginUser) throws Exception
	{
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 11";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);
		String combinedapn=responseAPNDetails.get("Name").get(0)+" , "+
				responseAPNDetails.get("Name").get(1)+" , "+
				responseAPNDetails.get("Name").get(2)+" , "+
				responseAPNDetails.get("Name").get(3)+" , "+
				responseAPNDetails.get("Name").get(4)+" , "+
				responseAPNDetails.get("Name").get(5)+" , "+
				responseAPNDetails.get("Name").get(6)+" , "+
				responseAPNDetails.get("Name").get(7)+" , "+
				responseAPNDetails.get("Name").get(8)+" , "+
				responseAPNDetails.get("Name").get(9)+" , "+
				responseAPNDetails.get("Name").get(10)+" , ";

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");  	
		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
		Map<String, String> hashMapNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");
		//Step1 - user login to APAS application
		objMappingPage.login(loginUser);		
		// Step2: Opening the PARCELS page  and searching the  parcel to perform remap mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);	
		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData); 				

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);					
		String parentWindow=driver.getWindowHandle();
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);				
		// Step 5: User enters into mapping page			
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapNewParcelMappingData.get("Action"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));				
		// Step 6: User enters new combined apn of 11 that alerady exists in the system		
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,combinedapn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));								
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),combinedapn,
				"SMAB-T2627: Verify that when many parcels are entered that exceed the allocated space, the system should automatically auto-wrap the parent APN so they are displayed properly");


		driver.switchTo().window(parentWindow);

		objMappingPage.logout();

	}

	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "remap" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2898,SMAB-T2667,SMAB-T2766,SMAB-T2910,SMAB-T3473,SMAB-T2909,SMAB-T3474,SMAB-T3475:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"remap\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_RemapMappingAction_NoPrimarySitusTRA(String loginUser) throws Exception {
		String queryAPN = "SELECT Id, Name FROM Parcel__c WHERE (Not Name like '%990')and (not Name like '134%') and Status__c = 'Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);
		String apnId1=responseAPNDetails.get("Id").get(0);
		String apnId2=responseAPNDetails.get("Id").get(1);
		
		//Fetching parcels that are Active with different map book and map page
		String mapBookForAPN1 = apn1.split("-")[0];
		String mapPageForAPN1 = apn1.split("-")[1];
		String mapBookForAPN2 = apn2.split("-")[0];
		String mapPageForAPN2 = apn2.split("-")[1];		
		queryAPN = "SELECT Id, Name FROM Parcel__c WHERE (Not Name like '%990') and (Not Name like '134%') and (Not Name like '"+mapBookForAPN1+"%') and (Not Name like '"+mapBookForAPN1+"-"+mapPageForAPN1+"%') and (Not Name like '"+mapBookForAPN2+"%') and (Not Name like '"+mapBookForAPN2+"-"+mapPageForAPN2+"%') and  Primary_Situs__c !=NULL and Status__c = 'Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') Limit 1";
		HashMap<String, ArrayList<String>> responseAPN3Details = salesforceAPI.select(queryAPN);
		String apn3=responseAPN3Details.get("Name").get(0);
		String apnId3=responseAPN3Details.get("Id").get(0);

		// Deleting ownership from parcels
		objMappingPage.deleteOwnershipFromParcel(apnId1);
		objMappingPage.deleteOwnershipFromParcel(apnId2);
		objMappingPage.deleteOwnershipFromParcel(apnId3);
		
		String concatenateAPNWithDifferentMapBookMapPage = apn2+","+apn3;
		
		//Add the parcels in a Hash Map for validations later
		Map<String,String> apnValue = new HashMap<String,String>(); 
		apnValue.put("APN1", apn1); 
		apnValue.put("APN2", apn2); 
		apnValue.put("APN3", apn3); 
		
		//Fetch some other values from database
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') and  Legacy__c = 'NO' limit 1");
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";
		String parcelSize	= "200";	
		
		//Creating Json Object
		JSONObject jsonObject = objMappingPage.getJsonObject();

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObject.put("Lot_Size_SQFT__c",parcelSize);

		//updating details
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(1),jsonObject);
		salesforceAPI.update("Parcel__c",responseAPN3Details.get("Id").get(0),jsonObject);
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), "TRA__c", responseTRADetails.get("Id").get(1));
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(1), "TRA__c", responseTRADetails.get("Id").get(1));	
		salesforceAPI.update("Parcel__c", responseAPN3Details.get("Id").get(0), "TRA__c", responseTRADetails.get("Id").get(1));

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		

		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
		Map<String, String> hashMapRemapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");

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
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel remap' 
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		ReportLogger.INFO("Add a parcel with different Map Book and Map Page in Parent APN field :: " + concatenateAPNWithDifferentMapBookMapPage);
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateAPNWithDifferentMapBookMapPage);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRemapMappingData.get("Action"));

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapRemapMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPN=gridDataHashMap.get("APN").get(0);
		String MappingScreen = driver.getWindowHandle();
			
		ReportLogger.INFO("validate that new APNs added are not lnked to WI before Remap Mapping Action is performed");
		driver.switchTo().window(parentWindow);
		objMappingPage.waitUntilPageisReady(driver);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItem);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh();
		Thread.sleep(5000);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		ReportLogger.INFO("validate that new APNs added are not lnked to WI before Remap Mapping Action is performed");
		softAssert.assertEquals(1,objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI,10).size(),
				"SMAB-T2667: Validate that only 1 APN is linked to Work Item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
				"SMAB-T2667: Validate that first Parent APN is displayed in the linked item");
		
      
		//Step 7: Click generate Parcel Button
		driver.switchTo().window(MappingScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);

		//Step 8: Navigating back to the WI that was created and clicking on related action link 
		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(workItem);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh();
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T2667:Verify user is able to submit the Work Item for approval");
		
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
				"SMAB-T2667,SMAB-T2766,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that first Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("1")),
				"SMAB-T2667,SMAB-T2766,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that second Parent APN is displayed in the linked item");
		objMappingPage.Click(objWorkItemHomePage.detailsTab);
		String referenceURl= objMappingPage.getFieldValueFromAPAS("Navigation Url", "Reference Data Details");
		softAssert.assertTrue(referenceURl.contains(concatenateAPNWithDifferentMapBookMapPage),
				"SMAB-T2910,SMAB-T3473: Validate that Parent APNs are present in the reference Link");
		
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		Thread.sleep(7000);
		
		//Step 9: Validation that User is navigated to a screen with following fields:APN
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2898: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertTrue(!objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2898: Validation that  There is No \"Update Parcel(s)\" button on return to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2898: Validation that APN column should not be editable on retirning to custom screen");
		
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		
		//Step 10: Login from Mapping Supervisor to approve the WI
		ReportLogger.INFO("Now logging in as supervisor to approve the work item");
		objWorkItemHomePage.login(MAPPING_SUPERVISOR);
		
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.completeWorkItem();
		softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Completed","SMAB-T2667:Verify user is able to complete the Work Item");
		
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
				"SMAB-T2667,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that first Parent APN is displayed in the linked item");
		softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("1")),
				"SMAB-T2667,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that second Parent APN is displayed in the linked item");
		objMappingPage.Click(objWorkItemHomePage.detailsTab);
		referenceURl= objMappingPage.getFieldValueFromAPAS("Navigation Url", "Reference Data Details");
		softAssert.assertTrue(referenceURl.contains(concatenateAPNWithDifferentMapBookMapPage),
				"SMAB-T2910,SMAB-T3473: Validate that Parent APNs are present in the reference Link");

		objWorkItemHomePage.logout();
	}


	/**
	 * This test verifies the attributes which will be inherited from the parent
	 * parcel to the child parcel and status of child parcels and parent parcel Also
	 * ,validate status of child and parent parcel before and after closing of WI.
	 * login user-Mapping user
	 * 
	 */
	@Test(description = "SMAB-6789:Parcel Management- Verify that User is able to perform  a \"remap\" mapping output actions for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement" })
	public void ParcelManagement_VerifyRemapMappingActionOutput(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') AND STATUS__C='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI.select(
				"SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String primarySitusValue = salesforceAPI.select(
				"SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='" + apn
						+ "')")
				.get("Name").get(0);
		String legalDescriptionValue = "Legal PM 85/25-260";
		String parentdistrictValue = "01";

		jsonObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c", "Active");
		jsonObject.put("Short_Legal_Description__c", legalDescriptionValue);
		jsonObject.put("District__c", parentdistrictValue);
		jsonObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c", responseTRADetails.get("Id").get(0));
		

		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonObject);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.REMAP_MAPPING_ACTION;
		Map<String, String> hashMapRemapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");

		// Step1: Login to the APAS application using the credentials passed through
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String workItem = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Selecting Action as 'perform parcel remap'
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,
				hashMapRemapMappingData.get("Action"));

		// Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapRemapMappingData);

		// Step 7: Click generate Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		// Step 8: Verify child parcel is visible in parcel related section
		HashMap<String, ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();
		gridDataHashMap.get("APN").stream().forEach(parcel -> {
			try {
				objMappingPage.Click(objMappingPage.getButtonWithText(parcel));
				objMappingPage.waitUntilPageisReady(driver);
				objMappingPage.waitForElementToBeVisible(60, objParcelsPage.moretab);

				objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelRelationshipsTabLabel);
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(apn)),
						"SMAB-T2575: Verify Parent Parcel: " + apn
								+ " is visible under Source Parcel Relationships section");
				driver.navigate().back();
			} catch (Exception e) {
				ExtentTestManager.getTest().log(LogStatus.FAIL,
						"Fail to validate Parent Parcel under Source Parcel Relationships section" + e);
			}
		});

		objMappingPage.waitUntilPageisReady(driver);
		String Apn2 = gridDataHashMap.get("APN").get(0);
		objMappingPage.Click(objMappingPage.getButtonWithText(Apn2));
		String Districtvalue = (objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelDistrictNeighborhood,
				"Summary Values"));
		String[] a = Districtvalue.split("/");
		String childDistrictValue = a[0];

		// Step 9: Verify Neighborhood Code value is inherited from Parent to Child
		// Parcels
		softAssert.assertEquals(
				objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelDistrictNeighborhood, "Summary Values"),
				responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2513: Verify District/Neighborhood Code of Child Parcel is inheritted from first Parent Parcel");

		// Step 10: Verify TRA value is inherited from Parent to Child Parcels

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelTRA, "Parcel Information"),
				responseTRADetails.get("Name").get(0),
				"SMAB-T2513: Verify TRA of Child Parcel is inheritted from first Parent Parcel");

		// Step 11: Validation that child parcel primary situs is inherited from parent
		// parcel
		String childPrimarySitusValue = objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus,
				"Parcel Information");
		softAssert.assertEquals(primarySitusValue.replaceFirst("\\s", ""), childPrimarySitusValue,
				"SMAB-T2513: Validation that primary situs of child parcel is same as primary sitrus of parent parcel");

		// Step 12: Verify District value is inherited from Parent to Child Parcels
		softAssert.assertEquals(childDistrictValue, parentdistrictValue,
				"SMAB-T2513: Verify District of Child Parcel is inheritted from first Parent Parcel");

		String childApnstatus = objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information");
		objMappingPage.globalSearchRecords(apn);
		// Step 13: Verify Status of Parent & Child Parcels before WI completion

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus, "Parcel Information"),
				"In Progress - To Be Expired", "SMAB-T2574: Verify Status of Parent Parcel: " + apn);
		softAssert.assertEquals(childApnstatus, "In Progress - New Parcel",
				"SMAB-T2574: Verify Status of Child Parcel: " + gridDataHashMap.get("APN").get(0));

		driver.switchTo().window(parentWindow);
		objMappingPage.logout();
		Thread.sleep(4000);

		// Step 14: Login as mapping supervisor
		objWorkItemHomePage.login(users.MAPPING_SUPERVISOR);
		objMappingPage.globalSearchRecords(workItem);

		// step 15: Complete the work item
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.completedTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.markStatusCompleteBtn);

		Thread.sleep(4000);
		objMappingPage.logout();
		Thread.sleep(3000);

		// Step 16: Login as mapping staff
		objWorkItemHomePage.login(users.MAPPING_STAFF);
		objMappingPage.globalSearchRecords(workItem);

		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);

		// Step 17: Verify Status of Parent & Child Parcels after WI completion
		objMappingPage.globalSearchRecords(apn);
		String parentAPN1Status = objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus,
				"Parcel Information");
		objMappingPage.globalSearchRecords(gridDataHashMap.get("APN").get(0));
		String childAPN1Status = objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus,
				"Parcel Information");
		softAssert.assertEquals(parentAPN1Status, "Retired", "SMAB-T2577: Verify Status of Parent Parcel: " + apn);
		softAssert.assertEquals(childAPN1Status, "Active",
				"SMAB-T2577: Verify Status of Child Parcel: " + gridDataHashMap.get("APN").get(0));

		// Step 18 : Switch to parent window and logout
		driver.switchTo().window(parentWindow);
		objMappingPage.logout();
	}
	
	/**
	 * This method is to  Verify WI rejection on Remap mapping action
	 *@param loginUser
	 * @throws Exception
	 */	
	@Test(description = "SMAB-T3464:Verify the Output validations for \"Remap\" mapping action for a Parcel (retired) after rejected the work item ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement" })
	public void ParcelManagement_VerifyWIRejectionAfterPerformRemapMappingAction(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI.select(
				"SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String primarySitusValue = salesforceAPI.select(
				"SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='" + apn
						+ "')")
				.get("Name").get(0);
		String legalDescriptionValue = "Legal PM 85/25-260";
		String parentdistrictValue = "01";

		jsonObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c", "Active");
		jsonObject.put("Short_Legal_Description__c", legalDescriptionValue);
		jsonObject.put("District__c", parentdistrictValue);
		jsonObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c", responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonObject);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.REMAP_MAPPING_ACTION;
		Map<String, String> hashMapRemapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");

		// Step1: Login to the APAS application using the credentials passed through
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String workItem = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Step 5: Selecting Action as 'perform parcel remap'
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,
				hashMapRemapMappingData.get("Action"));

		// Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapRemapMappingData);

		// Step 7: Click generate Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		// Step 8: Verify child parcel is visible in parcel related section
		HashMap<String, ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();
		objMappingPage.waitUntilPageisReady(driver);
		String Apn2 = gridDataHashMap.get("APN").get(0);
		objMappingPage.Click(objMappingPage.getButtonWithText(Apn2));
		driver.switchTo().window(parentWindow);
		//Step 9: Mark the WI complete
		String query = "Select Id from Work_Item__c where Name = '"+workItem+"'";
		salesforceAPI.update("Work_Item__c", query, "Status__c", "Submitted for Approval");
		
		objMappingPage.logout();
		Thread.sleep(4000);

		// Step 10: Login as mapping supervisor
		objWorkItemHomePage.login(users.MAPPING_SUPERVISOR);
		
		//step 11: rejecting work item
		objWorkItemHomePage.rejectWorkItem(workItem,"Other","Reject Mapping action after submit for approval");
				
		// Step 12: Verify Status of Parent Parcel after WI rejected
		objMappingPage.globalSearchRecords(apn);
		String parentAPN1Status = objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus,"Parcel Information");
		softAssert.assertEquals(parentAPN1Status,"Active","SMAB-T3464: Verify Status of Parent Parcel: "+apn);
	    
		//Step 13: Verify Child Parcels should be delete after WI rejected
		String childAPNNumber1 =gridDataHashMap.get("APN").get(0);
	    query = "SELECT Id FROM Parcel__c Where Name = '"+childAPNNumber1+ "'";
	    String targetedApnquery="SELECT  Id, Target_Parcel__c FROM Parcel_Relationship__c where source_parcel__r.name='"+apn+ "' and Parcel_Actions__c='Perform Parcel Remap'";
	    HashMap<String, ArrayList<String>> response = salesforceAPI.select(targetedApnquery);
		softAssert.assertEquals(response.size(),0,"SMAB-T3464: Validate that there is no parcel relationship on Parent Parcel when Rejected the Work tem after Split Mapping Action");
		
		response = salesforceAPI.select(query);
	    softAssert.assertEquals(response.size(),0,"SMAB-T3464: Validate that child apn should be deleted "+childAPNNumber1+" after Split Mapping Action after performing rejection of work item");
	   
	    // Step 14 : Switch to parent window and logout
	    objMappingPage.logout();
	}
	
	@Test(description = "SMAB-T3511,SMAB-T3512,SMAB-T3513:Verify that the Related Action label should"
			+ " match the Actions labels while creating WI and it should open mapping screen on clicking",
			dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, 
			groups = {"Regression","ParcelManagement","RecorderIntegration" },enabled=true)
	public void ParcelManagement_VerifyNewWICovenantsCondRestrGeneratedfromRecorderIntegrationAndRemapMappingAction(String loginUser) throws Exception {
				
		
		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);

		salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c"
				+ " where Sub_type__c='Covenants, Cond & Restr with condo' and status__c ='In pool'", 
				"status__c","In Progress");

		//finding DOC ID
		String queryAPN = "Select Id,Recorder_DOC_Number__c,"
				+ "(Select parcel__c from recorded_apns__r), "
				+ "Recorder_Doc_Type__c from recorded_document__c "
				+ "where status__c = 'Processed' and Recorder_Doc_Type__c = 'CCR' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String recordedDocumentId=responseAPNDetails.get("Id").get(0);
		
		//generating WI
		objtransfer.generateRecorderJobWorkItems(recordedDocumentId);

		String WorkItemQuery="SELECT Id,Name FROM Work_Item__c where Type__c='MAPPING'"
				+ " AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1"; 
		
		HashMap<String, ArrayList<String>> responseWIDetails = salesforceAPI.select(WorkItemQuery);
		String WorkItemNo=responseWIDetails.get("Name").get(0);		

		
		//Searching for the WI genrated
		objMappingPage.globalSearchRecords(WorkItemNo); 
		
		//making all retired parcel status active
		List<String> ApnNumber = new ArrayList<String>();
		int totalNoOfAPNs = objMappingPage.getGridDataInHashMap(1).get("APN").size();
		for(int i=0;i<totalNoOfAPNs;i++) {
			String ApnfromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(i);
			ApnNumber.add(ApnfromWIPage);
			//updating APN details
			String query = "Select Id from Parcel__c where Name = '"+ApnfromWIPage+"'";
			salesforceAPI.update("Parcel__c",query,"Status__c","Active");
		}
		objMappingPage.logout();
		
		//Mapping user logs in and perform mapping action on the WI genrated
		objMappingPage.login(loginUser);
		String mappingActionCreationData = testdata.REMAP_MAPPING_ACTION;
		Map<String, String> hashMapRemapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");
		objMappingPage.globalSearchRecords(WorkItemNo);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

		softAssert.assertTrue(!(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiEventId).equals(" ")),
				"SMAB-T3513: Verfiying the Event ID of WI genrated for given Recorded Document");
		
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS
				(objWorkItemHomePage.wiRelatedActionDetailsPage),"Covenants, Cond & Restr with condo" ,
				"SMAB-T3511: Verfiying the Related Action of WI genrated for given Recorded Document");

		softAssert.assertTrue(!objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.editEventIdButton),
				"SMAB-T3513-This field should not be editable.");

		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// Fill data  in mapping screen
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapRemapMappingData.get("Action"));		
		objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel, hashMapRemapMappingData.get("Reason code"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		
		//second screen of mapping action
		objMappingPage.waitForElementToBeClickable(10, objMappingPage.generateParcelButton);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		
		//switching to main screen
		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(WorkItemNo);

		//validate that The "Return " functionality for parcel mgmt activities should work for all these work items.
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertTrue(!objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T3512-validate that The Return functionality for parcel mgmt activities should work for all these work items.");
		driver.switchTo().window(parentWindow);
		
		objWorkItemHomePage.logout();

	}
}