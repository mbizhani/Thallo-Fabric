package org.devocative.thallo.fabric.chaincode.shim;

import org.hyperledger.fabric.contract.annotation.Serializer;
import org.hyperledger.fabric.contract.execution.JSONTransactionSerializer;
import org.hyperledger.fabric.contract.metadata.TypeSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Serializer
public class TJSONTransactionSerializer extends JSONTransactionSerializer {
	private static final Logger log = LoggerFactory.getLogger(TJSONTransactionSerializer.class);

	@Override
	public byte[] toBuffer(Object value, TypeSchema ts) {
		log.info("TJSONTransactionSerializer - toBuffer: type=[{}]", ts.getType());
		return super.toBuffer(value, ts);
	}

	@Override
	public Object fromBuffer(byte[] buffer, TypeSchema ts) {
		log.info("TJSONTransactionSerializer - fromBuffer: type=[{}]", ts.getType());
		return super.fromBuffer(buffer, ts);
	}
}
