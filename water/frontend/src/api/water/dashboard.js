import request from '@/utils/request'

// 管理员看板
export function getAdminDashboard() {
  return request({
    url: '/gk_api/dashboard/admin',
    method: 'get'
  })
}

// 监管局看板
export function getManagerDashboard() {
  return request({
    url: '/gk_api/dashboard/manager',
    method: 'get'
  })
}

// 养殖户看板
export function getFarmerDashboard() {
  return request({
    url: '/gk_api/dashboard/farmer',
    method: 'get'
  })
}
