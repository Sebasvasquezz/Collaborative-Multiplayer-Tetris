package Tetris.Tetris.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a score entry in the Tetris game.
 */
@Document(collection = "scores")
public class Score {
    
    @Id
    private String id;
    private String name;
    private int score;

    /**
     * Default constructor for creating a new Score object.
     */
    public Score() {}
    
    /**
     * Constructs a new Score object with the specified name and score.
     *
     * @param name the name of the player
     * @param score the score achieved by the player
     */
    public Score(String name, int score) {
        this.name = name;
        this.score = score;
    }
    
    /**
     * Returns the ID of the score.
     *
     * @return the ID of the score
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the score.
     *
     * @param id the ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the player.
     *
     * @return the name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the player.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the score achieved by the player.
     *
     * @return the score achieved by the player
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the score achieved by the player.
     *
     * @param score the score to set
     */
    public void setScore(int score) {
        this.score = score;
    }
}
