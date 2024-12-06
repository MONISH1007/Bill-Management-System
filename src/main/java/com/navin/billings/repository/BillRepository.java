package com.navin.billings.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.navin.billings.model.Bill;
import com.navin.billings.model.BillItem;
import com.navin.billings.model.Product;
import com.navin.billings.model.Vendor;

public class BillRepository {

	private static final String url = "jdbc:mysql://localhost:3306/billings";;
	private static final String username = "root";
	private static final String password = "root1234";

	public BillRepository() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	

	//Retrieving all bills from DB
	public List<Bill> getBills() throws SQLException{

		String query = "select bill.billId, bill.billNumber, bill.billDate, bill.billAmount, bill.dueDate, bill.description, bill.balanceAmount, "
				+ "vendor.vendorId, vendor.vendorName, vendor.contactNumber, vendor.gstNumber, "
				+ "product.productId, product.productName, product.unitPrice, "
				+ "billItem.billItemId, billItem.quantity, billItem.itemPrice from bill "
				+ "inner join vendor on bill.vendorId = vendor.vendorId "
				+ "inner join billItem on bill.billId = billItem.billId "
				+ "inner join product on billItem.productId = product.productId";

		List<Bill> billList = new ArrayList<Bill>();

		try (Connection connection = DriverManager.getConnection(url, username, password);
				Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				ResultSet resultSet = statement.executeQuery(query)) {
			
			while (resultSet.next()) {
				
				Bill bill = new Bill();
				List<BillItem> billItemList = new ArrayList<BillItem>();
				
				int billId = resultSet.getInt("billId");
				
				bill.setBillId(billId);
				bill.setBillNumber(resultSet.getString("billNumber"));
				bill.setBillDate(resultSet.getDate("billDate").toLocalDate());
				bill.setBillAmount(resultSet.getDouble("billAmount"));
				bill.setBalanceAmount(resultSet.getDouble("balanceAmount"));
				bill.setAmountPaid(bill.getBillAmount() - bill.getBalanceAmount());
				bill.setDueDate(resultSet.getDate("dueDate").toLocalDate());
				bill.setDescription(resultSet.getString("description"));

				Vendor vendor = new Vendor();
				vendor.setVendorId(resultSet.getInt("vendorId"));
				vendor.setVendorName(resultSet.getString("vendorName"));
				vendor.setContactNumber(resultSet.getString("contactNumber"));
				vendor.setGstNumber(resultSet.getString("gstNumber"));
				bill.setVendor(vendor);

				
				while(resultSet.getInt("billId") == billId) {
					
					BillItem billItem = new BillItem();
					Product product = new Product();

					product.setProductId(resultSet.getInt("productId"));
					product.setProductName(resultSet.getString("productName"));
					product.setUnitPrice(resultSet.getDouble("unitPrice"));

					billItem.setProduct(product);
					billItem.setBillItemId(resultSet.getInt("billItemId"));
					billItem.setQuantity(resultSet.getInt("quantity"));
					billItem.setItemPrice(resultSet.getDouble("itemPrice"));

					billItemList.add(billItem);
					
					if(!resultSet.next()) {
						
						break;
					}

				}		
				
				resultSet.previous();
				
				bill.setBillItems(billItemList);
				billList.add(bill);
				
			}
			
			return billList;

		} catch (SQLException e) {
			
			e.printStackTrace();
			throw e;
		}

	}
	
	
	
	

