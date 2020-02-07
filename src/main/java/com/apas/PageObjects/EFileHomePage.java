package com.apas.PageObjects;

import java.io.IOException;

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

	@FindBy(xpath = "//*[@name='docType']")
	WebElement fileTypedropdown;

	@FindBy(xpath = "//*[@name='source']")
	WebElement sourceDropdown;

	@FindBy(xpath = "//button[@title='Next']")
	public WebElement nextButton;

	@FindBy(xpath = "//*[@name='freq']")
	public WebElement periodDropdown;

	@FindBy(xpath = "//button[contains(.,'Confirm')]")
	public WebElement confirmButton;

	@FindBy(xpath = "//*[@data-key='upload']")
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

	public void selectFileAndSource(String filetype, String source) throws InterruptedException, IOException {
		System.out.println("File type is:" + filetype + " and Source is:" + source);
		Click(fileTypedropdown);
		Thread.sleep(2000);
		Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + filetype + "')]")));
		Thread.sleep(2000);
		Click(sourceDropdown);
		Thread.sleep(2000);
		Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + source + "')]")));
	}
}
