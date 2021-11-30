package com.apas.Tests.Exemption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.apas.PageObjects.*;
import com.apas.Utils.DateUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Reports.ReportLogger;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.Utils.SalesforceAPI;

public class Exemption_Institutional_Tests extends TestBase implements testdata, modules, users{

	private RemoteWebDriver driver;
	Page objPage = null;
	LoginPage objLoginPage = null;
	ValueAdjustmentsPage vaPageObj;
	ExemptionsPage exemptionPageObj;
	RealPropertySettingsLibrariesPage objRPSLPage;
	SalesforceAPI salesforceAPI;
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath="";
	ParcelsPage parcelObj;
	ValueAdjustmentsPage ObjValueAdjustmentPage;

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{

		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		vaPageObj=new ValueAdjustmentsPage(driver);
		exemptionPageObj=new ExemptionsPage(driver);
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		objRPSLPage = new RealPropertySettingsLibrariesPage(driver);
		parcelObj=new ParcelsPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
		salesforceAPI = new SalesforceAPI();
		ObjValueAdjustmentPage = new ValueAdjustmentsPage(driver);

	}

	// Below test case is used to validate permission access on Exemption and VA's
	@Test(description = "SMAB-T483,SMAB-T482,SMAB-T476,SMAB-T477: Verify User without permission is not able to create a new Exemption, VA record and RPSL",  dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {"Regression","InstitutionalExemption"})
	public void InstitutionalExemption__mandatoryfieldValidation(String loginInvalidUser) throws Exception {
		Map<String, String> exemptionndata = objUtil.generateMapFromJsonFile(exemptionFilePath, "InstitutionalExemptionData");
		
		//Fetching the exemption record from API
		
		//Step1: Login to the APAS application using the credentials passed through data provider
		exemptionPageObj.login(loginInvalidUser);

		//Step3: Verifying user is not able to see New and Edit button for creating/Editing Exemption record
		ReportLogger.INFO("Verifying user is not able to see New and Edit button for creating/Editing Exemption record");
		exemptionPageObj.searchModule(EXEMPTIONS);
		softAssert.assertTrue(!objPage.verifyElementVisible(exemptionPageObj.newExemptionButton), "SMAB-T483: User is not able to see New button to create a new Exemption record");
		exemptionPageObj.createInstitutionalExemption(exemptionndata);
		ReportLogger.INFO("Step 4: Click on the TAB Value Adjustments");
		objPage.javascriptClick(ObjValueAdjustmentPage.valueAdjustmentTab);
		ReportLogger.INFO("Step 5: Click on the link View All");
		objPage.waitForElementToBeVisible(ObjValueAdjustmentPage.viewAllLink, 10);
		objPage.javascriptClick(ObjValueAdjustmentPage.viewAllLink);
			//Logging out of the application
		exemptionPageObj.logout();

	}

	

}
		
		
	
	