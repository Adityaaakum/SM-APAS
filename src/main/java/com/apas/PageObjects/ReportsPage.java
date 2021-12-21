package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.DateUtil;
import com.apas.Utils.Util;
import com.apas.config.testdata;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ReportsPage extends ApasGenericPage {

	public static final String FORMATTED_EXPORT = "formatted-export";
	public static final String DATA_EXPORT = "data-export";

	public ReportsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//a[@title='All Reports']")
	public WebElement linkAllReports;

	@FindBy(xpath = "//div[@role='group']/button[text()='Edit']/../div/button")
	public WebElement arrowButton;

	@FindBy(xpath = "//a[@role='menuitem']/span[@title='Export']")
	public WebElement linkExport;

	@FindBy(xpath = "//div[@data-aura-class='reportsExportVisualPickerOption']/label[@for='formatted-export']")
	public WebElement formattedExportLabel;

	@FindBy(xpath = "//div[@data-aura-class='reportsExportVisualPickerOption']/label[@for='data-export']")
	public WebElement dataExportLabel;

	@FindBy(xpath = "//button[@title = 'Export']")
	public WebElement exportButton;
	
	@FindBy(xpath = "//table[contains(@class,'full')]//tr[1]//th[contains(@class,'action') and contains(@id, 'data-grid-7-fixedrow0-col')]//div//span[@class='lightning-table-cell-measure-header-value']")
	public List<WebElement> colNames;
	
	@FindBy(xpath = "//table[contains(@class,'full')]//span[text()='Exemption: Exemption Name']")
	public WebElement exemptionNameLabel;
	
	@FindBy(xpath="//button[@class='action-bar-action-toggleFilter reportAction report-action-toggleFilter slds-button slds-not-selected slds-button_icon-border']")
	public WebElement filterIcon;
	
	@FindBy(xpath="//div[@class='filter-button-group']//button[contains(text(),'Apply')]")
	public WebElement filterApply;
	
	@FindBy(xpath="//*[text()='Report: Custom Building Permit Report']//following::span[text()='Final Review Building Permits']")
	public WebElement buildingPermitHeaderText;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//input[@placeholder='Search all reports...']")
	public WebElement searchListEditBox;

	@FindBy(xpath = "//label[text()='Start Date']//parent::div//following-sibling::div/input")
	public WebElement startDateEditBox;

	@FindBy(xpath = "//label[text()='End Date']//parent::div//following-sibling::div/input")
	public WebElement endDateEditBox;

	@FindBy(xpath = "//span[text()='Created Date']")
	public WebElement CreatedDatelabel;

	@FindBy(xpath = "//a[text()='Customize']")
	public WebElement customizeLink;
	

	public String linkBuildingPermitNumber = "//table[contains(@class,'data-grid-full-table')]//tbody//tr//th[@data-row-index='2']/../td[@data-column-index='2']//a[contains(@href,'')]";
	public String linkAPN = "//table[contains(@class,'data-grid-full-table')]//tbody//tr//th[@data-row-index='3']/../td[@data-column-index='3']//a[contains(@href,'')]";
	public String linkAPNDV = "//table[contains(@class,'full')]//span[contains(text(),'APN')]//ancestor::tr//following-sibling::tr[1]//td[1]//a";
	public String linkExemptionName = "//table[contains(@class,'full')]//span[contains(text(),'Exemption: Exemption Name')]//ancestor::tr//following-sibling::tr[1]//td[1]//a";
	public String linkClaimantsName = "//table[contains(@class,'full')]//span[contains(text(),'Claimant')]//ancestor::tr//following-sibling::tr[1]//td[1]//a";
	public String linkRollYearSettings = "//table[contains(@class,'full')]//span[contains(text(),'Roll Year Settings')]//ancestor::tr//following-sibling::tr[1]//td[1]//a";
	
	
	/**
	 * Description: This method will export the report in the way passed in the parameter reportType
	 * @param reportType: Type of Report, Refer to class variables, it can be either FORMATTED_EXPORT or DATA_EXPORT
	 * @param reportName: Name of the Report
	 */
	public void exportReport(String reportName, String reportType) throws Exception {
		int initialFileCount = Objects.requireNonNull(new File(testdata.DOWNLOAD_FOLDER).listFiles()).length;
		// Opening all reports screen
		ReportLogger.INFO("Opening All Reports Screen");
		Click(linkAllReports);
		Thread.sleep(3000);

		//Opening the report passed in the parameter report name
		//Using the JavaScriptExecutor as CLICK method is not working
		ReportLogger.INFO("Opening the report " + reportName);
		JavascriptExecutor executor = driver;
		enter(searchListEditBox, reportName);
		WebElement webElement =  driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container')]//a[contains(@title,'" + reportName + "')]"));
		executor.executeScript("arguments[0].click();", webElement);
		Thread.sleep(2000);
		if (reportName.equals("RP Activity List")) {
			this.validateFilter();
		}
		driver.manage().window().maximize();
	    int size = driver.findElements(By.tagName("iframe")).size();
		
         //Switching the frame as the generated report is in different frame
		driver.switchTo().frame(size-1);
		Click(arrowButton);
		Click(linkExport);

		//Switching back to parent frame to export the report
		driver.switchTo().parentFrame();

		if (reportType.equals(FORMATTED_EXPORT))
			Click(formattedExportLabel);
		else
			Click(dataExportLabel);

		Click(exportButton);

		System.out.println("Start Time : " + DateUtil.getCurrentDate("ddhhmmss"));
		//Added this wait to allow the file to download
		for (int i=0; i<300/2;i++){
			if (Objects.requireNonNull(new File(testdata.DOWNLOAD_FOLDER).listFiles()).length > initialFileCount){
				break;
			}else{
				Thread.sleep(2000);
			}
		}
		System.out.println("End Time : " + DateUtil.getCurrentDate("ddhhmmss"));
		Thread.sleep(5000);
	}
	
	/**
	 * Description: This method will navigate to the report Name passed as parameter
	 * @param reportName: Name of the Report
	 * @return 
	 * @throws Exception 
	 */
	public String navigateToReport(String reportName) throws Exception {
		// Opening all reports screen
		ReportLogger.INFO("Opening All Reports Screen");
		Click(linkAllReports);
		Thread.sleep(3000);
	
		//Opening the report passed in the parameter report name
		//Using the JavaScriptExecutor as CLICK method is not working
		ReportLogger.INFO("Opening the report " + reportName);
		JavascriptExecutor executor = driver;
		enter(searchListEditBox, reportName);
		WebElement webElement =  driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container')]//a[contains(@title,'" + reportName + "')]"));
		
		String getReportName=getElementText(webElement);
        executor.executeScript("arguments[0].click();", webElement);
		Thread.sleep(3000);
		return getReportName;
		
	}
	
	/**
	 * Description: This method will sort the column of report in descending order
	 * @param colName: Column name for which report to be sorted in descending order
	 */
	public void sortReportColumn(String colName) throws Exception {
		String xPathsortArrowBtn = "//table[contains(@class,'full')]//span[text()='"+colName+"']//../following-sibling::span//button";
		String xpathSortDescendingBtn ="//span[text()='Sort Descending']";
		WebElement showMoreBtn = locateElement(xPathsortArrowBtn,30);
		javascriptClick(showMoreBtn);
		WebElement sortDescendingBtn = locateElement(xpathSortDescendingBtn,30);
		javascriptClick(sortDescendingBtn);
		waitUntilPageisReady(driver);
		Thread.sleep(5000);		
		locateElement(xPathsortArrowBtn,30);
	}
	
	/**
	 * Description: This method will update the filters with current roll year values
	 * @param filterNumber: Takes the position of the filter in the default view
	 * @param removeEntry: Takes current filter value to locate that element
	 * @param updateRollYear: Takes year value to be updated
	 */
	public void editFilterAndUpdate(String filterNumber, String removeEntry, String updateRollYear) throws Exception {
		String xpathStr1 = "//ul//li//div[@class='filter-card-index'][contains(text(),'" + filterNumber + "')]/parent::div//div//div//button";
		WebElement filterLocator = locateElement(xpathStr1, 30);
	    Click(filterLocator);
	    String xpathStr2 = "//div[@class='slds-form-element__control slds-input-has-icon slds-input-has-icon_right']//input[@class='slds-input'][@value='" + removeEntry + "']";
	    WebElement dateLocator = locateElement(xpathStr2, 30);
	    if (removeEntry.length() == 9) enter(dateLocator, removeEntry.substring(0, 5).concat(updateRollYear));
	    if (removeEntry.length() == 8) enter(dateLocator, removeEntry.substring(0, 4).concat(updateRollYear));
	    Click(filterApply);
	    Thread.sleep(5000);	
	}

	/**
	 * Description: This method will update the filters with start data and end date

	 */

	public void validateFilter() throws InterruptedException, IOException {

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date date = new Date();
		Calendar calDate = Calendar.getInstance();
		calDate.setTime(date);
		calDate.add(Calendar.DATE, -1);
		date = calDate.getTime();
		String endDate = dateFormat.format(date);
		ReportLogger.INFO("Previous day date is " + endDate);
		Thread.sleep(10000);
		driver.switchTo().frame(0);
		Click(filterIcon);
		Click(CreatedDatelabel);
		Click(customizeLink);
		startDateEditBox.sendKeys(endDate);
		endDateEditBox.sendKeys(endDate);
		Click(filterApply);
		driver.switchTo().defaultContent();

	}
}
