package com.cos.cmtgp.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class DateUtil {
	
	public static String getDateYDMHMS() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
	}
	
	/**
	 * 月
	 * <p>
	 * Title: getMonth
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * @return
	 * @throws ParseException 
	 */
	public static int getMonth(String date) throws ParseException {
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		Date d = df.parse(date);
		Calendar cal=Calendar.getInstance();
		cal.setTime(d);
		int month = cal.get(Calendar.MONTH) + 1;
		return month;
	}

	public static int getYear(String date) throws ParseException {
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		Date d = df.parse(date);
		Calendar cal=Calendar.getInstance();
		cal.setTime(d);
		int year = cal.get(Calendar.YEAR);
		return year;
	}
	
	public static int getDay(String date) throws ParseException {
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		Date d = df.parse(date);
		Calendar cal=Calendar.getInstance();
		cal.setTime(d);
		int day = cal.get(Calendar.DATE);
		return day;
	}
	
	public static String getDay(Date date) throws ParseException {
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		return df.format(date);
	}

	public static String getDayToString(Date date) throws ParseException {
		SimpleDateFormat df=new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
		return df.format(date);
	}
	
	public static long getDayStr(String date) {
		long lnum = 0;
		try {
			SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
			lnum = df.parse(date).getTime();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lnum;
	}
	
	/**
	 * 将"2015-08-31 21:08:06"型字符串转化为Date
	 * @param str
	 * @return
	 * @throws ParseException
	 */
	public static Date StringToDate(String str) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date date = (Date) formatter.parse(str);
		return date;
	}

	public static Date getStringToDate(String str) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = (Date) formatter.parse(str);
		return date;
	}
	
	public static String getDate(String Date) {
		String dateFormat = null;
		try {
			int y = getYear(Date);
			int m = getMonth(Date);
			int d = getDay(Date);
			if(d>25) {
				m++;
			}
			if(m>12) {
			  y++;	
			  m =1;
			}
			dateFormat = y+"";
			dateFormat += m>9?m:"0"+m;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateFormat;
	}
	
	/**
	 * 两个日期相减得到的天数
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public static int getDiffDays(Date beginDate, Date endDate) {
		if(beginDate==null||endDate==null) {
			throw new IllegalArgumentException("getDiffDays param is null!");
		}
		long diff=(endDate.getTime()-beginDate.getTime())/(1000*60*60*24);
		int days = (int)diff;
		return days;
	}
	
	/**
	 * 实现日期增加n（0/1/2/3/4 天/周/旬/月/季）
	 * <p>
	 * Title: addDay
	 * </p>
	 * <p>
	 * Description:
	 * </p>
	 * @param n
	 * @return
	 */
	public static Date addDay(Date date, int n, int itype) {
		Calendar cd = Calendar.getInstance();
		cd.setTime(date);
		switch (itype) {
			case 0:
				cd.add(Calendar.DATE, n);// 增加天
				break;
			case 1:
				cd.add(Calendar.DATE, n * 7);// 增加周
				break;
			case 2:
				cd.add(Calendar.DATE, n * 10);// 增加旬
				break;
			case 3:
				cd.add(Calendar.MONTH, n);// 增加月
				break;
			case 4:
				cd.add(Calendar.MONTH, n * 3);// 增加季度
				break;
			case 5:
				cd.add(Calendar.HOUR, n);// 增加时
				break;
			case 6:
				cd.add(Calendar.MINUTE, n);// 增加分
				break;
			case 7:
				cd.add(Calendar.SECOND, n);// 增加秒
		}
		return cd.getTime();
	}

	public static void main(String[] args) {

	}
}
