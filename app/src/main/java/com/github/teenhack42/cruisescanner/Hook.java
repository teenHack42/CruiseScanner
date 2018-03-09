package com.github.teenhack42.cruisescanner;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by grant on 10/2/18.
 */

public class Hook {

	URL url = null;

	Hook(String url_in) throws MalformedURLException {
		this.url = new URL(url_in);
	}

	public String post(String action, HashMap<String, Object> data_hash) {

		JSONObject postDataParams = new JSONObject();


		try {
			postDataParams.put("action_name", action);
			for (HashMap.Entry<String, Object> entry : data_hash.entrySet()) {
				postDataParams.put(entry.getKey(), entry.getValue().toString());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}


		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		conn.setReadTimeout(15000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		try {
			conn.setRequestMethod("POST");
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		conn.setDoInput(true);
		conn.setDoOutput(true);

		OutputStream os = null;
		try {



			os = conn.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(os, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		try {
			writer.write(getPostDataString(postDataParams));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			writer.flush();
			writer.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		int responseCode = 0;
		try {
			responseCode = conn.getResponseCode();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (responseCode == HttpsURLConnection.HTTP_OK) {

			Log.d("Hook", "Status: HTTP_OK");

			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			StringBuffer sb = new StringBuffer("");
			String line = "";

			try {
				while ((line = in.readLine()) != null) {

					sb.append(line);
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();

		} else {
			return new String("false : " + responseCode);
		}
	}

	public String getPostDataString(JSONObject params) throws Exception {

		StringBuilder result = new StringBuilder();
		boolean first = true;

		Iterator<String> itr = params.keys();

		while (itr.hasNext()) {

			String key = itr.next();
			Object value = params.get(key);

			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(key, "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(value.toString(), "UTF-8"));

		}
		return result.toString();
	}
}
