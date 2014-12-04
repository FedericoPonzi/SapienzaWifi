package it.uniroma1.sapienzawifi;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ErrorActivity extends ActionBarActivity
{
	public final static String ERROR = "Error"; // Key
	public final static String ERRORTIMEOUT = "TimeOut";
	public final static String ERRORALREADYCONNECTED = "Gia Connesso";
	public final static String ERRORWITHLOGIN = "Wrong Login";
	public final static String ERRORWITHWIFI = "WifiDisabled";
	public final static String ERRORWRONGAPN = "Not connected to sapienza";

	TextView mTextView;
	Button mButtonRetry;
	Button mButtonSettings;
	Button mButtonWifi;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Intent received = getIntent(); // It is supposed to be never null...
		// Well, never know:
		if (received != null)
		{
			String errorType = received.getExtras().getString(ERROR);
			// Since Android uses java 1.6, i need to use else if statements.
			if (errorType.equals(ERRORALREADYCONNECTED))
			{
				setContentView(R.layout.error_already_connected);
			}
			else if (errorType.equals(ERRORTIMEOUT))
			{
				setContentView(R.layout.error_timeout);
			}
			else if (errorType.equals(ERRORWRONGAPN))
			{
				setContentView(R.layout.error_conn_to_sap);
			}
			else if (errorType.equals(ERRORWITHLOGIN))
			{
				setContentView(R.layout.error_login);
			}
			else if (errorType.equals(ERRORWITHWIFI))
			{
				setContentView(R.layout.error_wifi_disabled);
			}
		}
	}

	public void buttonWifiClicked(View view)
	{
		startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		finish();
	}
	
	public void buttonRetryClicked(View view)
	{
		Intent retry = new Intent(this, MainActivity.class);
		retry.putExtra("Retry", true);
		startActivity(retry);
	}
	public void buttonSettingsClicked(View view)
	{
		startActivity(new Intent(this,SettingsActivity.class));
	}
}
