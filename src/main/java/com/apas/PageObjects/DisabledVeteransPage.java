package com.apas.PageObjects;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class DisabledVeteransPage extends Page {
	String exemptionFileLocation = "";

	public DisabledVeteransPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//a[@title='New']/div[@title='New'][1]")
	public WebElement newExemptionButton;

	@FindBy(xpath = "//input[@title='Search Parcels']")
	public WebElement apn;

	@FindBy(xpath = "//input[@title='Search Assessees']")
	public WebElement ownerApplyingName;

	@FindBy(xpath = "//label[contains(.,'Owner Applying SSN')]/following::input[1]")
	public WebElement ownerApplyingSSN;

	@FindBy(xpath = "//label[contains(.,'Spouse Name')]/following::input[1]")
	public WebElement spouseName;

	@FindBy(xpath = "//label[contains(.,'Spouse SSN')]/following::input[1]")
	public WebElement spouseSSN;

	@FindBy(xpath = "//label[contains(.,'Name of Veteran')]/following::input[1]")
	public WebElement nameOfVeteran;

	@FindBy(xpath = "//label[contains(.,'Veteran SSN')]/following::input[1]")
	public WebElement veteranSSN;

	@FindBy(xpath = "//label[contains(.,'Application Date')]/following::input[1]")
	public WebElement applicationDate;

	@FindBy(xpath = "//label[contains(.,'Date of Death of Veteran)]/following::input[1]")
	public WebElement dateOfDeathOfVeteran;

	@FindBy(xpath = "//label[contains(.,'Email Address')]/following::input[1]")
	public WebElement emailAddress;

	@FindBy(xpath = "//label[contains(.,'Telephone')]/following::input[1]")
	public WebElement telephone;

	@FindBy(xpath = "//label[contains(.,'Date Acquired Property')]/following::input[1]")
	public WebElement dateAquiredProperty;

	@FindBy(xpath = "//label[contains(.,'Date Occupy Property')]/following::input[1]")
	public WebElement dateOccupyProperty;

	@FindBy(xpath = "//label[contains(.,'Date of Notice of 100% RatingDate when USVDA provided disability rating*')]/following::input[1]")
	public WebElement dateOfNotice;

	@FindBy(xpath = "//label[contains(.,'Effective Date of 100% USDVA Rating')]/following::input[1]")
	public WebElement effectiveDateOfUSDVA;

	@FindBy(xpath = "//label[contains(.,'Prior Residence Street Address')]/following::input[1]")
	public WebElement priorResidenceStreetAddress;

	@FindBy(xpath = "//label[contains(.,'Date Move From Prior Residence')]/following::input[1]")
	public WebElement dateMoveFromProprResidence;

	@FindBy(xpath = "//label[contains(.,'Prior Residence City')]/following::input[1]")
	public WebElement priorResidenceCity;

	@FindBy(xpath = "//label[contains(.,'Prior Residence County')]/following::input[1]")
	public WebElement priorResidenceCounty;

	@FindBy(xpath = "//label[contains(.,'End Date of Rating')]/following::input[1]")
	public WebElement endDateOfRating;

	@FindBy(xpath = "//span[@data-aura-class='uiPicklistLabel' and contains(.,'Unmarried Spouse of Deceased Veteran?')]/following::div[1]//div/a")
	public WebElement unmarriedSpouseOfDisabledVeteran;

	@FindBy(xpath = "//span[@data-aura-class='uiPicklistLabel' and contains(.,'DV Exemption on Prior Residence')]/following::div[1]//div/a")
	public WebElement dvExemptionOnPriorResidence;

	@FindBy(xpath = "//span[@data-aura-class='uiPicklistLabel' and contains(.,'End Rating Reason')]/following::div[1]//div/a")
	public WebElement endRatingReason;

	@FindBy(xpath = "//span[@data-aura-class='uiPicklistLabel' and contains(.,'Prior Residence State')]/following::div[1]//div/a")
	public WebElement priorResidenceState;

	@FindBy(xpath = "//span[@data-aura-class='uiPicklistLabel' and contains(.,'Prior Residence State')]/following::div[1]//div/a")
	public WebElement basisForClaim;

}
