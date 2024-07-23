package Tetris.Tetris.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Tetris.Tetris.model.Score;
import Tetris.Tetris.repository.ScoreRepository;

import java.util.List;

/**
 * REST controller for handling score-related requests.
 * Provides endpoints for retrieving top scores.
 */
@RestController
@CrossOrigin(origins = "*")
public class ScoreController {

    private final ScoreRepository scoreRepository;


    /**
     * Constructor for ScoreController.
     * 
     * @param scoreRepository the repository for accessing score data
     */
    @Autowired
    public ScoreController(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

     /**
     * Endpoint to get the top scores.
     * 
     * @return a list of the top scores
     */
    @GetMapping("/api/scores/top")
    public List<Score> getTopScores() {
        return scoreRepository.findTopByOrderByScoreDesc();
    }
}
