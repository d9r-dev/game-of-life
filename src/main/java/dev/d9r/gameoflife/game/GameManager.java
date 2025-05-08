package dev.d9r.gameoflife.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameManager {
  private static final Logger LOG = LoggerFactory.getLogger(GameManager.class);
  private final Map<String, GameOfLife> sessionGames = new ConcurrentHashMap<>();

  public Map<String, List<SseEmitter>> getEmitters() {
    return emitters;
  }

  private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
  private final GameFactory gameFactory;

  public GameManager(GameFactory gameFactory) {
    this.gameFactory = gameFactory;
  }

  public String createSession(String sessionId) {
    sessionGames.put(sessionId, gameFactory.createGame(sessionId));
    return sessionId;
  }

  public GameOfLife getGame(String sessionId) {
    return sessionGames.get(sessionId);
  }

  @Scheduled(fixedRate = 250)
  public void updateGame() {
    sessionGames.values().stream().filter(GameOfLife::isRunning).forEach(GameOfLife::updateBoard);
    sessionGames.keySet().stream()
        .filter(sessionId -> sessionGames.get(sessionId).isRunning())
        .forEach(this::sendMessage);
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void cleanupGames() {
    LOG.info("Cleaning up games");
    var deadgames =
        sessionGames.values().stream()
            .filter(
                game ->
                    !game.isRunning()
                        && game.getLastmodified().isBefore(LocalDateTime.now().minusMinutes(10)))
            .toList();

    LOG.info("Cleaning up {} games", deadgames.size());
    deadgames.forEach(
        game -> {
          sessionGames.remove(game.getSessionId());
          emitters.remove(game.getSessionId());
        });
  }

  private void sendMessage(String sessionId) {
    GameOfLife gameOfLife = sessionGames.get(sessionId);
    sendUpdate(sessionId, gameOfLife.getBoard());
  }

  public void sendUpdate(String sessionId, boolean[][] gameState) {
    List<SseEmitter> sessionEmitters = emitters.get(sessionId);
    if (sessionEmitters == null) return;

    List<SseEmitter> deadEmitters = new ArrayList<>();

    for (SseEmitter emitter : sessionEmitters) {
      try {
        emitter.send(SseEmitter.event().name("GAME_UPDATE").data(gameState));
      } catch (IOException e) {
        deadEmitters.add(emitter);
      }
    }

    // Remove any dead emitters
    sessionEmitters.removeAll(deadEmitters);
  }
}
