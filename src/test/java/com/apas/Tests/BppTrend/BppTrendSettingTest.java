package com.apas.Tests.BppTrend;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class BppTrendSettingTest  extends TestBase {
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBppTrnPg;
	BuildingPermitPage objBuildPermit;
	Util objUtil;
	SoftAssertion softAssert;
	
	@BeforeMethod
	public void beforeMethod() {
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objBppTrnPg = new BppTrendPage(driver);
		objBuildPermit = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
	}

	@AfterMethod
	public void afterMethod() throws Exception {
		objApasGenericFunctions.logout();
	}
	
	@Test(description = "Create bpp trend setup entry", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, priority = 0, enabled = false)
	public void verifyBppTrendCreateTrendSetupEntry(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		
		//Step3: Click New button on the grid to open form / pop up to create new BPP Trend Setup
		objBuildPermit.openNewForm();
		
		//Step4: Click save button without entering mandatory details
		objBppTrnPg.Click(objBuildPermit.saveButton);
		
		//Step5: Validating the error messages in header section & against mandatory fields
		List<String> errorsList = objBuildPermit.retrieveMandatoryFieldsValidationErrorMsgs();
		String expMsgInPopUpHeader = CONFIG.getProperty("expErrorMsgInHeaderForBppTrendSetupFields");
		String actMsgInPopUpHeader = errorsList.get(0);
		softAssert.assertEquals(actMsgInPopUpHeader, expMsgInPopUpHeader, "SMAB-T190: Validating mandatory fields missing error in manual entry pop up's header.");		
		String expMsgForIndividualField = CONFIG.getProperty("expErrorMsgForIndividualMandatoryField");
		String actMsgForIndividualField = errorsList.get(1);	
		softAssert.assertEquals(expMsgForIndividualField, actMsgForIndividualField, "SMAB-T190: Validating mandatory fields missing error against individual fields");
		int fieldsCountInHeaderMsg = Integer.parseInt(errorsList.get(2));		
		int individualMsgsCount = Integer.parseInt(errorsList.get(3));
		softAssert.assertEquals(fieldsCountInHeaderMsg, individualMsgsCount, "SMAB-T190: Validating count of field names in header msg against count of individual error msgs");

		//Step6: Generating a data map from data file
		String bppTrendSetupData = System.getProperty("user.dir") + testdata.BPP_TREND_DATA;
		Map<String, String> bppTrendSetupDataMap = objUtil.generateMapFromJsonFile(bppTrendSetupData, "DataToCreateBppTrendSetup");
		
		//Step7: Entering bpp trend setup name the valid details and clicking save button to create BPP Trend Setup
		String bppTrendSetupName = bppTrendSetupDataMap.get("Roll Year") + CONFIG.getProperty("bppTrendSetupNameSuffix");
		objBppTrnPg.enter(objBppTrnPg.bppTrendSetupName, bppTrendSetupName);
		
		//Step8: Entering bpp trend roll year and tables status
		objBppTrnPg.enterBppTrendSettingRollYearAndTableStatus(bppTrendSetupDataMap);
		
		//Step9: Clicking save button to complete the creation of BPP Trend Setup
		objBppTrnPg.Click(objBuildPermit.saveAndNewButton);

		//Step10: Validating newly created bpp trend setup on details page
		ExtentTestManager.getTest().log(LogStatus.INFO, "Info: Validating newly created manual entry successfully displayed on details page.");
		objBppTrnPg.Click(objBuildPermit.closeEntryPopUp);
		boolean isBppSettingCreated = objBuildPermit.checkManualPermitEntryOnDetailsPage(bppTrendSetupName);
		softAssert.assertTrue(isBppSettingCreated, "SMAB-T418: BPP trend setup created successfully and reflecting on details page");
	}

	@Test(description = "SMAB-T229: Check for availability of input factor tables on bpp trend roll year screen", groups = {"smoke","regression"}, dataProvider = "loginBusinessAdminAndPrincipalUserAndBppAuditor", dataProviderClass = DataProviders.class, priority = 1, enabled = false)
	public void verifyBppTrendCreateAndValidateInputFactorTables(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
		String rollYear = CONFIG.getProperty("rollYear");
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		//Step4: Clicking on bpp property index factor tab and validating whether its table is visible
		objBppTrnPg.Click(objBppTrnPg.bppProperyIndexFactorsTab);
		boolean isPropertyIndexFactorTableVisible = objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.bppProperyIndexFactorsTableSection, 20).isDisplayed();
		softAssert.assertTrue(isPropertyIndexFactorTableVisible, "SMAB-T229: BPP propery index factors table is visible on roll year details page");

		//Step5: Clicking on bpp property good factor tab and validating whether its table is visible
		objBppTrnPg.Click(objBppTrnPg.bppProperyGoodFactorsTab);
		boolean isPercentGoodsFactorTableVisible = objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.bppPercentGoodFactorsTableSection, 20).isDisplayed();
		softAssert.assertTrue(isPercentGoodsFactorTableVisible, "SMAB-T229: BPP propery good factors table is visible on roll year details page");
		
		//Step6: Clicking more tab & then clicking on bpp valuation factor tab option & validating whether its table is visible
		objBppTrnPg.Click(objBppTrnPg.moreTabRightSection);
		objBppTrnPg.Click(objBppTrnPg.dropDownOptionBppImportedValuationFactors);
		boolean isValuationFactorsTableVisible = objBppTrnPg.waitForElementToBeVisible(objBppTrnPg.bppImportedValuationFactorsTableSection, 20).isDisplayed();
		softAssert.assertTrue(isValuationFactorsTableVisible, "SMAB-T229: BPP valuation factors table is visible on roll year details page");
	}
	
	@Test(description = "SMAB-T133,SMAB-T134: Create a new bpp setting and editing it with various values", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 2, enabled = true)
	public void verifyBppTrendCreateAndEditBppSetting(String loginUser) throws Exception {		
		//Step1: Login to the APAS application using the given user
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user: " + loginUser);
		objApasGenericFunctions.login(loginUser);

		//Step2: Generating a data map from data file
		String bppTrendSetupData = System.getProperty("user.dir") + testdata.BPP_TREND_DATA;
		Map<String, String> bppTrendSettingDataMap = objUtil.generateMapFromJsonFile(bppTrendSetupData, "DataToCreateBppTrendSetting");
		
		//Step3: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step4: Clicking on the roll year name in grid to navigate to details page of selected roll year
		String rollYear = bppTrendSettingDataMap.get("BPP Trend Roll Year");
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);
		
		//Step5: Move & click on BPP Setting drop down icon
		Thread.sleep(3000);
		String bppSettingCountBeforeCreatingNewSetting = objBppTrnPg.getCountOfBppSettings();
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppSetting));

		//Step6: Click on New option to create BPP Setting entry
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBppTrendSettingLink));
		
		//Step7: Validate error message with factor values less than minimum range
		String expectedErrorMsgOnIncorrectFactorValue;
		String actualErrorMsgOnIncorrectFactorValue;
		List<String> multipleFactorIncorrectVauesList = Arrays.asList(CONFIG.getProperty("FactorValuesLessThanMinRange").split(","));
		for(int i = 0; i < multipleFactorIncorrectVauesList.size(); i++) {
			objBppTrnPg.enterFactorValue(multipleFactorIncorrectVauesList.get(i));
			objBppTrnPg.Click(objBuildPermit.saveButton);
			
			if(i == 0) {
				Thread.sleep(3000);
				String factorValueInTxtBox = objBppTrnPg.getElementText(objBppTrnPg.factorTxtBox);
				softAssert.assertTrue(!(factorValueInTxtBox.equals(multipleFactorIncorrectVauesList.get(i))), "SMAB-T134: User is not allowed to enter letters. "
						+ "Value entered: "+ multipleFactorIncorrectVauesList.get(i) +". Value allowed to enter: "+ factorValueInTxtBox);
			}
			
			expectedErrorMsgOnIncorrectFactorValue = CONFIG.getProperty("ErrorMsgOnFactorValueLessThanMinRange");
			actualErrorMsgOnIncorrectFactorValue = objBppTrnPg.errorMsgOnIncorrectFactorValue();
			boolean isErrorMsgDislayed = actualErrorMsgOnIncorrectFactorValue.equals(expectedErrorMsgOnIncorrectFactorValue);
			softAssert.assertTrue(isErrorMsgDislayed, "SMAB-T134: Error message displayed: '" + actualErrorMsgOnIncorrectFactorValue + "' for factor value less than mumimum range.");
		}
		
		//Step8: Validate error message with factor values greater than maximum range
		String FactorValueGreaterThanMaxRange = CONFIG.getProperty("FactorValueGreaterThanMaxRange");
		objBppTrnPg.enterFactorValue(FactorValueGreaterThanMaxRange);
		objBppTrnPg.Click(objBuildPermit.saveButton);
		
		expectedErrorMsgOnIncorrectFactorValue = CONFIG.getProperty("ErrorMsgOnFactorValueGreaterThanMaxRange");
		actualErrorMsgOnIncorrectFactorValue = objBppTrnPg.errorMsgOnIncorrectFactorValue();
		boolean isErrorMsgDislayed = actualErrorMsgOnIncorrectFactorValue.contains(expectedErrorMsgOnIncorrectFactorValue);
		softAssert.assertTrue(isErrorMsgDislayed, "SMAB-T134: Error message displayed: '" + actualErrorMsgOnIncorrectFactorValue + "' for  factor value greater than minimum range.");
		
		//Step9: Close the currently opened bpp setting entry pop up
		objBppTrnPg.Click(objBuildPermit.closeEntryPopUp);
		
		//Step10: Create and Edit bpp setting entry with factor values within specified range
		List<String> multipleFactorCorrectVauesList = Arrays.asList(CONFIG.getProperty("FactorValuesWithinRange").split(","));
		for(int i = 0; i < multipleFactorCorrectVauesList.size(); i++) {
			Thread.sleep(4000);
			if(i == 0) {
				//Creating the BPP setting entry
				objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppSetting));
				objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBppTrendSettingLink));
				objBppTrnPg.enterRollYearInBppSettingDetails(rollYear);	
				objBppTrnPg.enterFactorValue(multipleFactorCorrectVauesList.get(i));
				objBppTrnPg.Click(objBuildPermit.saveButton);
			} else {
				//Retrieving equipment index factor value before performing edit operation
				String factorValueBeforeEdit = objBppTrnPg.getElementText(objBppTrnPg.locateElement("//div[text() = 'Maximum Equipment index Factor:']/following-sibling::div//span", 30));
				factorValueBeforeEdit = factorValueBeforeEdit.substring(0, factorValueBeforeEdit.length()-1);
				
				//Editing the BPP newly created setting entry				
				objBppTrnPg.clickAction(objBppTrnPg.dropDownIconDetailsSection);
				Thread.sleep(2000);
				objBppTrnPg.clickAction(objBuildPermit.editLinkUnderShowMore);
				objBppTrnPg.enterFactorValue(multipleFactorCorrectVauesList.get(i));
				objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermit.saveBtnEditPopUp));
				
				//Retrieving equipment index factor value after performing edit operation
				Thread.sleep(2000);
				String factorValueAfterEdit = objBppTrnPg.getElementText(objBppTrnPg.locateElement("//div[text() = 'Maximum Equipment index Factor:']/following-sibling::div//span", 30));
				factorValueAfterEdit = factorValueAfterEdit.substring(0, factorValueAfterEdit.length()-1);
				
				//Validation for checking whether updated values are reflecting or not
				softAssert.assertTrue(!(factorValueAfterEdit.equals(factorValueBeforeEdit)), "SMAB-T133: Maximum equipment index factor successfully updated & reflecting in right panel. Value before edit: "+ factorValueBeforeEdit +" || Value after edit: "+ factorValueAfterEdit);
			}
		}
		
		//Step11: Validating the count of BPP Setting before creating new bpp setting
		String bppSettingCountAfterCreatingNewSetting = objBppTrnPg.getCountOfBppSettings();
		softAssert.assertTrue(!(bppSettingCountAfterCreatingNewSetting.equals(bppSettingCountBeforeCreatingNewSetting)), "SMAB-T133: BPP trend setting successfully created & reflecting in right panel. Bpp setting count before creating new setting: "+ bppSettingCountBeforeCreatingNewSetting +" || Bpp setting count after creating and editing new setting: "+ bppSettingCountAfterCreatingNewSetting);

		//Step12: Retrieving the name of newly created bpp setting entry
		String xpathNewBppSetting = "//span[contains(text(), 'BPP Settings')]//ancestor::div[contains(@class, 'forceRelatedListCardHeader')]//following-sibling::div//h3//a";
		String bppSettingName = objBppTrnPg.getElementText(objBppTrnPg.locateElement(xpathNewBppSetting, 30));
		
		//Step13: Click ViewAll link to navigate to bpp settings grid and edit existing bpp setting entry
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.viewAllBppSettings));
		Thread.sleep(5000);
		
		//Step14: Retrieving the equipment index factor value before editing
		String xpathForFactorValueInGrid = "//tbody/tr//th//a[text() = '"+ bppSettingName +"']//parent::span//parent::th//following-sibling::td//span[contains(text(), '%')]";
		String factorValueDisplayedBeforeEditing = objBppTrnPg.getElementText(objBppTrnPg.locateElement(xpathForFactorValueInGrid, 30));

		//Step15: Editing and updating the equipment index factor value
		String factorValue = bppTrendSettingDataMap.get("Maximum Equipment index Factor");
		objBuildPermit.clickShowMoreLinkOnRecentlyViewedGrid(bppSettingName);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBuildPermit.editLinkUnderShowMore));
		
		int updatedFactorValue = Integer.parseInt(factorValue) + 1;
		objBppTrnPg.enterFactorValue(Integer.toString(updatedFactorValue));
		objBppTrnPg.Click(objBppTrnPg.waitForElementToBeClickable(objBuildPermit.saveBtnEditPopUp));

		//Step16: Retrieving and validating the equipment index factor value after editing
		Thread.sleep(2000);
		String factorValueDisplayedAfterEditing = objBppTrnPg.getElementText(objBppTrnPg.locateElement(xpathForFactorValueInGrid, 30));
		softAssert.assertTrue(!(factorValueDisplayedAfterEditing.equals(factorValueDisplayedBeforeEditing)), "SMAB-T133: Validation to check equipment index updated with new value. Factor value in grid before editing: "+ factorValueDisplayedBeforeEditing +" || Factor value in grid after editing: "+ factorValueDisplayedAfterEditing);
	}
	
	@Test(description = "Create a new bpp compsote factor setting", dataProvider = "loginBusinessAdmin", dataProviderClass = DataProviders.class, priority = 3, enabled = true)
	public void verifyBppTrendCreateBppCompositeFactorSetting(String loginUser) throws Exception {		
		//Step1: Navigating to Bpp Composite Factor Setting section and retrieve existing count of bpp composite factor setting
		//String bppTrendSetupName = System.getProperty("BppTrendSetupName");
		//String xpathBppSetupNameInBreadcrumb = "//span[text() = '"+ bppTrendSetupName +"']//parent::a[@class = 'forceBreadCrumbItem']";
		//objBppTrnPg.Click(objBppTrnPg.locateElement(xpathBppSetupNameInBreadcrumb, 20));
		
		//Step1: Generating a data map from data file
		String bppTrendSetupData = System.getProperty("user.dir") + testdata.BPP_TREND_DATA;
		Map<String, String> bppTrendSettingDataMap = objUtil.generateMapFromJsonFile(bppTrendSetupData, "DataToCreateBppTrendSetting");
		
		//Step2: Opening the BPP Trend module and set All as the view option in grid
		objApasGenericFunctions.searchModule(modules.BPP_TRENDS_SETUP);
		objApasGenericFunctions.selectAllOptionOnGrid();
		
		//Step3: Clicking on the roll year name in grid to navigate to details page of selected roll year
		String rollYear = bppTrendSettingDataMap.get("BPP Trend Roll Year");
		objBppTrnPg.clickBppTrendSetupRollYearNameInGrid(rollYear);

		//Step4: Clicking on the More Tab		
		Thread.sleep(5000);
		objBppTrnPg.clickAction(objBppTrnPg.moreTabRightSection);

		//Step5: Clicking on the Bpp Composite Factor Settings option
		Thread.sleep(2000);
		objBppTrnPg.clickAction(objBppTrnPg.bppCompositeFactorOption);
		String bppCompositeSettingCountBeforeCreatingNewSetting = objBppTrnPg.getCountOfBppCompositeFactorSettings();
		
		//Step6: Creating a new BPP Trend Commercial Setting Entry
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting));
		Thread.sleep(2000);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBppTrendSettingLink));
		Map<String, String> bppTrendCommercialSettingDataMap = objUtil.generateMapFromJsonFile(bppTrendSetupData, "DataToCreateBppTrendCommercialSetting");
		objBppTrnPg.enterRollYearInBppSettingDetails(rollYear);
		objBppTrnPg.enterFactorValue(bppTrendCommercialSettingDataMap.get("Minimum Good Factor"));
		objBppTrnPg.enterPropertyType(bppTrendCommercialSettingDataMap.get("Property Type"));
		objBppTrnPg.Click(objBuildPermit.saveButton);
		Thread.sleep(4000);
		
		//Step7: Creating a new BPP Trend Industrial Setting Entry
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting));
		Thread.sleep(2000);
		objBppTrnPg.clickAction(objBppTrnPg.newBppTrendSettingLink);
		Map<String, String> bppTrendIndustrialSettingDataMap = objUtil.generateMapFromJsonFile(bppTrendSetupData, "DataToCreateBppTrendIndustrialSetting");
		objBppTrnPg.enterRollYearInBppSettingDetails(rollYear);
		objBppTrnPg.enterFactorValue(bppTrendIndustrialSettingDataMap.get("Minimum Good Factor"));
		objBppTrnPg.enterPropertyType(bppTrendIndustrialSettingDataMap.get("Property Type"));
		objBppTrnPg.Click(objBuildPermit.saveButton);
		Thread.sleep(4000);
		
		//Step8: Creating a new BPP Trend Construction Setting Entry
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting));
		Thread.sleep(2000);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBppTrendSettingLink));
		Map<String, String> bppTrendConstructionSettingDataMap = objUtil.generateMapFromJsonFile(bppTrendSetupData, "DataToCreateBppTrendConstructionSetting");
		objBppTrnPg.enterRollYearInBppSettingDetails(rollYear);
		objBppTrnPg.enterFactorValue(bppTrendConstructionSettingDataMap.get("Minimum Good Factor"));
		objBppTrnPg.enterPropertyType(bppTrendConstructionSettingDataMap.get("Property Type"));
		objBppTrnPg.Click(objBuildPermit.saveButton);
		Thread.sleep(4000);
		
		//Step9: Creating a new BPP Trend Agricultural Setting Entry
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.dropDownIconBppCompFactorSetting));
		Thread.sleep(2000);
		objBppTrnPg.clickAction(objBppTrnPg.waitForElementToBeClickable(objBppTrnPg.newBppTrendSettingLink));
		Map<String, String> bppTrendAgriculturalSettingDataMap = objUtil.generateMapFromJsonFile(bppTrendSetupData, "DataToCreateBppTrendAgriculturalSetting");
		objBppTrnPg.enterRollYearInBppSettingDetails(rollYear);
		objBppTrnPg.enterFactorValue(bppTrendAgriculturalSettingDataMap.get("Minimum Good Factor"));
		objBppTrnPg.enterPropertyType(bppTrendAgriculturalSettingDataMap.get("Property Type"));
		objBppTrnPg.Click(objBuildPermit.saveButton);
		Thread.sleep(4000);
		
		//Step10: Validating the count of Bpp Composite Factor Setting post creating a new Entry
		String bppCompositeSettingCountAfterCreatingNewSetting = objBppTrnPg.getCountOfBppCompositeFactorSettings();
		softAssert.assertTrue(!(bppCompositeSettingCountAfterCreatingNewSetting.equals(bppCompositeSettingCountBeforeCreatingNewSetting)), "SMAB-T418: BPP trend composite setting successfully creaated & reflecting in right panel");
	}
}
