package com.qh.pay.controller;


import com.qh.common.config.CfgKeyConst;
import com.qh.common.config.Constant;
import com.qh.common.utils.*;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.*;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.config.PayConfig;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.MerchantService;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.ConfigDO;
import com.qh.system.domain.UserDO;
import com.qh.system.service.ConfigService;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 聚富商户
 *
 * @date 2017-11-01 10:05:41
 */

@Controller
@RequestMapping("/pay/merchant")
public class MerchantController {
	@Autowired
	private MerchantService merchantService;
	@Autowired
	private AgentService agentService;
	@Autowired
	private PayConfig payConfig;

	@GetMapping()
	@RequiresPermissions("pay:merchant:merchant")
	String Merchant(Model model){
		Map<String,Object> map = new HashMap<>();
		model.addAttribute("outChannels", OutChannel.merchAll());
		model.addAttribute("auditStatus", AuditResult.desc());
		model.addAttribute("auditStatusColor", AuditResult.descColor());
		model.addAttribute("yesOrNos", YesNoType.desc());
		model.addAttribute("agentTypes", AcctType.descMer());
		model.addAttribute("status", YesNoType.descStatus());
		model.addAttribute("payChannelType", PayChannelType.desc());
		map.put("level",AgentLevel.one.id());
		model.addAttribute("subAgents",agentService.getAgentNameMap(map));
		map.replace("level",AgentLevel.one.id(),AgentLevel.two.id());
		model.addAttribute("secondAgents",agentService.getAgentNameMap(map));
		return "pay/merchant/merchant";
	}

	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:merchant:merchant")
	public PageUtils list(@RequestParam Map<String, Object> params){

		//查询列表数据
		UserDO u = ShiroUtils.getUser();
		if(u.getUserType() == UserType.agent.id() ||u.getUserType() == UserType.subAgent.id()){
			params.put("pAgent", u.getUsername());
			if(u.getUserType() == UserType.agent.id()) {
				params.put("level", 1);
			}
		}else {
			if(u.getUserType() != UserType.user.id()) {
				return new PageUtils(null,0);
			}
		}
		//只展示审核通过的商户
		params.put("auditStatus",AuditResult.pass.id());
		Query query = new Query(params);
		List<Merchant> merchantList = merchantService.list(query);
		Map<String,String> agentNameMap = agentService.getAgentNameMap(new HashMap<>());
		for (Merchant merchant : merchantList) {
			String parentNumber = merchant.getParentAgent();
			if(StringUtils.isNotBlank(parentNumber)) {
				String  parentAgentName = agentNameMap.get(parentNumber);
				if(parentAgentName != null){
					merchant.setParentAgent(parentAgentName+ "(" +  parentNumber + ")");
				}
			}
		}
		int total = merchantService.count(query);
		PageUtils pageUtils = new PageUtils(merchantList, total);
		return pageUtils;
	}

	@GetMapping("/merchantInfo/{merchantNO}")
	@RequiresPermissions("pay:merchant:edit")
	String merchantInfo(@PathVariable("merchantNO") String merchantNO, Model model){
		Merchant merchant = merchantService.getById(merchantNO);
		model.addAttribute("certTypes", CertType.desc());
		model.addAttribute("acctTypes", AcctType.desc());
		model.addAttribute("merchant", merchant);
		return "pay/merchant/realName";
	}

	@GetMapping("/rateInfo/{merchantNO}")
	@RequiresPermissions("pay:merchant:edit")
	String rateInfo(@PathVariable("merchantNO") String merchantNO, Model model){
		Merchant merchant = merchantService.getById(merchantNO);
		model.addAttribute("rates", merchant.getCoinRate());
		model.addAttribute("merchant", merchant);
		model.addAttribute("rateUnits", RateUnit.desc());
		model.addAttribute("yesOrNos", YesNoType.desc());
		model.addAttribute("paymentMethods", PaymentMethod.desc());
		model.addAttribute("outChannels", OutChannel.desc());

		Agent agent = agentService.get(merchant.getParentAgent());
		agent = agentService.getById(agent.getAgentId());
		model.addAttribute("parentRates", agent.getCoinRate());
		return "pay/merchant/rate";
	}

