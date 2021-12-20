package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
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
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.jayway.jsonpath.JsonPath;

public class Parcel_Management_AuditTrail_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	String auditTrailData;
	AuditTrailPage objTrailPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage = new MappingPage(driver);
		auditTrailData = testdata.AUDIT_TRAIL_DATA;
		objTrailPage=new AuditTrailPage(driver);
	}

	@Test(description = "SMAB-T3700:Verify that user is able to create audit trail and linkage relationship should be created having EventID populated", dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelAuditTrail" })
	public void Parcel_AuditTrailUpdatesViaComponentActions(String loginUser) throws Exception {

		String activeApn = objParcelsPage.fetchActiveAPN();
		Map<String, String> dataToCreateAuditTrailRecord = objUtil.generateMapFromJsonFile(auditTrailData,
				"DataToCreateAuditTrail");
		
		Map<String, String> dataToCreateAuditTrailRecordToLinkWithParent = objUtil.generateMapFromJsonFile(auditTrailData,
				"DataToCreateAuditTrailToLinkWithParent");

		// Step 1: Login to the APAS application
		objMappingPage.login(loginUser);

		// Step 2: Opening the PARCELS page
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);

		// Step 3: Create audit Trail
		objParcelsPage.createUnrecordedEvent(dataToCreateAuditTrailRecord);

		//Step 4: Add the Event Id and get Event Title value
		String eventID = "1234Test";
		objParcelsPage.Click(objParcelsPage.editFieldButton("Event ID"));
		objParcelsPage.enter(objParcelsPage.getWebElementWithLabel("Event ID"), eventID);
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Save"));
		

		// Step 5: Opening the PARCELS page
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeApn);

		// Step 6: Create audit trail to link with parent audit trail
		objParcelsPage.createUnrecordedEvent(dataToCreateAuditTrailRecordToLinkWithParent);

		String eventIdChild = objParcelsPage.getFieldValueFromAPAS("Event ID");

		//Step 7: Verify that the audit trail is linked to parent audit trail as Event Id would be auto populated
		softAssert.assertEquals(eventIdChild, eventID,
				"SMAB-T3700: Verify that user is able to create audit trail and linkage relationship should be created having EventID populated");

		//Step 8 : Logout
		objParcelsPage.logout();
	}
	
	/**
	 * This method is to Verify that audit trail is created in Characteristic and business event with linkage record created"
	 * @param loginUser
	 * @throws Exception
	 */	
	
	@Test(description = "SMAB-T3702,SMAB-T3703:Verify that audit trail is created in Characteristica nd business event with linkage record created", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression", "ParcelManagement", "ParcelAuditTrail" })
	public void ParcelManagement_AudiTrail_Characteristics(String loginUser)
			throws Exception {

		String executionEnv = System.getProperty("region");
		String queryAPN = "Select Name,Id  From Parcel__c where name like '0%' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn = responseAPNDetails.get("Name").get(0);
		String apnId = responseAPNDetails.get("Id").get(0);

		ReportLogger.INFO("Delete characteristic record");
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);


		String mappingActionCreationData = testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String characteristicsRecordCreationData = testdata.CHARACTERISTICS;
		Map<String, String> hashMapImprovementCharacteristicsData = objUtil
				.generateMapFromJsonFile(characteristicsRecordCreationData, "DataToCreateImprovementCharacteristics");

		// Adding Characteristic record in the parcel
		ReportLogger.INFO("Adding Characteristic record in the parcel");
		objMappingPage.login(users.SYSTEM_ADMIN);

		driver.navigate().to(
				"https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/" + apnId + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
				objParcelsPage.getButtonWithText(objParcelsPage.parcelMapInGISPortal));

		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.createCharacteristicsOnParcel(hashMapImprovementCharacteristicsData, apn);

		objMappingPage.logout();
		Thread.sleep(4000);
		
		// Step1: Login to the APAS application using the credentials passed through
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page and searching the parcel to perform one to
		// one mapping
		driver.navigate().to(
				"https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/" + apnId + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
				objParcelsPage.getButtonWithText(objParcelsPage.parcelMapInGISPortal));

		// Step 3: Creating Manual work item for the Parcel

		String WorkItemNo = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step 4:Clicking the details tab for the work item newly created and clicking
		// on Related Action Link

		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);

		// Clicking on Action Dropdown

		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,
				hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,
				hashMapOneToOneMappingData.get("Are taxes fully paid?"));
		objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel, hashMapOneToOneMappingData.get("Reason code"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

		objMappingPage.waitForElementToBeClickable(10, objMappingPage.generateParcelButton);

		// Fetching the GRID data

		HashMap<String, ArrayList<String>> gridDataHashMap = objMappingPage.getGridDataInHashMap();
		String childApn = gridDataHashMap.get("APN").get(0);

		// Clicking on generate parcel button

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		Thread.sleep(5000);

		// Completing the work Item
		ReportLogger.INFO("Completing the work Item");
		String queryWI = "Select Id from Work_Item__c where Name = '" + WorkItemNo + "'";
		HashMap<String, ArrayList<String>> responseWI = salesforceAPI.select(queryWI);
		driver.switchTo().window(parentWindow);
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
				+ responseWI.get("Id").get(0) + "/view");
		objMappingPage.waitForElementToBeVisible(10, objWorkItemHomePage.appLauncher);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
	   	objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);		
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		
		ReportLogger.INFO("mapping supervisor login");
		objMappingPage.login(users.MAPPING_SUPERVISOR);
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
				+ responseWI.get("Id").get(0) + "/view");
		objMappingPage.waitForElementToBeVisible(10, objWorkItemHomePage.appLauncher);
		objWorkItemHomePage.completeWorkItem();
		objMappingPage.waitForElementToBeVisible(10, objWorkItemHomePage.linkedItemsWI);
		Thread.sleep(2000);
		
		ReportLogger.INFO("Navigate to child Apn");
		String query = "Select Id from Parcel__c where Name = '" + childApn + "'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
				+ response.get("Id").get(0) + "/view");
		objParcelsPage.waitForElementToBeVisible(20,
				objParcelsPage.getButtonWithText(objParcelsPage.parcelMapInGISPortal));
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.Click(objParcelsPage.fetchCharacteristicsList().get(0));
		ReportLogger.INFO("Verify Audit trail is displayed");
		objParcelsPage.waitForElementToBeVisible(objTrailPage.businessEventCharacteristicsAuditTrail, 10);
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objTrailPage.businessEventCharacteristicsAuditTrail),
				"SMAB-T3702: Verify that When a mapping work item is completed, Audit Trail component should be displayed at the characteristic level");

		objParcelsPage.Click(objTrailPage.businessEventCharacteristicsAuditTrail);
		objParcelsPage.waitForElementToBeVisible(objTrailPage.relatedBusinessRecords);
		objParcelsPage.Click(objTrailPage.relatedBusinessRecords);
		ReportLogger.INFO("verify Linked record is visible");
		objWorkItemHomePage.waitForElementToBeVisible(20, objTrailPage.linkedRecord);
		String trailSubject = objTrailPage.linkedRecord.getText();
		softAssert.assertEquals(trailSubject, "Linked Record",
				"SMAB-T3703: Verified that Related Businnes Events displays Linked Record");

		objWorkItemHomePage.logout();
	}
	
	/**
	* This method is to Parcel Management-Verify that When an Internal/External Request mapping work item is created, 
	* the system will create two audit trail records against the parcel associated to the request
	* @param loginUser
	* @throws Exception
	*/
	@Test(description = "SMAB-T3653,SMAB-T3655,SMAB-T3657:Parcel Management - Verify that When an Internal Mapping Request work item is created, "
			+ "the system will create two audit trail records against the parcel associated to the request ", 
			dataProvider = "loginMappingUser", 
			dataProviderClass = DataProviders.class, 
			groups = {"Regression","ParcelManagement","ParcelAuditTrail" })
	public void ParcelManagement_VerifyAuditTrailsCreatedOnInternalMappingRequestWI(String loginUser) throws Exception {
		
		//Fetching parcel that are Active
				String queryApnDetails ="SELECT Id,Name FROM Parcel__c where primary_situs__c != NULL and "
						+ "Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where "
						+ "type__c='CIO') and (Not Name like '100%') and (Not Name like '800%') "
						+ "and (Not Name like '%990') and (Not Name like '134%') Limit 2";
				
				JSONObject jsonObject = objMappingPage.getJsonObject();
				HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryApnDetails);
				String apn1=responseAPNDetails.get("Name").get(0);
				String apn2=responseAPNDetails.get("Name").get(1);
				String apnId1=responseAPNDetails.get("Id").get(0);
				String apnId2=responseAPNDetails.get("Id").get(1);
				
				objMappingPage.deleteOwnershipFromParcel(apnId1);
				objMappingPage.deleteOwnershipFromParcel(apnId2);
				
				String concatenateCondoWithNonCondo = apn1+","+apn2;
						
				String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
				HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

				String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
				HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		        
				String queryPUC = "SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c "
						+ "where Status__c='Active') limit 1";
				HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select(queryPUC);
				
				String legalDescriptionValue="Legal PM 85/25-260";
				String districtValue="District01";
				String parcelSize	= "200";

				jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
				jsonObject.put("Status__c","Active");
				jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
				jsonObject.put("District__c",districtValue);
				jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
				jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
				jsonObject.put("Lot_Size_SQFT__c",parcelSize);
		   
				salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);

				String workItemCreationData =  testdata.MANUAL_WORK_ITEMS;
				Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
						"DataToCreateWorkItemOfTypeMappingWithActionInternalRequestCombine");
		        
				String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
				Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
						"DataToPerformCombineMappingAction");
				
				// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
				objWorkItemHomePage.login(loginUser);

				// Step2: Navigating to the Parcel View page								
				String execEnv = System.getProperty("region");

				driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com"
						+ "/lightning/r/Parcel__c/"
						+ apnId1 + "/view");
						objParcelsPage.waitForElementToBeVisible(20, 
								objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));								
				
				// Step 3: Creating Manual work item for the Parcel 
				String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
				driver.navigate().refresh();						
		        
				String sqlgetTransanctionTrail ="SELECT Business_Event__r.name , Business_Event__r.Type__c, Business_Event__r.Event_Type__c "
						+ "FROM Work_Item_Linkage__c where work_item__r.name = '"+WINumber+"'";
					
				String jsonResponse = salesforceAPI.getSelectQueryDateInJson(sqlgetTransanctionTrail);		
		        
				String jsonPathName_1 = "$.records[0].Business_Event__r.Name";
				String TransactionTrailName_1 = JsonPath.read(jsonResponse, jsonPathName_1);
				String jsonPathType_1 = "$.records[0].Business_Event__r.Type__c";
				String TransactionTrailType_1 = JsonPath.read(jsonResponse, jsonPathType_1);
				String jsonPathEventType_1 = "$.records[0].Business_Event__r.Event_Type__c";
				String TransactionalTrailEventType_1 = JsonPath.read(jsonResponse, jsonPathEventType_1);
				
				boolean flag = objWorkItemHomePage.verifyTransactionalTrailRowDatafromWebTable(TransactionTrailName_1,TransactionTrailType_1);
				
				softAssert.assertTrue(flag, "SMAB-T2733: "
						+ "Transactional trail created successfully : "+TransactionTrailName_1 
						+ " with Type: "+TransactionTrailType_1);
										
				
				String jsonPathName_2 = "$.records[1].Business_Event__r.Name";
				String TransactionTrailName_2 = JsonPath.read(jsonResponse, jsonPathName_2);
				String jsonPathType_2 = "$.records[1].Business_Event__r.Type__c";
				String TransactionTrailType_2 = JsonPath.read(jsonResponse, jsonPathType_2);
				String jsonPathEventType_2 = "$.records[1].Business_Event__r.Event_Type__c";
				String TransactionalTrailEventType_2 = JsonPath.read(jsonResponse, jsonPathEventType_2);
				
				flag = objWorkItemHomePage.verifyTransactionalTrailRowDatafromWebTable(TransactionTrailName_2,TransactionTrailType_2);
				
				softAssert.assertTrue(flag, "SMAB-T3653: "
						+ "Transactional trail created successfully : "+TransactionTrailName_2 
						+ " with Type: "+TransactionTrailType_2);								
				
				
				objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
				
				String EventID = objWorkItemHomePage.getFieldValueFromAPAS("Event ID","Information");
				
				objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsTab);
				
				objWorkItemHomePage.clickTransactionTrailLink(TransactionTrailName_1);
				
				String TTEventID_1 = objMappingPage.getFieldValueFromAPAS("Event ID");
				
				softAssert.assertEquals(EventID, TTEventID_1, "SMAB-T3655: The Event ID of the Work Item will be "
						+ "passed to Audit trail record");
				
				String expectedEventType_1 = objMappingPage.getFieldValueFromAPAS("Event Type");
				
				softAssert.assertEquals(TransactionalTrailEventType_1, expectedEventType_1, "SMAB-T2733: "
						+ "Transactional trail Event Type:"+TransactionalTrailEventType_1);				
				
				driver.navigate().back();
				
				objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsTab);
				
				objWorkItemHomePage.clickTransactionTrailLink(TransactionTrailName_2);
				
		        String TTEventID_2 = objMappingPage.getFieldValueFromAPAS("Event ID");
				
				softAssert.assertEquals(EventID, TTEventID_2, "SMAB-T3655: The Event ID of the Work Item will be "
						+ "passed to Audit trail record");
				
				String expectedEventType_2 = objMappingPage.getFieldValueFromAPAS("Event Type");
				
				softAssert.assertEquals(TransactionalTrailEventType_2, expectedEventType_2, "SMAB-T3653: "
						+ "Transactional trail Event Type:"+TransactionalTrailEventType_2);
				
				driver.navigate().back();				
				
				objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
				
				String parentWindow = driver.getWindowHandle();
				ReportLogger.INFO("Switch to the Mapping Action screen");
				objWorkItemHomePage.switchToNewWindow(parentWindow);					
				
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateCondoWithNonCondo);
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				
				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
				objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
						
				Thread.sleep(5000);	
						
				String actualResonCode = objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value");
				
				softAssert.assertEquals(EventID, actualResonCode, "SMAB-T3655: The Event ID of the Work Item will be "
						+ "passed to Reason Code field on Mapping screen");
				
				objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel2,apn1);
				objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));				
				
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
				
				driver.switchTo().window(parentWindow);
				
				objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
				Thread.sleep(5000);
				objWorkItemHomePage.logout();
				Thread.sleep(5000);
				ReportLogger.INFO(" Supervisor logins to close the WI ");
				objMappingPage.login(users.MAPPING_SUPERVISOR);
				
				String query = "Select Id from Work_Item__c where Name = '"+WINumber+"'";
				HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
				driver.navigate().to("https://smcacre--"+execEnv+
				".lightning.force.com/lightning/r/Work_Item__c/"+response.get("Id").get(0)+"/view");
				
				objWorkItemHomePage.completeWorkItem();
				
				jsonResponse = salesforceAPI.getSelectQueryDateInJson(sqlgetTransanctionTrail);
				jsonPathEventType_2 = "$.records[1].Business_Event__r.Event_Type__c";
				TransactionalTrailEventType_2 = JsonPath.read(jsonResponse, jsonPathEventType_2);
				objWorkItemHomePage.clickTransactionTrailLink(TransactionTrailName_2);
				
		        expectedEventType_2 = objMappingPage.getFieldValueFromAPAS("Event Type");
				
				softAssert.assertEquals(TransactionalTrailEventType_2, expectedEventType_2,"SMAB-T3657: Parcel Management - Verify that When a "
						+ "mapping work item is completed, the draft audit trail record will be updated "
						+ "with the Request ID and the correct audit trail label activity based on the mapping action performed");
				
				objWorkItemHomePage.logout();
	}
	
	/**
	* This method is to Parcel Management-Verify that When an Internal/External Request mapping work item is created, 
	* the system will create two audit trail records against the parcel associated to the request
	* @param loginUser
	* @throws Exception
	*/
	@Test(description = "SMAB-T3672,SMAB-T3673,SMAB-3675:Parcel Management - Verify that When an Internal Mapping Request work item is created, "
			+ "the system will create two audit trail records against the parcel associated to the request ", 
			dataProvider = "loginMappingUser", 
			dataProviderClass = DataProviders.class, 
			groups = {"Regression","ParcelManagement","ParcelAuditTrail" })
	public void ParcelManagement_VerifyAuditTrailsCreatedOnExternalMappingRequestWI(String loginUser) throws Exception {
		
		//Fetching parcel that are Active
		String queryApnDetails ="SELECT Id,Name FROM Parcel__c where primary_situs__c != NULL and "
				+ "Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where "
				+ "type__c='CIO') and (Not Name like '1%') and (Not Name like '8%') "
				+ "and (Not Name like '%990') and (Not Name like '134%') Limit 2";
		
		JSONObject jsonObject = objMappingPage.getJsonObject();
		
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryApnDetails);
		String apn1=responseAPNDetails.get("Name").get(0);
		String apn2=responseAPNDetails.get("Name").get(1);
		String apnId1=responseAPNDetails.get("Id").get(0);
		String apnId2=responseAPNDetails.get("Id").get(1);
		
		objMappingPage.deleteOwnershipFromParcel(apnId1);
		objMappingPage.deleteOwnershipFromParcel(apnId2);
		
		String concatenateCondoWithNonCondo = apn1+","+apn2;
				
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
        
		String queryPUC = "SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c "
				+ "where Status__c='Active') limit 1";
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select(queryPUC);
		
		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";
		String parcelSize	= "200";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObject.put("Lot_Size_SQFT__c",parcelSize);
   
		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);

		String workItemCreationData =  testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeMappingWithActionCustomerRequestCombine");
        
		String mappingActionCreationData = testdata.COMBINE_MAPPING_ACTION;
		Map<String, String> hashMapCombineMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformCombineMappingAction");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objWorkItemHomePage.login(loginUser);

		// Step2: Navigating to the Parcel View page						
		String execEnv = System.getProperty("region");

		driver.navigate().to("https://smcacre--" + execEnv + ".lightning.force.com"
				+ "/lightning/r/Parcel__c/"
				+ apnId1 + "/view");
				objParcelsPage.waitForElementToBeVisible(20, 
						objParcelsPage.getButtonWithText(objParcelsPage.componentActionsButtonText));								

		
		// Step 3: Creating Manual work item for the Parcel 
		String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		driver.navigate().refresh();
		Thread.sleep(5000);		
        
		String sqlgetTransanctionTrail ="SELECT Business_Event__r.name , Business_Event__r.Type__c, Business_Event__r.Event_Type__c "
				+ "FROM Work_Item_Linkage__c where work_item__r.name = '"+WINumber+"'";
			
		String jsonResponse = salesforceAPI.getSelectQueryDateInJson(sqlgetTransanctionTrail);		
        
		String jsonPathName_1 = "$.records[0].Business_Event__r.Name";
		String TransactionTrailName_1 = JsonPath.read(jsonResponse, jsonPathName_1);
		String jsonPathType_1 = "$.records[0].Business_Event__r.Type__c";
		String TransactionTrailType_1 = JsonPath.read(jsonResponse, jsonPathType_1);
		String jsonPathEventType_1 = "$.records[0].Business_Event__r.Event_Type__c";
		String TransactionalTrailEventType_1 = JsonPath.read(jsonResponse, jsonPathEventType_1);
		
		boolean flag = objWorkItemHomePage.verifyTransactionalTrailRowDatafromWebTable(TransactionTrailName_1,TransactionTrailType_1);
		
		softAssert.assertTrue(flag, "SMAB-T3673: "
				+ "Transactional trail created successfully : "+TransactionTrailName_1 
				+ " with Type: "+TransactionTrailType_1);
								
		
		String jsonPathName_2 = "$.records[1].Business_Event__r.Name";
		String TransactionTrailName_2 = JsonPath.read(jsonResponse, jsonPathName_2);
		String jsonPathType_2 = "$.records[1].Business_Event__r.Type__c";
		String TransactionTrailType_2 = JsonPath.read(jsonResponse, jsonPathType_2);
		String jsonPathEventType_2 = "$.records[1].Business_Event__r.Event_Type__c";
		String TransactionalTrailEventType_2 = JsonPath.read(jsonResponse, jsonPathEventType_2);
		
		flag = objWorkItemHomePage.verifyTransactionalTrailRowDatafromWebTable(TransactionTrailName_2,TransactionTrailType_2);
		
		softAssert.assertTrue(flag, "SMAB-T3673: "
				+ "Transactional trail created successfully : "+TransactionTrailName_2 
				+ " with Type: "+TransactionTrailType_2);								
		
		
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		
		String EventID = objWorkItemHomePage.getFieldValueFromAPAS("Event ID","Information");
		
		objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsTab);
		
		objWorkItemHomePage.clickTransactionTrailLink(TransactionTrailName_1);
		
		String TTEventID_1 = objMappingPage.getFieldValueFromAPAS("Event ID");
		
		softAssert.assertEquals(EventID, TTEventID_1, "SMAB-T3672: The Event ID of the Work Item will be "
				+ "passed to Audit trail record");
		
		String expectedEventType_1 = objMappingPage.getFieldValueFromAPAS("Event Type");
		
		softAssert.assertEquals(TransactionalTrailEventType_1, expectedEventType_1, "SMAB-T2733: "
				+ "Transactional trail Event Type:"+TransactionalTrailEventType_1);				
		
		driver.navigate().back();
		
		objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsTab);
		
		objWorkItemHomePage.clickTransactionTrailLink(TransactionTrailName_2);
		
        String TTEventID_2 = objMappingPage.getFieldValueFromAPAS("Event ID");
		
		softAssert.assertEquals(EventID, TTEventID_2, "SMAB-T3655: The Event ID of the Work Item will be "
				+ "passed to Audit trail record");
		
		String expectedEventType_2 = objMappingPage.getFieldValueFromAPAS("Event Type");
		
		softAssert.assertEquals(TransactionalTrailEventType_2, expectedEventType_2, "SMAB-T3672: "
				+ "Transactional trail Event Type:"+TransactionalTrailEventType_2);
		
		driver.navigate().back();		
		
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		Thread.sleep(2000);			
		
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,concatenateCondoWithNonCondo);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapCombineMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
				
		Thread.sleep(2000);	
				
		String actualResonCode = objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value");
		
		softAssert.assertEquals(EventID, actualResonCode, "SMAB-T3672: The Event ID of the Work Item will be "
				+ "passed to Reason Code field on Mapping screen");
		
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel2,apn1);
		objMappingPage.scrollToElement(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));				
		
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		
		driver.switchTo().window(parentWindow);
		
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		
		ReportLogger.INFO(" Supervisor logins to close the WI ");
		objMappingPage.login(users.MAPPING_SUPERVISOR);
		
		String query = "Select Id from Work_Item__c where Name = '"+WINumber+"'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);
		driver.navigate().to("https://smcacre--"+execEnv+
		".lightning.force.com/lightning/r/Work_Item__c/"+response.get("Id").get(0)+"/view");
				
		objWorkItemHomePage.completeWorkItem();
		
		jsonResponse = salesforceAPI.getSelectQueryDateInJson(sqlgetTransanctionTrail);
		jsonPathEventType_2 = "$.records[1].Business_Event__r.Event_Type__c";
		TransactionalTrailEventType_2 = JsonPath.read(jsonResponse, jsonPathEventType_2);
		objWorkItemHomePage.clickTransactionTrailLink(TransactionTrailName_2);
		
        expectedEventType_2 = objMappingPage.getFieldValueFromAPAS("Event Type");
		
		softAssert.assertEquals(TransactionalTrailEventType_2, expectedEventType_2,"SMAB-T3657: Parcel Management - Verify that When a "
				+ "mapping work item is completed, the draft audit trail record will be updated "
				+ "with the Request ID and the correct audit trail label activity based on the mapping action performed");
		
		objWorkItemHomePage.logout();
		
		
	}
}