package com.gillsoft;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.abstract_rest_service.AbstractOrderService;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.client.BuyRequestType;
import com.gillsoft.client.CanReturnType;
import com.gillsoft.client.CancelResponse;
import com.gillsoft.client.MoneyType;
import com.gillsoft.client.OrderIdModel;
import com.gillsoft.client.OrderPart;
import com.gillsoft.client.PassengerModel;
import com.gillsoft.client.PricePart;
import com.gillsoft.client.RequestException;
import com.gillsoft.client.ReturnStatementType;
import com.gillsoft.client.ServicesIdModel;
import com.gillsoft.client.TicketResponse;
import com.gillsoft.client.TicketResponse.ReturnRuless;
import com.gillsoft.client.TripIdModel;
import com.gillsoft.model.CalcType;
import com.gillsoft.model.Commission;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Lang;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Price;
import com.gillsoft.model.RestError;
import com.gillsoft.model.ReturnCondition;
import com.gillsoft.model.Seat;
import com.gillsoft.model.Segment;
import com.gillsoft.model.ServiceItem;
import com.gillsoft.model.Tariff;
import com.gillsoft.model.ValueType;
import com.gillsoft.model.request.OrderRequest;
import com.gillsoft.model.response.OrderResponse;

@RestController
public class OrderServiceController extends AbstractOrderService {
	
	private static final String PAYMENT_CODE = "БНЛ";
	private static final String TARIFF_CODE = "ТРФ";
	private static final String RETURN_CODE = "ВЗР";
	private static final String YES_CODE = "yes";
	
	@Autowired
	private TCPClient client;
	
	@Autowired
	private SearchServiceController controller; 

	@Override
	public OrderResponse createResponse(OrderRequest request) {

		// формируем ответ
		OrderResponse response = new OrderResponse();
		response.setCustomers(request.getCustomers());
		
		// копия для определения пассажиров
		List<ServiceItem> items = new ArrayList<>();
		items.addAll(request.getServices());
		
		Map<String, Organisation> organisations = new HashMap<>();
		Map<String, Locality> localities = new HashMap<>();
		Map<String, Segment> segments = new HashMap<>();
		List<ServiceItem> resultItems = new ArrayList<>();
		
		// список билетов
		OrderIdModel orderId = new OrderIdModel();
		
		// группируем по рейсу
		for (Entry<String, List<ServiceItem>> order : getTripItems(request).entrySet()) {
			
			TripIdModel tripIdModel = new TripIdModel().create(order.getKey());
			
			try {
				TicketResponse ticket = controller.getTicketSeats(tripIdModel);
				OrderPart part = new OrderPart();
				part.setTrip(tripIdModel);
				orderId.getParts().add(part);
				for (ServiceItem serviceItem : order.getValue()) {
					ServiceItem item = new ServiceItem();
					
					String uid = null;
					if (serviceItem.getAdditionals() == null
							|| serviceItem.getAdditionals().get("uniqueId") == null) {
						uid = client.getRandomId();
					} else {
						uid = serviceItem.getAdditionals().get("uniqueId");
						client.saveUid(uid);
					}
					item.setId(new ServicesIdModel(uid, tripIdModel).asString());
					
					item.setNumber(client.createUid(uid));
					
					// рейс
					item.setSegment(addSegment(tripIdModel, localities, organisations, segments, ticket.getTicket().get(0)));
					
					// пассажир
					item.setCustomer(serviceItem.getCustomer());
					
					// место
					item.setSeat(createSeat(serviceItem.getSeat(), ticket));
					
					// стоимость
					item.setPrice(createPrice(ticket.getTicket().get(0).getMoney(), ticket.getReturnRuless()));
					
					// доппараметры для билета
					item.setAdditionals(new HashMap<>());
					item.getAdditionals().put("serverCode", tripIdModel.getServerCode());
					item.getAdditionals().put("serverName", tripIdModel.getServerName());
					item.getAdditionals().put("serverAddress", tripIdModel.getServerAddress());
					item.getAdditionals().put("tib", tripIdModel.getTib());
					
					// добавляем данные о пассажире необходимые при продаже
					PassengerModel passengerModel = new PassengerModel();
					passengerModel.setUid(uid);
					passengerModel.setCustomer(request.getCustomers().get(item.getCustomer().getId()));
					passengerModel.setPrice(item.getPrice().getAmount());
					passengerModel.setSeat(item.getSeat().getId());
					part.getPassengers().add(passengerModel);
					
					resultItems.add(item);
				}
			} catch (Exception e) {
				for (ServiceItem item : order.getValue()) {
					item.setError(new RestError(e.getMessage()));
					resultItems.add(item);
				}
			}
		}
		response.setOrderId(orderId.asString());
		response.setLocalities(localities);
		response.setOrganisations(organisations);
		response.setSegments(segments);
		response.setServices(resultItems);
		return response;
	}
	
