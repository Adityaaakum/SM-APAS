package com.apas.PageObjects;

import com.apas.Utils.DateUtil;
import com.apas.Utils.PasswordUtils;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.modulesObjectName;

import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.relevantcodes.extentreports.LogStatus;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.*;
import java.util.List;

public class ApasGenericPage extends Page {

	LoginPage objLoginPage;
	SalesforceAPI objSalesforceAPI = new SalesforceAPI();
	Util objUtil = new Util();
	JSONObject jsonObject= new JSONObject();
	
	public ApasGenericPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objLoginPage = new LoginPage(driver);
	}

	public String commonXpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]";
	public String tabDetails = "Details";
	public String tabRelated = "Related";
	public String tabLinkedItems = "Linked Items";
	public String columnInGrid =commonXpath+"//a[contains(@class,'toggle')]//span[text()='columnName']";
	public String deleteButtonText ="Delete";


	@FindBy(xpath = "//button[@title='Close error dialog']")
	public WebElement crossIcon;

	@FindBy(xpath = "//button[contains(@class,'page-error-button')]")
	public WebElement pageErrorButton;

	@FindBy(xpath = "//*[contains(@class,'forceFormPageError')]")
	public WebElement pageError;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@title = 'New']")
	public WebElement newButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@title = 'Edit']")
	public WebElement editButton;

	@FindBy(xpath = "//button[text()='Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//div[contains(.,'App Launcher')]//*[@class='slds-icon-waffle']")
	public WebElement appLauncher;
	
	@FindBy(xpath = "//table[@role='grid']//thead/tr//th")
	public WebElement dataGrid;

	@FindBy(xpath = "//input[contains(@placeholder, 'Search apps and items...')]|//input[@placeholder='Search apps or items...']")
	public WebElement appLauncherSearchBox;

	@FindBy(xpath = "//input[@placeholder='Search apps and items...']/..//button")
	public WebElement searchClearButton;

	@FindBy(xpath = "//div[@role='combobox']//div[@aria-label='Apps']/p")
	public WebElement appsListBox;

	@FindBy(xpath = "//div[@role='combobox']//div[@aria-label='Items']/p")
	public WebElement itemsListBox;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@role='button'][@title='Select List View'] | //div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@role='button'][@title='Select List View']")
	public WebElement selectListViewButton;

	@FindBy(xpath = "//a[@role='option']//span[text()='All' or text()='All Active Parcels']")
	public WebElement selectListViewOptionAll;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//input[@placeholder='Search this list...']")
	public WebElement searchListEditBox;

	@FindBy(xpath = "//div[@data-aura-class='forceSearchDesktopHeader']/div[@data-aura-class='forceSearchInputEntitySelector']//input")
	public WebElement globalSearchListDropDown;

	@FindBy(xpath = "//*[@data-aura-class='forceSearchAssistantDialog']//input[@type='search']")
	public WebElement globalSearchListEditBox;

	@FindBy(xpath = "//div[@data-aura-class='forceSearchAssistant']//button")
	public WebElement globalSearchButton;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//*[@class='countSortedByFilteredBy']")
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
	
	@FindBy(xpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div[not(contains(@class,'hasActiveSubtab'))]//lightning-formatted-text[contains(text(),'WI')]")
	public WebElement workItemNumberDetailView;

	public String menuList = "//div[contains(@class,'uiMenuList--default visible positioned')]";

	@FindBy(xpath = "//lightning-spinner")
	public WebElement spinner;
	
	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;
	
	public String xpathSpinner = "//lightning-spinner";

	public String maxEquipmentIndexFactor = "Maximum Equipment index Factor";


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
	
	@FindBy(xpath="//button[text()='Close All']")
	public WebElement closeAllBtn;
	
	@FindBy(xpath="//div/a[@role='button']/span/span//*[@data-key='down']//*")	
	public WebElement attachmentsDropdown;
	
	
	public String SaveButton="Save";
	public String NewButton="New";
	public String EditButton="Edit";

	/**
	 * Description: This will click on the module name from the drop down
	 */
	public void clickNavOptionFromDropDown(String navOption) throws Exception {
		String xpathStr = "//a[@data-label= '" + navOption + "']//b[text() = '" + navOption + "']|//a[contains(@class,'app-launcher') and @title='" + navOption + "']";
		WebElement drpDwnOption = waitForElementToBeClickable(20, xpathStr);
		drpDwnOption.click();
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

			waitForElementToBeVisible(yearDropDown);
			Select select = new Select(yearDropDown);
			select.selectByValue(yearToSelect);

			waitForElementToBeVisible(visibleMonth);

			WebElement visibleMnth = visibleMonth;
			String visibleMonthTxt = visibleMnth.getText().toLowerCase();
			visibleMonthTxt = visibleMonthTxt.substring(0, 1).toUpperCase() + visibleMonthTxt.substring(1).toLowerCase();

			int counter = 0;
			int indexOfDefaultMonth = monthsList.indexOf(visibleMonthTxt);
			int indexOfMonthToSelect = monthsList.indexOf(monthToSelect);
			int counterIterations = (Math.abs(indexOfDefaultMonth - indexOfMonthToSelect));

			while(!visibleMonthTxt.equalsIgnoreCase(monthToSelect) || counter > counterIterations) {
				if(indexOfMonthToSelect < indexOfDefaultMonth) {
					waitForElementToBeVisible(prevMnth);
					Click(prevMnth);
				} else {
					waitForElementToBeVisible(nextMnth);
					Click(nextMnth);
				}
				waitForElementToBeVisible(visibleMonth);
				visibleMonthTxt = visibleMonth.getText();
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
	public void searchAndSelectOptionFromDropDown(Object element, String value) throws Exception {
        WebElement webElement;
        String xpathDropDownOption;
        if (element instanceof String) {
            webElement = getWebElementWithLabel((String) element);
            xpathDropDownOption = "//div[contains(@class,'windowViewMode-normal') or "
            		+ "contains(@class,'windowViewMode-maximized')]"
            		+ "//label[text()=\""+element+"\"]/..//*[(@title='" + value + "') or "
            				+ "(text() = '" + value + "')]";
        } else{
            webElement = (WebElement) element;
            //xpathDropDownOption = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//*[@title='" + value + "']";
            xpathDropDownOption = "//div[contains(@class,'windowViewMode-normal') or "
            		+ "contains(@class,'windowViewMode-maximized') or "
            		+ "contains(@class,'lafAppLayoutHost forceAccess tablet')]//*[@title='" + value + "']";
        }
        
        enter(webElement, value);
        WebElement drpDwnOption = locateElement(xpathDropDownOption, 20);
        waitForElementToBeVisible(drpDwnOption, 10);
        //drpDwnOption.click();
        Click(drpDwnOption);
    }

	/**
	 * @Description: This method is to handle fields like Permit City Code or Processing Status
	 * by clicking the web element and then selecting the given value from drop down
	 * @param element: WebElement for required field
	 * @param value: Like 'Process' or 'No Process' for Processing Status field etc.
	 * @throws Exception
	 */
	public void selectOptionFromDropDown(Object element, String value) throws Exception {
        WebElement webElement;
		WebElement drpDwnOption;
        String xpathDropDownOption;
        if (element instanceof String) {
        	webElement = getWebElementWithLabel((String) element);
        	String commonPath = "//div[contains(@class,'windowViewMode-normal') "
        			+ "or contains(@class,'windowViewMode-maximized') "
        			+ "or contains(@class,'slds-listbox__option_plain') "
        			+ "or contains(@class,'flowruntimeBody') "
        			+ "or contains(@class,'slds-input slds-combobox__input')]";//the class flowruntimeBody has been added to handle elements in mapping actions page
			xpathDropDownOption = commonPath + 
					"//label[text()='" + element + "']/..//*[@title='" + value + "' or text() = '" + value + "']" ;
		
        } else{
            webElement = (WebElement) element;
            xpathDropDownOption="//*[contains(@class, 'left uiMenuList--short') "
            		+ "or contains(@class,'slds-listbox__option_plain') "
            		+ "or contains(@class,'select uiInput ')"
            		+ "or contains(@class,'slds-input slds-combobox__input') "
            		+ "or contains(@class,'slds-dropdown_length-with-icon')]//*[text() = '" + value + "' or @title= '" + value + "']";
            		
		}

        if (webElement.getTagName().equals("select")){
			//This condition is added as few drop downs are found to be of Select type
			SelectByVisibleText(webElement,value);
		}else{
			scrollToElement(webElement);
			javascriptClick(webElement);
			waitUntilElementIsPresent(xpathDropDownOption, 10);
			drpDwnOption = driver.findElement(By.xpath(xpathDropDownOption));
			scrollToElement(drpDwnOption);
			waitForElementToBeClickable(drpDwnOption, 8);
			javascriptClick(drpDwnOption);
		}

    }
	
	
	public List<Object> getAllOptionFromDropDown(String FieldName) throws Exception {
                WebElement webElement;
		        List<WebElement> drpDwnOption;
                String xpathDropDownOptions;
         
        	String commonPath = "//div[contains(@class,'windowViewMode-normal') "
        			+ "or contains(@class,'windowViewMode-maximized') "
        			+ "or contains(@class,'slds-listbox__option_plain') "
        			+ "or contains(@class,'flowruntimeBody') "
        			+ "or contains(@class,'slds-input slds-combobox__input')]";
        	
			xpathDropDownOptions = commonPath + 
					"//label[text()='" + FieldName + "']/..//lightning-base-combobox-item/span/span" ; 			        
			drpDwnOption = driver.findElements(By.xpath(xpathDropDownOptions));
			
			List<Object> pickListOptions = new ArrayList<>();
			
			for(WebElement el : drpDwnOption ) {
				pickListOptions.add(el.getAttribute("title"));
			}
			return pickListOptions;			
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
	 * @param modRecordName: Name of the entry displayed on grid which is to be accessed
	 * @throws Exception
	 */
	public void clickShowMoreButton(String modRecordName) throws Exception {		
		Thread.sleep(1000);
		String xpathStr = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table//tbody/tr//th//a[text() = '"+ modRecordName +"']//parent::span//parent::td//following-sibling::th//a[@role = 'button']|//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table//tbody/tr//td//a[text() = '"+ modRecordName +"']//parent::span//parent::td//following-sibling::td//a[@role = 'button']";
		WebElement modificationsIcon = locateElement(xpathStr, 30);
		clickAction(modificationsIcon);
		ReportLogger.INFO(modRecordName + " record exist and user is able to click Show More button against it");
		Thread.sleep(1000);
	}
	
	/**
	 * Description: This method will click 'Show More Button' on the Screen
	 * @param modRecordName: Record Number
	 * @param action: Action user want to perform - Edit/Delete
	 * @return: Boolean value
	 */
	public Boolean clickShowMoreButtonAndAct(String modRecordName, String action) throws Exception { 
		Boolean flag=false;
		clickShowMoreButton(modRecordName);
		String xpathStr = "//li//a[@title='" + action + "']//div[text()='" + action + "']";
		WebElement actionElement = waitForElementToBeClickable(10, xpathStr);
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

	/**
	 * Description: This method will open the tab with name which will be passed a parameter
	 * @param tabName: tabName
	 */
	public void openTab(String tabName) throws Exception {
		String tabXPath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@role='tab'][@data-label='"+ tabName +"']";
		waitUntilElementIsPresent(tabXPath,5);
		Click(driver.findElementByXPath(tabXPath));
		Thread.sleep(3000);
	}


	/**
	 * Description: This method will login to the APAS application with the user type passed as parameter
	 *
	 * @param userType : Type of the user e.g. business admin / appraisal support
	 */
	public void login(String userType) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + userType);
		String password = CONFIG.getProperty(userType + "Password");

		//Decrypting the password if the encrypted password is saved in envconfig file and passwordEncryptionFlag flag is set to true
		if (CONFIG.getProperty("passwordEncryptionFlag").equals("true")) {
			System.out.println("Decrypting the password : " + password);
			password = PasswordUtils.decrypt(password, "");
		}

		navigateTo(driver, envURL);
		enter(objLoginPage.txtuserName, CONFIG.getProperty(userType + "UserName"));
		enter(objLoginPage.txtpassWord, password);
		Click(objLoginPage.btnSubmit);
		ReportLogger.INFO("User logged in the application");
		//closeDefaultOpenTabs();
	}

	public void closeDefaultOpenTabs() throws Exception {
		ReportLogger.INFO("Closing all default tabs");

		waitForElementToBeClickable(appLauncher, 10);
    	/*Robot rb=new Robot();
    	rb.keyPress(KeyEvent.VK_SHIFT);
    	rb.keyPress(KeyEvent.VK_W);
    	rb.keyRelease(KeyEvent.VK_W);
    	rb.keyRelease(KeyEvent.VK_SHIFT);
		*/
		Actions objAction=new Actions(driver);
		objAction.keyDown(Keys.SHIFT).sendKeys("w").keyUp(Keys.SHIFT).perform();
		waitForElementToBeVisible(5,closeAllBtn);
		
		if(verifyElementVisible(closeAllBtn))
		{Click(closeAllBtn);}
		Thread.sleep(5000);

	}

	/**
	 * Description: This method will search the module in APAS based on the parameter passed
	 *
	 * @param moduleToSearch : Module Name to search and open
	 */
public void searchModule(String moduleToSearch) throws Exception {
		
		try{	
			waitForElementToBeClickable(appLauncher, 60);
			//Thread.sleep(5000);
			Click(appLauncher);			
			waitForElementToBeClickable(appLauncherSearchBox, 60);
			enter(appLauncherSearchBox, moduleToSearch);
			//Thread.sleep(2000);
			clickNavOptionFromDropDown(moduleToSearch);
			//This static wait statement is added as the module title is different from the module to search
			Thread.sleep(4000);
			ExtentTestManager.getTest().log(LogStatus.INFO, "Opening " + moduleToSearch + " tab");
		}
		catch(Exception e){
			Util utl = new Util();	
			modulesObjectName modobj = new modulesObjectName();
			moduleToSearch = moduleToSearch.replaceAll("\\s+", "").replace("-", "");
			String moduleURL = envURL + utl.getValueOf(modobj, moduleToSearch.trim());
			navigateTo(driver,moduleURL);
			Thread.sleep(4000);
			ExtentTestManager.getTest().log(LogStatus.INFO, "Navigating directly to - " + moduleToSearch);
		}
	}
	

	/**
	 * Description: This method will logout the logged in user from APAS application
	 */
	public void logout() throws IOException {
		//Logging out of the application
		ReportLogger.INFO("User is getting logged out of the application");
		Click(objLoginPage.imgUser);
		Click(objLoginPage.lnkLogOut);
		waitForElementToBeVisible(objLoginPage.txtpassWord, 30);
	}


	/**
	 * Description: This method will Edit a cell on a grid displayed from the first row
	 *
	 * @param columnNameOnGrid: Column name on which the cell needs to be updated
	 * @param expectedValue:    Modified value to be updated in the cell
	 */
	public void editGridCellValue(String columnNameOnGrid, String expectedValue) throws IOException, AWTException, InterruptedException {
		String xPath =  "//lightning-tab[contains(@class,'slds-show')]//*[@data-label='" + columnNameOnGrid + "'][@role='gridcell']//button | //div[contains(@class,'flowruntimeBody')]//*[@data-label='" + columnNameOnGrid + "']";

		WebElement webelement = driver.findElement(By.xpath(xPath));
		Click(webelement);
		Thread.sleep(1000);
		if(verifyElementVisible("//*[@data-label='" + columnNameOnGrid + "']//button[@data-action-edit='true']"))
			Click(driver.findElement(By.xpath("//*[@data-label='" + columnNameOnGrid + "']//button[@data-action-edit='true']")));
		WebElement webelementInput = driver.findElement(By.xpath("//input[@class='slds-input']"));

		waitForElementToBeClickable(30, webelementInput);
		webelementInput.clear();
		webelementInput.sendKeys(expectedValue);
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_ENTER);
		robot.keyRelease(KeyEvent.VK_ENTER);
		Thread.sleep(2000);
	}


	/**
	 * Description: This method will display all the records on the grid
	 */
	public void displayRecords(String displayOption) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Displaying all the records on the grid");
		Click(selectListViewButton);
		String xpathDisplayOption = "//div[contains(@class,'list uiAbstractList')]//a[@role='option']//span[text()='" + displayOption + "']";
		waitUntilElementIsPresent(xpathDisplayOption, 20);
		Click(driver.findElement(By.xpath(xpathDisplayOption)));
		Thread.sleep(2000);
		if (verifyElementExists(xpathSpinner)){
			waitForElementToDisappear(xpathSpinner,15);
		}
		waitForElementToBeClickable(countSortedByFilteredBy,15);
		Thread.sleep(2000);
	}

	/**
	 * Description: This method will filter out the records on the grid based on the search string from the APAS level search
	 *
	 * @param searchString: String to search the record
	 */
	public void globalSearchRecords(String searchString) throws Exception {

		ReportLogger.INFO("Searching and filtering the data through APAS level search with the String " + searchString);
		try {
			if (System.getProperty("region").toUpperCase().equals("E2E") || System.getProperty("region").toUpperCase().equals("PREUAT") || System.getProperty("region").toUpperCase().equals("STAGING")){
			WebElement element  = driver.findElement(By.xpath("//div[@data-aura-class='forceSearchDesktopHeader']/div[@data-aura-class='forceSearchInputDesktop']//input"));
			searchAndSelectOptionFromDropDown(element, searchString);
			
		}else{
			Click(globalSearchButton);
			enter(globalSearchListEditBox,searchString);
			String xpath = "//*[@role='option']//span[@title = '" + searchString + "']";
			waitUntilElementIsPresent(xpath,5);
			Click(driver.findElement(By.xpath(xpath)));
			
			}
			Thread.sleep(5000);
		}
			
			catch (Exception e) {
				
				String executionEnv = System.getProperty("region");
		
				// for parcel search
					if(searchString.length()== 11 && isSearchStringParcel(searchString)) {
						ReportLogger.INFO("Opening parcel record: " + searchString);
						String   query = "Select Id from Parcel__c where Name = '"+searchString+"'";
						HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);	
						driver.navigate().to("https://smcacre--"+executionEnv+
								 ".lightning.force.com/lightning/r/Parcel__c/"+response.get("Id").get(0)+"/view");
						ReportLogger.INFO("Navigating to Parcel Record - https://smcacre--"+executionEnv+
								 ".lightning.force.com/lightning/r/Parcel__c/"+response.get("Id").get(0)+"/view");
						Thread.sleep(15000);
					}
					// for work item search
					else if(searchString.startsWith("WI-")){
						ReportLogger.INFO("Opening WI record: " + searchString);
						String   query = "Select Id from Work_Item__c  where Name = '"+searchString+"'";
						HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);	
						driver.navigate().to("https://smcacre--"+executionEnv+
								 ".lightning.force.com/lightning/r/Work_Item__c/"+response.get("Id").get(0)+"/view");
						ReportLogger.INFO("Navigating to Work Item Record - https://smcacre--"+executionEnv+
								 ".lightning.force.com/lightning/r/Work_Item__c/"+response.get("Id").get(0)+"/view");
						Thread.sleep(15000);
					}
					 else {
						  ReportLogger.INFO("Unable to search string: " + searchString + e);
					 }
			}
	}

	
	public boolean isSearchStringParcel(String parcel) {

		int count = 0;
		if (parcel == null) {
			return false;
		}

		for (int i = 0; i < parcel.length(); i++) {
			char c = parcel.charAt(i);
			if (c == '-') {
				count++;
			}
		}
		if (count == 2) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Description: This method will filter out the records on the grid based on the search string
	 */
	public String searchRecords(String searchString) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Searching and filtering the data on the grid with the String " + searchString);
		enter(searchListEditBox, searchString);
		Click(countSortedByFilteredBy);
		Thread.sleep(3000);
		return getElementText(countSortedByFilteredBy);
	}

	/**
	 * Description: This method will delete all the files from the folder passed in the parameter folderPath
	 *
	 * @param folderPath: path of the folder
	 */
	public void deleteFilesFromFolder(String folderPath) {
		ReportLogger.INFO("Deleting the files from the folder : " + folderPath);
		File dir = new File(folderPath);
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (!file.isDirectory())
				file.delete();
		}
	}

	/**
	 * @param sectionName: name of the section where field is present
	 * @param fieldName:   Name of the field
	 * @return Value of the field
	 * @description: This method will return the value of the field passed in the parameter from the currently open page
	 */
	public String getFieldValueFromAPAS(String fieldName, String sectionName) {
		
		String excEnv = System.getProperty("region");
		String fieldValue;
		String sectionXpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//force-record-layout-section[contains(.,'"
				+ sectionName + "')]";
		String fieldPath = sectionXpath + "//force-record-layout-item//*[text()='" + fieldName
				+ "']/../..//slot[@slot='outputField']";
		String fieldXpath;
		if (!excEnv.equalsIgnoreCase("QA")) {
			fieldXpath = fieldPath + "//force-hoverable-link//a//span | " + fieldPath + "//lightning-formatted-text | "
					+ fieldPath + "//lightning-formatted-number | " + fieldPath + "//lightning-formatted-rich-text | "
					+ fieldPath + "//force-record-type//span | " + fieldPath + "//lightning-formatted-name | "
					+ fieldPath + "//a//span";
		} else {
			fieldXpath = fieldPath + "//force-hoverable-link//a | " + fieldPath + "//lightning-formatted-text | "
					+ fieldPath + "//lightning-formatted-number | " + fieldPath + "//lightning-formatted-rich-text | "
					+ fieldPath + "//force-record-type//span | " + fieldPath + "//lightning-formatted-name | "
					+ fieldPath + "//a";
		}

		waitForElementToBeVisible(20, fieldXpath);
		try {
			fieldValue = driver.findElement(By.xpath(fieldXpath)).getText();
		} catch (Exception ex) {
			fieldValue = "";
		}

		System.out.println(fieldName + " : " + fieldValue);
		return fieldValue;
	}

	/**
	 * @param fieldName: Name of the field
	 * @return Value of the field
	 * @description: This method will return the value of the field passed in the parameter from the currently open page
	 */
	public String getFieldValueFromAPAS(String fieldName) {
		return getFieldValueFromAPAS(fieldName, "");
	}

	/**
	 * Description: This method will save the grid data in hashmap (Default Behavior: First Table and All Rows displayed on UI)
	 *
	 * @return hashMap: Grid data in hashmap of type HashMap<String,ArrayList<String>>
	 */
	public HashMap<String, ArrayList<String>> getGridDataInHashMap() {
		return getGridDataInHashMap(1);
	}


	/**
	 * Description: This method will save the grid data in hashmap (Default Behavior: Table Index passed in the parameter and all the rows)
	 *
	 * @param tableIndex: Table Index displayed on UI if there are multiple tables displayed on UI
	 * @return hashMap: Grid data in hashmap of type HashMap<String,ArrayList<String>>
	 */
	public HashMap<String, ArrayList<String>> getGridDataInHashMap(int tableIndex) {
		return getGridDataInHashMap(tableIndex, -1);
	}


	/**
	 * Description: This method will save the grid data in hashmap for the Table Index and Row Number passed in the argument
	 *
	 * @param rowNumber: Row Number for which data needs to be fetched
	 * @return hashMap: Grid data in hashmap of type HashMap<String,ArrayList<String>>
	 */
	public HashMap<String, ArrayList<String>> getGridDataInHashMap(int tableIndex, int rowNumber) {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Fetching the data from the currently displayed grid");
		//This code is to fetch the data for a particular row in the grid in the table passed in tableIndex
		String xpath="(//*[contains(@class,'slds-show')]//table)[" + tableIndex + "]";
		String xpathTable = "(//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'flowruntimeBody')]//table)[" + tableIndex + "]";
		if(verifyElementVisible(xpath))
		{xpathTable=xpath;}

		String xpathHeaders = xpathTable + "//thead/tr/th";
		String xpathRows = xpathTable + "//tbody/tr";
		
		if (!(rowNumber == -1)) xpathRows = xpathRows + "[" + rowNumber + "]";

		HashMap<String, ArrayList<String>> gridDataHashMap = new HashMap<>();

		//Fetching the headers and data web elements from application
		List<WebElement> webElementsHeaders = driver.findElements(By.xpath(xpathHeaders));
		List<WebElement> webElementsRows = driver.findElements(By.xpath(xpathRows));

		String key, value;

		//Converting the grid data into hashmap
		for (WebElement webElementRow : webElementsRows) {
			int yearAcquiredKeyCounter = 0;
			List<WebElement> webElementsCells = webElementRow.findElements(By.xpath(".//td | .//th"));
			for (int gridCellCount = 0; gridCellCount < webElementsHeaders.size(); gridCellCount++) {
				key = webElementsHeaders.get(gridCellCount).getAttribute("aria-label");
				//Year Acquired Column appears twice in Commercial and Industrial Composite Factors table
				//Below code will not add column in hashmap appearing twice
				if(key != null && key.equalsIgnoreCase("Year Acquired")) {
					if(yearAcquiredKeyCounter<1) {
						yearAcquiredKeyCounter = yearAcquiredKeyCounter + 1;
					}else
						key=null;
				}

				if (key != null) {
					//"replace("Edit "+ key,"").trim()" code is user to remove the text \nEdit as few cells have edit button and the text of edit button is also returned with getText()
					value = webElementsCells.get(gridCellCount).getText();
					String[] splitValues = value.split("Edit " + key);
					if (splitValues.length > 0) value = splitValues[0].trim();
					else value = "";
					gridDataHashMap.computeIfAbsent(key, k -> new ArrayList<>());
					gridDataHashMap.get(key).add(value);
				}
			}
		}

		//Removing the Row Number key as this is meta data column and not part of grid
		gridDataHashMap.remove("Row Number");
		System.out.println("HashMap: "+gridDataHashMap);
		return gridDataHashMap;
	}

	
	/**
	 * Description: This method is to check unavailbility of an element
	 *
	 * @param element: xpath of the element
	 * @return : true if element not found
	 */
	public boolean isNotDisplayed(WebElement element) {
		return  !verifyElementVisible(element);
	}

	/**
	 * Description: This method will select multiple values from left pane to right pane
	 * (e.g:basis for claim,Deceased veteran Qualification)
	 *
	 * @param values:    values to select
	 * @param fieldName: e.g: Deceased Veterna Qualification
	 */
	public void selectMultipleValues(String values, String fieldName) throws IOException {
		String[] allValues = values.split(",");
		JavascriptExecutor js = (JavascriptExecutor) driver;
		for (String value : allValues) {
			WebElement elem = driver.findElement(By.xpath("//ul[@role='listbox']//li//span[text()='" + value + "']"));
			js.executeScript("arguments[0].scrollIntoView(true);", elem);
			Click(elem);
			WebElement arrow = driver.findElement(By.xpath("//div[text()='" + fieldName + "']//following::button[@title='Move selection to Chosen']"));
			js.executeScript("arguments[0].scrollIntoView(true);", arrow);
			Click(arrow);
		}
	}

	/**
	 * @param fieldName: name of the required field
	 * @param field:     field Webelement
	 * @param data:      the data to be entered intextbox
	 * @throws Exception
	 * @Description: This method is to edit(enter) a record by clicking on the pencil icon and save it(field level edit)
	 */
	public void editAndInputFieldData(String fieldName, Object field, String data) throws Exception {

		String xpath="//div//button/span[contains(.,'Edit " + fieldName + "')]/ancestor::button";
		Thread.sleep(2000);
		scrollToElement(driver.findElement(By.xpath(xpath)));
		Thread.sleep(2000);
		Click(driver.findElement(By.xpath(xpath)));
		Thread.sleep(2000);
		enter(field, data);
		Click(saveButton);
		Thread.sleep(4000);

	}

	/**
	 * This method is to edit(enter) a record by clicking on the pencil icon and save it(field level edit)
	 * @param fieldName: name of the required field
	 * @param data:      the data to be entered in text box
	 */
	public void editAndInputFieldData(String fieldName, String data) throws Exception {
		editAndInputFieldData(fieldName,null,data);
	}

	/**
	 * @param fieldName: name of the required field
	 * @param value:     the data to be selected from drop down
	 * @throws Exception
	 * @Description: This method is to edit(select) a record by clicking on the pencil icon and save it(field level edit)
	 */
	public void editAndSelectFieldData(String fieldName, String value) throws Exception {

		WebElement editButton = driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[contains(.,'Edit " + fieldName + "')]"));
		Click(editButton);
		selectOptionFromDropDown(fieldName,value);
		WebElement btnSave = getButtonWithText("Save");
		waitForElementToBeClickable(btnSave,20);
		Click(getButtonWithText("Save"));
		Thread.sleep(4000);

	}

	/**
	 * @Description: This method is to Zoom Out browser Content
	 */
	public void zoomOutPageContent() throws Exception {
		// Step6: Minimizing the browser content to 50%
		for (int i = 1; i < 6; i++) {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_SUBTRACT);
			robot.keyRelease(KeyEvent.VK_SUBTRACT);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_ENTER);
			Thread.sleep(1000);
		}
		Thread.sleep(1000);

	}

	/**
	 * @Description: This method is to Zoom Out browser Content
	 */
	public void zoomInPageContent() throws Exception {
		// Step6: Maximizing the browser content from 50 to 100%
		Thread.sleep(10);
		for (int i = 1; i < 6; i++) {
			Robot robot = new Robot();
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_ADD);
			robot.keyRelease(KeyEvent.VK_ADD);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyRelease(KeyEvent.VK_ENTER);
			Thread.sleep(1000);
		}
		Thread.sleep(1000);
	}


	/**
	 * @Description: This method is to fetch File Name last modified in Downloads Folder
	 * @param: Folder Path is passed in which last modified file is to be fetched
	 * @returns: File Name last modified
	 * @throws: Exception
	 */
	public String getLastModifiedFile(String path) throws Exception {
		File dir = new File(path);
		File[] files = dir.listFiles();
		String fileName = "";
		for (int i = 0; i < files.length; i++) {
			File lastModifiedFile = files[0];
			if (lastModifiedFile.lastModified() < files[i].lastModified()) {
				lastModifiedFile = files[i];
				fileName = lastModifiedFile.getName();
			}
		}
		return fileName;
	}


	/**
	 * Description: This method will save the grid data in ArrayList(Headers=value) for the Row Number passed in the argument
	 *
	 * @param rowNumber: Row Number for which data needs to be fetched
	 * @return hashMap: Grid data in ArrayList of type ArrayList<String>
	 */
	
	public HashMap<String, ArrayList<String>> getGridDataInLinkedHM(int rowNumber) {

		ExtentTestManager.getTest().log(LogStatus.INFO, "Fetching the data from the currently displayed grid");
		//This code is to fetch the data for a particular row in the grid in the table passed in tableIndex
		String xpathTable = "//table[contains(@class,'data-grid-full-table')]";
		String xpathHeaders = xpathTable + "//tbody//tr[1]//th//span[contains(@class,'header-value')]";
		String xpathRows = xpathTable + "//tbody/tr";
		if (!(rowNumber == -1)) xpathRows = xpathRows + "[" + rowNumber + "]";

		HashMap<String, ArrayList<String>> gridDataHashMap = new LinkedHashMap<>();

		//Fetching the headers and data web elements from application
		List<WebElement> webElementsHeaders = driver.findElements(By.xpath(xpathHeaders));
		List<WebElement> webElementsRows = driver.findElements(By.xpath(xpathRows));
		String key, value;
		boolean flag = false;
		//Converting the grid data into hashmap
		for (WebElement webElementRow : webElementsRows) {
			List<WebElement> webElementsCells = webElementRow.findElements(By.xpath(".//td | .//th | .//td/data-tooltip"));
			for (int gridCellCount = 0; gridCellCount < webElementsHeaders.size(); gridCellCount++) {
				key = webElementsHeaders.get(gridCellCount).getText();
				//Status column exists twice in Report and below check will add both as keys
				if (key.equals("Status") && flag == true) {
					key = key + "_1";
				}
				if (!key.equals("")) {
					value = webElementsCells.get(gridCellCount).getText().trim();
					System.out.println("value: " + value);
					gridDataHashMap.computeIfAbsent(key, k -> new ArrayList<>());
					gridDataHashMap.get(key).add(value);
					if (key.equals("Status")) {
						flag = true;
					}
				}
			}
		}
		return gridDataHashMap;
	}


	/**
	 * Description: This method will convert amount of type String to Float
	 *
	 * @param : Amount Object
	 */
	public float convertToFloat(Object amount) {
		String amt = (String) amount;
		String finalAmtAsString = (amt.substring(1, amt.length())).replaceAll(",", "");
		float convertedAmt = Float.parseFloat(finalAmtAsString);
		return convertedAmt;
	}

	/**
	 * @param fieldName: field name for which error message needs to be fetched
	 * @description: This method will return the error message appeared against the filed name passed in the parameter
	 */
	public String getIndividualFieldErrorMessage(String fieldName) throws Exception {
		String xpath = "//label[text()=\""+fieldName+"\"]/../..//*[contains(@class,'__help')] | //div[text()=\""+fieldName+"\"]//following-sibling::div/..//*[contains(@class,'slds-has-error')]";
		waitUntilElementIsPresent(xpath,20);
		return getElementText(driver.findElement(By.xpath(xpath)));
	}

	/**
	 * Description: This will update the status of Roll Year
	 *
	 * @param expectedStatus: Expected status like Open, Closed etc.
	 * @param rollYear:       Roll year for which the status needs to be updated
	 */
	public void updateRollYearStatus(String expectedStatus, String rollYear) throws Exception {
		//Query to update the status of Roll Year
		String queryForID = "Select Id From Roll_Year_Settings__c where Roll_Year__c = '" + rollYear + "'";
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("Status__c", expectedStatus);
		objSalesforceAPI.update("Roll_Year_Settings__c", queryForID, jsonObj);
	}

	/**
	 * Description: this function is to open the imported log created
	 *
	 * @throws IOException
	 * @throws Exception
	 * @param: filtyepe, Source and period values
	 */
	public void openLogRecordForImportedFile(String fileType, String source, String period, String filepath) throws Exception {
		String logName = fileType + " :" + source + " :" + period;
		String filename = filepath.substring(filepath.lastIndexOf("\\") + 1, filepath.lastIndexOf("."));
		javascriptClick(driver.findElement(By.xpath("(//a[text()='" + filename + "'])[1]")));

		for (String winHandle : driver.getWindowHandles()) {
			driver.switchTo().window(winHandle);
		}

		waitUntilElementIsPresent("//div[text()='" + logName + "']",20);
		javascriptClick(driver.findElement(By.xpath("//div[text()='" + logName + "']")));

	}

	/**
	 * @param entryDetails: Name of the entry displayed on grid which is to be accessed
	 * @throws Exception
	 * @description: Clicks on the show more link displayed against the given entry
	 */
	public void clickShowMoreLink(String entryDetails) throws Exception {
		String xpathStr = "//table//tbody/tr//th//a//span[text() = '" + entryDetails + "']//parent::*//ancestor::th//following-sibling::td//button | //table//tbody/tr//th//a[text() ='" + entryDetails + "']//ancestor::th//following-sibling::td//a[@role='button']";
		WebElement modificationsIcon = locateElement(xpathStr, 60);
		Click(modificationsIcon);
		waitUntilElementIsPresent(menuList, 5);
	}

	/**
	 * Description: this method is to sacea record and wait for the success message to disapper
	 *
	 * @throws: Exception
	 */
	public String saveRecord() throws Exception {
		Click(getButtonWithText("Save"));
		waitForElementToBeClickable(successAlert,25);
		String messageOnAlert = getElementText(successAlert);
		waitForElementToDisappear(successAlert,10);
		return messageOnAlert;
	}

	/**
	 * Description: this method is to save record and get the error
	 *
	 * @throws: Exception
	 */
	public String saveRecordAndGetError() throws Exception {
		Click(getButtonWithText("Save"));
		waitForElementToBeClickable(pageError,90);
		return getElementText(pageError);
	}

	/**
	 * Description: this method is to cancel the already opened pop up
	 */
	public void cancelRecord() throws Exception {
		Click(getButtonWithText("Cancel"));
	}

	/**
	 * Description: this method is to create a new record based on the object name from right hand side panel
	 * @param : Object name to be created
	 */
	public void OpenNewEntryFormFromRightHandSidePanel(String objectName) throws IOException, InterruptedException {
		String xpath = "//article[contains(.,'" + objectName + "')]//a[@title='Show one more action'] |  //article[contains(.,'" + objectName + "')]//*[@data-aura-class='forceDeferredDropDownAction']//a | //article[contains(.,'City Strat Codes')]//span[text()='Show more actions']";
		Click(driver.findElement(By.xpath(xpath)));
		Thread.sleep(1000);
		String xpath1 = "//div[contains(@class, 'uiMenuList') and contains(@class,'visible positioned')]//div[@title = 'New'][@role='button'] | //div[contains(@class, 'slds-dropdown__list slds-dropdown_length-with-icon')]//button[text()='New'][@type='button'] |  //div[contains(@class,'actionMenu')]//a[@title='New']";
		Click(driver.findElement(By.xpath(xpath1)));
	}

	/**
	 * @description: This method will return the filed value from the view duplicate screen
	 * @param fieldName: field name for which error message needs to be fetched
	 */
	public String getFieldValueFromViewDuplicateScreen(String fieldName) {
		return getElementText(driver.findElement(By.xpath("//*[@class='tableRowGroup'][contains(.,'" + fieldName + "')]//span")));
	}

	/**
	 * @description: Clicks on the show more link displayed against the given entry
	 * @param entryDetails: Name of the entry displayed on grid which is to be accessed
	 * @throws Exception
	 */
	public void clickShowMoreLinkOnRecentlyViewedGrid(String entryDetails) throws Exception {
		Thread.sleep(3000);
		String xpathStr = "//table//tbody/tr//th//a[text() = '"+ entryDetails +"']//parent::span//parent::th//following-sibling::td//a[@role = 'button']";
		WebElement modificationsIcon = locateElement(xpathStr, 60);
		clickAction(modificationsIcon);
	}

	/**
	 * This methods copies a file to temporary folder
	 * @param filePath: Path of the file to be copied
	 */
	public File createTempFile(String filePath) throws IOException {
		return createTempFile(new File(filePath));
	}

	/**
	 * This methods copies a file to temporary folder
	 * @param file: file to be copied
	 */
	public File createTempFile(File file) throws IOException {
		//Creating a temporary copy of the file to be processed to create unique name
		String timeStamp = DateUtil.getCurrentDate("yyMMddhhmmss");
		String destFile = System.getProperty("user.dir") + CONFIG.get("temporaryFolderPath") + timeStamp + "_" + file.getName();
		File tempFile = new File(destFile);
		FileUtils.copyFile(file, tempFile );
		return tempFile;
	}

	public String getAlertMessage() throws Exception {
		WebElement AlertText = locateElement("//div[contains(@class, 'toastContent')]"
				+ "//span[contains(@class, 'toastMessage')]",15);
		String alertTxt = AlertText.getText();
		return alertTxt;
	}
	/**
	 * Description: this method is to click on New Button and open the create record Pop Up
	 * @throws InterruptedException
	 */
	public void createRecord() throws Exception {
		Click(getButtonWithText("New"));
		Thread.sleep(1000);
	}
	/**
	 * Description: this method is to click on Edit Button and open the Edit record Pop Up
	 * @throws InterruptedException
	 */
	public void editRecord() throws Exception {
		Click(getButtonWithText("Edit"));
	}
	
	/*
    This method is used to return the first active APN from Salesforce
    @return: returns the active APN
   */
   public String fetchActiveAPN() {
       return fetchActiveAPN(1).get(0);
   }

   public ArrayList<String> fetchActiveAPN(int numberofAPNs) {

       String queryForID = "SELECT Name FROM Parcel__c where primary_situs__c != NULL and Status__c='Active' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and (Not Name like '100%') and (Not Name like '800%') Limit " + numberofAPNs;

       return objSalesforceAPI.select(queryForID).get("Name");
   }
   
   /*
   This method is used to return the In Progress APN from Salesforce
   @return: returns the In Progress APN
  */

  public String fetchInProgressAPN() throws Exception {
      
	  String queryAPNValue = "select Name from Parcel__c where Status__c='In Progress - To Be Expired' AND Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
      HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(queryAPNValue);
	  String inProgressAPNValue = "";
		 if(!response.isEmpty())
	            inProgressAPNValue = response.get("Name").get(0);
	        else
	        {
	            inProgressAPNValue= fetchActiveAPN();
	            jsonObject.put("PUC_Code_Lookup__c","In Progress - To Be Expired");
	            jsonObject.put("Status__c","In Progress - To Be Expired");
	            objSalesforceAPI.update("Parcel__c",fetchActiveAPN(),jsonObject);
	        }
	 return inProgressAPNValue;	 
  }
  
  /*
  This method is used to return the Retired APN from Salesforce
  @return: returns the Retired APN
 */

 public String fetchRetiredAPN() throws Exception {
     
	  String queryAPNValue = "select Name from Parcel__c where Status__c='Retired' limit 1";
	  return objSalesforceAPI.select(queryAPNValue).get("Name").get(0);
	}
 
