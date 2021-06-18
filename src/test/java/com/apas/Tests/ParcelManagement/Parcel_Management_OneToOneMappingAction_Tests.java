package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.generic.NEW;
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
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

import android.R.string;

public class Parcel_Management_OneToOneMappingAction_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject= new JSONObject();
	String apnPrefix=new String();

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
	 * This method is to Verify that User is able to perform a "One to One" mapping action for a Parcel (Active) of type Non Condo from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2655,SMAB-T2482,SMAB-T2488,SMAB-T2486,SMAB-T2481:Verify that User is able to perform a \"One to One\" mapping action for a Parcel (Active) of type Non Condo from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyOneToOneMappingActionNonCondoParcel(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String primarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ apn +"')").get("Name").get(0);
		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithoutAllFields");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);

        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
        objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 5: Validating that default values of net land loss and net land gain is 0
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandLossTextBoxLabel),"value"),"0",
				"SMAB-T2481: Validation that default value of net land loss  is 0");
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandGainTextBoxLabel),"value"),"0",
				"SMAB-T2481: Validation that default value of net land gain  is 0");

		//Step 6: Validating that reason code field is auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2481: Validation that reason code field is auto populated from parent parcel work item");

		//Step 7: Validating help icons
		objMappingPage.scrollToBottomOfPage();

		objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
				"SMAB-T2481: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");
		objMappingPage.Click(objMappingPage.helpIconLegalDescription);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank.",
				"SMAB-T2481: Validation that help text is generated on clicking the help icon for legal description");     		    
		objMappingPage.Click(objMappingPage.helpIconSitus);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent situs, leave as blank.",
				"SMAB-T2481: Validation that help text is generated on clicking the help icon for Situs text box");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.closeButton));

		//Step 8: entering data in form for one to one mapping
		objMappingPage.fillMappingActionForm(hashMapOneToOneMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 9: Verify that APNs generated must be 9-digits and should end in '0'
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

		//Step 10: Validation of ALL fields THAT ARE displayed on second screen

		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates District  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),primarySitusValue.replaceFirst("\\s+", ""),"SMAB-T2481: Validation that  System populates Situs  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
				"SMAB-T2481: Validation that  System populates reason code from before screen");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescriptionValue,
				"SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),responsePUCDetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates Use Code  from the parent parcel");

		//Step 13 :Clicking generate parcel button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 14: Validation of ALL fields THAT ARE displayed AFTER PARCEL ARE GENERATED
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),primarySitusValue.replaceFirst("\\s+", ""),
				"SMAB-T2481: Validation that  System populates Situs  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
				"SMAB-T2481: Validation that  System populates reason code from parent parcel work item");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescriptionValue,
				"SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates TRA from the parent parcel");
		
		//Step 15: Validation that child parcel primary situs is inherited from parent parcel
		String childPrimarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ gridDataHashMap.get("APN").get(0) +"')").get("Name").get(0);
		softAssert.assertEquals(primarySitusValue,childPrimarySitusValue,
				"SMAB-T2655: Validation that primary situs of child parcel is same as primary sitrus of parent parcel");
		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}

	/**
	 * This method is to  Verify that the One to One Mapping Action can only be performed on Active Parcels
	 *@param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2482,SMAB-T2485,SMAB-T2484,SMAB-T2545,SMAB-T2489:Verify that the One to One Mapping Action can only be performed on Active Parcels ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyOneToOneMappingActionOnlyOnActiveParcels(String loginUser) throws Exception {
		String queryAPN = "Select name From Parcel__c where Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String activeParcelToPerformMapping=responseAPNDetails.get("Name").get(0);
		String activeParcelWithoutHyphen=activeParcelToPerformMapping.replace("-","");

		// fetching  parcel that is retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);
		// fetching  parcel that is In Progress - To Be Expired		

		// fetching  parcel that is In Progress - To Be Expired		
		queryAPNValue = "select Name from Parcel__c where Status__c='In Progress - To Be Expired' limit 1";
		response = salesforceAPI.select(queryAPNValue);
		String inProgressAPNValue= response.get("Name").get(0);

		String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
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
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 5: Validation that proper error message is displayed if parent parcel is retired
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2489: Validation that proper error message is displayed if parent parcel is retired");

		//Step 6: Validation that proper error message is displayed if parent parcel is in progress
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue),"- In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2489: Validation that proper error message is displayed if parent parcel is in progress status");

		//Step 7: Validation that User should be allowed to enter the 9 digit APN without the - sign
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.parentAPNTextBoxLabel,activeParcelWithoutHyphen);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.parentAPNFieldValue),activeParcelToPerformMapping,
			"SMAB-T2482: Validation that User should be allowed to enter the 9 digit parent APN without the \"-\"");

		//Step 8: Validation that Reason CODE is a mandatory field
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.reasonCodeTextBoxLabel,"");
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.reasonCodeTextBoxLabel,""),"- Please enter the required field(s) : Reason Code",
				"SMAB-T2545: Validation that reason code is a mandatory field");
		objMappingPage.enter(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel), hashMapOneToOneMappingData.get("Reason code"));

		//Step 9: Validation that User should be allowed to enter the 9 digit APN without the - sign in first non condo parcel number field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "010234561");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.legalDescriptionTextBoxLabel));
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.firstNonCondoTextBoxLabel),"value"),"010-234-561",
				"SMAB-T2482: Validation that User should be allowed to enter the 9 digit APN without the \"-\" in first non condo number field");

		objMappingPage.scrollToBottomOfPage();

		//Step 10: Validation that proper error message is displayed if a parcel number starting from 100 is entered in non condo number field
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"100-234-561"),"- Non Condo Parcel Number cannot start with 100, Please enter valid Parcel Number",
				"SMAB-T2485: Validation that proper error message is displayed if a parcel number starting from 100 is entered in non condo number field");

		//Step 11: Validation that no error message is displayed if a parcel number starting from 134 is entered in non condo number field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "134-234-561");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.legalDescriptionTextBoxLabel));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		Thread.sleep(6000);
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(objMappingPage.previousButton)),
				"SMAB-T2485: Validation that no error message is displayed on first screen if a parcel number starting from 134 is entered in non condo number field");		
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));

		//Step 12: Validation that proper  error message is displayed if an alphanummeric parcel number is entered in non condo number field
		objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"abc123123abc");
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"abc123123abc"),"- This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2484: Validation that proper error message is displayed if an alphanummeric parcel number is entered in non condo number field");

		//Step 13: Validation that no error message is displayed if a parcel number starting from 800 is entered in non condo number field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "800-234-561");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.legalDescriptionTextBoxLabel));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		Thread.sleep(6000);
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(objMappingPage.previousButton)),
				"SMAB-T2485: Validation that no error message is displayed on first screen if a parcel number starting from 800 is entered in non condo number field");		
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));
		objMappingPage.scrollToBottomOfPage();

		//Step 14: Validation that proper  error message is displayed if parcel number  not of Nine digits is entered in non condo number field
		softAssert.assertEquals(objMappingPage.getMappingActionsFieldsErrorMessage(objMappingPage.firstNonCondoTextBoxLabel,"010123"),"- This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2484: Validation that proper error message is displayed if  parcel number  not of Nine digits is entered in non condo number field");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}
	/**
	 * This method is to Verify that User is able to perform a "One to One" mapping action for a Parcel (Active) by filling all fields in mapping action form for Condo and mobile home type parcels
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2655,SMAB-T2544:Verify that Verify that User is able to perform a One to One mapping action for a Parcel (Active) by filling all fields in mapping action form for Condo and mobile home type parcels", dataProvider = "Condo_MobileHome_Parcels", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyOneToOneMappingActionCondoMobileHomeParcels(String loginUser,String parcelType) throws Exception {
		if(parcelType.equals("Condo_Parcel"))
			apnPrefix="100";
		if(parcelType.equals("Mobile_Home_Parcel"))
			apnPrefix="134";

		String queryAPN  = "Select name,ID  From Parcel__c where name like '"+apnPrefix+"%' AND Primary_Situs__c !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);

		String mappingActionCreationData = testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");

		String situsCityName = hashMapOneToOneMappingData.get("Situs City Name");
		String direction = hashMapOneToOneMappingData.get("Direction");
		String situsNumber = hashMapOneToOneMappingData.get("Situs Number");
		String situsStreetName = hashMapOneToOneMappingData.get("Situs Street Name");
		String situsType = hashMapOneToOneMappingData.get("Situs Type");
		String situsUnitNumber = hashMapOneToOneMappingData.get("Situs Unit Number");
		String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;

		String workItemCreationData =  testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel 
		objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 5: Validating that default values of net land loss and net land gain is 0
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandLossTextBoxLabel),"value"),"0",
				"SMAB-T2544: Validation that default value of net land loss  is 0");
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.netLandGainTextBoxLabel),"value"),"0",
				"SMAB-T2544: Validation that default value of net land gain  is 0");
		//Step 6: Validating that reason code field is auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2544: Validation that reason code field is auto populated from parent parcel work item");

		//Step 7: Validating help icons
		objMappingPage.scrollToBottomOfPage();
		objMappingPage.Click(objMappingPage.helpIconFirstNonCondoParcelNumber);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use system generated APN, leave as blank.",
				"SMAB-T2544: Validation that help text is generated on clicking the help icon for First non-Condo Parcel text box");
		objMappingPage.Click(objMappingPage.helpIconLegalDescription);
		softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent legal description, leave as blank.",
				"SMAB-T2544: Validation that help text is generated on clicking the help icon for legal description");     		    
		objMappingPage.Click(objMappingPage.helpIconSitus);
	//	softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.helpIconToolTipBubble),"To use parent situs, leave as blank.",
		//		"SMAB-T2544: Validation that help text is generated on clicking the help icon for Situs text box");
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.closeButton));

		//Step 8: entering data in form for one to one mapping
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel));
		
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.editSitusLabelSitusModal),
				"SMAB-T2655: Validation that Edit Situs label is displayed as heading of situs modal window in first screen");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.situsInformationLabelSitusModal),
				"SMAB-T2655: Validation that  Situs Information label is displayed in  situs modal window in first screen");
		objMappingPage.editSitusModalWindowFirstScreen(hashMapOneToOneMappingData);
		
		objMappingPage.fillMappingActionForm(hashMapOneToOneMappingData);
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.situsTextBoxLabel),"value"),childprimarySitus,
				"SMAB-T2655: Validation that User is able to update a Situs for child parcel from the Parcel mapping screen");

		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 9: Verify that APNs generated must be 9-digits and should end in '0'
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

		//Step 9: Validation of ALL fields THAT ARE displayed on second screen
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2544: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),hashMapOneToOneMappingData.get("Reason code"),
				"SMAB-T2544: Validation that  System populates reason code from before screen");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),hashMapOneToOneMappingData.get("Legal Description"),
				"SMAB-T2544: Validation that  System populates Legal Description from the before screen");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2544: Validation that  System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),responsePUCDetails.get("Name").get(0),
				"SMAB-T2544: Validation that  System populates Use Code  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),childprimarySitus,
				"SMAB-T2655: Validation that System populates primary situs for child parcel on second screen with situs value that was added in first screen");

		//Step 10 :Verify that User is able to to edit reason code and legal description for the child parcel from the custom screen after performing one to one mapping action
		objMappingPage.editGridCellValue(objMappingPage.legalDescriptionColumnSecondScreen,"Legal Description PM/01");
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.editGridCellValue(objMappingPage.reasonCodeColumnSecondScreen,"ReasonCode Test");
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		Thread.sleep(5000);
		
		//Step 12 :Clicking generate parcel button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 13: Validation of ALL fields THAT ARE displayed AFTER PARCEL ARE GENERATED
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2544: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),"ReasonCode Test",
				"SMAB-T2544: Validation that  reason code is editable field");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),"Legal Description PM/01",
				"SMAB-T2544: Validation that  Legal Description is editable field");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2544: Validation that  System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),responsePUCDetails.get("Name").get(0),
				"SMAB-T2544: Verify that System populates use code  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0),childprimarySitus,
				"SMAB-T2655: Validation that  System populates primary situs for child parcel on last screen with situs value that was added in first screen");

		//Step 14: Validation that primary situs of child parcel has value that was entered in first screen
		String primarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ childAPNNumber +"')").get("Name").get(0);
		softAssert.assertEquals(primarySitusValue,childprimarySitus,
				"SMAB-T2655: Validation that primary situs of child parcel has value that  was entered in first screen through situs modal window");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}
	
		/*Verify the attributes which will be inherited from the parent parcel to the child parcel and status of child parcels and parent parcel
	 * Also ,validation of new Appraiser WI once after the Child parcel gets Active.
	 * login user-Mapping user
	 * 
	 */
	@Test(description = "SMAB-T2717,SMAB-T2718,SMAB-T2719,SMAB-T2720,SMAB-T2721:Verify the attributes which will be inherited from the parent parcel to the child parcel and status of child parcels and parent parcel is changed ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyOneToOneMappingActionChildInheritanceafterwI_Completion(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL  limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String primarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ apn +"')").get("Name").get(0);
		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObject.put("Lot_Size_SQFT__c",0);		

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		
		String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)		
		objMappingPage.login(loginUser);
		
		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping		
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);
		
		// Step 3: Creating Manual work item for the Parcel 
		
		 String WorkItemNo =objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
		 
		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		 
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
        objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
        
        //Clicking on Action Dropdown
        
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));		
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, hashMapOneToOneMappingData.get("Are taxes fully paid?"));
		objMappingPage.enter(objMappingPage.reasonCodeTextBoxLabel, hashMapOneToOneMappingData.get("Reason code"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeClickable(10, objMappingPage.generateParcelButton);
		
		//Fetching the GRID data
		
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childApn = gridDataHashMap.get("APN").get(0);
		
		//Clicking on genrate parcel button
		
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		Thread.sleep(5000);
		
		HashMap<String, ArrayList<String>> statusnewApn = objParcelsPage.fetchFieldValueOfParcel("Status__c",childApn);
		softAssert.assertEquals(statusnewApn.get("Status__c").get(0), "In Progress - New Parcel", "SMAB-T2718: Verifying the status of the new target parcel");
	        String pucLookeup = "SELECT Name FROM PUC_Code__c where id in (SELECT PUC_Code_Lookup__c FROM Parcel__c where name='"+childApn+"')"; 
	        
	        //Validating PUC Code of child parcel
	        
	        softAssert.assertEquals( salesforceAPI.select(pucLookeup).get("Name").get(0),"In Progress - New Parcel" ,"SMAB-T2718: Verifying the PUC of the new target parcel");        
	        HashMap<String, ArrayList<String>> statusoldApn = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn);
	        
	        //Validating status of parent parcel.
	        
	        softAssert.assertEquals(statusoldApn.get("Status__c").get(0), "In Progress - To Be Expired", "SMAB-T2718: Verifying the status of the source parcel");      
	        String pucLookeupold = "SELECT Name FROM PUC_Code__c where id in (SELECT PUC_Code_Lookup__c FROM Parcel__c where name='"+apn+"')"; 
	        
	        // Validating PUC of parent parcel.
	        
	        softAssert.assertEquals( salesforceAPI.select(pucLookeupold).get("Name").get(0),gridDataHashMap.get("Use Code").get(0) ,"SMAB-T2718: Verifying the Puc of the source parcel");
	        HashMap<String, ArrayList<String>> statusnewApns = objParcelsPage.fetchFieldValueOfParcel("Status__c",childApn);
	        
	        // validating status of brand new parcel  
	        
	        softAssert.assertEquals(statusnewApns.get("Status__c").get(0), "In Progress - New Parcel", "SMAB-T2719: Verifying the status of the new target parcel,if it is in progress ,no work Items can be carried ,as only active parcels can have mapping actions");
	        
		   // Completing the work Item	
	        String   queryWI = "Select Id from Work_Item__c where Name = '"+WorkItemNo+"'";
	     	   salesforceAPI.update("Work_Item__c",queryWI, "Status__c", "Submitted for Approval");
	        driver.switchTo().window(parentWindow);
	        objWorkItemHomePage.logout();
	        objMappingPage.login(users.MAPPING_SUPERVISOR);
	        Thread.sleep(5000);
	        objMappingPage.searchModule(WORK_ITEM);
			objMappingPage.globalSearchRecords(WorkItemNo);
			objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
			driver.navigate().refresh(); //refresh as the focus is getting lost
			Thread.sleep(5000);
			objWorkItemHomePage.completeWorkItem();
   	        HashMap<String, ArrayList<String>> statusnewApn2 = objParcelsPage.fetchFieldValueOfParcel("Status__c",childApn);
   	        
            // validating status of brand new parcel  
   	  
           softAssert.assertEquals(statusnewApn2.get("Status__c").get(0), "Active", "SMAB-T2720,SMAB-T2721: Verifying the status of the new target parcel");
           
            //Step 21: Verify 2 new WIs are generated and linked to Child Parcels after parcel is split and WI is completed
      
    		String queryToGetRequestType = "SELECT Work_Item__r.Request_Type__c FROM Work_Item_Linkage__c Where Parcel__r.Name = '"+gridDataHashMap.get("APN").get(0)+"' ";
    		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryToGetRequestType);
    		int expectedWorkItemsGenerated = response.get("Work_Item__r").size();
    		softAssert.assertEquals(expectedWorkItemsGenerated,1,"SMAB-T2717: Verify 2 new Work Items are generated and linked to each child parcel after one to one mapping action is performed and WI is completed");
           // currently Allocate value is not genrated as part of new story so removed asseration for that
    		softAssert.assertContains(response.get("Work_Item__r").get(0),"New APN - Update Characteristics & Verify PUC","SMAB-T2717: Verify Request Type of 2 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
    		
        //Validation that  System populates Situs  from the parent parcel
      
        softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),primarySitusValue.replaceFirst("\\s+", ""),
				"SMAB-T2720,SMAB-T2721: Validation that  System populates Situs  from the parent parcel");
        
        //Validation that  System populates neighborhood Code from the parent parcel
        
		softAssert.assertEquals(gridDataHashMap.get( "Dist/Nbhd").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2720,SMAB-T2721: Validation that  System populates neighborhood Code from the parent parcel");		
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescriptionValue,
				"SMAB-T2720,SMAB-T2721: Validation that  System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2720,SMAB-T2721: Validation that  System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),hashMapOneToOneMappingData.get("Reason code"),
				"SMAB-T2720,SMAB-T2721: Validation that  System populates Reason Code from the parent parcel");
		
		//Step 15: Validation that child parcel primary situs is inherited from parent parcel
		
		 String childPrimarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ gridDataHashMap.get("APN").get(0) +"')").get("Name").get(0);
		 softAssert.assertEquals(primarySitusValue,childPrimarySitusValue,
				"SMAB-T2720,SMAB-T2721: Validation that primary situs of child parcel is same as primary sitrus of parent parcel");
		 HashMap<String, ArrayList<String>> statusoldApn2 = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn);
         softAssert.assertEquals(statusoldApn2.get("Status__c").get(0), "Retired", "SMAB-T2720,SMAB-T2721: Verifying the status of the source parcel");      
		
		 objMappingPage.searchModule(PARCELS);
		 objMappingPage.globalSearchRecords(childApn);       
 	     objParcelsPage.selectOptionFromDropDown(objParcelsPage.moretab, objParcelsPage.parcelRelationshipsTabLabel);
 	     
 	     //Validating source parcel is present in child APN parcel relationships
 	     
 	    softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(apn)), "SMAB-T2720,SMAB-T2721: Verify Parent Parcel: "+apn+" is visible under Source Parcel Relationships section");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(WorkItemNo)), "SMAB-T2720,SMAB-T2721: Verify WI : "+WorkItemNo+" is visible under  Parcel Relationships section");
		objWorkItemHomePage.logout();

      
	
	}	
	
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "One To ONe" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2681:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"One To ONe\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_OneToOneMappingAction_NoPrimarySitusTRAOwnership(String loginUser) throws Exception {
		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithoutAllFields");
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		
		String workItem=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
				
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel one to one' 
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapOneToOneMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description").get(0);
		String tra=gridDataHashMap.get("TRA").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)").get(0);

		//Step 7: Click generate Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.getSuccessMessage();

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+apn+"') limit 1");
		if(responsePUCDetailsChildAPN.size()==0)
			  childAPNPUC ="";
		else
			childAPNPUC=responsePUCDetailsChildAPN.get("Name").get(0);
		
		//Step 8: Navigating back to the WI that was created and clicking on related action link 
		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.updateParcelsButton);

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2681: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),districtNeighborhood,
				"SMAB-T2681: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2681: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
				"SMAB-T2681: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescription,
				"SMAB-T2681: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),tra,
				"SMAB-T2681: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),childAPNPUC,
				"SMAB-T2681: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)").get(0),parcelSizeSQFT,
				"SMAB-T2681: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(objMappingPage.updateParcelsButton)),
				"SMAB-T2681: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");
		
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2681: Validation that APN column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Legal Description"),"SMAB-T2681: Validation that Legal Description column on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA"),"SMAB-T2681: Validation that TRA column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2681: Validation that Situs column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Reason Code"),"SMAB-T2681: Validation that Reason Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd"),"SMAB-T2681: Validation that District/Neighborhood column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code"),"SMAB-T2681: Validation that Use Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)"),"SMAB-T2681: Validation that Parcel Size (SQFT) column should  be editable on retirning to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "one to one" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2681:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"one to one\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_OneToOne_MappingAction_WithPrimarySitusTRA(String loginUser) throws Exception {

		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithoutAllFields");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);

		// Step 3: Creating Manual work item for the Parcel
		String workItem=objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);

		//Step 5: Selecting Action as 'perform parcel one to one' 
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 6: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapOneToOneMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description").get(0);
		String tra=gridDataHashMap.get("TRA").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)").get(0);

		//Step 7: Click one to one Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.getSuccessMessage();

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+apn+"') limit 1");
		if(responsePUCDetailsChildAPN.size()==0)
			  childAPNPUC ="";
		else
			childAPNPUC=responsePUCDetailsChildAPN.get("Name").get(0);
		
		//Step 8: Navigating back to the WI that was created and clicking on related action link 
		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.updateParcelsButton);

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2681: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),districtNeighborhood,
				"SMAB-T2681: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2681: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code").get(0),reasonCode,
				"SMAB-T2681: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescription,
				"SMAB-T2681: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),tra,
				"SMAB-T2681: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),childAPNPUC,
				"SMAB-T2681: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)").get(0),parcelSizeSQFT,
				"SMAB-T2681: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(objMappingPage.updateParcelsButton)),
				"SMAB-T2681: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	@Test(description = "SMAB-T2681:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"one to one\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_ReturnToCustomScreen_OneToOne_MappingAction_IndependentMappingActionWI(String loginUser) throws Exception {

		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);
		Thread.sleep(7000);
		objMappingPage.closeDefaultOpenTabs();

		// Step2: Opening the PARCELS page  and searching the  parcel 
		objMappingPage.searchModule("APAS");
		objMappingPage.searchModule("Mapping Action");
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.actionDropDownLabel);

		//Step 3: Selecting Action as 'perform parcel one to one' 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneMappingData.get("Action"));
		objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel,"Yes");

		//Step 4: filling all fields in mapping action screen
		objMappingPage.fillMappingActionForm(hashMapOneToOneMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		String childAPN=gridDataHashMap.get("APN").get(0);
		String legalDescription=gridDataHashMap.get("Legal Description").get(0);
		String tra=gridDataHashMap.get("TRA").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String reasonCode=gridDataHashMap.get("Reason Code").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)").get(0);

		//Step 5: Click one to one Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.getSuccessMessage();

		HashMap<String, ArrayList<String>> responsePUCDetailsChildAPN= salesforceAPI.select("SELECT Name FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Name='"+apn+"') limit 1");
		if(responsePUCDetailsChildAPN.size()==0)
			  childAPNPUC ="";
		else
			childAPNPUC=responsePUCDetailsChildAPN.get("Name").get(0);
		
		//Step 8: Navigating  to the independent mapping action WI that would have been created after performing one to one action and clicking on related action link 
		String workItemId= objWorkItemHomePage.getWorkItemIDFromParcelOnWorkbench(apn);
		String query = "SELECT Name FROM Work_Item__c where id = '"+ workItemId + "'";
		HashMap<String, ArrayList<String>> responseDetails = salesforceAPI.select(query);
		String workItem=responseDetails.get("Name").get(0);

		objMappingPage.globalSearchRecords(workItem);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(100, objMappingPage.updateParcelsButton);

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2681: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd").get(0),districtNeighborhood,
				"SMAB-T2681: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2681: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description").get(0),legalDescription,
				"SMAB-T2681: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA").get(0),tra,
				"SMAB-T2681: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code").get(0),childAPNPUC,
				"SMAB-T2681: Validation that  System populates Use Code that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)").get(0),parcelSizeSQFT,
				"SMAB-T2681: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(objMappingPage.updateParcelsButton)),
				"SMAB-T2681: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}


	
	
}
