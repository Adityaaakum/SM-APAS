package com.bdd.pageObjects;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginSFX extends Page {
	
	Logger logger = Logger.getLogger(LoginSFX.class);

	public LoginSFX(WebDriver driver) {
		
		super(driver);
		PageFactory.initElements(driver, this);
	}
	
	@FindBy(xpath="//input[@id='username']")
	private WebElement txtuserName;
	
	@FindBy(xpath="//input[@id='password']")
	private WebElement txtpassWord;
	
	@FindBy(xpath="//input[@id='Login']")
    private WebElement btnSubmit;
    
    
	public void loginIntoApp(String Username, String Password) throws Exception{
		
		enter(txtuserName,Username);
		enter(txtpassWord,Password);
		Click(btnSubmit);
	}
	
   public void enterTxtUserName(String UserName) throws Exception {
	   
	   enter(txtuserName,UserName);
	   
   }
   
   public void enterTxtPassword(String Password) throws Exception {
	   
	   enter(txtpassWord,Password);
	   
   }
   
   public void clickBtnSubmit() throws IOException {
	   
	   Click(btnSubmit);
   }
}
