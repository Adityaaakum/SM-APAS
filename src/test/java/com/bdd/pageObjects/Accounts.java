package com.bdd.pageObjects;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class Accounts extends Page {
	
	Logger logger = Logger.getLogger(Accounts.class);

	public Accounts(WebDriver driver) {
		
		super(driver);
		PageFactory.initElements(driver, this);
	}
	
	@FindBy(xpath="//*[@data-id='Account']")
	private WebElement tabAccount;
	
	@FindBy(xpath="//a[@title='New']")
	private WebElement lnkNew;
	
	@FindBy(xpath="//div/label/span[contains(text(),'Account Name')]//following::input")
	private WebElement txtAccountName;
	
	@FindBy(xpath="//button[@title='save']")
	private WebElement btnSave;
	
	
	public void createNewAccount(String AccountName) throws Exception {
		 
		Click(tabAccount);
		Click(lnkNew);
		enter(txtAccountName,AccountName);
		Click(btnSave);
		
	}
	
	public void clickTabAccounts() throws IOException {
		
		Click(tabAccount);		
		
	}
	
	public void enterAccountName(String AccountName) throws Exception {
		
		enter(txtAccountName,AccountName);
		
	}
	
	public void clickLnkNew() throws IOException {
		
		Click(lnkNew);
		
	}
	
	public void clickBtnSave() throws IOException {
		
		Click(btnSave);
	}
	

}
