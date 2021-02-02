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
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

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

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);

	}
		
	@Test(description = "SMAB-T2490,SMAB-T2436,SMAB-T2536:Verify that User is able to perform a Remap mapping action for a Parcel from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_VerifyRemapMappingActionForMultipleParcels(String loginUser) throws Exception {
       ArrayList<String> APNs=objMappingPage.fetchActiveAPN(2);
		String activeParcelToPerformMapping=APNs.get(0);
		String activeParcelToPerformMapping2=APNs.get(1);
		
		// fetching  parcel that is retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);

				
		String mappingActionCreationData = System.getProperty("user.dir") + testdata.REMAP_MAPPING_ACTION;
		Map<String, String> remapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
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
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
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
	@Test(description = "SMAB-T2490,SMAB-T2493,SMAB-T2532,SMAB-T2535,SMAB-T2531,SMAB-T2533:Verify that User is able to perform a Remap mapping action for a Parcel from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_VerifyRemapMappingAction(String loginUser) throws Exception {
		String activeParcelToPerformMapping=objMappingPage.fetchActiveAPN();
		String activeParcelWithoutHyphen=activeParcelToPerformMapping.replace("-","");
		
		// fetching  parcel that is retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);
		
		// fetching  parcel that is In progress	
		String inProgressAPNValue;
		queryAPNValue = "select Name from Parcel__c where Status__c='In Progress - To Be Expired' limit 1";
		response = salesforceAPI.select(queryAPNValue);
		if(!response.isEmpty())
		inProgressAPNValue= response.get("Name").get(0);
		else
		{
		inProgressAPNValue= objMappingPage.fetchActiveAPN();
		jsonObject.put("PUC_Code_Lookup__c","In Progress - To Be Expired");
		jsonObject.put("Status__c","In Progress - To Be Expired");
		salesforceAPI.update("Parcel__c",objMappingPage.fetchActiveAPN(),jsonObject);
		}
		
				
		String mappingActionCreationData = System.getProperty("user.dir") + testdata.REMAP_MAPPING_ACTION;
		Map<String, String> remapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");

		String workItemCreationData =testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
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
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2532: Validation that proper error message is displayed if parent parcel is retired");

		//Step 6: Validation that proper error message is displayed if parent parcel is in progress
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2532: Validation that proper error message is displayed if parent parcel is in progress status");

		//Step 7: Verifying that User should be allowed to enter the 9 digit APN without the \"-\" in Parent APN field
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, activeParcelWithoutHyphen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),activeParcelToPerformMapping,
				"SMAB-T2535: Validation that User should be allowed to enter the 9 digit APN without the \"-\" in Parent APN field");


		//Step 8: Validating that reason code field is auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2490: Validation that reason code field is auto populated from parent parcel work item");
		
		//Step 9: Verifying that proper error message is displayed if alphanumeric value  is entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "123-45*-78&");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));		
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2493: Validation that proper error message is displayed if alphanumeric value  is entered in First non condo parcel field");
		
		//Step 10:Verifying that proper error message is displayed if less than 9 disgits are entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "123-456-78");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));		
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"Parcel Number has to be 9 digit, please enter valid parcel number",
				"SMAB-T2493: Validation that proper error message is displayed if less than 9 disgits are entered in First non condo parcel field");
		
		//Step 11:Verifying that proper error message is displayed if parcel starting with 100 is entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "100-456-789");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));		
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"Non Condo Parcel Number cannot start with 100 or 134, Please enter valid Parcel Number",
				"SMAB-T2531: Validation that proper error message is displayed if parcel starting with 100 is entered in First non condo parcel field");
		
		//Step 12:Verifying that proper error message is displayed if parcel starting with 134 is entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "134-456-789");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));		
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"Non Condo Parcel Number cannot start with 100 or 134, Please enter valid Parcel Number",
				"SMAB-T2531: Validation that proper error message is displayed if parcel starting with 134 is entered in First non condo parcel field");
		
		//Step 13: Verifying that User should be allowed to enter the 9 digit APN without the \"-\" in First Non Condo Parcel Field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, activeParcelWithoutHyphen);
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));	
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.firstNonCondoTextBoxLabel),"value"),activeParcelToPerformMapping,
				"SMAB-T2535: Validation that User should be allowed to enter the 9 digit APN without the \"-\" in First Non Condo Parcel Field");
	
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
}
