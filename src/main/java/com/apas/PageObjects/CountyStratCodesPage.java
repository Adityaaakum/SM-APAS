package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

import java.util.Map;

public class CountyStratCodesPage extends ApasGenericPage {
	
	public CountyStratCodesPage(RemoteWebDriver driver) {
		super(driver);
		PageFactory.initElements(driver, this);
	}

	public String processingStatusDrpDown = "Processing Status";
	public String stratCodeRefNumInputFiled = "Strat Code Reference Number";
	public String stratCodeDescInputField = "Strat Code Description";
	public String permitValueOperatorDropDown = "Permit Value Operator";
	public String statusDropDown = "Status";
	public String permitValueLimit = "Permit Value Limit";

	/**
	 * Description: This method will open the new entry pop up
	 */
	public void openNewEntry() throws Exception {
		ReportLogger.INFO("Opening the City Strat code Entry Form");
		Click(newButton);
		waitForElementToBeClickable(stratCodeRefNumInputFiled, 10);
	}

	/**
	 * Description: This method enter values in given fields in new entry pop up
	 * @param dataMap: Data map containing keys as field names and values as their values
	 */
	public void enterCountyStratCodeDetails(Map<String, String> dataMap) throws Exception {
		enter(stratCodeRefNumInputFiled, dataMap.get("Strat Code Reference Number"));
		enter(stratCodeDescInputField, dataMap.get("Strat Code Description"));
		selectOptionFromDropDown(statusDropDown, dataMap.get("Status"));
		selectOptionFromDropDown(processingStatusDrpDown, dataMap.get("Processing Status"));
	}

	/**
	 * Description: This method will add a new city strat code
	 * @param dataMap: Data map containing keys as field names and values as their values
	 * @return String: returns the text message of success alert
	 */
	public String addAndSaveCountyStratCode(Map<String, String> dataMap) throws Exception {
		openNewEntry();
		Thread.sleep(1000);
		enterCountyStratCodeDetails(dataMap);
		return saveRecord();
	}

}
