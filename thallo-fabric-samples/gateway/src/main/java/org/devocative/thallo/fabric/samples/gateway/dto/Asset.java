package org.devocative.thallo.fabric.samples.gateway.dto;


import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Asset {
	private String id;
	private String name;
	private BigDecimal price;
}