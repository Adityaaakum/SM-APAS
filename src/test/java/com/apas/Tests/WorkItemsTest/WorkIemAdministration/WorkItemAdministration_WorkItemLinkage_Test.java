package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class WorkItemAdministration_WorkItemLinkage_Test extends TestBase implements testdata, modules, users {
	RemoteWebDriver driver;
	
	ParcelsPage objParcelsPage;
    WorkItemHomePage objWorkItemHomePage;
    MappingPage objMappingPage;
    Util objUtil = new Util();
    SoftAssertion softAssert = new SoftAssertion();
    SalesforceAPI salesforceAPI = new SalesforceAPI();

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod() throws Exception {

        driver = null;
        setupTest();
        driver = BrowserDriver.getBrowserInstance();
        objMappingPage= new MappingPage(driver);
        objWorkItemHomePage = new WorkItemHomePage(driver);
        objParcelsPage = new ParcelsPage(driver);
    }

    @Test(description = "SMAB-T2658: Verify APN field when there is only parcel record are added to WI via Linkages", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
            "Regression", "WorkItemAdministration" }, alwaysRun = true)
    public void WorkItemAdministration_VerifyAPNColumn(String loginUser) throws Exception {
    	//Fetch Active APN
    	String apn = objMappingPage.fetchActiveAPN();
    	
    	//Setup data to create Manual WI
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		
		// Step 4: Creating Manual work item for the Parcel
		String apnOnWIDetailsTab = objWorkItemHomePage.getFieldValueFromAPAS("APN");
		String remarks = objWorkItemHomePage.getFieldValueFromAPAS("Remarks");
		softAssert.assertEquals(apnOnWIDetailsTab, apn, "SMAB-T2658: Verify APN column displays parcel number");
    
		// Step 5: Navigate to Home Page and verify APN Column
    	objMappingPage.searchModule(HOME);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInProgress);
		Thread.sleep(2000);
		WebElement WI = objWorkItemHomePage.searchWIinGrid(WINumber);
		
		if(objWorkItemHomePage.verifyElementVisible(WI)) {
			HashMap<String, ArrayList<String>> hm = objMappingPage.getGridDataInHashMap();
			WINumber = WINumber + "\n" + "Launch Data Entry Screen";
			System.out.println("Keyset: "+hm.keySet());
			System.out.println("index of "+WINumber +": "+hm.get("Work item #").indexOf(WINumber));
			System.out.println("apn: "+hm.get("APN").get(hm.get("Work item #").indexOf(WINumber)));
			//softAssert.assertContains(objMappingPage.getGridDataInHashMap().get("APN").get(0), apn, "SMAB-T2658: APN Number Validation for Work Item");

		}else		
			softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(WI), "SMAB-T2658: Not able to find Work Item on Home Page: "+WINumber);


    }
}
