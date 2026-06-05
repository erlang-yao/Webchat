<template>
  <div class="validatenews-com">
    <div class="wrapper">
      <div class="title">验证消息</div>
      <div class="filter">
        <el-tag type="info">选择过滤条件：</el-tag>
        <el-select v-model="filterType" placeholder="请选择">
          <el-option
            label="群"
            :value="1"/>
          <el-option
            label="好友"
            :value="0"/>
        </el-select>
        <el-select v-model="filterStatus" placeholder="请选择">
          <el-option
            label="全部"
            :value="-1"/>
          <el-option
            label="未处理"
            :value="0"/>
          <el-option
            label="已同意"
            :value="1"/>
          <el-option
            label="已拒绝"
            :value="2"/>
        </el-select>
      </div>
      <div class="counter">
      
        <span>
          一共{{outComeList.length}}条记录
        </span>
      </div>
      <div class="validatenews-item" v-for="item in outComeList" :key="item.id">
      <div class="apply-info">
          <span class="title">
            <el-tooltip class="item" effect="dark" content="点击查看用户信息" placement="top">
              <span class="nickname">
                <a style="color: #606266" @click.prevent="lookUserInfo(item.senderId === userInfo.uid ? item.receiverId : item.senderId)">
                  {{ item.senderId === userInfo.uid ? '我' : item.senderNickname.slice(0,10) }}
                </a>
              </span>
            </el-tooltip>
            
            <span v-if="item.validateType === 0">
              {{ item.senderId === userInfo.uid ? '发出的好友申请' : validateNewsTips.applyFriend }}
            </span>
            <span v-else-if="item.validateType === 1">
              {{ item.senderId === userInfo.uid ? '发出的群聊申请' : validateNewsTips.applyGroup }}：{{item.groupInfo && item.groupInfo.title}}
            </span>
            <span class="time">
              {{item.time}}
            </span>
          </span>
        </div>
        <div class="go-operation">
          <el-popover
            placement="left"
            width="300"
            trigger="click">
<div class="validate-popover-body" v-loading="isAdding">
              <div class="sender-info">
                <el-avatar :size="60" :src="IMG_URL + item.senderAvatar" @error="()=>true">
                  <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
                </el-avatar>
                <div class="info">
                  <span class="nickname">{{ item.senderId === userInfo.uid ? '我发出的申请' : item.senderNickname }}</span>
                </div>
              </div>
              <div class="addition">
                <el-tag size="mini" type="info">附加消息：</el-tag>
                <div class="content">
                  <span>{{item.additionMessage}}</span>
                </div>
              </div>
              
              <div class="operation" v-if="item.senderId === userInfo.uid">
                <el-tag type="info" v-if="item.status === 0">等待对方处理</el-tag>
                <el-tag type="success" v-else-if="item.status === 1">对方已同意</el-tag>
                <div v-else-if="item.status === 2">
                  <span style="color: #F56C6C; margin-right: 10px; font-size: 14px;">对方已拒绝</span>
                  <el-button type="primary" size="mini" plain @click="handleResend(item)">重新发送</el-button>
                </div>
              </div>
              
              <div class="operation" v-else>
                <div v-if="item.status === 0">
                  <el-button type="success" plain size="small" @click="agreeValidate(item)">同意</el-button>
                  <el-button type="danger" plain size="small" @click="disagreeValidate(item)">拒绝</el-button>
                </div>
                <div v-else-if="item.status === 1">
                  <el-tag type="success">已同意</el-tag>
                </div>
                <div v-else-if="item.status === 2">
                  <el-tag type="danger">已拒绝</el-tag>
                  </div>
              </div>
              </div>
            <el-button slot="reference" type="success">查看</el-button>
          </el-popover>
        </div>
      </div>
    </div>
  </div>
</template>\

