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
	@Test(description = "SMAB-T2033: verify that work pool supervisor is not able to mass transfer WI's with status 'submitted for approval', 'Approval-On Hold' and 'completed'", dataProvider = "loginRPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {"Regression","SecurityAndSharing","WorkItemAdministration"  })
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
	/**
	 * This method is to verify All the required button present on WI Home page For Supervisor User
	 *
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T3129,SMAB-T2636: Work Input - verify All the required button present on WI Home page For Supervisor User", dataProvider = "loginRpBusinessAdminAndPrincipalUsers", dataProviderClass = DataProviders.class, groups = {"Smoke","Regression","SecurityAndSharing","WorkItemAdministration" })
	public void WorkItems_SharingAndSecurity_ButtonPresent_WIHomePage_SupervisorUser(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through dataprovider (RP Business Admin)
		objWorkItemHomePage.login(loginUser);

		//Step2: Navigate to home page
		objWorkItemHomePage.searchModule(HOME);

		//Step3: Validate that no  buttons in completed tab
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.lnkTABInProgress);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_COMPLETED);
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.reassignButton), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.PutOnHoldButton), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.assignLevel2Approver), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.returnToPool), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.ConsolidateButton), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.markInProgress), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.WithdrawButton), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.changeWorkPool), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.changeAssignee), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.btnApprove), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.returnWorkItemButton), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.btnMarkComplete), "SMAB-T2636: Validate that no button is visible in completed tab");
		softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.acceptWorkItemButton), "SMAB-T2636: Validate that no button is visible in completed tab");

		//Step4: Validate the buttons in Needs My Approval tab
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.reassignButton), "SMAB-T2636: Validate that reassignButton is visible in Needs My Approval  tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.btnApprove), "SMAB-T2636: Validate that btnApprove is visible in Needs My Approval  tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.returnWorkItemButton), "SMAB-T2636: Validate that returnWorkItemButton is visible in Needs My Approval  tab");
		//As part of story SMAB-8061 this functionality is changed
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.PutOnHoldButton), "SMAB-T3129:Validate that PutOnHoldButton is not visible in Needs My Approval  tab");//SMAB-T2636: Validate that PutOnHoldButton is visible in Needs My Approval  tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.assignLevel2Approver), "SMAB-T2636: Validate that assignLevel2Approver is visible in Needs My Approval  tab");

		//Step5: Validate the buttons in In Progress tab
		driver.navigate().refresh();
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.lnkTABInProgress);
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInProgress);
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.btnMarkComplete);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.btnMarkComplete), "SMAB-T2636: Validate that mark complete button is visible in InProgress tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.returnToPool), "SMAB-T2636: Validate that returnToPool button is visible in InProgress tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.PutOnHoldButton), "SMAB-T2033: Validate that PutOnHoldButton button is visible in InProgress tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.ConsolidateButton), "SMAB-T2636: Validate that ConsolidateButton button is visible in InProgress tab");

		//Step6: Validate the buttons in In Pool tab
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInPool);
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.acceptWorkItemButton);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.acceptWorkItemButton), "SMAB-T2636: Validate that accept work item button is visible in InPool tab");

		//Step7: Validate the buttons in On Hold tab
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_On_Hold);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.markInProgress), "SMAB-T2636: Validate that mark in progress button is visible in On_Hold tab");

		//Step8: Validate the buttons in SubmittedforApproval tab
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABMySubmittedforApproval);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.WithdrawButton), "SMAB-T2636: Validate that WithdrawButton is present in SubmittedforApproval tab");

		//Step9: Validate the buttons in StaffInProgress tab
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_StaffInProgress);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.changeWorkPool), "SMAB-T2636: Validate that changeWorkPool button is present in StaffInProgress tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.changeAssignee), "SMAB-T2636: Validate that changeAssignee button is present in StaffInProgress tab");

		//Step10: Validate the buttons in StaffOnHold tab
		driver.navigate().refresh();
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.lnkTABInProgress);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_StaffOnHold);
		//As part of story SMAB-8061 this functionality is changed
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.changeWorkPool), "SMAB-T3129:Validate that changeWorkPool button is not present in Staff on hold tab");// "SMAB-T2636: Validate that changeWorkPool button is present in Staff on hold tab"
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.changeAssignee), "SMAB-T2636: Validate that changeAssignee button is present in Staff on hold tab");

		//Step11: Validate the buttons in StaffInPool tab
		driver.navigate().refresh();
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.lnkTABInProgress);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_StaffInPool);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.changeWorkPool), "SMAB-T2636: Validate that changeWorkPool button is present in StaffInPool tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.changeAssignee), "SMAB-T2636: Validate that changeAssignee button is present in StaffInPool tab");

		objWorkItemHomePage.logout();
	}

	/**
	 * This method is to verify All the required button present on WI Home page For staff User
	 *
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2635: Work Input - verify All the required button present on WI Home page For staff User", dataProvider = "loginExemptionSupportStaff", dataProviderClass = DataProviders.class, groups = {"Smoke","Regression","SecurityAndSharing","WorkItemAdministration"  })
	public void WorkItems_SharingAndSecurity_ButtonPresent_WIHomePage_StaffUser(String loginUser) throws Exception {

		// Step1: Login to the APAS application using the credentials passed through dataprovider 
		objWorkItemHomePage.login(loginUser);

		//Step2: Navigate to home page
		objWorkItemHomePage.searchModule(HOME);

		//Step3: Validate that no  buttons in completed tab
		objWorkItemHomePage.waitForElementToBeVisible(10,objWorkItemHomePage.lnkTABInProgress);
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_COMPLETED);
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.reassignButton), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.PutOnHoldButton), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.assignLevel2Approver), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.returnToPool), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.ConsolidateButton), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.markInProgress), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.WithdrawButton), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.changeWorkPool), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(!objLoginPage.verifyElementVisible(objWorkItemHomePage.changeAssignee), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.btnApprove), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.returnWorkItemButton), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.btnMarkComplete), "SMAB-T2635: Validate that no button is visible in completed tab");
		softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.acceptWorkItemButton), "SMAB-T2635: Validate that no button is visible in completed tab");

		//Step4: Validate the buttons in In Progress tab
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInProgress);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.btnMarkComplete), "SMAB-T2635: Validate that mark complete button is visible in InProgress tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.returnToPool), "SMAB-T2635: Validate that returnToPool button is visible in InProgress tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.PutOnHoldButton), "SMAB-T2635: Validate that PutOnHoldButton button is visible in InProgress tab");
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.ConsolidateButton), "SMAB-T2635: Validate that ConsolidateButton button is visible in InProgress tab");

		//Step5: Validate the buttons in In Pool tab
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABInPool);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.acceptWorkItemButton), "SMAB-T2635: Validate that accept work item button is visible in InPool tab");

		//Step6: Validate the buttons in On Hold tab
		objWorkItemHomePage.openTab(objWorkItemHomePage.TAB_On_Hold);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.markInProgress), "SMAB-T2635: Validate that mark in progress button is visible in On_Hold tab");

		//Step7: Validate the buttons in SubmittedforApproval tab
		objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABMySubmittedforApproval);
		softAssert.assertTrue(objLoginPage.verifyElementVisible(objWorkItemHomePage.WithdrawButton), "SMAB-T2635: Validate that WithdrawButton is present in SubmittedforApproval tab");

		objWorkItemHomePage.logout();
	}
}