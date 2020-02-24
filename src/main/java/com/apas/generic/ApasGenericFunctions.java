package com.apas.generic;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.EFileHomePage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.PasswordUtils;
import com.relevantcodes.extentreports.LogStatus;

public class ApasGenericFunctions extends TestBase{
	
	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	EFileHomePage eFilePageObject;
	ApasGenericPage objApasGenericPage;
	
	public ApasGenericFunctions(RemoteWebDriver driver){
		this.driver = driver;
		objPage = new Page(this.driver);
		objLoginPage = new LoginPage(this.driver);
		objApasGenericPage = new ApasGenericPage(this.driver);
		eFilePageObject = new EFileHomePage(this.driver);
	}
	
	public void login(String userType) throws Exception{
		String password = CONFIG.getProperty(userType + "Password");
		if (CONFIG.getProperty("passwordEncryptionFlag").equals("true")){
			System.out.println("Decrypting the password : " + password);
			password = PasswordUtils.decrypt(password, "");
		}
				
		ExtentTestManager.getTest().log(LogStatus.INFO, userType + " User is logging in the application");
		
		objPage.navigateTo(driver, envURL);
		objLoginPage.enterLoginUserName(CONFIG.getProperty(userType + "UserName"));
		objLoginPage.enterLoginPassword(password);
		objLoginPage.clickBtnSubmit();
		System.out.println("User logged in the application");
	}
	
	public void searchApps(String appToSearch) throws Exception {	
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening " + appToSearch + " tab");
		objApasGenericPage.clickAppLauncher();
		objApasGenericPage.searchForApp(appToSearch);
		objApasGenericPage.clickNavOptionFromDropDown(appToSearch);
		Thread.sleep(4000);
	}
	
	public void uploadFileOnEfileIntake(String fileType, String source,String period, String fileName) throws Exception{
		fileName = "\"" + fileName + "\"";
		eFilePageObject.selectFileAndSource(fileType, source);
		Thread.sleep(4000);
		objPage.Click(eFilePageObject.nextButton);
		objPage.Click(eFilePageObject.periodDropdown);
		objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")));
		objPage.Click(eFilePageObject.confirmButton);
		objPage.Click(eFilePageObject.uploadFilebutton);
		Thread.sleep(2000);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading " + fileName + " on Efile Import Tool");
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
		ExtentTestManager.getTest().log(LogStatus.INFO, "User is getting logged out of the application");
		objLoginPage.clickImgUser();
		objLoginPage.clickLnkLogOut();
	}

	public void editGridCellValue(String columnNameOnGrid, String expectedValue) throws IOException, AWTException, InterruptedException{
		WebElement webelement = driver.findElement(By.xpath("//*[@data-label='" + columnNameOnGrid + "'][@role='gridcell']//button"));
		objPage.scrollToElement(webelement);
		objPage.Click(webelement);
		
		WebElement webelementInput = driver.findElement(By.xpath("//input[@class='slds-input']"));
		webelementInput.clear();
		webelementInput.sendKeys(expectedValue);
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_ENTER);
    	robot.keyRelease(KeyEvent.VK_ENTER);
    	Thread.sleep(1000);
	}
	
}
