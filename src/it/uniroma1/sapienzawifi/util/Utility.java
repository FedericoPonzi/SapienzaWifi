package it.uniroma1.sapienzawifi.util;

import it.uniroma1.sapienzawifi.DisconnectActivity;
import it.uniroma1.sapienzawifi.ErrorActivity;
import it.uniroma1.sapienzawifi.MainActivity;
import it.uniroma1.sapienzawifi.R;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;


public class Utility
{
	// Alcune costanti:
	public static final String APSSID = "sapienza";
	public static final int CONNECTION_TIMEOUT = 60000;
	private final static long[] mVibratePattern = { 0, 200, 200, 300 };

	/**
	 * Error with the wifi.
	 * 
	 * @param flag
	 */
	public static void wifiError(int flag, MainActivity mainActivity)
	{
		Intent errorActivity = new Intent(mainActivity, ErrorActivity.class);
		switch (flag)
		{
			case 0:
				errorActivity.putExtra(ErrorActivity.ERROR, ErrorActivity.ERRORWITHWIFI);
				break;
			case 1:
				errorActivity.putExtra(ErrorActivity.ERROR, ErrorActivity.ERRORWRONGAPN);
				break;
		}

		mainActivity.startActivity(errorActivity);
		mainActivity.finish();
	}

	/**
	 * Gets the wifi ip.
	 * 
	 * @return String ip
	 */
	public static String getWifiIp(Activity act)
	{
		WifiManager wifiManager = (WifiManager) act.getSystemService(Context.WIFI_SERVICE);
		int ip = wifiManager.getConnectionInfo().getIpAddress();
		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
	}

	/**
	 * This method is used to set the key-value of the login form.
	 * 
	 * @param boolean newSystem - If this is the ZeroShell system
	 * @param username
	 * @param password
	 * @return
	 */
	public static ArrayList<NameValuePair> getNameValuesWifi(boolean newSystem, String username, String password, String wifiIp)
	{
		ArrayList<NameValuePair> nameValue = new ArrayList<NameValuePair>();
		if (newSystem)
		{
			// Setting new Wifi post params:
			nameValue.add(new BasicNameValuePair("Section", "CPAuth"));
			nameValue.add(new BasicNameValuePair("Action", "Authenticate"));
			nameValue.add(new BasicNameValuePair("ZSCPRedirect", ":::http://sapienzawireless.uniroma1.it"));
			nameValue.add(new BasicNameValuePair("Powered", "Powered by ZeroShell - Infosapienza Ufficio Telecomunicazioni"));
			nameValue.add(new BasicNameValuePair("RND", Double.toString(Math.random())));
			nameValue.add(new BasicNameValuePair("Popup", "yes"));
			nameValue.add(new BasicNameValuePair("Realm", "uniroma1.it"));
			nameValue.add(new BasicNameValuePair("U", username));
			nameValue.add(new BasicNameValuePair("P", password));
		}
		else
		{
			// Settings old wifi post params:
			nameValue.add(new BasicNameValuePair("_FORM_SUBMIT", "1"));
			nameValue.add(new BasicNameValuePair("which_form", "reg"));
			nameValue.add(new BasicNameValuePair("destination", ""));
			nameValue.add(new BasicNameValuePair("source", wifiIp));
			nameValue.add(new BasicNameValuePair("bs_name", username));
			nameValue.add(new BasicNameValuePair("bs_password", password));

		}
		return nameValue;
	}

	public static ArrayList<NameValuePair> getNameValuesDisconnet(String matricola, String ip, String authenticator)
	{

		ArrayList<NameValuePair> nameValue = new ArrayList<NameValuePair>();

		nameValue.add(new BasicNameValuePair("Section", "CPGW"));
		nameValue.add(new BasicNameValuePair("Action", "Disconnect"));
		nameValue.add(new BasicNameValuePair("U", matricola));
		nameValue.add(new BasicNameValuePair("Realm", "uniroma1.it"));
		nameValue.add(new BasicNameValuePair("IP", ip));
		nameValue.add(new BasicNameValuePair("Authenticator", authenticator));
		nameValue.add(new BasicNameValuePair("Powered", "Powered+by+ZeroShell+-+Infosapienza+Ufficio+Telecomunicazioni"));
		nameValue.add(new BasicNameValuePair("DisconnectButton", "Disconnetti"));

		return nameValue;
	}


	/**
	 * Once connected, it sets the Disconnect Notification. This method is used
	 * from both the system.
	 * 
	 * @param logOutUrl
	 */
	public static void setDisconnectNotification(String logOutUrl, MainActivity mActivity, boolean nuovoSistema)
	{

		Intent disconnectIntent = new Intent(mActivity, DisconnectActivity.class);
		disconnectIntent.putExtra(DisconnectActivity.LOGOUT_URL, logOutUrl);
		disconnectIntent.putExtra(DisconnectActivity.NEWSYSTEM, nuovoSistema);

		PendingIntent pIntent = PendingIntent.getActivity(mActivity, 0, disconnectIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mActivity).setSmallIcon(R.drawable.ic_launcher).setContentTitle(mActivity.getString(R.string.disconnect_notification_title)).setContentIntent(pIntent).setAutoCancel(true).setContentText(mActivity.getString(R.string.disconnect_notification_message)).setVibrate(mVibratePattern);
		NotificationManager mNotifyMgr = (NotificationManager) mActivity.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyMgr.notify(DisconnectActivity.NOTIFICATION_ID, mBuilder.build());

	}
}
