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
	
	@BeforeMethod
	public void beforeMethod() {
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		objApasGenericFunctions.logout();
	}
	
	@Test(description = "SMAB-T206,SMAB-T303: Verifying download functionality for excel and PDF files", dataProvider = "loginBusinessAndPrincipalUsers", dataProviderClass = DataProviders.class, priority = 0, enabled = true)
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
		
		//Step4: Validating presence of Download, Export Composite Factors & Export Valuation Factors buttons and clicking then sequentially		
		boolean isDownloadBtnDisplayed = objBppTrnPg.isDownloadBtnVisible(20);
		softAssert.assertTrue(isDownloadBtnDisplayed, "SMAB-T206: Is Download button visible on approving all the the submitted calculations:");
		boolean isExportCompositeBtnDisplayed = objBppTrnPg.isExportCompositeFactorsBtnVisible(20);
		softAssert.assertTrue(isExportCompositeBtnDisplayed, "SMAB-T303: Is Export Composite Factors button visible on approving all the the submitted calculations:");		
		boolean isExportValuationBtnDisplayed = objBppTrnPg.isExportValuationFactorsBtnVisible(20);
		softAssert.assertTrue(isExportValuationBtnDisplayed, "SMAB-T303: Is Export Valuation Factors button visible on approving all the the submitted calculations:");

		objBppTrnPg.setParentWindowHandle();

		//Step5: Downloading PDF file by clicking Download button
		objBppTrnPg.clickDownloadBtn();
				
		//Step6: Downloading Composite Factors Excel file by clicking Export Composite Factors button
		objBppTrnPg.clickExportCompositeFactorsBtn();
		//objBppTrnPg.switchToNewWindow("Composite Factors");
		objBppTrnPg.switchToNewWindow();
		objBppTrnPg.exportBppTrendFactorsExcelFiles();
		driver.close();

		//Step7: Downloading Valuation Factors Excel file by clicking Export Valuation Factors button
		objBppTrnPg.switchToParentWindow();
		objBppTrnPg.clickExportValuationFactorsBtn();
		//objBppTrnPg.switchToNewWindow("Valuation Factors");
		objBppTrnPg.switchToNewWindow();
		objBppTrnPg.exportBppTrendFactorsExcelFiles();
		driver.close();
		
		objBppTrnPg.switchToParentWindow();
		
		//Step8: Validating whether files have been downloaded successfully in the download directory 
		String pdfFileName = CONFIG.getProperty("donwloadedPdfFileName").toUpperCase();
		String valuationExcelFileName = TestBase.CONFIG.getProperty("donwloadedValuationFactorFileName").toUpperCase();
		String compositeExcelFileName = TestBase.CONFIG.getProperty("donwloadedCompositeFactorFileName").toUpperCase();
				
		boolean isPdfDownloaded = false;
		boolean isCompositeExcelDownloaded = false;
		boolean isValuationExcelDownloaded = false;
		
		List<String> filesToDelete = new ArrayList<String>();
		
		List<String> downloadedFilesList = objBppTrnPg.checkFactorFilesInDownloadFolder();
		if(downloadedFilesList.size() == 0) {
			softAssert.assertTrue(false, "SMAB-T206,SMAB-T303: PDF & Excel files have not been downloaded");	
		} else {
			for(String fileName : downloadedFilesList) {
				if(fileName.contains(pdfFileName)) {
					filesToDelete.add(fileName);
					isPdfDownloaded = true;
				} else if (fileName.contains(valuationExcelFileName)) {
					filesToDelete.add(fileName);
					isValuationExcelDownloaded = true;
				} else if (fileName.contains(compositeExcelFileName)) {
					filesToDelete.add(fileName);
					isCompositeExcelDownloaded = true;
				}
			}			
		}
		
		softAssert.assertTrue(isPdfDownloaded, "SMAB-T206: Is PDF file downloaded successfully:");	
		softAssert.assertTrue(isCompositeExcelDownloaded, "SMAB-T303: Is Composite Factor XLSX file downloaded successfully:");	
		softAssert.assertTrue(isValuationExcelDownloaded, "SMAB-T303: Is Valuation Factor XLSX file downloaded successfully:");	

		//Step9: Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder(filesToDelete);
		
		softAssert.assertAll();
	}
}