	// Retrieving a bill from db
	public Bill getBill(int billId) throws SQLException{

		// inner join
		String billQuery = "select bill.billId, bill.billNumber, bill.billDate, bill.billAmount, bill.dueDate, bill.description, bill.balanceAmount, "
				+ "vendor.vendorId, vendor.vendorName, vendor.contactNumber, vendor.gstNumber, vendor.paymentAddress, "
				+ "product.productId, product.productName, product.unitPrice, "
				+ "billItem.billItemId, billItem.quantity, billItem.itemPrice from bill "
				+ "inner join vendor on bill.vendorId = vendor.vendorId "
				+ "inner join billItem on bill.billId = billItem.billId "
				+ "inner join product on billItem.productId = product.productId where bill.billId = "+billId;
		
		Bill bill = null;
		

		try (Connection connection = DriverManager.getConnection(url, username, password);
			 Statement statement = connection.createStatement();
			 ResultSet resultSet = statement.executeQuery(billQuery)) {

			if (resultSet.next()) {
				
				bill = new Bill();
				
				List<BillItem> billItemList = new ArrayList<BillItem>();
				
				bill.setBillId(billId);
				bill.setBillNumber(resultSet.getString("billNumber"));
				bill.setBillDate(resultSet.getDate("billDate").toLocalDate());
				bill.setBillAmount(resultSet.getDouble("billAmount"));
				bill.setBalanceAmount(resultSet.getDouble("balanceAmount"));
				bill.setAmountPaid(bill.getBillAmount() - bill.getBalanceAmount());
				bill.setDueDate(resultSet.getDate("dueDate").toLocalDate());
				bill.setDescription(resultSet.getString("description"));


				Vendor vendor = new Vendor();
				vendor.setVendorId(resultSet.getInt("vendorId"));
				vendor.setVendorName(resultSet.getString("vendorName"));
				vendor.setContactNumber(resultSet.getString("contactNumber"));
				vendor.setGstNumber(resultSet.getString("gstNumber"));
				vendor.setPaymentAddress(resultSet.getString("paymentAddress"));
				bill.setVendor(vendor);

				
				do {
					BillItem billItem = new BillItem();
					Product product = new Product();

					product.setProductId(resultSet.getInt("productId"));
					product.setProductName(resultSet.getString("productName"));
					product.setUnitPrice(resultSet.getDouble("unitPrice"));
					

					billItem.setProduct(product);
					billItem.setBillItemId(resultSet.getInt("billItemId"));
					billItem.setQuantity(resultSet.getInt("quantity"));
					billItem.setItemPrice(resultSet.getDouble("itemPrice"));

					billItemList.add(billItem);

				} while (resultSet.next());

				bill.setBillItems(billItemList);

			}
			
			return bill;

		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}


	}
	
	
	
	

