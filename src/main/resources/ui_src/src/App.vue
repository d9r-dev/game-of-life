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
  </div>
</template>

<script lang="ts" setup>
class GameBuffer {
  private buffer: Grid[]
  private readonly renderFps: number
  private isRunning: boolean
  private readonly frameInterval: number
  private lastRenderTime: number
  private readonly maxBufferLength: number

  constructor(renderFps = 30) {
    this.renderFps = renderFps
    this.isRunning = false
    this.frameInterval = 1000 / this.renderFps
    this.lastRenderTime = performance.now()
    this.buffer = []
    this.maxBufferLength = 60
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
      this.render()
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
    } else {
      requestAnimationFrame(this.render.bind(this))
    }
  }

  renderFunction(nextState: Grid) {
    grid.value = nextState
  }
}
import { ref } from 'vue'

type Grid = boolean[][]
type ErrorMessage = {
  error: string
  message: string
}
type CreateMessage = {
  sessionId: string
}
type InitMessage = {
  board: Grid
  sessionId: string
  message: string
}

const grid = ref<Grid>([])
const session = ref<string>('')
const sessionId = ref<string>('')
const eventSource = ref<EventSource>()
const buffer = ref<GameBuffer>(new GameBuffer(30))

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
        alert('Could not create game: ' + data.message)
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
    grid.value = data.board
    console.log(data.message)
    sessionId = data.sessionId
  })

  eventSource.value.addEventListener('GAME_UPDATE', (event) => {
    buffer.value.addState(JSON.parse(event.data) as Grid)
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
        return response.json()
      }
      throw new Error(response.statusText)
    })
    .then((data) => {
      console.log(data)
    })
}

const stop = () => {
  fetch('/game/' + session.value + '/stop', {
    method: 'POST',
  })
    .then((response) => {
      if (response.ok) {
        return response.json()
      }
      throw new Error(response.statusText)
    })
    .then((data) => {
      console.log(data)
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
