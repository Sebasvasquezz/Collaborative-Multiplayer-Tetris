package Tetris.Tetris.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreTest {

    @Test
    public void testDefaultConstructor() {
        Score score = new Score();
        assertNull(score.getId());
        assertNull(score.getName());
        assertEquals(0, score.getScore());
    }

    @Test
    public void testParameterizedConstructor() {
        String name = "Player1";
        int scoreValue = 100;
        Score score = new Score(name, scoreValue);

        assertNull(score.getId());
        assertEquals(name, score.getName());
        assertEquals(scoreValue, score.getScore());
    }

    @Test
    public void testGetAndSetId() {
        Score score = new Score();
        String id = "12345";
        score.setId(id);

        assertEquals(id, score.getId());
    }

    @Test
    public void testGetAndSetName() {
        Score score = new Score();
        String name = "Player1";
        score.setName(name);

        assertEquals(name, score.getName());
    }

    @Test
    public void testGetAndSetScore() {
        Score score = new Score();
        int scoreValue = 100;
        score.setScore(scoreValue);

        assertEquals(scoreValue, score.getScore());
    }

}
