package com.gillsoft;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.datetime.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.gillsoft.cache.CacheHandler;
import com.gillsoft.cache.IOCacheException;
import com.gillsoft.cache.RedisMemoryCache;
import com.gillsoft.client.Answer;
import com.gillsoft.client.ArrivalUpdateTask;
import com.gillsoft.client.Ask;
import com.gillsoft.client.BaseResponse;
import com.gillsoft.client.BuyRequestType;
import com.gillsoft.client.BuyRequestType.Ticket;
import com.gillsoft.client.Config;
import com.gillsoft.client.DispatchUpdateTask;
import com.gillsoft.client.IdentType;
import com.gillsoft.client.Point;
import com.gillsoft.client.RequestException;
import com.gillsoft.client.RequestType;
import com.gillsoft.client.TechInfoType;
import com.gillsoft.client.TripsResponse.Trip;
import com.gillsoft.util.StringUtil;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TCPClient {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public static final String STATIONS_CACHE_KEY = "buscomua.stations.";
	public static final String TRIPS_CACHE_KEY = "buscomua.trips.";
	
//	private final static String BALANCE_KEY = "BUS_COM_UA_BALANCE";
//	private final static String BALANCE_KEY_COUNTER = "BALANCE_KEY_COUNTER";
//	private final static String DEPOSIT_BALANCE_KEY = Utils.getStorageAccessorDepositBalanceKey(StorageAccessors.BUSCOMUA.getCode());
	private static final String TICKET_TYPE = "ОБЩ";
	private static final String DATE_FORMAT = "dd.MM.yy";
	private static final String CHARSET = "UTF-8";
	
	public static final FastDateFormat dateFormat = FastDateFormat.getInstance(DATE_FORMAT);
	public static final FastDateFormat fullDateFormat = FastDateFormat.getInstance("E MMM dd HH:mm:ss yyyy", Locale.ENGLISH);
	
	private BlockingQueue<Object> connections = new ArrayBlockingQueue<>(Config.getPoolSize());
	
	@Autowired
    @Qualifier("RedisMemoryCache")
	private CacheHandler cache;
	
	public TCPClient() {
		for (int i = 0; i < Config.getPoolSize(); i++) {
			addFreeObject();
		}
	}
	
	public boolean isAvailable() {
		Socket socket = null;
		try {
			// создаем соединение
			socket = createSocket();
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	private Socket createSocket() throws IOException {
		// создаем соединение
		Socket socket = new Socket(Proxy.NO_PROXY);

		// открываем соединение
		socket.connect(new InetSocketAddress(Config.getUrl(), Config.getPort()), Config.getSoTimeout());
		return socket;
	}
	
	private void addIdent(Ask request) {
		IdentType ident = new IdentType();
		ident.setFrom(Config.getFrom());
		ident.setTimestamp(fullDateFormat.format(new Date()));
		ident.setTo(Config.getTo());
		request.setIdent(ident);
	}
	
	private void addTechInfo(RequestType request) {
		request.setTechInfo(createTechInfo());
	}
	
	private void addTechInfo(BuyRequestType request) {
		request.setTechInfo(createTechInfo());
	}
	
	private TechInfoType createTechInfo() {
		TechInfoType.Client client = new TechInfoType.Client();
		client.setAgent(Config.getAgent());
		client.setWorkplace(Config.getWorkplace());
		client.setUid(getTransactionId());
		TechInfoType techInfo = new TechInfoType();
		techInfo.setClient(client);
		return techInfo;
	}

	private String getTransactionId() {
		UUID uuid = UUID.randomUUID();
		long lId = uuid.getLeastSignificantBits();
		long mId = uuid.getMostSignificantBits();
		StringBuilder sb = new StringBuilder(Config.getPrefix());
		addLong(sb, lId);
		addLong(sb, mId);
		return sb.toString();
	}
	
	private void addLong(StringBuilder sb, long id) {
		if (id < 0) {
			sb.append(0).append(Math.abs(id));
		} else {
			sb.append(id);
		}
	}
	
	private Object getFreeObject() throws InterruptedException {
		return connections.poll(Config.getSoTimeout(), TimeUnit.MILLISECONDS);
	}
	
	private Object addFreeObject() {
		return connections.add(new Object());
	}
	
	private Answer sendRequest(Ask ask, String requestName) throws RequestException {
		Socket socket = null;
		String request;
		try {
			// проверка на одновременное использование не больше указанного
			// количества соединений
			getFreeObject();
			
			// создаем соединение
			socket = createSocket();
			socket.setSoTimeout(6 * Config.getSoTimeout());
			
			// логируем запрос
			request = marshallToString(ask);
			
			String requestId = StringUtil.generateUUID();
			logInfo(requestId, request);
			
			// записываем и отправляем запрос
			OutputStream out = socket.getOutputStream();
			IOUtils.copy(new ByteArrayInputStream(request.getBytes(CHARSET)), out);
			out.flush();
			socket.shutdownOutput();
			
			// читаем ответ
			InputStream in = socket.getInputStream();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(in, baos);
			socket.shutdownInput();
			
			logInfo(requestId, new String(baos.toByteArray(), CHARSET));
			
			// из-за того, что ресурс присылает не совсем верную кодировку "utf8"
			// приходится читать с принудительным указанием кодировки
			Reader reader = new InputStreamReader(new ByteArrayInputStream(baos.toByteArray()), CHARSET);
			return unmarshallToObjectFromReader(reader, Answer.class);
		} catch (InterruptedException e) {
			throw new RequestException("Не удалось получить свободное соединение. В течении "
					+ (6 * Config.getSoTimeout() / 1000) + " секунд.");
		} catch (IOException e) {
			throw new RequestException("Не удалось отправить/получить запрос/ответ ресурса.");
		} catch (JAXBException e) {
			throw new RequestException("Не удалось преобразовать запрос/ответ ресурса.");
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
				addFreeObject();
			}
//			incRequest(requestName);
		}
	}
	
	private void logInfo(String id, String info) {
		LOGGER.info(new StringBuilder().append("\n")
			.append("Exchange id : ").append(id).append("\n")
			.append("Body        : ").append(info).append("\n"));
	}
	
	public String marshallToString(Object object) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal(object, baos);
    	return new String(baos.toByteArray());
    }
	
	@SuppressWarnings("unchecked")
	public <T> T unmarshallToObjectFromReader(Reader reader,
			Class<?> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (T) unmarshaller.unmarshal(reader);
    }
	
//	private void incRequest(String requestName) {
//		CallableStatement procedure = null;
//		Connection connection = null;
//		try {
//			Object[] args = { requestName };
//			List<Object> result = Utils.callProcedure("API_BUSCOMUA.INC_COUNTER", args, new int[] { });
//			procedure = (CallableStatement) result.get(0);
//			connection = (Connection) result.get(1);
//		} catch (SQLException e) {
//			Utils.logInfo(e);
//		} finally {
//			OracleConnectionManager.getInstance().freeConnection(connection);
//			try {
//				if (procedure != null) {
//					procedure.close();
//				}
//			} catch (SQLException ex) {
//				Utils.logInfo(ex.getMessage());
//			}
//		}
//	}
	
	private Answer checkAnswer(Answer answer, BaseResponse response) throws RequestException {
		if (answer.getError() != null) {
			throw new RequestException(answer.getError().getName().concat(": ").concat(answer.getError().getText()));
		}
		if (response != null && response.getError() != null) {
			throw new RequestException(response.getError().getName().concat(": ").concat(response.getError().getText()));
		}
		return answer;
	}
	
	public List<Point> getDispatchStations() throws RequestException {
		Ask request = new Ask();
		RequestType requestType = new RequestType();
		request.setGetListFromPointsKOATUU(requestType);
		addIdent(request);
		addTechInfo(request.getGetListFromPointsKOATUU());
		Answer answer = sendRequest(request, "getListFromPointsKOATUU");
		return checkAnswer(answer, answer.getGetListFromPointsKOATUU())
				.getGetListFromPointsKOATUU().getFromPointKOATUU();
	}
	
	public List<Point> getCachedDispatchStations() throws IOCacheException {
		return getCachedStations("", new DispatchUpdateTask(""));
	}
	
	public List<Point> getArrivalStations(String dispatchId) throws RequestException {
		Ask request = new Ask();
		RequestType requestType = new RequestType();
		requestType.setFromPointKOATUU(dispatchId);
		requestType.setRegularity(String.valueOf(Config.getRegularity()));
		requestType.setPrice(String.valueOf(Config.getMinPrice()));
		requestType.setRequiredInfo("emailphone");
		request.setGetListToPointsKOATUU(requestType);
		addIdent(request);
		addTechInfo(request.getGetListToPointsKOATUU());
		Answer answer = sendRequest(request, "getListToPointsKOATUU");
		return checkAnswer(answer, answer.getGetListToPointsKOATUU())
				.getGetListToPointsKOATUU().getToPointKOATUU();
	}
	
	public List<Point> getCachedArrivalStations(String dispatchId) throws IOCacheException {
		return getCachedStations(dispatchId, new ArrivalUpdateTask(dispatchId));
	}
	
	@SuppressWarnings("unchecked")
	private List<Point> getCachedStations(String dispatchId, Runnable task) throws IOCacheException {
		Map<String, Object> params = new HashMap<>();
		params.put(RedisMemoryCache.OBJECT_NAME, getStationCacheKey(dispatchId));
		params.put(RedisMemoryCache.UPDATE_TASK, task);
		return (List<Point>) cache.read(params);
	}
	
	public List<Trip> getTrips(String dispatchId, String arriveId, Date dispatchDate) throws RequestException {
		Ask request = new Ask();
		RequestType requestType = new RequestType();
		requestType.setFromPointKOATUU(dispatchId);
		requestType.setToPointKOATUU(arriveId);
		requestType.setDate(dateFormat.format(dispatchDate));
		requestType.setDaysForward("0");
		requestType.setRegularity(String.valueOf(Config.getRegularity()));
		requestType.setPrice(String.valueOf(Config.getMinPrice()));
		requestType.setRequiredInfo("emailphone");
		request.setGetListTripsWithFreePlacesKOATUU(requestType);
		addIdent(request);
		addTechInfo(request.getGetListTripsWithFreePlacesKOATUU());
		Answer answer = sendRequest(request, "getListTripsWithFreePlacesKOATUU");
		return checkAnswer(answer, answer.getGetListTripsWithFreePlacesKOATUU())
				.getGetListTripsWithFreePlacesKOATUU().getTrip();
	}
	
	public Answer getTripInfo(String serverId, String tripId, String dispatchId, String arriveId, Date dispatchDate,
			int tickets) throws RequestException {
		Ask request = new Ask();
		BuyRequestType requestType = new BuyRequestType();
		requestType.setFromPoint(dispatchId);
		requestType.setToPoint(arriveId);
		requestType.setDate(dateFormat.format(dispatchDate));
		requestType.setKod(serverId);
		requestType.setRoundNum(tripId);
		request.setInfoTicket(requestType);
		Ticket ticket = new Ticket();
		ticket.setType(TICKET_TYPE);
		for (int i = 0; i < tickets; i++) {
			requestType.getTicket().add(ticket);
		}
		addIdent(request);
		addTechInfo(request.getInfoTicket());
		return sendRequest(request, "infoTicket");
	}
	
//	public Answer buy(String serverId, String tripId, String dispatchId,
//			String arriveId, Date dispatchDate, List<Passenger> passengers)
//			throws RequestException {
//		Ask request = new Ask();
//		BuyRequestType requestType = new BuyRequestType();
//		requestType.setFromPoint(dispatchId);
//		requestType.setToPoint(arriveId);
//		requestType.setDate(Utils.formatDate(dispatchDate, DATE_FORMAT));
//		requestType.setKod(serverId);
//		requestType.setRoundNum(tripId);
//		request.setBuyTicket(requestType);
//		for (Passenger passenger : passengers) {
//			Ticket ticket = new Ticket();
//			ticket.setUid(createUid(passenger.getServiceId()));
//			String name = Utils.getFullyName(
//					passenger.getLastName(), passenger.getFirstName(), passenger.getSecondName());
//			name = name.replaceAll("[^ \\-'\\.a-zA-ZА-Яа-яіІїЇєЄ]", "");
//			if (name.length() > 32) {
//				name = name.substring(0, 32);
//			}
//			ticket.setPassengerInfo(name);
//			ticket.setEmail(passenger.getMail());
//			ticket.setPhone(Utils.getNumberFromStringAsString(passenger.getPhone()));
//			requestType.getTicket().add(ticket);
//		}
//		addIdent(request);
//		addTechInfo(request.getBuyTicket());
//		return sendRequest(request, "buyTicket");
//	}
	
	public static String createUid(String serviceId) {
		String s = Config.getPrefix() + String.format("%9s", serviceId).replaceAll(" ", "0");
		int summ = 0;
		for (int i = 0; i < s.length(); i++) {
			summ += Integer.valueOf(String.valueOf(s.charAt(i)));
		}
		String summStr = String.valueOf(summ);
		return s + summStr.charAt(summStr.length() - 1);
	}
	
	public Answer cancel(String serverId, String tripId, String dispatchId, String arriveId, Date dispatchDate,
			String serviceId, String asuid, String mode) throws RequestException {
		Ask request = new Ask();
		BuyRequestType requestType = new BuyRequestType();
		requestType.setFromPoint(dispatchId);
		requestType.setToPoint(arriveId);
		requestType.setDate(dateFormat.format(dispatchDate));
		requestType.setKod(serverId);
		requestType.setRoundNum(tripId);
		requestType.setMode(mode);
		request.setCancelTicket(requestType);
		Ticket ticket = new Ticket();
		ticket.setUid(createUid(serviceId));
		ticket.setAsUID(asuid);
		requestType.getTicket().add(ticket);
		addIdent(request);
		addTechInfo(request.getCancelTicket());
		return sendRequest(request, "cancelTicket");
	}
	
//	public Answer getStatus(Passenger passenger) throws RequestException{
//		Ask request = new Ask();
//		BuyRequestType requestType = new BuyRequestType();
//		
//		Map<String, Object> map = passenger.getDatasourceMap();
//		requestType.setKod(map.get("buscomuaServerCode").toString());
//		requestType.setRoundNum(map.get("TRIPNUMBER").toString());
//		requestType.setDate(Utils.formatDate((Date) map.get("TRIPPASSENGERDATE"), DATE_FORMAT));
//		requestType.setFromPoint(map.get("buscomuaPointfromCode").toString());
//		requestType.setToPoint(map.get("buscomuaPointtoCode").toString());
//		
//		request.setStatusTicket(requestType);
//		Ticket ticket = new Ticket();
//		
//		ticket.setUid(createUid(passenger.getServiceId()));
//		
//		requestType.getTicket().add(ticket);
//		addIdent(request);
//		addTechInfo(request.getStatusTicket());
//		return sendRequest(request, "statusTicket");
//	}
	
//	public void addBalance(BigDecimal added, int ticketCount) {
//		synchronized (BusTCPClient.class) {
//			BigDecimal balance = Utils.returnBigDecimal(Utils.getParamText(BALANCE_KEY));
//			Utils.setParamText(BALANCE_KEY, balance.add(added).toString());
//			Utils.setParamText(DEPOSIT_BALANCE_KEY, balance.add(added).toString());
//			
//			if (ticketCount != 0) {
//				BigDecimal balance_counter = Utils.returnBigDecimal(Utils.getParamText(BALANCE_KEY_COUNTER));
//			
//				if (balance_counter.compareTo(new BigDecimal(100)) >= 0) {
//					updateBalance();
//				} else {
//					Utils.setParamText(BALANCE_KEY_COUNTER, balance_counter.add(new BigDecimal(ticketCount)).toString());
//				}
//			}
//		}
//	}
	
//	public void updateBalance() {
//		Ask request = new Ask();
//		RequestType requestType = new RequestType();
//		request.setGetBalance(requestType);
//		addIdent(request);
//		addTechInfo(request.getGetBalance());
//		try {
//			Answer answer = sendRequest(request, "getBalance");
//			if (answer.getGetBalance().getBalance() != null) {
//				Utils.setParamText(BALANCE_KEY, answer.getGetBalance().getBalance().getCurrent().toString());
//				Utils.setParamText(DEPOSIT_BALANCE_KEY, answer.getGetBalance().getBalance().getCurrent().toString());
//				Utils.setParamText(BALANCE_KEY_COUNTER, "0");
//			}
//		} catch (RequestException e) {
//			Utils.logInfo(e);
//		}
//	}
	
	public static String getStationCacheKey(String id) {
		return STATIONS_CACHE_KEY + id;
	}

	public CacheHandler getCache() {
		return cache;
	}
	
}
