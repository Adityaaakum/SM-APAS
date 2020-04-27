package com.apas.JiraStatusUpdate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.apas.TestBase.TestBase;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;

public class JiraAdaptavistStatusUpdate extends TestBase {

	public static final String BASE_URI = "https://tools.publicis.sapient.com/jira/rest/atm/1.0/";
	public static HashMap<String, String> testStatus = new HashMap<>();

	/**
	 * @author yogsingh5 
	 * Method : updateJiraTestCaseStatus 
	 * @Params : jiraTestCaseId, testCaseStatus 
	 * Description : This method makes the API call for updating the test cases status in JIRA Adaptavist
	 **/
	public static void updateJiraTestCaseStatus(String jiraTestCaseId, String testCaseStatus) {
		try {
			// String username=System.getProperty("user.name");
			String username = "akaila";

			// Storing BaseURI
			RestAssured.baseURI = BASE_URI;
			RequestSpecification httpRequest = RestAssured.given();

			// setting content type to make the API call
			httpRequest.header("Content-Type", "application/json");
			httpRequest.header("Authorization", "Basic c2lrYmhhbWI6TmF2eWFAMTcyMg==");

			// instantiating pojo class and setting "Status" and "Executed By" fields
			TestCaseUpdateRequestMainPojo testCaseUpdateRequestMainPojo = new TestCaseUpdateRequestMainPojo();
			testCaseUpdateRequestMainPojo.setStatus(testCaseStatus);
			testCaseUpdateRequestMainPojo.setExecutedBy(username);

			// passing body to the API call
			httpRequest.body(testCaseUpdateRequestMainPojo);
			Response response = httpRequest.post("testrun/" + testCycle + "/testcase/" + jiraTestCaseId + "/testresult");

			@SuppressWarnings("rawtypes")
			ResponseBody res = response.getBody();
			System.out.println("Response body post status update in JIRA: " + res.asString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author Sikander Bhambhu 
	 * Description :
	 *         This method makes the API call for fetching all the test cases
	 *         from JIRA for given test cycle and created a map with these test
	 *         cases with null values
	 **/
	public static void retrieveJiraTestCases() {
		System.out.println("Retrieving the Test Case Keys for the Cycle : " + testCycle);
		// Storing BaseURI
		RestAssured.baseURI = BASE_URI;
		RequestSpecification httpRequest = RestAssured.given();

		// setting content type to make the API call
		httpRequest.header("Content-Type", "application/json");
		httpRequest.header("Authorization", "Basic c2lrYmhhbWI6TmF2eWFAMTcyMg==");

		// passing body to the API call and retrieving the response
		Response response = httpRequest.get("testrun/" + testCycle);
		String responseString = response.getBody().asString();
		JsonPath jsPath = new JsonPath(responseString);
		List<Object> testCaseKeys = jsPath.getList("items.testCaseKey");
		for (Object testCaseKey : testCaseKeys) {
			testStatus.put(testCaseKey.toString(), "Not Executed");
		}

		System.out.println("List of Test Cases in the Test Cycle " + testCycle + " is " + testCaseKeys);
	}

	/**
	 * @author Sikander Bhambhu
	 * Method : mapTestCaseStatusToJIRA 
	 * Description :
	 *         This method internally uses testStatus map and calls
	 *         updateJiraTestCaseStatus
	 **/
	public static void mapTestCaseStatusToJIRA() {
		for (Map.Entry<String, String> entry : testStatus.entrySet()) {
			String testCaseKey = entry.getKey();
			String testCaseStatus = entry.getValue();
			updateJiraTestCaseStatus(testCaseKey, testCaseStatus);
		}
	}

	/**
	 * Updates the test case map with assertion result
	 * @param testCaseKeyList: SMAB-T418
	 * @param testCaseStatus: Pass / Fail
	 */
	public static void updateTestCaseStatusInMap(String testCaseKeyList, String testCaseStatus) {
		if (testCycle!= null){
			String[] testCaseKeys= testCaseKeyList.split(",");
			for(String testCaseKey : testCaseKeys){
				String currentStatus = JiraAdaptavistStatusUpdate.testStatus.get(testCaseKey);
				if (currentStatus==null){
					System.out.println("Test Case Key " + testCaseKey + " is not part of Test Cycle " + testCycle);
				}else if(!(currentStatus.equalsIgnoreCase("Fail"))) {
					JiraAdaptavistStatusUpdate.testStatus.put(testCaseKey, testCaseStatus);
				}
			}
		}
	}

	/**
	 * Extracts the test case key from the argument and sets a system property with it
	 * @param message: "SMAB-T418: <Some validation message>"
	 */
	public static String extractTestCaseKey(String message) {
		return message.substring(0, message.indexOf(":")).trim();
	}
}