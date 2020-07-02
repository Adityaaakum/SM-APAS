package com.apas.PageObjects;

import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;

public class ParcelsPage extends Page {

	public ParcelsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//p[text()='Primary Situs']/../..//force-hoverable-link")
	public WebElement linkPrimarySitus;

	@FindBy(xpath = "//*[text()='More']")
	public WebElement moratab;

	
	/**
	 * Description: This method will open the parcel with the APN passed in the parameter
	 * @param APN: Value in the APN column
	 */
	public void openParcel(String APN) throws IOException, InterruptedException {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening the parcel with APN : " + APN);
		Click(driver.findElement(By.xpath("//a[@title='" + APN + "']")));
		Thread.sleep(2000);
	}
}
