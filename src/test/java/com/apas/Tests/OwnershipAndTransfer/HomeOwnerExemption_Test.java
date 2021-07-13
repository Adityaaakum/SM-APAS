package com.apas.Tests.OwnershipAndTransfer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.CIOTransferPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.Page;
import com.apas.PageObjects.ParcelsPage;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class HomeOwnerExemption_Test extends TestBase {
	
	private RemoteWebDriver driver;
	Page objPage;
	ParcelsPage objParcelsPage;
	ApasGenericPage objApasGenericPage;
	ExemptionsPage objExemptionsPage;
	CIOTransferPage objCIOTransferPage;
	Util objUtil;
	SoftAssertion softAssert;
	String homeOwnerExemptionData;
	String unrecordedEventData;
	
	@BeforeMethod(alwaysRun=true)
	public void beforeMethod() throws Exception{
		driver=null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objParcelsPage = new ParcelsPage(driver);
		objExemptionsPage = new ExemptionsPage(driver);
		objApasGenericPage = new ApasGenericPage(driver);
		objCIOTransferPage = new CIOTransferPage(driver);
		objUtil = new Util();
		softAssert = new SoftAssertion();
		homeOwnerExemptionData = System.getProperty("user.dir") + testdata.HOME_OWNER_EXEMPTION_DATA;
		unrecordedEventData = System.getProperty("user.dir") + testdata.UNRECORDED_EVENT_DATA;
		objApasGenericPage.updateRollYearStatus("Closed", "2021");
	}
	
	/**
	 Below test case is used to validate,
	 -Mandatory check validations on edit Exemption screen using Edit button
	 -Mandatory check validations on edit Exemption screen using Pencil Edit button
	 **/
	
	@Test(description = "SMAB-T522, SMAB-T527: Validate user is not able to edit and save Exemption record when mandatory fields are not entered before saving", groups = {"Regression","	", "HomeOwnerExemption"}, dataProvider = "loginCIOStaff", dataProviderClass = com.apas.DataProviders.DataProviders.class)
	public void HomeOwnerExemption_ExemptionRetain(String loginUser) throws Exception {
		
		String apn = objApasGenericPage.fetchActiveAPN();
		Map<String, String> dataToCreateHomeOwnerExemptionMap = objUtil.generateMapFromJsonFile(homeOwnerExemptionData, "NewHOECreation");
		Map<String, String> dataToCreateUnrecordedEventMap = objUtil.generateMapFromJsonFile(unrecordedEventData, "UnrecordedEventCreation");
		
		//Step1: Login to the APAS application using the user passed through the data provider
		objApasGenericPage.login(users.SYSTEM_ADMIN);
				
		//Step2: Open the Exemption module
		objApasGenericPage.searchModule(modules.PARCELS);
		objApasGenericPage.globalSearchRecords(apn);
		objParcelsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.editParcelButton));
		Thread.sleep(1000);
		objApasGenericPage.scrollToElement(objApasGenericPage.getWebElementWithLabel(objParcelsPage.exemptionLabel));
		objExemptionsPage.enter(objParcelsPage.exemptionLabel, dataToCreateHomeOwnerExemptionMap.get("Exemption"));
		objExemptionsPage.selectMultipleValues(dataToCreateHomeOwnerExemptionMap.get("ExemptionType"), objParcelsPage.exemptionTypeLabel);
		
		objExemptionsPage.Click(objParcelsPage.getButtonWithText(objParcelsPage.saveParcelButton));
		
		/*Step3: Create data map for the JSON file (DisabledVeteran_DataToCreateExemptionRecord.json)
				 Create Exemption record
				 Capture the record id and Exemption Name*/
		
		//objApasGenericPage.searchModule(modules.EXEMPTION);
		objParcelsPage.openParcelRelatedTab(objParcelsPage.exemptionRelatedTab);
		objExemptionsPage.createHomeOwnerExemption(dataToCreateHomeOwnerExemptionMap);
		//String recordId = objApasGenericPage.getCurrentRecordId(driver, "Exemption");
		String exemptionName = objPage.getElementText(objPage.waitForElementToBeVisible(objExemptionsPage.exemptionName));
		softAssert.assertTrue(exemptionName.contains("EXMPTN"),"SMAB-T522: Validate user is able to create Exemption with mandtory fields'");
		
		objApasGenericPage.logout();
		Thread.sleep(5000);
		
		objApasGenericPage.login(users.CIO_STAFF);
		objApasGenericPage.searchModule(modules.PARCELS);
		objApasGenericPage.globalSearchRecords(apn);
		String transferActivityId = objParcelsPage.createUnrecordedEvent(dataToCreateUnrecordedEventMap);
		
		objCIOTransferPage.editRecordedApnField(objCIOTransferPage.transferCodeLabel);
		objCIOTransferPage.searchAndSelectOptionFromDropDown(objCIOTransferPage.transferCodeLabel, "CIO-SALE");
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.saveButton));
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferCodeLabel, ""),"CIO-SALE",
				"SMAB-T2843,SMAB-T2838: Validate the Situs of child parcel generated");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferDescriptionLabel, ""),"Chg of Own, Ass, Sale",
				"SMAB-T2843,SMAB-T2838: Validate the Situs of child parcel generated");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.exemptionRetainLabel, ""),"No",
				"SMAB-T2843,SMAB-T2838: Validate the Situs of child parcel generated");
		
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.submitforApprovalButtonLabel));
		Thread.sleep(1000);
		objCIOTransferPage.Click(objCIOTransferPage.getButtonWithText(objCIOTransferPage.finishButton));
		Thread.sleep(1000);
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objCIOTransferPage.transferStatusLabel, "System Information"),"Submitted for Approval",
				"SMAB-T2843,SMAB-T2838: Validate the Situs of child parcel generated");
		
		objApasGenericPage.logout();
		Thread.sleep(5000);
		
		objApasGenericPage.login(users.EXEMPTION_SUPPORT_STAFF);
		objApasGenericPage.searchModule(modules.PARCELS);
		objApasGenericPage.globalSearchRecords(apn);
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionTypeLabel, "Summary Values"),"",
				"SMAB-T2843,SMAB-T2838: Validate the Situs of child parcel generated");
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objParcelsPage.exemptionLabel, "Summary Values"),"",
				"SMAB-T2843,SMAB-T2838: Validate the Situs of child parcel generated");
		
		objApasGenericPage.searchModule(modules.EXEMPTION);
		objApasGenericPage.globalSearchRecords(exemptionName);
		
		softAssert.assertEquals(objCIOTransferPage.getFieldValueFromAPAS(objExemptionsPage.qualification, "General Information"),"Not Qualified",
				"SMAB-T2843,SMAB-T2838: Validate the Situs of child parcel generated");
		
		objApasGenericPage.logout();
		
	}

}
