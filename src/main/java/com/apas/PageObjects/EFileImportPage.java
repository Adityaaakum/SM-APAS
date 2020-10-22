package com.apas.PageObjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.ExcelUtils;

import com.apas.Utils.SalesforceAPI;
import com.apas.config.modules;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.apas.Reports.ExtentTestManager;

import com.apas.generic.ApasGenericFunctions;

import com.relevantcodes.extentreports.LogStatus;

public class EFileImportPage extends Page {
    Page objPage;
    SalesforceAPI salesforceAPI = new SalesforceAPI();
    ApasGenericFunctions objApasGenericFunctions;

    public EFileImportPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
        objPage = new Page(driver);
        objApasGenericFunctions = new ApasGenericFunctions(driver);
    }

    @FindBy(xpath = "//one-app-nav-bar-item-root//a[@title = 'E-File Import Tool']")
    public WebElement efileImportToolLabel;

    @FindBy(xpath = "//*[@name='docType']")
    public WebElement fileTypedropdown;

    @FindBy(xpath = "//*[@class='warning']//h2[contains(.,'This is already in In Progress')]")
    public WebElement fileAlreadyInProgressMsg;

    @FindBy(xpath = "//lightning-spinner")
    public WebElement spinner;

    public String xpathSpinner = "//lightning-spinner";

    @FindBy(xpath = "//*[@name='source']")
    public WebElement sourceDropdown;

    @FindBy(xpath = "//*[@name='source']/parent::div//following-sibling::div[@role='listbox']//lightning-base-combobox-item/..")
    public WebElement sourceDropdownOptions;

    @FindBy(xpath = "//button[@title='Next']")
    public WebElement nextButton;

    @FindBy(xpath = "//*[@name='freq']")
    public WebElement periodDropdown;

    @FindBy(xpath = "//button[contains(.,'Confirm')]")
    public WebElement confirmButton;

    @FindBy(xpath = "//button[contains(.,'Cancel')]")
    public WebElement cancelButton;

    @FindBy(xpath = "//button[text()='Continue']")
    public WebElement continueButton;

    @FindBy(xpath = "//div[@class='warning']")
    public WebElement warning;

    @FindBy(xpath = "//*[@data-key='upload']")
    public WebElement uploadFilebutton;

    @FindBy(xpath = "//Input[@name='Upload Data CSV']")
    public WebElement uploadFileInputBox;

    @FindBy(xpath = "//button//span[contains(.,'Done')]")
    public WebElement doneButton;

    @FindBy(xpath = "//tr[1]/td[5]//div")
    public WebElement status;

    @FindBy(xpath = "(//button[@title='Preview'])[1]")
    public WebElement viewLink;

    @FindBy(xpath = "(//td[@data-label='Uploaded File']//a)[1]")
    public WebElement fileLink;

    @FindBy(xpath = "//span[contains(@title,'ERROR ROWS')]")
    public WebElement errorRowSection;

    @FindBy(xpath = "//lightning-tab[contains(@class,'slds-show')]//button[contains(.,'ERROR ROWS')]")
    public WebElement errorRowSectionExpandButton;

    @FindBy(xpath = "//span[contains(@title,'IMPORTED ROWS')]")
    public WebElement importedRowSection;

    @FindBy(xpath = "//button[contains(.,'IMPORTED ROWS')]")
    public WebElement importedRowSectionExpandButton;

    @FindBy(xpath = "(//td[@data-label='Status'])[1]")
    public WebElement statusImportedFile;

    @FindBy(xpath = "(//td[@data-label='Number of Tries'])[1]")
    public WebElement numberOfTimesTriedRetried;

    @FindBy(xpath = "(//td[@data-label='Import Count'])[1]")
    public WebElement totalRecordsImportedFile;

    @FindBy(xpath = "(//td[@data-label='File Count'])[1]")
    public WebElement totalRecordsInFile;

    @FindBy(xpath = "//a[contains(@data-label,'BuildingPermits')]")
    public WebElement buildingPermitLabel;

    @FindBy(xpath = "//input[@class='datatable-select-all'][@type='checkbox']/..//span[@class='slds-checkbox_faux']")
    public WebElement selectAllCheckBox;

    @FindBy(xpath = "//lightning-tab[contains(@class,'slds-show')]//td//span[@class='slds-checkbox_faux']")
    public WebElement rowSelectCheckBox;

    @FindBy(xpath = "//button[text()='Approve']")
    public WebElement approveButton;

    @FindBy(xpath = "//button[text()='Discard']")
    public WebElement discardButton;

    @FindBy(xpath = "//button[text()='Revert']")
    public WebElement revertButton;

    @FindBy(xpath = "//button[text()='Retry']")
    public WebElement retryButton;

    @FindBy(xpath = "//button[text()='Close']")
    public WebElement closeButton;

    @FindBy(xpath = "//*[text()='Thank you! Imported records has been approved successfully.']")
    public WebElement efileRecordsApproveSuccessMessage;

    @FindBy(xpath = "//*[text()='Thank you! All records has been reverted successfully.']")
    public WebElement revertSuccessMessage;

    @FindBy(xpath = "//span[@title='ERROR_MESSAGE']")
    public WebElement editErrorMessage;

    @FindBy(xpath = "//*[@data-label='PERMITNO'][@role='gridcell']//button")
    public WebElement editButtonValue;

    @FindBy(xpath = "//input[@class='slds-input']")
    public WebElement editButtonInput;

    @FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//table//tbody//tr")
    public List<WebElement> historyListItems;

    @FindBy(xpath = "//span[contains(@title,'IMPORTED ROWS')]")
    public WebElement successRowSection;

    @FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//table//tbody//td[contains(.,'Reverted')]/following-sibling::td[1]//span[not(contains(.,'View'))]")
    public List<WebElement> revertRecordsViewLinkNotVisible;

    @FindBy(xpath = "//*[contains(@id,'help')]")
    public WebElement invalidFileErrorMsg;

    @FindBy(xpath = "//*[@class='warning']//h2")
    public WebElement fileAlreadyApprovedMsg;

    @FindBy(xpath = "//div[@class='error']//h2")
    public WebElement duplicateFileMsg;

    @FindBy(xpath = "//ul[@role='tablist']//li//span[@class='slds-tabs__left-icon']/parent::a")
    public List<WebElement> tablesWithErrorRecords;

    @FindBy(xpath = "//label[contains(.,'Select All')]/preceding-sibling::input[@type='checkbox']/parent::span")
    public WebElement discardAllCheckbox;

    @FindBy(xpath = "//button[contains(.,'Continue')]")
    public WebElement discardContinue;

    @FindBy(xpath = "//button[contains(.,'Cancel')]")
    public WebElement discardCancel;

    @FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(.,'ERROR ROWS')]")
    public WebElement errorRowCount;

    @FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(.,'IMPORTED ROWS')]")
    public WebElement importedRowCount;

    @FindBy(xpath = "(//td[@data-label='Discard Count'])[1]")
    public WebElement disacrdCount;

    @FindBy(xpath = "//button[contains(.,'More...')]")
    public WebElement moreButton;

    @FindBy(xpath = "(//td[@data-label='Action'])[1]//button[@title='Preview' and text()='View']")
    public WebElement viewLinkRecord;

    @FindBy(xpath = "(//td[@data-label='Action'])[2]//button[@title='Preview' and text()='View']")
    public WebElement viewLinkForPreviousImport;

    @FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//table//tbody//tr//input/parent::span")
    public List<WebElement> errorRecordsRows;

    @FindBy(xpath = "//input[@name='efileName']")
    public WebElement fileNameInputBox;

    @FindBy(xpath = "//section[@role='dialog']//button[text()='Next']")
    public WebElement fileNameNext;

    @FindBy(xpath = "//lightning-input//div//input[@type = 'text']")
    public WebElement inputBoxOnImportPage;

    @FindBy(xpath = "//section[@role='dialog']//*[@role='alert']")
    public WebElement errorInFileNameMsg;

    @FindBy(xpath = "//button[@title='Source Details']//*[@data-key='success']")
    public WebElement sourceDetails;

    @FindBy(xpath = "//section[@role='dialog']//div[@data-dropdown-element='true']//span[@class='slds-media__body']")
    public WebElement periodFirstDropDownValue;

    @FindBy(xpath = "(//td[@data-label='Error Count'])[1]")
    public WebElement errorRecordsImportedFile;

    @FindBy(xpath = "//span[contains(.,'Upload Files')]")
    public WebElement uploadFilesButton;

    public String xpathFileTypedrpdwn = "//*[@name='docType']";

    /**
     * This method will select the file type and source from E-File Import Tool page
     *
     * @param fileType : Value from File Type Drop Down
     * @param source:  Value from source drop down
     * @throws Exception
     */
    public void selectFileAndSource(String fileType, String source) throws Exception {
        ReportLogger.INFO("Selecting File type :" + fileType + " and Source :" + source);
        objPage.waitUntilPageisReady(driver);
        objPage.waitUntilElementIsPresent(xpathFileTypedrpdwn, 60);
        objPage.waitForElementToBeClickable(fileTypedropdown, 30);
        Click(fileTypedropdown);
        Thread.sleep(2000);
        javascriptClick(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + fileType + "')]")));
        Click(sourceDropdown);
        WebElement webElementSourceOption = driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + source + "')]"));
        scrollToElement(webElementSourceOption);
        javascriptClick(webElementSourceOption);
    }

    /**
     * This method will upload the file on Efile Import module
     *
     * @param fileType          : Value from File Type Drop Down
     * @param source:           Value from source drop down
     * @param period:           Period for which the file needs to be uploaded
     * @param absoluteFilePath: Absoulte Path of the file with the file name
     */
    public void uploadFileOnEfileIntake(String fileType, String source, String period, String absoluteFilePath) throws Exception {
        ReportLogger.INFO("Uploading " + absoluteFilePath + " file");
        selectFileAndSource(fileType, source);
        objPage.waitUntilElementDisplayed(nextButton, 15);
        objPage.scrollToTop();
        objPage.Click(nextButton);
        objPage.Click(periodDropdown);
        objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")));
        objPage.Click(confirmButton);
        Thread.sleep(2000);
        uploadFileInputBox.sendKeys(absoluteFilePath);
        Thread.sleep(2000);
        objPage.waitForElementToBeClickable(doneButton);
        Thread.sleep(2000);
        objPage.Click(doneButton);
        waitForElementToBeClickable(statusImportedFile, 20);
        objPage.scrollToBottom();
        objPage.waitForElementTextToBe(statusImportedFile, "In Progress", 120);
    }

    /**
     * This method will upload the file on Efile Import module
     *
     * @param fileType          : Value from File Type Drop Down
     * @param source:           Value from source drop down
     * @param period:           Period for which the file needs to be uploaded
     * @param absoluteFilePath: Absoulte Path of the file with the file name
     */
    public void uploadInvalidFormatFileOnEfileIntake(String fileType, String source, String period, String absoluteFilePath) throws Exception {
        ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading " + absoluteFilePath + " file");
        selectFileAndSource(fileType, source);
        objPage.waitUntilElementDisplayed(nextButton, 10);
        objPage.scrollToTop();
        objPage.Click(nextButton);
        objPage.Click(periodDropdown);
        objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")));
        objPage.Click(confirmButton);
        Thread.sleep(2000);
        ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading " + absoluteFilePath + " on Efile Import Tool");
        uploadFileInputBox.sendKeys(absoluteFilePath);
        Thread.sleep(2000);
    }

    /**
     * This method will expand the section provided in webelement passed in the parameter
     *
     * @param element : section to be expanded
     */
    public void expandSection(WebElement element) throws IOException {
        String ariaExpanded = getAttributeValue(element, "aria-expanded");
        if (ariaExpanded.equals("false"))
            Click(element);
    }

    /**
     * This method will collapse the section provided in webelement passed in the parameter
     *
     * @param element : section to be expanded
     */
    public void collapseSection(WebElement element) throws IOException {
        String ariaExpanded = getAttributeValue(element, "aria-expanded");
        if (ariaExpanded.equals("true"))
            Click(element);
    }


    /**
     * This method will return the error message from error grid with work dercription having the value passed in parameter
     *
     * @param stringValueInRow : Value of the work description
     */
    public String getErrorMessageFromErrorGrid(String stringValueInRow) {
        String xpath = "(//lightning-tab[contains(@class,'slds-show')]//table)[1]//tbody//tr//lightning-base-formatted-text[text()='" + stringValueInRow + "']//..//..//..//..//parent::tr//td[@data-label='Error Message'] | "
                + "(//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table)[1]//tbody//tr[contains(.,'" + stringValueInRow + "')]//th[@data-label='ERROR_MESSAGE']";
        if (verifyElementExists(xpath))
            return getElementText(driver.findElement(By.xpath(xpath)));
        else
            return "";
    }



    /**
     * This method will import the file on Efile Import module
     *
     * @param fileType          : Value from File Type Drop Down
     * @param source:           Value from source drop down
     * @param filename:         Period or name of the file for which the file needs to be uploaded
     * @param absoluteFilePath: Absolute Path of the file with the file name
     */
    public void importFileOnEfileIntake(String fileType, String source, String filename, String absoluteFilePath) throws Exception {
        objApasGenericFunctions.searchModule(modules.EFILE_INTAKE);
        uploadFileOnEfileIntakeBP(fileType,source,filename,absoluteFilePath);
        ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
        objPage.waitForElementTextToBe(statusImportedFile, "Imported", 120);
    }

        /**
         * This method will upload the file on Efile Import module
         *
         * @param fileType          : Value from File Type Drop Down
         * @param source:           Value from source drop down
         * @param filename:         Period for which the file needs to be uploaded
         * @param absoluteFilePath: Absoulte Path of the file with the file name
         */
    public void uploadFileOnEfileIntakeBP(String fileType, String source, String filename, String absoluteFilePath) throws Exception {
        ReportLogger.INFO("Uploading " + absoluteFilePath + " file");
        selectFileAndSource(fileType, source);
        objPage.waitUntilElementDisplayed(nextButton, 15);
        objPage.scrollToElement(nextButton);
        objPage.Click(nextButton);
        objPage.enter(fileNameInputBox, filename);
        objPage.Click(fileNameNext);
        objPage.waitForElementToBeClickable(confirmButton, 15);
        objPage.Click(confirmButton);
        Thread.sleep(2000);
        uploadFileInputBox.sendKeys(absoluteFilePath);
        Thread.sleep(2000);
        objPage.waitForElementToBeClickable(doneButton);
        Thread.sleep(2000);
        objPage.Click(doneButton);
        waitForElementToBeClickable(statusImportedFile, 20);
        objPage.scrollToBottom();
        objPage.waitForElementTextToBe(statusImportedFile, "In Progress", 120);
    }

    /**
     * It retrieves the count of total rows in error or imported rows section
     *
     * @param: section name : Error or Imported
     * @return: Returns the count of imported rows
     * @throws: Throws Exception
     */
    public String getCountOfRowsFromErrorOrImportedRowsSection(String sectionName) throws Exception {
        String xpathErrorSection = "//lightning-tab[contains(@class,'slds-show')]//span[contains(@title,'" + sectionName + " ROWS')]";
        String countOfErrorRows = (getElementText(locateElement(xpathErrorSection, 30)).split(":"))[1].trim();
        return countOfErrorRows;
    }

    /**
     * It checks whether the error or imported rows section is displayed
     *
     * @param: section name : Error or Imported
     * @return: Returns the status of error rows section based on it visibility as true / false
     * @throws: Throws Exception
     */
    public boolean checkPresenceOfErrorOrImportedRowsSection(String sectionName) throws Exception {
        String xpathErrorSection = "//lightning-tab[contains(@class,'slds-show')]//span[contains(@title,'" + sectionName + " ROWS')]";
        return locateElement(xpathErrorSection, 60).isDisplayed();
    }

    /**
     * It selects All Error Rows and discard it
     *
     * @throws: Throws Exception
     */
    public void discardErrorRecords() throws Exception {
        discardErrorRecords("All");
    }

    /**
     * It selects an individual Error Row or All Error Rows based on parameter passed and discard it
     *
     * @param: Row Number to be selected
     * @throws: Throws Exception
     */
    public void discardErrorRecords(String errorRow) throws Exception {
        //Step 1: Select checkbox corresponding to Row Number for Error Records
        String xpathSelectErrorRecordCheckBox;
        if (errorRow.equalsIgnoreCase("All")) {
            xpathSelectErrorRecordCheckBox = "//lightning-tab[contains(@class,'slds-show')]//thead//tr//span[@class='slds-checkbox_faux']";
        } else
            xpathSelectErrorRecordCheckBox = "//lightning-tab[contains(@class,'slds-show')]//td//span[contains(text(),'" + errorRow + "')]//preceding-sibling::span";

        WebElement selectErrorRecordCheckBox = locateElement(xpathSelectErrorRecordCheckBox, 30);
        Click(selectErrorRecordCheckBox);

        //Step 2: Select Discard button
        String xpathDiscardButton = "//lightning-tab[contains(@class,'slds-show')]//button[text()='Discard']";
        WebElement discardButton = locateElement(xpathDiscardButton, 30);
        waitForElementToBeClickable(discardButton, 10);
        Click(discardButton);

        //Step 3: Select Continue button displayed on warning pop up
        String xpathContinueButton = "//button[text()='Continue']";
        WebElement continueButton = locateElement(xpathContinueButton, 30);
        waitForElementToBeClickable(continueButton, 10);
        clickAction(continueButton);
        Thread.sleep(4000);
    }

    /**
     * Description : Reads given excel file to retrieve total rows in each sheet and count rows specific to factor tables into map.
     *
     * @param filePath:  Takes the path of the XLSX workbook
     * @param sheetName: Sheet from which row count is returned
     * @return int: Return the row count
     * @throws Exception
     **/
    public int getRowCountSpecificToTable(String filePath, String sheetName) throws Exception {
        int rowCount = ExcelUtils.getRowCountFromExcelSheet(filePath, sheetName);
        if (sheetName.contains("Agricultural ME Good Factors")) {
            rowCount = rowCount * 2;
        } else if (sheetName.contains("M&E Good Factors")) {
            rowCount = 482;
        } else if (sheetName.contains("Copier Val Factors") || sheetName.contains("Litho Val Factors") || sheetName.contains("Set-Top Box Val Factors") || sheetName.contains("Elec. Slot Machines Val Factors") || sheetName.contains("Mech. Slot Machines Val Factors")) {
            rowCount = 41; //In Valuation Factors file if data for some years is present, the total imported records will always be 41. Last present record value will be copied for past years i.e. 2020 to 1980 for 2021 roll year
        } else if (sheetName.contains("Computer Val Factors") || sheetName.contains("Semiconductor Val Factors")) {
            rowCount = 41 * 2;
        } else if (sheetName.contains("Biopharmaceutical Val Factors")) {
            rowCount = 41 * 4;
        }

        return rowCount;
    }

    /**
     * This method will return the column value from error grid with Error Message and Column Name having the values passed in parameter
     *
     * @param ErrorMsg   : Error Message corresponding to which column value is required
     * @param columnName : Column Name for which value is required
     */
    public String getColumnValueFromErrorGrid(String ErrorMsg, String columnName) {
        String xpath = "(//lightning-tab[contains(@class,'slds-show')]//table)[1]//tbody//tr[contains(.,'" + ErrorMsg + "')]//td[@data-label='" + columnName + "']//lightning-base-formatted-text";
        if (verifyElementExists(xpath)) {
            WebElement ele = driver.findElement(By.xpath(xpath));
            return ele.getText();
        } else
            return "";
    }

    /**
     * This method will revert the status of already imported and approved building permit files
     * @param fileSource : building permit file source to be reverted
     */
    public void revertImportedAndApprovedFiles(String fileSource) {
        //Reverting the Approved Import logs if any in the system
        ReportLogger.INFO("Reverting the already approved and imported " + fileSource + " files");
        String query = "Select id From E_File_Import_Log__c where File_type__c = 'Building Permit' and File_Source__C = '" + fileSource + "' and Import_Period__C='Adhoc' and Status__c in ('Approved','Imported') ";
        salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Reverted");
    }

    /**
     * This method will revert the imported file records currently displayed on UI
     */
    public void revertImportedFile() throws Exception {
        ReportLogger.INFO("Reverting the imported file linked with the work item");
        objPage.waitForElementToBeVisible(revertButton, 20);
        objPage.Click(revertButton);
        objPage.Click(continueButton);
        objPage.waitForElementToBeVisible(revertSuccessMessage, 20);
        ReportLogger.INFO("Imported file reverted");
    }

    /**
     * This method will approve the imported file records currently displayed on UI
     */
    public void approveImportedFile() throws Exception {
        ReportLogger.INFO("Approving the imported file linked with the work item");
        objPage.waitForElementToBeVisible(approveButton, 20);
        objPage.Click(approveButton);
        objPage.waitForElementToBeVisible(efileRecordsApproveSuccessMessage, 20);
        ReportLogger.INFO("Imported file approved");
    }

    /**
     * This method will upload the file after File Type and Source are already selected
     * @param absoluteFilePath: Absoulte Path of the file with the file name
     */
    public void uploadFile(String absoluteFilePath) throws Exception {
        objPage.waitForElementToBeVisible(uploadFilesButton,120);
        uploadFileInputBox.sendKeys(absoluteFilePath);
        Thread.sleep(2000);
        objPage.waitForElementToBeClickable(doneButton);
        Thread.sleep(2000);
        objPage.Click(doneButton);
        waitForElementToBeClickable(statusImportedFile, 20);
        objPage.scrollToBottom();
        objPage.waitForElementTextToBe(statusImportedFile, "In Progress", 120);
        ReportLogger.INFO("Waiting for Status of the imported file to be converted to Imported");
        objPage.waitForElementTextToBe(statusImportedFile, "Imported", 360);
    }

    /**
     * This method will revert the 'Imported & Approved' Logs and delete the existing data from system before importing files
     * @param rollYear: Roll Year for which data needs to be deleted
     */
    public void deleteImportedRecords(String fileType, String fileSource, String rollYear){
        String query = "Select id From E_File_Import_Log__c where File_type__c = '" + fileType + "' and Import_Period__C='" + rollYear + "' and File_Source__C like '" + fileSource + "' and (Status__c = 'Imported' Or Status__c = 'Approved')";
        salesforceAPI.update("E_File_Import_Log__c", query, "Status__c", "Reverted");
        salesforceAPI.deleteBPPTrendRollYearData(rollYear);
    }
}