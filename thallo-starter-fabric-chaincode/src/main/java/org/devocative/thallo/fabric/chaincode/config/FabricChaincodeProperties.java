package org.devocative.thallo.fabric.chaincode.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "thallo.fabric.chaincode")
public class FabricChaincodeProperties {
	private String id;
	private String serverAddress;
	private DataTypeConfig dataType = new DataTypeConfig();
	private SerializerConfig serializer = new SerializerConfig();
	private TLSConfig tls = new TLSConfig();
	private DevMode devMode = new DevMode();

	@Getter
	@Setter
	public static class DataTypeConfig {
		private List<String> scanPackages = new ArrayList<>();
	}

	@Getter
	@Setter
	public static class SerializerConfig {
		private List<String> initBasePackages = Arrays.asList(
			"org.hyperledger.fabric.contract",
			"org.devocative.thallo.fabric.chaincode.shim");
		private List<String> scanPackages = new ArrayList<>();
	}

	@Getter
	@Setter
	public static class TLSConfig {
		private boolean enabled;
		private String certFile;
		private String keyFile;
		private String keyFilePassword;
	}

	@Getter
	@Setter
	public static class DevMode {
		private boolean enabled = false;
		private String peerAddress = "127.0.0.1:7052";
	}
}
