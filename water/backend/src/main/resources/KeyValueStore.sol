pragma solidity ^0.5.2;

/**
 * @title 通用键值存储合约
 * @dev 后端生成唯一 key，调用 bindString 上链 JSON 数据
 */
contract KeyValueStore {
    address public owner;

    mapping(bytes32 => string) public keyToString;

    event StringBinded(
        address indexed binder,
        bytes32 indexed key,
        string content,
        uint256 bindTime
    );

    constructor() public {
        owner = msg.sender;
    }

    function bindString(bytes32 _key, string calldata _str) external {
        require(bytes(_str).length > 0, "String cannot be empty");

        keyToString[_key] = _str;

        emit StringBinded(msg.sender, _key, _str, block.timestamp);
    }

    function getStringByKey(bytes32 _key) external view returns (string memory) {
        return keyToString[_key];
    }
}
