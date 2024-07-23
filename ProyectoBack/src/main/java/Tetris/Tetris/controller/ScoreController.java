package Tetris.Tetris.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Tetris.Tetris.model.Score;
import Tetris.Tetris.repository.ScoreRepository;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ScoreController {

    private final ScoreRepository scoreRepository;

    @Autowired
    public ScoreController(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    @GetMapping("/api/scores/top")
    public List<Score> getTop5Scores() {
        return scoreRepository.findTop5ByOrderByScoreDesc();
    }
}
