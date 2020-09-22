package com.apas.Tests.DisabledVeteran;

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
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.WorkItemMngmntHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.generic.ApasGenericFunctions;
import com.apas.config.*;
import com.apas.Utils.SalesforceAPI;

public class DisabledVeteran_Exemption_WorkItem_Tests extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objExemptionsPage;
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath;
	Map<String, String> rpslData;
	String rpslFileDataPath;
	String newExemptionName;
	WorkItemMngmntHomePage objWIHomePage;
	SalesforceAPI salesforceAPI;
	
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objWIHomePage = new WorkItemMngmntHomePage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
		objApasGenericFunctions.updateRollYearStatus("Closed", "2020");
		rpslFileDataPath = System.getProperty("user.dir") + testdata.RPSL_ENTRY_DATA;
		rpslData= objUtil.generateMapFromJsonFile(rpslFileDataPath, "DataToCreateRPSLEntryForValidation");
		salesforceAPI = new SalesforceAPI();
		
	}
	
	@Test(description = "SMAB-T1922 APAS system should generate a WI on new Exemption Creation", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class , groups = {"regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyWorkItemGeneratedOnExemptionCreation(String loginUser) throws Exception {
	
	Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
	//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
	objApasGenericFunctions.login(loginUser);
	objExemptionsPage.checkRPSLCurrentRollYearAndApproveRPSLPastYears(rpslData);
	
	//Step2: Opening the Exemption Module
	objApasGenericFunctions.searchModule(modules.EXEMPTIONS);
	
	//Step3: create a New Exemption record
	objPage.Click(objExemptionsPage.newExemptionButton);
	
	newExemptionName = objExemptionsPage.createNewExemptionWithMandatoryData(newExemptionData);
	
	String sqlgetWIDetails = objWIHomePage.getWorkItemDetails(newExemptionName, "Submitted for Approval", "Disabled Veterans", "Direct Review and Update", "Initial filing/changes");
	HashMap<String, ArrayList<String>> response  = salesforceAPI.select(sqlgetWIDetails);
    String WIName = response.get("Work_Item__c.Name").get(0);
    String WIRequestType = response.get("Work_Item__c.Request_Type__c").get(0);
    
	
			               
			               
							
							

   }
	
}	
