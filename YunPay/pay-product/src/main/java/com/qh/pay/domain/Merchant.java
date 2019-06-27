package com.qh.pay.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class Merchant implements Serializable{
	private static final long serialVersionUID = 1L;

	//id主键
	private Integer userId;
	//商户号 JFSH00000001
	private String merchNo;
	//RSA公钥
	private String publicKey;
	//
	private Date crtTime;
	//状态  1 启用  0禁用
	private Integer status;
	//审核状态  1通过  0 待审核  2 不通过
	private Integer auditStatus;
	//支付通道分类(走哪种分类的通道)
	private Integer payChannelType;
	//上级代理商户号   [商户开户信息开始]
	private String parentAgent;
	//商户名称   [商户联系人信息开始]
	private String merchantsName;
	//联系人
	private String contacts;
	//联系人电话
	private String contactsPhone;
	//联系人邮箱
	private String contactsEmail;
	//联系人QQ   [商户联系人信息结束]
	private String contactsQq;
	//商户logo地址
	private String logoUrl;
	//币开关
	private Map<String,String> coinSwitch;
	//币费率(手续费)
	private Map<String,Map<String,String>> coinRate;
	//客户费率(商户自己设置)
	private Map<String,BigDecimal> custRate;
	//代付CNY最小额度
	private Integer acpCnyMin;
	//代付CNY最大额度
	private Integer acpCnyMax;
	//代付CNY最小额度
	private Integer acpUsdtMin;
	//代付CNY最大额度
	private Integer acpUsdtMax;

	private BigDecimal balance;

	private Map<String,String> payCompany;

	public Map<String, String> getPayCompany() {
		return payCompany;
	}

	public void setPayCompany(Map<String, String> payCompany) {
		this.payCompany = payCompany;
	}

	/**
	 * 设置：id主键
	 */
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	/**
	 * 获取：id主键
	 */
	public Integer getUserId() {
		return userId;
	}
	/**
	 * 设置：商户号 JFSH00000001
	 */
	public void setMerchNo(String merchNo) {
		this.merchNo = merchNo;
	}
	/**
	 * 获取：商户号 JFSH00000001
	 */
	public String getMerchNo() {
		return merchNo;
	}
	/**
	 * 设置：RSA公钥
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	/**
	 * 获取：RSA公钥
	 */
	public String getPublicKey() {
		return publicKey;
	}
	/**
	 * 设置：
	 */
	public void setCrtTime(Date crtTime) {
		this.crtTime = crtTime;
	}
	/**
	 * 获取：
	 */
	public Date getCrtTime() {
		return crtTime;
	}
	/**
	 * 设置：状态  1 启用  0禁用
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	/**
	 * 获取：状态  1 启用  0禁用
	 */
	public Integer getStatus() {
		return status;
	}
	/**
	 * 设置：审核状态  1通过  0 待审核  2 不通过
	 */
	public void setAuditStatus(Integer auditStatus) {
		this.auditStatus = auditStatus;
	}
	/**
	 * 获取：审核状态  1通过  0 待审核  2 不通过
	 */
	public Integer getAuditStatus() {
		return auditStatus;
	}
	/**
	 * 设置：支付通道分类(走哪种分类的通道)
	 */
	public void setPayChannelType(Integer payChannelType) {
		this.payChannelType = payChannelType;
	}
	/**
	 * 获取：支付通道分类(走哪种分类的通道)
	 */
	public Integer getPayChannelType() {
		return payChannelType;
	}
	/**
	 * 设置：上级代理商户号   [商户开户信息开始]
	 */
	public void setParentAgent(String parentAgent) {
		this.parentAgent = parentAgent;
	}
	/**
	 * 获取：上级代理商户号   [商户开户信息开始]
	 */
	public String getParentAgent() {
		return parentAgent;
	}
	/**
	 * 设置：商户名称   [商户联系人信息开始]
	 */
	public void setMerchantsName(String merchantsName) {
		this.merchantsName = merchantsName;
	}
	/**
	 * 获取：商户名称   [商户联系人信息开始]
	 */
	public String getMerchantsName() {
		return merchantsName;
	}
	/**
	 * 设置：联系人
	 */
	public void setContacts(String contacts) {
		this.contacts = contacts;
	}
	/**
	 * 获取：联系人
	 */
	public String getContacts() {
		return contacts;
	}
	/**
	 * 设置：联系人电话
	 */
	public void setContactsPhone(String contactsPhone) {
		this.contactsPhone = contactsPhone;
	}
	/**
	 * 获取：联系人电话
	 */
	public String getContactsPhone() {
		return contactsPhone;
	}
	/**
	 * 设置：联系人邮箱
	 */
	public void setContactsEmail(String contactsEmail) {
		this.contactsEmail = contactsEmail;
	}
	/**
	 * 获取：联系人邮箱
	 */
	public String getContactsEmail() {
		return contactsEmail;
	}
	/**
	 * 设置：联系人QQ   [商户联系人信息结束]
	 */
	public void setContactsQq(String contactsQq) {
		this.contactsQq = contactsQq;
	}
	/**
	 * 获取：联系人QQ   [商户联系人信息结束]
	 */
	public String getContactsQq() {
		return contactsQq;
	}
	/**
	 * 设置：商户logo地址
	 */
	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}
	/**
	 * 获取：商户logo地址
	 */
	public String getLogoUrl() {
		return logoUrl;
	}

	public Map<String, String> getCoinSwitch() {
		return coinSwitch;
	}

	public void setCoinSwitch(Map<String, String> coinSwitch) {
		this.coinSwitch = coinSwitch;
	}

	public Map<String, Map<String, String>> getCoinRate() {
		return coinRate;
	}

	public void setCoinRate(Map<String, Map<String, String>> coinRate) {
		this.coinRate = coinRate;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Map<String, BigDecimal> getCustRate() {
		return custRate;
	}

	public void setCustRate(Map<String, BigDecimal> custRate) {
		this.custRate = custRate;
	}

	public Integer getAcpCnyMin() {
		return acpCnyMin;
	}

	public void setAcpCnyMin(Integer acpCnyMin) {
		this.acpCnyMin = acpCnyMin;
	}

	public Integer getAcpCnyMax() {
		return acpCnyMax;
	}

	public void setAcpCnyMax(Integer acpCnyMax) {
		this.acpCnyMax = acpCnyMax;
	}

	public Integer getAcpUsdtMin() {
		return acpUsdtMin;
	}

	public void setAcpUsdtMin(Integer acpUsdtMin) {
		this.acpUsdtMin = acpUsdtMin;
	}

	public Integer getAcpUsdtMax() {
		return acpUsdtMax;
	}

	public void setAcpUsdtMax(Integer acpUsdtMax) {
		this.acpUsdtMax = acpUsdtMax;
	}
}
