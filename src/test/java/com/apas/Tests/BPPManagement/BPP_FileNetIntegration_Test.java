package com.apas.Tests.BPPManagement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppManagementPage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.EFileImportLogsPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.BPPTablesData;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.relevantcodes.extentreports.LogStatus;

public class BPP_FileNetIntegration_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	BppManagementPage objBppManagementPage;
	EFileImportPage objEfileHomePage;
	EFileImportLogsPage objEFileImportLogPage;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	SalesforceAPI objSalesforceAPI = new SalesforceAPI();
	WorkItemHomePage objWorkItemHomePage;
	
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		
		objPage = new Page(driver);
		objBppManagementPage = new BppManagementPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objEFileImportLogPage=new EFileImportLogsPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objEfileHomePage.updateRollYearStatus("Open", rollYear);

	}
	
	/**
	 * DESCRIPTION: Validate Filenet document button on property record
	 */

	@Test(description = "SMAB-T4219: Verify filenet documents on property record page", dataProvider = "BPPAdminAndPrincipalAndAppraisalSupport", dataProviderClass = DataProviders.class, groups = {
			"Regression", "FileNetIntegration", "BPPManagement" })
	public void BPP_ValidateFileNetDocument(String loginUser) throws Exception {

		String execEnv = System.getProperty("region");		
		
		//STEP 1: Login as loginUser
		ReportLogger.INFO("Login as Appriaser user");
		objBppManagementPage.login(loginUser);
		String propertyStamentQuery = "SELECT Account_Number__c,id FROM Property_Statement__c where Account_Number__c!=null";
		String propertyStamentID = objSalesforceAPI.select(propertyStamentQuery).get("Id").get(0);
		String accountNumber = objSalesforceAPI.select(propertyStamentQuery).get("Account_Number__c").get(0);
		
		//STEP 2: Navigate to property statement record
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Property_Statement__c/"
				+ propertyStamentID + "/view");
		ReportLogger.INFO("Navigated to Property statement record");
		objBppManagementPage.waitForElementToBeClickable(20, objBppManagementPage.viewFileNetDocumentsButton);
		objWorkItemHomePage
				.Click(objWorkItemHomePage.getButtonWithText(objBppManagementPage.viewFileNetDocumentsButton));
		ReportLogger.INFO("Clicked on View File Net Documents Button");
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		String currentUrl = driver.getCurrentUrl();
		
		//STEP 3: Validate FinetDocument link
		softAssert.assertContains(currentUrl, "acrefncndev.smcare.org:", "SMAB-T4219: Navigated to FilenetDocument");
		softAssert.assertContains(currentUrl, accountNumber, "SMAB-T4219: Navigated to FilenetDocument");
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Property_Statement__c/"
				+ propertyStamentID + "/view");
		objBppManagementPage.waitForElementToBeClickable(20, objBppManagementPage.viewFileNetDocumentsButton);
		objBppManagementPage.logout();

	}

}
