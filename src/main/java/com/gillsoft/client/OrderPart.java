package com.gillsoft.client;

import java.util.ArrayList;
import java.util.List;

public class OrderPart {
	
	private TripIdModel trip;
	
	private List<PassengerModel> passengers = new ArrayList<>();

	public TripIdModel getTrip() {
		return trip;
	}

	public void setTrip(TripIdModel trip) {
		this.trip = trip;
	}

	public List<PassengerModel> getPassengers() {
		return passengers;
	}

	public void setPassengers(List<PassengerModel> passengers) {
		this.passengers = passengers;
	}
	
}
