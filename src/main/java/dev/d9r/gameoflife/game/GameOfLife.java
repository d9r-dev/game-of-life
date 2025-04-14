package dev.d9r.gameoflife.game;
import dev.d9r.gameoflife.game.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameOfLife {
    private static final Logger LOG = LoggerFactory.getLogger(GameOfLife.class);

    private final GameService gameService;

    private boolean[][] board;

    //TODO: Insert a game template
    public GameOfLife(GameService gameService) {
        this.gameService = gameService;
        this.setBoard(this.gameService.createRandomGame(20, 60));
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

    public void updateBoard() {
        this.setBoard(this.gameService.updateGame(this.board));
    }

    public boolean[][] getBoard() {
        return board;
    }

    public void setBoard(boolean[][] board) {
        this.board = board;
    }
}
