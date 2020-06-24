package com.apas.Utils;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class ExcelUtils {

    /**
     * @param filePath:         Takes the path of the XLSX workbook
     * @param sheetNameOrIndex: Takes the names of the Sheet that is be read from given workbook
     * @Description : Reads given excel file and converts the data into a map.
     * @return: Return a data map
     **/
    public static HashMap<String, ArrayList<String>> getExcelSheetData(String filePath, String sheetNameOrIndex) throws Exception {

        HashMap<String, ArrayList<String>> hashMapExcelSheet = new HashMap<>();
        FileInputStream file = new FileInputStream(new File(filePath));
        POIFSFileSystem fs = new POIFSFileSystem(file);
//        XSSFWorkbook workBook = new XSSFWorkbook(file);
//        XSSFSheet sheet = workBook.getSheet(sheetNameOrIndex);

        HSSFWorkbook workBook = new HSSFWorkbook(fs);
        HSSFSheet sheet = workBook.getSheet(sheetNameOrIndex);


        try {
            Row headerRow = sheet.getRow(0);
            int totalCells = headerRow.getLastCellNum();
            int rowCount = sheet.getPhysicalNumberOfRows();
            FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();

            for (int rowNum = 1; rowNum < rowCount; rowNum++) {
                Row currentRow = sheet.getRow(rowNum);
                for (int colNum = 0; colNum < totalCells; colNum++) {
                    Cell headerCell = headerRow.getCell(colNum);
                    String headerValue = headerCell.getStringCellValue();
                    Cell dataCell = currentRow.getCell(colNum);
                    CellValue cellValue = evaluator.evaluate(dataCell);
                    Object strColumnValue = cellValue.getNumberValue();

                    switch (cellValue.getCellType()) {
                        case Cell.CELL_TYPE_STRING:
                            strColumnValue = cellValue.getStringValue();
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            strColumnValue = cellValue.getNumberValue();
                            break;
                    }

                    hashMapExcelSheet.computeIfAbsent(headerValue, k -> new ArrayList<>());
                    hashMapExcelSheet.get(headerValue).add(strColumnValue.toString());
                }
            }
        }finally{
            workBook.close();
            file.close();
        }

        return hashMapExcelSheet;
    }

}
