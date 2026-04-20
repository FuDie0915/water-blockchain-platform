import request from '@/utils/request'

// 分页查询我的通知列表
export function getNotificationList(params) {
  return request({
    url: '/gk_api/notification/list',
    method: 'get',
    params
  })
}

// 获取未读通知数量
export function getUnreadCount() {
  return request({
    url: '/gk_api/notification/unreadCount',
    method: 'get'
  })
}

// 标记单条通知已读
export function markNotificationRead(id) {
  return request({
    url: '/gk_api/notification/read/' + id,
    method: 'post'
  })
}

// 全部标记已读
export function markAllRead() {
  return request({
    url: '/gk_api/notification/readAll',
    method: 'post'
  })
}

// 删除通知
export function deleteNotification(id) {
  return request({
    url: '/gk_api/notification/delete/' + id,
    method: 'post'
  })
}
