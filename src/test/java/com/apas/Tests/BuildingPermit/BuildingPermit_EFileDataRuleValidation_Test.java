package com.apas.Tests.BuildingPermit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.apas.PageObjects.EFileImportLogsPage;
import com.apas.Utils.DateUtil;
import org.json.JSONObject;
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
import com.apas.DataProviders.DataProviders;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermit_EFileDataRuleValidation_Test extends TestBase{
	
	RemoteWebDriver driver;
	Page objPage;
	BuildingPermitPage objBuildingPermitPage;
	SoftAssertion softAssert  = new SoftAssertion();
	Util objUtil  = new Util();
	EFileImportPage objEfileImportPage;
	EFileImportLogsPage objEFileImportLogsPage;
	SalesforceAPI salesforceAPI = new SalesforceAPI();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
		objEfileImportPage = new EFileImportPage(driver);
		objEFileImportLogsPage  = new EFileImportLogsPage(driver);
	}
	
	/**
	 Below test case is used to validate the building permit data imported through Efile Intake
	 **/
	@Test(description = "SMAB-T548: Validate removal of keywords from Work Description from UN File Type", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Regression","BuildingPermit"}, enabled = true)
	public void BuildingPermit_RemoveKeywordFromWorkDescription_UnincorporatedFileType(String loginUser) throws Exception {

		String unincorporatedBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_UNINCORPORATED + "WorkDescriptionWithKeywords_UN.txt";

		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Unincorporated%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Unincorporated Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Unincorporated Building permits", "WorkDescriptionWithKeywords_UN.txt", unincorporatedBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Expanding the import row section if its not already expanded
		ReportLogger.INFO("Expanding Import Row Section");
		objEfileImportPage.expandSection(objEfileImportPage.importedRowSectionExpandButton);

		//Step6: Comparing the data from the Imported Row Table with the expected data
		ExtentTestManager.getTest().log(LogStatus.INFO, "Data Validation of Imported Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualImportedRowTable  = objBuildingPermitPage.getGridDataInHashMap(1);
		String expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_UNINCORPORATED + "WorkDescriptionWithKeywords_UN_ExpectedImportedRecords.csv";
		HashMap<String, ArrayList<String>> expectedImportedRowTable = FileUtils.getCSVData(expectedImportedRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualImportedRowTable,expectedImportedRowTable),"","SMAB-T548: Data Comparison validation for Imported Row Table");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the building permit warning messages for retired parcel and situs information mismatch imported through Efile Intake
	 **/
	@Test(description = "SMAB-T453,SMAB-T455: Validate Retired parcel and situs information mismatch records imported through E-file intake module", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Smoke","Regression","BuildingPermit"}, enabled = true)
	public void BuildingPermit_ThroughEFileImportTool_WithRetiredParcelAndSitusMismatch(String loginUser) throws Exception {

		//Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Atherton%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
		String execEnv = System.getProperty("region");

		//Step1: Creating temporary file with random building permit number
		String buildingPermitNumber = "T" + DateUtil.getCurrentDate("dd-hhmmss");
		String buildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "RetiredParcelAndSitusMismatch.txt";
		String temporaryFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + "RetiredParcelAndSitusMismatch.txt";
		FileUtils.replaceString(buildingPermitFile,"<PERMITNO>",buildingPermitNumber,temporaryFile);

		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step3: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step4: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", "RetiredParcelAndSitusMismatch.txt", temporaryFile);

		//Step5: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);

		//Step6: Approving the imported file
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);
		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);
		String buildingPermitIDQuery = "SELECT Id FROM Building_Permit__c where Name = '"+buildingPermitNumber+"'";
		String buildingPermitId = salesforceAPI.select(buildingPermitIDQuery).get("Id").get(0);
		
		//Step7: Opening the building permit module
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Building_Permit__c/"+buildingPermitId+"/view");	
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		//Step8: Warning message validation for building permit(Imported through E-File Intake module) with retired permit and situs information mismatch
		softAssert.assertContains(objPage.getElementText(objBuildingPermitPage.warningMessageWithPriorityFlag).trim(), "APN is retired", "SMAB-T453,SMAB-T455: Warning message validation for building permit(Imported through E-File Intake module) with retired permit and situs information mismatch");
		softAssert.assertContains(objPage.getElementText(objBuildingPermitPage.warningMessageWithPriorityFlag).trim(), "City Situs not matching system", "SMAB-T453,SMAB-T455: Warning message validation for building permit(Imported through E-File Intake module) with retired permit and situs information mismatch");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the warning message for missing/wrong parcel
	 **/
	@Test(description = "SMAB-T451,SMAB-T374: Validate Warning message for Missing/Wrong Parcel and Situs Type auto population for the records imported through E-file intake module", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Smoke","Regression","BuildingPermit"},enabled = true)
	public void BuildingPermit_ThroughEFileImportTool_WrongParcelAndSitusTypePopulation(String loginUser) throws Exception {

		//Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Atherton%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Creating temporary file with random building permit number
		String missingAPNBuildingPermitNumber = "T" + DateUtil.getCurrentDate("dd-hhmmss");
		Thread.sleep(1000);
		String invalidAPNBuildingPermitNumber = "T" + DateUtil.getCurrentDate("dd-hhmmss");
		String execEnv = System.getProperty("region");
		String buildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "WrongParcelAndSitusTypePopulation.txt";
		String temporaryFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + "WrongParcelAndSitusTypePopulation.txt";
		FileUtils.replaceString(buildingPermitFile,"<PERMITNO>",missingAPNBuildingPermitNumber,temporaryFile);
		FileUtils.replaceString(temporaryFile,"<PERMIT-2>",invalidAPNBuildingPermitNumber,temporaryFile);

		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step3: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step4: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", "WrongParcelAndSitusTypePopulation.txt", temporaryFile);

		//Step5: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);

		//Step6: Approving the imported file
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);
		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);

		ReportLogger.INFO("Validating the warning message for missing APN for Building Permit number " + missingAPNBuildingPermitNumber);

		//Step3: Opening the Building Permit with the Building Permit Number imported through Efile import
		String buildingPermitNameQuery = "SELECT Id FROM Building_Permit__c where Name = '"+missingAPNBuildingPermitNumber+"'";
		HashMap<String, ArrayList<String>> hashMapBuildingPermitName = salesforceAPI.select(buildingPermitNameQuery);
		String buildingPermitName = hashMapBuildingPermitName.get("Id").get(0);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Building_Permit__c/"
				+ buildingPermitName + "/view");	
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);

		//Step4: Warning message validation for building permit(Imported through E-File Intake module) with retired permit and situs information mismatch
		softAssert.assertContains(objPage.getElementText(objBuildingPermitPage.warningMessageWithPriorityFlag).trim(), "Invalid APN.", "SMAB-T451: Warning message validation for building permit(Imported through E-File Intake module) with missing APN");

		//Step5: Validation of Situs Type population from Situs Street Name with special keywords
		ReportLogger.INFO("Validation of Situs Type population from Situs Street Name with special keywords");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Permit Situs Street Name","Situs Information"), "MOUNT", "SMAB-T374: 'Permit Situs Street Name' Field Validation in 'Situs Information' section for Situs street name having special keyword");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Permit Situs Type","Situs Information"), "BLVD", "SMAB-T374: 'Permit Situs Type' Field Validation in 'Situs Information' section for Situs street name having special keyword");

		ReportLogger.INFO("Validating the warning message for wrong APN for Building Permit number " + invalidAPNBuildingPermitNumber);

		//Step5: Opening the Building Permit with the Building Permit Number imported through Efile import
		String buildingPermitNumberQuery = "SELECT Id FROM Building_Permit__c where Name = '"+invalidAPNBuildingPermitNumber+"'";
		HashMap<String, ArrayList<String>> hashMapBuildingPermitNumber = salesforceAPI.select(buildingPermitNumberQuery);
		String buildingPermitNumber = hashMapBuildingPermitNumber.get("Id").get(0);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Building_Permit__c/"
				+ buildingPermitNumber + "/view");
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		
		//Step6: Warning message validation for building permit(Imported through E-File Intake module) with retired permit and situs information mismatch
		softAssert.assertContains(objBuildingPermitPage.warningMessageWithPriorityFlag.getText().trim(), "Invalid APN.", "SMAB-T451: Warning message validation for building permit(Imported through E-File Intake module) with wrong APN");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate that correct data is displayed after atherton file import
	 **/
	@Test(description = "SMAB-T417,SMAB-T456,SMAB-T452: Data validation after Atherton text file import", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Smoke","Regression","BuildingPermit"}, alwaysRun = true, enabled = true)
	public void BuildingPermit_DataValidationAfterAthertonTxtFileImport(String loginUser) throws Exception {

		String athertonBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "OneValidAndTwoInvalidRecordsForPermitValue.txt";

		//Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Atherton%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", "OneValidAndTwoInvalidRecordsForPermitValue.txt", athertonBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Comparing the data from the error row table with the expected data
		ReportLogger.INFO("Data Validation of Error Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualErrorRowTable  = objBuildingPermitPage.getGridDataInHashMap(1);
		String expectedErrorRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "OneValidAndTwoInvalidRecordsForPermitValue_ExpectedErrorRecords.csv";
		HashMap<String, ArrayList<String>> expectedErrorRowTable = FileUtils.getCSVData(expectedErrorRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualErrorRowTable,expectedErrorRowTable),"","SMAB-T417 : Data Comparison validation for Error Row Table");

		//Step6: Comparing the data from the Imported Row Table with the expected data
		ReportLogger.INFO("Data Validation of Imported Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualImportedRowTable  = objBuildingPermitPage.getGridDataInHashMap(2);
		String expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "OneValidAndTwoInvalidRecordsForPermitValue_ExpectedImportedRecords.csv";
		HashMap<String, ArrayList<String>> expectedImportedRowTable = FileUtils.getCSVData(expectedImportedRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualImportedRowTable,expectedImportedRowTable),"","SMAB-T417,SMAB-T456,SMAB-T452 : Data Comparison validation for Imported Row Table");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the data is imported correctly for San Mateo in Excel format
	 **/
	@Test(description = "SMAB-T417,SMAB-T456,SMAB-T357: Data validation for San Mateo Building Permit Import in XLS format", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Smoke","Regression","BuildingPermit"}, alwaysRun = true, enabled = true)
	public void BuildingPermit_DataValidationAfterSanMateoExcelFileImport(String loginUser) throws Exception {

		String sanMateoBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "NonNumericValueSanMateo.xlsx";

		//Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C = 'San Mateo Building permits' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "San Mateo Building permits", "NonNumericValueSanMateo.xlsx", sanMateoBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Comparing the data from the error row table with the expected data
		ReportLogger.INFO("Data Validation of Error Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualErrorRowTable  = objBuildingPermitPage.getGridDataInHashMap(1);
		String expectedErrorRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "NonNumericValueSanMateo_ExpectedErrorRecords.csv";
		HashMap<String, ArrayList<String>> expectedErrorRowTable = FileUtils.getCSVData(expectedErrorRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualErrorRowTable,expectedErrorRowTable),"","SMAB-T417:Data Comparison validation for Error Row Table");

		//Step6: Comparing the data from the Imported Row Table with the expected data
		ReportLogger.INFO("Data Validation of Imported Row Records on Review and Approve Data Page");
		HashMap<String, ArrayList<String>> actualImportedRowTable  = objBuildingPermitPage.getGridDataInHashMap(2);
		String expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "NonNumericValueSanMateo_ExpectedImportedRecords.csv";
		if (System.getProperty("region").toUpperCase().trim().equals("PREUAT")) {
			expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "NonNumericValueSanMateo_ExpectedImportedRecords_PREUAT.csv";
		}else if (System.getProperty("region").toUpperCase().trim().equals("STAGING")) {
			expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "NonNumericValueSanMateo_ExpectedImportedRecords_STAGING.csv";
		}else if (System.getProperty("region").toUpperCase().trim().equals("E2E")) {
			expectedImportedRowTableFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "NonNumericValueSanMateo_ExpectedImportedRecords_E2E.csv";
		}

		HashMap<String, ArrayList<String>> expectedImportedRowTable = FileUtils.getCSVData(expectedImportedRowTableFile);
		softAssert.assertEquals(FileUtils.compareHashMaps(actualImportedRowTable,expectedImportedRowTable),"","SMAB-T417,SMAB-T456,SMAB-T357:Data Comparison validation for Imported Row Table");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is validate the error message on Atherton file import
	 **/
	@Test(description = "SMAB-T315,SMAB-T458,SMAB-T459,SMAB-T619,SMAB-T621,SMAB-T624,SMAB-T625,SMAB-T457,SMAB-T549,SMAB-T460: Error message verification for the imported Atherton Building Permit in TXT Format", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Smoke","Regression","BuildingPermit"}, alwaysRun = true, enabled = true)
	public void BuildingPermit_ErrorMessageValidation_AthertonTxtFile(String loginUser) throws Exception {

		String buildingPermitFileName = "WrongMessageRecordsAtherton.txt";
		String athertonBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "WrongMessageRecordsAtherton.txt";

		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Atherton%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", buildingPermitFileName, athertonBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Comparing the data from the error row table with the expected data
		ReportLogger.INFO("Error Message Validation of Error Row Records on Review and Approve Data Page");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Blank City Strat Code"),"Invalid City Strat Code","SMAB-T457 : Error Message validation for the scenario 'Blank City Strat Code'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Wrong City Strat Code and City Code Combination"),"Invalid City Strat Code","SMAB-T457 : Error Message validation for the scenario 'Wrong City Strat Code and City Code Combination'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Issue Date Greater Than Today"),"Issue Date is greater than Current Date","SMAB-T459 : Error Message validation for the scenario 'Issue Date Greater Than Today'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Completed Date Greater Than Today"),"Completion Date is greater than Current Date","SMAB-T621 : Error Message validation for the scenario 'Completed Date Greater Than Today'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Completion Date Less Than Issue Date"),"Completion Date is less than Issue Date","SMAB-T458 : Error Message validation for the scenario 'Completion Date Less Than Issue Date'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Wrok Description with keyword Tree Removal"),"No Process for Work Desc. with \"Tree Removal\", \"Public Works Permits\" & \"Temporary Signs/Banners\"","SMAB-T315: Error Message validation for the scenario 'Wrok Description with keyword 'Tree Removal''");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Work Description with keyword public works permits"),"No Process for Work Desc. with \"Tree Removal\", \"Public Works Permits\" & \"Temporary Signs/Banners\"","SMAB-T315: Error Message validation for the scenario 'Work Description with keyword 'public works permits''");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Work Description with keywork temporary signs/banners"),"No Process for Work Desc. with \"Tree Removal\", \"Public Works Permits\" & \"Temporary Signs/Banners\"","SMAB-T315: Error Message validation for the scenario 'Work Description with keywork 'temporary signs/banners''");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Exponential Square Footage"),"Invalid Square Footage","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'Exponential Square Footage'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("String Square Footage"),"Invalid Square Footage","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'String Square Footage'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("String Permit Value"),"Invalid Permit Value","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'String Permit Value'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Exponential Permit Value"),"Invalid Permit Value","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'Exponential Permit Value'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("String Permit Fee"),"Invalid Permit Fee","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'String Permit Fee'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Exponential Permit Fee"),"Invalid Permit Fee","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'Exponential Permit Fee'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Blank City Code"),"City Code value must be AT","SMAB-T624 : Error Message validation for the scenario 'Blank City Code'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Wrong City Code"),"City Code value must be AT","SMAB-T624 : Error Message validation for the scenario 'Wrong City Code'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Negative Permit Value"),"Invalid Permit Value","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'Negative Permit Value'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Negative Permit Fee"),"Invalid Permit Fee","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'Negative Permit Fee'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Invalid Issue Date Format"),"Invalid Issue Date format","SMAB-T619 : Error Message validation for the scenario 'Invalid Issue Date Format'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Invalid Completion Date Format"),"Invalid Completed Date format","SMAB-T619 : Error Message validation for the scenario 'Invalid Completion Date Format'");
        softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Duplicate Record"),"Record is duplicate of another entry within the same file","SMAB-T1566 : Error Message validation for the scenario 'Duplicate Record'");
