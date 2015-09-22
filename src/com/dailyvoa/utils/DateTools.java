package com.dailyvoa.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.TextUtils;

public class DateTools {

	public static String getToday() {
		String today = "";
		Date date = new Date();
		Date dateyesterday = new Date(date.getTime()-3600*24*1000);
		DateFormat df = DateFormat.getDateInstance();
		return df.format(dateyesterday); // yyyy-[m]m-[d]d 
	}

	public static String getMonth() {
		Date date = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
		return df.format(date);
	}

}
