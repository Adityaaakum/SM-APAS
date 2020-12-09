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

public class DisabledVeterans_RetroFit_WorkItems_Test extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;

	Page objPage;
	LoginPage objLoginPage;
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

	@Test(description = "SMAB-T1888,SMAB-T1889,SMAB-T1933,SMAB-T1885,SMAB-T2087,SMAB-T2080,SMAB-T1993,SMAB-T1838,SMAB-T1881: Verify User is able to Claim the reminder Annual limit(RPSL) work item, enter annual limits and submit for supervisor approval, Verify user is able to access Work Item details after submitting it for approval", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class, groups = {
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
		objApasGenericPage.login(loginUser);

		// Step2: Opening the WI Home page
		objApasGenericPage.searchModule(modules.HOME);

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
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Roll Code", "Reference Data Details"),"SEC",
						"SMAB-T2080: Validation that 'Roll Code' fields getting automatically populated in the work item record");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,
						"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		
		// Step4: Now deleting the reminder WI, executing the Reminder job again and
		// verifying that new WI is linked with already existing RPSL--2021
		String deleteDVWIAgainQuery = "select id from Work_Item__c where Request_Type__c='Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits'";
		salesforceAPI.delete("Work_Item__c", deleteDVWIAgainQuery);
		salesforceAPI.generateReminderWorkItems(SalesforceAPI.REMINDER_WI_CODE_DV);
		 	
		driver.navigate().refresh();
		objApasGenericPage.searchModule(modules.HOME);
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
		objApasGenericPage.selectFromDropDown(rpslObj.statusDropDown, "Submitted for Approval");
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
		
		objPage.enter(rpslObj.dvLowIncomeExemptionAmountEditBox, "217910");
		objPage.enter(rpslObj.dvBasicIncomeExemptionAmountEditBox, "145273");
		objPage.enter(rpslObj.dvLowIncomeHouseholdLimitEditBox, "65337");
		objApasGenericPage.selectFromDropDown(rpslObj.statusDropDown, "Submitted for Approval");
		/*
		 * objPage.Click(rpslObj.saveButton); Thread.sleep(5000);
		 */
		objApasGenericPage.saveRecord();
		driver.close();
		driver.switchTo().window(parentwindow);

		// step5: now verifying the corresponding WI is also submitted
		ReportLogger.INFO("Now verifying that WI is also submitted upon submitting the corresponding RPSL");
		driver.navigate().refresh();
		Thread.sleep(5000);
		objPage.Click(workItemPageObj.detailsWI);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage, 5);
		
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Submitted for Approval","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		
		//Step6: Validate the field values after the Work Item is Submitted for Approval
		ReportLogger.INFO("User is able to access the Work Item details after submitting it for approval");
		String workPoolName = objApasGenericPage.getFieldValueFromAPAS("Work Pool", "Information");
		String supervisorName = objApasGenericPage.getFieldValueFromAPAS("Approver", "Approval & Supervisor Details");
		
		objPage.clickElementForFieldValue("Approver");
		String supervisorId = objApasGenericPage.getCurrentRecordId(driver,supervisorName);
		
		String supervisorNameQuery = "select Supervisor__c from Work_Pool__c where Name = '"+ workPoolName+ "' LIMIT 1";
		HashMap<String, ArrayList<String>> response = new SalesforceAPI().select(supervisorNameQuery);
        String supervisorIdFromDB = response.get("Supervisor__c").get(0);
        
        driver.navigate().back();
		Thread.sleep(2000);
		objPage.javascriptClick(workItemPageObj.detailsTab);
		objPage.scrollToBottom();
        
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Action", "Information"),"Update and Validate","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Related Action", "Information"),"Update and Validate","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Priority", "Information"),"None","SMAB-T1838: Validate user is able to view the value of 'Priority' field");
		softAssert.assertEquals(supervisorId,supervisorIdFromDB,"SMAB-T1838: Validate user is able to view the Id of Supervisor to verify 'Approver' field value");
		
		//Step7: Validate user who has submitted the Work Item for Approval is not able to Complete it
		ReportLogger.INFO("User is not able to approve the WI :: " + reminderWINumber);
		objPage.javascriptClick(workItemPageObj.completedTimeline);
		objPage.javascriptClick(workItemPageObj.markStatusCompleteBtn);
		softAssert.assertContains(objApasGenericPage.getAlertMessage(),"Status: You cannot change status from Submitted for Approval to Completed","SMAB-T1881 : Validate that after Work Item is submitted for approval, user is not able to mark it 'Complete' manually");
		objPage.Click(workItemPageObj.CloseErrorMsg);
		
		ReportLogger.INFO("Validate the status of the Work Item after closing the error message");
		Thread.sleep(1000);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage, 5);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Submitted for Approval","SMAB-T1881 : Validate the status of the Work Item");
		
		driver.navigate().back();
		driver.navigate().refresh();
		Thread.sleep(2000);
		
		//Step8: Validate WI details on the Home Page
		ReportLogger.INFO("Validate the Work Item details on the Home page");
		HashMap<String, ArrayList<String>> reminderSubmittedWI = workItemPageObj.getWorkItemData(workItemPageObj.TAB_MY_SUBMITTED_FOR_APPROVAL);
		int reminderSubmittedWIRowNumber = reminderSubmittedWI.get("Request Type").indexOf("Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits");
		String reminderSubmittedWINumber = reminderSubmittedWI.get("Work Item Number").get(reminderSubmittedWIRowNumber);
		
		softAssert.assertEquals(reminderSubmittedWI.get("Request Type").get(reminderSubmittedWIRowNumber),"Disabled Veterans - Update and Validate - Disabled veterans Yearly exemption amounts and income limits","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(reminderSubmittedWI.get("Work Pool Name").get(reminderSubmittedWIRowNumber),"Disabled Veterans","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(reminderSubmittedWINumber, reminderWINumber,"SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		softAssert.assertEquals(reminderSubmittedWI.get("Date").get(reminderSubmittedWIRowNumber),currentRollYear + "-01-01","SMAB-T1838: Validate user is able to view the date under 'Date' column");
		
		objApasGenericPage.logout();

	}

	/**
	 * below test case is for Annual limits Reminder WI verification and submission
	 * 
	 * @throws Exception
	 */

	@Test(description = "SMAB-T1921,SMAB-T1889,SMAB-T1919,SMAB-T1920,SMAB-T1867:Verify that once supervisor 'Return' the RPSL record then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' is also Returned and once RPSL approved then WI is also completed,Verify that Supervisor of a WI is able to edit the WI which is submitted for Approval ", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, dependsOnMethods = {
			"Disabledveteran_RPSLandReminderWIClaimSubmitValidations" }, groups = { "regression",
					"Work_Item_DV" }, alwaysRun = true)
	public void Disabledveteran_RPSLandReminderWIApprovalRejectionValidations(String loginUser) throws Exception {
		
		String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy");
		String currentRollYear=ExemptionsPage.determineRollYear(currentDate);
		
		// Step1: Login to the APAS application using the credentials passed through
		objApasGenericPage.login(loginUser);

		// Step2: Opening the WI Home page
		objApasGenericPage.searchModule("Home");

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
		 
		//Step4: Open the WI record and validate the details in the Detail tab
		ReportLogger.INFO("Opening the work item : " + reminderSubmittedWINumber);
		String xpath = "//a[@title='" + reminderSubmittedWINumber + "']";
		objPage.waitUntilElementIsPresent(xpath, 15);
		objPage.javascriptClick(driver.findElement(By.xpath(xpath)));
		Thread.sleep(3000);
		
		objPage.javascriptClick(workItemPageObj.detailsTab);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiActionDetailsPage),"Update and Validate","SMAB-T1867: Validate that Supervisor of a WI is able to view value for 'Action' field ");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.relatedActionLink),"Update and Validate","SMAB-T1867: Validate that Supervisor of a WI is able to view value for 'Related Action' field");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,"SMAB-T1867: Validate that 'Date' field is equal to 1/1/"+currentRollYear);
		
		//Step5: Edit the WI and update the details
		ReportLogger.INFO("Edit the WI and update the details : " + reminderSubmittedWINumber);
		objPage.javascriptClick(workItemPageObj.editBtn);
		objPage.enter("Value", "100");
		objPage.enter("Name", "Test");
		objPage.enter("DOV", "1/1/"+currentRollYear);
		objApasGenericPage.saveRecord();
		
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Value", "Reference Data Details"),"100.00",
				"SMAB-T1867: Validate that 'Value' field is updated with expected change");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Name", "Reference Data Details"),"Test",
				"SMAB-T1867: Validate that 'Name' field is updated with expected change");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("DOV", "Information"),"1/1/"+currentRollYear,
				"SMAB-T1867: Validate that 'DOV' field is equal to 1/1/"+currentRollYear);
		
		//Step6: Edit the WI again and remove the values entered before
		ReportLogger.INFO("Edit the WI again and remove the values entered before : " + reminderSubmittedWINumber);
		objPage.javascriptClick(workItemPageObj.editBtn);
		objPage.enter("Value", "");
		objPage.enter("Name", "");
		objPage.enter("DOV", "");
		objApasGenericPage.saveRecord();
		
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Value", "Reference Data Details"),"",
				"SMAB-T1867: Validate that 'Value' field is updated with expected change");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Name", "Reference Data Details"),"",
				"SMAB-T1867: Validate that 'Name' field is updated with expected change");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("DOV", "Information"),"",
				"SMAB-T1867: Validate that 'DOV' field is updated with expected change");
		
		
		//Step7: Change the status of RPSL
		objPage.javascriptClick(workItemPageObj.reviewLink);
		Thread.sleep(4000);
		objPage.waitUntilPageisReady(driver); 
		objPage.switchToNewWindow(parentwindow);
		objPage.javascriptClick(workItemPageObj.editBtn);
		//objPage.waitForElementToBeClickable(rpslObj.statusDropDown, 10);
		objApasGenericPage.selectFromDropDown(rpslObj.statusDropDown, "Returned by Approver");
		/*
		 * objPage.Click(rpslObj.saveButton); Thread.sleep(5000);
		 */
		objApasGenericPage.saveRecord();
		driver.close();
		driver.switchTo().window(parentwindow);
		driver.navigate().back();
		objApasGenericPage.globalSearchRecords(reminderSubmittedWINumber);

		// Step8:Supervisor Verifying that the WI also gets returned
		objPage.Click(workItemPageObj.detailsWI);
		//Thread.sleep(5000);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage, 10);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Returned","SMAB-T1921:Verify that once supervisor 'Rejects/Return' the exemption annual limits settings then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' should be returned back to 'Returned'");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline), "Returned","SMAB-T1921:Verify that once supervisor 'Rejects/Return' the exemption annual limits settings then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' should be returned back to 'Returned'");
		objApasGenericPage.logout();
		Thread.sleep(15000);

