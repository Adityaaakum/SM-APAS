package com.apas.Tests.DisabledVeteran;

import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
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
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;
	
    

public class DisabledVeterans_Exemption_Tests extends TestBase implements testdata, modules, users{

	private RemoteWebDriver driver;
	
	Page objPage;
	LoginPage objLoginPage;
	ApasGenericFunctions apasGenericObj;
	ValueAdjustmentsPage vaPageObj;
	ExemptionsPage exemptionPageObj;
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath;
	ApasGenericPage objApasGenericPage;
	BuildingPermitPage objBuildingPermitPage;
	

	@BeforeMethod
	public void beforeMethod() throws Exception{
		
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		System.out.println("invoking Before method");
		
		driver = BrowserDriver.getBrowserInstance();		
		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		apasGenericObj = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
		exemptionPageObj=new ExemptionsPage(driver);
		vaPageObj=new ValueAdjustmentsPage(driver);	
		objApasGenericPage= new ApasGenericPage(driver);
		objBuildingPermitPage=new BuildingPermitPage(driver);

	}
	
	
	
	/**
	 below test case is for business validations for Exemption fields
	 * @throws Exception 
	 */
	@Test(description = "SMAB-T488,SMAB-T491,SMAB-T492,SMAB-T493,SMAB-T495,SMAB-T496:Future dates Error Messages for date Fields",dataProvider="loginExemptionSupportStaff" ,dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteran" }, priority = 0, alwaysRun = true, enabled = true)
	public void verify_Disabledveteran_FutureDatesErrorMessagesWhileCreatingExemption(String loginUser) throws Exception
	{
		
			Map<String, String> fieldData = objUtil.generateMapFromJsonFile(exemptionFilePath, "BusinessValidationsForExemptionFields");
			
			String futureDate=DateUtil.getFutureORPastDate(java.time.LocalDate.now().toString(), 2, "yyyy-MM-dd");	        
			String endDateGreaterThanUSDVADate=DateUtil.getFutureORPastDate(java.time.LocalDate.now().toString(), 5, "yyyy-MM-dd");
			//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			objPage.Click(exemptionPageObj.newExemptionButton);
			
			//Step3: selecting mandatory details before verifying error message
			apasGenericObj.searchAndSelectFromDropDown(exemptionPageObj.apn,fieldData.get("APN"));
			apasGenericObj.searchAndSelectFromDropDown(exemptionPageObj.claimantName,fieldData.get("ClaimantName"));
			objPage.enter(exemptionPageObj.claimantSSN, fieldData.get("ClaimantSSN"));
			
			//step4:
			/**verifying 'Date Application Received','Date Acquire Property','Date of Death Veteran',
			 'Date occupied/Intend to occupy Property','Date Move From Prior Residence',
			 'Effective Date of 100% USDVA Rating','Date of Notice of 100% Rating', can not be future dates
			 **/
			ReportLogger.INFO("Verifying all entered date fields show future date error messages");
			objPage.enter(exemptionPageObj.dateApplicationReceived,futureDate);
			objPage.enter(exemptionPageObj.dateOfDeathOfVeteran,futureDate);
			objPage.enter(exemptionPageObj.veteranName, fieldData.get("VeteranName").concat(java.time.LocalDateTime.now().toString()));
			objPage.enter(exemptionPageObj.veteranSSN, fieldData.get("VeteranSSN"));
			objPage.enter(exemptionPageObj.dateAquiredProperty,futureDate);
			objPage.enter(exemptionPageObj.dateOccupyProperty,futureDate);
			objPage.enter(exemptionPageObj.effectiveDateOfUSDVA,futureDate);
			objPage.enter(exemptionPageObj.dateOfNotice,futureDate);
			apasGenericObj.selectMultipleValues(fieldData.get("BasisForClaim"), "Basis for Claim");
			objPage.enter(exemptionPageObj.endDateOfRating,endDateGreaterThanUSDVADate);
			apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, fieldData.get("Qualification"));
			objPage.Click(ExemptionsPage.saveButton);
			Thread.sleep(5000);
			//ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying all entered fields show future date error messages");
			
			String expected=" can't be a future date";
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Date Application Received"),"Date Application Received".concat(expected),"SMAB-T491: Verify Application Date can't be a Future Date");
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Date of Death of Veteran"),"Date of Death of Veteran".concat(expected),"SMAB-T494: Verify Date Of death of veteran Property can't be a Future Date");
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Date Acquired Property"),"Date Acquired Property".concat(expected),"SMAB-T492: Verify Date Acquired Property can't be a Future Date");
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Date Occupied/Intend to Occupy Property"),"Date Occupied/Intend to Occupy Property".concat(expected),"SMAB-T493: Verify Date Occupy Property can't be a Future Date");
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Effective Date of 100% USDVA Rating"),"Effective Date of 100% USDVA Rating".concat(expected),"SMAB-T496: Verify Effective Date of 100% USDVA Rating can't be a Future Date");
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Date of Notice of 100% Rating"),"Date of Notice of 100% Rating".concat(expected),"SMAB-T495: Verify Date of notice of 100% rating can't be a Future Date");
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("End Date of Rating"),"End Date of Rating".concat(expected),"SMAB-T488: Verify End Date of rating can't be a Future Date");
			objPage.Click(objApasGenericPage.crossButton);

