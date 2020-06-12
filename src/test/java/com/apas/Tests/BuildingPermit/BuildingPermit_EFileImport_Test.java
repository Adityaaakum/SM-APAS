package com.apas.Tests.BuildingPermit;

import java.util.List;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.FileUtils;
import com.apas.Utils.SalesforceAPI;
import com.apas.generic.DataProviders;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermit_EFileImport_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildingPermitPage;
	SoftAssertion softAssert  = new SoftAssertion();
	Util objUtil  = new Util();
	EFileImportPage objEfileImportPage;
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	String athertonBuildingPermitFile;
	String sanMateoBuildingPermitFile;
	String unincorporatedBuildingPermitFile;

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		
		if(driver==null) {
			
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objEfileImportPage = new EFileImportPage(driver);
		athertonBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "Import_TestData_ValidAndInvalidScenarios_AT.txt";
		sanMateoBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "SanMateoBuildingPermitsWithValidAndInvalidData.xlsx";
		unincorporatedBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_UNINCORPORATED + "WorkDescriptionWithKeywords_UN.txt";
	}

	/**
	 Below test case is used to validate below functionalities
	 1. Atherton building permit import functionality with business admin and appraisal support user roles
	 2. Validation of error and imported records on review and approve screen
	 3. Status validation of imported file on efile import transaction log screen
	 **/
	@Test(description = "SMAB-T362,SMAB-T363,SMAB-T315,SMAB-T417,SMAB-T360,SMAB-T458,SMAB-T459,SMAB-T619,SMAB-T621,SMAB-T624,SMAB-T625,SMAB-T456,SMAB-T457,SMAB-T549,SMAB-T435: Transaction record verification for the imported Building Permit in TXT Format", dataProvider = "loginUsers",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_DiscardAndApprove(String loginUser) throws Exception {

		String period = objUtil.getCurrentDate("MMMM YYYY");

		//Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and Import_Period__C='" + period + "' and File_Source__C like '%Atherton%' and Status__c = 'Approved' ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", period ,athertonBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step7: Validating that correct number of records are moved to error and imported row sections after file import
		ReportLogger.INFO("Validation of Error and Imported Row Records on Review and Approve Data Page");
		//Validation of number of records in error row section. Expected is 13 as 13 records are passed with wrong data in the input file
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileImportPage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "14", "SMAB-T362: Validation if correct number of records are displayed in Error Row Section after file import");
		//Validation of number of records in imported row section. Expected is 3 as 3 records are passed with correct data in the input file
		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileImportPage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "8", "SMAB-T362: Validation if correct number of records are displayed in Imported Row Section after file import");

		//Step8: Validation for Records discard functionality from Review and Approve Page
		ReportLogger.INFO("SMAB-T363: Validation that error records can be discarded from Review and Approve Data Page");
		objPage.Click(objEfileImportPage.rowSelectCheckBox);
		objPage.Click(objEfileImportPage.discardButton);
		objPage.Click(objEfileImportPage.continueButton);
		numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileImportPage.errorRowSection).split(":")[1].trim();
		//validating the number of records in the error row section after discarding a record as records should be moved the imported row section after discard
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "13", "SMAB-T363: Validation if correct number of records are displayed in Error Row Section after discarding a record");
		numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileImportPage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "8", "SMAB-T362: Validation if correct number of records are displayed in Imported Row Section after discarding a record");

		//Step9: Validating the approve functionality after all the records are cleared from error section and approved
		ReportLogger.INFO("SMAB-T362: Validation that status is approved after approving all the records");
		objPage.Click(objEfileImportPage.selectAllCheckBox);
		objPage.Click(objEfileImportPage.discardButton);
		objPage.Click(objEfileImportPage.continueButton);
		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);

		//Step8: Searching the efile intake module to validate the status of the imported file after approve
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileImportPage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.statusImportedFile), "Approved", "SMAB-T362: Validation if status of imported file is approved.");

		//Step9: Validating the warning message for the approved buildling permit records and user should not be allowed to re-import the approved building permit records
		objPage.scrollToTop();
		objPage.Click(objEfileImportPage.nextButton);
		objPage.Click(objEfileImportPage.periodDropdown);
		objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")));
		softAssert.assertContains(objPage.getElementText(objEfileImportPage.warning),"This file has been already approved by","SMAB-T435: Warning message validation once user tries to re-import approved building permits");
		softAssert.assertTrue(!objPage.verifyElementVisible(objEfileImportPage.confirmButton),"SMAB-T435: Validation that user should not be able to re-import the approved file by clicking on Confirm button i.e. confirm button should not be visible");
		softAssert.assertTrue(!objPage.verifyElementVisible(objEfileImportPage.cancelButton),"SMAB-T435: Validation that cancel button should not be displayed when trying to re-import the approved file");
		objPage.Click(objEfileImportPage.closeButton);

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate the building permit data imported through Efile Intake
	 **/
	@Test(description = "SMAB-T354,SMAB-T356,SMAB-T383,SMAB-T440: Validate the data on records imported through E-file intake module", dataProvider = "loginUsers",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, enabled = true)
	public void verify_BuildingPermit_ThroughEFileImportTool(String loginUser) throws Exception {

		String period = objUtil.getCurrentDate("MMMM YYYY");

		//Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and Import_Period__C='" + period + "' and File_Source__C like '%Atherton%' and Status__c = 'Approved' ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");

		//Step1: Creating temporary file with random building permit number
		String buildingPermitNumber = "T" + objUtil.getCurrentDate("dd-hhmmss");
		String buildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "SingleValidRecord_AT.txt";
		String temporaryFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + "SingleValidRecord_AT.txt";
		FileUtils.replaceString(buildingPermitFile,"<PERMITNO>",buildingPermitNumber,temporaryFile);

		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step3: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step4: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", period ,temporaryFile);

		//Step5: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);

		//step6: Validating that atherton file is successfully imported
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.totalRecordsInFile), "1", "SMAB-T440: Validation of total number of records with the records in file");
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.totalRecordsImportedFile), "1", "SMAB-T440: Validation of total records in file count is equal to the valid records in file");

		//Step7: Approving the imported file
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);
		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);

		//Step8: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step9: Opening the Building Permit with the Building Permit Number imported through Efile import
		objApasGenericFunctions.displayRecords("All Imported E-File Building Permits");
		objApasGenericFunctions.globalSearchRecords(buildingPermitNumber);

		ReportLogger.INFO("Validating the field values on building permit created through E-File Intake for Building Permit Number : " + buildingPermitNumber);

		//Step10: Validation for the fields in the section Building Permit Information
		//Processing and Calculating Processing Status are calculated based on "Processing Status Information" section from "County Strat Code Information"
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Building Permit Number", "Building Permit Information"), buildingPermitNumber, "SMAB-T356: 'Building Permit Number' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("APN", "Building Permit Information"), "060-241-050", "SMAB-T356: 'Parcel' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Estimated Project Value", "Building Permit Information"), "$1,000", "SMAB-T356: 'Estimated Project Value' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("City APN", "Building Permit Information"), "060241050", "SMAB-T356: 'Parcel' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Issue Date", "Building Permit Information"), "4/3/2018", "SMAB-T356: 'Issue Date' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Completion Date", "Building Permit Information"), "10/1/2019", "SMAB-T356: 'Completion Date' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Processing Status","Processing Status"), "Process", "SMAB-T356,SMAB-T354: 'Processing Status' Field Validation in 'Processing Status' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Calculated Processing Status","Processing Status"), "Process", "SMAB-T356,SMAB-T354,SMAB-T440: 'Calculated Processing Status' Field Validation in 'Processing Status' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Building Permit Fee", "Building Permit Information"), "$1,983.21", "SMAB-T356: 'Building Permit Fee' Field Validation in 'Building Permit Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Description", "Building Permit Information"), "Valid Record REMOVAL OF TREE", "SMAB-T356: 'Work Description' Field Validation in 'Building Permit Information' section");

		//Validation for the fields in the section 'City and County information'
		//Strat Code reference Number can be fetched from the County Strat Code Screen of the code choosen while creating the building permit
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("County Strat Code Description", "City and County Information"), "ADDITION", "SMAB-T356: 'City Strat Code Lookup' Field Validation in 'City and County Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("County Strat Code Description", "City and County Information"), "ADDITION", "SMAB-T356: 'County Strat Code Description' Field Validation in 'City and County Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("City Strat Code", "City and County Information"), "ADDITION", "SMAB-T356: 'County Strat Code Description' Field Validation in 'City and County Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Strat Code Reference Number","City and County Information"), "20", "SMAB-T356: 'Record Type' Field Validation in 'City and County Information' section");

		//Validation for the fields auto populated in the section 'Situs Information'
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit Situs Number","Situs Information"), "94", "SMAB-T356: 'Permit Situs Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit Situs Unit Number","Situs Information"), "", "SMAB-T356: 'Permit Situs Unit Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit Situs Street Name","Situs Information"), "VERNON", "SMAB-356: 'Permit Situs Street Name' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit Situs Direction","Situs Information"), "", "SMAB-T356: 'Permit Situs Direction' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit Situs Type","Situs Information"), "MOUNT", "SMAB-T356: 'Permit Situs Type' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit City Code","Situs Information"), "AT", "SMAB-T356: 'Permit City Code' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs","Situs Information"), "55 MOUNT VERNON LN, AT", "SMAB-T356: 'Situs' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Number","Situs Information"), "55", "SMAB-T356: 'Situs Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Unit Number","Situs Information"), "", "SMAB-T356: 'Situs Unit Number' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Street Name","Situs Information"), "MOUNT VERNON", "SMAB-T356: 'Situs Street Name' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Direction"), "", "SMAB-T356: 'Situs Direction' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs Type","Situs Information"), "LN", "SMAB-T356: 'Situs Type' Field Validation in 'Situs Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Situs City Code","Situs Information"), "AT", "SMAB-T356: 'Situs City Code' Field Validation in 'Situs Information' section");

		//Validation for the fields auto populated in the section 'Owner Information'
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Owner Name","Owner Information"), "FIRST4 LAST4", "SMAB-T356: 'Owner Name' Field Validation in 'Owner Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Owner Phone Number","Owner Information"), "", "SMAB-T356: 'Owner Phone Number' Field Validation in 'Owner Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Owner Address Line 1","Owner Information"), "55 MOUNT VERNON LN", "SMAB-T356: 'Owner Address Line 1' Field Validation in 'Owner Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Owner Address Line 2","Owner Information"), "", "SMAB-T356: 'Owner Address Line 2' Field Validation in 'Owner Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Owner Address Line 3","Owner Information"), "", "SMAB-T356: 'Owner Address Line 3' Field Validation in 'Owner Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Owner State","Owner Information"), "CA", "SMAB-T356: 'Owner State' Field Validation in 'Owner Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Owner City","Owner Information"), "ATHERTON", "SMAB-T356: 'Owner City' Field Validation in 'Owner Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Owner Zip Code","Owner Information"), "94022", "SMAB-T356: 'Owner Zip Code' Field Validation in 'Owner Information' section");

		//Validation for the fields auto populated in the section 'Other Information'
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Fiscal Year","Other Information"), "", "SMAB-T356: 'Fiscal Year' Field Validation in 'Other Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Square Footage","Other Information"), "100", "SMAB-T356: 'Square Footage' Field Validation in 'Other Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Application Name","Other Information"), "ABC APP", "SMAB-T356: 'Application Name' Field Validation in 'Other Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Contractor Phone","Other Information"), "", "SMAB-T356: 'Contractor Phone' Field Validation in 'Other Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Contractor Name","Other Information"), "TEST 4", "SMAB-T356: 'Contractor Name' Field Validation in 'Other Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Reissue","Other Information"), "No", "SMAB-T356: 'Reissue' Field Validation in 'Other Information' section");

		//Validation for the fields auto populated in the section System Information
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Record Type","System Information"), "E-File Building Permit", "SMAB-T356: 'Record Type' Field Validation in 'System Information' section");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Import Status","System Information"), "Import Approved", "SMAB-T356: 'Import Status' Field Validation in 'System Information' section");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate the revert functionality on the file having the error records
	 **/
	@Test(description = "SMAB-T361,SMAB-T358,SMAB-T970: Reverting the error records in building permit import", dataProvider = "loginUsers",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"},alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_Revert(String loginUser) throws Exception {

		String period = objUtil.getCurrentDate("MMMM YYYY");

		//Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and Import_Period__C='" + period + "' and File_Source__C like '%Atherton%' and Status__c like '%Approved%' ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", period,athertonBuildingPermitFile);

		//Step4: Validating that status of the imported file is in progress
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.statusImportedFile), "In Progress", "SMAB-T361: Validation if status of imported file is in progress.");

		//Step5: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);

		//Validation of number of times tried retried column for the imported file. Expected is 1 as it has not be retried/reverted yet
		ReportLogger.INFO("Reverting Imported Records on File Import History table");
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.numberOfTimesTriedRetried), "1", "SMAB-T358: Validation if number of times try/retry count is correct on file import");
		//Validation of "Total Records in File" column for the imported file. Expected is 10 as 10 records are sent in the file
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.totalRecordsInFile), "23", "SMAB-T358: Validation if total number of records count is correct on file import");
		//Validation of "Total Records Imported" column for the imported file. Expected is 1 as 1 records has the correct record out of 10 records sent in the file
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.totalRecordsImportedFile), "9", "SMAB-T358: Validation if total records in file count  is correct on file import");

		//Step6: Reverting the Atherton Building Permit file on Review and Approve Screen
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);
		objPage.Click(objEfileImportPage.revertButton);
		objPage.Click(objEfileImportPage.continueButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.revertSuccessMessage, 20);

		//Step7: Validation of the file status after reverting the imported file
		ReportLogger.INFO("Validation of file import status after revert");
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileImportPage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		//Status of the imported file should be changed to Reverted as the whole file is reverted for reimport
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.statusImportedFile), "Reverted", "SMAB-T361,SMAB-T970: Validation if status of imported file is reverted.");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate the retry functionality after correction on the records in error
	 **/
	@Test(description = "SMAB-T364, SMAB-T435: Retrying the error records in building permit import", dataProvider = "loginUsers",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_RetryErrorRecords(String loginUser) throws Exception {

		String period = objUtil.getCurrentDate("MMMM YYYY");

		//Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and Import_Period__C='" + period + "' and File_Source__C like '%Atherton%' and Status__c like '%Approved%' ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", period,athertonBuildingPermitFile);

		//Step4: Validating that status of the imported file is in progress
		ReportLogger.INFO("Retrying Imported Records after error correction on review and approve page");
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.statusImportedFile), "In Progress", "SMAB-T364: Validation if status of imported file is in progress.");
		//Step5: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step6: Correcting one of the error record to validate the retry functionality
		ExtentTestManager.getTest().log(LogStatus.INFO, "Correcting the error record to retry");
		objApasGenericFunctions.editGridCellValue("CITYSTRAT", "ADDITION");
		objEfileImportPage.collapseSection(objEfileImportPage.errorRowSectionExpandButton);
		objPage.Click(objEfileImportPage.retryButton);
		//Waiting for 15 seconds as the grid is taking some time to go after clicking Retry button. This needs to be removed later on based on optimum solution
		Thread.sleep(15000);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step7: Validating that corrected records are moved to imported row section after retry.
		ReportLogger.INFO("Validation of error and imported records on Review and Approve Data Screen after retry");
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileImportPage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "13", "SMAB-T364: Validation if correct number of records are displayed in Error Row Section after correcting and retrying the error record");

		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileImportPage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "9", "SMAB-T364: Validation if correct number of records are displayed in Imported Row Section after correcting and retrying the error record");

		//Step8: Valiation of import history columns as the value should be updated based on the records retried from error section
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of error and inported records on Import History table after retry");
		//Opening the Efile intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileImportPage.selectFileAndSource("Building Permit", "Atherton Building Permits");
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.numberOfTimesTriedRetried), "2", "SMAB-T364: Validation if number of times try/retry count is increased by 1 after retrying the error records");
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.totalRecordsInFile), "23", "SMAB-T364: Validation if total number of records remain same on the table");
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.totalRecordsImportedFile), "10", "SMAB-T364: Validation if total records in file count is increased by 1 after retrying the error records");

		//Validation of error message when trying to re-import the already imported file
		objPage.Click(objEfileImportPage.nextButton);
		objPage.Click(objEfileImportPage.periodDropdown);
		objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")));
		softAssert.assertContains(objPage.getElementText(objEfileImportPage.warning),"This file has been already imported by","SMAB-T435: Warning message validation once user tries to re-import imported building permits");
		softAssert.assertTrue(objPage.verifyElementVisible(objEfileImportPage.confirmButton),"SMAB-T435: Validation that user should be able to re-import the imported file by clicking on Confirm button i.e. confirm button should be visible");
		softAssert.assertTrue(objPage.verifyElementVisible(objEfileImportPage.cancelButton),"SMAB-T435: Validation that cancel button should be displayed when trying to re-import the already imported file");
		objPage.Click(objEfileImportPage.closeButton);

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate that building permit should only be visible after approval
	 **/
	@Test(description = "SMAB-T661: Validation for building permit record creation after import is approved", dataProvider = "loginUsers",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, enabled = true)
	public void verify_BuildingPermit_RecordCreationAfterImportApproved(String loginUser) throws Exception {

		String period = objUtil.getCurrentDate("MMMM YYYY");

		//Step1: Creating temporary file with random building permit number
		String buildingPermitNumber = "T" + objUtil.getCurrentDate("dd-hhmmss");
		String buildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "SingleValidRecord_AT.txt";
		String temporaryFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + "SingleValidRecord_AT.txt";
		FileUtils.replaceString(buildingPermitFile,"<PERMITNO>",buildingPermitNumber,temporaryFile);

		//Step2: Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and Import_Period__C='" + period + "' and File_Source__C like '%Atherton%' and Status__c = 'Approved' ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");

		//Step3: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step4: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step5: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", period ,temporaryFile);

		//Step6: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);

		//step7: Validating that atherton file is successfully imported
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.totalRecordsInFile), "1", "SMAB-T661: Validation of total number of records with the records in file");
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.totalRecordsImportedFile), "1", "SMAB-T661: Validation of total records in file count is equal to the valid records in file");

		//Step8: Validating the building permit records should not be visible in the system as it has not been approved yet
		String xpathBuildingPermit = "//*[@role='option']//*[@title='" + buildingPermitNumber + "']";
		objPage.enter(objBuildingPermitPage.globalSearchListEditBox,buildingPermitNumber);
		List<WebElement> webElementBuildingPermitBeforeApproved = driver.findElements(By.xpath(xpathBuildingPermit));
		softAssert.assertTrue(webElementBuildingPermitBeforeApproved.size() == 0,"SMAB-T661: Validating that building permit " + buildingPermitNumber + " should not be visible in the system as its not approved yet");

		//Step9: Approving the imported file
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);
		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);

		//Step10: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step11: Validating the building permit records should be visible in the system as it has been approved
		objPage.enter(objBuildingPermitPage.globalSearchListEditBox,buildingPermitNumber);
		objPage.waitUntilElementIsPresent(xpathBuildingPermit,10);
		List<WebElement> webElementBuildingPermitAfterApproved = driver.findElements(By.xpath(xpathBuildingPermit));
		softAssert.assertTrue(webElementBuildingPermitAfterApproved.size() == 1,"SMAB-T661: Validating that building permit " + buildingPermitNumber + " should be visible in the system only when import is approved");
		objApasGenericFunctions.globalSearchRecords(buildingPermitNumber);

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}
}