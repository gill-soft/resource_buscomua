package com.gillsoft.client;

import java.util.List;
import java.util.Map;

import com.gillsoft.TCPClient;
import com.gillsoft.cache.IOCacheException;

public class ArrivalUpdateTask extends AbstractStationsUpdateTask {

	private static final long serialVersionUID = -4455040510850292003L;
	
	public ArrivalUpdateTask() {

	}

	public ArrivalUpdateTask(String stationId) {
		this.stationId = stationId;
	}

	@Override
	protected List<Point> createCacheObject(TCPClient client, Map<String, Object> params)
			throws IOCacheException, RequestException {
		return client.getCachedArrivalStations(stationId);
	}

}
