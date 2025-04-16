package dev.d9r.gameoflife.game.service.impl;

import dev.d9r.gameoflife.game.service.GameService;
import org.springframework.stereotype.Service;

@Service
public class DefaultGameService implements GameService {
    @Override
    public boolean[][] updateGame(final boolean[][] game) {
        final int height = game[0].length;
        final int width = game.length;
        boolean[][] newGame = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                var leftCoord = x - 1;
                var topCoord = y - 1;
                var rightCoord = x + 1;
                var bottomCoord = y + 1;

                int neighbors = 0;

                if(leftCoord >= 0) {
                    if(game[leftCoord][y]) {
                        neighbors++;
                    }
                    if(topCoord >= 0) {
                        if(game[leftCoord][topCoord]) {
                            neighbors++;
                        }
                    }
                    if(bottomCoord < height) {
                        if(game[leftCoord][bottomCoord]) {
                            neighbors++;
                        }
                    }
                }

                if(rightCoord < width) {
                    if(game[rightCoord][y]) {
                        neighbors++;
                    }
                    if(topCoord >= 0) {
                        if(game[rightCoord][topCoord]) {
                            neighbors++;
                        }
                    }
                    if(bottomCoord < height) {
                        if(game[rightCoord][bottomCoord]) {
                            neighbors++;
                        }
                    }
                }

                if(topCoord >= 0) {
                    if(game[x][topCoord]) {
                        neighbors++;
                    }
                }
                if(bottomCoord < height) {
                    if(game[x][bottomCoord]) {
                        neighbors++;
                    }
                }

                if (game[x][y] && (neighbors == 2 || neighbors == 3)) {
                        newGame[x][y] = true;
                } else newGame[x][y] = !game[x][y] && neighbors == 3;
            }
        }

       return newGame;
    }

    @Override
    public boolean[][] createRandomGame(final Integer width, final Integer height) {
        boolean[][] game = new boolean[width][height];
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                int random = (int) (Math.random() * 2);
                if(random == 1) {
                    game[j][i] = true;
                }
            }
        }
        return game;
    }

}
