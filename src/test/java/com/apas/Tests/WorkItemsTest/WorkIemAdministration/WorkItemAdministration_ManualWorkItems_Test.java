package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.apas.PageObjects.*;
import com.apas.Reports.ReportLogger;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class WorkItemAdministration_ManualWorkItems_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	WorkPoolPage objWorkPoolPage;
	Page objPage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	ApasGenericPage apasGenericObj;
	RoutingAssignmentPage objRoutingAssignmentPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		apasGenericObj = new ApasGenericPage(driver);
		objParcelsPage = new ParcelsPage(driver);
		objPage = new Page(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objRoutingAssignmentPage = new RoutingAssignmentPage(driver);
		objWorkPoolPage = new WorkPoolPage(driver);
	}

	/**
	 * This method is to verify that user is able to view 'Use Code' and 'Street fields getting automatically populated in the work item record related to the linked Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1994,SMAB-T1838:verify that user is able to view 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel, Verify user is able to view Work Item details after submitting it for approval", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression","WorkItemAdministration" })
	public void WorkItemAdministration_Manual_LinkedParcelUseCodeStreetFields(String loginUser) throws Exception {
		String puc;
		String primarySitus;		

		// fetching a parcel where PUC and Primary Situs are not blank		
		String queryAPNValue = "select Name from Parcel__c where PUC_Code_Lookup__c!= null and Primary_Situs__c !=null AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String apnValue= response.get("Name").get(0);

		String workItemCreationData =  testdata.MANUAL_WORK_ITEMS;
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
		String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,
				"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertTrue(primarySitus.contains(objWorkItemHomePage.getFieldValueFromAPAS("Street", "Reference Data Details")),
				"SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");

		// Step 6: User submits the Work Item for Approval 
		ReportLogger.INFO("User submits the Work Item for Approval :: " + WINumber);
		driver.navigate().refresh();
		Thread.sleep(2000);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline, 10);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
		Thread.sleep(2000);
		softAssert.assertEquals(objPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1838:Verify user is able to submit the Work Item for approval");

		// Step 7: Validate the Work Item details after the Work Item is submitted for approval
		ReportLogger.INFO("User validates the Work Item details after it is Submitted for Approval");
		objWorkItemHomePage.openTab("Details");
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,
				"SMAB-T1838: Validate user is able to validate the value of 'Use Code'' field");
		softAssert.assertTrue(primarySitus.contains(apasGenericObj.getFieldValueFromAPAS("Street", "Reference Data Details")),
				"SMAB-T1838: Validate user is able to validate the value of 'Street' field");

		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus,"Information"),"Submitted for Approval","SMAB-T1838: Validate user is able to validate the value of 'Status' field");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkItemHomePage.wiTypeDetailsPage, "Information"),"RP","SMAB-T1838: Validate user is able to validate the value of 'Type' field");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkItemHomePage.wiActionDetailsPage, "Information"),"CPI Factor","SMAB-T1838: Validate user is able to validate the value of 'Action' field");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkItemHomePage.wiWorkPoolDetailsPage, "Information"),"Disabled Veterans","SMAB-T1838: Validate user is able to validate the value of 'Work Pool' field");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkItemHomePage.wiPriorityDetailsPage, "Information"),"Urgent","SMAB-T1838: Validate user is able to validate the value of 'Priority' field");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkItemHomePage.wiReferenceDetailsPage, "Information"),"Test WI","SMAB-T1838: Validate user is able to validate the value of 'Reference' field");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkItemHomePage.wiAPNDetailsPage, "Information"),apnValue,"SMAB-T1838: Validate user is able to validate the value of 'APN' field");

		objWorkItemHomePage.logout();
	}

	/**
	 * This method is to verify that user is able to view 'Use Code' which is  not blank but 'Street' field get automatically populated in the work item record related to the linked Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1994:verify that user is able to view 'Use Code' which is  blank but 'Street' field get automatically populated in the work item record related to the linked Parcel", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression","WorkItemAdministration"  })
	public void WorkItemAdministration_Manual_LinkedParcelUseCodeNotBlank_StreetFieldBlank(String loginUser) throws Exception {
		String puc;

		// fetching a parcel where PUC is not blank but  Primary Situs is blank
		String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String apnValue= response.get("Name").get(0);

		String workItemCreationData =  testdata.MANUAL_WORK_ITEMS;
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
	@Test(description = "SMAB-T2075:Verify User is able to view 'Roll Code' and 'Date' fields getting automatically populated in the work item record linked to a BPP Account", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Regression","WorkItemAdministration"  }, enabled = false)
	public void WorkItemAdministration_Manual_LinkedBPPAccountUseCode_DateFields(String loginUser) throws Exception {

		// fetching a BPP account where Roll code  is not blank 
		String queryBPPAccount = "select Name,Roll_Code__c from BPP_Account__c where Roll_Code__c!=NULL  and Status__c ='ACTIVE' Limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryBPPAccount);
		String bppAccount= response.get("Name").get(0);
		String rollCode= response.get("Roll_Code__c").get(0);

		String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy");
		String currentRollYear=ExemptionsPage.determineRollYear(currentDate);

		String workItemCreationData =  testdata.MANUAL_WORK_ITEMS;
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
	@Test(description = "SMAB-T2219: Verify that user gets the warning message when trying to accept the work item with prior DOV for Parcels", dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, groups = {"Regression","WorkItemAdministration"})
	public void WorkItemAdministration_Manual_Parcels_PriorDateOfValueSequencing(String loginUser) throws Exception {

		String apnValue= objParcelsPage.fetchActiveAPN();

		String workItemCreationData =testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData, "DOVSequencing");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);

		String activeApnUrl=driver.getCurrentUrl();
		// Step3: Creating Manual work item with earlier DOV
		hashMapmanualWorkItemData.put("DOV","11/21/2020");
		String workItemWithEarlierDOV = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

//		objWorkItemHomePage.globalSearchRecords(apnValue);
		driver.navigate().to(activeApnUrl);
		// Step4: Creating Manual work item with Later DOV
		hashMapmanualWorkItemData.put("DOV","11/22/2020");
		String workItemWithLaterDOV = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step5:Accept the work items created above
		driver.navigate().refresh();
		Thread.sleep(3000);
		objWorkItemHomePage.searchModule(HOME);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_PROGRESS);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
		objWorkItemHomePage.waitForElementToBeClickable(workItemWithEarlierDOV);
		objWorkItemHomePage.acceptWorkItem(workItemWithEarlierDOV);
		Thread.sleep(5000);
		objWorkItemHomePage.Click(objWorkItemHomePage.ageDays);
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
	@Test(description = "SMAB-T2220: Verify that user gets the warning message when trying to accept the work item with prior DOV for BPP Accounts", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Regression","WorkItemAdministration"}, enabled = false)
	public void WorkItemAdministration_Manual_BPPAccounts_PriorDateOfValueSequencing(String loginUser) throws Exception {

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
		driver.navigate().refresh();
		objWorkItemHomePage.searchModule(HOME);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_IN_POOL);

		objWorkItemHomePage.acceptWorkItem(workItemWithEarlierDOV);
		objWorkItemHomePage.acceptWorkItem(workItemWithLaterDOV);

		//Step 5: DOV Sequencing warning message
		String actualDOVSequencingWarningMessage = objWorkItemHomePage.getAlertMessage();
		String expectedDOVSequencingWarningMessage = "Other work items exist for this Parcel with an earlier DOV. Please work those Work Items before this one.";
		softAssert.assertEquals(actualDOVSequencingWarningMessage,expectedDOVSequencingWarningMessage,"SMAB-T2220: Validation for DOV Sequencing warning message");

		objWorkItemHomePage.logout();
	}


	/**
	 * Verify that work items are routed correctly as per the work item routing drop down for Parcels
	 */
	@Test(description = "SMAB-T1985,SMAB-T1989,SMAB-T1988: Verify that work items are routed correctly as per the work item routing drop down for Parcels", dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, groups = {"Regression","WorkItemAdministration"})
	public void WorkItemAdministration_Manual_Parcels_ManualWorkItemRouting(String loginUser) throws Exception {
		String workPool = "RP Admin";

		// fetching a parcel where PUC is not blank but  Primary Situs is blank
		String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
		String apnValue= salesforceAPI.select(queryAPNValue).get("Name").get(0);

		String querySupervisor = "select name from user where id in (SELECT Supervisor__c FROM Work_Pool__c where Name = '" + workPool + "')";
		String supervisor= salesforceAPI.select(querySupervisor).get("Name").get(0);

		String queryLevel2Supervisor = "select name from user where id in (SELECT Level_2_Supervisor__c FROM Work_Pool__c where Name = '" + workPool + "')";
		String level2Supervisor= salesforceAPI.select(queryLevel2Supervisor).get("Name").get(0);

		String workItemCreationData =testdata.MANUAL_WORK_ITEMS;

		// Step1: Login to the APAS application using the credentials passed through data provider
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the PARCELS page and searching a parcel
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);
		String activeApnUrl=driver.getCurrentUrl();
		// Step3: Creating Manual work item with work item routing as "Give Work Item to Someone Else"
		Map<String, String> hashMapGiveWorkItemToSomeoneElse = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToSomeoneElse");
		String workItemAssignedToSomeoneElse = objParcelsPage.createWorkItem(hashMapGiveWorkItemToSomeoneElse);
