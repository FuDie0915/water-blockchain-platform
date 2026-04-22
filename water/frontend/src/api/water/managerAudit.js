import request from '@/utils/request'

// 监管局提交审核申请
export function submitManagerAudit(data) {
  return request({
    url: '/gk_api/manager-audit/submit',
    method: 'post',
    data
  })
}

// 监管局查看审核状态
export function getManagerAuditStatus() {
  return request({
    url: '/gk_api/manager-audit/status',
    method: 'get'
  })
}

// 管理员查看审核列表
export function getAdminManagerAuditList(params) {
  return request({
    url: '/gk_api/manager-audit/admin/list',
    method: 'get',
    params
  })
}

// 管理员批准审核
export function adminApproveManagerAudit(data) {
  return request({
    url: '/gk_api/manager-audit/admin/approve',
    method: 'post',
    data
  })
}

// 管理员拒绝审核
export function adminRejectManagerAudit(data) {
  return request({
    url: '/gk_api/manager-audit/admin/reject',
    method: 'post',
    data
  })
}

// 管理员上链监管局审批记录
export function adminUpChainManagerAudit(id) {
  return request({
    url: '/gk_api/manager-audit/admin/upChain',
    method: 'post',
    params: { id }
  })
}
