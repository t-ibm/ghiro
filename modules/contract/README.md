# Smart Contracts
Provides smart contract language support.

#### Solidity Compile
Supporting build-time and/or run-time compilation of Solidity smart contracts.

The Solidity compiler allows to produce different output formats for a given source, most noticeable the following:
* Abstract Syntax Tree (AST) of all source files.
* Binary of the contracts in hex.
* Solidity interface of the contracts.
* Application Binary Interface (ABI) specification of the contracts.
* Natural Specification (Natspec) documentation of all contracts.

We are mostly interested in the binary representation and the ABI specification in order to deploy the contract and call
its functions.

###### Build-Time Approach
With this approach the Solidity sources are compiled during build-time and then the BIN and ABI results are picked up by
the run-time.
````
$ ./gradlew :modules:ghiro-contract:solc
````
###### ~~Run-Time Approach~~
With this approach the Solidity sources are directly picked up by the run-time and then the needed BIN and ABI results
are created on the fly.

#### Contract ABI
Providing a type-system for smart contracts.

A contract interface essentially declares a set of specifications that allow to interact with the contract. Those
specifications are of type `constructor`, `function`, or `event`. Beyond other properties, a function specification is composed
of `inputParameters` and `outputParameters` whereas constructor and event specifications are composed of `inputParameters` only.
For the complete set of declared properties see the [Ethereum Contract ABI specification](https://github.com/ethereum/wiki/blob/master/Ethereum-Contract-ABI.md).

For a Java application to interact with the contract, aka Ethereum Distributed Application (DApp), a type-system mapping
is needed. See file [ParameterTypeJava.java](./src/main/java/com/softwareag/tom/contract/abi/util/ParameterTypeJava.java) for the current implementation status.