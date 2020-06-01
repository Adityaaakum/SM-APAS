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
import com.apas.config.modules;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class ExemptionsPage extends ApasGenericPage {
	Logger logger;
	Page objPage;
	SoftAssertion softAssert1;
	ApasGenericFunctions apasGenericObj;
	String exemptionFileLocation = "";
	
	
	public ExemptionsPage(RemoteWebDriver driver) {
		
		super(driver);
		PageFactory.initElements(driver, this);
		logger = Logger.getLogger(LoginPage.class);
		objPage=new Page(driver);
		softAssert1=new SoftAssertion();
		apasGenericObj= new ApasGenericFunctions(driver);
		
		
		
	}

	@FindBy(xpath = "//div[@class='pageLevelErrors']//li")
	public WebElement errorMessage;

	@FindBy(xpath="//div[@class='genericNotification']/following-sibling::ul/li/a")
	public WebElement genericErrorMsg;
	
	@FindBy(xpath="//h2[contains(.,'Edit Exemption')]")
	public WebElement editExemptionHeader;
	

	@FindBy(xpath = "//button[@title='Cancel']")
	public WebElement cancelButton;
	
	
	@FindBy(xpath = "//a[@title='New']/div[@title='New'][1]")
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
	
	/*@FindBy(xpath="//flexipage-record-page-decorator[contains(@style,'display: block;')]//a[contains(.,'Details')]")
	public WebElement exemationDetails;
	*/
	
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
	
	/*	Next 7 locators are for handling date picker
	 *	These would be moved to common package/class
	 * */
	
	@FindBy(xpath = "//div[contains(@class, 'visible DESKTOP uiDatePicker')]")
	private WebElement datePicker;

	@FindBy(xpath = "//select[contains(@class, 'select picklist')]")
	private WebElement yearDropDown;
	
	@FindBy(xpath = "//a[@class='navLink prevMonth']")
	private WebElement prevMnth;
	
	@FindBy(xpath = "//a[@class='navLink nextMonth']")
	private WebElement nextMnth;
	
	@FindBy(xpath = "//span[(contains(@class, 'uiDayInMonthCell')) and (not (contains(@class, 'nextMonth '))) and (not (contains(@class, 'prevMonth ')))]")
	private List <WebElement> dates;

	@FindBy(xpath = "//h2[@class = 'monthYear']")
	private WebElement visibleMonth;

	@FindBy(xpath = "//button[text() = 'Today']")
	private WebElement currentDate;
	
	
	/*	Next 4 locators are for validating error messages for duplicate Exemption or missing details
	 *	These would be moved to common package/class
	 * */
	
	@FindBy(xpath = "//li[contains(text(), 'These required fields must be completed:')]")
	private WebElement errorMsgOnTop;
	
	@FindBy(xpath = "//li[text() = 'Complete this field'] | //span[text() = 'Complete this field']")
	private List<WebElement> errorMsgUnderLabels; 
	
	@FindBy(xpath = "//li[contains(text(),'There seems to be an existing record with a blank')]")
	public WebElement duplicateErrorMsgWithBlankEndDate;
	
	@FindBy(xpath = "//li[contains(text(),'There seems to be an existing record with overlapp')]")
	public WebElement duplicateErrorMsgWithOverlappingDetails;
	
	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;
	

	
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
	
	////To be deleted after using database query instead of using this
/**
 * Description: This method is to select a record from list screen  
 * User should be on the respective screen
 */





public String createNewExemption(Map<String,String> newExemptionData) throws Exception {
	// TODO Auto-generated method stub
	
	
	ExtentTestManager.getTest().log(LogStatus.INFO, "Entering/Selecting values for New Exemption record");
	apasGenericObj.searchAndSelectFromDropDown(apn,newExemptionData.get("APN"));
	objPage.enter(dateApplicationReceived, newExemptionData.get("DateApplicationReceived"));
	apasGenericObj.searchAndSelectFromDropDown(claimantName,newExemptionData.get("ClaimantName"));
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
	//apasGenericObj.selectFromDropDown(disabledVeteranObj.endRatingReason, newExemptionData.get("EndRatingReason"));
	//objPage.enter(disabledVeteranObj.endDateOfRating, newExemptionData.get("EnddateOfRating"));
	objPage.Click(saveButton);
	//checkIfDuplicateExemption(newExemptionData);
	objPage.locateElement("//a[contains(.,'Value Adjustments')]", 3);
	String exemptionName=newExemptionNameAftercreation.getText();
	System.out.println("Created Exemption:: "+exemptionName);
	return exemptionName;
	
	}


public String createNewExemptionWithMandatoryData(Map<String, String> newExemptionData) throws Exception {

	ExtentTestManager.getTest().log(LogStatus.INFO, "Entering/Selecting values for New Exemption record");
	String exemptionName = null;
	
		apasGenericObj.searchAndSelectFromDropDown(apn,newExemptionData.get("APN"));
		objPage.enter(dateApplicationReceived, newExemptionData.get("DateApplicationReceived"));
		apasGenericObj.searchAndSelectFromDropDown(claimantName,newExemptionData.get("ClaimantName"));
		objPage.enter(claimantSSN, newExemptionData.get("ClaimantSSN"));
		objPage.enter(veteranName, newExemptionData.get("VeteranName").concat(java.time.LocalDateTime.now().toString()));
		objPage.enter(veteranSSN, newExemptionData.get("VeteranSSN"));
		objPage.enter(dateAquiredProperty, newExemptionData.get("DateAquiredProperty"));
		objPage.enter(dateOccupyProperty, newExemptionData.get("DateOccupyProperty"));
		objPage.enter(effectiveDateOfUSDVA, newExemptionData.get("EffectiveDateOfUSDVA"));
		objPage.enter(dateOfNotice, newExemptionData.get("DateOfNotice"));
		apasGenericObj.selectMultipleValues(newExemptionData.get("BasisForClaim"), "Basis for Claim");
		apasGenericObj.selectFromDropDown(qualification, newExemptionData.get("Qualification"));
		if(newExemptionData.get("Qualification").contains("Not Qualified"))
		{
			apasGenericObj.selectFromDropDown(reasonNotQualified, newExemptionData.get("ReasonForNotQualified"));	
			
		}
		if(newExemptionData.containsKey("EnddateOfRatingNeeded"))
		{
			objPage.enter(endDateOfRating, newExemptionData.get("EnddateOfRatingNeeded"));
			apasGenericObj.selectFromDropDown(endRatingReason, newExemptionData.get("EndRatingReason"));
			
		}
		objPage.Click(saveButton);
		//checkIfDuplicateExemption(newExemptionData);
		objPage.locateElement("//div[@class='windowViewMode-normal oneContent active lafPageHost']//div[contains(.,'Date Application Received')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text", 4);
		exemptionName=newExemptionNameAftercreation.getText();
		System.out.println("Created Exemption:: "+exemptionName);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Created "+exemptionName+" Exemption with mandatory data");
	
	
	return exemptionName;
}



////////////////new functions//////////////////////////////////////////


/**
 * Description: This method will enter date
 * @param element: locator of element where date need to be put in
 * @param date: date to enter
 */

public void enterDate(WebElement element, String date) throws Exception {
	Click(element);
	selectDateFromDatePicker(date);
}

/**
 * Description: This method selects the date from date picker
 * @param date: date to enter
 */

public void selectDateFromDatePicker(String expctdDate) throws Exception {
	final Map<String, String> monthMapping = new HashMap<String, String>();
	monthMapping.put("01", "January");
	monthMapping.put("02", "February");
	monthMapping.put("03", "March");
	monthMapping.put("04", "April");
	monthMapping.put("05", "May");
	monthMapping.put("06", "June");
	monthMapping.put("07", "July");
	monthMapping.put("08", "August");
	monthMapping.put("09", "September");
	monthMapping.put("10", "October");
	monthMapping.put("11", "November");
	monthMapping.put("12", "December");
			
	final String[] monthsArr = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
	final List<String> monthsList = new ArrayList<>(Arrays.asList(monthsArr));
	
	Date presentDate = new Date();
	String formattedPresentDate = new SimpleDateFormat("MM/dd/yyyy").format(presentDate);		
	Date dt = new SimpleDateFormat("MM/dd/yyyy").parse(expctdDate);
	String formattedExpctdDate = new SimpleDateFormat("MM/dd/yyyy").format(dt);
	
	if(formattedPresentDate.equals(formattedExpctdDate)) {
		Click(currentDate);
	} else {		
		String[] dateArray = formattedExpctdDate.toString().split("/");
		String yearToSelect = dateArray[2];
		String monthToSelect = monthMapping.get(dateArray[0]);
		String dateToSelect;
		if(dateArray[1].startsWith("0")) {
			dateToSelect = dateArray[1].substring(1);
		} else {
			dateToSelect = dateArray[1];
		}

		Select select = new Select(waitForElementToBeVisible(yearDropDown));
		select.selectByValue(yearToSelect);
		
		WebElement visibleMnth = waitForElementToBeVisible(visibleMonth);
		String visibleMonthTxt = visibleMnth.getText().toLowerCase();
		visibleMonthTxt = visibleMonthTxt.substring(0, 1).toUpperCase() + visibleMonthTxt.substring(1).toLowerCase();
		
		int counter = 0;
		int indexOfDefaultMonth = monthsList.indexOf(visibleMonthTxt);		
		int indexOfMonthToSelect = monthsList.indexOf(monthToSelect);
		int counterIterations = (Math.abs(indexOfDefaultMonth - indexOfMonthToSelect));
		
		while(!visibleMonthTxt.equalsIgnoreCase(monthToSelect) || counter > counterIterations) {
			if(indexOfMonthToSelect < indexOfDefaultMonth) {
				waitForElementToBeVisible(prevMnth).click();
			} else {
				waitForElementToBeVisible(nextMnth).click();
			}
			visibleMonthTxt = waitForElementToBeVisible(visibleMonth).getText();
			counter++;
		}
		
		for(WebElement date : dates) {
			String currentDate = date.getText();
			if(currentDate.equals(dateToSelect)) {
				date.click();
				break;
			}
		}
	}
	
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
 * Description: This method will search and select from dropdown
 * @param element: locator of element where date need to be put in
 * @param value: field value to enter
 */

public void searchAndSelectFromDropDown(WebElement element, String value) throws Exception {
	enter(element, value);
	String xpathStr = "//div[@title='" + value + "']";
	WebElement drpDwnOption = locateElement(xpathStr, 200);
	drpDwnOption.click();
}

/**
 * Description: This method will click New button on Exemption screen
 * @param element: locator of element where date need to be put in
 * @param value: field value to enter
 */

public void openExemptionScreen() throws Exception {
	Thread.sleep(2000);
	Click(waitForElementToBeClickable(newExemptionButton));
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
	searchAndSelectFromDropDown(apn, dataMap.get("APN"));
	enter(dateApplicationReceived, dataMap.get("Date Application Received"));
	searchAndSelectFromDropDown(claimantName, dataMap.get("Claimant Name"));
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
	searchAndSelectFromDropDown(apn, dataMap.get("APN"));
	enter(dateApplicationReceived, dataMap.get("Date Application Received"));
	searchAndSelectFromDropDown(claimantName, dataMap.get("Claimant Name"));
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
	searchAndSelectFromDropDown(apn, dataMap.get("APN"));
	enter(dateApplicationReceived, dataMap.get("Date Application Received"));
	searchAndSelectFromDropDown(claimantName, dataMap.get("Claimant Name"));
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
	searchAndSelectFromDropDown(apn, dataMap.get("APN"));
	enter(dateApplicationReceived, dataMap.get("Date Application Received"));
	searchAndSelectFromDropDown(claimantName, dataMap.get("Claimant Name"));
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
	//ExtentTestManager.getTest().log(LogStatus.INFO, "Update 'End Date of Rating' and 'End Rating Reason' in the Exemption record");
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
	//ExtentTestManager.getTest().log(LogStatus.INFO, "Interchange values of 'Effective Date of 100% USDVA Rating' and 'Date Occupied/Intend to Occupy Property' in the Exemption record");
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
 * Description: This method will clear a field value on Exemption screen
 * @param elem: locator of element where field value needs to be cleared
 */

public void clearFieldValue(WebElement elem) throws Exception {
	waitForElementToBeClickable(15, elem);
	((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid green'", elem);		
	elem.clear();
	Thread.sleep(2000);
}

/**
 * Description: This method will retrieve error message in case mandatory fields are not filled on Exemption screen
 * @return : returns the list containing the error messages and the count of fields
 */

public List<String> retrieveExemptionMandatoryFieldsValidationErrorMsgs() throws Exception {
	List<String> errorsList = new ArrayList<String>();
	String errorMsgOnTopOfPopUpWindow = getElementText(waitForElementToBeVisible(errorMsgOnTop));
	errorsList.add(errorMsgOnTopOfPopUpWindow);

	String individualErrorMsg = getElementText(errorMsgUnderLabels.get(0));
	errorsList.add(individualErrorMsg);
	
	String fieldsStr = errorMsgOnTopOfPopUpWindow.split(":")[1];
	String[] totalMandatoryFields = fieldsStr.split(",");
	int countOfMandatoryFields = totalMandatoryFields.length;
	errorsList.add(Integer.toString(countOfMandatoryFields));
	
	int countOfIndividualErrorMsgs = errorMsgUnderLabels.size();
	errorsList.add(Integer.toString(countOfIndividualErrorMsgs));
	return errorsList;
}	

/**
 * Description: This method will fetch the current URL and process it to get the Record Id
 * @param driver: Driver Instance
 * @return : returns the Record Id
 */

public String getCurrentUrl(RemoteWebDriver driver) throws Exception {
	wait.until(ExpectedConditions.urlContains("/view"));
	String url = driver.getCurrentUrl();
	String recordId = url.split("/")[6];
	driver.navigate().refresh();
	ExtentTestManager.getTest().log(LogStatus.INFO, "Exemption record id - " + recordId);
	return recordId;

}	

/**
 * Description: This method includes other methods and creates an Exemption with limited fields data and without EndDateOfRating
 * @param dataMap: Map that is storing values from JSON file
 */

public void createExemptionWithoutEndDateOfRating(Map<String, String> dataMap) throws Exception {
	ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to fill the following details in the Exemption record : " + dataMap);
	openExemptionScreen();	
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
	openExemptionScreen();	
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
	openExemptionScreen();	
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
	openExemptionScreen();	
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
	//ExtentTestManager.getTest().log(LogStatus.INFO, "Update 'Qualification' and 'Reason for Not Qualified' in the Exemption record"));
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
	openExemptionScreen();
	waitForExemptionScreenToLoad();
	ExtentTestManager.getTest().log(LogStatus.INFO, "Without entering any data on the Exemption record, click 'Save' button");
	saveExemptionRecord();
}

/**
 * Description: This method will remove leading zeroes from the month and day value in Date
 * @param dateValue: Date value passed from the Json file
 * @return : returns the dateValue
 */
public void clickShowMoreButton(String recordId, String action) throws Exception {       
    Thread.sleep(1000);
    String xpathStr1 = "//a[@data-recordid='" + recordId + "']//parent::span//parent::th//following-sibling::td[6]//span//div//a//lightning-icon";
    WebElement showMoreIcon = locateElement(xpathStr1, 30);
    Click(showMoreIcon);
    Thread.sleep(1000);
    String xpathStr2 = "//li//a[@title='" + action + "']//div[text()='" + action + "']";
    WebElement editOnShowMoreIcon = locateElement(xpathStr2, 30);
    clickAction(editOnShowMoreIcon);
    Thread.sleep(1000);
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
 * Description: This method will expand icon for More Exemption Detail section on Exemption Detail page
 * @param element: WebElement on which action is to be performed
 */

public void expandIcon(WebElement element) throws Exception {
	String propertyValue = getAttributeValue(element, "aria-expanded");
	if (propertyValue.equals("false")){
		Click(element);
	}
}

/**
 * Description: This method will generate a Random Number
 * @param min, max: Numbers between a random number will be generated
 * @return : returns the random number
 */

public double getRandomIntegerBetweenRange(int min, int max){
	    int x = (int) (Math.random()*((max-min)+1))+min;
	    return x;
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
	apasGenericObj.selectListView("All");
	
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

}
