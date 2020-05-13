package com.apas.PageObjects;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.apas.Assertions.SoftAssertion;
import com.apas.Reports.ExtentTestManager;
import com.apas.generic.ApasGenericFunctions;
import com.relevantcodes.extentreports.LogStatus;

public class ValueAdjustmentsPage extends ApasGenericPage {
	
	Logger logger;
	Page objPage;
	SoftAssertion softAssert1;
	ApasGenericFunctions apasGenericObj;
	
	public ValueAdjustmentsPage(RemoteWebDriver driver) {
		super(driver);
		
		PageFactory.initElements(driver, this);
		logger = Logger.getLogger(LoginPage.class);
		objPage=new Page(driver);
		softAssert1=new SoftAssertion();
		apasGenericObj= new ApasGenericFunctions(driver);
		
		
		
	}
	@FindBy(xpath="//lst-list-view-manager-header//following::div//table//tbody//tr//td[1]//following-sibling::th//a")
	public List<WebElement> VAlist;
	
	@FindBy(xpath="//lst-list-view-manager-header//following::div//table//tbody//tr//td[4]//span/span")
	public List<WebElement> vaStatusFromList;	
	

	@FindBy(xpath="//div[contains(@id,'error-message')]")
	public WebElement editFieldErrorMsg;
		
	
	
	
	//VAR elements
	
	@FindBy(xpath = "//a[contains(.,'Value Adjustments')]")
	public WebElement valueAdjustmentTab;
	
	@FindBy(xpath = "//div[starts-with(@class,'windowViewMode-normal oneContent active lafPageHost')]//button[@name='Edit']")
	public WebElement vaEdit;
	
