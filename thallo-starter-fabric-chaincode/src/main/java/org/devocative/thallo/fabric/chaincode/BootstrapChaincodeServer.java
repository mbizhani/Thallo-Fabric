package org.devocative.thallo.fabric.chaincode;

import org.devocative.thallo.fabric.chaincode.config.FabricChaincodeProperties;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeServer;
import org.hyperledger.fabric.shim.ChaincodeServerProperties;
import org.hyperledger.fabric.shim.NettyChaincodeServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Component
public class BootstrapChaincodeServer {
	private static final Logger log = LoggerFactory.getLogger(BootstrapChaincodeServer.class);

	private ChaincodeServer chaincodeServer;

	private final ChaincodeBase chaincodeBase;
	private final FabricChaincodeProperties properties;

	// ------------------------------

	public BootstrapChaincodeServer(ChaincodeBase chaincodeBase, FabricChaincodeProperties properties) {
		this.chaincodeBase = chaincodeBase;
		this.properties = properties;
	}

	// ------------------------------

	@Async
	@EventListener(ApplicationStartedEvent.class)
	public void init() {
		log.info("--- BootstrapChaincodeServer ---");

		final String serverAddress = properties.getServerAddress();

		if (serverAddress == null || serverAddress.isEmpty()) {
			throw new RuntimeException("Chaincode Address Not Found");
		}
		log.info("--- BootstrapChaincodeServer - ServerAddress={}", serverAddress);

		final String[] parts = serverAddress.split(":");
		final ChaincodeServerProperties chaincodeServerProperties = new ChaincodeServerProperties();
		chaincodeServerProperties.setServerAddress(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));

		final FabricChaincodeProperties.TLSConfig tls = properties.getTls();
		if (tls != null && tls.isEnabled()) {
			chaincodeServerProperties.setTlsEnabled(true);
			chaincodeServerProperties.setKeyCertChainFile(tls.getCertFile());
			chaincodeServerProperties.setKeyFile(tls.getKeyFile());
			chaincodeServerProperties.setKeyPassword(tls.getKeyFilePassword());
		}

		try {
			chaincodeServer = new NettyChaincodeServer(chaincodeBase, chaincodeServerProperties);
			chaincodeServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@PreDestroy
	public void shutdown() {
		if (chaincodeServer != null) {
			log.info("--- BootstrapChaincodeServer - Shutting Down Chaincode Server");
			chaincodeServer.stop();
		}
	}
}
