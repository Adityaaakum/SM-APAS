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
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.CIOTransferPage;
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
	AuditTrailPage trail;
	CIOTransferPage objtransfer;
	

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		 trail= new AuditTrailPage(driver);
		 objtransfer=new CIOTransferPage(driver);

	}
	/**
	 * This method is to Verify that User is able to perform a "Brand New Parcel" mapping action for a Parcel (Active) of type Non Condo from a work item
	 * @param loginUser
	 * @throws Exception
	 */

	@Test(description = "SMAB-T3049,SMAB-T3495,SMAB-T3494,SMAB-T3496,SMAB-T2663,SMAB-T2263,SMAB-T2521,SMAB-T2522,SMAB-T2537,SMAB-T2547:Verify that User is able to perform a \"Brand New Parcel\" mapping action for a Parcel (Active) of type Non Condo from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" },enabled= true)
	public void ParcelManagement_VerifyBrandNewParcelMappingActionNonCondoParcel(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where (Not Name like '1%') and (Not Name like '8%')AND Primary_Situs__c !=NULL limit 1";
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

		// Step2: Opening the PARCELS page  and searching the  parcel to perform brand new parcel mapping
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
		
		//validating related action
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Related Action", "Information"),
				hashMapmanualWorkItemData.get("Actions"),"SMAB-T3494-Verify that the Related Action"
						+ " label should match the Actions labels while creating WI and it should"
						+ " open mapping screen on clicking-Perform Other Mapping Work ");
		
		//validating Event Id in Work item screen of Action type
		String eventIDValue = objWorkItemHomePage.getFieldValueFromAPAS("Event ID", "Information");
		softAssert.assertEquals(eventIDValue.contains("Alpha"),
				true,"SMAB-T3496-Verify that the Event ID based on the mapping should be"
						+ " created and populated on the Work item record.");
				

		softAssert.assertTrue(!objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.editEventIdButton),
				"SMAB-T3496-This field should not be editable.");

		

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
		
		Thread.sleep(1000);
        
		//Step 10: Validating warning message on second screen
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageSecondScreen),
			"Warning: Parcel number generated is different from the user selection based on "
			+ "established criteria. As a reference the number provided is 456-789-123",
			"SMAB-T2537: Validation that Warning present on secound screeen ");

		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		softAssert.assertTrue(gridDataHashMap.get("Situs").get(0).isEmpty(),"SMAB-T2663: Validation that primary situs of child parcel on second screen is blank as situs was not updated in first screen");

		//Step 11 :Clicking generate parcel button
		objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
		objMappingPage.editActionInMappingSecondScreen(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		//Step 12: Validating that Parcel has been successfully created.
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),
				"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2547,SMAB-T3049: Validation that Parcel has been created successfully. Please Review Spatial Information");

		//Step 13: Validation that child parcel primary situs is blank since  situs was not updated in first screen
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertTrue(gridDataHashMap.get("Situs").get(0).isEmpty(),"SMAB-T2663: Validation that primary situs of child parcel on last screen is blank as situs was not updated in first screen");

		HashMap<String, ArrayList<String>> childPrimarySitusValue =salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ childAPNNumber +"')");
		softAssert.assertTrue(childPrimarySitusValue.isEmpty(),
				"SMAB-T2663: Validation that primary situs of child parcel is blank as situs was not updated in first screen ");
		driver.switchTo().window(parentWindow);
		
		//validate that The "Return " functionality for parcel mgmt activities should work for all these work items.
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertEquals(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName).getText(),"Update Parcel(s)",
				"SMAB-T3495-validate that The Return functionality for parcel mgmt activities should work for all these work items.");
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
		objMappingPage.deleteRelationshipInstanceFromParcel(activeParcelToPerformMapping);

		String mappingActionCreationData =  testdata.Brand_New_Parcel_MAPPING_ACTION;

		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithoutAllFields");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		// Step1: Login to the APAS application using the credentials passed through data provider (mapping staff user)
		objMappingPage.login(loginUser);

		// Step 2: Opening the PARCELS page  and searching the  parcel to perform brand new parcel mapping
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
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage
				(objMappingPage.reasonCodeTextBoxLabel,""),
				"- Please enter the required field(s) : Reason Code, First Non-Condo Parcel Number",
				"SMAB-T2527: Validation that reason code is a mandatory field");
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), "Performing Brand New Parcel mapping action");
		objMappingPage.scrollToBottomOfPage();
        
		//Step 6: Validation that proper error message is displayed if a parcel number starting from 100 is entered in non condo number field
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage
				(objMappingPage.firstNonCondoTextBoxLabel,"123-456-789"),
				"- Non Condo Parcel Number cannot start with 100-199, Please enter valid Parcel Number",
				"SMAB-T2525: Validation that proper error message is displayed if a parcel number starting from 100 is entered in non condo number field");

		//Step 7: Validation that proper  error message is displayed if an special character parcel number is entered in non condo number field
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"12#-123-3@$");
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage
				(objMappingPage.firstNonCondoTextBoxLabel,"abc123123abc"),
				"- This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2523: Validation that proper error message is displayed if an alphanummeric parcel number is entered in non condo number field");

		//Step 8: Validation that proper  error message is displayed if an alphanummeric parcel number is entered in non condo number field
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"abc123123abc");
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage
				(objMappingPage.firstNonCondoTextBoxLabel,"abc123123abc"),
				"- This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2523: Validation that proper error message is displayed if an alphanummeric parcel number is entered in non condo number field");


		//Step 9: Validation that proper  error message is displayed if parcel number  not of Nine digits is entered in non condo number field
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage
				(objMappingPage.firstNonCondoTextBoxLabel,"010123"),
				"- This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2523: Validation that proper error message is displayed if  parcel number  not of Nine digits is entered in non condo number field");
		//Step 10: entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		Thread.sleep(5000);

		//Step 11 :Verify that User is able to to create a district, Use Code for the child parcel from the custom screen after performing brand new parcel mapping action
		objMappingPage.editGridCellValue(objMappingPage.apnColumnSecondScreen,activeParcelToPerformMapping);
		objMappingPage.editGridCellValue(objMappingPage.reasonCodeColumnSecondScreen,"001vacant");
		objMappingPage.editGridCellValue(objMappingPage.legalDescriptionColumnSecondScreen,"Legal Discription");
		objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
		objMappingPage.editActionInMappingSecondScreen(hashMapmanualWorkItemData);

		//Step 13 :Clicking generate parcel button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		
		//Below assertion failing due to change in rules for Condo and Non-Condo parcels APN
		//Step 14: Validating the warning message
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 456-789-123",
				"SMAB-T2524: Validation that Warning: Parcel number generated is different from the user selection based on established criteria. As a reference the number provided is 456-789-123");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}
	/**

	 *  Upon completion of the "Brand New Parcel" action, the system will create the desired parcels
	 * 
	 * @param loginUser-Mapping user
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2642,SMAB-T2643,SMAB-T2644,SMAB-T3243:Verify that User is able to perform a \"Brand New Parcel\" mapping action for a Parcel   from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
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

		//  Opening the PARCELS page  and searching the  parcel to perform brand new parcel mapping
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
		objMappingPage.waitForElementToBeVisible(3, objMappingPage.legalDescriptionColumnSecondScreen);
		objMappingPage.editGridCellValue(objMappingPage.legalDescriptionColumnSecondScreen, "Legal Discription");
		objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
		objMappingPage.editActionInMappingSecondScreen(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		// Validating that Parcel has been successfully created.
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2642: Validation that Parcel has been successfully created. Please Review Spatial Information");
		
		// Retriving new APN genrated
           HashMap<String, ArrayList<String>> gridParcelData = objMappingPage.getGridDataInHashMap();
           String newCreatedApn  =   gridParcelData.get("APN").get(0);                         
           HashMap<String, ArrayList<String>> statusnewApn = objParcelsPage.fetchFieldValueOfParcel("Status__c", newCreatedApn);
             // validating status of brand new parcel           
            softAssert.assertEquals(statusnewApn.get("Status__c").get(0), "In Progress - New Parcel", "SMAB-T2643,SMAB-T3243: Verifying the status of the new parcel");
            
            //Fetching required Child PUC after BrandNew action
            String childAPNPucFromGrid = gridParcelData.get("Use Code*").get(0);
            driver.switchTo().window(parentWindow);
            objMappingPage.searchModule(PARCELS);
    		objMappingPage.globalSearchRecords(newCreatedApn);
    		String childParcelPuc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
    		
    		softAssert.assertEquals(childParcelPuc, childAPNPucFromGrid,
    				" SMAB-T3243:Verify PUC of Child parcel"+newCreatedApn);
        
          //Fetch some other values from database
    		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

    		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
    		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

    		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
    		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

    		String legalDescriptionValue="Legal PM 85/25-260";
    		String parcelSize	= "200";	

    		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
    		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
    		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
    		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
    		jsonObject.put("Lot_Size_SQFT__c",parcelSize);

    		//update parcel details
    		salesforceAPI.update("Parcel__c",salesforceAPI.select("select Id from parcel__c where name='"+newCreatedApn+"'").get("Id").get(0),jsonObject);

            //Submit work item for approval
            String query = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
            salesforceAPI.update("Work_Item__c", query, "Status__c", "Submitted for Approval");

             driver.switchTo().window(parentWindow);
            objWorkItemHomePage.logout();
            Thread.sleep(5000);
            driver.navigate().refresh();
            Thread.sleep(6000);

             objMappingPage.login(users.MAPPING_SUPERVISOR);
            objMappingPage.searchModule(WORK_ITEM);
            objMappingPage.globalSearchRecords(workItemNumber);
            objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
            driver.navigate().refresh(); //refresh as the focus is getting lost
            Thread.sleep(5000);
            

            //Completing the workItem
           objWorkItemHomePage.completeWorkItem();             	   
     	   objMappingPage.searchModule(PARCELS);
		   objMappingPage.globalSearchRecords(newCreatedApn);
		   
     		//Validating the status of the workItem 
     		 HashMap<String, ArrayList<String>> statusCompletedApn = objParcelsPage.fetchFieldValueOfParcel("Status__c",newCreatedApn);
             //Validating the status of parcel after completing WI
           softAssert.assertEquals(statusCompletedApn.get("Status__c").get(0), "Active",
        		   "SMAB-T2644,SMAB-T3243: Validating that the status of new APN is active");
           // driver.switchTo().window(parentWindow);
           
         //Fetching Child's PUC after closing WI
           childParcelPuc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
           softAssert.assertEquals(childParcelPuc, childAPNPucFromGrid,
   				" SMAB-T3243: Verify PUC of Child parcel"+newCreatedApn);
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

		//  Opening the PARCELS page  and searching the  parcel to perform brand new parcel mapping
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
		objMappingPage.waitForElementToBeVisible(3, objMappingPage.legalDescriptionColumnSecondScreen);
		objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
		objMappingPage.editActionInMappingSecondScreen(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		HashMap<String, ArrayList<String>> gridParcelData = objMappingPage.getGridDataInHashMap();
		String newCreatedApn = gridParcelData.get("APN").get(0);
		driver.switchTo().window(parentWindow);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(newCreatedApn);
 		
 		//clicking on edit button in parcels page
 		 objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.EditButton));
 		  objParcelsPage.scrollToElement(objParcelsPage.getWebElementWithLabel(objParcelsPage.editApnField)); 		                   
 		 //Entering new apn value 		 
 		 objParcelsPage.enter(objParcelsPage.getWebElementWithLabel(objParcelsPage.editApnField), apn2);
 		 softAssert.assertEquals(objParcelsPage.saveRecordAndGetError().contains("You can't save this record because a duplicate"),true ,"SMAB-T2646: Verifying that new APN cannot be reupdated."); 	         
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
		//  Opening the PARCELS page  and searching the  parcel to perform brand new parcel mapping
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
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData = testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithSitusData");

		String cityName = hashMapBrandNewParcelMappingData.get("City Name");
		String direction = hashMapBrandNewParcelMappingData.get("Direction");
		String situsNumber = hashMapBrandNewParcelMappingData.get("Situs Number");
		String situsStreetName = hashMapBrandNewParcelMappingData.get("Situs Street Name");
		String situsType = hashMapBrandNewParcelMappingData.get("Situs Type");
		String situsUnitNumber = hashMapBrandNewParcelMappingData.get("Situs Unit Number");
		String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+cityName;

		// Step1: Login to the APAS application using the credentials passed through data provider (login Mapping User)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform brand new parcel mapping
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
  	
          // Step2: Opening the PARCELS page  and searching the  parcel to perform brand new parcel mapping
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
    /**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "brand new parcel" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2716:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"brand new parcel\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_BrandNewParcelMappingAction_NoPrimarySitusTRA(String loginUser) throws Exception {
		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c =NULL and TRA__c=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithSitusData");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		
		String workItem=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
				
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel brand new parcel' 
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)*").get(0);

		//Step 7: Click generate Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+childAPN+"') limit 1");
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
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2716: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2716: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2716: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2716: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2716: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2716: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2716: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2716: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2716: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");
		
		// Legal Description and Reason code are editable as part of SMAB-12026
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2716: Validation that APN column should not be editable on retirning to custom screen");
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Legal Description*"),"SMAB-T2716: Validation that Legal Description column on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*"),"SMAB-T2716: Validation that TRA column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2716: Validation that Situs column should not be editable on retirning to custom screen");
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Reason Code*"),"SMAB-T2716: Validation that Reason Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*"),"SMAB-T2716: Validation that District/Neighborhood column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*"),"SMAB-T2716: Validation that Use Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)*"),"SMAB-T2716: Validation that Parcel Size (SQFT) column should  be editable on retirning to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "brand new parcel" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2716:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"brand new parcel\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_BrandNewParcel_MappingAction_WithPrimarySitusTRA(String loginUser) throws Exception {

		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithSitusData");

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String workItem=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel brand new parcel' 
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)*").get(0);

		//Step 7: Click brand new parcel Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+childAPN+"') limit 1");
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
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2716: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2716: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2716: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2716: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2716: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2716: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2716: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2716: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2716: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	@Test(description = "SMAB-T2831,SMAB-T2716,SMAB-T3633,SMAB-T3634,SMAB-T3635:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"brand new parcel\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_BrandNewParcel_MappingAction_IndependentMappingActionWI(String loginUser) throws Exception {

		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String mappingActionCreationData =  testdata.Brand_New_Parcel_MAPPING_ACTION;
		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformBrandNewParcelMappingActionWithSitusData");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);
		Thread.sleep(7000);
		objMappingPage.closeDefaultOpenTabs();

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule("APAS");
		objMappingPage.searchModule("Mapping Action");
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);

		//Step 3: Selecting Action as 'perform parcel brand new parcel' 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));

		//Step 4: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		objMappingPage.waitForElementToBeVisible(3, objMappingPage.legalDescriptionColumnSecondScreen);
		objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
		objMappingPage.editActionInMappingSecondScreen(hashMapBrandNewParcelMappingData);

		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
	
		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String reasonCode=gridDataHashMap.get("Reason Code*").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)*").get(0);

		//Step 5: Click brand new parcel Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);

		softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2831: Validate that User is able to perform one to one  action from mapping actions tab");

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+childAPN+"') limit 1");
		if(responsePUCDetailsChildAPN.size()==0)
			  childAPNPUC ="";
		else
			childAPNPUC=responsePUCDetailsChildAPN.get("Name").get(0);
		
		//Step 8: Navigating  to the independent mapping action WI that would have been created after performing brand new parcel action and clicking on related action link 
		String workItem= objWorkItemHomePage.getLatestWorkItemDetailsOnWorkbench(1).get("Name").get(0);
		
		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type","Information"), "Mapping",
				"SMAB-T2831: Validation that  A new WI of type Mapping is created after performing one to one from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action","Information"), "Independent Mapping Action",
				"SMAB-T2831: Validation that  A new WI of action Independent Mapping Action is created after performing one to one from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(DateUtil.getCurrentDate("MM/dd/yyyy")), "SMAB-T2831: Validation that 'Date' fields is equal to date when this WI was created");
	
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);
		objMappingPage.waitForElementToBeVisible(3, objMappingPage.parcelSizeColumnSecondScreenWithSpace);
		objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace, "100");
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName));
		
		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2716: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2716: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2716: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2716: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2716: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2716: Validation that  System populates Use Code that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2716: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2716: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		
		objMappingPage.globalSearchRecords(gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),
				objMappingPage.getFieldValueFromAPAS("Parcel Size (SqFt)", "Parcel Information"),
				"SMAB-T3633,SMAB-T3635:Parcel size(SQFT) was updated successfully and user was able to go to update screen");

		// Mark the WI complete
		String query = "Select Id from Work_Item__c where Name = '" + workItem + "'";
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
	
	
	 /**
		 * This method is to  Verify  the custom edit on mapping page
		 *@param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T2835,SMAB-T2840,SMAB-T2669,SMAB-T3121: I need to have the ability to select specific fields from the mapping custom screen, so that the correct values can be assigned to the parcels. ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
				"Smoke","Regression","ParcelManagement" },enabled = true)
		public void ParcelManagement_VerifyBrandNewParcelEditAction(String loginUser) throws Exception {
			String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE (Not Name like '%990') and (Not Name like '134%') and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and  Status__c = 'Active' Limit 1";
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
			String apn1=responseAPNDetails.get("Name").get(0);
				
			// Fetching parcels that are Active with different map book and map page
			String mapBookForAPN1 = apn1.split("-")[0];
			String mapPageForAPN1 = apn1.split("-")[1];
			queryAPNValue = "SELECT Id, Name FROM Parcel_"
					+ "_c WHERE (Not Name like '%990') and (Not Name like '134%') and (Not Name like '"
					+ mapBookForAPN1 + "%') and (Not Name like '" + mapBookForAPN1 + "-" + mapPageForAPN1
					+ "%') and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 1";
			HashMap<String, ArrayList<String>> responseAPN2Details = salesforceAPI.select(queryAPNValue);
			String apn2 = responseAPN2Details.get("Name").get(0);
			
			//Deleting Relationship Instance From Parcel
			objMappingPage.deleteRelationshipInstanceFromParcel(apn1);
			objMappingPage.deleteRelationshipInstanceFromParcel(apn2);

			String concatenateAPNWithDifferentMapBookMapPage = apn1 + "," + apn2;

			// Add the parcels in a Hash Map for validations later
			Map<String, String> apnValue = new HashMap<String, String>();
			apnValue.put("APN1", apn1);
			apnValue.put("APN2", apn2);

			String mappingActionCreationData =  testdata.Brand_New_Parcel_MAPPING_ACTION;

			Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformBrandNewParcelMappingActionWithSitusData");
			String situsCityName = hashMapBrandNewParcelMappingData.get("City Name");
			String direction = hashMapBrandNewParcelMappingData.get("Direction");
			String situsNumber = hashMapBrandNewParcelMappingData.get("Situs Number");
			String situsStreetName = hashMapBrandNewParcelMappingData.get("Situs Street Name");
			String situsType = hashMapBrandNewParcelMappingData.get("Situs Type");
			String situsUnitNumber = hashMapBrandNewParcelMappingData.get("Situs Unit Number");
			String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;


			String PUC =salesforceAPI.select("SELECT Name FROM PUC_Code__c  limit 1").get("Name").get(0);
    	    String TRA= salesforceAPI.select("SELECT Name FROM TRA__c limit 1").get("Name").get(0); 
			
			String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");
			// Step1: Login to the APAS application using the credentials passed through data provider (mapping staff user)
			objMappingPage.login(loginUser);

			// Step 2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
			objMappingPage.searchModule(PARCELS);
			objMappingPage.globalSearchRecords(apn1);

			//Step 3: Creating Manual work item for the Parcel 
			String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
	
			//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
			Thread.sleep(3000);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
			String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();	
			objWorkItemHomePage.switchToNewWindow(parentWindow);
			
			String mappingActionWindow = driver.getWindowHandle();
			objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
			ReportLogger.INFO("Add a parcel with different Map Book and Map Page in Parent APN field :: "
					+ concatenateAPNWithDifferentMapBookMapPage);
			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, concatenateAPNWithDifferentMapBookMapPage);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
			
			
			objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));
			objMappingPage.waitForElementToBeVisible(objMappingPage.reasonCodeField);
			softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
					"SMAB-T2835: Validation that reason code field is auto populated from parent parcel work item");
			objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData); 
			
			objMappingPage.waitForElementToBeVisible(3, objMappingPage.parcelSizeColumnSecondScreenWithSpace);
			objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
			objMappingPage.editActionInMappingSecondScreen(hashMapBrandNewParcelMappingData);
			objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace, "100");
			objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
			 objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
				Thread.sleep(3000);
			
			objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
			ReportLogger.INFO("Validate the Grid values");
			HashMap<String, ArrayList<String>> gridDataHashMapAfterEditAction =objMappingPage.getGridDataInHashMap();
			String childAPNNumber= gridDataHashMapAfterEditAction.get("APN").get(0);
					    
		    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Situs").get(0),childprimarySitus,
					"SMAB-T2835,SMAB-T2840: Validation that System populates Situs from the parent parcel");
		    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("TRA*").get(0),TRA,
					"SMAB-T2835,SMAB-T2840: Validation that System populates TRA from the parent parcel");
		    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Use Code*").get(0),PUC,
					"SMAB-T2835,SMAB-T2840: Validation that System populates TRA from the parent parcel");
		    
		     // Step 7: Verify Linked Items on WI before Brand New Parcel Mapping Action is
			// performed
			ReportLogger.INFO("validate that new APNs added are not linked to WI before Brand New Parcel Mapping Action is performed");
			driver.switchTo().window(parentWindow);
			objMappingPage.waitUntilPageisReady(driver);
			objMappingPage.searchModule(WORK_ITEM);
			objMappingPage.globalSearchRecords(workItemNumber);
			objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
			driver.navigate().refresh();
			Thread.sleep(5000);
			objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
			objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);

			softAssert.assertEquals(1, objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI, 10).size(),
					"SMAB-T2669: Validate that only 1 APN is linked to Work Item and No new parcel is added in Work Item as Parent APN field is not considered in Brand New Parcel Mapping action");
			softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
					"SMAB-T2669: Validate that only 1 APN is linked to Work Item and No new parcel is added in Work Item as Parent APN field is not considered in Brand New Parcel Mapping action");

			driver.switchTo().window(mappingActionWindow);
			objMappingPage.waitUntilPageisReady(driver);
		    ReportLogger.INFO("Click on Combine Parcel button");
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
			softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
					"SMAB-T2835,SMAB-T2840: Validate that User is able to perform Combine action for multiple active parcels");			    
		    
		    driver.switchTo().window(parentWindow);
		    objMappingPage.searchModule(PARCELS);
			objMappingPage.globalSearchRecords(childAPNNumber);
			//Validate the Situs of child parcel generated
		    softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus, "Parcel Information"),childprimarySitus,
					"SMAB-T2840: Validate the Situs of child parcel generated");
		    
			objMappingPage.searchModule(WORK_ITEM);
			objMappingPage.globalSearchRecords(workItemNumber);
			objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
			driver.navigate().refresh();
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
			softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),
					"Submitted for Approval", "SMAB-T2669:Verify user is able to submit the Work Item for approval");

			objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
			objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);

			softAssert.assertEquals(1, objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI, 10).size(),
					"SMAB-T2669: Validate that only 1 APN is linked to Work Item and No new parcel is added in Work Item as Parent APN field is not considered in Brand New Parcel Mapping action");
			softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
					"SMAB-T2669: Validate that only 1 APN is linked to Work Item and No new parcel is added in Work Item as Parent APN field is not considered in Brand New Parcel Mapping action");
			
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			parentWindow = driver.getWindowHandle();
			objWorkItemHomePage.switchToNewWindow(parentWindow);
			objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);

			softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)*"),
					"SMAB-T3121: Validation that Parcel Size (SQFT) column should  be editable on retirning to custom screen");
			
			objMappingPage.waitForElementToBeVisible(3, objMappingPage.parcelSizeColumnSecondScreenWithSpace);
			objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace, "40");    
			objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName));

			HashMap<String, ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();
			String APN = gridDataHashMap.get("APN").get(0);

			driver.switchTo().window(parentWindow);
			objMappingPage.globalSearchRecords(APN);

			// Verify that the parcel size(SQFT)* of second screen with the parcel size on
			// parcel screen and also checks if the Parcel Size (SqFt)field is present on
			// the parcel screen
			softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),
					objMappingPage.getFieldValueFromAPAS("Parcel Size (SqFt)", "Parcel Information"),
					"SMAB-T3121:Parcel size(SQFT) matched and field is avilable on parcel screen");
			
			objWorkItemHomePage.logout();
			Thread.sleep(5000);
			
			// Step 10: Login from Mapping Supervisor to approve the WI
			ReportLogger.INFO(
					"Now logging in as RP Appraiser to approve the work item and validate that new WIs are accessible");
			objWorkItemHomePage.login(MAPPING_SUPERVISOR);

			objMappingPage.searchModule(WORK_ITEM);
			objMappingPage.globalSearchRecords(workItemNumber);
			objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
			objWorkItemHomePage.completeWorkItem();
			softAssert.assertEquals(objMappingPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),
					"Completed", "SMAB-T2669:Verify user is able to complete the Work Item");

			objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
			objMappingPage.waitForElementToBeClickable(objWorkItemHomePage.linkedItemsRecord);

			softAssert.assertEquals(1, objMappingPage.locateElements(objWorkItemHomePage.NoOfLinkedParcelsInWI, 10).size(),
					"SMAB-T2669: Validate that only 1 APN is linked to Work Item and No new parcel is added in Work Item as Parent APN field is not considered in Brand New Parcel Mapping action");
			softAssert.assertTrue(apnValue.containsValue(objMappingPage.getLinkedParcelInWorkItem("0")),
					"SMAB-T2669: Validate that only 1 APN is linked to Work Item and No new parcel is added in Work Item as Parent APN field is not considered in Brand New Parcel Mapping action");

			objWorkItemHomePage.logout();

		}
		
		/**
		 * This method is to Verify that User is able to genrate a recorded doc WI from recorderIntegration and is able to perform mapping actions on that document
		 * @param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T2946:Verify the type of WI system creates for different recorded document types for a recorded document with one APN ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
				"Smoke","Regression","ChangeInOwnershipManagement","RecorderIntegration" },enabled=true)
		public void ParcelManagement_VerifyNewWIgenratedfromRecorderIntegrationAndBrandNewMappingAction(String loginUser) throws Exception {
					
			
		objMappingPage.login(users.SYSTEM_ADMIN);
		objMappingPage.searchModule(PARCELS);
		salesforceAPI.update("Work_Item__c", "SELECT Id FROM Work_Item__c where Sub_type__c='Certificate of Compliance' and status__c ='In pool'", "status__c","In Progress");
		objtransfer.generateRecorderJobWorkItems(objMappingPage.DOC_CERTIFICATE_OF_COMPLIANCE, 1);
   		String WorkItemQuery="SELECT Id,name FROM Work_Item__c where Type__c='MAPPING'  AND AGE__C=0 And status__c='In pool' order by createdDate desc limit 1";     		
        String WorkItemNo=salesforceAPI.select(WorkItemQuery).get("Name").get(0);		         
        //Searching for the WI genrated
         objMappingPage.globalSearchRecords(WorkItemNo); 
        String ApnfromWIPage = objMappingPage.getGridDataInHashMap(1).get("APN").get(0);
        objMappingPage.deleteRelationshipInstanceFromParcel(ApnfromWIPage);

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
		

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObject.put("Lot_Size_SQFT__c",parcelSize);
		salesforceAPI.update("Parcel__c",salesforceAPI.select("select Id from parcel__c where name='"+ApnfromWIPage+"'").get("Id").get(0),jsonObject);

		
		
        Thread.sleep(2000);
        //Validating the fields on AT=C on the Recorder WI
         objMappingPage.scrollToElement(objWorkItemHomePage.firstRelatedBuisnessEvent);
         objMappingPage.Click(objWorkItemHomePage.firstRelatedBuisnessEvent);
         String EventLib=   objMappingPage.getFieldValueFromAPAS(trail.EventLibrary);
         softAssert.assertContains(EventLib, "Recorded Document - MAPPING ", "SMAB-T2946:Verifying Eventlibrary of correspondence AuditTrail");
         String EventType=   objMappingPage.getFieldValueFromAPAS(trail.EventType);
         softAssert.assertContains(EventLib, "Recorded Document - MAPPING ","SMAB-T2946:Verifying EventType of correspondence AuditTrail");
         String EventId=   objMappingPage.getFieldValueFromAPAS(trail.EventId);
         //Validating eventTitle of AT=C
         String EventTitle=   objMappingPage.getFieldValueFromAPAS(trail.EventTitle);
         softAssert.assertContains(EventTitle, EventType+" "+EventId, "SMAB-T2946:Verifying EventTitle of correspondence AuditTrail");         
         softAssert.assertContains(objMappingPage.getFieldValueFromAPAS(trail.RequestOrigin), "Recorder's Office" , "SMAB-T2946:Verifying RequestOrigin of correspondence AuditTrail");         
         softAssert.assertContains(objMappingPage.getFieldValueFromAPAS(trail.Status), "Completed" , "SMAB-T2946:Verifying Status of correspondence AuditTrail");
         //Navigating back to WI linked grid table
         driver.navigate().back();
         
         //Validating the fields on Second Buisness Event on the Recorder WI
         objMappingPage.scrollToElement(objWorkItemHomePage.secondRelatedBuisnessEvent);
         objMappingPage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);
         EventLib=   objMappingPage.getFieldValueFromAPAS(trail.EventLibrary);
         softAssert.assertEquals(EventLib, "DRAFT - MAPPING - CC", "SMAB-T2946:Verifying Eventlibrary of Buisnessevent AuditTrail");
         EventType=   objMappingPage.getFieldValueFromAPAS(trail.EventType);
         softAssert.assertEquals(EventType, "DRAFT - MAPPING - CC", "SMAB-T2946:Verifying EventType of Buisnessevent AuditTrail");
         EventId=   objMappingPage.getFieldValueFromAPAS(trail.EventId);
         EventTitle=   objMappingPage.getFieldValueFromAPAS(trail.EventTitle);
         //Validating eventitle of AT=BE
         softAssert.assertContains(EventTitle, EventType+" "+EventId, "SMAB-T2946:Verifying EventTitle of Buisnessevent AuditTrail");
         objMappingPage.getFieldValueFromAPAS(trail.RequestOrigin);
         softAssert.assertContains(objMappingPage.getFieldValueFromAPAS(trail.RequestOrigin), "Recorder's Office" ,"SMAB-T2946:Verifying RequestOrigin of Buisnessevent AuditTrail");         
         softAssert.assertContains(objMappingPage.getFieldValueFromAPAS(trail.Status), "Open" ,"SMAB-T2946:Verifying Status of Buisnessevent AuditTrail");
         driver.navigate().back();
         
         //Logging out as sysadmin
         objMappingPage.logout();
         //Mapping user logs in and perform mapping action on the WI genrated
         objMappingPage.login(loginUser);
         String mappingActionCreationData = testdata.Brand_New_Parcel_MAPPING_ACTION;
 		 Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
 				"DataToPerformBrandNewParcelMappingActionWithoutAllFields");
 		 objMappingPage.globalSearchRecords(WorkItemNo); 
         Thread.sleep(5000);
 		 objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
 		 objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
 		 softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiTypeDetailsPage),"Mapping" , "SMAB-T2946: Verfiying the type of WI genrated for given Recorded Document");
 		 softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiActionDetailsPage),"Certificate of Compliance" , "SMAB-T2946: Verfiying the Action of WI genrated for given Recorded Document");
 		 objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		 String parentWindow = driver.getWindowHandle();	
		 objWorkItemHomePage.switchToNewWindow(parentWindow);

         objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
 		 objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
 		// Validating that Parcel has been successfully created.
 		 softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),"Parcel(s) have been created successfully. Please review spatial information.",
 				"SMAB-T2642: Validation that Parcel has been successfully created. Please Review Spatial Information");
 		
 		// Retriving new APN genrated
          HashMap<String, ArrayList<String>> gridParcelData = objMappingPage.getGridDataInHashMap();
          String newCreatedApn  =   gridParcelData.get("APN").get(0);  
          jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
  		  jsonObject.put("Status__c","Active");
  		  jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
  		  jsonObject.put("District__c",districtValue);
  		  jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
  		  jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
  		  jsonObject.put("Lot_Size_SQFT__c",parcelSize);
  		  salesforceAPI.update("Parcel__c",salesforceAPI.select("select Id from parcel__c where name='"+newCreatedApn+"'").get("Id").get(0),jsonObject);

  		
          
          
          HashMap<String, ArrayList<String>> statusnewApn = objParcelsPage.fetchFieldValueOfParcel("Status__c", newCreatedApn);
          // validating status of brand new parcel           
          softAssert.assertEquals(statusnewApn.get("Status__c").get(0), "Active", "SMAB-T2643: Verifying the status of the new parcel");		                 
         //Submit work item for approval
          String query = "Select Id from Work_Item__c where Name = '"+WorkItemNo+"'";
          salesforceAPI.update("Work_Item__c", query, "Status__c", "Submitted for Approval");

          driver.switchTo().window(parentWindow);
          objWorkItemHomePage.logout();
          Thread.sleep(5000);
          //Logging as a mapping supervisor
          objMappingPage.login(users.MAPPING_SUPERVISOR);
          objMappingPage.searchModule(WORK_ITEM);
          objMappingPage.globalSearchRecords(WorkItemNo);
          objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
          driver.navigate().refresh(); //refresh as the focus is getting lost
          Thread.sleep(5000);   

         //Completing the workItem
          objWorkItemHomePage.completeWorkItem();             	   
  	      objMappingPage.searchModule(PARCELS);
	      objMappingPage.globalSearchRecords(newCreatedApn);
	   
  		//Validating the status of the workItem 
  		 HashMap<String, ArrayList<String>> statusCompletedApn = objParcelsPage.fetchFieldValueOfParcel("Status__c",newCreatedApn);
          //Validating the status of parcel after completing WI
         softAssert.assertEquals(statusCompletedApn.get("Status__c").get(0), "Active",
     		   "SMAB-T2644: Validating that the status of new APN is active");            
	     objMappingPage.logout();

		}

	}
	