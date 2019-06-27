package com.qh.pay.dao;


import com.qh.common.config.JsonTypeHandler;
import com.qh.pay.domain.Agent;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;
import java.util.Map;

@Mapper
public interface AgentMapper {
	
	@Select("select `agent_id`,`agent_number`,`status`,`audit_status`,`level`,`parent_agent`,`create_time`,`modify_time`,`agent_name`,`agent_short_name`,`contacts`,`contacts_phone`,`contacts_email`,`contacts_qq`,`coin_rate`,pay_channel_type "
			+ " from agent where agent_id = #{value}")
	@Results({
			@Result(column = "coin_rate", jdbcType = JdbcType.VARCHAR, property = "coinRate", typeHandler = JsonTypeHandler.class)
	})
	Agent get(Integer id);

	@Select("select `agent_id`,`agent_number`,`status`,`audit_status`,`level`,`parent_agent`,`create_time`,`modify_time`,`agent_name`,`agent_short_name`,`contacts`,`contacts_phone`,`contacts_email`,`contacts_qq`,`coin_rate`,pay_channel_type "
			+ " from agent where agent_number = #{value}")
	@Results({
			@Result(column = "coin_rate", jdbcType = JdbcType.VARCHAR, property = "coinRate", typeHandler = JsonTypeHandler.class)
	})
	Agent getByMerchNo(String merchNo);

	@Select("select `agent_id`,`agent_number`,`status`,`audit_status`,`level`,`parent_agent`,`create_time`,`modify_time`,`agent_name`,`agent_short_name`,`contacts`,`contacts_phone`,`contacts_email`,`contacts_qq`,`coin_rate`,pay_channel_type "
			+ " from agent where level = #{level} and status = 1 and audit_status = 1")
	@Results({
			@Result(column = "coin_rate", jdbcType = JdbcType.VARCHAR, property = "coinRate", typeHandler = JsonTypeHandler.class)
	})
	List<Agent> getAgentByLevel(@Param("level") int level);

	@Select("select `agent_id`,`agent_number`,`status`,`audit_status`,`level`,`parent_agent`,`create_time`,`modify_time`,`agent_name`,`agent_short_name`,`contacts`,`contacts_phone`,`contacts_email`,`contacts_qq`,`coin_rate` ,pay_channel_type "
			+ " from agent where level = #{level}")
	@Results({
			@Result(column = "coin_rate", jdbcType = JdbcType.VARCHAR, property = "coinRate", typeHandler = JsonTypeHandler.class)
	})
	List<Agent> getAgentByLevelAll(int level);

	List<Agent> list(Map<String, Object> map);

	List<Agent> listAgentInfo(Map<String, Object> map);

	int count(Map<String, Object> map);
	
	@Select("select count(1) from agent where agent_number = #{value}")
	int exist(String agentNumber);
	
	int save(Agent merchant);
	
	int update(Agent merchant);
	
	int remove(Integer id);
	
	int batchRemove(Integer[] ids);
	int batchqiyong(Integer[] ids);
	int batchjinyong(Integer[] ids);
	int batchAudit(Map<String, Object> map);

	@Select("select `agent_id`,`agent_number`,`status`,`audit_status`,`level`,`parent_agent`,`create_time`,`modify_time`,`agent_name`,`agent_short_name`,`contacts`,`contacts_phone`,`contacts_email`,`contacts_qq`,`coin_rate`,pay_channel_type "
			+ " from agent where parent_agent = #{parentAgent} and status = 1 and audit_status = 1")
	@Results({
			@Result(column = "coin_rate", jdbcType = JdbcType.VARCHAR, property = "coinRate", typeHandler = JsonTypeHandler.class)
	})
    List<Agent> getAgentByParent(@Param("parentAgent") String parentAgent);

	@Select("select `agent_id`,`agent_number`,`status`,`audit_status`,`level`,`parent_agent`,`agent_name`,`agent_short_name`,`coin_rate`,pay_channel_type "
			+ " from agent where level < #{level} and status = 1 and audit_status = 1")
	List<Agent> getAgentLimitLevel(@Param("level") int level);
}
