package org.devocative.thallo.fabric.chaincode.shim;

import org.devocative.thallo.fabric.chaincode.config.FabricChaincodeProperties;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.ExecutionFactory;
import org.hyperledger.fabric.contract.execution.ExecutionService;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.execution.SerializerInterface;
import org.hyperledger.fabric.contract.metadata.MetadataBuilder;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.RoutingRegistry;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
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
import java.util.Map;
import java.util.Properties;

@Component
public class TChaincodeBase extends ChaincodeBase {
	private static final Logger log = LoggerFactory.getLogger(TChaincodeBase.class);

	private final RoutingRegistry registry;
	private final TSerializerRegistry serializers;
	private final ApplicationContext context;

	private final String chaincodeId;

	// ------------------------------

	public TChaincodeBase(RoutingRegistry registry, TSerializerRegistry serializers, FabricChaincodeProperties properties, ApplicationContext context) {
		this.registry = registry;
		this.serializers = serializers;
		this.context = context;

		chaincodeId = properties.getId();
		if (chaincodeId == null || chaincodeId.isEmpty()) {
			throw new RuntimeException("Chaincode ID Not Found");
		}
		log.info("--- TChaincodeBase - Chaincode ID = {}", chaincodeId);
	}

	// ------------------------------

	@PostConstruct
	public void init() {
		super.initializeLogging();
		super.processEnvironmentOptions();
		super.processCommandLineOptions(new String[]{"-i", chaincodeId});
		super.validateOptions();

		final Properties props = super.getChaincodeConfig();
		Metrics.initialize(props);
		Traces.initialize(props);

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

		final TypeRegistry typeRegistry = TypeRegistry.getRegistry();
		registry.findAndSetContracts(typeRegistry);
		MetadataBuilder.initialize(registry, typeRegistry);
		log.info("TChaincodeBase.Metadata: {}", MetadataBuilder.debugString());
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
				log.info("Got the Invoke Request: func = {}({})", stub.getFunction(), stub.getParameters());
				final InvocationRequest request = ExecutionFactory.getInstance().createRequest(stub);
				final TxFunction txFn = getRouting(request);

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
}
