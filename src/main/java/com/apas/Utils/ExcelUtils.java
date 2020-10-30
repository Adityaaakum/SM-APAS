package com.apas.Utils;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ExcelUtils {

     /**
      * This method will return the data in hashmap. the table starts in the middle of the sheet so need to give the first cell details of the table
     * @param filePath: Takes the path of the XLSX workbook
     * @param sheetIndex: Takes the names of the Sheet that is be read from given workbook
     * @return Return a hash map
     **/
     public static HashMap<String, ArrayList<String>> getExcelSheetData(String filePath, int sheetIndex) throws Exception {
         return getExcelSheetData(filePath,sheetIndex,0,0);
     }

    /**
     * Reads given excel file and converts the data into a hashmap.
     * @param filePath:Takes the path of the XLSX workbook
     * @param sheetIndex: Takes the index of the Sheet that is be read from given workbook
     * @return Return a data map
     * Note: Few exported files have the table in the middle of the sheet thats why tableStartRow and tableStartColumn columns are required
     **/
     public static HashMap<String, ArrayList<String>> getExcelSheetData(String filePath, int sheetIndex, int tableStartRow, int tableStartColumn) throws Exception {

        HashMap<String, ArrayList<String>> hashMapExcelSheet = new LinkedHashMap<>();
        FileInputStream file = new FileInputStream(new File(filePath));
        XSSFWorkbook workBook = new XSSFWorkbook(file);
        Object strColumnValue;
        XSSFSheet sheet = workBook.getSheetAt(sheetIndex);
        boolean flag=false;

        try {
            Row headerRow = sheet.getRow(tableStartRow);
            int totalCells = headerRow.getLastCellNum();
            int rowCount = sheet.getPhysicalNumberOfRows();

            //Added this condition to restrict to return 200 records data as converting a lot of data in hashmap gives java memory issue
            if (rowCount > 200) rowCount = 200;

            FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();
            for (int rowNum = (tableStartRow + 1); rowNum < rowCount; rowNum++) {
                Row currentRow = sheet.getRow(rowNum);
                for (int colNum = tableStartColumn; colNum < (totalCells-tableStartColumn); colNum++) {
                    Cell headerCell = headerRow.getCell(colNum);
                    String headerValue = getCellValue(headerCell);
                  //Status column exists twice in Report and below check will add both as keys
        			if(headerValue.equals("Status") && flag == true) {
        				headerValue=headerValue + "_1";
        			}
                    Cell dataCell = currentRow.getCell(colNum);
                    strColumnValue = getCellValue(dataCell);

                    //This condition is added as blank headers are pulled in some exported files
                    if (!headerValue.equals("")){
                        hashMapExcelSheet.computeIfAbsent(headerValue, k -> new ArrayList<>());
                        hashMapExcelSheet.get(headerValue).add(strColumnValue.toString());
                        if(headerValue.equals("Status")){
        					flag = true;
        				}
                    }
                }
            }
        }finally{
            workBook.close();
            file.close();
        }

        return hashMapExcelSheet;
    }

    /**
     * This method will return the data cell value from excel
     * @param cell : Excel cell
     **/
     public static String getCellValue(Cell cell){

         String strCellValue;
         DataFormatter df = new DataFormatter();
         switch (cell.getCellType()) {
             case Cell.CELL_TYPE_STRING:
                 strCellValue = cell.getStringCellValue();
                 break;
             case Cell.CELL_TYPE_NUMERIC:
             	strCellValue = df.formatCellValue(cell);
                 break;
             case Cell.CELL_TYPE_FORMULA:
                 strCellValue = String.valueOf(cell.getStringCellValue());
                 break;
             default:
                 strCellValue = "";
         }

         return strCellValue;
     }
    
    public void setCellValueAndCopy(String srcFile, String newTextToReplace, String tmpFile) throws IOException {
    	
    	FileInputStream file = new FileInputStream(new File(srcFile));
        XSSFWorkbook workBook = new XSSFWorkbook(file);
        XSSFSheet sheet = workBook.getSheetAt(0);
        
        Cell cell = sheet.getRow(1).getCell(1);
        cell.setCellValue(newTextToReplace);
        
        workBook.getCreationHelper().createFormulaEvaluator().evaluateAll();
        
        file.close();        
        
        FileOutputStream out = new FileOutputStream(tmpFile, false);
        workBook.write(out);
        out.close();
        
        
    	
    	
    }
    /**
   	 * Description : Reads given excel file to retrieve total rows in each sheet the data into a map.
   	 * @param filePath: Takes the path of the XLSX workbook
   	 * @param sheetName: Sheet from which row count is returned
   	 * @return int: Return row Count
        * @throws Exception 
   	 **/
   	public static int getRowCountFromExcelSheet(String filePath, String sheetName, boolean containsHeaderFlag) throws Exception {
   		FileInputStream file = null;
   		XSSFWorkbook workBook = null;
   		int rowCount=0;
   		try {
   			file = new FileInputStream(new File(filePath));
   			workBook = new XSSFWorkbook(file);
   			XSSFSheet sheet = workBook.getSheet(sheetName);
   			rowCount = sheet.getPhysicalNumberOfRows();	
   			if(containsHeaderFlag) {
   				rowCount = rowCount -1;
   			}
   		}  catch (Exception ex) {
   			ex.printStackTrace();
   		} finally {
   			workBook.close();
   			file.close();
   		}							
   		return rowCount;
   	}
   	
   	/**
   	 * Description : Reads given excel file to retrieve total rows in each sheet the data into a map.
   	 * @param filePath: Takes the path of the XLSX workbook
   	 * @param sheetName: Sheet from which row count is returned
   	 * @return int: Return row Count
        * @throws Exception 
   	 **/
   	public static int getRowCountFromExcelSheet(String filePath, String sheetName) throws Exception {
   		int rowCount = getRowCountFromExcelSheet(filePath,sheetName,true);		
   		return rowCount;
   		
   	}
}
