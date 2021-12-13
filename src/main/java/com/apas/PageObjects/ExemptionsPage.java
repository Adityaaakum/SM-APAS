package com.apas.PageObjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.apas.Assertions.SoftAssertion;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.config.modules;

public class ExemptionsPage extends ApasGenericPage {
    Logger logger;
    Page objPage;
    SoftAssertion softAssert1;
    RealPropertySettingsLibrariesPage objRPSL;
    SalesforceAPI objSalesforceAPI = new SalesforceAPI();

    public ExemptionsPage(RemoteWebDriver driver) {

        super(driver);
        PageFactory.initElements(driver, this);
        logger = Logger.getLogger(LoginPage.class);
        objPage = new Page(driver);
        softAssert1 = new SoftAssertion();
        objRPSL = new RealPropertySettingsLibrariesPage(driver);
    }

    @FindBy(xpath = "//div[@class='pageLevelErrors']//li")
    public WebElement errorMessage;

    @FindBy(xpath = "//div[@class='genericNotification']/following-sibling::ul/li/a")
    public WebElement genericErrorMsg;

    @FindBy(xpath = "//h2[contains(.,'Edit Exemption')]")
    public WebElement editExemptionHeader;

    @FindBy(xpath = "//button[text()='Cancel']")
    public WebElement cancelButton;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//*[text() = 'New']")
    public WebElement newExemptionButton;
    
    public String apn = "APN";

    public String dateApplicationReceived = "Date Application Received";

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div[contains(.,'Date Application Received')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
    public WebElement dateApplicationReceivedExemptionDetails;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div[contains(.,'Grace End Date')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
    public WebElement graceEndDateExemptionDetails;

    public String claimantName = "Claimant 1 Name";

    public String claimantSSN = "Claimant's SSN";

    public String spouseName = "Spouse's Name";

    public String spouseSSN = "Spouse's SSN";

    public String unmarriedSpouseOfDisabledVeteran = "Unmarried Spouse of Deceased Veteran?";

    public String dateOfDeathOfVeteran = "Date of Death of Veteran";

    public String veteranName = "Veteran's Name";

    public String veteranSSN = "Veteran's SSN";

    public String dateAquiredProperty = "Date Acquired Property";

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div[contains(.,'Date Acquired Property')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
    public WebElement dateAquiredPropertyExemptionDetails;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//li[@title='Details']//a[@data-tab-value='detailTab']")
    public WebElement exemptionDetailsTab;

    public String dateOccupyProperty = "Date Occupied/Intend to Occupy Property";

    public String dateOfNoticeOfRating = "Date of Notice of 100% Rating";

    public String dateAcquiredProperty = "Date Acquired Property";

    @FindBy(xpath = "//button[@title='Save'] | //button[text()='Save']")
    public static WebElement saveButton;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div[contains(.,'Date Occupied/Intend to Occupy Property')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
    public WebElement dateOccupyPropertyExemptionDetails;

    public String dvExemptionOnPriorResidence = "DV Exemption on Prior Residence";

    public String dateMoveFromProprResidence = "Date Moved From Prior Residence";

    public String priorResidenceStreetAddress = "Prior Residence Street Address";

    public String priorResidenceCity = "Prior Residence City";

    public String priorResidenceState = "Prior Residence State";

    public String priorResidenceCounty = "Prior Residence County";

    public String effectiveDateOfUSDVA = "Effective Date of 100% USDVA Rating";

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div[contains(.,'Effective Date of 100% USDVA Rating')]/following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
    public WebElement effectiveDateOfUSDVAExemptionDetails;

    @FindBy(xpath = "//a[@title='Select List View']")
    public WebElement selectlistView;

    @FindBy(xpath = "//li[2]//a[contains(.,'All')]")
    public WebElement allView;

    public String dateOfNotice = "Date of Notice of 100% Rating";

    public String claimanatEmailAddress = "Claimant's Email Address";

    public String claimantTelephone = "Claimant's Telephone #";

    public String qualification = "Qualification?";

    public String reasonNotQualified = "Reason for Not Qualified";

    public String notQualifiedDetail = "Not Qualified Detail";

    public String endRatingReason = "End Rating Reason";

    public String endDateOfRating = "End Date of Rating";
    
    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text()='Next']")
	public WebElement exemptionRecordTypeNextButton;

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

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@name='Edit']")
    public WebElement editExemption;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@name='Delete']")
    public WebElement deleteExemption;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@title='Edit End Date of Rating']/preceding-sibling::span//slot[@slot='outputField']/lightning-formatted-text")
    public WebElement endDateOfRatingOnExemption;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//ul//li[contains(@title,'Business Events')]")
    public WebElement businessEvent;

