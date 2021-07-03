package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.RoutingAssignmentPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.testdata;
import com.apas.config.users;
import com.jayway.jsonpath.JsonPath;

public class WorkItemAdministration_RoutingAutomationRule_Test extends TestBase {
	
	RemoteWebDriver driver;

    WorkItemHomePage objWorkItemHomePage;
    ParcelsPage objParcelsPage;
    RoutingAssignmentPage objRoutingAssignmentPage;
    
    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI salesforceAPI = new SalesforceAPI();
    Util objUtil;

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();
        objUtil = new Util();

        objParcelsPage = new ParcelsPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objRoutingAssignmentPage = new RoutingAssignmentPage(driver);
        SoftAssertion softAssert = new SoftAssertion();
    }

    @Test(description = "SMAB-T2135: Work Item - Verify If the related WIC specifies \"SEC\" Roll Code,"
    		+ " then the system will find the first Routing Assignment record that matches by "
    		+ "Neighborhood Code of the related Parcel to assign the Work Pool to the Work Item", 
    		dataProvider = "loginExemptionSupportStaff", 
    		dataProviderClass = DataProviders.class, 
    		groups = {"Regression","WorkItemAdministration","Neighborhood"}, 
    		alwaysRun = true)
    public void WorkItemAdministration_RoutingRule_verifyRoutingRuleForSECCodeWithMatchedNB(String loginUser) throws Exception {
    	
    	String workItemConfigName;
        String workItemConfigID;
        String neighborhoodName;
        String workPoolName;
        String parcelName;
        String parcelID;
        
        String query_1 = "SELECT Name,id from work_item_configuration__c where work_item_type__c ='Disabled Veterans' and work_item_sub_type__c = 'Direct Review and Update'";
        
        HashMap<String, ArrayList<String>> response_1 = salesforceAPI.select(query_1);
        workItemConfigID = response_1.get("Id").get(0);
        workItemConfigName = response_1.get("Name").get(0);
                
        String query_2 = "SELECT Neighborhood__c ,Work_Pool__c FROM Routing_Assignment__c where configuration__c= '"+ workItemConfigID +"'";
                
        HashMap<String, ArrayList<String>> response_2 = salesforceAPI.select(query_2);       
        String neighborhoodID = response_2.get("Neighborhood__c").get(0);
        String workPoolId = response_2.get("Work_Pool__c").get(0);
        
        String queryNBName = "SELECT Name FROM Neighborhood__c where id= '"+neighborhoodID+"'";
        neighborhoodName = salesforceAPI.select(queryNBName).get("Name").get(0);
        
        String queryWPName = "SELECT Name FROM Work_Pool__c where id = '"+workPoolId+"'";
        workPoolName = salesforceAPI.select(queryWPName).get("Name").get(0);
        		
        		
        String query_3 = "SELECT ID FROM Parcel__c where  status__c = 'Active' and neighborhood_reference__r.name='"+neighborhoodName+"'";
        HashMap<String, ArrayList<String>> response_3 = salesforceAPI.select(query_3);   
        parcelID = response_3.get("Id").get(0);
           
    	
    	//Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objWorkItemHomePage.login(loginUser);
        
        String parcelURL = envURL+"/lightning/r/Parcel__c/"+parcelID+"/view";
        
        //Navigate to Parcel view page
        driver.get(parcelURL);
        
        String workItemCreationData=testdata.MANUAL_WORK_ITEMS; 
		Map<String, String> hashMapmanualWorkItemData =objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateWorkItemOfTypeDisableveterans");		

		// Step 2: Opening the PARCELS page and searching a parcel		
		String workItemNumber =objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		boolean flag;
		
		flag = objWorkItemHomePage.verifyWorkPoolName(workPoolName);
		
		softAssert.assertTrue(flag, "SMAB-T2135: WIC with Roll Code SEC successfully routed to the 1st matched "
				                     + "Neighborhood Code of the related Parcel to assign the Work Pool '"+workPoolName+"' to the Work Item : "+workItemNumber);       
                
    }

    @Test(description = "SMAB-T2136: Work Item - Work Item - Verify If the related WIC specifies \"SEC\" Roll Code, "
    		+ "and system cannot find the Routing Assignment record that matches by Neighborhood Code then system "
    		+ "will find the Routing Assignment record that has the same WIC lookup value", 
    		dataProvider = "loginExemptionSupportStaff", 
    		dataProviderClass = DataProviders.class, 
    		groups = {"Regression","WorkItemAdministration","Neighborhood"}, 
    		alwaysRun = true)
    public void WorkItemAdministration_RoutingRule_verifyRoutingRuleForSECCodeWithUnMatchedNB(String loginUser) throws Exception {
    	
    	String workItemConfigName;
        String workItemConfigID;
        String neighborhoodName;
        String workPoolName;
        String parcelName;
        String parcelID;
        
        String query_1 = "SELECT Name,id from work_item_configuration__c where "
        		+ "work_item_type__c ='Disabled Veterans' and work_item_sub_type__c = 'Direct Review and Update'";
        
        HashMap<String, ArrayList<String>> response_1 = salesforceAPI.select(query_1);
        workItemConfigID = response_1.get("Id").get(0);
        workItemConfigName = response_1.get("Name").get(0);
        
        
        String query_2 = "SELECT Neighborhood__c ,Work_Pool__c FROM Routing_Assignment__c "
        		+ "where configuration__c= '"+ workItemConfigID +"'";
        
		/*
		 * String jsonResponse = salesforceAPI.getSelectQueryDateInJson(query_2);
		 * JSONObject jsonObject = new JSONObject(jsonResponse); JSONArray jsonArray =
		 * jsonObject.getJSONArray("records"); String jsonObjectRecord; jsonObjectRecord
		 * = jsonArray.getJSONObject(0).getString("Neighborhood__r"); neighborhoodName =
		 * JsonPath.read(jsonObjectRecord, "$.Name");
		 */
        
        HashMap<String, ArrayList<String>> response_2 = salesforceAPI.select(query_2);       
        String neighborhoodID = response_2.get("Neighborhood__c").get(0);
        String workPoolId = response_2.get("Work_Pool__c").get(0);
        
        String queryNBName = "SELECT Name FROM Neighborhood__c where id= '"+neighborhoodID+"'";
        neighborhoodName = salesforceAPI.select(queryNBName).get("Name").get(0);
        
        String queryWPName = "SELECT Name FROM Work_Pool__c where id = '"+workPoolId+"'";
        workPoolName = salesforceAPI.select(queryWPName).get("Name").get(0);        
        
        String query_3 = "SELECT Id FROM Parcel__c where  status__c = 'Active' and "
        		+ "neighborhood_reference__r.name != '"+neighborhoodName+"' limit 1 ";
        
        parcelID = salesforceAPI.select(query_3).get("Id").get(0);
           
    	
    	//Step1: Login to the APAS application using the credentials passed through data provider (BPP Business Admin)
        objWorkItemHomePage.login(loginUser);        
        
        String parcelURL = envURL+"/lightning/r/Parcel__c/"+parcelID+"/view";
        
        //Navigate to Parcel view page
        driver.get(parcelURL);
        Thread.sleep(2000);
        
        String workItemCreationData=testdata.MANUAL_WORK_ITEMS; 
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateWorkItemOfTypeDisableveterans");
		
		// Step 2: Opening the PARCELS page and searching a parcel		
		String workItemNumber =objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		String rpLostWorkPoolName = "RP Lost in Routing";
		
		boolean flag;
		
		flag = objWorkItemHomePage.verifyWorkPoolName(rpLostWorkPoolName);
		
		softAssert.assertTrue(flag, "SMAB-T2136: WIC with Roll Code SEC successfully routed to the un-matched "
				                     + "Neighborhood Code of the related Parcel to assign the Work Pool '"+rpLostWorkPoolName+"' to the Work Item: "+workItemNumber);       
                
    }

}
