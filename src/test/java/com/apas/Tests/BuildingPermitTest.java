package com.apas.Tests;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.CustomSoftAssert;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.EFileImportTransactions;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtils;
import com.apas.config.apps;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.SalsesforceStandardFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermitTest extends TestBase implements testdata,apps,users {
	
	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	EFileImportTransactions objEfileImportTransactionsPage;
	SalsesforceStandardFunctions salesforceStandardFunctions;
	CustomSoftAssert softassert = new CustomSoftAssert();
	DateUtils dateUtil = new DateUtils();
	
	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactions(driver);	
		salesforceStandardFunctions = new SalsesforceStandardFunctions(driver);
	}

	@AfterMethod
	public void afterMethod() throws IOException{
		salesforceStandardFunctions.logout();
	}
	
	@Test(description = "Transaction record verification for the imported Building Permit in TXT Format", priority = 0, alwaysRun = true)
	public void transactionRecordVerificationBuildingPermitTXT() throws Exception {
		
		salesforceStandardFunctions.login(BUSINESS_ADMIN);
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the atherton building permit file");
		String athertonBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_ATHERTON;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", "January 2020",athertonBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		salesforceStandardFunctions.searchApps(EFILE_IMPORT_TRANSACTIONS);
		objPage.Click(driver.findElement(By.xpath("//a[contains(.,'Import Transaction-00')]")));
		softassert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		softassert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Imported", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		softassert.assertTrue(objPage.getElementText(objEfileImportTransactionsPage.efileImportLogLabel).contains("Atherton Building Permits"), "SMAB-T430: Validation that latest generated transaction log is for Atherton Building Permits");
	}
	
	@Test(description = "Transaction record verification for the imported Building Permit in XLS Format", priority = 1, alwaysRun = true)
	public void transactionRecordVerificationBuildingPermitXLS() throws Exception {
			
		salesforceStandardFunctions.login(BUSINESS_ADMIN);
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the San Mateo permit file");
		String sanMateoBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_SAN_MATEO;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "San Mateo Building permits", "January 2020",sanMateoBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		salesforceStandardFunctions.searchApps(EFILE_IMPORT_TRANSACTIONS);
		objPage.Click(driver.findElement(By.xpath("//a[contains(.,'Import Transaction-00')]")));
		softassert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		softassert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Imported", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		softassert.assertTrue(objPage.getElementText(objEfileImportTransactionsPage.efileImportLogLabel).contains("San Mateo Building permits"), "SMAB-T430: Validation that latest generated transaction log is for San Mateo Building permits");
	}
	
}
