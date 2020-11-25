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
import com.apas.PageObjects.Page;
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
		Page objPage;
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
			objPage = new Page(driver);
			objApasGenericFunctions = new ApasGenericFunctions(driver);
			objParcelsPage = new ParcelsPage(driver);
			objWorkItemHomePage = new WorkItemHomePage(driver);
			objUtil = new Util();
			softAssert = new SoftAssertion();
			salesforceAPI = new SalesforceAPI();
		}
		
				/**
		 * This method is to verify that work pool supervisor is able to select and approve multiple work items
		 * @param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T2241: verify that work pool supervisor is able to select and approve multiple work items", dataProvider = "loginExemption", dataProviderClass = DataProviders.class, groups = {
				"regression","work_item_manual"  },alwaysRun = true,enabled =true)
		public void WorkItems_VerifyMassApproval(String loginUser) throws Exception {
			
			String workItemNumber1;
			String workItemNumber2;
			
			// fetching a parcel where PUC is not blank but  Primary Situs is blank
			String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
			HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
			String apnValue= response.get("Name").get(0);
			
			String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeRP");
					
			// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
			objApasGenericFunctions.login(loginUser);

			// Step2: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
			objApasGenericFunctions.searchModule(PARCELS);
			objApasGenericFunctions.globalSearchRecords(apnValue);

			

			// Step 3: Creating Manual work item for the Parcel 
		    workItemNumber1 = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		    workItemNumber2 = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		    String querySelectWICName = "select name from Work_Item_Configuration__c WHERE Work_Item_Type__c='RP' AND Work_Item_Sub_Type__c = 'CPI Factor'";
		    salesforceAPI.update("Work_Item_Configuration__c",querySelectWICName, "Allow_Supervisor_Mass_Approval__c","No");
		   
		    objApasGenericFunctions.searchModule(modules.HOME);
		    
		    
		    objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);		   
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber1); 
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber2); 
		    objWorkItemHomePage.Click(objWorkItemHomePage.markCompleteButton);
		    objApasGenericFunctions.logout();
		    
		    objApasGenericFunctions.login(users.RP_BUSINESS_ADMIN);
	        objApasGenericFunctions.searchModule(modules.HOME);
	        objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber1); 
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber2);
	        objWorkItemHomePage.Click(objWorkItemHomePage.approveButton);
	        softAssert.assertTrue(objPage.verifyElementVisible(objWorkItemHomePage.errormsgOnWI),"Consolidated work items cannot be mass approved.");
	       
	        salesforceAPI.update("Work_Item_Configuration__c",querySelectWICName, "Allow_Supervisor_Mass_Approval__c","Yes");
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber1); 
	        objWorkItemHomePage.selectWorkItemOnHomePage(workItemNumber2);
	        objWorkItemHomePage.Click(objWorkItemHomePage.approveButton);
	        softAssert.assertTrue(objPage.verifyElementVisible(objWorkItemHomePage.successAlert),"Work Item(s) processed successfully.");
	        objApasGenericFunctions.logout();
	        
	        objApasGenericFunctions.login(users.EXEMPTION_SUPPORT_STAFF);
	        objApasGenericFunctions.searchModule(modules.HOME);
	        objWorkItemHomePage.Click(objWorkItemHomePage.completedTab);
	        softAssert.assertTrue(objWorkItemHomePage.isWorkItemExists(workItemNumber1),"Is completed");
	        softAssert.assertTrue(objWorkItemHomePage.isWorkItemExists(workItemNumber2),"Is completed");
			objApasGenericFunctions.logout();
		}
}