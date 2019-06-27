package com.qh.pay.service.impl;

import com.qh.pay.api.utils.DateUtil;
import com.qh.pay.api.utils.ParamUtil;
import com.qh.pay.dao.PayConfigCompanyDao;
import com.qh.pay.domain.PayConfigCompanyDO;
import com.qh.pay.service.PayConfigCompanyService;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * 
 * @ClassName PayConfigCompanyServiceImpl
 * @Description 支付通道配置  手动缓存
 * @Date 2017年11月6日 下午5:30:25
 * @version 1.0.0
 */
@Service
public class PayConfigCompanyServiceImpl implements PayConfigCompanyService {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PayConfigCompanyServiceImpl.class);
	
	@Autowired
	private PayConfigCompanyDao payConfigCompanyDao;

	@Override
	public List<PayConfigCompanyDO> getByChannel(String outChannel) {
		return payConfigCompanyDao.getByChannel(outChannel);
	}

	@Override
	public PayConfigCompanyDO getByCompany(String company, String outChannel) {
		PayConfigCompanyDO payCfgComp = (PayConfigCompanyDO) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_payConfigCompany + outChannel,
				company );
		if(payCfgComp == null){
			payCfgComp =  payConfigCompanyDao.getByCompany(company,outChannel);
			if(payCfgComp != null){
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_payConfigCompany + payCfgComp.getOutChannel(),
						payCfgComp.getCompany() + RedisConstants.link_symbol + payCfgComp.getPayMerch(), payCfgComp);
			}
		}
		return payCfgComp;
	}

	@Override
	public PayConfigCompanyDO get(String company,String payMerch,String outChannel){
		PayConfigCompanyDO payCfgComp = (PayConfigCompanyDO) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_payConfigCompany + outChannel,
				company + RedisConstants.link_symbol + payMerch);
		if(payCfgComp == null){
			payCfgComp =  payConfigCompanyDao.get(company,payMerch,outChannel);
			if(payCfgComp != null){
				RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_payConfigCompany + payCfgComp.getOutChannel(),
						payCfgComp.getCompany() + RedisConstants.link_symbol + payCfgComp.getPayMerch(), payCfgComp);
			}
		}
		return payCfgComp;
	}
	
	@Override
	public List<PayConfigCompanyDO> list(Map<String, Object> map){
		return payConfigCompanyDao.list(map);
	}
	
	@Override
	public int count(Map<String, Object> map){
		return payConfigCompanyDao.count(map);
	}
	
	@Override
	public PayConfigCompanyDO save(PayConfigCompanyDO payCfgCmp){
		
		//恢复redis中通道  到数据库
		/*Set<String> sets = OutChannel.desc().keySet();
		for (String string : sets) {
			List<Object> payCfgCmps = RedisUtil.getRedisTemplate().opsForHash().values(RedisConstants.cache_payConfigCompany + string);
			for (Object object : payCfgCmps) {
				
				payConfigCompanyDao.save((PayConfigCompanyDO)object);
			}
		}*/
		System.out.println("payCfgCmp.getCompany()======"+payCfgCmp.getCompany());
		PayConfigCompanyDO redisPayCfgComp = (PayConfigCompanyDO) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_payConfigCompany + payCfgCmp.getOutChannel(),
				payCfgCmp.getCompany() + RedisConstants.link_symbol + payCfgCmp.getPayMerch());
		if(redisPayCfgComp != null){
			logger.warn("改支付配置已经存在！");
			return payCfgCmp;
		}
		if(ParamUtil.isNotEmpty(payCfgCmp.getPayPeriod())){
			payCfgCmp.setPayPeriod(DateUtil.timeFormatToInt(payCfgCmp.getPayPeriod()));
		}
		int count = payConfigCompanyDao.save(payCfgCmp);
		if(count > 0){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_payConfigCompany + payCfgCmp.getOutChannel(),
					payCfgCmp.getCompany() + RedisConstants.link_symbol + payCfgCmp.getPayMerch(), payCfgCmp);
		}
		return payCfgCmp;
	}
	
	@Override
	public PayConfigCompanyDO update(PayConfigCompanyDO payCfgCmp){
		if(ParamUtil.isNotEmpty(payCfgCmp.getPayPeriod())){
			payCfgCmp.setPayPeriod(DateUtil.timeFormatToInt(payCfgCmp.getPayPeriod()));
		}
		int count = payConfigCompanyDao.update(payCfgCmp);
		if(count > 0){
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_payConfigCompany + payCfgCmp.getOutChannel(),
					payCfgCmp.getCompany() + RedisConstants.link_symbol + payCfgCmp.getPayMerch(), payCfgCmp);
		}
		return payCfgCmp;
	}
	
	@Override
	public int remove(String company,String payMerch,String outChannel){
		RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_payConfigCompany + outChannel, 
				company + RedisConstants.link_symbol + payMerch);
		return payConfigCompanyDao.remove(company,payMerch,outChannel);
	}

	/**
	 * 批量删除
	 */
	@Override
	public int batchRemove(String[] companys, String[] payMerchs, String[] outChannels) {
		int len = companys.length;
		if(len > 0){
			for(int i = 0; i<len ; i++){
				remove(companys[i], payMerchs[i], outChannels[i]);
			}
		}
		return len;
	}

	/**
	 * 根据渠道查询配置信息
	 */
	@Override
	public List<Object> getPayCfgCompByOutChannel(String outChannel) {
		return RedisUtil.getRedisTemplate().opsForHash().values(RedisConstants.cache_payConfigCompany + outChannel);
	}
}
