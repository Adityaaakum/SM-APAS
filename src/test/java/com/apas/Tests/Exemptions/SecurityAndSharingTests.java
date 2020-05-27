package com.apas.Tests.Exemptions;

import java.util.Calendar;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Reports.ExtentTestManager;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ValueAdjustmentsPage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.PageObjects.ParcelsPage;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
	
    //Value Adjustment, Security & Sharing and Penalty Calculation - All tests are in same class 
    //Couple of Test Cases for SMAB-1765 were failings (Incorrect Locators) - Fixed it
	//Existing methods aren't used. New ones are created - Replaced where it felt necessary
	//Redundant Extent Report's code - have cleaned it
	//Comments are not apt in Test files and Methods - Added comments in Tests for SMAB-1765
	//Redundant 'System.Out' statements present - For now commented
	//All test cases have 'Priority3' setup - Need to check with Yogi before fixing it (what was the thought process)
	
	//Not in sync with release1.1
	//No Data Provider
	//Test Description and other attributes is not proper in Test annotation
 	

public class SecurityAndSharingTests extends TestBase implements testdata, modules, users{

	private RemoteWebDriver driver;
	
	Page objPage = null;
	LoginPage objLoginPage = null;
	ApasGenericFunctions apasGenericObj;
	ValueAdjustmentsPage vaPageObj;
	ExemptionsPage exemptionPageObj;
	
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath="";
	ParcelsPage parcelObj;
	

	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		System.out.println("invoking Before method");
		driver = BrowserDriver.getBrowserInstance();
		vaPageObj=new ValueAdjustmentsPage(driver);
		exemptionPageObj=new ExemptionsPage(driver);
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		apasGenericObj = new ApasGenericFunctions(driver);
		parcelObj=new ParcelsPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
	    

	}
	/*@AfterMethod
	public void afterMethod() throws Exception {
		apasGenericObj.logout();
		Thread.sleep(3000);
	}
	*/

	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business Exemption Support Staff in an array
	 **/
	@DataProvider(name = "loginUsers")
	public Object[][] dataProviderLoginUserMethod() {
		return new Object[][] { { users.EXEMPTION_SUPPORT_STAFF } };
	}

	/**
	 Below test case is used to validate right side panel of Parcel
	 **/
	@Test(description = "verify Exemption,Value Adjustment, Building permit,Assesse,Situs, RPSL,Roll year right side section is same as parcel right side section",  dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, priority = 0, alwaysRun = true, enabled = true)
	public void verifyRightSideBarAsPerParcel(String loginUser)
	{
		
		try {
			
			Map<String, String> rightSidesectionsOnParcels = objUtil.generateMapFromJsonFile(exemptionFilePath, "verifyRightSideBarAsPerParcel");
			
			//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginUser);
			//Step2: Opening the exemption module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//Step3: selecting an Exemption record from the list
			boolean elementSelected=exemptionPageObj.checkForAndSelectRecordsInList();
			softAssert.assertEquals(elementSelected,true,"SMAB-T489:Verify user is able to navigate to an active Disabled Veterans Exemption record");
			//Step4:verifying right side bar tabs are present on Exemption object
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying Exemption and Value Adjustment object has same right side bar as Parcel object");
			int topSectionsExemption=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideSections",exemptionPageObj.rightSideTopSectionsOnUI);
			softAssert.assertEquals(topSectionsExemption, 4, "SMAB-T1169: Verification of right side top sections for Exemption object");
			//verifying new calendar and tasks section
			int bottomSetionExemption=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideBottomSections",exemptionPageObj.rightSideBottomSectionsOnUI);
			softAssert.assertEquals(bottomSetionExemption, 2, "SMAB-T1169: Verification of right side bottom sections for Exemption object");
		
			apasGenericObj.searchModule("Value Adjustments");
			exemptionPageObj.checkForAndSelectRecordsInList();

			//Step5:verifying right side bar tabs are present on value adjustment page
			int topSectoinsVA=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideSections",exemptionPageObj.rightSideTopSectionsOnUI);
			softAssert.assertEquals(topSectoinsVA, 4, "SMAB-T1169: Verification of right side top sections for VA object");
			//verifying new calendar and tasks section
			int bottomSectionVA=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideBottomSections",exemptionPageObj.rightSideBottomSectionsOnUI);
			softAssert.assertEquals(bottomSectionVA, 2, "SMAB-T1169: Verification of right side bottom sections for VA object");
			
			//Step6: verifying right side section for Building permit object
			ExtentTestManager.getTest().log(LogStatus.INFO, "verifying Building permit Details page has same right side sections as Parcel Details page ");
			apasGenericObj.searchModule(BUILDING_PERMITS);
			
			//selecting a record from the list
			exemptionPageObj.checkForAndSelectRecordsInList();
		
			//Step7:verifying right side tabs are present on BP object

			int topSectionsBP=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideSections",exemptionPageObj.rightSideTopSectionsOnUI);
			softAssert.assertEquals(topSectionsBP, 4, "SMAB-T1170: Verification of right side top sections for BP object");
			
			//verifying new calendar and tasks section
			int bottomSectionsBP=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideBottomSections",exemptionPageObj.rightSideBottomSectionsOnUI);
			softAssert.assertEquals(bottomSectionsBP, 2, "SMAB-T1170: Verification of right side bottom sections for BP object");
			
			
			//Step8:now verifying right side section for Assessees object
			ExtentTestManager.getTest().log(LogStatus.INFO, "verifying ASSESSEES Details page has same right side section as Parcel details page");
			apasGenericObj.searchModule(ASSESSEES);
			
			//selecting a record from the list
			exemptionPageObj.checkForAndSelectRecordsInList();
			int topSectionsAssessee=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideSections",exemptionPageObj.rightSideTopSectionsOnUI);
			softAssert.assertEquals(topSectionsAssessee, 4, "SMAB-T1171: Verification of right side top sections for Assessees object");
			//verifying new calendar and tasks section
			int bottomSectionAssessee=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideBottomSections",exemptionPageObj.rightSideBottomSectionsOnUI);
			softAssert.assertEquals(bottomSectionAssessee, 2, "SMAB-T1171: Verification of right side bottom sections for Assessees object");

			
			//step9:now verifying right side section for Situs object
			ExtentTestManager.getTest().log(LogStatus.INFO, "verifying right side sections of Situs details page is same as Parcel detail page");
			apasGenericObj.searchModule(SITUS);
			
			//selecting a record from the list
			exemptionPageObj.checkForAndSelectRecordsInList();
			
			//step10:verifying right side bar tabs are present on Situs object
			int topSectionsSitus=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideSections",exemptionPageObj.rightSideTopSectionsOnUI);
			softAssert.assertEquals(topSectionsSitus, 4, "SMAB-T1172: Verification of right side top sections for Situs object");
			//verifying new calendar and tasks section
			int bottomSectionSitus=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideBottomSections",exemptionPageObj.rightSideBottomSectionsOnUI);
			softAssert.assertEquals(bottomSectionSitus, 2, "SMAB-T1172: Verification of right side bottom sections for Situs object");

			
			//step11:now verifying right side section for RPSL object
			ExtentTestManager.getTest().log(LogStatus.INFO, "verifying right side sections of RPSL details page is same as Parcel detail page");
			apasGenericObj.searchModule(RPSL);
			//selecting a record from the list
			exemptionPageObj.checkForAndSelectRecordsInList();
			
			
			//verifying right side bar tabs are present on RPSL object
			int topSectionsRPSL=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideSections",exemptionPageObj.rightSideTopSectionsOnUI);
			softAssert.assertEquals(topSectionsRPSL, 4, "SMAB-T1172: Verification of right side top sections for RPSL object");
			//verifying new calendar and tasks section
			int bottomSectionRPSL=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideBottomSections",exemptionPageObj.rightSideBottomSectionsOnUI);
			softAssert.assertEquals(bottomSectionRPSL, 2, "SMAB-T1172: Verification of right side bottom sections for RPSL object");

			
			//step13:now verifying right side section for Roll Year object
			ExtentTestManager.getTest().log(LogStatus.INFO, "verifying right side sections of Roll Year details page is same as Parcel detail page");
			apasGenericObj.searchModule(ROLLYEAR);
			
			//selecting a record from the list
			exemptionPageObj.checkForAndSelectRecordsInList();
			//verifying right side bar tabs are present on Assessees object
			int topSectionsRollYear=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideSections",exemptionPageObj.rightSideTopSectionsOnUI);
			softAssert.assertEquals(topSectionsRollYear, 4, "SMAB-T1173: Verification of right side top sections for Roll Year object");
			//verifying new calendar and tasks section
			int bottomSectionRollYear=exemptionPageObj.verifySection(rightSidesectionsOnParcels,"rightSideBottomSections",exemptionPageObj.rightSideBottomSectionsOnUI);
			softAssert.assertEquals(bottomSectionRollYear, 2, "SMAB-T1173: Verification of right side bottom sections for Roll Year object");

			softAssert.assertAll();
			apasGenericObj.logout();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("error while validationg verifyRightSideBarAsPerParcel"+e.getMessage());
		}
	}
		
	

	
	
	@DataProvider(name = "loginInvalidUser")
	public Object[][] dataProviderLoginUserMethodForUser() {
		return new Object[][] { { users.RP_APPRAISER } };
	}
	
	// Below test case is used to validate permission access on Exemption and VA's
	
	@Test(description = "SMAB-T474: Verify User is able to create a VAR record",  dataProvider = "loginInvalidUser", groups = {
			"smoke", "regression" }, priority = 1, alwaysRun = true, enabled = true)
	public void invalidUserOnlyViewExemptionAndValueAdjustmentRecords(String loginInvalidUser)
	{
		
		try {
			
			//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginInvalidUser);
			//Step2: Opening the exemption module
			apasGenericObj.searchModule(EXEMPTIONS);
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying user is not able to see New and Edit button for creating/Editing exemption record");
			
			//Step3: Verifying new button not available for Rp Apprasier user
			softAssert.assertEquals(exemptionPageObj.isNotDisplayed("//a[@title='New']/div[@title='New'][1]"), false, "SMAB-T483: User is not able to see New button to create a new Exemption record");
			exemptionPageObj.checkForAndSelectRecordsInList();
			softAssert.assertEquals(exemptionPageObj.isNotDisplayed("//button[@name='Edit']"), false, "SMAB-T482: User is not able to edit/delete Exemption record");
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verified user is not able to see New and Edit button for creating/Editing exemption record");
			
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying user is not able to see New and Edit button for creating/Editing Value adjustments record");
			//objPage.Click(vaPageObj.valueAdjustmentTab);
			apasGenericObj.searchModule("Value Adjustments");
			exemptionPageObj.checkForAndSelectRecordsInList();
		
			softAssert.assertEquals(exemptionPageObj.isNotDisplayed("//button[@name='Edit']"), false, "SMAB-T476,SMAB-T477: User is not able to edit/delete VA record");
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verified user is not able to see New and Edit button for creating/Editing value adjustement record");
			
			softAssert.assertAll();
			apasGenericObj.logout();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	
}
		
		
	
	