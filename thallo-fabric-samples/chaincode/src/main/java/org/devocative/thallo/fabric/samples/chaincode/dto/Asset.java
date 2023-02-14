package org.devocative.thallo.fabric.samples.chaincode.dto;


import java.math.BigDecimal;

public class Asset {
	private String id;
	private String name;
	private BigDecimal price;

	// ------------------------------

	public Asset() {
	}

	public Asset(String id, String name, BigDecimal price) {
		this.id = id;
		this.name = name;
		this.price = price;
	}

	// ------------------------------

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Asset{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", price=" + price +
			'}';
	}
}
