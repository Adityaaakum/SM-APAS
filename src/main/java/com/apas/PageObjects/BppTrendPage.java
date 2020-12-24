package com.apas.PageObjects;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
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
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;

public class BppTrendPage extends ApasGenericPage {
	Logger logger = Logger.getLogger(LoginPage.class);
	BuildingPermitPage objBuildPermitPage;
	ApasGenericPage objApasGenericPage;
	Util objUtil;
	Page objPage;
	SalesforceAPI objSFAPI;
    public Map<String, String> trendSettingsOriginalValues;
    Map<String, Integer> trendSettingRowNumbers;
	String xpathCellData = null;

	public BppTrendPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objPage = new Page(driver);
		objUtil = new Util();
		objSFAPI = new SalesforceAPI();
	}

	@FindBy(xpath = "//input[@name = 'rollyear']")
	public WebElement rollYearDropdown;

	@FindBy(xpath = "//button[@title=  'Select']")
	public WebElement selectRollYearButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@title = 'More Tabs']")
	public WebElement moreTab;

	@FindBy(xpath = "//button[contains(@class, 'slds-button_brand') and text() = 'Cancel']")
	public WebElement cancelBtnInApproveTabPopUp;

	@FindBy(xpath = "//button[contains(@class, 'slds-button_brand') and text() = 'Confirm']")
	public WebElement confirmBtnInApproveTabPopUp;

	@FindBy(xpath = "//button[text() = 'Confirm']")
	public WebElement confirmBtnInPopUp;

	@FindBy(xpath = "//div[contains(@class, 'form-footer')]//button[text() = 'Save']")
	public WebElement saveBtnToSaveEditedCellData;

	@FindBy(xpath = "//button//span[text() = 'More Actions']")
	public WebElement moreActionsBtn;

	@FindBy(xpath = "//span[text() = 'Export']//parent::a")
	public WebElement exportLinkUnderMoreActions;

	@FindBy(xpath = "//div[contains(@class, 'modal-container slds')]//input[@id = 'formatted-export']")
	public WebElement formattedReportOption;

	@FindBy(xpath = "//div[contains(@class, 'modal-container slds')]//span[text() = 'Export']")
	public WebElement exportButton;

	@FindBy(xpath = "//center//div//h2")
	public WebElement pageLevelMsg;

	@FindBy(xpath = "//lightning-tab[contains(@data-id, 'BPP Prop 13 Factor')]//input[@name = 'inputCPIFactor']")
	public WebElement cpiFactorTxtBox;

	// **** Below elements are specific to BPP Trend Setup page ****
	@FindBy(xpath = "//button[text() = 'Close']")
	public WebElement closeButton;

	@FindBy(xpath = "//div[contains(@class, 'button-container-inner slds-float_right')]//button//span[text() = 'Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//a[text() = 'No actions available']")
	public WebElement noActionsLinkUnderShowMore;

	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[@class='forceActionLink'][@role='button'][text()='Edit']")
	public WebElement editLinkUnderShowMore;

	@FindBy(xpath = "(//h1[text() = 'BPP Settings']//ancestor::div[contains(@class, 'slds-page-header')]//following-sibling::div//table//tbody/tr//th//a[1])//parent::span//parent::th//following-sibling::td//a[@role = 'button']")
	public WebElement showMoreDropDownViewAllPage;

	@FindBy(xpath = "//div[text() = 'Maximum Equipment index Factor:']/following-sibling::div//span")
	public WebElement maxEquipIndexValueOnDetailsPage;

	@FindBy(xpath = "//td[@class = 'slds-cell-edit cellContainer']//a[contains(@class, 'slds-truncate outputLookupLink')]")
	public List<WebElement> rollYears;

	@FindBy(xpath = "//td[@class = 'slds-cell-edit cellContainer']//span[@class = 'slds-truncate uiOutputNumber']")
	public List<WebElement> cpiFactors;

	@FindBy(xpath = "//input[@class = 'input uiInputSmartNumber']")
	public WebElement cpiFactorInputBox;

	@FindBy(xpath = "//input[@title = 'Search Roll Year Settings']")
	public WebElement rollYearForCpiFactor;

	@FindBy(xpath = "//ul[contains(@class, 'has-error uiInputDefaultError')]//li")
	public WebElement errorMsgForInvalidCpiFactorValue;

	@FindBy(xpath = "//div[contains(@class, 'slds-inline_icon_text--warning forceDedupeManager')]//div")
	public WebElement errorMsgOnDuplicateCpiFactor;

	@FindBy(xpath = "//div[@class = 'uiBlock']//p//span")
	public WebElement errorMsgOnEditClick;

	@FindBy(xpath = "(//div[@data-aura-class='forcePageError']//li)[2]")
	public WebElement errorMsgOnInvalidPercentGoodsYearAcquired;

	@FindBy(xpath = "//ul[contains(@class, 'has-error uiInputDefaultError')]//li")
	public WebElement errorMsgOnInvalidValuationFactorYearAcquired;

	@FindBy(xpath = "//div[contains(@class, 'headerRegion forceListViewManagerHeader')]//a[@title = 'New']")
	public WebElement newBtnViewAllPage;

	@FindBy(xpath = "(//div[@data-aura-class='forcePageError']//li)[1]")
	public WebElement showMoreLinkForEditPostApprovalOfCalculation;

	@FindBy(xpath = "//div[contains(@id, 'help-message-')]")
	public WebElement errorMsgOnImportForInvalidFileFormat;

	@FindBy(xpath = "(//span[contains(@class, 'itemTitle slds-text-body') and contains(text(), '.pdf')])[1]")
	public WebElement downloadBtnBppTrendSetupPage;

	@FindBy(xpath = "//span[text() = 'Download']")
	public WebElement downloadBtnInPopUpOnBppTrendSetupPage;

	@FindBy(xpath = "//button[@title = 'Close']")
	public WebElement closeBtnFileDownloadPage;

	@FindBy(xpath = "(//th[@scope = 'row']//span//a)[1]")
	public WebElement firstEntryInGrid;

	@FindBy(xpath = "//div[text() = 'Maximum Equipment index Factor:']/following-sibling::div//span")
	public WebElement factorValue;

	@FindBy(xpath = "//span[contains(text(), 'BPP Settings')]//ancestor::div[contains(@class, 'forceRelatedListCardHeader')]//following-sibling::div//h3//a")
	public WebElement bppSettingName;

	@FindBy(xpath = "//span[@class = 'toastMessage slds-text-heading--small forceActionsText']")
	public WebElement popUpSaveBtn;

	// Unused Elements
	@FindBy(xpath = "//lightning-button-menu[contains(@class, 'slds-dropdown')]//button[@title = 'More Tabs']")
	public WebElement moreTabsFileColumns;

	@FindBy(xpath = "//span[text() = 'BPP Trend']//parent::a")
	public WebElement bppTrendTab;

	@FindBy(xpath = "//span[text() = 'BPP Trend Setup']//parent::a")
	public WebElement bppTrendSetupTab;

	@FindBy(xpath = "//div[contains(@class, 'modal-container slds')]//input[@id = 'data-export']")
	public WebElement detailsOnlyOption;

	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//a[text() = 'Details']")
	public WebElement detailsTab;

	@FindBy(xpath = "//a[text() = 'BPP Settings']")
	public WebElement bppSettingTab;

	@FindBy(xpath = "//button[text() = 'Cancel'] | //button[contains(@class, 'slds-button_brand') and text() = 'Cancel']")
	public WebElement cancelBtnInPopUp;

	@FindBy(xpath = "//span[text() = 'Composite Factors']//parent::a")
	public WebElement dropDownOptionCompositeFactors;

	@FindBy(xpath = "//button[text() = 'Calculate all'] | //button[@title = 'Calculate all']")
	public WebElement calculateAllBtn;

	@FindBy(xpath = "//button[text() = 'Recalculate all'] | //button[@title = 'ReCalculate all']")
	public WebElement reCalculateAllBtn;

	@FindBy(xpath = "//button[text() = 'Submit All Factors for Approval'] | //button[@title = 'Submit All Factors for Approval']")
	public WebElement submitAllFactorForApprovalButton;

	@FindBy(xpath = "//button[text() = 'Approve all'] | //button[@title = 'Approve all']")
	public WebElement approveAllButton;

	@FindBy(xpath = "//span[text() = 'BPP Trend Roll Year']//parent::label//following-sibling::div//input")
	public WebElement bppTrendRollYearTxtBox;

	@FindBy(xpath = "//li[contains(@class, 'uiAutocompleteOption')]//mark[text() = '2019']//ancestor::a")
	public WebElement bppTrendRollYearDrpDownList;

	@FindBy(xpath = "//a[@title = 'Edit']")
	public WebElement editBppTrendsettingLink;

	@FindBy(xpath = "//a[@title = 'Delete']")
	public WebElement deleteBppTrendsettingLink;

	@FindBy(xpath = "//span[text() = 'Name']//parent::label//following-sibling::input")
	public WebElement systemNameInBewBppTrendSetting;

	@FindBy(xpath = "//div[@class = 'actionsContainer']//div[contains(@class, 'slds-float_right')]//button[@title = 'Save & New']")
	public WebElement saveAndNewBtnInBppSettingPopUp;

	@FindBy(xpath = "//span[text() = 'Files']//parent::span[text() = 'View All']") //ancestor::a
	public WebElement viewAllFiles;

	@FindBy(xpath = "//lightning-spinner[contains(@class, 'slds-spinner_container')]//div[contains(@class, 'slds-spinner_medium')]")
	public WebElement statusSpinner;

	@FindBy(xpath = "(//th[@data-label = 'Year Acquired'])[1]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))]")
	public WebElement firstRowDataOfTable;

	@FindBy(xpath = "//a[text() = 'BPP Property Index Factors']")
	public WebElement propertyIndexFactorsTab;

	@FindBy(xpath = "//a[text() = 'BPP Percent Good Factors']")
	public WebElement percentGoodFactorsTab;

	@FindBy(xpath = "//a[text() = 'Imported Valuation Factors']")
	public WebElement importedValuationFactorsTab;

	@FindBy(xpath = "//span[text() = 'Home']")
	public WebElement homeTab;

	@FindBy(xpath = "//lightning-primitive-cell-factory[@class = 'slds-cell-wrap slds-has-focus']//span[text() = 'Edit Average' or text() = 'Edit Factor' or text() = 'Edit Valuation Factor']//ancestor::button//lightning-primitive-icon")
	public WebElement editIconInImportPageTable;

	@FindBy(xpath = "//lightning-formatted-text[text() = 'Junk_A' or text() = 'Junk_B']")
	public WebElement tableCellWithJunkTextOnImportPageTale;

	@FindBy(xpath = "//th[@title = 'Roll Year']//a")
	public WebElement rollYearSort;

	@FindBy(xpath = "//div[contains(@class, 'LOADED forceContentFileDroppableZone')]//span[contains(@class, 'itemTitle slds-text-body')]")
	public List<WebElement> filesListOnBppTrendSetupPage;

	@FindBy(xpath = "//span[contains(@title,'ERROR ROWS')]")
	public WebElement errorRowSection;

	@FindBy(xpath = "//a[contains(@href, 'Good_Factor')]//span[@class = 'view-all-label' and text() = 'View All']")
	public WebElement viewAllBtnUnderBppPercentGoodsTab;

	@FindBy(xpath = "//a[contains(@href, 'Valuation_Factor')]//span[@class = 'view-all-label' and text() = 'View All']")
	public WebElement viewAllBtnUnderValuationFactorsTab;

	@FindBy(xpath = "//span[contains(text(), 'Filtered by all cpi factors')]")
	public WebElement sortingMessage;

	@FindBy(xpath = "(//td[@class = 'slds-cell-edit cellContainer']//span[@class = 'slds-truncate uiOutputNumber'])[1]")
	public WebElement firstCpiFactorValue;

	@FindBy(xpath = "//table[contains(@class, 'slds-table--resizable-cols uiVirtualDataTable')]")
	public WebElement tableSection;

	@FindBy(xpath = "((//lightning-formatted-text[text() = 'Junk_A'] | //lightning-formatted-text[text() = 'Junk_B'])[1])//ancestor::td//preceding-sibling::td[2]")
	public WebElement cellAdjacentToErrorDataCellOnImportPage;

	@FindBy(xpath = "//lightning-tab[@data-id = 'BPP Prop 13 Factors']//th[@data-label = 'Roll Year']//lightning-formatted-text")
	public List<WebElement> prop13RollYearColumnList;

	@FindBy(xpath = "//lightning-tab[@data-id = 'BPP Prop 13 Factors']//td[@data-label = 'Year Acquired']//lightning-formatted-text")
	public List<WebElement> prop13YearAcquiredColumnList;

	@FindBy(xpath = "//lightning-tab[@data-id = 'BPP Prop 13 Factors']//td[@data-label = 'CPI Factor (Rounded)']//lightning-formatted-text")
	public List<WebElement> cpiRoundedValuesList;

	@FindBy(xpath = "//lightning-tab[@data-id = 'BPP Prop 13 Factors']//td[@data-label = 'Prop 13 Factor (Rounded)']//lightning-formatted-text")
	public List<WebElement> prop13RoundedValuesList;

	@FindBy(xpath = "//lightning-tab[@data-id = 'BPP Prop 13 Factors']//input[@disabled]")
	public WebElement disabledCpiInputField;

	@FindBy(xpath = "//div[text()='Already approved']")
	public WebElement alreadyApprovedLabel;

	@FindBy(xpath = "//button[text() = 'Export Composite Factors']")
	public WebElement exportCompositeFactorsBtn;

	@FindBy(xpath = "//button[text() = 'Export Valuation Factors']")
	public WebElement exportValuationFactorsBtn;

	@FindBy(xpath = "//div[@role='group']/button[text()='Edit']/../div/button")
	public WebElement arrowButton;

	@FindBy(xpath = "//a[@role='menuitem']/span[@title='Export']")
	public WebElement linkExport;

	@FindBy(xpath = "//lightning-tab[contains(@class,'slds-show')]//button[@title = 'Calculate']")
	public WebElement calculateBtn;

	@FindBy(xpath = "//lightning-tab[contains(@class,'slds-show')]//button[@title = 'Recalculate']")
	public WebElement reCalculateBtn;

	@FindBy(xpath = "//lightning-tab[contains(@class,'slds-show')]//div[@class = 'hightlight-tab-message']")
	public WebElement tableMessage;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div[@class='warning']//h2")
	public WebElement reCalculateWarningMessage;

	public String xPathBPPSettingName = "//span[contains(text(), 'BPP Settings')]//ancestor::div[contains(@class, 'forceRelatedListCardHeader')]//following-sibling::div//h3//a";

	public String xpathRollYear = "//input[@name = 'rollyear']";

	public String xpathAlreadyApprovedLabel = "//div[text()='Already approved']";

	public String xPathExportCompositeFactorsButton = "//button[text() = 'Export Composite Factors']";

	public String xPathExportValuationFactorsButton = "//button[text() = 'Export Valuation Factors']";

	public String xPathCalculateAllBtn = "//button[@title = 'Calculate all']";

	public String xPathReCalculateAllBtn = "//button[@title = 'ReCalculate all']";

	public String xPathCalculateBtn = "//lightning-tab[contains(@class,'slds-show')]//button[@title = 'Calculate']";

	public String xPathReCalculateBtn = "//lightning-tab[contains(@class,'slds-show')]//button[@title = 'Recalculate']";

	public String xpathErrorMsgPopUp = "//span[@class = 'toastMessage forceActionsText']";

	public String xpathSuccessMsgPopUp = "//div[contains(@class, 'toastContent')]//span[text() = 'Data calculated successfully']";

	public String  xpathTableMessage = "//lightning-tab[contains(@class,'slds-show')]//div[@class = 'hightlight-tab-message']";

	public String xpathSpinner = "//lightning-spinner";

	public String xpathSubmitAllFactorsForApprovalBtn = "//button[text() = 'Submit All Factors for Approval'] | //button[@title = 'Submit All Factors for Approval']";
	/**
	 * Description: This will select the roll year from the drop down
	 * @param rollYear: Roll Year to select from drop down
	 * @throws: Exception
	 */
	public void clickOnGivenRollYear(String rollYear) throws Exception {
		String xpathStr = "//div[contains(@id,'dropdown-element')]//span[contains(text(),'" + rollYear + "')]";
		WebElement element = locateElement(xpathStr, 20);
		waitForElementToBeClickable(element, 20);
		Click(element);
	}

	/**
	 * Description: This will click on the given table name
	 * @param tableName: Name of the table
	 * @throws: Exception
	 */
	public void clickOnTableOnBppTrendPage(String tableName) throws Exception {
//		Thread.sleep(2000);
		String xpath = "//a[text() = '"+ tableName +"']";

		if (verifyElementExists(xpath)){
			Click(driver.findElement(By.xpath(xpath)));
		}else{
			Click(moreTab);
			xpath = "//span[contains(text(), '"+ tableName +"')]";
			Click(driver.findElement(By.xpath(xpath)));
		}
	}

	/**
	 * Description: This will click on the given table name
	 * @param tableName: Name of the table
	 * @throws: Exception
	 */
	public boolean isTableDataVisible(String tableName, int timeOut) throws Exception {
		String tableXpath = "//lightning-tab[@data-id = '"+ tableName +"']//table";
		return verifyElementVisible(tableXpath);
	}

	/**
	 * Description: Locates the cell text box in grid
	 * @param tableName: Name of the table for which cell text box needs to be located
	 * @param rowNum: Row number in the table
	 * @param cellNum: Cell number in the table
	 * @return WebElement: Returns the cell data text box element
	 * @throws: Exception
	 */
	public WebElement locateCellToBeEdited(String tableName, int rowNum, int cellNum) throws Exception {
		String tagNameToAppend = null;

		if(tableName.equals("BPP Prop 13 Factors")) {
			tagNameToAppend = "td";
		} else {
			tagNameToAppend = "th";
		}

		xpathCellData = "((//lightning-tab[@data-id = '"+ tableName +"']//"+ tagNameToAppend +"[@data-label = 'Year Acquired'])["+ rowNum +"]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])["+ cellNum +"]";
		WebElement element=locateElement(xpathCellData, 30); ;	
		return element;
	}

	/**
	 * Description: Locates the edit button in the cell data text box
	 * @return WebElement: Returns the edit button element
	 * @throws: Exception
	 */
	public WebElement locateEditButtonInFocusedCell() throws Exception {
		WebElement editButton;
		Click(waitForElementToBeClickable(10, this.xpathCellData));
		String xpathEditBtn = "//td[contains(@class, 'has-focus')]//button[contains(@class, 'cell-edit')]//lightning-primitive-icon";
		editButton = locateElement(xpathEditBtn, 10);
		return editButton;
	}

	/**
	 * Description: Below method is used the edit the cell data in the table displayed for select roll year.
	 * @param data: Takes the integer / double value to enter in the cell
	 * @param tableName: Name of the table for which data needs to be updated
	 * @throws: Exception
	 */
	public void editCellDataInGridForGivenTable(String tableName, Object data) throws Exception {
		String xpathEditTxtBox = "//div//input[@name = 'dt-inline-edit-text']";
		WebElement editTxtBox;
		editTxtBox = locateElement(xpathEditTxtBox, 30);
		waitForElementToBeClickable(15, editTxtBox);
		((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid green'", editTxtBox);
		editTxtBox.clear();
		WebElement editBtn = locateEditButtonInFocusedCell();
		Click(editBtn);
		Thread.sleep(1000);
		editTxtBox = locateElement(xpathEditTxtBox, 30);
		editTxtBox.sendKeys(String.valueOf(data));
		Thread.sleep(1000);

		String tagAppend;
		if(tableName.equalsIgnoreCase("BPP Prop 13 Factors")) {
			tagAppend = "td";
		} else {
			tagAppend = "th";
		}

		WebElement year = locateElement("(//lightning-tab[@data-id = '"+ tableName +"']//"+ tagAppend +"[@data-label = 'Year Acquired'])[1]", 20);
		Click(year);
	}

	/**
	 * Description: Checks whether edited cell appears in edit mode
	 * @return boolean: Return status as true or false
	 * @throws: Exception
	 */
	public boolean isEditedCellFocused(WebElement editedCell) throws Exception {
		wait.until(ExpectedConditions.attributeContains(editedCell, "class", "slds-is-edited"));
		return (editedCell.getAttribute("class").contains("slds-is-edited"));
	}

	/**
	 * Description: This will retrieve the message displayed above table data on performing calculate / recalculate / submit for approval
	 * @param tableName: Name of the table
	 * @return : Message displayed above the table post performed action is complete
	 */
	public String retrieveMsgDisplayedAboveTable(String tableName) throws Exception {
		//Waiting for the page spinner to become visible and if visible then wait for it to go invisible
		String xpath = "//lightning-spinner//span[text() = 'Loading']";
		if(verifyElementVisible(xpathSpinner))
		waitForElementToDisappear(xpathSpinner, 50);

		//Locating the message displayed above the table once the loader / spinner becomes invisible
		xpath = "//lightning-tab[@data-id = '"+ tableName +"']//div[@class = 'hightlight-tab-message']";
		WebElement message = locateElement(xpath, 90);
		return getElementText(message);
	}

	/**
	 * Description: This will reset the status of tables based on the expected status
	 * @param expectedStatus: Expected status like Calculated, Not Calculated etc.
	 * @param rollYear: Roll year for which the status needs to be reset
	 */
	public void resetTablesStatusForGivenRollYear(List<String> columnsToReset, String expectedStatus, String rollYear) throws Exception {
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		//Query to update the status of composite & valuation factor tables
		String queryForID = "Select Id From BPP_Trend_Roll_Year__c where Roll_Year__c = '"+ rollYear +"'";

		JSONObject jsonObj = new JSONObject();
		for(String columnName : columnsToReset) {
			jsonObj.put(columnName, expectedStatus);
		}
		objSalesforceAPI.update("BPP_Trend_Roll_Year__c", queryForID, jsonObj);
	}

	/**
	 * Description: This will update maximum equipment index factor value
	 * @param expectedValue: Expected value like 125
	 * @param rollYear: Roll year for which the value needs to be reset, like '2020'
	 */
	public void updateMaximumEquipmentIndexFactorValue(String expectedValue, String rollYear) throws Exception {
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		//Below query is to set the maximum equipment index factor value to its default values
		String queryForMaxEquipFactor = "Select Id FROM BPP_Setting__c Where BPP_Trend_Roll_Year_Parent__c = '"+ rollYear +"'";
		objSalesforceAPI.update("BPP_Setting__c", queryForMaxEquipFactor, "Maximum_Equipment_index_Factor__c", expectedValue);
	}

	/**
	 * Description: This will update minimum equipment index factor value
	 * @param propertyType: Name of the property for which value needs to be updated, like 'Commercial'
	 * @param expectedValue: Expected value like 10
	 * @param rollYear: Roll year for which the value needs to be reset, like '2020'
	 */
	public void updateMinimumEquipmentIndexFactorValue(String propertyType, String expectedValue, String rollYear) throws Exception {
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		//Below query is to set the minimum equipment index factor value to its default values
		String queryForMinGoodsFactor = "Select Id FROM BPP_Composite_Factors_Setting__c Where BPP_Trend_Roll_Year_Parent__c = '"+rollYear+"' and Property_Type__c = '"+propertyType+"'";
		objSalesforceAPI.update("BPP_Composite_Factors_Setting__c", queryForMinGoodsFactor, "Minimum_Good_Factor__c", expectedValue);
	}

	/**
	 * Description: This will delete the existing BPP Setting entry (Max. Equip. Index) for given roll year
	 * @param rollYear:
	 * @throws: Exception
	 */
	public void removeExistingBppSettingEntry(String rollYear) throws Exception {
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		String queryForID = "SELECT Id FROM BPP_Setting__c where BPP_Trend_Roll_Year_Parent__c = '"+ rollYear +"'";
		objSalesforceAPI.delete("BPP_Setting__c", queryForID);
	}

	/**
	 * Description: This will delete the existing BPP Composite Factor Setting entries (Min. Equip. Index) for given roll year
	 * @param rollYear:
	 * @throws: Exception
	 */
	public void removeExistingBppFactorSettingEntry(String rollYear) throws Exception {
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		String queryForID = "SELECT Id FROM BPP_Composite_Factors_Setting__c Where BPP_Trend_Roll_Year_Parent__c = '"+ rollYear +"'";
		objSalesforceAPI.delete("BPP_Composite_Factors_Setting__c", queryForID);
	}

	/**
	 * Description: Exports the valuation or composite factors excel files based on parameter passed
	 * @param: fileType: Type of File. For e.g. Composite or Valuation Factors
	 * @throws: Exception
	 */
	public void exportCompositeOrValuationFactorsFiles(String fileType) throws Exception {
		// Click on Export button corresponding to File Type
		String parentwindow = driver.getWindowHandle();

		if(fileType.equalsIgnoreCase("Composite")) {
			objPage.Click(exportCompositeFactorsBtn);
		}else {
			objPage.Click(exportValuationFactorsBtn);
		}
		Thread.sleep(15000);
		// After clicking on 'export' button, new tab is opened
		switchToNewWindow(parentwindow); // Switch to new tab opened

		// Clicking more action button and then selecting Export option from drop down to open export option window
		Thread.sleep(3000);
		driver.switchTo().frame(0);
		System.out.println("after: "+arrowButton.getText());
		Click(arrowButton);
		Click(linkExport);
		driver.switchTo().defaultContent();

		// Selecting the export option and clicking export button to initiate export
		javascriptClick(formattedReportOption);
		javascriptClick(exportButton);
		Thread.sleep(10000);
		driver.close();
	}

	/**
	 * Description : Reads tabular grid data from UI
	 * @param tableName: Takes the name of the table for which data needs to be read
	 * @return Map: Return a data map
	 **/
	public Map<String, List<Object>> retrieveDataFromGridForGivenTable(String tableName) throws Exception {
		Map<String, List<Object>> uiTableDataMap = new LinkedHashMap<String, List<Object>>();

		String xpathTableRows = "//lightning-tab[contains(@data-id, '"+ tableName +"')]//table//tbody//tr";
		String xpathTableData = "//td[(not(contains(@data-label, 'Year Acquired'))) "
				+ "and (not(contains(@data-label, 'Year Acquired')) and not (contains(@data-label, 'Rounded')))]";
		List<WebElement> tableRows = locateElements(xpathTableRows, 30);
		for(int i = 0; i < tableRows.size(); i++) {
			int rowNum = i + 1;
			String xpathYearAcq;
			if(!tableName.contains("BPP Prop 13 Factors")) {
				xpathYearAcq = "("+ xpathTableRows +")["+ rowNum +"]//th";
			}
			else {
				xpathYearAcq = "("+ xpathTableRows +")["+ rowNum +"]//td[3]";
			}
			WebElement yearAcquiredElement = locateElement(xpathYearAcq, 30);
			String yearAcquiredTxt = getElementText(yearAcquiredElement);

			String xpathYearData = "("+ xpathTableRows +")["+ rowNum +"]"+ xpathTableData;
			List<WebElement> yearAcquiredDataElements = locateElements(xpathYearData, 10);

			List<Object> yearAcquiredData = new ArrayList<Object>();
			for(int j = 0; j < yearAcquiredDataElements.size(); j++) {
				Object cellData;
				if(!tableName.contains("BPP Prop 13 Factors")) {
					cellData = (int)Double.parseDouble(getElementText(yearAcquiredDataElements.get(j)));
				}
				else {
					cellData = (int)Math.round(Double.parseDouble(getElementText(yearAcquiredDataElements.get(j))));

				}
				yearAcquiredData.add(cellData);

			}
			uiTableDataMap.put(yearAcquiredTxt, yearAcquiredData);
		}
		return uiTableDataMap;
	}

	/**
	 * Description : Reads given excel file and converts the data into a map.
	 * @param filePath: Takes the path of the XLSX workbook
	 * @param sheetName: Takes the names of the Sheet that is be read from given workbook
	 * @return Map: Return a data map
	 **/
	@SuppressWarnings("deprecation")
	public Map<String, List<Object>> retrieveDataFromExcelForGivenTable(String filePath, String sheetName) throws Exception {
		String sheetNameForExcel = "";
		switch (sheetName) {
        case "Commercial Composite Factors":
        	sheetNameForExcel = "Commercial Composite";
            break;
        case "Industrial Composite Factors":
        	sheetNameForExcel = "Industrial Composite";
            break;
        case "Agricultural Composite Factors":
        	sheetNameForExcel = "Agricultural Composite";
            break;
        case "Construction Composite Factors":
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

		Map<String, List<Object>> dataMap = new LinkedHashMap<String, List<Object>>();
		FileInputStream file = null;
		XSSFWorkbook workBook = null;
		try {
            file = new FileInputStream(new File(filePath));
            workBook = new XSSFWorkbook(file);
            XSSFSheet sheet = workBook.getSheet(sheetNameForExcel);
            DataFormatter dataFormatter = new DataFormatter();

            int startingRow;
            Row headerRow;
            if(sheetNameForExcel.equalsIgnoreCase("Ag Mobile Equip Composite")) {
            	startingRow = 2;
            	headerRow = sheet.getRow(1);
            } else {
            	startingRow = 1;
            	headerRow = sheet.getRow(0);
            }

            int totalCells = headerRow.getLastCellNum();
            int cellIndexToSkip = -1;
            int cellIndexForYearAcq = 0;
            for(int i = 0; i < totalCells; i++) {
            	String cellValue = dataFormatter.formatCellValue(headerRow.getCell(i));
            	if(cellValue.toUpperCase().trim().equals("AGE") || cellValue.toUpperCase().contains("ACQ. DURING")) {
            		cellIndexToSkip = i;
            	}
            	if(cellValue.toUpperCase().contains("YEAR ACQUIRED") || cellValue.toUpperCase().contains("YEAR") || cellValue.toUpperCase().contains("YEAR ACQ.")) {
            		cellIndexForYearAcq = i;
            	}
            }

            int rowCount = sheet.getPhysicalNumberOfRows();
            for(int i = startingRow; i < rowCount; i++) {
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
                			int intValue = (int) Math.round((double)strValue);
                			currentRowData.add(intValue);
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
	 * Description: Return the list containing the names of columns visible of UI
	 * @param tableName: Takes the name of the table
	 * @return List: Returns a list of column names displayed in table grid
	 * @throws: Exception
	 */
	public List<String> retrieveColumnNamesOfGridForGivenTable(String tableName) throws Exception {
		List<String> columnNames = new ArrayList<String>();
		String xpathColNames = "//lightning-tab[contains(@data-id, '"+ tableName +"')]"
				+ "//slot//th[@scope = 'col' and (not(contains(@aria-label, 'Year Acquired')))]//span[@class = 'slds-truncate']";
		List<WebElement> totalColumnsList = locateElements(xpathColNames, 10);

		for(WebElement column : totalColumnsList) {
			String columnName = getElementText(column);
			columnNames.add(columnName);
		}
		return columnNames;
	}

	/**
	 * Description: Returns a list containing visible text of web elements found by given xpath string
	 * @param columnName: Takes the name of the column
	 * @param timeOutInSeconds: Takes time in seconds
	 * @return List: Returns a list of String having the visible text of elements found by given xpath
	 * @throws: Exception
	 */
	public List<String> retrieveTableDataForGivenColumn(String columnName, int timeOutInSeconds) throws Exception {
		String xpathCpiValues = "//table//tbody//th//ancestor::th//following-sibling::td[@data-label = '"+  columnName +"']";
		List<String> listOfVisibleTextFromElements = new ArrayList<String>();
		List<WebElement> elements = locateElements(xpathCpiValues, timeOutInSeconds);
		for(WebElement element : elements) {
			listOfVisibleTextFromElements.add(getElementText(element));
		}
		return listOfVisibleTextFromElements;
	}

	/**
	 * Checks whether calculate button is visible on BPP trend page for selected table
	 * @param tableName: Table for which the button needs to be checked
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isCalculateBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		String xpath = "//lightning-tab[@data-id = '"+ tableName + "']//button[@title = 'Calculate']";
		return verifyElementVisible(xpath);
	}


	/**
	 * Checks whether calculate button is visible on BPP trend page for selected table
	 * @param tableName: Table for which the button needs to be checked
	 * @return boolean: Returns the status as true / false based on visibility of the button
	 */
	public boolean isCalculateButtonVisible(String tableName) throws Exception {
		return isCalculateBtnVisible(5,tableName);
	}

	/**
	 * Checks whether re-calculate button is visible on BPP trend page for selected table
	 * @param tableName: Table for which the button needs to be checked
	 * @return boolean: Returns the status as true / false based on visibility of the button
	 */
	public boolean isReCalculateButtonVisible(String tableName) throws Exception {
		return isReCalculateBtnVisible(5,tableName);
	}

	/**
	 * Checks whether calculate all button is visible on BPP trend page
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isCalculateAllBtnVisible(int timeToLocateBtn) throws Exception {
		String xpath = "//button[@title = 'Calculate all']";
		return verifyElementVisible(xpath);
	}

	/**
	 * Checks whether re-calculate button is visible on BPP trend page for selected table
	 * @param tableName: Table for which the button needs to be checked
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isReCalculateBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		String xpath = "//lightning-tab[@data-id = '"+ tableName + "']//button[@title = 'Recalculate']";
		return verifyElementVisible(xpath);
	}

	/**
	 * Checks whether re-calculate all button is visible on BPP trend page
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isReCalculateAllBtnVisible(int timeToLocateBtn) throws Exception {
		String xpath = "//button[@title = 'ReCalculate all']";
		return verifyElementVisible(xpath);
	}

	/**
	 * Checks whether submit all factors for approval button is visible on BPP trend page
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based on visibility of the button
	 * @throws: Exception
	 */
	public boolean isSubmitAllFactorsForApprovalBtnVisible(int timeToLocateBtn) throws Exception {
		String xpath = "//button[@title = 'Submit All Factors for Approval']";
		return verifyElementVisible(xpath);
	}

	/**
	 * Checks whether approve button is visible on BPP trend page for selected table
	 * @param tableName: Table for which the button needs to be checked
	 * @return boolean: Returns the status as true / false based on presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isApproveButtonVisible(String tableName) throws Exception {
		return isApproveBtnVisible(5,tableName);
	}

	/**
	 * Checks whether approve button is visible on BPP trend page for selected table
	 * @param tableName: Table for which the button needs to be checked
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based on presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isApproveBtnVisible(int timeToLocateBtn, String tableName) throws Exception {
		String xpath = "//lightning-tab[@data-id = '"+ tableName + "']//button[@title = 'Approve']";
		return verifyElementVisible(xpath);
	}

	/**
	 * Checks whether approve all button is visible on BPP trend page for selected table
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based on presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isApproveAllBtnVisible(int timeToLocateBtn) throws Exception {
		String xpath = "//button[@title = 'Approve all']";
		return verifyElementVisible(xpath);
	}

	/**
	 * Checks whether Download button is visible
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based on presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isDownloadBtnVisible(int timeToLocateBtn) throws Exception {
		String xpath = "//button[text() = 'Download']";
		return verifyElementVisible(xpath);
	}

	/**
	 * Checks whether Export Composite Factors button is visible
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based of presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isExportCompositeFactorsBtnVisible(int timeToLocateBtn) throws Exception {
		String xpath = "//button[text() = 'Export Composite Factors']";
		return verifyElementVisible(xpath);
	}

	/**
	 * Checks whether Export Valuation Factors button is visible
	 * @param timeToLocateBtn: Timeout in seconds for driver should wait until button is located
	 * @return boolean: Returns the status as true / false based of presence / visibility of the button
	 * @throws: Exception
	 */
	public boolean isExportValuationFactorsBtnVisible(int timeToLocateBtn) throws Exception {
		String xpath = "//button[text() = 'Export Valuation Factors']";
		return verifyElementVisible(xpath);
	}

	/**
	 * Description: Checks whether pencil icon to edit table status on BPP Trend Status page is visible
	 * @return tableName: Return status of pencil icon as true or false
	 * @throws: Exception
	 */
	public boolean isPencilIconToEditTableStatusVisible(String tableName) throws Exception {
		String xpath = "//span[text() = '"+ tableName +"']//parent::div//following-sibling::div//button[contains(@class, 'test-id__inline-edit-trigger slds-shrink')]";
		return verifyElementVisible(xpath);
	}

	/**
	 * Clicks the Calculate button for selected table
	 * @param tableName: Takes the names of table for which button is to be clicked
	 * @throws: Exception
	 */
	public void clickCalculateBtn(String tableName) throws Exception {
		String xpath = "//lightning-tab[@data-id = '"+ tableName + "']//button[@title = 'Calculate']";
		Click(locateElement(xpath, 5));
	}

	/**
	 * Clicks the ReCalculate button for selected table
	 * @param tableName: Takes the names of table for which button is to be clicked
	 * @throws: Exception
	 */
	public void clickReCalculateBtn(String tableName) throws Exception {
		String xpath = "//lightning-tab[@data-id = '"+ tableName + "']//button[@title = 'Recalculate']";
		Click(locateElement(xpath, 5));
	}

	/**
	 * Clicks the Approve button for selected table
	 * @param tableName: Takes the names of table for which button is to be clicked
	 * @throws: Exception
	 */
	public void clickApproveButton(String tableName) throws Exception {
		String xpath = "//lightning-tab[@data-id = '"+ tableName + "']//button[@title = 'Approve']";
		Click(locateElement(xpath, 5));
	}

	/**
	 * Clicks the Calculate All button
	 * @throws: Exception
	 */
	public void clickCalculateAllBtn() throws Exception {
		String xpath = "//button[@title = 'Calculate all']";
		Click(locateElement(xpath, 20));
	}

	/**
	 * Clicks the ReCalculate All button
	 * @throws: Exception
	 */
	public void clickReCalculateAllBtn() throws Exception {
		String xpath = "//button[@title = 'ReCalculate all']";
		Click(locateElement(xpath, 20));
	}

	/**
	 * Clicks the Submit All Factors For Approval button
	 * @throws: Exception
	 */
	public void clickSubmitAllFactorsForApprovalBtn() throws Exception {
		String xpath = "//button[@title = 'Submit All Factors for Approval']";
		Click(locateElement(xpath, 20));
	}

	/**
	 * Clicks the Approve All button
	 * @throws: Exception
	 */
	public void clickApproveAllBtn() throws Exception {
		String xpath = "//button[@title = 'Approve all']";
		Click(locateElement(xpath, 20));
	}

	/**
	 * Clicks the Download button
	 * @throws: Exception
	 */
	public void clickDownloadBtn() throws Exception {
		String xpath = "//button[text() = 'Download']";
		Click(locateElement(xpath, 20));
		Thread.sleep(10000);
	}

	/**
	 * Close button to close the pop up message.
	 */
	public void closePageLevelMsgPopUp() {
		try {
			WebElement pageLevelPopUpCloseBtn = locateElement("//button[@title = 'Close']", 30);
			waitForElementToBeClickable(pageLevelPopUpCloseBtn, 10);
			javascriptClick(pageLevelPopUpCloseBtn);
		} catch(Exception ex) {
			ex.getMessage();
		}
	}

	/**
	 * It wait for the pop up message to show up when calculate button is clicked
	 * @param timeToLocateElemInSec: Timeout for which pop up message needs to be located
	 */
	public String waitForSuccessPopUpMsgOnCalculateClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'Data calculated successfully']";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}

	/**
	 * Description: Retrieve the error message displayed in pop up on Calculate button's click
	 * @param timeToLocateElemInSec:
	 * @return String: Return the error message displayed in pop up
	 * @throws: Exception
	 */
	public String waitForErrorPopUpMsgOnCalculateClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//span[@class = 'toastMessage forceActionsText']";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}

	/**
	 * Description: Retrieve the error message displayed in pop up on Calculate button's click
	 * @param timeToLocateElemInSec:
	 * @return String: Return the error message displayed in pop up
	 * @throws: Exception
	 */
	public String getErrorMsgFromPopUp(int timeToLocateElemInSec) throws Exception {
		String popUpMsg = getElementText(locateElement(xpathErrorMsgPopUp, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}

	/**
	 * It wait for the pop up message to show up when calculate button is clicked
	 * @param timeToLocateElemInSec: Timeout for which pop up message needs to be located
	 */
	public String getSuccessMessageFromPopUp(int timeToLocateElemInSec) throws Exception {
		String popUpMsg = getElementText(locateElement(xpathSuccessMsgPopUp, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;
	}
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param timeToLocateElemInSec: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnApproveClick(int timeToLocateElemInSec) throws Exception {
		String xpathErrorMsg = "//div[contains(@class, 'toastContent')]//span[text() = 'Roll year flag name and roll year must be present']";
		if(!verifyElementVisible(xpathErrorMsg)) {
			String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'Data approved successfully']";
			String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
			closePageLevelMsgPopUp();
			return popUpMsg;
		} else {
			return getElementText(locateElement(xpathErrorMsg, 5));
		}
	}

	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param timeToLocateElemInSec: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnCalculateAllClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(text(), 'Calculated all tabs successfully')]";
		WebElement popUpOnButtonClick = locateElement(xpath, timeToLocateElemInSec);
		if(popUpOnButtonClick.isDisplayed()) {
			String popUpMsg = getElementText(popUpOnButtonClick);
			closePageLevelMsgPopUp();
			return popUpMsg;
		} else {
			return null;
		}
	}

	/**
	 * Description: Retrieves the warning message displayed in ReCalculate pop up
	 * @return String: Return the warning message
	 * @throws: Exception
	 */
	public String retrieveReCalculatePopUpMessage() throws Exception {
		String xpath = "//h2[contains(text(), '"+ TestBase.CONFIG.getProperty("recalculatePopUpMsg") +"')]";
		return getElementText(locateElement(xpath, 10));
	}

	/**
	 * Description: Retrieves the warning message displayed on clicking approve button without saving edited data
	 * @return String: Return the warning message
	 * @throws: Exception
	 */
	public String retrieveApproveTabDataPopUpMessage() throws Exception {
		String xpath = "//h2[contains(text(), '"+ TestBase.CONFIG.getProperty("approveTabDataMsg") +"')]";
		return getElementText(locateElement(xpath, 10));
	}


//	public void deleteDuplicateCPI(String rollYear) {
//		String queryForRollYearId = "SELECT Id FROM Roll_Year_Settings__c Where Name = '"+rollYear+"'";
//		HashMap<String, ArrayList<String>> rollYearId = objSFAPI.select(queryForRollYearId);
//		String queryForDuplicateCPIFactor = "SELECT Id FROM CPI_Factor__c  Where Roll_Year__c ='"+rollYearId.get("Id").get(0)+"'  AND Status__c<>'Approved'";
//		objSFAPI.delete("CPI_Factor__c", queryForDuplicateCPIFactor);
//	}

	/**
	 * Description: Searches module BPP Trends module and Select the Roll Year passed as an argument
	 * @param: Takes roll year to be selected as an argument
	 * @throws: Exception
	 */
	public void selectRollYearOnBPPTrends(String rollYear) throws Exception {
		searchModule(modules.BPP_TRENDS);
		objPage.waitForElementToBeClickable(rollYearDropdown, 30);
		Click(rollYearDropdown);
		clickOnGivenRollYear(rollYear);
		Click(selectRollYearButton);
		Thread.sleep(2000);	
	}
	
	/**
	 * Description: This will update the status of tables based on the expected status
	 * @param expectedStatus: Expected status like Calculated, Not Calculated etc.
	 * @param rollYear: Roll year for which the status needs to be reset
	 */
	public void updateTablesStatusForGivenRollYear(String columnsToReset, String expectedStatus, String rollYear) throws Exception {	
		List<String> tablesToupdate = Arrays.asList(columnsToReset.split(","));
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		//Query to update the status of composite & valuation factor tables
		String queryForID = "Select Id From BPP_Trend_Roll_Year__c where Roll_Year__c = '"+ rollYear +"'";
		
		JSONObject jsonObj = new JSONObject();
		for(String columnName : tablesToupdate) {
			jsonObj.put(columnName, expectedStatus);
		}
		objSalesforceAPI.update("BPP_Trend_Roll_Year__c", queryForID, jsonObj);
	}
	
	/**
	 * Description: Waits until the page spinner goes invisible within given timeout
	 * @param: Takes Xpath as an argument
	 * @throws: Exception
	 */
	public void waitForPageSpinnerToDisappear(int...timeOutInSeconds) throws Exception {
		String xpath = "//lightning-spinner[contains(@class, 'slds-spinner_container')]//div[contains(@class, 'slds-spinner')]";		
		WebElement element;
		if(timeOutInSeconds.length == 0) {
			element = locateElement(xpath, 30);
		} else {
			element = locateElement(xpath, timeOutInSeconds[0]);
		}
		
		if(element != null) {
			for(int i = 0; i < 500; i++) {
				try{
					element = driver.findElement(By.xpath(xpath));
					Thread.sleep(100);
				} catch (Exception ex) {
					break;		
				}
			}
		}
	}
	
}