package org.devocative.thallo.fabric.samples.gateway.service;

import lombok.RequiredArgsConstructor;
import org.devocative.thallo.fabric.samples.gateway.iservice.ISampleContract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class SampleService {
	private static final Logger log = LoggerFactory.getLogger(SampleService.class);

	private final ISampleContract contract;

	// ------------------------------

	@PostConstruct
	public void init() {
		log.info("GetTime = {}", contract.getTime());

		contract.setTime(new Date(), Arrays.asList("A", "1", "null"));

		final var asset = contract.createAsset("سکه", new BigDecimal(1000));
		log.info("CreateAsset: {}", asset);

		contract.updateAsset(asset.getId(), "نیم سکه", new BigDecimal(1500));

		log.info("Get Asset: {}", new String(contract.getAsset(asset.getId())));

		log.info("GetAllAssets: {}", contract.getAllAssets());
	}
}
