package dev.d9r.gameoflife.game.service;

public interface GameService {
    boolean[][] updateGame(final boolean[][] game);
    boolean[][] createRandomGame(final Integer width, final Integer height);
}
