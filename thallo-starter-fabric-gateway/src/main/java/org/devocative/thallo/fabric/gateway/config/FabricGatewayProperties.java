package org.devocative.thallo.fabric.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "thallo.fabric.gateway")
public class FabricGatewayProperties {
	private String channel;
	private String chaincode;
	private String connectionProfileFile;
	private String orgMspId;
	private CAProperties ca;
	private IdentityProperties identity;

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getChaincode() {
		return chaincode;
	}

	public void setChaincode(String chaincode) {
		this.chaincode = chaincode;
	}

	public String getConnectionProfileFile() {
		return connectionProfileFile;
	}

	public void setConnectionProfileFile(String connectionProfileFile) {
		this.connectionProfileFile = connectionProfileFile;
	}

	public String getOrgMspId() {
		return orgMspId;
	}

	public void setOrgMspId(String orgMspId) {
		this.orgMspId = orgMspId;
	}

	public CAProperties getCa() {
		return ca;
	}

	public void setCa(CAProperties ca) {
		this.ca = ca;
	}

	public IdentityProperties getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityProperties identity) {
		this.identity = identity;
	}

	// ------------------------------

	public static class CAProperties {
		private String walletDir;
		private CAServerProperties server = new CAServerProperties();

		public String getWalletDir() {
			return walletDir;
		}

		public void setWalletDir(String walletDir) {
			this.walletDir = walletDir;
		}

		public CAServerProperties getServer() {
			return server;
		}

		public void setServer(CAServerProperties server) {
			this.server = server;
		}
	}

	public static class CAServerProperties {
		private String pemFile;
		private String url;
		private String username;
		private String password;

		public String getPemFile() {
			return pemFile;
		}

		public void setPemFile(String pemFile) {
			this.pemFile = pemFile;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}

	public static class IdentityProperties {
		private String privateKeyPemFile;
		private String privateKeyPem;
		private String certificatePemFile;
		private String certificatePem;

		public String getPrivateKeyPemFile() {
			return privateKeyPemFile;
		}

		public void setPrivateKeyPemFile(String privateKeyPemFile) {
			this.privateKeyPemFile = privateKeyPemFile;
		}

		public String getPrivateKeyPem() {
			return privateKeyPem;
		}

		public void setPrivateKeyPem(String privateKeyPem) {
			this.privateKeyPem = privateKeyPem;
		}

		public String getCertificatePemFile() {
			return certificatePemFile;
		}

		public void setCertificatePemFile(String certificatePemFile) {
			this.certificatePemFile = certificatePemFile;
		}

		public String getCertificatePem() {
			return certificatePem;
		}

		public void setCertificatePem(String certificatePem) {
			this.certificatePem = certificatePem;
		}

		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();

			if (certificatePemFile != null) {
				builder.append("certificatePemFile=[").append(certificatePemFile).append("] ");
			} else if (certificatePem != null) {
				builder.append("certificatePem=[***] ");
			}

			if (privateKeyPemFile != null) {
				builder.append("privateKeyPemFile=[").append(privateKeyPemFile).append("]");
			} else if (privateKeyPem != null) {
				builder.append("privateKeyPem=[***]");
			}

			return builder.toString();
		}
	}
}
