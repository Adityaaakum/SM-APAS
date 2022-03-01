package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;

public class WorkItemAdministration_1stLevelApprover_Test extends TestBase  {
		
		private RemoteWebDriver driver;
		Page objPage;
		ApasGenericPage objApasGenericPage;
		ExemptionsPage objExemptionsPage;
		ParcelsPage objParcelPage;
		Util objUtil;
		SoftAssertion softAssert;
		String exemptionFilePath;
		Map<String, String> rpslData;
		String rpslFileDataPath;
		String newExemptionName;
		WorkItemHomePage objWIHomePage;
		SalesforceAPI salesforceAPI;
		JavascriptExecutor javascriptexecutor;
		
		@BeforeMethod(alwaysRun=true)
		public void beforeMethod() throws Exception{
			driver=null;
			setupTest();
			driver = BrowserDriver.getBrowserInstance();

			objPage = new Page(driver);
			objExemptionsPage = new ExemptionsPage(driver);
			objApasGenericPage = new ApasGenericPage(driver);
			objWIHomePage = new WorkItemHomePage(driver);
			objParcelPage = new ParcelsPage(driver);
			objUtil = new Util();
			softAssert = new SoftAssertion();
			exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
			objApasGenericPage.updateRollYearStatus("Closed", "2020");
			rpslFileDataPath = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;
			rpslData= objUtil.generateMapFromJsonFile(rpslFileDataPath, "DataToCreateRPSLEntryForValidation");
			salesforceAPI = new SalesforceAPI();
			javascriptexecutor = (JavascriptExecutor) driver;
		}
		
		@Test(description = "SMAB-T2552:DV-Retro-work item:Verify that a Disabled Veteran retrofit work item is created automatically,the user can view the Approver field is populated based on the assigned 1st Level Supervisor on the related Work Pool of the work item.", 
				dataProvider = "loginExemptionSupportStaff",
				dataProviderClass = DataProviders.class , 
				groups = {"Regression","DisabledVeteran","WorkItemWorkflow_DisabledVeteran","WorkItemAdministration"})
		public void WorkItemAdministration_verify1stlevelApproverIsAssignedOnAutomatedWICreation(String loginUser) throws Exception {
			  
			   Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
			   String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy");
				String currentRollYear=ExemptionsPage.determineRollYear(currentDate);
				String execEnv = System.getProperty("region");
				//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
			   ReportLogger.INFO("Step 1: Login to the Salesforce ");
			   objApasGenericPage.login(loginUser);
			   objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
			  
			   //Step2: Opening the Exemption Module
			   ReportLogger.INFO("Step 2: Search open Exemption APP. from App Launcher");
			   objApasGenericPage.searchModule(modules.EXEMPTIONS);
			  
			   //Step3: create a New Exemption record
			   ReportLogger.INFO("Step 3: Create New Exemption");
			   objPage.Click(objExemptionsPage.newExemptionButton);
			  
			   //Get the WI Name from DB through the newly created Exemption
			   newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
			   Thread.sleep(2000);
			   
			   
			    //Step4: Opening the Work Item Module
			    ReportLogger.INFO("Step 4: Search open App. module - Work Item Management from App Launcher");
			   objApasGenericPage.searchModule(modules.HOME);

			   //Step5: Click on the Main TAB - Home
			   ReportLogger.INFO("Step 5: Click on the Main TAB - Home");
			   objPage.Click(objWIHomePage.lnkTABHome);
			   ReportLogger.INFO("Step 6: Click on the Sub  TAB - Work Items");
			   objPage.Click(objWIHomePage.lnkTABWorkItems);
			   ReportLogger.INFO("Step 8: Click on the SUB TAB - My Submitted for Approval");
			   objPage.Click(objWIHomePage.lnkTABMySubmittedforApproval);
			   
			   HashMap<String, ArrayList<String>> getWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
			   
			    String WIName = getWIDetails.get("Name").get(0);
			    String WIRequestType = getWIDetails.get("Request_Type__c").get(0);
			    String Work_Item_Details = "Select Id from Work_Item__c where Name = '"+WIName+"' ";
			    HashMap<String, ArrayList<String>> response_3  = salesforceAPI.select(Work_Item_Details);
			    String WIId = response_3.get("Id").get(0);
			   //Search the Work Item Name in the Grid 1st Column
			    javascriptexecutor.executeScript("window.scrollBy(0,800)");
			    objWIHomePage.Click(objWIHomePage.ageDays);
			    objWIHomePage.waitForElementToBeClickable(WIName);
				Thread.sleep(2000);
				objWIHomePage.findWorkItemInProgress(WIName);
				driver.navigate().to("https://smcacre--"+ execEnv + ".lightning.force.com/lightning/r/Work_Item__c/" + WIId + "/view");
			   
			   String WorkItem_Id = objWIHomePage.getWorkItemIDFromExemptionOnWorkBench(newExemptionName);
			   String WP_SupervisorName = objWIHomePage.getSupervisorDetailsFromWorkBench(WorkItem_Id);
			   
			   objPage.waitForElementToBeClickable(objWIHomePage.detailsWI);
			   objPage.javascriptClick(objWIHomePage.detailsTab);
			   
			   String Curr_Approver = objApasGenericPage.getFieldValueFromAPAS("Current Approver", "Approval & Supervisor Details");
			   String Approver = objApasGenericPage.getFieldValueFromAPAS("Approver", "Approval & Supervisor Details");

			   
			   ReportLogger.INFO("Step 9: Verify the Work Pool Supervisor : '"+WP_SupervisorName+"' is also populated on Work Item :"+WIName);
			   softAssert.assertEquals(Curr_Approver,WP_SupervisorName,"SMAB-T2552:Verify Current Approver and Work Pool supervisor are same");			   
			   softAssert.assertEquals(Approver,WP_SupervisorName,"SMAB-T2552:Verify Approver and Work Pool supervisor are same");
			   
			   ReportLogger.INFO("Step 10: Logging out from SF");

			   objApasGenericPage.logout();	
			   
		}
		
