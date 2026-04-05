import request from '@/utils/request'  // 确保已配置axios请求实例

// 用户登录
export function login(data) {
  return request({
    url: '/gk_api/user/loginOrRegister',
    method: 'post',
    data: {
      userAccount: data.userAccount,
      userName: ""
    }
  })
}


// 获取用户信息
export function getUserInfo(token) {
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
