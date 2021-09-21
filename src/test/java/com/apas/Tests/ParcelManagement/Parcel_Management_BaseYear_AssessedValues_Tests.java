package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class Parcel_Management_BaseYear_AssessedValues_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	ApasGenericPage objApasGenericPage;
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject = new JSONObject();
	String apnPrefix = new String();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);

	}

	/**
	 * This method is to Verify the fields mentioned on 'Assessed value' object on
	 * the Parcel.
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3196,SMAB-T3184:Verify the UI validations for the fields mentioned on 'Assessed value' object on the Parcel.Parcel record should exist containing Assessed Value records.", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement" })
	public void ParcelManagement_VerifyAssessedValuesObjectUIValidationsForProp19(String loginUser) throws Exception {

		// Fetching the Active Parcel
		String query = "SELECT Primary_Situs__c,Status__C,Name FROM Parcel__c where Status__C='Active' and Primary_Situs__C !='' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String parcelToSearch = response.get("Name").get(0);

		// Step1: Login to the APAS application using the credentials passed through
		objParcelsPage.login(loginUser);

		// Step2: Opening the Parcels module
		objParcelsPage.searchModule(modules.PARCELS);

		// Step3: Search and Open the Parcel then clicks on more tab and then clicks on
		// Assessed Values
		objParcelsPage.globalSearchRecords(parcelToSearch);
		objParcelsPage.Click(objParcelsPage.moretab);
		objParcelsPage.Click(objParcelsPage.assessedValue);

		objParcelsPage.openNewAssessedValueForm();
		objParcelsPage.enter(objParcelsPage.landCashValue, "400000");
		objParcelsPage.enter(objParcelsPage.improvementCashValue, "300000");
		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.fullCashValue), "Full Cash Value",
				"SMAB-T3196,SMAB-T3184: Validation that  Full Cash Value text is visible.");
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Prop 19");

		
		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.differenceValue), "Difference",
				"SMAB-T3196,SMAB-T3184: Validation that  Difference text is visible for Prop 19");

		
		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.totalValueOnForm), "Total",
				"SMAB-T3196,SMAB-T3184: Validation that  Total text is visible for Prop 19");

		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.newTaxableValueText), "New Taxable Value",
				"SMAB-T3196,SMAB-T3184: Validation that  New Taxable Value text is visible for Prop 19");

		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.originFcvText), "Origin FCV",
				"SMAB-T3196,SMAB-T3184: Validation that  Origin FCV text is visible for Prop 19");

		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.combinedFbyvAndHpi), "Combined FBYV and HPI",
				"SMAB-T3196,SMAB-T3184: Validation that  Combined FBYV and HPI text is visible for Prop 19");
		/*
		 * Following lines written for a field which one got changed but I wrote code
		 * according to the story SMAB-9823. This field is no more visible for Prop 19
		 */

