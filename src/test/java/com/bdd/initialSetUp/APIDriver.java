package com.bdd.initialSetUp;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class APIDriver {
	
	/**
	 * Function will return the response from the API.
	 *
	 * @param Send the request URL
	 * @return the response
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getResponse(String request) throws ClientProtocolException, IOException{
		
		String apiOutput=null;
		
		HttpClient httpClient = new DefaultHttpClient();		
		HttpGet getRequest = new HttpGet(request);
		
		getRequest.addHeader("Authorization", "Basic  ZGlnaXRhbHBvcnRhbEBkcGcuY29tOmZkREhhc2RmRDVkQnRQcSh0TEh4SUkkNztk");
		getRequest.addHeader("content-type", "application/json");
		
		HttpResponse response = httpClient.execute(getRequest);
        
        HttpEntity httpEntity = response.getEntity();
        
        apiOutput = EntityUtils.toString(httpEntity);
       
		return apiOutput;		
	}
	
	/**
	 * Post request.
	 *
	 * @param reqJSON the request JSON
	 * @param APIURI the apiuri
	 * @return the string
	 * @throws ClientProtocolException the client protocol exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws JSONException the JSON exception
	 */
	public String postRequest(String reqJSON,String APIURI) throws ClientProtocolException, IOException {
		
		String apiOutput=null;
		
		HttpClient httpClient = new DefaultHttpClient();		
        HttpPost postRequest = new HttpPost(APIURI); 
        
        StringEntity params =new StringEntity(reqJSON);
            
        postRequest.addHeader("Authorization", "Basic  ZGlnaXRhbHBvcnRhbEBkcGcuY29tOmZkREhhc2RmRDVkQnRQcSh0TEh4SUkkNztk");
        postRequest.addHeader("content-type", "application/json");
        
        postRequest.setEntity(params);
        
        HttpResponse response = httpClient.execute(postRequest);
        
        HttpEntity httpEntity = response.getEntity();
        apiOutput = EntityUtils.toString(httpEntity);
      
		return apiOutput;
	}

}
