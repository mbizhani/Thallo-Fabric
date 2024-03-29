package org.devocative.thallo.fabric.chaincode.shim;

import org.devocative.thallo.fabric.chaincode.shim.vo.TContractDefinition;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.ContractRuntimeException;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.contract.execution.InvocationRequest;
import org.hyperledger.fabric.contract.routing.ContractDefinition;
import org.hyperledger.fabric.contract.routing.RoutingRegistry;
import org.hyperledger.fabric.contract.routing.TxFunction;
import org.hyperledger.fabric.contract.routing.TypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class TRoutingRegistry implements RoutingRegistry {
	private static final Logger log = LoggerFactory.getLogger(TRoutingRegistry.class);

	private final Map<String, ContractDefinition> contracts = new HashMap<>();

	private final ApplicationContext context;

	// ------------------------------

	public TRoutingRegistry(ApplicationContext context) {
		this.context = context;
	}

	// ------------------------------

	@Override
	public ContractDefinition addNewContract(Class<ContractInterface> clz) {
		log.info("Adding New Contract Class: {}", clz.getCanonicalName());

		final ContractInterface contractInterface = context.getBean(clz);
		final ContractDefinition contractDefinition = new TContractDefinition(contractInterface);

		contracts.put(contractDefinition.getName(), contractDefinition);

		if (contractDefinition.isDefault()) {
			contracts.put(InvocationRequest.DEFAULT_NAMESPACE, contractDefinition);
		}

		for (final Method m : clz.getMethods()) {
			if (m.isAnnotationPresent(Transaction.class)) {
				contractDefinition.addTxFunction(m);
				log.info("Contract Method Added: [{}.{}]", contractDefinition.getName(), m.getName());
			}
		}

		return contractDefinition;
	}

	@Override
	public boolean containsRoute(InvocationRequest request) {
		if (contracts.containsKey(request.getNamespace())) {
			final ContractDefinition cd = contracts.get(request.getNamespace());
			return cd.hasTxFunction(request.getMethod());
		}
		return false;
	}

	@Override
	public TxFunction.Routing getRoute(InvocationRequest request) {
		final TxFunction txFunction = contracts.get(request.getNamespace()).getTxFunction(request.getMethod());
		return txFunction.getRouting();
	}

	@Override
	public TxFunction getTxFn(InvocationRequest request) {
		return contracts.get(request.getNamespace()).getTxFunction(request.getMethod());
	}

	@Override
	public ContractDefinition getContract(String namespace) {
		final ContractDefinition contract = contracts.get(namespace);

		if (contract == null) {
			throw new ContractRuntimeException("Undefined Contract Called: " + namespace);
		}

		return contract;
	}

	@Override
	public Collection<ContractDefinition> getAllDefinitions() {
		return contracts.values();
	}

	@Override
	public void findAndSetContracts(TypeRegistry typeRegistry) {
		throw new RuntimeException("No need for @DataType registry!");
	}
}
