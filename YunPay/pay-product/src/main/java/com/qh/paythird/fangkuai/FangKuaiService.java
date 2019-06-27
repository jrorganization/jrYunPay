package com.qh.paythird.fangkuai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.service.HtmlService;
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
import com.qh.paythird.fangkuai.utils.MD5;
import com.qh.paythird.fangkuai.utils.RQPayUtils;
import com.qh.paythird.fangkuai.utils.SSLView;
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
public class FangKuaiService {

        private  static final Logger logger = LoggerFactory.getLogger(FangKuaiService.class);

        @Autowired
        PayOrderDao payOrderDao;

        @Autowired
        PayService payService;
        @Autowired
        private MerchantService merchantService;
        @Autowired
        private HtmlService htmlService;
        /** 商户号 */
        public static final String TEST_MER = "793503617";

        /** 支付请求地址 */
        public static final String PAY_URL = "http://uee.ooo/submit.php";

        public static final String KEY = "abc123456";


        public R order(Order order) {
            logger.info("方块 支付通道选择 开始------------------------------------------------------");
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

                logger.error("方块 支付通道选择 不支持的支付渠道：{}", order.getOutChannel());
                return R.error("不支持的支付渠道");
            } finally {
                logger.info("方块 支付通道选择 结束------------------------------------------------------");
            }
        }


        /**
         * 支付数据
         */
        private R pay(Order order,String paytype) {
            logger.info("方块 支付数据 ：" +order.toString());
            try {
                SSLView.trustAllHttpsCertificates();
                HttpsURLConnection.setDefaultHostnameVerifier(new SSLView().hv);
                TreeMap<String,String> paramMap=new TreeMap<String,String>();
                paramMap.put("pid",TEST_MER);
                paramMap.put("type","alipay");
                paramMap.put("out_trade_no",order.getOrderNo());
                //paramMap.put("notify_url","http://49.4.91.134:8182/pay/notify/FangKuai/"+order.getMerchNo()+"/"+order.getOrderNo());
                paramMap.put("notify_url","http://119.3.39.3:8182/pay/notify/FangKuai/"+order.getMerchNo()+"/"+order.getOrderNo());
                paramMap.put("return_url","www.baidu.com");
                paramMap.put("name",order.getTitle());
                paramMap.put("money",order.getAmount().setScale(2).toString());
                paramMap.put("sitename","lol");
                String con=http_build_query(paramMap);
                System.out.println("con+KEY:"+con+KEY);
                String sign= MD5.md5(con+KEY);
                System.out.println("sign=====>"+sign);
                paramMap.put("sign", sign);
                paramMap.put("sign_type","md5");
                String request = http_build_query(paramMap);
                logger.info("请求参数：{}", paramMap);
                String result = HttpRequestUtil.sendPost(PAY_URL, request);
                logger.info("返回参数 ： " + result);
                if(result==null){
                    return R.error("支付返回参数为空");
                }
                String path=htmlService.make(order.getOrderNo(),result);
                System.out.println(path);
                int ints = payOrderDao.save(order);
                logger.info("ints : " + ints);
                JSONObject rdata = new JSONObject();
                rdata.put("code","1");
                rdata.put("msg","success");
                rdata.put("payurl",path);
                return R.okData(rdata);
            } catch (Exception e){
                logger.error("方块 测试支付 异常：" + e.getMessage());
                e.printStackTrace();
                return R.error("方块 下单失败");
            } finally {
                logger.info("方块  测试支付");
            }
        }


        /**
         * @Description 支付回调
         * @param order
         * @param request
         * @return
         */
        public R notify(Order order, HttpServletRequest request) {
            logger.info("方块 支付回调 开始-------------------------------------------------");
            String msg = "支付成功";
            try {
                TreeMap<String, String> params = RequestUtils.getRequestParam(request);
                logger.info("方块 回调 参数："+ JSON.toJSONString(params));
                String pid=request.getParameter("pid");
                String trade_no=request.getParameter("trade_no");
                String out_trade_no=request.getParameter("out_trade_no");
                String type=request.getParameter("type");
                String name=request.getParameter("name");
                String money=request.getParameter("money");
                String trade_status=request.getParameter("trade_status");
                String sign=request.getParameter("sign");
                String sign_type=request.getParameter("sign_type");
                System.out.println("sign---------------->"+sign);
                TreeMap<String, String> map = new TreeMap<>() ;
                map.put("pid",pid);
                map.put("trade_no",trade_no);
                map.put("out_trade_no",out_trade_no);
                map.put("type",type);
                map.put("name",name);
                map.put("money",money);
                map.put("trade_status",trade_status);
                StringBuffer SB=new StringBuffer();
                SB.append(http_build_query(map));
                SB.append(KEY);
                if (!"TRADE_SUCCESS" .equals(trade_status) ){
                    order.setOrderState(OrderState.fail.id());
                    msg = "支付失败";
                    return R.error(msg);
                }
                if(MD5.md5(SB.toString()).equals(sign)) {
                    msg = "支付成功";
                    order.setOrderState(OrderState.succ.id());
                    order.setNoticeState(1);
                    order.setBusinessNo(trade_no);
                    order.setMemo(money);
                    payOrderDao.update(order);
                    BigDecimal orderamount = order.getAmount();
                    BigDecimal sjamount = new BigDecimal(money);
//                    Integer  subt =Integer.parseInt(orderamount.subtract(sjamount).multiply(new BigDecimal("100")).toString());
//                    if(subt>=500){
//                        return R.error("实际支付金额与订单金额不相符！");
//                    }
                    String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                    payService.orderDataMsg(order.getMerchNo(), order.getOrderNo());
                    Merchant merchant = merchantService.get(order.getMerchNo());
                    String publicKey = merchant.getPublicKey();
                    logger.info("用户加密key ： " + publicKey);
                    LinkedHashMap jomap = new LinkedHashMap();
                    jomap.put("msg", msg);
                    System.out.println("msg========>"+msg);
                    jomap.put("amount", money);
                    jomap.put("orderNo", order.getOrderNo());
                    jomap.put("code", "1");
                    jomap.put("notifyTime", reqTime);
                    //用户公钥做加签处理
                    jomap.put("key", publicKey);
                    String text = http_build_query(jomap);
                    String Sign = MD5.md5(text);
                    jomap.put("sign", Sign);
                    //用户公钥不做参数传递
                    jomap.remove("key");
                    logger.info("方块 支付 回调 用户返回结果 jomap : " + jomap);
                    String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
                    logger.info(" 方块 支付 回调 用户返回结果 ： " + result);
                    return R.ok(msg);
                }else{
                    return  R.error("验签失败");
                }
            } catch (Exception e) {
                logger.info("方块 支付回调 异常："+e.getMessage());
                e.printStackTrace();
                order.setOrderState(OrderState.fail.id());
                payOrderDao.update(order);
                return R.error("方块 支付回调 异常");
            } finally{
                logger.info("{},{},{}", order.getMerchNo(), order.getOrderNo(), msg);
                logger.info("方块 支付回调 结束-------------------------------------------------");
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
            reString = reString.replace("%3D", "=").replace("%26", "&").replace("%3A",":").replace("%2F","/");
            return reString;
        }


    public static String genSign(Map<String, Object> paramMap, String key) {
        TreeMap<String,Object> treeMap=new TreeMap<>();
        for(Map.Entry<String,Object> entry:paramMap.entrySet()){
            treeMap.put(entry.getKey(),entry.getValue());
        }
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String,Object> entry:treeMap.entrySet()){
            if(!"".equals(entry.getValue())){
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString().substring(0,sb.toString().length()-1);
        result += key;
        logger.info("Sign Before MD5:" + result);
        result = MD5.md5(result).toLowerCase();
        logger.info("Sign Result:" + result);
        return result;

//        String result=PayDigestUtil.getSignV2(paramMap,key);
//        return result;
    }
}




