package com.broadeast.bean;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户列表Bean
 * @author xyzhang
 *
 */
public class UserpaymentInfoBean {
	private String userName;//联系电话,用户名
	private BigDecimal payAmount;//充值金额
	private int buyNum;//购买数量
	private String payName;//充值类型
	private Date createTime;//注册时间
	private BigDecimal totalAmount;//充值总金额
	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public BigDecimal getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(BigDecimal payAmount) {
		this.payAmount = payAmount;
	}
	

	public int getBuyNum() {
		return buyNum;
	}


	public String getPayName() {
		return payName;
	}

	public void setPayName(String payName) {
		this.payName = payName;
	}

	public void setBuyNum(int buyNum) {
		this.buyNum = buyNum;
	}
	
	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	@Override
	public String toString() {
		return "UserpaymentInfoBean [userName=" + userName + ", payAmount="
				+ payAmount + ", buyNum=" + buyNum + ", payName=" + payName
				+ ", createTime=" + createTime + ", totalAmount=" + totalAmount
				+ "]";
	}

	
}
