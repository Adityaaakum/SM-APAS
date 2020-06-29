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

public class DisabledVeteran_DuplicateExemption_Test extends TestBase {
	
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
	}
	

	/**
	 Below test case is used to validate that Exemption records can be created with overlapping dates but for different Veteran 
	 **/
	
    @Test(description = "SMAB-T528: Validate user is able to create a same Exemption record with different Veteran Name", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_DisabledVeteran_SameExemptionWithDifferentVeteranName(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		Map<String, String> dataToCreateExemptionWithVeteranOneMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithVeteranOne");
		dataToCreateExemptionWithVeteranOneMap.put("Veteran Name", dataToCreateExemptionWithVeteranOneMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		
		//Step4: Create an Exemption
		objExemptionsPage.createExemptionWithoutEndDateOfRating(dataToCreateExemptionWithVeteranOneMap);
		
		//Step5: Capture the Record Id, Veteran Name, Start Date, APN and Exemption Name. Also, validate some of its details
		String recordId1 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		String apn1 = (objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.parcelOnDetailPage))).substring(0, 11);
		String veteranName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage));
		
		objPage.expandIcon(objExemptionsPage.expandedIconForMoreExemptionOnDetailPage);
		String startDate1 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.startDateOnDetailPage));
		softAssert.assertEquals(veteranName1, dataToCreateExemptionWithVeteranOneMap.get("Veteran Name"), "SMAB-T528: Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(startDate1, objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionWithVeteranOneMap.get("Date Occupied/Intend to Occupy Property")), "SMAB-T528: Validate 'Start Date' in the Exemption record");
		softAssert.assertEquals(apn1, dataToCreateExemptionWithVeteranOneMap.get("APN"), "SMAB-T528: Validate 'APN' in the Exemption record");
		
		//Step6: Navigate back to the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step7: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		Map<String, String> dataToCreateExemptionWithVeteranTwoMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithVeteranTwo");
		dataToCreateExemptionWithVeteranTwoMap.put("Veteran Name", dataToCreateExemptionWithVeteranTwoMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		
		//Step8: Create another Exemption
		objExemptionsPage.createExemptionWithoutEndDateOfRating(dataToCreateExemptionWithVeteranTwoMap);
		
		//Step9: Capture the Record Id, Veteran Name, Start Date, APN and Exemption Name. Also, validate some of its details
		String recordId2 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		String apn2 = (objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.parcelOnDetailPage))).substring(0, 11);
		String veteranName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage));
		
		objPage.expandIcon(objExemptionsPage.expandedIconForMoreExemptionOnDetailPage);
		String startDate2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.startDateOnDetailPage));
		softAssert.assertEquals(veteranName2, dataToCreateExemptionWithVeteranTwoMap.get("Veteran Name"), "SMAB-T528: Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(startDate2, objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionWithVeteranTwoMap.get("Date Acquired Property")), "SMAB-T528: Validate 'Start Date' in the Exemption record");
		softAssert.assertEquals(apn2, dataToCreateExemptionWithVeteranTwoMap.get("APN"), "SMAB-T528: Validate 'APN' in the Exemption record");
		
		//Step10: Validate the 'Veteran Name' and 'Exemption Name' shouldn't match
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T528: Verify user is able to create a same Exemption record with different Veteran Name");
		softAssert.assertTrue(veteranName1 != veteranName2, "SMAB-T528: Exemption records with overlapping dates are successfully created but for different Veteran");
	
		objApasGenericFunctions.logout();
    }
	
    
	/**
	 Below test case is used to validate that Exemption records can be created with overlapping dates but on different Parcel 
	 **/
	
    @Test(description = "SMAB-T530: Validate user is able to create a same Exemption record with different Parcel", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_DisabledVeteran_SameExemptionWithDifferentParcel(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		Map<String, String> dataToCreateExemptionWithParcelOneMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithParcelOne");
		dataToCreateExemptionWithParcelOneMap.put("Veteran Name", dataToCreateExemptionWithParcelOneMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));

		//Step4: Create an Exemption
		objExemptionsPage.createExemptionWithoutEndDateOfRating(dataToCreateExemptionWithParcelOneMap);
		
		//Step5: Capture the Record Id, Parcel Number, Start Date, Veteran Name and Exemption Name. Also, validate some of its details
		String recordId1 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		String apn1 = (objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.parcelOnDetailPage))).substring(0, 11);
		String veteranName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage));
		
		objPage.expandIcon(objExemptionsPage.expandedIconForMoreExemptionOnDetailPage);
		String startDate1 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.startDateOnDetailPage));
		softAssert.assertEquals(veteranName1, dataToCreateExemptionWithParcelOneMap.get("Veteran Name"), "SMAB-T530: Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(apn1, dataToCreateExemptionWithParcelOneMap.get("APN"), "SMAB-T530: Validate 'APN' in the Exemption record");
		softAssert.assertEquals(startDate1, objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionWithParcelOneMap.get("Date Occupied/Intend to Occupy Property")), "SMAB-T530: Validate 'Start Date' in the Exemption record");
		
		//Step6: Navigate back to the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step7: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		Map<String, String> dataToCreateExemptionWithParcelTwoMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithParcelTwo");
		dataToCreateExemptionWithParcelTwoMap.put("Veteran Name", dataToCreateExemptionWithParcelOneMap.get("Veteran Name"));
				
		//Step8: Create another Exemption
		objExemptionsPage.createExemptionWithoutEndDateOfRating(dataToCreateExemptionWithParcelTwoMap);
		
		//Step9: Capture the Record Id, Parcel Number, Start Date, Veteran Name and Exemption Name. Also, validate some of its details
		String recordId2 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		String apn2 = (objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.parcelOnDetailPage))).substring(0, 11);
		String veteranName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage));

		objPage.expandIcon(objExemptionsPage.expandedIconForMoreExemptionOnDetailPage);
		String startDate2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.startDateOnDetailPage));
		softAssert.assertEquals(veteranName2, dataToCreateExemptionWithParcelTwoMap.get("Veteran Name"), "SMAB-T530: Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(apn2, dataToCreateExemptionWithParcelTwoMap.get("APN"), "SMAB-T530: Validate 'APN' in the Exemption record");
		softAssert.assertEquals(startDate2, objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionWithParcelTwoMap.get("Effective Date of 100% USDVA Rating")), "SMAB-T530: Validate 'Start Date' in the Exemption record");
		
		//Step10: Validate the 'APN' and 'Exemption Name' shouldn't match
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T530: Verify user is able to create a same Exemption record with different Parcel");
		softAssert.assertTrue(apn1 != apn2, "SMAB-T530: Exemption records with overlapping dates are successfully created but with different Parcel Number");
	
		objApasGenericFunctions.logout();
    }
	
	/** Below test case is used to validate that another Exemption can be created for a Veteran if,
	    -End Date of second Exemption is set to a date earlier than Start Date of original record (Without End Date of Rating)
	 **/
	
	@Test(description = "SMAB-T532, SMAB-T649: Validate a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date earlier than Start Date of original record (having End Date of Rating as Blank)", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_DisabledVeteran_SameExemptionWithNonOverlappingDates1(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		Map<String, String> dataToCreateExemptionWithNoEndDateOfRatingMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithNoEndDateOfRating");
		dataToCreateExemptionWithNoEndDateOfRatingMap.put("Veteran Name", dataToCreateExemptionWithNoEndDateOfRatingMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		
		//Step4: Create an Exemption
		objExemptionsPage.createExemptionWithoutEndDateOfRating(dataToCreateExemptionWithNoEndDateOfRatingMap);
		
		//Step5: Capture the Record Id and Exemption Name. Also, validate some of its details
		String recordId1 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage)), dataToCreateExemptionWithNoEndDateOfRatingMap.get("Veteran Name"), "SMAB-T532: Validate 'Veteran Name' in the Exemption record with no End Date");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranSSNOnDetailPage)).substring(7), dataToCreateExemptionWithNoEndDateOfRatingMap.get("Veteran SSN").substring(7), "SMAB-T532: Validate last 4 digits of 'Veteran's SSN' in the Exemption record with no End Date");
		
		//Step7: Validate that the status of Exemption when 'End Date of Rating' on the Exemption is not saved
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.statusOnDetailPage)), "Active", "SMAB-T649: Verify that User is able to validate Exemption 'Status' based on the 'End Date of Rating' for the Exemption record");
				
		//Step8: Navigate back to the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step9: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		Map<String, String> dataToCreateExemptionWithEndDateOfRatingMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithEndDateOfRating");
		dataToCreateExemptionWithEndDateOfRatingMap.put("Veteran Name", dataToCreateExemptionWithNoEndDateOfRatingMap.get("Veteran Name"));
		
		//Step10: Create another Exemption
		objExemptionsPage.createExemptionWithEndDateOfRating(dataToCreateExemptionWithEndDateOfRatingMap);
		
		//Step11: Capture the Record Id and Exemption Name. Also, validate some of its details
		String recordId2 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage)), dataToCreateExemptionWithEndDateOfRatingMap.get("Veteran Name"), "SMAB-T532: Validate 'Veteran Name' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranSSNOnDetailPage)).substring(7), dataToCreateExemptionWithEndDateOfRatingMap.get("Veteran SSN").substring(7), "SMAB-T532: Validate last 4 digits of 'Veteran's SSN' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		
		objPage.expandIcon(objExemptionsPage.expandedIconForMoreExemptionOnDetailPage);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.startDateOnDetailPage)), objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionWithEndDateOfRatingMap.get("Effective Date of 100% USDVA Rating")), "SMAB-T532: Validate 'Start Date' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.endDateOnDetailPage)), objExemptionsPage.removeZeroInMonthAndDay(dataToCreateExemptionWithEndDateOfRatingMap.get("End Date Of Rating")), "SMAB-T532: Validate 'End Date' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		
		//Step12: Validate the 'Record Ids' and 'Exemption Names' doesn't match for both the Exemptions created
		softAssert.assertTrue(recordId1 != recordId2, "SMAB-T532: Exemption records for same Veteran with non-overlapping dates are created with different Record Ids");
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T532: Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date earlier than Start Date of original record (having End Date of Rating as Blank");
	
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 Below test case is used to validate that, 
	 -Another Exemption can be created for a Veteran if End Date of second Exemption is set to a date earlier than Start Date of original record (With End Date of Rating)
	 -Another Exemption can be created for a Veteran if Start Date of second Exemption (With End Date of Rating) is set to a date later than End Date of original record
	 -'Status' field on Exemption record is read-only
	 **/
    
	@Test(description = "SMAB-T533, SMAB-T534, SMAB-T609, SMAB-T659, SMAB-T643: Validate a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date earlier than Start Date of original record (End Date of Rating is not blank),  Vlaidate a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of Rating of original record), Validate a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of original record and status of original record is marked as Inactive, Validate user is not able to view 'Status' field as editable field on Exemption record", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_DisabledVeteran_SameExemptionWithNonOverlappingDates2(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		Map<String, String> dataToCreateExemptionWithRequiredFieldsMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithRequiredFields");
		dataToCreateExemptionWithRequiredFieldsMap.put("Veteran Name", dataToCreateExemptionWithRequiredFieldsMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		
		//Step4: Create an Exemption record
		objExemptionsPage.createExemptionWithEndDateOfRating(dataToCreateExemptionWithRequiredFieldsMap);
		
		//Step5: Capture the Record Id and Exemption Name. Also, validate some of its details
		String recordId1 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage)), dataToCreateExemptionWithRequiredFieldsMap.get("Veteran Name"), "SMAB-T609: Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranSSNOnDetailPage)).substring(7), dataToCreateExemptionWithRequiredFieldsMap.get("Veteran SSN").substring(7), "SMAB-T609: Validate last 4 digits of 'Veteran's SSN' in the Exemption record");
		
		//Step6: Validate that the status of Exemption is saved as 'Inactive' as 'End Date of Rating' on the Exemption is a past date
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.statusOnDetailPage)), "Inactive", "SMAB-T643: Verify that User is able to validate Exemption 'Status' based on the 'End Date of Rating' for the Exemption record");
		
		//Step7: Validate that the 'Status' is read-only field on Exemption record
		softAssert.assertTrue(objExemptionsPage.editExemptionAndValidateEnabledStatusOnDetailPage("Status", driver).equals("true"), "SMAB-T659: Verify user is not able to view 'Status' field as editable field on Exemption record");
		
		//Step8: Navigate back to Exemption module and create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		Map<String, String> dataToCreateExemptionWithDatesEarlierThanOriginalRecordMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithDatesEarlierThanOriginalRecord");
		dataToCreateExemptionWithDatesEarlierThanOriginalRecordMap.put("Veteran Name", dataToCreateExemptionWithRequiredFieldsMap.get("Veteran Name"));
		
		//Step9: Create another Exemption
		objExemptionsPage.createExemptionWithEndDateOfRating(dataToCreateExemptionWithDatesEarlierThanOriginalRecordMap);
		
		//Step10: Capture the Record Id and Exemption Name. Also, validate some of its details
		String recordId2 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage)), dataToCreateExemptionWithDatesEarlierThanOriginalRecordMap.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranSSNOnDetailPage)).substring(7), dataToCreateExemptionWithDatesEarlierThanOriginalRecordMap.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		
		//Step11: Validate the 'Record Ids' and 'Exemption Names' doesn't match for both the Exemptions created
		softAssert.assertTrue(recordId1 != recordId2, "Exemption records for same Veteran with non-overlapping dates are created with different Record Ids");
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T533: Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date earlier than Start Date of original record (End Date of Rating is not blank)");
	
		//Step12: Navigate back to Exemption module and create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		Map<String, String> dataToCreateExemptionWithDatesLaterThanOriginalRecordWithEndDateOfRatingMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithDatesLaterThanOriginalRecordWithEndDateOfRating");
		dataToCreateExemptionWithDatesLaterThanOriginalRecordWithEndDateOfRatingMap.put("Veteran Name", dataToCreateExemptionWithRequiredFieldsMap.get("Veteran Name"));
		
		//Step13: Create the third Exemption record
		objExemptionsPage.createExemptionWithEndDateOfRating(dataToCreateExemptionWithDatesLaterThanOriginalRecordWithEndDateOfRatingMap);
				
		//Step14: Capture the Record Id and Exemption Name. Also, validate some of its details
		String recordId3 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName3 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage)), dataToCreateExemptionWithDatesLaterThanOriginalRecordWithEndDateOfRatingMap.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record with Start Date set to a date later than End Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranSSNOnDetailPage)).substring(7), dataToCreateExemptionWithDatesLaterThanOriginalRecordWithEndDateOfRatingMap.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption record with Start Date set to a date later than End Date of initial record");
			
		//Step15: Validate the 'Record Ids' and 'Exemption Names' doesn't match for 2nd and 3rd Exemption record created
		softAssert.assertTrue(recordId2 != recordId3, "Exemption records for same Veteran with non-overlapping dates are created with different Record Ids");
		softAssert.assertTrue(exemptionName2 != exemptionName3, "SMAB-T534: Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of Rating of original record");
			
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 Below test case is used to validate that,
	 -Another Exemption can be created for a Veteran if Start Date of second Exemption (With No End Date of Rating) is set to a date later than End Date of original record
	 **/
	
	@Test(description = "SMAB-T534: Validate a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of Rating of original record)", groups = {"regression","DisabledVeteranExemption"}, dataProvider = "loginExemptionSupportStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void verify_DisabledVeteran_SameExemptionWithNonOverlappingDates3(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		Map<String, String> dataToCreateExemptionWithEndDateMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithEndDate");
		dataToCreateExemptionWithEndDateMap.put("Veteran Name", dataToCreateExemptionWithEndDateMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		
		//Step4: Create an Exemption record
		objExemptionsPage.createExemptionWithEndDateOfRating(dataToCreateExemptionWithEndDateMap);
		
		//Step5: Capture the Record Id and Exemption Name. Also, validate some of its details
		String recordId1 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage)), dataToCreateExemptionWithEndDateMap.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranSSNOnDetailPage)).substring(7), dataToCreateExemptionWithEndDateMap.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption record");
		
		//Step6: Navigate back to Exemption module and create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		Map<String, String> dataToCreateExemptionWithoutEndDateMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithoutEndDate");
		dataToCreateExemptionWithoutEndDateMap.put("Veteran Name", dataToCreateExemptionWithEndDateMap.get("Veteran Name"));
		
		//Step7: Create another Exemption record
		objExemptionsPage.createExemptionWithoutEndDateOfRating(dataToCreateExemptionWithoutEndDateMap);
		
		//Step8: Capture the Record Id and Exemption Name. Also, validate some of its details
		String recordId2 = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		objExemptionsPage.expandIcon(objExemptionsPage.expandedIconForGeneralExemptionOnDetailPage);
		String exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranNameOnDetailPage)), dataToCreateExemptionWithoutEndDateMap.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption detail record with End Date set to a date earlier than Start Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.veteranSSNOnDetailPage)).substring(7), dataToCreateExemptionWithoutEndDateMap.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption detail record with End Date set to a date earlier than Start Date of initial record");
		
		//Step9: Validate the 'Record Ids' and 'Exemption Names' doesn't match for both the Exemptions created
		softAssert.assertTrue(recordId1 != recordId2, "Exemption records for same Veteran with non-overlapping dates are created with different Record Ids");
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T534: Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of Rating of original record)");
	
		objApasGenericFunctions.logout();
	}	

}
