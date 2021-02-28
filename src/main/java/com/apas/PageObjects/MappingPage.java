package com.apas.PageObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;

public class MappingPage extends ApasGenericPage {
	Util objUtil;

	public MappingPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	public WebElement situsFieldInTable;
	public String actionDropDownLabel = "Action";
	public String taxesPaidDropDownLabel = "Are taxes fully paid?";
	public String reasonCodeTextBoxLabel = "Reason Code";
	public String parcelSizeDropDownLabel  = "Parcel Size Validation for Parent & Children Needed?";
	public String netLandLossTextBoxLabel = "Net Land Loss";
	public String netLandGainTextBoxLabel = "Net Land Gain";
	public String firstNonCondoTextBoxLabel = "First non-Condo Parcel Number";
	public String legalDescriptionTextBoxLabel = "Legal Description Auto-populate field for Child Parcels";
	public String situsTextBoxLabel = "Situs Auto-populate field for Child Parcels";
	public String commentsTextBoxLabel = "Comments";
	public String parentAPNTextBoxLabel = "Parent APN(s)";
	public String legalDescriptionColumnSecondScreen = "Legal Description";
	public String districtColumnSecondScreen = "District";
	public String apnColumnSecondScreen = "APN";
	public String reasonCodeColumnSecondScreen = "Reason Code";
	public String useCodeColumnSecondScreen = "Use Code";
	public String districtEditTextBoxSecondScreenLabel = "District";
	public String useCodeEditTextBoxSecondScreenLabel = "Use Code";
	public String numberOfChildNonCondoTextBoxLabel = "Number of Child Non-Condo Parcels";
	public String numberOfChildCondoTextBoxLabel = "Number of Child Condo Parcels";
	public String nextButton = "Next";
	public String generateParcelButton = "Generate Parcel";
	public String combineParcelButton = "Combine Parcel";
	public String parentAPNEditButton = "Edit";
	public String previousButton = "Previous";
	public String retireButton = "Retire Parcel (s)";
	public String assessorMapLabel = "Assessor's Map";
	public String taxCollectorLabel = "Tax Collector Link(s)";
	public String taxField = "//label[text()='Are taxes fully paid?']";
	public String reasonCodeField = "//label[text()='Reason Code']";
	public String errorMessageOnScreenOne = "//div[contains(@class,'flowruntimeBody')]//li |//div[contains(@class,'error') and not(contains(@class,'message-font'))]";
	public String saveButton = "Save";
	public String firstCondoTextBoxLabel = "First Condo Parcel Number";
	public String splitParcelButton = "Split Parcel";
	public String parcelStatus = "Status";
	public String parcelPUC = "PUC";
	public String parcelTRA = "TRA";
	public String parcelPrimarySitus = "Primary Situs";
	public String parcelDistrictNeighborhood = "District / Neighborhood Code";
	public String parcelShortLegalDescription = "Short Legal Description";
	public String firstNonCondoTextBoxLabel2 = "First Non-Condo Parcel Number";
	public String legalDescriptionTextBoxLabel2 = "Legal Description Auto-populate Field for Child Parcels";
	public String parcelLotSize = "Lot Size (SQFT)";

