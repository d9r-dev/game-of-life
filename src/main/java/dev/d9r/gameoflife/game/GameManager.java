package dev.d9r.gameoflife.game;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameManager {
    private final Map<String, GameOfLife> sessionGames = new ConcurrentHashMap<>();
    private final GameFactory gameFactory;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public GameManager(GameFactory gameFactory, SimpMessagingTemplate simpMessagingTemplate) {
        this.gameFactory = gameFactory;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessionGames.put(sessionId, gameFactory.createGame());
        return sessionId;
    }

    public GameOfLife getGame(String sessionId) {
        return sessionGames.get(sessionId);
    }

    @Scheduled(fixedRate = 1000)
    public void updateGame() {
        sessionGames.values().forEach(GameOfLife::updateBoard);
        sessionGames.keySet().forEach(this::sendMessage);
    }

    private void sendMessage(String sessionId) {
        GameOfLife gameOfLife = sessionGames.get(sessionId);
        simpMessagingTemplate.convertAndSend("/topic/" + sessionId, gameOfLife.getBoard());
    }

}
