package com.apas.PageObjects;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AsseseePage extends ApasGenericPage {
	Util objUtil;
	SalesforceAPI objSalesforceAPI = new SalesforceAPI();

	public AsseseePage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	public String newButtonText = "New";

	public String assesseeNameLabel = "Assessee Name";
	public String newAssesseeLabel = "New Assessee";
	public String firstNameLabel = "First Name";
	public String lastNameLabel = "Last Name or Name";
	public String ssnLabel = "SSN";
	public String emailLabel = "Email";
	public String emailOptOutLabel = "Email Opt Out";
	public String typeLabel = "Type";
	public String careOfLabel = "Care Of";
	public String assesseeAddressLabel = "Assessee Address";
	public String phoneLabel = "Phone";
	public String nextButton = "Next";
	public String newAssesseeNextScreenLabel = "New Assessee: Business/Person";
	public String saveButton = "Save";
	public String editButtonText = "Edit";
	public String typeDropdownLabel = "type";

	// div[@data-aura-class = 'forceListViewPicker']//span[text() = 'Assessees']
	@FindBy(xpath = "//div[contains(text(),'List Views')]")
	public WebElement selectListViewButton;

	@FindBy(xpath = "//label[contains(., 'Business/Person')]//input[@type = 'radio']")
	public WebElement business_PersonRadioButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@role='tab'][@data-label='Details']")
	public WebElement detailsTab;

	@FindBy(xpath = "//h2[contains(text(),'Edit Person Assessee')]")
	public WebElement visibleEditpopUp;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//forcegenerated-adg-rollup_component___force-generated__flexipage_-record-page___-account_-record_-page1___-account___-v-i-e-w//button[text()='Edit']")
	public WebElement editButton;

	public void createNewAssesee(Map<String, String> dataMap) throws Exception {

		String firstName = dataMap.get("First Name");
		String lastName = dataMap.get("Last Name or Name");
		String ssn = dataMap.get("SSN");
		String email = dataMap.get("Email");
		String careof = dataMap.get("Care Of");
		String phone = dataMap.get("Phone");
		String type = dataMap.get("Type");

		Click(business_PersonRadioButton);
		Click(getButtonWithText(nextButton));

		if (firstName != null)
			enter(firstNameLabel, firstName);
		if (lastName != null)
			enter(lastNameLabel, lastName);
		if (type != null)
			selectOptionFromDropDown(typeDropdownLabel, type);
		if (ssn != null)
			enter(ssnLabel, ssn);
		if (email != null)
			enter(emailLabel, email);
		if (careof != null)
			enter(careOfLabel, careof);
		if (phone != null)
			enter(phoneLabel, phone);
		Click(getButtonWithText(saveButton));

	}

	public void createNewAgency(Map<String, String> dataMap) throws Exception {

		String firstName = dataMap.get("First Name");
		String lastName = dataMap.get("Last Name or Name");
		String ssn = dataMap.get("SSN");
		String email = dataMap.get("Email");
		String careof = dataMap.get("Care Of");
		String phone = dataMap.get("Phone");

		Click(business_PersonRadioButton);
		Click(getButtonWithText(nextButton));

		if (firstName != null)
			enter(firstNameLabel, firstName);
		if (lastName != null)
			enter(lastNameLabel, lastName);
		if (ssn != null)
			enter(ssnLabel, ssn);
		if (email != null)
			enter(emailLabel, email);
		if (careof != null)
			enter(careOfLabel, careof);
		if (phone != null)
			enter(phoneLabel, phone);
		Click(getButtonWithText(saveButton));

	}

	public String updatewAssesee(String careofValue) throws Exception {

		if (careofValue != null)
			enter(careOfLabel, careofValue);
		Click(getButtonWithText(saveButton));

		return careofValue;

	}

}