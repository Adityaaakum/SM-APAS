package com.apas.Tests.OwnershipAndTransfer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import org.openqa.selenium.By;

import org.json.JSONObject;

import org.openqa.selenium.JavascriptExecutor;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.AppraisalActivityPage;
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;

import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;

import com.apas.config.testdata;
import com.apas.config.users;



public class CIO_AppraisalActivity_NormalEnrollment_Test extends TestBase implements users {

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
	AuditTrailPage objAuditTrail;
	JavascriptExecutor javascriptexecutor;

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
		objAuditTrail = new AuditTrailPage(driver);
		 javascriptexecutor = (JavascriptExecutor) driver;
		
	}
	/*
	 * Verify user is able to create Appraisal WI after approval of CIO WI for recorded documents and is able to reject that WI to make further Corrections through CIO-REVIEW WI and is able to reapprove that WI
	 * 
	 * Last Modified by -Aditya 
	 * 
	 */
	

	@Test(description = "SMAB-T4154,SMAB-T3637,SMAB-T3749,SMAB-T3736,SMAB-T3786,SMAB-T3738,SMAB-T3933,SMAB-T3807,SMAB-T3762,SMAB-T4317 : Verify that CIO supervisor on approval is able to create Appraisal WI for non exempted CIO transfers ", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "NormalEnrollment" ,"EconomicUnits", "ChangeInOwnershipManagement"}, enabled = true)
	public void OwnershipAndTransfer_CreateAppraisalActivityWorkItem(String loginUser) throws Exception {

		String excEnv = System.getProperty("region");
		
		 
		

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
      
		    //STEP 1- Create appraisal WI through CIO Transfer WI approval
		
		String[] arrayForWorkItemAfterCIOSupervisorApproval = objCIOTransferPage
				.createAppraisalActivityWorkItemForRecordedCIOTransfer("Normal Enrollment",
						objCIOTransferPage.CIO_EVENT_CODE_COPAL, hashMapOwnershipAndTransferCreationData,
						hashMapOwnershipAndTransferGranteeCreationData, hashMapCreateOwnershipRecordData,
						hashMapCreateAssessedValueRecord);
		   //Step 2- LOGIN with appraiser staff 
		
		      objAppraisalActivity.login(APPRAISAL_SUPPORT);
		      Thread.sleep(4000);

				String workItemForAppraiser = arrayForWorkItemAfterCIOSupervisorApproval[0];

				objAppraisalActivity.globalSearchRecords(workItemForAppraiser);
				objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
				objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
				objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
				objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
				
				//STEP 3- Verifying the WI details from details page
				
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Work Pool"), "Normal Enrollment",
						"SMAB-T3786: Verify that Workpool of the WI is direct enrollment");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Type"), "Appraiser",
						"SMAB-T3786: Verify that Type of the WI is Appraisal");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Action"), "Appraisal Activity",
						"SMAB-T3786: Verify that Action of the WI is Appraisal Activity");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Request Type"),objAppraisalActivity.getFieldValueFromAPAS("Type")+" "+"-"+" "+objAppraisalActivity.getFieldValueFromAPAS("Action")+" "+"-"+" "+objAppraisalActivity.getFieldValueFromAPAS("Reference") ,
						"SMAB-T3762: Verify that request type  of the WI is cancatination of Type ,Action and reference");
				
				
				//STEP 4 -Navigating to appraisal activity screen
				
				objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
				String parentWindow = driver.getWindowHandle();
				objWorkItemHomePage.switchToNewWindow(parentWindow);

				//STEP 5 -Validating the status and owner name  of appraiser activity 
				
				objAppraisalActivity.waitForElementToBeVisible(10, objAppraisalActivity.appraisalActivityStatus);
				softAssert.assertEquals(
						objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.appraisalActivityStatus),
						"In Progress",
						"SMAB-T3786: Verify that status by default  of the appraisal activity is In Progress ");
				softAssert.assertEquals(
						objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.ownerName),
						hashMapOwnershipAndTransferCreationData.get("Formatted Name1"),
						"SMAB-T3933: Verify that owner name equals formattedName 1 of the active mail to record  ");
       
				String DOV = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.dovLabel);
				String DOR = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.dorLabel);
				String EventCode = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.eventCodeLabel);
				String apnLabel = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.apnLabel);
				String getApnLabelID = salesforceAPI.select("Select Id from Parcel__c where name ='"+apnLabel+"'").get("Id").get(0);

				JSONObject jsonNObject = objParcelsPage.getJsonObject();

				jsonNObject.put("of_Parcels_in_Economic_Unit__c", "");
				salesforceAPI.update("Parcel__c", getApnLabelID, jsonNObject);
				driver.navigate().refresh();
				String partOfEconomicUnitValueAppraisalScreen = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.partofEconomicUnit);

				softAssert.assertEquals(
						partOfEconomicUnitValueAppraisalScreen,
						"",
						"SMAB-T4154: Verify on the Appraisal Activity Screen, there needs to be a Field - Part of Economic Unit which is populated by the value of this field on parcel level  ");
       
				
				jsonNObject.put("of_Parcels_in_Economic_Unit__c", "Yes");
				salesforceAPI.update("Parcel__c", getApnLabelID, jsonNObject);

				driver.navigate().refresh();
				partOfEconomicUnitValueAppraisalScreen = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.partofEconomicUnit);

				softAssert.assertEquals(
						partOfEconomicUnitValueAppraisalScreen,
						"Yes",
						"SMAB-T4154: Verify on the Appraisal Activity Screen, there needs to be a Field - Part of Economic Unit which is populated by the value of this field on parcel level  ");
       
				jsonNObject.put("of_Parcels_in_Economic_Unit__c", "No");
				salesforceAPI.update("Parcel__c", getApnLabelID, jsonNObject);

				driver.navigate().refresh();
				partOfEconomicUnitValueAppraisalScreen = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.partofEconomicUnit);

				softAssert.assertEquals(
						partOfEconomicUnitValueAppraisalScreen,
						"No",
						"SMAB-T4154: Verify on the Appraisal Activity Screen, there needs to be a Field - Part of Economic Unit which is populated by the value of this field on parcel level  ");
       

				
				// Step 6- Rejecting the appraisal WI
				
				objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,
						objAppraisalActivity.getButtonWithText(objAppraisalActivity.rejectButton));
				objAppraisalActivity.waitForElementToBeVisible(objAppraisalActivity.rejectionReasonList, 10);

				objAppraisalActivity.Click(objAppraisalActivity.rejectionReasonList);
				Select selectForRejectionReason = new Select(objAppraisalActivity.rejectionReasonList);

				selectForRejectionReason
						.selectByVisibleText(objAppraisalActivity.rejectionReasonForIncorrectCioDetermination);
				objAppraisalActivity.Click(objAppraisalActivity.getButtonWithText(objAppraisalActivity.nextButton));

				Thread.sleep(7000);
				
				// Step 7 -Verifying that user is navgated to home page after rejection
				
				softAssert.assertContains(driver.getCurrentUrl(), "/home",
						"SMAB-T3637,SMAB-T3738: Verify that User is navigates to Home Page after rejecting the appraisal activity");

				objAppraisalActivity.globalSearchRecords(workItemForAppraiser);
				objAppraisalActivity.waitForElementToBeClickable(objWorkItemHomePage.detailsTab, 10);
				objAppraisalActivity.Click(objWorkItemHomePage.detailsTab);
				
				// Step 8 -Validating the status of WI after rejection
				
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus),
						"Completed", "SMAB-T3637: Verifying that WI status is changed to completed after rejection by appraisal support staff");
				softAssert.assertEquals(
						objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.dropDownRejected), "Yes",
						"SMAB-T3637: Verify that  is Rejected? is updated to Yes after rejection by appraisal support user");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.rejectedReason),
						objAppraisalActivity.rejectionReasonForIncorrectCioDetermination, "SMAB-T3637: Verify that rejection reason is same as that appraiser staff selected while rejecting the appraiser WI");
				
				driver.navigate().to("https://smcacre--" + excEnv
						+ ".lightning.force.com/lightning/r/Transaction_Trail__c/"
						+ salesforceAPI.select(
								"SELECT Business_Event__c,ID from work_item_linkage__c where work_item__r.name='"
										+ workItemForAppraiser + "'")
								.get("Business_Event__c").get(0)
						+ "/view");
				objAppraisalActivity.waitForElementToBeVisible(10, objAuditTrail.statusLabel);
				String rollYear = objAuditTrail.getFieldValueFromAPAS(objAuditTrail.rollYearLabel);
				String parentTransactionTrailForAppraiser = objAuditTrail
						.getFieldValueFromAPAS(objAuditTrail.relatedCorrespondenceLabel);
				
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Event Title"),objAppraisalActivity.getFieldValueFromAPAS("Event Library")+" "+objAppraisalActivity.getFieldValueFromAPAS("Event ID")+" "+objCIOTransferPage.updateDateFormat(objAppraisalActivity.getFieldValueFromAPAS("Date of Value")) ,
						"SMAB-T4317: Verify that event title   of the AT is cancatination of Event Library ,Event id or Event number  and date of value");

				
				// Step 8 -Validating the status of WI after rejection
				
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.statusLabel), "Completed",
						"SMAB-T3637: Verify that AT status for appraisal  WI is completed after rejection ");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.recordTypeLabel),
						"Business Event", "SMAB-T3637: Verify that AT for appraisal WI is of type buisness event ");
				
				String WorkItemForReviewCioSale = salesforceAPI.select(
						"SELECT NAME FROM WORK_ITEM__C WHERE TYPE__C='Normal Appraisal' AND SUB_TYPE__C='Review - CIO Sale' ORDER BY CREATEDDATE DESC")
						.get("Name").get(0);

				objAppraisalActivity.logout();
				Thread.sleep(3000);

				objCIOTransferPage.login(CIO_STAFF);
				Thread.sleep(4000);
				objAppraisalActivity.waitForElementToBeClickable(objApasGenericPage.appLauncher, 10);
				
				// Step 9 - Navigating to AT for newely create Review CIO -SALE WI
				
				driver.navigate().to("https://smcacre--" + excEnv
						+ ".lightning.force.com/lightning/r/Transaction_Trail__c/"
						+ salesforceAPI.select(
								"SELECT Business_Event__c,ID from work_item_linkage__c where work_item__r.name='"
										+ WorkItemForReviewCioSale + "'")
								.get("Business_Event__c").get(0)
						+ "/view");
				
				// STEP 10- Validating the status of AT for REVIEW CIO-SALE WI after rejection
				objCIOTransferPage.waitForElementToBeVisible(10, objAuditTrail.statusLabel);
				
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.statusLabel), "Open",
						"SMAB-T3637: Verify the status of AT is open");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.eventLibraryLabel),
						EventCode, "SMAB-T3637:Verify that Event Library remains same as that of Appraisal WI before rejection");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.dorLabel), DOR,
						"SMAB-T3637:Verifying DOR is remains same as that of Appraisal WI AT");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.dovLabel), DOV,
						"SMAB-T3637:Verifying DOV is remains same as that of Appraisal WI AT");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.requestOriginLabel),
						"Internal Request", "SMAB-T3637:Verify that request origin is Internal request");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.rollYearLabel),
						rollYear, "SMAB-T3637:Verify roll year remains same as that of Appraisla WI AT");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.recordTypeLabel),
						"Business Event", "SMAB-T3637:Verify that AT record is of type Buisness Event");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.relatedCorrespondenceLabel),
						parentTransactionTrailForAppraiser, "SMAB-T3637:Verify that Parent of Review CIO -SALE WI AT remains same as that of appraisal WI");

				objCIOTransferPage.globalSearchRecords(WorkItemForReviewCioSale);
				objCIOTransferPage.waitForElementToBeClickable(objWorkItemHomePage.inProgressOptionInTimeline, 10);
				objCIOTransferPage.Click(objWorkItemHomePage.detailsTab);
				
				// STEP 11- Validating the status of WI for REVIEW CIO-SALE WI after rejection
				
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus),
						"In Pool", "SMAB-T3637: Verify that status of WI is in-pool");
				softAssert.assertEquals(
						objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.wiTypeDetailsPage),
						"Normal Appraisal", "SMAB-T3637:Verify that type of WI is Normal Appraisal ");
				softAssert.assertEquals(
						objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.wiActionDetailsPage),
						"Review - CIO Sale", "SMAB-T3637:Verify that action of WI is Review - CIO Sale ");
				softAssert.assertEquals(
						objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.wiWorkPoolDetailsPage), "CIO",
						"SMAB-T3637: Verify Workpool of WI is CIO");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.apnLabel),
						apnLabel, "SMAB-T3637:Verify APN of WI remains same as that of Appraisal WIs");
				softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Request Type"),objAppraisalActivity.getFieldValueFromAPAS("Type")+" "+"-"+" "+objAppraisalActivity.getFieldValueFromAPAS("Action")+" "+"-"+" "+objAppraisalActivity.getFieldValueFromAPAS("Reference") ,
						"SMAB-T3762: Verify that request type  of the WI is cancatination of Type ,Action and reference");
				

				objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
				objCIOTransferPage.waitForElementToBeVisible(objWorkItemHomePage.relatedActionLink, 10);
				objCIOTransferPage.Click(objWorkItemHomePage.relatedActionLink);
				String parentWindowForReviewWI = driver.getWindowHandle();
				objWorkItemHomePage.switchToNewWindow(parentWindowForReviewWI);
				objCIOTransferPage.waitForElementToBeVisible((objCIOTransferPage.quickActionButtonDropdownIcon), 10);
				
				//STEP 12- Verify the status of CIO transfer activity from RAT screen
				
				softAssert.assertEquals(
						objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferStatusLabel), "Reopened",
						"SMAB-T3637:Verify that status of CIO transfer activity is reopened");

				String queryRecordedAPNTransfer = "SELECT Navigation_Url__c FROM Work_Item__c where name='"
						+ WorkItemForReviewCioSale + "'";
				HashMap<String, ArrayList<String>> navigationUrL = salesforceAPI.select(queryRecordedAPNTransfer);

				// STEP 13-Finding the recorded apn transfer id

				String recordeAPNTransferID = navigationUrL.get("Navigation_Url__c").get(0);

				driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/"
						+ recordeAPNTransferID + "/related/CIO_Transfer_Grantee_New_Ownership__r/view");
				
				//STEP 14-Updating the grantee records and marking it as rolled back active status
				
				objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.newButton, 10);
				objCIOTransferPage.Click(objCIOTransferPage.locateElement("//a[@data-refid='recordId']", 10));
				objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.Edit);
				objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.Edit));
				objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.ownerPercentage);
				objCIOTransferPage.selectOptionFromDropDown(objCIOTransferPage.Status, "Rolled Back - Active");
				objCIOTransferPage.selectOptionFromDropDown(objCIOTransferPage.originalTransferor, "Yes");
				objCIOTransferPage.selectOptionFromDropDown(objCIOTransferPage.vestingType, "JT");
				objCIOTransferPage.enter(objCIOTransferPage.remarksLabel,
						"New Active record  Record after Rolled back Active");
				objCIOTransferPage.Click(objCIOTransferPage.saveButtonModalWindow);
				
        
				//STEP 15 -Resubmitting for approval after edits on grantee records
				
				driver.navigate().to("https://smcacre--" + excEnv
						+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon, 10);
			objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null,
					objCIOTransferPage.quickActionOptionSubmitForApproval);
			objCIOTransferPage.waitForElementToBeClickable(
					objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton), 10);
			objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
			driver.navigate().refresh();
			objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon, 10);
			
			//STEP 16 -Validating the status of CIO transfer after submit for approval
			
			softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferStatusLabel),
					"Resubmitted for Approval",
					"SMAB-T3736: Verify that transfer status is changed to resubmitted for approval after staff roll backs and clicks on submit for approval");
			 

			driver.navigate()
			.to("https://smcacre--"
					+ excEnv + ".lightning.force.com/lightning/r/Parcel__c/" + salesforceAPI
							.select("Select Id from parcel__C where name='" + apnLabel + "'").get("Id").get(0)
					+ "/related/Property_Ownerships__r/view");
    		objCIOTransferPage.waitForElementToBeClickable(10, objCIOTransferPage.getButtonWithText(objCIOTransferPage.newButton));
    		
    		//STEP 17 -Validating that new record is generated whenever new changed are done in grantee records and it roll backs previous grantee record
    		
    		softAssert.assertTrue(objCIOTransferPage.getGridDataInHashMap().get("Remarks").contains("New Active record Record after Rolled back Active") ,"SMAB-T3736: Verified that new record is generated when remarks and others fields were updated and rolled back the existing one");
            
            objCIOTransferPage.logout();
            Thread.sleep(3000);
            
            objCIOTransferPage.login(CIO_SUPERVISOR);
            
            Thread.sleep(4000);
            objCIOTransferPage.globalSearchRecords(WorkItemForReviewCioSale);
           	
            driver.navigate().to("https://smcacre--" + excEnv
					+ ".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/" + recordeAPNTransferID + "/view");
            
            //STEP -18  Approving the transfer by supervisor
            
            objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon,10);
            objCIOTransferPage.clickQuickActionButtonOnTransferActivity(null, objCIOTransferPage.quickActionOptionApprove);
            objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton),10);
            objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
            driver.navigate().refresh();
            objCIOTransferPage.waitForElementToBeClickable(objCIOTransferPage.quickActionButtonDropdownIcon,10);
            
           //STEP -18(a)  Verify the status of transfer after reapproval
            
			softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferStatusLabel),
					"ReApproved",
					"SMAB-T3736: Verify that transfer status is changed to reapproved after supervisor clicks on approve quick action");
			
			driver.navigate().to("https://smcacre--" + excEnv
					+ ".lightning.force.com/lightning/r/Transaction_Trail__c/"
					+ salesforceAPI.select(
							"SELECT Business_Event__c,ID from work_item_linkage__c where work_item__r.name='"
									+ WorkItemForReviewCioSale + "'")
							.get("Business_Event__c").get(0)
					+ "/view");
			
			//Step 19 -Validating the status of AT after reapproval
			
			objCIOTransferPage.waitForElementToBeVisible(10,objAuditTrail.statusLabel);
			
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.statusLabel), "Completed",
					"SMAB-T3749:Verifying that Status of AT is closed related to Review CIO-Sale");
			
			objCIOTransferPage.globalSearchRecords(WorkItemForReviewCioSale);
			objCIOTransferPage.waitForElementToBeClickable(objWorkItemHomePage.inProgressOptionInTimeline, 10);
			objCIOTransferPage.Click(objWorkItemHomePage.detailsTab);
			objCIOTransferPage.waitForElementToBeVisible(10,objWorkItemHomePage.wiStatus );
			
			//Step 20 -Validating the status of WI after reapproval
			
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus),
					"Completed", "SMAB-T3749:Verifying that WI status is completed after re-approval by CIO supervisor");
			
			String WorkItemForAppraislAfterReapproval =salesforceAPI.select(
					"Select Id ,Name from Work_Item__c where type__c='Appraiser' and sub_type__c='Appraisal Activity' order by createdDate desc")
					.get("Name").get(0);
			
			objCIOTransferPage.logout();
			Thread.sleep(3000);
			
			objAppraisalActivity.login(APPRAISAL_SUPPORT);
			Thread.sleep(4000);
			
			driver.navigate().to("https://smcacre--" + excEnv
					+ ".lightning.force.com/lightning/r/Transaction_Trail__c/"
					+ salesforceAPI.select(
							"SELECT Business_Event__c,ID from work_item_linkage__c where work_item__r.name='"
									+ WorkItemForAppraislAfterReapproval + "'")
							.get("Business_Event__c").get(0)
					+ "/view");
			
			//Step 21 -Validating the status of AT for new appraisal WI
			
			objCIOTransferPage.waitForElementToBeVisible(10,objAuditTrail.statusLabel);
			
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.statusLabel), "Open",
					"SMAB-T3637:Verifying that status of the AT is Open");

			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.eventLibraryLabel),
					objCIOTransferPage.CIO_EVENT_CODE_PART, "SMAB-T3749:Verifying that EventLibrary of the AT is same as that CIO-Review WI");
			

			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.dorLabel), DOR,
					"SMAB-T3749:Verifying that DOR of the AT is same as that CIO-Review WI");
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.dovLabel), DOV,
					"SMAB-T3749: Verifying that DOV of the AT is same as that CIO-Review WI");
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.requestOriginLabel),
					"Internal Request", "SMAB-T3749: Verifying that request origin of AT is Internal request");
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.rollYearLabel),
					rollYear, "SMAB-T3749: Verify that roll year of appraiser WI is same as that of Appraisal WI before rejection provided there is no change in events date");
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.recordTypeLabel),
					"Business Event", "SMAB-T3749: Verify that AT for appraisal activity after reapproval is of type Buisness Event");
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.relatedCorrespondenceLabel),
					parentTransactionTrailForAppraiser, "SMAB-T3749: Verify that new AT for appraisal remains child of CIO Process transfer WI AT=C");
			
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAuditTrail.relatedCorrespondenceLabel),
					parentTransactionTrailForAppraiser, "SMAB-T3749: Verify that new AT for appraisal remains child of CIO Process transfer WI AT=C");
			
			
			objAppraisalActivity.globalSearchRecords(WorkItemForAppraislAfterReapproval);
			objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
			objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			
			//Step 21 -Validating the status of   new appraisal WI
			
			objCIOTransferPage.waitForElementToBeVisible(10,objWorkItemHomePage.wiStatus);
			
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Work Pool"), "Normal Enrollment",
					"SMAB-T3749: Verify that Workpool of the WI is direct enrollment");
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Type"), "Appraiser",
					"SMAB-T3749: Verify that Type of the WI is Appraisal");
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS("Action"), "Appraisal Activity",
					"SMAB-T3749: Verify that Action of the WI is Appraisal Activity");
			
			softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objWorkItemHomePage.apnLabel), apnLabel,
					"SMAB-T3749: Verify that Apn of the WI remains same for all the WI till REVIEW CIO-SALE");

			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindowforReapprovalAppraisalWI = driver.getWindowHandle();
			objWorkItemHomePage.switchToNewWindow(parentWindowforReapprovalAppraisalWI);

			objAppraisalActivity.waitForElementToBeVisible(10, objAppraisalActivity.appraisalActivityStatus);
			
			//Step 21 -Validating the status of   new appraisal Activity
			
			softAssert.assertEquals(
					objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.appraisalActivityStatus),
					"In Progress",
					"SMAB-T3749: Verify that status by default  of the appraisal activity is In Progress ");
			objCIOTransferPage.clickQuickActionButtonOnTransferActivity(objCIOTransferPage.backToWIsButtonLabel, objCIOTransferPage.quickActionOptionBackToWIs);

			objAppraisalActivity.waitForElementToBeClickable(
					objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.changeWorkPool), 10);
			softAssert.assertTrue(driver.getCurrentUrl().contains("/home"),
					"SMAB-T3738:Verify that Back to WI's navigates users back to the HOME page");
			
			objAppraisalActivity.logout();
			Thread.sleep(3000);
			
			objAppraisalActivity.login(users.APPRAISAL_SUPPORT);
			
			Thread.sleep(4000);
			objAppraisalActivity.globalSearchRecords(arrayForWorkItemAfterCIOSupervisorApproval[1]);
			objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.inProgressOptionInTimeline,10);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.wiStatus);
			
			// Step 22 -Validating the details of Questionnaire Correspondence WI created after CIO approval
			
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiWorkPoolDetailsPage),"Normal Enrollment" , "SMAB-T3807: Verify that the WI for questionnaire is routed to Normal Enrollment workpool");
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiTypeDetailsPage),"Appraiser" , "SMAB-T3807: Verify that the WI for questionnaire is of type  Appraiser");
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiActionDetailsPage),"Questionnaire Correspondence" , "SMAB-T3807: Verify that the WI for questionnaire is of subtype Questionnaire Correspondence");
			softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus),"Completed" , "SMAB-T3807: Verify that the WI for questionnaire is in pool status");
	
			objAppraisalActivity.logout();
	}
	/*
	 * This test methodes validates the fields on the layout of assessed value records for CIO transfer codes like CIO-P19D,CIO-P19B,CIO-P19 Exclusion
	 */

	@Test(description = "SMAB-T3768,SMAB-T3782,SMAB-T3812 : Verify the fileds and calculations on those fields on the layout of different type of assesssed value records  ", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression", "NormalEnrollment", "ChangeInOwnershipManagement" }, enabled = true)
	public void OwnershipAndTransfer_Verify_assessedValueRecords(String loginUser) throws Exception {
		
		String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
		Map<String, String> hashMapCreateAssessedValueRecordForP19D = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecordForP19D");

		Map<String, String> hashMapCreateAssessedValueRecordForP19B = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecordForP19B");
		Map<String, String> hashMapCreateAssessedValueRecordForP19E = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecordForP19E");

		objAppraisalActivity.login(users.SYSTEM_ADMIN);
		Thread.sleep(3000);

		objParcelsPage.closeDefaultOpenTabs();

		// Step1 -Creating a new acessed value record of type P19D

		objParcelsPage.deleteOldAndCreateNewAssessedValuesRecords(hashMapCreateAssessedValueRecordForP19D,
				salesforceAPI.select("Select Name from Parcel__c where status__c='Active' limit 1").get("Name").get(0));
		objParcelsPage.waitForElementToBeClickable(10, objParcelsPage.EditButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.EditButton));

		objParcelsPage.enter(objParcelsPage.landFactoredBaseYearValue, "200,000");
		objParcelsPage.waitForElementToBeVisible(10, objParcelsPage.improvementsFactoredBaseYearValue);
		objParcelsPage.enter(objParcelsPage.improvementsFactoredBaseYearValue, "100,000");
		objParcelsPage.waitForElementToBeClickable(10, objParcelsPage.SaveButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.SaveButton));

		// Scrolling to the middle of page

		objParcelsPage.waitForElementToBeVisible(10, objParcelsPage.difference);
		javascriptexecutor.executeScript("window.scrollBy(0,800)");

		// Step 2- Fetching field values from the layout

		String difference = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.difference);

		String land = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.land);
		String improvement = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.improvement);
		String total = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.total);
		String fullcashValue = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.fullCashValue);

		String differenceApportionedToLand = objParcelsPage
				.getFieldValueFromAPAS(objParcelsPage.differenceApportionedToLand);
		String differenceApportionedToImprovement = objParcelsPage
				.getFieldValueFromAPAS(objParcelsPage.differenceApportionedToImprovement);
		String factoredBaseYearValue = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.factoredBaseYearValue);

		// Step 3-Asserting that field values on the layout

		softAssert.assertEquals(difference.replace(",", ""),
				String.valueOf(Integer.parseInt(fullcashValue.replace(",", "")) - Integer
						.parseInt(hashMapCreateAssessedValueRecordForP19D.get("Sales Price").replace(",", ""))),
				"SMAB-T3768:Verifying that difference is subtraction of Full cash value -Sales Price for P19D");
		softAssert.assertEquals(factoredBaseYearValue, "300,000.00",
				"SMAB-T3768:Verifying that Factored Base Year value  is addition of Land Factored base Year value and Improvement Factored base year value");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.hpiValueAllowance),
				"SMAB-T3768:Verifying that HPI value allowance is not visible on P19D Layout");
		softAssert.assertEquals(land.replace(",", ""),
				String.valueOf(
						Integer.parseInt(differenceApportionedToLand.replace(",", "")) + Integer.parseInt("200000")),
				"SMAB-T3768:Verify that Land is sum of LandFactored base year value + Difference apportioned to land");
		softAssert.assertEquals(improvement.replace(",", ""),
				String.valueOf(Integer.parseInt(differenceApportionedToImprovement.replace(",", ""))
						+ Integer.parseInt("100000")),
				"SMAB-T3768:Verify that Improvement is sum of ImprovementFactored base year value + Difference apportioned to Improvement");
		softAssert.assertEquals(Integer.parseInt(total.replace(",", "")),
				Math.round(
						Double.parseDouble(land.replace(",", "")) + Double.parseDouble(improvement.replace(",", ""))),
				"SMAB-T3768:Verify that total is sum of Land and Improvement");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.combinedFactoredandHPI),
				"SMAB-T3768:Verifying that combined Factored and HPI value allowance is not visible on P19D Layout");

		// Creating another assessed value for P19B

		objParcelsPage.deleteOldAndCreateNewAssessedValuesRecords(hashMapCreateAssessedValueRecordForP19B,
				salesforceAPI.select("Select Name from Parcel__c where status__c='Active' limit 1").get("Name").get(0));

		objParcelsPage.waitForElementToBeClickable(10, objParcelsPage.EditButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.EditButton));
		objParcelsPage.enter(objParcelsPage.landFactoredBaseYearValue, "200,000");
		objParcelsPage.waitForElementToBeVisible(10, objParcelsPage.improvementsFactoredBaseYearValue);
		objParcelsPage.enter(objParcelsPage.improvementsFactoredBaseYearValue, "100,000");
		objParcelsPage.waitForElementToBeClickable(10, objParcelsPage.SaveButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.SaveButton));

		// fetching field values from layout

		objParcelsPage.waitForElementToBeVisible(10, objParcelsPage.difference);
		javascriptexecutor.executeScript("window.scrollBy(0,1000)");
		difference = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.difference);

		land = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.land);
		improvement = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.improvement);
		total = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.total);
		fullcashValue = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.fullCashValue);

		differenceApportionedToLand = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.differenceApportionedToLand);
		differenceApportionedToImprovement = objParcelsPage
				.getFieldValueFromAPAS(objParcelsPage.differenceApportionedToImprovement);
		factoredBaseYearValue = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.factoredBaseYearValue);

		objParcelsPage.waitForElementToBeVisible(10, objParcelsPage.difference);

		// Asserting that field values on the layout

		softAssert.assertEquals(difference.replace(",", ""),
				String.valueOf(Integer.parseInt(fullcashValue.replace(",", "")) - Integer
						.parseInt(hashMapCreateAssessedValueRecordForP19D.get("Sales Price").replace(",", ""))),
				"SMAB-T3782:Verifying that difference is subtraction of Full cash value -Sales Price for P19B");
		softAssert.assertEquals(factoredBaseYearValue, "300,000.00",
				"SMAB-T3782:Verifying that Factored Base Year value  is addition of Land Factored base Year value and Improvement Factored base year value");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.hpiValueAllowance),
				"SMAB-T3782:Verifying that HPI value allowance is not visible on P19B Layout");
		softAssert.assertEquals(land.replace(",", ""),
				String.valueOf(
						Integer.parseInt(differenceApportionedToLand.replace(",", "")) + Integer.parseInt("200000")),
				"SMAB-T3782:Verify that Land is sum of LandFactored base year value + Difference apportioned to land");
		softAssert.assertEquals(improvement.replace(",", ""),
				String.valueOf(Integer.parseInt(differenceApportionedToImprovement.replace(",", ""))
						+ Integer.parseInt("100000")),
				"SMAB-T3782:Verify that Improvement is sum of ImprovementFactored base year value + Difference apportioned to Improvement");
		softAssert.assertEquals(Integer.parseInt(total.replace(",", "")),
				Math.round(
						Double.parseDouble(land.replace(",", "")) + Double.parseDouble(improvement.replace(",", ""))),
				"SMAB-T3782:Verify that total is sum of Land and Improvement");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.combinedFactoredandHPI),
				"SMAB-T3782:Verifying that combined Factored and HPI value allowance is not visible on P19B Layout");

		// Creating another assessed value for P19E

		objParcelsPage.deleteOldAndCreateNewAssessedValuesRecords(hashMapCreateAssessedValueRecordForP19E,
				salesforceAPI.select("Select Name from Parcel__c where status__c='Active' limit 1").get("Name").get(0));

		objParcelsPage.waitForElementToBeClickable(10, objParcelsPage.EditButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.EditButton));
		objParcelsPage.enter(objParcelsPage.landFactoredBaseYearValue, "200,000");
		objParcelsPage.waitForElementToBeVisible(10, objParcelsPage.improvementsFactoredBaseYearValue);
		objParcelsPage.enter(objParcelsPage.improvementsFactoredBaseYearValue, "100,000");
		objParcelsPage.waitForElementToBeClickable(10, objParcelsPage.SaveButton);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.SaveButton));

		// fetching field values from layout

		objParcelsPage.waitForElementToBeVisible(10, objParcelsPage.difference);
		javascriptexecutor.executeScript("window.scrollBy(0,1000)");
		difference = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.difference);

		land = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.land);
		improvement = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.improvement);
		total = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.total);
		fullcashValue = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.fullCashValue);

		differenceApportionedToLand = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.differenceApportionedToLand);
		differenceApportionedToImprovement = objParcelsPage
				.getFieldValueFromAPAS(objParcelsPage.differenceApportionedToImprovement);
		factoredBaseYearValue = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.factoredBaseYearValue);
		String hpiValueAllowance = objParcelsPage.getFieldValueFromAPAS(objParcelsPage.hpiValueAllowance);

		// Asserting that field values on the layout

		softAssert.assertEquals(difference.replace(",", ""),
				String.valueOf(Integer.parseInt(fullcashValue.replace(",", ""))
						- (int) Double.parseDouble(factoredBaseYearValue.replace(",", ""))
						- Integer.parseInt(hpiValueAllowance.replace(",", ""))),
				"SMAB-T3812:Verifying that difference is subtraction of Full cash value -Factored BYV -HPI value allowance for P19E");
		softAssert.assertEquals(factoredBaseYearValue, "300,000.00",
				"SMAB-T3812:Verifying that Factored Base Year value  is addition of Land Factored base Year value and Improvement Factored base year value");
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objParcelsPage.hpiValueAllowance),
				"SMAB-T3812:Verifying that HPI value allowance is visible on P19E Layout");
		softAssert.assertEquals(hpiValueAllowance,"1,000,000",
				"SMAB-T3812:Verifying that HPI value allowance is defaulted to 1,000,000 on P19E Layout");
		softAssert.assertEquals(land.replace(",", ""),
				String.valueOf(
						Integer.parseInt(differenceApportionedToLand.replace(",", "")) + Integer.parseInt("200000")),
				"SMAB-T3812:Verify that Land is sum of LandFactored base year value + Difference apportioned to land");
		softAssert.assertEquals(improvement.replace(",", ""),
				String.valueOf(Integer.parseInt(differenceApportionedToImprovement.replace(",", ""))
						+ Integer.parseInt("100000")),
				"SMAB-T3812:Verify that Improvement is sum of ImprovementFactored base year value + Difference apportioned to Improvement");
		softAssert.assertEquals(Integer.parseInt(total.replace(",", "")),
				Math.round(
						Double.parseDouble(land.replace(",", "")) + Double.parseDouble(improvement.replace(",", ""))),
				"SMAB-T3812:Verify that total is sum of Land and Improvement");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.combinedFactoredandHPI),
				"SMAB-T3812:Verifying that combined Factored and HPI value allowance is not visible on P19E Layout");

		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.salesPrice),
				"SMAB-T3812:Verifying that sales price field value  is not visible on P19E Layout");
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.purchasePrice),
				"SMAB-T3812:Verifying that purchase price field value  is not visible on P19E Layout");

		objAppraisalActivity.logout();

	}
	/*
	 * @param :Transfer codes
	 * @Description: Verify AV record creation after appraisal for different transfer codes along with read and write access on various tables on AAS screen
	 */
	
	@Test(description = "SMAB-T4120,SMAB-T4121 : Verify that new AV records are generated for the given TransferCode when appraiser user appraises land and Improvement values on the appraiser screen", dataProvider = "dpForAppraisalActivityAVCreation", dataProviderClass = DataProviders.class, groups = {
			"Regression", "NormalEnrollment", "ChangeInOwnershipManagement" }, enabled = true)
	public void OwnershipAndTransfer_RelatedTablesInAppraiserActivityScreen(String eventCode) throws Exception {

		String excEnv = System.getProperty("region");		
		
		
		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "dataToCreateGranteeWithCompleteOwnership");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
		Map<String, String> hashMapCreateAssessedValueRecord = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecord");

		// STEP 1- Create appraisal WI through CIO Transfer WI approval

		
		String[] arrayForWorkItemAfterCIOSupervisorApproval = objCIOTransferPage
				.createAppraisalActivityWorkItemForRecordedCIOTransfer("Normal Enrollment",
						eventCode, hashMapOwnershipAndTransferCreationData,
						hashMapOwnershipAndTransferGranteeCreationData, hashMapCreateOwnershipRecordData,
						hashMapCreateAssessedValueRecord);

		// Step 2- LOGIN with appraiser staff

		objAppraisalActivity.login(APPRAISAL_SUPPORT);
		Thread.sleep(4000);
		String workItemForAppraiser = arrayForWorkItemAfterCIOSupervisorApproval[0];
		String query = "Select Id from Work_Item__c where Name = '"+workItemForAppraiser+"'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		driver.navigate().to("https://smcacre--"+excEnv+
		".lightning.force.com/lightning/r/Work_Item__c/"+response.get("Id").get(0)+"/view");
		objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// STEP 3 -Updating Land and Improvement Values and saving it for appraisal	
		objAppraisalActivity.waitForElementToBeVisible(10, objAppraisalActivity.appraisalActivityStatus);
		String apnNoForAppraisal = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.apnLabel);
		objAppraisalActivity
				.Click(objAppraisalActivity.appraisalActivityEditValueButton(objAppraisalActivity.landCashValueLabel));
		objAppraisalActivity.enter(objAppraisalActivity.landCashValueLabel, "200,000");
		objAppraisalActivity.enter(objAppraisalActivity.improvementCashValueLabel, "200,000");
		objAppraisalActivity.Click(objAppraisalActivity.getButtonWithText(objAppraisalActivity.SaveButton));		
		driver.navigate()
				.to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Parcel__c/"
						+ salesforceAPI.select("Select Id from Parcel__c where name ='"
								+ objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.apnLabel) + "'")
								.get("Id").get(0)
						+ "/related/Assessed_Values__r/view");		
		objAppraisalActivity.waitForElementToBeClickable(objAppraisalActivity.clickShowMoreActionButton, 15);
		HashMap<String, ArrayList<String>> hashMapForAssessedValueTable = objAppraisalActivity.getGridDataInHashMap();

		//Step 4-Verifying the New AV and AVO record after updating Land and Improvement Values 		
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Status").get(0), "Retired",
				"SMAB-T4121:Verify that earlier active AV record is getting retired  for 100% transfer");
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Status").get(1), "Active",
				"SMAB-T4121:Verify that new  active AV record is getting created  for 100% transfer");		
		if (eventCode.equals(CIOTransferPage.CIO_EVENT_DISABLED_OWNER_TRANSFER)) {
			softAssert.assertEquals(hashMapForAssessedValueTable.get("Assessed Value Type").get(1),
					"Prop 19 Disabled Owner Transfer",
					"SMAB-T4121:Verify that new  active AV record is getting created  for 100% transfer has assessed value type of Disabled owner transfer .");
		} else if (eventCode.equals(CIOTransferPage.CIO_EVENT_EXCLUSION)) {
			softAssert.assertEquals(hashMapForAssessedValueTable.get("Assessed Value Type").get(1),
					"Prop 19 Intergenerational Exclusion",
					"SMAB-T4121:Verify that new  active AV record is getting created  for 100% transfer has assessed value type of Prop 19 Intergenerational Exclusion .");
		} else {
			softAssert.assertEquals(hashMapForAssessedValueTable.get("Assessed Value Type").get(1),
					"Prop 19 Intergenerational 100% Assessment",
					"SMAB-T4121:Verify that new  active AV record is getting created  for 100% transfer has assessed value type of Prop 19 Intergenerational 100% Assessment .");

		}
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Effective End Date").get(0),
				hashMapForAssessedValueTable.get("Effective Start Date").get(1),
				"SMAB-T4121:Verify that old active AV record is getting retired with end date equals start date of new AV record   for 100% transfer");

		driver.navigate().to("https://smcacre--" + excEnv
				+ ".lightning.force.com/lightning/r/Assessed_Values_Ownership__c/"
				+ salesforceAPI.select("SELECT id  FROM Assessed_Values_Ownership__c where assessed_values__r.name = '"
						+ hashMapForAssessedValueTable.get("Assessed Values ID").get(0) + "'").get("Id").get(0)
				+ "/view");
		objAppraisalActivity.waitForElementToBeVisible(10, objParcelsPage.propertyOwner);

		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.statusLabel), "Retired",
				"SMAB-T4121:Verify that AVO of retired AV after appraisal gets retired");

		driver.navigate().to("https://smcacre--" + excEnv
				+ ".lightning.force.com/lightning/r/Assessed_Values_Ownership__c/"
				+ salesforceAPI.select("SELECT id  FROM Assessed_Values_Ownership__c where assessed_values__r.name = '"
						+ hashMapForAssessedValueTable.get("Assessed Values ID").get(1) + "'").get("Id").get(0)
				+ "/view");
		objAppraisalActivity.waitForElementToBeVisible(10, objParcelsPage.propertyOwner);

		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.statusLabel), "Active",
				"SMAB-T4121:Verify that AVO of new active  AV after appraisal is created .");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.dovLabel),
				hashMapForAssessedValueTable.get("Effective Start Date").get(1),
				"SMAB-T4121:Verify that for new  AVO  DOV is same as DOV/Startdate of the newely created AV record");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.startDateLabel),
				hashMapForAssessedValueTable.get("Effective Start Date").get(1),
				"SMAB-T4121:Verify that AVO of new active  AV after appraisal is created with start date as same as its associated AV record .");
		
		//Step 5 -Verifying the read and edit acess on appraiser staff for ownership AV and roll entry tables		
		driver.navigate().to(
				"https://smcacre--"+excEnv+".lightning.force.com/lightning/r/Parcel__c/"+salesforceAPI.select("Select Id from Parcel__c where name ='"+apnNoForAppraisal+"'").get("Id").get(0)+"/related/Property_Ownerships__r/view");
		objAppraisalActivity.waitForElementToBeClickable(15,objAppraisalActivity.clickShowMoreActionButton );		
		objAppraisalActivity.Click(objAppraisalActivity.clickShowMoreActionButton);
		
		softAssert.assertTrue(!objAppraisalActivity.verifyElementVisible(objAppraisalActivity.EditButton),
				"SMAB-4120:Verify that edit quick access is not visible on ownership table for the appraiser staff");
		
		driver.navigate().to(
				"https://smcacre--"+excEnv+".lightning.force.com/lightning/r/Parcel__c/"+salesforceAPI.select("Select Id from Parcel__c where name ='"+apnNoForAppraisal+"'").get("Id").get(0)+"/related/Assessed_Values__r/view");
		objAppraisalActivity.waitForElementToBeClickable(15,objAppraisalActivity.clickShowMoreActionButton );		;
		objAppraisalActivity.Click(objAppraisalActivity.clickShowMoreActionButton);
		
		softAssert.assertTrue(objAppraisalActivity.verifyElementVisible(objAppraisalActivity.EditButton),
				"SMAB-4120:Verify that edit quick access is  visible on AV table for the appraiser staff");

		driver.navigate().to(
				"https://smcacre--"+excEnv+".lightning.force.com/lightning/r/Parcel__c/"+salesforceAPI.select("Select Id from Parcel__c where name ='"+apnNoForAppraisal+"'").get("Id").get(0)+"/related/Roll_Entry__r/view");
		objAppraisalActivity.waitForElementToBeClickable(15,objAppraisalActivity.clickShowMoreActionButton );		;
		objAppraisalActivity.Click(objAppraisalActivity.clickShowMoreActionButton);
		
		softAssert.assertTrue(!objAppraisalActivity.verifyElementVisible(objAppraisalActivity.EditButton),
				"SMAB-4120:Verify that edit quick access is not visible on roll entry table for the appraiser staff");
	
	
	    objAppraisalActivity.logout();
		
	}	  
	/*
	 * @Description : Verify that new AV records are created for Prop 19 Intergenerational partial transfer and validating the values that it carries.
	 */
	
	@Test(description = "SMAB-T4120,SMAB-T4121 : Verify that new AV records are generated for the P19P when appraiser user appraises land and Improvement values on the appraiser screen",  groups = {
			"Regression", "NormalEnrollment", "ChangeInOwnershipManagement" }, enabled = true)
	public void OwnershipAndTransfer_IntergenerationalTransfer() throws Exception {

		String excEnv = System.getProperty("region");

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData,
				"DataToCreateGranteeWithIncompleteDataForIntergenerationalPartialTransfer");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecord");

		String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
		Map<String, String> hashMapCreateAssessedValueRecord = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecord");

		// Step 1- Creating appraiser WI for P19P transfer event		
		String[] arrayForWorkItemAfterCIOSupervisorApproval = objCIOTransferPage
				.createAppraisalActivityWorkItemForRecordedCIOTransfer("Normal Enrollment",
						CIOTransferPage.CIO_EVENT_INTERGENERATIONAL_TRANSFER, hashMapOwnershipAndTransferCreationData,
						hashMapOwnershipAndTransferGranteeCreationData, hashMapCreateOwnershipRecordData,
						hashMapCreateAssessedValueRecord);
		
		// Step 2- LOGIN with appraiser staff
		objAppraisalActivity.login(APPRAISAL_SUPPORT);
		Thread.sleep(4000);
		String workItemForAppraiser = arrayForWorkItemAfterCIOSupervisorApproval[0];
		String query = "Select Id from Work_Item__c where Name = '"+workItemForAppraiser+"'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		driver.navigate().to("https://smcacre--"+excEnv+
		".lightning.force.com/lightning/r/Work_Item__c/"+response.get("Id").get(0)+"/view");
		objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		// STEP 3 -Updating Land and Improvement Values and saving it for appraisal
		objAppraisalActivity.waitForElementToBeVisible(10, objAppraisalActivity.appraisalActivityStatus);
		String apnNoForAppraisal = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.apnLabel);
		objAppraisalActivity
				.Click(objAppraisalActivity.appraisalActivityEditValueButton(objAppraisalActivity.landCashValueLabel));
		objAppraisalActivity.enter(objAppraisalActivity.landCashValueLabel, "5000,000");
		objAppraisalActivity.enter(objAppraisalActivity.improvementCashValueLabel, "2000,000");
		objAppraisalActivity.Click(objAppraisalActivity.getButtonWithText(objAppraisalActivity.SaveButton));
		driver.navigate()
				.to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Parcel__c/"
						+ salesforceAPI.select("Select Id from Parcel__c where name ='"
								+ objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.apnLabel) + "'")
								.get("Id").get(0)
						+ "/related/Assessed_Values__r/view");
		objAppraisalActivity.waitForElementToBeClickable(objAppraisalActivity.clickShowMoreActionButton, 15);
		HashMap<String, ArrayList<String>> hashMapForAssessedValueTable = objAppraisalActivity.getGridDataInHashMap();

		//Step 4 -Verifying the AV generated  after updating land and improvement value on appraiser screen		
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Status").get(0), "Active",
				"SMAB-T4010:Verify that earlier active AV record is not getting retired  for Intergenerational partial Transfer");
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Status").get(1), "Active",
				"SMAB-T4010:Verify that new active AV record is getting created  for Intergenerational partial Transfer");
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Assessed Value Type").get(1),
				"Prop 19 Intergenerational Partial",
				"SMAB-T4010:Verify that new  active AV record is getting created  for Intergeneational transfer has assessed value type of Prop 19 Intergenerational Partial .");
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Land Value").get(1), "2,557,143",
				"SMAB-T4010:Verify that Land taxable value is getting reflected at grid");
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Improvement Value").get(1), "1,002,857",
				"SMAB-T4010:Verify that Improvement taxable value is getting reflected at grid");

		//Step 5 - Navigating to AV record		
		driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Assessed_BY_Values__c/"
				+ salesforceAPI.select("SELECT id  FROM Assessed_BY_Values__c where name = '"
						+ hashMapForAssessedValueTable.get("Assessed Values ID").get(1) + "'").get("Id").get(0)
				+ "/view");
		objAppraisalActivity.waitForElementToBeVisible(objAppraisalActivity.editButton, 10);
		javascriptexecutor.executeScript("window.scrollBy(0,800)");
		
		//Step 6 -Verifying values on AV record		
		softAssert.assertEquals( objAppraisalActivity.getFieldValueFromAPAS( objParcelsPage.landCashValue), "5,000,000",
				"SMAB-T4010:Verify that Land cash value is populated from Appraiser activity screen");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.improvementCashValue), "2,000,000",
				"SMAB-T4010:Verify that Improvementcash value is populated from Appraiser activity screen");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.landFactoredBaseYearValue), "2,550,000.00",
				"SMAB-T4010:Verify that Land Factored Value is sum of ownership perctange of grantor *Land factored forwarded base year value +ownership percentage of grantee *Land Cash Value");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.improvementsFactoredBaseYearValue), "1,010,000.00",
				"SMAB-T4010:Verify that Improvement Factored Value is sum of ownership perctange of grantor *Improvement factored forwarded base year value +ownership percentage of grantee *Improvement Cash Value");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.factoredBaseYearValue), "3,560,000.00",
				"SMAB-T4010:Verify that Land Factored Value is sum of ownership perctange of grantor * factored forwarded base year value +ownership percentage of grantee *Land Cash Value");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.difference), "5,880,000",
				"SMAB-T4010:Verify that difference Value equals full cash value -factored BYV -HPI");

		//Step 7 -Navigating to AVO record		
		driver.navigate().to("https://smcacre--" + excEnv
				+ ".lightning.force.com/lightning/r/Assessed_Values_Ownership__c/"
				+ salesforceAPI.select("SELECT id  FROM Assessed_Values_Ownership__c where assessed_values__r.name = '"
						+ hashMapForAssessedValueTable.get("Assessed Values ID").get(1) + "'").get("Id").get(0)
				+ "/view");
		objAppraisalActivity.waitForElementToBeVisible(10, objParcelsPage.propertyOwner);

		//Step 8 -Verifying fields on AVO records		
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.statusLabel), "Active",
				"SMAB-T4010:Verify that AVO of new active  AV after appraisal is created and is active.");
		softAssert.assertEquals(
				objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.ownershipPercentageTextBoxForAVO), "50.0000%",
				"SMAB-T4010:Verify that AVO of new active  AV after appraisal is created with ownership percentage 50%.");

		HashMap<String, ArrayList<String>> hashMapPropertyId = salesforceAPI
				.select("SELECT Name FROM Property_Ownership__c where parcel__c = '" + salesforceAPI
						.select("Select Id from Parcel__c where name ='" + apnNoForAppraisal + "'").get("Id").get(0)
						+ "' order by id");

		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.propertyOwner),
				hashMapPropertyId.get("Name").get(1),
				"SMAB-T4010:Verify that AVO of child  is created after updating land and improvement values ");

		//Step 9- Navigating to Old AV record 		
		driver.navigate().to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Assessed_BY_Values__c/"
				+ salesforceAPI.select("SELECT id  FROM Assessed_BY_Values__c where name = '"
						+ hashMapForAssessedValueTable.get("Assessed Values ID").get(0) + "'").get("Id").get(0)
				+ "/view");
		objAppraisalActivity.waitForElementToBeVisible(objParcelsPage.assessedValueOwnershipTab, 10);
		objAppraisalActivity.Click(objParcelsPage.assessedValueOwnershipTab);
		Thread.sleep(2000);
		HashMap<String, ArrayList<String>> hashMapAVOForOldAv = objAppraisalActivity.getGridDataInHashMap();

		//Step 10 -Verifying AVO's associated to the old AV records		
		softAssert.assertTrue(hashMapAVOForOldAv.get("DOV").size() == 2,
				"SMAB-T4010:Verify two AVO's are present on related list");
		softAssert.assertEquals(hashMapAVOForOldAv.get("End Date").get(0),
				hashMapForAssessedValueTable.get("Effective Start Date").get(1),
				"SMAB-T4010:Verify  AVO is present for 100% owner is retired with end date equals start date of the child ");

		//Step 11 - Navigating TO newly  retained AVO for Father		
		driver.navigate().to("https://smcacre--" + excEnv
				+ ".lightning.force.com/lightning/r/Assessed_Values_Ownership__c/"
				+ salesforceAPI
						.select("SELECT id  FROM Assessed_Values_Ownership__c where name = '" + hashMapAVOForOldAv
								.get("Assessed Values Ownership: Assessed Values Ownership ID").get(1) + "'")
						.get("Id").get(0)
				+ "/view");
		objAppraisalActivity.waitForElementToBeVisible(10, objParcelsPage.propertyOwner);

		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.statusLabel), "Active",
				"SMAB-T4010:Verify that AVO of  father with new retained percentage  AV after appraisal is created and is active.");
		softAssert.assertEquals(
				objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.ownershipPercentageTextBoxForAVO), "50.0000%",
				"SMAB-T4010:Verify that AVO of  father with new retained percentage  AV after appraisal is created with ownership percentage 50%.");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.propertyOwner),
				hashMapPropertyId.get("Name").get(2),
				"SMAB-T4010:Verify that AVO of father with new retained percentage is created after updating land and improvement values ");

		objAppraisalActivity.logout();
	}
	
	
	/*
	 * @Description : Verify that new AV records and enrollement  are created for Prop 19 100% reassessement transfer and validating the values that it carries.
	 */
	
	@Test(description = "SMAB-T4387 : Verify that new AV records are generated for the P19 when appraiser user appraises land and Improvement values on the appraiser screen",  groups = {
			"Regression", "NormalEnrollment", "ChangeInOwnershipManagement" }, enabled = true)
	public void OwnershipAndTransfer_Prop19Reassessement() throws Exception {

		String excEnv = System.getProperty("region");

		String OwnershipAndTransferCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData, "dataToCreateMailToRecordsWithIncompleteData");

		Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(
				OwnershipAndTransferCreationData,
				"dataToCreateGranteeWithCompleteOwnership");

		Map<String, String> hashMapCreateOwnershipRecordData = objUtil
				.generateMapFromJsonFile(OwnershipAndTransferCreationData, "DataToCreateOwnershipRecordForP19");

		String assessedValueCreationData = testdata.ASSESSED_VALUE_CREATION_DATA;
		Map<String, String> hashMapCreateAssessedValueRecord = objUtil
				.generateMapFromJsonFile(assessedValueCreationData, "dataToCreateAssesedValueRecordForP19");

		// Step 1- Creating appraiser WI for P19P transfer event		
		String[] arrayForWorkItemAfterCIOSupervisorApproval = objCIOTransferPage
				.createAppraisalActivityWorkItemForRecordedCIOTransfer("Normal Enrollment",
						CIOTransferPage.CIO_EVENT_REASSESSMENT, hashMapOwnershipAndTransferCreationData,
						hashMapOwnershipAndTransferGranteeCreationData, hashMapCreateOwnershipRecordData,
						hashMapCreateAssessedValueRecord);
		
		// Step 2- LOGIN with appraiser staff
		objAppraisalActivity.login(APPRAISAL_SUPPORT);
		Thread.sleep(4000);
		String workItemForAppraiser = arrayForWorkItemAfterCIOSupervisorApproval[0];
		String query = "Select Id from Work_Item__c where Name = '"+workItemForAppraiser+"'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		driver.navigate().to("https://smcacre--"+excEnv+
		".lightning.force.com/lightning/r/Work_Item__c/"+response.get("Id").get(0)+"/view");
		objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objAppraisalActivity.waitForElementToBeClickable(10, objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel, 10);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objAppraisalActivity.waitForElementToBeVisible(10, objAppraisalActivity.appraisalActivityStatus);
		String apnNoForAppraisal = objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.apnLabel);
		objAppraisalActivity
				.Click(objAppraisalActivity.appraisalActivityEditValueButton(objAppraisalActivity.landCashValueLabel));
		objAppraisalActivity.enter(objAppraisalActivity.landCashValueLabel, "5000,000");
		objAppraisalActivity.enter(objAppraisalActivity.improvementCashValueLabel, "2000,000");
		objAppraisalActivity.Click(objAppraisalActivity.getButtonWithText(objAppraisalActivity.SaveButton));
		driver.navigate()
				.to("https://smcacre--" + excEnv + ".lightning.force.com/lightning/r/Parcel__c/"
						+ salesforceAPI.select("Select Id from Parcel__c where name ='"
								+ apnNoForAppraisal + "'")
								.get("Id").get(0)
						+ "/related/Assessed_Values__r/view");
		objAppraisalActivity.waitForElementToBeClickable(objAppraisalActivity.clickShowMoreActionButton, 15);
		HashMap<String, ArrayList<String>> hashMapForAssessedValueTable = objAppraisalActivity.getGridDataInHashMap();

		//Step 4 -Verifying the AV generated  after updating land and improvement value on appraiser screen		
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Status").get(0), "Retired",
				"SMAB-T4387:Verify that earlier active AV record is  getting retired  for P19 100% Reassessement");
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Status").get(1), "Active",
				"SMAB-T4387:Verify that new active AV record is getting created  for P19 100% Reassessement");
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Assessed Value Type").get(1),
				"Prop 19 Intergenerational 100% Assessment",
				"SMAB-T4387:Verify that new  active AV record is getting created  for Intergeneational transfer has assessed value type ofProp 19 Intergenerational 100% Assessment");
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Land Value").get(1), "5,000,000",
				"SMAB-T4387:Verify that Land taxable value is getting reflected at grid");
		softAssert.assertEquals(hashMapForAssessedValueTable.get("Improvement Value").get(1), "2,000,000",
				"SMAB-T4387:Verify that Improvement taxable value is getting reflected at grid");
		
		//Navigating to the AVO record for new owner 
		driver.navigate().to("https://smcacre--" + excEnv
				+ ".lightning.force.com/lightning/r/Assessed_Values_Ownership__c/"
				+ salesforceAPI.select("SELECT id  FROM Assessed_Values_Ownership__c where assessed_values__r.name = '"
						+ hashMapForAssessedValueTable.get("Assessed Values ID").get(1) + "'").get("Id").get(0)
				+ "/view");
		objAppraisalActivity.waitForElementToBeVisible(10, objParcelsPage.propertyOwner);

		//Step 8 -Verifying fields on AVO records		
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objAppraisalActivity.statusLabel), "Active",
				"SMAB-T4387:Verify that AVO of new active  AV after appraisal is created and is active.");
		softAssert.assertEquals(
				objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.ownershipPercentageTextBoxForAVO), "100.0000%",
				"SMAB-T4387:Verify that AVO of new active  AV after appraisal is created with ownership percentage 100%.");

		HashMap<String, ArrayList<String>> hashMapPropertyId = salesforceAPI
				.select("SELECT Name FROM Property_Ownership__c where parcel__c = '" + salesforceAPI
						.select("Select Id from Parcel__c where name ='" + apnNoForAppraisal + "'").get("Id").get(0)
						+ "' order by id");
		softAssert.assertEquals(objAppraisalActivity.getFieldValueFromAPAS(objParcelsPage.propertyOwner),
				hashMapPropertyId.get("Name").get(1),
				"SMAB-T4387:Verify that AVO of child  is created after updating land and improvement values ");
		
		//Navigating to Roll Entry table
		driver.navigate().to(
				"https://smcacre--"+excEnv+".lightning.force.com/lightning/r/Parcel__c/"+salesforceAPI.select("Select Id from Parcel__c where name ='"+apnNoForAppraisal+"'").get("Id").get(0)+"/related/Roll_Entry__r/view");
		objAppraisalActivity.waitForElementToBeClickable(15,objAppraisalActivity.clickShowMoreActionButton );		
		  HashMap<String, ArrayList<String>>hashMapRollEntryTable =objAppraisalActivity.getGridDataInHashMap();
		  
		  //Validating Enrollement records generated for p19		  
		softAssert.assertEquals(hashMapRollEntryTable.get("Type").size(),2 , "SMAB-T4387: For DOV occuring after May 31 only 1 supplemental record is generated ");
		softAssert.assertEquals(hashMapRollEntryTable.get("Type").get(0) ,"Annual", "SMAB-T4387: Verify that Annual record is genrated after appraisal for year 2002 ");
		softAssert.assertEquals(hashMapRollEntryTable.get("Type").get(1) ,"Supplemental", "SMAB-T4387: Verify that Supplemental  record is genrated after appraisal for year 2001 ");
		softAssert.assertEquals(hashMapRollEntryTable.get("Type").get(1) ,"Supplemental", "SMAB-T4387: Verify that Supplemental  record is genrated after appraisal for year 2001 ");
		softAssert.assertEquals(hashMapRollEntryTable.get("Roll Year - Seq#").get(0) ,"2002 - 1", "SMAB-T4387: Verify that sequence of annual record is 1 for roll year 2002 ");
		softAssert.assertEquals(hashMapRollEntryTable.get("Roll Year - Seq#").get(1) ,"2001 - 2", "SMAB-T4387: Verify that sequence of supplemental record is 1 for roll year 2001 ");
		softAssert.assertTrue(!hashMapRollEntryTable.get("Land Assessed Value").get(0).equals(hashMapRollEntryTable.get("Land Assessed Value").get(1)) &&( hashMapRollEntryTable.get("Improvement Assessed Value").get(0).equals( hashMapRollEntryTable.get("Improvement Assessed Value").get(1))), "SMAB-T4387: Verify that annual enrollement record have factored forwarded values from previous supplemental records ");
		
		
		objAppraisalActivity.logout();
		
		


	}
	
}
