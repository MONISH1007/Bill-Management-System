package com.navin.billings.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.navin.billings.model.Product;

public class ProductRepository {
	
	private static final String url = "jdbc:mysql://localhost:3306/billings";;
	private static final String username = "root";
	private static final String password ="root1234";
	
	
	public ProductRepository(){
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	//retrieving all products
	public List<Product> getProducts() throws SQLException{
		
		String query = "select * from product";
		
		List<Product> productList = new ArrayList<Product>();
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query)){
			
			while(resultSet.next()) {
				
				Product product = new Product();
				product.setProductId(resultSet.getInt("productId"));
				product.setProductName(resultSet.getString("productName"));
				product.setUnitPrice(resultSet.getDouble("unitPrice"));
				product.setProductDescription(resultSet.getString("productDescription"));
				
				
				productList.add(product);
			}
			
		}catch(SQLException e) {
			e.printStackTrace();
			throw e;
		}
		
		
		return productList;
	}
	
	
	
	//retrieving a product
	public Product getProduct(int productId) throws SQLException{
		
		String query = "select * from product where productId = "+productId;
		
		Product product = null;
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query)){
			
			if(resultSet.next()) {
				
				product = new Product();
				
				product.setProductId(resultSet.getInt("productId"));
				product.setProductName(resultSet.getString("productName"));
				product.setUnitPrice(resultSet.getDouble("unitPrice"));
				product.setProductDescription(resultSet.getString("productDescription"));
				
			}
				
			
		}catch(SQLException e) {
			
			e.printStackTrace();
			throw e;
		}
		
		
		return product;
	}
	
	
	
	
	//creating a product
	public Product createProduct(Product product) throws SQLException{
		
		String query = "insert into product(productName, productDescription, unitPrice) values(?,?,?)";
		
		ResultSet generatedKey = null;
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)){
			
			preparedStatement.setString(1, product.getProductName());
			preparedStatement.setString(2, product.getProductDescription());
			preparedStatement.setDouble(3, product.getUnitPrice());
			preparedStatement.executeUpdate();
			
			generatedKey = preparedStatement.getGeneratedKeys();
			
			if(generatedKey.next()) {
				product.setProductId(generatedKey.getInt(1));
			}
			
			return product;
			
		}catch(SQLException e) {
			
			e.printStackTrace();
			throw e;
		
		}finally {
			if(generatedKey != null) {
				generatedKey.close();
			}
		}
	}
	
	
	
	
	//updating the product
	public Product updateProduct(Product product) throws SQLException {
		
		String query = "update product set productName = ?, productDescription = ? , unitPrice = ? where productId = ?";
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			PreparedStatement preparedStatement = connection.prepareStatement(query)){
			
				
			preparedStatement.setString(1, product.getProductName());
			preparedStatement.setString(2, product.getProductDescription());
			preparedStatement.setDouble(3, product.getUnitPrice());
			preparedStatement.setInt(4, product.getProductId());
			
			int rows = preparedStatement.executeUpdate();
			
			if(rows > 0) {
				return product;
			}else {
				return null;
			}
			
				
				
		}catch(SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	
	
	
	//deleting a product
	public boolean deleteProduct(int productId) throws SQLException{
		
		String query = "delete from product where productId = ?";
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			PreparedStatement preparedStatement = connection.prepareStatement(query)){
			
			preparedStatement.setInt(1, productId);
			int rows = preparedStatement.executeUpdate();
			
			return rows > 0;
			
		}catch(SQLException e) {
			e.printStackTrace();
			throw e;
		}
		
		
	}
	
	
	
}
