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
	ExemptionsPage objDisabledVeteransPage;
	Util objUtil;
	Map<String, String> dataMap1;
	Map<String, String> dataMap2;
	SoftAssertion softAssert;
	String mandatoryExemptionData;
	String recordId, exemptionName;
	String recordId1, exemptionName1;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		if(driver==null) {
			setupTest();
			driver = BrowserDriver.getBrowserInstance();
		}
		driver = BrowserDriver.getBrowserInstance();
		objPage = new Page(driver);
		objDisabledVeteransPage = new ExemptionsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		mandatoryExemptionData = System.getProperty("user.dir") + testdata.EXEMPTION_MANDATORY_FIELDS_ENTRY_DATA;
	}
	
	@AfterMethod
	public void afterMethod() throws IOException, InterruptedException{
		//objApasGenericFunctions.logout();
		softAssert.assertAll();
	}
	
	/**
	 * Below function will be used to login to application with different users
	 * @return Return the user Exemption Support Staff
	 **/
    @DataProvider(name = "loginUsers")
    public Object[][] dataProviderLoginUserMethod() {
        return new Object[][] {{ users.EXEMPTION_SUPPORT_STAFF }};
    }
    
   @DataProvider(name = "loginUsers1")
    public Object[][] dataProviderLoginUserMethod1() {
        return new Object[][] {{users.EXEMPTION_SUPPORT_STAFF}, {users.RP_BUSINESS_ADMIN}};
    }
    
    
	/**
	 Below test case is used to validate 
	 -Error message when no mandatory fields are entered in Exemption record before saving it
	 -Create Exemption with mandatory values
	 -Exemption Support staff and RP Business Admin users are able to create an Exemption record
	 **/
	
	@Test(description = "SMAB-T522, SMAB-T523, SMAB-T480: Validate Exemption Support staff and RP Business Admin is able to create/edit Exemption record by filling mandatory fields", groups = {"smoke", "regression", "DisabledVeteranExemption"}, dataProvider = "loginUsers1")
	public void verify_DisabledVeteran_CreateExemptionWithMandatoryFields(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Save an Exemption record without entering any details		
		objDisabledVeteransPage.saveExemptionRecordWithNoValues();
		
		//Step4: Validate error messages when no field value is entered and Exemption record is saved
		String expectedErrorMessageOnTop = "These required fields must be completed: Date Application Received, Basis for Claim, Date Acquired Property, Date Occupied/Intend to Occupy Property, Date of Notice of 100% Rating, Effective Date of 100% USDVA Rating, Claimant's Name, Claimant's SSN, APN, Qualification?";
		String expectedIndividualFieldMessage = "Complete this field";
		softAssert.assertEquals(objDisabledVeteransPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop,"SMAB-T523: Validating mandatory fields missing error in Exemption screen.");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Date Application Received"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Date Application Received'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("APN"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Parcel'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Date Acquired Property"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Date Acquired Property'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Date Occupied/Intend to Occupy Property"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Date Occupied/Intend to Occupy Property'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Effective Date of 100% USDVA Rating"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Effective Date of 100% USDVA Rating'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Date of Notice of 100% Rating"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Date of Notice of 100% Rating'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Basis for Claim"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Basis for Claim'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Qualification?"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Qualification?'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Claimant's Name"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Claimant's Name'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Claimant's SSN"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Claimant's SSN'");

		ExtentTestManager.getTest().log(LogStatus.INFO, "Error messages related to mandatory fields are validated");
		objDisabledVeteransPage.cancelExemptionRecord();
		
		/*Step5: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
				 Create Exemption record
				 Capture the record id and Exemption Name*/
		
		if (loginUser.equals("exemptionSupportStaff")){
			dataMap1 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFieldsOne");
			dataMap1.put("Veteran Name", dataMap1.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));
			objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap1);
			recordId = objDisabledVeteransPage.getCurrentUrl(driver);
			exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
			softAssert.assertTrue(exemptionName.contains("EXMPTN"),"SMAB-T522: Validate user is able to create Exemption with mandtory fields'");
		}
		else {
			dataMap2 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFieldsTwo");
			dataMap2.put("Veteran Name", dataMap2.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));
			objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap2);
			recordId1 = objDisabledVeteransPage.getCurrentUrl(driver);
			exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
			softAssert.assertTrue(exemptionName1.contains("EXMPTN"),"SMAB-T480: Validate user is able to create Exemption with mandtory fields'");
		}
		
		objApasGenericFunctions.logout();
	}

	/**
	 Below test case is used to validate,
	 -Mandatory check validations on edit Exemption screen using Edit button
	 -Mandatory check validations on edit Exemption screen using Pencil Edit button
	 **/
	
	@Test(description = "SMAB-T522, SMAB-T527: Validate user is not able to edit and save Exemption record when mandatory fields are not entered before saving", groups = {"smoke", "regression","DisabledVeteranExemption"}, dependsOnMethods = {"verify_DisabledVeteran_CreateExemptionWithMandatoryFields"}, dataProvider = "loginUsers")
	public void verify_DisabledVeteran_EditExemptionAndMandatoryFieldErrorValidation(String loginUser) throws Exception {
	if(!failedMethods.contains("createExemptionWithMandatoryFields")) {	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);

		//Step3: Search the existing Exemption record that was created in above Test
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(exemptionName);
		objDisabledVeteransPage.openExemptionRecord(exemptionName);
		
		//Step4: Editing this Exemption record using EDIT button on Exemption detail screen
		objDisabledVeteransPage.editExemptionRecord();
		
		//Step5: Clear the values from few of the mandatory fields and Save the record
		objDisabledVeteransPage.clearFieldValue(objDisabledVeteransPage.dateApplicationReceived);
		objDisabledVeteransPage.clearFieldValue(objDisabledVeteransPage.claimantSSN);
		objDisabledVeteransPage.selectFromDropDown(objDisabledVeteransPage.unmarriedSpouseOfDisabledVeteran, "--None--");
		objDisabledVeteransPage.saveExemptionRecord();
		
		//Step6: Validate error messages when few mandatory field values are not present and Exemption record is saved
		String expectedErrorMessageOnTop = "These required fields must be completed: Claimant's SSN, Unmarried Spouse of Deceased Veteran?, Date Application Received";
		String expectedIndividualFieldMessage = "Complete this field";
		softAssert.assertEquals(objDisabledVeteransPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop,"SMAB-T527: Validating mandatory fields missing error in Exemption screen.");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Claimant's SSN"),expectedIndividualFieldMessage,"SMAB-T527: Validating mandatory fields missing error for 'Claimant's SSN'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Unmarried Spouse of Deceased Veteran?"),expectedIndividualFieldMessage,"SMAB-T527: Validating mandatory fields missing error for 'Unmarried Spouse of Deceased Veteran?'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Date Application Received"),expectedIndividualFieldMessage,"SMAB-T527: Validating mandatory fields missing error for 'Date Application Received'");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Error messages related to mandatory fields are validated");
				
		//Step7: Enter the value in 'Unmarried Spouse of Deceased Veteran?' dropdown only and Save the record
		objDisabledVeteransPage.selectFromDropDown(objDisabledVeteransPage.unmarriedSpouseOfDisabledVeteran, "No");
		objDisabledVeteransPage.saveExemptionRecord();
		
		//Step8: Validate error messages when some of the mandatory field values are still not present and Exemption record is saved
		String expectedErrorMessageOnTop1 = "These required fields must be completed: Claimant's SSN, Date Application Received";
		String expectedIndividualFieldMessage1 = "Complete this field";
		softAssert.assertEquals(objDisabledVeteransPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop1,"SMAB-T527: Validating mandatory fields missing error in Exemption screen.");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Claimant's SSN"),expectedIndividualFieldMessage1,"SMAB-T527: Validating mandatory fields missing error for 'Claimant's SSN'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Date Application Received"),expectedIndividualFieldMessage1,"SMAB-T527: Validating mandatory fields missing error for 'Date Application Received'");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Error messages related to mandatory fields are validated");
		objDisabledVeteransPage.cancelExemptionRecord();
		
		//Step9: Edit the record on Detail page using EDIT button and update some details
		objDisabledVeteransPage.editExemptionRecord();
		objDisabledVeteransPage.enterEndDateOfRating(dataMap1);
		
		//Step10: Save the changes and validate the details
		objDisabledVeteransPage.saveExemptionRecord();
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.endDateOfRatingOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap1.get("End Date Of Rating")), "SMAB-T522: Validate updated value for 'End Date of Rating' appears in the Exemption record");						
	
		//Step11: Edit the record using pencil icon and remove 'Claimant's SSN' value and click Save button to validate the error message pop-up
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		objPage.Click(objDisabledVeteransPage.editPencilIconForClaimantSSNOnDetailPage);
		objDisabledVeteransPage.clearFieldValue(objDisabledVeteransPage.claimantSSNOnDetailEditPage);
		objPage.Click(objDisabledVeteransPage.saveButtonOnDetailPage);
		Thread.sleep(1500);
		softAssert.assertTrue(driver.findElements(By.xpath("//h2[@class='slds-truncate slds-text-heading_medium']")).size() == 1, "SMAB-T527: Validate error message pop-up that appear at the bottom of the page i.e. 'We hit a snag'");
		softAssert.assertTrue(driver.findElements(By.xpath("//a[contains(text(), " + "\"" + "Claimant" + "'s" + " SSN" + "\"" + ")]")).size() == 1, "SMAB-T527: Validate that 'Claimant's SSN' appears in error message pop-up");
		
		//Step12: Cancel the changes and validate that original value saved for 'Claimant SSN' appears back
		objPage.Click(objDisabledVeteransPage.cancelButtonOnDetailPage);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.claimantSSNOnDetailPage)).substring(7), dataMap1.get("Claimant SSN").substring(7), "Validate last 4 digits of the original 'Claimant's SSN' value saved in the Exemption record");
		
		/*Step13: Edit the record again by clicking pencil icon against 'Veteran SSN' field and update the field*/
		objPage.Click(objDisabledVeteransPage.editPencilIconForVeteranSSNOnDetailPage);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Update the Veteran SSN in the Exemption record to '800-45-6781'");
		Thread.sleep(1000);
		objDisabledVeteransPage.enter(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranSSNOnDetailEditPage), "800-45-6781");
		
		//Step14: Save the changes and validate it
		objPage.Click(objDisabledVeteransPage.saveButtonOnDetailPage);
		Thread.sleep(1000);
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranSSNOnDetailPage)).substring(7), "6781", "SMAB-T522: Validate updated value for 'Veteran SSN' appears in the Exemption record");		
		
		objApasGenericFunctions.logout();
	}
	else {
		softAssert.assertTrue(false, "This Test depends on 'createExemptionWithMandatoryFields' which got failed");	
	}
	}

	/**
	 Below test case is used to validate,
	 -Mandatory check validations on edit Exemption screen using Edit option on Show More button
	 -User is able to clear values on edit Exemption screen
	 -Previous values appear back on canceling the changes made
	 -Exemption Support staff and RP Business Admin users are able to view and edit an Exemption record
	 **/
	
	@Test(description = "SMAB-T523, SMAB-T479, SMAB-T481: Validate that RP Business admin and Exemption Support staff are able to view and edit an Exemption record", groups = {"regression","DisabledVeteranExemption"}, dependsOnMethods = {"verify_DisabledVeteran_CreateExemptionWithMandatoryFields"}, dataProvider = "loginUsers1")
	public void verify_DisabledVeteran_EditExemptionUsingShowMoreAndMandatoryFieldErrorValidation(String loginUser) throws Exception {
	if(!failedMethods.contains("createExemptionWithMandatoryFields")) {	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);

		//Step3: User is able to view the Exemption record - Search the existing one that was created in above Test and Edit it using EDIT option from Show More button
		objApasGenericFunctions.displayRecords("All");
		
		if (loginUser.equals("exemptionSupportStaff")){
			objApasGenericFunctions.searchRecords(exemptionName);
			objDisabledVeteransPage.clickShowMoreButton(recordId, "Edit");
			softAssert.assertTrue(objDisabledVeteransPage.saveButton.isDisplayed(), "SMAB-T481: Validate user is able to edit an Exemption record");
		}
		else {
			objApasGenericFunctions.searchRecords(exemptionName1);
			objDisabledVeteransPage.clickShowMoreButton(recordId1, "Edit");
			softAssert.assertTrue(objDisabledVeteransPage.saveButton.isDisplayed(), "SMAB-T481: Validate user is able to edit an Exemption record");
		}
		
		//Step4: Clear the values from the few mandatory fields and Save the record
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clear some Date fields and SAVE the record");
		objDisabledVeteransPage.clearFieldValue(objDisabledVeteransPage.dateAquiredProperty);
		objDisabledVeteransPage.clearFieldValue(objDisabledVeteransPage.dateOccupiedProperty);
		objDisabledVeteransPage.clearFieldValue(objDisabledVeteransPage.dateOfNotice);
		objDisabledVeteransPage.clearFieldValue(objDisabledVeteransPage.effectiveDateOfUSDVA);
		objDisabledVeteransPage.selectFromDropDown(objDisabledVeteransPage.qualification, "--None--");
		objDisabledVeteransPage.saveExemptionRecord();
		
		//Step5: Validate error messages when few mandatory field values are not present and Exemption record is saved	
		String expectedErrorMessageOnTop = "These required fields must be completed: Date Acquired Property, Effective Date of 100% USDVA Rating, Qualification?, Date of Notice of 100% Rating, Date Occupied/Intend to Occupy Property";
		String expectedIndividualFieldMessage = "Complete this field";
		softAssert.assertEquals(objDisabledVeteransPage.errorMsgOnTop.getText(),expectedErrorMessageOnTop,"SMAB-T523: Validating mandatory fields missing error in Exemption screen.");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Date Acquired Property"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Date Acquired Property'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Effective Date of 100% USDVA Rating"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Effective Date of 100% USDVA Rating'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Qualification?"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Qualification?'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Date of Notice of 100% Rating"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Date of Notice of 100% Rating'");
		softAssert.assertEquals(objDisabledVeteransPage.getIndividualFieldErrorMessage("Date Occupied/Intend to Occupy Property"),expectedIndividualFieldMessage,"SMAB-T523: Validating mandatory fields missing error for 'Date Occupied/Intend to Occupy Property'");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Error messages related to mandatory fields are validated");
		objDisabledVeteransPage.cancelExemptionRecord();
		
		//Step6: Open it again and Validate the values which were initially saved, appear there
		if (loginUser.equals("exemptionSupportStaff")){
		objDisabledVeteransPage.openExemptionRecord(exemptionName);
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.dateAcquiredOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap1.get("Date Acquired Property")), "SMAB-T479: Value for 'Date Acquired Property' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.dateOccupiedOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap1.get("Date Occupied/Intend to Occupy Property")), "SMAB-T479: Value for 'Date Occupied/Intend to Occupy Property' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.dateOfNoticeOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap1.get("Date of Notice of 100% Rating")), "SMAB-T479: Value for 'Date of Notice of 100% Rating' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.effectiveDateOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap1.get("Effective Date of 100% USDVA Rating")), "SMAB-T479: Value for 'Effective Date of 100% USDVA Rating' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.qualificationOnDetailPage)), dataMap1.get("Qualification?"), "SMAB-T479: Value for 'Qualification?' is retained post canceling the changes made on Edit Exemption screen");						
		}
		else {
		objDisabledVeteransPage.openExemptionRecord(exemptionName1);
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.dateAcquiredOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap2.get("Date Acquired Property")), "SMAB-T479: Value for 'Date Acquired Property' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.dateOccupiedOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap2.get("Date Occupied/Intend to Occupy Property")), "SMAB-T479: Value for 'Date Occupied/Intend to Occupy Property' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.dateOfNoticeOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap2.get("Date of Notice of 100% Rating")), "SMAB-T479: Value for 'Date of Notice of 100% Rating' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.effectiveDateOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap2.get("Effective Date of 100% USDVA Rating")), "SMAB-T479: Value for 'Effective Date of 100% USDVA Rating' is retained post canceling the changes made on Edit Exemption screen");						
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.qualificationOnDetailPage)), dataMap2.get("Qualification?"), "SMAB-T479: Value for 'Qualification?' is retained post canceling the changes made on Edit Exemption screen");						
		}
		
		objApasGenericFunctions.logout();
	}
	else {
		softAssert.assertTrue(false, "This Test depends on 'createExemptionWithMandatoryFields' which got failed");	
	}
	}
}
