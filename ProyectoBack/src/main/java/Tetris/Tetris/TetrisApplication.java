package Tetris.Tetris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "Tetris.Tetris.repository")
public class TetrisApplication {
    public static void main(String[] args) {
        SpringApplication.run(TetrisApplication.class, args);
    }
}
