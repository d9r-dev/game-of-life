import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useMessagesStore = defineStore('messages', () => {
  const messages = ref<string[]>([])

  function addMessage(message: string) {
    messages.value.push(message)
    setTimeout(() => {
      messages.value.shift()
    }, 3000)
  }

  return { messages, addMessage }
})
