package dev.d9r.gameoflife.controller;

import dev.d9r.gameoflife.game.GameManager;
import dev.d9r.gameoflife.game.GameOfLife;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class GameController {
  private final GameManager gameManager;

  @Autowired
  public GameController(GameManager gameManager) {
    this.gameManager = gameManager;
  }

  @GetMapping(path = "/stream-game/{sessionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter streamGameUpdates(@PathVariable String sessionId) {
    // Create emitter with a longer timeout
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    
    var game = gameManager.getGame(sessionId);
    if (game == null) {
      try {
        emitter.send(SseEmitter.event().name("ERROR").data("Game not found"));
        emitter.complete();
      } catch (IOException e) {
        emitter.completeWithError(e);
      }
      return emitter;
    }

    // Register emitter with the game manager
    gameManager.registerEmitter(sessionId, emitter);
    
    // Set up completion callbacks
    emitter.onCompletion(() -> gameManager.removeEmitter(sessionId, emitter));
    emitter.onTimeout(() -> gameManager.removeEmitter(sessionId, emitter));
    emitter.onError(e -> gameManager.removeEmitter(sessionId, emitter));

    // Send an initial event to establish the connection
    try {
      Map<String, Object> data = new HashMap<>();
      data.put("board", game.getBoard());
      data.put("sessionId", sessionId);
      data.put("message", "Connection established!");
      emitter.send(SseEmitter.event().name("INIT").data(data));
    } catch (IOException e) {
      emitter.completeWithError(e);
      return emitter;
    }

    return emitter;
  }

  @PostMapping("/game/{sessionId}/start")
  public ResponseEntity<String> startGame(@PathVariable String sessionId) {
    GameOfLife game = gameManager.getGame(sessionId);
    if (game == null) {
      return ResponseEntity.notFound().build();
    }
    game.startGame();
    return ResponseEntity.ok("Game started for session " + sessionId);
  }

  @PostMapping("/game/{sessionId}/stop")
  public ResponseEntity<String> stopGame(@PathVariable String sessionId) {
    GameOfLife game = gameManager.getGame(sessionId);
    if (game == null) {
      return ResponseEntity.notFound().build();
    }
    game.pauseGame();
    return ResponseEntity.ok("Game stopped for session " + sessionId);
  }

  @PostMapping("/game/create/{sessionId}")
  public ResponseEntity<Map<String, String>> createGame(@PathVariable String sessionId) {
    var game = gameManager.getGame(sessionId);
    String newSessionId;
    if (game == null) {
      newSessionId = gameManager.createSession(sessionId);
      HashMap<String, String> response = new HashMap<>();
      response.put("sessionId", newSessionId);
      return ResponseEntity.ok(response);
    } else {
      HashMap<String, String> response = new HashMap<>();
      response.put("error", "true");
      response.put("message", "Session already exists");
      return ResponseEntity.badRequest().body(response);
    }
  }
}
