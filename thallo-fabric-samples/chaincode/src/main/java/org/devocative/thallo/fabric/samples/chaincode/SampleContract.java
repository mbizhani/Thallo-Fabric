package org.devocative.thallo.fabric.samples.chaincode;

import lombok.extern.slf4j.Slf4j;
import org.devocative.thallo.fabric.samples.chaincode.dto.Asset;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hyperledger.fabric.contract.annotation.Transaction.TYPE.EVALUATE;

@Slf4j
@Default
@Contract(name = "sample", transactionSerializer = "org.devocative.thallo.fabric.chaincode.shim.TJSONTransactionSerializer")
@Component
public class SampleContract implements ContractInterface {
	@Override
	public void beforeTransaction(Context ctx) {
		final ChaincodeStub stub = ctx.getStub();

		log.info("--- SampleContract.B4: mspId=[{}], func=[{}], params={}",
			ctx.getClientIdentity().getMSPID(), stub.getFunction(), stub.getParameters());
	}

	@Transaction(intent = EVALUATE)
	public Date getTime(final Context ctx) {
		log.info("SampleContract.getTime");
		return new Date();
	}

	@Transaction
	public void setTime(final Context ctx, Date date, List<String> list) {
		log.info("SampleContract.setTime: {} - {}", date, list);
	}

	@Transaction
	public Asset createAsset(final Context ctx) {
		return new Asset("1", "A1", new BigDecimal(12));
	}

	@Transaction
	public void updateAsset(final Context ctx, Asset asset) {
		log.info("UpdateAsset: {}", asset);
	}

	@Transaction(intent = EVALUATE)
	public List<Asset> list(final Context ctx) {
		return Arrays.asList(
			new Asset("1", "A1", new BigDecimal(1)),
			new Asset("2", "A2", new BigDecimal(2)),
			new Asset("3", "A3", new BigDecimal(3))
		);
	}
}
