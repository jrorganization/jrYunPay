package com.qh.paythird.JinDuoDuo;

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
import com.qh.paythird.JinDuoDuo.utils.HttpRequestUtil;
import com.qh.paythird.JinDuoDuo.utils.MD5;
import com.qh.paythird.JinDuoDuo.utils.MD5Util;
import com.qh.paythird.JinDuoDuo.utils.RQPayUtils;
import com.qh.paythird.VNET.utils.SSLView;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

@Service
public class JddService {

    private  static final Logger logger = LoggerFactory.getLogger(JddService.class);

    @Autowired
    PayOrderDao payOrderDao;
    @Autowired
    PayService payService;
    @Autowired
    private MerchantService merchantService;
    @Autowired


    /**
     * 商户key  10071
     * @param order
     * @return
     */
    private static final String OrderKey = "65412h1y3w1flgna48lwx1dmiuupdstf";

    /** 订单返回状态码 */
    public static final String VENT_CODE = "00";

    /**
     * 请求地址
     * @param order
     * @return
     */
    private  static  final  String URL = "http://www.u-me.cn/Pay_Index.html";


    public R order(Order order) {
        logger.info("JddService 支付通道选择 开始------------------------------------------------------");

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
//                return pay(order,);
            }

            if (OutChannel.wxposs.name().equals(order.getOutChannel())) {
                //微信刷卡支付
//                return order_wxposs(order);
            }

            if (OutChannel.wap.name().equals(order.getOutChannel())) {
                //微信H5支付
                return pay(order,"wechat");
            }

            if (OutChannel.wy.name().equals(order.getOutChannel())) {
                //网银支付
//                return order_wy(order);
            }

            if (OutChannel.ali.name().equals(order.getOutChannel())) {
                //支付宝支付
//                return pay(order,"alipay");
            }

            if (OutChannel.aliwap.name().equals(order.getOutChannel())) {
                //支付宝H5支付
                return pay(order,"alipay" );
            }

            if (OutChannel.aliposs.name().equals(order.getOutChannel())) {
                //支付宝刷卡支付
//				return order_aliposs(order);
            }

            logger.error("JddService 支付通道选择 不支持的支付渠道：{}", order.getOutChannel());
            return R.error("不支持的支付渠道");
        } finally {
            logger.info("JddService 支付通道选择 结束------------------------------------------------------");
        }
    }


    /**
     * 支付数据
     */

    private R pay(Order order, String paytype ) {
        logger.info("JddServer 支付数据 开始 ------------------------------------------------------");
        logger.info(" JddService 支付数据 ：" + JSONObject.toJSON(order));
        // 跳过SSL 验证
        try {
            SSLView.trustAllHttpsCertificates();
            HttpsURLConnection.setDefaultHostnameVerifier(new SSLView().hv);
            // 请求参数拼接
            TreeMap<String, String> requestMap = new TreeMap<String, String>();
            //商户号
            requestMap.put("pay_memberid","10080"); 
            //订单号
            requestMap.put("pay_orderid",order.getOrderNo());
            //提交时间
            requestMap.put("pay_applydate",order.getReqTime());
            //银行编码
            requestMap.put("pay_bankcode","904");
            //服务端通知
            //requestMap.put("pay_notifyurl","http://49.4.91.134:8182/pay/notify/JDD/"+order.getMerchNo()+"/"+order.getOrderNo());
            requestMap.put("pay_notifyurl","http://119.3.39.3:8182/pay/notify/JDD/"+order.getMerchNo()+"/"+order.getOrderNo());
            //页面跳转通知
            requestMap.put("pay_callbackurl",order.getReturnUrl());
            //订单金额
            requestMap.put("pay_amount",order.getAmount().toString());

            String text = http_build_query(requestMap);
            System.out.println("输出 ： " + text);
            String sign = MD5.md5(text+"&key="+OrderKey);
            //MD5签名
            requestMap.put("pay_md5sign",sign);
            //附加字段
            requestMap.put("pay_attach","fujiaziduan");
            //商品名称
            requestMap.put("pay_productname","payname");
            //商品数量
            requestMap.put("pay_productnum","paynum");
            //商品描述
            requestMap.put("pay_productdesc","chongzhimiaoshu");
            //商户连接地址
            requestMap.put("pay_producturl","");
            //固码支付识别
            requestMap.put("pay_solid","1");
            //固码支付类型
            requestMap.put("pay_type",paytype);
            //返回类型
            requestMap.put("retype","1");

            logger.info("【请求参数】" + requestMap.toString());

            // 发起请求
            String request = http_build_query(requestMap);
            String result = HttpRequestUtil.sendPost(URL, request);
            logger.info("【返回信息】" + result);
            //处理返回结果
            if(StringUtils.isBlank(result)){
                return R.error("支付返回参数为空");
            }
            try{
                JSONObject jsoncontent = JSON.parseObject(result);
                String msg = jsoncontent.get("status").toString();
                if(msg.equals("error")){
                    return  R.error(jsoncontent.getString("msg"));
                }
                int ints = payOrderDao.save(order);
                logger.info("ints : " + ints);
                logger.info("payurl+++++++++++++++："+jsoncontent.get("url").toString());
                JSONObject content = new JSONObject();
                //拿出跳转地址返回
                content.put("payinfo","success");
//                content.put("payurl",jsoncontent.get("url").toString().replaceFirst("www.58hxgou.com","www.u-me.cn"));
                content.put("payurl",jsoncontent.get("url").toString().replaceFirst("http:","https:"));
                content.put("msg",msg);
                content.put("code","1");
                return R.okData(content);


            }catch (Exception e) {
                logger.error("Jdd支付 异常：" + e.getMessage());
                e.printStackTrace();
                return R.error(" Jdd 下单失败");
            }

        } catch (Exception e) {
            logger.error("Jdd支付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error(" Jdd 下单失败");
        } finally {
            logger.info("JddServer 支付数据 结束  ------------------------------------------------------");
        }
    }

    static String http_build_querymd5(TreeMap<String, String> array){
        String reString = "";
        //遍历数组形成akey=avalue&bkey=bvalue&ckey=cvalue形式的的字符串
        Iterator it = array.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,String> entry =(Map.Entry) it.next();
            String key = (String) entry.getKey();
            Object value =entry.getValue();
            System.out.println(value);
            if(null != value && !"".equals(value)) {
                reString += key + "=" + value + "&";
            }
        }
        reString = reString.substring(0, reString.length()-1);
        System.out.println("reString : " + reString);
        logger.info("reString : " + reString);
        return reString;
    }


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
        //将得到的字符串进行处理得到目标格式的字符串
