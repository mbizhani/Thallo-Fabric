package org.devocative.thallo.fabric.samples.gateway.service;

import org.devocative.thallo.fabric.samples.gateway.iservice.ISampleContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

@Service
public class SampleService {
	private static final Logger log = LoggerFactory.getLogger(SampleService.class);

	private final ISampleContract contract;

	// ------------------------------

	public SampleService(ISampleContract contract) {
		this.contract = contract;
	}

	// ------------------------------

	@PostConstruct
	public void init() {
		log.info("ISampleContract.getTime = {}", contract.getTime());

		contract.setTime(new Date(), Arrays.asList("A", "1", "null"));

		final var asset = contract.createAsset();
		log.info("ISampleContract.createAsset: {}", asset);

		asset.setPrice(new BigDecimal(1000));
		contract.updateAsset(asset);

		log.info("ISampleContract.list: {}", contract.list());
	}
}
