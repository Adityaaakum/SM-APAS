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

public class DuplicateExemptionCreationTest extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objDisabledVeteransPage;
	Util objUtil;
	Map<String, String> dataMap1;
	Map<String, String> dataMap2;
	Map<String, String> dataMap3;
	SoftAssertion softAssert;
	String mandatoryExemptionData, propertyValue;
	String recordId1, exemptionName1, veteranName1, apn1, startDate1, status1;
	String recordId2, exemptionName2, veteranName2, apn2, startDate2, status2;
	String recordId3, exemptionName3;
	
	
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
	 Below test case is used to validate that Exemption records can be created with overlapping dates but for different Veteran 
	 **/
	
    @Test(description = "SMAB-T528: Disabled Veteran - Verify user is able to create a same Exemption record with different Veteran Name", groups = {"regression", "DisabledVeteran"}, dataProvider = "loginUsers", alwaysRun = true)
	public void duplicateExemptionCreationButWithDifferentVeteranName(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		dataMap1 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithVeteranOne");
		dataMap1.put("Veteran Name", dataMap1.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));
		
		//Step4: Create an Exemption
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap1);
		
		//Step5: Capture the Record Id, Veteran Name, Start Date, APN and Exemption Name. Also, validate some of its details
		recordId1 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		apn1 = (objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.parcelOnDetailPage))).substring(0, 11);
		veteranName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage));
		
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForMoreExemptionOnDetailPage);
		startDate1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.startDateOnDetailPage));
		softAssert.assertEquals(veteranName1, dataMap1.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(startDate1, objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap1.get("Date Occupied/Intend to Occupy Property")), "Validate 'Start Date' in the Exemption record");
		softAssert.assertEquals(apn1, dataMap1.get("APN"), "Validate 'APN' in the Exemption record");
		
		//Step6: Navigate back to the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step7: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		dataMap2 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithVeteranTwo");
		dataMap2.put("Veteran Name", dataMap2.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));
		
		//Step8: Create another Exemption
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap2);
		
		//Step9: Capture the Record Id, Veteran Name, Start Date, APN and Exemption Name. Also, validate some of its details
		recordId2 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		apn2 = (objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.parcelOnDetailPage))).substring(0, 11);
		veteranName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage));
		
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForMoreExemptionOnDetailPage);
		startDate2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.startDateOnDetailPage));
		softAssert.assertEquals(veteranName2, dataMap2.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(startDate2, objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap2.get("Date Acquired Property")), "Validate 'Start Date' in the Exemption record");
		softAssert.assertEquals(apn2, dataMap2.get("APN"), "Validate 'APN' in the Exemption record");
		
		//Step10: Validate the 'Veteran Name' and 'Exemption Name' shouldn't match
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T528: Verify user is able to create a same Exemption record with different Veteran Name");
		softAssert.assertTrue(veteranName1 != veteranName2, "Exemption records with overlapping dates are successfully created but for different Veteran");
	
		objApasGenericFunctions.logout();
    }
	
    
	/**
	 Below test case is used to validate that Exemption records can be created with overlapping dates but on different Parcel 
	 **/
	
    @Test(description = "SMAB-T530: Disabled Veteran - Verify user is able to create a same Exemption record with different Parcel", groups = {"regression", "DisabledVeteran"}, dataProvider = "loginUsers", alwaysRun = true)
	public void duplicateExemptionCreationButForDifferentParcel(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		dataMap1 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithParcelOne");
		dataMap1.put("Veteran Name", dataMap1.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));

		//Step4: Create an Exemption
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap1);
		
		//Step5: Capture the Record Id, Parcel Number, Start Date, Veteran Name and Exemption Name. Also, validate some of its details
		recordId1 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		apn1 = (objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.parcelOnDetailPage))).substring(0, 11);
		veteranName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage));
		
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForMoreExemptionOnDetailPage);
		startDate1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.startDateOnDetailPage));
		softAssert.assertEquals(veteranName1, dataMap1.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(apn1, dataMap1.get("APN"), "Validate 'APN' in the Exemption record");
		softAssert.assertEquals(startDate1, objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap1.get("Date Occupied/Intend to Occupy Property")), "Validate 'Start Date' in the Exemption record");
		
		//Step6: Navigate back to the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step7: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		dataMap2 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithParcelTwo");
		dataMap2.put("Veteran Name", dataMap1.get("Veteran Name"));
				
		//Step8: Create another Exemption
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap2);
		
		//Step9: Capture the Record Id, Parcel Number, Start Date, Veteran Name and Exemption Name. Also, validate some of its details
		recordId2 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		apn2 = (objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.parcelOnDetailPage))).substring(0, 11);
		veteranName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage));

		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForMoreExemptionOnDetailPage);
		startDate2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.startDateOnDetailPage));
		softAssert.assertEquals(veteranName2, dataMap2.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(apn2, dataMap2.get("APN"), "Validate 'APN' in the Exemption record");
		softAssert.assertEquals(startDate2, objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap2.get("Effective Date of 100% USDVA Rating")), "Validate 'Start Date' in the Exemption record");
		
		//Step10: Validate the 'APN' and 'Exemption Name' shouldn't match
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T530: Verify user is able to create a same Exemption record with different Parcel");
		softAssert.assertTrue(apn1 != apn2, "Exemption records with overlapping dates are successfully created but with different Parcel Number");
	
		objApasGenericFunctions.logout();
    }
	
	/** Below test case is used to validate that another Exemption can be created for a Veteran if,
	    -End Date of second Exemption is set to a date earlier than Start Date of original record (Without End Date of Rating)
	 **/
	
	@Test(description = "SMAB-T532: Disabled Veteran - Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date earlier than Start Date of original record (having End Date of Rating as Blank)", groups = {"regression", "DisabledVeteran"}, dataProvider = "loginUsers", alwaysRun = true)
	public void duplicateExemptionWithNonOverlappingDates1(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		dataMap1 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithNoEndDateOfRating");
		dataMap1.put("Veteran Name", dataMap1.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));
		
		//Step4: Create an Exemption
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap1);
		
		//Step5: Capture the Record Id and Exemption Name. Also, validate some of its details
		recordId1 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage)), dataMap1.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record with no End Date");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranSSNOnDetailPage)).substring(7), dataMap1.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption record with no End Date");
		
		//Step7: Validate that the status of Exemption when 'End Date of Rating' on the Exemption is not saved
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.statusOnDetailPage)), "Active", "SMAB-T649: Verify that User is able to validate Exemption 'Status' based on the 'End Date of Rating' for the Exemption record");
				
		//Step8: Navigate back to the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step9: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		dataMap2 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithEndDateOfRating");
		dataMap2.put("Veteran Name", dataMap1.get("Veteran Name"));
		
		//Step10: Create another Exemption
		objDisabledVeteransPage.createExemptionWithEndDateOfRating(dataMap2);
		
		//Step11: Capture the Record Id and Exemption Name. Also, validate some of its details
		recordId2 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage)), dataMap2.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranSSNOnDetailPage)).substring(7), dataMap2.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForMoreExemptionOnDetailPage);
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.startDateOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap2.get("Effective Date of 100% USDVA Rating")), "Validate 'Start Date' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.endDateOnDetailPage)), objDisabledVeteransPage.removeZeroInMonthAndDay(dataMap2.get("End Date Of Rating")), "Validate 'End Date' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		
		//Step12: Validate the 'Record Ids' and 'Exemption Names' doesn't match for both the Exemptions created
		softAssert.assertTrue(recordId1 != recordId2, "Exemption records for same Veteran with non-overlapping dates are created with different Record Ids");
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T532: Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date earlier than Start Date of original record (having End Date of Rating as Blank");
	
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 Below test case is used to validate that, 
	 -Another Exemption can be created for a Veteran if End Date of second Exemption is set to a date earlier than Start Date of original record (With End Date of Rating)
	 -Another Exemption can be created for a Veteran if Start Date of second Exemption (With End Date of Rating) is set to a date later than End Date of original record
	 -'Status' field on Exemption record is read-only
	 **/
    
	@Test(description = "SMAB-T533: Disabled Veteran - Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date earlier than Start Date of original record (End Date of Rating is not blank), SMAB-T534: Disabled Veteran - Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of Rating of original record), SMAB-T609: Disabled Veteran - Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of original record and status of original record is marked as Inactive, SMAB-T659: Disabled Veteran - Verify user is not able to view 'Status' field as editable field on Exemption record", groups = {"regression", "DisabledVeteran"}, dataProvider = "loginUsers", alwaysRun = true)
	public void duplicateExemptionWithNonOverlappingDates2(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		dataMap1 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithRequiredFields");
		dataMap1.put("Veteran Name", dataMap1.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));
		
		//Step4: Create an Exemption record
		objDisabledVeteransPage.createExemptionWithEndDateOfRating(dataMap1);
		
		//Step5: Capture the Record Id and Exemption Name. Also, validate some of its details
		recordId1 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage)), dataMap1.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranSSNOnDetailPage)).substring(7), dataMap1.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption record");
		
		//Step6: Validate that the status of Exemption is saved as 'Inactive' as 'End Date of Rating' on the Exemption is a past date
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.statusOnDetailPage)), "Inactive", "SMAB-T643: Verify that User is able to validate Exemption 'Status' based on the 'End Date of Rating' for the Exemption record");
		
		//Step7: Validate that the 'Status' is read-only field on Exemption record
		softAssert.assertTrue(objDisabledVeteransPage.editExemptionAndValidateEnabledStatusOnDetailPage("Status", driver).equals("true"), "SMAB-T659: Verify user is not able to view 'Status' field as editable field on Exemption record");
		
		//Step8: Navigate back to Exemption module and create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		dataMap2 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithDatesEarlierThanOriginalRecord");
		dataMap2.put("Veteran Name", dataMap1.get("Veteran Name"));
		
		//Step9: Create another Exemption
		objDisabledVeteransPage.createExemptionWithEndDateOfRating(dataMap2);
		
		//Step10: Capture the Record Id and Exemption Name. Also, validate some of its details
		recordId2 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage)), dataMap2.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranSSNOnDetailPage)).substring(7), dataMap2.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption record with End Date set to a date earlier than Start Date of initial record");
		
		//Step11: Validate the 'Record Ids' and 'Exemption Names' doesn't match for both the Exemptions created
		softAssert.assertTrue(recordId1 != recordId2, "Exemption records for same Veteran with non-overlapping dates are created with different Record Ids");
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T533: Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date earlier than Start Date of original record (End Date of Rating is not blank)");
	
		//Step12: Navigate back to Exemption module and create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		dataMap3 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithDatesLaterThanOriginalRecordWithEndDateOfRating");
		dataMap3.put("Veteran Name", dataMap1.get("Veteran Name"));
		
		//Step13: Create the third Exemption record
		objDisabledVeteransPage.createExemptionWithEndDateOfRating(dataMap3);
				
		//Step14: Capture the Record Id and Exemption Name. Also, validate some of its details
		recordId3 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName3 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage)), dataMap3.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record with Start Date set to a date later than End Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranSSNOnDetailPage)).substring(7), dataMap3.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption record with Start Date set to a date later than End Date of initial record");
			
		//Step15: Validate the 'Record Ids' and 'Exemption Names' doesn't match for 2nd and 3rd Exemption record created
		softAssert.assertTrue(recordId2 != recordId3, "Exemption records for same Veteran with non-overlapping dates are created with different Record Ids");
		softAssert.assertTrue(exemptionName2 != exemptionName3, "SMAB-T534: Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of Rating of original record");
			
		objApasGenericFunctions.logout();
	}
	
	
	/**
	 Below test case is used to validate that,
	 -Another Exemption can be created for a Veteran if Start Date of second Exemption (With No End Date of Rating) is set to a date later than End Date of original record
	 **/
	
	@Test(description = "SMAB-T534: Disabled Veteran - Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of Rating of original record)", groups = {"regression", "DisabledVeteran"}, dataProvider = "loginUsers", alwaysRun = true)
	public void duplicateExemptionWithNonOverlappingDates3(String loginUser) throws Exception {
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericFunctions.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		
		//Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and append the Veteran Name with some random number to make it unique
		dataMap1 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithEndDate");
		dataMap1.put("Veteran Name", dataMap1.get("Veteran Name") + objDisabledVeteransPage.getRandomIntegerBetweenRange(100, 10000));
		
		//Step4: Create an Exemption record
		objDisabledVeteransPage.createExemptionWithEndDateOfRating(dataMap1);
		
		//Step5: Capture the Record Id and Exemption Name. Also, validate some of its details
		recordId1 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName1 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage)), dataMap1.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranSSNOnDetailPage)).substring(7), dataMap1.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption record");
		
		//Step6: Navigate back to Exemption module and create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json) and update the Veteran Name to make it same as above
		objApasGenericFunctions.searchModule(modules.EXEMPTION);
		dataMap2 = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithoutEndDate");
		dataMap2.put("Veteran Name", dataMap1.get("Veteran Name"));
		
		//Step7: Create another Exemption record
		objDisabledVeteransPage.createExemptionWithoutEndDateOfRating(dataMap2);
		
		//Step8: Capture the Record Id and Exemption Name. Also, validate some of its details
		recordId2 = objDisabledVeteransPage.getCurrentUrl(driver);
		objDisabledVeteransPage.expandIcon(objDisabledVeteransPage.expandedIconForGeneralExemptionOnDetailPage);
		exemptionName2 = objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.exemptionName));
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranNameOnDetailPage)), dataMap2.get("Veteran Name"), "Validate 'Veteran Name' in the Exemption detail record with End Date set to a date earlier than Start Date of initial record");
		softAssert.assertEquals(objPage.getElementText(objPage.waitForElementToBeVisible(objDisabledVeteransPage.veteranSSNOnDetailPage)).substring(7), dataMap2.get("Veteran SSN").substring(7), "Validate last 4 digits of 'Veteran's SSN' in the Exemption detail record with End Date set to a date earlier than Start Date of initial record");
		
		//Step9: Validate the 'Record Ids' and 'Exemption Names' doesn't match for both the Exemptions created
		softAssert.assertTrue(recordId1 != recordId2, "Exemption records for same Veteran with non-overlapping dates are created with different Record Ids");
		softAssert.assertTrue(exemptionName1 != exemptionName2, "SMAB-T534: Verify a duplicate Exemption record can be created when Effective Date of Rating, Date of Occupancy and End Date of Rating are set to a date later than End Date of Rating of original record)");
	
		objApasGenericFunctions.logout();
	}	

}
