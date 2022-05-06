package org.devocative.thallo.fabric.gateway.iservice;

public interface IFabricGatewayService {
	byte[] submit(String method, String... args) throws Exception;

	byte[] submit(String chaincode, String method, String... args) throws Exception;

	byte[] evaluate(String method, String... args) throws Exception;

	byte[] evaluate(String chaincode, String method, String... args) throws Exception;
}
