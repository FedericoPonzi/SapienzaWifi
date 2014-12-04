package it.uniroma1.sapienzawifi;

import it.uniroma1.sapienzawifi.util.ConnectRequest;
import it.uniroma1.sapienzawifi.util.Utility;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends ActionBarActivity
{
	private final static String LOG_TAG = MainActivity.class.getCanonicalName();
	private static final boolean DEBUG = true;


	SharedPreferences preferenze; // To access local saved settings

	private ConnectRequest requestToConnect;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		preferenze = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		requestToConnect = new ConnectRequest(this);
		// Se non sono presenti matricola o password, rimando alla settings
		// activity:
		if (!preferenze.contains("matricola") || !preferenze.contains("password"))
		{
			if (DEBUG)
			    Log.i(LOG_TAG, "O matricola O password assenti: Matricola: " + preferenze.contains("matricola") + ", Password:" + preferenze.contains("password"));
			startActivity(new Intent(this, SettingsActivity.class));
			finish();
		}

		// Setto i Listener:
		Button aboutButton = (Button) findViewById(R.id.button_about);
		Button settingsButton = (Button) findViewById(R.id.button_settings);

		aboutButton.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				builder.setCustomTitle(li.inflate(R.layout.about_dialog_title, null)).setView(li.inflate(R.layout.about_dialog, null));
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});

		settingsButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(MainActivity.this, SettingsActivity.class));
			}
		});

		// Se ha cambiato orientazione, controllo se la richiesta non era gia'
		// stata inviata.
		if (savedInstanceState != null)
		{
			if (requestToConnect != null && (requestToConnect.getStatus() != AsyncTask.Status.RUNNING))
			{
				requestToConnect.execute();
			}
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		// Se il wifi è attivato:
		if (wifiManager != null && wifiManager.isWifiEnabled())
		{
			WifiInfo info = wifiManager.getConnectionInfo();
			// Controllo che l'ssd sia quello della sapienza
			if (!info.getSSID().contains(Utility.APSSID))
			{
				if (DEBUG) Log.v(LOG_TAG, info.getSSID());
				Utility.wifiError(1, this);
			}
			else
			{
				if ((requestToConnect != null) && (requestToConnect.getStatus() != AsyncTask.Status.RUNNING))
				{
					if (DEBUG)
					    Log.i(LOG_TAG, "Sto inviando la richiesta da OnResume");
					requestToConnect.execute(preferenze.getString("matricola", "00"), preferenze.getString("password", "00"), Utility.getWifiIp(this));
				}
			}
		}
		else
		{
			Utility.wifiError(0, this);
		}
	}

}
