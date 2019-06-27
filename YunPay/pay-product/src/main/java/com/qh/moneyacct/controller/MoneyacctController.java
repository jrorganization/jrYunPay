package com.qh.moneyacct.controller;

import com.qh.common.utils.PageUtils;
import com.qh.common.utils.Query;
import com.qh.common.utils.R;
import com.qh.common.utils.ShiroUtils;
import com.qh.moneyacct.domain.MoneyacctDO;
import com.qh.moneyacct.querydao.*;
import com.qh.pay.api.Order;
import com.qh.pay.api.constenum.*;
import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.PasswordCheckUtils;
import com.qh.pay.dao.RecordFoundAcctDao;
import com.qh.pay.dao.RecordFoundAvailAcctDao;
import com.qh.pay.dao.RecordMerchAvailBalDao;
import com.qh.pay.dao.RecordMerchBalDao;
import com.qh.pay.domain.*;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayHandlerService;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.UserDO;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/moneyacct")
public class MoneyacctController {
    
    @Autowired
    private AgentDao agentDao;
    @Autowired
    private MerchantDao merchantDao;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private RcMerchBalDao rcMerchantDao;
    @Autowired
    private RcFoundBalDao rcFoundBalDao;
    @Autowired
    private RcPayMerchBalDao rcPayMerchBalDao;
    @Autowired
    private RecordFoundAcctDao rdFoundAcctDao;
    @Autowired
    private RecordMerchBalDao rdMerchBalDao;
    @Autowired
    private RecordMerchAvailBalDao rdMerchAvailBalDao;
    @Autowired
    private RecordFoundAvailAcctDao rdFoundAvailAcctDao;
    @Autowired
    private PayHandlerService payHandlerService;
    //聚富钱包
    @GetMapping("/jfmoney")
    @RequiresPermissions("moneyacct:jfmoney")
    public String jfmoney(Model model){
        model.addAttribute("feeTypes", FeeType.merchDesc());
        model.addAttribute("orderTypes", OrderType.desc());
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        String userName = user.getUsername();
        MoneyacctDO moneyacct = new MoneyacctDO().initZero();
        model.addAttribute("moneyacct", moneyacct);
        if(UserType.merch.id() == userType) {
            moneyacct = rcMerchantDao.statMerchByNo(userName);
            this.merchParamToMoneyacct(userName, moneyacct);
        } else {
            Query query = new Query();
            query.put("userType", userType);
            query.put("username", userName);
            model.addAttribute("feeTypes", FeeType.agentDesc());
            if(UserType.user.id() == userType) {
                userName =  RedisUtil.getPayFoundBal().getUsername(); 
                model.addAttribute("feeTypes", FeeType.platDesc());
            }
            query.put("subAgent", userName);
            moneyacct = rcFoundBalDao.findOneAgent(query);
            this.agentParamToMoneyacct(userName, moneyacct);
        }
        this.foundParamToMoneyacct(moneyacct);
        if(moneyacct != null) {
            model.addAttribute("moneyacct", moneyacct);
        }
        return "moneyacct/jfmoney";
    }
    @ResponseBody
    @GetMapping("/jfmoney/detailList")
    @RequiresPermissions("moneyacct:jfmoney")
    public PageUtils jfmoneyDetailList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        UserDO user = ShiroUtils.getUser();
        Query query = new Query(params);
        Integer userType = user.getUserType();
        String username = user.getUsername();
        query.put("userType", user.getUserType());
        query.put("username", user.getUsername());
        PageUtils pageUtils = null;
        if(UserType.merch.id() == userType) {
            query.put("merchNo", user.getUsername());
            List<RecordMerchBalDO> rcMerchBalList = rcMerchantDao.findMerchantDetailList(query);
            int total = rcMerchantDao.findMerchantDetailListCount(query);
            pageUtils = new PageUtils(rcMerchBalList, total);
        } else {
            query.put("userType", userType);
            query.put("username", username);
            if(UserType.user.id() == userType) {
                username =  RedisUtil.getPayFoundBal().getUsername(); 
            }else {
                query.put("subAgent", username);
            }
            List<RecordFoundAcctDO> rcMerchBalList = rcFoundBalDao.findAgentDetailList(query);
            int total = rcFoundBalDao.findAgentDetailListCount(query);
            pageUtils = new PageUtils(rcMerchBalList, total);
        }
        return pageUtils;
    }

    @ResponseBody
    @PostMapping("/jfmoney/detailList/footer")
    @RequiresPermissions("moneyacct:jfmoney")
    public R jfmoneyDetailListListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate, @RequestParam Map<String, Object> params){
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        //查询列表数据
        UserDO user = ShiroUtils.getUser();
        String username = user.getUsername();
        Integer userType = user.getUserType();
        params.put("userType", userType);
        params.put("username", username);
        FooterDO fdo = null;
        if(UserType.merch.id() == userType) {
            params.put("merchNo", user.getUsername());
            fdo = rcMerchantDao.findMerchantDetailListFooter(params);
        } else {
            if(UserType.user.id() == userType) {
                username =  RedisUtil.getPayFoundBal().getUsername(); 
            }
            params.put("agentNo", username);
            fdo = rcFoundBalDao.findAgentDetailListFooter(params);
        }
        
        return R.okData(fdo);
    }

    //第三方账户钱包
    @GetMapping("/payMerch")
    @RequiresPermissions("moneyacct:payMerch")
    public String payMerch(Model model){
        if(UserType.user.id() == ShiroUtils.getUser().getUserType()) {
            model.addAttribute("feeTypes", FeeType.merchDesc());
            model.addAttribute("orderTypes", OrderType.desc());
            model.addAttribute("payCompanys", PayCompany.desc());
            model.addAttribute("outChannels", OutChannel.desc());
        }
        return "moneyacct/payMerch";
    }
    @ResponseBody
    @GetMapping("/payMerch/list")
    @RequiresPermissions("moneyacct:payMerch")
    public PageUtils payMerchList(@RequestParam Map<String, Object> params){
        UserDO user = ShiroUtils.getUser();
        Query query = new Query(params);
        query.put("userType", user.getUserType());
        query.put("username", user.getUsername());
        List<MoneyacctDO> moneyaccts = new ArrayList<>();
        int total = 0;
        if(UserType.user.id() == user.getUserType()) {
            moneyaccts = rcPayMerchBalDao.findPayMerchList(query);
            for (MoneyacctDO moneyacct : moneyaccts) {
                this.payMerchParamToMoneyacct(moneyacct.getPayCompany(),moneyacct.getPayMerch(), moneyacct);
            }
            total = rcPayMerchBalDao.findPayMerchListCount(query);
        }
        PageUtils pageUtils = new PageUtils(moneyaccts, total);
        return pageUtils;
    }


    @GetMapping("/payMerch/detail/{payCompany}/{payMerch}/{outChannel}")
    @RequiresPermissions("moneyacct:agent")
    public String payMerchDetail(@PathVariable("payCompany") String payCompany, @PathVariable("payMerch") String payMerch, 
            @PathVariable("outChannel") String outChannel,  Model model){
        this.handlerPayMerchDetail(payCompany, payMerch, outChannel, model);
        return "moneyacct/agentDetail";
    }
    @GetMapping("/payMerch/detail/{payCompany}/{payMerch}")
    @RequiresPermissions("moneyacct:agent")
    public String payMerchDetail(@PathVariable("payCompany") String payCompany, @PathVariable("payMerch") String payMerch,  Model model){
        this.handlerPayMerchDetail(payCompany, payMerch, null, model);
        return "moneyacct/payMerchDetail";
    }
    
    private void handlerPayMerchDetail(String payCompany, String payMerch, String outChannel, Model model) {
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        MoneyacctDO moneyacct = new MoneyacctDO().initZero();
        model.addAttribute("moneyacct", moneyacct);
        if(UserType.user.id() == userType) {
        	
            model.addAttribute("payCompanys", PayCompany.desc());
            model.addAttribute("outChannels", OutChannel.desc());
        	
            model.addAttribute("payCompany", payCompany);
            model.addAttribute("payMerch",payMerch);
            model.addAttribute("outChannel",outChannel);
            model.addAttribute("feeTypes",FeeType.payMerchDesc());
            model.addAttribute("orderTypes", OrderType.desc());
            Query query = new Query();
            query.put("payCompany", payCompany);
            query.put("payMerch", payMerch);
            query.put("outChannel", outChannel);
            moneyacct = rcPayMerchBalDao.findOnePayMerch(query);
            this.payMerchParamToMoneyacct(payCompany, payMerch, moneyacct);
            this.mearchMoneyacctValue(moneyacct);
            if(moneyacct != null) {
                model.addAttribute("moneyacct", moneyacct);
            }
        }
    }
    @ResponseBody
    @GetMapping("/payMerch/detailList")
    @RequiresPermissions("moneyacct:payMerch")
    public PageUtils payMerchDetailList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        UserDO user = ShiroUtils.getUser();
        Query query = new Query(params);
        query.put("userType", user.getUserType());
        query.put("username", user.getUsername());
        List<RecordPayMerchBalDO> rcMerchBalList = rcPayMerchBalDao.findPayMerchDetailList(query);
        int total = rcPayMerchBalDao.findPayMerchDetailListCount(query);
        PageUtils pageUtils = new PageUtils(rcMerchBalList, total);
        return pageUtils;
    }

    @ResponseBody
    @PostMapping("/payMerch/detailList/footer")
    @RequiresPermissions("moneyacct:payMerch")
    public R payMerchDetailListListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate, @RequestParam Map<String, Object> params){
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        //查询列表数据
        UserDO user = ShiroUtils.getUser();
        params.put("userType", user.getUserType());
        params.put("username", user.getUsername());
        FooterDO fdo = rcPayMerchBalDao.findPayMerchDetailListFooter(params);
        return R.okData(fdo);
    }
    @ResponseBody
    @GetMapping("/payMerch/listForOutChannel")
    @RequiresPermissions("moneyacct:payMerch")
    public PageUtils payMerchListForOutChannel(@RequestParam Map<String, Object> params){
        UserDO user = ShiroUtils.getUser();
        Query query = new Query(params);
        query.put("userType", user.getUserType());
        query.put("username", user.getUsername());
        List<MoneyacctDO> moneyaccts = new ArrayList<>();
        int total = 0;
        if(UserType.user.id() == user.getUserType()) {
            moneyaccts = rcPayMerchBalDao.findPayMerchForOutChannelList(query);
            for (MoneyacctDO moneyacct : moneyaccts) {
                this.payMerchParamToMoneyacct(moneyacct.getPayCompany(),moneyacct.getPayMerch(), moneyacct);
            }
            total = 10;//rcPayMerchBalDao.findPayMerchListCount(query);
        }
        PageUtils pageUtils = new PageUtils(moneyaccts, total);
        return pageUtils;
    }
    
    //商户钱包
    @GetMapping("/merchant")
    @RequiresPermissions("moneyacct:merchant")
    public String merchant(Model model){
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        String userName = user.getUsername();
        if(UserType.subAgent.id() == userType) {
            model.addAttribute("agentNumber", user.getUsername());
            model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.agent.id() == userType) {
            model.addAttribute("agentNumber", user.getUsername());
            model.addAttribute("twoAgents", agentDao.findAgentByParent(userName));
            model.addAttribute("merchants", merchantDao.findMerchantByAgent(userName));
        }else if(UserType.merch.id() == userType) {
            model.addAttribute("merchNo", userName);
        }else if(UserType.user.id() == userType) {
            model.addAttribute("oneAgents", agentDao.findOneLevelAgent());
        }
        return "moneyacct/merchant";
    }
    
    @ResponseBody
    @GetMapping("/findAgentByParent")
    public R findAgentByParent(String oneAgent){
        return R.okData(agentDao.findAgentByParent(oneAgent));
    }
    
    @ResponseBody
    @GetMapping("/findMerchantByAgent")
    public R findMerchantByAgent(String parentAgent){
        return R.okData(merchantDao.findMerchantByAgent(parentAgent));
    }
    
    @ResponseBody
    @GetMapping("/merchant/list")
    @RequiresPermissions("moneyacct:merchant")
    public PageUtils merchantList(@RequestParam Map<String, Object> params){
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        List<MoneyacctDO> moneyaccts = new ArrayList<>();
        int total = 0;
        String merchNo = (String) params.get("merchNo");
        String subAgent = (String) params.get("twoAgent");
        String agent = (String) params.get("oneAgent");
        
        String userName = user.getUsername();
        if(UserType.merch.id() == userType) {
            if(ParamUtil.isEmpty(merchNo) || merchNo.equals(userName)) {
                MoneyacctDO moneyacct = rcMerchantDao.statMerchByNo(userName);
                if(moneyacct!=null) {
                    this.merchParamToMoneyacct(userName, moneyacct);
                    moneyaccts.add(moneyacct);
                }
            }
        }
        if(UserType.subAgent.id() == userType) {
            if(ParamUtil.isNotEmpty(merchNo)) {
                MoneyacctDO moneyacct = rcMerchantDao.statMerchByAgentAndMerchNo(userName,merchNo);
                if(moneyacct!=null) {
                    this.merchParamToMoneyacct(userName, moneyacct);
                    moneyaccts.add(moneyacct);
                }
            }else if(ParamUtil.isEmpty(merchNo) &&(ParamUtil.isEmpty(subAgent) || subAgent.equals(userName))) {
                Query query = new Query(params);
                query.put("subAgent", userName);
                List<MoneyacctDO> moneyacctData = rcMerchantDao.statMerchByAgent(query);
                for (MoneyacctDO moneyacctDO : moneyacctData) {
                    this.merchParamToMoneyacct(moneyacctDO.getMerchNo(), moneyacctDO);
                    moneyaccts.add(moneyacctDO);
                }
                total = rcMerchantDao.statMerchByAgentCount(query);
            }
        }
        if(UserType.agent.id() == userType) {
            if(ParamUtil.isNotEmpty(merchNo)) {
                MoneyacctDO moneyacct = null;
                if(ParamUtil.isNotEmpty(subAgent)) {
                    moneyacct = rcMerchantDao.statMerchByAgentAndMerchNoLimit(userName,subAgent,merchNo);
                }else {
                    moneyacct = rcMerchantDao.statMerchByAgentAndMerchNo(userName,merchNo);
                }
                if(moneyacct!=null) {
                    this.merchParamToMoneyacct(userName, moneyacct);
                    moneyaccts.add(moneyacct);
                }
            }else if(ParamUtil.isEmpty(merchNo) &&(ParamUtil.isEmpty(agent) || agent.equals(userName))) {
                Query query = new Query(params);
                List<MoneyacctDO> moneyacctData = null;
                if(ParamUtil.isNotEmpty(subAgent)) {
                    query.put("subAgent", subAgent);
                    query.put("agent", agent);
                    moneyacctData = rcMerchantDao.statMerchByAgentLimit(query);
                    total = rcMerchantDao.statMerchByAgentCountLimit(query);
                }else {
                    query.put("subAgent", userName);
                    moneyacctData = rcMerchantDao.statMerchByAgent(query);
                    total = rcMerchantDao.statMerchByAgentCount(query);
                }
                for (MoneyacctDO moneyacctDO : moneyacctData) {
                    this.merchParamToMoneyacct(moneyacctDO.getMerchNo(), moneyacctDO);
                    moneyaccts.add(moneyacctDO);
                }
            }
        }
        
        if(UserType.user.id() == userType) {
            if(ParamUtil.isNotEmpty(merchNo)) {
                MoneyacctDO moneyacct = rcMerchantDao.statMerchByNo(merchNo);
                if(moneyacct!=null) {
                    this.merchParamToMoneyacct(userName, moneyacct);
                    moneyaccts.add(moneyacct);
                }
            }else if(ParamUtil.isNotEmpty(subAgent)) {
                Query query = new Query(params);
                query.put("subAgent", subAgent);
                List<MoneyacctDO> moneyacctData = rcMerchantDao.statMerchByAgent(query);
                for (MoneyacctDO moneyacctDO : moneyacctData) {
                    this.merchParamToMoneyacct(moneyacctDO.getMerchNo(), moneyacctDO);
                    moneyaccts.add(moneyacctDO);
                }
                total = rcMerchantDao.statMerchByAgentCount(query);
            }else if(ParamUtil.isNotEmpty(agent)) {
                Query query = new Query(params);
                query.put("subAgent", agent);
                List<MoneyacctDO> moneyacctData = rcMerchantDao.statMerchByAgent(query);
                for (MoneyacctDO moneyacctDO : moneyacctData) {
                    this.merchParamToMoneyacct(moneyacctDO.getMerchNo(), moneyacctDO);
                    moneyaccts.add(moneyacctDO);
                }
                total = rcMerchantDao.statMerchByAgentCount(query);
            }else {
                Query query = new Query(params);
                List<MoneyacctDO> moneyacctData = rcMerchantDao.statMerchAll(query);
                for (MoneyacctDO moneyacctDO : moneyacctData) {
                    this.merchParamToMoneyacct(moneyacctDO.getMerchNo(), moneyacctDO);
                    moneyaccts.add(moneyacctDO);
                }
                total = rcMerchantDao.statMerchAllCount(query);
            }
        }
        if(total == 0) {
            total = moneyaccts.size();
        }
        for (MoneyacctDO moneyacctDO : moneyaccts) {
        	Merchant merchant = merchantService.get(moneyacctDO.getMerchNo());
        	moneyacctDO.setName(merchant.getMerchantsName());
		}
        PageUtils pageUtils = new PageUtils(moneyaccts, total);
        return pageUtils;
    }

    @GetMapping("/merchant/detail/{merchNo}")
    @RequiresPermissions("moneyacct:merchant")
    public String merchantDetail(@PathVariable("merchNo") String merchNo,Model model){
        model.addAttribute("merchNo", merchNo);
        model.addAttribute("feeTypes", FeeType.merchDesc());
        model.addAttribute("orderTypes", OrderType.desc());
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        String userName = user.getUsername();
        MoneyacctDO moneyacct = new MoneyacctDO().initZero();
        model.addAttribute("moneyacct", moneyacct);
        if(ParamUtil.isEmpty(merchNo)) {
            return "moneyacct/merchant"; 
        }
        if(UserType.merch.id() == userType && merchNo.equals(userName)) {
            moneyacct = rcMerchantDao.statMerchByNo(merchNo);
        } else if(UserType.subAgent.id() == userType) {
            moneyacct = rcMerchantDao.statMerchByAgentAndMerchNo(userName,merchNo);
        }else if(UserType.agent.id() == userType) {
            moneyacct = rcMerchantDao.statMerchByAgentLimitAndMerchNo(userName,merchNo);
        }else if(UserType.user.id() == userType) {
            moneyacct = rcMerchantDao.statMerchByNo(merchNo);
        }
        
        this.merchParamToMoneyacct(merchNo, moneyacct);
        this.mearchMoneyacctValue(moneyacct);
        if(moneyacct != null) {
            model.addAttribute("moneyacct", moneyacct);
        }
        return "moneyacct/merchantDetail";
    }
    
    @GetMapping("/merchant/detail")
    @RequiresPermissions("moneyacct:merchant:detail")
    public String merchantDetail(Model model){
    	UserDO u = ShiroUtils.getUser();
    	String merchNo = u.getUsername();
        model.addAttribute("merchNo", merchNo);
        model.addAttribute("feeTypes", FeeType.merchDesc());
        model.addAttribute("orderTypes", OrderType.desc());
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        String userName = user.getUsername();
        MoneyacctDO moneyacct = new MoneyacctDO().initZero();
        model.addAttribute("moneyacct", moneyacct);
        if(ParamUtil.isEmpty(merchNo)) {
            return "moneyacct/merchant"; 
        }
        if(UserType.merch.id() == userType && merchNo.equals(userName)) {
            moneyacct = rcMerchantDao.statMerchByNo(merchNo);
        } else if(UserType.subAgent.id() == userType) {
            moneyacct = rcMerchantDao.statMerchByAgentAndMerchNo(userName,merchNo);
        }else if(UserType.agent.id() == userType) {
            moneyacct = rcMerchantDao.statMerchByAgentLimitAndMerchNo(userName,merchNo);
        }else if(UserType.user.id() == userType) {
            moneyacct = rcMerchantDao.statMerchByNo(merchNo);
        }
        this.merchParamToMoneyacct(merchNo, moneyacct);
        this.mearchMoneyacctValue(moneyacct);
        if(moneyacct != null) {
            model.addAttribute("moneyacct", moneyacct);
        }
        return "moneyacct/merchantDetail";
    }
    
    @ResponseBody
    @GetMapping("/merchant/detailList")
    public PageUtils merchantDetailList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        UserDO user = ShiroUtils.getUser();
        Query query = new Query(params);
        query.put("userType", user.getUserType());
        query.put("username", user.getUsername());
        List<RecordMerchBalDO> rcMerchBalList = rcMerchantDao.findMerchantDetailList(query);
        int total = rcMerchantDao.findMerchantDetailListCount(query);
        PageUtils pageUtils = new PageUtils(rcMerchBalList, total);
        return pageUtils;
    }

    @ResponseBody
    @PostMapping("/merchant/detailList/footer")
    public R merchantDetailListListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate, @RequestParam Map<String, Object> params){
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        //查询列表数据
        UserDO user = ShiroUtils.getUser();
        params.put("userType", user.getUserType());
        params.put("username", user.getUsername());
        FooterDO fdo = rcMerchantDao.findMerchantDetailListFooter(params);
        return R.okData(fdo);
    }
    
    
    //*****************************代理商钱包
    @GetMapping("/agent")
    @RequiresPermissions("moneyacct:agent")
    public String agent(Model model){
        UserDO user = ShiroUtils.getUser();
        Integer userType = user.getUserType();
        String userName = user.getUsername();
        if(UserType.subAgent.id() == userType) {
            model.addAttribute("agentNumber", user.getUsername());
        }else if(UserType.agent.id() == userType) {
            model.addAttribute("agentNumber", user.getUsername());
            model.addAttribute("twoAgents", agentDao.findAgentByParent(userName));
        }else if(UserType.merch.id() == userType) {
            model.addAttribute("merchNo", userName);
        }else if(UserType.user.id() == userType) {
            model.addAttribute("oneAgents", agentDao.findOneLevelAgent());
        }
        return "moneyacct/agent";
    }
    
    @ResponseBody
    @GetMapping("/agent/list")
    @RequiresPermissions("moneyacct:agent")
    public PageUtils agentList(@RequestParam Map<String, Object> params){
        UserDO user = ShiroUtils.getUser();
        Query query = new Query(params);
        query.put("userType", user.getUserType());
        query.put("username", user.getUsername());
        List<MoneyacctDO> moneyaccts = new ArrayList<>();
        String subAgent = (String) params.get("subAgent");
        int total = 0;
        if(UserType.merch.id() == user.getUserType()) {
            
        }else if(ParamUtil.isNotEmpty(subAgent)) {
            MoneyacctDO moneyacct = rcFoundBalDao.findOneAgent(query);
            if(moneyacct!=null) {
                this.agentParamToMoneyacct(subAgent, moneyacct);
                moneyaccts.add(moneyacct);
            }
        }else {
            moneyaccts = rcFoundBalDao.findAgentList(query);
            for (MoneyacctDO moneyacctDO : moneyaccts) {
                this.agentParamToMoneyacct(moneyacctDO.getAgentNo(), moneyacctDO);
            }
            total = rcFoundBalDao.findAgentListCount(query);
        }
        if(total == 0) {
            total = moneyaccts.size();
        }
        for (MoneyacctDO moneyacctDO : moneyaccts) {
        	Agent agent = agentService.get(moneyacctDO.getAgentNo());
        	moneyacctDO.setName(agent.getAgentName());
		}
        PageUtils pageUtils = new PageUtils(moneyaccts, total);
        return pageUtils;
    }

    @GetMapping("/agent/detail/{agentNo}")
    @RequiresPermissions("moneyacct:agent")
    public String agentDetail(@PathVariable("agentNo") String agentNo,Model model){
        model.addAttribute("agentNo", agentNo);
        model.addAttribute("feeTypes", FeeType.agentDesc());
        model.addAttribute("orderTypes", OrderType.desc());
        UserDO user = ShiroUtils.getUser();
        MoneyacctDO moneyacct = new MoneyacctDO().initZero();
        model.addAttribute("moneyacct", moneyacct);
        if(ParamUtil.isEmpty(agentNo) || UserType.merch.id() == user.getUserType()) {
            return "moneyacct/agent"; 
        }
        Query query = new Query();
        query.put("userType", user.getUserType());
        query.put("username", user.getUsername());
        query.put("subAgent", agentNo);
        moneyacct = rcFoundBalDao.findOneAgent(query);
        this.agentParamToMoneyacct(agentNo, moneyacct);
        this.mearchMoneyacctValue(moneyacct);
        if(moneyacct != null) {
            model.addAttribute("moneyacct", moneyacct);
        }
        return "moneyacct/agentDetail";
    }
    
    @GetMapping("/agent/detail")
    @RequiresPermissions("moneyacct:agent:detail")
    public String agentDetail(Model model){
    	UserDO u = ShiroUtils.getUser();
    	String agentNo = u.getUsername();
        model.addAttribute("agentNo", agentNo);
        model.addAttribute("feeTypes", FeeType.agentDesc());
        model.addAttribute("orderTypes", OrderType.desc());
        UserDO user = ShiroUtils.getUser();
        MoneyacctDO moneyacct = new MoneyacctDO().initZero();
        model.addAttribute("moneyacct", moneyacct);
        if(ParamUtil.isEmpty(agentNo) || UserType.merch.id() == user.getUserType()) {
            return "moneyacct/agent"; 
        }
        Query query = new Query();
        query.put("userType", user.getUserType());
        query.put("username", user.getUsername());
        query.put("subAgent", agentNo);
        moneyacct = rcFoundBalDao.findOneAgent(query);
        this.agentParamToMoneyacct(agentNo, moneyacct);
        this.mearchMoneyacctValue(moneyacct);
        if(moneyacct != null) {
            model.addAttribute("moneyacct", moneyacct);
        }
        return "moneyacct/agentDetail";
    }

    @ResponseBody
    @GetMapping("/agent/detailList")
    public PageUtils agentDetailList(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate,@RequestParam Map<String, Object> params){
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        UserDO user = ShiroUtils.getUser();
        Query query = new Query(params);
        query.put("userType", user.getUserType());
        if(StringUtils.isBlank(params.get("agentNo").toString())) {
            query.put("username", user.getUsername());
        }
        List<RecordFoundAcctDO> rcMerchBalList = rcFoundBalDao.findAgentDetailList(query);
        int total = rcFoundBalDao.findAgentDetailListCount(query);
        PageUtils pageUtils = new PageUtils(rcMerchBalList, total);
        return pageUtils;
    }

    @ResponseBody
    @PostMapping("/agent/detailList/footer")
    public R agentDetailListListFooter(@RequestParam("beginDate") Date beginDate, @RequestParam("endDate") Date endDate, @RequestParam Map<String, Object> params){
        if(ParamUtil.isNotEmpty(beginDate)){
            params.put("beginDate", DateUtil.getBeginTimeIntZero(beginDate));
        }
        if(ParamUtil.isNotEmpty(endDate)){
            params.put("endDate", DateUtil.getEndTimeIntLast(endDate));
        }
        //查询列表数据
        UserDO user = ShiroUtils.getUser();
        params.put("userType", user.getUserType());
        params.put("username", user.getUsername());
        FooterDO fdo = rcFoundBalDao.findAgentDetailListFooter(params);
        return R.okData(fdo);
    }
    
    private void payMerchParamToMoneyacct(String payCompany, String payMerch, MoneyacctDO moneyacct) {
        PayAcctBal merchBal = RedisUtil.getPayMerchBal(payCompany, payMerch);
        if(merchBal != null && moneyacct != null) {
            moneyacct.setMerchNo(merchBal.getUsername());
            moneyacct.setBalance(merchBal.getBalance());
            moneyacct.setAvailBal(merchBal.getAvailBal());
        }
        
    }
    
    private void agentParamToMoneyacct(String subAgent, MoneyacctDO moneyacct) {
        PayAcctBal merchBal = RedisUtil.getAgentBal(subAgent);
        if(merchBal != null && moneyacct != null) {
            moneyacct.setAgentNo(merchBal.getUsername());
            moneyacct.setBalance(merchBal.getBalance());
            moneyacct.setAvailBal(merchBal.getAvailBal());
        }
        
    }
    
    private void merchParamToMoneyacct(String merchNo, MoneyacctDO moneyacct) {
        PayAcctBal merchBal = RedisUtil.getMerchBal(merchNo);
        if(merchBal != null && moneyacct != null) {
            moneyacct.setMerchNo(merchBal.getUsername());
            moneyacct.setBalance(merchBal.getBalance());
            moneyacct.setAvailBal(merchBal.getAvailBal());
        }
    }
    
    private void foundParamToMoneyacct(MoneyacctDO moneyacct) {
    	PayAcctBal foundBal = RedisUtil.getPayFoundBal();
        if(foundBal != null && moneyacct != null) {
        	moneyacct.setMerchNo(foundBal.getUsername());
            moneyacct.setBalance(foundBal.getBalance());
            moneyacct.setAvailBal(foundBal.getAvailBal());
            mearchMoneyacctValue(moneyacct);
        }
    }
    
    private void mearchMoneyacctValue(MoneyacctDO moneyacct) {
        if(moneyacct != null) {
            moneyacct.setForClear(moneyacct.getBalance().subtract(moneyacct.getAvailBal()));
            moneyacct.setInTrading(BigDecimal.ZERO);
        }
    }




    //跳转修改商户金额
    @GetMapping("/merchant/addMoney/{merchNo}")
    public String addMoney(@PathVariable("merchNo")String merchNo, Model model) {
        UserDO u = ShiroUtils.getUser();
        Integer type = u.getUserType();
        System.out.println("-------------------------->"+type);
        model.addAttribute("merchNo",merchNo);
        return "moneyacct/addMerchantMoney";
    }

    //修改商户金额
    @ResponseBody
    @PostMapping("/merchant/changeMoney/{merchNo}/{money}")
    public R changeMoney(@PathVariable("merchNo")String merchNo, @PathVariable("money") String money, @RequestParam("fundPassword")String fundPassword, HttpSession session){
        //验证资金密码
        System.out.println("资金密码:-------------------"+fundPassword);
        R r = PasswordCheckUtils.check(fundPassword,session.getAttribute("username").toString());
        if(R.ifError(r)){
            System.out.println("修改金额验证密码错误");
            return r;
        }
        System.out.println("--------------------changeMoney:"+money);
        UserDO u = ShiroUtils.getUser();
        if(u.getUserType()!=0){
            return  R.error("您不是管理员不能修改金额");
        }
        PayAcctBal pab = RedisUtil.getMerchBal(merchNo);
        Order order=new Order();
        order.setOrderNo("change"+new Random().nextInt(10000));
        order.setMerchNo(merchNo);
        order.setCrtDate(DateUtil.getCurrentTimeInt());
        if(new BigDecimal(money).intValue()>=0){
            // 增加商户余额
            RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchAdd(order, new BigDecimal(money),
                    FeeType.changeMoney.id(),  OrderType.change.id());
            rdMerchBal.setCrtDate(order.getCrtDate());
            rdMerchBal.setChangeUser(u.getUsername());
            rdMerchBalDao.save(rdMerchBal);
            //增加商户可用余额
            rdMerchBal = payHandlerService.availBalForMerchAdd(order, new BigDecimal(money),
                    FeeType.changeMoney.id(),  OrderType.change.id());
            rdMerchBal.setCrtDate(order.getCrtDate());
            rdMerchBal.setChangeUser(u.getUsername());
            rdMerchAvailBalDao.save(rdMerchBal);
        }else {
            // 减少商户余额
            BigDecimal changeMoney=new BigDecimal(money.substring(1,money.length()));
            RecordMerchBalDO rdMerchBal = payHandlerService.balForMerchSub(order,changeMoney,
                    FeeType.changeMoney.id(),  OrderType.change.id());
            rdMerchBal.setCrtDate(order.getCrtDate());
            rdMerchBal.setChangeUser(u.getUsername());
            rdMerchBalDao.save(rdMerchBal);
            //减少商户可用余额
            rdMerchBal = payHandlerService.availBalForMerchSub(order, changeMoney,
                    FeeType.changeMoney.id(),  OrderType.change.id());
            rdMerchBal.setCrtDate(order.getCrtDate());
            rdMerchBal.setChangeUser(u.getUsername());
            rdMerchAvailBalDao.save(rdMerchBal);
        }
        return R.ok();
    }

    //跳转  修改代理商金额
    @GetMapping("/agent/addMoney/{agentNo}")
    public String addAgentMoney(@PathVariable("agentNo")String agentNo, Model model) {
        UserDO u = ShiroUtils.getUser();
        Integer type = u.getUserType();
        System.out.println("-------------------------->"+type);
        model.addAttribute("agentNo",agentNo);
        return "moneyacct/addAgentMoney";
    }

    //修改代理商金额
    @ResponseBody
    @PostMapping("/agent/changeMoney/{agentNo}/{money}")
    public R changeAgentMoney(@PathVariable("agentNo")String agentNo,@PathVariable("money") String money,@RequestParam("fundPassword")String fundPassword,HttpSession session) {
        System.out.println("资金密码:-------------------"+fundPassword);
        R r = PasswordCheckUtils.check(fundPassword,session.getAttribute("username").toString());
        if(R.ifError(r)){
            System.out.println("修改金额验证密码错误");
            return r;
        }
        System.out.println("--------------------changeMoney:" + money);
        UserDO u = ShiroUtils.getUser();
        if (u.getUserType() != 0) {
            return R.error("您不是管理员不能修改金额");
        }
        Agent agent = agentService.get(agentNo);
        Order order = new Order();
        order.setOrderNo("change" + new Random().nextInt(10000));
        order.setMerchNo(" ");
        order.setCrtDate(DateUtil.getCurrentTimeInt());
        if (new BigDecimal(money).intValue() >= 0) {
            RecordFoundAcctDO rdFoundAcct = null;
            rdFoundAcct = payHandlerService.availBalForAgentAdd(order,new BigDecimal(money), agent.getAgentNumber(), FeeType.changeMoney.id(),OrderType.change.id());
            rdFoundAcct.setCrtDate(order.getCrtDate());
            rdFoundAcct.setChangeUser(u.getUsername());
            rdFoundAvailAcctDao.save(rdFoundAcct);

            rdFoundAcct = payHandlerService.balForAgentAdd(order,new BigDecimal(money), agent.getAgentNumber(), FeeType.changeMoney.id(),OrderType.change.id());
            rdFoundAcct.setCrtDate(order.getCrtDate());
            rdFoundAcct.setChangeUser(u.getUsername());
            rdFoundAcctDao.save(rdFoundAcct);
        }else {
            RecordFoundAcctDO rdFoundAcct = null;
            BigDecimal changeMoney=new BigDecimal(money.substring(1,money.length()));
            rdFoundAcct = payHandlerService.availBalForAgentSub(order,changeMoney, agent.getAgentNumber(), FeeType.changeMoney.id(),OrderType.change.id());
            rdFoundAcct.setCrtDate(order.getCrtDate());
            rdFoundAcct.setChangeUser(u.getUsername());
            rdFoundAvailAcctDao.save(rdFoundAcct);
            rdFoundAcct = payHandlerService.balForAgentSub(order,changeMoney, agent.getAgentNumber(), FeeType.changeMoney.id(),OrderType.change.id());
            rdFoundAcct.setCrtDate(order.getCrtDate());
            rdFoundAcct.setChangeUser(u.getUsername());
            rdFoundAcctDao.save(rdFoundAcct);
        }
        return R.ok();
    }
}