	private Segment addSegment(TripIdModel model, Map<String, Locality> localities,
			Map<String, Organisation> organisations, Map<String, Segment> segments, TicketResponse.Ticket ticket) {
		String segmentId = model.asString();
		Segment segment = segments.get(segmentId);
		if (segment == null) {
			segment = new Segment();
			
			segment.setNumber(ticket.getRound().getNum());
			
			segment.setDeparture(controller.addLocality(localities, ticket.getPointFrom(), model.getFromId()));
			segment.setArrival(controller.addLocality(localities, ticket.getPointTo(), model.getToId()));
			
			segment.setCarrier(controller.addOrganisation(organisations, ticket.getCarrier()));
			segment.setInsurance(controller.addOrganisation(organisations, ticket.getInsurance()));
			
			try {
				segment.setDepartureDate(
						TCPClient.dateTimeFormat.parse(ticket.getTimeDeparture().getDate()
								+ " " + ticket.getTimeDeparture().getTime()));
				segment.setArrivalDate(
						TCPClient.dateTimeFormat.parse(ticket.getTimeArrival().getDate()
								+ " " + ticket.getTimeArrival().getTime()));
			} catch (ParseException e) {
			}
			segments.put(segmentId, segment);
		}
		Segment result = new Segment();
		result.setId(segmentId);
		return result;
	}
	
