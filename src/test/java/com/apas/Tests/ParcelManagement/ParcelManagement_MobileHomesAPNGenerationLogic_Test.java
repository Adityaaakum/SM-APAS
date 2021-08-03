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
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class ParcelManagement_MobileHomesAPNGenerationLogic_Test extends TestBase implements testdata, modules, users {
	
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

	@Test(description = "SMAB-T3355: verify that when creating new mobile home parcels"
			+ "(parcels that start with 134) the system should "
			+ "allow the user to skip the sequential numbers.", 
			dataProvider = "loginMappingUser", 
			dataProviderClass = DataProviders.class, 
			groups = {"Regression","ParcelManagement","NewAPNGenMobHomes" },
			enabled= true)
	public void ParcelManagement_VerifyNewAPNGenerationForMobileHomes(String loginUser) throws Exception {
		
		//Fetch Active APN
    	ArrayList<String> apns = objMappingPage.fetchActiveAPN(2);
    	String apn = apns.get(0);
    	
    	//Setup data to create Manual WI
    			String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
    			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
    					"DataToCreateWorkItemOfTypeMappingWithActionMobileHomeRequest");
    			
    			//Step1: Login to the APAS application
    			objMappingPage.login(loginUser);

    			//Step2: Opening the PARCELS page and searching the parcel to create manual WI
    			objMappingPage.searchModule(PARCELS);
    			objMappingPage.globalSearchRecords(apn);		
    			
    			//Step 3: Creating Manual work item for the Parcel
    			String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
    	        
    			
    			//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
    			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
    			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
    			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
    			String parentWindow = driver.getWindowHandle();	
    			objWorkItemHomePage.switchToNewWindow(parentWindow);
    			objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
    			 
    			String mappingActionCreationData =  testdata.Brand_New_Parcel_MAPPING_ACTION;

    			Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
    					"DataToPerformBrandNewParcelMappingActionWithoutAllFields");

    			objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));
                
    			String slqLastParcelNumber = "SELECT Name FROM Parcel__c where Name LIKE '134%' order by Name desc";    			    			
    					
    			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(slqLastParcelNumber);
    			String activeParcelToPerformMapping=responseAPNDetails.get("Name").get(0);
    			
    			String nextAPNGenerated = objMappingPage.generateNextAvailableAPN(activeParcelToPerformMapping);
    			
    			nextAPNGenerated = objMappingPage.generateNextAvailableAPN(activeParcelToPerformMapping);
    			
    			objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel,nextAPNGenerated);
    			
    			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
    			Thread.sleep(2000);
    			
    			objMappingPage.editGridCellValue("Dist/Nbhd*","06/06");
    			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
    			
    			softAssert.assertContains(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
    	                "SMAB-T3355: Validation that success message is displayed when Parcels are generated for ");
    			
	}


}
