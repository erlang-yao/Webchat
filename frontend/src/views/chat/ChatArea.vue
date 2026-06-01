<template>
  <div class="chat-area__com">
    <chat-header
      :currentConversation="currentConversation"
      :set-current-conversation="setCurrentConversation"/>
    <transition name="slide-up">
      <div class="history-msg-container" v-if="showHistoryMsg">
        <history-msg :current-conversation="currentConversation"/>
      </div>
    </transition>
    <div :class="currentConversation.conversationType !== 'GROUP' ? 'main no-group' : 'main'">
      <div class="message-list-container">
        <message-list ref='messagelist'
                      @load-message="loadmessage"
                      :messagelist="messagesOutcome"
                      :scrollbottom="scrollBottom"
                      :hasmore="hasMore"
                      :isloading="isLoading"
                      :useanimation="useAnimation"
                      :currentConversation="currentConversation"
                      :last-enter-time="lastEnterTime"
                      :set-last-enter-time="setLastEnterTime"/>
      </div>
      <div class="group-desc" v-if="device !== 'Mobile' && currentConversation.conversationType === 'GROUP'">
        <group-desc :currentConversation="currentConversation" :key="datetamp"/>
      </div>
    </div>
    <div class="message-edit-container">
      <div class="tool">
         <span class="tool-item">
           <i class="item iconfont icon-emoji" @click.stop="showEmojiCom = !showEmojiCom"></i>
         </span>
        <span class="tool-item">
          <label for="upImg">
            <i class="item el-icon-picture">
              <input
                id="upImg"
                class="img-inp"
                type="file"
                title="选择图片"
                accept="image/png, image/jpeg, image/gif, image/jpg"
                @change="uploadImg">
            </i>
          </label>
        </span>
        <span class="tool-item">
          <i class="item el-icon-folder" @click.stop="showUpFileCom = !showUpFileCom"/>
          <transition name="fade">
            <up-file
              :visible="showUpFileCom"
              v-watchMouse="showUpFileCom"
              @handleSuccess="uploadFileSuccess"
              class="upFileComponent"
              @getStatus="getUploadResult"
              @getLocalUrl="getLocalUrl"
              :get-status="getUploadResult"
              :get-local-url="getLocalUrl"/>
          </transition>
        </span>
        <span class="tool-item" :class="{ active: isRecording }" @click.stop="toggleVoiceRecord">
          <i class="item iconfont icon-yuyin" title="语音消息"/>
        </span>
        <!--        <span class="tool-item">-->
        <!--          <i class="item iconfont icon-huaban"/>-->
        <!--        </span>-->
        <!--        <span class="tool-item">-->
        <!--          <i class="item iconfont icon-shipin"/>-->
        <!--        </span>-->
        <!--        <span class="tool-item">-->
        <!--          <i class="item el-icon-phone-outline"/>-->
        <!--        </span>-->
        <span class="tool-item"
              :class="showHistoryMsg ? 'history-btn normal-font el-icon-caret-bottom' : 'history-btn normal-font el-icon-caret-top'"
              @click="setShowHistoryMsg">历史记录
        </span>
        <span class="tool-item export-btn"
              :class="{ 'is-exporting': isExporting }"
              @click="exportChatHistory"
              title="导出聊天记录">
          <i class="item el-icon-download"/>
        </span>
      </div>
      <div class="operation">
        <el-button @click="send" type="success" size="small" round>发送</el-button>
        <el-button @click="clean" type="danger" size="small" round>清空</el-button>
      </div>
      <textarea ref="chatInp" class="textarea" v-model="messageText" maxlength="200" @input="scrollBottom = true"
                @keydown.enter="send($event)"></textarea>
      <transition name="fade">
        <custom-emoji v-if="showEmojiCom" class="emoji-component" @addemoji="addEmoji"/>
      </transition>
      <transition name="fade">
        <div v-if="isRecording" class="voice-record-panel">
          <span class="record-dot"/>
          <span class="record-time">{{ formatRecordTime(recordSeconds) }}</span>
          <span class="record-tip">录音中，再次点击麦克风结束并发送</span>
          <el-button size="mini" @click.stop="cancelVoiceRecord">取消</el-button>
        </div>
      </transition>
    </div>
  </div>
</template>