	@GetMapping("/add")
	@RequiresPermissions("pay:merchant:add")
	String add(Model model){
		model.addAttribute("agents",AgentService.forChoice(agentService.getAgentByLevel(AgentLevel.one.id())));
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		model.addAttribute("rateUnits", RateUnit.desc());
		model.addAttribute("onOff", YesNoType.descOnOff());
		model.addAttribute("payCoins",OutChannel.desc());
		model.addAttribute("payCompanys",PayCompany.desc());
		return "pay/merchant/add";
	}


	@GetMapping("/edit/{merchNo}")
	@RequiresPermissions("pay:merchant:edit")
	String edit(@PathVariable("merchNo") String merchNo, Model model){
		Merchant merchant = merchantService.getById(merchNo);
		Map<String,String> merCompany=merchant.getPayCompany();
		System.out.println("merchant:"+merchant.getCoinRate());
		model.addAttribute("merchant", merchant);
		if(merchant != null && ParamUtil.isNotEmpty(merchant.getParentAgent())){
			Agent agent = agentService.get(merchant.getParentAgent());
			if(agent != null){
				model.addAttribute("oneAgent",agent.getAgentName() + "(" + agent.getAgentNumber() + ")");
				model.addAttribute("parentCoinRate",agent.getCoinRate());
			}
		}
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		model.addAttribute("rateUnits", RateUnit.desc());
		model.addAttribute("onOff", YesNoType.descOnOff());
		model.addAttribute("payCoins",OutChannel.desc());
		model.addAttribute("payCompanys",PayCompany.desc());
		model.addAttribute("merCompany",merCompany);
		System.out.println(merCompany.toString());
		System.out.println(OutChannel.desc());
		this.setCustRateShow(model, merchant);
		return "pay/merchant/info";
	}

	@GetMapping("/infoQuery")
	@RequiresPermissions("pay:merchant:infoQ")
	String infoQuery(String merNo,Model model){
		UserDO u = ShiroUtils.getUser();
		model.addAttribute("merchFlag",ShiroUtils.ifMerch(u) || ShiroUtils.ifSubAgent(u));
		Merchant merchant;
		if(ParamUtil.isNotEmpty(merNo)) {
			merchant = merchantService.get(merNo);
		}else {
			merchant = merchantService.getById(u.getUsername());
		}
		if(merchant != null){
			Agent agent = agentService.get(merchant.getParentAgent());
			if(agent != null){
				model.addAttribute("oneAgent",agent.getAgentName() + "(" + agent.getAgentNumber() + ")");
				if(ParamUtil.isNotEmpty(agent.getParentAgent())){
					agent = agentService.get(merchant.getParentAgent());
					if(agent != null){
						model.addAttribute("subAgent",agent.getAgentName() + "(" + agent.getAgentNumber() + ")");
					}
				}
			}
		}else{
			model.addAttribute("msg","查无商户信息");
			return PayConstants.url_pay_error_frame;
		}
		model.addAttribute("merchant", merchant);
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		model.addAttribute("rateUnits", RateUnit.desc());
		model.addAttribute("onOff", YesNoType.descOnOff());
		model.addAttribute("payCoins",OutChannel.desc());
		try {
			model.addAttribute("qhPublicKey", merchant.getPublicKey());
		} catch (Exception e) {
			model.addAttribute("qhPublicKey","");
		}
		return "pay/merchant/InfoQuery";
	}

