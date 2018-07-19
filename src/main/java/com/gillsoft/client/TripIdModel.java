package com.gillsoft.client;

import java.util.Date;

import com.gillsoft.model.AbstractJsonModel;

public class TripIdModel extends AbstractJsonModel {

	private static final long serialVersionUID = 6685617842271023619L;

	private String serverCode;
	private String serverName;
	private String serverAddress;
	private String tib;
	private String id;
	private String fromId;
	private String toId;
	private Date date;
	
	public TripIdModel() {
		
	}

	public TripIdModel(String serverCode, String serverName, String serverAddress, String tib, String id, String fromId,
			String toId, Date date) {
		this.serverCode = serverCode;
		this.serverName = serverName;
		this.serverAddress = serverAddress;
		this.tib = tib;
		this.id = id;
		this.fromId = fromId;
		this.toId = toId;
		this.date = date;
	}

	public String getServerCode() {
		return serverCode;
	}

	public void setServerCode(String serverCode) {
		this.serverCode = serverCode;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public String getTib() {
		return tib;
	}

	public void setTib(String tib) {
		this.tib = tib;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFromId() {
		return fromId;
	}

	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	public String getToId() {
		return toId;
	}

	public void setToId(String toId) {
		this.toId = toId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public TripIdModel create(String arg0) {
		return (TripIdModel) super.create(arg0);
	}
}
