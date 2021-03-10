package com.apas.Tests.ParcelManagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFObjectData;
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

public class Parcel_managment_MappingActionsParentParcelValidations extends TestBase implements testdata,modules,users   {
 
	
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
	
		String mappingActionCreationData = System.getProperty("user.dir") + testdata.REMAP_MAPPING_ACTION;
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
		
														
				softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"Mapping","SMAB-T2483: Validation that work pool should be 'Mapping' on parent parcel work item");
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
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, "111111111");
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				softAssert.assertEquals(objMappingPage.getErrorMessage(),"The following Parent APNs do not exist : 111-111-111", "SMAB-T2483: validating error message of invalid APN");
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
				
			                                  							
				softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"Mapping","SMAB-T2483: Validation that work pool should be 'Mapping' on parent parcel work item");
				String parentWindow=driver.getWindowHandle();
				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		 //  User enters into mapping page							
				objWorkItemHomePage.switchToNewWindow(parentWindow);					
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
   * Verify that when many parcels are entered that exceed the allocated space, the system should automatically
   *  auto-wrap the parent APN so they are displayed properly
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
				softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"Mapping","SMAB-T2483: Validation that work pool should be 'Mapping' on parent parcel work item");
				String parentWindow=driver.getWindowHandle();
				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
		 // : User enters into mapping page				
				objWorkItemHomePage.switchToNewWindow(parentWindow);					
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));    	
    			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apnlessThan9);
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));				
				softAssert.assertEquals(objMappingPage.getErrorMessage(),"The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+apnlessThan9, "T-SMAB2628 validating error message of invalid APN less than 9 digits");			  
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));		    	
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,invalidApn);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));			
			softAssert.assertEquals(objMappingPage.getErrorMessage(),"The following parent parcel number(s) is not valid, it should contain 9 digit numeric values :"+" "+apnlessThan9, "T-SMAB2628 validating error message of invalid APN less than 9 digits");		  
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));		
			objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,validApn);
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));									
			objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));					
			softAssert.assertEquals(objMappingPage.getAttributeValue(objMappingPage.getWebElementWithLabel(objMappingPage.parentAPNTextBoxLabel),"value"),apn,
							"SMAB-T2356: Validate the APN value is valid"); 
    			driver.switchTo().window(parentWindow);
				objMappingPage.logout();
    	
    	
    }
    
    
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
  	
  		String mappingActionCreationData = System.getProperty("user.dir") + testdata.REMAP_MAPPING_ACTION;
  		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
  				"DataToPerformRemapMappingAction");  		
  		String invalidApn =  apn.substring(0, 10)+"$";
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
  												
  				softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"Mapping","SMAB-T2483: Validation that work pool should be 'Mapping' on parent parcel work item");
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
				
								
  			  driver.switchTo().window(parentWindow);
			  objMappingPage.logout();
    
    
    }
    
    @Test(description = "SMAB-T2631,SMAB-T2632 ,SMAB-T2693: Verify Parent APN field cannot be blank except if mapping action is brand new parcel ",dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class
  		  ,groups = {"Regression","ParcelManagement"},enabled =true)
    public void verifyBrandNewParcelAction(String loginUser) throws Exception
    {
  	  String queryAPN = "Select name,ID  From Parcel__c where name like '0%' AND Primary_Situs__c !=NULL limit 2";
  		HashMap<String, ArrayList<String>> responseAPNDetails = salesforceAPI.select(queryAPN);
  		String apn=responseAPNDetails.get("Name").get(0);
  		String apn1=responseAPNDetails.get("Name").get(1);
  		
  	String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
  		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
  				"DataToCreateWorkItemOfTypeParcelManagement");
  	
  		String mappingActionCreationData = System.getProperty("user.dir") + testdata.REMAP_MAPPING_ACTION;
  		Map<String, String> hashMapNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionCreationData,
  				"DataToPerformRemapMappingAction");
  		String mappingActionBrandNewParcelData =  testdata.Brand_New_Parcel_MAPPING_ACTION;
  		Map<String, String> hashMapBrandNewParcelMappingData = objUtil.generateMapFromJsonFile(mappingActionBrandNewParcelData,
  				"DataToPerformBrandNewParcelMappingActionWithoutAllFields");
  		
  		
  		  
  	
          //Step1 - user login to APAS application
  		       objMappingPage.login(loginUser);
  		
  	
          // Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
  				objMappingPage.searchModule(PARCELS);
  				objMappingPage.globalSearchRecords(apn);
  	
  		
  		// Step 3: Creating Manual work item for the Parcel 
  				objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
  				Thread.sleep(3000);
  				

  		//Step 4:Clicking the  details tab for the work item newly created and clicking on Related Action Link
  				objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
  				objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.referenceDetailsLabel);		
  		
  														
  				softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"Mapping","SMAB-T2483: Validation that work pool should be 'Mapping' on parent parcel work item");
  				String parentWindow=driver.getWindowHandle();
  				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
  				
  		 // Step 5: User enters into mapping page	
  				
   				objWorkItemHomePage.switchToNewWindow(parentWindow);
   				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapBrandNewParcelMappingData.get("Action"));
   				softAssert.assertEquals(objMappingPage.verifyElementVisible(objMappingPage.parentAPNEditButton),false,"SMAB-T2632: Verify edit button is not available");
   				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapNewParcelMappingData.get("Action"));
   				softAssert.assertEquals(objMappingPage.verifyElementVisible(objMappingPage.parentAPNEditButton),true,"SMAB-T2632: Verify edit button is  available");
    
   				objMappingPage.selectOptionFromDropDown(objMappingPage.actionDropDownLabel,hashMapNewParcelMappingData.get("Action"));
   				softAssert.assertEquals(objMappingPage.getWebElementWithLabel(objMappingPage.reasonCodeTextBoxLabel).isEnabled(), true, "SMAB-T2693 Verify fields are populated");
   				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, "");
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				softAssert.assertEquals(objMappingPage.verifyElementVisible(objMappingPage.reasonCodeTextBoxLabel), false, "SMAB-T2693 Verify fields are not populated");
				
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel, "");
				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));
				softAssert.assertEquals(objMappingPage.getErrorMessage(),"The Parent APN cannot be blank.", "SMAB-T2631: validating error message of blank  APN");
   				
   			   driver.switchTo().window(parentWindow);
			   objMappingPage.logout();
               
    
    
    
    
    
}
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
  	
  		String mappingActionCreationData = System.getProperty("user.dir") + testdata.REMAP_MAPPING_ACTION;
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
  		
  														
  				softAssert.assertEquals(objWorkItemHomePage.getFieldValueFromAPAS("Work Pool", "Information"),"Mapping",": Validation that work pool should be 'Mapping' on parent parcel work item");
  				String parentWindow=driver.getWindowHandle();
  				objWorkItemHomePage.Click(objWorkItemHomePage.reviewLink);
  				
  		 //  User enters into mapping page	 				
  				 objWorkItemHomePage.switchToNewWindow(parentWindow);  				
  				 objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.parentAPNEditButton));
				  					
  				objMappingPage.enter(objMappingPage.parentAPNTextBoxLabel,apn+","+apn1);
  				objMappingPage.Click(objMappingPage.getButtonWithText(objMappingPage.saveButton));	 	          
  				objMappingPage.remapActionForm(RemapParcelMappingData);  			
  			  			
  			HashMap<String, ArrayList<String>> gridParcelData=      objMappingPage.getGridDataInHashMap();            
                      objMappingPage.editGridCellValue("APN", gridParcelData.get("APN").get(1));    
                    objMappingPage.Click(objMappingPage.remapParcelButton);
                   softAssert.assertEquals(objMappingPage.getElementText(objMappingPage.remapErrorMessageonSecondScreen), "The APN provided is a duplicate APN.Please check.", "SMAB-T2634: validate duplicates Apn Cannot be entered in parcel remap");
  			
                   driver.switchTo().window(parentWindow);
                   objMappingPage.logout();
  			
    } 
    
    
}


