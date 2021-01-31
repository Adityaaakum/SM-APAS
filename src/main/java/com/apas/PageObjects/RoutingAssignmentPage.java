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

public class RoutingAssignmentPage extends ApasGenericPage{

    public RoutingAssignmentPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
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
		Click(getButtonWithText(NewButton));
		enterRoutingAssignmentRecordDetails(routingAssignmentReferenceData);
		String successMessage = saveRecord();
		return successMessage;
	}
	/**
	 * This method will Create Routing Assignment Record
	 *
	 * @param routingAssignmentReferenceData : WorkItem Configuration
	 **/
	public void enterRoutingAssignmentRecordDetails(Map<String, String> routingAssignmentReferenceData) throws Exception{

		if(routingAssignmentReferenceData.get("Work Item Configuration")!=null) 
			searchAndSelectOptionFromDropDown(workItemConfigurationDropDown,routingAssignmentReferenceData.get("Work Item Configuration"));

		if(routingAssignmentReferenceData.get("Work Pool")!=null)
			searchAndSelectOptionFromDropDown(workPoolDropDown,routingAssignmentReferenceData.get("Work Pool"));

		if(routingAssignmentReferenceData.get("RAType").equals("BPP") && routingAssignmentReferenceData.get("Territory")!=null)
			searchAndSelectOptionFromDropDown(territoryDropDown,routingAssignmentReferenceData.get("Territory"));

		if(routingAssignmentReferenceData.get("RAType").equals("RP") && routingAssignmentReferenceData.get("Neighborhood")!=null)
			searchAndSelectOptionFromDropDown(neighborhoodDropDown,routingAssignmentReferenceData.get("Neighborhood"));

	}
}