		@Test(description = "SMAB-T2565:DV-Retro-work item: "
				+ "BP -Retro-work item: Verify that Apporver Is present On WI After "
				+ "BP records after claiming the WI 'Final Review'",
				dataProvider = "loginExemptionSupportStaff",
				dataProviderClass = DataProviders.class , 
				groups = {"Regression","WorkItemWorkflow_DisabledVeteran","DisabledVeteran","WorkItemAdministration"})
		public void WorkItemAdministration_verify1stlevelApproverIsAssignedOnManualWICreation(String loginUser) throws Exception {
			  
			   
			//Fetching the Active Parcel
			String query ="SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Active' and Primary_Situs__C !='' limit 1";
			HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
			String parcelToSearch = response.get("Name").get(0);

			//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
			ReportLogger.INFO("Step 1: Login to the Salesforce ");
			objApasGenericPage.login(loginUser);

			//Step2: Opening the Parcels module
			objApasGenericPage.searchModule(modules.PARCELS);

			//Step3: Search and Open the Parcel
			objApasGenericPage.globalSearchRecords(parcelToSearch);
     	    		   
			   String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			   
			   Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
						"WorkItemRoutingGiveToMe");
			   String WIName = objParcelPage.createWorkItem(hashMapmanualWorkItemData);
			   
			   objPage.waitForElementToBeClickable(objWIHomePage.detailsWI);
			   Thread.sleep(3000);
			   objPage.javascriptClick(objWIHomePage.detailsWI);
			   
			   String WorkItem_Id = objWIHomePage.getWorkItemIDFromParcelOnWorkbench(parcelToSearch);
				
			   String WP_SupervisorName = objWIHomePage.getSupervisorDetailsFromWorkBench(WorkItem_Id);
			   String Approver = objApasGenericPage.getFieldValueFromAPAS("Approver", "Approval & Supervisor Details");

			   
			   ReportLogger.INFO("Step 9: Verify the Work Pool Supervisor : '"+WP_SupervisorName+"' is also populated on Work Item :"+WIName);
			   			   
			   softAssert.assertEquals(Approver,WP_SupervisorName,"SMAB-T2565:Verify Approver and Work Pool supervisor are same");
			   
			   String updateWIStatus = "SELECT Id FROM Work_Item__c where Name = '"+WIName+"'";
			   salesforceAPI.update("Work_Item__c", updateWIStatus, "Status__c", "Completed");

			   ReportLogger.INFO("Step 11: Logging out from SF");

			   objApasGenericPage.logout();			
		
		}	


}
