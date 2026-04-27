<script setup>
import { ref, nextTick } from 'vue'
import avatar from '@/assets/soro.png'
import { useChatStore } from '@/stores/chat'

const store = useChatStore()
const inputText = ref('')

function sendMessage() {
  const text = inputText.value.trim()
  if (!text || store.loading) return

  store.sendMessage(text)
  inputText.value = ''
  scrollToBottom()
}

const messageList = ref(null)
function scrollToBottom() {
  nextTick(() => {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight
    }
  })
}

function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}
</script>

<template>
  <div class="chat-container">
    <div class="message-list" ref="messageList">
      <div
        v-for="msg in store.messages"
        :key="msg.id"
        :class="['message-row', msg.isUser ? 'row-right' : 'row-left']"
      >
        <img v-if="!msg.isUser" class="avatar" :src="avatar" alt="avatar" />

        <div :class="['bubble', msg.isUser ? 'bubble-user' : 'bubble-ai']">
          <div class="bubble-text">{{ msg.text }}</div>
        </div>

        <div v-if="msg.isUser" class="avatar-placeholder"></div>
      </div>

      <div v-if="store.loading" class="message-row row-left">
        <img class="avatar" :src="avatar" alt="avatar" />
        <div class="bubble bubble-ai">
          <div class="typing-indicator">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>
    </div>

    <div class="input-area">
      <input
        v-model="inputText"
        class="input-box"
        type="text"
        placeholder="输入消息..."
        :disabled="store.loading"
        @keydown="onKeydown"
      />
      <button class="send-btn" @click="sendMessage" :disabled="store.loading || !inputText.trim()">
        发送
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 600px;
  margin: 0 auto;
  background-color: #ededed;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 85%;
}

.row-left {
  align-self: flex-start;
}

.row-right {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  flex-shrink: 0;
}

.avatar-placeholder {
  width: 40px;
  flex-shrink: 0;
}

.bubble {
  padding: 10px 14px;
  border-radius: 8px;
  word-break: break-word;
  white-space: pre-wrap;
  line-height: 1.5;
  font-size: 15px;
}

.bubble-user {
  background-color: #95ec69;
  border-top-right-radius: 2px;
}

.bubble-ai {
  background-color: #ffffff;
  border-top-left-radius: 2px;
}

.bubble-text {
  min-height: 1.5em;
}

.input-area {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #f7f7f7;
  border-top: 1px solid #ddd;
}

.input-box {
  flex: 1;
  height: 40px;
  border: none;
  border-radius: 6px;
  padding: 0 12px;
  font-size: 15px;
  outline: none;
  background-color: #fff;
}

.input-box:disabled {
  background-color: #eee;
}

.send-btn {
  flex-shrink: 0;
  height: 40px;
  padding: 0 16px;
  border: none;
  border-radius: 6px;
  background-color: #07c160;
  color: #fff;
  font-size: 15px;
  cursor: pointer;
}

.send-btn:disabled {
  background-color: #b3b3b3;
  cursor: not-allowed;
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 0;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #888;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% { opacity: 0.3; transform: translateY(0); }
  30% { opacity: 1; transform: translateY(-4px); }
}
</style>