	/**
	 * 保存
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/save")
	@RequiresPermissions("pay:merchant:add")
	public R save(Merchant merchant, @RequestParam("coinRateStr") String coinRateStr, @RequestParam("coinSwitchStr") String coinSwitchStr,@RequestParam("payCompanysStr") String payCompanysStr){
		merchant.setPayCompany(JSONObject.fromObject(payCompanysStr));
		merchant.setCoinRate(JSONObject.fromObject(coinRateStr));
		merchant.setCoinSwitch(JSONObject.fromObject(coinSwitchStr));
		merchant.setStatus(0);
		merchant.setAuditStatus(0);
		merchant.setCrtTime(new Date());
		if(merchant.getAcpCnyMin().compareTo(merchant.getAcpCnyMax()) > 0){
			return R.error("提现CNY最小值不能大于最大值");
		}
//		if(merchant.getAcpUsdtMin().compareTo(merchant.getAcpUsdtMax()) > 0){
//			return R.error("提现USDT最小值不能大于最大值");
//		}
		Agent agent = agentService.get(merchant.getParentAgent());
		if(agent==null || !agent.getStatus().equals(YesNoType.yes.id())) {
			return R.error("上级代理异常或不存在!");
		}else {
			if(agent.getLevel().equals(AgentLevel.two.id())) {
				agent = agentService.get(agent.getParentAgent());
				if(agent==null || !agent.getStatus().equals(YesNoType.yes.id())) {
					return R.error("上级代理异常或不存在!");
				}
			}
		}
		int count = merchantService.save(merchant);
		if(count == 1 ){
			return R.ok();
		}else if(count == Constant.data_exist){
			return R.error(merchant.getMerchNo() + "商户已经存在");
		}
		return R.error();
	}


	/**
	 * 修改
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("pay:merchant:edit")
	public R update(Merchant merchant, @RequestParam("coinRateStr") String coinRateStr, @RequestParam("coinSwitchStr") String coinSwitchStr, @RequestParam("payCompanysStr") String payCompanysStr){
		merchant.setPayCompany(JSONObject.fromObject(payCompanysStr));
		merchant.setCoinRate(JSONObject.fromObject(coinRateStr));
		merchant.setCoinSwitch(JSONObject.fromObject(coinSwitchStr));
		if(merchant.getAcpCnyMin().compareTo(merchant.getAcpCnyMax()) > 0){
			return R.error("提现CNY最小值不能大于最大值");
		}
//		if(merchant.getAcpUsdtMin().compareTo(merchant.getAcpUsdtMax()) > 0){
//			return R.error("提现USDT最小值不能大于最大值");
//		}
		merchantService.update(merchant);
		return R.ok();
	}

	@GetMapping("/custRate")
	@RequiresPermissions("pay:merchant:custRate")
	String custRate(Model model){
		String merchNo = ShiroUtils.getUsername();
		Merchant merchant = merchantService.getById(merchNo);
		if(merchant == null){
			model.addAttribute("msg","查无商户信息");
			return PayConstants.url_pay_error_frame;
		}
		model.addAttribute("merchant", merchant);
		model.addAttribute("rateUnits", RateUnit.desc());
		model.addAttribute("onOff", YesNoType.descOnOff());
		model.addAttribute("payCoins",OutChannel.desc());
		this.setCustRateShow(model, merchant);
		return "pay/merchant/custRate";
	}

	@Autowired
	private ConfigService configService;

	private void setCustRateShow(Model model, Merchant merchant) {
		Map<String,BigDecimal> accpRate = new HashMap<>();
		Map<String,BigDecimal> resultRate = new HashMap<>();
		model.addAttribute("accpRate",accpRate);
		model.addAttribute("resultRate",resultRate);
	}

	@GetMapping("/multBigForRate")
	@ResponseBody
	public R multBigForRate(@RequestParam("payCoin")String payCoin, @RequestParam("rate") String rate){
		return R.ok();
	}


	/**
	 * 修改客户费率
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/updateCustRate")
	@RequiresPermissions("pay:merchant:custRate")
	public R updateCustRate(Merchant merchant,@RequestParam("custRateStr") String custRateStr){
		merchant.setCustRate(JSONObject.fromObject(custRateStr));
		for (Map.Entry<String, BigDecimal> entry : merchant.getCustRate().entrySet()) {
			if(new BigDecimal(String.valueOf(entry.getValue())).compareTo(BigDecimal.ZERO) < 0){
				return R.error("请输入大于0的值");
			}
		}
		merchantService.updateCustRate(merchant);
		return R.ok();
	}


	/**
	 * 修改实名
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/updateRealName")
	@RequiresPermissions("pay:merchant:edit")
	public R update(Merchant merchant){
		merchantService.update(merchant);
		return R.ok();
	}

	/**
	 * 修改公有密钥,同时保存私钥到缓存
	 */
	@ResponseBody
	@RequestMapping("/updatePKey")
	@RequiresPermissions("pay:merchant:updatePKey")
	public R updatePKey(@RequestParam Map<String,Object> params){
		Merchant merchantU = new Merchant();
		String merchNo = params.get("merchNo").toString();
		String publicKey=params.get("publicKey").toString();
		String privateKey=params.get("privateKey").toString();
		if(StringUtils.isBlank(merchNo)) {
			UserDO u = ShiroUtils.getUser();
			merchNo = u.getUsername();
		}
		publicKey = publicKey.replaceAll("\r|\n", "").replaceAll(" ", "+");
		merchantU.setPublicKey(publicKey);
		merchantU.setMerchNo(merchNo);
		merchantService.updatePKey(merchNo,publicKey);
		privateKey = privateKey.replaceAll("\r|\n", "").replaceAll(" ", "+");
		merchantService.updatePrivateKey(merchNo,privateKey);
		return R.ok();
	}

