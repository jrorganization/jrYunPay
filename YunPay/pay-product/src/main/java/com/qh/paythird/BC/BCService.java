package com.qh.paythird.BC;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.HttpClientUtil;
import com.qh.pay.dao.PayOrderDao;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayService;
import com.qh.paythird.BC.utils.MD5;
import org.apache.commons.collections4.map.LinkedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * 支付宝原生
 *
 */

@Service
public class BCService {

    private  static final Logger logger = LoggerFactory.getLogger(BCService.class);

    @Autowired
    private MerchantService merchantService;

    @Autowired
    private PayService payService;

    @Autowired
    PayOrderDao payOrderDao;

    /**
     * 下单路径
     */
    public final static String urlup = "http://pay.lepayzf.com/apisubmit";

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

            //在OutChannerl中加一个alipaywap！！！  只对接了alipaywap
           /* if (OutChannel.alipaywap.name().equals(order.getOutChannel())) {
                //支付宝原生
                return pay(order,"alipaywap");
            }*/



            if (OutChannel.wx.name().equals(order.getOutChannel())) {
                //微信扫码支付
                return pay(order,"weixin");
            }

            if (OutChannel.wap.name().equals(order.getOutChannel())) {
                //微信H5支付
				return pay(order,"wxwap");
            }

            if (OutChannel.wy.name().equals(order.getOutChannel())) {
                //网银支付
               return pay(order,"bank");
            }

			if (OutChannel.ali.name().equals(order.getOutChannel())) {
                //支付宝支付
				return pay(order,"alipay");
			}

            if (OutChannel.aliwap.name().equals(order.getOutChannel())) {
                //支付宝H5支付
                return pay(order,"aliwap");
            }



            logger.error("测试钱包支付 不支持的支付渠道：{}", order.getOutChannel());
            return R.error("不支持的支付渠道");
        } finally {
            logger.info("测试钱包支付 结束------------------------------------------------------");
        }
    }


    /**
     * 支付数据
     */
    private R pay(Order order,String payType ){
        logger.info("支付数据 ：" +order.toString());
        try {
            LinkedMap<String, String> map = new LinkedMap<>() ;

            String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            //版本号
            map.put("version", "1.0");

            //商户编号
            map.put("customerid", "10894");

            //订单金额
            map.put("total_fee",order.getAmount().setScale(2).toString());

            //商户订单号
            map.put("sdorderno", order.getOrderNo());

            //异步通知URL
            //map.put("notifyurl", "http://49.4.91.134:8182/pay/notify/BCPay/"+order.getMerchNo()+"/"+order.getOrderNo());
            map.put("notifyurl", "http://119.3.39.3:8182/pay/notify/BCPay/"+order.getMerchNo()+"/"+order.getOrderNo());

            //同步跳转URL
            map.put("returnurl", "www.baidu.com");

            String cont=http_build_query(map);
            cont +="&b3ff831f7fc101f6e57469deb3b2c9efcd88df3c";    //key
            String md5Cont= MD5.md5(cont);
            logger.info("md5==========>"+md5Cont);

            //支付编号
            map.put("paytype", "alipaywap");

            if(order.getOutChannel().equals(OutChannel.wy.name())){
                //银行编号
                map.put("bankcode","CMB");
            }

            //订单备注说明
            map.put("remark", "支付");

            //md5签名串
            map.put("sign", md5Cont);

            logger.info("请求参数：{}", map);
            logger.info("请求参数：{}", map.toString());

            Map result = HttpClientUtil.doPostQueryCmd(urlup,map);
            System.out.println("result : " + result.toString());
            System.out.println("status=========>"+result.get("status"));

            System.out.println("bool=========>"+result.get("status").toString().equals("302"));
            if(! result.get("status").toString().equals("302")){
                return R.error("支付异常!");
            }
            String content = result.get("content").toString();
            System.out.println("content : " + content);
            logger.info("返回数据 ： " +content);
            JSONObject json = JSON.parseObject(content);
            String resCode = json.get("code").toString();
            if(!"1".equals(resCode)){
                return R.error("提交订单失败！");
            }else{
                int ints = payOrderDao.save(order);
                logger.info("ints : " + ints);
                JSONObject data = new JSONObject();
                //拿出跳转地址返回
                logger.info("获取测试返回路径 ：" +json.get("url").toString() );
                data.put("payurl",json.get("url").toString() );
                data.put("payinfo","SUCCESS");
                data.put("msg","success");
                data.put("code","1");
                return R.okData(data);
            }
        } catch (Exception e){
            logger.error("测试支付 异常：" + e.getMessage());
            e.printStackTrace();
            return R.error("支付异常");
        } finally {
            logger.info("测试支付");
        }
    }


    /**
     * 后台返回地址
     * @param
     * @return
     */
    public R notify(Order order, HttpServletRequest request) {
        LinkedMap<String,String> requestMap=new LinkedMap<String,String>();
        requestMap.put("customerid",request.getParameter("customerid"));
        requestMap.put("status",request.getParameter("status"));
        requestMap.put("sdpayno",request.getParameter("sdpayno"));
        requestMap.put("sdorderno",request.getParameter("sdorderno"));
        requestMap.put("total_fee",request.getParameter("total_fee"));
        requestMap.put("paytype",request.getParameter("paytype"));
        String context=http_build_query(requestMap);
        context +="&b3ff831f7fc101f6e57469deb3b2c9efcd88df3c";    //key
        String md5Cont= MD5.md5(context);
        requestMap.put("sign",request.getParameter("sign"));
        System.out.println("回调加密++++++++++++++++++++"+md5Cont);
        String sign=request.getParameter("sign");
        if(!md5Cont.equals(sign)){
            return  R.error("验签失败!");
        }
        requestMap.put("remark",request.getParameter("remark"));

        String code="fail";
        String msg="失败";
        String pay_status=request.getParameter("status");
        if("1".equals(pay_status)){
            order.setOrderState(1);
            payOrderDao.update(order);
            payService.orderDataMsg(order.getMerchNo(),order.getOrderNo());
            code="success";
            msg="成功";
        }
        Map jomap=new HashMap();
        jomap.put("orderNo",order.getOrderNo());
        jomap.put("code",code);
        jomap.put("msg",msg);
        Map<String, Object> result=HttpClientUtil.doPostQueryCmd(order.getNotifyUrl(),jomap);
        System.out.println("result------------"+result);
        return R.ok();
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
        /*reString = java.net.URLEncoder.encode(reString);
        System.out.println("reString : " + reString);
        reString = reString.replace("%3D", "=").replace("%26", "&").replace("%3A",":").replace("%2F","/");
*/       return reString;
    }

}


