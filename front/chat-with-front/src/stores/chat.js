import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const messages = ref([])
  const loading = ref(false)

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
      // 替换整个对象触发 persist 插件写 localStorage
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
          // still processing, continue polling
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
    // 刷新后尝试恢复（最多轮询3次，6秒内无结果则放弃）
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

  // 页面刷新后，尝试恢复未完成的音频
  for (const msg of messages.value) {
    if (msg.messageId && !msg.audioBase64) {
      tryRecoverAudio(msg.messageId)
    }
  }

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

  return { messages, loading, sendMessage, addMessage }
}, {
  persist: {
    key: 'chat-history',
    storage: localStorage,
    pick: ['messages']
  }
})
