package org.devocative.thallo.fabric.samples.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devocative.thallo.fabric.samples.gateway.iservice.ISampleContract;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Service
public class SampleService {
	private final ISampleContract contract;

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
