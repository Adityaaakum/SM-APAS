package com.apas.PageObjects;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;



import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.DateUtil;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.modules;

public class AppraisalActivityPage extends ApasGenericPage implements modules {
	Util objUtil;
	SalesforceAPI salesforceApi;
	MappingPage objMappingPage;
	WorkItemHomePage objWorkItemHomePage;

	public AppraisalActivityPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
		salesforceApi = new SalesforceAPI();
		objMappingPage = new MappingPage(driver);

	}
	
	
	public String returnButton ="Return";
	public String appraisalActivityStatus ="Appraiser Activity Status";
	public String rejectButton ="Reject";
	public String rejectionReasonForIncorrectCioDetermination ="CIO - Incorrect Transfer Work & Determination";
	public String nextButton ="Next";
	public String eventCodeLabel ="Event Code";
	public String dorLabel ="DOR";
	public String dovLabel ="DOV";
	public String apnLabel ="APN";
	public String ownerName ="Owner Name";
	public String partofEconomicUnit ="Part of Economic Unit";
	public String rollEntryStatus = "Status";
	public String rollEntryNoticeDate = "Notice Date";
	public String landCashValueLabel ="Land Cash Value";
	public String improvementCashValueLabel ="Improvement Cash Value";
	public String statusLabel="Status";
	public String startDateLabel="Start Date";
	public final String commonXpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]";
	public String recordTypeLable ="Record Type";
	public String doeLabel ="DOE";


	
	@FindBy(xpath = "//select[@name='Rejection_Reason_PickList']")
	public WebElement rejectionReasonList;
	
	@FindBy(xpath ="//*[@class='slds-modal__footer']//*[text()='Save']")
	public WebElement calculatePenaltySaveButton;
	
	@FindBy(xpath ="//span[@title='Assessed Value']")
	public WebElement assessedValueTableView;
	
	@FindBy(xpath ="//span[@title='Roll Entry for Parent Parcel']")
	public WebElement rollEntryTableView;
	
	@FindBy(xpath ="//a[@title='Parcels']")
	public WebElement parcelsLink;
	
	@FindBy(xpath = commonXpath + "//*[@class='slds-truncate' and text()='Approve'] | //button[text()='Approve']")
	public WebElement quickActionOptionApprove;
	
	@FindBy(xpath = commonXpath + "//*[@class='slds-truncate' and text()='Return']| //button[text()='Return']")
	public WebElement quickActionOptionReturn;

	@FindBy(xpath = "//a[contains(@class,'slds-button slds-button--icon-x-small slds-button--icon-border-filled')]")
	public WebElement clickShowMoreActionButton;

	/*
	 * This method is to find the xpath of the edit pencil icon.
	 */
	 public WebElement appraisalActivityEditValueButton(String feildName) {
		 
		 String xpath="//*[@class='base-record-form-header-container slds-card__body slds-card__body_inner']//*[@title='Edit "+ feildName+"']";
		 WebElement xPath = driver.findElement(By.xpath(xpath));
		 return xPath;
	 }
	 
		
		/*
		 * This method is only for the calculate penalty form. This button can be found
		 * on LEOP Appraisal Activity Screen. It's xpaths are different from another
		 * drop down forms.
		 */

		public void selectDropDownValueForCalculatePenalty(String element, String value) throws Exception {
			String commonFirstPath = "//*[@class='slds-grid slds-col slds-is-editing slds-has-flexi-truncate mdp forcePageBlockItem forcePageBlockItemEdit']";
			String firstXpath = commonFirstPath + "//*[text()='" + element + "']//following::a";
			
			WebElement webElement=driver.findElement(By.xpath(firstXpath));
			WebElement drpDwnOption;
			String commonPath = "//*[contains(@class,'select-options')]";
			String xpathDropDownOption = commonPath + "//*[@title='" + value + "']";

			scrollToElement(webElement);
			javascriptClick(webElement);
			waitUntilElementIsPresent(xpathDropDownOption, 10);
			drpDwnOption = driver.findElement(By.xpath(xpathDropDownOption));
			scrollToElement(drpDwnOption);
			waitForElementToBeClickable(drpDwnOption, 8);
			javascriptClick(drpDwnOption);
		}
		
		/*
		 * This method is to calculate supplemental roll entry record for a particular parcel based on the DOV in AAS ,AV and AVO records that exists on parcel
		 *  */

		public void calculateSupplementalRollEntryRecord(String dovAppraisalScreen, String parcelId) throws Exception {
			   
			  HashMap<Integer,Double> mapCPIFactors=new HashMap<Integer,Double>();  

			String dovQuery ="SELECT min(DOV__c) FROM Assessed_BY_Values__c where APN__c='"+parcelId+"' and status__c='Active'";
		  	String lowestDOVAVRecords = objSalesforceAPI.select(dovQuery).get("expr0").get(0);
		  	
		  	Date dovDAte=new SimpleDateFormat("yyyy-MM-dd").parse(lowestDOVAVRecords);  
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(dovDAte);
			Integer minDOVYear = calendar.get(Calendar.YEAR);
			
		  	
		  	 dovDAte=new SimpleDateFormat("MM/dd/yyyy").parse(dovAppraisalScreen);  
		  	 calendar = new GregorianCalendar();
			calendar.setTime(dovDAte);
			Integer maxDOVYear = calendar.get(Calendar.YEAR);
			
			Integer baseYear=(maxDOVYear+1);
			String baseYearNewAVrecord=baseYear.toString();
			
			Integer initialYear=minDOVYear;
			
		  	while(initialYear<=maxDOVYear)
		  		
		  	
		  	{   		
		  		//fetching CPI factors 

		    String queryForRollYearId = "SELECT Id FROM Roll_Year_Settings__c Where Name = '"+initialYear.toString()+"'";
		  	
			HashMap<String, ArrayList<String>> rollYearId = objSalesforceAPI.select(queryForRollYearId);
			Double CPIFactor = Double.parseDouble(objSalesforceAPI.select("SELECT CPI_Factor__c FROM CPI_Factor__c  Where Roll_Year__c ='"+rollYearId.get("Id").get(0)+"' ").get("CPI_Factor__c").get(0));
			mapCPIFactors.put(initialYear, CPIFactor);
			
			++initialYear;
			
		  	}
			
			System.out.print(mapCPIFactors);
			
			String AVRecordsQuery ="SELECT Name,Id,DOV__c ,Land_Value_Formula__c,Improvement_Value_Formula__c FROM Assessed_BY_Values__c where status__c='Active' and APN__c='"+parcelId+"' "
					+ "and Base_Year__c!='"+baseYearNewAVrecord+"' ";
			
			HashMap<String, ArrayList<String>> responseAVRecordDetails  = objSalesforceAPI.select(AVRecordsQuery);
			int factorForwardedLandValue=0;
			int factorForwardedImprovementValue=0;
			HashMap<String, Long> factorForwardedValue=new HashMap<String,Long>(); 
		  	
			for(int i=0;i< responseAVRecordDetails.size();i++)
			{
				
				String avRecordId=responseAVRecordDetails.get("Id").get(i);
				String avRecordDOV=responseAVRecordDetails.get("DOV__c").get(i);
				Double avRecordLaand=Double.parseDouble(responseAVRecordDetails.get("Land_Value_Formula__c").get(i));
				Double avRecordImprovement=Double.parseDouble(responseAVRecordDetails.get("Improvement_Value_Formula__c").get(i));

				String AVORecordsQuery ="SELECT Id,Name,Ownership__c FROM Assessed_Values_Ownership__c where status__c='Active' and Assessed_Values__c='"+avRecordId+"' ";
				
				HashMap<String, ArrayList<String>> responseAVORecordDetails  = objSalesforceAPI.select(AVORecordsQuery);
				for(int j=0;j< responseAVORecordDetails.size();j++)
				{
					//code for factor forward 
					
					Double avoRecordOwnershipPercentage=Double.parseDouble(responseAVORecordDetails.get("Ownership__c").get(j));
					HashMap<String, Long> factorForwardedValueAVOREcord=
					performFactorForwarding(avRecordDOV,maxDOVYear,maxDOVYear-1,mapCPIFactors,avRecordLaand,avRecordImprovement,avoRecordOwnershipPercentage );
					
					
					factorForwardedValue.put(maxDOVYear+"-Land",factorForwardedValue.get(maxDOVYear+"-Land")+ factorForwardedValueAVOREcord.get(maxDOVYear+"-Land"));
					factorForwardedValue.put(maxDOVYear+"-Improvement",factorForwardedValue.get(maxDOVYear+"-Improvement")+ factorForwardedValueAVOREcord.get(maxDOVYear+"-Improvement"));
					factorForwardedValue.put(maxDOVYear-1+"-Land",factorForwardedValue.get(maxDOVYear-1+"-Land")+ factorForwardedValueAVOREcord.get(maxDOVYear-1+"-Land"));
					factorForwardedValue.put(maxDOVYear-1+"-Improvement",factorForwardedValue.get(maxDOVYear-1+"-Improvement")+ factorForwardedValueAVOREcord.get(maxDOVYear-1+"-Improvement"));

					System.out.println(factorForwardedValue);

					
				}
				
			}
			System.out.println(factorForwardedValue);

			
		}
		
		/*
		 * This method is to perform factor forwarding of an existing AV record till the years as passed in argument
		 *  */

		public HashMap<String, Long> performFactorForwarding(String existingAVRecordToFactorForward_DOV,int factorForwardTillyear1,int factorForwardTillyear2,HashMap<Integer,Double> mapCPIFactors,Double existingAVRecordToFactorForwardLand,Double existingAVRecordToFactorForwardImprovement,Double existingAVORecordToFactorForwardOwnershipPercentage ) throws Exception
		{
			
			long factorForwardedLandValue=0;
			long factorForwardedImprovementValue=0;
			
			HashMap<String,Long> factorForwardedValues=new HashMap<String,Long>();

			
			Date dovDAte1=new SimpleDateFormat("yyyy-MM-dd").parse(existingAVRecordToFactorForward_DOV);  
			Calendar calendar1 = new GregorianCalendar();
			calendar1.setTime(dovDAte1);
			int dovYear = calendar1.get(Calendar.YEAR);
			int startYear;
			int dovMonth = calendar1.get(Calendar.MONTH)+1;
			
			Double calculatedLandFactorBasevalue =(existingAVORecordToFactorForwardOwnershipPercentage* existingAVRecordToFactorForwardLand)/100;
			Double calculatedImprovementFactorBasevalue =(existingAVORecordToFactorForwardOwnershipPercentage* existingAVRecordToFactorForwardImprovement)/100;

			if (dovMonth<=6)
				 startYear=dovYear+1;
			
			else
				 startYear=dovYear+2;
			
			
			
			for(int i=startYear;i<=factorForwardTillyear2;i++)

			{
				
				double cpiFactor=mapCPIFactors.get(i);
				 factorForwardedLandValue=(new Double(calculatedLandFactorBasevalue + (calculatedLandFactorBasevalue * (cpiFactor - 1)))).longValue();
								
				 factorForwardedImprovementValue =(new Double(calculatedImprovementFactorBasevalue + (calculatedImprovementFactorBasevalue * (cpiFactor - 1)))).longValue();		

				 calculatedLandFactorBasevalue=new Double(factorForwardedLandValue);
				 calculatedImprovementFactorBasevalue=new Double(factorForwardedImprovementValue);
				 
				 if(i==factorForwardTillyear1)
				 {
					 factorForwardedValues.put(factorForwardTillyear1+"-Land", factorForwardedLandValue);
				 
				 factorForwardedValues.put(factorForwardTillyear1+"-Improvement", factorForwardedImprovementValue);

				 }
				 
				 if(i==factorForwardTillyear2)
				 {
					 factorForwardedValues.put(factorForwardTillyear2+"-Land", factorForwardedLandValue);
				 
				 factorForwardedValues.put(factorForwardTillyear2+"-Improvement", factorForwardedImprovementValue);

				 }
				 
				 
					 
				 
			}
			
			
			System.out.println(factorForwardedValues);
			return factorForwardedValues;

			
		}

}