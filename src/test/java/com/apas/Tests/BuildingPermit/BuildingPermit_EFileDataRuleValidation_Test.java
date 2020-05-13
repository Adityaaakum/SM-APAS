package com.apas.Tests.BuildingPermit;

import java.util.ArrayList;
import java.util.HashMap;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.FileUtils;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.apas.generic.DataProviders;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermit_EFileDataRuleValidation_Test extends TestBase{
	
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
	 Below test case is used to validate the building permit data imported through Efile Intake
	 **/
	@Test(description = "SMAB-T548: Validate removal of keywords from Work Description from UN File Type", dataProvider = "loginUsers",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, enabled = true)
	public void verify_BuildingPermit_RemoveKeywordFromWorkDescription_UnincorporatedFileType(String loginUser) throws Exception {

		String period = objUtil.getCurrentDate("MMMM YYYY");

		//Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and Import_Period__C='" + period + "' and File_Source__C like '%Unincorporated%' and Status__c = 'Approved' ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Unincorporated Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntake("Building Permit", "Unincorporated Building permits", period ,unincorporatedBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Expanding the import row section if its not already expanded
		ReportLogger.INFO("Expanding Import Row Section");
		objEfileImportPage.expandSection(objEfileImportPage.importedRowSectionExpandButton);

		//Step6: Comparing the data from the Imported Row Table with the expected data
		ExtentTestManager.getTest().log(LogStatus.INFO, "Data Validation of Imported Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualImportedRowTable  = objApasGenericFunctions.getGridDataInHashMap(1);
		String expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_UNINCORPORATED + "WorkDescriptionWithKeywords_UN_ExpectedImportedRecords.csv";
		HashMap<String, ArrayList<String>> expectedImportedRowTable = FileUtils.getCSVData(expectedImportedRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualImportedRowTable,expectedImportedRowTable),"","Data Comparison validation for Imported Row Table");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate the building permit warning messages for retired parcel and situs information mismatch imported through Efile Intake
	 **/
	@Test(description = "SMAB-T453,SMAB-T455: Validate Retired parcel and situs information mismatch records imported through E-file intake module", dataProvider = "loginUsers",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, priority = 2,enabled = true)
	public void verify_BuildingPermit_ThroughEFileImportTool_WithRetiredParcelAndSitusMismatch(String loginUser) throws Exception {

		/*
		 *The Building Permit with retired parcel and situs information mismatch data is being fetched from the Atherton file used in verify_BuildingPermit_DiscardAndApprove method
		 *If the data is changed in E-file import file, there will be an impact on this scenario as well and data for below fields need to be updated
		 */
		String buildingPermitNumber = "T17-002941";

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Opening the Building Permit with the Building Permit Number imported through Efile import
		objApasGenericFunctions.globalSearchRecords(buildingPermitNumber);

		//Step4: Warning message validation for building permit(Imported through E-File Intake module) with retired permit and situs information mismatch
		String expectedMessage = "The parcel is retired. Please review and confirm the APN on the Building Permit.\nSitus information from the Building Permit does not match in the system.";
		softAssert.assertEquals(objPage.getElementText(objBuildingPermitPage.warningMessageWithPriorityFlag).trim(), expectedMessage, "SMAB-T453,SMAB-T455: Warning message validation for building permit(Imported through E-File Intake module) with retired permit and situs information mismatch");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}
	/**
	 Below test case is used to validate the warning message for missing/wrong parcel
	 **/
	@Test(description = "SMAB-T451,SMAB-T374: Validate Warning message for Missing/Wrong Parcel and Situs Type auto population for the records imported through E-file intake module", dataProvider = "loginUsers",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"},priority = 2,enabled = true)
	public void verify_BuildingPermit_ThroughEFileImportTool_WrongParcelAndSitusTypePopulation(String loginUser) throws Exception {

		/*
		 *The Building Permit with missing APN data is being fetched from the Atherton file used in verify_BuildingPermit_DiscardAndApprove method
		 *If the data is changed in E-file import file, there will be an impact on this scenario as well and data for below fields need to be updated
		 */
		String missingBuildingPermitNumber = "T17-002101";

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		ReportLogger.INFO("Validating the warning message for missing APN for Building Permit number " + missingBuildingPermitNumber);

		//Step2: Opening the building permit module to validate warning message for missing APN
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);

		//Step3: Opening the Building Permit with the Building Permit Number imported through Efile import
		objApasGenericFunctions.globalSearchRecords(missingBuildingPermitNumber);

		//Step4: Warning message validation for building permit(Imported through E-File Intake module) with retired permit and situs information mismatch
		String expectedMessage = "Parcel and Situs information from the Building Permit does not match in the system.";
		softAssert.assertEquals(objPage.getElementText(objBuildingPermitPage.warningMessageWithPriorityFlag).trim(), expectedMessage, "SMAB-T451: Warning message validation for building permit(Imported through E-File Intake module) with missing APN");

		//Step5: Validation of Situs Type population from Situs Street Name with special keywords
		ReportLogger.INFO("Validation of Situs Type population from Situs Street Name with special keywords");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit Situs Street Name","Situs Information"), "MOUNT", "SMAB-374: 'Permit Situs Street Name' Field Validation in 'Situs Information' section for Situs street name having special keyword");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Permit Situs Type","Situs Information"), "BLVD", "SMAB-T374: 'Permit Situs Type' Field Validation in 'Situs Information' section for Situs street name having special keyword");

		String wrongBuildingPermitNumber = "T17-002105";
		ReportLogger.INFO("Validating the warning message for wrong APN for Building Permit number " + wrongBuildingPermitNumber);

		//Step5: Opening the Building Permit with the Building Permit Number imported through Efile import
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
		objApasGenericFunctions.globalSearchRecords(wrongBuildingPermitNumber);

		//Step6: Warning message validation for building permit(Imported through E-File Intake module) with retired permit and situs information mismatch
		expectedMessage = "Parcel and Situs information from the Building Permit does not match in the system.";
		softAssert.assertEquals(objBuildingPermitPage.warningMessageWithPriorityFlag.getText(), expectedMessage, "SMAB-T451: Warning message validation for building permit(Imported through E-File Intake module) with wrong APN");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate below functionalities
	 1. Atherton building permit import functionality with business admin and appraisal support user roles
	 2. Validation of error and imported records on review and approve screen
	 3. Status validation of imported file on efile import transaction log screen
	 **/
	@Test(description = "SMAB-T362,SMAB-T363,SMAB-T315,SMAB-T417,SMAB-T360,SMAB-T458,SMAB-T459,SMAB-T619,SMAB-T621,SMAB-T624,SMAB-T625,SMAB-T456,SMAB-T457,SMAB-T549,SMAB-T435: Transaction record verification for the imported Building Permit in TXT Format", dataProvider = "loginUsers",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_DiscardAndApprove_AthertonTxtFile(String loginUser) throws Exception {

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

		//Step5: Comparing the data from the error row table with the expected data
		ReportLogger.INFO("Data Validation of Error Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualErrorRowTable  = objApasGenericFunctions.getGridDataInHashMap(1);
		String expectedErrorRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "Import_TestData_ValidAndInvalidScenarios_AT_ExpectedErrorRecords.csv";
		HashMap<String, ArrayList<String>> expectedErrorRowTable = FileUtils.getCSVData(expectedErrorRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualErrorRowTable,expectedErrorRowTable),"","Data Comparison validation for Error Row Table");

		//Step6: Comparing the data from the Imported Row Table with the expected data
		ReportLogger.INFO("Data Validation of Imported Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualImportedRowTable  = objApasGenericFunctions.getGridDataInHashMap(2);
		String expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "Import_TestData_ValidAndInvalidScenarios_AT_ExpectedImportedRecords.csv";
		HashMap<String, ArrayList<String>> expectedImportedRowTable = FileUtils.getCSVData(expectedImportedRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualImportedRowTable,expectedImportedRowTable),"","Data Comparison validation for Imported Row Table");

		//Step7: Validating that correct number of records are moved to error and imported row sections after file import
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of Error and Imported Row Records on Review and Approve Data Page");
		//Validation of number of records in error row section. Expected is 13 as 13 records are passed with wrong data in the input file
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileImportPage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "14", "SMAB-T362: Validation if correct number of records are displayed in Error Row Section after file import");
		//Validation of number of records in imported row section. Expected is 3 as 3 records are passed with correct data in the input file
		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileImportPage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "8", "SMAB-T362: Validation if correct number of records are displayed in Imported Row Section after file import");

		//Step8: Validation for Records discard functionality from Review and Approve Page
		ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T363: Validation that error records can be discarded from Review and Approve Data Page");
		objPage.Click(objEfileImportPage.rowSelectCheckBox);
		objPage.Click(objEfileImportPage.discardButton);
		objPage.Click(objEfileImportPage.continueButton);
		numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileImportPage.errorRowSection).split(":")[1].trim();
		//validating the number of records in the error row section after discarding a record as records should be moved the imported row section after discard
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "13", "SMAB-T363: Validation if correct number of records are displayed in Error Row Section after discarding a record");
		numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileImportPage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "8", "SMAB-T362: Validation if correct number of records are displayed in Imported Row Section after discarding a record");

		//Step9: Validating the approve functionality after all the records are cleared from error section and approved
		ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T362: Validation that status is approved after approving all the records");
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
	 Below test case is used to validate below functionalities
	 1. San Mateo building permit import functionality
	 2. Validation of error and imported records on review and approve screen
	 **/
	@Test(description = "SMAB-T362,SMAB-T363: Verify Discard and Approve functionality for San Mateo Building Permit Import in XLS format", dataProvider = "loginUsers", dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, alwaysRun = true, enabled = true)
	public void verify_BuildingPermit_ApproveAndDiscard_SanMateo(String loginUser) throws Exception {

		String period = objUtil.getCurrentDate("MMMM YYYY");

		//Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and Import_Period__C='" + period + "' and File_Source__C = 'San Mateo Building permits' and Status__c = 'Approved'";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntake("Building Permit", "San Mateo Building permits", period ,sanMateoBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Comparing the data from the error row table with the expected data
		ExtentTestManager.getTest().log(LogStatus.INFO, "Data Validation of Error Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualErrorRowTable  = objApasGenericFunctions.getGridDataInHashMap(1);
		String expectedErrorRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "SanMateoBuildingPermitsWithValidAndInvalidData_ExpectedErrorRecords.csv";
		HashMap<String, ArrayList<String>> expectedErrorRowTable = FileUtils.getCSVData(expectedErrorRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualErrorRowTable,expectedErrorRowTable),"","Data Comparison validation for Error Row Table");

		//Step6: Comparing the data from the Imported Row Table with the expected data
		ExtentTestManager.getTest().log(LogStatus.INFO, "Data Validation of Imported Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualImportedRowTable  = objApasGenericFunctions.getGridDataInHashMap(2);
		String expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "SanMateoBuildingPermitsWithValidAndInvalidData_ExpectedImportedRecords.csv";
		HashMap<String, ArrayList<String>> expectedImportedRowTable = FileUtils.getCSVData(expectedImportedRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualImportedRowTable,expectedImportedRowTable),"","Data Comparison validation for Imported Row Table");

		//Step7: Validating that correct number of records are moved to error and imported row sections after file import
		ExtentTestManager.getTest().log(LogStatus.INFO, "Validation of Error and Imported Row Records on Review and Approve Data Page");
		//Validation of number of records in error row section. Expected is 13 as 13 records are passed with wrong data in the input file
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileImportPage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "13", "SMAB-T362: Validation if correct number of records are displayed in Error Row Section after file import");
		//Validation of number of records in imported row section. Expected is 3 as 3 records are passed with correct data in the input file
		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileImportPage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "3", "SMAB-T362: Validation if correct number of records are displayed in Imported Row Section after file import");

		//Step9: Validating the approve functionality after all the records are cleared from error section and approved
		ExtentTestManager.getTest().log(LogStatus.INFO, "SMAB-T362: Validation that status is approved after approving all the records");
		objPage.Click(objEfileImportPage.selectAllCheckBox);
		objPage.Click(objEfileImportPage.discardButton);
		objPage.Click(objEfileImportPage.continueButton);

		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);

		//Step8: Searching the efile intake module to validate the status of the imported file after approve
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		objEfileImportPage.selectFileAndSource("Building Permit", "San Mateo Building permits");
		softAssert.assertEquals(objPage.getElementText(objEfileImportPage.statusImportedFile), "Approved", "SMAB-T362: Validation if status of imported file is approved.");

		//Logout at the end of the test
		objApasGenericFunctions.logout();
	}



}
