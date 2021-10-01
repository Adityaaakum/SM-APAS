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

}