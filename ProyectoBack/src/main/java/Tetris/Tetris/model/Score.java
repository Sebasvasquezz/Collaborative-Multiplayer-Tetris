package Tetris.Tetris.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "scores")
public class Score {
    
    @Id
    private String id;
    private String name;
    private int score;

    // Constructores, getters y setters
    public Score() {}

    public Score(String name, int score) {
        this.name = name;
        this.score = score;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

