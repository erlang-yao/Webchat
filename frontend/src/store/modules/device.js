/**
 * 设备信息查询
 */
import {getDeviceType, findParentNode} from '@/utils'

const deviceMap = {
  Desktop: 'Desktop',
  Mobile: 'Mobile'
}

const _registerEvent = () => {
  document.addEventListener('click', (e) => {
    // conversation-list
    const conversationList = findParentNode(e.target, 'conversation-list')
    // conversationitem__cmp active
    const conversationItem = findParentNode(e.target, 'conversationitem__cmp')
    /**同时会话进入聊天区域 */
    if (conversationItem && conversationList) {
      state.currentUI = UIMap.chatArea
    }
  })
}

const _initCSS = () => {
  const style = document.createElement('style')
  style.setAttribute('device', 'mobile')
  style.innerText = `.el-message-box {width: auto} !important;`
  const head = document.querySelector('head')
  head.appendChild(style)
}

/**
 * 针对移动端设备下，【会话列表】和【聊天区域】等...不可以同时出现的情况
 */
const UIMap = {
  conversation: 'conversation',
  chatArea: 'chatArea'
}

const state = {
  deviceType: deviceMap[getDeviceType()],
  currentUI: UIMap.conversation,
  /** 桌面端左侧导航激活项: 'messages' | 'contacts' */
  asideActive: 'messages',
  /** 暗黑模式 */
  darkMode: false
}

if (state.deviceType === deviceMap.Mobile) {
  _registerEvent()
  _initCSS()
}

const mutations = {
  setCurrentUI(state, data) {
    state.currentUI = data
  },
  setAsideActive(state, data) {
    state.asideActive = data
  },
  toggleDarkMode(state) {
    state.darkMode = !state.darkMode
    if (state.darkMode) {
      document.documentElement.setAttribute('data-theme', 'dark')
    } else {
      document.documentElement.removeAttribute('data-theme')
    }
  }
}

const actions = {
  SET_CURRENT_UI({commit}, data) {
    commit('setCurrentUI', data)
  },
  SET_ASIDE_ACTIVE({commit}, data) {
    commit('setAsideActive', data)
  },
  TOGGLE_DARK_MODE({commit}) {
    commit('toggleDarkMode')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
