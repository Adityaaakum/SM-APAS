package com.apas.Tests;

import com.apas.Utils.ExcelUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class TestCode {
    public static void main (String[] args) throws Exception {
        System.out.println("Hello World");
        String filepath = "C:\\Users\\nikjain2\\Downloads\\Building Permit by City Code-2020-06-23-22-13-21.xlsx";
        HashMap<String, ArrayList<String>> abv =  ExcelUtils.getExcelSheetData(filepath,"1");
        System.out.println(abv.keySet());
    }
}
