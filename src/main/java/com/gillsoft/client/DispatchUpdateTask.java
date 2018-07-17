package com.gillsoft.client;

import java.util.List;
import java.util.Map;

import com.gillsoft.TCPClient;
import com.gillsoft.cache.IOCacheException;

public class DispatchUpdateTask extends AbstractStationsUpdateTask {

	private static final long serialVersionUID = 156273606948615373L;
	
	public DispatchUpdateTask() {
		super();
	}

	public DispatchUpdateTask(String stationId) {
		super(stationId);
	}

	@Override
	protected List<Point> createCacheObject(TCPClient client, Map<String, Object> params)
			throws IOCacheException, RequestException {
		return client.getDispatchStations();
	}


}
