# Sample chaincode based on Thallo Fabric Chaincode

## Sample Method Calls

- `./start-fabric-dev-mode/run.sh call i setTime "\"1676198566260\", \"[\\\"A\\\"]\""`
- `./start-fabric-dev-mode/run.sh call i createAsset "\"{\\\"name\\\":\\\"a1\\\",\\\"price\\\":12.5}\""`
- `./start-fabric-dev-mode/run.sh call i createAsset "\"\\\"a1\\\"\",\"12.5\""`
- `./start-fabric-dev-mode/run.sh call q getAllAssets`