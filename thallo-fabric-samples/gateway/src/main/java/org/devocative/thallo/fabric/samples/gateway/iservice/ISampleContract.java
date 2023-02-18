package org.devocative.thallo.fabric.samples.gateway.iservice;

import org.devocative.thallo.fabric.gateway.Evaluate;
import org.devocative.thallo.fabric.gateway.FabricClient;
import org.devocative.thallo.fabric.gateway.Submit;
import org.devocative.thallo.fabric.samples.gateway.dto.Asset;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@FabricClient
public interface ISampleContract {
	@Evaluate
	Date getTime();

	@Submit
	void setTime(Date dt, List<String> list);

	@Submit
	Asset createAsset(String name, BigDecimal price);

	@Submit
	void updateAsset(String id, String name, BigDecimal price);

	@Evaluate
	byte[] getAsset(String id);

	@Evaluate
	List<Asset> getAllAssets();
}
