package com.zahra.BankTransactions;

import java.math.BigDecimal;

public class Deposit {
	
	private String customer;
	private String id;
	private BigDecimal initialBalance;
	private BigDecimal upperBound;
	
	public String getCustomer() {
		return customer;
	}
	public void setCustomer(String customer) {
		this.customer = customer;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public BigDecimal getInitialBalance() {
		return initialBalance;
	}
	public void setInitialBalance(BigDecimal initialBalance) {
		this.initialBalance = initialBalance;
	}
	public BigDecimal getUpperBound() {
		return upperBound;
	}
	public void setUpperBound(BigDecimal upperBound) {
		this.upperBound = upperBound;
	}
}