	/**
	 * 删除
	 */
	@PostMapping( "/remove")
	@ResponseBody
	@RequiresPermissions("pay:merchant:remove")
	public R remove(String merchNo){
		if(merchantService.remove(merchNo)>0){
			return R.ok();
		}
		return R.error();
	}

	/**
	 * 删除
	 */
	@PostMapping( "/batchRemove")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchRemove")
	public R remove(@RequestParam("merchNos[]") String[] merchNos){
		merchantService.batchRemove(merchNos);
		return R.ok();
	}

	@PostMapping("/exist")
	@ResponseBody
	boolean exist(@RequestParam("merchNo") String merchNo) {
		// 存在，不通过，false
		return !merchantService.exist(merchNo);
	}



	@GetMapping("/merchantPersonal")
	@RequiresPermissions("pay:merchant:merchant")
	String merchantPersonal(Model model){
		UserDO user = ShiroUtils.getUser();
		Merchant merchant = merchantService.getWithBalance(user.getUsername());
		PayAcctBal payAcctBal = RedisUtil.getMerchBal(user.getUsername());
		merchant.setBalance(payAcctBal.getBalance());
		model.addAttribute("merchant",merchant);
		model.addAttribute("outChannels", OutChannel.merchAll());
		return "pay/merchantPersonal/merchantPersonal";
	}

	/**
	 * 启用  禁用代理
	 */
	@PostMapping( "/batchOperate")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchOperate")
	public R batchOperate(@RequestParam("merchantIds[]") Integer[] merchantIds, @RequestParam("flag") String flag){
		if(merchantIds.length == 1) {
			Merchant merchant = merchantService.get(merchantIds[0]);
			if(merchant.getAuditStatus() == AuditResult.pass.id()) {
				merchantService.batchOperate(flag,merchantIds);
			}else {
				return R.error("请先审核通过该商户资料！");
			}
		}
		return R.ok();
	}
	/**
	 * 审核
	 */
	@PostMapping( "/batchAudit")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchAudit")
	public R batchAudit(@RequestParam("merchantIds[]") Integer[] merchantIds, @RequestParam("flag") boolean flag){
		if(merchantIds.length == 1) {
			Map<String,Object> map = new HashMap<>();
			map.put("auditStatus", flag?1:2);
			map.put("array", merchantIds);
			int count = merchantService.batchAudit(map);

		}
		return R.ok();
	}