    @FindBy(xpath = "//table//tr[1]//th[@scope='row']//a")
    public WebElement newRecord;

    @FindBy(xpath = "//li[@class='form-element__help']")
    public List<WebElement> fieldsErrorMsg;

    @FindBy(xpath = "//li[@class='form-element__help']")
    public WebElement fieldErrorMsg;

    @FindBy(xpath = "//div[contains(@id,'error-message')]")
    public WebElement fieldErrorMessage;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div[contains(text(),'Exemption')]//following-sibling::slot/slot/lightning-formatted-text")
    public WebElement newExemptionNameAftercreation;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[contains(text(),'Status')]/parent::div/following-sibling::div//span//slot[@slot='outputField']//lightning-formatted-text")
    public WebElement exemationStatusOnDetails;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//slot[@name='sidebar']//ul[@role='tablist']//li[not(contains(@style,'visibility: hidden;'))]")
    public List<WebElement> rightSideTopSectionsOnUI;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//ul[@class='tabs__nav']//li/a//span[@class='title']")
    public List<WebElement> rightSideBottomSectionsOnUI;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//label[contains(text(),'Qualification?')]/parent::lightning-combobox//div[contains(@id,'help-text')]")
    public WebElement QualificationOnDetailsPageErrorMsg;


    //////////////////////////New locators////////////////////////////////

    public String nameOfVeteran = "Veteran's Name";

    public String emailAddress = "Email Address";

    public String telephone = "Telephone";

    public String dateOccupiedProperty = "Date Occupied/Intend to Occupy Property";

    @FindBy(xpath = "//span[contains(text(),'Blind In Both Eyes')]")
    public WebElement basisForClaim1;

    @FindBy(xpath = "//span[contains(text(),'Disabled Because Of Loss Of Use Of 2 Or More Limbs')]")
    public WebElement basisForClaim2;

