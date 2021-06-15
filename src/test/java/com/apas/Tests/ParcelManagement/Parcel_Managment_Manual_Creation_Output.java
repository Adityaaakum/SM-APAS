package com.apas.Tests.ParcelManagement;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;

public class Parcel_Managment_Manual_Creation_Output extends TestBase implements testdata,modules,users {
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
	
	
	@Test(description = "SMAB-T2642,SMAB-T2643,SMAB-T2644:Verify that User is able to perform a \"Brand New Parcel\" mapping action for a Parcel   from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" },enabled = false)
	public void ParcelManagement_VerifyBrandNewParcelMappingAction(String loginUser) throws Exception {
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
	        String workItemNumber =	objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
	    String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		//Step 5: Validation that work pool should be 'Mapping' on parent parcel work item
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"Mapping","SMAB-T2263: Validation that work pool should be 'Mapping' on parent parcel work item");
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		//Clearning the values inside parent apn button
		
		//clicking on save button
						
		//Step 8: entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.CreateNewParcelButton));
		//Step 12: Validating that Parcel has been successfully created.
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.confirmationMessageOnSecondScreen),"Parcel has been successfully created. Please Review Spatial Information",
				"SMAB-T2642: Validation that Parcel has been successfully created. Please Review Spatial Information");
		
		//clicking again on next as toast message is recieved
           HashMap<String, ArrayList<String>> gridParcelData=      objMappingPage.getGridDataInHashMap();

           String newCreatedApn  =   gridParcelData.get("APN").get(0);
                            
           HashMap<String, ArrayList<String>> statusnewApn   =  objParcelsPage.fetchFieldValueOfParcel("Status__c", newCreatedApn);
             // validating status of brand new parcel           
            softAssert.assertEquals(statusnewApn.get("Status__c").get(0), "In Progress - New Parcel", "SMAB-T2643: Verifying the status of the new parcel");
              //Completing the workItem
           String   queryWI = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
     	 salesforceAPI.update("Work_Item__c",queryWI, "Status__c", "Completed");
     		//Validating the status of the workItem 
     		
     		 HashMap<String, ArrayList<String>> statusCompletedApn = objParcelsPage.fetchFieldValueOfParcel("Status__c",newCreatedApn);
             
           softAssert.assertEquals(statusCompletedApn.get("Status__c").get(0), "Active", "SMAB-T2644 Validating that the status of new APN is active");
     		  		
     		    	   		    		
             		                           
		    driver.switchTo().window(parentWindow);
		
		    objMappingPage.logout();
		
	}

	@Test(description = "SMAB-T2646: Once the parcel creation has been approved, the user will not be allowed to change the APN allocated.", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" },enabled = true)
	public void ParcelManagement_VerifyNoTAllowedToChangeNewAPN(String loginUser) throws Exception {
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
		Thread.sleep(3000);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		//Step 5: Validation that work pool should be 'Mapping' on parent parcel work item
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"Mapping","SMAB-T2263: Validation that work pool should be 'Mapping' on parent parcel work item");
		
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		
		
		//Clearning the values inside parent apn button
		
		//clicking on save button
		
	
		
				//Step 8: entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.CreateNewParcelButton));
		
		 HashMap<String, ArrayList<String>> gridParcelData=      objMappingPage.getGridDataInHashMap();

         String newApn  =   gridParcelData.get("APN").get(0);
         
         driver.switchTo().window(parentWindow);
         
         objMappingPage.searchModule(PARCELS);
         
 		objMappingPage.globalSearchRecords(newApn);
 		
 		//clicking on edit button in parcels page
 		
 		   objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.EditButton));
 		 
 		   
 		       
 		 objParcelsPage.scrollToElement(objParcelsPage.getWebElementWithLabel(objParcelsPage.editApnField));
 		         
 		 //Entering new apn value
 		 
 		 objParcelsPage.enter(objParcelsPage.getWebElementWithLabel(objParcelsPage.editApnField), apn2);
 		         
 		// objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.SaveButton));
 		         
 		
 		 softAssert.assertEquals(objParcelsPage.saveRecordAndGetError().contains("You can't save this record because a duplicate record already exists"),true ,"SMAB-T2646: Verifying that new APN cannot be reupdated.");
 		   
 		 objParcelsPage.cancelRecord();
 		 
 		        
 		 
 		        
 		 objParcelsPage.logout();
 		         
	}

	
	
	@Test(description = "SMAB-T2647: The update legal and short legal description should be visible in parcel if added while creating the parcel and these fields should be editable after the parcel is approved", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" },enabled = false)
	public void ParcelManagement_VerifyLegalDescIsEditable(String loginUser) throws Exception {
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
		
						//Step 8: entering data in form for Brand New Parcel mapping
		objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.CreateNewParcelButton));
		
		 HashMap<String, ArrayList<String>> gridParcelData=      objMappingPage.getGridDataInHashMap();

         String newApn  =   gridParcelData.get("APN").get(0);
         
         driver.switchTo().window(parentWindow);
         
         objMappingPage.searchModule(PARCELS);
         
 		objMappingPage.globalSearchRecords(newApn);
 		
 		//clicking on edit button in parcels page
 		
 		 objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.EditButton));
 		 
 	       	  
 		   		 
 	      boolean status = objParcelsPage.verifyElementEnabled(objParcelsPage.getWebElementWithLabel(objParcelsPage.LongLegalDescriptionLabel));
 		
 		 
 		 objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.SaveButton)); 
 		 
 		  		 
 		 //validating that long desc field is editable
 		 softAssert.assertEquals(status ,true, "SMAB-T2647:Validating long desc field is editable");
 		 
 		 objParcelsPage.logout();
 		  
 		  
	}
}
