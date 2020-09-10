package com.cos.cmtgp.common.util;

import com.jfinal.kit.PropKit;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.security.*;

public class UrlUtil {

	/**
	 * 获得url的基础地址（到action部分，用于权限管理）
	 */
	public static String formatBaseUrl(String url) {
		url = removeUrlParam(url).replaceAll("//+", "/").replaceAll("/$", "");
		if(url.split("/").length >= 2) {
			url = url.replaceAll("/\\w*$", "");
		}
		return url;
	}
	
	public static String formatUrl(String url) {
		url = url.replaceAll("//+", "/").replaceAll("/$", "");
		return url;
	}
	
	/**
	 * 去掉url参数
	 */
	public static String removeUrlParam(String url) {
		return url.replaceAll("[?#].*", "");
	}
	
	public static String getAsText(String url) {
		String urlString = "";
		try {
			URLConnection urlConnection = new URL(url).openConnection();
			urlConnection.setConnectTimeout(1000 * 30);	//30秒超时
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String current;
			while ((current = in.readLine()) != null) {
				urlString += current;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return urlString;
	}


	public static String sendPost(String url, String xmlObj,boolean cert) throws Exception{
		BasicHttpClientConnectionManager connManager = null;
		if(cert){
			// 证书
			char[] password = MiniUtil.MERCHANT_NO.toCharArray();
			InputStream certStream = UrlUtil.class.getResourceAsStream("/apiclient_cert.p12");
			KeyStore ks = KeyStore.getInstance("PKCS12");
			ks.load(certStream, password);

			// 实例化密钥库 & 初始化密钥工厂
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, password);

			// 创建 SSLContext
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());

			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
					sslContext,
					new String[]{"TLSv1"},
					null,
					new DefaultHostnameVerifier());

			connManager = new BasicHttpClientConnectionManager(
					RegistryBuilder.<ConnectionSocketFactory>create()
							.register("http", PlainConnectionSocketFactory.getSocketFactory())
							.register("https", sslConnectionSocketFactory)
							.build(),
					null,
					null,
					null
			);
		}
		HttpPost httpPost = new HttpPost(url);

		httpPost.addHeader("Content-Type", "text/xml");
		if(!"".equals(xmlObj)){
			StringEntity postEntity = new StringEntity(xmlObj, "UTF-8");
			httpPost.setEntity(postEntity);
		}

		//设置请求器的配置
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(6*1000).setConnectTimeout(8*1000).build();
		httpPost.setConfig(requestConfig);
		HttpClient httpClient = null;
		if(cert){
			httpClient = HttpClientBuilder.create()
					.setConnectionManager(connManager)
					.build();
		}else{
			httpClient = HttpClients.createDefault();
		}
		
		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity, "UTF-8");
		return result;
	}
}
