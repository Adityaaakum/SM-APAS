package com.apas.Tests.WorkItemsTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Reports.ReportLogger;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.EFileImportLogsPage;
import com.apas.PageObjects.EFileImportPage;
import com.apas.PageObjects.EFileImportTransactionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;
import com.apas.generic.ApasGenericFunctions;


public class ManualWorkItems_Tests extends TestBase implements testdata, modules, users {

	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	ApasGenericFunctions objApasGenericFunctions;
	BppTrendPage objBPPTrendPage;
	ParcelsPage objParcelsPage;

	Util objUtil;
	SoftAssertion softAssert;
	String eFileTestDataPath;
	String athertonBuildingPermitFile;
	String athertonBuildingPermitFile1;
	String sanMateoBuildingPermitFile;
	String sanMateoBuildingPermitFileWithError;
	String unincorporatedBuildingPermitFile;
	SalesforceAPI salesforceAPI;
	EFileImportPage objEFileImport;
	EFileImportLogsPage objEFileImportLogPage;
	EFileImportTransactionsPage objEFileImportTransactionpage;
	String EFileinvalidFormatFilepath;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{

		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objLoginPage = new LoginPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objEFileImport=new EFileImportPage(driver);
		objEFileImportLogPage=new EFileImportLogsPage(driver);
		objEFileImportTransactionpage=new EFileImportTransactionsPage(driver);
		objBPPTrendPage= new BppTrendPage(driver);
		 objParcelsPage= new ParcelsPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		salesforceAPI=new SalesforceAPI();
		}
	
	/**
	 * This method is to verify that user is able to view 'Use Code' and 'Street' fields getting automatically populated in the work item record 
	 * related to the linked Parcel
	 * @param loginUser
	 * @throws Exception
	 */
	
	@Test(description = "SMAB-T1994:verify that user is able to view 'Use Code' and 'Street' fields getting automatically populated in the work item record related to the linked Parcel", dataProvider = "loginRPBusinessAdmin",dataProviderClass = DataProviders.class, groups = {
		"regression" })
	public void workItems_verifyUseCode_streetFields(String loginUser)throws Exception{

		String apnValue="002-011-090";
		//changing the status of Approved Import logs if any in the system in order to import a new file
		//String query = "Select id From E_File_Import_Log__c where File_type__c = 'BPP Trend Factors' and File_Source__C = 'BOE - Index and Percent Good Factors' and Status__c = 'Approved' ";
		//salesforceAPI.update("E_File_Import_Log__c",query,"Status__c","Imported");
		
		//Step1: Login to the APAS application using the credentials passed through data provider (RP Business Admin)

		objApasGenericFunctions.login(loginUser);
		
		//Step2: Opening the PARCELS from global search box
		objApasGenericFunctions.searchModule(PARCELS);
		objApasGenericFunctions.globalSearchRecords(apnValue);

		//Step 3: Creating Manual work item for a Parcel records using Component Action flow where PUC and Primary Situs field (Street) have values saved
		Map<String, String> workItemFieldsMap = objParcelsPage.getWorkItemCreationTestData();
		System.out.print(objParcelsPage.createWorkItem(workItemFieldsMap));
		
		
		
		objEFileImport.selectFileAndSource("BPP Trend Factors", "BOE - Index and Percent Good Factors");
		objPage.waitForElementToBeClickable(objEFileImport.nextButton, 10);
		objPage.Click(objEFileImport.nextButton);
		objPage.Click(objEFileImport.periodDropdown);
		objPage.clickAction(objEFileImport.periodFirstDropDownValue);
		//apasGenericObj.selectFromDropDown(objEFileImport.periodDropdown, period);//period drop down xpath needs to be updated
		objPage.waitForElementToBeClickable(objEFileImport.confirmButton, 10);
		objPage.Click(objEFileImport.confirmButton);
		Thread.sleep(2000);
		
		  
        	ReportLogger.INFO("Verify invalid file format not allowed for file:");
        	objPage.waitForElementToBeClickable(objEFileImport.invalidFileErrorMsg,5);
        	
        	//softAssert.assertEquals(objEFileImport.invalidFileErrorMsg.getText(),"Your company doesn't support the following file types: ."+.substring(name.lastIndexOf(".")+1),"SMAB-T82:Verify the admin user is not able to select file for import with unacceptable formats using Upload button");
    	 
        objPage.Click(objEFileImport.closeButton);
        
       // objApasGenericFunctions.logout();
	
	}
	
	
	
}
		
		
	
	