package com.apas.PageObjects;

import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.File;
import java.io.IOException;

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

	/**
	 * Description: This method will export the report in the way passed in the parameter reportType
	 * @param reportType: Type of Report
	 * @param reportName: Name of the Report
	 */
	public void exportReport(String reportName, String reportType) throws IOException, InterruptedException {
		// Opening all reports screen
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening All Reports Screen");
		Click(linkAllReports);
		//Opening the report "Building Permit By City Code"
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening the report " + reportName);
		//Added this wait as report was taking sometime to open
		Thread.sleep(3000);

		//Using the JavaScriptExecutor as CLICK method is not working
		WebElement webElement =  driver.findElement(By.xpath("//a[@title='" + reportName + "']"));
		JavascriptExecutor executor = driver;
		executor.executeScript("arguments[0].click();", webElement);

		//Exporting the report in desired format
		Click(arrowButton);
		Click(linkExport);
		if (reportType.equals(FORMATTED_EXPORT))
			Click(formattedExportLabel);
		else
			Click(dataExportLabel);
		Click(exportButton);
		//Added this wait to allow the file to download
		Thread.sleep(5000);
	}
}
