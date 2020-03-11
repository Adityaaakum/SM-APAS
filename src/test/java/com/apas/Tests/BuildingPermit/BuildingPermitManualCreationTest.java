package com.apas.Tests.BuildingPermit;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BuildingPermitManualCreationTest extends TestBase {

	private RemoteWebDriver driver;
	Page objPage;
	EFileImportTransactionsPage objEfileImportTransactionsPage;
	ApasGenericFunctions objApasGenericFunctions;
	BuildingPermitPage objBuildPermit;
	EFileImportPage objEfileHomePage;
	Util objUtil;
	SoftAssertion softAssert;
	Map<String, String> dataMap;
	
	@BeforeMethod
	public void beforeMethod(){
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objEfileImportTransactionsPage = new EFileImportTransactionsPage(driver);
		objEfileHomePage = new EFileImportPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}
		
	@AfterMethod
	public void afterMethod() throws IOException, InterruptedException{
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business admin and appraisal support in an array
	 **/
    @DataProvider(name = "loginUsers")
    public Object[][] dataProviderLoginUserMethod() {
        return new Object[][] { { users.BUSINESS_ADMIN }, { users.APPRAISAL_SUPPORT } };
    }

	/**
	 Below test case is used to validate the manual creation of building permit 
	 **/
	@Test(description = "Creating manual entry for building permit", groups = {"smoke","regression"}, priority = 2, enabled = true)
	public void bldngPrmtsCreateManualEntry() throws Exception {
		
		//Step1: Login to the APAS application using the business admin user
		objApasGenericFunctions.login(users.BUSINESS_ADMIN);
		
		//Step2: Opening the building permit module
		objApasGenericFunctions.searchModule(modules.BUILDING_PERMITS);
					
		//Step3: Enter the data to on manual create building permit screen
		String newManualEntryData = System.getProperty("user.dir") + testdata.BUILDING_PERMIT_NEW_MANUAL_ENTRY_DATA;
		dataMap = objUtil.generateMapFromDataFile(newManualEntryData);
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Opening manual entry pop and clicking save button without entring any data to validate error messages for mandatory fields.");
		objBuildPermit.openManualEntryForm();
		objBuildPermit.waitForManualEntryFormToLoad();
		objBuildPermit.saveManualEntry();
		
		
		List<String> errorsList = objBuildPermit.retrieveMandatoryFieldsValidationErrorMsgs();
		String expMsgInPopUpHeader = CONFIG.getProperty("expectedErrorMsgForAllMandatoryFieldsNewEntry");
		String actMsgInPopUpHeader = errorsList.get(0);
		softAssert.assertEquals(actMsgInPopUpHeader, expMsgInPopUpHeader, "SMAB-T418: Validating mandatory fields missing error in manual entry pop up's header.");		
		String expMsgForIndividualField = CONFIG.getProperty("expectedErrorMsgForIndividualMandatoryField");
		String actMsgForIndividualField = errorsList.get(1);	
		softAssert.assertEquals(expMsgForIndividualField, actMsgForIndividualField, "SMAB-T418: Validating mandatory fields missing error against individual fields");
		int fieldsCountInHeaderMsg = Integer.parseInt(errorsList.get(2));		
		int individualMsgsCount = Integer.parseInt(errorsList.get(3));
		softAssert.assertEquals(fieldsCountInHeaderMsg, individualMsgsCount, "SMAB-T418: Validating count of field names in header msg against count of individual error msgs");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating to ensure entry is not created when all mandatory fields filled and process is aborted without saving.");	
		objBuildPermit.enterManualEntryData(dataMap);
		objBuildPermit.abortManualEntry();
		boolean buildingPermitNotCreated = objBuildPermit.checkBuildingPermitOnGrid();
		softAssert.assertTrue(!buildingPermitNotCreated, "SMAB-T418: Validating whether manual entry successfully aborted without saving.");
		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Verifying whether manual entry gets saved and pop up window for new entry gets displayed when 'Save & New' option is selected.");
		objBuildPermit.openManualEntryForm();
		objBuildPermit.enterManualEntryData(dataMap);
		boolean isNewManualEntryPopUpDisplayed = objBuildPermit.saveManualEntryAndExit();
		softAssert.assertTrue(isNewManualEntryPopUpDisplayed, "SMAB-T418: Validating whether pop up for new entry is displayed.");
		
		boolean isLoaderVisible = objBuildPermit.checkAndHandlePageLoaderOnEntryCreation();
		if(isLoaderVisible) {
			ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating newly created manual entry successfully displayed on recently viewed grid.");
			boolean isEntryDisplayed = objBuildPermit.checkBuildingPermitOnGrid();
			softAssert.assertTrue(isEntryDisplayed, "SMAB-T418: Validating whether manual entry successfully created and displayed on recently viewed grid.");
		} else {
			ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating newly created manual entry successfully displayed on details page.");
			boolean isEntryDisplayed = objBuildPermit.checkBuildingPermitOnDetailsPage();
			softAssert.assertTrue(isEntryDisplayed, "SMAB-T418: Validating whether manual entry successfully created and displayed on details page.");	
		}
		
		softAssert.assertAll();
	}
}
