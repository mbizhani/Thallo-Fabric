package org.devocative.thallo.fabric.gateway.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "thallo.fabric.gateway")
public class FabricGatewayProperties {
	private String channel;
	private String chaincode;
	private String connectionProfileFile;
	private String identityWalletDir;
	private String orgMspId;
	private CAServerProperties caServer;

	@Getter
	@Setter
	public static class CAServerProperties {
		private String pemFile;
		private String url;
		private String username;
		private String password;
	}
}
