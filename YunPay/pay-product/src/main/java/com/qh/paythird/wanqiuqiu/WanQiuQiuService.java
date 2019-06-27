package com.qh.paythird.wanqiuqiu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.dao.UserBankDao;
import com.qh.common.domain.UserBankDO;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.ClearState;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.dao.PayOrderAcpDao;
import com.qh.pay.dao.PayOrderDao;
import com.qh.pay.dao.RecordMerchAvailBalDao;
import com.qh.pay.dao.RecordMerchBalDao;
import com.qh.pay.domain.Merchant;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayHandlerService;
import com.qh.pay.service.PayService;
import com.qh.paythird.wanqiuqiu.utils.HttpUtil;
import com.qh.paythird.wanqiuqiu.utils.MD5;
import com.qh.paythird.wanqiuqiu.utils.RQPayUtils;
import com.qh.paythird.wanqiuqiu.utils.SSLView;
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
public class WanQiuQiuService {

        private  static final Logger logger = LoggerFactory.getLogger(WanQiuQiuService.class);

        @Autowired
        PayOrderDao payOrderDao;

        @Autowired
        PayService payService;

        @Autowired
        PayOrderAcpDao payOrderAcpDao;
        @Autowired
        private MerchantService merchantService;

        @Autowired
        private PayHandlerService payHandlerService;

        @Autowired
        private RecordMerchAvailBalDao rdMerchAvailBalDao;

        @Autowired
        private RecordMerchBalDao rdMerchBalDao;

        @Autowired
        UserBankDao userBankDao;

        /** 商户号 */
        public static final String TEST_MER = "10000005";

        /** 支付请求地址 */
        public static final String PAY_URL = "http://119.3.63.9:3020/api/pay/create_order";

        public static final String ACP_URL = "http://119.3.63.9:3020/api/settle/create_order";

        public static final String KEY = "4a7c5fbd74e82134e152f76de023347f";

