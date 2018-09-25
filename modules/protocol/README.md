# Client Interfaces
Leverages access to the DLT node and its hosted EVM.

#### Protocol ABI
At the protocol level we are trying to be in compliance as much as possible with the [Ethereum JSON-RPC](https://github.com/ethereum/wiki/wiki/JSON-RPC)
specification, resp. [Web3](https://github.com/ethereum/wiki/wiki/JavaScript-API#web3js-api-reference). On the wire protocol
however we need of course be in compliance with what ever Burrow exposes; see the [Burrow JSON-RPC](https://github.com/monax/burrow/blob/master/docs/specs/api.md)
specification for details.

Regardless of the implementation, a clean approach for specifying the interface between the EVM and IS is by means of Protocol
Buffers. See file [types.proto](./src/main/proto/types.proto) for the current implementation status.