	@FindBy(xpath = "//div[starts-with(@class,'windowViewMode-normal oneContent active lafPageHost')]//span[text()='End Date']//parent::div//following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement vaEndDateDetails;
	
	@FindBy(xpath = "//span[text()='Status']//parent::div//following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement vaStatusDetails;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[text()='Determination']//parent::div//following-sibling::div//slot[@slot='outputField']/lightning-formatted-text")
	public WebElement vaDetermination;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[text()='Roll Year Due Date']//parent::div//following-sibling::lightning-helptext/following-sibling::div//span")
	public WebElement penaltyDate1;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[text()='Roll Year Due Date 2']//parent::div//following-sibling::lightning-helptext/following-sibling::div//span")
	public WebElement penaltyDate2;
	
	
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[text()='Roll Year Settings']//parent::div/following-sibling::lightning-helptext/following-sibling::div//slot//a")
	public WebElement vaRollYear;
	
	@FindBy(xpath = "//div[@class='windowViewMode-normal oneContent active lafPageHost']//span[contains(.,'Penalty Percentage')]/parent::div/following-sibling::div/span//slot[@slot='outputField']//slot/lightning-formatted-number")
	public WebElement vaPenaltyPercentage;
	
	@FindBy(xpath = "//span[contains(.,'Net Exemption Amount')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaNetExemptionAmount;
	
	@FindBy(xpath = "//span[contains(.,'Exemption Amount Calculated')]/parent::div/following-sibling::div/span")
	public WebElement vaExemptionAmountCalculated;
	
	
	@FindBy(xpath = "//span[contains(.,'Penalty Amount Calculated')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaPenaltyAmountCalculated;
	
	@FindBy(xpath = "//span[contains(.,'Roll Year Basic Reference Amount')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaRollYearBasicRefAmount;
	
	@FindBy(xpath = "//span[contains(.,'Roll Year Low Income Reference Amount')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaRollYearLowIncomeRefAmount;
	
	
	@FindBy(xpath = "//span[contains(.,'Tax Year Prorated Percentage')]/parent::div/following-sibling::div/span//lightning-formatted-number")
	public WebElement vataxYearProratedPercentage;
	
	@FindBy(xpath = "//span[contains(.,'Determination')]/parent::div/following-sibling::div/span//lightning-formatted-text")
	public WebElement vaDeterminationType;
	
	
	
	@FindBy(xpath = "//lst-list-view-manager-header//following::div//table//tbody//tr//td[1]//following-sibling::td[1]")
	public List<WebElement> vaStartDateFromList;
	
	@FindBy(xpath = "//lst-list-view-manager-header//following::div//table//tbody//tr//td[1]//following-sibling::td[2]//span/span")
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
	
	@FindBy(xpath = "//span[contains(.,'Determination Denial Detail')]//following::div[1]//following::input[1]")
	public WebElement determinationDenialDetail;
	
	@FindBy(xpath = "//label[contains(.,'Annual Form Received Date')]/following::input[1]")
	public WebElement annualFormReceivedDate;
	
	@FindBy(xpath = "//label[contains(.,'Start Date')]/following::input[1]")
	public WebElement startDate;
	
	@FindBy(xpath = "//label[contains(.,'End Date')]/following::input[1]")
	public WebElement endDate;
	
	@FindBy(xpath = "//label[contains(.,'Total Annual Household Income')]/following::input[1]")
	public WebElement totalAnnualHouseholdIncome;
	
	@FindBy(xpath = "//label[contains(.,'Penalty Amount - User Adjusted')]/following::input[1]")
	public WebElement penaltyAmountUserAdjusted;
	
	@FindBy(xpath = "//span[text() = 'Penalty Adjustment Reason']/parent::span/following-sibling::div//a[@class = 'select']")
	public WebElement penaltyAdjustmentReason;
	
	@FindBy(xpath = "//label[contains(.,'Penalty Adjustment Other Reason Detail')]/following::textarea[1]")
	public WebElement penaltyAdjustmentOtherReasonDetail;
	
	
	@FindBy(xpath = "//input[contains(@placeholder,'Search this list...')]")
	public WebElement searchList;

//--------- Deepika's Locators ----------------
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//table//tbody//tr")
    public List<WebElement> numberOfValueAdjustments;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Start Date']//..//following-sibling::div//slot//slot//*")
    public WebElement startDateValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='End Date']//..//following-sibling::div//slot//slot//*")
    public WebElement endDateValueLabel;

	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Roll Year Basic Reference Amount']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement basicReferenceAmountLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Roll Year Low Income Reference Amount']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement lowIncomeReferenceAmountLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Roll Year Settings']//parent::div//following-sibling::div//a")
    public WebElement rollYearSettingsLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//button[text() = 'Edit']")
    public WebElement editButton;
	
	@FindBy(xpath = "//label//span[text()='Total Annual Household Income']/../../input")
	public WebElement totalAnnualHouseholdIncomeEditBox;
	
	@FindBy(xpath = "//label//span[text()='Annual Form Received Date']/../following-sibling::div/input")
	public WebElement annualFormReceivedDateEditBox;
	
	@FindBy(xpath = "//div[contains(@class,'modal-footer')]//button//span[text() = 'Save']")
    public WebElement saveButton;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Roll Year Low Income Threshold Amount']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement rollYearLowIncomeThreshholdAmountLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Roll Year Low Income Due Date']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement rollYearLowIncomeDueDateLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Determination']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement determinationValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Number of days']//..//following-sibling::div//lightning-formatted-number")
    public WebElement noOfDaysValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Exemption Amount Calculated']//..//following-sibling::div//lightning-formatted-text")
    public WebElement exemptionAmountCalculatedValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Tax Start Date']//..//following-sibling::div//slot//slot//*")
    public WebElement taxStartDateValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Tax End Date']//..//following-sibling::div//slot//slot//*")
    public WebElement taxEndDateValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Penalty Amount Calculated']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement penaltyAmtCalcValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Penalty Amount - User Adjusted']//parent::div//following-sibling::div//lightning-formatted-text")
    public WebElement penaltyAmtUserAdjustValueLabel;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal')]//span[text()='Net Exemption Amount']//..//following-sibling::div//lightning-formatted-text")
    public WebElement netExemptionAmountCalculatedValueLabel;
	
	@FindBy(xpath = "//div[@role='alert'][@data-key='success']//span[@data-aura-class='forceActionsText']")
	public WebElement successAlertText;
	
