package com.apas.PageObjects;

import java.awt.AWTException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.apas.config.users;

public class MappingPage extends ApasGenericPage {
	Util objUtil;

	public MappingPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	public WebElement situsFieldInTable;
	public String actionDropDownLabel = "Action";
	public String taxesPaidDropDownLabel = "Are Taxes Fully Paid?";
	public String reasonCodeTextBoxLabel = "Reason Code";
	public String parcelSizeDropDownLabel  = "Parcel Size Validation for Parent & Children Needed?";
	public String netLandLossTextBoxLabel = "Net Land Loss (SQ FT)";
	public String netLandGainTextBoxLabel = "Net Land Gain (SQ FT)";
	public String firstNonCondoTextBoxLabel = "First Non-Condo Parcel Number";
	public String legalDescriptionTextBoxLabel = "Legal Description Auto-Populate Field for Child Parcels";
	public String situsTextBoxLabel = "Situs Auto-Populate Field for Child Parcels";
	public String situsTextBoxLabelForBrandNewParcel = "Situs";
	public String commentsTextBoxLabel = "Comments";
	public String parentAPNTextBoxLabel = "Parent APN(s)";
	public String legalDescriptionColumnSecondScreen = "Legal Description*";
	public String distNbhdColumnSecondScreen = "Dist/Nbhd*";
	public String districtColumnSecondScreen = "District";
	public String apnColumnSecondScreen = "APN";
	public String reasonCodeColumnSecondScreen = "Reason Code*";
	public String useCodeColumnSecondScreen = "Use Code";
	public String districtEditTextBoxSecondScreenLabel = "District";
	public String useCodeEditTextBoxSecondScreenLabel = "Use Code";
	public String numberOfChildNonCondoTextBoxLabel = "Number of Child Non-Condo Parcels";
	public String numberOfChildCondoTextBoxLabel = "Number of Child Condo Parcels";
	public String numberOfIntermiParcelLabel = "Number of Interim Parcel";
	public String nextButton = "Next";
	public String generateParcelButton = "Generate Parcel(s)";
	public String combineParcelButton = "Combine Parcel";
	public String parentAPNEditButton = "Edit";
	public String previousButton = "Previous";
	public String retireButton = "Retire Parcel (s)";
	public String assessorMapLabel = "Assessor's Map";
	public String taxCollectorLabel = "Tax Collector Link(s)";
	public String taxField = "//label[text()='Are Taxes Fully Paid?']";
	public String reasonCodeField = "//label[text()='Reason Code']";
	public String errorMessageOnScreenOne = "//div[contains(@class,'flowruntimeBody')]//li |//div[contains(@class,'error') and not(contains(@class,'message-font'))]";
	public String saveButton = "Save";
	public String firstCondoTextBoxLabel = "First Condo Parcel Number";
	public String parcelStatus = "Status";
	public String parcelPUC = "PUC";
	public String parcelTRA = "TRA";
	public String parcelPrimarySitus = "Primary Situs";
	public String parcelDistrictNeighborhood = "District / Neighborhood Code";
	public String parcelShortLegalDescription = "Short Legal Description";
	public String firstNonCondoTextBoxLabel2 = "First Non-Condo Parcel Number";
	public String legalDescriptionTextBoxLabel2 = "Legal Description Auto-Populate Field for Child Parcels";
	public String legalDescriptionBrandNewTextBoxLabel = "Legal Description";
	public String parcelLotSize = "Lot Size (SQFT)";
	public String situsCityDescriptionLabel = "Situs City Description";
	public String cityNameLabel = "City Name";
	public String situsCityCodeLabel = "Situs City Code";
	public String situsCityNameLabel = "Situs City Name";
	public String directionLabel = "Direction";
	public String situsNumberLabel = "Situs Number";
	public String situsStreetNameLabel = "Situs Street Name";
	public String situsTypeLabel = "Situs Type";
	public String situsUnitNumberLabel = "Situs Unit Number";
	public String closeButton = "Close";
	public String CreateNewParcelButton="Create Brand New Parcel";
	public String updateParcelsButton = "//button[text()='Update Parcel(s)']";
	public String updateParcelButtonLabelName = "Update Parcel(s)";
	public String parcelSizeColumnSecondScreen = "Parcel Size(SQFT)*";
	public String apn = "APN";
	public String parcelSizeColumnSecondScreenWithSpace = "Parcel Size (SQFT)*";
	public final String DOC_CERTIFICATE_OF_COMPLIANCE="CC";
	public final String DOC_LOT_LINE_ADJUSTMENT="LL";
	public final String DOC_Covenants_Cond_Restr_with_condo = "CCR";
	public final String DOC_Condominium_plans = "CP";
	public final String DOC_Decl_of_Covenants_Cond_Restrictions = "DR";
	public final String DOC_Easements = "ES";
	public final String DOC_Offers_of_Dedication = "IC";
	public final String DOC_Lot_Consolidation_Certificate = "LCC";
	public final String DOC_Notice_of_Merger = "NM";
	public final String DOC_Property_Settlement_Agreement = "PSA";
	public final String DOC_Sub_Divison_Map = "SDM";
	public final String DOC_Official_Map  = "OM";
	public String secondScreenEditButton = "//button[contains(@class,'slds-button_icon-border slds-button_icon-x-small')]";
	public String errorCompleteThisField = "Complete this field.";
	public String editParcel = "Edit Parcel";
	public String parcelSitus ="Parcel Situs";
	public String performAdditionalMappingButton = "Perform Additional Mapping Action";
	public String userNameForRpAppraiser = CONFIG.getProperty(users.RP_APPRAISER + "UserName");
	public String appraiserwWorkPool = "Appraiser";

