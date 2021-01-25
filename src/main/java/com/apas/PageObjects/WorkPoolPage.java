package com.apas.PageObjects;

import com.apas.Reports.ReportLogger;
import com.apas.Utils.SalesforceAPI;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.PageFactory;

public class WorkPoolPage extends ApasGenericPage {

    public WorkPoolPage(RemoteWebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public String wpSupervisor = "Supervisor";
    public String wpLevel2Supervisor = "Level2 Supervisor";
    public String wpLevel2ValueCriteriaSupervisor = "Level2 Value Criteria";
    public String wpWorkPoolName = "Work Pool Name";
    public String buttonRemoveStaff = "Remove Staff";

    /**
     * This method will create a WorkPool
     * @param poolName :Work Pool Name
     * @param supervisorName :Supervisor Name
     **/

    public String createWorkPool(String poolName, String supervisorName) throws Exception {
        return createWorkPool(poolName, supervisorName, "", "");
    }

    /**
     * This method will create a WorkPool
     * @param poolName :Work Pool Name
     * @param supervisorName :Supervisor Name
     * @param level2SupervisorName :Level 2 Supervisor Name
     * @param level2ValueCriteria :Level 2 Value Criteria
     **/

    public String createWorkPool(String poolName, String supervisorName, String level2SupervisorName, String level2ValueCriteria) throws Exception {
        ReportLogger.INFO("Create a Work Pool record :: " + poolName);
        Click(newButton);
        enter(wpWorkPoolName, poolName);
        searchAndSelectOptionFromDropDown(wpSupervisor, supervisorName);
        if(!level2SupervisorName.equals(""))searchAndSelectOptionFromDropDown(wpLevel2Supervisor, level2SupervisorName);
        if(!level2ValueCriteria.equals(""))enter(wpLevel2ValueCriteriaSupervisor, level2ValueCriteria);
        return saveRecord();
    }


}
