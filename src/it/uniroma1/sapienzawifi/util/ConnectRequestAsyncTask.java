package it.uniroma1.sapienzawifi.util;

import it.uniroma1.sapienzawifi.DisconnectActivity;
import it.uniroma1.sapienzawifi.ErrorActivity;
import it.uniroma1.sapienzawifi.MainActivity;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;


/**
 * Questa classe permette di: 1. Capire se l' utente si trova sulla vecchia o
 * nuova rete. 2. Inviare una richiesta eseguendo il login alla rete. Nota: Nel
 * caso si tratti del nuovo sistema, questa classe non è sufficiente per il
 * login: verrà inviata un' altra richiesta utilizzando la classe
 * {@link GWRequestAsyncTask} Il risultato di
 * {@link RequestToConnect#doInBackground(Void...)} è una stringa creata con il
 * contenuto della pagina che ha ricevuto in risposta alla richiesta.
 * 
 * @author FedericoPonzi
 */

public class ConnectRequestAsyncTask extends AsyncTask<String, Void, String>
{
	private static final String LOG_TAG = ConnectRequestAsyncTask.class.getCanonicalName();

	private static final boolean DEBUG = true;

	// Connection setup:
	AndroidHttpClient mClient = AndroidHttpClient.newInstance("");
	HttpPost post;
	ResponseHandler<String> responseHandler;

	// Access data:
	String username;
	String password;
	String wifiIp;

	MainActivity mActivity;
	boolean nuovoSistema;
	Intent errorActivity;
	// Url for disconnection:
	String urlForDisconnection;
	private HttpParams httpParams; // Some httpparams, same for all requests.


	public ConnectRequestAsyncTask(MainActivity mainActivity)
	{
		super();
		mActivity = mainActivity;
		errorActivity = new Intent(mainActivity, ErrorActivity.class);
	}

	@Override
	protected void onPreExecute()
	{
		httpParams = new BasicHttpParams();

		HttpConnectionParams.setConnectionTimeout(httpParams, Utility.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, Utility.CONNECTION_TIMEOUT);
		// Setting some attributes:
		responseHandler = new BasicResponseHandler();
	}

