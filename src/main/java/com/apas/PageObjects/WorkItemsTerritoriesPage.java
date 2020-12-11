package com.apas.PageObjects;
import java.util.Map;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import com.apas.Utils.SalesforceAPI;
import com.apas.PageObjects.ApasGenericPage;

public class WorkItemsTerritoriesPage extends ApasGenericPage {
    ApasGenericPage objApasGenericPage;
    Page objPageObj;
    SalesforceAPI salesforceAPI ;

    public WorkItemsTerritoriesPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
        objApasGenericPage = new ApasGenericPage(driver);
        objPageObj=new Page(driver);
    }
    public String territoryNameEditBox="Territory Name";
    public String primaryAuditorDropDown="Primary Auditor";
    public String territoryDropDown= "Territory";
    public String territoryDescriptionEditBox="Territory Description";

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

}
