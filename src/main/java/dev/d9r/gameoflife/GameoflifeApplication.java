package dev.d9r.gameoflife;

import dev.d9r.gameoflife.game.GameOfLife;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GameoflifeApplication implements CommandLineRunner {
	private static final Logger LOG = LoggerFactory.getLogger(GameoflifeApplication.class);

	public GameoflifeApplication() {
	}

	public static void main(String[] args) {
		SpringApplication.run(GameoflifeApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.debug("Starting game of life application");
	}
}
