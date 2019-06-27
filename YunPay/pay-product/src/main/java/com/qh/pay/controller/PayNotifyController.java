package com.qh.pay.controller;

import com.alibaba.fastjson.JSON;
import com.qh.common.config.Constant;
import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.OrderState;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.api.utils.RequestUtils;
import com.qh.pay.dao.PayOrderDao;
import com.qh.pay.service.PayService;
import com.qh.redis.service.RedisUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @ClassName PayNotifyController
 * @Description 回调
 * @Date 2017年11月9日 下午2:26:14
 * @version 1.0.0
 */
@RestController
@RequestMapping("/pay")
public class PayNotifyController {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayNotifyController.class);

	
	@Autowired
	private PayService payService;
	
	@Autowired
	private PayOrderDao payOrderDao;
	
	/**
	 * 
	 * @Description 支付前台回调
	 * @param company
	 * @param merchNo
	 * @param orderNo
	 * @param response
	 */
	@RequestMapping("/return/{company}/{merchNo}/{orderNo}")
	public void returnUrl(@PathVariable("company") String company, @PathVariable("merchNo") String merchNo, @PathVariable("orderNo") String orderNo, 
			HttpServletResponse response){
		logger.info("前台回调：{},{},{}",company,merchNo,orderNo);
		Order order = RedisUtil.getOrder(merchNo,orderNo);
		if (order == null) {
			logger.error("订单为空：{},{},{}",company,merchNo,orderNo);
			order = payOrderDao.get(orderNo);
		}
		
		if(order != null){
			try {
				logger.info("调用返回地址：{}",order.getReturnUrl());
				response.sendRedirect(order.getReturnUrl());
			} catch (IOException e) {
				logger.error("{}返回异常！", order.getReturnUrl());
			}
		} else {
			handlerReturnError(company, merchNo,orderNo,response);
		}
	}
	
	/**
	 * @param orderNo 
	 * @param merchNo 
	 * @param response 
	 * @Description 处理错误结果
	 */
	private void handlerReturnError(String company, String merchNo, String orderNo, HttpServletResponse response) {
		try {
			logger.info("调用错误结果处理：{}","订单不存在！" + company + ","+ merchNo + "," + orderNo);
			response.setCharacterEncoding("UTF-8");
			PrintWriter pw = response.getWriter();
			pw.write("订单不存在！" + company + ","+ merchNo + "," + orderNo);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			logger.error("handlerReturnError异常！{},{},{}",company,merchNo,orderNo);
		}
		
	}

	/**
	 * 
	 * @Description 支付后台通知
	 * @param request
	 * @param requestBody
	 * @return
	 */

	@RequestMapping("/notify/{company}/{merchNo}/{orderNo}")
	public String notifyUrl(@PathVariable("company") String company, @PathVariable("merchNo") String merchNo, @PathVariable("orderNo") String orderNo,
			HttpServletRequest request,@RequestBody(required=false) String requestBody){
		logger.info("后台通知：{},{},{}",company,merchNo,orderNo);
		try{
			TreeMap<String, String> params = RequestUtils.getRequestParam(request);
			logger.info("回调信息："+ JSON.toJSONString(params));
			logger.info("回调信息:"+requestBody);
		}catch (Exception e){
			logger.info("回调信息:"+requestBody);
		}
		Order order=payOrderDao.get(orderNo);
//		logger.info("回调通知 获取交易金额 ： " + order.getAmount().toString());
//		order.setNoticeState(0);
		//Order order = RedisUtil.getOrder(merchNo, orderNo);
		if(order != null){
			Integer orderState = order.getOrderState();
			if(new Integer(OrderState.init.id()).equals(orderState) || new Integer(OrderState.ing.id()).equals(orderState)||new Integer(OrderState.fail.id()).equals(orderState)) {
				R  r = payService.notify(merchNo,orderNo,request, requestBody);
				logger.info("获取回调通知code值 ： " + r.get(Constant.result_code));
				return "success";
//				return notifyStr(String.valueOf(r.get(Constant.result_code)), company);
			}else {
				logger.info("订单已被处理：{},{},{},{}",company,merchNo,orderNo,orderState);
			}
		}else {
			logger.info("订单不存在：{},{},{}",company,merchNo,orderNo);
			return "failed";
		}
		return "success";
//		return notifyStr(String.valueOf(Constant.result_code_succ), company);
	}


	public static final Map<String,String> notifyStrMap = new HashMap<>();
	static{
		notifyStrMap.put(Constant.result_code_succ + PayCompany.ysb.name() , "result=SUCCESS");
		notifyStrMap.put(Constant.result_code_error + PayCompany.ysb.name(), "result=FAILED");
	}
	
	/**
	 * @Description 返回提示
	 * @param code
	 * @param company
	 * @return
	 */
	private String notifyStr(String code,String company) {
		return notifyStrMap.get(code + company);
	}
	
	/**
	 * 
	 * @Description 代付后台通知
	 * @param company
	 * @param merchNo
	 * @param orderNo
	 * @param request
	 * @param requestBody
	 * @return
	 */
	@RequestMapping("/notify/acp/{company}/{merchNo}/{orderNo}")
	public String notifyAcpUrl(@PathVariable("company") String company, @PathVariable("merchNo") String merchNo, @PathVariable("orderNo") String orderNo,
			HttpServletRequest request,@RequestBody(required=false) String requestBody){
		logger.info("代付后台通知：{},{},{}",company,merchNo,orderNo);
		logger.info("代付数据requestBody:"+requestBody);
		logger.info("代付数据request:"+ JSON.toJSONString(request));

		Order order = RedisUtil.getOrderAcp(merchNo, orderNo);
		if(order != null){
			Integer orderState = order.getOrderState();
			if(new Integer(OrderState.init.id()).equals(orderState) || new Integer(OrderState.ing.id()).equals(orderState)){
				R  r = payService.notifyAcp(merchNo,orderNo,request, requestBody);
				return notifyStr(String.valueOf(r.get(Constant.result_code)), company);
			}else {
				logger.info("代付订单已被处理：{},{},{},{}",company,merchNo,orderNo,orderState);
			}
		}else {
			logger.info("代付订单不存在：{},{},{}",company,merchNo,orderNo);
		}
		return notifyStr(String.valueOf(Constant.result_code_succ), company);
	}
}
