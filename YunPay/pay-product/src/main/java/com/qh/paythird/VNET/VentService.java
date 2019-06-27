package com.qh.paythird.VNET;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.dao.PayOrderDao;
import com.qh.pay.domain.Merchant;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayService;
import com.qh.paythird.VNET.utils.*;
import com.qh.paythird.VNET.utils.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VentService {

    private  static final Logger logger = LoggerFactory.getLogger(VentService.class);


    @Autowired
    PayOrderDao payOrderDao;
    @Autowired
    PayService payService;
    @Autowired
    private MerchantService merchantService;

    /** zl商户号 */
    public static final String TEST_MER = "A190515114828342";
    /** zl公钥 */
    public static final String TEST_PUB_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQD0/70l3vVj4y4zfDTFxczuh1Wv\n" +
            "05Ev4OTlCTrKMR+rIGKk0p+3VZDw9wocE/dE4CvQAH3nBY3S3oJBJnG+U+skU5p4\n" +
            "9qkxxv5NaOkN9l5WnGMjqNAd0QeYOMLIu1hzm11lGcVo356a+LjD9/odk59egSs5\n" +
            "NYdgXPX+csjkhOO/0wIDAQAB";


    /** zl私钥 */
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
    /** 测试回调地址 */
    public static final String NORIFY_URL = "https://www.baidu.com";
    /** 订单返回状态码 */
    public static final String VENT_CODE = "1";


    public R order(Order order) {
        logger.info("VentService 支付通道选择 开始------------------------------------------------------");
        try {
            /**
             *
             * 支付宝支付（H5）
             * 支付宝扫码支付
             * 支付宝刷卡支付
             * 微信支付（H5）
             * 微信扫码支付
             * 微信刷卡支付
             * 网银跳转
             *
             */

            if (OutChannel.wx.name().equals(order.getOutChannel())) {
                //微信扫码支付
                return pay(order,"2");
            }

            if (OutChannel.wxposs.name().equals(order.getOutChannel())) {
                //微信刷卡支付
//                return order_wxposs(order);
            }

            if (OutChannel.wap.name().equals(order.getOutChannel())) {
                //微信H5支付
//                return order_wap(order);
            }

            if (OutChannel.wy.name().equals(order.getOutChannel())) {
                //网银支付
//                return order_wy(order);
            }

            if (OutChannel.ali.name().equals(order.getOutChannel())) {
                //支付宝支付
				return pay(order,"1");
            }

            if (OutChannel.aliwap.name().equals(order.getOutChannel())) {
                //支付宝H5支付
//				return order_aliwap(order);
            }

            if (OutChannel.aliposs.name().equals(order.getOutChannel())) {
                //支付宝刷卡支付
//				return order_aliposs(order);
            }

            logger.error("VentService 支付通道选择 不支持的支付渠道：{}", order.getOutChannel());
            return R.error("不支持的支付渠道");
        } finally {
            logger.info("VentService 支付通道选择 结束------------------------------------------------------");
        }
    }



    /**
     * 支付数据
     */

    private R pay(Order order,String paytype) {
        logger.info(" VentService 支付数据 ：" + JSONObject.toJSON(order));
        // 跳过SSL 验证
        try {
            SSLView.trustAllHttpsCertificates();
            HttpsURLConnection.setDefaultHostnameVerifier(new SSLView().hv);
            // 请求参数拼接
            Map<String, String> requestMap = new HashMap<String, String>();
            requestMap.put("mno", TEST_MER);
            requestMap.put("orderno", order.getOrderNo());
            String amount = order.getAmount().multiply(new BigDecimal("100")).toString();
            requestMap.put("amount", StringUtils.substringBefore(amount, "."));// 单位分
            // 支付类型 1=支付宝 2=微信（停用） 3=网关 4=银联 5=百度钱包（停用）6=京东支付（停用）
            requestMap.put("pt_id", paytype);
            // 应用场景 1=移动端 2=PC端
            requestMap.put("device", "2");
            // 签名
            requestMap.put("sign", getSign(requestMap));
            // 除同步通知notify_url和异步通知async_notify_url无需加入参数
            requestMap.put("notify_url", NORIFY_URL);// 需要填写自己的同步通知URL
//            requestMap.put("async_notify_url", NORIFY_URL);// 需要填写自己的异步通知URL
            //requestMap.put("async_notify_url", "http://47.102.149.20:8181/pay/notify/"+"Vent"+"/"+order.getMerchNo()+"/"+order.getOrderNo());
            //requestMap.put("async_notify_url", "http://49.4.91.134:8182/pay/notify/"+"Vent"+"/"+order.getMerchNo()+"/"+order.getOrderNo());
            requestMap.put("async_notify_url", "http://119.3.39.3:8182/pay/notify/"+"Vent"+"/"+order.getMerchNo()+"/"+order.getOrderNo());

            logger.info("【请求参数】" + requestMap.toString());
            // 发起请求
            String result = RQPayUtils.sendPost(PAY_URL, requestMap);
            logger.info("【返回信息】" + result);
            //处理返回结果
            JSONObject jsoncontent = JSON.parseObject(result);
            System.out.println("content : " + jsoncontent);
            JSONObject jsondata = JSON.parseObject(jsoncontent.get("data").toString());

           String payurl = jsondata.get("payurl").toString();
           String payinfo = jsondata.get("payinfo").toString();
           String msg = jsoncontent.get("msg").toString();
           String code = jsoncontent.get("code").toString();
            if(StringUtils.isBlank(result)){
                return R.error("支付返回参数为空");
            }
            int ints = payOrderDao.save(order);
            logger.info("ints : " + ints);
            JSONObject content = new JSONObject();

            //拿出跳转地址返回
            logger.info("获取Vent返回路径 ：" +jsondata.get("payurl").toString() );
            logger.info("获取Vent返回路径 ：" +payurl );
            logger.info("获取Vent返回路径 ：" +jsoncontent );
            content.put("payurl",payurl);
            content.put("payinfo",payinfo);
            content.put("msg",msg);
            content.put("code",code);
            return R.okData(content);
        } catch (Exception e) {
            logger.error("Vent支付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error(" Vent 下单失败");
        } finally {
            logger.info("Vent 支付");
        }

    }



    /**
     * 签名
     *
     * @param params
     * @return
     * @date 2018-12-27
     */
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
//			String str=new String(RSAUtils.decryptByPublicKey(Base64.decode(sign), TEST_PUB_KEY));
//			System.out.println("公钥解密=="+str);
//
//			//生成数字签名
//			String qming=RSAUtils.sign(Base64.decode(sign), TEST_PRI_KEY);
//			System.out.println("数字签名："+qming);
//
//			//验签名
//			boolean boo=RSAUtils.verify(Base64.decode(sign), TEST_PUB_KEY, qming);
//			System.out.println(boo);



        } catch (Exception e) {
            e.printStackTrace();
        }
        return sign;
    }


    /**
     * @Description 支付回调
     * @param order
     * @param request
     * @return
     */
    public R notify(Order order, HttpServletRequest request) {

        logger.info("VentService 支付回调 开始-------------------------------------------------");
        String msg = "";
        try {
            TreeMap<String, String> params = RequestUtils.getRequestParam(request);
            logger.info("Vent 回调 参数："+ JSON.toJSONString(params));
            String userid = request.getParameter("mno");
            //第三方平台订单号
            String s_orderno = request.getParameter("s_orderno");
            String orderno = request.getParameter("orderno");
            String code = request.getParameter("status");
            BigDecimal amount = new BigDecimal(request.getParameter("amount"));
            String paytime = request.getParameter("paytime");
            String sign = request.getParameter("sign");
            String payMerch = order.getPayMerch();

            if (!code.equals(VENT_CODE)){
                order.setOrderState(OrderState.fail.id());
                msg = "支付失败";
                return R.error(msg);
            }
            //验证参数
            boolean sign_verify = checkSign(sign);
            if(sign_verify){//验签成功
                msg = "支付成功";
                order.setOrderState(OrderState.succ.id());
                order.setNoticeState(1);
                order.setBusinessNo(s_orderno);
                payOrderDao.update(order);
                payService.orderDataMsg(order.getMerchNo(),order.getOrderNo());
                Merchant merchant = merchantService.get(order.getMerchNo());
                String publicKey = merchant.getPublicKey();
                logger.info("用户加密key ： " + publicKey);
                String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                LinkedHashMap jomap=new LinkedHashMap();
                jomap.put("msg",msg);
                jomap.put("amount",amount.divide(new BigDecimal("100")).toString());
                jomap.put("orderNo",order.getOrderNo());
                jomap.put("code",code);
                jomap.put("notifyTime",reqTime);
                //用户公钥做加签处理
                jomap.put("key",publicKey);
                String text = http_build_query(jomap);
                String Sign = MD5.md5(text);

                jomap.put("sign",Sign);
                //用户公钥不做参数传递
                jomap.remove("key");
                logger.info("Vent 支付 回调 用户返回结果 jomap : " + jomap);
                String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                logger.info(" Vent 支付 回调 用户返回结果 ： " +result);
                return R.ok(msg);
            }else{
                logger.info(" Vent 支付 回调 验证签名不通过");
                return R.error("验签失败！");
            }

        } catch (Exception e) {
            logger.info("Vent 支付回调 异常："+e.getMessage());
            e.printStackTrace();
            order.setOrderState(OrderState.fail.id());
            payOrderDao.update(order);
            return R.error(" Vent 支付回调 异常：" + e.getMessage());
        } finally{
            logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
            logger.info("VentService 支付回调 结束-------------------------------------------------");
        }
    }


    static String http_build_query(LinkedHashMap<String, Object> array){
        String reString = "";
        //遍历数组形成akey=avalue&bkey=bvalue&ckey=cvalue形式的的字符串
        Iterator it = array.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,String> entry =(Map.Entry) it.next();
            String key = (String) entry.getKey();
            Object value =entry.getValue();
            System.out.println(value);
            reString += key+"="+value+"&";
        }
        reString = reString.substring(0, reString.length()-1);
        System.out.println("reString : " + reString);
        //将得到的字符串进行处理得到目标格式的字符串
//        reString = java.net.URLEncoder.encode(reString);
        System.out.println("reString : " + reString);
        reString = reString.replace("%3D", "=").replace("%26", "&").replace("%3A",":").replace("%2F","/");
        return reString;
    }

    /**
     * 异步回调验签
     * @param sign
     * @return
     * @date 2018-12-27
     */
    private boolean checkSign(String sign) {
        //公钥解密
        String str;
        boolean boo=false;
        try {
            str = new String(RSAUtils.decryptByPublicKey(Base64.decode(sign), TEST_PUB_KEY));
            System.out.println("公钥解密=="+str);
//
//			//生成数字签名
            String qming=RSAUtils.sign(Base64.decode(sign), TEST_PRI_KEY);
            System.out.println("数字签名："+qming);
//
////		//验签名
            boo=RSAUtils.verify(Base64.decode(sign), TEST_PUB_KEY, qming);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return boo;
    }
}
