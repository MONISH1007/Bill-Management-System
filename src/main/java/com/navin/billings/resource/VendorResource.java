package com.navin.billings.resource;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import com.navin.billings.model.Vendor;
import com.navin.billings.repository.VendorRepository;

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

@Path("vendors")
public class VendorResource {
	
	VendorRepository vendorRepository = new VendorRepository();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVendors(){
		
		try {
			
			List<Vendor> vendors = vendorRepository.getAllVendors();
			
			if(!vendors.isEmpty()) {
				return Response.ok(vendors).build();
			}else {
				return Response.status(Response.Status.NOT_FOUND).entity("Vendors not found").build();
			}
			
		}catch(SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while retrieving vendors").build();
		}
		
		
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response getVendor(@PathParam("id")int id) {
		
		try {

			Vendor vendor = vendorRepository.getVendor(id);

			if (vendor != null) {
				return Response.ok(vendor).build();
			} else {
				return Response.status(Response.Status.NOT_FOUND).entity("Vendor not found").build();
			}

		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Internal Error while retrieving vendor").build();
		}
	}
	
	
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createVendor(Vendor vendor) {

		if (vendor == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Vendor data is required").build();
		}
		if (vendor.getVendorName() == null || vendor.getVendorName().isBlank()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Vendor name is required").build();
		}

		try {
			Vendor createdVendor = vendorRepository.createVendor(vendor);
			return Response.ok(createdVendor.getVendorId()).build();

		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while creating vendor")
					.build();
		}
	}
	
	
	
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response updateVendor(@PathParam("id") int id, Vendor updatedVendor) {

		if (updatedVendor == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Vendor data is required").build();
		}
		if (updatedVendor.getVendorName() == null || updatedVendor.getVendorName().isBlank()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Vendor name is required").build();
		}

		try {

			Vendor existingVendor = vendorRepository.getVendor(id);

			if (existingVendor == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Vendor not found").build();
			}

			updatedVendor.setVendorId(id);
			Vendor vendor = vendorRepository.updateVendor(updatedVendor);
			return Response.ok(vendor.getVendorId()).build();

		} catch (SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while updating vendor")
					.build();
		}

	}
	
	
	
	
	@DELETE
	@Path("{id}")
	public Response deleteVendor(@PathParam("id") int id) {
		
		try {
		
			boolean deleted = vendorRepository.deleteVendor(id);
			
			if(deleted) {
				return Response.ok().entity("Successfully deleted vendor").build();
			}else {
				return Response.status(Response.Status.NOT_FOUND).entity("Vendor not found").build();
			}
		
		}catch(SQLIntegrityConstraintViolationException sqlIntegrityConstraintViolationException) {
			
			return Response.status(Response.Status.CONFLICT).entity("Delete all the bills associated with the vendorId : "+id+" before deleting the vendor").build();
		}
		catch(SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while deleting vendor").build();
		}
	}
	
}
