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
	public String typeDropdownLabel = "Type";

	@FindBy(xpath = "//div[contains(text(),'List Views')]")
	public WebElement selectListViewButton;

	@FindBy(xpath = "//label[contains(., 'Agency')]//input[@type = 'radio']")
	public WebElement agencyRadioButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@role='tab'][@data-label='Details']")
	public WebElement detailsTab;

	@FindBy(xpath = "//h2[contains(text(),'Edit Person Assessee')]")
	public WebElement visibleEditpopUp;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//button[text()='Edit']")
	public WebElement editButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'slds-listbox__option_plain') or contains(@class,'flowruntimeBody')]//span[text()='Type']/../following-sibling::div")
	public WebElement typeDropdown;

	public void createNewAssesee(Map<String, String> dataMap) throws Exception {

		String firstName = dataMap.get("First Name");
		String lastName = dataMap.get("Last Name or Name");
		String ssn = dataMap.get("SSN");
		String email = dataMap.get("Email");
		String careof = dataMap.get("Care Of");
		String phone = dataMap.get("Phone");

		enter(firstNameLabel, firstName);
		enter(lastNameLabel, lastName);
		enter(ssnLabel, ssn);
		enter(emailLabel, email);
		enter(careOfLabel, careof);
		enter(phoneLabel, phone);
		
		Click(getButtonWithText(saveButton));

	}

	public void createNewAgency(Map<String, String> dataMap) throws Exception {

		String firstName = dataMap.get("First Name");
		String lastName = dataMap.get("Last Name or Name");
		String email = dataMap.get("Email");
		String careof = dataMap.get("Care Of");
		String phone = dataMap.get("Phone");
		String type = dataMap.get("Type");

		WebElement fieldElement;
		Click(agencyRadioButton);
		Click(getButtonWithText(nextButton));

		enter(firstNameLabel, firstName);
		enter(lastNameLabel, lastName);
		enter(emailLabel, email);
		enter(careOfLabel, careof);
		enter(phoneLabel, phone);

		String fieldXpath = "//span[text()='" + typeDropdownLabel + "']/../following-sibling::div//a";
		fieldElement = locateElement(fieldXpath, 20);
		selectOptionFromDropDown(fieldElement, type);
		Click(getButtonWithText(saveButton));

	}

	public String updateAssesee(String careofValue) throws Exception {

		enter(careOfLabel, careofValue);
		Click(getButtonWithText(saveButton));
		return careofValue;

	}


}