	@FindBy(xpath = "//*[contains(@class,'slds-dropdown__item')]/a")
	public WebElement editButtonInSeconMappingScreen;
	
	@FindBy(xpath = "//button[contains(@class,'slds-button_icon-border slds-button_icon-x-small')]/ancestor::tr/following-sibling::tr//button[contains(@class,'slds-button_icon-border slds-button_icon-x-small')]")
	public WebElement secondmappingSecondScreenEditActionGridButton;
	
	@FindBy(xpath = "//button[@title='Clear Selection'][1]/ancestor::lightning-input-field[1]//button")
	public WebElement clearSelectionTRA;
	
	@FindBy(xpath = "//button[@title='Clear Selection'][1]/ancestor::lightning-input-field/following-sibling::lightning-input-field//button")
	public WebElement clearSelectionNeigh;
	
	@FindBy(xpath = "(//button[@title='Clear Selection'][1]/ancestor::lightning-input-field/following-sibling::lightning-input-field//button)[2]")
	public WebElement clearSelectionPUC;
	
	@FindBy(xpath = "//button[contains(@class,'slds-button_icon-border slds-button_icon-x-small')]")
	public WebElement mappingSecondScreenEditActionGridButton;
	
	@FindBy(xpath = "//label[text()='First Non-Condo Parcel Number']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconFirstNonCondoParcelNumber;

	@FindBy(xpath = "//label[text()='Legal Description Auto-Populate Field for Child Parcels']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconLegalDescription;

	@FindBy(xpath = "//label[text()='Situs Auto-Populate Field for Child Parcels']/..//div[@class='slds-form-element__icon']")
	public WebElement helpIconSitus;

	@FindBy(xpath = "//div[contains(@id,'salesforce-lightning-tooltip-bubble')]")
	public WebElement helpIconToolTipBubble;
	
	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//*[@data-label='Legal Description*']")
	public WebElement legalDescriptionFieldSecondScreen;

	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//li[last()] |//div[contains(@class,'error') and not(contains(@class,'message-font'))]")
	public WebElement errorMessageFirstScreen;
	
	@FindBy(xpath = "//div[contains(@id,'help-message')]")
	public WebElement errorMessageOnFirstCustomScreen;
	
	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//li[1]")
	public WebElement errorMessageonSecondCustomScreen;
	
	@FindBy(xpath = "//div[contains(@class,'flowRuntimeV2')]//c-org_parcel-process-brand-new-view[1]//div[contains(@class,'error')]//li")
	public WebElement errorMessageSecondScreen;
	
	@FindBy(xpath = "//div[contains(@class,'flowruntimeBody')]//*[@data-label='Use Code*']")
	public WebElement useCodeFieldSecondScreen;
	
	@FindBy(xpath = "//div[contains(@class,'message-font slds-align_absolute-center slds-text-color_success')]")
	public WebElement confirmationMessageOnSecondScreen;
	
	@FindBy(xpath = "//header[@class='slds-modal__header']//h2[text()='Edit Situs']")
	public WebElement editSitusLabelSitusModal;
	
	@FindBy(xpath = "//div[@class='slds-card__header slds-grid']//span[text()='Situs Information']")
	public WebElement situsInformationLabelSitusModal;
	
	@FindBy(xpath = "//*[text()='Parent APN(s)']//following::span[@class='slds-col']")
	public WebElement parentAPNFieldValue;
	
	@FindBy(xpath = "//button[@title='Remap Parcel (s)']")
	public WebElement remapParcelButton ;
	
	@FindBy(xpath = "//div[@class='slds-hyphenate']/*[contains(text(),'The APN provided is a duplicate')]")
	public WebElement remapErrorMessageonSecondScreen;
	
	@FindBy(xpath="//div[@class='uiOutputRichText']//span")
	public WebElement mappingScreenError;
	
	@FindBy(xpath="//div[contains(@class,'error')]//li[1]")
	public WebElement secondScreenParcelSizeWarning;
	
	@FindBy(xpath="//th[@data-label='APN']")
	public WebElement apnFieldInGridOnCustomScreen;

	@FindBy(xpath="//div[contains(@class,'error')][1]")
	public WebElement dividedInterestErrorMsgSecondScreen;
	
	@FindBy(xpath="//div[@title='Edit']")
	public WebElement parcelSitusEditButton;
	
	@FindBy(xpath = "//*[contains(@class,'forceVirtualActionMarker forceVirtualAction')]//a")
	public WebElement parcelSitusGridEditButton;
	
	@FindBy(xpath = "//lightning-button//button[text()='Save']")
	public WebElement parcelSitusEditSaveButton;
	
	@FindBy(xpath = "//h2[contains(text(),'Edit PS-')]")
	public WebElement visibleParcelSitusEditpopUp;
	
	@FindBy(xpath = "//*[contains(@class,'NewButtonForParcel')]//div[@class='override_error']")
	public WebElement createNewParcelErrorMessage;
	
	@FindBy(xpath = "//*[contains(@class,'message-font slds-align_absolute-center slds-text-color_success slds-m-bottom_medium slds-m-top_medium')]")
	public WebElement createNewParcelSuccessMessage;

	
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
		String legalDescriptionBrandNewAction = dataMap.get("Legal Descriptions");
		String comments= dataMap.get("Comments");
		String numberOfChildNonCondoParcels= dataMap.get("Number of Child Non-Condo Parcels");
		String numberOfChildCondoParcels= dataMap.get("Number of Child Condo Parcels");
		String firstCondoParcelNumber= dataMap.get("First Condo Parcel Number");

		selectOptionFromDropDown(actionDropDownLabel, action);
		if (taxesPaid != null)selectOptionFromDropDown(taxesPaidDropDownLabel, taxesPaid);
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
		// Below check added exclusively for Brand New action form
		if (legalDescriptionBrandNewAction != null)
			enter(legalDescriptionBrandNewTextBoxLabel, legalDescriptionBrandNewAction);
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
	 * @Description: This method will create situs for child parcel from situs modal window from first screen	
	 * @param dataMap: A data map which contains data to create situs
	 * @throws Exception
	 */
	public void editSitusModalWindowFirstScreen(Map<String, String> dataMap) throws Exception {
		
		String cityName = dataMap.get("City Name");
		String situsCityCode = dataMap.get("Situs City Code");
		String situsCityName = dataMap.get("Situs City Name");
		String direction = dataMap.get("Direction");
		String situsNumber = dataMap.get("Situs Number");
		String situsStreetName = dataMap.get("Situs Street Name");
		String situsType = dataMap.get("Situs Type");
		String situsUnitNumber = dataMap.get("Situs Unit Number");
		
		if (cityName != null) selectOptionFromDropDown(cityNameLabel, cityName);
		if (situsCityCode != null) selectOptionFromDropDown(situsCityCodeLabel, situsCityCode);
		if (situsCityName != null) enter(situsCityNameLabel, situsCityName);
		if (direction != null) selectOptionFromDropDown(directionLabel, direction);
		if (situsNumber != null) enter(situsNumberLabel, situsNumber);
		if (situsStreetName != null) enter(situsStreetNameLabel, situsStreetName);
		if (situsType != null) selectOptionFromDropDown(situsTypeLabel, situsType);
		if (situsUnitNumber != null) enter(situsUnitNumberLabel, situsUnitNumber);
		Click(getButtonWithText(saveButton));
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
		String xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//tr[@data-row-key-value='row-" + rowNum + "']//th/lightning-primitive-cell-factory[@data-label='APN']";
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
	   String queryRetiredAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c = 'Retired' Limit 1";
 	  	return objSalesforceAPI.select(queryRetiredAPNValue);
   }
 
  /*
  This method is used to return the In Progress APN having no Ownership record
  @return: returns the In Progress APN
 */

   public HashMap<String, ArrayList<String>> getInProgressApnHavingNoOwner() throws Exception {
	   String queryInProgressAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') and Status__c like 'In Progress%' Limit 1";
	   return objSalesforceAPI.select(queryInProgressAPNValue);
   }
    
    /*
    This method is used to return the Active APN having no Ownership record
    @return: returns the Active APN
   */
    
     public HashMap<String, ArrayList<String>> getActiveApnWithNoOwner() throws Exception {
    	return getActiveApnWithNoOwner(1);
     }
    
     public HashMap<String, ArrayList<String>> getActiveApnWithNoOwner(int numberofRecords) throws Exception {
        
    	 String queryActiveAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and (Not Name like '%990') and (Not Name like '1%') and (Not Name like '8%') "
    	 		+ "and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') "
    	 		+ "and Status__c = 'Active' Limit " + numberofRecords;   	    
     	return objSalesforceAPI.select(queryActiveAPNValue);
     }
     
     /*
     This method is used to return the Condo APN (Active) having a specific Ownership record
     @return: returns the Condo Active APN
    */

      public HashMap<String, ArrayList<String>> getCondoApnWithNoOwner() throws Exception {
        	return getCondoApnWithNoOwner(1);
        }
        
      public HashMap<String, ArrayList<String>> getCondoApnWithNoOwner(int numberofRecords) throws Exception {
        	String queryCondoAPNValue = "SELECT Name, Id from parcel__c where Id NOT in (Select parcel__c FROM Property_Ownership__c) and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO')  and (Not Name like '%990') and name like '1%' and Status__c = 'Active' Limit " + numberofRecords;
        	return objSalesforceAPI.select(queryCondoAPNValue);
        }
     
      /**
       *  This method will delete existing relationship instances  from the Parcel
       * @param apn-Apn whose records needs to be deleted
       * @return
       * @throws Exception
       */
      public void deleteRelationshipInstanceFromParcel(String apn)
      {
    	  String query ="SELECT  Id,Target_Parcel__c FROM Parcel_Relationship__c where source_parcel__r.name='" +apn+"'";
    	  HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);
    	  
    	  if(!response.isEmpty())
    	  {
    		      response.get("Id").stream().forEach(Id ->{
    			  objSalesforceAPI.delete("Parcel_Relationship__c", Id);    			  
    		  });     	    				  
    	  }
      }
      
      /*
      This method will convert APN into Integer
     */
      
      public int convertAPNIntoInteger(String apn) throws Exception {
    	 String apnComponent[] = apn.split("-");
  		 String consolidateAPN = apnComponent[0] + apnComponent[1] + apnComponent[2];
  		 return Integer.valueOf(consolidateAPN);
  	  }
  /*
   * 
   *     This method is used to enter the values in edit action screen
   *     
   */
      public void editActionInMappingSecondScreen(Map<String, String> dataMap) throws Exception {
    		
			String PUC = objSalesforceAPI.select("SELECT Name FROM PUC_Code__c  limit 1").get("Name").get(0);
			String TRA = objSalesforceAPI.select("SELECT Name FROM TRA__c limit 1").get("Name").get(0);
			String distNeigh = objSalesforceAPI.select("SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 1").get("Name").get(0);
		    objSalesforceAPI.update("PUC_Code__c",objSalesforceAPI.select("Select Id from PUC_Code__c where name='"+PUC+"'").get("Id").get(0), "Legacy__c", "No");

			Click(editButtonInSeconMappingScreen);

			clearSelectionFromLookup("TRA");
			enter(parcelTRA, TRA);
			Thread.sleep(2000);
			selectOptionFromDropDown(parcelTRA, TRA);
			ReportLogger.INFO("TRA:" +TRA);

			clearSelectionFromLookup("District / Neighborhood Code");
			enter(parcelDistrictNeighborhood, distNeigh);
			selectOptionFromDropDown(parcelDistrictNeighborhood, distNeigh);
			ReportLogger.INFO("District / Neighborhood Code:" +distNeigh);

			clearSelectionFromLookup("PUC");
			enter(parcelPUC, PUC);
			selectOptionFromDropDown(parcelPUC, PUC);
			ReportLogger.INFO("PUC:" + PUC);

			editSitusModalWindowFirstScreen(dataMap);
			
	}
      
      public void updateMultipleGridCellValue(String columnNameOnGrid, String expectedValue,int i) throws IOException, AWTException, InterruptedException {
    		String xPath =  "//lightning-tab[contains(@class,'slds-show')]//tr["+i+"]"
    				+ "//*[contains(@data-label,'" + columnNameOnGrid + "')][@role='gridcell']"
    						+ "//button | //div[contains(@class,'flowruntimeBody')]"
    						+ "//*[contains(@data-label,'" + columnNameOnGrid + "')]";

	    		 WebElement webelement = driver.findElement(By.xpath(xPath));
	    	 	 Click(webelement);
	   		     Thread.sleep(1000);
	    		 if(verifyElementVisible("//tr["+i+"]//*[contains(@data-label,'" + columnNameOnGrid + "')]"
	    				+ "//button[@data-action-edit='true']"))
	    			Click(driver.findElement(By.xpath("//tr["+i+"]//*[contains(@data-label,'"
	    				+ columnNameOnGrid + "')]//button[@data-action-edit='true']")));
	    		WebElement webelementInput = driver.findElement(By.xpath("//input[@class='slds-input']"));
	
	    		waitForElementToBeClickable(30, webelementInput);
	    		webelementInput.clear();
	    		webelementInput.sendKeys(expectedValue);
	    		
	    		Actions objAction=new Actions(driver);
	  		    objAction.sendKeys(Keys.ENTER).build().perform();
	    		Thread.sleep(2000);
    	}
      /*
       * this method is used to validate parent APNs on custom mapping Second screen
       */
      public boolean validateParentAPNsOnMappingScreen(String parentAPNs) {
    	  boolean flag = false;
     	    if(parentAPNs.contains(",")) {
     	    	String [] parentAPN=parentAPNs.split(",");    	    
     	        for(int i=0;i<parentAPN.length;i++) {
     	          String xPath="//div//*[text()='Parent APN(s): ']/following-sibling::a[text()='"+parentAPN[i]+"']";
     	          if( verifyElementVisible(xPath)) flag=true;
     	         
     	    }
     	    }else {
     	    	String xPath="//div//*[text()='Parent APN(s): ']/following-sibling::a[text()='"+parentAPNs+"']";
   	          if( verifyElementVisible(xPath)) flag=true;
   	          
     	    }
     	  return flag;
       }
      
      /*
       * this method is used to validate parent APNs on custom mapping first screen
       */
		public boolean validateParentAPNsOnMappingFirstScreen(String parentAPNs) {
			boolean flag = false;

			String xPath = "//label[text()='Parent APN(s)']/following::span[text()='" + parentAPNs + "']";
			if (verifyElementVisible(xPath))
				flag = true;

			return flag;
		}
		
		 /**
	       *  This method will delete existing characteristic instances  from the Parcel
	       * @param apn-Apn whose records needs to be deleted
	       * @return
	       * @throws Exception
	       */
			public void deleteCharacteristicInstanceFromParcel(String apn) {
				String query = "SELECT Id FROM Characteristics__c where APN__c in( SELECT id FROM Parcel__c where name='"
						+ apn + "')";
				HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);

				if (!response.isEmpty()) {
					response.get("Id").stream().forEach(Id -> {
						objSalesforceAPI.delete("Characteristics__c", Id);
						ReportLogger.INFO("Characteristics deleted for Id ::"+Id);
					});
				}
			}
			
			
			/**
			 *  This method will delete existing mailTo instances  from the Parcel
			 * @param apn-Apn whose records needs to be deleted
			 * @return
			 * @throws Exception
			 */
			public void deleteMailToInstanceFromParcel(String apn) {
				String query = "SELECT Id FROM Mail_To__C where Parcel__c in( SELECT id FROM Parcel__c where name='"
						+ apn + "')";
				HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);

