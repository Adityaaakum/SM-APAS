package com.apas.PageObjects;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

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

public class Page {

	public static final int MAX_TIMEOUT = 60;
	public static final int MAX_Element_TIMEOUT = 30;
	public static final int PAGE_LOAD_TIMEOUT = 60;

	Logger logger = Logger.getLogger(Page.class);

	public RemoteWebDriver driver = null;

	public WebDriverWait wait;

	public FluentWait<RemoteWebDriver> flwait;


	Actions actions;

	/**
	 * Instantiates a new page.
	 *
	 * @param driver
	 *            the driver
	 */
	public Page(RemoteWebDriver driver) {

		this.driver = driver;
		this.wait = new WebDriverWait(driver, 30);
		this.flwait = new FluentWait<RemoteWebDriver>(driver).withTimeout(30, TimeUnit.SECONDS)
				.pollingEvery(100, TimeUnit.MILLISECONDS).ignoring(NoSuchElementException.class)
				.ignoring(StaleElementReferenceException.class);

	}

	/**
	 * Function will return the size of the list of Webelements.
	 *
	 * @param elem
	 *            the Webelement
	 * @return the element size
	 */
	public int getElementSize(List<WebElement> elem) {
		int size = 0;
		size = elem.size();
		return size;

	}

	/**
	 * Function will verify whether the element is enabled or not.
	 *
	 * @param element
	 *            the element
	 */
	public boolean verifyElementEnabled(WebElement element) {
		boolean flag = false;
		try {

			if (element.isEnabled()) {
				flag = true;
			}
		} catch (Exception e) {
			logger.info("Element: " + element + "is not getting enabled.");
			System.out.println("Element is not enabled");
		}
		return flag;
	}

	/**
	 * Function will verify whether the element is disabled or not.
	 *
	 * @param element
	 *            the element
	 */
	public boolean verifyElementDisabled(WebElement element) {
		boolean flag = true;
		try {

			if (element.isEnabled()) {

				flag = false;
			}
		} catch (Exception e) {
			logger.info("Element: " + element + "is not getting enabled.");
			System.out.println("Element is not enabled");
		}
		return flag;
	}

	/**
	 * Function will return true if element is visible.
	 *
	 * @param elem
	 *            the Webelement
	 * @return true, if successful
	 */
	public boolean verifyElementVisible(WebElement elem) {
		boolean flag = false;
		try {

			if (elem.isDisplayed()) {
				flag = true;
			}
		} catch (Exception e) {
			logger.info("Element: " + elem + "is not getting dispayed.");
			System.out.println("Element is not visible :" + elem);
		}
		return flag;
	}

	public boolean verifyElementExists(String xpath) {
		try {
			driver.findElement(By.xpath(xpath));
		} catch (Exception e) {
			return false;
		}
		return true;
	}


	/**
	 * Function will return true if element is not visible.
	 *
	 * @param elem
	 *            the Webelement
	 * @return true, if successful
	 */
	public boolean verifyElementNotVisible(WebElement elem) {
		boolean flag = false;
		try {

			if (!elem.isDisplayed()) {
				flag = true;
			}
		} catch (Exception e) {
			logger.info("Element: " + elem + "is dispayed on page.");
			System.out.println("Element is visible on page");
		}
		return flag;
	}

	/**
	 * Function will navigate to a particular page.
	 *
	 * @param driver
	 *            the Webdriver
	 * @param url
	 *            the url which has to be navigated
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void navigateTo(RemoteWebDriver driver, String url) throws IOException {
		// waitUntilPageisReady(driver);
		driver.get(url);
		// waitUntilPageisReady(driver);
		System.out.println("Page is loaded");
	}

	/**
	 * Function will scroll to the webelement.
	 *
	 * @param element
	 *            the element
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
		JavascriptExecutor jse = (JavascriptExecutor)driver;
		jse.executeScript("scroll(250, 0)");
	}


	/**
	 * Function will scroll to the bottom of UI.
	 * Note: scrollToElement was not working on few elements
	 */
	public void scrollToBottom() {
		JavascriptExecutor jse = (JavascriptExecutor)driver;
		jse.executeScript("scroll(0, 250)");
	}

