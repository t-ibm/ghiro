pragma solidity ^0.4.0;

contract Console {
    event LogAddress(address);
    function log() {
        LogAddress(address(this));
    }
    event LogUint(bytes32);
    function log(uint x) {
        LogUint(uintToBytes(x));
    }
    function uintToBytes(uint v) constant returns (bytes32 ret) {
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
}