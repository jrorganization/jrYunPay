package com.qh.pay.service.impl;


import com.qh.common.domain.Tree;
import com.qh.common.utils.BuildTree;
import com.qh.common.utils.EmptyUtil;
import com.qh.pay.api.constenum.AgentLevel;
import com.qh.pay.dao.AgentMapper;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.service.AgentService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.UserDO;
import com.qh.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentServiceImpl implements AgentService {

	@Autowired
	private AgentMapper agentDao;
	
	@Override
	public Agent get(String merchNo) {
		return agentDao.getByMerchNo(merchNo);
	}

	@Override
	public Map<String, String> getAgentNameMap(Map<String, Object> map) {
		List<Agent> agents =  agentDao.list(map);
		Map<String,String> agentNameMap = new HashMap<>();
		for(Agent agent:agents){
			agentNameMap.put(""+agent.getAgentNumber(),agent.getAgentName());
		}
		return agentNameMap;
	}

	@Override
	public Map<String, String> getNameMap(Map<String, Object> map) {
		List<Agent> agents =  agentDao.list(map);
		Map<String,String> agentNameMap = new HashMap<>();
		for(Agent agent:agents){
			agentNameMap.put(agent.getAgentNumber(),agent.getAgentName().concat("--").concat(AgentLevel.desc().get(agent.getLevel())));
		}
		return agentNameMap;
	}

	@Override
	public Agent get(Integer agentId){
		return agentDao.get(agentId);
	}
	//全表数据
	@Override
	public Agent getById(Integer agentId){
		return agentDao.get(agentId);
	}


	@Override
	public List<Agent> list(Map<String, Object> map) {
		List<Agent> agents =  agentDao.list(map);
		//同步缓存中的余额
		for (Agent agent : agents) {
			syncBalanceFromCache(agent);
		}
		return agents;
	}

	@Override
	public List<Agent> listAgentInfo(Map<String, Object> map) {
		return agentDao.listAgentInfo(map);
	}

	public void syncBalanceFromCache(Agent agent){
		PayAcctBal acctBal =  (PayAcctBal) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_bal_merch, agent.getAgentNumber());
		if(acctBal != null){
			agent.setBalance(acctBal.getBalance());
		}
	}
	
	@Override
	public int count(Map<String, Object> map) {
		return agentDao.count(map);
	}

	@Override
	public int save(Agent agent) {
	
		int count = agentDao.save(agent);
		if(count>0) {
			updateRedis(agent.getAgentNumber());

		}
		return count;
	}
	@Autowired
	private UserService userService;
	@Override
	@Transactional
	public int update(Agent agent) {
		int count = agentDao.update(agent);
		if(count>0) {
			updateRedis(get(agent.getAgentId()).getAgentNumber());
			UserDO userDO = userService.get(agent.getAgentId());
			userDO.setMobile(agent.getContactsPhone());
			userDO.setEmail(agent.getContactsEmail());
			userService.update(userDO);
		}
		return count;
	}

	@Override
	public int remove(String merchNos) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int remove(Integer agentId){
		return agentDao.remove(agentId);
	}

	@Override
	public int batchRemove(String[] merchNos) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int batchRemove(Integer[] agentIds){
		return agentDao.batchRemove(agentIds);
	}
	
	@Override
	public int batchOperate(String flag,Integer[] agentIds){
		int count = 0;
		if("1".equals(flag)){
			count = agentDao.batchqiyong(agentIds);
		}else{
			count = agentDao.batchjinyong(agentIds);
		}
		if(count >0 ) {
			updateRedis(get(agentIds[0]).getAgentNumber());
		}
		return count;
	}

	@Override
	public String defaultAgentNo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exist(String agentNumber) {
		return agentDao.exist(agentNumber)>0;
	}


	@Override
	public List<Agent> getAgentByLevel(int level) {
		return agentDao.getAgentByLevel(level);
	}

	@Override
	public List<Agent> getAgentByParent(String parentAgent) {
		return agentDao.getAgentByParent(parentAgent);
	}

	@Override
	public List<Agent> getAgentByLevelAll(int level) {
		return agentDao.getAgentByLevelAll(level);
	}

	@Override
	public int batchAudit(Map<String, Object> map) {
		
		int count = agentDao.batchAudit(map);
		if(count > 0) {
			Integer[] agentIds = (Integer[])map.get("array");
			updateRedis(get(agentIds[0]).getAgentNumber());
		}
		return count;
	}

	private void updateRedis(String merchNo) {
		Agent agent = agentDao.getByMerchNo(merchNo);
		if(agent != null){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_agent, merchNo, agent);
		}
	}

	@Override
	public Tree<Agent> getTree() {
		List<Tree<Agent>> trees = new ArrayList<>();
		List<Agent> agents = agentDao.getAgentLimitLevel(AgentLevel.max_level);
		for (Agent agent : agents) {
			Tree<Agent> tree = new Tree<>();
			tree.setId(agent.getAgentNumber());
			tree.setParentId(agent.getParentAgent());
			tree.setText(agent.getAgentName() + "(" + agent.getAgentNumber() + ")");
			Map<String, Object> state = new HashMap<>(32);
			state.put("opened", true);
			tree.setState(state);
			trees.add(tree);
		}
		// 默认顶级菜单为０，根据数据库实际情况调整
		Tree<Agent> t = BuildTree.build(trees);
		return t;
	}

	@Override
	public List<Agent> getParentAgents(String agentNo) {
		List<Agent> agents = new ArrayList<Agent>();
		Agent agent = agentDao.getByMerchNo(agentNo);
		if(agent!=null){
			agents.add(agent);
		}
		while(agent != null && EmptyUtil.isNotEmpty(agent.getParentAgent())){
			agent = agentDao.getByMerchNo(agent.getParentAgent());
			if(agent!=null){
				agents.add(agent);
			}
		}
		return agents;
	}

}
