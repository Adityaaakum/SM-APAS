package com.apas.Tests.ApasSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.TRAPage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class TRATest extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	TRAPage objTRAPage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objTRAPage = new TRAPage(driver);
	}

	/**
	 * This method is to Verify that system should not allow duplicate TRAs with the
	 * same name and error message and link to the existing TRA should be displayed
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3256:Verify that system should not allow duplicate TRAs with the same name and error message and link to the existing TRA should be displayed", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement","TRAUpdates" })

	public void ParcelManagement_VerifyErrorMessagesDuplicateTRA(String loginUser) throws Exception {

		// Fetching TRA data
		String TRACreationData = testdata.TRA_DATA;
		Map<String, String> hashMapTRAData = objUtil.generateMapFromJsonFile(TRACreationData, "DataToCreateTRA");

		// deleting the existing TRA record
		String queryTRAValue = "SELECT id FROM TRA__c where Name='" + hashMapTRAData.get("TRA Number") + "'";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		if (responseTRADetails.size() > 0)

			responseTRADetails.get("Id").stream().forEach(Id ->

			{
				salesforceAPI.delete(responseTRADetails.get("Id").get(0));
			});

		// Step1: Login to the APAS application
		objTRAPage.login(loginUser);

		// Step2: Opening the TRA page and searching the parcel to perform Retire Action
		objTRAPage.searchModule(EFILE_INTAKE_VIEW);
		objTRAPage.searchModule(TRA);
		objTRAPage.createTRARecord(hashMapTRAData);
		objTRAPage.waitForElementToBeClickable(objTRAPage.successAlert, 25);

		// Step3 : Creating a new TRA record with same name as above created TRA record
		objTRAPage.searchModule(TRA);
		objTRAPage.createTRARecord(hashMapTRAData);
		softAssert.assertContains(objTRAPage.saveRecordAndGetError(),
				"You can't save this record because a duplicate record already exists. To save, use different information.",
				"SMAB-T3256: Verify that system should not allow duplicate TRAs with the same name and error message and link to the existing TRA should be displayed");

		// Step 4: Clicking on view duplicates LInk
		objTRAPage.Click(objTRAPage.viewDuplicatesLinkPageError);
		softAssert.assertEquals(objTRAPage.getElementText(objTRAPage.viewDuplicatesLinkPopUpMessage),"There is an existing TRA that matches the criteria entered. Duplicate TRAs are not allowed.",
				"SMAB-T3256: Verify that link to the existing TRA should open up View Duplicates Pop Up");
		objTRAPage.logout();

	}
}
