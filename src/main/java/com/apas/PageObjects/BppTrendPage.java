package com.apas.PageObjects;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.config.modules;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;

public class BppTrendPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);
	BuildingPermitPage objBuildPermitPage;
	ApasGenericFunctions objApasGenericFunctions;
	
	public BppTrendPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
	}

	// **** Below elements are specific to BPP Trend page ****
	@FindBy(xpath = "//input[@name = 'rollyear']")
	public WebElement rollYearDropdown;

	@FindBy(xpath = "//button[@title=  'Select']")
	public WebElement selectRollYearButton;
	
	@FindBy(xpath = "//button[@title = 'More Tabs']")
	public WebElement moreTabs;
	
	@FindBy(xpath = "//button[text() = 'Cancel']")
	public WebElement cancelBtnInPopUp;
	
	@FindBy(xpath = "//button[text() = 'Confirm']")
	public WebElement confirmBtnInPopUp;
	
	@FindBy(xpath = "//lightning-spinner[contains(@class, 'slds-spinner_container')]//div[contains(@class, 'slds-spinner')]")
	public WebElement statusSpinner;
	
	@FindBy(xpath = "(//th[@data-label = 'Year Acquired'])[1]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))]")
	public WebElement firstRowDataOfTable;
	
	@FindBy(xpath = "//div[contains(@class, 'form-footer')]//button[text() = 'Save']")
	public WebElement saveEditedCellData;
	
	@FindBy(xpath = "//button//span[text() = 'More Actions']")
	public WebElement moreActionsBtn;
	
	@FindBy(xpath = "//span[text() = 'Export']//parent::a")
	public WebElement exportLinkUnderMoreActions;
	
	@FindBy(xpath = "//div[contains(@class, 'modal-container slds')]//input[@id = 'formatted-export']")
	public WebElement formattedReportOption;

	@FindBy(xpath = "//div[contains(@class, 'modal-container slds')]//input[@id = 'data-export']")
	public WebElement detailsOnlyOption;
	
	@FindBy(xpath = "//div[contains(@class, 'modal-container slds')]//span[text() = 'Export']")
	public WebElement exportButton;

	@FindBy(xpath = "//center//div//h2")
	public WebElement pageLevelMsg;
	
	@FindBy(xpath = "//lightning-tab[contains(@data-id, 'BPP Prop 13 Factor')]//input[@name = 'inputCPIFactor']")
	public WebElement cpiFactorTxtBox;
	
	
	// **** Below elements are specific to BPP Trend Setup page ****
	@FindBy(xpath = "//span[text() = 'BPP Settings']/ancestor::header//following-sibling::div//a[contains(@class, 'slds-button') and @role = 'button']")
	public WebElement dropDownIconHeaderPanel;

	@FindBy(xpath = "//a[@title = 'New']")
	public WebElement newBppTrendsettingLink;
	
	@FindBy(xpath = "//ul[@class = 'uiAbstractList']//li//a[contains(@class, 'slds-button') and @role = 'button']")
	public WebElement dropDownIconDetailsSection;

	@FindBy(xpath = "//a[@title = 'Edit']")
	public WebElement editBppTrendsettingLink;
	
	@FindBy(xpath = "//a[@title = 'Delete']")
	public WebElement deleteBppTrendsettingLink;
	
	@FindBy(xpath = "//div[contains(@class, 'modal-footer slds')]//button[@title = 'Save']")
	public WebElement saveBtnInBppSettingPopUp;
	
	@FindBy(xpath = "//div[contains(@class, 'modal-footer slds')]//button[@title = 'Save & New']")
	public WebElement saveAndNewBtnInBppSettingPopUp;
	
	@FindBy(xpath = "//div[contains(@class, 'modal-footer slds')]//button[@title = 'Cancel']")
	public WebElement cancelBtnInBppSettingPopUp;
	
	@FindBy(xpath = "//div[contains(@class, 'modal-footer slds')]//button[@title = 'Delete']")
	public WebElement deletBtnInBppSettingPopUp;
	
	@FindBy(xpath = "//a[@class = 'deleteAction']")
	public WebElement deleteIconForPrePopulatedRollYear;
	
	@FindBy(xpath = "//span[text() = 'BPP Trend Roll Year']//parent::label//following-sibling::div//input")
	public WebElement bppTrendRollYearTxtBox;
	
	@FindBy(xpath = "//li[contains(@class, 'uiAutocompleteOption')]//mark[text() = '2019']//ancestor::a")
	public WebElement bppTrendRollYearDrpDownList;
	
	@FindBy(xpath = "//span[text() = 'Maximum Equipment index Factor']//parent::label//following-sibling::input")
	public WebElement maxEquipFactorTxtBox;
	
	@FindBy(xpath = "//span[text() = 'Name']//parent::label//following-sibling::input")
	public WebElement systemNameInBewBppTrendSetting;
	
	@FindBy(xpath = "//span[text() = 'Files']//parent::span[text() = 'View All']//ancestor::a")
	public WebElement viewAllFiles;
	
	@FindBy(xpath = "//span[text() = 'BPP Settings']//parent::span[text() = 'View All']//ancestor::a")
	public WebElement viewAllBppSettings;
	
	
	/**
	 * Description: This will select the roll year from the drop down
	 * @param rollYear: Roll Year for which the BPP Trend Name needs to be clicked
	 * @throws: Exception
	 */	
	public void clickBppTrendSetupRollYearNameInGrid(String rollYear) throws Exception {
		String xpath = "//span[text() = '"+ rollYear +"']//ancestor::td//preceding-sibling::th//a[contains(@title, 'BPP Trend')]";
		WebElement bppTrendSetupName = locateElement(xpath, 20);
		Click(bppTrendSetupName);
	}
	
	/**
	 * Description: This will select the roll year from the drop down
	 * @param rollYear: Roll Year to select from drop down
	 * @throws: Exception 
	 */
	public void clickOnGivenRollYear(String rollYear) throws Exception {
		String xpathStr = "//div[contains(@id,'dropdown-element')]//span[contains(text(),'" + rollYear + "')]";
		WebElement element = waitForElementToBeClickable(xpathStr);
		element.click();
	}
			
	/**
	 * Description: This will click on the given table name
	 * @param tableName: Name of the table
	 * @param isTableUnderMoreTab: true / false flag to specify whether given table falls under more tab
	 */
	public void clickOnTableOnBppTrendPage(String tableName, boolean isTableUnderMoreTab) throws Exception {
		String xpathStr;
		if(isTableUnderMoreTab) {
			Click(waitForElementToBeClickable(moreTabs));
			xpathStr = "//span[contains(text(), '" + tableName + "')]";
		} else {
			xpathStr = "//a[contains(@data-label, '" + tableName + "')]";
		}		
		WebElement givenTable = locateElement(xpathStr, 20);
		Click(givenTable);
	}
			
	/**
	 * Description: This will check whether calculate button is visible
	 * @param args: It supports variable arguments, either 1, 2 or 3 arguments
	 * First argument always has to be the name of button to locate on web-page
	 * Second argument would always be the timeOut in seconds for which availability of element needs to be checked
	 * Third argument if required would always be the table name (in case of checking button at table level)
	 * @return : Status of calculate button as true / false
	 */
	private boolean checkAvailabilityOfRequiredButton(Object ...args) throws Exception {
		String xpathBtn = null;
		int timeoutInSeconds = 0;
		if(args.length == 3) {
			xpathBtn = getXpathForRequiredBtton(args[0].toString(), args[2].toString());
			timeoutInSeconds = Integer.parseInt(args[1].toString());
		} else if (args.length == 2) {
			xpathBtn = getXpathForRequiredBtton(args[0].toString());
			timeoutInSeconds = Integer.parseInt(args[1].toString());
		}
		
		WebElement button = locateElement(xpathBtn, timeoutInSeconds);
		if(button == null) {
			return false;
		} else {
			return button.isDisplayed();
		}
	}
	
	/**
	 * Description: This will initiate the calculation by clicking calculate button for individual table (at table level)
	 * @param tableName: Name of the table
	 */
	private void clickRequiredButton(String ...args) throws Exception {
		String xpath = getXpathForRequiredBtton(args);
		WebElement button = locateElement(xpath, 40); 
		javascriptClick(button);		
	}
	
	/**
	 * Description: This find the XPath of given button for given table name
	 * @param args: It supports variable arguments, either 1 or 2 argumets
	 * where first variable always has to be the name of button to locate on webpage
	 * and second variable if required would always be the table name
	 * @return : Return the xpath for given button
	 */
	private String getXpathForRequiredBtton(String ...args) throws Exception {		
		String buttonName = null;
		String btnXpath = null;
		if(args.length == 1) {
			buttonName = args[0];
			btnXpath = "//button[text() = '"+ args[0] +"'] | //button[@title = '"+ args[0] +"']";
		} else if (args.length == 2) {
			buttonName = args[0];
			String tableName = args[1];
			
			String xpathBeforeTblName = "//lightning-tab[@data-id = '";
			String xpathAfterTblName = "']//button[@title = '";
			String xpathAfterBtnName = "']";
			
			btnXpath = xpathBeforeTblName + tableName + xpathAfterTblName + buttonName + xpathAfterBtnName;
		}
		return btnXpath;
	}

	/**
	 * @Description: Below method would read and return the cell data from the table displayed for selected roll year
	 * @param cellDetails: Details of cell to edit (row number and column number if required) 
	 * @return: Returns the visible text displayed in the select cell
	 * @throws Exception
	 */
	public String getCellDataFromGridForGivenTable(int... cellDetails) throws Exception {
		String xpathCellData = null;
		if(cellDetails.length == 0) {
			xpathCellData = "((//th[@data-label = 'Year Acquired'])[1]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])[1]";
		} else if (cellDetails.length == 1) {
			xpathCellData = "((//th[@data-label = 'Year Acquired'])[1]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])["+ cellDetails[0] +"]"; 
		} else if (cellDetails.length == 2) {
			xpathCellData = "((//th[@data-label = 'Year Acquired'])["+ cellDetails[0] +"]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])["+ cellDetails[1] +"]";
		}
		System.setProperty("xpathCellData", xpathCellData);
		WebElement element = locateElement(xpathCellData, 20);
		return getElementText(element);
	}
	
	/**
	 * @Description: Below method is used the edit the cell data in the table displayed for select roll year.
	 * @param: Takes the integer / double value to enter in the cell
	 * @throws Exception 
	 */	
	public void editCellDataInGridForGivenTable(Object data) throws Exception {
		clickAction(waitForElementToBeClickable(System.getProperty("xpathCellData")));
		String xpathEditBtn = "//td[contains(@class, 'has-focus')]//button[contains(@class, 'cell-edit')]//lightning-primitive-icon";
		WebElement editButton = locateElement(xpathEditBtn, 20);
		Click(editButton);
		
		String xpathEditTxtBox = "//div//input[@name = 'dt-inline-edit-text']";
		WebElement editTxtBox = locateElement(xpathEditTxtBox, 20);
		enter(editTxtBox, String.valueOf(data));
		WebElement year = locateElement("//th[@data-label = 'Year Acquired']", 20);
		Click(year);
	}

	/**
	 * Description: This will retrieve the message displayed above table data on performing calculate / recalculate / submit for approval
	 * @param tableName: Name of the table
	 * @return : Message displayed to show performed action's status
	 */
	public String retrieveMsgDisplayedAboveTable(String tableName) throws Exception {		
		String xpath = "//lightning-tab[@data-id = '"+ tableName +"']//div[@class = 'hightlight-tab-message']";
		WebElement message = locateElement(xpath, 30);
		String text = getElementText(message);
		return text;
	}

	/**
	 * Description: This will reset the status of tables based on the expected status
	 * @param factorTablesToReset: List of table names for which status is to be reset
	 * @param expectedStatus: Expected status like Calculated, Not Calculated etc.
	 * @param rollYear: Roll year for which the status needs to be reset
	 */
	public void resetTablesStatusForGivenRollYear(List<String> factorTablesToReset, String expectedStatus, String rollYear) throws Exception {
		objApasGenericFunctions.login(users.SYSTEM_ADMIN);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		
		clickBppTrendSetupRollYearNameInGrid(rollYear);
		WebElement element;
		for(String factorTableName : factorTablesToReset) {
			String xpathEditIcon = "//span[text() = '"+ factorTableName +"']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]";
			element = locateElement(xpathEditIcon, 10);
			javascriptClick(element);
			
			String xpathStrInputFields = "//label[text() = '"+ factorTableName +"']//following-sibling::div//input[@type = 'text']";
			element = waitForElementToBeClickable(xpathStrInputFields);
			javascriptClick(element);
			
			String dropDownOptionXpath = "//div[@role='listbox' and contains(@id, 'dropdown-element')]//lightning-base-combobox-item[@data-value = '"+ expectedStatus +"']"; 
			WebElement drpDownElement = locateElement(dropDownOptionXpath, 10);
			javascriptClick(drpDownElement);
			Click(objBuildPermitPage.saveBtnDetailsPage);
		}
		objApasGenericFunctions.logout();
		Thread.sleep(3000);
	}

	/**
	 * Description: Checks the downloaded file in user's system
	 * @param fileExtension: Expected extension of the downloaded file 
	 * @return: Return the status true/false on basis of existence of downloaded file
	 */
	public List<String> checkFactorFilesInDownloadFolder() throws Exception {
		List<String> fileNames = new ArrayList<String>(); 
		String fileNameFromDir = null;
		String filePath = "C:/Downloads/";
		File sourceFiles = new File(filePath);
		File[] listOfFiles = sourceFiles.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			fileNameFromDir = listOfFiles[i].getName().toUpperCase();			  
			if (listOfFiles[i].isFile()) {
				fileNameFromDir = listOfFiles[i].getName().toUpperCase();
				fileNames.add(fileNameFromDir);
			}
		}
		return fileNames;
	}
	
	/**
	 * Deletes the given files from the downloads directory
	 */
	public void deleteFactorFilesFromDownloadFolder(List<String> filesToDelete) {
		File file;
		for(String fileName : filesToDelete) {
			String filePath = "C:/Downloads/";
			filePath = filePath + fileName;

			file = new File(filePath);
			file.delete();	
		}
	}
	
	/**
	 * @Description: Exports the valuation & composite factors excel files
	 * @throws Exception
	 */
	public void exportBppTrendFactorsExcelFiles() throws Exception {
		// Clicking MoreAction button and then selecting Export option from drop down to open export option window
		javascriptClick(waitForElementToBeClickable(moreActionsBtn));
		clickAction(waitForElementToBeClickable(exportLinkUnderMoreActions));
		
		// Selecting the export option and clicking export button to initiate export
		driver.switchTo().defaultContent();
		javascriptClick(waitForElementToBeClickable(formattedReportOption));
		javascriptClick(waitForElementToBeClickable(exportButton));
		
		Thread.sleep(10000);
	}
	
	/**
	 * @Description : Reads tabular grid data from UI
	 * @param tableName: Takes the name of the table for which data needs to be read
	 * @return: Return a data map          
	 **/
	public Map<String, List<Object>> retrieveDataFromGridForGivenTable(String tableName) throws Exception {	
		Map<String, List<Object>> uiTableDataMap = new HashMap<String, List<Object>>();
		
		String xpathTableRows = "//lightning-tab[contains(@data-id, '"+ tableName +"')]//table//tbody//tr";
		String xpathTableData = "//td[(not(contains(@data-label, 'Year Acquired'))) "
				+ "or (not(contains(@data-label, 'Year Acquired')) and contains(@data-label, 'Rounded'))]";
		List<WebElement> tableRows = locateElements(xpathTableRows, 30);
		
		for(int i = 0; i < tableRows.size(); i++) {
			int rowNum = i + 1;
			String xpathYearAcq = "("+ xpathTableRows +")["+ rowNum +"]//th";
			WebElement yearAcquiredElement = locateElement(xpathYearAcq, 10);
			String yearAcquiredTxt = getElementText(yearAcquiredElement);
			
			String xpathYearData = "("+ xpathTableRows +")["+ rowNum +"]"+ xpathTableData;
			List<WebElement> yearAcruiredDataElements = locateElements(xpathYearData, 10);
			
			List<Object> yearAcruiredData = new ArrayList<Object>();
			for(int j = 0; j < yearAcruiredDataElements.size(); j++) {
				Object cellData = Double.parseDouble(getElementText(yearAcruiredDataElements.get(j)));
				yearAcruiredData.add(cellData);
			}
			uiTableDataMap.put(yearAcquiredTxt, yearAcruiredData);
		}
		return uiTableDataMap;
	}
	
	/**
	 * @Description : Reads given excel file and converts the data into a map.
	 * @param filePath: Takes the path of the XLSX workbook
	 * @param sheetName: Takes the names of the Sheet that is be read from given workbook
	 * @return: Return a data map          
	 **/
	@SuppressWarnings("deprecation")
	public Map<String, List<Object>> retrieveDataFromExcelForGivenTable(String filePath, String sheetName) throws Exception {
		String sheetNameForExcel = "";
		switch (sheetName) {
        case "Commercial Composite Factors":
        	sheetNameForExcel = "Commercial Composite";
            break;
        case "Industrial Compoiste Factors":
        	sheetNameForExcel = "Industrial Composite";
            break;
        case "Agricultural Compoiste Factors":
        	sheetNameForExcel = "Agricultural Composite";
            break;
        case "Construction Compoiste Factors":
        	sheetNameForExcel = "Construction Composite";
            break;
        case "Agricultural Mobile Equipment Composite Factors":
        	sheetNameForExcel = "Ag Mobile Equip Composite";
            break;
        case "Construction Mobile Equipment Composite Factors":
        	sheetNameForExcel = "Construction Mobile Composite";
            break;
        case "BPP Prop 13 Factors":
        	sheetNameForExcel = "2019 CPI";
            break;
        } 

		Map<String, List<Object>> dataMap = new HashMap<String, List<Object>>();
		FileInputStream file = null;
		XSSFWorkbook workBook = null;
		try {
            file = new FileInputStream(new File(filePath));
            workBook = new XSSFWorkbook(file);            
            XSSFSheet sheet = workBook.getSheet(sheetNameForExcel);            
            DataFormatter dataFormatter = new DataFormatter(); 
            
            Row headerRow = sheet.getRow(0);
            int totalCells = headerRow.getLastCellNum();   
            int cellIndexToSkip = -1;
            int cellIndexForYearAcq = 0;
            for(int i = 0; i < totalCells; i++) {
            	String cellValue = dataFormatter.formatCellValue(headerRow.getCell(i));
            	if(cellValue.toUpperCase().contains("AGE") || cellValue.toUpperCase().contains("ACQ. DURING")) {
            		cellIndexToSkip = i;
            	}
            	if(cellValue.toUpperCase().contains("YEAR ACQUIRED") || cellValue.toUpperCase().contains("YEAR") || cellValue.toUpperCase().contains("YEAR ACQ.")) {
            		cellIndexForYearAcq = i;
            	}
            }
            
            int totalRows = sheet.getPhysicalNumberOfRows();      		
            for(int i = 1; i <= totalRows; i++) {
                Row currentRow = sheet.getRow(i);
                List<Object> currentRowData = new ArrayList<>();
                String yearAcquired = null;

                FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();                
                for(int j = 0; j < totalCells; j++) {
                	if(j != cellIndexToSkip) {
                		Cell cell = currentRow.getCell(j);
                		CellValue cellValue = evaluator.evaluate(cell);
                		Object strValue = cellValue.getNumberValue();
                		
                		switch (cellValue.getCellType()) {
                		    case Cell.CELL_TYPE_STRING:
                		        strValue = cellValue.getStringValue();
                		        break;
                		    case Cell.CELL_TYPE_NUMERIC:
                		        strValue = cellValue.getNumberValue();
                		        break;
                		}
                		                		
                		if(j != cellIndexForYearAcq) {
                			currentRowData.add(strValue);
                		} else {
                			yearAcquired = strValue.toString().substring(0, strValue.toString().indexOf("."));
                		}
                	}
                }
                dataMap.put(yearAcquired, currentRowData);
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
	 * Description: It highlights the mismatched cell data of the table in red color
	 * @param tableName: Name of the table on BPP Trend page
	 * @param yearAcquired: Acquired year that is being validated
	 * @param indexValueOfCell: Table data/Cell number that is mismatched
	 * @throws Exception
	 */
	public void highlightMismatchedGridCellsForGivenTable(String tableName, String yearAcquired, int indexValueOfCell) throws Exception {
		indexValueOfCell = indexValueOfCell + 1;
		String xpathMisMatchedCell = "(//lightning-tab[contains(@data-id, '"+tableName+"')]//"
				+ "th//lightning-formatted-text[text() = '"+yearAcquired+"']//"
				+ "ancestor::th//following-sibling::td[not(contains(@data-label, 'Year Acquired')) "
				+ "or (not(contains(@data-label, 'Year Acquired')) and contains(@data-label, 'Rounded'))])["+ indexValueOfCell +"]";
		
		WebElement misMatchedCell = locateElement(xpathMisMatchedCell, 20);
		((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid red'", misMatchedCell);
	}
	
	/**
	 * @Description: Return the list containing the names of columns visible of UI
	 * @param: Takes the name of the table
	 * @return: Returns a list of column names displayed in table grid 
	 * @throws Exception 
	 */
	public List<String> retrieveColumnNamesOfGridForGivenTable(String tableName) throws Exception {
		List<String> columnNames = new ArrayList<String>();
		String xpathColNames = "//lightning-tab[contains(@data-id, '"+ tableName +"')]//slot"
				+ "//th[@scope = 'col' and (not(contains(@aria-label, 'Year Acquired')))]";
		List<WebElement> colNames = locateElements(xpathColNames, 10);
		for(WebElement element : colNames) {
			columnNames.add(getElementText(element));
		}
		return columnNames;
	}

	/**
	 * @Description: Returns a list containing visible text of web elements found by given xpath string
	 * @param: Takes the xpath of the elements from which text needs to be read
	 * @return: Returns a list of String having the visible text of elements found by given xpath 
	 * @throws Exception 
	 */
	public List<String> getTextOfMultipleElements(String xpath) throws Exception {
		List<String> listOfVisibleTextFromElements = new ArrayList<String>();
		List<WebElement> elements = locateElements(xpath, 30);
		for(WebElement element : elements) {
			listOfVisibleTextFromElements.add(getElementText(element));
		}
		return listOfVisibleTextFromElements;
	}

	/**
	 * Checks whether calculate button is visible on BPP trend page for selected table
	 * @param: Table for which the button needs to be checked
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of visibility of the button
	 * @throws: Exception
	 */
	public boolean isCalculateBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		return checkAvailabilityOfRequiredButton("Calculate", timeToLocateBtn, tableName);
	}
	
	/**
	 * Checks whether calculate all button is visible on BPP trend page
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of visibility of the button
	 * @throws: Exception
	 */
	public boolean isCalculateAllBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("Calculate all", timeToLocateBtn);
	}
	
	/**
	 * Checks whether re-calculate button is visible on BPP trend page for selected table
	 * @param: Table for which the button needs to be checked
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of visibility of the button
	 * @throws: Exception
	 */
	public boolean isReCalculateBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		return checkAvailabilityOfRequiredButton("Recalculate", timeToLocateBtn, tableName);
	}
	
	/**
	 * Checks whether re-calculate all button is visible on BPP trend page
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of visibility of the button
	 * @throws: Exception
	 */
	public boolean isReCalculateAllBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("ReCalculate all", timeToLocateBtn);
	}
	
	/**
	 * Checks whether submit for approval button is visible on BPP trend page for selected table
	 * @param: Table for which the button needs to be checked
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of visibility of the button
	 * @throws: Exception
	 */
	public boolean isSubmitForApprovalBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		return checkAvailabilityOfRequiredButton("Submit For Approval", timeToLocateBtn, tableName);
	}
	
	/**
	 * Checks whether submit all factors for approval button is visible on BPP trend page
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of visibility of the button
	 * @throws: Exception
	 */
	public boolean isSubmitAllForApprovalBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("Submit All Factors for Approval", timeToLocateBtn);
	}
	
	/**
	 * Checks whether approve button is visible on BPP trend page for selected table
	 * @param: Table for which the button needs to be checked
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isApproveBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		return checkAvailabilityOfRequiredButton("Approve", timeToLocateBtn, tableName);
	}

	/**
	 * Checks whether approve all button is visible on BPP trend page for selected table
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isApproveAllBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("Approve all", timeToLocateBtn);
	}

	/**
	 * Checks whether Download button is visible
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isDownloadBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("Download", timeToLocateBtn);
	}
	
	/**
	 * Checks whether Export Composite Factors button is visible
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isExportCompositeFactorsBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("Export Composite Factors", timeToLocateBtn);
	}
	
	/**
	 * Checks whether Export Valuation Factors button is visible
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based of presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isExportValuationFactorsBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("Export Valuation Factors", timeToLocateBtn);
	}
	
	
	/**
	 * Clicks the Calculate button for selected table
	 * @param: Takes the names of table for which button is to be clicked
	 * @throws: Exception
	 */
	public void clickCalculateBtn(String tableName) throws Exception {
		clickRequiredButton("Calculate", tableName);
	}
	
	/**
	 * Clicks the ReCalculate button for selected table
	 * @param: Takes the names of table for which button is to be clicked
	 * @throws: Exception
	 */
	public void clickReCalculateBtn(String tableName) throws Exception {
		clickRequiredButton("Recalculate", tableName);
	}
	
	/**
	 * Clicks the calculate button for selected table
	 * @param: Takes the names of table for which button is to be clicked
	 * @throws: Exception
	 */
	public void clickSubmitForApprovalBtn(String tableName) throws Exception {
		clickRequiredButton("Submit For Approval", tableName);
	}
	
	/**
	 * Clicks the Approve button for selected table
	 * @param: Takes the names of table for which button is to be clicked
	 * @throws: Exception
	 */
	public void clickApproveButton(String tableName) throws Exception {
		clickRequiredButton("Approve", tableName);
	}
	
	/**
	 * Clicks the Calculate All button
	 * @throws: Exception
	 */
	public void clickCalculateAllBtn() throws Exception {
		clickRequiredButton("Calculate all");
	}
	
	/**
	 * Clicks the ReCalculate All button
	 * @throws: Exception
	 */
	public void clickReCalculateAllBtn() throws Exception {
		clickRequiredButton("ReCalculate all");
	}
	
	/**
	 * Clicks the Submit All Factors For Approval button
	 * @throws: Exception
	 */
	public void clickSubmitAllFactorsForApprovalBtn() throws Exception {
		clickRequiredButton("Submit All Factors for Approval");
	}
	
	/**
	 * Clicks the Approve All button
	 * @throws: Exception
	 */
	public void clickApproveAllBtn() throws Exception {
		clickRequiredButton("Approve all");
	}
	
	/**
	 * Clicks the Download button
	 * @throws: Exception
	 */
	public void clickDownloadBtn() throws Exception {
		clickRequiredButton("Download");
	}
	
	/**
	 * Clicks the Export Composite Factors button
	 * @throws: Exception
	 */
	public void clickExportCompositeFactorsBtn() throws Exception {
		clickRequiredButton("Export Composite Factors");
	}
	
	/**
	 * Clicks the Export Valuation Factors button
	 * @throws: Exception
	 */
	public void clickExportValuationFactorsBtn() throws Exception {
		clickRequiredButton("Export Valuation Factors");
	}
	
	/**
	 * It wait for the pop up message to show up when calculate button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnCalculateClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'Data calculated successfully']";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when submit for approval button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnSubmitForApprovalClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'All Factors have been submitted for approval']";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnApproveClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'Data approved successfully']";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnCalculateAllClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(text(), 'Calculated all tabs successfully')]";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnSubmitAllForApprovalClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(text(), 'All Factors have been submitted for approval')]";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnApproveAllClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(text(), 'Approved all tabs')]";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		return popUpMsg;
	}
}