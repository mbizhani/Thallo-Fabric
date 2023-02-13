package org.devocative.thallo.fabric.chaincode.shim;

import org.devocative.thallo.fabric.chaincode.config.FabricChaincodeProperties;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.SerializerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TSerializerRegistry {
	private static final Logger log = LoggerFactory.getLogger(TSerializerRegistry.class);

	private final Map<String, SerializerInterface> serializers = new HashMap<>();
	private final FabricChaincodeProperties config;
	private final ApplicationContext context;

	private SerializerInterface defaultSerializer;

	// ------------------------------

	public TSerializerRegistry(FabricChaincodeProperties config, ApplicationContext context) {
		this.config = config;
		this.context = context;
	}

	// ------------------------------

	public SerializerInterface getSerializer(final String name, final Serializer.TARGET target) {
		if (serializers.containsKey(name)) {
			return serializers.get(name);
		} else if (defaultSerializer != null) {
			return defaultSerializer;
		} else {
			throw new RuntimeException("No Valid Serializer: " + name);
		}
	}

	public void findAndSetContents() throws InstantiationException, IllegalAccessException {
		final Map<String, SerializerInterface> beans = context.getBeansOfType(SerializerInterface.class);

		for (SerializerInterface value : beans.values()) {
			final String name = value.getClass().getName();
			serializers.put(name, value);

			if (name.equals(config.getDefaultSerializer())) {
				defaultSerializer = value;
				log.info("TSerializerRegistry - default-serializer = {}", name);
			} else {
				log.info("TSerializerRegistry - serializer = {}", name);
			}
		}
	}
}
