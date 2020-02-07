package com.apas.PageObjects;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EFileImportTransactions extends Page {

	public EFileImportTransactions(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//a[@data-label='Details' and @role='tab']")
	public WebElement detailsTab;
	
	@FindBy(xpath = "//*[@class='slds-form__row']//*[@class='test-id__field-label' and contains(.,'Status')]/../..//*[@data-output-element-id='output-field']]")
	public WebElement statusLabel;
	
}