	/*
	 * Сумма, которую следует перечислить системе «Автовокзал-Сеть» складывается
	 * из стоимости билета <payment> и стоимости обслуживания операций <addTax>.
	 */
	private Price createPrice(MoneyType money, ReturnRuless rules, String paymentCode, String tariffCode,
			boolean addCommissions) {
		BigDecimal amount = null;
		Tariff tariff = null;
		List<Commission> commissions = new ArrayList<>();
		for (PricePart part : money.getPayment()) {
			
			// если сумма
			if (part.getCode().equals(paymentCode)) {
				amount = new BigDecimal(part.getAmount());
				
			// если тариф
			} else if (part.getCode().equals(tariffCode)) {
				tariff = new Tariff();
				tariff.setValue(new BigDecimal(part.getAmount()));
				tariff.setCode(part.getCode());
				tariff.setName(part.getName());
			} else if (!part.getCode().contains("nВ")
					&& !part.getCode().endsWith("н")) {
				commissions.add(createCommission(part));
			}
		}
		for (PricePart part : money.getAddTax()) {
			commissions.add(createCommission(part));
		}
		if (tariff == null) {
			tariff = new Tariff();
			tariff.setValue(amount);
		}
		// по договору сборы насчитываются не на тариф, а на полную стоимость. по этому за тариф принимаем стоимость
		tariff.setValue(amount);
		
		Price price = new Price();
		price.setAmount(amount);
		price.setCurrency(Currency.UAH);
		price.setTariff(tariff);
		if (addCommissions) {
			price.setCommissions(commissions);
		}
		if (amount != null
				&& rules != null) {
			List<ReturnCondition> conditions = new ArrayList<>();
			for (CanReturnType returnType : rules.getCanReturn()) {
				if ("yes".equals(returnType.getValue())) {
					ReturnCondition condition = new ReturnCondition();
					condition.setMinutesBeforeDepart(returnType.getSecToDepMore() / 60);
					condition.setReturnPercent(new BigDecimal(100).subtract(returnType.getRetain().multiply(new BigDecimal(100)).divide(amount, 2, RoundingMode.HALF_UP)));
					condition.setTitle(Lang.UA, "Повернення " + condition.getReturnPercent() + "%");
					if (condition.getMinutesBeforeDepart() > 0) {
						condition.setDescription(Lang.UA, "За "
								+ (condition.getMinutesBeforeDepart() / 60 > 0 ? (condition.getMinutesBeforeDepart() / 60) + " год. " : "")
								+ (condition.getMinutesBeforeDepart() % 60 > 0 ? (condition.getMinutesBeforeDepart() % 60) + " хв. " : "")
								+ " до відправлення повертається " + condition.getReturnPercent() + "%");
					} else {
						condition.setDescription(Lang.UA, "Через "
								+ (condition.getMinutesBeforeDepart() / 60 > 0 ? (condition.getMinutesBeforeDepart() / 60) + " год. " : "")
								+ (condition.getMinutesBeforeDepart() % 60 > 0 ? (condition.getMinutesBeforeDepart() % 60) + " хв. " : "")
								+ " після відправлення повертається " + condition.getReturnPercent() + "%");
					}
					conditions.add(condition);
				}
			}
			if (!conditions.isEmpty()) {
				tariff.setReturnConditions(conditions);
			}
		}
		return price;
	}
	
	private Price createPrice(MoneyType money, ReturnRuless rules) {
		return createPrice(money, rules, PAYMENT_CODE, TARIFF_CODE, true);
	}
	
	private Commission createCommission(PricePart part) {
		Commission commission = new Commission();
		commission.setCode(part.getCode());
		commission.setName(part.getName());
		commission.setType(ValueType.FIXED);
		
		// чтобы сборы не участвовали в очистке, то ставим их от тарифа
		commission.setValueCalcType(CalcType.FROM);
		commission.setValue(new BigDecimal(part.getAmount()));
		return commission;
	}
	
	private Seat createSeat(Seat seat, TicketResponse response) {
		if (response.getSeats() != null) {
			if (seat != null) {
				for (TicketResponse.Seats.Seat respSeat : response.getSeats().getSeat()) {
					if (Objects.equals(respSeat.getValue(), seat.getId())) {
						seat.setNumber(seat.getId());
						response.getSeats().getSeat().remove(respSeat);
						return seat;
					}
				}
			}
			// если указанного места нет, то первое свободное
			TicketResponse.Seats.Seat respSeat = response.getSeats().getSeat().remove(0);
			Seat newSeat = new Seat();
			newSeat.setId(String.valueOf(respSeat.getValue()));
			newSeat.setNumber(newSeat.getId());
			return newSeat;
		}
		return null;
	}
	
	private Map<String, List<ServiceItem>> getTripItems(OrderRequest request) {
		Map<String, List<ServiceItem>> trips = new HashMap<>();
		for (ServiceItem item : request.getServices()) {
			String tripId = item.getSegment().getId();
			List<ServiceItem> items = trips.get(tripId);
			if (items == null) {
				items = new ArrayList<>();
				trips.put(tripId, items);
			}
			items.add(item);
		}
		return trips;
	}

	@Override
	public OrderResponse addServicesResponse(OrderRequest request) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse removeServicesResponse(OrderRequest request) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse updateCustomersResponse(OrderRequest request) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse getResponse(String orderId) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse getServiceResponse(String serviceId) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse bookingResponse(String orderId) {
		throw TCPClient.createUnavailableMethod();
	}
	
