# Client Interfaces
Leverages access to the DLT node and its hosted EVM.

#### Protocol ABI
At the protocol level we are trying to be in compliance as much as possible with the [Ethereum JSON-RPC](https://github.com/ethereum/wiki/wiki/JSON-RPC)
specification, resp. [Web3](https://github.com/ethereum/wiki/wiki/JavaScript-API#web3js-api-reference). On the wire however
we need of course be compatible with whatever Burrow exposes, which is only a subset of Web3 and a proprietary [gRPC API](https://hyperledger.github.io/burrow/#/js-api?id=api-reference);
note that there is some overlap between the two.

Regardless of the implementation, a clean approach for specifying the interface between the EVM and IS is by means of Protocol
Buffers. See file [types.proto](./src/main/proto/types.proto) for the current implementation status.