import request from '@/utils/request'

// 获取图形验证码
export function getCaptcha() {
  return request({
    url: '/gk_api/user/captcha',
    method: 'get'
  })
}

// 养殖户注册
export function registerFarmer(data) {
  return request({
    url: '/gk_api/user/register/farmers',
    method: 'post',
    data
  })
}

// 监管局注册
export function registerManager(data) {
  return request({
    url: '/gk_api/user/register/manager',
    method: 'post',
    data
  })
}

// 管理员登录
export function loginAdmin(data) {
  return request({
    url: '/gk_api/user/login/admin',
    method: 'post',
    data
  })
}

// 养殖户登录
export function loginFarmer(data) {
  return request({
    url: '/gk_api/user/login/farmers',
    method: 'post',
    data
  })
}

// 监管局登录
export function loginManager(data) {
  return request({
    url: '/gk_api/user/login/manager',
    method: 'post',
    data
  })
}

// 修改个人信息
export function updateProfile(data) {
  return request({
    url: '/gk_api/user/updateProfile',
    method: 'post',
    data
  })
}

// 管理员查看所有用户列表
export function getAdminUserList(params) {
  return request({
    url: '/gk_api/user/admin/list',
    method: 'get',
    params
  })
}

// 管理员修改用户状态
export function updateUserStatus(data) {
  return request({
    url: '/gk_api/user/admin/updateStatus',
    method: 'post',
    data: {
      userId: data.userId || data.id,
      userStatus: data.userStatus || data.status,
    }
  })
}

// 管理员修改用户信息
export function adminUpdateUser(data) {
  return request({
    url: '/gk_api/user/admin/updateUser',
    method: 'post',
    data
  })
}

// 管理员创建用户
export function adminCreateUser(data) {
  return request({
    url: '/gk_api/user/admin/createUser',
    method: 'post',
    data
  })
}

// 管理员删除用户
export function adminDeleteUser(userId) {
  return request({
    url: '/gk_api/user/admin/deleteUser',
    method: 'post',
    params: { userId }
  })
}

// 批量修改用户状态
export function batchUpdateStatus(data) {
  return request({
    url: '/gk_api/user/admin/batchUpdateStatus',
    method: 'post',
    data
  })
}

// 批量删除用户
export function batchDeleteUsers(data) {
  return request({
    url: '/gk_api/user/admin/batchDelete',
    method: 'post',
    data
  })
}

// 根据token获取登录用户信息
export function getLoginInfoByToken(token) {
  return request({
    url: '/gk_api/user/getLoginInfoByToken/' + token,
    method: 'get'
  })
}

// 退出登录
export function logout(token) {
  return request({
    url: '/gk_api/user/logout/' + token,
    method: 'get'
  })
}

// 导出用户列表Excel
export function exportUserList(params) {
  return request({
    url: '/gk_api/user/admin/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
