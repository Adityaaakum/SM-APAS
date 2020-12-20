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
		String activeParcelToPerformMapping="002-023-190";

		// fetching  all data of the active parcel to perform one to one mapping		

		String queryAPNDetails = "select PUC_Code_Lookup__c,Primary_Situs__c ,Short_Legal_Description__c ,District__c, Neighborhood_Reference__c ,TRA__c from Parcel__c where Name='"+activeParcelToPerformMapping+"'";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNDetails);

		String queryNeighborhoodValue = "SELECT Name  FROM Neighborhood__c Name where id='"+responseAPNDetails.get("Neighborhood_Reference__c").get(0)+"'";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryNeighborhoodValue);
		String neighborhoodValue= response.get("Name").get(0);

		String queryPUCValue = "SELECT Name  FROM PUC_Code__c Name where id='"+responseAPNDetails.get("PUC_Code_Lookup__c").get(0)+"'";
		response = salesforceAPI.select(queryPUCValue);
		String pucValue= response.get("Name").get(0);

		String queryTRAValue = "SELECT Name  FROM TRA__c Name where id='"+responseAPNDetails.get("TRA__c").get(0)+"'";
		response = salesforceAPI.select(queryTRAValue);
		String traValue= response.get("Name").get(0);

		String queryPrimarySitusValue = "SELECT Name  FROM Situs__c Name where id='"+responseAPNDetails.get("Primary_Situs__c").get(0)+"'";
		response = salesforceAPI.select(queryPrimarySitusValue);
		String primarySitusValue= response.get("Name").get(0);

		String mappingActionCreationData = System.getProperty("user.dir") + testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingAction");

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
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);


		

		//Step 8: Validating that default values of net land loss and net land gain is 0
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandLossTextBoxLabel),"value"),"0",
				"SMAB-T2481: Validation that default value of net land loss  is 0");
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandGainTextBoxLabel),"value"),"0",
				"SMAB-T2481: Validation that default value of net land gain  is 0");

		//Step 9: Validating that reason code field is auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2481: Validation that reason code field is auto populated from parent parcel work item");

		//Step 9: Validating that default value of Parcel Size drop down is yes
		softAssert.assertEquals(objMappingPage.getFirstSelectedDropDownValue(objMappingPage.parcelSizeDropDownLabel),"Yes",
				"SMAB-T2481: Validation that default value of Parcel Size drop down is yes");

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

		//Step 10: entering data in form for one to one mapping
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
		softAssert.assertEquals(gridDataHashMap.get("Neighborhood Code").get(0),neighborhoodValue,
				"SMAB-T2481: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),hashMapOneToOneMappingData.get("Reason code"),
				"SMAB-T2481: Validation that  System populates reason code from before screen");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),responseAPNDetails.get("Short_Legal_Description__c").get(0),
				"SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");

		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),traValue,
				"SMAB-T2481: Validation that  System populates TRA from the parent parcel");

		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),pucValue,
				"SMAB-T2481: Validation that  System populates Use Code  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("District").get(0),responseAPNDetails.get("District__c").get(0),
				"SMAB-T2481: Validation that  System populates District  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),primarySitusValue,
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
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 14: Validation of ALL fields THAT ARE displayed AFTER PARCEL ARE GENERATED
		softAssert.assertEquals(gridDataHashMap.get("Neighborhood Code").get(0),neighborhoodValue,
				"SMAB-T2481: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),hashMapOneToOneMappingData.get("Reason code"),
				"SMAB-T2481: Validation that  System populates reason code from before screen");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),responseAPNDetails.get("Short_Legal_Description__c").get(0),
				"SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),traValue,
				"SMAB-T2481: Validation that  System populates TRA from the parent parcel");

		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),"001vacant",
				"SMAB-T2486: Verify that User is able to to create a Use Code for the child parcel from the custom screen ");
		softAssert.assertEquals(gridDataHashMap.get("District").get(0),"Distrct 001",
				"SMAB-T2486: Verify that User is able to to create a  district for the child parcel from the custom screen ");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),primarySitusValue,
				"SMAB-T2481: Validation that  System populates Situs  from the parent parcel");
		driver.switchTo().window(parentWindow);

		objWorkItemHomePage.logout();

	}

	/**
	 * This method is to Verify all the validations for parent parcel for "One to One" mapping action 
	 * 	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2489:Verify all the validations for parent parcel for \"One to One\" mapping action ", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_VerifyParentParcelValidationOneToOneMappingAction(String loginUser) throws Exception {
		String activeParcelToPerformMapping="002-023-190";
		String activeParcelWithoutHyphen=activeParcelToPerformMapping.replace("-","");

		// fetching  parcel that is retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);
		// fetching  parcel that is In progress		
		queryAPNValue = "select Name from Parcel__c where Status__c='In Progress' limit 1";
		response = salesforceAPI.select(queryAPNValue);
		String inProgressAPNValue= response.get("Name").get(0);

		String mappingActionCreationData = System.getProperty("user.dir") + testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingAction");

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
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 4: Validation that proper error message is displayed if parent parcel is retired
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.editButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2489: Validation that proper error message is displayed if parent parcel is retired");

		//Step 5: Validation that proper error message is displayed if parent parcel is in progress
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.editButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2489: Validation that proper error message is displayed if parent parcel is in progress status");

		//Step 6: Validation that User should be allowed to enter the 9 digit APN without the - sign

		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.editButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, activeParcelWithoutHyphen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),activeParcelToPerformMapping,
				"SMAB-T2482: Validation that User should be allowed to enter the 9 digit parent APN without the \"-\"");

		
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "100234560");
	String error=	objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen);

		//Step 6: Validation that ReASON CODE is a mandatory field

		objMappingPage.clearFieldValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),"Reason Code is Mandatory.",
				"SMAB-T2489: Validation that proper error message is displayed if reason code field is left blank");

		//Step 6: Validation that User should be allowed to enter the 9 digit APN without the - sign in first non condo parcel number field

		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "010234560");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.legalDescriptionTextBoxLabel));
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.firstNonCondoTextBoxLabel),"value"),"010-234-560",
				"SMAB-T2482: Validation that User should be allowed to enter the 9 digit APN without the \"-\" in first non condo number field");

		driver.switchTo().window(parentWindow);

		objWorkItemHomePage.logout();

	}
}
