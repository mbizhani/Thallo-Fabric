package org.devocative.thallo.fabric.gateway.iservice;

import org.hyperledger.fabric.gateway.ContractException;

public interface IFabricGatewayService {
	byte[] submit(String method, String... args) throws ContractException;

	byte[] submit(String chaincode, String method, String... args) throws ContractException;

	byte[] evaluate(String method, String... args) throws ContractException;

	byte[] evaluate(String chaincode, String method, String... args) throws ContractException;
}
