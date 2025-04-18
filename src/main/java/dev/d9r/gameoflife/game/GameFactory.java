package dev.d9r.gameoflife.game;

import dev.d9r.gameoflife.game.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GameFactory {
    private final GameService gameService;

    @Autowired
    public GameFactory(GameService gameService) {
        this.gameService = gameService;
    }

    public GameOfLife createGame(String sessionId) {
        return new GameOfLife(gameService, sessionId);
    }
}
