<template>
  <div class="login-page">
    <div class="ceshi" style="'marginTop': '30px'">
      <el-alert :closable="false" show-icon title="测试账号密码" description="账号1：cc1218，密码1：123456 | 账号2：lt0623，密码2：123456"
                type="success"/>
    </div>
    <div class="wrapper hor-ver-center" :style="device === 'Mobile' ? {width: '90%'}:{}">
      <div class="login-logo">WebChat</div>
      <el-form class="login-form" v-if="isLoginState">
        <div class="form-fields">
          <el-form-item>
            <el-input autocomplete="new-password" v-model="loginInfo.username" prefix-icon="el-icon-user"
                      @keydown.enter="login" placeholder="请输入账号"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input autocomplete="new-password" type="password" v-model="loginInfo.password" prefix-icon="el-icon-lock"
                      placeholder="请输入密码"></el-input>
          </el-form-item>
          <el-form-item class="cv-code">
            <el-input autocomplete="on" class="cv-code-inp" v-model="loginInfo.cvCode" @keydown.enter.native="login"
                      prefix-icon="el-icon-lock" placeholder="验证码(不区分大小写)"></el-input>
            <canvas v-show="!cvCodeIng" width="120" height="44" ref="loginCanvas" @click="getCVCode"></canvas>
            <span style="width: 200px" v-show="cvCodeIng" @click="getCVCode">获取中...</span>
          </el-form-item>
        </div>
        <div class="form-bottom">
          <el-form-item>
            <el-button class="login-btn" type="primary" @click="login">登录</el-button>
          </el-form-item>
          <p style="text-align: center; margin: 0;">
            <span>没有账号？<span class="operation-text" style="display: inline" @click="changeState(false)">注册</span></span>
          </p>
        </div>
      </el-form>
      <el-form class="register-form" v-if="!isLoginState">
        <div class="form-fields">
          <el-form-item>
            <el-input type="text" autocomplete="new-password" v-model="registerInfo.username" prefix-icon="el-icon-user"
                      placeholder="请输入账号"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input type="text" autocomplete="new-password" onfocus="this.type = 'password'"
                      v-model="registerInfo.password" prefix-icon="el-icon-lock" placeholder="请输入密码"></el-input>
          </el-form-item>
          <el-form-item>
            <el-input type="text" autocomplete="new-password" onfocus="this.type = 'password'"
                      v-model="registerInfo.rePassword" prefix-icon="el-icon-lock" placeholder="请确认密码"></el-input>
          </el-form-item>
          <el-form-item class="cv-code">
            <el-input class="cv-code-inp" v-model="registerInfo.cvCode" prefix-icon="el-icon-lock"
                      placeholder="验证码(不区分大小写)"></el-input>
            <canvas width="120" height="44" ref="registerCanvas" @click="getCVCode"></canvas>
          </el-form-item>
        </div>
        <div class="form-bottom">
          <el-form-item>
            <el-button class="login-btn" type="primary" @click="register">注册</el-button>
          </el-form-item>
          <p style="text-align: center; margin: 0;">
            <span>已有账号？<span class="operation-text" style="display: inline" @click="changeState(true)">登录</span></span>
          </p>
        </div>
      </el-form>
    </div>
  </div>
</template>


