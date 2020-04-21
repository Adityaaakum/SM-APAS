package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class BuildingPermitTransactionsLogTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildPermit;
	EFileImportPage objEfileHomePage;
	Util objUtil  = new Util();
	SoftAssertion softAssert = new SoftAssertion();

	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}

	@AfterMethod
	public void afterMethod() throws IOException{
		objApasGenericFunctions.logout();
		softAssert.assertAll();
	}


	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business admin and appraisal support in an array
	 **/
    @DataProvider(name = "loginUsers")
    public Object[][] dataProviderLoginUserMethod() {
        return new Object[][] {{ users.BUSINESS_ADMIN } };
    }

	/**
	 Below test case is used to validate status of the imported San Mateo Building Permit file which in XLS format
	 **/
	@Test(description = "SMAB-T357,SMAB-T430: Transaction record verification for the imported Building Permit in XLS Format", dataProvider = "loginUsers", groups = {"smoke","regression"}, alwaysRun = true)
	public void transactionRecordVerificationBuildingPermitXLS(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the San Mateo Building Permit file having error and success records through Efile Intake Import
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the San Mateo permit file");
		String sanMateoBuildingPermitsFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO;
		objEfileHomePage.uploadFileOnEfileIntake("Building Permit", "San Mateo Building permits", objUtil.getCurrentDate("MMMM YYYY"),sanMateoBuildingPermitsFile);

		//Step4: Validating that status of the imported file is in progress
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported records on Import History table");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "In Progress", "SMAB-T357: Validation if status of imported file is in progress.");

		//Step5: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);

		//Step6: Validation of columns of Import history table if those are populated correctly from the imported san mateo building permit file. There are 9 records in error and 1 record correct in the file
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "1", "SMAB-T357: Validation if number of times try/retry count is correct on file import");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "10", "SMAB-T357: Validation if total number of records count is correct on file import");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "1", "SMAB-T357: Validation if total records in file count  is correct on file import");

		//Step7: Validation of error and imported records on review and approve screen if those are populated correctly from the imported san mateo building permit file. There are 9 records in error and 1 record correct in the file
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported records on Review and Approve Data Screen");
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);

		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileHomePage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "9", "SMAB-T357: Validation if correct number of records are displayed in Error Row Section");

		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileHomePage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "1", "SMAB-T357: Validation if correct number of records are displayed in Imported Row Section");

		//Step9: Opening the Efile import transaction module
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of imported file on efile import transaction screen");
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);
		objPage.Click(objEfileImportTransactionsPage.importTransactionName);

		//By default "Details" tab should be opened showing the transaction details of the imported file
		softAssert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		//Status of the file should be displayed as imported as the file is imported
		softAssert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Imported", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		softAssert.assertTrue(objPage.getElementText(objEfileImportTransactionsPage.efileImportLogLabel).contains("San Mateo Building permits"), "SMAB-T430: Validation that latest generated transaction log is for San Mateo Building permits");
	}

}