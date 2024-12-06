package com.navin.billings.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Payment {
	
	private int paymentId;
	private String paymentMode;
	private double paidAmount;
	@JsonFormat(pattern = "dd-MM-yyyy")
	private LocalDate paidDate;
	private Vendor vendor;
	@JsonProperty("payments")
	private List<BillPayment> billPayments;
	
	public Payment() {
		
	}
	
	
	
	
	public Payment(int paymentId, String paymentMode, double paidAmount, LocalDate paidDate, Vendor vendor,
			List<BillPayment> billPayments) {
		super();
		this.paymentId = paymentId;
		this.paymentMode = paymentMode;
		this.paidAmount = paidAmount;
		this.paidDate = paidDate;
		this.vendor = vendor;
		this.billPayments = billPayments;
	}




	public int getPaymentId() {
		return paymentId;
	}
	public void setPaymentId(int paymentId) {
		this.paymentId = paymentId;
	}
	public String getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	public double getPaidAmount() {
		return paidAmount;
	}
	public void setPaidAmount(double paidAmount) {
		this.paidAmount = paidAmount;
	}
	public LocalDate getPaidDate() {
		return paidDate;
	}
	public void setPaidDate(LocalDate paidDate) {
		this.paidDate = paidDate;
	}
	public Vendor getVendor() {
		return vendor;
	}
	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}
	public List<BillPayment> getBillPayments() {
		return billPayments;
	}
	public void setBillPayments(List<BillPayment> billPayments) {
		this.billPayments = billPayments;
	}

	
	
	
}
