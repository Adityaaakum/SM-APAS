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

	@Test(description = "SMAB-T2663,SMAB-T2263,SMAB-T2521,SMAB-T2522,SMAB-T2537,SMAB-T2547:Verify that User is able to perform a \"Brand New Parcel\" mapping action for a Parcel (Active) of type Non Condo from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" },enabled= true)
	public void ParcelManagement_VerifyBrandNewParcelMappingActionNonCondoParcel(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData = testdata.Brand_New_Parcel_MAPPING_ACTION;
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
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));

		//Step 6: Validating warning for parent parcel for brand new parcel on first screen
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"- Warning: If a parent parcel value is present it will not be taken into consideration while creating a new parcel",
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

		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		softAssert.assertTrue(gridDataHashMap.get("Situs").get(0).isEmpty(),"SMAB-T2663: Validation that primary situs of child parcel on second screen is blank as situs was not updated in first screen");

		//Step 11 :Clicking generate parcel button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		//Step 12: Validating that Parcel has been successfully created.
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),
				"Parcel(s) have been successfully created. Please Review Spatial Information",
				"SMAB-T2547: Validation that Parcel has been created successfully. Please Review Spatial Information");

		//Step 13: Validation that child parcel primary situs is blank since  situs was not updated in first screen
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertTrue(gridDataHashMap.get("Situs").get(0).isEmpty(),"SMAB-T2663: Validation that primary situs of child parcel on last screen is blank as situs was not updated in first screen");

		HashMap<String, ArrayList<String>> childPrimarySitusValue =salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ childAPNNumber +"')");
		softAssert.assertTrue(childPrimarySitusValue.isEmpty(),
				"SMAB-T2663: Validation that primary situs of child parcel is blank as situs was not updated in first screen ");
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}
	/**
	 * This method is to  Verify that the Brand new parcels Mapping Action can only be performed on Active Parcels
	 *@param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2523,SMAB-T2524,SMAB-T2525,SMAB-T2527:Validation on the Brand New parcel Mapping Action can only be performed on Active Parcels ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Smoke","Regression","ParcelManagement" },enabled = true)
	public void ParcelManagement_VerifyFirstNonCondoFieldOnBrandNewparcelAction(String loginUser) throws Exception {
		String queryAPN = "Select name From Parcel__c where Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String activeParcelToPerformMapping=responseAPNDetails.get("Name").get(0);

		String mappingActionCreationData =  testdata.Brand_New_Parcel_MAPPING_ACTION;

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
		 objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
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
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		
		//Step 14: Validating the warning message
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789",
				"SMAB-T2524: Validation that Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}

	 /* This method is to Verify that User is able to update Situs from the Parcel mapping screen for "Brand New Parcel" mapping action

		 * @param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T2663: Verify that User is able to update Situs from the Parcel mapping screen for \"Brand New Parcel\" mapping action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
				"Regression","ParcelManagement" },enabled= false)
		public void ParcelManagement_UpdateChildParcelSitusFirstScreen_BrandNewMappingAction1(String loginUser) throws Exception {
			String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 1";
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
			String apn=responseAPNDetails.get("Name").get(0);
			
			String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");

			String mappingActionCreationData =  testdata.Brand_New_Parcel_MAPPING_ACTION;
			Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformBrandNewParcelMappingActionWithSitusData");

			String situsCityName = hashMapBrandNewParcelMappingData.get("Situs City Name");
			String direction = hashMapBrandNewParcelMappingData.get("Direction");
			String situsNumber = hashMapBrandNewParcelMappingData.get("Situs Number");
			String situsStreetName = hashMapBrandNewParcelMappingData.get("Situs Street Name");
			String situsType = hashMapBrandNewParcelMappingData.get("Situs Type");
			String situsUnitNumber = hashMapBrandNewParcelMappingData.get("Situs Unit Number");
			String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;

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
			
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();	
			objWorkItemHomePage.switchToNewWindow(parentWindow);
	        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
			objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));

			//Step 5: editing situs for child parcel and filling all fields
			objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabelForBrandNewParcel));

			softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.editSitusLabelSitusModal),
					"SMAB-T2663: Validation that Edit Situs label is displayed as heading of situs modal window in first screen");
			softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.situsInformationLabelSitusModal),
					"SMAB-T2663: Validation that  Situs Information label is displayed in  situs modal window in first screen");
			objMappingPage.editSitusModalWindowFirstScreen(hashMapBrandNewParcelMappingData);
			softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabelForBrandNewParcel),"value"),childprimarySitus,
					"SMAB-T2663: Validation that User is able to update a Situs for child parcel from the Parcel mapping screen");

			//Step 6: entering data in form for Brand New Parcel mapping
			objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);

			//Step 7: Validation that primary situs on second screen is getting populated from situs entered in first screen
			HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
			String childAPNNumber =gridDataHashMap.get("APN").get(0);
			softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),childprimarySitus,
					"SMAB-T2663: Validation that System populates primary situs on second screen for child parcel  with the situs value that was added in first screen");

			//Step 8 :Clicking generate parcel button
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

			//Step 9: Validation that primary situs on last screen screen is getting populated from situs entered in first screen
			gridDataHashMap =objMappingPage.getGridDataInHashMap();
			softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),childprimarySitus,
					"SMAB-T2663: Validation that System populates primary situs on last screen for child parcel with the situs value that was added in first screen");

			String primarySitusValueChildParcel =salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ childAPNNumber +"')").get("Name").get(0);
			softAssert.assertEquals(primarySitusValueChildParcel,childprimarySitus,
					"SMAB-T2663: Validation that primary situs of  child parcel  has value that was entered in first screen through situs modal window");
					driver.switchTo().window(parentWindow);
			objWorkItemHomePage.logout();

		}
	/**

	 *  Upon completion of the "Brand New Parcel" action, the system will create the desired parcels
	 * 
	 * @param loginUser-Mapping user
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2642,SMAB-T2643,SMAB-T2644:Verify that User is able to perform a \"Brand New Parcel\" mapping action for a Parcel   from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" },enabled =true)
	public void ParcelManagement_Verify_Brand_NewParcel_Mapping_Action(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData = testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithoutAllFields");

		//  Login to the APAS application using the credentials passed through data provider (login Mapping User)
		objMappingPage.login(loginUser);

		//  Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		//  Creating Manual work item for the Parcel 
	     String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
	    
			
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		
		//objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
						
		// entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		// Validating that Parcel has been successfully created.
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2642: Validation that Parcel has been successfully created. Please Review Spatial Information");
		
		// Retriving new APN genrated
           HashMap<String, ArrayList<String>> gridParcelData = objMappingPage.getGridDataInHashMap();
           String newCreatedApn  =   gridParcelData.get("APN").get(0);                         
           HashMap<String, ArrayList<String>> statusnewApn = objParcelsPage.fetchFieldValueOfParcel("Status__c", newCreatedApn);
             // validating status of brand new parcel           
            softAssert.assertEquals(statusnewApn.get("Status__c").get(0), "In Progress - New Parcel", "SMAB-T2643: Verifying the status of the new parcel");
            driver.switchTo().window(parentWindow);
		    objMappingPage.logout();  
            
		    Thread.sleep(10000);
		    objMappingPage.login(users.RP_APPRAISER);
   		
            //Completing the workItem
           String   queryWI = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
     	   salesforceAPI.update("Work_Item__c",queryWI, "Status__c", "Completed");
     	   
     	   objMappingPage.searchModule(PARCELS);
		   objMappingPage.globalSearchRecords(newCreatedApn);
		   
     		//Validating the status of the workItem 
     		 HashMap<String, ArrayList<String>> statusCompletedApn = objParcelsPage.fetchFieldValueOfParcel("Status__c",newCreatedApn);
             //Validating the status of parcel after completing WI
           softAssert.assertEquals(statusCompletedApn.get("Status__c").get(0), "Active",
        		   "SMAB-T2644: Validating that the status of new APN is active");
           // driver.switchTo().window(parentWindow);
		    objMappingPage.logout();
		   		
            		                          
		   
	}
	/**
	 * Once the parcel creation has been approved, the user will not be allowed to change the APN allocated.
	 * 
	 * 
	 * @param loginUser-Mapping user
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T2646: Once the parcel creation has been approved, the user will not be allowed to change the APN allocated.", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" },enabled = true)
	public void ParcelManagement_Verify_NoTAllowed_ToChange_NewAPN(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 2";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);
		
	

	

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");


		String mappingActionCreationData =  testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithoutAllFields");

		//  Login to the APAS application using the credentials passed through data provider (login Mapping User)
		objMappingPage.login(loginUser);

		//  Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		//  Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		 
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);	
	     // entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));		
		 HashMap<String, ArrayList<String>> gridParcelData = objMappingPage.getGridDataInHashMap();
         String newCreatedApn  =   gridParcelData.get("APN").get(0);
          driver.switchTo().window(parentWindow);         
          objMappingPage.searchModule(PARCELS);         
 		  objMappingPage.globalSearchRecords(newCreatedApn);
 		
 		//clicking on edit button in parcels page
 		 objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.EditButton));
 		  objParcelsPage.scrollToElement(objParcelsPage.getWebElementWithLabel(objParcelsPage.editApnField)); 		                   
 		 //Entering new apn value 		 
 		 objParcelsPage.enter(objParcelsPage.getWebElementWithLabel(objParcelsPage.editApnField), apn2); 		         
 		 softAssert.assertEquals(objParcelsPage.saveRecordAndGetError().contains("You can't save this record because a duplicate record already exists"),true ,"SMAB-T2646: Verifying that new APN cannot be reupdated."); 	         
 		 objParcelsPage.cancelRecord();
 		 objParcelsPage.logout(); 
 		       
 		      
 				         
	}
	
	/**
	 * The update legal and short legal description should be visible in parcel if added while creating the parcel and these fields should be editable after the parcel is approved
	 * 
	 * @param loginUser - Mapping user
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T2647: The update legal and short legal description should be visible in parcel if added while creating the parcel and these fields should be editable after the parcel is approved", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" },enabled =true)
	public void ParcelManagement_VerifyLegalDescIsEditable(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		String mappingActionCreationData =   testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithoutAllFields");

		//  Login to the APAS application using the credentials passed through data provider (login Mapping User)
		objMappingPage.login(loginUser);
		//  Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		//  Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);			
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		
	    // entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));		
		 HashMap<String, ArrayList<String>> gridParcelData=      objMappingPage.getGridDataInHashMap();
         String newCreatedApn  =   gridParcelData.get("APN").get(0);         
         driver.switchTo().window(parentWindow);         
         objMappingPage.searchModule(PARCELS);         
 		objMappingPage.globalSearchRecords(newCreatedApn); 		
 		//clicking on edit button in parcels page 		
 		 objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.EditButton));
 	     boolean status = objParcelsPage.verifyElementEnabled(objParcelsPage.getWebElementWithLabel(objParcelsPage.LongLegalDescriptionLabel));		
 	 	objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.SaveButton)); 
 		  //validating that long desc field is editable
 		 softAssert.assertEquals(status ,true, "SMAB-T2647:Validating long desc field is editable");
 		  objParcelsPage.logout();
 		  
 		  
	}
	/**
	 * This method is to Verify that User is able to update Situs from the Parcel mapping screen for "Brand New Parcel" mapping action

	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2663: Verify that User is able to update Situs from the Parcel mapping screen for \"Brand New Parcel\" mapping action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" },enabled= true)
	public void ParcelManagement_UpdateChildParcelSitusFirstScreen_BrandNewMappingAction(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithSitusData");

		String situsCityName = hashMapBrandNewParcelMappingData.get("Situs City Name");
		String direction = hashMapBrandNewParcelMappingData.get("Direction");
		String situsNumber = hashMapBrandNewParcelMappingData.get("Situs Number");
		String situsStreetName = hashMapBrandNewParcelMappingData.get("Situs Street Name");
		String situsType = hashMapBrandNewParcelMappingData.get("Situs Type");
		String situsUnitNumber = hashMapBrandNewParcelMappingData.get("Situs Unit Number");
		String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;

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
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));

		//Step 5: editing situs for child parcel and filling all fields
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabelForBrandNewParcel));

		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.editSitusLabelSitusModal),
				"SMAB-T2663: Validation that Edit Situs label is displayed as heading of situs modal window in first screen");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.situsInformationLabelSitusModal),
				"SMAB-T2663: Validation that  Situs Information label is displayed in  situs modal window in first screen");
		objMappingPage.editSitusModalWindowFirstScreen(hashMapBrandNewParcelMappingData);
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabelForBrandNewParcel),"value"),childprimarySitus,
				"SMAB-T2663: Validation that User is able to update a Situs for child parcel from the Parcel mapping screen");

		//Step 6: entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);

		//Step 7: Validation that primary situs on second screen is getting populated from situs entered in first screen
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),childprimarySitus,
				"SMAB-T2663: Validation that System populates primary situs on second screen for child parcel  with the situs value that was added in first screen");

		//Step 8 :Clicking generate parcel button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		//Step 9: Validation that primary situs on last screen screen is getting populated from situs entered in first screen
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),childprimarySitus,
				"SMAB-T2663: Validation that System populates primary situs on last screen for child parcel with the situs value that was added in first screen");

		String primarySitusValueChildParcel =salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ childAPNNumber +"')").get("Name").get(0);
		softAssert.assertEquals(primarySitusValueChildParcel,childprimarySitus,
				"SMAB-T2663: Validation that primary situs of  child parcel  has value that was entered in first screen through situs modal window");
				driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}
	
	 /**
     * Verify Parent APN field cannot be  greyed  except if mapping action is brand new parcel 
     * 
     * @param loginUser
     * @throws Exception
     */
    
    @Test(description = "SMAB-T2632,SMAB-T2693 : Verify Parent APN field cannot be greyed except if mapping action is brand new parcel ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
  		  ,groups = {"Regression","ParcelManagement"},enabled =true)
    public void ParcelManagment_verify_BrandNewParcel_ParentParcel_Greyed_BrandNewMappingAction(String loginUser) throws Exception
    {
  	  String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 2";
  		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
  		String apn=responseAPNDetails.get("Name").get(0);
  		String apn1=responseAPNDetails.get("Name").get(1);
  		
  	String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
  		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
  				"DataToCreateWorkItemOfTypeParcelManagement");
  	
  		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
  		Map<String, String> hashMapNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
  				"DataToPerformRemapMappingAction");
  		String mappingActionBrandNewParcelData =  testdata.Brand_New_Parcel_MAPPING_ACTION;
  		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionBrandNewParcelData,
  				"DataToPerformBrandNewParcelMappingActionWithoutAllFields");  		  
  	
          //Step1 - user login to APAS application
  		       objMappingPage.login(loginUser);		
  	
          // Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
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
   			 objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
   				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));
   				softAssert.assertEquals(objMappingPage.verifyElementVisible(objMappingPage.parentAPNEditButton),false,"SMAB-T2632: Verify edit button is not available");
   				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapNewParcelMappingData.get("Action"));
   				//Verifying APN field  is populated(enabled) for parcel remap unlike brand new parcel mapping
   				softAssert.assertEquals(objMappingPage.verifyElementVisible(objMappingPage.parentAPNEditButton),true,"SMAB-T2632: Verify edit button is  available");
                //Validating fields are auto-populated or not when apn field is empty
   				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapNewParcelMappingData.get("Action"));
   				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, "");
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				softAssert.assertEquals(objMappingPage.verifyElementVisible(objMappingPage.reasonCodeTextBoxLabel), false, "SMAB-T2693: Verify fields are not populated");
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, apn1);
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapNewParcelMappingData.get("Action"));
								//Verifying that fields got auto populated when there is a change in apn
				softAssert.assertEquals(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel).isEnabled(), true, "SMAB-T2693:Verify fields are populated");

				
   				
   			   driver.switchTo().window(parentWindow);
			   objMappingPage.logout();
               
    
    
    
    
    
}
}
