package com.qh.paythird.impl;


import com.qh.common.utils.R;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.PayCompany;
import com.qh.pay.domain.MerchUserSignDO;
import com.qh.paythird.BC.BCService;
import com.qh.paythird.JinDuoDuo.JddService;
import com.qh.paythird.PayBaseService;
import com.qh.paythird.VNET.VentService;

import com.qh.paythird.fangkuai.FangKuaiService;

import com.qh.paythird.taiShan.TaiShanService;
import com.qh.paythird.test.ZhiFuService;

import com.qh.paythird.wanqiuqiu.WanQiuQiuService;
import com.qh.paythird.weiSao.WeiSaoService;
import com.qh.paythird.xiongMao.XiongMaoService;
import com.qh.paythird.ysb.YinShengBaoService;
import com.qh.paythird.yunpay.YunPay;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @ClassName PayBaseServiceImpl
 * @Description 对接支付基础类
 * @Date 2017年11月8日 下午5:21:06
 * @version 1.0.0
 */
@Service
public class PayBaseServiceImpl implements PayBaseService{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayBaseServiceImpl.class);

	@Autowired
	private YinShengBaoService yinShengBaoService;
	@Autowired
	private ZhiFuService zhiFuTService;
	@Autowired
	private XiongMaoService xiongMaoService;
	@Autowired
	private VentService ventService;
	@Autowired
	private YunPay yunPay;
	@Autowired
	private JddService jddService;
	@Autowired
	private TaiShanService taiShanService;
	@Autowired
	private FangKuaiService fangKuaiService;
	@Autowired
	private WeiSaoService weiSaoService;
	@Autowired
	private WanQiuQiuService wanQiuQiuService;
	@Autowired
	private BCService bcService;

//	@Autowired
//	private ShangYinService shangYinService;
	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#order(com.qh.pay.domain.PayConfigCompanyDO, com.qh.pay.api.Order)
	 */
	@Override
	public R order(Order order) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		logger.info("order.getPayCompany() : " + order.getPayCompany());
		logger.info("对接支付 company : " + company);
		if(company == null){
			logger.error("下单未找到支付公司！");
			result = R.error("下单未找到支付公司！");
			return result;
		}
		switch (company) {
			case mofang:
				result = yunPay.order(order);
				break;
			case ysb:
				result = yinShengBaoService.order(order);
				break;
			case Vnet:
				result=ventService.order(order);
				//result=wanQiuQiuService.order(order);
				//result = yinShengBaoService.order(order);
				break;
				//线上
            case taishan:
				result = taiShanService.order(order);
				break;
				//本地
			case fangkuai:
				result = fangKuaiService.order(order);
				break;
			case Weisao:
				result = weiSaoService.order(order);
				break;
			case xiongmao:
				result = xiongMaoService.order(order);
				//result=wanQiuQiuService.order(order);
				break;
			case BC:
				result = bcService.order(order);
				break;
			case wqq:
				result=wanQiuQiuService.order(order);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#notify(com.qh.pay.api.Order, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public R notify(Order order, HttpServletRequest request, String requestBody) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		logger.info("company:"+company);
		if(company == null){
			logger.error("下单回调未找到支付公司！");
			result = R.error("下单回调未找到支付公司！");
			return result;
		}
		logger.info("requestBody:"+requestBody);
		switch (company) {
			case mofang:
				result = yunPay.notify(order,request);
				break;
			case Vnet:
				//result = wanQiuQiuService.notify(order,request);
				result = ventService.notify(order,request);
				break;
			case ysb:
				result = yinShengBaoService.notify(order,request,requestBody);
				break;
			case taishan:
                result = taiShanService.notify(order,request);
				break;
			case fangkuai:
				result=fangKuaiService.notify(order,request);
				break;
			case Weisao:
				result = weiSaoService.notify(order,request);
				break;
			case xiongmao:
				result = xiongMaoService.notify(order,request,requestBody);
				break;
			case BC:
				result = bcService.notify(order,request);
				break;
			case wqq:
				result = wanQiuQiuService.notify(order,request);
				break;
			default:
				logger.error("下单回调未找到支付公司！");
				result = R.error("下单回调未找到支付公司！");
				break;
		}
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#query(com.qh.pay.api.Order)
	 */
	@Override
	public R query(Order order) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		switch (company) {
			case ysb:
				result = yinShengBaoService.query(order);
				break;
			case wqq:
				result=wanQiuQiuService.query(order);
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;

	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#orderAcp(com.qh.pay.api.Order)
	 */
	@Override
	public R orderAcp(Order order) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("代付未找到支付公司！"+order.getPayCompany());
			result = R.error("代付未找到支付公司！");
			return result;
		}
		switch (company) {
			case Vnet:
				result=wanQiuQiuService.acp(order,"");
				break;
			case wqq:
				result=wanQiuQiuService.acp(order,"");
				break;
			case ysb:
				result = yinShengBaoService.order(order);
				break;
			default:
				logger.error("代付未找到支付公司！");
				result = R.error("代付未找到支付公司！");
				break;
		}
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#notifyAcp(com.qh.pay.api.Order, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	@Override
	public R notifyAcp(Order order, HttpServletRequest request, String requestBody) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		switch (company) {
			case Vnet:
				result=wanQiuQiuService.acp_notify(order,request);
			case wqq:
				result=wanQiuQiuService.acp_notify(order,request);
				break;
			case ysb:
				result = yinShengBaoService.notify_acp(order,request,requestBody);
				break;
			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#acpQuery(com.qh.pay.api.Order)
	 */
	@Override
	public R acpQuery(Order order) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		switch (company) {

			case ysb:
				result = yinShengBaoService.query(order);
				break;

			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#refreshBanks(com.qh.pay.api.Order, java.utils.List, java.utils.List)
	 */
	@Override
	public void refreshBanks(Order order, List<String> bank_savings, List<String> bank_credits) {
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			return;
		}
		switch (company) {

			default:
				logger.error("未找到支付公司！");
				break;
		}

	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#cardBind(com.qh.pay.api.Order, com.qh.pay.domain.MerchUserSignDO)
	 */
	@Override
	public R cardBind(Order order, MerchUserSignDO userSign) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		switch (company) {

			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.pay.service.third.PayBaseService#cardBindConfirm(com.qh.pay.api.Order, com.qh.pay.domain.MerchUserSignDO)
	 */
	@Override
	public R cardBindConfirm(Order order, MerchUserSignDO userSign) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		switch (company) {

			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.qh.paythird.PayBaseService#cardMsgResend(com.qh.pay.api.Order, java.lang.Integer)
	 */
	@Override
	public R cardMsgResend(Order order, Integer sendType) {
		R result = null;
		PayCompany company = PayCompany.payCompany(order.getPayCompany());
		if(company == null){
			logger.error("未找到支付公司！");
			result = R.error("未找到支付公司！");
			return result;
		}
		switch (company) {

			default:
				logger.error("未找到支付公司！");
				result = R.error("未找到支付公司！");
				break;
		}
		return result;
	}

}