	@FindBy(xpath = "//li[@title='Value Adjustments']//a")			
    public WebElement valueAdjustmentRelatedListTab;
	
	@FindBy(xpath = "//span[text()='View All']")
    public WebElement viewAllLink;
//--------- Deepika's Locators ----------------
	
	public String getPastDateFromADate(String date,int days) throws ParseException
	{
	
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		Date appdate1=sdf.parse(date);
		
		Date appdate2=DateUtils.addDays(appdate1, days);
		String pastDate = sdf.format(appdate2);  
		//System.out.println(tommorowDate);
		
		return pastDate;
	}
	

public int getVACountBasedOnDates(String startDate,String endDate)
{
	int totalVA=0;

	try{
	
	String endYear=endDate;
	String[] startDateYear=startDate.split("/");
	int startYear=Integer.parseInt(startDateYear[2]);
	int startMonth=Integer.parseInt(startDateYear[0]);
	/*if(endDate.contains("/"))
	{
		String[] endDateYear=endDate.split("/");
		endYear=endDateYear[2];
	}
	*/
	if(startMonth<=6)
	{
		startYear=startYear-1;	
	}
	
	int endYearFinal=Integer.parseInt(endYear);
	
	 totalVA = (endYearFinal-startYear)+1;
	
	}
	catch(Exception e)
	{
		System.out.println("Error while counting actual VA's to be cretaed"+e.getMessage());
	}
	
	return totalVA;
}


public int verifyValueAdjustments(String maxDate, String endDate2,String currentRollYear) throws IOException, InterruptedException {
	int actualVAtoBeCreated=0;
	try{
	objPage.Click(valueAdjustmentTab);
	//Thread.sleep(3000);
	apasGenericObj.locateElement("//div[@class='windowViewMode-normal oneContent active lafPageHost']//a[contains(@class,'header-title-container')]//span[text()='Value Adjustments']", 3);
	
	if(endDate2!=null && endDate2.contains("/"))
		{actualVAtoBeCreated=getVACountBasedOnDates(maxDate, endDate2);}
	else
	{ actualVAtoBeCreated=getVACountBasedOnDates(maxDate, currentRollYear);}

	}
	
	
	catch(Exception e)
	{
		System.out.println("error while verfyiing value adjustments count:"+e.getMessage());
		System.out.println("stack trace while verfyiing value adjustments count:"+e.getStackTrace());
	}
	//System.out.println(actualVAtoBeCreated);
	return actualVAtoBeCreated;
	
}


public int vaCount() throws InterruptedException
{
	Thread.sleep(3000);
	List<WebElement> vaLists=VAlist;
	
	return vaLists.size();

}


public void verifyPenltyPercantgeAndVariousAmountsForVA(String currentDate,String applicationDate,String graceEndDate,String endDateOFratingOnExemption) throws Exception {
	
	
	System.out.println("calculating penlaty%");
	Thread.sleep(3000);
	List<WebElement> vaLists=VAlist;
	//Iterator<WebElement> allVA=vaLists.iterator();
	System.out.println("Total VA's present ::"+vaLists.size());
	String currentRollYearFinal = null;
	String[] appSplit=applicationDate.split("/");
	String dateBeforeAppdate=getPastDateFromADate(applicationDate, -1);
	System.out.println("Annual Form received date to be entered for VA::"+dateBeforeAppdate);
	try{
	if(endDateOFratingOnExemption!=null && endDateOFratingOnExemption.contains("/"))
	{
		currentRollYearFinal=ExemptionsPage.determineRollYear(endDateOFratingOnExemption);
	}
	else 
	{
		currentRollYearFinal=ExemptionsPage.determineRollYear(currentDate);//2020
	}
	String appRollyear=ExemptionsPage.determineRollYear(applicationDate);//2020
	int appdateRollYear=Integer.parseInt(appRollyear);
	JavascriptExecutor js=(JavascriptExecutor) driver;
	int currentRollYearis=Integer.parseInt(currentRollYearFinal);
	System.out.println("VA's should be created till roll year "+currentRollYearis+" as end date of rating is ::"+endDateOFratingOnExemption);
	for(WebElement e: vaLists)
	{
		System.out.println("clicking on VA::"+e.getText());
		js.executeScript("arguments[0].click();", e);
		//objPage.Click(vaLists.get(i));
		
		apasGenericObj.locateElement("//span[text()='End Date']//parent::div", 3);
		//exmPagePobj.editAndInputFieldData("Annual Form Received Date",annualFormReceivedDate,dateBeforeAppdate);
		//softAssert1.assertEquals(editFieldErrorMsg.getText(), "Annual Form Received Date should not be greater than today or less than Exemption's Date Application Received", "SMAB-T1291:Verified that Annual Fomr Received date can't be less than Application Received date");
		
//		softAssert1.assertEquals(vaDetermination.getText(), "Basic Disabled Veterans Exemption", "SMAB-T516:Verify that all the 'Value Adjustment Records' created for an Exemption Records are of type 'Basic Disabled Veteran Exemption '");
		
		String vaRollYearString=vaRollYear.getText().trim();
		
		int vaRollYear=Integer.parseInt(vaRollYearString);
		System.out.println("VA roll year is:"+vaRollYear);
		
		String penaltyPercentageString=vaPenaltyPercentage.getText().trim();
		int indexof=penaltyPercentageString.indexOf(".");
    	
		String penaltyPercentage1=penaltyPercentageString.substring(0, indexof);
    	
		
		int penaltyPercentage=Integer.parseInt(penaltyPercentage1);
		System.out.println("penalty Percentage::"+penaltyPercentage);
		//2018 - 2013
		if(appdateRollYear>vaRollYear)
			{
			System.out.println("Since its a closed roll year VA hence penalty percenatage for VA should be 15");
			softAssert1.assertEquals(penaltyPercentage, 15, "SMAB-T1290:Since it's a closed roll year VA hence penalty percenatage for VA should be 15");
			}
		else if(appdateRollYear<vaRollYear)
		{
			System.out.println("Since it's a future roll year VA hence penalty percenatage for VA should be 0");
			softAssert1.assertEquals(penaltyPercentage, 0, "SMAB-T1290:penalty percenatage for VA should be 0");
		}
		else
		{			//4/28/2015   			3/27/2015
			//if(applicationDate.compareTo(graceEndDate)>0 && Integer.parseInt(appSplit[0])>=2 && Integer.parseInt(appSplit[0])<12 && appSplit[1].compareTo("15")>0 )
			System.out.println("verifying penlaty for same Roll year VA as Application Roll year");
			if(applicationDate.compareTo(graceEndDate)>0){
											//2/15/2018
				if(applicationDate.compareTo(penaltyDate1.getText().trim())>0 && applicationDate.compareTo(penaltyDate2.getText().trim())<0)
				{
					System.out.println("SMAB-T1290:Application submitted date is "+applicationDate+" is greater than penlaty due date1 2/15/"+appSplit[2]+" hence 10% penlty" );
					softAssert1.assertEquals(penaltyPercentage, 10, "SMAB-T1290:Application submitted date is "+applicationDate+" is greater than penlaty due date1:"+penaltyDate1+"and less than peanlty date 2:"+penaltyDate2+" hence 10% penalty" );	
				}
				else if(applicationDate.compareTo(penaltyDate2.getText().trim())>0)
				{
					System.out.println("SMAB-T1290:Application submitted date is "+applicationDate+" is greater than penlaty due date2:"+penaltyDate2+" hence 15% penlty" );
					softAssert1.assertEquals(penaltyPercentage, 10, "SMAB-T1290:Application submitted date is "+applicationDate+" is greater than penlaty due date2:"+penaltyDate2+" hence 15 % penlaty" );	
					
					
				}
				
			}
			else
			{
				System.out.println("SMAB-T1290:Application submitted date is "+applicationDate+" is less than grace end date"+graceEndDate+" hence 0% penlty" );
				softAssert1.assertEquals(penaltyPercentage, 0, "SMAB-T1290:Application submitted date is "+applicationDate+" is less then grace end date "+graceEndDate+" hence 0% penalty" );
			}
			
		}
		
		/**
		 * logic for Net Exemption Amount, penalty amount calculated and Exemption amount calculated
		 */
		// calculateExemptionAmountPenaltyAmountNetExemptionVA(allVA.next());
		
	driver.navigate().back();
	}
	}
	catch(Exception e)
	{
		System.out.println("Error message while calculating penalty percentgae::"+e.getMessage());
		System.out.println("Error stack trace while validating penalty percentgae::"+e.getStackTrace());
	}
	
}



private void calculateExemptionAmountPenaltyAmountNetExemptionVA(WebElement va) {
System.out.println("execeuting amoutns verifications for"+va.getText());
	try{
	double exemptionAmountcalculated=converToDouble(vaExemptionAmountCalculated.getText());
	double penaltyAmountCalculated=converToDouble(vaPenaltyAmountCalculated.getText());
	double netExemptionAmount=converToDouble(vaNetExemptionAmount.getText());
	double rollYearBasicRefAmt=converToDouble(vaRollYearBasicRefAmount.getText());
	double rollYearLowIncomeRefAmt=converToDouble(vaRollYearLowIncomeRefAmount.getText());
	
	
	
	String[] taxyearProratedString=vataxYearProratedPercentage.getText().split("%");
	double taxyearProrated=Double.parseDouble(taxyearProratedString[0]);
	String[] penaltyPercentageString=vaPenaltyPercentage.getText().split(".");
	double penalty=Double.parseDouble(penaltyPercentageString[0]);
	
	double exemptionAmtCalculatedAsPerForm;	
	double penaltyAmtCalAsPerForm;
	double netExemptionAmtAsPerForm;
	//DecimalFormat df = new DecimalFormat("###.###");
	
	
	if(vaDeterminationType.equals("Basic Disabled Veterans Exemption"))
	{
		
	exemptionAmtCalculatedAsPerForm=Precision.round( rollYearBasicRefAmt * (taxyearProrated/100) , 2);	
	penaltyAmtCalAsPerForm= Precision.round( exemptionAmtCalculatedAsPerForm * (penalty/100) , 2);
	netExemptionAmtAsPerForm=Precision.round( exemptionAmtCalculatedAsPerForm-penaltyAmtCalAsPerForm , 2);
	
	}
	else
	{
	exemptionAmtCalculatedAsPerForm=Precision.round((rollYearBasicRefAmt + (rollYearLowIncomeRefAmt *((100- penalty)/100))) * (taxyearProrated/100) , 2);
	
	penaltyAmtCalAsPerForm=Precision.round((rollYearBasicRefAmt  *((100- penalty)/100)) * (taxyearProrated/100) , 2);
	//netExemptionAmtAsPerForm=Precision.round(rollYearBasicRefAmt + (rollYearLowIncomeRefAmt - rollYearBasicRefAmt)*((100-penalty)/100) , 2);
	netExemptionAmtAsPerForm=Precision.round( exemptionAmtCalculatedAsPerForm-penaltyAmtCalAsPerForm , 2);
	}
	
	System.out.println("Exemption Amount calculated::"+exemptionAmountcalculated+" penalty Amount Calculated::"+penaltyAmountCalculated+" net Exemption Amount:;"+netExemptionAmount);
	softAssert1.assertEquals(exemptionAmountcalculated, exemptionAmtCalculatedAsPerForm, "verifying exemption amount calculated for VA "+va.getText());
	softAssert1.assertEquals(penaltyAmountCalculated, penaltyAmtCalAsPerForm, "verifying penalty amount calculated for VA "+va.getText());
	softAssert1.assertEquals(netExemptionAmount, netExemptionAmtAsPerForm, "SMAB-T1292:verifying Net exemption amount calculated for VA "+va.getText());
	}
	catch(Exception e)
	{
		System.out.println("Error while calcaulting penalty amounts::"+e.getMessage());
	System.out.println("Error stack trace::"+e.getStackTrace());}
	
}




public double converToDouble(Object amount)
{
	
	String amtWithDollar=(String)amount;
	String amtAsString=amtWithDollar.substring(1, amtWithDollar.length());
	String finalAmtAsString=amtAsString.replace(",","");
	double convertedAmt=Double.parseDouble(finalAmtAsString);
	System.out.println("Final Amt calcualted is:"+convertedAmt);
	
	
	return convertedAmt;
	
}



public int getActiveVA() {
	
	List<WebElement> vaLists=vaStatusFromList;
	int activeVA = 0;
	for(WebElement e: vaLists)
	{
		if(e.getText().equals("Active"));
		{
		activeVA++;
		}
	}
	
	return activeVA;
}


public String findVABasedOnEndDate(String endDateOfRating) throws Exception {
	
	
	List<WebElement> vaEndDates=vaEndDateFromList;
	
	int i=0;
	String newVA=null;
	for(Iterator<WebElement> end= vaEndDates.iterator(); end.hasNext();i++)
	{
		if(end.next().getText().equals(endDateOfRating))
		{
			newVA=driver.findElement(By.xpath("//lst-list-view-manager-header//following::div//table//tbody//tr["+(i)+"]//td[1]//following-sibling::th//a")).getText();
			String startDate=driver.findElement(By.xpath("//lst-list-view-manager-header//following::div//table//tbody//tr["+(i)+"]//td[2]//span/span")).getText();
			String endDate=driver.findElement(By.xpath("//lst-list-view-manager-header//following::div//table//tbody//tr["+(i)+"]//td[3]//span/span")).getText();
			String status=driver.findElement(By.xpath("//lst-list-view-manager-header//following::div//table//tbody//tr["+(i)+"]//td[4]//span/span")).getText();
			System.out.println("New Va created with Start date::"+startDate+" and end date::"+end.next().getText());
			
			
			softAssert1.assertEquals(vaEndDateDetails.getText(), endDateOfRating, "SMAB-T601:-VA with Start date::"+startDate+" and end date: "+endDate+" is created with status:"+status);
			
			break;
		}
		
	}
	

	return newVA;
}


public int verifyVAType(int listSize) {
	int basicVA=0;
	try{
		for(int i=1;i<=listSize;i++)
		{
			String vaStstus=driver.findElement(By.xpath("//lst-list-view-manager-header//following::div//table//tbody//tr["+(i)+"]//td[6]//span/span")).getText();
			if(vaStstus.equals("Basic Disabled Veterans Exemption"))
			{basicVA++;}
		}
		
	}
	catch(Exception e)
	{
		System.out.println("error while verifying BAsic VA's"+e.getMessage());
	}
	
	return basicVA;
}

/*
public void editValueAdjustment(Map<String, String> newExemptionData) {
	try{
		
		WebElement firstVA=driver.findElement(By.xpath("//lst-list-view-manager-header//following::div//table//tbody//tr[1]//td[1]//following-sibling::th//a"));
		JavascriptExecutor executor = (JavascriptExecutor)driver;
		executor.executeScript("arguments[0].click();", firstVA);
		
		objPage.Click(exmPagePobj.editExemption);
		apasGenericObj.locateElement("//h2[contains(.,'Edit Value Adjustment')]", 4000);
		
		
		
		
		
	}
	catch(Exception e)
	{}
}
*/

//---------------------------- Deepika's Methods ---------------------------------
/**
 * @throws Exception 
 * @description: This method will return the no. of VAs on the page
 * @return : returns the no. of VAs
 */
public int getnumberOfValueAdjustments() throws Exception {
	return numberOfValueAdjustments.size();
}

/**
 * @description: This method will return difference of no of days between 2 dates
 * @param fieldName: element from which start date is fetched
 * @param fieldName: element from which end date is fetched
 * @return : returns the difference of no of days between 2 dates
 */
public float verifyNoOfDays(WebElement eleStartDate, WebElement eleEndDate) {
	System.out.println("inside verifynoofdays");
	String startDate = getElementText(eleStartDate);
	String endDate = getElementText(eleEndDate);	
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
    Date firstDate = null;
    Date secondDate= null;
	try {
		firstDate = sdf.parse(startDate);
		secondDate = sdf.parse(endDate);
		System.out.println("inside try");
	} catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}  
    long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
    float diff = (TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS))+1;
    
    System.out.println("diff: " + diff);
    
    return diff;	
}


