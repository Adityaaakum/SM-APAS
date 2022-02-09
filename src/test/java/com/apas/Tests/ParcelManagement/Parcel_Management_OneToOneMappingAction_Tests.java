package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.AuditTrailPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.MappingPage;
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
	CIOTransferPage objtransfer;
	AuditTrailPage trail;


	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objtransfer=new CIOTransferPage(driver);
		trail= new AuditTrailPage(driver);


	}
	/**
	 * This method is to Verify that User is able to perform a "One to One" mapping action for a Parcel (Active) of type Non Condo from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3495,SMAB-T3494,SMAB-T3496,SMAB-T2655,SMAB-T2482,SMAB-T2488,SMAB-T2486,SMAB-T2481:Verify that User is able to perform a \"One to One\" mapping action for a Parcel (Active) of type Non Condo from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","OneToOneAction" })
	public void ParcelManagement_VerifyOneToOneMappingActionNonCondoParcel(String loginUser) throws Exception {
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
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
		String parcelSize	= "200";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObject.put("Lot_Size_SQFT__c",parcelSize);

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeMappingWithActionCustomerRequestCombine");

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
		
		//validating related action
		softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Related Action", "Information"),
						hashMapmanualWorkItemData.get("Actions"),"SMAB-T3494-Verify that the Related Action"
								+ "label should match the Actions labels while creating WI and it should"
								+ "open mapping screen on clicking Perform Customer Request Combine");
				
		//validating Event Id in Work item screen of Action type
		String eventIDValue = objWorkItemHomePage.getFieldValueFromAPAS("Event ID", "Information");
		softAssert.assertEquals(eventIDValue.contains("CRC"),
						true,"SMAB-T3496-Verify that the Event ID based on the mapping should be"
								+ "created and populated on the Work item record.");
				
		softAssert.assertTrue(!objWorkItemHomePage.waitForElementToBeVisible(6, objWorkItemHomePage.editEventIdButton),
						"SMAB-T3496-This field should not be editable.");
				
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

		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates District  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),primarySitusValue.replaceFirst("\\s+", ""),"SMAB-T2481: Validation that  System populates Situs  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2481: Validation that  System populates reason code from before screen");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescriptionValue,
				"SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),responsePUCDetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates Use Code  from the parent parcel");

		//Step 13 :Clicking generate parcel button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 14: Validation of ALL fields THAT ARE displayed AFTER PARCEL ARE GENERATED
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),primarySitusValue.replaceFirst("\\s+", ""),
				"SMAB-T2481: Validation that  System populates Situs  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2481: Validation that  System populates reason code from parent parcel work item");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescriptionValue,
				"SMAB-T2481: Validation that  System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2481: Validation that  System populates TRA from the parent parcel");
		
		//Step 15: Validation that child parcel primary situs is inherited from parent parcel
		String childPrimarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ gridDataHashMap.get("APN").get(0) +"')").get("Name").get(0);
		softAssert.assertEquals(primarySitusValue,childPrimarySitusValue,
				"SMAB-T2655: Validation that primary situs of child parcel is same as primary sitrus of parent parcel");
		driver.switchTo().window(parentWindow);
		
		//validate that The "Return " functionality for parcel mgmt activities should work for all these work items.
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		parentWindow = driver.getWindowHandle();	
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		softAssert.assertEquals(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName).getText(),"Update Parcel(s)",
				"SMAB-T3495-validate that The Return functionality for parcel mgmt activities should work for all these work items.");
		driver.switchTo().window(parentWindow);
		
		objWorkItemHomePage.logout();

	}

	/**
	 * This method is to  Verify that the One to One Mapping Action can only be performed on Active Parcels
	 *@param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3052,SMAB-T2482,SMAB-T2485,SMAB-T2484,SMAB-T2545,SMAB-T2489:Verify that the One to One Mapping Action can only be performed on Active Parcels ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","OneToOneAction" })
	public void ParcelManagement_VerifyOneToOneMappingActionOnlyOnActiveParcels(String loginUser) throws Exception {
		String queryAPN = "Select name From Parcel__c where (not Name like '100%') and (not Name like '134%') "
				+ "and Status__c='Active' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
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

		String apn[] = activeParcelToPerformMapping.split("-");
		String updateMapPageofChildApn= apn[0]+apn[1].substring(0,2)+
				String.valueOf(Integer.parseInt(apn[1].substring(2)) +1)+apn[2];
		ReportLogger.INFO(updateMapPageofChildApn);
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, updateMapPageofChildApn);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
		objMappingPage.waitForElementToBeVisible(10,objMappingPage.generateParcelButton);
	    softAssert.assertContains(objMappingPage.getErrorMessage(), 
				"Warning: Parcel number generated is different from the user"
				+ " selection based on established criteria. As a reference the number provided is", 
				"SMAB-T3052-Verify that for APN generation, If map page is changed for child parcels,"
				+ "then it should display message(which is returned from backend) for respective parcels");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();

	}
	/**
	 * This method is to Verify that User is able to perform a "One to One" mapping action for a Parcel (Active) by filling all fields in mapping action form for Condo and mobile home type parcels
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2655,SMAB-T2544:Verify that Verify that User is able to perform a One to One mapping action for a Parcel (Active) by filling all fields in mapping action form for Condo and mobile home type parcels", dataProvider = "Condo_MobileHome_Parcels", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","OneToOneAction" })
	public void ParcelManagement_VerifyOneToOneMappingActionCondoMobileHomeParcels(String loginUser,String parcelType) throws Exception {
		if(parcelType.equals("Condo_Parcel"))
			apnPrefix="100";
		if(parcelType.equals("Mobile_Home_Parcel"))
			apnPrefix="134";

		String queryAPN  = "Select name,ID  From Parcel__c where name like '"+apnPrefix+"%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') AND Status__c='Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";
		String parcelSize	= "200";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObject.put("Lot_Size_SQFT__c",parcelSize);

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);

		String mappingActionCreationData = testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");

		String situsCityName = hashMapOneToOneMappingData.get("City Name");
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
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2544: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),hashMapOneToOneMappingData.get("Reason code"),
				"SMAB-T2544: Validation that  System populates reason code from before screen");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),hashMapOneToOneMappingData.get("Legal Description"),
				"SMAB-T2544: Validation that  System populates Legal Description from the before screen");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2544: Validation that  System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),responsePUCDetails.get("Name").get(0),
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
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2544: Validation that  System populates neighborhood Code from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),"ReasonCode Test",
				"SMAB-T2544: Validation that  reason code is editable field");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),"Legal Description PM/01",
				"SMAB-T2544: Validation that  Legal Description is editable field");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2544: Validation that  System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),responsePUCDetails.get("Name").get(0),
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
	@Test(description = "SMAB-T3453,SMAB-T3548,SMAB-T2717,SMAB-T2718,SMAB-T2719,SMAB-T2720,SMAB-T2721,SMAB-T3771:Verify the attributes which will be inherited from the parent parcel to the child parcel and status of child parcels and parent parcel is changed ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","OneToOneAction" })
	public void ParcelManagement_VerifyOneToOneMappingActionChildInheritanceafterwI_Completion(String loginUser) throws Exception {
		
		JSONObject jsonObject = objMappingPage.getJsonObject();
		String queryAPN = "Select Name,Id  From Parcel__c where name like '0%' and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		String apnId=responseAPNDetails.get("Id").get(0);

		objMappingPage.deleteOwnershipFromParcel(apnId);
		objMappingPage.deleteCharacteristicInstanceFromParcel(apn);
		objMappingPage.deleteMailToInstanceFromParcel(apn);
		objMappingPage.deleteParcelSitusFromParcel(apn);
		
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,Id FROM PUC_Code__c "
				+ "where Legacy__c = 'NO' limit 1");

		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";

		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObject.put("Lot_Size_SQFT__c",100);		

		salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
		
		String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		String ownershipCreationData = testdata.MANUAL_PARCEL_CREATION_DATA;
		Map<String, String> hashMapCreateOwnershipRecordData = objUtil.generateMapFromJsonFile(ownershipCreationData,
				"DataToCreateOwnershipRecord");

		String mailToRecordCreationData = testdata.OWNERSHIP_AND_TRANSFER_CREATION_DATA;
		Map<String, String> hashMapMailToRecordData = objUtil.generateMapFromJsonFile(mailToRecordCreationData,
				"dataToCreateMailToRecordsWithIncompleteData");

		String characteristicsRecordCreationData = testdata.CHARACTERISTICS;
		Map<String, String> hashMapImprovementCharacteristicsData = objUtil.generateMapFromJsonFile(characteristicsRecordCreationData,
				"DataToCreateImprovementCharacteristics");
		Map<String, String> hashMapLandCharacteristicsData = objUtil.generateMapFromJsonFile(characteristicsRecordCreationData,
				"DataToCreateLandCharacteristics");

		// Adding Ownership records in the parcels
		objMappingPage.login(users.SYSTEM_ADMIN);

		objMappingPage.searchModule(PARCELS);
		objMappingPage.closeDefaultOpenTabs();

		objMappingPage.globalSearchRecords(apn);
		String primarySitusValue = objParcelsPage.createParcelSitus(apn);
		
		HashMap<String, ArrayList<String>> responseAssesseeDetails = objMappingPage.getOwnerForMappingAction(2);
	    String assesseeName1 = responseAssesseeDetails.get("Name").get(0);

		// Opening the PARCELS page and searching the parcel to create ownership record  

		try {

			objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
			objParcelsPage.createOwnershipRecord(assesseeName1, hashMapCreateOwnershipRecordData);
		}
		catch(Exception e) {
			ReportLogger.INFO("Fail to create ownership record : "+e);
		}

		objMappingPage.globalSearchRecords(apn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.mailTo);
		objParcelsPage.createMailToRecord(hashMapMailToRecordData,apn);

		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.createCharacteristicsOnParcel(hashMapImprovementCharacteristicsData,apn);
		
		objMappingPage.globalSearchRecords(apn);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
		objParcelsPage.createCharacteristicsOnParcel(hashMapLandCharacteristicsData,apn);

		objMappingPage.logout();
		Thread.sleep(4000);
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
		objMappingPage.editGridCellValue("Parcel Size (SQFT)*","100");
		
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
	        
	      // softAssert.assertEquals( salesforceAPI.select(pucLookeup).get("Name").get(0),"In Progress - New Parcel" ,"SMAB-T2718: Verifying the PUC of the new target parcel");        
	        HashMap<String, ArrayList<String>> statusoldApn = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn);
	        
	        //Validating status of parent parcel.
	        
	        softAssert.assertEquals(statusoldApn.get("Status__c").get(0), "In Progress - To Be Expired", "SMAB-T2718: Verifying the status of the source parcel");      
	        String pucLookeupold = "SELECT Name FROM PUC_Code__c where id in (SELECT PUC_Code_Lookup__c FROM Parcel__c where name='"+apn+"')"; 
	        
	        // Validating PUC of parent parcel.
	        
	        softAssert.assertEquals( salesforceAPI.select(pucLookeupold).get("Name").get(0),gridDataHashMap.get("Use Code*").get(0) ,"SMAB-T2718: Verifying the Puc of the source parcel");
	        HashMap<String, ArrayList<String>> statusnewApns = objParcelsPage.fetchFieldValueOfParcel("Status__c",childApn);
	        
	        // validating status of one to one child new parcel  
	        
	        softAssert.assertEquals(statusnewApns.get("Status__c").get(0), "In Progress - New Parcel", "SMAB-T2719: Verifying the status of the new target parcel,if it is in progress ,no work Items can be carried ,as only active parcels can have mapping actions");
	        
//		    child apn validation
	        objMappingPage.globalSearchRecords(childApn);
	        softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS
	        		(objMappingPage.parcelDistrictNeighborhood, "Summary Values"),responseNeighborhoodDetails.get("Name").get(0),
					"SMAB-T3548: Verify District/Neighborhood Code of Child Parcel is inheritted from first Parent Parcel");

			// Step 10: Verify TRA value is inherited from Parent to Child Parcels
			softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelTRA, "Parcel Information"),
					responseTRADetails.get("Name").get(0),
					"SMAB-T3548: Verify TRA of Child Parcel is inheritted from first Parent Parcel");	

			String childParcelLegal = objMappingPage.getFieldValueFromAPAS("Short Legal Description", "Legal Description");
    		softAssert.assertEquals(childParcelLegal,legalDescriptionValue," SMAB-T3620:Verify Legal of Child parcel");
    		
    		String childParcelSize = objMappingPage.getFieldValueFromAPAS("Parcel Size (SqFt)", "Parcel Information");
    		softAssert.assertEquals(childParcelSize,100," SMAB-T3620:Verify parcel size of Child parcel");
				
    		String childParcelSitus = objMappingPage.getFieldValueFromAPAS("Primary Situs", "Summary Values");
    		softAssert.assertEquals(childParcelSitus, primarySitusValue,
    				" SMAB-T3620:Verify Situs of Child parcel");
    		
			objParcelsPage.openParcelRelatedTab(objParcelsPage.ownershipTabLabel);
			HashMap<String, ArrayList<String>>	gridOwnershipDataHashMap = objMappingPage.getGridDataInHashMap();
			softAssert.assertEquals(gridOwnershipDataHashMap.get("Owner").get(0),assesseeName1,
					"SMAB-T3548: Verify Owner of Child Parcel is inheritted from first Parent Parcel");
			softAssert.assertContains(gridOwnershipDataHashMap.get("Ownership Percentage").get(0),hashMapCreateOwnershipRecordData.get("Ownership Percentage"),
					"SMAB-T3548: Verify Ownership Percentage of Child Parcel is inheritted from first Parent Parcel");
			softAssert.assertEquals(gridOwnershipDataHashMap.get("Status").get(0),hashMapCreateOwnershipRecordData.get("Status"),
					"SMAB-T3548: Verify Status of Child Parcel is inheritted from first Parent Parcel");

			objParcelsPage.openParcelRelatedTab(objParcelsPage.parcelCharacteristics);
			HashMap<String, ArrayList<String>>	gridCharateristicsDataHashMap  = objMappingPage.getGridDataInHashMap();
			softAssert.assertEquals(gridCharateristicsDataHashMap.get("Characteristics Screen").get(0),
					hashMapImprovementCharacteristicsData.get("Characteristics Screen"),
					"SMAB-T3548: Verify Characteristics Screen of Child Parcel is inheritted from first Parent Parcel");
			softAssert.assertEquals(gridCharateristicsDataHashMap.get("Characteristics Screen").get(1),
					hashMapLandCharacteristicsData.get("Characteristics Screen"),
					"SMAB-T3548: Verify Characteristics Screen of Child Parcel is inheritted from first Parent Parcel");

			objParcelsPage.openParcelRelatedTab(objParcelsPage.mailTo);
			HashMap<String, ArrayList<String>>	gridMailToDataHashMap = objMappingPage.getGridDataInHashMap();
			softAssert.assertContains(gridMailToDataHashMap.get("Formatted Name 1").get(0),
					hashMapMailToRecordData.get("Formatted Name1"),
					"SMAB-T3548: Verify mail to of Child Parcel is inheritted from first Parent Parcel");
			
	        
		   // Completing the work Item	
	        String   queryWI = "Select Id from Work_Item__c where Name = '"+WorkItemNo+"'";
	     	   salesforceAPI.update("Work_Item__c",queryWI, "Status__c", "Submitted for Approval");
	        driver.switchTo().window(parentWindow);
	        objWorkItemHomePage.logout();
	        objMappingPage.login(users.MAPPING_SUPERVISOR);
	        Thread.sleep(5000);
	        objMappingPage.searchModule(WORK_ITEM);
			objMappingPage.globalSearchRecords(WorkItemNo);
			driver.navigate().refresh(); //refresh as the focus is getting lost
			objMappingPage.waitForElementToBeVisible(10,objWorkItemHomePage.appLauncher);
			objWorkItemHomePage.completeWorkItem();
			objMappingPage.waitForElementToBeVisible(10,objWorkItemHomePage.linkedItemsWI);
			objWorkItemHomePage.Click(objWorkItemHomePage.linkedItemsWI);
	        
	        //navigating to business event audit trail
	        objWorkItemHomePage.scrollToElement(objWorkItemHomePage.secondRelatedBuisnessEvent);
	        objWorkItemHomePage.Click(objWorkItemHomePage.secondRelatedBuisnessEvent);
			String auditTrailNo = objWorkItemHomePage.getFieldValueFromAPAS("Name", "");
			ReportLogger.INFO("Audit Trail no:" +auditTrailNo);
			softAssert.assertContains(objMappingPage.getFieldValueFromAPAS("Event Library"),
					"one","SMAB-T3548: Verify event library updated from draft to one");
   	        HashMap<String, ArrayList<String>> statusnewApn2 = objParcelsPage.fetchFieldValueOfParcel("Status__c",childApn);
   	        
            // validating status of brand new parcel  
   	  
           softAssert.assertEquals(statusnewApn2.get("Status__c").get(0), "Active", "SMAB-T2720,SMAB-T2721: Verifying the status of the new target parcel");
           
            //Step 21: Verify 2 new WIs are generated and linked to Child Parcels after parcel is split and WI is completed
      
    		String queryToGetRequestType = "SELECT Work_Item__r.Request_Type__c FROM Work_Item_Linkage__c Where Parcel__r.Name = '"+gridDataHashMap.get("APN").get(0)+"' ";
    		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryToGetRequestType);
    		int expectedWorkItemsGenerated = response.get("Work_Item__r").size();
    		String requestType = response.get("Work_Item__r.Request_Type__c").get(0);
    		softAssert.assertTrue(!requestType.equals("New APN - Allocate Value"),
    				"SMAB-T2373: Validate system should not automatically create the work item Allocate Value when mapping action is completed");
    		softAssert.assertEquals(expectedWorkItemsGenerated,1,"SMAB-T2717: Verify 1 new Work Items are generated and linked to each child parcel after one to one mapping action is performed and WI is completed");
           // currently Allocate value is not genrated as part of new story so removed asseration for that
    		softAssert.assertContains(response,"New APN - Update Characteristics & Verify PUC",
    				"SMAB-T2717,SMAB-T3548: Verify Request Type of 1 new Work Items generated that are linked to each child parcel after many to many mapping action is performed and WI is completed");
    		softAssert.assertTrue(!response.get("Work_Item__r").get(0).contains("New APN - Allocate Value"),
					"SMAB-T3453:When a mapping action is completed the system should not automatically create the work item \"Allocate Value\"");
        //Validation that  System populates Situs  from the parent parcel      
        softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),primarySitusValue.replaceFirst("\\s+", ""),
				"SMAB-T2720,SMAB-T2721: Validation that  System populates Situs  from the parent parcel");
        
        //Validation that  System populates neighborhood Code from the parent parcel
        
		softAssert.assertEquals(gridDataHashMap.get( "Dist/Nbhd*").get(0),responseNeighborhoodDetails.get("Name").get(0),
				"SMAB-T2720,SMAB-T2721: Validation that  System populates neighborhood Code from the parent parcel");		
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescriptionValue,
				"SMAB-T2720,SMAB-T2721: Validation that  System populates Legal Description from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),responseTRADetails.get("Name").get(0),
				"SMAB-T2720,SMAB-T2721: Validation that  System populates TRA from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),hashMapOneToOneMappingData.get("Reason code"),
				"SMAB-T2720,SMAB-T2721: Validation that  System populates Reason Code from the parent parcel");
		
		//Step 15: Validation that child parcel primary situs is inherited from parent parcel
		
		 String childPrimarySitusValue=salesforceAPI.select("SELECT Name  FROM Situs__c Name where id in (SELECT Primary_Situs__c FROM Parcel__c where name='"+ gridDataHashMap.get("APN").get(0) +"')").get("Name").get(0);
		 softAssert.assertEquals(primarySitusValue,childPrimarySitusValue,
				"SMAB-T2720,SMAB-T2721: Validation that primary situs of child parcel is same as primary sitrus of parent parcel");
		 HashMap<String, ArrayList<String>> statusoldApn2 = objParcelsPage.fetchFieldValueOfParcel("Status__c",apn);
         softAssert.assertEquals(statusoldApn2.get("Status__c").get(0), "Retired", "SMAB-T2720,SMAB-T2721: Verifying the status of the source parcel");      
		
		 objMappingPage.searchModule(PARCELS);
		 objMappingPage.globalSearchRecords(childApn);
		 Thread.sleep(4000);
 	     objParcelsPage.selectOptionFromDropDown(objParcelsPage.moretab, objParcelsPage.parcelRelationshipsTabLabel);
 	     
 	     //Validating source parcel is present in child APN parcel relationships
 	     
 	    softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(apn)), "SMAB-T2720,SMAB-T2721: Verify Parent Parcel: "+apn+" is visible under Source Parcel Relationships section");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText(WorkItemNo)), "SMAB-T2720,SMAB-T2721: Verify WI : "+WorkItemNo+" is visible under  Parcel Relationships section");
		objWorkItemHomePage.logout();
		ReportLogger.INFO(" Appraiser logins ");
		objMappingPage.login(users.RP_APPRAISER);
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(childApn);
		objParcelsPage.Click(objParcelsPage.workItems);
		
		//Moving to the Update Characteristics Verify PUC WI
		objParcelsPage.Click(objParcelsPage.updateCharacteristicsVerifyPUC);
		objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		objWorkItemHomePage.waitForElementToBeVisible(40, objWorkItemHomePage.referenceDetailsLabel);
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objParcelsPage.Click(objMappingPage.getButtonWithText("Next"));
		objParcelsPage.Click(objParcelsPage.getButtonWithText("Done"));
		ReportLogger.INFO("Update Characteristics Verify PUC WI Completed");

		driver.switchTo().window(parentWindow);
		driver.navigate().refresh();
		objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		String workItemStatus = objMappingPage.getFieldValueFromAPAS("Status", "Information");
		softAssert.assertEquals(workItemStatus, "Completed", "SMAB-T3771: Validation WI completed successfully");
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(childApn);
		
		//Moving to Allocate Values WI
		objParcelsPage.Click(objParcelsPage.workItems);
		objParcelsPage.Click(objParcelsPage.allocateValue);
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		String assignedTo = objMappingPage.getFieldValueFromAPAS("Assigned To", "Information");
		String workPool = objMappingPage.getFieldValueFromAPAS("Work Pool", "Information");
		softAssert.assertEquals(assignedTo, salesforceAPI.select("SELECT Name FROM User where Username ='" + objMappingPage.userNameForRpAppraiser + "'").get("Name").get(0), "SMAB-T3771:Assiged to matched successfully");
		softAssert.assertEquals(workPool, objMappingPage.appraiserwWorkPool, "SMAB-T3771:workPool is matched successfully");

		objWorkItemHomePage.logout();

      
	
	}	
	  /**
		 * This method is to  Verify  the custom edit on mapping page
		 *@param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T3451,SMAB-T3459,SMAB-T3452,SMAB-T2837,SMAB-T2842: I need to have the ability to select specific fields from the mapping custom screen, so that the correct values can be assigned to the parcels. ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
				"Smoke","Regression","ParcelManagement","OneToOneAction" },enabled = true)
		public void ParcelManagement_VerifyOneToOneParcelEditAction(String loginUser) throws Exception {
			String queryAPN = "Select name,Id From Parcel__c where Status__c='Active'and name like '0%' and "
					+ " Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";			
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
			String activeParcelToPerformMapping=responseAPNDetails.get("Name").get(0);
			objMappingPage.deleteRelationshipInstanceFromParcel(activeParcelToPerformMapping);

			//Fetch some other values from database
			HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id"
					+ "  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c "
					+ "where Status__c='Active') limit 1");
		
			String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
			HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

			String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
			HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

			String legalDescriptionValue="Legal PM 85/25-260";
			String districtValue="District01";
			String parcelSize	= "200";	
			
			String PUC = salesforceAPI.select("SELECT Name FROM PUC_Code__c  limit 1").get("Name").get(0);
		    String TRA=salesforceAPI.select("SELECT Name FROM TRA__c limit 1").get("Name").get(0); 

			jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
			jsonObject.put("Status__c","Active");
			jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
			jsonObject.put("District__c",districtValue);
			jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
			jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
			jsonObject.put("Lot_Size_SQFT__c",parcelSize);

			//updating PUC details
			salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);

			String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
			Map<String, String> hashMapOneToOneParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformOneToOneMappingActionWithAllFields");
			String situsCityName = hashMapOneToOneParcelMappingData.get("City Name");
			String direction = hashMapOneToOneParcelMappingData.get("Direction");
			String situsNumber = hashMapOneToOneParcelMappingData.get("Situs Number");
			String situsStreetName = hashMapOneToOneParcelMappingData.get("Situs Street Name");
			String situsType = hashMapOneToOneParcelMappingData.get("Situs Type");
			String situsUnitNumber = hashMapOneToOneParcelMappingData.get("Situs Unit Number");
			String childprimarySitus=situsNumber+" "+direction+" "+situsStreetName+" "+situsType+" "+situsUnitNumber+", "+situsCityName;


			String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");
			// Step1: Login to the APAS application using the credentials passed through data provider (mapping staff user)
			objMappingPage.login(loginUser);

			// Step 2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
			objMappingPage.searchModule(PARCELS);
			objMappingPage.globalSearchRecords(activeParcelToPerformMapping);

			//Step 3: Creating Manual work item for the Parcel 
			String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

			//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
			Thread.sleep(3000);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
			String reasonCode=objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();	
			objWorkItemHomePage.switchToNewWindow(parentWindow);
			 objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
			objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneParcelMappingData.get("Action"));
			objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, hashMapOneToOneParcelMappingData.get("Are taxes fully paid?"));
			objMappingPage.waitForElementToBeVisible(objMappingPage.reasonCodeField);
			softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
					"SMAB-T2837: Validation that reason code field is auto populated from parent parcel work item");
			objMappingPage.fillMappingActionForm(hashMapOneToOneParcelMappingData);
			
			//updating child parcel size in second screen on mapping action 
			objMappingPage.waitForElementToBeVisible(5,objMappingPage.parcelSizeColumnSecondScreen);
			objMappingPage.updateMultipleGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace,"99",1);

			//	validating second screen warning message
			String parcelsizewarningmessage=objMappingPage.secondScreenParcelSizeWarning.getText();
			softAssert.assertEquals(parcelsizewarningmessage,
					"Parent Parcel Size = "+parcelSize+", Net Land Loss = 10, Net Land Gain = 0, "
							+ "Total Child Parcel(s) Size = 99.",
					"SMAB-T3451,SMAB-T3459-Verify that parent parcel size and entered net gain/loss and value is getting displayed");
			objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
			objMappingPage.editActionInMappingSecondScreen(hashMapOneToOneParcelMappingData);
			objMappingPage.waitForElementToBeClickable(5, objMappingPage.generateParcelButton);
			ReportLogger.INFO("Validate the Grid values");
			HashMap<String, ArrayList<String>> gridDataHashMapAfterEditAction =objMappingPage.getGridDataInHashMap();
			String childAPNNumber= gridDataHashMapAfterEditAction.get("APN").get(0);
			//Verifying new situs,TRA ,use code is populated in grid table		    
		    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Situs").get(0),childprimarySitus,
					"SMAB-T2837,SMAB-T2842: Validation that System populates Situs from the parent parcel");
		    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("TRA*").get(0),TRA,
					"SMAB-T2837,SMAB-T2842: Validation that System populates TRA from the parent parcel");
		    softAssert.assertEquals(gridDataHashMapAfterEditAction.get("Use Code*").get(0),PUC,
					"SMAB-T2837,SMAB-T2842: Validation that System populates TRA from the parent parcel");
		    ReportLogger.INFO("Click on Combine Parcel button");
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
			softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
					"SMAB-T2837,SMAB-T2842: Validate that User is able to perform Combine action for multiple active parcels");			    
		    
		    driver.switchTo().window(parentWindow);
		    objMappingPage.searchModule(PARCELS);
			
			objMappingPage.globalSearchRecords(childAPNNumber);
			//Validate the Situs of child parcel generated
		    softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS(objMappingPage.parcelPrimarySitus, "Parcel Information"),childprimarySitus,
					"SMAB-T2842: Validate the Situs of child parcel generated");
		    
		  //submit WI for approval
		    String   queryWI = "Select Id from Work_Item__c where Name = '"+workItemNumber+"'";
		    salesforceAPI.update("Work_Item__c",queryWI, "Status__c", "Submitted for Approval");
		    objWorkItemHomePage.logout();
		    
		    //login as supervisor 
		    objMappingPage.login(users.MAPPING_SUPERVISOR);
		    objMappingPage.searchModule(WORK_ITEM);
		    objMappingPage.globalSearchRecords(workItemNumber);
		    objWorkItemHomePage.javascriptClick(objWorkItemHomePage.dataTabCompleted);
		    objWorkItemHomePage.javascriptClick(objWorkItemHomePage.markAsCurrentStatusButton);
		    objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentParcelSizeErrorMsg, 20);
		    
		    String parcelSizeMismatchMsg = objWorkItemHomePage.parentParcelSizeErrorMsg.getText();
		    softAssert.assertEquals(parcelSizeMismatchMsg.contains("Total Child Parcel(s) size must match the Parent's Parcel Size"),
		    	 true,"SMAB-T3452: parent parcel validation at Work Item level");
			objWorkItemHomePage.logout();

		    
			

	}
	
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "One To ONe" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2681:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"One To ONe\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","OneToOneAction" })
	public void ParcelManagement_ReturnToCustomScreen_OneToOneMappingAction_NoPrimarySitusTRA(String loginUser) throws Exception {
		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c =NULL and TRA__c=NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), "Lot_Size_SQFT__c", "100");

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");
		
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
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)*").get(0);
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(apn), "SMAB-T3360 : Verify that for \"One to One\" mapping action, in custom action second screen and third screen Parent APN (s) "+apn+" is displayed");

		//Step 7: Click generate Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(apn), "SMAB-T3360 : Verify that for \"One to One\" mapping action, in custom action second screen and third screen Parent APN (s) "+apn+" is displayed");

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
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);
		softAssert.assertTrue(objMappingPage.validateParentAPNsOnMappingScreen(apn), "SMAB-T3360 : Verify that for \"One to One\" mapping action, in custom action second screen and third screen Parent APN (s) "+apn+" is displayed");

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2681: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2681: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2681: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2681: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2681: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2681: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2681: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2681: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2681: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");
		
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("APN"),"SMAB-T2681: Validation that APN column should not be editable on retirning to custom screen");
		// Legal Description and Reason code are editable as part of SMAB-12026
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Legal Description*"),"SMAB-T2681: Validation that Legal Description column on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("TRA*"),"SMAB-T2681: Validation that TRA column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Situs"),"SMAB-T2681: Validation that Situs column should not be editable on retirning to custom screen");
		// Legal Description and Reason code are editable as part of SMAB-12026
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Reason Code*"),"SMAB-T2681: Validation that Reason Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Dist/Nbhd*"),"SMAB-T2681: Validation that District/Neighborhood column should not be editable on retirning to custom screen");
		softAssert.assertTrue(!objMappingPage.verifyGridCellEditable("Use Code*"),"SMAB-T2681: Validation that Use Code column should not be editable on retirning to custom screen");
		softAssert.assertTrue(objMappingPage.verifyGridCellEditable("Parcel Size (SQFT)*"),"SMAB-T2681: Validation that Parcel Size (SQFT) column should  be editable on retirning to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	/**
	 * This method is to Parcel Management- Verify that User is able to Return to Custom Screen after performing  a "one to one" mapping action for a Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2681:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"one to one\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","OneToOneAction" })
	public void ParcelManagement_ReturnToCustomScreen_OneToOne_MappingAction_WithPrimarySitusTRA(String loginUser) throws Exception {

		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);

		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");

		String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;
		Map<String, String> hashMapOneToOneMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformOneToOneMappingActionWithAllFields");

		objParcelsPage.updateTraAndSitusWithSameCity(apn);

		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

		jsonObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));

		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonObject);
		
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
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)*").get(0);

		//Step 7: Click one to one Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(objMappingPage.confirmationMessageOnSecondScreen);

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
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2681: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2681: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2681: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Reason Code*").get(0),reasonCode,
				"SMAB-T2681: Validation that  System populates reason code in return to custom screen from the sane reason code that was entered while perfroming mapping action");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2681: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2681: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2681: Validation that  System populates Use Code as that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2681: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2681: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objWorkItemHomePage.logout();
	}
	@Test(description = "SMAB-T2652,SMAB-T2681,SMAB-T3634,SMAB-T3633,SMAB-T3635:Parcel Management- Verify that User is able to Return to Custom Screen after performing  a \"one to one\" mapping action for a Parcel", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement","OneToOneAction" })
	public void ParcelManagement_ReturnToCustomScreen_OneToOne_MappingAction_IndependentMappingActionWI(String loginUser) throws Exception {

		String childAPNPUC;
		
		String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL and  Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' limit 1";
		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
		String apn=responseAPNDetails.get("Name").get(0);
		
		String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
		HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
		String legalDescriptionValue="Legal PM 85/25-260";
		String districtValue="District01";
		HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");
		String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
		HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);
		
		jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
		jsonObject.put("Status__c","Active");
		jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
		jsonObject.put("District__c",districtValue);
		jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
		jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
		jsonObject.put("Lot_Size_SQFT__c", 100);
		jsonObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
		salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonObject);

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
		objMappingPage.waitForElementToBeVisible(20,objMappingPage.generateParcelButton);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();
		String childAPN=gridDataHashMap.get("APN").get(0);	
		String legalDescription=gridDataHashMap.get("Legal Description*").get(0);
		String tra=gridDataHashMap.get("TRA*").get(0);
		String situs=gridDataHashMap.get("Situs").get(0);
		String reasonCode=gridDataHashMap.get("Reason Code*").get(0);
		String districtNeighborhood=gridDataHashMap.get("Dist/Nbhd*").get(0);
		String parcelSizeSQFT=gridDataHashMap.get("Parcel Size (SQFT)*").get(0);

		//Step 5: Click one to one Parcel Button
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
		objMappingPage.waitForElementToBeVisible(20,objMappingPage.confirmationMessageOnSecondScreen);
		softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
				"SMAB-T2652: Validate that User is able to perform one to one  action from mapping actions tab");

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
		
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Type","Information"), "Mapping",
				"SMAB-T2652: Validation that  A new WI of type Mapping is created after performing one to one from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Action","Information"), "Independent Mapping Action",
				"SMAB-T2652: Validation that  A new WI of action Independent Mapping Action is created after performing one to one from mapping action tab");
		softAssert.assertEquals(objMappingPage.getFieldValueFromAPAS("Date", "Information"),DateUtil.removeZeroInMonthAndDay(DateUtil.getCurrentDate("MM/dd/yyyy")), "SMAB-T2652: Validation that 'Date' fields is equal to date when this WI was created");
	
		objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		String parentWindow = driver.getWindowHandle();
		objWorkItemHomePage.switchToNewWindow(parentWindow);
		objMappingPage.waitForElementToBeVisible(10, objMappingPage.updateParcelsButton);
		objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace, "90");
		objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.updateParcelButtonLabelName));

		//Step 9: Validation that User is navigated to a screen with following fields:APN,Legal Description,Parcel Size(SQFT),TRA,Situs,Reason Code,District/Neighborhood,Use Code
		gridDataHashMap =objMappingPage.getGridDataInHashMap();
		softAssert.assertEquals(gridDataHashMap.get("APN").get(0),childAPN,
				"SMAB-T2681: Validation that  System populates apn in return to custom screen  with the APN of child parcel");
		softAssert.assertEquals(gridDataHashMap.get("Dist/Nbhd*").get(0),districtNeighborhood,
				"SMAB-T2681: Validation that  System populates District/Neighborhood in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Situs").get(0).replaceFirst("\\s+", ""),situs.replaceFirst("\\s+", ""),"SMAB-T2681: Validation that  System populates Situs in return to custom screen  from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Legal Description*").get(0),legalDescription,
				"SMAB-T2681: Validation that  System populates Legal Description in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("TRA*").get(0),tra,
				"SMAB-T2681: Validation that  System populates TRA in return to custom screen from the parent parcel");
		softAssert.assertEquals(gridDataHashMap.get("Use Code*").get(0),childAPNPUC,
				"SMAB-T2681: Validation that  System populates Use Code that was edited in custom screen");
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),parcelSizeSQFT,
				"SMAB-T2681: Validation that  System populates parcel size column in return to custom screen from the parcel size that was entered while performing mapping action  ");
		softAssert.assertTrue(objMappingPage.verifyElementVisible(objMappingPage.updateParcelsButton),
				"SMAB-T2681: Validation that  There is \"Update Parcel(s)\" button on return to custom screen");

		driver.switchTo().window(parentWindow);
		objMappingPage.globalSearchRecords(gridDataHashMap.get("APN").get(0));
		softAssert.assertEquals(gridDataHashMap.get("Parcel Size (SQFT)*").get(0),
				objMappingPage.getFieldValueFromAPAS("Parcel Size (SqFt)", "Parcel Information"),
				"SMAB-T3633,SMAB-T3635:Parcel size(SQFT) was updated successfully and user was able to go to update screen");

		// Mark the WI complete
		query = "Select Id from Work_Item__c where Name = '" + workItem + "'";
		salesforceAPI.update("Work_Item__c", query, "Status__c", "Submitted for Approval");
		objWorkItemHomePage.logout();
		Thread.sleep(5000);
		ReportLogger.INFO(" Supervisor logins to close the WI ");
		objMappingPage.login(users.MAPPING_SUPERVISOR);
		objMappingPage.searchModule(WORK_ITEM);
		objMappingPage.globalSearchRecords(workItem);
		objMappingPage.Click(objWorkItemHomePage.linkedItemsWI);
		// refresh as the focus is getting lost
		driver.navigate().refresh();
		Thread.sleep(5000);
		objWorkItemHomePage.completeWorkItem();
		String workItemStatus = objMappingPage.getFieldValueFromAPAS("Status", "Information");
		softAssert.assertEquals(workItemStatus, "Completed", "SMAB-T3634: Validation WI completed successfully");
		objWorkItemHomePage.logout();
	}
	
	 /**
		 * This method is to  Verify  Divided Interest Parcel generation
		 *@param loginUser
		 * @throws Exception
		 */
		@Test(description = "SMAB-T3283,SMAB-T3669,SMAB-T2956,SMAB-T2881:Verify that user is able to perform One To One mapping action "
				+ "having Divided Interest parcel as Parent APN ", dataProvider = "loginMappingUser", 
				dataProviderClass = DataProviders.class, groups = {"Regression","ParcelManagement","OneToOneAction" })
		public void ParcelManagement_VerifyOneToOneDividedInterestParcelGeneration(String loginUser) throws Exception {
			
			objMappingPage.login(users.SYSTEM_ADMIN);
			String createNewParcel = testdata.MANUAL_PARCEL_CREATION_DATA;
			Map<String, String> hashMapCreateNewParcel = objUtil.generateMapFromJsonFile(createNewParcel,
						"DataToDividedInterestCreateNewParcel");
			String parentDividedInterestAPN1 = hashMapCreateNewParcel.get("APN1");
			String newParcelNumber1 = hashMapCreateNewParcel.get("Parcel Number1");
			HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,id"
					+ "  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c "
					+ "where Status__c='Active') limit 1");
			String PUC = responsePUCDetails.get("Name").get(0);
			objMappingPage.searchModule(PARCELS);
			objParcelsPage.createNewParcel(parentDividedInterestAPN1,newParcelNumber1,PUC);
			objParcelsPage.Click(objParcelsPage.getButtonWithText("Cancel"));
			
			objWorkItemHomePage.logout();	
				
			//Fetch some other values from database
				
			String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
			HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

			String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 2";
			HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

			String legalDescriptionValue="Legal PM 85/25-260";
			String districtValue="District01";
			String parcelSize	= "200";	

			jsonObject.put("PUC_Code_Lookup__c",responsePUCDetails.get("Id").get(0));
			jsonObject.put("Status__c","Active");
			jsonObject.put("Short_Legal_Description__c",legalDescriptionValue);
			jsonObject.put("District__c",districtValue);
			jsonObject.put("Neighborhood_Reference__c",responseNeighborhoodDetails.get("Id").get(0));
			jsonObject.put("TRA__c",responseTRADetails.get("Id").get(0));
			jsonObject.put("Lot_Size_SQFT__c",parcelSize);

			//updating Parcel details
			String queryApnId = "SELECT Name,Id FROM Parcel__c where Name in('"+
					parentDividedInterestAPN1+"')";
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryApnId);
			salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonObject);
			
			String mappingActionCreationData =  testdata.ONE_TO_ONE_MAPPING_ACTION;

			Map<String, String> hashMapOneToOneParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformOneToOneMappingActionWithAllFields");
			

			String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");
			
			// Step1: Login to the APAS application using the credentials passed through data provider (mapping staff user)
			objMappingPage.login(loginUser);

			// Step 2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
			objMappingPage.searchModule(PARCELS);
			objMappingPage.globalSearchRecords(parentDividedInterestAPN1);

			//Step 3: Creating Manual work item for the Parcel 
			String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

			//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
			Thread.sleep(3000);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();	
			objWorkItemHomePage.switchToNewWindow(parentWindow);
			
			//Step 5: Fill mandatory fields in mapping action screen 
			ReportLogger.INFO("Child parcel ends with 0 parent parcel is divideInterent Parcel: ");
			objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
			objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapOneToOneParcelMappingData.get("Action"));
			objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, hashMapOneToOneParcelMappingData.get("Are taxes fully paid?"));
			objMappingPage.waitForElementToBeVisible(objMappingPage.reasonCodeField);
			objMappingPage.fillMappingActionForm(hashMapOneToOneParcelMappingData);
			objMappingPage.waitForElementToBeVisible(5, objMappingPage.generateParcelButton);
			
			//Step 6: Generate Parcel
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
			softAssert.assertEquals(objMappingPage.confirmationMsgOnSecondScreen(),"Parcel(s) have been created successfully. Please review spatial information.",
					"SMAB-T3283:Verify that user is able to perform One To One mapping action having Divided Interest parcel as Parent APN");			    
		    
			String parentAPNComponent[] = parentDividedInterestAPN1.split("-");
			HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

			String childAPNNumber=gridDataHashMap.get("APN").get(0);	
			String childAPNComponents[] = childAPNNumber.split("-");
			softAssert.assertEquals(childAPNComponents[0],parentAPNComponent[0],
					"SMAB-T3283: Validation that MAP BOOK of parent and child parcels are same" );
			softAssert.assertEquals(childAPNComponents[1],parentAPNComponent[1],
					"SMAB-T3283: Validation that MAP page of parent and child parcels are same");
			softAssert.assertTrue(childAPNNumber.endsWith("0"),
					"SMAB-T3283: Validation that child APN number ends with 0");
			driver.switchTo().window(parentWindow);
			
			objParcelsPage.addParcelDetails("", "", "", "", "", "", gridDataHashMap, "APN");
			driver.navigate().refresh();
			objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.submittedForApprovalOptionInTimeline);
            objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentParcelSizeErrorMsg);
			String errorMsg = objWorkItemHomePage.parentParcelSizeErrorMsg.getText();
			objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
			softAssert.assertEquals(errorMsg,"Status: In order to submit or close the work item, the following field needs to be populated : Short Legal Description, Parcel Size (SqFt), TRA, District / Neighborhood Code, PUC. Please navigate to the mapping custom screen to provide the necessary information.",
					"SMAB-T3669 :Expected error message is displayed successfully");

			objParcelsPage.addParcelDetails("", "Legal", districtValue, responseNeighborhoodDetails.get("Id").get(0),
					responseTRADetails.get("Id").get(0), parcelSize, gridDataHashMap, "APN");
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentParcelSizeErrorMsg);
			errorMsg = objWorkItemHomePage.parentParcelSizeErrorMsg.getText();
			objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
			softAssert.assertEquals(errorMsg,"Status: In order to submit or close the work item, the following field needs to be populated : PUC. Please navigate to the mapping custom screen to provide the necessary information.",
					"SMAB-T3669 :Expected error message is displayed successfully");
			
			objParcelsPage.addParcelDetails(responsePUCDetails.get("Id").get(0), "", districtValue, responseNeighborhoodDetails.get("Id").get(0),
					responseTRADetails.get("Id").get(0), parcelSize, gridDataHashMap, "APN");
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentParcelSizeErrorMsg);
			errorMsg = objWorkItemHomePage.parentParcelSizeErrorMsg.getText();
			objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
			softAssert.assertEquals(errorMsg,"Status: In order to submit or close the work item, the following field needs to be populated : Short Legal Description. Please navigate to the mapping custom screen to provide the necessary information.",
					"SMAB-T3669 :Expected error message is displayed successfully");
			
			objParcelsPage.addParcelDetails(responsePUCDetails.get("Id").get(0), "Legal", districtValue, responseNeighborhoodDetails.get("Id").get(0),
					responseTRADetails.get("Id").get(0),"", gridDataHashMap, "APN");
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.submittedForApprovalOptionInTimeline);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.parentParcelSizeErrorMsg);
			errorMsg = objWorkItemHomePage.parentParcelSizeErrorMsg.getText();
			objWorkItemHomePage.Click(objWorkItemHomePage.CloseErrorMsg);
			softAssert.assertEquals(errorMsg,"Status: In order to submit or close the work item, the following field needs to be populated : Parcel Size (SqFt). Please navigate to the mapping custom screen to provide the necessary information.",
					"SMAB-T2956,SMAB-T2881 :Expected error message is displayed when parcel size is missing");

			objWorkItemHomePage.logout();

		}
		
		@Test(description = "SMAB-T2955,SMAB-T2880,SMAB-T2878,SMAB-T2953,SMAB-T2952,SMAB-T2877,SMAB-T2954,SMAB-T2879,SMAB-T2951,SMAB-T2876 ,SMAB-T2950,SMAB-T2814: Verify Parcel size validations for One to One mapping action", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {"Regression","ParcelManagement","OneToOneAction" })

		public void ParcelManagement_VerifyParcelSizeValidationsForOnetoOneMappingAction(String loginUser) throws Exception {

			// Fetching parcels that are Active with same Ownership record
			String queryAPNValue = "SELECT Id, Name FROM Parcel__c WHERE Id NOT IN (SELECT Parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '134%') and  Primary_Situs__c !=NULL and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Active' Limit 1";
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPNValue);
			String apn1 = responseAPNDetails.get("Name").get(0);
			
			String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");

			String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
			HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

			HashMap<String, ArrayList<String>> responsePUCDetails = salesforceAPI.select(
					"SELECT Name,id  FROM PUC_Code__c where id in (Select PUC_Code_Lookup__c From Parcel__c where Status__c='Active') limit 1");

			String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1";
			HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);
			String legalDescriptionValue = "Legal PM 85/25-260";

			JSONObject jsonParcelObject = objMappingPage.getJsonObject();
			jsonParcelObject.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
			jsonParcelObject.put("Short_Legal_Description__c", legalDescriptionValue);
			jsonParcelObject.put("TRA__c", responseTRADetails.get("Id").get(0));
			jsonParcelObject.put("Lot_Size_SQFT__c", "200");
			jsonParcelObject.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));

			salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), jsonParcelObject);

			String mappingActionCreationData = testdata.ONE_TO_ONE_MAPPING_ACTION;
			Map<String, String> hashMapCombineActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformOneToOneMappingActionWithAllFields");
			// Step1: Login to the APAS application
			objMappingPage.login(loginUser);

			// Step2: Opening the PARCELS page and searching the parcel to perform Combine
			// Action
			objMappingPage.searchModule(PARCELS);
			objMappingPage.globalSearchRecords(apn1);

			// Step 3: Creating Manual work item for the Parcel
			String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

			// Step 4:Clicking the details tab for the work item newly created and clicking
			// on Related Action Link
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
			String reasonCode = objWorkItemHomePage.getFieldValueFromAPAS("Reference", "Information");
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();
			objWorkItemHomePage.switchToNewWindow(parentWindow);

			// populating fields on mapping action screen
			objMappingPage.waitForElementToBeVisible(10, objMappingPage.actionDropDownLabel);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, apn1);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

			objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel, "One To One");
			objMappingPage.selectOptionFromDropDown(objMappingPage.taxesPaidDropDownLabel, "Yes");
			objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "-10");
			objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "");
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
					"- Please provide valid field value: Net Land Loss (SQ FT)",
					"SMAB-T2955,SMAB-T2880:Verify error message successfully when there is negative value for Net Loss field");

			objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");
			objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "-10");
			objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
					"- Please provide valid field value: Net Land Gain (SQ FT)",
					"SMAB-T2955,SMAB-T2880:Verify error message successfully when there is negative value for Net Gain field");

			objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "b1");
			objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "");
			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageOnFirstCustomScreen), "Enter a valid value.",
					"SMAB-T2955:Verify error message successfully when there is invalid value for Net Loss field");

			objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");
			objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "b1");
			objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "");

			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageOnFirstCustomScreen), "Enter a valid value.",
					"SMAB-T2955:Verify error message successfully when there is invalid value for Net Gain field");

			objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "100");
			objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "100");
			objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "");
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
					"- Please populate either \"Net Land Loss (SQ FT)\" or \"Net Land Gain (SQ FT)\"",
					"SMAB-T2953,SMAB-T2955,SMAB-T2878:Verify error message successfully when both Net Loss and Net Gain field are populated");

			objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "020");
			objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");

			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
			objMappingPage.waitForElementToBeVisible(objMappingPage.legalDescriptionFieldSecondScreen);
			objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace, "200");
			objMappingPage.Click(objMappingPage.legalDescriptionFieldSecondScreen);
			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageonSecondCustomScreen),
					"Parent Parcel Size = 200, Net Land Loss = 0, Net Land Gain = 020, Total Child Parcel(s) Size = 220.",
					"SMAB-T2952,SMAB-T2877:Verify message on Second custom screen when user enter Net Gain value");
			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
					"Total Child Parcel (s) size currently match the Parent's Parcel Size!",
					"SMAB-T2954,SMAB-T2952,SMAB-T2879:Verify message when parent parcel size matches the child's parcel size when there is Net Gain");

			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));

			objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "20");
			objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "");

			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));
			objMappingPage.waitForElementToBeVisible(objMappingPage.legalDescriptionFieldSecondScreen);
			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageonSecondCustomScreen),
					"Parent Parcel Size = 200, Net Land Loss = 20, Net Land Gain = 0, Total Child Parcel(s) Size = 1"
					+ "80.",
					"SMAB-T2951,SMAB-T2876;Verify message on Second custom screen when user enter Net Loss value ");
			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
					"Total Child Parcel (s) size currently match the Parent's Parcel Size!",
					"SMAB-T2951,SMAB-T2879;Verify message when parent parcel size matches the child's parcel size when there is Net Loss");

			objMappingPage.editGridCellValue(objMappingPage.parcelSizeColumnSecondScreenWithSpace, "-100");
			objMappingPage.clickAction(objMappingPage.legalDescriptionFieldSecondScreen);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));
			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageFirstScreen),
					"- Please provide valid field value: Parcel Size",
					"SMAB-T2955: Verify error message on negative parcel size value");

			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.previousButton));

			objMappingPage.enter(objMappingPage.netLandLossTextBoxLabel, "");
			objMappingPage.enter(objMappingPage.netLandGainTextBoxLabel, "");
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.nextButton));

			softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.errorMessageonSecondCustomScreen),
					"Parent Parcel Size = 200, Net Land Loss = 0, Net Land Gain = 0, Total Child Parcel(s) Size = 200.",
					"SMAB-T2950,SMAB-T2814:verify message on second custom screen when there is no Net Loss and Net Gain");
			
			driver.switchTo().window(parentWindow);
			objWorkItemHomePage.logout();

		}
		

		// This test method will generate WI for updateChars and Verify PUC and Allocate Value
		
		@Test(description = "SMAB-T4031,SMAB-T4032,SMAB-T4012,SMAB-T4013,SMAB-T4014,SMAB-T4015,SMAB-T4011,SMAB-T3978,SMAB-T3942,SMAB-T3941,SMAB-T3946,SMAB-T3947: "
				+ "verify that User is able to work on the with update chars and verify PUC work"
				+ " items along with Allocate Values work items", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
				"Regression", "ParcelManagement"})

		public void ParcelManagement_Verify_oneToOne_Mapping_Action_UpdateChars_VerifyPUC_And_Allocate_Values_Output(String loginUser)
				throws Exception {

			JSONObject jsonForOutputValidations = objMappingPage.getJsonObject();
			String executionEnv = System.getProperty("region");

			String queryAPN = "Select name,ID  From Parcel__c where name like '0%'and"
					+ " Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and status__c = 'Active' limit 2";
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
			String apn = responseAPNDetails.get("Name").get(0);
			String apnId = responseAPNDetails.get("Id").get(0);
			
			objMappingPage.deleteParcelSitusFromParcel(apn);
			
			objMappingPage.deleteOwnershipFromParcel(apnId);


			salesforceAPI.update("Parcel__c", responseAPNDetails.get("Id").get(0), "Lot_Size_SQFT__c", "100");
			

			String queryNeighborhoodValue = "SELECT Name,Id  FROM Neighborhood__c where Name !=NULL and legacy__c ='No' limit 1";
			HashMap<String, ArrayList<String>> responseNeighborhoodDetails = salesforceAPI.select(queryNeighborhoodValue);

			String queryTRAValue = "SELECT Name,Id FROM TRA__c limit 1";
			HashMap<String, ArrayList<String>> responseTRADetails = salesforceAPI.select(queryTRAValue);

			HashMap<String, ArrayList<String>> responsePUCDetails= salesforceAPI.select("SELECT Name,Id FROM PUC_Code__c "
					+ "where Legacy__c = 'NO' limit 1");

			String legalDescriptionValue = "Legal PM 85/25-260";
			String districtValue = "District01";

			jsonForOutputValidations.put("PUC_Code_Lookup__c", responsePUCDetails.get("Id").get(0));
			jsonForOutputValidations.put("Short_Legal_Description__c", legalDescriptionValue);
			jsonForOutputValidations.put("District__c", districtValue);
			jsonForOutputValidations.put("Neighborhood_Reference__c", responseNeighborhoodDetails.get("Id").get(0));
			jsonForOutputValidations.put("TRA__c", responseTRADetails.get("Id").get(0));

			salesforceAPI.update("Parcel__c",responseAPNDetails.get("Id").get(0),jsonForOutputValidations);
			
			String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");

			String mappingActionCreationData = testdata.ONE_TO_ONE_MAPPING_ACTION;
			Map<String, String> hashMapOneToOneActionMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformOneToOneMappingActionWithoutAllFields");

			// Step1: Login to the APAS application using the credentials passed through
			// dataprovider (RP Business Admin)
			objMappingPage.login(loginUser);

			// Step2: Opening the PARCELS page and searching the parcel to perform combine mapping
			driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
					+ responseAPNDetails.get("Id").get(0) + "/view");
			objParcelsPage.waitForElementToBeVisible(20, objParcelsPage.componentActionsButtonText);

			String workItemNumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);

			// Step 4:Clicking the details tab for the work item newly created and clicking
			// on Related Action Link
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			String parentWindow = driver.getWindowHandle();
			objWorkItemHomePage.switchToNewWindow(parentWindow);

			// Step 5: Selecting Action as 'Split' & Taxes Paid fields value as 'N/A'
			objMappingPage.waitForElementToBeVisible(60, objMappingPage.actionDropDownLabel);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
			

			// Step 6: entering correct data in mandatory fields
			objMappingPage.fillMappingActionForm(hashMapOneToOneActionMappingData);
			objMappingPage.waitForElementToBeVisible(10, objMappingPage.generateParcelButton);

			// Step 7: Click Split Parcel Button
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.generateParcelButton));

			// Retriving new APN genrated
			HashMap<String, ArrayList<String>> gridParcelData = objMappingPage.getGridDataInHashMap();
			String newCreatedApn = gridParcelData.get("APN").get(0);

			String query = "Select Id from Parcel__c where Name = '" + newCreatedApn + "'";
			HashMap<String, ArrayList<String>> response = salesforceAPI.select(query);

			// Submit work item for approval
			String querys = "Select Id from Work_Item__c where Name = '" + workItemNumber + "'";
			salesforceAPI.update("Work_Item__c", querys, "Status__c", "Submitted for Approval");

			driver.switchTo().window(parentWindow);
			objWorkItemHomePage.logout();
			Thread.sleep(5000);

			objMappingPage.login(users.MAPPING_SUPERVISOR);
			objMappingPage.searchModule(WORK_ITEM);
			objMappingPage.globalSearchRecords(workItemNumber);
			objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.completedOptionInTimeline);
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);
			objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.successAlert);
			objMappingPage.logout();
			Thread.sleep(5000);

			ReportLogger.INFO(" Appraiser logins ");
			objMappingPage.login(users.RP_APPRAISER);

			// Navigate to Child APN
			driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
					+ response.get("Id").get(0) + "/view");
		
			objParcelsPage.waitForElementToBeVisible(20, objParcelsPage.componentActionsButtonText);
			String puc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
			String districtAndNeighCode = objMappingPage.getFieldValueFromAPAS("District / Neighborhood Code",
					"Summary Values");

			objParcelsPage.Click(objParcelsPage.workItems);

			// Moving to the Update Characteristics Verify PUC WI
			objParcelsPage.Click(objParcelsPage.updateCharacteristicsVerifyPUC);
			
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.inProgressOptionInTimeline);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.reviewLink);
			String requestType = objMappingPage.getFieldValueFromAPAS("Request Type", "Information");
			softAssert.assertEquals(requestType, "New APN - Update Characteristics & Verify PUC",
					"SMAB-T3942: Verify Request Type of 1 new Work Items generated that are linked to each child parcel after Split mapping action is performed and WI is completed");

			String relatedAction = objMappingPage.getFieldValueFromAPAS("Related Action",
					"Information");

			softAssert.assertEquals(relatedAction, "Update Characteristics & Verify PUC",
					"SMAB-T3904: Update Characteristics & Verify PUC link is present");
			objWorkItemHomePage.waitForElementToBeVisible(40, objWorkItemHomePage.referenceDetailsLabel);
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			objWorkItemHomePage.switchToNewWindow(parentWindow);
			objWorkItemHomePage.waitForElementToBeVisible(20, objParcelsPage.getButtonWithText("Next"));

			// Verify that audit trail and GIS portal links in form of buttons are displayed
			// on the page
			softAssert.assertTrue(
					objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText("Parent Audit Trail")),
					"SMAB-T3949:Verify that Link of the associated Audit Trail record original request (Mapping Request) must be displayed in the custom screen");
			softAssert.assertTrue(
					objMappingPage.verifyElementVisible(objMappingPage.getButtonWithText("Parcel Map in GIS Portal")),
					"SMAB-T3947:Verify that A link to GIS must be displayed in the custom screen");
			List<WebElement> childParcelsList = driver.findElements(By.xpath(objMappingPage.listOfChildParcels));

			softAssert.assertEquals(childParcelsList.get(0).getText(), gridParcelData.get("APN").get(0),
						"SMAB-T3941: Child parcels are displayed in the correct order:" + childParcelsList.get(0).getText());

			objParcelsPage.Click(objParcelsPage.getButtonWithText("Next"));

			objMappingPage.Click(objMappingPage.mappingSecondScreenEditActionGridButton);
			objMappingPage.Click(objMappingPage.editButtonInSeconMappingScreen);
			objMappingPage.clearSelectionFromLookup("PUC");
			objMappingPage.clearSelectionFromLookup("District / Neighborhood Code");
			objMappingPage.Click(objMappingPage.getButtonWithText("Save"));
			Thread.sleep(2000);
			objParcelsPage.Click(objParcelsPage.getButtonWithText("Done"));

			softAssert.assertContains(objMappingPage.getErrorMessage(),
					"Enter valid values for PUC and District / Neighborhood for all Child Parcels.",
					"SMAB-T3978,SMAB-T3946: Validation that error message is displayed when  the PUC and District and Neighborhood are not populated, it will provide the following error message");

			String[] pucAndNeighCode = objMappingPage.editActionInUpdatePucAndCharsScreen(districtAndNeighCode, puc);
			objParcelsPage.Click(objParcelsPage.getButtonWithText("Done"));
			ReportLogger.INFO("Update Characteristics Verify PUC WI Completed");

			driver.switchTo().window(parentWindow);
			driver.navigate().refresh();
			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.submittedforApprovalTimeline);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			String workItemStatus = objMappingPage.getFieldValueFromAPAS("Status", "Information");
			softAssert.assertEquals(workItemStatus, "Completed",
					"SMAB-T3906: Validation WI completed successfully");
			
			// Moving to child APN
			driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
					+ response.get("Id").get(0) + "/view");

			// Moving to Allocate Values WI
			objParcelsPage.waitForElementToBeVisible(20, objParcelsPage.workItems);
			districtAndNeighCode = objMappingPage.getFieldValueFromAPAS("District / Neighborhood Code", "Summary Values");
			puc = objMappingPage.getFieldValueFromAPAS("PUC", "Parcel Information");
			softAssert.assertEquals(districtAndNeighCode, pucAndNeighCode[1],
					"SMAB-T4015: District and Neighbrhood code was updated successfully");
			softAssert.assertEquals(puc, pucAndNeighCode[0], "SMAB-T4015:PUC was updated successfully");
			objParcelsPage.Click(objParcelsPage.workItems);
			objParcelsPage.Click(objParcelsPage.allocateValue);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			String assignedTo = objMappingPage.getFieldValueFromAPAS("Assigned To", "Information");
			String workPool = objMappingPage.getFieldValueFromAPAS("Work Pool", "Information");
			softAssert.assertEquals(assignedTo,
					salesforceAPI
							.select("SELECT Name FROM User where Username ='" + objMappingPage.userNameForRpAppraiser + "'")
							.get("Name").get(0),
					"SMAB-T4011:Assiged to is matched successfully");
			softAssert.assertEquals(workPool, objMappingPage.appraiserwWorkPool,
					"SMAB-T4011:workPool is matched successfully");
			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			parentWindow = driver.getWindowHandle();
			objWorkItemHomePage.switchToNewWindow(parentWindow);
			objMappingPage.enter("Remarks", "It's a Remarks");

			// fetching the grid values to verify the presence of expected fields
			gridParcelData = objMappingPage.getGridDataInHashMap();

			softAssert.assertEquals(gridParcelData.containsKey("Parcel"), "true",
					"SMAB-T4012,SMAB-T4013:Verifying Parcel field is present");
			softAssert.assertEquals(gridParcelData.containsKey("Parcel Size (SqFt)"), "true",
					"SMAB-T4012,SMAB-T4013:Verifying parcel Size (SqFt) field is present");
			softAssert.assertEquals(gridParcelData.containsKey("Land %"), "true",
					"SMAB-T4012,SMAB-T4013:Verifying Land % field is persent");
			softAssert.assertEquals(gridParcelData.containsKey("Land (FBYV)"), "true",
					"SMAB-T4012,SMAB-T4013:Verifying Adjusted Land (FBYV) field is present");
			softAssert.assertEquals(gridParcelData.containsKey("Imp (FBYV)"), "true",
					"SMAB-T4012,SMAB-T4013:Verifying Imp (FBYV) field is present");
			softAssert.assertEquals(gridParcelData.containsKey("Total (FBYV)"), "true",
					"SMAB-T4012,SMAB-T4013:Verifying Total (FBYV) field is present");
			objParcelsPage.Click(objParcelsPage.getButtonWithText("Done"));

			driver.switchTo().window(parentWindow);
			objWorkItemHomePage.logout();
			Thread.sleep(5000);

			ReportLogger.INFO(" RP Principal logins ");
			objMappingPage.login(users.RP_PRINCIPAL);
			// Navigating to child APN
			driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
					+ response.get("Id").get(0) + "/view");

			objParcelsPage.waitForElementToBeVisible(20, objParcelsPage.workItems);
			objParcelsPage.Click(objParcelsPage.workItems);
			objParcelsPage.Click(objParcelsPage.allocateValue);
			objWorkItemHomePage.waitForElementToBeClickable(30, objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
			objWorkItemHomePage.waitForElementToBeVisible(20, objWorkItemHomePage.previousRelatedAction);

			String previousRelatedAction = objWorkItemHomePage.previousRelatedAction.getText();
			softAssert.assertEquals(previousRelatedAction, "Update Characteristics & Verify PUC",
					"SMAB-T4014:Update Characteristics & Verify PUC link is present");
			objWorkItemHomePage.clickOnTimelineAndMarkComplete(objWorkItemHomePage.completedOptionInTimeline);

			// Navigating to child APN
			driver.navigate().to("https://smcacre--" + executionEnv + ".lightning.force.com/lightning/r/Parcel__c/"
					+ response.get("Id").get(0) + "/view");

			objWorkItemHomePage.waitForElementToBeVisible(20, objParcelsPage.valueAllocation);
			objParcelsPage.Click(objParcelsPage.valueAllocation);
			String remarksOnAuditTrails = objMappingPage.getFieldValueFromAPAS("Remarks", "");
			softAssert.assertEquals(remarksOnAuditTrails, "It's a Remarks",
					"SMAB-T4032: Remarks added by Appraiser was saved successfully");
			trail.Click(trail.relatedBusinessRecords);
			objWorkItemHomePage.waitForElementToBeVisible(20, trail.valuesAllocated);
			String trailSubject = trail.valuesAllocated.getText();
			softAssert.assertEquals(trailSubject, "Values Allocated",
					"SMAB-T4031: Verified Summary that summary is Values Allocate ");
			objWorkItemHomePage.logout();
		}

	}