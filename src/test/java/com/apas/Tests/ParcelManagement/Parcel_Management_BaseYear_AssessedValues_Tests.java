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
import com.apas.Reports.ReportLogger;
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
	@Test(description = "SMAB-T3196,SMAB-T3184,SMAB-T3221, SMAB-T3198:Verify the UI validations for the fields mentioned on 'Assessed value' object on the Parcel.Parcel record should exist containing Assessed Value records.", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "BaseYearManagement" })
	public void ParcelManagement_VerifyAssessedValuesObjectUIValidations(String loginUser) throws Exception {

		// Fetching the Active Parcel
		String query = "SELECT name from Parcel__c where Status__c='Active' Limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		String parcelToSearch = response.get("Name").get(0);

		// Step1: Login to the APAS application using the credentials passed through
		objParcelsPage.login(loginUser);

		// Step2: Opening the Parcels module
		objParcelsPage.searchModule(modules.PARCELS);

		// Step3: Search and Open the Parcel then clicks on more tab and then clicks on Assessed Values

		objParcelsPage.globalSearchRecords(parcelToSearch);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.assessedValueLable);
		
		objParcelsPage.Click(objParcelsPage.getButtonWithText("New"));
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.landCashValue);
		objParcelsPage.enter(objParcelsPage.landCashValue, "400000");
		objParcelsPage.enter(objParcelsPage.improvementCashValue, "300000");

		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.returnElementXpathOnAVForm("2", "3")),
				"Full Cash Value", "SMAB-T3196,SMAB-T3184: Validation that  Full Cash Value text is visible.");

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Prop 19");

		objParcelsPage.waitForElementToBeVisible(objParcelsPage.returnElementXpathOnAVForm("2", "4"));
		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.returnElementXpathOnAVForm("2", "4")),
				"Difference", "SMAB-T3196,SMAB-T3184: Validation that  Difference text is visible for Prop 19");

		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.returnElementXpathOnAVForm("1", "2")), "Total",
				"SMAB-T3196,SMAB-T3184: Validation that  Total text is visible for Prop 19");

		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.returnElementXpathOnAVForm("1", "3")),
				"New Taxable Value",
				"SMAB-T3196,SMAB-T3184: Validation that  New Taxable Value text is visible for Prop 19");

		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.returnElementXpathOnAVForm("1", "7")),
				"Origin FCV", "SMAB-T3196,SMAB-T3184: Validation that  Origin FCV text is visible for Prop 19");

		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.returnElementXpathOnAVForm("1", "9")),
				"Combined FBYV and HPI",
				"SMAB-T3196,SMAB-T3184: Validation that  Combined FBYV and HPI text is visible for Prop 19");

		objParcelsPage.enter(objParcelsPage.originDov, "1/1/2021");
		objParcelsPage.enter(objParcelsPage.originLandValue, "112021");
		objParcelsPage.enter(objParcelsPage.originImprovementValue, "112021");
		objParcelsPage.enter(objParcelsPage.hpiValueAllowance, "112021");

		WebElement differenceValue = objParcelsPage.returnElementXpathOnAVForm("2", "4");
		WebElement newTaxableValueText = objParcelsPage.returnElementXpathOnAVForm("1", "3");
		WebElement combinedFbyvAndHpi = objParcelsPage.returnElementXpathOnAVForm("1", "9");

		// Asserting those fields shouldn't be visible on Prop 60

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Prop 60");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(difference Value) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxable Value Text) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are not visible when Assessed Type is not 'Prop 19'");

		// Asserting those fields shouldn't be visible on Prop 90

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Prop 90");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(difference Value) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxable Value Text) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are not visible when Assessed Type is not 'Prop 19'");

		// Asserting those fields shouldn't be visible on Prop 110

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Prop 110");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(difference Value) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxable Value Text) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are not visible when Assessed Type is not 'Prop 19'");

		// Asserting those fields shouldn't be visible on Temporary Value

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Temporary Value");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(difference Value) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxable Value Text) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are not visible when Assessed Type is not 'Prop 19'");

		// Asserting those fields shouldn't be visible on CIP

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "CIP");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(difference Value) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxable Value Text) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are not visible when Assessed Type is not 'Prop 19'");

		// Asserting those fields shouldn't be visible on Assessed Value

		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Assessed Value");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(objParcelsPage.objHpiValueAllowance)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (HPI Value Allowance) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(differenceValue)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields(difference Value) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(newTaxableValueText)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (newTaxable Value Text) are not visible when Assessed Type is not 'Prop 19'");
		softAssert.assertTrue(!(objMappingPage.verifyElementVisible(combinedFbyvAndHpi)),
				"SMAB-T3196,SMAB-T3184: Validation that all fields (Combined FBYV and HPI) are not visible when Assessed Type is not 'Prop 19'");
		ReportLogger.INFO("Verification has been completed for Prop 19 only fields ");

		//Validating when we use temporary Value as type and adding additional declines then it should display smallest value of land and improvement on detail page. 
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

		// Step5: Clicks on New button and validate first type of decline should be Calamity and additional decline should be Decline.
		
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

		// Step6: After clicking Save Button user is on new created assessed Value page. User is validating the land, improvement & total value here.
		
		String landValueText = objParcelsPage.returnElementXpathOnAVHeader("Land Value").getText();
		String improvementValueText = objParcelsPage.returnElementXpathOnAVHeader("Improvement Value").getText();
		String totalValueText = objParcelsPage.returnElementXpathOnAVHeader("Total Value").getText();
		objParcelsPage.waitForElementToBeVisible(objParcelsPage.moretab, 8);
	
		softAssert.assertEquals(improvementValueSmall, improvementValueText,
				"SMAB-T3198: Validation that Improvement Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals(landValueSmall, landValueText,
				"SMAB-T3198: Validation that Land Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals("300,000", totalValueText,
				"SMAB-T3198: Validation that Total Value should be the total of land and improvement value.");
		ReportLogger.INFO("Verification has been completed for Land Value, Improvement Value and Total Value fields ");

		// Step 7: User going to create new Assessed Value with the Assessed Value type to verify the Land Cash Value and Improvement Cash Value
		String executionEnv = "";
		
		if (System.getProperty("region").toUpperCase().equals("QA"))
			executionEnv = "qa";
		if (System.getProperty("region").toUpperCase().equals("E2E"))
			executionEnv = "e2e";
		if (System.getProperty("region").toUpperCase().equals("PREUAT"))
			executionEnv = "preuat";
		if (System.getProperty("region").toUpperCase().equals("STAGING"))
			executionEnv = "staging";
		
		driver.navigate().to("https://smcacre--"+executionEnv+".lightning.force.com/lightning/o/Assessed_BY_Values__c/new");
		objParcelsPage.waitForElementToBeClickable(objParcelsPage.apn);
		objParcelsPage.searchAndSelectOptionFromDropDown(objParcelsPage.apn, parcelToSearch);
		String landCashValueNumber = "200,000";
		String improvementCashValueNumber = "100,000";
		objParcelsPage.enter(objParcelsPage.landCashValue, landCashValueNumber);
		objParcelsPage.enter(objParcelsPage.improvementCashValue, improvementCashValueNumber);
		objParcelsPage.selectOptionFromDropDown(objParcelsPage.assessedValueType, "Assessed Value");

		objParcelsPage.javascriptClick(objParcelsPage.saveButton);
		Thread.sleep(2000);
//		Verify Land, Improvement& total value is available in header only not on detail page. 

		softAssert.assertEquals(improvementCashValueNumber, improvementValueText,
				"SMAB-T3198: Validation that Improvement Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals(landCashValueNumber, landValueText,
				"SMAB-T3198: Validation that Land Value should be the smallest value from Calamity or Decline.");
		softAssert.assertEquals("300,000", totalValueText,
				"SMAB-T3198: Validation that Total Value should be the total of land and improvement value.");
		ReportLogger.INFO("Verification has been completed for Land Value, Improvement Value and Total Value fields for Assessed Value Type");
		ReportLogger.INFO("All the validations completed successfully");

		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.detailPagelandValue),
				"SMAB-T3198: Validation that Land Value should not be visible.");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.detailPageImprovementValue),
				"SMAB-T3198: Validation that Improvement Value should not be visible.");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.detailPageTotalValue),
				"SMAB-T3198: Validation that Total Value should not be visible.");

		objParcelsPage.logout();

	}

}
