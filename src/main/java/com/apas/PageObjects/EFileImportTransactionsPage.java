package com.apas.PageObjects;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EFileImportTransactionsPage extends Page {

	public EFileImportTransactionsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//a[@data-label='Details' and @role='tab']")
	public WebElement detailsTab;
	
	@FindBy(xpath = "//*[@class='slds-form__row']//*[@class='test-id__field-label' and contains(.,'Status')]/../..//*[@data-output-element-id='output-field']")
	public WebElement statusLabel;
	
	@FindBy(xpath = "//nav[@aria-label = 'Global Navigation']//one-app-nav-bar-item-root//a//span[text() = 'E-File Import Logs']")
	public WebElement efileImportLogLabel;
	
	@FindBy(xpath = "//nav[@aria-label = 'Global Navigation']//one-app-nav-bar-item-root//a//span[text() = 'E-File Import Transactions']")
	public WebElement efileImportTransactionLabel;
	
	@FindBy(xpath = "//a[contains(.,'Import Transaction-00')]")
	public WebElement importTransactionName;
	
	@FindBy(xpath = "(//a[contains(@title, 'BPP Trend Factors')])[1]")
	public WebElement importBppTransactionName;
	
}
