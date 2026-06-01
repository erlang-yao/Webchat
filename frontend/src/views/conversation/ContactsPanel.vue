<template>
  <div class="contacts-panel">
    <!-- 顶部操作栏 -->
    <div class="contacts-actions">
      <div class="action-item" @click="goAddFriend">
        <i class="el-icon-user-solid"/>
        <span>添加好友/群聊</span>
      </div>
      <div class="action-item" @click="createGroup">
        <i class="el-icon-circle-plus-outline"/>
        <span>创建群聊</span>
      </div>
    </div>

    <!-- 添加分组（折叠式） -->
    <div v-show="showAddFenZu" class="add-fenzu-row space-bw">
      <el-input size="small" v-model="newFenZuName" placeholder="请输入分组名" style="marginRight: 5px"/>
      <el-button size="small" type="success" @click="addNewFenZuItem" :loading="isAdding">添加</el-button>
    </div>

    <!-- 联系人区块 -->
    <div class="contact-section">
      <div class="section-header" @click="contactExpanded = !contactExpanded">
        <i :class="contactExpanded ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"/>
        <span>联系人 ({{ totalFriendCount }})</span>
      </div>
      <div v-show="contactExpanded" class="section-body">
        <fenzu-conversation-list
          :current-conversation="currentConversation"
          :set-current-conversation="setCurrentConversation"
          @setCurrentConversation="onSetConversation"
        />
      </div>
    </div>

    <!-- 群聊区块 -->
    <div class="group-section">
      <div class="section-header" @click="groupExpanded = !groupExpanded">
        <i :class="groupExpanded ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"/>
        <span>群聊 ({{ totalGroupCount }})</span>
      </div>
      <div v-show="groupExpanded" class="section-body">
        <group-conversation-list
          :current-conversation="currentConversation"
          :set-current-conversation="setCurrentConversation"
          @setCurrentConversation="onSetConversation"
        />
      </div>
    </div>
  </div>
</template>

<script>
  import './../../../static/iconfont/iconfont.css'
  import fenzuConversationList from './FenzuConversation'
  import groupConversationList from './GroupConversation'

  export default {
    name: 'ContactsPanel',
    props: {
      currentConversation: Object,
      setCurrentConversation: Function
    },
    data() {
      return {
        contactExpanded: true,
        groupExpanded: false,
        showAddFenZu: false,
        newFenZuName: '',
        isAdding: false
      }
    },
    computed: {
      userInfo() {
        return this.$store.state.user.userInfo
      },
      allFriends() {
        return this.$store.state.app.allFriends || []
      },
      allConversation() {
        return this.$store.state.app.allConversation || []
      },
      totalFriendCount() {
        return this.allFriends.length
      },
      totalGroupCount() {
        // 从 allConversation 中筛选群聊
        return this.allConversation.filter(item => item.isGroup).length
      },
      friendFenZu() {
        return Object.keys(this.userInfo.friendFenZu || {})
      }
    },
    methods: {
      onSetConversation(data) {
        this.$emit('setCurrentConversation', data)
      },
      goAddFriend() {
        this.$router.push('/chat/add')
      },
      createGroup() {
        this.$eventBus.$emit('toggleCreateGroup', {show: true})
      },
      async addNewFenZuItem() {
        if (!this.newFenZuName.trim()) return
        if (this.friendFenZu.includes(this.newFenZuName.trim())) {
          this.$message({type: 'warning', message: '已有该分组'})
          this.newFenZuName = ''
          return
        }
        this.isAdding = true
        const params = {
          fenZuName: this.newFenZuName.trim(),
          userId: this.userInfo.uid
        }
        const {data} = await this.$http.addNewFenZu(params)
        if (data.code !== 2000) {
          this.$message({message: data.message, type: 'warning'})
        }
        this.newFenZuName = ''
        const res = await this.$http.getUserInfo(this.userInfo.uid)
        this.isAdding = false
        this.showAddFenZu = false
        this.$store.dispatch('user/LOGIN', res.data.data.userInfo)
      }
    },
    components: {
      fenzuConversationList,
      groupConversationList
    }
  }
</script>

<style lang="scss">
  .contacts-panel {
    height: 100%;
    overflow-y: auto;
    background-color: #F7F7F7;

    .contacts-actions {
      display: flex;
      padding: 10px 12px;
      gap: 8px;
      border-bottom: 1px solid #ECECEC;

      .action-item {
        flex: 1;
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;
        padding: 10px 8px;
        background-color: #FFFFFF;
        border-radius: 8px;
        cursor: pointer;
        color: #191919;
        font-size: 13px;
        transition: background-color 0.2s;

        &:hover {
          background-color: #F0F0F0;
        }

        i {
          font-size: 16px;
          color: #2DC100;
        }
      }
    }

    .add-fenzu-row {
      padding: 8px 12px;
      background-color: #FFFFFF;
      border-bottom: 1px solid #ECECEC;
    }

    .section-header {
      display: flex;
      align-items: center;
      padding: 12px 16px;
      background-color: #FFFFFF;
      border-bottom: 1px solid #ECECEC;
      cursor: pointer;
      font-size: 14px;
      font-weight: 500;
      color: #191919;
      transition: background-color 0.15s;
      user-select: none;

      &:hover {
        background-color: #F7F7F7;
      }

      i {
        margin-right: 8px;
        font-size: 12px;
        color: #8E8E93;
      }
    }

    .section-body {
      background-color: #FFFFFF;

      /* 覆盖 FenzuConversation / GroupConversation 的固定高度 */
      .fenzu-conversation-list,
      .group-conversation-list {
        height: auto !important;
        max-height: none !important;
        overflow-y: visible !important;
      }
    }
  }
</style>
