package com.qh.pay.controller;


import com.qh.common.config.CfgKeyConst;
import com.qh.common.domain.Tree;
import com.qh.common.utils.*;
import com.qh.pay.api.PayConstants;
import com.qh.pay.api.constenum.*;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.IndustryDO;
import com.qh.pay.domain.Merchant;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.IndustryService;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayAcctBalService;
import com.qh.redis.RedisConstants;
import com.qh.system.domain.ConfigDO;
import com.qh.system.domain.UserDO;
import com.qh.system.service.ConfigService;
import com.qh.system.service.UserService;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.Currency;

/**
 * 
 * 
 * @date 2018-02-24 17:25:59
 */
 
@Controller
@RequestMapping("/pay/agent")
public class AgentController {
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private AgentService agentService;
	@Autowired
	private IndustryService industryService;
	@Autowired
	private UserService userService;
	@Autowired
	private MerchantService merchantService;

	
	@GetMapping()
	@RequiresPermissions("pay:agent:agent")
	String Agent(Model model){
		model.addAttribute("auditStatus", AuditResult.desc());
		model.addAttribute("auditStatusColor", AuditResult.descColor());
		model.addAttribute("merTypes", AcctType.descMer());
		model.addAttribute("status", YesNoType.descStatus());
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		CryptoPayUtil.getAgentNoPrefix();
	    return "pay/agent/agent";
	}
	
	@ResponseBody
	@GetMapping("/list")
	@RequiresPermissions("pay:agent:agent")
	public PageUtils list(@RequestParam Map<String, Object> params){
		//查询列表数据
		UserDO u = ShiroUtils.getUser();
		if(u.getUserType() == UserType.agent.id() || u.getUserType() == UserType.subAgent.id()){
			params.put("pAgent", u.getUsername());
		}else {
			if(u.getUserType() != UserType.user.id()) {
				return new PageUtils(null,0);
			}
		}
        Query query = new Query(params);
		List<Agent> agentList = agentService.list(query);
		for (Agent agent : agentList) {
			String parentAgentNumber = agent.getParentAgent();
			if(StringUtils.isNotBlank(parentAgentNumber)) {
				Agent parentAgent = agentService.get(parentAgentNumber);
				agent.setParentAgent(parentAgent.getAgentName());
			}
		}
		int total = agentService.count(query);
		PageUtils pageUtils = new PageUtils(agentList, total);
		return pageUtils;
	}

	@GetMapping("/merchantInfo")
	@RequiresPermissions("pay:agent:merchantInfo")
	String merchant(Model model){
		return "pay/agent/merchantInfo";
	}

	@ResponseBody
	@GetMapping("/merchantList")
	@RequiresPermissions("pay:agent:merchantInfo")
	public PageUtils Merchantlist(@RequestParam Map<String, Object> params){
		UserDO userDO = ShiroUtils.getUser();
		if(ShiroUtils.ifAgent(userDO) || ShiroUtils.ifSubAgent(userDO)){
			params.put("pAgent",userDO.getUsername());
		}
		Query query = new Query(params);
		List<Merchant> merchantList = merchantService.list(query);
		int total = merchantService.count(query);
		PageUtils pageUtils = new PageUtils(merchantList, total);
		return pageUtils;
	}
	
	@GetMapping("/add")
	@RequiresPermissions("pay:agent:add")
	String add(Model model){
		//可选择的 代理商
		model.addAttribute("agentNs",AgentService.forChoice(agentService.getAgentByLevel(AgentLevel.one.id())));
        //费率单位
        model.addAttribute("rateUnits", RateUnit.desc());
		//可选择的币种渠道
		model.addAttribute("payCoins", OutChannel.desc());
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		return "pay/agent/add";
	}

	@GetMapping("/edit/{agentId}")
	@RequiresPermissions("pay:agent:edit")
	String edit(@PathVariable("agentId") Integer agentId, Model model){
		Agent agent = agentService.getById(agentId);
		if(agent != null && EmptyUtil.isNotEmpty(agent.getParentAgent())){
			Agent parentAgent = agentService.get(agent.getParentAgent());
			model.addAttribute("parentAgent",parentAgent.getAgentName() + "(" + parentAgent.getAgentNumber() + ")");
			model.addAttribute("parentCoinRate",parentAgent.getCoinRate());
		}
		model.addAttribute("agent",agent);
		//费率单位
		model.addAttribute("rateUnits", RateUnit.desc());
		//可选择的币种渠道
		model.addAttribute("payCoins", OutChannel.desc());
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		return "pay/agent/info";
	}
	
