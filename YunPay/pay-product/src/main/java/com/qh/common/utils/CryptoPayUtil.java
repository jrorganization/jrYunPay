package com.qh.common.utils;

import com.qh.common.config.CfgKeyConst;
import com.qh.pay.api.constenum.YesNoType;
import com.qh.pay.api.utils.Md5Util;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.domain.ConfigDO;

/**
 * @ClassName CryptoPayUtil
 * @Description 支付工具类
 * @Date 2017年10月31日 上午11:44:01
 * @version 1.0.0
 */
public class CryptoPayUtil {
	/**
	 * cryptopay密钥
	 */
	private static final String cryptopayKey = Md5Util.MD5("chgdx");

	/**
	 * 商户号前缀
	 */
	private static String merchNoPrefix = ""; 
	/**
	 * 代理商户号前缀
	 */
	private static String agentNoPrefix = ""; 
	
	/***
	 * 加密支付平台 公钥
	 */
	private static String cryptoPayPubKey = "";
	
	/**
	 * 加密支付平台
	 */
	private static String cryptoPayPriKey = "";
	
	/**
	 * 
	 * @Description 获取商户号前缀
	 * @return
	 */
	public static String getMerchNoPrefix(){
		ConfigDO config = (ConfigDO)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_config, "merchNoPrefix");
		if(config != null && Integer.valueOf(YesNoType.not.id()).equals(config.getIsClose())) {
			CryptoPayUtil.merchNoPrefix = config.getConfigValue();
		}
		if(EmptyUtil.isEmpty(CryptoPayUtil.merchNoPrefix)){
			return "SH";
		}
		return CryptoPayUtil.merchNoPrefix;
	}


	/**
	 * 
	 * @Description 获取代理前缀
	 * @return
	 */
	public static String getAgentNoPrefix(){
		ConfigDO config = (ConfigDO)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_config, "agentNoPrefix");
		if(config != null && Integer.valueOf(YesNoType.not.id()).equals(config.getIsClose())) {
			CryptoPayUtil.agentNoPrefix = config.getConfigValue();
		}
		if(EmptyUtil.isEmpty(CryptoPayUtil.agentNoPrefix)){
			return "DL";
		}
		return CryptoPayUtil.agentNoPrefix;
	}

	/**
	 * 
	 * @Description 加密
	 * @param content
	 * @return
	 */
	public static String encrypt(String content) throws Exception {
		return AESUtil.encryptData(content, cryptopayKey);
	}
	
	/**
	 * 
	 * @Description 解密
	 * @param result
	 * @return
	 */
	public static String decrypt(String result) throws Exception {
		return AESUtil.decryptData(result, cryptopayKey);
	};

	/**
	 * 
	 * @Description 获取平台私钥
	 * @return
	 */
	public static String getCryptoPayPriKey() throws Exception {
		if(EmptyUtil.isNotEmpty(CryptoPayUtil.cryptoPayPriKey)){
			return CryptoPayUtil.cryptoPayPriKey;
		}
		ConfigDO config = (ConfigDO)RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_config, CfgKeyConst.privateKeyPath);
		if(config != null && Integer.valueOf(YesNoType.not.id()).equals(config.getIsClose())) {
			CryptoPayUtil.cryptoPayPriKey = ParamUtil.readTxtFileFilter(config.getConfigValue());
		}
		return CryptoPayUtil.cryptoPayPriKey;
	}

}
