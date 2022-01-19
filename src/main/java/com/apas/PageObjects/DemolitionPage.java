package com.apas.PageObjects;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Utils.Util;

public class DemolitionPage extends ApasGenericPage {

	Util objUtil;

	public DemolitionPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	@FindBy(xpath = "//lightning-formatted-rich-text[contains(@class,'slds-rich-text-editor__output')]//p")
	public WebElement errorMsgUp;

	@FindBy(xpath = "//button[@title='Close error dialog']")
	public WebElement crossButton;

	public String createNewConstructionWIBtn = "Create New Construction WI";
	public String submitForApprovalDEMOWIBtn = "Submit For Approval (DEMO)";
	public String errorMessageWithLinks = "//*[contains(@class,'forceFormPageError')]//strong//following::a";
	public String errorMsgDown = ("//*[contains(@class,'forceFormPageError')]//ul");
	public String cancel = "Cancel";
	public String returnButton = "Return";
	public String approveDemoButton = "Approve(Demo)";
	public String nextButton = "Next";

}