	@GetMapping("/agentInfo/{agentId}")
	@RequiresPermissions("pay:agent:edit")
	String agentInfo(@PathVariable("agentNo") String agentNo, Model model){
		UserDO u = ShiroUtils.getUser();
		Integer agentId = null;
		if(EmptyUtil.isNotEmpty(agentNo)) {
			Agent agent = agentService.get(agentNo);
			if(u.getUsername().equals(agent.getParentAgent())) {
				agentId = agent.getAgentId();
			}else {
				return null;
			}
		}else {
			Agent agent = agentService.get(u.getUsername());
			agentId = agent.getAgentId();
		}
		Agent agent = agentService.getById(agentId);
		if(agent != null && EmptyUtil.isNotEmpty(agent.getParentAgent())){
			this.setParentAgentDisplay(model, agent);
		}
		model.addAttribute("agent",agent);
		//费率单位
		model.addAttribute("rateUnits", RateUnit.desc());
		//可选择的币种渠道
		model.addAttribute("payCoins", OutChannel.desc());
		model.addAttribute("payChannelTypes", PayChannelType.desc());

		return "pay/agent/agentInfo";
	}
	

	
	@GetMapping("/infoQuery")
	@RequiresPermissions("pay:agent:infoQuery")
	String infoQuery(String agentNo,Model model){
		UserDO u = ShiroUtils.getUser();
		Integer agentId = null;
		if(EmptyUtil.isNotEmpty(agentNo)) {
			Agent agent = agentService.get(agentNo);
			if(u.getUsername().equals(agent.getParentAgent())) {
				agentId = agent.getAgentId();
			}else {
				return null;
			}
		}else {
			Agent agent = agentService.get(u.getUsername());
			agentId = agent.getAgentId();
		}
		Agent agent = agentService.getById(agentId);
		if(agent != null && EmptyUtil.isNotEmpty(agent.getParentAgent())){
			this.setParentAgentDisplay(model, agent);
		}
		model.addAttribute("agent",agent);
		//费率单位
		model.addAttribute("rateUnits", RateUnit.desc());
		//可选择的币种渠道
		model.addAttribute("payCoins", OutChannel.desc());
		model.addAttribute("payChannelTypes", PayChannelType.desc());
		if(agent != null && EmptyUtil.isNotEmpty(agent.getParentAgent())){
			Agent parentAgent = agentService.get(agent.getParentAgent());
			model.addAttribute("parentAgent",parentAgent.getAgentName() + "(" + parentAgent.getAgentNumber() + ")");
		}
		return "pay/agent/InfoQuery";
	}

	private void setParentAgentDisplay(Model model, Agent agent) {
		List<Agent> agentByLevel = agentService.getAgentByLevelAll(AgentLevel.one.id());
		for (Agent oAgent : agentByLevel) {
			if(oAgent.getAgentNumber().equals(agent.getParentAgent())){
				model.addAttribute("parentAgent",oAgent.getAgentName() + "(" + oAgent.getAgentNumber() + ")");
				break;
			}
		}
	}

