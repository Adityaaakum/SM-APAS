package com.apas.generic;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.apas.Reports.ReportLogger;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.ExemptionsPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.DateUtil;
import com.apas.Utils.PasswordUtils;
import com.apas.Utils.SalesforceAPI;
import com.relevantcodes.extentreports.LogStatus;

public class ApasGenericFunctions extends TestBase {

    private RemoteWebDriver driver;
    Page objPage;
    LoginPage objLoginPage;
    ApasGenericPage objApasGenericPage;
    SalesforceAPI objSalesforceAPI;

    public ApasGenericFunctions(RemoteWebDriver driver) {
        this.driver = driver;
        objPage = new Page(this.driver);
        objLoginPage = new LoginPage(this.driver);
        objApasGenericPage = new ApasGenericPage(this.driver);
        objSalesforceAPI = new SalesforceAPI();
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

        objPage.navigateTo(driver, envURL);
        objPage.enter(objLoginPage.txtuserName, CONFIG.getProperty(userType + "UserName"));
        objPage.enter(objLoginPage.txtpassWord, password);
        objPage.Click(objLoginPage.btnSubmit);
        ReportLogger.INFO("User logged in the application");
        //closeDefaultOpenTabs();
    }

    private void closeDefaultOpenTabs() throws Exception {
    	ReportLogger.INFO("Closing all default tabs");
    	
    	objPage.waitForElementToBeClickable(objApasGenericPage.appLauncher, 10);
    	/*Robot rb=new Robot();
    	rb.keyPress(KeyEvent.VK_SHIFT);
    	rb.keyPress(KeyEvent.VK_W);
    	rb.keyRelease(KeyEvent.VK_W);
    	rb.keyRelease(KeyEvent.VK_SHIFT);
		*/
		  Actions objAction=new Actions(driver);
		  objAction.keyDown(Keys.SHIFT).sendKeys("w").keyUp(Keys.SHIFT).perform();
		 
		if(objPage.verifyElementVisible(objApasGenericPage.closeAllBtn))
			{objPage.javascriptClick(objApasGenericPage.closeAllBtn);}
		Thread.sleep(3000);
		
	}

	/**
     * Description: This method will search the module in APAS based on the parameter passed
     *
     * @param moduleToSearch : Module Name to search and open
     */
    public void searchModule(String moduleToSearch) throws Exception {
        ExtentTestManager.getTest().log(LogStatus.INFO, "Opening " + moduleToSearch + " tab");
        objPage.waitForElementToBeClickable(objApasGenericPage.appLauncher, 60);
        Thread.sleep(5000);
        objPage.Click(objApasGenericPage.appLauncher);
        objPage.waitForElementToBeClickable(objApasGenericPage.appLauncherSearchBox, 60);
        objPage.enter(objApasGenericPage.appLauncherSearchBox, moduleToSearch);
        Thread.sleep(2000);
        objApasGenericPage.clickNavOptionFromDropDown(moduleToSearch);
        //This static wait statement is added as the module title is different from the module to search
        Thread.sleep(4000);
    }


    /**
     * Description: This method will logout the logged in user from APAS application
     */
    public void logout() throws IOException {
        //Logging out of the application
        ReportLogger.INFO("User is getting logged out of the application");
        objPage.Click(objLoginPage.imgUser);
        objPage.Click(objLoginPage.lnkLogOut);
        objPage.waitForElementToBeVisible(objLoginPage.txtpassWord, 30);
    }


