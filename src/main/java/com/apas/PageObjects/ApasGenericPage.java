package com.apas.PageObjects;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.relevantcodes.extentreports.LogStatus;

import java.text.SimpleDateFormat;
import java.util.*;

public class ApasGenericPage extends Page {
	Logger logger = Logger.getLogger(LoginPage.class);

	public ApasGenericPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	@FindBy(xpath = "//one-app-launcher-header/button[contains(@class,'slds-button')] | //nav[@class='appLauncher slds-context-bar__icon-action']//div[@class='slds-icon-waffle']")
	public WebElement appLauncher;

	@FindBy(xpath = "//table[@role='grid']//thead/tr//th")
	public WebElement dataGrid;

	@FindBy(xpath = "//input[contains(@placeholder, 'Search apps and items')]")
	public WebElement appLauncherSearchBox;

	@FindBy(xpath = "//input[@placeholder='Search apps and items...']/..//button")
	public WebElement searchClearButton;

	@FindBy(xpath = "//div[@role='combobox']//div[@aria-label='Apps']/p")
	public WebElement appsListBox;

	@FindBy(xpath = "//div[@role='combobox']//div[@aria-label='Items']/p")
	public WebElement itemsListBox;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@role='button'][@title='Select List View']")
	public WebElement selectListViewButton;

	@FindBy(xpath = "//a[@role='option']//span[text()='All' or text()='All Active Parcels']")
	public WebElement selectListViewOptionAll;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//input[@placeholder='Search this list...']")
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

	@FindBy(xpath = "//div[@data-key='success'][@role='alert']")
	public WebElement successAlert;
	
	@FindBy(xpath = "//span[text()='Delete']")
	public WebElement deleteConfirmationPostDeleteAction;
	
	@FindBy(xpath = "//h2[@class='slds-truncate slds-text-heading_medium']")
	public WebElement popUpErrorMessageWeHitASnag;

	public String menuList = "//div[contains(@class,'uiMenuList--default visible positioned')]";

	@FindBy(xpath = "//lightning-spinner")
	public WebElement spinner;
	
	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;
	
	public String xpathSpinner = "//lightning-spinner";


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
	
	@FindBy(xpath = "//div[@class = 'slds-media__body slds-align-middle']//span[contains(@class, 'triggerLinkText selectedListView uiOutputText')]")
	public WebElement currenltySelectViewOption;
	
	@FindBy(xpath = "//a[@title = 'Select List View']")
	public WebElement selectListViewIcon;

	@FindBy(xpath = "//div[@class = 'scroller']//span[contains(@class,'virtualAutocompleteOptionText') and text() = 'All Imported E-File Building Permits']")
	public WebElement allImportedEfileBuildingPermitsOption;

	@FindBy(xpath = "//div[@class = 'scroller']//span[contains(@class,'virtualAutocompleteOptionText') and text() = 'All Manual Building Permits']")
	public WebElement allManualBuilingdPermitsOption;
	
	@FindBy(xpath = "//div[@class = 'scroller']//span[contains(@class,'virtualAutocompleteOptionText') and text() = 'Recently Viewed']")
	public WebElement recentlyViewedOption;
	
	@FindBy(xpath = "//div[@class = 'scroller']//span[contains(@class,'virtualAutocompleteOptionText') and text() = 'All']")
	public WebElement allOption;
	
	@FindBy(xpath = "//force-list-view-manager-pin-button//button[contains(@class, 'slds-button slds-button_icon')]//lightning-primitive-icon")
	public WebElement pinIcon;
	

	/**
	 * Description: This will click on the module name from the drop down
	 */
	public void clickNavOptionFromDropDown(String navOption) throws Exception {
		String xpathStr = "//a[contains(@data-label, '" + navOption + "')]//b[text() = '" + navOption + "']";
		WebElement drpDwnOption = waitForElementToBeClickable(20, xpathStr);
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
		String xpathStr = "//*[@role='option']//*[@title='" + value + "'] | //div[@title='" + value + "']";
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

	/** @Description: This method is to handle fields like Parcel or Strat Code
	 * by clicking on the web element, entering the provided string in textbox
	 * and then selects value from drop down
	 * @param element: WebElement for required field
	 * @param value: Like Roof Repair or Repairs for strat code field etc.
	 * @throws Exception
	 */
	public void searchAndSelectOptionFromDropDown(WebElement element, String value) throws Exception {
		enter(element, value);
		String xpathStr = "//div[@title='" + value.toUpperCase() + "'] | //mark[text() = '" + value + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 20);
		waitForElementToBeVisible(drpDwnOption, 10);
		drpDwnOption.click();
	}

	/**
	 * @Description: This method is to handle fields like Permit City Code or Processing Status
	 * by clicking the web element and then selecting the given value from drop down
	 * @param element: WebElement for required field
	 * @param value: Like 'Process' or 'No Process' for Processing Status field etc.
	 * @throws Exception
	 */
	public void selectOptionFromDropDown(WebElement element, String value) throws Exception {
		Click(element);
		String xpathStr = "//div[contains(@class, 'left uiMenuList--short visible positioned')]//a[text() = '" + value + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 30);
		waitForElementToBeClickable(drpDwnOption, 10);
		drpDwnOption.click();
	}
	
	/**
	 * Description: This method will fetch the current URL and process it to get the Record Id
	 * @param driver: Driver Instance
	 * @return : returns the Record Id
	 */
	
	public String getCurrentRecordId(RemoteWebDriver driver, String Mod) throws Exception {
		wait.until(ExpectedConditions.urlContains("/view"));
		String url = driver.getCurrentUrl();
		String recordId = url.split("/")[6];
		driver.navigate().refresh();
		ReportLogger.INFO(Mod + " record id - " + recordId);
		Thread.sleep(1000);
		return recordId;
	
	}
	
	
	/**
	 * @description: Clicks on the show more link displayed against the given entry
	 * @param entryDetails: Name of the entry displayed on grid which is to be accessed
	 * @throws Exception
	 */
	public void clickShowMoreButton(String modRecordName) throws Exception {		
		Thread.sleep(1000);
		String xpathStr = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table//tbody/tr//th//a[text() = '"+ modRecordName +"']//parent::span//parent::th//following-sibling::td//a[@role = 'button']";
		WebElement modificationsIcon = locateElement(xpathStr, 30);
		clickAction(modificationsIcon);
		ReportLogger.INFO(modRecordName + " record exist and user is able to click Show More button against it");
		Thread.sleep(1000);
	}
	
	/**
	 * Description: This method will click 'Show More Button' on the Screen
	 * @param screenName: Screen Name
	 * @param modRecordName: Record Number
	 * @param action: Action user want to perform - Edit/Delete
	 * @return: Boolean value
	 */
	public Boolean clickShowMoreButtonAndAct(String modRecordName, String action) throws Exception { 
		Boolean flag=false;
		clickShowMoreButton(modRecordName);
		String xpathStr = "//li//a[@title='" + action + "']//div[text()='" + action + "']";
		WebElement actionElement = locateElement(xpathStr, 30);
			if (actionElement != null){
					clickAction(actionElement);
					ReportLogger.INFO("User is able to click " + action + " option for " + modRecordName + " record");
					Thread.sleep(2000);
					flag=true;
					if (action.equals("Delete")){
						Click(deleteConfirmationPostDeleteAction);
						ReportLogger.INFO(action + modRecordName + " record");
						Thread.sleep(2000);
					}
			 }
		return flag;
	}	
	
	
	/**
	 * Description: This method will enter date
	 * @param element: locator of element where date need to be put in
	 * @param date: date to enter
	 */

	public void enterDate(WebElement element, String date) throws Exception {
		Click(element);
		selectDateFromDatePicker(date);
	}
	
	/**
	 * Description: This method will return element from the pop-up error message that appear on Detail page
	 * @param value: field name
	 */
	
	public WebElement returnElemOnPopUpScreen(String value) throws Exception {
		String xpathStr = "";
		if (value.contains("Claimant's") || value.contains("Veteran's")) {
			xpathStr = "//a[contains(text()," + "\"" + value + "\"" + ")]";
		}else{
			xpathStr = "//a[contains(text(),'" + value + "')]";
		}
		WebElement elementOnPopUp = locateElement(xpathStr, 200);
		return elementOnPopUp;
	}
	
}
