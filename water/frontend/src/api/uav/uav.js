import request from '@/utils/request'  // 确保已配置axios请求实例

// 创建无人机
export function createUav() {
  return request({
    url: '/gk_api/aircraft/create',
    method: 'post'
  })
}

// 无人机信息查询
export function getUavInfo() {
  return request({
    url: '/gk_api/aircraft/list',
    method: 'get'
  })
}

// 步骤一信息
export function getStepOneInfo() {
  return request({
    url: '/gk_api/aircraft/step1Info',
    method: 'post'
  })
}

// 光伏板问题生成
export function createSolarPanelProblem(data) {
  return request({
    url: '/gk_api/aircraft/issues/gen',
    method: 'post',
    data: data
  })
}

// 光伏板问题详情查询
export function getSolarPanelProblem(data) {
  return request({
    url: '/gk_api/aircraft/issues/query',
    method: 'get',
    params: {
      location: data
    }
  })
}

// 光伏板巡检过区域查询
export function getSolarPanelInspectionArea() {
  return request({
    url: '/gk_api/aircraft/location/list',
    method: 'get'
  })
}



// 光伏板问题上链
export function upChainProblem(data) {
  return request({
    url: '/gk_api/aircraft/issues/upChain',
    method: 'post',
    data: data
  })
}