<script>
  import {SET_UNREAD_NEWS_TYPE_MAP} from '@/store/constants'
  import {validateNewsTips} from '@/const'

  export default {
    props: ['validateNewsList'],
    data() {
      return {
        validateNewsTips,
        IMG_URL: process.env.IMG_URL,
        filterType: 0, // 0人，1群
        filterStatus: 0, // -1全部，0未处理，1已同意，2已拒绝
        isAdding: false
      }
    },
    computed: {
      outComeList() {
        // console.log("验证消息列表为：", this.validateNewsList)
        return this.validateNewsList.filter(item => {
          if (this.filterStatus === -1) {
            return item.validateType === this.filterType
          } else {
            return item.status === this.filterStatus && item.validateType === this.filterType
          }
        })
      },
      systemValidateUsers() {
        return this.$store.state.app.sysUsers.filter(item => item.code === "111111")[0]
      },
      userInfo() {
        return this.$store.state.user.userInfo
      },
      device() {
        return this.$store.state.device.deviceType
      }
    },
    methods: {
      // 找个空位把这段完整贴进去，注意和上下方法之间要用逗号 , 隔开
    handleResend(item) {
      this.$prompt('请输入新的验证留言', '重新发送验证消息', {
        confirmButtonText: '发送',
        cancelButtonText: '取消',
        inputValue: item.additionMessage || '你好，我想重新添加你' 
      }).then(({ value }) => {
      
       const params = {
         id: item._id || item.id, // 根据你们实际的 MongoDB 主键名来，通常是 _id 或 id
         additionMessage: value
       };

      // 调用我们在 validate.js 中新增的接口
       this.$http.resendValidateMessage(params).then(res => {
         if (res.data.code === 2000) { 
            this.$message.success('验证消息已重新发送！');
          // 将状态重置为 0 (待处理)，页面会自动隐藏“重新发送”按钮
            item.status = 0; 
           item.additionMessage = value;
          } else {
           this.$message.error(res.data.message || '发送失败');
          }
       });

     }).catch(() => {
      // 点击取消
     });
   },
      async lookUserInfo(uid) {
        let friendInfo = {}
        await this.$http.getUserInfo(uid).then(res => {
          // console.log("查询返回的用户信息为：", res.data.data.userInfo)
          if (res.data.code === 2000) {
            friendInfo = res.data.data.userInfo
          }
        })
        this.$eventBus.$emit('showUserProfile', {
          show: true,
          data: {
            friendInfo: friendInfo
          },
        })
      },
      //同意好友或群聊请求
      async agreeValidate(item) {
        // console.log("同意添加好友或群聊的信息：item", item)
        if (item.validateType === 0) {
          this.$socket.emit('sendAgreeFriendValidate', item)
          this.$eventBus.$emit("getMyFriends") //触发事件去通知不同组件调用对应的函数
          //重新请求一下当前登录的用户信息，放在getMyFriends里面请求
        } else if (item.validateType === 1) {
          this.$socket.emit('sendAgreeGroupValidate', item)
          this.$eventBus.$emit("getMyGroups") //触发事件去通知不同组件调用对应的函数
        }
        this.$store.dispatch('app/SET_AGREE_FRIEND_VALIDATE', true)
        this.isAdding = true
        setTimeout(() => {
          this.isAdding = false
          this.$emit('changeValidateNewsStatus', item, 1)
          this.$alert('添加成功', '提示！', {
            confirmButtonText: '确定'
          });
        }, 500)
      },
      //拒绝好友或群聊请求
      async disagreeValidate(item) {
        // console.log("拒绝添加好友或群聊的信息：item", item)
        if (item.validateType === 0) {
          this.$socket.emit('sendDisAgreeFriendValidate', item)
        } else if (item.validateType === 1) {
          this.$socket.emit('sendDisAgreeGroupValidate', item)
        }
        //改变父组件的状态值
        this.$emit('changeValidateNewsStatus', item, 2)
      }
    },
    mounted() {
      this.$store.dispatch('news/SET_UNREAD_NEWS', {
        roomId: this.systemValidateUsers.sid + '-' + this.userInfo.uid,
        count: 0,
        type: SET_UNREAD_NEWS_TYPE_MAP.clear
      })
    },
  }
</script>

<style lang="scss">
  .validate-popover-body {
    .sender-info {
      display: flex;
      align-items: center;

      .info {
        display: flex;
        flex-direction: column;
        margin-left: 10px;

        .nickname {
          font-size: 20px;
          color: #2DC100;
        }
      }
    }

    .addition {
      margin: 17px 0;

      .content {
        margin-top: 5px;
        font-size: 14px;
      }
    }

    .operation {
      text-align: right;
    }
  }

  .validatenews-com {
    background-color: #fff;
    width: 100%;
    height: 95%;
    overflow-x: hidden;

    .wrapper {
      padding: 10px;
      box-sizing: border-box;

      .filter {
        display: flex;
        align-items: center;
        padding: 10px 0;

        * {
          padding: 0 5px;
        }
      }

      .counter {
        padding: 10px 0;
      }

      .validatenews-item {
        padding: 10px 0;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-top: 1px solid #C0C4CC;

        &:last-child {
          border-bottom: 1px solid #C0C4CC;
        }

        .apply-info {
          .title {
            line-height: 20px;

            .nickname {
              display: inline-block;
            }
          }
        }
      }
    }
  }
</style>
