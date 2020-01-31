package com.apas.Tests;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;

public class LoginTest extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLogin;
	
	@Test(description="Verify the Login page" , groups= {"Sanity"})
	public  void verifySalesForceLogin() throws Exception{		
		boolean flag=true;
		driver = BrowserDriver.getBrowserInstance();
		SoftAssert s_assert = new SoftAssert();
		objPage = new Page(driver);
		objLogin = new LoginPage(driver);
		
		
		objPage.navigateTo(driver, "https://login.salesforce.com/");		
		objLogin.enterLoginUserName("akaila@sapient.com");
		objLogin.enterLoginPassword("Welcome@123");
		objLogin.clickBtnSubmit();
		objLogin.clickImgUser();
	    objLogin.clickLnkLogOut();
		
		s_assert.assertTrue(flag, "login page is getting displayed");
		s_assert.assertAll();
	}
	
}
