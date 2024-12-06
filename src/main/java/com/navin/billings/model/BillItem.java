package com.navin.billings.model;

public class BillItem {
	
	private int billItemId;
	private Product product;
	private int quantity;
	private double itemPrice;
	
	
	public Product getProduct() {
		return product;
	}
	public void setProduct(Product product) {
		this.product = product;
	}
	public int getBillItemId() {
		return billItemId;
	}
	public void setBillItemId(int billItemId) {
		this.billItemId = billItemId;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public double getItemPrice() {
		return itemPrice;
	}
	public void setItemPrice(double itemPrice) {
		this.itemPrice = itemPrice;
	}
	
}
