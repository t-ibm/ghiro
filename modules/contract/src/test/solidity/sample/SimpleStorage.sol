pragma solidity ^0.4.16;

import "./util/Console.sol";

/**
 * @title A contract with one state variable for permanently storing exactly one value in contract storage.
 */
contract SimpleStorage is Console {
    uint private storedData;
    /**
     * @notice A newly deployed contract of this type will result in contract storage being set to value 5.
     */
    function SimpleStorage() public {
        storedData = 5;
    }
    /**
     * @param p An arbitrary unsigned integer value to be persisted to the contract storage.
     */
    function set(uint p) public {
        storedData = p;
        log(storedData);
    }
    /**
     * @return r The unsigned integer value from the contract storage.
     */
    function get() public view returns (uint r) {
        r = storedData;
    }
}