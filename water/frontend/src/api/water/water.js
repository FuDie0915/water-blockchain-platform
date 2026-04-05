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

// 监管局审批许可
export function Login(data) {
  return request({
    url: '/gk_api/water/user/login',
    method: 'post',
    data: {
      userAccount: data.userAccount,
      userPassword: data.userPassword,
      loginType: data.loginType,
    }
  })
}