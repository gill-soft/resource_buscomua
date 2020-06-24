package com.gillsoft.client;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gillsoft.TCPClient;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.client.TripsResponse.Trip;
import com.gillsoft.util.ContextProvider;

public class TripsUpdateTask implements Runnable, Serializable {
	
	private static final long serialVersionUID = -3455321368029371037L;
	
	private String dispatchId;
	private String arriveId;
	private Date dispatchDate;
	
	public TripsUpdateTask() {
		
	}

	public TripsUpdateTask(String dispatchId, String arriveId, Date dispatchDate) {
		this.dispatchId = dispatchId;
		this.arriveId = arriveId;
		this.dispatchDate = dispatchDate;
	}

	@Override
	public void run() {
		TCPClient client = ContextProvider.getBean(TCPClient.class);
		client.addSearchTask(() -> {
			Map<String, Object> params = new HashMap<>();
			params.put(RedisMemoryCache.OBJECT_NAME, TCPClient.getTripCacheKey(dispatchId, arriveId, dispatchDate));
			params.put(RedisMemoryCache.UPDATE_TASK, this);
			params.put(RedisMemoryCache.UPDATE_DELAY, Config.getCacheTripUpdateDelay());
			
			Object cache = null;
			try {
				List<Trip> trips = client.getTrips(dispatchId, arriveId, dispatchDate);
				params.put(RedisMemoryCache.TIME_TO_LIVE, getTimeToLive(trips));
				cache = trips;
			} catch (RequestException e) {
				
				// ошибку поиска тоже кладем в кэш но с другим временем жизни
				params.put(RedisMemoryCache.TIME_TO_LIVE, Config.getCacheErrorTimeToLive());
				params.put(RedisMemoryCache.UPDATE_DELAY, Config.getCacheErrorUpdateDelay());
				cache = e;
			}
			try {
				client.getCache().write(cache, params);
			} catch (IOCacheException e) {
			}
		});
	}
	
	// время жизни до момента самого позднего отправления
	private long getTimeToLive(List<Trip> trips) {
		if (Config.getCacheTripTimeToLive() != 0) {
			return Config.getCacheTripTimeToLive();
		}
		long max = 0;
		for (Trip trip : trips) {
			try {
				Date date = TCPClient.dateTimeFormat.parse(trip.getDate() + " " + trip.getDepFromPoint().getTime());
				if (date.getTime() > max) {
					max = date.getTime();
				}
			} catch (ParseException e) {
			}
		}
		if (max == 0
				|| max < System.currentTimeMillis()) {
			return Config.getCacheErrorTimeToLive();
		}
		return max - System.currentTimeMillis();
	}

}
