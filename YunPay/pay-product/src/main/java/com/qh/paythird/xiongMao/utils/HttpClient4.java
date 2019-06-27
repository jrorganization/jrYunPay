package com.qh.paythird.xiongMao.utils;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;


public class HttpClient4 {

    /**
     * 熊猫公钥
     */
    public final static String PublicKeyXiongmao = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQ6Apl/5Y9p/BvwRNXLLX+pCCCKKjyP4/7grgD+xAzS93u1CwDvYHoiyo3vsQhmmHX9Mnhci5khfIZffDipkMdKjaEvOutCGFwIHzHC08d7x/hL5XdJ/rFJuSwScEK7xmcJpfmNKHvpMBUdh48Hxifr9B9GuEUXbopZWZxfzJCzQIDAQAB";
    /**
     * 熊猫公私钥
     */
    public final static String privateKeyXiongmao = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJDoCmX/lj2n8G/BE1cstf6kIIIoqPI/j/uCuAP7EDNL3e7ULAO9geiLKje+xCGaYdf0yeFyLmSF8hl98OKmQx0qNoS8660IYXAgfMcLTx3vH+Evld0n+sUm5LBJwQrvGZwml+Y0oe+kwFR2HjwfGJ+v0H0a4RRduillZnF/MkLNAgMBAAECgYB87WARobEwhLnYTxfSfzyERZG1RUKqXzxNtNvaqrfD0bOLdKZhrx7xkhEasD+9TLDwEx19XQg8J/KaIabscDTI/jf7hvuyeL0mERFM3NXuLo1l1R56n8h97FmgTBi8d9Ql7ndBAwwzTK8cfKYYpzLG6+SrNsWZtJ+pvWkCQtkjgQJBAOQ4zMNLSCLueOcm+Iw5yTt//6QYdptOrLzPZbP1WoQQ/FvcblsmcRQb1P65TxQ4vSXh3/EAwDOgFnKN+zTCBS0CQQCiizOT+ZUrxTBP7wFo0ceaWTz2Xh//biCvGYr9DyEA8ve041VU8sJwNWUlw1W1lUPdJyG9c+30nz6QCqAFXPghAkEA4mWqU036CJUTIROa2th0VO8cNbgC6Px6BW+kj4okugBzp9kbLJcM9ArMF8jStte2Y78XvWemQ1BbFFbeza5vHQJAfAlTu7j6n2Mjgev2HGHxOpSck7iyHD6SzGvmh0PzQIEoi63rIR77R5tHa3DLR/z2w52n/qWn0UNv/4VMJauTYQJAIBLfNg714fYrNCgEHkVKym91/2bFj5py9XKGhkf1LxoBStiAiy6EzCHkWHethT9yg3blwOnQYz1f+nvBISdenw==";



