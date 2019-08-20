package com.bdd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
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
import com.sun.mail.iap.Response;

public class EmailDriver {
	
	private static String EnvironmentURL = null;
	
	public static Store emailSession;
	
	public Store readEmail(String host, String userName, String Password ){	
		
		Properties props = new Properties();
	    props.setProperty("mail.imap.port", "993");
		props.setProperty("mail.store.protocol", "imap");
		props.put("mail.imap.ssl.enable", "true");
		props.put("mail.imap.socketFactory.class","javax.net.ssl.SSLSocketFactory");
		props.put("mail.imap.socketFactory.fallback", "false");      
		
        try {
        	
        	Session session = Session.getInstance(props, null);
            emailSession = session.getStore("imap");       
            emailSession.connect(Util.getValFromResource(host), Util.getValFromResource(userName),
					             Util.getValFromResource(Password));
        }catch (Exception e) {
            e.printStackTrace();
        }
		return emailSession;
		
	}
	
	
}
