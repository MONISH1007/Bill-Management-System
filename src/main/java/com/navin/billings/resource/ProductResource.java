package com.navin.billings.resource;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

import com.navin.billings.model.Product;
import com.navin.billings.repository.ProductRepository;

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

@Path("products")
public class ProductResource {
	
	ProductRepository productRepository = new ProductRepository();
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProducts(){
		
		try {
			
			List<Product> products =  productRepository.getProducts();
			if(products.isEmpty()) {
				return Response.status(Response.Status.NOT_FOUND).entity("No Products found").build();
			}else {
				return Response.ok(products).build();
			}
			
		}catch(SQLException e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while retrieving the products").build();
		}
		
		
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response getProduct(@PathParam("id") int id) {
		
		try {
		
			Product product =  productRepository.getProduct(id);
			
			if(product != null) {
				return Response.ok(product).build();
			}else {
				return Response.status(Response.Status.NOT_FOUND).entity("Product not found").build();
			}
			
		}catch(SQLException sqlException) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while retrieving the product").build();
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createProduct(Product product) {
		
		if(product == null) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Product data is required").build();
		}
		
		if(product.getProductName() == null || product.getProductName().isBlank()) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Product name is required").build();
		}
		if(product.getUnitPrice() <= 0) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Invalid unit price").build();
		}
			
		try {
			Product createdProduct = productRepository.createProduct(product);
			return Response.ok(createdProduct.getProductId()).build();
			
		}catch(SQLException e) {
			
			if(e.getMessage().contains("Duplicate entry")) {
				return Response.status(Response.Status.CONFLICT).entity("Product already exist").build();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while creating product").build();
		}
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response updateProduct(@PathParam("id")int id, Product updatedProduct) {
		
		try {
		
			if(updatedProduct == null) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Product data is required").build();
			}
			if(updatedProduct.getProductName() == null || updatedProduct.getProductName().isBlank()) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Product name is Required").build();
			}
			if(updatedProduct.getUnitPrice() <= 0) {
				return Response.status(Response.Status.BAD_REQUEST).entity("Product price is required").build();
			}
			
			Product existingProduct = productRepository.getProduct(id);
			if(existingProduct == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Product not found").build();
			}
			
			updatedProduct.setProductId(id);
			Product product = productRepository.updateProduct(updatedProduct);
			return Response.ok(product.getProductId()).build();
			
		}catch(SQLException sqlException) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while updating product").build();
		}
		
	}
	
	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response deleteProduct(@PathParam("id")int id) {
		
		try {
			
			boolean deleted = productRepository.deleteProduct(id);
			if(deleted) {
				return Response.ok().entity("Deleted Successfully").build();
			}else {
				return Response.status(Response.Status.NOT_FOUND).entity("Product not found").build();
			}
		
		}catch(SQLIntegrityConstraintViolationException sqlIntegrityConstraintViolationException) {
			
			return Response.status(Response.Status.CONFLICT).entity("Delete all the bills associated with the productId : "+id+" before deleting the product").build();
		}
		catch (SQLException sqlException) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal Error while deleting product").build();
		}
		
		
	}
}
