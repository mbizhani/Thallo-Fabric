package org.devocative.thallo.fabric.samples.gateway.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
public class Asset {
	private String id;
	private String name;
	private BigDecimal price;
	private Instant createdDateTime;
	private Instant updatedDateTime;
	private String createdOrgId;
	private String updatedOrgId;
}