	private List<BuyRequestType.Ticket> createRequestTickets(List<PassengerModel> passengerModels) {
		List<BuyRequestType.Ticket> tickets = new ArrayList<>();
		for (PassengerModel passModel : passengerModels) {
			BuyRequestType.Ticket ticket = new BuyRequestType.Ticket();
			ticket.setUid(client.createUid(passModel.getUid()));
			String name = String.join(" ", passModel.getCustomer().getSurname(),
					passModel.getCustomer().getName());
			name = name.replaceAll("[^ \\-'\\.a-zA-ZА-Яа-яіІїЇєЄ]", "");
			if (name.length() > 32) {
				name = name.substring(0, 32);
			}
			ticket.setPassengerInfo(name);
			ticket.setEmail(passModel.getCustomer().getEmail());
			if (passModel.getCustomer().getPhone() != null) {
				ticket.setPhone(passModel.getCustomer().getPhone().replaceAll("\\D", ""));
			}
			ticket.setSeat(passModel.getSeat());
			tickets.add(ticket);
		}
		return tickets;
	}

	@Override
	public OrderResponse confirmResponse(String orderId) {

		// формируем ответ
		OrderResponse response = new OrderResponse();
		List<ServiceItem> resultItems = new ArrayList<>();
		
		// преобразовываем ид заказа в объкт
		OrderIdModel orderIdModel = new OrderIdModel().create(orderId);
		
		// выкупаем заказы и формируем ответ
		for (OrderPart part : orderIdModel.getParts()) {
			try {
				List<BuyRequestType.Ticket> tickets = createRequestTickets(part.getPassengers());
				
				TicketResponse ticketResponse = client.buy(part.getTrip().getServerCode(),
						part.getTrip().getId(),
						part.getTrip().getFromId(),
						part.getTrip().getToId(),
						part.getTrip().getDate(),
						tickets);
				for (TicketResponse.Ticket ticket : ticketResponse.getTicket()) {
					for (PassengerModel passModel : part.getPassengers()) {
						if (Objects.equals(ticket.getUid(), client.createUid(passModel.getUid()))) {
							ServiceItem item = new ServiceItem();
							item.setId(new ServicesIdModel(passModel.getUid(), part.getTrip()).asString());
							item.setConfirmed(true);
							
							// обновляем стоимость билета
							item.setPrice(createPrice(ticket.getMoney(), ticketResponse.getReturnRuless()));
							
							// добавляем AsUid
							item.setAdditionals(new HashMap<>());
							item.getAdditionals().put("AsUid", ticket.getAsUID().getValue());
							item.getAdditionals().put("Askod", ticket.getAsUID().getAskod());
							item.getAdditionals().put("distance", ticket.getDistance());
							item.getAdditionals().put("route", ticket.getRound().getPointFrom().getValue()
									+ " " + ticket.getRound().getPointTo().getValue());
							
							// сохраняем AsUid в кэш, чтобы можно было аннулировать потом заказ
							saveAsUid(ticket.getUid(), ticket.getAsUID().getValue());
							
							resultItems.add(item);
							break;
						}
					}
				}
			} catch (Exception e) {
				for (PassengerModel passModel : part.getPassengers()) {
					addServiceItems(resultItems, new ServicesIdModel(passModel.getUid(), part.getTrip()),
							false, new RestError(e.getMessage()));
				}
			}
		}
		response.setOrderId(orderId);
		response.setServices(resultItems);
		return response;
	}
	