//******************************* staff member verifying WI status is returned and is under InProgress********************///////////////
		ReportLogger.INFO("Now logging in as staff member verifying the returned WI ststus and submitting the WI again");
		objApasGenericPage.login(EXEMPTION_SUPPORT_STAFF);
		objApasGenericPage.searchModule(modules.HOME);
		workItemPageObj.openWorkItem(reminderSubmittedWINumber);

		objPage.Click(workItemPageObj.detailsWI);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage,10);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Returned","SMAB-T1921:Verify that once supervisor 'Rejects/Return' the exemption annual limits settings then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' should be returned back to 'Returned'");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline), "Returned","SMAB-T1921:Verify that once supervisor 'Rejects/Return' the exemption annual limits settings then WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' should be returned back to 'Returned'");

		// step9: Now staff member submitting the WI again by submitting the RPSL
		 parentwindow = driver.getWindowHandle();
		objPage.Click(workItemPageObj.relatedActionLink);
		objPage.switchToNewWindow(parentwindow);
		// Thread.sleep(3000);
		objPage.javascriptClick(workItemPageObj.editBtn);
		//Thread.sleep(3000);
		//objPage.waitForElementToBeClickable(rpslObj.statusDropDown,10);
		objApasGenericPage.selectFromDropDown(rpslObj.statusDropDown, "Submitted for Approval");
		/*
		 * objPage.Click(rpslObj.saveButton); Thread.sleep(5000);
		 */
		objApasGenericPage.saveRecord();
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
		softAssert.assertEquals(reminderSubmittedWIAgain.get("Work Pool Name").get(reminderAgainSubmittedWIRowNumber),"Disabled veterans","SMAB-T1889:Verify that once user submits the exemption annual settings then work item 'Disabled Veterans Update and Validate Annual exemption amounts and income limits' also gets submitted to supervisor");
		
		objApasGenericPage.logout();

