package org.devocative.thallo.fabric.gateway.scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.devocative.thallo.fabric.gateway.FabricClient;
import org.devocative.thallo.fabric.gateway.Submit;
import org.devocative.thallo.fabric.gateway.iservice.IFabricGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

public class FabricClientMethodHandler implements InvocationHandler {
	private static final Logger log = LoggerFactory.getLogger(FabricClientMethodHandler.class);

	private final Class<?> clientInterfaceClass;
	private final IFabricGatewayService hlfService;
	private final ObjectMapper objectMapper;

	// ------------------------------

	public FabricClientMethodHandler(Class<?> clientInterfaceClass, IFabricGatewayService hlfService, ObjectMapper objectMapper) {
		this.clientInterfaceClass = clientInterfaceClass;
		this.hlfService = hlfService;
		this.objectMapper = objectMapper;
	}

	// ------------------------------

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) {
		log.info("Invoke Client: method=[{}] args={}", method.getName(), Arrays.toString(args));

		switch (method.getName()) {
			case "hashCode":
				return clientInterfaceClass.hashCode();
			case "toString":
				return clientInterfaceClass.getCanonicalName();
			default:
				return callChaincode(method, args);
		}
	}

	// ------------------------------

	private Object callChaincode(Method method, Object[] args) {
		try {
			final FabricClient hlfClient = clientInterfaceClass.getAnnotation(FabricClient.class);
			final String chaincode = hlfClient.chaincode();

			String[] chaincodeArgs = null;
			if (args != null) {
				chaincodeArgs = new String[args.length];
				for (int i = 0; i < args.length; i++) {
					chaincodeArgs[i] = args[i] != null ? objectMapper.writeValueAsString(args[i]) : "";
				}
			}

			final byte[] result;
			if (method.isAnnotationPresent(Submit.class)) {
				if (log.isDebugEnabled()) {
					log.debug("Submit: chaincode=[{}], method=[{}], args={}",
						chaincode, method.getName(), Arrays.toString(chaincodeArgs));
				} else {
					log.info("Submit: chaincode=[{}], method=[{}]", chaincode, method.getName());
				}

				result = hlfService.submit(chaincode, method.getName(), chaincodeArgs);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Evaluate: chaincode=[{}], method=[{}], args={}",
						chaincode, method.getName(), Arrays.toString(chaincodeArgs));
				} else {
					log.info("Evaluate: chaincode=[{}], method=[{}]", chaincode, method.getName());
				}

				result = hlfService.evaluate(chaincode, method.getName(), chaincodeArgs);
			}
			log.info("Result: method=[{}]", method.getName());
			return processResult(method, result);
		} catch (Exception e) {
			throw new RuntimeException(String.format("HlfClient: %s::%s(%s)",
				clientInterfaceClass.getCanonicalName(), method.getName(), Arrays.toString(args)), e);
		}
	}

	private Object processResult(Method method, byte[] result) throws Exception {
		final Class<?> returnClass = method.getReturnType();
		if (Void.TYPE.equals(returnClass)) {
			return null;
		}
		return objectMapper.readValue(result, returnClass);
	}
}
