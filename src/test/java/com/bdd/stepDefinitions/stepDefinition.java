package com.bdd.stepDefinitions;

import java.lang.reflect.Method;
import java.util.Properties;

import org.openqa.selenium.WebDriver;

import com.bdd.initialSetUp.BrowserDriver;
import com.bdd.initialSetUp.TestBase;
import com.bdd.pageObjects.Page;
import com.bdd.utils.Util;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;


public class stepDefinition  {
	
		private WebDriver driver;
		String methodName;
		String className;
		Object objConstructor;
		Method objMethod ;
	
		public void invokeMethod(String methodName) throws Exception {
			
			driver=BrowserDriver.getBrowserInstance();
			String [] arrayOFParams= methodName.split("\\.");
			this.className=arrayOFParams[0];
			this.methodName=arrayOFParams[1];
			
			Method[] objMethods;
			Object objConstructor;
			
            Class aClass = Class.forName("com.bdd.pageObjects."+this.className);
			
			objConstructor = aClass.getDeclaredConstructor(WebDriver.class).newInstance(driver);
			
			objMethods = aClass.getDeclaredMethods();
			
			for(Method m : objMethods) {
				
				if(m.getName().equals(this.methodName)) {
					
					Class [] parameterTypes = m.getParameterTypes();
					this.objMethod = aClass.getDeclaredMethod(this.methodName, parameterTypes);
				}
			}
			           
			this.objConstructor = objConstructor;								
			
		}
		
		@Given("^I navigate browser to \"([^\"]*)\"$")
		public void navigateBrowserTo(String URL) throws Exception {			
			driver=BrowserDriver.getBrowserInstance();
			Page objPage= new Page(driver);
			
			if(URL.startsWith("TestData")) {
				
				URL = URL.substring(URL.indexOf(".")+1);
				objPage.navigateTo(driver, Util.getValFromResource(URL) );
				
			}else {
				
				objPage.navigateTo(driver, URL);
			}
				
		}
		
		@When("^I enter the \"([^\"]*)\" as \"([^\"]*)\"$")
		public void enterThe(String methodName , String parameter) throws Exception {	
			invokeMethod(methodName);
			
			if(parameter.startsWith("TestData")) {
				
				parameter = parameter.substring(parameter.indexOf(".")+1);
				objMethod.invoke(objConstructor,Util.getValFromResource(parameter));
				
			}else {
				
				objMethod.invoke(objConstructor,parameter);
				
			}
			
		}
		
		@When("^I click the \"([^\"]*)\"$")
		public void clickThe(String methodName) throws Exception {
			invokeMethod(methodName);
            objMethod.invoke(objConstructor); 
			
		}
		
		@Then("^I verify the \"([^\"]*)\"$")
		public void verifyThe(String methodName) throws Exception {
			invokeMethod(methodName);
            objMethod.invoke(objConstructor);
			
		}
		

}
