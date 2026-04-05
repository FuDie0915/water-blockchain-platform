import request from '@/utils/request'  // 确保已配置axios请求实例

// 上传文件
export function uploadCommonFile(file) {
  return request({
    url: '/gk_api/common/upload',
    method: 'post',
    headers: {
      'Content-Type': 'multipart/form-data'  // 设置正确的 Content-Type
    },
    data: file
  })
}

// 绑定智能合约
export function bindContract(data) {
  return request({
    url: '/gk_api/user_contract/create',
    method: 'post',
    data: data
  })
}

// 区块链看板
export function getBlockchainBoard() {
  return request({
    url: '/gk_api/common/indexData',
    method: 'get'
  })
}

export function getUavOnChainList() {
  return request({
    url: '/gk_api/aircraft/issues/index/query',
    method: 'get'
  })
}
