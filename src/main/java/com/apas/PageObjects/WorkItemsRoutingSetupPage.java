package com.apas.PageObjects;
import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkItemsRoutingSetupPage extends ApasGenericPage{
	ApasGenericPage objApasGenericPage;
	Page objPageObj;
	SalesforceAPI salesforceAPI ;

	public WorkItemsRoutingSetupPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericPage = new ApasGenericPage(driver);
		objPageObj=new Page(driver);
	}

	public String workItemConfigurationDropDown= "Work Item Configuration";
	public String workPoolDropDown= "Work Pool";
	public String neighborhoodDropDown= "Neighborhood";
	public String territoryDropDown= "Territory";

	public String RP_BPP_RadioButton = "//div[contains(@class,'windowViewMode-normal') or contains(@class,'windowViewMode-maximized') or contains(@class,'slds-listbox__option_plain')]//span[contains(@class,'radio')]";

	/**
	 * This method will Create Routing Assignment Record
	 * @param routingAssignmentReferenceData : Routing AssignmentReferenceData record Details
	 * @throws Exception
	 **/
	public String createRoutingAssignmentRecord(Map<String, String> routingAssignmentReferenceData) throws Exception{
		Click(getButtonWithText(objApasGenericPage.NewButton));
		enterRoutingAssignmentRecordDetails(routingAssignmentReferenceData);
		String successMessage = objApasGenericPage.saveRecord();
		return successMessage;
	}
	/**
	 * This method will Create Routing Assignment Record
	 *
	 * @param workItemConfiguration : WorkItem Configuration
	 * @param workPool : Work Pool
	 * @param territoryORneighborhood : User will enter Territory or Neighborhood based on Roll Code
	 * @param BPPRollCode : True Roll Code is BPP else False if Roll Code is RP
	 * @throws Exception
	 **/
	public void enterRoutingAssignmentRecordDetails(Map<String, String> routingAssignmentReferenceData) throws Exception{

		if(routingAssignmentReferenceData.get("Work Item Configuration")!=null) 
			objApasGenericPage.searchAndSelectOptionFromDropDown(workItemConfigurationDropDown,routingAssignmentReferenceData.get("Work Item Configuration"));

		if(routingAssignmentReferenceData.get("Work Pool")!=null)
			objApasGenericPage.searchAndSelectOptionFromDropDown(workPoolDropDown,routingAssignmentReferenceData.get("Work Pool"));

		if(routingAssignmentReferenceData.get("RAType").equals("BPP") && routingAssignmentReferenceData.get("Territory")!=null)
			objApasGenericPage.searchAndSelectOptionFromDropDown(territoryDropDown,routingAssignmentReferenceData.get("Territory"));

		if(routingAssignmentReferenceData.get("RAType").equals("RP") && routingAssignmentReferenceData.get("Neighborhood")!=null)
			objApasGenericPage.searchAndSelectOptionFromDropDown(neighborhoodDropDown,routingAssignmentReferenceData.get("Neighborhood"));

	}
}


