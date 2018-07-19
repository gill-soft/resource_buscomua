package com.gillsoft;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import com.gillsoft.abstract_rest_service.SimpleAbstractTripSearchService;
import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.client.Config;
import com.gillsoft.client.RequestException;
import com.gillsoft.client.TicketResponse;
import com.gillsoft.client.TicketResponse.Ticket;
import com.gillsoft.client.TripIdModel;
import com.gillsoft.client.TripPackage;
import com.gillsoft.client.TripPoint;
import com.gillsoft.client.TripsResponse.Trip;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Document;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Price;
import com.gillsoft.model.RequiredField;
import com.gillsoft.model.RestError;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Route;
import com.gillsoft.model.Seat;
import com.gillsoft.model.SeatsScheme;
import com.gillsoft.model.Segment;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.TripContainer;
import com.gillsoft.model.Vehicle;
import com.gillsoft.model.request.TripSearchRequest;
import com.gillsoft.model.response.TripSearchResponse;
import com.gillsoft.util.StringUtil;

@RestController
public class SearchServiceController extends SimpleAbstractTripSearchService<TripPackage> {
	
	@Autowired
	private TCPClient client;
	
	@Autowired
	@Qualifier("MemoryCacheHandler")
	private CacheHandler cache;

	@Override
	public List<ReturnCondition> getConditionsResponse(String tripId, String tariffId) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public List<Document> getDocumentsResponse(String tripId) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public List<RequiredField> getRequiredFieldsResponse(String tripId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Route getRouteResponse(String tripId) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public TripSearchResponse getSearchResultResponse(String searchId) {
		return simpleGetSearchResponse(cache, searchId);
	}
	
	@Override
	public void addNextGetSearchCallablesAndResult(List<Callable<TripPackage>> callables, Map<String, Vehicle> vehicles,
			Map<String, Locality> localities, Map<String, Organisation> organisations, Map<String, Segment> segments,
			List<TripContainer> containers, TripPackage result) {
		if (!result.isInProgress()) {
			addResult(vehicles, localities, organisations, segments, containers, result);
		} else if (result.getRequest() != null) {
			addInitSearchCallables(callables, result.getRequest().getLocalityPairs().get(0),
					result.getRequest().getDates().get(0));
		}
	}

	private void addResult(Map<String, Vehicle> vehicles,
			Map<String, Locality> localities, Map<String, Organisation> organisations, Map<String, Segment> segments,
			List<TripContainer> containers, TripPackage result) {
		TripContainer container = new TripContainer();
		container.setRequest(result.getRequest());
		if (result != null
				&& result.getTrips() != null) {

			List<com.gillsoft.model.Trip> trips = new ArrayList<>();
			for (Trip trip : result.getTrips()) {
				if (trip.getServer().getConnect().compareTo(Config.getConnect()) >= 0) {
					
					
					// делаем ид, по которому сможем продать
					TripIdModel model = null;
					try {
						model = new TripIdModel(trip.getServer().getKod(),
								trip.getServer().getName(),
								getAddress(trip.getServer()),
								trip.getTib(),
								trip.getRound().getNum(),
								trip.getFromPoint().getKod(),
								trip.getToPoint().getKod(),
								TCPClient.dateFormat.parse(trip.getDate()));
					} catch (ParseException e) {
					}
					if (model != null) {
						
						// проверяем можно ли продавать на рейс
						try {
							client.getCachedTripInfo(model.getServerCode(), model.getId(),
									model.getFromId(), model.getToId(),
									model.getDate(), null);
						} catch (RequestException e) {
							// если ошибка, то на этот рейс нельзя продать и его в результат не добавляем
							continue;
						} catch (IOCacheException e) {
							// добавляем в результат так как нет еще данных для анализа
						}
						String segmentKey = model.asString();
						com.gillsoft.model.Trip resTrip = new com.gillsoft.model.Trip();
						addSegment(segmentKey, vehicles, localities, organisations, segments, trip);
						resTrip.setId(segmentKey);
						trips.add(resTrip);
					}
				}
			}
			container.setTrips(trips);
		}
		if (result.getException() != null) {
			container.setError(new RestError(result.getException().getMessage()));
		}
		containers.add(container);
	}
	
	private void addSegment(String segmentKey, Map<String, Vehicle> vehicles, Map<String, Locality> localities,
			Map<String, Organisation> organisations, Map<String, Segment> segments, Trip trip) {
		
		// сегменты
		Segment segment = segments.get(segmentKey);
		if (segment == null) {
			segment = new Segment();
			
			segment.setNumber(trip.getRound().getNum() + " "
	        		+ trip.getRound().getValue());
			
			segment.setDeparture(addLocality(localities, trip.getFromPoint()));
			segment.setArrival(addLocality(localities, trip.getToPoint()));
			
			segment.setVehicle(addVehicle(vehicles, trip.getBus()));
			segment.setFreeSeatsCount((int) trip.getFreePlaces());
			
			segment.setCarrier(addOrganisation(organisations, trip.getCarrier()));
			try {
				segment.setDepartureDate(
						TCPClient.dateTimeFormat.parse(trip.getDate()
								+ " " + trip.getDepFromPoint().getTime()));
				segment.setArrivalDate(
						TCPClient.dateTimeFormat.parse(trip.getArrToPoint().getDate()
								+ " " + trip.getArrToPoint().getTime()));
			} catch (ParseException e) {
			}
			addPrice(segment, trip.getPrice());
			segments.put(segmentKey, segment);
		}
	}
	
	private void addPrice(Segment segment, BigDecimal price) {
		Price tripPrice = new Price();
		Tariff tariff = new Tariff();
		tariff.setValue(price);
		tripPrice.setCurrency(Currency.UAH);
		tripPrice.setAmount(price);
		tripPrice.setTariff(tariff);
	}
	
	private Organisation addOrganisation(Map<String, Organisation> organisations, String name) {
		if (name == null) {
			return null;
		}
		String key = StringUtil.md5(name);
		Organisation organisation = organisations.get(key);
		if (organisation == null) {
			organisation = new Organisation();
			organisation.setName(Lang.UA, name);
			organisations.put(key, organisation);
		}
		return new Organisation(key);
	}
	
	private Vehicle addVehicle(Map<String, Vehicle> vehicles, String bus) {
		if (bus == null) {
			return null;
		}
		String key = StringUtil.md5(bus);
		Vehicle vehicle = vehicles.get(key);
		if (vehicle == null) {
			vehicle = new Vehicle();
			vehicle.setModel(bus);
			vehicles.put(key, vehicle);
		}
		return new Vehicle(key);
	}
	
	private Locality addLocality(Map<String, Locality> localities, TripPoint point) {
		Locality locality = localities.get(point.getKod());
		if (locality == null) {
			locality = new Locality();
			locality.setName(Lang.UA, point.getName());
			locality.setAddress(Lang.UA, getAddress(point));
			localities.put(point.getKod(), locality);
		}
		return new Locality(point.getKod());
	}
	
	private String getAddress(TripPoint point) {
		if (point.getAddress() == null
				&& point.getPhone() == null) {
			return null;
		} else {
			return (point.getAddress() == null ? "" : point.getAddress())
					+ (point.getPhone() == null ? "" : " тел.:" + point.getPhone());
		}
	}
	
	@Override
	public List<Seat> getSeatsResponse(String tripId) {
		//TODO
		
		// получаем информацию о рейсе
		try {
			TripIdModel model = new TripIdModel().create(tripId);
			Ticket ticket = client.getCachedTripInfo(model.getServerCode(), model.getId(),
					model.getFromId(), model.getToId(),
					model.getDate(), null);
			if (ticket != null) {
				
			}
		} catch (RequestException e) {
			throw new RestClientException(e.getMessage());
		} catch (IOCacheException e) {
			// добавляем в результат так как нет еще данных для анализа
		}
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public SeatsScheme getSeatsSchemeResponse(String tripId) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public List<Tariff> getTariffsResponse(String tripId) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public TripSearchResponse initSearchResponse(TripSearchRequest request) {
		return simpleInitSearchResponse(cache, request);
	}
	
	@Override
	public void addInitSearchCallables(List<Callable<TripPackage>> callables, String[] pair, Date date) {
		callables.add(() -> {
			TripPackage tripPackage = new TripPackage();
			tripPackage.setRequest(TripSearchRequest.createRequest(pair, date));
			try {
				tripPackage.setTrips(client.getCacehdTrips(pair[0], pair[1], date));
			} catch (IOCacheException e) {
				tripPackage.setInProgress(true);
			} catch (RequestException e) {
				tripPackage.setInProgress(false);
				tripPackage.setException(e);
			}
			return tripPackage;
		});
	}

	@Override
	public List<Seat> updateSeatsResponse(String tripId, List<Seat> seats) {
		throw TCPClient.createUnavailableMethod();
	}

}
