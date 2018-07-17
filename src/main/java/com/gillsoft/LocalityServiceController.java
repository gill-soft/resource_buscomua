package com.gillsoft;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.abstract_rest_service.AbstractLocalityService;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.client.Point;
import com.gillsoft.client.Point.Location.Item;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.request.LocalityRequest;

@RestController
public class LocalityServiceController extends AbstractLocalityService {
	
	private static List<Locality> all;
	private static Map<String, List<String>> binding;
	
	@Autowired
	private TCPClient client;

	@Override
	public List<Locality> getAllResponse(LocalityRequest request) {
		return getAllLocalities(request);
	}

	@Override
	public Map<String, List<String>> getBindingResponse(LocalityRequest request) {
		createLocalities();
		return binding;
	}

	@Override
	public List<Locality> getUsedResponse(LocalityRequest request) {
		return getAllLocalities(request);
	}
	
	private List<Locality> getAllLocalities(LocalityRequest request) {
		createLocalities();
		if (all != null) {
			return all;
		}
		return null;
	}
	
	@Scheduled(initialDelay = 60000, fixedDelay = 900000)
	private void createLocalities() {
		if (LocalityServiceController.all == null) {
			synchronized (LocalityServiceController.class) {
				if (LocalityServiceController.all == null) {
					boolean cacheError = true;
					do {
						try {
							// пункты отправления
							List<Point> points = client.getCachedDispatchStations();
							if (points != null) {
								List<Locality> all = new CopyOnWriteArrayList<>();
								Map<String, List<String>> binding = new ConcurrentHashMap<>();
								for (Point point : points) {
									Locality dispatch = createLocality(point);
									all.add(dispatch);
									
									// получаем пункты прибытия
									List<Point> arrivalPoints = client.getCachedArrivalStations(point.getKod());
									if (arrivalPoints != null) {
										List<String> arrivalIds = new CopyOnWriteArrayList<>();
										for (Point arrivalPoint : points) {
											Locality arrival = createLocality(arrivalPoint);
											all.add(arrival);
										}
										binding.put(dispatch.getId(), arrivalIds);
									}
								}
								LocalityServiceController.all = all;
								LocalityServiceController.binding = binding;
								cacheError = false;
							}
						} catch (IOCacheException e) {
							try {
								TimeUnit.MILLISECONDS.sleep(100);
							} catch (InterruptedException ie) {
							}
						}
					} while (cacheError);
				}
			}
		}
	}
	
	private Locality createLocality(Point point) {
		Locality locality = new Locality();
		locality.setId(point.getKod());

		locality.setAddress(Lang.UA, getAddress(point, item -> item.getNameUa()));
		locality.setAddress(Lang.RU, getAddress(point, item -> item.getNameRu()));
		locality.setAddress(Lang.EN, getAddress(point, item -> item.getNameEn()));
		
		locality.setName(Lang.UA, point.getNameUa());
		locality.setName(Lang.RU, point.getNameUa());
		locality.setName(Lang.EN, point.getNameUa());
		
		return locality;
	}
	
	private String getAddress(Point point, Function<Item, String> func) {
		return point.getLocation().getItem().stream().sorted((p1, p2) -> { 
					return p1.getLevel().compareTo(p2.getLevel());
			}).map(func).collect(Collectors.joining(", "));
	}
	
	/**
	 * Возвращает населенный пункт по его ид
	 * 
	 * @param id
	 *            Для пунктов отправления ид пункта отправления, для пунктов
	 *            прибытия ид отправления + ";" + ид прибытия
	 * @return Населенный пункт
	 */
//	public static Locality getLocality(String id) {
//		if (all == null) {
//			return null;
//		}
//		return internalAll.get(id);
//	}

}
