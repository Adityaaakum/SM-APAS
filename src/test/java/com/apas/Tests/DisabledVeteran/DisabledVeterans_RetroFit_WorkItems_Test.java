package com.apas.Tests.DisabledVeteran;

import java.util.ArrayList;
import java.util.HashMap;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Reports.ReportLogger;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.RealPropertySettingsLibrariesPage;
import com.apas.PageObjects.ValueAdjustmentsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;

public class DisabledVeterans_RetroFit_WorkItems_Test extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;

	Page objPage;
	LoginPage objLoginPage;
	ApasGenericFunctions apasGenericObj;
	Util objUtil;
	SoftAssertion softAssert;
	String exemptionFilePath;
	ApasGenericPage objApasGenericPage;
	SalesforceAPI salesforceAPI;
	WorkItemHomePage workItemPageObj;
	BppTrendPage objBPPTrendPage;
	RealPropertySettingsLibrariesPage rpslObj;
	ValueAdjustmentsPage objValueAdjustmentPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		apasGenericObj = new ApasGenericFunctions(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		exemptionFilePath = System.getProperty("user.dir") + testdata.EXEMPTION_DATA;
		objApasGenericPage = new ApasGenericPage(driver);
		salesforceAPI = new SalesforceAPI();
		workItemPageObj = new WorkItemHomePage(driver);
		objBPPTrendPage = new BppTrendPage(driver);
		rpslObj = new RealPropertySettingsLibrariesPage(driver);
		objValueAdjustmentPage = new ValueAdjustmentsPage(driver);

	}

	/**
	 * below test case is for Annual limits Reminder WI verification and submission
	 * 
	 * @throws Exception
	 */

	@Test(description = "SMAB-T1888,SMAB-T1889,SMAB-T1933,SMAB-T1885,SMAB-T2087,SMAB-T2080,SMAB-T1993: Verify User is able to Claim the reminder Annual limit(RPSL) work item, enter annual limits and submit for supervisor approval", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class, groups = {
			"regression", "Work_Item_DV" }, alwaysRun = true)
	public void Disabledveteran_RPSLandReminderWIClaimSubmitValidations(String loginUser) throws Exception {
		// deleting Existing WI from Disabled veterans Work pool and 2021-RPSL from
		// system
		String deleteDVWIQuery = "select id from Work_Item__c where Request_Type__c in ('Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits','Disabled Veterans - Review and Update - Annual exemption amount verification')";
		salesforceAPI.delete("Work_Item__c", deleteDVWIQuery);

		String deleteRPSLQuery = "select id from Real_Property_Settings_Library__c where Roll_Year_Settings__r.name='2021'";
		salesforceAPI.delete("Real_Property_Settings_Library__c", deleteRPSLQuery);
		String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy");
		String currentRollYear=ExemptionsPage.determineRollYear(currentDate);

		// To run the reminder job WI creation query
		salesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_DV);

		// Step1: Login to the APAS application using the credentials passed through
		// data provider
		apasGenericObj.login(loginUser);

		// Step2: Opening the WI Home page
		apasGenericObj.searchModule(modules.HOME);

		// Step3: Navigating to In Pool section and verifying Linked record and
		// submitting linked record without accepting the WI
		   objPage.Click(workItemPageObj.inPoolTab);
		
		HashMap<String, ArrayList<String>> InPoolWorkItems = workItemPageObj.getWorkItemData(workItemPageObj.TAB_IN_POOL);
		
		int reminderDVWorkItemCount = (int) InPoolWorkItems.get("Request Type").stream().filter(request -> request.equals("Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits")).count();
		int reminderWIRowNumber = InPoolWorkItems.get("Request Type").indexOf("Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits");
		String reminderDVWINumber = InPoolWorkItems.get("Work Item Number").get(reminderWIRowNumber);

		softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(reminderWIRowNumber),"Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits","SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");
		softAssert.assertEquals(reminderDVWorkItemCount, 1, "DV Reminder WI count is 1");

		ReportLogger.INFO("verifying Current year RPSL record is created along with Reminder WI");
		workItemPageObj.openWorkItem(reminderDVWINumber);			
		objPage.waitForElementToBeClickable(workItemPageObj.linkedItemsRecord);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.linkedItemsRecord), "Exemption Limits - 2021","SMAB-T1885:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and RPSL for current roll year(if not present) upon job execution");

		//Clicking the  details tab for the work item newly created and fetching the RoLL code and Date Fields values
		workItemPageObj.Click(workItemPageObj.detailsTab);
		workItemPageObj.waitForElementToBeVisible(6, workItemPageObj.referenceDetailsLabel);

		//Validating that 'Roll Code' field and 'Date' field gets automatically populated in the work item record
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Roll Code", "Reference Data Details"),"SEC",
						"SMAB-T2080: Validation that 'Roll Code' fields getting automatically populated in the work item record");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,
						"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		
		// Step4: Now deleting the reminder WI, executing the Reminder job again and
		// verifying that new WI is linked with already existing RPSL--2021
		String deleteDVWIAgainQuery = "select id from Work_Item__c where Request_Type__c='Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits'";
		salesforceAPI.delete("Work_Item__c", deleteDVWIAgainQuery);
		salesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_DV);
		 	
		driver.navigate().refresh();
		apasGenericObj.searchModule(modules.HOME);
		objPage.Click(workItemPageObj.inPoolTab);

		// Step3: Now verifying the New reminder WI is linked with existing RPSL
		
		HashMap<String, ArrayList<String>> InPoolReminderWorkItems = workItemPageObj.getWorkItemData(workItemPageObj.TAB_IN_POOL);
		int reminderWorkItemCount = (int) InPoolReminderWorkItems.get("Request Type").stream().filter(request -> request.equals("Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits")).count();
		int reminderWIRNumber = InPoolReminderWorkItems.get("Request Type").indexOf("Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits");
		String reminderWINumber = InPoolReminderWorkItems.get("Work Item Number").get(reminderWIRNumber);

		softAssert.assertEquals(InPoolWorkItems.get("Request Type").get(reminderWIRNumber),"Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits","SMAB-T1993:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and existing RPSL for current roll year is linked to it upon job execution");
		softAssert.assertEquals(reminderWorkItemCount, 1, "DV Reminder WI count is 1");
		workItemPageObj.openWorkItem(reminderWINumber);
			
		objPage.waitForElementToBeClickable(workItemPageObj.detailsWI);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.linkedItemsRecord), "Exemption Limits - 2021","SMAB-T1993:Verify that reminder WI 'Disabled Veterans -Update and Validate -Disabled veterans Yearly exemption amounts and income limits' and existing RPSL for current roll year is linked to it upon job execution");
		objPage.javascriptClick(workItemPageObj.linkedItemsRecord);
		//Thread.sleep(3000);
		objPage.waitForElementToBeClickable(rpslObj.lowIncomeExemptionAmtDetails, 10);
		softAssert.assertEquals(objPage.getElementText(rpslObj.lowIncomeExemptionAmtDetails), "$1.00","SMAB-T2040:Verify current Roll Year RPSL is generated with default Amounts as $1 along with reminder WI generation");
		softAssert.assertEquals(objPage.getElementText(rpslObj.basicExemptionAmtDetails), "$1.00","SMAB-T2040:Verify current Roll Year RPSL is generated with default Amounts as $1 along with reminder WI generation");
		softAssert.assertEquals(objPage.getElementText(rpslObj.lowIncomeHouseholdLimitDetails), "$1.00","SMAB-T2040:Verify current Roll Year RPSL is generated with default Amounts as $1 along with reminder WI generation");
		ReportLogger.INFO("verifying valiadtion when user tries to submit the RPSL setting without accpeting the WI: "+ reminderWINumber);
		objPage.javascriptClick(workItemPageObj.editBtn);
		//Thread.sleep(3000);
		//objPage.waitForElementToBeClickable(rpslObj.statusDropDown, 10);
		apasGenericObj.selectFromDropDown(rpslObj.statusDropDown, "Submitted for Approval");
		/*
		 * objPage.Click(rpslObj.saveButton); Thread.sleep(3000);
		 */
		objPage.Click(workItemPageObj.saveButton);
		objPage.waitForElementToBeVisible(6, workItemPageObj.pageLevelErrorMsg);
		softAssert.assertContains(workItemPageObj.pageLevelErrorMsg.getText(), "Please accept the WorkItem :","SMAB-T1933:Verify that user is not able to submit the annual setting without claiming the WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits'");
		objPage.Click(workItemPageObj.cancelBtn);
		driver.navigate().back();
		driver.navigate().back();

		// Step4:Accepting the Work item and editing/submitting the linked record
		workItemPageObj.acceptWorkItem(reminderWINumber);
		objPage.Click(workItemPageObj.lnkTABInProgress);
		objPage.scrollToBottom();
		String parentwindow = driver.getWindowHandle();
		//SMAB-T2087 opening the action link to validate that link redirects to Exemption Limits Record Details page 
		workItemPageObj.openActionLink(reminderWINumber);
		objPage.switchToNewWindow(parentwindow);
		
		softAssert.assertTrue(objPage.verifyElementVisible(workItemPageObj.editBtn),
				"SMAB-T2087: Validation that edit button is visible");
		softAssert.assertTrue(objPage.verifyElementVisible(rpslObj.realPropertySettingsLibraryHeaderText),
				"SMAB-T2087: Validation that Real Property Settings Library label is visible");
		softAssert.assertTrue(objPage.verifyElementVisible(rpslObj.getRPSLRecord("2021")),
				"SMAB-T2087: Validation that Exemption Limits label is present");
		
		driver.close();
		driver.switchTo().window(parentwindow);
		 parentwindow = driver.getWindowHandle();
		workItemPageObj.openRelatedActionRecord(reminderWINumber);
		// step4a: editing the record and submitting it
		objPage.switchToNewWindow(parentwindow);
		objPage.javascriptClick(workItemPageObj.editBtn);
		//Thread.sleep(3000);
		//objPage.waitForElementToBeClickable(rpslObj.dvLowIncomeExemptionAmountEditBox, 10);
		objPage.enter(rpslObj.dvLowIncomeExemptionAmountEditBox, "217910");
		objPage.enter(rpslObj.dvBasicIncomeExemptionAmountEditBox, "145273");
		objPage.enter(rpslObj.dvLowIncomeHouseholdLimitEditBox, "65337");
		apasGenericObj.selectFromDropDown(rpslObj.statusDropDown, "Submitted for Approval");
		/*
		 * objPage.Click(rpslObj.saveButton); Thread.sleep(5000);
		 */
		apasGenericObj.saveRecord();
		driver.close();
		driver.switchTo().window(parentwindow);

		// step5: now verifying the corresponding WI is also submitted
		ReportLogger.INFO("Now verifying that WI is also submitted upon submitting the corresponding RPSL");
		driver.navigate().refresh();
		Thread.sleep(5000);
		objPage.Click(workItemPageObj.detailsWI);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage, 5);
		// Thread.sleep(5000);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Submitted for Approval","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		
		//5642
		
		String workPoolName = apasGenericObj.getFieldValueFromAPAS("Work Pool", "Information");
		String supervisorName = apasGenericObj.getFieldValueFromAPAS("Approver", "Approval & Supervisor Details");
		
		objPage.clickElementForFieldValue("Approver");
		Thread.sleep(1000);
		//objPage.Click(apasGenericObj.getFieldValueFromAPAS("Approver", "Approval & Supervisor Details"));
		String supervisorId = objApasGenericPage.getCurrentRecordId(driver,supervisorName);
		driver.navigate().back();
		Thread.sleep(2000);
		
		objPage.javascriptClick(workItemPageObj.detailsTab);
		objPage.scrollToBottom();
		
		//span[text()='Assigned To']/parent::div/following-sibling::div//div[@class='slds-grid']//a/span
		
		String supervisorNameQuery = "select Supervisor__c from Work_Pool__c where Name = '"+ workPoolName+ "' LIMIT 1";
		HashMap<String, ArrayList<String>> response = new SalesforceAPI().select(supervisorNameQuery);
        String supervisorIdFromDB = response.get("Supervisor__c").get(0);
        
        //String supervisorNameQuery = "SELECT Approver__c FROM Work_Item__c where Name = '"+ reminderWINumber +"'";
        //HashMap<String, ArrayList<String>> response = new SalesforceAPI().select(supervisorNameQuery);
        //String supervisorNameFromDB = response.get("Approver__c").get(0);
        
        
       // String query="SELECT Name FROM Work_Item__c WHERE Work_Pool__r.name='Disabled Veterans' and Status__c='In Progress' and Assigned_To__r.name!='rp appraiserAUT' limit 2";
		
        
        //query = "select Status__c from Work_Item__c where Name = '"+ importWorkItem +"'";
       // workItemData = new SalesforceAPI().select(query);
        //actualWIStatus = workItemData.get("Status__c").get(0);
      //HashMap<String, ArrayList<String>> response = salesforceAPI.select(supervisorNameQuery);
      		//String supervisorNameFromDB= response.get("Supervisor__c").get(0);
		
		System.out.println(workPoolName);
		System.out.println(supervisorName);
		System.out.println(supervisorIdFromDB);
		
		//softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiActionDetailsPage),"Update and Validate","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		//softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiRelatedActionDetailsPage),"Update and Validate","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Action", "Information"),"Update and Validate","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Related Action", "Information"),"Update and Validate","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Priority", "Information"),"None",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		softAssert.assertEquals(supervisorId,supervisorIdFromDB,
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		
		
		
		
		
		//5642
		ReportLogger.INFO("User is not able to approve the WI :: " + reminderWINumber);
		objPage.javascriptClick(workItemPageObj.completedTimeline);
		objPage.javascriptClick(workItemPageObj.markStatusCompleteBtn);
		
		//instaed of error message use Nikhil's method
		//softAssert.assertContains(objPage.getElementText(workItemPageObj.errormsgOnWI), "Status: You cannot change status from Submitted for Approval to Completed","SMAB-T436: Validating the pop message on successful creation of City Strat Code entry from County Strat details page");
		softAssert.assertContains(apasGenericObj.getAlertMessage(),"Status: You cannot change status from Submitted for Approval to Completed","SMAB-T2287 : Error validation You cannot change status from In Progress to Completed On WI page" );



		objPage.Click(workItemPageObj.closeErrorMsg);
		//driver.navigate().refresh();
		Thread.sleep(1000);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage, 5);
		// Thread.sleep(5000);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Submitted for Approval","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		//softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		
		
		
		
		
		driver.navigate().back();
		driver.navigate().refresh();
		Thread.sleep(2000);
		
		HashMap<String, ArrayList<String>> reminderSubmittedWI = workItemPageObj.getWorkItemData(workItemPageObj.TAB_MY_SUBMITTED_FOR_APPROVAL);
		int reminderSubmittedWIRowNumber = reminderSubmittedWI.get("Request Type").indexOf("Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits");
		String reminderSubmittedWINumber = reminderSubmittedWI.get("Work Item Number").get(reminderSubmittedWIRowNumber);
		
		
		softAssert.assertEquals(reminderSubmittedWI.get("Request Type").get(reminderSubmittedWIRowNumber),"Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(reminderSubmittedWI.get("Work Pool Name").get(reminderSubmittedWIRowNumber),"Disabled Veterans","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(reminderSubmittedWINumber, reminderWINumber,"SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		
		//5642
		softAssert.assertEquals(reminderSubmittedWI.get("Date").get(reminderSubmittedWIRowNumber),currentRollYear + "-01-01","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		
		//Go over the test case - 1 & 2
		
		
		
		
		apasGenericObj.logout();
	}

	/**
	 * below test case is for Annual limits Reminder WI verification and submission
	 * 
	 * @throws Exception
	 */

	@Test(description = "SMAB-T1921,SMAB-T1889,SMAB-T1919,SMAB-T1920:Verify that once supervisor 'Return' the RPSL record then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' is also Returned and once RPSL approved then WI is aloso completed", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, dependsOnMethods = {
			"Disabledveteran_RPSLandReminderWIClaimSubmitValidations" }, groups = { "regression",
					"Work_Item_DV" }, alwaysRun = true)
	public void Disabledveteran_RPSLandReminderWIApprovalRejectionValidations(String loginUser) throws Exception {
		
		String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy");
		String currentRollYear=ExemptionsPage.determineRollYear(currentDate);
		
		// Step1: Login to the APAS application using the credentials passed through
		apasGenericObj.login(loginUser);

		// Step2: Opening the WI Home page
		apasGenericObj.searchModule("Home");

		// Step3: Navigating to In 'Needs My Approval' section and verifying Linked
		// record and returning the linked record
		objPage.javascriptClick(workItemPageObj.toggleBUtton);
		HashMap<String, ArrayList<String>> needsMyApprovalWI = workItemPageObj.getWorkItemData(workItemPageObj.TAB_NEED_MY_APPROVAL);
		int reminderSubmittedWIRowNumber = needsMyApprovalWI.get("Request Type").indexOf("Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits");
		String reminderSubmittedWINumber = needsMyApprovalWI.get("Work Item Number").get(reminderSubmittedWIRowNumber);
		
		ReportLogger.INFO("Returning the RPSL setting and verifying the Wi status");
		
		String parentwindow = driver.getWindowHandle();
		//SMAB-T2087 opening the action link to validate that link redirects to correct page
		workItemPageObj.openActionLink(reminderSubmittedWINumber);
		objPage.switchToNewWindow(parentwindow);
		
		softAssert.assertTrue(objPage.verifyElementVisible(workItemPageObj.editBtn),
				"SMAB-T2087: Validation that edit button is visible");
		softAssert.assertTrue(objPage.verifyElementVisible(rpslObj.realPropertySettingsLibraryHeaderText),
				"SMAB-T2087: Validation that Real Property Settings Library label is visible");
		softAssert.assertTrue(objPage.verifyElementVisible(rpslObj.getRPSLRecord("2021")),
				"SMAB-T2087: Validation that Exemption Limits label is present");
		
		driver.close();
		driver.switchTo().window(parentwindow);
		parentwindow = driver.getWindowHandle();
		//workItemPageObj.openRelatedActionRecord(reminderSubmittedWINumber);
		 
		//5642
		ReportLogger.INFO("Opening the Related action window linked with work item : " + reminderSubmittedWINumber);
		String xpath = "//a[@title='" + reminderSubmittedWINumber + "']";
		objPage.waitUntilElementIsPresent(xpath, 15);
		
		objPage.javascriptClick(driver.findElement(By.xpath(xpath)));
		Thread.sleep(3000);
		
		objPage.javascriptClick(workItemPageObj.detailsTab);
		//objPage.scrollToBottom();
		
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiActionDetailsPage),"Update and Validate","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.relatedActionLink),"Update and Validate","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Roll Code", "Reference Data Details"),"SEC",
				"SMAB-T2080: Validation that 'Roll Code' fields getting automatically populated in the work item record");
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		
		//objPage.Click(workItemPageObj.editValueonDetailPage);
		objPage.javascriptClick(workItemPageObj.editBtn);
		objPage.enter("Value", "100");
		objPage.enter("Name", "Test");
		objPage.enter("DOV", "1/1/"+currentRollYear);
		apasGenericObj.saveRecord();
		
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Value", "Reference Data Details"),"100.00",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Name", "Reference Data Details"),"Test",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("DOV", "Information"),"1/1/"+currentRollYear,
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		
		objPage.javascriptClick(workItemPageObj.editBtn);
		objPage.enter("Value", "");
		objPage.enter("Name", "");
		objPage.enter("DOV", "");
		apasGenericObj.saveRecord();
		
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Value", "Reference Data Details"),"",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("Name", "Reference Data Details"),"",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		softAssert.assertEquals(apasGenericObj.getFieldValueFromAPAS("DOV", "Information"),"",
				"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		
		
		
		objPage.javascriptClick(workItemPageObj.reviewLink);
		Thread.sleep(4000);
		objPage.waitUntilPageisReady(driver); 
		
		
		
		objPage.switchToNewWindow(parentwindow);
		objPage.javascriptClick(workItemPageObj.editBtn);
		//objPage.waitForElementToBeClickable(rpslObj.statusDropDown, 10);
		apasGenericObj.selectFromDropDown(rpslObj.statusDropDown, "Returned by Approver");
		/*
		 * objPage.Click(rpslObj.saveButton); Thread.sleep(5000);
		 */
		apasGenericObj.saveRecord();
		driver.close();
		driver.switchTo().window(parentwindow);
		driver.navigate().back();
		apasGenericObj.globalSearchRecords(reminderSubmittedWINumber);

		// Step4:Supervisor Verifying that the WI also gets returned
		objPage.Click(workItemPageObj.detailsWI);
		//Thread.sleep(5000);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage, 10);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Returned","SMAB-T1921:Verify that once supervisor 'Rejects/Return' the exemption annual limits settings then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' should be returned back to 'Returned'");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline), "Returned","SMAB-T1921:Verify that once supervisor 'Rejects/Return' the exemption annual limits settings then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' should be returned back to 'Returned'");
		apasGenericObj.logout();
		Thread.sleep(15000);