//		WebElement objFactoredBYV = driver.findElement(By.xpath("//slot/slot/flexipage-column2[2]/div/slot/flexipage-field[6]/slot/record_flexipage-record-field/div/div/div[1]/span[1]"));
//		System.out.println(objMappingPage.getElementText(objFactoredBYV));
//		softAssert.assertEquals(objMappingPage.getElementText(objFactoredBYV),"Factored BYV",
//				"SMAB-T3184: Validation that Factored BYV text is visible for Prop 19");

		objParcelsPage.enter(objParcelsPage.originDov, "1/1/2021");
		objParcelsPage.enter(objParcelsPage.originLandValue, "112021");
		objParcelsPage.enter(objParcelsPage.originImprovementValue, "112021");
		objParcelsPage.enter(objParcelsPage.hpiValueAllowance, "112021");


		// Asserting those fields shouldn't be visible on Prop 60

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Prop 60");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(differenceValue) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxableValueText) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objFactoredBYV)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Factored BYV) are nor visible when Assessed Type is not 'Prop 19'");

		// Asserting those fields shouldn't be visible on Prop 90

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Prop 90");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(differenceValue) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxableValueText) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objFactoredBYV)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Factored BYV) are nor visible when Assessed Type is not 'Prop 19'");

		// Asserting those fields shouldn't be visible on Prop 110

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Prop 110");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(differenceValue) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxableValueText) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objFactoredBYV)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Factored BYV) are nor visible when Assessed Type is not 'Prop 19'");

		// Asserting those fields shouldn't be visible on Temporary Value

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Temporary Value");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(differenceValue) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxableValueText) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objFactoredBYV)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Factored BYV) are nor visible when Assessed Type is not 'Prop 19'");

		// Asserting those fields shouldn't be visible on CIP

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "CIP");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(differenceValue) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxableValueText) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objFactoredBYV)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Factored BYV) are nor visible when Assessed Type is not 'Prop 19'");
		// Asserting those fields shouldn't be visible on Assessed Value

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Assessed Value");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(differenceValue) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxableValueText) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are nor visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objFactoredBYV)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Factored BYV) are nor visible when Assessed Type is not 'Prop 19'");

	}

	/**
	 * This method is to Verify the fields mentioned on 'Assessed value' object on
	 * the Parcel for Admin User.
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3221, SMAB-3198:Verify the UI validations for the fields mentioned on 'Assessed value' object on the Parcel.Parcel record should exist containing Assessed Value records.", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement" })
	public void ParcelManagement_VerifyAssessedValuesObjectUIValidationsTemporaryValues(String loginUser)
			throws Exception {

		// Fetching the Active Parcel
		String query ="SELECT name from Parcel__c where Status__c='Active' Limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String parcelToSearch = response.get("Name").get(0);

		// Step1: Login to the APAS application using the credentials passed through
		
		objParcelsPage.login(loginUser);

		// Step2: Opening the Parcels module
		objParcelsPage.searchModule(modules.PARCELS);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.inlineEditIcon);
		objParcelsPage.waitForElementToBeInVisible(parcelToSearch, 0);

		// Step3: Search and Open the Parcel then clicks on more tab and then clicks on
		// Assessed Values
		objParcelsPage.globalSearchRecords(parcelToSearch);
		objParcelsPage.Click(objParcelsPage.moretab);
		objParcelsPage.Click(objParcelsPage.assessedValue);

		// Step4: Clicks on New button and validate first type of decline should be
		// Decline and additional decline should be Calamity.

//		objParcelsPage.openNewAssessedValueForm();
		driver.navigate().to("https://smcacre--qa.lightning.force.com/lightning/o/Assessed_BY_Values__c/new?count=1");
		objParcelsPage.searchAndSelectOptionFromDropDown(objParcelsPage.apn, parcelToSearch);
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Temporary Value");
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.typeOfDecline, "Decline");
		objParcelsPage.enter(objParcelsPage.land, "400000");
		objParcelsPage.enter(objParcelsPage.improvements, "300000");
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.additionalDeclines, "Yes");

		WebElement webElement = driver.findElement(By.xpath(
				"//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//flexipage-field[contains(@data-field-id,'RecordAdditional_Type_of_Decline__cField')]//label[text()='Type of Decline']"));

		objParcelsPage.scrollToElement(webElement);
		objParcelsPage.javascriptClick(webElement);

		WebElement xpathDropDownOption = driver.findElement(By.xpath(
				"//flexipage-field[contains(@data-field-id,'RecordAdditional_Type_of_Decline__cField')]//span[contains(@title,'Calamity')]"));
		objParcelsPage.waitForElementToBeClickable(xpathDropDownOption, 8);
		objParcelsPage.scrollToElement(xpathDropDownOption);
		objParcelsPage.waitForElementToBeClickable(xpathDropDownOption, 8);

		objParcelsPage.javascriptClick(xpathDropDownOption);


		String landValueSmall = "200,000";
		String improvementValueSmall = "100,000";
		objParcelsPage.enter(objParcelsPage.additionalLand, landValueSmall);
		objParcelsPage.enter(objParcelsPage.additionalImprovement, improvementValueSmall);

		// Step5: Clicks on New button and validate first type of decline should be
		// Calamity and additional decline should be Decline.

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.typeOfDecline, "Calamity");
		objParcelsPage.scrollToElement(webElement);
		objParcelsPage.javascriptClick(webElement);

		WebElement xpathSecondDropDownOption = driver.findElement(By.xpath(
				"//flexipage-field[contains(@data-field-id,'RecordAdditional_Type_of_Decline__cField')]//span[contains(@title,'Decline')]"));
		objParcelsPage.waitForElementToBeClickable(xpathSecondDropDownOption, 8);
		objParcelsPage.scrollToElement(xpathSecondDropDownOption);
		objParcelsPage.waitForElementToBeClickable(xpathSecondDropDownOption, 8);
		objParcelsPage.javascriptClick(xpathSecondDropDownOption);

		WebElement saveButton = driver.findElement(By.xpath("//button[@name='SaveEdit']"));
		objParcelsPage.javascriptClick(saveButton);

		// Step6: After clicking Save Button user is on new created assessed Value page.
		// User is validating the total value here.

		objParcelsPage.waitForElementToBeVisible(objParcelsPage.landValue, 8);


		String landValueText = objParcelsPage.landValue.getText();
		String improvementValueText = objParcelsPage.improvementValue.getText();
		String totalValueText = objParcelsPage.totalValue.getText();
		softAssert.assertEquals(improvementValueSmall, improvementValueText,
				"SMAB-T3198: Validation that Improvement Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals(landValueSmall, landValueText,
				"SMAB-T3198: Validation that Land Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals("300,000", totalValueText,
				"SMAB-T3198: Validation that Total Value should be the total of land and improvement value.");

		// Step 7: User going to create new Assessed Value with the Assessed Value type
		// to verify the Land Cash Value and Improvement Cash Value

//		objParcelsPage.globalSearchRecords(parcelToSearch);
//
//		objParcelsPage.waitForElementToBeClickable(objParcelsPage.addNewAssessedValue, 8);
//		objParcelsPage.Click(objParcelsPage.addNewAssessedValue);
		
		driver.navigate().to("https://smcacre--qa.lightning.force.com/lightning/o/Assessed_BY_Values__c/new?count=1");
		objParcelsPage.searchAndSelectOptionFromDropDown(objParcelsPage.apn, parcelToSearch);
		String landCashValueNumber = "200,000";
		String improvementCashValueNumber = "100,000";
		objParcelsPage.enter(objParcelsPage.landCashValue, landCashValueNumber);
		objParcelsPage.enter(objParcelsPage.improvementCashValue, improvementCashValueNumber);
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Assessed Value");

		objParcelsPage.javascriptClick(objParcelsPage.saveButton);
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.landValue, 8);
		String landCashValueText = objParcelsPage.landValue.getText();
		String improvementCashValueText = objParcelsPage.improvementValue.getText();
		String totalCashValueText = objParcelsPage.totalValue.getText();
		softAssert.assertEquals(improvementCashValueNumber, improvementCashValueText,
				"SMAB-T3198: Validation that Improvement Value should be the improvement Cash Value.");
		softAssert.assertEquals(landCashValueNumber, landCashValueText,
				"SMAB-T3198: Validation that Land Value should be the Land Cash Value.");
		softAssert.assertEquals("300,000", totalCashValueText,
				"SMAB-T3198: Validation that Total Value should be the total of land cash Value and improvement cash value.");

	}

	/**
	 * This method is to Verify the fields mentioned on 'Assessed value' object on
	 * the Parcel.
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3198:Verify the UI validations for the fields mentioned on 'Assessed value' detail page object on the Parcel.Land and improvement values should not be visible", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement" })
	public void ParcelManagement_VerifyAssessedValuesObjectUIValidationsforAssessedValueType(String loginUser)
			throws Exception {

		// Fetching the Active Parcel
		String query ="SELECT name from Parcel__c where Status__c='Active' Limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String parcelToSearch = response.get("Name").get(0);

		// Step1: Login to the APAS application using the credentials passed through
		objParcelsPage.login(loginUser);

		// Step2: Opening the Parcels module
		objParcelsPage.searchModule(modules.PARCELS);

		// Step3: Search and Open the Parcel then clicks on more tab and then clicks on
		// Assessed Values
		objParcelsPage.globalSearchRecords(parcelToSearch);
		objWorkItemHomePage.openTab(objWorkItemHomePage.Tab_Details);
		String landValueText = objWorkItemHomePage.getFieldValueFromAPAS("Land", "Summary Values");
		String improvementValueText = objWorkItemHomePage.getFieldValueFromAPAS("Improvements", "Summary Values");
		softAssert.assertEquals("", landValueText, "SMAB-T3198: Validation that Land Value should not be visible.");
		softAssert.assertEquals("", improvementValueText,
				"SMAB-T3198: Validation that Improvement Value should not be visible");

	}
}
