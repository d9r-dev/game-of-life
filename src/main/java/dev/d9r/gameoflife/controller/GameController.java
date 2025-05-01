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
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        var emitters = gameManager.getEmitters();

        // Add emitter to the session's list
        emitters.computeIfAbsent(sessionId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        var game = gameManager.getGame(sessionId);
        if (game == null) {
            emitter.completeWithError(new RuntimeException("Game not found"));
        }

        // Send an initial event to establish the connection
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("board", game.getBoard());
            data.put("sessionId", sessionId);
            data.put("message", "Connection established!");
            emitter.send(SseEmitter.event()
                    .name("INIT")
                    .data(data));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    private void removeEmitter(String sessionId, SseEmitter emitter) {
        // Remove emitter from session
        List<SseEmitter> sessionEmitters = gameManager.getEmitters().get(sessionId);
        if (sessionEmitters != null) {
            sessionEmitters.remove(emitter);

            // If this was the last emitter for this session, clean up the session
            if (sessionEmitters.isEmpty()) {
                gameManager.getEmitters().remove(sessionId);
            }
        }
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
            return ResponseEntity.badRequest().build();
        }
    }

}
