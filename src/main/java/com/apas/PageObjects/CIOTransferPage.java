package com.apas.PageObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;

public class CIOTransferPage extends ApasGenericPage {
	Util objUtil;

	public CIOTransferPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	public String componentActionsButtonLabel = "Component Actions";
	public String submitforApprovalButtonLabel = "Submit for Approval";
	public String copyToMailToButtonLabel = "Copy to Mail To";
	public String calculateOwnershipButtonLabel  = "Calculate Ownership";
	public String checkOriginalTransferListButtonLabel = "Check Original Transfer List";
	public String backToWIsButtonLabel = "Back to WIs";
	
	@FindBy(xpath = "//div[contains(@class,'uiOutputRichText')]")
	public WebElement confirmationMessageOnTranferScreen;
	
	@FindBy(xpath = "//div[@class='highlights slds-clearfix slds-page-header slds-page-header_record-home']//ul[@class='slds-button-group-list']//lightning-primitive-icon")
	public WebElement quickActionButtonDropdownIcon;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Back']")
	public WebElement quickActionOptionBack;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Approve']")
	public WebElement quickActionOptionApprove;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Return']")
	public WebElement quickActionOptionReturn;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Submit for Review']")
	public WebElement quickActionOptionSubmitForReview;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[@class='slds-truncate' and text()='Review Complete']")
	public WebElement quickActionOptionReviewComplete;
	
	

	  
}