	@Autowired
	private ConfigService configService;
	@Autowired
	private PayAcctBalService payAcctBalService;
	/**
	 * 保存
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@PostMapping("/save")
	@Transactional
	@RequiresPermissions("pay:agent:add")
	public R save( Agent agent,@RequestParam("coinRateStr") String coinRateStr){
		String agentNumber = CryptoPayUtil.getAgentNoPrefix()+ParamUtil.generateCode6();
		while(agentService.exist(agentNumber)){
			agentNumber = CryptoPayUtil.getAgentNoPrefix()+ParamUtil.generateCode6();
		}
		agent.setCoinRate(JSONObject.fromObject(coinRateStr));
		Date d = new Date();
		agent.setAgentNumber(agentNumber);
		agent.setCreateTime(d);
		agent.setModifyTime(d);
		agent.setStatus(0);
		agent.setAuditStatus(0);
		UserDO u = ShiroUtils.getUser();
		
		UserDO user = new UserDO();
		List<Integer> roleIds = new ArrayList<Integer>();
		if(ParamUtil.isEmpty(agent.getParentAgent())){
			agent.setLevel(AgentLevel.one.id());
			user.setUserType(UserType.agent.id());
			roleIds.add(UserRole.agent.id());
		}else{
			user.setUserType(UserType.subAgent.id());
			roleIds.add(UserRole.subAgent.id());
			Agent parentAgent = agentService.get(agent.getParentAgent());
			if(parentAgent==null || !parentAgent.getStatus().equals(YesNoType.yes.id())) {
				return R.error("上级代理异常或不存在!");
			}
			agent.setLevel(parentAgent.getLevel() + 1);
		}
		user.setRoleIds(roleIds);
		user.setMobile(agent.getContactsPhone());
		user.setUsername(agent.getAgentNumber());
		user.setName(agent.getAgentName());
		user.setEmail(agent.getContactsEmail());
		String password = "123456";
		ConfigDO configDO = configService.get(CfgKeyConst.pass_default_agent);
		if(configDO != null && ParamUtil.isNotEmpty(configDO.getConfigValue())){
			password = configDO.getConfigValue();
		}
		user.setPassword(Md5Util.MD5(password));
		user.setStatus(1);
		user.setUserIdCreate(u.getUserId());
		user.setGmtCreate(d);
		
		if(userService.save(user)>0){
			agent.setAgentId(user.getUserId());
			agentService.save(agent);
			redisTemplate.opsForHash().put(RedisConstants.cache_agent, agent.getAgentNumber(), agent);
			payAcctBalService.saveFromUser(user);

			return R.ok();
		}
		return R.error();
	}
	
	/**
	 * 修改
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/updateRate")
	@RequiresPermissions("pay:agent:edit")
	public R updateRate( Agent agent){
		agent.setModifyTime(new Date());

		agentService.update(agent);
		return R.ok();
	}
	
	/**
	 * 修改
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping("/update")
	@RequiresPermissions("pay:agent:edit")
	public R update( Agent agent,@RequestParam("coinRateStr") String coinRateStr){
		agent.setModifyTime(new Date());
		agent.setCoinRate(JSONObject.fromObject(coinRateStr));
		agentService.update(agent);
		return R.ok();
	}
    //获取子行业
    @ResponseBody
    @RequestMapping("/getAgentByParent")
    public R getAgentByParent(String parentAgent){
        return R.okData(AgentService.forChoice(agentService.getAgentByParent(parentAgent)));
    }
	//获取子行业
	@ResponseBody
	@RequestMapping("/getSubs")
	public R getSubs(String industryId){
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("parentid", industryId);
		List<IndustryDO> list = industryService.listSub(map);
		return R.okData(list);
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/remove")
	@ResponseBody
	@RequiresPermissions("pay:agent:remove")
	public R remove( Integer agentId){
		if(agentService.remove(agentId)>0){
		return R.ok();
		}
		return R.error();
	}
	
	/**
	 * 删除
	 */
	@PostMapping( "/batchRemove")
	@ResponseBody
	@RequiresPermissions("pay:agent:batchRemove")
	public R remove(@RequestParam("ids[]") Integer[] agentIds){
		agentService.batchRemove(agentIds);
		return R.ok();
	}
	/**
	 * 启用  禁用代理
	 */
	@PostMapping( "/batchOperate")
	@ResponseBody
	@RequiresPermissions("pay:agent:batchOperate")
	public R batchOperate(@RequestParam("agentIds[]") Integer[] agentIds, @RequestParam("flag") String flag){
		if(agentIds.length == 1) {
			Agent agent = agentService.get(agentIds[0]);
			if(agent.getAuditStatus() == AuditResult.pass.id()) {
				agentService.batchOperate(flag,agentIds);
			}else {
				return R.error("请先审核通过该商户资料！");
			}
		}
		return R.ok();
	}
	
	/**
	 * 审核代理
	 */
	@PostMapping( "/batchAudit")
	@ResponseBody
	@RequiresPermissions("pay:agent:batchAudit")
	public R batchAudit(@RequestParam("agentIds[]") Integer[] agentIds, @RequestParam("flag") boolean flag){
		
		if(agentIds.length == 1) {
			Map<String,Object> map = new HashMap<>();
			map.put("auditStatus", flag?1:2);
			map.put("array", agentIds);
			agentService.batchAudit(map);
		}
		return R.ok();
	}

	@GetMapping("/findSecondBySubAgent")
	@ResponseBody
	public R findSecondBySubAgent(String parentAgent){
		if(EmptyUtil.isEmpty(parentAgent)){
			List<Agent> agents = agentService.getAgentByLevel(AgentLevel.two.id());
			return  R.okData(agents);
		}
		List<Agent> agents = agentService.getAgentByParent(parentAgent);
		return R.okData(agents);
	}

	@GetMapping("/agentListInfo")
	@ResponseBody
	public R getAgentListInfo(){
		List<Agent> agentList = agentService.listAgentInfo(new HashMap<>());
		return R.okData(agentList);
	}

	@PostMapping("/parentAgentRate")
	@ResponseBody
	public R getAgentRateInfo(@RequestParam("parentAgentNumber")String parentAgentNumber){
		Agent agent = agentService.get(parentAgentNumber);
		Map<String,String> parentAgentRate = agent.getCoinRate().get(PayCoin.CNY.name());
		return R.okData(agent.getCoinRate());
	}



	@GetMapping("/tree")
	@ResponseBody
	public Tree<Agent> tree() {
		return agentService.getTree();
	}

	@GetMapping("/treeView")
	String treeView() {
		return  "pay/agent/agentTree";
	}


}
