package org.devocative.thallo.fabric.samples.gateway;

import org.devocative.thallo.fabric.gateway.EnableFabricGateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableFabricGateway
@SpringBootApplication
public class SampleGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(SampleGatewayApplication.class, args);
	}

}
