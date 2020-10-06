package com.apas.PageObjects;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Reports.ReportLogger;
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
	
	@FindBy(xpath="//a[@data-label='Needs My Approval']")
	public WebElement lnkTABNeedsMyApproval;
	
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
	
	@FindBy(xpath="//button[@title='Approve']")
	public WebElement btnApprove;
	
	@FindBy(xpath="//button[@title='Reassign']")
	public WebElement btnReassign;
	
	@FindBy(xpath="//button[@title='Return']")
	public WebElement btnReturn;
	
	@FindBy(xpath="//button[@title='Put On Hold']")
	public WebElement btnPutOnHold;
	
	@FindBy(xpath= "//label[contains(text(),'Returned Reason')]//../div/input")
	public WebElement txtReturnedReason;
	
	@FindBy(xpath="//footer/button[text()='Save']")
	public WebElement btnSaveOnReturnDlg;
	
	@FindBy(xpath="//a[@data-label='Linked Items']")
	public WebElement lnkLinkedItems;
	
	@FindBy(xpath="//a[@data-label='Details']")
	public WebElement lnkDetails;
	
	
	
	
	
	public String getWorkItemDetails(String newExemptionName, String WIStatus, String WIType, String WISubType, String WIReference) {
		
         String query = "Select Work_Item__r.Name,Work_Item__r.Request_Type__c "
         		   + "from Work_Item_linkage__c "
	               + "where Exemption__r.name      = '"+newExemptionName+"' "
	               + "and Work_Item__r.Status__c   = '"+WIStatus+"' "
	               + "and Work_Item__r.Type__c     = '"+WIType+"' "
	               + "and Work_Item__r.Sub_Type__c = '"+WISubType+"' "
	               + "and Work_Item__r.Reference__c ='"+WIReference+"'" ;
		return query;
         
        }
	public String getActiveExistingExemptionWithCompletedWiStatus(String WIStatus, String WIType, String WISubType, String WIReference) {
		
        String query = "Select Exemption__r.Name,Work_Item__r.Name,Work_Item__r.Request_Type__c "
        		   + "from Work_Item_linkage__c "
	               + "where Work_Item__r.Status__c   = '"+WIStatus+"' "
	               + "and Work_Item__r.Type__c     = '"+WIType+"' "
	               + "and Work_Item__r.Sub_Type__c = '"+WISubType+"' "
	               + "and Work_Item__r.Reference__c ='"+WIReference+"'"
	               + "and Exemption__r.Status__c = 'Active'" ;
	               
		return query;
        
       }
	public String getWorkItemCountforExemption(String ExemptionName) {
		
		String query = "Select count() from Work_Item_Linkage__c where Exemption__r.Name="+ExemptionName+"'";
		
		return query;
		
		
	}
	public String getWorkItemDetailsForVA(String VAName, String WIStatus, String WIType, String WISubType, String WIReference) {
		
        String query = "Select Work_Item__r.Name,Work_Item__r.Request_Type__c "
        		   + "from Work_Item_linkage__c "
	               + "where Value_Adjustments__r.Name  = '"+VAName+"' "
	               + "and Work_Item__r.Status__c   = '"+WIStatus+"' "
	               + "and Work_Item__r.Type__c     = '"+WIType+"' "
	               + "and Work_Item__r.Sub_Type__c = '"+WISubType+"' "
	               + "and Work_Item__r.Reference__c ='"+WIReference+"'" ;
        
		return query;
        
       }
	public WebElement searchWIinGrid(String WIName) {
		WebElement actualWIName=null;
		
		try {
			 actualWIName = driver.findElementByXPath("//table/tbody//tr/th//a[@title='"+WIName+"']");
		} catch (Exception e) {
			
			ReportLogger.INFO(e.getMessage());
		}
		
		return actualWIName;
	}
	
	public void clickExemptionNameLink(String ExemptionName) throws IOException {
		
		WebElement lnkExemptionName = driver.findElementByXPath("//a[text()='"+ExemptionName+"']");
		objPage.Click(lnkExemptionName);
	}
	
	public void clickCheckBoxForSelectingWI(String WIName) throws IOException {
		
		WebElement chkBoxWI = driver.findElementByXPath("//table/tbody//tr/th//a[@title='"+WIName+"']//..//..//..//..//..//..//input)");
				
		objPage.Click(chkBoxWI);
	}
	
	public WebElement searchLinkedExemption(String ExemptionName) {
		
        WebElement actualWIName=null;
		
		try {
			 actualWIName = driver.findElementByXPath("//table/tbody//th[@data-label='Name']//a[@title='"+ExemptionName+"']");
		} catch (Exception e) {
			
			ReportLogger.INFO(e.getMessage());
		}
		
		return actualWIName;
	}
	
	public WebElement searchRequestTypeNameonWIDetails(String RequestTypeName) {
		
		WebElement actualRequestTypeName = null;
		
		 try {
			 actualRequestTypeName = driver.findElement(By.xpath("//*[text()='"+RequestTypeName+"']"));
		} catch (Exception e) {
			
			ReportLogger.INFO(e.getMessage());
		}
		return actualRequestTypeName;
	}
		
 }

