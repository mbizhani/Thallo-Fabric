package org.devocative.thallo.fabric.chaincode.shim;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.SerializerInterface;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Serializer
public class TJSONTransactionSerializer implements SerializerInterface {
	private static final Logger log = LoggerFactory.getLogger(TJSONTransactionSerializer.class);

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public byte[] toBuffer(Object value, TypeSchema ts) {
		log.info("TJSONTransactionSerializer - toBuffer: type={}", ts);
		try {
			return mapper.writeValueAsBytes(value);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object fromBuffer(byte[] buffer, TypeSchema ts) {
		log.info("TJSONTransactionSerializer - fromBuffer: type={}", ts);

		final TypeSchema schema = (TypeSchema) ts.get("schema");
		final TypeReference<?> tr = (TypeReference) schema.get("type");

		try {
			return mapper.readValue(buffer, tr);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