	private void saveAsUid(String uid, String asUid) {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, TCPClient.getUidCacheKey(uid));
		params.put(RedisMemoryCache.IGNORE_AGE, true);
		try {
			client.getCache().write(asUid, params);
		} catch (IOCacheException e) {
		}
	}
	
	private String getAsUid(String uid) {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, TCPClient.getUidCacheKey(uid));
		try {
			return String.valueOf(client.getCache().read(params));
		} catch (IOCacheException e) {
		}
		return null;
	}

	@Override
	public OrderResponse cancelResponse(String orderId) {
		// формируем ответ
		OrderResponse response = new OrderResponse();
		List<ServiceItem> resultItems = new ArrayList<>();
		
		// преобразовываем ид заказа в объкт
		OrderIdModel orderIdModel = new OrderIdModel().create(orderId);
		
		// отменяем заказы и формируем ответ
		for (OrderPart part : orderIdModel.getParts()) {
			for (PassengerModel passModel : part.getPassengers()) {
				try {
					String asUid = getAsUid(client.createUid(passModel.getUid()));
					client.cancel(part.getTrip().getServerCode(),
							part.getTrip().getId(),
							part.getTrip().getFromId(),
							part.getTrip().getToId(),
							part.getTrip().getDate(),
							passModel.getUid(),
							asUid, TCPClient.CANCELATION_MODE);
					addServiceItems(resultItems, new ServicesIdModel(passModel.getUid(), part.getTrip()),
							true, null);
				} catch (Exception e) {
					addServiceItems(resultItems, new ServicesIdModel(passModel.getUid(), part.getTrip()),
							false, new RestError(e.getMessage()));
				}
			}
		}
		response.setOrderId(orderId);
		response.setServices(resultItems);
		return response;
	}
	
	private void addServiceItems(List<ServiceItem> resultItems, ServicesIdModel ticket, boolean confirmed,
			RestError error) {
		ServiceItem serviceItem = new ServiceItem();
		serviceItem.setId(ticket.asString());
		serviceItem.setConfirmed(confirmed);
		serviceItem.setError(error);
		resultItems.add(serviceItem);
	}

	@Override
	public OrderResponse prepareReturnServicesResponse(OrderRequest request) {
		OrderResponse response = new OrderResponse();
		response.setServices(new ArrayList<>(request.getServices().size()));
		for (ServiceItem serviceItem : request.getServices()) {
			ServicesIdModel model = new ServicesIdModel().create(serviceItem.getId());
			try {
				String asUid = getAsUid(client.createUid(model.getUid()));
				TicketResponse ticketResponse = client.getStatus(
						model.getTrip().getServerCode(),
						model.getTrip().getId(),
						model.getTrip().getFromId(),
						model.getTrip().getToId(),
						model.getTrip().getDate(),
						model.getUid(), asUid);
				if (ticketResponse.getReturnStatement() != null) {
					Price returnPrice = createPrice(ticketResponse.getReturnStatement().getMoney(), null, RETURN_CODE, null, false);
					addReturnPrice(serviceItem, returnPrice, ticketResponse.getReturnStatement());
				} else if (ticketResponse.getCanReturn() != null
						&& !Objects.equals(YES_CODE, ticketResponse.getCanReturn().getValue())) {
					throw new Exception(ticketResponse.getCanReturn().getReason());
				}
			} catch (Exception e) {
				serviceItem.setError(new RestError(e.getMessage()));
			}
			response.getServices().add(serviceItem);
		}
		return response;
	}

	@Override
	public OrderResponse returnServicesResponse(OrderRequest request) {
		OrderResponse response = new OrderResponse();
		response.setServices(new ArrayList<>(request.getServices().size()));
		for (ServiceItem serviceItem : request.getServices()) {
			ServicesIdModel model = new ServicesIdModel().create(serviceItem.getId());
			try {
				String asUid = getAsUid(client.createUid(model.getUid()));
				
				ReturnStatementType returnStatement = null;
				
				// для исходной стоимости
				TicketResponse ticketResponse = null;
				try {
					ticketResponse = client.getStatus(
							model.getTrip().getServerCode(),
							model.getTrip().getId(),
							model.getTrip().getFromId(),
							model.getTrip().getToId(),
							model.getTrip().getDate(),
							model.getUid(), asUid);
					
					// проверяем возвращен ли уже билет
					if (ticketResponse != null
							&& ticketResponse.getReturnStatement() != null) {
						returnStatement = ticketResponse.getReturnStatement();
					}
				} catch (Exception e) {
				}
				// если еще не вернули
				CancelResponse cancelResponse = null;
				if (returnStatement == null) {
					cancelResponse = client.cancel(
							model.getTrip().getServerCode(),
							model.getTrip().getId(),
							model.getTrip().getFromId(),
							model.getTrip().getToId(),
							model.getTrip().getDate(),
							model.getUid(),
							asUid, TCPClient.RETURN_MODE);
				}
				// исходная стоимость
				Price salePrice = null;
				if (returnStatement == null
						&& ticketResponse != null) {
					for (TicketResponse.Ticket ticket : ticketResponse.getTicket()) {
						if (Objects.equals(ticket.getUid(), client.createUid(model.getUid()))) {
							salePrice = createPrice(ticket.getMoney(), ticketResponse.getReturnRuless());
						}
					}
				}
				if (cancelResponse != null) {
					returnStatement = cancelResponse.getReturnStatement();
				}
				Price returnPrice = createPrice(returnStatement.getMoney(), null, RETURN_CODE, null, false);
				
				// если есть исходная стоимость, то считаем возврат
				if (salePrice != null) {
					
					// стоимость удержания
					Price retainPrice = createPrice(returnStatement.getMoney(), null);
					salePrice.setAmount(returnPrice.getAmount());
					if (retainPrice.getTariff() != null
							&& retainPrice.getTariff().getValue() != null) {
						salePrice.getTariff().setValue(salePrice.getTariff().getValue().subtract(retainPrice.getTariff().getValue()));
						
						// по договору сборы насчитываются не на тариф, а на полную стоимость. по этому за тариф принимаем стоимость
						salePrice.getTariff().setValue(returnPrice.getAmount());
					}
					for (Commission saleComm : salePrice.getCommissions()) {
						for (Commission retainComm : salePrice.getCommissions()) {
							if (Objects.equals(saleComm.getCode(), retainComm.getCode())) {
								saleComm.setValue(saleComm.getValue().subtract(retainComm.getValue()));
								break;
							}
						}
					}
					returnPrice = salePrice;
				}
				addReturnPrice(serviceItem, returnPrice, returnStatement);
				serviceItem.setConfirmed(true);
			} catch (Exception e) {
				serviceItem.setConfirmed(false);
				serviceItem.setError(new RestError(e.getMessage()));
			}
			response.getServices().add(serviceItem);
		}
		return response;
	}
	
	private void addReturnPrice(ServiceItem serviceItem, Price returnPrice, ReturnStatementType returnStatement) {
		
		// устанавливаем условие возврата
		if (returnStatement.getRulesReturn() != null) {
			
			// добавляем тариф с условием возврата
			if (returnPrice.getTariff() == null) {
				Tariff tariff = new Tariff();
				
				// по договору сборы насчитываются не на тариф, а на полную стоимость. по этому за тариф принимаем стоимость
				tariff.setValue(returnPrice.getAmount());
				returnPrice.setTariff(tariff);
			}
			ReturnCondition condition = new ReturnCondition();
			condition.setDescription(returnStatement.getRulesReturn().getRule()
					.stream().map(rule -> rule.getValue()).collect(Collectors.joining("\n")));
			returnPrice.getTariff().setReturnConditions(Collections.singletonList(condition));
		}
		serviceItem.setPrice(returnPrice);
	}

	@Override
	public OrderResponse getPdfDocumentsResponse(OrderRequest request) {
		throw TCPClient.createUnavailableMethod();
	}
	
	@GetMapping("/api/balance")
	public String getBalance() {
		try {
			return client.getBalance();
		} catch (RequestException e) {
			return null;
		}
	}

}