//		objWorkItemHomePage.globalSearchRecords(apnValue);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.componentActionsButtonText)),"SMAB-T1988 : Validation that Parcel screen is displayed when work item routing option is selected as 'Give Work Item to Someone Else'");

		// Step4: Creating Manual work item with work item routing as "Give Work Item to Default Work Pool"
		Map<String, String> hashMapGiveWorkItemToDefaultWorkPool = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingToDefaultPool");
		String workItemDefaultToWorkPool = objParcelsPage.createWorkItem(hashMapGiveWorkItemToDefaultWorkPool);
//		objWorkItemHomePage.globalSearchRecords(apnValue);
		driver.navigate().to(activeApnUrl);
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
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To"),hashMapGiveWorkItemToSomeoneElse.get("Work Item Owner"),"SMAB-T1988: Validation that newly created work item is assigned to the selected user in work item routing");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Approver"),supervisor,"SMAB-T1988: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Level2 Approver"),level2Supervisor,"SMAB-T1988: Validation that newly created work item having the correct level2 approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool"),workPool,"SMAB-T1988: Validation that newly created work item is in the correct work pool");

		//Logging off the APAS
		objWorkItemHomePage.logout();
	}

	/**
	 * This method is to Verify User is able to create a Work Pool or update an existing Work Pool to indicate if a second level Approver is needed
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T1935,SMAB-T1936,SMAB-T1940:Verify User is able to create a Work Pool or update an existing Work Pool to indicate if a second level Approver is needed,Verify User can designate a value amount for the second level Approver on the Work Pool record,Verify the 2nd Level approver on a Work Pool cannot be the same user as the designated Supervisor", dataProvider = "loginBppAndRpBusinessAdminUsers", dataProviderClass = DataProviders.class, groups = {"Regression","WorkItemAdministration" })
	public void WorkItemAdministration_Manual_WorkItems_VerifyWorkPoolCreation(String loginUser) throws Exception {

		//Step1: Setup the Work Pool Name
		String poolName="";
		String dataAdmin = CONFIG.getProperty(users.DATA_ADMIN + "UserName");
		String rpBusinessAdmin = CONFIG.getProperty(users.RP_BUSINESS_ADMIN + "UserName");
		String bppBusinessAdmin = CONFIG.getProperty(users.BPP_BUSINESS_ADMIN + "UserName");

		if(loginUser.equals(users.RP_BUSINESS_ADMIN))poolName = "Automation_Test_1";
		if(loginUser.equals(users.BPP_BUSINESS_ADMIN))poolName = "Automation_Test_2";

		//Step2: Get the user name through queries
		ReportLogger.INFO("Get the user names through SOQL queries");
		String rpBusinessAdminNameQuery = "select Name from User where UserName__c = '"+ rpBusinessAdmin + "'";
		HashMap<String, ArrayList<String>> response1 = new SalesforceAPI().select(rpBusinessAdminNameQuery);
		String rpBusinessAdminName = response1.get("Name").get(0);

		String bppBusinessAdminNameQuery = "select Name from User where UserName__c = '"+ bppBusinessAdmin + "'";
		HashMap<String, ArrayList<String>> response2 = new SalesforceAPI().select(bppBusinessAdminNameQuery);
		String bppBusinessAdminName = response2.get("Name").get(0);

		String dataAdminNameQuery = "select Name from User where UserName__c = '"+ dataAdmin + "'";
		HashMap<String, ArrayList<String>> response3 = new SalesforceAPI().select(dataAdminNameQuery);
		String dataAdminName = response3.get("Name").get(0);

		// Step3: Login to the APAS application using the credentials passed through dataprovider 
		apasGenericObj.login(loginUser);

		// Step4: Opening the Work pool module
		apasGenericObj.searchModule(WORK_POOL);
		apasGenericObj.displayRecords("All");

		// Step5.a: Create a Work Pool record if there is no existing record
		if (apasGenericObj.searchRecords(poolName).substring(0, 6).equals("0 item")) {
			ReportLogger.INFO("There is no existing Work Pool record with the name :: " + poolName);
	    	ReportLogger.INFO("Create a New Work Pool record");
	    	String successMessage = objWorkPoolPage.createWorkPool(poolName,rpBusinessAdminName,bppBusinessAdminName,"500");
	    		 				
	    	// Step5.b: Validate the success message after creation of work pool and Value Criteria field
	    	softAssert.assertEquals(successMessage,"success\nWork Pool \"" + poolName + "\" was created.\nClose","SMAB-T1935 : Validate success message on creation of the Work Pool" );
	    	softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkPoolPage.wpLevel2ValueCriteriaSupervisor),"500.00",
	    				"SMAB-T1935 : Validate user is able to enter and save Level2 Value Criteria in the Work Pool");
		}
		// Step6: Open the work pool record if there is an existing record
		else {
			ReportLogger.INFO("There is an existing Work Pool record with the name :: " + poolName);
			ReportLogger.INFO("Update the Work Pool record");
			objWorkItemHomePage.openWorkPoolRecord(poolName);
		}

		// Step7: Edit the work pool record and update field values in it
		objWorkItemHomePage.waitForElementToBeVisible(6, objPage.getButtonWithText(objWorkItemHomePage.editButton));
    	objPage.Click(objPage.getButtonWithText(objWorkItemHomePage.editButton));
    	objPage.clearSelectionFromLookup(objWorkPoolPage.wpLevel2Supervisor);
    	ReportLogger.INFO("Update the value for Level2 Supervisor in the Work Pool record");
    	apasGenericObj.searchAndSelectOptionFromDropDown(objWorkPoolPage.wpLevel2Supervisor, dataAdminName);
    	objPage.enter(objWorkPoolPage.wpLevel2ValueCriteriaSupervisor, "400");
    	String successMessage = apasGenericObj.saveRecord();
            
    	// Step8 Validate the success message after saving the work pool and other fields
    	softAssert.assertEquals(successMessage,"success\nWork Pool \"" + poolName + "\" was saved.\nClose","SMAB-T1935 : Validate user is able to edit and save the Work Pool" );
    	objWorkItemHomePage.waitForElementToBeVisible(6, objPage.getButtonWithText(objWorkItemHomePage.editButton));
    	softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkPoolPage.wpLevel2Supervisor),dataAdminName,
    				"SMAB-T1935 : Validate user is able to update value for Level2 Supervisor in the Work Pool");
    	softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS(objWorkPoolPage.wpLevel2ValueCriteriaSupervisor),"400.00",
    				"SMAB-T1936: Validate user is able to update value for Level2 Value Criteria in the Work Pool");
    		
    	// Step9: Edit the work pool record again with same user in Approver & Level2 Supervisor fields
    	ReportLogger.INFO("Update the value for Level2 Supervisor in the Work Pool record to keep it same as the Supervisor");
    	objPage.Click(objPage.getButtonWithText(objWorkItemHomePage.editButton));
    	objPage.clearSelectionFromLookup(objWorkPoolPage.wpLevel2Supervisor);
    	apasGenericObj.searchAndSelectOptionFromDropDown(objWorkPoolPage.wpLevel2Supervisor, rpBusinessAdminName);
    	softAssert.assertEquals(apasGenericObj.saveRecordAndGetError(),"Close error dialog\nWe hit a snag.\nReview the errors on this page.\nSupervisor and Level 2 Supervisor should not be same.","SMAB-T1940 : Verify the 2nd Level approver on a Work Pool cannot be the same user as the designated Supervisor");
    		
    	apasGenericObj.logout();
	}

	/**
	 * Verify that work items are routed correctly as per the work item routing drop down for Parcels
	 */
	@Test(description = "SMAB-T1987,SMAB-T1990,SMAB-T1991: Verify that work items are routed correctly as per the work item routing drop down for BPP Account", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Regression","WorkItemAdministration"}, enabled = false)
	public void WorkItemAdministration_Manual_BPPAccount_ManualWorkItemRouting(String loginUser) throws Exception {
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
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.componentActionsButtonText)),"SMAB-T1990 : Validation that Parcel screen is displayed when work item routing option is selected as 'Give Work Item to Default Work Pool'");

		// Step5: Creating Manual work item with work item routing as "Give Work Item to Me"
		Map<String, String> hashMapGiveWorkItemToMe = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToMe");
		objParcelsPage.createWorkItem(hashMapGiveWorkItemToMe);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.markStatusAsCompleteButton),"SMAB-T1987 : Validation that Work Item screen is displayed when work item routing option is selected as 'Give Work Item to Me'");
		objWorkItemHomePage.openTab(objWorkItemHomePage.tabDetails);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Me'");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To"),"bpp adminAUT","SMAB-T1987: Validation that newly created work item is assigned to the logged in user");
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
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Assigned To"),"","SMAB-T1990: Validation that newly created work item is not assigned to any work user");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool"),workPool,"SMAB-T1990: Validation that newly created work item is in the default work pool");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Approver"),supervisor,"SMAB-T1990: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Level2 Approver"),level2Supervisor,"SMAB-T1990: Validation that newly created work item having the correct level2 approver");

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
	@Test(description = "SMAB-T2474: Verify WorkItem should be visible in  2nd level supervisor's 'Completed' tab", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Regression", "WorkItemAdministration" }, alwaysRun = true)
	public void WorkItemAdministration_Manual_WIPresentInCompletedTab_2ndLevelApprover(String loginUser) throws Exception {

		//fetching a parcel where PUC is not blank but Primary Situs is blank 
		String apnValue=apasGenericObj.fetchActiveAPN();
		String workItemCreationData =testdata.MANUAL_WORK_ITEMS; 
		Map<String, String> hashMapmanualWorkItemData =objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateWorkItemOfTypeRP");

		// Step 1: Login to the APAS application using the credentials of staff user
		objWorkItemHomePage.login(users.EXEMPTION_SUPPORT_STAFF);

		// Step 2: Opening the PARCELS page and searching a parcel
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);

		// Step 3: Creating Manual work item for the Parcel 
		String Workitem =objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitUntilPageisReady(driver);

		// Step 4: Update Value in workitem details page to update second level supervisor
		apasGenericObj.editAndInputFieldData(objWorkItemHomePage.valueTextBox,objWorkItemHomePage.valueTextBox, "21");

		//Step 5: Open the Work Item Home Page 
		driver.navigate().refresh(); 
		objWorkItemHomePage.searchModule(HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		objWorkItemHomePage.waitUntilPageisReady(driver);

		//steps 6: select work item from in progress tab and mark complete
		objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
		objWorkItemHomePage.Click(objWorkItemHomePage.btnMarkComplete);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.closeButton, 3);
		objWorkItemHomePage.Click(objWorkItemHomePage.closeButton);
		objWorkItemHomePage.logout();
		Thread.sleep(15000);

		//Login With supervisor 1 with rp admin 
		objWorkItemHomePage.login(loginUser);
		objWorkItemHomePage.searchModule(modules.HOME);
		objWorkItemHomePage.waitUntilPageisReady(driver);

		// steps 7: Approve WI from needs my approval tab
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
		objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.closeButton, 3);
		objWorkItemHomePage.Click(objWorkItemHomePage.closeButton);
		objWorkItemHomePage.logout();
		Thread.sleep(15000);

		//Login supervisor 2 users.DATA_ADMIN
		objWorkItemHomePage.login(users.DATA_ADMIN);
		objWorkItemHomePage.searchModule(modules.HOME);
		objWorkItemHomePage.waitUntilPageisReady(driver);

		// steps 8: Approve WI from needs my approval tab
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.toggleBUtton);
		Thread.sleep(3000); 
		objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
		objWorkItemHomePage.Click(objWorkItemHomePage.btnApprove);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.closeButton, 3);
		objWorkItemHomePage.Click(objWorkItemHomePage.closeButton);
		objWorkItemHomePage.Click(objWorkItemHomePage.completedTab);

		//steps 9: Validation on after approve by second lavel approval work item should be visible in completed tab
		HashMap<String, ArrayList<String>> PrimaryWorkItems = objWorkItemHomePage.getWorkItemData(objWorkItemHomePage.TAB_COMPLETED);
		softAssert.assertTrue(PrimaryWorkItems.get("Work item #").contains(Workitem), "SMAB-T2474: Verify WorkItem should be visible in  2nd level supervisor's 'Completed' tab"); 
		objWorkItemHomePage.logout();
	}

	/**
	 * Verify Withdraw' Button on Staff and  level 1 supervisor with  submitted for approval and Approval -On Hold Status
	 **/
	@Test(description = "SMAB-T2476,SMAB-T2477,SMAB-T2478: Verify Withdraw' Button on Staff and  level 1 supervisor with  submitted for approval and Approval -On Hold Status", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Smoke", "Regression", "WorkItemAdministration" }, alwaysRun = true)
	public void WorkItemAdministration_Manual_WithrawButtonOnWI(String loginUser) throws Exception {
		String ExemptionSupport = CONFIG.getProperty(users.EXEMPTION_SUPPORT_STAFF + "UserName");
		String rpBusinessAdmin = CONFIG.getProperty(users.RP_BUSINESS_ADMIN + "UserName");

		//Get the user name through queries
		String ExemptionSupportNameQuery = "select Name from User where UserName__c = '"+ ExemptionSupport + "'";
		HashMap<String, ArrayList<String>> response1 = new SalesforceAPI().select(ExemptionSupportNameQuery);
		String ExemptionSupportName = response1.get("Name").get(0);

		//Get the user name through queries
		String rpBusinessAdminNameQuery = "select Name from User where UserName__c = '"+ rpBusinessAdmin + "'";
		response1 = new SalesforceAPI().select(rpBusinessAdminNameQuery);
		String rpBusinessAdminName = response1.get("Name").get(0);

		//fetching a parcel where PUC is not blank but Primary Situs is blank 
		String apnValue=apasGenericObj.fetchActiveAPN();
		String workItemCreationData =testdata.MANUAL_WORK_ITEMS; 
		Map<String, String> hashMapmanualWorkItemData =objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateWorkItemOfTypeRP");

		// Step 1: Login to the APAS application using the credentials of staff user
		apasGenericObj.login(users.EXEMPTION_SUPPORT_STAFF);

		// Step 2: Opening the PARCELS page and searching a parcel
		apasGenericObj.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);
		// Step 3: Creating Manual work item for the Parcel 
		String Workitem =objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitUntilPageisReady(driver);

		// Step 4: Update Value in workitem details page to update second level supervisor
		apasGenericObj.editAndInputFieldData(objWorkItemHomePage.valueTextBox,objWorkItemHomePage.valueTextBox, "21");

		//Step 5: Open the Work Item Home Page 
		driver.navigate().refresh(); 
		apasGenericObj.searchModule(HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		objWorkItemHomePage.waitUntilPageisReady(driver);

		//steps 6: select work item from in progress tab and mark complete
		objWorkItemHomePage.clickCheckBoxForSelectingWI(Workitem);
		objWorkItemHomePage.Click(objWorkItemHomePage.btnMarkComplete);
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		Thread.sleep(2000);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_MY_SUBMITTED_FOR_APPROVAL);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(Workitem);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.WithdrawButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		Thread.sleep(2000);
		objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		objWorkItemHomePage.openWorkItem(Workitem);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(2000);	  
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"In Progress","SMAB-T2476,SMAB-T2477 : Status Of WI Should be In Progress ");
		driver.navigate().back();
		Thread.sleep(2000);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(Workitem);
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
		objWorkItemHomePage.waitUntilPageisReady(driver);
		Thread.sleep(2000);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_MY_SUBMITTED_FOR_APPROVAL);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(Workitem);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.WithdrawButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		Thread.sleep(2000);
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		objWorkItemHomePage.openWorkItem(Workitem);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(2000);	  
		//get user name
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Current Approver"),rpBusinessAdminName,"SMAB-T2478 : Verify Current Approver should be 1st level supervisor after withdrawthe workitem from 2nd level approval");
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Status"),"Submitted for Approval","SMAB-T2478 : Status Of WI Should be Submitted for Approval ");
		apasGenericObj.logout();
	}

	/**
	 * Verify 'withdraw' button visible on work item timeline on parcel page
	 **/
	@Test(description = "SMAB-T2479,SMAB-T2480 : Verify 'withdraw' button visible on work item timeline on parcel page", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Smoke", "Regression", "WorkItemAdministration" }, alwaysRun = true)
	public void WorkItemAdministration_Manual_Withraw_OnWITimelineOnParcel(String loginUser) throws Exception {

		//fetching a parcel where PUC is not blank but Primary Situs is blank 
		String apnValue= apasGenericObj.fetchActiveAPN();
		String workItemCreationData =testdata.MANUAL_WORK_ITEMS; 
		Map<String, String> hashMapmanualWorkItemData =objUtil.generateMapFromJsonFile(workItemCreationData,"DataToCreateWorkItemOfTypeRP");

		// Step 1: Login to the APAS application using the credentials of staff user
		apasGenericObj.login(users.EXEMPTION_SUPPORT_STAFF);

		// Step 2: Opening the PARCELS page and searching a parcel
		apasGenericObj.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);
		// Step 3: Creating Manual work item for the Parcel 
		String Workitem =objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		objWorkItemHomePage.waitForElementToBeClickable(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitUntilPageisReady(driver);
		Thread.sleep(2000);
		// Step 4: Update Value in work Item details page to update second level supervisor
		apasGenericObj.editAndInputFieldData(objWorkItemHomePage.valueTextBox,objWorkItemHomePage.valueTextBox, "21");
		driver.navigate().back();
		// Step 5: Changing the status of workitem from 'In Progress' to 'Submitted For Apporval' On workItem  timeline on parcel page 
		objWorkItemHomePage.openTab(objWorkItemHomePage.Tab_WorkItems_ON_parcel);
		objWorkItemHomePage.Click(objParcelsPage.ExpendWIOnParcels);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.SubmittedForApprovalButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);

		// Step 6: Verify  'withdraw' button on workItem  timeline on parcel page with Submitted for approval status
		objWorkItemHomePage.Click(objParcelsPage.ExpendWIOnParcels);
		softAssert.assertEquals(objParcelsPage.getFieldvalueFromWITimeLine("Status"),"Submitted for Approval","SMAB-T2479 :Verify Workitem status should be Submitted for Approval");
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.WithdrawButton)), "SMAB-T2479 :Verify 'Withdraw'should be present Workitem timeline with Submitted for Approval status",true);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.WithdrawButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);

		// Step 7: Changing the status of workitem from 'In Progress' to 'Submitted For Apporval' On workItem  timeline on parcel page 
		objWorkItemHomePage.Click(objParcelsPage.ExpendWIOnParcels);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.SubmittedForApprovalButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		apasGenericObj.logout();
		Thread.sleep(15000);

		// Login With supervisor 1 with rp admin 
		apasGenericObj.login(loginUser);
		apasGenericObj.searchModule(modules.HOME);
		objWorkItemHomePage.waitUntilPageisReady(driver);

		// steps 8: Put On Hold the  WI from needs my approval tab of supervisor level 1st
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		objWorkItemHomePage.selectWorkItemOnHomePage(Workitem);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.PutOnHoldButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		apasGenericObj.logout();
		Thread.sleep(5000);

		// Login With staff user 
		apasGenericObj.login(users.EXEMPTION_SUPPORT_STAFF);  
		apasGenericObj.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue); 
		objWorkItemHomePage.openTab(objWorkItemHomePage.Tab_WorkItems_ON_parcel);
		objWorkItemHomePage.Click(objParcelsPage.ExpendWIOnParcels);

		// Step 9: Verify  'withdraw' button on workItem  timeline on parcel page with Approval On Hold status
		softAssert.assertEquals(objParcelsPage.getFieldvalueFromWITimeLine("Status"),"Approval - On Hold","SMAB-T2479 :Verify Workitem status should be Approval -On Hold");
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.WithdrawButton)), "SMAB-T2479 :Verify 'Withdraw'should be present Workitem timeline with Approval-on Hold status",true);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.WithdrawButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);

		// Step 10: Changing the status of workitem from 'In Progress' to 'Submitted For Apporval' On workItem  timeline on parcel page 
		objWorkItemHomePage.Click(objParcelsPage.ExpendWIOnParcels);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.SubmittedForApprovalButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		apasGenericObj.logout();
		Thread.sleep(5000);

		//Login With supervisor 1 with rp admin 
		apasGenericObj.login(loginUser);
		apasGenericObj.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);	    
		objWorkItemHomePage.openTab(objWorkItemHomePage.Tab_WorkItems_ON_parcel);
		objWorkItemHomePage.Click(objParcelsPage.ExpendWIOnParcels);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.ApprovalButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		objWorkItemHomePage.Click(objParcelsPage.ExpendWIOnParcels);

		// Step 11: Verify  'withdraw' button on workItem  timeline on parcel page with Submitted for approval status
		softAssert.assertEquals(objParcelsPage.getFieldvalueFromWITimeLine("Status"),"Submitted for Approval","SMAB-T2480 :Verify Workitem status should be Submitted for Approval");
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.WithdrawButton)), "SMAB-T2480 :Verify 'Withdraw'should be present Workitem timeline with Submitted for Approval status",true);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.WithdrawButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		objWorkItemHomePage.Click(objParcelsPage.ExpendWIOnParcels);
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objParcelsPage.ApprovalButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		apasGenericObj.logout();
		Thread.sleep(5000);

		// Login supervisor 2 users.DATA_ADMIN
		objWorkItemHomePage.login(users.DATA_ADMIN);
		objWorkItemHomePage.searchModule(modules.HOME);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_NEED_MY_APPROVAL);
		objWorkItemHomePage.Click(objWorkItemHomePage.toggleBUtton);
		Thread.sleep(3000);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(Workitem);

		// steps 12: Put On Hold the  WI from needs my approval tab of supervisor level 2st
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.PutOnHoldButton));
		objWorkItemHomePage.waitForElementToBeClickable(apasGenericObj.closeButton, 3);
		objWorkItemHomePage.Click(apasGenericObj.closeButton);
		apasGenericObj.logout();
		Thread.sleep(5000);

		apasGenericObj.login(loginUser);  
		apasGenericObj.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);
		objWorkItemHomePage.openTab(objWorkItemHomePage.Tab_WorkItems_ON_parcel);
		// step 13: Verify 'Withdraw' Button should be present on Workitem timeline with Approval On Hold status on Level For level 1 approval
		objWorkItemHomePage.Click(objParcelsPage.ExpendWIOnParcels);
		softAssert.assertEquals(objParcelsPage.getFieldvalueFromWITimeLine("Status"),"Approval - On Hold","SMAB-T2480 :Verify Workitem status should be Approval -On Hold");
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.getButtonWithText(objParcelsPage.WithdrawButton)), "SMAB-T2480 :Verify 'Withdraw' Button should be present on Workitem timeline with 'Approval On Hold' status for Level 1 approval",true);
		apasGenericObj.logout();
	}


	/**
	 * This method is to Verify that Level1 Approver is able to assign WIs to a Level2 Approver and 2nd Level supervisor assigns it back to Level1 Approver
	 */
	@Test(description = "SMAB-T2558,SMAB-T2563: Verify that Level1 Approver is able to assign WIs to a Level2 Approver and 2nd Level supervisor assigns it back to Level1 Approver", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Regression","WorkItemAdministration"})
	public void WorkItemAdministration_Manual_verifyLevel2ApproverIsAbleToAssignWorkItems(String loginUser) throws Exception {

		ReportLogger.INFO("Get the user names through SOQL query");
		String rpBusinessAdminName = salesforceAPI.getUserName(users.RP_BUSINESS_ADMIN);
		String dataAdminName = salesforceAPI.getUserName(users.DATA_ADMIN);
		String appraisalSupportName = salesforceAPI.getUserName(users.APPRAISAL_SUPPORT);

		// fetching a parcel where PUC is not blank but Primary Situs is blank
		String queryAPNValue1 = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 2";
		HashMap<String, ArrayList<String>> response1 = salesforceAPI.select(queryAPNValue1);
		String apnValue1= response1.get("Name").get(1);

		// fetching a parcel where PUC and Primary Situs are not blank		
		String queryAPNValue2 = "select Name from Parcel__c where PUC_Code_Lookup__c!= null and Primary_Situs__c !=null AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response2 = salesforceAPI.select(queryAPNValue2);
		String apnValue2= response2.get("Name").get(0);

		String workItemCreationData1 =  testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData1 = objUtil.generateMapFromJsonFile(workItemCreationData1, "DOVSequencing");
		
		String workItemCreationData2 =  testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData2 = objUtil.generateMapFromJsonFile(workItemCreationData2,"DataToCreateWorkItemOfTypeRP");

		String warningMsgOnAssignLevel2Approver = "warning\nYou are already the 2nd Level approver on one or more of the selected work items. If you want to delegate 2nd Level approval, then select a different user.";

		// Step1: Login to the APAS application (RP Business Admin)
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the PARCELS page and search the first parcel
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue1);

		// Step3: Creating Manual work item with earlier DOV
		hashMapmanualWorkItemData1.put("DOV","11/21/2020");
		String workItem1 = objParcelsPage.createWorkItem(hashMapmanualWorkItemData1);
		objWorkItemHomePage.searchModule(WORK_ITEM);
		objWorkItemHomePage.globalSearchRecords(workItem1);

		// Step4: Change the status of WI to Submitted for Approval
		driver.navigate().refresh();
		Thread.sleep(5000);
		objPage.waitForElementToBeClickable(objWorkItemHomePage.submittedforApprovalTimeline, 10);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);

		//Step5: Verify the Status and Supervisor details in WI
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.wiStatusDetailsPage);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2563: Validate the status of Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),dataAdminName,
				"SMAB-T2563: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Current Approver on the Work Item");

		// Step6: Opening the PARCELS page and search the second parcel
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue2);

		// Step 7: Creating Manual work item for the Parcel 
		String workItem2 = objParcelsPage.createWorkItem(hashMapmanualWorkItemData2);
		objWorkItemHomePage.searchModule(WORK_ITEM);
		objWorkItemHomePage.globalSearchRecords(workItem2);

		// Step8: Change the status of WI to Submitted for Approval
		driver.navigate().refresh();
		Thread.sleep(5000);
		objPage.waitForElementToBeClickable(objWorkItemHomePage.submittedforApprovalTimeline, 10);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);

		//Step9: Verify the Status and Supervisor details in WI
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.wiStatusDetailsPage);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2563: Validate the status of Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),"",
				"SMAB-T2563: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Current Approver on the Work Item");

		//Step10: Click on the Main TAB - Home followed by Needs my Approval tab
		objWorkItemHomePage.searchModule(modules.HOME);
		ReportLogger.INFO("Click on the Main TAB - Home");
		objPage.Click(objWorkItemHomePage.lnkTABHome);
		ReportLogger.INFO("Click on the Sub  TAB - Work Items");
		objPage.Click(objWorkItemHomePage.lnkTABWorkItems);
		ReportLogger.INFO("Click on Needs My Approval TAB");
		objPage.Click(objWorkItemHomePage.needsMyApprovalTab);

		//Step11: Search for the Work Item and select the checkbox
		ReportLogger.INFO("Search for the Work Items and select the corresponding checkbox");
		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2);

		//Step12: Click on the Assign Level2 Supervisor button and validate the details
		ReportLogger.INFO("Click the Assign Level2 Supervisor button");
		objPage.javascriptClick(objPage.getButtonWithText(objWorkItemHomePage.assignLevel2Approver));	
		objWorkItemHomePage.searchAndSelectOptionFromDropDown(objWorkItemHomePage.wiLevel2ApproverDetailsPage, appraisalSupportName);

		String successMessage = objWorkItemHomePage.saveRecord();
		softAssert.assertEquals(successMessage,"success\nSuccess\nWork item(s) processed successfully!\nClose","SMAB-T2563 : Validate user is able to assign the WI" );
		objWorkItemHomePage.waitForElementToBeClickable(6, objPage.getButtonWithText(objWorkItemHomePage.assignLevel2Approver));

		//Step13: Verify the Status and Supervisor details in WIs
		ReportLogger.INFO("Verify the Status and Supervisor details in WIs");
		objWorkItemHomePage.searchModule(modules.WORK_ITEM);
		objWorkItemHomePage.globalSearchRecords(workItem1); 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2563: Validate the status of Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),appraisalSupportName,
				"SMAB-T2563: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Current Approver on the Work Item");

		objWorkItemHomePage.globalSearchRecords(workItem2); 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2563: Validate the status of Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),appraisalSupportName,
				"SMAB-T2563: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Current Approver on the Work Item");

		//Step14: Select & Approve the WI
		objWorkItemHomePage.searchModule(modules.HOME);
		ReportLogger.INFO("Click on the Main TAB - Home");
		objPage.Click(objWorkItemHomePage.lnkTABHome);
		ReportLogger.INFO("Click on the Sub  TAB - Work Items");
		objPage.Click(objWorkItemHomePage.lnkTABWorkItems);
		ReportLogger.INFO("Click on Needs My Approval TAB");
		objPage.Click(objWorkItemHomePage.needsMyApprovalTab);

		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2);

		ReportLogger.INFO("Click on the Approve button");
		objPage.javascriptClick(objWorkItemHomePage.btnApprove);
		softAssert.assertEquals(successMessage,"success\nSuccess\nWork item(s) processed successfully!\nClose","SMAB-T2563 : Validate user is able to approve the WI" );
		Thread.sleep(5000);
		softAssert.assertTrue(!objWorkItemHomePage.verifyElementExists(workItem1),
				"SMAB-T2563: Validate the First WI is not visible anymore");
		softAssert.assertTrue(!objWorkItemHomePage.verifyElementExists(workItem2),
				"SMAB-T2563: Validate the Second WI is not visible anymore");

		//Step15: Verify the Status and Supervisor details in WIs
		ReportLogger.INFO("Verify the Status and Supervisor details in WIs");
		objWorkItemHomePage.searchModule(modules.WORK_ITEM);
		objWorkItemHomePage.globalSearchRecords(workItem1); 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2563: Validate the status of Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),appraisalSupportName,
				"SMAB-T2563: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),appraisalSupportName,
				"SMAB-T2563: Validate the Current Approver on the Work Item");

		objWorkItemHomePage.globalSearchRecords(workItem2); 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2563: Validate the status of Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),appraisalSupportName,
				"SMAB-T2563: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),appraisalSupportName,
				"SMAB-T2563: Validate the Current Approver on the Work Item");

		//Step16: Logout from the application
		objWorkItemHomePage.logout();
		Thread.sleep(5000);

		//Step17: Login in the application
		objWorkItemHomePage.login(users.APPRAISAL_SUPPORT);

		//Step18: Opening the Work Item Module
		objWorkItemHomePage.searchModule(modules.HOME);

		//Step19: Click on the Main TAB - Home followed by Needs my Approval tab
		ReportLogger.INFO("Click on the Main TAB - Home");
		objPage.Click(objWorkItemHomePage.lnkTABHome);
		ReportLogger.INFO("Click on the Sub  TAB - Work Items");
		objPage.Click(objWorkItemHomePage.lnkTABWorkItems);
		ReportLogger.INFO("Click on Needs My Approval TAB");
		objPage.Click(objWorkItemHomePage.needsMyApprovalTab);
		ReportLogger.INFO("Search for the Work Item and select the checkbox");

		//Step20: Search for the Work Item and select the checkbox
		ReportLogger.INFO("Search for the Work Items and select the corresponding checkbox");
		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2);

		//Step21: Click on the Assign Level2 Supervisor button and validate the details
		ReportLogger.INFO("Work Items are assigned back to RP Business Admin");
		objPage.javascriptClick(objPage.getButtonWithText(objWorkItemHomePage.assignLevel2Approver));
		softAssert.assertEquals(objPage.getElementText(objWorkItemHomePage.warningOnAssignLevel2Approver),warningMsgOnAssignLevel2Approver,
				"SMAB-T2558: Validate the warning message is displayed on the pop-up screen");
		objWorkItemHomePage.searchAndSelectOptionFromDropDown(objWorkItemHomePage.wiLevel2ApproverDetailsPage, rpBusinessAdminName);

		successMessage = objWorkItemHomePage.saveRecord();
		softAssert.assertEquals(successMessage,"success\nSuccess\nWork item(s) processed successfully!\nClose","SMAB-T2563 : Validate user is able to assign the WI" );
		objWorkItemHomePage.waitForElementToBeClickable(6, objPage.getButtonWithText(objWorkItemHomePage.assignLevel2Approver));

		//Step22: Select & Approve the WI
		ReportLogger.INFO("Click on Needs My Approval TAB");
		objPage.Click(objWorkItemHomePage.needsMyApprovalTab);

		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2);

		ReportLogger.INFO("Click on the Approve button");
		objPage.javascriptClick(objWorkItemHomePage.btnApprove);
		softAssert.assertEquals(successMessage,"success\nSuccess\nWork item(s) processed successfully!\nClose","SMAB-T2563 : Validate user is able to approve the WI" );
		Thread.sleep(5000);
		softAssert.assertTrue(!objWorkItemHomePage.verifyElementExists(workItem1),
				"SMAB-T2563: Validate the First WI is not visible anymore");
		softAssert.assertTrue(!objWorkItemHomePage.verifyElementExists(workItem2),
				"SMAB-T2563: Validate the Second WI is not visible anymore");

		//Step23: Logout from the application
		objWorkItemHomePage.logout();
		Thread.sleep(5000);

		//Step24: Login in the application
		objWorkItemHomePage.login(loginUser);

		//Step25: Opening the Work Item Module
		objWorkItemHomePage.searchModule(modules.HOME);

		//Step26: Click on the Main TAB - Home followed by Needs my Approval tab
		ReportLogger.INFO("Click on the Main TAB - Home");
		objPage.Click(objWorkItemHomePage.lnkTABHome);
		ReportLogger.INFO("Click on the Sub  TAB - Work Items");
		objPage.Click(objWorkItemHomePage.lnkTABWorkItems);
		ReportLogger.INFO("Click on Needs My Approval TAB");
		objPage.Click(objWorkItemHomePage.needsMyApprovalTab);
		ReportLogger.INFO("Search for the Work Item and select the checkbox");

		//Step27: Search for the Work Item and select the checkbox
		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem1);
		objWorkItemHomePage.clickCheckBoxForSelectingWI(workItem2);

		//Step28: Verify the Status and Supervisor details in WI
		ReportLogger.INFO("Verify the Status and Supervisor details in WI");
		objWorkItemHomePage.searchModule(modules.WORK_ITEM);
		objWorkItemHomePage.globalSearchRecords(workItem1); 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2563: Validate the status of Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Current Approver on the Work Item");

		objWorkItemHomePage.globalSearchRecords(workItem2); 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiStatus, "Information"),"Submitted for Approval",
				"SMAB-T2563: Validate the status of Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiLevel2ApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Level2 Approver on the Work Item");

		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS(objWorkItemHomePage.wiCurrentApproverDetailsPage, "Approval & Supervisor Details"),rpBusinessAdminName,
				"SMAB-T2563: Validate the Current Approver on the Work Item");

		//Step29: Logout from the application
		objWorkItemHomePage.logout();

	} 
	/**
	 * This method is to Verify the WI generated for a WIC with roll code=SEC that does not have RA record gets assigned to In Pool TAB of Work Pool value of "RP Lost in Routing"
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2278,SMAB-T2280:Verify the WI generated for a WIC with roll code=SEC that does not have RA record gets assigned to In Pool TAB of Work Pool value of \"RP Lost in Routing\"", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression","WorkItemAdministration" })
	public void WorkItemAdministration_VerifyRPLostInRouting_WorkPool(String loginUser) throws Exception {

		// fetching an active parcel with no neighborhood record 
		String queryAPNValue = "select Name from Parcel__c where Status__c='Active' and Neighborhood_Reference__c= NULL limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String apnValue= response.get("Name").get(0);

		// fetching an already existing  neighborhood record in system
		String queryNeighborhoodValue = "SELECT Name FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
		String neighborhood=responseNeighborhoodDetails.get("Name").get(0);

		String workItemCreationData =  testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeBuildingPermitBurlingameFileUpload");

		String routingAssignmentCreationData = testdata.WORK_ITEMS_ROUTING_SETUP;
		Map<String, String> hashMapRoutingAssignmentData = objUtil.generateMapFromJsonFile(routingAssignmentCreationData,"DataToCreateRP_RoutingAssignment");
		String query = "SELECT Name,Id FROM Work_Item_Configuration__c WHERE Work_Item_Sub_Type__c = '"+hashMapRoutingAssignmentData.get("Work Item Sub Type")+"' and Work_Item_Type__c='"+hashMapRoutingAssignmentData.get("Work Item Type")+"'";
		HashMap<String, ArrayList<String>> responseWICDetails = salesforceAPI.select(query);
		String workItemCofiguration=responseWICDetails.get("Name").get(0);
		String workItemCofigurationId=responseWICDetails.get("Id").get(0);
		hashMapRoutingAssignmentData.put("Work Item Configuration", workItemCofiguration);
		hashMapRoutingAssignmentData.put("Neighborhood", neighborhood);

		//Delete routing Assignment for the WIC fetched above
		String routingAssignmentQuery = "SELECT Id FROM Routing_Assignment__c Where Configuration__c  = '"+workItemCofigurationId+"'";
		salesforceAPI.delete("Routing_Assignment__c",routingAssignmentQuery);

		// Step 1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objWorkItemHomePage.login(loginUser);

		/* Following steps cover the scenario :
		 * Parcel fetched is such that neighborhood value of parcel does not match with Neighborhood value of RA for particular WIC with roll code=SEC 
		 * RA is created for a WIC with roll code=SEC with some neighborhood value and a work item is created for a parcel
		 */
		
		//Step 2: Open the Routing Assignments Page 
		objWorkItemHomePage.searchModule(modules.ROUTING_ASSIGNMENTS);

		//Step 3:Create a new Routing Assignment for the WIC fetched above 
		objRoutingAssignmentPage.createRoutingAssignmentRecord(hashMapRoutingAssignmentData);

		// Step 4: Opening the PARCELS page  and searching  parcel
		objWorkItemHomePage.searchModule(PARCELS);
		objWorkItemHomePage.globalSearchRecords(apnValue);

		// Step 5: Creating Manual work item of  type Building Permit- Burlingame File Upload for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 6:Clicking the  details tab for the work item newly created and verifying that work pool is  - "RP Lost in Routing" 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"RP Lost in Routing",
				"SMAB-T2280: Validation that work pool name is RP Lost in Routing if no match found between  Neighborhood of Parcel and neighborhood value of RA");

		/* Following steps cover the scenario :
		 * When no RA exists for a WIC with roll code=SEC and a work item is created for a parcel
		 */
		
		//Step 7:Deleting the routing assignment record for the WIC fetched above
		salesforceAPI.delete("Routing_Assignment__c",routingAssignmentQuery);

		//Step 8: searching an active parcel
		objWorkItemHomePage.globalSearchRecords(apnValue);

		// Step 9: Creating Manual work item of  type Building Permit- Burlingame File Upload for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 10:Clicking the  details tab for the work item newly created and verifying that work pool is  - "RP Lost in Routing" 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"RP Lost in Routing",
				"SMAB-T2278: Validation that work pool name is RP Lost in Routing for WI generated for a WIC with roll code=SEC that does not have RA record");

		objWorkItemHomePage.logout();
	}

	/**
	 * This method is to Verify the WI generated for a WIC with roll code=UNS that does not have RA record gets assigned to In Pool TAB of Work Pool value of "BPP Lost in Routing"
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2284,SMAB-T2280:Verify the WI generated for a WIC with roll code=UNS that does not have RA record gets assigned to In Pool TAB of Work Pool value of \"BPP Lost in Routing\"", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"Regression","WorkItemAdministration" })
	public void WorkItemAdministration_VerifyBPPLostInRouting_WorkPool(String loginUser) throws Exception {

		// fetching a BPP account parcel with no territory record 
		String queryBPPAccount ="select Name from BPP_Account__c where Roll_Code__c='UNS' and Territory__c =NULL and Status__c ='ACTIVE' Limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryBPPAccount);
		String bppAccountValue= response.get("Name").get(0);

		// fetching an already existing  territory record in system
		String queryTerritoryValue = "SELECT Name FROM Territory__c where name!=NULL";
		HashMap<String, ArrayList<String>> responseTerritoryValueDetails = salesforceAPI.select(queryTerritoryValue);
		String territoryValue=responseTerritoryValueDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeBuildingPermitSanBrunoFileUpload");

		String routingAssignmentCreationData = testdata.WORK_ITEMS_ROUTING_SETUP;
		Map<String, String> hashMapRoutingAssignmentData = objUtil.generateMapFromJsonFile(routingAssignmentCreationData,"DataToCreateBPP_RoutingAssignment");
		String query = "SELECT Name,Id FROM Work_Item_Configuration__c WHERE Work_Item_Sub_Type__c = '"+hashMapRoutingAssignmentData.get("Work Item Sub Type")+"' and Work_Item_Type__c='"+hashMapRoutingAssignmentData.get("Work Item Type")+"'";
		HashMap<String, ArrayList<String>> responseWICDetails = salesforceAPI.select(query);
		String workItemCofiguration=responseWICDetails.get("Name").get(0);
		String workItemCofigurationId=responseWICDetails.get("Id").get(0);
		hashMapRoutingAssignmentData.put("Work Item Configuration", workItemCofiguration);
		hashMapRoutingAssignmentData.put("Territory", territoryValue);

		//Delete routing Assignment for the WIC fetched above
		String routingAssignmentQuery = "SELECT Id FROM Routing_Assignment__c Where Configuration__c  = '"+workItemCofigurationId+"'";
		salesforceAPI.delete("Routing_Assignment__c",routingAssignmentQuery);

		// Step 1: Login to the APAS application using the credentials passed through dataprovider (BPP Business Admin)
		objWorkItemHomePage.login(loginUser);

		/* Following steps cover the scenario :
		 * BPP account fetched is such that territory value of BPP account does not match with territory value of RA for particular WIC with roll code=UNS
		 * RA is created for a WIC with roll code=UNS with some territory value and a work item is created for a BPP account
		 */
		
		//Step 2: Open the Routing Assignments Page 
		objWorkItemHomePage.searchModule(modules.ROUTING_ASSIGNMENTS);

		//Step 3: Create a new Routing Assignment for the WIC fetched above 
		objRoutingAssignmentPage.createRoutingAssignmentRecord(hashMapRoutingAssignmentData);

		// Step 4: Opening the BPP accounts page  and searching  bpp account
		objWorkItemHomePage.searchModule(BPP_ACCOUNTS);
		objWorkItemHomePage.globalSearchRecords(bppAccountValue);

		// Step 5: Creating Manual work item of  type Building Permit- San Bruno File Upload for the bpp account 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 6:Clicking the  details tab for the work item newly created and verifying that work pool is  - "BPP Lost in Routing" 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"BPP Lost in Routing",
				"SMAB-T2280: Validation that work pool name is BPP Lost in Routing if no match found between  Territory of BPP Account and Territory value of RA");

		/* Following steps cover the scenario :
		 * When no RA exists for a WIC with roll code=UNS and a work item is created for a BPP Account
		 */
		
		//Step 7:Deleting the routing assignment record for the WIC fetched above
		salesforceAPI.delete("Routing_Assignment__c",routingAssignmentQuery);

		//Step 8: searching the BPP account
		objWorkItemHomePage.globalSearchRecords(bppAccountValue);

		// Step 9: Creating Manual work item of  type Building Permit- San Bruno File Upload for the bpp account 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 10:Clicking the  details tab for the work item newly created and verifying that work pool is  - "BPP Lost in Routing" 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"BPP Lost in Routing",
				"SMAB-T2284: Verify the WI generated for a WIC with roll code=UNS that does not have RA record gets assigned to In Pool TAB of Work Pool value of \"BPP Lost in Routing\"");

		objWorkItemHomePage.logout();
	}
	
	
}
