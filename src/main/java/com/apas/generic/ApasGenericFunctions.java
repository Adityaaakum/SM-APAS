package com.apas.generic;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.EFileImportPage;
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
	EFileImportPage eFilePageObject;
	ApasGenericPage objApasGenericPage;
	
	public ApasGenericFunctions(RemoteWebDriver driver){
		this.driver = driver;
		objPage = new Page(this.driver);
		objLoginPage = new LoginPage(this.driver);
		objApasGenericPage = new ApasGenericPage(this.driver);
		eFilePageObject = new EFileImportPage(this.driver);
	}
	
	/**
	 * Description: This method will login to the APAS application with the user type passed as parameter
	 * @param userType : Type of the user e.g. business admin / appraisal support
	 */
	public void login(String userType) throws Exception{
		String password = CONFIG.getProperty(userType + "Password");
		
		//Decrypting the password if the encrypted password is saved in envconfig file and passwordEncryptionFlag flag is set to true
		if (CONFIG.getProperty("passwordEncryptionFlag").equals("true")){
			System.out.println("Decrypting the password : " + password);
			password = PasswordUtils.decrypt(password, "");
		}
				
		ExtentTestManager.getTest().log(LogStatus.INFO, userType + " User is logging in the application");
		
		objPage.navigateTo(driver, envURL);
		objPage.enter(objLoginPage.txtuserName, CONFIG.getProperty(userType + "UserName"));
		objPage.enter(objLoginPage.txtpassWord,password);
		objPage.Click(objLoginPage.btnSubmit);
		System.out.println("User logged in the application");
	}
	
	/**
	 * Description: This method will search the module in APAS based on the parameter passed
	 * @param moduleToSearch : Module Name to search and open
	 */
	public void searchModule(String moduleToSearch) throws Exception {	
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening " + moduleToSearch + " tab");
		objPage.Click(objApasGenericPage.appLauncher);
		objPage.enter(objApasGenericPage.appLauncherSearchBox, moduleToSearch);
		objApasGenericPage.clickNavOptionFromDropDown(moduleToSearch);
		//This static wait statement is added as the module title is different from the module to search 
		Thread.sleep(4000);
	}
	
	/**
	 * Description: This method will upload the file on Efile Import module
	 * @param fileType : Value from File Type Drop Down
	 * @param source: Value from source drop down
	 * @param period: Period for which the file needs to be uploaded
	 * @param fileName: Absoulte Path of the file with the file name
	 */
	public void uploadFileOnEfileIntake(String fileType, String source,String period, String fileName) throws Exception{
		fileName = "\"" + fileName + "\"";
		eFilePageObject.selectFileAndSource(fileType, source);
		objPage.waitUntilElementDisplayed(eFilePageObject.nextButton, 10);
		objPage.Click(eFilePageObject.nextButton);
		objPage.Click(eFilePageObject.periodDropdown);
		objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")));
		objPage.Click(eFilePageObject.confirmButton);
		objPage.Click(eFilePageObject.uploadFilebutton);
		//This static wait of 2 second is kept for File Upload window to open
		Thread.sleep(2000);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading " + fileName + " on Efile Import Tool");
		uploadFile(fileName);
		objPage.waitForElementToBeClickable(eFilePageObject.doneButton);
		objPage.Click(eFilePageObject.doneButton);
		Thread.sleep(3000);
	}
	
	/**
	 * Description: This method will upload the file using AutoIt tool
	 * @param absoulteFilePath : Absolute path of the file to be uploaded
	 */
	public void uploadFile(String absoulteFilePath) throws AWTException, InterruptedException, IOException{
		Runtime.getRuntime().exec(System.getProperty("user.dir") + "//src//test//resources//AutoIt//FileUpload.exe"+" " + absoulteFilePath);
		
		/**
		 * Below Code is to upload the file using Robot. Using AutoIt as this was not working on Jenkins
		 StringSelection ss = new StringSelection(absoulteFilePath);
		 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
		 
		 Robot robot = new Robot();

		 robot.keyPress(KeyEvent.VK_CONTROL);
		 robot.keyPress(KeyEvent.VK_V);
		 robot.keyRelease(KeyEvent.VK_V);
		 robot.keyRelease(KeyEvent.VK_CONTROL);
		 robot.keyPress(KeyEvent.VK_ENTER);
		 robot.keyRelease(KeyEvent.VK_ENTER);
		 */
	}
	
	/**
	 * Description: This method will logout the logged in user from APAS application
	 */
	public void logout() throws IOException, InterruptedException{	
		ExtentTestManager.getTest().log(LogStatus.INFO, "User is getting logged out of the application");
		objPage.Click(objLoginPage.imgUser);
		objPage.Click(objLoginPage.lnkLogOut);
		objPage.waitForElementToBeVisible(objLoginPage.txtpassWord,30);
	}

	
	/**
	 * Description: This method will Edit a cell on a grid displayed from the first row
	 * @param columnNameOnGrid: Column name on which the cell needs to be updated
	 * @param expectedValue: Modified value to be updated in the cell
	 */
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