    /**
     * Description: This method will Edit a cell on a grid displayed from the first row
     *
     * @param columnNameOnGrid: Column name on which the cell needs to be updated
     * @param expectedValue:    Modified value to be updated in the cell
     */
    public void editGridCellValue(String columnNameOnGrid, String expectedValue) throws IOException, AWTException, InterruptedException {
    	String xPath =  "//lightning-tab[contains(@class,'slds-show')]//*[@data-label='" + columnNameOnGrid + "'][@role='gridcell']//button";
        WebElement webelement = driver.findElement(By.xpath(xPath));
        objPage.Click(webelement);
        WebElement webelementInput = driver.findElement(By.xpath("//input[@class='slds-input']"));
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
        objPage.Click(objApasGenericPage.selectListViewButton);
        String xpathDisplayOption = "//div[contains(@class,'list uiAbstractList')]//a[@role='option']//span[text()='" + displayOption + "']";
        objPage.waitUntilElementIsPresent(xpathDisplayOption, 10);
        objPage.Click(driver.findElement(By.xpath(xpathDisplayOption)));
        Thread.sleep(2000);
        if (objPage.verifyElementExists(objApasGenericPage.xpathSpinner)){
            objPage.waitForElementToDisappear(objApasGenericPage.xpathSpinner,15);
        }
        objPage.waitForElementToBeClickable(objApasGenericPage.countSortedByFilteredBy,15);
        Thread.sleep(2000);
    }

    /**
     * Description: This method will filter out the records on the grid based on the search string from the APAS level search
     *
     * @param searchString: String to search the record
     */
    public void globalSearchRecords(String searchString) throws Exception {
        ReportLogger.INFO("Searching and filtering the data through APAS level search with the String " + searchString);
        objPage.Click(driver.findElement(By.xpath("//div[@data-aura-class='forceSearchAssistant']//button")));
        objPage.enter(objApasGenericPage.globalSearchListEditBox,searchString);
        String xpath = "//*[@role='option']//span[@title = '" + searchString + "']";
        objPage.waitUntilElementIsPresent(xpath,5);
        objPage.Click(driver.findElement(By.xpath(xpath)));
        Thread.sleep(5000);
    }

    /**
     * Description: This method will filter out the records on the grid based on the search string
     */
    public String searchRecords(String searchString) throws Exception {
        ExtentTestManager.getTest().log(LogStatus.INFO, "Searching and filtering the data on the grid with the String " + searchString);
        objPage.enter(objApasGenericPage.searchListEditBox, searchString);
        objPage.Click(objApasGenericPage.countSortedByFilteredBy);
        Thread.sleep(3000);
        return objPage.getElementText(objApasGenericPage.countSortedByFilteredBy);
    }

