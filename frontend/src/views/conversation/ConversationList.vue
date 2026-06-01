<template>
  <div class="conversationlist-com">
    <div
      class="search"
      :style="device === 'Mobile' ? {marginLeft: '50px'} : {}">
      <!--      <top-search/>-->
    </div>
    <div class="todo">
      <todo/>
    </div>

    <!-- 消息模式：最近会话列表 -->
    <div v-if="asideActive === 'messages'" class="panel-body messages-panel">
      <recent-conversation-list
        :current-conversation="currentConversation"
        :set-current-conversation="setCurrentConversation"
        @setCurrentConversation="setCurrentConversation"
      />
    </div>

    <!-- 通讯录模式：联系人 + 群聊折叠面板 -->
    <div v-else class="panel-body contacts-panel-wrapper">
      <contacts-panel
        :current-conversation="currentConversation"
        :set-current-conversation="setCurrentConversation"
        @setCurrentConversation="setCurrentConversation"
      />
    </div>
  </div>
</template>

<script>
  import './../../../static/iconfont/iconfont.css'
  import recentConversationList from './RecentConversation'
  import contactsPanel from './ContactsPanel'
  import todo from '@/components/todo'
  import topSearch from './TopSearch'

  export default {
    name: "ConversationListComponent",
    props: {
      currentConversation: Object,
      setCurrentConversation: Function
    },
    data() {
      return {
        conversationList: []
      }
    },
    computed: {
      userInfo() {
        return this.$store.state.user.userInfo
      },
      asideActive() {
        return this.$store.state.device.asideActive
      },
      device() {
        return this.$store.state.device.deviceType
      }
    },
    methods: {
      joinChatRoom() {
        this.conversationList.forEach(item => {
          this.$socket.emit("join", item)
        })
      }
    },
    components: {
      recentConversationList,
      contactsPanel,
      todo,
      topSearch
    },
  }
</script>

<style lang="scss">
  @import './../../../static/css/var.scss';

  .conversationlist-com {
    height: 100%;
    display: flex;
    flex-direction: column;

    .search {
      padding: 10px 10px 0;
    }

    .todo {
      padding: 0 10px 5px;
    }

    .panel-body {
      flex: 1;
      overflow-y: auto;
    }

    .messages-panel {
      padding: 0 10px;

      /* 覆盖 RecentConversation 的固定高度 */
      .recent-conversation-list {
        height: 100% !important;
      }
    }

    .contacts-panel-wrapper {
      overflow: hidden;
    }

    .el-collapse {
      .el-collapse-item__header {
        padding-left: 10px;
      }

      .el-collapse-item__content {
        padding-bottom: 0;
      }
    }
  }
</style>

