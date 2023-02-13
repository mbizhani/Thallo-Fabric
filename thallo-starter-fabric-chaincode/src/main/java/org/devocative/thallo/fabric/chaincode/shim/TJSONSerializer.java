package org.devocative.thallo.fabric.chaincode.shim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.contract.execution.SerializerInterface;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TJSONSerializer implements SerializerInterface {
	private static final Logger log = LoggerFactory.getLogger(TJSONSerializer.class);

	private final ObjectMapper mapper;

	// ------------------------------

	public TJSONSerializer(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	// ------------------------------

	@Override
	public byte[] toBuffer(Object value, TypeSchema ts) {
		log.debug("TJSONSerializer - toBuffer: type={}", ts);

		try {
			return mapper.writeValueAsBytes(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object fromBuffer(byte[] buffer, TypeSchema ts) {
		log.debug("TJSONSerializer - fromBuffer: type={}", ts);

		final TypeSchema schema = (TypeSchema) ts.get("schema");
		final Class<?> cls = (Class) schema.get("type");

		try {
			return mapper.readValue(buffer, cls);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
