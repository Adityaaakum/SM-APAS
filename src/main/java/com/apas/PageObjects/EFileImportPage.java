package com.apas.PageObjects;

import java.io.IOException;

import com.apas.Reports.ReportLogger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;

public class EFileImportPage extends Page {

	public EFileImportPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	Page objPage = new Page(this.driver);

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

	@FindBy(xpath = "//button[contains(.,'Cancel')]")
	public WebElement cancelButton;

	@FindBy(xpath = "//button[text()='Continue']")
	public WebElement continueButton;

	@FindBy(xpath = "//div[@class='warning']")
	public WebElement warning;

	@FindBy(xpath = "//*[@data-key='upload']")
	public WebElement uploadFilebutton;

	@FindBy(xpath = "//Input[@name='Upload Data CSV']")
	public WebElement uploadFileInputBox;

	@FindBy(xpath = "//button//span[contains(.,'Done')]")
	public WebElement doneButton;

	@FindBy(xpath = "//tr[1]/td[5]//div")
	public WebElement status;

	@FindBy(xpath = "(//button[@title='Preview'])[1]")
	public WebElement viewLink;

	@FindBy(xpath = "//span[contains(@title,'ERROR ROWS')]")
	public WebElement errorRowSection;

	@FindBy(xpath = "//button[contains(.,'ERROR ROWS')]")
	public WebElement errorRowSectionExpandButton;

	@FindBy(xpath = "//span[contains(@title,'IMPORTED ROWS')]")
	public WebElement importedRowSection;

	@FindBy(xpath = "//button[contains(.,'IMPORTED ROWS')]")
	public WebElement importedRowSectionExpandButton;

	@FindBy(xpath = "(//td[@data-label='Status'])[1]")
	public WebElement statusImportedFile;

	@FindBy(xpath = "(//td[@data-label='Number of Tries'])[1]")
	public WebElement numberOfTimesTriedRetried;

	@FindBy(xpath = "(//td[@data-label='Import Count'])[1]")
	public WebElement totalRecordsImportedFile;

	@FindBy(xpath = "(//td[@data-label='File Count'])[1]")
	public WebElement totalRecordsInFile;

	@FindBy(xpath = "//a[contains(@data-label,'BuildingPermits')]")
	public WebElement buildingPermitLabel;

	@FindBy(xpath = "//input[@class='datatable-select-all'][@type='checkbox']/..//span[@class='slds-checkbox_faux']")
	public WebElement selectAllCheckBox;

	@FindBy(xpath = "//td//span[@class='slds-checkbox_faux']")
	public WebElement rowSelectCheckBox;

	@FindBy(xpath = "//button[text()='Approve']")
	public WebElement approveButton;

	@FindBy(xpath = "//button[text()='Discard']")
	public WebElement discardButton;

	@FindBy(xpath = "//button[text()='Revert']")
	public WebElement revertButton;

	@FindBy(xpath = "//button[text()='Retry']")
	public WebElement retryButton;

	@FindBy(xpath = "//button[text()='Close']")
	public WebElement closeButton;

	@FindBy(xpath = "//*[text()='Thank you! Imported records has been approved successfully.']")
	public WebElement efileRecordsApproveSuccessMessage;

	@FindBy(xpath = "//*[text()='Thank you! All records has been reverted successfully.']")
	public WebElement revertSuccessMessage;

	@FindBy(xpath = "//span[@title='ERROR_MESSAGE']")
	public WebElement editErrorMessage;

	@FindBy(xpath = "//*[@data-label='PERMITNO'][@role='gridcell']//button")
	public WebElement editButtonValue;

	@FindBy(xpath = "//input[@class='slds-input']")
	public WebElement editButtonInput;

	/**
	 * This method will select the file type and source from E-File Import Tool page
	 * @param fileType : Value from File Type Drop Down
	 * @param source: Value from source drop down
	 */
	public void selectFileAndSource(String fileType, String source) throws IOException, InterruptedException {
		System.out.println("File type is:" + fileType + " and Source is:" + source);
		Click(fileTypedropdown);
		Thread.sleep(2000);
		Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + fileType + "')]")));
		Click(sourceDropdown);
		Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + source + "')]")));
	}

	/**
	 * This method will upload the file on Efile Import module
	 * @param fileType : Value from File Type Drop Down
	 * @param source: Value from source drop down
	 * @param period: Period for which the file needs to be uploaded
	 * @param absoluteFilePath: Absoulte Path of the file with the file name
	 */
	public void uploadFileOnEfileIntake(String fileType, String source,String period, String absoluteFilePath) throws Exception{
		ReportLogger.INFO("Uploading " +  absoluteFilePath + " file");
		selectFileAndSource(fileType, source);
		objPage.waitUntilElementDisplayed(nextButton, 15);
		objPage.scrollToTop();
		objPage.Click(nextButton);
		objPage.Click(periodDropdown);
		objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")));
		objPage.Click(confirmButton);
		Thread.sleep(2000);
		uploadFileInputBox.sendKeys(absoluteFilePath);
		Thread.sleep(2000);
		objPage.waitForElementToBeClickable(doneButton);
		Thread.sleep(2000);
		objPage.Click(doneButton);
		waitForElementToBeClickable(statusImportedFile,20);
		objPage.waitForElementTextToBe(statusImportedFile, "In Progress", 120);
	}
	
	/**
	 * This method will upload the file on Efile Import module
	 * @param fileType : Value from File Type Drop Down
	 * @param source: Value from source drop down
	 * @param period: Period for which the file needs to be uploaded
	 * @param absoluteFilePath: Absoulte Path of the file with the file name
	 */
	public void uploadInvalidFormatFileOnEfileIntake(String fileType, String source,String period, String absoluteFilePath) throws Exception{
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading " +  absoluteFilePath + " file");
		selectFileAndSource(fileType, source);
		objPage.waitUntilElementDisplayed(nextButton, 10);
		objPage.Click(nextButton);
		objPage.Click(periodDropdown);
		objPage.Click(driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")));
		objPage.Click(confirmButton);
		Thread.sleep(2000);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Uploading " + absoluteFilePath + " on Efile Import Tool");
		uploadFileInputBox.sendKeys(absoluteFilePath);
		Thread.sleep(2000);
	}
	
	/**
	 * This method will expand the section provided in webelement passed in the parameter
	 * @param element : section to be expanded
	 */
	public void expandSection(WebElement element) throws IOException {
		String ariaExpanded = getAttributeValue(element,"aria-expanded");
		if (ariaExpanded.equals("false"))
			Click(element);
	}

	/**
	 * This method will collapse the section provided in webelement passed in the parameter
	 * @param element : section to be expanded
	 */
	public void collapseSection(WebElement element) throws IOException {
		String ariaExpanded = getAttributeValue(element,"aria-expanded");
		if (ariaExpanded.equals("true"))
			Click(element);
	}

}