<script>
  import {mapState} from "vuex"
  import {cloneDeep} from 'lodash'
  import chatHeader from "./components/Header"
  import messageList from "./components/MessageList"
  import {SET_UNREAD_NEWS_TYPE_MAP} from "@/store/constants"
  import {conversationTypes, uploadStatusMap, MSG_TYPES} from '@/const'
  import customEmoji from '@/components/customEmoji'
  import upFile from '@/components/customUploadFile'
  import groupDesc from './components/GroupDesc'
  import historyMsg from './components/HistoryMsg'
  import xss from '@/utils/xss'
  import {genGuid, fromatTime} from '@/utils'

  export default {
    props: {
      currentConversation: Object,
      setLoading: Function,
      setCurrentConversation: Function
    },
    data() {
      return {
        messageText: "",
        messages: [],
        showEmojiCom: false,
        showUpFileCom: false,
        pageIndex: 0,
        pageSize: 15,
        hasMore: true,
        showTopOperation: false,
        scrollBottom: true,
        isLoading: false,
        useAnimation: false,
        lastEnterTime: Date.now(), // 对方进入该会话的时间
        showHistoryMsg: false,
        datetamp: Date.now(), // 切换群聊重新强制加载群聊详情
        isRecording: false,
        recordSeconds: 0,
        recordTimer: null,
        mediaRecorder: null,
        audioChunks: [],
        recordStream: null,
        voiceUploading: false,
        isExporting: false
      }
    },
    computed: {
      ...mapState("user", {
        userInfo: "userInfo"
      }),
      messagesOutcome() {
        // console.log("生成消息列表时的会话信息为:", this.currentConversation)
        // console.log("生成消息列表时的信息为:", this.messages)
        return this.messages.filter(item => {
          return item.roomId === this.currentConversation.roomId
        })
      },
      device() {
        return this.$store.state.device.deviceType
      }
    },
    sockets: {
      receiveMessage(news) {
        // console.log("收到新的消息", news)
        // console.log("原本的消息列表为：", this.messages)
        // console.log("当前会话房间为：", this.currentConversation.roomId)
        this.messages = [...this.messages, news]
        if (news.roomId === this.currentConversation.roomId) {
          // console.log("进来设置该房间未读消息数为0")
          setTimeout(() => {
            this.$store.dispatch("news/SET_UNREAD_NEWS", {
              roomId: news.roomId,
              count: 0,
              type: SET_UNREAD_NEWS_TYPE_MAP.clear
            })
          }, 0)
        }
      },
      conversationList(list) {
      }
    },
    methods: {
      uploadFileSuccess(res, file) { // 上传文件成功，通知父组件关闭展示
        this.showUpFileCom = false
        // console.log("文件上传成功，结果为：", res, file)
        let filePath = res.data.filePath
        let fileType;
        if (file.raw.type.indexOf('image') > -1) {
          fileType = 'img'  //图片类型
        } else {
          fileType = 'file' //文件类型
        }
        const common = this.generatorMessageCommon()
        const newMessage = {
          ...common,
          fileRawName: file.name,
          message: filePath,
          messageType: fileType, // emoji/text/img/file/sys/artboard/audio/video
        }
        this.messages = [...this.messages, newMessage]
        // console.log("上传文件后的最新消息为：", newMessage)
        this.$socket.emit("sendNewMessage", newMessage)
        this.$store.dispatch('news/SET_LAST_NEWS', {
          type: 'edit',
          res: {
            roomId: this.currentConversation.roomId,
            news: newMessage
          }
        })
        this.messageText = ""
      },
      clean() {
        this.messageText = ""
      },
      setShowHistoryMsg() {
        this.showHistoryMsg = !this.showHistoryMsg
      },
      /** 导出当前会话聊天记录 */
      async exportChatHistory() {
        if (this.isExporting) return
        if (!this.currentConversation || !this.currentConversation.roomId) {
          this.$message.warning('请先选择一个会话')
          return
        }
        this.isExporting = true
        const loading = this.$loading({
          lock: true,
          text: '正在导出聊天记录，请稍候...',
          spinner: 'el-icon-loading',
          background: 'rgba(0, 0, 0, 0.6)'
        })
        try {
          // 获取会话名称
          let conversationName = '聊天记录'
          if (this.currentConversation.isGroup) {
            conversationName = (this.currentConversation.groupInfo &&
              this.currentConversation.groupInfo.title) || '群聊'
          } else {
            conversationName = this.currentConversation.nickname || '联系人'
          }
          const params = {
            roomId: this.currentConversation.roomId,
            conversationType: this.currentConversation.conversationType,
            conversationName: conversationName
          }
          const result = await this.$http.exportChatHistory(params)
          // 触发浏览器下载
          const url = window.URL.createObjectURL(result.blob)
          const link = document.createElement('a')
          link.href = url
          link.download = result.filename
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          window.URL.revokeObjectURL(url)
          this.$message.success('聊天记录导出成功！')
        } catch (err) {
          console.error('导出聊天记录失败:', err)
          this.$message.error('导出失败：' + (err.message || '未知错误'))
        } finally {
          this.isExporting = false
          loading.close()
        }
      },
      /**最后进入该会话的时间 */
      setLastEnterTime(time) {
        this.lastEnterTime = time
      },
      generatorMessageCommon() {
        return {
          roomId: this.currentConversation.roomId,
          senderId: this.userInfo.uid,
          senderName: this.userInfo.username,
          senderNickname: this.userInfo.nickname,
          senderAvatar: this.userInfo.photo,
          time: Date.now(),
          isReadUser: [this.userInfo.uid],
          conversationType: this.currentConversation.conversationType,
        }
      },
      getUploadResult(res) {
        const {guid} = res // 图片的唯一标识
        const msgListClone = cloneDeep(this.messages)
        if (res.status === uploadStatusMap.error) {
          this.$message.error('文件上传失败！')
          return
        }
        if (res.status === uploadStatusMap.next) {
          const percent = Number((res.data && res.data.total && res.data.total.percent) || 0).toFixed(2)
          const loaded = (res.data && res.data.total && res.data.total.loaded) || 0
          const size = (res.data && res.data.total && res.data.total.size) || 0
          console.log(`文件大小：${size}，已上传：${loaded}，百分比：${percent}`)
          msgListClone.forEach(item => {
            if (item.guid === guid) {
              item.uploadPercent = Number(percent)
            }
          })
          // console.log("消息列表为：", this.messages)
          this.messages = msgListClone
          return
        }
        if (res.status === uploadStatusMap.complete) {
          const common = this.generatorMessageCommon()
          const newMessage = {
            ...common,
            fileRawName: res.data.name,
            message: res.data.key,
            messageType: res.data.type, // emoji/text/img/file/sys/artboard/audio/video
          }
          // console.log("上传文件后的最新消息为：", newMessage)
          msgListClone.forEach(item => {
            if (item.guid === guid) {
              item.uploading = false
              delete item.uploadPercent
            }
          })
          this.messages = msgListClone
          this.$socket.emit("sendNewMessage", newMessage)
          this.$store.dispatch('news/SET_LAST_NEWS', {
            type: 'edit',
            res: {
              roomId: this.currentConversation.roomId,
              news: newMessage
            }
          })
          this.messageText = ""
        }
      },
      /**
       * 直接获取本地的地址
       */
      getLocalUrl(url, guid) {
        const common = this.generatorMessageCommon()
        const newMessage = {
          ...common,
          uploading: true,
          guid,
          message: url,
          messageType: "img",
        }
        this.messages = [...this.messages, newMessage]
        this.$store.dispatch('news/SET_LAST_NEWS', {
          type: 'edit',
          res: {
            roomId: this.currentConversation.roomId,
            news: newMessage
          }
        })
      },
      createObjURL(file, guid) { //先读取本地文件进行展示，然后等待上传完成
        const url = window.URL.createObjectURL(file)
        // console.log("获取文件本地url为：", url)
        this.getLocalUrl(url, guid)
      },
      uploadImg(e) { //上传图片，默认每次只读取第一个文件
        const guid = genGuid()
        const file = e.target.files[0]
        const isLt1M = file.size / 1024 / 1024 < 1;
        if (!isLt1M) {
          this.$message.error('图片大小不能超过 1MB！请重新选择图片！')
          return
        }
        // console.log("即将要上传到服务器的文件为：", file)
        typeof this.getLocalUrl === 'function' && this.createObjURL(file, guid)
        const formdata = new FormData() //构造表单
        formdata.append('file', file)
        this.$http.uploadFile(formdata).then(res => {
          // console.log('上传文件结果：', res)
          const {data} = res
          if (data.code === 2000) {
            this.getUploadResult({
              status: uploadStatusMap.complete,
              data: {key: data.data.filePath, type: 'img', name: file.name},
              guid
            })
          } else {
            this.$message({type: 'error', message: '上传图片失败！'})
          }
        })
      },
      addEmoji(emoji = '') {
        this.messageText += emoji
      },
      formatRecordTime(seconds) {
        const m = Math.floor(seconds / 60)
        const s = seconds % 60
        return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
      },
      getVoiceMimeType() {
        if (typeof MediaRecorder !== 'undefined' && MediaRecorder.isTypeSupported('audio/webm;codecs=opus')) {
          return 'audio/webm;codecs=opus'
        }
        if (typeof MediaRecorder !== 'undefined' && MediaRecorder.isTypeSupported('audio/webm')) {
          return 'audio/webm'
        }
        return 'audio/mp4'
      },
      async toggleVoiceRecord() {
        if (this.voiceUploading) return
        if (this.isRecording) {
          await this.stopVoiceRecordAndSend()
          return
        }
        await this.startVoiceRecord()
      },
      async startVoiceRecord() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
          this.$message.error('当前浏览器不支持录音')
          return
        }
        try {
          this.recordStream = await navigator.mediaDevices.getUserMedia({audio: true})
          const mimeType = this.getVoiceMimeType()
          this.audioChunks = []
          try {
            this.mediaRecorder = new MediaRecorder(this.recordStream, {mimeType})
          } catch (err) {
            this.mediaRecorder = new MediaRecorder(this.recordStream)
          }
          this.mediaRecorder.ondataavailable = (e) => {
            if (e.data && e.data.size > 0) {
              this.audioChunks.push(e.data)
            }
          }
          this.mediaRecorder.start()
          this.isRecording = true
          this.recordSeconds = 0
          this.recordTimer = setInterval(() => {
            this.recordSeconds++
            if (this.recordSeconds >= 60) {
              this.stopVoiceRecordAndSend()
            }
          }, 1000)
        } catch (err) {
          this.$message.error('无法访问麦克风，请检查权限')
        }
      },
      cleanupVoiceRecord() {
        if (this.recordTimer) {
          clearInterval(this.recordTimer)
          this.recordTimer = null
        }
        if (this.recordStream) {
          this.recordStream.getTracks().forEach(track => track.stop())
          this.recordStream = null
        }
        this.mediaRecorder = null
        this.audioChunks = []
        this.isRecording = false
        this.recordSeconds = 0
      },
      cancelVoiceRecord() {
        if (this.mediaRecorder && this.mediaRecorder.state !== 'inactive') {
          this.mediaRecorder.ondataavailable = null
          this.mediaRecorder.onstop = null
          this.mediaRecorder.stop()
        }
        this.cleanupVoiceRecord()
      },
      stopVoiceRecordAndSend() {
        return new Promise((resolve) => {
          if (!this.mediaRecorder || this.mediaRecorder.state === 'inactive') {
            this.cleanupVoiceRecord()
            resolve()
            return
          }
          const duration = Math.max(1, this.recordSeconds)
          this.mediaRecorder.onstop = async () => {
            const mimeType = this.getVoiceMimeType()
            const ext = mimeType.indexOf('webm') > -1 ? 'webm' : 'mp4'
            const blob = new Blob(this.audioChunks, {type: mimeType.split(';')[0]})
            this.cleanupVoiceRecord()
            if (blob.size < 100 || duration < 1) {
              this.$message.warning('录音时间太短')
              resolve()
              return
            }
            await this.uploadAndSendVoice(blob, duration, ext)
            resolve()
          }
          this.mediaRecorder.stop()
        })
      },
      async uploadAndSendVoice(blob, duration, ext) {
        const fileName = `voice_${Date.now()}.${ext}`
        const file = new File([blob], fileName, {type: blob.type})
        const formdata = new FormData()
        formdata.append('file', file)
        this.voiceUploading = true
        try {
          const res = await this.$http.uploadFile(formdata)
          if (res.data.code !== 2000) {
            this.$message.error('语音上传失败')
            return
          }
          const common = this.generatorMessageCommon()
          const newMessage = {
            ...common,
            fileRawName: fileName,
            message: res.data.data.filePath,
            messageType: MSG_TYPES.voice,
            duration
          }
          this.messages = [...this.messages, newMessage]
          this.$socket.emit('sendNewMessage', newMessage)
          this.$store.dispatch('news/SET_LAST_NEWS', {
            type: 'edit',
            res: {
              roomId: this.currentConversation.roomId,
              news: newMessage
            }
          })
          this.scrollBottom = true
        } catch (err) {
          const errMsg = (err.response && err.response.data && err.response.data.message) || '语音发送失败，请检查服务端是否已启动'
          this.$message.error(errMsg)
        } finally {
          this.voiceUploading = false
        }
      },
      async send(e) {
        e.preventDefault()
        if (!this.messageText) {
          return
        }
        const common = this.generatorMessageCommon()
        let mText = xss(this.messageText)
        //这里就暂时请求一个http get 接口来过滤掉发送的消息，应该换成 post 请求的
        const checkSensitiveMessage = {
          roomId: common.roomId,
          senderId: common.senderId,
          senderName: common.senderName,
          type: common.conversationType,
          message: mText,
          time: fromatTime(new Date())
        }
        await this.$http.filterMessage(checkSensitiveMessage).then(res => {
          if (res.data.code === 2000) {
            mText = res.data.data.message
          }
        })
        const newMessage = {
          ...common,
          message: mText,
          messageType: "text",
        }
        this.messages = [...this.messages, newMessage]
        // console.log("发送的消息是：", newMessage)
        this.$socket.emit("sendNewMessage", newMessage)
        this.$store.dispatch('news/SET_LAST_NEWS', {
          type: 'edit',
          res: {
            roomId: this.currentConversation.roomId,
            news: newMessage
          }
        })
        this.messageText = ""
      },
      joinChatRoom() {
        // console.log("加入房间：", this.currentConversation)
        this.$socket.emit("join", this.currentConversation)
      },
      async getRecentMessages(init = true) {
        /**
         * getRecentMessages 分为两种目前分为两种情况：1、获取两两好友之间的；2、获取群聊
         */
        if (this.isLoading) return // 防止重复发起请求
        this.isLoading = true
        init && this.setLoading(true) // 只有在第一次加载的时候才让ChatArea有loading动画，后面加载时不显示
        const {roomId, conversationType} = this.currentConversation
        // console.log("获取最近的单聊消息，会话参数为：", this.currentConversation)
        const params = {
          roomId,
          pageIndex: this.pageIndex,
          pageSize: this.pageSize
        }
        if (conversationType === conversationTypes.friend) {
          const {data, status} = await this.$http.getRecentSingleMessages(params)
          // console.log("获取最近的单聊信息返回结果为：", data)
          this.setLoading(false)
          if (data.code === 2000 && status === 200) {
            this.isLoading = false
            // reverse() 会改变原数组，并且当前作用域的对象都会改变
            data.data.recentMessage.reverse()
            this.messages = [...data.data.recentMessage, ...this.messages]
            if (data.data.recentMessage.length < this.pageSize) {
              this.hasMore = false
              return
            }
            this.pageIndex++
          }
        } else if (conversationType === conversationTypes.group) {
          const {data, status} = await this.$http.getRecentGroupMessages(params)
          this.setLoading(false)
          this.isLoading = false
          if (data.code === 2000 && status === 200) {
            data.data.recentGroupMessages.reverse()
            this.messages = [...data.data.recentGroupMessages, ...this.messages]
            if (data.data.recentGroupMessages.length < this.pageSize) {
              this.hasMore = false
              return
            }
            this.pageIndex++
          }
        }
      },
      handlerShowEmoji() {
        this.showEmojiCom = false
        this.showUpFileCom = false
      },
      loadmessage() {
        this.scrollBottom = false
        this.useAnimation = true
        if (this.hasMore) {
          this.getRecentMessages(false)
        }
      },
      watchWebRtcMsg() {
        this.$eventBus.$on('web_rtc_msg', (e) => {
          const {type} = e
          // const
          const common = this.generatorMessageCommon()
          const newMessage = {
            ...common,
            message: '',
            messageType: type
          }
          this.messages = [...this.messages, newMessage]
          this.$socket.emit("sendNewMessage", newMessage)
          this.$store.dispatch('news/SET_LAST_NEWS', {
            type: 'edit',
            res: {
              roomId: this.currentConversation.roomId,
              news: newMessage
            }
          })
        })
      },
      /**聊天内容输入框自动聚焦 */
      chatInpAutoFocus() {
        this.$nextTick(() => {
          this.$refs.chatInp.focus();
        })
      }
    },
    components: {
      chatHeader,
      messageList,
      customEmoji,
      groupDesc,
      upFile,
      historyMsg
    },
    watch: {
      currentConversation(newVal, oldVal) {
        // console.log("currentConversation->newVal.id：", newVal.id)
        if (newVal && newVal.id) {
          this.chatInpAutoFocus()
          this.pageIndex = 0
          this.scrollBottom = true
          this.showHistoryMsg = false
          this.setLoading(true)
          this.messageText = ""
          this.messages = []
          this.hasMore = true
          this.joinChatRoom()
          this.getRecentMessages()
          this.datetamp = Date.now()
        }
      }, deep: true, immediate: true
    },
    created() {
      // console.log('chatArea created')
      document.addEventListener('click', this.handlerShowEmoji)
      this.getRecentMessages()
    },
    mounted() {
      this.watchWebRtcMsg() //执行 WebRtc 类型，添加到聊天消息
    },
    beforeDestroy() {
      // console.log('chatArea BeforeDestroy')
      document.removeEventListener('click', this.handlerShowEmoji)
      this.cancelVoiceRecord()
    },
  };
