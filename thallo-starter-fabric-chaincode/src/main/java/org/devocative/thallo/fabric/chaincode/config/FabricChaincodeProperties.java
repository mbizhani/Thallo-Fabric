package org.devocative.thallo.fabric.chaincode.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "thallo.fabric.chaincode")
public class FabricChaincodeProperties {
	private String id;
	private String serverAddress;
	private DataTypeConfig dataType = new DataTypeConfig();
	private String defaultSerializer = "org.devocative.thallo.fabric.chaincode.shim.TJSONSerializer";
	private TLSProperties tls = new TLSProperties();
	private DevModeProperties devMode = new DevModeProperties();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public DataTypeConfig getDataType() {
		return dataType;
	}

	public void setDataType(DataTypeConfig dataType) {
		this.dataType = dataType;
	}

	public String getDefaultSerializer() {
		return defaultSerializer;
	}

	public void setDefaultSerializer(String defaultSerializer) {
		this.defaultSerializer = defaultSerializer;
	}

	public TLSProperties getTls() {
		return tls;
	}

	public void setTls(TLSProperties tls) {
		this.tls = tls;
	}

	public DevModeProperties getDevMode() {
		return devMode;
	}

	public void setDevMode(DevModeProperties devMode) {
		this.devMode = devMode;
	}

	// ------------------------------

	public static class DataTypeConfig {
		private List<String> scanPackages = new ArrayList<>();

		public List<String> getScanPackages() {
			return scanPackages;
		}

		public void setScanPackages(List<String> scanPackages) {
			this.scanPackages = scanPackages;
		}
	}

	public static class TLSProperties {
		private boolean enabled;
		private String certFile;
		private String keyFile;
		private String keyFilePassword;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getCertFile() {
			return certFile;
		}

		public void setCertFile(String certFile) {
			this.certFile = certFile;
		}

		public String getKeyFile() {
			return keyFile;
		}

		public void setKeyFile(String keyFile) {
			this.keyFile = keyFile;
		}

		public String getKeyFilePassword() {
			return keyFilePassword;
		}

		public void setKeyFilePassword(String keyFilePassword) {
			this.keyFilePassword = keyFilePassword;
		}
	}

	public static class DevModeProperties {
		private boolean enabled = false;
		private String peerAddress = "127.0.0.1:7052";

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getPeerAddress() {
			return peerAddress;
		}

		public void setPeerAddress(String peerAddress) {
			this.peerAddress = peerAddress;
		}
	}
}
