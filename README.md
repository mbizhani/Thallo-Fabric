# Thallo & Hyperledger Fabric

Spring Boot Integration for Hyperledger Fabric Gateway &amp; Chaincode

## Start Fabric Services in Dev Mode

- Download `configtxgen`, `orderer`, and `peer` binary files
  from [Fabric's GitHub](https://github.com/hyperledger/fabric/releases)
- Create dir `start-fabric-dev-mode/files/bin` and copy previous files in it
- Exec `./start-fabric-dev-mode/run.sh start`

### Sample Method Calls

- `./start-fabric-dev-mode/run.sh call q getTime`
- `./start-fabric-dev-mode/run.sh call i setTime "\"1676198566260\", \"[\\\"A\\\"]\""`
- `./start-fabric-dev-mode/run.sh call i updateAsset "\"{\\\"id\\\":\\\"1\\\",\\\"price\\\":12.5}\""`