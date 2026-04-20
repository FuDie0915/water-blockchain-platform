import request from '@/utils/request'

// 新增苗种投放
export function createSeed(data) {
  return request({
    url: '/gk_api/seed/create',
    method: 'post',
    data
  })
}

// 苗种投放记录列表
export function getSeedList(params) {
  return request({
    url: '/gk_api/seed/list',
    method: 'get',
    params
  })
}

// 监管局查看苗种投放记录
export function getManagerSeedList(params) {
  return request({
    url: '/gk_api/seed/manager/list',
    method: 'get',
    params
  })
}

// 监管局审核苗种投放记录
export function managerAuditSeed(data) {
  return request({
    url: '/gk_api/seed/manager/audit',
    method: 'post',
    data
  })
}

// 监管局批量审核苗种投放记录
export function managerBatchAuditSeed(data) {
  return request({
    url: '/gk_api/seed/manager/batchAudit',
    method: 'post',
    data
  })
}

// 修改苗种投放记录
export function updateSeed(data) {
  return request({
    url: '/gk_api/seed/update',
    method: 'post',
    data
  })
}

// 删除苗种投放记录
export function deleteSeed(id) {
  return request({
    url: '/gk_api/seed/delete',
    method: 'post',
    params: { id }
  })
}

// 导出苗种投放记录Excel
export function exportSeedList(params) {
  return request({
    url: '/gk_api/seed/manager/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 监管局上链苗种记录
export function managerUpChainSeed(data) {
  return request({
    url: '/gk_api/seed/manager/upChain',
    method: 'post',
    data
  })
}
