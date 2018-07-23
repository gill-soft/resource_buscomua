package com.gillsoft.client;

import java.util.ArrayList;
import java.util.List;

import com.gillsoft.model.AbstractJsonModel;

public class OrderIdModel extends AbstractJsonModel {

	private static final long serialVersionUID = 4318484251645220464L;

	private List<OrderPart> parts = new ArrayList<>();

	public OrderIdModel() {

	}

	public List<OrderPart> getParts() {
		return parts;
	}

	public void setParts(List<OrderPart> parts) {
		this.parts = parts;
	}

	@Override
	public OrderIdModel create(String json) {
		return (OrderIdModel) super.create(json);
	}

}
