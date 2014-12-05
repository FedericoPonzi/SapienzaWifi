package it.uniroma1.sapienzawifi;

import it.uniroma1.sapienzawifi.util.DisconnectRequestAsyncTask;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DisconnectActivity extends ActionBarActivity {
	// Lo sharedpref:
	public static final String MY_PREFERENCES = "TEMPORARY_PREF";

	// Usate per creare l' intent
	@SuppressWarnings("unused")
	private static final String LOG_TAG = DisconnectActivity.class
			.getCanonicalName();
	public static final String NEWSYSTEM = "NuovoSistema";

	public static final int NOTIFICATION_ID = 42;

	// Per lo sharedPreferences:
	public static final String MATRICOLA = "MATRICOLA";
	public static final String IP = "IP";
	public static final String AUTHENTICATOR = "Authenticator";
	public static final String LOGOUT_URL = "LOGOUT_URL";

	// Alcuni parametri:
	private boolean nuovoSistema = true;
	private String authenticator;
	private String ip;
	private String matricola;
	private String logoutUrl;
	private DisconnectRequestAsyncTask requestToDisconnect;

	private Button mRetryBtn;
	private Button mExitBtn;
	private ProgressBar mProgressBar;
	private TextView mainTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disconnect);

		requestToDisconnect = new DisconnectRequestAsyncTask(this);

		Intent intent = getIntent();
		SharedPreferences s = getSharedPreferences(MY_PREFERENCES,
				Context.MODE_PRIVATE);

		if (intent != null) {
			// Se è il nuovo sistema, mi prendo il necessario
			nuovoSistema = intent.getBooleanExtra(NEWSYSTEM, true);
			if (nuovoSistema) {
				authenticator = s.getString(AUTHENTICATOR, null);
				ip = s.getString(IP, null);
				matricola = s.getString(MATRICOLA, null);
			}
			logoutUrl = s.getString(LOGOUT_URL, null);
			if (logoutUrl == null || ip == null || matricola == null) {
				requestToDisconnect = null;
				setDisconnectedMessage(
						getString(R.string.disconnect_null_values), true);
			} else {
				requestToDisconnect.execute(matricola, ip, authenticator);
			}
		}

		mProgressBar = (ProgressBar) findViewById(R.id.progressBar_disconnetti);
		mRetryBtn = (Button) findViewById(R.id.bottone_disconnetti);
		mainTv = (TextView) findViewById(R.id.main_tv);
		mExitBtn = (Button) findViewById(R.id.exit_btn);

		mExitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
				System.exit(0);
			}
		});
		mRetryBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (requestToDisconnect != null
						&& requestToDisconnect.getStatus() != AsyncTask.Status.RUNNING) {
					mProgressBar.setVisibility(View.VISIBLE);
					mainTv.setText(R.string.disconnecting_message);

					requestToDisconnect.execute();
					mRetryBtn.setClickable(false);
				}
			}

		});
	}

	public void setDisconnectedMessage(String text, boolean disconnesso) {
		mainTv.setText(text);
		mProgressBar.setVisibility(View.GONE);

		if (disconnesso) {
			mRetryBtn.setVisibility(View.GONE);
			mExitBtn.setVisibility(View.VISIBLE);
		} else {
			mRetryBtn.setClickable(true);
			mRetryBtn.setVisibility(View.VISIBLE);
			mExitBtn.setVisibility(View.GONE);

		}

	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public boolean getIsNuovoSistema() {
		return nuovoSistema;
	}
}
