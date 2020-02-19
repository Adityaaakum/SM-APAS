package com.apas.Tests;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileHomePage;
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
	EFileHomePage objEfileHomePage;
	Util objUtil;
	SoftAssertion softAssert;
	
	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactions(driver);
		objEfileHomePage = new EFileHomePage(driver);
		salesforceStandardFunctions = new SalsesforceStandardFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}
	
    @DataProvider(name = "loginUsers")
    public Object[][] dataProviderLoginUserMethod() {
        return new Object[][] { { BUSINESS_ADMIN }, { APPRAISAL_SUPPORT } };
    }
	
	@Test(description = "SMAB-T362:SMAB-T363:SMAB-T430: Transaction record verification for the imported Building Permit in TXT Format", dataProvider = "loginUsers", priority = 0, alwaysRun = true, enabled = true)
	public void transactionRecordVerificationBuildingPermitTXT(String loginUser) throws Exception {
				
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		salesforceStandardFunctions.login(loginUser);
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the atherton building permit file");
		String athertonBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_ATHERTON;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", objUtil.getCurrentDate("MMMM YYYY"),athertonBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of Imported Records on File Import History table");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
				
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of Error and Imported Row Records on Review and Approve Data Page");
		boolean isErrorRowSectionDisplayed = objPage.verifyElementVisible(objEfileHomePage.errorRowSection);
		boolean isImportedRowSectionDisplayed = objPage.verifyElementVisible(objEfileHomePage.importedRowSection);
		if (isErrorRowSectionDisplayed){
			String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileHomePage.errorRowSection).split(":")[1].trim();
			softAssert.assertEquals(numberOfRecordsInErrorRowSection, "9", "SMAB-T362: Validation if correct number of records are displayed in Error Row Section after file import");
		}else{
			softAssert.assertTrue(false,"SMAB-T362: Validation for Error Row Section presence after clickig view link button");	
		}
		
		if (isImportedRowSectionDisplayed){
			String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileHomePage.importedRowSection).split(":")[1].trim();
			softAssert.assertEquals(numberOfRecordsInImportedRowSection, "1", "SMAB-T362: Validation if correct number of records are displayed in Imported Row Section after file import");
		}else{
			softAssert.assertTrue(false,"SMAB-T362: Validation for Imported Row Section presence after clickig view link button");	
		}		
		
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T363: Validation that error records can be dicarded from Review and Approve Data Page");
		objPage.Click(objEfileHomePage.rowSelectCheckBox);
		objPage.Click(objEfileHomePage.discardButton);
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileHomePage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "8", "SMAB-T363: Validation if correct number of records are displayed in Error Row Section after discarding a record");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T362: Validation that status is approved after approving all the records");
		objPage.Click(objEfileHomePage.selectAllCheckBox);
		objPage.Click(objEfileHomePage.discardButton);
		objPage.Click(objEfileHomePage.approveButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);
		
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T362: Validation if status of imported file is approved.");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		salesforceStandardFunctions.searchApps(EFILE_IMPORT_TRANSACTIONS);
		objPage.Click(objEfileImportTransactionsPage.importTransactionName);
		softAssert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Imported", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		softAssert.assertTrue(objPage.getElementText(objEfileImportTransactionsPage.efileImportLogLabel).contains("Atherton Building Permits"), "SMAB-T430: Validation that latest generated transaction log is for Atherton Building Permits");
		salesforceStandardFunctions.logout();
		softAssert.assertAll();
	}

	@Test(description = "SMAB-T361:SMAB-T358: Reverting the error records in building permit import", dataProvider = "loginUsers", priority = 0, alwaysRun = true, enabled = true)
	public void revertBuildingPermitImport(String loginUser) throws Exception {
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		salesforceStandardFunctions.login(loginUser);
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the atherton building permit file");
		String athertonBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_ATHERTON;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", objUtil.getCurrentDate("MMMM YYYY"),athertonBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Reverting  Imported Records on File Import History table");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "In Progress", "SMAB-T361: Validation if status of imported file is in progress.");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "1", "SMAB-T358: Validation if number of times try/retry count is correct on file import");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "10", "SMAB-T358: Validation if total number of records count is correct on file import");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "1", "SMAB-T358: Validation if total records in file count  is correct on file import");
		
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of file import status after revert");
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		objPage.Click(objEfileHomePage.revertButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.revertSuccessMessage, 20);
		
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T361: Validation if status of imported file is reverted.");
		salesforceStandardFunctions.logout();
		softAssert.assertAll();
	}
	
	@Test(description = "SMAB-T364: Retrying the error records in building permit import", dataProvider = "loginUsers", priority = 0, alwaysRun = true, enabled = true)
	public void retryBuildingPermitImport(String loginUser) throws Exception {
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		salesforceStandardFunctions.login(loginUser);
		
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the atherton building permit file");
		String athertonBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_ATHERTON;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", objUtil.getCurrentDate("MMMM YYYY"),athertonBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Retrying Imported Records after error correction on review and approve page");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "In Progress", "SMAB-T364: Validation if status of imported file is in progress.");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Correcting the error record to retry");
		objPage.Click(objEfileHomePage.rowSelectCheckBox);
		salesforceStandardFunctions.editGridCellValue("PERMITNO", "abc");
		objPage.Click(objEfileHomePage.retryButton);
		Thread.sleep(10000);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of error and inported records on Review and Approve Data Screen after retry");
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileHomePage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "8", "SMAB-T364: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
	
		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileHomePage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "2", "SMAB-T364: Validation if correct number of records are displayed in Imported Row Section after correcting and retrying the error record");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of error and inported records on Import History table after retry");
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "2", "SMAB-T364: Validation if number of times try/retry count is increased by 1 after retrying the error records");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "10", "SMAB-T364: Validation if total number of records remain same on the table");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "2", "SMAB-T364: Validation if total records in file count is increased by 1 after retrying the error records");
		salesforceStandardFunctions.logout();
		softAssert.assertAll();
	}
	
	@Test(description = "SMAB-T357:SMAB-T430 Transaction record verification for the imported Building Permit in XLS Format", dataProvider = "loginUsers", priority = 1, alwaysRun = true, enabled = true)
	public void transactionRecordVerificationBuildingPermitXLS(String loginUser) throws Exception {
			
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		salesforceStandardFunctions.login(loginUser);
		salesforceStandardFunctions.searchApps(EFILE_INTAKE);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the San Mateo permit file");
		String sanMateoBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_SAN_MATEO;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "San Mateo Building permits", objUtil.getCurrentDate("MMMM YYYY"),sanMateoBuildingPermitsFile);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported records on Import History table");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "In Progress", "SMAB-T357: Validation if status of imported file is in progress.");
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "1", "SMAB-T357: Validation if number of times try/retry count is correct on file import");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "10", "SMAB-T357: Validation if total number of records count is correct on file import");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "1", "SMAB-T357: Validation if total records in file count  is correct on file import");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported records on Review and Approve Data Screen");
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileHomePage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "9", "SMAB-T357: Validation if correct number of records are displayed in Error Row Section");
	
		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileHomePage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "1", "SMAB-T357: Validation if correct number of records are displayed in Imported Row Section");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		salesforceStandardFunctions.searchApps(EFILE_IMPORT_TRANSACTIONS);
		objPage.Click(objEfileImportTransactionsPage.importTransactionName);
		softAssert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Imported", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		softAssert.assertTrue(objPage.getElementText(objEfileImportTransactionsPage.efileImportLogLabel).contains("San Mateo Building permits"), "SMAB-T430: Validation that latest generated transaction log is for San Mateo Building permits");
		salesforceStandardFunctions.logout();
		softAssert.assertAll();
	}

	@Test(description = "Creating manual entry for building permit", groups = {"smoke"}, priority = 2, enabled = false)
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
