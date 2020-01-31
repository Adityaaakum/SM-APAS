package com.apas.PageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EFileHomePage extends Page {

	public EFileHomePage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(id = "input-53")
	WebElement fileTypedropdown;

	@FindBy(id = "input-57")
	WebElement sourceDropdown;

	@FindBy(xpath = "//button[@title='Next']")
	public WebElement nextButton;

	@FindBy(id = "input-411")
	public WebElement periodDropdown;

	@FindBy(xpath = "//button[contains(.,'Confirm')]")
	public WebElement confirmButton;

	@FindBy(xpath = "//input[@id='input-file-425']")
	public WebElement uploadFilebutton;

	@FindBy(xpath = "//button//span[contains(.,'Done')]")
	public WebElement doneButton;

	@FindBy(xpath = "//tr[1]/td[5]//div")
	public WebElement status;

	@FindBy(xpath = "//button[@class='slds-button']")
	WebElement appLauncher;

	@FindBy(xpath = "//input[@class='slds-input input']")
	WebElement searchAppsbox;

	public void searchApps(String appToSearch) throws InterruptedException {
		appLauncher.click();
		searchAppsbox.sendKeys(appToSearch);
		Thread.sleep(3000);
		driver.findElement(By.xpath("//a[@title='" + appToSearch + "']//span/mark[contains(.,'" + appToSearch + "')]"))
				.click();
		// driver.findElement(By.xpath("//a[@title='"+appToSearch+"']/span[contains(.,'"+appToSearch+"')]")).click();
	}

	public void selectFileAndSource(String filetype, String source) throws InterruptedException {
		System.out.println("File type is:" + filetype + " and Source is:" + source);
		fileTypedropdown.click();
		driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + filetype + "')]")).click();
		Thread.sleep(2000);
		sourceDropdown.click();
		driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + source + "')]")).click();

		/*
		 * nextButton.click();
		 * 
		 * periodDropdown.click(); driver.findElement(By.xpath(
		 * "//span[@class='slds-media__body']/span[contains(.,'"+period+"')]")).
		 * click();
		 * 
		 * confirmButton.click();
		 * 
		 * uploadFilebutton.click(); //code for file upload
		 * 
		 * doneButton.click();
		 */
	}
}
