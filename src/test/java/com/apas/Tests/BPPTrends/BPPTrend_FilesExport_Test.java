package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BPPTrend_FilesExport_Test extends TestBase {

	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermitPage;
	Util objUtil;
	SoftAssertion softAssert;
	Map<String, String> dataMap;
	String rollYear;
	
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		
		if(driver==null) {
            setupTest();
            driver = BrowserDriver.getBrowserInstance();
        }
		
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		//objApasGenericFunctions.logout();
	}
	
	/**
	 * DESCRIPTION: Performing following once all tables are APPROVED:
	 * 1. Validating availability of the buttons to Export Valuation & Composite Factors EXCEL files:: Test Case/JIRA ID: SMAB-T303
	 * 2. Downloading Valuation Factors as Excel file:: Test Case/JIRA ID: SMAB-T303
	 * 3. Downloading Composite Factors as Excel file:: Test Case/JIRA ID: SMAB-T303
	 * 4. Validating whether files have been successfully downloaded in the system at given path:: Test Case/JIRA ID: SMAB-T303
	 * 5. Deleting the files once verification is done
	 */
	@Test(description = "SMAB-T303,SMAB-T266,SMAB-T313: Verifying download functionality for excel files", dataProvider = "loginPrincipalUser", groups = {"regression","BPPTrend"}, dataProviderClass = DataProviders.class)
	public void verify_BppTrend_DownloadCompositeAndValuationExcelFiles(String loginUser) throws Exception {		
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
		
		//Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder();
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or Principal User)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
		
		//Step4: Validating presence of Export Composite Factors & Export Valuation Factors buttons and clicking then sequentially
		boolean isExportCompositeBtnDisplayed = objBppTrnPg.isExportCompositeFactorsBtnVisible(20);
		softAssert.assertTrue(isExportCompositeBtnDisplayed, "SMAB-T303: Export Composite Factors button is visible");
		softAssert.assertTrue(isExportCompositeBtnDisplayed, "SMAB-T266: Export Composite Factors button is visible");
		softAssert.assertTrue(isExportCompositeBtnDisplayed, "SMAB-T313: Export Valuation Factors button is visible");
		boolean isExportValuationBtnDisplayed = objBppTrnPg.isExportValuationFactorsBtnVisible(20);
		softAssert.assertTrue(isExportValuationBtnDisplayed, "SMAB-T303: Export Valuation Factors button is visible");
		softAssert.assertTrue(isExportValuationBtnDisplayed, "SMAB-T266: Export Valuation Factors button is visible");
		softAssert.assertTrue(isExportValuationBtnDisplayed, "SMAB-T313: Export Valuation Factors button is visible");
	
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
		softAssert.assertTrue(isCompositeExcelDownloaded, "SMAB-T266: Composite Factor XLSX file downloaded successfully");
		softAssert.assertTrue(isValuationExcelDownloaded, "SMAB-T303: Valuation Factor XLSX file downloaded successfully");
		softAssert.assertTrue(isValuationExcelDownloaded, "SMAB-T266: Valuation Factor XLSX file downloaded successfully");

		//Step8: Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder();

		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

	/**
	 * DESCRIPTION: Performing following once all tables are APPROVED:
	 * 1. Validating availability of the Download button:: Test Case/JIRA ID: SMAB-T206
	 * 2. Downloading the PDF file:: Test Case/JIRA ID: SMAB-T206
	 * 3. Validating whether PDF file has been successfully downloaded in the system at given path:: Test Case/JIRA ID: SMAB-T206
	 * 4. Deleting the file once verification is done
	 */
	@Test(description = "SMAB-T206: Verifying download functionality for PDF files", dataProvider = "loginBusinessAndPrincipalUsers", groups = {"regression","BPPTrend"}, dataProviderClass = DataProviders.class)
	public void verify_BppTrend_DownloadBppTrendPdfFile(String loginUser) throws Exception {		
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
		
		//Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder();
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or Principal User)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);

		//Step3: Selecting role year from drop down
		String rollYear = CONFIG.getProperty("rollYear");
		objPage.waitForElementToBeClickable(objBppTrnPg.rollYearDropdown, 30);
		objBppTrnPg.Click(objBppTrnPg.rollYearDropdown);
		objBppTrnPg.clickOnGivenRollYear(rollYear);
		objBppTrnPg.Click(objBppTrnPg.selectRollYearButton);
		
		//Step4: Validating presence of Download buttons and clicking then sequentially		
		boolean isDownloadBtnDisplayed = objBppTrnPg.isDownloadBtnVisible(20);
		softAssert.assertTrue(isDownloadBtnDisplayed, "SMAB-T206: Download button is visible");

		//Step5: Downloading PDF file by clicking Download button
		objBppTrnPg.clickDownloadBtn();
			
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

		//Step7: Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder();
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing following once all tables are APPROVED:
	 * 1. Downloading the PDF file from BPP Trend Setup page:: Test Case/JIRA ID: SMAB-T206
	 * 2. Validating whether PDF file has been successfully downloaded in the system at given path:: Test Case/JIRA ID: SMAB-T206
	 * 3. Deleting the file once verification is done
	 */
	@Test(description = "SMAB-T206: Verifying download functionality for PDF file from Bpp Trend Setup page", dataProvider = "loginBusinessAndPrincipalUsers", groups = {"regression","BPPTrend"}, dataProviderClass = DataProviders.class)
	public void verify_BppTrend_DownloadBppTrendPdfFile_From_BppTrendSetupPage(String loginUser) throws Exception {		
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
		
		//Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder();
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or Principal User)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		int year = Integer.parseInt(rollYear);
		year = year - 1;
		String valueToSearch = year + ".pdf";
		objBppTrnPg.waitForElementToBeClickable(objApasGenericPage.globalSearchListEditBox, 10);
		objApasGenericPage.searchAndSelectOptionFromDropDown(objApasGenericPage.globalSearchListEditBox, valueToSearch);
		
		//Step3: Clicking download button
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking download button");
		objBppTrnPg.Click(objBppTrnPg.locateElement("//span[text() = 'Download']", 10));
		Thread.sleep(5000);
		
		//Step4: Validating whether files have been downloaded successfully in the download directory 
		String pdfFileName = CONFIG.getProperty("donwloadedPdfFileName").toUpperCase();
		boolean isPdfDownloaded = false;
	
		ExtentTestManager.getTest().log(LogStatus.INFO, "Locating the files in local directory.");
		List<String> filesToDelete = new ArrayList<String>();
		List<String> downloadedFilesList = objBppTrnPg.checkFactorFilesInDownloadFolder();
		if(downloadedFilesList.size() == 1 && downloadedFilesList.get(0).contains(pdfFileName)) {
			String fileName = downloadedFilesList.get(0);
			filesToDelete.add(fileName);
			isPdfDownloaded = true;
		}
		softAssert.assertTrue(isPdfDownloaded, "SMAB-T207: PDF file downloaded with "+ loginUser +" user successfully");
		
		//Step5: Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder();
				
		softAssert.assertAll();
		objBppTrnPg.javascriptClick(objBppTrnPg.locateElement("//button[@title = 'Close']", 10));
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing following once all tables are APPROVED:
	 * 1. Validating unavailability of buttons to download composite and valuation excel file from BPP Trend Setup:: Test Case/JIRA ID: SMAB-T312
	 */
	@Test(description = "SMAB-T312: Verifying unaailablity of buttons to download valuation and composite excel files from Bpp Trend Setup page", dataProvider = "loginBusinessAdmin", groups = {"regression","BPPTrend"}, dataProviderClass = DataProviders.class)
	public void verify_BppTrend_Download_CompositeAndValuation_ExcelFiles_From_BppTrendSetupPage(String loginUser) throws Exception {		
		//Resetting the composite factor tables status to Not Calculated
		List<String> compositeFactorTablesToReset = Arrays.asList(CONFIG.getProperty("compositeFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(compositeFactorTablesToReset, "Approved", rollYear);

		//Resetting the valuation factor tables status to Yet to be submitted
		List<String> valuationFactorTablesToReset = Arrays.asList(CONFIG.getProperty("valuationFactorTablesOnBppSetupPage").split(","));
		objBppTrnPg.resetTablesStatusForGivenRollYear(valuationFactorTablesToReset, "Approved", rollYear);
		
		//Deleting downloaded files from download directory
		objBppTrnPg.deleteFactorFilesFromDownloadFolder();
		
		//Step1: Login to the APAS application using the credentials passed through data provider (Business administrator or Principal User)
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
		int year = Integer.parseInt(rollYear);
		year = year - 1;
		objBppTrnPg.clickOnEntryNameInGrid(Integer.toString(year));
		
		//Step4: Clicking on view all option to navigate to all files grid
		boolean isExportCompositeBtnDisplayed = objBppTrnPg.isExportCompositeFactorsBtnVisible(10);
		softAssert.assertTrue(!isExportCompositeBtnDisplayed, "SMAB-T312: Export Composite Factors button is not visible of Bpp Trend Setup screen");
		boolean isExportValuationBtnDisplayed = objBppTrnPg.isExportValuationFactorsBtnVisible(10);
		softAssert.assertTrue(!isExportValuationBtnDisplayed, "SMAB-T312: Export Valuation Factors button is not visible of Bpp Trend Setup screen");
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

}