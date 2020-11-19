package com.apas.PageObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import org.apache.log4j.Logger;	
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class BppTrendSetupPage extends Page {

	Logger logger = Logger.getLogger(LoginPage.class);
	BuildingPermitPage objBuildPermitPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	Util objUtil;
	Page objPage;
	public String rollYearForErrorValidationOnCalculate;

	public BppTrendSetupPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objPage = new Page(driver);
		objUtil = new Util();
	}

	@FindBy(xpath = "//a[@title = 'New'")
	public WebElement newButton;

	@FindBy(xpath = "//a[text()='No actions available']")
	public WebElement NoActionAvailableMenuItem;

	@FindBy(xpath = "//span[text() = 'BPP Composite Factors Settings']//parent::a")
	public WebElement bppCompositeFactorOption;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[text() = 'BPP Property Index Factors']")
	public WebElement bppPropertyIndexFactorsTab;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text()='BPP Property Index Factors']//ancestor::lst-list-view-manager-header//following-sibling::div//table//span[@title = 'Name (Roll Year - Property Type)']")
	public WebElement bppPropertyIndexFactorsTableSection;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[text() = 'BPP Percent Good Factors']")
	public WebElement bppPropertyGoodFactorsTab;

	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//a[text() = 'BPP Percent Good Factors']//ancestor::lightning-tab-bar//following-sibling::slot//table")
	public WebElement bppPercentGoodFactorsTableSection;

	@FindBy(xpath = "//div[contains(@class, 'column region-main')]//li[not(contains(@style,'visibility: hidden'))]//button[@title = 'More Tabs']")
	public WebElement moreTabLeftSection;

	@FindBy(xpath = "//button[@aria-expanded = 'true']//following-sibling::div//span[text() = 'Imported Valuation Factors']//parent::a")
	public WebElement dropDownOptionBppImportedValuationFactors;

	@FindBy(xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//a[text() = 'Imported Valuation Factors']//ancestor::lightning-tab-bar//following-sibling::slot//table")
	public WebElement bppImportedValuationFactorsTableSection;

	@FindBy(xpath = "//div[contains(@class, 'column region-sidebar-right')]//button[@title = 'More Tabs']")
	public WebElement moreTabRightSection;

	@FindBy(xpath = "//a[text() = 'BPP Composite Factors Settings']")
	public WebElement bppCompFactorSettingTab;

	@FindBy(xpath = "//span[text() = 'BPP Settings']//ancestor::div[contains(@class,'firstHeaderRow')]//following-sibling::div[@class='actionsWrapper']//a")
	public WebElement dropDownIconBppSetting;

	@FindBy(xpath = "//span[text() = 'BPP Composite Factors Settings']//ancestor::div[contains(@class,'firstHeaderRow')]//following-sibling::div[@class='actionsWrapper']//a")
	public WebElement dropDownIconBppCompFactorSetting;

	@FindBy(xpath = "//a[@title = 'New']")
	public WebElement newBtnToCreateEntry;

	@FindBy(xpath = "//span[contains(text(), 'Roll Year')]//parent::label//following-sibling::div//input[contains(@class, 'uiInputTextForAutocomplete')]")
	public WebElement rollYearTxtBox;

	@FindBy(xpath = "//lst-list-view-row-level-action//button | //lst-list-view-row-level-action//a")
	public WebElement dropDownIconDetailsSection;

	@FindBy(xpath = "//div[@class = 'actionsContainer']//div[contains(@class, 'slds-float_right')]//button[@title = 'Save']")
	public WebElement saveBtnInBppSettingPopUp;

	@FindBy(xpath = "//button[@title = 'Cancel']//span[text() = 'Cancel']")
	public WebElement cancelBtnInBppSettingPopUp;

	public String xPathErrorMsg = "//div[@class='uiBlock']//p[@class='detail']//span";

	@FindBy(xpath = "//div[@class='uiBlock']//p[@class='detail']//span")
	public WebElement errorMsgforEdit;

	@FindBy(xpath = "//span[text()='Close this window']//ancestor::button")
	public WebElement closeErrorPopUp;

	@FindBy(xpath = "//a[@class = 'deleteAction']")
	public WebElement deleteIconForPrePopulatedRollYear;

	@FindBy(xpath = "//span[contains(text(), 'Factor')]//parent::label//following-sibling::input")
	public WebElement factorTxtBox;

	@FindBy(xpath = "//span[text() = 'BPP Settings']//parent::span[text() = 'View All']")
	public WebElement viewAllBppSettings;

	@FindBy(xpath = "//span[text()='BPP Composite Factors Settings']//ancestor::lst-common-list//following-sibling::a//span[text() = 'View All']")
	public WebElement viewAllBppCompositeFactorSettings;

	@FindBy(xpath = "//span[text() = 'Name']//parent::label//following-sibling::input")
	public WebElement bppTrendSetupName;

	@FindBy(xpath = "//span[text() = 'Property Type']//parent::span/following-sibling::div")
	public WebElement propertyType;
	
	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;
	
	@FindBy(xpath = "//div[@role='dialog']//button//*[text() = 'Save']")
	public WebElement saveButton;
	
	@FindBy(xpath = "//ul[@class='errorsList']//li")
	public WebElement errorMsgOnTop;
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Edit']")
	public WebElement editLinkUnderShowMore;
	
	@FindBy(xpath = "//button[@title='Close this window']")
	public WebElement closeEntryPopUp;
	
	@FindBy(xpath = "//button[@title = 'Close']")
	public WebElement successAlertCloseBtn;
	
	@FindBy(xpath = "//button[text() = 'Edit']")
	public WebElement editButton;

	@FindBy(xpath = "//button[text() = 'Delete']")
	public WebElement deleteButton;
	
	@FindBy(xpath = "//p[@class='detail']")
	public WebElement accessErrorMsg;

	@FindBy(xpath = "//div[contains(@class,'fieldLevelErrors')]//li")
	public WebElement fieldError;

	/**
	 * Description: Creates the BPP Composite Factor Setting on BPP trend status page
	 * @param propertyType: Takes composite factor setting factor value as
	 * @param minGoodFactorValue: Takes composite factor setting factor value as
	 * @throws: Exception
	 */
	public void createBppCompositeFactorSetting(String propertyType, String minGoodFactorValue) throws Exception {
		WebElement moreTab = locateElement("//div[contains(@class, 'column region-sidebar-right')]//button[@title = 'More Tabs']", 10);
		if(moreTab != null) {
			waitForElementToBeClickable(moreTab, 10);
			clickAction(moreTab);
			waitForElementToBeVisible(bppCompositeFactorOption, 10);
			clickAction(bppCompositeFactorOption);
		} else {
			clickAction(bppCompFactorSettingTab);
		}

		clickAction(dropDownIconBppCompFactorSetting);
		clickAction(newBtnToCreateEntry);

		enter("Minimum Good Factor",minGoodFactorValue);
		objApasGenericPage.selectOptionFromDropDown("Property Type",propertyType);
		Click(objPage.getButtonWithText("Save"));

		Thread.sleep(1000);
	}

	/**
	 * Description: Clicks on the show more link displayed against the given entry
	 * @param propertyName: Name of the entry displayed on grid which is to be accessed
	 * @throws: Exception
	 */
	public void clickOnShowMoreLinkInGridForGivenPropertyType(String propertyName) throws Exception {
		String xpathShowMoreLink = "//table//tbody/tr//td//span[text() = '"+ propertyName +"']//parent::span//parent::td//following-sibling::td//a[@role = 'button']";
		Thread.sleep(2000);
		WebElement modificationsIcon = locateElement(xpathShowMoreLink, 20);
		clickAction(modificationsIcon);
	}

	/**
	 * Description: Finds the show more link displayed against the given entry
	 * @param propertyName: Name of the entry displayed on grid which is to be accessed
	 * @throws: Exception
	 */
	public String retrieveExistingMinEquipFactorValueFromGrid(String propertyName) throws Exception {
		String xpathShowMoreLink = "//table//tbody/tr//td//span[text() = '"+ propertyName +"']//parent::span//parent::td//following-sibling::td//span[@class = 'slds-truncate uiOutputNumber']";
		return getElementText(locateElement(xpathShowMoreLink, 20));
	}

	/**
	 * Description: Creates the BPP Setting on BPP trend status page
	 * @param equipIndexFactorValue: Takes equipment factor value
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
	 * @param equipIndexFactorValue: Takes equipment factor value
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
	 * @throws: Exception
	 */
	public void enterFactorValue(String factorValue) throws Exception {
		waitForElementToBeClickable(factorTxtBox, 10);
		factorTxtBox.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		//factorTxtBox.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		factorTxtBox.sendKeys(Keys.BACK_SPACE);
		enter(factorTxtBox, factorValue);
	}

	/**
	 * Description: This will fill data in property type field in bpp trend setting
	 * @param propType: Property type like 'Commercial', 'Agricultural' etc.
	 * @throws: Exception
	 */
	public void enterPropertyType(String propType) throws Exception {
		objApasGenericPage.selectOptionFromDropDown(propertyType, propType);
	}

	/**
	 * Description: Retrieves the error message displayed on entering an invalid value for max equipment index factor
	 * @return String: Returns the error message
	 * @throws: Exception
	 */
	public String errorMsgOnIncorrectFactorValue() throws Exception {
		String xpath = "//span[text() = 'Maximum Equipment index Factor']//parent::label//parent::div//following-sibling::ul//li";
		return getElementText(locateElement(xpath, 10));
	}

	/**
	 * Description: Retrieves the count of Bpp Settings currently displayed / available
	 */
	public String getCountOfBppSettings() throws Exception {
		String xpath = "//article[contains(@class, 'slds-card slds-card_boundary')]//span[text() = 'BPP Settings']//following-sibling::span";
		WebElement bppSettingsCount = locateElement(xpath, 30);
		//wait.until(ExpectedConditions.textToBePresentInElement(bppSettingsCount, "("+ expectedCount +")"));
		Thread.sleep(3000);
		return getElementText(bppSettingsCount).substring(1, 2);
	}

	/**
	 * Description: Retrieves the error message displayed on entering an invalid value for min. equipment index factor
	 * @return String: Returns the error message
	 * @throws: Exception
	 */
	public String errorMsgUnderMinEquipFactorIndexField() throws Exception {
		String xpath = "//span[text() = 'Minimum Good Factor']//parent::label//parent::div//following-sibling::ul//li";
		return getElementText(locateElement(xpath, 10));
	}

	/**
	 * Description: Retrieves the value from maximum equipment index factor value from BPP Setting pop up
	 * @throws: Exception
	 */
	public String retrieveMaxEqipIndexValueFromPopUp() throws Exception {
		String xpathMaxEquipIndexFactorValue = "//dt[text()='Maximum Equipment index Factor']//following-sibling::dd";
		return getElementText(waitUntilElementIsPresent(10,xpathMaxEquipIndexFactorValue));
	}

	/**
	 * Description: Retrieves the current status of the given table from details page under given BPP Trend Setup
	 * @param tableName: Takes the names of the table
	 * @return String: Returns the table status
	 * @throws: Exception
	 */
	public String getTableStatusFromBppTrendSetupDetailsPage(String tableName) throws Exception {
		String tableNameForTrendSetupPage;
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
		WebElement compTrendsStats = waitUntilElementIsPresent(20,xpathCompositeTrendsStatus);
		String attributeValue = compTrendsStats.getAttribute("aria-expanded");
		if(attributeValue.equals("false")) {
			Click(compTrendsStats);
		}

		String xpathValuationTrendStatus = "//span[text() = 'Valuation Trend Status']//ancestor::button[contains(@class, 'test-id__section-header-button slds')]";
		WebElement valuationTrendsStats = waitUntilElementIsPresent(20,xpathValuationTrendStatus);
		attributeValue = valuationTrendsStats.getAttribute("aria-expanded");
		if(attributeValue.equals("false")) {
			Click(valuationTrendsStats);
		}

		String xpathProp13FactorStatus = "//span[text() = 'Prop 13 Factor Status']//ancestor::button[contains(@class, 'test-id__section-header-button slds-section')]";
		WebElement prop13FactorStatus = waitUntilElementIsPresent(20,xpathProp13FactorStatus);
		attributeValue = prop13FactorStatus.getAttribute("aria-expanded");
		if(attributeValue.equals("false")) {
			Click(prop13FactorStatus);
		}

		String xpathTableStatus = "//span[text() = '"+ tableNameForTrendSetupPage +"']//parent::div//following-sibling::div//lightning-formatted-text";
		WebElement tableStatus = waitUntilElementIsPresent(20,xpathTableStatus);
		return getElementText(tableStatus);
	}

	/**
	 * Description: Create a new bpp trend setup with no bpp settings and no bpp composite settings and no data uploaded
	 * @return String: Return the name of the bpp trend setup created
	 * @throws: Exception
	 */
	public String createDummyBppTrendSetupForErrorsValidation(String compFactorTablesStatus, int rollYear) throws Exception {
		//Step1: Click New button on the grid to open form / pop up to create new BPP Trend Setup
		WebElement newButton = objPage.locateElement("//div[contains(@class, 'headerRegion forceListViewManagerHeader')]//a[@title = 'New']", 10);
		Click(newButton);

		//Step2: Entering BPP trend setup name and roll year
		//int year = Integer.parseInt(TestBase.CONFIG.getProperty("rollYear")) + 2;
		String trendSetupName = rollYear + " " + TestBase.CONFIG.getProperty("bppTrendSetupNameSuffix");

		this.rollYearForErrorValidationOnCalculate = Integer.toString(rollYear);
		enter(bppTrendSetupName, trendSetupName);
		WebElement rollYearField = locateElement("//span[text() = 'Roll Year']//parent::span//following-sibling::div", 10);
		objApasGenericPage.selectOptionFromDropDown(rollYearField, Integer.toString(rollYear));

		//Step3: Setting the status of composite factor tables to Not Calculated
		objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Commercial Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus);
		objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Const. Mobile Equipment Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus);
		objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Industrial Trend Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus);
		objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Ag. Mobile Equipment Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus);
		objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Const. Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus);
		objApasGenericPage.selectOptionFromDropDown(locateElement("//span[text() = 'Ag. Trends Status']//parent::span//following-sibling::div[@class = 'uiMenu']", 10), compFactorTablesStatus);

		//Step4: Clicking save button
		Click(saveBtnInBppSettingPopUp);
		Thread.sleep(2000);
		return trendSetupName;
	}

	/**
	 * It wait for the pop up message to show up when delete or save button is clicked
	 */
	public String getSuccessMsgText() throws Exception {
		String xpath = "//div[contains(@class, 'toastContent')]//span[contains(@class, 'toastMessage')]";
		WebElement successAlertText = locateElement("//div[contains(@class, 'toastContent')]//span[contains(@class, 'toastMessage')]",15);	
		String alertTxt = successAlertText.getText();
		if(objPage.verifyElementVisible(successAlertCloseBtn)) {
			objPage.Click(successAlertCloseBtn);
		}
		return alertTxt;
	}
	/**
	 * Description: Retrieves the index value of given column from given factor table
	 * @param factorName: Takes name of the factor table
	 * @param columnName: Takes name of the column
	 * @return String: Return the index position of given table column
	 * @throws: Exception
	 */
	private String getIndexPositionOfGivenColumnFromGivenFactorTable(String factorName, String columnName) throws Exception {
		String xpathTableHeader = null;
		if(factorName.equalsIgnoreCase("BPP Property Index Factors")) {
			xpathTableHeader = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text() = 'BPP Property Index Factors']//ancestor::lst-list-view-manager-header//following-sibling::div//table//thead//tr//th";
		} else if(factorName.equalsIgnoreCase("BPP Percent Good Factors")) {
			xpathTableHeader = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text() = 'BPP Percent Good Factors']//ancestor::lst-list-view-manager-header//following-sibling::div//table//thead//tr//th";
		} else if(factorName.equalsIgnoreCase("Imported Valuation Factors")) {
			xpathTableHeader = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text() = 'Imported Valuation Factors']//ancestor::lst-list-view-manager-header//following-sibling::div//table//thead//tr//th";
		}

		int indexPositionOfGivenColumn = -1;
		List <WebElement> tableColumns = locateElements(xpathTableHeader, 30);
		for(int i = 0; i < tableColumns.size(); i++) {
			int indexPos = i+1;
			String colName = getAttributeValue(waitUntilElementIsPresent(10,"("+ xpathTableHeader +")["+ indexPos +"]"),"aria-label");
			if(colName.equalsIgnoreCase(columnName)) {
				indexPositionOfGivenColumn = i;
				break;
			}
		}
		return Integer.toString(indexPositionOfGivenColumn);
	}

	/**
	 * Description:: Generates the row number to be read basis on the parameter provided
	 * @param propertyName: Takes Property Name as an argument like 'Agricultural', 'Commercial'
	 * @param rollYear: Roll Year
	 * @param tableName: Name of the table whose rows are to be edited like 'BPP Property Index Factors'
	 * @return String: Index of the row to be edited
	 * @throws: Exception
	 */
	private String getRowNumberToUpdate(String tableName, String rollYear, String propertyName) throws Exception {
		String expRowHeading = rollYear + "-" + propertyName;
		String xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[text() = '"+tableName+"']//ancestor::lightning-tab-bar//following-sibling::slot//table//tbody//tr//th//a";

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
	 * @return String: Return the property type value
	 * @throws: Exception
	 */
	public String readPropertyTypeValueFromBppPropIndexFactors(String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate("BPP Property Index Factors", TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String indexOfPropertyType = getIndexPositionOfGivenColumnFromGivenFactorTable("BPP Property Index Factors", "Property Type");
		String xpathPropertyType = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text() = 'BPP Property Index Factors']//ancestor::lst-list-view-manager-header//following-sibling::div//table//tbody//tr["+rowNum+"]//td["+ indexOfPropertyType +"]";
		return getElementText(waitUntilElementIsPresent(10,xpathPropertyType));
	}

	/**
	 * Description: Retrieves the year acquired value from given factor table
	 * @return String: Return the year acquired value
	 * @throws: Exception
	 */
	public String readAcquiredYearValueFromGivenFactorTable(String factorTableName, String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate(factorTableName, TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String indexOfYearAcquired = getIndexPositionOfGivenColumnFromGivenFactorTable(factorTableName, "Year Acquired");
		String xpathYear = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text() = '"+ factorTableName +"']//ancestor::lst-list-view-manager-header//following-sibling::div//table//tbody//tr["+rowNum+"]//td["+ indexOfYearAcquired +"]";
		return getElementText(waitUntilElementIsPresent(10,xpathYear));
	}

	/**
	 * Description: Retrieves the Name value from given factor table
	 * @return String: Return the name value
	 * @throws: Exception
	 */
	public String readNameValueFromGivenFactorTable(String factorTableName, String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate(factorTableName, TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String xpathName = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text() = '"+ factorTableName +"']//ancestor::lst-list-view-manager-header//following-sibling::div//table//tbody//tr["+rowNum+"]//th[1]//a";
		return getElementText(waitUntilElementIsPresent(10,xpathName));
	}

	/**
	 * Description: Retrieves the Index Factor value from given factor table
	 * @return String: Return the Index Factor value
	 * @throws: Exception
	 */
	public String readIndexFactorValue(String propertyType) throws Exception {
		String rowNum = getRowNumberToUpdate("BPP Property Index Factors", TestBase.CONFIG.getProperty("rollYear"), propertyType);
		String indexOfIndexFactor = getIndexPositionOfGivenColumnFromGivenFactorTable("BPP Property Index Factors", "Index Factor");
		String xpathIndexFactor = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text() = 'BPP Property Index Factors']//ancestor::lst-list-view-manager-header//following-sibling::div//table//tbody//tr["+rowNum+"]//td["+ indexOfIndexFactor +"]";
		return getElementText(waitUntilElementIsPresent(10,xpathIndexFactor));
	}

	/**
	 * Description: It locates the new button for given factor section on bpp trend setup details page
	 * @param factorsTable: Takes the names of the factor section under which New button needs to be located
	 * @return WebElement: It returns New button element
	 * @throws: Exception
	 */
	public WebElement clickNewButtonUnderFactorSection(String factorsTable) throws Exception {
		String newBtnXpath = "//span[text() = '"+ factorsTable +"']//ancestor::div[contains(@class, 'firstHeaderRow')]//following-sibling::div[@class='actionsWrapper']//a[@title = 'New']";
		WebElement newButton = locateElement(newBtnXpath, 10);
//		try {
//			newButton = locateElement(newBtnXpath, 10);
		clickAction(newButton);
//		} catch(Exception ex) {
//			newButton = locateElement(newBtnXpath, 10);
//			clickAction(newButton);
//		}
		return newButton;
	}

	/**
	 * Description: It creates a new factor entry under given factor section
	 * @param fieldName: Takes the names of the factor field
	 * @param fieldValue: Takes the value of the factor field
	 * @throws: Exception
	 */
	public void enterDataInNewEntryPopUpInGivenField(String fieldName, String fieldValue) throws Exception {
		String xpath;
		WebElement element;
		if(fieldName.equalsIgnoreCase("Year Acquired") || fieldName.equalsIgnoreCase("Property Type") || fieldName.equalsIgnoreCase("Good Factor Type")) {
			String fieldXpath = "//span[text() = '"+ fieldName +"']//parent::span//following-sibling::div[@class = 'uiMenu']";
			WebElement fieldElement = locateElement(fieldXpath, 20);
			objApasGenericPage.selectOptionFromDropDown(fieldElement, fieldValue);
		} else {
			xpath = "//span[contains(text(), '"+ fieldName +"')]//parent::label//following-sibling::input";
			element = locateElement(xpath, 30);
			enter(element, fieldValue);
//			if(fieldName.toUpperCase().contains("NAME (Roll Year")) {
//				System.setProperty("factorEntryName", fieldValue);
//			}
		}
	}

	/**
	 * Description: Clicks on the show more drop down in the table for given entry.
	 * @param factorTableName: It takes name of factor table on BPP Trend Setup details page
	 * @throws: Exception
	 */
	public void clickShowMoreDropDownForGivenFactorEntry(String factorTableName) throws Exception {
		//This condition is added as Factor Table Name and Title are different
		if (factorTableName.equals("Composite Factors")) factorTableName = "BPP " + factorTableName;
		String xpath;
		if(factorTableName.equalsIgnoreCase("BPP Percent Good Factors")) {
			xpath = "(//span[text() = 'Machinery and Equipment'])[1]//parent::td//following-sibling::td//a | (//span[text() = 'Machinery and Equipment'])[1]//parent::td//following-sibling::td//a[@title = 'Show 2 more actions']";
//			xpath = "(//span[text() = 'Machinery and Equipment'])[1]//parent::td//following-sibling::td//span[text() = 'Show More'] | (//span[text() = 'Machinery and Equipment'])[1]//parent::td//following-sibling::td//a[@title = 'Show 2 more actions']";
		} else {
			xpath = "//div[@class = 'windowViewMode-normal oneContent active lafPageHost']//span[text() = '"+factorTableName+"']//ancestor::div[contains(@class, 'slds-grid slds-page-header')]//following::div//table//tbody//tr[1]//a[@role = 'button']";
		}
		Thread.sleep(2000);
		WebElement showMoreDropDown = locateElement(xpath, 30);
		if(showMoreDropDown == null) {
			showMoreDropDown = locateElement(xpath, 30);
		}
		clickAction(showMoreDropDown);
		Thread.sleep(1000);
	}

	/**
	 * Description: Retrieves the value of maximum factor value from the grid
	 * @param bppSettingName: Name of the BPP Setting
	 * @return String: Return the value as String
	 * @throws: Exception
	 */
	public String retrieveFactorValueFromGrid(String bppSettingName) throws Exception {
		String xpathForFactorValueInGrid = "//tbody/tr//th//a[text() = '"+ bppSettingName +"']//parent::span//parent::th//following-sibling::td//span[contains(text(), '%')]";
		return getElementText(locateElement(xpathForFactorValueInGrid, 10));
	}

	/**
	 * Description: This will select the roll year from the drop down
	 * @param rollYear: Roll Year for which the BPP Trend Name needs to be clicked or BPP trend name itself
	 * @throws: Exception
	 */
	public void clickOnEntryNameInGrid(String rollYear) throws Exception {
		String xpath = "//tbody//tr//th//a[contains(text(), '"+ rollYear +"')]";
		Click(waitUntilElementIsPresent(20,xpath));
	}
	
	/**
	 * Description: creates the Max Equip Index Settings
	 * @returns: success message
	 * @throws: Exception
	 */
	public void createBPPSettingEntry(String rollYear, String factorValue) throws Exception{		
			
		//Step1: Click BPP Setting drop down
		clickAction(waitForElementToBeClickable(dropDownIconBppSetting));

		//Step2: Click on New option to create BPP Setting entry
		clickAction(waitForElementToBeClickable(newBtnToCreateEntry));
		enterFactorValue(factorValue);
		Click(objBuildPermitPage.saveButton);
	}
	
	/**
	 * Description: Retrieves the current status of the given table from details page under given BPP Trend Setup
	 * @param tableName: Takes the names of the table
	 * @return String: Returns the table status
	 * @throws: Exception
	 */
	public String getTableStatus(String tableName,String rollYear) throws Exception {
		String tableNameForTrendSetupPage;
		switch(tableName) {
			case"Commercial Composite Factors":
				tableName = "Commercial_Trends_Status__c";
				break;
			case"Industrial Composite Factors":
				tableName = "Industrial_Trend_Status__c";
				break;
			case"Construction Composite Factors":
				tableName = "Const_Trends_Status__c";
				break;
			case"Construction Mobile Equipment Composite Factors":
				tableName = "Const_Mobile_Equipment_Trends_Status__c";
				break;
			case"Agricultural Mobile Equipment Composite Factors":
				tableName = "Ag_Mobile_Equipment_Trends_Status__c";
				break;
			case"Agricultural Composite Factors":
				tableName = "Ag_Trends_Status__c";
				break;
			case"BPP Prop 13 Factors":
				tableName = "Prop_13_Factor_Status_New__c";
				break;
			case"Computer Valuation Factors":
				tableName = "Computer_Trends_Status__c";
				break;
			case"Biopharmaceutical Valuation Factors":
				tableName = "Biopharmaceutical_Trends_Status__c";
				break;
			case"Copier Valuation Factors":
				tableName = "Copier_Trends_Status__c";
				break;
			case"Semiconductor Valuation Factors":
				tableName = "Semiconductor_Trends_Status__c";
				break;
			case"Litho Valuation Factors":
				tableName = "Litho_Trends_Status__c";
				break;
			case"Mechanical Slot Machines Valuation Factors":
				tableName = "Mechanical_Slot_Machine_Trends_Status__c";
				break;
			case"Set-Top Box Valuation Factors":
				tableName = "Set_Top_Box_Trends_Status__c";
				break;
			case"Electrical Slot Machines Valuation Factors":
				tableName = "Electronic_Slot_Machine_Trends_Status__c";
				break;
		}
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		//Query to fetch the status of composite & valuation factor tables
		String queryForID = "Select "+tableName+" From BPP_Trend_Roll_Year__c where Roll_Year__c = '"+ rollYear +"'";	
		HashMap<String, ArrayList<String>> tableStatus = objSalesforceAPI.select(queryForID);
		return tableStatus.get(tableName).get(0);
	}
	
	
	/**
	 * Description: This will Click on Edit buttonand fill data in maximum & minimum factor field in bpp trend setting and save it
	 * @param factorValue: Maximum or minimum factor value like 125%
	 * @throws: Exception
	 */
	public void editSaveFactorValue(String factorValue) throws Exception {
		objPage.waitForElementToBeClickable(dropDownIconDetailsSection, 10);
		objPage.javascriptClick(dropDownIconDetailsSection);
		objPage.waitForElementToBeClickable(editLinkUnderShowMore, 10);
		objPage.javascriptClick(editLinkUnderShowMore);
		enter("Maximum Equipment index Factor",factorValue);
		objPage.Click(objPage.getButtonWithText("Save"));
		Thread.sleep(2000);
	}
	
	
	/**
	 * Description: This will create maximum equipment index factor settings if it does not exist
	 * @param rollYear: roll year for which settings will be created
	 * @throws: Exception
	 */
	public void createMaxEquip(String rollYear) throws Exception {
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		//Query to fetch the settings
		String queryForID = "SELECT Maximum_Equipment_index_Factor__c FROM BPP_Setting__c WHERE BPP_Trend_Roll_Year_Parent__c = '"+ rollYear +"'";	
		HashMap<String, ArrayList<String>> factorSettings = objSalesforceAPI.select(queryForID);
		if(!(factorSettings.size()>0)) {
			objPage.waitForElementToBeClickable(dropDownIconBppSetting, 20);
			objPage.javascriptClick(dropDownIconBppSetting);
			objPage.waitForElementToBeClickable(newBtnToCreateEntry, 20);
			objPage.javascriptClick(newBtnToCreateEntry);
			enter("Maximum Equipment index Factor","125");
			objPage.Click(objPage.getButtonWithText("Save"));
			Thread.sleep(1000);
		}
	}

	/**
	 * Description: This will open the tab for Trend Factor passed in the parameter
	 * @param factorName : Factor tab name to be opened
	 */
	public void openFactorTab(String factorName) throws Exception {
		String xpath = "//a[@data-label='" + factorName + "'][@role='tab']";
		if (verifyElementExists(xpath)){
			Click(driver.findElement(By.xpath(xpath)));
		}else{
			Click(driver.findElement(By.xpath("//ul[@role='tablist']//*[@title='More Tabs']")));
			Click(driver.findElement(By.xpath("//a[@role='menuitem']//span[text()='" + factorName + "']")));
		}
		waitUntilElementIsPresent("//span[contains(@title,'" + factorName + ")']",10);
	}


}