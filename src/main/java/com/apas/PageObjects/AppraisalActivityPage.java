package com.apas.PageObjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;

public class AppraisalActivityPage extends ApasGenericPage implements modules {
	Util objUtil;
	SalesforceAPI salesforceApi;
	MappingPage objMappingPage;
	WorkItemHomePage objWorkItemHomePage;

	public AppraisalActivityPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
		salesforceApi = new SalesforceAPI();
		objMappingPage = new MappingPage(driver);

	}
	
	
	public String returnButton ="Return";
	public String appraisalActivityStatus ="Appraiser Activity Status";
	public String rejectButton ="Reject";
	public String rejectionReasonForIncorrectCioDetermination ="CIO - Incorrect Transfer Work & Determination";
	public String nextButton ="Next";
	public String eventCodeLabel ="Event Code";
	public String dorLabel ="DOR";
	public String dovLabel ="DOV";
	public String apnLabel ="APN";
	
	
	@FindBy(xpath = "//select[@name='Rejection_Reason_PickList']")
	public WebElement rejectionReasonList;
	
	@FindBy(xpath ="//*[@class='slds-modal__footer']//*[text()='Save']")
	public WebElement calculatePenaltySaveButton;
	
	/*
	 * This method is to find the xpath of the edit pencil icon.
	 */
	 public WebElement appraisalActivityEditValueButton(String feildName) {
		 
		 String xpath="//*[@class='base-record-form-header-container slds-card__body slds-card__body_inner']//*[@title='Edit "+ feildName+"']";
		 WebElement xPath = driver.findElement(By.xpath(xpath));
		 return xPath;
	 }
}