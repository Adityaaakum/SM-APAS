package com.apas.Tests.Exemptions;

import java.io.IOException;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Reports.ExtentTestManager;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.ApasGenericPage;
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
	
    

public class ExemptionTests extends TestBase implements testdata, modules, users{

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

	}
	/*@AfterMethod
	public void afterMethod() throws Exception {
		apasGenericObj.logout();
		Thread.sleep(3000);
	}*/
	
	/**
	 * Below function will be used to login to application with different users
	 *
	 * @return Return the user business Exemption Support Staff in an array
	 **/
	@DataProvider(name = "loginUsers")
	public Object[][] dataProviderLoginUserMethod() {
		return new Object[][] { { users.BPP_BUSINESS_ADMIN } };
	}
	
	/**
	 below test case is for business validations for Exemption fields
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@Test(description = "SMAB-T488,SMAB-T491,SMAB-T492,SMAB-T493,SMAB-T495,SMAB-T496:Future dates Error Messages for date Fields", dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, priority = 0, alwaysRun = true, enabled = true)
	public void verifyFuturedatesErrorMessagesWhileCreatingExemption(String loginUser) throws IOException, InterruptedException
	{
		try{
			
			Map<String, String> fieldData = objUtil.generateMapFromJsonFile(exemptionFilePath, "BusinessValidationsForExemptionFields");
			
			String futureDate=exemptionPageObj.getTommorowsdate();
	        
			//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			objPage.Click(exemptionPageObj.newExemptionButton);
			
			//Step3: selecting mandatory details before verifying error message
			apasGenericObj.searchAndSelectFromDropDown(exemptionPageObj.apn,fieldData.get("APN"),"//ul[@class='lookup__list  visible']");
			apasGenericObj.searchAndSelectFromDropDown(exemptionPageObj.claimantName,fieldData.get("ClaimantName"),"//ul[@class='lookup__list  visible']");
			objPage.enter(exemptionPageObj.claimantSSN, fieldData.get("ClaimantSSN"));
			
			//step4:
			/**verifying 'Date Application Received','Date Acquire Property','Date of Death Veteran',
			 'Date occupied/Intend to occupy Property','Date Move From Prior Residence',
			 'Effective Date of 100% USDVA Rating','Date of Notice of 100% Rating', can not be future dates
			 **/
			
			objPage.enter(exemptionPageObj.dateApplicationReceived,futureDate);
			objPage.enter(exemptionPageObj.dateOfDeathOfVeteran,futureDate);
			objPage.enter(exemptionPageObj.veteranName, fieldData.get("VeteranName"));
			objPage.enter(exemptionPageObj.veteranSSN, fieldData.get("VeteranSSN"));
			objPage.enter(exemptionPageObj.dateAquiredProperty,futureDate);
			objPage.enter(exemptionPageObj.dateOccupyProperty,futureDate);
			objPage.enter(exemptionPageObj.effectiveDateOfUSDVA,futureDate);
			objPage.enter(exemptionPageObj.dateOfNotice,futureDate);
			exemptionPageObj.selectbasisForClaims(fieldData, "BasisForClaim");
			objPage.enter(exemptionPageObj.endDateOfRating,futureDate);
			apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, fieldData.get("Qualification"));
			objPage.Click(exemptionPageObj.saveButton);
			Thread.sleep(5000);
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying all entered fields show future date error messages");
			int msgFound=exemptionPageObj.verifyErrorMessagesWhileCreatingExemption(fieldData,"FuturedatesErrorMessage");
			softAssert.assertEquals(msgFound,7,"SMAB-T488,SMAB-T491,SMAB-T492,SMAB-T493,SMAB-T495,SMAB-T496:Verify Application Date,Date Of Death Of Veteran, Date Acquired Property, Date Occupied/Intended to occupy Property, Date of Notice of 100% Rating,Effective Date of 100% USDVA Rating, End Date of rating can't be a Future Date");
			
			objPage.Click(objApasGenericPage.crossButton);
			softAssert.assertAll();
			apasGenericObj.logout();
			}
		catch(Exception e)
		{
			System.out.println("Error while validating Future dates error messages validations for Exemption "+e.getMessage());
			
		}
		
		
	}
	
	
	@Test(description = "SMAB-T501,SMAB-T502,SMAB-T503,SMAB-T497,SMAB-T498,SMAB-T1278,SMAB-T1122,SMAB-T1223,SMAB-T1263,SMAB-T1264:business validations for Exemption fields", dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, priority = 1, alwaysRun = true, enabled = true)
	public void verifyBusinessValidationsForExemptionFields(String loginUser) throws IOException, InterruptedException
	{
		try{
			
			Map<String, String> businessValidationdata = objUtil.generateMapFromJsonFile(exemptionFilePath, "BusinessValidationsForExemptionFields");
			
			//Step1: Login to the APAS application using the credentials passed through data provider
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			objPage.Click(exemptionPageObj.newExemptionButton);
			
			//Step3: selecting mandatory details before verifying error message
			apasGenericObj.searchAndSelectFromDropDown(exemptionPageObj.apn,businessValidationdata.get("APN"),"//ul[@class='lookup__list  visible']");
			objPage.enter(exemptionPageObj.dateApplicationReceived,businessValidationdata.get("DateApplicationReceived"));
			apasGenericObj.searchAndSelectFromDropDown(exemptionPageObj.claimantName,businessValidationdata.get("ClaimantName"),"//ul[@class='lookup__list  visible']");
			objPage.enter(exemptionPageObj.claimantSSN, businessValidationdata.get("ClaimantSSN"));
			objPage.enter(exemptionPageObj.veteranName, businessValidationdata.get("VeteranName"));
			objPage.enter(exemptionPageObj.dateAquiredProperty,businessValidationdata.get("DateAquiredProperty"));
			objPage.enter(exemptionPageObj.dateOccupyProperty,businessValidationdata.get("DateOccupyProperty"));
			objPage.enter(exemptionPageObj.effectiveDateOfUSDVA,businessValidationdata.get("EffectiveDateOfUSDVA"));
			objPage.enter(exemptionPageObj.dateOfNotice,businessValidationdata.get("DateOfNotice"));
			exemptionPageObj.selectbasisForClaims(businessValidationdata, "BasisForClaim");
			objPage.enter(exemptionPageObj.endDateOfRating,businessValidationdata.get("EnddateOfRating"));
			apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, businessValidationdata.get("Qualification"));
			objPage.Click(exemptionPageObj.saveButton);
		
			//step4:
			/**Verifying Veteran SSN is required if Veteran Name is mentioned
			**/
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying Veteran SSN is required if Veteran Name is mentioned");
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.veteranSSNErrorMsg.getText(),"Veteran's SSN is required" ,"SMAB-T502:Verify Veteran SSN is required if Veteran Name is mentioned");
			
			//step5:
			/**verifying 'Effective Date of 100% USDVA Rating' can not be greater than 'Date of Notice of 100% Rating'
			 *and 'End Date of Rating'
			**/
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying Effective Date of 100% USDVA Rating can't be greater than Date of Notice of 100% Rating");
			int msgFoundEDOUSDVA=exemptionPageObj.verifyErrorMessagesWhileCreatingExemption(businessValidationdata,"EffectiveDateOfUSDVAErrorMsg");
			softAssert.assertEquals(msgFoundEDOUSDVA, 3, "SMAB-T497,SMAB-T498,SMAB-T1278,SMAB-T1122:Verify End Rating Reason is required if End Date of rating is not BLANK and Verify End Date of rating must be greater than Effective Date of 100% USDVA Rating");
			
			
			
			//step6:
			/**verifying 'Date of Death of Veteran' and 'Deceased Veteran Qualification' are required when 'Unmarried_Spouse_of_Deceased_Veteran__c is 'Yes'
			**/
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying validation when unmarriedSpouseOfDisabledVeteran is Yes");
			apasGenericObj.selectFromDropDown(exemptionPageObj.unmarriedSpouseOfDisabledVeteran, "Yes");
			objPage.Click(exemptionPageObj.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.dateOfDeathOfVeteranErrorMsg.getText().trim(),"Date of Death of Veteran is required","SMAB-T494:Verify 'Date of Death of Veteran' is required field");
			softAssert.assertEquals(exemptionPageObj.deceasedVeteranQualificationErrormsg.getText(), "Deceased Veteran Qualification is required","SMAB-T498:Verify Deceased Veteran Qualification is required when unmarriedSpouseOfDisabledVeteran is Yes");

			
		
			//step7:
			/**verifying When 'DV_Exemption_on_Prior_Residence__c is 'Yes' then Prior Residence Street Address, 
			 Prior Residence City,Prior Residence State,Prior Residence County, Date moved from Prior Residence 
			 are all mandatory
			**/
			
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying validation for dvExemptionOnPriorResidence and realted fields");
			apasGenericObj.selectFromDropDown(exemptionPageObj.dvExemptionOnPriorResidence, "Yes");
			objPage.Click(exemptionPageObj.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.errorMessage.getText(),businessValidationdata.get("dvExemptionOnPriorResidenceYesErrorMsg"),"SMAB-T503:Verify Prior Residence Street Address, Prior Residence City,Prior Residence State,Prior Residence County, Date move from Prior Residence are required if 'DV Exemption on Prior Residence' is 'Yes'");
			
			//step8:
		/*
		  verifying Spouse SSN is required if Spouse Name is not blank
		*/ 
			objPage.enter(exemptionPageObj.spouseName, businessValidationdata.get("SpouseName"));
			objPage.Click(exemptionPageObj.saveButton);
			//objPage.locateElement("//div[contains(.,\"Spouse's SSN\")]/following-sibling::ul/li[@class='form-element__help']", 5);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.spouseSSNErrorMsg.getText(),"Spouse's SSN is required","SMAB-T501:Verify Spouse SSN is required if Spouse Name is mentioned");			
			
			//step9:
			/**verifying when 'Qualification' is Not Qualified then 'Qualification Denial Detail' and 'Qualification Denial Reason' are mandatory
			 * End Date of Rating has to be blank when Qualification is set to Not Qualified.
			 **/
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying validations when 'Qualification' is Not Qualified");
			objPage.enter(exemptionPageObj.endDateOfRating, "01/01/2020");
			apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, "Not Qualified");
			objPage.Click(exemptionPageObj.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.reasonForNotQualifiedErrorMsg.getText(), businessValidationdata.get("ReasonForNotQualifiedErrorMsg"), "SMAB-T1223:Verify When Exemption qualification is Not Qualified and the Exemption record is saved, then user is prompted to select a value for Qualification Denial Reason");
			softAssert.assertEquals(exemptionPageObj.enddateOfRatingErrorMsg.getText(),businessValidationdata.get("EnddateOfRatingErrorMsg1") , "mandatory check for End Date of Rating");
			
			
			//step10:
			/**verifying when 'Qualification' is Not Qualified and 'Reason for not Qualified' is 'Other' then 'Not Qualified Detail' is mandatory
			 *
			 **/
			
			apasGenericObj.selectFromDropDown(exemptionPageObj.reasonNotQualified, "Other");
			objPage.Click(exemptionPageObj.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.notQualifiedDetailErrorMsg.getText(), businessValidationdata.get("NotQualifiedDetailErrorMsg"), "SMAB-T1262:Verify When Exemption qualification is Not Qualified, Reason for Not Qualified is Other and the Exemption record is saved, then user is prompted to select a value for 'Not Qualified Detail'");
			
			
			//step11:
			/**verifying when 'Qualification' is Qualified and 'Reason for not Qualified' is Not Blank then user sees corresponding error message
			 **/
			
			apasGenericObj.selectFromDropDown(exemptionPageObj.qualification, "Qualified");
			objPage.Click(exemptionPageObj.saveButton);
			Thread.sleep(2000);
			softAssert.assertEquals(exemptionPageObj.reasonForNotQualifiedErrorMsg.getText(), "Reason for Not Qualified must be blank", "SMAB-T1263,SMAB-T1264:Verify When the Exemption qualification is Qualified,Reason for Not Qualified is not Blank and the Exemption record is saved, then user is prompted with error message");
			
			objPage.Click(objApasGenericPage.crossButton);
			softAssert.assertAll();
			apasGenericObj.logout();
			}
		catch(Exception e)
		{
			
			System.out.println("Error while validating business validations for Exemption "+e.getMessage());
			
		}
		
		
	}
	

	/**
	 Below test case is used to Edit an Exemption record
	 **/
	@Test(description = "SMAB-T1282:Verify date fields are not editable once entered for an Exemption",dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, priority = 2, alwaysRun = true, enabled = true)
	public void verifyDateFieldsNotEditableonCreatedExemptionDetailsPage(String loginUser)
	{
		try
		{
			Map<String, String> dataToEdit = objUtil.generateMapFromJsonFile(exemptionFilePath, "editExemptionData");
			
			//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
				
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//step3: creating an exemption record
			objPage.Click(exemptionPageObj.newExemptionButton);
			exemptionPageObj.createNewExemptionWithMandatoryData(dataToEdit);
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verifying after an Exemption record is created Date Acquired Property, Date Occupied/Intend to Occupy Property, Date of Notice of 100% Rating, Application Date and Effective Date of 100% USDVA Rating cannot be edited");
			
			//Step4: Updating 'Date Acquired Property' to 1/11/2020
			exemptionPageObj.editAndInputFieldData("Date Acquired Property",exemptionPageObj.dateAcquiredProperty,dataToEdit.get("DateAquiredPropertyUpdated"));
			softAssert.assertEquals(exemptionPageObj.genericErrorMsg.getText(), "Date Acquired Property", "Verify that Date Acquired Property can't be updated generic error message beside cancel button");
			softAssert.assertEquals(objPage.getElementText(vaPageObj.editFieldErrorMsg), dataToEdit.get("dateAcauiredErrorMessage"),"Verify that Date Acquired Property can't be updated");
			objPage.Click(exemptionPageObj.cancelButton);
			
			//Step5: Updating 'Date Occupy Property' to 1/9/2020
			exemptionPageObj.editAndInputFieldData("Date Occupied/Intend to Occupy Property",exemptionPageObj.dateOccupyProperty,dataToEdit.get("DateOccupyPropertyUpdated"));
			softAssert.assertEquals(exemptionPageObj.genericErrorMsg.getText(), "Date Occupied/Intend to Occupy Property", "Verify that Date Occupied Property can't be updated generic error message beside cancel button");
			softAssert.assertEquals(objPage.getElementText(vaPageObj.editFieldErrorMsg), dataToEdit.get("dateOccupyErrorMessage"),"Verify that Date Occupied Property can't be updated");			
			objPage.Click(exemptionPageObj.cancelButton);
			
			//Step6: Updating 'Effective Date of 100% USDVA Rating' to 1/10/2020
			exemptionPageObj.editAndInputFieldData("Effective Date of 100% USDVA Rating",exemptionPageObj.effectiveDateOfUSDVA,dataToEdit.get("EffectiveDateOfUSDVAUpdated"));
			softAssert.assertEquals(exemptionPageObj.genericErrorMsg.getText(), "Effective Date of 100% USDVA Rating", "Verify that Effective Date Of USDVA can't be updated generic error message beside cancel button");
			softAssert.assertEquals(objPage.getElementText(vaPageObj.editFieldErrorMsg), dataToEdit.get("effectiveDateErrorMessage"),"Verify that effective Date can't be updated");			
			objPage.Click(exemptionPageObj.cancelButton);
			
			//Step7: Updating 'Date of Notice of Rating' to 1/10/2020
			exemptionPageObj.editAndInputFieldData("Date of Notice of 100% Rating",exemptionPageObj.dateOfNoticeOfRating,dataToEdit.get("DateOfNoticeUpdated"));
			softAssert.assertEquals(exemptionPageObj.genericErrorMsg.getText(), "Date of Notice of 100% Rating", "Verify that Date of Notice of 100% rating can't be updated generic error message beside cancel button");
			softAssert.assertEquals(objPage.getElementText(vaPageObj.editFieldErrorMsg), dataToEdit.get("dateOfNoticeErrorMessage"),"SMAB-T1282:Verify that Date of Notice of Rating can't be updated");			
			objPage.Click(exemptionPageObj.cancelButton);
			
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verified after an Exemption record is created Date Acquired Property, Date Occupied/Intend to Occupy Property, Date of Notice of 100% Rating, Application Date and Effective Date of 100% USDVA Rating cannot be edited");
			softAssert.assertAll();
			apasGenericObj.logout();
			
				
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 Below test case is used to verify if 'End date Of Rating can not be Modified Once Entered by user'
	 **/
	@Test(description = "End Date of Rating can't be modified if initially set",dataProvider = "loginUsers", groups = {
			"smoke", "regression" }, priority = 3, alwaysRun = true, enabled = true)	
	public void verifyEnddateOfRatingNotModifiableOnceEntered(String loginUser)
	{
		
		try{

			Map<String, String> dataToEdit = objUtil.generateMapFromJsonFile(exemptionFilePath, "newExemptionMandatoryData");
			
			//Step1: Login to the APAS application using the credentials passed through data provider (Business admin or appraisal support)
				
			apasGenericObj.login(loginUser);
			
			//Step2: Opening the Exemption Module
			apasGenericObj.searchModule(EXEMPTIONS);
			
			//step3: creating an exempton record
			objPage.Click(exemptionPageObj.newExemptionButton);
			//apasGenericObj.locateElement("//h2[contains(text(),'New Exemption: Disabled Veterans')", 3);
			exemptionPageObj.createNewExemptionWithMandatoryData(dataToEdit);
			String expectedError=dataToEdit.get("endDateOfRatingErrorMessage");
			
			
			//step4: adding End date of rating
			ExtentTestManager.getTest().log(LogStatus.INFO, "Added End date Of Rating");
			objPage.Click(exemptionPageObj.editExemption);
			//apasGenericObj.locateElement("//h2[contains(.,'Edit Exemption')]", 3);
			objPage.scrollToElement(exemptionPageObj.endDateOfRating);
			objPage.enter(exemptionPageObj.endDateOfRating, dataToEdit.get("EnddateOfRating")); 
			apasGenericObj.selectFromDropDown(exemptionPageObj.endRatingReason, dataToEdit.get("EndRatingReason"));
			objPage.Click(exemptionPageObj.saveButton);
			//softAssert.assertEquals(actualError, expectedError, "SMAB-T485:Verified End date of Rating can't be modified once set");
			//ste5: updating end date of rating on page level edit and verifying it should not be updated
			ExtentTestManager.getTest().log(LogStatus.INFO, "Verify End date of Rating can't be modified once set on page level Edit");
			objPage.Click(exemptionPageObj.editExemption);
			//apasGenericObj.locateElement("//h2[contains(.,'Edit Exemption')]", 3);
			objPage.scrollToElement(exemptionPageObj.endDateOfRating);
			objPage.enter(exemptionPageObj.endDateOfRating, dataToEdit.get("EnddateOfRatingUpdated")); 
			String actualError=exemptionPageObj.fieldErrorMsg.getText().trim();
			softAssert.assertEquals(actualError, expectedError, "SMAB-T500:Verified End date of Rating can't be modified once set");
			objPage.Click(exemptionPageObj.cancelButton);
		
			//step6:now updating end date of rating on field level and verifying it should not be updated
			exemptionPageObj.editAndInputFieldData("End Date of Rating",exemptionPageObj.endDateOfRating,dataToEdit.get("EnddateOfRatingUpdated"));
			//String genericErrorMsg=exemptionPageObj.genericErrorMsg.getText().trim();
			//softAssert.assertEquals(genericErrorMsg, "End Date of Rating", "End Date of Rating can't be modified if initially set at field level");
			String erroMsgOnField=vaPageObj.editFieldErrorMsg.getText().trim();
			softAssert.assertEquals(erroMsgOnField, expectedError,"SMAB-T1218:verified End Date of Rating can't be modified if set once on field level edit.");
			objPage.Click(exemptionPageObj.cancelButton);
			String exemptionStatus=exemptionPageObj.exemationStatusOnDetails.getText().trim();
			softAssert.assertEquals(exemptionStatus, "Inactive","SMAB-T643:Verify that User is able to validate Exemption 'Status' based on the 'End Date of Rating' for the Exemption record");
			
			softAssert.assertAll();
			apasGenericObj.logout();
			
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			
		}
	}
	
}
		
		
	
	