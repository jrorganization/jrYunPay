package com.qh.paythird.test;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.HttpClientUtil;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.dao.PayOrderDao;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * 支付测试对接 cbd 平台
 */

@Service
public class ZhiFuTest {

    private  static final Logger logger = LoggerFactory.getLogger(ZhiFuTest.class);

    @Autowired
    private PayOrderDao payOrderDao;
    /**
     * 下单路径
     */
    public final static String urlup = "http://www.long-pay.com/api/index/order";

    /**
     * @Description 支付发起
     * @param order
     * @return
     */
//    public R order(Order order) {
//
//        logger.info("测试钱包支付 开始------------------------------------------------------");
//        try {
//            /**
//             *
//             * 支付宝支付（H5）
//             * 支付宝扫码支付
//             * 支付宝刷卡支付
//             * 微信支付（H5）
//             * 微信扫码支付
//             * 微信刷卡支付
//             * 网银跳转
//             *
//             */
//
//            if (OutChannel.wx.name().equals(order.getOutChannel())) {
//                //微信扫码支付
////                return order_wx(order);
//            }
//
//            if (OutChannel.wxposs.name().equals(order.getOutChannel())) {
//                //微信刷卡支付
////                return order_wxposs(order);
//            }
//
//            if (OutChannel.wap.name().equals(order.getOutChannel())) {
//                //微信H5支付
//				return order_wap(order);
//            }
//
//            if (OutChannel.wy.name().equals(order.getOutChannel())) {
//                //网银支付
////                return order_wy(order);
//            }
//
//			if (OutChannel.ali.name().equals(order.getOutChannel())) {
//				//支付宝支付
////				return order_ali(order);
//			}
//
//            if (OutChannel.aliwap.name().equals(order.getOutChannel())) {
//                //支付宝H5支付
////				return order_aliwap(order);
//            }
//
//            if (OutChannel.aliposs.name().equals(order.getOutChannel())) {
//                //支付宝刷卡支付
////				return order_aliposs(order);
//            }
//
//            logger.error("测试钱包支付 不支持的支付渠道：{}", order.getOutChannel());
//            return R.error("不支持的支付渠道");
//        } finally {
//            logger.info("测试钱包支付 结束------------------------------------------------------");
//        }
//    }

    /**
     *
     * 微信H5支付
     */
//     private R order_wap (Order order){
//
//            return pay(order);
//     }


    /**
     * 支付数据
     */
    /*private R pay(Order order ){

        try {
            HashMap<String, String> map = new HashMap<String,String>() ;

            String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            //商户号
            map.put("user_id", "335");
            //订单号
            map.put("remark", reqTime + new Random().nextInt(10000));
            //交易金额
            map.put("pay_amount","1");
            //支付渠道
            map.put("channel_id", "53");
            //订单标题 & 产品编号
            map.put("product_id", "904");
            //前端返回地址
            map.put("return_url", "http://www.baidu.com");
            //后台通知地址
            map.put("notify_url", "http://192.168.11.151:8888");

            String contStr = http_build_query(map);

            logger.info("请求source:" + contStr);

            String context = Base64.encode(contStr.getBytes());

            byte[] decode = Base64.decode(context);
            System.out.println("decode : " + new String(decode));

            System.out.println("context : " + context);
            String sign = Md5Util.sign(context,"d53b8fc50f6e41223f6f85b1e7691e40","utf-8");
            String md5 = Md5.getMd5ofStr(context+"d53b8fc50f6e41223f6f85b1e7691e40");

//            System.out.println("sign ： " + sign);
//            System.out.println("md5 ： " + md5);

            JSONObject jo = new JSONObject();

            jo.put("sign", sign);
            jo.put("data", context);
            jo.put("user_id","335");

            Map<String,String > jomap = new HashMap<>();
            jomap.put("sign", sign);
            jomap.put("data", context);
            jomap.put("user_id","335");

            logger.info("请求参数：{}", jomap);
            logger.info("请求参数：{}", jomap.toString());

            Map result = HttpClientUtil.doPostQueryCmd(urlup,jomap);

            System.out.println("result : " + result.toString());

            String content = result.get("content").toString();
            System.out.println("content : " + content);
            if(StringUtils.isBlank(result.toString())){
                return R.error("支付返回参数为空");
            }
            JSONObject json = JSON.parseObject(content);
            logger.info("返回数据 ： " +json.get("data"));
            JSONObject jsondata = JSON.parseObject(json.get("data").toString());
            String resCode = json.get("code").toString();
            if("000000".equals(resCode)){
                return R.error(json.get("msg").toString());
            }else{
//                int ints = payOrderDao.save(order);
//                logger.info("ints : " + ints);
                Map<String,String> data = new HashMap<>();
                //拿出跳转地址返回
                logger.info("获取测试返回路径 ：" +jsondata.get("qrCode").toString() );
                data.put(PayConstants.web_qrcode_url,jsondata.get("qrCode").toString());
                return R.okData(data);
            }
        } catch (Exception e){
            logger.error("测试支付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("支付异常");
        } finally {
            logger.info("测试支付");
        }

    }*/


    static String http_build_query(Map<String, String> array){
        String reString = "";
        //遍历数组形成akey=avalue&bkey=bvalue&ckey=cvalue形式的的字符串
        Iterator it = array.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,String> entry =(Map.Entry) it.next();
            String key = entry.getKey();
            String value = entry.getValue();
            reString += key+"="+value+"&";
        }
        reString = reString.substring(0, reString.length()-1);
        System.out.println("reString : " + reString);
        //将得到的字符串进行处理得到目标格式的字符串
        reString = java.net.URLEncoder.encode(reString);
        System.out.println("reString : " + reString);
        reString = reString.replace("%3D", "=").replace("%26", "&");
        return reString;
    }

}


