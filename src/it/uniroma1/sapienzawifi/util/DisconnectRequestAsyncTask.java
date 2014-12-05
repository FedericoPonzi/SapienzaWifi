package it.uniroma1.sapienzawifi.util;

import it.uniroma1.sapienzawifi.DisconnectActivity;
import it.uniroma1.sapienzawifi.R;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.params.HttpProtocolParams;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

/**
 * AsyncTask per richiedere la disconnessione dalla rete wireless della
 * sapienza. Prende la stringa di logout salvata al momento del login, ed invia
 * una richiesta GET per terminare la sessione.
 * 
 */

public class DisconnectRequestAsyncTask extends AsyncTask<String, Void, String> {
	AndroidHttpClient mClient;

	ResponseHandler<String> handler;
	DisconnectActivity mActivity;
	public DisconnectRequestAsyncTask(DisconnectActivity act)
	{
		mActivity = act;
	}
	@Override
	protected void onPreExecute() {
		mClient = AndroidHttpClient.newInstance("");
		handler = new BasicResponseHandler();
		HttpProtocolParams.setUserAgent(mClient.getParams(), "Android");

	}

	@Override
	protected String doInBackground(String... params) {
		String matricola = params[0];
		String ip = params[1];
		String authenticator = params[2];
		
		String url = mActivity.getLogoutUrl();
		Log.i("App", "Logout url: " + url);
		try {
			if (mActivity.getIsNuovoSistema()) // TODO: controllare se worka.
			{
				HttpPost post = new HttpPost(url);
				post.setEntity(new UrlEncodedFormEntity(Utility
						.getNameValuesDisconnet(matricola, ip, authenticator)));

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
			mActivity.setDisconnectedMessage(mActivity.getString(R.string.disconnect_successful),
					true);

		} else {

			mActivity.setDisconnectedMessage(mActivity.getString(R.string.disconnect_failed), false);
		}
	}
}