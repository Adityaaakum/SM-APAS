package com.apas.Tests.DisabledVeteran;

import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Reports.ReportLogger;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ValueAdjustmentsPage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.PageObjects.ParcelsPage;
import com.apas.generic.ApasGenericFunctions;


public class DisabledVeterans_ValueAdjustments_Test extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	ApasGenericFunctions apasGenericObj;
	ValueAdjustmentsPage vaPageObj;
	ExemptionsPage exemptionPageObj;
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath;
	ParcelsPage parcelObj;
	BuildingPermitPage objBuildingPermitPage;
	
	@BeforeMethod
	public void beforeMethod() throws Exception{
		

		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		apasGenericObj = new ApasGenericFunctions(driver);
		parcelObj=new ParcelsPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		vaPageObj=new ValueAdjustmentsPage(driver);
		exemptionPageObj=new ExemptionsPage(driver);
		objBuildingPermitPage= new BuildingPermitPage(driver);
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
	  }
	
	
	/**
	 Below test case is used to verify that no VA's are created for past dates
	 * @throws Exception 
	 **/
	@Test(description = "SMAB-T633:Verify Zero value adjustments record is created for historic Start Date and End Date", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteran" }, priority = 0, alwaysRun = true, enabled = true)
	public void verify_Disabledveteran_NoVACreationBeyondDefaultLimitDatesExemption(String loginUser) throws Exception{
		
		Map<String, String> noVAData = objUtil.generateMapFromJsonFile(exemptionFilePath, "noVAData");
			//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//Step3: Clicking on New button to create a New Exemption record
			objPage.Click(exemptionPageObj.newExemptionButton);
			
			//Step4: Exemption creation
			
			exemptionPageObj.createNewExemptionWithMandatoryData(noVAData);
			//step4: 
			/** Verifying corresponding VA's count
			 **/
			objPage.Click(vaPageObj.valueAdjustmentTab);
			objPage.waitForElementToBeVisible(vaPageObj.valueAdjustmentsCountLabel, 5);
			ReportLogger.INFO("verifying Actual VA count should be 0 Scenario");
			softAssert.assertEquals(vaPageObj.VAlist.size(), 0, "SMAB-T633: Verify Zero value adjustments record is created for historic Start Date and End Date");
			apasGenericObj.logout();
		}
	
	/**
	 Below test case is used to create a new Exemption record and verify VAR' created
	 * @throws Exception 
	 **/

	@Test(description = "SMAB-T1261:verify user is able to see new VAs Getting Created After Updating a Not Qualified Exemption To Qualified",  dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteran" }, priority = 1, alwaysRun = true, enabled = true)
	
	public void verify_Disabledveteran_VAsGetsCreatedAfterUpdatingNotQualifiedExemptionToQualified(String loginUser) throws Exception{
		Map<String, String> noVAData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NotQualifiedToQualifiedData");
		//Step1: Login to the APAS application using the credentials passed through data provider
		apasGenericObj.login(loginUser);
		
		//Step2: Opening the Exemption Module
		apasGenericObj.searchModule(EXEMPTIONS);
		
		//Step3: Clicking on New button to create a New Exemption record
		objPage.Click(exemptionPageObj.newExemptionButton);
		
		//Step4: Exemption creation
		
		String cretaedExemptionName=exemptionPageObj.createNewExemptionWithMandatoryData(noVAData);
		ReportLogger.INFO("New Exemption created with Qualification as Not Qualified::"+cretaedExemptionName);
		String maxDate=DateUtil.determineMaxDate(exemptionPageObj.dateAquiredPropertyExemptionDetails.getText(),exemptionPageObj.dateOccupyPropertyExemptionDetails.getText(),exemptionPageObj.effectiveDateOfUSDVAExemptionDetails.getText());
		String currentDate=DateUtil.getDateInRequiredFormat(java.time.LocalDate.now().toString(),"yyyy-MM-dd","MM/dd/yyyy");
		String currentRollYear=ExemptionsPage.determineRollYear(currentDate);
		
		
		//step5: 
		/** Verifying corresponding VA's count should be 0
		 **/
		objPage.Click(vaPageObj.valueAdjustmentTab);
		objPage.waitForElementToBeVisible(vaPageObj.valueAdjustmentsCountLabel, 5);
		ReportLogger.INFO("verifying Actual VA count should be 0 as Qualification is Not Qualified");
		softAssert.assertEquals(vaPageObj.VAlist.size(), 0, "SMAB-1261: Verifying Zero value adjustments record is created for Not Qualified Exemption");
		
		/** step6: updating qualification to Qualified and
		Verifying corresponding VA's count should be as per the dates
		 **/
		
		
		objPage.Click(exemptionPageObj.editExemption);
		apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, "Qualified");
		apasGenericObj.selectFromDropDown(exemptionPageObj.reasonNotQualified, "--None--");
		objPage.Click(ExemptionsPage.saveButton);
		ReportLogger.INFO("Updated Qualification from Not Qualified to Qualified ");
		apasGenericObj.waitForElementToDisappear(vaPageObj.editVAPopUp, 10);
		objPage.Click(exemptionPageObj.exemptionDetailsTab);
		objPage.waitForElementToBeVisible(exemptionPageObj.dateApplicationReceivedExemptionDetails, 5);
		softAssert.assertEquals(exemptionPageObj.qualificationOnDetailPage.getText().trim(),"Qualified" , "SMAB-1261:Qualification is updated to Qualified");
		ReportLogger.INFO("verifying actual VA count after Qualification is updated to Qualified ");
		int actualVAtoBeCreated=vaPageObj.verifyValueAdjustments(maxDate, null, currentRollYear);
		ReportLogger.INFO("actual VA to be cretaed after updating Qualification to Qualified should be:"+actualVAtoBeCreated);
		softAssert.assertEquals(vaPageObj.VAlist.size(), actualVAtoBeCreated, "SMAB-T1261:Verify when the Exemption Qualification? is changed from Not Qualified to Qualified then new Value Adjustment records are created");
		apasGenericObj.logout();
		
	}
	
	
	
	/**
	 Below test case is used to create a new Exemption record and verify VAR' created
	 * @throws Exception 
	 **/

	@Test(description = "SMAB-T473,SMAB-T479,SMAB-T524,SMAB-T1222,SMAB-T1280:Verify User is able to see correct number VA's after creating an Exemption",  dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteran" }, priority = 2, alwaysRun = true, enabled = true)
	public void verify_Disabledveteran_createExemptionRecordAndVerifyAllVAAreBasicByDefault(String loginUser) throws Exception{
		Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NewExemptionCreation");
			//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//Step3: create a New Exemption record
			objPage.Click(exemptionPageObj.newExemptionButton);
			
			exemptionPageObj.createNewExemptionWithMandatoryData(newExemptionData);
			//step4: determining all dates
			String endDateOfrating=exemptionPageObj.endDateOfRatingOnExemption.getText();
			String maxDate=DateUtil.determineMaxDate(exemptionPageObj.dateAquiredPropertyExemptionDetails.getText(),exemptionPageObj.dateOccupyPropertyExemptionDetails.getText(),exemptionPageObj.effectiveDateOfUSDVAExemptionDetails.getText());
			String currentDate=DateUtil.getDateInRequiredFormat(java.time.LocalDate.now().toString(),"yyyy-MM-dd","MM/dd/yyyy");
			String currentRollYear=ExemptionsPage.determineRollYear(currentDate);
			
			//step5:
			/** Verifying corresponding VA's count
			 **/
			int actualVAtoBeCreated=vaPageObj.verifyValueAdjustments(maxDate, endDateOfrating, currentRollYear);
			ReportLogger.INFO("actual VA to be cretaed as per data should be:"+actualVAtoBeCreated);
			objPage.Click(vaPageObj.valueAdjustmentTab);
			objPage.waitForElementToBeClickable(vaPageObj.viewAllLink, 10);
			softAssert.assertEquals(vaPageObj.VAlist.size(), actualVAtoBeCreated, "SMAB-T473,SMAB-T531,SMAB-T555,SMAB-T583,SMAB-T1222,SMAB-T1280: user is able to view at least last 8 years of Exemption Limits records");
			
			//step6:code for verifying all VA's are basic
			int basicVAs=vaPageObj.fetchVACountBasedOnParameters("Determination","Basic Disabled Veterans Exemption");
			softAssert.assertEquals(actualVAtoBeCreated, basicVAs, "SMAB-T516:Verify that all the 'Value Adjustment Records' created for an Exemption Records are of type Basic Disabled Veteran Exemption");
			
			apasGenericObj.logout();
	}
	

	
	/**
	 Below test case is used to verify that on entering End date of Rating future VA's are deactivated,deleted and new is created
	 **/
	@Test(description = "SMAB-T601,SMAB-T602,SMAB-T485,SMAB-T486,SMAB-T1281: Verify future dated VA's are DEACTIVATED with Status Not Active when end date of rating is entered",  dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteran" }, priority = 3, alwaysRun = true, enabled = true)
	public void verify_Disabledveteran_UpdatingEndDateOfRating_DeletesFutureVA_CreatesNewVA_DeActivesVA(String loginUser) throws Exception	{
		Map<String, String> endDateOfRatingData = objUtil.generateMapFromJsonFile(exemptionFilePath, "newExemptionMandatoryData1");
		
			//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
			apasGenericObj.login(loginUser);
			//Step2: Opening the parcels module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//Step3: create a New Exemption record
			objPage.Click(exemptionPageObj.newExemptionButton);
			exemptionPageObj.createNewExemptionWithMandatoryData(endDateOfRatingData);
			
			//ste4:Verify New exemption will always have status as active if Qualification is Qualified
			softAssert.assertEquals(exemptionPageObj.exemationStatusOnDetails.getText().trim(), "Active", "SMAB-T499:Verify New exemption will always have status as active if Qualification is Qualified");
			
			//Step 5: verifying value adjustments count as per System date
						
			String maxDate=DateUtil.determineMaxDate(exemptionPageObj.dateAquiredPropertyExemptionDetails.getText(),exemptionPageObj.dateOccupyPropertyExemptionDetails.getText(),
			exemptionPageObj.effectiveDateOfUSDVAExemptionDetails.getText());
			String currentDate=DateUtil.getDateInRequiredFormat(java.time.LocalDate.now().toString(),"yyyy-MM-dd","MM/dd/yyyy");
			String currentRollYear=ExemptionsPage.determineRollYear(currentDate);
			
			
			//step6:
			/** Verifying corresponding VA's count
			 **/
			
			objPage.Click(vaPageObj.valueAdjustmentTab);
			objPage.waitForElementToBeClickable(vaPageObj.viewAllLink, 10);
			int vaCreatedBasedOnDates=vaPageObj.verifyValueAdjustments(maxDate,null,currentRollYear);
			ReportLogger.INFO("Actual va's created as per enterd data ::"+vaCreatedBasedOnDates);
		
			//Step7: now entering end date of rating
			objPage.Click(exemptionPageObj.exemptionDetailsTab);
			objPage.waitForElementToBeClickable(exemptionPageObj.dateApplicationReceivedExemptionDetails, 10);
			objPage.Click(exemptionPageObj.editExemption);
			objPage.enter(exemptionPageObj.endDateOfRating, endDateOfRatingData.get("EnddateOfRating")); 
			apasGenericObj.selectFromDropDown(exemptionPageObj.endRatingReason, endDateOfRatingData.get("EndRatingReason"));
			objPage.Click(ExemptionsPage.saveButton);
			apasGenericObj.waitForElementToDisappear(vaPageObj.editVAPopUp, 10);
			ReportLogger.INFO("Added End date OF Rating::"+endDateOfRatingData.get("EnddateOfRating"));
			softAssert.assertEquals(exemptionPageObj.exemationStatusOnDetails.getText().trim(),"Inactive", "SMAB-T601:---Verify user can terminate the exemption by entering end date of rating(new created)");
			String endDateOfRating=exemptionPageObj.endDateOfRatingOnExemption.getText().trim();
			
			//step 8: verifying deletedVA, InactiveVA and newly cretaed VA
			ReportLogger.INFO("Verifying VA for current Roll year should be deleted after entering end date of rating");
			objPage.Click(vaPageObj.valueAdjustmentTab);
			objPage.waitForElementToBeVisible(vaPageObj.viewAllLink, 10);
			ReportLogger.INFO("Verifying Actual and Expected count of VA after end date of rating Actual::"+vaPageObj.VAlist.size()+"|| Expected::"+(vaCreatedBasedOnDates-1));
			softAssert.assertEquals(vaPageObj.VAlist.size(),vaCreatedBasedOnDates-1, "SMAB-T485,SMBA-T602,SMAB-T1281:---Verify user can terminate the exemption by entering end date of rating(Future VA deleted)");
			ReportLogger.INFO("Verifying remaining Active and Deactivated Value Adjustment records as per end date of rating");
			int expectedActiveVA=vaPageObj.verifyValueAdjustments(maxDate,endDateOfRating,currentRollYear);//1/14/2012 & 6/30/2018
			softAssert.assertEquals(vaPageObj.fetchVACountBasedOnParameters("Status","Active"), expectedActiveVA, "SMAB-T485,SMBA-T602,SMAB-T1281:---Verify user can terminate the exemption by entering end date of rating(Active VA)");
			softAssert.assertEquals(vaPageObj.fetchVACountBasedOnParameters("Status","Not Active"),(vaPageObj.VAlist.size()-expectedActiveVA), "SMAB-T486,SMAB-T1281:---Verify future dated Value Adjustment records are DEACTIVATED with Status Not Active when end date of rating is entered");
			String newCreatedVA=vaPageObj.findVANameBasedOnEndDate(endDateOfRating);
			ReportLogger.INFO("newly created VA as per End date oF Ratintg::"+newCreatedVA);

			apasGenericObj.logout();
				
	}
	
	
	
	/**
	 Below test case is used to verify that only one VA is created for current roll year
	 **/
	
	
	@Test(description = "SMAB-T562:Verify only one VA(basic Disabled veteran)is created for Current Roll", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","DisabledVeteran" }, priority = 4, alwaysRun = true, enabled = true)
	public void verify_DisabledVeteran_OnlyOneVAForCurrentRollyear(String loginUser) throws Exception{
		
		Map<String, String> newExemptionData = objUtil.generateMapFromJsonFile(exemptionFilePath, "onlyOneVAtestData");
		//Step1: Login to the APAS application using the credentials passed through data provider
		apasGenericObj.login(loginUser);
		
		//Step2: Opening the Exemption Module
		apasGenericObj.searchModule(EXEMPTIONS);
		
		//Step3: Clicking on New button to create a New Exemption record
		objPage.Click(exemptionPageObj.newExemptionButton);

		//Step4: creating an Exemption creation
		
		exemptionPageObj.createNewExemption(newExemptionData);
	
	//step5:
	/**
	 * Validation No Penalty for any of the VA's as Application is submitted before grace end date 
	 */
	objPage.Click(vaPageObj.valueAdjustmentTab);
	objPage.waitForElementToBeVisible(vaPageObj.viewAllLink, 10);
	softAssert.assertEquals(vaPageObj.VAlist.size(), 1,"SMAB-T562:Verify only one VA(basic Disabled veteran)is created for Current Roll");
	softAssert.assertEquals(vaPageObj.fetchVACountBasedOnParameters("Determination","Basic Disabled Veterans Exemption"), 1,"SMAB-T562:Verify only one VA(basic Disabled veteran)is created for Current Roll");
	softAssert.assertEquals(vaPageObj.fetchVACountBasedOnParameters("Status","Active"), 1, "SMAB-T485,SMBA-T602:---Verify user can terminate the exemption by entering end date of rating(Active VA)");
	
	apasGenericObj.logout();
	}
	
	
	/**
	 * verify no penalty is applied if application is submitted before grace end date
	 **/
	

	@Test(description = "SMAB-T1276:Verify No penalty is applied to all VA's if application is submitted before Grace end date", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
		"smoke", "regression","DisabledVeteran" }, priority = 5, alwaysRun = true, enabled = true)
public void verify_Disabledveteran_NoPenlatyIfApplicationSubmittedBeforeGraceEndDate(String loginUser) throws Exception{
		Map<String, String> newExemptionMandatoryData = objUtil.generateMapFromJsonFile(exemptionFilePath, "NoPenaltyData");
		//Step1: Login to the APAS application using the credentials passed through data provider
		apasGenericObj.login(loginUser);
		
		//Step2: Opening the Exemption Module
		apasGenericObj.searchModule(EXEMPTIONS);
		
		//Step3: Clicking on New button to create a New Exemption record
		objPage.Click(exemptionPageObj.newExemptionButton);

		//Step4: creating an Exemption creation
		
	exemptionPageObj.createNewExemptionWithMandatoryData(newExemptionMandatoryData);
	//step5:
	/**
	 * Validation No Penalty for any of the VA's as Application is submitted before grace end date 
	 */
	objPage.Click(vaPageObj.valueAdjustmentTab);
	objPage.waitForElementToBeVisible(vaPageObj.viewAllLink, 10);
	String penaltyPercentageString;
	double penaltyPercentage;
	double penaltyAmtUI;
	for(int i=0;i<vaPageObj.VAlist.size();i++)
	{
		ReportLogger.INFO("Verifying Penalty percentage for VA::"+vaPageObj.VAlist.get(i).getText().trim());
		objPage.javascriptClick(vaPageObj.VAlist.get(i));
		objPage.waitForElementToBeClickable(vaPageObj.vaPenaltyPercentage, 10);
		penaltyPercentageString=vaPageObj.vaPenaltyPercentage.getText().trim();
		penaltyPercentage=Double.parseDouble(penaltyPercentageString.substring(0, penaltyPercentageString.indexOf(".")));
		penaltyAmtUI=vaPageObj.converToDouble(vaPageObj.vaPenaltyAmountCalculated.getText().trim());
		softAssert.assertEquals(penaltyPercentage, 0.0, "SMAB-T1276:Verify late penalty(No Penalty%) is not applied when the 'Application Received Date' is less than or equal to Grace End date");
		softAssert.assertEquals(penaltyAmtUI, 0.0, "SMAB-T1276:Verify late penalty(No Penalty amount) is not applied when the 'Application Received Date' is less than or equal to Grace End date");
		driver.navigate().back();
	}
	apasGenericObj.logout();
	
}


	/**
	 Below test case is used to verify Penalty percentage and Penalty amounts for VA's
	 * @throws Exception 
	 **/
	@Test(description = "SMAB-T1276,,SMAB-T1375,SMAB-T581,SMAB-T1277:Verify correct penlaty percenatge is applied to all VA's", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteran" }, priority = 6, alwaysRun = true, enabled = true)
	public void verify_Disabledveteran_PenlatyPercentageForAllVAs(String loginUser) throws Exception{
			Map<String, String> newExemptionMandatoryData = objUtil.generateMapFromJsonFile(exemptionFilePath, "newExemptionMandatoryData");
			//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//Step3: Clicking on New button to create a New Exemption record
			objPage.Click(exemptionPageObj.newExemptionButton);

			//Step4: creating an Exemption creation
			
		exemptionPageObj.createNewExemptionWithMandatoryData(newExemptionMandatoryData);
		
		String applicationDate=exemptionPageObj.dateApplicationReceivedExemptionDetails.getText().trim();//5/13/2018
		String graceEndDate=exemptionPageObj.graceEndDateExemptionDetails.getText().trim();//1/1/2015
		
		//ste5:
		/**
		 * Validation VA's are created with correct Start Date and End Date and correct Penalty Percentage is applied to all VA's 
		 */
		objPage.Click(vaPageObj.valueAdjustmentTab);
		objPage.waitForElementToBeClickable(vaPageObj.viewAllLink, 5);
		ReportLogger.INFO("Verifying Penalty percentage applied correctly to all VA's");
		boolean initialFilingLowIncomePenaltyVerified=false;
		boolean annualFilingLowIncomePenaltyVerified=false;
		for(int i=0;i<vaPageObj.VAlist.size();i++){
			ReportLogger.INFO("Verifying Penalty percentage for VA::"+vaPageObj.VAlist.get(i).getText().trim());
			objPage.javascriptClick(vaPageObj.VAlist.get(i));
			objPage.waitForElementToBeClickable(vaPageObj.vaPenaltyPercentage, 10);
			String penaltyPercentageString=vaPageObj.vaPenaltyPercentage.getText().trim();
			double penaltyPercentage=Double.parseDouble(penaltyPercentageString.substring(0, penaltyPercentageString.indexOf(".")));
			double penaltyAmount=vaPageObj.converToDouble(vaPageObj.vaPenaltyAmountCalculated.getText().trim());
			String dateBeforeAppdate=DateUtil.getFutureORPastDate(applicationDate, -5, "MM/dd/yyyy");
			String dateAfterAppdate=DateUtil.getFutureORPastDate(applicationDate, 1, "MM/dd/yyyy");
			String determination=vaPageObj.vaDetermination.getText().trim();
			
			boolean vaType=vaPageObj.verifyIfInitialFilingOrAnnualVA(applicationDate);
			softAssert.assertEquals(vaType,vaPageObj.vaInitialFilingFlag.isSelected(), "SMAB-T1365,SMAB-1387:Verify user is able to view 'Is Initial Filing Exemption?' field on all Value Adjustment records after creating an Exemption record");
			double penaltyPercentageCalculated=vaPageObj.calculatePenaltyPercentageForVA(vaType,applicationDate, graceEndDate,determination);
			softAssert.assertEquals(penaltyPercentage,penaltyPercentageCalculated, "SMAB-T1375,SMAB-T1290,SMAB-T1277:Verify user is able to view correct 'Penalty Percentage' and 'Penalty Amount calculated' for 'Initial Filling Basic Exemption' and 'Initial Filling Low income' VA's");
			double penaltyAmountCalculated=vaPageObj.calculatePenaltyAmountForVA(penaltyPercentageCalculated,vaType,determination);
			softAssert.assertEquals(penaltyAmount,penaltyAmountCalculated, "SMAB-T1375,SMAB-T1290,SMAB-T1277:Verify user is able to view correct 'Penalty Percentage' and 'Penalty Amount calculated' for 'Initial Filling Basic Exemption' and 'Initial Filling Low income' VA's");
	
			if((!initialFilingLowIncomePenaltyVerified && vaType) || (!annualFilingLowIncomePenaltyVerified && !vaType))
			{
				//verifying Annual form received date should not be less than Application received date
				ReportLogger.INFO("Verifying Annual Form Received date can not be less than Application received date");	
				objPage.Click(vaPageObj.editButton);
				objPage.waitForElementToBeClickable(vaPageObj.vaEditDeterminationDropDown, 10);
				apasGenericObj.selectFromDropDown(vaPageObj.vaEditDeterminationDropDown,"Low-Income Disabled Veterans Exemption");
				objPage.enter(vaPageObj.vaAnnualFormReceiveddate,dateBeforeAppdate);
				objPage.enter(vaPageObj.vaTotalAnuualHouseholdIncome,"10000");
				objPage.Click(ExemptionsPage.saveButton);
				softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Annual Form Received Date"),"Annual Form Received Date should not be greater than today or less than Exemption's Date Application Received","SMAB-T1291:Verified that Annual Form Received date can't be less than Application Received date");
				objPage.enter(vaPageObj.vaAnnualFormReceiveddate,dateAfterAppdate);
				objPage.Click(ExemptionsPage.saveButton);
				apasGenericObj.waitForElementToDisappear(vaPageObj.editVAPopUp, 10);
				String determination1=vaPageObj.vaDetermination.getText().trim();
				String AnnFormRcvddate=vaPageObj.annualFormReceivedDateOnUI.getText().trim();
				String penaltyPercentageLowIncomeString=vaPageObj.vaPenaltyPercentage.getText().trim();
				double penaltyPercentageLowIncome=Double.parseDouble(penaltyPercentageLowIncomeString.substring(0, penaltyPercentageLowIncomeString.indexOf(".")));
				double penaltyAmountLowIncome=vaPageObj.converToDouble(vaPageObj.vaPenaltyAmountCalculated.getText().trim());
				double penaltyPercentageCalculatedForLowIncomeVA=vaPageObj.calculatePenaltyPercentageForVA(vaType,AnnFormRcvddate, graceEndDate,determination1);
				softAssert.assertEquals(penaltyPercentageLowIncome,penaltyPercentageCalculatedForLowIncomeVA, "SMAB-T1375,SMAB-T1290,SMAB-T1277:Verify user is able to view correct 'Penalty Percentage' and 'Penalty Amount calculated' for 'Initial Filling Basic Exemption' and 'Initial Filling Low income' VA's");
				double penaltyAmountCalculatedLowIncomeVA=vaPageObj.calculatePenaltyAmountForVA(penaltyPercentageCalculatedForLowIncomeVA,vaType,determination1);
				softAssert.assertEquals(penaltyAmountLowIncome,penaltyAmountCalculatedLowIncomeVA, "SMAB-T1375,SMAB-T1290,SMAB-T1277:Verify user is able to view correct 'Penalty Percentage' and 'Penalty Amount calculated' for 'Initial Filling Basic Exemption' and 'Initial Filling Low income' VA's");
				if(vaType){initialFilingLowIncomePenaltyVerified=true;}
				else{annualFilingLowIncomePenaltyVerified=true;}
				
			}
			driver.navigate().back();
		}
		apasGenericObj.logout();
	}

}
		
		
	
	