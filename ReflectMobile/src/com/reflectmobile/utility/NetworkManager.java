package com.reflectmobile.utility;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

public class NetworkManager {
	
	private static final String hostName = "http://rewyndr.truefitdemo.com";
	
	private static HashMap<String, String> cookie = new HashMap<String, String>();

	public static interface HttpTaskHandler {
		void taskSuccessful(String result);

		void taskFailed(String reason);
	}
	
	public static interface HttpImageTaskHandler {
		void taskSuccessful(Drawable drawable);

		void taskFailed(String reason);
	}
	
		

	private static void setCookies(HttpURLConnection httpURLConnection) {
		StringBuilder cookiesToSet = new StringBuilder();
		for (String key : cookie.keySet()) {
			cookiesToSet.append(key + "=" + cookie.get(key) + "; ");
		}
		Log.d("SentCookies", cookiesToSet.toString());
		httpURLConnection.setRequestProperty("Cookie", cookiesToSet.toString());
	}

	private static void getCookies(HttpURLConnection httpURLConnection) {
		String headerName = null;
		for (int i = 1; (headerName = httpURLConnection.getHeaderFieldKey(i)) != null; i++) {
			if (headerName.equals("Set-Cookie")) {
				String cookies = httpURLConnection.getHeaderField(i);
				Log.d("ReceivedCookies", cookies);
				for (String keyValue : cookies.split(";")) {
					String[] splittedKeyValue = keyValue.trim().split("=");
					if (splittedKeyValue.length >= 2) {
						String key = splittedKeyValue[0];
						String value = splittedKeyValue[1];

						if (key != "path") {
							cookie.put(key, value);
						}
					}
				}
			}
		}

	}

	private static String readStream(String TAG, InputStream in) {
		BufferedReader reader = null;
		StringBuffer data = new StringBuffer("");
		try {
			reader = new BufferedReader(new InputStreamReader(in));
			String line = "";
			while ((line = reader.readLine()) != null) {
				data.append(line);
			}
		} catch (IOException e) {
			Log.e(TAG, "IOException");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return data.toString();
	}
	

	public static class HttpGetTask extends AsyncTask<String, Void, String> {

		private HttpTaskHandler handler;

		public HttpGetTask(HttpTaskHandler handler) {
			this.handler = handler;
		}

		private String TAG = "HttpGetTask";

		@Override
		protected String doInBackground(String... params) {
			String data = "";
			HttpURLConnection httpUrlConnection = null;

			try {
				httpUrlConnection = (HttpURLConnection) new URL(params[0])
						.openConnection();

				setCookies(httpUrlConnection);

				httpUrlConnection.connect();

				InputStream in = new BufferedInputStream(
						httpUrlConnection.getInputStream());

				data = readStream(TAG, in);

				getCookies(httpUrlConnection);

			} catch (MalformedURLException exception) {
				this.handler.taskFailed("MalformedURLException");
			} catch (IOException exception) {
				this.handler.taskFailed("IOException");
			} finally {
				if (null != httpUrlConnection)
					httpUrlConnection.disconnect();
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			this.handler.taskSuccessful(result);
		}
	}

	public static class HttpPostTask extends AsyncTask<String, Void, String> {

		private HttpTaskHandler handler;
		private String payload;

		public HttpPostTask(HttpTaskHandler handler, String payload) {
			this.handler = handler;
			this.payload = payload;
		}

		private String TAG = "HttpPostTask";

		@Override
		protected String doInBackground(String... params) {
			String data = "";
			HttpURLConnection httpUrlConnection = null;

			try {

				httpUrlConnection = (HttpURLConnection) new URL(params[0])
						.openConnection();
				httpUrlConnection.setRequestMethod("POST");
				httpUrlConnection.setRequestProperty("Content-Type",
						"application/json;charset=UTF-8");

				setCookies(httpUrlConnection);

				httpUrlConnection.connect();

				OutputStream outputStream = httpUrlConnection.getOutputStream();
				BufferedWriter writer = new BufferedWriter(
						new OutputStreamWriter(outputStream, "UTF-8"));

				writer.write(payload);
				writer.flush();
				writer.close();
				outputStream.close();

				httpUrlConnection.connect();

				getCookies(httpUrlConnection);

				InputStream in = new BufferedInputStream(
						httpUrlConnection.getInputStream());

				data = readStream(TAG, in);

			} catch (MalformedURLException exception) {
				this.handler.taskFailed("MalformedURLException");
			} catch (IOException exception) {
				this.handler.taskFailed("IOException");
			} finally {
				if (null != httpUrlConnection)
					httpUrlConnection.disconnect();
			}
			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			this.handler.taskSuccessful(result);
		}
	}
	
	public static class HttpGetImageTask extends AsyncTask<String, Void, Drawable> {

		private HttpImageTaskHandler handler;

		public HttpGetImageTask(HttpImageTaskHandler handler) {
			this.handler = handler;
		}

		private String TAG = "HttpGetImageTask";

		@Override
		protected Drawable doInBackground(String... params) {
			Drawable drawable = null;
			HttpURLConnection httpUrlConnection = null;

			try {
				httpUrlConnection = (HttpURLConnection) new URL(hostName + params[0])
						.openConnection();

				setCookies(httpUrlConnection);

				httpUrlConnection.connect();

				InputStream in = new BufferedInputStream(
						httpUrlConnection.getInputStream());

				drawable = Drawable.createFromStream(in, null);

				getCookies(httpUrlConnection);

			} catch (MalformedURLException exception) {
				this.handler.taskFailed("MalformedURLException");
			} catch (IOException exception) {
				this.handler.taskFailed("IOException");
			} finally {
				if (null != httpUrlConnection)
					httpUrlConnection.disconnect();
			}
			return drawable;
		}

		@Override
		protected void onPostExecute(Drawable drawable) {
			this.handler.taskSuccessful(drawable);
		}
	}
}
