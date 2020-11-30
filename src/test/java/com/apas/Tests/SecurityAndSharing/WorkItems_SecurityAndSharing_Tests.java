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
import com.apas.generic.ApasGenericFunctions;

public class WorkItems_SecurityAndSharing_Tests extends TestBase implements testdata, modules, users {
	private RemoteWebDriver driver;
	LoginPage objLoginPage;
	ApasGenericFunctions objApasGenericFunctions;
	WorkItemHomePage objWorkItemHomePage;
	SoftAssertion softAssert;
	ApasGenericPage objApasGenericPage;

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {
		driver = null;
		setupTest();
		driver = BrowserDriver.getBrowserInstance();
		objLoginPage = new LoginPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
		objWorkItemHomePage = new WorkItemHomePage(driver);
		objApasGenericPage=new ApasGenericPage(driver);
		softAssert = new SoftAssertion();
	}

	/**
	 * This method is to verify that supervisor is able to view 'Action' column only on 'Staff - In Progress' & 'Needs My Approval' tabs
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2085:verify that Verify supervisor is able to view 'Action' column only on 'Staff - In Progress' & 'Needs My Approval' tabs", dataProvider = "loginPrincipalUser", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual" })
	public void WorkItems_VerifySupervisor_ActionColumn(String loginUser) throws Exception {		
		// Step1: Login to the APAS application using the credentials passed through dataprovider (loginPrincipalUser)
		objApasGenericFunctions.login(loginUser);

		//Step2: Open Home Page
		objApasGenericFunctions.searchModule(modules.HOME);

		//Step 3: Navigate to tabs : Need My approval,In progress and verify that Action column is visible 
		objWorkItemHomePage.Click(objWorkItemHomePage.needsMyApprovalTab);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2085: Validation that 'ACTION' column is visible on Need my Approval tab");

		objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.actionColumnInProgressTab),
				"SMAB-T2085: Validation that 'ACTION' column is visible on In Progress tab");

		//Step 4: Navigate to tabs : In pool,On hold ,Submitted for Approval,Staff in progress,Staff on hold,Staff in pool,Completed and  verify that Action column is not visible 
		objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2085: Validation that 'ACTION' column is not visible on In pool tab");

		objApasGenericPage.openTab(objWorkItemHomePage.TAB_On_Hold);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2085: Validation that 'ACTION' column is not visible on On hold tab");

		objApasGenericPage.openTab(objWorkItemHomePage.TAB_MY_SUBMITTED_FOR_APPROVAL);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2085: Validation that 'ACTION' column is not visible on Submitted for Approval tab");

		objApasGenericPage.openTab(objWorkItemHomePage.TAB_StaffInProgress);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2085: Validation that 'ACTION' column is not visible on Staff in progress tab");

		objApasGenericPage.openTab(objWorkItemHomePage.TAB_StaffOnHold);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2085: Validation that 'ACTION' column is not visible on Staff on hold tab");

		objApasGenericPage.openTab(objWorkItemHomePage.TAB_StaffInPool);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2085: Validation that 'ACTION' column is not visible on Staff in pool tab");

		objApasGenericPage.openTab(objWorkItemHomePage.TAB_COMPLETED);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2085: Validation that 'ACTION' column is not visible on completed tab");

		objApasGenericFunctions.logout();

	}
	/**
	 * This method is to verify that staff member is able to view 'Action' column only on 'In Progress' tab
	 * @param loginUser
	 * @throws Exception
	 */
	@Test(description = "SMAB-T2086:Verify staff member is able to view 'Action' column only on 'In Progress' tab", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual"  })
	public void WorkItems_VerifyStaffUser_ActionColumn(String loginUser) throws Exception {
		// Step1: Login to the APAS application using the credentials passed through dataprovider (loginBPPBusinessAdmin)
		objApasGenericFunctions.login(loginUser);

		//Step2: Open Home Page
		objApasGenericFunctions.searchModule(modules.HOME);

		//Step 3: Navigate to tabs : In progress and verify that Action column is visible 
		objWorkItemHomePage.Click(objWorkItemHomePage.inProgressTab);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2086: Validation that 'ACTION' column is visible on In Progress tab");

		//Step 4: Navigate to tabs : In pool,On hold ,Submitted for Approval,Completed and  verify that Action column is not visible 
		objWorkItemHomePage.Click(objWorkItemHomePage.inPoolTab);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2086: Validation that 'ACTION' column is not visible on In pool tab");

		objApasGenericPage.openTab(objWorkItemHomePage.TAB_On_Hold);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2086: Validation that 'ACTION' column is not visible on On hold tab");

		objApasGenericPage.openTab(objWorkItemHomePage.TAB_MY_SUBMITTED_FOR_APPROVAL);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2086: Validation that 'ACTION' column is not visible on Submitted for Approval tab");

		objApasGenericPage.openTab(objWorkItemHomePage.TAB_COMPLETED);
		softAssert.assertTrue(objWorkItemHomePage.verifyElementNotVisible(objWorkItemHomePage.actionColumn),
				"SMAB-T2086: Validation that 'ACTION' column is not visible on completed tab");

		objApasGenericFunctions.logout();
	}

	
	@Test(description = "SMAB-T2033: verify that work pool supervisor is not able to mass transfer WI's with status 'submitted for approval', 'Approval-On Hold' and 'completed'", dataProvider = "loginBPPBusinessAdmin", dataProviderClass = DataProviders.class, groups = {
			"regression","work_item_manual"  })
	public void WorkItems_NoMassApproval(String loginUser) throws Exception {
		objApasGenericFunctions.login(loginUser);
		 
	    objApasGenericFunctions.searchModule(WORK_ITEM_MANAGEMENT_CONSOLE);
	    objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABMySubmittedforApproval);
	    softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.workItems),
	    		"SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'submitted for approval'");
	    softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.changeWorkPool),
	    		"SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'submitted for approval'");
	    softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.changeAsignee),
	    		"SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'submitted for approval'");
	    
	    objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABOnHold);
	    softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.workItems),
	    		"SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'Approval-On Hold'");
	    softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.changeWorkPool),
	    		"SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'Approval-On Hold'");
	    softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.changeAsignee),
	    		"SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'Approval-On Hold'");
	    
	    objWorkItemHomePage.Click(objWorkItemHomePage.lnkTABCompleted);
	    softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.workItems),
	    		"SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'completed'");
	    softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.changeWorkPool),
	    		"SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'completed'");
	    softAssert.assertTrue(objLoginPage.verifyElementNotVisible(objWorkItemHomePage.changeAsignee),
	    		"SMAB-T2033: Validate that work pool supervisor is not able to mass transfer WI's with status 'completed'");
	    
	    objApasGenericFunctions.logout();
	}
}
