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
	@Test(description = "SMAB-T3196,SMAB-T3184,SMAB-T3221, SMAB-3198:Verify the UI validations for the fields mentioned on 'Assessed value' object on the Parcel.Parcel record should exist containing Assessed Value records.", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement","BaseYearManagement" })
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

		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.combinedFbyvAndHpi),
				"Combined FBYV and HPI",
				"SMAB-T3196,SMAB-T3184: Validation that  Combined FBYV and HPI text is visible for Prop 19");

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

		driver.navigate().refresh();
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.apn, 8);

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

		Thread.sleep(2000);

		// Step6: After clicking Save Button user is on new created assessed Value page.
		// User is validating the land, improvement & total value here.
		
		String firstPartValue="//div/div/one-record-home-flexipage2/forcegenerated-adg-rollup_component___force-generated__flexipage_-record-page___-assessed_-values_-lightning_-record_-page___-assessed_-b-y_-values__c___-v-i-e-w/forcegenerated-flexipage_assessed_values_lightning_record_page_assessed_by_values__c__view_js/record_flexipage-record-page-decorator/div[1]/records-record-layout-event-broker/slot/slot/flexipage-record-home-template-desktop2/div/div[1]/slot/slot/flexipage-component2/slot/records-lwc-highlights-panel/records-lwc-record-layout/forcegenerated-highlightspanel_assessed_by_values__c___012000000000000aaa___compact___view___recordlayout2/force-highlights2/div[1]/div[2]/slot/slot/force-highlights-details-item";
	String lastPartValue="/div/p[2]/slot/records-formula-output/slot/lightning-formatted-number";
	WebElement landValue= driver.findElement(By.xpath(firstPartValue+"[4]"+lastPartValue));
	WebElement improvementValue= driver.findElement(By.xpath(firstPartValue+"[5]"+lastPartValue));
	WebElement totalValue= driver.findElement(By.xpath(firstPartValue+"[6]"+lastPartValue));
		

		objParcelsPage.waitForElementToBeVisible(landValue, 8);

		String landValueText = landValue.getText();
		String improvementValueText = improvementValue.getText();
		String totalValueText = totalValue.getText();
		softAssert.assertEquals(improvementValueSmall, improvementValueText,
				"SMAB-T3198: Validation that Improvement Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals(landValueSmall, landValueText,
				"SMAB-T3198: Validation that Land Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals("300,000", totalValueText,
				"SMAB-T3198: Validation that Total Value should be the total of land and improvement value.");

		// Step 7: User going to create new Assessed Value with the Assessed Value type
		// to verify the Land Cash Value and Improvement Cash Value


		driver.navigate().to("https://smcacre--qa.lightning.force.com/lightning/o/Assessed_BY_Values__c/new?count=1");
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.apn);
		objParcelsPage.searchAndSelectOptionFromDropDown(objParcelsPage.apn, parcelToSearch);
		String landCashValueNumber = "200,000";
		String improvementCashValueNumber = "100,000";
		objParcelsPage.enter(objParcelsPage.landCashValue, landCashValueNumber);
		objParcelsPage.enter(objParcelsPage.improvementCashValue, improvementCashValueNumber);
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Assessed Value");

		objParcelsPage.javascriptClick(objParcelsPage.saveButton);
		Thread.sleep(2000);
//		Verify Land, Improvement& total value is avaialble in header only not on detail page. 
		
		softAssert.assertEquals(improvementValueSmall, improvementValueText,
				"SMAB-T3198: Validation that Improvement Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals(landValueSmall, landValueText,
				"SMAB-T3198: Validation that Land Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals("300,000", totalValueText,
				"SMAB-T3198: Validation that Total Value should be the total of land and improvement value.");

		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.detailPagelandValue),"SMAB-T3198: Validation that Land Value should not be visible.");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.detailPageImprovementValue),"SMAB-T3198: Validation that Improvement Value should not be visible.");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.detailPageTotalValue),"SMAB-T3198: Validation that Total Value should not be visible.");
		
		objParcelsPage.logout();

	}

	
}
