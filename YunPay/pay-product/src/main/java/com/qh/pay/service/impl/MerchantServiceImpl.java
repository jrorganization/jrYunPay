package com.qh.pay.service.impl;


import com.qh.common.config.CfgKeyConst;
import com.qh.common.utils.CryptoPayUtil;
import com.qh.common.utils.EmptyUtil;
import com.qh.common.utils.ShiroUtils;
import com.qh.pay.api.constenum.UserRole;
import com.qh.pay.api.constenum.UserType;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.api.utils.RSAUtil;
import com.qh.pay.dao.MerchantMapper;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayAcctBalService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.dao.UserDao;
import com.qh.system.dao.UserRoleDao;
import com.qh.system.domain.UserDO;
import com.qh.system.domain.UserRoleDO;
import com.qh.system.service.RoleService;
import com.qh.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class MerchantServiceImpl implements MerchantService {
	@Autowired
	private MerchantMapper merchantDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private UserRoleDao userRoleDao;

	//	@Autowired
//	PayMerchantAuditService payMerchantAuditService;
	@Autowired
	private RoleService roleService;
	/**
	 * 先从缓存中获取，缓存中没有再从数据库中同步
	 */
	@Override
	public Merchant get(String merchNo) {
		return merchantDao.getByMerchNo(merchNo);
	}
	@Override
	public Merchant get(Integer userId) {
		Merchant merchant = merchantDao.get(userId);
		return merchant;
	}
	@Override
	public Merchant getById(String merchNo) {
		Merchant merchant = merchantDao.getByMerchNo(merchNo);
		return merchant;
	}

	@Override
	public Merchant getWithBalance(String merchNo) {
		Merchant merchant = get(merchNo);
		syncBalanceFromCache(merchant);
		return merchant;
	}

	@Override
	public List<Merchant> list(Map<String, Object> map){
		List<Merchant> merchants =  merchantDao.list(map);
		//同步缓存中的余额
		for (Merchant merchant : merchants) {
			syncBalanceFromCache(merchant);
		}
		return merchants;
	}

	@Override
	public Map<String, String> getMerchantNameMap(Map<String, Object> map) {
		List<Merchant> merchants =  merchantDao.list(map);
		Map<String,String> merchantNameMap = new HashMap<>();
		for(Merchant merchant:merchants){
			merchantNameMap.put(merchant.getMerchNo(),merchant.getMerchantsName());
		}
		return merchantNameMap;
	}

	public void syncBalanceFromCache(Merchant merchant){
		PayAcctBal acctBal =  (PayAcctBal) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_bal_merch, merchant.getMerchNo());
		if(acctBal != null){
			merchant.setBalance(acctBal.getBalance());
		}
	}

	@Override
	public int count(Map<String, Object> map){
		return merchantDao.count(map);
	}

	@Autowired
	private PayAcctBalService payAcctBalService;

	@Override
	@Transactional
	public int save(Merchant merchant){
		if(EmptyUtil.isEmpty(merchant.getMerchNo())){
			merchant.setMerchNo(this.defaultMerchantNo());
		}
		merchant.setAuditStatus(YesNoType.yes.id());
		//修改用户的用户名 创建者 用户状态 角色
		UserDO user = this.createUserForMerchant(merchant);
		merchant.setMerchNo(user.getUsername());
		user.setUserIdCreate(ShiroUtils.getUserId());
		user.setStatus(YesNoType.yes.id());
		List<Integer> roleIds = new ArrayList<>();
		roleIds.add(UserRole.merch.id());
		user.setRoleIds(roleIds);
		if(userService.update(user) >0){
			merchant.setUserId(user.getUserId());
			int count = merchantDao.save(merchant);
			if(count == 1){
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_merchant, merchant.getMerchNo(), merchant);
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_bal_merch, merchant.getMerchNo(), MerchantService.createPayAcctBal(merchant));
				payAcctBalService.saveFromUser(user);
			}
			return count;
		}else{
			return 0;
		}
	}

	private UserDO createUserForMerchant(Merchant merchant){
		UserDO user = new UserDO();
		user.setUserIdCreate(ShiroUtils.getUserId());
		user.setUsername(merchant.getMerchNo());
		user.setPassword(Md5Util.MD5(RedisUtil.getSysConfigValue(CfgKeyConst.pass_default_merch)));
		user.setName(merchant.getMerchantsName());
		user.setMobile(merchant.getContactsPhone());
		user.setStatus(YesNoType.yes.id());
		user.setUserType(UserType.merch.id());
		user.setEmail(merchant.getContactsEmail());
		if(userDao.save(user) > 0){
			Integer userId = user.getUserId();
			UserRoleDO ur = new UserRoleDO();
			ur.setUserId(userId);
			ur.setRoleId(UserRole.merch.id());
			userRoleDao.save(ur);
			return user;
		}else{
			return null;
		}
	}

	@Autowired
	private UserService userService;

	@Override
	@Transactional
	public int update(Merchant merchant){
		int count =  merchantDao.update(merchant);
		if(count>0) {
			this.updateRedis(merchant.getMerchNo());
			UserDO userDO = userService.get(merchant.getUserId());
			userDO.setName(merchant.getMerchantsName());
			userDO.setEmail(merchant.getContactsEmail());
			userDO.setMobile(merchant.getContactsPhone());
			userService.update(userDO);
		}
		return count;
	}

	@Override
	public int updateCustRate(Merchant merchant){
		int count =  merchantDao.updateCustRate(merchant);
		if(count>0) {
			this.updateRedis(merchant.getMerchNo());
		}
		return count;
	}

	@Override
	@Transactional
	public int remove(String merchNo){
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_merchant, merchNo);
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_bal_merch, merchNo);
		userRoleDao.removeByUsername(merchNo);
		userDao.removeByUsername(merchNo);
		return merchantDao.removeByMerchNo(merchNo);
	}

	@Override
	public int batchRemove(String[] merchNos){
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_merchant, (Object[])merchNos);
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_bal_merch, (Object[])merchNos);
		userRoleDao.batchRemoveByUsername(merchNos);
		userDao.batchRemoveByUsername(merchNos);
		return merchantDao.batchRemoveByMerchNo(merchNos);
	}

	/* (非 Javadoc)
	 * Description:
	 * @see com.yz.pay.crypto.pay.service.MerchantService#defaultMerchantNo()
	 */
	@Override
	public String defaultMerchantNo() {
		String merchNo = CryptoPayUtil.getMerchNoPrefix() + ParamUtil.generateCode6();
		while (exist(merchNo)) {
			merchNo = CryptoPayUtil.getMerchNoPrefix() + ParamUtil.generateCode6();
		}
		return merchNo;
	}


	@Override
	public boolean exist(String merchNo) {
		return merchantDao.exist(merchNo) > 0;
	}



	@Override
	public Set<Object> getAllMerchNos() {
		return RedisUtil.getRedisTemplate().opsForHash().keys(RedisConstants.cache_merchant);
	}
	@Override
	public int batchOperate(String flag,Integer[] merchantId){
		int count = 0;
		if("1".equals(flag)){
			count = merchantDao.batchqiyong(merchantId);
		}else{
			count = merchantDao.batchjinyong(merchantId);
		}
		if(count >0 ) {
			updateRedis(merchantDao.get(merchantId[0]).getMerchNo());
		}
		return count;
	}
	@Override
	public int batchAudit(Map<String, Object> map){
		int count =  merchantDao.batchAudit(map);
		if(count >0 ) {
			Integer[] merchantId = (Integer[])map.get("array");
			//审核通过自动生成秘钥
			String merchNo = merchantDao.get(merchantId[0]).getMerchNo();
			Object auditStatus = map.get("auditStatus");
			if("1".equals(String.valueOf(auditStatus))){
				try {
					map=RSAUtil.genKeyPair();
					String publicKey = RSAUtil.getPublicKey(map);
					String privateKey = RSAUtil.getPrivateKey(map);
					publicKey = publicKey.replaceAll("\r|\n", "").replaceAll(" ", "+");
					this.updatePKey(merchNo,publicKey);
					privateKey = privateKey.replaceAll("\r|\n", "").replaceAll(" ", "+");
					this.updatePrivateKey(merchNo,privateKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			this.updateRedis(merchNo);
		}
		return count;
	}

	@Override
	public int batchWithdrawal(Map<String, Object> map) {
		return 0;
	}

	@Override
	public int batchPaid(Map<String, Object> map) {
		return 0;
	}

	@Override
	public int updatePKey(String merchNo, String publicKey) {
		return merchantDao.updatePKey(merchNo,publicKey);
	}

	private void updateRedis(String merchNo) {
		Merchant merchant = merchantDao.getByMerchNo(merchNo);
		if(merchant != null){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_merchant, merchNo, merchant);
		}
	}

	@Override
	public void updatePrivateKey(String merchNo, String privateKey) {
		RedisUtil.setHashValue(CfgKeyConst.qhPrivateKey,merchNo,privateKey);
	}

	@Override
	public String getPrivateKey(String merchNo) {
		return (String)RedisUtil.getHashValue(CfgKeyConst.qhPrivateKey,merchNo);
	}
}
