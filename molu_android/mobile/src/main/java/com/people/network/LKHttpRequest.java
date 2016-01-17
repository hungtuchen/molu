package com.people.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import android.content.res.AssetManager;
import org.syxc.util.Logger;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.people.lyy.client.ApplicationEnvironment;
import com.people.lyy.client.Constants;
import com.people.lyy.client.TransferRequestTag;

public class LKHttpRequest {

	private int tag;
	private int methodTag;
	private int jobdone = 0;
	private HashMap<String, Object> requestDataMap;
	private LKAsyncHttpResponseHandler responseHandler;
	private AsyncHttpClient client;
	private LKHttpRequestQueue queue;

	public LKHttpRequest(int methodTag, HashMap<String, Object> requestMap, LKAsyncHttpResponseHandler handler) {
		this.methodTag = methodTag;
		this.requestDataMap = requestMap;
		this.responseHandler = handler;
		client = new AsyncHttpClient();
		
		if( Constants.IP.startsWith("https://") ){
			this.setHttpsSSLKey();
		}

		if (null != this.responseHandler) {
			this.responseHandler.setRequest(this);
		}
	}
	
	

	public int getJobdone() {
		return jobdone;
	}



	public void setJobdone(int jobdone) {
		this.jobdone = jobdone;
	}



	public int getTag() {
		return tag;
	}

	public void setTag(int tag) {
		this.tag = tag;
	}

	public int getMethodTag() {
		return methodTag;
	}

	public LKHttpRequestQueue getRequestQueue() {
		return this.queue;
	}

	public void setRequestQueue(LKHttpRequestQueue queue) {
		this.queue = queue;
	}

	public HashMap<String, Object> getRequestDataMap() {
		return requestDataMap;
	}

	public LKAsyncHttpResponseHandler getResponseHandler() {
		return responseHandler;
	}

	public AsyncHttpClient getClient() {
		return client;
	}

	/****************************************/

	public void post() {
		this.client.post(ApplicationEnvironment.getInstance().getApplication(), TransferRequestTag.getRequestTagMap().get(this.getMethodTag()), this.getHttpEntity(this), null, this.responseHandler);
	}
	
	public void get() {
		this.client.get(ApplicationEnvironment.getInstance().getApplication(), TransferRequestTag.getRequestTagMap().get(this.getMethodTag()), param2RequestParam(requestDataMap), this.responseHandler);
	}

	private HttpEntity getHttpEntity(LKHttpRequest request) {

		StringBuffer bodySB = new StringBuffer();
		bodySB.append(this.param2JsonString(request.getRequestDataMap()));

//		request.getClient().addHeader("Content-Length", bodySB.length() + "");
		Logger.i("dongyaqin request body:", request.getRequestDataMap().toString());
		Logger.i("dongyaqin request body:", bodySB.toString());

		HttpEntity entity = null;
		try {
			entity = new StringEntity(bodySB.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return entity;
	}
	
	private RequestParams param2RequestParam( HashMap<String, Object> paramMap ) {
		RequestParams requestParams = new RequestParams();
		
		for ( String key : paramMap.keySet() ) {
			requestParams.put(key, paramMap.get(key).toString());
		}
		return requestParams;
	}
	
	@SuppressWarnings("unused")
	private String param2JsonString( HashMap<String, Object> paramMap ) {
		JSONObject jsonParams = new JSONObject( paramMap );
		return jsonParams.toString();
	}

	@SuppressWarnings("unchecked")
	private String param2String(HashMap<String, Object> paramMap) {
		StringBuffer sb = new StringBuffer();

		for (String key : paramMap.keySet()) {
			Object obj = paramMap.get(key);
			sb.append(key).append("=").append(obj).append("&");
		}

		sb.deleteCharAt(sb.length() - 1);
		
		return sb.toString();
		
	}
	
	private void setHttpsSSLKey(){
		AssetManager am = ApplicationEnvironment.getInstance().getApplication().getAssets();
	    CertificateFactory cf;
		try {
			cf = CertificateFactory.getInstance("X.509");
			InputStream in = am.open("root.crt");
			Certificate ca = cf.generateCertificate(in);
			
			KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
			keystore.load(null, null);
			keystore.setCertificateEntry("ca", ca);
			
			MySSLSocketFactory msf = new MySSLSocketFactory( keystore );
			msf.setHostnameVerifier( org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER );
			this.client.setSSLSocketFactory( msf );
			
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
