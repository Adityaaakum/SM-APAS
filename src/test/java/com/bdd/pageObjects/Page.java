package com.bdd.pageObjects;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.bdd.constants.SuiteConstants;
import com.bdd.initialSetUp.BrowserDriver;
import com.bdd.initialSetUp.TestBase;
import com.cucumber.listener.Reporter;



public class Page extends TestBase {

	Logger logger = Logger.getLogger(Page.class);

	public static WebDriver driver = null;

	public WebDriverWait wait;

	public FluentWait<WebDriver> flwait;

	Actions actions;

	/**
	 * Instantiates a new page.
	 *
	 * @param driver
	 *            the driver
	 */
	public Page(WebDriver driver) {

		this.driver = driver;
		this.wait = new WebDriverWait(driver, 30);
		this.flwait = new FluentWait<WebDriver>(driver).withTimeout(30, TimeUnit.SECONDS)
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
	 * @param element the element
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
	 * @param element the element
	 */
	public boolean verifyElementDisabled(WebElement element) {
		boolean flag = true ;
		try {

			if (element.isEnabled()) {
				   
				flag = false ;
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
			System.out.println("Element is not visible :" +elem);
			//TestBase.reportLogger(flag, "The element is not present :"+elem);
		}
		return flag;
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
	public void navigateTo(WebDriver driver, String url) throws IOException {
		waitUntilPageisReady(driver);
		driver.get(url);
		waitUntilPageisReady(driver);
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
	 * Function will wait until the page is not in ready state.
	 *
	 * @param driver
	 *            the Webdriver
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void waitUntilPageisReady(WebDriver driver) throws IOException {

		ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver driver) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");
			}
		};

		int time = SuiteConstants.PAGE_LOAD_TIMEOUT ;
		WebDriverWait wait = new WebDriverWait(driver, time);
		wait.until(pageLoadCondition);
	}

	/**
	 * Function will return true flag if the URL returns a valid response status.
	 *
	 * @param elem
	 *            the Webelement
	 * @return true, if successful
	 */
	public boolean verifyURLStatus(WebElement elem) {
		boolean flag = true;
		String url = elem.getAttribute("href");
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		try {
			HttpResponse response = client.execute(request);
			System.out.println("Page status is :" + response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() != 200)
				flag = false;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
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
		
		waitForElementToBeVisible(elem, 30);
		waitForElementToBeClickable(elem, 50);
		if (browserName.equalsIgnoreCase("Edge")) {
			javascriptClick(elem);
		}
		else if(browserName.equalsIgnoreCase("IE")){
			elem.sendKeys(Keys.ENTER);
		}
		else {
			elem.click();
		}

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
		waitForElementToBeClickable(elem, 30);
		/*if (ExcelDriver.getBrowserVal().equalsIgnoreCase("Edge")) {
			actions=new Actions(driver);		
			actions.moveToElement(elem);		
			actions.click();		
			Thread.sleep(1000);		
			actions.sendKeys(value);
			//actions.sendKeys(Keys.TAB);			
			actions.build().perform();	
			Thread.sleep(1000);	
		}		
		else*/
        elem.clear();
		elem.sendKeys(value);
		Thread.sleep(3000);
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
			// Date date2 = new SimpleDateFormat("yyyy-MM-dd").parse(currentDate);
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
			flwait.withTimeout(timeoutInSeconds, TimeUnit.SECONDS).pollingEvery(100, TimeUnit.MILLISECONDS)
					.ignoring(NoSuchElementException.class)
					.ignoring(org.openqa.selenium.StaleElementReferenceException.class)
					.until(ExpectedConditions.elementToBeClickable(element));
		} catch (org.openqa.selenium.StaleElementReferenceException e) {
			e.printStackTrace();

		}
	}

	/**
	 * Function will wait for an element to be visible on the page.
	 *
	 * @param element
	 *            the element
	 * @param timeoutInSeconds
	 *            the timeout in seconds
	 */
	public void waitForElementToBeVisible(WebElement element, int timeoutInSeconds) {

		try {
			wait.withTimeout(timeoutInSeconds, TimeUnit.SECONDS).until(ExpectedConditions.visibilityOf(element));
		} catch (org.openqa.selenium.StaleElementReferenceException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Function will wait for an element to be Invisible on the page.
	 *
	 * @param by
	 *            the by
	 * @param timeoutInSeconds
	 *            the timeout in seconds
	 */
	public void waitForElementToBeInVisible(By by, int timeoutInSeconds) {

		try {
			wait.withTimeout(timeoutInSeconds, TimeUnit.SECONDS)
					.until(ExpectedConditions.invisibilityOfElementLocated(by));
		} catch (org.openqa.selenium.StaleElementReferenceException e) {

			e.printStackTrace();
		}
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
		Select select = new Select(elem);
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
		String sText1 = new Select(elem).getFirstSelectedOption().getAttribute("value");
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
		elem.click();
		new Select(elem).selectByVisibleText(value);
		waitUntilPageisReady(driver);
		return true;
	}
	
	
	public boolean SelectByIndex(WebElement elem, int index) throws Exception {
		elem.click();
		new Select(elem).selectByIndex(index);
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
	 * Function will wait until to Max timeout until the weblement is located.
	 *
	 * @param element
	 *            the element
	 * @return true, if successful
	 */
	public boolean waitUntilElementIsPresent(final WebElement element) {

		Integer timeoutInSeconds = SuiteConstants.MAX_TIMEOUT;
		FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver);
		wait.pollingEvery(1, TimeUnit.SECONDS);
		wait.withTimeout(timeoutInSeconds, TimeUnit.SECONDS);
		wait.ignoring(NoSuchElementException.class);
		WebElement elem = wait.until(new Function<WebDriver, WebElement>() {
			public WebElement apply(WebDriver driver) {
				if (element.isEnabled()) {
					// System.out.println("Element is Present.");
					return element;
				} else {
					return null;
				}
			}
		});
		// If element is found then it will display the status
		return elem.isEnabled();
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
		actions = new Actions(driver);
		actions.moveToElement(element).build().perform();
		actions.click(element).build().perform();
		waitUntilPageisReady(driver);
	}
	
	/**
	 * This method Gets the CSS value.
	 *
	 * @param ele the ele
	 * @param property the property
	 * @return the CSS value
	 */
	public String getCSSValue(WebElement ele, String property) {
		String value;
		value=ele.getCssValue(property);
		return value;
	}
	
	/**
	 * This method Gets the attribute/property value.
	 *
	 * @param ele the element
	 * @param property the property
	 * @return the property value
	 */
	public String getAttributeValue(WebElement ele, String property) {
		String value;
		value=ele.getAttribute(property);
		return value;
	}
	
	
	/**
	 * This method set the attribute/property value.
	 *
	 * @param ele the element
	 * @param property the property
	 * @return the property value
	 */
	public void setAttributeValue(WebElement ele, String property, String Val) {
		
		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", 
				ele, property, Val);
		
	}
	
	public String generateRandomString(int noOfchar){
		String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789#@abcdefghijklmnopqrstuvwxyz0";
		//String randomString = RandomStringUtils.random(noOfchar, true, true);
		
		String randomString =RandomStringUtils.randomAlphanumeric(10);
		String randomNumber= RandomStringUtils.randomNumeric(2);
		
		randomString= randomString + randomNumber;
		randomString = randomString + "@";
		return randomString;
	}
	
	public String generateRandomStringWithoutSpecialchar(int noOfchar){		
		String randomString = RandomStringUtils.random(noOfchar, true, false);
		return randomString;
	}

	public String generateRandomNumber(String noOfchar) {
		String randomNumber= RandomStringUtils.randomNumeric(Integer.parseInt(noOfchar));
		return randomNumber;	
	}

	
}
