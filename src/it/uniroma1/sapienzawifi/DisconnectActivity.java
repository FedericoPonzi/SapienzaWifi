package it.uniroma1.sapienzawifi;

import it.uniroma1.sapienzawifi.util.Utility;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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
	private RequestToDisconnect requestToDisconnect;

	private Button mRetryBtn;
	private Button mExitBtn;
	private ProgressBar mProgressBar;
	private TextView mainTv;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_disconnect);

		requestToDisconnect = new RequestToDisconnect();

		Intent intent = getIntent();
		SharedPreferences s = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);

		if (intent != null)
		{
			// Se è il nuovo sistema, mi prendo il necessario
			nuovoSistema = intent.getBooleanExtra(NEWSYSTEM, true);
			if (nuovoSistema)
			{
				authenticator = s.getString(AUTHENTICATOR, null);
				ip = s.getString(IP, null);
				matricola = s.getString(MATRICOLA, null);
			}
			logoutUrl = s.getString(LOGOUT_URL, null);
			if (logoutUrl == null || ip == null || matricola == null)
			{
				requestToDisconnect = null;
				// TODO: devi per forza disconnetterti a mano.
			}
			else
			{
				requestToDisconnect.execute();
			}
		}

		mProgressBar = (ProgressBar) findViewById(R.id.progressBar_disconnetti);
		mRetryBtn = (Button) findViewById(R.id.bottone_disconnetti);
		mainTv = (TextView) findViewById(R.id.main_tv);
		mExitBtn = (Button) findViewById(R.id.exit_btn);

		mExitBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				finish();
				System.exit(0);
			}
		});
		mRetryBtn.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View arg0)
			{
				if (requestToDisconnect != null && requestToDisconnect.getStatus() != AsyncTask.Status.RUNNING)
				{
					mProgressBar.setVisibility(View.INVISIBLE);
					mainTv.setText(R.string.disconnecting_message);
					
					requestToDisconnect.execute();
					mRetryBtn.setClickable(false);
				}
			}

		});
	}

	/**
	 * AsyncTask per richiedere la disconnessione dalla rete wireless della
	 * sapienza. Prende la stringa di logout salvata al momento del login, ed
	 * invia una richiesta GET per terminare la sessione.
	 * 
	 */

	private class RequestToDisconnect extends AsyncTask<Void, Void, String> {
		AndroidHttpClient mClient;

		ResponseHandler<String> handler;

		@Override
		protected void onPreExecute() {
			mClient = AndroidHttpClient.newInstance("");
			handler = new BasicResponseHandler();
			HttpProtocolParams.setUserAgent(mClient.getParams(), "Android");

		}

		@Override
		protected String doInBackground(Void... params) {
			String url = logoutUrl;
			Log.i("App", "Logout url: " + url);
			try {
				if (nuovoSistema) // TODO: controllare se worka.
				{
					HttpPost post = new HttpPost(url);
					post.setEntity(new UrlEncodedFormEntity(Utility
							.getNameValuesDisconnet(matricola, ip,
									authenticator)));

					return mClient.execute(post, handler);

				} else {
					HttpGet get = new HttpGet(url);
					return mClient.execute(get, handler);
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException exception) {
				exception.printStackTrace();
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (null != mClient)
				mClient.close();

			if (result != null && result.contains("logged out")
					|| result.contains("<script>window.close();</script>")) {
				setDisconnectedMessage(
						getString(R.string.disconnect_successful), true);

			} else {
				mRetryBtn.setClickable(true);
				setDisconnectedMessage(getString(R.string.disconnect_failed),
						false);
			}
		}
	}

	private void setDisconnectedMessage(String text, boolean disconnesso) {
		mainTv.setText(text);
		mProgressBar.setVisibility(View.INVISIBLE);

		if (disconnesso) {
			mRetryBtn.setVisibility(View.GONE);
			mExitBtn.setVisibility(View.VISIBLE);
		}

	}
}
