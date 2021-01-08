package com.apas.PageObjects;

import com.apas.Utils.SalesforceAPI;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class WorkPoolPage extends ApasGenericPage {

    public WorkPoolPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public String buttonRemoveStaff = "Remove Staff";

}
