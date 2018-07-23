package com.gillsoft.client;

import com.gillsoft.model.AbstractJsonModel;

public class ServicesIdModel extends AbstractJsonModel {

	private static final long serialVersionUID = -7108292911174683245L;

	private String uid;

	private TripIdModel trip;
	
	public ServicesIdModel() {
		
	}

	public ServicesIdModel(String uid, TripIdModel trip) {
		this.uid = uid;
		this.trip = trip;
	}

	public TripIdModel getTrip() {
		return trip;
	}

	public void setTrip(TripIdModel trip) {
		this.trip = trip;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
	public ServicesIdModel create(String json) {
		return (ServicesIdModel) super.create(json);
	}

}
