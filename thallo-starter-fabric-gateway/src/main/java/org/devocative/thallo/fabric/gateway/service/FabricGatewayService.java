package org.devocative.thallo.fabric.gateway.service;

import org.devocative.thallo.fabric.gateway.SystemException;
import org.devocative.thallo.fabric.gateway.config.FabricGatewayProperties;
import org.devocative.thallo.fabric.gateway.iservice.IFabricCAService;
import org.devocative.thallo.fabric.gateway.iservice.IFabricGatewayService;
import org.devocative.thallo.fabric.gateway.iservice.IFabricTransactionReader;
import org.hyperledger.fabric.gateway.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Service
public class FabricGatewayService implements IFabricGatewayService {
	private static final Logger log = LoggerFactory.getLogger(FabricGatewayService.class);

	private final FabricGatewayProperties properties;
	private final Optional<IFabricCAService> caServiceOpt;
	private final List<? extends IFabricTransactionReader> transactionReaders;
	private final ThreadPoolTaskExecutor taskExecutor;

	private Network network;

	// ------------------------------

	public FabricGatewayService(
		FabricGatewayProperties properties,
		Optional<IFabricCAService> caServiceOpt,
		List<? extends IFabricTransactionReader> transactionReaders,
		ThreadPoolTaskExecutor taskExecutor) {

		this.properties = properties;
		this.caServiceOpt = caServiceOpt;
		this.transactionReaders = transactionReaders;
		this.taskExecutor = taskExecutor;
	}

	// ------------------------------

	@PostConstruct
	public void init() {
		try {
			final Path networkConfigPath = Paths.get(properties.getConnectionProfileFile());

			final Gateway.Builder builder = Gateway.createBuilder();
			builder.networkConfig(networkConfigPath);

			if (caServiceOpt.isPresent()) {
				final String username = properties.getCa().getServer().getUsername();

				final IFabricCAService caService = caServiceOpt.get();
				final Wallet wallet = caService.enroll(username, properties.getCa().getServer().getPassword());

				builder.identity(wallet, username);
			} else if (properties.getIdentity() != null) {
				log.info("Identity for Gateway: {}", properties.getIdentity());

				final PrivateKey privateKey = loadPrivateKey();
				final X509Certificate certificate = loadCertificate();

				builder.identity(Identities.newX509Identity(properties.getOrgMspId(), certificate, privateKey));
			} else {
				throw new RuntimeException("Invalid application config: 'ca.server' or 'identity' must be set");
			}

			final Gateway gateway = builder.connect();

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
	public byte[] submit(String method, String... args) throws ContractException {
		return submit(properties.getChaincode(), method, args);
	}

	@Override
	public byte[] submit(String chaincode, String method, String... args) throws ContractException {
		final Contract contract = network.getContract(getChaincode(chaincode));
		try {
			return contract.submitTransaction(method, args != null ? args : new String[0]);
		} catch (TimeoutException | InterruptedException e) {
			throw new SystemException(e);
		}
	}

	@Override
	public byte[] evaluate(String method, String... args) throws ContractException {
		return evaluate(properties.getChaincode(), method, args);
	}

	@Override
	public byte[] evaluate(String chaincode, String method, String... args) throws ContractException {
		final Contract contract = network.getContract(getChaincode(chaincode));
		return contract.evaluateTransaction(method, args != null ? args : new String[0]);
	}

	// ------------------------------

	private String getChaincode(String chaincode) {
		return StringUtils.hasLength(chaincode) ? chaincode : properties.getChaincode();
	}

	private PrivateKey loadPrivateKey() throws IOException, InvalidKeyException {
		final FabricGatewayProperties.IdentityProperties identity = properties.getIdentity();

		final PrivateKey privateKey;
		if (identity.getPrivateKeyPemFile() != null) {
			try (final FileReader reader = new FileReader(identity.getPrivateKeyPemFile())) {
				privateKey = Identities.readPrivateKey(reader);
			}
		} else if (identity.getPrivateKeyPem() != null) {
			privateKey = Identities.readPrivateKey(identity.getPrivateKeyPem());
		} else {
			throw new RuntimeException("Invalid application config: private key data must be set in 'identity'");
		}
		return privateKey;
	}

	private X509Certificate loadCertificate() throws IOException, CertificateException {
		final FabricGatewayProperties.IdentityProperties identity = properties.getIdentity();

		final X509Certificate certificate;
		if (identity.getCertificatePemFile() != null) {
			try (final FileReader reader = new FileReader(identity.getCertificatePemFile())) {
				certificate = Identities.readX509Certificate(reader);
			}
		} else if (identity.getCertificatePem() != null) {
			certificate = Identities.readX509Certificate(identity.getCertificatePem());
		} else {
			throw new RuntimeException("Invalid application config: certificate data must be set in 'identity'");
		}
		return certificate;
	}

}
