package com.qh.paythird.Beagle;

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
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class BeagleService {

    private  static final Logger logger = LoggerFactory.getLogger(BeagleService.class);

    @Autowired
    PayOrderDao payOrderDao;
    @Autowired
    PayService payService;
    @Autowired
    private MerchantService merchantService;

    /**
     * 商户key  10071
     * @param order
     * @return
     */
    private static final String OrderKey = "866dc8953907762746a252b2c97af644";

    /** 订单返回状态码 */
    public static final String VENT_CODE = "1";

    /**
     * 请求地址
     * @param order
     * @return
     */
    private  static  final  String URL = "http://166188.9tian.me/api/gateway";


    public R order(Order order) {
        logger.info("BeagleService 支付通道选择 开始------------------------------------------------------");

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

            if (OutChannel.ali.name().equals(order.getOutChannel())) {
                //支付宝支付
                return pay(order,"alipay.mini");
            }

            logger.error("BeagleService 支付通道选择 不支持的支付渠道：{}", order.getOutChannel());
            return R.error("不支持的支付渠道");
        } finally {
            logger.info("BeagleService 支付通道选择 结束------------------------------------------------------");
        }
    }


    /**
     * 支付数据
     */

    private R pay(Order order, String paytype ) {
        logger.info(" BeagleService 支付数据 ：" + JSONObject.toJSON(order));
        // 跳过SSL 验证
        try {
            SSLView.trustAllHttpsCertificates();
            HttpsURLConnection.setDefaultHostnameVerifier(new SSLView().hv);
            // 请求参数拼接
            TreeMap<String, String> requestMap = new TreeMap<String, String>();
            //商户号
            requestMap.put("app_id","668127");
            //接口名称
            requestMap.put("method",paytype);
            //订单号
            requestMap.put("order_id",order.getOrderNo());
            //订单总金额
            requestMap.put("total_amount",order.getAmount().toString());
            //订单标题
            requestMap.put("subject",order.getTitle());
            //页面跳转通知
            //requestMap.put("notify_url","http://49.4.91.134:8182/pay/notify/Beagle/"+order.getMerchNo()+"/"+order.getOrderNo());
            requestMap.put("notify_url","http://119.3.39.3:8182/pay/notify/Beagle/"+order.getMerchNo()+"/"+order.getOrderNo());
            String text = http_build_query(requestMap);
            System.out.println("输出 ： " + text);
            String sign = MD5.md5(text+"&key="+OrderKey);
            //MD5签名
            requestMap.put("sign",sign);
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
                String code=jsoncontent.getString("code");
                String msg = jsoncontent.get("msg").toString();
                if(!code.equals("10000")){
                    return  R.error(msg);
                }
                int ints = payOrderDao.save(order);
                logger.info("ints : " + ints);
                logger.info("payurl+++++++++++++++："+jsoncontent.get("pay_url").toString());
                JSONObject content = new JSONObject();
                //拿出跳转地址返回
                content.put("payurl",jsoncontent.get("pay_url").toString());
                content.put("msg",msg);
                content.put("code","1");
                return R.okData(content);
            }catch (Exception e) {
                logger.error("Beagle支付 异常：" + e.getMessage());
                e.printStackTrace();
                return R.error(" Beagle 下单失败");
            }
        } catch (Exception e) {
            logger.error("Beagle支付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error(" Beagle 下单失败");
        } finally {
            logger.info("Beagle 支付");
        }
    }

    static String http_build_query(Map<String, String> array){
        String reString = "";
        //遍历数组形成akey=avalue&bkey=bvalue&ckey=cvalue形式的的字符串
        Iterator it = array.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,String> entry =(Map.Entry) it.next();
            String key = (String) entry.getKey();
            Object value =entry.getValue();
            System.out.println(value);
            if(null != value && !"".equals(value)) {
                reString += key+"="+value+"&";
            }
        }
        reString = reString.substring(0, reString.length()-1);
        logger.info("reString : " + reString);
        return reString;
    }

    /**
     * 异步回调通知
     */
    public R notify(Order order, HttpServletRequest request) {

        logger.info("BeagleService 支付回调 开始-------------------------------------------------");
        String msg = "";
        try {
            TreeMap<String, String> params = RequestUtils.getRequestParam(request);
            logger.info("Beagle 回调 参数："+ JSON.toJSONString(params));
            //第三方平台订单号
            String out_trade_no = request.getParameter("out_trade_no");
            String orderid = request.getParameter("order_id");
            BigDecimal total_amount = new BigDecimal(request.getParameter("total_amount"));
            String status = request.getParameter("status");
            String sign = request.getParameter("sign");
            String subject=request.getParameter("subject");
            //组装sign参数
            TreeMap<String ,String> parameters = new TreeMap<String ,String>();
            parameters.put("order_id", orderid);
            parameters.put("out_trade_no", out_trade_no);
            parameters.put("total_amount", total_amount.toString());
            parameters.put("status", status);
            parameters.put("subject",subject);
            String context=http_build_query(parameters);
            context+= "&key="+OrderKey;
            System.out.println("md5+key==========>"+context);
            String mySign = MD5.md5(context);
            //String characterEncoding = "UTF-8";         //指定字符集UTF-8
           // String mySign = createSign(characterEncoding,parameters,OrderKey);
            System.out.println("etext===========>"+mySign);
            if (status.equals(VENT_CODE)){
                //验证参数
                if(sign.equals(mySign)){//验签成功
                    msg = "订单处理完成";
                    order.setOrderState(OrderState.succ.id());
                    order.setBusinessNo(out_trade_no);
                    payOrderDao.update(order);
                    payService.orderDataMsg(order.getMerchNo(),order.getOrderNo());
                    Merchant merchant = merchantService.get(order.getMerchNo());
                    String publicKey = merchant.getPublicKey();
                    logger.info("用户加密key ： " + publicKey);
                    LinkedHashMap jomap=new LinkedHashMap();
                    jomap.put("msg",msg);
                    jomap.put("amount",total_amount);
                    jomap.put("orderNo",order.getOrderNo());
                    jomap.put("code","1");
                    String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    jomap.put("notifyTime",reqTime);
                    //用户公钥做加签处理
                    jomap.put("key",publicKey);
                    String textconts = http_build_query(jomap);
                    String Sign = MD5.md5(textconts);
                    jomap.put("sign",Sign);
                    //用户公钥不做参数传递
                    jomap.remove("key");
                    logger.info("Beagle 支付 回调 用户返回结果 jomap : " + jomap);
                    String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                    logger.info(" Beagle 支付 回调 用户返回结果 ： " +result);
                    //全部完成后修改订单回调状态
                    Order order1=payOrderDao.get(order.getOrderNo());
                    order1.setNoticeState(1);
                    payOrderDao.update(order1);
                    return R.ok(msg);
                    //需返回json {"status":10000}
                }else{
                    logger.info(" Beagle 支付 回调 验证签名不通过");
                    return R.error("验签失败！");
                }
            } else {
                order.setOrderState(OrderState.fail.id());
                msg = "订单处理失败";
                return R.error(msg);
            }

        } catch (Exception e) {
            logger.info("Beagle 支付回调 异常："+e.getMessage());
            e.printStackTrace();
            order.setOrderState(OrderState.fail.id());
            order.setNoticeState(0);
            payOrderDao.update(order);
            return R.error(" Beagle 支付回调 异常：" + e.getMessage());
        } finally{
            logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
            logger.info("BeagleService 支付回调 结束-------------------------------------------------");
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
