package com.apas.Tests.SecurityAndSharing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apas.PageObjects.BppTrendSetupPage;
import com.apas.config.BPPTablesData;
import com.apas.config.users;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.server.handler.DeleteSession;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import com.apas.Assertions.SoftAssertion;
import com.apas.BrowserDriver.BrowserDriver;
import com.apas.DataProviders.DataProviders;
import com.apas.PageObjects.AsseseePage;
import com.apas.PageObjects.BppTrendPage;
import com.apas.PageObjects.BuildingPermitPage;
import com.apas.PageObjects.Page;
import com.apas.TestBase.TestBase;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;

public class Assesee_SecurityAndSharing_Test extends TestBase {
	private RemoteWebDriver driver;
	Page objPage;
	AsseseePage objAsseseePage;
	SoftAssertion softAssert;
	SalesforceAPI objSalesforceAPI;
	SoftAssert objSoftAssert;
	Util objUtil = new Util();

	@BeforeMethod(alwaysRun = true)
	public void beforeMethod() throws Exception {

		setupTest();
		driver = BrowserDriver.getBrowserInstance();

		objPage = new Page(driver);
		objAsseseePage = new AsseseePage(driver);
		softAssert = new SoftAssertion();
		objSalesforceAPI = new SalesforceAPI();
		objSoftAssert = new SoftAssert();

	}

