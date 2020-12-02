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
import com.apas.PageObjects.ApasGenericPage;
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
		ApasGenericPage objApasGenericPage;
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
			objApasGenericPage=new ApasGenericPage(driver);
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
					"DataToCreateWorkItemOfTypeRP");
					
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
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber1); 
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber2); 
		    objWorkItemHomePage.Click(objWorkItemHomePage.btnMarkComplete);
		    objApasGenericFunctions.logout();
		    Thread.sleep(15000);
		    
		    //Step 6: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		    objApasGenericFunctions.login(users.RP_BUSINESS_ADMIN);
		    
	       //Step 7:Navigate to home and need my approval tab
		    objApasGenericFunctions.searchModule(modules.HOME);
	        objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
	        
	        //Step 8:Select Work Items
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber1); 
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber2);
	        
	        //Step 9:Click on Approve Button
	        objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
	        
	        //Step 10:Validating the error message that Work items can't be Approved
	        softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(), "One or more of the selected Work Items are not enabled for mass approval.",
	        		"SMAB-T2241:Validating the error message that Work items can't be Approved");
	        
	        objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
	        
	       //Updating the value of 'Allow Supervisor Mass Approval'
	        salesforceAPI.update("Work_Item_Configuration__c",querySelectWICName, "Allow_Supervisor_Mass_Approval__c","Yes");
	        
	        //Step 11: Navigating to Home page
	        objApasGenericFunctions.searchModule(modules.HOME);
	        driver.navigate().refresh();
	        
	        //Step 12: Navigate to need my approval tab
	        objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
	        
	        //Step 13: Selecting the work Items
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber1); 
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber2);
	        
	        //Step 14: Click on Approve Button
	        objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
	        
	        //Step 15:Validating the success message that Work items are Approved
	        softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
	        		"SMAB-T2241:Validating the success message that Work items are Approved");
	        softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(), "Work Item(s) process succesfully!",
	        		"SMAB-T2241:Validating the success message that Work items are Approved");
	        objApasGenericFunctions.logout();
	        Thread.sleep(15000);
	        
	        //Step16: Login to the APAS application using the credentials passed through data provider (Exemption support staff)
	        objApasGenericFunctions.login(users.EXEMPTION_SUPPORT_STAFF);
	        objApasGenericFunctions.searchModule(modules.HOME);
	        objWorkItemHomePage.Click(objWorkItemHomePage.completedTab);
	        
	        HashMap<String, ArrayList<String>> PrimaryWorkItems =
	                objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
	        
	        // Validating the approved WI's should be in completed tab
	                softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(workItemNumber1),
	                		"SMAB-T2241:Validating the approved WI's should be in completed tab");
	           
	                softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(workItemNumber2),
	    	                "SMAB-T2241:Validating the approved WI's should be in completed tab");
	       	        
			objApasGenericFunctions.logout();
		}
		
		/**
		 * This method is to verify that work pool supervisor gets error
		 *  when user tries to change assignee for WIs with different Work Pools
		 * 
		 * @param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T2042: Verify that Work pool Supervisor gets error when user tries to change assignee for WIs with different Work Pools", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
				"regression", "work_item_manual" })
		public void WorkItems_ErrorChangeAsignee(String loginUser) throws Exception {
			
			String workItem1InProgress ,workItem1InPool, workItem2InProgress, workItem2InPool;
           // Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
			objApasGenericFunctions.login(loginUser);
			
			//Step2: Navigating to Home and In pool Tab 
			objApasGenericFunctions.searchModule(HOME);
			objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
			
			//fetching the work items in pool
			String queryWIDVInPool = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name='Disabled Veterans' and Status__c='In Pool'";
			HashMap<String, ArrayList<String>> valueWIDVInpool = salesforceAPI.select(queryWIDVInPool);			
			String queryWINotDVInPool = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name!='Disabled Veterans' and (Work_Pool__r.name='RP Admin' or Work_Pool__r.name='RP Lost in Routing') and Status__c='In Pool'";
			HashMap<String, ArrayList<String>> valueWINotDVInPool = salesforceAPI.select(queryWINotDVInPool);			
					
			//fetching the work items in progress
			String queryWIDVInProgress = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name='Disabled Veterans' and Status__c='In Progress'";
			HashMap<String, ArrayList<String>> valueWIDVInProgress = salesforceAPI.select(queryWIDVInProgress);			
			String queryWINotDVInProgress = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name!='Disabled Veterans' and (Work_Pool__r.name='RP Admin' or Work_Pool__r.name='RP Lost in Routing') and Status__c='In Progress'";
			HashMap<String, ArrayList<String>> valueWINotDVInProgress = salesforceAPI.select(queryWINotDVInProgress);
			
			//Creating Work Items if value returned is null or empty
			if((valueWIDVInProgress == null ||valueWINotDVInProgress == null 
					|| valueWIDVInpool == null || valueWINotDVInPool == null))
			{
				String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
				HashMap<String, ArrayList<String>> value = salesforceAPI.select(queryAPNValue);
				String apnValue= value.get("Name").get(0);
				
				String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
				Map<String, String> hashMapmanualWorkItemDataDVInProgress = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInprogress");
				Map<String, String> hashMapmanualWorkItemDataDVInPool = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInpool");
				Map<String, String> hashMapmanualWorkItemDataRPInProgress = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeRPInProgress");
				Map<String, String> hashMapmanualWorkItemDataRPInPool = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeRPInpool");

				// Step1: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
				objApasGenericFunctions.searchModule(PARCELS);
				objApasGenericFunctions.globalSearchRecords(apnValue);

			
				// Step2: Creating Manual work items 
				 workItem1InProgress= objParcelsPage.createWorkItem(hashMapmanualWorkItemDataDVInProgress);
			     objApasGenericFunctions.globalSearchRecords(apnValue);
			     workItem1InPool= objParcelsPage.createWorkItem(hashMapmanualWorkItemDataDVInPool);
			     objApasGenericFunctions.globalSearchRecords(apnValue);
			     workItem2InProgress= objParcelsPage.createWorkItem(hashMapmanualWorkItemDataRPInProgress);
			     objApasGenericFunctions.globalSearchRecords(apnValue);
			     workItem2InPool=objParcelsPage.createWorkItem(hashMapmanualWorkItemDataRPInPool);
			}
			else {
			 workItem1InPool = valueWIDVInpool.get("Name").get(0);
			 workItem2InPool = valueWINotDVInPool.get("Name").get(0);
			 workItem1InProgress = valueWIDVInProgress.get("Name").get(0);
			 workItem2InProgress = valueWINotDVInProgress.get("Name").get(0);
			}
			
			//Step3: Selecting the work Items
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem1InPool);
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem2InPool);
			
			//Step4 :Click On Assignee Button
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeAssignee));
			
			//Validating error message for Asignee cannot be changed for staff in pool

			softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(), "Please select the Work Item from same Work Pool.",
					"SMAB-T2042: Validating error message for Asignee cannot be changed for in pool");
			
			objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
			objApasGenericFunctions.searchModule(HOME);
			driver.navigate().refresh();
			objApasGenericPage.openTab("Staff - In Pool");
			
			//Step5: Selecting the work Items
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem1InPool);
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem2InPool);
			
			//Step6:Click On Assignee Button
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeAssignee));
			
			//Validating error message for Asignee cannot be changed for staff in pool
			softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(), "Please select the Work Item from same Work Pool.",
					"SMAB-T2042: Validating error message for Asignee cannot be changed for in pool");
			
			//Step7: Close the error message
			objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
			
			//Step8: Navigate to Staff in progress tab
			objApasGenericFunctions.searchModule(HOME);
			driver.navigate().refresh();
			objApasGenericPage.openTab("Staff - In Progress");
			
			
			//Step9: Selecting the work Items
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem1InProgress);
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem2InProgress);
			
			//Step10: Click on Assignee button
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeAssignee));
			
			//Validating error message for Asignee cannot be changed for staff in progress
			softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(), "Please select the Work Item from same Work Pool.",
					"SMAB-T2042: Validating error message for Asignee cannot be changed for in pool");
			
			//Step7: Close the error message
			objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
			objApasGenericFunctions.logout();
		}
		
		/**
		 * This method is to verify that work pool supervisor is able to select multiple 'Staff-In Pool' 
		 * work items and assign them to a specific user or a work pool
		 * 
		 * @param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T2010: verify that work pool supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user or a work pool.", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
				"regression", "work_item_manual" })
		public void workItems_ChangeAsigneeAndWorkPoolStaffInPool(String loginUser) throws Exception {
			String workItem1, workItem2;
			// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
			objApasGenericFunctions.login(loginUser);
			
			// Step2: Opening Home Page    	
			objApasGenericFunctions.searchModule(HOME);

			// Step3: Navigating to staff In Pool Tab
			objApasGenericPage.openTab("Staff - In Pool");
			
			//fetching work items that are of same work pool and are in Staff in pool status
			String query = "SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name='Disabled Veterans' and Status__c='In Pool' limit 2";
			HashMap<String, ArrayList<String>> wiValue = salesforceAPI.select(query);
			
			//Creating Work Items if value returned is null or empty
			if(wiValue == null )
			{
				// fetching a parcel value				
				String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
				HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
				String apnValue = response.get("Name").get(0);
				 				
				String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;	
				Map<String, String> hashMapmanualWorkItemDataInPool = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInpool");
				// Step1: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
				objApasGenericFunctions.searchModule(PARCELS);
				objApasGenericFunctions.globalSearchRecords(apnValue);
		
				// Step2: Creating Manual work items 
				workItem1=   objParcelsPage.createWorkItem(hashMapmanualWorkItemDataInPool);
				objApasGenericFunctions.globalSearchRecords(apnValue);
				workItem2=   objParcelsPage.createWorkItem(hashMapmanualWorkItemDataInPool);
				objApasGenericFunctions.globalSearchRecords(apnValue);	
			}
			else {
			 workItem1 = wiValue.get("Name").get(0);
			 workItem2 = wiValue.get("Name").get(1);
			}
		            
			// Step10: Selecting the work items
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem1);
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem2);
			
			// Step11: Clicking on change workpool button to change the workpool and save 
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeWorkPool));
			objApasGenericPage.searchAndSelectFromDropDown(objWorkItemHomePage.WorkPool, "RP Lost in Routing");
			objWorkItemHomePage.enter(objWorkItemHomePage.reasonForTransferring,"Test");
			objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
			
			// Step12: Validating that work pool supervisor is able to select and approve multiple work items"
		    softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
		    		"SMAB-T2010: Validating that work pool supervisor is able to select and approve multiple work items");
		    softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(), "Work Item(s) process succesfully!",
            		"SMAB-T2010: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user.");
		    
		   // Step13 :Navigate to details tab to validate work pool
		    objApasGenericFunctions.globalSearchRecords(workItem1);
		    objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		    Thread.sleep(3000);
		    
		  //  Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific work pool
	        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool"), "RP Lost in Routing",
	        		"SMAB-T2010: Validating that work pool supervisor is able to select and approve multiple work items");
	        objApasGenericFunctions.globalSearchRecords(workItem2);
	        objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			Thread.sleep(3000);
			 softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool"), "RP Lost in Routing",
		        		"SMAB-T2010: Validating that work pool supervisor is able to select and approve multiple work items");
			
			//Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific work pool
	        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool"), "RP Lost in Routing",
	        		"SMAB-T2010: Validating that work pool supervisor is able to select and approve multiple work items");
	        
	        objApasGenericFunctions.searchModule(HOME);
			objApasGenericPage.openTab("Staff - In Pool");
			
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItem1);
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem2);
			
			// Step6: Clicking on change assignee button to change the assignee and save 
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeAssignee));
			objApasGenericPage.searchAndSelectFromDropDown(objWorkItemHomePage.AssignedTo, "rp appraiserAUT");
			objWorkItemHomePage.enter(objWorkItemHomePage.reasonForTransferring,"Test");
			objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
			
			// Step7: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user."
			softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
					"SMAB-T2010: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user.");
            softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(), "Work Item(s) process succesfully!",
            		"SMAB-T2010: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user.");
            
			// Step8 :Navigate to details tab to validate Assignee
			objApasGenericFunctions.globalSearchRecords(workItem1);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			Thread.sleep(3000);
			
			//Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user
			 softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To"),"rp appraiserAUT", 
		    		"SMAB-T2010: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user.");
		    objApasGenericFunctions.globalSearchRecords(workItem2);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		    objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		    Thread.sleep(3000);
		    
		   // Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user
		    softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To"),"rp appraiserAUT", 
		    		"SMAB-T2010: Validating that Work pool Supervisor is able to select multiple 'Staff-In Pool' work items and assign them to a specific user.");
			
	        objApasGenericFunctions.logout();
	}	
		
		/**
		 * This method is to Verify that Work pool Supervisor is able to select multiple 'Staff-In Progress'
		 * work items and assign them to a specific user or a work pool
		 * 
		 * @param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T2019:Verify that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user or a work pool.", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
				"regression", "work_item_manual" })
		public void workItems_ChangeAsigneeAndWorkPoolStaffInProgress(String loginUser) throws Exception {
			String workItem1 ,workItem2;
			// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
			objApasGenericFunctions.login(loginUser);
			
			// Step2: Navigating to Home Page
			objApasGenericFunctions.searchModule(HOME);
			
			// Step3: Navigating to staff In Progress Tab
			objApasGenericPage.openTab("Staff - In Progress");
			
			//fetching work items that are of same work pool and are in Staff in progress status
			String query="SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name='Disabled Veterans' and Status__c='In Progress' and Assigned_To__r.name!='rp appraiserAUT' limit 2";
			HashMap<String, ArrayList<String>> wiValue = salesforceAPI.select(query);
			 //Creating Work Items if value returned is null or empty
			if(wiValue == null)
			{
				// fetching a parcel value				
				String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
				HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
				String apnValue = response.get("Name").get(0);
				 
				
				String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;	
				Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeDVInpool");

				// Step1: Opening the PARCELS page  and searching a parcel
				objApasGenericFunctions.searchModule(PARCELS);
				objApasGenericFunctions.globalSearchRecords(apnValue);

			
				// Step2: Creating Manual work items 
				workItem1=   objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
				objApasGenericFunctions.globalSearchRecords(apnValue);
				workItem2=   objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
	
			}

			 workItem1 = wiValue.get("Name").get(0);
		     workItem2 = wiValue.get("Name").get(1);
		
		 	        
	        // Step5: Selecting the work items
		    objWorkItemHomePage.selectWorkItemOnHomePage(workItem1);
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem2);
			
			// Step6: Clicking on change assignee button to change the assignee and save 
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeAssignee));
			objApasGenericPage.searchAndSelectFromDropDown(objWorkItemHomePage.AssignedTo, "rp appraiserAUT");
			objWorkItemHomePage.enter(objWorkItemHomePage.reasonForTransferring,"Test");
			objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
			
			// Step7: Validating that work pool supervisor is able to select and approve multiple work items"
			softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
					"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user");
			softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(), "Work Item(s) process succesfully!",
					"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user");
			//Step8 :Navigate to details tab to validate Assignee
			objApasGenericFunctions.globalSearchRecords(workItem1);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			Thread.sleep(3000);
			
			//Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user
			 softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To"),"rp appraiserAUT",
		    		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user");
		    objApasGenericFunctions.globalSearchRecords(workItem2);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		    objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		    Thread.sleep(3000);	
		    
		    //Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user
		    softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To"),"rp appraiserAUT",
		    		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user");
			
			// Step9: Navigating to Staff in progress Tab
			objApasGenericFunctions.searchModule(HOME);
			objApasGenericPage.openTab("Staff - In Progress");
			
			// Step10: Selecting the work items
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem1);
			objWorkItemHomePage.selectWorkItemOnHomePage(workItem2);
			
			// Step11: Clicking on change workpool button to change the workpool and save 
			objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeWorkPool));
			objApasGenericPage.searchAndSelectFromDropDown(objWorkItemHomePage.WorkPool, "RP Lost in Routing");
			objWorkItemHomePage.enter(objWorkItemHomePage.reasonForTransferring,"Test");
			objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
					
			// Step12: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool"
		    softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.successAlert),
		    		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool");
		    softAssert.assertEquals(objApasGenericFunctions.getAlertMessage(), "Work Item(s) process succesfully!",
					"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific user");
		    
		   // Step13 :Navigate to details tab to validate Assignee
		    objApasGenericFunctions.globalSearchRecords(workItem1);
		    objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		    Thread.sleep(3000);
		    
		    //Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool
		    softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool"),"RP Lost in Routing",
	        		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool");
	        objApasGenericFunctions.globalSearchRecords(workItem2);
	        objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			Thread.sleep(3000);
			
			//Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool
	        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool"),"RP Lost in Routing",
	        		"SMAB-T2019: Validate that Work pool Supervisor is able to select multiple 'Staff-In Progress' work items and assign them to a specific work pool");
	        
	        objApasGenericFunctions.logout();
	}			
}