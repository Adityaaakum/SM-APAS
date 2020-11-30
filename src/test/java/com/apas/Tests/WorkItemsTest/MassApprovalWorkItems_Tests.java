package com.apas.Tests.WorkItemsTest;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
public class MassApprovalWorkItems_Tests extends TestBase implements testdata, modules, users { 
		private RemoteWebDriver driver;
		LoginPage objLoginPage;
		ApasGenericFunctions objApasGenericFunctions;
		ParcelsPage objParcelsPage;
		WorkItemHomePage objWorkItemHomePage;
		Util objUtil;
		SoftAssertion softAssert;
		SalesforceAPI salesforceAPI;

		@BeforeMethod(alwaysRun = true)
		public void beforeMethod() throws Exception {
			driver = null;
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
			objLoginPage = new LoginPage(driver);
			objApasGenericFunctions = new ApasGenericFunctions(driver);
			objParcelsPage = new ParcelsPage(driver);
			objWorkItemHomePage = new WorkItemHomePage(driver);
			objUtil = new Util();
			softAssert = new SoftAssertion();
			salesforceAPI = new SalesforceAPI();
		}
		
		/**
		 * This method is to verify that work pool supervisor is able to select and
		 * approve multiple work items
		 * 
		 * @param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T2241: verify that work pool supervisor is able to select and approve multiple work items", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class, groups = {
				"regression","work_item_manual"  })
		public void WorkItems_VerifyMassApproval(String loginUser) throws Exception {
			
			String workItemNumber1;
			String workItemNumber2;
			
			// fetching a parcel where PUC is not blank but  Primary Situs is blank		
			String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
			HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
			String apnValue = response.get("Name").get(0);
			 
			
			String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeDV");
					
			// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
			objApasGenericFunctions.login(loginUser);

			// Step2: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
			objApasGenericFunctions.searchModule(PARCELS);
			objApasGenericFunctions.globalSearchRecords(apnValue);

			// Step 3: Creating Manual work item for the Parcels 
		    workItemNumber1 = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);		    
			objApasGenericFunctions.globalSearchRecords(apnValue);
		    workItemNumber2 = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		      
		    //Step 4:Navigating to home page
		    objApasGenericFunctions.searchModule(HOME);
		    
		    //fetching WIC corresponding to work item
		    String query="SELECT Work_Item_Configuration__c FROM Work_Item__c Where Name= '"+ workItemNumber2+"'";
		    HashMap<String, ArrayList<String>> response5=salesforceAPI.select(query);
			String querySelectWICName=response5.get("Work_Item_Configuration__c").get(0);
			
			//Updating the value of 'Allow Supervisor Mass Approval'
		    salesforceAPI.update("Work_Item_Configuration__c", querySelectWICName, "Allow_Supervisor_Mass_Approval__c", "No");
		    objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);	
		    
		    //Step 5:-Selecting he work items
	        objWorkItemHomePage.clickCheckBoxForSelectingWI(workItemNumber1); 
	        objWorkItemHomePage.clickCheckBoxForSelectingWI(workItemNumber2); 
		    objWorkItemHomePage.Click(objWorkItemHomePage.btnMarkComplete);
		    objApasGenericFunctions.logout();
		    
		    //Step 6: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		    objApasGenericFunctions.login(users.RP_BUSINESS_ADMIN);
		    
	       //Step 7:Navigate to home and need my approval tab
		    objApasGenericFunctions.searchModule(modules.HOME);
	        objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
	        
	        //Step 8:Select Work Items
	        objWorkItemHomePage.clickCheckBoxForSelectingWI(workItemNumber1); 
	        objWorkItemHomePage.clickCheckBoxForSelectingWI(workItemNumber2);
	        
	        //Step 9:Click on Approve Button
	        objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
	        
	        //Step 10:Validating the error message that Work items can't be Approved
	        softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.errormsgOnWI),
	        		"SMAB-T2241:Validating the error message that Work items can't be Approved");
	        objWorkItemHomePage.Click(objWorkItemHomePage.closeErrorMsg);
	        
	       //Updating the value of 'Allow Supervisor Mass Approval'
	        salesforceAPI.update("Work_Item_Configuration__c",querySelectWICName, "Allow_Supervisor_Mass_Approval__c","Yes");
	        
	        //Step 11: Navigating to Home page
	        objApasGenericFunctions.searchModule(modules.HOME);
	        driver.navigate().refresh();
	        
	        //Step 12: Navigate to need my approval tab
	        objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
	        
	        //Step 13: Selecting the work Items
	        objWorkItemHomePage.clickCheckBoxForSelectingWI(workItemNumber1); 
	        objWorkItemHomePage.clickCheckBoxForSelectingWI(workItemNumber2);
	        
	        //Step 14: Click on Approve Button
	        objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
	        
	        //Step 15:Validating the success message that Work items are Approved
	        softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
	        		"SMAB-T2241:Validating the success message that Work items are Approved");
	        objApasGenericFunctions.logout();
	        
	        //Step16: Login to the APAS application using the credentials passed through data provider (Exemption support staff)
	        objApasGenericFunctions.login(users.EXEMPTION_SUPPORT_STAFF);
	        objApasGenericFunctions.searchModule(modules.HOME);
	        objWorkItemHomePage.Click(objWorkItemHomePage.completedTab);
	        
	        // Validating the approved WI's should be in completed tab
	        softAssert.assertTrue(objWorkItemHomePage.isWorkItemExists(workItemNumber1),
	        		"SMAB-T2241:Validating the approved WI's should be in completed tab");
	        softAssert.assertTrue(objWorkItemHomePage.isWorkItemExists(workItemNumber2),
	        		"SMAB-T2241:Validating the approved WI's should be in completed tab");
	        
			objApasGenericFunctions.logout();
		}
				
		@Test(description = "SMAB-T2042: Verify that Work pool Supervisor gets error when user tries to change assignee for WIs with different Work Pools", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
				"regression", "work_item_manual" })
		public void WorkItems_ErrorChangeAsignee(String loginUser) throws Exception {
			
			String changeAssignee= "Change Assignee";
           // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
			objApasGenericFunctions.login(loginUser);
			
			//Step2: Navigating to Home and In pool Tab 
			objApasGenericFunctions.searchModule(HOME);
			objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
			
			//fetching the work items in pool
			String queryWIDVInPool = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name='Disabled Veterans' and Status__c='In Pool'";
			HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryWIDVInPool);
			String workItem1InPool = response.get("Name").get(0);
			String queryWINotDVInPool = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name!='Disabled Veterans' and (Work_Pool__r.name='RP Admin' or Work_Pool__r.name='RP Lost in Routing') and Status__c='In Pool'";
			HashMap<String, ArrayList<String>> response2 = salesforceAPI.select(queryWINotDVInPool);
			String workItem2InPool = response2.get("Name").get(0);
			
			
			//fetching the work items in progress
			String queryWIDVInProgress = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name='Disabled Veterans' and Status__c='In Progress'";
			HashMap<String, ArrayList<String>> response3 = salesforceAPI.select(queryWIDVInProgress);
			String workItem1InProgress = response3.get("Name").get(0);
			String queryWINotDVInProgress = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name!='Disabled Veterans' and (Work_Pool__r.name='RP Admin' or Work_Pool__r.name='RP Lost in Routing') and Status__c='In Progress'";
			HashMap<String, ArrayList<String>> response4 = salesforceAPI.select(queryWINotDVInProgress);
			String workItem2InProgress = response4.get("Name").get(0);
			
			if(workItem1InPool.isEmpty()||workItem2InPool.isEmpty() || workItem1InProgress.isEmpty() || workItem2InProgress.isEmpty())
			{
				String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
				HashMap<String, ArrayList<String>> value = salesforceAPI.select(queryAPNValue);
				String apnValue= value.get("Name").get(0);
				
				String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
				Map<String, String> hashMapmanualWorkItemData2 = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInprogress");
				Map<String, String> hashMapmanualWorkItemData3 = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInpool");
				Map<String, String> hashMapmanualWorkItemData4 = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeRPInProgress");
				Map<String, String> hashMapmanualWorkItemData5 = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeRPInpool");

				// Step1: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
				objApasGenericFunctions.searchModule(PARCELS);
				objApasGenericFunctions.globalSearchRecords(apnValue);

			
				// Step2: Creating Manual work items 
				 workItem1InProgress= objParcelsPage.createWorkItem(hashMapmanualWorkItemData2);
			     objApasGenericFunctions.globalSearchRecords(apnValue);
			     workItem1InPool= objParcelsPage.createWorkItem(hashMapmanualWorkItemData3);
			     objApasGenericFunctions.globalSearchRecords(apnValue);
			     workItem1InProgress= objParcelsPage.createWorkItem(hashMapmanualWorkItemData4);
			     objApasGenericFunctions.globalSearchRecords(apnValue);
			     workItem2InPool=objParcelsPage.createWorkItem(hashMapmanualWorkItemData5);
			}

			//Step3: Selecting the work Items
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1InPool);
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2InPool);
			
			//Step4 :Click On Assignee Button
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(changeAssignee));
			
			//Validating error message for Asignee cannot be changed for staff in pool
			softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.errormsgOnWI), 
					"SMAB-T2042: Validating error message for Asignee cannot be changed for in pool");
			
			objWorkItemHomePage.Click(objWorkItemHomePage.closeErrorMsg);
			objApasGenericFunctions.searchModule(HOME);
			driver.navigate().refresh();
			objWorkItemHomePage.Click(objWorkItemHomePage.staffInPoolTab);
			
			//Step5: Selecting the work Items
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1InPool);
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2InPool);
			
			//Step6:Click On Assignee Button
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(changeAssignee));
			
			//Validating error message for Asignee cannot be changed for staff in pool
			softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.errormsgOnWI),
					"SMAB-T2042: Validating error message for Asignee cannot be changed for staff in pool");
			
			//Step7: Close the error message
			objWorkItemHomePage.Click(objWorkItemHomePage.closeErrorMsg);
			
			//Step8: Navigate to Staff in progress tab
			objApasGenericFunctions.searchModule(HOME);
			driver.navigate().refresh();
			objWorkItemHomePage.Click(objWorkItemHomePage.staffInProgressTab);
			
			
			//Step9: Selecting the work Items
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1InProgress);
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2InProgress);
			
			//Step10: Click on Assignee button
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(changeAssignee));
			
			//Validating error message for Asignee cannot be changed for staff in progress
			softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.errormsgOnWI),
					"SMAB-T2042: Validating error message for Asignee cannot be changed for staff in progress");
			//Step7: Close the error message
			objWorkItemHomePage.Click(objWorkItemHomePage.closeErrorMsg);
			objApasGenericFunctions.logout();
		}
		
		@Test(description = "SMAB-T2010: verify that work pool supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user or a work pool.", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
				"regression", "work_item_manual" })
		public void workItems_ChangeAsigneeAndWorkPoolStaffInPool(String loginUser) throws Exception {
			
			 String changeWorkPool= "Change Work Pool";
			 String changeAssignee= "Change Assignee";
			
			// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
			objApasGenericFunctions.login(loginUser);
			
			// Step2: Opening Home Page    	
			objApasGenericFunctions.searchModule(HOME);
			
			// Step3: Navigating to staff In Pool Tab
			objWorkItemHomePage.Click(objWorkItemHomePage.staffInPoolTab);
			
			//fetching work items that are of same work pool and are in Staff in pool status
			String query = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name='Disabled Veterans' and Status__c='In Pool' limit 2";
			HashMap<String, ArrayList<String>> response3 = salesforceAPI.select(query);
			
			String queryNoAssignee = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name!='Disabled Veterans' and Status__c='In Pool' and Assigned_To__c=NULL limit 2";
			HashMap<String, ArrayList<String>> response4 = salesforceAPI.select(queryNoAssignee);
			String workItem1 = response4.get("Name").get(0);
			String workItem2 = response4.get("Name").get(1);
			String workItem3 = response3.get("Name").get(0);
		    String workItem4 = response3.get("Name").get(1);
	
			if(workItem1.isEmpty()||workItem2.isEmpty()|| workItem3.isEmpty()|| workItem4.isEmpty())
			{
				// fetching a parcel value				
				String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
				HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
				String apnValue = response.get("Name").get(0);
				 				
				String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;	
				Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInpool");
				Map<String, String> hashMapmanualWorkItemData2 = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInpool");

				// Step1: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
				objApasGenericFunctions.searchModule(PARCELS);
				objApasGenericFunctions.globalSearchRecords(apnValue);
		
				// Step2: Creating Manual work items 
				workItem1=   objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
				objApasGenericFunctions.globalSearchRecords(apnValue);
				workItem2=   objParcelsPage.createWorkItem(hashMapmanualWorkItemData2);
	
			}
	        
	        // Step5: Selecting the work items
		    objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1);
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2);
			
			// Step6: Clicking on change assignee button to change the assignee and save 
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(changeAssignee));
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.selectOptionDropDownAsigneeModal);
			objWorkItemHomePage.Click(objWorkItemHomePage.selectOptionDropDownAsigneeModal);
			objWorkItemHomePage.selectOptionDropDownAsigneeModal.sendKeys("rp appraiserAUT");
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.asigneeNameModal);
			objWorkItemHomePage.Click(objWorkItemHomePage.asigneeNameModal);
			objWorkItemHomePage.Click(objWorkItemHomePage.reasonForTransferring);
			objWorkItemHomePage.reasonForTransferring.sendKeys("Test");
			objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
			
			// Step7: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user."
			softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
					"SMAB-T2010: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user.");

			// Step8 :Navigate to details tab to validate Assignee
			objApasGenericFunctions.globalSearchRecords(workItem1);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			Thread.sleep(3000);
			
			//Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user
		    softAssert.assertTrue(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To","").equalsIgnoreCase("rp appraiserAUT"),
		    		"SMAB-T2010: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user.");
		    objApasGenericFunctions.globalSearchRecords(workItem2);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		    objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		    Thread.sleep(3000);
		    
		   // Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user
		    softAssert.assertTrue(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To","").equalsIgnoreCase("rp appraiserAUT"), 
		    		"SMAB-T2010: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user.");
			
			// Step9: Navigating to Staff in pool Tab
			objApasGenericFunctions.searchModule(HOME);
			objWorkItemHomePage.Click(objWorkItemHomePage.staffInPoolTab);
			
			// Step10: Selecting the work items
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem3);
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem4);
			
			// Step11: Clicking on change workpool button to change the workpool and save 
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(changeWorkPool));
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.workPoolModal);
			objWorkItemHomePage.Click(objWorkItemHomePage.workPoolModal);
		    objWorkItemHomePage.workPoolModal.sendKeys("RP Lost in Routing");
		    objWorkItemHomePage.Click(objWorkItemHomePage.asigneeNameModal);
		    objWorkItemHomePage.Click(objWorkItemHomePage.reasonForTransferring);
			objWorkItemHomePage.reasonForTransferring.sendKeys("Test");
			objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
			
			
			// Step12: Validating that work pool supervisor is able to select and approve multiple work items"
		    softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
		    		"SMAB-T2010: Validating that work pool supervisor is able to select and approve multiple work items");
		    
		   // Step13 :Navigate to details tab to validate work pool
		    objApasGenericFunctions.globalSearchRecords(workItem3);
		    objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		    Thread.sleep(3000);
		    
		  //  Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific work pool
	        softAssert.assertTrue(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool","").equalsIgnoreCase("RP Lost in Routing"),
	        		"SMAB-T2010: Validating that work pool supervisor is able to select and approve multiple work items");
	        objApasGenericFunctions.globalSearchRecords(workItem4);
	        objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			Thread.sleep(3000);
			
			//Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific work pool
	        softAssert.assertTrue(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool","").equalsIgnoreCase("RP Lost in Routing"),
	        		"SMAB-T2010: Validating that work pool supervisor is able to select and approve multiple work items");
	        
	        objApasGenericFunctions.logout();
	}	
		
		@Test(description = "SMAB-T2019:Verify that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user or a work pool.", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
				"regression", "work_item_manual" })
		public void workItems_ChangeAsigneeAndWorkPoolStaffInProgress(String loginUser) throws Exception {
			 String changeWorkPool= "Change Work Pool";
			 String changeAssignee= "Change Assignee";
			 
			// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
			objApasGenericFunctions.login(loginUser);
			
			// Step2: Navigating to Home Page
			objApasGenericFunctions.searchModule(HOME);
			
			// Step3: Navigating to staff In Progress Tab
			objWorkItemHomePage.Click(objWorkItemHomePage.staffInProgressTab);
			
			//fetching work items that are of same work pool and are in Staff in progress status
			String query="SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name='Disabled Veterans' and Status__c='In Progress' and Assigned_To__r.name!='rp appraiserAUT' limit 2";
			HashMap<String, ArrayList<String>> response3 = salesforceAPI.select(query);			
			String workItem1 = response3.get("Name").get(0);
		    String workItem2 = response3.get("Name").get(1);
		
			if(workItem1.isEmpty()||workItem2.isEmpty())
			{
				// fetching a parcel value				
				String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
				HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
				String apnValue = response.get("Name").get(0);
				 
				
				String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;	
				Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInpool");
				Map<String, String> hashMapmanualWorkItemData2 = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInpool");

				// Step1: Opening the PARCELS page  and searching a parcel
				objApasGenericFunctions.searchModule(PARCELS);
				objApasGenericFunctions.globalSearchRecords(apnValue);

			
				// Step2: Creating Manual work items 
				workItem1=   objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
				objApasGenericFunctions.globalSearchRecords(apnValue);
				workItem2=   objParcelsPage.createWorkItem(hashMapmanualWorkItemData2);
	
			}
	        
	        // Step5: Selecting the work items
		    objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1);
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2);
			
			// Step6: Clicking on change assignee button to change the assignee and save 
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(changeAssignee));
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.selectOptionDropDownAsigneeModal);
			objWorkItemHomePage.Click(objWorkItemHomePage.selectOptionDropDownAsigneeModal);
			objWorkItemHomePage.selectOptionDropDownAsigneeModal.sendKeys("rp appraiserAUT");
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.asigneeNameModal);
			objWorkItemHomePage.Click(objWorkItemHomePage.asigneeNameModal);
			objWorkItemHomePage.Click(objWorkItemHomePage.reasonForTransferring);
			objWorkItemHomePage.reasonForTransferring.sendKeys("Test");
			objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
			
			// Step7: Validating that work pool supervisor is able to select and approve multiple work items"
			softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
					"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user");

			//Step8 :Navigate to details tab to validate Assignee
			objApasGenericFunctions.globalSearchRecords(workItem1);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			Thread.sleep(3000);
			
			//Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user
		    softAssert.assertTrue(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To","").equalsIgnoreCase("rp appraiserAUT"),
		    		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user");
		    objApasGenericFunctions.globalSearchRecords(workItem2);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		    objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		    Thread.sleep(3000);	
		    
		    //Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user
		    softAssert.assertTrue(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To","").equalsIgnoreCase("rp appraiserAUT"),
		    		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user");
			
			// Step9: Navigating to Staff in progress Tab
			objApasGenericFunctions.searchModule(HOME);
			objWorkItemHomePage.Click(objWorkItemHomePage.staffInProgressTab);
			
			// Step10: Selecting the work items
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1);
			objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2);
			
			// Step11: Clicking on change workpool button to change the workpool and save 
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(changeWorkPool));
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.workPoolModal);
			objWorkItemHomePage.Click(objWorkItemHomePage.workPoolModal);
		    objWorkItemHomePage.workPoolModal.sendKeys("RP Lost in Routing");
		    objWorkItemHomePage.Click(objWorkItemHomePage.asigneeNameModal);
		    objWorkItemHomePage.Click(objWorkItemHomePage.reasonForTransferring);
			objWorkItemHomePage.reasonForTransferring.sendKeys("Test");
			objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
					
			// Step12: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool"
		    softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
		    		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool");
		    
		   // Step13 :Navigate to details tab to validate Assignee
		    objApasGenericFunctions.globalSearchRecords(workItem1);
		    objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		    Thread.sleep(3000);
		    
		    //Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool
	        softAssert.assertTrue(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool","").equalsIgnoreCase("RP Lost in Routing"),
	        		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool");
	        objApasGenericFunctions.globalSearchRecords(workItem2);
	        objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			Thread.sleep(3000);
			
			//Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool
	        softAssert.assertTrue(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool","").equalsIgnoreCase("RP Lost in Routing"),
	        		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool");
	        
	        objApasGenericFunctions.logout();
	}			
}