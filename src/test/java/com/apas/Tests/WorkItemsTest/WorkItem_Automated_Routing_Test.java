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
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.testdata;

public class WorkItem_Automated_Routing_Test extends TestBase {
	
	
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	Page objPage;
	WorkItemHomePage objWorkItemHomePage;
	ApasGenericPage objAPASGeneric ;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
	}


	@Test(description = "SMAB-T2135:Work Item - Verify If the related WIC specifies SEC Roll Code, "
			+ "then the system will find the first Routing Assignment record that matches by Neighborhood Code "
			+ "of the related Parcel to assign the Work Pool to the Work Item", 
			dataProvider = "loginRPBusinessAdmin", 
			dataProviderClass = DataProviders.class, 
			groups = {"regression","work_item_manual" })
	public void WorkItems_VerifyWIIsRoutedBasedOnMatchingNeighborhoodCode(String loginUser) throws Exception {
		
		//fetching a parcel where PUC is not blank but  Primary Situs is blank		
		String queryAPNValue = "select Name, id from Parcel__c "
				+ "where puc_code_lookup__c != NULL "
				+ "and primary_situs__c = NULL "
				+ "and Status__c='Active' limit 1";
		
		HashMap<String, ArrayList<String>> response_1 = salesforceAPI.select(queryAPNValue);
		String apnID = response_1.get("id").get(0);
		String apnName = response_1.get("Name").get(0);
		
		String parcelURL = CONFIG.getProperty("URL_" + region.toLowerCase())+"/lightning/r/Parcel__c/'"+apnID+"'/view";
		
		objWorkItemHomePage.login(loginUser);
		objPage.navigateTo(driver, parcelURL);
		objPage.Click(objAPASGeneric.editButton);
		objAPASGeneric.selectOptionFromDropDown(objParcelsPage.NeighborhoodCode,"TestAmit");
		objPage.Click(objAPASGeneric.saveButton);
		
		//Step : Creating Manual work item from Parcel of type Direct Review and Update
		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapGiveWorkItemToSomeoneElse = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToSomeoneElse");
		String workItemAssignedToSomeoneElse = objParcelsPage.ParcelComponentActions(apnName, hashMapGiveWorkItemToSomeoneElse);
		objPage.Click(objWorkItemHomePage.detailsWI);
		String workPool = objWorkItemHomePage.getFieldValueFromAPAS("Work Pool");
		
		String workItemType = hashMapGiveWorkItemToSomeoneElse.get("Work Item Type");
		String actions = hashMapGiveWorkItemToSomeoneElse.get("Actions");
		
		String queryWPName = "SELECT work_pool__r.Name , Configuration__r.Name FROM Routing_Assignment__c "
				+ "where neighborhood__r.Name ='TestAmit' "
				+ "and Configuration__c IN (select id from work_item_configuration__c "
				+ "where work_item_type__c = '"+workItemType+"'"
				+ "and work_item_sub_type__c = '"+actions+"'"
				+ "and Roll_Code__c = 'SEC')";
		
		HashMap<String, ArrayList<String>> response_2 = salesforceAPI.select(queryAPNValue);
		String dbWPName = response_2.get("work_pool__r.Name").get(0);
		
		ReportLogger.INFO("Step: Verify the Work Item is Routed to the correct work pool as per routing rule");
		softAssert.assertEquals(workPool,dbWPName,"SMAB-T2135:Verify Work Item - Verify If the related WIC specifies Roll Code - SEC, "
				+ "then the system will find the first Routing Assignment "
				+ "record that matches by Neighborhood Code of the related "
				+ "Parcel to assign the Work Pool to the Work Item");
		
		objAPASGeneric.logout();   
		
		
	}
	@Test(description = "SMAB-T2136:Work Item - Verify If the related WIC specifies Roll Code - SEC, "
			+ "and system cannot find the Routing Assignment record that matches by Neighborhood Code "
			+ "then system will find the Routing Assignment record that has the same WIC lookup value", 
			dataProvider = "loginRPBusinessAdmin", 
			dataProviderClass = DataProviders.class, 
			groups = {"regression","work_item_manual" })
	public void WorkItems_VerifyWIIsRoutedBasedOnNonMatchingNeighborhoodCode(String loginUser) throws Exception {
		
		//fetching a parcel where PUC is not blank but  Primary Situs is blank		
		String queryAPNValue = "select Name, id from Parcel__c "
				+ "where puc_code_lookup__c != NULL "
				+ "and primary_situs__c = NULL "
				+ "and Status__c='Active' limit 1";
		
		HashMap<String, ArrayList<String>> response_1 = salesforceAPI.select(queryAPNValue);
		String apnID = response_1.get("id").get(0);
		String apnName = response_1.get("Name").get(0);
		
		String parcelURL = CONFIG.getProperty("URL_" + region.toLowerCase())+"/lightning/r/Parcel__c/'"+apnID+"'/view";
		
		objWorkItemHomePage.login(loginUser);
		objPage.navigateTo(driver, parcelURL);
		objPage.Click(objAPASGeneric.editButton);
		objAPASGeneric.selectOptionFromDropDown(objParcelsPage.NeighborhoodCode,"");
		objPage.Click(objAPASGeneric.saveButton);
		
		//Step : Creating Manual work item from Parcel of type Direct Review and Update
		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapGiveWorkItemToSomeoneElse = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToSomeoneElse");
		String workItemAssignedToSomeoneElse = objParcelsPage.ParcelComponentActions(apnName, hashMapGiveWorkItemToSomeoneElse);
		objPage.Click(objWorkItemHomePage.detailsWI);
		String workPool = objWorkItemHomePage.getFieldValueFromAPAS("Work Pool");
		
		String workItemType = hashMapGiveWorkItemToSomeoneElse.get("Work Item Type");
		String actions = hashMapGiveWorkItemToSomeoneElse.get("Actions");
		
		String queryWPName = "SELECT work_pool__r.Name , Configuration__r.Name FROM Routing_Assignment__c "
				+ "where neighborhood__r.Name ='TestAmit' "
				+ "and Configuration__c IN (select id from work_item_configuration__c "
				+ "where work_item_type__c = '"+workItemType+"'"
				+ "and work_item_sub_type__c = '"+actions+"'"
				+ "and Roll_Code__c = 'SEC')";
		
		HashMap<String, ArrayList<String>> response_2 = salesforceAPI.select(queryAPNValue);
		String dbWPName = response_2.get("work_pool__r.Name").get(0);
		
		ReportLogger.INFO("Step: Verify the Work Item is Routed to the correct work pool as per routing rule");
		softAssert.assertEquals(workPool,dbWPName,"SMAB-T2136:Work Item - Verify If the related WIC specifies Roll Code - SEC," 
							+ "and system cannot find the Routing Assignment record that matches by Neighborhood Code" 
							+ "then system will find the Routing Assignment record that has the same WIC lookup value ");
		
		objAPASGeneric.logout();   
		
		
	}

}
