package com.gillsoft.client;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gillsoft.TCPClient;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.util.ContextProvider;

public abstract class AbstractStationsUpdateTask implements Runnable, Serializable {
	
	private static final long serialVersionUID = 1098435170170110085L;
	
	protected String stationId;
	
	public AbstractStationsUpdateTask() {

	}

	public AbstractStationsUpdateTask(String stationId) {
		this.stationId = stationId;
	}

	@Override
	public void run() {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, TCPClient.getStationCacheKey(stationId));
		params.put(RedisMemoryCache.IGNORE_AGE, true);
		params.put(RedisMemoryCache.UPDATE_DELAY, Config.getCacheStationsUpdateDelay());
		
		try {
			TCPClient client = ContextProvider.getBean(TCPClient.class);
			Object cacheObject = null;
			try {
				cacheObject = createCacheObject(client, params);
			} catch (RequestException e) {
			}
			if (cacheObject == null) {
				cacheObject = client.getCache().read(params);
			}
			params.put(RedisMemoryCache.UPDATE_TASK, this);
			client.getCache().write(cacheObject, params);
		} catch (IOCacheException e) {
			e.printStackTrace();
		}
	}

	protected abstract List<Point> createCacheObject(TCPClient client, Map<String, Object> params)
			throws IOCacheException, RequestException;
	
}
