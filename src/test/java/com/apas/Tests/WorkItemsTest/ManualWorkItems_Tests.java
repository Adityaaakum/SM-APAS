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

public class ManualWorkItems_Tests extends TestBase implements testdata, modules, users {
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
	 * This method is to verify that user is able to view 'Use Code' and 'Street fields getting automatically populated in the work item record related to the linked Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1994:verify that user is able to view 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual" })
	public void WorkItems_VerifyLinkedParcelUseCodeStreetFields(String loginUser) throws Exception {
		String puc;
		String primarySitus;		
		
		// fetching a parcel where PUC and Primary Situs are not blank		
		String queryAPNValue = "select Name from Parcel__c where PUC_Code_Lookup__c!= null and Primary_Situs__c !=null AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String apnValue= response.get("Name").get(0);
		
		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeRP");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC and Primary Situs field (Street) have values saved
		objApasGenericFunctions.searchModule(PARCELS);
		objApasGenericFunctions.globalSearchRecords(apnValue);

		// fetching the PUC and Primary Situs fields values of parcel
		puc = objApasGenericFunctions.getFieldValueFromAPAS("PUC", "Parcel Information");
		primarySitus = objApasGenericFunctions.getFieldValueFromAPAS("Primary Situs", "Parcel Information");

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,
				"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertTrue(primarySitus.contains(objApasGenericFunctions.getFieldValueFromAPAS("Street", "Reference Data Details")),
				"SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");

		objApasGenericFunctions.logout();
	}
	
	/**
	 * This method is to verify that user is able to view 'Use Code' which is  not blank but 'Street' field get automatically populated in the work item record related to the linked Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1994:verify that user is able to view 'Use Code' which is  blank but 'Street' field get automatically populated in the work item record related to the linked Parcel", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual"  })
	public void WorkItems_VerifyLinkedParcelUseCodeNotBlank_StreetFieldBlank(String loginUser) throws Exception {
		String puc;
		
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

		// fetching the PUC and Primary Situs fields values of parcel
		puc = objApasGenericFunctions.getFieldValueFromAPAS("PUC", "Parcel Information");

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		//Step 4:Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Street' is blank and 'Use CoDE' field gets automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Street", "Reference Data Details"),"","SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");

		objApasGenericFunctions.logout();
	}
	/**
	 * This method is to Verify User is able to view 'Roll Code' and 'Date' fields getting automatically populated in the work item record linked to a BPP Account
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2075:Verify User is able to view 'Roll Code' and 'Date' fields getting automatically populated in the work item record linked to a BPP Account", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual"  })
	public void WorkItems_VerifyLinkedBPPAccountUseCode_DateFields(String loginUser) throws Exception {
		
		// fetching a BPP account where Roll code  is not blank 
		String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String bppAccount= response.get("Name").get(0);
		
		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeBPP");
				
		// Step1: Login to the APAS application using the credentials passed through dataprovider (BPP Business Admin)
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the BPP Accounts page  and searching a BPP Account where Roll code is not blank
		objApasGenericFunctions.searchModule(BPP_ACCOUNTS);
		objApasGenericFunctions.globalSearchRecords("1234");

		// Step 3: Creating Manual work item for the BPP Account 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		//Step 4:Clicking the  details tab for the work item newly created and fetching the RoLL code and Date Fields values
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Roll Code' field and 'Date' field gets automatically populated in the work item record related to the linked BPP ACCOUNT
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Roll Code", "Reference Data Details"),"UNS - Secured",
				"SMAB-T2075: Validation that 'Roll Code' fields getting automatically populated in the work item record related to the linked BPP Account");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Date", "Information"),"1/1/2021",
				"SMAB-T2075: Validation that 'Date' fields is equal to the 1/1/{Roll Year}");

		objApasGenericFunctions.logout();
	}

}