//******************************* staff member verifying WI status is returned and is under InProgress********************///////////////
		ReportLogger.INFO("Now logging in as staff member verifying the returned WI ststus and submitting the WI again");
		apasGenericObj.login(EXEMPTION_SUPPORT_STAFF);
		apasGenericObj.searchModule(modules.HOME);
		workItemPageObj.openWorkItem(reminderSubmittedWINumber);

		objPage.Click(workItemPageObj.detailsWI);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage,10);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Returned","SMAB-T1921:Verify that once supervisor 'Rejects/Return' the exemption annual limits settings then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' should be returned back to 'Returned'");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline), "Returned","SMAB-T1921:Verify that once supervisor 'Rejects/Return' the exemption annual limits settings then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' should be returned back to 'Returned'");

		// step6: Now staff member submitting the WI again by submitting the RPSL
		 parentwindow = driver.getWindowHandle();
		objPage.Click(workItemPageObj.relatedActionLink);
		objPage.switchToNewWindow(parentwindow);
		// Thread.sleep(3000);
		objPage.javascriptClick(workItemPageObj.editBtn);
		//Thread.sleep(3000);
		//objPage.waitForElementToBeClickable(rpslObj.statusDropDown,10);
		apasGenericObj.selectFromDropDown(rpslObj.statusDropDown, "Submitted for Approval");
		/*
		 * objPage.Click(rpslObj.saveButton); Thread.sleep(5000);
		 */
		apasGenericObj.saveRecord();
		driver.switchTo().window(parentwindow);
		driver.navigate().refresh();
		objPage.Click(workItemPageObj.detailsWI);
		//Thread.sleep(5000);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage,10);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Submitted for Approval","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		driver.navigate().back();
		driver.navigate().refresh();
		
		HashMap<String, ArrayList<String>> reminderSubmittedWIAgain = workItemPageObj.getWorkItemData(workItemPageObj.TAB_MY_SUBMITTED_FOR_APPROVAL);

		int reminderAgainSubmittedWIRowNumber = reminderSubmittedWIAgain.get("Request Type").indexOf("Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits");
		String reminderAgainSubmittedWINumber = reminderSubmittedWIAgain.get("Work Item Number").get(reminderAgainSubmittedWIRowNumber);

		softAssert.assertEquals(reminderSubmittedWIAgain.get("Request Type").get(reminderAgainSubmittedWIRowNumber),"Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(reminderSubmittedWIAgain.get("Work Pool Name").get(reminderAgainSubmittedWIRowNumber),"Disabled Veterans","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		apasGenericObj.logout();

//******************** step7: Now supervisor will Approve the RPSl and verify the WI is Completed***************///

		ReportLogger.INFO("Now logging in as Superviosr and approving the RPSL and verifying the corresponding WI status");
		apasGenericObj.login(loginUser);
		apasGenericObj.searchModule(modules.HOME);
		objPage.Click(workItemPageObj.toggleBUtton);

		objPage.javascriptClick(workItemPageObj.needsMyApprovalTab);
		objPage.waitForElementToDisappear(objApasGenericPage.xpathSpinner, 10);
		workItemPageObj.openRelatedActionRecord(reminderAgainSubmittedWINumber);
		objPage.javascriptClick(workItemPageObj.editBtn);
		//Thread.sleep(3000);
		//objPage.waitForElementToBeClickable(rpslObj.statusDropDown, 10);
		apasGenericObj.selectFromDropDown(rpslObj.statusDropDown, "Approved");
		/*
		 * objPage.Click(rpslObj.saveButton); Thread.sleep(5000);
		 */
		apasGenericObj.saveRecord();
		driver.navigate().back();
		apasGenericObj.globalSearchRecords(reminderAgainSubmittedWINumber);

		// Step8:Supervisor Verifying that the WI is completed
		objPage.Click(workItemPageObj.detailsWI);
		//Thread.sleep(5000);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage, 10);
		
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Completed","SMAB-T1919:Verify that supervisor is able to approve the annual limits from WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits'");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline), "Completed","SMAB-T1919:Verify that supervisor is able to approve the annual limits from WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits'");
		apasGenericObj.logout();
	}

}