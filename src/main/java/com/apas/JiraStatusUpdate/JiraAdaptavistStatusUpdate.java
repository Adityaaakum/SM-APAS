package com.apas.JiraStatusUpdate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.apas.TestBase.TestBase;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.specification.RequestSpecification;

public class JiraAdaptavistStatusUpdate extends TestBase {
	public static HashMap<String, String> testStatus = new HashMap<String, String>();

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
			RestAssured.baseURI = "https://tools.publicis.sapient.com/jira/rest/atm/1.0/";
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
	 * Method : retrieveJiraTestCases 
	 * Description :
	 *         This method makes the API call for fetching all the test cases
	 *         from JIRA for given test cycle and created a map with these test
	 *         cases with null values
	 **/
	public static void retrieveJiraTestCases() {
		try {
			// Storing BaseURI
			RestAssured.baseURI = "https://tools.publicis.sapient.com/jira/rest/atm/1.0/";
			RequestSpecification httpRequest = RestAssured.given();

			// setting content type to make the API call
			httpRequest.header("Content-Type", "application/json");
			httpRequest.header("Authorization", "Basic c2lrYmhhbWI6TmF2eWFAMTcyMg==");

			// passing body to the API call and retrieving the response
			Response response = httpRequest.get("testrun/" + testCycle);
			String responseString = response.getBody().asString();
			JsonPath jsPath = new JsonPath(responseString);
			List<Object> items = jsPath.getList("items");
			
			Iterator<Object> itr = items.iterator();
			while (itr.hasNext()) {
				int testCaseKeyIndex = -1;
				List<String> currentIndexValueList = Arrays.asList(itr.next().toString().split(","));
				for (int i = 0; i < currentIndexValueList.size(); i++) {
					if (currentIndexValueList.get(i).contains("testCaseKey")) {
						testCaseKeyIndex = i;
						break;
					}
				}

				String testCaseKey = currentIndexValueList.get(testCaseKeyIndex).split("=")[1];
				testStatus.put(testCaseKey, null);
			}
		} catch (Exception e) {

		}
	}

	/**
	 * @author Sikander Bhambhu
	 * Method : mapTestCaseStatusToJIRA 
	 * Description :
	 *         This method internally uses testStatus map and calls
	 *         updateJiraTestCaseStatus
	 **/
	public static void mapTestCaseStatusToJIRA() {
		try {
			for (Map.Entry<String, String> entry : testStatus.entrySet()) {
				String testCaseKey = entry.getKey();
				String testCaseStatus = entry.getValue();
				if (testCaseStatus != null) {
					updateJiraTestCaseStatus(testCaseKey, testCaseStatus);
				} else {
					updateJiraTestCaseStatus(testCaseKey, "Not Executed");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
