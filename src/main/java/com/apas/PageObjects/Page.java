package com.apas.PageObjects;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.apas.TestBase.TestBase;
import org.apache.commons.lang.RandomStringUtils;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.apas.Reports.ReportLogger;
import com.google.common.base.Function;

public class Page extends TestBase {

	public static final int MAX_TIMEOUT = 60;
	public static final int MAX_Element_TIMEOUT = 30;
	public static final int PAGE_LOAD_TIMEOUT = 60;

	public RemoteWebDriver driver = null;

	public WebDriverWait wait;

	public FluentWait<RemoteWebDriver> flwait;


	Actions actions;

	/**
	 * Instantiates a new page.
	 *
	 * @param driver : The driver
	 */
	public Page(RemoteWebDriver driver) {

		this.driver = driver;
		this.wait = new WebDriverWait(driver, 30);
		this.flwait = new FluentWait<RemoteWebDriver>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(100, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class)
				.ignoring(StaleElementReferenceException.class);

	}

	/**
	 * Function will verify whether the element is enabled or not.
	 *
	 * @param element the element
	 */
	public boolean verifyElementEnabled(WebElement element) {
		try {
			if (element.isEnabled()) return true;
		} catch (Exception e) {
			ReportLogger.INFO("Element: " + element + "is not getting enabled.");
		}
		return false;
	}

	/**
	 * Function will verify whether the element is disabled or not.
	 *
	 * @param element the element
	 */
	public boolean verifyElementDisabled(WebElement element) {
		return !verifyElementEnabled(element);
	}

	/*
    Description: This method will return true/false based on the existence
    @Params xpath : Xpath of the element
	 */
	public boolean verifyElementExists(String xpath) {
		try {
			driver.findElement(By.xpath(xpath));
		} catch (Exception e) {
			return false;
		}
		return true;
	}


	/**
	 * Function will return true if element is visible.
	 *
	 * @param object the Webelement
	 * @return true, if successful
	 */
	public boolean verifyElementVisible(Object object) {

		WebElement webElement;

		try {
			if (object instanceof String) {
				//This try catch block is to handle if the string is given as full xpath or just the label of the Element
				try {
					webElement = driver.findElement(By.xpath(object.toString()));
				} catch (Exception ignored) {
					try

					{webElement = getWebElementWithLabel(object.toString());
					}
					catch(Exception exception) {
						webElement = getButtonWithText(object.toString());
					}}

			} else if (object instanceof By) {
				webElement = driver.findElement((By) object);
			} else
				webElement = (WebElement) object;

			if (webElement.isDisplayed()) return true;
		} catch (Exception ignored) {
			
		}
		return false;
	}


	/**
	 * Function will return true if element is not visible.
	 *
	 * @param elem the Webelement
	 * @return true, if successful
	 */
	public boolean verifyElementNotVisible(WebElement elem) {
		return !verifyElementVisible(elem);
	}

