import request from '@/utils/request';

// 用户登录（多角色入口）
export function login(data) {
  const account = (data.userAccount || '').trim().toLowerCase();
  if (account === 'admin') {
    return request({
      url: '/gk_api/user/loginOrRegister',
      method: 'post',
      data: {
        userAccount: data.userAccount,
        userPassword: data.userPassword,
        userName: 'platform',
        userRole: 'admin',
      },
    });
  }

  const roleType = data.roleType || 'company';
  const loginType = roleType === 'manager' ? 1 : 0;

  return request({
    url: '/gk_api/water/user/login',
    method: 'post',
    data: {
      userAccount: data.userAccount,
      userPassword: data.userPassword,
      loginType,
    },
  });
}

// 用户注册（仅养殖户 / 监管端）
export function register(data) {
  return request({
    url: '/gk_api/user/register',
    method: 'post',
    data: {
      userAccount: data.userAccount,
      userPassword: data.userPassword,
      userName: data.userName || '',
      userRole: data.roleType,
    },
  });
}

// 获取用户信息
export function getUserInfo(token) {
  return request({
    url: '/gk_api/user/getLoginInfoByToken/' + token,
    method: 'get',
  });
}

// 退出登录
export function logout(token) {
  return request({
    url: '/gk_api/user/logout/' + token,
    method: 'get',
  });
}
