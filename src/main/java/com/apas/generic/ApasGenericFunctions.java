package com.apas.generic;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import com.apas.PageObjects.ApasGenericPage;
import com.apas.PageObjects.LoginPage;
import com.apas.PageObjects.Page;
import com.apas.Reports.ExtentTestManager;
import com.apas.TestBase.TestBase;
import com.apas.Utils.PasswordUtils;
import com.relevantcodes.extentreports.LogStatus;

public class ApasGenericFunctions extends TestBase{
	
	private RemoteWebDriver driver;
	Page objPage;
	LoginPage objLoginPage;
	ApasGenericPage objApasGenericPage;
	
	public ApasGenericFunctions(RemoteWebDriver driver){
		this.driver = driver;
		objPage = new Page(this.driver);
		objLoginPage = new LoginPage(this.driver);
		objApasGenericPage = new ApasGenericPage(this.driver);
	}
	
	/**
	 * Description: This method will login to the APAS application with the user type passed as parameter
	 * @param userType : Type of the user e.g. business admin / appraisal support
	 */
	public void login(String userType) throws Exception{
		ExtentTestManager.getTest().log(LogStatus.INFO, "Executing the tests case with user : " + userType);
		String password = CONFIG.getProperty(userType + "Password");
		
		//Decrypting the password if the encrypted password is saved in envconfig file and passwordEncryptionFlag flag is set to true
		if (CONFIG.getProperty("passwordEncryptionFlag").equals("true")){
			System.out.println("Decrypting the password : " + password);
			password = PasswordUtils.decrypt(password, "");
		}

		objPage.navigateTo(driver, envURL);
		objPage.enter(objLoginPage.txtuserName, CONFIG.getProperty(userType + "UserName"));
		objPage.enter(objLoginPage.txtpassWord,password);
		objPage.Click(objLoginPage.btnSubmit);
		System.out.println("User logged in the application");
	}
	
	/**
	 * Description: This method will search the module in APAS based on the parameter passed
	 * @param moduleToSearch : Module Name to search and open
	 */
	public void searchModule(String moduleToSearch) throws Exception {	
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening " + moduleToSearch + " tab");
		objPage.waitForElementToBeClickable(objApasGenericPage.appLauncher, 60);
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
	public void logout() throws IOException{
		ExtentTestManager.getTest().log(LogStatus.INFO, "User is getting logged out of the application");
		System.out.println("Logout from APAS Application");
		//Handling the situation where pop remains opened before logging out
//		if (objPage.verifyElementVisible(objApasGenericPage.crossButton))
//			objPage.Click(objApasGenericPage.crossButton);
//		else if (objPage.verifyElementVisible(objApasGenericPage.closeButton))
//			objPage.Click(objApasGenericPage.closeButton);
//		else if (objPage.verifyElementVisible(objApasGenericPage.cancelButton))
//			objPage.Click(objApasGenericPage.cancelButton);

		//Logging out of the application
		objPage.waitForElementToBeClickable(objLoginPage.imgUser, 10);
		objPage.Click(objLoginPage.imgUser);
		objPage.waitForElementToBeClickable(objLoginPage.lnkLogOut, 10);
		objPage.Click(objLoginPage.lnkLogOut);
		objPage.waitForElementToBeVisible(objLoginPage.txtpassWord,30);
	}

	
	/**
	 * Description: This method will Edit a cell on a grid displayed from the first row
	 * @param columnNameOnGrid: Column name on which the cell needs to be updated
	 * @param expectedValue: Modified value to be updated in the cell
	 */
	public void editGridCellValue(String columnNameOnGrid, String expectedValue) throws IOException, AWTException, InterruptedException{
		WebElement webelement = driver.findElement(By.xpath("//*[@data-label='" + columnNameOnGrid + "'][@role='gridcell']//button"));
		objPage.scrollToElement(webelement);
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
	public void displayRecords(String displayOption) throws IOException, InterruptedException {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Displaying all the records on the grid");
		objPage.Click(objApasGenericPage.selectListViewButton);
		Thread.sleep(1000);
		objPage.Click(driver.findElement(By.xpath("//a[@role='option']//span[text()='" + displayOption + "']")));
		Thread.sleep(5000);
	}

	/**
	 * Description: This method will filter out the records on the grid based on the search string from the APAS level search
	 * @param searchString: String to search the record
	 */
	public void globalSearchRecords(String searchString) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Searching and filtering the data through APAS level search with the String " + searchString);
		objApasGenericPage.searchAndSelectFromDropDown(objApasGenericPage.globalSearchListEditBox,searchString);
		Thread.sleep(5000);
	}

	/**
	 * Description: This method will filter out the records on the grid based on the search string
	 */
	public String searchRecords(String searchString) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Searching and filtering the data on the grid with the String " + searchString);
		objPage.enter(objApasGenericPage.searchListEditBox,searchString);
		objPage.Click(objApasGenericPage.countSortedByFilteredBy);
		Thread.sleep(3000);
		return objPage.getElementText(objApasGenericPage.countSortedByFilteredBy);
	}

