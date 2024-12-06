package com.navin.billings.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.navin.billings.model.Bill;
import com.navin.billings.model.BillPayment;
import com.navin.billings.model.Payment;
import com.navin.billings.model.Vendor;

public class PaymentRepository {
	
	private static final String url = "jdbc:mysql://localhost:3306/billings";;
	private static final String username = "root";
	private static final String password = "root1234";

	public PaymentRepository() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			
		}
	}
	
	//retrieve all payment details
	public List<Payment> getAllPayments() throws SQLException{
		
		String query = "select Payment.PaymentId, Payment.PaymentMode, Payment.PaidAmount, Payment.PaidDate, "
				+ "Vendor.VendorId, Vendor.VendorName, Vendor.GstNumber, "
				+ "BillPayment.BillPaymentId, BillPayment.BillId, BillPayment.AmountApplied, "
				+ "Bill.BillAmount, Bill.BalanceAmount from Payment "
				+ "inner join vendor on Payment.VendorId = Vendor.VendorId "
				+ "inner join BillPayment on Payment.PaymentId = BillPayment.PaymentId inner join Bill on BillPayment.BillId = Bill.BillId";
		
		List<Payment> paymentList = new ArrayList<Payment>();
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ResultSet resultSet = statement.executeQuery(query)
			){
			
			
			while(resultSet.next()) {
				
				int paymentId = resultSet.getInt("PaymentId");
				
				Payment payment = new Payment();
				payment.setPaymentId(paymentId);
				payment.setPaymentMode(resultSet.getString("paymentMode"));
				payment.setPaidAmount(resultSet.getDouble("PaidAmount"));
				payment.setPaidDate(resultSet.getDate("PaidDate").toLocalDate());
				
				Vendor vendor = new Vendor();
				vendor.setVendorId(resultSet.getInt("VendorId"));
				vendor.setVendorName(resultSet.getString("VendorName"));
				vendor.setGstNumber(resultSet.getString("GstNumber"));
				payment.setVendor(vendor);
				
				List<BillPayment> billPaymentList = new ArrayList<BillPayment>();
				
				//iterate until same paymentId to get billPayments for the paymentId
				while(resultSet.getInt("paymentId") == paymentId){
					
					Bill bill = new Bill();
					bill.setBillId(resultSet.getInt("BillId"));
					bill.setBillAmount(resultSet.getDouble("BillAmount"));
					bill.setBalanceAmount(resultSet.getDouble("BalanceAmount"));
					
					BillPayment billPayment = new BillPayment();
					billPayment.setBillPaymentId(resultSet.getInt("BillPaymentId"));
					billPayment.setAmountApplied(resultSet.getDouble("AmountApplied"));
					billPayment.setBill(bill);
					
					billPaymentList.add(billPayment);
					
					if(!resultSet.next()) {
						break;
					}
					
				}
				
				payment.setBillPayments(billPaymentList);
				paymentList.add(payment);
				
				resultSet.previous();
			
			}
			
			return paymentList;
			
		}catch(SQLException sqlException) {
			
			sqlException.printStackTrace();
			throw sqlException;
		}
		
	}
	
	
	
	//Retrieve a particular payment details
	public Payment getPayment(int paymentId) throws SQLException{
		
		String query = "select Payment.PaymentId, Payment.PaymentMode, Payment.PaidAmount, Payment.PaidDate, "
				+ "Vendor.VendorId, Vendor.VendorName, Vendor.GstNumber, "
				+ "BillPayment.BillPaymentId, BillPayment.BillId, BillPayment.AmountApplied, "
				+ "Bill.BillAmount, Bill.BalanceAmount from Payment "
				+ "inner join vendor on Payment.VendorId = Vendor.VendorId "
				+ "inner join BillPayment on Payment.PaymentId = BillPayment.PaymentId inner join Bill on BillPayment.BillId = Bill.BillId "
				+ "where Payment.PaymentId = "+paymentId;
		
		
		Payment payment = null;
		
		try(Connection connection = DriverManager.getConnection(url, username, password);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query)){
			
			if(resultSet.next()) {
				
				payment = new Payment();
				payment.setPaymentId(paymentId);
				payment.setPaymentMode(resultSet.getString("PaymentMode"));
				payment.setPaidAmount(resultSet.getDouble("PaidAmount"));
				payment.setPaidDate(resultSet.getDate("PaidDate").toLocalDate());
				
				Vendor vendor = new Vendor();
				vendor.setVendorId(resultSet.getInt("VendorId"));
				vendor.setVendorName(resultSet.getString("VendorName"));
				vendor.setGstNumber(resultSet.getString("GstNumber"));
				payment.setVendor(vendor);
				
				List<BillPayment> billPaymentList = new ArrayList<BillPayment>();
				
				//iterate until same paymentId to get billPayments for the paymentId
				do{
					
					Bill bill = new Bill();
					bill.setBillId(resultSet.getInt("BillId"));
					bill.setBillAmount(resultSet.getDouble("BillAmount"));
					bill.setBalanceAmount(resultSet.getDouble("BalanceAmount"));
					
					BillPayment billPayment = new BillPayment();
					billPayment.setBillPaymentId(resultSet.getInt("BillPaymentId"));
					billPayment.setBill(bill);
					billPayment.setAmountApplied(resultSet.getDouble("AmountApplied"));
					
					billPaymentList.add(billPayment);
					
				}while(resultSet.next());
				
				payment.setBillPayments(billPaymentList);
				
			}
			
			return payment;
			
		}catch(SQLException sqlException) {
			
			sqlException.printStackTrace();
			throw sqlException;
		}
		
	}
	
	
	
	//Create payment
	public Payment recordPayment(Payment payment) throws SQLException{
		
		String insertPaymentQuery = "Insert into Payment(PaymentMode, PaidAmount, PaidDate, VendorId) values(?,?,?,?)";
		String insertBillPaymentQuery = "Insert into BillPayment(PaymentId, BillId, AmountApplied) values(?,?,?)";
		String updateBillQuery = "Update Bill set balanceAmount = balanceAmount - ? where billId = ?";
		String updateVendorTotalPayableQuery = "Update Vendor set totalPayable = totalPayable - ? where vendorId = ?";
		
		
		Connection connection = null;
		PreparedStatement paymentPreparedStatement = null;
		PreparedStatement billPaymentPreparedStatement = null;
		PreparedStatement billUpdateStatement = null;
		PreparedStatement vendorTotalPayableUpdateStatement = null;
		ResultSet generatedPaymentId = null;
		ResultSet generatedBillPaymentId = null;
		
		try {
			connection = DriverManager.getConnection(url, username, password);
			paymentPreparedStatement = connection.prepareStatement(insertPaymentQuery, Statement.RETURN_GENERATED_KEYS);
			billPaymentPreparedStatement = connection.prepareStatement(insertBillPaymentQuery, Statement.RETURN_GENERATED_KEYS);
			billUpdateStatement = connection.prepareStatement(updateBillQuery);
			vendorTotalPayableUpdateStatement = connection.prepareStatement(updateVendorTotalPayableQuery);
			
			connection.setAutoCommit(false);
			
			
			//Insert payment details in payment table
			paymentPreparedStatement.setString(1, payment.getPaymentMode());
			paymentPreparedStatement.setDouble(2, payment.getPaidAmount());
			paymentPreparedStatement.setDate(3, Date.valueOf(payment.getPaidDate()));
			paymentPreparedStatement.setInt(4, payment.getVendor().getVendorId());
			paymentPreparedStatement.executeUpdate();
			
			//update vendor total payable 
			vendorTotalPayableUpdateStatement.setDouble(1, payment.getPaidAmount());
			vendorTotalPayableUpdateStatement.setInt(2, payment.getVendor().getVendorId());
			vendorTotalPayableUpdateStatement.executeUpdate();
			
			//retrieve auto-generated PaymentId from Payment table
			generatedPaymentId = paymentPreparedStatement.getGeneratedKeys();
			if(generatedPaymentId.next()) {
				payment.setPaymentId(generatedPaymentId.getInt(1));
			}
			
			//Insert BillPayment details in BillPayment table for the particular Payment and update balanceAmount in bill
			for(BillPayment billPayment : payment.getBillPayments()) {
				
				billPaymentPreparedStatement.setInt(1, payment.getPaymentId());
				billPaymentPreparedStatement.setInt(2, billPayment.getBill().getBillId());
				billPaymentPreparedStatement.setDouble(3, billPayment.getAmountApplied());
				billPaymentPreparedStatement.executeUpdate();
				
				billUpdateStatement.setDouble(1, billPayment.getAmountApplied());
				billUpdateStatement.setInt(2, billPayment.getBill().getBillId());
				billUpdateStatement.executeUpdate();
				
				//retrieve auto-generated PaymentId from Payment table
				generatedBillPaymentId = billPaymentPreparedStatement.getGeneratedKeys();
				if(generatedBillPaymentId.next()) {
					billPayment.setBillPaymentId(generatedBillPaymentId.getInt(1));
				}
				
			}
			
			connection.commit();
			return payment;
			
			
		}catch(SQLException sqlException) {	
			
			if(connection != null) {
				connection.rollback();
			}
			
			sqlException.printStackTrace();
			throw sqlException;
			
		}finally {
			
			if(paymentPreparedStatement != null) {
				paymentPreparedStatement.close();
			}
			if(billPaymentPreparedStatement != null) {
				billPaymentPreparedStatement.close();
			}
			if(generatedBillPaymentId != null) {
				generatedBillPaymentId.close();
			}
			if(generatedPaymentId != null) {
				generatedBillPaymentId.close();
			}
			if(connection != null) {
				connection.close();
			}
			
		}	
		
	}
	
	
	
	//Update a payment
	public Payment updateRecordedPayment(Payment existingPayment, Payment updatedPayment) throws SQLException{
		
		String updatePaymentQuery = "UPDATE Payment set PaymentMode = ?, PaidAmount = ?, PaidDate = ?, VendorId = ? where PaymentId = ?";
		String updateBillPaymentQuery = "UPDATE BillPayment set BillId = ? , AmountApplied = ? where BillPaymentId= ?";
		String insertBillPaymentQuery = "INSERT into BillPayment(PaymentId, BillId, AmountApplied) values(?,?,?)";
		String deleteBillPaymentQuery = "DELETE from BillPayment where BillPaymentId = ?";
		String updateBillQuery = "UPDATE Bill set balanceAmount = balanceAmount - ? where billId = ?";
		String updateVendorTotalPayableQuery = "UPDATE Vendor set totalPayable = totalPayable - ? where vendorId = ?";
		
		Connection connection = null;
		PreparedStatement updatePaymentStatement = null;
		PreparedStatement updateBillPaymentStatement = null;
		PreparedStatement insertBillPaymentStatement = null;
		PreparedStatement deleteBillPaymentStatement = null;
		PreparedStatement billUpdateStatement = null;
		PreparedStatement vendorTotalPayableUpdateStatement = null;
		ResultSet generatedBillPaymentId = null;
		
		try {
			connection = DriverManager.getConnection(url, username, password);
			updatePaymentStatement = connection.prepareStatement(updatePaymentQuery);
			updateBillPaymentStatement = connection.prepareStatement(updateBillPaymentQuery);
			insertBillPaymentStatement = connection.prepareStatement(insertBillPaymentQuery, Statement.RETURN_GENERATED_KEYS);
			deleteBillPaymentStatement = connection.prepareStatement(deleteBillPaymentQuery);
			billUpdateStatement = connection.prepareStatement(updateBillQuery);
			vendorTotalPayableUpdateStatement = connection.prepareStatement(updateVendorTotalPayableQuery);
			
			connection.setAutoCommit(false);
			
			
			//update payment details
			updatePaymentStatement.setString(1, updatedPayment.getPaymentMode());
			updatePaymentStatement.setDouble(2, updatedPayment.getPaidAmount());
			updatePaymentStatement.setDate(3, Date.valueOf(updatedPayment.getPaidDate()));
			updatePaymentStatement.setInt(4, updatedPayment.getVendor().getVendorId());
			updatePaymentStatement.setInt(5, updatedPayment.getPaymentId());
			updatePaymentStatement.executeUpdate();
			
			//update vendor TotalPayable
			vendorTotalPayableUpdateStatement.setDouble(1, updatedPayment.getPaidAmount() - existingPayment.getPaidAmount());
			vendorTotalPayableUpdateStatement.setInt(2, updatedPayment.getVendor().getVendorId());
			vendorTotalPayableUpdateStatement.executeUpdate();

				
			List<BillPayment> existingBillPaymentList = existingPayment.getBillPayments();
			List<BillPayment> updatedBillPaymentList = updatedPayment.getBillPayments();
			
			
			//update existing BillPayment or Insert new BillPayment and update the balance amount in bill
			for(BillPayment updatedBillPayment : updatedBillPaymentList) {
				boolean flag = false;
				for(BillPayment existingBillPayment : existingBillPaymentList) {
					
					//if bill payment is already present, then update the bill payment
					if(updatedBillPayment.getBillPaymentId() == existingBillPayment.getBillPaymentId()) {
						
						double updatedBillPaymentAmount = updatedBillPayment.getAmountApplied();
						double existingBillPaymentAmount = existingBillPayment.getAmountApplied();
						double increaseInAppliedAmount = updatedBillPaymentAmount - existingBillPaymentAmount;
						
						//update bill amount if bill is already exists
						if(updatedBillPayment.getBill().getBillId() == existingBillPayment.getBill().getBillId()) {
							
							billUpdateStatement.setDouble(1, increaseInAppliedAmount);
							billUpdateStatement.setInt(2, updatedBillPayment.getBill().getBillId());
							billUpdateStatement.executeUpdate();
						
						}else {
							//if bill is changed
							//update the existing bill balanceAmount
							billUpdateStatement.setDouble(1, -1 * existingBillPayment.getAmountApplied());
							billUpdateStatement.setInt(2, existingBillPayment.getBill().getBillId());
							billUpdateStatement.executeUpdate();
							
							//update the updated bill balaceAmount
							billUpdateStatement.setDouble(1, updatedBillPayment.getAmountApplied());
							billUpdateStatement.setInt(2, updatedBillPayment.getBill().getBillId());
							billUpdateStatement.executeUpdate();
						}
						
						//Update billPayment
						updateBillPaymentStatement.setInt(1,updatedBillPayment.getBill().getBillId());
						updateBillPaymentStatement.setDouble(2, updatedBillPayment.getAmountApplied());
						updateBillPaymentStatement.setInt(3, updatedBillPayment.getBillPaymentId());
						updateBillPaymentStatement.executeUpdate();
						
						//If existing BillPayment is updated then remove the existing billPayment
						existingBillPaymentList.remove(existingBillPayment);
						
						flag = true;
						break;
					}
				}
				
				//if bill payment doesn't exists, then insert the bill payment
				if(!flag) {
					
					billUpdateStatement.setDouble(1, updatedBillPayment.getAmountApplied());
					billUpdateStatement.setInt(2, updatedBillPayment.getBill().getBillId());
					billUpdateStatement.executeUpdate();
					
					
					
					insertBillPaymentStatement.setInt(1, updatedPayment.getPaymentId());
					insertBillPaymentStatement.setInt(2, updatedBillPayment.getBill().getBillId());
					insertBillPaymentStatement.setDouble(3, updatedBillPayment.getAmountApplied());
					insertBillPaymentStatement.executeUpdate();
					
					generatedBillPaymentId = insertBillPaymentStatement.getGeneratedKeys();
					if(generatedBillPaymentId.next()) {
						updatedBillPayment.setBillPaymentId(generatedBillPaymentId.getInt(1));
						
					}
				}
			}
			
			
			//delete the existing BillPayment that is not present in updatedPayment and update the balance amount in bill
			for(BillPayment existingBillPayment : existingBillPaymentList) {
				
				billUpdateStatement.setDouble(1,  -1 * existingBillPayment.getAmountApplied());
				billUpdateStatement.setInt(2, existingBillPayment.getBill().getBillId());
				billUpdateStatement.executeUpdate();
				
				deleteBillPaymentStatement.setInt(1, existingBillPayment.getBillPaymentId());
				deleteBillPaymentStatement.executeUpdate();
				
				
			}
			
			connection.commit();
			
			return updatedPayment;
			
		}catch(SQLException sqlException) {
			
			if(connection != null) {
				connection.rollback();
			}
			
			sqlException.printStackTrace();
			throw sqlException;
			
		}finally {
			
			if(generatedBillPaymentId != null) {
				generatedBillPaymentId.close();
			}
			if(updatePaymentStatement != null) {
				updatePaymentStatement.close();
			}
			if(updateBillPaymentStatement != null) {
				updateBillPaymentStatement.close();
			}
			if(insertBillPaymentStatement != null) {
				insertBillPaymentStatement.close();
			}
			if(deleteBillPaymentStatement != null) {
				deleteBillPaymentStatement.close();
			}
			if(connection != null) {
				connection.close();
			}
		}
		
	}
	
	
	
	//deleting a payment
	public boolean deletePayment(Payment payment) throws SQLException {
		
		String updateBillQuery = "UPDATE Bill set balanceAmount = balanceAmount + ? where billId = ?";
		String updateVendorTotalPayable = "update vendor set totalPayable = totalPayable + ? where vendorId = ?";
		String deletePayment = "delete from Payment where PaymentId = ?";
		
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(url, username, password);
			try(PreparedStatement billUpdateStatement = connection.prepareStatement(updateBillQuery);
				PreparedStatement deletePaymentStatement = connection.prepareStatement(deletePayment);
				PreparedStatement updateVendorTotalPayableStatement = connection.prepareStatement(updateVendorTotalPayable)){
				
				connection.setAutoCommit(false);
				
				//update bill balance amount
				for(BillPayment billPayment : payment.getBillPayments()) {
					
					billUpdateStatement.setDouble(1, billPayment.getAmountApplied());
					billUpdateStatement.setInt(2, billPayment.getBill().getBillId());
					billUpdateStatement.executeUpdate();
				}
				
				//update vendor total payable
				updateVendorTotalPayableStatement.setDouble(1, payment.getPaidAmount());
				updateVendorTotalPayableStatement.setInt(2, payment.getVendor().getVendorId());
				updateVendorTotalPayableStatement.executeUpdate();
				
				//delete the payment
				deletePaymentStatement.setInt(1, payment.getPaymentId());
				int rowsAffected = deletePaymentStatement.executeUpdate();
				connection.commit();
				
				return rowsAffected == 1;
			
			}
		
		}catch(SQLException sqlException) {
			connection.rollback();
			sqlException.printStackTrace();
			throw sqlException;
		}
	}
	
}
