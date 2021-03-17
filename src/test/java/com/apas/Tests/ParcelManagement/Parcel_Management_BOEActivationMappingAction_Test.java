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
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.relevantcodes.extentreports.LogStatus;

public class Parcel_Management_BOEActivationMappingAction_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	JSONObject jsonObject= new JSONObject();
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
	 * This method is to Verify that User is able to view various error messages while perform a "BOE Activation" mapping action from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2748,SMAB-T2689,SMAB-T2688,SMAB-T2754,SMAB-T2749,:Verify that User is able to view the various error message during BOE Activation mapping Action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","Smoke" })
	public void ParcelManagement_VerifyErrorMessagesInBOEActivationMappingAction(String loginUser) throws Exception {
		
		//Fetching parcel that is Retired 		
		String queryAPNValue = "SELECT Source_Parcel__r.Name, Parcel_Actions__c,Id,Name  From Parcel_Relationship__c Where Parcel_Actions__c != 'BOE Activation' And Target_Parcel_Status__c = 'Retired' Limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);
		String retiredParcelWithoutHyphen=retiredAPNValue.replace("-","");

		//Fetching parcels that are Active 
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Status__c='Active'";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn1=responseAPNDetails.get("Name").get(0);
		
		String expectedIndividualFieldMessage = "Complete this field.";
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

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
		Thread.sleep(5000);
		
		// Step 6: Select the BOE activation value in Action field
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,"BOE Activation");
		//Step 7: Validating Error for parent parcel for 'BOE Activation' on first screen
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"-In order to proceed with this action, the parent parcel (s) must be Retired.",
						"SMAB-T2748: Validation that Warning: -In order to proceed with  BOE activation , the parent parcel (s) must be Retired.");
		Thread.sleep(2000);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
	    Thread.sleep(2000);
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,retiredParcelWithoutHyphen);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,"BOE Activation");
		
		//Step 8: Validate that Reason CODE is a mandatory field		
		ReportLogger.INFO("Remove the value from Reason Code field and click Retire button");
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), "");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertEquals(objMappingPage.getIndividualFieldErrorMessage("Reason Code"),expectedIndividualFieldMessage,
						"SMAB-T2749: Validate that 'Reason Code' is a mandatory field");
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), "test");

		objMappingPage.enter("First non-Condo Parcel Number","123456789");

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		//Step 9: Validation that Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789",
						"SMAB-T2754,SMAB-T2689: Validation that Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789");

		//Step 10 :Clicking generate parcel button
	    objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelsButton));
	    softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Please Review Spatial Information",
				"SMAB-T2688: Validate that User is able to perform BOE Activation action for one retired parcel");
		
		objMappingPage.logout();

	}
	 /**
     * This method is to Verify that User is able to perform output validations for "BOE Activation" mapping action for a Parcel (Active) from a work item
     * @param loginUser
     * @throws Exception
     */
    @Test(description = "SMAB-T2757,SMAB-T2758,SMAB-T2759,SMAB-T2760,SMAB-T2761,SMAB-T2687:"
    		+ "Verify the Output validations for \"BOE Activation\" mapping action for a Parcel (retired) from a work item",
    		dataProvider = "loginMappingUser",
    		dataProviderClass = DataProviders.class, 
    		groups = {"Regression","ParcelManagement" })
    public void ParcelManagement_VerifyBOEActivationMappingActionOutputValidations(String loginUser) throws Exception {

        // Step 1: Fetching parcels that are Active with no Ownership record
        String queryAPNValue = "SELECT Source_Parcel__r.Name, Parcel_Actions__c,Id,Name  From Parcel_Relationship__c Where Parcel_Actions__c != 'BOE Activation' And Target_Parcel_Status__c = 'Retired' Limit 1";
        HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
        String apn1=responseAPNDetails.get("Name").get(0);
        //step 2: getting Neighborhood and tra value
        String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
        // Step 3: update  values on Parcels
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
	
        String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
                "DataToCreateWorkItemOfTypeParcelManagement");

        // Step 4: Login to the APAS application using the credentials passed through data provider
        objMappingPage.login(loginUser);

        // Step 5: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn1);

        // Step 6: Creating Manual work item for the Parcel
        String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

        //Step 7: Clicking the  details tab for the work item newly created and clicking on Related Action Link
        objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
        objWorkItemHomePage.waitForElementToBeVisible(40,objWorkItemHomePage.referenceDetailsLabel);
        objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
        String parentWindow = driver.getWindowHandle();
        objWorkItemHomePage.switchToNewWindow(parentWindow);
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

         //Step 8: Selecting Action as 'Many To Many' & Taxes Paid fields value as 'N/A'
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,"BOE Activation");
		objMappingPage.enter("First non-Condo Parcel Number","123456789");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		Thread.sleep(2000);
		//Step 9: Validating that
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789",
						"Validation that Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789");
		//Step 10: generate  new child parcels 
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelsButton));

        //Step 11: Verify the success message after parcels are generated
        softAssert.assertContains(objMappingPage.getSuccessMessage(),"Please Review Spatial Information",
                "Validation that success message is displayed when Parcels are generated");

        //Step 12: Verify the grid cells are not editable after parcels are generated
        HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
        boolean actionColumn = gridDataHashMap.containsKey("Action");
        softAssert.assertTrue(!actionColumn,"Validation that columns should not be editable as Action column has disappeared after generating parcels");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN")," Validation that APN column should not be editable after generating parcels");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Legal Description")," Validation that Legal Description column should not be editable after generating parcels");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA"),"Validation that TRA column should not be editable after generating parcels");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs")," Validation that Situs column should not be editable after generating parcels");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Reason Code"),"Validation that Reason Code column should not be editable after generating parcels");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("District/Neighborhood")," Validation that District/Neighborhood column should not be editable after generating parcels");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code")," Validation that Use Code column should not be editable after generating parcels");
       
        //Step 13: Open Parent APN and verify Target Relationship details
        gridDataHashMap.get("APN").stream().forEach(parcel -> {
        	try {
				objMappingPage.Click(objMappingPage.getButtonWithText(parcel));
				objMappingPage.waitUntilPageisReady(driver);
				objMappingPage.waitForElementToBeVisible(60, objParcelsPage.moretab);
				
				objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelRelationshipsTabLabel);
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(apn1)), "SMAB-T2757: Verify Parent Parcel: "+apn1+" is visible under Source Parcel Relationships section");				
				driver.navigate().back();
        	}
        	catch(Exception e) {
        		ExtentTestManager.getTest().log(LogStatus.FAIL, "Fail to validate Parent Parcel under Source Parcel Relationships section"+e);
        	}
        });
        
        //Step 14: Verify Status of Parent & Child Parcels before WI completion
        HashMap<String, ArrayList<String>> parentAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn1);
        HashMap<String, ArrayList<String>> childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(0));
  		softAssert.assertEquals(parentAPN1Status.get("Status__c").get(0),"In Progress - To Be Expired","SMAB-T2759: Verify Status of Parent Parcel: "+apn1);
  		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2759: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(0));
  		
		//Step 15: Verify Neighborhood Code value is inherited from Parent to Child Parcels
  		HashMap<String, ArrayList<String>> parentAPNNeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",apn1);
  		HashMap<String, ArrayList<String>> childAPN1NeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(parentAPNNeighborhoodCode.get("Name").get(0),childAPN1NeighborhoodCode.get("Name").get(0),"SMAB-T2760: Verify District/Neighborhood Code of Child Parcel is inheritted from first Parent Parcel");

		//Step 16: Verify TRA value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNTRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",apn1);
		HashMap<String, ArrayList<String>> childAPN1TRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(parentAPNTRA.get("Name").get(0),childAPN1TRA.get("Name").get(0),"SMAB-T2760: Verify TRA of Child Parcel is inheritted from first Parent Parcel");

		//Step 17: Verify District value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNDistrict = objParcelsPage.fetchFieldValueOfParcel("District__c",apn1);
		HashMap<String, ArrayList<String>> childAPN1District = objParcelsPage.fetchFieldValueOfParcel("District__c",gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(parentAPNDistrict.get("District__c").get(0),childAPN1District.get("District__c").get(0),"SMAB-T2760: Verify District of Child Parcel is inheritted from first Parent Parcel");
		
		//Step 18: Verify Primary Situs value is inherited from Parent to Child Parcels
  		HashMap<String, ArrayList<String>> parentAPNPrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",apn1);
  		HashMap<String, ArrayList<String>> childAPN1PrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",gridDataHashMap.get("APN").get(0));
  		softAssert.assertEquals(parentAPNPrimarySitus.get("Name").get(0),childAPN1PrimarySitus.get("Name").get(0),"SMAB-T2760: Verify Primary Situs of Child Parcel is inheritted from first Parent Parcel");
		
		
		//Step 19: Mark the WI complete
		String query = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
		salesforceAPI.update("Work_Item__c", query, "Status__c", "Completed");
		
		//Step 20: Verify Status of Parent & Child Parcels after WI completion
		parentAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn1);
		childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(0));
  		softAssert.assertEquals(parentAPN1Status.get("Status__c").get(0),"Retired","SMAB-T2761: Verify Status of Parent Parcel: "+apn1);
  		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"Active","SMAB-T2761: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(0));
        
  		//Step 21: Verify 2 new WIs are generated and linked to Child Parcels after parcel is split and WI is completed
		String queryToGetRequestType = "SELECT Work_Item__r.Request_Type__c FROM Work_Item_Linkage__c Where Parcel__r.Name = '"+gridDataHashMap.get("APN").get(0)+"' OR Parcel__r.Name = '"+gridDataHashMap.get("APN").get(1)+"'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryToGetRequestType);
		int expectedWorkItemsGenerated = response.get("Work_Item__r").size();
		softAssert.assertEquals(expectedWorkItemsGenerated,4,"SMAB-T2758: Verify 2 new Work Items are generated and linked to each child parcel after many to many mapping action is performed and WI is completed");

		softAssert.assertContains(response.get("Work_Item__r").get(0),"New APN - Update Characteristics & Verify PUC","SMAB-T2758: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
		softAssert.assertContains(response.get("Work_Item__r").get(1),"New APN - Allocate Value","SMAB-T2758: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
		softAssert.assertContains(response.get("Work_Item__r").get(2),"New APN - Update Characteristics & Verify PUC","SMAB-T2758: Verify Request Type of 2 new Work Items generated that are linked to each many to many mapping action is performed and WI is completed");
		softAssert.assertContains(response.get("Work_Item__r").get(3),"New APN - Allocate Value","SMAB-T2758: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
  		
		driver.switchTo().window(parentWindow);
		
		//Step 22: Open Parent APN and verify Target Relationship details
		responseAPNDetails.get("Name").stream().forEach(parcel -> {
        	try {
        		objMappingPage.searchModule(PARCELS);
                objMappingPage.globalSearchRecords(parcel);
				objMappingPage.waitForElementToBeVisible(60, objParcelsPage.moretab);
			
				objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelRelationshipsTabLabel);
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(gridDataHashMap.get("APN").get(0))), "SMAB-T2757: Verify Child Parcel: "+gridDataHashMap.get("APN").get(0)+" is visible under Target Parcel Relationships section");
				driver.navigate().back();
        	}
        	catch(Exception e) {
        		ExtentTestManager.getTest().log(LogStatus.FAIL, "Fail to validate Parent Parcel under Source Parcel Relationships section"+e);
        	}
        });
		// Step 23: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(apn1);

        // Step 24: Creating Manual work item for the Parcel
         workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

        //Step 25: Clicking the  details tab for the work item newly created and clicking on Related Action Link
        objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
        objWorkItemHomePage.waitForElementToBeVisible(40,objWorkItemHomePage.referenceDetailsLabel);
        objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
         parentWindow = driver.getWindowHandle();
        objWorkItemHomePage.switchToNewWindow(parentWindow);
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

         //Step 26: Selecting Action as 'Many To Many' & Taxes Paid fields value as 'N/A'
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,"BOE Activation");
		objMappingPage.enter("First non-Condo Parcel Number","123456789");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		Thread.sleep(2000);
		//Step 27: Validating that -In order to proceed with BOE Activation, the parent parcel (s) should not have been previously activated.
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"-In order to proceed with BOE Activation, the parent parcel (s) should not have been previously activated.",
						"SMAB-T2687: Validation that Warning: -In order to proceed with BOE Activation, the parent parcel (s) should not have been previously activated.");

        objWorkItemHomePage.logout();
    }
    
}
