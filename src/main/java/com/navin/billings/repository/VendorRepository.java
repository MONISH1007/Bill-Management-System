package com.navin.billings.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.navin.billings.model.Vendor;

public class VendorRepository {
	
	private static final String url = "jdbc:mysql://localhost:3306/billings";;
	private static final String username = "root";
	private static final String password ="root1234";
	
	
	
	public VendorRepository(){
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//Get all vendors from db
	public List<Vendor> getAllVendors() throws SQLException{
		
		String query = "select vendorId, vendorName, contactNumber, paymentAddress, gstNumber, totalPayable  from vendor";
		
		List<Vendor> vendorList = new ArrayList<Vendor>();
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query)){
			
			while(resultSet.next()) {
				
				Vendor vendor = new Vendor();
				vendor.setVendorId(resultSet.getInt("vendorId"));
				vendor.setVendorName(resultSet.getString("vendorName"));
				vendor.setContactNumber(resultSet.getString("contactNumber"));
				vendor.setPaymentAddress(resultSet.getString("paymentAddress"));
				vendor.setGstNumber(resultSet.getString("gstNumber"));
				vendor.setTotalPayable(resultSet.getDouble("totalPayable"));
				
				vendorList.add(vendor);
			}
			
		}catch(SQLException e) {
			e.printStackTrace();
			throw e;
		}
		
		
		return vendorList;
	}
	
	
	
	
	
	//Get a particular vendor from db
	public Vendor getVendor(int vendorId) throws SQLException{
		
		String query = "select vendorId, vendorName, contactNumber, paymentAddress, gstNumber, totalPayable from vendor where vendorId = "+vendorId;
		
		Vendor vendor = null;
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query)){
			
			
			if(resultSet.next()) {
				
				vendor = new Vendor();
				
				vendor.setVendorId(resultSet.getInt("vendorId"));
				vendor.setVendorName(resultSet.getString("vendorName"));
				vendor.setContactNumber(resultSet.getString("contactNumber"));
				vendor.setPaymentAddress(resultSet.getString("paymentAddress"));
				vendor.setGstNumber(resultSet.getString("gstNumber"));
				vendor.setTotalPayable(resultSet.getDouble("totalPayable"));
			}
			
			
		}catch(SQLException e) {
			e.printStackTrace();
			throw e;
			
		}
		
		return vendor;
		
	}
	
	
	
	//create a new vendor in db
	public Vendor createVendor(Vendor vendor) throws SQLException{
		
		String query = "insert into vendor(vendorName, contactNumber, paymentAddress, gstNumber) values(?,?,?,?)";
		
		ResultSet generatedKey = null;
		
		Vendor createdVendor = null;
		
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){
			
			preparedStatement.setString(1, vendor.getVendorName());
			preparedStatement.setString(2, vendor.getContactNumber());
			preparedStatement.setString(3, vendor.getPaymentAddress());
			preparedStatement.setString(4, vendor.getGstNumber());
			preparedStatement.executeUpdate();
			
			
			createdVendor = vendor;
			
			//Retrieving auto-generated vendorId
			generatedKey = preparedStatement.getGeneratedKeys();
			
			if(generatedKey.next()) {
				createdVendor.setVendorId(generatedKey.getInt(1));
			}
			
			return createdVendor;
			
		}catch(SQLException e) {
			
			e.printStackTrace();
			throw e;
			
		}finally {
			try {
				if(generatedKey != null) {
					generatedKey.close();
				}
			}catch(SQLException e) {
				e.printStackTrace();
				throw e;
			}
		}
		
	}
	
	
	
	
	//updating a vendor in db
	public Vendor updateVendor(Vendor vendor) throws SQLException{
		
		String query = "update vendor set vendorName = ? ,contactNumber = ? ,paymentAddress = ? ,gstNumber = ? where vendorId=?";
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			PreparedStatement preparedStatement = connection.prepareStatement(query)){
			
				preparedStatement.setString(1, vendor.getVendorName());
				preparedStatement.setString(2, vendor.getContactNumber());
				preparedStatement.setString(3, vendor.getPaymentAddress());
				preparedStatement.setString(4, vendor.getGstNumber());
				preparedStatement.setInt(5, vendor.getVendorId());
				int rows = preparedStatement.executeUpdate();
				
				if(rows > 0) {
					return vendor;
				}else {
					return null;
				}
				
								
			}catch(SQLException e) {
				e.printStackTrace();
				throw e;
			}
	}
	
	
	
	//delete vendor
	public boolean deleteVendor(int vendorId) throws SQLIntegrityConstraintViolationException, SQLException{
		
		String query = "delete from vendor where vendorId = ?";
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			PreparedStatement preparedStatement = connection.prepareStatement(query)){
			
			preparedStatement.setInt(1, vendorId);
			
			int rows = preparedStatement.executeUpdate();
			return rows == 1;
			
			
		}catch(SQLIntegrityConstraintViolationException sqlIntegrityConstraintViolationException) {
			sqlIntegrityConstraintViolationException.printStackTrace();
			throw sqlIntegrityConstraintViolationException;
			
			
		}catch(SQLException sqlException) {
			sqlException.printStackTrace();
			throw sqlException;
		}
		
	}
	
	
	
	
}
