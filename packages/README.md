# Application Layer
User interaction with the DLT layer is through two IS packages.

#### Support for Distributed Applications
IS package `WmDApp` contains administrative services as well as the runtime services mapping the contract to the DLT layer.

###### Configuration
There is exactly one configuration file [`packages/WmDApp/resources/Node.yaml`](../modules/core/src/testFixture/resources/Node.yaml) controlling all aspects of a DLT node.
Declaring a named node is done via package property `node` as persisted in file `packages/WmDApp/manifest.v3`.

###### Administrative Services
|Service|Description|
|-------|-----------|
|`wm.dapp.Admin:syncContracts`|Synchronizes the contracts to the IS namespace.|
|`wm.dapp.Admin:loadContractAddresses`|Retrieves the contract-address mappings as persisted in file `${config.location}/contract-addresses.json`.|
|`wm.dapp.Admin:deployContract`|Deploys the contract to the distributed ledger and stores the contract-address mappings to file `${config.location}/contract-addresses.json` if successful.|

###### Runtime Services
|Service|Description|
|-------|-----------|
|`wm.dapp.Contract:call`|Calls the contract. To be used by stateless EVM contracts.|
|`wm.dapp.Contract:sendTransaction`|Sends a transaction. To be used by stateful EVM contracts.|

#### The actual Distributed Applications
IS package `WmDAppContract` contains all generated smart contract services.