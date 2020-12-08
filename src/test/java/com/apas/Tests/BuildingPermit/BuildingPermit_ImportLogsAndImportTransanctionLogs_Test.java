package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BuildingPermit_ImportLogsAndImportTransanctionLogs_Test extends TestBase {
    
	RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	BuildingPermitPage objBuildPermit;
	EFileImportPage objEfileHomePage;
	EFileImportLogsPage objEFileImportLogsPage;
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objEFileImportLogsPage  = new EFileImportLogsPage(driver);
	}

	/**
	 Below test case is used to validate Import Logs of the imported Atherton Building Permit file which in txt format
	 **/
	@Test(description = "SMAB-T431,SMAB-T430,SMAB-T662,SMAB-T92,SMAB-T93: Import Logs and Transactions verification for the imported Atherton Building Permit file in txt format", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, alwaysRun = true, enabled = true)
	public void ImportLogsAndTransactions_BuildingPermitAtherton(String loginUser) throws Exception {

		String athertonBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "OneValidAndTwoInvalidRecordsForPermitValue.txt";
		File tempFile = objBuildPermit.createTempFile(athertonBuildingPermitFile);

		//Pre-requisite : Reverting the Approved Import logs if any in the system
		String period = "Adhoc";
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Atherton%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildPermit.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildPermit.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the San Mateo Building Permit file having error and success records through Efile Intake Import
		objEfileHomePage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", tempFile.getName(), tempFile.getAbsolutePath());

		//Step4: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);

		//Step5: Opening the Efile Import Logs module
		objBuildPermit.searchModule(modules.EFILE_IMPORT_LOGS);
		objBuildPermit.displayRecords("All");

		HashMap<String, ArrayList<String>> importLogsGridData = objBuildPermit.getGridDataInHashMap(1, 1);

		//Step6: Import Logs grid validation for the imported Atherton Building Permit file
		ReportLogger.INFO("Validating the data on Import Logs Grid for Atherton Building Permit file");
		softAssert.assertEquals(importLogsGridData.size(),10,"SMAB-T431,SMAB-T92 : Columns count validation on Import Logs Grid");
		softAssert.assertEquals(importLogsGridData.get("Name").get(0),"Building Permit :Atherton Building Permits :" + period,"SMAB-T431,SMAB-T92,SMAB-T662: Validation for column 'Name' on Import Logs Grid");
		softAssert.assertEquals(importLogsGridData.get("File Type").get(0),"Building Permit","SMAB-T431,SMAB-T92,SMAB-T662: Validation for column 'File Type' on Import Logs Grid");
		softAssert.assertEquals(importLogsGridData.get("File Source").get(0),"Atherton Building Permits","SMAB-T431,SMAB-T92,SMAB-T662: Validation for column 'File Source' on Import Logs Grid");
		softAssert.assertEquals(importLogsGridData.get("Import Period").get(0),period,"SMAB-T431,SMAB-T92,SMAB-T662: Validation for column 'Import Period' on Import Logs Grid");
		softAssert.assertEquals(importLogsGridData.get("Status").get(0),"Imported","SMAB-T431,SMAB-T92,SMAB-T662: Validation for column 'Status' on Import Logs Grid");
		softAssert.assertEquals(importLogsGridData.get("File Count").get(0),"3","SMAB-T431,SMAB-T92,SMAB-T662: Validation for column 'File Count' on Import Logs Grid");
		softAssert.assertEquals(importLogsGridData.get("Import Count").get(0),"1","SMAB-T431,SMAB-T92,SMAB-T662: Validation for column 'Import Count' on Import Logs Grid");

		//Step7: Open the import logs of the building permit file uploaded in previous steps
		objEFileImportLogsPage.openImportLog("Building Permit :Atherton Building Permits :" + period);

		//Import logs Details tab field validation
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("Name"), "Building Permit :Atherton Building Permits :" + period, "SMAB-T431,SMAB-T93 : 'Name' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("Import Name"), tempFile.getName().split("\\.")[0], "SMAB-T431,SMAB-T93 : 'Import Name' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("File Type"), "Building Permit", "SMAB-T431,SMAB-T93 : 'File Type' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("Import Period"), period, "SMAB-T431,SMAB-T93 : 'Import Period' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("File Source"), "Atherton Building Permits", "SMAB-T431,SMAB-T93 : 'File Source' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("Status"), "Imported", "SMAB-T431,SMAB-T93 : 'Status' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("File Count"), "3", "SMAB-T431,SMAB-T93 : 'File Count' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("Error Count"), "2", "SMAB-T431,SMAB-T93 : 'Error Count' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("Import Count"), "1", "SMAB-T431,SMAB-T93 : 'Import Count' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("Allowed File Format"), "txt", "SMAB-T431,SMAB-T93 : 'Allowed File Format' Field Validation on Import Logs details tab");

		//Step8: Opening the transaction tab
		objPage.Click(objEFileImportLogsPage.transactionsTab);
		Thread.sleep(5000);
		HashMap<String, ArrayList<String>> importTransactionsGridData = objBuildPermit.getGridDataInHashMap(1, 1);
		softAssert.assertEquals(importTransactionsGridData.get("Status").get(0),"Imported","SMAB-T430,SMAB-T93 : Status validation on Transactions tab of Import Logs screen");

		objBuildPermit.logout();
	}


	/**
	 Below test case is used to validate Import Logs of the imported San Mateo Building Permit file which in xls format
	 **/
	@Test(description = "SMAB-T431,SMAB-T95,SMAB-T96: Import Logs and Transactions verification for the imported San Mateo Building Permit file in xls format", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"regression","buildingPermit"}, alwaysRun = true, enabled = true)
	public void ImportLogsAndTransactions_BuildingPermitSanMateo(String loginUser) throws Exception {
		String sanMateoBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "SanMateoBuildingPermitsWithValidAndInvalidData.xlsx";

		//Pre-requisite : Reverting the Approved Import logs if any in the system
		//step1:Reverting the Approved Import logs if any in the system
		String period = "Adhoc";
		String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%San Mateo%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
		salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objBuildPermit.login(loginUser);

		//Step2: Opening the file import intake module
		objBuildPermit.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the San Mateo Building Permit file having error and success records through Efile Intake Import
		objEfileHomePage.uploadFileOnEfileIntakeBP("Building Permit", "San Mateo Building permits", "SanMateoBuildingPermitsWithValidAndInvalidData.xlsx", sanMateoBuildingPermitFile);

		//Step4: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);

		//Step5: Opening the Efile Import Transactions module
		objBuildPermit.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);

		HashMap<String, ArrayList<String>> importTransactionsGridData = objBuildPermit.getGridDataInHashMap(1, 1);

		//Step6: Import Transactions grid validation for the imported San Mateo Building Permit file
		ReportLogger.INFO("Validating the data on Import Logs Grid for San Mateo Building Permit file");
		softAssert.assertEquals(importTransactionsGridData.size(),8,"SMAB-T430,SMAB-T95 : Columns count validation on Import Transactions Grid");
		softAssert.assertEquals(importTransactionsGridData.get("Status").get(0),"Imported","SMAB-T430,SMAB-T95 : Validation for column 'Status' on Import Transactions Grid");
		softAssert.assertEquals(importTransactionsGridData.get("E-File Import Log").get(0),"Building Permit :San Mateo Building permits :" + period,"SMAB-T430,SMAB-T95 : Validation for column 'Name' on Import Transactions Grid");

		//Step7: Open the import Transaction of the building permit file uploaded in previous steps
		objEfileImportTransactionsPage.openImportTransactions(importTransactionsGridData.get("Name").get(0));
		Thread.sleep(5000);

		//Step8: Details Tab validation on Import Transactions tab
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("Name"), importTransactionsGridData.get("Name").get(0), "SMAB-T431,SMAB-T96 : 'Name' Field Validation on Import Transaction details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("E-File Import Log"), "Building Permit :San Mateo Building permits :" + period, "SMAB-T431,SMAB-T96 : 'E-File Import Log' Field Validation on Import Transactions details tab");
		softAssert.assertEquals(objBuildPermit.getFieldValueFromAPAS("Status"), "Imported", "SMAB-T431,SMAB-T96 : 'Status' Field Validation on Import Transactions details tab");
		softAssert.assertTrue(objPage.verifyElementVisible(objEfileImportTransactionsPage.transactionTrailTab), "SMAB-T431,SMAB-T96 : Validation for existence of Transactions Trails tab on Import Transactions Screen");

		objBuildPermit.logout();
	}

}
