package com.apas.generic;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.EFileHomePage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;

public class SalsesforceStandardFunctions extends TestBase{
	
	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	EFileHomePage eFilePageObject;
	BppTrendPage objBppTrendsPage;
	
	public SalsesforceStandardFunctions(RemoteWebDriver driver){
		this.driver = driver;
		objPage = new Page(this.driver);
		objLoginPage = new LoginPage(this.driver);
		objBppTrendsPage = new BppTrendPage(this.driver);
		eFilePageObject = new EFileHomePage(this.driver);
	}
	
	public void login(String userType) throws Exception{					
		objPage.navigateTo(driver, envURL);
		objLoginPage.enterLoginUserName(CONFIG.getProperty(userType + "UserName"));
		objLoginPage.enterLoginPassword(CONFIG.getProperty(userType + "Password"));
		objLoginPage.clickBtnSubmit();
		System.out.println("User logged in the application");
	}
	
	public void searchApps(String appToSearch) throws Exception {	
		objBppTrendsPage.clickAppLauncher();
		objBppTrendsPage.searchForApp(appToSearch);
		objBppTrendsPage.clickNavOptionFromDropDown(appToSearch);
		Thread.sleep(4000);
	}
	
	public void uploadFileOnEfileIntake(String fileType, String source,String period, String fileName) throws Exception{
		eFilePageObject.selectFileAndSource(fileType, source);
		Thread.sleep(4000);
		objPage.Click(eFilePageObject.nextButton);
		objPage.Click(eFilePageObject.periodDropdown);
		objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")));
		objPage.Click(eFilePageObject.confirmButton);
		objPage.Click(eFilePageObject.uploadFilebutton);
		Thread.sleep(2000);
		uploadFile(fileName);
		Thread.sleep(5000);
		objPage.Click(eFilePageObject.doneButton);	
		Thread.sleep(3000);
	}
	
	public void uploadFile(String absoulteFilePath) throws AWTException, InterruptedException{
		 StringSelection ss = new StringSelection(absoulteFilePath);
		 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		 
		 Robot robot = new Robot();

		 robot.keyPress(KeyEvent.VK_CONTROL);
		 robot.keyPress(KeyEvent.VK_V);
		 robot.keyRelease(KeyEvent.VK_V);
		 robot.keyRelease(KeyEvent.VK_CONTROL);
		 robot.keyPress(KeyEvent.VK_ENTER);
		 robot.keyRelease(KeyEvent.VK_ENTER);
	}
	
	public void logout() throws IOException{	
		objLoginPage.clickImgUser();
		objLoginPage.clickLnkLogOut();
		System.out.println("User logged out of the application");
	}

}
