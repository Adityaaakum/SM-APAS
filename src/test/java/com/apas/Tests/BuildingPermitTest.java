package com.apas.Tests;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportTransactions;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.apps;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.SalsesforceStandardFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermitTest extends TestBase implements testdata, apps, users {

	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	EFileImportTransactions objEfileImportTransactionsPage;
	SalsesforceStandardFunctions salesforceStandardFunctions;
	BuildingPermitPage objBuildPermit;
	Util objUtil;
	SoftAssertion softAssert;
	
	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactions(driver);
		salesforceStandardFunctions = new SalsesforceStandardFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}
	
	@AfterTest
	public void afterTest() throws IOException{
		salesforceStandardFunctions.logout();
		softAssert.assertAll();
	}
	
	@Test(description = "Transaction record verification for the imported Building Permit in TXT Format", priority = 0, alwaysRun = true, enabled = false)
	public void transactionRecordVerificationBuildingPermitTXT() throws Exception {
		
		salesforceStandardFunctions.login(BUSINESS_ADMIN);
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the atherton building permit file");
		String athertonBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_ATHERTON;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", "January 2020",athertonBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		salesforceStandardFunctions.searchApps(EFILE_IMPORT_TRANSACTIONS);
		objPage.Click(driver.findElement(By.xpath("//a[contains(.,'Import Transaction-00')]")));
		softAssert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Importedsdfasdf", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		softAssert.assertTrue(objPage.getElementText(objEfileImportTransactionsPage.efileImportLogLabel).contains("Atherton Building Permits"), "SMAB-T430: Validation that latest generated transaction log is for Atherton Building Permits");
	}
	
	@Test(description = "Transaction record verification for the imported Building Permit in XLS Format", priority = 1, alwaysRun = true, enabled = false)
	public void transactionRecordVerificationBuildingPermitXLS() throws Exception {
			
		salesforceStandardFunctions.login(BUSINESS_ADMIN);
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the San Mateo permit file");
		String sanMateoBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_SAN_MATEO;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "San Mateo Building permits", "January 2020",sanMateoBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		salesforceStandardFunctions.searchApps(EFILE_IMPORT_TRANSACTIONS);
		objPage.Click(driver.findElement(By.xpath("//a[contains(.,'Import Transaction-00')]")));
		softAssert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Imported", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		softAssert.assertTrue(objPage.getElementText(objEfileImportTransactionsPage.efileImportLogLabel).contains("San Mateo Building permits"), "SMAB-T430: Validation that latest generated transaction log is for San Mateo Building permits");
	}

	@Test(description = "Creating manual entry for building permit", groups = {"smoke"}, priority = 2, enabled = true)
	public void bldngPrmtsCreateManualEntry() throws Exception {
		salesforceStandardFunctions.login(BUSINESS_ADMIN);
		salesforceStandardFunctions.searchApps(BUILDING_PERMITS);
						
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating manual entry without entering mandatory fields to validate error messages.");
		objBuildPermit.openManualEntryForm();
		objBuildPermit.saveManualEntry();
		List<String> errorsList = objBuildPermit.retrieveMandatoryFieldsValidationErrorMsgs();
		String expMsgInPopUpHeader = CONFIG.getProperty("expectedErrorMsgForAllMandatoryFields");
		String actMsgInPopUpHeader = errorsList.get(0);
		softAssert.assertEquals(actMsgInPopUpHeader, expMsgInPopUpHeader, "SMAB-T418: Validating mandatory fields missing error in manual entry pop up's header.");
		
		String expMsgForIndividualField = CONFIG.getProperty("expectedErrorMsgForIndividualMandatoryField");
		String actMsgForIndividualField = errorsList.get(1);	
		softAssert.assertEquals(expMsgForIndividualField, actMsgForIndividualField, "SMAB-T418: Validating mandatory fields missing error against individual fields");
		
		int fieldsCountInHeaderMsg = Integer.parseInt(errorsList.get(2));		
		int individualMsgsCount = Integer.parseInt(errorsList.get(3));
		softAssert.assertEquals(fieldsCountInHeaderMsg, individualMsgsCount, "SMAB-T418: Validating count of field names in header msg against count of individual error msgs");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating to ensure entry is not created when all mandatory fields filled and aborted without saving.");
		String newManualEntryData = System.getProperty("user.dir") + BUILDING_PERMIT_MANUAL_ENTRY_DATA;
		Map<String, String> dataMap = objUtil.generateMapFromDataFile(newManualEntryData);
		
		objBuildPermit.enterManualEntryData(dataMap);
		objBuildPermit.abortManualEntry();
		boolean buildingPermitNotCreated = objBuildPermit.checkBuildingPermitOnGrid();
		softAssert.assertFalse(buildingPermitNotCreated, "SMAB-T418: Validating whether manual entry successfully aborted without saving.");

		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating a manaul entry and initiating a new one by choosing 'Save & New' option and validating new manual entry window gets populated.");
		objBuildPermit.openManualEntryForm();
		objBuildPermit.enterManualEntryData(dataMap);
		boolean isNewManualEntryPopUpDisplayed = objBuildPermit.saveManualEntryAndExit();
		softAssert.assertTrue(isNewManualEntryPopUpDisplayed, "SMAB-T418: Validating whether pop up for new entry is displayed.");

		ExtentTestManager.getTest().log(LogStatus.INFO, "Validating newly created manual entry successfully displayed on details page.");
		boolean isNewManualEntryDisplayedOnDetailsPage = objBuildPermit.checkBuildingPermitOnDetailsPage();
		softAssert.assertTrue(isNewManualEntryDisplayedOnDetailsPage, "SMAB-T418: Validating whether manual entry successfully created and displayed on details page.");
	}
}
