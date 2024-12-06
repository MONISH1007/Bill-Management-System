package com.navin.billings.model;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;

public class Bill {

	private int billId;
	private String billNumber;
	private double billAmount;
	private double balanceAmount;
	private double amountPaid;
	@JsonFormat(pattern = "dd-MM-yyyy")
	private LocalDate billDate;
	@JsonFormat(pattern = "dd-MM-yyyy")
	private LocalDate dueDate;
	private String description;
	private List<BillItem> billItems;
	private Vendor vendor;

	public Bill() {
		
	}
	
	

	public Bill(int billId, String billNumber, Vendor vendor, double billAmount, double balanceAmount,
			double amountPaid, LocalDate billDate, LocalDate dueDate, String description,
			List<BillItem> billItems) {
		super();
		this.billId = billId;
		this.billNumber = billNumber;
		this.vendor = vendor;
		this.billAmount = billAmount;
		this.balanceAmount = balanceAmount;
		this.amountPaid = amountPaid;
		this.billDate = billDate;
		this.dueDate = dueDate;
		this.description = description;
		this.billItems = billItems;
	}

	public String getBillNumber() {
		return billNumber;
	}
	public void setBillNumber(String billNumber) {
		this.billNumber = billNumber;
	}
	public int getBillId() {
		return billId;
	}
	public void setBillId(int billId) {
		this.billId = billId;
	}
	public Vendor getVendor() {
		return vendor;
	}
	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}
	public double getBillAmount() {
		return billAmount;
	}
	public void setBillAmount(double billAmount) {
		this.billAmount = billAmount;
	}
	public LocalDate getBillDate(){
		return billDate;
	}
	public void setBillDate(LocalDate billDate) {
		this.billDate = billDate;
	}
	public LocalDate getDueDate() {
		return dueDate;
	}
	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<BillItem> getBillItems() {
		return billItems;
	}
	public void setBillItems(List<BillItem> billItems) {
		this.billItems = billItems;
	}
	public double getBalanceAmount() {
		return balanceAmount;
	}

	public void setBalanceAmount(double balanceAmount) {
		this.balanceAmount = balanceAmount;
	}

	public double getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(double amountPaid) {
		this.amountPaid = amountPaid;
	}
	
//	@JsonProperty("vendorId")
//	public int getVendorId() {
//		return vendor.getVendorId();
//	}
//	@JsonProperty("vendorName")
//	public String getVendorName() {
//		return vendor.getVendorName();
//	}
//	@JsonProperty("vendorContactNumber") 
//	public String getContactNumber() {
//		return vendor.getContactNumber();
//	}
//	@JsonProperty("vendorGstNumber")
//	public String getGstNumber() {
//		return vendor.getGstNumber();
//	}


	public double calculateBillAmount(List<BillItem> billItemList) {
		
		billItemList = billItemList.stream().peek(billItem -> billItem.setItemPrice(billItem.getProduct().getUnitPrice() * billItem.getQuantity())).collect(Collectors.toList());
		return billItemList.stream().mapToDouble(billItem -> billItem.getItemPrice()).sum();
	}
	
}
