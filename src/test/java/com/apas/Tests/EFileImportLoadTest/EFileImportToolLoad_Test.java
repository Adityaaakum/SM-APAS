/*
 * package com.apas.Tests.EFileImportLoadTest;
 * 
 * import org.testng.annotations.Test;
 * 
 * import com.apas.DataProviders.DataProviders; import
 * com.apas.Reports.ReportLogger; import com.apas.config.modules; import
 * com.apas.config.testdata;
 * 
 * import java.util.ArrayList; import java.util.HashMap;
 * 
 * import org.json.JSONObject; import
 * org.openqa.selenium.remote.RemoteWebDriver; import
 * org.testng.annotations.BeforeMethod; import org.testng.annotations.Factory;
 * import org.testng.annotations.Test; import com.apas.Assertions.SoftAssertion;
 * import com.apas.BrowserDriver.BrowserDriver; import
 * com.apas.PageObjects.BuildingPermitPage; import
 * com.apas.PageObjects.EFileImportPage; import com.apas.PageObjects.Page;
 * import com.apas.Reports.ExtentTestManager; import
 * com.apas.Reports.ReportLogger; import com.apas.TestBase.TestBase; import
 * com.apas.Utils.ExcelUtils; import com.apas.Utils.FileUtils; import
 * com.apas.Utils.SalesforceAPI; import com.apas.Utils.Util; import
 * com.apas.config.modules; import com.apas.config.testdata; import
 * com.apas.generic.ApasGenericFunctions; import
 * com.apas.DataProviders.DataProviders; import
 * com.relevantcodes.extentreports.LogStatus;
 * 
 * public class EFileImportToolLoad_Test extends TestBase {
 * 
 * 
 * private String fileType; private String source; private String fileImport;
 * private String fileName;
 * 
 * RemoteWebDriver driver; Page objPage; ApasGenericFunctions
 * objApasGenericFunctions; BuildingPermitPage objBuildingPermitPage;
 * SoftAssertion softAssert = new SoftAssertion(); Util objUtil = new Util();
 * EFileImportPage objEfileImportPage; SalesforceAPI salesforceAPI = new
 * SalesforceAPI();
 * 
 * @BeforeMethod(alwaysRun = true) public void beforeMethod() throws Exception {
 * 
 * driver = null; setupTest(); driver = BrowserDriver.getBrowserInstance();
 * 
 * objPage = new Page(driver); objBuildingPermitPage = new
 * BuildingPermitPage(driver); objApasGenericFunctions = new
 * ApasGenericFunctions(driver); objEfileImportPage = new
 * EFileImportPage(driver); }
 * 
 * public EFileImportToolLoad_Test(String fileType, String source,String
 * fileImport, String fileName) {
 * 
 * this.fileType = fileType; this.source = source; this.fileImport = fileImport;
 * this.fileName = fileName;
 * 
 * }
 * 
 *//**
	 * Below test scenario is to upload different file types through Efile Intake
	 * for BP and BPP Trends
	 *//*
		 * @Test(description
		 * ="Below test scenario is to upload different file types through Efile Intake for BP and BPP Trends"
		 * , dataProvider = "loginBPPBusinessAdmin", dataProviderClass =
		 * DataProviders.class, groups = { "LoadTest" }) public void
		 * Upload_EFileImportTool(String loginUser) throws Exception {
		 * 
		 * 
		 * String unincorporatedBuildingPermitFile = System.getProperty("user.dir") +
		 * testdata.BUILDING_PERMIT_UNINCORPORATED +
		 * "WorkDescriptionWithKeywords_UN.txt";
		 * 
		 * //step1:Reverting the Approved Import logs if any in the system String query
		 * =
		 * "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C like '%Unincorporated%' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') "
		 * ; salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
		 * 
		 * 
		 * //Step0: Creating temporary file with random building permit number String
		 * buildingPermitNumber = "BD-2020-" + objUtil.getCurrentDate("hhmmss"); String
		 * newFileName = fileName + objUtil.getCurrentDate("dd-hhmmss");
		 * 
		 * String srcFile = System.getProperty("user.dir") + testdata.LOAD_Test +
		 * "\\" + fileName +".xlsx";
		 * 
		 * String tmpFile = System.getProperty("user.dir") +
		 * CONFIG.get("temporaryFolderPath") + newFileName + ".xlsx";
		 * 
		 * 
		 * //FileUtils.replaceString(buildingPermitFile,"<PERMITNO>",
		 * buildingPermitNumber,temporaryFile);
		 * 
		 * ExcelUtils exl = new ExcelUtils(); exl.setCellValueAndCopy(srcFile,
		 * buildingPermitNumber, tmpFile);
		 * 
		 * // Step1: Login to the APAS application using the credentials passed through
		 * // data provider (Business admin or appraisal support)
		 * 
		 * objApasGenericFunctions.login(loginUser);
		 * 
		 * // Step2: Opening the file import intake module
		 * objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
		 * 
		 * // Step3: Uploading the Unincorporated Building Permit file having error and
		 * // success records through Efile Intake Import
		 * 
		 * objEfileImportPage.uploadFileOnEfileIntake("Building Permit",
		 * "San Mateo Building permits",
		 * "SanMateoBuildingPermitsWithValidAndInvalidData.xlsx", buildingPermitFile);
		 * 
		 * 
		 * 
		 * objEfileImportPage.uploadFileOnEfileIntake(fileType, source, fileImport,
		 * tmpFile);
		 * 
		 * // Step4: Waiting for Status of the imported file to be converted to
		 * "Imported" ReportLogger.
		 * INFO("Waiting for Status of the imported file to be converted to Imported");
		 * objPage.waitForElementTextToBe(objEfileImportPage.statusImportedFile,
		 * "Imported", 120); objPage.Click(objEfileImportPage.viewLink);
		 * 
		 * }
		 * 
		 * }
		 */