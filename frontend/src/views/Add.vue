<template>
  <div class="add-page">
    <!-- 顶部搜索输入框 -->
    <div class="search-header">
      <el-input
        class="search-input"
        placeholder="输入 Chat 账号、用户名、昵称或群名称搜索"
        prefix-icon="el-icon-search"
        v-model="searchKey"
        clearable
        maxlength="20"
        @input="onSearch"
        @clear="onClear">
      </el-input>
    </div>

    <!-- 筛选标签 -->
    <div class="filter-tabs">
      <span
        class="filter-tab"
        :class="{ active: filterType === 'all' }"
        @click="switchFilter('all')">全部</span>
      <span
        class="filter-tab"
        :class="{ active: filterType === 'user' }"
        @click="switchFilter('user')">用户</span>
      <span
        class="filter-tab"
        :class="{ active: filterType === 'group' }"
        @click="switchFilter('group')">群聊</span>
    </div>

    <!-- 搜索结果 -->
    <div class="search-body" v-loading="isFetching">
      <!-- 全部模式：用户 + 群聊，带分组标题 -->
      <template v-if="filterType === 'all'">
        <!-- 用户分组 -->
        <template v-if="userResults.length">
          <div class="section-label">用户</div>
          <div class="result-list">
            <div class="result-item" v-for="item in userResults" :key="'u-' + item.uid">
              <el-avatar class="item-avatar" shape="square" :size="48" :src="IMG_URL + item.photo"/>
              <div class="item-info">
                <span class="item-name" @click="lookUserInfo(item)">{{ item.nickname }}</span>
                <span class="item-detail">Chat号: {{ item.code }}</span>
                <span class="item-detail" v-if="item.signature">{{ item.signature }}</span>
              </div>
              <div class="item-action">
                <el-button
                  v-if="!(myFriends || []).includes(item.uid)"
                  type="primary" size="small" icon="el-icon-plus"
                  @click="openAddUserDialog(item)">添加</el-button>
                <el-button v-else type="info" size="small" disabled>已添加</el-button>
              </div>
            </div>
          </div>
        </template>
        <!-- 群聊分组 -->
        <template v-if="groupResults.length">
          <div class="section-label">群聊</div>
          <div class="result-list">
            <div class="result-item" v-for="item in groupResults" :key="'g-' + item.gid">
              <el-avatar class="item-avatar" shape="square" :size="48" :src="IMG_URL + item.img"/>
              <div class="item-info">
                <span class="item-name" @click="lookGroupInfo(item)">{{ item.title }}</span>
                <span class="item-detail">Chat号: {{ item.code }}</span>
                <span class="item-detail" v-if="item.desc">{{ item.desc }}</span>
              </div>
              <div class="item-action">
                <el-button
                  v-if="!(myGroups || []).includes(item.gid)"
                  type="primary" size="small" icon="el-icon-plus"
                  @click="openAddGroupDialog(item)">添加</el-button>
                <el-button v-else type="info" size="small" disabled>已添加</el-button>
              </div>
            </div>
          </div>
        </template>
        <!-- 全部模式下无结果（仅在有搜索词且两个都为空时） -->
        <div class="no-results" v-if="searchKey && !userResults.length && !groupResults.length && !isFetching">
          未找到匹配的用户或群聊
        </div>
      </template>

      <!-- 仅用户模式 -->
      <template v-if="filterType === 'user'">
        <div class="result-list" v-if="userResults.length">
          <div class="result-item" v-for="item in userResults" :key="'u-' + item.uid">
            <el-avatar class="item-avatar" shape="square" :size="48" :src="IMG_URL + item.photo"/>
            <div class="item-info">
              <span class="item-name" @click="lookUserInfo(item)">{{ item.nickname }}</span>
              <span class="item-detail">Chat号: {{ item.code }}</span>
              <span class="item-detail" v-if="item.signature">{{ item.signature }}</span>
            </div>
            <div class="item-action">
              <el-button
                v-if="!(myFriends || []).includes(item.uid)"
                type="primary" size="small" icon="el-icon-plus"
                @click="openAddUserDialog(item)">添加</el-button>
              <el-button v-else type="info" size="small" disabled>已添加</el-button>
            </div>
          </div>
        </div>
        <div class="no-results" v-if="searchKey && !userResults.length && !isFetching">
          未找到匹配的用户
        </div>
      </template>

      <!-- 仅群聊模式 -->
      <template v-if="filterType === 'group'">
        <div class="result-list" v-if="groupResults.length">
          <div class="result-item" v-for="item in groupResults" :key="'g-' + item.gid">
            <el-avatar class="item-avatar" shape="square" :size="48" :src="IMG_URL + item.img"/>
            <div class="item-info">
              <span class="item-name" @click="lookGroupInfo(item)">{{ item.title }}</span>
              <span class="item-detail">Chat号: {{ item.code }}</span>
              <span class="item-detail" v-if="item.desc">{{ item.desc }}</span>
            </div>
            <div class="item-action">
              <el-button
                v-if="!(myGroups || []).includes(item.gid)"
                type="primary" size="small" icon="el-icon-plus"
                @click="openAddGroupDialog(item)">添加</el-button>
              <el-button v-else type="info" size="small" disabled>已添加</el-button>
            </div>
          </div>
        </div>
        <div class="no-results" v-if="searchKey && !groupResults.length && !isFetching">
          未找到匹配的群聊
        </div>
      </template>

      <!-- 初始空状态（无搜索词） -->
      <div class="no-input-hint" v-if="!searchKey && !isFetching">
        <i class="el-icon-search" style="font-size: 48px; color: #CCC;"/>
        <p>输入关键词搜索用户或群聊</p>
      </div>
    </div>

    <!-- 添加好友弹窗 -->
    <el-dialog title="添加好友" :visible.sync="showUserDialog" width="420px" :close-on-click-modal="false">
      <el-input type="textarea" :rows="3" placeholder="发送添加好友申请" v-model="addUserMsg"/>
      <span slot="footer">
        <el-button @click="showUserDialog = false">取消</el-button>
        <el-button type="primary" @click="sendUserApply" :loading="sendingUserApply">发送</el-button>
      </span>
    </el-dialog>

    <!-- 添加群聊弹窗 -->
    <el-dialog title="申请加群" :visible.sync="showGroupDialog" width="420px" :close-on-click-modal="false">
      <el-input type="textarea" :rows="3" placeholder="发送加群申请" v-model="addGroupMsg"/>
      <span slot="footer">
        <el-button @click="showGroupDialog = false">取消</el-button>
        <el-button type="primary" @click="sendGroupApply" :loading="sendingGroupApply">发送</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
  import {debounce} from '@/utils'
  import {fromatTime} from '@/utils'

  export default {
    name: 'Add',
    data() {
      return {
        searchKey: '',
        filterType: 'all',         // 'all' | 'user' | 'group'
        isFetching: false,
        userResults: [],
        groupResults: [],
        IMG_URL: process.env.IMG_URL,
        myFriends: JSON.parse(window.localStorage.getItem('friends') || '[]'),
        myGroups: JSON.parse(window.localStorage.getItem('groups') || '[]'),
        // 添加好友弹窗
        showUserDialog: false,
        addUserMsg: '',
        selectedUser: null,
        sendingUserApply: false,
        // 添加群聊弹窗
        showGroupDialog: false,
        addGroupMsg: '',
        selectedGroup: null,
        sendingGroupApply: false
      }
    },
    computed: {
      userInfo() {
        return this.$store.state.user.userInfo
      },
      validateSysUser() {
        return (this.$store.state.app.sysUsers || []).find(item => item.code === '111111') || {}
      }
    },
    methods: {
      // ── 搜索 ──────────────────────────
      onSearch: debounce(function () {
        if (!this.searchKey.trim()) {
          this.onClear()
          return
        }
        this.doSearch()
      }, 400),

      onClear() {
        this.searchKey = ''
        this.userResults = []
        this.groupResults = []
      },

      switchFilter(type) {
        this.filterType = type
        // 有搜索词时，切换到对应模式若数据为空则补搜
        if (this.searchKey.trim()) {
          if (type === 'user' && !this.userResults.length) this.searchUsers()
          else if (type === 'group' && !this.groupResults.length) this.searchGroups()
        }
      },

      async doSearch() {
        this.isFetching = true
        try {
          const [users, groups] = await Promise.all([
            this.searchAllUsers(),
            this.searchAllGroups()
          ])
          // 去重：同一用户可能被多个字段检索出来
          this.userResults = this.dedupBy(users, 'uid')
          this.groupResults = this.dedupBy(groups, 'gid')
        } finally {
          this.isFetching = false
        }
      },

      /** 跨 code / username / nickname 搜索用户并去重 */
      async searchAllUsers() {
        const q = this.searchKey.trim()
        const types = ['code', 'username', 'nickname']
        const results = await Promise.all(
          types.map(type =>
            this.$http.preFetchUser({type, searchContent: q, pageIndex: 0, pageSize: 30})
              .then(r => (r.data.code === 2000 ? r.data.data.userList : []))
              .catch(() => [])
          )
        )
        return this.dedupBy(results.flat(), 'uid')
      },

      /** 跨 code / title 搜索群聊并去重 */
      async searchAllGroups() {
        const q = this.searchKey.trim()
        const types = ['code', 'title']
        const results = await Promise.all(
          types.map(type =>
            this.$http.preFetchGroup({type, searchContent: q, pageIndex: 0, pageSize: 30})
              .then(r => (r.data.code === 2000 ? r.data.data.groupList : []))
              .catch(() => [])
          )
        )
        return this.dedupBy(results.flat(), 'gid')
      },

      async searchUsers() {
        this.isFetching = true
        try {
          this.userResults = this.dedupBy(await this.searchAllUsers(), 'uid')
        } finally {
          this.isFetching = false
        }
      },

      async searchGroups() {
        this.isFetching = true
        try {
          this.groupResults = this.dedupBy(await this.searchAllGroups(), 'gid')
        } finally {
          this.isFetching = false
        }
      },

      dedupBy(arr, key) {
        const seen = new Set()
        return arr.filter(item => {
          const v = item[key]
          if (seen.has(v)) return false
          seen.add(v)
          return true
        })
      },

      // ── 查看资料 ──────────────────────
      lookUserInfo(u) {
        this.$eventBus.$emit('showUserProfile', {show: true, data: {friendInfo: u}})
      },
      lookGroupInfo(g) {
        this.$eventBus.$emit('showGroupProfile', {show: true, data: {groupInfo: g}})
      },

      // ── 添加好友 ──────────────────────
      openAddUserDialog(item) {
        this.selectedUser = item
        this.addUserMsg = ''
        this.showUserDialog = true
      },
      async sendUserApply() {
        this.sendingUserApply = true
        try {
          const sys = this.validateSysUser
          const val = {
            roomId: sys.sid + '-' + this.selectedUser.uid,
            senderId: this.userInfo.uid,
            senderName: this.userInfo.username,
            senderNickname: this.userInfo.nickname,
            senderAvatar: this.userInfo.photo,
            receiverId: this.selectedUser.uid,
            time: fromatTime(new Date()),
            additionMessage: this.addUserMsg,
            status: 0,
            validateType: 0
          }
          const {data} = await this.$http.getValidateMessage({
            roomId: val.roomId, status: 0, validateType: 0
          })
          if (data.data.validateMessage && data.data.validateMessage.roomId) {
            this.$message({type: 'warning', message: '您已向该用户发送过好友请求，请等待验证'})
          } else {
            this.$message({type: 'success', message: '好友申请已发送！'})
            this.$socket.emit('sendValidateMessage', val)
            this.showUserDialog = false
          }
        } finally {
          this.sendingUserApply = false
        }
      },

      // ── 添加群聊 ──────────────────────
      openAddGroupDialog(item) {
        this.selectedGroup = item
        this.addGroupMsg = ''
        this.showGroupDialog = true
      },
      async sendGroupApply() {
        this.sendingGroupApply = true
        try {
          const sys = this.validateSysUser
          const val = {
            roomId: sys.sid + '-' + this.selectedGroup.holderUserInfo.uid,
            senderId: this.userInfo.uid,
            senderName: this.userInfo.username,
            senderNickname: this.userInfo.nickname,
            senderAvatar: this.userInfo.photo,
            receiverId: this.selectedGroup.holderUserInfo.uid,
            groupId: this.selectedGroup.gid,
            time: fromatTime(new Date()),
            additionMessage: this.addGroupMsg,
            status: 0,
            validateType: 1
          }
          const {data} = await this.$http.getValidateMessage({
            roomId: val.roomId, status: 0, validateType: 1
          })
          if (data.data.validateMessage && data.data.validateMessage.roomId) {
            this.$message({type: 'warning', message: '您已向该群主发送过加群请求，请等待验证'})
          } else {
            this.$message({type: 'success', message: '加群申请已发送！'})
            this.$socket.emit('sendValidateMessage', val)
            this.showGroupDialog = false
          }
        } finally {
          this.sendingGroupApply = false
        }
      }
    }
  }
