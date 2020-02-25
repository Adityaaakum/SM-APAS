package com.apas.PageObjects;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class BppTrendPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);

	public BppTrendPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//input[@name='rollyear']")
	public WebElement rollYearDropdown;

	@FindBy(xpath = "//button[@title='Select']")
	public WebElement selectRollYearButton;

	@FindBy(xpath = "//button[@title='Calculate']")
	public WebElement calculateButton;

	@FindBy(xpath = "//button[@title='Calculate all']")
	public WebElement calculateAllButton;

	@FindBy(xpath = "//button[@title='ReCalculate all']")
	public WebElement reCalculateAllButton;

	@FindBy(xpath = "//button[@title='More Tabs']")
	public WebElement moreTabs;

	@FindBy(xpath = "//button[@name='btnSubApproval' and contains(.,'Submit for Approval')]")
	public WebElement submitForApprovalButton;

	/**
	 * Description: This will select the roll year from the drop down
	 */
	public void clickOnGivenRollYear(String rollYear) throws Exception {
		String xpathStr = "//div[contains(@id,'dropdown-element')]//span[contains(text(),'" + rollYear + "')]";
		WebElement element = waitForElementToBeClickable(xpathStr);
		element.click();
	}

	/**
	 * Description: This will return the list of table on the screen
	 * @return : List of visible tables on the screen
	 */
	public List<WebElement> getVisibleTables() throws Exception {
		String xpathStr = "//ul[@role='tablist']//li//a";
		List<WebElement> visibleTables = locateElements(xpathStr, 500);
		return visibleTables;
	}
	
	/**
	 * Description: This will select the table irrespective of hidden or displayed
	 * @param tableName: Name of the table
	 * @param isTableOutsideMoreTab: True/False if the table is hidden or not
	 */
	public void clickOnGivenTableName(String tableName, boolean isTableOutsideMoreTab) throws Exception {
		String xpathStr = null;
		if (isTableOutsideMoreTab) {
			xpathStr = "//a[contains(@data-label, '" + tableName + "')]";
		} else {
			xpathStr = "//span[contains(text(), '" + tableName + "')]";
		}
		WebElement givenTable = waitForElementToBeClickable(xpathStr);
		givenTable.click();
	}
	
	/**
	 * Description: This will calculate the index
	 * @param tableName: Name of the table
	 * @return : Status of the index data submission
	 */
	public String clickCalculateButton(String tableName) throws Exception {
		String xpathStr = "//lightning-tab[contains(@data-id, '" + tableName + "')]/slot/div";
		String currentMessage = waitForElementToBeClickable(xpathStr).getText();
		if(currentMessage.equalsIgnoreCase("Yet to be calculated")) {
			Click(calculateButton);	
			currentMessage = waitForElementToBeClickable(xpathStr).getText();
		}
		return currentMessage;
	}
	
}