/**
 * @description: This method will return the Tax Year Prorated Percentage Calculated
 * @return : returns the Tax Year Prorated Percentage Calculated
 * @throws Exception 
 */
public float verifyTaxYearProatedPercentage() throws Exception {
	float numberOfDays = verifyNoOfDays(startDateValueLabel,endDateValueLabel);	
	locateElement("//div[contains(@class,'windowViewMode-normal')]//span[text()='Roll Year Settings']//parent::div//following-sibling::div//a", 2);
	Click(rollYearSettingsLabel);
	float totalNoOfDays = verifyNoOfDays(taxStartDateValueLabel,taxEndDateValueLabel);
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
	float basicExemptionAmt = convertToFloat(basicReferenceAmountLabel.getText());
	DecimalFormat d = new DecimalFormat("0.00");
					
	float exemptionAmountCalculated = Float.parseFloat(d.format((basicExemptionAmt*taxYearProatedPercentage)/100));
	System.out.println("final exemption amt is:"+exemptionAmountCalculated);
	return exemptionAmountCalculated;
}

/**
 * @description: This method will return the Basic Exemption Amount Calculated
 * @return : returns the Basic Exemption Amount Calculated
 * @throws IOException 
 */
public float calculateNetExemptionAmount(float exemptionAmountCalculated) throws Exception {
	float netExemptionAmountCalculated=0;
	float penaltyAmtCal=0;
	float penaltyAmtUserAdjustCal=0;
	
	String penaltyAmt = penaltyAmtCalcValueLabel.getText();		
	if(penaltyAmt!=null) {
		String penaltyAmtC = (penaltyAmt.substring(1, penaltyAmt.length())).replaceAll(",", "");
		penaltyAmtCal = Float.parseFloat(penaltyAmtC);
	}
	else {
		penaltyAmtCal = (float) 0.00;
	}
		
	String penaltyAmtUserAdjust = penaltyAmtUserAdjustValueLabel.getText();		
	if(penaltyAmtUserAdjust!=null) {
		String penaltyAmtUserAdjustC = (penaltyAmt.substring(1, penaltyAmt.length())).replaceAll(",", "");
		penaltyAmtUserAdjustCal = Float.parseFloat(penaltyAmtUserAdjustC);
		}
		else {
			penaltyAmtUserAdjustCal = (float) 0.00;
		}
	
	if(penaltyAmtUserAdjust==null) {
		netExemptionAmountCalculated = exemptionAmountCalculated - penaltyAmtCal;
	}
	else {
		netExemptionAmountCalculated = exemptionAmountCalculated - penaltyAmtUserAdjustCal;
	}
	
	System.out.println("final net exemption amt is:"+netExemptionAmountCalculated);
	return netExemptionAmountCalculated;
}

