import request from '@/utils/request'  // 确保已配置axios请求实例

// 水数据生成
export function collectWaterGen(data) {
  return request({
    url: '/gk_api/water/data/gen',
    method: 'post',
    data: {
      dataType: data,
    }
  })
}

// 步骤一数据
export function getStepOneData() {
  return request({
    url: '/gk_api/water/step1Info',
    method: 'post'
  })
}

// 水数据分页查询
export function waterInfoQuery(data) {
  return request({
    url: '/gk_api/water/data/page',
    method: 'get',
    params: {
      DataType: data.DataType,
      pageNum: data.pageNum,
      pageSize: data.pageSize,
    }
  })
}

// 水数据上链
export function upChain(data) {
  return request({
    url: '/gk_api/water/data/upChain',
    method: 'post',
    data: {
      id: data,
    }
  })
}

// 企业提交许可
export function commitPermission(data) {
  return request({
    url: '/gk_api/water/permission/commit',
    method: 'post',
    data: data
  })
}

// 企业查询许可
export function queryPermission() {
  return request({
    url: '/gk_api/water/permission/company/query',
    method: 'get',
  })
}

// 许可证链上比对
export function chainCompare(data) {
  return request({
    url: '/gk_api/water/permission/isOnChain/' + data,
    method: 'post',
  })
}


// 监管局查询许可
export function queryPermissionByManager() {
  return request({
    url: '/gk_api/water/permission/manager/query',
    method: 'get',
  })
}

// 监管局审批许可
export function approvePermissionByManager(data) {
  return request({
    url: '/gk_api/water/permission/verify',
    method: 'post',
    data: {
      id: data.id,
      status: data.status,
    }
  })
}


// 监管局审批许可
export function Logout(data) {
  return request({
    url: '/gk_api/user/logout/' + data,
    method: 'get'
  })
}

// 监管局查询所有许可证
export function queryAllPermissionByManager() {
  return request({
    url: '/gk_api/water/permission/manager/allQuery',
    method: 'get'
  })
}

// 删除许可证（仅被拒绝的）
export function deletePermission(certId) {
  return request({
    url: '/gk_api/water/permission/delete/' + certId,
    method: 'post'
  })
}

// 删除水质数据（仅未上链的）
export function deleteWaterData(dataId) {
  return request({
    url: '/gk_api/water/data/delete/' + dataId,
    method: 'post'
  })
}

// 水数据采集
export function collectWaterData(data) {
  return request({
    url: '/gk_api/water/data/collect',
    method: 'post',
    data
  })
}

// 养殖户手动上报水质数据
export function manualReportWaterData(data) {
  return request({
    url: '/gk_api/water/data/manual',
    method: 'post',
    data
  })
}

// 新建预警阈值配置
export function createThreshold(data) {
  return request({
    url: '/gk_api/water/threshold/create',
    method: 'post',
    data
  })
}

// 修改预警阈值配置
export function updateThreshold(data) {
  return request({
    url: '/gk_api/water/threshold/update',
    method: 'post',
    data
  })
}

// 获取预警阈值配置
export function getThreshold() {
  return request({
    url: '/gk_api/water/threshold/get',
    method: 'get'
  })
}

// 监管局查看管辖养殖户的水质数据
export function getManagerWaterDataList(params) {
  return request({
    url: '/gk_api/water/data/manager/list',
    method: 'get',
    params
  })
}

// 监管局审核水质数据
export function managerAuditWaterData(data) {
  return request({
    url: '/gk_api/water/data/manager/audit',
    method: 'post',
    data
  })
}

// 监管局批量审核水质数据
export function managerBatchAuditWaterData(data) {
  return request({
    url: '/gk_api/water/data/manager/batchAudit',
    method: 'post',
    data
  })
}

// 监管局导出水质数据Excel
export function exportWaterDataList(params) {
  return request({
    url: '/gk_api/water/data/manager/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}