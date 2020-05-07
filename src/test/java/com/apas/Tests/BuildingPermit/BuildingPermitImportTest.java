package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BuildingPermitImportTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildPermit;
	EFileImportPage objEfileHomePage;
	Util objUtil  = new Util();
	SoftAssertion softAssert  = new SoftAssertion();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}
		
	@AfterMethod(alwaysRun=true)
	public void afterMethod() throws IOException, InterruptedException{
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
	 Below test case is used to validate below functionalities
	 1. Atherton building permit import functionality with business admin and appraisal support user roles
	 2. Validation of error and imported records on review and approve screen
	 3. Status validation of imported file on efile import transaction log screen
	 **/
	@Test(description = "SMAB-T362,SMAB-T363: Verify Discard and Approve functionality for Building Permit Import in TXT format", dataProvider = "loginUsers", groups = {"smoke","regression","BuildingPermit"}, alwaysRun = true)
	public void verifyApproveDiscardBuildingPermitImport(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);


		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import intake module
//		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
//
//		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
//		String athertonBuildingPermitsFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "Import_TestData_InvalidPrefix_IncorrectWorkDescription_AT.txt";
//		objEfileHomePage.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", objUtil.getCurrentDate("MMMM YYYY"),athertonBuildingPermitsFile);
//
//		//Step4: Waiting for Status of the imported file to be converted to "Imported"
//		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
//		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
//		objPage.Click(objEfileHomePage.viewLink);
//		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);

		//Step5: Comparing the data from the error row table with the expected data
		ExtentTestManager.getTest().log(LogStatus.INFO, "Data Validation of Error Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualErrorRowTable  = objApasGenericFunctions.getGridDataInHashMap(1);
		String expectedErrorRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "Import_TestData_InvalidPrefix_IncorrectWorkDescription_AT_ExpectedErrorRecords.csv";
		HashMap<String, ArrayList<String>> expectedErrorRowTable = FileUtils.getCSVData(expectedErrorRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualErrorRowTable,expectedErrorRowTable),"","Data Comparison validation for Error Row Table");

		//Step6: Comparing the data from the Imported Row Table with the expected data
		ExtentTestManager.getTest().log(LogStatus.INFO, "Data Validation of Imported Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualImportedRowTable  = objApasGenericFunctions.getGridDataInHashMap(2);
		String expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "Import_TestData_InvalidPrefix_IncorrectWorkDescription_AT_ExpectedImportedRecords.csv";
		HashMap<String, ArrayList<String>> expectedImportedRowTable = FileUtils.getCSVData(expectedImportedRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualImportedRowTable,expectedImportedRowTable),"","Data Comparison validation for Imported Row Table");

		//Step7: Validating that correct number of records are moved to error and imported row sections after file import
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of Error and Imported Row Records on Review and Approve Data Page");
		//Validation of number of records in error row section. Expected is 9 as 9 records are passed with wrong data in the input file
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileHomePage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "9", "SMAB-T362: Validation if correct number of records are displayed in Error Row Section after file import");
		//Validation of number of records in imported row section. Expected is 1 as 1 record is passed with correct data in the input file
		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileHomePage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "1", "SMAB-T362: Validation if correct number of records are displayed in Imported Row Section after file import");

		//Step8: Validation for Records discard functionality from Review and Approve Page
		ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T363: Validation that error records can be discarded from Review and Approve Data Page");
		objPage.Click(objEfileHomePage.rowSelectCheckBox);
		objPage.Click(objEfileHomePage.discardButton);
		objPage.Click(objEfileHomePage.continueButton);
		numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileHomePage.errorRowSection).split(":")[1].trim();
		//validating the number of records in the error row section after discarding a record as records should be moved the imported row section after discard
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "8", "SMAB-T363: Validation if correct number of records are displayed in Error Row Section after discarding a record");
		
		//Step9: Validating the approve functionality after all the records are cleared from error section and approved
		ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T362: Validation that status is approved after approving all the records");
		objPage.Click(objEfileHomePage.selectAllCheckBox);
		objPage.Click(objEfileHomePage.discardButton);
		objPage.Click(objEfileHomePage.continueButton);
		//***********Commenting the below code until API integration is done as Approved records can't be imported again********************//

