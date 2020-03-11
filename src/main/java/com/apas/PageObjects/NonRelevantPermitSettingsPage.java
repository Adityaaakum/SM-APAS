package com.apas.PageObjects;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NonRelevantPermitSettingsPage extends Page {

	public NonRelevantPermitSettingsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//a[@title = 'New']")
	public WebElement newButton;

	@FindBy(xpath = "//button[@title = 'Cancel']")
	public WebElement cancelButton;

	@FindBy(xpath = "//span[text() = 'City Code']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement cityCodeDrpDown;

	@FindBy(xpath = "//span[text() = 'Status']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement statusDrpDown;

	@FindBy(xpath = "//*[text()='This record looks like a duplicate.']")
	public WebElement messageDuplicateData;

	@FindBy(xpath = "//a[text()='View Duplicates']")
	public WebElement linkViewDuplicates;
}
