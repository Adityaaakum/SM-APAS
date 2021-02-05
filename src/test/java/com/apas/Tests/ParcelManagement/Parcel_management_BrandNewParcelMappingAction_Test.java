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

public class Parcel_management_BrandNewParcelMappingAction_Test extends TestBase implements testdata, modules, users{
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
	 * This method is to Verify that User is able to perform a "Brand New Parcel" mapping action for a Parcel (Active) of type Non Condo from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2263,SMAB-T2521,SMAB-T2522,SMAB-T2537,SMAB-T2547:Verify that User is able to perform a \"Brand New Parcel\" mapping action for a Parcel (Active) of type Non Condo from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_VerifyBrandNewParcelMappingActionNonCondoParcel(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData = System.getProperty("user.dir") + testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithoutAllFields");

		// Step1: Login to the APAS application using the credentials passed through data provider (login Mapping User)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		//Step 5: Validation that work pool should be 'Mapping' on parent parcel work item
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"Mapping","SMAB-T2263: Validation that work pool should be 'Mapping' on parent parcel work item");
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));
		
		//Step 6: Validating warning for parent parcel for brand new parcel on first screen
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Warning: If a parent parcel value is present it will not be taken into consideration while creating a new parcel",
				"SMAB-T2522: Validation that Warning: If a parent parcel value is present it will not be taken into consideration while creating a new parcel");
		
		//Step 7: Validating that reason code field is auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2263: Validation that reason code field is auto populated from parent parcel work item");
		objMappingPage.scrollToBottomOfPage();
		
		//Step 8: entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		
		//Step 10: Validating warning message on second screen
				softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789",
						"SMAB-T2537: Validation that Warning present on secound screeen ");
				
		//Step 11 :Clicking generate parcel button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.CreateNewParcelButton));
		//Step 12: Validating that Parcel has been successfully created.
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),"Parcel has been successfully created. Please Review Spatial Information",
				"SMAB-T2547: Validation that Parcel has been successfully created. Please Review Spatial Information");
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}
	/**
	 * This method is to  Verify that the Brand new parcels Mapping Action can only be performed on Active Parcels
	 *@param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2523,SMAB-T2524,SMAB-T2525,SMAB-T2527:Validation on the Brand New parcel Mapping Action can only be performed on Active Parcels ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_VerifyFirstNonCondoFieldOnBrandNewparcelAction(String loginUser) throws Exception {
		String queryAPN = "Select name From Parcel__c where Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String activeParcelToPerformMapping=responseAPNDetails.get("Name").get(0);
		
		String mappingActionCreationData = System.getProperty("user.dir") + testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithoutAllFields");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		// Step1: Login to the APAS application using the credentials passed through data provider (mapping staff user)
		objMappingPage.login(loginUser);

		// Step 2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeParcelToPerformMapping);

		//Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));
		
	
		//Step 5: Validating mandatory field validation
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.reasonCodeTextBoxLabel,"");
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.reasonCodeTextBoxLabel,""),"Please enter the required field : Reason Code, First non-Condo Parcel Number",
				"SMAB-T2527: Validation that reason code is a mandatory field");
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), "Performing Brand New Parcel mapping action");
		objMappingPage.scrollToBottomOfPage();

		//Step 6: Validation that proper error message is displayed if a parcel number starting from 100 is entered in non condo number field
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"100-234-561"),"Non Condo Parcel Number cannot start with 100, Please enter valid Parcel Number",
				"SMAB-T2525: Validation that proper error message is displayed if a parcel number starting from 100 is entered in non condo number field");
		
		//Step 7: Validation that proper  error message is displayed if an special character parcel number is entered in non condo number field
				objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"12#-123-3@$");
				softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"abc123123abc"),"This parcel number is not valid, it should contain 9 digit numeric values.",
						"SMAB-T2523: Validation that proper error message is displayed if an alphanummeric parcel number is entered in non condo number field");

		//Step 8: Validation that proper  error message is displayed if an alphanummeric parcel number is entered in non condo number field
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"abc123123abc");
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"abc123123abc"),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2523: Validation that proper error message is displayed if an alphanummeric parcel number is entered in non condo number field");

		
		//Step 9: Validation that proper  error message is displayed if parcel number  not of Nine digits is entered in non condo number field
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"010123"),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2523: Validation that proper error message is displayed if  parcel number  not of Nine digits is entered in non condo number field");
		//Step 10: entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		Thread.sleep(5000);
				
		//Step 11 :Verify that User is able to to create a district, Use Code for the child parcel from the custom screen after performing one to one mapping action
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,activeParcelToPerformMapping);
		objMappingPage.editGridCellValue(objMappingPage.reasonCodeColumnSecondScreen,"001vacant");
		objMappingPage.editGridCellValue(objMappingPage.legalDescriptionColumnSecondScreen,"Legal Discription");
				
		//Step 13 :Clicking generate parcel button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.CreateNewParcelButton));
		//Step 14: Validating that
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789",
					"SMAB-T2524: Validation that Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789");
			
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}

}
