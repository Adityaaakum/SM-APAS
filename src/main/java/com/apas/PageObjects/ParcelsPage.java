package com.apas.PageObjects;

import com.apas.Reports.ExtentTestManager;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import com.apas.Utils.Util;
import com.relevantcodes.extentreports.LogStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParcelsPage extends ApasGenericPage {
	Util objUtil;
	SalesforceAPI objSalesforceAPI = new SalesforceAPI();

	public ParcelsPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objUtil = new Util();
	}

	public String componentActionsButtonText = "Component Actions";
	public String nextButtonComponentsActionsModal = "Next";
	public String parcelMapInGISPortal = "Parcel Map in GIS Portal";

	public String workItemTypeDropDownComponentsActionsModal = "Work Item Type";
	public String referenceInputTextBoxComponentActionModal = "Reference";
	public String descriptionInputTextBoxComponentActionModal = "Description";
	public String dueDateInputTextBox = "Due_Date";
	public String dovInputTextBox = "DOV";
	public String actionsDropDownLabel = "Actions";
	public String selectOptionDropDownComponentsActionsModal = "Select Option";
	public String priorityDropDownComponentsActionsModal = "Priority";
	public String workItemRoutingDropDownComponentsActionsModal = "Work Item Routing";
	public String workItemOwnerSearchBox = "Work Item Owner (if someone other than you)";

	
	public String editApnField ="APN";	
	public String LongLegalDescriptionLabel="Long Legal Description"; 

	public String statusDropDownLabel = "Status";
	public String parcelRelationshipsTabLabel = "Parcel Relationships";
	public String ownershipTabLabel = "Ownership";
	public String ownerDropDown = "Owner";
	public String typeDropDown = "Type";
	public String statusDropDown = "Status";
	public String bppAccountDropDown = "BPP Account";
	public String ownershipStartTextBox = "Ownership Start Date";
	

	@FindBy(xpath = "//p[text()='Primary Situs']/../..//force-hoverable-link")
	public WebElement linkPrimarySitus;

	@FindBy(xpath = "//li[not(contains(@style,'visibility: hidden'))]//*[@title='More Tabs']")
	public WebElement moretab;

	@FindBy(xpath = "//*[@role='menuitem' and contains(.,'Exemptions')]")
	public WebElement exemptionRelatedList;

	@FindBy(xpath = "//div[contains(@class,'flowruntime-input-error')]//span")
	public WebElement descriptionError;

	@FindBy(xpath = "//div[contains(@class,'ErrorText')]")
	public WebElement workItemTypeAndSubTypeError;

	@FindBy (xpath= "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container')]//li[1]//button[@title='Toggle details for work item']")
	public WebElement ExpendWIOnParcels;
	
	@FindBy(xpath = "//button[contains(text(),'Advanced')]")
	public WebElement advancedButton;
	
	@FindBy(xpath = "//*[contains(text(),'Proceed to')]")
	public WebElement proceedButton;
	
	@FindBy(xpath = "//button[contains(text(),'Open Assessor')]")
	public WebElement openAsessorsMapButton;
	
	
	
	
    public String SubmittedForApprovalButton="Submit for Approval";
	
    public String WithdrawButton="Withdraw";
	
    public String ApprovalButton="Approve";
	/**
	 * Description: This method will open the parcel with the APN passed in the
	 * parameter
	 * 
	 * @param APN: Value in the APN column
	 */
	public void openParcel(String APN) throws IOException, InterruptedException {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening the parcel with APN : " + APN);
		Click(driver.findElement(By.xpath("//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//a[@title='" + APN + "']")));
		Thread.sleep(2000);
	}
	/**
	 * @Description: This method will fill all the fields in manual work item modal and create a work item
	 * @param dataMap: A data map which contains data to create work item
	 * @throws Exception
	 */
	public String createWorkItem(Map<String, String> dataMap) throws Exception {
		String timeStamp = String.valueOf(System.currentTimeMillis());
		String workItemType = dataMap.get("Work Item Type");
		String actions = dataMap.get("Actions");
		String reference = dataMap.get("Reference");
		String description = dataMap.get("Description") + "_" + timeStamp;
		String priority = dataMap.get("Priority");
		String workItemRouting = dataMap.get("Work Item Routing");
		String dueDate = dataMap.get("Due Date");
		String dov = dataMap.get("DOV");
		String workItemOwner= dataMap.get("Work Item Owner");
		String workItemNumber;
		
		waitForElementToBeClickable(getButtonWithText(componentActionsButtonText));
		Click(getButtonWithText(componentActionsButtonText));
		waitForElementToBeClickable(selectOptionDropDownComponentsActionsModal);
		selectOptionFromDropDown(selectOptionDropDownComponentsActionsModal, "Create Work Item");
		Click(getButtonWithText(nextButtonComponentsActionsModal));
		waitForElementToBeClickable(workItemTypeDropDownComponentsActionsModal);
		
		selectOptionFromDropDown(workItemTypeDropDownComponentsActionsModal, workItemType);
		selectOptionFromDropDown(actionsDropDownLabel, actions);

		if (reference != null)enter(referenceInputTextBoxComponentActionModal, reference);
		enter(descriptionInputTextBoxComponentActionModal, description);
		selectOptionFromDropDown(priorityDropDownComponentsActionsModal, priority);
		selectOptionFromDropDown(workItemRoutingDropDownComponentsActionsModal, workItemRouting);
		if (dueDate != null) enter(dueDateInputTextBox, dueDate);
		if (dov != null) enter(dovInputTextBox, dov);
		if (workItemOwner != null) searchAndSelectOptionFromDropDown(workItemOwnerSearchBox,workItemOwner);
		Click(getButtonWithText(nextButtonComponentsActionsModal));
		Thread.sleep(2000);

		String workItemQuery = "SELECT Name FROM Work_Item__c where Description__c = '" + description + "' order by Name desc limit 1";
		workItemNumber = objSalesforceAPI.select(workItemQuery).get("Name").get(0);
		ReportLogger.INFO("Work item created is " + workItemNumber  );
		
		return workItemNumber;
	}
	 
    
    /**
	 * Description: This method will save the table data in hashmap for the Target/Source Parcel relationship
	 *
	 * @param rowNumber: Row Number for which data needs to be fetched
	 * @return hashMap: Grid data in hashmap of type HashMap<String,ArrayList<String>>
	 */
    public HashMap<String, ArrayList<String>> getParcelTableDataInHashMap(String tableName) {
		return getParcelTableDataInHashMap(tableName, -1);
	}
    
	public HashMap<String, ArrayList<String>> getParcelTableDataInHashMap(String tableName, int rowNumber) {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Fetching the data from the currently displayed grid");
		String xpathTable = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'flowruntimeBody')]//span[@title='" + tableName + "']//ancestor::lst-list-view-manager-header//following-sibling::div[@class='slds-grid listDisplays']//table";
		
		String xpathHeaders = xpathTable + "//thead/tr/th";
		String xpathRows = xpathTable + "//tbody/tr";
		if (!(rowNumber == -1)) xpathRows = xpathRows + "[" + rowNumber + "]";

		HashMap<String, ArrayList<String>> gridDataHashMap = new HashMap<>();

		//Fetching the headers and data web elements from application
		List<WebElement> webElementsHeaders = driver.findElements(By.xpath(xpathHeaders));
		List<WebElement> webElementsRows = driver.findElements(By.xpath(xpathRows));

		String key, value;

		//Converting the grid data into hashmap
		for (WebElement webElementRow : webElementsRows) {
			int yearAcquiredKeyCounter = 0;
			List<WebElement> webElementsCells = webElementRow.findElements(By.xpath(".//td | .//th"));
			for (int gridCellCount = 0; gridCellCount < webElementsHeaders.size(); gridCellCount++) {
				key = webElementsHeaders.get(gridCellCount).getAttribute("aria-label");
				//Year Acquired Column appears twice in Commercial and Industrial Composite Factors table
				//Below code will not add column in hashmap appearing twice
				if(key != null && key.equalsIgnoreCase("Year Acquired")) {
					if(yearAcquiredKeyCounter<1) {
						yearAcquiredKeyCounter = yearAcquiredKeyCounter + 1;
					}else
						key=null;
				}

				if (key != null) {
					//"replace("Edit "+ key,"").trim()" code is user to remove the text \nEdit as few cells have edit button and the text of edit button is also returned with getText()
					value = webElementsCells.get(gridCellCount).getText();
					String[] splitValues = value.split("Edit " + key);
					if (splitValues.length > 0) value = splitValues[0].trim();
					else value = "";
					gridDataHashMap.computeIfAbsent(key, k -> new ArrayList<>());
					gridDataHashMap.get(key).add(value);
				}
			}
		}

		//Removing the Row Number key as this is meta data column and not part of grid
		gridDataHashMap.remove("Row Number");
		System.out.println("HashMap: "+gridDataHashMap);
		return gridDataHashMap;
	}
	
	/**
	 * Description: This method will open the parcel Related TAB Name passed in the
	 * parameter
	 * @param tabName: Value in the APN column
	 * @throws Exception 
	 */
	public void openParcelRelatedTab(String tabName) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Opening the parcel Related List Tab : " + tabName);
		String xPath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'modal-container') or contains(@class,'flowruntimeBody')]//*[text()='" + tabName + "']";
		if(verifyElementVisible(xPath)) {
			Click(getButtonWithText(tabName));
		}
		else {
			Click(moretab);
			Click(driver.findElement(By.xpath(xPath)));
		}
		Thread.sleep(2000);
	}
	
	/**
	 * @Description: This method will create Ownership record
	 * @param dataMap: A data map which contains data to perform create Ownership record
	 * @throws Exception
	 */
	public String createOwnershipRecord(String assesseeName, Map<String, String> dataMap) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating Ownership Record");        
		String owner = assesseeName;
		String type = dataMap.get("Type");
		String status = dataMap.get("Status");
		String bppAccount = dataMap.get("BPP Account");
		String ownershipStartDate = dataMap.get("Ownership Start Date");
		
		createRecord();
		searchAndSelectOptionFromDropDown(ownerDropDown, owner);
		selectOptionFromDropDown(typeDropDown, type);
		selectOptionFromDropDown(statusDropDown, status);
		if (ownershipStartDate != null)
			enter(ownershipStartTextBox, ownershipStartDate);
		if (bppAccount != null)
			searchAndSelectOptionFromDropDown(bppAccountDropDown, bppAccount);
		
		String successMsg = saveRecord();
		return successMsg;
	}

	/*
	   This method is used to fetch field value for mentioned APN
	   @Param: fieldName: Field name for which value needs to be fetched
	   @Param: apnNumber: Parcel Number for which field value needs to be fetched
	   @return: returns the value of the field
	  */
		public HashMap<String, ArrayList<String>> fetchFieldValueOfParcel(String fieldName, String apnNumber) throws Exception {
			String query = "SELECT "+fieldName+" FROM Parcel__c where Name = '"+apnNumber+"'";
			HashMap<String, ArrayList<String>> response = objSalesforceAPI.select(query);

			if(fieldName.equalsIgnoreCase("Neighborhood_Reference__c")){
				query = "SELECT Name FROM Neighborhood__c Where Id = '"+response.get(fieldName).get(0)+"'";
			}else if(fieldName.equalsIgnoreCase("TRA__c")){
				query = "SELECT Name FROM TRA__c Where Id = '"+response.get(fieldName).get(0)+"'";
			}else if(fieldName.equalsIgnoreCase("Primary_Situs__c")){
				query = "SELECT Name FROM Situs__c Where Id = '"+response.get(fieldName).get(0)+"'";
			}
			response = objSalesforceAPI.select(query);		
			return response;
		}

}