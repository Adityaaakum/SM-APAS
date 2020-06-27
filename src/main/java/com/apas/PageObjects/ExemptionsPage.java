package com.apas.PageObjects;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
//import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.apas.Assertions.SoftAssertion;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class ExemptionsPage extends ApasGenericPage {
	Logger logger;
	Page objPage;
	SoftAssertion softAssert1;
	ApasGenericFunctions apasGenericObj;
	ApasGenericPage objApasGenericPage;
	String exemptionFileLocation = "";
	
	
	public ExemptionsPage(RemoteWebDriver driver) {
		
		super(driver);
		PageFactory.initElements(driver, this);
		logger = Logger.getLogger(LoginPage.class);
		objPage=new Page(driver);
		softAssert1=new SoftAssertion();
		apasGenericObj= new ApasGenericFunctions(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		
	}

	
	@FindBy(xpath = "//div[@class='pageLevelErrors']//li")
	public WebElement errorMessage;

	@FindBy(xpath="//div[@class='genericNotification']/following-sibling::ul/li/a")
	public WebElement genericErrorMsg;
	
	@FindBy(xpath="//h2[contains(.,'Edit Exemption')]")
	public WebElement editExemptionHeader;
	
	@FindBy(xpath = "//button[@title='Cancel']")
	public WebElement cancelButton;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@title = 'New']")
	public WebElement newExemptionButton;

	@FindBy(xpath = "//input[@title='Search Parcels']")
	public WebElement apn;

	@FindBy(xpath = "//label[contains(.,'Date Application Received')]/following::input[1]")
	public WebElement dateApplicationReceived;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//div[contains(.,'Date Application Received')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement dateApplicationReceivedExemptionDetails;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//div[contains(.,'Grace End Date')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement graceEndDateExemptionDetails;
	
	@FindBy(xpath = "//input[@title='Search Assessees']")
	public WebElement claimantName;

	@FindBy(xpath = "//label[contains(.,\"Claimant's SSN\")]/following::input[1]")
	public WebElement claimantSSN;

	@FindBy(xpath = "//label[contains(.,\"Spouse's Name\")]/following::input[1]")
	public WebElement spouseName;

	@FindBy(xpath = "//label[contains(.,\"Spouse's SSN\")]/following::input[1]")
	public WebElement spouseSSN;

	@FindBy(xpath = "//span[@data-aura-class='uiPicklistLabel' and contains(.,'Unmarried Spouse of Deceased Veteran?')]/following::div[1]//div/a")
	public WebElement unmarriedSpouseOfDisabledVeteran;

	@FindBy(xpath = "//label[contains(.,'Date of Death of Veteran')]/following::input[1]")
	public WebElement dateOfDeathOfVeteran;

	@FindBy(xpath = "//label[contains(.,\"Veteran's Name\")]/following::input[1]")
	public WebElement veteranName;

	@FindBy(xpath = "//label[contains(.,\"Veteran's SSN\")]/following::input[1]")
	public WebElement veteranSSN;

	@FindBy(xpath = "//label[contains(.,'Date Acquired Property')]/following::input[1]")
	public WebElement dateAquiredProperty;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//div[contains(.,'Date Acquired Property')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement dateAquiredPropertyExemptionDetails;
	
	@FindBy(xpath="//div[@class='windowViewMode-normal oneContent active lafPageHost']//li[@title='Details']//a[@data-tab-value='detailTab']")
	public WebElement exemptionDetailsTab;

	@FindBy(xpath = "//label[contains(.,'Date Occupied/Intend to Occupy Property')]/following::input[1]")
	public WebElement dateOccupyProperty;
	
	@FindBy(xpath = "//label[contains(.,'Date of Notice of 100% Rating')]/following::input[1]")
	public WebElement dateOfNoticeOfRating;
	
	@FindBy(xpath = "//label[contains(.,'Date Acquired Property')]/following::input[1]")
	public WebElement dateAcquiredProperty;
	
	@FindBy(xpath = "//button[@title='Save']")
	public static WebElement saveButton;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//div[contains(.,'Date Occupied/Intend to Occupy Property')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement dateOccupyPropertyExemptionDetails;

	@FindBy(xpath = "//span[@data-aura-class='uiPicklistLabel' and contains(.,'DV Exemption on Prior Residence')]/following::div[1]//div/a")
	public WebElement dvExemptionOnPriorResidence;

	@FindBy(xpath = "//label[contains(.,'Date Moved From Prior Residence')]/following::input[1]")
	public WebElement dateMoveFromProprResidence;

	@FindBy(xpath = "//label[contains(.,'Prior Residence Street Address')]/following::input[1]")
	public WebElement priorResidenceStreetAddress;

	@FindBy(xpath = "//label[contains(.,'Prior Residence City')]/following::input[1]")
	public WebElement priorResidenceCity;

	@FindBy(xpath = "//span[@data-aura-class='uiPicklistLabel' and contains(.,'Prior Residence State')]/following::div[1]//div/a")
	public WebElement priorResidenceState;

	@FindBy(xpath = "//label[contains(.,'Prior Residence County')]/following::input[1]")
	public WebElement priorResidenceCounty;

	@FindBy(xpath = "//label[contains(.,'Effective Date of 100% USDVA Rating')]/following::input[1]")
	public WebElement effectiveDateOfUSDVA;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//div[contains(.,'Effective Date of 100% USDVA Rating')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement effectiveDateOfUSDVAExemptionDetails;

	@FindBy(xpath = "//a[@title='Select List View']")
	public WebElement selectlistView;
	
	@FindBy(xpath = "//li[2]//a[contains(.,'All')]")
	public WebElement allView;
	
	@FindBy(xpath = "//label[contains(.,'Date of Notice of 100% Rating')]/following::input[1]")
	public WebElement dateOfNotice;

	@FindBy(xpath = "//label[contains(.,'Email Address')]/following::input[1]")
	public WebElement claimanatEmailAddress;

	@FindBy(xpath = "//label[contains(.,'Telephone')]/following::input[1]")
	public WebElement claimantTelephone;

	@FindBy(xpath = "//span[contains(.,'Qualification?')]/following-sibling::div[@class='uiMenu']")
	public WebElement qualification;
	
	@FindBy(xpath = "//span[contains(.,'Reason for Not Qualified')]/following-sibling::div[@class='uiMenu']")
	public WebElement reasonNotQualified;
	
	@FindBy(xpath = "//label[contains(.,'Not Qualified Detail')]/following-sibling::textarea")
	public WebElement notQualifiedDetail;

	@FindBy(xpath = "//span[@data-aura-class='uiPicklistLabel' and contains(.,'End Rating Reason')]/following::div[1]//div/a")
	public WebElement endRatingReason;
	
	@FindBy(xpath = "//label[contains(.,'End Date of Rating')]/following::input[1]")
	public WebElement endDateOfRating;

	@FindBy(xpath = "//div[text()='Basis for Claim']//following::button[@title='Move selection to Chosen'][1]")
	public WebElement basisForClaim;

	@FindBy(xpath = "//div[text()='Deceased Veteran Qualification']//following::button[@title='Move selection to Chosen']")
	public WebElement deceasedVeteranQualification;
	
	@FindBy(xpath = "//div[contains(.,\"Veteran's SSN\")]/following-sibling::ul/li[@class='form-element__help']")
	public WebElement veteranSSNErrorMsg;
	
	@FindBy(xpath = "//div[contains(.,\"Spouse's SSN\")]/following-sibling::ul/li[@class='form-element__help']")
	public WebElement spouseSSNErrorMsg;
	
	@FindBy(xpath = "//div[contains(.,'Date of Death of Veteran')]/following-sibling::ul/li[@class='form-element__help']")
	public WebElement dateOfDeathOfVeteranErrorMsg;
	
	@FindBy(xpath = "//div[contains(.,'Deceased Veteran Qualification')]/following-sibling::span[starts-with(@id,'error-message')]")
	public WebElement deceasedVeteranQualificationErrormsg;
	
	@FindBy(xpath = "//div[contains(.,'Date of Notice of 100% Rating')]/following-sibling::ul/li")
	public WebElement effectiveDateOfUSDVAErrorMsg;
	
	@FindBy(xpath = "//div[contains(.,'End Date of Rating')]/following-sibling::ul/li")
	public WebElement enddateOfRatingErrorMsg;
	
	@FindBy(xpath = "//div[contains(.,'Reason for Not Qualified')]/following-sibling::ul/li")
	public WebElement reasonForNotQualifiedErrorMsg;
	
	@FindBy(xpath = "//div[contains(.,'Not Qualified Detail')]/following-sibling::ul/li")
	public WebElement notQualifiedDetailErrorMsg;
	
	@FindBy(xpath="//div[@class='windowViewMode-normal oneContent active lafPageHost']//button[@name='Edit']")
	public WebElement editExemption;

	@FindBy(xpath="//div[@class='windowViewMode-normal oneContent active lafPageHost']//button[@title='Edit End Date of Rating']/preceding-sibling::span//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement endDateOfRatingOnExemption;
	
	@FindBy(xpath="//div[@class='windowViewMode-normal oneContent active lafPageHost']//ul//li[contains(@title,'Business Events')]")
	public WebElement businessEvent;
	
	@FindBy(xpath = "//table//tr[1]//th[@scope='row']//a")
	public WebElement newRecord;
	
	@FindBy(xpath="//li[@class='form-element__help']")
	public List<WebElement> fieldsErrorMsg;
	
	@FindBy(xpath="//li[@class='form-element__help']")
	public WebElement fieldErrorMsg;
	
	@FindBy(xpath="//div[@class='windowViewMode-normal oneContent active lafPageHost']//div[contains(text(),'Exemption')]//following-sibling::slot/slot/lightning-formatted-text")
	public WebElement newExemptionNameAftercreation;
	
	@FindBy(xpath="//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(text(),'Status')]/parent::div/following-sibling::div//span//slot[@slot='outputField']//lightning-formatted-text")
	public WebElement exemationStatusOnDetails;
	
	@FindBy(xpath="//div[@class='windowViewMode-normal oneContent active lafPageHost']//slot[@name='sidebar']//ul[@role='tablist']//li[not(contains(@style,'visibility: hidden;'))]")
	public List<WebElement> rightSideTopSectionsOnUI;
	
	@FindBy(xpath="//div[@class='windowViewMode-normal oneContent active lafPageHost']//ul[@class='tabs__nav']//li/a//span[@class='title']")
	public List<WebElement> rightSideBottomSectionsOnUI;
	
	@FindBy(xpath="//div[@class='windowViewMode-normal oneContent active lafPageHost']//label[contains(text(),'Qualification?')]/parent::lightning-combobox//div[contains(@id,'help-text')]")
	public WebElement QualificationOnDetailsPageErrorMsg;
	
	
	//////////////////////////New locators////////////////////////////////
	
	@FindBy(xpath = "//label[contains(.,\"Veteran's Name\")]/following::input[1]")
	public WebElement nameOfVeteran;

	@FindBy(xpath = "//label[contains(.,'Email Address')]/following::input[1]")
	public WebElement emailAddress;

	@FindBy(xpath = "//label[contains(.,'Telephone')]/following::input[1]")
	public WebElement telephone;
	
	@FindBy(xpath = "//label[contains(.,'Date Occupied/Intend to Occupy Property')]/following::input[1]")
	public WebElement dateOccupiedProperty;

	@FindBy(xpath = "//span[contains(text(),'Blind In Both Eyes')]")
	public WebElement basisForClaim1;
	
	@FindBy(xpath = "//span[contains(text(),'Disabled Because Of Loss Of Use Of 2 Or More Limbs')]")
	public WebElement basisForClaim2;
	
	@FindBy(xpath = "//div[text()='Basis for Claim']//following::button[@title='Move selection to Chosen'][1]")
	public WebElement basisForClaimButton;
	
	@FindBy(xpath = "//button[text() = 'Edit']")
	public WebElement editButton;
	
	@FindBy(xpath = "//div[@role='menu']//li[@class='uiMenuItem']/a[@title = 'Edit']")
	public WebElement editMenuItemButton;
	
	@FindBy(xpath = "//lightning-formatted-text[contains(text(),'EXMPTN-')]")
	public WebElement exemptionName;
	
	
	//Locators added for elements on Exemption screen - Detail Page
	
	@FindBy(xpath = "//span[text() =" + "\"" + "Veteran" + "'s" + " Name" + "\"" + "]//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement veteranNameOnDetailPage;
	
	@FindBy(xpath = "//span[text() =" + "\"" + "Veteran" + "'s" + " SSN" + "\"" + "]//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement veteranSSNOnDetailPage;
	
	@FindBy(xpath = "//span[text() =" + "\"" + "Claimant" + "'s" + " SSN" + "\"" + "]//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement claimantSSNOnDetailPage;
	
	@FindBy(xpath = "//span[text() =" + "\"" + "Claimant" + "'s" + " Name" + "\"" + "]//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement claimantNameOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Start Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement startDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'End Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement endDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Status']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement statusOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'APN']//parent::div//following-sibling::div//div[@class='slds-grid']")
	public WebElement parcelOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Date Acquired Property']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement dateAcquiredOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Date Occupied/Intend to Occupy Property']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement dateOccupiedOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Date of Notice of 100% Rating']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement dateOfNoticeOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Effective Date of 100% USDVA Rating']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement effectiveDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'End Date of Rating']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement endDateOfRatingOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'End Rating Reason']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement endRatingReasonOnDetailPage;
	
	@FindBy(xpath = "//button[contains(text(),'Cancel')]")
	public WebElement cancelButtonOnDetailPage;
	
	@FindBy(xpath = "//button[contains(text(),'Save')]")
	public WebElement saveButtonOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Date Application Received']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement dateOfApplicationOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Qualification?']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement qualificationOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Reason for Not Qualified']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement reasonForNotQualifiedOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'More Exemption Detail']//ancestor::button[contains(@class, 'test-id__section-header-button slds')]")
	public WebElement expandedIconForMoreExemptionOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'General Information']//ancestor::button[contains(@class, 'test-id__section-header-button slds')]")
	public WebElement expandedIconForGeneralExemptionOnDetailPage;
	
	@FindBy(xpath = "//span[text() = " + "\"" + "Veteran" + "'s" + " Name" + "\"" + "]//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]")
	public WebElement editPencilIconForVeteranNameOnDetailPage;
	
	@FindBy(xpath = "//span[text() = " + "\"" + "Veteran" + "'s" + " SSN" + "\"" + "]//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]")
	public WebElement editPencilIconForVeteranSSNOnDetailPage;
	
	@FindBy(xpath = "//span[text() = " + "\"" + "Claimant" + "'s" + " SSN" + "\"" + "]//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]")
	public WebElement editPencilIconForClaimantSSNOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'End Date of Rating']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]")
	public WebElement editPencilIconForEndDateOfRatingOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Date Application Received']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]")
	public WebElement editPencilIconForDateOfApplicationOnDetailPage;
	
	
	//Locators added for elements on Exemption screen - Edit Detail Page
	
	@FindBy(xpath = "//div[@class='slds-form-element__control slds-grow']//input[@name='Owner_Applying_SSN__c']")
	public WebElement claimantSSNOnDetailEditPage;
	
	@FindBy(xpath = "//div[@class='slds-form-element__control slds-grow']//input[@name='Name_of_Veteran__c']")
	public WebElement nameOfVeteranOnDetailEditPage;
	
	@FindBy(xpath = "//div[@class='slds-form-element__control slds-grow']//input[@name='Veteran_SSN__c']")
	public WebElement veteranSSNOnDetailEditPage;
	
	@FindBy(xpath = "//div[@class='slds-form-element__control slds-input-has-icon slds-input-has-icon_right']//input[@name='Application_Date__c']")
	public WebElement applicationDateOnDetailEditPage;
	
	@FindBy(xpath = "//h2[@class='slds-truncate slds-text-heading_medium']")
	public WebElement hitSnagOnDetailEditPage;
	
	@FindBy(xpath = "//div[@class='slds-form-element__control slds-input-has-icon slds-input-has-icon_right']//input[@name='End_Date_of_Rating__c']")
	public WebElement endDateOfRatingOnDetailEditPage;
	
	@FindBy(xpath = "//label[text() = 'End Rating Reason']//following-sibling::div[@class='slds-form-element__control']//lightning-base-combobox")
	public WebElement endRatingReasonOnDetailEditPage;
	
	
	/*	Next 4 locators are for validating error messages for duplicate Exemption or missing details
	 *	These would be moved to common package/class
	 * */
	
	@FindBy(xpath = "//li[contains(text(), 'These required fields must be completed:')]")
	public WebElement errorMsgOnTop;
	
	@FindBy(xpath = "//li[text() = 'Complete this field'] | //span[text() = 'Complete this field']")
	private List<WebElement> errorMsgUnderLabels; 
	
	@FindBy(xpath = "//li[contains(text(),'There seems to be an existing record with a blank')]")
	public WebElement duplicateErrorMsgWithBlankEndDate;
	
	@FindBy(xpath = "//li[contains(text(),'There seems to be an existing record with overlapp')]")
	public WebElement duplicateErrorMsgWithOverlappingDetails;
	
	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;
	
	public String exemptionNumber = "//lightning-formatted-text[contains(text(),'EXMPTN-')]";
	

	
	/**
	 * Description: This method is to determine the Roll Year of any given date(e.g Application received date) 
	 * @param date:  date for which to determine Roll Year
	 * @return roll Year: returns the Roll year
	 */
	
	public static String determineRollYear(String date)
	{	
		System.out.println("Determining Roll year for::"+date);
		String[] appRcvddate=date.split("/");
		String month=appRcvddate[0].replaceAll("0", "");
		int appMonth=Integer.parseInt(month);
		int rollYear=Integer.parseInt(appRcvddate[2]);
		if(appMonth>=7)
		{
		rollYear=rollYear+1;
		}	
		return String.valueOf(rollYear);
		
	}
	

