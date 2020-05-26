package com.apas.PageObjects;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import com.apas.Reports.ExtentTestManager;
import com.apas.Utils.PasswordUtils;
import com.apas.Utils.SalesforceAPI;
import com.gargoylesoftware.htmlunit.javascript.host.dom.Document;
import com.relevantcodes.extentreports.LogStatus;

public class RollYearSettingsPage extends Page {
	String exemptionFileLocation = "";
	SalesforceAPI objSalesforceAPI;

	public RollYearSettingsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}
	
	
	//Locators added for elements on Roll Year Settings screen

	@FindBy(xpath = "//a[@title='New']/div[@title='New'][1]")
	public WebElement newExemptionButton;
	
	@FindBy(xpath = "//span[text()='Recently Viewed']")
	public WebElement recentlyViewedListView;
	
	@FindBy(xpath = "//span[text() = 'Roll Year Settings']//parent::label//following-sibling::input[@class=' input']")
	public WebElement rollYearSettings;

	@FindBy(xpath = "//span[text() = 'Roll Year']//parent::span//following-sibling::div[@class='uiMenu']")
	public WebElement rollYear;

	@FindBy(xpath = "//span[text() = 'Open Roll Start Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement fiscalStartDate;

	@FindBy(xpath = "//span[text() = 'Open Roll End Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement fiscalEndDate;

	@FindBy(xpath = "//span[text() = 'Calendar Start Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement calendarStartDate;

	@FindBy(xpath = "//span[text() = 'Calendar End Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement calendarEndDate;

	@FindBy(xpath = "//span[text() = 'Lien Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement lienDate;

	@FindBy(xpath = "//span[text() = 'Tax Start Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement taxStartDate;

	@FindBy(xpath = "//span[text() = 'Tax End Date']//parent::label//following-sibling::div//input[@class=' input']")
	public WebElement taxEndDate;

	@FindBy(xpath = "//span[text() = 'Status']//parent::span//following-sibling::div[@class='uiMenu']")
	public WebElement status;
	
	@FindBy(xpath = "//button[@title='Save']//span[text()='Save']")
	public WebElement saveButton;
	
	@FindBy(xpath = "//button[@title='Cancel']//span[text()='Cancel']")
	public WebElement cancelButton;
	
	@FindBy(xpath = "//button[text() = 'Edit']")
	public WebElement editButton;
	
	@FindBy(xpath = "//div[@role='menu']//li[@class='uiMenuItem']/a[@title = 'Edit']")
	public WebElement editMenuItemButton;
	
	@FindBy(xpath = "//div[text() = 'This record looks like a duplicate.']")
	public WebElement duplicateRecord;
	
	@FindBy(xpath = "//div//a[text() = 'View Duplicates']")
	public WebElement viewDuplicateRecord;
	
	@FindBy(xpath = "//li[text()='Lien Date year should be same as Roll Year']")
	public WebElement errorOnLienDate;
	
	@FindBy(xpath = "//li[text()='Tax Start Date year should be same as Roll Year']")
	public WebElement errorOnTaxStartDate;
	
	@FindBy(xpath = "//li[text()= " + "\"" + "Tax End Date" + "'s" + " year should be one year greater of selected Roll Year" + "\"]")
	public WebElement errorOnTaxEndDate;
	
	@FindBy(xpath = "//li[text()= " + "\"" + "Start Date" + "'s" + " year should be one year less of selected Roll Year" + "\"]")
	public WebElement errorOnFiscalStartDate;
	
	@FindBy(xpath = "//li[text()='End Date year should be same as Roll Year']")
	public WebElement errorOnFiscalEndDate1;
	
	@FindBy(xpath = "//li[text()='End Date must be greater than Start Date']")
	public WebElement errorOnFiscalEndDate2;
	
	@FindBy(xpath = "//li[text()='Calendar Start Date year should be same as Roll Year']")
	public WebElement errorOnCalendarStartDate;
	
	@FindBy(xpath = "//li[text()='Calendar End Date must be greater than Calendar Start Date']")
	public WebElement errorOnCalendarEndDate1;
	
	@FindBy(xpath = "//li[text()='Calendar End Date year should be same as Roll Year']")
	public WebElement errorOnCalendarEndDate2;
	
	@FindBy(xpath = "//span[text()='Delete']")
	public WebElement deleteConfirmationPostDeleteAction;
	
	
	
	//Locators added for elements on Roll Years Settings screen - Detail Page
	
	@FindBy(xpath = "//span[text() = 'Roll Year Settings']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement rollYearSettingsOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Roll Year']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement rollYearOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Fiscal Start Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement fiscalStartDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Fiscal End Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement fiscalEndDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Calendar Start Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement calendarStartDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Calendar End Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement calendarEndDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Lien Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement lienDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Tax Start Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement taxStartDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Tax End Date']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement taxEndDateOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Status']//parent::div//following-sibling::div//lightning-formatted-text")
	public WebElement statusOnDetailPage;
	
	@FindBy(xpath = "//button[contains(text(),'Cancel')]")
	public WebElement cancelButtonOnDetailPage;
	
	@FindBy(xpath = "//button[contains(text(),'Save')]")
	public WebElement saveButtonOnDetailPage;
	
	@FindBy(xpath = "//a[text()='Real Property Settings Library']")
	public WebElement rpslOnDetailPage;
	
	@FindBy(xpath = "//span[text() = 'Roll Year']//parent::div/following-sibling::div//button[contains(@class, 'inline-edit-trigger')]")
	public WebElement editPencilIconForRollYearOnDetailPage;
	
	
	//Locators added for elements on Roll Year Settings screen - Edit Detail Page
	
	@FindBy(xpath = "//label[text()='Roll Year Settings']//following-sibling::div//input")
	public WebElement rollYearSettingsOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Roll Year']//following-sibling::div//input")
	public WebElement rollYearOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Status']//following-sibling::div//input")
	public WebElement statusOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Calendar Start Date']//following-sibling::div//input")
	public WebElement calendarStartDateOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Calendar End Date']//following-sibling::div//input")
	public WebElement calendarEndDateOnDetailEditPage;
	
	@FindBy(xpath = "//label[text()='Tax Start Date']//following-sibling::div//input")
	public WebElement taxStartDateOnDetailEditPage;
	
	/*	Next 7 locators are for handling date picker
	 *	These would be moved to common package/class
	 * */
	
	@FindBy(xpath = "//div[contains(@class, 'visible DESKTOP uiDatePicker')]")
	private WebElement datePicker;

	@FindBy(xpath = "//select[contains(@class, 'select picklist')]")
	private WebElement yearDropDown;
	
	@FindBy(xpath = "//a[@class='navLink prevMonth']")
	private WebElement prevMnth;
	
	@FindBy(xpath = "//a[@class='navLink nextMonth']")
	private WebElement nextMnth;
	
	@FindBy(xpath = "//span[(contains(@class, 'uiDayInMonthCell')) and (not (contains(@class, 'nextMonth '))) and (not (contains(@class, 'prevMonth ')))]")
	private List <WebElement> dates;

	@FindBy(xpath = "//h2[@class = 'monthYear']")
	private WebElement visibleMonth;

	@FindBy(xpath = "//button[text() = 'Today']")
	private WebElement currentDate;
	
	
	/*	Next 4 locators are for validating error messages for duplicate Exemption or missing details
	 *	These would be moved to common package/class
	 * */
	
	@FindBy(xpath = "//li[contains(text(), 'These required fields must be completed:')]")
	private WebElement errorMsgOnTop;
	
	@FindBy(xpath = "//li[text() = 'Complete this field'] | //span[text() = 'Complete this field']")
	private List<WebElement> errorMsgUnderLabels; 
	
	@FindBy(xpath = "//li[contains(text(),'There seems to be an existing record with a blank')]")
	public WebElement duplicateErrorMsgWithBlankEndDate;
	
	@FindBy(xpath = "//li[contains(text(),'There seems to be an existing record with overlapp')]")
	public WebElement duplicateErrorMsgWithOverlappingDetails;
	
	
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
	 * Description: This method selects the date from date picker
	 * @param date: date to enter
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
	
	
	/**
	 * Description: This method will save a Roll Year record with no values entered
	 */
	public void saveRollYearRecordWithNoValues() throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to open a Roll Year record");
		openRollYearScreen();
		waitForRollYearScreenToLoad();
		ExtentTestManager.getTest().log(LogStatus.INFO, "Without entering any data on the Roll Year record, click 'Save' button");
		saveRollYearRecord();
	}
	
	
	/**
	 * Description: This method will click New button on Roll Year screen
	 */
	
	public void openRollYearScreen() throws Exception {
		Thread.sleep(2000);
		Click(waitForElementToBeClickable(newExemptionButton));
	}
	
	
	/**
	 * Description: This method will select from dropdown
	 * @param element: locator of element where date need to be put in
	 * @param value: field value to enter
	 */
	
	public void selectFromDropDown(WebElement element, String value) throws Exception {
		Click(element);
		String xpathStr = "//div[contains(@class, 'left uiMenuList--short visible positioned')]//a[text() = '" + value + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 200);
		drpDwnOption.click();
	}
	
	/**
	 * Description: This method will search and select from dropdown
	 * @param element: locator of element where date need to be put in
	 * @param value: field value to enter
	 */
	
	public void searchAndSelectFromDropDown(WebElement element, String value) throws Exception {
		enter(element, value);
		String xpathStr = "//div[@title='" + value + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 200);
		drpDwnOption.click();
	}
	

	/**
	 * Description: This method will wait for Roll Year screen to load before entering values
	 */
	
	public void waitForRollYearScreenToLoad() {
		waitForElementToBeClickable(rollYearSettings);
		waitForElementToBeClickable(rollYear);
	}
	
	/**
	 * Description: This method will click SAVE button on Roll Year screen
	 */
	
	public void saveRollYearRecord() throws Exception {
		Click(saveButton);
		Thread.sleep(1000);
	}
	
	/**
	 * Description: This method will retrieve error message in case mandatory fields are not filled on Roll Year screen
	 * @return : returns the list containing the error messages and the count of fields
	 */
	
	public List<String> retrieveRollYearMandatoryFieldsValidationErrorMsgs() throws Exception {
		List<String> errorsList = new ArrayList<String>();
		String errorMsgOnTopOfPopUpWindow = getElementText(waitForElementToBeVisible(errorMsgOnTop));
		errorsList.add(errorMsgOnTopOfPopUpWindow);

		String individualErrorMsg = getElementText(errorMsgUnderLabels.get(0));
		errorsList.add(individualErrorMsg);
		
		String fieldsStr = errorMsgOnTopOfPopUpWindow.split(":")[1];
		String[] totalMandatoryFields = fieldsStr.split(",");
		int countOfMandatoryFields = totalMandatoryFields.length;
		errorsList.add(Integer.toString(countOfMandatoryFields));
		
		int countOfIndividualErrorMsgs = errorMsgUnderLabels.size();
		errorsList.add(Integer.toString(countOfIndividualErrorMsgs));
		return errorsList;
	}	
	
	/**
	 * Description: This method will click CANCEL button on Roll Year screen
	 */
	
	public void cancelRollYearRecord() throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Cancel' button to move out of the Roll Year screen");
		Click(cancelButton);
	}
	
	/**
	 * Description: This method includes other methods and creates a Roll Year record
	 * @param dataMap: Map that is storing values from JSON file
	 */
	
	public void createRollYearRecord(Map<String, String> dataMap) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'New' button to open a Roll Year record");
		openRollYearScreen();
		waitForRollYearScreenToLoad();
		enterRollYearData(dataMap);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Save' button to save the details entered in Exemption record");
		saveRollYearRecord();
		
	}
	
	/**
	 * Description: This method includes other methods and creates a Roll Year record
	 * @param dataMap: Map that is storing values from JSON file
	 */
	
	public void editRollYearRecord(Map<String, String> dataMap) throws Exception {
		editRollYearRecord();
		waitForRollYearScreenToLoad();
		enterRollYearData(dataMap);
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Save' button to save the details entered in Exemption record");
		saveRollYearRecord();
	}
	
	/**
	 * Description: This method will enter mandatory field values in Roll Year screen
	 * @param dataMap: Map that is storing values from JSON file
	 */
	
	public void enterRollYearData(Map<String, String> dataMap) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Enter the following values : " + dataMap);
		enter(rollYearSettings, dataMap.get("Roll Year Settings"));
		selectFromDropDown(rollYear, dataMap.get("Roll Year"));
		enterDate(lienDate, dataMap.get("Lien Date"));
		enterDate(taxStartDate, dataMap.get("Tax Start Date"));
		enterDate(taxEndDate, dataMap.get("Tax End Date"));
		enterDate(fiscalStartDate, dataMap.get("Fiscal Start Date"));
		enterDate(fiscalEndDate, dataMap.get("Fiscal End Date"));
		enterDate(calendarStartDate, dataMap.get("Calendar Start Date"));
		enterDate(calendarEndDate, dataMap.get("Calendar End Date"));	
	}
	
	/**
	 * Description: This method will open the Roll Year record passed in the argument
	 * @param exempName: Takes Roll Year record as an argument
	 */
	public void openRollYearRecord(String rollYearName) throws IOException, InterruptedException {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Open the Roll Year record : " + rollYearName);
		Click(driver.findElement(By.xpath("//a[@title='" + rollYearName + "']")));
		Thread.sleep(3000);
	}
	
	/**
	 * Description: This method will click EDIT button on Roll Year Detail screen
	 */
	public void editRollYearRecord() throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Edit' button to update it");
		Click(editButton);
		Thread.sleep(1000);
	}
	
	/**
	 * Description: This method will click EDIT pencil icon on Roll Year Detail screen
	 */
	public void editPencilIconRollYearRecord() throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Click 'Edit' pencil icon to update it");
		Click(editPencilIconForRollYearOnDetailPage);
		Thread.sleep(1000);
	}
	
	
	/**
	 * Description: This method will update field values in Roll Year Detail screen
	 */
	public void updateRollYearRecord(Map<String, String> dataMap) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Update the field values" + dataMap);
		enter(rollYearSettings, dataMap.get("Roll Year Settings"));
		selectFromDropDown(rollYear, dataMap.get("Roll Year"));
		enterDate(lienDate, dataMap.get("Lien Date"));
		enterDate(taxStartDate, dataMap.get("Tax Start Date"));
		enterDate(taxEndDate, dataMap.get("Tax End Date"));
		enterDate(fiscalStartDate, dataMap.get("Fiscal Start Date"));
		enterDate(fiscalEndDate, dataMap.get("Fiscal End Date"));
		enterDate(calendarStartDate, dataMap.get("Calendar Start Date"));
		enterDate(calendarEndDate, dataMap.get("Calendar End Date"));
	}
	
	/**
	 * Description: This method will fetch the current URL and process it to get the Record Id
	 * @param driver: Driver Instance
	 * @return : returns the Record Id
	 */
	
	public String getCurrentUrl(RemoteWebDriver driver) throws Exception {
		wait.until(ExpectedConditions.urlContains("/view"));
		String url = driver.getCurrentUrl();
		String recordId = url.split("/")[6];
		ExtentTestManager.getTest().log(LogStatus.INFO, "Roll Year record id - " + recordId);
		Thread.sleep(1000);
		return recordId;
	
	}
	
	/**
	 * Description: This method will clear a field value on Roll Year screen
	 * @param elem: locator of element where field value needs to be cleared
	 */
	
	public void clearFieldValue(WebElement elem) throws Exception {
		waitForElementToBeClickable(15, elem);
		((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid green'", elem);		
		elem.clear();
		Thread.sleep(2000);
	}
	
	/**
	 * Description: This method will delete an existing Roll Year record
	 * @param dataMap: Map that is storing values from JSON file
	 */
	
	public void deleteExistingRollYearRecordThroughQuery(Map<String, String> dataMap) throws Exception {
		WebElement elem = locateElement("//a[@title='" + dataMap.get("Roll Year") + "']", 3);
		
		if (elem != null){
			ExtentTestManager.getTest().log(LogStatus.INFO, "Roll Year settings record exist");
			objSalesforceAPI.delete("Roll_Year_Settings__c", "SELECT Id FROM Roll_Year_Settings__c Where Name = '" + dataMap.get("Roll Year") + "'");
			ExtentTestManager.getTest().log(LogStatus.INFO, "Existing Roll Year settings record is deleted");
			Thread.sleep(2000); 
		}
		else{
			ExtentTestManager.getTest().log(LogStatus.INFO, "Roll Year settings record doesn't exist");
		}
	}
	
	/**
	 * Description: This method will click 'Show More Button' on Roll Year Screen
	 * @param recordId: Roll Year Settings Name
	 * @param action: Action user want to perform - Edit/Delete
	 */
	public void clickShowMoreButton(String rollYearName, String action) throws Exception {       
        Thread.sleep(1000);
        String xpathStr1 = "//a[@title='" + rollYearName + "']//parent::span//parent::th//following-sibling::td[9]//span//div//a//lightning-icon";
        WebElement showMoreIcon = locateElement(xpathStr1, 3);
        if (showMoreIcon != null){
        	ExtentTestManager.getTest().log(LogStatus.INFO, "Roll Year settings record exist");
        	Click(showMoreIcon);
        	Thread.sleep(1000);
        	String xpathStr2 = "//li//a[@title='" + action + "']//div[text()='" + action + "']";
        	WebElement actionOnShowMoreIcon = locateElement(xpathStr2, 3);
        	
        	if(actionOnShowMoreIcon != null){
        		clickAction(actionOnShowMoreIcon);
        		Thread.sleep(1000);
        		if (action.equals("Delete")){
        			Click(deleteConfirmationPostDeleteAction);
        			ExtentTestManager.getTest().log(LogStatus.INFO, "Existing Roll Year settings record is deleted");
        			Thread.sleep(2000);
        		}
        	}
        	else{
    			ExtentTestManager.getTest().log(LogStatus.INFO, "'" + action + "' option is not visible for the user");
    		}	
        } 
        else{
			ExtentTestManager.getTest().log(LogStatus.INFO, "Roll Year settings record doesn't exist");
		}
    }
}
