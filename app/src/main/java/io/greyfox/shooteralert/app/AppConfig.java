package io.greyfox.shooteralert.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AppConfig {
	// Server API url
	public static String API_URL = "http://shootingalertapp.com/shootalert/";

	// Public Variable
	public static ArrayList<ShootInfo> shootInfoList = new ArrayList<ShootInfo>();
	public static String NOTIFICATION_BROADCAST_ACTION = "com.shooteralert";
	public static String APP_NAME = "Shooter Alert";

	public static void removeData() {
		for(int i = shootInfoList.size()-1 ; i >= 0; i--){
			shootInfoList.remove(shootInfoList.get(i));
		}
	}

	//add shoot info
	public static boolean addShoot(ShootInfo info) {
		ShootInfo newItem = new ShootInfo();
		newItem.id = info.id;
		newItem.incident_date = info.incident_date;
		newItem.state = info.state;
		newItem.city = info.city;
		newItem.address = info.address;
		newItem.killed = info.killed;
		newItem.injured = info.injured;
		newItem.url1 = info.url1;
		newItem.url2 = info.url2;
		newItem.latitude = info.latitude;
		newItem.longitude = info.longitude;

		shootInfoList.add(newItem);

		return true;
	}

	public static String parseDateToddMMyyyy(String time) {
		String inputPattern = "yyyy-MM-dd";
		String outputPattern = "MMM dd, yyyy";
		SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
		SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);

		Date date = null;
		String str = null;

		try {
			date = inputFormat.parse(time);
			str = outputFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return str;
	}

	public static int[] getShootInfo(ArrayList<ShootInfo> shootInfos, int month, int year) {

		String selDate = String.format("%d-%02d", year, month);
		int[] nRes = new int[2];
		for (int i = 0; i < shootInfos.size(); i ++) {
			ShootInfo info = shootInfos.get(i);
			if (info.incident_date.contains(selDate)) {
				nRes[0] += info.killed;
				nRes[1] += info.injured;
			}
		}
		return nRes;
	}
}
