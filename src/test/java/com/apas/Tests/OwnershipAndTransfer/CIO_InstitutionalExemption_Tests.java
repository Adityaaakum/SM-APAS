package com.apas.Tests.OwnershipAndTransfer;

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

public class CIO_InstitutionalExemption_Tests extends TestBase implements testdata, modules, users{

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

	// Below test case is used to validate fields on Institutional Exemption and VA's
	@Test(description = "SMAB-T3973,SMAB-T4284,SMAB-T4265: Verify Fields on institutional exemption and related Value adjustment tab",  dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {"Regression","InstitutionalExemption","Exemption"})
	public void InstitutionalExemption__VerifFieldsOnExemptionAndVAs(String loginInvalidUser) throws Exception {
		Map<String, String> exemptionndata = objUtil.generateMapFromJsonFile(exemptionFilePath, "InstitutionalExemptionData");		
		
		//Step1: Login to the APAS application using the credentials passed through data provider
		exemptionPageObj.login(loginInvalidUser);

		//Step2: creating new institutional exemption record
		ReportLogger.INFO(" creating new institutional Exemption record");
		exemptionPageObj.searchModule(EXEMPTIONS);
		exemptionPageObj.createInstitutionalExemption(exemptionndata);
		//Step3: Verify fields on institutional record
	    softAssert.assertTrue(exemptionPageObj.verifyElementVisible(exemptionPageObj.exemptionCode), "SMAB-T3973: Verify that exemption code is present in Institutional Exemption");
	    softAssert.assertTrue(exemptionPageObj.verifyElementVisible(exemptionPageObj.Penalty), "SMAB-T3973: Verify that penalty field is present in Institutional Exemption");
	    softAssert.assertTrue(exemptionPageObj.verifyElementVisible(exemptionPageObj.filingStatus), "SMAB-T3973: Verify that Filing Status is present in Institutional Exemption");
        
	    //Step4: Creating new Value Adjustments
		ReportLogger.INFO("Step 4: Creating new Value Adjustments");
		objPage.javascriptClick(ObjValueAdjustmentPage.valueAdjustmentTab);
		exemptionPageObj.createNewVAsOnInstitutionalExemption();
		
		//Step5: Validating fields and data on Created VAs
	    softAssert.assertTrue(exemptionPageObj.verifyElementVisible(exemptionPageObj.propertySqFtProrated), "SMAB-T4284: Verify that Property Sq Ft Prorated % is present in Institutional Exemption");
		softAssert.assertEquals(exemptionPageObj.getFieldValueFromAPAS(exemptionPageObj.Remark), "User adjusted exemption amount is 2000.", "");	
		softAssert.assertEquals(exemptionPageObj.getFieldValueFromAPAS(exemptionPageObj.penaltyPercentage), "20.00%", "SMAB-T4284,SMAB-T4265: Verify that The penalties % is manually entered in the Exemptions Details Page which then flows to the Value Adjustment Page .");	
		softAssert.assertEquals(exemptionPageObj.getFieldValueFromAPAS(exemptionPageObj.ExemptionAmountUserAdjusted), "$2,000.00", "SMAB-T4265: Verify user adjueted amount is populated in user adjueted exemption amount field.");	
		softAssert.assertEquals(exemptionPageObj.getFieldValueFromAPAS(exemptionPageObj.netExemptionAmount), "$2,000.00", "SMAB-T4265: Verify user adjueted amount is populated in net exemption amount field.");	

		// Step6: Logging out of the application		
		exemptionPageObj.logout();

	}

	

}
		
		
	
	