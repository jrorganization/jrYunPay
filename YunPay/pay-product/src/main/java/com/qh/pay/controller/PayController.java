package com.qh.pay.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qh.common.config.Constant;
import com.qh.common.controller.BaseController;
import com.qh.common.domain.UserBankDO;
import com.qh.common.service.LocationService;
import com.qh.common.service.UserBankService;
import com.qh.common.utils.R;
import com.qh.common.utils.ShiroUtils;
import com.qh.moneyacct.querydao.RcMerchBalDao;
import com.qh.pay.api.Order;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.*;
import com.qh.pay.api.utils.*;
import com.qh.pay.dao.RecordMerchAvailBalDao;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayService;
import com.qh.paythird.VNET.utils.MD5;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.UserDO;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 * @ClassName PayController
 * @Description pay
 * @Date 2017年10月24日 上午11:30:22
 * @version 1.0.0
 */
@RestController
@RequestMapping("/pay")
public class PayController  extends BaseController{

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayController.class);
    @Autowired
    private MerchantService merchantService;
    @Autowired
	private AgentService agentService;
    @Autowired
    private PayService payService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private UserBankService userBankService;
	@Autowired
	private RcMerchBalDao rcMerchantDao;
	@Autowired
	private RecordMerchAvailBalDao rdMerchAvailBalDao;

	public final static String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCRATDdlWIjeXHlqVpn2CiwMWtkTJRO998UPlYoGw9cS7ZJVU8tChVKinUbp2HCMdpGHsdnAw43ixw49u0K+mQCyo2/Y6HURNwclvOIWt8rBoH3FmjlrnkmC8lyD8ULtvGUK236O0jP430tzdCbaQpRUPqPEH5pWK4TQUx3pcfkPQIDAQAB";

	public final static String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJEBMN2VYiN5ceWpWmfYKLAxa2RMlE733xQ+VigbD1xLtklVTy0KFUqKdRunYcIx2kYex2cDDjeLHDj27Qr6ZALKjb9jodRE3ByW84ha3ysGgfcWaOWueSYLyXIPxQu28ZQrbfo7SM/jfS3N0JtpClFQ+o8QfmlYrhNBTHelx+Q9AgMBAAECgYBQw08KO6e7hmrPtbGq4RRYfk4IQTbhfOF9DvNBx0CY8XqIAcHlnhYZvGBZlLK4TLf8EiyRzXvfp9WCTNXeJXQJ2dQV1pHRunq9jIn/KZJRb52jAkQ0qx82SPEsYPJCbjp+dZsT06JCvbcd3iTZi22g1RQRHOwz4iSAq1hvhjCr6QJBANjS8/maKXQMfCMcNJDXyM+hawzNuz3JgCtHJZE3cf/jeoWqgmLswiJ51/TmGAJbCSsEvRxDi4IGMo6DmfEayBcCQQCrNEgUCPvRPhms+YpT5nTCDkwpMxh9pccBveqvklwe5tlOgdPXQDjmaLVuEfjtpARBIzV+ey/guWQryNwk+NbLAkEAj/giv825ULEpjDaiQLrHP/aymiHQ/knZrOLk8vOZ4ostQ6vgP8dtcG7vElHmB0pjYAkZeLbw3zk2QKLpiMp7qQJATqjrwQDLqjytEVNp4diNpqdpCLjoNLqZL8yxak+FsdEA4Ng3m7tvKTXMvjDVvWHRbpgduOoiek7TnmZf90C5dwJBAKU0f1QcHh6pIRCXFGEzW4sMzgJhQi+6zSa1ggEoMnN/19eAyVJG12VISbOvdTU8TtwsrSIikNQ3/dkh2nH80UA=";



	@GetMapping("/merchant/{merchNo}")
    public Merchant findByMerchNo(@PathVariable String merchNo){
        return merchantService.get(merchNo);
    }
    
    /**
     * 
     * @Description 支付下单
     * @param request
     * @return
     */
    @PostMapping("/order")
    public Object order(HttpServletRequest request)throws Exception{
    	R r =  commDataCheck(request);
    	if(R.ifSucc(r)){
    		return payService.order((Merchant)r.get(Constant.param_merch), (JSONObject)r.get(Constant.param_jsonData));
    	}else{
			return r;
		}
    }

    /**
     * 
     * @Description 代付下单
     * @param request
     * @return
     */
    @PostMapping("/order/acp")
    public Object orderAcp(HttpServletRequest request)throws Exception{
    	R r =  commDataCheck(request);
    	if(R.ifSucc(r)){
    		Merchant merchant = (Merchant)r.get(Constant.param_merch);
			return payService.orderAcp(merchant, (JSONObject)r.get(Constant.param_jsonData));
    	}else{
    		return r;
    	}
    }


	/**
	 *
	 * @Description 代付余额查询
	 * @param request
	 * @return
	 */
	@PostMapping("/query/{merchNo}")
	public Object queryAmount(HttpServletRequest request,@PathVariable("merchNo") String merchNo)throws Exception{
		Merchant merchant=merchantService.get(merchNo);
		String publicKey=merchant.getPublicKey();
		JSONObject jsonObject =  RequestUtils.getJsonResultStream(request);
		System.out.println("jsonObject : " + jsonObject);
		if(jsonObject == null){
			logger.info("获取请求参数失败");
			return R.error("请检查请求参数！");
		}
		String sign = jsonObject.getString("sign");
		logger.info("请求签名：{}",sign);
		if(ParamUtil.isEmpty(sign)){
			logger.info("获取sign失败，参数为:" + jsonObject.toJSONString());
			return R.error("请检查签名参数！");
		}
		TreeMap map=new TreeMap();
		map.put("merchNo",jsonObject.get("merchNo"));
		map.put("date",jsonObject.get("date"));
        String s=http_build_query(map);
        s+="&key="+merchant.getPublicKey();
        String sign1=MD5.md5(s);
		System.out.println("sign1="+sign1);
        if(sign1.equals(sign)){
            return payService.queryAmount(merchant);
        }else {
            logger.error("验签失败！" + merchNo);
            return R.error("验签失败");
        }
	}



	/**
     * 
     * @Description 支付查询
     * @param request
     * @return
     */
    @PostMapping("/order/query")
    public Object query(HttpServletRequest request)throws Exception{
    	R r =  commDataCheck(request);
    	if(R.ifSucc(r)){
    		return payService.query((Merchant)r.get(Constant.param_merch), (JSONObject)r.get(Constant.param_jsonData));
    	}else{
    		return r;
    	}
    }

	/**
	 * @Description 通用检查方法
	 * @param request
	 * @return
	 */
	private R commDataCheck(HttpServletRequest request)throws Exception {
		try {
			System.out.println("request : " + request);
			JSONObject jsonObject =  RequestUtils.getJsonResultStream(request);
			System.out.println("jsonObject : " + jsonObject);
			if(jsonObject == null){
				logger.info("获取请求参数失败");
				return R.error("请检查请求参数！");
			}
			String sign = jsonObject.getString("sign");
			logger.info("请求签名：{}",sign);
			if(ParamUtil.isEmpty(sign)){
				logger.info("获取sign失败，参数为:" + jsonObject.toJSONString());
				return R.error("请检查签名参数！");
			}
			byte[] context = jsonObject.getBytes("context");

//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			String sourcesStr = jsonObject.getString("context");
			System.out.println("sourcesStr : " + sourcesStr);
			byte[] sources = RSAUtil.decryptByPrivateKey(Base64Utils.decode(sourcesStr),privateKey);
			String source = new String(sources);
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
			logger.info("请求加密结果：{}", context);
			if(ParamUtil.isEmpty(sources)){
				logger.info("获取context失败，参数为:" + jsonObject.toJSONString());
				return R.error("请检查加密内容！");
			}
			logger.info("请求加密结果：{}", source);
			try {
				//解密
//			String source = new String(context);
				logger.info("解密结果！" + source);
				JSONObject jo = JSON.parseObject(source);
				String merchNo = jo.getString(OrderParamKey.merchNo.name());
				logger.info("merchNo : " +merchNo );
				if(ParamUtil.isEmpty(merchNo)){
					logger.error("商户号为空！" + source);
					return R.error("商户号为空！" + source);
				}
				Merchant merchant = merchantService.get(merchNo);
				logger.info("merchant : " + merchant.toString());
				if(merchant == null){
					logger.error("商户不存在！" + merchNo);
					return R.error("商户不存在！" + merchNo);
				}
				if(!merchant.getStatus().equals(YesNoType.yes.id())) {
					logger.error("商户被禁用！" + merchNo);
					return R.error("商户异常！" + merchNo);
				}
				Agent agent = agentService.get(merchant.getParentAgent());
				if(agent==null || !agent.getStatus().equals(YesNoType.yes.id())) {
					logger.error("商户上级代理被禁用！" + merchNo);
					return R.error("商户异常!");
				}else {
					if(agent.getLevel().equals(AgentLevel.two.id())) {
						agent = agentService.get(agent.getParentAgent());
						if(agent==null || !agent.getStatus().equals(YesNoType.yes.id())) {
							logger.error("商户上级一级代理被禁用！" + merchNo);
							return R.error("商户异常!");
						}
					}
				}

//				String privateKey = merchantService.getPrivateKey(merchNo);
				String publicKey = merchant.getPublicKey();
				logger.info("publicKey :" + publicKey);
				if(Md5Util.verify(sourcesStr, sign, publicKey, "UTF-8")){
					logger.info("验签成功！", merchant.getPublicKey());
					jo.put(OrderParamKey.reqIp.name(), ParamUtil.getIpAddr(request));
					logger.info("OrderParamKey.reqIp.name() : " + OrderParamKey.reqIp.name());
					return R.ok().put(Constant.param_merch, merchant).put(Constant.param_jsonData, jo);
				}else{
					logger.error("验签失败！" + merchNo);
					return R.error("验签失败！" + merchNo);
				}

			} catch (Exception e) {
				logger.info(e.getMessage(),e);
				return R.error("支付异常！" + e.getMessage());
			}
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			return R.error("支付异常！" + e.getMessage());
		}
	}
	
	
	/**
	 * 
	 * @Description 同步订单信息
	 * @param outChannel
	 * @param merchNo
	 * @param orderNo
	 * @param businessNo
	 * @return
	 */
	@GetMapping("/syncOrder/{outChannel}/{merchNo}/{orderNo}")
	@RequiresPermissions("pay:syncOrder")
	public Object syncOrder(@PathVariable("outChannel") String outChannel,@PathVariable("merchNo") String merchNo, 
			@PathVariable("orderNo") String orderNo,@RequestParam(required=false,name="businessNo") String businessNo){
		if(ParamUtil.isEmpty(outChannel)){
			return R.error("渠道编码不能为空！");
		}
		if(OutChannel.jfDesc().containsKey(outChannel) && ParamUtil.isEmpty(businessNo)){
			return R.error("业务单号不能为空！");
		}
		if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
			return R.error("参数不能为空！");
		}
		
		return payService.syncOrder(merchNo,orderNo,businessNo);
	}
	
	/**
	 * 
	 * @Description 同步代付订单信息
	 * @param outChannel
	 * @param merchNo
	 * @param orderNo
	 * @param businessNo
	 * @return
	 */
	@GetMapping("/syncOrderAcp/{outChannel}/{merchNo}/{orderNo}")
	@RequiresPermissions("pay:syncOrderAcp")
	public Object syncOrderAcp(@PathVariable("outChannel") String outChannel,@PathVariable("merchNo") String merchNo, 
			@PathVariable("orderNo") String orderNo,@RequestParam(required=false,name="businessNo") String businessNo){
		if(ParamUtil.isEmpty(outChannel)){
			return R.error("渠道编码不能为空！");
		}
		if(OutChannel.jfDesc().containsKey(outChannel) && ParamUtil.isEmpty(businessNo)){
			return R.error("业务单号不能为空！");
		}
		if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
			return R.error("参数不能为空！");
		}
		
		return payService.syncOrderAcp(merchNo,orderNo,businessNo);
	}
	
	/**
     * 
     * @Description 提现跳转
     * @param model
     * @return
     */
    @RequiresPermissions("pay:withdraw")
    @GetMapping("/withdraw")
    public ModelAndView withdraw(Model model){
        UserDO user = ShiroUtils.getUser();
        System.out.println(user.getUserType());
        if(UserType.merch.id() != user.getUserType() && UserType.agent.id() != user.getUserType() && UserType.subAgent.id() != user.getUserType()){
        	model.addAttribute("msg", "目前只支持商户或代理提现");
        	return new ModelAndView(PayConstants.url_pay_error_frame);
        }

    	this.buildWithdrawParam(model, user.getUsername(), user.getUserType());
        return new ModelAndView(PayConstants.url_pay_withdraw);
    }
    /**
	 * @Description 组装提现页面参数
	 * @param model
	 * @param username
	 * @param userType
	 */
	private void buildWithdrawParam(Model model, String username, Integer userType) {
		//商户号
		model.addAttribute("username", username);
		PayAcctBal payAcctBal = null;
		if(UserType.merch.id() == userType){
			payAcctBal = RedisUtil.getMerchBal(username);
//			Merchant merchant = merchantService.getById(username);
//			model.addAttribute("merObj",merchant);
		}else if(UserType.user.id() == userType) {
			payAcctBal = RedisUtil.getPayFoundBal();
		}else if(UserType.agent.id() == userType || UserType.subAgent.id() == userType) {
			payAcctBal = RedisUtil.getAgentBal(username);
//			Agent agent = agentService.get(username);
//			model.addAttribute("merObj",agentService.getById(agent.getAgentId()));
		}
		//资金余额
        model.addAttribute("payAcctBal", payAcctBal);
        //用户卡列表信息
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("username", username);
        List<UserBankDO> userBanks = userBankService.list(map);
        model.addAttribute("userBanks", userBanks);
        //所有的省信息
        model.addAttribute("provinces", locationService.listProvinces());
        //银行代码选择
        model.addAttribute("bankCodes", BankCode.desc());
        //银行卡类型
        model.addAttribute("cardTypes", CardType.desc());
	}
    
	/**
	 * 
	 * @Description 提现确认
	 * @param order
	 * @return
	 */
	@PostMapping("/withdraw/confirm")
	@RequiresPermissions("pay:withdraw")
	public Object withdraw(@RequestParam("fundPassword") String fundPassword,Order order){
        UserDO user = ShiroUtils.getUser();
        if(UserType.merch.id() != user.getUserType() && UserType.agent.id() != user.getUserType() && UserType.subAgent.id() != user.getUserType()){
            return R.error("目前只支持商户或代理提现");
        }
        if(UserType.merch.id() == user.getUserType()) {
            Merchant merchant = merchantService.get(user.getUsername());
            BigDecimal min=new BigDecimal(merchant.getAcpCnyMin());
            BigDecimal max=new BigDecimal(merchant.getAcpCnyMax());
            if(order.getAmount() .compareTo(min)==-1){
                return R.error("最小提现金额为"+min+"!");
            }
            if(order.getAmount() .compareTo(max)==1){
                return R.error("最大提现金额为"+max+"!");
            }

            if(!merchant.getStatus().equals(YesNoType.yes.id())) {
                return R.error("您的提现状态被禁用!");
            }
        }
        if(UserType.agent.id() == user.getUserType() || UserType.subAgent.id() == user.getUserType()) {
            Agent agent = agentService.get(user.getUsername());
            if(!agent.getStatus().equals(YesNoType.yes.id())) {
                return R.error("您的状态被禁用!");
            }
        }
        //验证资金密码
        R r = PasswordCheckUtils.checkFundPassword(fundPassword);
        if(R.ifError(r)){
            return r;
        }
        order.setMerchNo(user.getUsername());
        order.setUserType(user.getUserType());
        return payService.withdraw(order);
    }
	
	/**
     * 
     * @Description 跳转动态表单提交
     * @param context
     * @return
     */
    @GetMapping("/order/jump")
    public ModelAndView jump(@RequestParam(PayConstants.web_context) String context,Model model){
    	logger.info(PayConstants.web_context + context);
    	if(ParamUtil.isNotEmpty(context)){
    		try {
    			context = new String(RSAUtil.decryptByPrivateKey(Base64Utils.decode(context), QhPayUtil.getQhPrivateKey()));
			} catch (Exception e) {
				model.addAttribute(Constant.result_msg, "解密异常！");
    			return new ModelAndView(PayConstants.url_pay_error);
			}
    		JSONObject jo = JSONObject.parseObject(context);
    		String merchNo = jo.getString(OrderParamKey.merchNo.name());
    		String orderNo = jo.getString(OrderParamKey.orderNo.name());
    		if(ParamUtil.isEmpty(merchNo) || ParamUtil.isEmpty(orderNo)){
    			model.addAttribute(Constant.result_msg, "订单号或者商户号为空！");
    			return new ModelAndView(PayConstants.url_pay_error);
    		}
    		Order order = RedisUtil.getOrder(merchNo, orderNo);
    		if(order == null){
    			model.addAttribute(Constant.result_msg, "订单不存在！");
    	    	return new ModelAndView(PayConstants.url_pay_error);
    		}
    		model.addAttribute(PayConstants.web_jumpData, order.getJumpData());
    		return new ModelAndView(PayConstants.url_pay_jump);
    	}
    	model.addAttribute(Constant.result_msg, "请勿频繁测试！");
    	return new ModelAndView(PayConstants.url_pay_error);
    }

	/**
	 *
	 * @Description 手动补单回调
	 * @return
	 */
	@ResponseBody
	@PostMapping("/order/detection/{orderNO}")
	public  Object result(HttpServletRequest request,@PathVariable("orderNO") String orderNO) throws Exception {

		return payService.datetion(orderNO);

	}


    String http_build_query(Map<String, String> array){
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


    @ResponseBody
	@PostMapping("/order/test")
	public R pay_test(HttpServletRequest request) throws  Exception {
		JSONObject jsObj = new JSONObject();
		String reqTime = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		//商户号
		//jsObj.put("merchNo",jsonObject.getString("merchantId"));
		jsObj.put("merchNo", request.getParameter("merchantId"));
		//订单号
		String orderNo = reqTime + new Random().nextInt(10000);
		jsObj.put("orderNo", orderNo);
		System.out.println("orderNo--------" + orderNo);
		//支付渠道
		String channel = request.getParameter("channel_id");
		//String channel=OutChannel.wap.name();
		jsObj.put("outChannel", channel);
		//用户标志
		jsObj.put("userId", "201");
		//订单标题
		jsObj.put("title", "chongzhi");
		//产品名称
		jsObj.put("product", "chongzhi");
		//支付金额 单位 元
		jsObj.put("amount", request.getParameter("pay_amount"));
		//jsObj.put("amount","1");
		//币种
		jsObj.put("currency", "CNY");
		//前端返回地址
		jsObj.put("returnUrl", "www.baidu.com");
		//后台通知地址
		jsObj.put("notifyUrl", "www.baidu.com");
		//请求时间
		jsObj.put("reqTime", reqTime);
		//对公
		jsObj.put("acctType", 1);
		byte[] context = RSAUtil.encryptByPublicKey(JSON.toJSONBytes(jsObj), publicKey);
		String ccc = Base64Utils.encode(context);
		String sign = Md5Util.sign(ccc, "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQAq0/GuQOAifRYiE4htVrxhWVco+dTpK4zvTqZpfOVwEjVPe7IsvSX4usny3biI5fyXXclYyZCGTmy0Q3w8WqPZBFngkDgabFa7IME7stgODEEWnVGdDXE0RlCNt+Lut1zF+EJ1ekBI+TNO8y6MpNsrjVusUOydpMHZ5+8Xq/iwIDAQAB", "UTF-8");
		logger.info("签名结果：{}", sign);
		JSONObject jo = new JSONObject();
		jo.put("sign", sign);
		jo.put("context", context);
		logger.info("请求参数：{}", jo.toJSONString());
		String result = RequestUtils.doPostJson("http://123.1.170.6:8182/pay/order", jo.toJSONString());
		logger.info(result);
		JSONObject js = JSONObject.parseObject(result);
		String contextStr = js.getString("context");
		byte[] contextbyte = Base64Utils.decode(contextStr);
		String text = new String(contextbyte);
		logger.info("text : " + text);
		JSONObject objecttext = JSONObject.parseObject(text);
		objecttext.get("payurl");
//		JSONObject context = JSONObject
////				.getJSONObject("data").getString("payurl");
		Map<String, String> data = new HashMap<>();
		data.put("payurl", objecttext.get("payurl").toString());
		return R.okData(data);
	}
}