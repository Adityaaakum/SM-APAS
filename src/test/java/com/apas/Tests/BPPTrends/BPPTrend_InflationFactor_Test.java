package com.apas.Tests.BPPTrends;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;


public class BPPTrend_InflationFactor_Test extends TestBase {
	
	RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermit;
	Util objUtil;
	SoftAssertion softAssert;
	String rollYear;
	BuildingPermitPage objBuildPermitPage;
	ApasGenericPage objApasGenericPage;
	
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		
		if(driver==null) {
            setupTest();
            driver = BrowserDriver.getBrowserInstance();
        }
		
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		rollYear = CONFIG.getProperty("rollYear");
		objBuildPermitPage = new BuildingPermitPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
	}
	
	@AfterMethod
	public void afterMethod() throws Exception {
		//objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating that (Appraiser/Auditor) are not able to create inflation factors records:: TestCase/JIRA ID: SMAB-T210
	 * 2. Validating that (Appraiser/Auditor) are not able to edit inflation factors records:: TestCase/JIRA ID: SMAB-T210
	 */
	@Test(description = "SMAB-T210: Appraiser and Auditor users unable to create, edit the value of CPI factor and saves, approve table data", groups = {"smoke","regression","BPPTrend"}, dataProvider = "rpApprasierAndBPPAuditor", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_CreateAndEdit_InflationFactor_WithRestrictedUsers(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.CPI_FACTORS);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step3: Checking unavailability of new button on grid page
		softAssert.assertTrue(!objBppTrnPg.isElementAvailable(objBppTrnPg.newBtnViewAllPage, 10), "SMAB-T210: For User "+ loginUser +"-- New button is not visible on grid page");

		//Step4: Finding the first entry from the grid to perform edit operation on it
		if(objBppTrnPg.isElementAvailable(objBppTrnPg.firstEntryInGrid, 10)) {
			//Step5: Clicking show more icon and checking availability of edit link under it
			String cpiFactorToEdit = objBppTrnPg.getElementText(objBppTrnPg.firstEntryInGrid);			
			objBuildPermitPage.clickShowMoreLinkOnRecentlyViewedGrid(cpiFactorToEdit);

			//Step6: Checking unavailability of edit link under show more drop down on view all grid
			softAssert.assertTrue(!objBppTrnPg.isElementAvailable(objBuildPermit.editLinkUnderShowMore, 5), "SMAB-T210: For User "+ loginUser +"-- Edit link is not visible under show more option on grid");
			
			//Step7: Checking unavailability of edit button on details page
			objBppTrnPg.clickOnEntryNameInGrid(cpiFactorToEdit);
			softAssert.assertTrue(!objBppTrnPg.isElementAvailable(objBuildPermit.editBtnDetailsPage, 5), "SMAB-T210: For User "+ loginUser +"-- Edit button is not visible on details page");
		}
		else {
			softAssert.assertTrue(true, "SMAB-T210: For User "+ loginUser +"-- No records available for editing on grid or on details page");			
		}
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}

	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating that 'Inflation Factor' is >= 0 and <=1.02:: TestCase/JIRA ID: SMAB-T179,SMAB-T182
	 * 2. Validating that 'Year' is not older than 1975:: TestCase/JIRA ID: SMAB-T179
	 * 3. Validating that duplicate year records are not displayed:: TestCase/JIRA ID: SMAB-T179
	 * 4. Validate that user is able to create a new Inflation Factors for a year with valid data:: TestCase/JIRA ID: SMAB-T180
	 * 5. Validate the error message on adding entry for duplicate year:: TestCase/JIRA ID: SMAB-T181
	 */
	@Test(description = "SMAB-T179,SMAB-T180,SMAB-T181,SMAB-T182: Create new CPI Factor", groups = {"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_Create_CpiFactors(String loginUser) throws Exception {						
		//Step1: Retrieving a roll year from data base to create CPI Factor value for it
		String queryForRollYear = "Select Name FROM Roll_Year_Settings__c Where Status__c = 'open' Order By Roll_Year__c Desc LIMIT 1";
		HashMap<String, ArrayList<String>> dataFromApi = new SalesforceAPI().select(queryForRollYear);
		String rollYearForCpiFactor = dataFromApi.get("Name").get(0);
		
		//Step2: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step4: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.CPI_FACTORS);
		objApasGenericFunctions.selectAllOptionOnGrid();
		Thread.sleep(2000);
		
		//Step5: Adding all roll year elements into a list
		List<WebElement> rollYearsElementsList = objBppTrnPg.rollYears;
		
		//Step6: Adding up the roll year values into a set and a list
		Set<String> uniqueSetOfRollYears = new HashSet<String>();
		List<String> listOfRollYears = new ArrayList<String>();

		for(WebElement element: rollYearsElementsList) {
			String rollYearValue = objBppTrnPg.getElementText(element);
			uniqueSetOfRollYears.add(rollYearValue);
			listOfRollYears.add(rollYearValue);
		}
		
		//Step7: Iterating over the set to check year values are not older than 1975
		Iterator<String> itr = uniqueSetOfRollYears.iterator();
		boolean status = false;
		int falseCounter = 0;
		while(itr.hasNext()) {
			int rollYear = Integer.parseInt(itr.next());
			status = (rollYear >= 1975);
			if(!status) {
				falseCounter = falseCounter + 1;
				softAssert.assertTrue(status, "SMAB-T179: 'Year' value "+ rollYear +" for is older than Year 1975 in the grid");
			}
		}
		
		if(falseCounter == 0) {
			softAssert.assertTrue(status, "SMAB-T179: 'Year' value for all "+ listOfRollYears.size() +" records is greater than 1975");
		}
		
		//Step8: Checking the size of list and set containing roll year values to confirm roll year values are unique or duplicate
		if(uniqueSetOfRollYears.size() == listOfRollYears.size()) {
			//Validation to confirm that all roll year values are unique
			softAssert.assertTrue(true, "SMAB-T179: 'Year' value for all "+ listOfRollYears.size() +" records is unique (no duplicate year found)");
		} else {
			//Removing the roll year values from the list containing roll years
			itr = uniqueSetOfRollYears.iterator();
			while(itr.hasNext()) {
				listOfRollYears.remove(itr.next());
			}
					
			//Iterating over the list containing roll years to confirm duplicate values
			for(String rollYear: listOfRollYears) {
				//Validation to confirm that roll year values are duplicate
				softAssert.assertTrue(true, "SMAB-T179: Year value "+ rollYear +" is duplicate");	
			}
		}
		
		//Step9: Adding all CPI factor elements into a list
		List<WebElement> cpiFactorsElementsList = objBppTrnPg.cpiFactors;
		List<String> listOfCpiFactors = new ArrayList<String>();
		for(WebElement element: cpiFactorsElementsList) {
			String cpiFactorValue = objBppTrnPg.getElementText(element);
			listOfCpiFactors.add(cpiFactorValue);
		}
		
		//Step10: Iterating over the list to check CPI factor values are between 0 and 1.02
		status = false;
		falseCounter = 0;
		double cpiFactor;
		for(int i = 0; i < listOfCpiFactors.size(); i++) {
			cpiFactor = Double.parseDouble(listOfCpiFactors.get(i));
			status = (cpiFactor >= 0.0 && cpiFactor <= 1.02);
			if(!status) {
				falseCounter = falseCounter + 1;
				softAssert.assertTrue(status, "SMAB-T179: 'CPI Factor' value '"+ cpiFactor +"' for year "+ listOfRollYears.get(i) +" is not in the specified range");	
			}
		}
		
		if(falseCounter == 0) {
			softAssert.assertTrue(status, "SMAB-T179: 'CPI Factor' value for all "+ listOfCpiFactors.size() +" records are within specified range of 0 to 1.02");
		}
		
		//Step11: Click new button and enter valid value for roll year 
		objBppTrnPg.Click(objBuildPermit.newButton);
		objApasGenericPage.searchAndSelectOptionFromDropDown(objBppTrnPg.rollYearForCpiFactor, rollYear);
		
		//Step12: Entering CPI Factor value less than minimum range and clicking save button
		objBppTrnPg.enter(objBppTrnPg.cpiFactorInputBox, "-1");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		
		//Step13: Validating the error message on providing invalid CPI factor value and canceling the pop up window
		String expErrorMsgForLessThanMinValue = CONFIG.getProperty("errorMsgOnCpiFactorLessThanMinValue");
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.errorMsgForInvalidCpiFactorValue, 10);
		String actErrorMsgForLessThanMinValue = objBppTrnPg.getElementText(objBppTrnPg.errorMsgForInvalidCpiFactorValue);
		softAssert.assertContains(actErrorMsgForLessThanMinValue, expErrorMsgForLessThanMinValue, "SMAB-T181: Validation for CPI Factor value less than minimum range");
		softAssert.assertContains(actErrorMsgForLessThanMinValue, expErrorMsgForLessThanMinValue, "SMAB-T182: Validation for CPI Factor value less than minimum range");
		
		objBppTrnPg.Click(objBuildPermitPage.cancelButton);
		
		//Step14: Clicking new button again and entering CPI Factor value less than minimum range and clicking save button
		objBppTrnPg.Click(objBuildPermit.newButton);
		objApasGenericPage.searchAndSelectOptionFromDropDown(objBppTrnPg.rollYearForCpiFactor, rollYear);
		
		objBppTrnPg.enter(objBppTrnPg.cpiFactorInputBox, "1.22000");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		
		//Step15: Validating the error message on providing invalid CPI factor value and canceling the pop up
		String expErrorMsgForMoreThanMaxValue = CONFIG.getProperty("errorMsgOnCpiFactorMoreThanMaxValue");
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.errorMsgForInvalidCpiFactorValue, 10);
		String actErrorMsgForMoreThanMaxValue = objBppTrnPg.getElementText(objBppTrnPg.errorMsgForInvalidCpiFactorValue);
		softAssert.assertContains(actErrorMsgForMoreThanMaxValue, expErrorMsgForMoreThanMaxValue, "SMAB-T181: Validation for CPI Factor value more than maximum range");

		objBppTrnPg.Click(objBuildPermitPage.cancelButton);
		
		//Step16: Clicking new button to open pop up
		objBppTrnPg.Click(objBuildPermit.newButton);
		
		//Step17: Entering roll year and an existing value for selected roll year and clicking save button
		objApasGenericPage.searchAndSelectOptionFromDropDown(objBppTrnPg.rollYearForCpiFactor, rollYear);
		objBppTrnPg.enter(objBppTrnPg.cpiFactorInputBox, "1.02000");
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		
		//Step18: Validating error message for duplicate entry and clicking cancel button to close the pop up
		String expErrorMsgOnDuplicatEntry = CONFIG.getProperty("errorMsgOnDulicateCpiFactor");
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.errorMsgOnDuplicateCpiFactor, 10);
		String actErrorMsgOnDuplicatEntry = objBppTrnPg.getElementText(objBppTrnPg.errorMsgOnDuplicateCpiFactor);
		softAssert.assertContains(actErrorMsgOnDuplicatEntry, expErrorMsgOnDuplicatEntry, "SMAB-T181: Validation for duplicate entry of CPI Factor");
		
		objBppTrnPg.Click(objBuildPermitPage.cancelButton);
		
		//Step19: Clicking new button to open pop up
		objBppTrnPg.Click(objBuildPermit.newButton);
		
		//Step20: Entering valid details and clicking save button to create new CPI Factor entry
		objApasGenericPage.searchAndSelectOptionFromDropDown(objBppTrnPg.rollYearForCpiFactor, rollYearForCpiFactor);
		objBppTrnPg.enter(objBppTrnPg.cpiFactorInputBox, "0.00");
		
		objBppTrnPg.Click(objBuildPermitPage.saveButton);
		String actMsgInPopUpOnSave = objBppTrnPg.waitForPopUpMsg(10);
		boolean isRecordCreated = actMsgInPopUpOnSave.contains("was created");
		softAssert.assertTrue(isRecordCreated, "SMAB-T180: Validationg message on successfully creating CPI Factor entry. "+ actMsgInPopUpOnSave);
		
		int startIndex = actMsgInPopUpOnSave.indexOf('"') + 1;
		int endIndex = actMsgInPopUpOnSave.lastIndexOf('"');
		String newCpiFactorName = actMsgInPopUpOnSave.substring(startIndex, endIndex);

		String queryForCpiFactorID = "Select Id FROM CPI_Factor__c Where Name = '"+ newCpiFactorName +"'";
		new SalesforceAPI().delete("CPI_Factor__c", queryForCpiFactorID);
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * DESCRIPTION: Performing Following Validations::
	 * 1. Validating business administrator is not able to edit approved inflation factor:: TestCase/JIRA ID: SMAB-T183
	 */
	@Test(description = "SMAB-T183: Validating business administrator user is not able to edit the approved inflation factor", groups = {"smoke","regression","BPPTrend"}, dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class)
	public void verify_BppTrend_EditCpiFactors_ByRestrictedUser(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.CPI_FACTORS);
		objApasGenericFunctions.selectAllOptionOnGrid();
		Thread.sleep(2000);
		
		
		String cpiFactorToEdit = objBppTrnPg.getElementText(objBppTrnPg.firstEntryInGrid);

		//Step3: Retrieving CPI Factor to edit using roll year fetched in above step
		String queryForCpiFactorID = "Select Id, Status__c FROM CPI_Factor__c Where Name = '"+ cpiFactorToEdit +"'";
		HashMap<String, ArrayList<String>> cpiFactorData = new SalesforceAPI().select(queryForCpiFactorID);
		String cpiFactorID = cpiFactorData.get("Id").get(0);
		String cpiFactorCurrentStatus = cpiFactorData.get("Status__c").get(0).trim();
		
		new SalesforceAPI().update("CPI_Factor__c", cpiFactorID, "Status__c", "Approved");
		
		//Step5: Click the show more icon and clicking on edit button on grid
		driver.navigate().refresh();
		objBuildPermitPage.clickShowMoreLinkOnRecentlyViewedGrid(cpiFactorToEdit);
		objBppTrnPg.clickAction(objBuildPermitPage.editLinkUnderShowMore);
		
		//Step6: Validating the error message on clicking edit button
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.errorMsgOnEditClick, 10);
		String actualErrorMsg = objBppTrnPg.getElementText(objBppTrnPg.errorMsgOnEditClick);
		String expectedErrorMsg = CONFIG.getProperty("errorMsgOnEditingCpiFactorViaEditBtn");
		softAssert.assertContains(actualErrorMsg, expectedErrorMsg, "SMAB-T183: Validation for business admin is not able to edit aproved CPI Factor from grid");
		objBppTrnPg.Click(objBuildPermitPage.closeEntryPopUp);
		
		//Step7: Clicking on the CPI Factor name to navigate To details page
		objBppTrnPg.clickOnEntryNameInGrid(cpiFactorToEdit);
		objBppTrnPg.waitForElementToBeClickable(objBuildPermitPage.editButton, 10);
		
		//Step8: Clicking edit button on details page
		objBppTrnPg.Click(objBuildPermitPage.editButton);
		
		//Step9: Validating the error message on clicking edit button
		objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.errorMsgOnEditClick, 10);
		actualErrorMsg = objBppTrnPg.getElementText(objBppTrnPg.errorMsgOnEditClick);
		softAssert.assertContains(actualErrorMsg, expectedErrorMsg, "SMAB-T183: Validation for business admin is not able to edit aproved CPI Factor from details page");
		objBppTrnPg.Click(objBuildPermitPage.closeEntryPopUp);

		//Step10: Resetting the status of updated CPI Factor
		new SalesforceAPI().update("CPI_Factor__c", cpiFactorID, "Status__c", cpiFactorCurrentStatus);
		
		softAssert.assertAll();
		objApasGenericFunctions.logout();
	}
	
	
	
	
	
}