/*
This method is used to return the Interim APN (starts with 800) from Salesforce
@return: returns the Interim APN
*/

	public String fetchInterimAPN() throws Exception {
 
	  String queryAPNValue = "Select name,ID  From Parcel__c where name like '800%' "
	  		+ "and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') "
	  		+ "AND Id NOT in (Select parcel__c FROM Property_Ownership__c) "
	  		+ "AND Status__c='Active' limit 1";

	  return objSalesforceAPI.select(queryAPNValue).get("Name").get(0);
	}
 
   
   /*
    * Get Field Value from WI TimeLine 
    */
   public String getFieldvalueFromWITimeLine(String fieldName) {
		String fieldValue="";
		String fieldXpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//flexipage-component2[@data-component-id='tem_workItemTimeline']//li[1]//*[text()='"+fieldName+"']/../following-sibling::span";
		try{
			fieldValue =driver.findElement(By.xpath(fieldXpath)).getText();
		}catch (Exception ex){
			fieldValue= "";
		}
		return fieldValue;
	}
   
   public String getErrorMessage() throws Exception {
	   	String ErrorTxt = "";
	   	Thread.sleep(7000);
		List<WebElement> ErrorText = locateElements("//div[contains(@class,'color_error')] |"
				+ "//div[contains(@class,'error') and not(contains(@class,'message-font'))]",15);
	   	if(ErrorText.get(0).getAttribute("class").contains("color_error")){
			for(WebElement errorMsg : ErrorText){
				ErrorTxt = (ErrorTxt + errorMsg.getText()).trim();
			}
		}else
			ErrorTxt = ErrorText.get(0).getText();
	   	
		return ErrorTxt;
	}
   
   /**
	 * Description: This method will save the grid data in hashmap First Table and RowsIndex displayed on UI
	 *
	 * @return hashMap: Grid data in hashmap of type HashMap<String,ArrayList<String>>
	 */
	public HashMap<String, ArrayList<String>> getGridDataForRowString(String rowString) {
		String xpath="(//*[contains(@class,'slds-show')]//table/tbody//tr)";
		String xpathTable = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'flowruntimeBody')]//table/tbody//tr";
		if(verifyElementVisible(xpath))
		{xpathTable=xpath;}

		List<WebElement> tableRows = driver.findElementsByXPath(xpathTable) ;

		for(int rowIndex = 0; rowIndex < tableRows.size(); rowIndex++) {
			if(tableRows.get(rowIndex).getText().contains(rowString)) {
				return getGridDataInHashMap(1,(rowIndex + 1));
			}
		}
		return null;			 	
	}

	/*
   This method is used to return the Divided Interest APN from Salesforce
   @return: returns the Divided Interest APN
  */
	public String fetchDividedInterestAPN() throws Exception {
		String queryAPNValue = "select Name, Status__c from Parcel__c Where NOT(Name like '%0')  limit 1";
		HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(queryAPNValue);
		String dividedInterestAPNValue = "";
		dividedInterestAPNValue = response.get("Name").get(0);
		 String   dividedInterestStatusCheck= "Select Status__c from Parcel__c where name='"+dividedInterestAPNValue+"'";
		if(objSalesforceAPI.select(dividedInterestStatusCheck).get("Status__c").get(0)!="Active")
		{
			 objSalesforceAPI.update("Parcel__c","Select Id from Parcel__c where name='"+dividedInterestAPNValue+"'", "Status__c", "Active");
		}

		return dividedInterestAPNValue;
	}

	/*
   This method is used to click on Tax Collector link of mentioned APN
   @Param: apnNumber
   @return: returns the In Progress APN
  */
	public void clickTaxCollectorLink(String apnNumber) throws Exception {
		String xPath = "//a[text()='"+apnNumber+"']";
		waitForElementToBeVisible(20, xPath);
		WebElement taxCollectorLink = waitForElementToBeClickable(20, xPath);
		Click(taxCollectorLink);
	}
	

	public String getSuccessMessage() throws Exception {
		String SuccessTxt = "";
		waitForElementToDisappear(xpathSpinner,15);
		Thread.sleep(2000);
		List<WebElement> SuccessText = locateElements("//div[contains(@class,'color_success')]",15);
		if(SuccessText.get(0).getAttribute("class").contains("color_success")){
			for(WebElement successMsg : SuccessText){
				SuccessTxt = (SuccessTxt + successMsg.getText()).trim();
			}
		}else
			SuccessTxt = SuccessText.get(0).getText();

		return SuccessTxt;
	}

	/**
	 * Description: This method will verify is a cell on a grid displayed from the first row is editable
	 *
	 * @param columnNameOnGrid: Column name on which the cell needs to be updated
	 *  
	 */
	public boolean verifyGridCellEditable(String columnNameOnGrid, int rowNum)  {
		boolean isCellEditable = false;
		if(verifyElementVisible("//tr[@data-row-key-value='row-" + rowNum +"']//lightning-primitive-cell-factory[@data-label='" + columnNameOnGrid + "']//button[@data-action-edit='true']")){
			isCellEditable = true;
		}
		return isCellEditable;
	
	}
	
	public boolean verifyGridCellEditable(String columnNameOnGrid)  {
		return verifyGridCellEditable(columnNameOnGrid, 0);
	}
	
	/**
     *  This method will delete existing ownership records for the Parcel
     * @param apn-Apn whose records needs to be deleted
     * @return
     * @throws Exception
     */
    public void deleteOwnershipFromParcel(String apnId)
    {
  	  String query ="SELECT  Id FROM Property_Ownership__c where parcel__c='" +apnId+"' or Ownership_Roll_Back__c='" +apnId+"'";
  	  HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);
  	  
  	  if(!response.isEmpty())
  	  {
  		  response.get("Id").stream().forEach(Id ->{
  			  objSalesforceAPI.delete("Property_Ownership__c", Id);
  			  
  		  });      	    				  
  	  }

    }
    
    /**
     *  This method will edit the particular field  by clicking on pencil icon
     * @param field name whose value needs to be edited
     * @return webelement
     */
    public WebElement editFieldButton(String fieldName)
	{
		return driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[contains(.,'Edit " + fieldName + "')]"));
	}
    
    /**
	 * Description: This will update the status of all open Roll Year except the current rollyear to closed
	 */
	public void updateAllOpenRollYearStatus() throws Exception {
		
		 String currentDate=DateUtil.getCurrentDate("MM/dd/yyyy"); 
		 String currentRollYear=ExemptionsPage.determineRollYear(currentDate);	
		HashMap<String, ArrayList<String>> responseRollYearDetails = objSalesforceAPI.select("SELECT Name FROM Roll_Year_Settings__c where status__c ='Open' and name !='"+currentRollYear+"'");
		
		if (responseRollYearDetails.size()>0)
		
		responseRollYearDetails.get("Name").stream().forEach(Name->
		{
			try {
				updateRollYearStatus("Closed",Name);
			} catch (Exception e) {
				ReportLogger.INFO("UNABLE TO UPDATE"+Name+" roll year status to Closed" );	
			}
		});
		
	}
	
	/**
	 * @Description: This method will click on upload of attachments  
	 */
	public void ClickUploadButtonOnSidePanel(String objectName) throws IOException, InterruptedException {
		String xpath = "//article[contains(.,'" + objectName
				+ "')]//a[@title='Show one more action'] |  //article[contains(.,'" + objectName
				+ "')]//*[@data-aura-class='forceDeferredDropDownAction']//a | //article[contains(.,'City Strat Codes')]//span[text()='Show more actions']";
		Click(driver.findElement(By.xpath(xpath)));
		Thread.sleep(2000);
		String xpath1 = "//div[contains(@class, 'uiMenuList') and contains(@class,'visible positioned')]//div[@title = 'Upload Files'][@role='button'] | //div[contains(@class, 'slds-dropdown__list slds-dropdown_length-with-icon')]//button[text()='Upload Files'][@type='button'] |  //div[contains(@class,'actionMenu')]//a[@title='Upload Files']";
		waitForElementToBeClickable(driver.findElement(By.xpath(xpath1)), 20);
		Click(driver.findElement(By.xpath(xpath1)));
	}
	
	
	/**
	 * @Description: This method will sort a column in grid  
	 */
	public void sortInGrid(String columnName,boolean isAscending) throws IOException, InterruptedException  {
		String xpath = columnInGrid.replace("columnName",columnName);
		String xpathSorting=xpath+"//following::span";
		Click(driver.findElement(By.xpath(xpath)));
		Thread.sleep(3000);
		
		String sortStatus=getElementText(driver.findElement(By.xpath(xpathSorting)));
		
		if(isAscending==true && !sortStatus.equals("Sorted Ascending"))
			{
			Click(driver.findElement(By.xpath(xpath)));
			waitForElementTextToBe(driver.findElement(By.xpath(xpathSorting)), "Sorted Ascending", 10);
			}

		if(isAscending==false && sortStatus.equals("Sorted Ascending"))
			{
			Click(driver.findElement(By.xpath(xpath)));		
			waitForElementTextToBe(driver.findElement(By.xpath(xpathSorting)), "Sorted Descending", 10);
			}

		
	} 

	public void ClickCharacteristicCorrespondingDropdown(String objectName)throws IOException, InterruptedException 
	{String xpath = "//article[contains(.,'" + objectName + "')]//a[contains(@title,'more actions')] |  //article[contains(.,'" + objectName + "')]//*[@data-aura-class='forceDeferredDropDownAction']//a | //article[contains(.,'City Strat Codes')]//span[text()='Show more actions']";
	Click(driver.findElement(By.xpath(xpath)));
	Thread.sleep(1000);
	}
	
	public void ClickDeleteCorrespondingDropdown() throws IOException, InterruptedException {
		String xpath1 = "//div[@title = 'Delete'][@role='button']";
		Click(driver.findElement(By.xpath(xpath1)));
	}	
	


	/**
	 *@Description: This method will delete situs from Parcel situs tab
	 **/
	public void deleteParcelSitusFromParcel(String apn)
	{
		String query ="SELECT Id FROM Parcel_Situs__c where parcel__c ='" +objSalesforceAPI.select("Select Id from parcel__c where name='"+apn+"'").get("Id").get(0)+"'";
		HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);

		if(!response.isEmpty())
		{
			response.get("Id").stream().forEach(Id ->{
				objSalesforceAPI.delete("Parcel_Situs__c", Id);

			});      	    				  
		}

	}
	
}

