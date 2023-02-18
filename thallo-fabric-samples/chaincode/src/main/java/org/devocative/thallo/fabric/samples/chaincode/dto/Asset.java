package org.devocative.thallo.fabric.samples.chaincode.dto;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
public class Asset {
	private String id;
	private String name;
	private BigDecimal price;
	private Instant createdDateTime;
	private Instant updatedDateTime;
	private String createdOrgId;
	private String updatedOrgId;

	// ------------------------------

	public Asset() {
	}

	public Asset(String id, String name, BigDecimal price, Instant createdDateTime, String createdOrgId) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.createdDateTime = createdDateTime;
		this.createdOrgId = createdOrgId;
	}

	// ------------------------------

	@Override
	public String toString() {
		return "Asset{" +
			"id='" + id + '\'' +
			", name='" + name + '\'' +
			", price=" + price +
			'}';
	}
}
