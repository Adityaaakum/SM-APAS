package com.apas.Tests.PermissionSets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ValueAdjustmentsPage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.PageObjects.ParcelsPage;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
import com.apas.Utils.SalesforceAPI;	

public class DisabledVeterans_SecurityAndSharing_Tests extends TestBase implements testdata, modules, users{

	private RemoteWebDriver driver;
	
	Page objPage = null;
	LoginPage objLoginPage = null;
	ApasGenericFunctions apasGenericObj;
	ValueAdjustmentsPage vaPageObj;
	ExemptionsPage exemptionPageObj;
	SalesforceAPI salesforceAPI;
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath="";
	ParcelsPage parcelObj;
	

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		System.out.println("invoking Before method");
		driver = BrowserDriver.getBrowserInstance();
		vaPageObj=new ValueAdjustmentsPage(driver);
		exemptionPageObj=new ExemptionsPage(driver);
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		apasGenericObj = new ApasGenericFunctions(driver);
		parcelObj=new ParcelsPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
		salesforceAPI = new SalesforceAPI();

	}

	
	// Below test case is used to validate permission access on Exemption and VA's
	
	@Test(description = "SMAB-T474: Verify User is able to create a VAR record",  dataProvider = "rpApprasierAndBPPAuditor",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteranExemption" })
	public void verify_Disabledveteran_NonAdminsAndAppraisalSupportUserNotAbleToCREDExemptionAndValueAdjustment(String loginInvalidUser) throws Exception
	{

		//Fetching the exemption record from API
		String exemptionQuery = "select Name from Exemption__c where Status__c= 'Active' Limit 1";
		String vaQuery="select Name from Value_Adjustments__c where Exemption_Status__c= 'Active' Limit 1";
		HashMap<String, ArrayList<String>> ExemptionRecordMap=salesforceAPI.select(exemptionQuery);
		HashMap<String, ArrayList<String>> vaRecordMap=salesforceAPI.select(vaQuery);
		String exemptionRecord=ExemptionRecordMap.get("Name").get(0);
		String vaRecord=vaRecordMap.get("Name").get(0);
		
			//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginInvalidUser);
			//Step2: Opening the exemption module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//Step3: Verifying new button not available for Rp Apprasier user
			softAssert.assertEquals(apasGenericObj.isNotDisplayed(exemptionPageObj.newExemptionButton), false, "SMAB-T483: User is not able to see New button to create a new Exemption record");
			apasGenericObj.globalSearchRecords(exemptionRecord);
			softAssert.assertEquals(apasGenericObj.isNotDisplayed(exemptionPageObj.editExemption), false, "SMAB-T482: User is not able to edit/delete Exemption record");
			
			ReportLogger.INFO("Verifying user is not able to see New and Edit button for creating/Editing Value adjustments record");
			apasGenericObj.searchModule("Value Adjustments");
			apasGenericObj.globalSearchRecords(vaRecord);
			//exemptionPageObj.checkForAndSelectRecordsInList();
			softAssert.assertEquals(apasGenericObj.isNotDisplayed(exemptionPageObj.editExemption), false, "SMAB-T476,SMAB-T477: User is not able to edit/delete VA record");
			apasGenericObj.logout();
		
	}
	
	// Below test case is used to validate permission access on Roll year
	@Test(description = "SMAB-T642: Verify User is able to create a VAR record",  dataProvider = "loginBppAuditor",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteranExemption"})
	public void verify_Disabledveteran_nonSystemAdminNotAbleToCREDRollYearObjrct(String loginInvalidUser) throws Exception
	{
		String currentYear = objUtil.getCurrentDate("YYYY");
		String rollyearQuery="select Name from Roll_Year_Settings__c where Name= '"+currentYear+"'";
		HashMap<String, ArrayList<String>> rollYearMap=salesforceAPI.select(rollyearQuery);
		String rollYearRecord=rollYearMap.get("Name").get(0);
		//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginInvalidUser);
			//Step2: Opening the exemption module
			apasGenericObj.searchModule(ROLLYEAR);
		
			//Step3: Verifying new button not available for Rp Apprasier user
			softAssert.assertEquals(apasGenericObj.isNotDisplayed(exemptionPageObj.newExemptionButton), false, "SMAB-T642: User is not able to see New button to create a new Roll Year record");
			apasGenericObj.globalSearchRecords(rollYearRecord);
			softAssert.assertEquals(apasGenericObj.isNotDisplayed(exemptionPageObj.editExemption), false, "SMAB-T482: User is not able to edit/delete Roll Year record");
			
			apasGenericObj.logout();
		
	}
	
	
	
	
}
		
		
	
	