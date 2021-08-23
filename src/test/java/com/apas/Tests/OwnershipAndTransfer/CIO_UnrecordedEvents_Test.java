package com.apas.Tests.OwnershipAndTransfer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.generic.NEW;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

import android.R.string;

public class CIO_UnrecordedEvents_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject= new JSONObject();
	String apnPrefix=new String();
	CIOTransferPage objCIOTransferPage;
	ExemptionsPage objExemptionsPage;
	AuditTrailPage trail;
	String unrecordedEventData;
	String ownershipCreationData;


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objCIOTransferPage = new CIOTransferPage(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		trail= new AuditTrailPage(driver);
		unrecordedEventData = testdata.UNRECORDED_EVENT_DATA;
		ownershipCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
	}
	
	/*
	 * Verify the warning message on CIO Transfer screen when UT is created on Retired Parcel With 99 PUC
	 */
	
	@Test(description = "SMAB-T3287:Verify the warning message on CIO Transfer screen when UT is created on Retired Parcel with 99 PUC", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_WarningMessageForRetiredParcelWith99PUC(String loginUser) throws Exception {
		
		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Retired' limit 1";
		String retiredApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String retiredApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		String activeApn = objCIOTransferPage.fetchActiveAPN();
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT id FROM PUC_Code__c where Name = '99-RETIRED PARCEL' limit 1");
		salesforceAPI.update("Parcel__c", retiredApnId, "PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(retiredApn);
		
		// Step3: Create UT event and validate warning message on CIO Transfer screen
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		softAssert.assertEquals(objCIOTransferPage.getElementText(objCIOTransferPage.transferPageMessageArea),"Please select an active APN before performing any action related to CIO Transfer",
				"SMAB-T3287: Validate the warning message on CIO Transfer screen");
		
		// Step4: Edit the Transfer activity and update the Transfer Code
		ReportLogger.INFO("Add the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.transferCodeLabel);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
				"SMAB-T3287: Validate that CIO staff is able to update and save values on CIO Transfer Screen");
		
		// Step5: Update APN field with active APN and validate Warning message disappears
		ReportLogger.INFO("Update the APN value to an Active Parcel value");
		objParcelsPage.Click(objCIOTransferPage.editFieldButton("APN"));
		objCIOTransferPage.clearSelectionFromLookup("APN");
		
		objCIOTransferPage.searchAndSelectOptionFromDropDown("APN", activeApn);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText("Save"));
		Thread.sleep(5000);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementExists(objCIOTransferPage.warningMessageArea),
				"SMAB-T3287: Validate that no warning message is displayed on CIO Transfer screen");
	
		objCIOTransferPage.logout();
	}
	
	/*
	 * Verify the warning message on CIO Transfer screen when UT is created on Retired Parcel Without 99 PUC
	 */
	
	@Test(description = "SMAB-T3287:Verify the warning message on CIO Transfer screen when UT is created on Retired Parcel without 99 PUC", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_WarningMessageForRetiredParcelWithout99PUC(String loginUser) throws Exception {
		
		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Retired' limit 1";
		String retiredApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String retiredApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT id FROM PUC_Code__c where Name in ('101- Single Family Home','105 - Apartment') limit 1");
		salesforceAPI.update("Parcel__c", retiredApnId, "PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		
		// Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(retiredApn);
		
		// Step3: Create UT event and validate warning message on CIO Transfer screen
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		softAssert.assertTrue(!objCIOTransferPage.verifyElementExists(objCIOTransferPage.warningMessageArea),
				"SMAB-T3287: Validate that no warning message is displayed on CIO Transfer screen");
		
		// Step4: Edit the Transfer activity and update the Transfer Code
		ReportLogger.INFO("Add the Transfer Code");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(6, objCIOTransferPage.transferCodeLabel);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
				"SMAB-T3287: Validate that CIO staff is able to update and save values on CIO Transfer Screen");
		
		objCIOTransferPage.logout();
	}
	
	
	/*
	 * Verify details on the Unrecorded Transfer event
	 */
	
	@Test(description = "SMAB-T3231:Verify details on the Unrecorded Transfer event", dataProvider = "loginCIOStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ChangeInOwnershipManagement","UnrecordedEvent" })
	public void UnrecordedEvent_TransferScreenConfiguration(String loginUser) throws Exception {
		
		//Getting Owner or Account records
		String assesseeName = objMappingPage.getOwnerForMappingAction();
		
		//Getting Active APN
		String queryAPNValue = "select Name, Id from Parcel__c where Status__c='Active' limit 1";
		String activeApn = salesforceAPI.select(queryAPNValue).get("Name").get(0);
		String activeApnId = salesforceAPI.select(queryAPNValue).get("Id").get(0);
		
		//Setup the data for the validations
		String legalDescriptionValue="Legal PM 85/25-260";
		String execEnv= System.getProperty("region");	
		
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData, "DataToCreateOwnershipRecord");
		
		String OwnershipAndTransferCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
	       Map<String, String> hashMapOwnershipAndTransferCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferCreationData,
				"dataToCreateMailToRecordsWithIncompleteData");
	    
	    String OwnershipAndTransferGranteeCreationData =  testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		   Map<String, String> hashMapOwnershipAndTransferGranteeCreationData = objUtil.generateMapFromJsonFile(OwnershipAndTransferGranteeCreationData,
					"dataToCreateGranteeWithIncompleteData");
		   
		//Get values from Database and enter values in the Parcels
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT id, Name FROM PUC_Code__c where Name Not in ('99-RETIRED PARCEL') limit 1");
		HashMap<String, ArrayList<String>> responseSitusDetails= salesforceAPI.select("SELECT Id, Name FROM Situs__c where Name != NULL LIMIT 1");
		String primarySitusId=responseSitusDetails.get("Id").get(0);
		String primarySitusValue=responseSitusDetails.get("Name").get(0);
		
		jsonObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("Primary_Situs__c",primarySitusId);
		salesforceAPI.update("Parcel__c", activeApnId, jsonObject);
		
		//Delete Ownership records on the Parcel
		objMappingPage.deleteOwnershipFromParcel(activeApnId);
		
		// Add ownership records in the parcels
        objMappingPage.login(users.SYSTEM_ADMIN);
        objMappingPage.searchModule(PARCELS);
        objMappingPage.globalSearchRecords(activeApn);
        objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
        objMappingPage.scrollToBottom();
        objParcelsPage.createOwnershipRecord(assesseeName, hashMapCreateOwnershipRecordData);
        
        objWorkItemHomePage.logout();
        Thread.sleep(5000);
		
		//Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		//Step2: Opening the PARCELS page 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);
		
		//Step3: Create UT event and get the Transfer ID
		objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		String recordeAPNTransferID = driver.getCurrentUrl().split("/")[6];
		String unrecordedEventId = objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.eventIDLabel);
		
		//Step4 : Validate the values on Transfer Screen
		ReportLogger.INFO("Validate the UT values");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.eventIDLabel, "").substring(0, 2),"UT",
				"SMAB-T3231: Validate that CIO staff is able to verify the prefix of Event ID");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.eventIDLabel, "").length(),"10",
				"SMAB-T3231: Validate that CIO staff is able to verify the length of Event ID");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.situsLabel, ""),primarySitusValue,
				"SMAB-T3231: Validate that CIO staff is able to verify the Situs value on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.shortLegalDescriptionLabel, ""),legalDescriptionValue,
				"SMAB-T3231: Validate that CIO staff is able to verify the Short Legal Description value on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.pucCodeLabel, ""),responsePUCDetails.get("Name").get(0),
				"SMAB-T3231: Validate that CIO staff is able to verify the PUC value on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.doeLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3231: Validate that CIO staff is able to verify the DOE on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dovLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3231: Validate that CIO staff is able to verify the DOV on UT");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dorLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Recording")),
				"SMAB-T3231: Validate that CIO staff is able to verify the DOR on UT");
		
		//Step5: Edit the Transfer activity and update the DOE
		ReportLogger.INFO("Update the DOE");
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.doeLabel);
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.doeLabel);
		objCIOTransferPage.enter(objCIOTransferPage.doeLabel, "07/05/2021");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(10, objCIOTransferPage.doeLabel);
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.doeLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Updated DOE")),
				"SMAB-T3231: Validate that CIO staff is able to update and save DOE on CIO Transfer Screen");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dovLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Event")),
				"SMAB-T3231: Validate that DOV on CIO Transfer Screen still remains the same");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.dorLabel, ""),objExemptionsPage.removeZeroInMonthAndDay(dataToCreateUnrecordedEventMap.get("Date of Recording")),
				"SMAB-T3231: Validate that DOR on CIO Transfer Screen still remains the same");
		
		
		//objCIOTransferPage.Click(driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//a[contains(@title,'more actions')]")));
		
		
		/*
		 * String xpathStr1 =
		 * "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//a[contains(@href,'\"\r\n"
		 * + activeApnId + \"')]//span[text() = 'View All']"; WebElement fieldLocator1 =
		 * objCIOTransferPage.locateElement(xpathStr1, 30);
		 * objCIOTransferPage.Click(fieldLocator1);
		 * objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.
		 * ownershipLabelOnGridForGrantee);
		 * 
		 */
		
		
		//Step6: Navigating to mail to screen and Create mail to record 
		ReportLogger.INFO("Navigate to Mail-To screen and create a Mail To record");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/"+recordeAPNTransferID+""+"/related/CIO_Transfer_Mail_To__r/view");
	    objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.newButton));
	    objCIOTransferPage.enter(objCIOTransferPage.formattedName1Label, hashMapOwnershipAndTransferCreationData.get("Formatted Name1"));
	    objCIOTransferPage.enter(objCIOTransferPage.startDate, hashMapOwnershipAndTransferCreationData.get("Start Date"));
	    objCIOTransferPage.enter(objCIOTransferPage.endDate,  hashMapOwnershipAndTransferCreationData.get("End Date"));
		objCIOTransferPage.enter(objCIOTransferPage.mailingZip,hashMapOwnershipAndTransferCreationData.get("Mailing Zip"));
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		objCIOTransferPage.waitForElementToBeVisible(3,objCIOTransferPage.formattedName1Label );		  
		softAssert.assertContains( objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.formattedName1Label),hashMapOwnershipAndTransferCreationData.get("Formatted Name1"),
				"SMAB-T3231: Verify user is  able to save mail to record");
		
		//Step7: Navigate to RAT screen and validate number of Grantors/Grantee on the UT activity 
		ReportLogger.INFO("Navigate back to RAT and validate number of Grantors/Grantee on the UT activity");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfGrantorLabel);
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfGrantorLabel),"0",
				"SMAB-T3231: Verify user is  able to validate number of Grantors on the UT activity");
		softAssert.assertContains( objCIOTransferPage.getElementText(objCIOTransferPage.numberOfGranteeLabel),"0",
				"SMAB-T3231: Verify user is  able to validate number of Grantee on the UT activity");
		
		//Step8 :Create the new Grantee
		ReportLogger.INFO("Create New Grantee record");
		objCIOTransferPage.createNewGranteeRecords(recordeAPNTransferID, hashMapOwnershipAndTransferGranteeCreationData);			
		
		//Step9: Navigate to RAT screen and click View ALL to see all Grantee records in grid
		ReportLogger.INFO("Navigate to RAT screen and click View ALL to see all Grantee records in grid");
		driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfGrantorLabel);
        
        String xpathStr1 = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//a[contains(@href,'CIO_Transfer_Grantee')]//span[text() = 'View All']";		        
        WebElement fieldLocator1 = objCIOTransferPage.locateElement(xpathStr1, 30);
        objCIOTransferPage.Click(fieldLocator1);
        objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.ownershipLabelOnGridForGrantee);
        
        // Step10: Navigate to RAT screen and validate the details in the grid
        ReportLogger.INFO("Validate the Grantee record in Grid");
        HashMap<String, ArrayList<String>>HashMapLatestGrantee  = objCIOTransferPage.getGridDataInHashMap();
        softAssert.assertEquals(HashMapLatestGrantee.get("Recorded Document").get(0), unrecordedEventId, 
    		  "SMAB-T3231: Validate the Recorded Document number on Grantee record");
        softAssert.assertEquals(HashMapLatestGrantee.get("Status").get(0), "Active", 
    		  "SMAB-T3231: Validate the status on Grantee record");
        softAssert.assertEquals(HashMapLatestGrantee.get("Owner Percentage").get(0), hashMapOwnershipAndTransferGranteeCreationData.get("Owner Percentage")+".0000%", 
    		  "SMAB-T3231: Validate the percentage on Grantee record");
        softAssert.assertEquals(HashMapLatestGrantee.get("Grantee/Retain Owner Name").get(0),hashMapOwnershipAndTransferGranteeCreationData.get("Last Name") , 
        		  "SMAB-T3231: Validate the Grantee Name on Grantee record");
        
        //Step11: Navigate to RAT screen and click View ALL to see current Ownership records in grid
        ReportLogger.INFO("Navigate to RAT screen and click View ALL to see current Ownership records in grid");
        driver.navigate().to("https://smcacre--"+execEnv+".lightning.force.com/lightning/r/Recorded_APN_Transfer__c/"+recordeAPNTransferID+"/view");
        objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.numberOfGrantorLabel);
              
		String xpathStr2 = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//a[contains(@href,'"+ activeApnId + "')]//span[text() = 'View All']"; 
		WebElement fieldLocator2 = objCIOTransferPage.locateElement(xpathStr2, 30);
		objCIOTransferPage.Click(fieldLocator2);
		objCIOTransferPage.waitForElementToBeVisible(10,objCIOTransferPage.
		ownershipLabelOnGridForGrantee);
	
        // Step12: Navigate to RAT screen and validate the details in the grid
        ReportLogger.INFO("Validate the Current Ownership record in Grid");
        HashMap<String, ArrayList<String>>HashMapLatestOwner  = objCIOTransferPage.getGridDataInHashMap();
        softAssert.assertEquals(HashMapLatestOwner.get("Owner").get(0), assesseeName, 
    		  "SMAB-T3231: Validate the owner name on Grantee record");
        softAssert.assertEquals(HashMapLatestOwner.get("Status").get(0), "Active", 
    		  "SMAB-T3231: Validate the status on Grantee record");
        softAssert.assertEquals(HashMapLatestOwner.get("Ownership Percentage").get(0), "100.0000%", 
    		  "SMAB-T3231: Validate the percentage on Grantee record");
        softAssert.assertEquals(HashMapLatestOwner.get("Ownership Start Date").get(0),hashMapCreateOwnershipRecordData.get("Ownership Start Date") , 
    		  "SMAB-T3231: Validate the start date on Grantee record");
      
       
		objCIOTransferPage.logout();
	}

}