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

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objLoginPage = new LoginPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objParcelsPage = new ParcelsPage(driver);
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
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC and Primary Situs field (Street) have values saved
		objApasGenericFunctions.searchModule(PARCELS);
		objApasGenericFunctions.globalSearchRecords(apnValue);

		// fetching the PUC and Primary Situs fields values of parcel
		puc = objApasGenericFunctions.getFieldValueFromAPAS("PUC", "Parcel Information");
		primarySitus = objApasGenericFunctions.getFieldValueFromAPAS("Primary Situs", "Parcel Information");

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);

		//Step 5: Validating that 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Use Code", "Reference Data Details"),puc,
				"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertTrue(primarySitus.contains(objApasGenericFunctions.getFieldValueFromAPAS("Street", "Reference Data Details")),
				"SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");

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
	 * This method is to verify that user gets prior date of value sequencing restriction warning message
	 */
	@Test(description = "SMAB-T2219: Verify that user gets the warning message when trying to accept the work item with prior DOV", dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, groups = {"regression","work_item_manual"})
	public void WorkItems_PriorDateOfValueSequencing(String loginUser) throws Exception {

		// fetching a parcel where PUC is not blank but  Primary Situs is blank
		String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String apnValue= response.get("Name").get(0);

		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData, "DOVSequencing");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC is blank but Primary Situs field (Street) is not blank
		objApasGenericFunctions.searchModule(PARCELS);
		objApasGenericFunctions.globalSearchRecords(apnValue);

		// Step3: Creating Manual work item with earlier DOV
		hashMapmanualWorkItemData.put("DOV","11/21/2020");
		String workItemWithEarlierDOV = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		// Step4: Creating Manual work item with Later DOV
		hashMapmanualWorkItemData.put("DOV","11/22/2020");
		String workItemWithLaterDOV = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step5:Accept the work items created above
		objApasGenericFunctions.searchModule(HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
		Thread.sleep(3000);

		objWorkItemHomePage.acceptWorkItem(workItemWithEarlierDOV);
		objWorkItemHomePage.acceptWorkItem(workItemWithLaterDOV);

		//Step 5: DOV Sequencing warning message
		String actualDOVSequencingWarningMessage = objApasGenericFunctions.getAlertMessage();
		String expectedDOVSequencingWarningMessage = "Other work items exist for this Parcel with an earlier DOV. Please work those Work Items before this one.";
		softAssert.assertEquals(actualDOVSequencingWarningMessage,expectedDOVSequencingWarningMessage,"SMAB-T2219: Validation for DOV Sequencing warning message");

		objApasGenericFunctions.logout();
	}

	/**
	 * This method is to verify that user gets prior date of value sequencing restriction warning message
	 */
	@Test(description = "SMAB-T1985,SMAB-T1989,SMAB-T1991: Verify that work items are routed correctly as per the work item routing drop down", dataProvider = "loginApraisalUser", dataProviderClass = DataProviders.class, groups = {"regression","work_item_manual"})
	public void WorkItems_ManualWorkItemRouting(String loginUser) throws Exception {

		// fetching a parcel where PUC is not blank but  Primary Situs is blank
		String queryAPNValue = "select Name from Parcel__c where puc_code_lookup__c != NULL and primary_situs__c = NULL and Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String apnValue= response.get("Name").get(0);

		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;

		// Step1: Login to the APAS application using the credentials passed through data provider
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the PARCELS page and searching a parcel
		objApasGenericFunctions.searchModule(PARCELS);
		objApasGenericFunctions.globalSearchRecords(apnValue);

		// Step3: Creating Manual work item with work item routing as "Give Work Item to Someone Else"
		Map<String, String> hashMapGiveWorkItemToSomeoneElse = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToSomeoneElse");
		String workItemAssignedToSomeoneElse = objParcelsPage.createWorkItem(hashMapGiveWorkItemToSomeoneElse);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objParcelsPage.componentActionsButtonText),"SMAB-T1991 : Validation that Parcel screen is displayed when work item routing option is selected as 'Give Work Item to Someone Else'");

		// Step4: Creating Manual work item with work item routing as "Give Work Item to Default Work Pool"
		Map<String, String> hashMapGiveWorkItemToDefaultWorkPool = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingToDefaultPool");
		String workItemDefaultToWorkPool = objParcelsPage.createWorkItem(hashMapGiveWorkItemToDefaultWorkPool);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objParcelsPage.componentActionsButtonText),"SMAB-T1989 : Validation that Parcel screen is displayed when work item routing option is selected as 'Give Work Item to Default Work Pool'");

		// Step5: Creating Manual work item with work item routing as "Give Work Item to Me"
		Map<String, String> hashMapGiveWorkItemToMe = objUtil.generateMapFromJsonFile(workItemCreationData, "WorkItemRoutingGiveToMe");
		objParcelsPage.createWorkItem(hashMapGiveWorkItemToMe);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.markStatusAsCompleteButton),"SMAB-T1985 : Validation that Parcel screen is displayed when work item routing option is selected as 'Give Work Item to Me'");
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(3000);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Me'");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To"),"appraisal supportAUT","SMAB-T1985: Validation that newly created work item is assigned to the logged in user");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("DOV"),hashMapGiveWorkItemToMe.get("DOV"),"SMAB-T1804: Validation that DOV is reflected correctly");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Due Date"),hashMapGiveWorkItemToMe.get("Due Date"),"SMAB-T1804: Validation that Due Date is reflected correctly");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Approver"),"RP Admin","SMAB-T1804: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Level2 Approver"),"RP Admin","SMAB-T1804: Validation that newly created work item having the correct level2 approver");

		// Step6: Opening the Home Page
		objApasGenericFunctions.searchModule(HOME);

		// Step7: Validation of the work item created for default work pool
		objApasGenericFunctions.globalSearchRecords(workItemDefaultToWorkPool);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(3000);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Default Work Pool'");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To"),"","SMAB-T1989: Validation that newly created work item is not assigned to any work user");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool"),"RP Admin","SMAB-T1989: Validation that newly created work item is in the default work pool");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Approver"),"RP Admin","SMAB-T1989: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Level2 Approver"),"RP Admin","SMAB-T1989: Validation that newly created work item having the correct level2 approver");

		// Step7: Validation of the work item created for Someone Else
		objApasGenericFunctions.globalSearchRecords(workItemAssignedToSomeoneElse);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(3000);
		ReportLogger.INFO("Validations when work item routing is selected as 'Give Work Item To Someone Else'");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Assigned To"),hashMapGiveWorkItemToSomeoneElse.get("Work Item Owner"),"SMAB-T1991: Validation that newly created work item is assigned to the selected user in work item routing");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Approver"),"RP Admin","SMAB-T1991: Validation that newly created work item having the correct approver");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Level2 Approver"),"RP Admin","SMAB-T1991: Validation that newly created work item having the correct level2 approver");
		softAssert.assertEquals(objApasGenericFunctions.getFieldValueFromAPAS("Work Pool"),"RP Admin","SMAB-T1991: Validation that newly created work item is in the correct work pool");

		//Logging off the APAS
		objApasGenericFunctions.logout();
	}


}
