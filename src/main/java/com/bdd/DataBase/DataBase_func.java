package com.bdd.DataBase;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;


public class DataBase_func {

	/**
	 * Create a DataBase connection
	 * 
	 * @return connection
	 * @throws IOException 
	 */

	public static Connection getDataBaseConnection(String url, String dbName, String driver, String userName,
			String password)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException, IOException {

		Connection conn = null;
		Class.forName(driver).newInstance();
		conn = DriverManager.getConnection(url + ";" + "databaseName=" + dbName, userName, password);
		return conn;

	}
	
	/**
	 * Function will execute the sql query in database.
	 * 
	 * @param sqlQuery
	 *            the sql query
	 * @param conn
	 *            the conn
	 * @throws SQLException
	 *             the SQL exception
	 */
	public void executeDatabaseQuery(String sqlQuery, Connection conn) throws SQLException {
		System.out.println(sqlQuery);
		Statement sta = conn.createStatement();
		sta.executeUpdate(sqlQuery);
	}

	/**
	 * Function will return the field value from the database.
	 * 
	 * @param sqlQuery
	 *            the sql query
	 * @param column
	 *            the column for which value has to get
	 * @param conn
	 *            the conn
	 * @return the field value
	 * @throws SQLException
	 *             the SQL exception
	 */
	public String getColumnValue(String sqlQuery, String column, Connection conn) throws SQLException {
		String temp = null;
		System.out.println(sqlQuery);
		Statement sta = conn.createStatement();
		ResultSet rs = sta.executeQuery(sqlQuery);
		if (rs.next()) {

			temp = rs.getString(column);
				}
		
		return temp;
	}
	
	
	/**
	 * Function will return the list of values from the database with respect to sql
	 * query.
	 *
	 * @param sqlQuery
	 *            the sql query
	 * @param conn
	 *            the conn
	 * @return the list from database
	 * @throws SQLException
	 *             the SQL exception
	 */
	public List<String> getListFrmDatabase(String sqlQuery, Connection conn) throws SQLException {
		List<String> listOfValues = new ArrayList<String>();
		System.out.println(sqlQuery);
		Statement sta = conn.createStatement();
		ResultSet rs = sta.executeQuery(sqlQuery);
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		if(rs.getFetchSize() == 0)
		{
			System.out.println("No value in DB hence failed test case");
		}
		
		 
		while (rs.next()) {
			String upColumnValue = null;
			String replaceString;
			for (int i = 1; i <= columnCount; i++) {
				
				int type = rsmd.getColumnType(i);
			
				if (type == Types.VARCHAR || type == Types.CHAR||type==Types.DECIMAL||type==Types.BIT) {
					String columnValue = rs.getString(i).trim();
					upColumnValue = upColumnValue + " " + columnValue;
	            }
				else if(type == Types.DATE){
					
					 java.sql.Date date=rs.getDate(rsmd.getColumnName(i));
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					String columnValue = sdf.format(date);
					upColumnValue = upColumnValue + " " + columnValue;
				}				
			}
			replaceString = upColumnValue.replace("null", "");
			upColumnValue = (replaceString.toLowerCase()).trim();
			listOfValues.add(upColumnValue);
		}		
		return listOfValues;

	}
}	
		

