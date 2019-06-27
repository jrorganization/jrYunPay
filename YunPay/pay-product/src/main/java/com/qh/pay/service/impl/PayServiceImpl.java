package com.qh.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.config.CfgKeyConst;
import com.qh.common.config.Constant;
import com.qh.common.domain.UserBankDO;
import com.qh.common.service.UserBankService;
import com.qh.common.utils.R;
import com.qh.moneyacct.domain.MoneyacctDO;
import com.qh.moneyacct.querydao.RcMerchBalDao;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.*;
import com.qh.pay.api.utils.*;
import com.qh.pay.dao.*;
import com.qh.pay.domain.*;
import com.qh.pay.service.*;
import com.qh.paythird.PayBaseService;
import com.qh.paythird.VNET.utils.MD5;
import com.qh.paythird.VNET.utils.RQPayUtils;
import com.qh.redis.RedisConstants;
import com.qh.redis.constenum.ConfigParent;
import com.qh.redis.service.RedisMsg;
import com.qh.redis.service.RedisUtil;
import com.qh.redis.service.RedissonLockUtil;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName PayServiceImpl
 * @Description 支付实现类
 * @Date 2017年11月6日 下午2:48:20
 * @version 1.0.0
 */
@Service
public class PayServiceImpl implements PayService {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayServiceImpl.class);
	@Autowired
	private PayConfigCompanyService payCfgCompService;
	@Autowired
	private PayBaseService payBaseService;
	@Autowired
	private MerchantService merchantService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private PayHandlerService payHandlerService;
	@Autowired
	private PayOrderDao payOrderDao;
	@Autowired
	private RecordFoundAcctDao rdFoundAcctDao;
	@Autowired
	private RecordMerchBalDao rdMerchBalDao;
	@Autowired
	private PayAuditDao payAuditDao;
	@Autowired
	private PayOrderAcpDao payOrderAcpDao;
	@Autowired
	private RcMerchBalDao rcMerchantDao;
	@Autowired
	private RecordMerchAvailBalDao rdMerchAvailBalDao;
	@Autowired
	private RecordFoundAvailAcctDao rdFoundAvailAcctDao;
	@Autowired
	private RecordPayMerchAvailBalDao rdPayMerchAvailBalDao;
	@Autowired
	private RecordPayMerchBalDao rdPayMerchBalDao;
	@Autowired
	private PayQrService payQrService;
	@Autowired
	private UserBankService userBankService;
	@Autowired
	private PayAuditService payAuditService;
	/**
	 * 发起支付
	 */
	@Override
	public Object order(Merchant merchant, JSONObject jo) {
		String merchNo = merchant.getMerchNo();
		String orderNo = jo.getString(OrderParamKey.orderNo.name());
		RLock lock = RedissonLockUtil.getOrderLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			try {
				Order order = new Order();
				order.setOrderType(OrderType.pay.id());
				// 渠道编码
				order.setOutChannel(jo.getString(OrderParamKey.outChannel.name()));
				logger.info("渠道编码 jo.getString(OrderParamKey.outChannel.name()) : " + jo.getString(OrderParamKey.outChannel.name()));
				String initResult = null;
				if (OutChannel.jfDesc().containsKey(order.getOutChannel())) {
					//初始化扫码通道订单
					initResult = payHandlerService.initQrOrder(order, jo);
				}else{
					// 初始化订单信息
					initResult = payHandlerService.initOrder(order, jo);
				}
				if (ParamUtil.isNotEmpty(initResult)) {
					logger.error(initResult);
					return R.error(initResult);
				}
				//userId不能为商户号
				if(ParamUtil.isNotEmpty(order.getUserId()) && merchNo.equals(order.getUserId())){
					return R.error("userId不能与商户号一致");
				}
				if (RedisUtil.getOrder(merchNo, orderNo) != null) {
					logger.error(merchNo + "," + orderNo + "订单号已经存在！");
					return R.error(merchNo + "," + orderNo + "订单号已经存在！");
				}else if(payOrderDao.get(orderNo)!= null) {
					logger.error(merchNo + "," + orderNo + "订单号已经存在！");
					return R.error(merchNo + "," + orderNo + "订单号已经存在！");
				}
				R r;
				if (OutChannel.jfDesc().containsKey(order.getOutChannel())) {
					//扫码通道订单处理
					r = payQrService.qrOrder(order,merchant);
					if(R.ifError(r) && order.getRealAmount() != null && order.getRealAmount().compareTo(BigDecimal.ZERO) > 0 ){
						payQrService.releaseMonAmount(order);
					}
					order.setPayCompany(PayCompany.jf.name());
				}else{
					//检查支付规则
					r = this.checkCfgComp(order);
					if(R.ifError(r)){
						return r;
					}
					r = (R) payBaseService.order(order);
				}
				
				//返回处理
				if (R.ifSucc(r)) {
					logger.info("r : " + r);
					@SuppressWarnings("unchecked")
					JSONObject jsondata = JSON.parseObject(r.get("data").toString());
					Map<String, String> data = (Map<String, String>) jsondata.get("data");
					order.setResultMap(data);
					RedisUtil.setOrder(order);
					logger.info("order : " + merchantService.get(order.getMerchNo()).toString());
					logger.info("data : " + data);
//					int ints = payOrderDao.save(order);
//					logger.info("ints : " +ints);
//					if (ints==0){
//						return R.error(merchNo + "," + orderNo + "下单失败！");
//					}
					RedisUtil.getRedisNotifyTemplate().opsForValue().set(RedisConstants.cache_keyevent_not_pay_ord + merchNo + RedisConstants.link_symbol +  orderNo, RedisConstants.keyevent_40, RedisConstants.keyevent_40, TimeUnit.MINUTES);
//					return decryptData(r,merchant,jo).put(Constant.result_msg,
//							r.get(Constant.result_msg));
					return decryptData(r,merchant,jo);
				}
				return r;
			} finally {
				lock.unlock();
			}
		} else {
			return R.error(merchNo + "," + orderNo + "下单失败！");
		}
	}

	@Override
	public R queryAmount(Merchant merchant) {
		MoneyacctDO moneyacct = new MoneyacctDO().initZero();
		String merchNo=merchant.getMerchNo();
		moneyacct = rcMerchantDao.statMerchByNo(merchNo);
		PayAcctBal merchBal = RedisUtil.getMerchBal(merchNo);
		if(merchBal != null && moneyacct != null) {
			moneyacct.setMerchNo(merchBal.getUsername());
			moneyacct.setBalance(merchBal.getBalance());
			moneyacct.setAvailBal(merchBal.getAvailBal());
		}
		System.out.println(moneyacct.getAvailBal());
		JSONObject rdata = new JSONObject();
		rdata.put("merchNo",merchNo);
		rdata.put("amount",moneyacct.getAvailBal());
		return R.okData(rdata);
	}


	/**
	 * @Description 支付通道 选择
	 * @param order
	 * @return
	 */
	private R checkCfgComp(Order order) {
		Merchant merchant = merchantService.get(order.getMerchNo());
		logger.info("支付通道 ：" + JSONArray.toJSONString(merchant));
		Integer mPayChannelType = merchant.getPayChannelType();
		logger.info("检查通道 ： " + mPayChannelType);
		logger.info("检查通道 ： " + PayChannelType.desc().containsKey(mPayChannelType));
		//判断商户是否有分配  通道分类   没有分配不支持支付
		if(mPayChannelType == null || !PayChannelType.desc().containsKey(mPayChannelType)) {
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未分配支付通道");
		}

		//List<Object> payCfgComps = payCfgCompService.getPayCfgCompByOutChannel(order.getOutChannel());
		List<PayConfigCompanyDO> payCfgComps = payCfgCompService.getByChannel(order.getOutChannel());
		logger.info("支付通道 ：" + order.getOutChannel());
		logger.info("支付通道 ：" + JSONArray.toJSONString(payCfgComps));

		if (payCfgComps == null || payCfgComps.size() == 0) {
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "通道配置错误！");
		}
		List<PayConfigCompanyDO> pccList = new ArrayList<>();
		Map<String,String> payCompany=merchant.getPayCompany();
		System.out.println(payCompany.toString());
		String company=payCompany.get(order.getOutChannel());
		PayConfigCompanyDO payCfgComp=payCfgCompService.getByCompany(company,order.getOutChannel());
		Map<String,String> coinSwitch=merchant.getCoinSwitch();
		Integer s=Integer.parseInt(coinSwitch.get(order.getOutChannel()));
		if (s == 0) {
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "支付通道未开启");
		}
		if(payCfgComp==null){
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未找到相应的支付通道");
		}
		if(payCfgComp.getIfClose() == 0){
			Integer cPayChannelType = payCfgComp.getPayChannelType();
			logger.info("支付通道 选择 cPayChannelType : " + cPayChannelType);
			Integer paymentMethod = payCfgComp.getPaymentMethod();
			logger.info("支付通道 选择 paymentMethod : " + paymentMethod);
			pccList.add(payCfgComp);
			logger.info("支付通道 选择 pccList : " + JSONArray.toJSONString(pccList));
		}
		pccList.add(payCfgComp);
		/*PayConfigCompanyDO payCfgComp = null;
		for (Object object : payCfgComps) {
			payCfgComp = (PayConfigCompanyDO) object;
			Map<String,String> coinSwitch=merchant.getCoinSwitch();
			Integer s=Integer.parseInt(coinSwitch.get(order.getOutChannel()));
//			payCfgComp.setIfClose(s);
			if (s == 0) {
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "支付通道未开启");
			}
			if(payCfgComp.getIfClose() == 0){
				Integer cPayChannelType = payCfgComp.getPayChannelType();
				logger.info("支付通道 选择 cPayChannelType : " + cPayChannelType);
				Integer paymentMethod = payCfgComp.getPaymentMethod();
				logger.info("支付通道 选择 paymentMethod : " + paymentMethod);
				pccList.add(payCfgComp);
				logger.info("支付通道 选择 pccList : " + JSONArray.toJSONString(pccList));
			}
		}*/

		if(pccList.size() <= 0) {
			//当前商户没有 同类通道
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未找到相应的支付通道");
		}

		//同类 通道只有一个
		payCfgComp = pccList.get(0);
		String payPeriod=payCfgComp.getPayPeriod();
		if(payPeriod!=null&& !(payPeriod.equals(""))) {
			SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
			Date currentTime = new Date();
			String dateString = formatter.format(currentTime);
			//将当前时间转换为int类型
			Integer ss = DateUtil.timeToInt(dateString);
			String[] strings=payPeriod.split(",");
			for (int i=0;i<strings.length;i++){
				System.out.println("strings[i]=============>"+strings[i]);
				String[] split = strings[i].split("-");
				logger.info("时间 ： " + split[0]);
				Integer f = Integer.parseInt(split[0]);
				Integer l = Integer.parseInt(split[1]);
				if (ss < f || ss > l) {
					return R.error(order.getMerchNo() + "," + order.getOutChannel() + "支付通道的支付时间为" + DateUtil.intFormatToTime(payCfgComp.getPayPeriod()));
				}
			}
		}
		if(payCfgComp == null){
			return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未找到相应的支付通道");
		}
		Integer maxPayAmt = payCfgComp.getMaxPayAmt();
		Integer minPayAmt = payCfgComp.getMinPayAmt();
		if(maxPayAmt != null && new BigDecimal(maxPayAmt).compareTo(order.getAmount()) == -1){
			return R.error("单笔最高限额:"+maxPayAmt+"元");
		}
		if(minPayAmt != null && new BigDecimal(minPayAmt).compareTo(order.getAmount()) == 1){
			return R.error("单笔最低限额为:"+minPayAmt+"元");
		}
		order.setPayCompany(payCfgComp.getCompany());
		order.setPayMerch(payCfgComp.getPayMerch());
		order.setCallbackDomain(payCfgComp.getCallbackDomain());
		return R.ok();
	}


	
	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#notify(java.lang.String,
	 * java.lang.String, java.lang.String,
	 * javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public R notify(String merchNo, String orderNo, HttpServletRequest request, String requestBody) {
		logger.info("后台返回数据++++++++++++++++++++ ： " + merchNo);
		RLock lock = RedissonLockUtil.getOrderLock(merchNo + RedisConstants.link_symbol + orderNo);
		logger.info("后台返回数据++++++++++++++++++++ ： " + lock);
		try {
			lock.lock();
			Order order = payOrderDao.get(orderNo);
			logger.info("后台返回数据++++++++++++++++++++ ： " + order);
			if (order != null &&(order.getLastLockTime() == null || DateUtil.getCurrentTimeInt() - order.getLastLockTime() > 20)) {
				R r = payBaseService.notify(order, request, requestBody);
				System.out.println("R.msg=========>"+(String) r.get(Constant.result_msg));
				//order.setMsg((String) r.get(Constant.result_msg));
				order.setLastLockTime(DateUtil.getCurrentTimeInt());
				RedisUtil.setOrder(order);
//				payOrderDao.update(order);
				lock.unlock();
				RedisMsg.orderNotifyMsg(merchNo, orderNo);
				logger.info("rrrrrrrrrrrrrrrrrr : " + r);
				return r;
			}
		} finally {
			if(lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
		return R.error("无效的结果");
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderNotifyMsg(java.lang.String)
	 */
	@Override
	public String orderNotifyMsg(String merchNo, String orderNo) {
		RLock lock = RedissonLockUtil.getOrderLock(merchNo, orderNo);
		if (lock.tryLock()) {
			try {
				String result = orderNotify(merchNo, orderNo);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("orderNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#eventOrderNotifyMsg(java.lang.String, java.lang.String)
	 */
	@Override
	public String eventOrderNotifyMsg(String merchNo, String orderNo) {
		RLock lock = RedissonLockUtil.getEventOrderLock(merchNo, orderNo);
		if (lock.tryLock()) {
			try {
				String result = orderNotify(merchNo, orderNo);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("eventOrderNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}
	/**
	 * @Description 通知保存
	 * @param merchNo
	 * @param orderNo
	 * @return
	 */
	private String orderNotify(String merchNo, String orderNo) {
		return orderNotify(RedisUtil.getOrder(merchNo,orderNo));
	}

	/**
	 * @Description 订单通知
	 * @param order
	 * @return
	 */
	private String orderNotify(Order order) {
		if(order == null){
			return null;
		}
		String stateDesc = OrderState.desc().get(order.getOrderState());
		String result = null;
		logger.info("发送通知请求：{},{},{},{}", order.getNotifyUrl(),order.getMerchNo(), order.getOrderNo(),stateDesc);
		if (OrderState.init.id() == order.getOrderState()) {
			//RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
			logger.info("{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
		} else {
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount()==null?order.getAmount().toString():order.getRealAmount().toString());
			data.put(OrderParamKey.orderNo.name(), order.getOrderNo());
			Merchant merchant = merchantService.get(order.getMerchNo());
			result = RequestUtils.doPostJson(order.getNotifyUrl(),
					decryptAndSign(data, merchant.getPublicKey(), order.getMsg()).jsonStr());
			logger.info("{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
			if(result.contains("ok") || result.contains("success")) {
				result = Constant.result_msg_ok;
			}
		}
		return result;
	}
	
	/**
	 * @Description 订单通知
	 * @param order
	 * @return
	 */
	private String orderNotify(Order order,String notifyUrl) {
		if(order == null){
			return null;
		}
		String stateDesc = OrderState.desc().get(order.getOrderState());
		String result = null;
		if(StringUtils.isBlank(notifyUrl))
			notifyUrl = order.getNotifyUrl();
		logger.info("发送通知请求：{},{},{},{}", notifyUrl,order.getMerchNo(), order.getOrderNo(),stateDesc);
		if (OrderState.init.id() == order.getOrderState()) {
			//RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
			logger.info("{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
		} else {
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount().toString());
			data.put(OrderParamKey.orderNo.name(), order.getOrderNo());
			Merchant merchant = merchantService.get(order.getMerchNo());
			result = RequestUtils.doPostJson(notifyUrl,
					decryptAndSign(data, merchant.getPublicKey(), order.getMsg()).jsonStr());
			logger.info("{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
			if(result.contains("ok") || result.contains("success")) {
				result = Constant.result_msg_ok;
			}
		}
		return result;
	}
	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderDataMsg(java.lang.String,java.lang.String)
	 */
	@Override
	public void orderDataMsg(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				//Order order = RedisUtil.getOrder(merchNo,orderNo);
				Order order=payOrderDao.get(orderNo);
				if (order == null) {
					logger.info("支付订单数据保存失败，订单不存在，{}，{}",merchNo,orderNo);
					return;
				}
				Integer orderState = order.getOrderState();
				if (orderState == OrderState.succ.id() || orderState == OrderState.fail.id()
						|| orderState == OrderState.close.id()) {
					boolean saveFlag = false;
					if(OutChannel.jfDesc().containsKey(order.getOutChannel())){
						saveFlag = payQrService.saveQrOrderData(order);
					}else{
						saveFlag = this.saveOrderData(order);
					}
					if (saveFlag) {
						RedisUtil.removeOrder(merchNo,orderNo);
						logger.info("支付订单数据保存成功，{}，{}",merchNo,orderNo);
						lock.unlock();
						RedisUtil.delKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo);
					}
				}else{
					logger.info("支付订单数据保存失败，订单状态'{}'不满足条件，{}，{}",orderState,merchNo,orderNo);
				}
			} finally {
				if(lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}

		}else {
			logger.info("支付订单数据保存失败，未获取到锁，不能保存条件，{}，{}",merchNo,orderNo);
		}
	}

	/**
	 * @Description 保存订单数据
	 * @param order
	 */
	@Transactional
	boolean saveOrderData(Order order) {
		// 商户信息
		Merchant merchant = merchantService.get(order.getMerchNo());
		// 支付通道信息
		PayConfigCompanyDO payCfgComp = payCfgCompService.get(order.getPayCompany(), order.getPayMerch(),
				order.getOutChannel());
		BigDecimal amount = order.getAmount();
		//费率单位
		Integer costRateUnit = payCfgComp.getCostRateUnit();
		logger.info("费率单位 ----------------- :" +costRateUnit);
		// 成本金额
		if(payCfgComp.getCostRate() != null){
			if(costRateUnit.equals(PaymentRateUnit.PRECENT.id())) {
				order.setCostAmount(ParamUtil.multBig(amount, payCfgComp.getCostRate().divide(new BigDecimal(100))));
				logger.info("成本CostAmount-------------"+order.getCostAmount());
			} else if(costRateUnit.equals(PaymentRateUnit.YUAN.id())) {
				order.setCostAmount(payCfgComp.getCostRate());
			}
		}else{
			order.setCostAmount(BigDecimal.ZERO);
		}
		// 商户金额
		BigDecimal jfRate = null;
		Integer jfUnit = null;
		String outChannel = order.getOutChannel();
		logger.info("商户金额 ++++++++++++++++++++++++++ " + outChannel);
		Map<String,String> rateMap = merchant.getCoinRate().get(outChannel);
		jfRate = new BigDecimal(rateMap.get(PayConstants.PAYMENT_RATE).toString());
		jfUnit = Integer.valueOf(rateMap.get(PayConstants.PAYMENT_UNIT).toString());
		order.setPaymentMethod(PaymentMethod.D0.id());
		BigDecimal qhAmount = BigDecimal.ZERO;
		if(jfRate != null){
			if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
				jfRate = jfRate.divide(new BigDecimal(100));
				qhAmount = ParamUtil.multBig(amount, jfRate);
			}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
				qhAmount = jfRate;
			}
		}
		BigDecimal maxFee = payCfgComp.getMaxFee();
		BigDecimal minFee = payCfgComp.getMinFee();
		if(minFee != null && minFee.compareTo(qhAmount) == 1) {
			qhAmount = minFee;
		}else if(maxFee != null && maxFee.compareTo(qhAmount) == -1) {
			qhAmount = maxFee;
		}else if(PayConstants.MIN_FEE.compareTo(qhAmount) == 1){
			qhAmount = PayConstants.MIN_FEE;
		}
		order.setQhAmount(qhAmount);
		logger.info("qhAmount------------"+qhAmount);
		// 代理信息
		Agent agent = agentService.get(merchant.getParentAgent());
		rateMap = agent.getCoinRate().get(outChannel);
		jfRate = new BigDecimal(rateMap.get(PayConstants.PAYMENT_RATE).toString());
		jfUnit = Integer.valueOf(rateMap.get(PayConstants.PAYMENT_UNIT).toString());
		BigDecimal agentAmount = BigDecimal.ZERO;
		if (jfRate != null) {
			if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
				logger.info("jfP----------------------");
				jfRate = jfRate.divide(new BigDecimal(100));
				agentAmount = amount.multiply(jfRate);
				//ParamUtil.multSmall(amount, jfRate);   不做舍去,否则在后续算手续费时可能给代理多算 如：654行
			}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
				System.out.println("");
				agentAmount = jfRate;
			}
			logger.info("代理费:agentAmount---------------"+agentAmount);
		}
		String parentAgentNumber = agent.getParentAgent();
		//上级代理
		if(agent.getLevel() == AgentLevel.two.id() ) {
			 System.out.println("二级代理-------------");
			order.setSubAgentAmount(ParamUtil.subSmall(order.getQhAmount(), agentAmount));//qhAmount=3.51  agentAmount=2.6499   在644行：不舍去99等于0.8601  舍去99等于0.87
			Agent paramAgent = agentService.get(parentAgentNumber);
			rateMap = paramAgent.getCoinRate().get(outChannel);
			jfRate = new BigDecimal(rateMap.get(PayConstants.PAYMENT_RATE).toString());
			logger.info("代理jfRate---------------------"+jfRate);
			jfUnit = Integer.valueOf(rateMap.get(PayConstants.PAYMENT_UNIT).toString());
			/*if (ParamUtil.isNotEmpty(merchant.getFeeRate())) {
				feeRate = merchant.getFeeRate().get(order.getOutChannel());
			}*/
			if (jfRate != null) {
				BigDecimal parentAgentAmount = BigDecimal.ZERO;
				if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
					jfRate = jfRate.divide(new BigDecimal(100));
					parentAgentAmount = amount.multiply(jfRate);//ParamUtil.multSmall(amount, jfRate);  不做舍去,否则在后续算手续费时可能给代理多算
				}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
					parentAgentAmount = jfRate;
				}
				logger.info("parentAgentAmount--------------"+parentAgentAmount);
				order.setAgentAmount(ParamUtil.subSmall(agentAmount, parentAgentAmount));
				agentAmount = parentAgentAmount;
			}
		}else {
			System.out.println("一级代理------------------------");
			logger.info("一级agent-----------"+agentAmount);
			order.setAgentAmount(agentAmount);
		}
		int crtDate = order.getCrtDate();
		if (ParamUtil.isNotEmpty(order.getMsg()) && order.getMsg().length() > 50) {
			order.setMsg(order.getMsg().substring(0, 50));
		}
		order.setClearState(ClearState.succ.id());
		order.setRealAmount(new BigDecimal(order.getAmount().subtract(qhAmount).toString()));
		logger.info(new BigDecimal(order.getAmount().subtract(qhAmount).toString()).toString());
		payOrderDao.update(order);
		int orderState = order.getOrderState();
		if (orderState != OrderState.succ.id()) {
			return true;
		}
		String key = CfgKeyConst.MERCHANT_DAY_LIMIT+order.getMerchNo();
		Object value = RedisUtil.getValue(key);
		if(value!=null) {
			BigDecimal amountDay = (BigDecimal)value;
			amountDay = amountDay.add(order.getAmount());
			RedisUtil.setValue(key, amountDay);
			RedisUtil.getRedisTemplate().expire(key, DateUtil.getDayLeftSeconds(), TimeUnit.SECONDS);
		}
		key = CfgKeyConst.MERCHANT_MONTH_LIMIT+order.getMerchNo();
		value = null;
		value = RedisUtil.getValue(key);
		if(value!=null) {
			BigDecimal amountMonth = (BigDecimal)value;
			amountMonth = amountMonth.add(order.getAmount());
			RedisUtil.setValue(key, amountMonth);
			RedisUtil.getRedisTemplate().expire(key, DateUtil.getMonthLeftSeconds(), TimeUnit.SECONDS);
		}
		// 增加商户余额以及流水
		RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchAdd(order, order.getAmount(),
				FeeType.merchIn.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchBalDao.save(rdMerchBal);
		//减少商户手续费
		rdMerchBal = payHandlerService.balForMerchSub(order, order.getQhAmount(),
				FeeType.merchHandFee.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchBalDao.save(rdMerchBal);

		RecordFoundAcctDO rdFoundAcct = null;
		// 增加代理余额以及流水
		if (agent.getLevel() == AgentLevel.two.id()) {
			rdFoundAcct = payHandlerService.balForAgentAdd(order, order.getSubAgentAmount(),agent.getAgentNumber(), FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);
			// 增加上级代理余额以及流水
			rdFoundAcct = payHandlerService.balForAgentAdd(order, order.getAgentAmount(),parentAgentNumber, FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);
		}else {
			rdFoundAcct = payHandlerService.balForAgentAdd(order, order.getAgentAmount(),agent.getAgentNumber(), FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);
		}
		BigDecimal subAgentAmount = order.getSubAgentAmount();
		subAgentAmount =  subAgentAmount == null?BigDecimal.ZERO:subAgentAmount;
		BigDecimal platMoney = order.getQhAmount().subtract(order.getCostAmount().add(order.getAgentAmount()).add(subAgentAmount));
		platMoney = platMoney.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : platMoney;
		// 增加平台资金账户余额以及流水
		rdFoundAcct = payHandlerService.balForPlatAdd(order,platMoney,
				FeeType.platIn.id(),OrderType.pay.id());
		rdFoundAcct.setCrtDate(crtDate);
		rdFoundAcctDao.save(rdFoundAcct);

		/*// 增加平台资金账户可用余额以及流水
		rdFoundAcct = payHandlerService.availBalForPlatAdd(order, platMoney, FeeType.platIn.id(),OrderType.pay.id());
		rdFoundAcct.setCrtDate(crtDate);
		rdFoundAvailAcctDao.save(rdFoundAcct);
		*/
		// 增加第三方支付公司资金账户余额以及流水

		RecordPayMerchBalDO rdPayMerchAcct = payHandlerService.balForPayMerchAdd(order,order.getCostAmount(),
				FeeType.payMerchTrade.id(),OrderType.pay.id());
		rdPayMerchAcct.setCrtDate(crtDate);
		rdPayMerchBalDao.save(rdPayMerchAcct);
		
		// 减去第三方支付公司资金账户余额以及流水    减手续费

		/*rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getCostAmount(),

				FeeType.payMerchTradeHand.id(),OrderType.pay.id());
		rdPayMerchAcct.setCrtDate(crtDate);
		rdPayMerchBalDao.save(rdPayMerchAcct);
*/
		try {
			//dZeroSettlement(order, agent, parentAgentNumber, crtDate, amount);
			List<Order> updateOrders = new ArrayList<Order>();
			order.setClearState(ClearState.succ.id());
			updateOrders.add(order);
			if(payHandlerService.updateClearStateBatch(payCfgComp.getCompany(), updateOrders) > 0){
				payHandlerService.availBalForOrderClearSucc(updateOrders);
			}
		} catch (Exception e) {
			logger.debug("D0清算异常(保存订单数据)");
			e.printStackTrace();
		}
		//记录支付公司单个用户的当日交易金额
		JSONObject useCapitalPoolJo = (JSONObject)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.CACHE_COMPANY_MERCHANT_CAPITAL_POOL + order.getPayCompany(), order.getPayMerch());
		if(useCapitalPoolJo!=null) {
			BigDecimal curMoney = useCapitalPoolJo.getBigDecimal(RedisConstants.COMPANY_MERCHANT_CUR_MONEY);
			curMoney = curMoney == null?new BigDecimal(0):curMoney;
			curMoney = curMoney.add(order.getAmount());
			useCapitalPoolJo.put(RedisConstants.COMPANY_MERCHANT_CUR_MONEY, curMoney);
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.CACHE_COMPANY_MERCHANT_CAPITAL_POOL + order.getPayCompany(), order.getPayMerch(), useCapitalPoolJo);
		}
		return true;
	}

	/**
	 * D0 清算
	 * @param order
	 * @param agent
	 * @param parentAgentNumber
	 * @param crtDate
	 * @param ptAmount
	 */
	@Transactional
	void dZeroSettlement(Order order, Agent agent, String parentAgentNumber, int crtDate,
			BigDecimal ptAmount) {
		RecordMerchBalDO rdMerchBal = null;
		RecordFoundAcctDO rdFoundAcct = null;
		//D0结算,实时结算
		// 增加商户可用余额以及流水
		rdMerchBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(),
				FeeType.merchIn.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchAvailBalDao.save(rdMerchBal);
		// 减少商户可用余额以及流水 手续费
		rdMerchBal = payHandlerService.availBalForMerchSub(order, order.getQhAmount(),
				FeeType.merchHandFee.id(),  OrderType.pay.id());
		rdMerchBal.setCrtDate(crtDate);
		rdMerchAvailBalDao.save(rdMerchBal);
		if (agent.getLevel() == AgentLevel.two.id()) {
			rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getQhAmount().subtract(order.getSubAgentAmount()), agent.getAgentNumber(), FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);
			// 增加上级代理余额以及流水
			rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getSubAgentAmount().subtract(order.getAgentAmount()), parentAgentNumber, FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);
			ptAmount = order.getAgentAmount();
		}else {
			rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getQhAmount().subtract(order.getAgentAmount()), agent.getAgentNumber(), FeeType.agentIn.id(),OrderType.pay.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);
		}
		ptAmount = order.getAgentAmount();
		// 增加平台资金账户可用余额以及流水
		rdFoundAcct = payHandlerService.availBalForPlatAdd(order,
				ptAmount.subtract(order.getCostAmount()),
				FeeType.platIn.id(),OrderType.pay.id());
		rdFoundAcct.setCrtDate(crtDate);
		rdFoundAvailAcctDao.save(rdFoundAcct);
		
		// 增加第三方支付公司资金账户可用余额以及流水
		RecordPayMerchBalDO rdPayMerchAcct = payHandlerService.availBalForPayMerchAdd(order,order.getAmount(),
				FeeType.payMerchTrade.id(),OrderType.pay.id());
		rdPayMerchAcct.setCrtDate(crtDate);
		rdPayMerchAvailBalDao.save(rdPayMerchAcct);
		
		// 减去第三方支付公司资金账户可用余额以及流水    减手续费
		rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getCostAmount(),
				FeeType.payMerchTradeHand.id(),OrderType.pay.id());
		rdPayMerchAcct.setCrtDate(crtDate);
		rdPayMerchAvailBalDao.save(rdPayMerchAcct);
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#query(com.qh.pay.domain.Merchant,
	 * com.alibaba.fastjson.JSONObject)
	 */
	@Override
	public R query(Merchant merchant, JSONObject jo) {
		String orderNo = jo.getString(OrderParamKey.orderNo.name());
		if (ParamUtil.isEmpty(orderNo)) {
			return R.error("查询支付订单号为空！");
		}
 		RLock lock = RedissonLockUtil.getOrderLock(merchant.getMerchNo() + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			Order order = null;
			String msg = Constant.result_msg_succ;
			try {
				order = RedisUtil.getOrder(merchant.getMerchNo(), orderNo);
				boolean dataFlag = false;
				if (order == null) {
					order = payOrderDao.get(orderNo);
					dataFlag = true;
				}
				if (order == null) {
					return R.error(merchant.getMerchNo() + "," + orderNo + "支付订单不存在！");
				}
				// 无支付成功结果去第三方查询
				if (!dataFlag && OrderState.succ.id() != order.getOrderState()) {
					R r = payBaseService.query(order);
					if (R.ifError(r)) {
						return r;
					}
					msg = (String) r.get(Constant.result_msg);
					order.setMsg(msg);
					RedisUtil.setOrder(order);
					if(OrderState.succ.id() == order.getOrderState()){
						if(payOrderDao.get(orderNo)== null) {
							//支付成功。 同步保存
							lock.unlock();
							RedisMsg.orderDataMsg(merchant.getMerchNo(),orderNo);
						}else {
							logger.info("订单接口查询，订单已存在，不发通知保存.{},{}",merchant.getMerchNo(),orderNo);
						}
					}
				}
			} finally {
				if(lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), String.valueOf(order.getRealAmount()));
			data.put(OrderParamKey.orderNo.name(), order.getOrderNo());
			return decryptAndSign(data, merchant.getPublicKey(), msg);
		} else {
			return R.error("查询过于繁忙，请稍后再试！");
		}
	}

	/**
	 * @param data
	 * @return
	 * @Description 公钥加密，私钥签名
	 */
	private R decryptAndSign(Map<String, ?> data, String publicKey) {
		return decryptAndSign(data, publicKey, "");
	}

	/**
	 * @param rc
	 * @return
	 * @Description 返回数据MD5加密
	 */
	private R decryptData(Map<String, ?> rc,Merchant merchant,JSONObject jo) {
		return decryptData(rc, merchant,jo,"");
	}

	/**
	 * @param data
	 * @return
	 * @Description 公钥加密，私钥签名
	 */
	private R decryptAndSign(Map<String, ?> data, String publicKey, String msg) {
		try {
			logger.info("回调商户 返回明文数据:" + JSON.toJSONString(data));
			logger.info("回调商户 返回明文数据:" + data.toString());
			logger.info("回调商户 返回明文数据:" + Base64Utils.encode(JSON.toJSONBytes(data)));

			String orderNO = data.get("orderNo").toString();
			String merchNo=data.get("merchNo").toString();
			Order order = payOrderDao.get(orderNO);
			if(null == order){
				order=payOrderAcpDao.get(orderNO,merchNo);
			}
			LinkedHashMap jomap=new LinkedHashMap();
			String code = "1";
			String msgs = "支付成功";
			if(order.getOrderState()==OrderState.init.id()){
				msgs="代付订单已受理";
			}
			String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			Merchant merchant = merchantService.get(order.getMerchNo());
			String publicKeys = merchant.getPublicKey();
			logger.info("补单回调 publicKey ： " + publicKey );
			BigDecimal amount = order.getAmount();
			jomap.put("msg",msgs);
			jomap.put("amount",amount.toString());
			jomap.put("orderNo",order.getOrderNo());
			jomap.put("code",code);
			jomap.put("notifyTime",reqTime);
			//用户公钥做加签处理
			jomap.put("key",publicKeys);
			String text = http_build_query(jomap);
			String Sign = MD5.md5(text);
			jomap.put("sign",Sign);
			//用户公钥不做参数传递
			jomap.remove("key");
			R r = R.ok();
			r.put("msg",msgs);
			r.put("amount",amount.toString());
			r.put("orderNo",order.getOrderNo());
			r.put("code",code);
			r.put("notifyTime",reqTime);
			r.put("sign",Sign);
			logger.info("回调商户 返回加密数据:" + JSON.toJSONString(r));
			return r;
		} catch (Exception e) {
			logger.error("返回数据签名 失败！");
		}
		return R.error("返回数据签名失败！");
	}

	/**
	 * @param
	 * @return
	 * @Description
	 */
	private R decryptData(Map<String, ?> rc,Merchant merchant ,JSONObject jo, String msg) {
//		try {
//			logger.info("回调商户 返回明文数据:" + JSON.toJSONString(rc));
//			logger.info("rc.get(data).toString() : " + rc.get("data").toString() );
//
//			String merchNo = merchant.getMerchNo();
//			String orderNo = jo.getString(OrderParamKey.orderNo.name());
//			logger.info("返回数据merchNo ：" + merchNo);
//			logger.info("返回数据orderNo ：" + orderNo);
//			R r = R.ok();
//			JSONObject jsondata = JSON.parseObject(rc.get("data").toString());
//			//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//			//原生支付宝H5做处理
//			 System.out.println("jsondata : " + jsondata);
//			String msgstr = jsondata.get("msg").toString().toLowerCase();
//			logger.info("jsondata ： " +jsondata);
//			String  jsoncont = jsondata.get("context").toString();
//
//			//页面返回值也不同
//			r.put("context", jsoncont);
//			r.put("merchNo", merchNo).put("orderNo", orderNo).put("code",msgstr.equals("success")?"1":"0");
//			logger.info("回调商户 返回加密数据:" + JSON.toJSONString(r));
//			return r;
//		} catch (Exception e) {
//			logger.error(JSON.toJSONString(rc));
//		}
//		return R.error(JSON.toJSONString(rc));


		//82
		try {
			logger.info("回调商户 返回明文数据:" + JSON.toJSONString(rc));
			logger.info("rc.get(data).toString() : " + rc.get("data").toString() );
			String msgStr = rc.get("msg").toString();
			String merchNo = merchant.getMerchNo();
			String orderNo = jo.getString(OrderParamKey.orderNo.name());
			logger.info("返回数据merchNo ：" + merchNo);
			logger.info("返回数据orderNo ：" + orderNo);
			R r = R.ok();
			JSONObject jsondata = JSON.parseObject(rc.get("data").toString());
			logger.info("jsondata ： " +jsondata);
			//Baes64加密
			String conStr = Base64Utils.encode(JSON.toJSONBytes(jsondata));
			r.put("context", conStr);
			r.put("merchNo", merchNo).put("orderNo", orderNo).put("code",msgStr.equals("success")?"1":"0");
			logger.info("回调商户 返回加密数据:" + JSON.toJSONString(r));
			return r;
		} catch (Exception e) {
			logger.error("返回数据签名 失败！");
		}
		return R.error("返回数据签名失败！");

	}


	private R acp_decryptData(Map<String, ?> rc,Merchant merchant ,JSONObject jo, String msg) {
		try {
			logger.info("回调商户 返回明文数据:" + JSON.toJSONString(rc));
			logger.info("rc.get(data).toString() : " + rc.get("data").toString() );
			String msgStr = rc.get("msg").toString();
			String merchNo = merchant.getMerchNo();
			String orderNo = jo.getString("order_id");
			logger.info("返回数据merchNo ：" + merchNo);
			logger.info("返回数据orderNo ：" + orderNo);
			R r = R.ok();
			JSONObject jsondata = JSON.parseObject(rc.get("data").toString());
			logger.info("jsondata ： " +jsondata);
			//Baes64加密
			String conStr = Base64Utils.encode(JSON.toJSONBytes(jsondata));
			r.put("context", conStr);
			r.put("merchNo", merchNo).put("orderNo", orderNo).put("code",msgStr.equals("success")?"1":"0");
			logger.info("回调商户 返回加密数据:" + JSON.toJSONString(r));
			return r;
		} catch (Exception e) {
			logger.error("返回数据签名 失败！");
		}
		return R.error("返回数据签名失败！");
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderAcp(com.qh.pay.domain.Merchant,
	 * com.alibaba.fastjson.JSONObject)
	 */
	@Override
	public R orderAcp(Merchant merchant, JSONObject jo) {
		String merchNo = merchant.getMerchNo();
		String orderNo = jo.getString(OrderParamKey.orderNo.name());
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			try {
				Order order = new Order();
				// 初始化订单信息
                order.setOutChannel(OutChannel.acp.name());
				String initResult = payHandlerService.initOrder(order, jo);

				userBankService.save(order.getMobile(),merchNo, order);
				order.setOrderType(OrderType.acp.id());
				if (ParamUtil.isNotEmpty(initResult)) {
					logger.error(initResult);
					return R.error(initResult);
				}
				//userId不能为商户号
				if(ParamUtil.isNotEmpty(order.getUserId()) && merchNo.equals(order.getUserId())){
					return R.error("userId不能与商户号一致");
				}
				if (RedisUtil.getOrderAcp(merchNo, orderNo) != null) {
					logger.error(merchNo + "," + orderNo + "订单号已经存在！");
					return R.error(merchNo + "," + orderNo + "订单号已经存在！");
				}else if(payOrderAcpDao.get(orderNo, merchNo)!= null) {
					logger.error(merchNo + "," + orderNo + "订单号已经存在！");
					return R.error(merchNo + "," + orderNo + "订单号已经存在！");
				}
				
				BigDecimal amount = order.getAmount();
				//计算出  商户代付  手续费费率
				Map<String,?> paidMap = merchant.getCoinRate().get(OutChannel.acp.name());
				BigDecimal jfRate = new BigDecimal(paidMap.get(PayConstants.PAYMENT_RATE).toString());
				Integer jfUnit = Integer.valueOf(paidMap.get(PayConstants.PAYMENT_UNIT).toString());
				if(jfRate != null){
					/*if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
						jfRate = jfRate.divide(new BigDecimal(100));
						order.setQhAmount(ParamUtil.multBig(amount, jfRate));
					}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
						order.setQhAmount(jfRate);
					}*/
					//单笔扣除固定的手续费
					order.setQhAmount(jfRate);
				}
				
				amount = amount.add(order.getQhAmount());
				
				List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<RecordMerchBalDO>();
				RecordMerchBalDO rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.merchAcpOut.id(),
						OrderType.acp.id(), ProfitLoss.loss.id());
				rdMerchAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
				// 检查商户余额 -----》商户可用余额
				RLock merchLock = RedissonLockUtil.getBalMerchLock(merchNo);
				try {
					merchLock.lock();
					PayAcctBal pab = RedisUtil.getMerchBal(merchNo);
					System.out.println(pab.getAvailBal());
					if (pab == null || pab.getAvailBal().compareTo(amount) < 0) {
						return R.error("商户号 " + merchNo + ",可用余额不足！");
					}
					R r = checkAcpCfgComp(order);
					if(R.ifError(r)){
						return r;
					}
					rdMerchAvailBal.setTranAmt(order.getAmount());
					payHandlerService.availBalForMerchSub(order, rdMerchAvailBal, pab);
					RedisUtil.setMerchBal(pab);
					rdMerchAvailBalList.add(rdMerchAvailBal);
					
					//扣除商户代付手续费
					rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.merchAcpHandFee.id(),
							OrderType.acp.id(), ProfitLoss.loss.id());
					rdMerchAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
					rdMerchAvailBal.setTranAmt(order.getQhAmount());
					payHandlerService.availBalForMerchSub(order, rdMerchAvailBal, pab);
					RedisUtil.setMerchBal(pab);
					rdMerchAvailBalList.add(rdMerchAvailBal);
				} finally {
					merchLock.unlock();
				}
				PayAuditDO payAudit = PayService.initPayAudit(order, AuditType.order_acp.id());
				int count = 0;
				try {
					payAudit.setCrtTime(rdMerchAvailBal.getCrtDate());
					count = payAuditDao.save(payAudit);
				} catch (Exception e) {
					count = 0;
				}
				try {
					//保存可用余额流水
					rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
				} catch (Exception e) {
					logger.error("代付保存可用余额流水记录失败！{}，{}",merchNo,orderNo);
				}
				if (count == 1) {
					RedisUtil.setOrderAcp(order);
					//如果成功，商户余额减少
					try {
						rdMerchBalDao.save(payHandlerService.balForMerchSub(order, order.getAmount(), FeeType.merchAcpOut.id(),	OrderType.acp.id()));
						rdMerchBalDao.save(payHandlerService.balForMerchSub(order, order.getQhAmount(), FeeType.merchAcpHandFee.id(),	OrderType.acp.id()));
					} catch (Exception e) {
						logger.error("代付保存余额流水记录失败！{}，{}",merchNo,orderNo);
					}
					
					//自动审核通过
					String pollMoneyValue = RedisUtil.getConfigValue(CfgKeyConst.PAY_AUDIT_AUTO_ACP, ConfigParent.payAuditConfig.name());
					if(pollMoneyValue != null && YesNoType.yes.id() == Integer.parseInt(pollMoneyValue)) {
						if(payAuditService.audit(orderNo,merchNo,AuditType.order_acp.id(),AuditResult.pass.id(),null)>0){
							lock.unlock();
							payOrderAcpDao.save(order);
							R r=orderAcp(order.getMerchNo(),order.getOrderNo());
							if(R.ifError(r)){
								return r;
							}
						}
					}
					Map<String, String> data = PayService.initRspData(order);
					data.put(OrderParamKey.orderState.name(), String.valueOf(OrderState.init.id()));
					return decryptAndSign(data, merchant.getPublicKey(), "代付订单已受理!");
				} else {// 出错了返还可用余额
					rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(), FeeType.merchAcpFail.id(), OrderType.acp.id());
					rdMerchAvailBal.setCrtDate(payAudit.getCrtTime());
					rdMerchAvailBalDao.save(rdMerchAvailBal);
					//返回手续费
					rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(), FeeType.merchAcpHandFee.id(), OrderType.acp.id());
					rdMerchAvailBal.setCrtDate(payAudit.getCrtTime());
					rdMerchAvailBalDao.save(rdMerchAvailBal);
					return R.error("代付异常！");
				}
			} finally {
				if(lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
		} else {
			return R.error(merchNo + "," + orderNo + "下单失败！");
		}
	}





	/*
	 * (非 Javadoc) Description: 审核未通过 可用余额/余额返还
	 * 
	 * @see
	 * com.qh.pay.service.PayService#orderAcpNopassDataMsg(java.lang.String,java.lang.String)
	 */
	@Override
	public void orderAcpNopassDataMsg(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrderAcp(merchNo,orderNo);
				if (order != null) {
					//提现与代付区别
					Integer orderType = order.getOrderType();
					Integer feeType = FeeType.merchAcpFail.id();
					if(orderType == null){
						orderType = OrderType.acp.id();
					}
					if(orderType == OrderType.withdraw.id()){
						feeType = FeeType.withdrawFail.id();
					}
					Integer userType = order.getUserType();
					if(userType != null && (UserType.agent.id() == userType || UserType.subAgent.id() == userType)) {
						List<RecordFoundAcctDO> rdFoundAvailBalList = new ArrayList<RecordFoundAcctDO>();
						List<RecordFoundAcctDO> rdFoundBalList = new ArrayList<RecordFoundAcctDO>();
						int crtTime = DateUtil.getCurrentTimeInt();
						RecordFoundAcctDO rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getAmount(),order.getMerchNo(), feeType, orderType);
						rdAgentAvailBal.setCrtDate(crtTime);
						rdFoundAvailBalList.add(rdAgentAvailBal);
						
						rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getQhAmount(),order.getMerchNo(), FeeType.agentWithDrawHandFee.id(), orderType);
						rdAgentAvailBal.setCrtDate(crtTime);
						rdFoundAvailBalList.add(rdAgentAvailBal);
						
						RecordFoundAcctDO rdAgentBal = payHandlerService.balForAgentAdd(order, order.getAmount(),order.getMerchNo(), feeType,orderType);
						rdAgentBal.setCrtDate(crtTime);
						rdFoundBalList.add(rdAgentBal);
						rdAgentBal = payHandlerService.balForAgentAdd(order, order.getQhAmount(),order.getMerchNo(), FeeType.agentWithDrawHandFee.id(),	orderType);
						rdAgentBal.setCrtDate(crtTime);
						rdFoundBalList.add(rdAgentBal);
						
						RedisUtil.removeOrderAcp(merchNo,orderNo);
						
						rdFoundAvailAcctDao.saveBatch(rdFoundAvailBalList);
						rdFoundAcctDao.save(rdFoundBalList.get(0));
						rdFoundAcctDao.save(rdFoundBalList.get(1));
					}else {
						List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<RecordMerchBalDO>();
						List<RecordMerchBalDO> rdMerchBalList = new ArrayList<RecordMerchBalDO>();
						//返还可用余额
						RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(),feeType,orderType);
						rdMerchAvailBalList.add(rdMerchAvailBal);
						rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(),FeeType.merchWithDrawHandFee.id(),orderType);
						rdMerchAvailBalList.add(rdMerchAvailBal);
						//返还余额
						RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchAdd(order, order.getAmount(),feeType,orderType);
						rdMerchBalList.add(rdMerchBal);
						rdMerchBal = payHandlerService.balForMerchAdd(order, order.getQhAmount(),FeeType.merchWithDrawHandFee.id(),orderType);
						rdMerchBalList.add(rdMerchBal);
						
						RedisUtil.removeOrderAcp(merchNo,orderNo);
						
						rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
						rdMerchBalDao.save(rdMerchBalList.get(0));
						rdMerchBalDao.save(rdMerchBalList.get(1));
					}
					
					//如果是提现 不需要发送
					if(orderType != OrderType.withdraw.id()){
						logger.error("发送代付审核不通过请求：{},{},{}", order.getNotifyUrl(),order.getMerchNo(), order.getOrderNo());
						RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
					}
				}
				order.setOrderState(OrderState.back.id());
				order.setClearState(ClearState.fail.id());
				payOrderAcpDao.update(order);
			} finally {
				lock.unlock();
			}
		}
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderAcp(java.lang.String,java.lang.String)
	 */
	@Override
	public R orderAcp(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
//		if (lock.tryLock()) {
			try {
				//Order order = RedisUtil.getOrderAcp(merchNo,orderNo);
				Order order = payOrderAcpDao.get(orderNo,merchNo);
				if(order == null){
					return R.error("代付订单不存在");
				}
				R r = payBaseService.orderAcp(order);
				System.out.println(R.ifSucc(r));
				order.setMsg((String) r.get(Constant.result_msg));
				if(R.ifError(r)) {
					order.setOrderState(OrderState.fail.id());
				}
				Merchant merchant=merchantService.get(merchNo);
				RedisUtil.setOrderAcp(order);
				lock.unlock();
				//如果成功，并且订单状态成功，并且是实时返回结果则直接发起回调
				if(R.ifSucc(r) ){
					//	&& Integer.valueOf(OrderState.succ.id()).equals(order.getOrderState())	&& Integer.valueOf(YesNoType.yes.id()).equals(r.get(PayConstants.acp_real_time))

						logger.info("r : " + r);
						@SuppressWarnings("unchecked")
						JSONObject jsondata = JSON.parseObject(r.get("data").toString());
						Map<String, String> data = (Map<String, String>) jsondata.get("data");
						order.setResultMap(data);
						RedisUtil.setOrder(order);
						logger.info("order : " + merchantService.get(order.getMerchNo()).toString());
//						logger.info("data : " + data);
						RedisUtil.getRedisNotifyTemplate().opsForValue().set(RedisConstants.cache_keyevent_not_pay_ord + merchNo + RedisConstants.link_symbol +  orderNo, RedisConstants.keyevent_40, RedisConstants.keyevent_40, TimeUnit.MINUTES);
						return acp_decryptData(r,merchant,jsondata,"");
				}
				if(Integer.valueOf(OrderState.fail.id()).equals(order.getOrderState())) {
					r = R.error(order.getMsg());
				}
				return r;
			} finally {
				if(lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
//		}else {
//			logger.info("orderAcp，未获取到锁，{}，{}",merchNo,orderNo);
//		}
		//return null;
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderAcpNotifyMsg(java.lang.String,java.lang.String)
	 */
	@Override
	public String orderAcpNotifyMsg(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				String result = orderAcpNotify(merchNo, orderNo);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
		}else {
			logger.info("orderAcpNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#eventOrderAcpNotifyMsg(java.lang.String, java.lang.String)
	 */
	@Override
	public String eventOrderAcpNotifyMsg(String merchNo, String orderNo) {
		RLock lock = RedissonLockUtil.getEventOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				String result = orderAcpNotify(merchNo, orderNo);
				lock.unlock();
				return result;
			} finally {
				if(lock.isHeldByCurrentThread())
					lock.unlock();
			}
		}else {
			logger.info("eventOrderAcpNotifyMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}
		return null;
	}
	/**
	 * @Description 代付订单通知发起
	 * @param merchNo
	 * @param orderNo
	 * @return
	 */
	private String orderAcpNotify(String merchNo, String orderNo) {
		Order order = RedisUtil.getOrderAcp(merchNo, orderNo);
		if(Integer.valueOf(OrderType.withdraw.id()).equals(order.getOrderType())){
			return Constant.result_msg_ok;
		}
		return orderAcpNotify(order);
	}


	/**
	 * @Description 代付通知
	 * @param order
	 * @return
	 */
	private String orderAcpNotify(Order order,String notifyUrl) {
		if(order == null){
			return null;
		}
		String stateDesc = OrderState.desc().get(order.getOrderState());
		String result = null;
		if(StringUtils.isBlank(notifyUrl)) {
			notifyUrl = order.getNotifyUrl();
		}
		logger.info("发送代付通知请求：{},{},{},{}", notifyUrl,order.getMerchNo(), order.getOrderNo(),stateDesc);
		if (OrderState.init.id() == order.getOrderState()) {
			//result = RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
			logger.info("代付发起{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo());
		} else {
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount()==null?order.getAmount().toString():order.getRealAmount().toString());
			Merchant merchant = merchantService.get(order.getMerchNo());
			
			result = RequestUtils.doPostJson(notifyUrl,
					decryptAndSign(data, merchant.getPublicKey(), order.getMsg()).jsonStr());
			logger.info("代付发起{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
			if(result.contains("ok") || result.contains("success")) {
				result = Constant.result_msg_ok;
			}
		}
		return result;
	}

	/**
	 * @Description 代付通知
	 * @param order
	 * @return
	 */
	private String orderAcpNotify(Order order) {
		if(order == null){
			return null;
		}
		String stateDesc = OrderState.desc().get(order.getOrderState());
		String result = null;
		logger.info("发送代付通知请求：{},{},{},{}", order.getNotifyUrl(),order.getMerchNo(), order.getOrderNo(),stateDesc);
		if (OrderState.init.id() == order.getOrderState()) {
			//result = RequestUtils.doPostJson(order.getNotifyUrl(), R.error(order.getMsg()).jsonStr());
			logger.info("代付发起{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo());
		} else {
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(order.getOrderState()));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount()==null?order.getAmount().toString():order.getRealAmount().toString());
			Merchant merchant = merchantService.get(order.getMerchNo());
			result = RequestUtils.doPostJson(order.getNotifyUrl(),
					decryptAndSign(data, merchant.getPublicKey(), order.getMsg()).jsonStr());
			logger.info("代付发起{}状态返回结果：{},{},{}", stateDesc, order.getMerchNo(), order.getOrderNo(), result);
			if(result.contains("ok") || result.contains("success")) {
				result = Constant.result_msg_ok;
			}
		}
		return result;
	}

	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#orderAcpDataMsg(java.lang.String,java.lang.String)
	 */
	@Override
	public void orderAcpDataMsg(String merchNo,String orderNo) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrderAcp(merchNo,orderNo);
				if (order == null) {
					logger.info("代付订单数据保存失败，订单不存在，{}，{}",merchNo,orderNo);
					return;
				}
				Integer orderState = order.getOrderState();
				if (orderState == OrderState.succ.id() || orderState == OrderState.fail.id()
						|| orderState == OrderState.close.id()) {
					Integer orderType = order.getOrderType();
					//order.setClearState(ClearState.succ.id());
                   // order.setOrderState(OrderState.succ.id());
					if(OrderType.withdraw.id() == orderType) {
						if (this.saveOrderWithdrawData(order)) {
							RedisUtil.removeOrderAcp(merchNo,orderNo);
							logger.info("提现订单数据保存成功，{}，{}",merchNo,orderNo);
							lock.unlock();
							RedisUtil.delKeyEventExpired(RedisConstants.cache_keyevent_acp, merchNo, orderNo);
						}
					}else {
						if (this.saveOrderAcpData(order)) {
							RedisUtil.removeOrderAcp(merchNo,orderNo);
							logger.info("代付订单数据保存成功，{}，{}",merchNo,orderNo);
							lock.unlock();
							RedisUtil.delKeyEventExpired(RedisConstants.cache_keyevent_acp, merchNo, orderNo);
						}
					}
				}else{
					logger.info("代付订单数据保存失败，订单状态'{}'不满足条件，{}，{}",orderState,merchNo,orderNo);
				}
			} finally {
				if(lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
		}else {
			logger.info("orderAcpDataMsg，未获取到锁，{}，{}",merchNo,orderNo);
		}

	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#notifyAcp(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public R notifyAcp(String merchNo, String orderNo, HttpServletRequest request, String requestBody) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo + RedisConstants.link_symbol + orderNo);
		try {
			lock.lock();
			Order order = RedisUtil.getOrderAcp(merchNo, orderNo);
			if (order != null &&(order.getLastLockTime() == null || DateUtil.getCurrentTimeInt() - order.getLastLockTime() > 20)) {
				R r = payBaseService.notifyAcp(order, request, requestBody);
				order.setMsg((String) r.get(Constant.result_msg));
				RedisUtil.setOrderAcp(order);
				RedisMsg.orderAcpNotifyMsg(merchNo, orderNo);
				return r;
			}
		} finally {
			lock.unlock();
		}
		return R.error("无效的结果");
	}

	
	/*
	 * (非 Javadoc) Description:
	 * 
	 * @see com.qh.pay.service.PayService#acpQuery(com.qh.pay.domain.Merchant,
	 * com.alibaba.fastjson.JSONObject)
	 */
	@Override
	public R acpQuery(Merchant merchant, JSONObject jo) {
		String orderNo = jo.getString(OrderParamKey.orderNo.name());
		if (ParamUtil.isEmpty(orderNo)) {
			return R.error("查询代付订单号为空！");
		}
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchant.getMerchNo() + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			Order order = null;
			Integer orderState = null;
			String msg = null;
			try {
				order = RedisUtil.getOrderAcp(merchant.getMerchNo(), orderNo);
				boolean dataFlag = false;
				if (order == null) {
					order = payOrderAcpDao.get(orderNo, merchant.getMerchNo());
					dataFlag = true;
				}
				if (order == null) {
					return R.error(merchant.getMerchNo() + "," + orderNo + "代付订单不存在！");
				}
				orderState = order.getOrderState();
				// 无支付结果去第三方查询
				if (!dataFlag && OrderState.succ.id() != order.getOrderState()) {
					R r = payBaseService.acpQuery(order);
					if (R.ifError(r)) {
						return r;
					}
					msg = (String) r.get(Constant.result_msg);
					orderState = order.getOrderState();
					order.setMsg(msg);
					RedisUtil.setOrderAcp(order);
					
					if(OrderState.succ.id() == order.getOrderState()){
						if(payOrderAcpDao.get(orderNo, merchant.getMerchNo())== null) {
							lock.unlock();
							RedisMsg.orderAcpDataMsg(merchant.getMerchNo(),orderNo);
						}else {
							logger.info("代付接口查询，订单已存在，不发通知保存.{},{}",merchant.getMerchNo(),orderNo);
						}
					}
				}
			} finally {
				if(lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
			Map<String, String> data = PayService.initRspData(order);
			data.put(OrderParamKey.orderState.name(), String.valueOf(orderState));
			data.put(OrderParamKey.businessNo.name(), order.getBusinessNo());
			data.put(OrderParamKey.amount.name(), order.getRealAmount().toString());
			return decryptAndSign(data, merchant.getPublicKey(), msg);
		} else {
			return R.error("查询过于繁忙，请稍后再试！");
		}
	}

	/**
	 * 补单回调
	 * @param orderNO
	 * @return
	 */
	@Override
	public R datetion(String orderNO) {
		logger.info("手动补单回调 ： " + orderNO);
		try {
			Order order = payOrderDao.get(orderNO);
			LinkedHashMap jomap=new LinkedHashMap();
			String code = "1";
			String msg = "支付成功";
			String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
			Merchant merchant = merchantService.get(order.getMerchNo());
			String publicKey = merchant.getPublicKey();
			logger.info("手动补单回调 publicKey ： " + publicKey );
			Integer orderState = order.getOrderState();
			System.out.println("orderState :" + orderState);
			System.out.println("OrderState.succ :" + OrderState.succ);
			System.out.println("OrderState.succ.id :" + OrderState.succ.id());
			if (orderState == OrderState.succ.id()){
				BigDecimal amount = order.getAmount();
				jomap.put("msg",msg);
				jomap.put("amount",amount.toString());
				jomap.put("orderNo",order.getOrderNo());
				jomap.put("code",code);
				jomap.put("notifyTime",reqTime);
				//用户公钥做加签处理
				jomap.put("key",publicKey);
				String text = http_build_query(jomap);
				String Sign = MD5.md5(text);

				jomap.put("sign",Sign);
				//用户公钥不做参数传递
				jomap.remove("key");
				logger.info("手动 支付 回调 用户返回结果 jomap : " + jomap);
				String result = RQPayUtils.sendPost(order.getNotifyUrl(), jomap);
				logger.info("用户返回值 ： " + result);
				order.setNoticeState(1);
				payOrderDao.update(order);
				return R.ok();
			}
			return R.error("手动回调失败,请联系管理员");
		} catch (Exception e){
			return R.error("手动回调失败！");
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
		System.out.println("reString : " + reString);
		reString = reString.replace("%3D", "=").replace("%26", "&").replace("%3A",":").replace("%2F","/");
		return reString;
	}

	/**
	 * @Description 保存提现订单数据
	 * @param order
	 */
	@Transactional
	boolean saveOrderWithdrawData(Order order){

		Integer orderState = order.getOrderState();
		// 商户信息
		Merchant merchant = merchantService.get(order.getMerchNo());
		// 支付通道信息
		PayConfigCompanyDO payCfgComp = payCfgCompService.get(order.getPayCompany(), order.getPayMerch(),
				order.getOutChannel());
		BigDecimal amount = order.getAmount();

		// 成本金额
		if(payCfgComp != null && payCfgComp.getCostRate() != null){
			//费率单位
			Integer costRateUnit = payCfgComp.getCostRateUnit();
			/*if(costRateUnit.equals(PaymentRateUnit.PRECENT.id())) {
				order.setCostAmount(ParamUtil.multBig(amount, payCfgComp.getCostRate().divide(new BigDecimal(100))));
			} else if(costRateUnit.equals(PaymentRateUnit.YUAN.id())) {
				order.setCostAmount(payCfgComp.getCostRate());order.setCostAmount(payCfgComp.getCostRate());
			}*/
			order.setCostAmount(payCfgComp.getCostRate());
		}else{
			order.setCostAmount(BigDecimal.ZERO);
		}
		int crtDate = order.getCrtDate();
		if (ParamUtil.isNotEmpty(order.getMsg()) && order.getMsg().length() > 50) {
			order.setMsg(order.getMsg().substring(0, 50));
		}

		order.setOrderState(OrderState.succ.id());

		Integer userType = order.getUserType();
		if(OrderState.succ.id() == orderState){
			RecordFoundAcctDO rdFoundAcct = null;
			BigDecimal dfAmount = order.getQhAmount();
			System.out.println("提现手续费：----------"+dfAmount);
			System.out.println("提现成本费:-----------"+order.getCostAmount());
			// 增加平台资金账户可用余额以及流水
			rdFoundAcct = payHandlerService.availBalForPlatAdd(order,
					order.getQhAmount(),
					FeeType.platWithdrawIn.id(),OrderType.withdraw.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);

			// 增加平台资金账户余额以及流水
			rdFoundAcct = payHandlerService.balForPlatAdd(order,order.getQhAmount(),
					FeeType.platWithdrawIn.id(),OrderType.withdraw.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);

			// 减去第三方支付公司资金账户可用余额以及流水
			RecordPayMerchBalDO rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getAmount(),
					FeeType.payMerchAcp.id(),OrderType.withdraw.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchAvailBalDao.save(rdPayMerchAcct);

			// 减去第三方支付公司资金账户可用余额以及流水    减手续费
			rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getQhAmount(),
					FeeType.payMerchAcpHand.id(),OrderType.withdraw.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchAvailBalDao.save(rdPayMerchAcct);

			// 减去第三方支付公司资金账户余额以及流水
			rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getAmount(),
					FeeType.payMerchAcp.id(),OrderType.withdraw.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchBalDao.save(rdPayMerchAcct);

			// 减去第三方支付公司资金账户余额以及流水    减手续费
			rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getQhAmount(),
					FeeType.payMerchAcpHand.id(),OrderType.withdraw.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchBalDao.save(rdPayMerchAcct);

			order.setRealAmount(order.getAmount().add(order.getQhAmount()));
			order.setClearState(ClearState.succ.id());
            //查出支行信息，添加到下发msg中
            UserBankDO userDO = userBankService.get(order.getMerchNo(),order.getBankNo());
            order.setMemo(userDO.getBankBranch());
			payOrderAcpDao.update(order);
			return true;
		}else{
			if(UserType.merch.id() == userType){
				List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<>();
				//返还可用余额
				RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(),FeeType.withdrawFail.id(),OrderType.withdraw.id());
				rdMerchAvailBal.setCrtDate(crtDate);
				rdMerchAvailBalList.add(rdMerchAvailBal);

				rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(),FeeType.merchWithDrawHandFee.id(),OrderType.withdraw.id());
				rdMerchAvailBal.setCrtDate(crtDate);
				rdMerchAvailBalList.add(rdMerchAvailBal);

				rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);

				//返还余额
				RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchAdd(order, order.getAmount(),FeeType.withdrawFail.id(),OrderType.withdraw.id());
				rdMerchBal.setCrtDate(crtDate);
				rdMerchBalDao.save(rdMerchBal);

				rdMerchBal = payHandlerService.balForMerchAdd(order, order.getQhAmount(),FeeType.merchWithDrawHandFee.id(),OrderType.withdraw.id());
				rdMerchBal.setCrtDate(crtDate);
				rdMerchBalDao.save(rdMerchBal);

			} else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {

				RecordFoundAcctDO rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getAmount(),order.getMerchNo(), FeeType.withdrawFail.id(), OrderType.withdraw.id());
				rdAgentAvailBal.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdAgentAvailBal);

				rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getQhAmount(),order.getMerchNo(), FeeType.agentWithDrawHandFee.id(), OrderType.withdraw.id());
				rdAgentAvailBal.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdAgentAvailBal);

				rdFoundAcctDao.save(payHandlerService.balForAgentAdd(order, order.getAmount(),order.getMerchNo(), FeeType.withdrawFail.id(),	OrderType.withdraw.id()));
				rdFoundAcctDao.save(payHandlerService.balForAgentAdd(order, order.getQhAmount(),order.getMerchNo(), FeeType.agentWithDrawHandFee.id(),	OrderType.withdraw.id()));

			}
			return true;
		}

	}
	
	/**
	 * @Description 保存代付订单数据
	 * @param order
	 */
	@Transactional
	boolean saveOrderAcpData(Order order) {
		Integer orderState = order.getOrderState();
		// 商户信息
		Merchant merchant = merchantService.get(order.getMerchNo());
		// 支付通道信息
		PayConfigCompanyDO payCfgComp = payCfgCompService.get(order.getPayCompany(), order.getPayMerch(),
				order.getOutChannel());
		BigDecimal amount = order.getAmount();
		//费率单位
		Integer costRateUnit = payCfgComp.getCostRateUnit();
		// 成本金额
		if(payCfgComp.getCostRate() != null){
			if(costRateUnit.equals(PaymentRateUnit.PRECENT.id())) {
				order.setCostAmount(ParamUtil.multBig(amount, payCfgComp.getCostRate().divide(new BigDecimal(100))));
			} else if(costRateUnit.equals(PaymentRateUnit.YUAN.id())) {
				order.setCostAmount(payCfgComp.getCostRate());
			}
		}else{
			order.setCostAmount(BigDecimal.ZERO);
		}
		
		// 商户金额
		Integer jfUnit = null;
		// 代理金额
		BigDecimal feeRate = null;
		Agent agent = agentService.get(merchant.getParentAgent());
		
		BigDecimal agentAmount = BigDecimal.ZERO;
		
		Map<String,String> paidAcpMap = agent.getCoinRate().get(order.getOutChannel());
		feeRate = new BigDecimal(paidAcpMap.get(PayConstants.PAYMENT_RATE).toString());
		jfUnit = Integer.valueOf(paidAcpMap.get(PayConstants.PAYMENT_UNIT).toString());
		if (feeRate != null) {
			if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
				feeRate = feeRate.divide(new BigDecimal(100));
				agentAmount = ParamUtil.multSmall(amount, feeRate);
			}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
				agentAmount = feeRate;
			}
		}
		//上级代理
		String parentAgentNumber = agent.getParentAgent();
		if(agent.getLevel() ==AgentLevel.two.id() ) {
			order.setSubAgentAmount(order.getQhAmount().subtract(agentAmount));
			
			feeRate = null;
			Agent paramAgent = agentService.get(parentAgentNumber);
			paidAcpMap = paramAgent.getCoinRate().get(order.getOutChannel());
			feeRate = new BigDecimal(paidAcpMap.get(PayConstants.PAYMENT_RATE).toString());
			jfUnit = Integer.valueOf(paidAcpMap.get(PayConstants.PAYMENT_UNIT).toString());
			if (feeRate != null) {
				BigDecimal parentAgentAmount = BigDecimal.ZERO;
				if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
					feeRate = feeRate.divide(new BigDecimal(100));
					parentAgentAmount = ParamUtil.multSmall(amount, feeRate);
				}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
					parentAgentAmount = feeRate;
				}
				order.setAgentAmount(agentAmount.subtract(parentAgentAmount));
				agentAmount = parentAgentAmount;
			}
		}else {
			order.setAgentAmount(order.getQhAmount().subtract(agentAmount));
		}
		
		int crtDate = order.getCrtDate();
		if (ParamUtil.isNotEmpty(order.getMsg()) && order.getMsg().length() > 50) {
			order.setMsg(order.getMsg().substring(0, 50));
		}
		payOrderAcpDao.save(order);
		if(OrderState.succ.id() == orderState){
			RecordFoundAcctDO rdFoundAcct = null;
			// 增加代理可用/余额以及流水
			if(agent.getLevel() ==AgentLevel.two.id() ) {
				rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getSubAgentAmount(), agent.getAgentNumber(), FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdFoundAcct);
				
				rdFoundAcct = payHandlerService.balForAgentAdd(order,order.getSubAgentAmount(), agent.getAgentNumber(), FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAcctDao.save(rdFoundAcct);
				
				//上级代理
				rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getAgentAmount(), parentAgentNumber, FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdFoundAcct);
				
				rdFoundAcct = payHandlerService.balForAgentAdd(order,order.getAgentAmount(), parentAgentNumber, FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAcctDao.save(rdFoundAcct);
			}else {
				rdFoundAcct = payHandlerService.availBalForAgentAdd(order,order.getAgentAmount(), agent.getAgentNumber(), FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAvailAcctDao.save(rdFoundAcct);
				
				rdFoundAcct = payHandlerService.balForAgentAdd(order,order.getAgentAmount(), agent.getAgentNumber(), FeeType.agentAcpIn.id(),OrderType.acp.id());
				rdFoundAcct.setCrtDate(crtDate);
				rdFoundAcctDao.save(rdFoundAcct);
			}
			
			// 增加平台资金账户可用余额以及流水
			rdFoundAcct = payHandlerService.availBalForPlatAdd(order,
					order.getQhAmount(),
					FeeType.platAcpIn.id(),OrderType.acp.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAvailAcctDao.save(rdFoundAcct);
			
			// 增加平台资金账户余额以及流水
			rdFoundAcct = payHandlerService.balForPlatAdd(order,order.getQhAmount(),
					FeeType.platAcpIn.id(),OrderType.acp.id());
			rdFoundAcct.setCrtDate(crtDate);
			rdFoundAcctDao.save(rdFoundAcct);
			
			// 减去第三方支付公司资金账户余额以及流水
			RecordPayMerchBalDO rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getAmount(),
					FeeType.payMerchAcp.id(),OrderType.pay.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchBalDao.save(rdPayMerchAcct);
			
			// 减去第三方支付公司资金账户余额以及流水    减手续费
			rdPayMerchAcct = payHandlerService.balForPayMerchSub(order,order.getCostAmount(),
					FeeType.payMerchAcpHand.id(),OrderType.pay.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchBalDao.save(rdPayMerchAcct);
			
			// 减去第三方支付公司资金账户可用余额以及流水
			rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getAmount(),
					FeeType.payMerchAcp.id(),OrderType.pay.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchAvailBalDao.save(rdPayMerchAcct);
			
			// 减去第三方支付公司资金账户可用余额以及流水    减手续费
			rdPayMerchAcct = payHandlerService.availBalForPayMerchSub(order,order.getCostAmount(),
					FeeType.payMerchAcpHand.id(),OrderType.pay.id());
			rdPayMerchAcct.setCrtDate(crtDate);
			rdPayMerchAvailBalDao.save(rdPayMerchAcct);
			return true;
		}else{

			List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<>();
			//返还可用余额
			RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(),FeeType.merchAcpFail.id(),OrderType.acp.id());
			rdMerchAvailBal.setCrtDate(crtDate);
			rdMerchAvailBalList.add(rdMerchAvailBal);
			
			rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(),FeeType.merchAcpHandFee.id(),OrderType.acp.id());
			rdMerchAvailBal.setCrtDate(crtDate);
			rdMerchAvailBalList.add(rdMerchAvailBal);
			
			//返还余额
			RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchAdd(order, order.getAmount(),FeeType.merchAcpFail.id(),OrderType.acp.id());
			rdMerchBal.setCrtDate(crtDate);
			rdMerchBalDao.save(rdMerchBal);
			
			rdMerchBal = payHandlerService.balForMerchAdd(order, order.getQhAmount(),FeeType.merchAcpHandFee.id(),OrderType.acp.id());
			rdMerchBal.setCrtDate(crtDate);
			rdMerchBalDao.save(rdMerchBal);
			
			rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
			
			return true;
		}
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#chargeDataMsg(java.lang.String, java.lang.String)
	 */
	@Override
	public void chargeDataMsg(String merchNo, String orderNo) {
		payQrService.chargeDataMsg(merchNo,orderNo);
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#syncOrder(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public R syncOrder(String merchNo, String orderNo, String businessNo) {
		Merchant merchant = merchantService.get(merchNo);
		if(merchant == null){
			return R.error("商户不存在 " + merchNo);
		}
		RLock lock = RedissonLockUtil.getOrderLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			Order order = null;
			String msg = Constant.result_msg_succ;
			try {
				order = RedisUtil.getOrder(merchNo, orderNo);
				boolean dataFlag = false;
				if (order == null) {
					order = payOrderDao.get(orderNo);
					dataFlag = true;
				}
				if (order == null) {
					return R.error(merchNo + "," + orderNo + "支付订单不存在！");
				}
				/*if(OrderState.succ.id() == order.getOrderState()){
					return R.error(merchNo + "," + orderNo + "订单已同步！");
				}*/
				// 无支付成功结果去第三方查询
				if (!dataFlag && OrderState.succ.id() != order.getOrderState()) {
					R r;
					if(OutChannel.jfDesc().containsKey(order.getOutChannel())){
						r = payQrService.syncOrder(merchant,order,businessNo);
					}else {
						r = payBaseService.query(order);
					}
					if (R.ifError(r)) {
						return r;
					}
					msg = (String) r.get(Constant.result_msg);
					order.setMsg(msg);
					RedisUtil.setOrder(order);
				}
				
				if(!dataFlag && OrderState.succ.id() == order.getOrderState()){
					String result = orderNotify(order,businessNo);
					if(payOrderDao.get(orderNo)== null) {
						if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
							lock.unlock();
							RedisMsg.orderDataMsg(merchNo,orderNo);
						}else{
							if(!RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo)){
								lock.unlock();
								RedisMsg.orderDataMsg(merchNo,orderNo);
							}
						}
					}else {
						logger.info("手动同步，订单已存在，不发通知保存.{},{},通知结果：{}",merchNo,orderNo,result);
					}
				}
				return R.ok("手动同步操作成功,订单状态：" + OrderState.desc().get(order.getOrderState()));
			} finally {
				if(lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
		} else {
			return R.error("同步过于繁忙，请稍后再试！");
		}
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#syncOrderAcp(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public R syncOrderAcp(String merchNo, String orderNo, String businessNo) {
		Merchant merchant = merchantService.get(merchNo);
		if(merchant == null){
			return R.error("商户不存在 " + merchNo);
		}
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			Order order = null;
			String msg = Constant.result_msg_succ;
			try {
				order = RedisUtil.getOrderAcp(merchNo, orderNo);
				boolean dataFlag = false;
				if (order == null) {
					order = payOrderAcpDao.get(orderNo, merchNo);
					dataFlag = true;
				}
				if (order == null) {
					return R.error(merchNo + "," + orderNo + "支付订单不存在！");
				}
				// 无支付成功结果去第三方查询
				if (!dataFlag && OrderState.succ.id() != order.getOrderState()) {
					R r = payBaseService.acpQuery(order);
					if (R.ifError(r)) {
						return r;
					}
					msg = (String) r.get(Constant.result_msg);
					order.setMsg(msg);
					RedisUtil.setOrderAcp(order);
				}
				//提现类型的订单直接 手动同步返回
				if(Integer.valueOf(OrderType.withdraw.id()).equals(order.getOrderType())){
					lock.unlock();
					RedisMsg.orderAcpDataMsg(merchNo,orderNo);
					return R.ok("手动同步操作成功,订单状态：" + OrderState.desc().get(order.getOrderState()));
				}
				if(!dataFlag && OrderState.succ.id() == order.getOrderState()){
					String result = orderAcpNotify(order,businessNo);
					if(payOrderDao.get(orderNo)== null) {
						if(Constant.result_msg_succ.equalsIgnoreCase(result) || Constant.result_msg_ok.equalsIgnoreCase(result)){
							lock.unlock();
							RedisMsg.orderAcpDataMsg(merchNo,orderNo);
						}else{
							if(!RedisUtil.setKeyEventExpired(RedisConstants.cache_keyevent_ord, merchNo, orderNo)){
								lock.unlock();
								RedisMsg.orderAcpDataMsg(merchNo,orderNo);
							}
						}
					}else {
						logger.info("手动同步，订单已存在，不发通知保存.{},{},通知结果：{}",merchNo,orderNo,result);
					}
				}
				return R.ok("手动同步操作成功,订单状态：" + OrderState.desc().get(order.getOrderState()));
			} finally {
				if(lock.isHeldByCurrentThread()) {
					lock.unlock();
				}
			}
		} else {
			return R.error("同步过于繁忙，请稍后再试！");
		}
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#withdraw(com.qh.pay.api.Order)
	 */
	@Override
	public R withdraw(Order order) {
		String merchNo = order.getMerchNo();
		String orderNo = ParamUtil.getOrderId();
		order.setOrderNo(orderNo);
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo + RedisConstants.link_symbol + orderNo);
		if (lock.tryLock()) {
			try {
				if(RedisUtil.getOrderAcp(merchNo, orderNo) != null){
					return R.error("系统繁忙，请稍后再试");
				}
				// 检查提现订单信息
				String checkResult = payHandlerService.checkUserWithDrawOrder(order);
				order.setOrderType(OrderType.withdraw.id());
				if (ParamUtil.isNotEmpty(checkResult)) {
					logger.error(checkResult);
					return R.error(checkResult);
				}
				/*R r = checkAcpCfgComp(order);
				if(R.ifError(r)){
					return r;
				}*/
				//保存银联信息
				userBankService.save(order.getMobile(),merchNo, order);
				
				order.setUserId(merchNo);
				Integer userType = order.getUserType();
				BigDecimal amount = order.getAmount();
				PayAcctBal pab = null;
				Integer crtDate = null;
				Agent agent = null;
				List<RecordMerchBalDO> rdMerchAvailBalList = new ArrayList<RecordMerchBalDO>();
				List<RecordFoundAcctDO> rdFoundAvailBalList = new ArrayList<RecordFoundAcctDO>();
				if(UserType.merch.id() == userType){
					// 检查商户余额 -----》商户可用余额
					RLock merchLock = RedissonLockUtil.getBalMerchLock(merchNo);
					try {
						merchLock.lock();
						// 商户信息
						Merchant merchant = merchantService.get(merchNo);
						//计算出  商户代付  手续费费率
						Map<String,String> paidMap = merchant.getCoinRate().get(OutChannel.acp.name());
						BigDecimal jfRate = new BigDecimal(paidMap.get(PayConstants.PAYMENT_RATE).toString());
						//Integer jfUnit = Integer.valueOf(paidMap.get(PayConstants.PAYMENT_UNIT).toString());
						/*if(jfRate != null){
							if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
								jfRate = jfRate.divide(new BigDecimal(100));
								order.setQhAmount(ParamUtil.multBig(amount, jfRate));
							}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
								order.setQhAmount(jfRate);
							}
						}*/
						order.setQhAmount(jfRate);
						RecordMerchBalDO rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.withdrawOut.id(),
								OrderType.withdraw.id(), ProfitLoss.loss.id());
						rdMerchAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
						pab = RedisUtil.getMerchBal(merchNo);
						if (pab == null || pab.getAvailBal().compareTo(order.getAmount().add(order.getQhAmount())) < 0) {
							return R.error("商户号 " + merchNo + ",可用余额不足！");
						}
						rdMerchAvailBal.setTranAmt(order.getAmount());
						if(rdMerchAvailBal.getTranAmt().compareTo(BigDecimal.ZERO) < 0) {
							return R.error("商户号 " + merchNo + ",提现金额不能小于：" + order.getQhAmount());
						}
						payHandlerService.availBalForMerchSub(order, rdMerchAvailBal, pab);
						RedisUtil.setMerchBal(pab);
						rdMerchAvailBalList.add(rdMerchAvailBal);
						
						//扣除商户提现手续费
						rdMerchAvailBal = PayService.initRdMerchBal(order, FeeType.merchWithDrawHandFee.id(),
								OrderType.withdraw.id(), ProfitLoss.loss.id());
						rdMerchAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
						rdMerchAvailBal.setTranAmt(order.getQhAmount());
						payHandlerService.availBalForMerchSub(order, rdMerchAvailBal, pab);
						RedisUtil.setMerchBal(pab);
						rdMerchAvailBalList.add(rdMerchAvailBal);
						crtDate = rdMerchAvailBal.getCrtDate();

					} finally {
						merchLock.unlock();
					}
				}else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
					pab = RedisUtil.getAgentBal(merchNo);
					agent = agentService.get(merchNo);
					Map<String,String> paidAcpMap = agent.getCoinRate().get(OutChannel.acp.name());
					BigDecimal feeRate = new BigDecimal(paidAcpMap.get(PayConstants.PAYMENT_RATE).toString());
					/*Integer jfUnit = Integer.valueOf(paidAcpMap.get(PayConstants.PAYMENT_UNIT).toString());
					if (feeRate != null) {
						if(jfUnit.equals(PaymentRateUnit.PRECENT.id())) {
							feeRate = feeRate.divide(new BigDecimal(100));
							order.setQhAmount(ParamUtil.multSmall(amount, feeRate));
						}else if(jfUnit.equals(PaymentRateUnit.YUAN.id())) {
							order.setQhAmount(feeRate);
						}
					}*/
					order.setQhAmount(feeRate);
					RecordFoundAcctDO rdAgentAvailBal = PayService.initRdFoundAcct(order, FeeType.withdrawOut.id(), OrderType.withdraw.id(), ProfitLoss.loss.id());
					rdAgentAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
					pab = RedisUtil.getAgentBal(agent.getAgentNumber());
					if (pab == null || pab.getAvailBal().compareTo(order.getAmount().add(order.getQhAmount())) < 0) {
						return R.error("代理 " + merchNo + ",可用余额不足！");
					}
					rdAgentAvailBal.setTranAmt(order.getAmount());
					if(rdAgentAvailBal.getTranAmt().compareTo(BigDecimal.ZERO) < 0) {
						return R.error("商户号 " + merchNo + ",提现金额不能小于：" + order.getQhAmount());
					}
					rdAgentAvailBal.setUsername(agent.getAgentNumber());
					payHandlerService.availBalForAgentSub(order, rdAgentAvailBal, pab);
					RedisUtil.setAgentBal(pab);;
					rdFoundAvailBalList.add(rdAgentAvailBal);
					//扣除商户提现手续费
					rdAgentAvailBal = PayService.initRdFoundAcct(order, FeeType.agentWithDrawHandFee.id(),
							OrderType.withdraw.id(), ProfitLoss.loss.id());
					rdAgentAvailBal.setCrtDate(DateUtil.getCurrentTimeInt());
					rdAgentAvailBal.setTranAmt(order.getQhAmount());
					payHandlerService.availBalForAgentSub(order, rdAgentAvailBal, pab);
					RedisUtil.setAgentBal(pab);
					rdFoundAvailBalList.add(rdAgentAvailBal);
					crtDate = rdAgentAvailBal.getCrtDate();
				}
				order.setAmount(order.getAmount());
				PayAuditDO payAudit = PayService.initPayAudit(order, AuditType.order_withdraw.id());
				int count = 0;
				try {
					payAudit.setCrtTime(crtDate);
					count = payAuditDao.save(payAudit);
				} catch (Exception e) {
					count = 0;
				}
				try {
					if(rdMerchAvailBalList.size() > 0 ) {
						//保存商户可用余额流水
						rdMerchAvailBalDao.saveBatch(rdMerchAvailBalList);
					}
					if(rdFoundAvailBalList.size() > 0 ) {
						//保存代理可用余额流水
						rdFoundAvailAcctDao.saveBatch(rdFoundAvailBalList);
					}
					
				} catch (Exception e) {
					logger.error("提现保存可用余额流水记录失败！{}，{}",merchNo,orderNo);
				}
				if (count == 1) {
					RedisUtil.setOrderAcp(order);
					payOrderAcpDao.save(order);
					//如果成功，商户余额减少
					try {
						if(UserType.merch.id() == userType){
							rdMerchBalDao.save(payHandlerService.balForMerchSub(order, order.getAmount(), FeeType.withdrawOut.id(),	OrderType.withdraw.id()));
							rdMerchBalDao.save(payHandlerService.balForMerchSub(order, order.getQhAmount(), FeeType.merchWithDrawHandFee.id(),	OrderType.withdraw.id()));
						}else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
							rdFoundAcctDao.save(payHandlerService.balForAgentSub(order, order.getAmount(),agent.getAgentNumber(), FeeType.withdrawOut.id(),	OrderType.withdraw.id()));
							rdFoundAcctDao.save(payHandlerService.balForAgentSub(order, order.getQhAmount(),agent.getAgentNumber(), FeeType.agentWithDrawHandFee.id(),	OrderType.withdraw.id()));
						}
					} catch (Exception e) {
						logger.error("代付保存余额流水记录失败！{}，{}",merchNo,orderNo);
					}
					Map<String, String> data = PayService.initRspData(order);
					data.put(OrderParamKey.orderState.name(), String.valueOf(OrderState.init.id()));
					return R.okData(data).put(Constant.result_msg, "提现请求已受理!");
				} else {// 出错了返还可用余额
					if(UserType.merch.id() == userType){
						RecordMerchBalDO rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getAmount(), FeeType.withdrawFail.id(), OrderType.withdraw.id());
						rdMerchAvailBal.setCrtDate(payAudit.getCrtTime());
						rdMerchAvailBalDao.save(rdMerchAvailBal);
						
						rdMerchAvailBal = payHandlerService.availBalForMerchAdd(order, order.getQhAmount(), FeeType.merchWithDrawHandFee.id(), OrderType.withdraw.id());
						rdMerchAvailBal.setCrtDate(payAudit.getCrtTime());
						rdMerchAvailBalDao.save(rdMerchAvailBal);
					}else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
						RecordFoundAcctDO rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getAmount(),agent.getAgentNumber(), FeeType.withdrawFail.id(), OrderType.withdraw.id());
						rdAgentAvailBal.setCrtDate(payAudit.getCrtTime());
						rdFoundAvailAcctDao.save(rdAgentAvailBal);
						
						rdAgentAvailBal = payHandlerService.availBalForAgentAdd(order, order.getQhAmount(),agent.getAgentNumber(), FeeType.agentWithDrawHandFee.id(), OrderType.withdraw.id());
						rdAgentAvailBal.setCrtDate(payAudit.getCrtTime());
						rdFoundAvailAcctDao.save(rdAgentAvailBal);
					}
					return R.error("代付异常！");
				}
			} finally {
				lock.unlock();
			}
		} else {
			return R.error("系统繁忙，请稍后再试");
		}
	}


	/**
	 * @Description 
	 * @param order
	 */
	private R checkAcpCfgComp(Order order) {
		R r = checkCfgCompDF(order);
		if(R.ifError(r)){
			return r;
		}
		//如果满足需要行号的支付公司则需要传递银联行号参数
		if(Integer.valueOf(YesNoType.yes.id()).equals(PayCompany.companyUnionPay(order.getPayCompany())) &&(
				ParamUtil.isEmpty(order.getUnionpayNo()) || ParamUtil.isEmpty(order.getBankBranch()))){
			return R.error("未找到银联行号信息参数");
		}
		return R.ok();
	}
	
	/**
	 * @Description 代付通道 选择
	 * @param order
	 * @return
	 */
	private R checkCfgCompDF(Order order) {

		String payCompany="";
		String payMerch="";
		String callbackDomain = "";
		Merchant merchant = merchantService.get(order.getMerchNo());
//		Map<String,String> paidChannelMap = new HashMap<>();
//		//如果为商户指定了通道，则直接使用指定通道
//		payCompany = order.getPayCompany();
//		payMerch = order.getPayMerch();

		Map<String,String> payCompanys=merchant.getPayCompany();
		System.out.println(payCompanys.toString());
		String company=payCompanys.get(order.getOutChannel());
		if(company!=null){
			PayConfigCompanyDO payCfgComp=payCfgCompService.getByCompany(company,order.getOutChannel());
			if(payCfgComp==null){
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未找到相应的支付通道");
			}
			String payPeriod=payCfgComp.getPayPeriod();
			if(payPeriod!=null&& !(payPeriod.equals(""))) {
				SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
				Date currentTime = new Date();
				String dateString = formatter.format(currentTime);
				//将当前时间转换为int类型
				Integer ss = DateUtil.timeToInt(dateString);
				String[] strings=payPeriod.split(",");
				for (int i=0;i<strings.length;i++){
					System.out.println("strings[i]=============>"+strings[i]);
					String[] split = strings[i].split("-");
					logger.info("时间 ： " + split[0]);
					Integer f = Integer.parseInt(split[0]);
					Integer l = Integer.parseInt(split[1]);
					if (ss < f || ss > l) {
						return R.error(order.getMerchNo() + "," + order.getOutChannel() + "代付通道的代付时间为" + DateUtil.intFormatToTime(payCfgComp.getPayPeriod()));
					}
				}
			}
			if(payCfgComp.getIfClose()!= 0) {
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "通道关闭！");
			}
			payCompany=payCfgComp.getCompany();
			payMerch=payCfgComp.getPayMerch();
			callbackDomain = payCfgComp.getCallbackDomain();
		}else {
			payCompany = "yinshengbao";
			payMerch = "yinshengbao001";
			Integer mPayChannelType = merchant.getPayChannelType();
			//判断商户是否有分配  通道分类   没有分配不支持支付
			if(mPayChannelType == null || !PayChannelType.desc().containsKey(mPayChannelType)) {
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未分配支付通道");
			}
			List<Object> payCfgComps = payCfgCompService.getPayCfgCompByOutChannel(order.getOutChannel());
			if (payCfgComps == null || payCfgComps.size() == 0) {
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "通道配置错误！");
			}
			List<PayConfigCompanyDO> pccList = new ArrayList<PayConfigCompanyDO>();
			PayConfigCompanyDO payCfgComp = null;
			for (Object object : payCfgComps) {
				payCfgComp = (PayConfigCompanyDO) object;
				if(payCfgComp.getIfClose() == 0){
					Integer cPayChannelType = payCfgComp.getPayChannelType();
					if(cPayChannelType != null && cPayChannelType.equals(mPayChannelType)) {
						//将与商户  同类的通道 找出来
						pccList.add(payCfgComp);
					}
				}
			}
			if(pccList.size() <= 0) {
				//当前商户没有 同类通道
				return R.error(order.getMerchNo() + "," + order.getOutChannel() + "未找到相应的支付通道");
			}
			payCfgComp = null;
			BigDecimal amount = order.getAmount();
			BigDecimal qhAmount = order.getQhAmount();
			amount = amount.add(qhAmount);
			PayAcctBal pab = RedisUtil.getMerchBal(order.getMerchNo());
			Map<String, BigDecimal> companyPayAvailBal = pab.getCompanyPayAvailBal();
			if(companyPayAvailBal!=null) {
				for (PayConfigCompanyDO payConfigCompanyDO : pccList) {
					String payCompanyName = payConfigCompanyDO.getCompany();
					BigDecimal singleAvailBal = companyPayAvailBal.get(payCompanyName + RedisConstants.link_symbol +  payConfigCompanyDO.getPayMerch());
					logger.info("{}商户在支付公司{}下,商户号为{}的可用余额剩余:{},商户此次支出金额为:{}",order.getMerchNo(),payCompanyName,payConfigCompanyDO.getPayMerch(),singleAvailBal,amount);
					if(singleAvailBal != null && amount.compareTo(singleAvailBal) <= 0) {
						//如果订单金额小于当前商户在当前支付公司交易的可用余额， 则使用此通道
						payCfgComp = payConfigCompanyDO;
						break;
					}
				}
			}
			//TODO 如果上面的代码调试通过则不需要下面的4行代码
			payCfgComp = pccList.get(0);
			if(payCfgComp == null){
				return R.error(order.getMerchNo() +  "未找到相应的支付通道或者可用余额不足");
			}
			Integer maxPayAmt = payCfgComp.getMaxPayAmt();
			Integer minPayAmt = payCfgComp.getMinPayAmt();
			if(maxPayAmt != null && new BigDecimal(maxPayAmt).compareTo(order.getAmount()) == -1){
				return R.error("单笔最高限额:"+maxPayAmt+"元");
			}
			if(minPayAmt != null && new BigDecimal(minPayAmt).compareTo(order.getAmount()) == 1){
				return R.error("单笔最低限额为:"+minPayAmt+"元");
			}
			payCompany = payCfgComp.getCompany();
			payMerch = payCfgComp.getPayMerch();
			callbackDomain = payCfgComp.getCallbackDomain();
		}
		order.setPayCompany(payCompany);
		order.setPayMerch(payMerch);
		order.setCallbackDomain(callbackDomain);
		return R.ok();
	}


	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.PayService#offlineTransfer(java.lang.String, java.lang.String, java.lang.Integer)
	 */
	@Override
	public R offlineTransfer(String orderNo, String merchNo, Integer auditType) {
		RLock lock = RedissonLockUtil.getOrderAcpLock(merchNo,orderNo);
		if (lock.tryLock()) {
			try {
				Order order = RedisUtil.getOrderAcp(merchNo,orderNo);
				if (order == null) {
					logger.info("代付订单数据保存失败，订单不存在，{}，{}",merchNo,orderNo);
					return R.error("订单不存在");
				}
				if(!Integer.valueOf(OrderType.withdraw.id()).equals(order.getOrderType())){
					return R.error("只有提现类订单才支持线下转账");
				}
				Integer orderState = order.getOrderState();
				if (orderState == OrderState.init.id()) {
					order.setOrderState(OrderState.succ.id());
					order.setRealAmount(order.getAmount());
					if (this.saveOrderWithdrawData(order)) {
						RedisUtil.removeOrderAcp(merchNo,orderNo);
						logger.info("代付订单数据保存成功，{}，{}",merchNo,orderNo);
						return R.ok("线下转账成功");
					}
					return R.ok("线下转账失败");
				}else{
					logger.info("代付订单数据保存失败，订单状态'{}'不满足条件，{}，{}",orderState,merchNo,orderNo);
					return R.ok("线下转账失败，订单状态不满足条件");
				}
			} finally {
				lock.unlock();
			}
		}else{
			return R.error("系统繁忙，请稍后再试");
		}
	}

}