</script>

<style lang="scss">
  @import './../../../static/css/animation.scss';

  .chat-area__com {
    position: relative;
    height: 100%;

    .history-msg-container {
      position: absolute;
      z-index: 1004;
      height: calc(100% - 206px);
      width: 100%;
    }

    .main {
      display: flex;
      position: relative;
      height: calc(100% - 206px);
      width: 100%;
      background-color: #ECECEC;

      .message-list-container {
        position: relative;
        height: 100%;
        width: 75%;
        flex: 1;

        .top-operation {
          position: absolute;
        }
      }

      .group-desc {
        height: 100%;
        width: 25%;
      }
    }

    .main.no-group {
      .message-list-container {
        height: 100%;
        width: 100%;
      }

      .group-desc {
        width: 0%;
      }
    }

    .message-edit-container {
      box-sizing: border-box;
      position: relative;
      height: 150px;
      border-top: 1px solid #E6E6E6;
      background-color: #FFFFFF;

      .tool {
        width: 100%;
        height: 32px;
        line-height: 32px;
        text-align: left;
        background-color: #FFFFFF;
        padding: 0 10px;
        box-sizing: border-box;

        .tool-item {
          cursor: pointer;
          display: inline-block;
          height: 100%;
          position: relative;

          i {
            padding: 0 5px;
          }

          .emoji-container {
            width: 400px;
            height: 260px;
            position: absolute;
            bottom: 30px;
            left: 0;
            z-index: 10;
            transition: all 0.2s;
            /*transform: scaleX(0);*/
            /*opacity: 0;*/
          }

          input {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            opacity: 0;
          }

          .upFileComponent {
            position: absolute;
            left: 13px;
            top: 2px;
          }
        }

        .tool-item:hover {
          background-color: rgba(0, 0, 0, 0.05);
        }

        .tool-item.active {
          background-color: rgba(0, 0, 0, 0.05);
        }

        .tool-item.active .emoji-container {
          transform: scaleX(1);
          opacity: 1;
        }

        i {
          margin: 0;
        }

        .history-btn {
          height: 20px;
          position: absolute;
          top: 7px;
          right: 5px;
          cursor: pointer;
        }

        .export-btn {
          position: absolute;
          top: 4px;
          right: 85px;
          cursor: pointer;
          font-size: 18px;
          color: #8E8E93;
          transition: color 0.2s;

          &:hover {
            color: #2DC100;
          }

          &.is-exporting {
            color: #2DC100;
            cursor: not-allowed;
            opacity: 0.6;
          }
        }
      }

      .operation {
        position: absolute;
        bottom: 5px;
        right: 2px;
      }

      .textarea {
        overflow-x: hidden;
        box-sizing: border-box;
        height: calc(100% - 36px);
        width: 100%;
        outline: none;
        border: 0;
        border-radius: 6px;
        background-color: #F7F7F7;
        padding: 10px;
        resize: none;
        font-size: 14px;
        color: #191919;

        img {
          width: 50px;
        }
      }

      .emoji-component {
        position: absolute;
        bottom: 101%;
      }

      .voice-record-panel {
        position: absolute;
        left: 10px;
        right: 10px;
        bottom: 10px;
        display: flex;
        align-items: center;
        padding: 8px 12px;
        background: rgba(0, 0, 0, 0.75);
        color: #fff;
        border-radius: 6px;
        z-index: 20;

        .record-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background: #f56c6c;
          margin-right: 8px;
          animation: record-blink 1s infinite;
        }

        .record-time {
          font-size: 14px;
          margin-right: 12px;
          min-width: 42px;
        }

        .record-tip {
          flex: 1;
          font-size: 12px;
          opacity: 0.85;
        }
      }
    }
  }

  @keyframes record-blink {
    0%, 100% {
      opacity: 1;
    }
    50% {
      opacity: 0.3;
    }
  }
</style>
