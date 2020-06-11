package com.apas.Utils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	
	/**
	 * Description: This method will return the desired date from given date 
	 *@return desirderDate: returns the desired Date from given date after adding subtracting given number of days
	 * @throws ParseException 
	 */
	 public static String getFutureORPastDate(String givenDate, int numberOfdays,String givenFormat) throws IOException, ParseException {
		 
		 
		 	SimpleDateFormat sdf = new SimpleDateFormat(givenFormat);
	    	Calendar c = Calendar.getInstance();
	        c.setTime(sdf.parse(givenDate));
	        c.add(Calendar.DATE, numberOfdays);
	        String afterAddingDays=sdf.format(c.getTime());
	        /*System.out.println("given date is:"+givenDate);
	        System.out.println("After adding number of days to given date::"+afterAddingDays);
	        */
	    	SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");
	    	String desiredDate = sdf1.format(sdf.parse(afterAddingDays));
	    	//System.out.println("after converting the date is:"+desiredDate+"::::");
		
			return desiredDate;
			
	 }

	 

public static String getDateInRequiredFormat(String date,String givenFormat,String expectedFormat) throws ParseException
	{
	SimpleDateFormat sdf = new SimpleDateFormat(givenFormat);
	SimpleDateFormat sdf1 = new SimpleDateFormat(expectedFormat);
	
	Date converteddate = sdf.parse(date);
	
	String expectedFormatdate=sdf1.format(converteddate).toString();
	
	return expectedFormatdate;
	}

/**
 * Description: This method is to determine maximum dates out of given number of dates 
 * @param alldates...: n number of date
 * @return : returns the maximum date in String format
 * @throws ParseException 
 */


public static String determineMaxDate(String... alldates) throws ParseException
{
	SimpleDateFormat sdfo = new SimpleDateFormat("MM/dd/yyyy");
	 Date max = sdfo.parse(alldates[0]);
	
	 for(int i=1;i<alldates.length;i++)
	 {
	
	 Date next = sdfo.parse(alldates[i]);
	 
	 if (max.compareTo(next) > 0 || max.compareTo(next) == 0) {
		 
		} 
	 else if (max.compareTo(next) < 0) { 
		
		  max=next;
       } 
 
        
	 }
	
	return sdfo.format(max).toString();

}



}
