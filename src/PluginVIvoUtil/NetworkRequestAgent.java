package com.catcap.IAP.PluginVIvoUtil;

import android.util.Log;

import org.apache.http.NameValuePair;



public class NetworkRequestAgent {
	private static final String TAG = "NetworkRequestAgent";
	public static final String URL_INITIAL_PAYMENT = "https://pay.vivo.com.cn/vivoPay/getVivoOrderNum";
//	public static final String URL_INITIAL_PAYMENT = "http://113.98.231.125:1690/vivoPay/getVivoOrderNum";

	public String checkForPayAppUpdate(String url,
			NameValuePair[] nameValuePairs) {
		String response_msg = null;
		try {
			response_msg = sendRequest(url, nameValuePairs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response_msg;
	}

	public String initialPayment(NameValuePair[] nameValuePairs) {
		String response_msg = "dfsfsdfsafd";
		try {
			Log.d(TAG, "URL_INITIAL_PAYMENT="+URL_INITIAL_PAYMENT);
			response_msg = sendRequest(URL_INITIAL_PAYMENT, nameValuePairs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response_msg;
	}

	public byte[] retrieveFromServer(String newAppUrl) throws Exception {
		HttpTransportAgent httpTransportAgent = new HttpTransportAgent(
				newAppUrl, Constant.DEFAULT_CHARSET, null, false, false, null);
		byte[] reqestData = null;
		byte[] outData = httpTransportAgent.sendRequest(reqestData);

		return outData;

	}

	public String requestByUrlOnly(String request_url) {
		String response_msg = null;
		try {
			NameValuePair[] nameValuePairs = null;
			response_msg = sendRequest(request_url, nameValuePairs);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response_msg;
	}

	public String sendRequest(String url, NameValuePair... nameValuePairs)
			throws Exception {
		String response_msg = null;
		HttpTransportAgent httpTransportAgent = new HttpTransportAgent(url,
				Constant.DEFAULT_CHARSET, null, false, false, null);

		if (null != url) {
			response_msg = httpTransportAgent.sendRequest(nameValuePairs);

			Log.d("PaymentTypeActivity", "send request to server done,result="
					+ response_msg);
		} else {
			Log.d("PaymentTypeActivity",
					"request url is null in send request method");
		}
		return response_msg;
	}
}
