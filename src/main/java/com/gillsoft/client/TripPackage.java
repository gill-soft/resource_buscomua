package com.gillsoft.client;

import java.io.Serializable;
import java.util.List;

import com.gillsoft.client.TripsResponse.Trip;
import com.gillsoft.model.request.TripSearchRequest;

public class TripPackage implements Serializable {
	
	private static final long serialVersionUID = 8970859082105159779L;

	private TripSearchRequest request;
	
	private boolean inProgress;
	
	private List<Trip> trips;
	
	private RequestException exception;

	public TripSearchRequest getRequest() {
		return request;
	}

	public void setRequest(TripSearchRequest request) {
		this.request = request;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public List<Trip> getTrips() {
		return trips;
	}

	public void setTrips(List<Trip> trips) {
		this.trips = trips;
	}

	public RequestException getException() {
		return exception;
	}

	public void setException(RequestException exception) {
		this.exception = exception;
	}
	
}
