package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.util.List;

public class EFileImportTransactionsPage extends Page {

	public EFileImportTransactionsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//a[@data-label='Details' and @role='tab']")
	public WebElement detailsTab;

	@FindBy(xpath = "//a[@data-label='Transaction Trails' and @role='tab']")
	public WebElement transactionTrailTab;

	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and contains(.,'Status')]/parent::div/following-sibling::div//lightning-formatted-text")
	public WebElement statusLabel;
	
	@FindBy(xpath = "//one-app-nav-bar-item-root//a[@title = 'E-File Import Logs']")
	public WebElement efileImportLogLabel;
	
	@FindBy(xpath = "//one-app-nav-bar-item-root//a[@title = 'E-File Import Transactions']")
	public WebElement efileImportTransactionLabel;
	
	@FindBy(xpath = "//a[contains(.,'Import Transaction-00')]")
	public WebElement importTransactionName;

	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//a[@data-label='Transactions' and @role='tab']")
	public WebElement transactionsTab;

	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//table//tr//a[contains(.,'Import Transaction')]")
	public List<WebElement> transactionsRecords;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//table//tr//a[contains(.,'Trail')]")
	public List<WebElement> transactionTrailRecords;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and text()='Type']/parent::div/following-sibling::div//lightning-formatted-text")
	public WebElement transactionType;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and text()='Sub Type']/parent::div/following-sibling::div//lightning-formatted-text")
	public WebElement transactionSubType;
		
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and text()='E-File Import Transaction']/parent::div/following-sibling::div//a")
	public WebElement efileImportTransactionLookUp;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and text()='Building Permit']/parent::div/following-sibling::div//a")
	public WebElement transactionBuildingPermit;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and text()='APN']/parent::div/following-sibling::div//a")
	public WebElement transactionAPN;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and text()='Description']/parent::div/following-sibling::div//lightning-formatted-text")
	public WebElement transactionDescription;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and text()='Uploaded File']/parent::div/following-sibling::div//a")	
	public WebElement uploadedFileInAuditTrail;	
		
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//*[@class='test-id__field-label' and text()='Duplicate Count']/parent::div/following-sibling::div//slot[@slot='outputField']")	
	public WebElement duplicateCountTransaction;	
			
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//a[@title='Download']")	
	public WebElement downloadButtonTransactionTrail;	
		
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(text(),'File Count')]/parent::div/following-sibling::div//lightning-formatted-number")	
	public WebElement transactionFileCount;	
		
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(text(),'Total Records Imported')]/parent::div/following-sibling::div//lightning-formatted-number")	
	public WebElement transactionImportCount;	
		
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(text(),'Total Error Records')]/parent::div/following-sibling::div//lightning-formatted-number")	
	public WebElement transactionErrorCount;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//table//tr//a[contains(.,'Trail')]")	
	public List<WebElement> transactionTrailrecordsCount;
	
	/**
	 * Description: This method will open the Import Transactions with the Name passed in the parameter
	 * @param name: Name of the Import Log
	 */
	public void openImportTransactions(String name) throws IOException, InterruptedException {
		ReportLogger.INFO("Opening the Import Transactions with the name : " + name);
		Click(driver.findElement(By.xpath("//a[@title='" + name + "']")));
		Thread.sleep(5000);
	}


}