	// Creating bill in db
	public Bill createBill(Bill bill) throws SQLException{

		String createBillQuery = "insert into bill(billNumber, vendorId, billAmount, billDate, dueDate, description, balanceAmount) values(?,?,?,?,?,?,?)";
		String createBillItemQuery = "insert into billItem(billId,productId,quantity,itemPrice) values(?,?,?,?)";
		String updateTotalPayabaleQuery = "update vendor set totalPayable = COALESCE(totalPayable, 0) + ? where vendorId = ?";
	
		Connection connection = null;
		PreparedStatement billStatement = null;
		PreparedStatement billItemStatement = null;
		PreparedStatement updateTotalPayableStatement = null;
		ResultSet generatedBillId = null;
		ResultSet generatedBillItemId = null;

		try{

			connection = DriverManager.getConnection(url, username, password);
			billStatement = connection.prepareStatement(createBillQuery,Statement.RETURN_GENERATED_KEYS);
			billItemStatement = connection.prepareStatement(createBillItemQuery,Statement.RETURN_GENERATED_KEYS);
			updateTotalPayableStatement = connection.prepareStatement(updateTotalPayabaleQuery);

			connection.setAutoCommit(false);

			// Insert bill
			billStatement.setString(1, bill.getBillNumber());
			billStatement.setInt(2, bill.getVendor().getVendorId());
			billStatement.setDouble(3, bill.getBillAmount());
			billStatement.setDate(4, Date.valueOf(bill.getBillDate()));
			billStatement.setDate(5, Date.valueOf(bill.getDueDate()));
			billStatement.setString(6, bill.getDescription());
			billStatement.setDouble(7, bill.getBalanceAmount());
			billStatement.executeUpdate();
			
			//Update totalPayable
			updateTotalPayableStatement.setDouble(1, bill.getBillAmount());
			updateTotalPayableStatement.setInt(2, bill.getVendor().getVendorId());
			int rowsUpdated = updateTotalPayableStatement.executeUpdate();
			
			if(rowsUpdated != 1) {
				throw new SQLException(); 
			}

			// Retrieve auto-generated billId
			generatedBillId = billStatement.getGeneratedKeys();
			if (generatedBillId.next()) {
				bill.setBillId(generatedBillId.getInt(1));
			}

			// Insert BillItem
			for (BillItem billItem : bill.getBillItems()) {
				
				billItemStatement.setInt(1, bill.getBillId());
				billItemStatement.setInt(2, billItem.getProduct().getProductId());
				billItemStatement.setInt(3, billItem.getQuantity());
				billItemStatement.setDouble(4, billItem.getItemPrice());
				billItemStatement.executeUpdate();
				
				//Retrieve auto-generated billItemId
				generatedBillItemId = billItemStatement.getGeneratedKeys();
				
				if(generatedBillItemId.next()) {
					
					int billItemId = generatedBillItemId.getInt(1);
					billItem.setBillItemId(billItemId);
				}
				
			}
			
			Bill createdBill = bill;
			
			connection.commit();
			
			return createdBill;

		} catch (SQLException e) {
			
			if (connection != null) {
				connection.rollback();
			}
			
			e.printStackTrace();	
			throw e;

		} finally {
			
			try {
				if (billItemStatement != null) {
					billItemStatement.close();
				}

				if (billStatement != null) {
					billStatement.close();
				}

				if (generatedBillId != null) {
					generatedBillId.close();
				}
				
				if(generatedBillItemId != null) {
					generatedBillItemId.close();
				}
				
				if (connection != null) {
					connection.setAutoCommit(true);
					connection.close();
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				throw e;
			}
		}
		

	}
	
	// Updating a bill
	public Bill updateBill(Bill existingBill, Bill updatedBill) throws SQLException{

		String updateBillQuery = "update bill set bill.billNumber = ?, bill.vendorId = ?, bill.billAmount = ?, bill.billDate = ?, bill.dueDate = ?, bill.balanceAmount = ?, bill.description = ? where bill.billId = ?";
		String updateBillItemQuery = "update billItem set billItem.productId = ?, billItem.quantity = ?, billItem.itemPrice = ? where billItemId = ?";
		String insertBillItemQuery = "insert into billItem(billId,productId,quantity,itemPrice) values(?,?,?,?)";
		String deleteBillItemQuery = "delete from billItem where billItemId = ?";
		String updateTotalPayabaleQuery = "update vendor set totalPayable = totalPayable + ? where vendorId = ?";

		Connection connection = null;
		PreparedStatement billUpdateStatement = null;
		PreparedStatement billItemUpdateStatement = null;
		PreparedStatement billItemInsertStatement = null;
		PreparedStatement billItemDeleteStatement = null;
		PreparedStatement updateTotalPayableStatement = null;
		ResultSet generatedBillItemId = null;
		
		double existingBillAmount = existingBill.getBillAmount();
		double updatedBillAmount = updatedBill.getBillAmount();
		double increasedAmount = updatedBillAmount - existingBillAmount;

		try {

			connection = DriverManager.getConnection(url, username, password);
			billUpdateStatement = connection.prepareStatement(updateBillQuery);
			billItemUpdateStatement = connection.prepareStatement(updateBillItemQuery);
			billItemInsertStatement = connection.prepareStatement(insertBillItemQuery, Statement.RETURN_GENERATED_KEYS);
			billItemDeleteStatement = connection.prepareStatement(deleteBillItemQuery);
			updateTotalPayableStatement = connection.prepareStatement(updateTotalPayabaleQuery);

			connection.setAutoCommit(false);
			
			billUpdateStatement.setString(1, updatedBill.getBillNumber());
			billUpdateStatement.setInt(2, updatedBill.getVendor().getVendorId());
			billUpdateStatement.setDouble(3, updatedBill.getBillAmount());
			billUpdateStatement.setDate(4, Date.valueOf(updatedBill.getBillDate()));
			billUpdateStatement.setDate(5, Date.valueOf(updatedBill.getDueDate()));
			billUpdateStatement.setDouble(6, existingBill.getBalanceAmount() + increasedAmount);
			billUpdateStatement.setString(7, updatedBill.getDescription());
			
			billUpdateStatement.setInt(8, updatedBill.getBillId());
			billUpdateStatement.executeUpdate();

			
			//update totalPayable of vendor
			if(existingBill.getVendor().getVendorId() == updatedBill.getVendor().getVendorId()) {

				//if vendor not updated then just add the difference between existing billAmount and updatedBillAmount
				updateTotalPayableStatement.setDouble(1, increasedAmount); //totalPayable
				updateTotalPayableStatement.setInt(2, updatedBill.getVendor().getVendorId());    //vendorId
				updateTotalPayableStatement.executeUpdate();
			
			}else {
				
				//If vendor updated
				//First subtract the existing billAmount in totalPayabale of the vendor in existing bill
				updateTotalPayableStatement.setDouble(1, -1 * existingBillAmount);  			   //totalPayable -= existingBillAmount
				updateTotalPayableStatement.setInt(2, existingBill.getVendor().getVendorId()); //vendorId
				updateTotalPayableStatement.executeUpdate();
				
				//Then add the updated billAmount in totalPayabale of the vendor in updated bill
				updateTotalPayableStatement.setDouble(1, updatedBillAmount);				  //totalPayable += updatedBillAmount
				updateTotalPayableStatement.setInt(2, updatedBill.getVendor().getVendorId()); //vendorId
				updateTotalPayableStatement.executeUpdate();
				
			}
			
			List<BillItem> existingBillItems = existingBill.getBillItems();
			List<BillItem> updatedBillItems = updatedBill.getBillItems();

			// update or insert bill items
			for (BillItem updatedBillItem : updatedBillItems) {

				boolean flag = false;
				
				//update billItem for the item present in updatedBill that is existing in DB 
				for (BillItem existingBillItem : existingBillItems) {

					if (updatedBillItem.getBillItemId() == existingBillItem.getBillItemId()) {

						billItemUpdateStatement.setInt(1, updatedBillItem.getProduct().getProductId());
						billItemUpdateStatement.setInt(2, updatedBillItem.getQuantity());
						billItemUpdateStatement.setDouble(3, updatedBillItem.getItemPrice());
						billItemUpdateStatement.setInt(4, updatedBillItem.getBillItemId());
						billItemUpdateStatement.executeUpdate();
						
						//remove the billItem from existing billItemList if it is updated
						existingBillItems.remove(existingBillItem);
						flag = true;
						break;

					}
				}
				
				//Insert the billItem for the item present in updatedBill, which is not existing in DB
				if (!flag) {
					billItemInsertStatement.setInt(1, updatedBill.getBillId());
					billItemInsertStatement.setInt(2, updatedBillItem.getProduct().getProductId());
					billItemInsertStatement.setInt(3, updatedBillItem.getQuantity());
					billItemInsertStatement.setDouble(4, updatedBillItem.getItemPrice());
					billItemInsertStatement.executeUpdate();
					
					generatedBillItemId = billItemInsertStatement.getGeneratedKeys();
					
					if(generatedBillItemId.next()) {
						updatedBillItem.setBillItemId(generatedBillItemId.getInt(1));
					}
					
					
				}
			}

			//delete excess billItems from existing bill in DB
			for (BillItem existingBillItem : existingBillItems) {

				billItemDeleteStatement.setInt(1, existingBillItem.getBillItemId());
				billItemDeleteStatement.executeUpdate();


			}

			connection.commit();
			
			return updatedBill;

		} catch (SQLException e) {

			try {
				if (connection != null) {
					connection.rollback();
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
			
			throw e;

		} finally {

			try {
				if (billUpdateStatement != null) {
					billUpdateStatement.close();
				}
				if (billItemUpdateStatement != null) {
					billItemUpdateStatement.close();
				}
				if (billItemInsertStatement != null) {
					billItemInsertStatement.close();
				}
				if (billItemDeleteStatement != null) {
					billItemDeleteStatement.close();
				}
				if (connection != null) {
					connection.setAutoCommit(true);
					connection.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
				throw e;
			}
		}

	}
	
	
	
	
	//deleting a bill
	public boolean deleteBill(Bill bill) throws SQLException{
		
		String deleteBill = "delete from bill where bill.billId = ?";
		String updateTotalPayabaleQuery = "update vendor set totalPayable = totalPayable - ? where vendorId = ?";
		
		if(bill == null) {
			return false;
		}
		
		Connection connection = null;
		PreparedStatement deleteBillPreparedStatement = null;
		PreparedStatement updateTotalPayableStatement = null;
		
		try {
		
			connection = DriverManager.getConnection(url, username, password);
			deleteBillPreparedStatement = connection.prepareStatement(deleteBill);
			updateTotalPayableStatement = connection.prepareStatement(updateTotalPayabaleQuery);
			
				
			connection.setAutoCommit(false);

			updateTotalPayableStatement.setDouble(1, bill.getBillAmount());
			updateTotalPayableStatement.setInt(2, bill.getVendor().getVendorId());
			
			int updatedRows = updateTotalPayableStatement.executeUpdate();
			
			if(updatedRows == 1) {
				
				deleteBillPreparedStatement.setInt(1, bill.getBillId());
	
				int rowAffected = deleteBillPreparedStatement.executeUpdate();
	
				if (rowAffected == 1) {
					connection.commit();
					return true;
				}
			}

			return false;
				
			
		}catch(SQLIntegrityConstraintViolationException sqlIntegrityConstraintViolationException) {
			if(connection != null) {
				connection.rollback();
			}
			sqlIntegrityConstraintViolationException.printStackTrace();
			throw sqlIntegrityConstraintViolationException;
			
		}catch(SQLException e) {
			if(connection != null) {
				connection.rollback();
			}
			e.printStackTrace();
			throw e;
			
		}finally {
			if(deleteBillPreparedStatement != null) {
				deleteBillPreparedStatement.close();
			}
			if(updateTotalPayableStatement != null) {
				updateTotalPayableStatement.close();
			}
			if(connection != null) {
				connection.close();
			}
		}
		
	}
}
