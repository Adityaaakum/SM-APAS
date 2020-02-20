package com.apas.Utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.PropertiesConfiguration;

import com.apas.TestBase.TestBase;

public class Util {

	public enum FileType {

		TEST_DATA_FILE("/TestData.properties"), CONFIG_FILE("/envConfig.properties");
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
			if (fileName.contains("envConfig")) {
				absoultePath = System.getProperty("user.dir") + "//src//test//resources//envConfig.properties";
			} else if (fileName.contains("TestData")) {
				absoultePath = System.getProperty("user.dir") + "//src//test//resources//TestData.properties";
			}
			PropertiesConfiguration config = new PropertiesConfiguration(absoultePath);
			config.setProperty(key, value);
			config.save();
			TestBase.loadPropertyFiles();
		} catch (URISyntaxException e) {
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
	 * @author Sikander Bhambhu Method : generateMapFromDataFile
	 * 
	 *         Description : This method internally uses
	 *         retrieveListOfLinesFromTxtFile which returns a list all the lines
	 *         found in the provided idata file. Using this list, it creates a
	 *         data map which has various data fields in the application as keys
	 *         and their values as per provided by user as Strings.
	 * 
	 * @param key:
	 *            Path of the file to be read. Example
	 *            'C:\Users\Administrator\Desktop\DataFile.txt'
	 **/
	public Map<String, String> generateMapFromDataFile(String filePath) {
		List<List<String>> allLinesList = retrieveListOfLinesFromTxtFile(filePath);
		Map<String, String> dataMap = new HashMap<String, String>();
		for (int i = 0; i < allLinesList.get(0).size(); i++) {
			dataMap.put(allLinesList.get(0).get(i), allLinesList.get(1).get(i));
		}
		return dataMap;
	}

	/**
	 * @author Sikander Bhambhu Method : getCurrentDate
	 * 
	 * @Description: This method takes an expected format for date and return
	 *               the current date in the format provided.
	 * 
	 * @param key:
	 *            expected format of date, example 'MM/dd/yyyy'
	 **/
	public String getCurrentDate(String format) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	/**
	 * @author Sikander Bhambhu Method : migrateOldReportsToAcrhive
	 * 
	 * @Description : This method internally collects all the execution reports
	 *              from AutomationReport folder and moves them to an archive
	 *              folder on start of test execution.
	 **/
	public void migrateOldReportsToAcrhive() throws IOException {
		String sourceDir = System.getProperty("user.dir") + "\\test-output\\AutomationReport";
		String destinationDir = System.getProperty("user.dir") + "\\test-output\\AutomationReport\\" + "Reports Archive";
		File directory = new File(destinationDir);
		if (!directory.exists()) {
			directory.mkdir();
		}

		File sourceFiles = new File(sourceDir);
		String[] fileNames = sourceFiles.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".html")) {
					return true;
				} else {
					return false;
				}
			}
		});

		if (fileNames.length > 0) {
			for (String fileName : fileNames) {
				String source = sourceDir + "\\" + fileName;
				String destination = destinationDir + "\\" + fileName;
				Files.move(Paths.get(source), Paths.get(destination));
			}
		}
	}
}
