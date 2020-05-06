package com.apas.Tests.BppTrend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrendFileDownloadTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	Map<String, String> dataMap;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() {
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}

	@AfterMethod(alwaysRun=true)
	public void afterMethod() throws Exception {
		objApasGenericFunctions.logout();
	}
	
	@Test(description = "SMAB-T303: Verifying download functionality for excel and PDF files", groups = {"smoke","regression","BPP Trends"}, dataProvider = "loginPrincipalUser", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
	public void verifyBppTrendDownloadCompositeAndValuationExcelFiles(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or Principal User)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.javascriptClick(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.javascriptClick(objBppTrnPg.selectRollYearButton);
		
		//Step4: Validating presence of Export Composite Factors & Export Valuation Factors buttons and clicking then sequentially
		boolean isExportCompositeBtnDisplayed = objBppTrnPg.isExportCompositeFactorsBtnVisible(20);
		softAssert.assertTrue(isExportCompositeBtnDisplayed, "SMAB-T303: Export Composite Factors button is visible");		
		boolean isExportValuationBtnDisplayed = objBppTrnPg.isExportValuationFactorsBtnVisible(20);
		softAssert.assertTrue(isExportValuationBtnDisplayed, "SMAB-T303: Export Valuation Factors button is visible");
	
		objBppTrnPg.setParentWindowHandle();

		//Step5: Downloading Composite Factors Excel file by clicking Export Composite Factors button
		objBppTrnPg.clickExportCompositeFactorsBtn();
		objBppTrnPg.switchToNewWindow();
		objBppTrnPg.exportBppTrendFactorsExcelFiles();
		driver.close();
		objBppTrnPg.switchToParentWindow();
	
		//Step6: Downloading Valuation Factors Excel file by clicking Export Valuation Factors button
		objBppTrnPg.clickExportValuationFactorsBtn();
		objBppTrnPg.switchToNewWindow();
		objBppTrnPg.exportBppTrendFactorsExcelFiles();
		driver.close();
		objBppTrnPg.switchToParentWindow();
		
		//Step7: Validating whether files have been downloaded successfully in the download directory 
		String valuationExcelFileName = TestBase.CONFIG.getProperty("donwloadedValuationFactorFileName").toUpperCase();
		String compositeExcelFileName = TestBase.CONFIG.getProperty("donwloadedCompositeFactorFileName").toUpperCase();

		boolean isCompositeExcelDownloaded = false;
		boolean isValuationExcelDownloaded = false;
		
		List<String> filesToDelete = new ArrayList<String>();
		
		List<String> downloadedFilesList = objBppTrnPg.checkFactorFilesInDownloadFolder();
		if(downloadedFilesList.size() == 2) {
			for(String fileName : downloadedFilesList) {
				if (fileName.contains(valuationExcelFileName)) {
					filesToDelete.add(fileName);
					isValuationExcelDownloaded = true;
				} else if (fileName.contains(compositeExcelFileName)) {
					filesToDelete.add(fileName);
					isCompositeExcelDownloaded = true;
				}
			}
		}

		softAssert.assertTrue(isCompositeExcelDownloaded, "SMAB-T303: Composite Factor XLSX file downloaded successfully");	
		softAssert.assertTrue(isValuationExcelDownloaded, "SMAB-T303: Valuation Factor XLSX file downloaded successfully");	
		
		//Step9: Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder(filesToDelete);
		
		softAssert.assertAll();
	}
	
	@Test(description = "SMAB-T206: Verifying download functionality for PDF files", groups = {"smoke","regression","BPP Trends"},dataProvider = "loginBusinessAndPrincipalUsers", dataProviderClass = DataProviders.class, priority = 1, enabled = true)
	public void verifyBppTrendDownloadBppTrendDetailsFile(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or Principal User)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.javascriptClick(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.javascriptClick(objBppTrnPg.selectRollYearButton);
		
		//Step4: Validating presence of Download buttons and clicking then sequentially		
		boolean isDownloadBtnDisplayed = objBppTrnPg.isDownloadBtnVisible(20);
		softAssert.assertTrue(isDownloadBtnDisplayed, "SMAB-T206: Download button is visible");

		//Step5: Downloading PDF file by clicking Download button
		objBppTrnPg.clickDownloadBtn();
		Thread.sleep(5000);
		
		//Step6: Validating whether files have been downloaded successfully in the download directory 
		String pdfFileName = CONFIG.getProperty("donwloadedPdfFileName").toUpperCase();
		boolean isPdfDownloaded = false;

		List<String> filesToDelete = new ArrayList<String>();
		
		List<String> downloadedFilesList = objBppTrnPg.checkFactorFilesInDownloadFolder();
		if(downloadedFilesList.size() == 1 && downloadedFilesList.get(0).contains(pdfFileName)) {
			String fileName = downloadedFilesList.get(0);
			filesToDelete.add(fileName);
			isPdfDownloaded = true;
		}
		softAssert.assertTrue(isPdfDownloaded, "SMAB-T206: PDF file downloaded with "+ loginUser +" user successfully");
		
		//Step9: Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder(filesToDelete);
		
		softAssert.assertAll();
	}
}