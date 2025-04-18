package dev.d9r.gameoflife.controller;

import dev.d9r.gameoflife.game.GameManager;
import dev.d9r.gameoflife.models.SessionMessage;
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
    public SessionResponse startGame(SessionMessage sessionMessage) {
        var game = gameManager.getGame(sessionMessage.getSessionId());
        if (game != null && !game.isRunning()) {
            game.startGame();
            return new SessionResponse(game.getSessionId());
        }
        var sessionId = gameManager.createSession(sessionMessage.getSessionId());
        //send sessionId to Client
       return new SessionResponse(sessionId);
    }

    @MessageMapping("/stop")
    public void stopGame(SessionMessage sessionMessage) {
        gameManager.getGame(sessionMessage.getSessionId()).pauseGame();
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
