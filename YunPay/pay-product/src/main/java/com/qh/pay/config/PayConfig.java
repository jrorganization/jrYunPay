package com.qh.pay.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import com.qh.pay.api.constenum.OutChannel;
import org.redisson.api.RLock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;

import com.qh.pay.api.constenum.UserType;
import com.qh.pay.dao.AgentMapper;
import com.qh.pay.dao.MerchantMapper;
import com.qh.pay.dao.PayAcctBalMapper;
import com.qh.pay.dao.PayConfigCompanyDao;
import com.qh.pay.dao.PayPropertyDao;
import com.qh.pay.domain.Agent;
import com.qh.pay.domain.Merchant;
import com.qh.pay.domain.PayAcctBal;
import com.qh.pay.domain.PayConfigCompanyDO;
import com.qh.pay.domain.PayPropertyDO;
import com.qh.pay.service.AgentService;
import com.qh.pay.service.MerchantService;
import com.qh.pay.service.PayConfigCompanyService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.redis.service.RedissonLocker;
import com.qh.system.dao.ConfigDao;
import com.qh.system.domain.ConfigDO;

/**
 * @ClassName PayConfig
 * @Description 支付相关配置
 * @Date 2017年11月6日 下午12:01:19
 * @version 1.0.0
 */
@Configuration
@Order(2)
public class PayConfig {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayConfig.class);
	
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	private RedissonLocker redissonLocker;
	
	@Autowired
	private PayAcctBalMapper payAcctBalMapper;
	
	@Autowired
	private ConfigDao configDao;
	
	@Autowired
	private PayPropertyDao payPropertyDao;
	
	@Autowired
	private MerchantMapper merchantMapper;
	
	@Autowired
	private AgentMapper agentMapper;
	
	@Autowired
	private PayConfigCompanyDao payConfigCompanyDao;
	
	@PostConstruct
	public void init(){
		
		RLock lock = redissonLocker.getLock(RedisConstants.lock_bal_foundAcct);
		if(lock.tryLock()){
			try {
				PayAcctBal acctBal =  (PayAcctBal) redisTemplate.opsForValue().get(RedisConstants.cache_bal_foundAcct);
				if(acctBal == null){
					redisTemplate.opsForValue().set(RedisConstants.cache_bal_foundAcct,payAcctBalMapper.singleByType(UserType.foundAcct.id()));
				}
			} finally {
				lock.unlock();
			}
		}
		
		List<ConfigDO> configs = configDao.list(null);
		for (ConfigDO configDO : configs) {
			RedisUtil.syncConfig(configDO, false);
		}
		
		List<PayPropertyDO> payPropertyDOs = payPropertyDao.list(null);
		for (PayPropertyDO payPropertyDO : payPropertyDOs) {
			RedisUtil.syncPayConfig(payPropertyDO);
		}
		
		List<Merchant> merchants = merchantMapper.list(null);
		PayAcctBal acctBal = null;
		for (Merchant merchant : merchants) {
			redisTemplate.opsForHash().put(RedisConstants.cache_merchant, merchant.getMerchNo(), merchant);
			lock = redissonLocker.getLock(RedisConstants.lock_bal_merch + merchant.getMerchNo());
			if(lock.tryLock()){
				try {
					acctBal = (PayAcctBal) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_bal_merch, merchant.getMerchNo());
					if(acctBal == null){
						RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_bal_merch, merchant.getMerchNo(), MerchantService.createPayAcctBal(merchant));
					}
				} finally {
					lock.unlock();
				}
			}
		}
		
		List<Agent> agents = agentMapper.list(null);
		for (Agent agent : agents) {
			redisTemplate.opsForHash().put(RedisConstants.cache_agent, agent.getAgentNumber(), agent);
			lock = redissonLocker.getLock(RedisConstants.lock_bal_agent + agent.getAgentNumber());
			if(lock.tryLock()){
				try {
					acctBal = (PayAcctBal) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_bal_agent, agent.getAgentNumber());
					if(acctBal == null){
						RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_bal_agent,agent.getAgentNumber(), AgentService.createPayAcctBal(agent));
					}
				} finally {
					lock.unlock();
				}
			}
		}

		for (String outChannel : OutChannel.all().keySet()) {
			redisTemplate.opsForHash().getOperations().delete(RedisConstants.cache_payConfigCompany + outChannel);
		}
		List<PayConfigCompanyDO> configCompanyDOs = payConfigCompanyDao.list(null);
		for (PayConfigCompanyDO payCfgCmp : configCompanyDOs) {
			redisTemplate.opsForHash().put(RedisConstants.cache_payConfigCompany + payCfgCmp.getOutChannel(),
					payCfgCmp.getCompany() + RedisConstants.link_symbol + payCfgCmp.getPayMerch(), payCfgCmp);
			String key = payCfgCmp.getCompany() + RedisConstants.link_symbol + payCfgCmp.getPayMerch();
			lock = redissonLocker.getLock(RedisConstants.lock_bal_payMerch + key);
			if(lock.tryLock()){
                try {
                    acctBal = (PayAcctBal) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_bal_payMerch, key);
                    /*if(acctBal == null){
                        RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_bal_payMerch,key, PayConfigCompanyService.createPayAcctBal(payCfgCmp));
                    }*/
                } finally {
                    lock.unlock();
                }
            }
		}
		
	}

}
