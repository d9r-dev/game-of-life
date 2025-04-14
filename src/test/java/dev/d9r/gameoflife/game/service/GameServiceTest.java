package dev.d9r.gameoflife.game.service;

import dev.d9r.gameoflife.game.service.impl.DefaultGameService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class GameServiceTest {

    @Test
    void getRandomGame() {
        GameService gameService = new DefaultGameService();
        boolean[][] game = gameService.createRandomGame(10, 10);
        assertThat(game).isNotNull();
        assertThat(game.length).isEqualTo(10);
        assertThat(game[1].length).isEqualTo(10);
    }
}
