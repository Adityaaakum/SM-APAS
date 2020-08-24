package com.apas.Tests.SecurityAndSharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.apas.PageObjects.*;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Reports.ReportLogger;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.apas.Utils.SalesforceAPI;

public class DisabledVeterans_SecurityAndSharing_Tests extends TestBase implements testdata, modules, users{

	private RemoteWebDriver driver;
	Page objPage = null;
	LoginPage objLoginPage = null;
	ApasGenericFunctions apasGenericObj;
	ValueAdjustmentsPage vaPageObj;
	ExemptionsPage exemptionPageObj;
	RealPropertySettingsLibrariesPage objRPSLPage;
	SalesforceAPI salesforceAPI;
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath="";
	ParcelsPage parcelObj;

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{

		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		vaPageObj=new ValueAdjustmentsPage(driver);
		exemptionPageObj=new ExemptionsPage(driver);
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		apasGenericObj = new ApasGenericFunctions(driver);
		objRPSLPage = new RealPropertySettingsLibrariesPage(driver);
		parcelObj=new ParcelsPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
		salesforceAPI = new SalesforceAPI();
		apasGenericObj.updateRollYearStatus("Closed", "2020");

	}

	// Below test case is used to validate permission access on Exemption and VA's
	@Test(description = "SMAB-T483,SMAB-T482,SMAB-T476,SMAB-T477: Verify User without permission is not able to create a new Exemption, VA record and RPSL",  dataProvider = "rpApprasierAndBPPAuditor",dataProviderClass = DataProviders.class, groups = {"smoke", "regression","DisabledVeteranExemption" })
	public void DisabledVeteran_AccessValidation_RPAndBPPAppraiser(String loginInvalidUser) throws Exception {

		//Fetching the exemption record from API
		String exemptionQuery = "select Name from Exemption__c where Status__c= 'Active' Limit 1";
		String vaQuery="select Name from Value_Adjustments__c where Exemption_Status__c= 'Active' Limit 1";
		HashMap<String, ArrayList<String>> ExemptionRecordMap=salesforceAPI.select(exemptionQuery);
		HashMap<String, ArrayList<String>> vaRecordMap=salesforceAPI.select(vaQuery);
		String exemptionRecord=ExemptionRecordMap.get("Name").get(0);
		String vaRecord=vaRecordMap.get("Name").get(0);
		
		//Step1: Login to the APAS application using the credentials passed through data provider
		apasGenericObj.login(loginInvalidUser);

		//Step3: Verifying user is not able to see New and Edit button for creating/Editing Exemption record
		ReportLogger.INFO("Verifying user is not able to see New and Edit button for creating/Editing Exemption record");
		apasGenericObj.searchModule(EXEMPTIONS);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T483: User is not able to see New button to create a new Exemption record");
		apasGenericObj.globalSearchRecords(exemptionRecord);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.editExemption),  "SMAB-T482: User is not able to edit Exemption record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption),  "SMAB-T482: User is not able to delete Exemption record");

