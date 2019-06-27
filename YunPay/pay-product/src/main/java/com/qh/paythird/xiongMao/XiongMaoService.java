package com.qh.paythird.xiongMao;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.dao.PayOrderDao;
import com.qh.pay.domain.Merchant;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayService;
import com.qh.paythird.VNET.utils.MD5;
import com.qh.paythird.VNET.utils.RQPayUtils;
import com.qh.paythird.xiongMao.utils.HttpClient4;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class XiongMaoService {


    private  static final Logger logger = LoggerFactory.getLogger(XiongMaoService.class);

    @Autowired
    PayOrderDao payOrderDao;
    @Autowired
    private PayService payService;
    @Autowired
    private MerchantService merchantService;

    /**
     * 下单路径
     */
    public final static String urlup = "http://pay.panda-kpay.com/pay/unifiedorder";

    /**
     * 熊猫公钥
     */
    public final static String PublicKeyXiongmao = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCQ6Apl/5Y9p/BvwRNXLLX+pCCCKKjyP4/7grgD+xAzS93u1CwDvYHoiyo3vsQhmmHX9Mnhci5khfIZffDipkMdKjaEvOutCGFwIHzHC08d7x/hL5XdJ/rFJuSwScEK7xmcJpfmNKHvpMBUdh48Hxifr9B9GuEUXbopZWZxfzJCzQIDAQAB";
    /**
     * 熊猫公私钥
     */
    public final static String privateKeyXiongmao = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAJDoCmX/lj2n8G/BE1cstf6kIIIoqPI/j/uCuAP7EDNL3e7ULAO9geiLKje+xCGaYdf0yeFyLmSF8hl98OKmQx0qNoS8660IYXAgfMcLTx3vH+Evld0n+sUm5LBJwQrvGZwml+Y0oe+kwFR2HjwfGJ+v0H0a4RRduillZnF/MkLNAgMBAAECgYB87WARobEwhLnYTxfSfzyERZG1RUKqXzxNtNvaqrfD0bOLdKZhrx7xkhEasD+9TLDwEx19XQg8J/KaIabscDTI/jf7hvuyeL0mERFM3NXuLo1l1R56n8h97FmgTBi8d9Ql7ndBAwwzTK8cfKYYpzLG6+SrNsWZtJ+pvWkCQtkjgQJBAOQ4zMNLSCLueOcm+Iw5yTt//6QYdptOrLzPZbP1WoQQ/FvcblsmcRQb1P65TxQ4vSXh3/EAwDOgFnKN+zTCBS0CQQCiizOT+ZUrxTBP7wFo0ceaWTz2Xh//biCvGYr9DyEA8ve041VU8sJwNWUlw1W1lUPdJyG9c+30nz6QCqAFXPghAkEA4mWqU036CJUTIROa2th0VO8cNbgC6Px6BW+kj4okugBzp9kbLJcM9ArMF8jStte2Y78XvWemQ1BbFFbeza5vHQJAfAlTu7j6n2Mjgev2HGHxOpSck7iyHD6SzGvmh0PzQIEoi63rIR77R5tHa3DLR/z2w52n/qWn0UNv/4VMJauTYQJAIBLfNg714fYrNCgEHkVKym91/2bFj5py9XKGhkf1LxoBStiAiy6EzCHkWHethT9yg3blwOnQYz1f+nvBISdenw==";
    /**
     * 熊猫用户 key
     */
    public final static String userkey = "283fea92c8b23ac634324aa6989c01d8";

    /**
     * @Description 支付发起
     * @param order
     * @return
     */
    public R order(Order order) {
        logger.info("测试钱包支付 开始------------------------------------------------------");
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
//                return order_wx(order);
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
//				return order_ali(order);
            }

            if (OutChannel.aliwap.name().equals(order.getOutChannel())) {
                //支付宝H5支付
				return order(order,"ali_h5");
            }

            if (OutChannel.aliposs.name().equals(order.getOutChannel())) {
                //支付宝刷卡支付
//				return order_aliposs(order);
            }

            logger.error("测试钱包支付 不支持的支付渠道：{}", order.getOutChannel());
            return R.error("不支持的支付渠道");
        } finally {
            logger.info("测试钱包支付 结束------------------------------------------------------");
        }
    }


    /**
     *
     * 支付宝H5支付
     */
    private R order (Order order,String channel){
        logger.info("支付数据 ：" +JSONObject.toJSON(order));
        String msg = "支付失败";
        try {
//            JSONObject map = new JSONObject() ;
            LinkedHashMap<String, Object> map = new LinkedHashMap() ;

            String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            //商户订单号
            map.put("out_trade_no", order.getOrderNo());
//            map.put("out_trade_no", "201905140953025772");
//            map.put("remark", reqTime + new Random().nextInt(10000));
            //商品标识
            map.put("subject",order.getTitle());
            //商品详情
            map.put("body",order.getProduct());

            //交易金额
            map.put("amount",order.getAmount().toString());
            //交易币种、
            map.put("currency","CNY");
            //支付渠道（支付方式）
            map.put("channel", channel);
            map.put("extparam",order.getMemo());
            //用户id
            map.put("mchid", "101086");
            //前端返回地址
            map.put("return_url", "http://47.102.149.20:8181/pay/return");
            //后台通知地址
            //map.put("notify_url", "http://47.102.149.20:8181/pay/notify/"+"xioangmao"+"/"+order.getUserId()+"/"+order.getOrderNo());
            //map.put("notify_url", "http://49.4.91.134:8182/pay/notify/"+"xioangmao"+"/"+order.getUserId()+"/"+order.getOrderNo());
//            map.put("notify_url", "http://119.3.39.3:8182/pay/notify/"+"xioangmao"+"/"+order.getUserId()+"/"+order.getOrderNo());
            map.put("notify_url", "http://119.3.39.3:8181/pay/notify/"+"xioangmao"+"/"+order.getUserId()+"/"+order.getOrderNo());
            //IP地址
            map.put("client_ip","101.81.138.224");
            //加密类型1:采用RAS算法; 2:采用MD5算法
            map.put("sign_type","2");

            TreeMap  signtest = new TreeMap();
            signtest.put("out_trade_no",map.get("out_trade_no"));
            signtest.put("amount",map.get("amount"));
            signtest.put("currency",map.get("currency"));
            signtest.put("channel",map.get("channel"));
            signtest.put("mchid",map.get("mchid"));
            signtest.put("return_url",map.get("return_url"));
            signtest.put("notify_url",map.get("notify_url"));

            String mapstr = http_build_query(signtest);
            logger.info("mapstr :" +mapstr+"&key="+userkey+privateKeyXiongmao);
            String sign = Md5Util.MD5(mapstr+"&key="+userkey+privateKeyXiongmao);
            //signMD5加密
            map.put("sign",sign.toLowerCase());

            HashMap<String,Object> jo = new HashMap();
            jo.put("data",map.toString());
                logger.info("请求参数map ： " + map);
            String result = HttpClient4.doPost(urlup,map);
            logger.info("返回的参数 ：" + result);

            if(StringUtils.isBlank(result)){
                return R.error("支付返回参数为空");
            }

            JSONObject contentjson = JSON.parseObject(result);
            System.out.println("content : " + contentjson);
            try {
                String codejson = contentjson.get("result_code").toString();
                String msgjson = contentjson.get("result_msg").toString();
                if(codejson.equals("OK")){
                    int ints = payOrderDao.save(order);
                    logger.info("ints : " + ints);
                    JSONObject content = new JSONObject();
                    JSONObject data = new JSONObject();
                    JSONObject jsondata = JSON.parseObject(contentjson.get("charge").toString());
                    String payurl = jsondata.get("credential").toString();
                    String amount = jsondata.get("amount").toString();
                    //拿出跳转地址返回
                    content.put("msg",msgjson);
                    content.put("code","1");
                    content.put("amount",amount);
                    content.put("payurl",payurl);
                    return R.okData(content);
                }
            } catch (Exception e) {
                return R.okData(contentjson);
            }
            return  R.ok();
        } catch (Exception e){
            logger.error("测试支付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("支付异常");
        } finally {
            logger.info("测试支付");
        }
    }




    static String http_build_query(TreeMap<String, Object> array){
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
     *
     * 订单回调
     */
    public R notify(Order order, HttpServletRequest request, String requestBody) {
        logger.info("熊猫 支付回调 开始-------------------------------------------------");
        String msg = "支付失败";
        JSONObject jsonObject = JSONObject.parseObject(requestBody);
        logger.info("熊猫 回调 参数：" + JSON.toJSONString(jsonObject));

        String codejson = request.getParameter("result_code");
        msg = request.getParameter("result_msg");

        JSONObject charge = jsonObject.getJSONObject("charge");
        String orderNojson = charge.get("out_trade_no").toString();
        String amount = charge.get("amount").toString();
        String sign = charge.get("sign").toString();

        TreeMap<String, Object> mapverif = new TreeMap<>() ;
        mapverif.put("out_trade_no",charge.get("out_trade_no").toString());
        mapverif.put("amount",charge.get("amount").toString());
        mapverif.put("currency",charge.get("currency").toString());
        mapverif.put("channel",charge.get("channel").toString());
        mapverif.put("mchid",charge.get("mchid").toString());
        mapverif.put("noncestr",charge.get("noncestr").toString());
        mapverif.put("trade_no",charge.get("trade_no").toString());

        String mapstr = http_build_query(mapverif);
        logger.info("mapstr :" +mapstr+"&key="+userkey+privateKeyXiongmao);
        String signverif = Md5Util.MD5(mapstr+"&key="+userkey+privateKeyXiongmao);

        if(!"OK".equals(codejson)){
            order.setOrderState(OrderState.fail.id());
            return R.error(msg);
        }

        try {
            if(sign.equals(signverif)){//验签成功
                order.setOrderState(OrderState.succ.id());
                order.setNoticeState(1);
                order.setBusinessNo(orderNojson);
                payOrderDao.update(order);
                payService.orderDataMsg(order.getMerchNo(),order.getOrderNo());
                Merchant merchant = merchantService.get(order.getMerchNo());
                String publicKey = merchant.getPublicKey();
                String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                LinkedHashMap jomap=new LinkedHashMap();
                jomap.put("msg",msg);
                jomap.put("amount",amount);
                jomap.put("orderNo",orderNojson);
                jomap.put("code",codejson.equals("OK")?"1":"0");
                jomap.put("notifyTime",reqTime);
                //用户公钥做加签处理
                jomap.put("key",publicKey);

                String text = http_build_query_yun(jomap);
                String Sign = MD5.md5(text);

                jomap.put("sign",Sign);
                //用户公钥不做参数传递
                jomap.remove("key");

                logger.info("熊猫 支付 回调 用户返回结果 jomap : " + jomap);
                String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                logger.info(" 熊猫 支付 回调 用户返回结果 ： " +result);
                return R.ok(msg);
            }else{
                logger.info(" 熊猫 支付 回调 验证签名不通过");
                return R.error("验签失败！");
            }
        } catch (Exception e) {
            return R.error("验签失败！");
        } finally {
            logger.info("熊猫 支付回调 结束-------------------------------------------------");
        }



    }

    static String http_build_query_yun(LinkedHashMap<String, Object> array){
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

}
