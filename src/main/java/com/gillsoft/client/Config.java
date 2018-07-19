package com.gillsoft.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class Config {
	
	private static Properties properties;
	
	static {
		try {
			Resource resource = new ClassPathResource("resource.properties");
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getUrl() {
		return properties.getProperty("url");
	}
	
	public static int getPort() {
		return Integer.valueOf(properties.getProperty("port"));
	}
	
	public static int getSoTimeout() {
		return Integer.valueOf(properties.getProperty("so.timeout"));
	}
	
	public static int getPoolSize() {
		return Integer.valueOf(properties.getProperty("pool.size"));
	}
	
	public static String getPrefix() {
		return properties.getProperty("prefix");
	}
	
	public static String getAgent() {
		return properties.getProperty("agent");
	}
	
	public static String getFrom() {
		return properties.getProperty("from");
	}
	
	public static String getTo() {
		return properties.getProperty("to");
	}
	
	public static String getWorkplace() {
		return properties.getProperty("workplace");
	}
	
	public static String getMinPrice() {
		return properties.getProperty("min.price");
	}
	
	public static BigDecimal getConnect() {
		return new BigDecimal(properties.getProperty("connect"));
	}
	
	public static String getRegularity() {
		return properties.getProperty("regularity");
	}
	
	public static int getRequestTimeout() {
		return Integer.valueOf(properties.getProperty("request.timeout"));
	}
	
	public static int getSearchRequestTimeout() {
		return Integer.valueOf(properties.getProperty("request.search.timeout"));
	}
	
	public static long getCacheTripTimeToLive() {
		return Long.valueOf(properties.getProperty("cache.trip.time.to.live"));
	}
	
	public static long getCacheTripUpdateDelay() {
		return Long.valueOf(properties.getProperty("cache.trip.update.delay"));
	}
	
	public static long getCacheTripSeatsTimeToLive() {
		return Long.valueOf(properties.getProperty("cache.trip.seats.time.to.live"));
	}
	
	public static long getCacheTripSeatsUpdateDelay() {
		return Long.valueOf(properties.getProperty("cache.trip.seats.update.delay"));
	}
	
	public static long getCacheStationsUpdateDelay() {
		return Long.valueOf(properties.getProperty("cache.stations.update.delay"));
	}
	
	public static long getCacheErrorTimeToLive() {
		return Long.valueOf(properties.getProperty("cache.error.time.to.live"));
	}
	
	public static long getCacheErrorUpdateDelay() {
		return Long.valueOf(properties.getProperty("cache.error.update.delay"));
	}
	
}