//      As per story SMAB-4193 records with zero permit fee and value will be successful now, hence commenting out these 2 validations
//		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Zero Permit Fee"),"Invalid Permit Fee","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'Zero Permit Fee'");
//      softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Zero Permit Value"),"Invalid Permit Value","SMAB-T625,SMAB-T460 : Error Message validation for the scenario 'Zero Permit Value'");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is validate the error message on Belmont file import
	 **/
	@Test(description = "SMAB-T362,SMAB-T1406: Error message verification for the imported Belmont Building Permit in TXT Format", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Regression","BuildingPermit"}, alwaysRun = true, enabled = true)
	public void BuildingPermit_ErrorMessageValidation_BelmontTxtFile(String loginUser) throws Exception {

		String belmontBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_BELMONT + "WrongMessageRecordsBelmont.txt";

		//Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Belmont%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Belmont Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Belmont Building permits", "WrongMessageRecordsBelmont.txt", belmontBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Comparing the data from the error row table with the expected data
		ReportLogger.INFO("Error Message Validation of Error Row Records on Review and Approve Data Page");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with TR"),"No Process for TR, MI & TE permits","SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with TR'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with MI"),"No Process for TR, MI & TE permits","SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with MI'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with TE"),"No Process for TR, MI & TE permits","SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with TE'");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is validate the error message on Burlingame file import
	 **/
	@Test(description = "SMAB-T362,SMAB-T1406: Error message verification for the imported Burlingame Building Permit in TXT Format", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Regression","BuildingPermit"}, alwaysRun = true, enabled = true)
	public void BuildingPermit_ErrorMessageValidation_BurlingameTxtFile(String loginUser) throws Exception {

		String burlingameBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_BURLINGAME + "WrongMessageRecordsBurlingame.txt";

		//Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Burlingame%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Burlingame Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Burlingame Building permits", "WrongMessageRecordsBurlingame.txt", burlingameBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Comparing the data from the error row table with the expected data
		ReportLogger.INFO("Error Message Validation of Error Row Records on Review and Approve Data Page");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with PW"),"No Process for PW, SW, PARK & REC permits","SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with PW'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with SW"),"No Process for PW, SW, PARK & REC permits","SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with SW'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with PARK"),"No Process for PW, SW, PARK & REC permits","SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with PARK'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with REC"),"No Process for PW, SW, PARK & REC permits","SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with REC'");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is validate the error message on Unincorporaed file import
	 **/
	@Test(description = "SMAB-T362,SMAB-T1406: Error message verification for the imported Unincorporated Building Permit in TXT Format", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Regression","BuildingPermit"}, alwaysRun = true, enabled = true)
	public void BuildingPermit_ErrorMessageValidation_UnincorporatedTxtFile(String loginUser) throws Exception {

		String buildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_UNINCORPORATED + "WrongMessageRecordsUnincorporated.txt";

		//Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Unincorporated%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Unincorporated Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Unincorporated Building permits", "WrongMessageRecordsUnincorporated.txt", buildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Comparing the data from the error row table with the expected data
		ReportLogger.INFO("Error Message Validation of Error Row Records on Review and Approve Data Page");
		String expectedErrorMessage = "No Process for DPW, INF, SWN, REV & M & Non-BLD permits";

		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with DPW"),expectedErrorMessage,"SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with DPW'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with INF"),expectedErrorMessage,"SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with INF'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with SWN"),expectedErrorMessage,"SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with SWN'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with REV"),expectedErrorMessage,"SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with REV'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number starts with M"),expectedErrorMessage,"SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number starts with M'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permits Number without BLD"),expectedErrorMessage,"SMAB-T1406 : Error Message validation for the scenario 'Building Permits Number without BLD'");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate error message for San Mateo file import
	 **/
	@Test(description = "SMAB-T315,SMAB-T458,SMAB-T459,SMAB-T619,SMAB-T621,SMAB-T624,SMAB-T625,SMAB-T457,SMAB-T549,SMAB-T1388,SMAB-T1566: Error message verification for the imported San Mateo Building Permit in XLS Format", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Smoke","Regression","BuildingPermit"}, alwaysRun = true, enabled = true)
	public void BuildingPermit_ErrorMessageValidation_SanMateoExcelFile(String loginUser) throws Exception {

		String sanMateoBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "WrongMessageRecordsSanMateo.xlsx";

		//Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C = 'San Mateo Building permits' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "San Mateo Building permits", "WrongMessageRecordsSanMateo.xlsx", sanMateoBuildingPermitFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ExtentTestManager.getTest().log(LogStatus.INFO, "Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Comparing the data from the error row table with the expected data
		ExtentTestManager.getTest().log(LogStatus.INFO, "Data Validation of Error Row Records on Review and Approve Data Page");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Blank City Strat Code"),"Invalid City Strat Code","SMAB-T457 : Error Message validation for the scenario 'Blank City Strat Code'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Wrong City Strat Code and City Code Combination"),"Invalid City Strat Code","SMAB-T457 : Error Message validation for the scenario 'Wrong City Strat Code and City Code Combination'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Issue Date Greater Than Today"),"Issue Date is greater than Current Date","SMAB-T459 : Error Message validation for the scenario 'Issue Date Greater Than Today'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Completed Date Greater Than Today"),"Completion Date is greater than Current Date","SMAB-T621 : Error Message validation for the scenario 'Completed Date Greater Than Today'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Completion Date Less Than Issue Date"),"Completion Date is less than Issue Date","SMAB-T458 : Error Message validation for the scenario 'Completion Date Less Than Issue Date'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Wrok Description with keyword Tree Removal"),"No Process for Work Desc. with \"Tree Removal\", \"Public Works Permits\" & \"Temporary Signs/Banners\"","SMAB-T315: Error Message validation for the scenario 'Wrok Description with keyword 'Tree Removal''");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Work Description with keyword public works permits"),"No Process for Work Desc. with \"Tree Removal\", \"Public Works Permits\" & \"Temporary Signs/Banners\"","SMAB-T315: Error Message validation for the scenario 'Work Description with keyword 'public works permits''");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Work Description with keywork temporary signs/banners"),"No Process for Work Desc. with \"Tree Removal\", \"Public Works Permits\" & \"Temporary Signs/Banners\"","SMAB-T315: Error Message validation for the scenario 'Work Description with keywork 'temporary signs/banners''");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("String Square Footage"),"Invalid Square Footage","SMAB-T625 : Error Message validation for the scenario 'String Square Footage'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("String Permit Value"),"Invalid Permit Value","SMAB-T625 : Error Message validation for the scenario 'String Permit Value'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("String Permit Fee"),"Invalid Permit Fee","SMAB-T625 : Error Message validation for the scenario 'String Permit Fee'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Blank City Code"),"City Code value must be SM","SMAB-T624 : Error Message validation for the scenario 'Blank City Code'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Wrong City Code"),"City Code value must be SM","SMAB-T624 : Error Message validation for the scenario 'Wrong City Code'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Negative Permit Value"),"Invalid Permit Value","SMAB-T625 : Error Message validation for the scenario 'Negative Permit Value'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Negative Permit Fee"),"Invalid Permit Fee","SMAB-T625 : Error Message validation for the scenario 'Negative Permit Fee'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Invalid Issue Date Format"),"Invalid Issue Date format","SMAB-T619 : Error Message validation for the scenario 'Invalid Issue Date Format'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Invalid Completion Date Format"),"Invalid Completed Date format","SMAB-T619 : Error Message validation for the scenario 'Invalid Completion Date Format'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permit Starts With ST"),"No Process for ST & MISC permits","SMAB-T1388 : Error Message validation for the scenario 'Building Permit Starts With ST'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Building Permit Starts With MISC"),"No Process for ST & MISC permits","SMAB-T1388 : Error Message validation for the scenario 'Building Permit Starts With MISC'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Duplicate Record"),"Record is duplicate of another entry within the same file","SMAB-T1566 : Error Message validation for the scenario 'Duplicate Record'");
