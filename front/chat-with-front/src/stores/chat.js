import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const messages = ref([])
  const loading = ref(false)
  const sessions = ref([])
  const activeSessionId = ref(null)

  // 每个会话的消息独立存储到 localStorage
  function saveMessages() {
    if (activeSessionId.value) {
      localStorage.setItem('chat-messages-' + activeSessionId.value, JSON.stringify(messages.value))
    }
  }

  function loadMessages(sessionId) {
    const data = localStorage.getItem('chat-messages-' + sessionId)
    messages.value = data ? JSON.parse(data) : []
  }

  // 消息变化自动保存
  watch(messages, () => saveMessages(), { deep: true })

  function addMessage(msg) {
    messages.value.push({
      id: msg.id || Date.now(),
      text: msg.text,
      isUser: msg.isUser,
      messageId: msg.messageId || null,
      audioBase64: msg.audioBase64 || null
    })
  }

  function updateMessageAudio(messageId, audioBase64) {
    const idx = messages.value.findIndex(m => m.messageId === messageId)
    if (idx !== -1) {
      messages.value[idx] = { ...messages.value[idx], audioBase64 }
    }
  }

  function clearMessageAudio(messageId) {
    const idx = messages.value.findIndex(m => m.messageId === messageId)
    if (idx !== -1) {
      messages.value[idx] = { ...messages.value[idx], messageId: null }
    }
  }

  function pollAudio(messageId) {
    const maxRetries = 30
    let retries = 0
    const interval = setInterval(async () => {
      retries++
      try {
        const res = await fetch('/chat-with/audio/' + messageId)
        if (res.status === 200) {
          const data = await res.json()
          updateMessageAudio(messageId, data.audioBase64)
          clearInterval(interval)
        } else if (res.status === 204) {
          // still processing
        } else {
          clearInterval(interval)
        }
      } catch (e) {
        if (retries >= maxRetries) {
          clearMessageAudio(messageId)
          clearInterval(interval)
        }
      }
    }, 2000)
  }

  function tryRecoverAudio(messageId) {
    let retries = 0
    const interval = setInterval(async () => {
      retries++
      try {
        const res = await fetch('/chat-with/audio/' + messageId)
        if (res.status === 200) {
          const data = await res.json()
          updateMessageAudio(messageId, data.audioBase64)
          clearInterval(interval)
        } else if (res.status === 204 && retries >= 3) {
          clearMessageAudio(messageId)
          clearInterval(interval)
        } else if (res.status !== 204) {
          clearMessageAudio(messageId)
          clearInterval(interval)
        }
      } catch (e) {
        clearMessageAudio(messageId)
        clearInterval(interval)
      }
    }, 2000)
  }

  // 恢复未完成的音频
  function recoverAudioForCurrent() {
    for (const msg of messages.value) {
      if (msg.messageId && !msg.audioBase64) {
        tryRecoverAudio(msg.messageId)
      }
    }
  }

  // ---- 会话管理 ----

  async function fetchSessions() {
    try {
      const res = await fetch('/chat-with/sessions')
      if (res.ok) {
        sessions.value = await res.json()
      }
    } catch (e) {
      console.error('获取会话列表失败', e)
    }
  }

  async function createSession(name) {
    try {
      const res = await fetch('/chat-with/sessions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name })
      })
      if (res.ok) {
        const session = await res.json()
        sessions.value.unshift(session)
        await switchSession(session.id)
        return session
      }
    } catch (e) {
      console.error('创建会话失败', e)
    }
    return null
  }

  async function renameSession(id, name) {
    try {
      await fetch('/chat-with/sessions/' + id, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name })
      })
      const s = sessions.value.find(s => s.id === id)
      if (s) s.name = name
    } catch (e) {
      console.error('重命名会话失败', e)
    }
  }

  async function deleteSession(id) {
    try {
      await fetch('/chat-with/sessions/' + id, { method: 'DELETE' })
      sessions.value = sessions.value.filter(s => s.id !== id)
      localStorage.removeItem('chat-messages-' + id)
      if (activeSessionId.value === id) {
        // 切到第一个或重新加载
        await fetchSessions()
        if (sessions.value.length > 0) {
          await switchSession(sessions.value[0].id)
        }
      }
    } catch (e) {
      console.error('删除会话失败', e)
    }
  }

  async function switchSession(id) {
    if (activeSessionId.value === id) return
    try {
      await fetch('/chat-with/sessions/' + id + '/activate', { method: 'PUT' })
    } catch (e) {
      console.error('切换会话失败', e)
    }
    activeSessionId.value = id
    loadMessages(id)
    recoverAudioForCurrent()
  }

  // ---- 发送消息 ----

  async function sendMessage(text) {
    if (!text.trim() || loading.value) return

    addMessage({ text, isUser: true })
    loading.value = true

    try {
      const res = await fetch('/chat-with/send', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ prompt: text })
      })
      const data = await res.json()
      addMessage({
        text: data.reply,
        isUser: false,
        messageId: data.messageId
      })
      if (data.messageId) {
        pollAudio(data.messageId)
      }
    } catch (e) {
      addMessage({ text: '请求失败: ' + e.message, isUser: false })
    } finally {
      loading.value = false
    }
  }

  // ---- 初始化 ----

  async function init() {
    await fetchSessions()

    // 迁移旧数据：如果 session 列表为空但有旧的 chat-history
    if (sessions.value.length === 0) {
      const old = localStorage.getItem('chat-history')
      if (old) {
        try {
          const parsed = JSON.parse(old)
          if (parsed.messages && parsed.messages.length > 0) {
            // 旧格式有消息，但我们没有会话，无法恢复
            // 至少清除旧数据避免混淆
            localStorage.removeItem('chat-history')
          }
        } catch {}
      }
      // 等后端创建默认会话后重新获取
      await fetchSessions()
    }

    // 确保有活跃会话
    if (!activeSessionId.value && sessions.value.length > 0) {
      activeSessionId.value = sessions.value[0].id
      try {
        await fetch('/chat-with/sessions/' + activeSessionId.value + '/activate', { method: 'PUT' })
      } catch {}
    }

    // 加载当前会话的消息
    if (activeSessionId.value) {
      loadMessages(activeSessionId.value)
      recoverAudioForCurrent()
    }
  }

  return {
    messages, loading, sessions, activeSessionId,
    addMessage, sendMessage,
    fetchSessions, createSession, renameSession, deleteSession, switchSession,
    init
  }
}, {
  persist: {
    key: 'chat-sessions',
    storage: localStorage,
    pick: ['sessions', 'activeSessionId']
  }
})
