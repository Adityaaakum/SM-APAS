package com.apas.Tests;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.apas.PageObjects.EFileHomePage;
import com.apas.PageObjects.LoginPage;
import com.apas.TestBase.TestBase;

public class EFileTest extends TestBase {

	// WebDriver driver;
	private RemoteWebDriver driver;
	LoginPage loginPageObject;
	EFileHomePage eFilePageObject;

	/*
	 * List<WebElement> countryList =
	 * driver.findElements(By.cssSelector("#select2-drop ul li"));
	 * for(WebElement country : countryList){
	 * if(country.getText().equals("India")) { country.click();
	 * Thread.sleep(3000); break; } }
	 */

	/**
	 * function uploadEFile:- to upload an E-File
	 * 
	 * @param filetype
	 * @param source
	 * @param period
	 * @param fileLocation
	 * @throws Exception
	 */

	@Test(description = "To Upload an E-File")
	public void uploadEFile(String filetype, String source, String period) throws Exception {
		eFilePageObject = new EFileHomePage(driver);
		loginPageObject = new LoginPage(driver);
		loginPageObject.loginToSandbox();

		eFilePageObject.selectFileAndSource(filetype, source);
		eFilePageObject.nextButton.click();
		eFilePageObject.periodDropdown.click();
		driver.findElement(By.xpath("//span[@class='slds-media__body']/span[contains(.,'" + period + "')]")).click();
		eFilePageObject.confirmButton.click();

		eFilePageObject.uploadFilebutton.click();
		// code for file upload
		eFilePageObject.doneButton.click();

		// now verifying the status of file import
		driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
		Assert.assertEquals("In Progress", eFilePageObject.status.getText());
	}

	/**
	 * function ViewImportedRecords:- to view the imported records
	 * 
	 * @param filetype
	 * @param source
	 * @param period
	 * @param fileLocation
	 * @throws InterruptedException
	 */
	@Test(description = "To View Import Records from E-File Import Tools screen")
	public void ViewImportedRecords(String filetype, String source, String period) throws InterruptedException {
		// TODO Auto-generated method stub

		eFilePageObject.selectFileAndSource(filetype, source);
		Thread.sleep(2000);

		// now selecting first record from list with 'Imported' status for the
		// given period

	}

	/**
	 * function viewImportLogs:- to view the logs of the file import
	 * 
	 * @param filetype
	 * @param source
	 * @param period
	 * @param fileLocation
	 * @throws InterruptedException
	 */
	@Test(description = "To View Import Logs")
	public void viewImportLogs(String filetype, String source, String period, String owner) {
		// TODO Auto-generated method stub
		driver.findElement(By.xpath("//span[@class='slds-truncate' and contains(.,'E-File Import Logs')]")).click();

		// now checking the status of the log
		Assert.assertEquals("Imported", driver.findElement(By.xpath("//tr[1]//td[6]//span/span[1]")).getText());

		/*
		 * String status=driver.findElement(By.
		 * xpath("//div[@class='uiVirtualDataTable indicator']//following-sibling::table[@role='grid']//tbody/tr[1]/td[6]/span/span[1]"
		 * )).getText(); Assert.assertEquals("Imported",status);
		 */

		// checking the created log
		driver.findElement(
				By.xpath("//tr[1]//a[@title='BPP Trend Factors :BOE - Index and Percent Good Factors :2019']")).click();

		// verify the fields
		Assert.assertEquals(filetype + " :" + source + " :" + period,
				driver.findElement(By
						.xpath("//span[@class='test-id__field-label' and contains(.,'Name')]/following::div/span/span"))
						.getText());
		Assert.assertEquals(owner,
				driver.findElement(By
						.xpath("//span[@class='test-id__field-label' and contains(.,'Owner')]/following::div/span//a"))
						.getText());
		Assert.assertEquals(source,
				driver.findElement(By
						.xpath("//span[@class='test-id__field-label' and contains(.,'File Source')]/following::div[1]/span[1]/span"))
						.getText());
		Assert.assertEquals(filetype,
				driver.findElement(By
						.xpath("//span[@class='test-id__field-label' and contains(.,'File Type')]/following::div[1]/span[1]/span"))
						.getText());
		Assert.assertEquals(period,
				driver.findElement(By
						.xpath("//span[@class='test-id__field-label' and contains(.,'Import Period')]/following::div[1]/span[1]/span"))
						.getText());
		System.out.println("Total Records in File::" + driver
				.findElement(By
						.xpath("//span[@class='test-id__field-label' and contains(.,'Total Records in File')]/following::div[1]/span[1]/span"))
				.getText());

		// Verifying the Transaction
		driver.findElement(By.xpath("//span[@class='title' and contains(.,'Transactions')]")).click();

		// verifying status
		Assert.assertEquals("Imported",
				driver.findElement(By.xpath("//div[@class='listViewContent']//table//tbody/tr[1]/td[1]")).getText());

		System.out.println("");
	}

	/**
	 * function uploadEFileFromNewStatus:- to upload an E-File
	 * 
	 * @param filetype
	 * @param source
	 * @param period
	 * @param fileLocation
	 * @throws InterruptedException
	 */

	@Test(description = "Uploading an E-File with status New")
	private void uploadEFileForNewStatus(String filetype, String source, String period) throws InterruptedException {
		eFilePageObject.selectFileAndSource(filetype, source);
		// now selecting first record from list with 'Imported' status for the
		// given period
	}
}
