package com.qh.paythird.fangkuai.utils;

import net.sf.json.JSONObject;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * 工具类
 * @author poly
 */
public class RQPayUtils {
	
	
	
	/**
	 * 平台返回sign 代付请求返回验签
	 */
	public static boolean payVerify(JSONObject object, String key){
		//获取平台返回sign 进行解密获取到MD5密文  f667dc880188bfac55e594a30259de26   f6ee8d3dac2827542b369ee48ba251b5
		String md5=null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			md5 = new String(RSAUtils.decryptByPublicKey(Base64.decode(object.getString("sign")), key));
			System.out.println("解密成功获取MD5密文:"+md5);//公钥解密
		} catch (Exception e) {
			System.out.println("【解密失败,联系管理员】");
			return false;
		}
		if(object.containsKey("code")){
			map.put("code", object.getString("code"));
		}
		if(object.containsKey("msg")){
			map.put("msg", object.getString("msg"));
		}
		if(object.containsKey("merId")){
			map.put("merId", object.getString("merId"));
		}
		if(object.containsKey("orderNo")){
			map.put("orderNo", object.getString("orderNo"));
		}
		if(object.containsKey("orderStatus")){
			map.put("orderStatus", object.getString("orderStatus"));
		}
		if(object.containsKey("orderDesc")){
			map.put("orderDesc", object.getString("orderDesc"));
		}
		if(object.containsKey("tbOrderNo")){
			map.put("tbOrderNo", object.getString("tbOrderNo"));
		}
		if(object.containsKey("amount")){
			map.put("amount", object.getString("amount"));
		}
		if(object.containsKey("accIdCard")){
			map.put("accIdCard", object.getString("accIdCard"));
		}
		if(object.containsKey("mobile")){
			map.put("mobile", object.getString("mobile"));
		}
		if(object.containsKey("bankName")){
			map.put("bankName", object.getString("bankName"));
		}
		if(object.containsKey("bankId")){
			map.put("bankId", object.getString("bankId"));
		}
		if(object.containsKey("branchName")){
			map.put("branchName", object.getString("branchName"));
		}
		if(object.containsKey("businessType")){
			map.put("businessType", object.getString("businessType"));
		}
		if(object.containsKey("businessNo")){
			map.put("businessNo", object.getString("businessNo"));
		}
		if(object.containsKey("tfbRsptime")){
			map.put("tfbRsptime", object.getString("tfbRsptime"));
		}
		if(object.containsKey("amountType")){
			map.put("amountType", object.getString("amountType"));
		}
		if(object.containsKey("dfOrderNo")){
			map.put("dfOrderNo", object.getString("dfOrderNo"));
		}
		if(object.containsKey("accName")){
			map.put("accName", object.getString("accName"));
		}
		return verify(map, md5);
	}
	
	/**验签*/
	public static boolean verify(Map<String, String> map,String md5){
		TreeMap<String, String> param = new TreeMap<String, String>(map);
		String signInfo = "";
		for (String pkey : param.keySet()) {
			signInfo += pkey + "=" + param.get(pkey) + "&";
		}
		signInfo = signInfo.substring(0, signInfo.length() - 1);
		System.out.println("[签名字段排列]："+signInfo);
		//MD5加密
		String md5back;
		try {
			md5back = getMD5(signInfo);
			System.out.println("【MD5加密密文】:"+md5back);
			if(md5.equals(md5back)){
				return true;
			}else {
				return false;
			}
		} catch (IOException e) {
			return false;
		}
	}
	
	
	
	
	
	
	
	/**
	 * 私钥加密方法
	 * @param args
	 * @date 2018-5-7
	 */
	public static String getSign(Map<String, String> params,String privateKey1) {
		TreeMap<String, String> param = new TreeMap<String, String>(params);
		String signInfo = "";
		String sign = "";// 生成签名
		for (String pkey : param.keySet()) {
			signInfo += pkey + "=" + param.get(pkey) + "&";
		}
		signInfo = signInfo.substring(0, signInfo.length() - 1);
		System.out.println("签名字段排列："+signInfo);
		
		try {
			
//			sign=
			sign = Base64.encode(RSAUtils.encryptByPrivateKey(signInfo.getBytes("UTF-8"), privateKey1));//
			System.out.println("【私钥加密签名】"+sign);
			
//			String str=new String(RSAUtils.decryptByPublicKey(Base64.decode(sign), CommonDemo.PT_PUB_KEY));
//			System.out.println("公钥解密=="+str);//公钥解密
			
			//生成数字签名
//			String qming=RSAUtils.sign(Base64.decode(sign), privateKey1);
//			System.out.println("数字签名："+qming);
			
//			//验签名
//			boolean boo=RSAUtils.verify(Base64.decode(sign), pub, qming);
//			System.out.println(boo);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sign;
	}
	
	
	
	
	
	/**
	 * 需要验证的参数
	 */
	
	
	
	
	/**
	 * 返回参数排序 验证
	 * @param params
	 * @param privateKey1
	 * @param condetionString 排序字符串
	 * @return
	 * @date 2018-12-25
	 */
	public static boolean getSort(Map<String, String> params,String condetionString) {
		TreeMap<String, String> param = new TreeMap<String, String>(params);
		String signInfo = "";
		for (String pkey : param.keySet()) {
			signInfo += pkey + "=" + param.get(pkey) + "&";
		}
		signInfo = signInfo.substring(0, signInfo.length() - 1);
		System.out.println("签名字段排列："+signInfo);
		if(signInfo.equals(condetionString)){
			return true;
		}
		return false;
	}
	
	
	
	
	
	/**请求工具*/
	public static String sendPost(String url, Map<String, String> parameters) {  
        String result = "";// 返回的结果  
        BufferedReader in = null;// 读取响应输入流  
        PrintWriter out = null;  
        StringBuffer sb = new StringBuffer();// 处理请求参数  
        String params = "";// 编码之后的参数  
        try {
            // 编码请求参数  
            if (parameters.size() == 1) {  
                for (String name : parameters.keySet()) {  
                    sb.append(name).append("=").append(java.net.URLEncoder.encode(parameters.get(name), "UTF-8"));
                }  
                params = sb.toString();  
            } else {  
                for (String name : parameters.keySet()) {  
                    sb.append(name).append("=").append(java.net.URLEncoder.encode(parameters.get(name), "UTF-8")).append("&");
                }  
                String temp_params = sb.toString();
                params = temp_params.substring(0, temp_params.length() - 1).replace("%3A",":").replace("%2F","/");
            }  
            // 创建URL对象  
            java.net.URL connURL = new java.net.URL(url);  
            // 打开URL连接  
            java.net.HttpURLConnection httpConn = (java.net.HttpURLConnection) connURL  
                    .openConnection();  
            // 设置通用属性  
            httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
            httpConn.setRequestProperty("Connection", "Keep-Alive");  
            httpConn.setRequestProperty("User-Agent",  
                    "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1)");

            // 设置POST方式  
            httpConn.setDoInput(true);  
            httpConn.setDoOutput(true);
            out = new PrintWriter(new OutputStreamWriter(httpConn.getOutputStream(), "UTF-8"));
            // 发送请求参数
			System.out.println("params===========>"+params);
            out.write(params);
            // flush输出流的缓冲
            out.flush();  
            // 定义BufferedReader输入流来读取URL的响应，设置编码方式  
            in = new BufferedReader(new InputStreamReader(httpConn  
                    .getInputStream(), "UTF-8"));
			return in.readLine();
        } catch (Exception e) {  
            e.printStackTrace();
            return  null;
        } finally {  
            try {  
                if (out != null) {  
                    out.close();  
                }  
                if (in != null) {  
                    in.close();  
                }  
            } catch (IOException ex) {  
                ex.printStackTrace();  
            }  
        }  
        //return result;
    }
	/**
	 * MD5
	 * @param str
	 * @return
	 * @throws IOException
	 * @date 2018-12-13
	 */
	public static String getMD5(String str) throws IOException{
		 String result = "";
	       try {
	           MessageDigest md = MessageDigest.getInstance("MD5");
	           md.update(str.getBytes("UTF-8"));
	           byte b[] = md.digest();
	           int i;
	           StringBuffer buf = new StringBuffer("");
	           for (int offset = 0; offset < b.length; offset++) {
	               i = b[offset];
	               if (i < 0)
	                   i += 256;
	               if (i < 16)
	                   buf.append("0");
	               buf.append(Integer.toHexString(i));
	           }
	           result = buf.toString();
	       } catch (NoSuchAlgorithmException e) {
	           System.out.println(e);
	       }
	       return result;
	}
	
}
