package com.qh.pay.service;

import com.qh.common.domain.Tree;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.PayAcctBal;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚富代理
 * @author Administrator
 *
 */
public interface AgentService {

	Agent get(String agentNo);

	Map<String,String> getAgentNameMap(Map<String, Object> map);

	Map<String,String> getNameMap(Map<String, Object> map);

	Agent get(Integer agentId);
	//全表数据
	Agent getById(Integer agentId);

	List<Agent> list(Map<String, Object> map);

	List<Agent> listAgentInfo(Map<String, Object> map);

	int count(Map<String, Object> map);
	
	int save(Agent agent);
	
	int update(Agent agent);
	
	int remove(String merchNos);
	
	int remove(Integer agentId);
	
	int batchRemove(String[] merchNos);
	
	int batchRemove(Integer[] agentIds);
	int batchOperate(String flag, Integer[] agentIds);
	
	int batchAudit(Map<String, Object> map);

	/**
	 * @Description 默认商户号
	 * @return
	 */
	String defaultAgentNo();

	/**
	 * @Description 是否存在
	 * @param merchNo
	 * @return
	 */
	boolean exist(String merchNo);


	/**
	 * 
	 * @Description 创建支付账户余额
	 * @param agent
	 * @return
	 */
	public static PayAcctBal createPayAcctBal(Agent agent){
		PayAcctBal payAcctBal = new PayAcctBal();
		payAcctBal.setUserId(agent.getAgentId());
		payAcctBal.setUsername(agent.getAgentNumber());
		Integer userType = agent.getLevel()==1?UserType.agent.id():UserType.subAgent.id();
		payAcctBal.setUserType(userType);
		payAcctBal.setBalance(BigDecimal.ZERO);
		payAcctBal.setAvailBal(BigDecimal.ZERO);
		return payAcctBal;
	}

	List<Agent> getAgentByLevel(int id);

	List<Agent> getAgentByParent(String parentAgent);

    List<Agent> getAgentByLevelAll(int id);


    static Map<String,String> forChoice(List<Agent> agents){
		Map<String, String> agentChoices = new HashMap<>();
		if(CollectionUtils.isNotEmpty(agents)){
			for (Agent agent : agents) {
				agentChoices.put(agent.getAgentNumber(),agent.getAgentName() + "(" + agent.getAgentNumber() + ")");
			}
		}
    	return agentChoices;
	}

    Tree<Agent> getTree();

    List<Agent> getParentAgents(String agentNo);
}
