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
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;

public class DisabledVeteran_RestrictDuplicateExemption_Test extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objDisabledVeteransPage;
	Util objUtil;
	Map<String, String> dataMap;
	Map<String, String> dataMap1;
	Map<String, String> dataMap2;
	SoftAssertion softAssert;
	String mandatoryExemptionData;
	String recordId, exemptionName;
	String recordId1, exemptionName1;
	String recordId2, exemptionName2;
	
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
        return new Object[][] {{ users.EXEMPTION_SUPPORT_STAFF } };
    }
	
	/**
	 Below test case is used to validate no duplicate Exemption record can be created with overlapping details
	 Exemption1 : Without End Date of Rating
	 Exemption2 : Various combinations - Without End Date of Rating, With End Date of Rating, Interchangeable Start Dates, Different Qualified details
	 **/
	
	@Test(description = "SMAB-T526: Validate user is not able to create duplicate Exemption record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void verify_DisabledVeteran_DuplicateExemptionIsNotCreated(String loginUser) throws Exception {
	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
			
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		dataMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFields");
		dataMap.put("Veteran Name", dataMap.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));

		//Step4: Create Exemption record with some required fields with Veteran details but no End Date of Rating
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap);
			
		//Step5: Capture the record id and Exemption Name
		recordId = objDisabledVeteransPage.getCurrentUrl(driver);
		exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		
		//Step6: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step7: Create another data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
	    dataMap1 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFields");
	    dataMap1.put("Veteran Name", dataMap.get("Veteran Name"));
	    
		//Step8: Save an Exemption record with overlapping details
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap1);
			
		//Step9: Validate error message when Duplicate Exemption is created for same Veteran on same dates but with No End Date of Rating	
		String expectedDuplicateErrorMsgOnTop1 = "There seems to be an existing record with a blank end date. Please refer to record with id " + recordId + " for details";
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.duplicateErrorMsgWithBlankEndDate)), expectedDuplicateErrorMsgOnTop1, "SMAB-T526: Exemption already exist for the Veteran with no End Date");
			
		//Step10: Enter End date of Rating and save the record and validate the error message
		objDisabledVeteransPage.enterEndDateOfRating(dataMap1);
		objDisabledVeteransPage.saveExemptionRecord();
			
		//Step11: Validate error message when duplicate Exemption is created for same Veteran with overlapping details	
		String expectedDuplicateErrorMsgOnTop2 = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId + " for details.";
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop2, "SMAB-T526: Exemption already exist for the Veteran with overlapping details");
			
		//Step12: Interchange Date Occupied and Effective Date values and save Exemption and validate the error message
		objDisabledVeteransPage.interchangeOccupiedAndEffectiveDate(dataMap1);
		objDisabledVeteransPage.saveExemptionRecord();
			
		//Step13: Validate error message when duplicate Exemption is created for same Veteran with overlapping details	
		String expectedDuplicateErrorMsgOnTop3 = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId + " for details.";
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop3, "SMAB-T526: Exemption still has overlapping details as the Start Date is 'Date Acquired Property' or 'Effective Date of 100% USDVA Rating' or 'Date Occupied/Intend to Occupy Property' (whichever is later)");
			
		//Step14: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
	    dataMap2 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToEditExemptionForQualification");
	   
		//Step15: Update the Qualification field to 'Unqualified' and save the record and validate the error message
		objDisabledVeteransPage.updateQualifiedData(dataMap2);
		objDisabledVeteransPage.saveExemptionRecord();
			
		//Step16: Validate error message when duplicate Exemption is created for same Veteran with overlapping details
		String expectedDuplicateErrorMsgOnTop4 = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId + " for details.";
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop4, "SMAB-T526: Exemption already exist for the Veteran with overlapping details");
		objDisabledVeteransPage.cancelExemptionRecord();
		
		objApasGenericFunctions.logout();
		
	}
	
	
	/**
	 Below test case is used to validate no duplicate Exemption records can be created for overlapping details
	 Exemption1 : With End Date of Rating
	 Exemption2 : Without End Date of Rating
	 **/
	
    @Test(description = "SMAB-T526: Validate user is not able to create duplicate Exemption record", groups = {"regression","DisabledVeteranExemption"}, dependsOnMethods = {"verify_DisabledVeteran_DuplicateExemptionIsNotCreated"}, dataProvider = "loginUsers")
	public void verify_DisabledVeteran_EditExemptionAndDuplicateErrorValidation(String loginUser) throws Exception {
    if(!failedMethods.contains("duplicateExemptionIsNotCreated")) {	
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);

		//Step3: Search the existing Exemption record that was created in above Test
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(exemptionName);
		objDisabledVeteransPage.openExemptionRecord(exemptionName);
		
		//Step4: Editing this Exemption record
		objDisabledVeteransPage.editExemptionRecord();
		
		//Step5: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
	    //dataMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFields");
	    
	    //Step5: Enter End date of Rating and save the record
		objDisabledVeteransPage.enterEndDateOfRating(dataMap);
		objDisabledVeteransPage.saveExemptionRecord();
		
		//Step6: Save an Exemption record with overlapping details	
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap);
			
		//Step7: Validate error message when duplicate Exemption is created for same Veteran with overlapping details	
		String expectedDuplicateErrorMsgOnTop = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId + " for details.";
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop, "SMAB-T526: Both Exemptions has overlapping dates, hence another Exemption cannot be created for this Veteran");					
		objDisabledVeteransPage.cancelExemptionRecord();
		
		objApasGenericFunctions.logout();
    }
	else {
		softAssert.assertTrue(false, "This Test depends on 'duplicateExemptionIsNotCreated' which got failed");	
	}
	}
	
	/**
	 Below test case is used to validate no duplicate Exemption records can be created for overlapping details
	 Exemption1 : With End Date of Rating
	 Exemption2 : With End Date of Rating
	 **/
	
	@Test(description = "SMAB-T526: Validate user is not able to create duplicate Exemption record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void verify_DisabledVeteran_DuplicateExemptionWithOverlappingDates(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);

		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);

		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		dataMap1 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithEndDateOfRatingOne");
		dataMap1.put("Veteran Name", dataMap1.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));

		//Step4: Create Exemption record with some required fields
		objDisabledVeteransPage.createExemptionWithEndDateOfRating(dataMap1);
					
		//Step5: Capture the record id and Exemption Name
		recordId1 = objDisabledVeteransPage.getCurrentUrl(driver);
		exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
				
		//Step6: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
				
		//Step7: Create another data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		dataMap2 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithEndDateOfRatingTwo");
		dataMap2.put("Veteran Name", dataMap1.get("Veteran Name"));
		
		//Step8: Save another Exemption record with overlapping details
		objDisabledVeteransPage.createExemptionWithEndDateOfRating(dataMap2);
					
		//Step9: Validate error message when Duplicate Exemption is created for same Veteran on same dates and with End Date of Rating	
		String expectedDuplicateErrorMsgOnTop1 = "There seems to be an existing record with overlapping details. Please refer to record with id " + recordId1 + " for details.";
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.duplicateErrorMsgWithOverlappingDetails)), expectedDuplicateErrorMsgOnTop1, "SMAB-T526: Exemption already exist for this Veteran with overlapping dates");
		objDisabledVeteransPage.cancelExemptionRecord();
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 Below test case is used to validate that,
	 -No duplicate Exemption (Qualified) record can be created if one with 'Not Qualified' and same details already exist 
	 -Non Qualified Exemption is saved with status as 'Inactive'
	 -Non Qualified is updated with 'Qualified' and status changes to 'Active'
	 **/
	
	@Test(description = "SMAB-T526: Validate user is not able to create duplicate Exemption record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginUsers")
	public void verify_DisabledVeteran_NotQualifiedExemptionDuplicateCreation(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		dataMap1 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateNonQualifiedExemptionWithNoEndDateOfRating");
		dataMap1.put("Veteran Name", dataMap1.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));

		//Step4: Create an Exemption
		objDisabledVeteransPage.createNonQualifiedExemption(dataMap1);
		
		//Step5: Capture the Record Id, Parcel Number, Start Date, Veteran Name and Exemption Name. Also, validate some of its details
		recordId2 = objDisabledVeteransPage.getCurrentUrl(driver);
		exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.statusOnDetailPage)), "Inactive", "SMAB-T526: Validate 'Status' field in the Exemption record");
		
		//Step6: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
						
		//Step7: Create another data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		dataMap2 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateQualifiedExemptionWithNoEndDateOfRating");
		dataMap2.put("Veteran Name", dataMap1.get("Veteran Name"));

		//Step8: Save a Qualified Exemption record with overlapping details as above
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap2);
							
		//Step9: Validate error message when Duplicate Exemption is created for same Veteran on same dates but with No End Date of Rating	
		String expectedDuplicateErrorMsgOnTop = "There seems to be an existing record with a blank end date. Please refer to record with id " + recordId2 + " for details";
		softAssert.assertEquals(objDisabledVeteransPage.getElementText(objDisabledVeteransPage.waitForElementToBeVisible(objDisabledVeteransPage.duplicateErrorMsgWithBlankEndDate)), expectedDuplicateErrorMsgOnTop, "SMAB-T526: Validate if a Non Qualified exemption exist, another Qualified Exemption with same details cannot be created");
	
		//Step10: Cancel the existing record and then EDIT the Exemption record created initially
		objDisabledVeteransPage.cancelExemptionRecord();
		objDisabledVeteransPage.clickShowMoreButton(recordId2, "Edit");
		
		//Step12: Update the Exemption record to mark it as 'Qualified' and save it
		objDisabledVeteransPage.selectFromDropDown(objDisabledVeteransPage.qualification, "Qualified");
		objDisabledVeteransPage.selectFromDropDown(objDisabledVeteransPage.reasonNotQualified, "--None--");
		objDisabledVeteransPage.saveExemptionRecord();
		
		//Step13: Search the Exemption record and open it using the locator
		objApasGenericFunctions.displayRecords("All");
		objApasGenericFunctions.searchRecords(exemptionName2);
		objDisabledVeteransPage.openExemptionUsingLocator(recordId2, exemptionName2);
		
		//Step14: Validate the changes made are updated in the record and status is changed to 'Active'
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.qualificationOnDetailPage)), "Qualified", "SMAB-T526: Validate 'Qualification?' field in the Exemption record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.statusOnDetailPage)), "Active", "SMAB-T526: Validate 'Status' field in the Exemption record");
		
		objApasGenericFunctions.logout();
	}
}
