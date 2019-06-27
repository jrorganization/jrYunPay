package com.qh.pay.api.utils;

import com.alibaba.fastjson.JSON;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

public class HttpClientUtil {

	private static Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

	/**
	 * 发送post请求
	 * 
	 * @param strURL
	 * @param req
	 * @return
	 */
	public static Map<String, Object> doPostQueryCmd(String strURL, Map<String, String> req) {
		
		log.info("[doPostQueryCmd url :]"+strURL+",param : "+req);
		
		Map<String, Object> json = new HashMap<>();
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;
		BufferedReader br = null;
		try {
			// 构造HttpClient
			httpClient = HttpClients.createDefault();
			// 使用请求路径创建HttpPost请求
			HttpPost httpPost = new HttpPost(strURL);
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
			// 设置参数
			List<NameValuePair> nvps = new ArrayList<>();
			for (Map.Entry<String, String> entry : req.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			// 发送请求
			resp = httpClient.execute(httpPost);
			json.put("status", resp.getStatusLine().getStatusCode());
			StringBuffer sb = new StringBuffer();
			br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			json.put("content", sb.toString());
		} catch (Exception e) {
			log.error("[post error] url: [" + strURL + "] ", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("[close BufferedReader error ]", e);
				}
			}
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e) {
					log.error("[close CloseableHttpResponse error ]", e);
				}
			}
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					log.error("[close CloseableHttpClient error ]", e);
				}
			}
		}
		return json;
	}



	/**
	 * 发送post请求
	 *
	 * @param strURL
	 * @param req
	 * @return
	 */
	public static Map<String, Object> doPostQueryCmdXm(String strURL, Map<String,String> req) {

		log.info("[doPostQueryCmd url :]"+strURL+",param : "+req);

		Map<String, Object> json = new HashMap<>();
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;
		BufferedReader br = null;
		try {
			// 构造HttpClient
			httpClient = HttpClients.createDefault();
			// 使用请求路径创建HttpPost请求
			HttpPost httpPost = new HttpPost(strURL);
			httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
			// 设置参数
			List<NameValuePair> nvps = new ArrayList<>();
			for (Map.Entry<String, String> entry : req.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
			String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			String uuid = reqTime + new Random().nextInt(10000);
			httpPost.setHeader("Accept","application/json; charset=UTF-8");
			httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
			httpPost.setHeader("X-Ca-Resturl",strURL);
			httpPost.setHeader("X-Ca-Timestamp",reqTime);
			httpPost.setHeader("X-Ca-Noncestr",uuid);
			httpPost.setHeader("X-Ca-Auth","283fea92c8b23ac634324aa6989c01d8");
			//signdata
			String signdata = "/pay/unifiedorder"+"\n"+uuid+"\n"+reqTime+"\n"+ JSON.toJSONString(req);
			String Singdata = Base64Utils.encode(signdata.getBytes());
			 System.out.println("Singdata ： " + Singdata);
			httpPost.setHeader("X-Ca-Signature",Singdata);

			// 执行Post请求
			resp = httpClient.execute(httpPost);
			// 将response对象转换成String类型
//			String responseStr = EntityUtils.toString(resp.getEntity(), "utf-8");
			System.out.println("返回结果 ： " + resp);
			json.put("status", resp.getStatusLine().getStatusCode());
			StringBuffer sb = new StringBuffer();
			br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			json.put("content", sb.toString());
		} catch (Exception e) {
			log.error("[post error] url: [" + strURL + "] ", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("[close BufferedReader error ]", e);
				}
			}
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e) {
					log.error("[close CloseableHttpResponse error ]", e);
				}
			}
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					log.error("[close CloseableHttpClient error ]", e);
				}
			}
		}
		return json;
	}

	/*
	 * params 填写的URL的参数 encode 字节编码
	 */
	public static String sendPostMessage(String strURL, Map<String, String> params, String encode) {

		log.info("[sendPostMessage url :]"+strURL+",param : "+params+",encode : "+encode);
		
		StringBuffer stringBuffer = new StringBuffer();

		if (params != null && !params.isEmpty()) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				try {
					stringBuffer.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), encode))
							.append("&");

				} catch (UnsupportedEncodingException e) {
				}
			}
			// 删掉最后一个 & 字符
			stringBuffer.deleteCharAt(stringBuffer.length() - 1);

			try {
				URL url = new URL(strURL);
				HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
				httpURLConnection.setConnectTimeout(3000);
				httpURLConnection.setDoInput(true);// 从服务器获取数据
				httpURLConnection.setDoOutput(true);// 向服务器写入数据

				// 获得上传信息的字节大小及长度
				byte[] mydata = stringBuffer.toString().getBytes();
				// 设置请求体的类型
				httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				httpURLConnection.setRequestProperty("Content-Lenth", String.valueOf(mydata.length));

				// 获得输出流，向服务器输出数据
				OutputStream outputStream = (OutputStream) httpURLConnection.getOutputStream();
				outputStream.write(mydata);

				// 获得服务器响应的结果和状态码
				int responseCode = httpURLConnection.getResponseCode();
				if (responseCode == 200) {

					// 获得输入流，从服务器端获得数据
					InputStream inputStream = (InputStream) httpURLConnection.getInputStream();
					return (changeInputStream(inputStream, encode));

				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return "";
	}

	/*
	 * // 把从输入流InputStream按指定编码格式encode变成字符串String
	 */
	public static String changeInputStream(InputStream inputStream, String encode) {

		// ByteArrayOutputStream 一般叫做内存流
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
		String result = "";
		if (inputStream != null) {

			try {
				while ((len = inputStream.read(data)) != -1) {
					byteArrayOutputStream.write(data, 0, len);

				}
				result = new String(byteArrayOutputStream.toByteArray(), encode);

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return result;
	}
	
	
	
	/**
	 * 发送post请求
	 * 
	 * @param strURL
	 * @param req
	 * @return
	 */
	public static Map<String, Object> doPostQueryCmdGBK(String strURL, Map<String, String> req) {
		log.info("[doPostQueryCmdGBK url :]"+strURL+",param : "+req);
		Map<String, Object> json = new HashMap<>();
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse resp = null;
		BufferedReader br = null;
		try {
			// 构造HttpClient
			httpClient = HttpClients.createDefault();
			// 使用请求路径创建HttpPost请求
			HttpPost httpPost = new HttpPost(strURL);
			httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=GBK");
			// 设置参数
			List<NameValuePair> nvps = new ArrayList<>();
			for (Map.Entry<String, String> entry : req.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "GBK"));
			// 发送请求
			resp = httpClient.execute(httpPost);
			json.put("status", resp.getStatusLine().getStatusCode());
			StringBuffer sb = new StringBuffer();
			br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "GBK"));
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			json.put("content", sb.toString());
		} catch (Exception e) {
			log.error("[post error] url: [" + strURL + "] ", e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					log.error("[close BufferedReader error ]", e);
				}
			}
			if (resp != null) {
				try {
					resp.close();
				} catch (IOException e) {
					log.error("[close CloseableHttpResponse error ]", e);
				}
			}
			if (httpClient != null) {
				try {
					httpClient.close();
				} catch (IOException e) {
					log.error("[close CloseableHttpClient error ]", e);
				}
			}
		}
		return json;
	}

	public static void main(String[] args) {
		/*String SpCode = URLDecoder.decode("201971", "GBK");
		String LoginName = URLDecoder.decode("nj_jmh", "GBK");
		String Password = URLDecoder.decode("409967", "GBK");
		String MessageContent = URLDecoder.decode("您的验证码为：123456", "GBK");
		String UserNumber = URLDecoder.decode(phone, "GBK");
		String SerialNumber = URLDecoder.decode("", "GBK");
		String ScheduleTime = URLDecoder.decode("", "GBK");
		String f =  URLDecoder.decode("1", "GBK");;*/
		
		Map<String, String> obj = new HashMap<>();
		obj.put("SpCode", "201971");
		obj.put("LoginName", "nj_jmh");
		obj.put("Password", "409967");
		obj.put("MessageContent", "您的验证码为：123456");
		obj.put("UserNumber", "13052597018");
		obj.put("SerialNumber", "");
		obj.put("ScheduleTime", "");
		obj.put("f", "1");
		Map<String, Object> map1 = doPostQueryCmdGBK("https://api.ums86.com:9600/sms/Api/Send.do", obj);
		System.out.println(map1.get("content").toString());
	}
}