/**
 * Description: This method is to select a record from list screen  
 * User should be on the respective screen
 */

public String createNewExemption(Map<String,String> newExemptionData) throws Exception {

	ExtentTestManager.getTest().log(LogStatus.INFO, "Entering/Selecting values for New Exemption record");
	apasGenericObj.searchAndSelectFromDropDown(apn,fetchActiveAPN());
	objPage.enter(dateApplicationReceived, newExemptionData.get("DateApplicationReceived"));
	apasGenericObj.searchAndSelectFromDropDown(claimantName,fetchAssesseeName());
	objPage.enter(claimantSSN, newExemptionData.get("ClaimantSSN"));
	objPage.enter(spouseName, newExemptionData.get("SpouseName"));
	objPage.enter(spouseSSN, newExemptionData.get("SpouseSSN"));
	apasGenericObj.selectFromDropDown(unmarriedSpouseOfDisabledVeteran, newExemptionData.get("UnmarriedSpouseOfDisabledVeteran"));
	objPage.enter(dateOfDeathOfVeteran, newExemptionData.get("DateOfDeathOfVeteran"));
	objPage.enter(veteranName, newExemptionData.get("VeteranName").concat(java.time.LocalDateTime.now().toString()));
	objPage.enter(veteranSSN, newExemptionData.get("VeteranSSN"));
	objPage.enter(dateAquiredProperty, newExemptionData.get("DateAquiredProperty"));
	objPage.enter(dateOccupyProperty, newExemptionData.get("DateOccupyProperty"));
	apasGenericObj.selectFromDropDown(dvExemptionOnPriorResidence, newExemptionData.get("DVExemptionOnPriorResidence"));
	objPage.enter(dateMoveFromProprResidence, newExemptionData.get("DateMovedPriorResidence"));
	objPage.enter(priorResidenceStreetAddress, newExemptionData.get("PriorResidenceStreet"));
	objPage.enter(priorResidenceCity, newExemptionData.get("PriorResidenceCity"));
	apasGenericObj.selectFromDropDown(priorResidenceState, newExemptionData.get("PriorResidenceState"));
	objPage.enter(priorResidenceCounty, newExemptionData.get("PriorResidenceCounty"));
	objPage.enter(effectiveDateOfUSDVA, newExemptionData.get("EffectiveDateOfUSDVA"));
	objPage.enter(dateOfNotice, newExemptionData.get("DateOfNotice"));
	apasGenericObj.selectMultipleValues(newExemptionData.get("BasisForClaim"), "Basis for Claim");
	objPage.enter(claimanatEmailAddress, newExemptionData.get("EmailAddress"));
	objPage.enter(claimantTelephone,newExemptionData.get("Telephone"));
	apasGenericObj.selectMultipleValues(newExemptionData.get("DeceasedVeteranQualification"), "Deceased Veteran Qualification");
	apasGenericObj.selectFromDropDown(qualification, newExemptionData.get("Qualification"));
	objPage.enter(endDateOfRating, newExemptionData.get("EnddateOfRating"));
	apasGenericObj.selectFromDropDown(endRatingReason, newExemptionData.get("EndRatingReason"));
	objPage.Click(saveButton);
	
	//objPage.locateElement("//a[contains(.,'Value Adjustments')]", 3);
	objPage.waitForElementToBeVisible(newExemptionNameAftercreation, 20);
	ReportLogger.INFO("Created "+newExemptionNameAftercreation.getText()+" Exemption with mandatory data");
	return newExemptionNameAftercreation.getText();

	}


