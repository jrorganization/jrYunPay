package com.qh.pay.dao;

import com.qh.common.config.JsonTypeHandler;
import com.qh.pay.domain.Merchant;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;
import java.util.Map;

@Mapper
public interface MerchantMapper {
	
	@Select("select `user_id`,`merch_no`,`public_key`,`crt_time`,`status`,`audit_status`,`pay_channel_type`,`parent_agent`,`merchants_name`,`contacts`,`contacts_phone`,`contacts_email`,`contacts_qq`,`logo_url`,`coin_switch`,`coin_rate`,cust_rate"
			+ ",acp_cny_min,acp_cny_max,acp_usdt_min,acp_usdt_max,payCompany from merchant where user_id = #{value}")
	@Results({
			@Result(column = "coin_switch", jdbcType = JdbcType.VARCHAR, property = "coinSwitch", typeHandler = JsonTypeHandler.class),
			@Result(column = "cust_rate", jdbcType = JdbcType.VARCHAR, property = "custRate", typeHandler = JsonTypeHandler.class),
			@Result(column = "coin_rate", jdbcType = JdbcType.VARCHAR, property = "coinRate", typeHandler = JsonTypeHandler.class),
			@Result(column = "payCompany", jdbcType = JdbcType.VARCHAR, property = "payCompany", typeHandler = JsonTypeHandler.class)
	})
	Merchant get(Integer id);

	@Select("select `user_id`,`merch_no`,`public_key`,`crt_time`,`status`,`audit_status`,`pay_channel_type`,`parent_agent`,`merchants_name`,`contacts`,`contacts_phone`,`contacts_email`,`contacts_qq`,`logo_url`,`coin_switch`,`coin_rate`,cust_rate"
			+ ",acp_cny_min,acp_cny_max,acp_usdt_min,acp_usdt_max,payCompany from merchant where merch_no = #{value}")
	@Results({
			@Result(column = "coin_switch", jdbcType = JdbcType.VARCHAR, property = "coinSwitch", typeHandler = JsonTypeHandler.class),
			@Result(column = "cust_rate", jdbcType = JdbcType.VARCHAR, property = "custRate", typeHandler = JsonTypeHandler.class),
			@Result(column = "coin_rate", jdbcType = JdbcType.VARCHAR, property = "coinRate", typeHandler = JsonTypeHandler.class),
			@Result(column = "payCompany", jdbcType = JdbcType.VARCHAR, property = "payCompany", typeHandler = JsonTypeHandler.class)
	})
    Merchant getByMerchNo(String merchNo);

	List<Merchant> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	@Select("select count(1) from merchant where merch_no = #{value}")
	int exist(String merchNo);

	int save(Merchant merchant);
	
	int update(Merchant merchant);

	int updateCustRate(Merchant merchant);

	int remove(Integer id);
	
	int batchRemove(Integer[] ids);
	
	int batchqiyong(Integer[] ids);

	int batchjinyong(Integer[] ids);
	
	int batchAudit(Map<String, Object> map);
	
	int removeByMerchNo(String merchNo);
	
	int batchRemoveByMerchNo(String[] merchNos);

	@Update("update merchant set public_key = #{publicKey} where merch_no = #{merchNo}")
	int updatePKey(@Param("merchNo") String merchNo, @Param("publicKey") String publicKey);
}
