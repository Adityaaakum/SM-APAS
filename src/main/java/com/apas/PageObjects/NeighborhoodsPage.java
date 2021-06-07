package com.apas.PageObjects;

import java.util.Map;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;
import com.apas.Utils.SalesforceAPI;
import com.apas.PageObjects.ApasGenericPage;

public class NeighborhoodsPage extends ApasGenericPage {
    ApasGenericPage objApasGenericPage;
    Page objPageObj;
    SalesforceAPI salesforceAPI ;

    public NeighborhoodsPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
        objApasGenericPage = new ApasGenericPage(driver);
        objPageObj=new Page(driver);
    }

    public String districtNeighborhoodCodeEditBox = "District / Neighborhood Code";
    public String neighborhoodCodeEditBox="Neighborhood Code";
    public String primaryAppraiserDropDown="Primary Appraiser";
    public String districtDropDown="District";
    public String neighborhoodDescriptionEditBox="Neighborhood Description";
    public String neighborhoodDropDown= "Neighborhood";
    public String districtDescriptionEditBox="District Description";

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
     * @param neighborhoodReferenceData : Neighborhood Reference record Details
     * @throws Exception
     **/
    public void enterNeighborhoodReferenceRecordDetails(Map<String, String> neighborhoodReferenceData) throws Exception{
        enter(districtNeighborhoodCodeEditBox,neighborhoodReferenceData.get("District / Neighborhood Code"));
        enter(neighborhoodCodeEditBox,neighborhoodReferenceData.get("Neighborhood Code"));
        enter(neighborhoodDescriptionEditBox,neighborhoodReferenceData.get("Neighborhood Description"));
        objApasGenericPage.searchAndSelectOptionFromDropDown(primaryAppraiserDropDown,neighborhoodReferenceData.get("Primary Appraiser"));
        objApasGenericPage.selectOptionFromDropDown(districtDropDown,neighborhoodReferenceData.get("District"));
        if(neighborhoodReferenceData.get("District Description")!=null)
            enter(districtDescriptionEditBox,neighborhoodReferenceData.get("District Description"));
    }
}
