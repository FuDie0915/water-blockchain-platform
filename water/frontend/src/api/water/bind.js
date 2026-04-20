import request from '@/utils/request'

// 获取所有已注册监管局列表
export function getManagerList() {
  return request({
    url: '/gk_api/bind/managers',
    method: 'get'
  })
}

// 养殖户申请绑定监管局
export function applyBind(data) {
  return request({
    url: '/gk_api/bind/apply',
    method: 'post',
    data
  })
}

// 养殖户查看绑定状态
export function getBindStatus() {
  return request({
    url: '/gk_api/bind/status',
    method: 'get'
  })
}

// 监管局查看所有绑定申请
export function getManagerBindList(params) {
  return request({
    url: '/gk_api/bind/manager/list',
    method: 'get',
    params
  })
}

// 监管局同意绑定
export function managerApproveBind(id) {
  return request({
    url: '/gk_api/bind/manager/approve',
    method: 'post',
    params: { id }
  })
}

// 监管局拒绝绑定
export function managerRejectBind(id) {
  return request({
    url: '/gk_api/bind/manager/reject',
    method: 'post',
    params: { id }
  })
}

// 管理员查看所有绑定关系
export function getAdminBindList(params) {
  return request({
    url: '/gk_api/bind/admin/list',
    method: 'get',
    params
  })
}

// 养殖户主动解绑
export function unbind() {
  return request({
    url: '/gk_api/bind/unbind',
    method: 'post'
  })
}

// 监管局主动解除绑定
export function managerUnbind(id) {
  return request({
    url: '/gk_api/bind/manager/unbind',
    method: 'post',
    params: { id }
  })
}

// 监管局上链绑定记录
export function managerUpChainBind(id) {
  return request({
    url: '/gk_api/bind/manager/upChain',
    method: 'post',
    params: { id }
  })
}
