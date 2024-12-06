package com.navin.billings.resource;

import java.util.List;

import com.navin.billings.model.Bill;
import com.navin.billings.model.BillItem;
import com.navin.billings.model.Product;
import com.navin.billings.model.Vendor;

public class BillValidator {
	
	public boolean validateBill(Bill bill) {
		
		return bill.getBillDate() != null  && isValidBillNumber(bill.getBillNumber()) &&  
			   isValidVendor(bill.getVendor()) && isValidBillItems(bill.getBillItems());
	}
	
	private boolean isValidBillNumber(String billNumber) {
		return billNumber != null && !billNumber.isBlank();
	}
	
	
	private boolean isValidVendor(Vendor vendor) {
		return vendor != null && vendor.getVendorId() > 0;
	}
	
	
	private boolean isValidBillItems(List<BillItem> billItems) {
		if(billItems == null || billItems.isEmpty()) {
			return false;
		}
		for(BillItem billItem : billItems) {
			if(!isValidBillItem(billItem)) {
				return false;
			}
		}	
		return true;
	}
	
	
	private boolean isValidBillItem(BillItem billItem) {
		return billItem != null && billItem.getQuantity() > 0 && isValidProduct(billItem.getProduct());
	}
	
	private boolean isValidProduct(Product product) {
		return product != null && product.getProductName() != null && !product.getProductName().isBlank() && product.getUnitPrice() > 0;
	}
	
	
	
}
