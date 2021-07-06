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
	public String copyToMailToButtonLabel = "Copy to Mail To ";
	public String calculateOwnershipButtonLabel  = "Calculate Ownership";
	public String checkOriginalTransferListButtonLabel = "Check Original Transfer List";
	  
}
