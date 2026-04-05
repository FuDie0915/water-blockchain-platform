pragma solidity ^0.5.2; // 指定编译器版本，^表示兼容0.5.2及以上但小于0.6.0的版本
pragma experimental ABIEncoderV2;

// 合约业务逻辑解释：
// 本合约`Water`用于管理水质数据和企业排污许可证。它允许用户插入水质数据，并且只有监管局可以上传企业排污许可证。
// 合约中定义了两种数据结构：水质数据和企业排污许可证，分别用于存储与用户相关的环境监测数据和企业资质信息。
// 合约通过事件记录关键操作，以便外部系统可以监听和响应这些操作。


// 合约声明
contract Water {
    // 状态变量声明
    address public company; // 公共状态变量，存储企业地址
    address public manager; // 公共状态变量，存储监管局地址

    // 结构体定义
    struct WaterData { // 定义水质数据结构体
        uint256 id;       // 唯一标识符
        uint256 userId;   // 用户ID
        uint256 dataType; // 数据类型
        string data;      // 数据内容
        string status;    // 状态
        uint256 time;     // 时间戳
    }

    struct CompanyCert { // 定义企业排污许可证结构体
        uint256 id;        // 唯一标识符
        uint256 userId;    // 用户ID
        string imageUrl;   // 排污许可证图片URL
        uint256 createTime; // 创建时间
    }

    // 映射声明
    mapping(uint256 => WaterData[]) public waterDataByUserId; // 映射，键为用户ID，值为水质数据数组
    mapping(uint256 => CompanyCert[]) public companyCertByUserId; // 映射，键为用户ID，值为企业排污许可证数组

    // 事件声明
    event WaterDataInserted(uint256 id, uint256 userId, uint256 dataType); // 水质数据插入事件
    event CompanyCertUploaded(uint256 id, uint256 userId); // 企业排污许可证上传事件

    // 构造函数
    constructor(address companyAddress, address managerAddress) public {
        company = companyAddress; // 初始化企业地址
        manager = managerAddress; // 初始化监管局地址
    }

    // 修饰符定义
    modifier onlyCompany() { // 修饰符，仅允许企业地址执行函数
        require(msg.sender == company, "只有企业可以执行此操作"); // 检查调用者是否为企业地址
        _; // 如果是，继续执行函数
    }

    modifier onlyManager() { // 修饰符，仅允许监管局地址执行函数
        require(msg.sender == manager, "只有监管局可以执行此操作"); // 检查调用者是否为监管局地址
        _; // 如果是，继续执行函数
    }

    // 插入水质数据的函数
    function insertWaterData(
        uint256 id,
        uint256 userId,
        uint256 dataType,
        string memory data,
        string memory status,
        uint256 time
    ) public {
        // 创建一个新的水质数据结构体实例
        WaterData memory newData = WaterData(id, userId, dataType, data, status, time);
        // 将新数据添加到用户对应的水质数据列表中
        waterDataByUserId[userId].push(newData);
        // 触发水质数据插入事件
        emit WaterDataInserted(id, userId, dataType);
    }

    // 根据用户ID和数据类型获取水质数据的函数
    function getWaterDataByUserIdAndType(uint256 userId, uint256 dataType) public view returns (WaterData[] memory) {
        // 获取指定用户的所有水质数据
        WaterData[] memory allData = waterDataByUserId[userId];
        // 创建一个数组来存储匹配的数据类型
        WaterData[] memory result = new WaterData[](allData.length);
        uint256 count = 0;
        // 遍历所有数据，查找匹配的数据类型
        for (uint256 i = 0; i < allData.length; i++) {
            if (allData[i].dataType == dataType) {
                result[count] = allData[i];
                count++;
            }
        }
        // 创建一个新的数组，仅包含匹配的数据类型
        WaterData[] memory finalResult = new WaterData[](count);
        for (uint256 j = 0; j < count; j++) {
            finalResult[j] = result[j];
        }
        // 返回匹配的数据类型数组
        return finalResult;
    }

    // 上传企业排污许可证的函数，只有监管局可以调用此函数
    function uploadCompanyCert(
        uint256 id,
        uint256 userId,
        string memory imageUrl,
        uint256 createTime
    ) public onlyManager {
        // 创建一个新的企业排污许可证结构体实例
        CompanyCert memory newCert = CompanyCert(id, userId, imageUrl, createTime);
        // 将新排污许可证添加到用户对应的企业排污许可证列表中
        companyCertByUserId[userId].push(newCert);
        // 触发企业排污许可证上传事件
        emit CompanyCertUploaded(id, userId);
    }

    // 根据用户ID获取企业排污许可证的函数
    function getCompanyCertByUserId(uint256 userId) public view returns (CompanyCert[] memory) {
        // 返回指定用户的所有企业排污许可证
        return companyCertByUserId[userId];
    }
}