	@Override
	protected String doInBackground(String... params)
	{
		username = params[0];
		password = params[1];
		wifiIp = params[2];
		try
		{
			// Try to connect to my blog
			URL url = new URL("http://blog.informaticalab.com");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
			conn.addRequestProperty("User-Agent", "Android");
			@SuppressWarnings("unused")
			InputStream in = new BufferedInputStream(conn.getInputStream());
			// If the connection has been redirected:

			if (!url.getHost().equals(conn.getURL().getHost()))
			{
				// You have to connect
				if (DEBUG)
				    Log.i(LOG_TAG, "Url AP: " + conn.getURL().getHost());

				try
				{
					// Get the url from where i get redirectd
					urlForDisconnection = "https://" + conn.getURL().getHost();
					if (urlForDisconnection.contains("wifigw"))
					{
						// In this case is the old system. I save the url
						// for disconnection and
						// Set the post params to access the old system
						post = new HttpPost("https://" + conn.getURL().getHost() + "/login.pl");

						post.setEntity(new UrlEncodedFormEntity(Utility.getNameValuesWifi(false, username, password, wifiIp)));
						Log.i(LOG_TAG, "New Sapienza AP found: https://" + conn.getURL().getHost() + "/login.pl");
					}
					else
					{
						// In this case, is the new system. I change the
						// flag nuovoSistema
						// And set the right post's params.
						nuovoSistema = true;
						post = new HttpPost("https://" + conn.getURL().getHost() + ":12081/cgi-bin/zscp");
						urlForDisconnection = "https://" + conn.getURL().getHost() + ":12081/cgi-bin/zscp";
						post.setEntity(new UrlEncodedFormEntity(Utility.getNameValuesWifi(true, username, password, wifiIp)));
						if (DEBUG)
						    Log.i(LOG_TAG, "New Sapienza AP found: https://" + conn.getURL().getHost() + ":12081/cgi-bin/zscp");
					}
					post.setParams(httpParams);
					return mClient.execute(post, responseHandler);
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
				catch (ClientProtocolException exception)
				{
					exception.printStackTrace();
				}
				catch (IOException exception)
				{
					exception.printStackTrace();
				}
			}
			else
			{
				if (DEBUG)
				    Log.i(LOG_TAG, "Already Connected! Url: " + conn.getURL().getHost());
				// Already connected.

				return "AlreadyConnected";
			}
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		// If something went wrong, return "TimeOut".
		return "Timeout";
	}


	@Override
	protected void onPostExecute(String result)
	{
		if (DEBUG) Log.i(LOG_TAG, "OnPostExecute() - " + result);
		if (null != mClient) mClient.close();

		/*
		 * Ricrea il popup in caso l'utente l'avesse perso.
		 */
		if (result.equals("AlreadyConnected"))
		{
			SharedPreferences s = mActivity.getSharedPreferences(DisconnectActivity.MY_PREFERENCES, Context.MODE_PRIVATE);
			Utility.setDisconnectNotification(s.getString(DisconnectActivity.LOGOUT_URL, null), mActivity, nuovoSistema);

			mActivity.finish();
			return;
		}

		if (nuovoSistema && !result.equals("Timeout"))
		{
			if (DEBUG) Log.i(LOG_TAG, "Nuovo sistema.");
			// Connection succesfull:
			if (result.contains("autenticato con successo"))
			{
				String authenticator = result.substring(result.indexOf("Authenticator value=\"") + 21, result.indexOf("==\">") + 2);
				new GWRequestAsyncTask().execute(username, wifiIp, authenticator);
				Utility.setDisconnectNotification(urlForDisconnection, mActivity, nuovoSistema);

				// Mi salvo i dati nello sharedpref. In questo modo se perde
				// la notifica può recuperarla.

				SharedPreferences s = mActivity.getSharedPreferences(DisconnectActivity.MY_PREFERENCES, Context.MODE_PRIVATE);
				Editor z = s.edit();
				z.putString(DisconnectActivity.AUTHENTICATOR, authenticator);
				z.putString(DisconnectActivity.IP, wifiIp);
				z.putString(DisconnectActivity.MATRICOLA, username);
				z.putString(DisconnectActivity.LOGOUT_URL, urlForDisconnection);
				z.commit();

				mActivity.finish();
				return;
			}

			// Gia connesso con altri dispositivi:
			else if (result.contains("connessioni simultanee non permesse"))
			{
				errorActivity.putExtra(ErrorActivity.ERROR, ErrorActivity.ERRORALREADYCONNECTED);
				mActivity.startActivity(errorActivity);
			}
			// Wrong login:
			else if (result.contains("Utente sconosciuto o password non corretta."))
			{
				errorActivity.putExtra(ErrorActivity.ERROR, ErrorActivity.ERRORWITHLOGIN);
				mActivity.startActivity(errorActivity);
			}
			else
			// Something strange happened.
			{
				errorActivity.putExtra(ErrorActivity.ERROR, ErrorActivity.ERRORTIMEOUT);
				Log.e(LOG_TAG, result);
			}
		}

		else if (!nuovoSistema && !result.equals("Timeout"))
		{
			// Connection Successfull:
			if (result.contains("clicka"))
			{
				// Save the logout url:
				String logoutUrl = result.substring(result.indexOf("internet<A HREF=\"") + 17, result.indexOf("\">logout</A>.</P>"));
				if (DEBUG) Log.i(LOG_TAG, logoutUrl);
				// Dispatch notification:
				Utility.setDisconnectNotification(urlForDisconnection + logoutUrl, mActivity, nuovoSistema);
				mActivity.finish();
				return;
			}
			// Already connected:
			else if (result.contains("This user already") || result.contains("connessioni simultanee"))
			{
				errorActivity.putExtra(ErrorActivity.ERROR, ErrorActivity.ERRORALREADYCONNECTED);
			}
			// Wrong login:
			else if (result.contains("invalid name or"))
			{
				errorActivity.putExtra(ErrorActivity.ERROR, ErrorActivity.ERRORWITHLOGIN);
			}
			else
			// Something strange happened.
			{
				errorActivity.putExtra(ErrorActivity.ERROR, ErrorActivity.ERRORTIMEOUT);
			}
			Log.e(LOG_TAG, result);
		}
		else
		// If result equals TimeOut, in both case i need to do this:
		{
			errorActivity.putExtra(ErrorActivity.ERROR, ErrorActivity.ERRORTIMEOUT);
			Log.e(LOG_TAG, result);
		}
		mActivity.startActivity(errorActivity);

	}

}
