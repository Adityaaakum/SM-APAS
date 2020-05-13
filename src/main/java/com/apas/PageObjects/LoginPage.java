package com.apas.PageObjects;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);

	public LoginPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//input[@name='username']")
	public WebElement txtuserName;

	@FindBy(xpath = "//input[@name='pw']")
	public WebElement txtpassWord;

	@FindBy(xpath = "//input[@name='Login']")
	public WebElement btnSubmit;

	@FindBy(xpath = "//h2[text() = 'Verify Your Identity']")
	public WebElement verificationCode;
	
	@FindBy(xpath = "//button//div/span[@class='uiImage']")
	public WebElement imgUser;

	@FindBy(xpath = "//div/a[contains(text(),'Log Out')]")
	public WebElement lnkLogOut;

}