public String createNewExemptionWithMandatoryData(Map<String, String> newExemptionData) throws Exception {
	
	ExtentTestManager.getTest().log(LogStatus.INFO, "Entering/Selecting values for New Exemption record");
		apasGenericObj.searchAndSelectFromDropDown(apn,fetchActiveAPN());
		objPage.enter(dateApplicationReceived, newExemptionData.get("DateApplicationReceived"));
		apasGenericObj.searchAndSelectFromDropDown(claimantName,fetchAssesseeName());
		objPage.enter(claimantSSN, newExemptionData.get("ClaimantSSN"));
		objPage.enter(veteranName, newExemptionData.get("VeteranName").concat(java.time.LocalDateTime.now().toString()));
		objPage.enter(veteranSSN, newExemptionData.get("VeteranSSN"));
		objPage.enter(dateAquiredProperty, newExemptionData.get("DateAquiredProperty"));
		objPage.enter(dateOccupyProperty, newExemptionData.get("DateOccupyProperty"));
		objPage.enter(effectiveDateOfUSDVA, newExemptionData.get("EffectiveDateOfUSDVA"));
		objPage.enter(dateOfNotice, newExemptionData.get("DateOfNotice"));
		apasGenericObj.selectMultipleValues(newExemptionData.get("BasisForClaim"), "Basis for Claim");
		apasGenericObj.selectFromDropDown(qualification, newExemptionData.get("Qualification"));
		if(newExemptionData.get("Qualification").contains("Not Qualified")) {
			apasGenericObj.selectFromDropDown(reasonNotQualified, newExemptionData.get("ReasonForNotQualified"));
		}
		if(newExemptionData.containsKey("EnddateOfRatingNeeded")) {
			objPage.enter(endDateOfRating, newExemptionData.get("EnddateOfRatingNeeded"));
			apasGenericObj.selectFromDropDown(endRatingReason, newExemptionData.get("EndRatingReason"));
		}
		objPage.Click(saveButton);

		objPage.waitForElementToBeVisible(newExemptionNameAftercreation, 20);
		ReportLogger.INFO("Created "+newExemptionNameAftercreation.getText()+" Exemption with mandatory data");
	
	
	return newExemptionNameAftercreation.getText();
}



	////////////////new functions//////////////////////////////////////////


