<template>
  <div>
    lelelel
    <button @click="connect">Connect</button>

    <div v-for="row in grid" class="row">
      <div key="row" v-for="cell in row" class="cell" :class="cell ? 'alive' : 'dead'">
      </div>
      </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from "vue";
import SockJS from "sockjs-client";
import {Client, type Message, Stomp} from "@stomp/stompjs";

type Grid = boolean[][];

const grid = ref<Grid>([]);
const session = ref<string>("");

function connect() {
  const socket = ref<WebSocket>(new SockJS("/ws"));
  const stompClient = ref<Client>(Stomp.over(socket.value));

  stompClient.value.activate();

  stompClient.value.onConnect = () => {
    stompClient.value.subscribe("/topic/session", (message: Message) => {
      session.value = JSON.parse(message.body).sessionId;
      console.log("Session: " + session.value);
      stompClient.value.subscribe(
        "/topic/" + session.value,
        (message: Message) => {
          grid.value = JSON.parse(message.body);
          console.log(grid.value);
        }
      );
    });
    stompClient.value.publish({ destination: "/game/start" });
  };
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
}

.row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
}

.cell {
  width: 50px;
  height: 50px;
  border: 2px solid black;
}

.cell.alive {
  background-color: darkred;
}

.cell.dead {
  background-color: white;
}
</style>
