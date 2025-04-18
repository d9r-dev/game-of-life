<template>
  <div>
    lelelel
    <input v-model="sessionId" />
    <button @click="create">Create</button>
    <button @click="connect">Connect</button>
    <button @click="start">Start</button>
    <button @click="stop">Stop</button>

    <div v-for="row in grid" class="row">
      <div key="row" v-for="cell in row" class="cell" :class="cell ? 'alive' : 'dead'"></div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from 'vue'
import SockJS from 'sockjs-client'
import { Client, type Message, Stomp } from '@stomp/stompjs'

type Grid = boolean[][]
type ResponseMessage = {
  sessionId: string | null
  error: boolean
  errorMessage: string | null
}

const grid = ref<Grid>([])
const session = ref<string>('')
const sessionId = ref<string>('')
const stompClient = ref<Client>()

onMounted(() => {
  const socket = ref<WebSocket>(new SockJS('/ws'))
  stompClient.value = Stomp.over(socket.value)

  stompClient.value.activate()
  stompClient.value.onConnect = () => {
    stompClient.value?.subscribe('/topic/session', (message: Message) => {
      const response: ResponseMessage = JSON.parse(message.body)
      if (response.error) {
        alert(response.errorMessage)
        return
      }

      session.value = response.sessionId ? response.sessionId : ''
      if (session.value === '') {
        alert('Session not found')
        return
      }
      stompClient.value?.subscribe('/topic/' + session.value, (message: Message) => {
        grid.value = JSON.parse(message.body)
      })
    })
  }
})

function connect() {
  if (sessionId.value === '') {
    alert("Session id can't be empty")
    return
  }
  stompClient.value?.publish({
    destination: '/game/connect',
    body: JSON.stringify({ sessionId: sessionId.value }),
  })
}

function create() {
  stompClient.value?.publish({
    destination: '/game/create',
    body: JSON.stringify({ sessionId: sessionId.value }),
  })
}

function stop() {
  stompClient.value?.publish({
    destination: '/game/stop',
    body: JSON.stringify({ sessionId: sessionId.value }),
  })
}
function start() {
  stompClient.value?.publish({
    destination: '/game/start',
    body: JSON.stringify({ sessionId: sessionId.value }),
  })
}
</script>

<style>
.row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}

.cell {
  flex: 1 0 5px;
  height: 5px;
  border: 1px solid black;
}

.cell.alive {
  background-color: darkred;
  border: 1px solid darkred;
}

.cell.dead {
  background-color: white;
}
</style>
