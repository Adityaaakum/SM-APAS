package com.apas.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import com.apas.Reports.ReportLogger;
import com.apas.TestBase.TestBase;
import com.apas.config.users;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.JSONException;

public class SalesforceAPI extends TestBase {

    private static String baseUri;
    private static Header oauthHeader;
    private static Header prettyPrintHeader = new BasicHeader("X-PrettyPrint", "1");
    public static String REMINDER_WI_CODE_DV = "new+DisabledVeteransAnnualReminderWIService().createReminderWorkItems(ApexUtility.getCurrentRollYear());";
    public static String REMINDER_WI_CODE_BPP_ANNUAL_FACTORS = "new+BPPTrendsAnnualFactorWorkItemService().createBppTrendsAnnualFactorsWI(ApexUtility.getCurrentRollYear());";
    public static String REMINDER_WI_CODE_BPP_EFILE = "new+BPPTrendsImportReminderWorkItemService().createReminderWorkItemsForEFile(ApexUtility.getCurrentRollYear());";
    
    /**
     * This method will create HTTP Post connection with Salesforce
     @return Http Post Connection
     */
    private HttpPost salesforceCreateConnection() {

        String userName = CONFIG.getProperty("restApiUserName");
        String securityToken = CONFIG.getProperty("restApiSecurityToken");
        String password = CONFIG.getProperty("restApiPassword");
        String loginUrl = envURL;
        String grantService = "/services/oauth2/token?grant_type=password";
        String clientId = CONFIG.getProperty("clientId");
        String clientSecretKey = CONFIG.getProperty("clientSecretKey");

        //Decrypting the password if the encrypted password is saved in envconfig file and passwordEncryptionFlag flag is set to true
        if (CONFIG.getProperty("passwordEncryptionFlag").equals("true")){
            System.out.println("Decrypting the password : " + password);
            password = PasswordUtils.decrypt(password, "");
        }
        String passwordWithSecurityToken = password + securityToken;

        // Assemble the login request URL
        String loginURL = loginUrl +
                grantService +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecretKey +
                "&username=" + userName +
                "&password=" + passwordWithSecurityToken;

        // Login requests must be POSTs
        return new HttpPost(loginURL);
    }

    /**
     * This method will verify HTTP connection and will update BaseUri and oauthHeader
     * @param httpPost : Http Post connection
     * @return true/false
     */
    private boolean salesforceAuthentication(HttpPost httpPost) {

        HttpClient httpclient = HttpClientBuilder.create().build();

        try {
            // Execute the login POST request
            HttpResponse response = httpclient.execute(httpPost);

            // verify response is HTTP OK
            final int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == HttpStatus.SC_OK) {
                String getResult = EntityUtils.toString(response.getEntity());

                JSONObject jsonObject = (JSONObject) new JSONTokener(getResult).nextValue();
                String loginAccessToken = jsonObject.getString("access_token");
                String loginInstanceUrl = jsonObject.getString("instance_url");
                String REST_ENDPOINT = "/services/data";
                String API_VERSION = "/v49.0";
                baseUri = loginInstanceUrl + REST_ENDPOINT + API_VERSION;
                oauthHeader = new BasicHeader("Authorization", "OAuth " + loginAccessToken);

                System.out.println("oauthHeader1: " + oauthHeader);
                System.out.println("\n" + response.getStatusLine());
                System.out.println("Successful login");
                System.out.println("instance URL: " + loginInstanceUrl);
                System.out.println("access token/session ID: " + loginAccessToken);
                System.out.println("baseUri: " + baseUri);

                return true;
            } else {
                ReportLogger.FAIL("Error authenticating to Force.com: " + statusCode);
                return false;
            }

        } catch (IOException | JSONException cpException) {
            cpException.printStackTrace();
        }

