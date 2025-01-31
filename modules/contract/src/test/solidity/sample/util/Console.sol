pragma solidity ^0.5.0;

/**
 * @title A contract providing convenient interfaces with the EVM logging facilities.
 */
contract Console {
    /**
     * @notice Triggering logging of this contract's address.
     */
    function log() public {
        emit LogAddress(address(this));
    }
    event LogAddress(address contractAddress);
    /**
     * @notice Triggering logging of an arbitrary unsigned integer value.
     */
    function log(uint x) public {
        emit LogUint(uintToBytes(x));
    }
    function uintToBytes(uint v) public pure returns (bytes32 ret) {
        if (v == 0) {
            ret = '0';
        }
        else {
            while (v > 0) {
                ret = bytes32(uint(ret) / (2 ** 8));
                ret |= bytes32(((v % 10) + 48) * 2 ** (8 * 31));
                v /= 10;
            }
        }
        return ret;
    }
    event LogUint(bytes32 ret);
}