/**
 * Description: This method will enter date
 * @param element: locator of element where date need to be put in
 * @param date: date to enter
 */

public void enterDate(WebElement element, String date) throws Exception {
	Click(element);
	objApasGenericPage.selectDateFromDatePicker(date);
}


/**
 * Description: This method will select from dropdown
 * @param element: locator of element where date need to be put in
 * @param value: field value to enter
 */

public void selectFromDropDown(WebElement element, String value) throws Exception {
	Click(element);
	String xpathStr = "//div[contains(@class, 'left uiMenuList--short visible positioned')]//a[text() = '" + value + "']";
	WebElement drpDwnOption = locateElement(xpathStr, 200);
	drpDwnOption.click();
}

/**
 * Description: This method will wait for Exemption screen to load before entering values
 */

public void waitForExemptionScreenToLoad() {
	waitForElementToBeClickable(apn);
	waitForElementToBeClickable(claimantName);
}


/**
 * Description: This method will enter mandatory field values along with Non Qualified data in Exemption screen
 * @param dataMap: Map that is storing values from JSON file
 */

public void enterNonQualifiedExemptionData(Map<String, String> dataMap) throws Exception {
	String assesseeName = fetchAssesseeName();
	searchAndSelectFromDropDown(apn, dataMap.get("APN"));
	enter(dateApplicationReceived, dataMap.get("Date Application Received"));
	searchAndSelectFromDropDown(claimantName, assesseeName);
	enter(claimantSSN, dataMap.get("Claimant SSN"));
	enter(nameOfVeteran, dataMap.get("Veteran Name"));
	enter(veteranSSN, dataMap.get("Veteran SSN"));
	enterDate(dateAquiredProperty, dataMap.get("Date Acquired Property"));
	enterDate(dateOccupiedProperty, dataMap.get("Date Occupied/Intend to Occupy Property"));
	enterDate(dateOfNotice, dataMap.get("Date of Notice of 100% Rating"));
	enterDate(effectiveDateOfUSDVA, dataMap.get("Effective Date of 100% USDVA Rating"));
	addBasisForClaim(basisForClaim1, basisForClaim2, basisForClaimButton);
	selectFromDropDown(qualification, dataMap.get("Qualification?"));
	selectFromDropDownUsingTitle(reasonNotQualified, dataMap.get("Reason for Not Qualified"));
}

