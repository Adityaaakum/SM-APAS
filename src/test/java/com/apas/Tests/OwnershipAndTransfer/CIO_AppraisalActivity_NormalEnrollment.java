package com.apas.Tests.OwnershipAndTransfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.AppraisalActivityPage;

import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class CIO_AppraisalActivity_NormalEnrollment extends TestBase implements users {

	private RemoteWebDriver driver;
	Page objPage;
	ParcelsPage objParcelsPage;
	ApasGenericPage objApasGenericPage;
	CIOTransferPage objCIOTransferPage;
	Util objUtil;
	SoftAssertion softAssert;
	WorkItemHomePage objWorkItemHomePage;	
	MappingPage objMappingPage;
	AppraisalActivityPage objAppraisalActivity;

	SalesforceAPI salesforceAPI = new SalesforceAPI();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objParcelsPage = new ParcelsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objCIOTransferPage = new CIOTransferPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		objMappingPage =  new MappingPage(driver);
		objAppraisalActivity= new AppraisalActivityPage(driver);
		
	}
	/*
	 * Verify user is able to create Appraisal WI after approval of CIO WI for recorded documents
	 * 
	 * Last Modified by -Aditya 
	 * 
	 */
	
	@Test(description = "SMAB-T3786 : Verify that CIO supervisor on approval is able to create Appraisal WI for non exempted CIO transfers ", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ChangeInOwnershipManagement" }, enabled = true)
	public void OwnershipAndTransfer_CreateAppraisalActivityWorkItem(String loginUser) throws Exception {

		String excEnv = System.getProperty("region");

		JSONObject jsonForAppraiserActivity = objCIOTransferPage.getJsonObject();

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		String OwnershipAndTransferGranteeCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferGranteeCreationData, "dataToCreateGranteeWithIncompleteData");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
		Map<String, String> hashMapCreateAssessedValueRecord = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecord");
		
		String[]arrayForWorkItemAfterCIOSupervisorApproval    =  objCIOTransferPage.createAppraisalActivityWorkItemForRecordedCIOTransfer("Normal Enrollment", objCIOTransferPage.CIO_EVENT_CODE_COPAL, hashMapOwnershipAndTransferCreationData, hashMapOwnershipAndTransferGranteeCreationData, hashMapCreateOwnershipRecordData, hashMapCreateAssessedValueRecord);

		      objAppraisalActivity.login(APPRAISAL_SUPPORT);
		      objAppraisalActivity.globalSearchRecords(arrayForWorkItemAfterCIOSupervisorApproval[0]);
		      objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
			  objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
			  objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
			  objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			  softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Work Pool"), "Normal Enrollment", "SMAB-T3786: Verify that Workpool of the WI is direct enrollment");
			  softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Type"), "Appraiser", "SMAB-T3786: Verify that Type of the WI is Appraisal");
			  softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Action"), "Appraisal Activity", "SMAB-T3786: Verify that Action of the WI is Appraisal Activity");			    
			  
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel,10);
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();
			objWorkItemHomePage.switchToNewWindow(parentWindow);
			
			objAppraisalActivity.waitForElementToBeVisible(10, objAppraisalActivity.appraisalActivityStatus);
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.appraisalActivityStatus), "In Progress", "SMAB-T3786: Verify that status by default  of the appraisal activity is In Progress ");			    
			
            objAppraisalActivity.logout();
		      
		      
		      
	}
	

}
