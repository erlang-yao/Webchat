import { getCookie } from '@/utils/token'

/**
 * 聊天记录导出 API 模块
 * 使用 fetch 而非 axios，因为需要接收 blob 二进制响应
 */
const API_BASE = '/api'

export default {
  /**
   * 导出聊天记录为 ZIP 文件下载
   * @param {Object} data - { roomId, conversationType, conversationName, startDate?, endDate?, types? }
   */
  async exportChatHistory(data) {
    const token = getCookie()
    const response = await fetch(`${API_BASE}/chatExport/export`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': token } : {})
      },
      body: JSON.stringify(data)
    })

    if (!response.ok) {
      // 尝试从 JSON 错误响应中读取错误信息
      let errorMsg = '导出失败'
      try {
        const errorData = await response.json()
        errorMsg = errorData.message || errorMsg
      } catch (e) {
        // 如果无法解析 JSON，使用 HTTP 状态文本
        errorMsg = `导出失败 (${response.status})`
      }
      throw new Error(errorMsg)
    }

    // 从 Content-Disposition 头部提取文件名
    const disposition = response.headers.get('Content-Disposition')
    let filename = 'chat_records.zip'
    if (disposition) {
      const match = disposition.match(/filename\*?=(?:UTF-8'')?"?([^";]+)"?/)
      if (match) {
        filename = decodeURIComponent(match[1])
      }
    }

    // 返回 blob 和文件名
    const blob = await response.blob()
    return { blob, filename }
  }
}