	/**
	 * Function will wait until the page is not in ready state.
	 *
	 * @param driver
	 *            the Webdriver
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
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
	 * Function will verify that links visibilty on a component.
	 *
	 * @param eleList
	 *            the list of Webelements
	 * @return true, if successful
	 */
	public boolean verifyLinksVisibilty(List<WebElement> eleList) {
		boolean flag = false;
		int icounter = 0;
		if (getElementSize(eleList) > 0) {
			for (WebElement ele : eleList) {
				if (verifyElementVisible(ele)) {
					icounter++;
				} else {
					System.out.println("Link: " + ele + "is not visible");
				}
			}
		}
		if (icounter == getElementSize(eleList) & getElementSize(eleList) > 0) {
			flag = true;
		}
		return flag;
	}

	/**
	 * Function will click on an element.
	 *
	 * @param elem
	 *            the Webelement
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void Click(WebElement elem) throws IOException {
		waitForElementToBeVisible(20, elem);
		waitForElementToBeClickable(20, elem);

		/*
		 * if (browserName.equalsIgnoreCase("Edge")) { javascriptClick(elem); }
		 * else if(browserName.equalsIgnoreCase("IE")){
		 * elem.sendKeys(Keys.ENTER); } else { elem.click(); }
		 */
		
		((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid green'", elem);
		elem.click();
		waitUntilPageisReady(driver);
	}

	/**
	 * Function will enter the value in the element.
	 *
	 * @param elem
	 *            Element in which value needs to be entered
	 * @param value
	 *            the value needs to be entered
	 * @throws Exception
	 *             the exception
	 */
	public void enter(WebElement elem, String value) throws Exception {
		waitForElementToBeClickable(15, elem);

		/*
		 * if (ExcelDriver.getBrowserVal().equalsIgnoreCase("Edge")) {
		 * actions=new Actions(driver); actions.moveToElement(elem);
		 * actions.click(); Thread.sleep(1000); actions.sendKeys(value);
		 * //actions.sendKeys(Keys.TAB); actions.build().perform();
		 * Thread.sleep(1000); } else
		 */

		((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid green'", elem);		
		elem.clear();
		elem.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		elem.sendKeys(Keys.BACK_SPACE);
		elem.sendKeys(value);
		Thread.sleep(2000);
	}

	/**
	 * Function will enter the value in the element.
	 *
	 * @param elem
	 *            Element in which value needs to be entered
	 * @param key
	 *            the value needs to be entered
	 * @throws Exception
	 *             the exception
	 */
	public void enter(WebElement elem, Keys key) throws Exception {
		waitForElementToBeClickable(15, elem);

		/*
		 * if (ExcelDriver.getBrowserVal().equalsIgnoreCase("Edge")) {
		 * actions=new Actions(driver); actions.moveToElement(elem);
		 * actions.click(); Thread.sleep(1000); actions.sendKeys(value);
		 * //actions.sendKeys(Keys.TAB); actions.build().perform();
		 * Thread.sleep(1000); } else
		 */

		((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid green'", elem);		
		//elem.clear();
		elem.sendKeys(key);
		Thread.sleep(2000);
	}
	
	/**
	 * Compare two values.
	 *
	 * @param actual
	 *            the actual value from webpage
	 * @param expected
	 *            the expected value
	 * @return true/false
	 */
	public boolean compareTwoValues(String actual, String expected) {
		boolean flag = false;
		flag = expected.equalsIgnoreCase(actual);
		return flag;
	}

	/**
	 * Difference of dates in years.
	 *
	 * @param DOB
	 *            Enter the Date which needs to be compared with current date
	 * @return the int
	 */
	public int differenceOfDatesInYears(String DOB) {
		int age = 0;
		int factor = 0;

		try {
			String dateOfBirth = DOB;
			// String currentDate = LocalDate.now().toString();
			Date date = new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			String currentDate = df.format(date);
			System.out.println(currentDate);
			Calendar cal1 = new GregorianCalendar();
			Calendar cal2 = new GregorianCalendar();
			Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
			// Date date2 = new
			// SimpleDateFormat("yyyy-MM-dd").parse(currentDate);
			Date date2 = df.parse(currentDate);
			cal1.setTime(date1);
			cal2.setTime(date2);
			if (cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR)) {
				factor = -1;
			}
			age = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) + factor;
			System.out.println("Your age is: " + age);
		} catch (ParseException e) {
			System.out.println(e);
		}

		return age;
	}
	
	public String deductDaysToDate(String dateToModify, String expectedFormat, int numberOfDaysToAdd) throws Exception{
		String dateToModifyFormatted = null;
		if(expectedFormat.contains("/")) {
			if(dateToModify.contains("/")) {
				dateToModifyFormatted = dateToModify;
			}
			if (dateToModify.contains("-")) {
				dateToModifyFormatted = dateToModify.replace("-", "/");
			}
		} 
		if(expectedFormat.contains("-")) {
			if(dateToModify.contains("-")) {
				dateToModifyFormatted = dateToModify;
			} 			
			if (dateToModify.contains("/")) {
				dateToModifyFormatted = dateToModify.replace("/", "-");
			}
		}
		
		Date date = new SimpleDateFormat(expectedFormat).parse(dateToModifyFormatted);
		
		SimpleDateFormat sdf = new SimpleDateFormat(expectedFormat);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, -numberOfDaysToAdd);
		String newDate = sdf.format(c.getTime());
		return newDate;
	}

	/**
	 * Function will wait for an element to be clicked on the page.
	 *
	 * @param element
	 *            the element
	 * @param timeoutInSeconds
	 *            the timeout in seconds
	 */
	public void waitForElementToBeClickable(WebElement element, int timeoutInSeconds) {

		try {
			flwait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).ignoring(NoSuchElementException.class)
					.ignoring(org.openqa.selenium.StaleElementReferenceException.class)
					.until(ExpectedConditions.elementToBeClickable(element));
		//} catch (org.openqa.selenium.StaleElementReferenceException e) {
		} catch (Exception e) {
			e.printStackTrace();

		}
	}
	
	/**
	 * Function will wait for an element to come in clickable state on the page.
	 * @param timeoutInSeconds the timeout in seconds
	 * @param object (Xpath, By)
	 */
	public WebElement waitForElementToBeClickable(int timeoutInSeconds, Object object) {
		boolean isElementClickable = false;
		WebElement element = null;
		try {
			if(object instanceof String) {
				element = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath(object.toString()))));
			} else if (object instanceof By) {
				element = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable((By) object));
			} else {
				element = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.elementToBeClickable((WebElement) object));
			}
			isElementClickable = true;
		} catch (Exception ex) {
			isElementClickable = false;
		}
		//return isElementClickable;
		return element;
	}

	/**
	 * Function will wait for an element to be visible on the page.
	 *
	 * @param element
	 *            the element
	 * @param timeoutInSeconds
	 *            the timeout in seconds
	 */
	public WebElement waitForElementToBeVisible(WebElement element, int timeoutInSeconds) {
		try {
			wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.visibilityOf(element));

//			wait.until(ExpectedConditions.visibilityOf(element));
		} catch (org.openqa.selenium.StaleElementReferenceException e) {
			e.printStackTrace();
		}
		return element;
	}
	
	public void waitUntilElementDisplayed(WebElement webElement, int timeoutInSeconds) {
		driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
		WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);
		ExpectedCondition<Boolean> elementIsDisplayed = new ExpectedCondition<Boolean>() {
		public Boolean apply(WebDriver arg0) {
		  try {
		     webElement.isDisplayed();
		     return true;
		  }
		  catch (NoSuchElementException e ) {
		    return false;
		  }
		  catch (StaleElementReferenceException f) {
		    return false;
		  }
		    }
		};
		wait.until(elementIsDisplayed);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		}
	
	public void waitUntilElementNotDisplayed(WebElement webElement, int timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(driver, timeoutInSeconds);
		ExpectedCondition<Boolean> elementIsDisplayed = new ExpectedCondition<Boolean>() {
		public Boolean apply(WebDriver arg0) {
		  try {
		     webElement.isDisplayed();
		     return false;
		  }
		  catch (NoSuchElementException e ) {
		    return true;
		  }
		  catch (StaleElementReferenceException f) {
		    return true;
		  }
		    }
		};
		wait.until(elementIsDisplayed);
		}
	
	/**
	 * Function will wait for an element to be Invisible on the page.
	 *
	 * @param element
	 *            the element
	 * @param timeoutInSeconds
	 *            the timeout in seconds
	 */
	public WebElement waitForElementToBeInVisible(WebElement element, int timeoutInSeconds) {
		try {
			wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.invisibilityOf(element));
		} catch (org.openqa.selenium.StaleElementReferenceException e) {
			e.printStackTrace();
		}
		return element;
	}


	/**
	 * Function will wait for an element to be Invisible on the page.
	 *
	 * @param object (Xpath, By)
	 * @param timeoutInSeconds
	 *            the timeout in seconds
	 */
	public void waitForElementToBeInVisible(Object object, int timeoutInSeconds) {
		//boolean isElementInvisible;
		try {
			if(object instanceof String) {
				wait.until(ExpectedConditions.invisibilityOf(driver.findElement(By.xpath(object.toString()))));
			} else if (object instanceof By) {
				wait.until(ExpectedConditions.invisibilityOfElementLocated((By) object));
			}
		} catch (org.openqa.selenium.StaleElementReferenceException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Function will wait for an element to attain a value.
	 *
	 * @param element (Xpath, By)
	 * @param timeoutInSeconds
	 *            the timeout in seconds
	 */
	public void waitForElementTextToBe(WebElement element, String expectedValue, int timeoutInSeconds) {
		System.out.println("Element: "+ element);
		System.out.println("Expected Value: "+ expectedValue);
		System.out.println("Time Out Value: "+ timeoutInSeconds);
		try {
			wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.textToBePresentInElement(element,expectedValue));
		} catch (Exception e) { //org.openqa.selenium.StaleElementReferenceException e) {
			e.printStackTrace();
		}
	}

	
	
	/**
	 * Function will wait for an element to be Visible on the page.
	 * @param timeoutInSeconds the timeout in seconds
	 * @param object (Xpath, By)
	 */
	public boolean waitForElementToBeVisible(int timeoutInSeconds, Object object) {
		boolean isElementVisible = false;
		try {
			if(object instanceof String) {
				wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(object.toString()))));
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
	
	/**
	 * this function will click on a Webelement using Javascript click.
	 *
	 * @param element
	 *            the Webelement
	 * @throws IOException
	 */
	public void javascriptClick(WebElement element) throws IOException {

		JavascriptExecutor ex = (JavascriptExecutor) driver;
		ex.executeScript("arguments[0].click();", element);
		waitUntilPageisReady(driver);
	}

	/**
	 * Function will return all options in a list.
	 *
	 * @param elem
	 *            the select option webElement
	 * @return the list of options
	 * @throws Exception
	 *             the exception
	 */
	public List<WebElement> getdropdownOptions(WebElement elem) throws Exception {
		org.openqa.selenium.support.ui.Select select = new org.openqa.selenium.support.ui.Select(elem);
		List<WebElement> allOptions = select.getOptions();
		return allOptions;
	}

	/**
	 * Function will verify the selected value from a dropdown.
	 *
	 * @param elem
	 *            the WebElement
	 * @param value
	 *            the value need to be verified
	 * @return true, if successful
	 */
	public boolean verifySelectedValue(WebElement elem, String value) {
		boolean flag = false;
		String sText1 = new org.openqa.selenium.support.ui.Select(elem).getFirstSelectedOption().getAttribute("value");
		flag = sText1.toLowerCase().contains(value.toLowerCase());
		return flag;
	}

	/**
	 * Function will select a value from dropdown.
	 *
	 * @param elem
	 *            the WebElement
	 * @param value
	 *            the value need to be selected
	 * @return true, if successful
	 * @throws Exception
	 *             the exception
	 */
	public boolean Select(WebElement elem, String value) throws Exception {
		Click(elem);
		Click(driver.findElement(By.xpath("//a[@role = 'menuitemradio'][@title='" + value + "']")));
//		elem.click();
//		new org.openqa.selenium.support.ui.Select(elem).selectByVisibleText(value);
		waitUntilPageisReady(driver);
		return true;
	}

	public boolean SelectByIndex(WebElement elem, int index) throws Exception {
		elem.click();
		new org.openqa.selenium.support.ui.Select(elem).selectByIndex(index);
		// new Select(elem).selectByIndex(2);
		waitUntilPageisReady(driver);
		return true;
	}

	/**
	 * Function will compare two lists and print the difference if not equal.
	 *
	 * @param ExpList
	 *            the expected list
	 * @param actualList
	 *            the actual list
	 * @return true, if successful
	 */
	public boolean compareLists(List<String> ExpList, List<String> actualList) {
		boolean flag = false;

		flag = ExpList.equals(actualList);
		if (!flag) {
			if (ExpList.size() > actualList.size()) {
				ExpList.removeAll(actualList);
				System.out.println("Difference between lists :" + ExpList);
			} else {
				actualList.removeAll(ExpList);
				System.out.println("Difference between lists :" + actualList);
			}
		}
		return flag;
	}

	/**
	 * Function will wait until to Max timeout until the webelement is located.
	 *
	 * @param xpath : xpath of the element to be located
	 * @param timeOut : maximum time to wait for element to be present
	 */
	public void waitUntilElementIsPresent(String xpath, int timeOut) throws Exception {
		locateElement(xpath,timeOut);
	}
	
	/**
	 * Function will try an element finding multiple times on the page.
	 *
	 * @param by
	 *            the by
	 * @return the list
	 */
	public List<WebElement> retryingFindElement(By by) {
		List<WebElement> listOfElem = null;
		int attempts = 0;
		while (attempts < 4) {
			try {
				listOfElem = driver.findElements(by);
				break;
			} catch (StaleElementReferenceException e) {
			}
			attempts++;
		}
		return listOfElem;
	}

	public void clickAction(WebElement element) throws IOException {
		waitForElementToBeVisible(element, 10);
		waitForElementToBeClickable(element, 10);
		
		actions = new Actions(driver);
		actions.moveToElement(element).build().perform();
		((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid green'", element);
		actions.click(element).build().perform();
		waitUntilPageisReady(driver);
	}

	/**
	 * This method Gets the CSS value.
	 *
	 * @param ele
	 *            the ele
	 * @param property
	 *            the property
	 * @return the CSS value
	 */
	public String getCSSValue(WebElement ele, String property) {
		String value;
		value = ele.getCssValue(property);
		return value;
	}
	
	/**
	 * This method Gets the attribute/property value.
	 *
	 * @param ele
	 *            the element
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
	 * @param ele
	 *            the element
	 * @param property
	 *            the property
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
	 * @param ele
	 *            the element
	 * @param property
	 *            the property
	 * @return the property value
	 */
	public void setAttributeValue(WebElement ele, String property, String Val) {

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", ele, property, Val);

	}

	public String generateRandomString(int noOfchar) {
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789#@abcdefghijklmnopqrstuvwxyz0";
		// String randomString = RandomStringUtils.random(noOfchar, true, true);

		String randomString = RandomStringUtils.randomAlphanumeric(10);
		String randomNumber = RandomStringUtils.randomNumeric(2);

		randomString = randomString + randomNumber;
		randomString = randomString + "@";
		return randomString;
	}

	public String generateRandomStringWithoutSpecialchar(int noOfchar) {
		String randomString = RandomStringUtils.random(noOfchar, true, false);
		return randomString;
	}

	public String generateRandomNumber(String noOfchar) {
		String randomNumber = RandomStringUtils.randomNumeric(Integer.parseInt(noOfchar));
		return randomNumber;
	}

	public List <WebElement> waitForAllElementsToBeVisible(Object object) {
		List <WebElement> elements = null;
		if(object instanceof String) {
			String[] arr = object.toString().split("~");
			if(arr[1].equalsIgnoreCase("ID")) {
				elements = wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.id(arr[0]))));	
			} else if (arr[1].equalsIgnoreCase("tagName")) {
				elements = wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.tagName(arr[0]))));	
			} else if (arr[1].equalsIgnoreCase("name")) {
				elements = wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.name(arr[0]))));	
			} else if (arr[1].equalsIgnoreCase("linkText")) {
				elements = wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.linkText(arr[0]))));	
			} else if (arr[1].equalsIgnoreCase("partialLinkText")) {
				elements = wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.partialLinkText(arr[0]))));	
			} else if (arr[1].equalsIgnoreCase("className")) {
				elements = wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.className(arr[0]))));	
			} else if (arr[1].equalsIgnoreCase("cssSelector")) {
				elements = wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.cssSelector(arr[0]))));	
			} else {
				elements = wait.until(ExpectedConditions.visibilityOfAllElements(driver.findElements(By.xpath(arr[0]))));	
			}
		} else if (object instanceof By) {
			elements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy((By) object));
		} else {
			elements = wait.until(ExpectedConditions.visibilityOfAllElements((WebElement)object));	
		}
		return elements;
	}
	
	public WebElement locateElement(String locatorValue, int timeoutInSeconds) throws Exception {
		WebElement element = null;
		wait = new WebDriverWait(driver, timeoutInSeconds);
		try {
			element = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locatorValue)));
		} catch (Exception ex) {

		}
		
		if(element != null) {
			try {
				element = wait.until(ExpectedConditions.visibilityOf(element));
				return element;
			} catch (Exception ex) {
				return null;
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Function will find all the elements based on the specified xpath
	 * @param: Xpath of the element to be locate
	 * @param: timeout in seconds
	 */
	public List<WebElement> locateElements(String xpath, int timeoutInSeconds) throws Exception {
		List<WebElement> elements = null;
		for(int i = 0; i < timeoutInSeconds; i++) {
			elements = driver.findElements(By.xpath(xpath));
			if(elements != null) {
				break;
			} else {
				Thread.sleep(250);
			}
		}
		return elements;
	}

	/**
	 * Function will wait until to Max timeout until the WebElement is located.
	 * @param: Takes xpath to locate element, time out in seconds, pooling time in seconds
	 * @return: Returns the element
	 */
	public WebElement waitUntilElementIsPresent(int timeOutInSec, final String xpath){
	    Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofSeconds(timeOutInSec))
	    		.pollingEvery(Duration.ofSeconds(500))
	    		.ignoring(NoSuchElementException.class)
	    		.ignoring(StaleElementReferenceException.class);

	    WebElement element = wait.until(new Function<WebDriver, WebElement>() {
	        public WebElement apply(WebDriver driver) {
	            return driver.findElement(By.xpath(xpath));
	        }
	    });
	    return element;
	};
	
	/**
	 * Function will wait until to Max timeout until the WebElement is located.
	 * @param: Takes xpath to locate element, time out in seconds, pooling time in seconds
	 * @return: Returns the element
	 */
	public WebElement waitUntilElementIsPresent(final String xpath, int timeOutInSec, int poolingTimeInSec){
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
	};
	
	/**
	 * Function will wait until to Max timeout until the WebElements are located.
	 * @param: Takes xpath to locate element, time out in seconds, pooling time in seconds
	 * @return: Returns the elements
	 */
	public List<WebElement> waitUntilElementsArePresent(final String xpath, int timeOutInSec, int poolingTimeInSec){
	    Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(Duration.ofSeconds(timeOutInSec))
	    		.pollingEvery(Duration.ofSeconds(poolingTimeInSec))
	    		.ignoring(NoSuchElementException.class)
	    		.ignoring(StaleElementReferenceException.class);

	    List<WebElement> elements = wait.until(new Function<WebDriver, List<WebElement>>() {
	        public List<WebElement> apply(WebDriver driver) {
	            return driver.findElements(By.xpath(xpath));
	        }
	    });
	    return elements;
	};
	
	public WebElement waitForElementToBeVisible(Object object) {
		WebElement element = null;
		if(object instanceof String) {
				element = wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(object.toString()))));	
		} else if (object instanceof By) {
			element = wait.until(ExpectedConditions.visibilityOfElementLocated((By) object));
		} else {
			element = wait.until(ExpectedConditions.visibilityOf((WebElement)object));
		}
		return element;
	}
	
	public boolean waitForElementToBeInVisible(Object object) {
		boolean isElementInvisible = false;
		if(object instanceof String) {
			isElementInvisible = wait.until(ExpectedConditions.invisibilityOf(driver.findElement(By.xpath(object.toString()))));	
		} else if (object instanceof By) {
			isElementInvisible = wait.until(ExpectedConditions.invisibilityOfElementLocated((By) object));
		}
		return isElementInvisible;
	}
	
	public WebElement waitForElementToBeClickable(Object object) {
		WebElement element = null;
		if(object instanceof String) {
				element = wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath(object.toString()))));	
		} else if (object instanceof By) {
			element = wait.until(ExpectedConditions.elementToBeClickable((By) object));
		} else {
			element = wait.until(ExpectedConditions.elementToBeClickable((WebElement)object));
		}
		return element;
	}
	
	public void clickElementOnVisiblity(Object object) {
		WebElement element = waitForElementToBeVisible(object);
		element.click();
	}
	
	/**
     * Function will wait for an element to be Invisible on the page.
     * @param: Xpath of the element to locate
     * @param: timeout in seconds
     */
    public boolean validateAbsenceOfElement(Object object, int timeoutInSeconds) {
        boolean elementStatus = false;
        if(object instanceof String) {
            By by = By.xpath(object.toString());
            elementStatus = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.invisibilityOfElementLocated(by));
        } else {
            elementStatus = wait.withTimeout(Duration.ofSeconds(timeoutInSeconds)).until(ExpectedConditions.invisibilityOf((WebElement) object));           
        }
        return elementStatus;
    }
    
    /**
	 * Sets the parent window handle property with parent window's handle
	 */
	public void setParentWindowHandle() {
		System.setProperty("parentWindowHandle", driver.getWindowHandle());
	}
	
	/**
	 * Switches the focus on the new window
	 */
	public void switchToNewWindow() throws Exception {		
		Set<String> windowHandles = driver.getWindowHandles();
		for(int i = 0; i < 200; i++) {
			windowHandles = driver.getWindowHandles();
			if(windowHandles.size() > 1) {
				break;
			} else {
				Thread.sleep(250);
			}
		}

		Iterator<String> itr = windowHandles.iterator();
		while(itr.hasNext()) {
			String currentHandle = itr.next();
			if(!(currentHandle.equals(System.getProperty("parentWindowHandle")))) {
				driver.switchTo().window(currentHandle);
				waitUntilPageisReady(driver);
			}
		}
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(0));
	}
	
	/**
	 * Switches the focus on the parent window
	 */
	public void switchToParentWindow() {
		driver.switchTo().window(System.getProperty("parentWindowHandle"));
	}
    
	/**
	 * Description: This method will scroll to the bottom of page
	 * @throws: Throws Exception
	 */
	public void scrollToBottomOfPage() throws Exception {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

	/**
	 * This method checks the Radio Button.
	 *
	 */
	public void checkRadioButton(WebElement ele) {
		waitForElementToBeVisible(ele, 30);
		waitForElementToBeClickable(ele, 30);
		if(!ele.isSelected()) {
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
	 * @throws: Throws Exception
	 */
	public void scrollToTopOfPage() throws Exception {
		((JavascriptExecutor) driver).executeScript("window.scrollTo(0, -document.body.scrollHeight)");
	}
		
	/**
	 * Switches the focus out of the frame
	 * @throws InterruptedException 
	 */
	public void switchBackFromFrame() throws InterruptedException {
		driver.switchTo().defaultContent();
		Thread.sleep(1000);		
	}
	
	/**
	 * Description: This method will clear a field value on the screen
	 * @param elem: locator of element where field value needs to be cleared
	 */
	
	public void clearFieldValue(WebElement elem) throws Exception {
		waitForElementToBeClickable(15, elem);
		((JavascriptExecutor)driver).executeScript("arguments[0].style.border='3px solid green'", elem);		
		elem.clear();
		Thread.sleep(2000);
	}
	
	/**
	 * Description: This method will expand icon on Detail page
	 * @param element: WebElement on which action is to be performed
	 */

	public void expandIcon(WebElement element) throws Exception {
		String propertyValue = getAttributeValue(element, "aria-expanded");
		if (propertyValue.equals("false")){
			Click(element);
		}
	}
	/**
	 * Description: Checks the downloaded file in user's system
	 * @param folderPath: Path of the directory
	 * @param fileNameWithExt: Name of the file to be checked in directory
	 * @return boolean: Return the status true/false on basis of existence of downloaded file
	 */
	public boolean verifyFileInGivenFolder(String folderPath, String fileNameWithExt) throws Exception {
		Thread.sleep(2000);
		String fileNameFromDir;
		File sourceFiles = new File(folderPath);
		File[] listOfFiles = sourceFiles.listFiles();
		boolean status = false;

		for (int i = 0; i < listOfFiles.length; i++) {
			fileNameFromDir = listOfFiles[i].getName().toUpperCase();
			if(fileNameFromDir.startsWith(fileNameWithExt.toUpperCase())){
			//if(fileNameWithExt.equalsIgnoreCase(fileNameFromDir)) {
				status = true;
				break;
			}
		}
		return status;
	}
	/**
	 * Deletes all the files from download directory
	 * @param directoryPath: Path from where all files are to be deleted
	 */
	public void deleteAllFilesFromGivenDirectory(String directoryPath) {
		File dir = new File(directoryPath);
		if(dir.listFiles().length > 0) {
			for(File file: dir.listFiles()) {
				if (!file.isDirectory())
					file.delete();
			}
		}
	}
	/**
	 * Description: Checks whether element is displayed on the web page
	 * @param element: WebElement whose availability is to be checked
	 * @param timeOutInSec: Time out in seconds
	 * @return boolean: Returns true / false bases on availability of element
	 * @throws: Exception
	 */
	public boolean isElementAvailable(WebElement element, int timeOutInSec) throws Exception {
		try {
			waitForElementToBeVisible(element, timeOutInSec);
			return true;
		} catch(TimeoutException ex) {
			return false;
		} catch(java.util.NoSuchElementException ex) {
			return false;
		} catch(StaleElementReferenceException ex) {
			return false;
		}
	}
	
	
	public boolean compareDropDownvalues(String actualvalues,String expectedSourcesBPP) {
		String []allexpectedvalues=expectedSourcesBPP.split("\n");
		String valueNotpresent = "";
		for(int i=0;i<allexpectedvalues.length;i++)
		{	if(actualvalues.contains(allexpectedvalues[i]))
			{}
			else{valueNotpresent=valueNotpresent+"\n"+allexpectedvalues[i];}
		}
		if(!valueNotpresent.isEmpty())
		{ReportLogger.INFO("Actual values::\n"+actualvalues+"\n||Expected Values::\n"+expectedSourcesBPP+"\n||Values not found::"+valueNotpresent);
		return false;
		}else
		{ReportLogger.INFO("All values are found\n||Actual values::\n"+actualvalues+"\n||Expected Values::\n"+expectedSourcesBPP);
		 return true;
		 }
	}
	

}
