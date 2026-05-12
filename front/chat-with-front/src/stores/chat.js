import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useChatStore = defineStore('chat', () => {
  const messages = ref([])
  const loading = ref(false)

  function addMessage(msg) {
    messages.value.push({
      id: Date.now(),
      text: msg.text,
      isUser: msg.isUser,
      audioBase64: msg.audioBase64 || null
    })
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
      addMessage({ text: data.reply, isUser: false, audioBase64: data.audioBase64 })
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
