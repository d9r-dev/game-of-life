<script setup lang="ts">
import { ref } from 'vue'
import { useMessagesStore } from '@/stores/messages.ts'
const { messages } = useMessagesStore()
</script>

<template>
  <section class="alert">
    <template v-for="msg in messages.values()" :key="msg">
      <output class="toast">{{ msg }}</output>
    </template>
  </section>
</template>

<style scoped>
.alert {
  position: fixed;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  top: 10%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 9999;
}

.toast {
  display: inline-block;
  padding: 0.5rem 1rem;
  border-radius: 0.25rem;
  font-size: 0.9rem;
  font-weight: bold;
  color: #fff;
  background-color: #28a745;
}
</style>
