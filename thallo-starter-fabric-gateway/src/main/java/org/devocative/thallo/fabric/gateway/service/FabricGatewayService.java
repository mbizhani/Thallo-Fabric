package org.devocative.thallo.fabric.gateway.service;

import org.devocative.thallo.fabric.gateway.FabricGatewayProperties;
import org.devocative.thallo.fabric.gateway.iservice.IFabricCAService;
import org.devocative.thallo.fabric.gateway.iservice.IFabricGatewayService;
import org.devocative.thallo.fabric.gateway.iservice.IFabricTransactionReader;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class FabricGatewayService implements IFabricGatewayService {
	private static final Logger log = LoggerFactory.getLogger(FabricGatewayService.class);

	private final FabricGatewayProperties properties;
	private final IFabricCAService caService;
	private final List<? extends IFabricTransactionReader> transactionReaders;
	private final ThreadPoolTaskExecutor taskExecutor;

	private Network network;

	// ------------------------------

	public FabricGatewayService(
		FabricGatewayProperties properties,
		IFabricCAService caService,
		List<? extends IFabricTransactionReader> transactionReaders,
		ThreadPoolTaskExecutor taskExecutor) {

		this.properties = properties;
		this.caService = caService;
		this.transactionReaders = transactionReaders;
		this.taskExecutor = taskExecutor;
	}

	// ------------------------------

	@PostConstruct
	public void init() {
		try {
			final String username = properties.getCaServer().getUsername();
			final Wallet wallet = caService.enroll(username, properties.getCaServer().getPassword());

			final Path networkConfigPath = Paths.get(properties.getConnectionProfileFile());

			final Gateway.Builder builder = Gateway.createBuilder();
			Gateway gateway = builder
				.identity(wallet, username)
				.networkConfig(networkConfigPath)
				.connect();

			network = gateway.getNetwork(properties.getChannel());
		} catch (Exception e) {
			throw new RuntimeException("HLF Init Error", e);
		}

		log.info("IHlfTransactionReader: count={}", transactionReaders.size());
		transactionReaders.forEach(reader -> reader.handleTransaction(
			new FabricTransactionReaderHandler(network.getChannel(), taskExecutor)));
	}

	@PreDestroy
	public void shutdown() {
		if (network != null) {
			network.getGateway().close();
		}
	}

	// ---------------

	@Override
	public byte[] submit(String method, String... args) throws Exception {
		return submit(properties.getChaincode(), method, args);
	}

	@Override
	public byte[] submit(String chaincode, String method, String... args) throws Exception {
		final Contract contract = network.getContract(getChaincode(chaincode));
		return contract.submitTransaction(method, args != null ? args : new String[0]);
	}

	@Override
	public byte[] evaluate(String method, String... args) throws Exception {
		return evaluate(properties.getChaincode(), method, args);
	}

	@Override
	public byte[] evaluate(String chaincode, String method, String... args) throws Exception {
		final Contract contract = network.getContract(getChaincode(chaincode));
		return contract.evaluateTransaction(method, args != null ? args : new String[0]);
	}

	// ------------------------------

	private String getChaincode(String chaincode) {
		return StringUtils.hasLength(chaincode) ? chaincode : properties.getChaincode();
	}
}
