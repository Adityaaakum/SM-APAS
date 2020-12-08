package com.apas.Tests.WorkItemsTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
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
import com.apas.generic.ApasGenericFunctions;

public class ManualWorkItems_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;
	LoginPage objLoginPage;
	ApasGenericFunctions objApasGenericFunctions;
	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil;
	SoftAssertion softAssert;
	SalesforceAPI salesforceAPI;
	Page objPage;
	ApasGenericPage objApasGenericPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objLoginPage = new LoginPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objParcelsPage = new ParcelsPage(driver);
		objPage = new Page(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		salesforceAPI = new SalesforceAPI();
	}
	
	/**
	 * This method is to verify that user is able to view 'Use Code' and 'Street fields getting automatically populated in the work item record related to the linked Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1994,SMAB-T1838:verify that user is able to view 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel, Verify user is able to view Work Item details after submitting it for approval", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual" })
	public void WorkItems_VerifyLinkedParcelUseCodeStreetFields(String loginUser) throws Exception {
		String puc;
		String primarySitus;		
		
		// fetching a parcel where PUC and Primary Situs are not blank		
		String queryAPNValue = "select Name from Parcel__c where PUC_Code_Lookup__c!= null and Primary_Situs__c !=null AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String apnValue= response.get("Name").get(0);
		
		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeRP");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC and Primary Situs field (Street) have values saved
		objApasGenericFunctions.searchModule(PARCELS);
		objApasGenericFunctions.globalSearchRecords(apnValue);

		// fetching the PUC and Primary Situs fields values of parcel
		puc = objApasGenericFunctions.getFieldValueFromAPAS("PUC", "Parcel Information");
		primarySitus = objApasGenericFunctions.getFieldValueFromAPAS("Primary Situs", "Parcel Information");

		// Step 3: Creating Manual work item for the Parcel 
		String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,
				"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertTrue(primarySitus.contains(objApasGenericFunctions.getFieldValueFromAPAS("Street", "Reference Data Details")),
				"SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");
		
		// Step 6: User submits the Work Item for Approval 
		ReportLogger.INFO("User submits the Work Item for Approval :: " + WINumber);
		driver.navigate().refresh();
		Thread.sleep(2000);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline, 10);
		objPage.javascriptClick(objWorkItemHomePage.submittedforApprovalTimeline);
		Thread.sleep(2000);
		objPage.javascriptClick(objWorkItemHomePage.markStatusCompleteBtn);
		Thread.sleep(2000);
		softAssert.assertEquals(objPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1838:Verify user is able to submit the Work Item for approval");
		
		// Step 7: Validate the Work Item details after the Work Item is submitted for approval
		ReportLogger.INFO("User validates the Work Item details after it is Submitted for Approval");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);
		
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,
				"SMAB-T1838: Validate user is able to validate the value of 'Use Code'' field");
		softAssert.assertTrue(primarySitus.contains(objApasGenericFunctions.getFieldValueFromAPAS("Street", "Reference Data Details")),
				"SMAB-T1838: Validate user is able to validate the value of 'Street' field");
		
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Status", "Information"),"Submitted for Approval","SMAB-T1838: Validate user is able to validate the value of 'Status' field");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Type", "Information"),"RP","SMAB-T1838: Validate user is able to validate the value of 'Type' field");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Action", "Information"),"CPI Factor","SMAB-T1838: Validate user is able to validate the value of 'Action' field");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool", "Information"),"Disabled Veterans","SMAB-T1838: Validate user is able to validate the value of 'Work Pool' field");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Priority", "Information"),"Urgent","SMAB-T1838: Validate user is able to validate the value of 'Priority' field");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Reference", "Information"),"Test WI","SMAB-T1838: Validate user is able to validate the value of 'Reference' field");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("APN", "Information"),apnValue,"SMAB-T1838: Validate user is able to validate the value of 'APN' field");

		objApasGenericFunctions.logout();
	}
	
	/**
	 * This method is to verify that user is able to view 'Use Code' which is  not blank but 'Street' field get automatically populated in the work item record related to the linked Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1994:verify that user is able to view 'Use Code' which is  blank but 'Street' field get automatically populated in the work item record related to the linked Parcel", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual"  })
	public void WorkItems_VerifyLinkedParcelUseCodeNotBlank_StreetFieldBlank(String loginUser) throws Exception {
		String puc;
		
		// fetching a parcel where PUC is not blank but  Primary Situs is blank
		String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String apnValue= response.get("Name").get(0);
		
		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeRP");
				
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
		objApasGenericFunctions.searchModule(PARCELS);
		objApasGenericFunctions.globalSearchRecords(apnValue);

		// fetching the PUC and Primary Situs fields values of parcel
		puc = objApasGenericFunctions.getFieldValueFromAPAS("PUC", "Parcel Information");

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		//Step 4:Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Street' is blank and 'Use CoDE' field gets automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Street", "Reference Data Details"),"","SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");

		objApasGenericFunctions.logout();
	}
	/**
	 * This method is to Verify User is able to view 'Roll Code' and 'Date' fields getting automatically populated in the work item record linked to a BPP Account
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2075:Verify User is able to view 'Roll Code' and 'Date' fields getting automatically populated in the work item record linked to a BPP Account", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual"  })
	public void WorkItems_VerifyLinkedBPPAccountUseCode_DateFields(String loginUser) throws Exception {
		
		// fetching a BPP account where Roll code  is not blank 
		String queryBPPAccount = "select Name,Roll_Code__c from BPP_Account__c where Roll_Code__c!=NULL Limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryBPPAccount);
		String bppAccount= response.get("Name").get(0);
		String rollCode= response.get("Roll_Code__c").get(0);

		
		String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy");
		String currentRollYear=ExemptionsPage.determineRollYear(currentDate);

		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeBPP");
				
		// Step1: Login to the APAS application using the credentials passed through dataprovider (BPP Business Admin)
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the BPP Accounts page  and searching a BPP Account where Roll code is not blank
		objApasGenericFunctions.searchModule(BPP_ACCOUNTS);
		objApasGenericFunctions.globalSearchRecords(bppAccount);

		// Step 3: Creating Manual work item for the BPP Account 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		//Step 4:Clicking the  details tab for the work item newly created and fetching the RoLL code and Date Fields values
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Roll Code' field and 'Date' field gets automatically populated in the work item record related to the linked BPP ACCOUNT
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Roll Code", "Reference Data Details"),rollCode,
				"SMAB-T2075: Validation that 'Roll Code' fields getting automatically populated in the work item record related to the linked BPP Account");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,
				"SMAB-T2075: Validation that 'Date' fields is equal to the 1/1/"+currentRollYear);
		
		objApasGenericFunctions.logout();
	}
	
	/**
	 * This method is to Verify User is able to create a Work Pool or update an existing Work Pool to indicate if a second level Approver is needed
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1935,SMAB-T1936,SMAB-T1940:Verify User is able to create a Work Pool or update an existing Work Pool to indicate if a second level Approver is needed,Verify User can designate a value amount for the second level Approver on the Work Pool record,Verify the 2nd Level approver on a Work Pool cannot be the same user as the designated Supervisor", dataProvider = "loginBppAndRpBusinessAdminUsers", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual" })
	public void WorkItems_VerifyWorkPoolCreation(String loginUser) throws Exception {
		
		String poolName = "Test".concat(java.time.LocalDateTime.now().toString());
		String env = "";
		if (System.getProperty("region").toUpperCase().trim().equals("QA")) env = "qa";
		if (System.getProperty("region").toUpperCase().trim().equals("E2E")) env = "e2e";
		String rpBusinessAdmin = "rp.admin.aut@smcacre.org." + env;
		String bppBusinessAdmin = "bpp.admin.aut@smcacre.org." + env;
		String dataAdmin = "data.admin.aut@smcacre.org." + env;
		
		//Get the user name through queries
		String rpBusinessAdminNameQuery = "select Name from User where UserName__c = '"+ rpBusinessAdmin + "'";
		HashMap<String, ArrayList<String>> response1 = new SalesforceAPI().select(rpBusinessAdminNameQuery);
        String rpBusinessAdminName = response1.get("Name").get(0);
		
        String bppBusinessAdminNameQuery = "select Name from User where UserName__c = '"+ bppBusinessAdmin + "'";
		HashMap<String, ArrayList<String>> response2 = new SalesforceAPI().select(bppBusinessAdminNameQuery);
        String bppBusinessAdminName = response2.get("Name").get(0);
        
        String dataAdminNameQuery = "select Name from User where UserName__c = '"+ dataAdmin + "'";
		HashMap<String, ArrayList<String>> response3 = new SalesforceAPI().select(dataAdminNameQuery);
        String dataAdminName = response3.get("Name").get(0);
				
		// Step1: Login to the APAS application using the credentials passed through dataprovider 
        objApasGenericFunctions.login(loginUser);

		// Step2: Opening the Work pool module and create a NEW one
		objApasGenericFunctions.searchModule(WORK_POOL);
		ReportLogger.INFO("Create a New Work Pool record");
		objPage.Click(objWorkItemHomePage.newButton);
		objPage.enter("Work Pool Name", poolName);
		objApasGenericPage.searchAndSelectFromDropDown("Supervisor", rpBusinessAdminName);
		objApasGenericPage.searchAndSelectFromDropDown("Level2 Supervisor", bppBusinessAdminName);
		objPage.enter("Level2 Value Criteria", "500");
		objPage.Click(objPage.getButtonWithText(objWorkItemHomePage.SaveButton));
		
		// Step3: Validate the success message after creation of work pool
		Thread.sleep(1000);
		softAssert.assertContains(objApasGenericFunctions.getAlertMessage(),"Work Pool \"" + poolName + "\" was created.","SMAB-T1935 : Validate user is able to create a Work Pool" );
		
		// Step4: Edit the work pool record and update some field values in it
		Thread.sleep(1000);
		ReportLogger.INFO("Update the Work Pool record");
		objWorkItemHomePage.waitForElementToBeVisible(6, objPage.getButtonWithText(objWorkItemHomePage.editButton));
		objPage.Click(objPage.getButtonWithText(objWorkItemHomePage.editButton));
	
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Level2 Value Criteria"),"500.00",
				"SMAB-T1935 : Validate user is able to update Level2 Value Criteria in the Work Pool");
		
		objPage.clearSelection("Level2 Supervisor");
		ReportLogger.INFO("Update the value for Level2 Supervisor in the Work Pool record");
		objApasGenericPage.searchAndSelectFromDropDown("Level2 Supervisor", dataAdminName);
		objPage.enter("Level2 Value Criteria", "400");
		objPage.Click(objPage.getButtonWithText(objWorkItemHomePage.SaveButton));
		
		// Step5 Validate the success message after saving the work pool
		Thread.sleep(1000);
		softAssert.assertContains(objApasGenericFunctions.getAlertMessage(),"Work Pool \"" + poolName + "\" was saved.","SMAB-T1935 : Validate user is able to edit and save the Work Pool" );
		Thread.sleep(1000);
		objWorkItemHomePage.waitForElementToBeVisible(6, objPage.getButtonWithText(objWorkItemHomePage.editButton));
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Level2 Supervisor"),dataAdminName,
				"SMAB-T1935 : Validate user is able to update value for Level2 Supervisor in the Work Pool");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Level2 Value Criteria"),"400.00",
				"SMAB-T1936: Validate user is able to update value for Level2 Value Criteria in the Work Pool");
		
		// Step6: Edit the work pool record again with same user in Approver & Level2 Supervisor fields
		Thread.sleep(1000);
		ReportLogger.INFO("Update the value for Level2 Supervisor in the Work Pool record to keep it same as the Supervisor");
		objPage.Click(objPage.getButtonWithText(objWorkItemHomePage.editButton));
		objPage.clearSelection("Level2 Supervisor");
		objApasGenericPage.searchAndSelectFromDropDown("Level2 Supervisor", rpBusinessAdminName);
		softAssert.assertEquals(objApasGenericFunctions.saveRecordAndGetError(),"Close error dialog\nWe hit a snag.\nReview the errors on this page.\nSupervisor and Level 2 Supervisor should not be same.","SMAB-T1940 : Verify the 2nd Level approver on a Work Pool cannot be the same user as the designated Supervisor");
		
		// Step7: Delete the Work Pool record
		ReportLogger.INFO("Delete the Work Pool record");
		String deleteWPQuery = "select id from Work_Pool__c where Name = '" + poolName + "'";
		salesforceAPI.delete("Work_Pool__c", deleteWPQuery);
		
		objApasGenericFunctions.logout();
	}

}