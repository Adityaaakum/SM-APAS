package com.apas.Tests.ParcelManagement;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.json.JSONObject;
import org.openqa.selenium.Platform;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.MappingPage;
import com.apas.PageObjects.ParcelsPage;
import com.apas.PageObjects.ReportsPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class Parcel_Management_GIS_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;

	ParcelsPage objParcelsPage;
	WorkItemHomePage objWorkItemHomePage;
	Util objUtil = new Util();
	SoftAssertion softAssert = new SoftAssertion();
	SalesforceAPI salesforceAPI = new SalesforceAPI();
	MappingPage objMappingPage;
	JSONObject jsonObject= new JSONObject();
	String apnPrefix=new String();
	ReportsPage objReportsPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objParcelsPage = new ParcelsPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objMappingPage= new MappingPage(driver);
		objReportsPage = new ReportsPage(driver);
	}
		
		
	@Test(description = "SMAB-T2362:Verify user is able to access GIS by clicking on the parcel map button", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_AcessToOpenGIS(String loginUser) throws Exception {
       ArrayList<String> APNs=objMappingPage.fetchActiveAPN(1);
		String activeParcelToPerformMapping=APNs.get(0);
		String APNWithoutHypen=activeParcelToPerformMapping.replace("-", "");

	    String ExpectedUrl="https://172.16.17.18/careapps/forAPAS/index.html?find="  + APNWithoutHypen;		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeParcelToPerformMapping);
		objParcelsPage.clickAction(objParcelsPage.getButtonWithText(objParcelsPage.parcelMapInGISPortal));
		Thread.sleep(3000);
		String parent = driver.getWindowHandle();
		Set<String> s = driver.getWindowHandles();
		Iterator<String> I1 = s.iterator();
		while (I1.hasNext()) {
			String child_window = I1.next();
			if (!parent.equals(child_window)) {
				driver.switchTo().window(child_window);
				objParcelsPage.Click(objParcelsPage.advancedButton);
				objParcelsPage.Click(objParcelsPage.proceedButton);
 
				//Verify user is able to access GIS by clicking on the parcel map button
				softAssert.assertEquals(driver.getCurrentUrl(), ExpectedUrl, "SMAB-T2362:Verify user is able to access GIS by clicking on the parcel map button");
				driver.close();
			}
		}
		driver.switchTo().window(parent);
		objWorkItemHomePage.logout();
	}
	
	@Test(description = "SMAB-T2361:Verify tif file is downloaded by clicking on Open Assessor's Map button ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_OpenAssessorMap(String loginUser) throws Exception {
       ArrayList<String> APNs=objMappingPage.fetchActiveAPN(1);
		String activeParcelToPerformMapping=APNs.get(0);
		String APNWithoutHypen=activeParcelToPerformMapping.replace("-", "").substring(0, 4);
		String downloadLocation = testdata.DOWNLOAD_FOLDER;
        ReportLogger.INFO("Download location : " + downloadLocation);
        
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(activeParcelToPerformMapping);
		objParcelsPage.clickAction(objParcelsPage.openAsessorsMapButton);
		Thread.sleep(3000);
		String parent = driver.getWindowHandle();
		Set<String> s = driver.getWindowHandles();
		Iterator<String> I1 = s.iterator();
		while (I1.hasNext()) {
			String child_window = I1.next();
			if (!parent.equals(child_window)) {
				driver.switchTo().window(child_window);
				objParcelsPage.Click(objParcelsPage.advancedButton);
				objParcelsPage.Click(objParcelsPage.proceedButton);				        
		        File downloadedFile = Objects.requireNonNull(new File(downloadLocation).listFiles())[0];
		        String  genartedFileName = downloadedFile.getName();
		        softAssert.assertTrue(genartedFileName.contains(APNWithoutHypen), "SMAB-T2361: Verify tif file is downloaded by clicking on Open Assessor's Map button " + genartedFileName);
		        softAssert.assertEquals(genartedFileName.split("\\.")[1],"tif", "SMAB-T2361: Verify tif file is downloaded by clicking on Open Assessor's Map button");
		        objReportsPage.deleteFilesFromFolder(downloadLocation);
				driver.close();
			}
		}
		driver.switchTo().window(parent);
		objWorkItemHomePage.logout();
	}
	
	
	
	@Test(description = "SMAB-T2411:Verify tif file is not downloaded and 404 server error comes by clicking on Open Assessor's Map button ", dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, groups = {
			"regression","parcel_management" })
	public void ParcelManagement_OpenAssessorMapInactiveParcel(String loginUser) throws Exception {
       String inactiveParcel="232-323-234";

        
		// Step1: Login to the APAS application using the credentials passed through data provider (RP Business Admin)
		objMappingPage.login(loginUser);

		// Step2: Opening the PARCELS page  and searching the  parcel to perform one to one mapping
		objMappingPage.searchModule(PARCELS);
		objMappingPage.globalSearchRecords(inactiveParcel);
		//driver.navigate().refresh();
		Thread.sleep(2000);
		objParcelsPage.clickAction(objParcelsPage.openAsessorsMapButton);
		Thread.sleep(3000);
		String parent = driver.getWindowHandle();
		Set<String> s = driver.getWindowHandles();
		Iterator<String> I1 = s.iterator();
		while (I1.hasNext()) {
			String child_window = I1.next();
				if (!parent.equals(child_window)) {
					driver.switchTo().window(child_window);
					objParcelsPage.Click(objParcelsPage.advancedButton);
					objParcelsPage.Click(objParcelsPage.proceedButton);
				String title= driver.switchTo().window(child_window).getTitle();
		        softAssert.assertEquals(title,"404 - File or directory not found.", "SMAB-T2411: Verify tif file is not downloaded and 404 server error comes by clicking on Open Assessor's Map button");
				driver.close();
			}
		}
		driver.switchTo().window(parent);
		objWorkItemHomePage.logout();
	}
}		

