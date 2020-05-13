package com.apas.PageObjects;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

public class EFileImportLogsPage extends Page {

	public EFileImportLogsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//a[@data-label='Transactions' and @role='tab']")
	public WebElement transactionsTab;

	/**
	 * Description: This method will open the Import Logs with the Name passed in the parameter
	 * @param name: Name of the Import Log
	 */
	public void openImportLog(String name) throws IOException, InterruptedException {
		ReportLogger.INFO("Opening the Import Logs with the name : " + name);
		Click(driver.findElement(By.xpath("//a[@title='" + name + "']")));
		Thread.sleep(5000);
	}

	/**
	 * Description: This method will open the Import Logs with the Name passed in the parameter
	 * @param name: Name of the Import Log
	 */
	public void openTransactionLog(String name) throws IOException, InterruptedException {
		ReportLogger.INFO("Opening the Transaction Logs with the name : " + name);
		Click(driver.findElement(By.xpath("//a[text()='" + name + "']")));
		Thread.sleep(7000);
	}

}
