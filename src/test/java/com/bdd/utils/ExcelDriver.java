package com.bdd.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Set;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import com.bdd.initialSetUp.TestBase;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;



public class ExcelDriver extends TestBase {

	static XSSFWorkbook workbook;

	public static String strBrowserVal;

	public static String urlval;
	public String strTestCaseName;

	
	/**
	 * Function will load the main driver(Controler) excel sheet
	 *
	 * @param excelsheetName
	 *            the Excel sheet name
	 * @throws FileNotFoundException
	 *             the file not found exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void readwrkbook(String excelsheetName) throws FileNotFoundException, IOException {
		try {
			workbook = new XSSFWorkbook(new FileInputStream(
					System.getProperty("user.dir") + "//" + "src//test//resources//" + excelsheetName + ".xlsx"));
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	/**
	 * Function wil return the worksheet instance.
	 *
	 * @param SheetName the worksheet name
	 * @return the XSSF sheet
	 */
	public XSSFSheet accesssheet(String SheetName) {
		XSSFSheet sheet = workbook.getSheet(SheetName);
		return sheet;
	}

	/**
	 * Function will read the excel workbook and return the map of Test to be
	 * executed
	 *
	 * @return the multi hash map
	 * @throws Exception
	 *             the exception
	 */
	public ListMultimap readExcelData(String suiteName) throws Exception {
		
		ListMultimap<String, String> mp = ArrayListMultimap.create();
		//MultiHashMap mp = new MultiHashMap();
		String a; String inp;
		List<String> Suites = new ArrayList<String>();
		List<String> strTestScripts = new ArrayList<String>();
		List<String> items;

		try {

			readwrkbook("TestSuite");
				XSSFSheet workSheet = accesssheet(suiteName);
				for (int i = 1; i < workSheet.getLastRowNum() + 1; i++) {
					inp = (workSheet.getRow(i).getCell(2)).toString();
					if (inp.equals("Y")) {
						strTestCaseName = (workSheet.getRow(i).getCell(1).toString());
						strTestScripts.add(strTestCaseName);
					}
				}
			for (String strValue : strTestScripts) {
				String[] parts = strValue.split("\\.");
				for (int i = 0; i < parts.length; i += 2) {
					mp.put(parts[i], parts[i + 1]);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		System.out.println(mp);
		return mp;

	}

	/**
	 * Function will return the Browser value.
	 *
	 * @return the browser name
	 */
	public static String getBrowserVal() {
		return strBrowserVal;
	}

	/**
	 * Function will return the environment url value.
	 *
	 * @return the url value
	 */
	public static String geturlval() {
		return urlval;
	}		
	
	public List<String> getSuiteNames(){
		List<String> Suites = new ArrayList<String>();
		XSSFSheet sheet = accesssheet("Controller");
		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			String inp = (sheet.getRow(i).getCell(1)).toString();
			if (inp.equalsIgnoreCase("Y")) {
				String a = (sheet.getRow(i).getCell(0).toString());
				Suites.add(a);
				strBrowserVal = (sheet.getRow(i).getCell(2).toString());
				//System.out.println(strBrowserVal);
				urlval = (sheet.getRow(i).getCell(3).toString());
			}
		}
		return Suites;
	}
	
	
	public String getBrowserName(String SuiteName){
		String browserName = null;
		XSSFSheet sheet = accesssheet("Controller");
		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			String inp = (sheet.getRow(i).getCell(0)).toString();
			if (inp.equalsIgnoreCase(SuiteName)) {
				browserName = (sheet.getRow(i).getCell(2).toString());
				break;
			}
			//strBrowserVal=browserName;
		}
		return browserName;
	}
	
	
	public String getEnvURL(String SuiteName){
		String EnvURL = null;
		XSSFSheet sheet = accesssheet("Controller");
		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			String inp = (sheet.getRow(i).getCell(0)).toString();
			if (inp.equalsIgnoreCase(SuiteName)) {
				EnvURL = (sheet.getRow(i).getCell(3).toString());
				break;
			}
		}
		return EnvURL;
	}
	
	

}
