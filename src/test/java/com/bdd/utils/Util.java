package com.bdd.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.PropertiesConfigurationLayout;

import com.bdd.initialSetUp.TestBase;

public class Util {																		

	public enum FileType {

		TEST_DATA_FILE ("/TestData.properties"), CONFIG_FILE ("/envConfig.properties"), DB_FILE ("/DataBaseQueries.properties");
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
	public static void setValIntoResource(String key, String value, Util.FileType fileType) throws Exception {	
		//TestBase.CONFIG.setProperty(key, value);
		URL url = Util.class.getResource(fileType.getFileName());
		String fileName;
		try {
			fileName = url.toURI().getPath();
		
		String absoultePath = null;
		if(fileName.contains("envConfig")){
			absoultePath=System.getProperty("user.dir") + "//src//test//resources//envConfig.properties";
		}
		/*else if(fileName.contains("DataBaseQueries")){
			absoultePath=System.getProperty("user.dir") + "//src//test//resources//DataBaseQueries.properties";
		}*/
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

	
}
