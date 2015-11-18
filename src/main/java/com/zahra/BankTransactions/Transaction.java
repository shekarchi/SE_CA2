package com.zahra.BankTransactions;

import java.math.BigDecimal;

public class Transaction {

	private String id;
	private String type;
	private BigDecimal amount;
	private String deposit;
	private String status;
	
	public Transaction(String id, String type, BigDecimal amount, String deposit) {
		this.id = id;
		this.type = type;
		this.amount = amount;
		this.deposit = deposit;
	}
	
	@Override
    public String toString() {
        return id + "#" 
        	+ type + "#" 
        	+ amount.toString() + "#"
        	+ deposit;
    }
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public String getDeposit() {
		return deposit;
	}
	public void setDeposit(String deposit) {
		this.deposit = deposit;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
}
