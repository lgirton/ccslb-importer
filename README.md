# California Contractor State License Board Import Application
## Overview
This application is retrieves License Contractor information through a REST API at 
[CCSLB Contractor Listing](https://www.cslb.ca.gov/OnlineServices/DataPortal/DownLoadFile.ashx?fName=MasterLicenseData&type=C)
and loads the information into a MongoDB instance.  At the time of writing this, there were approx. 290,000 licensed
contractors in the state of California.  This is intended to be run as a CRON Batch Job that is infrequently 
run to cache information that would be used another service that would expose a REST API to search for California 
contractor information.

Below are the semantic stages of the processing flow:

* Initialize
* Extract
* Transform
* Load
* Cleanup

And corresponding diagram:

![alt text](Pipeline.png)

This project uses Quarkus, the Supersonic Subatomic Java Framework and Camel Quarkus extensions.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `ccslb-importer-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/ccslb-importer-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/ccslb-importer-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
