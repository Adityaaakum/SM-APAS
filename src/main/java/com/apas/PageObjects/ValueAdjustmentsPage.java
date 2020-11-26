package com.apas.PageObjects;


import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Assertions.SoftAssertion;
import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;


public class ValueAdjustmentsPage extends Page {
	
	Logger logger;
	Page objPage;
	SoftAssertion softAssert1;
	ApasGenericFunctions apasGenericObj;
	BuildingPermitPage objBuildingPermitPage;
	
	public ValueAdjustmentsPage(RemoteWebDriver driver) {
		super(driver);
		
		PageFactory.initElements(driver, this);
		logger = Logger.getLogger(LoginPage.class);
		objPage=new Page(driver);
		softAssert1=new SoftAssertion();
		apasGenericObj= new ApasGenericFunctions(driver);
		objBuildingPermitPage=new BuildingPermitPage(driver);
		
		
		
	}
	@FindBy(xpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//div//table//tbody//tr//td[1]//following-sibling::th//a")
	public List<WebElement> VAlist;
	
	@FindBy(xpath="//div//div//table//tbody//tr//td[4]//span/span")
	public List<WebElement> vaStatusFromList;	
	

	@FindBy(xpath="//div[contains(@id,'error-message')]")
	public WebElement editFieldErrorMsg;
		
	@FindBy(xpath="//div[@role='dialog']//h2[contains(.,'Edit')]")
	public WebElement editVAPopUp;
	
	
	
	//VAR elements
	
	public String vaAnnualFormReceiveddate="Annual Form Received Date";
		
	public String vaTotalAnuualHouseholdIncome="Total Annual Household Income";
		
	@FindBy(xpath = "//div//span[text()='Is Initial Filing Exemption?']//parent::label//parent::span//input")
	public WebElement vaInitialFilingFlag;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//li[@title='Value Adjustments']//a[contains(.,'Value Adjustments')]")
	public WebElement valueAdjustmentTab;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[@title='Value Adjustments']")
	public WebElement valueAdjustmentsCountLabel;
	
	@FindBy(xpath = "//div//span[text()='Roll Year Due Date']//parent::div//following-sibling::lightning-helptext/following-sibling::div//span//lightning-formatted-text")
	public WebElement penaltyDate1;
	
	@FindBy(xpath = "//div//span[text()='Roll Year Due Date 2']//parent::div//following-sibling::lightning-helptext/following-sibling::div//span//lightning-formatted-text")
	public WebElement penaltyDate2;
	
	
	@FindBy(xpath = "//div[starts-with(@class,'windowViewMode-normal oneContent active lafPageHost')]//button[@name='Edit']")
	public WebElement vaEdit;
	
	@FindBy(xpath = "//div[starts-with(@class,'windowViewMode-normal oneContent active lafPageHost')]//span[text()='End Date']//parent::div//following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement vaEndDateDetails;
	
	@FindBy(xpath = "//div//span[text()='Status']//parent::div//following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement vaStatusDetails;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text()='Determination']//parent::div//following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement vaDetermination;
	
	public String vaEditDeterminationDropDown="Determination";
		
	public String vaDeterminationDropDwon="Determination";
	
	@FindBy(xpath = "//div//span[text()='Roll Year Due Date']//parent::div//following-sibling::lightning-helptext/following-sibling::div//span//lightning-formatted-text")
	public WebElement vaRollYearDueDate;
	
	@FindBy(xpath = "//div//span[text()='Roll Year Due Date 2']//parent::div//following-sibling::lightning-helptext/following-sibling::div//span//lightning-formatted-text")
	public WebElement vaRollYearDueDate2;
	
	@FindBy(xpath = "//div//span[text()='Roll Year Settings']//parent::div/following-sibling::lightning-helptext/following-sibling::div//slot//a")
	public WebElement vaRollYear;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[contains(.,'Penalty Percentage')]/parent::div/following-sibling::div/span//slot[@slot='outputField']//slot/lightning-formatted-number")
	public WebElement vaPenaltyPercentage;
	
	@FindBy(xpath = "//span[contains(.,'Net Exemption Amount')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaNetExemptionAmount;
	
	@FindBy(xpath = "//span[contains(.,'Exemption Amount Calculated')]/parent::div/following-sibling::div/span")
	public WebElement vaExemptionAmountCalculated;
	
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[contains(.,'Penalty Amount Calculated')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaPenaltyAmountCalculated;
	
	@FindBy(xpath = "//div//span[contains(.,'Roll Year Basic Reference Amount')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaRollYearBasicRefAmount;
	
	@FindBy(xpath = "//div//span[contains(.,'Roll Year Low Income Reference Amount')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaRollYearLowIncomeRefAmount;
	
	
	@FindBy(xpath = "//div//span[contains(.,'Tax Year Prorated Percentage')]/parent::div/following-sibling::div/span//lightning-formatted-number")
	public WebElement vataxYearProratedPercentage;
	
	@FindBy(xpath = "//div//span[contains(.,'Determination')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaDeterminationType;
	
	@FindBy(xpath = "//div//span[text()='Open Roll Start Date']//parent::div/following-sibling::lightning-helptext/following-sibling::div//lightning-formatted-text")
	public WebElement openRollStartDate;
	
	
	@FindBy(xpath = "//div//div//table//tbody//tr//td[1]//following-sibling::td[1]")
	public List<WebElement> vaStartDateFromList;
	
	@FindBy(xpath = "//div//div//table//tbody//tr//td[1]//following-sibling::td[2]//span/span")
	public List<WebElement> vaEndDateFromList;
	
	@FindBy(xpath = "//div[text()='Value Adjustments")
	public WebElement valueAdjustmentViewAll;
	
	@FindBy(xpath = "//input[@title='Search Roll Year Settings']")
	public WebElement searchRollYearSettings;

	@FindBy(xpath = "//span[text() = 'Determination']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement detemination;
	
	@FindBy(xpath = "//input[@title='Search Real Property Settings Libraries']")
	public WebElement searchRealPropertySettingsLibraries;
	
	@FindBy(xpath = "//span[text() = 'Determination Denial Reason']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement determinationDenialReason;
	
	@FindBy(xpath = "//input[@title='Search Exemption']")
	public WebElement searchExemption;
	
	public String determinationDenialDetail="Determination Denial Detail";
		
	public String annualFormReceivedDate="Annual Form Received Date";
	
	@FindBy(xpath = "//div//span[text()='Annual Form Received Date']//parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement annualFormReceivedDateOnUI;
	
	
	@FindBy(xpath = "//label[contains(.,'Start Date')]/following::input[1]")
	public WebElement startDate;
	
	@FindBy(xpath = "//label[contains(.,'End Date')]/following::input[1]")
	public WebElement endDate;
	
	@FindBy(xpath = "//div//label[contains(.,'Total Annual Household Income')]/following::input[1]")
	public WebElement totalAnnualHouseholdIncome;
	
	@FindBy(xpath = "//label[contains(.,'Penalty Amount - User Adjusted')]/following::input[1]")
	public WebElement penaltyAmountUserAdjusted;
	
	public String penaltyAdjustmentReason="Penalty Adjustment Reason";
	
	@FindBy(xpath = "//label[contains(.,'Penalty Adjustment Other Reason Detail')]/following::textarea[1]")
	public WebElement penaltyAdjustmentOtherReasonDetail;
	
	
	@FindBy(xpath = "//input[contains(@placeholder,'Search this list...')]")
	public WebElement searchList;

//--------- Deepika's Locators ----------------
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//table//tbody//tr")
    public List<WebElement> numberOfValueAdjustments;
	
	public String startDateValueLabel ="Start Date";
	
	public String endDateValueLabel ="End Date";

	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Roll Year Basic Reference Amount']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement basicReferenceAmountLabel;
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Roll Year Low Income Reference Amount']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement lowIncomeReferenceAmountLabel;
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Roll Year Settings']//parent::div//following-sibling::div//a")
    public WebElement rollYearSettingsLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[text() = 'Edit']")
    public WebElement editButton;
	
	public String totalAnnualHouseholdIncomeEditBox="Total Annual Household Income";
		
	public String annualFormReceivedDateEditBox="Annual Form Received Date";
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@title='Save' and text()='Save']")
    public WebElement saveButton;
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Roll Year Low Income Threshold Amount']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement rollYearLowIncomeThreshholdAmountLabel;
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Roll Year Low Income Due Date']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement rollYearLowIncomeDueDateLabel;
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Determination']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement determinationValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Number of days']//..//following-sibling::div//lightning-formatted-number")
    public WebElement noOfDaysValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Exemption Amount Calculated']//..//following-sibling::div//lightning-formatted-text")
    public WebElement exemptionAmountCalculatedValueLabel;
	
	public String taxStartDateValueLabel ="Tax Start Date";
	
	public String taxEndDateValueLabel ="Tax End Date";
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Penalty Amount Calculated']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement penaltyAmtCalcValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Penalty Amount - User Adjusted']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement penaltyAmtUserAdjustValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'slds-tabs_default')]//span[text()='Net Exemption Amount']//..//following-sibling::div//lightning-formatted-text")
    public WebElement netExemptionAmountCalculatedValueLabel;
	
	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;
	
	@FindBy(xpath = "//div//li[@title='Value Adjustments']//a")			
    public WebElement valueAdjustmentRelatedListTab;
	
	@FindBy(xpath = "//div//span[text()='View All']")
    public WebElement viewAllLink;
	
	@FindBy(xpath = "//div//span[text()='Roll Year Low Income Late Penalty']//parent::div//following-sibling::lightning-helptext/following-sibling::div//lightning-formatted-number")
    public WebElement vaRollYearLowIncomeLatePenaltyLabel;
	
	@FindBy(xpath = "//div//span[text()='Roll Year Low Income Late Penalty 2']//parent::div//following-sibling::lightning-helptext/following-sibling::div//lightning-formatted-number")
    public WebElement vaRollYearLowIncomeLatePenalty2Label;
	
	@FindBy(xpath = "//div[@class='test-id__field-label-container slds-form-element__label no-utility-icon']//span[text()='Name']")
    public WebElement vAnameLabel;
	
	@FindBy(xpath = "//div[@class='test-id__field-label-container slds-form-element__label no-utility-icon']//span[text()='Name']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement vAnameValue;
	
	@FindBy(xpath = "//span[text()='Start Date']//parent::div//parent::div[@class='slds-form-element slds-hint-parent test-id__output-root slds-form-element_readonly recordlayout_in_editing_mode slds-form-element_horizontal']")
	public WebElement startDateReadOnlyField;
	
	@FindBy(xpath = "//span[text()='End Date']//parent::div//parent::div[@class='slds-form-element slds-hint-parent test-id__output-root slds-form-element_readonly recordlayout_in_editing_mode slds-form-element_horizontal']")
	public WebElement endDateReadOnlyField;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//label[text()='Determination']/..//input//parent::div/following-sibling::div//span[@class='slds-media__body']")
	public List<WebElement> determinationFieldValuesList;
	
	@FindBy(xpath="//div[contains(@role,'listitem')]//span[text()='Penalty Adjustment Reason']//..//parent::div//following-sibling::ul//li")
	public WebElement errMsgPenaltyAdjstmntRsn;
	
	@FindBy(xpath="//div[contains(@role,'listitem')]//span[text()='Penalty Adjustment Other Reason Detail']//..//parent::div//following-sibling::ul//li")
	public WebElement errMsgPenaltyAdjstmntOthRsnDetail;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[@title='Cancel' and text()='Cancel']")
    public WebElement cancelButton;
	
	@FindBy(xpath = "//label[text() = 'Determination']//following-sibling::div//input")
    public WebElement determinationDropDown;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//tr//span[contains(text(),'Active')]//..//parent::td//..//span[contains(text(),'Basic Disabled')]//..//..//preceding-sibling::th//a")
    public WebElement activeBasicDetVA;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//tr[1]//span[contains(text(),'Start Date')]//..//..//parent::th//parent::tr//..//..//tbody//span[text()='7/1/2020']//..//..//preceding-sibling::th//a")
    public WebElement vAforRY2020;
	
	public String xPathStatus = "//div//span[@title='Status']";
	public String xPathRollYearLowIncomeThresholdAmount = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text()='Roll Year Low Income Threshold Amount']//parent::div//following-sibling::div//lightning-formatted-text";
//--------- Deepika's Locators ----------------
	

	
	/**
	 * @description: This method will return the number of VA's based on Start date,EndDateOFRating and Current Roll Year
	 * @return : returns the number of VA's
	 * @param  :startDate, endDateOfRating, currentRollYear
	 * @throws Exception 
	 */

	
	
public int verifyValueAdjustments(String startDate, String endDateOfRating,String currentRollYear) throws Exception {
	int actualVAtoBeCreated=0;
	int maxStartVARollYearAsPerCurrentRollYear=Integer.parseInt(currentRollYear)-8;//2020-8=2012
	int finalStartYear = 0;
	String[] startDateSplit=startDate.split("/");
	int startYear=Integer.parseInt(startDateSplit[2]);//1/14/2012=2012
	if(Integer.parseInt(startDateSplit[0])<7){ startYear=Integer.parseInt(startDateSplit[2])-1;}//2011
		if(endDateOfRating!=null && !(endDateOfRating.isEmpty()))
		{
			String[] endDateYear=endDateOfRating.split("/");
			int endYear=Integer.parseInt(endDateYear[2]);//6/30/2018
			if(Integer.parseInt(endDateYear[0])<7){ endYear=Integer.parseInt(endDateYear[2])-1;}//2017
			if(startYear>=maxStartVARollYearAsPerCurrentRollYear)
			{finalStartYear=startYear;}
			else
			{finalStartYear=maxStartVARollYearAsPerCurrentRollYear;}//2012
			actualVAtoBeCreated = (endYear-finalStartYear)+1;//6
		}
		else
		{	if(startYear>=maxStartVARollYearAsPerCurrentRollYear)
				{finalStartYear=startYear;}
			else
				{finalStartYear=maxStartVARollYearAsPerCurrentRollYear;}
			actualVAtoBeCreated = (Integer.parseInt(currentRollYear)-finalStartYear)+1;}
return actualVAtoBeCreated;							//	2020			-2012			
}


/**
 * @description: This method will return the Penalty Percentage based on VA type,determination, Application date
 * @return : returns the Penalty percentage
 * @param  :VA type,determination, Application date
 * @throws Exception 
 */


public double calculatePenaltyPercentageForVA(boolean vaType,String applicationdate,String graceEndDate,String determination) throws ParseException{
	SimpleDateFormat sdfo = new SimpleDateFormat("MM/dd/yyyy");
	Date appdate = sdfo.parse(applicationdate);
	Date gracedate=sdfo.parse(graceEndDate);
	double penaltyPercentageAsPerLogic = 0.0;
	Date vaDueDate=sdfo.parse(vaRollYearDueDate.getText().trim());
	Date vaDueDate2=sdfo.parse(vaRollYearDueDate2.getText().trim());
	
	if(appdate.compareTo(gracedate)<0 || !vaType)
		{penaltyPercentageAsPerLogic=0.0;}
	 else
	 {		if(appdate.compareTo(vaDueDate2)>0)
			{penaltyPercentageAsPerLogic=15.0;}
			else if(appdate.compareTo(vaDueDate)>0 && appdate.compareTo(vaDueDate2)<0)
			{penaltyPercentageAsPerLogic=10.0;}
			else 
			{penaltyPercentageAsPerLogic=0.0;}
	 }
	return penaltyPercentageAsPerLogic;
}

/**
 * @description: This method will return the Penalty Amount based on VA type,determination, Penalty percentage
 * @return : returns the Penalty Amount
 * @param  :VA type,determination, Penalty percentage
 * @throws Exception 
 */
public double calculatePenaltyAmountForVA(double penaltyPercentage, boolean vaType, String determination) throws ParseException{
	String taxyearProratedString=vataxYearProratedPercentage.getText().trim();
	double taxyearProrated=Double.parseDouble(taxyearProratedString.substring(0,taxyearProratedString.indexOf(".")));
	double rollYearBasicRefAmt=converToDouble(vaRollYearBasicRefAmount.getText().trim());
	double rollYearLowIncomeRefAmt=converToDouble(vaRollYearLowIncomeRefAmount.getText().trim());
	double penaltyAmtAsPerFormula=0;
	
	if(vaType)
	{
			if(determination.equals("Basic Disabled Veterans Exemption"))
			{penaltyAmtAsPerFormula= Precision.round( rollYearBasicRefAmt * (penaltyPercentage/100) * (taxyearProrated/100) , 2);}
			else
			{penaltyAmtAsPerFormula= Precision.round( rollYearLowIncomeRefAmt * (penaltyPercentage/100) * (taxyearProrated/100) , 2);
			}
	}


	else
	{
		if(determination.equals("Basic Disabled Veterans Exemption"))
			{penaltyAmtAsPerFormula=0.0;
			}
		else
			{penaltyAmtAsPerFormula= Precision.round( (rollYearLowIncomeRefAmt-rollYearBasicRefAmt) * (penaltyPercentage/100) * (taxyearProrated/100) , 2); }
	}
	return penaltyAmtAsPerFormula;
}


/**
 * @description: This method will return if VA type is Initial filing or Annual
 * @return : returns true if Initial filing , false if Annual
 * @param  :Application date
 * @throws Exception 
 */
public boolean verifyIfInitialFilingOrAnnualVA(String applicationdate) throws Exception{
	SimpleDateFormat sdfo = new SimpleDateFormat("MM/dd/yyyy");
	Date appdate = sdfo.parse(applicationdate);
	objPage.clickElementOnVisiblity(vaRollYear);
	objPage.waitForElementToBeVisible(openRollStartDate, 10);
	Date openRollstartdate=sdfo.parse(openRollStartDate.getText().trim());
	driver.navigate().back();
	objPage.waitForElementToBeVisible(vaPenaltyPercentage, 10);
	return (appdate.compareTo(openRollstartdate)>0);
}


/**
 * @description: This method will return VA name
 * @return : returns VA name
 * @param  :End date of rating
 *
 */

public String findVANameBasedOnEndDate(String endDateOfRating) {
	return driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//td[@data-label='End Date']//*[text()='"+endDateOfRating+"']")).getText();
}

/**
 * @description: This method will count of VA's based on Column and values
 * @return : returns VA count
 * @param  :column, value
 *
 */


public int fetchVACountBasedOnParameters(String column,String value) {
	return driver.findElements(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//td[@data-label='"+column+"']//*[text()='"+value+"']")).size();
}

/**
 * @description: This method converts the Amounts into double
 * @return : returns the amount into double
 * @param  :amount
 *
 */

public double converToDouble(Object amount){
	String amtWithDollar=(String)amount;
	String amtAsString=amtWithDollar.substring(1, amtWithDollar.length());
	String finalAmtAsString=amtAsString.replace(",","");
	double convertedAmt=Double.parseDouble(finalAmtAsString);
	return convertedAmt;
	}

/**
 * @description: This method will return the Tax Year Prorated Percentage Calculated
 * @return : returns the Tax Year Prorated Percentage Calculated
 * @throws Exception 
 */
public float verifyTaxYearProatedPercentage() throws Exception {
	objPage.waitForElementToBeClickable(60, startDateValueLabel);
	float numberOfDays = DateUtil.getDateDiff(apasGenericObj.getFieldValueFromAPAS(startDateValueLabel),apasGenericObj.getFieldValueFromAPAS(endDateValueLabel));
	locateElement("//div[contains(@class,'slds-tabs_default')]//span[text()='Roll Year Settings']//parent::div//following-sibling::div//a", 2);
	Click(rollYearSettingsLabel);
	objPage.waitForElementToBeClickable(60, taxStartDateValueLabel);
	float totalNoOfDays = DateUtil.getDateDiff(apasGenericObj.getFieldValueFromAPAS(taxStartDateValueLabel),apasGenericObj.getFieldValueFromAPAS(taxEndDateValueLabel));
	driver.navigate().back();
	float taxYearProatedPercentage = 0;
	float taxYearProated = 0;
	taxYearProatedPercentage = (numberOfDays / totalNoOfDays)*100;	
	DecimalFormat d = new DecimalFormat("0.0000");
	taxYearProated = Float.parseFloat(d.format(taxYearProatedPercentage));
    return taxYearProated;	
}
/**
 * @description: This method will return the Basic Exemption Amount Calculated
 * @return : returns the Basic Exemption Amount Calculated
 * @throws IOException 
 */
public float calculateBasicExemptionAmount() throws Exception {
	double taxYearProatedPercentage = verifyTaxYearProatedPercentage();
	float basicExemptionAmt = apasGenericObj.convertToFloat(basicReferenceAmountLabel.getText());
	DecimalFormat d = new DecimalFormat("0.00");					
	float exemptionAmountCalculated = Float.parseFloat(d.format((basicExemptionAmt*taxYearProatedPercentage)/100));
	return exemptionAmountCalculated;
}

/**
 * @description: This method will return the Basic Exemption Amount Calculated
 * @return : returns the Basic Exemption Amount Calculated
 * @throws IOException 
 */
public float calculateNetExemptionAmount(float exemptionAmountCalculated) throws Exception {
	float netExemptionAmountCalculated=0;
	float penaltyAmt=0;
	float penaltyAmtUserAdjustCal=0;
	
	String penaltyAmtCalculated = penaltyAmtCalcValueLabel.getText();
	String penaltyAmtUserAdjust = penaltyAmtUserAdjustValueLabel.getText();		
	
	if("".equals(penaltyAmtCalculated) && "".equals(penaltyAmtUserAdjust)) {
		penaltyAmt = (float) 0.00;	
	}else if("".equals(penaltyAmtUserAdjust) && !("".equals(penaltyAmtCalculated))) {
		penaltyAmt = Math.round(apasGenericObj.convertToFloat(penaltyAmtCalculated));
	}else {
		penaltyAmt = Math.round(apasGenericObj.convertToFloat(penaltyAmtUserAdjust));
	}
    netExemptionAmountCalculated = exemptionAmountCalculated - penaltyAmt;
	return netExemptionAmountCalculated;
}


/**
 * @description: This method will return the Low Income Exemption Amount Calculated
 * @return : returns the Basic Exemption Amount Calculated
 * @throws Exception 
 */
public float calculateLowIncomeExemptionAmount() throws Exception {
	double taxYearProatedPercentage = verifyTaxYearProatedPercentage();
	float lowIncomeExemptionAmt = apasGenericObj.convertToFloat(lowIncomeReferenceAmountLabel.getText());
	DecimalFormat d = new DecimalFormat("0.00");
	Float exemptionAmountCalculated = Float.parseFloat(d.format((lowIncomeExemptionAmt*taxYearProatedPercentage)/100));
	return exemptionAmountCalculated;
}
/**
 * Description: This method will click on save and add a new Real Property settings
 * @return : returns the text message of success alert
 */
public String successAlretText() throws Exception {
	locateElement("//div[@role='alert'][@data-key='success']",2);
	locateElement("//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']",2);
	return getElementText(successAlertText);
}

/**
 * Description: This method will select the VA for a particular Roll Year
 * @param : rollYear for which VA will be opened
 * @throws Exception 
 */
public String selectVAByStartDate(String startDate) throws Exception
{	
	String xpath = "//div[contains(@class,'windowViewMode-normal')]//span[contains(text(),'"+startDate+"')]//..//..//preceding-sibling::th//a";
	waitUntilElementIsPresent(xpath,40);
	WebElement valueAdjustmentLink = driver.findElement(By.xpath(xpath));
	String vALinkName = valueAdjustmentLink.getText();
	ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on Value Adjustment Link: "+ vALinkName);
	Click(valueAdjustmentLink);	
	Thread.sleep(3000);	
	return vALinkName;
}


/**
 * Description: This method will navigate to VA List View in Exemption from VA Related List
 * @throws Exception 
 */
public void navigateToVAListViewInExemption() throws Exception
{	
	//Step1: Selecting the Value Adjustment Related List Tab
	Thread.sleep(2000);
	ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on Related List - Value Adjustement Tab");
	objPage.locateElement("//div//li[@title='Value Adjustments']//a", 30);
	waitForElementToBeClickable(valueAdjustmentRelatedListTab);
	objPage.Click(valueAdjustmentRelatedListTab);
	Thread.sleep(2000);

	//Step2: Clicking on 'View All' Link of Value Adjustment Related List Tab
	ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on View All Link");
	objPage.locateElement("//div//span[text()='View All']", 20);
	waitForElementToBeClickable(viewAllLink);
	objPage.javascriptClick(viewAllLink);
	Thread.sleep(3000);
	}

public String fetchVA() throws Exception {
    SalesforceAPI objSalesforceAPI = new SalesforceAPI();
    String queryForID = "SELECT Name FROM Value_Adjustments__c where Net_Exemption_Amount__C != 0.0";
    HashMap<String, ArrayList<String>> response  = objSalesforceAPI.select(queryForID);
    String vaName = response.get("Name").get(0);
    ReportLogger.INFO("VAs fetched through Salesforce API : " + vaName);
    return vaName;
}
/**
 * Description: This method will click on VAR for year passed if it exists else it will return false
 * @throws Exception 
 */
public boolean clickVA(String rollYear) throws Exception
{	
	boolean fVACreated = false;
	String xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//tr[1]//span[contains(text(),'Start Date')]//..//..//parent::th//parent::tr//..//..//tbody//span[text()='7/1/"+rollYear+"']//..//..//preceding-sibling::th//a";
	fVACreated = objPage.waitForElementToBeVisible(50, xpath);
	if(fVACreated) {
		WebElement vAforCurrentRY = locateElement(xpath, 50);
		String vaLinkName = objPage.getElementText(vAforCurrentRY);
		ReportLogger.INFO("Clicking on Value Adjustment Link: "+ vaLinkName);
		objPage.Click(vAforCurrentRY);
	}
	return fVACreated;
	}

}