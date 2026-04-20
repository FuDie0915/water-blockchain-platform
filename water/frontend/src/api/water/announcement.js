import request from '@/utils/request'

// 管理员发布公告
export function adminPublishAnnouncement(data) {
  return request({
    url: '/gk_api/announcement/admin/publish',
    method: 'post',
    data
  })
}

// 管理员查看所有公告
export function getAdminAnnouncementList(params) {
  return request({
    url: '/gk_api/announcement/admin/list',
    method: 'get',
    params
  })
}

// 管理员删除公告
export function adminDeleteAnnouncement(id) {
  return request({
    url: '/gk_api/announcement/admin/delete/' + id,
    method: 'post'
  })
}

// 用户根据角色查看公告
export function getAnnouncementList(params) {
  return request({
    url: '/gk_api/announcement/list',
    method: 'get',
    params
  })
}

// 管理员上链系统公告
export function adminUpChainAnnouncement(id) {
  return request({
    url: '/gk_api/announcement/admin/upChain',
    method: 'post',
    params: { id }
  })
}
