package dev.d9r.gameoflife.controller;

import dev.d9r.gameoflife.game.GameManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GameController {
    private final GameManager gameManager;

    @Autowired
    public GameController(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @MessageMapping("/start")
    @SendTo("/topic/session")
    public SessionResponse startGame() {
        var sessionId = gameManager.createSession();
        //send sessionId to Client
       return new SessionResponse(sessionId);

    }

    public static class SessionResponse {
        private String sessionId;

        public SessionResponse() {}

        public SessionResponse(String sessionId) {
            this.sessionId = sessionId;
        }


        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }

}
