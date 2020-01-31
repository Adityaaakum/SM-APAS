package com.apas.Tests;

import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.Test;

import com.apas.PageObjects.DisabledVetransPage;
import com.apas.PageObjects.EFileHomePage;
import com.apas.PageObjects.LoginPage;
import com.apas.TestBase.TestBase;

public class DisabledVeterans extends TestBase {

	// WebDriver driver;
	private RemoteWebDriver driver;
	LoginPage loginPageObject;
	DisabledVetransPage disabledVetObj;
	static EFileHomePage eFileobj;

	String apn = "";
	String ownerApplyingName = "";
	String ownerApplyingSSN = "";
	String nameOfVeteran = "";
	String veteranSSN = "";
	String emailAddress = "";
	String telephone = "";

	String Status = "";
	String applicationDate = "";
	String spouseName = "";
	String spouseSSN = "";
	String unmarriedSpouseOfDeceasedVeteran = "";
	String dateOfDeathOfDeceasedVeteran = "";
	String[] basisForClaim = { "", "", "", "", "" };
	String[] deaceasedVeteranQualification = { "", "", "", "", "" };

	/**
	 * function uploadEFile:- to upload an E-File
	 * 
	 * @param filetype
	 * @param source
	 * @param period
	 * @param fileLocation
	 * @throws Exception
	 */

	@Test(description = "To Create a New Exemption")
	public void createExemption(String filetype, String source, String period) throws Exception {
		loginPageObject = new LoginPage(driver);
		disabledVetObj = new DisabledVetransPage(driver);
		eFileobj = new EFileHomePage(driver);

		loginPageObject.loginToSandbox();
		eFileobj.searchApps("Exemption");
		disabledVetObj.newExemptionButton.click();

		// Now entering data into fields
		disabledVetObj.apn.sendKeys("apn");
		disabledVetObj.ownerApplyingName.sendKeys("owner name");
		disabledVetObj.ownerApplyingSSN.sendKeys("owner ssn");
		disabledVetObj.nameOfVeteran.sendKeys("veteran name");
		disabledVetObj.veteranSSN.sendKeys("vet ssn");
		disabledVetObj.emailAddress.sendKeys("email");
		disabledVetObj.telephone.sendKeys("telphone");
		disabledVetObj.applicationDate.sendKeys("app date");
		disabledVetObj.spouseName.sendKeys("spouse name");
		disabledVetObj.spouseSSN.sendKeys("spouse ssn");
		disabledVetObj.dateOfDeathOfVeteran.sendKeys("date");

		for (String s : basisForClaim) {
			driver.findElement(By
					.xpath("//div[contains(.,'Basis for Claim')]//div[contains(@id,'keyboard-interacton')]/following::div[1]/div/ul/li//span[contains(.,'"
							+ s + "')]"))
					.click();
			driver.findElement(By.xpath("")).click();
		}

		for (String s1 : deaceasedVeteranQualification) {
			driver.findElement(By
					.xpath("//div[contains(.,'Basis for Claim')]//div[contains(@id,'keyboard-interacton')]/following::div[1]/div/ul/li//span[contains(.,'"
							+ s1 + "')]"))
					.click();
			driver.findElement(By.xpath("")).click();
		}
		disabledVetObj.dateAquiredProperty.sendKeys("");
		disabledVetObj.effectiveDateOfUSDVA.sendKeys("");
	}
}