		//Step4: Verifying user is not able to see New and Edit button for creating/Editing Value Adjustments record
		ReportLogger.INFO("Verifying user is not able to see New and Edit button for creating/Editing Value Adjustments record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T476,SMAB-T477: User is not able to see New button to create a Value Adjustment record");
		apasGenericObj.searchModule("Value Adjustments");
		apasGenericObj.globalSearchRecords(vaRecord);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.editExemption), "SMAB-T476,SMAB-T477: User is not able to edit VA record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption), "SMAB-T476,SMAB-T477: User is not able to delete VA record");

		//Step4: Verify the user access on Real Property Settings Library screen
		ReportLogger.INFO("Verifying user is not able to see New and Edit button for creating/Editing Real Property Settings Library");
		apasGenericObj.searchModule(REAL_PROPERTY_SETTINGS_LIBRARIES);
		softAssert.assertTrue(!objPage.verifyElementVisible(objRPSLPage.newButton), "SMAB-T476,SMAB-T477: User is not able to see New button to create a Real Property Settings Library record");
		apasGenericObj.displayRecords("All");
		Map<String, ArrayList<String>> manualRPSLGridDataMap = apasGenericObj.getGridDataInHashMap();
		apasGenericObj.globalSearchRecords(manualRPSLGridDataMap.get("RP Setting Name").get(0));
		softAssert.assertTrue(!objPage.verifyElementVisible(objRPSLPage.editButton), "SMAB-T476,SMAB-T477: User is not able to Edit Real Property Settings Library record");
		softAssert.assertTrue(!objPage.verifyElementVisible(objRPSLPage.deleteButton), "SMAB-T476,SMAB-T477: User is not able to Delete Real Property Settings Library record");

		//Logging out of the application
		apasGenericObj.logout();
		
	}
	
	// Below test case is used to validate permission access on Roll year
	@Test(description = "SMAB-T642,SMAB-T482:Verify User without permission is not able to create a new Roll Year record",  dataProvider = "loginBppAuditor",dataProviderClass = DataProviders.class, groups = {"regression","DisabledVeteranExemption"})
	public void DisabledVeteran_nonSystemAdminNotAbleToCREDRollYearObject(String loginInvalidUser) throws Exception {

		String currentYear = objUtil.getCurrentDate("YYYY");
		String rollyearQuery="select Name from Roll_Year_Settings__c where Name= '"+currentYear+"'";
		HashMap<String, ArrayList<String>> rollYearMap=salesforceAPI.select(rollyearQuery);
		String rollYearRecord=rollYearMap.get("Name").get(0);

		//Step1: Login to the APAS application using the credentials passed through data provider
		apasGenericObj.login(loginInvalidUser);
		//Step2: Opening the exemption module
		apasGenericObj.searchModule(ROLLYEAR);

		//Step3: Verifying new button not available for Rp Apprasier user
		softAssert.assertTrue(apasGenericObj.isNotDisplayed(exemptionPageObj.newExemptionButton),  "SMAB-T642: User is not able to see New button to create a new Roll Year record");
		apasGenericObj.globalSearchRecords(rollYearRecord);
		softAssert.assertTrue(apasGenericObj.isNotDisplayed(exemptionPageObj.editExemption), "SMAB-T482: User is not able to edit/delete Roll Year record");

		apasGenericObj.logout();
		
	}

	// Below test case is used to validate permission access on Exemption and VA's for BPP Admin
	@Test(description = "SMAB-T483,SMAB-T482,SMAB-T476,SMAB-T477: Verify BPP Admin should be able to create a new Exemption and VA record",  dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_AccessValidation_BPPAdmin(String loginUser) throws Exception {

		//Fetching the exemption record from API
		String exemptionQuery = "select Name from Exemption__c where Status__c= 'Active' Limit 1";
		String vaQuery="select Name from Value_Adjustments__c where Exemption_Status__c= 'Active' Limit 1";
		HashMap<String, ArrayList<String>> ExemptionRecordMap=salesforceAPI.select(exemptionQuery);
		HashMap<String, ArrayList<String>> vaRecordMap=salesforceAPI.select(vaQuery);
		String exemptionRecord=ExemptionRecordMap.get("Name").get(0);
		String vaRecord=vaRecordMap.get("Name").get(0);

		//Step1: Login to the APAS application using the credentials passed through data provider
		apasGenericObj.login(loginUser);

		//Step2: Verifying the access on Exemption for logged in user
		ReportLogger.INFO("Verifying user is able to see New and Edit button for creating/Editing Exemptions record");
		apasGenericObj.searchModule(EXEMPTIONS);
		softAssert.assertTrue(objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T483: User is able to see New button to create a new Exemption record");
		apasGenericObj.globalSearchRecords(exemptionRecord);
		softAssert.assertTrue(objPage.verifyElementVisible(exemptionPageObj.editExemption),  "SMAB-T482: User is able to edit Exemption record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption),  "SMAB-T482: User is not able to delete Exemption record");

		//Step3: Verify the user access on Value Adjustment Record screen
		ReportLogger.INFO("Verifying user is not able to see New and Edit button for creating/Editing Value adjustments record");
		apasGenericObj.searchModule(VALUE_ADJUSTMENTS);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T476,SMAB-T477: User is not able to see New button to create a Value Adjustment record");
		apasGenericObj.globalSearchRecords(vaRecord);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.editExemption), "SMAB-T476,SMAB-T477: User is not able to edit VA record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption), "SMAB-T476,SMAB-T477: User is not able to delete VA record");

		//Step4: Verify the user access on Real Property Settings Library screen
		ReportLogger.INFO("Verifying user is able to see New and Edit button for creating/Editing Real Property Settings Library");
		apasGenericObj.searchModule(REAL_PROPERTY_SETTINGS_LIBRARIES);
		softAssert.assertTrue(objPage.verifyElementVisible(objRPSLPage.newButton), "SMAB-T476,SMAB-T477: User is able to see New button to create a Real Property Settings Library record");
		apasGenericObj.displayRecords("All");
		Map<String, ArrayList<String>> manualRPSLGridDataMap = apasGenericObj.getGridDataInHashMap();
		apasGenericObj.globalSearchRecords(manualRPSLGridDataMap.get("RP Setting Name").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objRPSLPage.editButton), "SMAB-T476,SMAB-T477: User is able to Edit Real Property Settings Library record");
		softAssert.assertTrue(!objPage.verifyElementVisible(objRPSLPage.deleteButton), "SMAB-T476,SMAB-T477: User is not able to Delete Real Property Settings Library record");

		//Logging out of the application
		apasGenericObj.logout();

	}

	// Below test case is used to validate permission access on Exemption and VA's for RP Admin
	@Test(description = "SMAB-T483,SMAB-T482,SMAB-T476,SMAB-T477: Verify RP Admin should be able to create a new Exemption and VA record",  dataProvider = "loginRPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_AccessValidation_RPAdmin(String loginUser) throws Exception {

		//Fetching the exemption record from API
		String exemptionQuery = "select Name from Exemption__c where Status__c= 'Active' Limit 1";
		String vaQuery="select Name from Value_Adjustments__c where Exemption_Status__c= 'Active' Limit 1";
		HashMap<String, ArrayList<String>> ExemptionRecordMap=salesforceAPI.select(exemptionQuery);
		HashMap<String, ArrayList<String>> vaRecordMap=salesforceAPI.select(vaQuery);
		String exemptionRecord=ExemptionRecordMap.get("Name").get(0);
		String vaRecord=vaRecordMap.get("Name").get(0);

		//Step1: Login to the APAS application using the credentials passed through data provider
		apasGenericObj.login(loginUser);

		//Step2: Verifying the access on Exemption for logged in user
		ReportLogger.INFO("Verifying user is able to see New and Edit button for creating/Editing Exemptions record");
		apasGenericObj.searchModule(EXEMPTIONS);
		softAssert.assertTrue(objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T483: User is able to see New button to create a new Exemption record");
		apasGenericObj.globalSearchRecords(exemptionRecord);
		softAssert.assertTrue(objPage.verifyElementVisible(exemptionPageObj.editExemption),  "SMAB-T482: User is able to edit Exemption record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption),  "SMAB-T482: User is not able to delete Exemption record");

		//Step3: Verify the user access on Value Adjustment Record screen
		ReportLogger.INFO("Verifying user is not able to see New and Edit button for creating/Editing Value adjustments record");
		apasGenericObj.searchModule(VALUE_ADJUSTMENTS);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T476,SMAB-T477: User is not able to see New button to create a Value Adjustment record");
		apasGenericObj.globalSearchRecords(vaRecord);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.editExemption), "SMAB-T476,SMAB-T477: User is not able to edit VA record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption), "SMAB-T476,SMAB-T477: User is not able to delete VA record");

		//Step4: Verify the user access on Real Property Settings Library screen
		ReportLogger.INFO("Verifying user is able to see New and Edit button for creating/Editing Real Property Settings Library");
		apasGenericObj.searchModule(REAL_PROPERTY_SETTINGS_LIBRARIES);
		softAssert.assertTrue(objPage.verifyElementVisible(objRPSLPage.newButton), "SMAB-T476,SMAB-T477: User is able to see New button to create a Real Property Settings Library record");
		apasGenericObj.displayRecords("All");
		Map<String, ArrayList<String>> manualRPSLGridDataMap = apasGenericObj.getGridDataInHashMap();
		apasGenericObj.globalSearchRecords(manualRPSLGridDataMap.get("RP Setting Name").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objRPSLPage.editButton), "SMAB-T476,SMAB-T477: User is able to Edit Real Property Settings Library record");
		softAssert.assertTrue(!objPage.verifyElementVisible(objRPSLPage.deleteButton), "SMAB-T476,SMAB-T477: User is not able to Delete Real Property Settings Library record");

		//Logging out of the application
		apasGenericObj.logout();

	}

	// Below test case is used to validate permission access on Exemption and VA's for Appraisal Support
	@Test(description = "SMAB-T483,SMAB-T482,SMAB-T476,SMAB-T477: Verify Appraisal Support should be able to create a new Exemption and VA record",  dataProvider = "loginApraisalUser",dataProviderClass = DataProviders.class, groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_AccessValidation_AppraisalSupport(String loginUser) throws Exception {

		//Fetching the exemption record from API
		String exemptionQuery = "select Name from Exemption__c where Status__c= 'Active' Limit 1";
		String vaQuery="select Name from Value_Adjustments__c where Exemption_Status__c= 'Active' Limit 1";
		HashMap<String, ArrayList<String>> ExemptionRecordMap=salesforceAPI.select(exemptionQuery);
		HashMap<String, ArrayList<String>> vaRecordMap=salesforceAPI.select(vaQuery);
		String exemptionRecord=ExemptionRecordMap.get("Name").get(0);
		String vaRecord=vaRecordMap.get("Name").get(0);

		//Step1: Login to the APAS application using the credentials passed through data provider
		apasGenericObj.login(loginUser);

		//Step2: Verifying the access on Exemption for logged in user
		ReportLogger.INFO("Verifying user is able to see New and Edit button for creating/Editing Exemptions record");
		apasGenericObj.searchModule(EXEMPTIONS);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T483: User is not able to see New button to create a new Exemption record");
		apasGenericObj.globalSearchRecords(exemptionRecord);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.editExemption),  "SMAB-T482: User is not able to edit Exemption record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption),  "SMAB-T482: User is not able to delete Exemption record");

		//Step3: Verify the user access on Value Adjustment Record screen
		ReportLogger.INFO("Verifying user is able to see New and Edit button for creating/Editing Value adjustments record");
		apasGenericObj.searchModule(VALUE_ADJUSTMENTS);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T483: User is not able to see New button to create a Value Adjustment record");
		apasGenericObj.globalSearchRecords(vaRecord);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.editExemption), "SMAB-T476: User is not able to edit VA record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption), "SMAB-T476: User is not able to delete VA record");

		//Step4: Verify the user access on Real Property Settings Library screen
		ReportLogger.INFO("Verifying user is able to see New and Edit button for creating/Editing Real Property Settings Library");
		apasGenericObj.searchModule(REAL_PROPERTY_SETTINGS_LIBRARIES);
		softAssert.assertTrue(!objPage.verifyElementVisible(objRPSLPage.newButton), "SMAB-T483: User is not able to see New button to create a Real Property Settings Library record");
		apasGenericObj.displayRecords("All");
		Map<String, ArrayList<String>> manualRPSLGridDataMap = apasGenericObj.getGridDataInHashMap();
		apasGenericObj.globalSearchRecords(manualRPSLGridDataMap.get("RP Setting Name").get(0));
		softAssert.assertTrue(!objPage.verifyElementVisible(objRPSLPage.editButton), "SMAB-T476: User is not able to Edit Real Property Settings Library record");
		softAssert.assertTrue(!objPage.verifyElementVisible(objRPSLPage.deleteButton), "SMAB-T476: User is not able to Delete Real Property Settings Library record");

		//Logging out of the application
		apasGenericObj.logout();

	}


	// Below test case is used to validate permission access on Exemption and VA's for RP Admin
	@Test(description = "SMAB-T483,SMAB-T482,SMAB-T476,SMAB-T477: Verify Exemption Support Staff access on Exemption, VA record and RPSL",  dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_AccessValidation_ExemptionSupportStaff(String loginUser) throws Exception {

		//Fetching the exemption record from API
		String exemptionQuery = "select Name from Exemption__c where Status__c= 'Active' Limit 1";
		String vaQuery="select Name from Value_Adjustments__c where Exemption_Status__c= 'Active' Limit 1";
		HashMap<String, ArrayList<String>> ExemptionRecordMap=salesforceAPI.select(exemptionQuery);
		HashMap<String, ArrayList<String>> vaRecordMap=salesforceAPI.select(vaQuery);
		String exemptionRecord=ExemptionRecordMap.get("Name").get(0);
		String vaRecord=vaRecordMap.get("Name").get(0);

		//Step1: Login to the APAS application using the credentials passed through data provider
		apasGenericObj.login(loginUser);

		//Step2: Verifying the access on Exemption for logged in user
		ReportLogger.INFO("Verifying user is able to see New and Edit button for creating/Editing Exemptions record");
		apasGenericObj.searchModule(EXEMPTIONS);
		softAssert.assertTrue(objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T483: User is able to see New button to create a new Exemption record");
		apasGenericObj.globalSearchRecords(exemptionRecord);
		softAssert.assertTrue(objPage.verifyElementVisible(exemptionPageObj.editExemption),  "SMAB-T482: User is able to edit Exemption record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption),  "SMAB-T482: User is not able to delete Exemption record");

		//Step3: Verify the user access on Value Adjustment Record screen
		ReportLogger.INFO("Verifying user is not able to see New and Edit button for creating/Editing Value adjustments record");
		apasGenericObj.searchModule(VALUE_ADJUSTMENTS);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T476,SMAB-T477: User is not able to see New button to create a Value Adjustment record");
		apasGenericObj.globalSearchRecords(vaRecord);
		softAssert.assertTrue(objPage.verifyElementVisible(exemptionPageObj.editExemption), "SMAB-T476,SMAB-T477: User is not able to edit VA record");
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.deleteExemption), "SMAB-T476,SMAB-T477: User is not able to delete VA record");

		//Step4: Verify the user access on Real Property Settings Library screen
		ReportLogger.INFO("Verifying user is able to see New and Edit button for creating/Editing Real Property Settings Library");
		apasGenericObj.searchModule(REAL_PROPERTY_SETTINGS_LIBRARIES);
		softAssert.assertTrue(objPage.verifyElementVisible(objRPSLPage.newButton), "SMAB-T476,SMAB-T477: User is able to see New button to create a Real Property Settings Library record");
		apasGenericObj.displayRecords("All");
		Map<String, ArrayList<String>> manualRPSLGridDataMap = apasGenericObj.getGridDataInHashMap();
		apasGenericObj.globalSearchRecords(manualRPSLGridDataMap.get("RP Setting Name").get(0));
		softAssert.assertTrue(objPage.verifyElementVisible(objRPSLPage.editButton), "SMAB-T476,SMAB-T477: User is able to Edit Real Property Settings Library record");
		softAssert.assertTrue(!objPage.verifyElementVisible(objRPSLPage.deleteButton), "SMAB-T476,SMAB-T477: User is not able to Delete Real Property Settings Library record");

		//Logging out of the application
		apasGenericObj.logout();

	}


}
		
		
	
	