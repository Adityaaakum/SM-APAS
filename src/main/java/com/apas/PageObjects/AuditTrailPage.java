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

public class AuditTrail extends ApasGenericPage {
	Util objUtil;

	public AuditTrail(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	
	public String EventLibrary = "Event Library";
	public String EventType="Event Type";
	public String Status="Status";
	public String RequestOrigin="Request Origin";
	public String EventId="Event ID";
	public String EventTitle="Event Title";
		
	
		             
	
	
  	   
}