//		objPage.Click(objEfileHomePage.approveButton);
//		objPage.waitForElementToBeVisible(objEfileHomePage.efileRecordsApproveSuccessMessage, 20);
//
//		//Step8: Searching the efile intake module to validate the status of the imported file after approve
//		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
//		objEfileHomePage.selectFileAndSource("Building Permit", "Atherton Building Permits");
//		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Approved", "SMAB-T362: Validation if status of imported file is approved.");
	}

    /**
	 Below test case is used to validate the revert functionality on the file having the error records
	 **/
	@Test(description = "SMAB-T361,SMAB-T358: Reverting the error records in building permit import", dataProvider = "loginUsers", groups = {"smoke","regression","BuildingPermit"}, alwaysRun = true)
	public void verifyRevertBuildingPermitImport(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the atherton building permit file");
		String athertonBuildingPermitsFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON;
		objEfileHomePage.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", objUtil.getCurrentDate("MMMM YYYY"),athertonBuildingPermitsFile);
		
		//Step4: Validating that status of the imported file is in progress
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "In Progress", "SMAB-T361: Validation if status of imported file is in progress.");
		
		//Step5: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		
		//Validation of number of times tried retried column for the imported file. Expected is 1 as it has not be retried/reverted yet
		ExtentTestManager.getTest().log(LogStatus.INFO, "Reverting  Imported Records on File Import History table");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "1", "SMAB-T358: Validation if number of times try/retry count is correct on file import");
		//Validation of "Total Records in File" column for the imported file. Expected is 10 as 10 records are sent in the file
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "10", "SMAB-T358: Validation if total number of records count is correct on file import");
		//Validation of "Total Records Imported" column for the imported file. Expected is 1 as 1 records has the correct record out of 10 records sent in the file
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "1", "SMAB-T358: Validation if total records in file count  is correct on file import");
		
		//Step6: Reverting the Atherton Building Permit file on Review and Approve Screen
		ExtentTestManager.getTest().log(LogStatus.INFO, "Reverting the imported file");
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		objPage.Click(objEfileHomePage.revertButton);
		objPage.Click(objEfileHomePage.continueButton);
		objPage.waitForElementToBeVisible(objEfileHomePage.revertSuccessMessage, 20);
		
		//Step7: Validation of the file status after reverting the imported file
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of file import status after revert");
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		//Status of the imported file should be changed to Reverted as the whole file is reverted for reimport
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "Reverted", "SMAB-T361: Validation if status of imported file is reverted.");
	}
	
    /**
	 Below test case is used to validate the retry functionality after correction on the records in error
	 **/
	@Test(description = "SMAB-T364: Retrying the error records in building permit import", dataProvider = "loginUsers", groups = {"smoke","regression","BuildingPermit"}, alwaysRun = true)
	public void verifyRetryBuildingPermitImport(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		
		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading the atherton building permit file");
		String athertonBuildingPermitsFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON;
		objEfileHomePage.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", objUtil.getCurrentDate("MMMM YYYY"),athertonBuildingPermitsFile);
		
		//Step4: Validating that status of the imported file is in progress
		ExtentTestManager.getTest().log(LogStatus.INFO, "Retrying Imported Records after error correction on review and approve page");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.statusImportedFile), "In Progress", "SMAB-T364: Validation if status of imported file is in progress.");
		//Step5: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileHomePage.viewLink);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		//Step6: Correcting one of the error record to validate the retry functionality
		ExtentTestManager.getTest().log(LogStatus.INFO, "Correcting the error record to retry");
		objPage.Click(objEfileHomePage.rowSelectCheckBox);
		objApasGenericFunctions.editGridCellValue("PERMITNO", "abc");
		objPage.Click(objEfileHomePage.retryButton);
		//Waiting for 15 seconds as the grid is taking some time to go after clicking Retry button. This needs to be removed later on based on optimum solution
		Thread.sleep(15000);
		objPage.waitForElementToBeVisible(objEfileHomePage.errorRowSection,30);
		
		//Step7: Validating that corrected records are moved to imported row section after retry.		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of error and inported records on Review and Approve Data Screen after retry");
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileHomePage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "8", "SMAB-T364: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");
	
		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileHomePage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "2", "SMAB-T364: Validation if correct number of records are displayed in Imported Row Section after correcting and retrying the error record");
		
		//Step8: Valiation of import history columns as the value should be updated based on the records retried from error section
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of error and inported records on Import History table after retry");
		//Opening the Efile intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileHomePage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.numberOfTimesTriedRetried), "2", "SMAB-T364: Validation if number of times try/retry count is increased by 1 after retrying the error records");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsInFile), "10", "SMAB-T364: Validation if total number of records remain same on the table");
		softAssert.assertEquals(objPage.getElementText(objEfileHomePage.totalRecordsImportedFile), "2", "SMAB-T364: Validation if total records in file count is increased by 1 after retrying the error records");
	}

}