	/**
	 * 审核
	 */
	@PostMapping( "/batchWithdrawal")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchWithdrawal")
	public R batchWithdrawal(@RequestParam("merchantIds[]") Integer[] merchantIds, @RequestParam("flag") String flag){
		if(merchantIds.length == 1) {
			Map<String,Object> map = new HashMap<>();
			map.put("withdrawalStatus", flag);
			map.put("array", merchantIds);
			merchantService.batchWithdrawal(map);
		}
		return R.ok();
	}

	/**
	 * 审核
	 */
	@PostMapping( "/batchPaid")
	@ResponseBody
	@RequiresPermissions("pay:merchant:batchPaid")
	public R batchPaid(@RequestParam("merchantIds[]") Integer[] merchantIds, @RequestParam("flag") String flag){
		if(merchantIds.length == 1) {
			Map<String,Object> map = new HashMap<>();
			map.put("paidStatus", flag);
			map.put("array", merchantIds);
			merchantService.batchPaid(map);
		}
		return R.ok();
	}

	@PostMapping( "/createPrivateKey")
	@RequiresPermissions("pay:merchant:updatePKey")
	@ResponseBody
	public List<String> createPrivateKey(){
		List<String> list =new ArrayList<>();
		Map<String, Object>  map=null;
		try {
			map= RSAUtil.genKeyPair();
			list.add(RSAUtil.getPublicKey(map));
			list.add(RSAUtil.getPrivateKey(map));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@PostMapping( "/sendEmail")
	@RequiresPermissions("pay:merchant:sendEmail")
	@ResponseBody
	public R sendEmail(@RequestParam("merchNo") String merchNo, Integer state){
		Merchant merchant =merchantService.get(merchNo);
		if(merchantService.getPrivateKey(merchNo)==null||merchant.getPublicKey()==null||"".equals(merchant.getPublicKey())){
			return R.error("请先配置秘钥!");
		}
		if(RedisUtil.getHashValue(CfgKeyConst.email_message,merchNo)!=null&&state==0){
			return  R.error("已发送邮箱");
		}
		String password = "123456";
		ConfigDO configDO = configService.get(CfgKeyConst.pass_default_merch);
		if(configDO != null && ParamUtil.isNotEmpty(configDO.getConfigValue())){
			password = configDO.getConfigValue();
		}
		String email = merchant.getContactsEmail();
		String privateKey= merchantService.getPrivateKey(merchNo);
		String cryptoPayPubKey = null;
		try {
			cryptoPayPubKey = merchant.getPublicKey();
		} catch (Exception e) {
			return R.error("获取平台公钥失败！");
		}

		String paydoMain = "localhost";
		configDO = configService.get(CfgKeyConst.pay_domain);
		if(configDO != null && ParamUtil.isNotEmpty(configDO.getConfigValue())){
			paydoMain = configDO.getConfigValue();
		}
		if(paydoMain==null||"".equals(paydoMain)){
			return R.error("请先配置平台地址！");
		}
		String html = SendMailUtil.getHtml(paydoMain,cryptoPayPubKey,privateKey,merchNo,password);
		R r = SendMailUtil.sendEmail(email,html);
		if(R.ifSucc(r)) {
			RedisUtil.setHashValue(CfgKeyConst.email_message,merchNo,html);
			return R.ok();
		}
		return r;
	}


	@GetMapping("/RSAConfig/{merchNo}")
	String RSAConfig(@PathVariable("merchNo") String merchNo, Model model){
		Merchant merchant =merchantService.get(merchNo);
		model.addAttribute("merchant",merchant);
		return "pay/merchant/RSAConfig";
	}

	@ResponseBody
	@GetMapping("/merchantList")
	public  R merchantList(){
		List<Merchant> list = merchantService.list(new HashMap<>());
		return R.okData(list);
	}
}
