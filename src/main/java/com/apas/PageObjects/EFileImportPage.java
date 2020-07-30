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
	public EFileImportPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objPage=new Page(driver);
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

	@FindBy(xpath = "//button[contains(.,'ERROR ROWS')]")
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

	@FindBy(xpath = "//td//span[@class='slds-checkbox_faux']")
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
	
	public String xpathFileTypedrpdwn = "//*[@name='docType']";
	/**
	 * This method will select the file type and source from E-File Import Tool page
	 * @param fileType : Value from File Type Drop Down
	 * @param source: Value from source drop down
	 */
	public void selectFileAndSource(String fileType, String source) throws IOException, InterruptedException {
		System.out.println("File type is:" + fileType + " and Source is:" + source);
		Thread.sleep(3000);
		Click(fileTypedropdown);
		Thread.sleep(3000);
		Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + fileType + "')]")));
		Thread.sleep(3000);
		Click(sourceDropdown);
		Thread.sleep(3000);
		WebElement webElementSourceOption = driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + source + "')]"));
		scrollToElement(webElementSourceOption);
		Click(webElementSourceOption);
	}

	/**
	 * This method will upload the file on Efile Import module
	 * @param fileType : Value from File Type Drop Down
	 * @param source: Value from source drop down
	 * @param period: Period for which the file needs to be uploaded
	 * @param absoluteFilePath: Absoulte Path of the file with the file name
	 */
	public void uploadFileOnEfileIntake(String fileType, String source,String fileImport, String absoluteFilePath) throws Exception{
		ReportLogger.INFO("Uploading " +  absoluteFilePath + " file");
		selectFileAndSource(fileType, source);
		objPage.waitUntilElementDisplayed(nextButton, 15);
		objPage.scrollToTop();
		objPage.Click(nextButton);
		
		if(fileType.equals("BPP Trend Factors")) {
			
			objPage.Click(periodDropdown);
			objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + fileImport + "')]")));		
			
		}
		else if(fileType.equals("Building Permit")) {
			
			objPage.enter(fileNameInputBox, fileImport);
			objPage.Click(fileNameNext);
			
		}
		objPage.waitForElementToBeClickable(confirmButton, 15);
		objPage.Click(confirmButton);
		Thread.sleep(2000);
		uploadFileInputBox.sendKeys(absoluteFilePath);
		Thread.sleep(2000);
		objPage.waitForElementToBeClickable(doneButton);
		Thread.sleep(2000);
		objPage.Click(doneButton);
		waitForElementToBeClickable(statusImportedFile,20);
		objPage.waitForElementTextToBe(statusImportedFile, "In Progress", 120);
	}
	
	/**
	 * This method will upload the file on Efile Import module
	 * @param fileType : Value from File Type Drop Down
	 * @param source: Value from source drop down
	 * @param period: Period for which the file needs to be uploaded
	 * @param absoluteFilePath: Absoulte Path of the file with the file name
	 */
	public void uploadInvalidFormatFileOnEfileIntake(String fileType, String source,String period, String absoluteFilePath) throws Exception{
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading " +  absoluteFilePath + " file");
		selectFileAndSource(fileType, source);
		objPage.waitUntilElementDisplayed(nextButton, 10);
	
		objPage.scrollToTopOfPage();
		
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
	 * @param element : section to be expanded
	 */
	public void expandSection(WebElement element) throws IOException {
		String ariaExpanded = getAttributeValue(element,"aria-expanded");
		if (ariaExpanded.equals("false"))
			Click(element);
	}

	/**
	 * This method will collapse the section provided in webelement passed in the parameter
	 * @param element : section to be expanded
	 */
	public void collapseSection(WebElement element) throws IOException {
		String ariaExpanded = getAttributeValue(element,"aria-expanded");
		if (ariaExpanded.equals("true"))
			Click(element);
	}
	


	/**
	 * This method will return the error message from error grid with work dercription having the value passed in parameter
	 * @param stringValueInRow : Value of the work description
	 */
	public String getErrorMessageFromErrorGrid(String stringValueInRow){
		String xpath = "(//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table)[1]//tbody//tr[contains(.,'" + stringValueInRow + "')]//th[@data-label='ERROR_MESSAGE']";
		if (verifyElementExists(xpath))
			return getElementText(driver.findElement(By.xpath(xpath)));
		else
			return "";
	}

/*
	public void clickViewLinkForParameters(String user,String status) throws Exception{
		
		driver.findElements(By.xpath("//div[@class='windowViewMode-normal oneContent active lafPageHost']//table//tbody//th[contains(.,'"+user+"')]/following-sibling::td[contains(.,'"+status+"')]//following-sibling::td//span[contains(.,'View')]")).get(1).click();
		
		
		}*/
	

	/**
	 * This method will upload the file on Efile Import module
	 * @param fileType : Value from File Type Drop Down
	 * @param source: Value from source drop down
	 * @param filename: Period for which the file needs to be uploaded
	 * @param absoluteFilePath: Absoulte Path of the file with the file name
	 */
	public void uploadFileOnEfileIntakeBP(String fileType, String source, String filename, String absoluteFilePath) throws Exception{
		ReportLogger.INFO("Uploading " +  absoluteFilePath + " file");
		selectFileAndSource(fileType, source);
		objPage.waitUntilElementDisplayed(nextButton, 15);
		objPage.scrollToTop();
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
		waitForElementToBeClickable(statusImportedFile,20);
		objPage.waitForElementTextToBe(statusImportedFile, "In Progress", 120);
	}
	
	/**
	 * Description : Reads given excel file to retrieve total rows in each sheet the data into a map.
	 * @param filePath: Takes the path of the XLSX workbook
	 * @return Map: Return a data map
	 **/
	public Map<String, Object> getTotalRowsCountFromExcelForGivenTable(String filePath, String rollYear) throws Exception {
		List<String> sheetNames = new ArrayList<String>();
		Map<String, Object> dataMap = new HashMap<String, Object>();
		FileInputStream file = null;
		XSSFWorkbook workBook = null;
		try {
			file = new FileInputStream(new File(filePath));
			workBook = new XSSFWorkbook(file);

			for (int i = 0; i < workBook.getNumberOfSheets(); i++) {
				String name = workBook.getSheetName(i);
				if(!name.equals("Document Details")) {
					sheetNames.add(name);
				}
			}

			if(filePath.contains("Index") || filePath.contains("Percent")) {
				for(String sheetName : sheetNames) {
					XSSFSheet sheet = workBook.getSheet(sheetName);
					int rowCount = sheet.getPhysicalNumberOfRows() - 1;

					int multipliedCount = 0;
					if(sheetName.contains("Agricultural ME Good Factors")) {
						multipliedCount = rowCount * 2;
						dataMap.put(sheetName, multipliedCount);
					} else if(sheetName.contains("M&E Good Factors")) {
						dataMap.put(sheetName, TestBase.CONFIG.getProperty("totalRecordsInMEGoodFactors"));
					} else {
						dataMap.put(sheetName, rowCount);
					}
				}
			}
			else {
				for (String sheetName : sheetNames) {
					int endingRecordYearForValuationFile = Integer.parseInt(TestBase.CONFIG.getProperty("endingRecordYearForValuationFile"));
					int currentRollYear = Integer.parseInt(rollYear);
					int totalRows;

					if(sheetName.equalsIgnoreCase("Computer Val Factors") || sheetName.equalsIgnoreCase("Semiconductor Val Factors")) {
						int numberOfDataColumns = Integer.parseInt(TestBase.CONFIG.getProperty("dataColumnsInComputeAndSemiConductorValTables"));
						totalRows = (currentRollYear - endingRecordYearForValuationFile) * numberOfDataColumns;
					} else if (sheetName.equalsIgnoreCase("Biopharmaceutical Val Factors")) {
						int numberOfDataColumns = Integer.parseInt(TestBase.CONFIG.getProperty("totalDataColumnsInBioPharmaTable"));
						totalRows = (currentRollYear - endingRecordYearForValuationFile) * numberOfDataColumns;
					} else {
						totalRows = currentRollYear - endingRecordYearForValuationFile;
					}
					dataMap.put(sheetName, totalRows);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			workBook.close();
			file.close();
		}
		return dataMap;
	}

	/**
	 * Description: Retrieves the file status from the table grid on import logs page
	 */
	public String fileStatusOnImportLogsPage(String fileType) throws Exception {
		String name = null;
		if(fileType.equalsIgnoreCase("BOE - Valuation Factors")) {
			name = "BOE - Valuation Factors";
		} else if(fileType.equalsIgnoreCase("BOE - Index and Percent Good Factors")) {
			name = "Index and Percent Good Factors";
		} else if(fileType.equalsIgnoreCase("CAA - Valuation Factors")) {
			name = "CAA - Valuation Factors";
		}

		String xpathStatusColumn = "//span[text() = 'E-File Import Logs']//ancestor::div[contains(@class, 'slds-page-header')]//following-sibling::div//thead//tr//th[contains(@class, 'slds-is-resizable')]//span[@class ='slds-truncate']";
		int indexPositionOfStatus = -1;
		int indexPositionOfName = -1;

		List <WebElement> tableColumns = locateElements(xpathStatusColumn, 30);
		for(int i = 0; i < tableColumns.size(); i++) {
			int indexPos = i+1;
			String columnName = getElementText(locateElement("("+ xpathStatusColumn +")["+ indexPos +"]", 10));
			if(columnName.equalsIgnoreCase("Status")) {
				indexPositionOfStatus = i;
			} else if(columnName.equalsIgnoreCase("Name")) {
				indexPositionOfName = i;
			}

			if(indexPositionOfStatus > -1 && indexPositionOfName > -1) {
				break;
			}
		}

		String xpath;
		int indexDifference = indexPositionOfName - indexPositionOfStatus;
		int elementIndex = Math.abs(indexDifference);

		if(indexPositionOfName < indexPositionOfStatus) {
			xpath = "((//a[contains(@title, '"+ name +"')])[1])//ancestor::th//following-sibling::td["+elementIndex+"]";
		} else {
			xpath = "((//a[contains(@title, '"+ name +"')])[1])//ancestor::th//preceding-sibling::td["+elementIndex+"]";
		}
		return getElementText(locateElement(xpath, 30));
	}
	
	/**
	 * Description: Retrieves the file status from the table grid on import transactions page
	 */
	public String fileStatusOnImportTransactionPage(String fileType) throws Exception {
		String name = null;
		if(fileType.equalsIgnoreCase("BOE - Valuation Factors")) {
			name = "BOE - Valuation Factors";
		} else if(fileType.equalsIgnoreCase("BOE - Index and Percent Good Factors")) {
			name = "Index and Percent Good Factors";
		} else if(fileType.equalsIgnoreCase("CAA - Valuation Factors")) {
			name = "CAA - Valuation Factors";
		}

		String xpathStatusColumn = "//span[text() = 'E-File Import Transactions']//ancestor::div[contains(@class, 'slds-page-header')]//following-sibling::div//thead//tr//th[contains(@class, 'slds-is-resizable')]//span[@class ='slds-truncate']";
		int indexPositionOfStatus = -1;
		int indexPositionOfImportLog = -1;
		List <WebElement> tableColumns = locateElements(xpathStatusColumn, 30);
		for(int i = 0; i < tableColumns.size(); i++) {
			int indexPos = i+1;
			String columnName = getElementText(locateElement("("+ xpathStatusColumn +")["+ indexPos +"]", 10));

			if(columnName.equalsIgnoreCase("Status")) {
				indexPositionOfStatus = i;
			} else if(columnName.equalsIgnoreCase("E-File Import Log")) {
				indexPositionOfImportLog = i;
			}

			if(indexPositionOfStatus > -1 && indexPositionOfImportLog > -1) {
				break;
			}
		}

		String xpath;
		int indexDifference = indexPositionOfImportLog - indexPositionOfStatus;
		int elementIndex = Math.abs(indexDifference);
		if(indexPositionOfImportLog < indexPositionOfStatus) {
			xpath = "((//a[contains(@title, '"+ name +"')])[1])//ancestor::td//following-sibling::td["+elementIndex+"]";
		} else {
			xpath = "((//a[contains(@title, '"+ name +"')])[1])//ancestor::td//preceding-sibling::td["+elementIndex+"]";
		}
		return getElementText(locateElement(xpath, 30));
	}
	
	/**
	 * It checks whether the error rows section is displayed
	 * @return: Returns the status of error rows section based on it visibility as true / false
	 * @throws: Throws Exception
	 */
	public boolean checkPresenceOfErrorRowsSection() throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathErrorSection = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//span[contains(@title,'ERROR ROWS')]";
		return locateElement(xpathErrorSection, 60).isDisplayed();
	}
	/**
	 * It retrieves the count of total rows in error rows section
	 * @return: Returns the count of error rows
	 * @throws: Throws Exception
	 */
	public String getCountOfRowsFromErrorRowsSection(String... expectedCountToBe) throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathErrorSection = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//span[contains(@title,'ERROR ROWS')]";
		if(expectedCountToBe.length == 1) {
			wait.until(ExpectedConditions.textToBe(By.xpath(xpathErrorSection), expectedCountToBe[0]));
		}

		String countOfErrorRows = (getElementText(locateElement(xpathErrorSection, 30)).split(":"))[1].trim();
		return countOfErrorRows;
	}

	/**
	 * It checks whether the imported rows section is displayed
	 * @return: Returns the status of error rows section based on it visibility as true / false
	 * @throws: Throws Exception
	 */
	public boolean checkPresenceOfImportedRowsSection() throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathImportedSection = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//span[contains(@title,'IMPORTED ROWS')]";
		return locateElement(xpathImportedSection, 60).isDisplayed();
	}

	/**
	 * It retrieves the count of total rows in imported rows section
	 * @return: Returns the count of imported rows
	 * @throws: Throws Exception
	 */
	public String getCountOfRowsFromImportedRowsSection() throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathErrorSection = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//span[contains(@title,'IMPORTED ROWS')]";
		String countOfErrorRows = (getElementText(locateElement(xpathErrorSection, 30)).split(":"))[1].trim();
		return countOfErrorRows;
	}
	
	/**
	 * It selects an individual error row and discard it
	 * @throws: Throws Exception
	 */
	public void discardIndividualErrorRow() throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathIndividualCheckBox = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']"
				+ "//td//span[@class='slds-checkbox_faux']";
		WebElement individualCheckBox = locateElement(xpathIndividualCheckBox, 30);
		Click(individualCheckBox);

		String xpathDiscardButton = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//button[text()='Discard']";
		WebElement discardButton = locateElement(xpathDiscardButton, 30);
		waitForElementToBeClickable(discardButton, 10);
		Click(discardButton);

		String xpathContinueButton = "//button[text()='Continue']";
		WebElement continueButton = locateElement(xpathContinueButton, 30);
		if(continueButton == null) {
			discardButton = locateElement(xpathDiscardButton, 30);
			waitForElementToBeClickable(discardButton, 10);
			Click(discardButton);

			continueButton = locateElement(xpathContinueButton, 30);
		}
		waitForElementToBeClickable(continueButton, 10);
		clickAction(continueButton);
	}

	/**
	 * It selects all error rows at once and discard them
	 * @throws: Throws Exception
	 */
	public void discardAllErrorRows() throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathSelectAllCheckBox = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']"
				+ "//input[@class='datatable-select-all'][@type='checkbox']/..//span[@class='slds-checkbox_faux']";
		WebElement selectAllCheckBox = locateElement(xpathSelectAllCheckBox, 60);
		Click(selectAllCheckBox);

		String xpathDiscardButton = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//button[text()='Discard']";
		WebElement discardButton = locateElement(xpathDiscardButton, 30);
		waitForElementToBeClickable(discardButton, 10);
		Click(discardButton);

		String xpathContinueButton = "//button[text()='Continue']";
		WebElement continueButton = locateElement(xpathContinueButton, 30);
		if(continueButton == null) {
			discardButton = locateElement(xpathDiscardButton, 30);
			waitForElementToBeClickable(discardButton, 10);
			Click(discardButton);

			continueButton = locateElement(xpathContinueButton, 30);
		}

		waitForElementToBeClickable(continueButton, 10);
		clickAction(continueButton);
	}
	
	/**
	 * Description: Return a map containing error records, records to be imported and total records in given excel
	 */
	public Map<String, Object> countOfDifferentRowTypesInExcel(String filePath, String rollYear) throws Exception {
		Map<String, Object> dataMap = getTotalRowsCountFromExcelForGivenTable(filePath, rollYear);
		int totalImportedRows = 0;
		int totalErrorRows = Integer.parseInt(TestBase.CONFIG.getProperty("errorRecordsCount")) * dataMap.size();

		int totalRows = 0;
		for(Map.Entry<String, Object> entry : dataMap.entrySet()) {
			String strValue = entry.getValue().toString();
			int intValue = Integer.parseInt(strValue);
			totalRows = totalRows + intValue;
		}

		totalImportedRows = totalRows - totalErrorRows;
		dataMap.put("File Count", totalRows);
		dataMap.put("Import Count", totalImportedRows);
		dataMap.put("Error Count", totalErrorRows);
		return dataMap;
	}
	/**
	 * Description: Retrieves the value of file count column form history table
	 * @throws: Exception
	 */
	public String getFileCountFromHistoryTable() throws Exception {
		String indexPosFileCount = getIndexPositionOfGivenColumn("File Count");
		String xpath = "//h2[text() = 'E-File Import Tool']//ancestor::header//following-sibling::div[@class = 'pageBody']//tbody//tr[1]//td["+ indexPosFileCount +"]";
		return getElementText(locateElement(xpath, 30));
	}

	/**
	 * Description: Retrieves the value of import count column form history table
	 * @throws: Exception
	 */
	public String getImportCountFromHistoryTable() throws Exception {
		String indexPosImportCount = getIndexPositionOfGivenColumn("Import Count");
		String xpath = "//h2[text() = 'E-File Import Tool']//ancestor::header//following-sibling::div[@class = 'pageBody']//tbody//tr[1]//td["+ indexPosImportCount +"]";
		return getElementText(locateElement(xpath, 30));
	}

	/**
	 * Description: Retrieves the value of error count column form history table
	 * @throws: Exception
	 */
	public String getErrorCountFromHistoryTable() throws Exception {
		String indexPosErrorCount = getIndexPositionOfGivenColumn("Error Count");
		String xpath = "//h2[text() = 'E-File Import Tool']//ancestor::header//following-sibling::div[@class = 'pageBody']//tbody//tr[1]//td["+ indexPosErrorCount +"]";
		return getElementText(locateElement(xpath, 30));
	}

	/**
	 * Description: Retrieves the index value of specified column form history table
	 * @param colName: Takes name of the column from history table on Efile import page
	 * @throws: Exception
	 */
	private String getIndexPositionOfGivenColumn(String colName) throws Exception {
		String xpath = "//h2[text() = 'E-File Import Tool']//ancestor::header//following-sibling::div[@class = 'pageBody']//thead//span[@class = 'slds-truncate']";

		int indexPositionOfGivenColumn = -1;

		List <WebElement> tableColumns = locateElements(xpath, 30);
		for(int i = 0; i < tableColumns.size(); i++) {
			int indexPos = i+1;
			String columnName = getElementText(locateElement("("+ xpath +")["+ indexPos +"]", 10));
			if(columnName.equalsIgnoreCase(colName)) {
				indexPositionOfGivenColumn = i;
				break;
			}
		}
		return Integer.toString(indexPositionOfGivenColumn);
	}
	
	/**
	 * Description: Clicks the junk data cell in the selected table and updated the correct value in it
	 * @param tableNumber: Number of the table
	 * @param updatedValue: Value to be entered
	 * @throws Exception
	 */
	public void updateCorrectDataInTable(String tableNumber, String updatedValue) throws Exception {
		String junkDataCellXpath = "(//lightning-tab[@aria-labelledby = '"+tableNumber+"__item']//span[contains(@title,'ERROR ROWS')]//ancestor::div[@class = 'slds-accordion__summary']//following-sibling::div//table//lightning-base-formatted-text[starts-with(text(), 'Junk_')])";
		Click(locateElement(junkDataCellXpath, 10));

		String xpathEditIcon = "//lightning-primitive-cell-factory[@class = 'slds-cell-wrap slds-has-focus']//span[text() = 'Edit Average' or text() = 'Edit Factor' or text() = 'Edit Valuation Factor']//ancestor::button//lightning-primitive-icon";
		WebElement editIcon = locateElement(xpathEditIcon, 10);
		javascriptClick(editIcon);
		enter(inputBoxOnImportPage, updatedValue);
		enter(inputBoxOnImportPage, Keys.TAB);
	}
	/**
	 * Description: Retrieves the value of given field from import log details page
	 * @param fileType: BPP Trend file type
	 * @param fieldName: Takes the name of the field as argument
	 * @return String: Return the String value
	 * @throws: Exception
	 */
	public String getFieldValuesFromImportLogsDetailsPage(String fileType, String fieldName) throws Exception {
		String name = null;
		if(fileType.equalsIgnoreCase("BOE - Valuation Factors")) {
			name = "BOE - Valuation Factors";
		} else if(fileType.equalsIgnoreCase("BOE - Index and Percent Good Factors")) {
			name = "Index and Percent Good Factors";
		} else if(fileType.equalsIgnoreCase("CAA - Valuation Factors")) {
			name = "CAA - Valuation Factors";
		}

		String xpathForNameField = "((//a[contains(@title, '"+ name +"')])[1])";
		Click(locateElement(xpathForNameField, 30));

		String xpathForFieldValue = null;
		if (fieldName.equalsIgnoreCase("File Count") || fieldName.equalsIgnoreCase("Import Count")) {
			xpathForFieldValue = "//div[text() = 'E-File Import Log']//ancestor::div[contains(@class, 'row region-header')]//following-sibling::div//span[text() = '"+ fieldName +"']//parent::div//following-sibling::div//lightning-formatted-number";
		} else {
			xpathForFieldValue = "//div[text() = 'E-File Import Log']//ancestor::div[contains(@class, 'row region-header')]//following-sibling::div//span[text() = '"+ fieldName +"']//parent::div//following-sibling::div//lightning-formatted-text";
		}
		return getElementText(locateElement(xpathForFieldValue, 30));
	}
	/**
	 * It retrieves the count of total rows in imported rows section
	 * @return: Returns the count of imported rows
	 * @throws: Throws Exception
	 */
	public String getCountOfRowsFromImportedRowsSectionForValuationFile(String rollYear, String tableName) throws Exception {
		int endingRecordYearForValuationFile = Integer.parseInt(TestBase.CONFIG.getProperty("endingRecordYearForValuationFile"));
		int errorRecords = Integer.parseInt(TestBase.CONFIG.getProperty("errorRecordsCount"));

		int currentRollYear = Integer.parseInt(rollYear);
		int totalRecordsCount = currentRollYear - endingRecordYearForValuationFile;
		System.setProperty("totalRecordsCount", Integer.toString(totalRecordsCount));

		int expectedRecordsCount = 0;
		int numberOfDataColumns;

		if(tableName.contains("Computer Val Factors") || tableName.contains("Semiconductor Val Factors")) {
			numberOfDataColumns = Integer.parseInt(TestBase.CONFIG.getProperty("dataColumnsInComputeAndSemiConductorValTables"));
			totalRecordsCount = (totalRecordsCount * numberOfDataColumns);
			expectedRecordsCount = totalRecordsCount - errorRecords;
		}
		else if(tableName.contains("Biopharmaceutical Val Factors")) {
			//numberOfDataColumns = Integer.parseInt(TestBase.CONFIG.getProperty("dataColumnsToBeApprovedInBioPharmaTable"));
			numberOfDataColumns = Integer.parseInt(TestBase.CONFIG.getProperty("totalDataColumnsInBioPharmaTable"));
			totalRecordsCount = (totalRecordsCount * numberOfDataColumns);
			expectedRecordsCount = totalRecordsCount - errorRecords;
		}
		else {
			expectedRecordsCount = totalRecordsCount - errorRecords;
		}

		return Integer.toString(expectedRecordsCount);
	}
	/**
	 * Description: Return value of Error Message column from tables based on given parameters
	 * @param tableName: Name of the table which is currently selected
	 * @param rowNumber: Row number in which element needs to be located
	 * @return String: Return the value
	 * @throws: Exception
	 */
	public String getErrorMessageFromTable(String tableName, int rowNumber) throws Exception {
		String columnName = "Error Message";
		return readDataFromBppTrendFactorTableOnEfileImportPage(tableName, rowNumber, columnName);
	}
	/**
	 * Description: Generates xpath for elements in table based on given parameters on approve and revert page in Efile import
	 * @param tableName: Name of the table which is currently selected
	 * @param rowNumber: Row number in which element needs to be located
	 * @param columnName: Name of column whose value needs to be read
	 * @return String: Return value read from table
	 * @throws: Exception
	 */
	public String readDataFromBppTrendFactorTableOnEfileImportPage(String tableName, int rowNumber, String columnName) throws Exception {
		String tableNumber = "0";
		String columnData;
		if("Commercial Equipment Index".equalsIgnoreCase(tableName)) {
			tableNumber = "1";
		} else if("Agricultural Index".equalsIgnoreCase(tableName)) {
			tableNumber = "3";
		} else if("Agricultural ME Good Factors".equalsIgnoreCase(tableName)) {
			tableNumber = "7";
		}

		String xpath = "//lightning-tab[@aria-labelledby = '"+tableNumber+"__item']//span[contains(text(), 'ERROR ROWS')]"
				+ "//ancestor::div[@class = 'slds-accordion__summary']"
				+ "//following-sibling::div[@class = 'slds-accordion__content']"
				+ "//table//tbody//tr["+rowNumber+"]//td[@data-label = '"+columnName+"']//lightning-formatted-text";

		List<WebElement> elements = locateElements(xpath, 20);

		try {
			columnData = getElementText(elements.get(0));
		} catch(Exception ex) {
			columnData = "";
		}
		return columnData;
	}
	/**
	 * Description: Generated an expected error message based on given parameters
	 * @param columnName: Name of the column for which expected error message must be generated
	 * @param columnValue: Value of the given column
	 * @return: Return the expected error message as String
	 * @throws Exception
	 */
	public String generateExpectedErrorMsgForTableColumn(String columnName, String columnValue) throws Exception {
		if("Age".equalsIgnoreCase(columnName)) {
			if("".equals(columnValue)) {
				return "Age, found blank but expected a number between 1.0 and 40.0";
			}
			else {
				if(isColumnValueOnlyNumeric(columnValue)) {
					if(Integer.parseInt(columnValue) == 0) {
						return "Field Age, found "+columnValue+" but expected a number between 1.0 and 40.0";
					} else if(Integer.parseInt(columnValue) > 40) {
						return "Field Age, found "+columnValue+" but expected a number between 1.0 and 40.0";
					}
				}
				else {
					return "Field Age, found "+columnValue+" but expected a number between 1.0 and 40.0";
				}
			}
		}

		else if("Average".equalsIgnoreCase(columnName)) {
			if("".equals(columnValue)) {
				return "Average must be present";
			}
			else {
				if(isColumnValueOnlyNumeric(columnValue)) {
					if(Integer.parseInt(columnValue) == 0) {
						return "Index Factor should be 1 or more";
					} else if(Integer.parseInt(columnValue) > 999) {
						return "NUMBER_OUTSIDE_VALID_RANGE:Index Factor: value outside of valid range";
					}
				}
				else {
					return "Field avg, found "+columnValue+" but expected a number greater than 0";
				}
			}
		}

		else if("Year".equalsIgnoreCase(columnName)) {
			if("".equals(columnValue) || " ".equals(columnValue)) {
				return "Year must be present.";
			}
			else {
				if(columnValue.length() == 4) {
					if(isColumnValueOnlyNumeric(columnValue)) {
						if(isGivenYearValid(columnValue)) {
							return "DUPLICATE_VALUE:duplicate value found";
						} else {
							if(Integer.parseInt(columnValue) < 1974) {
								return "Field year, found "+columnValue+" but expected greater than 1973";
							} else if(Integer.parseInt(columnValue) > Integer.parseInt(TestBase.CONFIG.getProperty("rollYear"))){
								return "Year must be less than 2020";
							}
						}
					} else {
						return "Field year, found "+columnValue+" but expected a valid year";
					}
				}
				else {
					if(isColumnValueOnlyNumeric(columnValue)) {
						if(isGivenYearValid(columnValue)) {
							return "DUPLICATE_VALUE:duplicate value found";
						} else {
							if(Integer.parseInt(columnValue) < 1974) {
								return "Field year, found "+columnValue+" but expected greater than 1973";
							} else if(Integer.parseInt(columnValue) > Integer.parseInt(TestBase.CONFIG.getProperty("rollYear"))){
								return "Year must be less than "+ TestBase.CONFIG.getProperty("rollYear");
							}
						}
					} else if(columnValue.equalsIgnoreCase("XYZ")) {
						return "Field year, found "+columnValue+" but expected a valid year"+"\r\n"+"Field year, found "+columnValue+" but expected it to be less than 2021";
					} 
					else {
						return "Field year, found "+columnValue+" but expected a valid year";
					}
				}
			}
		}
		return "Unsupported column name or unsupported value has been provided. Please Retry With Valid Data!!";
	}
	/**
	 * Description: Checks whether given value from table on approve & revert page in Efile import is numeric
	 * @param str: Takes string value of the number
	 * @return: Return the status as true / false
	 * @throws: Exception
	 */
	private boolean isColumnValueOnlyNumeric(String str) throws Exception {
		if(str.startsWith("-") || str.startsWith("+") || str.contains(".")) {
			return false;
		} else {
			try {
				Integer.parseInt(str);
				return true;
			} catch (NumberFormatException ex) {
				return false;
			}
		}
	}

	/**
	 * Description: Checks whether value of given year is a valid year
	 * @param year: Takes string value of year which needs to be validated
	 * @return: Return status as true / false
	 * @throws: Exception
	 */
	private boolean isGivenYearValid(String year) throws Exception {
		int givenYear = Integer.parseInt(year);
		if(givenYear > 1974 && givenYear < Integer.parseInt(TestBase.CONFIG.getProperty("rollYear"))) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