        return false;
    }

    /**
     * This method will release HTTP Post connection with Salesforce
     */
    private void salesforceReleaseConnection(HttpPost httpPost) {
        httpPost.releaseConnection();
    }

    /**
     * This method will execute the Select SQL query
     * @param sqlQuery : Select SQL query
     * @return Json response in String format
     */
    private String getJsonResponse(String sqlQuery) {
        String responseString = "";
        System.out.println("Executing Query : " + sqlQuery);
        try {
            //Set up the HTTP objects needed to make the request.
            HttpClient httpClient = HttpClientBuilder.create().build();
            String uri = baseUri + "/query?q=" + sqlQuery;
            System.out.println("Query URL: " + uri);
            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);

            // Make the request.
            HttpResponse response = httpClient.execute(httpGet);

            // Process the result
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                responseString = EntityUtils.toString(response.getEntity());
            } else {
                ReportLogger.FAIL("Query was unsuccessful. Status code returned is " + statusCode);
                ReportLogger.FAIL("An error has occurred. Http status: " + response.getStatusLine().getStatusCode());
            }
        } catch (IOException | NullPointerException ioe) {
            ioe.printStackTrace();
        }

        return responseString;
    }

    /**
     * This method will parse Json response and will convert the output into as hash map
     * @param responseString : Response received from HTTP Post request
     * @return HashMap containing the data received from HTTP Post request
     */
    private HashMap<String, ArrayList<String>> parseJason(String responseString) {
        JSONObject jsonObjectRecord;
        HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(responseString);
            JSONArray jsonArray = jsonObject.getJSONArray("records");
            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObjectRecord = jsonArray.getJSONObject(i);
                Iterator jsonObjectKeys = jsonObjectRecord.keys();
                while (jsonObjectKeys.hasNext()) {
                    String key = jsonObjectKeys.next().toString();
                    String value = jsonObjectRecord.getString(key);
                    hashMap.computeIfAbsent(key, k -> new ArrayList<>());
                    hashMap.get(key).add(value);
                }
            }
            hashMap.remove("attributes");

        } catch (JSONException je) {
            je.printStackTrace();
        }

        return hashMap;
    }

    /**
     * This method will execute the Select SQL query end to end from creating HTTP connection
     * to release connection
     * @param sqlQuery : Select SQL query
     * @return String json data in string
     */
    public String getSelectQueryDateInJson(String sqlQuery) {
        ReportLogger.INFO("Executing the query : " + sqlQuery);
        sqlQuery = sqlQuery.replace("%", "%25");
        sqlQuery = sqlQuery.replace(" ", "%20");
        sqlQuery = sqlQuery.replace("'", "%27");
        sqlQuery = sqlQuery.replace("=", "%3D");
        sqlQuery = sqlQuery.replace("!", "%21");
        sqlQuery = sqlQuery.replace("<", "%3C");
        sqlQuery = sqlQuery.replace(">", "%3E");
        System.out.println("Modified query for URI : " + sqlQuery);

        String jsonData = "";

        //Creating HTTP Post Connection
        HttpPost httpPost = salesforceCreateConnection();

        //Authenticating the HTTP Post connection. Executing the SQL Query
        if (salesforceAuthentication(httpPost)) {
            jsonData = getJsonResponse(sqlQuery);
        }

        //Releasing HTTP Post connection
        salesforceReleaseConnection(httpPost);

        return jsonData;
    }

    /**
     * This method will execute the Select SQL query end to end from creating HTTP connection
     * to release connection
     * @param sqlQuery : Select SQL query
     * @return HashMap containing the data received from HTTP Post request
     */
    public HashMap<String, ArrayList<String>> select(String sqlQuery) {
        String jsonResponse = getSelectQueryDateInJson(sqlQuery);
        return parseJason(jsonResponse);
    }

    /**
     * This method will return the ids in a String separated by comma
     * @param sqlQuery : SQL Query to fetch the IDs
     */
    private String getCommaSeparatedIds(String sqlQuery){
        HashMap<String, ArrayList<String>> queryDataHashMap = select(sqlQuery);
        String Ids = "";
        if (queryDataHashMap.get("Id") != null){
            Ids = queryDataHashMap.get("Id").toString().replace("[","").replace("]","");
        }
        return Ids;
    }

    /**
     * This method will delete the records based on object name and ids
     * @param table : Name of the object from where records are to be deleted
     * @param commaSeparatedIdsORSqlQuery : List of ids separated by comma to be deleted or Select query fetching the IDs to be deleted
     */
    public void delete(String table, String commaSeparatedIdsORSqlQuery) {
        ReportLogger.INFO("Deleting the records from " + table + " for following query or comma separated IDs : " + commaSeparatedIdsORSqlQuery);
        String commaSeparatedIds = commaSeparatedIdsORSqlQuery;

        //Converting the IDs returned from SQL query to comma separated
        if (commaSeparatedIds.toUpperCase().trim().startsWith("SELECT")){
            commaSeparatedIds = getCommaSeparatedIds(commaSeparatedIdsORSqlQuery);
        }

        ReportLogger.INFO("Deleting " + commaSeparatedIds + " from table " + table);

        //Creating HTTP Post Connection
        HttpPost httpPost = salesforceCreateConnection();

        //Authenticating the HTTP Post connection
        if (salesforceAuthentication(httpPost)){

            //Set up the objects necessary to make the request.
            HttpClient httpClient = HttpClientBuilder.create().build();
            try {

            	if(!commaSeparatedIds.equals("")) {
	                String[] ids= commaSeparatedIds.split(",");
	                for (String id: ids){
	                    String uri = baseUri + "/sobjects/" + table + "/" + id.trim();
	                    HttpDelete httpDelete = new HttpDelete(uri);
	                    httpDelete.addHeader(oauthHeader);
	                    httpDelete.addHeader(prettyPrintHeader);
	
	                    HttpResponse response = httpClient.execute(httpDelete);
	
	                    int statusCode = response.getStatusLine().getStatusCode();
	                    if (statusCode == 204) {
	                        System.out.println(id + " Deleted Successfully.");
	                    } else {
	                        ReportLogger.FAIL(id + " Delete NOT Successful. Status Code : " + statusCode);
	                    }
	                }
            }
            } catch (IOException | NullPointerException ioe) {
                ioe.printStackTrace();
            }
        } else
            System.out.println("Salesforce authentication failed");

        //Releasing HTTP Post connection
        salesforceReleaseConnection(httpPost);

    }

    /**
     * This method will delete maxmimum 200 records based on object name and ids
     * @param commaSeparatedIdsORSqlQuery : List of ids separated by comma to be deleted or Select query fetching the IDs to be deleted
     */
    public void delete(String commaSeparatedIdsORSqlQuery) {
        int statusCode;

        if (commaSeparatedIdsORSqlQuery.toUpperCase().trim().startsWith("SELECT")) {
            //This will delete maximum 200 record in one go as there is a limit of 200 records
            String commaSeparatedIds = "";
            HashMap<String, ArrayList<String>> queryDataHashMap = select(commaSeparatedIdsORSqlQuery);
            if (queryDataHashMap.get("Id") != null){
                int noOfRecordsToBeDeleted = queryDataHashMap.get("Id").size();
                int noOfLoops = noOfRecordsToBeDeleted/200;
                String queryWith200Limit = commaSeparatedIdsORSqlQuery + " limit 200";
                ReportLogger.INFO("Delete Query : " + queryWith200Limit);
                for (int i = 0; i<=noOfLoops; i++){
                    commaSeparatedIds = getCommaSeparatedIds(queryWith200Limit);
                    statusCode = delete(commaSeparatedIds,0);
                    if (statusCode == 200 || statusCode==0 )    //When all the Ids are deleted StatusCode returns 0.
                        ReportLogger.PASS("Status Code for Delete Query : " + statusCode);
                    else
                        ReportLogger.FAIL("Status Code for Delete Query : " + statusCode);
                }
            }

        }else{
            //This will delete the records if comma separated Ids are passed
            ReportLogger.INFO("Comma Separated IDs : " + commaSeparatedIdsORSqlQuery);
            statusCode = delete(commaSeparatedIdsORSqlQuery,0);
            if (statusCode == 200)
                ReportLogger.PASS("Status Code for comma separated ids : " + statusCode);
            else
                ReportLogger.FAIL("Status Code for comma separated ids : " + statusCode);
        }
    }

    /**
     * This method will delete maxmimum 200 records based on object name and ids
     * @param commaSeparatedIds : List of ids separated by comma to be deleted or Select query fetching the IDs to be deleted
     * @param placeHolder : This parameter is just a place holder for future use
     */
    private int delete(String commaSeparatedIds, int placeHolder) {
        int statusCode = 0;

        ReportLogger.INFO("Deleting following comma separated IDs" + commaSeparatedIds);

        if (!commaSeparatedIds.trim().equals("")){
            //Creating HTTP Post Connection
            HttpPost httpPost = salesforceCreateConnection();

            //Authenticating the HTTP Post connection
            if (salesforceAuthentication(httpPost)){

                //Set up the objects necessary to make the request.
                HttpClient httpClient = HttpClientBuilder.create().build();
                try {
                    if(!commaSeparatedIds.equals("")) {
                        String uri = baseUri + "/composite/sobjects?ids=" + commaSeparatedIds.replace(" ","").trim();
                        System.out.println("URI : " + uri);
                        HttpDelete httpDelete = new HttpDelete(uri);
                        httpDelete.addHeader(oauthHeader);
                        httpDelete.addHeader(prettyPrintHeader);

                        HttpResponse response = httpClient.execute(httpDelete);

                        statusCode = response.getStatusLine().getStatusCode();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            //Releasing HTTP Post connection
            salesforceReleaseConnection(httpPost);

        }

        return statusCode;
    }


    /**
     * This method will update a single column value
     * @param table : Name of the object from where records are to be updated
     * @param commaSeparatedIdsORSQLQuery : Comma Separated IDs of the record to be updated or the SQL query
     * @param column : Name of column where the value to be updated
     * @param value : New value to be updated
     */
    public void update(String table, String commaSeparatedIdsORSQLQuery, String column, String value)  {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(column,value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        update(table,commaSeparatedIdsORSQLQuery,jsonObject);
    }

    /**
     * This method will update a single column value
     * @param table : Name of the object from where records are to be updated
     * @param commaSeparatedIdsORSQLQuery : Comma Separated IDs of the record to be updated or the SQL query
     * @param jsonObject : List of columns to be updated in form of json object
     */
    public void update(String table, String commaSeparatedIdsORSQLQuery, JSONObject jsonObject) {

    	ReportLogger.INFO("Updating the object " + table + " through Salesforce API for following query or comma separated IDs : " + commaSeparatedIdsORSQLQuery);    	
        //Creating HTTP Post Connection
        HttpPost httpPost = salesforceCreateConnection();

        //Authenticating the HTTP Post connection
        if (salesforceAuthentication(httpPost)){
            HttpClient httpClient = HttpClientBuilder.create().build();

            //Converting IDs from SQL query to comma separated IDs
            String commaSeparatedIds = commaSeparatedIdsORSQLQuery;
            if (commaSeparatedIdsORSQLQuery.toUpperCase().startsWith("SELECT")){
                commaSeparatedIds = getCommaSeparatedIds(commaSeparatedIdsORSQLQuery);
            }

            ReportLogger.INFO("Updating " + table + " for IDs : " + commaSeparatedIds);

            if (!commaSeparatedIds.equals("")){
                String[] ids = commaSeparatedIds.split(",");

                for (String id : ids){
                    String uri = baseUri + "/sobjects/" + table + "/" + id.trim();
                    try {

                        HttpPatch httpPatch = new HttpPatch(uri);
                        httpPatch.addHeader(oauthHeader);
                        httpPatch.addHeader(prettyPrintHeader);
                        StringEntity body = new StringEntity(jsonObject.toString(1));
                        body.setContentType("application/json");
                        httpPatch.setEntity(body);

                        HttpResponse response = httpClient.execute(httpPatch);

                        //Process the response
                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode == 204) {
                            System.out.println("Update " + table + " successful for Id " + id);
                        } else {
                            ReportLogger.FAIL("Update " + table + " Not successful for id " + id + ". Status code is " + statusCode);
                        }
                    } catch (JSONException | IOException | NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //Release HTTP Post connection
        salesforceReleaseConnection(httpPost);
    }
    
    /**
     * This method will delete BPP Trend data for the roll years passed in the parameter
     * @param rollYear : Roll Year data to be deleted
     */
    public void deleteBPPTrendRollYearData(String rollYear){
        String queryBPPCompositeFactor = "select id from BPP_Composite_Factor__c where Roll_Year__c = '" + rollYear  + "'";
        String queryBPPPropertyIndexFactor = "select id from BPP_Property_Index_Factor__c where Roll_Year__c = '" + rollYear  + "'";
        String queryBPPTrendValuationFactor = "select id from BPP_Trend_Valuation_Factor__c where Roll_Year__c = '" + rollYear  + "'";
        String queryBPPPropertyGoodFactor = "select id from BPP_Property_Good_Factor__c where Roll_Year__c = '" + rollYear  + "'";

        delete(queryBPPCompositeFactor);
        delete(queryBPPPropertyIndexFactor);
        delete(queryBPPTrendValuationFactor);
        delete(queryBPPPropertyGoodFactor);
    }

    /**
     * This method will delete import logs and transaction trails based on the file type
     * @param fileType : File Type of the data to be deleted
     */
    public void deleteImportTransactionTrailAndLogs(String fileType){

        String queryTransactionTrails = "select id from Transaction_Trail__c where E_File_Import_Transaction__r.E_File_Import_Log__r.File_type__c = '" + fileType + "' and E_File_Import_Transaction__r.E_File_Import_Log__r.Owner.Email like '%Sapient%'";
        String queryImportLogs = "select id from E_File_Import_Log__c where File_type__c = '" + fileType + "' and Owner.Email like '%Sapient%'";

        delete("Transaction_Trail__c",queryTransactionTrails);
        delete("E_File_Import_Log__c",queryImportLogs);
    }
    
   
    /**
     * This method will trigger the job to generate reminder work items
     */
    public void generateReminderWorkItems(String reminderWorkItemCode) throws IOException {

        ReportLogger.INFO("Generating Disabled Veteran Reminder Work Items");
        //Creating HTTP Post Connection
        HttpPost httpPost = salesforceCreateConnection();

        //Authenticating the HTTP Post connection
        if (salesforceAuthentication(httpPost)){
            HttpClient httpClient = HttpClientBuilder.create().build();
            String uri = baseUri + "/tooling/executeAnonymous/?anonymousBody=" + reminderWorkItemCode ;
            System.out.println("URL: " + uri);
            HttpGet httpGet = new HttpGet(uri);
            httpGet.addHeader(oauthHeader);
            httpGet.addHeader(prettyPrintHeader);

            // Make the request.
            HttpResponse response = httpClient.execute(httpGet);

            // Process the result
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                ReportLogger.PASS("Reminder Work Item Job Triggered Successfully. Status Code : " + statusCode);
            } else {
                ReportLogger.FAIL("Reminder Work Item Job Was Unsuccessful. Status code returned is " + statusCode);
            }
        }

        //Release HTTP Post connection
        salesforceReleaseConnection(httpPost);
    }

    /**
     * This method will return the user based on the user name
     */
    public String getUserName(String user){
        user = CONFIG.getProperty(user + "UserName");
        String userName = "select Name from User where UserName__c = '"+ user + "'";
        return select(userName).get("Name").get(0);
    }

    /**
     * This method will delete the work items based on the age of the work item
     * @param age: Age beyound which work items need to be deleted
     */
    public void deleteWorkItemsBasedOnAge(int age){
        String queryToDeleteWorkItems = "SELECT id FROM Work_Item__c where age__c > " + age ;
        delete(queryToDeleteWorkItems);
    }
}