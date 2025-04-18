<template>
  <div>
    lelelel
    <input v-model="sessionId"/>
    <button @click="connect">Connect</button>
    <button @click="stop">Strop</button>

    <div v-for="row in grid" class="row">
      <div key="row" v-for="cell in row" class="cell" :class="cell ? 'alive' : 'dead'">
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import SockJS from "sockjs-client";
import {Client, type Message, Stomp} from "@stomp/stompjs";

type Grid = boolean[][];

const grid = ref<Grid>([]);
const session = ref<string>("");
const sessionId = ref<string>("");
const stompClient = ref<Client>();

function connect() {
  if (sessionId.value === "") return;
  const socket = ref<WebSocket>(new SockJS("/ws"));
  stompClient.value = Stomp.over(socket.value);

  stompClient.value.activate();

  stompClient.value.onConnect = () => {
    stompClient.value?.subscribe("/topic/session", (message: Message) => {
      session.value = JSON.parse(message.body).sessionId;
      console.log("Session: " + session.value);
      stompClient.value?.subscribe(
        "/topic/" + session.value,
        (message: Message) => {
          grid.value = JSON.parse(message.body);
          console.log(grid.value);
        }
      );
    });
    stompClient.value?.publish({
      destination: "/game/start",
      body: JSON.stringify({sessionId: sessionId.value})
    });
  };
}

function stop() {
  stompClient.value?.publish({
    destination: "/game/stop",
    body: JSON.stringify({sessionId: sessionId.value})
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
}

.cell.dead {
  background-color: white;
}
</style>
