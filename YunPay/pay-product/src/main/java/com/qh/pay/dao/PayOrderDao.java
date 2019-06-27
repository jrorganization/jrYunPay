package com.qh.pay.dao;

import com.qh.pay.api.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 支付订单
 * @date 2017-11-14 11:32:01
 */
@Mapper
public interface PayOrderDao {

	Order get(@Param("orderNo")String orderNo);
	
	List<Order> list(Map<String,Object> map);
	
	int updateClearState(Order payOrder);
	
	int updateClearStateBatch(List<Order> orders);
	
	int save(Order payOrder);
	
	int update(Order payOrder);

	List<Order> detection();
}