    @FindBy(xpath = "//div[text()='Basis for Claim']//following::button[@title='Move selection to Chosen'][1]")
    public WebElement basisForClaimButton;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@name='Edit']")
    public WebElement editButton;

    @FindBy(xpath = "//div[@role='menu']//li[@class='uiMenuItem']/a[@title = 'Edit']")
    public WebElement editMenuItemButton;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//lightning-formatted-text[contains(text(),'EXMPTN-')]")
    public WebElement exemptionName;
    
    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[text() = 'New']")
    public WebElement newExemptionRelatedButton;
    
  



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

    public String errorMessageOnTop = "//div[@class='pageLevelErrors']//li";
    
    /** Locators for Home Owner Exemptions **/
    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//label[contains(@class,'slds-radio topdown-radio')]//span[text()='HOE']")
    public WebElement homeOwnerExemptionRadioButton;
    
    /** Locators for Institution Exemptions **/
    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//label[contains(@class,'slds-radio topdown-radio')]//span[text()='Institutional']")
    public WebElement institutionalExemptionRadioButton;
    
    @FindBy(xpath = "//button[text()='New']")
    public WebElement creatNewVAsButton;
    
    public String exemptionCode = "Exemption Code";
    
    public String Penalty = "Penalty %";
    
    public String rollYearSettingOnVAs = "Roll Year Settings";
    
    public String RPSLfieldOnVAs = "Real Property Settings Library";
    
    public String ExemptionAmountUserAdjusted = "Exemption Amount - User Adjusted";
    
    public String Remark = "Remark";
    
    public String filingStatus = "Filing Status";
    
    public String propertySqFtProrated = "Property Sq Ft Prorated %";
    
    public String penaltyPercentage = "Penalty Percentage";
    
    public String netExemptionAmount = "Net Exemption Amount";
    
  
    
    /**
     * Description: This method is to determine the Roll Year of any given date(e.g Application received date)
     *
     * @param date: date for which to determine Roll Year
     * @return roll Year: returns the Roll year
     */

    public static String determineRollYear(String date) {
        System.out.println("Determining Roll year for::" + date);
        String[] appRcvddate = date.split("/");
        //String month=appRcvddate[0].replaceAll("0", "");
        int appMonth = Integer.parseInt(appRcvddate[0]);
        int rollYear = Integer.parseInt(appRcvddate[2]);
        if (appMonth >= 7) {
            rollYear = rollYear + 1;
        }
        return String.valueOf(rollYear);

    }


    /**
     * Description: This method is to select a record from list screen
     * User should be on the respective screen
     */

    public String createNewExemption(Map<String, String> newExemptionData) throws Exception {

    	Click(exemptionRecordTypeNextButton);
        ReportLogger.INFO("Entering/Selecting values for New Exemption record");
        searchAndSelectOptionFromDropDown("APN", fetchActiveAPN());
        objPage.enter(dateApplicationReceived, newExemptionData.get("DateApplicationReceived"));
        searchAndSelectOptionFromDropDown(claimantName, fetchAssesseeName());
        objPage.enter("Claimant's SSN", newExemptionData.get("ClaimantSSN"));
        objPage.enter(spouseName, newExemptionData.get("SpouseName"));
        objPage.enter(spouseSSN, newExemptionData.get("SpouseSSN"));
        selectOptionFromDropDown(unmarriedSpouseOfDisabledVeteran, newExemptionData.get("UnmarriedSpouseOfDisabledVeteran"));
        objPage.enter(dateOfDeathOfVeteran, newExemptionData.get("DateOfDeathOfVeteran"));
        objPage.enter(veteranName, newExemptionData.get("VeteranName").concat(java.time.LocalDateTime.now().toString()));
        objPage.enter(veteranSSN, newExemptionData.get("VeteranSSN"));
        objPage.enter(dateAquiredProperty, newExemptionData.get("DateAquiredProperty"));
        objPage.enter(dateOccupyProperty, newExemptionData.get("DateOccupyProperty"));
        selectOptionFromDropDown(dvExemptionOnPriorResidence, newExemptionData.get("DVExemptionOnPriorResidence"));
        objPage.enter(dateMoveFromProprResidence, newExemptionData.get("DateMovedPriorResidence"));
        objPage.enter(priorResidenceStreetAddress, newExemptionData.get("PriorResidenceStreet"));
        objPage.enter(priorResidenceCity, newExemptionData.get("PriorResidenceCity"));
        selectOptionFromDropDown(priorResidenceState, newExemptionData.get("PriorResidenceState"));
        objPage.enter(priorResidenceCounty, newExemptionData.get("PriorResidenceCounty"));
        objPage.enter(effectiveDateOfUSDVA, newExemptionData.get("EffectiveDateOfUSDVA"));
        objPage.enter(dateOfNotice, newExemptionData.get("DateOfNotice"));
        selectMultipleValues(newExemptionData.get("BasisForClaim"), "Basis for Claim");
        objPage.enter(claimanatEmailAddress, newExemptionData.get("EmailAddress"));
        objPage.enter(claimantTelephone, newExemptionData.get("Telephone"));
        selectMultipleValues(newExemptionData.get("DeceasedVeteranQualification"), "Deceased Veteran Qualification");
        selectOptionFromDropDown(qualification, newExemptionData.get("Qualification"));
        objPage.enter(endDateOfRating, newExemptionData.get("EnddateOfRating"));
        selectOptionFromDropDown(endRatingReason, newExemptionData.get("EndRatingReason"));
        objPage.Click(saveButton);

        objPage.waitForElementToBeClickable(dateAquiredPropertyExemptionDetails, 20);
        ReportLogger.INFO("Created " + newExemptionNameAftercreation.getText() + " Exemption with mandatory data");
        return newExemptionNameAftercreation.getText();

    }


    public String createNewExemptionWithMandatoryData(Map<String, String> newExemptionData) throws Exception {

    	 String assesse=fetchAssesseeName();
    	// String assesse_SSN= objSalesforceAPI.select("Select SSN__pc from Account where FirstName = '"+assesse.split(" ")[0]+"'").get("SSN__pc").get(0);
    	 //System.out.println(assesse_SSN);
    	 objSalesforceAPI.update("Account",objSalesforceAPI.select("SELECT ID FROM ACCOUNT WHERE where FirstName = '"+assesse.split(" ")[0]+"'").get("Id").get(0)  ,"SSN__pc", newExemptionData.get("ClaimantSSN"));
    	
    	Click(exemptionRecordTypeNextButton);
        ReportLogger.INFO("Entering/Selecting values for New Exemption record");
        searchAndSelectOptionFromDropDown("APN", fetchActiveAPN());
        objPage.enter(dateApplicationReceived, newExemptionData.get("DateApplicationReceived"));
        searchAndSelectOptionFromDropDown("Claimant 1 Name", assesse);
        objPage.enter(claimantSSN, newExemptionData.get("ClaimantSSN"));
        objPage.enter(veteranName, newExemptionData.get("VeteranName").concat(java.time.LocalDateTime.now().toString()));
        objPage.enter(veteranSSN, newExemptionData.get("VeteranSSN"));
        objPage.enter(dateAquiredProperty, newExemptionData.get("DateAquiredProperty"));
        objPage.enter(dateOccupyProperty, newExemptionData.get("DateOccupyProperty"));
        objPage.enter(effectiveDateOfUSDVA, newExemptionData.get("EffectiveDateOfUSDVA"));
        objPage.enter(dateOfNotice, newExemptionData.get("DateOfNotice"));
        selectMultipleValues(newExemptionData.get("BasisForClaim"), "Basis for Claim");
        selectOptionFromDropDown(qualification, newExemptionData.get("Qualification"));
        if (newExemptionData.get("Qualification").contains("Not Qualified")) {
            selectOptionFromDropDown(reasonNotQualified, newExemptionData.get("ReasonForNotQualified"));
        }
        if (newExemptionData.containsKey("EnddateOfRatingNeeded")) {
            objPage.enter(endDateOfRating, newExemptionData.get("EnddateOfRatingNeeded"));
            selectOptionFromDropDown(endRatingReason, newExemptionData.get("EndRatingReason"));
        }
        objPage.Click(saveButton);
        objPage.waitForElementToBeClickable(dateAquiredPropertyExemptionDetails, 20);

        //Added the below code as SCript is loosing the focus from the Exemption screen
        driver.navigate().refresh();
        Thread.sleep(2000);
        objPage.waitForElementToBeVisible(exemptionName);
        String exmpName = objPage.getElementText(exemptionName);

        ReportLogger.INFO("Created " + exmpName + " Exemption with mandatory data");
        objPage.waitForElementToBeClickable(10, dateApplicationReceivedExemptionDetails);

        return exmpName;
    }


    ////////////////new functions//////////////////////////////////////////


    /**
     * Description: This method will enter field values in Exemption screen
     *
     * @param dataMap: Map that is storing values from JSON file
     */

    public void enterExemptionData(Map<String, String> dataMap) throws Exception {

    	Click(exemptionRecordTypeNextButton);
    	String assesseeName = fetchAssesseeName();

        String apnNumber = fetchActiveAPN();
        if (dataMap.containsKey("Different APN")) apnNumber = dataMap.get("Different APN");
        if (dataMap.containsKey("Same APN")) apnNumber = dataMap.get("Same APN");
        searchAndSelectOptionFromDropDown(apn, apnNumber);
        enter(dateApplicationReceived, dataMap.get("Date Application Received"));
        searchAndSelectOptionFromDropDown(claimantName, assesseeName);
        enter(claimantSSN, dataMap.get("Claimant SSN"));
        if (dataMap.containsKey("Veteran Name")) enter(nameOfVeteran, dataMap.get("Veteran Name"));
        if (dataMap.containsKey("Veteran SSN")) enter(veteranSSN, dataMap.get("Veteran SSN"));
        enter(dateAquiredProperty, dataMap.get("Date Acquired Property"));
        enter(dateOccupiedProperty, dataMap.get("Date Occupied/Intend to Occupy Property"));
        enter(dateOfNotice, dataMap.get("Date of Notice of 100% Rating"));
        enter(effectiveDateOfUSDVA, dataMap.get("Effective Date of 100% USDVA Rating"));
        selectMultipleValues(dataMap.get("Basis For Claim"), "Basis for Claim");
        selectOptionFromDropDown(qualification, dataMap.get("Qualification?"));
        if (dataMap.containsKey("Reason for Not Qualified"))
            selectOptionFromDropDown(reasonNotQualified, dataMap.get("Reason for Not Qualified"));
        if (dataMap.containsKey("End Date Of Rating")) enter(endDateOfRating, dataMap.get("End Date Of Rating"));
        if (dataMap.containsKey("End Rating Reason"))
            selectOptionFromDropDown(endRatingReason, dataMap.get("End Rating Reason"));
        Thread.sleep(1000);
    }

    /**
     * Description: This method will update a field value on Exemption screen
     *
     * @param fieldType:  It is Field Type i.e. Date field, Dropdown field
     * @param fieldName:  It is the name of the field on the screen
     * @param fieldValue: It is the value of the field that is to be updated
     */

    public void updateFieldValue(String fieldType, String fieldName, String fieldValue) throws Exception {
        Thread.sleep(1000);
        WebElement elemLocator = findElem(fieldType, fieldName);
        scrollToElement(elemLocator);
        if (fieldType.equals("Date")) enter(elemLocator, fieldValue);
        if (fieldType.equals("Dropdown")) selectOptionFromDropDown(elemLocator, fieldValue);
    }


    public WebElement findElem(String fieldType, String fieldName) throws Exception {
        return objPage.getWebElementWithLabel(fieldName);
    }

    /**
     * Description: This method will click SAVE button on Exemption screen
     */
    public void saveExemptionRecord() throws Exception {
        Click(saveButton);
        Thread.sleep(5000);
    }

    /**
     * Description: This method will click EDIT button on Exemption screen
     */
    public void editExemptionRecord() throws Exception {
        ReportLogger.INFO("Click 'Edit' button to update it");
        Click(editButton);
        Thread.sleep(2000);
    }

    /**
     * Description: This method includes other methods and creates an Exemption
     *
     * @param dataMap: Map that is storing values from JSON file
     */

    public void createExemption(Map<String, String> dataMap) throws Exception {
        ReportLogger.INFO("Click 'New' button to fill the following details in the Exemption record : " + dataMap);
        Thread.sleep(2000);
        Click(waitForElementToBeClickable(newExemptionButton));
        Thread.sleep(2000);
        waitForElementToBeClickable(apn, 10);
        waitForElementToBeClickable(claimantName, 10);
        enterExemptionData(dataMap);
        ReportLogger.INFO("Click 'Save' button to save the details entered in Exemption record");
        saveExemptionRecord();
    }

    /**
     * Description: This method will open the Exemption Name passed in the argument
     *
     * @param exempName: Takes Exemption Name as an argument
     */
    public void openExemptionRecord(String exempName) throws IOException, InterruptedException {
        ReportLogger.INFO("Open the Exemption record : " + exempName);
        Click(driver.findElement(By.xpath("//a[@title='" + exempName + "']")));
        Thread.sleep(3000);
    }

    /**
     * Description: This method will open the Exemption record using RecordId
     *
     * @param exempName: Takes Exemption Name as an argument
     * @param recordId:  Takes RecordId as an argument
     */
    public void openExemptionRecord(String recordId, String exempName) throws Exception {
        ReportLogger.INFO("Open the Exemption record : " + exempName);
        String xpathStr = "//a[@data-recordid='" + recordId + "']";
        WebElement exemptionLocator = locateElement(xpathStr, 30);
        Click(exemptionLocator);
    }

    /**
     * Description: This method will remove leading zeroes from the month and day value in Date
     *
     * @param dateValue: Date value passed from the Json file
     * @return : returns the dateValue
     */

    public String removeZeroInMonthAndDay(String dateValue) throws Exception {
        //boolean flag = false;
        String apnd;

        if (dateValue.charAt(0) == '0') {
            dateValue = dateValue.substring(1);
            //flag = true;

            if (dateValue.charAt(2) == '0') {
                apnd = dateValue.substring(0, 2);
                dateValue = apnd + dateValue.substring(3);
            }
        } else {
            if (dateValue.charAt(3) == '0') {
                apnd = dateValue.substring(0, 3);
                dateValue = apnd + dateValue.substring(4);
            }
        }
        return dateValue;
    }

    /**
     * Description: This method will determine the enabled/disabled status of the 'Status' field
     *
     * @param fieldLabel: Field Label on Exemption detail page
     * @param driver:     Driver Instance
     * @return : returns true or false
     */

    public String editExemptionAndValidateEnabledStatusOnDetailPage(String fieldLabel, RemoteWebDriver driver) throws Exception {

        String flag = "false";
        Click(editPencilIconForVeteranNameOnDetailPage);

        String propertyValue = getAttributeValue(expandedIconForMoreExemptionOnDetailPage, "aria-expanded");
        if (propertyValue.equals("false")) {
            Click(expandedIconForMoreExemptionOnDetailPage);
        }

        //Get all the read-only elements in a List
        scrollToElement(statusOnDetailPage);
        List<WebElement> field_elements = driver.findElements(By.xpath("//div[text() = 'Exemption']//ancestor::div[@class = 'slds-col slds-size_1-of-1 row region-header']//following-sibling::div[contains(@class, 'col slds-size_1-of-1 row')]//slot[@slot = 'outputField']//lightning-formatted-text//ancestor::div[@class = 'slds-form-element__control']//preceding-sibling::div"));
        ReportLogger.INFO("There are " + field_elements.size() + " read only fields on Exemption record");

        for (WebElement fieldElement : field_elements) {
            waitForElementToBeVisible(fieldElement);
            String elementName = getElementText(fieldElement);
            if (elementName.equals(fieldLabel)) {
                flag = "true";
                ReportLogger.INFO("Validate 'Status' field is read only on Exemption Detail screen");
                break;
            }
        }
        Click(cancelButtonOnDetailPage);
        return flag;
    }


    public String fetchAssesseeName() {
        //This AssesseeName is temporarily hard coded for PreUAT environment as there is some code deference in preuat and qa
        String assesseeName = "SMtestPerson";
        if (!System.getProperty("region").toUpperCase().trim().equals("PREUAT")) {
            String queryForID = "SELECT FirstName, LastName FROM Account WHERE Type In('Person','Business') and FirstName !=NULL ";
            HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(queryForID);
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
        return fetchActiveAPN(1).get(0);
    }

    public ArrayList<String> fetchActiveAPN(int numberofAPNs) {
        String queryForID = "SELECT Name FROM Parcel__c where Status__c='Active' and PUC_Code_Lookup__r.name in ('000- Vacant Land (Migrated)','000B Vacant Land - Brush, Barren','01-SINGLE FAMILY RES','02-DUPLEX','03-TRIPLEX','04-FOURPLEX','05-FIVE or MORE UNITS','07-MOBILEHOME','07F-FLOATING HOME','89-RESIDENTIAL MISC.','91-MORE THAN 1 DETACHED LIVING UNITS','92-SFR CONVERTED TO 2 UNITS','94-TWO DUPLEXES','96-FOURPLEX PLUS A RESIDENCE DUPLEX OR TRI','97-RESIDENTIAL CONDO','97H-HOTEL CONDO','98-CO-OPERATIVE APARTMENT') Limit " + numberofAPNs;
        return objSalesforceAPI.select(queryForID).get("Name");
    }

	 /*
	   This method is used to return the Exemption Name with particular Veteran Name
	   @param: Veteran Name used to create Exemption 
	   @return: returns the Exemption Name
	    */

    public String fetchExemptionName(String veteranName) throws Exception {
        String queryForID = "SELECT Name FROM Exemption__c WHERE Name_of_Veteran__c ='" + veteranName + "'";
        HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(queryForID);
        String exemptionName = response.get("Name").get(0);
        ReportLogger.INFO("Exemption Name fetched through Salesforce API : " + exemptionName);
        return exemptionName;
    }

    /**
     * @throws Exception
     * @description: This method is to create Current Roll Year RPSL if not present and
     * Approve UnApproved RPSL present in system for past 9 years from current roll year
     */
    public void checkRPSLCurrentRollYearAndApproveRPSLPastYears(Map<String, String> rpslData) throws Exception {
        String currentDate = DateUtil.getDateInRequiredFormat(java.time.LocalDate.now().toString(), "yyyy-MM-dd", "MM/dd/yyyy");
        String currentRollYear = ExemptionsPage.determineRollYear(currentDate);
        String lastRolYearToVerify = Integer.toString(Integer.parseInt(currentRollYear) - 8);
        
        //verifying if current roll year's RPSL is present or not if not present then create one
        ReportLogger.INFO("Verifying and creating if Current Roll Year's RPSL is not present");
        String currentRollYearRPSLQuery = "select id from Real_Property_Settings_Library__c where Roll_Year_Settings__r.name ='" + currentRollYear + "' and Status__c='Approved'";
        HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(currentRollYearRPSLQuery);
        if (response.size() == 0) {
        	//deleting unapproved RPSL for current year because this is give error when we approved this.
        	ReportLogger.INFO("Deleting unapproved RPSL for Current Roll Year ::" + currentRollYear);
            String currentRollYearUnApprovedRPSLQuery = "select id from Real_Property_Settings_Library__c where Roll_Year_Settings__r.name ='" + currentRollYear + "' and Status__c!='Approved'";
            objSalesforceAPI.delete(currentRollYearUnApprovedRPSLQuery);
            ReportLogger.INFO("Current Roll Year RPSL is not present hence creating one for ::" + currentRollYear);
            searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
            objRPSL.enterRealPropertySettingsDetails(rpslData, currentRollYear);
            objRPSL.saveRealPropertySettings();
        }
        ReportLogger.INFO("Verifying and Approving previous 9 year's unapproved RPSL");
        //Getting All Roll year which has Approved RPSL 
        String ApprovedRPSLQuery ="select Name from Roll_Year_Settings__c where id In(select Roll_Year_Settings__c from Real_Property_Settings_Library__c where Roll_Year_Settings__r.name <='" + currentRollYear + "' and Roll_Year_Settings__r.name >='" + lastRolYearToVerify +"' and Status__c='Approved')order by Name desc"; 		
        HashMap<String, ArrayList<String>> approvedResponse = objSalesforceAPI.select(ApprovedRPSLQuery);
        ArrayList<String> Approvedrollyear=approvedResponse.get("Name");
        String ApprovedRPSLrollyear="";
        for(int i=0;i<Approvedrollyear.size();i++) {	
        	ApprovedRPSLrollyear=ApprovedRPSLrollyear+"'"+ Approvedrollyear.get(i)+"',";
        }
        ApprovedRPSLrollyear = ApprovedRPSLrollyear.substring(0, ApprovedRPSLrollyear.length() - 1);
        //Now verifying if UnApproved RPSL present among past 9 years records and Approve them if so   
        String notApprovedRPSLQuery = "select id from Real_Property_Settings_Library__c where Roll_Year_Settings__r.name <='" + currentRollYear + "' and Roll_Year_Settings__r.name >='" + lastRolYearToVerify + "' and Roll_Year_Settings__r.name NOT IN("+ApprovedRPSLrollyear+") and Status__c!='Approved' order by Roll_Year_Settings__r.name desc";
        String closedRollYear = "Select id From Roll_Year_Settings__c where Status__c = 'Closed' and Roll_Year__c>='" + lastRolYearToVerify + "' and Roll_Year__c<='" + currentRollYear + "'and Roll_Year__c NOT IN("+ApprovedRPSLrollyear+") order by name desc";
        objSalesforceAPI.update("Roll_Year_Settings__c", closedRollYear, "Status__c", "Open");
        objSalesforceAPI.update("Real_Property_Settings_Library__c", notApprovedRPSLQuery, "Status__c", "Approved");
        String openRollYear = "Select id From Roll_Year_Settings__c where Status__c = 'Open' and Roll_Year__c!='" + currentRollYear + "' and Roll_Year__c>='" + lastRolYearToVerify + "' and Roll_Year__c<'" + currentRollYear + "' order by name desc";
        objSalesforceAPI.update("Roll_Year_Settings__c", openRollYear, "Status__c", "Closed");
        
    }
    
    /**
     * Description: This method includes other methods and creates a Home Owner Exemption
     * @param dataMap: Map that is storing values from JSON file
     */

    public void createHomeOwnerExemption(Map<String, String> dataMap) throws Exception {
    	Thread.sleep(2000);
        ReportLogger.INFO("Click 'New' button to fill the following details in the Home Owner Exemption record : " + dataMap);
        Click(waitForElementToBeClickable(newExemptionButton));
        Thread.sleep(2000);
        waitForElementToBeClickable(apn, 10);
        waitForElementToBeClickable(claimantName, 10);
        enterHomeOwnerExemptionData(dataMap);
        ReportLogger.INFO("Click 'Save' button to save the details entered in Exemption record");
        saveExemptionRecord();
    }
    
    
    /**
     * Description: This method will enter field values in Home Onwer Exemption screen
     * @param dataMap: Map that is storing values from JSON file
     */

    public void enterHomeOwnerExemptionData(Map<String, String> dataMap) throws Exception {
    	Click(homeOwnerExemptionRadioButton);
    	Click(exemptionRecordTypeNextButton);
    	
    	//String assesseeName = fetchAssesseeName();
    	//Commented above code temporarily and added below code till current HOE implementation is completed 
    	String queryForID = "SELECT FirstName, LastName FROM Account WHERE Type In('Person','Business') and FirstName = 'DoNot'";
        HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(queryForID);
        String assesseeName = response.get("FirstName").get(0) + " " + response.get("LastName").get(0);
        
        searchAndSelectOptionFromDropDown(claimantName, assesseeName);
        selectOptionFromDropDown(qualification, dataMap.get("Qualification?"));
        scrollToElement(getWebElementWithLabel(dateApplicationReceived));
        enter(dateApplicationReceived, dataMap.get("Date Application Received"));
        Thread.sleep(1000);
    }
    /**
     * Description: This method will enter field values in Institutional Exemption screen
     * @param dataMap: Map that is storing values from JSON file
     */

    public void createInstitutionalExemption(Map<String, String> dataMap) throws Exception {
    	Thread.sleep(2000);
        ReportLogger.INFO("Click 'New' button to fill the following details in the Institutional Exemption record : " + dataMap);
        Click(waitForElementToBeClickable(newExemptionButton));
        Thread.sleep(2000);
    	Click(institutionalExemptionRadioButton);
    	Click(exemptionRecordTypeNextButton);
       	String queryForID = "SELECT FirstName, LastName FROM Account WHERE Type In('Person','Business') and FirstName = 'DoNot'";
        HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(queryForID);
        String assesseeName = response.get("FirstName").get(0) + " " + response.get("LastName").get(0);
        String apnNumber = fetchActiveAPN();
        searchAndSelectOptionFromDropDown(apn, apnNumber);
        searchAndSelectOptionFromDropDown(claimantName, assesseeName);
        enter(claimantSSN, dataMap.get("ClaimantSSN"));
        selectOptionFromDropDown(exemptionCode, dataMap.get("exemptionCode"));
        enter(Penalty, dataMap.get("PenaltyPercentage"));
        selectOptionFromDropDown(qualification, dataMap.get("Qualification"));
        enter(dateApplicationReceived, dataMap.get("DateApplicationReceived"));
        scrollToElement(getWebElementWithLabel(dateApplicationReceived));
        enter(dateApplicationReceived, dataMap.get("DateApplicationReceived"));
        enter(dateAcquiredProperty, dataMap.get("DateAquiredProperty"));
        enter(dateOccupyProperty, dataMap.get("DateOccupyProperty"));
        Thread.sleep(1000);
        ReportLogger.INFO("Click 'Save' button to save the details entered in Exemption record");
        saveExemptionRecord();
    }
    /**
     * Description: This method will enter field values in Value adjustment on Institutional Exemption screen
     */
    public void createNewVAsOnInstitutionalExemption() throws Exception {
    	Thread.sleep(2000);
        ReportLogger.INFO("Click 'New' button to fill the following details in the VAs record : ");
        Click(waitForElementToBeClickable(creatNewVAsButton));
        Thread.sleep(2000);
    	Click(institutionalExemptionRadioButton);
    	Click(exemptionRecordTypeNextButton);
    	String date = DateUtil.getCurrentDate("MM/dd/yyyy");
    	String strRollYear = ExemptionsPage.determineRollYear(date);
    	strRollYear = String.valueOf(Integer.parseInt(strRollYear));
        selectOptionFromDropDown(rollYearSettingOnVAs, strRollYear);
        selectOptionFromDropDown(RPSLfieldOnVAs, "Exemption Limits - "+strRollYear);
        enter(ExemptionAmountUserAdjusted,"2000");
        enter(Remark,"User adjusted exemption amount is 2000.");
        saveExemptionRecord();
        
    }
}