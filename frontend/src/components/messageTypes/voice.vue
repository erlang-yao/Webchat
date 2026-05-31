<template>
  <div
    class="message-type__voice"
    :class="{ playing: isPlaying, 'is-me': isMe }"
    :style="{ width: bubbleWidth + 'px' }"
    @click="togglePlay">
    <i :class="isPlaying ? 'el-icon-video-pause' : 'iconfont icon-yuyin'" class="voice-icon"/>
    <span class="voice-duration">{{ displayDuration }}</span>
    <span v-if="isPlaying" class="voice-wave">
      <i v-for="n in 3" :key="n" class="wave-bar"/>
    </span>
    <audio ref="audio" :src="message.message" preload="metadata" @ended="onEnded" @loadedmetadata="onLoaded"/>
  </div>
</template>

<script>
  import './../../../static/iconfont/iconfont.css'

  export default {
    props: ['message'],
    data() {
      return {
        isPlaying: false,
        audioDuration: 0,
        playId: `${this.message.senderId}-${this.message.time}`
      }
    },
    computed: {
      isMe() {
        return this.message.senderId === this.$store.state.user.userInfo.uid
      },
      durationSec() {
        return this.message.duration || this.audioDuration || 0
      },
      displayDuration() {
        const sec = Math.max(1, Math.round(this.durationSec))
        return `${sec}"`
      },
      bubbleWidth() {
        const sec = Math.min(Math.max(this.durationSec, 1), 60)
        return Math.round(80 + (sec / 60) * 120)
      }
    },
    methods: {
      onLoaded() {
        if (!this.message.duration && this.$refs.audio) {
          this.audioDuration = this.$refs.audio.duration
        }
      },
      togglePlay() {
        const audio = this.$refs.audio
        if (!audio) return
        if (this.isPlaying) {
          audio.pause()
          this.isPlaying = false
          return
        }
        this.$eventBus.$emit('voice_play', this.playId)
        audio.play().then(() => {
          this.isPlaying = true
        }).catch(() => {
          this.$message.error('语音播放失败')
        })
      },
      pause() {
        const audio = this.$refs.audio
        if (audio) {
          audio.pause()
          audio.currentTime = 0
        }
        this.isPlaying = false
      },
      onEnded() {
        this.isPlaying = false
      },
      onVoicePlay(playId) {
        if (playId !== this.playId) {
          this.pause()
        }
      }
    },
    mounted() {
      this.$eventBus.$on('voice_play', this.onVoicePlay)
    },
    beforeDestroy() {
      this.pause()
      this.$eventBus.$off('voice_play', this.onVoicePlay)
    }
  }
</script>

<style lang="scss">
  .message-type__voice {
    display: flex;
    align-items: center;
    cursor: pointer;
    user-select: none;
    min-width: 80px;
    padding: 2px 0;

    .voice-icon {
      font-size: 18px;
      margin-right: 6px;
      flex-shrink: 0;
    }

    .voice-duration {
      font-size: 14px;
      white-space: nowrap;
    }

    .voice-wave {
      display: flex;
      align-items: center;
      margin-left: 8px;
      height: 16px;

      .wave-bar {
        display: inline-block;
        width: 3px;
        height: 8px;
        margin-right: 2px;
        background-color: currentColor;
        border-radius: 2px;
        animation: voice-wave 0.8s ease-in-out infinite;

        &:nth-child(2) {
          animation-delay: 0.15s;
        }

        &:nth-child(3) {
          animation-delay: 0.3s;
        }
      }
    }

    &.is-me {
      flex-direction: row-reverse;

      .voice-icon {
        margin-right: 0;
        margin-left: 6px;
        transform: scaleX(-1);
      }

      .voice-wave {
        margin-left: 0;
        margin-right: 8px;
      }
    }

    audio {
      display: none;
    }
  }

  @keyframes voice-wave {
    0%, 100% {
      height: 6px;
    }
    50% {
      height: 14px;
    }
  }
</style>
