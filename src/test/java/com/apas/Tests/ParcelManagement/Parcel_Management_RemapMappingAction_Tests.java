package com.apas.Tests.ParcelManagement;

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
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class Parcel_Management_RemapMappingAction_Tests extends TestBase implements testdata, modules, users {
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
		
	@Test(description = "SMAB-T2490,SMAB-T2536:Verify that User is able to perform a Remap mapping action for a Parcel from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyRemapMappingActionForMultipleParcels(String loginUser) throws Exception {
       ArrayList<String> APNs=objMappingPage.fetchActiveAPN(2);
		String activeParcelToPerformMapping=APNs.get(0);
		String activeParcelToPerformMapping2=APNs.get(1);
		
		// fetching  parcel that is retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);

				
		String mappingActionCreationData = System.getProperty("user.dir") + testdata.REMAP_MAPPING_ACTION;
		Map<String, String> remapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");

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

		//Step 5: Validation that proper error message is displayed if one of the parent parcel is retired
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,remapMappingData.get("Action"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue + ","+ activeParcelToPerformMapping2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2490: Validation that proper error message is displayed if parent parcel is retired");
		
	
		//Step 6: entering data in form for remap 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,activeParcelToPerformMapping + ","+ activeParcelToPerformMapping2);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.remapActionForm(remapMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		gridDataHashMap =objMappingPage.getGridDataInHashMap();	
		
		//Step 7: Verifying whether the Remap action is performed for multiple parcels and total number of parcel generated are equal to number of parent parcels
		softAssert.assertEquals(gridDataHashMap.get("APN").size(),2,
				"SMAB-T2536: Verify that after remap total number of parcel generated are equal to number of parent parcels");
        driver.switchTo().window(parentWindow);
        
        //Step 8: Logout
	      objWorkItemHomePage.logout();

	}
	
	/**
	 * This method is to Verify that User is able to perform a "Remap" mapping action for a Parcel from a work item
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2490,SMAB-T2493,SMAB-T2532,SMAB-T2535,SMAB-T2531,SMAB-T2533:Verify that User is able to perform a Remap mapping action for a Parcel from a work item", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"Regression","ParcelManagement" })
	public void ParcelManagement_VerifyRemapMappingAction(String loginUser) throws Exception {
		String activeParcelToPerformMapping=objMappingPage.fetchActiveAPN();
		String activeParcelWithoutHyphen=activeParcelToPerformMapping.replace("-","");
		
		// fetching  parcel that is retired 		
		String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
		HashMap<String, ArrayList<String>> response = salesforceAPI.select(queryAPNValue);
		String retiredAPNValue= response.get("Name").get(0);
		
		// fetching  parcel that is In progress	
		String inProgressAPNValue;
		queryAPNValue = "select Name from Parcel__c where Status__c='In Progress - To Be Expired' limit 1";
		response = salesforceAPI.select(queryAPNValue);
		if(!response.isEmpty())
		inProgressAPNValue= response.get("Name").get(0);
		else
		{
		inProgressAPNValue= objMappingPage.fetchActiveAPN();
		jsonObject.put("PUC_Code_Lookup__c","In Progress - To Be Expired");
		jsonObject.put("Status__c","In Progress - To Be Expired");
		salesforceAPI.update("Parcel__c",objMappingPage.fetchActiveAPN(),jsonObject);
		}
		
				
		String mappingActionCreationData = System.getProperty("user.dir") + testdata.REMAP_MAPPING_ACTION;
		Map<String, String> remapMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
				"DataToPerformRemapMappingAction");

		String workItemCreationData =testdata.MANUAL_WORK_ITEMS;
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

		//Step 5: Validation that proper error message is displayed if parent parcel is retired
		objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,remapMappingData.get("Action"));
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,retiredAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2532: Validation that proper error message is displayed if parent parcel is retired");

		//Step 6: Validation that proper error message is displayed if parent parcel is in progress
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,inProgressAPNValue);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"-In order to proceed with this action, the parent parcel (s) must be active",
				"SMAB-T2532: Validation that proper error message is displayed if parent parcel is in progress status");

		//Step 7: Verifying that User should be allowed to enter the 9 digit APN without the \"-\" in Parent APN field
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, activeParcelWithoutHyphen);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));

		//Step 8: Validating that reason code field is auto populated from parent parcel work item
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel),"value"),reasonCode,
				"SMAB-T2490: Validation that reason code field is auto populated from parent parcel work item");
		
		//Step 9: Verifying that proper error message is displayed if alphanumeric value  is entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "123-45*-78&");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));		
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"This parcel number is not valid, it should contain 9 digit numeric values.",
				"SMAB-T2493: Validation that proper error message is displayed if alphanumeric value  is entered in First non condo parcel field");
		
		//Step 10:Verifying that proper error message is displayed if less than 9 disgits are entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "123-456-78");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));		
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"Parcel Number has to be 9 digit, please enter valid parcel number",
				"SMAB-T2493: Validation that proper error message is displayed if less than 9 disgits are entered in First non condo parcel field");
		
		//Step 11:Verifying that proper error message is displayed if parcel starting with 100 is entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "100-456-789");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));		
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"Non Condo Parcel Number cannot start with 100 or 134, Please enter valid Parcel Number",
				"SMAB-T2531: Validation that proper error message is displayed if parcel starting with 100 is entered in First non condo parcel field");
		
		//Step 12:Verifying that proper error message is displayed if parcel starting with 134 is entered in First non condo parcel field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, "134-456-789");
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));		
		softAssert.assertEquals(objMappingPage.getErrorMessage(),"Non Condo Parcel Number cannot start with 100 or 134, Please enter valid Parcel Number",
				"SMAB-T2531: Validation that proper error message is displayed if parcel starting with 134 is entered in First non condo parcel field");
		
		//Step 13: Verifying that User should be allowed to enter the 9 digit APN without the \"-\" in First Non Condo Parcel Field
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, activeParcelWithoutHyphen);
		objMappingPage.Click(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel));	
		softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.firstNonCondoTextBoxLabel),"value"),activeParcelToPerformMapping,
				"SMAB-T2535: Validation that User should be allowed to enter the 9 digit APN without the \"-\" in First Non Condo Parcel Field");
	
		objMappingPage.enter(objMappingPage.firstNonCondoTextBoxLabel, " ");
		//Step 14: entering data in form for remap 
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,activeParcelToPerformMapping);
		objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
		objMappingPage.remapActionForm(remapMappingData);
		HashMap<String, ArrayList<String>> gridDataHashMap =objMappingPage.getGridDataInHashMap();

		//Step 15: Verify that APNs generated must be 9-digits and should end in '0'
		String childAPNNumber =gridDataHashMap.get("APN").get(0);
		String childAPNComponents[] = childAPNNumber.split("-");
		softAssert.assertEquals(childAPNComponents.length,3,
				"SMAB-T2533: Validation that child APN number contains 3 parts: map book,map page,parcel number");
		softAssert.assertEquals(childAPNComponents[0].length(),3,
				"SMAB-T2533: Validation that MAP BOOK of child parcels contains 3 digits");
		softAssert.assertEquals(childAPNComponents[1].length(),3,
				"SMAB-T2533: Validation that MAP page of child parcels contains 3 digits");
		softAssert.assertEquals(childAPNComponents[2].length(),3,
				"SMAB-T2533: Validation that parcel number of child parcels contains 3 digits");
		softAssert.assertTrue(childAPNNumber.endsWith("0"),
				"SMAB-T2533: Validation that child APN number ends with 0");

		gridDataHashMap =objMappingPage.getGridDataInHashMap();	
        driver.switchTo().window(parentWindow);
        
        //Step 16: Logout
	      objWorkItemHomePage.logout();

	}
	
	
	 /*
	   * This method is to verify that APN exists in the system
	   * @param -Login user
	   * @throws-Exception
	   * 
	   */
	  
	  
	  @Test(description = "SMAB-T2483,SMAB-T2691,SMAB-T2692: Verify APN entered must exist in APAS,And no dupicates Apn allowed ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
			  ,groups = {"Regression","ParcelManagement"},enabled =true)
	  public void verifyAPNEnteredMustExistInApas(String loginUser) throws Exception
	  {
		  String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 2";
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
			String apn=responseAPNDetails.get("Name").get(0);
			String apn1=responseAPNDetails.get("Name").get(1);
			
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");
		
		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
		Map<String, String> RemapParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformRemapMappingAction");
		
		                               
		       //  user login to APAS application
			       objMappingPage.login(loginUser);			
	        //  Opening the PARCELS page  and searching the  parcel to perform one to one mapping
					objMappingPage.searchModule(PARCELS);
					objMappingPage.globalSearchRecords(apn);		
			//  Creating Manual work item for the Parcel 
					objParcelsPage.createWorkItem(hashMapmanualWorkItemData);			
					
			//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
					objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
					objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);														
					String parentWindow=driver.getWindowHandle();
					objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
					
			 //  User enters into mapping page									
					objWorkItemHomePage.switchToNewWindow(parentWindow);
					objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,RemapParcelMappingData.get("Action"));		          
				    objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));				
			// Step 6: User enters new APN that alerady exists in the system		
					objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn1);
					objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));								
					objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
					softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn1,
							"SMAB-T2483: Validate the APN value in Parent APN field");								 
					objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
					objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,RemapParcelMappingData.get("Action"));
					objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
					objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, "");
					objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				// validating error msg if no parent Apn is provided
					softAssert.assertEquals(objMappingPage.getErrorMessage(),"The Parent APN cannot be blank.", "SMAB-T2692: validating error message of no parent  APN");
								
	        // Step 7 : User enters an invalid APN to check
					objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
					objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, "000000000");
					objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
					softAssert.assertEquals(objMappingPage.getErrorMessage(),"The following Parent APNs do not exist : 000-000-000", "SMAB-T2483: validating error message of invalid APN");
			//Entering multiple same Apns and validating error messages
					objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
					objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, apn+","+apn);
					objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
					softAssert.assertEquals(objMappingPage.getErrorMessage(),"The Parent APN can not have duplicate APNs.","SMAB-T2691: Verify that when multiple same APN's are added in parent APN field, it should throw error or show the warning");
								
	  			  driver.switchTo().window(parentWindow);
				 objMappingPage.logout();
	  }
	  
	 
	  /**
	   * 
	   *  Verify that when multiple parent parcels are entered, if a space is entered or not after a comma, the system should format the parcel as expected.
	   * @param loginUser
	   * @throws Exception
	   */
	  
	  @Test(description = "SMAB-T2625:  Verify that when multiple parent parcels are entered, if a space is entered or not after a comma, the system should format the parcel as expected. ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
			  ,groups = {"Regression","ParcelManagement"},enabled=true)
	  public void verifyMultipleParentParcelsInduntation(String loginUser) throws Exception
	  {
		  String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 2";
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
			String apn=responseAPNDetails.get("Name").get(0);
			String apn1=responseAPNDetails.get("Name").get(1);
			
		    String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");
			String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
			Map<String, String> RemapParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformRemapMappingAction");	
		     String combineApn=apn+","+apn1;
			   
	           // user login to APAS application
			       objMappingPage.login(loginUser);		
		       //  Opening the PARCELS page  and searching the  parcel to perform one to one mapping
				   objMappingPage.searchModule(PARCELS);
				   objMappingPage.globalSearchRecords(apn);
			  //  Creating Manual work item for the Parcel 
				objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
			 //Clicking the  details tab for the work item newly created and clicking on Related Action Link
				objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
				objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);		                           							
				String parentWindow=driver.getWindowHandle();
				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			 //  User enters into mapping page							
				objWorkItemHomePage.switchToNewWindow(parentWindow);
				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,RemapParcelMappingData.get("Action"));
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));								
				//  Entering combined APN'S
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,combineApn);
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));				
				softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn+" , "+apn1,
							"SMAB-T2625:  Verify that when multiple parent parcels are entered, if a space is entered or not after a comma, the system should format the parcel as expected.");	
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
	  			driver.switchTo().window(parentWindow);
				objMappingPage.logout();
	                   

	}

	  
	  /**
	   * Verify that Parent APN should be a 9 digit number only
	 * @throws Exception 
	   * 
	   * 
	   * */
	    @Test(description = "SMAB-T2628:Verify that Parent APN should be a 9 digit number only ",
	   		enabled =true,groups = {"Regression","ParcelManagment"},dataProvider = "loginMappingUser",dataProviderClass = DataProviders.class)
	       public void ParentValidations(String LoginUser) throws Exception
	    {
	    	
	    	
	    	String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 2";
			HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
			String apn=responseAPNDetails.get("Name").get(0);
			String apn1=responseAPNDetails.get("Name").get(1);
			
		    String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
			Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
					"DataToCreateWorkItemOfTypeParcelManagement");	
			String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
			Map<String, String> RemapParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
					"DataToPerformRemapMappingAction");		
		       String apnlessThan9 = apn.substring(0, 10);
			   String validApn = apn.replace("-", "");
			   String invalidApn = apnlessThan9.replace("-", "");		   
	        // user login to APAS application
			    objMappingPage.login(LoginUser);	
	        //  Opening the PARCELS page  and searching the  parcel to perform one to one mapping
				objMappingPage.searchModule(PARCELS);
				objMappingPage.globalSearchRecords(apn);	
			
			//  Creating Manual work item for the Parcel 
				objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
			//Clicking the  details tab for the work item newly created and clicking on Related Action Link
				objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
				objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);				      		        						
				String parentWindow=driver.getWindowHandle();
				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
			 // : User enters into mapping page				
				objWorkItemHomePage.switchToNewWindow(parentWindow);
				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,RemapParcelMappingData.get("Action"));
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
				// user enters apn less than 9 digits
	    		objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apnlessThan9);
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));				
				softAssert.assertEquals(objMappingPage.getErrorMessage(),"The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+apnlessThan9, "T-SMAB2628 validating error message of invalid APN less than 9 digits");			  
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));	
				// user enter apn less than 9 and without -
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,invalidApn);
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));			
				softAssert.assertEquals(objMappingPage.getErrorMessage(),"The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+apnlessThan9, "T-SMAB2628 validating error message of invalid APN less than 9 digits");		  
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));	
				//user enters a valid apn
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,validApn);
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));									
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));					
				softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn,
								"SMAB-T2356: Validate the APN value is valid"); 
	    		driver.switchTo().window(parentWindow);
				objMappingPage.logout();
	    	
	    }
	    
	    
	    /**Verify APN entered must not have special character
	     * 
	     * @param loginUser
	     * @throws Exception
	     */
	    
	    
	    @Test(description = "SMAB-T2629,SMAB-T2630 : Verify APN entered must not have special character ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
	  		  ,groups = {"Regression","ParcelManagement"},enabled = true)
	    public void verifyAPNEnteredMustNotHaveSpcChar(String loginUser) throws Exception
	    {
	  	  String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 2";
	  		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
	  		String apn=responseAPNDetails.get("Name").get(0);
	  		String apn1=responseAPNDetails.get("Name").get(1);
	  		
	  	  String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
	  		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
	  				"DataToCreateWorkItemOfTypeParcelManagement");
	  	
	  		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
	  		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
	  				"DataToPerformRemapMappingAction");  		
	  		String invalidApn =  apn.substring(0, 10)+"$";
	  		String invalidApn2 =  apn.substring(0, 10)+".";
	  		String spacedApn = apn.replace("-", " ");  	
	          // user login to APAS application
	  		      objMappingPage.login(loginUser);  	
	          //  Opening the PARCELS page  and searching the  parcel to perform one to one mapping
	  			  objMappingPage.searchModule(PARCELS);
	  			  objMappingPage.globalSearchRecords(apn);   		
	  		//  Creating Manual work item for the Parcel 
	  			  objParcelsPage.createWorkItem(hashMapmanualWorkItemData);   				
	  		//Clicking the  details tab for the work item newly created and clicking on Related Action Link
	  			  objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
	  			  objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel); 			  
	  			  String parentWindow=driver.getWindowHandle();
	  			  objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);  				
	  		 //  User enters into mapping page	  				
	  		      objWorkItemHomePage.switchToNewWindow(parentWindow);
	  			  objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));   
	  			//	objMappingPage.fillMappingActionForm(hashMapBrandNewParcelMappingData);
	  			  objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));    
	  			  objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, invalidApn);
				  objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				  softAssert.assertEquals(objMappingPage.getErrorMessage(),"The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+invalidApn, "T-SMAB2629 validating error message of invalid APN");			  
				  objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));				
					//  User enters new APN that alerady exists in the system		
				  objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,spacedApn);
				  objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));					
				  objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));						
				  softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn,
									"SMAB-T2630: Validate the APN value in Parent APN field is without spaces");					 
				  objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));						
				  objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
				  // Entering . at the end of 8 digit apn
	  			  objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, invalidApn2);
				  objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				  softAssert.assertEquals(objMappingPage.getErrorMessage(),"The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+invalidApn2, "T-SMAB2629 validating error message of invalid APN");			  	
				
				  
	  			  driver.switchTo().window(parentWindow);
				  objMappingPage.logout();
	    
	    
	    }
	    
	    /**
	     * on mapping screen(second screen) when manually update the apn and enter the same existing as generated for another child parcel and try to finalize the action of multiple parcels generated error should be thrownS
	     * 
	     * @param loginUser
	     * @throws Exception
	     */

	    
	    
	    @Test(description = "SMAB-T2634: on mapping screen(second screen) when manually update the apn and enter the same existing as generated for another child parcel and try to finalize the action of multiple parcels generated error should be thrownS ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
	  		  ,groups = {"Regression","ParcelManagement"},enabled =true)
	    public void verifyRemapWithDuplicateApns(String loginUser) throws Exception
	    {
	  	  String queryAPN = "select name from parcel__c where status__c ='Active' limit 2";
	  		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
	  		String apn=responseAPNDetails.get("Name").get(0);
	  		String apn1=responseAPNDetails.get("Name").get(1);
	  		
	  	String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
	  		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
	  				"DataToCreateWorkItemOfTypeParcelManagement");
	  	
	  		String mappingActionCreationData = testdata.REMAP_MAPPING_ACTION;
	  		Map<String, String> RemapParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
	  				"DataToPerformRemapMappingAction");		  
	  	
	          // user login to APAS application
	  		       objMappingPage.login(loginUser);
	          //  Opening the PARCELS page  and searching the  parcel to perform one to one mapping
	  			 objMappingPage.searchModule(PARCELS);
	  			 objMappingPage.globalSearchRecords(apn);  	
	  		
	  		//  Creating Manual work item for the Parcel 
	  			objParcelsPage.createWorkItem(hashMapmanualWorkItemData);  				
	  				
	  		//Clicking the  details tab for the work item newly created and clicking on Related Action Link
	  			objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
	  			objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);  				
	  			String parentWindow=driver.getWindowHandle();
	  			objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);  				
	  		 //  User enters into mapping page	 				
	  			objWorkItemHomePage.switchToNewWindow(parentWindow);  				
	  			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));	
	  			//User duplicates apn in remap mapping 2 screen 
	  		    objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn+","+apn1);
	  		    objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));	 	          
	  		    objMappingPage.remapActionForm(RemapParcelMappingData);  	 			  			
	  			HashMap<String, ArrayList<String>> gridParcelData=      objMappingPage.getGridDataInHashMap();            
	             objMappingPage.editGridCellValue("APN", gridParcelData.get("APN").get(1));    
	             objMappingPage.Click(objMappingPage.remapParcelButton);
	             //Validating error message ,that duplicate apn cannot be remapped to parcels
	             softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.remapErrorMessageonSecondScreen), "The APN provided is a duplicate APN.Please check.", "SMAB-T2634: validate duplicates Apn Cannot be entered in parcel remap");  			
	                   
	             driver.switchTo().window(parentWindow);
	                  
	             objMappingPage.logout();
	  			
	    } 
	   
	    
	    /**
	     * Verify that when many parcels are entered that exceed the allocated space, the system should automatically auto-wrap the parent APN so they are displayed properly
	     * 
	     * 
	     * 
	     * @param loginUser
	     * @throws Exception
	     */
	    
	    @Test(description = "SMAB-T2627: Verify that when many parcels are entered that exceed the allocated space, the system should automatically auto-wrap the parent APN so they are displayed properly ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
	  		  ,groups = {"Regression","ParcelManagement"},enabled =true)
	    public void manyNewParcelApnFormatted(String loginUser) throws Exception
	    {
	  	  String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 11";
	  		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
	  		String apn = responseAPNDetails.get("Name").get(0);
	  		String combinedapn=responseAPNDetails.get("Name").get(0)+" , "+
	  		  responseAPNDetails.get("Name").get(1)+" , "+
	  			responseAPNDetails.get("Name").get(2)+" , "+
	  			responseAPNDetails.get("Name").get(3)+" , "+
	  			responseAPNDetails.get("Name").get(4)+" , "+
	  			responseAPNDetails.get("Name").get(5)+" , "+
	  			responseAPNDetails.get("Name").get(6)+" , "+
	  			responseAPNDetails.get("Name").get(7)+" , "+
	  			responseAPNDetails.get("Name").get(8)+" , "+
	  			responseAPNDetails.get("Name").get(9)+" , "+
	  			responseAPNDetails.get("Name").get(10)+" , ";
	  		
	  	String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
	  		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
	  				"DataToCreateWorkItemOfTypeParcelManagement");  	
	  		String mappingActionCreationData =  testdata.REMAP_MAPPING_ACTION;
	  		Map<String, String> hashMapNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
	  				"DataToPerformRemapMappingAction");
	        //Step1 - user login to APAS application
			       objMappingPage.login(loginUser);		
	        // Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
					objMappingPage.searchModule(PARCELS);
					objMappingPage.globalSearchRecords(apn);	
			// Step 3: Creating Manual work item for the Parcel 
					objParcelsPage.createWorkItem(hashMapmanualWorkItemData); 				

			//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
					objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
					objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);					
					String parentWindow=driver.getWindowHandle();
					objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);				
			 // Step 5: User enters into mapping page			
	 				objWorkItemHomePage.switchToNewWindow(parentWindow);
	 				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapNewParcelMappingData.get("Action"));
	 				 objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));				
	 				// Step 6: User enters new combined apn of 11 that alerady exists in the system		
	 				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,combinedapn);
	 				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));								
	 				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
	 				softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),combinedapn,
	 								"SMAB-T2627: Verify that when many parcels are entered that exceed the allocated space, the system should automatically auto-wrap the parent APN so they are displayed properly");
	 				
	 				
	 				 driver.switchTo().window(parentWindow);
	                 
		             objMappingPage.logout();
		  			
	    }
	    

	
	
	
}
