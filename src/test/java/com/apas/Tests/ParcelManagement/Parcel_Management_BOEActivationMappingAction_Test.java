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


public class Parcel_Management_BOEActivationMappingAction_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	JSONObject jsonObject= new JSONObject();
	MappingPage objMappingPage;
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

	/**
	 * This method is to Verify that User is able to view various error messages while perform a "BOE Activation" mapping action from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2748,SMAB-T2689,SMAB-T2688,SMAB-T2754,SMAB-T2749,:Verify that User is able to view the various error message during BOE Activation mapping Action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","Smoke" })
	public void ParcelManagement_VerifyErrorMessagesInBOEActivationMappingAction(String loginUser) throws Exception {
		
		//Fetching parcel that is Retired 		
		String queryAPNValue = "SELECT name from Parcel__c where Status__c='Retired' Limit 1 ";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);
		System.out.println("Logger"+retiredAPNValue);
		objMappingPage.deleteRelationshipInstanceFromParcel(retiredAPNValue);
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
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"- In order to proceed with this action, the parent parcel(s) must be Retired.",
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

		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.firstNonCondoTextBoxLabel),"123456789");

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		//Step 9: Validation that Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789
		softAssert.assertContains(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
				"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789",
						"SMAB-T2754,SMAB-T2689: Validation that Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789");

		//Step 10 :Clicking generate parcel button
	    objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
	    softAssert.assertContains(objMappingPage.confirmationMsgOnSecondScreen(),"pending verification from the supervisor in order to be activated.",
				"SMAB-T2688: Validate that User is able to perform BOE Activation action for one retired parcel");
	    driver.switchTo().window(parentWindow);
		objMappingPage.logout();

	}
	/**
     * This method is to Verify that User is able to perform output validations for "BOE Activation" mapping action for a Parcel (Active) from a work item
     * @param loginUser
     * @throws Exception
     */
    @Test(description = "SMAB-T3818,SMAB-T3737,SMAB-T2757,SMAB-T2758,SMAB-T2759,SMAB-T2760,SMAB-T2761,SMAB-T2687,SMAB-T3245:"
    		+ "Verify the Output validations for \"BOE Activation\" mapping action for a Parcel (retired) from a work item",
    		dataProvider = "loginMappingUser",
    		dataProviderClass = DataProviders.class, 
    		groups = {"Regression","ParcelManagement" })
    public void ParcelManagement_VerifyBOEActivationMappingActionOutputValidations(String loginUser) throws Exception {

        // Step 1: Fetching parcels that are Active with no Ownership record
    	String queryAPNValue = "SELECT Name,Id from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		System.out.println(response);
		String retiredAPNValue= response.get("Name").get(0);		
		objMappingPage.deleteRelationshipInstanceFromParcel(retiredAPNValue);
        //step 2: getting Neighborhood and tra value
        String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c","");
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
        // Step 3: update  values on Parcels
		salesforceAPI.update("Parcel__c",response.get("Id").get(0),jsonObject);
	
        String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
                "DataToCreateWorkItemOfTypeParcelManagement");

        String mappingActionCreationData =  testdata.BOEACtivation_MAPPING_ACTION;
		Map<String, String> hashMapBOEACtivationMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBOEMappingActionWithAllFields");
        // Step 4: Login to the APAS application using the credentials passed through data provider
        objMappingPage.login(loginUser);

        // Step 5: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(retiredAPNValue);
        
        //Fetching PUC of parent before BOE Action
        String parentAPNPucBeforeAction = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");

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
        Thread.sleep(3000);
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"456798123");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.generateParcelButton);
		//Step 9: Validating that parcel generated is different 
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 456-789-123 for Non-Condo Parcel.",
						"Validation that Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 123-456-789");
		//Step 10: generate  new child parcels 
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
        Thread.sleep(2000);
        softAssert.assertContains(objMappingPage.getErrorMessage(),"The district and neighborhood is required in order to proceed",
				"SMAB-T3737: Verify that for all mapping actions the \"District/Neighborhood\" must be mandatory "
				+ "and error msg should be displayed on generating parcel if District/Neighborhood "
				+ "is empty");
          
        objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
        objMappingPage.editActionInMappingSecondScreen(hashMapBOEACtivationMappingData);
        
		objMappingPage.waitForElementToBeVisible(20, objMappingPage.generateParcelButton);
        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));


        //Step 11: Verify the success message after parcels are generated
        softAssert.assertContains(objMappingPage.getSuccessMessage(),"is pending verification from the supervisor in order to be activated.",
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
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd")," Validation that District/Neighborhood column should not be editable after generating parcels");
        softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code")," Validation that Use Code column should not be editable after generating parcels");
       
        //Step 13: Open Parent APN and verify Target Relationship details
        gridDataHashMap.get("APN").stream().forEach(parcel -> {
        	try {
				objMappingPage.Click(objMappingPage.getButtonWithText(parcel));
				objMappingPage.waitUntilPageisReady(driver);
				objMappingPage.waitForElementToBeVisible(60, objParcelsPage.moretab);
				
				objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelRelationshipsTabLabel);
				ReportLogger.INFO("Parent Parcel: "+retiredAPNValue+"is visible under Source Parcel Relationships section");
				softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(retiredAPNValue)), "SMAB-T2757: Verify Parent Parcel: "+retiredAPNValue+" is visible under Source Parcel Relationships section");				
				driver.navigate().back();
        	}
        	catch(Exception e) {
        		ExtentTestManager.getTest().log(LogStatus.FAIL, "Fail to validate Parent Parcel under Source Parcel Relationships section"+e);
        	}
        });
        
        //Step 14: Verify Status of Parent & Child Parcels before WI completion
        ReportLogger.INFO("validate status of parent and child parcels");
        HashMap<String, ArrayList<String>> parentAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",retiredAPNValue);
        HashMap<String, ArrayList<String>> childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(0));
  		softAssert.assertEquals(parentAPN1Status.get("Status__c").get(0),"In Progress - To Be Expired","SMAB-T2759,SMAB-T3245: Verify Status of Parent Parcel: "+retiredAPNValue);
  		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"In Progress - New Parcel","SMAB-T2759,SMAB-T3245: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(0));
  		
  	    //Fetching required PUC's of parent and child after BOE Action
  		String childAPNNumber = gridDataHashMap.get("APN").get(0);
  		String childAPNPucFromGrid = gridDataHashMap.get("Use Code*").get(0);
  		driver.switchTo().window(parentWindow);
        objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(childAPNNumber);
		String childParcelPuc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
		objMappingPage.globalSearchRecords(retiredAPNValue);
		String parentAPNPuc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
				
	    softAssert.assertEquals(parentAPNPuc,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Parent Parcel:"+retiredAPNValue);
	    softAssert.assertEquals(childAPNPucFromGrid,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Child Parcel:"+childAPNNumber);
		
		//Step 15: Verify Neighborhood Code value is inherited from Parent to Child Parcels
  		HashMap<String, ArrayList<String>> parentAPNNeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",retiredAPNValue);
  		HashMap<String, ArrayList<String>> childAPN1NeighborhoodCode = objParcelsPage.fetchFieldValueOfParcel("Neighborhood_Reference__c",gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(parentAPNNeighborhoodCode.get("Name").get(0),childAPN1NeighborhoodCode.get("Name").get(0),"SMAB-T2760: Verify District/Neighborhood Code of Child Parcel is inheritted from first Parent Parcel");

		//Step 16: Verify TRA value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNTRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",retiredAPNValue);
		HashMap<String, ArrayList<String>> childAPN1TRA = objParcelsPage.fetchFieldValueOfParcel("TRA__c",gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(parentAPNTRA.get("Name").get(0),childAPN1TRA.get("Name").get(0),"SMAB-T2760: Verify TRA of Child Parcel is inheritted from first Parent Parcel");

		//Step 17: Verify District value is inherited from Parent to Child Parcels
		HashMap<String, ArrayList<String>> parentAPNDistrict = objParcelsPage.fetchFieldValueOfParcel("District__c",retiredAPNValue);
		HashMap<String, ArrayList<String>> childAPN1District = objParcelsPage.fetchFieldValueOfParcel("District__c",gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(parentAPNDistrict.get("District__c").get(0),childAPN1District.get("District__c").get(0),"SMAB-T2760: Verify District of Child Parcel is inheritted from first Parent Parcel");
		
		//Step 18: Verify Primary Situs value is inherited from Parent to Child Parcels
  		HashMap<String, ArrayList<String>> parentAPNPrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",retiredAPNValue);
  		HashMap<String, ArrayList<String>> childAPN1PrimarySitus = objParcelsPage.fetchFieldValueOfParcel("Primary_Situs__c",gridDataHashMap.get("APN").get(0));
  		softAssert.assertEquals(parentAPNPrimarySitus.get("Name").get(0),childAPN1PrimarySitus.get("Name").get(0),"SMAB-T2760: Verify Primary Situs of Child Parcel is inheritted from first Parent Parcel");
		
		
		//Step 19: Mark the WI complete
		String query = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
		salesforceAPI.update("Work_Item__c", query, "Status__c", "Submitted for Approval");
		objMappingPage.searchModule(PARCELS);
		String childAPN = gridDataHashMap.get("APN").get(0);
        objMappingPage.globalSearchRecords(childAPN);
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),
				objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelDistrictNeighborhood, "Summary Values"),
				"SMAB-T3818: Parcel Management- Verify that for all relevant mapping actions the"
						+ " \"District/Neighborhood\" must be mandatory and should be inherited in child parcel");

        objWorkItemHomePage.logout();
        objMappingPage.login(users.MAPPING_SUPERVISOR);
        Thread.sleep(5000);
        objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItemNumber);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		driver.navigate().refresh(); //refresh as the focus is getting lost
		Thread.sleep(5000);
		objWorkItemHomePage.completeWorkItem();
		
		//Step 20: Verify Status of Parent & Child Parcels after WI completion
		parentAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",retiredAPNValue);
		childAPN1Status = objParcelsPage.fetchFieldValueOfParcel("Status__c",gridDataHashMap.get("APN").get(0));
  		softAssert.assertEquals(parentAPN1Status.get("Status__c").get(0),"Retired","SMAB-T2761,SMAB-T3245: Verify Status of Parent Parcel: "+retiredAPNValue);
  		softAssert.assertEquals(childAPN1Status.get("Status__c").get(0),"Active","SMAB-T2761,SMAB-T3245: Verify Status of Child Parcel: "+gridDataHashMap.get("APN").get(0));
  		
  	    //Fetching required PUC's of parent and child after closing WI
        objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(childAPNNumber);
		childParcelPuc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
		objMappingPage.globalSearchRecords(retiredAPNValue);
		parentAPNPuc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
			
		softAssert.assertEquals(parentAPNPuc,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Parent Parcel:"+retiredAPNValue);
		softAssert.assertEquals(childParcelPuc,parentAPNPucBeforeAction,"SMAB-T3245:Verify PUC of Child Parcel:"+childAPNNumber);
        
  		//Step 21: Verify 2 new WIs are generated and linked to Child Parcels after parcel is split and WI is completed
  		String queryToGetRequestType = "SELECT Work_Item__r.Request_Type__c FROM Work_Item_Linkage__c Where Parcel__r.Name = '"+gridDataHashMap.get("APN").get(0)+"' ";
		HashMap<String, ArrayList<String>> responseRequestType = salesforceAPI.select(queryToGetRequestType);
		int expectedWorkItemsGenerated = responseRequestType.get("Work_Item__r").size();
		softAssert.assertEquals(expectedWorkItemsGenerated,1,"SMAB-T2717: Verify 2 new Work Items are generated and linked to each child parcel after one to one mapping action is performed and WI is completed");
       // currently Allocate value is not genrated as part of new story so removed asseration for that
		softAssert.assertContains(responseRequestType,"New APN - Update Characteristics & Verify PUC","SMAB-T2717: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
		
		driver.switchTo().window(parentWindow);
		
		//Step 22: Open Parent APN and verify Target Relationship details
		response.get("Name").stream().forEach(parcel -> {
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
        objMappingPage.globalSearchRecords(retiredAPNValue);

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
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"123456789");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		Thread.sleep(2000);
		//Step 27: Validating that -In order to proceed with BOE Activation, the parent parcel (s) should not have been previously activated.
		softAssert.assertContains(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"- In order to proceed with BOE Activation, the parent parcel(s) should not have been previously activated.",
						"SMAB-T2687: Validation that Warning: -In order to proceed with BOE Activation, the parent parcel (s) should not have been previously activated.");

		driver.switchTo().window(parentWindow);
        objWorkItemHomePage.logout();
    }
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "BOEACtivation" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2898:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"BOEACtivation\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_BOEACtivationMappingAction(String loginUser) throws Exception {
		String childAPNPUC;
		
		String queryAPN = "Select name From Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String parentAPN=responseAPNDetails.get("Name").get(0);
		objMappingPage.deleteRelationshipInstanceFromParcel(parentAPN);
		
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.BOEACtivation_MAPPING_ACTION;
		Map<String, String> hashMapBOEACtivationMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBOEMappingActionWithAllFields");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(parentAPN);

		// Step 3: Creating Manual work item for the Parcel

		String workItem=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link

		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel BOEACtivation' 
		objMappingPage.waitForElementToBeVisible(3, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBOEACtivationMappingData.get("Action"));
		

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapBOEACtivationMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);

		//Step 7: Click generate Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);
		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+parentAPN+"') limit 1");
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
		Thread.sleep(7);
		
		//Step 9: Validation that User is navigated to a screen with following fields:APN
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2898: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertTrue(!objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2898: Validation that  There is No \"Update Parcel(s)\" button on return to custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
						"SMAB-T2898: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2898: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
						"SMAB-T2898: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
						"SMAB-T2898: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
						"SMAB-T2898: Validation that  System populates Use Code that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2898: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");

		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2898: Validation that APN column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Legal Description*"),"SMAB-T2898: Validation that Legal Description column on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*"),"SMAB-T2898: Validation that TRA column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2898: Validation that Situs column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Reason Code*"),"SMAB-T2898: Validation that Reason Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*"),"SMAB-T2898: Validation that District/Neighborhood column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*"),"SMAB-T2898: Validation that Use Code column should not be editable on retirning to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	
	@Test(description = "SMAB-T2832,SMAB-T2898,SMAB-T3623,SMAB-T3634,SMAB-T3771:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"BOEACtivation\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_BOEACtivation_MappingAction_IndependentMappingActionWI(String loginUser) throws Exception {
		String childAPNPUC;

		//Fetching parcel that is Retired 		
		String queryAPN = "Select name From Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String parentAPN=responseAPNDetails.get("Name").get(0);
		objMappingPage.deleteRelationshipInstanceFromParcel(parentAPN);  
		objMappingPage.deleteCharacteristicInstanceFromParcel(parentAPN);
		
		String mappingActionCreationData =  testdata.BOEACtivation_MAPPING_ACTION;
		Map<String, String> hashMapBOEACtivationMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBOEMappingActionWithAllFields");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);
		Thread.sleep(7000);
		objMappingPage.closeDefaultOpenTabs();
		
				
		// Step2: Opening the PARCELS page  and searching the  parcel 	
		objMappingPage.globalSearchRecords(parentAPN);
		objParcelsPage.createParcelSitus(parentAPN);
		objMappingPage.searchModule("APAS");
		objMappingPage.searchModule("Mapping Action");
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);

		//Step 3: Selecting Action as 'perform parcel BOEACtivation' 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,parentAPN);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBOEACtivationMappingData.get("Action"));
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingFirstScreen(parentAPN), "SMAB-T3365 : Verify that for \"BOE Activation\" mapping action, in custom action second screen and third screen Parent APN (s) "+parentAPN+" is displayed");

		//Step 4: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapBOEACtivationMappingData);
		objMappingPage.waitForElementToBeVisible(10,objMappingPage.generateParcelButton);

		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);

		//Step 5: Click BOEACtivation Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(parentAPN), "SMAB-T3365 : Verify that for \"BOE Activation\" mapping action, in custom action second screen and third screen Parent APN (s) "+parentAPN+" is displayed");

		softAssert.assertContains(objMappingPage.confirmationMsgOnSecondScreen(),"is pending verification from the supervisor in order to be activated.",
	                "SMAB-T2832,SMAB-T3623: Validate that User is able to perform boe activation  action from mapping actions tab");

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+parentAPN+"') limit 1");
		if(responsePUCDetailsChildAPN.size()==0)
			  childAPNPUC ="";
		else
			childAPNPUC=responsePUCDetailsChildAPN.get("Name").get(0);
		

		//Step 6: Navigating  to the independent mapping action WI that would have been created after performing BOEACtivation action and clicking on related action link 
		String workItemId= objWorkItemHomePage.getWorkItemIDFromParcelOnWorkbench(parentAPN);
		String query = "SELECT Name FROM Work_Item__c where id = '"+ workItemId + "'";
		HashMap<String, ArrayList<String>> responseDetails = salesforceAPI.select(query);
		String workItem=responseDetails.get("Name").get(0);

		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		

		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type","Information"), "Mapping",
				"SMAB-T2832: Validation that  A new WI of type Mapping is created after performing one to one from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action","Information"), "Independent Mapping Action",
				"SMAB-T2832: Validation that  A new WI of action Independent Mapping Action is created after performing one to one from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(DateUtil.getCurrentDate("MM/dd/yyyy")), "SMAB-T2832: Validation that 'Date' fields is equal to date when this WI was created");
	
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		Thread.sleep(5000);
		
		//Step 7: Validation that User is navigated to a screen with following fields:APN
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2898: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2898: Validation that  There is No \"Update Parcel(s)\" button on return to custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2898: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2898: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2898: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2898: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2898: Validation that  System populates Use Code that was edited in custom screen");
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(parentAPN), "SMAB-T3365 : Verify that for \"BOE Activation\" mapping action, in custom action second screen and third screen Parent APN (s) "+parentAPN+" is displayed");

		driver.switchTo().window(parentWindow);
		
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

		// Checking the status of parent parcel after WI closed
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(childAPN);
		String Status = objMappingPage.getFieldValueFromAPAS("Status", "Parcel Information");
		softAssert.assertEquals(Status, "Active", "SMAB-T3623: Verify Status of Parcel:" + childAPN);
		objWorkItemHomePage.logout();

		ReportLogger.INFO(" Appraiser logins ");
		objMappingPage.login(users.RP_APPRAISER);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(childAPN);
		objParcelsPage.Click(objParcelsPage.workItems);
		
		//Moving to the Update Characteristics Verify PUC WI
		objParcelsPage.Click(objParcelsPage.updateCharacteristicsVerifyPUC);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(40, objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objParcelsPage.Click(objMappingPage.getButtonWithText("Next"));
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Done"));
		ReportLogger.INFO("Update Characteristics Verify PUC WI Completed");

		driver.switchTo().window(parentWindow);
		driver.navigate().refresh();
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		workItemStatus = objMappingPage.getFieldValueFromAPAS("Status", "Information");
		softAssert.assertEquals(workItemStatus, "Completed", "SMAB-T3771: Validation WI completed successfully");
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(childAPN);
		
		//Moving to Allocate Values WI
		objParcelsPage.Click(objParcelsPage.workItems);
		objParcelsPage.Click(objParcelsPage.allocateValue);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		String assignedTo = objMappingPage.getFieldValueFromAPAS("Assigned To", "Information");
		String workPool = objMappingPage.getFieldValueFromAPAS("Work Pool", "Information");
		softAssert.assertEquals(assignedTo, "rp appraiserAUT", "SMAB-T3771:Assiged to is matched successfully");
		softAssert.assertEquals(workPool, "Appraiser", "SMAB-T3771:WorkPool is matched successfully");

		objWorkItemHomePage.logout();

	}
	
	
	 /**
		 * This method is to  Verify  the custom edit on mapping page
		 *@param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T2839,SMAB-T2844,SMAB-T2733,SMAB-T2767,SMAB-T2910,SMAB-T3473,SMAB-T2909,SMAB-T3474,SMAB-T3475: I need to have the ability to select specific fields from the mapping custom screen, so that the correct values can be assigned to the parcels. ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
				"Smoke","Regression","ParcelManagement" },enabled = true)
		public void ParcelManagement_VerifyBOEParcelEditAction(String loginUser) throws Exception {
			String queryAPN = "SELECT Id, Name FROM Parcel__c WHERE (Not Name like '%990') and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and  Status__c = 'Retired' Limit 1";
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
			String apn1=responseAPNDetails.get("Name").get(0);
					
			String PUC = salesforceAPI.select("SELECT Name FROM PUC_Code__c  limit 1").get("Name").get(0);
    	    String TRA=salesforceAPI.select("SELECT Name FROM TRA__c limit 1").get("Name").get(0); 
    	    
    	 	//Fetching parcels that are Active with different map book and map page
    	   	String mapBookForAPN1 = apn1.split("-")[0];
    	   	String mapPageForAPN1 = apn1.split("-")[1];		
    	   	queryAPN = "SELECT Id, Name FROM Parcel__c WHERE (Not Name like '%990') and (Not Name like '"+mapBookForAPN1+"%') and (Not Name like '"+mapBookForAPN1+"-"+mapPageForAPN1+"%') and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Retired' Limit 2";
    	   	HashMap<String, ArrayList<String>> responseAPN2Details = salesforceAPI.select(queryAPN);
    	   	String apn2=responseAPN2Details.get("Name").get(0);
    	   	String apn3=responseAPN2Details.get("Name").get(1);
    	   	

    	   	//Deleting Relationship Instance
    	   	objMappingPage.deleteRelationshipInstanceFromParcel(apn2);
    	   	objMappingPage.deleteRelationshipInstanceFromParcel(apn3);
    	   	objMappingPage.deleteRelationshipInstanceFromParcel(apn1);
    	   	
    	   	objMappingPage.deleteCharacteristicInstanceFromParcel(apn2);
    	   	objMappingPage.deleteCharacteristicInstanceFromParcel(apn3);
    	   	
    	   	
    	   	String concatenateAPNWithDifferentMapBookMapPage = apn2+","+apn3;
    	   	
    	   	//Add the parcels in a Hash Map for validations later
    	   	Map<String,String> apnValue = new HashMap<String,String>(); 
    	   	apnValue.put("APN1", apn1); 
    	   	apnValue.put("APN2", apn2);
    	   	apnValue.put("APN3", apn3);

			String mappingActionCreationData =  testdata.BOEACtivation_MAPPING_ACTION;

			Map<String, String> hashMapBOEParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformBOEMappingActionWithAllFields");
			String situsCityName = hashMapBOEParcelMappingData.get("City Name");
			String direction = hashMapBOEParcelMappingData.get("Direction");
			String situsNumber = hashMapBOEParcelMappingData.get("Situs Number");
			String situsStreetName = hashMapBOEParcelMappingData.get("Situs Street Name");
			String situsType = hashMapBOEParcelMappingData.get("Situs Type");
			String situsUnitNumber = hashMapBOEParcelMappingData.get("Situs Unit Number");
			String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;


			String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");
			// Step1: Login to the APAS application using the credentials passed through data provider (mapping staff user)
			objMappingPage.login(loginUser);

			// Step 2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
			objMappingPage.searchModule(PARCELS);
			objMappingPage.globalSearchRecords(apn1);
			
			//Step 3: Creating Manual work item for the Parcel 
			String workItem = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

			//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
			Thread.sleep(3000);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
			String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();
			objWorkItemHomePage.switchToNewWindow(parentWindow);
			objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
			ReportLogger.INFO("Add a parcel with different Map Book and Map Page in Parent APN field :: "
					+ concatenateAPNWithDifferentMapBookMapPage);
			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, concatenateAPNWithDifferentMapBookMapPage);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
			objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel, "BOE Activation");
			objMappingPage.waitForElementToBeVisible(objMappingPage.reasonCodeField);
			softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), "value"),reasonCode,
					"SMAB-T2839: Validation that reason code field is auto populated from parent parcel work item");
			
			objMappingPage.fillMappingActionForm(hashMapBOEParcelMappingData);
			HashMap<String,ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();
			Thread.sleep(3000);
			for (int i = 1; i <= gridDataHashMap.get("APN").size(); i++) {

				objMappingPage.Click(
						objMappingPage.locateElement("//tr[" + i + "]" + objMappingPage.secondScreenEditButton, 2));
				objMappingPage.editActionInMappingSecondScreen(hashMapBOEParcelMappingData);
				objMappingPage.waitForElementToBeVisible(10, objMappingPage.generateParcelButton);
			}
			objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
			
			String MappingScreen = driver.getWindowHandle();
			ReportLogger.INFO("Validate the Grid values");
			HashMap<String, ArrayList<String>> gridDataHashMapAfterEditAction = objMappingPage.getGridDataInHashMap();
			String childAPNNumber = gridDataHashMapAfterEditAction.get("APN").get(0);
			// Verifying new situs,TRA ,use code is populated in grid table
			softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Situs").get(0), childprimarySitus,
					"SMAB-T2839,SMAB-T2844: Validation that System populates Situs from the parent parcel");
			softAssert.assertEquals(gridDataHashMapAfterEditAction.get("TRA*").get(0), TRA,
					"SMAB-T2839,SMAB-T2844: Validation that System populates TRA from the parent parcel");
			softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Use Code*").get(0), PUC,
					"SMAB-T2839,SMAB-T2844: Validation that System populates TRA from the parent parcel");
			ReportLogger.INFO("Click on Combine Parcel button");
		    
		    driver.switchTo().window(parentWindow);
		    objMappingPage.searchModule(WORK_ITEM);
		   	objMappingPage.globalSearchRecords(workItem);
		   	driver.navigate().refresh();//refresh as the focus is getting lost
		   	Thread.sleep(5000);
		   	objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		   	objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		   	
		   	softAssert.assertEquals(1,objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI,10).size(),
		   			"SMAB-T2733,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that only 1 APN is linked to Work Item");
		   	softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
		   			"SMAB-T2733,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that first Parent APN is displayed in the linked item");
		   	
		   	driver.switchTo().window(MappingScreen);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
			Thread.sleep(3000);
			softAssert.assertContains(objMappingPage.confirmationMsgOnSecondScreen(),"pending verification from the supervisor",
					"SMAB-T2839,SMAB-T2844: Validate that User is able to perform Combine action for multiple active parcels");			    
		    
		    driver.switchTo().window(parentWindow);
		    objMappingPage.searchModule(PARCELS);
			
			objMappingPage.globalSearchRecords(childAPNNumber);
			//Validate the Situs of child parcel generated
		    softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus, "Parcel Information"),childprimarySitus,
					"SMAB-T2844,SMAB-T2839: Validate the Situs of child parcel generated");
				
			//Step 9: Submit the WI for approval and validate the linked parcels to the WI
	
		   	objMappingPage.searchModule(WORK_ITEM);
		   	objMappingPage.globalSearchRecords(workItem);
		   	objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		   	driver.navigate().refresh();
		   	objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
		   	objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		   	softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T2669:Verify user is able to submit the Work Item for approval");
		   	
		   	objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		   	objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		   	ReportLogger.INFO("validate that new APNs added are linked to WI after Mapping Action is performed");
		   	softAssert.assertEquals(2,objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI,10).size(),
					"SMAB-T2733,SMAB-T2767,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that 2 APNs are linked to Work Item");
			softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
					"SMAB-T2733,SMAB-T2767,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that first Parent APN is displayed in the linked item");
			softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("1")),
					"SMAB-T2733,SMAB-T2767,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that second Parent APN is displayed in the linked item");
			objMappingPage.Click(objWorkItemHomePage.detailsTab);
			String referenceURl= objMappingPage.getFieldValueFromAPAS("Navigation Url", "Reference Data Details");
			softAssert.assertTrue(referenceURl.contains(concatenateAPNWithDifferentMapBookMapPage),
					"SMAB-T2910,SMAB-T3473: Validate that Parent APNs are present in the reference Link");

		   	
		   	objWorkItemHomePage.logout();
		   	Thread.sleep(5000);
		   	
		   	//Step 10: Login from Mapping Supervisor to approve the WI
		   	ReportLogger.INFO("Now logging in as RP Appraiser to approve the work item and validate that new WIs are accessible");
		   	objWorkItemHomePage.login(MAPPING_SUPERVISOR);
		   	objMappingPage.searchModule(WORK_ITEM);
		   	objMappingPage.globalSearchRecords(workItem);
		   	objWorkItemHomePage.completeWorkItem();
		   	softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Completed","SMAB-T2669:Verify user is able to complete the Work Item");
		   	
		   	objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		   	objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);
		   	
		   	softAssert.assertEquals(2,objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI,10).size(),
					"SMAB-T2733,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that 2 APNs are linked to Work Item");
			softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
					"SMAB-T2733,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that first Parent APN is displayed in the linked item");
			softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("1")),
					"SMAB-T2733,SMAB-T2909,SMAB-T3474,SMAB-T3475: Validate that second Parent APN is displayed in the linked item");
			objMappingPage.Click(objWorkItemHomePage.detailsTab);
			referenceURl= objMappingPage.getFieldValueFromAPAS("Navigation Url", "Reference Data Details");
			softAssert.assertTrue(referenceURl.contains(concatenateAPNWithDifferentMapBookMapPage),
					"SMAB-T2910,SMAB-T3473: Validate that all Parent APNs are present in the reference Link");

			objWorkItemHomePage.logout();

		}
		/**
		 * This method is to  Verify WI rejection on BOE mapping action
		 *@param loginUser
		 * @throws Exception
		 */		
		@Test(description = "SMAB-T3464:Verify the Output validations for \"BOE Activation\" mapping action for a Parcel (retired) after rejected the work item ",
	    		dataProvider = "loginMappingUser",
	    		dataProviderClass = DataProviders.class, 
	    		groups = {"Regression","ParcelManagement" })
	    public void ParcelManagement_VerifyWIRejectionAfterPerformBOEActivationMappingAction(String loginUser) throws Exception {

	        // Step 1: Fetching parcels that are Active with no Ownership record
	    	String queryAPNValue = "SELECT Name,Id from Parcel__c where Status__c='Retired' Limit 1 ";
			HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
			String retiredAPNValue= response.get("Name").get(0);		
			objMappingPage.deleteRelationshipInstanceFromParcel(retiredAPNValue);
	        
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
			salesforceAPI.update("Parcel__c",response.get("Id").get(0),jsonObject);
		
	        String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
	        Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
	                "DataToCreateWorkItemOfTypeParcelManagement");

	        // Step 4: Login to the APAS application using the credentials passed through data provider
	        objMappingPage.login(loginUser);

	        // Step 5: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
	        objMappingPage.searchModule(PARCELS);
	        objMappingPage.globalSearchRecords(retiredAPNValue);

	        // Step 6: Creating Manual work item for the Parcel
	        String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

	        //Step 7: Clicking the  details tab for the work item newly created and clicking on Related Action Link
	        objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
	        objWorkItemHomePage.waitForElementToBeVisible(40,objWorkItemHomePage.referenceDetailsLabel);
	        objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
	        String parentWindow = driver.getWindowHandle();
	        objWorkItemHomePage.switchToNewWindow(parentWindow);
	        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

	         //Step 8: Selecting Action as 'BOE Activation' & Taxes Paid fields value as 'N/A'
	        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,"BOE Activation");
	        objMappingPage.waitForElementToBeVisible(60, objMappingPage.firstNonCondoTextBoxLabel);
			objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,"123456789");
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		
			//Step 9: generate  new child parcels 
	        objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

	        //Step 10: Verify the grid cells are not editable after parcels are generated
	        HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
			
			//Step 11: Mark the WI complete
			String query = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
			salesforceAPI.update("Work_Item__c", query, "Status__c", "Submitted for Approval");
			driver.switchTo().window(parentWindow);
	        objWorkItemHomePage.logout();
	        Thread.sleep(5000);
	        
	        // step 12: login with supervisor
	        objMappingPage.login(users.MAPPING_SUPERVISOR);
	        
	        objWorkItemHomePage.rejectWorkItem(workItemNumber,"Other","Reject Mapping action after submit for approval");
	        
	        // Step 13: Verify Status of Parent Parcel after WI rejected
			objMappingPage.globalSearchRecords(retiredAPNValue);
			String parentAPNStatus = objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelStatus,"Parcel Information");			    
			softAssert.assertEquals(parentAPNStatus,"Retired","SMAB-T3464: Verify Status of Parent Parcel: "+retiredAPNValue);
			
			//Step 14: Verify Child Parcels should be delete after WI rejected
			String childAPNNumber1 =gridDataHashMap.get("APN").get(0);
		    query = "SELECT Id FROM Parcel__c Where Name = '"+childAPNNumber1+ "'";
			response = salesforceAPI.select(query);
			softAssert.assertEquals(response.size(),0,"SMAB-T3464: Validate that child apn should be deleted "+childAPNNumber1+" after BOE activation Mapping Action after performing rejection of work item");
			String targetedApnquery="SELECT  Id, Target_Parcel__c FROM Parcel_Relationship__c where source_parcel__r.name='"+retiredAPNValue+ "' and   Parcel_Actions__c='BOE Activation'";
		    response = salesforceAPI.select(targetedApnquery);
			softAssert.assertEquals(response.size(),0,"SMAB-T3464: Validate that there is no parcel relationship on Parent Parcel when Rejected the Work tem after Split Mapping Action");
			
		    // Step 15 : Switch to parent window and logout
			objMappingPage.logout();
		}
		
		@Test(description = "SMAB-T3511,SMAB-T3512,SMAB-T3513:Verify that the Related Action label should"
				+ " match the Actions labels while creating WI and it should open mapping screen on clicking",
				dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, 
				groups = {"Regression","ParcelManagement","RecorderIntegration"},enabled=true)
		public void ParcelManagement_VerifyNewWIDeclofCovenantsCondRestrictionsGeneratedfromRecorderIntegrationAndBOEMappingAction(String loginUser) throws Exception {

			JSONObject jsonObject= new JSONObject();
			objMappingPage.login(users.SYSTEM_ADMIN);
			objMappingPage.searchModule(PARCELS);

			salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c"
					+ " where Sub_type__c='Decl of Covenants, Cond & Restrictions' and status__c ='In pool'", 
					"status__c","In Progress");

			//generating WI
			objtransfer.generateRecorderJobWorkItems(objMappingPage.DOC_Decl_of_Covenants_Cond_Restrictions, 1);

			String WorkItemQuery="SELECT Id,Name FROM Work_Item__c where Type__c='MAPPING'"
					+ " AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1"; 

			HashMap<String, ArrayList<String>> responseWIDetails = salesforceAPI.select(WorkItemQuery);
			String WorkItemNo=responseWIDetails.get("Name").get(0);		


			//Searching for the WI genrated
			objMappingPage.globalSearchRecords(WorkItemNo); 
			String ApnfromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);

			objMappingPage.deleteRelationshipInstanceFromParcel(ApnfromWIPage);
			objMappingPage.deleteCharacteristicInstanceFromParcel(ApnfromWIPage);

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

			jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
			jsonObject.put("Status__c","Retired");
			jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
			jsonObject.put("District__c",districtValue);
			jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
			jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));

			//updating APN details
			String query = "Select Id from Parcel__c where Name = '"+ApnfromWIPage+"'";
			salesforceAPI.update("Parcel__c",query,jsonObject);
			objMappingPage.logout();

			//Mapping user logs in and perform mapping action on the WI genrated
			objMappingPage.login(loginUser);
			String mappingActionCreationData = testdata.BOEACtivation_MAPPING_ACTION;
			Map<String, String> hashMapBOEMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformBOEMappingActionWithAllFields");
			objMappingPage.globalSearchRecords(WorkItemNo);
			Thread.sleep(5000);
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);

			softAssert.assertTrue(!(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiEventId).equals(" ")) ,
					"SMAB-T3513: Verfiying the Event ID of WI genrated for given Recorded Document");

			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS
					(objWorkItemHomePage.wiRelatedActionDetailsPage),"Decl of Covenants, Cond & Restrictions" ,
					"SMAB-T3511: Verfiying the Related Action of WI genrated for given Recorded Document");

			softAssert.assertTrue(!objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.editEventIdButton),
					"SMAB-T3513-This field should not be editable.");

			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();	
			objWorkItemHomePage.switchToNewWindow(parentWindow);

			// Fill data  in mapping screen
			objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
			objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBOEMappingData.get("Action"));		
			objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel, hashMapBOEMappingData.get("Reason code"));
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

			//second screen of mapping action
			objMappingPage.waitForElementToBeClickable(10, objMappingPage.generateParcelButton);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
			objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);

			
			//second screen of mapping action
			softAssert.assertContains(objMappingPage.confirmationMsgOnSecondScreen(),
					"is pending verification from the supervisor in order to be activated",
					"SMAB-T3512: Validate that User is able to perform Retire action for one active parcel");

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