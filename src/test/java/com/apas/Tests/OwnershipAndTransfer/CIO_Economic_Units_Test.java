package com.apas.Tests.OwnershipAndTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;


public class CIO_Economic_Units_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	CIOTransferPage objCioTransfer;

	Page objPage;
	String apnPrefix = new String();
	

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objCioTransfer = new CIOTransferPage(driver);

		objPage= new Page(driver);
		
		driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);

	}
	/*
	 * Verify that NO APN WI is genrated for document without APN and user has the
	 * ability to add recorded APN on it to create a WI for MAPPING OR CIO
	 * 
	 */

	@Test(description = "SMAB-T3763,SMAB-T3106,SMAB-T3111:Verify the type of WI system created for a recorded document with no APN ", dataProvider = "RPAppraiser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "EconomicUnits" }, enabled = true)
	public void RecorderIntegration_VerifyNewWIgeneratedfromRecorderIntegrationForNOAPNRecordedDocument(
			String loginUser) throws Exception {
		
		//fetch parcels to add in economic unit 
		String getApnToAddInEconomicUNit = "Select Id,Name from Parcel__c where Status__c='Active' and PUC_Code_Lookup__c not in ( select id from puc_code__c where name like '%retire%') Limit 3";
		HashMap<String, ArrayList<String>> hashMapApn = salesforceAPI.select(getApnToAddInEconomicUNit);
		String APNNameList[] = new String[3];
		String APNIdList[] = new String[3];

		
		for (int i=0;i<3;i++)
		{
			APNNameList[i]=hashMapApn.get("Name").get(i);
			APNIdList[i]=hashMapApn.get("Id").get(i);

		}
		String listOfParcelsEconomicUnit =APNNameList[0]+","+APNNameList[1]+","+APNNameList[2];

		
		JSONObject jsonNObject= objParcelsPage.getJsonObject();
		
		jsonNObject.put("of_Parcels_in_Economic_Unit__c","");
		jsonNObject.put("Part_of_Economic_Unit__c","");
		jsonNObject.put("List_of_all_Parcels_in_Economic_Unit__c","");

		//updating parcel  details
		salesforceAPI.update("Parcel__c",APNIdList[0],jsonNObject);
		salesforceAPI.update("Parcel__c",APNIdList[1],jsonNObject);
		salesforceAPI.update("Parcel__c",APNIdList[2],jsonNObject);

		
		// login with RP Appraiser Profile USer and search for a parcel to create economic unit 

		objParcelsPage.login(loginUser);
		objParcelsPage.searchModule(PARCELS);
		objParcelsPage.globalSearchRecords(APNNameList[0]);
		
		
		// creating economic unit 
		
		objCioTransfer.editRecordedApnField(objParcelsPage.partOfEconomicUnit);
		objCioTransfer.waitForElementToBeVisible(6, objParcelsPage.partOfEconomicUnit);

		objParcelsPage.scrollToElement(objParcelsPage.getWebElementWithLabel(objParcelsPage.partOfEconomicUnit));
		objCioTransfer.selectOptionFromDropDown(objParcelsPage.partOfEconomicUnit,
				"Yes");
		objCioTransfer.enter(objParcelsPage.listOfParcelsEconomicUnit,listOfParcelsEconomicUnit);
		objCioTransfer.enter(objParcelsPage.numberOfParcelsEconomicUnit,"3");
		
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));
		
		
		
		
		objParcelsPage.logout();

	}

	}