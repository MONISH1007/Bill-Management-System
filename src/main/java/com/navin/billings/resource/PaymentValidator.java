package com.navin.billings.resource;

import java.util.List;

import com.navin.billings.model.BillPayment;
import com.navin.billings.model.Payment;
import com.navin.billings.model.Vendor;

public class PaymentValidator {
	
	public boolean validatePayment(Payment payment) {
		
		return payment != null && payment.getPaymentMode() != null  && validateBillPayment(payment.getBillPayments()) 
				&& validatePaymentAmount(payment) && validateVendorDetails(payment.getVendor());
	}
	
	public boolean validateVendor(Vendor existingVendor, Vendor updatedVendor) {
		return existingVendor.getVendorId() == updatedVendor.getVendorId();
	}

	
	private boolean validatePaymentAmount(Payment payment) {
		double paidAmount = payment.getPaidAmount();
		
		if(paidAmount > 0) {
			double amountAppliedForBills = payment.getBillPayments().stream().mapToDouble(billPayment -> billPayment.getAmountApplied()).sum();
			
			if(amountAppliedForBills == paidAmount) {
				return true;
			}
		}
		return false;
	}
	
	
	private boolean validateVendorDetails(Vendor vendor) {
		return vendor != null && vendor.getVendorId() > 0;		
	}
	
	private boolean validateBillPayment(List<BillPayment> billPaymentList) {
		if(billPaymentList == null || billPaymentList.isEmpty()) {
			return false;
		}
		
		for(BillPayment billPayment : billPaymentList) {
			
			if(!(billPayment.getBill() != null) || !(billPayment.getBill().getBillId() > 0) || !(billPayment.getAmountApplied() >= 0)) {
				return false;
			}
		}
		return true;
		
	}
	
}
