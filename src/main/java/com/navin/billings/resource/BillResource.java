package com.navin.billings.resource;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import com.navin.billings.model.Bill;
import com.navin.billings.repository.BillRepository;

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

@Path("bills")
public class BillResource {

	private BillRepository billRepository = new BillRepository();
	private BillValidator billValidator = new BillValidator();

	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getBills() {
		
		try {
			List<Bill> bills =  billRepository.getBills();
			
			if(!bills.isEmpty()) {
				
				return Response.ok(bills).build();
				
			}else {
				
				return Response.status(Response.Status.NOT_FOUND).entity("No bills found").build();
			}
			
		}catch(SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while Fetching Bills").build();
		}

		
	}
	
	
	

	@GET
	@Path("{id}")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getBill(@PathParam("id") int billId) {

		try {
			
			Bill bill = billRepository.getBill(billId);
			
			if(bill != null) {
				
				return Response.ok(bill).build();
			}else {
				
				return Response.status(Response.Status.NOT_FOUND).entity("Bill not found").build();
			}
			
			
			
		}catch(SQLException e) {
			
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal error while retrieving Bill").build();
		}
	}

	
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createBill(Bill bill) {
		
		//validate bill
		if (!billValidator.validateBill(bill)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid bill data").build();
		}

		// calculate Bill Amount
		 double billAmount = bill.calculateBillAmount(bill.getBillItems());
		 bill.setBillAmount(billAmount);
		 bill.setBalanceAmount(billAmount);
		

		try {

			// creating bill
			Bill createdBill = billRepository.createBill(bill);
			return Response.status(Response.Status.CREATED).entity(createdBill.getBillId()).build();

		} catch (SQLIntegrityConstraintViolationException sqlIntegrityConstraintViolationException) {

			String message = sqlIntegrityConstraintViolationException.getMessage();

			// Bill contains product that is not present in product database
			if (message.contains("fk_product")) {
				return Response.status(Response.Status.CONFLICT)
						.entity("Create the products associated with the bill before creating the bill").build();
			}

			// Bill contains vendor that is not present in vendor database
			if (message.contains("fk_vendor")) {
				return Response.status(Response.Status.CONFLICT)
						.entity("Create the vendor associated with the bill before creating the bill").build();
			}

			return Response.status(Response.Status.CONFLICT).build();
			
		} catch (SQLException sqlException) {

			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error").build();
		}

	}
	
	
	

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response updateBill(@PathParam("id") int id, Bill updatedBill) {
		
		//validate bill
		if (!billValidator.validateBill(updatedBill)) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid bill data").build();
		}

		
		try {
			
			//retrieve the existing bill
			Bill existingBill = billRepository.getBill(id);		
			if(existingBill == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Bill not found").build();
			}	
			
			// calculate Bill Amount
			double billAmount =  updatedBill.calculateBillAmount(updatedBill.getBillItems());
			double paidAmount = existingBill.getAmountPaid();
			double balanceAmount = billAmount - paidAmount;
			
			updatedBill.setBillAmount(billAmount);
			updatedBill.setAmountPaid(paidAmount);
			updatedBill.setBalanceAmount(balanceAmount);
			
			updatedBill.setBillId(id);
			
			//update bill
			Bill bill = billRepository.updateBill(existingBill, updatedBill);
			return Response.ok(bill.getBillId()).build();	
			
		}catch(SQLIntegrityConstraintViolationException e) {
			
			String message = e.getMessage();	
			if(message.contains("fk_product")) {
				return Response.status(Response.Status.CONFLICT).
						entity("Create the products associated with the bill before creating the bill").build();
			}
			if(message.contains("fk_vendor")) {
				return Response.status(Response.Status.CONFLICT).
						entity("Create the vendor associated with the bill before creating the bill").build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while updating").build();
		
		}catch(SQLException e) {
			
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while updating bill").build();
		}
		
	}
	
	

	@DELETE
	@Path("{id}")
	public Response deleteBill(@PathParam("id") int id) {
		
		try {
			Bill bill = billRepository.getBill(id);
			boolean deleted = billRepository.deleteBill(bill);
			
			if(deleted) {
				
				return Response.ok().entity("Deleted Successfully").build();
			}else {
				
				return Response.status(Response.Status.NOT_FOUND).entity("Bill not found").build();
			}
			
		}catch(SQLIntegrityConstraintViolationException sqlIntegrityConstraintViolationException) {
			
			if(sqlIntegrityConstraintViolationException.getMessage().contains("")) {
				return Response.status(Response.Status.CONFLICT).entity("Cannot delete bill associated with payment").build();
			}
			return Response.status(Response.Status.CONFLICT).build();
			
		}catch(SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error deleting bill").build();
		}
	}

}
