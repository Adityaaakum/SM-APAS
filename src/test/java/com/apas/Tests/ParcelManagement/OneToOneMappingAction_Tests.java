package com.apas.Tests.ParcelManagement;

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
import com.apas.PageObjects.MappingPage;
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

public class OneToOneMappingAction_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;
	
	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Page objPage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	ApasGenericPage apasGenericObj;
	MappingPage objMappingPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		apasGenericObj = new ApasGenericPage(driver);
		objParcelsPage = new ParcelsPage(driver);
		objPage = new Page(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		
	}
	/**
	 * This method is to Verify that User is able to perform a "One to One" mapping action for a Parcel (Active) from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2481:Verify that User is able to perform a One to One mapping action for a Parcel (Active) from a work item", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual" })
	public void ParcelManagement_VerifyOneToOneMappingAction(String loginUser) throws Exception {
		String puc;
		String primarySitus;		
		
		// fetching a parcel where PUC and Primary Situs are not blank		
		/*String queryAPNValue = "select Name from Parcel__c where PUC_Code_Lookup__c!= null and Situs__c !=null AND Status__c='Active' \r\n"
				+ "and Long_Legal_Description__c !=null\r\n"
				+ "and District__c !=null\r\n"
				+ "and Neighborhood__c !=null\r\n"
				+ "and TRA__c !=null limit 1;";*/
		//HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		//String apnValue= response.get("Name").get(0);
		String apnValue="102-271-320";
		
		String mappingActionCreationData = System.getProperty("user.dir") + testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingAllFieldsEntered");
		
		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objWorkItemHomePage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching a parcel where PUC and Primary Situs field (Street) have values saved
		//objWorkItemHomePage.searchModule(PARCELS);
		//objWorkItemHomePage.globalSearchRecords(apnValue);

		// Step 3: Creating Manual work item for the Parcel 
		//String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		objWorkItemHomePage.searchModule(modules.HOME);
        objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
        objWorkItemHomePage.openActionLink("WI-00000139");

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		//objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		//objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.referenceDetailsLabel);
		String parentWindow = driver.getWindowHandle();	
		//objWorkItemHomePage.openRelatedActionRecord(WINumber);
        objPage.switchToNewWindow(parentWindow);

        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));
        objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		
        //validation for apn numbers
  		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.editButton));
  		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, "1234");
  		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

  		//check error messages
  		
      //Step 5: Validating that default values of net land loss and net land gain is 0
      		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandLossTextBoxLabel),"value"),"0",
      				"SMAB-T2481: Validation that default value of net land loss  is 0");
      		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandGainTextBoxLabel),"value"),"0",
      				"SMAB-T2481: Validation that default value of net land gain  is 0");
      		
      		objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
      		
      		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
      				"SMAB-T2481: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");
      		      		
	
      		
            objMappingPage.performOneToOneMappingAction(hashMapOneToOneMappingData);

}}
