package com.apas.Reports;

import com.relevantcodes.extentreports.LogStatus;

public class ReportLogger {
    public static void PASS(String message) {
        ExtentTestManager.getTest().log(LogStatus.PASS, message);
        System.out.println(message);
    }

    public static void FAIL(String message) {
        ExtentTestManager.getTest().log(LogStatus.FAIL,  message);
        System.out.println(message);
    }

    public static void INFO(String message) {
        ExtentTestManager.getTest().log(LogStatus.INFO, message);
        System.out.println(message);
    }

    public static void ERROR(String message) {
        ExtentTestManager.getTest().log(LogStatus.ERROR, message);
        System.out.println(message);
    }

    public static void SKIP(String message) {
        ExtentTestManager.getTest().log(LogStatus.SKIP, message);
        System.out.println(message);
    }
}