    public static String doGet(String url) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result = "";
        try {
            // 通过址默认配置创建一个httpClient实例
            httpClient = HttpClients.createDefault();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(url);
            // 设置请求头信息，鉴权
            httpGet.setHeader("Authorization", "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0");
            // 设置配置请求参数
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 连接主机服务超时时间
                    .setConnectionRequestTimeout(35000)// 请求超时时间
                    .setSocketTimeout(60000)// 数据读取超时时间
                    .build();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            response = httpClient.execute(httpGet);
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String doPost(String url, Map<String, Object> paramMap) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse httpResponse = null;
        String result = "";
        // 创建httpClient实例
        httpClient = HttpClients.createDefault();
        // 创建httpPost远程连接实例
        HttpPost httpPost = new HttpPost(url);
        // 配置请求参数实例
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(35000)// 设置连接主机服务超时时间
                .setConnectionRequestTimeout(35000)// 设置连接请求超时时间
                .setSocketTimeout(60000)// 设置读取数据连接超时时间
                .build();
        // 为httpPost实例设置配置
        httpPost.setConfig(requestConfig);
        // 设置请求头
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
        // 封装post请求参数
        if (null != paramMap && paramMap.size() > 0) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            // 通过map集成entrySet方法获取entity
            Set<Entry<String, Object>> entrySet = paramMap.entrySet();
            // 循环遍历，获取迭代器
            Iterator<Entry<String, Object>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Entry<String, Object> mapEntry = iterator.next();
                nvps.add(new BasicNameValuePair(mapEntry.getKey(), mapEntry.getValue().toString()));
            }
            // 为httpPost设置封装好的请求参数
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
                httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
//                String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
//                String uuid = reqTime + new Random().nextInt(10000);
//                httpPost.setHeader("Accept","application/json; charset=UTF-8");
//                httpPost.setHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; rv:2.0.1) Gecko/20100101 Firefox/4.0.1");
//                httpPost.setHeader("X-Ca-Resturl",url);
//                httpPost.setHeader("X-Ca-Timestamp","2019051409530");
//                httpPost.setHeader("X-Ca-Noncestr","201905140953035714");
////                httpPost.setHeader("X-Ca-Noncestr",uuid);
//                httpPost.setHeader("X-Ca-Auth","283fea92c8b23ac634324aa6989c01d8");
//                //signdata
//
//                String data = JSON.toJSONString(paramMap).replace("\\","");
//                 System.out.println("data[] :"+data);
//
//                String signdata = "/pay/unifiedorder"+"\n"+"\n"+"201905140953035714"+"\n"+"2019051409530"+"\n"+ data;
//                System.out.println("signdata ： " + signdata);
//                String Singdata = Base64Utils.encode((signdata.getBytes("utf-8")));
//                System.out.println("Singdata Base64Utils :" + Singdata);
//                String rsadata = rsaSign(data,privateKeyXiongmao,"utf-8");
//                byte[] rsadata3 = RSAUtil.encryptByPrivateKey(JSON.toJSONBytes(data),privateKeyXiongmao);
//                byte[] rsadata2 = RSAUtil.encryptByPrivateKey(data.getBytes(),privateKeyXiongmao);
//                String ccc3 = Base64Utils.encode(rsadata3);
//                String ccc2 = Base64Utils.encode(rsadata2);
//                 System.out.println("RSAUtil ccc : " + rsadata);
//                 System.out.println("RSAUtil rsadata3 : " + ccc3);
//                 System.out.println("RSAUtil rsadata3 : " + ccc2);
//                httpPost.setHeader("X-Ca-Signature",rsadata);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            // httpClient对象执行post请求,并返回响应参数对象
            httpResponse = httpClient.execute(httpPost);
            // 从响应对象中获取响应内容
            HttpEntity entity = httpResponse.getEntity();
            result = EntityUtils.toString(entity);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != httpResponse) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


//    public static String rsaSign(String content, String privateKey, String charset) throws SignatureException {
//        try {
////            PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey.getBytes()));
////
////            Signature signature = Signature.getInstance("SHA1WithRSA");
////            signature.initSign(priKey);
////            if (StringUtils.isEmpty(charset)) {
////                signature.update(content.getBytes());
////            } else {
////                signature.update(content.getBytes(charset));
////            }
////
////            byte[] signed = signature.sign();
////            return Base64Utils.encode(signed);
//            PrivateKey priKey = getPrivateKeyFromPKCS8("RSA", new ByteArrayInputStream(privateKey.getBytes()));
//
//            Signature signature = Signature.getInstance("SHA1WithRSA");//MD5withRSA  SHA1WithRSA SHA256WithRSA
//            signature.initSign(priKey);
//            if (StringUtils.isEmpty(charset)) {
//                signature.update(content.getBytes());
//            } else {
//                signature.update(content.getBytes(charset));
//            }
//            byte[] signed = signature.sign();
//            return new String(Base64.encodeBase64(signed));
//        } catch (Exception e) {
//            throw new SignatureException("RSAcontent = " + content + "; charset = " + charset, e);
//        }
//    }

//    public PrivateKey getPrivateKeyFromPKCS8(String algorithm, InputStream ins) throws Exception {
//        if (ins == null || StringUtils.isEmpty(algorithm)) {
//            return null;
//        }
//
//        KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
//        byte[] encodedKey = StreamUtil.readText(ins).getBytes();
//        encodedKey = Base64.decodeBase64(encodedKey);
//        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodedKey));
//    }

}
