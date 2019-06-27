package com.qh.paythird.taiShan;

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
import com.qh.paythird.taiShan.utils.MD5;
import com.qh.paythird.taiShan.utils.RQPayUtils;
import com.qh.paythird.taiShan.utils.SSLView;
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
public class TaiShanService {

        private  static final Logger logger = LoggerFactory.getLogger(TaiShanService.class);

        @Autowired
        PayOrderDao payOrderDao;

        @Autowired
        PayService payService;
        @Autowired
        private MerchantService merchantService;

        /** 商户号 */
        public static final String TEST_MER = "11223";

        /** 支付请求地址 */
        public static final String PAY_URL = "https://api.tai3pay.com/gateway/index/gopay.do";


        public R order(Order order) {
            logger.info("泰山 支付通道选择 开始------------------------------------------------------");
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
                    return pay(order,"alipay_auto");
                }

                if (OutChannel.aliwap.name().equals(order.getOutChannel())) {
                    //支付宝H5支付
//                    return pay(order,"alipay_auto");
                }

                if (OutChannel.aliposs.name().equals(order.getOutChannel())) {
                    //支付宝刷卡支付
//				return order_aliposs(order);
                }
                if (OutChannel.ysf.name().equals(order.getOutChannel())) {
                    //云闪付
                    return pay(order,"unionpay_auto");
                }