    /**
     * Description: This method will delete all the files from the folder passed in the parameter folderPath
     *
     * @param folderPath: path of the folder
     */
    public void deleteFilesFromFolder(String folderPath) {
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
        String sectionXpath = "//force-record-layout-section[contains(.,'" + sectionName + "')]";
        String fieldPath = sectionXpath + "//force-record-layout-item//*[text()='" + fieldName + "']/../..//slot[@slot='outputField']";

        String fieldXpath = fieldPath + "//force-hoverable-link//a | " +
                            fieldPath + "//lightning-formatted-text | " +
                            fieldPath + "//lightning-formatted-number | " +
                            fieldPath + "//lightning-formatted-rich-text | " +
                            fieldPath + "//force-record-type//span";

        String fieldValue = driver.findElement(By.xpath(fieldXpath)).getText();
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
        String xpath="(//*[@class='slds-tabs_scoped__content slds-show']//table)[" + tableIndex + "]";
        String xpathTable = "(//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//table)[" + tableIndex + "]";
        if(objPage.verifyElementVisible(xpath))
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
                    if (splitValues.length > 0) value = splitValues[0];
                    else value = "";
                    gridDataHashMap.computeIfAbsent(key, k -> new ArrayList<>());
                    gridDataHashMap.get(key).add(value);
                }
            }
        }

        //Removing the Row Number key as this is meta data column and not part of grid
        gridDataHashMap.remove("Row Number");

        return gridDataHashMap;
    }


    public WebElement locateElement(String xpath, int timeoutInSeconds) throws Exception {
        WebElement element = null;
        for (int i = 0; i < timeoutInSeconds; i++) {
            try {
                element = driver.findElement(By.xpath(xpath));
                if (element != null) {
                    break;
                }
            } catch (Exception ex) {
                Thread.sleep(750);
            }
        }
        return element;
    }

    public void selectFromDropDown(Object element, String value) throws Exception {
        objApasGenericPage.selectOptionFromDropDown(element, value);
    }

    public void searchAndSelectFromDropDown(Object element, String value) throws Exception {
        objApasGenericPage.searchAndSelectOptionFromDropDown(element, value);
    }

    /**
     * Description: This method is to check unavailbility of an element
     *
     * @param element: xpath of the element
     * @return : true if element not found
     */
    public boolean isNotDisplayed(WebElement element) {
        //driver.findElement((By) element);
        try {
            if (element.isDisplayed()) {
                return false;
            }

        } catch (org.openqa.selenium.NoSuchElementException e) {
            return true;
        }
        return true;

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
            objPage.Click(elem);
            WebElement arrow = driver.findElement(By.xpath("//div[text()='" + fieldName + "']//following::button[@title='Move selection to Chosen']"));
            js.executeScript("arguments[0].scrollIntoView(true);", arrow);
            objPage.Click(arrow);
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
//        objPage.clickElementOnVisiblity("//div[@class='windowViewMode-normal oneContent active lafPageHost']//button/span[contains(.,'Edit " + fieldName + "')]/ancestor::button");
        if (field == null){
            field = driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[contains(.,'Edit " + fieldName + "')]"));
        }
        objPage.enter(field, data);
        objPage.Click(objApasGenericPage.saveButton);
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
        objPage.Click(editButton);
        objApasGenericPage.selectOptionFromDropDown(fieldName,value);
        objPage.Click(objPage.getButtonWithText("Save"));
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
     * @param element,          timeout: webelement to be searched
     * @param timeOutInSeconds: timeout in seconds
     * @throws Exception
     * @Description: This method is to check for the disapperance of an element
     */

    public void waitForElementToDisappear(WebElement element, int timeOutInSeconds) throws Exception {
        objPage.waitForElementToDisappear(element,timeOutInSeconds);
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
        String xpath = "//label[text()='" + fieldName + "']/../..//*[contains(@class,'__help')]";
        objPage.waitUntilElementIsPresent(xpath,20);
        return objPage.getElementText(driver.findElement(By.xpath(xpath)));
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
    public void openLogRecordForImportedFile(String fileType, String source, String period, String filepath) throws IOException {
        String logName = fileType + " :" + source + " :" + period;
        String filename = filepath.substring(filepath.lastIndexOf("\\") + 1, filepath.lastIndexOf("."));
        objPage.javascriptClick(driver.findElement(By.xpath("(//a[text()='" + filename + "'])[1]")));

        for (String winHandle : driver.getWindowHandles()) {
            driver.switchTo().window(winHandle);
        }
        objPage.javascriptClick(driver.findElement(By.xpath("//div[text()='" + logName + "']")));

    }

    /**
     * @param entryDetails: Name of the entry displayed on grid which is to be accessed
     * @throws Exception
     * @description: Clicks on the show more link displayed against the given entry
     */
    public void clickShowMoreLink(String entryDetails) throws Exception {
        String xpathStr = "//table//tbody/tr//th//a[text() = '" + entryDetails + "']//parent::*//parent::th//following-sibling::td//a[@role = 'button']";
        WebElement modificationsIcon = locateElement(xpathStr, 60);
        objPage.clickAction(modificationsIcon);
        objPage.waitUntilElementIsPresent(objApasGenericPage.menuList, 5);
    }

    /**
     * Description: this method is to sacea record and wait for the success message to disapper
     *
     * @throws: Exception
     */
    public String saveRecord() throws Exception {
        objPage.Click(objPage.getButtonWithText("Save"));
        objPage.waitForElementToBeClickable(objApasGenericPage.successAlert,20);
        String messageOnAlert = objPage.getElementText(objApasGenericPage.successAlert);
        waitForElementToDisappear(objApasGenericPage.successAlert,10);
        return messageOnAlert;
    }
}
