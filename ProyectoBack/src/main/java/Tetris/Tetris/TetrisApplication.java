package Tetris.Tetris;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Main application class for the Tetris game application.
 * This class is responsible for bootstrapping the Spring Boot application.
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "Tetris.Tetris.repository")
public class TetrisApplication {
    
    /**
     * Main method that serves as the entry point for the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(TetrisApplication.class, args);
    }
}
