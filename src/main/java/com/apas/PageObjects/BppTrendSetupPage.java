package com.apas.PageObjects;

import java.util.List;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class BppTrendSetupPage extends Page {

	Logger logger = Logger.getLogger(LoginPage.class);

	public BppTrendSetupPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//one-app-launcher-header/button[@class = 'slds-button']")
	private WebElement appLauncher;

	@FindBy(xpath = "//input[contains(@placeholder, 'Search apps and items')]")
	private WebElement appLauncherSearchBox;

	@FindBy(xpath = "//a[@data-label = '$navOptionToSelect']")
	private WebElement navDropDownOption;

	@FindBy(xpath = "//span[contains(@title, '$selectedNavOption')]")
	private WebElement textForSelectedNavOption;

	@FindBy(xpath = "//span[@class='slds-truncate'][contains(text(),'$headerOptionToSelect')]")
	private List<WebElement> headerOptions;

	@FindBy(xpath = "//span[contains(@class, 'uiOutputText') and text() = '$selectedHeaderOption']")
	private WebElement textForSelectedHeaderOption;

	@FindBy(xpath = "//label[text() = 'Roll Year']/following-sibling::div//lightning-base-combobox//div[@role = 'none']")
	private WebElement dropDownBoxRollYear;

	@FindBy(xpath = "//label[text() = 'Roll Year']/following-sibling::div//lightning-base-combobox//div[@role = 'listbox']//span[text() = '$rollYear']")
	private WebElement rollYearOption;

	@FindBy(xpath = "//button[@name='btnGetRollYearData']")
	private WebElement btnSelectRollYear;

	@FindBy(xpath = "//a[contains(text(), '$tableName')]")
	private WebElement bppTrendTable;

	@FindBy(xpath = "//button[text() = 'More']")
	private WebElement moreBtn;

	@FindBy(xpath = "//li[@class='slds-tabs_scoped__item slds-tabs_scoped__overflow-button']//button[contains(text(),'More')]/following-sibling::div//span[text() = '$tableName']")
	private WebElement tableName;

	/**
	 * Description: This will return the text of the selected option
	 * @return : Text of the app
	 */
	public String getSelectedAppText(String selectedNavOption) {
		String actualAppText = "Bad element requested!! Either it not present or not visible/enabled.";
		boolean isElementPresent = verifyElementEnabled(textForSelectedNavOption);
		if (isElementPresent) {
			actualAppText = textForSelectedNavOption.getText().trim();
		}
		return actualAppText;
	}

	/**
	 * Description: This will click on the header text
	 */
	public void clickHeaderOption(String headerOptionToSelect) throws Exception {
		if (headerOptions.size() == 1) {
			Click(headerOptions.get(0));
		} else {
			for (WebElement headerOption : headerOptions) {
				String innerTextCurrentElement = headerOption.getText().trim();
				if (innerTextCurrentElement.equalsIgnoreCase(headerOptionToSelect)) {
					Click(headerOption);
				}
			}
		}
	}

	/**
	 * Description: This will return the text of the header
	 * @return : Text of the header
	 */
	public String getSelectedHeaderText(String selectedHeaderOption) {
		String actualHeaderText = "Bad element requested!! Either it not present or not visible/enabled.";
		boolean isElementPresent = verifyElementEnabled(textForSelectedHeaderOption);
		if (isElementPresent) {
			actualHeaderText = textForSelectedHeaderOption.getText().trim();
		}
		return actualHeaderText;
	}
}
