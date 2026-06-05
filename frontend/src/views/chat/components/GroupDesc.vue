<template>
  <div class="group-desc-com">
    <div class="notice">
      <div class="notice-header">
        <span class="notice-title">群公告</span>
        <i
          v-if="isGroupOwner && !isEditingNotice"
          class="el-icon-edit notice-edit-icon"
          title="编辑群公告"
          @click.stop="startEditNotice"
        />
      </div>
      <div class="notice-body">
        <div v-if="!isEditingNotice" class="notice-content" @dblclick="isGroupOwner && startEditNotice()">
          {{ groupNotice || '暂无群公告' }}
        </div>
        <div v-else class="notice-edit-area">
          <el-input
            ref="noticeTextarea"
            type="textarea"
            v-model="editNoticeText"
            :rows="4"
            :maxlength="500"
            show-word-limit
            placeholder="请输入群公告内容"
            @keydown.esc.native="cancelEditNotice"
          />
          <div class="notice-edit-actions">
            <el-button size="mini" type="primary" @click="saveNotice" :loading="savingNotice">保存</el-button>
            <el-button size="mini" @click="cancelEditNotice">取消</el-button>
            <el-button size="mini" type="danger" @click="clearNotice" :disabled="!editNoticeText">清空</el-button>
          </div>
        </div>
      </div>
    </div>
    <div class="member">
      <group-user-list :userList="groupMembers"/>
    </div>
  </div>
</template>

<script>
  import GroupUserList from '@/components/customGroupUserList'

  export default {
    name: "GroupDescComponent",
    props: ["currentConversation"],
    data() {
      return {
        groupMembers: [],
        groupNotice: '',
        isEditingNotice: false,
        editNoticeText: '',
        savingNotice: false,
      }
    },
    computed: {
      isGroupOwner() {
        const currentUser = this.$store.state.user.userInfo
        const groupInfo = this.currentConversation.groupInfo
        return currentUser && groupInfo && currentUser.username === groupInfo.holderName
      }
    },
    methods: {
      async fetchGroupInfo() {
        if (this.currentConversation.conversationType === 'GROUP') {
          const groupId = this.currentConversation.groupInfo.gid
          const {data} = await this.$http.getGroupInfo(groupId)
          if (data.code === 2000) {
            this.groupMembers = data.data.users
            this.groupNotice = (data.data.groupInfo && data.data.groupInfo.notice) || ''
          }
        }
      },
      startEditNotice() {
        this.editNoticeText = this.groupNotice || ''
        this.isEditingNotice = true
        this.$nextTick(() => {
          if (this.$refs.noticeTextarea) {
            this.$refs.noticeTextarea.focus()
          }
        })
      },
      cancelEditNotice() {
        this.isEditingNotice = false
        this.editNoticeText = ''
      },
      async saveNotice() {
        if (this.savingNotice) return
        this.savingNotice = true
        try {
          const {data} = await this.$http.updateGroupNotice({
            groupId: this.currentConversation.groupInfo.gid,
            notice: this.editNoticeText
          })
          if (data.code === 2000) {
            this.groupNotice = this.editNoticeText
            if (this.currentConversation.groupInfo) {
              this.currentConversation.groupInfo.notice = this.editNoticeText
            }
            this.isEditingNotice = false
            this.$message({ type: 'success', message: '群公告更新成功' })
          } else {
            this.$message({ type: 'error', message: data.message || '更新失败' })
          }
        } catch (err) {
          this.$message({ type: 'error', message: '请求失败，请稍后重试' })
        } finally {
          this.savingNotice = false
        }
      },
      async clearNotice() {
        this.editNoticeText = ''
        await this.saveNotice()
      },
      handleDocumentClick(e) {
        if (this.isEditingNotice) {
          const noticeEl = this.$el.querySelector('.notice')
          if (noticeEl && !noticeEl.contains(e.target)) {
            this.cancelEditNotice()
          }
        }
      }
    },
    components: {
      GroupUserList
    },
    created() {
      this.fetchGroupInfo()
      document.addEventListener('click', this.handleDocumentClick)
    },
    beforeDestroy() {
      document.removeEventListener('click', this.handleDocumentClick)
    },
  }
</script>

<style lang="scss">
  .group-desc-com {
    height: 100%;
    width: 100%;
    border-left: 1px solid #E6E6E6;

    .notice {
      height: 50%;
      border-bottom: 1px solid #E6E6E6;
      display: flex;
      flex-direction: column;

      .notice-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 12px;
        border-bottom: 1px solid #f0f0f0;
        flex-shrink: 0;

        .notice-title {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
        }

        .notice-edit-icon {
          cursor: pointer;
          color: #909399;
          font-size: 16px;
          &:hover {
            color: #409EFF;
          }
        }
      }

      .notice-body {
        flex: 1;
        overflow-y: auto;
        padding: 10px 12px;

        .notice-content {
          font-size: 13px;
          color: #606266;
          line-height: 1.6;
          white-space: pre-wrap;
          word-break: break-word;
          cursor: default;
        }

        .notice-edit-area {
          .el-textarea {
            margin-bottom: 8px;
          }
          .notice-edit-actions {
            display: flex;
            gap: 6px;
            justify-content: flex-end;
          }
        }
      }
    }

    .member {
      height: 50%;
    }
  }
</style>
