package org.devocative.thallo.fabric.samples.gateway.iservice;

import org.devocative.thallo.fabric.gateway.FabricClient;
import org.devocative.thallo.fabric.samples.gateway.dto.Asset;

import java.util.Date;
import java.util.List;

@FabricClient
public interface ISampleContract {
	Date getTime();

	void setTime(Date dt, List<String> list);

	Asset createAsset();

	void updateAsset(Asset asset);

	List<Asset> list();
}