<script>
  import {createCanvas} from '@/utils/cvcode'
  import canvasImg from './../../static/image/canvas2.jpg'
  import {usernameReg, passwordReg} from '@/utils/index'

  const faceRandom = Math.ceil(Math.random() * 6)
  export default {
    name: 'Login',
    data() {
      return {
        loginInfo: {
          username: '',
          password: '',
          cvCode: '',
          avatar: JSON.parse(window.localStorage.getItem('userInfo') || '{}').photo || ''
        },
        registerInfo: {
          username: '',
          password: '',
          rePassword: '',
          cvCode: '',
          avatar: `face/face${faceRandom}.jpg` //默认为随机值
        },
        cvCode: '', // 验证码
        cvCodeIng: true, // 正在获取验证码？
        isLoginState: true,
        IMG_URL: process.env.IMG_URL
      }
    },
    computed: {
      device() {
        return this.$store.state.device.deviceType
      }
    },
    methods: {
      login() {
        if (!usernameReg.test(this.loginInfo.username)) {
          return this.$message.error('请输入3-6位由数字字母下划线组成的账号')
        }
        if (!passwordReg.test(this.loginInfo.password)) {
          return this.$message.error('请输入6-16位由数字字母组成的密码')
        }
        //获取ip和国家名
        const returnCitySN = window.returnCitySN ? window.returnCitySN : {}
        const params = {
          ...this.loginInfo,
          setting: {
            os: window.OSInfo,
            browser: window.Browser,
            ip: returnCitySN["cip"],
            country: returnCitySN["cname"]
          }
        }
        // console.log("登录的请求参数为：", params)
        this.$http.login(params).then(res => {
          // console.log("登录返回结果为：", res.data)
          let {code, message, data} = res.data
          // 验证码错误或失效重新获取验证码
          if (code === 1007) {
            this.loginInfo.cvCode = ''
            this.getCVCode()
            return
          }
          if (res.status === 200 && code === 1000) {
            this.$message.success('登录成功！')
            this.$store.dispatch('user/LOGIN', data.userInfo)
            const redirect = this.$router.currentRoute.query.redirect
            const next = redirect ? redirect : '/chat/home'
            this.$router.replace(next)
          } else {
            this.$message.error(message)
            if (code === 1200) {
              this.$confirm(`你的${message}，如需恢复请联系管理员：sharezzw@zzw.com`, `通知：${message}`, {
                // confirmButtonText: '确定',
                // cancelButtonText: '取消',
                type: 'error'
              })
            }
          }
        })
      },
      register() {
        if (!usernameReg.test(this.registerInfo.username)) {
          return this.$message.error('请输入3-6位由数字字母下划线组成的账号')
        }
        if (!passwordReg.test(this.registerInfo.password)) {
          return this.$message.error('请输入6-16位由数字字母组成的密码')
        }
        if (this.registerInfo.password !== this.registerInfo.rePassword) {
          return this.$message.error('两次输入的密码不一致')
        }
        this.$http.register(this.registerInfo).then(res => {
          let {code, message} = res.data
          if (code !== 1005) {
            this.$message.error(message)
            code === 1007 ? this.getCVCode() : ''
          } else if (code === 1005) {
            this.$alert(`这是你的chat账号:${res.data.data.userCode}，你可以以此账号登录系统`, '注册成功')
            this.isLoginState = true
            this.getCVCode()
          }
        })
      },
      getCVCode() { // 获取验证码
        this.cvCodeIng = true
        this.$http.getCVCode().then(res => {
          this.cvCode = res.data.data.code
          this.$nextTick(() => {
            const currCanvas = this.isLoginState ? this.$refs.loginCanvas : this.$refs.registerCanvas
            createCanvas(this.cvCode, currCanvas, canvasImg, () => {
              this.cvCodeIng = false
            })
          })
        })
      },
      changeState(flag) {
        this.isLoginState = flag
        this.getCVCode()
      },
    },
    async mounted() {
      this.getCVCode()
    }
  };
</script>

<style lang="scss">
  .login-page {
    @import "./../../static/css/animation.scss";
    @import "./../../static/css/var.scss";
    height: 100vh;
    background: #fff;
    background-repeat: no-repeat;
    background-size: cover;
    transition: all .8s ease;

    .ceshi {
      display: none;
      position: absolute;
      top: 50px;
      left: 50%;
      transform: translateX(-50%);
      margin: 0 auto;
      width: 60%;
    }

    .wrapper {
      display: flex;
      flex-direction: column;
      background-color: #fff;
      width: 420px;
      height: 600px;
      opacity: .95;
      padding: 100px 36px 36px;
      border-radius: 12px;
      box-shadow: 0 4px 24px rgba(0, 0, 0, .12);

      .login-logo {
        text-align: center;
        font-family: "Georgia", "Times New Roman", serif;
        font-size: 32px;
        font-weight: 700;
        letter-spacing: 2px;
        padding-bottom: 30px;
        background: linear-gradient(135deg, #2DC100 0%, #00B4D8 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        user-select: none;
      }

      .login-form, .register-form {
        flex: 1;
        display: flex;
        flex-direction: column;
        position: relative;

        .form-fields {
          flex: 1;
        }

        .form-bottom {
          flex-shrink: 0;
          padding-bottom: 10px;
        }

        .avatar {
          position: absolute;
          z-index: 1001;
          top: -120px;
          left: 50%;
          transform: translateX(-50%);
          text-align: center;

          .el-avatar {
            box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
            border-radius: 12px;
          }
        }

        .el-input__inner {
          height: 44px;
          line-height: 44px;
        }

        .el-button--primary {
          background-color: #2DC100;
          border-color: #2DC100;
          height: 44px;

          &:hover {
            background-color: #25A800;
            border-color: #25A800;
          }
        }
      }

      .cv-code {
        .el-form-item__content {
          display: flex;
          justify-content: space-between;

          .cv-code-inp {
            margin-right: 20px;
          }
        }
      }

      .form-bottom {
        .el-form-item {
          margin-bottom: 12px;
        }
      }

      .login-btn {
        width: 100%;
        height: 44px;
        background-color: #2DC100;
        border-color: #2DC100;

        &:hover {
          background-color: #25A800;
          border-color: #25A800;
        }
      }
    }
  }
</style>
