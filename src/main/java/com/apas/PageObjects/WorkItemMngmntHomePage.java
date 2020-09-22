package com.apas.PageObjects;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Utils.Util;
import com.apas.generic.ApasGenericFunctions;

public class WorkItemMngmntHomePage extends Page {
	
	Logger logger = Logger.getLogger(LoginPage.class);
	ApasGenericFunctions objApasGenericFunctions;
	ApasGenericPage objApasGenericPage;
	Util objUtil;
	Page objPage;


	public WorkItemMngmntHomePage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericPage = new ApasGenericPage(driver);
		objPage = new Page(driver);
		objUtil = new Util();
	}
	
	@FindBy(xpath="//a[@title='Home']")
	public WebElement lnkTABHome;
	
	@FindBy(xpath="//a/span[text()='Work Items']")
	public WebElement lnkTABWorkItems;	
	
	@FindBy(xpath="//label/input[@type='checkbox']")
	public WebElement chkShowRP;
		
	@FindBy(xpath="//a[@data-label='In Progress']")
	public WebElement lnkTABInProgress;
	
	@FindBy(xpath="//a[@data-label='In Pool']")
	public WebElement lnkTABInPool;
	
	@FindBy(xpath="//a[@data-label='My Submitted for Approval']")
	public WebElement lnkTABMySubmittedforApproval;
	
	@FindBy(xpath="//a[@data-label='Completed']")
	public WebElement lnkTABCompleted;
	
	@FindBy(xpath="//input[@placeholder='Search Work Pool']")
	public WebElement txtSearchWorkPool;
	
	@FindBy(xpath="//input[@name='status']")
	public WebElement selStatus;
	
	@FindBy(xpath="//input[@name='type']")
	public WebElement selType;
	
	@FindBy(xpath="//input[@name='action']")
	public WebElement selAction;
	
	
	public String getWorkItemDetails(String newExemptionName, String WIStatus, String WIType, String WISubType, String WIReference) {
		
         String query = "Select Work_Item__r.name,Work_Item__r.Request_Type__c from Work_Item_linkage__c "
	               + "where Exemption__r.name      = '"+newExemptionName+"' "
	               + "and Work_Item__r.Status__c   = '"+WIStatus+"' "
	               + "and Work_Item__r.Type__c     = '"+WIType+"' "
	               + "and Work_Item__r.Sub_Type__c = '"+WISubType+"' "
	               + "and Work_Item__r.Reference__c ='"+WIReference+"'" ;
		return query;
         
         
	}
	
	


}