//      As per story SMAB-4193 records with zero permit fee and value will be successful now, hence commenting out these 2 validations
//		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Zero Permit Fee"),"Invalid Permit Fee","SMAB-T625 : Error Message validation for the scenario 'Zero Permit Fee'");
//      softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Zero Permit Value"),"Invalid Permit Value","SMAB-T625 : Error Message validation for the scenario 'Zero Permit Value'");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is used to validate the duplicate building permit error message on efile import
	 **/
	@Test(description = "SMAB-T549,SMAB-T623: Validate the upsert functionality for record with same Permit Number, Parcel and City", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Smoke","Regression","BuildingPermit"})
	public void BuildingPermit_UpsertValidationForDuplicateRecord(String loginUser) throws Exception {

		//Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Atherton%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
		String execEnv = System.getProperty("region");

		//Step1: Creating temporary file with building permit number with existing city apn and parcel
		String buildingPermitNumber = "T" + DateUtil.getCurrentDate("dd-hhmmss");
		String buildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "DuplicateRecordUpsertValidation_AT.txt";
		String temporaryFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + "DuplicateRecordUpsertValidation_AT.txt";
		FileUtils.replaceString(buildingPermitFile,"<PERMITNO>",buildingPermitNumber,temporaryFile);

		//Updating the existing record in system with predefined values
		String buildingPermitUpdateQuery = "SELECT Id FROM Building_Permit__c where city_apn__C ='060241050' and situs_city_code__c='AT' limit 1";
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("Name",buildingPermitNumber);
		jsonObject.put("Issue_Date__C","2018-12-12");
		jsonObject.put("completion_date__c","2018-12-15");
		jsonObject.put("city_strat_code__c","ADDITION");
		jsonObject.put("building_permit_fee__C","1000");
		jsonObject.put("estimated_project_value__C","2000");
		salesforceAPI.update("Building_Permit__c",buildingPermitUpdateQuery,jsonObject);

		//Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step3: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step4: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", "DuplicateRecordUpsertValidation_AT.txt", temporaryFile);

		//Step5: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step6: Validation that there should be only 1 record in import section as the duplicate record should upsert
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileImportPage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "0", "SMAB-T549,SMAB-T623: Validation that error section should not have any record as the duplicate record should upsert");

		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileImportPage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "1", "SMAB-T549,SMAB-T623: Validation that import section should have only one record as the records should get upsert because of same City APN, City Code and Permit Number");

		//Step7: Approve the record
		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);
		String buildingPermitNameQuery = "SELECT Id FROM Building_Permit__c where Name = '"+buildingPermitNumber+"'";
		HashMap<String, ArrayList<String>> hashMapBuildingPermitName = salesforceAPI.select(buildingPermitNameQuery);
		String buildingPermitName = hashMapBuildingPermitName.get("Id").get(0);
		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Building_Permit__c/"
				+ buildingPermitName + "/view");	
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);

		//Step10: Validation that value of upserted records are reflected in the building permit
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Issue Date", "Building Permit Information"), "6/3/2018", "SMAB-T549,SMAB-T623: 'Issue Date' Field Validation in 'Building Permit Information' section for upsert record");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Completion Date", "Building Permit Information"), "11/1/2019", "SMAB-T549,SMAB-T623: 'Completion Date' Field Validation in 'Building Permit Information' section for upsert record");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Estimated Project Value"), "$2,300","SMAB-T549,SMAB-T623: 'Estimated Project Value' validation on the data displayed on the grid for upsert record");
		//Commenting the validation for city strat code as this validation was removed as part of story#1542