</script>

<style lang="scss">
  .add-page {
    height: 100%;
    display: flex;
    flex-direction: column;
    background-color: #F5F5F5;

    .search-header {
      padding: 16px 20px;
      background: #FFFFFF;
      border-bottom: 1px solid #E6E6E6;
    }

    .search-input {
      .el-input__inner {
        border-radius: 8px;
        height: 42px;
        font-size: 15px;
      }
    }

    .filter-tabs {
      display: flex;
      padding: 0 20px;
      background: #FFFFFF;
      border-bottom: 1px solid #E6E6E6;

      .filter-tab {
        padding: 10px 20px;
        font-size: 14px;
        cursor: pointer;
        color: #8E8E93;
        border-bottom: 2px solid transparent;
        transition: color 0.2s, border-color 0.2s;
        user-select: none;

        &:hover { color: #191919; }

        &.active {
          color: #191919;
          font-weight: 600;
          border-bottom-color: #2DC100;
        }
      }
    }

    .search-body {
      flex: 1;
      overflow-y: auto;
      padding: 12px 0;
    }

    /* ── 分组标题 ── */
    .section-label {
      padding: 8px 20px 4px;
      font-size: 12px;
      color: #8E8E93;
      letter-spacing: 0.5px;
    }

    /* ── 结果列表 ── */
    .result-list {
      background: #FFFFFF;
      margin-bottom: 8px;
    }

    .result-item {
      display: flex;
      align-items: center;
      padding: 12px 20px;
      border-bottom: 1px solid #F0F0F0;
      transition: background-color 0.15s;

      &:hover { background-color: #FAFAFA; }
      &:last-child { border-bottom: none; }

      .item-avatar {
        flex-shrink: 0;
        border-radius: 6px;
      }

      .item-info {
        flex: 1;
        margin-left: 14px;
        display: flex;
        flex-direction: column;
        gap: 3px;
        min-width: 0;

        .item-name {
          font-size: 15px;
          color: #191919;
          cursor: pointer;
          font-weight: 500;
          &:hover { color: #2DC100; }
        }

        .item-detail {
          font-size: 12px;
          color: #8E8E93;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .item-action {
        flex-shrink: 0;
        margin-left: 12px;
      }
    }

    /* ── 空状态 ── */
    .no-results {
      text-align: center;
      padding: 40px 0;
      color: #8E8E93;
      font-size: 14px;
    }

    .no-input-hint {
      text-align: center;
      padding: 80px 0 40px;
      color: #B0B0B0;
      p {
        margin-top: 12px;
        font-size: 14px;
      }
    }
  }
</style>
