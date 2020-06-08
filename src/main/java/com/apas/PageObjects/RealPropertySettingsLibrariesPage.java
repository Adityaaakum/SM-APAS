package com.apas.PageObjects;

import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Assertions.SoftAssertion;
import com.apas.Reports.ExtentTestManager;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;
import com.apas.config.testdata;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class RealPropertySettingsLibrariesPage extends ApasGenericPage{
	SoftAssertion softAssert;
	ApasGenericFunctions objApasGenericFunctions;
	Util objUtils;
	
	public RealPropertySettingsLibrariesPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericFunctions= new ApasGenericFunctions(driver);
		objUtils = new Util();
		softAssert = new SoftAssertion();
	}
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//a[@title = 'New']")
	public WebElement newButton;
	
	@FindBy(xpath = "//button[@title = 'Cancel']")
	public WebElement cancelButton;
	
	@FindBy(xpath = "//button[@title = 'Save']")
	public WebElement saveButton;

	@FindBy(xpath = "//label/span[text() = 'RP Setting Name']/../../input")
	public WebElement rpSettingNameEditBox;

	@FindBy(xpath = "//span[text() = 'Status']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement statusDropDown;
	
	@FindBy(xpath = "//input[@title='Search Roll Year Settings']")
	public WebElement searchRollYearSettingsLookup;
	
	@FindBy(xpath = "//label/span[text()='DV Low Income Exemption Amount']/../../input")
	public WebElement dvLowIncomeExemptionAmountEditBox;

	@FindBy(xpath = "//label/span[text()='DV Basic Exemption Amount']/../../input")
	public WebElement dvBasicIncomeExemptionAmountEditBox;
	
	@FindBy(xpath = "//label/span[text()='DV Low Income Household Limit']/../../input")
	public WebElement dvLowIncomeHouseholdLimitEditBox;
	
	@FindBy(xpath = "//label/span[text()='DV Annual Due Date']/../following-sibling::div/input")
	public WebElement dvLowIncomeHouseholdLimitDatePicker;
	
	@FindBy(xpath = "//label/span[text()='DV Annual Due Date 2']/../following-sibling::div/input")
	public WebElement dvAnnualLowIncomeDueDate2DatePicker;
	
	@FindBy(xpath = "//div[@role='alert'][@data-key='success']")
	public WebElement successAlert;

	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;
	
	@FindBy(xpath = "//ul[@class='errorsList']//li")
	public WebElement errorMsgOnTop;
	
	@FindBy(xpath = "//div[contains(@class,'warning')]//div")
	public WebElement warningMsgOnTop;
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Edit']")
	public WebElement editLinkUnderShowMore;
	
	@FindBy(xpath = "//div[contains(@class, 'uiMenuList--default visible positioned')]//div[text() = 'Delete']")
	public WebElement deleteLinkUnderShowMore;
	
	@FindBy(xpath = "//h2[contains(text(),'Delete Real')]//..//following-sibling::div//span[text()='Delete']")
	public WebElement deleteButtonOnPopUp;
	
	@FindBy(xpath = "//div[@data-aura-class='forceSearchDesktopHeader']/div[@data-aura-class='forceSearchInputDesktop']//input")
	public WebElement globalSearchListEditBox;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//button[text() = 'Edit']")
    public WebElement editButton;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text() = 'Status']//..//following-sibling::div//lightning-formatted-text")
	public WebElement statusLabelValueDropDown;
	
	@FindBy(xpath = "//span[text() = 'Next']")
	public WebElement nextButton;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//table//tbody//tr")
    public List<WebElement> numberOfRPSL;
	
	@FindBy(xpath = "//div[@class='uiBlock']//p[@class='detail']//span")
	public WebElement errorMsgforEdit;
	
	@FindBy(xpath = "//li[@title = 'Details']")
	public WebElement detailsTabLabel;
	
	@FindBy(xpath = "//span[text()='Close this window']//ancestor::button")
	public WebElement closeErrorPopUp;
	
	
	
	
	/**
	 * @Description: This method is to handle fields like Status
	 * by clicking the web element and then selecting the given value from drop down
	 * @param element: WebElement for required field
	 * @param value: Like 'Yet to be submit for Approval or Approved' for Status field etc.
	 * @throws Exception
	 */
	public void selectFromDropDown(WebElement element, String value) throws Exception {
		Click(element);
		String xpathStr = "//div[contains(@class, 'uiPopupTarget')]//a[text() = '" + value + "']";
		WebElement drpDwnOption = locateElement(xpathStr, 100);
		drpDwnOption.click();
	}
	
	/** @Description: This method is to handle fields like Roll Year Settings
	 * by clicking on the web element, entering the provided string in textbox
	 * and then selects value from drop down
	 * @param element: WebElement for required field
	 * @param value: Like 2020 0r 2021 for Roll Year Settings field etc.
	 * @throws Exception
	 */
	public void searchAndSelectFromDropDown(WebElement element, String value) throws Exception {
		enter(element, value);
		String xpathStr = "//*[@role='option']//div[@title='" + value + "']";
		locateElement(xpathStr, 2);
		Click(driver.findElement(By.xpath(xpathStr)));
	}
	
	/**
	 * @description: This method will return the error message appeared against the field name passed in the parameter
	 * @param fieldName: field name for which error message needs to be fetched
	 * @throws Exception 
	 */
	public String getIndividualFieldErrorMessage(String fieldName) throws Exception {
		//locateElement("//*/span[text() = '" + fieldName + "']//..//..//..//ul[contains(@class,'has-error')]//li",2);
		return getElementText(driver.findElement(By.xpath("//*/span[text() = '" + fieldName + "']//..//..//..//ul[contains(@class,'has-error')]//li")));
	}

	
	/**
	 * Description: This method will only enter the values of Real Property settings Libraries fields on the application
	 * @param rollYearSettings: Roll Year Settings
	 * @param DVLowIncomeExemptionAmount: DV Low Income Exemption Amount
	 * @param DVBasicExemptionAmount: DV Basic Exemption Amount
	 * @param DVLowIncomeHouseholdLimit: DV Low Income Household Limit
	 * @param DVAnnualLowIncomeDueDate: DV Annual Low Income Due Date
	 * @param DVAnnualLowIncomeDueDate2: DV Annual Low Income Due Date 2
	 * @param status: Status
	 */
	public void enterRealPropertySettingsDetails(Map<String, String> dataMap) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on New Button");
		Click(newButton);
		//selectRecordType("Exemption Limits");
		ExtentTestManager.getTest().log(LogStatus.INFO, "Entering details for Real Property Settings record");
		enter(rpSettingNameEditBox,dataMap.get("Roll Year Settings"));
		selectFromDropDown(statusDropDown,dataMap.get("Status"));
		searchAndSelectFromDropDown(searchRollYearSettingsLookup,dataMap.get("Roll Year Settings"));
		enter(dvLowIncomeExemptionAmountEditBox,dataMap.get("DV Low Income Exemption Amount"));
		enter(dvBasicIncomeExemptionAmountEditBox,dataMap.get("DV Basic Exemption Amount"));
		enter(dvLowIncomeHouseholdLimitEditBox,dataMap.get("DV Low Income Household Limit Amount"));
		enter(dvLowIncomeHouseholdLimitDatePicker, dataMap.get("DV Annual Low Income Due Date"));
		enter(dvAnnualLowIncomeDueDate2DatePicker, dataMap.get("DV Annual Low Income Due Date2"));	
	}
	
	/**
	 * Description: This method will click on save and add a new Real Property settings
	 * @return : returns the text message of success alert
	 */
	public String saveRealPropertySettings() throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on Save Button");
		Click(saveButton);
		//waitForElementToBeVisible(successAlert,30);
		locateElement("//div[@role='alert'][@data-key='success']",2);
		locateElement("//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']",2);
		return getElementText(successAlertText);
	}
	
	/**
	 * Description: This method will click on cancel and verify if success message is present or not
	 * @return : returns the flag either true or false based on whether success message is present or not
	 */
	public boolean cancelRealPropertySettings() throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on Cancel Button");
		Click(cancelButton);
		String locatorKey = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']";
		boolean flag = false;	
		WebElement successAlert = null;
		try {			
			successAlert = driver.findElement(By.xpath(locatorKey));
			flag = true;	    
		} 
		
		catch (org.openqa.selenium.NoSuchElementException e) {
	    }
		
		catch (Exception e) {
	    }
		return flag;
		
	}
	
	/**
	 * Description: This method will verify if 8 years of Real Property settings are displayed or not
	 * @return : returns the flag either true or false based on whether 8 years of RPSL are displayed or not
	 */
	public boolean verify8YearsRPSL() throws Exception {
		boolean flag = false;
		int noOfRPSL = numberOfRPSL.size();
		if(noOfRPSL>=8) {
			flag = true;
		}
		return flag;
	}
	
	

	public void searchAndSelectOptionFromDropDown(WebElement element, String value) throws Exception {
		enter(element, value);
		String yearInValue = value.substring(19, value.length());
		System.out.println("yearInValue: "+yearInValue);
        String xpathStr = "//span[@title ='" + value.toUpperCase() + "']//mark[text() = '" + value.toUpperCase() + "']";
        WebElement drpDwnOption = locateElement(xpathStr, 20);
        drpDwnOption.click();
    }
	

	/**
	 * @description: Clicks on the show more link displayed against the given entry
	 * @param entryDetails: Name of the entry displayed on grid which is to be accessed
	 * @throws Exception
	 */
	public void clickShowMoreLink(String entryDetails) throws Exception {		
		Thread.sleep(2000);
		String xpathStr = "//table//tbody/tr//th//a[text() = '"+ entryDetails +"']//parent::span//parent::th//following-sibling::td//a[@role = 'button']";
		WebElement modificationsIcon = locateElement(xpathStr, 30);
		clickAction(modificationsIcon);
	}
	
	/**
	 * @description: Selects Radio Button displayed corresponding to record Type
	 * @param recordType: Name of the record displayed which is to be selected
	 * @throws Exception
	 */
	public void selectRecordType(String recordType) throws Exception {	
		ExtentTestManager.getTest().log(LogStatus.INFO, "Selecting Record Type: "+recordType);
		String xpathStr = "//span[text()='"+recordType+"']//..//preceding-sibling::div//input";
		WebElement radBtn = locateElement(xpathStr, 30);
		checkRadioButton(radBtn);
		Click(nextButton);
		waitUntilPageisReady(driver);
		
	}
	

	/**
	 * @description: Selects Radio Button displayed corresponding to record Type
	 * @param recordType: Name of the record displayed which is to be selected
	 * @throws Exception
	 */
	public String verifyEditAccess(int noOfRPSL) throws Exception {	
		String errorMsgText = null;
		
		 for (int i = 1; i <=noOfRPSL; i++) { 
			  System.out.println("inside for " +i); 
			  locateElement("//div[contains(@class,'windowViewMode-normal')]//table//tbody//tr["+ i + "]//td[8]//span//span",3); 		  				  
			  WebElement status = driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal')]//table//tbody//tr["+ i + "]//td[8]//span//span"));  
			  System.out.println("Now Print Status"+status.getText());
			  
		// Step1: Verifying if Status of RPSL is 'Approved' 
			  String actualStatus = status.getText().trim(); 
			  String expectedStatus = "Approved"; 
			  if(actualStatus.equals(expectedStatus.trim())) {
				  System.out.println("inside if " + i);
				  
		// Step2: Clicking on 'Approved' RPSL record 
			  locateElement("//div[contains(@class,'windowViewMode-normal')]//table//tbody//tr["+ i + "]//th//a",3);
			  WebElement exemptionLink = driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal')]//table//tbody//tr["+ i + "]//th//a"));		  				  
			  System.out.println("Exemption Link text: " + exemptionLink.getText());
			  Click(exemptionLink);
			  waitUntilPageisReady(driver);
			  ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on 'Edit' button for record whose status is 'Approved'");
			  Click(editButton);			  
			  locateElement("//div[@class='uiBlock']//p[@class='detail']//span",3);
			  System.out.println("eror: "+errorMsgforEdit.getText());
			  errorMsgText =  errorMsgforEdit.getText();
			  }
			  break;
			}
		return errorMsgText;
	
	}
	

	 public void removeExistingRealPropertySettingEntry(String rollYear) throws Exception {
	        SalesforceAPI objSalesforceAPI = new SalesforceAPI();
	        String queryForID = "SELECT Id FROM Real_Property_Settings_Library__c where Name = 'Exemption Limits - " + rollYear +"'";
	        objSalesforceAPI.delete("Real_Property_Settings_Library__c", queryForID);
	    }
	
	
	 /**
		 * @description: Verify if given RPSL record already exists then delete it
		 * @param rollYear: Roll Year for which RPSL to be deleted
		 * @throws Exception
		 */
		public void verifyAndDeleteExistingRPSL(String rollYear) throws Exception {	
			String locatorKey = "//table//tbody/tr//td//a[text() = '"+ rollYear +"']";
			boolean flag = false;				
			try {			
				WebElement ele = driver.findElement(By.xpath(locatorKey));
				flag = true;
				System.out.println("Flag inside try: "+ flag);
			} catch (org.openqa.selenium.NoSuchElementException e) {
		    	flag =  false;
		    	System.out.println("Flag inside catch: "+ flag);
		    }
			catch (Exception e) {
		    	flag =  false;
		    }
			if(flag){
				removeExistingRealPropertySettingEntry(rollYear);
			}	
		
		}
	
	
		/**
		 * Function will clear existing value and enter the value in the element.
		 *
		 * @param elem
		 *            Element in which value needs to be entered
		 * @param value
		 *            the value needs to be entered
		 * @throws Exception
		 *             the exception
		 */
	 public void clearAndEnterValue(WebElement elem, String value) throws Exception {
	        waitForElementToBeClickable(elem).sendKeys(Keys.chord(Keys.CONTROL, "a"));
	        elem.sendKeys(Keys.BACK_SPACE);
	        enter(elem, value);
	    }
	
	 
	 /**
		 * Function will return true if element is not visible.
		 *
		* @param elem
	 *            the Webelement
	 * @return true, if successful
	 * @throws Exception
     *             the exception
	 */
	 public boolean verifyButtonNotPresent(String locatorKey) throws Exception {			
			boolean flag = false;			
			try {	
				
				WebElement ele = driver.findElement(By.xpath(locatorKey));
				flag = true;        
		    
			} catch (org.openqa.selenium.NoSuchElementException e) {
				flag = false;		    	
		    }
			
			return flag;			
	    }
	 
	 /**
		 * @description: This method will edit the Status of RPSL
		 * @param rollYear: Roll Year for which RPSL status to be edited
		 * @throws Exception
		 */
	 public void editRPSLStatus(String rollYear, String status) throws Exception {			
		
		//Step1: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step2: Selecting 'All' List View
		objApasGenericFunctions.displayRecords("All");
		
		//Step3: Fetching value of RPSL for which status needs to be updated
		String value = "Exemption Limits - "+ rollYear;
		
		//Step4: Searching and selecting the RPSL
		clickShowMoreLink(value);
		clickAction(waitForElementToBeClickable(editLinkUnderShowMore));
		waitUntilPageisReady(driver);
		
		//Step5: Edit the RPSL Status
		ExtentTestManager.getTest().log(LogStatus.INFO, "Editing the Status field to '"+status + "'");
		selectFromDropDown(statusDropDown,status);		
		
		//Step6: Saving the RPSL after editing 'Status' dropdown
		String strSuccessAlertMessage = saveRealPropertySettings();
		System.out.println("success message is :"+strSuccessAlertMessage);
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + value + "\" was saved.","RPSL is edited successfully");			 
	 }
	 
	 /**
		 * @description: This method will create the RPSL
		 * @param dataMap: Data with which RPSL is created
		 * @throws Exception
		 */
	 public void createRPSL(Map<String, String> dataMap) throws Exception {			
		 String strRollYear = dataMap.get("Roll Year Settings");
		 
		//Step1: Opening the Real Property Settings Libraries module
		objApasGenericFunctions.searchModule(modules.REAL_PROPERTY_SETTINGS_LIBRARIES);
		
		//Step2: Selecting 'All' List View
		objApasGenericFunctions.displayRecords("All");
		
		//Step3: Delete current Roll Year's RPSL if it already exists	
		verifyAndDeleteExistingRPSL(strRollYear);
		 		
		//Step4: Creating the RPSL record for current year
		ExtentTestManager.getTest().log(LogStatus.INFO, "Adding current year's 'Real Property Settings' record with following details: "+ dataMap);
		enterRealPropertySettingsDetails(dataMap);
		
		//Step7: Clicking on Save button & Verifying the RPSL record for current year after creation		
		String strSuccessAlertMessage = saveRealPropertySettings();
		softAssert.assertEquals(strSuccessAlertMessage,"Real Property Settings Library \"" + strRollYear + "\" was created.","Verify the User is able to create Exemption limit record for the current roll year");	
		
	 }
	 
	 	 
}