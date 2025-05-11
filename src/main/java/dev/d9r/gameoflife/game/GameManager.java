package dev.d9r.gameoflife.game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class GameManager {
  private static final Logger LOG = LoggerFactory.getLogger(GameManager.class);
  private final Map<String, GameOfLife> sessionGames = new ConcurrentHashMap<>();
  private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

  public Map<String, List<SseEmitter>> getEmitters() {
    return emitters;
  }

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

  public synchronized void registerEmitter(String sessionId, SseEmitter emitter) {
    LOG.info("Registering emitter for session: {}", sessionId);
    emitters.computeIfAbsent(sessionId, id -> new CopyOnWriteArrayList<>()).add(emitter);
  }

  public synchronized void removeEmitter(String sessionId, SseEmitter emitter) {
    LOG.info("Removing emitter for session: {}", sessionId);
    List<SseEmitter> sessionEmitters = emitters.get(sessionId);
    if (sessionEmitters != null) {
      sessionEmitters.removeIf(e -> e == emitter);

      // If this was the last emitter for this session, consider cleaning up
      if (sessionEmitters.isEmpty()) {
        LOG.info("No more emitters for session: {}", sessionId);
        emitters.remove(sessionId);
      }
    }
  }

  public void sendUpdate(String sessionId, boolean[][] gameState) {
    List<SseEmitter> sessionEmitters = emitters.get(sessionId);
    if (sessionEmitters == null || sessionEmitters.isEmpty()) return;

    List<SseEmitter> deadEmitters = new ArrayList<>();

    // Add timestamp to help with client-side rendering
    Map<String, Object> payload = new HashMap<>();
    payload.put("timestamp", System.currentTimeMillis());
    payload.put("state", gameState);

    for (SseEmitter emitter : sessionEmitters) {
      try {
        emitter.send(SseEmitter.event().name("GAME_UPDATE").data(payload));
      } catch (IOException e) {
        LOG.error("Failed to send update to emitter: {}", e.getMessage());
        deadEmitters.add(emitter);
      }
    }

    // Remove any dead emitters
    if (!deadEmitters.isEmpty()) {
      LOG.info("Removing {} dead emitters for session: {}", deadEmitters.size(), sessionId);
      sessionEmitters.removeAll(deadEmitters);

      // If this was the last batch of emitters, clean up
      if (sessionEmitters.isEmpty()) {
        emitters.remove(sessionId);
      }
    }
  }
}
