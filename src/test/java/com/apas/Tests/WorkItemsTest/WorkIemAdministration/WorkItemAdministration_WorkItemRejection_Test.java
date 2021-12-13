package com.apas.Tests.WorkItemsTest.WorkIemAdministration;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class WorkItemAdministration_WorkItemRejection_Test extends TestBase implements testdata, modules, users{
	
    RemoteWebDriver driver;
	
	ParcelsPage objParcelsPage;
	Page objPage;
    WorkItemHomePage objWorkItemHomePage;
    MappingPage objMappingPage;
    ApasGenericPage objApasGenericPage;
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
        objApasGenericPage = new ApasGenericPage(driver);
        objPage = new Page(driver);
    }

	
	@Test(description = "SMAB-T3271,SMAB-T3694: Work Input - Verify user should be able to reject the work item."
			+ "Verify the Rejection Reasons in the Picklist", 
    		dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, 
    		groups = {"Regression", "WorkItemAdministration","WIRejection" }, 
    		alwaysRun = true)
    public void WorkItemAdministration_VerifyWIRejection(String loginUser) throws Exception {
    	//Fetch Active APN
    	ArrayList<String> apns = objMappingPage.fetchActiveAPN(2);
    	String apn = apns.get(0);
    	    	
    	//Setup data to create Manual WI
		String workItemCreationData = testdata.MANUAL_WORK_ITEMS;
		Map<String, String> hashMapmanualWorkItemData = objUtil.generateMapFromJsonFile(workItemCreationData,
				"DataToCreateWorkItemOfTypeParcelManagement");
		
		//Step1: Login to the APAS application
		objMappingPage.login(loginUser);

		//Step2: Opening the PARCELS page and searching the parcel to create manual WI
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(apn);		
		
		//Step 3: Creating Manual work item for the Parcel
		String WINumber = objParcelsPage.createWorkItem(hashMapmanualWorkItemData);
        
		objWorkItemHomePage.Click(objWorkItemHomePage.detailsTab);
		
		//objWorkItemHomePage.Click(objApasGenericPage.editFieldButton(objWorkItemHomePage.selRejected));
		objWorkItemHomePage.Click(objWorkItemHomePage.getButtonWithText(objWorkItemHomePage.EditButton));
		objApasGenericPage.selectOptionFromDropDown(objWorkItemHomePage.selRejected, "Yes");	
		objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
		
		String expectedRejectionResonErrorMsg = "Rejection Reason is required";
		String actualRejectionReasonErrorMsg = objApasGenericPage.getIndividualFieldErrorMessage(objWorkItemHomePage.selRejectionReason);
		
		softAssert.assertContains(expectedRejectionResonErrorMsg, actualRejectionReasonErrorMsg, 
				"SMAB-T3271: If Rejected field is set to YES, Rejection Reason is required field.");
		
		objApasGenericPage.selectOptionFromDropDown(objWorkItemHomePage.selRejectionReason, "Other");
		objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);
		
		String expectedErrorRejectionCommentMsg = "Rejection Comment is required";
		String actualErrorRejectionCommentMsg = objApasGenericPage.getIndividualFieldErrorMessage(objWorkItemHomePage.txtRejectionComment);
		
		softAssert.assertContains(expectedRejectionResonErrorMsg, actualRejectionReasonErrorMsg, 
				"SMAB-T3271: If Rejection Reason field is set to Other, Rejection Comment is required field.");
		
		objApasGenericPage.selectOptionFromDropDown(objWorkItemHomePage.selRejectionReason, "Mapping - Different Ownership");		
		objApasGenericPage.enter(objWorkItemHomePage.txtRejectionComment, "This is Rejected for Reason Selected");
		
		List<Object> actualPickListOptions = new ArrayList<Object>();				
		actualPickListOptions = objApasGenericPage.getAllOptionFromDropDown(objWorkItemHomePage.selRejectionReason);
		//JSONArray actualArray = new JSONArray(actualPickListOptions);
		
		List<Object> expectedPickListOptions = new ArrayList<Object>();
		String workItemRejectionReasonData = testdata.WORKITEMREJECTIONREASONS;
		
		JSONParser parser = new JSONParser();
		Object object = parser
                .parse(new FileReader(workItemRejectionReasonData));
		
		JSONObject jsonObject = (JSONObject)object;
		
		JSONArray expectedArray = (JSONArray)jsonObject.get("RejectionReasons");
				
		for(int i=0; i<expectedArray.size(); i++) {
			
			expectedPickListOptions.add(expectedArray.get(i));
			
		}			
		
		boolean flag = false ;
		//iterator using for-each loop  
		for(Object tempList : actualPickListOptions) {

			flag = expectedPickListOptions.contains(tempList) ? true : false ;  
			
			if(flag = false) {
				break;
			}
		}  		
		
		softAssert.assertTrue(flag, "SMAB-T3694: Verify the Rejection Reasons in the Picklist : "+ actualPickListOptions);
		
		objWorkItemHomePage.Click(objWorkItemHomePage.saveButton);

		Thread.sleep(3000);
		
		String sqlQuery =  "SELECT Status__c from work_item__c where name = '"+WINumber+"'";        
        HashMap<String, ArrayList<String>> response_1 = salesforceAPI.select(sqlQuery);        
        String expectedStatus = response_1.get("Status__c").get(0);
        
        softAssert.assertEquals(objPage.getElementText(objWorkItemHomePage.currenWIStatusonTimeline),expectedStatus,
        		"SMAB-T3271: Verify the WI Status is Completed on Saving the Rejected WI.");                            		
		
	}

}