			apasGenericObj.logout();
		
	}
	
	
	@Test(description = "SMAB-T501,SMAB-T502,SMAB-T503,SMAB-T497,SMAB-T498,SMAB-T1278,SMAB-T1122,SMAB-T1223,SMAB-T1263,SMAB-T1264,SMAB-T1221:business validations for Exemption fields", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteran" }, priority = 1, alwaysRun = true, enabled = true)
	public void verify_Disabledveteran_BusinessValidationsForExemptionFields(String loginUser) throws Exception
	{
			Map<String, String> businessValidationdata = objUtil.generateMapFromJsonFile(exemptionFilePath, "BusinessValidationsForExemptionFields");
			
			//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			objPage.Click(exemptionPageObj.newExemptionButton);
			
			//Step3: selecting mandatory details before verifying error message
			apasGenericObj.searchAndSelectFromDropDown(exemptionPageObj.apn,businessValidationdata.get("APN"));
			objPage.enter(exemptionPageObj.dateApplicationReceived,businessValidationdata.get("DateApplicationReceived"));
			apasGenericObj.searchAndSelectFromDropDown(exemptionPageObj.claimantName,businessValidationdata.get("ClaimantName"));
			objPage.enter(exemptionPageObj.claimantSSN, businessValidationdata.get("ClaimantSSN"));
			objPage.enter(exemptionPageObj.veteranName, businessValidationdata.get("VeteranName").concat(java.time.LocalDateTime.now().toString()));
			objPage.enter(exemptionPageObj.dateAquiredProperty,businessValidationdata.get("DateAquiredProperty"));
			objPage.enter(exemptionPageObj.dateOccupyProperty,businessValidationdata.get("DateOccupyProperty"));
			objPage.enter(exemptionPageObj.effectiveDateOfUSDVA,businessValidationdata.get("EffectiveDateOfUSDVA"));
			objPage.enter(exemptionPageObj.dateOfNotice,businessValidationdata.get("DateOfNotice"));
			apasGenericObj.selectMultipleValues(businessValidationdata.get("BasisForClaim"), "Basis for Claim");
			objPage.enter(exemptionPageObj.endDateOfRating,businessValidationdata.get("EnddateOfRating"));
			apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, businessValidationdata.get("Qualification"));
			objPage.Click(ExemptionsPage.saveButton);
		
			//step4:
			/**Verifying Veteran SSN is required if Veteran Name is mentioned
			**/
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying Veteran SSN is required if Veteran Name is mentioned");
			ReportLogger.INFO("Verifying Veteran SSN is required if Veteran Name is mentioned");
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.veteranSSNErrorMsg.getText(),"Veteran's SSN is required" ,"SMAB-T502:Verify Veteran SSN is required if Veteran Name is mentioned");
			
			//step5:
			/**verifying 'Effective Date of 100% USDVA Rating' can not be greater than 'Date of Notice of 100% Rating'
			 *and 'End Date of Rating'
			**/
			//ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying Effective Date of 100% USDVA Rating can't be greater than Date of Notice of 100% Rating");
			ReportLogger.INFO("Verifying Effective Date of 100% USDVA Rating can't be greater than Date of Notice of 100% Rating");
			
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("Date of Notice of 100% Rating"),"Date of Notice of 100% Rating cannot be less than the Effective Date of 100% USDVA Rating.","SMAB-T1278: Verify Date of Notice of 100% Rating cannot be less than the Effective Date of 100% USDVA Rating.");
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("End Rating Reason"),"End Rating Reason Is required","SMAB-T498: Verify End Rating Reason is required if End Date of rating is not BLANK");
			softAssert.assertEquals(objBuildingPermitPage.getIndividualFieldErrorMessage("End Date of Rating"),"End Date of Rating must be greater than Effective Date","SMAB-T497,SMAB-T1122: Verify End Date of rating must be greater than Effective Date of 100% USDVA Rating");
					
			
			//step6:
			/**verifying 'Date of Death of Veteran' and 'Deceased Veteran Qualification' are required when 'Unmarried_Spouse_of_Deceased_Veteran__c is 'Yes'
			**/
			//ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying validation when unmarriedSpouseOfDisabledVeteran is Yes");
			ReportLogger.INFO("Verifying validation when unmarriedSpouseOfDisabledVeteran is Yes");
			apasGenericObj.selectFromDropDown(exemptionPageObj.unmarriedSpouseOfDisabledVeteran, "Yes");
			objPage.Click(ExemptionsPage.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.dateOfDeathOfVeteranErrorMsg.getText().trim(),"Date of Death of Veteran is required","SMAB-T494:Verify 'Date of Death of Veteran' is required field");
			softAssert.assertEquals(exemptionPageObj.deceasedVeteranQualificationErrormsg.getText(), "Deceased Veteran Qualification is required","SMAB-T498:Verify Deceased Veteran Qualification is required when unmarriedSpouseOfDisabledVeteran is Yes");

			
		
			//step7:
			/**verifying When 'DV_Exemption_on_Prior_Residence__c is 'Yes' then Prior Residence Street Address, 
			 Prior Residence City,Prior Residence State,Prior Residence County, Date moved from Prior Residence 
			 are all mandatory
			**/
			
			//ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying validation for dvExemptionOnPriorResidence and realted fields");
			ReportLogger.INFO("Verifying validation for dvExemptionOnPriorResidence and realted fields");
			apasGenericObj.selectFromDropDown(exemptionPageObj.dvExemptionOnPriorResidence, "Yes");
			objPage.Click(ExemptionsPage.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.errorMessage.getText(),businessValidationdata.get("dvExemptionOnPriorResidenceYesErrorMsg"),"SMAB-T503:Verify Prior Residence Street Address, Prior Residence City,Prior Residence State,Prior Residence County, Date move from Prior Residence are required if 'DV Exemption on Prior Residence' is 'Yes'");
			
			//step8:
		/*
		  verifying Spouse SSN is required if Spouse Name is not blank
		*/ ReportLogger.INFO("verifying Spouse SSN is required if Spouse Name is not blank");
			objPage.enter(exemptionPageObj.spouseName, businessValidationdata.get("SpouseName"));
			objPage.Click(ExemptionsPage.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.spouseSSNErrorMsg.getText(),"Spouse's SSN is required","SMAB-T501:Verify Spouse SSN is required if Spouse Name is mentioned");			
			
			//step9:
			/**verifying when 'Qualification' is Not Qualified then 'Qualification Denial Detail' and 'Qualification Denial Reason' are mandatory
			 * End Date of Rating has to be blank when Qualification is set to Not Qualified.
			 **/
			//ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying validations when 'Qualification' is Not Qualified");
			ReportLogger.INFO("Verifying error messages when 'Qualification' is Not Qualified");
			objPage.enter(exemptionPageObj.endDateOfRating, "01/01/2020");
			apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, "Not Qualified");
			objPage.Click(ExemptionsPage.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.reasonForNotQualifiedErrorMsg.getText(), businessValidationdata.get("ReasonForNotQualifiedErrorMsg"), "SMAB-T1223:Verify When Exemption qualification is Not Qualified and the Exemption record is saved, then user is prompted to select a value for Qualification Denial Reason");
			softAssert.assertEquals(exemptionPageObj.enddateOfRatingErrorMsg.getText(),businessValidationdata.get("EnddateOfRatingErrorMsg1") , "SMAB-T1221:mandatory check for End Date of Rating");
			
			
			//step10:
			/**verifying when 'Qualification' is Not Qualified and 'Reason for not Qualified' is 'Other' then 'Not Qualified Detail' is mandatory
			 *
			 **/
			ReportLogger.INFO("verifying when 'Qualification' is Not Qualified and 'Reason for not Qualified' is 'Other' then 'Not Qualified Detail' is mandatory");
			apasGenericObj.selectFromDropDown(exemptionPageObj.reasonNotQualified, "Other");
			objPage.Click(ExemptionsPage.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.notQualifiedDetailErrorMsg.getText(), businessValidationdata.get("NotQualifiedDetailErrorMsg"), "SMAB-T1262:Verify When Exemption qualification is Not Qualified, Reason for Not Qualified is Other and the Exemption record is saved, then user is prompted to select a value for 'Not Qualified Detail'");
			
			
			//step11:
			/**verifying when 'Qualification' is Qualified and 'Reason for not Qualified' is Not Blank then user sees corresponding error message
			 **/
			ReportLogger.INFO("verifyin error message when 'Qualification' is Not Qualified and 'Reason for not Qualified' is Not Blank");		
			apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, "Qualified");
			objPage.Click(ExemptionsPage.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.reasonForNotQualifiedErrorMsg.getText(), "Reason for Not Qualified must be blank", "SMAB-T1263,SMAB-T1264:Verify When the Exemption qualification is Qualified,Reason for Not Qualified is not Blank and the Exemption record is saved, then user is prompted with error message");
			
			/*//step12:
			*//**verifying when 'Qualification' is Qualified and 'Reason for not Qualified' is Not Blank then user sees corresponding error message
			 **//*
			ReportLogger.INFO("verifyin error message when 'Qualification' is Not Qualified and 'Reason for not Qualified' is Not Blank");	
			apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, "Qualified");
			objPage.Click(exemptionPageObj.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.reasonForNotQualifiedErrorMsg.getText(), "Reason for Not Qualified must be blank", "SMAB-T1221,SMAB-T1263,SMAB-T1264:Verify When the Exemption qualification is Qualified,Reason for Not Qualified is not Blank and the Exemption record is saved, then user is prompted with error message");
			
		*/	
			
			objPage.Click(objApasGenericPage.crossButton);

			apasGenericObj.logout();
			}
	

	/**
	 Below test case is used to Edit an Exemption record
	 * @throws Exception 
	 **/
	@Test(description = "SMAB-T1282:Verify date fields are not editable once entered for an Exemption",dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression" ,"DisabledVeteran"}, priority = 2, alwaysRun = true, enabled = true)
	public void verify_Disabledveteran_DateFieldsNotEditableonCreatedExemptionDetailsPage(String loginUser) throws Exception
	{	Map<String, String> dataToEdit = objUtil.generateMapFromJsonFile(exemptionFilePath, "editExemptionData");
			
			//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
				
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//step3: creating an exemption record
			objPage.Click(exemptionPageObj.newExemptionButton);
			exemptionPageObj.createNewExemptionWithMandatoryData(dataToEdit);


			//Step4: Updating 'Date Acquired Property' to 1/11/2020
			ReportLogger.INFO("verifying Date Acquired Property can't be edited once record is saved");
			apasGenericObj.editAndInputFieldData("Date Acquired Property",exemptionPageObj.dateAcquiredProperty,dataToEdit.get("DateAquiredPropertyUpdated"));
			softAssert.assertEquals(exemptionPageObj.genericErrorMsg.getText(), "Date Acquired Property", "Verify that Date Acquired Property can't be updated generic error message beside cancel button");
			softAssert.assertEquals(objPage.getElementText(vaPageObj.editFieldErrorMsg), dataToEdit.get("dateAcauiredErrorMessage"),"Verify that Date Acquired Property can't be updated");
			objPage.Click(exemptionPageObj.cancelButton);
			
			//Step5: Updating 'Date Occupy Property' to 1/9/2020
			ReportLogger.INFO("verifying Date Occupied/Intend to Occupy Property can't be edited once record is saved");
			apasGenericObj.editAndInputFieldData("Date Occupied/Intend to Occupy Property",exemptionPageObj.dateOccupyProperty,dataToEdit.get("DateOccupyPropertyUpdated"));
			softAssert.assertEquals(exemptionPageObj.genericErrorMsg.getText(), "Date Occupied/Intend to Occupy Property", "Verify that Date Occupied Property can't be updated generic error message beside cancel button");
			softAssert.assertEquals(objPage.getElementText(vaPageObj.editFieldErrorMsg), dataToEdit.get("dateOccupyErrorMessage"),"Verify that Date Occupied Property can't be updated");			
			objPage.Click(exemptionPageObj.cancelButton);
			
			//Step6: Updating 'Effective Date of 100% USDVA Rating' to 1/10/2020
			ReportLogger.INFO("verifying Effective Date of 100% USDVA Rating can't be edited once record is saved");
			apasGenericObj.editAndInputFieldData("Effective Date of 100% USDVA Rating",exemptionPageObj.effectiveDateOfUSDVA,dataToEdit.get("EffectiveDateOfUSDVAUpdated"));
			softAssert.assertEquals(exemptionPageObj.genericErrorMsg.getText(), "Effective Date of 100% USDVA Rating", "Verify that Effective Date Of USDVA can't be updated generic error message beside cancel button");
			softAssert.assertEquals(objPage.getElementText(vaPageObj.editFieldErrorMsg), dataToEdit.get("effectiveDateErrorMessage"),"Verify that effective Date can't be updated");			
			objPage.Click(exemptionPageObj.cancelButton);
			
			//Step7: Updating 'Date of Notice of Rating' to 1/10/2020
			ReportLogger.INFO("verifying Date of Notice of 100% Rating can't be edited once record is saved");
			apasGenericObj.editAndInputFieldData("Date of Notice of 100% Rating",exemptionPageObj.dateOfNoticeOfRating,dataToEdit.get("DateOfNoticeUpdated"));
			softAssert.assertEquals(exemptionPageObj.genericErrorMsg.getText(), "Date of Notice of 100% Rating", "Verify that Date of Notice of 100% rating can't be updated generic error message beside cancel button");
			softAssert.assertEquals(objPage.getElementText(vaPageObj.editFieldErrorMsg), dataToEdit.get("dateOfNoticeErrorMessage"),"SMAB-T1282:Verify that Date of Notice of Rating can't be updated");			
			objPage.Click(exemptionPageObj.cancelButton);
			
			//step8: Verifying user gets error message when updating the 'Qualification?' from Qualified to Not Qualified
			ReportLogger.INFO("Verifying user gets error message when updating the 'Qualification?' from Qualified to Not Qualified");
			apasGenericObj.editAndSelectFieldData("Qualification?", "Not Qualified");			
			softAssert.assertEquals(objPage.getElementText(exemptionPageObj.QualificationOnDetailsPageErrorMsg), dataToEdit.get("QualificationUpdateErrorMsg"),"SMAB-T1269:Verify user gets error message when updating the 'Qualification?' from Qualified to Not Qualified");
			
			objPage.Click(exemptionPageObj.cancelButton);

			apasGenericObj.logout();
			
	
	}
	
	/**
	 Below test case is used to verify if 'End date Of Rating can not be Modified Once Entered by user'
	 * @throws Exception 
	 **/
	@Test(description = "SMAB-T1218:End Date of Rating can't be modified if initially set",dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression","DisabledVeteran" }, priority = 3, alwaysRun = true, enabled = true)	
	public void verify_Disabledveteran_EnddateOfRatingNotModifiableOnceEntered(String loginUser) throws Exception
	{
			Map<String, String> dataToEdit = objUtil.generateMapFromJsonFile(exemptionFilePath, "newExemptionMandatoryData");
			String expectedError=dataToEdit.get("endDateOfRatingErrorMessage");
			
			//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
				
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//step3: creating an exempton record
			objPage.Click(exemptionPageObj.newExemptionButton);
			
			exemptionPageObj.createNewExemptionWithMandatoryData(dataToEdit);
			//step4: adding end date of rating
			ReportLogger.INFO("Adding End date of Rating in the exemption");
			objPage.Click(exemptionPageObj.editExemption);
			objPage.enter(exemptionPageObj.endDateOfRating, dataToEdit.get("EnddateOfRating")); 
			apasGenericObj.selectFromDropDown(exemptionPageObj.endRatingReason, dataToEdit.get("EndRatingReason"));
			objPage.Click(ExemptionsPage.saveButton);
			
				
			
			
			//step6:now updating end date of rating on field level and verifying it should not be updated
			//ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying End date of Rating can't be modified once set on field level Edit");
			ReportLogger.INFO("Verifying End date of Rating can't be modified once updated on field level Edit");
			Thread.sleep(5000);
			//driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
			apasGenericObj.editAndInputFieldData("End Date of Rating",exemptionPageObj.endDateOfRating,dataToEdit.get("EnddateOfRatingUpdated"));
			String erroMsgOnField=vaPageObj.editFieldErrorMsg.getText().trim();
			softAssert.assertEquals(erroMsgOnField, expectedError,"SMAB-T1218:verified End Date of Rating can't be modified if set once on field level edit.");
			objPage.Click(exemptionPageObj.cancelButton);
			String exemptionStatus=exemptionPageObj.exemationStatusOnDetails.getText().trim();
			softAssert.assertEquals(exemptionStatus, "Inactive","SMAB-T643:Verify that User is able to validate Exemption 'Status' based on the 'End Date of Rating' for the Exemption record");
		
			
			
			//ste5: updating end date of rating on page level edit and verifying it should not be updated
			///ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying End date of Rating can't be modified once set on page level Edit");
			ReportLogger.INFO("Verifying End date of Rating can't be modified once set on Page level Edit");
			Thread.sleep(8000);
			objPage.waitForElementToBeClickable(5, exemptionPageObj.editExemption);
			objPage.Click(exemptionPageObj.editExemption);
			objPage.waitForElementToBeClickable(5,exemptionPageObj.endDateOfRating);
			objPage.enter(exemptionPageObj.endDateOfRating, dataToEdit.get("EnddateOfRatingUpdated")); 
			objPage.Click(ExemptionsPage.saveButton);
			Thread.sleep(3000);
			String actualError=exemptionPageObj.fieldErrorMsg.getText().trim();
			softAssert.assertEquals(actualError, expectedError, "SMAB-T500:Verify End date of Rating can't be modified once set");
			objPage.Click(exemptionPageObj.cancelButton);
			
			
			apasGenericObj.logout();
			
			
		}
		

	
}
		
		
	
	