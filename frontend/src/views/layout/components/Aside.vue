<template>
  <div class="layout-cmp__aside">
    <div class="avatar">
      <router-link to="/chat/setting">
        <el-avatar shape="square" :size="50" :src="IMG_URL + userInfo.photo"></el-avatar>
      </router-link>
      <span class="nickname">{{userInfo.nickname}}</span>
    </div>
    <div class="nav-list">
      <div class="nav-item" :class="{ active: asideActive === 'messages' }"
           @click="setActive('messages')" title="消息">
        <i class="el-icon-chat-line-round"/>
      </div>
      <div class="nav-item" :class="{ active: asideActive === 'contacts' }"
           @click="setActive('contacts')" title="通讯录">
        <i class="el-icon-notebook-2"/>
      </div>
    </div>
    <div class="theme-toggle">
      <div class="nav-item" @click="toggleDarkMode" :title="darkMode ? '切换浅色模式' : '切换深色模式'">
        <span class="theme-char">{{ darkMode ? '☼' : '☾' }}</span>
      </div>
    </div>
    <div class="operation">
      <oper-menu @setShowTheme="setShowTheme"/>
    </div>
  </div>
</template>

<script>
  import operMenu from './operMenu'

  export default {
    props: {
      setShowTheme: {
        type: Function
      }
    },
    data() {
      return {
        IMG_URL: process.env.IMG_URL
      }
    },
    computed: {
      userInfo() {
        return this.$store.state.user.userInfo
      },
      asideActive() {
        return this.$store.state.device.asideActive
      },
      darkMode() {
        return this.$store.state.device.darkMode
      }
    },
    methods: {
      setActive(mode) {
        this.$store.dispatch('device/SET_ASIDE_ACTIVE', mode)
        if (this.$route.path !== '/chat/home') {
          this.$router.push('/chat/home')
        }
      },
      toggleDarkMode() {
        this.$store.dispatch('device/TOGGLE_DARK_MODE')
      }
    },
    components: {
      operMenu
    }
  }
</script>

<style lang="scss">
  @import './../../../../static/iconfont/iconfont.css';

  .layout-cmp__aside {
    display: flex;
    position: relative;
    flex-direction: column;
    align-items: center;
    width: 100%;
    height: 100%;
    padding: 10px 0;
    background-color: #2E2E2E;

    .avatar {
      display: flex;
      justify-content: center;
      flex-wrap: wrap;

      .nickname {
        display: inline-block;
        width: 50px;
        margin-top: 10px;
        cursor: pointer;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: #B0B0B0;
        font-size: 12px;
        text-align: center;
      }
    }

    .nav-list {
      margin-top: 30px;

      .nav-item {
        display: flex;
        justify-content: center;
        align-items: center;
        width: 42px;
        height: 42px;
        color: #B0B0B0;
        margin-top: 20px;
        font-size: 22px;
        cursor: pointer;
        transition: color 0.2s, background-color 0.2s;
        border-radius: 8px;

        &:hover {
          color: #FFFFFF;
          background-color: rgba(255, 255, 255, 0.08);
        }

        &.active {
          color: #2DC100;
          background-color: rgba(45, 193, 0, 0.1);
        }
      }
    }

    .about-list {
      margin-top: 150px;
    }

    .theme-toggle {
      position: absolute;
      bottom: 90px;

      .nav-item {
        margin-top: 0;
      }

      .theme-char {
        font-size: 24px;
        line-height: 1;
        color: #FFD54F;
        text-shadow: 0 0 6px rgba(255, 213, 79, 0.3);
      }
    }

    .operation {
      position: absolute;
      bottom: 30px;

      .oper-item {
        font-size: 30px;
        color: #B0B0B0;
        cursor: pointer;
        transition: color 0.2s;

        &:hover {
          color: #2DC100;
        }
      }
    }
  }
</style>
