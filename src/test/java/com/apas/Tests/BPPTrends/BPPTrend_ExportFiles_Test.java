package com.apas.Tests.BPPTrends;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.apas.config.testdata;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BppTrendSetupPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ReportsPage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.BPPTablesData;
import com.apas.config.modules;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_ExportFiles_Test extends TestBase  {
	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	BppTrendPage objBppTrend;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	Map<String, String> dataMap;
	String rollYear;
    BppTrendSetupPage objBppTrendSetupPage;
    
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objBppTrend = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = "2020";
        objBppTrendSetupPage = new BppTrendSetupPage(driver);
        objApasGenericFunctions.updateRollYearStatus("Open", "2020");
	}
	/* DESCRIPTION: Performing following once all tables are APPROVED:
		 * 1. Validate Export Valuation & Composite Factors buttons are not visible before status of all the Factor Tables is 'Approved':: Test Case/JIRA ID: SMAB-T266, SMAB-T303, SMAB-T313
		 * 2. Validate Export Valuation & Composite Factors buttons are visible after status of all the Factor Tables is 'Approved':: Test Case/JIRA ID: SMAB-T266, SMAB-T303, SMAB-T313
		 */
	@Test(description = "SMAB-T266,SMAB-T303,SMAB-T313: Verifying download functionality for excel files", dataProvider = "loginPrincipalUser", groups = {"regression","BPPTrend"}, dataProviderClass = DataProviders.class)
	public void BppTrend_Verify_Export_CompositeAndValuationButtons_Visible(String loginUser) throws Exception {
		
		String downloadLocation = testdata.DOWNLOAD_FOLDER;
		ReportLogger.INFO("Download location : " + downloadLocation);		
		
		/*
		 * Validate Export Composite Factors & Export Valuation Factors buttons visibility  
		 * with status of Factor Tables as : Not Calculated/Yet to submit for Approval
		 */		
		
		//Step1: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Not Calculated", rollYear);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);
		
		//Step2: Login to the APAS application, Opening the BPP Trend module and selecting the Roll Year
		objApasGenericFunctions.login(loginUser);
		objBppTrend.selectRollYearOnBPPTrends(rollYear);
		
		//Step3: Validate Export Composite Factors & Export Valuation Factors buttons are not visible when Status is 'Not Calculated/Yet to be Submit for Approval'
		boolean exportCompositeFactorButtonVisible = Objects.isNull(objPage.locateElement(objBppTrend.xPathExportCompositeFactorsButton, 10));
		softAssert.assertTrue(exportCompositeFactorButtonVisible, "SMAB-T313: Verify Export Composite Factors button is not visible when status is 'Not Calculated/Yet to be Submit for Approval'");
		
		boolean exportValuationFactorButtonVisible = Objects.isNull(objPage.locateElement(objBppTrend.xPathExportValuationFactorsButton, 10));
		softAssert.assertTrue(exportValuationFactorButtonVisible, "SMAB-T313: Verify Export Valuation Factors button is not visible when status is 'Not Calculated/Yet to be Submit for Approval'");
		
		/*
		 * Validate Export Composite Factors & Export Valuation Factors buttons visibility  
		 * with status of Factor Tables as : Calculated/Yet to submit for Approval
		 */	
		
		//Step4 Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Calculated", rollYear);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Yet to submit for Approval", rollYear);
				
		//Step5: Opening the BPP Trend module and selecting the Roll Year
		objBppTrend.selectRollYearOnBPPTrends(rollYear);
		
		//Step6: Validating presence of Export Composite Factors & Export Valuation Factors buttons
		exportCompositeFactorButtonVisible = Objects.isNull(objPage.locateElement(objBppTrend.xPathExportCompositeFactorsButton, 10));
		softAssert.assertTrue(exportCompositeFactorButtonVisible, "SMAB-T313: Verify Export Composite Factors button is not visible when status is 'Calculated/Yet to be Submit for Approval'");
		
		exportValuationFactorButtonVisible = Objects.isNull(objPage.locateElement(objBppTrend.xPathExportValuationFactorsButton, 10));
		softAssert.assertTrue(exportValuationFactorButtonVisible, "SMAB-T313: Verify Export Valuation Factors button is not visible when status is 'Calculated/Yet to be Submit for Approval'");
		
		/*
		 * Validate Export Composite Factors & Export Valuation Factors buttons visibility  
		 * with status of Factor Tables as : Submitted for Approval
		 */
 
		//Step7 Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Submitted for Approval", rollYear);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Submitted for Approval", rollYear);
				
		//Step8: Opening the BPP Trend module and selecting the Roll Year
		objBppTrend.selectRollYearOnBPPTrends(rollYear);
		
		//Step9: Validating presence of Export Composite Factors & Export Valuation Factors buttons
		exportCompositeFactorButtonVisible = Objects.isNull(objPage.locateElement(objBppTrend.xPathExportCompositeFactorsButton, 10));
		softAssert.assertTrue(exportCompositeFactorButtonVisible, "SMAB-T313: Verify Export Composite Factors button is not visible when status is 'Submitted for Approval'");
		
		exportValuationFactorButtonVisible = Objects.isNull(objPage.locateElement(objBppTrend.xPathExportValuationFactorsButton, 10));
		softAssert.assertTrue(exportValuationFactorButtonVisible, "SMAB-T313: Verify Export Valuation Factors button is not visible when status is 'Submitted for Approval'");
		
		/*
		 * Validate Export Composite Factors & Export Valuation Factors buttons visibility  
		 * with status of Factor Tables as : Approved
		 */
	 
		//Step10 Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Approved", rollYear);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Approved", rollYear);
				
		//Step11: Opening the BPP Trend module and selecting the Roll Year
		objBppTrend.selectRollYearOnBPPTrends(rollYear);
		
		//Step12: Validating presence of Export Composite Factors & Export Valuation Factors buttons
		exportCompositeFactorButtonVisible = Objects.nonNull(objPage.locateElement(objBppTrend.xPathExportCompositeFactorsButton, 20));
		softAssert.assertTrue(exportCompositeFactorButtonVisible, "SMAB-T266,SMAB-T303,SMAB-T313: Verify Export Composite Factors button is visible when status is 'Approved'");
		
		exportValuationFactorButtonVisible = Objects.nonNull(objPage.locateElement(objBppTrend.xPathExportValuationFactorsButton, 20));
		softAssert.assertTrue(exportValuationFactorButtonVisible, "SMAB-T266,SMAB-T303,SMAB-T313: Verify Export Valuation Factors button is not visible when status is 'Approved'");
					
		objApasGenericFunctions.logout();
	}
	
	/* DESCRIPTION: Performing following once all tables are APPROVED:
	 * 1. Validate BPP Trends File is downloaded successfully in Excel format on clicking Download button:: Test Case/JIRA ID: SMAB-T206
	 */
	@Test(description = "SMAB-T206: Verifying download functionality for excel files", dataProvider = "loginPrincipalUser", groups = {"regression","BPPTrend"}, dataProviderClass = DataProviders.class)
	public void BppTrend_Verify_Download(String loginUser) throws Exception {
		
		String downloadLocation = testdata.DOWNLOAD_FOLDER;
		ReportLogger.INFO("Download location : " + downloadLocation);		
		
		/*
		 * Validate Export Composite Factors & Export Valuation Factors buttons visibility  
		 * with status of Factor Tables as : Approved
		 */		
		
		//Step1: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Approved", rollYear);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Approved", rollYear);
				
		//Deleteing all the previously downloaded files
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);
		
		//Step2: Login to the APAS application, Opening the BPP Trend module and selecting the Roll Year
		objApasGenericFunctions.login(loginUser);
		objBppTrend.selectRollYearOnBPPTrends(rollYear);
		
		//Step3: Downloading PDF file by clicking Download button
		objBppTrend.clickDownloadBtn();
		
		File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
		String exportedFileName = downloadedFile.getName();
		softAssert.assertTrue(exportedFileName.contains("BPP Trends Details " + rollYear + ".pdf"), "SMAB-T206: Verify Principal User/Business admin are able to export Output tables in PDF format");
		
		objApasGenericFunctions.logout();
	}
	
	/* DESCRIPTION: Performing following once all tables are APPROVED:
	 * 1. Validate Excel Files are downloaded on clicking Export Valuation & Composite Factors buttons :: Test Case/JIRA ID: SMAB-T1132
	 */
	@Test(description = "SMAB-T1132: Verifying download functionality for excel files", dataProvider = "loginPrincipalUser", groups = {"regression","BPPTrend"}, dataProviderClass = DataProviders.class)
	public void BppTrend_Export_CompositeAndValuationFactor_Files(String loginUser) throws Exception {
		
		String downloadLocation = testdata.DOWNLOAD_FOLDER;
		ReportLogger.INFO("Download location : " + downloadLocation);		
		
		/*
		 * Validate Export Composite Factors & Export Valuation Factors buttons visibility  
		 * with status of Factor Tables as : Approved
		 */		
		
		//Step1: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Approved", rollYear);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Approved", rollYear);
				
		//Deleteing all the previously downloaded files
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);
		
		//Step2: Login to the APAS application, Opening the BPP Trend module and selecting the Roll Year
		objApasGenericFunctions.login(loginUser);
		objBppTrend.selectRollYearOnBPPTrends(rollYear);		
		
		//Step3: Downloading Composite Factors Excel file by clicking Export Composite Factors button
		String parentwindow = driver.getWindowHandle();
		objBppTrend.exportCompositeOrValuationFactorsFiles("Composite");
		driver.switchTo().window(parentwindow);
		
		File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
		String exportedFileName = downloadedFile.getName();
		softAssert.assertTrue(exportedFileName.contains("BPP - Composite Factors by Roll Year-" + rollYear), "SMAB-T1132: Verify downloaded excel file: "+ exportedFileName);
		
		//Deleteing all the previously downloaded files
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);
		
		//Step4: Downloading Valuation Factors Excel file by clicking Export Valuation Factors button
		objBppTrend.exportCompositeOrValuationFactorsFiles("Valuation");
		driver.switchTo().window(parentwindow);
		
		downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
		exportedFileName = downloadedFile.getName();
		softAssert.assertTrue(exportedFileName.contains("BPP - Valuation Factors by Roll Year-" + rollYear), "SMAB-T1132: Verify downloaded excel file: "+ exportedFileName);
		
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing following once all tables are APPROVED:
	 * 1. Validate PDF file has been successfully downloaded in the system at given path:: Test Case/JIRA ID: SMAB-T207, SMAB-T1130
	 */
	@Test(description = "SMAB-T207,SMAB-T1130: Verifying download functionality for PDF file from Bpp Trend Setup page", dataProvider = "loginBusinessAndPrincipalUsers", groups = {"regression","BPPTrend"}, dataProviderClass = DataProviders.class)
	public void BppTrend_DownloadBppTrendPdfFile_From_BppTrendSetupPage(String loginUser) throws Exception {

		//Step1: Login to the APAS application, Opening the BPP Trend module and selecting the Roll Year
		objApasGenericFunctions.login(loginUser);
		
		String downloadLocation = testdata.DOWNLOAD_FOLDER;
		ReportLogger.INFO("Download location : " + downloadLocation);
		
		//Step1: Updating the composite factor and Valuation factor tables status
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.COMPOSITE_TABLES_API_NAMES, "Approved", rollYear);
		objBppTrend.updateTablesStatusForGivenRollYear(BPPTablesData.VALUATION_TABLES_API_NAMES, "Approved", rollYear);

		//Deleteing all the previously downloaded files
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);

		//Step2: Opening the BPP Trend module and selecting the Roll Year
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.displayRecords("All");
		objBppTrendSetupPage.clickOnEntryNameInGrid(rollYear);
		Thread.sleep(2000);	

		//*** Downloading BPP Trends File from BPP Trend Setup Page ***		
		//Step3: Clicking 1st BPP Trends File Link
		ReportLogger.INFO("Clicking download button on Bpp Trend Setup Details page");
		objPage.Click(objBppTrend.downloadBtnBppTrendSetupPage);
		
		//Step4: Clicking 'Download' button present on Pop-Up
		ReportLogger.INFO("Clicking download button In pop up");
		objBppTrend.Click(objBppTrend.downloadBtnInPopUpOnBppTrendSetupPage);
		Thread.sleep(5000);

		//Step5: Validate whether files have been downloaded successfully in the download directory
		ReportLogger.INFO("Locating the files in local directory.");
		File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
		String exportedFileName = downloadedFile.getName();
		System.out.println("fileName: "+exportedFileName);
		softAssert.assertTrue(exportedFileName.contains("BPP Trends Details " + rollYear + ".pdf"), "SMAB-T207,SMAB-T1130: Verify Principal User/Business admin are download BPP Trends File in PDF format");

		//Step6: Deleting downloaded files from download directory
		objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);

		//Step7: Closing File Download Pop-Up
		objBppTrend.javascriptClick(objBppTrend.closeBtnFileDownloadPage);
		
		objApasGenericFunctions.logout();
	}
}
