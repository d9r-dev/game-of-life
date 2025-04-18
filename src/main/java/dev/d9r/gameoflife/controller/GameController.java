package dev.d9r.gameoflife.controller;

import dev.d9r.gameoflife.game.GameManager;
import dev.d9r.gameoflife.models.SessionMessage;
import dev.d9r.gameoflife.models.SessionRequest;
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
    public SessionMessage startGame(SessionRequest req) {
        var game = gameManager.getGame(req.getSessionId());
        if (game != null && !game.isRunning()) {
            game.startGame();
            return new SessionMessage(req.getSessionId(), false, null);
        }

        if (game == null) {
            return new SessionMessage(null, true, "Game not found");
        }

        return new SessionMessage(null, true, "Game is already running");
    }

    @MessageMapping("/connect")
    @SendTo("/topic/session")
    public SessionMessage connect(SessionRequest req) {
        var game = gameManager.getGame(req.getSessionId());
        if (game != null) {
            return new SessionMessage(req.getSessionId(), false, null);
        }
        return new SessionMessage(null, true, "Game not found");
    }

    @MessageMapping("/create")
    @SendTo("/topic/session")
    public SessionMessage createSession(SessionRequest req) {
        var game = gameManager.getGame(req.getSessionId());
        if (game == null) {
            var sId = gameManager.createSession(req.getSessionId());
            return new SessionMessage(sId, false, null);
        } else {
            return new SessionMessage(null, true, "Session already exists");
        }
    }

    @MessageMapping("/stop")
    public void stopGame(SessionRequest req) {
        gameManager.getGame(req.getSessionId()).pauseGame();
    }

}
