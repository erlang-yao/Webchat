<template>
  <div class="history-msg-item-cmp">
    <div class="history-msg-item__header">
      <span class="name normal-font">{{name}}</span>
      <span class="time secondary-font">{{msgItem.time | formatDateToZH}}</span>
    </div>
    <div :class="msgItem.messageType === 'file'?'history-msg-item__content primary-font green':'history-msg-item__content primary-font' ">
      <img v-if="msgItem.messageType === 'img'" :src="msgItem.message" class="historyImage"
           @click.stop="setShowPicturePreview(true)"/>
      <div v-else-if="msgItem.messageType === 'file'" class="fileMessageBox">
        <img src="../../../assets/img/file.png" alt="" class="fileImage">
        <span class="fileWord">
          <div :title="msgItem.fileRawName">{{msgItem.fileRawName.lastIndexOf('.') > 12 ? msgItem.fileRawName.slice(0, 12) +
          '...' + msgItem.fileRawName.slice(msgItem.fileRawName.lastIndexOf('.')) : msgItem.fileRawName}}</div>
          <a class="fileDownload" :href="download">
             下载
            <i class="el-icon-download"></i>
          </a>
        </span>
      </div>
      <span v-else-if="msgItem.messageType === 'voice'" class="voice-history">[语音 {{ msgItem.duration || 1 }}"]</span>
      <span v-else>{{msgItem.message}}</span>
    </div>
    <transition name="fade">
      <picture-preview
        v-if="showPicturePreview"
        :current-img="msgItem.message"
        :img-list="imgUrlList"
        @setshow="setShowPicturePreview"/>
    </transition>
  </div>
</template>

<script>
  // 单条历史消息展示：文本/图片/语音/文件，文件类型支持下载
  import {formatDateToZH} from '@/utils'
  import '../../../../static/css/animation.scss'
  import picturePreview from '@/components/picturePreview'

  export default {
    props: ["msgItem", "imgTypeMsgList"],
    data() {
      return {
        showPicturePreview: false
      }
    },
    methods: {
      setShowPicturePreview(flag) {
        this.showPicturePreview = flag
      },
    },
    computed: {
      /**
       * 文件下载链接
       * message 字段存的是 FastDFS 完整访问路径，需截取 group 开头的 fileId 传给下载接口
       * fileName 用于服务端设置 Content-Disposition，保证下载时使用原始文件名
       */
      download() {
        return "/api/sys/downloadFile?fileId=" + this.msgItem.message.slice(this.msgItem.message.indexOf('group')) + "&fileName=" + this.msgItem.fileRawName
      },
      imgUrlList() {
        return (this.imgTypeMsgList || []).map(item => item.message)
      },
      userInfo() {
        return this.$store.state.user.userInfo
      },
      friendBeiZhu() { // 好友备注Map {id2: '备注1', id1: '备注2'}
        return this.userInfo.friendBeiZhu || {}
      },
      name() {
        const msg = this.msgItem
        const friendBeiZhu = this.friendBeiZhu
        let res = ''
        if (msg.senderId === this.userInfo.uid) {
          res = '我'
        } else {
          res = friendBeiZhu[msg.senderId] ? friendBeiZhu[msg.senderId] : msg.senderNickname
        }
        return res
      }
    },
    filters: {
      formatDateToZH(val) {
        return formatDateToZH(val)
      }
    },
    components: {
      picturePreview
    }
  }
</script>

<style lang="scss">
  .history-msg-item-cmp {
    padding: 5px 0;

    .history-msg-item__content {
      border-radius: 5px;
      margin-top: 5px;
      word-break: break-word;
      .historyImage{
        width: 30%;
      }
    }

    .green{
      background-color: #67C23A;
      border-radius: 5px;
      width: 50%;
    }

    .fileMessageBox {

      display: flex;
      justify-content: flex-start;
      align-items: center;


      .fileImage {
        margin: 10px;
        width: 20%;
      }

      .fileWord {
        /*margin: 17px 0 0 10px;*/
        height: 100%;
        display: flex;
        flex-direction: column;

        .fileDownload {
          display: inline-block;
          color: rgb(15, 66, 255);
          margin-top: 15px;
          font-size: 14px;
          text-decoration: none;
        }
      }
    }
  }

  @media screen and (max-width: 400px) {
    .fileWord > div {
      font-size: 14px;
    }
    .fileDownload {
      margin-top: 6px !important;
      font-size: 12px;
    }
  }
</style>
