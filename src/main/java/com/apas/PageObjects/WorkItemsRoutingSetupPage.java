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
    public String neighborhoodCodeEditBox="Neighborhood Code";
    public String neighborhoodDescriptionEditBox="Neighborhood Description";
    public String primaryAppraiserDropDown="Primary Appraiser";
    public String districtDropDown="District";
    public String territoryNameEditBox="Territory Name";
    public String primaryAuditorDropDown="Primary Auditor";
    public String workItemConfigurationDropDown= "Work Item Configuration";
    public String workPoolDropDown= "Work Pool";
    public String districtDescriptionEditBox="District Description";
    public String territoryDescriptionEditBox="Territory Description";
    public String neighborhoodDropDown= "Neighborhood";
    public String territoryDropDown= "Territory";

    /**
     * This method will Create Neighborhood Reference Record
     * @param neighborhoodReferenceData : Neighborhood Reference record Details
     * @throws Exception
     **/
    public String createNeighborhoodReferenceRecord(Map<String, String> neighborhoodReferenceData) throws Exception{
        Click(getButtonWithText(objApasGenericPage.NewButton));
        enterNeighborhoodReferenceRecordDetails(neighborhoodReferenceData);
        String successMessage = objApasGenericPage.saveRecord();
        return successMessage;
    }
    /**
     * This method will Create Neighborhood Reference Record
     *
     * @param neighborhoodCode : Neighborhood Code
     * @param neighborhoodDescription : Neighborhood Description
     * @param primaryAppraiser : Primary Appraiser
     * @param district : District
     * @param districtDescription : District Description
     * @throws Exception
     **/
    public void enterNeighborhoodReferenceRecordDetails(Map<String, String> neighborhoodReferenceData) throws Exception{
        enter(neighborhoodCodeEditBox,neighborhoodReferenceData.get("Neighborhood Code"));
        enter(neighborhoodDescriptionEditBox,neighborhoodReferenceData.get("Neighborhood Description"));
        objApasGenericPage.searchAndSelectOptionFromDropDown(primaryAppraiserDropDown,neighborhoodReferenceData.get("Primary Appraiser"));
        objApasGenericPage.selectOptionFromDropDown(districtDropDown,neighborhoodReferenceData.get("District"));
        if(neighborhoodReferenceData.get("District Description")!=null)
            enter(districtDescriptionEditBox,neighborhoodReferenceData.get("District Description"));
    }

    /**
     * This method will Create Territory Record
     * @param territoryReferenceData : Territory Reference record Details
     * @throws Exception
     **/
    public String createTerritoryRecord(Map<String, String> territoryReferenceData) throws Exception{
        Click(getButtonWithText(objApasGenericPage.NewButton));
        enterTerritoryRecordDetails(territoryReferenceData);
        String successMessage = objApasGenericPage.saveRecord();
        return successMessage;
    }
    /**
     * This method will Create Territory Record
     * @param territoryName : Territory Name
     * @param territoryDescription : Territory Description
     * @param primaryAuditor : Primary Auditor
     * @throws Exception
     **/
    public void enterTerritoryRecordDetails(Map<String, String> territoryReferenceData) throws Exception{
        enter(territoryNameEditBox,territoryReferenceData.get("Territory Name"));
        objApasGenericPage.searchAndSelectOptionFromDropDown(primaryAuditorDropDown,territoryReferenceData.get("Primary Auditor"));
        if(territoryReferenceData.get("Territory Description")!=null)
            enter(territoryDescriptionEditBox,territoryReferenceData.get("Territory Description"));
    }

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
        objApasGenericPage.searchAndSelectOptionFromDropDown(workItemConfigurationDropDown,routingAssignmentReferenceData.get("Work Item Configuration"));
        if(routingAssignmentReferenceData.get("Work Pool")!=null)
            objApasGenericPage.searchAndSelectOptionFromDropDown(workPoolDropDown,routingAssignmentReferenceData.get("Work Pool"));
        if(routingAssignmentReferenceData.get("BPP Roll Code")!=null) {
            if(routingAssignmentReferenceData.get("BPP Roll Code").equalsIgnoreCase("BPP")) {
                objApasGenericPage.searchAndSelectOptionFromDropDown(territoryDropDown,routingAssignmentReferenceData.get(""));
            }
            else
                objApasGenericPage.searchAndSelectOptionFromDropDown(neighborhoodDropDown,routingAssignmentReferenceData.get(""));
        }
    }

}
