package com.catcap.IAP.PluginVIvoUtil;

import org.apache.http.NameValuePair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.GZIPOutputStream;

public class HttpTransportAgent implements TransportAgent {
	private String requestURL;
	private String charset;
	private String useragent;
	private boolean iscompression;
	private boolean isForceCookie;
	private ProxyConfig proxyConfig;
	private HttpConnectionAgent httpConnection;

	public HttpTransportAgent(String requestURL, String charset,
			String useragent, boolean iscompression, boolean isForceCookie,
			ProxyConfig proxyConfig) {
		this.requestURL = requestURL;
		this.charset = charset;
		this.useragent = useragent;
		this.iscompression = iscompression;
		this.isForceCookie = isForceCookie;
		this.proxyConfig = proxyConfig;

		httpConnection = new HttpConnectionAgent();
		httpConnection.setProxyConfig(proxyConfig);
	}

	public String sendRequest(String requestMsg) throws Exception {
		return sendRequest(requestMsg, this.charset);
	}

	public String sendRequest(String requestMsg, String charset)
			throws Exception {
		byte[] inData = null;
		String responseMsg = null;
		if (null != charset) {
			try {
				inData = requestMsg.getBytes(charset);
			} catch (Exception e) {
				charset = null;
				inData = requestMsg.getBytes();
			}
		} else {
			inData = requestMsg.getBytes();
		}

		byte[] outDate = sendRequest(inData);
		inData = null;
		if (null == outDate) {
			responseMsg = "response data is null";
			throw new Exception(requestMsg);
		}
		if (null != charset) {
			try {
				responseMsg = new String(outDate, charset);
			} catch (Exception e) {
				responseMsg = new String(outDate);
			}
		} else {
			responseMsg = new String(outDate);
		}
		return responseMsg;
	}

	public byte[] sendRequest(byte[] request_msg) throws Exception {
		byte[] outData = null;
		InputStream is = null;
		try {
			if (null != request_msg) {
				byte[] compress_msg = request_msg;

				if (iscompression) {
					compress_msg = compressRequestMsg(request_msg);

					httpConnection.setRequestHeader(HEADER_CONTENT_LENGTH, ""
							+ request_msg.length);
					httpConnection.setRequestHeader(
							HEADER_UNCOMPRESSED_CONTENT_LENGTH, ""
									+ compress_msg.length);
				}
				is = new ByteArrayInputStream(compress_msg);
				httpConnection.executeRequest(requestURL, is,
						compress_msg.length);
			} else {
				httpConnection.executeRequest(requestURL, is, 0);
			}

			int len = httpConnection.getResponseLength();
			String contentEncoding = httpConnection
					.getResponseHeader("Content-Encoding");
			if (len == -1 && null == contentEncoding) {
				System.out.println("response is null");
			} else {
				is = httpConnection.openInputStream();
				if (null != is) {
					outData = readStream(is, len);
				}
			}
		} catch (Exception e) {
			throw e;
		}

		return outData;
	}

	public String sendRequest(NameValuePair... nameValuePairs) throws Exception {
		String responseMsg = null;
		try {
			httpConnection.setRequestMethod("POST");
			if (null != nameValuePairs) {
				httpConnection.executeRequest(requestURL, nameValuePairs);
			} else {
				httpConnection.executeRequest(requestURL);
			}
			responseMsg = httpConnection.getResponseMessage();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return responseMsg;
	}

	public void setRequestURL(String url) throws Exception {
		this.requestURL = url;
	}

	public String getResponseData() throws Exception {
		return null;
	}

	public void setRequestContentType(String contentType) {

	}

	public void setCustomHeaders(Hashtable headers) {
		if (null != headers) {
			Enumeration keys = headers.keys();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				String value = (String) headers.get(key);
				httpConnection.setRequestHeader(key, value);
			}
		}
	}

	public void setCustomHeaders(String key, String value) {
		if (null != key && null != value) {
			httpConnection.setRequestHeader(key, value);
		}
	}

	private byte[] readStream(InputStream is, int length) throws IOException {
		if (length < 0) {
			length = 1024;
		}
		ByteArrayOutputStream bis = new ByteArrayOutputStream(length);
		byte[] buff = new byte[1024];
		int n = 0;

		while ((n = is.read(buff)) > 0) {
			bis.write(buff, 0, n);
		}
		buff = null;
		return bis.toByteArray();
	}

	private byte[] compressRequestMsg(byte[] request_msg) throws Exception {
		ByteArrayOutputStream bis = null;
		GZIPOutputStream gos = null;
		byte[] compress_msg = null;
		try {
			bis = new ByteArrayOutputStream();
			gos = new GZIPOutputStream(bis);
			gos.write(request_msg, 0, request_msg.length);
			gos.flush();

			compress_msg = bis.toByteArray();
		} catch (Exception e) {
			throw e;
		} finally {
			if (null != gos) {
				gos.close();
			}
			if (null != bis) {
				bis.close();
			}
		}
		return compress_msg;
	}
}
