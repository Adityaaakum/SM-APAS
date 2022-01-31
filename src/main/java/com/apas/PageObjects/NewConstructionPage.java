package com.apas.PageObjects;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Utils.Util;

public class NewConstructionPage extends ApasGenericPage {

	Util objUtil;

	public NewConstructionPage (RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	@FindBy(xpath = "//lightning-formatted-rich-text[contains(@class,'slds-rich-text-editor__output')]//p")
	public WebElement messageForDemolition;

	@FindBy(xpath = "//button[@title='Close this window']")
	public WebElement crossButton;
	
	@FindBy(xpath = "//button[@name='CancelEdit']")
	public WebElement cancelButton;
	
	@FindBy(xpath = "//*[contains(@class,'forceFormPageError')]//ul//li")
	public WebElement errorMsg;
	
	@FindBy(xpath = "//strong[contains(normalize-space(),'New Construction - Manual Entry')]")
	public WebElement ncAuditTrail;

	public String createDemolitionWIBtn = "Create Demolition WI";
	public String submitForApprovalNCWIBtn = "Submit For Approval(NC)";
	public String errorMessageWithLinks = "//*[contains(@class,'forceFormPageError')]//strong//following::a";
	public String errorMsgDown = ("//*[contains(@class,'forceFormPageError')]//ul");
	public String cancel = "Cancel";
	public String returnButton = "Return";
	public String approveNCButton = "Approve(NS)";
	public String puc = "PUC Code";
	public String improvementCashValueLabel = "Improvement Cash Value";
	public String landCashValueLabel = "Land Cash Value";
	public String nextButton = "Next";

	


}
