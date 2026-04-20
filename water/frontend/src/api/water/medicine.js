import request from '@/utils/request'

// 新增用药记录
export function createMedicine(data) {
  return request({
    url: '/gk_api/medicine/create',
    method: 'post',
    data
  })
}

// 用药记录列表
export function getMedicineList(params) {
  return request({
    url: '/gk_api/medicine/list',
    method: 'get',
    params
  })
}

// 监管局查看用药记录
export function getManagerMedicineList(params) {
  return request({
    url: '/gk_api/medicine/manager/list',
    method: 'get',
    params
  })
}

// 监管局审核用药记录
export function managerAuditMedicine(data) {
  return request({
    url: '/gk_api/medicine/manager/audit',
    method: 'post',
    data
  })
}

// 监管局批量审核用药记录
export function managerBatchAuditMedicine(data) {
  return request({
    url: '/gk_api/medicine/manager/batchAudit',
    method: 'post',
    data
  })
}

// 修改用药记录
export function updateMedicine(data) {
  return request({
    url: '/gk_api/medicine/update',
    method: 'post',
    data
  })
}

// 删除用药记录
export function deleteMedicine(id) {
  return request({
    url: '/gk_api/medicine/delete',
    method: 'post',
    params: { id }
  })
}

// 导出用药记录Excel
export function exportMedicineList(params) {
  return request({
    url: '/gk_api/medicine/manager/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}

// 监管局上链用药记录
export function managerUpChainMedicine(data) {
  return request({
    url: '/gk_api/medicine/manager/upChain',
    method: 'post',
    data
  })
}
