package com.apas.Tests.DisabledVeteran;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class DisabledVeteran_ExemptionWithMandatoryFields_Test extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objExemptionsPage;
	Util objUtil;
	SoftAssertion softAssert;
	String mandatoryExemptionData;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		mandatoryExemptionData = System.getProperty("user.dir") + testdata.EXEMPTION_MANDATORY_FIELDS_ENTRY_DATA;
		objApasGenericFunctions.updateRollYearStatus("Closed", "2020");
	}
	
	/**
	 Below test case is used to validate 
	 -Error message when no mandatory fields are entered in Exemption record before saving it
	 -Create Exemption with mandatory values
	 -Exemption Support staff and RP Business Admin users are able to create an Exemption record
	 -Mandatory check validations on edit Exemption screen using Edit option on Show More button
	 -User is able to clear values on edit Exemption screen
	 -Previous values appear back on canceling the changes made
	 -Exemption Support staff and RP Business Admin users are able to view and edit an Exemption record
	 **/
	
	@Test(description = "SMAB-T522, SMAB-T523, SMAB-T479, SMAB-T480, SMAB-T481: Validate Exemption Support staff and RP Business Admin is able to view, create and edit Exemption record by filling mandatory fields", groups = {"smoke", "regression", "DisabledVeteranExemption"}, dataProvider = "loginRpBusinessAdminAndExemptionSupportUsers", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_DisabledVeteran_CreateAndEditExemptionAndMandatoryFieldErrorValidation(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Save an Exemption record without entering any details		
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to open an Exemption record");
		Thread.sleep(2000);
		objPage.Click(objPage.waitForElementToBeClickable(objExemptionsPage.newExemptionButton));
		objPage.waitForElementToBeClickable(objExemptionsPage.apn);
		objPage.waitForElementToBeClickable(objExemptionsPage.claimantName);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Without entering any data on the Exemption record, click 'Save' button");
		objExemptionsPage.saveExemptionRecord();
		
		//Step4: Validate error messages when no field value is entered and Exemption record is saved
		String expectedErrorMessageOnTop1 = "These required fields must be completed: Date Application Received, Basis for Claim, Date Acquired Property, Date Occupied/Intend to Occupy Property, Date of Notice of 100% Rating, Effective Date of 100% USDVA Rating, Claimant's Name, Claimant's SSN, APN, Qualification?";
		String expectedIndividualFieldMessage1 = "Complete this field";
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			expectedIndividualFieldMessage1 = "Complete this field.";
		}
		softAssert.assertEquals(objExemptionsPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop1,"SMAB-T523: Validating mandatory fields missing error in Exemption screen.");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date Application Received"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Date Application Received'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("APN"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Parcel'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date Acquired Property"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Date Acquired Property'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date Occupied/Intend to Occupy Property"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Date Occupied/Intend to Occupy Property'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Effective Date of 100% USDVA Rating"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Effective Date of 100% USDVA Rating'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date of Notice of 100% Rating"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Date of Notice of 100% Rating'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Basis for Claim"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Basis for Claim'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Qualification?"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Qualification?'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Claimant's Name"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Claimant's Name'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Claimant's SSN"),expectedIndividualFieldMessage1,"SMAB-T523: Validating mandatory fields missing error for 'Claimant's SSN'");

		ExtentTestManager.getTest().log(LogStatus.INFO, "Error messages related to mandatory fields are validated");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Cancel' button to move out of the Exemption screen");
		objPage.Click(objExemptionsPage.cancelButton);
		
		/*Step5: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
				 Create Exemption record
				 Capture the record id and Exemption Name*/
		
		Map<String, String> dataToCreateExemptionMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemption");
		dataToCreateExemptionMap.put("Veteran Name", dataToCreateExemptionMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		
		objExemptionsPage.createExemptionWithoutEndDateOfRating(dataToCreateExemptionMap);
		
		String recordId = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertTrue(exemptionName.contains("EXMPTN"),"SMAB-T480,SMAB-T522: Validate " + loginUser + " user is able to create Exemption with mandatory fields'");
	
		//Step6: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);

		//Step7: Search the existing Exemption record that was created and Edit it
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(exemptionName);
		softAssert.assertTrue(objApasGenericPage.clickShowMoreButtonAndAct("Exemptions", recordId, "Edit"),"SMAB-T481: Validate user is able to edit an Exemption record");
		softAssert.assertTrue(objExemptionsPage.saveButton.isDisplayed(), "SMAB-T481: Validate user is able to view the edit screen for the Exemption record");
	
		//Step8: Clear the values from the few mandatory fields and Save the record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clear some Date fields and SAVE the record");
		objPage.clearFieldValue(objExemptionsPage.dateAquiredProperty);
		objPage.clearFieldValue(objExemptionsPage.dateOccupiedProperty);
		objPage.clearFieldValue(objExemptionsPage.dateOfNotice);
		objPage.clearFieldValue(objExemptionsPage.effectiveDateOfUSDVA);
		objApasGenericFunctions.selectFromDropDown(objExemptionsPage.qualification, "--None--");
		objExemptionsPage.saveExemptionRecord();
				
		//Step9: Validate error messages when few mandatory field values are not present and Exemption record is saved	
		String expectedErrorMessageOnTop2 = "These required fields must be completed: Date Acquired Property, Effective Date of 100% USDVA Rating, Qualification?, Date of Notice of 100% Rating, Date Occupied/Intend to Occupy Property";
		String expectedIndividualFieldMessage2 = "Complete this field";
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			expectedIndividualFieldMessage2 = "Complete this field.";
		}
		softAssert.assertEquals(objExemptionsPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop2,"SMAB-T523: Validating mandatory fields missing error in Exemption screen.");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date Acquired Property"),expectedIndividualFieldMessage2,"SMAB-T523: Validating mandatory fields missing error for 'Date Acquired Property'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Effective Date of 100% USDVA Rating"),expectedIndividualFieldMessage2,"SMAB-T523: Validating mandatory fields missing error for 'Effective Date of 100% USDVA Rating'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Qualification?"),expectedIndividualFieldMessage2,"SMAB-T523: Validating mandatory fields missing error for 'Qualification?'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date of Notice of 100% Rating"),expectedIndividualFieldMessage2,"SMAB-T523: Validating mandatory fields missing error for 'Date of Notice of 100% Rating'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date Occupied/Intend to Occupy Property"),expectedIndividualFieldMessage2,"SMAB-T523: Validating mandatory fields missing error for 'Date Occupied/Intend to Occupy Property'");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Error messages related to mandatory fields are validated");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Cancel' button to move out of the Exemption screen");
		objPage.Click(objExemptionsPage.cancelButton);
				
		//Step10: Open it again and Validate the values which were initially saved, appear there
		Thread.sleep(1000);
		objExemptionsPage.openExemptionRecord(recordId, exemptionName);
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.dateAcquiredOnDetailPage)), objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionMap.get("Date Acquired Property")), "SMAB-T479: Value for 'Date Acquired Property' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.dateOccupiedOnDetailPage)), objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionMap.get("Date Occupied/Intend to Occupy Property")), "SMAB-T479: Value for 'Date Occupied/Intend to Occupy Property' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.dateOfNoticeOnDetailPage)), objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionMap.get("Date of Notice of 100% Rating")), "SMAB-T479: Value for 'Date of Notice of 100% Rating' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.effectiveDateOnDetailPage)), objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionMap.get("Effective Date of 100% USDVA Rating")), "SMAB-T479: Value for 'Effective Date of 100% USDVA Rating' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.qualificationOnDetailPage)), dataToCreateExemptionMap.get("Qualification?"), "SMAB-T479: Value for 'Qualification?' is retained post canceling the changes made on Edit Exemption screen");						
				
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate,
	 -Mandatory check validations on edit Exemption screen using Edit button
	 -Mandatory check validations on edit Exemption screen using Pencil Edit button
	 **/
	
	@Test(description = "SMAB-T522, SMAB-T527: Validate user is not able to edit and save Exemption record when mandatory fields are not entered before saving", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_DisabledVeteran_EditExemptionAndMandatoryFieldErrorValidation(String loginUser) throws Exception {

		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
				
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
					
		/*Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
				 Create Exemption record
				 Capture the record id and Exemption Name*/
				
		Map<String, String> dataToCreateExemptionMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemption");
		dataToCreateExemptionMap.put("Veteran Name", dataToCreateExemptionMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		objExemptionsPage.createExemptionWithoutEndDateOfRating(dataToCreateExemptionMap);
		String recordId = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertTrue(exemptionName.contains("EXMPTN"),"SMAB-T522: Validate user is able to create Exemption with mandtory fields'");
		
		//Step6: Editing this Exemption record using EDIT button on Exemption detail screen
		objExemptionsPage.editExemptionRecord();
		
		//Step7: Clear the values from few of the mandatory fields and Save the record
		objPage.clearFieldValue(objExemptionsPage.dateApplicationReceived);
		objPage.clearFieldValue(objExemptionsPage.claimantSSN);
		objApasGenericFunctions.selectFromDropDown(objExemptionsPage.unmarriedSpouseOfDisabledVeteran, "--None--");
		objExemptionsPage.saveExemptionRecord();
		
		//Step8: Validate error messages when few mandatory field values are not present and Exemption record is saved
		String expectedErrorMessageOnTop = "These required fields must be completed: Claimant's SSN, Unmarried Spouse of Deceased Veteran?, Date Application Received";
		String expectedIndividualFieldMessage = "Complete this field";
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			expectedIndividualFieldMessage = "Complete this field.";
		}
		softAssert.assertEquals(objExemptionsPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop,"SMAB-T527: Validating mandatory fields missing error in Exemption screen.");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Claimant's SSN"),expectedIndividualFieldMessage,"SMAB-T527: Validating mandatory fields missing error for 'Claimant's SSN'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Unmarried Spouse of Deceased Veteran?"),expectedIndividualFieldMessage,"SMAB-T527: Validating mandatory fields missing error for 'Unmarried Spouse of Deceased Veteran?'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date Application Received"),expectedIndividualFieldMessage,"SMAB-T527: Validating mandatory fields missing error for 'Date Application Received'");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Error messages related to mandatory fields are validated");
				
		//Step9: Enter the value in 'Unmarried Spouse of Deceased Veteran?' dropdown only and Save the record
		objApasGenericFunctions.selectFromDropDown(objExemptionsPage.unmarriedSpouseOfDisabledVeteran, "No");
		objExemptionsPage.saveExemptionRecord();
		
		//Step10: Validate error messages when some of the mandatory field values are still not present and Exemption record is saved
		String expectedErrorMessageOnTop1 = "These required fields must be completed: Claimant's SSN, Date Application Received";
		String expectedIndividualFieldMessage1 = "Complete this field";
		if(System.getProperty("region").equalsIgnoreCase("preuat")) {
			expectedIndividualFieldMessage1 = "Complete this field.";
		}
		softAssert.assertEquals(objExemptionsPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop1,"SMAB-T527: Validating mandatory fields missing error in Exemption screen.");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Claimant's SSN"),expectedIndividualFieldMessage1,"SMAB-T527: Validating mandatory fields missing error for 'Claimant's SSN'");
		softAssert.assertEquals(objExemptionsPage.getIndividualFieldErrorMessage("Date Application Received"),expectedIndividualFieldMessage1,"SMAB-T527: Validating mandatory fields missing error for 'Date Application Received'");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Error messages related to mandatory fields are validated");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Cancel' button to move out of the Exemption screen");
		objPage.Click(objExemptionsPage.cancelButton);
		
		//Step11: Edit the record on Detail page using EDIT button and update some details
		objExemptionsPage.editExemptionRecord();
		objExemptionsPage.enterEndDateOfRating(dataToCreateExemptionMap);
		
		//Step12: Save the changes and validate the details
		objExemptionsPage.saveExemptionRecord();
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.endDateOfRatingOnDetailPage)), objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionMap.get("End Date Of Rating")), "SMAB-T522: Validate updated value for 'End Date of Rating' appears in the Exemption record");						
	
		//Step13: Edit the record using pencil icon and remove 'Claimant's SSN' value and click Save button to validate the error message pop-up
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		objPage.Click(objExemptionsPage.editPencilIconForClaimantSSNOnDetailPage);
		objPage.clearFieldValue(objExemptionsPage.claimantSSNOnDetailEditPage);
		objPage.Click(objExemptionsPage.saveButtonOnDetailPage);
		Thread.sleep(1500);
		softAssert.assertTrue(objApasGenericPage.popUpErrorMessageWeHitASnag.isDisplayed(), "SMAB-T527: Validate error message pop-up that appear at the bottom of the page i.e. 'We hit a snag'");
		softAssert.assertTrue(objApasGenericPage.returnElemOnPopUpScreen("Claimant's SSN").isDisplayed(), "SMAB-T527: Validate that 'Claimant's SSN' appears in error message pop-up");
		
		//Step14: Cancel the changes and validate that original value saved for 'Claimant SSN' appears back
		objPage.Click(objExemptionsPage.cancelButtonOnDetailPage);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.claimantSSNOnDetailPage)).substring(7), dataToCreateExemptionMap.get("Claimant SSN").substring(7), "Validate last 4 digits of the original 'Claimant's SSN' value saved in the Exemption record");
		
		//Step15: Edit the record again by clicking pencil icon against 'Veteran SSN' field and update the field*
		objPage.Click(objExemptionsPage.editPencilIconForVeteranSSNOnDetailPage);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Update the Veteran SSN in the Exemption record to '800-45-6781'");
		Thread.sleep(1000);
		objExemptionsPage.enter(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.veteranSSNOnDetailEditPage), "800-45-6781");
		
		//Step16: Save the changes and validate it
		objPage.Click(objExemptionsPage.saveButtonOnDetailPage);
		Thread.sleep(3000);
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.veteranSSNOnDetailPage)).substring(7), "6781", "SMAB-T522: Validate updated value for 'Veteran SSN' appears in the Exemption record");		
		
		objApasGenericFunctions.logout();
	}

}
