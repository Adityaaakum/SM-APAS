package com.apas.Tests.DisabledVeteran;

import java.io.IOException;
import java.util.Map;

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
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class DisabledVeteran_RestrictDuplicateExemption_Test extends TestBase {
	
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
	 Below test case is used to validate no duplicate Exemption record can be created with overlapping details
	 Exemption1 : Without End Date of Rating
	 Exemption2 : Various combinations - Without End Date of Rating, With End Date of Rating, Interchangeable Start Dates, Different Qualified details
	 **/
	
	@Test(description = "SMAB-T526: Validate user is not able to create duplicate Exemption record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void DisabledVeteran_DuplicateExemptionIsNotCreated(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
			
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		Map<String, String> dataToCreateExemptionWithMandatoryFieldsMapOne = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFields1");
		dataToCreateExemptionWithMandatoryFieldsMapOne.put("Veteran Name", dataToCreateExemptionWithMandatoryFieldsMapOne.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));

		//Step4: Create Exemption record with some required fields with Veteran details but no End Date of Rating
		objExemptionsPage.createExemption(dataToCreateExemptionWithMandatoryFieldsMapOne);
			
		//Step5: Capture the record id 
		String recordId = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		String apn1 = (objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.parcelOnDetailPage))).substring(0, 11);
		
		//Step6: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step7: Save an Exemption record with overlapping details with no End Date of Rating
		dataToCreateExemptionWithMandatoryFieldsMapOne.put("Same APN", apn1);
		objExemptionsPage.createExemption(dataToCreateExemptionWithMandatoryFieldsMapOne);
			
		//Step8: Validate error message when Duplicate Exemption is created for same Veteran on same dates but with No End Date of Rating	
		String expectedDuplicateErrorMsgOnTop1 = "There seems to be an existing record with a blank end date. Please refer to record with id " + recordId + " for details";
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.duplicateErrorMsgWithBlankEndDate)), expectedDuplicateErrorMsgOnTop1, "SMAB-T526: Exemption already exist for the Veteran with no End Date");
		
		//Step9: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		Map<String, String> dataToCreateExemptionWithMandatoryFieldsMapTwo = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFields2");
				
		//Step10: Enter End date of Rating and save the record and validate the error message
		//objExemptionsPage.enterEndDateOfRating(dataToCreateExemptionWithMandatoryFieldsMapTwo);
		ReportLogger.INFO("Update some details in the Exemption record i.e. End Date Of Rating :  " + dataToCreateExemptionWithMandatoryFieldsMapTwo.get("End Date Of Rating") + " and End Rating Reason : " + dataToCreateExemptionWithMandatoryFieldsMapTwo.get("End Rating Reason"));
		objExemptionsPage.updateFieldValue("Date", "End Date of Rating", dataToCreateExemptionWithMandatoryFieldsMapTwo.get("End Date Of Rating"));
		objExemptionsPage.updateFieldValue("Dropdown", "End Rating Reason", dataToCreateExemptionWithMandatoryFieldsMapTwo.get("End Rating Reason"));
		objExemptionsPage.saveExemptionRecord();
			
		//Step11: Validate error message when duplicate Exemption is created for same Veteran with overlapping details	
		String expectedDuplicateErrorMsgOnTop2 = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId + " for details.";
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop2, "SMAB-T526: Exemption already exist for the Veteran with overlapping details");
			
		//Step12: Interchange Date Occupied and Effective Date values and save Exemption and validate the error message
		ReportLogger.INFO("Interchange values of 'Effective Date of 100% USDVA Rating' and 'Date Occupied/Intend to Occupy Property' in the Exemption record i.e. Date Occupied/Intend to Occupy Property :  " + dataToCreateExemptionWithMandatoryFieldsMapTwo.get("Effective Date of 100% USDVA Rating") + " and Effective Date of 100% USDVA Rating : " + dataToCreateExemptionWithMandatoryFieldsMapTwo.get("Date Occupied/Intend to Occupy Property"));
		objExemptionsPage.updateFieldValue("Date", "Date Occupied/Intend to Occupy Property", dataToCreateExemptionWithMandatoryFieldsMapTwo.get("Effective Date of 100% USDVA Rating"));
		objExemptionsPage.updateFieldValue("Date", "Effective Date of 100% USDVA Rating", dataToCreateExemptionWithMandatoryFieldsMapTwo.get("Date Occupied/Intend to Occupy Property"));
		objExemptionsPage.saveExemptionRecord();
			
		//Step13: Validate error message when duplicate Exemption is created for same Veteran with overlapping details	
		String expectedDuplicateErrorMsgOnTop3 = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId + " for details.";
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop3, "SMAB-T526: Exemption still has overlapping details as the Start Date is 'Date Acquired Property' or 'Effective Date of 100% USDVA Rating' or 'Date Occupied/Intend to Occupy Property' (whichever is later)");
			
		//Step14: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		Map<String, String> dataToEditExemptionForQualificationMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToEditExemptionForQualification");
	   
		//Step15: Update the Qualification field to 'Unqualified' and save the record and validate the error message
		ReportLogger.INFO("Update some details in the Exemption record i.e. Qualification? :  " + dataToEditExemptionForQualificationMap.get("Qualification?") + " and Reason for Not Qualified : " + dataToEditExemptionForQualificationMap.get("Reason for Not Qualified"));
		objExemptionsPage.updateFieldValue("Dropdown", "Qualification?", dataToEditExemptionForQualificationMap.get("Qualification?"));
		objExemptionsPage.updateFieldValue("Dropdown", "Reason for Not Qualified", dataToEditExemptionForQualificationMap.get("Reason for Not Qualified"));
		objExemptionsPage.saveExemptionRecord();
		
		//Step16: Validate error message when duplicate Exemption is created for same Veteran with overlapping details
		String expectedDuplicateErrorMsgOnTop4 = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId + " for details.";
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop4, "SMAB-T526: Exemption already exist for the Veteran with overlapping details");
		ReportLogger.INFO("Click 'Cancel' button to move out of the Exemption screen");
		objPage.Click(objExemptionsPage.cancelButton);
		
		objApasGenericFunctions.logout();
		
	}
	
	
	/**
	 Below test case is used to validate no duplicate Exemption records can be created for overlapping details
	 Exemption1 : Without End Date of Rating  ---->  Updated to save End Date of Rating
	 Exemption2 : Without End Date of Rating
	 **/
	
    @Test(description = "SMAB-T526: Validate user is not able to create duplicate Exemption record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void DisabledVeteran_EditExemptionAndDuplicateErrorValidation(String loginUser) throws Exception {
    	
    	//Step1: Login to the APAS application using the user passed through the data provider
    	objApasGenericFunctions.login(loginUser);
    		
    	//Step2: Open the Exemption module
    	objApasGenericFunctions.searchModule(modules.EXEMPTION);
    	
    	//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
    	Map<String, String> dataToCreateExemptionWithMandatoryFieldsMapOne = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFields1");
    	dataToCreateExemptionWithMandatoryFieldsMapOne.put("Veteran Name", dataToCreateExemptionWithMandatoryFieldsMapOne.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));

    	//Step4: Create Exemption record with some required fields with Veteran details but no End Date of Rating
    	objExemptionsPage.createExemption(dataToCreateExemptionWithMandatoryFieldsMapOne);
    	String recordId = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
    	String apn1 = (objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.parcelOnDetailPage))).substring(0, 11);
  
    	//Step5: Editing this Exemption record
    	objExemptionsPage.editExemptionRecord();
    	
    	//Step6: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it similar to the one in above record
    	Map<String, String> dataToCreateExemptionWithMandatoryFieldsMapTwo = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFields2");
    	dataToCreateExemptionWithMandatoryFieldsMapTwo.put("Veteran Name", dataToCreateExemptionWithMandatoryFieldsMapOne.get("Veteran Name"));
    	    
    	//Step7: Enter End date of Rating and save the record
    	ReportLogger.INFO("Update some details in the Exemption record i.e. End Date Of Rating :  " + dataToCreateExemptionWithMandatoryFieldsMapTwo.get("End Date Of Rating") + " and End Rating Reason : " + dataToCreateExemptionWithMandatoryFieldsMapTwo.get("End Rating Reason"));
		objExemptionsPage.updateFieldValue("Date", "End Date of Rating", dataToCreateExemptionWithMandatoryFieldsMapTwo.get("End Date Of Rating"));
		objExemptionsPage.updateFieldValue("Dropdown", "End Rating Reason", dataToCreateExemptionWithMandatoryFieldsMapTwo.get("End Rating Reason"));
    	objExemptionsPage.saveExemptionRecord();
    		
    	//Step8: Create another Exemption record with overlapping details but no End Date of Rating
    	objApasGenericFunctions.searchModule(modules.EXEMPTION);
    	dataToCreateExemptionWithMandatoryFieldsMapTwo.put("Same APN", apn1);
    	objExemptionsPage.createExemption(dataToCreateExemptionWithMandatoryFieldsMapTwo);
    				
    	//Step9: Validate error message when duplicate Exemption is created for same Veteran with overlapping details	
    	String expectedDuplicateErrorMsgOnTop = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId + " for details.";
    	softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop, "SMAB-T526: Both Exemptions has overlapping dates, hence another Exemption cannot be created for this Veteran");					
    	ReportLogger.INFO("Click 'Cancel' button to move out of the Exemption screen");
		objPage.Click(objExemptionsPage.cancelButton);
    			
    	objApasGenericFunctions.logout();
    	
	}
	
	/**
	 Below test case is used to validate no duplicate Exemption records can be created for overlapping details
	 Exemption1 : With End Date of Rating
	 Exemption2 : With End Date of Rating
	 **/
	
	@Test(description = "SMAB-T526: Validate user is not able to create duplicate Exemption record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void DisabledVeteran_DuplicateExemptionWithOverlappingDates(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);

		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		Map<String, String> dataToCreateExemptionWithEndDateOfRatingMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithEndDateOfRating");
		dataToCreateExemptionWithEndDateOfRatingMap.put("Veteran Name", dataToCreateExemptionWithEndDateOfRatingMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));

		//Step4: Create Exemption record with End Date of Rating
		objExemptionsPage.createExemption(dataToCreateExemptionWithEndDateOfRatingMap);
					
		//Step5: Capture the record id 
		String recordId = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		String apn1 = (objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.parcelOnDetailPage))).substring(0, 11);
				
		//Step6: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
				
		//Step7: Save another Exemption record with overlapping details and End Date of Rating
		dataToCreateExemptionWithEndDateOfRatingMap.put("Same APN", apn1);
		objExemptionsPage.createExemption(dataToCreateExemptionWithEndDateOfRatingMap);
					
		//Step8: Validate error message when Duplicate Exemption is created for same Veteran on same dates and with End Date of Rating	
		String expectedDuplicateErrorMsgOnTop1 = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId + " for details.";
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop1, "SMAB-T526: Exemption already exist for this Veteran with overlapping dates");
		ReportLogger.INFO("Click 'Cancel' button to move out of the Exemption screen");
		objPage.Click(objExemptionsPage.cancelButton);
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate that,
	 -No duplicate Exemption (Qualified) record can be created if one with 'Not Qualified' and same details already exist 
	 -Non Qualified Exemption is saved with status as 'Inactive'
	 -Non Qualified is updated with 'Qualified' and status changes to 'Active'
	 **/
	
	@Test(description = "SMAB-T526: Validate user is not able to create duplicate Exemption record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void DisabledVeteran_NotQualifiedExemptionDuplicateCreation(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		Map<String, String> dataToCreateNonQualifiedExemptionWithNoEndDateOfRatingMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateNonQualifiedExemptionWithNoEndDateOfRating");
		dataToCreateNonQualifiedExemptionWithNoEndDateOfRatingMap.put("Veteran Name", dataToCreateNonQualifiedExemptionWithNoEndDateOfRatingMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));

		//Step4: Create a non-Qualified Exemption with no end date of rating
		objExemptionsPage.createExemption(dataToCreateNonQualifiedExemptionWithNoEndDateOfRatingMap);
		
		//Step5: Capture the Record Id, Parcel Number, Start Date, Veteran Name and Exemption Name. Also, validate some of its details
		String recordId = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		String apn1 = (objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.parcelOnDetailPage))).substring(0, 11);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.statusOnDetailPage)), "Inactive", "SMAB-T526: Validate 'Status' field in the Exemption record");
		
		//Step6: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
						
		//Step7: Create another data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		Map<String, String> dataToCreateQualifiedExemptionWithNoEndDateOfRatingMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateQualifiedExemptionWithNoEndDateOfRating");
		dataToCreateQualifiedExemptionWithNoEndDateOfRatingMap.put("Veteran Name", dataToCreateNonQualifiedExemptionWithNoEndDateOfRatingMap.get("Veteran Name"));

		//Step8: Save a Qualified Exemption record with overlapping details as above
		dataToCreateQualifiedExemptionWithNoEndDateOfRatingMap.put("Same APN", apn1);
		objExemptionsPage.createExemption(dataToCreateQualifiedExemptionWithNoEndDateOfRatingMap);
							
		//Step9: Validate error message when Duplicate Exemption is created for same Veteran on same dates but with No End Date of Rating	
		String expectedDuplicateErrorMsgOnTop = "There seems to be an existing record with a blank end date. Please refer to record with id " + recordId + " for details";
		softAssert.assertEquals(objExemptionsPage.getElementText(objExemptionsPage.waitForElementToBeVisible(objExemptionsPage.duplicateErrorMsgWithBlankEndDate)), expectedDuplicateErrorMsgOnTop, "SMAB-T526: Validate if a Non Qualified exemption exist, another Qualified Exemption with same details cannot be created");
	
		//Step10: Cancel the existing record and then EDIT the Exemption record created initially
		ReportLogger.INFO("Click 'Cancel' button to move out of the Exemption screen");
		objPage.Click(objExemptionsPage.cancelButton);
		softAssert.assertTrue(objApasGenericPage.clickShowMoreButtonAndAct(exemptionName, "Edit"),"SMAB-T481: Validate user is able to edit the Exemption record : " + exemptionName);
		
		//Step11: Update the Exemption record to mark it as 'Qualified' and save it
		objApasGenericFunctions.selectFromDropDown(objExemptionsPage.qualification, "Qualified");
		objApasGenericFunctions.selectFromDropDown(objExemptionsPage.reasonNotQualified, "--None--");
		objExemptionsPage.saveExemptionRecord();
		
		//Step12: Search the Exemption record and open it using the locator
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(exemptionName);
		objExemptionsPage.openExemptionRecord(recordId, exemptionName);
		
		//Step13: Validate the changes made are updated in the record and status is changed to 'Active'
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.qualificationOnDetailPage)), "Qualified", "SMAB-T526: Validate 'Qualification?' field in the Exemption record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.statusOnDetailPage)), "Active", "SMAB-T526: Validate 'Status' field in the Exemption record");
		
		objApasGenericFunctions.logout();
	}
}
