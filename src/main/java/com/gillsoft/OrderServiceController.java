package com.gillsoft;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.gillsoft.abstract_rest_service.AbstractOrderService;
import com.gillsoft.client.MoneyType;
import com.gillsoft.client.OrderIdModel;
import com.gillsoft.client.PricePart;
import com.gillsoft.client.ServiceIdmodel;
import com.gillsoft.client.TicketResponse;
import com.gillsoft.client.TripIdModel;
import com.gillsoft.model.CalcType;
import com.gillsoft.model.Commission;
import com.gillsoft.model.Currency;
import com.gillsoft.model.Locality;
import com.gillsoft.model.Organisation;
import com.gillsoft.model.Price;
import com.gillsoft.model.RestError;
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
				
				for (ServiceItem serviceItem : order.getValue()) {
					ServiceItem item = new ServiceItem();
					
					String uid = client.getRandomId();
					ServiceIdmodel idmodel = new ServiceIdmodel(uid, tripIdModel);
					orderId.getIds().add(idmodel);
					item.setId(idmodel.asString());
					item.setNumber(client.createUid(uid));
					
					// рейс
					item.setSegment(addSegment(tripIdModel, localities, organisations, segments, ticket.getTicket().get(0)));
					
					// пассажир
					item.setCustomer(serviceItem.getCustomer());
					
					// место
					item.setSeat(createSeat(serviceItem.getSeat(), ticket));
					
					// стоимость
					item.setPrice(createPrice(ticket.getTicket().get(0).getMoney()));
					
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
		response.setCustomers(request.getCustomers());
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
			
			segment.setDeparture(controller.addLocality(localities, ticket.getPointFrom()));
			segment.setArrival(controller.addLocality(localities, ticket.getPointTo()));
			
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
	private Price createPrice(MoneyType money) {
		BigDecimal amount = null;
		Tariff tariff = null;
		List<Commission> commissions = new ArrayList<>();
		for (PricePart part : money.getPayment()) {
			
			// если сумма
			if (part.getCode().equals(PAYMENT_CODE)) {
				amount = new BigDecimal(part.getAmount());
				
			// если тариф
			} else if (part.getCode().equals(TARIFF_CODE)) {
				tariff = new Tariff();
				tariff.setValue(new BigDecimal(part.getAmount()));
				tariff.setCode(part.getCode());
				tariff.setName(part.getName());
				// TODO maybe add return conditions
			} else if (!part.getCode().contains("nВ")
					&& !part.getCode().endsWith("н")) {
				commissions.add(createCommission(part));
			}
		}
		for (PricePart part : money.getAddTax()) {
			Commission commission = createCommission(part);
			commissions.add(commission);
			amount = amount.add(commission.getValue());
		}
		Price price = new Price();
		price.setAmount(amount);
		price.setCurrency(Currency.UAH);
		price.setTariff(tariff);
		price.setCommissions(commissions);
		return price;
	}
	
	private Commission createCommission(PricePart part) {
		Commission commission = new Commission();
		commission.setCode(part.getCode());
		commission.setName(part.getName());
		commission.setType(ValueType.FIXED);
		commission.setValueCalcType(CalcType.OUT);
		commission.setValue(new BigDecimal(part.getAmount()));
		return commission;
	}
	
	private Seat createSeat(Seat seat, TicketResponse response) {
		if (response.getSeats() != null) {
			for (Byte number : response.getSeats().getSeat()) {
				if (number == Byte.valueOf(seat.getId())) {
					seat.setNumber(seat.getId());
					return seat;
				}
			}
			// если указанного места нет, то первое свободное
			Byte number = response.getSeats().getSeat().remove(0);
			Seat newSeat = new Seat();
			newSeat.setId(String.valueOf(number));
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse getServiceResponse(String serviceId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse bookingResponse(String orderId) {
		throw TCPClient.createUnavailableMethod();
	}

	@Override
	public OrderResponse confirmResponse(String orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse cancelResponse(String orderId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse prepareReturnServicesResponse(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse returnServicesResponse(OrderRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderResponse getPdfDocumentsResponse(OrderRequest request) {
		throw TCPClient.createUnavailableMethod();
	}

}
