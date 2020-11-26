package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;
import com.apas.generic.ApasGenericFunctions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
//import sun.applet.AppletSecurity;

public class CityStratCodesPage extends ApasGenericPage {

	ApasGenericPage objApasGenericPage;
	BuildingPermitPage objBuildingPermitPage;
	ApasGenericFunctions objApasGenericFunctions;

	public CityStratCodesPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
		objApasGenericPage = new ApasGenericPage(driver);
		objBuildingPermitPage = new BuildingPermitPage(driver);
		objApasGenericFunctions = new ApasGenericFunctions(driver);
	}

	public String countyStratCodeEditBox = "County Strat Code";
	public String cityCodeDropDown = "City Code";
	public String cityStratCodeEditBox = "City Strat Code";
	public String statusDropDown = "Status";

	/**
	 * Description: This method will only enter the values of city strat code fiels on the application
	 * @param countyStratCode: County Strat Code
	 * @param cityCode: City Code
	 * @param cityStratCode: City Strat Code
	 * @param status: Status
	 */
	public void enterCityStratCodeDetails(String countyStratCode, String cityCode, String cityStratCode, String status) throws Exception {
		objApasGenericPage.searchAndSelectOptionFromDropDown(countyStratCodeEditBox,countyStratCode);
		objApasGenericPage.selectOptionFromDropDown(cityCodeDropDown,cityCode);
		enter(cityStratCodeEditBox,cityStratCode);
		objApasGenericPage.selectOptionFromDropDown(statusDropDown,status);
	}

	/**
	 * Description: This method will add a new city strat code
	 * @param countyStratCode: County Strat Code
	 * @param cityCode: City Code
	 * @param cityStratCode: City Strat Code
	 * @param status: Status
	 * @return : returns the text message of success alert
	 */
	public String addAndSaveCityStratcode(String countyStratCode, String cityCode, String cityStratCode, String status) throws Exception {
		Click(newButton);
		waitForElementToBeClickable(10,countyStratCodeEditBox);
		enterCityStratCodeDetails(countyStratCode,cityCode,cityStratCode,status);
		return objApasGenericFunctions.saveRecord();
	}

	/**
	 * Description: This method will open the new entry pop up
	 */
	public void openNewEntry() throws Exception {
		ReportLogger.INFO("Opening the City Strat code Entry Form");
		Click(newButton);
		waitForElementToBeVisible(10,cityStratCodeEditBox);
	}

}
