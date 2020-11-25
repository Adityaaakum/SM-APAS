package com.apas.Tests.BuildingPermit;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.*;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.ExcelUtils;
import com.apas.Utils.Util;
import com.apas.config.BPFileSource;
import com.apas.config.fileTypes;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class BuildingPermit_WorkItems_Test extends TestBase {

    RemoteWebDriver driver;
    Page objPage;
    ApasGenericFunctions objApasGenericFunctions;
    BuildingPermitPage objBuildingPermitPage;
    WorkItemHomePage objWorkItemHomePage;
    SoftAssertion softAssert = new SoftAssertion();
    Util objUtil = new Util();
    EFileImportPage objEfileImportPage;
    ReportsPage objReportsPage;

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();

        objPage = new Page(driver);
        objBuildingPermitPage = new BuildingPermitPage(driver);
        objApasGenericFunctions = new ApasGenericFunctions(driver);
        objEfileImportPage = new EFileImportPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objReportsPage = new ReportsPage(driver);
    }

    /**
     * This test case is to validate work item creation functionality and the work item flow after file is approved
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and RP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1890, SMAB-T1892, SMAB-T1900, SMAB-T1901, SMAB-T1902,SMAB-T1903: Validation for work item generation after building permit file import and approve", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BP"}, alwaysRun = true)
    public void BuildingPermit_WorkItemAfterImportAndApprove(String loginUser) throws Exception {

        String downloadLocation = testdata.DOWNLOAD_FOLDER;
        ReportLogger.INFO("Download location : " + downloadLocation);

        //Deleting all the previously downloaded files
        objApasGenericFunctions.deleteFilesFromFolder(downloadLocation);

        //Creating a temporary copy of the file to be processed to create unique name
        String timeStamp = objUtil.getCurrentDate("ddhhmmss");
        String sourceFileName = "MultipleValidRecord.xlsx";

        String fileNameWithoutExtension = "MultipleValidRecord" + timeStamp;
        String fileName = fileNameWithoutExtension + ".xlsx";

        String sourceFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_SAN_MATEO + sourceFileName;
        String destFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + fileName;

        FileUtils.copyFile(new File(sourceFile), new File(destFile));

        //Step1: Reverting the Approved Import logs if any in the system
        objEfileImportPage.revertImportedAndApprovedFiles(BPFileSource.ATHERTON);

        //Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
        objApasGenericFunctions.login(loginUser);

        //Step3: Importing the Building Permit file having all correct records
        objEfileImportPage.importFileOnEfileIntake(fileTypes.BUILDING_PERMITS, BPFileSource.SAN_MATEO, fileName, destFile);

        //Stpe4: Open the Work Item Home Page
        objApasGenericFunctions.searchModule(modules.HOME);
        driver.navigate().refresh();

        //Step5: "Import Review" Work Item generation validation after file is imported
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importReviewWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("Building Permit - Review - " + fileNameWithoutExtension)).count();
        int importReviewRowNumber = InPoolWorkItems.get("Request Type").indexOf("Building Permit - Review - " + fileNameWithoutExtension);
        String importReviewWorkItem = InPoolWorkItems.get("Work Item Number").get(importReviewRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importReviewRowNumber), "Building Permit - Review - " + fileNameWithoutExtension, "SMAB-T1890: File Name and Review Work Item Name validation in the imported review work item");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importReviewRowNumber), "RP Admin", "SMAB-T1890: Imported review work item pool name validation");
        softAssert.assertEquals(importReviewWorkItemCount, 1, "SMAB-T1890: Imported review work item count validation");

        //Step6: Accepting the work item and approving the file
        objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objPage.scrollToBottom();
        objWorkItemHomePage.openRelatedActionRecord(importReviewWorkItem);
        String parentWindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentWindow);
        objEfileImportPage.approveImportedFile();
        driver.switchTo().window(parentWindow);

        //Step7: Open Home Page
        objApasGenericFunctions.searchModule(modules.HOME);
        driver.navigate().refresh();

        //Step8: Validation for Import Review work item moved to completed status
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importReviewWorkItem), "SMAB-T1890: Validation that import review work item moved to Completed status after import file is approved");

        //Step9: Validation for generation of "Final Review" Work Item
        InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int finalReviewWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("Building Permit - Final Review - " + fileNameWithoutExtension)).count();
        int finalReviewRowNumber = InPoolWorkItems.get("Request Type").indexOf("Building Permit - Final Review - " + fileNameWithoutExtension);
        String finalReviewWorkItem = InPoolWorkItems.get("Work Item Number").get(finalReviewRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(finalReviewRowNumber), "Building Permit - Final Review - " + fileNameWithoutExtension, "SMAB-T1900: File Name and Final Review Work Item Name validation in the final review work item");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(finalReviewRowNumber), "RP Admin", "SMAB-T1900: Final review work item pool name validation");
        softAssert.assertEquals(finalReviewWorkItemCount, 1, "SMAB-T1900: Final review work item count validation");

        //Step10: Accept the work item
        objWorkItemHomePage.acceptWorkItem(finalReviewWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objPage.scrollToBottom();
        objWorkItemHomePage.openWorkItem(finalReviewWorkItem);

        //Step11: Validation that E-File logs are linked to the work item and the status of the E-logs is Approved
        softAssert.assertTrue(objPage.verifyElementExists(objWorkItemHomePage.linkedItemEFileIntakeLogs), "SMAB-T1900: Validation that efile logs are linked with work item");
        HashMap<String, ArrayList<String>> efileImportLogsData = objApasGenericFunctions.getGridDataInHashMap();
        softAssert.assertEquals(efileImportLogsData.get("Status").get(0), "Approved", "SMAB-T1900: Validation that status of the efile logs linked with work item is reverted");

        //Step12: Validation the building permit records are linked with the work item
        objPage.scrollToBottom();
        softAssert.assertTrue(objPage.verifyElementExists(objWorkItemHomePage.relatedBuildingPermits), "SMAB-T1900: Validation that Building Permit Records are linked with work item");
        HashMap<String, ArrayList<String>> buildingPermitRecordsData = objApasGenericFunctions.getGridDataInHashMap(2);
        softAssert.assertEquals(buildingPermitRecordsData.get("Building Permit Number").size(), "3", "SMAB-T1900: Building Permit Records Count validation");

        //Step13: Update the building permit record
        objBuildingPermitPage.openBuildingPermit(buildingPermitRecordsData.get("Building Permit Number").get(0));
        objPage.scrollToBottom();
        objApasGenericFunctions.editAndInputFieldData("Estimated Project Value",timeStamp);
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Estimated Project Value").replace(",",""), "$" + timeStamp, "SMAB-T1901: Validation that updated valueis reflected in the building permit");

        //Step14: Open Home Page
        objApasGenericFunctions.searchModule(modules.HOME);
        driver.navigate().refresh();

        //Step15: Open the work item
        objPage.scrollToBottom();
        objWorkItemHomePage.openRelatedActionRecord(finalReviewWorkItem);

        //Step16: Report data validation for linked building permit records
        Thread.sleep(5000);
        parentWindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentWindow);
        //Switching the frame as the generated report is in different frame
        driver.switchTo().frame(0);
        objPage.Click(objReportsPage.arrowButton);
        objPage.Click(objReportsPage.linkExport);
        //Switching back to parent frame to export the report
        driver.switchTo().parentFrame();
        objPage.Click(objReportsPage.formattedExportLabel);
        objPage.Click(objReportsPage.exportButton);
        //Added this wait to allow the file to download
        Thread.sleep(10000);
        File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];

        //Step17: Columns validation in exported report
        HashMap<String, ArrayList<String>> hashMapExcelData = ExcelUtils.getExcelSheetData(downloadedFile.getAbsolutePath(),0,10,1);
        String expectedColumnsInExportedExcel = "[Created Date, Permit City Code, Building Permit Number, APN: APN, Issue Date(YYYYMMDD), Completion Date(YYYYMMDD), Reissue, City APN, City Strat Code, Building Permit Fee, Work Description, Square Footage, Estimated Project Value, Application Name, Owner Name, Owner Address Line 1, Owner Address Line 2, Owner Address Line 3, Owner State, Owner Zip Code, Owner Phone Number, Contractor Name, Contractor Phone]";
        softAssert.assertEquals(hashMapExcelData.keySet().toString(),expectedColumnsInExportedExcel,"SMAB-T1900: Columns Validation in downloaded building permit report");

        driver.switchTo().window(parentWindow);

        //Step18: Complete the final review work item
        String successMessageText = objWorkItemHomePage.completeWorkItem();
        softAssert.assertEquals(successMessageText, "success\nStatus changed successfully.\nClose", "SMAB-T1903,SMAB-T1902,SMAB-T1901: Validation that status of the efile logs linked with work item is reverted");

        //Step19: Open Home Page
        objApasGenericFunctions.searchModule(modules.HOME);
        driver.navigate().refresh();

        //Step20: Close "Final Review" Work Item
        completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(finalReviewWorkItem), "SMAB-T1903: Validation that Final Review work item moved to Completed status after Final Review work item is manually completed");

        //Logout at the end of the test
        objApasGenericFunctions.logout();
    }


    /**
     * This test case is to validate work item creation functionality and the work item flow after file is reverted
     * Pre-Requisite: Work Pool, Work Item Configuration, Routing Assignment and RP-WI Management permission configuration should exist
     **/
    @Test(description = "SMAB-T1890, SMAB-T1899: Validation for work item generation after building permit file import and revert", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"smoke", "regression", "Work_Item_BP"}, alwaysRun = true)
    public void BuildingPermit_WorkItemAfterImportAndRevert(String loginUser) throws Exception {

        //Creating a temporary copy of the file to be processed to create unique name
        String timeStamp = objUtil.getCurrentDate("ddhhmmss");
        String sourceFileName = "SingleValidRecord_AT.txt";

        String fileNameWithoutExtension = "SingleValidRecord_AT_" + timeStamp;
        String fileName = fileNameWithoutExtension + ".txt";

        String sourceFile = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_ATHERTON + sourceFileName;
        String destFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + fileName;

        FileUtils.copyFile(new File(sourceFile), new File(destFile));

        //Step1: Reverting the Approved Import logs if any in the system
        objEfileImportPage.revertImportedAndApprovedFiles(BPFileSource.ATHERTON);

        //Step2: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
        objApasGenericFunctions.login(loginUser);

        //Step3: Importing the Building Permit file having all correct records
        objEfileImportPage.importFileOnEfileIntake(fileTypes.BUILDING_PERMITS, BPFileSource.ATHERTON, fileName, destFile);

        //Stpe4: Open the Work Item Home Page
        objApasGenericFunctions.searchModule(modules.HOME);
        driver.navigate().refresh();

        //Step5: "Import Review" Work Item generation validation after file is imported
        HashMap<String, ArrayList<String>> InPoolWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_IN_POOL);
        int importReviewWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.contains(fileNameWithoutExtension)).count();
        int importReviewRowNumber = InPoolWorkItems.get("Request Type").indexOf("Building Permit - Review - " + fileNameWithoutExtension);
        String importReviewWorkItem = InPoolWorkItems.get("Work Item Number").get(importReviewRowNumber);

        softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(importReviewRowNumber), "Building Permit - Review - " + fileNameWithoutExtension, "SMAB-T1890: File Name and Review Work Item Name validation in the imported review work item");
        softAssert.assertEquals(InPoolWorkItems.get("Work Pool Name").get(importReviewRowNumber), "RP Admin", "SMAB-T1890: Imported review work item pool name validation");
        softAssert.assertEquals(importReviewWorkItemCount, 1, "SMAB-T1890: Imported review work item count validation");

        //Step6: Accepting the work item
        objWorkItemHomePage.acceptWorkItem(importReviewWorkItem);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objPage.scrollToBottom();
        objWorkItemHomePage.openRelatedActionRecord(importReviewWorkItem);
        String parentWindow = driver.getWindowHandle();
        objPage.switchToNewWindow(parentWindow);
        objEfileImportPage.revertImportedFile();
        driver.switchTo().window(parentWindow);

        //Step7: Open Home Page
        objApasGenericFunctions.searchModule(modules.HOME);
        driver.navigate().refresh();

        //Step8: Validation for Import Review work item moved to completed status
        HashMap<String, ArrayList<String>> completedWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
        softAssert.assertTrue(completedWorkItems.get("Work Item Number").contains(importReviewWorkItem), "SMAB-T1890: Validation that import review work item moved to Completed status after import file is approved");

        //Step9: Opening the completed work item
        objPage.scrollToBottom();
        objWorkItemHomePage.openWorkItem(importReviewWorkItem);

        //Step10: Validation that E-File logs are linked to the work item and the status of the E-logs is reverted
        softAssert.assertTrue(objPage.verifyElementExists(objWorkItemHomePage.linkedItemEFileIntakeLogs), "SMAB-T1900: Validation that efile logs are linked with work item");
        HashMap<String, ArrayList<String>> efileImportLogsData = objApasGenericFunctions.getGridDataInHashMap();
        softAssert.assertEquals(efileImportLogsData.get("Status").get(0), "Reverted", "SMAB-T1900: Validation that status of the efile logs linked with work item is reverted");

        //Step11: Validating the status should be completed
        objPage.Click(objWorkItemHomePage.detailsTab);
        objPage.scrollToBottom();
        softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Status"), "Completed", "SMAB-T1890: Validation that status of the work item should be completed");

        //Logout at the end of the test
        objApasGenericFunctions.logout();

    }

}
