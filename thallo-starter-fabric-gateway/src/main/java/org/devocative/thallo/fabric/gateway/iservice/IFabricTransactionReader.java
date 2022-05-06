package org.devocative.thallo.fabric.gateway.iservice;

import org.devocative.thallo.fabric.gateway.service.FabricTransactionReaderHandler;

public interface IFabricTransactionReader {
	void handleTransaction(FabricTransactionReaderHandler handler);
}