//		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("City Strat Code", "City and County Information"), "ALTERATION", "SMAB-T549,SMAB-T623: 'County Strat Code Description' Field Validation in 'City and County Information' section for upsert record");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Building Permit Fee", "Building Permit Information"), "$2,222.33", "SMAB-T549,SMAB-T623: 'Building Permit Fee' Field Validation in 'Building Permit Information' section for upsert record");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is validate that APN is mandatory at the time of upsert only if processing status = "Process" and the file name is updated in import logs and building permits
	 Note: APN is mandatory for Processing Status = "Process" only at the time of upsert. At the time of insert the other validation for invalid APN will take priority and building permit will be created with the warning message "Invalid APN"
	 **/
	@Test(description = "SMAB-T1536,SMAB-T1564,SMAB-T1565: Validate that APN is mandatory at the time of upsert only if processing status is Process and Import Name field validation", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Regression","BuildingPermit"}, alwaysRun = true)
	public void BuildingPermit_APNMandatoryForProcessDuringUpsert_ImportNameValidation(String loginUser) throws Exception {

		//Pre-requisite: Renaming the building permits(From File to be imported) already in the system to make the record as a fresh record
		//Creating two files for the same records as the same record processed needs to be processed two times
		String currentTimestamp = DateUtil.getCurrentDate("ddhhmmss");
		String firstBuildingPermitFileNameWithoutExtension = "SMAB2061_APNRequiredForProcess";
		String secondBuildingPermitFileNameWithoutExtension = "SMAB2061_APNRequiredForProcessNewName";
		String buildingPermitFileName = firstBuildingPermitFileNameWithoutExtension + ".txt";
		String secondBuildingPermitFileName  = secondBuildingPermitFileNameWithoutExtension + ".txt";
		String athertonBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + buildingPermitFileName;
		String temporaryFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + buildingPermitFileName;
		FileUtils.replaceString(athertonBuildingPermitFile,"<PERMIT>",currentTimestamp,temporaryFile);
		String secondTemporaryFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + secondBuildingPermitFileName;
		FileUtils.replaceString(athertonBuildingPermitFile,"<PERMIT>",currentTimestamp,secondTemporaryFile);

		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Atherton%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", buildingPermitFileName, temporaryFile);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Approve the imported records
		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);
		objPage.Click(objEfileImportPage.sourceDetails);
		
		String buildingPermitNumber = "T1" + currentTimestamp ;
		String buildingPermitIDQuery = "SELECT Id FROM Building_Permit__c where Name = '"+buildingPermitNumber+"'";
		String buildingPermitId = salesforceAPI.select(buildingPermitIDQuery).get("Id").get(0);
        String execEnv = System.getProperty("region");
		
		//Step7: Opening the building permit module
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Building_Permit__c/"+buildingPermitId+"/view");		
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		
		//Step6: Validate the import file name in the newly processed file. Below building permit is processed in the file uploaded in previous steps
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Import Name", "Building Permit Information"), firstBuildingPermitFileNameWithoutExtension, "SMAB-T1564: 'Import Name' Field Validation in 'Building Permit Information' section on Building Permit Screen");
		objBuildingPermitPage.searchModule(modules.EFILE_IMPORT_LOGS);
		objEFileImportLogsPage.openImportLog("Building Permit :Atherton Building Permits :Adhoc");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Import Name"), firstBuildingPermitFileNameWithoutExtension, "SMAB-T1564: 'Import Name' Field Validation on Import Logs Screen");

		//Step6: Searching the efile intake module to reimport the file after approve
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step8: Re-Uploading the Atherton Building Permit file
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", secondBuildingPermitFileName, secondTemporaryFile);
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step9: Validating that correct number of records are movered in Error Row section
		ReportLogger.INFO("Validation of error and imported records on Review and Approve Data Screen after import");
		String numberOfRecordsInErrorRowSection = objPage.getElementText(objEfileImportPage.errorRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInErrorRowSection, "2", "SMAB-T1536: Validation if correct number of records are displayed in Error Row Section after import");

		String numberOfRecordsInImportedRowSection = objPage.getElementText(objEfileImportPage.importedRowSection).split(":")[1].trim();
		softAssert.assertEquals(numberOfRecordsInImportedRowSection, "2", "SMAB-T364: Validation if correct number of records are displayed in Imported Row Section after import");

		//Step10: Comparing the error message when APN is mandatory for the process
		ReportLogger.INFO("Error Message Validation of Error Row Records on Review and Approve Data Page");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Invalid APN With Process(Permit Value Greater Than 25000 for REPAIR ROOF)"),"APN required for Process","SMAB-T1536 : Error Message validation for the scenario 'Invalid APN With Process(Permit Value Greater Than 25000 for REPAIR ROOF)'");
		softAssert.assertEquals(objEfileImportPage.getErrorMessageFromErrorGrid("Blank APN With Process(Permit Value Greater Than 25000 for REPAIR ROOF)"),"APN required for Process","SMAB-T1536 : Error Message validation for the scenario 'Blank APN With Process(Permit Value Greater Than 25000 for REPAIR ROOF)'");

		//Step6: Validate the import file name in the newly processed file. Below building permit is processed in the file uploaded in previous steps
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Building_Permit__c/"+buildingPermitId+"/view");
		objPage.waitForElementToBeClickable(objBuildingPermitPage.editButton,15);
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Import Name", "Building Permit Information"), secondBuildingPermitFileNameWithoutExtension, "SMAB-T1565: 'Import Name' Field Validation in 'Building Permit Information' section for the file name update once the same record is processed in the new file");
		objBuildingPermitPage.searchModule(modules.EFILE_IMPORT_LOGS);
		objEFileImportLogsPage.openImportLog("Building Permit :Atherton Building Permits :Adhoc");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Import Name"), secondBuildingPermitFileNameWithoutExtension, "SMAB-T1565: 'Import Name' Field Validation on Import Logs Screen after reprocessing the same record in new file");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

	/**
	 Below test case is validate if file name is updated in import logs and building permits
	 **/
	@Test(description = "SMAB-T1564,SMAB-T1565: Validate Import Name field is populated in import logs and building permit", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"Regression","BuildingPermit"}, alwaysRun = true)
	public void BuildingPermit_SanMateo_ImportNameValidation(String loginUser) throws Exception {

		//Pre-requisite: Renaming the building permits(From File to be imported) already in the system to make the record as a fresh record
		//Creating two files for the same records as the same record processed needs to be processed two times
		String buildingPermitNumber = "ABD-2025-123022";
		String firstBuildingPermitFileName = "SingleValidRecord.xlsx";
		String secondBuildingPermitFileName  = "SingleValidRecordNewName.xlsx";
		String firstBuildingPermitFilePath = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + firstBuildingPermitFileName;
		String secondBuildingPermitFilePath = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + secondBuildingPermitFileName;
		String execEnv = System.getProperty("region");
		File firstBuildingPermitFile = objBuildingPermitPage.createTempFile(firstBuildingPermitFilePath);
		File secondBuildingPermitFile = objBuildingPermitPage.createTempFile(secondBuildingPermitFilePath);
		firstBuildingPermitFileName = firstBuildingPermitFile.getName();
		secondBuildingPermitFileName  = secondBuildingPermitFile.getName();
		firstBuildingPermitFilePath = firstBuildingPermitFile.getAbsolutePath();
		secondBuildingPermitFilePath = secondBuildingPermitFile.getAbsolutePath();
		String firstBuildingPermitFileNameWithoutExtension = firstBuildingPermitFile.getName().split("\\.")[0];
		String secondBuildingPermitFileNameWithoutExtension = secondBuildingPermitFile.getName().split("\\.")[0];

		//step1:Reverting the Approved Import logs if any in the system
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C = 'San Mateo Building permits' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		String buildingPermitDeleteQuery = "SELECT Id FROM Building_Permit__c where Name ='" + buildingPermitNumber + "'";
		salesforceAPI.delete("Building_Permit__c",buildingPermitDeleteQuery);

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildingPermitPage.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the Atherton Building Permit file having error and success records through Efile Intake Import
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "San Mateo Building permits", firstBuildingPermitFileName, firstBuildingPermitFilePath);

		//Step4: Waiting for Status of the imported file to be converted to "Imported"
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);
		objPage.Click(objEfileImportPage.viewLink);
		objPage.waitForElementToBeVisible(objEfileImportPage.errorRowSection,30);

		//Step5: Approve the imported records
		objPage.Click(objEfileImportPage.approveButton);
		objPage.waitForElementToBeVisible(objEfileImportPage.efileRecordsApproveSuccessMessage, 20);
		objPage.Click(objEfileImportPage.sourceDetails);
		String buildingPermitIDQuery = "SELECT Id FROM Building_Permit__c where Name = '"+buildingPermitNumber+"'";
		String buildingPermitId = salesforceAPI.select(buildingPermitIDQuery).get("Id").get(0);
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Building_Permit__c/"+buildingPermitId+"/view");

		//Step6: Validate the import file name in the newly processed file. Below building permit is processed in the file uploaded in previous steps
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Import Name"), firstBuildingPermitFileNameWithoutExtension, "SMAB-T1564: 'Import Name' Field Validation in 'Building Permit Information' section on Building Permit Screen");
		objBuildingPermitPage.searchModule(modules.EFILE_IMPORT_LOGS);
		objEFileImportLogsPage.openImportLog("Building Permit :San Mateo Building permits :Adhoc");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Import Name"), firstBuildingPermitFileNameWithoutExtension, "SMAB-T1564: 'Import Name' Field Validation on Import Logs Screen");

		//Step7: Searching the efile intake module to reimport the file after approve
		objBuildingPermitPage.searchModule(modules.EFILE_INTAKE);

		//Step8: Re-Uploading the San Meteo Building Permit file
		objEfileImportPage.uploadFileOnEfileIntakeBP("Building Permit", "San Mateo Building permits", secondBuildingPermitFileName, secondBuildingPermitFilePath);
		ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
		objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile, "Imported", 120);

		//Step9: Validate the import file name in the newly processed file. Below building permit is processed in the file uploaded in previous steps
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Building_Permit__c/"+buildingPermitId+"/view");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Import Name"), secondBuildingPermitFileNameWithoutExtension, "SMAB-T1565: 'Import Name' Field Validation in 'Building Permit Information' section for the file name update once the same record is processed in the new file");
		objBuildingPermitPage.searchModule(modules.EFILE_IMPORT_LOGS);
		objEFileImportLogsPage.openImportLog("Building Permit :San Mateo Building permits :Adhoc");
		softAssert.assertEquals(objBuildingPermitPage.getFieldValueFromAPAS("Import Name"), secondBuildingPermitFileNameWithoutExtension, "SMAB-T1565: 'Import Name' Field Validation on Import Logs Screen after reprocessing the same record in new file");

		//Logout at the end of the test
		objBuildingPermitPage.logout();
	}

}