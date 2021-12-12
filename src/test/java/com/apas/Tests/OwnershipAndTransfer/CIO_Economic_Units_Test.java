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
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	CIOTransferPage objCioTransfer;

	Page objPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objCioTransfer = new CIOTransferPage(driver);

		driver.manage().timeouts().pageLoadTimeout(15, TimeUnit.SECONDS);

	}
	/*
	 * Economic Units- Verify that RP Appraiser is able to select certain parcels
	 * and group them as economic units
	 */

	@Test(description = "SMAB-T4280,SMAB-T4153,SMAB-T4152: Verify that RP Appraiser is able to select certain parcels and group them as economic units ", dataProvider = "loginRPAppraiser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "EconomicUnits" ,"ChangeInOwnershipManagement"}, enabled = true)
	public void EconomicUnits_GroupParcelsIntoEconomicUnit(String loginUser) throws Exception {

		// fetch parcels to add in economic unit
		String getApnToAddInEconomicUNit = "Select Id,Name from Parcel__c where Status__c='Active' and PUC_Code_Lookup__c not in ( select id from puc_code__c where name like '%retire%') Limit 3";
		HashMap<String, ArrayList<String>> hashMapApn = salesforceAPI.select(getApnToAddInEconomicUNit);
		String APNNameList[] = new String[3];
		String APNIdList[] = new String[3];

		for (int i = 0; i < 3; i++) {
			APNNameList[i] = hashMapApn.get("Name").get(i);
			APNIdList[i] = hashMapApn.get("Id").get(i);

		}
		String listOfParcelsEconomicUnit = APNNameList[0] + "," + APNNameList[1] + "," + APNNameList[2];
		String incorrectlistOfParcelsEconomicUnit = "010234567,234567890,123456789";

		JSONObject jsonNObject = objParcelsPage.getJsonObject();

		jsonNObject.put("of_Parcels_in_Economic_Unit__c", "");
		jsonNObject.put("Part_of_Economic_Unit__c", "");
		jsonNObject.put("List_of_all_Parcels_in_Economic_Unit__c", "");

		// updating parcel details
		salesforceAPI.update("Parcel__c", APNIdList[0], jsonNObject);
		salesforceAPI.update("Parcel__c", APNIdList[1], jsonNObject);
		salesforceAPI.update("Parcel__c", APNIdList[2], jsonNObject);

		// login with RP Appraiser Profile USer and search for a parcel to create
		// economic unit

		objParcelsPage.login(loginUser);
		String execEnv = System.getProperty("region");

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/"+
				APNIdList[0] + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
		objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		

		// creating economic unit

		objCioTransfer.editRecordedApnField(objParcelsPage.partOfEconomicUnit);
		objCioTransfer.waitForElementToBeVisible(6, objParcelsPage.partOfEconomicUnit);

		objParcelsPage.scrollToElement(objParcelsPage.getWebElementWithLabel(objParcelsPage.partOfEconomicUnit));
		objCioTransfer.selectOptionFromDropDown(objParcelsPage.partOfEconomicUnit, "Yes");
		objCioTransfer.enter(objParcelsPage.listOfParcelsEconomicUnit, listOfParcelsEconomicUnit);
		objCioTransfer.enter(objParcelsPage.numberOfParcelsEconomicUnit, "3");

		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

		// verify that economic unit is created properly for the first parcel
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.partOfEconomicUnit,
						"Parcel Characteristics Summary"),
				"Yes", "SMAB-T4152:-Part of economic unit field should be Yes after creatinmg economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.numberOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				"3",
				"SMAB-T4152:- NUmber of parcels in economic unit field should be correct after creating economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.listOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				listOfParcelsEconomicUnit,
				"SMAB-T4152:- list Of Parcels in EconomicUnit field should be correct after creating economic unit");

		// verify that economic unit is created properly for the second parcel


		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/"+
				APNIdList[1] + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
		objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.partOfEconomicUnit,
						"Parcel Characteristics Summary"),
				"Yes", "SMAB-T4152:-Part of economic unit field should be Yes after creatinmg economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.numberOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				"3",
				"SMAB-T4152:- NUmber of parcels in economic unit field should be correct after creating economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.listOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				listOfParcelsEconomicUnit,
				"SMAB-T4152:- list Of Parcels in EconomicUnit field should be correct after creating economic unit");

		// verify that economic unit is created properly for the third parcel

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/"+
				APNIdList[2] + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
		objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.partOfEconomicUnit,
						"Parcel Characteristics Summary"),
				"Yes", "SMAB-T4152:-Part of economic unit field should be Yes after creatinmg economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.numberOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				"3",
				"SMAB-T4152:- NUmber of parcels in economic unit field should be correct after creating economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.listOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				listOfParcelsEconomicUnit,
				"SMAB-T4152:- list Of Parcels in EconomicUnit field should be correct after creating economic unit");

		// verifying validations of economic unit fields in third parcel

		objCioTransfer.editRecordedApnField(objParcelsPage.partOfEconomicUnit);
		objCioTransfer.waitForElementToBeVisible(6, objParcelsPage.partOfEconomicUnit);

		objParcelsPage.scrollToElement(objParcelsPage.getWebElementWithLabel(objParcelsPage.partOfEconomicUnit));
		objCioTransfer.enter(objParcelsPage.numberOfParcelsEconomicUnit, "2");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

		softAssert.assertContains(objCioTransfer.getElementText(objCioTransfer.pageError),
				"ENTER ALL THE PARCELS FOR THIS ECONOMIC UNIT",
				"SMAB-T4153: Validate that proper message is displayed when there is mismatch in number of parcels and list of parcels in economic unit ");

		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));
		objCioTransfer.editRecordedApnField(objParcelsPage.partOfEconomicUnit);
		objCioTransfer.waitForElementToBeVisible(6, objParcelsPage.partOfEconomicUnit);

		objCioTransfer.enter(objParcelsPage.listOfParcelsEconomicUnit, incorrectlistOfParcelsEconomicUnit);

		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

		softAssert.assertContains(objCioTransfer.getElementText(objCioTransfer.pageError),
				"ENTER PARCEL NUMBER IN XXX-XXX-XXX FORMAT",
				"SMAB-T4153: Validate that proper message is displayed when the parcel number added in list of parcels in economic unit field is in improper format ");

		// verifying removal of third parcel from economic unit

		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.CancelButton));

		objCioTransfer.editRecordedApnField(objParcelsPage.partOfEconomicUnit);
		objCioTransfer.waitForElementToBeVisible(6, objParcelsPage.partOfEconomicUnit);

		objParcelsPage.scrollToElement(objParcelsPage.getWebElementWithLabel(objParcelsPage.partOfEconomicUnit));

		objCioTransfer.selectOptionFromDropDown(objParcelsPage.partOfEconomicUnit, "No");
		objCioTransfer.Click(objCioTransfer.getButtonWithText(objCioTransfer.saveButton));

		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.partOfEconomicUnit,
						"Parcel Characteristics Summary"),
				"No", "SMAB-T4280:-Part of economic unit field should be No after removing parcel from  economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.numberOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				"",
				"SMAB-T4280:- NUmber of parcels in economic unit field should be blank after  removing parcel from economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.listOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				"",
				"SMAB-T4280:- list Of Parcels in EconomicUnit field should be blank  after removing parcel from economic unit");

		listOfParcelsEconomicUnit = APNNameList[0] + "," + APNNameList[1];

		// verify that economic unit still exists for first parcel

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/"+
				APNIdList[0] + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
		objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.partOfEconomicUnit,
						"Parcel Characteristics Summary"),
				"Yes",
				"SMAB-T4280:-Part of economic unit field should be Yes for first parcel even though third parcel is removed from economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.numberOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				"2",
				"SMAB-T4280:- NUmber of parcels in economic unit field should be updated after third parcel is removed from economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.listOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				listOfParcelsEconomicUnit,
				"SMAB-T4280:- list Of Parcels in EconomicUnit field should be updated after third parcel is removed from economic unit");

		// verify that economic unit still exists for second parcel

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com/lightning/r/Parcel__c/"+
				APNIdList[1] + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
		objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));
		
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.partOfEconomicUnit,
						"Parcel Characteristics Summary"),
				"Yes",
				"SMAB-T4280:-Part of economic unit field should be Yes for second parcel even though third parcel is removed from economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.numberOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				"2",
				"SMAB-T4280:- NUmber of parcels in economic unit field should be updated after third parcel is removed from economic unit");
		softAssert.assertEquals(
				objParcelsPage.getFieldValueFromAPAS(objParcelsPage.listOfParcelsEconomicUnit,
						"Parcel Characteristics Summary"),
				listOfParcelsEconomicUnit,
				"SMAB-T4280:- list Of Parcels in EconomicUnit field should be updated after third parcel is removed from economic unit");
		
		objParcelsPage.logout();

	}

}