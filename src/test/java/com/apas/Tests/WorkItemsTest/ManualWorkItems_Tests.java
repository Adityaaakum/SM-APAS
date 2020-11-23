package com.apas.Tests.WorkItemsTest;

import java.util.Map;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
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
			"regression" })
	public void WorkItems_VerifyLinkedParcelUseCodeStreetFields(String loginUser) throws Exception {
		String apnValue = "002-011-040";
		//String workItemNumber;
		String puc;
		String primarySitus;		
		Map<String, String> workItemFieldsMap;
		
		// fetching a parcel where PUC and Primary Situs are not blank
		//String queryActiveAPN = "SELECT Name FROM Parcel__c where Status__c='Active' and PUC_Code_Lookup__r.name='99-RETIRED PARCEL' and Limit 1";
		//HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryActiveAPN);
		//String activeAPNName= response.get("Name").get(0);
		
		// fetching a parcel where PUC is not blank but  Primary Situs is blank
				//String queryActiveAPN = "SELECT Name FROM Parcel__c where Status__c='Active' and PUC_Code_Lookup__r.name='99-RETIRED PARCEL' and Limit 1";
				//HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryActiveAPN);
				//String activeAPNName= response.get("Name").get(0);
				
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objApasGenericFunctions.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC and Primary Situs field (Street) have values saved
		objApasGenericFunctions.searchModule(PARCELS);
		objApasGenericFunctions.globalSearchRecords(apnValue);

		// fetching the PUC and Primary Situs fields values of parcel
		puc = objApasGenericFunctions.getFieldValueFromAPAS("PUC", "Parcel Information");
		primarySitus = objApasGenericFunctions.getFieldValueFromAPAS("Primary Situs", "Parcel Information");

		// Step 3: Creating Manual work item for the Parcel 
		ReportLogger.INFO("Work item created is " + objParcelsPage.createWorkItem(objParcelsPage.getWorkItemCreationTestData()));
		//Step 4:Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(5000);

		//Step 5: Validating that 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(puc, objApasGenericFunctions.getFieldValueFromAPAS("Use Code", "Reference Data Details"),
				"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertTrue(primarySitus.contains(objApasGenericFunctions.getFieldValueFromAPAS("Street", "Reference Data Details")),
				"SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");

		// Scenario 2: searching a parcel where PUC is not blank but  Primary Situs is blank
		objApasGenericFunctions.globalSearchRecords(apnValue);

		// fetching the PUC and Primary Situs fields values of parcel
		puc = objApasGenericFunctions.getFieldValueFromAPAS("PUC", "Parcel Information");
		primarySitus = objApasGenericFunctions.getFieldValueFromAPAS("Primary Situs", "Parcel Information");

		// Creating Manual work item for the Parcel 
		workItemFieldsMap = objParcelsPage.getWorkItemCreationTestData();
		ReportLogger.INFO("Work item created is " + objParcelsPage.createWorkItem(workItemFieldsMap));

		//Clicking the  details tab for the work item newly created and fetching the use code and street fields values 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		Thread.sleep(5000);
		
		// Validating that 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel
		softAssert.assertEquals(puc, objApasGenericFunctions.getFieldValueFromAPAS("Use Code", "Reference Data Details"),
				"SMAB-T1994: Validation that 'Use Code' fields getting automatically populated in the work item record related to the linked Parcel");
		softAssert.assertTrue(primarySitus.contains(objApasGenericFunctions.getFieldValueFromAPAS("Street", "Reference Data Details")),
				"SMAB-T1994: Validation that 'Street' fields getting automatically populated in the work item record related to the linked Parcel");

		objApasGenericFunctions.logout();

	}

}
