<template>
  <div>
    <input v-model="sessionId" />
    <button @click="create">Create</button>
    <button @click="connect">Connect</button>
    <button @click="start">Start</button>
    <button @click="stop">Stop</button>

    <div v-for="row in grid" class="row">
      <div key="row" v-for="cell in row" class="cell" :class="cell ? 'alive' : 'dead'"></div>
    </div>
    <Alert></Alert>
  </div>
</template>

<script lang="ts" setup>
import { useMessagesStore } from '@/stores/messages.ts'

class GameBuffer {
  private buffer: Grid[]
  private readonly renderFps: number
  private isRunning: boolean
  private readonly frameInterval: number
  private lastRenderTime: number
  private readonly maxBufferLength: number
  private rafId: number | null

  constructor(renderFps = 60) {
    this.renderFps = renderFps
    this.isRunning = false
    this.frameInterval = 1000 / this.renderFps
    this.lastRenderTime = performance.now()
    this.buffer = []
    this.maxBufferLength = 120
    this.rafId = null
  }

  addState(grid: Grid) {
    this.buffer.push(grid)

    if (this.buffer.length > this.maxBufferLength) {
      const overflow = this.buffer.length - this.maxBufferLength
      this.buffer = this.buffer.slice(overflow)
      console.log(`Buffer overflow: dropped ${overflow} frame(s)`)
    }

    if (!this.isRunning) {
      this.isRunning = true
      this.rafId = requestAnimationFrame(this.render.bind(this))
    }
  }

  render(timestamp?: number) {
    if (!this.isRunning) {
      return
    }

    if (!timestamp) {
      timestamp = performance.now()
    }

    const elapsed = timestamp - this.lastRenderTime
    if (elapsed >= this.frameInterval) {
      this.lastRenderTime = timestamp - (elapsed % this.frameInterval)

      if (this.buffer.length > 0) {
        const nextState = this.buffer.shift()!
        this.renderFunction(nextState)
      }
    }

    if (this.buffer.length === 0) {
      this.isRunning = false
      this.rafId = null
    } else {
      this.rafId = requestAnimationFrame(this.render.bind(this))
    }
  }

  stop() {
    if (this.rafId !== null) {
      cancelAnimationFrame(this.rafId)
      this.rafId = null
    }
    this.isRunning = false
  }

  renderFunction(nextState: Grid) {
    grid.value = JSON.parse(JSON.stringify(nextState))
  }
}
import { ref } from 'vue'
import Alert from '@/components/__tests__/Alert.vue'
import { decodeBinaryGrid } from '@/util/encode.ts'

type ErrorMessage = {
  error: string
  message: string
}
type CreateMessage = {
  sessionId: string
}
type InitMessage = UpdateData & {
  sessionId: string
  message: string
}

type UpdateData = {
  board: string
  rows: number
  cols: number
}

const grid = ref<Grid>([])
const session = ref<string>('')
const sessionId = ref<string>('')
const eventSource = ref<EventSource>()
const buffer = ref<GameBuffer>(new GameBuffer(60))
const { addMessage } = useMessagesStore()

const create = () => {
  fetch('/game/create/' + sessionId.value, {
    method: 'POST',
  })
    .then((response) => {
      return response.json()
    })
    .then((data: CreateMessage | ErrorMessage) => {
      if ('error' in data) {
        console.error('Could not create game: ' + data.message)
        addMessage('Could not create game: ' + data.message)
      } else {
        session.value = data.sessionId!
        connectToSession(session.value)
      }
    })
}

const connectToSession = (sessionId: string) => {
  session.value = sessionId
  const url = new URL(window.location.href)
  url.searchParams.set('sessionId', sessionId)
  window.history.pushState({}, '', url)

  if (eventSource.value) {
    eventSource.value.close()
  }

  eventSource.value = new EventSource('/stream-game/' + sessionId)

  eventSource.value.addEventListener('INIT', (event) => {
    const data = JSON.parse(event.data) as InitMessage
    grid.value = decodeBinaryGrid(data.board, data.rows, data.cols)
    console.log(data.message)
    sessionId = data.sessionId
  })

  eventSource.value.addEventListener('GAME_UPDATE', (event) => {
    const data: UpdateData = JSON.parse(event.data)
    const state = decodeBinaryGrid(data.board, data.rows, data.cols)
    buffer.value.addState(state)
  })

  eventSource.value.addEventListener('ERROR', (event) => {
    const errorMessage = JSON.parse(event.data)
    console.error('Server error:', errorMessage)
    addMessage('Server error: ' + errorMessage)
  })
}

const connect = () => {
  connectToSession(sessionId.value)
}

const start = () => {
  fetch('/game/' + session.value + '/start', {
    method: 'POST',
  })
    .then((response) => {
      if (response.ok) {
        return response.text()
      }
      throw new Error(response.statusText)
    })
    .then((data) => {
      console.log(data)
      addMessage('Game started')
    })
}

const stop = () => {
  buffer.value.stop()
  fetch('/game/' + session.value + '/stop', {
    method: 'POST',
  })
    .then((response) => {
      if (response.ok) {
        return response.text()
      }
      throw new Error(response.statusText)
    })
    .then(() => {
      addMessage('Game stopped')
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
