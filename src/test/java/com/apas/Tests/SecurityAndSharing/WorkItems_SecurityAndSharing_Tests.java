package com.apas.Tests.SecurityAndSharing;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.WorkItemHomePage;
import com.apas.TestBase.TestBase;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.config.users;

public class WorkItems_SecurityAndSharing_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;
	LoginPage objLoginPage;
	WorkItemHomePage objWorkItemHomePage;
	SoftAssertion softAssert = new SoftAssertion();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objLoginPage = new LoginPage(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
	}

	/**
	 * This method is to verify that work pool supervisor is not able to mass transfer WI's with status
	 * 'submitted for approval', 'Approval-On Hold' and 'completed'
	 * 
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2033: verify that work pool supervisor is not able to mass transfer WI's with status 'submitted for approval', 'Approval-On Hold' and 'completed'", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"regression","work_item_manual"  })
	public void WorkItems_SharingAndSecurity_NoMassApproval(String loginUser) throws Exception {
		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objWorkItemHomePage.login(loginUser);
		 
		//Step2: Navigate to home and submittedForApproval
		objWorkItemHomePage.searchModule(HOME);
	    objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABMySubmittedforApproval);
	    
	    
	    //Validate that work pool supervisor is not able to mass transfer WI's with status 'submitted for approval'
	    softAssert.assertTrue(objLoginPage.waitForElementToBeInVisible(objWorkItemHomePage.changeWorkPool ,10), "SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'submitted for approval'");
	    softAssert.assertTrue(objLoginPage.waitForElementToBeInVisible(objWorkItemHomePage.changeAssignee,10), "SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'submitted for approval'");
	    
	    //Validate that work pool supervisor is not able to mass transfer WI's with status 'Approval-On Hold'
		objWorkItemHomePage.searchModule(HOME);
	    driver.navigate().refresh();
	    objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.lnkTABMySubmittedforApproval);
		objWorkItemHomePage.openTab("On Hold");
	    softAssert.assertTrue(objLoginPage.waitForElementToBeInVisible(objWorkItemHomePage.changeWorkPool ,10), "SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'Approval-On Hold'");
	    softAssert.assertTrue(objLoginPage.waitForElementToBeInVisible(objWorkItemHomePage.changeAssignee,10), "SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'Approval-On Hold'");
	    
	    //Validate that work pool supervisor is not able to mass transfer WI's with status 'completed'
		objWorkItemHomePage.searchModule(HOME);
	    driver.navigate().refresh();
	    objWorkItemHomePage.waitForElementToBeVisible(objWorkItemHomePage.lnkTABCompleted);
	    objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABCompleted);
	    softAssert.assertTrue(objLoginPage.waitForElementToBeInVisible(objWorkItemHomePage.changeWorkPool ,10), "SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'completed'");
	    softAssert.assertTrue(objLoginPage.waitForElementToBeInVisible(objWorkItemHomePage.changeAssignee,10), "SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'completed'");

		objWorkItemHomePage.logout();
	}
}
