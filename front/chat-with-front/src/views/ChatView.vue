<script setup>
import { ref, nextTick, onMounted } from 'vue'
import avatar from '@/assets/soro.png'
import { useChatStore } from '@/stores/chat'

const store = useChatStore()
const inputText = ref('')
const showSidebar = ref(true)
const editingId = ref(null)
const editName = ref('')

onMounted(() => {
  store.init()
})

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

function playAudio(base64) {
  const audio = new Audio('data:audio/wav;base64,' + base64)
  audio.play()
}

function onKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function selectSession(id) {
  if (id !== store.activeSessionId) {
    store.switchSession(id)
  }
}

function startRename(session) {
  editingId.value = session.id
  editName.value = session.name
}

function confirmRename() {
  const name = editName.value.trim()
  if (name && editingId.value) {
    store.renameSession(editingId.value, name)
  }
  editingId.value = null
  editName.value = ''
}

function cancelRename() {
  editingId.value = null
  editName.value = ''
}

function onRenameKeydown(e) {
  if (e.key === 'Enter') confirmRename()
  else if (e.key === 'Escape') cancelRename()
}

async function newSession() {
  await store.createSession('新会话')
}

async function removeSession(id) {
  if (!confirm('确定删除该会话？消息将被清除。')) return
  await store.deleteSession(id)
}
</script>

<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside :class="['sidebar', { collapsed: !showSidebar }]">
      <div class="sidebar-header">
        <span v-if="showSidebar">会话列表</span>
        <button class="toggle-btn" @click="showSidebar = !showSidebar">
          {{ showSidebar ? '☰' : '☰' }}
        </button>
      </div>

      <div v-if="showSidebar" class="sidebar-body">
        <button class="new-session-btn" @click="newSession">+ 新会话</button>

        <div class="session-list">
          <div
            v-for="s in store.sessions"
            :key="s.id"
            :class="['session-item', { active: s.id === store.activeSessionId }]"
            @click="selectSession(s.id)"
          >
            <div class="session-name" v-if="editingId !== s.id" @dblclick="startRename(s)">
              {{ s.name }}
            </div>
            <input
              v-else
              v-model="editName"
              class="rename-input"
              @keydown="onRenameKeydown"
              @blur="confirmRename"
              @click.stop
              autofocus
            />
            <button class="delete-btn" @click.stop="removeSession(s.id)" title="删除会话">×</button>
          </div>
        </div>
      </div>
    </aside>

    <!-- 聊天区域 -->
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
            <div v-if="msg.audioBase64" class="play-btn" @click="playAudio(msg.audioBase64)">&#x1f50a; 播放语音</div>
            <div v-else-if="msg.messageId && !msg.isUser" class="audio-pending">语音生成中...</div>
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
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
  background-color: #ededed;
}

/* ---- 侧边栏 ---- */
.sidebar {
  width: 200px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background-color: #2e2e2e;
  color: #ccc;
  transition: width 0.2s;
  overflow: hidden;
}

.sidebar.collapsed {
  width: 40px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 8px;
  font-size: 14px;
  font-weight: bold;
  color: #fff;
  white-space: nowrap;
}

.toggle-btn {
  background: none;
  border: none;
  color: #aaa;
  font-size: 16px;
  cursor: pointer;
  padding: 2px 4px;
  flex-shrink: 0;
}

.toggle-btn:hover {
  color: #fff;
}

.sidebar-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.new-session-btn {
  margin: 6px 8px;
  padding: 8px 0;
  border: 1px dashed #555;
  border-radius: 6px;
  background: none;
  color: #aaa;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s;
}

.new-session-btn:hover {
  border-color: #07c160;
  color: #07c160;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.1s;
  border-left: 3px solid transparent;
}

.session-item:hover {
  background-color: #3a3a3a;
}

.session-item.active {
  background-color: #3a3a3a;
  border-left-color: #07c160;
  color: #fff;
}

.session-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rename-input {
  flex: 1;
  border: 1px solid #07c160;
  border-radius: 3px;
  padding: 2px 6px;
  font-size: 13px;
  background: #fff;
  color: #333;
  outline: none;
  min-width: 0;
}

.delete-btn {
  background: none;
  border: none;
  color: #888;
  font-size: 16px;
  cursor: pointer;
  padding: 0 2px;
  margin-left: 4px;
  flex-shrink: 0;
  visibility: hidden;
}

.session-item:hover .delete-btn {
  visibility: visible;
}

.delete-btn:hover {
  color: #e74c3c;
}

/* ---- 聊天区域 ---- */
.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  max-width: 600px;
  margin: 0 auto;
  min-width: 0;
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

.play-btn {
  margin-top: 8px;
  font-size: 13px;
  color: #576b95;
  cursor: pointer;
  user-select: none;
}

.audio-pending {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.play-btn:hover {
  text-decoration: underline;
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
