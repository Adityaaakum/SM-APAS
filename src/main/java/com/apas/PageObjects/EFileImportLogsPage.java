package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;
import com.apas.generic.ApasGenericFunctions;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

public class EFileImportLogsPage extends Page {
	Page objPage;
	ApasGenericFunctions apasGenericObj;
	EFileImportPage objEFileImport;
	

	public EFileImportLogsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		apasGenericObj=new ApasGenericFunctions(driver);
		objEFileImport=new EFileImportPage(driver);
		objPage=new Page(driver);
	
	}

	@FindBy(xpath = "//a[@data-label='Transactions' and @role='tab']")
	public WebElement transactionsTab;

	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//table")
	public WebElement importLogRecordsTable;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(text(),'File Count')]/parent::div/following-sibling::div//lightning-formatted-number")
	public WebElement logFileCount;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(text(),'Import Count')]/parent::div/following-sibling::div//lightning-formatted-number")
	public WebElement logImportCount;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(text(),'Error Count')]/parent::div/following-sibling::div//lightning-formatted-number")
	public WebElement logErrorCount;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(text(),'Status')]/parent::div/following-sibling::div//lightning-formatted-text")
	public WebElement logStatus;
	
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[text()='View All']")
	public WebElement viewAlllink;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and text()='Duplicates in File']/parent::div/following-sibling::div//slot[@slot='outputField']")	
	public WebElement duplicatesInFileImportLog;	
		
	@FindBy(xpath = "//button[contains(@title,'Edit')]")	
	public WebElement inlineEditButton;
	
	
	
	/**
	 * Description: This method will open the Import Logs with the Name passed in the parameter
	 * @param name: Name of the Import Log
	 */
	public void openImportLog(String name) throws IOException, InterruptedException {
		ReportLogger.INFO("Opening the Import Logs with the name : " + name);
		Click(driver.findElement(By.xpath("//a[@title='" + name + "']")));
		Thread.sleep(5000);
	}

	/**
	 * Description: This method will open the Import Logs with the Name passed in the parameter
	 * @param name: Name of the Import Log
	 */
	public void openTransactionLog(String name) throws IOException, InterruptedException {
		ReportLogger.INFO("Opening the Transaction Logs with the name : " + name);
		Click(driver.findElement(By.xpath("//a[text()='" + name + "']")));
		Thread.sleep(7000);
	}
	
	
	
public void clickLogReocrdForParameters(String user,String status) throws Exception{
		
		driver.findElements(By.xpath("//div[@class='windowViewMode-normal oneContent active lafPageHost']//table//tbody//th[contains(.,'"+user+"')]/following-sibling::td[contains(.,'"+status+"')]//following-sibling::td//span[contains(.,'View')]")).get(1).click();
		objPage.waitForElementToBeClickable(objEFileImport.errorRowSection , 20);
		
		}

}
