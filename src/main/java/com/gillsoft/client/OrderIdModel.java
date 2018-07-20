package com.gillsoft.client;

import java.util.ArrayList;
import java.util.List;

import com.gillsoft.model.AbstractJsonModel;

public class OrderIdModel extends AbstractJsonModel {

	private static final long serialVersionUID = 4318484251645220464L;

	private List<ServiceIdmodel> ids = new ArrayList<>();

	public OrderIdModel() {

	}

	public List<ServiceIdmodel> getIds() {
		return ids;
	}

	public void setIds(List<ServiceIdmodel> ids) {
		this.ids = ids;
	}

	@Override
	public OrderIdModel create(String json) {
		return (OrderIdModel) super.create(json);
	}

}
