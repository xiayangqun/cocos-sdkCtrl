package com.catcap.IAP.PluginVIvoUtil;

import org.apache.http.NameValuePair;

import java.util.Hashtable;

public interface TransportAgent {
	public static final String HEADER_CONTENT_LENGTH = "Content-Length";
	public static final String HEADER_UNCOMPRESSED_CONTENT_LENGTH = "Uncompressed-Content-Length";
	
	public String sendRequest(String requestMsg) throws Exception;

	public String sendRequest(String requestMsg, String charset) throws Exception;

	public byte[] sendRequest(byte[] request) throws Exception;

	public String sendRequest(NameValuePair... nameValuePairs) throws Exception;

	public void setRequestURL(String url) throws Exception;

	public String getResponseData() throws Exception;

	public void setRequestContentType(String contentType);

	public void setCustomHeaders(Hashtable headers);

}
