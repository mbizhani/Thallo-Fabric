package org.devocative.thallo.fabric.samples.chaincode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.devocative.thallo.fabric.samples.chaincode.dto.Asset;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hyperledger.fabric.contract.annotation.Transaction.TYPE.EVALUATE;

@RequiredArgsConstructor
@Default
@Contract(name = "sample")
@Component
public class SampleContract implements ContractInterface {
	private static final Logger log = LoggerFactory.getLogger(SampleContract.class);

	private final ObjectMapper mapper;

	// ------------------------------

	@Override
	public void beforeTransaction(Context ctx) {
		final ChaincodeStub stub = ctx.getStub();

		log.info("--- SampleContract.B4: mspId=[{}], func=[{}], params={}",
			ctx.getClientIdentity().getMSPID(), stub.getFunction(), stub.getParameters());
	}

	// ---------------

	@Transaction(intent = EVALUATE)
	public Date getTime(final Context ctx) {
		log.info("SampleContract.getTime");
		return new Date();
	}

	@Transaction
	public void setTime(final Context ctx, Date date, List<String> list) {
		log.info("SampleContract.setTime: {} - {}", date, list);
	}

	// ---------------

	@Transaction
	public Asset createAsset(final Context ctx, String name, BigDecimal price) {
		log.info("CreateAsset: name=[{}] price=[{}]", name, price);

		final ChaincodeStub stub = ctx.getStub();

		final String id = "ast_" + stub.getTxId();
		final Instant instant = stub.getTxTimestamp();
		final String orgId = ctx.getClientIdentity().getMSPID();

		final Asset asset = new Asset(id, name, price, instant, orgId);
		stub.putState(id, serialize(asset));

		return asset;
	}

	@Transaction
	public void updateAsset(final Context ctx, String id, String name, BigDecimal price) {
		log.info("UpdateAsset: id=[{}] name=[{}] price=[{}]", id, name, price);

		final ChaincodeStub stub = ctx.getStub();

		final Instant instant = stub.getTxTimestamp();
		final String orgId = ctx.getClientIdentity().getMSPID();

		final Asset asset = readAsset(stub, id).orElseThrow(() -> new RuntimeException("Asset Not Found: " + id));
		asset.setName(name);
		asset.setPrice(price);
		asset.setUpdatedDateTime(instant);
		asset.setUpdatedOrgId(orgId);

		stub.putState(id, serialize(asset));
	}

	@Transaction(intent = EVALUATE)
	public byte[] getAsset(final Context ctx, String id) {
		log.info("GetAsset: id=[{}]", id);

		final ChaincodeStub stub = ctx.getStub();
		return stub.getState(id);
	}

	@Transaction(intent = EVALUATE)
	public String getAllAssets(final Context ctx) {
		try (final QueryResultsIterator<KeyValue> range = ctx.getStub().getStateByRange("", "")) {
			final String list = StreamSupport
				.stream(range.spliterator(), false)
				.map(KeyValue::getStringValue)
				.collect(Collectors.joining(","));

			return String.format("[%s]", list);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// ------------------------------

	private Optional<Asset> readAsset(ChaincodeStub stub, String id) {
		final byte[] state = stub.getState(id);
		if (state != null) {
			return Optional.of(deserialize(state));
		}
		return Optional.empty();
	}

	private byte[] serialize(Asset asset) {
		try {
			return mapper.writeValueAsBytes(asset);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private Asset deserialize(byte[] bytes) {
		try {
			return mapper.readValue(bytes, Asset.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
