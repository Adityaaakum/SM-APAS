package com.apas.PageObjects;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;

public class BppManagementPage extends ApasGenericPage {
	Logger logger = Logger.getLogger(LoginPage.class);
	ApasGenericPage objApasGenericPage;
	Util objUtil;
	Page objPage;
	SalesforceAPI objSFAPI;

	public BppManagementPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericPage = new ApasGenericPage(driver);
		objPage = new Page(driver);
		objUtil = new Util();
		objSFAPI = new SalesforceAPI();
	}

	//Locators for BPP Annual Settings screen
	public String rollYearSettingsLabel = "Roll Year Settings";
	public String rollYearLabel = "Roll Year";
	public String statusLabel = "Status";
	public String improvementPILabel = "PI Improvement %";
	public String landPILabel = "PI Land %";
	public String salesTaxLabel = "Sales Tax %";
	public String viewFileNetDocumentsButton = "View FileNet Documents";
	public String rentFeeLabel = "Rent Fee";
	public String rentFactorLabel = "Rent Factor";
	public String svcRateLabel = "SVC Rate";
	public String svcDescriptionLabel = "SVC Description";
	public String typeOfSpaceLabel = "Type Of Space";
	public String spaceNameLabel = "Space Name";
	public String airPortLabel = "Airport";	
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'slds-listbox__option_plain')]//a[@title = 'New'] | //flexipage-tab2[contains(@class,'show')]//*[text()='New']")
	public WebElement newButton;
	
	
	//Locators for BPP Annual Settings WI screen
	public String relatedActionLabel = "Related Action";
	public String workPoolLabel = "Work Pool";
	public String actionLabel = "Action";
	
	
	//Other Locators
	@FindBy(xpath = "//a[text()='View Duplicates']")
	public WebElement viewDuplicateLink;
	
	@FindBy(xpath = "//div[@class='bBody']//p")
	public WebElement viewDuplicateScreenMessageArea;
	
	@FindBy(xpath = "//div[contains(.,'View Duplicates')]/button[@title='Close this window']")
	public WebElement closeViewDuplicatesPopUpButton;
	
	/**
	 * This Method is used to create PI record
	 * @param dataToCreatePISPACERecord is a Map
	 * @throws Exception
	 */
	
	public void createPISpaceRecord(Map<String, String>dataToCreatePISPACERecord ) throws Exception
	 {
		    searchAndSelectOptionFromDropDown(rollYearLabel, dataToCreatePISPACERecord.get(rollYearLabel));
			Thread.sleep(1000);
			enter(rentFeeLabel, dataToCreatePISPACERecord.get(rentFeeLabel));
			enter(rentFactorLabel, dataToCreatePISPACERecord.get(rentFactorLabel));
			enter(svcRateLabel,dataToCreatePISPACERecord.get(svcRateLabel));
			enter(svcDescriptionLabel, dataToCreatePISPACERecord.get(svcDescriptionLabel));
			selectOptionFromDropDown(airPortLabel, dataToCreatePISPACERecord.get(airPortLabel));
			selectOptionFromDropDown(typeOfSpaceLabel,dataToCreatePISPACERecord.get(typeOfSpaceLabel));
			selectOptionFromDropDown(spaceNameLabel,dataToCreatePISPACERecord.get(spaceNameLabel));
			Click(objPage.getButtonWithText("Save"));
			ReportLogger.INFO("Created PI Space settings record");
			Thread.sleep(1000);

		}
	
}