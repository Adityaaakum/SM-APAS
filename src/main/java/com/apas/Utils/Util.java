package com.apas.Utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.apas.TestBase.TestBase;

public class Util {																		

	public enum FileType {

		TEST_DATA_FILE ("/TestData.properties"), CONFIG_FILE ("/envConfig.properties");
		private String fileName;

		public String getFileName() {
			return fileName;
		}

		private FileType(String fileName) {
			this.fileName = fileName;
		}
	}
		
	/**
	 * Function will return the value corresponding to the key passed
	 * 
	 * @param key
	 *            the key
	 * @return the value from resource file
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static String getValFromResource(String key) throws IOException {		
		return TestBase.CONFIG.getProperty(key).trim();
	}

	/**
	 * Function will set the value corresponding to the key passed.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param configFile 
	 * @throws Exception 
	 */
	public static void setValIntoResource(String key, String value, FileType fileType) throws Exception {	
		
		URL url = Util.class.getResource(fileType.getFileName());
		String fileName;
		try {
			fileName = url.toURI().getPath();
		
		String absoultePath = null;
		if(fileName.contains("envConfig")){
			absoultePath=System.getProperty("user.dir") + "//src//test//resources//envConfig.properties";
		}
		else if(fileName.contains("TestData")){
			absoultePath=System.getProperty("user.dir") + "//src//test//resources//TestData.properties";
		}
		PropertiesConfiguration config = new PropertiesConfiguration(absoultePath);
		config.setProperty(key, value);
		config.save();
		TestBase.loadPropertyFiles();	
		} 
		catch (URISyntaxException e) {
			e.printStackTrace();
		}	
	}
	
	/**
	 * @author Sikander Bhambhu Method : retrieveListOfLinesFromNotepad
	 *         Description : This method read the diven text file and return a
	 *         of all the lines (comma seperated) read from it.
	 **/
	public List<List<String>> retrieveListOfLinesFromTxtFile(String filePath) {
		List<List<String>> allLines = new ArrayList<List<String>>();
		try {
			RandomAccessFile file = new RandomAccessFile(filePath, "r");
			String currentLineStr;
			while ((currentLineStr = file.readLine()) != null) {
				List<String> tempList = Arrays.asList(currentLineStr.split(","));
				allLines.add(tempList);
			}
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allLines;
	}

	/**
	 * @author Sikander Bhambhu Method : generateMapFromDataFile Description :
	 *         This method internally uses retrieveListOfLinesFromTxtFile which
	 *         returns a list all the lines found in the provided idata file.
	 *         Using this list, it creates a data map which has various data
	 *         fields in the application as keys and their values as per
	 *         provided by user as Strings.
	 **/
	public Map<String, String> generateMapFromDataFile(String filePath) {
		List<List<String>> allLinesList = retrieveListOfLinesFromTxtFile(filePath);
		Map<String, String> dataMap = new HashMap<String, String>();
		for (int i = 0; i < allLinesList.get(0).size(); i++) {
			dataMap.put(allLinesList.get(0).get(i), allLinesList.get(1).get(i));
		}
		return dataMap;
	}

//	/**
//	 * @author Sikander Bhambhu Method : generateMapFromDataFile Description :
//	 *         This method internally uses retrieveListOfLinesFromTxtFile which
//	 *         returns a list all the lines found in the provided idata file.
//	 *         Using this list, it creates a data map which has various data
//	 *         fields in the application as keys and their values as per
//	 *         provided by user as List of Strings.
//	 **/
//	public Map<String, List<String>> generateMapFromDataFile(String filePath) {
//		List<String> columnNames = new ArrayList<String>();
//		List<List<String>> columnValues = new ArrayList<List<String>>();
//		Map<String, List<String>> dataMap = new HashMap<String, List<String>>();
//
//		List<List<String>> allLinesList = retrieveListOfLinesFromTxtFile(filePath);
//		for (int i = 0; i < allLinesList.size(); i++) {
//			if (i == 0) {
//				columnNames.addAll(allLinesList.get(0));
//			} else {
//				columnValues.add(allLinesList.get(i));
//			}
//		}
//
//		List<String> tempList;
//		for (int i = 0; i < columnNames.size(); i++) {
//			tempList = new ArrayList<String>();
//			for (int j = 0; j < columnValues.size(); j++) {
//				tempList.add(columnValues.get(j).get(i));
//			}
//			dataMap.put(columnNames.get(i), tempList);
//		}
//		return dataMap;
//	}
}
