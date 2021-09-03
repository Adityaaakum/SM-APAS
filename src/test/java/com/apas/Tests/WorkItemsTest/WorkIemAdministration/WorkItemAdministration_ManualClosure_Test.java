package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

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
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.RoutingAssignmentPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.PageObjects.WorkPoolPage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class WorkItemAdministration_ManualClosure_Test extends TestBase implements testdata, modules, users  {

		private RemoteWebDriver driver;

		ParcelsPage objParcelsPage;
		WorkItemHomePage objWorkItemHomePage;
		WorkPoolPage objWorkPoolPage;
		Page objPage;
		Util objUtil = new Util();
		SoftAssertion softAssert = new SoftAssertion();
		SalesforceAPI salesforceAPI = new SalesforceAPI();
		ApasGenericPage ObjApasGeneric;
		RoutingAssignmentPage objRoutingAssignmentPage;

		@BeforeMethod(alwaysRun = true)
		public void beforeMethod() throws Exception {
			driver = null;
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
			ObjApasGeneric = new ApasGenericPage(driver);
			objParcelsPage = new ParcelsPage(driver);
			objPage = new Page(driver);
			objWorkItemHomePage = new WorkItemHomePage(driver);
			objRoutingAssignmentPage = new RoutingAssignmentPage(driver);
			objWorkPoolPage = new WorkPoolPage(driver);
		}

		@Test(description = "SMAB-T2872: Work Input -  "
				+ "verify that when manual closure is set to No on WI Config, "
				+ "Mark Complete should throw an error", 
				dataProvider = "loginExemptionSupportStaff", 
				dataProviderClass = DataProviders.class, 
				groups = {"Regression","WorkItemAdministration","ManualClosure" })
		public void WorkItemAdministration_ManualClosureAsNO(String loginUser) throws Exception {
			
			// fetching a parcel where PUC and Primary Situs are not blank		
			String queryAPNValue = "select Name from Parcel__c where PUC_Code_Lookup__c!= null "
					+ "and Primary_Situs__c !=null AND Status__c='Active' limit 1";
			HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
			String apnValue= response.get("Name").get(0);

			String workItemCreationData =  testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeDisableveterans");

			// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
			objWorkItemHomePage.login(loginUser);

			// Step2: Opening the PARCELS page  and searching a parcel where PUC and Primary Situs field (Street) have values saved
			objWorkItemHomePage.searchModule(PARCELS);
			objWorkItemHomePage.globalSearchRecords(apnValue);
			
			// Step 3: Creating Manual work item for the Parcel 
			String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
			driver.navigate().refresh();
			Thread.sleep(30000);
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
			
			String expectedErrorMsg = "Status: The work item selected cannot be closed manually";
			String actualErrorMsg = ObjApasGeneric.getAlertMessage();
			
			softAssert.assertEquals(actualErrorMsg, expectedErrorMsg,"SMAB-T2872: "
					+ "Work Input -  verify that when manual closure is set to No on WI Config, "
					+ "Mark Complete should throw an error");
			

		}
		
}