	/**
	 * Function will navigate to a particular page.
	 *
	 * @param driver the Webdriver
	 * @param url    the url which has to be navigated
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void navigateTo(RemoteWebDriver driver, String url) throws IOException {
		driver.get(url);
		System.out.println("Page is loaded");
	}

	/**
	 * Function will scroll to the webelement.
	 *
	 * @param element the element
	 */
	public void scrollToElement(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].scrollIntoView()", element);
	}

	/**
	 * Function will scroll to the top of the UI.
	 * Note: scrollToElement was not working on few elements
	 */
	public void scrollToTop() {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("scroll(250, 0)");
	}


	/**
	 * Function will scroll to the bottom of UI.
	 * Note: scrollToElement was not working on few elements
	 */
	public void scrollToBottom() {
		JavascriptExecutor jse = (JavascriptExecutor) driver;
		jse.executeScript("scroll(0, 250)");
	}

	/**
	 * Function will wait until the page is not in ready state.
	 *
	 * @param driver the Webdriver
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void waitUntilPageisReady(RemoteWebDriver driver) throws IOException {

		ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {

			@Override
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};

		int time = PAGE_LOAD_TIMEOUT;
		WebDriverWait wait = new WebDriverWait(driver, time);
		wait.until(pageLoadCondition);
	}

	/**
	 * Function will click on an element.
	 *
	 * @param elem the Webelement
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void Click(WebElement elem) throws IOException {

		waitForElementToBeVisible(20, elem);
		waitForElementToBeClickable(20, elem);
		((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid green'", elem);

		try {
			elem.click();
		} catch (Exception ex) {
			try {
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", elem);
			} catch (Exception ignored) {
				actions = new Actions(driver);
				actions.moveToElement(elem).build().perform();
				((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid green'", elem);
				actions.click(elem).build().perform();
			}
		}
		
		waitUntilPageisReady(driver);
	}

	/**
	 * Function will enter the value in the element.
	 *
	 * @param element Element in which value needs to be entered
	 * @param value   the value needs to be entered
	 * @throws Exception the exception
	 */
	public void enter(Object element, String value) throws Exception {
		WebElement elem;
		if (element instanceof String) {
			elem = getWebElementWithLabel(element.toString());
		} else
			elem = (WebElement) element;

		 waitForElementToBeClickable(15, elem);
		((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid green'", elem);
		elem.clear();
		elem.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		elem.sendKeys(Keys.BACK_SPACE);
		elem.sendKeys(value);
		Thread.sleep(2000);
	}

	/**
	 * Function will enter the value in the element.
	 *
	 * @param elem Element in which value needs to be entered
	 * @param key  the value needs to be entered
	 * @throws Exception the exception
	 */
	public void enter(WebElement elem, Keys key) throws Exception {
		waitForElementToBeClickable(15, elem);
		((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid green'", elem);
		//elem.clear();
		elem.sendKeys(key);
		Thread.sleep(2000);
	}

	/**
	 * Function will wait for an element to come in clickable state on the page.
	 *
	 * @param object (Xpath, By)
	 */
	public void waitForElementToBeClickable(Object object, int timeoutInSeconds) {
		waitForElementToBeClickable(timeoutInSeconds, object);
	}

	public WebElement waitForElementToBeClickable(Object object) {
		return waitForElementToBeClickable(10, object);
	}

	public WebElement waitForElementToBeClickable(int timeoutInSeconds, Object object) {
		WebElement element = null;
		try {
			if (object instanceof String) {
				//Added this try catch block to handle if the string is passed as xpath or just element label
				try {
					element = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath(object.toString()))));
				} catch (Exception ignored) {
					element = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable(getWebElementWithLabel(object.toString())));
				}

			} else if (object instanceof By) {
				element = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable((By) object));
			} else {
				element = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable((WebElement) object));
			}
		} catch (Exception ex) {
		}

		return element;
	}


	/**
	 * Function will wait for an element to attain a value.
	 *
	 * @param element          (Xpath, By)
	 * @param timeoutInSeconds the timeout in seconds
	 */
	public void waitForElementTextToBe(WebElement element, String expectedValue, int timeoutInSeconds) {
		System.out.println("Element: " + element);
		System.out.println("Expected Value: " + expectedValue);
		System.out.println("Time Out Value: " + timeoutInSeconds);
		try {
			wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.textToBePresentInElement(element, expectedValue));
		} catch (Exception e) { //org.openqa.selenium.StaleElementReferenceException e) {
			e.printStackTrace();
		}
	}


	/**
	 * this function will click on a Webelement using Javascript click.
	 *
	 * @param element the Webelement
	 * @throws IOException
	 */
	public void javascriptClick(WebElement element) throws IOException {
		waitForElementToBeVisible(20, element);
		waitForElementToBeClickable(20, element);
		JavascriptExecutor ex = (JavascriptExecutor) driver;
		ex.executeScript("arguments[0].click();", element);
		waitUntilPageisReady(driver);
	}

	public boolean SelectByIndex(WebElement elem, int index) throws Exception {
		elem.click();
		new org.openqa.selenium.support.ui.Select(elem).selectByIndex(index);
		// new Select(elem).selectByIndex(2);
		waitUntilPageisReady(driver);
		return true;
	}

	public void SelectByVisibleText(WebElement elem, String value) throws Exception {
		elem.click();
		new org.openqa.selenium.support.ui.Select(elem).selectByVisibleText(value);
		waitUntilPageisReady(driver);
	}

	public void clickAction(WebElement element) throws IOException {
		waitForElementToBeVisible(element, 10);
		waitForElementToBeClickable(element, 10);

		actions = new Actions(driver);
		actions.moveToElement(element).build().perform();
		((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid green'", element);
		actions.click(element).build().perform();
		waitUntilPageisReady(driver);

	}


	/**
	 * This method Gets the attribute/property value.
	 *
	 * @param ele the element
	 * @return the text value of the web element
	 */
	public String getElementText(WebElement ele) {
		waitForElementToBeVisible(ele, 30);
		String value;
		value = ele.getText();
		return value;
	}

	/**
	 * This method Gets the attribute/property value.
	 *
	 * @param ele      the element
	 * @param property the property
	 * @return the property value
	 */
	public String getAttributeValue(WebElement ele, String property) {
		waitForElementToBeVisible(ele, 30);
		String value;
		value = ele.getAttribute(property);
		return value;
	}

	/**
	 * This method set the attribute/property value.
	 *
	 * @param ele      the element
	 * @param property the property
	 * @return the property value
	 */
	public void setAttributeValue(WebElement ele, String property, String Val) {

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", ele, property, Val);

	}

	public String generateRandomString(int noOfchar) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789#@abcdefghijklmnopqrstuvwxyz0";

		String randomString = RandomStringUtils.randomAlphanumeric(10);
		String randomNumber = RandomStringUtils.randomNumeric(2);

		randomString = randomString + randomNumber;
		randomString = randomString + "@";
		return randomString;
	}

	public String generateRandomStringWithoutSpecialChar(int noOfchar) {
		return RandomStringUtils.random(noOfchar, true, false);
	}

	public String generateRandomNumber(String noOfchar) {
		return RandomStringUtils.randomNumeric(Integer.parseInt(noOfchar));
	}


	/**
	 * Function will find all the elements based on the specified xpath
	 *
	 * @param: Xpath of the element to be locate
	 * @param: timeout in seconds
	 */
	public List<WebElement> locateElements(String xpath, int timeoutInSeconds) throws Exception {
		List<WebElement> elements = null;
		for (int i = 0; i < timeoutInSeconds; i++) {
			elements = driver.findElements(By.xpath(xpath));
			if (elements != null) {
				break;
			} else {
				Thread.sleep(250);
			}
		}
		return elements;
	}

	public WebElement locateElement(String locatorValue, int timeoutInSeconds) {
		WebElement element;
		wait = new WebDriverWait(driver, timeoutInSeconds);

		element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locatorValue)));
		element = wait.until(ExpectedConditions.visibilityOf(element));

		return element;
	}

	/**
     * Function will wait until to Max timeout until the WebElement is located.
     *
     * @param: Takes xpath to locate element, time out in seconds, pooling time in seconds
     * @return: Returns the element
     */
    public void waitUntilElementIsPresent(String xpath, int timeOut) throws Exception {
        locateElement(xpath, timeOut);
    }

    public WebElement waitUntilElementIsPresent(int timeOutInSec, String xpath) {
        return waitUntilElementIsPresent(xpath, timeOutInSec, 500);
    }

    public WebElement waitUntilElementIsPresent(String xpath, int timeOutInSec, int poolingTimeInSec) {
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofSeconds(timeOutInSec))
                .pollingEvery(Duration.ofSeconds(poolingTimeInSec))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class);

        WebElement element = wait.until(new Function<WebDriver, WebElement>() {
            public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(xpath));
            }
        });
        return element;
    }
	/**
	 * Function will wait for an element to be Invisible on the page.
	 *
	 * @param: Xpath of the element to locate
	 * @param: timeout in seconds
	 */
	public boolean waitForElementToBeInVisible(Object object) {
		return waitForElementToBeInVisible(object, 15);
	}

	public boolean waitForElementToBeInVisible(Object object, int timeoutInSeconds) {
		boolean elementStatus = false;
		if (object instanceof String) {
			By by = By.xpath(object.toString());
			elementStatus = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.invisibilityOfElementLocated(by));
		} else {
			elementStatus = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.invisibilityOf((WebElement) object));
		}
		return elementStatus;
	}

	/**
	 * Function will wait for an element to be Visible on the page.
	 *
	 * @param timeoutInSeconds the timeout in seconds
	 */
	public void waitForElementToBeVisible(WebElement element, int timeoutInSeconds) {
		waitForElementToBeVisible(timeoutInSeconds, element);
	}

	public boolean waitForElementToBeVisible(int timeoutInSeconds, Object object) {
		boolean isElementVisible;
		try {
			if (object instanceof String) {
				//Added this try catch block to handle if the string is passed as xpath or just element label
				try {
					wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(object.toString()))));
				} catch (Exception ignored) {
					wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.visibilityOf(getWebElementWithLabel(object.toString())));
				}

			} else if (object instanceof By) {
				wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.visibilityOfElementLocated((By) object));
			} else {
				wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.visibilityOf((WebElement) object));
			}
			isElementVisible = true;
		} catch (Exception ex) {
			isElementVisible = false;
		}
		return isElementVisible;
	}

	public WebElement waitForElementToBeVisible(Object object) {
		WebElement element = null;
		if (object instanceof String) {
			element = wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(object.toString()))));
		} else if (object instanceof By) {
			element = wait.until(ExpectedConditions.visibilityOfElementLocated((By) object));
		} else {
			element = wait.until(ExpectedConditions.visibilityOf((WebElement) object));
		}
		return element;
	}


	/**
	 * Switches the focus on the new window
	 */
	public void switchToNewWindow(String parentWinHandle) throws Exception {
		for (String winHandle : driver.getWindowHandles()) {
			if (!winHandle.equalsIgnoreCase(parentWinHandle)) {
				driver.switchTo().window(winHandle);
			}
		}
		waitUntilPageisReady(driver);
	}

	/**
	 * Description: This method will scroll to the bottom of page
	 *
	 * @throws: Throws Exception
	 */
	public void scrollToBottomOfPage() throws Exception {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

	/**
	 * This method checks the Radio Button.
	 */
	public void checkRadioButton(WebElement ele) {
		waitForElementToBeVisible(ele, 30);
		waitForElementToBeClickable(ele, 30);
		if (!ele.isSelected()) {
			ele.click();
		}

	}

	/**
	 * Switches the focus on the frame
	 */
	public void switchToFrameByIndex(int frameIndex) {
		driver.switchTo().frame(frameIndex);

	}

	/**
	 * Description: This method will scroll to the top of page
	 *
	 * @throws: Throws Exception
	 */
	public void scrollToTopOfPage() throws Exception {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, -document.body.scrollHeight)");
	}

	/**
	 * Switches the focus out of the frame
	 *
	 * @throws InterruptedException
	 */
	public void switchBackFromFrame() throws InterruptedException {
		driver.switchTo().defaultContent();
		Thread.sleep(1000);
	}

	/**
	 * Description: This method will clear a field value on the screen
	 *
	 * @param elem: locator of element where field value needs to be cleared
	 */

	public void clearFieldValue(Object elem) throws Exception {

		WebElement element = waitForElementToBeClickable(15, elem);
		((JavascriptExecutor) driver).executeScript("arguments[0].style.border='3px solid green'", element);
		element.clear();
		Thread.sleep(2000);
	}

	/**
	 * Description: This method will expand icon on Detail page
	 *
	 * @param element: WebElement on which action is to be performed
	 */

	public void expandIcon(WebElement element) throws Exception {
		String propertyValue = getAttributeValue(element, "aria-expanded");
		if (propertyValue.equals("false")) {
			Click(element);
		}
	}


	/**
	 * Deletes all the files from download directory
	 *
	 * @param directoryPath: Path from where all files are to be deleted
	 */
	public void deleteAllFilesFromGivenDirectory(String directoryPath) {
		File dir = new File(directoryPath);
		if (dir.listFiles().length > 0) {
			for (File file : dir.listFiles()) {
				if (!file.isDirectory())
					file.delete();
			}
		}
	}

	/**
	 * Description: this function is to compare all drop down values with set of values in no order
	 *
	 * @param actualvalues:       actual values present in drop down
	 * @param expectedSourcesBPP: expected values to be compared
	 * @return boolean: Returns true / false bases on comparison of values
	 * @throws: Exception
	 */
	public boolean compareDropDownvalues(String actualvalues, String expectedSourcesBPP) {
		String[] allexpectedvalues = expectedSourcesBPP.split("\n");
		String valueNotpresent = "";
		for (int i = 0; i < allexpectedvalues.length; i++) {
			if (actualvalues.contains(allexpectedvalues[i])) {
			} else {
				valueNotpresent = valueNotpresent + "\n" + allexpectedvalues[i];
			}
		}
		if (!valueNotpresent.isEmpty()) {
			ReportLogger.INFO("Actual values::\n" + actualvalues + "\n||Expected Values::\n" + expectedSourcesBPP + "\n||Values not found::" + valueNotpresent);
			return false;
		} else {
			ReportLogger.INFO("All values are found\n||Actual values::\n" + actualvalues + "\n||Expected Values::\n" + expectedSourcesBPP);
			return true;
		}
	}

	/**
	 * Description: this function is to return the currently selected value of drop down
	 *
	 * @param dropDown: drop down element to be verified
	 * @return String: Returns the currently selected value of drop down
	 * @throws Exception
	 */
	public String getSelectedDropDownValue(WebElement dropDown) throws Exception {
		waitForElementToBeVisible(dropDown, 30);
		Click(dropDown);
		String array[]=dropDown.toString().split("xpath:");
        String tempValue=array[1];
        String actualValue=tempValue.substring(0,tempValue.lastIndexOf("]")).trim();
        String dropDownFieldValue= actualValue +"/../following-sibling::div//lightning-base-combobox-item//*[@data-key='check']//ancestor::span/following-sibling::span/span";
		String value =  driver.findElement(By.xpath(dropDownFieldValue)).getText();
		Click(dropDown);
		return value;
	}


	/**
	 * Description: this function is to return the all the values from the drop down
	 *
	 * @param dropDown: drop down element to be verified
	 * @return String: Returns all the values from drop down
	 * @throws Exception
	 */
	public String getDropDownValue(String dropDown) throws Exception {
		Click(getWebElementWithLabel(dropDown));
		return driver.findElement(By.xpath("//label[text()='" + dropDown + "']/..//*[@role='listbox']")).getText().trim();
	}
	/**
	 * Description: Waits until the element goes invisible within given timeout
	 *
	 * @param object           : Object to disappear
	 * @param timeOutInSeconds : Max time to wait to disappear
	 */
	public void waitForElementToDisappear(Object object, int timeOutInSeconds) throws Exception {
		WebElement webElement;
		if (object instanceof String) {
			webElement = driver.findElement(By.xpath(object.toString()));
		} else if (object instanceof By) {
			webElement = driver.findElement((By) object);
		} else
			webElement = (WebElement) object;

		for (int i = 0; i < timeOutInSeconds; i++) {
			if (verifyElementVisible(webElement))
				Thread.sleep(1000);
			else
				break;
		}
	}
	
	/**
	 * Description: returns the webelement based on the label of the element
	 *
	 * @param label : label of the object which needs to be identified
	 * @return : webelement against the label
	 */
	public WebElement getWebElementWithLabel(String label) throws Exception {
		String commonPath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'slds-listbox__option_plain') or contains(@class,'flowruntimeBody') or contains(@class,'slds-form-element')]";
		String xpath = commonPath + "//label[text()=\"" + label + "\"]/..//input | " +
				commonPath + "//input[@name=\"" + label + "\"] | " + //this condition was observed on manual work item creation pop up for edit boxes
				commonPath + "//*[(@class='inputHeader' or @class='uiBlock') and contains(.,\"" + label + "\")]/..//Select |"+ //This condition was observed for few drop downs of Select Type
				commonPath + "//label[text()=\"" + label + "\"]//parent::div//div//a | " + //this condition was observed on Mapping Action screen for Assessor' Map label
				commonPath + "//label[text()=\"" + label + "\"]//parent::div//div//span[@class='slds-col'] | " + //this condition was observed on Mapping Action screen for Parent APN(s) field
				commonPath + "//label[text()=\"" + label + "\"]/..//textarea | "+//this condition was added to handle webelements of type textarea
				commonPath + "//span[text()=\"" + label + "\"]/../following-sibling::input | "+
				commonPath + "//label[text()=\"" + label + "\"]/following-sibling::div/input[@name='Owner_Applying_SSN__c']";	//this condition is added to handle claiment name xpath on exemption page
		waitUntilElementIsPresent(xpath, 10);
		return driver.findElement(By.xpath(xpath));
		
	}
	
	/**	 * Description: returns the web element based on the text of the button
	 *
	 * @param text : text of the button
	 * @return : button element
	 */
	public WebElement getButtonWithText(String text) throws Exception {
		Thread.sleep(1000);
		String commonxPath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]";
		String xpath = commonxPath + "//button[text()='" + text + "'] | " +
				commonxPath + "//div[text()='" + text + "']//.. | " +
				commonxPath + "//*[contains(@class,'slds-is-open')]//button[text()='" + text + "'] | " +
				commonxPath + "//a[text()='" + text + "'] | " +
				commonxPath + "//span[text()='" + text + "']";
		waitUntilElementIsPresent(xpath, 10);
		return driver.findElement(By.xpath(xpath));
	}


	/**
	 * Description: This method will clear the value from the lookup field
	 *
	 * @param fieldName: Takes field name as an argument
	 */
	public void clearSelectionFromLookup(String fieldName) throws Exception {
		Thread.sleep(1000);
		String xpathStr = "//label[text()='" + fieldName + "']/parent::lightning-grouped-combobox//span[text()='Clear Selection']";
		if (waitForElementToBeVisible(5, xpathStr))
			Click(driver.findElement(By.xpath(xpathStr)));
		Thread.sleep(1000);
	}
	/**
	 * Description: This method will click element with hyperlink in the lookup field
	 *
	 * @param fieldName: Takes field name as an argument
	 */
	public void clickHyperlinkOnFieldValue(String fieldName) throws Exception {
		String xpathStr = "//span[text()='" + fieldName + "']/parent::div/following-sibling::div//div[@class='slds-grid']//a/span";
		waitUntilElementIsPresent(xpathStr, 3);
		Click(driver.findElement(By.xpath(xpathStr)));
		Thread.sleep(1000);
	}

	/*
	 * @Description - This method returns a new JSON object everytime it is  called.
	 * 
	 *  	
	 */
	public JSONObject getJsonObject()
	{
		return new JSONObject();
	}
}