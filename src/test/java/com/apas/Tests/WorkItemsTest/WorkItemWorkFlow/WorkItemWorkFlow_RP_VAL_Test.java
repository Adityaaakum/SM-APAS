package com.apas.Tests.WorkItemsTest.WorkItemWorkFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.MappingPage;
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

public class WorkItemWorkFlow_RP_VAL_Test extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objExemptionsPage;
	Util objUtil;
	SoftAssertion softAssert;
	String manualWIFilePath;
	Map<String, String> rpslData;
	String rpslFileDataPath;
	String newExemptionName;
	WorkItemHomePage objWIHomePage;
	SalesforceAPI salesforceAPI;
	ParcelsPage objParcel;
	MappingPage objMappingPage;
	

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objWIHomePage = new WorkItemHomePage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		manualWIFilePath = testdata.MANUAL_WORK_ITEMS;
		objApasGenericPage.updateRollYearStatus("Closed", "2020");		
		rpslData= objUtil.generateMapFromJsonFile(rpslFileDataPath, "DataToCreateRPSLEntryForValidation");
		salesforceAPI = new SalesforceAPI();
		objMappingPage= new MappingPage(driver);
		objParcel = new ParcelsPage(driver);

	}

	/*
	 * This method is to create a WI for the Construction Discovery by the Appraiser
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T4230,SMAB-T4231,SMAB-T4232:RP Construction Discovery- Verify that Appraisers "
			+ "will have the ability to create a construction discovery work item from the Component Actions"
			+ " list and its Type label should be NC, the Actions label should be Construction - Other", 
			dataProvider = "RPAppraiser", 
			dataProviderClass = DataProviders.class , 
			groups = {"Smoke","Regression","RP_VAL","WorkItemWorkflow_RP_VAL_Construction"})
	public void WorkItemWorkflow_RP_VAL_ConstructionManualWICreation(String loginUser) throws Exception {
		
		//Fetching parcel that are Active
		String queryApnDetails ="SELECT Id,Name FROM Parcel__c where primary_situs__c != NULL and "
				+ "Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where "
				+ "type__c='CIO') and (Not Name like '100%') and (Not Name like '800%') "
				+ "and (Not Name like '%990') and (Not Name like '134%') Limit 2";
		
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryApnDetails);
		String apn1=responseAPNDetails.get("Name").get(0);		
		String apnId1=responseAPNDetails.get("Id").get(0);
				
		
		Map<String, String> newConstructionWIData = objUtil.generateMapFromJsonFile(manualWIFilePath, 
				                               "DataToCreateWorkItemOfTypeNCWithActionConstructionOther");
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ReportLogger.INFO("Step 1: Login to the Salesforce ");
		objApasGenericPage.login(loginUser);		
		
		objApasGenericPage.searchModule(modules.APAS);
	
		// Step2: Navigating to the Parcel View page								
		String execEnv = System.getProperty("region");

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com"
				+ "/lightning/r/Parcel__c/"
				+ apnId1 + "/view");
		
		
		// Step 3: Creating Manual work item for the Parcel 
		String WINumber = objParcel.createWorkItem(newConstructionWIData);
		if(!WINumber.isEmpty()) {
			
			softAssert.assertTrue(true, "SMAB-T4230: Construction Discovery WI is created successfully");
		}else {
			
			softAssert.assertTrue(false, "SMAB-T4230: Construction Discovery WI is created successfully");
		}				
		
		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWIHomePage.Click(objWIHomePage.detailsTab);
		
		String sqlWIDetails = "select type__c ,sub_type__c ,"
				+ "use_code_f__c , event_id__c ,request_type__c "
				+ "from work_item__c where name = '"+WINumber+"'";
				
		HashMap<String, ArrayList<String>> responseWIDetails= salesforceAPI.select(sqlWIDetails);	
		
		String expectedType = responseWIDetails.get("Type__c").get(0); 
		String expectedAction = responseWIDetails.get("Sub_Type__c").get(0);
		String expectedReference = responseWIDetails.get("Event_ID__c").get(0);
		String expectedRequestType = responseWIDetails.get("Request_Type__c").get(0);
		String expectedRelatedAction = responseWIDetails.get("Sub_Type__c").get(0);
		String expectedEventID = responseWIDetails.get("Event_ID__c").get(0);
		String expectedUseCode = responseWIDetails.get("Use_Code_f__c").get(0);
		
		
		//verify the WI details		
		String actualType = objWIHomePage.getFieldValueFromAPAS("Type"); 
		String actualAction = objWIHomePage.getFieldValueFromAPAS("Action");;
		String actualReference = objWIHomePage.getFieldValueFromAPAS("Reference");;
		String actualRequestType = objWIHomePage.getFieldValueFromAPAS("Request Type");;
		String actualRelatedAction = objWIHomePage.getFieldValueFromAPAS("Related Action");;
		String actualAPN = objWIHomePage.getFieldValueFromAPAS("APN");
		String actualEventID = objWIHomePage.getFieldValueFromAPAS("Event ID");
		String actualUseCode = objWIHomePage.getFieldValueFromAPAS("Use Code");
		
		//Asserrtions
		softAssert.assertEquals(actualType, expectedType, "SMAB-T4231: The WI Type is verified");
		softAssert.assertEquals(actualAction, expectedAction, "SMAB-T4231: The WI Action is verified");
		softAssert.assertEquals(actualReference, expectedReference, "SMAB-T4231: The WI Reference is verified");
		softAssert.assertEquals(actualRequestType, expectedRequestType, "SMAB-T4231: The WI RequestType is verified");
		softAssert.assertEquals(actualRelatedAction, expectedRelatedAction, "SMAB-T4231: The WI Related Action is verified");
		softAssert.assertEquals(actualAPN, apn1, "SMAB-T4231: The WI APN is verified");
		softAssert.assertEquals(actualEventID, expectedEventID, "SMAB-T4231: The WI Event ID is verified");
		softAssert.assertEquals(actualUseCode, expectedUseCode, "SMAB-T4231: The WI Use Code is verified");
		
		objWIHomePage.Click(objWIHomePage.reviewLink);
		
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWIHomePage.switchToNewWindow(parentWindow);
					
		String newConstructionPage = "As part of the new construction process, confirm and update the values as needed:";			
		
		String  constructPage = driver.findElement(By.xpath("//div[@class='slds-rich-text-editor__output uiOutputRichText forceOutputRichText']/p/span")).getText();
		
		boolean flag= false;
	    if(constructPage.equalsIgnoreCase(newConstructionPage)) {
	    	
	    	flag = true;
	    	
	    	softAssert.assertTrue(flag, "SMAB-T4232:" + newConstructionPage);
	    }
		
	}
	
	/*
	 * This method is to create a WI for the Demolition Discovery by the Appraiser
	 * @param loginUser
	 * @throws Exception
	 */

	@Test(description = "SMAB-T4272,SMAB-T4273,SMAB-T4274:RP Demolition Discovery- Verify that Appraisers "
			+ "will have the ability to create a demolition discovery work item from the Component Actions"
			+ " list and its Type label should be NC, the Actions label should be Demo - Other", 
			dataProvider = "RPAppraiser", 
			dataProviderClass = DataProviders.class , 
			groups = {"Smoke","Regression","RP_VAL","WorkItemWorkflow_RP_VAL_Demolition"})
	public void WorkItemWorkflow_RP_VAL_DemolitionManualWICreation(String loginUser) throws Exception {
		
		//Fetching parcel that are Active
		String queryApnDetails ="SELECT Id,Name FROM Parcel__c where primary_situs__c != NULL and "
				+ "Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where "
				+ "type__c='CIO') and (Not Name like '100%') and (Not Name like '800%') "
				+ "and (Not Name like '%990') and (Not Name like '134%') Limit 2";
		
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryApnDetails);
		String apn1=responseAPNDetails.get("Name").get(0);		
		String apnId1=responseAPNDetails.get("Id").get(0);
				
		
		Map<String, String> newConstructionWIData = objUtil.generateMapFromJsonFile(manualWIFilePath, 
				                               "DataToCreateWorkItemOfTypeNCWithActionDemolitionOther");
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ReportLogger.INFO("Step 1: Login to the Salesforce ");
		objApasGenericPage.login(loginUser);
		
		
		objApasGenericPage.searchModule(modules.APAS);
		
		// Step2: Navigating to the Parcel View page								
		String execEnv = System.getProperty("region");

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com"
				+ "/lightning/r/Parcel__c/"
				+ apnId1 + "/view");
				
		// Step 3: Creating Manual work item for the Parcel 
		String WINumber = objParcel.createWorkItem(newConstructionWIData);
		
		if(!WINumber.isEmpty()) {
						
			softAssert.assertTrue(true, "SMAB-T4272: Demolition Discovery WI is created successfully");
		}else {
			
			softAssert.assertTrue(false, "SMAB-T4272: Demolition Discovery WI is created successfully");
		}
						
		
		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWIHomePage.Click(objWIHomePage.detailsTab);
	
		String sqlWIDetails = "select type__c ,sub_type__c ,"
				+ "use_code_f__c , event_id__c ,request_type__c "
				+ "from work_item__c where name = '"+WINumber+"'";
				
		HashMap<String, ArrayList<String>> responseWIDetails= salesforceAPI.select(sqlWIDetails);	
		
		String expectedType = responseWIDetails.get("Type__c").get(0); 
		String expectedAction = responseWIDetails.get("Sub_Type__c").get(0);
		String expectedReference = responseWIDetails.get("Event_ID__c").get(0);
		String expectedRequestType = responseWIDetails.get("Request_Type__c").get(0);
		String expectedRelatedAction = responseWIDetails.get("Sub_Type__c").get(0);
		String expectedEventID = responseWIDetails.get("Event_ID__c").get(0);
		String expectedUseCode = responseWIDetails.get("Use_Code_f__c").get(0);
		
		
		//verify the WI details		
		String actualType = objWIHomePage.getFieldValueFromAPAS("Type"); 
		String actualAction = objWIHomePage.getFieldValueFromAPAS("Action");;
		String actualReference = objWIHomePage.getFieldValueFromAPAS("Reference");;
		String actualRequestType = objWIHomePage.getFieldValueFromAPAS("Request Type");;
		String actualRelatedAction = objWIHomePage.getFieldValueFromAPAS("Related Action");;
		String actualAPN = objWIHomePage.getFieldValueFromAPAS("APN");
		String actualEventID = objWIHomePage.getFieldValueFromAPAS("Event ID");
		String actualUseCode = objWIHomePage.getFieldValueFromAPAS("Use Code");
		
		//Asserrtions
		softAssert.assertEquals(actualType, expectedType, "SMAB-T4273: The WI Type is verified");
		softAssert.assertEquals(actualAction, expectedAction, "SMAB-T4273: The WI Action is verified");
		softAssert.assertEquals(actualReference, expectedReference, "SMAB-T4273: The WI Reference is verified");
		softAssert.assertEquals(actualRequestType, expectedRequestType, "SMAB-T4273: The WI RequestType is verified");
		softAssert.assertEquals(actualRelatedAction, expectedRelatedAction, "SMAB-T4273: The WI Related Action is verified");
		softAssert.assertEquals(actualAPN, apn1, "SMAB-T4273: The WI APN is verified");
		softAssert.assertEquals(actualEventID, expectedEventID, "SMAB-T4273: The WI Event ID is verified");
		softAssert.assertEquals(actualUseCode, expectedUseCode, "SMAB-T4273: The WI Use Code is verified");
		
		objWIHomePage.Click(objWIHomePage.reviewLink);
		
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWIHomePage.switchToNewWindow(parentWindow);
		
		String newConstructionPage = "As part of the new construction process, confirm and update the values as needed:";		
		
		String  constructPage = driver.findElement(By.xpath("//div[@class='slds-rich-text-editor__output uiOutputRichText forceOutputRichText']/p/span")).getText();
		
		boolean flag= false;
	    if(constructPage.equalsIgnoreCase(newConstructionPage)) {
	    	
	    	flag = true;
	    	
	    	softAssert.assertTrue(flag, "SMAB-T4274:" + newConstructionPage);
	    }
		
		
		
	}


}
