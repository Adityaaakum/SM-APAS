package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class Parcel_Management_Parcel_Verification_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject= new JSONObject();
	String apnPrefix=new String();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);

	}

	@Test(description = ",SMAB-T2481: Verify that when a Parcel Situs record is created/updated,"
			+ " the system should automatically populate the Primary Situs field on the Parcel record",
			dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
					"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyPrimarySitusOnParcelDetail(String loginUser) throws Exception {

		String execEnv= System.getProperty("region");		

		String apn = objMappingPage.fetchActiveAPN();
		String queryAPN = "Select Id from Parcel__c where Name = '"+apn+"'";

		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String id=responseAPNDetails.get("Id").get(0);
		objMappingPage.deleteParcelSitusFromParcel(id);
		
		//  user login to APAS application
		objMappingPage.login(loginUser);
		
		//  Opening the PARCELS page  and searching the  parcel to perform remap mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);	

		//User navigate to parcel situs tab
		String parcelSitusURL = "https://smcacre--"+ execEnv+ ".lightning.force.com/lightning/r/Parcel__c/"
				+ id+ "/related/Parcel_Situs__r/view";
		ReportLogger.INFO("Navigate to situs URL: " +parcelSitusURL);
		driver.navigate().to(parcelSitusURL);

		String createNewParcelSitus = testdata.MANUAL_PARCEL_CREATION_DATA;
		Map<String, String> hashMapCreateNewParcelSitus = objUtil.generateMapFromJsonFile(createNewParcelSitus,
				"DataToCreateParcelSitus");
		String primarySitus = objParcelsPage.createParcelSitus(hashMapCreateNewParcelSitus);

		objMappingPage.globalSearchRecords(apn);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS
				(objMappingPage.parcelPrimarySitus,"Parcel Information"),primarySitus,
				"SMAB-T3543: Verify that when a Parcel Situ record is created/updated,"
				+ " the system should automatically populate the Primary Situs field on the Parcel record");
		
		ReportLogger.INFO("Navigate to situs URL: " +parcelSitusURL);

		driver.navigate().to(parcelSitusURL);
		objMappingPage.waitForElementToBeVisible(objMappingPage.parcelSitusGridEditButton, 10);
		objMappingPage.Click(objMappingPage.parcelSitusGridEditButton);
		objMappingPage.Click(objMappingPage.parcelSitusEditButton);
		objMappingPage.waitForElementToBeVisible(objMappingPage.visibleParcelSitusEditpopUp, 10);
		objMappingPage.selectOptionFromDropDown(objParcelsPage.isPrimaryDropdown, "No");
		objMappingPage.Click(objMappingPage.parcelSitusEditSaveButton);
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS
				(objMappingPage.parcelPrimarySitus,"Parcel Information"),"",
				"SMAB-T3543: Verify that when a Parcel Situ record is created/updated,"
				+ " the system should automatically populate the Primary Situs field on the Parcel record");
		
		objMappingPage.logout();

	}
}