//        reString = java.net.URLEncoder.encode(reString);
        logger.info("reString : " + reString);
        reString = reString.replace("%3D", "=").replace("%26", "&").replace("%3A",":").replace("%2F","/");
        return reString;
    }

    /**
     * 异步回调通知
     */
    public R notify(Order order, HttpServletRequest request) {

        logger.info("JDDService 支付回调 开始-------------------------------------------------");
        String msg = "";
        try {
            TreeMap<String, String> params = RequestUtils.getRequestParam(request);
            logger.info("JDD 回调 参数："+ JSON.toJSONString(params));
            String memberid = request.getParameter("memberid");
            //第三方平台订单号
            String transaction_id = request.getParameter("transaction_id");
            String orderid = request.getParameter("orderid");
            String returncode = request.getParameter("returncode");
            BigDecimal amount = new BigDecimal(request.getParameter("amount"));
            String datetime = request.getParameter("datetime");
            String sign = request.getParameter("sign");

            //组装sign参数
            SortedMap<Object,Object> parameters = new TreeMap<Object,Object>();
            parameters.put("memberid",memberid);
            parameters.put("orderid", orderid);
            parameters.put("transaction_id",transaction_id);
            parameters.put("amount",amount.toString());
            parameters.put("datetime",datetime);
            parameters.put("returncode",returncode);
            String characterEncoding = "UTF-8";         //指定字符集UTF-8
            String mySign = createSign(characterEncoding,parameters,OrderKey);
            System.out.println("etext===========>"+mySign);

            if (returncode.equals(VENT_CODE)){
                //验证参数
                if(sign.equals(mySign)){//验签成功
                    msg = "支付成功";
                    order.setOrderState(OrderState.succ.id());
                    order.setBusinessNo(transaction_id);
                    payOrderDao.update(order);
                    payService.orderDataMsg(order.getMerchNo(),order.getOrderNo());
                    Merchant merchant = merchantService.get(order.getMerchNo());
                    String publicKey = merchant.getPublicKey();
                    logger.info("用户加密key ： " + publicKey);
                    LinkedHashMap jomap=new LinkedHashMap();
                    jomap.put("msg",msg);
                    jomap.put("amount",amount.toString());
                    jomap.put("orderNo",order.getOrderNo());
                    jomap.put("code","1");
                    jomap.put("notifyTime",datetime);
                    //用户公钥做加签处理
                    jomap.put("key",publicKey);
                    String textconts = http_build_query(jomap);
                    String Sign = MD5.md5(textconts);
                    jomap.put("sign",Sign);
                    //用户公钥不做参数传递
                    jomap.remove("key");
                    logger.info("JDD 支付 回调 用户返回结果 jomap : " + jomap);
                    String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                    logger.info(" JDD 支付 回调 用户返回结果 ： " +result);
                    //全部完成后修改订单回调状态
                    Order order1=payOrderDao.get(order.getOrderNo());
                    order1.setNoticeState(1);
                    payOrderDao.update(order1);
                    return R.ok(msg);
                }else{
                    logger.info(" JDD 支付 回调 验证签名不通过");
                    return R.error("验签失败！");
                }
            } else {
                order.setOrderState(OrderState.fail.id());
                msg = "订单处理失败";
                return R.error(msg);
            }

        } catch (Exception e) {
            logger.info("JDD 支付回调 异常："+e.getMessage());
            e.printStackTrace();
            order.setOrderState(OrderState.fail.id());
            order.setNoticeState(0);
            payOrderDao.update(order);
            return R.error(" JDD 支付回调 异常：" + e.getMessage());
        } finally{
            logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
            logger.info("JDDService 支付回调 结束-------------------------------------------------");
        }
    }

    /**
     * sign 验证
     */
    static  boolean MD5sign(String sign,String text){
        boolean returntext = false;
        String md5test = MD5.md5(text);
        return returntext = sign.equals(md5test);
    };


    public static String createSign(String characterEncoding, SortedMap<Object,Object> parameters, String key){
        StringBuffer sb = new StringBuffer();
        StringBuffer sbkey = new StringBuffer();
        Set es = parameters.entrySet();  //所有参与传参的参数按照accsii排序（升序）
        Iterator it = es.iterator();
        while(it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String k = (String)entry.getKey();
            Object v = entry.getValue();
            //空值不传递，不参与签名组串
            if(null != v && !"".equals(v)) {
                sbkey.append(k + "=" + v + "&");
            }
        }
        sbkey=sbkey.append("key="+key);
        System.out.println("字符串:"+sbkey.toString());
        //MD5加密,结果转换为大写字符
        String sign = MD5Util.md5Encrypt32Upper(sbkey.toString());
        System.out.println("MD5加密值:"+sign);
        return sign;
    }
}
