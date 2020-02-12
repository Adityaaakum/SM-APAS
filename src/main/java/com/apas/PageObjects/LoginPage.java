package com.apas.PageObjects;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class LoginPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);

	public LoginPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//input[@name='username']")
	private WebElement txtuserName;

	@FindBy(xpath = "//input[@name='pw']")
	private WebElement txtpassWord;

	@FindBy(xpath = "//input[@name='Login']")
	private WebElement btnSubmit;

	@FindBy(xpath = "//h2[text() = 'Verify Your Identity']")
	private WebElement verificationCode;
	
	@FindBy(xpath = "//div/span[@class='uiImage']")
	private WebElement imgUser;

	@FindBy(xpath = "//div/a[contains(text(),'Log Out')]")
	private WebElement lnkLogOut;

	public void loginIntoApp(String Username, String Password) throws Exception {
		enter(txtuserName, Username);
		enter(txtpassWord, Password);
		Click(btnSubmit);
	}

	public void enterLoginUserName(String UserName) throws Exception {
		enter(txtuserName, UserName);
	}

	public void enterLoginPassword(String Password) throws Exception {
		enter(txtpassWord, Password);
	}

	public void clickBtnSubmit() throws IOException {
		Click(btnSubmit);
	}

	public void clickImgUser() throws IOException {
		try {
			Thread.sleep(10000);
			Click(imgUser);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void clickLnkLogOut() throws IOException {
		try {
			Thread.sleep(10000);
			Click(lnkLogOut);
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void loginToSandbox() throws Exception {
		driver.get("https://smcacre--qa.my.salesforce.com/?login=true");
		enterLoginUserName("yogender.singh@smcacre.org.qa");
		enterLoginPassword("HR26dz@1045");
		clickBtnSubmit();
		driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
		Thread.sleep(15000);
		System.out.println("Login function over");
	}
}