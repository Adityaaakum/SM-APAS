package com.bdd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.util.IOUtils;

import com.sendgrid.Attachments;
import com.sendgrid.Content;
import com.sendgrid.Email;
import com.sendgrid.Mail;
import com.sendgrid.Method;
import com.sendgrid.Personalization;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;

public class sendResultEmail {
	
	private String EnvironmentURL = null;

	public void sendResultEmail(String resultFile,String Recipients) throws IOException {

		   try {
			    String strArray[]= resultFile.split("AutomationReport");
			    String strUPArray[]= strArray[1].split("_");
			    String strLatestArray[]= strUPArray[2].split("\\.");	    
		     
			     //This mail has 2 part, the BODY and the embedded image
	             MimeMultipart multipart = new MimeMultipart("mixed");

	            // first part (the html)
	            BodyPart messageBodyPart = new MimeBodyPart();
	         
	            MimeBodyPart htmlPart = new MimeBodyPart();
		        DataSource source = new FileDataSource(resultFile);
		        htmlPart.setDataHandler(new DataHandler(source));  
		        String strResultArray[]= resultFile.split("AutomationReport");
		        htmlPart.setFileName(strResultArray[1]);  
		        htmlPart.setHeader("Content-ID","file");
		        multipart.addBodyPart(htmlPart);
		        
	         //Set key values
		     String upFileName= resultFile.replace("//", "\\");
		     upFileName="\\"+ upFileName;
		     System.out.println(upFileName);
	         Map<String, String> input = new HashMap<String, String>();
	         input.put("suite", strUPArray[1]);
	         input.put("browser", strLatestArray[0].toUpperCase());
	         input.put("environment", EnvironmentURL);
	         input.put("rsFile", upFileName);
	         
	            Attachments attachments = new Attachments();
	            attachments.setContent(convertToBase64(".\\src\\test\\resources\\ImageFiles\\logonew.jpg"));
	            attachments.setType("image/jpg");
	            attachments.setFilename("logo.jpg");
	            attachments.setDisposition("inline");
	            attachments.setContentId("<logo>");
	            
	            Attachments attachments1 = new Attachments();
             attachments1.setContent(convertToBase64(".\\src\\test\\resources\\ImageFiles\\service-portal-new.jpg"));
	            attachments1.setType("image/jpg");
	            attachments1.setFilename("ServicePortalBanner.jpg");
	            attachments1.setDisposition("inline");
	            attachments1.setContentId("<banner>");
	         
	        String htmlText = readEmailFromHtml(".\\src\\test\\resources\\ResultEmail.html",input);	         
	        Personalization personalization = new Personalization();
	 		Mail mail = new Mail();
	 		Email from = new Email("dpropertieschangepassword@sapient.com");
	         String subject = "Automation Result on Environment-" +EnvironmentURL;
	         String Recipents= Util.getValFromResource(Recipients);
	         String RecipentsArray[]= Recipents.split(",");
	         
	         for(int i=0;i<RecipentsArray.length;i++){
	        	 
	        	 Email to = new Email(RecipentsArray[i]);
	        	 personalization.addTo(to);
	         }
	         mail.setFrom(from);
	         mail.setSubject(subject);

	   
	      Content content = new Content("text/html", htmlText);
	     // Mail mail = new Mail(from, subject, to, content);
	      mail.addPersonalization(personalization);
	      mail.addContent(content);
	      Content content1 = new Content();
	         content1.setType("image/png");
	         content1.setValue(".\\src\\test\\resources\\ImageFiles\\logonew.jpg");
	         mail.addContent(content1);
	       
	         mail.addAttachments(attachments);
	         mail.addAttachments(attachments1);
	            
	      SendGrid sg = new SendGrid("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");
	      Request request = new Request();
	      
	      
	      try {
	          request.setMethod(Method.POST);
	          request.setEndpoint("mail/send");
	          request.setBody(mail.build());
	          
	          com.sendgrid.Response response = sg.api(request);
	
	        } 
	        catch (IOException ex) {
	          throw ex;
	        }
	            messageBodyPart.setContent(htmlText, "text/html");
	            multipart.addBodyPart(messageBodyPart); 	                      	            
	            
	      		  //second part (the image)
		         MimeBodyPart imagePart = new MimeBodyPart();
		         DataSource fds1 = new FileDataSource
		         (".\\src\\test\\resources\\ImageFiles\\logonew.jpg");
		         imagePart.setDataHandler(new DataHandler(fds1));
		         imagePart.addHeader("Content-ID","<image1>");
		         imagePart.setDisposition(MimeBodyPart.INLINE);
		         multipart.addBodyPart(imagePart);
		         	         
			    System.out.println("Email Sent");
			   } catch (MessagingException e) {
			    throw new RuntimeException(e);
			   }			   	
	}
	
	public String convertToBase64(String filepath) {
  File file = new File(filepath);
   byte[] fileData = null;
   try {
       fileData = IOUtils.toByteArray(new FileInputStream(file));
   } catch (IOException ex) {
   }
   Base64 x = new Base64();
   String imageDataString = x.encodeAsString(fileData);
		 
		return imageDataString; 	 
}
	
	//Method to replace the values for keys
	protected String readEmailFromHtml(String filePath, Map<String, String> input)
	{
	      String msg = readContentFromFile(filePath);
	      try{
	    	  Set<Entry<String, String>> entries =input.entrySet();
	          for(Map.Entry<String, String> entry : entries) {
	        	  msg = msg.replace(entry.getKey().trim(),entry.getValue().trim());
	        	  }
	          }catch(Exception exception)
	          {
	              exception.printStackTrace();
	          }
	      return msg;
	}
	
	//Method to read HTML file as a String 
	private String readContentFromFile(String fileName)
	{
	   StringBuffer contents = new StringBuffer();
	
	   try {
	   //use buffering, reading one line at a time
	        BufferedReader reader =new BufferedReader(new FileReader(fileName));
	         try {
	             String line = null; 
	             while (( line = reader.readLine()) != null){
	             contents.append(line);
	             contents.append(System.getProperty("line.separator"));
	              }
	             }finally {
	                reader.close();
	             }
	        } catch (IOException ex){
	          ex.printStackTrace();
	         }
	 return contents.toString();
	}


}
