package org.devocative.thallo.fabric.gateway.dto;

import java.util.Arrays;

public class FabricTransactionInfo {
	private final Long blockNumber;
	private final String transactionId;
	private final Integer responseStatus;
	private final String chainCode;
	private final String methodName;
	private final String[] methodArgs;
	private final String methodResult;
	private final Long timestamp;

	// ------------------------------

	public FabricTransactionInfo(Long blockNumber, String transactionId, Integer responseStatus, String chainCode, String methodName, String[] methodArgs, String methodResult, Long timestamp) {
		this.blockNumber = blockNumber;
		this.transactionId = transactionId;
		this.responseStatus = responseStatus;
		this.chainCode = chainCode;
		this.methodName = methodName;
		this.methodArgs = methodArgs;
		this.methodResult = methodResult;
		this.timestamp = timestamp;
	}

	// ------------------------------

	public Long getBlockNumber() {
		return blockNumber;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public Integer getResponseStatus() {
		return responseStatus;
	}

	public String getChainCode() {
		return chainCode;
	}

	public String getMethodName() {
		return methodName;
	}

	public String[] getMethodArgs() {
		return methodArgs;
	}

	public String getMethodResult() {
		return methodResult;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	@Override
	public String toString() {
		return "FabricTransactionInfo{" +
			"methodArgs=" + Arrays.toString(methodArgs) +
			", methodResult='" + methodResult + '\'' +
			'}';
	}
}