	/**
	 * Description: This method will delete all the files from the folder passed in the parameter folderPath
	 * @param folderPath: path of the folder
	 */
	public void deleteFilesFromFolder(String folderPath){
		File dir = new File(folderPath);
		for (File file: Objects.requireNonNull(dir.listFiles())){
			if (!file.isDirectory())
				file.delete();
		}
	}

	/**
	 * @description: This method will return the value of the field passed in the parameter from the currently open page
	 * @param sectionName: name of the section where field is present
	 * @param fieldName: Name of the field
	 * @return  Value of the field
	 */
	public String getFieldValueFromAPAS(String fieldName, String sectionName) {
		String sectionXpath = "//force-record-layout-section[contains(.,'" + sectionName + "')]";

		String fieldXpath = sectionXpath + "//force-record-layout-item//span[text()='" + fieldName  + "']/../..//slot[@slot='outputField']//force-hoverable-link//a | " +
				sectionXpath + "//force-record-layout-item//span[text()='" + fieldName + "']/../..//slot[@slot='outputField']//lightning-formatted-text | " +
				sectionXpath + "//force-record-layout-item//span[text()='" + fieldName + "']/../..//slot[@slot='outputField']//lightning-formatted-number | " +
				sectionXpath + "//force-record-layout-item//span[text()='" + fieldName + "']/../..//slot[@slot='outputField']//lightning-formatted-rich-text | " +
				sectionXpath + "//force-record-layout-item//span[text()='" + fieldName + "']/../..//slot[@slot='outputField']//force-record-type//span";

		String fieldValue = driver.findElement(By.xpath(fieldXpath)).getText();
		System.out.println(fieldName + " : " + fieldValue);
		return fieldValue;
	}

	/**
	 * @description: This method will return the value of the field passed in the parameter from the currently open page
	 * @param fieldName: Name of the field
	 * @return Value of the field
	 */
	public String getFieldValueFromAPAS(String fieldName) {
		return getFieldValueFromAPAS(fieldName,"");
	}

	/**
	 * Description: This method will save the grid data in hashmap (Default Behavior: First Table and All Rows displayed on UI)
	 * @return hashMap: Grid data in hashmap of type HashMap<String,ArrayList<String>>
	 */
	public HashMap<String, ArrayList<String>> getGridDataInHashMap() {
		return getGridDataInHashMap(1);
	}


	/**
	 * Description: This method will save the grid data in hashmap (Default Behavior: Table Index passed in the parameter and all the rows)
	 * @param tableIndex: Table Index displayed on UI if there are multiple tables displayed on UI
	 * @return hashMap: Grid data in hashmap of type HashMap<String,ArrayList<String>>
	 */
	public HashMap<String, ArrayList<String>> getGridDataInHashMap(int tableIndex) {
		return getGridDataInHashMap(tableIndex,-1);
	}


	/**
	 * Description: This method will save the grid data in hashmap for the Table Index and Row Number passed in the argument
	 * @param rowNumber: Row Number for which data needs to be fetched
	 * @return hashMap: Grid data in hashmap of type HashMap<String,ArrayList<String>>
	 */
	public HashMap<String, ArrayList<String>> getGridDataInHashMap(int tableIndex, int rowNumber) {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Fetching the data from the currently displayed grid");
		//This code is to fetch the data for a particular row in the grid in the table passed in tableIndex
		String xpathTable = "(//table)[" + tableIndex + "]";
		String xpathHeaders = xpathTable + "//thead/tr/th";
		String xpathRows = xpathTable + "//tbody/tr";
		if (!(rowNumber == -1)) xpathRows = xpathRows + "[" + rowNumber + "]";

		HashMap<String,ArrayList<String>> gridDataHashMap = new HashMap<>();

		//Fetching the headers and data web elements from application
		List<WebElement> webElementsHeaders = driver.findElements(By.xpath(xpathHeaders));
		List<WebElement> webElementsRows = driver.findElements(By.xpath(xpathRows));

		String key, value;

		//Converting the grid data into hashmap
		for(WebElement webElementRow : webElementsRows){
			List<WebElement> webElementsCells = webElementRow.findElements(By.xpath(".//td | .//th"));
			for(int gridCellCount=0; gridCellCount< webElementsHeaders.size(); gridCellCount++){
				key = webElementsHeaders.get(gridCellCount).getAttribute("aria-label");
				if (key!=null){
					//"replace("Edit "+ key,"").trim()" code is user to remove the text \nEdit as few cells have edit button and the text of edit button is also returned with getText()
					value = webElementsCells.get(gridCellCount).getText().replace("Edit "+ key,"").trim();
					gridDataHashMap.computeIfAbsent(key, k -> new ArrayList<>());
					gridDataHashMap.get(key).add(value);
				}
			}
		}
		return gridDataHashMap;
	}
	
	/**
     * Description: This will select the All mode on grid
     */
    public void selectAllOptionOnGrid() throws Exception {
        String currentlyVisibleOption = objPage.getElementText(objApasGenericPage.currenltySelectViewOption);
        if(!(currentlyVisibleOption.equalsIgnoreCase("All"))) {
            objPage.Click(objApasGenericPage.selectListViewIcon);
            objPage.Click(objApasGenericPage.allOption);
            objPage.Click(objApasGenericPage.pinIcon);
        }
    }

}
