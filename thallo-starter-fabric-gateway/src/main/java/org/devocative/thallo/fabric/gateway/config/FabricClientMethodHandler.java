package org.devocative.thallo.fabric.gateway.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.devocative.thallo.fabric.gateway.FabricClient;
import org.devocative.thallo.fabric.gateway.Submit;
import org.devocative.thallo.fabric.gateway.iservice.IFabricGatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
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
					chaincodeArgs[i] = args[i] != null ? args[i].toString() : "";
				}
			}

			final byte[] result;
			if (method.isAnnotationPresent(Submit.class)) {
				log.info("Submit: chaincode=[{}], method=[{}], args={}",
					chaincode, method.getName(), Arrays.toString(chaincodeArgs));
				result = hlfService.submit(chaincode, method.getName(), chaincodeArgs);
			} else {
				log.info("Evaluate: chaincode=[{}], method=[{}], args={}",
					chaincode, method.getName(), Arrays.toString(chaincodeArgs));
				result = hlfService.evaluate(chaincode, method.getName(), chaincodeArgs);
			}
			final String resultAsStr = new String(result);
			log.info("Result: method=[{}], result=[{}]", method.getName(), resultAsStr);
			return processResult(method, result, resultAsStr);
		} catch (Exception e) {
			throw new RuntimeException(String.format("HlfClient: %s::%s(%s)",
				clientInterfaceClass.getCanonicalName(), method.getName(), Arrays.toString(args)), e);
		}
	}

	private Object processResult(Method method, byte[] result, String resultAsStr) throws Exception {
		final Class<?> returnClass = method.getReturnType();
		if (byte[].class.equals(returnClass)) {
			return result;
		} else if (Void.TYPE.equals(returnClass)) {
			return null;
		} else if (String.class.equals(returnClass)) {
			return resultAsStr;
		} else if (Boolean.class.equals(returnClass)) {
			return Boolean.valueOf(resultAsStr);
		} else if (Integer.class.equals(returnClass)) {
			return Integer.valueOf(resultAsStr);
		} else if (Long.class.equals(returnClass)) {
			return Long.valueOf(resultAsStr);
		} else if (Float.class.equals(returnClass)) {
			return Float.valueOf(resultAsStr);
		} else if (Double.class.equals(returnClass)) {
			return Double.valueOf(resultAsStr);
		} else if (BigDecimal.class.equals(returnClass)) {
			return new BigDecimal(resultAsStr);
		} else {
			final Type genericReturnType = method.getGenericReturnType();
			final TypeFactory typeFactory = objectMapper.getTypeFactory();
			final JavaType javaType = typeFactory.constructType(genericReturnType);
			return objectMapper.readValue(resultAsStr, javaType);
		}
	}
}
