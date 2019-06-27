package com.qh.paythird.weiSao;


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
import com.qh.paythird.weiSao.utils.MD5;
import com.qh.paythird.weiSao.utils.RQPayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
public class WeiSaoService {

    private  static final Logger logger = LoggerFactory.getLogger(WeiSaoService.class);


    @Autowired
    PayOrderDao payOrderDao;
    @Autowired
    PayService payService;
    @Autowired
    private MerchantService merchantService;

    /** zl商户号 */
    public static final String TEST_MER = "1771487158";

    /** zl公钥 */
    public static final String TEST_PUB_KEY = "fjcxkyvsrmfmsg8b1wlbo9reilwtsn24";

    /** 支付请求地址 */
    public static final String PAY_URL = "http://47.111.169.22/smartpayment/pay/gateway";

    /** 测试回调地址 */
    public static final String NORIFY_URL = "https://www.baidu.com";

    /** 订单返回状态码 */
    public static final String VENT_CODE = "0";


    public R order(Order order) {
        logger.info("WeiSaoService 支付通道选择 开始------------------------------------------------------");
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

            logger.error("WeiSaoService 支付通道选择 不支持的支付渠道：{}", order.getOutChannel());
            return R.error("不支持的支付渠道");
        } finally {
            logger.info("WeiSaoService 支付通道选择 结束------------------------------------------------------");
        }
    }



    /**
     * 支付数据
     */

    private R pay(Order order, String paytype) {
        logger.info(" WeiSaoService 支付数据 ：" + JSONObject.toJSON(order));
        // 跳过SSL 验证
        try {
            TreeMap<String, String>  params = new TreeMap<>();
            params.put("service", "OUT_C_A003902");	//根据请求的支付类型不一样， 选择不同的支付方式
            params.put("version", "1.0");				//请求版本 1.0 请勿更改
            params.put("charset", "UTF-8"); 			//字符编码请勿更改
            params.put("sign_type", "MD5");   			//目前仅支持md5签名的方式
            params.put("merchant_id", TEST_MER);  //根据平台分配的merchantid, 替换配置
            params.put("out_trade_no", order.getOrderNo());   //商家系统的订单号， 请保持在商家系统里面唯一
            params.put("goods_desc", "test");    		//购买物品的描述
            params.put("total_amount", order.getAmount().toString());  		//价格 精确到小数点后两位
            //params.put("notify_url", "http://49.4.91.134:8182/pay/notify/weiSao/"+order.getMerchNo()+"/"+order.getOrderNo()); //支付成功后的回调地址
            params.put("notify_url", "http://119.3.39.3:8182/pay/notify/weiSao/"+order.getMerchNo()+"/"+order.getOrderNo()); //支付成功后的回调地址
            params.put("return_url", "https://www.baidu.com");	//支付成功后的同步跳转地址  目前不支持
            params.put("nonce_str", String.valueOf(new Date().getTime()));//随机字符串

            String md5Str=http_build_query(params);
            String sign = MD5.md5(md5Str+"&key=" + TEST_PUB_KEY);
            params.put("sign", sign);
            String result = RQPayUtils.sendPost(PAY_URL, params);
            logger.info("【返回信息】" + result);
            if(StringUtils.isBlank(result)){
                return R.error("支付返回参数为空");
            }
            //处理返回结果
            JSONObject jsoncontent = JSON.parseObject(result);
            String status=jsoncontent.getString("status");
            System.out.println("status : " + jsoncontent.get("status"));
            if(status.equals("0")){
                if(null != jsoncontent.getString("err_msg")){
                    return R.error(jsoncontent.getString("err_msg"));
                }
                String payurl = jsoncontent.get("pay_info").toString();
                int ints = payOrderDao.save(order);
                logger.info("ints : " + ints);
                JSONObject content = new JSONObject();
                //拿出跳转地址返回
                logger.info("获取WeiSao返回路径 ：" +jsoncontent.get("pay_info").toString() );
                logger.info("获取WeiSao返回路径 ：" +payurl );
                logger.info("获取WeiSao返回路径 ：" +jsoncontent );
                content.put("payurl",payurl);
                content.put("msg","下单成功");
                content.put("code","1");
                return R.okData(content);
            }else {
                return R.error("下单失败");
            }
        } catch (Exception e) {
            logger.error("WeiSao支付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error(" WeiSao 下单失败");
        } finally {
            logger.info("WeiSao 支付");
        }

    }


    /**
     * @Description 支付回调
     * @param order
     * @param request
     * @return
     */
    public R notify(Order order, HttpServletRequest request) {

        logger.info("WeiSaoService 支付回调 开始-------------------------------------------------");
        String msg = "";
        try {
            TreeMap<String, String> params = RequestUtils.getRequestParam(request);
            logger.info("WeiSao 回调 参数："+ JSON.toJSONString(params));
            TreeMap map=new TreeMap();
            //第三方订单号
            String transaction_id = request.getParameter("transaction_id");
            String charset = request.getParameter("charset");
            String nonce_str = request.getParameter("nonce_str");
            //上游的第三方的订单号
            String out_transaction_id = request.getParameter("out_transaction_id");
            String merchant_id = request.getParameter("merchant_id");
            String fee_type = request.getParameter("fee_type");
            String version = request.getParameter("version");
            String pay_result = request.getParameter("pay_result");
            String real_amount = request.getParameter("real_amount");
            String total_amount = request.getParameter("total_amount");
            //平台的订单号
            String out_trade_no = request.getParameter("out_trade_no");
            String trade_type = request.getParameter("trade_type");
            String result_code = request.getParameter("result_code");
            String time_end = request.getParameter("time_end");
            String sign_type = request.getParameter("sign_type");
            String account = request.getParameter("account");
            String status = request.getParameter("status");
            String sign = request.getParameter("sign");

            map.put("version",version);
            map.put("charset",charset);
            map.put("sign_type",sign_type);
            map.put("status",status);
            map.put("result_code",result_code);
            map.put("merchant_id",merchant_id);
            map.put("nonce_str",nonce_str);

            map.put("trade_type",trade_type);
            map.put("pay_result",pay_result);
            map.put("transaction_id",transaction_id);
            map.put("out_transaction_id",out_transaction_id);
            map.put("out_trade_no",out_trade_no);
            map.put("total_amount",total_amount);
            map.put("real_amount",real_amount);
            map.put("fee_type",fee_type);
            map.put("time_end",time_end);

            map.put("account",account);
            String str=http_build_query(map);
            String signStr = MD5.md5(str+"&key=" + TEST_PUB_KEY);
            System.out.println("yanqian"+signStr);
            if (!result_code.equals(VENT_CODE)){
                order.setOrderState(OrderState.fail.id());
                msg = "订单处理失败";
                return R.error(msg);
            }
            if(sign.equals(signStr)){//验签成功
                msg = "订单处理完成";
                order.setOrderState(OrderState.succ.id());
                order.setNoticeState(1);
                order.setBusinessNo(transaction_id);
                payOrderDao.update(order);
                payService.orderDataMsg(order.getMerchNo(),order.getOrderNo());
                Merchant merchant = merchantService.get(order.getMerchNo());
                String publicKey = merchant.getPublicKey();
                logger.info("用户加密key ： " + publicKey);
                LinkedHashMap jomap=new LinkedHashMap();
                jomap.put("msg",msg);
                jomap.put("amount",real_amount);
                jomap.put("orderNo",order.getOrderNo());
                jomap.put("code","1");
                jomap.put("notifyTime",time_end);
                //用户公钥做加签处理
                jomap.put("key",publicKey);
                String text = http_build_query(jomap);
                String Sign = MD5.md5(text);
                jomap.put("sign",Sign);
                //用户公钥不做参数传递
                jomap.remove("key");
                logger.info("WeiSao 支付 回调 用户返回结果 jomap : " + jomap);
                String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                logger.info(" WeiSao 支付 回调 用户返回结果 ： " +result);
                return R.ok(msg);
            }else{
                logger.info(" WeiSao 支付 回调 验证签名不通过");
                return R.error("验签失败！");
            }
        } catch (Exception e) {
            logger.info("WeiSao 支付回调 异常："+e.getMessage());
            e.printStackTrace();
            order.setOrderState(OrderState.fail.id());
            payOrderDao.update(order);
            return R.error(" WeiSao 支付回调 异常：" + e.getMessage());
        } finally{
            logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
            logger.info("WeiSaoService 支付回调 结束-------------------------------------------------");
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
     *//*
    private boolean checkSign(String sign) {
        //公钥解密
        String str;
        boolean boo=false;
        try {
            str = new String(RSAUtils.decryptByPublicKey(Base64.decode(sign), TEST_PUB_KEY));
            System.out.println("公钥解密=="+str);
//
//			//生成数字签名
            String qming= RSAUtils.sign(Base64.decode(sign), TEST_PRI_KEY);
            System.out.println("数字签名："+qming);
//
////		//验签名
            boo= RSAUtils.verify(Base64.decode(sign), TEST_PUB_KEY, qming);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return boo;
    }*/
}
