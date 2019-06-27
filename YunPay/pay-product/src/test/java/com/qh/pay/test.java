package com.qh.pay;

import com.alibaba.fastjson.JSONObject;
import com.qh.paythird.VNET.utils.*;
import com.qh.paythird.VNET.utils.Base64;

import javax.net.ssl.HttpsURLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

public class test {

    /** 测试商户号 */
    public static final String TEST_MER = "A190515114828342";
    /** 测试公钥 */
    public static final String TEST_PUB_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD0/70l3vVj4y4zfDTFxczuh1Wv\n" +
            "05Ev4OTlCTrKMR+rIGKk0p+3VZDw9wocE/dE4CvQAH3nBY3S3oJBJnG+U+skU5p4\n" +
            "9qkxxv5NaOkN9l5WnGMjqNAd0QeYOMLIu1hzm11lGcVo356a+LjD9/odk59egSs5\n" +
            "NYdgXPX+csjkhOO/0wIDAQAB";


    /** 测试私钥 */
    public static final String TEST_PRI_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAPT/vSXe9WPjLjN8\n" +
            "NMXFzO6HVa/TkS/g5OUJOsoxH6sgYqTSn7dVkPD3ChwT90TgK9AAfecFjdLegkEm\n" +
            "cb5T6yRTmnj2qTHG/k1o6Q32XlacYyOo0B3RB5g4wsi7WHObXWUZxWjfnpr4uMP3\n" +
            "+h2Tn16BKzk1h2Bc9f5yyOSE47/TAgMBAAECgYBhcnL2ZDcwcsUGn2mSHHnFijjP\n" +
            "xZPi+k2wt7oVf/J7q1bw/gGe9z6+SVRRFLPuRkfG5NsvD2t4qx4d8b+eF1zX/9b2\n" +
            "NddTzxWuHKBZQh33OxfPVt4NynbzVVTx+w11q+1C9BLqg2bqi/jbDNw7RhstBHAg\n" +
            "DE4rs7kw/Ao0R2RtuQJBAP28TVUVnYwsBQqOEF4lyDY3JeWOrznOpy7Gq52ef9vg\n" +
            "rxgQIb6mLpkgRd8DtNj2/xO1rfIkBI1jm1N3rvqb818CQQD3L3oK8GY3DiOJkoxb\n" +
            "DfMzH2gRFPn0CC7bw+Ll7gjoeuKtrCOBZaa64GGFif7WYyn/AzR2XbEiqEBTJlkL\n" +
            "5hwNAkA5DHBas/xzOqZ7hAt8D4SfY/DaVyVgmu0N4E9PpgZbWn4jq+TaZ6TMeuwa\n" +
            "w0uTCJ27Qbr1WVHItF+E+cDWyd+DAkAywXPs5RoH+gZADHB3jfC/MZa70zPT8Q42\n" +
            "IA3qKqt3mSwMf0k3G7lVrBc6RXCncus/qtfN6kUiGaxDpp8oO1LNAkEA0DvQEYOL\n" +
            "y/tozRb516CnYkf6uZuDtZUIzkqEd84HdsWGP8D/Z65Z5XDR0+TIotJsYPqFmXk6\n" +
            "+dOp/T2Y/ok8+g==";
    /** 支付请求地址 */
    public static final String PAY_URL = "https://api.v-pays.net";
    /** 订单查询地址 */
    public static final String QUERY_URL = "https://api.v-pays.net/status/";
//    public static final String QUERY_URL = "https://www.a-pay.vip/status/";
    /** 测试回调地址 */
    public static final String NORIFY_URL = "https://www.baidu.com";


