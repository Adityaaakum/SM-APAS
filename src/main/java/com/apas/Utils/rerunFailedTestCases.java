package com.apas.Utils;

import com.apas.TestBase.TestBase;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class rerunFailedTestCases extends TestBase implements IRetryAnalyzer {

    private int retryCount = 0;
    int maximumRetryCount;

    //This method will be called every time a test fails. It will return TRUE if a test fails and need to be retried, else it returns FALSE
    public boolean retry(ITestResult result) {
        //You could mentioned maxRetryCnt (Maximiun Retry Count) as per your requirement. Here I took 2, If any failed testcases then it runs two times

        String retryCounter;
        if (System.getProperty("rerunFailedTestCaseCounter") != null)
            retryCounter = System.getProperty("rerunFailedTestCaseCounter");
        else if (CONFIG.getProperty("rerunFailedTestCaseCounter")!= null)
            retryCounter = CONFIG.getProperty("rerunFailedTestCaseCounter");
        else
            retryCounter = "0";

        maximumRetryCount = Integer.parseInt(retryCounter);

        if (retryCount < maximumRetryCount) {
            System.out.println("Retrying " + result.getName() + " again and the count is " + (retryCount + 1));
            retryCount++;
            return true;
        }
        return false;
    }

}
