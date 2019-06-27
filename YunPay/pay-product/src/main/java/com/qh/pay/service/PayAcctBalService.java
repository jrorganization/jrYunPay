package com.qh.pay.service;

import com.qh.pay.domain.PayAcctBal;
import com.qh.system.domain.UserDO;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 * 账号余额表
 * 
 * @date 2017-11-06 11:41:35
 */
public interface PayAcctBalService {

	List<PayAcctBal> list();

	List<PayAcctBal> list(int userType);

	List<PayAcctBal> listBlur(String username);

	List<PayAcctBal> listBlur(int userType,String username);

	int count(String key);

	static PayAcctBal createPayAcctBal(UserDO user){
		PayAcctBal payAcctBal = new PayAcctBal();
		payAcctBal.setUserId(user.getUserId());
		payAcctBal.setUsername(user.getUsername());
		payAcctBal.setUserType(user.getUserType());
		payAcctBal.setAvailBal(BigDecimal.ZERO);
		payAcctBal.setBalance(BigDecimal.ZERO);
		payAcctBal.setFreezeBal(BigDecimal.ZERO);
		payAcctBal.setCompanyPayAvailBal(new HashMap<>());
		payAcctBal.setTotalIncome(BigDecimal.ZERO);
		payAcctBal.setTotalPoundage(BigDecimal.ZERO);
		payAcctBal.setTotalSpending(BigDecimal.ZERO);
		return payAcctBal;
	}

	PayAcctBal saveFromUser(UserDO user);
}
