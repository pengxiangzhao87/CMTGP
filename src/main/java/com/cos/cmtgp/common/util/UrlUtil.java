package com.cos.cmtgp.common.util;

import com.cos.cmtgp.business.model.CommodityInfo;
import com.cos.cmtgp.common.mini.WXPayUtil;
import com.jfinal.json.FastJson;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Record;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.*;
import java.text.ParseException;
import java.util.*;

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



	public static List<Map<String,Object>>  getExpressInfo(String type, String no) {
		String host = "https://goexpress.market.alicloudapi.com";// 【1】请求地址 支持http 和 https 及 WEBSOCKET
		String path = "/goexpress";// 【2】后缀
		String appcode = "7e31044ce4f548caa3a80951c02f9e49"; // 【3】开通服务后 买家中心-查看AppCode
//		String no = "780098068058"; // 【4】请求参数，详见文档描述
//		String type = "zto"; //  【4】请求参数，不知道可不填 95%能自动识别
		String urlSend = host + path + "?no=" + no + "&type=" + type; // 【5】拼接请求链接
		try {
			URL url = new URL(urlSend);
			HttpURLConnection httpURLCon = (HttpURLConnection) url.openConnection();
			httpURLCon.setRequestProperty("Authorization", "APPCODE " + appcode);// 格式Authorization:APPCODE
			// (中间是英文空格)
			int httpCode = httpURLCon.getResponseCode();
			if (httpCode == 200) {
				String json = read(httpURLCon.getInputStream());
				Map<String, String> result = FastJson.getJson().parse(json, Map.class);
				if(result.get("code").equals("OK")){
					List<Map<String,Object>>  resultList =FastJson.getJson().parse(result.get("list").toString(), List.class);
					Collections.sort(resultList, new Comparator<Map<String, Object>>() {
						public int compare(Map<String, Object> a, Map<String, Object> b) {
							Date dateA = null;
							Date dateB = null;
							try {
								dateA = DateUtil.getStringToDate(a.get("time").toString());
								dateB =  DateUtil.getStringToDate(b.get("time").toString());
							} catch (ParseException e) {
								e.printStackTrace();
							}

							return dateB.compareTo(dateA);
						}
					});
					return resultList;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/*
	 * 读取返回结果
	 */
	private static String read(InputStream is) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = br.readLine()) != null) {
			line = new String(line.getBytes(), "utf-8");
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}
}
