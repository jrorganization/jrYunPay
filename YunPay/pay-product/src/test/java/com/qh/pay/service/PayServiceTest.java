package com.qh.pay.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.pay.api.constenum.OutChannel;
import com.qh.pay.api.utils.Base64Utils;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.api.utils.RequestUtils;
import org.junit.Test;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @ClassName PayServiceTest
 * @Description 支付测试
 * @Date 2017年10月31日 上午11:42:01
 * @version 1.0.0
 */
@Service
public class PayServiceTest extends PayBaseServiceTest{

	public final static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRATDdlWIjeXHlqVpn2CiwMWtkTJRO998UPlYoGw9cS7ZJVU8tChVKinUbp2HCMdpGHsdnAw43ixw49u0K+mQCyo2/Y6HURNwclvOIWt8rBoH3FmjlrnkmC8lyD8ULtvGUK236O0jP430tzdCbaQpRUPqPEH5pWK4TQUx3pcfkPQIDAQAB";

	public  final static String privatestr = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQAq0/GuQOAifRYiE4htVrxhWVco+dTpK4zvTqZpfOVwEjVPe7IsvSX4usny3biI5fyXXclYyZCGTmy0Q3w8WqPZBFngkDgabFa7IME7stgODEEWnVGdDXE0RlCNt+Lut1zF+EJ1ekBI+TNO8y6MpNsrjVusUOydpMHZ5+8Xq/iwIDAQAB";

	/**
	 * 
	 *订单支付测试
	 * @throws Exception 
	 */
	@Test
	public void order_test() throws Exception{
		JSONObject jsObj = new JSONObject();
		String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		//商户号
		jsObj.put("merchNo", "SH714443");
		//jsObj.put("merchNo", "SH992255");
		//订单号
		String orderNo=reqTime + new Random().nextInt(100000);
		jsObj.put("orderNo", orderNo);
		System.out.println("orderNo--------"+orderNo);
		//支付渠道
		jsObj.put("outChannel", OutChannel.acp.name());
		if(OutChannel.acp.name().equals(jsObj.get("outChannel"))){
			jsObj.put("bankCode", "BOC");
			jsObj.put("bankName", "建设银行");
            jsObj.put("acctName", "sdsd");
            jsObj.put("certType", "1");
            jsObj.put("certNo", "430502199710251526");
		}
		//用户标志
		jsObj.put("userId", "201");
		//订单标题
		jsObj.put("title", "chongzhi");
		//产品名称
		jsObj.put("product", "chongzhi");
		//支付金额 单位 元
		jsObj.put("amount", "1");
		//币种
		jsObj.put("currency", "CNY");
		//前端返回地址
		jsObj.put("returnUrl", "http://192.168.11.151");
		//后台通知地址
		jsObj.put("notifyUrl", "http://119.3.39.3:8181/pay/notify/"+"YunPay"+"/"+"SH992255"+"/"+orderNo);
		//请求时间
		jsObj.put("reqTime", reqTime);
		//对公
		jsObj.put("acctType", 1);
		String key="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDdPNt6QId4/b5W/pFxj2eSGEDcg0X3wRrptDchb2gDK6T2YnSbTrBew7ea24joGWN84DTkiAS3sTTNFd4ltVVrXJNpVlHGCNMhFvkGtPfj091m49Jl/uf0Yby6Ek0Osj6eHWuW38p1cAsDa464pe7LIvoeyxAuK35d43M2M5X3uwIDAQAB";
		System.out.println("JSON.toJSONBytes(jsObj) ： " +JSON.toJSONBytes(jsObj));
		byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
		System.out.println("context ： " + context);
		String ccc = Base64Utils.encode(context);
		System.out.println("ccc ：" + ccc);
		//SH714443
		String sign = Md5Util.sign(ccc,key,"UTF-8");
		//SH992255
		//String sign = Md5Util.sign(ccc,privatestr,"UTF-8");

		logger.info("签名结果：{}" ,sign);
		JSONObject jo = new JSONObject();
		jo.put("sign", sign);
		jo.put("context", context);
		logger.info("请求参数：{}", jo.toJSONString());

//		String result = RequestUtils.doPostJson(url, jo.toJSONString());
//		logger.info("请求结果！{}",result);
//		jo = JSONObject.parseObject(result);
//		if("0".equals(jo.getString("code"))){
//			sign = jo.getString("sign");
//			context = jo.getBytes("context");
//			if(RSAUtil.verify(context, publicKey, sign)){
//				String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
//				logger.info("解密结果：" + source);
//				jo = JSONObject.parseObject(source);
//				logger.info("网银支付链接地址：{}", jo.getString("code_url"));
//
//			}else{
//				logger.info("验签失败！{}");
//			}
//		}

		}

//	}
	