				if (!response.isEmpty()) {
					response.get("Id").stream().forEach(Id -> {
						objSalesforceAPI.delete("Mail_To__C", Id);
						ReportLogger.INFO("Mail To deleted for Id ::"+Id);
					});
				}
			}
			
			/**
			 *  This method will delete existing Work item instances  from the Parcel
			 * @param apn-Apn whose records needs to be deleted
			 * @return
			 * @throws Exception
			 */
			public void deleteExistingWIFromParcel(String apn) {
				String query ="Select id, name , parcel__c, work_item__r.Name from"
						+ " work_item_linkage__c where parcel__r.Name = '"+ apn 
						+"' and work_item__r.status__c != 'completed' and  Work_Item__r.type__c = 'CIO'";
				
				HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);

				if (!response.isEmpty()) {
					response.get("Id").stream().forEach(Id -> {
						objSalesforceAPI.delete("work_item_linkage__c", Id);
						ReportLogger.INFO("work_item_linkage__c  deleted for Id ::"+Id);
					});
				}
			}

			/**
			 * @Description: This method will fill  the fields in Mapping Action Page mapping action
			 * @param dataMap: A data map which contains data to perform  mapping action
			 * @throws Exception
			 */
			public void fillMappingActionFormWithSitus(Map<String, String> dataMap) throws Exception {
				String action = dataMap.get("Action");
				String taxesPaid = dataMap.get("Are taxes fully paid?");
				String reasonCode = dataMap.get("Reason code");
				String parcelSizeValidation = dataMap.get("Parcel Size Validation");
				String netLandLoss = dataMap.get("Net Land Loss");
				String netLandGain = dataMap.get("Net Land Gain");
				String firstnonCondoParcelNumber = dataMap.get("First non-Condo Parcel Number");
				String legalDescription = dataMap.get("Legal Description");
				String legalDescriptionBrandNewAction = dataMap.get("Legal Descriptions");
				String comments= dataMap.get("Comments");
				String numberOfChildNonCondoParcels= dataMap.get("Number of Child Non-Condo Parcels");
				String numberOfChildCondoParcels= dataMap.get("Number of Child Condo Parcels");
				String firstCondoParcelNumber= dataMap.get("First Condo Parcel Number");

				selectOptionFromDropDown(actionDropDownLabel, action);
				if (taxesPaid != null)selectOptionFromDropDown(taxesPaidDropDownLabel, taxesPaid);
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
				if (comments != null)
					enter(commentsTextBoxLabel, comments);
				
				clearFieldValue("Situs");
				enter(getWebElementWithLabel("Situs Number"), "101");
				enter(getWebElementWithLabel("Situs Street Name"), "ST");
				enter(getWebElementWithLabel("Situs Unit Number"), "102");
				clearFieldValue("Situs Type");
				selectOptionFromDropDown("Situs Type", "DR");
				clearFieldValue("City Name");
				selectOptionFromDropDown("City Name", "ATHERTON");
				Click(getButtonWithText("Save"));

				
			}

			/**
			 * @Description This method can be utilized to update parcel's PUC and District
			 *              and neighbourhood Code through Puc and District WI Custom page
			 * @return Array of String , containing updated PUC and District Neighbrood code
			 * @throws Exception
			 */
		
			public String[] editActionInUpdatePucAndCharsScreen() throws Exception {

				Click(mappingSecondScreenEditActionGridButton);
				Click(editButtonInSeconMappingScreen);

				String PUC = objSalesforceAPI.select("SELECT Name FROM PUC_Code__c where legacy__c='no' limit 4").get("Name").get(3);

				String distNeigh = objSalesforceAPI.select("SELECT Name,Id  FROM Neighborhood__c where Name !=NULL limit 4").get("Name").get(3);
				//objSalesforceAPI.update("PUC_Code__c",objSalesforceAPI.select("Select Id from PUC_Code__c where name='" + PUC + "'").get("Id").get(0),"Legacy__c", "No");
				clearSelectionFromLookup("District / Neighborhood Code");
				enter(parcelDistrictNeighborhood, distNeigh);
				selectOptionFromDropDown(parcelDistrictNeighborhood, distNeigh);
				ReportLogger.INFO("District / Neighborhood Code:" + distNeigh);

				clearSelectionFromLookup("PUC");
				enter(parcelPUC, PUC);
				selectOptionFromDropDown(parcelPUC, PUC);
				ReportLogger.INFO("PUC:" + PUC);
				Click(getButtonWithText(SaveButton));
				Thread.sleep(2000);
				return new String[] { PUC, distNeigh };
			}

		}