/**
 * Description: This method will enter mandatory field values in Exemption screen
 * @param dataMap: Map that is storing values from JSON file
 */

public void enterExemptionDataWithMandatoryField(Map<String, String> dataMap) throws Exception {
	String assesseeName = fetchAssesseeName();
	searchAndSelectFromDropDown(apn, dataMap.get("APN"));
	enter(dateApplicationReceived, dataMap.get("Date Application Received"));
	searchAndSelectFromDropDown(claimantName, assesseeName);
	enter(claimantSSN, dataMap.get("Claimant SSN"));
	enterDate(dateAquiredProperty, dataMap.get("Date Acquired Property"));
	enterDate(dateOccupiedProperty, dataMap.get("Date Occupied/Intend to Occupy Property"));
	enterDate(dateOfNotice, dataMap.get("Date of Notice of 100% Rating"));
	enterDate(effectiveDateOfUSDVA, dataMap.get("Effective Date of 100% USDVA Rating"));
	addBasisForClaim(basisForClaim1, basisForClaim2, basisForClaimButton);
	selectFromDropDown(qualification, dataMap.get("Qualification?"));			
}

/**
 * Description: This method will enter mandatory field values (along with Veteran details - Name & SSN) in Exemption screen
 * @param dataMap: Map that is storing values from JSON file
 */

