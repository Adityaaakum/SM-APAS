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

public class AuditTrailPage extends ApasGenericPage {
	Util objUtil;

	public AuditTrailPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	public String eventLibraryLabel = "Event Library";
	public String eventTypeLabel="Event Type";
	public String statusLabel="Status";
	public String requestOriginLabel="Request Origin";
	public String eventIdLabel="Event ID";
	public String eventTitleLabel="Event Title";
	public String relatedCorrespondenceLabel="Related Correspondence";
	public String relatedBusinessEventLabel="Related Business Event";
	public String descriptionLabel = "Description";
	public String nameLabel = "Name";
	public String recordTypeLabel = "Record Type";
	public String dovLabel ="Date of Value";
	public String dorLabel ="Date of Recording";
	public String rollYearLabel ="Applicable To Roll Year";
	public String relatedActionLabel ="Related Action";
	public String eventNumberLabel = "Event Number";
	
	@FindBy(xpath="//li[@title='Related Business Records']//a[@data-label='Related Business Records']")
	public WebElement relatedBusinessRecords;
	
	@FindBy(xpath="//div[contains(@class,'outputLookupContainer forceOutputLookupWithPreview')]/a[@target='_blank'][.='Values Allocated']")
	public WebElement valuesAllocated;     
	
	
  	   
}