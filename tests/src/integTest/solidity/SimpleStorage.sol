pragma solidity ^0.4.0;

import "Console.sol";

contract SimpleStorage is Console {
    uint public storedData;

    function SimpleStorage() {
        storedData = 5;
    }

    function set(uint x) {
        storedData = x;
        log(storedData);
    }

    function get() constant returns (uint retVal) {
        log(storedData);
        return storedData;
    }
}