public void enterExemptionData(Map<String, String> dataMap) throws Exception {
	String assesseeName = fetchAssesseeName();
	searchAndSelectFromDropDown(apn, dataMap.get("APN"));
	enter(dateApplicationReceived, dataMap.get("Date Application Received"));
	searchAndSelectFromDropDown(claimantName, assesseeName);
	enter(claimantSSN, dataMap.get("Claimant SSN"));
	enter(nameOfVeteran, dataMap.get("Veteran Name"));
	enter(veteranSSN, dataMap.get("Veteran SSN"));
	enterDate(dateAquiredProperty, dataMap.get("Date Acquired Property"));
	enterDate(dateOccupiedProperty, dataMap.get("Date Occupied/Intend to Occupy Property"));
	enterDate(dateOfNotice, dataMap.get("Date of Notice of 100% Rating"));
	enterDate(effectiveDateOfUSDVA, dataMap.get("Effective Date of 100% USDVA Rating"));
	addBasisForClaim(basisForClaim1, basisForClaim2, basisForClaimButton);
	selectFromDropDown(qualification, dataMap.get("Qualification?"));			
}

/**
 * Description: This method will enter mandatory field values (along with Veteran and End Date of Rating details) in Exemption screen
 * @param dataMap: Map that is storing values from JSON file
 */

public void enterExemptionDataWithEndDateOfRating(Map<String, String> dataMap) throws Exception {
	String assesseeName = fetchAssesseeName();
	searchAndSelectFromDropDown(apn, dataMap.get("APN"));
	enter(dateApplicationReceived, dataMap.get("Date Application Received"));
	searchAndSelectFromDropDown(claimantName, assesseeName);
	enter(claimantSSN, dataMap.get("Claimant SSN"));
	enter(nameOfVeteran, dataMap.get("Veteran Name"));
	enter(veteranSSN, dataMap.get("Veteran SSN"));
	enterDate(dateAquiredProperty, dataMap.get("Date Acquired Property"));
	enterDate(dateOccupiedProperty, dataMap.get("Date Occupied/Intend to Occupy Property"));
	enterDate(dateOfNotice, dataMap.get("Date of Notice of 100% Rating"));
	enterDate(effectiveDateOfUSDVA, dataMap.get("Effective Date of 100% USDVA Rating"));
	addBasisForClaim(basisForClaim1, basisForClaim2, basisForClaimButton);
	selectFromDropDown(qualification, dataMap.get("Qualification?"));
	enterDate(endDateOfRating, dataMap.get("End Date Of Rating"));	
	selectFromDropDown(endRatingReason, dataMap.get("End Rating Reason"));				
}

/**
 * Description: This method will enter only EndDateOfRating
 * @param dataMap: Map that is storing values from JSON file
 */

public void enterEndDateOfRating(Map<String, String> dataMap) throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Update some details in the Exemption record i.e. End Date Of Rating :  " + dataMap.get("End Date Of Rating") + " and End Rating Reason : " + dataMap.get("End Rating Reason"));
	Thread.sleep(1000);
	enterDate(endDateOfRating, dataMap.get("End Date Of Rating"));	
	Thread.sleep(1000);
	selectFromDropDown(endRatingReason, dataMap.get("End Rating Reason"));
}

/**
 * Description: This method is interchanging Date Occupied and Effective Date
 * @param dataMap: Map that is storing values from JSON file
 */

public void interchangeOccupiedAndEffectiveDate(Map<String, String> dataMap) throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Interchange values of 'Effective Date of 100% USDVA Rating' and 'Date Occupied/Intend to Occupy Property' in the Exemption record i.e. Date Occupied/Intend to Occupy Property :  " + dataMap.get("Effective Date of 100% USDVA Rating") + " and Effective Date of 100% USDVA Rating : " + dataMap.get("Date Occupied/Intend to Occupy Property"));
	enterDate(dateOccupiedProperty, dataMap.get("Effective Date of 100% USDVA Rating"));
	enterDate(effectiveDateOfUSDVA, dataMap.get("Date Occupied/Intend to Occupy Property"));	
}

/**
 * Description: This method will add values in 'Basis for Claim' field
 * @param dataMap: Map that is storing values from JSON file
 */

public void addBasisForClaim(WebElement Option1, WebElement Option2, WebElement addButton) throws Exception {
	Click(Option1);
	Click(addButton);
	Click(Option2);
	Click(addButton);		
}

/**
 * Description: This method will click SAVE button on Exemption screen
 */
public void saveExemptionRecord() throws Exception {
	Click(saveButton);
	Thread.sleep(1000);
}

/**
 * Description: This method will click EDIT button on Exemption screen
 */
public void editExemptionRecord() throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Edit' button to update it");
	Click(editButton);
	Thread.sleep(1000);
}

/**
 * Description: This method will click CANCEL button on Exemption screen
 */

public void cancelExemptionRecord() throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Cancel' button to move out of the Exemption screen");
	Click(cancelButton);
}

/**
 * Description: This method includes other methods and creates an Exemption with limited fields data and without EndDateOfRating
 * @param dataMap: Map that is storing values from JSON file
 */

public void createExemptionWithoutEndDateOfRating(Map<String, String> dataMap) throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to fill the following details in the Exemption record : " + dataMap);	
	Thread.sleep(2000);
	Click(waitForElementToBeClickable(newExemptionButton));
	waitForExemptionScreenToLoad();
	enterExemptionData(dataMap);
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Save' button to save the details entered in Exemption record");
	saveExemptionRecord();
}

/**
 * Description: This method includes other methods and creates an Exemption with limited fields data and with EndDateOfRating
 * @param dataMap: Map that is storing values from JSON file
 */

