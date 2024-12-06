package com.navin.billings.resource;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import com.navin.billings.model.Bill;
import com.navin.billings.model.BillPayment;
import com.navin.billings.model.Payment;
import com.navin.billings.repository.BillRepository;
import com.navin.billings.repository.PaymentRepository;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("payments")
public class PaymentResource {
	
	private PaymentRepository paymentRepository = new PaymentRepository();
	private PaymentValidator paymentValidator = new PaymentValidator();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllPayments(){
		
		try {
			List<Payment> paymentList = paymentRepository.getAllPayments();
		
			if(paymentList == null || paymentList.isEmpty()) {
				return Response.status(Response.Status.NOT_FOUND).entity("No Payments found").build();
			}else {
				return Response.ok(paymentList).build();
			}
		
		}catch(SQLException sqlException) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error fetching payments").build();
		}
		
	}
	
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response getPayment(@PathParam("id") int id){
		
		try {
			
			Payment payment = paymentRepository.getPayment(id);
			
			if(payment != null) {
				return Response.ok(payment).build();
			}else {
				return Response.status(Response.Status.NOT_FOUND).entity("Payment Not Found").build();
			}
		
		}catch(SQLException sqlException) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error fetching payment").build();
		}
		
		
	}
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createPayment(Payment payment) {
		
		if(!paymentValidator.validatePayment(payment)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Payment Data").build();
		}
		
		try {
			
			//check the bills of vendor
			for(BillPayment billPayment : payment.getBillPayments()) {
				
				Bill bill = new BillRepository().getBill(billPayment.getBill().getBillId());
				if(bill.getVendor().getVendorId() != payment.getVendor().getVendorId()) {
					return Response.status(Response.Status.BAD_REQUEST).entity("Include only the bills of vendor").build();
				}
			}
			
			Payment recordedPayment = paymentRepository.recordPayment(payment);		
			return Response.ok(recordedPayment.getPaymentId()).build();
			
		}catch(SQLIntegrityConstraintViolationException sqlInegerityException) {
			
			if(sqlInegerityException.getMessage().contains("fk_billpayment_bill")) {
				return Response.status(Response.Status.CONFLICT).entity("Bill not matching").build();
			}
			if(sqlInegerityException.getMessage().contains("fk_payment_vendor")) {
				return Response.status(Response.Status.CONFLICT).entity("Vendor not matching").build();
			}
			return Response.status(Response.Status.CONFLICT).build();
			
		}catch(SQLException sqlException) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error recording payment").build();
		}
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response updatePayment(@PathParam("id") int id ,Payment updatedPayment) {
		
		if(!paymentValidator.validatePayment(updatedPayment)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Payment Data").build();
		}
		try {
			//check the bills of vendor
			for(BillPayment billPayment : updatedPayment.getBillPayments()) {
				
				Bill bill = new BillRepository().getBill(billPayment.getBill().getBillId());
				if(bill.getVendor().getVendorId() != updatedPayment.getVendor().getVendorId()) {
					return Response.status(Response.Status.BAD_REQUEST).entity("Include only the bills of vendor").build();
				}
			}
			//get the existing payment
			Payment existingPayment = paymentRepository.getPayment(id);
			
			if(existingPayment == null) {
				
				return Response.status(Response.Status.NOT_FOUND).entity("Payment Not Found").build();
			
			}else {
				
				//check the vendor of the existing bill and the updated bill
				if(paymentValidator.validateVendor(existingPayment.getVendor(), updatedPayment.getVendor())) {
					
					updatedPayment.setPaymentId(id);
					Payment payment = paymentRepository.updateRecordedPayment(existingPayment, updatedPayment);
					return Response.ok(payment.getPaymentId()).build();
					
				}else {
					return Response.status(Response.Status.BAD_REQUEST).entity("Vendor cannot be updated").build();
				}
			}
			
		}catch(SQLIntegrityConstraintViolationException sqlInegerityException) {
			if(sqlInegerityException.getMessage().contains("fk_billpayment_bill")) {
				return Response.status(Response.Status.CONFLICT).entity("Bill not matching").build();
			}
			if(sqlInegerityException.getMessage().contains("fk_payment_vendor")) {
				return Response.status(Response.Status.CONFLICT).entity("Vendor not matching").build();
			}
			return Response.status(Response.Status.CONFLICT).build();
		}
		catch(SQLException sqlException) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error updating payment").build();
		}
	}
	
	@DELETE
	@Path("{id}")
	public Response deletePayment(@PathParam("id") int id) {
		try {
			
			Payment payment = paymentRepository.getPayment(id);
			boolean deleted = paymentRepository.deletePayment(payment);
			
			if(deleted) {
				
				return Response.ok().entity("Deleted Successfully").build();
			}else {
				
				return Response.status(Response.Status.NOT_FOUND).entity("Payment not found").build();
			}
			
		}catch(SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error deleting payment").build();
		}
	}
	
	

}