	@FindBy(xpath = "//label[text()='First non-Condo Parcel Number']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconFirstNonCondoParcelNumber;

	@FindBy(xpath = "//label[text()='Legal Description Auto-populate field for Child Parcels']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconLegalDescription;

	@FindBy(xpath = "//label[text()='Situs Auto-populate field for Child Parcels']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconSitus;

	@FindBy(xpath = "//div[contains(@id,'salesforce-lightning-tooltip-bubble')]")
	public WebElement helpIconToolTipBubble;

	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//li |//div[contains(@class,'error') and not(contains(@class,'message-font'))]")
	public WebElement errorMessageFirstScreen;
	
	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//*[@data-label='Legal Description']")
	public WebElement legalDescriptionFieldSecondScreen;
	
	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//*[@data-label='Use Code']")
	public WebElement useCodeFieldSecondScreen;
	
	@FindBy(xpath = "//div[contains(@class,'message-font slds-align_absolute-center slds-text-color_success')]")
	public WebElement confirmationMessageOnSecondScreen;
	
	/**
	 * @Description: This method will fill  the fields in Mapping Action Page mapping action
	 * @param dataMap: A data map which contains data to perform  mapping action
	 * @throws Exception
	 */
	public void fillMappingActionForm(Map<String, String> dataMap) throws Exception {
		String action = dataMap.get("Action");
		String taxesPaid = dataMap.get("Are taxes fully paid?");
		String reasonCode = dataMap.get("Reason code");
		String parcelSizeValidation = dataMap.get("Parcel Size Validation");
		String netLandLoss = dataMap.get("Net Land Loss");
		String netLandGain = dataMap.get("Net Land Gain");
		String firstnonCondoParcelNumber = dataMap.get("First non-Condo Parcel Number");
		String legalDescription = dataMap.get("Legal Description");
		String situs= dataMap.get("Situs");
		String comments= dataMap.get("Comments");
		String numberOfChildNonCondoParcels= dataMap.get("Number of Child Non-Condo Parcels");
		String numberOfChildCondoParcels= dataMap.get("Number of Child Condo Parcels");
		String firstCondoParcelNumber= dataMap.get("First Condo Parcel Number");


		selectOptionFromDropDown(actionDropDownLabel, action);
		selectOptionFromDropDown(taxesPaidDropDownLabel, taxesPaid);
		if (reasonCode != null)enter(reasonCodeTextBoxLabel, reasonCode);
		if (parcelSizeValidation != null)selectOptionFromDropDown(parcelSizeDropDownLabel, parcelSizeValidation);
		if (netLandLoss != null)enter(netLandLossTextBoxLabel, netLandLoss);
		if (netLandGain != null)enter(netLandGainTextBoxLabel, netLandGain);
		if (numberOfChildNonCondoParcels != null)
			enter(numberOfChildNonCondoTextBoxLabel, numberOfChildNonCondoParcels);
		if (firstnonCondoParcelNumber != null)
			enter(firstNonCondoTextBoxLabel, firstnonCondoParcelNumber);
		if (numberOfChildCondoParcels != null)
			enter(numberOfChildCondoTextBoxLabel, numberOfChildCondoParcels);
		if (firstCondoParcelNumber != null)
			enter(firstCondoTextBoxLabel, firstCondoParcelNumber);
		if (legalDescription != null)
			enter(legalDescriptionTextBoxLabel, legalDescription);
		if (situs != null)
			enter(situsTextBoxLabel, situs);
		if (comments != null)
			enter(commentsTextBoxLabel, comments);
		
		Click(getButtonWithText(nextButton));
	}

	/**
	 * @Description: This method will generate child parcels for  mapping action 
	 *@param dataMap: A data map which contains data to perform one to one mapping
	 * @throws Exception
	 */
	public void generateChildParcelsMappingActions(Map<String, String> dataMap) throws Exception {
		fillMappingActionForm(dataMap);
		Click(getButtonWithText(generateParcelButton));
	}
	/**
	 * @Description: This method will enter value in mapping action page fields and return the error message that would be displayed on page
	 **@param element: ThE element on which validations are needed to be verified
	 * @throws Exception
	 */
	public String getMappingActionsFieldsErrorMessage(Object element,String value) throws Exception {
		enter(element, value);
		
		if(verifyElementVisible(saveButton))
			Click(getButtonWithText(saveButton));

		else
		Click(getButtonWithText(nextButton));
		Thread.sleep(6000);
		if(verifyElementVisible(errorMessageFirstScreen))
				return  getElementText(errorMessageFirstScreen);
		else
			return "No error message is displayed on page";
		
	}

	public void remapActionForm(Map<String, String> dataMap) throws Exception {
		String action = dataMap.get("Action");
		String reasonCode = dataMap.get("Reason code");
		String firstnonCondoParcelNumber = dataMap.get("First non-Condo Parcel Number");
		String comments= dataMap.get("Comments");

		selectOptionFromDropDown(actionDropDownLabel, action);
		if (reasonCode != null)enter(reasonCodeTextBoxLabel, reasonCode);
		if (firstnonCondoParcelNumber != null)
			enter(firstNonCondoTextBoxLabel, firstnonCondoParcelNumber);
		if (comments != null)
			enter(commentsTextBoxLabel, comments);
		Click(getButtonWithText(nextButton));

	}
	
	/**
	 * Description: this method is to get the confirmation message after mapping action is completed
	 * 	 
	 * @throws: Exception
	 */
	public String confirmationMsgOnSecondScreen() throws Exception {
		Thread.sleep(3000);
		return getElementText(waitForElementToBeClickable(20, confirmationMessageOnSecondScreen));
	}
	
	
	/**
	 * Description: This method will take the generated APN (from Mapping action) and then create the next one in that series
	 * @param Num: Takes APN as an argument
     * @returns  Returns the created APN
     */
	public String generateNextAvailableAPN(String apn) throws Exception {
	
		String updatedAPN = "";
		
		/*Some Examples*/
		/*100-100-010  --> 100-100-020, 100-090-980  --> 100-090-990, 100-090-890  --> 100-090-900, 100-890-070	 --> 100-890-070*/
		if (!apn.substring(8, 10).equals("99")){
			String getLastThreeDigits = apn.substring(8);
			int incrementByTen = Integer.valueOf(getLastThreeDigits)  +  10;
			String incrementedAPN = String.valueOf(incrementByTen);
			if (incrementedAPN.length() < 3) updatedAPN = apn.substring(0, 8).concat("0").concat(incrementedAPN);
			if (incrementedAPN.length() == 3) updatedAPN = apn.substring(0, 8).concat(incrementedAPN);	
		}
		else{		
			if (!apn.substring(4, 6).equals("99")){
				
				/*Some Examples*/
				/*100-090-990  -->  100-100-010, 100-290-990  -->  100-300-010, 100-280-990  -->  100-290-010, 100-297-990  -->  100-300-010*/
				if (apn.substring(6, 7).equals("0")){
					String getMiddleThreeDigits = apn.substring(4,7);
					int incrementByTen = Integer.valueOf(getMiddleThreeDigits)  +  10;
					String incrementedAPN = String.valueOf(incrementByTen);
					if (incrementedAPN.length() < 3) updatedAPN = apn.substring(0, 4).concat("0").concat(incrementedAPN).concat("-010");
					if (incrementedAPN.length() == 3) updatedAPN = apn.substring(0, 4).concat(incrementedAPN).concat("-010");	
				}
				else{
					
					/*Some Examples*/
					/*100-145-990  -->  100-150-010, 100-237-990  -->  100-240-010*/
					String getPartOfMapPage = apn.substring(4,6);
					int incrementByOne = Integer.valueOf(getPartOfMapPage)  +  1;
					String incrementedAPN = String.valueOf(incrementByOne);
					if (incrementedAPN.length() < 2) updatedAPN = apn.substring(0, 4).concat("0").concat(incrementedAPN).concat("0-010");
					if (incrementedAPN.length() == 2) updatedAPN = apn.substring(0, 4).concat(incrementedAPN).concat("0-010");	
				}
			}	
			else{
				/*Example : 100-990-990*/
				updatedAPN = "Warning : 990 limit has been reached for current Map Page, so move to the next Map Book";
			}
	    }
		return updatedAPN;
	}
	
	/**
	 * Description: This method will fetch the Parcel# from the Work Item
	 * @param rowNum: Row# in the linked item
	 * return : Returns the Parcel#
	 */
	public String getLinkedParcelInWorkItem(String rowNum) throws Exception {
		Thread.sleep(1000);
		String xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//tr[@data-row-key-value='row-" + rowNum + "']//th";
		waitUntilElementIsPresent(xpath, 10);
		return getElementText(driver.findElement(By.xpath(xpath)));
	}
	
	/*
    This method is used to return the first owner record from Salesforce
    @return: returns the active APN
   */
   public String getOwnerForMappingAction() {
       return getOwnerForMappingAction(1).get("Name").get(0);
   }

   public HashMap<String, ArrayList<String>> getOwnerForMappingAction(int numberofRecords) {
	   String queryOwnerRecord = "SELECT Id, Name FROM Account Limit " + numberofRecords;
	   return objSalesforceAPI.select(queryOwnerRecord);
   }
   
   /*
   This method is used to return the Retired APN having no Ownership record
   @return: returns the Retired APN
  */

   public HashMap<String, ArrayList<String>> getRetiredApnHavingNoOwner() throws Exception {
	   String queryRetiredAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Status__c = 'Retired' Limit 1";
 	  	return objSalesforceAPI.select(queryRetiredAPNValue);
   }
 
  /*
  This method is used to return the In Progress APN having no Ownership record
  @return: returns the In Progress APN
 */

   public HashMap<String, ArrayList<String>> getInProgressApnHavingNoOwner() throws Exception {
	   String queryInProgressAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Status__c like 'In Progress%' Limit 1";
	   return objSalesforceAPI.select(queryInProgressAPNValue);
   }
 	
 		
   /*
   This method is used to return the Active APN having a specific Ownership record
   @return: returns the Active APN
  */
    public HashMap<String, ArrayList<String>> getActiveApnHavingOwner(String assesseeName) throws Exception {
    	return getActiveApnHavingOwner(assesseeName, 1);
    }
    
    public HashMap<String, ArrayList<String>> getActiveApnHavingOwner(String assesseeName, int numberofRecords) throws Exception {
    	String queryActiveAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName + "') and (Not Name like '%990') and (Not Name like '134%') and (Not Name like '100%') and Status__c = 'Active' Limit " + numberofRecords;
    	return objSalesforceAPI.select(queryActiveAPNValue);
    }
    
    /*
    This method is used to return the Active APN having no Ownership record
    @return: returns the Active APN
   */
    
     public HashMap<String, ArrayList<String>> getActiveApnWithNoOwner() throws Exception {
    	return getActiveApnWithNoOwner(1);
     }
    
     public HashMap<String, ArrayList<String>> getActiveApnWithNoOwner(int numberofRecords) throws Exception {
     	String queryActiveAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '134%') and (Not Name like '100%') and Status__c = 'Active' Limit " + numberofRecords;
     	return objSalesforceAPI.select(queryActiveAPNValue);
     }
     
     /*
     This method is used to return the Condo APN (Active) having a specific Ownership record
     @return: returns the Condo Active APN
    */
      public HashMap<String, ArrayList<String>> getCondoApnHavingOwner(String assesseeName) throws Exception {
      	return getCondoApnHavingOwner(assesseeName, 1);
      }
      
      public HashMap<String, ArrayList<String>> getCondoApnHavingOwner(String assesseeName, int numberofRecords) throws Exception {
      	String queryCondoAPNValue = "SELECT Name, Id from parcel__c where Id in (Select parcel__c FROM Property_Ownership__c where Owner__r.name = '" + assesseeName + "') AND Id Not IN (Select parcel__c FROM Property_Ownership__c where Owner__r.name != '" + assesseeName + "') and (Not Name like '%990') and name like '100%' and Status__c = 'Active' Limit " + numberofRecords;
      	return objSalesforceAPI.select(queryCondoAPNValue);
      }
     
      /*
      This method will delete existing relationship instances (Source) from the Parcel
     */
      
      public void deleteSourceRelationshipInstanceFromParcel(String apn) throws Exception {
    	  String query = "SELECT Id FROM Parcel_Relationship__c where Source_Parcel__r.name = '" + apn + "'";
    	  HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);
    	  if(!response.isEmpty())objSalesforceAPI.delete("Parcel_Relationship__c", query);
  	  }
      
      /*
      This method will convert APN into Integer
     */
      
      public int convertAPNIntoInteger(String apn) throws Exception {
    	 String apnComponent[] = apn.split("-");
  		 String consolidateAPN = apnComponent[0] + apnComponent[1] + apnComponent[2];
  		 return Integer.valueOf(consolidateAPN);
  	  }
       
}
