package com.apas.PageObjects;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.text.SimpleDateFormat;
import java.util.*;

public class ApasGenericPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);

	public ApasGenericPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//one-app-launcher-header/button[@class = 'slds-button']")
	public WebElement appLauncher;
	
	@FindBy(xpath = "//input[contains(@placeholder, 'Search apps and items')]")
	public WebElement appLauncherSearchBox;

	@FindBy(xpath = "//input[@placeholder='Search apps and items...']/..//button")
	public WebElement searchClearButton;

	@FindBy(xpath = "//div[@role='combobox']//div[@aria-label='Apps']/p")
	public WebElement appsListBox;

	@FindBy(xpath = "//div[@role='combobox']//div[@aria-label='Items']/p")
	public WebElement itemsListBox;

	@FindBy(xpath = "//a[@role='button'][@title='Select List View']")
	public WebElement selectListViewButton;

	@FindBy(xpath = "//a[@role='option']//span[text()='All' or text()='All Active Parcels']")
	public WebElement selectListViewOptionAll;

	@FindBy(xpath = "//input[@placeholder='Search this list...']")
	public WebElement searchListEditBox;

	@FindBy(xpath = "//div[@data-aura-class='forceSearchDesktopHeader']/div[@data-aura-class='forceSearchInputEntitySelector']//input")
	public WebElement globalSearchListDropDown;

	@FindBy(xpath = "//div[@data-aura-class='forceSearchDesktopHeader']/div[@data-aura-class='forceSearchInputDesktop']//input")
	public WebElement globalSearchListEditBox;

	@FindBy(xpath = "//*[@class='countSortedByFilteredBy']")
	public WebElement countSortedByFilteredBy;

	@FindBy(xpath = "//button[@title='Cancel']")
	public WebElement cancelButton;

	@FindBy(xpath = "//button[@title='Close']")
	public WebElement closeButton;

	@FindBy(xpath = "//button[@title='Close this window']")
	public WebElement crossButton;

	/*	Sikander Bhambhu:
	 *	Next 7 locators are for handling date picker
	 * */

	@FindBy(xpath = "//div[contains(@class, 'visible DESKTOP uiDatePicker')]")
	public WebElement datePicker;

	@FindBy(xpath = "//select[contains(@class, 'select picklist')]")
	public WebElement yearDropDown;

	@FindBy(xpath = "//a[@class='navLink prevMonth']")
	public WebElement prevMnth;

	@FindBy(xpath = "//a[@class='navLink nextMonth']")
	public WebElement nextMnth;

	@FindBy(xpath = "//span[(contains(@class, 'uiDayInMonthCell')) and (not (contains(@class, 'nextMonth '))) and (not (contains(@class, 'prevMonth ')))]")
	public List <WebElement> dates;

	@FindBy(xpath = "//h2[@class = 'monthYear']")
	public WebElement visibleMonth;

	@FindBy(xpath = "//button[text() = 'Today']")
	public WebElement currentDate;

	/**
	 * Description: This will click on the module name from the drop down
	 */
	public void clickNavOptionFromDropDown(String navOption) throws Exception {
		String xpathStr = "//a[contains(@data-label, '" + navOption + "')]//b[text() = '" + navOption + "']";
		WebElement drpDwnOption = waitForElementToBeClickable(xpathStr);
		drpDwnOption.click();
	}

	/** @Description: This method is to handle fields like Parcel or Strat Code
	 * by clicking on the web element, entering the provided string in textbox
	 * and then selects value from drop down
	 * @param element: WebElement for required field
	 * @param value: Like Roof Repair or Repairs for strat code field etc.
	 * @throws Exception
	 */
	public void searchAndSelectFromDropDown(WebElement element, String value) throws Exception {
		enter(element, value);
		String xpathStr = "//*[@role='option']//div[@title='" + value + "']";
		Click(driver.findElement(By.xpath(xpathStr)));
	}

	/**
	 * @Description: This method selects year, month and date from date picker / calender
	 * @param expctdDate: Accepts date in mm/dd/yyyy format
	 * @throws Exception
	 */
	public void selectDateFromDatePicker(String expctdDate) throws Exception {
		final Map<String, String> monthMapping = new HashMap<String, String>();
		monthMapping.put("01", "January");
		monthMapping.put("02", "February");
		monthMapping.put("03", "March");
		monthMapping.put("04", "April");
		monthMapping.put("05", "May");
		monthMapping.put("06", "June");
		monthMapping.put("07", "July");
		monthMapping.put("08", "August");
		monthMapping.put("09", "September");
		monthMapping.put("10", "October");
		monthMapping.put("11", "November");
		monthMapping.put("12", "December");

		final String[] monthsArr = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
		final List<String> monthsList = new ArrayList<>(Arrays.asList(monthsArr));

		Date presentDate = new Date();
		String formattedPresentDate = new SimpleDateFormat("MM/dd/yyyy").format(presentDate);
		Date dt = new SimpleDateFormat("MM/dd/yyyy").parse(expctdDate);
		String formattedExpctdDate = new SimpleDateFormat("MM/dd/yyyy").format(dt);

		if(formattedPresentDate.equals(formattedExpctdDate)) {
			Click(currentDate);
		} else {
			String[] dateArray = formattedExpctdDate.toString().split("/");
			String yearToSelect = dateArray[2];
			String monthToSelect = monthMapping.get(dateArray[0]);
			String dateToSelect;
			if(dateArray[1].startsWith("0")) {
				dateToSelect = dateArray[1].substring(1);
			} else {
				dateToSelect = dateArray[1];
			}

			Select select = new Select(waitForElementToBeVisible(yearDropDown));
			select.selectByValue(yearToSelect);

			WebElement visibleMnth = waitForElementToBeVisible(visibleMonth);
			String visibleMonthTxt = visibleMnth.getText().toLowerCase();
			visibleMonthTxt = visibleMonthTxt.substring(0, 1).toUpperCase() + visibleMonthTxt.substring(1).toLowerCase();

			int counter = 0;
			int indexOfDefaultMonth = monthsList.indexOf(visibleMonthTxt);
			int indexOfMonthToSelect = monthsList.indexOf(monthToSelect);
			int counterIterations = (Math.abs(indexOfDefaultMonth - indexOfMonthToSelect));

			while(!visibleMonthTxt.equalsIgnoreCase(monthToSelect) || counter > counterIterations) {
				if(indexOfMonthToSelect < indexOfDefaultMonth) {
					waitForElementToBeVisible(prevMnth).click();
				} else {
					waitForElementToBeVisible(nextMnth).click();
				}
				visibleMonthTxt = waitForElementToBeVisible(visibleMonth).getText();
				counter++;
			}

			for(WebElement date : dates) {
				String currentDate = date.getText();
				if(currentDate.equals(dateToSelect)) {
					date.click();
					break;
				}
			}
		}
	}

}
