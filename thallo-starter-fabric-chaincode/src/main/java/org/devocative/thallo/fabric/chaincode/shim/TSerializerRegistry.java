package org.devocative.thallo.fabric.chaincode.shim;

import org.devocative.thallo.fabric.chaincode.ClassUtil;
import org.devocative.thallo.fabric.chaincode.config.FabricChaincodeProperties;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.SerializerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class TSerializerRegistry {
	private static final Logger log = LoggerFactory.getLogger(TSerializerRegistry.class);

	private final Map<String, SerializerInterface> contents = new HashMap<>();
	private final Class<Serializer> annotationClass = Serializer.class;

	private final FabricChaincodeProperties config;
	private final ApplicationContext context;

	// ------------------------------

	public TSerializerRegistry(FabricChaincodeProperties config, ApplicationContext context) {
		this.config = config;
		this.context = context;
	}

	// ------------------------------

	public SerializerInterface getSerializer(final String name, final Serializer.TARGET target) {
		final String key = name + ":" + target;
		return contents.get(key);
	}

	public void findAndSetContents() throws InstantiationException, IllegalAccessException {

		final Set<String> basePackages = ClassUtil.findBasePackages(context);
		basePackages.add(config.getSerializer().getFabricBasePackage());
		basePackages.addAll(config.getSerializer().getScanPackages());

		log.info("Scan for @{}: basePackage = {}", annotationClass.getSimpleName(), basePackages);

		ClassUtil.scanPackagesForAnnotatedClasses(annotationClass, basePackages, beanDefinition -> {
			try {
				final Class<? extends SerializerInterface> cls = (Class<? extends SerializerInterface>) Class.forName(beanDefinition.getBeanClassName());
				log.info("TSerializerRegistry - SerializerInterface = {}", cls.getName());
				add(cls.getName(), Serializer.TARGET.TRANSACTION, cls);
			} catch (ClassNotFoundException e) {
				log.warn("@{} Class Not Found: {}",
					annotationClass.getSimpleName(), beanDefinition.getBeanClassName(), e);
			}
		});
	}

	// ------------------------------

	private void add(final String name, final Serializer.TARGET target, final Class<? extends SerializerInterface> clazz) {
		log.debug("Adding new Class [{}] for [{}]", clazz.getName(), target);

		try {
			final String key = name + ":" + target;
			final SerializerInterface newObj = clazz.getDeclaredConstructor().newInstance();
			contents.put(key, newObj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