                logger.error("泰山 支付通道选择 不支持的支付渠道：{}", order.getOutChannel());
                return R.error("不支持的支付渠道");
            } finally {
                logger.info("泰山 支付通道选择 结束------------------------------------------------------");
            }
        }



        /**
         * 支付数据
         */

        private R pay(Order order,String paytype) {
            logger.info("泰山 支付数据 ：" +order.toString());
            try {
                SSLView.trustAllHttpsCertificates();
                HttpsURLConnection.setDefaultHostnameVerifier(new SSLView().hv);
                TreeMap<String, String> map = new TreeMap<>() ;
                String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                //
                map.put("account_id", TEST_MER);

                //交易金额
                map.put("amount",order.getAmount().setScale(2, BigDecimal.ROUND_UNNECESSARY ).toString());
                //异步回调地址
                //map.put("callback_url","http://49.4.91.134:8182/pay/notify/TaiShan/"+order.getMerchNo()+"/"+order.getOrderNo());
                map.put("callback_url","http://119.3.39.3:8182/pay/notify/TaiShan/"+order.getMerchNo()+"/"+order.getOrderNo());

                map.put("content_type","json");

                map.put("error_url","www.baidu.com");

                map.put("out_trade_no",order.getOrderNo());

                map.put("robin","2");

                map.put("success_url","www.baidu.com");

                map.put("thoroughfare",paytype);

                if(paytype.equals("alipay_auto")){
                    map.put("type","2");
                }else {
                    map.put("type","3");
                }
                String con=http_build_query(map);
                con+="&key=A1051A286F0DFA";
                String sign = MD5.md5(con);
                map.put("sign", sign);
                logger.info("请求参数：{}", map);

                String result = RQPayUtils.sendPost(PAY_URL, map);
                logger.info("返回参数 ： " + result);
                if(result==null){
                    return R.error("支付返回参数为空");
                }
                JSONObject jsoncontent = JSON.parseObject(result.toString());
                System.out.println("content : " + jsoncontent);
                String code=jsoncontent.get("code").toString();
                String msg=jsoncontent.get("msg").toString();
                JSONObject data=JSON.parseObject(jsoncontent.getString("data"));
                String order_id=data.getString("order_id");
                String qrcode=data.getString("qrcode");
                String amount=data.getString("amount");

                if(code.equals("200")){
                    int ints = payOrderDao.save(order);
                    logger.info("ints : " + ints);
                    JSONObject rdata = new JSONObject();
                    rdata.put("code","1");
                    rdata.put("msg","success");
                    rdata.put("order_id",order_id);
                    rdata.put("qrcode",qrcode);
                    rdata.put("amount",amount);
                    return R.okData(rdata);
                }else if(code.equals("-1")){
                    return  R.error("支付金额不正确");
                }else if(code.equals("-2")){
                    return  R.error("订单过期");
                }else if(code.equals("-3")){
                    return  R.error("签名失败");
                }
                return  R.ok();
            } catch (Exception e){
                logger.error("泰山 测试支付 异常：" + e.getMessage());
                e.printStackTrace();
                return R.error("泰山 下单失败");
            } finally {
                logger.info("泰山  测试支付");
            }
        }


        /**
         * @Description 支付回调
         * @param order
         * @param request
         * @return
         */
        public R notify(Order order, HttpServletRequest request) {
            logger.info("泰山 支付回调 开始-------------------------------------------------");
            String msg = "支付成功";
            try {
                TreeMap<String, String> params = RequestUtils.getRequestParam(request);
                logger.info("泰山 回调 参数："+ JSON.toJSONString(params));
                String account_id = request.getParameter("account_id");
                String account_name = request.getParameter("account_name");
                String pay_time = request.getParameter("pay_time");
                String status = request.getParameter("status");
                //提交金额
                String tj_amount = request.getParameter("tj_amount");
                //实际金额(风控)
                String amount = request.getParameter("amount");
                //平台订单
                String out_trade_no = request.getParameter("out_trade_no");
                //第三方订单
                String trade_no = request.getParameter("trade_no");
                String fees = request.getParameter("fees");
                String callback_time = request.getParameter("callback_time");
                String type = request.getParameter("type");
                String sign = request.getParameter("sign");
                System.out.println("sign---------------->"+sign);

                TreeMap<String, String> map = new TreeMap<>() ;
                map.put("account_id",account_id);
                map.put("account_name",account_name);
                map.put("amount",amount);
                //map.put("callback_content",callback_content);
                map.put("callback_time",callback_time);
                map.put("fees",fees);
                map.put("out_trade_no",out_trade_no);
                map.put("pay_time",pay_time);
                map.put("status",status);
                map.put("tj_amount",tj_amount);
                map.put("trade_no",trade_no);
                map.put("type",type);
                StringBuffer SB=new StringBuffer();
                SB.append(http_build_query(map));
                SB.append("&key=A1051A286F0DFA");

                if (!"success" .equals(status) ){
                    order.setOrderState(OrderState.fail.id());
                    msg = "支付失败";
                    return R.error(msg);
                }
                if(MD5.md5(SB.toString()).equals(sign)) {
                    msg = "支付成功";
                    order.setOrderState(OrderState.succ.id());
                    order.setNoticeState(1);
                    order.setBusinessNo(trade_no);
                    order.setMemo(amount);
                    payOrderDao.update(order);

                    BigDecimal orderamount = order.getAmount();
                    BigDecimal sjamount = new BigDecimal(amount);
//                    Integer  subt =Integer.parseInt(orderamount.subtract(sjamount).multiply(new BigDecimal("100")).toString());
//
//                    if(subt>=500){
//                        return R.error("实际支付金额与订单金额不相符！");
//                    }
                    payService.orderDataMsg(order.getMerchNo(), order.getOrderNo());
                    Merchant merchant = merchantService.get(order.getMerchNo());
                    String publicKey = merchant.getPublicKey();
                    logger.info("用户加密key ： " + publicKey);
                    LinkedHashMap jomap = new LinkedHashMap();
                    jomap.put("msg", msg);
//                     jomap.put("amount",amount.divide(new BigDecimal("100")).toString());
                    jomap.put("amount", amount);
                    jomap.put("orderNo", order.getOrderNo());
                    jomap.put("code", "1");
                    jomap.put("notifyTime", callback_time);
                    //用户公钥做加签处理
                    jomap.put("key", publicKey);
                    String text = http_build_query(jomap);
                    String Sign = MD5.md5(text);
                    jomap.put("sign", Sign);
                    //用户公钥不做参数传递
                    jomap.remove("key");
                    logger.info("泰山 支付 回调 用户返回结果 jomap : " + jomap);
                    String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                    logger.info(" 泰山 支付 回调 用户返回结果 ： " + result);
                    return R.ok(msg);
                }else{
                    return  R.error("验签失败");
                }
            } catch (Exception e) {
                logger.info("泰山 支付回调 异常："+e.getMessage());
                e.printStackTrace();
                order.setOrderState(OrderState.fail.id());
                payOrderDao.update(order);
                return R.error("泰山 支付回调 异常");
            } finally{
                logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
                logger.info("泰山 支付回调 结束-------------------------------------------------");
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
                reString += key+"="+value+"&";
            }
            reString = reString.substring(0, reString.length()-1);
            System.out.println("reString : " + reString);
            //将得到的字符串进行处理得到目标格式的字符串
            reString = java.net.URLEncoder.encode(reString);
            reString = reString.replace("%3D", "=").replace("%26", "&");
            return reString;
        }
    }


