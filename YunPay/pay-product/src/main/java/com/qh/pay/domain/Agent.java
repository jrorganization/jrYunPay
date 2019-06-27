package com.qh.pay.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

public class Agent implements Serializable{
	private static final long serialVersionUID = 1L;

	//代理商Id
	private Integer agentId;
	//代理商户号
	private String agentNumber;
	//状态  1 启用  0禁用
	private Integer status;
	//审核状态  1通过  0 待审核  2 不通过
	private Integer auditStatus;
	//代理商级别    1 一级  2 二级
	private Integer level;
	//上级代理商户号  不填，默认一级代理(最大支持二级代理)
	private String levelName;

	private String parentAgent;
	//创建时间
	private Date createTime;
	//修改时间
	private Date modifyTime;
	//代理商名称   [代理商联系人信息开始]
	private String agentName;
	//代理商简称
	private String agentShortName;
	//联系人
	private String contacts;
	//联系人电话
	private String contactsPhone;
	//联系人邮箱
	private String contactsEmail;
	//联系人QQ   [代理商联系人信息结束]
	private String contactsQq;
	//支付通道分类(走哪种分类的通道)
	private Integer payChannelType;
	//币费率
	private Map<String,Map<String,String>> coinRate;
	//余额
	private BigDecimal balance;


	public String getLevelName() {
		return levelName;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	/**
	 * 设置：
	 */
	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}
	/**
	 * 获取：
	 */
	public Integer getAgentId() {
		return agentId;
	}
	/**
	 * 设置：代理商户号
	 */
	public void setAgentNumber(String agentNumber) {
		this.agentNumber = agentNumber;
	}
	/**
	 * 获取：代理商户号
	 */
	public String getAgentNumber() {
		return agentNumber;
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
	 * 设置：代理商级别    1 一级  2 二级
	 */
	public void setLevel(Integer level) {
		this.level = level;
	}
	/**
	 * 获取：代理商级别    1 一级  2 二级
	 */
	public Integer getLevel() {
		return level;
	}
	/**
	 * 设置：上级代理商户号  不填，默认一级代理(最大支持二级代理)
	 */
	public void setParentAgent(String parentAgent) {
		this.parentAgent = parentAgent;
	}
	/**
	 * 获取：上级代理商户号  不填，默认一级代理(最大支持二级代理)
	 */
	public String getParentAgent() {
		return parentAgent;
	}
	/**
	 * 设置：创建时间
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	/**
	 * 获取：创建时间
	 */
	public Date getCreateTime() {
		return createTime;
	}
	/**
	 * 设置：修改时间
	 */
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	/**
	 * 获取：修改时间
	 */
	public Date getModifyTime() {
		return modifyTime;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getAgentShortName() {
		return agentShortName;
	}

	public void setAgentShortName(String agentShortName) {
		this.agentShortName = agentShortName;
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
	 * 设置：联系人QQ   [代理商联系人信息结束]
	 */
	public void setContactsQq(String contactsQq) {
		this.contactsQq = contactsQq;
	}
	/**
	 * 获取：联系人QQ   [代理商联系人信息结束]
	 */
	public String getContactsQq() {
		return contactsQq;
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

	public Integer getPayChannelType() {
		return payChannelType;
	}

	public void setPayChannelType(Integer payChannelType) {
		this.payChannelType = payChannelType;
	}
}