	/**
	 * 
	 * @Description 支付订单查询
	 * @throws Exception
	 */
	@Test
	public void order_query() throws Exception{
		String orderNo = "201711151647288410";
		JSONObject jsObj = new JSONObject();
		//商户号
		jsObj.put("merchNo", merchNo);
		jsObj.put("orderNo", orderNo);
		byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
		String sign = RSAUtil.sign(context, mcPrivateKey);
		logger.info("签名结果：{}" ,sign);
		JSONObject jo = new JSONObject();
		jo.put("sign", sign);
		jo.put("context", context);
		logger.info("请求参数：{}", jo.toJSONString());
		String result = RequestUtils.doPostJson(url + "/query", jo.toJSONString());
		logger.info("请求结果！{}",result);
		jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			sign = jo.getString("sign");
			context = jo.getBytes("context");
			if(RSAUtil.verify(context, publicKey, sign)){
				String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
				logger.info("解密结果：" + source);
				jo = JSONObject.parseObject(source);
				logger.info("订单支付状态", jo.getString("orderState"));
				
			}else{
				logger.info("验签失败！{}");
			}
		}
	}
	
	
	/**
	 * 
	 * @Description 代付订单
	 * @throws Exception
	 */
	@Test
	public void order_acp() throws Exception{
		JSONObject jsObj = new JSONObject();
		String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		//商户号
		jsObj.put("merchNo", "SH992255");
		//订单号
		jsObj.put("orderNo", reqTime + new Random().nextInt(10000));	
		//支付渠道  ---acp
		jsObj.put("outChannel", OutChannel.acp.name());
		jsObj.put("bankCode", "BOC");
		jsObj.put("bankName", "建设银行");
		//用户标识
		jsObj.put("userId","335");
		//订单标题 
		jsObj.put("title", "商城网银代付");
		//产品名称
		jsObj.put("product", "产品名称");
		//代付付金额 单位 元
		jsObj.put("amount", "10");
		//jsObj.put("amount", String.valueOf(new Random().nextInt(10000)));
		//币种
		jsObj.put("currency", "CNY");
		//后台通知地址
		jsObj.put("notifyUrl", "http://www.baidu.com");
		//请求时间
		jsObj.put("reqTime", reqTime);
		//银行卡
		jsObj.put("acctName","das");
		//证件号
		jsObj.put("certNo","123123456");
		//对公
		jsObj.put("acctType", 1);

		byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
		String sign = Md5Util.sign(new String(context), "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQAq0/GuQOAifRYiE4htVrxhWVco+dTpK4zvTqZpfOVwEjVPe7IsvSX4usny3biI5fyXXclYyZCGTmy0Q3w8WqPZBFngkDgabFa7IME7stgODEEWnVGdDXE0RlCNt+Lut1zF+EJ1ekBI+TNO8y6MpNsrjVusUOydpMHZ5+8Xq/iwIDAQAB","UTF-8");
		logger.info("签名结果：{}" ,sign);
		JSONObject jo = new JSONObject();
		jo.put("sign", sign);
		jo.put("context", context);
		logger.info("请求参数：{}", jo.toJSONString());
		String result = RequestUtils.doPostJson(url + "/acp", jo.toJSONString());
		logger.info("请求结果！{}",result);
		jo = JSONObject.parseObject(result);
		if("0".equals(jo.getString("code"))){
			sign = jo.getString("sign");
			context = jo.getBytes("context");
			if(RSAUtil.verify(context, publicKey, sign)){
				String source = new String(RSAUtil.decryptByPrivateKey(context, mcPrivateKey));
				logger.info("解密结果：" + source);
				jo = JSONObject.parseObject(source);
				logger.info("网银支付链接地址：{}", jo.getString("code_url"));
			}else{
				logger.info("验签失败！{}");
			}
		}
	}
	
}
