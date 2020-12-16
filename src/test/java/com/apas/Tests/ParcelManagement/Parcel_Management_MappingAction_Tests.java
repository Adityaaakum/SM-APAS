package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

public class Parcel_Management_MappingAction_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;

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
	 * This method is to Verify that User is able to perform a "One to One" mapping action for a Parcel (Active) from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2489,SMAB-T2482,SMAB-T2488,SMAB-T2486,SMAB-T2481:Verify that User is able to perform a One to One mapping action for a Parcel (Active) from a work item", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_VerifyOneToOneMappingAction(String loginUser) throws Exception {
		String activeParcelToPerformMapping="102-271-320";
		String activeParcelWithoutHyphen=activeParcelToPerformMapping.replace("-","");

		// fetching  parcel that is retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);
		// fetching  parcel that is In progress		
		queryAPNValue = "select Name from Parcel__c where Status__c='In Progress' limit 1";
		response = salesforceAPI.select(queryAPNValue);
		String inProgressAPNValue= response.get("Name").get(0);

		// fetching  all data of the active parcel to perform one to one mapping		

		queryAPNValue = "select PUC_Code_Lookup__c,Situs__c,Short_Legal_Description__c ,District__c, Neighborhood_Code__c  ,TRA__c from Parcel__c where Name='"+activeParcelToPerformMapping+"'";
		response = salesforceAPI.select(queryAPNValue);
		String useCode= response.get("PUC_Code_Lookup__c").get(0);
		String situs= response.get("Situs__c").get(0);
		String legalDescription= response.get("Short_Legal_Description__c").get(0);
		String district= response.get("District__c").get(0);
		String neighborhoodCode= response.get("Neighborhood_Code__c").get(0);
		String tra= response.get("TRA__c").get(0);

		String mappingActionCreationData = System.getProperty("user.dir") + testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingAllFieldsExceptNonCondoEntered");

		String workItemCreationData = System.getProperty("user.dir") + testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeParcelToPerformMapping);

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Validation that proper error message is displayed if parent parcel is retired
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.editButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageForParentParcels),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2489: Validation that proper error message is displayed if parent parcel is retired");

		//Step 6: Validation that proper error message is displayed if parent parcel is in progress
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.editButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageForParentParcels),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2489: Validation that proper error message is displayed if parent parcel is in progress status");

		//Step 7: Validation that User should be allowed to enter the 9 digit APN without the - sign

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.editButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, activeParcelWithoutHyphen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),activeParcelToPerformMapping,
				"SMAB-T2482: Validation that User should be allowed to enter the 9 digit APN without the \"-\"");

		//Step 8: Validating that default values of net land loss and net land gain is 0
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandLossTextBoxLabel),"value"),"0",
				"SMAB-T2481: Validation that default value of net land loss  is 0");
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandGainTextBoxLabel),"value"),"0",
				"SMAB-T2481: Validation that default value of net land gain  is 0");

		//Step 9: Validating help icons
		objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
				"SMAB-T2481: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");
		objMappingPage.Click(objMappingPage.helpIconLegalDescription);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank.",
				"SMAB-T2481: Validation that help text is generated on clicking the help icon for legal description");     		    
		objMappingPage.Click(objMappingPage.helpIconSitus);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent situs, leave as blank.",
				"SMAB-T2481: Validation that help text is generated on clicking the help icon for Situs text box");

		//Step 10: entering data in all fields for one to one mapping
		objMappingPage.fillOneToOneMappingActionForm(hashMapOneToOneMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 11: Verify that APNs generated must be 9-digits and should end in '0'
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		String childAPNComponents[] = childAPNNumber.split("-");
		softAssert.assertEquals(childAPNComponents.length,3,
				"SMAB-T2488: Validation that child APN number contains 3 parts: map book,map page,parcel number");
		softAssert.assertEquals(childAPNComponents[0].length(),3,
				"SMAB-T2488: Validation that MAP BOOK of child parcels contains 3 digits");
		softAssert.assertEquals(childAPNComponents[1].length(),3,
				"SMAB-T2488: Validation that MAP page of child parcels contains 3 digits");
		softAssert.assertEquals(childAPNComponents[2].length(),3,
				"SMAB-T2488: Validation that parcel number of child parcels contains 3 digits");
		softAssert.assertTrue(childAPNNumber.endsWith("0"),
				"SMAB-T2488: Validation that child APN number ends with 0");

		//Step 12: Validation of ALL fields THAT ARE displayed on second screen
		softAssert.assertEquals(gridDataHashMap.get("Neighborhood Code").get(0),neighborhoodCode,
				"SMAB-T2481: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),hashMapOneToOneMappingData.get("Reason code"),
				"SMAB-T2481: Validation that  System populates reason code from before screen");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescription,
				"SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");

		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),tra,
				"SMAB-T2481: Validation that  System populates TRA from the parent parcel");

		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),useCode,
				"SMAB-T2481: Validation that  System populates Use Code  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("District").get(0),district,
				"SMAB-T2481: Validation that  System populates District  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),situs,
				"SMAB-T2481: Validation that  System populates Situs  from the parent parcel");

		//Step 13 :Verify that User is able to to create a district, Use Code for the child parcel from the custom screen after performing one to one mapping action
		objMappingPage.waitForElementToBeClickable(objMappingPage.districtFieldInTable);
		objMappingPage.Click(objMappingPage.districtFieldInTable);
		objMappingPage.waitForElementToBeClickable(objMappingPage.ditrictFieldEditIcon);
		objMappingPage.Click(objMappingPage.ditrictFieldEditIcon);
		objMappingPage.enter(objMappingPage.districtEditTextBoxSecondScreenLabel, "Distrct 001");

		objMappingPage.Click(objMappingPage.useCodeFieldInTable);
		objMappingPage.waitForElementToBeClickable(objMappingPage.useCodeFieldEditIcon);
		objMappingPage.Click(objMappingPage.useCodeFieldEditIcon);
		objMappingPage.enter(objMappingPage.useCodeEditTextBoxSecondScreenLabel, "001vacant");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.oneToOneParcelButton));

		//Step 14: Validation of ALL fields THAT ARE displayed AFTER PARCEL ARE GENERATED
		softAssert.assertEquals(gridDataHashMap.get("Neighborhood Code").get(0),neighborhoodCode,
				"SMAB-T2481: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),hashMapOneToOneMappingData.get("Reason code"),
				"SMAB-T2481: Validation that  System populates reason code from before screen");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescription,
				"SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),tra,
				"SMAB-T2481: Validation that  System populates TRA from the parent parcel");

		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),"001vacant",
				"SMAB-T2486: Verify that User is able to to create a Use Code for the child parcel from the custom screen ");
		softAssert.assertEquals(gridDataHashMap.get("District").get(0),"Distrct 001",
				"SMAB-T2486: Verify that User is able to to create a  district for the child parcel from the custom screen ");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),situs,
				"SMAB-T2481: Validation that  System populates Situs  from the parent parcel");
		
	      objWorkItemHomePage.logout();

	}
}
