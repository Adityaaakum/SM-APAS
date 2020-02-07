package com.apas.Tests;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.apas.Assertions.CustomSoftAssert;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.EFileImportTransactions;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;
import com.apas.config.testdata;
import com.apas.generic.SalsesforceStandardFunctions;

public class BuildingPermitTest extends TestBase implements testdata {
	
	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	EFileImportTransactions objEfileImportTransactionsPage;
	SalsesforceStandardFunctions salesforceStandardFunctions;
	CustomSoftAssert softassert = new CustomSoftAssert();

	@Test(description = "SMAB-T430: Transaction record verification for the import BP file", priority = 0, alwaysRun = true)
	public void transactionRecordVerificationBuildingPermit() throws Exception {
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactions(driver);	
		salesforceStandardFunctions = new SalsesforceStandardFunctions(driver);
			
		salesforceStandardFunctions.login();
		salesforceStandardFunctions.searchApps("E-File Intake");
		String athertonBuildingPermitsFile = System.getProperty("user.dir") + BUILDING_PERMIT_ATHERTON;
		salesforceStandardFunctions.uploadFileOnEfileIntake("Building Permit", "Atherton Building Permits", "January 2020",athertonBuildingPermitsFile);
		salesforceStandardFunctions.searchApps("E-File Import Transactions");
		objPage.Click(driver.findElement(By.xpath("//a[contains(.,'Import Transaction-00')]")));
		softassert.assertEquals(objPage.getAttributeValue(objEfileImportTransactionsPage.detailsTab,"aria-selected"), "true", "SMAB-T430: Validation if Details tab is selected by default after clicking on import transaction link");
		softassert.assertEquals(objPage.getElementText(objEfileImportTransactionsPage.statusLabel), "Imported", "SMAB-T430: Status Validation of the imported file on import transaction tab");
		salesforceStandardFunctions.logout();
		softassert.assertAll();
		
	}
}
