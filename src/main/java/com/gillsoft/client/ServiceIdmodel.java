package com.gillsoft.client;

import com.gillsoft.model.AbstractJsonModel;

public class ServiceIdmodel extends AbstractJsonModel {

	private static final long serialVersionUID = -7108292911174683245L;

	private String uid;

	private TripIdModel trip;
	
	public ServiceIdmodel() {
		
	}

	public ServiceIdmodel(String uid, TripIdModel trip) {
		this.uid = uid;
		this.trip = trip;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public TripIdModel getTrip() {
		return trip;
	}

	public void setTrip(TripIdModel trip) {
		this.trip = trip;
	}

	@Override
	public ServiceIdmodel create(String json) {
		return (ServiceIdmodel) super.create(json);
	}

}
