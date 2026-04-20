import request from '@/utils/request'

// 新增收获记录
export function createHarvest(data) {
  return request({
    url: '/gk_api/harvest/create',
    method: 'post',
    data
  })
}

// 收获记录列表
export function getHarvestList(params) {
  return request({
    url: '/gk_api/harvest/list',
    method: 'get',
    params
  })
}

// 监管局查看收获记录
export function getManagerHarvestList(params) {
  return request({
    url: '/gk_api/harvest/manager/list',
    method: 'get',
    params
  })
}

// 监管局审核收获记录
export function managerAuditHarvest(data) {
  return request({
    url: '/gk_api/harvest/manager/audit',
    method: 'post',
    data
  })
}

// 监管局批量审核收获记录
export function managerBatchAuditHarvest(data) {
  return request({
    url: '/gk_api/harvest/manager/batchAudit',
    method: 'post',
    data
  })
}

// 修改收获记录
export function updateHarvest(data) {
  return request({
    url: '/gk_api/harvest/update',
    method: 'post',
    data
  })
}

// 删除收获记录
export function deleteHarvest(id) {
  return request({
    url: '/gk_api/harvest/delete',
    method: 'post',
    params: { id }
  })
}

// 导出收获记录Excel
export function exportHarvestList(params) {
  return request({
    url: '/gk_api/harvest/manager/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 监管局上链收获记录
export function managerUpChainHarvest(data) {
  return request({
    url: '/gk_api/harvest/manager/upChain',
    method: 'post',
    data
  })
}
