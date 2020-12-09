package com.apas.Tests.WorkItemsTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.apas.Reports.ReportLogger;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class ManualWorkItems_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;
	LoginPage objLoginPage;
	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil;
	SoftAssertion softAssert;
	SalesforceAPI salesforceAPI;
	ApasGenericPage apasGenericObj;
	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objLoginPage = new LoginPage(driver);
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objUtil = new Util();
		apasGenericObj = new ApasGenericPage(driver);
		softAssert = new SoftAssertion();
		salesforceAPI = new SalesforceAPI();
	}
	
	/**
	 * This method is to verify that user is able to view 'Use Code' and 'Street fields getting automatically populated in the work item record related to the linked Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1994:verify that user is able to view 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
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
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC and Primary Situs field (Street) have values saved
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);

		// fetching the PUC and Primary Situs fields values of parcel
		puc = objWorkItemHomePage.getFieldValueFromAPAS("PUC", "Parcel Information");
		primarySitus = objWorkItemHomePage.getFieldValueFromAPAS("Primary Situs", "Parcel Information");

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,
				"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertTrue(primarySitus.contains(objWorkItemHomePage.getFieldValueFromAPAS("Street", "Reference Data Details")),
				"SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");

		objWorkItemHomePage.logout();
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
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);

		// fetching the PUC and Primary Situs fields values of parcel
		puc = objWorkItemHomePage.getFieldValueFromAPAS("PUC", "Parcel Information");

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		//Step 4:Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Street' is blank and 'Use CoDE' field gets automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Street", "Reference Data Details"),"","SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");

		objWorkItemHomePage.logout();
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
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the BPP Accounts page  and searching a BPP Account where Roll code is not blank
		objWorkItemHomePage.searchModule(BPP_ACCOUNTS);
		objWorkItemHomePage.globalSearchRecords(bppAccount);

		// Step 3: Creating Manual work item for the BPP Account 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		
		//Step 4:Clicking the  details tab for the work item newly created and fetching the RoLL code and Date Fields values
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Roll Code' field and 'Date' field gets automatically populated in the work item record related to the linked BPP ACCOUNT
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Roll Code", "Reference Data Details"),rollCode,
				"SMAB-T2075: Validation that 'Roll Code' fields getting automatically populated in the work item record related to the linked BPP Account");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,
				"SMAB-T2075: Validation that 'Date' fields is equal to the 1/1/"+currentRollYear);
		
		objWorkItemHomePage.logout();
	}

	/**
	 * This method is to verify that user gets prior date of value sequencing restriction warning message for Parcels
	 */
	@Test(description = "SMAB-T2219: Verify that user gets the warning message when trying to accept the work item with prior DOV for Parcels", dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, groups = {"regression","work_item_manual"})
	public void WorkItems_Parcels_PriorDateOfValueSequencing(String loginUser) throws Exception {

		// fetching a parcel where PUC is not blank but  Primary Situs is blank
		String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
		String apnValue= salesforceAPI.select(queryAPNValue).get("Name").get(0);

		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData, "DOVSequencing");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);

		// Step3: Creating Manual work item with earlier DOV
		hashMapmanualWorkItemData.put("DOV","11/21/2020");
		String workItemWithEarlierDOV = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step4: Creating Manual work item with Later DOV
		hashMapmanualWorkItemData.put("DOV","11/22/2020");
		String workItemWithLaterDOV = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step5:Accept the work items created above
		objWorkItemHomePage.searchModule(HOME);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);

		objWorkItemHomePage.acceptWorkItem(workItemWithEarlierDOV);
		objWorkItemHomePage.acceptWorkItem(workItemWithLaterDOV);

		//Step 5: DOV Sequencing warning message
		String actualDOVSequencingWarningMessage = objWorkItemHomePage.getAlertMessage();
		String expectedDOVSequencingWarningMessage = "Other work items exist for this Parcel with an earlier DOV. Please work those Work Items before this one.";
		softAssert.assertEquals(actualDOVSequencingWarningMessage,expectedDOVSequencingWarningMessage,"SMAB-T2219: Validation for DOV Sequencing warning message");

		objWorkItemHomePage.logout();
	}


	/**
	 * This method is to verify that user gets prior date of value sequencing restriction warning message for BPP Accounts
	 */
	@Test(description = "SMAB-T2219: Verify that user gets the warning message when trying to accept the work item with prior DOV for BPP Accounts", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","work_item_manual"})
	public void WorkItems_BPPAccounts_PriorDateOfValueSequencing(String loginUser) throws Exception {

		String queryBPPAccountValue = "SELECT Name FROM BPP_Account__c where Status__C = 'Active' limit 1";
		String BPPAccountValue= salesforceAPI.select(queryBPPAccountValue).get("Name").get(0);

		Map<String, String> hashMapManualWorkItemData = objUtil.generateMapFromJsonFile(testdata.MANUAL_WORK_ITEMS_BPP_ACCOUNTS, "DOVSequencing");

		// Step1: Login to the APAS application using the credentials passed through data provider
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the BPP Account
		objWorkItemHomePage.searchModule(BPP_ACCOUNTS);
		objWorkItemHomePage.globalSearchRecords(BPPAccountValue);

		// Step3: Creating Manual work item with earlier DOV
		hashMapManualWorkItemData.put("DOV","11/21/2020");
		String workItemWithEarlierDOV = objParcelsPage.createWorkItem(hashMapManualWorkItemData);

		// Step4: Creating Manual work item with Later DOV
		hashMapManualWorkItemData.put("DOV","11/22/2020");
		String workItemWithLaterDOV = objParcelsPage.createWorkItem(hashMapManualWorkItemData);

		//Step5:Accept the work items created above
		objWorkItemHomePage.searchModule(HOME);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);

		objWorkItemHomePage.acceptWorkItem(workItemWithEarlierDOV);
		objWorkItemHomePage.acceptWorkItem(workItemWithLaterDOV);

		//Step 5: DOV Sequencing warning message
		String actualDOVSequencingWarningMessage = objWorkItemHomePage.getAlertMessage();
		String expectedDOVSequencingWarningMessage = "Other work items exist for this Parcel with an earlier DOV. Please work those Work Items before this one.";
		softAssert.assertEquals(actualDOVSequencingWarningMessage,expectedDOVSequencingWarningMessage,"SMAB-T2219: Validation for DOV Sequencing warning message");

		objWorkItemHomePage.logout();
	}


	/**
	 * Verify that work items are routed correctly as per the work item routing drop down for Parcels
	 */
	@Test(description = "SMAB-T1985,SMAB-T1989,SMAB-T1991: Verify that work items are routed correctly as per the work item routing drop down for Parcels", dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, groups = {"regression","work_item_manual"})
	public void WorkItems_Parcels_ManualWorkItemRouting(String loginUser) throws Exception {
		String workPool = "RP Admin";

		// fetching a parcel where PUC is not blank but  Primary Situs is blank
		String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
		String apnValue= salesforceAPI.select(queryAPNValue).get("Name").get(0);

		String querySupervisor = "select name from user where id in (SELECT Supervisor__c FROM Work_Pool__c where Name = '" + workPool + "')";
		String supervisor= salesforceAPI.select(querySupervisor).get("Name").get(0);

		String queryLevel2Supervisor = "select name from user where id in (SELECT Level_2_Supervisor__c FROM Work_Pool__c where Name = '" + workPool + "')";
		String level2Supervisor= salesforceAPI.select(queryLevel2Supervisor).get("Name").get(0);

		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;

		// Step1: Login to the APAS application using the credentials passed through data provider
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the PARCELS page and searching a parcel
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);

		// Step3: Creating Manual work item with work item routing as "Give Work Item to Someone Else"
		Map<String, String> hashMapGiveWorkItemToSomeoneElse = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToSomeoneElse");
		String workItemAssignedToSomeoneElse = objParcelsPage.createWorkItem(hashMapGiveWorkItemToSomeoneElse);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.componentActionsButtonText)),"SMAB-T1991 : Validation that Parcel screen is displayed when work item routing option is selected as 'Give Work Item to Someone Else'");

		// Step4: Creating Manual work item with work item routing as "Give Work Item to Default Work Pool"
		Map<String, String> hashMapGiveWorkItemToDefaultWorkPool = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingToDefaultPool");
		String workItemDefaultToWorkPool = objParcelsPage.createWorkItem(hashMapGiveWorkItemToDefaultWorkPool);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.componentActionsButtonText)),"SMAB-T1989 : Validation that Parcel screen is displayed when work item routing option is selected as 'Give Work Item to Default Work Pool'");

		// Step5: Creating Manual work item with work item routing as "Give Work Item to Me"
		Map<String, String> hashMapGiveWorkItemToMe = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToMe");
		objParcelsPage.createWorkItem(hashMapGiveWorkItemToMe);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.markStatusAsCompleteButton),"SMAB-T1985 : Validation that Work Item screen is displayed when work item routing option is selected as 'Give Work Item to Me'");
		objWorkItemHomePage.openTab(objWorkItemHomePage.tabDetails);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Me'");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To"),"appraisal supportAUT","SMAB-T1985: Validation that newly created work item is assigned to the logged in user");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("DOV"),hashMapGiveWorkItemToMe.get("DOV"),"SMAB-T1804: Validation that DOV is reflected correctly");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Due Date"),hashMapGiveWorkItemToMe.get("Due Date"),"SMAB-T1804: Validation that Due Date is reflected correctly");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Approver"),supervisor,"SMAB-T1804: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Level2 Approver"),level2Supervisor,"SMAB-T1804: Validation that newly created work item having the correct level2 approver");

		// Step6: Opening the Home Page
		objWorkItemHomePage.searchModule(HOME);

		// Step7: Validation of the work item created for default work pool
		objWorkItemHomePage.globalSearchRecords(workItemDefaultToWorkPool);
		objWorkItemHomePage.openTab(objWorkItemHomePage.tabDetails);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Default Work Pool'");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To"),"","SMAB-T1989: Validation that newly created work item is not assigned to any work user");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool"),workPool,"SMAB-T1989: Validation that newly created work item is in the default work pool");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Approver"),supervisor,"SMAB-T1989: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Level2 Approver"),level2Supervisor,"SMAB-T1989: Validation that newly created work item having the correct level2 approver");

		// Step7: Validation of the work item created for Someone Else
		objWorkItemHomePage.globalSearchRecords(workItemAssignedToSomeoneElse);
		objWorkItemHomePage.openTab(objWorkItemHomePage.tabDetails);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Someone Else'");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To"),hashMapGiveWorkItemToSomeoneElse.get("Work Item Owner"),"SMAB-T1991: Validation that newly created work item is assigned to the selected user in work item routing");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Approver"),supervisor,"SMAB-T1991: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Level2 Approver"),level2Supervisor,"SMAB-T1991: Validation that newly created work item having the correct level2 approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool"),workPool,"SMAB-T1991: Validation that newly created work item is in the correct work pool");

		//Logging off the APAS
		objWorkItemHomePage.logout();
	}

	/**
	 * Verify that work items are routed correctly as per the work item routing drop down for Parcels
	 */
	@Test(description = "SMAB-T1985,SMAB-T1989,SMAB-T1991: Verify that work items are routed correctly as per the work item routing drop down for BPP Account", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","work_item_manual"})
	public void WorkItems_BPPAccount_ManualWorkItemRouting(String loginUser) throws Exception {
		String workPool = "BPP Admin";

		String queryBPPAccountValue = "SELECT Name FROM BPP_Account__c where Status__C = 'Active' limit 1";
		String BPPAccountValue= salesforceAPI.select(queryBPPAccountValue).get("Name").get(0);

		String querySupervisor = "select name from user where id in (SELECT Supervisor__c FROM Work_Pool__c where Name = '" + workPool + "')";
		String supervisor= salesforceAPI.select(querySupervisor).get("Name").get(0);

		String queryLevel2Supervisor = "select name from user where id in (SELECT Level_2_Supervisor__c FROM Work_Pool__c where Name = '" + workPool + "')";
		String level2Supervisor= salesforceAPI.select(queryLevel2Supervisor).get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS_BPP_ACCOUNTS;

		// Step1: Login to the APAS application using the credentials passed through data provider
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the PARCELS page and searching a parcel
		objWorkItemHomePage.searchModule(BPP_ACCOUNTS);
		objWorkItemHomePage.globalSearchRecords(BPPAccountValue);

		// Step3: Creating Manual work item with work item routing as "Give Work Item to Someone Else"
		Map<String, String> hashMapGiveWorkItemToSomeoneElse = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToSomeoneElse");
		String workItemAssignedToSomeoneElse = objParcelsPage.createWorkItem(hashMapGiveWorkItemToSomeoneElse);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.componentActionsButtonText)),"SMAB-T1991 : Validation that Parcel screen is displayed when work item routing option is selected as 'Give Work Item to Someone Else'");

		// Step4: Creating Manual work item with work item routing as "Give Work Item to Default Work Pool"
		Map<String, String> hashMapGiveWorkItemToDefaultWorkPool = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingToDefaultPool");
		String workItemDefaultToWorkPool = objParcelsPage.createWorkItem(hashMapGiveWorkItemToDefaultWorkPool);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.componentActionsButtonText)),"SMAB-T1989 : Validation that Parcel screen is displayed when work item routing option is selected as 'Give Work Item to Default Work Pool'");

		// Step5: Creating Manual work item with work item routing as "Give Work Item to Me"
		Map<String, String> hashMapGiveWorkItemToMe = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToMe");
		objParcelsPage.createWorkItem(hashMapGiveWorkItemToMe);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.markStatusAsCompleteButton),"SMAB-T1985 : Validation that Work Item screen is displayed when work item routing option is selected as 'Give Work Item to Me'");
		objWorkItemHomePage.openTab(objWorkItemHomePage.tabDetails);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Me'");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To"),"bpp adminAUT","SMAB-T1985: Validation that newly created work item is assigned to the logged in user");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("DOV"),hashMapGiveWorkItemToMe.get("DOV"),"SMAB-T1804: Validation that DOV is reflected correctly");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Due Date"),hashMapGiveWorkItemToMe.get("Due Date"),"SMAB-T1804: Validation that Due Date is reflected correctly");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Approver"),supervisor,"SMAB-T1804: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Level2 Approver"),level2Supervisor,"SMAB-T1804: Validation that newly created work item having the correct level2 approver");

		// Step6: Opening the Home Page
		objWorkItemHomePage.searchModule(HOME);

		// Step7: Validation of the work item created for default work pool
		objWorkItemHomePage.globalSearchRecords(workItemDefaultToWorkPool);
		objWorkItemHomePage.openTab(objWorkItemHomePage.tabDetails);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Default Work Pool'");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To"),"","SMAB-T1989: Validation that newly created work item is not assigned to any work user");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool"),workPool,"SMAB-T1989: Validation that newly created work item is in the default work pool");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Approver"),supervisor,"SMAB-T1989: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Level2 Approver"),level2Supervisor,"SMAB-T1989: Validation that newly created work item having the correct level2 approver");

		// Step7: Validation of the work item created for Someone Else
		objWorkItemHomePage.globalSearchRecords(workItemAssignedToSomeoneElse);
		objWorkItemHomePage.openTab(objWorkItemHomePage.tabDetails);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Someone Else'");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To"),hashMapGiveWorkItemToSomeoneElse.get("Work Item Owner"),"SMAB-T1991: Validation that newly created work item is assigned to the selected user in work item routing");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Approver"),supervisor,"SMAB-T1991: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Level2 Approver"),level2Supervisor,"SMAB-T1991: Validation that newly created work item having the correct level2 approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool"),workPool,"SMAB-T1991: Validation that newly created work item is in the correct work pool");

		//Logging off the APAS
		objWorkItemHomePage.logout();
	}
	/**
	* Verify WorkItem should be visible in  2nd level supervisor's 'Completed' tab
	**/
    @Test(description = "SMAB-T2466: Verify WorkItem should be visible in  2nd level supervisor's 'Completed' tab", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"smoke", "regression", "Work_Items_Manual" }, alwaysRun = true)
	public void WorkItem_VerifyWIpresentInCompletedTabOf2nd_LevelApprover(String loginUser) throws Exception {

		  //fetching a parcel where PUC is not blank but Primary Situs is blank 
		  String queryAPNValue ="select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1" ;  
		  HashMap<String, ArrayList<String>> response =salesforceAPI.select(queryAPNValue); 
		  String apnValue=response.get("Name").get(0); 
		  String workItemCreationData = System.getProperty("user.dir") +testdata.MANUAL_WORK_ITEMS; 
		  Map<String, String> hashMapmanualWorkItemData =objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateWorkItemOfTypeRP");
		  
		  // Step 1: Login to the APAS application using the credentials of staff user
		  apasGenericObj.login(users.EXEMPTION_SUPPORT_STAFF);
		  // Step 2: Opening the PARCELS page and searching a parcel
		  apasGenericObj.searchModule(PARCELS);
		  apasGenericObj.globalSearchRecords(apnValue); 
		  
		  // Step 3: Creating Manual work item for the Parcel 
		  String Workitem =objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		  objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);
		  objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		  objWorkItemHomePage.waitUntilPageisReady(driver);
		  
		  // Step 4: Update Value in workitem details page to update second level supervisor
		  apasGenericObj.editAndInputFieldData(objWorkItemHomePage.value,objWorkItemHomePage.value, "21");
		  
		  //Step 5: Open the Work Item Home Page 
		  driver.navigate().refresh(); 
		  apasGenericObj.searchModule(HOME);
		  objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		  objWorkItemHomePage.waitUntilPageisReady(driver);
		  
		  //steps 6: select work item from in progress tab and mark complete
		  objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
		  objWorkItemHomePage.Click(objWorkItemHomePage.btnMarkComplete);
		  objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		  objWorkItemHomePage.Click(apasGenericObj.closeButton);
		  apasGenericObj.logout();
		  Thread.sleep(15000);
		  
		  //Login With supervisor 1 with rp admin 
		  apasGenericObj.login(loginUser);
		  apasGenericObj.searchModule(modules.HOME);
		  objWorkItemHomePage.waitUntilPageisReady(driver);
		  
		  // steps 7: Approve WI from needs my approval tab
		  objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		  objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
		  objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
		  objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		  objWorkItemHomePage.Click(apasGenericObj.closeButton);
		  apasGenericObj.logout();
		  Thread.sleep(15000);
		  
          //Login supervisor 2 users.DATA_ADMIN
		  apasGenericObj.login(users.DATA_ADMIN);
		  apasGenericObj.searchModule(modules.HOME);
		  objWorkItemHomePage.waitUntilPageisReady(driver);
		  
          // steps 8: Approve WI from needs my approval tab
		  objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		  objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
		  objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
		  objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		  objWorkItemHomePage.Click(apasGenericObj.closeButton);
		  objWorkItemHomePage.Click(objWorkItemHomePage.completedTab);
		  
		  //steps 9: Validation on after approve by second lavel approval work item should be visible in completed tab
		  HashMap<String, ArrayList<String>> PrimaryWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
          softAssert.assertTrue(PrimaryWorkItems.get("Work Item Number").contains(Workitem), "SMAB-T2466: Verify WorkItem should be visible in  2nd level supervisor's 'Completed' tab"); 
	      apasGenericObj.logout();
     }
}
