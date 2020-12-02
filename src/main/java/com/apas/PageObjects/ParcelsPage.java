package com.apas.PageObjects;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.Util;
import com.apas.config.testdata;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParcelsPage extends ApasGenericPage {
	Util objUtil;
	public ParcelsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();

	}

	@FindBy(xpath = "//p[text()='Primary Situs']/../..//force-hoverable-link")
	public WebElement linkPrimarySitus;

	@FindBy(xpath = "//li[not(contains(@style,'visibility: hidden'))]//*[@title='More Tabs']")
	public WebElement moretab;

	@FindBy(xpath = "//*[@role='menuitem' and contains(.,'Exemptions')]")
	public WebElement exemptionRelatedList;

	@FindBy(xpath = "//*[contains(@class,'windowViewMode-normal')]//div[text()='Select Option']//following::select")
	public WebElement selectOptionDropDownComponentsActionsModal;

	@FindBy(xpath = "//*[contains(@class,'modal-container')]//button[contains(text(),'Next')]")
	public WebElement nextButtonComponentsActionsModal;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-norma')]//label[contains(text(),'Work Item Type')]//following::input")
	public WebElement workItemTypeDropDownComponentsActionsModal;

	@FindBy(xpath = "//input[@name='Reference']")
	public WebElement referenceInputTextBoxComponentActionModal;

	@FindBy(xpath = "//input[@name='Description']")
	public WebElement descriptionInputTextBoxComponentActionModal;

	@FindBy(xpath = "//*[contains(@class,'windowViewMode-normal')]//div[text()='Priority']//following::select")
	public WebElement priorityDropDownComponentsActionsModal;

	@FindBy(xpath = "//*[contains(@class,'windowViewMode-normal')]//div[text()='Work Item Routing']//following::select")
	public WebElement workItemRoutingDropDownComponentsActionsModal;

	public String componentActionsButtonText = "Component Actions";
	public String actionsDropDownLabel = "Actions";

	/**
	 * Description: This method will open the parcel with the APN passed in the
	 * parameter
	 * 
	 * @param APN: Value in the APN column
	 */
	public void openParcel(String APN) throws IOException, InterruptedException {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening the parcel with APN : " + APN);
		Click(driver.findElement(By.xpath(
				"//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@title='"
						+ APN + "']")));
		Thread.sleep(2000);
	}
	/**
	 * @Description: This method will fill all the fields in manual work item modal and create a work item
	 * @param dataMap: A data map which contains data to create work item
	 * @throws Exception
	 */
	public String createWorkItem(Map<String, String> dataMap) throws Exception {
		String workItemType = dataMap.get("Work Item Type");
		String actions = dataMap.get("Actions");
		String reference = dataMap.get("Reference");
		String description = dataMap.get("Description");
		String priority = dataMap.get("Priority");
		String workItemRouting = dataMap.get("Work Item Routing");

		Click(getButtonWithText(componentActionsButtonText));
		waitForElementToBeVisible(selectOptionDropDownComponentsActionsModal);
		selectOptionFromDropDown(selectOptionDropDownComponentsActionsModal, "Create Work Item");
		Click(nextButtonComponentsActionsModal);
		waitForElementToBeVisible(workItemTypeDropDownComponentsActionsModal);
		selectOptionFromDropDown(workItemTypeDropDownComponentsActionsModal, workItemType);
		selectOptionFromDropDown(actionsDropDownLabel, actions);
		enter(referenceInputTextBoxComponentActionModal, reference);
		enter(descriptionInputTextBoxComponentActionModal, description);
		selectOptionFromDropDown(priorityDropDownComponentsActionsModal, priority);
		selectOptionFromDropDown(workItemRoutingDropDownComponentsActionsModal, workItemRouting);
		Click(nextButtonComponentsActionsModal);
		String parentwindow = driver.getWindowHandle();
	    switchToNewWindow(parentwindow);
	    waitForElementToBeVisible(workItemNumberDetailView);
	    String workItemNumber= getElementText(workItemNumberDetailView);
			
		ReportLogger.INFO("Work item created is " + workItemNumber  );
		
		return workItemNumber;
	}
}
