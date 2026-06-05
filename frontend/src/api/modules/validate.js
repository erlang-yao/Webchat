import request from '@/utils/request'

export default {
  getMyValidateMessageList(userId) {
    return request.get(`/api/validate/getMyValidateMessageList?userId=${userId}`)
  },
  getValidateMessage(data) {
    const {roomId, status, validateType} = data
    return request.get(`/api/validate/getValidateMessage?roomId=${roomId}&status=${status}&validateType=${validateType}`)
  },
  
  // ▼▼▼ 新增下面这个方法 ▼▼▼
  resendValidateMessage(data) {
    // 这里的路径对应你后端 ValidateMessageController 中的 /validate/resend
    return request.post('/api/validate/resend', data)
  }
}

