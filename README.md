# Thallo & Hyperledger Fabric

## Introduction

This project is an integration between

- `thallo-starter-fabric-chaincode`: `spring-boot` and `fabric-chaincode-shim` to create chaincode application.
- `thallo-starter-fabric-gateway`: `spring-boot` and `fabric-gateway-java` to build client (gateway) application.

### Chaincode

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.devocative.thallo/thallo-starter-fabric-chaincode/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.devocative.thallo/thallo-starter-fabric-chaincode)

Add following dependency to your Spring Boot's `pom.xml` file:

```xml

<dependency>
	<groupId>org.devocative.thallo</groupId>
	<artifactId>thallo-starter-fabric-chaincode</artifactId>
	<version>1.0</version>
</dependency>
```

You can create a contract like the following code:

```java
@Default
@Contract(name = "sample")
@Component
public class SampleContract implements ContractInterface {
  @Transaction
  public String createAsset(final Context ctx, String name, BigDecimal price) {
    // Logics to create an Asset
    return id;
  }

  @Transaction(intent = EVALUATE)
  public byte[] getAsset(final Context ctx, String id) {
    final ChaincodeStub stub = ctx.getStub();
	  return stub.getState(id);
  }

	// Other functions ...
}
```

Now you can deploy your chaincode in hyperledger fabric network.

### Gateway (client)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.devocative.thallo/thallo-starter-fabric-gateway/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.devocative.thallo/thallo-starter-fabric-gateway)

Add following dependency to your Spring Boot's `pom.xml` file:

```xml

<dependency>
	<groupId>org.devocative.thallo</groupId>
	<artifactId>thallo-starter-fabric-gateway</artifactId>
	<version>1.0</version>
</dependency>
```

Then annotate your spring main class with `@EnableFabricGateway`:

```java

@EnableFabricGateway
@SpringBootApplication
public class SampleGatewayApplication {
  public static void main(String[] args) {
    SpringApplication.run(SampleGatewayApplication.class, args);
  }
}
```

Finally, create an interface such as the following one:

```java

@FabricClient
public interface ISampleContract {

  @Submit
  String createAsset(String name, BigDecimal price);

  @Evaluate
  Asset getAsset(String id);

  @Evaluate(method = "getAsset")
  byte[] getAssetArr(String id);

  @Evaluate(method = "getAsset")
  String getAssetStr(String id);

  // map other functions here
}
```

As you can see, this module is like _OpenFeign_, and you can represent the contract class just with an interface.
The `thallo-starter-fabric-gateway` module implements the interface at runtime, and call all the necessary code to send
your request to fabric network.

Note that the following config in your `application.yml` is necessary for the above code:

```yaml
thallo:
  fabric:
    gateway:
      chaincode: # Chaincode Name
      channel: # Channel Name
      connection-profile-file: # File address to an ORG's connection profile 
      org-msp-id: # The ORG's MSP Id
      ca:
        wallet-dir: # A directory where the Fabric CA's client wallet is created
        server:
          pem-file: # File address to Fabric CA's root certificate in pem format 
          url: # ORG's Fabric CA's URL
          username: # Already created username in ORG's Fabric CA
          password: # The password for the above username
```

## Start Fabric Services in Dev Mode

- Clone this repository
- Download `configtxgen`, `orderer`, and `peer` binary files
  from [Fabric's GitHub](https://github.com/hyperledger/fabric/releases)
  - Create dir `start-fabric-dev-mode/files/bin` and copy mentioned files in it
- Exec `./start-fabric-dev-mode/run.sh start`
- Now start your chaincode spring boot application with following configuration (`application.yml`):

```yaml
thallo:
  fabric:
    chaincode:
      id: "mycc:1.0"
      dev-mode:
        enabled: true
      server-address: "127.0.0.1:9999"
```