    public static void query()throws Exception{
        // 跳过SSL 验证
        SSLView.trustAllHttpsCertificates();
        HttpsURLConnection.setDefaultHostnameVerifier(new SSLView().hv);
        // 请求参数拼接
        Map<String, String> requestMap = new HashMap<String, String>();
        requestMap.put("mno", TEST_MER);
        requestMap.put("orderno", "2019051514581451090");
        requestMap.put("amount", "1000");
        requestMap.put("sign", getSign(requestMap));
        System.out.println("【请求参数】" + requestMap.toString());
        // 发起请求
        String result = RQPayUtils.sendPost(QUERY_URL, requestMap);
        System.out.println("【返回信息】" + result);
        /**
         * 返回示例：
         * 【返回信息】{"
         * code":"success","
         * msg":"请求查询订单交易信息","
         * data":{"
         * s_orderno":"2018122715301038297589570946","
         * m_orderno":"20181227153004264","
         * status":0,"
         * pt_id":1,"
         * addtime":"2018-12-27 15:30:10","
         * paytime":"","
         * paystatus":"未支付"}}
         */


    }


    public static String getSign(Map<String, String> params) {
        TreeMap<String, String> param = new TreeMap<String, String>(params);
        String signInfo = "";
        for (String pkey : param.keySet()) {
            signInfo += pkey + "=" + param.get(pkey) + "&";
        }
        signInfo = signInfo.substring(0, signInfo.length() - 1);
        System.out.println("signInfo:" + signInfo);
        String sign = "";// 生成签名
        try {
            //私钥加密
            sign = Base64.encode(RSAUtils.encryptByPrivateKey(signInfo.getBytes("UTF-8"), TEST_PRI_KEY));
            /**异步回调时 公钥解密--私钥生成数字签名---验签---返回true  则验签完成*/
//			//公钥解密
//            sign=new String(RSAUtils.decryptByPublicKey(Base64.decode(sign), TEST_PUB_KEY));
//			System.out.println("公钥解密=="+sign);
//
//			//生成数字签名
//			String qming=RSAUtils.sign(Base64.decode(sign), TEST_PRI_KEY);
//			System.out.println("数字签名："+qming);
//
////			//验签名
//			boolean boo=RSAUtils.verify(Base64.decode(sign), TEST_PUB_KEY, qming);
//			System.out.println(boo);



        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }

    public static void main(String[] args) throws  Exception{
//        RedisUtil.removeOrder("SH992255","2019050915151371");
            //query();
        //check();
        String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        TreeMap map=new TreeMap();
        map.put("merchNo","SH992255");
        map.put("date",reqTime);
        String s=http_build_query(map);
        s+="&key="+"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQAq0/GuQOAifRYiE4htVrxhWVco+dTpK4zvTqZpfOVwEjVPe7IsvSX4usny3biI5fyXXclYyZCGTmy0Q3w8WqPZBFngkDgabFa7IME7stgODEEWnVGdDXE0RlCNt+Lut1zF+EJ1ekBI+TNO8y6MpNsrjVusUOydpMHZ5+8Xq/iwIDAQAB";
        String sign=MD5.md5(s);
        System.out.println("sign_"+sign);
        JSONObject jo = new JSONObject();
        jo.put("merchNo", "SH992255");
        jo.put("date",reqTime);
        jo.put("sign", sign);
        System.out.println(jo.toJSONString());
    }



     /*static void make() throws Exception{
         // 创建 FileReader 对象
         FileReader fr = new FileReader(file);
         char[] a = new char[50];
         fr.read(a); // 从数组中读取内容
         for (char c : a)
             System.out.print(c); // 一个个打印字符
         fr.close();
    }
*/

    static String http_build_query(Map<String, String> array){
        String reString = "";
        //遍历数组形成akey=avalue&bkey=bvalue&ckey=cvalue形式的的字符串
        Iterator it = array.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,String> entry =(Map.Entry) it.next();
            String key = (String) entry.getKey();
            Object value =entry.getValue();
            reString += key+"="+value+"&";
        }
        reString = reString.substring(0, reString.length()-1);
        System.out.println("reString : " + reString);
        //将得到的字符串进行处理得到目标格式的字符串
        return reString;
    }

}
