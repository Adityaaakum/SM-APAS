package com.apas.PageObjects;

import java.io.IOException;
import java.util.ArrayList;
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

	@FindBy(xpath = "//span[contains(@class, 'triggerLinkText selectedListView') and text() = 'All']")
	private WebElement listViews;

	@FindBy(xpath = "//div[@class = 'scroller']//span[contains(@class,'virtualAutocompleteOptionText') and text() = '$listViewToSelect']")
	private WebElement listViewOptions;

	@FindBy(xpath = "//button[@class='slds-button slds-button_icon']//lightning-primitive-icon")
	private WebElement pinIcon;

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

	public List<String> tableNamesVisibleOnPage(List<String> listOfTablesToValidate) {
		List<String> listOfVisibleTablesOnPage = new ArrayList<String>();
		for (String currentTableName : listOfTablesToValidate) {
			String a = "";
		}
		return listOfVisibleTablesOnPage;
	}

	public List<String> tableNamesVisibleInMoreDrpDown(List<String> listOfTablesToValidate) {
		List<String> listOfVisibleTablesInDrpDown = new ArrayList<String>();
		return listOfVisibleTablesInDrpDown;
	}

	public void calculateGivenBppTrendTableData(String tableName) {
		boolean isElemDisplayed = new Page(driver).verifyElementVisible(bppTrendTable);
		if (isElemDisplayed) {
			// Click calculate button
		} else {
			// Logic to click "More" button. Then retrieve all table names and
			// inside for each click the one which is given. Then now check this
			// element on page and then click
			// calculate button.
		}
	}

	// ********* Methods to perform action on page objects *********
	public void clickAppLauncher() throws Exception {
		Click(appLauncher);
	}

	public void searchForApp(String appToSearch) {
		try {
			//enter(appLauncherSearchBox, appToSearch);
			Thread.sleep(5000);
			appLauncherSearchBox.click();
			appLauncherSearchBox.clear();
			appLauncherSearchBox.sendKeys(appToSearch);
		} catch (Exception ex) {

		}
	}

	public void clickNavOptionFromDropDown(String navOptionToSelect) throws Exception {
		try {
			Click(navDropDownOption);
		} catch (Exception ex) {

		}
	}

	public String getSelectedAppText(String selectedNavOption) {
		String actualAppText = "Bad element requested!! Either it not present or not visible/enabled.";
		boolean isElementPresent = verifyElementEnabled(textForSelectedNavOption);
		if (isElementPresent) {
			actualAppText = textForSelectedNavOption.getText().trim();
		}
		return actualAppText;
	}

	public String clickHeaderOption(String headerOptionToSelect) throws Exception {
		String headerOptionText = null;
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
		return headerOptionText;
	}

	public String getSelectedHeaderText(String selectedHeaderOption) {
		String actualHeaderText = "Bad element requested!! Either it not present or not visible/enabled.";
		boolean isElementPresent = verifyElementEnabled(textForSelectedHeaderOption);
		if (isElementPresent) {
			actualHeaderText = textForSelectedHeaderOption.getText().trim();
		}
		return actualHeaderText;
	}

	public void clickListViewDropDown() throws IOException {
		Click(listViews);
	}

	public void selectListViewOption(String listViewToSelect) throws IOException {
		Click(listViewOptions);
	}

	public void clickPinIcon() throws Exception {
		Click(pinIcon);
	}

	public void clickRollYearTxtBox() throws Exception {
		Click(dropDownBoxRollYear);
	}

	public void clickRollYearOption(String rollYear) throws Exception {
		Click(dropDownBoxRollYear);
	}

	public void clickBtnRollYearSelect() throws Exception {
		Click(dropDownBoxRollYear);
	}
}
