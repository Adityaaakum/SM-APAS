package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

public class EFileImportTransactionsPage extends Page {

	public EFileImportTransactionsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//a[@data-label='Details' and @role='tab']")
	public WebElement detailsTab;

	@FindBy(xpath = "//a[@data-label='Transaction Trails' and @role='tab']")
	public WebElement transactionTrailTab;

	@FindBy(xpath = "//*[@class='slds-form__row']//*[@class='test-id__field-label' and contains(.,'Status')]/../..//*[@data-output-element-id='output-field']")
	public WebElement statusLabel;
	
	@FindBy(xpath = "//*[@class='slds-form__row']//*[@class='test-id__field-label' and contains(.,'E-File Import Log')]/../..//*[@data-output-element-id='output-field']")
	public WebElement efileImportLogLabel;
	
	@FindBy(xpath = "//a[contains(.,'Import Transaction-00')]")
	public WebElement importTransactionName;

	/**
	 * Description: This method will open the Import Transactions with the Name passed in the parameter
	 * @param name: Name of the Import Log
	 */
	public void openImportTransactions(String name) throws IOException, InterruptedException {
		ReportLogger.INFO("Opening the Import Transactions with the name : " + name);
		Click(driver.findElement(By.xpath("//a[@title='" + name + "']")));
		Thread.sleep(5000);
	}


}
