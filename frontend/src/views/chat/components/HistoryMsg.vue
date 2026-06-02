<template>
  <div class="history-msg-cmp" v-loading="isLoading">
    <div class="history-msg__header">
      <div
        :class="device === 'Mobile' ? 'item type mobile' : 'item type'">
        <el-radio-group v-model="searchType" size="mini" @change="searchTypeChange">
          <el-radio-button value="all" label="全部"></el-radio-button>
          <el-radio-button value="img" label="图片"></el-radio-button>
          <el-radio-button value="file" label="文件"></el-radio-button>
        </el-radio-group>
      </div>
      <div class="item search">
        <el-input
          placeholder="请输入搜索内容"
          prefix-icon="el-icon-search"
          size="mini"
          v-model="searchWord"
          @keydown.native="searchWordChange"
        />
      </div>
      <div
        :class="device === 'Mobile' ? 'item date mobile' : 'item date'">
        <el-date-picker
          v-model="searchDate"
          type="date"
          placeholder="选择日期"
          size="mini"
          @change="searchDateChange"/>
      </div>
    </div>
    <div class="history-msg__body">
      <div class="msg-list-container">
        <div class="msg-item" v-for="item in msgList" :key="item.id">
          <msgItem :msg-item="item"
                   :img-type-msg-list="imgTypeMsgList"
          />
        </div>
      </div>
      <div class="no-data hor-ver-center" v-if="!msgList.length">
        <empty-svg width="200" height="200"/>
        <p style="text-align: center">没有数据~</p>
      </div>
    </div>
    <div class="history-msg__footer">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageIndex"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="pageSize"
        :total="total"
        :layout="paginationArgs"
      />
    </div>
  </div>
</template>

<script>
  // 历史聊天记录查询面板：支持按类型、关键词、日期筛选，并分页展示
  import msgItem from './HistoryMsgItem'
  import emptySvg from '@/SVGComponents/empty'
  import {debounce} from '@/utils'

  // 前端展示文案与后端 type 参数的映射
  const typeTextToValue = {
    '全部': 'all',
    '图片': 'img',
    '文件': 'file'
  }
  export default {
    props: {
      currentConversation: {
        type: Object
      }
    },
    data() {
      return {
        msgList: [],
        searchType: '全部',
        searchWord: '',
        searchDate: '',
        pageIndex: 1,
        pageSize: 10,
        total: 0,
        isLoading: false
      }
    },
    computed: {
      imgTypeMsgList() {
        return (this.msgList || []).filter(item => item.messageType === 'img')
      },
      device() {
        return this.$store.state.device.deviceType
      },
      paginationArgs() {
        if (this.device === 'Mobile') {
          return 'total, sizes, pager'
        } else {
          return 'total, sizes, prev, pager, next, jumper'
        }
      }
    },
    methods: {
      /**
       * 查询历史聊天记录
       * 根据当前会话类型（单聊/群聊）调用不同接口，支持类型、关键词、日期组合筛选
       */
      getHistoryMsg() {
        if (this.isLoading) return
        this.isLoading = true
        const params = {
          roomId: this.currentConversation.roomId,
          type: typeTextToValue[this.searchType],
          query: this.searchWord,
          date: this.searchDate,
          pageIndex: this.pageIndex - 1, // 后端分页从 0 开始
          pageSize: this.pageSize
        }
        // 群聊与单聊共用同一套筛选 UI，接口按会话类型区分
        const fetch = this.currentConversation.isGroup ? this.$http.getGroupHistoryMessages : this.$http.getSingleHistoryMessages
        fetch(params).then(res => {
          this.msgList = res.data.data.msgList
          this.total = res.data.data.total
          this.isLoading = false
        })
      },
      // 切换消息类型（全部/图片/文件）时重置到第一页
      searchTypeChange() {
        this.pageIndex = 1
        this.getHistoryMsg()
      },
      // 关键词搜索防抖，避免每次按键都请求后端
      searchWordChange: debounce(function () {
        this.pageIndex = 1
        this.getHistoryMsg()
      }, 500),
      // 选择日期后按当天范围查询
      searchDateChange() {
        this.pageIndex = 1
        this.getHistoryMsg()
      },
      handleSizeChange(pageSize) {
        this.pageSize = pageSize
        this.getHistoryMsg()
      },
      handleCurrentChange(currentPage) {
        this.pageIndex = currentPage
        this.getHistoryMsg()
      }
    },
    components: {
      msgItem,
      emptySvg
    },
    mounted() {
      this.getHistoryMsg()
    },
  }
</script>

<style lang="scss">
  .history-msg-cmp {
    display: flex;
    flex-direction: column;
    height: 100%;
    padding: 10px 10px 5px;
    background-color: #fff;
    opacity: 1;

    .history-msg__header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      // flex-wrap: wrap;
      .search {
        flex: 1;
        margin: 0 5px;
      }

      .type.mobile {
        .el-radio-button--mini .el-radio-button__inner {
          padding: 7px 7px;
        }
      }

      .date.mobile {
        width: 64px;
        overflow: hidden;

        .el-date-editor.el-input {
          width: 40px;

          // .el-input {
          //   display: none;
          // }
        }
      }
    }

    .history-msg__body {
      position: relative;
      flex: 1;
      overflow-x: hidden;
    }

    .history-msg__footer {
      align-items: center;
    }
  }
</style>
