package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.io.IOException;
import java.util.List;

public class ReportsPage extends Page {

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

	public String linkBuildingPermitNumber = "//table[contains(@class,'data-grid-full-table')]//tbody//tr//th[@data-row-index='3']/../td[@data-column-index='2']//a[contains(@href,'')]";
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
	public void exportReport(String reportName, String reportType) throws IOException, InterruptedException {
		// Opening all reports screen
		ReportLogger.INFO("Opening All Reports Screen");
		Click(linkAllReports);
		Thread.sleep(3000);

		//Opening the report passed in the parameter report name
		//Using the JavaScriptExecutor as CLICK method is not working
		ReportLogger.INFO("Opening the report " + reportName);
		JavascriptExecutor executor = driver;
		WebElement webElement =  driver.findElement(By.xpath("//a[@title='" + reportName + "']"));
		executor.executeScript("arguments[0].click();", webElement);
		Thread.sleep(30000);

		//Switching the frame as the generated report is in different frame
		driver.switchTo().frame(0);

		Click(arrowButton);
		Click(linkExport);

		//Switching back to parent frame to export the report
		driver.switchTo().parentFrame();

		if (reportType.equals(FORMATTED_EXPORT))
			Click(formattedExportLabel);
		else
			Click(dataExportLabel);

		Click(exportButton);

		//Added this wait to allow the file to download
		Thread.sleep(10000);
	}
	
	/**
	 * Description: This method will navigate to the report Name passed as parameter
	 * @param reportName: Name of the Report
	 */
	public void navigateToReport(String reportName) throws IOException, InterruptedException {
		// Opening all reports screen
		ReportLogger.INFO("Opening All Reports Screen");
		Click(linkAllReports);
		Thread.sleep(3000);
	
		//Opening the report passed in the parameter report name
		//Using the JavaScriptExecutor as CLICK method is not working
		ReportLogger.INFO("Opening the report " + reportName);
		JavascriptExecutor executor = driver;
		WebElement webElement =  driver.findElement(By.xpath("//a[@title='" + reportName + "']"));
		executor.executeScript("arguments[0].click();", webElement);
		Thread.sleep(30000);
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
}
