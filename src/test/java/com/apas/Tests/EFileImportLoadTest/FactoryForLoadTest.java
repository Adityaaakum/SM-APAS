package com.apas.Tests.EFileImportLoadTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;

public class FactoryForLoadTest {

	
	  @Factory(dataProvider = "dp") 
	  public Object[] factoryMethod(String fileType,
	  String source,String fileImport, String fileName) {
	 
	/*@Factory
	public Object[] factoryMethod() {
		
		
		  return new Object[] { 
				  new EFileImportToolLoad_Test("Building Permit","San Mateo Building permits",
		                  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest",
		                  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest"),
				  new EFileImportToolLoad_Test("Building Permit","San Mateo Building permits",
						  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest", 
						  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest"),C
				  new EFileImportToolLoad_Test("Building Permit","San Mateo Building permits",
						  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest",
						  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest"),
				  new EFileImportToolLoad_Test("Building Permit","San Mateo Building permits",
						  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest",
						  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest"),
				  new EFileImportToolLoad_Test("Building Permit","San Mateo Building permits",
						  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest",
						  "SanMateoBuildingPermitsWithValidAndInvalidDataForTest"),
				 new EFileImportToolLoad_Test("BPP Trend Factors",
					       "BOE - Index and Percent Good Factors", "2021",
					       "BOE Equipment Index Factors and Percent Good Factors 2021") };
		 
*/		
		
	/*
	 * return new Object[] { new EFileImportToolLoad_Test("BPP Trend Factors",
	 * "BOE - Index and Percent Good Factors", "2020",
	 * "BOE Equipment Index Factors and Percent Good Factors 2020") };
	 */
		 
		
		
		  return new Object[] { new EFileImportToolLoad_Test(fileType, source,
		  fileImport, fileName) };
		 
    }
	
	
	  @DataProvider public Object[][] dp() { return new Object[][]
	  {{"Building Permit","San Mateo Building permits","SanMateoBuildingPermitWithWrongMessageRecordsForLoadTest_13K","SanMateoBuildingPermitWithWrongMessageRecordsForLoadTest_13K"},
	   
	   }; }
	 }
