package com.apas.Utils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import com.apas.BrowserDriver.BrowserDriver;
import com.apas.Reports.ExtentTestManager;
import com.relevantcodes.extentreports.LogStatus;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.apas.TestBase.TestBase;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;

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
	 * @param fileType
	 * @throws Exception
	 */
	public static void setValIntoResource(String key, String value, FileType fileType) throws Exception {

		//Getting the url of the file type passed through the parameter
		URL url = Util.class.getResource(fileType.getFileName());
		String fileName;
		try {
			fileName = url.toURI().getPath();

			String absoultePath = null;
			
			//Setting absolute path of the file based on the file name
			if (fileName.contains("envConfig")) {
				absoultePath = System.getProperty("user.dir") + "//src//test//resources//envConfig.properties";
			} else if (fileName.contains("TestData")) {
				absoultePath = System.getProperty("user.dir") + "//src//test//resources//TestData.properties";
			}
			
			//Loading the properties of the property file
			PropertiesConfiguration config = new PropertiesConfiguration(absoultePath);
			//Setting the value of the key in the property file
			config.setProperty(key, value);
			config.save();
			//Reloading the properties after the property update
			TestBase.loadPropertyFiles();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author Sikander Bhambhu 
	 *         Description : This method read the diven text file and return a
	 *         of all the lines (comma seperated) read from it.
	 *         @param filePath
	 *         			path of file to be conveted to list
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
	 * @author Sikander Bhambhu 
	 * 
	 *         Description : This method internally uses
	 *         retrieveListOfLinesFromTxtFile which returns a list all the lines
	 *         found in the provided idata file. Using this list, it creates a
	 *         data map which has various data fields in the application as keys
	 *         and their values as per provided by user as Strings.
	 * 
	 * @param filePath:
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
	 * @author Sikander Bhambhu 
	 * 
	 * @Description: This method takes an expected format for date and return
	 *               the current date in the format provided.
	 * 
	 * @param format:
	 *            expected format of date, example 'MM/dd/yyyy'
	 **/
	public String getCurrentDate(String format) {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		String formattedDate = sdf.format(date);
		return formattedDate;
	}

	/**
	 * @author Sikander Bhambhu
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

		//Fetching all the files ending with .html in the source folder
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

		//Moving all the files identified above from source to destination folder
		if (fileNames.length > 0) {
			for (String fileName : fileNames) {
				String source = sourceDir + "\\" + fileName;
				String destination = destinationDir + "\\" + fileName;
				Files.move(Paths.get(source), Paths.get(destination));
			}
		}
	}

	/**
	 * Captures the screen shot on assertion failure and attaches it toExtent report
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public void getScreenShot(String message) {
		String methodName = System.getProperty("currentMethodName");
		RemoteWebDriver ldriver = BrowserDriver.getBrowserInstance();
		TakesScreenshot ts = (TakesScreenshot) ldriver;
		File source = ts.getScreenshotAs(OutputType.FILE);

		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
		String upDate = sdf.format(date);
		String dest = System.getProperty("user.dir")+ "//test-output//ErrorScreenshots//" + methodName + upDate +".png";

		File destination = new File(dest);
		try {
			FileUtils.copyFile(source, destination);
			ExtentTestManager.getTest().log(LogStatus.INFO, "Snapshot for the failed validation : " + message
					+ ExtentTestManager.getUpTestVariable().addScreenCapture(encodeFileToBase64Binary(destination)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method will convert a file into Base 64 Binary to embed in the report
	 * @param file: File to be encoded
	 */
	public String encodeFileToBase64Binary(File file){
		String encodedFile = null;
		try {
			FileInputStream fileInputStreamReader = new FileInputStream(file);
			byte[] bytes = new byte[(int)file.length()];
			fileInputStreamReader.read(bytes);
			encodedFile = Base64.getEncoder().encodeToString(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "data:image/jpeg;base64," + encodedFile;
	}


}
