package com.apas.Tests.SecurityAndSharing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
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

public class ParcelManagement_SecurityAndSharing_Test extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonParcelObject= new JSONObject();


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);

	}
	
	/**
	 * Verify that other than mapping user should not access the mapping custon screen via action link on work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2468 : Verify that other than mapping user should not access the mapping custon screen via action link on work item", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_AccessValidation_MappingCustomScreen(String loginUser) throws Exception {
		
		//Fetching Active parcel
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL AND Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		softAssert.assertTrue(!objParcelsPage.verifyElementVisible(objParcelsPage.getButtonWithText(objParcelsPage.CreateNewParcelButton)),"SMAB- : Create new button should not be visible to other users");
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.reviewLink);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		Thread.sleep(2000); 		//Wait till the Mapping action screen loads in another browser tab
		String parentWindow = driver.getWindowHandle();
		ReportLogger.INFO("Switch to the Mapping Action screen");
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.MappingScreenError),"Only mapping users have access to perform mapping related actions"
				,"SMAB-T2468: Only mapping users have access to perform mapping related actions");
		objParcelsPage.logout();
	}
	/**
	 * This method is to validate system admin and mapping user should create new APN and edit apn 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2469, SMAB-T2464, SMAB-T2465: Verify system admin and mapping user should be able to create and update parcel", dataProvider = "loginSystemAdminAndMappingStaffAndMappingSupervisor", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_AccessValidationOnCreationAndEdition(String loginUser) throws Exception {
		
		String ParcelCreationData = testdata.MANUAL_PARCEL_CREATION_DATA;
		Map<String, String> hashMapManualParcelData = objUtil.generateMapFromJsonFile(ParcelCreationData,
				"DataToCreateParcel");
		String APN = hashMapManualParcelData.get("APN");
		System.out.println("print APN "+APN);
		String ParcelNumber = hashMapManualParcelData.get("Parcel Number");
		//Fetching parcel and delete the parcel
		String queryAPN = "Select Id  From Parcel__c where name='"+APN+"'";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apnId=responseAPNDetails.get("Id").get(0);
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c limit 1");
		String ActivePUC=responsePUCDetails.get("Name").get(0);
		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
		String TRAValue = salesforceAPI.select(queryTRAValue).get("Name").get(0);
		if(apnId!="" ||apnId!=null) {
			salesforceAPI.delete("Parcel__c",queryAPN);}
						
		// Step1: Login to the APAS application using the credentials passed through dataprovider 
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page
		objMappingPage.searchModule(PARCELS);
		//Steps3: verify new button on parcel page
		softAssert.assertTrue(objParcelsPage.verifyElementVisible(objParcelsPage.getButtonWithText(objParcelsPage.CreateNewParcelButton)),"SMAB-T2469: Create new button should not be visible to other users");
		
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.CreateNewParcelButton));
		
		objParcelsPage.enter("APN", APN);
		objParcelsPage.searchAndSelectOptionFromDropDown("PUC", ActivePUC);
		objParcelsPage.enter("Parcel Number", ParcelNumber);
		objParcelsPage.Click(objParcelsPage.saveButton);
		Thread.sleep(5000);
        objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.EditParcelButton));		
		objParcelsPage.enter("Short Legal Description", "short legal description testing");
		objParcelsPage.searchAndSelectOptionFromDropDown("TRA", TRAValue);
		objParcelsPage.Click(objParcelsPage.saveButton);
		softAssert.assertEquals(objMappingPage.getElementText(objParcelsPage.getparcel),APN
				,"SMAB-T2469, SMAB-T2464, SMAB-T2465: Only mapping users have access to perform mapping related actions");
		objParcelsPage.logout();
		
	}
	
	@Test(description = "SMAB-T2463: Validate RP Business admin and Exemption Support staff should not able to delete parcel", groups = {"Regression","ParcelManagement"}, dataProvider = "loginRpBusinessAdminAndExemptionSupportUsers", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void ParcelManagement_ValidationOnDeleteRecord(String loginUser) throws Exception {
		String queryAPN = "Select Name From Parcel__c limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		//Step1: Login to the APAS application using the user passed through the data provider
		objParcelsPage.login(loginUser);
		
		//Step2: Open the Roll Year Settings module
		objParcelsPage.searchModule(modules.PARCELS);
		
		//Step3: Select ALL from the List view and Search the Roll Year record
		Thread.sleep(1000);
		objParcelsPage.displayRecords("All Active Parcels");
		objParcelsPage.searchRecords(apn);
		
		//Step4: Search the existing Roll Year record - Delete/Edit options should not be visbile to non-admin users
		softAssert.assertTrue(!objParcelsPage.clickShowMoreButtonAndAct(apn, "Delete"),"SMAB-T2463: Validate non system admin user is not able to view 'Delete' option to delete the existing parcel record");
				
		objParcelsPage.logout();
	}
	
}
