package org.devocative.thallo.fabric.chaincode.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "borna.hlf.cc")
public class FabricChaincodeProperties {
	private String chaincodeId;
	private String serverAddress;
	private DataTypeConfig dataType = new DataTypeConfig();
	private SerializerConfig serializer = new SerializerConfig();

	@Getter
	@Setter
	public static class DataTypeConfig {
		private List<String> scanPackages = new ArrayList<>();
	}

	@Getter
	@Setter
	public static class SerializerConfig {
		private String fabricBasePackage = "org.hyperledger.fabric.contract";
		private List<String> scanPackages = new ArrayList<>();
	}
}
