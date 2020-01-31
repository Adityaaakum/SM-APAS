package com.apas.PageObjects;

import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BppTrendPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);

	public BppTrendPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//one-app-launcher-header/button[@class = 'slds-button']")
	private WebElement appLauncher;

	@FindBy(xpath = "//input[contains(@placeholder, 'Search apps and items')]")
	private WebElement appLauncherSearchBox;

	@FindBy(xpath = "//input[@name='rollyear']")
	public WebElement rollYearDropdown;

	@FindBy(xpath = "//button[@title='Select']")
	public WebElement selectRollYearButton;

	@FindBy(xpath = "//button[@title='Calculate all']")
	public WebElement calculateAllButton;

	@FindBy(xpath = "//button[@title='ReCalculate all']")
	public WebElement reCalculateAllButton;

	@FindBy(xpath = "//button[contains(text(), 'More')]")
	public WebElement moreTab;

	@FindBy(xpath = "//button[@title='More Tabs']")
	public WebElement moreTabs;

	@FindBy(xpath = "//button[@name='btnSubApproval' and contains(.,'Submit for Approval')]")
	public WebElement submitForApprovalButton;

	public void clickAppLauncher() throws Exception {
		Click(appLauncher);
	}

	public void searchForApp(String appToSearch) throws Exception {
		enter(appLauncherSearchBox, appToSearch);
	}

	public void clickNavOptionFromDropDown(String navOption) throws Exception {
		String xpathString = "//a[contains(@data-label, '" + navOption + "')]//b[text() = '" + navOption + "']";
		WebElement navDropDownOption = driver.findElement(By.xpath(xpathString));
		Click(navDropDownOption);
	}

	public void clickRollYearDropDown() throws Exception {
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOf(rollYearDropdown));
		rollYearDropdown.click();
		//Click(rollYearDropdown);
	}

	public void clickOnGivenRollYear(String rollYear) throws Exception {
		String xpathString = "//div[contains(@id,'dropdown-element')]//span[contains(text(),'" + rollYear + "')]";
		Thread.sleep(2000);
		WebElement rollYearOption = driver.findElement(By.xpath(xpathString));
		Click(rollYearOption);
	}

	public void clickBtnSelect() throws Exception {
		Click(selectRollYearButton);
	}
	
	public void clickOnGivenTableName(String tableName) throws Exception {
		//List<String> tableNames = new ArrayList<String>();
		List<WebElement> tablesList = driver.findElements(By.xpath("//ul[@class='slds-tabs_scoped__nav']//li//span"));
		int sizeTablesList = tablesList.size();
		System.out.println("sizeTablesList: " + sizeTablesList);
		
		String xpathString = "//a[contains(@id, '" + tableName + "')]";
		//WebElement table = driver.findElement(By.xpath(xpathString));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
