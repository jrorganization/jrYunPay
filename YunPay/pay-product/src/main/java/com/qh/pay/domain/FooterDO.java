package com.qh.pay.domain;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 历史订单表Footer汇总信息展示
 * @Description 
 * @Author chensi
 * @Time   2018/1/3 16:40
 */
public class FooterDO implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal amount;
    private BigDecimal realAmount;
    private BigDecimal costAmount;
    private BigDecimal qhAmount;
    private BigDecimal agentAmount;
    private BigDecimal tranAmt;
    private BigDecimal subAgentAmount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(BigDecimal realAmount) {
        this.realAmount = realAmount;
    }

    public BigDecimal getCostAmount() {
        return costAmount;
    }

    public void setCostAmount(BigDecimal costAmount) {
        this.costAmount = costAmount;
    }

    public BigDecimal getQhAmount() {
        return qhAmount;
    }

    public void setQhAmount(BigDecimal qhAmount) {
        this.qhAmount = qhAmount;
    }

    public BigDecimal getAgentAmount() {
        return agentAmount;
    }

    public void setAgentAmount(BigDecimal agentAmount) {
        this.agentAmount = agentAmount;
    }

    public BigDecimal getTranAmt() {
        return tranAmt;
    }

    public void setTranAmt(BigDecimal tranAmt) {
        this.tranAmt = tranAmt;
    }

    public BigDecimal getSubAgentAmount() {
        return subAgentAmount;
    }
    
    public void setSubAgentAmount(BigDecimal subAgentAmount) {
        this.subAgentAmount = subAgentAmount;
    }
    
}
