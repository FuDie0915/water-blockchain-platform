import request from '@/utils/request'

// 新增投喂记录
export function createFeed(data) {
  return request({
    url: '/gk_api/feed/create',
    method: 'post',
    data
  })
}

// 投喂记录列表
export function getFeedList(params) {
  return request({
    url: '/gk_api/feed/list',
    method: 'get',
    params
  })
}

// 监管局查看投喂记录
export function getManagerFeedList(params) {
  return request({
    url: '/gk_api/feed/manager/list',
    method: 'get',
    params
  })
}

// 监管局审核投喂记录
export function managerAuditFeed(data) {
  return request({
    url: '/gk_api/feed/manager/audit',
    method: 'post',
    data
  })
}

// 监管局批量审核投喂记录
export function managerBatchAuditFeed(data) {
  return request({
    url: '/gk_api/feed/manager/batchAudit',
    method: 'post',
    data
  })
}

// 修改投喂记录
export function updateFeed(data) {
  return request({
    url: '/gk_api/feed/update',
    method: 'post',
    data
  })
}

// 删除投喂记录
export function deleteFeed(id) {
  return request({
    url: '/gk_api/feed/delete',
    method: 'post',
    params: { id }
  })
}

// 导出投喂记录Excel
export function exportFeedList(params) {
  return request({
    url: '/gk_api/feed/manager/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 监管局上链投喂记录
export function managerUpChainFeed(data) {
  return request({
    url: '/gk_api/feed/manager/upChain',
    method: 'post',
    data
  })
}
