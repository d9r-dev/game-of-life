package dev.d9r.gameoflife.game;
import dev.d9r.gameoflife.game.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class GameOfLife {
    private static final Logger LOG = LoggerFactory.getLogger(GameOfLife.class);

    private final GameService gameService;

    private boolean[][] board;
    private boolean running;
    private String sessionId;
    private LocalDateTime lastmodified;

    //TODO: Insert a game template
    public GameOfLife(GameService gameService, String sessionId) {
        this.gameService = gameService;
        this.setBoard(this.gameService.createRandomGame(200, 200));
        this.startGame();
        this.setSessionId(sessionId);
        this.setLastmodified(LocalDateTime.now());
    }

    public void printBoard() {
        System.out.print("\n\n\n\n\n");
        for (boolean[] booleans : getBoard()) {
            for (boolean aBoolean : booleans) {
                if (aBoolean) {
                    System.out.print("#");
                } else {
                    System.out.print(" ");
                }
            }
            System.out.println();
        }
    }

    public void pauseGame() {
        this.setRunning(false);
        this.setLastmodified(LocalDateTime.now());
    }

    public void startGame() {
        this.setRunning(true);
        this.setLastmodified(LocalDateTime.now());
    }

    public void updateBoard() {
        this.setBoard(this.gameService.updateGame(this.board));
    }

    public boolean[][] getBoard() {
        return board;
    }

    public void setBoard(boolean[][] board) {
        this.board = board;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getLastmodified() {
        return lastmodified;
    }

    public void setLastmodified(LocalDateTime lastmodified) {
        this.lastmodified = lastmodified;
    }
}
