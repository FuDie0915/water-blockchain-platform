import request from '@/utils/request'

// 新增养殖池
export function createPond(data) {
  return request({
    url: '/gk_api/pond/create',
    method: 'post',
    data
  })
}

// 获取当前用户养殖池列表
export function getPondList() {
  return request({
    url: '/gk_api/pond/list',
    method: 'get'
  })
}

// 更新养殖池信息
export function updatePond(data) {
  return request({
    url: '/gk_api/pond/update',
    method: 'post',
    data
  })
}

// 更新养殖池状态
export function updatePondStatus(data) {
  return request({
    url: '/gk_api/pond/update/status',
    method: 'post',
    data
  })
}

// 删除养殖池
export function deletePond(id) {
  return request({
    url: '/gk_api/pond/delete',
    method: 'post',
    params: { id }
  })
}

// 监管局查看管辖养殖户的养殖池
export function getManagerPondList(params) {
  return request({
    url: '/gk_api/pond/manager/list',
    method: 'get',
    params
  })
}

// 监管局审核养殖池
export function managerAuditPond(data) {
  return request({
    url: '/gk_api/pond/manager/audit',
    method: 'post',
    data
  })
}

// 监管局批量审核养殖池
export function managerBatchAuditPond(data) {
  return request({
    url: '/gk_api/pond/manager/batchAudit',
    method: 'post',
    data
  })
}

// 监管局导出养殖池Excel
export function exportPondList(params) {
  return request({
    url: '/gk_api/pond/manager/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 监管局上链养殖池
export function managerUpChainPond(data) {
  return request({
    url: '/gk_api/pond/manager/upChain',
    method: 'post',
    data
  })
}