//******************** step10: Now supervisor will Approve the RPSl and verify the WI is Completed***************///

		ReportLogger.INFO("Now logging in as Superviosr and approving the RPSL and verifying the corresponding WI status");
		objApasGenericPage.login(loginUser);
		objApasGenericPage.searchModule(modules.HOME);
		objPage.Click(workItemPageObj.toggleBUtton);

		objPage.javascriptClick(workItemPageObj.needsMyApprovalTab);
		objPage.waitForElementToDisappear(objApasGenericPage.xpathSpinner, 10);
		workItemPageObj.openRelatedActionRecord(reminderAgainSubmittedWINumber);
		objPage.javascriptClick(workItemPageObj.editBtn);
		//Thread.sleep(3000);
		//objPage.waitForElementToBeClickable(rpslObj.statusDropDown, 10);
		objApasGenericPage.selectFromDropDown(rpslObj.statusDropDown, "Approved");
		/*
		 * objPage.Click(rpslObj.saveButton); Thread.sleep(5000);
		 */
		objApasGenericPage.saveRecord();
		driver.navigate().back();
		objApasGenericPage.globalSearchRecords(reminderAgainSubmittedWINumber);

		// Step11:Supervisor Verifying that the WI is completed
		objPage.Click(workItemPageObj.detailsWI);
		//Thread.sleep(5000);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage, 10);
		
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "Completed","SMAB-T1919:Verify that supervisor is able to approve the annual limits from WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits'");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline), "Completed","SMAB-T1919:Verify that supervisor is able to approve the annual limits from WI 'Disabled Veterans Update and Validate Annual exemption amounts and income limits'");
		objApasGenericPage.logout();
	}
	
	/**
	 * below test case is for Annual limits Reminder WI verification and submission
	 * 
	 * @throws Exception
	 */

	@Test(description = "SMAB-T2080,SMAB-T2091,SMAB-T1918:Verify system generates WI 'Disabled Veteran -Review and Update-Annual exemption amount verification' for all active Exemption with low income VA for previous roll year once 'Annual Exemption Limits' for current roll year is approved", dataProvider = "loginExemptionSupportStaff", dependsOnMethods = {
			"Disabledveteran_RPSLandReminderWIApprovalRejectionValidations" }, dataProviderClass = DataProviders.class, groups = {
					"regression", "Work_Item_DV" }, alwaysRun = true)
	public void Disabledveteran_LowIncomeExemptionWIVerification(String loginUser) throws Exception {

		String rollYear = "2020";
		String lowIncomeVaQuery = "select id from Value_Adjustments__c where Exemption_Status__c='Active' and Roll_Year__c='"+ rollYear + "' and Determination__c='Low-Income Disabled Veterans Exemption'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(lowIncomeVaQuery);
		int lowIncomeVACountInSystem = response.get("Id").size();
		String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy");
		String currentRollYear=ExemptionsPage.determineRollYear(currentDate);

		// Step1: Login to the APAS application using the credentials passed through
		objApasGenericPage.login(loginUser);

		// Step2: Opening the Exemption Module
		objApasGenericPage.searchModule(modules.HOME);

		// Step3: Navigating to In Pool section and verifying Low income exemption
		// amount verification WI's for all Active Exemptions
		
		HashMap<String, ArrayList<String>> InPoolLowIncomeWI = workItemPageObj.getWorkItemData(workItemPageObj.TAB_IN_POOL);
		int lowIncomeWiCount = (int) InPoolLowIncomeWI.get("Request Type").stream().filter(request -> request.equals("Disabled Veterans - Review and Update - Annual exemption amount verification")).count();
		ReportLogger.INFO("Total low income Annual Exemption verification amount WI::" + lowIncomeWiCount);
		int reminderWIRNumber = InPoolLowIncomeWI.get("Request Type").indexOf("Disabled Veterans - Review and Update - Annual exemption amount verification");
		String lowIncomeWIName = InPoolLowIncomeWI.get("Work Item Number").get(reminderWIRNumber);

		// step4: Now accepting and verifying a Low Income WI
		softAssert.assertEquals(lowIncomeWiCount, lowIncomeVACountInSystem,"Verifying number of WI are same as Low income VA presnet for Previous roll year ");
		ReportLogger.INFO("Accpeting a Low Income Annual Exemption WI :: " + lowIncomeWIName);
		objPage.scrollToBottom();
		workItemPageObj.acceptWorkItem(lowIncomeWIName);

		objPage.Click(workItemPageObj.inProgressTab);
		objPage.scrollToBottom();
		String parentwindow = driver.getWindowHandle();
		//SMAB-T2091 opening the action link to validate that link redirects to correct page
		workItemPageObj.openActionLink(lowIncomeWIName);
		objPage.switchToNewWindow(parentwindow);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.vaRollYear), "2021","SMAB-T2091: Verify that user is able to navigate to current roll year from action link");
		softAssert.assertTrue(objPage.verifyElementVisible(objValueAdjustmentPage.valueAdjustmentViewAll),
				"SMAB-T2091: Validation that Value Adjustments label is present");
		
		driver.close();
		driver.switchTo().window(parentwindow);	
		workItemPageObj.openWorkItem(lowIncomeWIName);
		objPage.javascriptClick(workItemPageObj.detailsTab);
		softAssert.assertTrue(objPage.verifyElementVisible(workItemPageObj.relatedActionLink),"SMAB-T1918:Verify that User is able to see the Low income WI under 'In Progress' tab after accpeting it");

		//Validating that 'Roll Code' field and 'Date' field gets automatically populated in the work item record
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Roll Code", "Reference Data Details"),"SEC",
								"SMAB-T2080: Validation that 'Roll Code' fields getting automatically populated in the work item record");
		softAssert.assertEquals(objApasGenericPage.getFieldValueFromAPAS("Date", "Information"),"1/1/"+currentRollYear,
								"SMAB-T2080: Validation that 'Date' fields is equal to 1/1/"+currentRollYear);		
		objPage.Click(workItemPageObj.detailsWI);
		//Thread.sleep(3000);
		objPage.waitForElementToBeClickable(workItemPageObj.wiStatusDetailsPage,10);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.wiStatusDetailsPage), "In Progress","SMAB-T1951: Verify that user is able to accept the WI and is able to see correct status of WI");
		 parentwindow = driver.getWindowHandle();
		objPage.Click(workItemPageObj.relatedActionLink);
		objPage.switchToNewWindow(parentwindow);
		// step4a: Verifying the WI was created for current Roll Year VA
		Thread.sleep(3000);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.vaRollYear), "2021","SMAB-T1918,SMAB-T1951: Verify that user is able to navigate to current roll year VA from corresponding WI");
		driver.switchTo().window(parentwindow);

		// step5: now Submitting the Work Item manually
		driver.navigate().refresh();
		Thread.sleep(3000);
		objPage.Click(workItemPageObj.detailsWI);
		//Thread.sleep(3000);
		objPage.waitForElementToBeClickable(workItemPageObj.submittedforApprovalTimeline, 10);
		ReportLogger.INFO("Submitting the WI and verifying the Status :: " + lowIncomeWIName);
		objPage.javascriptClick(workItemPageObj.submittedforApprovalTimeline);
		objPage.javascriptClick(workItemPageObj.markStatusCompleteBtn);

		softAssert.assertContains(objPage.getElementText(workItemPageObj.successAlert), "Status changed successfully","SMAB-T436: Validating the pop message on successful creation of City Strat Code entry from County Strat details page");
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1952:Verify that user is able to submit the Low Income WI manually from corresponding WI Home page");
		driver.navigate().refresh();
		driver.navigate().back();
		
		objPage.Click(workItemPageObj.lnkTABMySubmittedforApproval);
		objPage.waitForElementToDisappear(objApasGenericPage.xpathSpinner, 10);
		workItemPageObj.openWorkItem(lowIncomeWIName);
		objPage.javascriptClick(workItemPageObj.detailsTab);
		softAssert.assertEquals(objPage.getElementText(workItemPageObj.currenWIStatusonTimeline),"Submitted for Approval","SMAB-T1952:Verify that user is able to submit the Low Income WI manually from corresponding WI Home page");

		objApasGenericPage.logout();

	}
}