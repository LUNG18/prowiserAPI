package com.prowiser.api.utils;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
public class HttpClientCall {
	private static Logger log = LoggerFactory.getLogger(HttpClientCall.class);
	public String proxyHost;
	public int proxyPort;

	public HttpClientCall() {

	}

	@SuppressWarnings("unused")
	protected String callHttps(String URL, String entity) throws Exception {
		return callHttps(URL, entity, null, 0, 0);
	}

	@SuppressWarnings("unused")
	public String callHttps(String URL, String entity, String contentType, int connTimeout,
			int readTimeout) throws Exception {
		JSONObject json_result = new JSONObject();

		CloseableHttpClient httpClient =   HttpClients.custom()
	            .setSSLSocketFactory(new SSLConnectionSocketFactory(SSLContexts.custom()
	                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
	                    .build()
	                )
	            ).build();
		
		Builder configBuilder = RequestConfig.custom().
				setConnectTimeout(connTimeout).setConnectionRequestTimeout(readTimeout);

		if (proxyHost != null) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			configBuilder.setProxy(proxy);
		}

		log.info("URL : " + URL);
		log.info(" entity : " + entity);
		HttpPost postRequest = new HttpPost(URL);
		postRequest.setConfig(configBuilder.build());

		StringEntity input = new StringEntity(entity, "UTF-8");
		input.setContentEncoding("UTF-8");
        if(contentType!=null){
            input.setContentType(contentType);
        }else {
            input.setContentType("application/json");//application/x-www-form-urlencoded
        }

		postRequest.setEntity(input);
		long time = System.currentTimeMillis();
		HttpResponse response = httpClient.execute(postRequest);
		log.info("api[" + URL + "] call =====>"
				+ (System.currentTimeMillis() - time) + "ms");
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent()), "utf-8"));
		String output;
		StringBuffer cc = new StringBuffer();
		while ((output = br.readLine()) != null) {
			cc.append(output);
		}
		log.info(" : " + cc);
		//httpClient.getConnectionManager().shutdown();
		httpClient.close();
		return cc.toString();
	}

	@SuppressWarnings("unused")
	protected String callHttps(String URL, BasicHeader... obj) throws Exception {
		JSONObject json_result = new JSONObject();

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		Builder configBuilder = RequestConfig.custom().
				setConnectTimeout(500).setConnectionRequestTimeout(500);

		if (proxyHost != null) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			configBuilder.setProxy(proxy);
		}

		log.info("URL : " + URL);
		HttpGet getRequest = new HttpGet(URL);
		getRequest.setConfig(configBuilder.build());

		if (obj != null && obj.length > 0)
			for (int i = 0; i < obj.length; i++)
				getRequest.addHeader(obj[i]);

		long time = System.currentTimeMillis();
		HttpResponse response = httpClient.execute(getRequest);
		log.info("api[" + URL + "] call =====>"
				+ (System.currentTimeMillis() - time) + "ms");
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent()), "utf-8"));
		String output;
		StringBuffer cc = new StringBuffer();
		while ((output = br.readLine()) != null) {
			cc.append(output);
		}
		log.info(" : " + cc);
		//httpClient.getConnectionManager().shutdown();
		httpClient.close();
		return cc.toString();
	}

	public static String sendGet(String url) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString = url;
			URL realUrl = new URL(urlNameString);
			URLConnection connection = realUrl.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent",
					"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	static void fixSSL(DefaultHttpClient httpClient) {
		SSLContext sslContext;
		try {
			sslContext = SSLContext.getInstance("SSL");
			// set up a TrustManager that trusts everything
			try {
				sslContext.init(null,
						new TrustManager[] { new X509TrustManager() {
							public X509Certificate[] getAcceptedIssuers() {
								return null;
							}

							public void checkClientTrusted(
									X509Certificate[] certs, String authType) {
							}

							public void checkServerTrusted(
									X509Certificate[] certs, String authType) {
							}
						} }, new SecureRandom());
			} catch (KeyManagementException e) {
			}
			SSLSocketFactory ssf = new SSLSocketFactory(sslContext);
			ClientConnectionManager ccm = httpClient.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));

		} catch (Exception e) {
		}
	}

	@SuppressWarnings("unused")
	public String doPut(String URL, String entity) throws Exception {
		JSONObject json_result = new JSONObject();


		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		Builder configBuilder = RequestConfig.custom().
				setConnectTimeout(500).setConnectionRequestTimeout(500);
		
		if (proxyHost != null) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			configBuilder.setProxy(proxy);
		}

		log.info("URL : " + URL);
		log.info(" entity : " + entity);
		HttpPut postRequest = new HttpPut(URL);
		postRequest.setConfig(configBuilder.build());
		StringEntity input = new StringEntity(entity, "UTF-8");
		input.setContentEncoding("UTF-8");
		input.setContentType("application/json");

		postRequest.setEntity(input);
		long time = System.currentTimeMillis();
		HttpResponse response = httpClient.execute(postRequest);
		log.info("api[" + URL + "] call =====>"
				+ (System.currentTimeMillis() - time) + "ms");
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(response.getEntity().getContent()), "utf-8"));
		String output;
		StringBuffer cc = new StringBuffer();
		while ((output = br.readLine()) != null) {
			cc.append(output);
		}
		log.info(" : " + cc);
		//httpClient.getConnectionManager().shutdown();
		httpClient.close();
		return cc.toString();
	}

	public static void main(String[] args) {
		HttpClientCall call = new HttpClientCall();
		try {
			String abc = call.callHttps(
							"http://c2.topchef.net.cn/app-unileverexpiry/unileverapi/getOpenIdByUnionId",
							"unionid=oT2jev3WUOTbzym2NiVFeJj5l3T8source=16secret=ca3694604bcbe0a33851307f03e28be6", null,
							500, 500);
			System.out.println(abc);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
