package com.navin.billings.model;

public class BillPayment {
	
	private int billPaymentId;
	private Bill bill;
	private double amountApplied;
	
	
	public int getBillPaymentId() {
		return billPaymentId;
	}
	public void setBillPaymentId(int billPaymentId) {
		this.billPaymentId = billPaymentId;
	}
	public Bill getBill() {
		return bill;
	}
	public void setBill(Bill bill) {
		this.bill = bill;
	}
	public double getAmountApplied() {
		return amountApplied;
	}
	public void setAmountApplied(double amountApplied) {
		this.amountApplied = amountApplied;
	}
	
	
	
}
