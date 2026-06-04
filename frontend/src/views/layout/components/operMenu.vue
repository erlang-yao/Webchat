<template>
  <div class="oper-menu">
    <el-dropdown placement="left">
      <span class="el-dropdown-link">
        <el-badge is-dot :value="validateUnReadCount" :hidden="!validateUnReadCount">
          <i class="oper-item el-icon-s-operation"></i>
        </el-badge>
      </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item>
          <el-badge
            :value="validateUnReadCount"
            :hidden="!validateUnReadCount">
            <router-link to="/chat/system" class="aside-menu-link">
              <el-button type="text">系统消息</el-button>
            </router-link>
          </el-badge>
        </el-dropdown-item>
        <el-dropdown-item>
          <el-button type="text" @click="logout">退出登录</el-button>
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
  import {removeCookie} from '@/utils/token'

  export default {
    data() {
      return {
      }
    },
    computed: {
      userInfo() {
        return this.$store.state.user.userInfo
      },
      unreadNews() {
        return this.$store.state.news.unreadNews
      },
      validateUnReadCount() {
        const validateSysUser = (this.$store.state.app.sysUsers || []).find(item => item.code === '111111')
        const key = (validateSysUser || {}).sid + '-' + (this.userInfo || {}).uid
        return this.unreadNews[key]
      }
    },
    methods: {
      logout() {
        this.$confirm("确认登出", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }).then(() => {
          this.$message({
            type: "success",
            message: "登出成功！",
          });
          this.$router.replace("/login");
          this.$socket.emit("leave");
          this.$store.dispatch("user/LOGOUT");
          removeCookie(); //删除客户端的 token
        }).catch(() => {
          this.$message({
            type: "info",
            message: "已取消登出！",
          });
        });
      },
      setShowTheme(flag) {
        this.$emit('setShowTheme', flag)
      },
    },
  }
</script>

<style lang="scss">
  .oper-menu {
    .menu-item {
      display: block;
    }
  }
</style>
