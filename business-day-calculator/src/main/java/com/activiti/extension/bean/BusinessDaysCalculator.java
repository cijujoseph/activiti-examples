package com.activiti.extension.bean;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.activiti.engine.delegate.BpmnError;
import org.activiti.engine.impl.util.json.JSONArray;
import org.activiti.engine.impl.util.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("businessDaysCalculator")
public class BusinessDaysCalculator {

	private DateFormat dateFormat = new SimpleDateFormat("d-M-yyyy");
	
	private static ArrayList<String> HOLIDAY_LIST = new ArrayList<String>();

	protected static final Logger log = LoggerFactory
			.getLogger(BusinessDaysCalculator.class);

	public BusinessDaysCalculator() {
		super();
		try {
			String holidays = IOUtils.toString(getClass().getResourceAsStream(
					"/public-holidays-usa.json"));
			JSONArray jsonArray = new JSONArray(holidays);
			//load a couple of years data into holiday list
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);			
			int nextYear = currentYear + 1;
			for (int i = 0, size = jsonArray.length(); i < size; i++) {
				JSONObject dateObject = jsonArray.getJSONObject(i)
						.getJSONObject("date");
				int year = (int) dateObject.get("year");
				if (year == currentYear || year == nextYear) {
					int day = (int) dateObject.get("day");
					int month = (int) dateObject.get("month");
					String date = day + "-" + month + "-" + year;

					HOLIDAY_LIST.add(date);
				}
			}
		} catch (IOException e) {
			log.error("Error initialising BusinessDaysCalculator",
					e.getMessage());
		}
	}

	public Date getDueDateUsingBusinessDays(Integer numberOfDays)
			throws ParseException, IOException {

		
		Calendar cal = Calendar.getInstance();
		int today = cal.get(Calendar.DAY_OF_WEEK);
		Date todayDate = cal.getTime();
		
		// iterate over the dates from now and check if each days is a business
		// day
		int businessDayCounter = 0;
		while (businessDayCounter < numberOfDays) {
			cal.add(Calendar.DAY_OF_YEAR, 1);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek != Calendar.SATURDAY
					&& dayOfWeek != Calendar.SUNDAY
					&& !HOLIDAY_LIST.contains(dateFormat.format(cal.getTime()))) {				
				businessDayCounter++;
			}
		}

		//If today is a holiday, set the time of due date to 23:59:59 to allow one complete working day
		if (today == Calendar.SATURDAY
				|| today == Calendar.SUNDAY
				|| HOLIDAY_LIST.contains(dateFormat.format(todayDate))) {
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		} else {
			return cal.getTime();
		}

	}


	public Object businessDueDateCalculator(String dueDate) {
		if (dueDate.startsWith("P") && dueDate.endsWith("D")) {
			int numberOfBusinessDays = Period.parse(dueDate).getDays();			
			try {
				Date newDueDate = getDueDateUsingBusinessDays(numberOfBusinessDays);
				return newDueDate;
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				// TODO Auto-generated catch block
				throw new BpmnError("ERROR_CALCULATING_BUSINESS_DUE_DATE",
						"Error Calculating Business Due Date:" + e.getMessage());
			}
		} else {
			return dueDate;
		}
	}
}
