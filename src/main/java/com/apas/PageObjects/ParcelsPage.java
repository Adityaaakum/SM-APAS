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
	public String auditTrailRecordDropDownComponentsActionsModal = "Is this Audit Trail Record linked to any Existing Audit Trail Record?";

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
	public String createNewParcelButton="New";
	public String editParcelButton="Edit";
	public String parcelCharacteristics = "Characteristics";
	public String parcel = "Parcel";
	
	
	
	/** Added to identify fields, dropdowns for CIO functionality **/
	public String exemptionTypeLabel="Exemption Type(s)";
	public String exemptionLabel="Exemption";
	public String exemptionRelatedTab="Exemptions";
	public String saveParcelButton="Save";
	public String recordTypeDropdown = "Record Type";

	public String group="Group";	

	public String typeOfAuditTrailDropdown = "Type of Audit Trail Record?";
	public String sourceDropdown = "Source";
	public String dateOfEventInputTextBox = "Date of Event";
	public String dateOfValueInputTextBox = "Date of Value";
	public String dateOfRecordingInputTextBox = "Date of Recording";
	public String descriptionInputTextBox = "Description";
	public String saveAndNextButton="Save and Next";
	public String ownershipPercentageTextBox="Ownership Percentage";
	
	public String parcelNumber = "Parcel Number";
	public String puc = "PUC";
	
	public String parcelSitus = "Parcel Situs";
	public String newParcelSitus="New Parcel Situs";
	public String isPrimaryDropdown = "Is Primary?";
	public String situsSearch = "Situs";
	
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
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text()='Next']")
	public WebElement ownershipNextButton;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'slds-listbox__option_plain') or contains(@class,'flowruntimeBody')]//select[@name=\"Component_Action_Options\"]")
	public WebElement selectOptionDropdown;
	
	@FindBy(xpath = "//a[starts-with(@title,\"RD-APN\")][@class='tabHeader slds-context-bar__label-action '][@aria-selected='true']//span[@class='title slds-truncate']")
	public WebElement recordedDocumentApnGenerated;
	
	@FindBy(xpath = "//div[contains(@class, 'notesEditPanel')]/div//input[contains(@class, 'notesTitle')]")
	public WebElement notes;
	
	@FindBy(xpath = "//span[contains(.,'Upload Files')]")
    public WebElement uploadFilesButton;
	
	@FindBy(xpath = "//input[contains(@id,'input-file')]")
    public WebElement uploadFileInputBox;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//button[text()='Save']")
    public WebElement ownershipSaveButton;
	
	@FindBy(xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized')]//span[text() = 'Mail-To']/following-sibling::span")
	public WebElement numberOfMailToOnParcelLabel;
	
	@FindBy(xpath = "//span[text() = 'View All']")
	public WebElement viewAll;
	
	@FindBy(xpath = "//span[@title='Target Parcel Relationships']")
	public WebElement targetParcelLabel;
	
	
    public String SubmittedForApprovalButton="Submit for Approval";
    public String WithdrawButton="Withdraw";
    public String ApprovalButton="Approve";
    public String auditTrailElementPath="//label[text() = 'Is this Audit Trail Record linked to any Existing Audit Trail Record?']";
    
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
		String auditTrail = dataMap.get("Audit Trail");
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
		//waitForElementToBeClickable(selectOptionDropDownComponentsActionsModal);
		//selectOptionFromDropDown(selectOptionDropDownComponentsActionsModal, "Create Work Item");
		Click(getButtonWithText(nextButtonComponentsActionsModal));
		waitForElementToBeClickable(workItemTypeDropDownComponentsActionsModal);
		
		selectOptionFromDropDown(workItemTypeDropDownComponentsActionsModal, workItemType);
		selectOptionFromDropDown(actionsDropDownLabel, actions);
	
		if(verifyElementExists(auditTrailElementPath)) selectOptionFromDropDown(auditTrailRecordDropDownComponentsActionsModal, auditTrail);

		if (reference != null)enter(referenceInputTextBoxComponentActionModal, reference);
		enter(descriptionInputTextBoxComponentActionModal, description);
		//selectOptionFromDropDown(priorityDropDownComponentsActionsModal, priority);
		//selectOptionFromDropDown(workItemRoutingDropDownComponentsActionsModal, workItemRouting);
		if (dueDate != null) enter(dueDateInputTextBox, dueDate);
		if (dov != null) enter(dovInputTextBox, dov);
		if (workItemOwner != null) searchAndSelectOptionFromDropDown(workItemOwnerSearchBox,workItemOwner);
		Click(getButtonWithText(nextButtonComponentsActionsModal));
		Thread.sleep(5000);

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
		Thread.sleep(5000);
	}
	
	/**
	 * @Description: This method will create Ownership record
	 * @param dataMap: A data map which contains data to perform create Ownership record
	 * @throws Exception
	 */
	public String createOwnershipRecord(String apn, String assesseeName, Map<String, String> dataMap) throws Exception {	
		globalSearchRecords(apn);
        openParcelRelatedTab(ownershipTabLabel);
        scrollToBottom();
        Thread.sleep(1000);
        
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating Ownership Record");        
		String owner = assesseeName;
		String type = dataMap.get("Type");
		String status = dataMap.get("Status");
		String bppAccount = dataMap.get("BPP Account");
		String ownershipStartDate = dataMap.get("Ownership Start Date");
		
		createRecord();
		Click(ownershipNextButton);
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
	
	public String createOwnershipRecord(String assesseeName, Map<String, String> dataMap) throws Exception {
		ExtentTestManager.getTest().log(LogStatus.INFO, "Creating Ownership Record");        
		String owner = assesseeName;
		String type = dataMap.get("Type");
		String status = dataMap.get("Status");
		String bppAccount = dataMap.get("BPP Account");
		String ownershipStartDate = dataMap.get("Ownership Start Date");
		String ownershipPercentage=dataMap.get("Ownership Percentage");
		
		scrollToBottom();
		createRecord();
		Click(ownershipNextButton);
		searchAndSelectOptionFromDropDown(ownerDropDown, owner);
		selectOptionFromDropDown(typeDropDown, type);
		selectOptionFromDropDown(statusDropDown, status);
		if (ownershipStartDate != null)
			enter(ownershipStartTextBox, ownershipStartDate);
		if (bppAccount != null)
			searchAndSelectOptionFromDropDown(bppAccountDropDown, bppAccount);
		if(ownershipPercentage!= null)
			enter(ownershipPercentageTextBox, ownershipPercentage);
		
		Click(ownershipSaveButton);
        waitForElementToBeClickable(successAlert,25);
        String messageOnAlert = getElementText(successAlert);
        waitForElementToDisappear(successAlert,10);
		return messageOnAlert;
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
		
		/**
		 * @Description: This method will fill all the fields in Adit Trail record to create unrecorded event
		 * @param dataMap: A data map which contains data to create audit trail record
		 * @throws Exception
		 */
		public String createUnrecordedEvent(Map<String, String> dataMap) throws Exception {
			ReportLogger.INFO("Create Unrecorded Event Transfer");
			String timeStamp = String.valueOf(System.currentTimeMillis());
			String description = dataMap.get("Description") + "_" + timeStamp;
			
			waitForElementToBeClickable(getButtonWithText(componentActionsButtonText));
			Click(getButtonWithText(componentActionsButtonText));
			waitForElementToBeClickable(selectOptionDropdown);
			selectOptionFromDropDown(selectOptionDropdown, "Create Audit Trail Record");
			Click(getButtonWithText(nextButtonComponentsActionsModal));
			waitForElementToBeClickable(workItemTypeDropDownComponentsActionsModal);
			
			selectOptionFromDropDown(recordTypeDropdown, dataMap.get("Record Type"));

			selectOptionFromDropDown(group,dataMap.get("Group"));

			

			Thread.sleep(2000);
			selectOptionFromDropDown(typeOfAuditTrailDropdown, dataMap.get("Type of Audit Trail Record?"));
			if(dataMap.get("Source")!=null) {selectOptionFromDropDown(sourceDropdown, dataMap.get("Source"));}
			if(dataMap.get("Date of Event")!=null) {enter(dateOfEventInputTextBox, dataMap.get("Date of Event"));}
			if(dataMap.get("Date of Value")!=null) {enter(dateOfValueInputTextBox, dataMap.get("Date of Value"));}
			enter(dateOfRecordingInputTextBox, dataMap.get("Date of Recording"));
			enter(descriptionInputTextBox, description);
			Click(getButtonWithText(saveAndNextButton));
			Thread.sleep(5000);
			if(dataMap.get("Record Type").equalsIgnoreCase("Correspondence")) {return null;}				
			
			return getElementText(recordedDocumentApnGenerated);
		}		
		
		//To create new parcel manually
		public String createNewParcel(String apn,String parcelNum,String PUC) {
	        String querySearchAPN = "Select id from Parcel__c where name ='"+apn+"'";
		    HashMap<String, ArrayList<String>> responseSearchedAPN = objSalesforceAPI.select(querySearchAPN);
		    
		    String querySearchApnAgain = "Select name,id from Parcel__c where name ='"+apn+"' and Id NOT IN (SELECT APN__c FROM Work_Item__c where type__c='CIO') limit 1";
		    HashMap<String, ArrayList<String>> responseSearchedApnAgain = objSalesforceAPI.select(querySearchApnAgain);
		    
		    if(responseSearchedAPN.isEmpty()) {
		    	createParcel(apn,parcelNum,PUC);
		    }else if (responseSearchedApnAgain.isEmpty()) {
		    	objSalesforceAPI.delete("Parcel__c",querySearchAPN);
		    	createParcel(apn,parcelNum,PUC);
		    }else {
		    	ReportLogger.INFO("Parcel record already present in system : "+apn);
		    }
		    return apn;
		}
		
		public void createParcel(String apn,String parcelNum,String PUC) {
			try {
	    		waitForElementToBeInVisible(createNewParcelButton, 10);
	    		Click(getButtonWithText(createNewParcelButton));
	    	    enter(editApnField,apn);
	    		enter(parcelNumber,parcelNum);
	    		selectOptionFromDropDown(puc,PUC);
	    		Click(saveButton);
	    		ReportLogger.INFO("Successfully created parcel record : "+apn);
		    	}
		    catch(Exception e) {
	        		ReportLogger.INFO("Fail to create parcel record : "+e);
	        	}
		}
		
		
		/**
		 * @Description: This method will return the list of the characteristics present
		 * @return list of web elements
		 */
		public List<WebElement> fetchCharacteristicsList() {
			String xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'flowruntimeBody')]//table/tbody//tr/th//div//div/a";
			List<WebElement> webElementsHeaders = driver.findElements(By.xpath(xpath));
			return webElementsHeaders;
		}
		
		/**
		 * @Description: This method will return the list of notes attached
		 * @param absoluteName: name of the note in the list
		 * @return web element
		 */
		public WebElement sidePanelNotesList(String noteName) {
			String xpath = "//li[contains(@class, 'notesContentNoteRelatedListStencil ')]/a/div/div/h2/span[text()='"
					+ noteName + "']";
			return driver.findElement(By.xpath(xpath));
		}
		/**
		 * @Description: This method will return elements on side panel 
		 * @param absoluteName: name of the element 
		 * @return web element
		 */		
		public WebElement getButtonWithTextForSidePanels(String name) {
			String xpath = "//span[text()='" + name + "']";
			return driver.findElement(By.xpath(xpath));
		}
		
		/**
		 * @Description: This method will return the pop up that comes for confirmation 
		 * @param absoluteName: name of the element on pop up dialog
		 * @return web element
		 */
		public WebElement getPopUpconfirmation(String name) {
			String xpath = "//div[contains(@class, 'modal-container slds-modal__container')]//div//span[text()='Delete']";
			return driver.findElement(By.xpath(xpath));
		}

		/**
		 * @Description: This method will return of attachments in side panel 
		 * @param absoluteName: Attachment name
		 * @return Web Element
		 */
		public WebElement sideOptionsAttachmentList(String attachmentName) {
			String xpath = "//div[contains(@class, 'filerow')]/div/div/span[text()='" + attachmentName + "']";
			return driver.findElement(By.xpath(xpath));
		}
		
		/**
		 * @Description: This method will upload the file form the given path
		 * @param absolutePath: Path of the file location from where file has to be uploaded
		 */
		public void uploadFile(String absoluteFilePath) throws Exception {
			waitForElementToBeClickable(uploadFilesButton, 120);
			uploadFileInputBox.sendKeys(absoluteFilePath);
			Thread.sleep(2000);
			waitForElementToBeClickable(getButtonWithText("Done"));
			Click(getButtonWithText("Done"));
			Thread.sleep(2000);
		}
		
		/**
		 * @Description: This method will return the list of the characteristics present
		 * @return list of web elements
		 */
		public List<WebElement> fetchAllCreatedChar() {
			String xpath = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'flowruntimeBody')]//table/tbody//tr/th//a\r\n"
					+ "";
			List<WebElement> webElementsHeaders = driver.findElements(By.xpath(xpath));
			return webElementsHeaders;
		}
		
		/**
		 * @Description: This method will return the list of the characteristics present
		 * @return list of web elements
		 */
		public List<WebElement> charDropdown() {
			String xpath="//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'flowruntimeBody')]//table//tr//td//span//div//a[@role='button']";
			List<WebElement> webElementsHeaders = driver.findElements(By.xpath(xpath));
			return webElementsHeaders;
		}
		
		/**
		 * @Description: This method will create primary on parcel 
		 * 
		 */
		public String createParcelSitus( String APN) throws Exception {
			
			String excEnv= System.getProperty("region");
			ExtentTestManager.getTest().log(LogStatus.INFO, "Creating Parcel Situs Record");        
			 
			deleteParcelSitusFromParcel(APN);
			

		    driver.navigate().to("https://smcacre--"+excEnv+".lightning.force.com/lightning/o/Parcel_Situs__c/new?count=1");
		    waitForElementToBeVisible(10,isPrimaryDropdown);
			selectOptionFromDropDown(isPrimaryDropdown, "Yes");
			selectOptionFromDropDown(parcel, APN);
			searchAndSelectOptionFromDropDown(situsSearch, objSalesforceAPI.select("Select Name from Situs__c where name != null limit 1").get("Name").get(0));
			Click(saveButton);
			waitForElementToBeClickable(successAlert,25);
			String messageOnAlert = getElementText(successAlert);
			waitForElementToDisappear(successAlert,10);
			ReportLogger.INFO("Primary Situs created on parcel : "+messageOnAlert);
			String situsCreated = getFieldValueFromAPAS("Situs","");
			ReportLogger.INFO("Primary Situs created on parcel : "+APN);
			globalSearchRecords(APN);
			Thread.sleep(5000);
			return situsCreated;	
		}
		
		
}