public void createExemptionWithEndDateOfRating(Map<String, String> dataMap) throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to fill the following details in the Exemption record : " + dataMap);	
	Thread.sleep(2000);
	Click(waitForElementToBeClickable(newExemptionButton));
	waitForExemptionScreenToLoad();
	enterExemptionDataWithEndDateOfRating(dataMap);
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Save' button to save the details in Exemption record");
	saveExemptionRecord();
}

/**
 * Description: This method includes other methods and creates an Exemption with Non Qualified data
 * @param dataMap: Map that is storing values from JSON file
 */

public void createNonQualifiedExemption(Map<String, String> dataMap) throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to fill the following details in the Exemption record : " + dataMap);	
	Thread.sleep(2000);
	Click(waitForElementToBeClickable(newExemptionButton));
	waitForExemptionScreenToLoad();
	enterNonQualifiedExemptionData(dataMap);
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Save' button to save the details entered in Exemption record");
	saveExemptionRecord();
}

/**
 * Description: This method includes other methods and creates an Exemption with Non Qualified data
 * @param dataMap: Map that is storing values from JSON file
 */

public void createExemptionWithMandatoryFields(Map<String, String> dataMap) throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to fill the following details in the Exemption record : " + dataMap);	
	Thread.sleep(2000);
	Click(waitForElementToBeClickable(newExemptionButton));
	waitForExemptionScreenToLoad();
	enterExemptionDataWithMandatoryField(dataMap);
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Save' button to save the details entered in Exemption record");
	saveExemptionRecord();
}

/**
 * Description: This method will fetch the current URL and process it to get the Record Id
 * @param dataMap: Map that is storing values from JSON file
 */

public void updateQualifiedData(Map<String, String> dataMap) throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Update some details in the Exemption record i.e. Qualification? :  " + dataMap.get("Qualification?") + " and Reason for Not Qualified : " + dataMap.get("Reason for Not Qualified"));
	Thread.sleep(1000);
	selectFromDropDown(qualification, dataMap.get("Qualification?"));
	Thread.sleep(1000);
	selectFromDropDownUsingTitle(reasonNotQualified, dataMap.get("Reason for Not Qualified"));
}


/**
 * Description: This method will select from dropdown for 'Reason for Not Qualified' as existing function doesn't work for this
 * @param element: locator of element where value need to be selected
 * @param value: field value to enter
 */

public void selectFromDropDownUsingTitle(WebElement element, String value) throws Exception {
	Click(element);
	String xpathStr = "//a[contains(text(),'" + value + "')]";
	WebElement drpDwnOption = locateElement(xpathStr, 200);
	drpDwnOption.click();
}

/**
 * Description: This method will open the Exemption Name passed in the argument
 * @param exempName: Takes Exemption Name as an argument
 */
public void openExemptionRecord(String exempName) throws IOException, InterruptedException {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Open the Exemption record : " + exempName);
	Click(driver.findElement(By.xpath("//a[@title='" + exempName + "']")));
	Thread.sleep(3000);
}

/**
 * Description: This method will open the Exemption
 * @param exempName: Takes Exemption Name as an argument
 * @param recordId: Takes RecordId as an argument
 */
public void openExemptionUsingLocator(String recordId, String exempName) throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Open the Exemption record : " + exempName);
	String xpathStr = "//a[@data-recordid='" + recordId + "']";
    WebElement exemptionLocator = locateElement(xpathStr, 30);
    Click(exemptionLocator);
}

/**
 * Description: This method will save an Exemption record with no values entered
 */
public void saveExemptionRecordWithNoValues() throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to open an Exemption record");
	Thread.sleep(2000);
	Click(waitForElementToBeClickable(newExemptionButton));
	waitForExemptionScreenToLoad();
	ExtentTestManager.getTest().log(LogStatus.INFO, "Without entering any data on the Exemption record, click 'Save' button");
	saveExemptionRecord();
}

/**
 * Description: This method will remove leading zeroes from the month and day value in Date
 * @param dateValue: Date value passed from the Json file
 * @return : returns the dateValue
 */

public String removeZeroInMonthAndDay(String dateValue) throws Exception {
	boolean flag = false;
	String apnd;
	
	if (dateValue.charAt(0) == '0') {
		dateValue = dateValue.substring(1);
		flag = true;
	}
	
	if (flag){
		if (dateValue.charAt(2) == '0') {
			apnd = dateValue.substring(0, 2);
			dateValue = apnd + dateValue.substring(3);
		}
	}
	else {
			if (dateValue.charAt(3) == '0') {
				apnd = dateValue.substring(0, 3);
				dateValue = apnd + dateValue.substring(4);
			}
		}
	
	return dateValue;
}

/**
 * Description: This method will determine the enabled/disabled status of the 'Status' field
 * @param fieldLabel: Field Label on Exemption detail page
 * @param driver: Driver Instance
 * @return : returns true or false
 */

public String editExemptionAndValidateEnabledStatusOnDetailPage(String fieldLabel, RemoteWebDriver driver) throws Exception {
	
	String flag = "false";
	Click(editPencilIconForVeteranNameOnDetailPage);
	
	String propertyValue = getAttributeValue(expandedIconForMoreExemptionOnDetailPage, "aria-expanded");
	if (propertyValue.equals("false")){
		Click(expandedIconForMoreExemptionOnDetailPage);
	}
	
	//Get all the read-only elements in a List
	scrollToElement(statusOnDetailPage);
	List<WebElement> field_elements = driver.findElements(By.xpath("//div[text() = 'Exemption']//ancestor::div[@class = 'slds-col slds-size_1-of-1 row region-header']//following-sibling::div[contains(@class, 'col slds-size_1-of-1 row')]//slot[@slot = 'outputField']//lightning-formatted-text//ancestor::div[@class = 'slds-form-element__control']//preceding-sibling::div"));
	ExtentTestManager.getTest().log(LogStatus.INFO, "There are " + field_elements.size() + " read only fields on Exemption record");
	
	for(WebElement fieldElement: field_elements) 
	{	
		String elementName = getElementText(waitForElementToBeVisible(fieldElement));
		if (elementName.equals(fieldLabel)){
			flag = "true";
			ExtentTestManager.getTest().log(LogStatus.INFO, "Validate 'Status' field is read only on Exemption Detail screen");
			break;
		}
	}
	Click(cancelButtonOnDetailPage);
	return flag;
}

