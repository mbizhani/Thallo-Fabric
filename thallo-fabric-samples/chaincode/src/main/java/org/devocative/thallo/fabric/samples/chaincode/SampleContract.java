package org.devocative.thallo.fabric.samples.chaincode;

import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Default
@Contract(name = "sample")
@Component
public class SampleContract implements ContractInterface {
	@Override
	public void beforeTransaction(Context ctx) {
		final ChaincodeStub stub = ctx.getStub();

		log.info("--- SampleContract.B4: mspId=[{}], func=[{}], params={}",
			ctx.getClientIdentity().getMSPID(), stub.getFunction(), stub.getParameters());
	}

	@Transaction
	public String init(final Context ctx) {
		log.info("SampleContract.init");
		return new Date().toString();
	}
}