/**
 * @description: This method will return the Low Income Exemption Amount Calculated
 * @return : returns the Basic Exemption Amount Calculated
 * @throws Exception 
 */
public float calculateLowIncomeExemptionAmount() throws Exception {
	double taxYearProatedPercentage = verifyTaxYearProatedPercentage();
	float lowIncomeExemptionAmt = convertToFloat(lowIncomeReferenceAmountLabel.getText());
	System.out.println("lowIncome Exemption Amt: "+ lowIncomeExemptionAmt);
	DecimalFormat d = new DecimalFormat("0.00");
	Float exemptionAmountCalculated = Float.parseFloat(d.format((lowIncomeExemptionAmt*taxYearProatedPercentage)/100));
	System.out.println("final exemption amt is:"+exemptionAmountCalculated);
	return exemptionAmountCalculated;
}

/**
 * @description: This method will verify true or false based on year passed as an argument is leap or not
 * @param Element: Roll Year for which leap year needs to be calculated
 * @return : returns the true or false based on year passed as an argument is leap or not
 */
public boolean verifyLeapYear(WebElement elem) {
	String yearToVerify = elem.getText().trim();		
	int year = Integer.parseInt(yearToVerify);		
    boolean leap = false;	
    if(year % 4 == 0)
    {
        if( year % 100 == 0)
        {
            // year is divisible by 400, hence the year is a leap year
            if ( year % 400 == 0)
                leap = true;
            else
                leap = false;
        }
        else
            leap = true;
    }
    else
        leap = false;
    
    return leap;

}


