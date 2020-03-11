package com.apas.PageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class CityStratCodesPage extends Page {

	public CityStratCodesPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//a[@title = 'New']")
	public WebElement newButton;

	@FindBy(xpath = "//button[@title='Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//input[@title='Search County Strat Codes']")
	public WebElement countyStratCodeEditBox;

	@FindBy(xpath = "//span[text() = 'City Code']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement cityCodeDropDown;

	@FindBy(xpath = "//label/span[text()='City Strat Code']/../../input")
	public WebElement cityStratCodeEditBox;

	@FindBy(xpath = "//span[text() = 'Status']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement statusDropDown;

	@FindBy(xpath = "//div[@role='alert'][@data-key='success']")
	public WebElement successAlert;

	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;

	/**
	 * Description: This method will only enter the values of city strat code fiels on the application
	 * @param countyStratCode: County Strat Code
	 * @param cityCode: City Code
	 * @param cityStratCode: City Strat Code
	 * @param status: Status
	 */
	public void enterCityStratCodeDetails(String countyStratCode, String cityCode, String cityStratCode, String status) throws Exception {
		Click(newButton);
		enter(countyStratCodeEditBox,countyStratCode);
		Click(driver.findElement(By.xpath("//*[@role='option']//mark[text()='" + countyStratCode + "']")));
		Select(cityCodeDropDown,cityCode);
		enter(cityStratCodeEditBox,cityStratCode);
		Select(statusDropDown,status);
	}

	/**
	 * Description: This method will add a new city strat code
	 * @param countyStratCode: County Strat Code
	 * @param cityCode: City Code
	 * @param cityStratCode: City Strat Code
	 * @param status: Status
	 * @return : returns the text message of success alert
	 */
	public String addAndSaveCityStratcode(String countyStratCode, String cityCode, String cityStratCode, String status) throws Exception {
		enterCityStratCodeDetails(countyStratCode,cityCode,cityStratCode,status);
		Click(saveButton);
		waitForElementToBeVisible(successAlert,20);
		return getElementText(successAlertText);
	}
}
