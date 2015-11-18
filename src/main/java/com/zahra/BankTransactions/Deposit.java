package com.zahra.BankTransactions;

import java.math.BigDecimal;

public class Deposit {
	
	private String customer;
	private String id;
	private BigDecimal initialBalance;
	private BigDecimal upperBound;
	
	public Deposit(String customer, String id, BigDecimal initialBalance, BigDecimal upperBound) {
		this.customer = customer;
		this.id = id;
		this.initialBalance = initialBalance;
		this.upperBound = upperBound;
	}
	
	public void applyRequestOnDepo(String requestType, BigDecimal value) throws Exception{
		try {
			if(requestType.equals("deposit")) {
				increaseBalance(value);
			}
			else if (requestType.equals("withdraw")) {
				decreaseBalance(value);
			}
			else {
				throw new InvalidRequestException();
			}
		} catch(Exception e) {
			throw e;
		}
	}
	
	private void increaseBalance (BigDecimal val) throws Exception{
		if(val.compareTo(new BigDecimal(0)) < 0)
			throw new NegativeAmountException();
		BigDecimal tempBalance = initialBalance;
		if(tempBalance.add(val).compareTo(upperBound) > 0)
			throw new UpperBoundExceededException();
		initialBalance.add(val);
	}
	
	private void decreaseBalance (BigDecimal val) throws Exception{
		if(val.compareTo(new BigDecimal(0)) < 0)
			throw new NegativeAmountException();
		BigDecimal tempBalance = initialBalance;
		if(tempBalance.subtract(val).compareTo(new BigDecimal(0)) < 0)
			throw new LowerBoundExceededException();
		initialBalance.subtract(val);
	}
	
	public String getCustomer() {
		synchronized (this) {
			return customer;
		}
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

class UpperBoundExceededException extends Exception {
	public String toString(){ 
	       return ("UpperBoundExceededException") ;
	    }
	
}
class NegativeAmountException extends Exception {
	public String toString(){ 
	       return ("NegativeIncreamentException") ;
	    }
	
}
class LowerBoundExceededException extends Exception {
	public String toString(){ 
	       return ("LowerBoundExceededException") ;
	    }
	
}
class InvalidRequestException extends Exception {
	public String toString(){ 
	       return ("InvalidRequestException") ;
	    }
	
}