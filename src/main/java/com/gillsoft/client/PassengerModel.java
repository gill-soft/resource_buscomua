package com.gillsoft.client;

import java.math.BigDecimal;

import com.gillsoft.model.Customer;

public class PassengerModel {
	
	private String uid;
	
	private String seat;
	
	private BigDecimal price;
	
	private Customer customer;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getSeat() {
		return seat;
	}

	public void setSeat(String seat) {
		this.seat = seat;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
}
