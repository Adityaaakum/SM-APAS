package com.apas.Utils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebElement;

public class DateUtil {

    /**
     * Description: This method will return the desired date from given date
     *
     * @return desirderDate: returns the desired Date from given date after adding subtracting given number of days
     * @throws ParseException
     */
    public static String getFutureORPastDate(String givenDate, int numberOfdays, String givenFormat) throws IOException, ParseException {


        SimpleDateFormat sdf = new SimpleDateFormat(givenFormat);
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(givenDate));
        c.add(Calendar.DATE, numberOfdays);
        String afterAddingDays = sdf.format(c.getTime());
	        /*System.out.println("given date is:"+givenDate);
	        System.out.println("After adding number of days to given date::"+afterAddingDays);
	        */
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");
        String desiredDate = sdf1.format(sdf.parse(afterAddingDays));
        //System.out.println("after converting the date is:"+desiredDate+"::::");

        return desiredDate;

    }


    public static String getDateInRequiredFormat(String date, String givenFormat, String expectedFormat) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(givenFormat);
        SimpleDateFormat sdf1 = new SimpleDateFormat(expectedFormat);

        Date converteddate = sdf.parse(date);

        String expectedFormatdate = sdf1.format(converteddate).toString();

        return expectedFormatdate;
    }

    /**
     * Description: This method is to determine maximum dates out of given number of dates
     *
     * @return : returns the maximum date in String format
     * @throws ParseException
     */
    public static String determineMaxDate(String... alldates) throws ParseException {
        SimpleDateFormat sdfo = new SimpleDateFormat("MM/dd/yyyy");
        Date max = sdfo.parse(alldates[0]);

        for (int i = 1; i < alldates.length; i++) {

            Date next = sdfo.parse(alldates[i]);

            if (max.compareTo(next) > 0 || max.compareTo(next) == 0) {

            } else if (max.compareTo(next) < 0) {

                max = next;
            }


        }

        return sdfo.format(max).toString();

    }

    /**
     * @param format: expected format of date, example 'MM/dd/yyyy'
     * @author Sikander Bhambhu
     * @Description: This method takes an expected format for date and return
     * the current date in the format provided.
     **/
    public static String getCurrentDate(String format) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String formattedDate = sdf.format(date);
        return formattedDate;
    }

    /**
     * Description: This method will remove leading zeroes from the month and day value in Date
     *
     * @param dateValue: Date value passed from the Json file
     * @return : returns the dateValue
     */

    public static String removeZeroInMonthAndDay(String dateValue) throws Exception {
        //boolean flag = false;
        String apnd;

        if (dateValue.charAt(0) == '0') {
            dateValue = dateValue.substring(1);
            //flag = true;

            if (dateValue.charAt(2) == '0') {
                apnd = dateValue.substring(0, 2);
                dateValue = apnd + dateValue.substring(3);
            }
        } else {
            if (dateValue.charAt(3) == '0') {
                apnd = dateValue.substring(0, 3);
                dateValue = apnd + dateValue.substring(4);
            }
        }
        return dateValue;
    }

    /**
     * @param eleStartDate: start date
     * @param eleEndDate:   end date
     * @return : returns the difference of no of days between 2 dates in "MM/dd/yyyy" format
     * @throws ParseException
     * @description: This method will return difference of no of days between 2 dates i.e. end Date - start Date
     */
    public static float getDateDiff(String eleStartDate, String eleEndDate) throws ParseException {

        String startDate = eleStartDate;
        String endDate = eleEndDate;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
        Date firstDate = null;
        Date secondDate = null;
        firstDate = sdf.parse(startDate);
        secondDate = sdf.parse(endDate);

        long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
        float diff = (TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)) + 1;
        return diff;
    }


    /**
     * Difference of dates in years.
     *
     * @param DOB Enter the Date which needs to be compared with current date
     * @return the int
     */
    public int differenceOfDatesInYears(String DOB) {
        int age = 0;
        int factor = 0;

        try {
            String dateOfBirth = DOB;
            // String currentDate = LocalDate.now().toString();
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = df.format(date);
            System.out.println(currentDate);
            Calendar cal1 = new GregorianCalendar();
            Calendar cal2 = new GregorianCalendar();
            Date date1 = new SimpleDateFormat("yyyy-MM-dd").parse(dateOfBirth);
            // Date date2 = new
            // SimpleDateFormat("yyyy-MM-dd").parse(currentDate);
            Date date2 = df.parse(currentDate);
            cal1.setTime(date1);
            cal2.setTime(date2);
            if (cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR)) {
                factor = -1;
            }
            age = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR) + factor;
            System.out.println("Your age is: " + age);
        } catch (ParseException e) {
            System.out.println(e);
        }

        return age;
    }

    public String deductDaysToDate(String dateToModify, String expectedFormat, int numberOfDaysToAdd) throws Exception {
        String dateToModifyFormatted = null;
        if (expectedFormat.contains("/")) {
            if (dateToModify.contains("/")) {
                dateToModifyFormatted = dateToModify;
            }
            if (dateToModify.contains("-")) {
                dateToModifyFormatted = dateToModify.replace("-", "/");
            }
        }
        if (expectedFormat.contains("-")) {
            if (dateToModify.contains("-")) {
                dateToModifyFormatted = dateToModify;
            }
            if (dateToModify.contains("/")) {
                dateToModifyFormatted = dateToModify.replace("/", "-");
            }
        }

        Date date = new SimpleDateFormat(expectedFormat).parse(dateToModifyFormatted);

        SimpleDateFormat sdf = new SimpleDateFormat(expectedFormat);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, -numberOfDaysToAdd);
        String newDate = sdf.format(c.getTime());
        return newDate;
    }


}
