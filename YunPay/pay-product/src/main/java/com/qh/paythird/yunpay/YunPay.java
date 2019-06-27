package com.qh.paythird.yunpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.Base64Utils;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.dao.PayOrderDao;
import com.qh.pay.domain.Merchant;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayService;
import com.qh.paythird.VNET.utils.MD5;
import com.qh.paythird.VNET.utils.RQPayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class YunPay {

    private  static final Logger logger = LoggerFactory.getLogger(YunPay.class);


    @Autowired
    PayOrderDao payOrderDao;

    @Autowired
    PayService payService;
    @Autowired
    private MerchantService merchantService;

    /**
     * 下单路径
     */
    public final static String url = "http://47.102.149.20:8181/pay/order";

    public final static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRATDdlWIjeXHlqVpn2CiwMWtkTJRO998UPlYoGw9cS7ZJVU8tChVKinUbp2HCMdpGHsdnAw43ixw49u0K+mQCyo2/Y6HURNwclvOIWt8rBoH3FmjlrnkmC8lyD8ULtvGUK236O0jP430tzdCbaQpRUPqPEH5pWK4TQUx3pcfkPQIDAQAB";

    /**
     * zhulong公钥 SH951638
     */
    public  final static String privatestrzl = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDh6i4w6tpVsoYyC4Fptuv8kxes52MB7UAgCM1ajIuQFg9pvdiYMpK4zDWNJ9816oHRFeDZqr/R0cQuO6H29q8F/Xos96sDErBZO4I5j9KU9Fw5mPOr9kARRB0T4QdFPunJChz9f255mFDPaavNNMQ+1mLj1IVQNHGqPRfRfx8xJQIDAQAB";
    /**
     *ninghui公钥 SH463395
     */
    public  final static String privatestrnh = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCLkmcL2OWhEogAL3m4gqA1NgTSngaLaFoTe9tkkOevRQvFFKzhR38rXvaW7z5Uvy3upq2bp54fL3sSp7rolY2twfzGQNn9kC3CdpeAi5xCecfNz/zIgu1bWOmoQ/1sOFXxp8fToMOrf9Tf+WRUkzVGk8ljhUzH0rcoleTCWC28zwIDAQAB";

    /**
     * xianggang公钥 SH418676
     */
    public  final static String privatestrxg = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCdsW4p3+YNiNhMC906L2SPzIXbnYc6ucPlAj43477fJZCa6yGjz8TPGK/vBcgJMp/+4vwV+arJyyqpfu0Qf2LvSFSbCUfL896xkmO+antFSNLhpjXEr3cDn7+WLNHT69RMqYyRYziUCm3RVh/bZEHa2+iYBAVH+exvfcGQmnrbDQIDAQAB";

    /**
     * xianggang公钥 SH452859
     */
    public  final static String privatestrxg2 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC9TRUCqVcUP3PSu7NRvACs86Cn/S67GmALbHaHs0VGC3Hv5iv2xGV8xbdf0pTiCxrOlK9EoIfI4+DB6Rwebu1Okvx5q+mO2X7q5EbtiSoE8ZvuZ6CwBJuWcwNqfC5tREJ0mj1Kk/eGA7yH4PUFZCIiZ4Ywa1tECIpkv2ksC7WqFQIDAQAB";



    /** 订单返回状态码 */
    public static final String VENT_CODE = "1";

    public R order(Order order) {
        logger.info("YunPay 支付通道选择 开始------------------------------------------------------");
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
                return pay(order,"wx");
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
                return pay(order,"ali");
            }

            if (OutChannel.aliwap.name().equals(order.getOutChannel())) {
                //支付宝H5支付
				return pay(order,"aliwap");
            }

            if (OutChannel.aliposs.name().equals(order.getOutChannel())) {
                //支付宝刷卡支付
//				return order_aliposs(order);
            }

            logger.error("YunPay 支付通道选择 不支持的支付渠道：{}", order.getOutChannel());
            return R.error("不支持的支付渠道");
        } finally {
            logger.info("YunPay 支付通道选择 结束------------------------------------------------------");
        }
    }


    private R pay(Order order,String paytype) {
        logger.info(" YunPay 支付数据 ：" + JSONObject.toJSON(order));
        // 跳过SSL 验证
        try {
            JSONObject jsObj = new JSONObject();
            String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            //商户号
            //zulong
            jsObj.put("merchNo", "SH951638");
            //ninghui
//            jsObj.put("merchNo", "SH463395");
            //xianggang
//            jsObj.put("merchNo","SH418676");
            //xianggang8182
//            jsObj.put("merchNo","SH452859");
            //订单号
//            String orderNo=reqTime + new Random().nextInt(100000);
            jsObj.put("orderNo", order.getOrderNo());
            System.out.println("orderNo--------"+order.getOrderNo());
            //支付渠道
            jsObj.put("outChannel", paytype);
            //用户标志
            jsObj.put("userId", order.getUserId());
            //订单标题
            jsObj.put("title", order.getTitle());
            //产品名称
            jsObj.put("product", order.getProduct());
            //支付金额 单位 元
            jsObj.put("amount", order.getAmount());
            //币种
            jsObj.put("currency", order.getCurrency());
            //前端返回地址
            jsObj.put("returnUrl", order.getReturnUrl());
            //后台通知地址nh
//            jsObj.put("notifyUrl", "http://49.4.91.134:8181/pay/notify/"+"YunPay"+"/"+order.getMerchNo()+"/"+order.getOrderNo());
            //zl
            jsObj.put("notifyUrl", "http://119.3.39.3:8181/pay/notify/"+"YunPay"+"/"+order.getMerchNo()+"/"+order.getOrderNo());
            //xg
//            jsObj.put("notifyUrl", "http://123.1.170.6:8181/pay/notify/"+"YunPay"+"/"+order.getMerchNo()+"/"+order.getOrderNo());
            //xg2
//            jsObj.put("notifyUrl", "http://123.1.170.6:8182/pay/notify/"+"YunPay"+"/"+order.getMerchNo()+"/"+order.getOrderNo());

            //请求时间
            jsObj.put("reqTime", order.getReqTime());
            //对公
            jsObj.put("acctType", 1);
            byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
            String ccc = Base64Utils.encode(context);
            String sign = Md5Util.sign(ccc,privatestrzl,"UTF-8");
            logger.info("签名结果：{}" ,sign);
            JSONObject jo = new JSONObject();
            jo.put("sign", sign);
            jo.put("context", context);
            logger.info("请求参数：{}", jo.toJSONString());

            String result = RequestUtils.doPostJson(url, jo.toJSONString());
            logger.info("请求结果！{}",result);
            logger.info("【返回信息】" + result);
            //处理返回结果
            JSONObject jsoncontent = JSON.parseObject(result);
            System.out.println("context : " + jsoncontent);
            try{

                if(StringUtils.isBlank(result)){
                    return R.error("支付返回参数为空");
                }
                int ints = payOrderDao.save(order);
                logger.info("ints : " + ints);

                return R.okData(result);

            }catch (Exception e) {
                return R.error(" YunPay 下单失败" + result);
            }

        } catch (Exception e) {
            logger.error("YunPay支付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error(" YunPay 下单失败" );
        } finally {
            logger.info("Vent 支付");
        }

    }

    /**
     * @Description 支付回调
     * @param order
     * @param request
     * @return
     */
    public R notify(Order order, HttpServletRequest request) {

        logger.info("YunPay 支付回调 开始-------------------------------------------------");
        String msg = "支付失败";
        try {
            TreeMap<String, String> params = RequestUtils.getRequestParam(request);
            logger.info("YunPay 回调 参数：" + JSON.toJSONString(params));
            String orderNo = request.getParameter("orderNo");
            String code = request.getParameter("code");
            msg = request.getParameter("msg");
            String notifyTime = request.getParameter("notifyTime");
            String amount = request.getParameter("amount");
            String sign = request.getParameter("sign");
            Merchant merchant = merchantService.get(order.getMerchNo());
            String publicKey = merchant.getPublicKey();
            logger.info("用户公钥 ：" + publicKey);

            if (!code.equals(VENT_CODE)) {
                order.setOrderState(OrderState.fail.id());
                return R.error(msg);
            }
            //验证参数
            boolean sign_verify = code.equals(VENT_CODE);
            if(sign_verify){//验签成功
                order.setOrderState(OrderState.succ.id());
                order.setNoticeState(1);
                order.setBusinessNo(orderNo);
                payOrderDao.update(order);
                payService.orderDataMsg(order.getMerchNo(),order.getOrderNo());
                msg = "支付成功";
                LinkedHashMap jomap=new LinkedHashMap();
                jomap.put("msg",msg);
                jomap.put("amount",amount);
                jomap.put("orderNo",orderNo);
                jomap.put("code",code);
                jomap.put("notifyTime",notifyTime);
                //用户公钥做加签处理
                jomap.put("key",publicKey);

                String text = http_build_query(jomap);
                String Sign = MD5.md5(text);

                jomap.put("sign",Sign);
                //用户公钥不做参数传递
                jomap.remove("key");

                logger.info("YunPay 支付 回调 用户返回结果 jomap : " + jomap);
                String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                logger.info(" YunPay 支付 回调 用户返回结果 ： " +result);
                return R.ok(msg);
            }else{
                logger.info(" YunPay 支付 回调 验证签名不通过");
                return R.error("验签失败！");
            }

        } catch (Exception e) {
            logger.info("YunPay 支付回调 异常："+e.getMessage());
            e.printStackTrace();
            order.setOrderState(OrderState.fail.id());
            payOrderDao.update(order);
            return R.error(" YunPay 支付回调 异常：" + e.getMessage());
        } finally{
            logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
            logger.info("YunPay 支付回调 结束-------------------------------------------------");
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
        logger.info("reString : " + reString);
        reString = reString.replace("%3D", "=").replace("%26", "&").replace("%3A",":").replace("%2F","/");
        return reString;
    }
}
