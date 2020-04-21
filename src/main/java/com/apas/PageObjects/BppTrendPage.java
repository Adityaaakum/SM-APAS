package com.apas.PageObjects;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.TestBase.TestBase;
import com.apas.config.modules;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;

public class BppTrendPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);
	BuildingPermitPage objBuildPermitPage;
	BuildingPermitPage objBuildPermit;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	
	public BppTrendPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
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
	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//a[text() = 'BPP Property Index Factors']")
	public WebElement bppProperyIndexFactorsTab;
	
	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'BPP Property Index Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//th[@title = 'Name (Roll Year - Property Type)']")
	public WebElement bppProperyIndexFactorsTableSection;

	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//a[text() = 'BPP Percent Good Factors']")
	public WebElement bppProperyGoodFactorsTab;
	
	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'BPP Percent Good Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table")
	public WebElement bppPercentGoodFactorsTableSection;

	@FindBy(xpath = "//div[contains(@class, 'column region-main')]//button[@title = 'More Tabs']")
	public WebElement moreTabRightSection;
	
	@FindBy(xpath = "//span[text() = 'Imported Valuation Factors']//parent::a")
	public WebElement dropDownOptionBppImportedValuationFactors;
	
	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'Imported Valuation Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table")
	public WebElement bppImportedValuationFactorsTableSection;
	
	@FindBy(xpath = "//span[text() = 'Composite Factors']//parent::a")
	public WebElement dropDownOptionCompositeFactors;
	
	@FindBy(xpath = "//div[contains(@class, 'column region-sidebar-right')]//button[@title = 'More Tabs']")
	public WebElement moreTabLeftSection;
	
	@FindBy(xpath = "//span[text() = 'BPP Composite Factors Settings']//parent::a")
	public WebElement bppCompositeFactorOption;
	
	@FindBy(xpath = "//span[text() = 'BPP Settings']/ancestor::header//following-sibling::div//a[contains(@class, 'slds-button') and @role = 'button']")
	public WebElement dropDownIconBppSetting;

	@FindBy(xpath = "//span[text() = 'BPP Composite Factors Settings']/ancestor::header//following-sibling::div//a[contains(@class, 'slds-button') and @role = 'button']")
	public WebElement dropDownIconBppCompFactorSetting;
	
	@FindBy(xpath = "//a[@title = 'New']")
	public WebElement newBppTrendSettingLink;
	
	@FindBy(xpath = "//span[contains(text(), 'Roll Year')]//parent::label//following-sibling::div//input[contains(@class, 'uiInputTextForAutocomplete')]")
	public WebElement rollYearTxtBox;
	
	@FindBy(xpath = "//ul[@class = 'uiAbstractList']//li//a[contains(@class, 'slds-button') and @role = 'button']")
	public WebElement dropDownIconDetailsSection;

	@FindBy(xpath = "//a[@title = 'Edit']")
	public WebElement editBppTrendsettingLink;
	
	@FindBy(xpath = "//a[@title = 'Delete']")
	public WebElement deleteBppTrendsettingLink;
	
	@FindBy(xpath = "//div[@class = 'actionsContainer']//div[contains(@class, 'slds-float_right')]//button[@title = 'Save']")
	public WebElement saveBtnInBppSettingPopUp;
	
	@FindBy(xpath = "//div[@class = 'actionsContainer']//div[contains(@class, 'slds-float_right')]//button[@title = 'Save & New']")
	public WebElement saveAndNewBtnInBppSettingPopUp;
	
	@FindBy(xpath = "//div[@class = 'actionsContainer']//div[contains(@class, 'slds-float_right')]//button[@title = 'Cancel']")
	public WebElement cancelBtnInBppSettingPopUp;
	
	@FindBy(xpath = "//div[contains(@class, 'modal-footer slds')]//button[@title = 'Delete']")
	public WebElement deletBtnInBppSettingPopUp;
	
	@FindBy(xpath = "//a[@class = 'deleteAction']")
	public WebElement deleteIconForPrePopulatedRollYear;
	
	@FindBy(xpath = "//span[text() = 'BPP Trend Roll Year']//parent::label//following-sibling::div//input")
	public WebElement bppTrendRollYearTxtBox;
	
	@FindBy(xpath = "//li[contains(@class, 'uiAutocompleteOption')]//mark[text() = '2019']//ancestor::a")
	public WebElement bppTrendRollYearDrpDownList;
	
	@FindBy(xpath = "//span[contains(text(), 'Factor')]//parent::label//following-sibling::input")
	public WebElement factorTxtBox;
	
	@FindBy(xpath = "//span[text() = 'Name']//parent::label//following-sibling::input")
	public WebElement systemNameInBewBppTrendSetting;
	
	@FindBy(xpath = "//span[text() = 'Files']//parent::span[text() = 'View All']//ancestor::a")
	public WebElement viewAllFiles;
	
	//@FindBy(xpath = "//span[text() = 'BPP Settings']//parent::span[text() = 'View All']//ancestor::a")
	@FindBy(xpath = "//span[text() = 'BPP Settings']//parent::span[text() = 'View All']")
	public WebElement viewAllBppSettings;
	
	@FindBy(xpath = "//span[text() = 'Name']//parent::label//following-sibling::input")
	public WebElement bppTrendSetupName;
	
	@FindBy(xpath = "//span[text() = 'Property Type']//parent::span/following-sibling::div")
	public WebElement propertyType;
	
	@FindBy(xpath = "//a[text() = 'BPP Property Index Factors']")
	public WebElement propertyIndexFactorsTab;

	@FindBy(xpath = "//a[text() = 'BPP Percent Good Factors']")
	public WebElement percentGoodFactorsTab;
	
	@FindBy(xpath = "//a[text() = 'Imported Valuation Factors']")
	public WebElement importedValuationFactorsTab;
	
	/**
	 * Description: This will select the roll year from the drop down
	 * @param rollYear: Roll Year for which the BPP Trend Name needs to be clicked or bpp trend name itself
	 * @throws: Exception
	 */	
	public void clickBppTrendSetupRollYearNameInGrid(String rollYearDetails) throws Exception {
		String expRegexPattern = "\\d\\d\\d\\d";
		Pattern.compile(expRegexPattern);
	
		String xpath;
		if(Pattern.matches(expRegexPattern, rollYearDetails)) {
			xpath = "//span[text() = '"+ rollYearDetails +"']//ancestor::td//preceding-sibling::th//a[contains(@title, 'BPP Trend')]";
		} else {
			xpath = "//th//a[contains(@title, '"+ rollYearDetails +"')]";
		}
		
		WebElement bppTrendSetup = locateElement(xpath, 20);
		String bppTrendSetupName = getElementText(bppTrendSetup).trim();
		javascriptClick(bppTrendSetup);
		System.setProperty("BppTrendSetupName", bppTrendSetupName);
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
	 * @throws: Exception
	 */
	public void clickOnTableOnBppTrendPage(String tableName, boolean isTableUnderMoreTab) throws Exception {
		String xpathStr;
		if(isTableUnderMoreTab) {
			Click(waitForElementToBeClickable(moreTabs));
			xpathStr = "//span[contains(text(), '" + tableName + "')]";
		} else {
			xpathStr = "//a[contains(@data-label, '" + tableName + "')]";
		}
		WebElement givenTable = locateElement(xpathStr, 120);
		Click(givenTable);
	}

	/**
	 * Description: This will click on the given table name
	 * @param tableName: Name of the table
	 * @throws: Exception
	 */
	public boolean isTableVisibleOnCalculateClick(String tableName) throws Exception {
		String tableXpath = "//lightning-tab[@data-id = '"+ tableName +"']//table";
		WebElement tableContent = locateElement(tableXpath, 60);
		if(tableContent.isDisplayed()) {
			return true;
		} else {
			return false;
		}
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
	 * @Description: Locates the cell text box in grid
	 * @param tableName: Name of the table for which cell text box needs to be located 
	 * @param cellDetails: Details of cell to edit (row number and column number if required) 
	 * @return: Returns the cell data text box element
	 * @throws Exception
	 */
	public WebElement locateCellTxtBoxElementInGrid(String tableName, int... cellDetails) throws Exception {
		String xpathCellData = null;
		if(cellDetails.length == 0) {
			xpathCellData = "((//lightning-tab[@data-id = '"+ tableName +"']//th[@data-label = 'Year Acquired'])[1]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])[1]";
		} else if (cellDetails.length == 1) {
			xpathCellData = "((//lightning-tab[@data-id = '"+ tableName +"']//th[@data-label = 'Year Acquired'])[1]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])["+ cellDetails[0] +"]"; 
		} else if (cellDetails.length == 2) {
			xpathCellData = "((//lightning-tab[@data-id = '"+ tableName +"']//th[@data-label = 'Year Acquired'])["+ cellDetails[0] +"]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])["+ cellDetails[1] +"]";
		}
		System.setProperty("xpathCellData", xpathCellData);
		WebElement element = locateElement(xpathCellData, 20);
		return element;
	}

	/**
	 * @Description: Locates the edit button in the cell data text box
	 * @param cellDetails: Details of cell to edit (row number and column number if required) 
	 * @return: Returns the edit button element
	 * @throws Exception
	 */
	public WebElement locateEditButtonInFocusedCellTxtBox() throws Exception {
		WebElement editButton = null;
		clickAction(waitForElementToBeClickable(System.getProperty("xpathCellData")));
		String xpathEditBtn = "//td[contains(@class, 'has-focus')]//button[contains(@class, 'cell-edit')]//lightning-primitive-icon";
		editButton = locateElement(xpathEditBtn, 30);
		return editButton;
	}
	
	/**
	 * @Description: Below method is used the edit the cell data in the table displayed for select roll year.
	 * @param: Takes the integer / double value to enter in the cell
	 * @throws Exception 
	 */	
	public void editCellDataInGridForGivenTable(Object data) throws Exception {		
		String xpathEditTxtBox = "//div//input[@name = 'dt-inline-edit-text']";
		WebElement editTxtBox = locateElement(xpathEditTxtBox, 20);
		enter(editTxtBox, String.valueOf(data));
		WebElement year = locateElement("//th[@data-label = 'Year Acquired']", 20);
		Click(year);
	}

	/**
	 * Description: This will retrieve the message displayed above table data on performing calculate / recalculate / submit for approval
	 * @param tableName: Name of the table
	 * @return : Message displayed above the table post performed action is complete
	 */
	public String retrieveMsgDisplayedAboveTable(String tableName) throws Exception {
		//Waiting for the page spinner / loader to go invisible
		validateAbsenceOfElement("//lightning-spinner//span[text() = 'Loading']", 180);
		
		//Locating the message displayed above the table once the loader / spinner becomes invisible
		String xpath = "//lightning-tab[@data-id = '"+ tableName +"']//div[@class = 'hightlight-tab-message']";
		WebElement message = locateElement(xpath, 120);
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
	}

	/**
	 * Description: This will retrieve current status of tables
	 * @param factorTablesToReset: List of table names for which status is to be reset
	 * @param rollYear: Roll year for which the status needs to be reset
	 */
	public String retrieveTablesStatusForGivenRollYear(String tableName, String rollYear) throws Exception {
		objApasGenericFunctions.login(users.SYSTEM_ADMIN);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		
		clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		String xpathFieldValue = "//span[text() = '"+ tableName +"']//parent::div//following-sibling::div//span[contains(@class, 'test-id__field-value')]//lightning-formatted-text";
		WebElement element = locateElement(xpathFieldValue, 10);
		return getElementText(element);
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
	public List<String> getTextOfMultipleElementsFromProp13Table() throws Exception {
		String xpathCpiValues = "//lightning-tab[contains(@data-id, 'BPP Prop 13 Factors')]//table"
				+ "//tbody//th//lightning-formatted-text[text() = '"+ TestBase.CONFIG.getProperty("rollYear") +"']//ancestor::th"
				+ "//following-sibling::td[contains(@data-label, 'CPI Factor')]";
		List<String> listOfVisibleTextFromElements = new ArrayList<String>();
		List<WebElement> elements = locateElements(xpathCpiValues, 30);
		for(WebElement element : elements) {
			listOfVisibleTextFromElements.add(getElementText(element));
		}
		return listOfVisibleTextFromElements;
	}

	/**
	 * Checks whether calculate button is visible on BPP trend page for selected table
	 * @param: Table for which the button needs to be checked
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isCalculateBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		return checkAvailabilityOfRequiredButton("Calculate", timeToLocateBtn, tableName);
	}
	
	/**
	 * Checks whether calculate all button is visible on BPP trend page
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isCalculateAllBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("Calculate all", timeToLocateBtn);
	}
	
	/**
	 * Checks whether re-calculate button is visible on BPP trend page for selected table
	 * @param: Table for which the button needs to be checked
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isReCalculateBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		return checkAvailabilityOfRequiredButton("Recalculate", timeToLocateBtn, tableName);
	}
	
	/**
	 * Checks whether re-calculate all button is visible on BPP trend page
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isReCalculateAllBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("ReCalculate all", timeToLocateBtn);
	}
	
	/**
	 * Checks whether submit for approval button is visible on BPP trend page for selected table
	 * @param: Table for which the button needs to be checked
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isSubmitForApprovalBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		return checkAvailabilityOfRequiredButton("Submit For Approval", timeToLocateBtn, tableName);
	}
	
	/**
	 * Checks whether submit all factors for approval button is visible on BPP trend page
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isSubmitAllForApprovalBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("Submit All Factors for Approval", timeToLocateBtn);
	}
	
	/**
	 * Checks whether approve button is visible on BPP trend page for selected table
	 * @param: Table for which the button needs to be checked
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based on presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isApproveBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		return checkAvailabilityOfRequiredButton("Approve", timeToLocateBtn, tableName);
	}

	/**
	 * Checks whether approve all button is visible on BPP trend page for selected table
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based on presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isApproveAllBtnVisible(int timeToLocateBtn) throws Exception {
		return checkAvailabilityOfRequiredButton("Approve all", timeToLocateBtn);
	}

	/**
	 * Checks whether Download button is visible
	 * @param: Timeout in seconds for driver should wait until button is located
	 * @return: Returns the status as true / false based on presence / visibility of the button
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
	 * Close button to close the pop up message.
	 */
	public void closePageLevelMsgPopUp() {
		try {
			Click(locateElement("//button[@title = 'Close']", 20));
		} catch(Exception ex) {
			ex.getMessage();
		}
	}
		
	/**
	 * It wait for the pop up message to show up when calculate button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnCalculateClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'Data calculated successfully']";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when submit for approval button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnSubmitForApprovalClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'All Factors have been submitted for approval']";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnApproveClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'Data approved successfully']";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnCalculateAllClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(text(), 'Calculated all tabs successfully')]";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnSubmitAllForApprovalClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(text(), 'All Factors have been submitted for approval')]";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}
	
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnApproveAllClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(text(), 'Approved all tabs')]";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}
	
	/**
	 * Clicks on the BOE Index & Good Factors file on import transactions page
	 * @throws: Throws Exception
	 */
	public void clickOnBoeIndexAndGoodFactorsImportLog(String rollYear) throws Exception {
		WebElement importLogNameInGrid = locateElement("//a[contains(text(), 'BOE - Index and Percent Good Factors :"+ rollYear +"')]", 30);
		Click(importLogNameInGrid);
	}

	/**
	 * Clicks on the BOE Valuations file on import transactions page
	 * @throws: Throws Exception
	 */
	public void clickOnBoeValuationFactorsImportLog(String rollYear) throws Exception {
		WebElement importLogNameInGrid = locateElement("//a[contains(text(), 'BOE - Valuation Factors :"+ rollYear +"')]", 30);
		Click(importLogNameInGrid);
	}

	/**
	 * Clicks on the CAA Valuations file on import transactions page
	 * @throws: Throws Exception
	 */
	public void clickOnCaaValuationFactorsImportLog(String rollYear) throws Exception {
		WebElement importLogNameInGrid = locateElement("//a[contains(text(), 'CAA - Valuation Factors :"+ rollYear +"')]", 30);
		Click(importLogNameInGrid);
	}
	
	/**
	 * It checks whether the error rows section is displayed
	 * @return: Returns the status of error rows section based on it visibility as true / false
	 * @throws: Throws Exception
	 */
	public boolean checkPresenceOfErrorRowsSection() throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathErrorSection = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//span[contains(@title,'ERROR ROWS')]";
		return locateElement(xpathErrorSection, 30).isDisplayed();
	}

	/**
	 * It retrieves the count of total rows in error rows section
	 * @return: Returns the count of error rows
	 * @throws: Throws Exception
	 */
	public String getCountOfRowsFromErrorRowsSection() throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathErrorSection = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//span[contains(@title,'ERROR ROWS')]";
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
		return locateElement(xpathImportedSection, 30).isDisplayed();
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
		Click(discardButton);
		
		String xpathContinueButton = "//button[text()='Continue']";
		WebElement continueButton = locateElement(xpathContinueButton, 30);
		Click(continueButton);
	}
	
	/**
	 * It selects all error rows at once and discard them
	 * @throws: Throws Exception
	 */
	public void discardAllErrorRows() throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathSelectAllCheckBox = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']"
				+ "//input[@class='datatable-select-all'][@type='checkbox']/..//span[@class='slds-checkbox_faux']";
		WebElement selectAllCheckBox = locateElement(xpathSelectAllCheckBox, 30);
		Click(selectAllCheckBox);
		
		String xpathDiscardButton = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//button[text()='Discard']";
		WebElement discardButton = locateElement(xpathDiscardButton, 30);
		Click(discardButton);
		
		String xpathContinueButton = "//button[text()='Continue']";
		WebElement continueButton = locateElement(xpathContinueButton, 30);
		Click(continueButton);
	}

	/**
	 * Description: This will fill data in roll year and tables status data in bpp trend setting pop up
	 * @param bppTrendSetupDataMap: Data map containing keys as field names in setting pop up and values as values of these fields
	 * @throws: Throws Exception
	 */
	public void enterBppTrendSettingRollYearAndTableStatus(Map<String, String> bppTrendSetupDataMap) throws Exception {
		for(Map.Entry<String, String> entryData : bppTrendSetupDataMap.entrySet()) {
			String key = entryData.getKey();
			String value = entryData.getValue();
			String xpathDropDownField = "//span[text() = '"+ key +"']//parent::span//following-sibling::div//div[@class = 'uiPopupTrigger']";
			WebElement dropDownField = locateElement(xpathDropDownField, 30);
			objApasGenericPage.selectOptionFromDropDown(dropDownField, value);
		}
	}
	
	/**
	 * Description: This will fill data in roll year field in bpp trend setting
	 * @param bppSettingRollYear: BPP trend setup roll year value like 2018, 2019, 2020
	 * @throws: Throws Exception
	 */
	public void enterRollYearInBppSettingDetails(String bppSettingRollYear) throws Exception {
		Click(deleteIconForPrePopulatedRollYear);
		rollYearTxtBox.sendKeys(bppSettingRollYear);
		String xpathStr = "//mark[text() = '" + bppSettingRollYear.toUpperCase() + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 20);
		drpDwnOption.click();
	}

	/**
	 * Description: This will fill data in maximum & minimum factor field in bpp trend setting
	 * @param factorValue: Maximum or minimum factor value like 125% 
	 * @throws Exception
	 */
	public void enterFactorValue(String factorValue) throws Exception {
		waitForElementToBeClickable(factorTxtBox).sendKeys(Keys.chord(Keys.CONTROL, "a"));
		factorTxtBox.sendKeys(Keys.BACK_SPACE);
		enter(factorTxtBox, factorValue);
	}
	
	/**
	 * Description: This will fill data in property type field in bpp trend setting
	 * @param propType: Property type like 'Commercial', 'Agricultural' etc. 
	 * @throws Exception
	 */
	public void enterPropertyType(String propType) throws Exception {
		objApasGenericPage.selectOptionFromDropDown(propertyType, propType);
	}
	
	/**
	 * Description: Retrieves the count of Bpp Settings currently displayed / available
	 */
	public String getCountOfBppSettings() throws Exception {
		String xpath = "//article[contains(@class, 'slds-card slds-card_boundary')]//span[text() = 'BPP Settings']//following-sibling::span";
		Thread.sleep(2000);
		return getElementText(locateElement(xpath, 30)).substring(1, 2);
	}
	
	/**
	 * Description: Retrieves the count of Bpp Settings currently displayed / available
	 */
	public String getCountOfBppCompositeFactorSettings() throws Exception {
		String xpath = "//article[contains(@class, 'slds-card slds-card_boundary')]//span[text() = 'BPP Composite Factors Settings']//following-sibling::span";
		Thread.sleep(2000);
		return getElementText(locateElement(xpath, 30)).substring(1, 2);
	}
	
	/**
	 * Description: Retrieves the error message displayed on entering an invalid value for max equipment index factor
	 * @return: Returns the error message
	 * @throws: Exception
	 */
	public String errorMsgOnIncorrectFactorValue() throws Exception {
		String xpath = "//span[text() = 'Maximum Equipment index Factor']//parent::label//parent::div//following-sibling::ul//li";
		return getElementText(locateElement(xpath, 30));
	}
	
	/**
	 * Description: Retrieves the current status of the given table from details page under given BPP Trend Setup
	 * @param: Takes the names of the table
	 * @return: Returns the table status
	 * @throws: Exception
	 */
	public String getTableStatusFromBppTrendSetupDetailsPage(String tableName) throws Exception {
		String xpathTableStatus = "//span[text() = '"+ tableName +"']//parent::div//following-sibling::div//lightning-formatted-text";
		WebElement tableStatus = locateElement(xpathTableStatus, 30);
		return getElementText(tableStatus);
	}
	
	/**
	 * Description: Clicks on the given factor type section on details age of bpp trend setup
	 * @param factorSectionNameToOpen: Takes the names of the factor section that needs to be accessed
	 * @return: Returns the table status
	 * @throws: Exception
	 */
	public void clickOnFactorSectionOnDetailsPageOfBppTrendSetup(String factorSectionNameToOpen) throws Exception {
		switch(factorSectionNameToOpen.toUpperCase()) {
			case "BPP PROPERTY INDEX FACTORS":
				Click(bppProperyIndexFactorsTab);
			case "BPP PERCENT GOOD FACTORS":
				Click(bppProperyGoodFactorsTab);
			case "IMPORTED VALUATION FACTORS":
				Click(moreTabRightSection);
				Click(dropDownOptionBppImportedValuationFactors);
			default:
				System.out.println("Incorrect factor type has been specified.");
		}		
	}

	/**
	 * Description: It locates the new button for given factor section on bpp trend setup details page
	 * @param factorSectionNameToOpen: Takes the names of the factor section under which New button needs to be located
	 * @return: It returns New button element
	 * @throws Exception
	 */
	public WebElement getNewBtnUnderFactorSection(String factorSectionNameToOpen) throws Exception {
		String newBtnXpath = "//span[text() = '"+ factorSectionNameToOpen +"']//ancestor::header[contains(@class, 'slds-media slds-media--center')]//following-sibling::div//a[@title = 'New']";
		WebElement newButton = locateElement(newBtnXpath, 60);
		return newButton;
	}
	
	/**
	 * Description: It creates a new factor entry under given factor section
	 * @param factorSectionNameToOpen: Takes the names of the factor section under which New button needs to be located
	 * @throws Exception
	 */
	public void enterDataInFactorEntry(Map<String, String> systemInfoMap) throws Exception {
		for(Entry<String, String> entry : systemInfoMap.entrySet()) {
			String fieldName = entry.getKey();
			String fieldValue = entry.getValue();
			String xpath = null;
			WebElement element = null;
			if(fieldName.equalsIgnoreCase("Year Acquired") || fieldName.equalsIgnoreCase("Property Type") || fieldName.equalsIgnoreCase("Good Factor Type")) {
				xpath = "//span[contains(text(), '"+ fieldName +"')]//parent::span//following-sibling::div[@class = 'uiMenu']";
				element = locateElement(xpath, 30);
				Click(element);
				
				String dropDownOptionXpath = "//div[contains(@class, 'left uiMenuList--short visible positioned')]//ul//li//a[text() = "+ fieldValue +"]"; 
				WebElement drpDownElement = locateElement(dropDownOptionXpath, 10);
				javascriptClick(drpDownElement);				
			} else {
				xpath = "//span[contains(text(), '"+ fieldName +"')]//parent::label//following-sibling::input";
				element = locateElement(xpath, 30);
				enter(element, fieldValue);
				if(fieldName.toUpperCase().contains("NAME (Roll Year")) {
					System.setProperty("factorEntryName", fieldValue);
				}
			}
		}
		Click(objBuildPermit.saveButton);
	}
	
	/**
	 * Description: Checks the newly created entry in the table
	 * @return: Returns the status as true or false
	 * @throws: Exception
	 */
	public boolean isNewlyCreatedEntryVisibleInTable() throws Exception {
		String newlyCreatedEntryName = System.getProperty("factorEntryName");
		return locateElement("//a[text() = '"+ newlyCreatedEntryName +"']", 30).isDisplayed();
	}
	
	/**
	 * Description: Clicks on the show more drop down in the table for given entry.
	 * @throws: Exception
	 */
	public void clickShowMoreDropDownForGivenEntry(String newlyCreatedEntryName) throws Exception {
		String xpath = "//a[text() = '"+ newlyCreatedEntryName +"']//following::td//span[text() = 'Show More']//ancestor::a";
		WebElement showMoreDropDown = locateElement(xpath, 30);
		clickAction(showMoreDropDown);
	}
	
	/**
	 * Description: Checks the availability of Edit link under show more for given factor entry in the table
	 * @return: Returns the status as true or false
	 * @throws: Exception
	 */
	public boolean isEditLinkAvailableForNewEntry() throws Exception {
		String newlyCreatedEntryName = System.getProperty("factorEntryName");
		clickShowMoreDropDownForGivenEntry(newlyCreatedEntryName);
		return objBuildPermit.editLinkUnderShowMore.isDisplayed();
	}
	
	/**
	 * Description: Checks the availability of Delete link under show more for given factor entry in the table
	 * @return: Returns the status as true or false
	 * @throws: Exception
	 */
	public boolean isDeleteLinkAvailableForNewEntry() throws Exception {
		String newlyCreatedEntryName = System.getProperty("factorEntryName");
		clickShowMoreDropDownForGivenEntry(newlyCreatedEntryName);
		return objBuildPermit.editLinkUnderShowMore.isDisplayed();
	}
}