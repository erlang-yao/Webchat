<template>
  <div class="setting-panel-cmp">
    <div class="operation-list">
      <span
        class="oper-item operation-text"
        @click.stop="showFenZu"
        v-if="currentConversation.conversationType === conversationTypes.friend">切换分组</span>
      <span
        class="oper-item operation-text"
        slot="reference"
        @click.stop="showBeiZhu"
        v-if="currentConversation.conversationType === conversationTypes.friend">修改备注</span>
      <span
        class="oper-item operation-text"
        @click.stop="viewFriendProfile"
        v-if="currentConversation.conversationType === conversationTypes.friend">查看资料</span>
      <span
        class="oper-item operation-text"
        @click.stop="showGroupInfo"
        v-if="currentConversation.conversationType === conversationTypes.group">查看群资料</span>
      <span
        class="oper-item operation-text__danger"
        @click.stop="quitGroupOp"
        v-if="currentConversation.conversationType === conversationTypes.group">
        {{ isHolderMsg }}
      </span>
      <!-- 通用操作 -->
      <span class="oper-item operation-text" @click.stop="toggleHistoryMsg">历史聊天记录</span>
      <span class="oper-item operation-text" @click.stop="exportChat">导出聊天记录</span>
      <!-- 删除好友（置底） -->
      <el-popover
        v-if="currentConversation.conversationType === conversationTypes.friend"
        placement="left" width="180" v-model="showDelPop">
        <p style="font-size:13px;margin:0 0 8px;">删除好友后聊天记录也会被删除，是否继续？</p>
        <div style="text-align: right;">
          <el-button size="mini" @click="showDelPop = false">取消</el-button>
          <el-button type="danger" size="mini" @click.stop="deleteFriend">确定</el-button>
        </div>
        <span slot="reference" class="oper-item operation-text__danger" @click.stop="()=>{}">删除好友</span>
      </el-popover>
    </div>

  </div>
</template>

<script>
  import {conversationTypes} from '@/const'

  export default {
    props: ['currentConversation'],
    data() {
      return {
        conversationTypes,
        showDelPop: false
      }
    },
    computed: {
      userInfo() {
        return this.$store.state.user.userInfo
      },
      isHolderMsg() {
        return this.currentConversation.holder === 1 ? "解散群聊" : "退出群聊"
      }
    },
    methods: {
      showGroupInfo() {
        this.$eventBus.$emit('showGroupProfile', {
          show: true,
          data: {
            groupInfo: this.currentConversation.groupInfo,
          },
        })
      },
      quitGroupOp() {
        // console.log("退出群聊，当前群聊会话信息为：", this.currentConversation)
        this.$alert(this.isHolderMsg + '后聊天记录等信息也会被删除，是否继续？', '提示：您正在' + this.isHolderMsg, {
          confirmButtonText: '确定',
          type: 'warning',
          callback: action => {
            //当点击确认按钮后，才去执行请求
            if (action === 'confirm') {
              // console.log("执行了确认请求，准备解散或退出群聊~")
              this.$http.quitGroup({
                holder: this.currentConversation.holder,
                groupId: this.currentConversation.roomId,
                userId: this.currentConversation.userId
              }).then(res => {
                if (res.data.code === 2000) {
                  //发送 socket 通知该群的所有成员
                  this.$socket.emit('sendQuitGroup', this.currentConversation)
                  //重新获取我的群聊
                  this.$eventBus.$emit("getMyGroups")
                  //无论是解散群聊还是退出群聊，都要更新一下我的本地最近会话列表
                  this.$eventBus.$emit("removeRecentGroup", {
                    delCon: this.currentConversation
                  })
                  this.$message({type: 'success', message: this.isHolderMsg + '成功！'})
                } else {
                  this.$message({type: 'error', message: this.isHolderMsg + '失败！'})
                }
              })
            }
          }
        })
      },
      showFenZu() {
        this.$eventBus.$emit('toggleFenZuModal', {
          show: true,
          data: {
            currentConversation: this.currentConversation
          }
        })
      },
      showBeiZhu() {
        this.$eventBus.$emit('toggleBeiZhuModal', {
          show: true,
          data: {
            currentConversation: this.currentConversation
          }
        })
      },
      toggleHistoryMsg() {
        this.$eventBus.$emit('toggleHistoryMsg')
      },
      exportChat() {
        this.$eventBus.$emit('exportChatHistory')
      },
      async viewFriendProfile() {
        const {data} = await this.$http.getUserInfo(this.currentConversation.id)
        if (data.code === 2000) {
          this.$eventBus.$emit('showUserProfile', {
            show: true,
            data: { friendInfo: data.data.userInfo }
          })
        }
      },
      async deleteFriend() {
        const {data} = await this.$http.deleteGoodFriend({
          userM: this.userInfo.uid,
          userY: this.currentConversation.id,
          roomId: this.currentConversation.roomId
        })
        if (data.code === 2000) {
          this.$socket.emit('sendDelGoodFriend', this.currentConversation)
          this.$eventBus.$emit('getMyFriends')
          this.$store.dispatch('app/SET_ALL_FRIENDS', {
            resource: this.currentConversation.id,
            type: 'delete'
          })
          this.showDelPop = false
          this.$message({type: 'success', message: '删除成功！'})
        } else {
          this.$message({type: 'error', message: data.message})
        }
      }
    }
  }
</script>

<style lang="scss">
  .setting-panel-cmp {
    height: 100%;
    background-color: #fff;

    .operation-list {
      height: 100%;
      text-align: center;
      display: flex;
      flex-direction: column;

      .oper-item {
        line-height: 20px;
        margin-top: 10px;
      }

      /* 让 el-popover 的 reference 包装层与 .oper-item 统一样式 */
      .el-popover__reference {
        display: block;
        margin-top: 10px;

        .oper-item {
          display: block;
          width: 100%;
          margin-top: 0;
        }
      }
    }
  }
</style>

