package com.catcap.IAP.PluginVIvoUtil;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpConnectionAgent {
    private static final int DEFAULT_TIMEOUT = 5 * 1000;
    private static final int DEFAULT_CONNECTION_TIMEOUT = 10 * 1000;
    private static final int DEFAULT_SO_TIMEOUT = 60 * 1000;
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    private static final String DEFAULT_CHARSET = HTTP.UTF_8;
    private static final String POST_REQUEST_METHOD = "POST";
    private static final String PUT_REQUEST_METHOD = "PUT";

    private String requestMethod = "GET";
    private DefaultHttpClient httpClient;
    private Map<String, String> requestHeaders;
    private HttpRequestBase request;
    private HttpResponse httpResponse;
    private HttpEntity response_entity;
    private int chunkLength = -1;
    private ProxyConfig proxyConfig;

    public HttpConnectionAgent() {
	KeyStore trustStore;
	try {
	    trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

	    trustStore.load(null, null);

	    SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
	    sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	    HttpParams params = new BasicHttpParams();
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, DEFAULT_CHARSET);
	    HttpProtocolParams.setUseExpectContinue(params, false);

	    HttpConnectionParams.setConnectionTimeout(params,
		    DEFAULT_CONNECTION_TIMEOUT);
	    HttpConnectionParams.setSoTimeout(params, DEFAULT_SO_TIMEOUT);
	    HttpConnectionParams.setStaleCheckingEnabled(params, false);

	    SchemeRegistry schemeRegistry = new SchemeRegistry();
	    schemeRegistry.register(new Scheme("http", PlainSocketFactory
		    .getSocketFactory(), DEFAULT_HTTP_PORT));
	    schemeRegistry.register(new Scheme("https", sf, DEFAULT_HTTPS_PORT));
	    ClientConnectionManager ccm = new ThreadSafeClientConnManager(
		    params, schemeRegistry);
	    httpClient = new DefaultHttpClient(ccm, params);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void executeRequest(String url) throws IOException {
	if (POST_REQUEST_METHOD.equalsIgnoreCase(requestMethod)) {
	    request = new HttpPost(url);
	} else if (PUT_REQUEST_METHOD.equalsIgnoreCase(requestMethod)) {
	    request = new HttpPut(url);
	} else {
	    request = new HttpGet(url);
	}
	performRequest(request);

    }

    public void executeRequest(String url, NameValuePair... nameValuePairs)
	    throws IOException {
	List<NameValuePair> list_nvp = new ArrayList<NameValuePair>();
	for (NameValuePair nvp : nameValuePairs) {
	    list_nvp.add(nvp);
	}
	UrlEncodedFormEntity encodeEntity = new UrlEncodedFormEntity(list_nvp,
		DEFAULT_CHARSET);
	if (POST_REQUEST_METHOD.equalsIgnoreCase(requestMethod)) {
	    request = new HttpPost(url);
	    ((HttpPost) request).setEntity(encodeEntity);
	} else if (PUT_REQUEST_METHOD.equalsIgnoreCase(requestMethod)) {
	    request = new HttpPut(url);
	    ((HttpPost) request).setEntity(encodeEntity);
	} else {
	    request = new HttpGet(url);
	}
	performRequest(request);

    }

    public void executeRequest(String url, InputStream is, long length)
	    throws IOException {

	String content_type = null;
	if (null != requestHeaders) {
	    content_type = requestHeaders.get("Content_Type");
	}
	if (null == content_type) {
	    content_type = "binary/octet-stream";
	}

	if (POST_REQUEST_METHOD.equalsIgnoreCase(requestMethod)) {
	    request = new HttpPost(url);
	    if (null != is) {
		InputStreamEntity isEntity = new InputStreamEntity(is, length);
		isEntity.setContentType(content_type);
		if (chunkLength > 0) {
		    isEntity.setChunked(true);
		}
		((HttpPost) request).setEntity(isEntity);
	    }

	} else if (PUT_REQUEST_METHOD.equalsIgnoreCase(requestMethod)) {
	    request = new HttpPut(url);
	    if (null != is) {
		InputStreamEntity isEntity = new InputStreamEntity(is, length);
		isEntity.setContentType(content_type);
		if (chunkLength > 0) {
		    isEntity.setChunked(true);
		}
		((HttpPost) request).setEntity(isEntity);
	    }
	} else {
	    request = new HttpGet(url);
	}
	performRequest(request);

    }

    private void performRequest(HttpRequestBase request) throws IOException {
	if (null != requestHeaders && requestHeaders.size() > 0) {
	    for (String key : requestHeaders.keySet()) {
		String value = requestHeaders.get(key);
		request.setHeader(key, value);
	    }
	}

	if (null != proxyConfig) {
	    HttpParams params = request.getParams();
	    ConnRouteParams.setDefaultProxy(
		    params,
		    new HttpHost(proxyConfig.getAddress(), proxyConfig
			    .getPort()));
	    request.setParams(params);
	}

	try {
	    httpResponse = httpClient.execute(request);

	    StatusLine statusLine = httpResponse.getStatusLine();
	    if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
		throw new RuntimeException("request failed");
	    }
	    response_entity = httpResponse.getEntity();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new IOException(e.toString());
	}
    }

    public String getResponseMessage() throws IOException {
	if (null != response_entity) {
	    return EntityUtils.toString(response_entity, DEFAULT_CHARSET);
	}
	return null;
    }

    public String getResponseHeader(String key) {
	Header header = httpResponse.getFirstHeader(key);
	if (null != header) {
	    return header.getValue();
	}
	return null;
    }

    public int getResponseLength() {
	String len = getResponseHeader("content-length");
	if (null != len) {
	    try {
		return Integer.parseInt(len);
	    } catch (Exception e) {
		return -1;
	    }
	} else {
	    return -1;
	}
    }

    public InputStream openInputStream() throws IOException {
	if (null != response_entity) {
	    return response_entity.getContent();
	}
	return null;
    }

    public void setRequestHeader(String key, String value) {
	if (null == requestHeaders) {
	    requestHeaders = new HashMap<String, String>();
	}
	requestHeaders.put(key, value);
    }

    public void setRequestMethod(String requestMethod) {
	this.requestMethod = requestMethod;
    }

    public void setChunkStreamMode(int chunkLength) {
	this.chunkLength = chunkLength;
    }

    public void setProxyConfig(ProxyConfig proxyConfig) {
	this.proxyConfig = proxyConfig;
    }

    public static HttpClient getNewHttpClient() {
	try {
	    KeyStore trustStore = KeyStore.getInstance(KeyStore
		    .getDefaultType());
	    trustStore.load(null, null);

	    SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
	    sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	    HttpParams params = new BasicHttpParams();
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	    SchemeRegistry registry = new SchemeRegistry();
	    registry.register(new Scheme("http", PlainSocketFactory
		    .getSocketFactory(), 80));
	    registry.register(new Scheme("https", sf, 443));

	    ClientConnectionManager ccm = new ThreadSafeClientConnManager(
		    params, registry);

	    return new DefaultHttpClient(ccm, params);
	} catch (Exception e) {
	    return new DefaultHttpClient();
	}
    }
}
