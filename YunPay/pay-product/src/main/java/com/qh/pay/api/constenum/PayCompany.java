package com.qh.pay.api.constenum;

import com.qh.common.config.Constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName PayCompany
 * @Description 支付公司
 * @Date 2017年11月9日 上午9:44:10
 * @version 1.0.0
 */
public enum PayCompany {
	/**银生宝   快捷 网关  代付**/
	ysb,
	/**聚富****/
	jf,
	/**mofangceo***/
	mofang,
	/**测试**/
	Vnet,
	/**熊猫**/
	xiongmao,
	/** 方块**/
	fangkuai,
	/**  泰山 **/
	taishan,
	/**  Beagle  **/
	Beagle,
	/** weisao **/
	Weisao,
	/**BC**/
	BC,
	/** 玩球球 **/
	wqq,
	/** 云pay  **/
	yunpay
	;
	private static final Map<String,String> descMap = new HashMap<>(10);

	private static final Map<String,PayCompany> enumMap = new HashMap<>(10);

	static{
		descMap.put(mofang.name(), "mofang");
		enumMap.put(mofang.name(), mofang);
		descMap.put(Vnet.name(),"Vnet");
		enumMap.put(Vnet.name(),Vnet);
		descMap.put(xiongmao.name(),"xiongmao");
		enumMap.put(xiongmao.name(),xiongmao);
		descMap.put(fangkuai.name(),"fangkuai");
		enumMap.put(fangkuai.name(),fangkuai);
		descMap.put(taishan.name(),"taishan");
		enumMap.put(taishan.name(),taishan);
		descMap.put(ysb.name(),"yinshengbao");
		enumMap.put(ysb.name(),ysb);
		descMap.put(Beagle.name(),"Beagle");
		enumMap.put(Beagle.name(),Beagle);
		descMap.put(Weisao.name(),"Weisao");
		enumMap.put(Weisao.name(),Weisao);
		descMap.put(BC.name(),"BC");
		enumMap.put(BC.name(),BC);
		descMap.put(wqq.name(),"wqq");
		enumMap.put(wqq.name(),wqq);
	}

	/***当前支付公司*******/
	private static final Map<String,String> jfDescMap = new HashMap<>(4);
	static{
		jfDescMap.put(jf.name(), Constant.pay_name);
		enumMap.put(jf.name(), jf);
	}

	public static Map<String, String> jfDesc() {
		return jfDescMap;
	}

	/***配置支付公司支持的卡类型****/
	private static final Map<String,Integer> companyCardTypeMap = new HashMap<>();
	static{

	}

	public static Integer companyCardType(String name){
		return companyCardTypeMap.get(name);
	}

	/***配置支付公司是否需要绑卡短信****/
	private static final Map<String,Integer> companyBindCardSMSMap = new HashMap<>();
	static{

	}

	public static Integer companyBindCardSMS(String name){
		return companyBindCardSMSMap.get(name);
	}

	/***配置支付公司是否有重发短信验证码****/
	private static final Map<String,Integer> companyResendSMSMap = new HashMap<>();
	static{

	}

	public static Integer companyResendSMS(String name){
		return companyResendSMSMap.get(name);
	}

	/***配置支付公司代付是否需要银联行号****/
	private static final Map<String,Integer> companyUnionPayNeedMap = new HashMap<>();
	static{

	}

	public static Integer companyUnionPay(String name){
		return companyUnionPayNeedMap.get(name);
	}

	public static PayCompany payCompany(String name){
		return enumMap.get(name);

	}

	/****所有通道*****************/;
	private static final Map<String,String> allDescMap = new HashMap<>(16);
	static{
		allDescMap.putAll(descMap);
		allDescMap.putAll(jfDescMap);
	}

	/**
	 * @Description 返回所有的通道
	 * @return
	 */
	public static final Map<String,String> all() {
		return allDescMap;
	}

	public static Map<String, String> desc() {
		return descMap;
	}

}
