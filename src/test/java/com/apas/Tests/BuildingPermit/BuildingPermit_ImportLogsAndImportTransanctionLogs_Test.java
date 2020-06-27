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
import com.apas.generic.ApasGenericFunctions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BuildingPermit_ImportLogsAndImportTransanctionLogs_Test extends TestBase {
    
	RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildPermit;
	EFileImportPage objEfileHomePage;
	EFileImportLogsPage objEFileImportLogsPage;
	Util objUtil  = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objEFileImportLogsPage  = new EFileImportLogsPage(driver);
	}

	/**
	 Below test case is used to validate Import Logs of the imported Atherton Building Permit file which in txt format
	 **/
	@Test(description = "SMAB-T431,SMAB-T430,SMAB-T662,SMAB-T92,SMAB-T93: Import Logs and Transactions verification for the imported Atherton Building Permit file in txt format", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, alwaysRun = true, enabled = true)
	public void verify_ImportLogsAndTransactions_BuildingPermitAtherton(String loginUser) throws Exception {

		String athertonBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + "OneValidAndTwoInvalidRecordsForPermitValue.txt";
		String period = objUtil.getCurrentDate("MMMM YYYY");

		//Pre-requisite : Reverting the Approved Import logs if any in the system
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			//Reverting the Approved Import logs if any in the system
			String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and Import_Period__C='" + period + "' and File_Source__C like '%Atherton%' and Status__c = 'Approved' ";
			salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");
		}else{
			//step1:Reverting the Approved Import logs if any in the system
			String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Atherton%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
			salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
		}

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the San Mateo Building Permit file having error and success records through Efile Intake Import
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			objEfileHomePage.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", period ,athertonBuildingPermitFile);
		} else{
			objEfileHomePage.uploadFileOnEfileIntakeBP("Building Permit", "Atherton Building Permits", "OneValidAndTwoInvalidRecordsForPermitValue.txt", athertonBuildingPermitFile);
		}

		//Step4: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);

		//Step5: Opening the Efile Import Logs module
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_LOGS);

		HashMap<String, ArrayList<String>> importLogsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);

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
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Name"), "Building Permit :Atherton Building Permits :" + period, "SMAB-T431,SMAB-T93 : 'Name' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("File Type"), "Building Permit", "SMAB-T431,SMAB-T93 : 'File Type' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Import Period"), period, "SMAB-T431,SMAB-T93 : 'Import Period' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("File Source"), "Atherton Building Permits", "SMAB-T431,SMAB-T93 : 'File Source' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Status"), "Imported", "SMAB-T431,SMAB-T93 : 'Status' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("File Count"), "3", "SMAB-T431,SMAB-T93 : 'File Count' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Error Count"), "2", "SMAB-T431,SMAB-T93 : 'Error Count' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Import Count"), "1", "SMAB-T431,SMAB-T93 : 'Import Count' Field Validation on Import Logs details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Allowed File Format"), "txt", "SMAB-T431,SMAB-T93 : 'Allowed File Format' Field Validation on Import Logs details tab");

		//Step8: Opening the transaction tab
		objPage.Click(objEFileImportLogsPage.transactionsTab);
		Thread.sleep(5000);
		HashMap<String, ArrayList<String>> importTransactionsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);
		softAssert.assertEquals(importTransactionsGridData.get("Status").get(0),"Imported","SMAB-T430,SMAB-T93 : Status validation on Transactions tab of Import Logs screen");

		objApasGenericFunctions.logout();
	}


	/**
	 Below test case is used to validate Import Logs of the imported San Mateo Building Permit file which in xls format
	 **/
	@Test(description = "SMAB-T431,SMAB-T95,SMAB-T96: Import Logs and Transactions verification for the imported San Mateo Building Permit file in xls format", dataProvider = "loginBPPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {"smoke","regression","buildingPermit"}, alwaysRun = true, enabled = true)
	public void verify_ImportLogsAndTransactions_BuildingPermitSanMateo(String loginUser) throws Exception {
		String sanMateoBuildingPermitFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + "SanMateoBuildingPermitsWithValidAndInvalidData.xlsx";
		String period = objUtil.getCurrentDate("MMMM YYYY");

		//Pre-requisite : Reverting the Approved Import logs if any in the system
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			//Reverting the Approved Import logs if any in the system
			String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and Import_Period__C='" + period + "' and File_Source__C like '%San Mateo%' and Status__c = 'Approved' ";
			salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");
		}else{
			//step1:Reverting the Approved Import logs if any in the system
			String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%San Mateo%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
			salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
		}

		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the file import intake module
		objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);

		//Step3: Uploading the San Mateo Building Permit file having error and success records through Efile Intake Import
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			objEfileHomePage.uploadFileOnEfileIntake("Building Permit", "San Mateo Building permits", period ,sanMateoBuildingPermitFile);
		} else{
			objEfileHomePage.uploadFileOnEfileIntakeBP("Building Permit", "San Mateo Building permits", "SanMateoBuildingPermitsWithValidAndInvalidData.xlsx", sanMateoBuildingPermitFile);
		}

		//Step4: Waiting for the status of the file to be converted to Imported
		objPage.waitForElementTextToBe(objEfileHomePage.statusImportedFile, "Imported", 120);

		//Step5: Opening the Efile Import Transactions module
		objApasGenericFunctions.searchModule(modules.EFILE_IMPORT_TRANSACTIONS);

		HashMap<String, ArrayList<String>> importTransactionsGridData = objApasGenericFunctions.getGridDataInHashMap(1, 1);

		//Step6: Import Transactions grid validation for the imported San Mateo Building Permit file
		ReportLogger.INFO("Validating the data on Import Logs Grid for San Mateo Building Permit file");
		softAssert.assertEquals(importTransactionsGridData.size(),8,"SMAB-T430,SMAB-T95 : Columns count validation on Import Transactions Grid");
		softAssert.assertEquals(importTransactionsGridData.get("Status").get(0),"Imported","SMAB-T430,SMAB-T95 : Validation for column 'Status' on Import Transactions Grid");
		softAssert.assertEquals(importTransactionsGridData.get("E-File Import Log").get(0),"Building Permit :San Mateo Building permits :" + period,"SMAB-T430,SMAB-T95 : Validation for column 'Name' on Import Transactions Grid");

		//Step7: Open the import Transaction of the building permit file uploaded in previous steps
		objEfileImportTransactionsPage.openImportTransactions(importTransactionsGridData.get("Name").get(0));
		Thread.sleep(5000);

		//Step8: Details Tab validation on Import Transactions tab
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Name"), importTransactionsGridData.get("Name").get(0), "SMAB-T431,SMAB-T96 : 'Name' Field Validation on Import Transaction details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("E-File Import Log"), "Building Permit :San Mateo Building permits :" + period, "SMAB-T431,SMAB-T96 : 'E-File Import Log' Field Validation on Import Transactions details tab");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Status"), "Imported", "SMAB-T431,SMAB-T96 : 'Status' Field Validation on Import Transactions details tab");
		softAssert.assertTrue(objPage.verifyElementVisible(objEfileImportTransactionsPage.transactionTrailTab), "SMAB-T431,SMAB-T96 : Validation for existence of Transactions Trails tab on Import Transactions Screen");

		objApasGenericFunctions.logout();
	}

}
