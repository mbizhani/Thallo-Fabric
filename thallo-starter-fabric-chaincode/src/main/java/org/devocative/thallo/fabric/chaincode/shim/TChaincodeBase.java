package org.devocative.thallo.fabric.chaincode.shim;

import org.devocative.thallo.fabric.chaincode.config.FabricChaincodeProperties;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.ExecutionFactory;
import org.hyperledger.fabric.contract.execution.ExecutionService;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.execution.SerializerInterface;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.RoutingRegistry;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.metrics.Metrics;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.traces.Traces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class TChaincodeBase extends ChaincodeBase {
	private static final Logger log = LoggerFactory.getLogger(TChaincodeBase.class);

	private final RoutingRegistry registry;
	private final TSerializerRegistry serializers;
	private final ApplicationContext context;
	private final FabricChaincodeProperties properties;

	private final String chaincodeId;

	// ------------------------------

	public TChaincodeBase(RoutingRegistry registry, TSerializerRegistry serializers, FabricChaincodeProperties properties, ApplicationContext context) {
		this.registry = registry;
		this.serializers = serializers;
		this.properties = properties;
		this.context = context;

		chaincodeId = properties.getId();
		if (chaincodeId == null || chaincodeId.isEmpty()) {
			throw new RuntimeException("Chaincode ID Not Found");
		}

		if (properties.getDevMode() != null && properties.getDevMode().isEnabled()) {
			log.info("--- TChaincodeBase (*** D E V   M O D E ***) - Chaincode ID = {}", chaincodeId);
		} else {
			log.info("--- TChaincodeBase - Chaincode ID = {}", chaincodeId);
		}
	}

	// ------------------------------

	@PostConstruct
	public void init() {
		final FabricChaincodeProperties.DevModeProperties devMode = properties.getDevMode();

		final List<String> args = new ArrayList<>();
		args.add("-i");
		args.add(chaincodeId);
		if (devMode != null && devMode.isEnabled()) {
			args.add("-a");
			args.add(devMode.getPeerAddress());
		}

		super.initializeLogging();
		super.processEnvironmentOptions();
		super.processCommandLineOptions(args.toArray(new String[0]));
		super.validateOptions();

		final Properties props = super.getChaincodeConfig();
		Metrics.initialize(props);
		Traces.initialize(props);

		if (devMode != null && devMode.isEnabled()) {
			try {
				connectToPeer();
			} catch (IOException e) {
				log.error("DevMode: ConnectToPeer", e);
			}
		}

		final Map<String, ContractInterface> contracts = context.getBeansOfType(ContractInterface.class);
		contracts.values().forEach(contractInterface -> {
			final Class<? extends ContractInterface> cls = contractInterface.getClass();
			log.info("--- TChaincodeBase - ContractInterface as Bean: {}", cls.getName());
			registry.addNewContract((Class<ContractInterface>) cls);
		});

		try {
			serializers.findAndSetContents();
		} catch (InstantiationException | IllegalAccessException e) {
			log.error("BornaChaincodeBase.Init", e);
			throw new RuntimeException("Unable to locate Serializers", e);
		}

		//TODO: there is no need for @DataType registry
		//final TypeRegistry typeRegistry = TypeRegistry.getRegistry();
		//registry.findAndSetContracts(typeRegistry);
		//MetadataBuilder.initialize(registry, typeRegistry);
		//log.info("TChaincodeBase.Metadata: {}", MetadataBuilder.debugString());
	}

	@Override
	public Response init(ChaincodeStub stub) {
		return processRequest(stub);
	}

	@Override
	public Response invoke(ChaincodeStub stub) {
		return processRequest(stub);
	}

	// ------------------------------

	private Response processRequest(final ChaincodeStub stub) {
		try {
			if (stub.getStringArgs().size() > 0) {
				if (log.isDebugEnabled()) {
					log.debug("TChaincodeBase: Incoming Request - func = {}({})", stub.getFunction(), stub.getParameters());
				} else {
					log.info("TChaincodeBase: Incoming Request - func = {}", stub.getFunction());
				}
				final InvocationRequest request = ExecutionFactory.getInstance().createRequest(stub);
				final TxFunction txFn = getRouting(request);

				validateRequestByTxFunction(request, txFn);

				final SerializerInterface si = serializers.getSerializer(txFn.getRouting().getSerializerName(),
					Serializer.TARGET.TRANSACTION);
				final ExecutionService executor = ExecutionFactory.getInstance().createExecutionService(si);

				log.info("Got Routing: {}", txFn.getRouting());
				return executor.executeRequest(txFn, request, stub);
			} else {
				return ResponseUtils.newSuccessResponse();
			}
		} catch (final Throwable throwable) {
			return ResponseUtils.newErrorResponse(throwable);
		}
	}

	private TxFunction getRouting(final InvocationRequest request) {
		if (registry.containsRoute(request)) {
			return registry.getTxFn(request);
		} else {
			log.info("Namespace: {}", request.getNamespace());
			final ContractDefinition contract = registry.getContract(request.getNamespace());
			return contract.getUnknownRoute();
		}
	}

	private static void validateRequestByTxFunction(InvocationRequest request, TxFunction txFn) {
		if (txFn.getParamsList().size() != request.getArgs().size()) {
			throw new ContractRuntimeException(String.format(
				"Mismatch Parameter(s): chaincode func [%s] should have [%s] parameter(s), sent [%s]",
				txFn.getName(), txFn.getParamsList().size(), request.getArgs().size()));
		}
	}
}