/**
 * @description: This method will modify the 'Determination' of VA from Basic to Low-Income
 * @throws Exception
 * returns: boolean value based if 'Determination' of VA is modified from Basic to Low-Income
 */
public String modifyVADeterminationToLowIncome() throws Exception {
	Thread.sleep(1000);
	boolean flag = false;
	String lowIncomeThreshholdAmount = rollYearLowIncomeThreshholdAmountLabel.getText();
	
	System.out.println("lowIncomeThreshholdAmount: "+lowIncomeThreshholdAmount);
	
	Thread.sleep(1000);
	String totalAnnualHouseHoldIncome = (lowIncomeThreshholdAmount.substring(1, lowIncomeThreshholdAmount.length())).replaceAll(",", "");		
	
	System.out.println("totalAnnualHouseHoldIncome: "+totalAnnualHouseHoldIncome);	
	Thread.sleep(1000);
	
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);		
	Date date = DateUtils.addDays(new Date(), -1);
	String currentDate = sdf.format(date);
	Thread.sleep(1000);
	
	Click(editButton);	
	
	enter(annualFormReceivedDateEditBox,currentDate);
	
	totalAnnualHouseholdIncomeEditBox.clear();
	
	enter(totalAnnualHouseholdIncomeEditBox,totalAnnualHouseHoldIncome);
	ExtentTestManager.getTest().log(LogStatus.INFO, "Modifying Determination of VA from 'Basic' to 'Low-Income'");
	
	Click(saveButton);	
	
	String actualSuccessAlertText = successAlretText();
	return actualSuccessAlertText;
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
 * Description: This method will click on Active Value Adjustment
 * @param : it takes row no. as an argument
 * @throws Exception 
 */
