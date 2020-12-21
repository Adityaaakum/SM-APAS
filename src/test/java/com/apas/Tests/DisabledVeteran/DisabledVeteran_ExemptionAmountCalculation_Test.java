package com.apas.Tests.DisabledVeteran;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ValueAdjustmentsPage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;

public class DisabledVeteran_ExemptionAmountCalculation_Test extends TestBase{

	private RemoteWebDriver driver;
	Page objPage;
	ValueAdjustmentsPage objValueAdjustmentPage;
	SoftAssertion softAssert;
	Util objUtil;
	ExemptionsPage objExemptionsPage;
	ApasGenericPage objApasGenericPage;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception {
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objUtil = new Util();
		objValueAdjustmentPage = new ValueAdjustmentsPage(driver);
		softAssert = new SoftAssertion();
		objApasGenericPage = new ApasGenericPage(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		objApasGenericPage.updateRollYearStatus("Closed", "2020");
	}
	
	/**
	 * Below test case will verify if 
	 * 1. 'Basic Exemption Amount' Calculated is correct for each VA(Current + Past years)
	 * 2. Net Exemption Amount is correct for each VA(Current + Past Years)
	 **/
	@Test(description = "SMAB-T1213, SMAB-T612: Verify Basic & Net Exemption Amount for each 'Active' Value Adjustemnt", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {"smoke", "regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyBasicExemptionAmount(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		objApasGenericPage.login(loginUser);
		
		//Step2: Open the Exemption module
		objApasGenericPage.searchModule(modules.EXEMPTION);
				
		/*Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record
		 Capture the Exemption Name	*/
		String mandatoryExemptionData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;	
		Map<String, String> createExmeptiondataMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFields");
		createExmeptiondataMap.put("Veteran Name", createExmeptiondataMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		objExemptionsPage.createExemption(createExmeptiondataMap);
		driver.navigate().refresh();
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		
		ReportLogger.INFO("Exemption: "+exemptionName + " is created");
		objPage.waitUntilPageisReady(driver);	
		String endDateOfrating=objExemptionsPage.endDateOfRatingOnExemption.getText();
		String maxDate=DateUtil.determineMaxDate(objExemptionsPage.dateAquiredPropertyExemptionDetails.getText(),objExemptionsPage.dateOccupyPropertyExemptionDetails.getText(),objExemptionsPage.effectiveDateOfUSDVAExemptionDetails.getText());
		String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy");
		String currentRollYear=ExemptionsPage.determineRollYear(currentDate);
		int actualVAtoBeCreated=objValueAdjustmentPage.verifyValueAdjustments(maxDate, endDateOfrating, currentRollYear);	
		
		//Step4: Selecting the Value Adjustment Related List Tab		
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step5: Calculate and verify Total number of Value Adjustments in an Exemption		
		 //objPage.waitUntilElementIsPresent(objValueAdjustmentPage.xPathStatus,50);
		 int noOfVAs =  objValueAdjustmentPage.numberOfValueAdjustments.size(); 
		 softAssert.assertEquals(noOfVAs, actualVAtoBeCreated, "SMAB-T1213 : Verify Number of Value Adjustments");
		   
		  //Step6: Looping through each Value Adjustments to calculate Exemption Amount 
		  for (int VARowNo = 0; VARowNo<noOfVAs; VARowNo++) { 			 				  
			//Step7: Clicking on 'Active' Value Adjustment link
			driver.navigate().refresh();
			String xpPathActiveVA = "//div//tr["+(VARowNo+1)+"]//span[contains(text(),'Active')]//..//..//preceding-sibling::th//a";
			objPage.waitUntilElementIsPresent(xpPathActiveVA,50);
			WebElement vaLink = objPage.locateElement(xpPathActiveVA,10);
			
			String vANAme = vaLink.getText();
			ReportLogger.INFO("Clicking on Value Adjustment Link: "+ vANAme);
			objPage.Click(vaLink);
			objPage.waitUntilPageisReady(driver);		
			
			//Step8: Calculate Basic Exemption Amount in an 'Active' Value Adjustment					  
			float expectedExemptionAmount = objValueAdjustmentPage.calculateBasicExemptionAmount();
			String exemptionAmount = objPage.getElementText(objPage.waitForElementToBeVisible(objValueAdjustmentPage.exemptionAmountCalculatedValueLabel));
			float actualExemptionAmount = objApasGenericPage.convertToFloat(exemptionAmount); 		 
			ReportLogger.INFO("Verifying Exemption Amount Calculated");
			softAssert.assertEquals(actualExemptionAmount,expectedExemptionAmount,"SMAB-T1213: Verify Exemption Amount calculated for each eligible year if the Determination is 'Basic'");
			   
			//Step9: Calculate Net Exemption Amount in an 'Active' Value Adjustment			
			ReportLogger.INFO("Verifying Net Exemption Amount");
			float expectedNetExemptionAmount = objValueAdjustmentPage.calculateNetExemptionAmount(actualExemptionAmount);				  
			float actualNetExemptionAmount = objApasGenericPage.convertToFloat(objValueAdjustmentPage.netExemptionAmountCalculatedValueLabel.getText());			 			  
			softAssert.assertEquals(actualNetExemptionAmount,expectedNetExemptionAmount,"SMAB-T612: Verify Net Exemption Amount calculated for each eligible year & multiple retroactive years if Penalty is applied");
			driver.navigate().back();		  
		  }	 
		 objApasGenericPage.logout();
	}
	
	
	/**
	 * Below test case will verify if 
	 * 1.'Low Income Exemption Amount' Calculated is correct for each VA(Current + Past years)
	 * 2. User can successfully Edit Value Adjustemnt record
	 * 3. User Can Modify 'Determination' Field in VA from'Basic' to Low-Income'
	 * 4. Net Exemption Amount of Low Income VAR that results in penalty
	 **/
	
  @Test(description = "SMAB-T573, SMAB-T512, SMAB-T475,SMAB-T1292: Verify Low Income Exemption Amount for each 'Active' Value Adjustemnt", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {"regression","DisabledVeteranExemption" })
  	public void DisabledVeteran_verifyLowIncomeExemptionAmount(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		objApasGenericPage.login(loginUser);
		
		// Step2: Opening the Exemption module
		objApasGenericPage.searchModule(modules.EXEMPTION);
		
		/*Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
		 Create Exemption record
		 Capture the Exemption Name*/
		String mandatoryExemptionData = System.getProperty("user.dir") + testdata.ANNUAL_PROCESS_DATA;	
		Map<String, String> createExmeptiondataMap = objUtil.generateMapFromJsonFile(mandatoryExemptionData, "DataToCreateExemptionWithMandatoryFields");
		createExmeptiondataMap.put("Veteran Name", createExmeptiondataMap.get("Veteran Name").concat(java.time.LocalDateTime.now().toString()));
		objExemptionsPage.createExemption(createExmeptiondataMap);
		driver.navigate().refresh();
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		
		ReportLogger.INFO("Exemption: "+exemptionName + " is created");
		objPage.waitUntilPageisReady(driver);		
		
		//Step4: Selecting the VA Related List Tab & then navigating to VA List View
		objValueAdjustmentPage.navigateToVAListViewInExemption();
		
		//Step5: Wait for VA List View to be visible	
		//objPage.waitUntilElementIsPresent(objValueAdjustmentPage.xPathStatus,50);
				  
		// Step6: Click on Active VA having Determination "Basic Disabled Veterans" 
		objPage.waitForElementToBeClickable(objValueAdjustmentPage.activeBasicDetVA, 10);
		String vAName = objValueAdjustmentPage.activeBasicDetVA.getText();
		objPage.Click(objValueAdjustmentPage.activeBasicDetVA);

		// Step7: Modify 'Determination' of Value Adjustment from Basic to Low Income 
		objPage.waitUntilElementIsPresent(objValueAdjustmentPage.xPathRollYearLowIncomeThresholdAmount,50);
		
		// Step8: Get Low Income Threshold Amount to modify VA  
		String lowIncomeThreshholdAmount = objValueAdjustmentPage.rollYearLowIncomeThreshholdAmountLabel.getText();		
		String totalAnnualHouseHoldIncome = (lowIncomeThreshholdAmount.substring(1, lowIncomeThreshholdAmount.length())).replaceAll(",", "");
		
		// Step9: Get Yesterday's date to modify VA
		String date = DateUtil.getCurrentDate("MM/dd/yyyy");
		String currentDate = DateUtil.getFutureORPastDate(date, -1, "MM/dd/yyyy");
		Thread.sleep(1000);
		
		// Step10: Click on 'Edit' Button to modify VA
		objPage.Click(objValueAdjustmentPage.editButton);	
		
		// Step11: Enter yesterday's date in Annual Form Received Date field to modify VA
		objPage.enter(objValueAdjustmentPage.annualFormReceivedDateEditBox,currentDate);
		
		// Step12: Enter Low Income Threshold Amount in Total Annual HouseHold Income field to modify VA
		//objValueAdjustmentPage.totalAnnualHouseholdIncomeEditBox.clear();	
		objPage.enter(objValueAdjustmentPage.totalAnnualHouseholdIncomeEditBox,totalAnnualHouseHoldIncome);
		ReportLogger.INFO("Modifying Determination of VA from 'Basic' to 'Low-Income'");	

		// Step13: Click on 'Save' Button to modify VA
		objPage.Click(objValueAdjustmentPage.saveButton);	
		Thread.sleep(2000);
		
		// Step14: Get the success alert text after modifying VA
		String actualSuccessAlertText = objValueAdjustmentPage.successAlretText();
		
		// Step15:Verify success alert text after modifying VA
		softAssert.assertEquals(actualSuccessAlertText,"Value Adjustments \"" +vAName+ "\" was saved.","SMAB-T512: Verify Determination modified from 'Basic' to 'Low-Income'");
		softAssert.assertEquals(actualSuccessAlertText,"Value Adjustments \"" +vAName+ "\" was saved.","SMAB-T475: Verify User is able to Edit Value Adjustment successfully'");
		  
		// Step16: Calculate and Verify Exemption Amount in an 'Active' Low Income Value Adjustment
		float expectedExemptionAmount = objValueAdjustmentPage.calculateLowIncomeExemptionAmount();
		Float actualExemptionAmount = objApasGenericPage.convertToFloat(objValueAdjustmentPage.exemptionAmountCalculatedValueLabel.getText());
		ReportLogger.INFO("Verifying Exemption Amount Calculated for Low-Income Disabled Veterans Exemption");
		softAssert.assertEquals(actualExemptionAmount,expectedExemptionAmount,"SMAB-T573: Verify Exemption Amount calculated for eligible years & multiple retroactive years if the effective date of rating is prior to current tax year, Exemption End Date of Rating is Blank & Determination is 'Low Income'");
			 		  
		//Step17: Verify Net Exemption Amount for Low Income VAR that results in penalty
		ReportLogger.INFO("Verifying Net Exemption Amount Calculated for Low-Income Disabled Veterans Exemption that results in penalty");			
		float expectedNetExemptionAmount = objValueAdjustmentPage.calculateNetExemptionAmount(actualExemptionAmount);					  
		float actualNetExemptionAmount = objApasGenericPage.convertToFloat(objValueAdjustmentPage.netExemptionAmountCalculatedValueLabel.getText());			 			  
		softAssert.assertEquals(actualNetExemptionAmount,expectedNetExemptionAmount,"SMAB-T1292: Verify 'Net Exemption Amount' is calculated approapriately when a Low Income VAR is created that results in a penalty");
		   
		objApasGenericPage.logout();
	 }
  
  /**
	 * Below test case will verify if 
	 * 1. on editing VAR, the Start and End Date fields are read-only
	 * 2."Not Qualified" value does not appear in the Determination field
	 * 3.'Value Adjustment Name' attribute is displayed on VAR
	 * 4. on editing VAR,the user cannot enter a value into the Penalty Adjustment Reason 
	 *    or Penalty Adjustment Other Reason Detail when the Penalty Amount - User Adjusted is blank or $0
	 **/
	@Test(description = "SMAB-T1134, SMAB-T1266, SMAB-T1267, SMAB-T1268: Verify validations on Value Adjustemnt", dataProvider = "loginExemptionSupportStaff",dataProviderClass = DataProviders.class, groups = {"smoke", "regression","DisabledVeteranExemption" })
	public void DisabledVeteran_verifyValidationsOnVA(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through
		objApasGenericPage.login(loginUser);
		
		//Step2: Fetch Value Adjustment Name
		String VAName = objValueAdjustmentPage.fetchVA();
		Thread.sleep(1000);
		
		//Step3: Searching and selecting the Exemption
		objApasGenericPage.searchModule(modules.VALUE_ADJUSTMENTS);
		objApasGenericPage.globalSearchRecords(VAName);		
		
		//Step4: Verify 'Value Adjustment Name' attribute is displayed on VAR
		objPage.waitForElementToBeVisible(objValueAdjustmentPage.vAnameLabel,50);
		boolean vANameVisible = objPage.verifyElementVisible(objValueAdjustmentPage.vAnameLabel);
		objPage.waitForElementToBeClickable(objValueAdjustmentPage.vAnameValue,30);
		String vANameValue = objPage.getElementText(objValueAdjustmentPage.vAnameValue);			
		
		softAssert.assertTrue(vANameVisible, "SMAB-T1134: 'Value Adjustment Name' attribute is displayed on VAR");
		softAssert.assertEquals(vANameValue,VAName,"SMAB-T1134: 'Value Adjustment Name' attribute's value is displayed on VAR");
		
		 //Step5: Verify 'Start Date & End Date' fields are read only on VAR
		 objPage.Click(objValueAdjustmentPage.editButton);
		 
		 objPage.waitForElementToBeClickable(objValueAdjustmentPage.startDateReadOnlyField,20);
		 boolean startDateReadOnly = objPage.verifyElementVisible(objValueAdjustmentPage.startDateReadOnlyField);
		 objPage.waitForElementToBeClickable(objValueAdjustmentPage.endDateReadOnlyField,20);
		 boolean endDateReadOnly = objPage.verifyElementVisible(objValueAdjustmentPage.endDateReadOnlyField);
		 softAssert.assertTrue(startDateReadOnly, "SMAB-T1267: Verify while editing VAR, the Start Date field is read-only");
		 softAssert.assertTrue(endDateReadOnly, "SMAB-T1267: Verify while editing VAR, the End Date field is read-only");
		  
		//Step6: Verify "Not Qualified" value does not appear in the Determination field of VAR
		objApasGenericPage.selectOptionFromDropDown("Determination", "Basic Disabled Veterans Exemption");
		List<WebElement> determinationOptions = objValueAdjustmentPage.determinationFieldValuesList;
		String expectedDetermination = "Not Qualified";
		boolean detValueFound = true;
		for(WebElement determinationValue: determinationOptions) {
		 if(determinationValue.getAttribute("title").equals(expectedDetermination)) {
			 detValueFound=false;
		 }
		}
		softAssert.assertTrue(detValueFound, "SMAB-T1266: Verify 'Not Qualified' value does not appear in the Determination field of VAR");
		
		//Step7: Verify on editing VAR,the user cannot enter a value into the Penalty Adjustment Reason or Penalty Adjustment Other Reason Detail when the Penalty Amount - User Adjusted is blank or $0
		objApasGenericPage.selectOptionFromDropDown(objValueAdjustmentPage.penaltyAdjustmentReason, "Supervisory Judgement");
		objPage.enter(objValueAdjustmentPage.penaltyAdjustmentOtherReasonDetail, "Testing automation");
		objPage.Click(objValueAdjustmentPage.saveButton);
		
		String expectedPenaltyAdjstmntErrorMsg = "Penalty Adjustment Reason is not allowed when the Penalty Amount - User Adjusted is blank.";
		String actualPenaltyAdjstmntErrorMsg = objApasGenericPage.getIndividualFieldErrorMessage("Penalty Adjustment Reason");
		softAssert.assertEquals(actualPenaltyAdjstmntErrorMsg, expectedPenaltyAdjstmntErrorMsg, "SMAB-T1268: Verify on editing VAR,the user cannot enter a value into the Penalty Adjustment Reason when the Penalty Amount - User Adjusted is blank or $0");		 
		
		String expectedPenaltyAdjstmntReasonDetailErrorMsg = "Penalty Adjustment Other Reason Detail is allowed when the Penalty Adjustment Reason is 'Other'.";
		String actualPenaltyAdjstmntReasonDetailErrorMsg = objApasGenericPage.getIndividualFieldErrorMessage("Penalty Adjustment Other Reason Detail");
		softAssert.assertEquals(actualPenaltyAdjstmntReasonDetailErrorMsg, expectedPenaltyAdjstmntReasonDetailErrorMsg, "SMAB-T1268: Verify on editing VAR,the user cannot enter a value into the Penalty Adjustment Other Reason Detail when the Penalty Amount - User Adjusted is blank or $0");
		
		objPage.Click(objValueAdjustmentPage.cancelButton);
		objApasGenericPage.logout();
	} 
	
}
  
  
    