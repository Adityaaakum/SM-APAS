package com.apas.Utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class FileUtils {

    /**
     * This function will convert the CSV data into a Hash Map
     * @param csvFilePath: CSV File absolute Path
     * @return CSV Data converted in to Hash Map
     */
    public static HashMap<String, ArrayList<String>> getCSVData(String csvFilePath) throws IOException {

        HashMap<String, ArrayList<String>> csvDataHashMap = new HashMap<>();
        //Delimiter and Headers existence in the file can be configured here
        CSVParser csvParser = new CSVParser(new FileReader(csvFilePath), CSVFormat.RFC4180.withHeader().withDelimiter(','));
        Object[] csvHeader = csvParser.getHeaderMap().keySet().toArray();
        List<CSVRecord> csvRecords = csvParser.getRecords();

        String key,value;
        for (CSVRecord csvRecord: csvRecords){
            for(int count = 0; count<csvHeader.length;count++){
                key = csvHeader[count].toString();
                value=csvRecord.get(count);
                csvDataHashMap.computeIfAbsent(key, k -> new ArrayList<>());
                csvDataHashMap.get(key).add(value);
            }
        }
        csvParser.close();
        return csvDataHashMap;
    }


    /**
     * This function will compare the 2 Hash Maps and will print the cell wise results in the report
     * @param hashMap1: First Hash Map to be compared
     * @param hashMap2: Second Hash Map to be compared
     * @return boolean flag True/False
     */
    public static String compareHashMaps(HashMap<String, ArrayList<String>> hashMap1, HashMap<String, ArrayList<String>> hashMap2){

        String errors = "";

        //Validating if both the hash maps contain the same keys
        if (hashMap1.keySet().equals(hashMap2.keySet())){
            Set<String> keySet = hashMap1.keySet();
            for (String key: keySet){
                ArrayList<String> listOfValue1 = hashMap1.get(key);
                ArrayList<String> listOfValue2 = hashMap2.get(key);
                //Data validation of the has maps
                for (int count =0; count< listOfValue1.size();count++){
                    if (listOfValue1.size() == listOfValue2.size()){
                        if (!listOfValue1.get(count).trim().equals(listOfValue2.get(count).trim())) {
                            errors = errors + "Value mismatch for Row# " + (count + 1) + " and Column:" + key + " | HashMap1 : " + listOfValue1.get(count) + " | HashMap2 : " + listOfValue2.get(count) + "\n";
                        }
                    } else {
                        errors = errors + "Rows count validation failed for column : " + key + " | HashMap1 : " + listOfValue1.size()  + " | HashMap2 : " + listOfValue2.size()  + "\n";
                    }
                }
            }
        } else
            errors = "Header(keySet) not matching. HashMap1 : " + hashMap1.keySet() + " | HashMap2 : " + hashMap2.keySet();

        System.out.println("Errors While Comparing HashMaps : " + errors);

        return errors;
    }

    /**
     * This function will create a folder at the specified path if it doesn't exist
     * @param folderPath: Path of the folder to be created
     */
    public static void createFolder(String folderPath){
        File dir = new File(folderPath);
        if (!dir.exists()) dir.mkdirs();
    }

    /**
     * This function will replace String in the file and create new file
     * @param sourceFile: Path of the file where String needs to be replaced
     * @param stringToBeReplaced : Old Text
     * @param newTextToReplace : New Text
     * @param destinationFile : New file to be created with replaced string
     */
    public static void replaceString(String sourceFile, String stringToBeReplaced, String newTextToReplace,String destinationFile) throws IOException {
        String content = org.apache.commons.io.FileUtils.readFileToString(new File(sourceFile), "UTF-8");
        content = content.replaceAll("<PERMITNO>", newTextToReplace);
        File tempFile = new File(destinationFile);
        org.apache.commons.io.FileUtils.writeStringToFile(tempFile, content, "UTF-8");
    }
    
    

}
