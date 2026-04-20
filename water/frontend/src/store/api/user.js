import request from '@/utils/request';

// 用户登录（多角色入口）
export function login(data) {
  const roleType = data.roleType || 'company';

  // 根据角色选择不同的登录接口
  if (roleType === 'admin' || data.userAccount === 'admin') {
    return request({
      url: '/gk_api/user/login/admin',
      method: 'post',
      data: {
        userAccount: data.userAccount,
        userPassword: data.userPassword,
      },
    });
  }

  if (roleType === 'manager') {
    return request({
      url: '/gk_api/user/login/manager',
      method: 'post',
      data: {
        userAccount: data.userAccount,
        userPassword: data.userPassword,
      },
    });
  }

  // 默认为养殖户登录
  return request({
    url: '/gk_api/user/login/farmers',
    method: 'post',
    data: {
      userAccount: data.userAccount,
      userPassword: data.userPassword,
    },
  });
}

// 用户注册（仅养殖户 / 监管端）
export function register(data) {
  const roleType = data.roleType || 'company';
  const url = roleType === 'manager' ? '/gk_api/user/register/manager' : '/gk_api/user/register/farmers';

  return request({
    url: url,
    method: 'post',
    data: {
      userAccount: data.userAccount,
      userPassword: data.userPassword,
      userName: data.userName || '',
      captchaKey: data.captchaKey || '',
      captchaCode: data.captchaCode || '',
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
