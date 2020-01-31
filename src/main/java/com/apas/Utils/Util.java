package com.apas.Utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

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
	
}
