package com.apas.PageObjects;

import com.apas.Utils.SalesforceAPI;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class PoolAssignmentPage extends ApasGenericPage {

    SalesforceAPI salesforceAPI = new SalesforceAPI();

    public PoolAssignmentPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public String dropDownRole = "Role";
    public String lookUpUser = "User";
    public String lookUpNeighborhood = "Neighborhood";
    public String errorWarningMessage = "//div[@class= 'error warning']";

    /**
     * This method will Create Pool Assignment Record
     * @param user : user details
     * @param role : roles details
     * @param neighborhood : Neighborhood details
     **/
    public String createPoolAssignment(String user, String role, String neighborhood) throws Exception{
        createRecord();
        enterPoolAssignmentDetails(user, role, neighborhood);
        return saveRecord();
    }

    /**
     * This method will enter the details on  Pool Assignment Record screen
     * @param user : user details
     * @param role : roles details
     * @param neighborhood : Neighborhood details
     **/
    public void enterPoolAssignmentDetails(String user, String role, String neighborhood) throws Exception{
        selectOptionFromDropDown(dropDownRole, role);
        searchAndSelectOptionFromDropDown(lookUpUser,user);
        if (!neighborhood.isEmpty()) searchAndSelectOptionFromDropDown(lookUpNeighborhood, neighborhood);
    }
}