	@Test(description = "SMAB-T2991:Verify user is able to create, edit and update the Assessee records", groups = {
			"Assessees",
			"Regression" }, dataProvider = "loginCIOuser", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void Assessee_VerifyCreateEditUpdateAssesseeRecords(String loginUser) throws Exception {

		String asseseeCreationData = testdata.ASSESEE_DATA;
		Map<String, String> hashAsseseeCreationData = objUtil.generateMapFromJsonFile(asseseeCreationData,
				"DataToCreateNewAssesee");

		String queryAPN = "Select Id  From Account where" + " FirstName='" + hashAsseseeCreationData.get("First Name")
				+ "' and " + " LastName='" + hashAsseseeCreationData.get("Last Name or Name") + "'";

		objSalesforceAPI.delete("Account", queryAPN);

		// Step2: Login to the APAS application using the given user
		objAsseseePage.login(loginUser);
		objAsseseePage.searchModule(modules.ASSESSEES);

		objAsseseePage.waitForElementToBeInVisible(objAsseseePage.newButtonText, 10);
		objAsseseePage.Click(objAsseseePage.getButtonWithText(objAsseseePage.newButtonText));
		objAsseseePage.createNewAssesee(hashAsseseeCreationData);
		objAsseseePage.Click(objAsseseePage.detailsTab);

		softAssert.assertEquals(
				hashAsseseeCreationData.get("First Name") + " " + hashAsseseeCreationData.get("Last Name or Name"),
				objAsseseePage.getFieldValueFromAPAS("Assessee Name", ""), "SMAB-T2991: Validate Assessee Name");

		softAssert.assertEquals(hashAsseseeCreationData.get("Email"), objAsseseePage.getFieldValueFromAPAS("Email", ""),
				"SMAB-T2991: Validate Assessee Email Address");

		String ssnFieldValue = objAsseseePage.getFieldValueFromAPAS("SSN", "");
		softAssert.assertEquals(
				(hashAsseseeCreationData.get("SSN").substring(hashAsseseeCreationData.get("SSN").length() - 4)),
				(ssnFieldValue.substring(ssnFieldValue.length() - 4)), "SMAB-T2991: Validate SSN");

		softAssert.assertEquals(hashAsseseeCreationData.get("Care Of"),
				objAsseseePage.getFieldValueFromAPAS("Care Of", ""), "SMAB-T2991: Validate Assessee Care Of");

		softAssert.assertEquals(hashAsseseeCreationData.get("Phone"),
				objAsseseePage.getFieldValueFromAPAS("Phone", "Contact Information"),
				"SMAB-T2991: Validate Assessee Phone Number");

		objAsseseePage.Click(objAsseseePage.editButton);

		objPage.waitForElementToBeVisible(objAsseseePage.visibleEditpopUp, 10);

		String updatedValue = objAsseseePage.updatewAssesee("careOfUpdated");
		objAsseseePage.Click(objAsseseePage.detailsTab);
		softAssert.assertEquals(updatedValue, objAsseseePage.getFieldValueFromAPAS("Care Of", ""),
				"SMAB-T2991: Validate Assessee Care Of");

		objAsseseePage.logout();

	}

	@Test(description = "SMAB-T2993:Verify user is not able to delete the Assessee records", groups = { "Assessees",
			"Regression" }, dataProvider = "loginMappingUser", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void Assessee_VerifyUserCannotCreateorUpdateorDeleteAssesseeRecords(String loginUser) throws Exception {

		objAsseseePage.login(loginUser);
		objAsseseePage.searchModule(modules.ASSESSEES);

		softAssert.assertTrue(!objAsseseePage.verifyElementVisible(objAsseseePage.newButtonText),
				"SMAB-T2994: new button should not be visible to other users");

		Thread.sleep(1000);
		objAsseseePage.displayRecords("All Assessees");
		String queryAPN = "Select FirstName,LastName From Account limit 1";

		HashMap<String, ArrayList<String>> responseAPNDetails = objSalesforceAPI.select(queryAPN);
		String firstName = responseAPNDetails.get("FirstName").get(0);
		String lastName = responseAPNDetails.get("LastName").get(0);
		String name = firstName + " " + lastName;
		objAsseseePage.searchRecords(name);
		Thread.sleep(1000);

		softAssert.assertTrue(!objAsseseePage.clickShowMoreButtonAndAct(name, "Edit"),
				"SMAB-T2994: edit button should not be visible to other users");

		softAssert.assertTrue(!objAsseseePage.clickShowMoreButtonAndAct(name, "Delete"),
				"SMAB-T2993: delete button should not be visible to other users");

		objAsseseePage.logout();

	}

	@Test(description = "SMAB-T2991:Verify user is able to create, edit and update the Assessee records", groups = {
			"Assessees",
			"Regression" }, dataProvider = "loginSystemAdmin", dataProviderClass = DataProviders.class, alwaysRun = true)
	public void Assessee_VerifyCreateEditUpdateAgencyAssesseeRecords(String loginUser) throws Exception {

		String asseseeCreationData = testdata.ASSESEE_DATA;
		Map<String, String> hashAsseseeCreationData = objUtil.generateMapFromJsonFile(asseseeCreationData,
				"DataToCreateNewAssesee");

		String queryAPN = "Select Id  From Account where" + " FirstName='" + hashAsseseeCreationData.get("First Name")
				+ "' and " + " LastName='" + hashAsseseeCreationData.get("Last Name or Name") + "'";

		objSalesforceAPI.delete("Account", queryAPN);

		// Step2: Login to the APAS application using the given user
		objAsseseePage.login(loginUser);
		objAsseseePage.searchModule(modules.ASSESSEES);

		objAsseseePage.waitForElementToBeInVisible(objAsseseePage.newButtonText, 10);
		objAsseseePage.Click(objAsseseePage.getButtonWithText(objAsseseePage.newButtonText));
		objAsseseePage.createNewAgency(hashAsseseeCreationData);
		objAsseseePage.Click(objAsseseePage.detailsTab);

		softAssert.assertEquals(
				hashAsseseeCreationData.get("First Name") + " " + hashAsseseeCreationData.get("Last Name or Name"),
				objAsseseePage.getFieldValueFromAPAS("Assessee Name", ""), "SMAB-T2991: Validate Agency Name");

		softAssert.assertEquals(hashAsseseeCreationData.get("Email"), objAsseseePage.getFieldValueFromAPAS("Email", ""),
				"SMAB-T2991: Validate Agency Email Address");

		softAssert.assertEquals(hashAsseseeCreationData.get("Type"), objAsseseePage.getFieldValueFromAPAS("Type", ""),
				"SMAB-T2991: Validate Agency Type");

		softAssert.assertEquals(hashAsseseeCreationData.get("Care Of"),
				objAsseseePage.getFieldValueFromAPAS("Care Of", ""), "SMAB-T2991: Validate Agency Care Of");

		softAssert.assertEquals(hashAsseseeCreationData.get("Phone"),
				objAsseseePage.getFieldValueFromAPAS("Phone", "Contact Information"),
				"SMAB-T2991: Validate Agency Phone Number");

		objAsseseePage.Click(objAsseseePage.editButton);

		objPage.waitForElementToBeVisible(objAsseseePage.visibleEditpopUp, 10);

		String updatedValue = objAsseseePage.updatewAssesee("careOfUpdated");
		objAsseseePage.Click(objAsseseePage.detailsTab);
		softAssert.assertEquals(updatedValue, objAsseseePage.getFieldValueFromAPAS("Care Of", ""),
				"SMAB-T2991: Validate Agency Care Of");

		objAsseseePage.logout();

	}

}
