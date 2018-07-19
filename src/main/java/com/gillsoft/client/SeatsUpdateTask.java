package com.gillsoft.client;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.gillsoft.TCPClient;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.util.ContextProvider;

public class SeatsUpdateTask implements Runnable, Serializable {

	private static final long serialVersionUID = 9053604246842096838L;
	
	private String serverId;
	private String tripId;
	private String dispatchId;
	private String arriveId;
	private Date dispatchDate;
	
	public SeatsUpdateTask() {
		
	}

	public SeatsUpdateTask(String serverId, String tripId, String dispatchId, String arriveId, Date dispatchDate) {
		this.serverId = serverId;
		this.tripId = tripId;
		this.dispatchId = dispatchId;
		this.arriveId = arriveId;
		this.dispatchDate = dispatchDate;
	}

	@Override
	public void run() {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, TCPClient.getTripInfoCacheKey(
				serverId, tripId, dispatchId, arriveId, dispatchDate));
		params.put(RedisMemoryCache.UPDATE_TASK, this);
		params.put(RedisMemoryCache.TIME_TO_LIVE, Config.getCacheTripSeatsTimeToLive());
		params.put(RedisMemoryCache.UPDATE_DELAY, Config.getCacheTripSeatsUpdateDelay());
		
		TCPClient client = ContextProvider.getBean(TCPClient.class);
		Object cache = null;
		try {
			cache = client.getTripInfo(serverId, tripId, dispatchId, arriveId, dispatchDate);
		} catch (RequestException e) {
			
			// ошибку тоже кладем в кэш до времени отправления рейсов и не обновляем
			params.put(RedisMemoryCache.TIME_TO_LIVE, dispatchDate.getTime() - System.currentTimeMillis());
			params.put(RedisMemoryCache.UPDATE_DELAY, null);
			cache = e;
		}
		try {
			client.getCache().write(cache, params);
		} catch (IOCacheException e) {
		}
	}

}
