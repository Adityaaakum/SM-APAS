package com.apas.PageObjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.generic.ApasGenericFunctions;

/*
 * public class WorkItemMngmntHomePage extends Page {
 * 
 * 
 * Logger logger = Logger.getLogger(LoginPage.class); ApasGenericFunctions
 * objApasGenericFunctions; ApasGenericPage objApasGenericPage; SalesforceAPI
 * salesforceAPI; Util objUtil; Page objPage;
 * 
 * 
 * public WorkItemMngmntHomePage(RemoteWebDriver driver) { super(driver);
 * PageFactory.initElements(driver, this); objApasGenericPage = new
 * ApasGenericPage(driver); objPage = new Page(driver); objUtil = new Util(); }
 * 
 * @FindBy(xpath="//a[@title='Home']") public WebElement lnkTABHome;
 * 
 * @FindBy(xpath="//a/span[text()='Work Items']") public WebElement
 * lnkTABWorkItems;
 * 
 * @FindBy(
 * xpath="//label/span[contains(text(),'Show RP')]/..//span[@class='slds-checkbox_faux_container']"
 * ) public WebElement chkShowRP;
 * 
 * @FindBy(xpath="//a[@data-label='In Progress']") public WebElement
 * lnkTABInProgress;
 * 
 * @FindBy(xpath="//a[@data-label='In Pool']") public WebElement lnkTABInPool;
 * 
 * @FindBy(xpath="//a[@data-label='Submitted for Approval']") public WebElement
 * lnkTABMySubmittedforApproval;
 * 
 * @FindBy(xpath="//a[@data-label='Needs My Approval']") public WebElement
 * lnkTABNeedsMyApproval;
 * 
 * @FindBy(xpath="//a[@data-label='Completed']") public WebElement
 * lnkTABCompleted;
 * 
 * @FindBy(xpath="//input[@placeholder='Search Work Pool']") public WebElement
 * txtSearchWorkPool;
 * 
 * @FindBy(xpath="//input[@name='status']") public WebElement selStatus;
 * 
 * @FindBy(xpath="//input[@name='type']") public WebElement selType;
 * 
 * @FindBy(xpath="//input[@name='action']") public WebElement selAction;
 * 
 * @FindBy(xpath="//button[@title='Approve']") public WebElement btnApprove;
 * 
 * @FindBy(xpath="//button[@title='Reassign']") public WebElement btnReassign;
 * 
 * @FindBy(xpath="//button[@title='Return']") public WebElement btnReturn;
 * 
 * @FindBy(xpath="//button[@title='Put On Hold']") public WebElement
 * btnPutOnHold;
 * 
 * @FindBy(xpath= "//label[contains(text(),'Returned Reason')]//../div/input")
 * public WebElement txtReturnedReason;
 * 
 * @FindBy(xpath="//footer/button[text()='Save']") public WebElement
 * btnSaveOnReturnDlg;
 * 
 * @FindBy(xpath="//button[@title='Accept Work Item']") public WebElement
 * btnAcceptWorkItem;
 * 
 * @FindBy(xpath="//button[@title='Mark Complete']") public WebElement
 * btnMarkComplete;
 * 
 * @FindBy(xpath="//a[@data-label='Linked Items']") public WebElement
 * lnkLinkedItems;
 * 
 * //@FindBy(xpath="//div/lightning-tab-bar/ul//li/a[@data-label='Details']")
 * 
 * @FindBy(xpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//li[@data-label='Details']"
 * ) public WebElement lnkDetails;
 * 
 * 
 * 
 * public HashMap<String, ArrayList<String>> getWorkItemDetails(String
 * newExemptionName, String WIStatus, String WIType, String WISubType, String
 * WIReference) throws InterruptedException {
 * 
 * 
 * String query = "Select Work_Item__r.Name,Work_Item__r.Request_Type__c " +
 * "from Work_Item_linkage__c " +
 * "where Exemption__r.name      = '"+newExemptionName+"' " +
 * "and Work_Item__r.Status__c   = '"+WIStatus+"' " +
 * "and Work_Item__r.Type__c     = '"+WIType+"' " +
 * "and Work_Item__r.Sub_Type__c = '"+WISubType+"' " +
 * "and Work_Item__r.Reference__c ='"+WIReference+"'" ;
 * 
 * salesforceAPI = new SalesforceAPI();
 * 
 * String sqlExemption_Id =
 * "Select Id from Exemption__c where Name = '"+newExemptionName+"'";
 * HashMap<String, ArrayList<String>> response_1 =
 * salesforceAPI.select(sqlExemption_Id); String Exemption_Id =
 * response_1.get("Id").get(0);
 * 
 * String slqWork_Item_Id =
 * "Select Work_Item__c from Work_Item_Linkage__c where Exemption__c = '"
 * +Exemption_Id+"'and Work_Item__r.Status__c ='"+WIStatus+"'";
 * Thread.sleep(5000); HashMap<String, ArrayList<String>> response_2 =
 * salesforceAPI.select(slqWork_Item_Id); String WorkItem_Id =
 * response_2.get("Work_Item__c").get(0);
 * 
 * String slqWork_Item_Details =
 * "Select Name, Request_Type__c from Work_Item__c "+
 * "where Id = '"+WorkItem_Id+"' "+ "and Type__c     = '"+WIType+"' " +
 * "and Sub_Type__c = '"+WISubType+"' " + "and Reference__c ='"+WIReference+"'";
 * HashMap<String, ArrayList<String>> response_3 =
 * salesforceAPI.select(slqWork_Item_Details); return response_3 ;
 * 
 * }
 * 
 * 
 * public HashMap<String, ArrayList<String>> getWorkItemDetailsForVA(String
 * VAName, String WIStatus, String WIType, String WISubType, String WIReference)
 * throws InterruptedException {
 * 
 * 
 * String query = "Select Work_Item__r.Name,Work_Item__r.Request_Type__c " +
 * "from Work_Item_linkage__c " +
 * "where Value_Adjustments__r.Name  = '"+VAName+"' " +
 * "and Work_Item__r.Status__c   = '"+WIStatus+"' " +
 * "and Work_Item__r.Type__c     = '"+WIType+"' " +
 * "and Work_Item__r.Sub_Type__c = '"+WISubType+"' " +
 * "and Work_Item__r.Reference__c ='"+WIReference+"'" ;
 * 
 * salesforceAPI = new SalesforceAPI();
 * 
 * String sqlValueAdjustment_Id =
 * "Select Id from Value_Adjustments__c where Name = '" + VAName + "'";
 * HashMap<String, ArrayList<String>> response_1 =
 * salesforceAPI.select(sqlValueAdjustment_Id); String ValueAdjustment_Id =
 * response_1.get("Id").get(0);
 * 
 * String slqWork_Item_Id =
 * "Select Work_Item__c from Work_Item_Linkage__c where Value_Adjustments__c = '"
 * + ValueAdjustment_Id + "'"; Thread.sleep(2000); HashMap<String,
 * ArrayList<String>> response_2 = salesforceAPI.select(slqWork_Item_Id); String
 * WorkItem_Id = response_2.get("Work_Item__c").get(0);
 * 
 * String slqWork_Item_Details =
 * "Select Name, Request_Type__c from Work_Item__c " + "where Id = '" +
 * WorkItem_Id + "' " + "and Status__c   = '" + WIStatus + "' " +
 * "and Type__c     = '" + WIType + "' " + "and Sub_Type__c = '" + WISubType +
 * "' " + "and Reference__c ='" + WIReference + "'"; HashMap<String,
 * ArrayList<String>> response_3 = salesforceAPI.select(slqWork_Item_Details);
 * return response_3;
 * 
 * }
 * 
 * 
 * public String searchandClickWIinGrid(String WIName) { WebElement actualWIName
 * = null; String actualWINamefrmGrid = null;
 * 
 * try { actualWIName =
 * driver.findElementByXPath("//table/tbody//tr/th//a[@title='" + WIName +
 * "']"); javascriptClick(actualWIName); actualWINamefrmGrid =
 * actualWIName.getText(); } catch (Exception e) {
 * 
 * ReportLogger.INFO(e.getMessage()); } return actualWINamefrmGrid; }
 * 
 * 
 * public void clickExemptionNameLink(String ExemptionName) throws IOException {
 * 
 * WebElement lnkExemptionName = driver.findElementByXPath("//a[text()='" +
 * ExemptionName + "']"); objPage.Click(lnkExemptionName); }
 * 
 * public void clickCheckBoxForSelectingWI(String WIName) throws IOException {
 * 
 * WebElement chkBoxWI = driver.findElementByXPath(
 * "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table/tbody//tr/th//a[@title='"
 * + WIName + "']//..//..//..//..//..//..//input");
 * 
 * // objPage.Click(chkBoxWI); javascriptClick(chkBoxWI); }
 * 
 * public String searchLinkedExemptionOrVA(String ExemptionOrVAName) {
 * 
 * WebElement actualWIName = null; String actualExemptionNameFrmGrid = null;
 * 
 * try { actualWIName =
 * driver.findElementByXPath("//table/tbody//tr//a[@title='" + ExemptionOrVAName
 * + "']"); actualExemptionNameFrmGrid = actualWIName.getText();
 * 
 * } catch (Exception e) {
 * 
 * ReportLogger.INFO(e.getMessage()); }
 * 
 * return actualExemptionNameFrmGrid; }
 * 
 * public String searchRequestTypeNameonWIDetails(String RequestTypeName) {
 * 
 * WebElement actualRequestTypeName = null; String actualRequestTypeNameFrmGrid
 * = null;
 * 
 * try { actualRequestTypeName = driver.findElement(By.xpath("//*[text()='" +
 * RequestTypeName + "']")); actualRequestTypeNameFrmGrid =
 * actualRequestTypeName.getText(); } catch (Exception e) {
 * 
 * ReportLogger.INFO(e.getMessage()); } return actualRequestTypeNameFrmGrid; }
 * 
 * }
 */