/**
 * Description: This method will search and select the Exemption
 * @param : Exemption Name which needs to be selected
 * @throws Exception 
 */
public void searchAndSelectExemption(String exemptionName) throws Exception
{	
	
	// Step1: Opening the Exemption module
	apasGenericObj.searchModule(modules.EXEMPTION);
	
	// Step2: Selecting List View 'All'
	apasGenericObj.displayRecords("All");
	
	//Step3: Fetching value of Exemption created above
	String value = exemptionName;
	
	//Step4: Searching and selecting the Exemption
	apasGenericObj.searchAndSelectFromDropDown(globalSearchListEditBox, value);
	objPage.waitUntilPageisReady(driver);	
}

/**
 * Description: This method will fetch the exemption Name from the text of success alert
 * @return : returns the exemption Name
 */
public String getExemptionNameFromSuccessAlert() throws Exception {
	//waitForElementToBeVisible(successAlert,30);
	locateElement("//div[@role='alert'][@data-key='success']",2);
	locateElement("//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']",2);
	String successAlert =  getElementText(successAlertText);
	String exemptionName = successAlert.split("-")[1].split("\"")[0];
	System.out.println("InActive Exemption Name after split:"+exemptionName);
	return exemptionName;
}


public String fetchAssesseeName() {
		//This AssesseeName is temporarily hard coded for PreUAT environment as there is some code deference in preuat and qa
	   String assesseeName = "SMtestPerson";
	   if (!System.getProperty("region").toUpperCase().trim().equals("PREUAT")) {
		   SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		   String queryForID = "SELECT FirstName, LastName FROM Account WHERE Type = 'Person' OR Type = 'Business'";
		   HashMap<String, ArrayList<String>> response  = objSalesforceAPI.select(queryForID);
		   assesseeName = response.get("FirstName").get(0) + " " + response.get("LastName").get(0);
	   }
       ReportLogger.INFO("Assessee Name fetched through Salesforce API : " + assesseeName);
       return assesseeName;
   }


   /*
   This method is used to return the first active APN from Salesforce
   @return: returns the active APN
    */
	public String fetchActiveAPN() {
	SalesforceAPI objSalesforceAPI = new SalesforceAPI();
	String queryForID = "SELECT Name FROM Parcel__c where Status__c='Active' and PUC_Code_Lookup__r.name in ('01-SINGLE FAMILY RES','02-DUPLEX','03-TRIPLEX','04-FOURPLEX','05-FIVE or MORE UNITS','07-MOBILEHOME','07F-FLOATING HOME','89-RESIDENTIAL MISC.','91-MORE THAN 1 DETACHED LIVING UNITS','92-SFR CONVERTED TO 2 UNITS','94-TWO DUPLEXES','96-FOURPLEX PLUS A RESIDENCE DUPLEX OR TRI','97-RESIDENTIAL CONDO','97H-HOTEL CONDO','98-CO-OPERATIVE APARTMENT') Limit 1";
	if (System.getProperty("region").toUpperCase().trim().equals("PREUAT")) {
		queryForID = "SELECT Name FROM Parcel__c where Status__c='Active' limit 1";
	}
	HashMap<String, ArrayList<String>> response  = objSalesforceAPI.select(queryForID);
	return response.get("Name").get(0);
	}


	 /*
	   This method is used to return the Exemption Name with particular Veteran Name
	   @param: Veteran Name used to create Exemption 
	   @return: returns the Exemption Name
	    */
	public String fetchExemptionName(String veteranName) throws Exception {
		SalesforceAPI objSalesforceAPI = new SalesforceAPI();
		String queryForID = "SELECT Name FROM Exemption__c WHERE Name_of_Veteran__c ='"+veteranName+"'";
		HashMap<String, ArrayList<String>> response  = objSalesforceAPI.select(queryForID);
		String exemptionName = response.get("Name").get(0);
		ReportLogger.INFO("Exemption Name fetched through Salesforce API : " + exemptionName);
		return exemptionName;
	   }
	
	/**
	 * @description: This method will return the error message appeared against the filed name passed in the parameter
	 * @param fieldName: field name for which error message needs to be fetched
	 */
	public String getIndividualFieldErrorMessage(String fieldName) throws Exception {
		String xpath;
		if (fieldName.contains("Claimant's")){
			 xpath = "//div[@role='listitem']//span[text()=" + "\"" + fieldName + "\"" + "]/../../../ul[contains(@data-aura-class,'uiInputDefaultError')]";
		}else if(fieldName.equals("Basis for Claim")){
			 xpath = "//span[@class='slds-has-error slds-form-element__help']";
		}else{
			 xpath = "//div[@role='listitem']//span[text()='" + fieldName + "']/../../../ul[contains(@data-aura-class,'uiInputDefaultError')]";
		}
		waitUntilElementIsPresent(xpath,20);
		return getElementText(driver.findElement(By.xpath(xpath)));
	}
}
