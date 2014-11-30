package test;

import interval.Interval;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.ServiceLoader;

import model.Model;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import data.Entry;
import util.DateUtils;
import util.FileUtils;

public class Relational {
	protected Model model;
	protected SimpleDateFormat inputSDF;
	protected SimpleDateFormat outputSDF;

	public Relational() throws SQLException {
		model = new Model(DateUtils.getCalendar());
		inputSDF = new SimpleDateFormat("yyyyMMdd HH:mm");
		inputSDF.setTimeZone(DateUtils.getTimeZone());
		outputSDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		inputSDF.setTimeZone(DateUtils.getTimeZone());
	}

	public static void main(String[] args) throws FileNotFoundException {
		try {
			new Relational().start();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void start() throws Exception {
		insertFromFile();
		generateIntervals();
//		model.test(
//			DateUtils.makeTimestamp(2012, 1, 1, 14, 0, 0, 0),
//			DateUtils.makeTimestamp(2013, 7, 1, 14, 0, 0, 0), "Day");
		model.test();
	}

	public void insertFromFile() throws SQLException, IOException {
		Date endDate;
		boolean checkDate = ((endDate = model.getLastEntryDate()) != null);
		System.out.println(endDate);
		JSONParser parser = new JSONParser();
		BufferedReader bufferedReader = FileUtils.resourceToReader("pvoutput.debug", true);
		String line;
		int i = 0;
		while ((line = bufferedReader.readLine()) != null) {
			Object object = null;
			try {
				JSONArray json = (JSONArray) parser.parse(line);
				object = parser.parse(json.get(3).toString());
			} catch (ParseException e) {
				continue;
			}
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>) object;
			String value = String.valueOf(map.get("v5"));
			if (!value.equals("null")) {
				try {
					Date date = inputSDF.parse(String.format("%s %s", map.get("d"), map.get("t")));
					if (checkDate && date.compareTo(endDate) < 0) {
						break;
					}
					if (++i > 100) break;
					model.insertDataBatch(date.getTime(), Double.parseDouble(value));
				} catch (java.text.ParseException e) {
					continue;
				}
			}
		}
		bufferedReader.close();
		model.insertDataBatchLast();
	}

	public void generateIntervals() throws Exception {
		Date startDate = model.getFirstEntryDate();
		Date endDate = model.getLastEntryDate();

		for (Object object : ServiceLoader.load(interval.Interval.class)) {
			Interval interval = (Interval) object;
			interval.setDate(startDate, endDate);
			processInterval(interval);
    	}		
	}

	protected void processInterval(Interval interval) throws Exception {
		String name = interval.getClass().getSimpleName();
		model.insertInterval(name);
		int id = model.selectIntervalId(name);

		System.out.printf("[%d] %s\n", id, name);
		System.out.println("========================================");

		do {
			Date intervalStartDate = interval.get();
			Date intervalEndDate = interval.next();

			ArrayList<Entry> entryList = model.selectDataBetween(intervalStartDate, intervalEndDate);
			
			int count = entryList.size();
			if (count > 0) {
				System.out.printf("%s - %s (%d)\n", formatDate(intervalStartDate), formatDate(intervalEndDate), count);
				ArrayList<Double> valueList = new ArrayList<Double>();
				for (Entry entry : entryList) {
					System.out.printf("%s\t%f\n", formatDate(entry.date), entry.value);
					valueList.add(entry.value);
				}
				model.insertExtremesBatch(
						id,
						intervalStartDate,
						entryList.get(0).date,
						entryList.get(count - 1).date,
						count,
						Collections.min(valueList),
						Collections.max(valueList));
			}
		}
		while (interval.hasNext());
		model.insertExtremesBatchLast();
	}

	protected String formatDate(Date date) {
		return outputSDF.format(date.getTime());
	}
}
