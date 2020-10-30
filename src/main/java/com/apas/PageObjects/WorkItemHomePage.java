package com.apas.PageObjects;

import android.text.style.ClickableSpan;
import com.apas.Reports.ReportLogger;
import com.apas.generic.ApasGenericFunctions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class WorkItemHomePage extends Page {

    public final String TAB_IN_PROGRESS = "In Progress";
    public final String TAB_IN_POOL = "In Pool";
    public final String TAB_MY_SUBMITTED_FOR_APPROVAL = "";
    public final String TAB_NEED_MY_APPROVAL = "";
    public final String TAB_COMPLETED = "Completed";

    ApasGenericPage objApasGenericPage;
    ApasGenericFunctions objApasGenericFunctions;

    public WorkItemHomePage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
        objApasGenericPage = new ApasGenericPage(driver);
        objApasGenericFunctions = new ApasGenericFunctions(driver);
    }

    @FindBy(xpath = "//div[@data-key='success'][@role='alert']")
    public WebElement successAlert;

    @FindBy(xpath = "//label[contains(.,'Show RP')]//span[@class='slds-checkbox_faux_container']")
    public WebElement chkShowRP;

    @FindBy(xpath = "//button[text()='Accept Work Item']")
    public WebElement acceptWorkItemButton;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//a[@data-tab-name='Completed']")
    public WebElement dataTabCompleted;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//button[contains(.,'Mark Status as Complete')]")
    public WebElement markStatusAsCompleteButton;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//button[contains(.,'Mark as Current Status')]")
    public WebElement markAsCurrentStatusButton;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//a[@role='tab'][@data-label='Details']")
    public WebElement detailsTab;

    @FindBy(xpath = "//a[@role='tab'][@data-label='In Progress']")
    public WebElement inProgressTab;

    @FindBy(xpath = "//a[@role='tab'][@data-label='In Pool']")
    public WebElement inPoolTab;

    @FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//force-record-layout-item[contains(.,'Related Action')]//a[@target='_blank']")
    public WebElement reviewLink;

    @FindBy(xpath = "//a[@role='tab'][@data-label='Needs My Approval']")
    public WebElement needsMyApprovalTab;

    @FindBy(xpath = "//button[text()='Return']")
    public WebElement returnWorkItemButton;

    @FindBy(xpath = "//button[text() = 'Save']")
    public WebElement saveButton;

    @FindBy(xpath = "//label[text()='Returned Reason']//following-sibling::div//input")
    public WebElement returnedReasonTxtBox;

    public String linkedItemEFileIntakeLogs = "//flexipage-tab2[contains(@class,'slds-show')]//c-org_work-item-related-list[contains(.,'E File Intake Logs')]";

    public String relatedBuildingPermits = "//flexipage-tab2[contains(@class,'slds-show')]//c-org_work-item-related-list[contains(.,'Related Building Permits')]";


    /**
     * This method will return grid data from the work item home page tab passed in the parameter
     *
     * @param tabName : Tab name for which data needs to be fetched
     **/
    public HashMap<String, ArrayList<String>> getWorkItemData(String tabName) throws Exception {
        String xpath = "//a[@role='tab'][@data-label='" + tabName + "']";
        waitUntilElementIsPresent(xpath, 10);
        WebElement webElement = driver.findElement(By.xpath(xpath));
        Click(webElement);
        Thread.sleep(2000);
        return objApasGenericFunctions.getGridDataInHashMap(2);
    }

    /**
     * This method will open the work item passed in the parameter
     *
     * @param workItem: Work item number to be opened
     **/
    public void openWorkItem(String workItem) throws IOException {
        WebElement webElement = driver.findElement(By.xpath("//lightning-formatted-url//a[@title='" + workItem + "']"));
        javascriptClick(webElement);
    }

    /**
     * This method will open related action record linked with the work item
     *
     * @param workItem: Work item number linked with the item
     **/
    public void openRelatedActionRecord(String workItem) throws Exception {
        ReportLogger.INFO("Opening the imported file linked with work item : " + workItem);
        String xpath = "//a[@title='" + workItem + "']";
        waitUntilElementIsPresent(xpath, 15);
        waitForElementToBeClickable(driver.findElement(By.xpath(xpath)), 10);
        javascriptClick(driver.findElement(By.xpath(xpath)));
        Thread.sleep(3000);
        Click(detailsTab);
        scrollToBottom();
        javascriptClick(reviewLink);
    }

    /**
     * This method will accept the work item passed in the parameter
     *
     * @param workItem: Work item number to be accepted
     **/
    public void acceptWorkItem(String workItem) throws Exception {
        ReportLogger.INFO("Accepting the work item: " + workItem);
        WebElement webElementCheckBox = driver.findElement(By.xpath("//table//tr[contains(.,'" + workItem + "')]//span[@class='slds-checkbox_faux']"));
        scrollToElement(webElementCheckBox);
        Click(webElementCheckBox);
        scrollToElement(acceptWorkItemButton);
        Click(acceptWorkItemButton);
        waitForElementToBeVisible(successAlert, 20);
        waitForElementToDisappear(successAlert, 10);
    }

    /**
     * This method will completed the work item currently displayed on UI
     **/
    public String completeWorkItem() throws Exception {
        ReportLogger.INFO("Completing the work item");
        javascriptClick(dataTabCompleted);
        javascriptClick(markAsCurrentStatusButton);
        waitForElementToBeVisible(successAlert, 20);
        String messageText = successAlert.getText();
        waitForElementToDisappear(successAlert, 10);
        return messageText;
    }
    /**
     * This method will open record under Action link for the specified work item
     *
     * @param workItem: Work item number linked with the item
     **/
    public void openActionLink(String workItem) throws Exception {
        ReportLogger.INFO("Clicking on Action Link of the work item : " + workItem);
        String xpath = "//a[@title='" + workItem + "']//ancestor::th//following-sibling::td//a";
        waitUntilElementIsPresent(xpath, 15);
        waitForElementToBeClickable(driver.findElement(By.xpath(xpath)), 10);
        javascriptClick(driver.findElement(By.xpath(xpath)));
        Thread.sleep(4000);
    }

    /**
     * This method will reject the work item passed in the parameter
     *
     * @param workItem: Work item number to be returned
     **/
    public void returntWorkItem(String workItem) throws Exception {
        ReportLogger.INFO("Rejecting the work item: " + workItem);
        WebElement webElementCheckBox = driver.findElement(By.xpath("//table//tr[contains(.,'" + workItem + "')]//span[@class='slds-checkbox_faux']"));
        scrollToElement(webElementCheckBox);
        Click(webElementCheckBox);
        scrollToElement(returnWorkItemButton);
        Click(returnWorkItemButton);
        enter(returnedReasonTxtBox,"Test Reason");
        Click(saveButton);
        waitForElementToBeVisible(successAlert, 20);
        waitForElementToDisappear(successAlert, 10);
    }
}