        public R order(Order order) {
            logger.info("玩球球 支付通道选择 开始------------------------------------------------------");
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
//                 return order_wy(order);
                   return pay(order,"1209");
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

                if(OutChannel.acp.name().equals(order.getOutChannel())){
                    return acp(order,"acp");
                }

                logger.error("玩球球 支付通道选择 不支持的支付渠道：{}", order.getOutChannel());
                return R.error("不支持的支付渠道");
            } finally {
                logger.info("玩球球 支付通道选择 结束------------------------------------------------------");
            }
        }



        /**
         * 支付数据
         */

        private R pay(Order order,String paytype) {
            logger.info("玩球球 支付数据 ：" +order.toString());
            try {
                SSLView.trustAllHttpsCertificates();
                HttpsURLConnection.setDefaultHostnameVerifier(new SSLView().hv);
                TreeMap<String, String> map = new TreeMap<>() ;
                String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                //
                map.put("mchId", TEST_MER);
                map.put("mchOrderNo", order.getOrderNo());
                map.put("channelId", "1208");
                map.put("amount", order.getAmount().multiply(new BigDecimal("100")).toString());
                //异步回调地址
                map.put("notifyUrl","http://119.3.39.3:8182/pay/notify/WQQ/"+order.getMerchNo()+"/"+order.getOrderNo());
                map.put("redirectUrl","www.baidu.com");
                map.put("subject","ceshi");
                map.put("body","ceshi");
                map.put("clientIp","119.3.39.3");
                map.put("uid","201");
                //map.put("bankCode",order.getBankCode());
                map.put("cardId",order.getCertNo());
                //map.put("bankCardNo",order.getBankNo());
                map.put("phone",order.getMobile());
                map.put("bankCardName",order.getAcctName());
                String con=http_build_query(map);
                con +="&key="+KEY;
                System.out.println("con==="+con);
                String sign = MD5.md5(con);
                map.put("sign", sign);
                JSONObject jsonObject =(JSONObject) JSONObject.toJSON(map);
                logger.info("请求参数：{}", jsonObject);
                String content=HttpUtil.post(jsonObject,PAY_URL);
                JSONObject result=JSONObject.parseObject(content);
                logger.info("返回参数 ： " + result);
                if(result==null){
                    return R.error("支付返回参数为空");
                }
                System.out.println("content : " + result);
                String status=result.get("status").toString();
                String msg=result.get("msg").toString();
                if(status.equals("200")){
                    String payUrl=result.get("payUrl").toString();
                    int ints = payOrderDao.save(order);
                    logger.info("ints : " + ints);
                    JSONObject rdata = new JSONObject();
                    rdata.put("code","1");
                    rdata.put("msg","success");
                    rdata.put("order_id",order.getOrderNo());
                    rdata.put("payUrl",payUrl);
                    rdata.put("amount",order.getAmount());
                    return R.okData(rdata);
                }
                return  R.ok();
            } catch (Exception e){
                logger.error("玩球球 测试支付 异常：" + e.getMessage());
                e.printStackTrace();
                return R.error("玩球球 下单失败");
            } finally {
                logger.info("玩球球  测试支付");
            }
        }


        /**
         * @Description 支付回调
         * @param order
         * @param request
         * @return
         */
        public R notify(Order order, HttpServletRequest request) {
            logger.info("玩球球 支付回调 开始-------------------------------------------------");
            String msg = "支付成功";
            try {
                TreeMap<String, String> params = RequestUtils.getRequestParam(request);
                logger.info("玩球球 回调 参数："+ JSON.toJSONString(params));
                String payOrderId = request.getParameter("payOrderId");
                String amount = request.getParameter("amount");
                String mchOrderNo = request.getParameter("mchOrderNo");
                String status = request.getParameter("status");
                String sign = request.getParameter("sign");
                System.out.println("sign---------------->"+sign);
                TreeMap<String, String> map = new TreeMap<>() ;
                map.put("payOrderId",payOrderId);

                map.put("amount",amount);
                map.put("mchOrderNo",mchOrderNo);
                map.put("status",status);
                StringBuffer SB=new StringBuffer();
                SB.append(http_build_query(map));
                SB.append("&key=4a7c5fbd74e82134e152f76de023347f");

                if (! "2" .equals(status) ){
                    order.setOrderState(OrderState.fail.id());
                    msg = "支付失败";
                    return R.error(msg);
                }
                if(MD5.md5(SB.toString()).equals(sign)) {
                    BigDecimal money=new BigDecimal(amount).divide(new BigDecimal("100")).setScale(0);
                    msg = "支付成功";
                    order.setOrderState(OrderState.succ.id());
                    order.setNoticeState(1);
                    order.setBusinessNo(payOrderId);
                    order.setMemo(money.toString());
                    payOrderDao.update(order);
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
                    //jomap.put("notifyTime", callback_time);
                    //用户公钥做加签处理
                    jomap.put("key", publicKey);
                    String text = http_build_query(jomap);
                    String Sign = MD5.md5(text);
                    jomap.put("sign", Sign);
                    //用户公钥不做参数传递
                    jomap.remove("key");
                    logger.info("玩球球 支付 回调 用户返回结果 jomap : " + jomap);
                    String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                    logger.info(" 玩球球 支付 回调 用户返回结果 ： " + result);
                    return R.ok(msg);
                }else{
                    return  R.error("验签失败");
                }
            } catch (Exception e) {
                logger.info("玩球球 支付回调 异常："+e.getMessage());
                e.printStackTrace();
                order.setOrderState(OrderState.fail.id());
                payOrderDao.update(order);
                return R.error("玩球球 支付回调 异常");
            } finally{
                logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
                logger.info("玩球球 支付回调 结束-------------------------------------------------");
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
            return reString;
        }

        public R acp(Order order,String paytype){
            logger.info("玩球球  代付数据 ：" +order.toString());
            order=payOrderAcpDao.get(order.getOrderNo(),order.getMerchNo());
            UserBankDO bankDO=userBankDao.get(order.getMerchNo(),order.getBankNo());
            System.out.println("order.getBankProvince()"+order.getBankProvince());
            try {
                TreeMap<String, String> map = new TreeMap<>() ;
                String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                //
                map.put("mchId", TEST_MER);
                map.put("channelType", "1208");
                map.put("mchSettleOrderNo", order.getOrderNo());
                map.put("amount", order.getAmount().multiply(new BigDecimal("100")).setScale(0).toString());
                map.put("cardId", bankDO.getCertNo());
                map.put("bankName",order.getBankName());
                map.put("bankCardNo", bankDO.getBankNo());
                map.put("bankCardName",bankDO.getAcctName());
                map.put("bankBranch", bankDO.getBankBranch());
                map.put("bankBranchAddress",bankDO.getBankProvince()+bankDO.getBankCity());
                String bankCardType="";
                if(order.getAcctType()==0){
                    bankCardType="1";
                }else {
                    bankCardType="2";
                    map.put("bankNumber",order.getUnionpayNo());
                }
                map.put("bankCardType",bankCardType);
                //异步回调地址
                map.put("notifyUrl","http://119.3.39.3:8182/pay/notify/acp/WQQ/"+order.getMerchNo()+"/"+order.getOrderNo());
                String con=http_build_query(map);
                con +="&key="+KEY;
                System.out.println("con==="+con);
                String sign = MD5.md5(con);
                map.put("sign", sign);
                JSONObject jsonObject =(JSONObject) JSONObject.toJSON(map);
                logger.info("请求参数：{}", jsonObject);
                String content=HttpUtil.post(jsonObject,ACP_URL);
                JSONObject result=JSONObject.parseObject(content);
                logger.info("返回参数 ： " + result);
                if(result==null){
                    return R.error("支付返回参数为空");
                }
                System.out.println("content : " + result);
                String status=result.get("status").toString();
                //String msg=result.get("msg").toString();
                if(status.equals("200")){
                    System.out.println("200");
                    JSONObject rdata = new JSONObject();
                    rdata.put("code","1");
                    rdata.put("msg","success");
                    rdata.put("order_id",order.getOrderNo());
                    rdata.put("amount",order.getAmount());
                    order.setOrderState(OrderState.succ.id());
                    order.setClearState(ClearState.succ.id());
                    payOrderAcpDao.update(order);
                    return R.okData(rdata);
                }else{
                    payService.orderAcpNopassDataMsg(order.getMerchNo(),order.getOrderNo());
                    JSONObject rdata = new JSONObject();
                    rdata.put("code","0");
                    rdata.put("msg","erro");
                    rdata.put("order_id",order.getOrderNo());
                    rdata.put("amount",order.getAmount());
                    order.setOrderState(OrderState.fail.id());
                    order.setClearState(ClearState.fail.id());
                    payOrderAcpDao.update(order);
                    return R.okData(rdata);
                }
            } catch (Exception e){
                logger.error("玩球球 代付测试 异常：" + e.getMessage());
                e.printStackTrace();
                return R.error("玩球球代付 下单失败");
            } finally {
                logger.info("玩球球代付测试");
            }
        }

    public R acp_notify(Order order, HttpServletRequest request) {
        logger.info("玩球球 结算回调 开始-------------------------------------------------");
        String msg = "下发成功";
        try {
            TreeMap<String, String> params = RequestUtils.getRequestParam(request);
            logger.info("玩球球 回调 参数："+ JSON.toJSONString(params));
            String settleOrderId = request.getParameter("settleOrderId");
            String mchId = request.getParameter("mchId");
            String amount = request.getParameter("amount");
            String mchSettleOrderNo = request.getParameter("mchSettleOrderNo");
            String status = request.getParameter("status");
            String sign = request.getParameter("sign");
            System.out.println("sign---------------->"+sign);
            TreeMap<String, String> map = new TreeMap<>() ;
            map.put("settleOrderId",settleOrderId);
            map.put("amount",amount);
            map.put("mchId",mchId);
            map.put("mchSettleOrderNo",mchSettleOrderNo);
            map.put("status",status);
            StringBuffer SB=new StringBuffer();
            SB.append(http_build_query(map));
            SB.append("&key=4a7c5fbd74e82134e152f76de023347f");
            if (! "2" .equals(status) ){
                order.setOrderState(OrderState.fail.id());
                msg = "支付失败";
                return R.error(msg);
            }
            if(MD5.md5(SB.toString()).equals(sign)) {
                msg = "支付成功";
                BigDecimal money=new BigDecimal(amount).divide(new BigDecimal("100")).setScale(2);
                order.setOrderState(OrderState.succ.id());
                order.setNoticeState(1);
                order.setBusinessNo(settleOrderId);
                order.setMemo(money.toString());
                payOrderDao.update(order);
                payService.orderDataMsg(order.getMerchNo(), order.getOrderNo());
                Merchant merchant = merchantService.get(order.getMerchNo());
                String publicKey = merchant.getPublicKey();
                logger.info("用户加密key ： " + publicKey);
                LinkedHashMap jomap = new LinkedHashMap();
                jomap.put("msg", msg);
//                 jomap.put("amount",amount.divide(new BigDecimal("100")).toString());
                jomap.put("amount", money.toString());
                jomap.put("orderNo", order.getOrderNo());
                jomap.put("code", "1");
                //jomap.put("notifyTime", callback_time);
                //用户公钥做加签处理
                jomap.put("key", publicKey);
                String text = http_build_query(jomap);
                String Sign = MD5.md5(text);
                jomap.put("sign", Sign);
                //用户公钥不做参数传递
                jomap.remove("key");
                logger.info("玩球球 支付 回调 用户返回结果 jomap : " + jomap);
                String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                logger.info(" 玩球球 支付 回调 用户返回结果 ： " + result);
                return R.ok(msg);
            }else{
                return  R.error("验签失败");
            }
        } catch (Exception e) {
            logger.info("玩球球 支付回调 异常："+e.getMessage());
            e.printStackTrace();
            order.setOrderState(OrderState.fail.id());
            payOrderDao.update(order);
            return R.error("玩球球 支付回调 异常");
        } finally{
            logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
            logger.info("玩球球 支付回调 结束-------------------------------------------------");
        }
    }

    public R query(Order order){
        String query_url="http://119.3.63.9:3020/api/settle/order/query";
        TreeMap<String, String> map = new TreeMap<>() ;
        String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        //
        map.put("mchId", TEST_MER);
        map.put("mchSettleOrderNo", order.getOrderNo());
        return null;
    }
}


