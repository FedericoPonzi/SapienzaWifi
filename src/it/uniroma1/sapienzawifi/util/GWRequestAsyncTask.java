package it.uniroma1.sapienzawifi.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpProtocolParams;

import android.os.AsyncTask;
import android.util.Log;

public class GWRequestAsyncTask extends AsyncTask<String, Void, Void>
{
	private static final String TAG = "GWRequest";
	String username;
	String wifiIp;
	String authenticator;

	// Metodo di comodo per creare la query
	@Override
	protected Void doInBackground(String... arg)
	{

		username = arg[0];
		wifiIp = arg[1];
		authenticator = arg[2];

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost("https://wifi-cont1.uniroma1.it:12081/cgi-bin/zscp");

		// Post Data
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("Section", "CPGW"));
		params.add(new BasicNameValuePair("Action", "Connect"));
		params.add(new BasicNameValuePair("U", username));
		params.add(new BasicNameValuePair("Realm", "uniroma1.it"));
		params.add(new BasicNameValuePair("IP", wifiIp));
		params.add(new BasicNameValuePair("ZSCPRedirect", "http%3A//www.uniroma1.it"));
		params.add(new BasicNameValuePair("Powered", "Powered by ZeroShell - Infosapienza Ufficio Telecomunicazioni"));
		params.add(new BasicNameValuePair("RS", "https://wifi-cont1.uniroma1.it:12081"));
		params.add(new BasicNameValuePair("Authenticator", authenticator));
		params.add(new BasicNameValuePair("Popup", "no"));
		// Encoding POST data
		try
		{
			
			//Allego gli entity alla richiesta post:
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			
			//Imposto lo user agent:
			HttpProtocolParams.setUserAgent(httpClient.getParams(), "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");

			//Eseguo la richiesta:

			Log.i(TAG, "Eseguo la richiesta: ");
			HttpResponse response = httpClient.execute(httpPost);
			
			//La loggo: TODO:controllo incrociato? serve? boh
			//Log.i(TAG, EntityUtils.toString(response.getEntity()));

		}
		catch (UnsupportedEncodingException e)
		{
			// log exception
			e.printStackTrace();
		}

		catch (ClientProtocolException e)
		{
			// Log exception
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// Log exception
			e.printStackTrace();
		}
		return null;

	}

}