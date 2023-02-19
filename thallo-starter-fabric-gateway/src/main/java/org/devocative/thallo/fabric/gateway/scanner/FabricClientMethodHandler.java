package org.devocative.thallo.fabric.gateway.scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.devocative.thallo.fabric.gateway.Evaluate;
import org.devocative.thallo.fabric.gateway.FabricClient;
import org.devocative.thallo.fabric.gateway.Submit;
import org.devocative.thallo.fabric.gateway.iservice.IFabricGatewayService;
import org.hyperledger.fabric.gateway.ContractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (log.isDebugEnabled()) {
			log.debug("Invoke @FabricClient: class=[{}] method=[{}] args={}",
				clientInterfaceClass.getName(), method.getName(), Arrays.toString(args));
		} else {
			log.info("Invoke @FabricClient: class=[{}] method=[{}]",
				clientInterfaceClass.getName(), method.getName());
		}

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

	private Object callChaincode(Method method, Object[] args) throws IOException, ContractException {
		final FabricClient hlfClient = clientInterfaceClass.getAnnotation(FabricClient.class);
		final String chaincode = StringUtils.hasText(hlfClient.chaincode()) ?
			hlfClient.chaincode() :
			hlfService.getDefaultChaincode();

		String[] chaincodeArgs = null;
		if (args != null) {
			chaincodeArgs = new String[args.length];
			for (int i = 0; i < args.length; i++) {
				chaincodeArgs[i] = args[i] != null ? objectMapper.writeValueAsString(args[i]) : "";
			}
		}

		final byte[] result;
		final String ccMethod;
		final long start = System.currentTimeMillis();
		if (method.isAnnotationPresent(Submit.class)) {
			final Submit submit = method.getAnnotation(Submit.class);
			ccMethod = StringUtils.hasText(submit.method()) ? submit.method() : method.getName();

			if (log.isDebugEnabled()) {
				log.debug("Submit: chaincode=[{}], ccMethod=[{}], args={}",
					chaincode, ccMethod, Arrays.toString(chaincodeArgs));
			} else {
				log.info("Submit: chaincode=[{}], ccMethod=[{}]", chaincode, ccMethod);
			}

			result = hlfService.submit(chaincode, ccMethod, chaincodeArgs);
		} else if (method.isAnnotationPresent(Evaluate.class)) {
			final Evaluate evaluate = method.getAnnotation(Evaluate.class);
			ccMethod = StringUtils.hasText(evaluate.method()) ? evaluate.method() : method.getName();

			if (log.isDebugEnabled()) {
				log.debug("Evaluate: chaincode=[{}], ccMethod=[{}], args={}",
					chaincode, ccMethod, Arrays.toString(chaincodeArgs));
			} else {
				log.info("Evaluate: chaincode=[{}], ccMethod=[{}]", chaincode, ccMethod);
			}

			result = hlfService.evaluate(chaincode, ccMethod, chaincodeArgs);
		} else {
			//TODO: change exception type
			throw new RuntimeException("No Annotation for Client Method : method = " + method.getName());
		}

		log.info("@FabricClient Method Executed: method=[{}] ccMethod=[{}] dur=[{}]",
			method.getName(), ccMethod, System.currentTimeMillis() - start);

		return processResult(method, result);
	}

	private Object processResult(Method method, byte[] result) throws IOException {
		final Type returnType = method.getGenericReturnType();
		final Class<?> returnClass = method.getReturnType();

		if (Void.TYPE.equals(returnClass)) {
			return null;
		} else if (byte[].class.equals(returnClass)) {
			return result;
		} else if (CharSequence.class.isAssignableFrom(returnClass)) {
			return new String(result);
		}

		return objectMapper.readValue(result, new GeneralTypeReference<>(returnType));
	}
}
