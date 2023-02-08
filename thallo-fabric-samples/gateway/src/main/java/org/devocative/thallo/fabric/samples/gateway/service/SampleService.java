package org.devocative.thallo.fabric.samples.gateway.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devocative.thallo.fabric.gateway.iservice.IFabricGatewayService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@RequiredArgsConstructor
@Service
public class SampleService {
	private final IFabricGatewayService gatewayService;

	@PostConstruct
	public void init() {
		try {
			final byte[] result = gatewayService.submit("init");
			log.info("init method = {}", new String(result));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