public void clickActiveVA(int rowNum) throws Exception {
int i = rowNum;
locateElement("//h1[@title='Value Adjustments']//ancestor::div[@role='banner']//following-sibling::div[contains(@class,'listDisplays')]//table//tbody//tr["+i+"]//td[4]//span//span",3); 		  				  
WebElement status = driver.findElement(By.xpath("//h1[@title='Value Adjustments']//ancestor::div[@role='banner']//following-sibling::div[contains(@class,'listDisplays')]//table//tbody//tr["+ i + "]//td[4]//span//span"));  
 
//Step10: Verifying if Status of Value Adjustment is 'Active' 
String actualStatus = status.getText().trim(); 
String expectedStatus = "Active"; 
if(actualStatus.equals(expectedStatus.trim())) {		  
	//Step11: Clicking on 'Active' Value Adjustment link 
	locateElement("//h1[@title='Value Adjustments']//ancestor::div[@role='banner']//following-sibling::div[contains(@class,'listDisplays')]//table//tbody//tr["+i+"]//td[4]//span//span//..//..//preceding-sibling::th//span//a",3);
	WebElement valueAdjustmentLink = driver.findElement(By.xpath("//h1[@title='Value Adjustments']//ancestor::div[@role='banner']//following-sibling::div[contains(@class,'listDisplays')]//table//tbody//tr["+i+"]//td[4]//span//span//..//..//preceding-sibling::th//span//a"));
	System.out.println("VA Link clicked is: "+ valueAdjustmentLink.getText());
	ExtentTestManager.getTest().log(LogStatus.INFO, "Clicking on Value Adjustment Link: "+ valueAdjustmentLink.getText());
	Click(valueAdjustmentLink);
	  }
}

/**
 * Description: This method will convert amount of type String to Float
 * @param : Amount Object
 */
public float convertToFloat(Object amount)
{	
	String amt=(String)amount;
	String finalAmtAsString=(amt.substring(1, amt.length())).replaceAll(",", "");
	float convertedAmt=Float.parseFloat(finalAmtAsString);	
	return convertedAmt;		
}
//---------------------------- Deepika's Methods ---------------------------------

}