package com.qh.paythird.taiShan.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpUtil {
    /**
     * 
     * @param url
     * @param data
     * @return
     * @throws Exception
     */
    public static String postHttpRequest(String url, String data) {
        // 创建链接
        HttpURLConnection hconn = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader reader = null;
        String returneddata = "";
        try {
            hconn = (HttpURLConnection) new URL(url).openConnection();
            hconn.setRequestMethod("POST"); // 设置为post请求
            hconn.setDoInput(true);
            hconn.setDoOutput(true);
            hconn.setUseCaches(false);
            hconn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            hconn.setConnectTimeout(30000); // 30s
            hconn.setReadTimeout(30000); // 30s
            
            os = hconn.getOutputStream();
            byte[] f = data.getBytes("UTF-8");
            os.write(f, 0, f.length);
            os.flush();
            // 接收数据
            int code = hconn.getResponseCode();
            String sCurrentLine = "";
            // url访问成功
            if (code == 200)
            {
                is = hconn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                while (null != (sCurrentLine = reader.readLine())){
                    if (sCurrentLine.length() > 0){
                        returneddata = returneddata + sCurrentLine.trim();
                    }
                }
            }
            System.out.println("http-code:" + code);
            
        } catch (Exception e) {
            
            e.printStackTrace();
        } finally {
            if (hconn != null) {
                hconn.disconnect();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null)
            {
                try
                {
                    reader.close();
                    reader = null;
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        return returneddata;
    }
    
}
