package com.apas.PageObjects;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;

public class BppTrendPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);
	BuildingPermitPage objBuildPermitPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	Util objUtil;
	Page objPage;
	
    public Map<String, String> trendSettingsOriginalValues;
    Map<String, Integer> trendSettingRowNumbers;
	
	public BppTrendPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objPage = new Page(driver);
		objUtil = new Util();
	}
	
	// **** Below elements are specific to BPP Trend page ****
	@FindBy(xpath = "//span[text() = 'BPP Trend']//parent::a")
	public WebElement bppTrendTab;

	@FindBy(xpath = "//span[text() = 'BPP Trend Setup']//parent::a")
	public WebElement bppTrendSetupTab;
	
	@FindBy(xpath = "//input[@name = 'rollyear']")
	public WebElement rollYearDropdown;

	@FindBy(xpath = "//button[@title=  'Select']")
	public WebElement selectRollYearButton;
	
	@FindBy(xpath = "//div[@class = 'dv-tab-bppt-container']//button[@title = 'More Tabs']")
	public WebElement moreTabs;
	
	@FindBy(xpath = "//lightning-button-menu[contains(@class, 'slds-dropdown')]//button[@title = 'More Tabs']")
	public WebElement moreTabsFileColumns;
	
	@FindBy(xpath = "//button[contains(@class, 'slds-button_brand') and text() = 'Cancel']")
	public WebElement cancelBtnInApproveTabPopUp;
	
	@FindBy(xpath = "//button[contains(@class, 'slds-button_brand') and text() = 'Confirm']")
	public WebElement confirmBtnInApproveTabPopUp;
	
	@FindBy(xpath = "//button[text() = 'Cancel'] | //button[contains(@class, 'slds-button_brand') and text() = 'Cancel']")
	public WebElement cancelBtnInPopUp;
	
	@FindBy(xpath = "//button[text() = 'Confirm']")
	public WebElement confirmBtnInPopUp;
	
	@FindBy(xpath = "//lightning-spinner[contains(@class, 'slds-spinner_container')]//div[contains(@class, 'slds-spinner')]")
	public WebElement statusSpinner;
	
	@FindBy(xpath = "(//th[@data-label = 'Year Acquired'])[1]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))]")
	public WebElement firstRowDataOfTable;
	
	@FindBy(xpath = "//div[contains(@class, 'form-footer')]//button[text() = 'Save']")
	public WebElement saveBtnToSaveEditedCellData;
	
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
	
	@FindBy(xpath = "//button[text() = 'Calculate all'] | //button[@title = 'Calculate all']")
	public WebElement calculateAllBtn;

	@FindBy(xpath = "//button[text() = 'Recalculate all'] | //button[@title = 'ReCalculate all']")
	public WebElement reCalculateAllBtn;
	
	// **** Below elements are specific to BPP Trend Setup page ****
	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//a[text() = 'Details']")
	public WebElement detailsTab;
	
	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//a[text() = 'BPP Property Index Factors']")
	public WebElement bppPropertyIndexFactorsTab;
	
	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'BPP Property Index Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//th[@title = 'Name (Roll Year - Property Type)']")
	public WebElement bppPropertyIndexFactorsTableSection;

	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//a[text() = 'BPP Percent Good Factors']")
	public WebElement bppPropertyGoodFactorsTab;
	
	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'BPP Percent Good Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table")
	public WebElement bppPercentGoodFactorsTableSection;

	@FindBy(xpath = "//div[contains(@class, 'column region-main')]//button[@title = 'More Tabs']")
	public WebElement moreTabLeftSection;
	
	@FindBy(xpath = "//button[@aria-expanded = 'true']//following-sibling::div//span[text() = 'Imported Valuation Factors']//parent::a")
	public WebElement dropDownOptionBppImportedValuationFactors;
	
	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'Imported Valuation Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table")
	public WebElement bppImportedValuationFactorsTableSection;
	
	@FindBy(xpath = "//span[text() = 'Composite Factors']//parent::a")
	public WebElement dropDownOptionCompositeFactors;
	
	@FindBy(xpath = "//div[contains(@class, 'column region-sidebar-right')]//button[@title = 'More Tabs']")
	public WebElement moreTabRightSection;
	
	@FindBy(xpath = "//a[text() = 'BPP Composite Factors Settings']")
	public WebElement bppCompFactorSettingTab;
	
	@FindBy(xpath = "//span[text() = 'BPP Composite Factors Settings']//parent::a")
	public WebElement bppCompositeFactorOption;

	@FindBy(xpath = "//a[text() = 'BPP Settings']")
	public WebElement bppSettingTab;
	
	@FindBy(xpath = "//span[text() = 'BPP Settings']/ancestor::header//following-sibling::div//a[contains(@class, 'slds-button') and @role = 'button']")
	public WebElement dropDownIconBppSetting;

	@FindBy(xpath = "//span[text() = 'BPP Composite Factors Settings']/ancestor::header//following-sibling::div//a[contains(@class, 'slds-button') and @role = 'button']")
	public WebElement dropDownIconBppCompFactorSetting;

	@FindBy(xpath = "//a[@title = 'New']")
	public WebElement newBtnToCreateEntry;

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
	
	//@FindBy(xpath = "//div[@class = 'actionsContainer']//div[contains(@class, 'slds-float_right')]//button[@title = 'Cancel']")
	@FindBy(xpath = "//button[@title = 'Cancel']//span[text() = 'Cancel']")
	public WebElement cancelBtnInBppSettingPopUp;
	
	@FindBy(xpath = "//div[contains(@class, 'modal-footer slds')]//button[@title = 'Delete']")
	public WebElement deletBtnInPopUp;
	
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
	
	@FindBy(xpath = "//span[text() = 'Files']//parent::span[text() = 'View All']") //ancestor::a
	public WebElement viewAllFiles;
	
	@FindBy(xpath = "//span[text() = 'BPP Settings']//parent::span[text() = 'View All']")
	public WebElement viewAllBppSettings;
	
	@FindBy(xpath = "//span[text() = 'BPP Composite Factors Settings']//parent::span[text() = 'View All']")
	public WebElement viewAllBppCompositeFactorSettings;
	
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
	
	@FindBy(xpath = "//span[text() = 'Home']")
	public WebElement homeTab;
	
	@FindBy(xpath = "//button[text() = 'Close']")
	public WebElement closeButton;
	
	@FindBy(xpath = "//lightning-primitive-cell-factory[@class = 'slds-cell-wrap slds-has-focus']//span[text() = 'Edit Average' or text() = 'Edit Factor' or text() = 'Edit Valuation Factor']//ancestor::button//lightning-primitive-icon")
	public WebElement editIconInImportPageTable;
	
	@FindBy(xpath = "//lightning-formatted-text[text() = 'Junk_A' or text() = 'Junk_B']")
	public WebElement tableCellWithJunkTextOnImportPageTale;
	
	@FindBy(xpath = "//lightning-input//div//input[@type = 'text']")
	public WebElement inputBoxOnImportPage;
	
	@FindBy(xpath = "//span[contains(@title,'ERROR ROWS')]")
	public WebElement errorRowSection;
	
	@FindBy(xpath = "//div[contains(@class, 'button-container-inner slds-float_right')]//button//span[text() = 'Save']")
	public WebElement saveButton;
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//a[text() = 'No actions available']")
	public WebElement noActionsLinkUnderShowMore;
	
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
	
	@FindBy(xpath = "//span[contains(text(), 'Filtered by all cpi factors')]")
	public WebElement sortingMessage;
	
	@FindBy(xpath = "(//td[@class = 'slds-cell-edit cellContainer']//span[@class = 'slds-truncate uiOutputNumber'])[1]")
	public WebElement firstCpiFactorValue;
	
	@FindBy(xpath = "//table[contains(@class, 'slds-table--resizable-cols uiVirtualDataTable')]")
	public WebElement tableSection;
	
	@FindBy(xpath = "//div[@class = 'uiBlock']//p//span")
	public WebElement errorMsgOnEditClick;
	
	@FindBy(xpath = "//th[@title = 'Roll Year']//a")
	public WebElement rollYearSort;
	
	@FindBy(xpath = "//div[contains(@class, 'LOADED forceContentFileDroppableZone')]//span[contains(@class, 'itemTitle slds-text-body')]")
	public List<WebElement> filesListOnBppTrendSetupPage;
	
	@FindBy(xpath = "(//div[@data-aura-class='forcePageError']//li)[2]")
	public WebElement errorMsgOnInvalidPercentGoodsYearAcquired;
	
	@FindBy(xpath = "//ul[contains(@class, 'has-error uiInputDefaultError')]//li")
	public WebElement errorMsgOnInvalidValuationFactorYearAcquired;
	
	@FindBy(xpath = "//div[contains(@class, 'headerRegion forceListViewManagerHeader')]//a[@title = 'New']")
	public WebElement newBtnViewAllPage;
	
	@FindBy(xpath = "//a[contains(@href, 'Good_Factor')]//span[@class = 'view-all-label' and text() = 'View All']")
	public WebElement viewAllBtnUnderBppPercentGoodsTab;

	@FindBy(xpath = "//a[contains(@href, 'Valuation_Factor')]//span[@class = 'view-all-label' and text() = 'View All']")
	public WebElement viewAllBtnUnderValuationFactorsTab;
	
	@FindBy(xpath = "(//div[@data-aura-class='forcePageError']//li)[1]")
	public WebElement showMoreLinkForEditPostApprovalOfCalculation;
	
	@FindBy(xpath = "//div[contains(@id, 'help-message-')]")
	public WebElement errorMsgOnImportForInvalidFileFormat;
	
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
	
	
	/**
	 * Description: This will select the roll year from the drop down
	 * @param rollYear: Roll Year for which the BPP Trend Name needs to be clicked or BPP trend name itself
	 * @throws: Exception
	 */	
	public void clickOnEntryNameInGrid(String detailsOfEntryToSelect) throws Exception {
		String expRegexPattern = "\\d\\d\\d\\d";
		Pattern.compile(expRegexPattern);
	
		String xpath;
		if(Pattern.matches(expRegexPattern, detailsOfEntryToSelect)) {
			xpath = "//span[text() = '"+ detailsOfEntryToSelect +"']//ancestor::td//preceding-sibling::th//a[contains(@title, 'BPP Trend')]";
		} else {
			xpath = "//th//a[contains(@title, '"+ detailsOfEntryToSelect +"')]";
		}
		
		String bppTrendSetupName;
		WebElement bppTrendSetup = locateElement(xpath, 60);
		try {
			waitForElementToBeClickable(bppTrendSetup, 10);
			bppTrendSetupName = getElementText(bppTrendSetup).trim();
			Click(bppTrendSetup);
		} catch(Exception ex) {
			bppTrendSetup = locateElement(xpath, 60);
			waitForElementToBeClickable(bppTrendSetup, 10);
			bppTrendSetupName = getElementText(bppTrendSetup).trim();
			Click(bppTrendSetup);
		}
		System.setProperty("BppTrendSetupName", bppTrendSetupName);
	}
	
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
	 * @param isTableUnderMoreTab: true / false flag to specify whether given table falls under more tab
	 * @throws: Exception
	 */
	public WebElement clickOnTableOnBppTrendPage(String tableName, boolean isTableUnderMoreTab, boolean... isTableOnEfileImportPage) throws Exception {
		WebElement givenTable = null;
		if(isTableOnEfileImportPage.length == 0 || (isTableOnEfileImportPage.length == 1 && isTableOnEfileImportPage[0] == false)) {
			String xpathStr;
			if(isTableUnderMoreTab) {
				waitForElementToBeVisible(moreTabs, 10);
				waitForElementToBeClickable(moreTabs, 10);
				Click(moreTabs);
				WebElement dropDownList = locateElement("//button[@title = 'More Tabs' and @aria-expanded = 'true']", 10);
				if(dropDownList == null) {
					waitForElementToBeClickable(moreTabs, 10);
					Click(moreTabs);	
				}
				xpathStr = "//span[contains(text(), '" + tableName + "')]";
			} else {
				xpathStr = "//a[contains(@data-label, '" + tableName + "')]";
			}
			givenTable = locateElement(xpathStr, 30);
			waitForElementToBeClickable(givenTable);
			clickAction(givenTable);			
		} else {
			String xpathStr = "//a[contains(@data-label, '" + tableName + "')]";
			givenTable = locateElement(xpathStr, 20);	
			if(givenTable != null) {
				clickAction(givenTable);
			} else {
				waitForElementToBeClickable(moreTabsFileColumns, 30);
				javascriptClick(moreTabsFileColumns);
				xpathStr = "//span[contains(text(), '" + tableName + "')]";
				givenTable = locateElement(xpathStr, 20);
				clickAction(givenTable);
			}			
		}
		return givenTable;
	}

	/**
	 * Description: This will click on the given table name
	 * @param tableName: Name of the table
	 * @throws: Exception
	 */
	public boolean isTableDataVisible(String tableName, int...timeOut) throws Exception {
		String tableXpath = "//lightning-tab[@data-id = '"+ tableName +"']//table";
		int searchTimeOut = 30;
		if(timeOut.length == 1) {
			searchTimeOut = timeOut[0];
		}
		
		WebElement tableContent = locateElement(tableXpath, searchTimeOut);
		if(tableContent.isDisplayed()) {
			return true;
		} else {
			return false;
		}
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
		if(button != null) {
			waitForElementToBeClickable(button, 30);
			return button.isDisplayed();
		} else {
			return false;
		}
	}
	
	/**
	 * Description: This will initiate the calculation by clicking calculate button for individual table (at table level)
	 * @param tableName: Name of the table
	 */
	private void clickRequiredButton(String ...args) throws Exception {
		String xpath = getXpathForRequiredBtton(args);
		WebElement button = locateElement(xpath, 60);
		if(button == null) {
			button = locateElement(xpath, 60);
		}
		waitForElementToBeClickable(button, 10);
		Click(button);
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
	public WebElement locateCellToBeEdited(String tableName, int... cellDetails) throws Exception {
		String xpathCellData = null;
		String tagNameToAppend = null;
		
		if(tableName.equals("BPP Prop 13 Factors")) {
			tagNameToAppend = "td";
		} else {
			tagNameToAppend = "th";
		}
		
		if(cellDetails.length == 0) {
			xpathCellData = "((//lightning-tab[@data-id = '"+ tableName +"']//"+ tagNameToAppend +"[@data-label = 'Year Acquired'])[1]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])[1]";
		} else if (cellDetails.length == 1) {
			xpathCellData = "((//lightning-tab[@data-id = '"+ tableName +"']//"+ tagNameToAppend +"[@data-label = 'Year Acquired'])[1]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])["+ cellDetails[0] +"]"; 
		} else if (cellDetails.length == 2) {
			xpathCellData = "((//lightning-tab[@data-id = '"+ tableName +"']//"+ tagNameToAppend +"[@data-label = 'Year Acquired'])["+ cellDetails[0] +"]//following-sibling::td[not (contains(@data-label, 'Year Acquired'))])["+ cellDetails[1] +"]";
		}

		System.setProperty("xpathCellData", xpathCellData);
		WebElement element = locateElement(xpathCellData, 30);
		if(element == null) {
			element = locateElement(xpathCellData, 30);
		}
		return element;
	}

	/**
	 * @Description: Locates the edit button in the cell data text box
	 * @param cellDetails: Details of cell to edit (row number and column number if required) 
	 * @return: Returns the edit button element
	 * @throws Exception
	 */
	public WebElement locateEditButtonInFocusedCell() throws Exception {
		WebElement editButton = null;
		Click(waitForElementToBeClickable(10, System.getProperty("xpathCellData")));
		String xpathEditBtn = "//td[contains(@class, 'has-focus')]//button[contains(@class, 'cell-edit')]//lightning-primitive-icon";
		editButton = locateElement(xpathEditBtn, 10);
		return editButton;
	}
	
	/**
	 * @Description: Below method is used the edit the cell data in the table displayed for select roll year.
	 * @param: Takes the integer / double value to enter in the cell
	 * @throws Exception 
	 */	
	public void editCellDataInGridForGivenTable(String tableName, Object data) throws Exception {		
		String xpathEditTxtBox = "//div//input[@name = 'dt-inline-edit-text']";
		WebElement editTxtBox = locateElement(xpathEditTxtBox, 60);
		enter(editTxtBox, String.valueOf(data));

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
	 * @returns: Return status as true or false
	 * @throws Exception
	 */
	public boolean isEditedCellHighlighted(WebElement editedCell) throws Exception {
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
		WebElement spinner = locateElement(xpath, 5);
		if(spinner != null) {
			validateAbsenceOfElement(xpath, 180);
		}

		//Locating the message displayed above the table once the loader / spinner becomes invisible
		xpath = "//lightning-tab[@data-id = '"+ tableName +"']//div[@class = 'hightlight-tab-message']";
		WebElement message = locateElement(xpath, 90);
		String text = getElementText(message);
		return text;
	}

	/**
	 * Description: This will reset the status of tables based on the expected status
	 * @param factorTablesToReset: List of table names for which status is to be reset
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
			//objSalesforceAPI.update("BPP_Trend_Roll_Year__c", queryForID, columnName, expectedStatus);
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
	 * @param rollYear
	 * @throws Exception
	 */
	public void removeExistingBppSettingEntry(String rollYear) throws Exception {
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		String queryForID = "SELECT Id FROM BPP_Setting__c where BPP_Trend_Roll_Year_Parent__c = '"+ rollYear +"'";
		objSalesforceAPI.delete("BPP_Setting__c", queryForID);
	}

	/**
	 * Description: This will delete the existing BPP Composite Factor Setting entries (Min. Equip. Index) for given roll year
	 * @param rollYear
	 * @throws Exception
	 */
	public void removeExistingBppFactorSettingEntry(String rollYear) throws Exception {
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		String queryForID = "SELECT Id FROM BPP_Composite_Factors_Setting__c Where BPP_Trend_Roll_Year_Parent__c = '"+ rollYear +"'";
		objSalesforceAPI.delete("BPP_Composite_Factors_Setting__c", queryForID);
	}	
	
	/**
	 * Description: This will retrieve current status of tables
	 * @param factorTablesToReset: List of table names for which status is to be reset
	 * @param rollYear: Roll year for which the status needs to be reset
	 */
	public String retrieveTablesStatusForGivenRollYear(String tableName, String rollYear) throws Exception {
		objApasGenericFunctions.login(users.SYSTEM_ADMIN);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		
		clickOnEntryNameInGrid(rollYear);
		
		String xpathFieldValue = "//span[text() = '"+ tableName +"']//parent::div//following-sibling::div//span[contains(@class, 'test-id__field-value')]//lightning-formatted-text";
		WebElement element = locateElement(xpathFieldValue, 30);
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
	 * Deletes files from the downloads directory
	 */
	public void deleteFactorFilesFromDownloadFolder() {
		File dir = new File(TestBase.CONFIG.getProperty("fileDownloadPath"));
		if(dir.listFiles().length > 0) {
			for(File file: dir.listFiles()) {
			    if (!file.isDirectory()) 
			        file.delete();
			}
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
				+ "and (not(contains(@data-label, 'Year Acquired')) and not (contains(@data-label, 'Rounded')))]";
		List<WebElement> tableRows = locateElements(xpathTableRows, 30);
		
		for(int i = 0; i < tableRows.size(); i++) {
			int rowNum = i + 1;
			String xpathYearAcq = "("+ xpathTableRows +")["+ rowNum +"]//th";
			WebElement yearAcquiredElement = locateElement(xpathYearAcq, 30);
			String yearAcquiredTxt = getElementText(yearAcquiredElement);
			
			String xpathYearData = "("+ xpathTableRows +")["+ rowNum +"]"+ xpathTableData;
			List<WebElement> yearAcruiredDataElements = locateElements(xpathYearData, 10);
			
			List<Object> yearAcruiredData = new ArrayList<Object>();
			for(int j = 0; j < yearAcruiredDataElements.size(); j++) {
				Object cellData = (int)Double.parseDouble(getElementText(yearAcruiredDataElements.get(j)));
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

		Map<String, List<Object>> dataMap = new HashMap<String, List<Object>>();
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
	 * @Description: Updates the existing trend setting data into excel
	 * @param filePath: Takes the path of the XLSX workbook
	 * @param sheetName: Takes the names of the Sheet that is be read from given workbook
	 * @throws: Throws Exception          
	 **/
	public void updateTrendSettingInExcel(String filePath, String propertyType, String propertyValue, String... sheetName) throws Exception {
		String sheetNameForExcel;
		if(sheetName.length == 0) {
			sheetNameForExcel = "Trends Settings ";
		} else {
			sheetNameForExcel = sheetName[0];
		}
	
		File file = null;
		FileInputStream inputStream = null;
		FileOutputStream outputStream = null;
		Workbook wb = null;
		
		try {
			//Create an object of File class to open file
	        file = new File(filePath);
	        //Create an object of FileInputStream class to read excel file
	        inputStream = new FileInputStream(file);
	        
	        wb = new XSSFWorkbook(inputStream);
	        Sheet sheet = wb.getSheet(sheetNameForExcel);
	
	        //Get the current count of rows in excel file
	        int totalRows = sheet.getPhysicalNumberOfRows();
	
	        //Retrieving original values of trend settings from excel file before updating them and finding row number of trend settings
	        trendSettingsOriginalValues = new HashMap<String, String>();
	        trendSettingRowNumbers = new HashMap<String, Integer>();
	        for(int rowNum = 0; rowNum < totalRows; rowNum++) {
	            Row currentRow = sheet.getRow(rowNum);
	        	String trendSettingName = currentRow.getCell(0).getStringCellValue();
	        	String trendSettingData = null;
	        	
	        	switch (currentRow.getCell(1).getCellType()) {
    		    	case Cell.CELL_TYPE_STRING:
    		    		trendSettingData = currentRow.getCell(1).getStringCellValue();
    		    		break;
    		    	case Cell.CELL_TYPE_NUMERIC:
    		    		int cellData = (int)currentRow.getCell(1).getNumericCellValue();
    		    		trendSettingData = Integer.toString(cellData);
    		    		break;
	        	}
	        	
	        	if(trendSettingName.contains("Industrial")) {
	        		trendSettingRowNumbers.put("Industrial", rowNum);
	        		trendSettingsOriginalValues.put("Industrial", trendSettingData);
	        	} else if(trendSettingName.contains("Commercial")) {
	        		trendSettingRowNumbers.put("Commercial", rowNum);
	        		trendSettingsOriginalValues.put("Commercial", trendSettingData);
	        	} else if(trendSettingName.contains("Agricultural")) {
	        		trendSettingRowNumbers.put("Agricultural", rowNum);
	        		trendSettingsOriginalValues.put("Agricultural", trendSettingData);
	        	} else if(trendSettingName.contains("Construction")) {
	        		trendSettingRowNumbers.put("Construction", rowNum);
	        		trendSettingsOriginalValues.put("Construction", trendSettingData);
	        	}
	        }
	        
	        Cell cell = sheet.getRow(trendSettingRowNumbers.get(propertyType)).getCell(1);
        	int updatedCellValue = Integer.parseInt(propertyValue);
        	cell.setCellValue(updatedCellValue);
	        
	        //Create an object of FileOutputStream class to create write data in excel file
	        outputStream = new FileOutputStream(file);
	        //write data in the excel file
	        wb.write(outputStream);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			//close input stream, workbook and output stream
			inputStream.close();
			wb.close();
			outputStream.close();
		}
	}
	
	/**
	 * Description: It reverts the trend setting values to original values in excel file
	 * @param filePath: Complete file path
	 * @param sheetName: Optional parameter which takes name of the sheet
	 */
	public void revertTrendSettingsDataInExcel(String filePath, String... sheetName) throws Exception {
		String sheetNameForExcel;
		if(sheetName.length == 0) {
			sheetNameForExcel = "Trends Settings ";
		} else {
			sheetNameForExcel = sheetName[0];
		}
	
		File file = null;
		FileInputStream inputStream = null;
		Workbook wb = null;
		FileOutputStream outputStream = null;
		
		try {
			//Create an object of File class to open file
	        file = new File(filePath);
	        //Create an object of FileInputStream class to read excel file
	        inputStream = new FileInputStream(file);
	        
	        wb = new XSSFWorkbook(inputStream);
	        Sheet sheet = wb.getSheet(sheetNameForExcel);
	        
	        Cell cell = null;
	        // Updating the industrial trend setting value
	        cell = sheet.getRow(trendSettingRowNumbers.get("Industrial")).getCell(1);
	        cell.setCellValue(Integer.parseInt(trendSettingsOriginalValues.get("Industrial")));
	        // Updating the commercial trend setting value
	        cell = sheet.getRow(trendSettingRowNumbers.get("Commercial")).getCell(1);
	        cell.setCellValue(Integer.parseInt(trendSettingsOriginalValues.get("Commercial")));
	        // Updating the agricultural trend setting value
	        cell = sheet.getRow(trendSettingRowNumbers.get("Agricultural")).getCell(1);
	        cell.setCellValue(Integer.parseInt(trendSettingsOriginalValues.get("Agricultural")));
	        // Updating the construction trend setting value
	        cell = sheet.getRow(trendSettingRowNumbers.get("Construction")).getCell(1);
	        cell.setCellValue(Integer.parseInt(trendSettingsOriginalValues.get("Construction")));

	        //Create an object of FileOutputStream class to create write data in excel file
	        outputStream = new FileOutputStream(file);
	        //write data in the excel file
	        wb.write(outputStream);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			//close input stream, workbook and output stream
			inputStream.close();
			wb.close();
			outputStream.close();
		}
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
		
		WebElement misMatchedCell = locateElement(xpathMisMatchedCell, 30);
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
	public boolean isSubmitAllFactorsForApprovalBtnVisible(int timeToLocateBtn) throws Exception {
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
	 * Description: Checks whether pencil icon to edit table status on BPP Trend Status page is visible
	 *  @return: Return status of pencil icon as true or false
	 * @throws Exception
	 */
	public boolean isPencilIconToEditTableStatusVisible(String tableName) throws Exception {
		String xpathPencilIcon = "//span[text() = '"+ tableName +"']//parent::div//following-sibling::div//button[contains(@class, 'test-id__inline-edit-trigger slds-shrink')]";
		WebElement element = locateElement(xpathPencilIcon, 5);
		if(element != null) {
			return element.isDisplayed();
		} else {
			return false;
		}
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
		Thread.sleep(10000);
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
			WebElement pageLevelPopUpCloseBtn = locateElement("//button[@title = 'Close']", 30);
			waitForElementToBeClickable(pageLevelPopUpCloseBtn, 10);
			javascriptClick(pageLevelPopUpCloseBtn);
		} catch(Exception ex) {
			ex.getMessage();
		}
	}
		
	/**
	 * It wait for the pop up message to show up when calculate button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForSuccessPopUpMsgOnCalculateClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'Data calculated successfully']";
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
	 * @return: Return the warning message
	 * @throws Exception
	 */
	public String retrieveReCalculatePopUpMessage() throws Exception {
		String xpath = "//h2[contains(text(), '"+ TestBase.CONFIG.getProperty("recalculatePopUpMsg") +"')]";
		return getElementText(locateElement(xpath, 10));
	}

	/**
	 * Description: Retrieves the warning message displayed on clicking approve button without saving edited data
	 * @return: Return the warning message
	 * @throws Exception
	 */
	public String retrieveApproveTabDataPopUpMessage() throws Exception {
		String xpath = "//h2[contains(text(), '"+ TestBase.CONFIG.getProperty("approveTabDataMsg") +"')]";
		return getElementText(locateElement(xpath, 10));
	}
	
	/**
	 * Description: Retrieve the error message displayed in pop up on Calculate button's click
	 * @param timeToLocateElemInSec
	 * @return: Return the error message displayed in pop up
	 * @throws Exception
	 */
	public String waitForErrorPopUpMsgOnCalculateClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//span[@class = 'toastMessage forceActionsText']";
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
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnApproveClick(int timeToLocateElemInSec) throws Exception {
		String xpathErrorMsg = "//div[contains(@class, 'toastContent')]//span[text() = 'Roll year flag name and roll year must be present']";
		WebElement errorMsg = locateElement(xpathErrorMsg, 5);
		if(errorMsg == null) {
			String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'Data approved successfully']";
			WebElement popUpOnButtonClick = locateElement(xpath, timeToLocateElemInSec);
			if(popUpOnButtonClick.isDisplayed()) {
				String popUpMsg = getElementText(popUpOnButtonClick);
				closePageLevelMsgPopUp();
				return popUpMsg;
			} else {
				return null;
			}			
		} else {
			return getElementText(errorMsg);
		}
	}
	
	/**
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
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
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnSubmitAllForApprovalClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(text(), 'All Factors have been submitted for approval')]";
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
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsgOnApproveAllClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(text(), 'Approved all tabs')]";
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
	 * It wait for the pop up message to show up when approve button is clicked
	 * @param: Timeout for which pop up message needs to be located 
	 */
	public String retrieveForPopUpMsgOnSavingBppFactor(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span";
		String popUpMsg = getElementText(locateElement(xpath, timeToLocateElemInSec));
		closePageLevelMsgPopUp();
		return popUpMsg;		
	}

	/**
	 * *************************** Below Methods Are For BPP EFile Import Page ****************************
	 */

	/**
	 * Clicks on the BOE Index & Good Factors file on import transactions page
	 * @throws: Throws Exception
	 */
	public void clickOnBoeIndexAndGoodFactorsImportLog(String rollYear) throws Exception {
		WebElement importLogNameInGrid = locateElement("//a[contains(text(), 'BOE - Index and Percent Good Factors :"+ rollYear +"')]", 60);
		Click(importLogNameInGrid);
	}

	/**
	 * Clicks on the BOE Valuations file on import transactions page
	 * @throws: Throws Exception
	 */
	public void clickOnBoeValuationFactorsImportLog(String rollYear) throws Exception {
		WebElement importLogNameInGrid = locateElement("//a[contains(text(), 'BOE - Valuation Factors :"+ rollYear +"')]", 60);
		Click(importLogNameInGrid);
	}

	/**
	 * Clicks on the CAA Valuations file on import transactions page
	 * @throws: Throws Exception
	 */
	public void clickOnCaaValuationFactorsImportLog(String rollYear) throws Exception {
		WebElement importLogNameInGrid = locateElement("//a[contains(text(), 'CAA - Valuation Factors :"+ rollYear +"')]", 60);
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
	 * It selects an individual error row and discard it
	 * @throws: Throws Exception
	 */
	public void discardIndividualErrorRow() throws Exception {
		String tableNumber = System.getProperty("tableNumber");
		String xpathIndividualCheckBox = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']"
				+ "//td//span[@class='slds-checkbox_faux']";
		WebElement individualCheckBox = locateElement(xpathIndividualCheckBox, 60);
		Click(individualCheckBox);
		
		String xpathDiscardButton = "//lightning-tab[@aria-labelledby = '"+ tableNumber +"__item']//button[text()='Discard']";
		WebElement discardButton = locateElement(xpathDiscardButton, 60);
		waitForElementToBeClickable(discardButton, 10);
		Click(discardButton);
		
		String xpathContinueButton = "//button[text()='Continue']";
		WebElement continueButton = locateElement(xpathContinueButton, 60);
		if(continueButton == null) {
			discardButton = locateElement(xpathDiscardButton, 60);
			waitForElementToBeClickable(discardButton, 10);
			Click(discardButton);
			
			continueButton = locateElement(xpathContinueButton, 60);
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
			discardButton = locateElement(xpathDiscardButton, 60);
			waitForElementToBeClickable(discardButton, 10);
			Click(discardButton);
			
			continueButton = locateElement(xpathContinueButton, 60);
		}
		
		waitForElementToBeClickable(continueButton, 10);
		clickAction(continueButton);
	}

	/**
	 * Description: This will fill data in roll year and tables status data in BPP trend setting pop up
	 * @param bppTrendSetupDataMap: Data map containing keys as field names in setting pop up and values as values of these fields
	 * @throws: Throws Exception
	 */
	public void enterBppTrendSettingRollYearAndTableStatus(Map<String, String> bppTrendSetupDataMap) throws Exception {
		for(Map.Entry<String, String> entryData : bppTrendSetupDataMap.entrySet()) {
			String key = entryData.getKey();
			String value = entryData.getValue();
			
			String xpathDropDownField = "//span[text() = '"+ key +"']//parent::span//following-sibling::div//div[@class = 'uiPopupTrigger']";
			WebElement dropDownField = locateElement(xpathDropDownField, 60);
			objApasGenericPage.selectOptionFromDropDown(dropDownField, value);
		}
	}
	
	/**
	 * Description: This will fill data in roll year field in BPP trend setting
	 * @param bppSettingRollYear: BPP trend setup roll year value like 2018, 2019, 2020
	 * @throws: Throws Exception
	 */
	public void enterRollYearInBppSettingDetails(String bppSettingRollYear) throws Exception {
		waitForElementToBeClickable(deleteIconForPrePopulatedRollYear, 10);
		Click(deleteIconForPrePopulatedRollYear);
		waitForElementToBeClickable(rollYearTxtBox, 10);
		enter(rollYearTxtBox, bppSettingRollYear);
		
		String xpathStr = "//mark[text() = '" + bppSettingRollYear.toUpperCase() + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 60);
		drpDwnOption.click();
	}

	/**
	 * Description: This will fill data in maximum & minimum factor field in bpp trend setting
	 * @param factorValue: Maximum or minimum factor value like 125% 
	 * @throws Exception
	 */
	public void enterFactorValue(String factorValue) throws Exception {
		waitForElementToBeClickable(factorTxtBox, 10);
		factorTxtBox.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		factorTxtBox.sendKeys(Keys.chord(Keys.CONTROL, "a"));
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
	public String getCountOfBppSettings(String expectedCount) throws Exception {
		String xpath = "//article[contains(@class, 'slds-card slds-card_boundary')]//span[text() = 'BPP Settings']//following-sibling::span";
		WebElement bppSettingsCount = locateElement(xpath, 60);
		wait.until(ExpectedConditions.textToBePresentInElement(bppSettingsCount, "("+ expectedCount +")"));
		return getElementText(bppSettingsCount).substring(1, 2);
	}
	
	/**
	 * Description: Retrieves the count of Bpp Settings currently displayed / available
	 */
	public String getCountOfBppCompositeFactorSettings() throws Exception {
		String xpath = "//article[contains(@class, 'slds-card slds-card_boundary')]//span[text() = 'BPP Composite Factors Settings']//following-sibling::span";
		return getElementText(locateElement(xpath, 30)).substring(1, 2);
	}
	
	/**
	 * Description: Retrieves the error message displayed on entering an invalid value for max equipment index factor
	 * @return: Returns the error message
	 * @throws: Exception
	 */
	public String errorMsgOnIncorrectFactorValue() throws Exception {
		String xpath = "//span[text() = 'Maximum Equipment index Factor']//parent::label//parent::div//following-sibling::ul//li";
		return getElementText(locateElement(xpath, 10));
	}
	
	/**
	 * Description: Retrieves the error message displayed on entering an invalid value for min. equipment index factor
	 * @return: Returns the error message
	 * @throws: Exception
	 */
	public String errorMsgUnderMinEquipFactorIndexField() throws Exception {
		String xpath = "//span[text() = 'Minimum Good Factor']//parent::label//parent::div//following-sibling::ul//li";
		return getElementText(locateElement(xpath, 10));
	}
	
	/**
	 * Description: Retrieves the current status of the given table from details page under given BPP Trend Setup
	 * @param: Takes the names of the table
	 * @return: Returns the table status
	 * @throws: Exception
	 */
	public String getTableStatusFromBppTrendSetupDetailsPage(String tableName) throws Exception {
		String tableNameForTrendSetupPage = null;
		switch(tableName) {
		case"Commercial Composite Factors":
			tableNameForTrendSetupPage = "Commercial Trends Status";
			break;
		case"Industrial Composite Factors":
			tableNameForTrendSetupPage = "Industrial Trend Status";
			break;
		case"Construction Composite Factors":
			tableNameForTrendSetupPage = "Const. Trends Status";
			break;
		case"Construction Mobile Equipment Composite Factors":
			tableNameForTrendSetupPage = "Const. Mobile Equipment Trends Status";
			break;
		case"Agricultural Mobile Equipment Composite Factors":
			tableNameForTrendSetupPage = "Ag. Mobile Equipment Trends Status";
			break;
		case"Agricultural Composite Factors":
			tableNameForTrendSetupPage = "Ag. Trends Status";
			break;
		case"BPP Prop 13 Factors":
			tableNameForTrendSetupPage = "Prop 13 Factor Status";
			break;
		case"Computer Valuation Factors":
			tableNameForTrendSetupPage = "Computer Trends Status";
			break;
		case"Biopharmaceutical Valuation Factors":
			tableNameForTrendSetupPage = "Biopharmaceutical Trends Status";
			break;
		case"Copier Valuation Factors":
			tableNameForTrendSetupPage = "Copier Trends Status";
			break;
		case"Semiconductor Valuation Factors":
			tableNameForTrendSetupPage = "Semiconductor Trends Status";
			break;
		case"Litho Valuation Factors":
			tableNameForTrendSetupPage = "Litho Trends Status";
			break;
		case"Mechanical Slot Machines Valuation Factors":
			tableNameForTrendSetupPage = "Mechanical Slot Machine Trends Status";
			break;
		case"Set-Top Box Valuation Factors":
			tableNameForTrendSetupPage = "Set-Top Box Trends Status";
			break;
		case"Electrical Slot Machines Valuation Factors":
			tableNameForTrendSetupPage = "Electronic Slot Machine Trends Status";
			break;
		default:
			tableNameForTrendSetupPage = tableName;
		}
				
		String xpathCompositeTrendsStatus = "//span[text() = 'Composite Trends Status']//ancestor::button[contains(@class, 'test-id__section-header-button slds')]";
		WebElement compTrendsStats = locateElement(xpathCompositeTrendsStatus, 30);
		String attributeValue = compTrendsStats.getAttribute("aria-expanded");
		if(attributeValue.equals("false")) {
			Click(compTrendsStats);
		}
		
		String xpathValuationTrendStatus = "//span[text() = 'Valuation Trend Status']//ancestor::button[contains(@class, 'test-id__section-header-button slds')]";
		WebElement valuationTrendsStats = locateElement(xpathValuationTrendStatus, 30);
		attributeValue = valuationTrendsStats.getAttribute("aria-expanded");
		if(attributeValue.equals("false")) {
			Click(valuationTrendsStats);
		}		
		
		String xpathProp13FactorStatus = "//span[text() = 'Prop 13 Factor Status']//ancestor::button[contains(@class, 'test-id__section-header-button slds-section')]";	
		WebElement prop13FactorStatus = locateElement(xpathProp13FactorStatus, 30);
		attributeValue = prop13FactorStatus.getAttribute("aria-expanded");
		if(attributeValue.equals("false")) {
			Click(prop13FactorStatus);
		}
		
		String xpathTableStatus = "//span[text() = '"+ tableNameForTrendSetupPage +"']//parent::div//following-sibling::div//lightning-formatted-text";
		WebElement tableStatus = locateElement(xpathTableStatus, 60);
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
				Click(bppPropertyIndexFactorsTab);
			case "BPP PERCENT GOOD FACTORS":
				Click(bppPropertyGoodFactorsTab);
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
	public WebElement clickNewButtonUnderFactorSection(String factorsTable) throws Exception {
		String newBtnXpath = "//span[text() = '"+ factorsTable +"']//ancestor::header[contains(@class, 'slds-media slds-media--center')]//following-sibling::div//a[@title = 'New']";
		WebElement newButton = locateElement(newBtnXpath, 10);
		try {
			newButton = locateElement(newBtnXpath, 10);
			waitForElementToBeClickable(newButton, 10);
			clickAction(newButton);
		} catch(Exception ex) {
			newButton = locateElement(newBtnXpath, 10);
			waitForElementToBeClickable(newButton, 10);
			clickAction(newButton);			
		}

		return newButton;
	}
	
	/**
	 * Description: Retrieves the file status from the table grid on import transactions page
	 * @param: Name of the column whose index position is to be found
	 * @return: Index position of given column from the the grid table on View All page
	 * @throws: Exception
	 */
	public String retrieveIndexPositionOfGivenColumnFromViewAllPage(String columnName) throws Exception {		
		String xpathOfGridColumns = "//span[text() = 'BPP Trend Setup']//ancestor::div[contains(@class, 'slds-page-header')]//following-sibling::div//thead//tr//th[contains(@class, 'slds-is-resizable')]//span[@class ='slds-truncate']";
		int indexPositionOfGivenColumn = -1;
		List <WebElement> tableColumns = locateElements(xpathOfGridColumns, 20);

		int indexPos;
		String xpath;
		for(int i = 0; i < tableColumns.size(); i++) {
			indexPos = i+1;
			xpath = "("+ xpathOfGridColumns +")["+ indexPos +"]";
			String currentColumnName = getElementText(locateElement(xpath, 10));
			if(columnName.equalsIgnoreCase(currentColumnName)) {
				indexPositionOfGivenColumn = i;
				break;
			}
		}
		return Integer.toString(indexPositionOfGivenColumn);
	}
	
	public String getExistingNameFromViewAllGrid() throws Exception {
		WebElement nameOfPropType = locateElement("//tbody//tr[1]//th//a", 10);
		return getElementText(nameOfPropType);
	}
	
	public String getExistingYearAcquiredFromViewAllGrid() throws Exception {
		String indexPosition = retrieveIndexPositionOfGivenColumnFromViewAllPage("Year Acquired");
		String xpath = "(//tbody//tr[1]//td)["+ indexPosition +"]//span[@class = 'slds-truncate']";
		WebElement yearAcquired = locateElement(xpath, 10);
		return getElementText(yearAcquired);
	}

	public String getExistingPropertyTypeFromViewAllGrid() throws Exception {
		String indexPosition = retrieveIndexPositionOfGivenColumnFromViewAllPage("Property Type");
		String xpath = "(//tbody//tr[1]//td)["+ indexPosition +"]//span[@class = 'slds-truncate']";
		WebElement propertyType = locateElement(xpath, 10);
		return getElementText(propertyType);
	}

	public String getExistingValuationFactorFromViewAllGrid() throws Exception {
		String indexPosition = retrieveIndexPositionOfGivenColumnFromViewAllPage("Valuation Factor");
		String xpath = "(//tbody//tr[1]//td)["+ indexPosition +"]//span[@class = 'slds-truncate']";
		WebElement valuationFactor = locateElement(xpath, 10);
		return getElementText(valuationFactor);
	}

	
	
	public String getExistingAgeFromViewAllGrid() throws Exception {
		String indexPosition = retrieveIndexPositionOfGivenColumnFromViewAllPage("Age");
		String xpath = "(//tbody//tr[1]//td)["+ indexPosition +"]//span[@class = 'slds-truncate']";
		WebElement age = locateElement(xpath, 10);
		return getElementText(age);
	}
	
	public String getExistingGoodFactorTypeFromViewAllGrid() throws Exception {
		String indexPosition = retrieveIndexPositionOfGivenColumnFromViewAllPage("Good Factor Type");
		String xpath = "(//tbody//tr[1]//td)["+ indexPosition +"]//span[@class = 'slds-truncate']";
		WebElement goodFactorType = locateElement(xpath, 10);
		return getElementText(goodFactorType);
	}

	public String getExistingExpectedLifeFromViewAllGrid() throws Exception {
		String indexPosition = retrieveIndexPositionOfGivenColumnFromViewAllPage("Expected Life");
		String xpath = "(//tbody//tr[1]//td)["+ indexPosition +"]//span[@class = 'slds-truncate']";
		WebElement expectedLife = locateElement(xpath, 10);
		return getElementText(expectedLife);
	}

	public String getExistingGeneralGoodFactorFromViewAllGrid() throws Exception {
		String indexPosition = retrieveIndexPositionOfGivenColumnFromViewAllPage("General Good Factor");
		String xpath = "(//tbody//tr[1]//td)["+ indexPosition +"]//span[@class = 'slds-truncate']";
		WebElement generalGoodFactor = locateElement(xpath, 10);
		return getElementText(generalGoodFactor);
	}
	
	
	
	/**
	 * Description: It creates a new factor entry under given factor section
	 * @param factorSectionNameToOpen: Takes the names of the factor section under which New button needs to be located
	 * @throws Exception
	 */
	public void enterDataInGivenFieldInNewFactorPopUp(String fieldName, String fieldValue) throws Exception {
		String xpath = null;
		WebElement element = null;
		if(fieldName.equalsIgnoreCase("Year Acquired") || fieldName.equalsIgnoreCase("Property Type") || fieldName.equalsIgnoreCase("Good Factor Type")) {			
			String fieldXpath = "//span[text() = '"+ fieldName +"']//parent::span//following-sibling::div[@class = 'uiMenu']";
			WebElement fieldElement = locateElement(fieldXpath, 20);			
			objApasGenericPage.selectOptionFromDropDown(fieldElement, fieldValue);
		} else {
			xpath = "//span[contains(text(), '"+ fieldName +"')]//parent::label//following-sibling::input";
			element = locateElement(xpath, 30);
			enter(element, fieldValue);
			if(fieldName.toUpperCase().contains("NAME (Roll Year")) {
				System.setProperty("factorEntryName", fieldValue);
			}
		}
	}
	
	/**
	 * Description: Checks the newly created entry in the table
	 * @return: Returns the status as true or false
	 * @throws: Exception
	 */
	public boolean isFactorEntryVisibleInTable(String newlyCreatedEntryName) throws Exception {
		WebElement element = locateElement("//a[text() = '"+ newlyCreatedEntryName +"']", 10);
		if(element == null) {
			element = locateElement("//a[text() = '"+ newlyCreatedEntryName +"']", 10);
		}
		return element.isDisplayed();
	}
	
	/**
	 * Description: Clicks on the show more drop down in the table for given entry.
	 * @param newlyCreatedEntryName: It takes BPP factor name as argument
	 * @throws: Exception
	 */
	public void clickShowMoreDropDownForGivenFactorEntry(String factorTableName) throws Exception {
		//String rowNum = getRowNumberToUpdate(factorTableName, TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String xpath;
		if(factorTableName.equalsIgnoreCase("BPP Percent Good Factors")) {
			xpath = "(//span[text() = 'Machinery and Equipment'])[1]//parent::td//following-sibling::td//span[text() = 'Show More'] | (//span[text() = 'Machinery and Equipment'])[1]//parent::td//following-sibling::td//a[@title = 'Show 2 more actions']";
		} else {
			xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = '"+factorTableName+"']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr[1]//a[@role = 'button']";
		}
		
		WebElement showMoreDropDown = locateElement(xpath, 15);
		if(showMoreDropDown == null) {
			showMoreDropDown = locateElement(xpath, 15);
		}
		clickAction(showMoreDropDown);
	}
	
	/**
	 * Description: Checks the availability of Edit link under show more for given factor entry in the table
	 * @return: Returns the status as true or false
	 * @throws: Exception
	 */
	public boolean isEditLinkAvailableForGivenFactorEntry(String factorTableName) throws Exception {
		clickShowMoreDropDownForGivenFactorEntry(factorTableName);
		waitForElementToBeVisible(objBuildPermitPage.editLinkUnderShowMore, 10);
		return objBuildPermitPage.editLinkUnderShowMore.isDisplayed();
	}
	
	/**
	 * Description: Checks the availability of Delete link under show more for given factor type
	 * @return: Returns the status as true or false
	 * @throws: Exception
	 */
	public boolean isDeleteLinkAvailableForGivenFactorEntry(String factorTableName) throws Exception {
		clickShowMoreDropDownForGivenFactorEntry(factorTableName);
		waitForElementToBeVisible(objBuildPermitPage.deleteLinkUnderShowMore, 10);
		return objBuildPermitPage.deleteLinkUnderShowMore.isDisplayed();
	}
	
	/**
	 * It wait for the pop up message to show up when save button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForSuccessPopUpMsgOnSaveButtonClick(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[text() = 'Data calculated successfully']";
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
	 * It wait for the pop up message to show up when delete or save button is clicked
	 * @param: Timeout for which pop up message needs to be located
	 */
	public String waitForPopUpMsg(int timeToLocateElemInSec) throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]";
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
	 * Description: Retrieves the index value of given column from given factor table
	 * @param: Takes name of the factor table
	 * @param: Takes name of the column
	 * @return: Return the index position of given table column
	 * @throws: Exception
	 */
	private String getIndexPositionOfGivenColumnFromGivenFactorTable(String factorName, String columnName) throws Exception {
		String xpathTableHeader = null;
		if(factorName.equalsIgnoreCase("BPP Property Index Factors")) {
			xpathTableHeader = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'BPP Property Index Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//thead//tr//th";
		} else if(factorName.equalsIgnoreCase("BPP Percent Good Factors")) {
			xpathTableHeader = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'BPP Percent Good Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//thead//tr//th";
		} else if(factorName.equalsIgnoreCase("Imported Valuation Factors")) {
			xpathTableHeader = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'Imported Valuation Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//thead//tr//th";
		}

		int indexPositionOfGivenColumn = -1;
		List <WebElement> tableColumns = locateElements(xpathTableHeader, 30);	
		for(int i = 0; i < tableColumns.size(); i++) {
			int indexPos = i+1;
			String colName = getElementText(locateElement("("+ xpathTableHeader +")["+ indexPos +"]", 10));
			if(colName.equalsIgnoreCase(columnName)) {
				indexPositionOfGivenColumn = i;
				break;
			}
		}
		return Integer.toString(indexPositionOfGivenColumn);
	}
	
	/**
	 * Description:: Generates the row number to be read basis on the parameter provided
	 * @param: Takes Property Name as an argument like 'Agricultural', 'Commercial'
	 * @param: Roll Year
	 * @param: Name of the table whose rows are to be edited like 'BPP Property Index Factors'
	 * @returns: Index of the row to be edited
	 * @throws Exception
	 */
	private String getRowNumberToUpdate(String tableName, String rollYear, String propertyName) throws Exception {
		String expRowHeading = rollYear + "-" + propertyName;		
		String xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = '"+tableName+"']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr//th//a";
		
		List<WebElement> rowDetailsList = locateElements(xpath, 10);
		if(rowDetailsList == null) {
			rowDetailsList = locateElements(xpath, 10);
		}
		
		String currentRowHeading;
		int rowNumberToUpdate = -1;
		for(int i = 0; i < rowDetailsList.size(); i++) {
			currentRowHeading = getElementText(rowDetailsList.get(i));			
			if(currentRowHeading.equalsIgnoreCase(expRowHeading)) {
				rowNumberToUpdate = i + 1;
				break;
			}
		}
		return Integer.toString(rowNumberToUpdate);
	}
	
	
	/**
	 * Description: Retrieves the property type value from BPP Property Index Factors table
	 * @return: Return the property type value
	 * @throws: Exception
	 */
	public String readPropertyTypeValueFromBppPropIndexFactors(String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate("BPP Property Index Factors", TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String indexOfPropertyType = getIndexPositionOfGivenColumnFromGivenFactorTable("BPP Property Index Factors", "Property Type");		
		String xpathPropertyType = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'BPP Property Index Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr["+rowNum+"]//td["+ indexOfPropertyType +"]";
		String propertyTypeValue = getElementText(locateElement(xpathPropertyType, 20));
		return propertyTypeValue;
	}
	
	/**
	 * Description: Retrieves the good factor type value from BPP Percent Good Factors table
	 * @return: Return the good factor type value
	 * @throws: Exception
	 */
	public String readGoodFactorTypeValueFromBppPercentGoodFactors(String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate("BPP Percent Good Factors", TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String indexOfGoodFactorType = getIndexPositionOfGivenColumnFromGivenFactorTable("BPP Percent Good Factors", "Good Factor Type");
		String xpathGoodFactorType = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'BPP Percent Good Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr["+rowNum+"]//td["+ indexOfGoodFactorType +"]";
		String goodFactorTypeValue = getElementText(locateElement(xpathGoodFactorType, 20));
		return goodFactorTypeValue;
	}
	
	public String readPercentGoodFactorsMandatoryValuesFromDetailsPage() throws Exception {
		String xpath = "//span[text() = 'BPP Trend Setup']//ancestor::div[contains(@class, 'slds-page-header')]//following-sibling::div//thead//tr//th[contains(@class, 'slds-is-resizable')]//span[@class ='slds-truncate']";
	
		
		return "";
	}
	
	/**
	 * Description: Retrieves the property type value from Imported Valuation Factors table
	 * @return: Return the property type value
	 * @throws: Exception
	 */
	public String readPropertyTypeValueFromImportedValuationFactors(String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate("Imported Valuation Factors", TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String indexOfPropertyType = getIndexPositionOfGivenColumnFromGivenFactorTable("Imported Valuation Factors", "Property Type");		
		String xpathPropertyType = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'Imported Valuation Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr["+rowNum+"]//td["+ indexOfPropertyType +"]";
		String propertyTypeValue = getElementText(locateElement(xpathPropertyType, 20));
		return propertyTypeValue;
	}
	
	/**
	 * Description: Retrieves the year acquired value from given factor table
	 * @return: Return the year acquired value
	 * @throws: Exception
	 */
	public String readAcquiredYearValueFromGivenFactorTable(String factorTableName, String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate(factorTableName, TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String indexOfYearAcquired = getIndexPositionOfGivenColumnFromGivenFactorTable(factorTableName, "Year Acquired");
		String xpathYear = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = '"+ factorTableName +"']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr["+rowNum+"]//td["+ indexOfYearAcquired +"]";
		String yearAcquiredValue = getElementText(locateElement(xpathYear, 20));
		return yearAcquiredValue;
	}

	/**
	 * Description: Retrieves the Name value from given factor table
	 * @return: Return the name value
	 * @throws: Exception
	 */
	public String readNameValueFromGivenFactorTable(String factorTableName, String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate(factorTableName, TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String xpathName = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = '"+ factorTableName +"']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr["+rowNum+"]//th[1]//a";
		String nameValue = getElementText(locateElement(xpathName, 20));
		return nameValue;
	}
	
	/**
	 * Description: Retrieves the Index Factor value from given factor table
	 * @return: Return the Index Factor value
	 * @throws: Exception
	 */
	public String readIndexFactorValue(String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate("BPP Property Index Factors", TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String indexOfIndexFactor = getIndexPositionOfGivenColumnFromGivenFactorTable("BPP Property Index Factors", "Index Factor");
		String xpathIndexFactor = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'BPP Property Index Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr["+rowNum+"]//td["+ indexOfIndexFactor +"]";
		String indexFactorValue = getElementText(locateElement(xpathIndexFactor, 20));
		return indexFactorValue;
	}
	
	/**
	 * Description: Retrieves the Valuation Factor value from given factor table
	 * @return: Return the Valuation Factor value
	 * @throws: Exception
	 */
	public String readValuationFactorValue(String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate("Imported Valuation Factors", TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String indexOfValuationFactor = getIndexPositionOfGivenColumnFromGivenFactorTable("Imported Valuation Factors", "Valuation Factor");
		String xpathValuationFactor = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'Imported Valuation Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr["+rowNum+"]//td["+ indexOfValuationFactor +"]";
		String valuationFactorValue = getElementText(locateElement(xpathValuationFactor, 20));
		return valuationFactorValue;
	}
	
	/**
	 * Description: Retrieves the Valuation Factor value from given factor table
	 * @return: Return the Valuation Factor value
	 * @throws: Exception
	 */
	public Object isYearDataForAllRecordsUnique() throws Exception {
		String indexOfValuationFactor = getIndexPositionOfGivenColumnFromGivenFactorTable("Imported Valuation Factors", "Valuation Factor");
		String xpathValuationFactor = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'Imported Valuation Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr[1]//td["+ indexOfValuationFactor +"]";
		String valuationFactorValue = getElementText(locateElement(xpathValuationFactor, 20));
		return valuationFactorValue;
	}
	
	/**
	 * Description: Retrieves the Valuation Factor value from given factor table
	 * @return: Return the Valuation Factor value
	 * @throws: Exception
	 */
	public Object isYearDataForAllRecordsWithinValidRange() throws Exception {
		String indexOfValuationFactor = getIndexPositionOfGivenColumnFromGivenFactorTable("Imported Valuation Factors", "Valuation Factor");
		String xpathValuationFactor = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'Imported Valuation Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr[1]//td["+ indexOfValuationFactor +"]";
		String valuationFactorValue = getElementText(locateElement(xpathValuationFactor, 20));
		return valuationFactorValue;
	}
	
	/**
	 * Description: Retrieves the Valuation Factor value from given factor table
	 * @return: Return boolean as true if all values are within range or mismatched values
	 * @throws: Exception
	 */
	public Object isInflationValueForAllRecordsWithinValidRange() throws Exception {
		String indexOfValuationFactor = getIndexPositionOfGivenColumnFromGivenFactorTable("Imported Valuation Factors", "Valuation Factor");
		String xpathValuationFactor = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = 'Imported Valuation Factors']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr[1]//td["+ indexOfValuationFactor +"]";
		String valuationFactorValue = getElementText(locateElement(xpathValuationFactor, 20));
		return valuationFactorValue;
	}
	
	/**
	 * Description: It highlights the mismatched cell data of the table in red color
	 * @param tableName: Name of the table on BPP Trend page
	 * @param yearAcquired: Acquired year that is being validated
	 * @param indexValueOfCell: Table data/Cell number that is mismatched
	 * @throws Exception
	 */
	public void highlightMismatchedCellOnUI(String tableName, String yearAcquired, int indexValueOfCell) throws Exception {
		System.setProperty("isElementHighlightedDueToFailre", "true");
		indexValueOfCell = indexValueOfCell + 1;
		String xpathMisMatchedCell = "(//lightning-tab[contains(@data-id, '"+tableName+"')]//"
				+ "th//lightning-formatted-text[text() = '"+yearAcquired+"']//"
				+ "ancestor::th//following-sibling::td[not(contains(@data-label, 'Year Acquired'))]//lightning-formatted-text)["+ indexValueOfCell +"]";
		
		WebElement misMatchedCell = locateElement(xpathMisMatchedCell, 30);
		((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid red'", misMatchedCell);
	}
	
	
	/**
	 * Description: It clicks on the show more link on of given BPP composite factor setting on view all page
	 * @param settingName: It takes BPP composite factor setting as an argument like 'Industrial', 'Commercial' etc.
	 * @throws Exception
	 */
	public void clickShowMoreLinkOfBppSettingOnViewAllGrid(String settingName) throws Exception {
		String xpath = "//td//span[text() = '"+ settingName +"']//ancestor::td//following-sibling::td//a";
		WebElement showMoreLink = locateElement(xpath, 30);
		waitForElementToBeClickable(10, showMoreLink);
		javascriptClick(showMoreLink);
	}
	
	public WebElement checkAvailabilityOfBppSettingsDropDownButton() throws Exception {
		String otherElementToCheck = "//span[text() = 'Files']//parent::span[text() = 'View All']//ancestor::a";
		WebElement viewAllFiles = locateElement(otherElementToCheck, 10);
		waitForElementToBeClickable(viewAllFiles, 10);
		
		Click(bppSettingTab);
		
		String xpath = "//span[text() = 'BPP Settings']/ancestor::header//following-sibling::div//a[contains(@class, 'slds-button') and @role = 'button']";
		WebElement element = locateElement(xpath, 20);
		//waitForElementToBeClickable(element, 10);
		Thread.sleep(4000);
		return element;
	}

	/**
	 * Description: Creates the BPP Composite Factor Setting on BPP trend status page
	 * @param: Takes composite factor setting factor value as
	 * @throws: Exception
	 */
	public void createBppCompositeFactorSetting(String propertyType, String minGoodFactorValue, String...rollYear) throws Exception {
		WebElement moreTab = locateElement("//div[contains(@class, 'column region-sidebar-right')]//button[@title = 'More Tabs']", 10);
		if(moreTab != null) {
			waitForElementToBeClickable(moreTab, 10);
			clickAction(moreTab);
			waitForElementToBeVisible(bppCompositeFactorOption, 10);
			clickAction(bppCompositeFactorOption);
        } else {
			clickAction(bppCompFactorSettingTab);
		}
		
		waitForElementToBeVisible(dropDownIconBppCompFactorSetting, 10);
		waitForElementToBeClickable(dropDownIconBppCompFactorSetting, 10);
		clickAction(waitForElementToBeClickable(dropDownIconBppCompFactorSetting));
		
		waitForElementToBeVisible(newBtnToCreateEntry, 20);
		clickAction(waitForElementToBeClickable(newBtnToCreateEntry));

		if(rollYear.length == 1) {
			enterRollYearInBppSettingDetails(rollYear[0]);			
		}
		enterFactorValue(minGoodFactorValue);
		enterPropertyType(propertyType);
		Click(saveBtnInBppSettingPopUp);
		Thread.sleep(1000);
	}
	
	/**
	 * Description: It updates the BPP composite factor settings on view all page
	 * @param: Takes a data map containing trend settings names as map keys and their values as map values
	 * @throws: Throws Exception
	 */
	public void editBppCompositeFactorValueOnViewAllPage(Map<String, String> updatedSettingValues) throws Exception {
		WebElement moreTab = locateElement("//div[contains(@class, 'column region-sidebar-right')]//button[@title = 'More Tabs']", 10);

		if(moreTab != null) {
            clickAction(moreTab);
			clickAction(bppCompositeFactorOption);
        } else {
			clickAction(bppCompFactorSettingTab);
		}
		clickAction(viewAllBppCompositeFactorSettings);

		for(Map.Entry<String, String> entry : updatedSettingValues.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			clickShowMoreLinkOfBppSettingOnViewAllGrid(key);
			clickAction(waitForElementToBeClickable(objBuildPermitPage.editLinkUnderShowMore));
			enterFactorValue(value);
			Click(waitForElementToBeClickable(objBuildPermitPage.saveBtnEditPopUp));
		}
	}
	
	/**
	 * Description: It deletes the BPP composite factor settings from view all page
	 * @param: Takes a data map containing trend settings names as map keys and their values as map values
	 * @throws: Throws Exception
	 */
	public void deleteBppCompositeFactorValueOnViewAllPage(String[] namesOfSettingsToDelete) throws Exception {		
		waitForElementToBeClickable(10, moreTabRightSection);
		clickAction(moreTabRightSection);

		waitForElementToBeClickable(10, bppCompositeFactorOption);
		clickAction(bppCompositeFactorOption);

		clickAction(waitForElementToBeClickable(viewAllBppCompositeFactorSettings));
		
		for(int i = 0; i < namesOfSettingsToDelete.length; i++) {
			clickShowMoreLinkOfBppSettingOnViewAllGrid(namesOfSettingsToDelete[i]);
			clickAction(waitForElementToBeClickable(objBuildPermitPage.deleteLinkUnderShowMore));
			clickAction(objBuildPermitPage.deleteLinkUnderShowMore);
			Click(waitForElementToBeClickable(objBuildPermitPage.deleteBtnInDeletePopUp));			
		}
	}
	
	/**
	 * Description: Clicks on the show more link displayed against the given entry
	 * @param entryDetails: Name of the entry displayed on grid which is to be accessed
	 * @throws Exception
	 */
	public void clickOnShowMoreLinkInGridForGivenProprtyType(String propertyName) throws Exception {
		String xpathShowMoreLink = "//table//tbody/tr//td//span[text() = '"+ propertyName +"']//parent::span//parent::td//following-sibling::td//a[@role = 'button']";
		Thread.sleep(2000);
		WebElement modificationsIcon = locateElement(xpathShowMoreLink, 20);
		clickAction(modificationsIcon);
	}
	
	/**
	 * Description: Finds the show more link displayed against the given entry
	 * @param entryDetails: Name of the entry displayed on grid which is to be accessed
	 * @throws Exception
	 */
	public String retrieveExistingMinEquipFactorValueFromGridForGivenProprtyType(String propertyName) throws Exception {
		String xpathShowMoreLink = "//table//tbody/tr//td//span[text() = '"+ propertyName +"']//parent::span//parent::td//following-sibling::td//span[@class = 'slds-truncate uiOutputNumber']";
		return getElementText(locateElement(xpathShowMoreLink, 20));
	}
	
	/**
	 * Description: Creates the BPP Setting on BPP trend status page
	 * @param: Takes equipment factor value
	 * @throws: Exception
	 */
	public void createBppSetting(String equipIndexFactorValue) throws Exception {
		clickAction(waitForElementToBeClickable(dropDownIconBppSetting));
		clickAction(waitForElementToBeClickable(newBtnToCreateEntry));
		enterFactorValue(equipIndexFactorValue);
		Click(saveBtnInBppSettingPopUp);
	}
	
	/**
	 * Description: Edits the BPP Setting on BPP trend status page
	 * @param: Takes equipment factor value
	 * @throws: Exception
	 */
	public void editBppSettingValueOnDetailsPage(String equipIndexFactorValue) throws Exception {
		waitForElementToBeClickable(dropDownIconDetailsSection, 10);
		clickAction(dropDownIconDetailsSection);
		waitForElementToBeClickable(objBuildPermitPage.editLinkUnderShowMore, 10);
		clickAction(objBuildPermitPage.editLinkUnderShowMore);
		enterFactorValue(equipIndexFactorValue);
	}
	
	/**
	 * Description: Deletes the BPP Setting on BPP trend status page
	 * @throws: Exception
	 */
	public void deleteBppSettingValueOnDetailsPage() throws Exception {
		clickAction(dropDownIconDetailsSection);

		waitForElementToBeVisible(objBuildPermitPage.deleteLinkUnderShowMore, 10);
		clickAction(objBuildPermitPage.deleteLinkUnderShowMore);
		Click(waitForElementToBeClickable(objBuildPermitPage.deleteBtnInDeletePopUp));
	}
	
	/**
	 * @Description : Reads given excel file to retrieve total rows in each sheet the data into a map.
	 * @param filePath: Takes the path of the XLSX workbook
	 * @return: Return a data map
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
					//int currentRollYear = Integer.parseInt(TestBase.CONFIG.getProperty("rollYear"));
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
	 * Description: Generates a map having sheet names of excel as keys and column names to be updated as values
	 */
	public Map<String, String> mapHavingtableNamesAndEditedColumnName() throws Exception {
		List<String> SheetNamesWithColumnNames = new ArrayList<String>();
		SheetNamesWithColumnNames.add(TestBase.CONFIG.getProperty("SheetNameWithColumnHarvestAverage"));
		SheetNamesWithColumnNames.add(TestBase.CONFIG.getProperty("SheetNameWithColumnNumericvalue"));
		SheetNamesWithColumnNames.add(TestBase.CONFIG.getProperty("SheetNameWithColumnAgricultural"));
		SheetNamesWithColumnNames.add(TestBase.CONFIG.getProperty("SheetNameWithColumnConstruction"));
		SheetNamesWithColumnNames.add(TestBase.CONFIG.getProperty("SheetNamesWithColumnAverage"));
		
		Map<String, String> dataMap = new HashMap<String, String>();
		
		for(int i = 0; i < SheetNamesWithColumnNames.size(); i++) {
			List<String> tempList = Arrays.asList(SheetNamesWithColumnNames.get(i).split(":"));			
			if(tempList.get(0).contains(",")) {
				List<String> listSheetNamesWithSameColumnName = Arrays.asList(tempList.get(0).split(","));
				for(int j = 0; j < listSheetNamesWithSameColumnName.size(); j++) {
					dataMap.put(listSheetNamesWithSameColumnName.get(j), tempList.get(1));		
				}
			} else {
				dataMap.put(tempList.get(0), tempList.get(1));	
			}
		}
		return dataMap;
	}

	/**
	 * Description: This method reads the excel file, retrieves original values of cell from various sheets that re to be updated with Junk value
	 * @param: Takes complete path of the file as the argument
	 */
	@SuppressWarnings("deprecation")
	public Map<String, Map<String, List<String>>> updateExcelDataForGivenFactorTables(String filePath) throws Exception {		
		//Step1: Creating a map by using above list, this map holds sheet name as "Keys" and column names as "Values"
		Map<String, String> dataMap = mapHavingtableNamesAndEditedColumnName();
		
		Map<String, Map<String, List<String>>> dataMapHavingOriginalValues = new HashMap<String, Map<String, List<String>>>();
		FileInputStream file = null;
		XSSFWorkbook workBook = null;
		try {
            file = new FileInputStream(new File(filePath));
            workBook = new XSSFWorkbook(file);
            Set<String> sheetNames = dataMap.keySet();
            Map<String, List<String>> dataMapWithColNameAndOriginalValues = new HashMap<String, List<String>>();
            
    		for(String sheetName : sheetNames) {
    			XSSFSheet sheet = workBook.getSheet(sheetName);
    			int rowCount = sheet.getPhysicalNumberOfRows();
    			if(rowCount > 0) {
    	            Row headerRow = sheet.getRow(0);
    	            int cellIndexOfAverageCol = -1;
    	            int totalCells = headerRow.getLastCellNum();
    	            String columnName = dataMap.get(sheetName).toString();    	            
    	            
    	            FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();                
    	            for(int i = 0; i < totalCells; i++) {
                		Cell cell = headerRow.getCell(i);
                		CellValue cellData = evaluator.evaluate(cell);
                		String cellValue = null;                		
                		switch (cellData.getCellType()) {
                		    case Cell.CELL_TYPE_STRING:
                		    	cellValue = cellData.getStringValue();
                		        break;
                		    case Cell.CELL_TYPE_NUMERIC:
                		        int integerValue = (int) cellData.getNumberValue(); 
                		        cellValue = Integer.toString(integerValue);
                		        break;
                		}
    	            	
    	            	if(cellValue.toUpperCase().equals(columnName.toUpperCase())) {
    	            		cellIndexOfAverageCol = i;
    	            		columnName = cellValue;
    	            		break;
    	            	}
    	            }
    	            
    	            if(cellIndexOfAverageCol > -1) {
    	            	String[] updatedValues = TestBase.CONFIG.getProperty("junkValueaForRows").split(",");
        	            int recordsToManipulate = Integer.parseInt(TestBase.CONFIG.getProperty("errorRecordsCount"));
        	            
        	            List<String> listOfOriginalExcelValues = new ArrayList<String>();
        	            for(int i = 1; i <= recordsToManipulate; i++) {
        	            	Cell cell = null;
        	    	        cell = sheet.getRow(i).getCell(cellIndexOfAverageCol);
        	    	        
        	    	        CellValue cellData = evaluator.evaluate(cell);
                    		String originalValue = null;                		
                    		switch (cellData.getCellType()) {
                    		    case Cell.CELL_TYPE_STRING:
                    		    	originalValue = cellData.getStringValue();
                    		        break;
                    		    case Cell.CELL_TYPE_NUMERIC:
                    		        int integerValue = (int) cellData.getNumberValue(); 
                    		        originalValue = Integer.toString(integerValue);
                    		        break;
                    		}

        	    	        listOfOriginalExcelValues.add(originalValue);
        	    	        String junkValueToEnter = updatedValues[(i - 1)];
        	    	        
        	    	        cell.setCellType(Cell.CELL_TYPE_STRING);
        	    	        cell.setCellValue(junkValueToEnter);
        	            }
        	            
        	            System.setProperty(columnName, Integer.toString(cellIndexOfAverageCol));
        	            dataMapWithColNameAndOriginalValues.put(Integer.toString(cellIndexOfAverageCol), listOfOriginalExcelValues);
        	            dataMapHavingOriginalValues.put(sheetName, dataMapWithColNameAndOriginalValues);
        	            
    	            } else {
    	            	System.out.println("Column name '"+ dataMap.get(sheetName) +"' is not found in sheet name '"+ sheetName +"'");
    	            }
    			} else {
    				System.out.println("Table name '"+ sheetName +"' does not any data in excel file. Please check & retry!!");
    			}
    		}
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
        	workBook.close();
        	file.close();
        }
		return dataMapHavingOriginalValues;
	}
	
	/**
	 * Description: Retrieves the index value of specified column form history table
	 * @param: Takes name of the column from history table on Efile import page
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
				
		String xpath = null;
		int indexDifference = indexPositionOfName - indexPositionOfStatus;
		int elementIndex = Math.abs(indexDifference);

		if(indexPositionOfName < indexPositionOfStatus) {
			xpath = "((//a[contains(@title, '"+ name +"')])[1])//ancestor::th//following-sibling::td["+elementIndex+"]";
		} else {
			xpath = "((//a[contains(@title, '"+ name +"')])[1])//ancestor::th//preceding-sibling::td["+elementIndex+"]";			
		}
		String fileStatusOnImportPage = getElementText(locateElement(xpath, 30));
		return fileStatusOnImportPage;
	}
	
	/**
	 * Description: Retrieves the value of given field from import log details page
	 * @param: Takes the name of the field as argument
	 * @return: Return the String value
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
		
		String xpath = null;
		int indexDifference = indexPositionOfImportLog - indexPositionOfStatus;
		int elementIndex = Math.abs(indexDifference);
		
		if(indexPositionOfImportLog < indexPositionOfStatus) {
			xpath = "((//a[contains(@title, '"+ name +"')])[1])//ancestor::td//following-sibling::td["+elementIndex+"]";
		} else {
			xpath = "((//a[contains(@title, '"+ name +"')])[1])//ancestor::td//preceding-sibling::td["+elementIndex+"]";			
		}
		
		String fileStatusOnImportPage = getElementText(locateElement(xpath, 30));
		return fileStatusOnImportPage;
	}
	
	/**
	 * Description: Return value of Error Message column from tables based on given parameters
	 * @param tableName: Name of the table which is currently selected
	 * @param rowNumber: Row number in which element needs to be located
	 * @param columnName: Name of column whose value needs to be read
	 * @return: Return the value
	 * @throws Exception
	 */
	public String getErrorMessageFromTable(String tableName, int rowNumber) throws Exception {
		String columnName = "Error Message";
		String message = readDataFromBppTrendFactorTableOnEfileImportPage(tableName, rowNumber, columnName);
		return message;
	}
	
	/**
	 * Description: Return value of Age column from tables based on given parameters
	 * @param tableName: Name of the table which is currently selected
	 * @param rowNumber: Row number in which element needs to be located
	 * @param columnName: Name of column whose value needs to be read
	 * @return: Return the value
	 * @throws Exception
	 */
	public String getAgeValueFromTable(String tableName, int rowNumber, String columnName) throws Exception {
		return readDataFromBppTrendFactorTableOnEfileImportPage(tableName, rowNumber, columnName);
	}
	
	/**
	 * Description: Return value of Average column from tables based on given parameters
	 * @param tableName: Name of the table which is currently selected
	 * @param rowNumber: Row number in which element needs to be located
	 * @param columnName: Name of column whose value needs to be read
	 * @return: Return the value
	 * @throws Exception
	 */
	public String getAverageValueFromTable(String tableName, int rowNumber, String columnName) throws Exception {
		return readDataFromBppTrendFactorTableOnEfileImportPage(tableName, rowNumber, columnName);
	}
	
	/**
	 * Description: Return value of Year column from tables based on given parameters
	 * @param tableName: Name of the table which is currently selected
	 * @param rowNumber: Row number in which element needs to be located
	 * @param columnName: Name of column whose value needs to be read
	 * @return: Return the value
	 * @throws Exception
	 */
	public String getYearValueFromTable(String tableName, int rowNumber, String columnName) throws Exception {
		return readDataFromBppTrendFactorTableOnEfileImportPage(tableName, rowNumber, columnName);
	}
	
	/**
	 * Description: Generates xpath for elements in table based on given parameters on approve and revert page in Efile import
	 * @param tableName: Name of the table which is currently selected
	 * @param rowNumber: Row number in which element needs to be located
	 * @param columnName: Name of column whose value needs to be read
	 * @return: Return value read from table
	 * @throws Exception
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
	public String getnerateExpectedErrorMsgForTableColumn(String columnName, String columnValue) throws Exception {
		if("Age".equalsIgnoreCase(columnName)) {
			if("".equals(columnValue)) {
				return "Age must be present";
			}
			else {
				if(isColumnValueOnlyNumeric(columnValue)) {
					if(Integer.parseInt(columnValue) == 0) {
						return "Age should be between 1 and 40";
					} else if(Integer.parseInt(columnValue) > 40) {
						return "Age must be less than 40";
					}
				} 
				else {
					return "Age must be numeric without any sign";
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
					return "Average must be numeric without any sign";
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
						if(isGivenYearVaild(columnValue)) {
							return "DUPLICATE_VALUE:duplicate value found";
						} else {
							if(Integer.parseInt(columnValue) < 1974) {
								return "Year must be greater than 1974";
							} else if(Integer.parseInt(columnValue) > Integer.parseInt(TestBase.CONFIG.getProperty("rollYear"))){
								return "Year must be less than 2020";
							}
						}
					} else {
						return "Year must be numeric without any sign";
					}
				} 
				else {
					if(isColumnValueOnlyNumeric(columnValue)) {
						if(isGivenYearVaild(columnValue)) {
							return "DUPLICATE_VALUE:duplicate value found";
						} else {
							if(Integer.parseInt(columnValue) < 1974) {
								return "Year must be greater than 1974";
							} else if(Integer.parseInt(columnValue) > Integer.parseInt(TestBase.CONFIG.getProperty("rollYear"))){
								return "Year must be less than "+ TestBase.CONFIG.getProperty("rollYear");
							}
						}
					} else {
						return "Year must be numeric without any sign";
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
	private boolean isGivenYearVaild(String year) throws Exception {
		int givenYear = Integer.parseInt(year);
		if(givenYear > 1974 && givenYear < Integer.parseInt(TestBase.CONFIG.getProperty("rollYear"))) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Description: Create a new bpp trend setup with no bpp settings and no bpp composite settings and no data uploaded
	 * @return: Return the name of the bpp trend setup created
	 * @throws: Exception
	 */
	public String createDummyBppTrendSetupForErrorsValidation(String... compFactorTablesStatus) throws Exception {
		//Step3: Click New button on the grid to open form / pop up to create new BPP Trend Setup
		WebElement newButton = objPage.locateElement("//div[contains(@class, 'headerRegion forceListViewManagerHeader')]//a[@title = 'New']", 10);
		Click(newButton);
		
		//Step4: Entering BPP trend setup name and roll year
		int year = Integer.parseInt(TestBase.CONFIG.getProperty("rollYear")) + 2;
		String trendSetupName = year + " " + TestBase.CONFIG.getProperty("bppTrendSetupNameSuffix");

		System.setProperty("trendSetupForErrorValidationOnCalcuate", trendSetupName);
		System.setProperty("rollYearForErrorValidationOnCalculate", Integer.toString(year));
		enter(bppTrendSetupName, trendSetupName);
		WebElement rollYearField = locateElement("//span[text() = 'Roll Year']//parent::span//following-sibling::div", 10);

		//Step3: 
		objApasGenericPage.selectOptionFromDropDown(rollYearField, Integer.toString(year));
		
		//Step4: Setting the status of composite factor tables to Not Calculated
		if(compFactorTablesStatus.length == 1) {
			objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Commercial Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus[0]);
			objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Const. Mobile Equipment Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus[0]);
			objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Industrial Trend Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus[0]);
			objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Ag. Mobile Equipment Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus[0]);
			objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Const. Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus[0]);
			objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Ag. Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus[0]);
		}
		
		//Step5: Clicking save button
		Click(saveBtnInBppSettingPopUp);
		Thread.sleep(2000);
				
		return trendSetupName;
	}
	
	/**
	 * Description: Clicks on calculate button to perform calculation with missing Bpp Settings in selected roll year
	 * @returns: Return the error message displayed in pop up
	 * @throws: Exception
	 * calculationWith_BppCompFactorSettings_MissingInSystem
	 */
	public String calculation_With_Missing_BppSetting(String tableName, boolean isTableUnderMoreBtn) throws Exception {		
		//Clicking on given table name and then calculate button
		clickOnTableOnBppTrendPage(tableName, isTableUnderMoreBtn);
		clickCalculateBtn(tableName);
		
		//Retrieve and return the error message displayed in pop up on clicking calculate button
		return waitForErrorPopUpMsgOnCalculateClick(20);
	}
	
	/**
	 * Description: Clicks on calculate button to perform calculation with missing Bpp Composite Factor Settings in selected roll year
	 * @returns: Return the error message displayed in pop up
	 * @throws: Exception
	 */
	public String calculation_With_Missing_BppCompFactorSettings(String tableName, boolean isTableUnderMoreBtn) throws Exception {		
		//Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Clicking on the roll year name in grid to navigate to details page of selected roll year
		String rollYear = System.getProperty("rollYearForErrorValidationOnCalculate");
		clickOnEntryNameInGrid(rollYear);
		
		//Create a BPP Setting under selected BPP Trend Setup
		createBppSetting("125");
		
		//Navigating to BPP Trend page and selecting given roll year
		Thread.sleep(2000);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		waitForElementToBeClickable(rollYearDropdown, 30);
		Click(rollYearDropdown);
		clickOnGivenRollYear(rollYear);
		Click(selectRollYearButton);

		//Clicking on given table name and then calculate button
		clickOnTableOnBppTrendPage(tableName, isTableUnderMoreBtn);
		clickCalculateBtn(tableName);
		
		//Retrieve and return the error message displayed in pop up on clicking calculate button
		return waitForErrorPopUpMsgOnCalculateClick(20);
	}
	
	/**
	 * Description: Clicks on calculate button to perform calculation with missing Bpp Composite Factor Settings in selected roll year
	 * @returns: Return the error message displayed in pop up
	 * @throws: Exception
	 */
	public String calculation_With_Missing_IndexAndGoodFactors(String tableName, boolean isTableUnderMoreBtn) throws Exception {		
		//Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Clicking on the roll year name in grid to navigate to details page of selected roll year
		String rollYear = System.getProperty("rollYearForErrorValidationOnCalculate");
		clickOnEntryNameInGrid(rollYear);
		
		//Create a BPP Composite Factor Settings
		if(tableName.contains("Commercial")) {
			createBppCompositeFactorSetting("Commercial", "10");	
		} else if(tableName.contains("Industrial")) {
			createBppCompositeFactorSetting("Industrial", "9");	
		} else if(tableName.contains("Agricultural")) {
			createBppCompositeFactorSetting("Agricultural", "11");	
		} else if(tableName.contains("Construction")) {
			createBppCompositeFactorSetting("Construction", "10");	
		}

		//Navigating to BPP Trend page and selecting given roll year
		Thread.sleep(2000);
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS);
		waitForElementToBeClickable(rollYearDropdown, 30);
		Click(rollYearDropdown);
		clickOnGivenRollYear(rollYear);
		Click(selectRollYearButton);

		//Clicking on given table name and then calculate button
		clickOnTableOnBppTrendPage(tableName, isTableUnderMoreBtn);
		clickCalculateBtn(tableName);
		
		//Retrieve and return the error message displayed in pop up on clicking calculate button
		return waitForErrorPopUpMsgOnCalculateClick(20);
	}
	
	/**
	 * Description: Delete BPP Trend Setup entry
	 */
	public void deleteDummyBppTrendSetup(String bppTrendSetupName) throws Exception {
		if(bppTrendSetupName != null) {
			//Opening the BPP Trend module and set All as the view option in grid
			objApasGenericFunctions.login(users.SYSTEM_ADMIN);
			objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
			objApasGenericFunctions.selectAllOptionOnGrid();
			
			objBuildPermitPage.clickShowMoreLinkOnRecentlyViewedGrid(bppTrendSetupName);
			waitForElementToBeClickable(objBuildPermitPage.deleteLinkUnderShowMore, 10);
			javascriptClick(objBuildPermitPage.deleteLinkUnderShowMore);

			waitForElementToBeClickable(deletBtnInPopUp, 10);
			Click(deletBtnInPopUp);
		}
	}
	
	/**
	 * Description: Retrieves the value from maximum equipment index factor value from BPP Setting pop up
	 * @throws: Exception
	 */
	public String retrieveMaxEqipIndexValueFromPopUp() throws Exception {
		String xpathMaxEquipIndexFactorValue = "//div[text() = 'Maximum Equipment index Factor:']/following-sibling::div//span";
		return getElementText(locateElement(xpathMaxEquipIndexFactorValue, 20));
	}
	
	/**
	 * Description: Retrieve the name of newly created BPP Setting
	 * @throws: Exception
	 */
	public String retrieveBppSettingName() throws Exception {
		String xpathNewBppSetting = "//span[contains(text(), 'BPP Settings')]//ancestor::div[contains(@class, 'forceRelatedListCardHeader')]//following-sibling::div//h3//a";
		return getElementText(locateElement(xpathNewBppSetting, 20));
	}
	
	/**
	 * Description: Retrieves the value from maximum equipment index factor value from BPP Setting pop up
	 * @throws: Exception
	 */
	public String retrieveMaxEqipIndexValueFromViewAllGrid(String bppSettingName) throws Exception {
		String xpathForMaxEqipIndexInGrid = "//tbody/tr//th//a[text() = '"+ bppSettingName +"']//parent::span//parent::th//following-sibling::td//span[contains(text(), '%')]";
		return getElementText(locateElement(xpathForMaxEqipIndexInGrid, 20));
	}

	/**
	 * Description: Clicks the junk data cell in the selected table and updated the correct value in it
	 * @param tableNumber: Number of the table
	 * @param updatedValue: Value to be entered
	 * @throws Exception
	 */
	public void updateCorrectDataInTable(String tableNumber, String updatedValue) throws Exception {
		String junkDataCellXpath = "(//lightning-tab[@aria-labelledby = '"+tableNumber+"__item']//span[contains(@title,'ERROR ROWS')]//ancestor::div[@class = 'slds-accordion__summary']//following-sibling::div//table//lightning-formatted-text[starts-with(text(), 'Junk_')])";
		Click(locateElement(junkDataCellXpath, 10));
		
		String xpathEditIcon = "//lightning-primitive-cell-factory[@class = 'slds-cell-wrap slds-has-focus']//span[text() = 'Edit Average' or text() = 'Edit Factor' or text() = 'Edit Valuation Factor']//ancestor::button//lightning-primitive-icon";
		WebElement editIcon = locateElement(xpathEditIcon, 10);
		javascriptClick(editIcon);
		enter(inputBoxOnImportPage, updatedValue);
		enter(inputBoxOnImportPage, Keys.TAB);
		
	}
	
	/**
	 * Description: Retrieves the value of maximum factor value from the grid
	 * @return: Return the value as String
	 * @throws: Exception
	 */
	public String retrieveFactorValueFromGrid(String bppSettingName) throws Exception {
		String xpathForFactorValueInGrid = "//tbody/tr//th//a[text() = '"+ bppSettingName +"']//parent::span//parent::th//following-sibling::td//span[contains(text(), '%')]";
		return getElementText(locateElement(xpathForFactorValueInGrid, 10));
	}
	
	/**
	 * DescriptioN: Checks whether element is displayed on the web page
	 * @param: WebElement
	 * @return: Returns true / false bases on availability of element
	 * @throws: Exception 
	 */
	public boolean isElementAvailable(WebElement element, int timeOutInSec) throws Exception {
		try {
			waitForElementToBeVisible(element, timeOutInSec);
			return true;
		} catch(TimeoutException ex) {
			return false;
		} catch(NoSuchElementException ex) {
			return false;
		} catch(StaleElementReferenceException ex) {
			return false;
		}
	}
	
}